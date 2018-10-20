/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 工具函数集。
 * 
 * 依赖:
 * jquery.js
 * jquery-ui.js
 * datagear-model.js
 */

(function($, undefined)
{
	$.extend(
	{
		/**
		 * 打开给定URL页面。
		 * 
		 * @param url 请求的URL。
		 * @param options 选项，格式如下：
		 * 			{
		 * 				//可选，打开目标：document.body 页面内jquery对话框；"_blank" 新网页；"_file" 文件下载
		 * 				target : document.body,
		 * 				
		 *				//可选，传递给新页面的参数，可以在目标页面通过$.pageParam(dom)获取
		 * 				pageParam : undefined,
		 * 
		 * 				//是否在titlebar添加pin按钮
		 * 				pinTitleButton : false || true,
		 * 				
		 * 				//其他$.ui.dialog参数
		 * 				...
		 * 
		 * 				//其他$.ajax参数
		 * 				...
		 * 			}
		 */
		open : function(url, options)
		{
			if(options && (options.target == "_blank" || options.target == "_file"))
			{
				if(options.target == "_blank")
				{
					//使用window.open()如果options.data很大的话会使URL超长导致请求失败，因而改为postOnForm
					
					//url = url + (options.data ? "?" + $.param(options.data) : "");
					//window.open(url);
					
					$.postOnForm(url, {"data" : options.data, "target" : "_blank"});
				}
				else
				{
					//使用<a>标签如果options.data很大的话会使URL超长，因而改为postOnForm()方案
					
					/*
					url = url + (options.data ? "?" + $.param(options.data) : "");
					
					//对于文件下载，采用window.open会使浏览器闪烁，而采用<a>标签不会
					var $fileLink = $("#_file_download_link");
					if($fileLink.length == 0)
					{
						$fileLink = $("<a />").attr("id", "_file_download_link")
							.css("width", "0px").css("height", "0px").appendTo(document.body);
					}
					
					$fileLink.attr("href", url);
					$fileLink[0].click();//$fileLink.click()无法触发下载动作
					*/
					
					$.postOnForm(url, {"data" : options.data});
				}
			}
			else
			{
				options = $.extend(
					{
						title : undefined, 
						target : document.body,
						pageParam : undefined,
						pinTitleButton : false,
						modal : true,
						classes: { "ui-dialog": "ui-corner-all ui-widget-shadow" }
					}, options);
				
				var ajaxOptions =
				{
					data : (options.data ? options.data : undefined),
					success : function(data, textStatus, jqXHR)
					{
						var container=$(options.target ? options.target : document.body);
						
						var $dialog = $("<div id='dialog-"+new Date().getTime()+"' class='dialog-content-container'></div>").appendTo(container);
						
						if(options.pageParam)
							$dialog.data("pageParam", options.pageParam);
						
						$._dialog($dialog, options);
						$dialog.html(data);
						
						if(!options.title)
						{
							var title = $("> title", $dialog).text();
							$dialog.dialog( "option", "title", title);
						}
					},
					type : "POST"
				};
				
				$.ajax(url, ajaxOptions);
			}
		},
		
		/**
		 * 获取由$.open打开的页面所传递的页面参数。
		 * 如果没有将返回null。
		 * 
		 * @param dom 任意dom元素
		 */
		pageParam : function(dom)
		{
			var $dom = $(dom);
			
			if($dom.hasClass("dialog-content-container"))
				return $dom.data("pageParam");
			else
			{
				if($dom.is(document.body))
					return null;
				else
					return $.pageParam($dom.parent());
			}
		},
		
		/**
		 * 构建与此组件关联的对话框。
		 * 
		 * @param options 对话框选项集。
		 */
		_dialog : function(element, options)
		{
			options = $.extend(
			{
				//是否
				pin : true,
				
				close : function(event)
				{
					if(!event.isHideDialog)
						$(this).dialog("destroy").remove();
				},
				
				position: {my: "center top", at: "center top+6%"},
				
				width : "73%"
			},
			options);
			
			//处理百分比高度
			if(options.height && typeof(options.height)=="string" && options.height.charAt(options.height.length - 1)=="%")
				options.height = parseInt(options.height.substr(0, options.height.length - 1)) * $(window).height() / 100;
			
			element.dialog(options);
			
			if(options.pinTitleButton)
			{
				var titlebar = $(".ui-dialog-titlebar", $(element).dialog("widget"));
				
				var pinButton = $("<button type='button'></button>")
				.button({
					label: $( "<a>" ).text( "pin" ).html(),
					icon: "ui-icon-pin-s",
					showLabel: false
				})
				.appendTo( titlebar )
				.addClass("dialog-titlebar-pin stated-active")
				.click(function(event)
				{
					var $this = $(this);
					
					if($this.hasClass("ui-state-active"))
						$(this).removeClass("ui-state-active");
					else
						$(this).addClass("ui-state-active");
				});
			}
		},
		
		/**
		 * 判断给定dom元素是否在对话框中。
		 * 
		 * @param dom 任意DOM元素
		 */
		isInDialog : function(dom)
		{
			var myDialog = $.getInDialog(dom);
			
			return (myDialog && myDialog.length > 0);
		},
		
		/**
		 * 获取元素所处的对话框DOM对象，如果不在对话框中，返回一个空的Jquery对象（长度为0）。
		 */
		getInDialog : function(dom)
		{
			var dialog = $(dom).closest(".dialog-content-container");
			
			return dialog;
		},
		
		/**
		 * 关闭并销毁对话框。
		 */
		closeDialog : function($dialog)
		{
			$dialog.dialog("destroy").remove();
		},
		
		/**
		 * 对话框是否设置为钉住。
		 */
		isDialogPinned : function($dialog)
		{
			var titlebar = $(".ui-dialog-titlebar", $dialog.dialog("widget"));
			
			var pinButton = titlebar.find(".dialog-titlebar-pin");
			
			if(pinButton.length > 0 && pinButton.hasClass("ui-state-active"))
				return true;
			else
				return false;
		},
		
		/**
		 * 转换为表单提交。
		 * 此方法会把数据写入表单参数，然后提交表单。
		 * 
		 * @param url
		 * @param options
		 * 			{
		 * 				//表单参数对象
		 * 				data : undefined,
		 * 				
		 * 				//表单提交目标
		 * 				target : undefined
		 * 			}
		 */
		postOnForm : function(url, options)
		{
			options = $.extend({ data : {}, target : ""}, options);
			
			var $form = $("#___post_on_form___");
			if($form.length == 0)
			{
				$form = $("<form />").attr("id", "___post_on_form___")
					.attr("method", "POST")
					.css("width", "0px").css("height", "0px").appendTo(document.body);
			}
			else
				$form.empty();
			
			$form.attr("action", url).attr("target", options.target);
			
			var param = $.param(options.data);
			
			//XXX $.param会将" "转换为"+"，而这里的decodeURIComponent并不会将"+"恢复为" "，因此需要在这里预先转换
			param = param.replace(/\+/g, " ");
			
			var paramArray = param.split("&");
			
			for(var i=0; i<paramArray.length; i++)
			{
				var paramPair = paramArray[i].split("=");
				
				var name = decodeURIComponent(paramPair[0]);
				var value = decodeURIComponent(paramPair[1]);

				$("<input type='hidden' />").attr("name", name).attr("value", value).appendTo($form);
			}
			
			$form[0].submit();
		},
		
		/**
		 * 打开确认对话框。
		 * @param content 显示内容
		 * @param options
		 * 			{
		 * 				//可选，确定回调函数
		 * 				confirm : function(){},
		 * 
		 * 				//可选，取消回调函数
		 * 				cancel : function(){},
		 * 
		 * 				//可选，确定按钮文本
		 * 				confirmText : "Confirm",
		 * 
		 * 				//可选，取消按钮文本
		 * 				cancelText : "Cancel",
		 * 
		 * 				//可选，取消按钮文本
		 * 				title : "Confirm"
		 * 			}
		 * @param yesCallback 确定回调函数
		 * @param noCallback 取消回调函数
		 */
		confirm : function(content, options)
		{
			options = (options || {});
			options = $.extend({confirmText : "Confirm", cancelText : "Cancel", title : "Confirm"}, options);
			
			var $confirmDialog = $("#dialog-confirm");
			
			if($confirmDialog.length == 0)
			{
				$confirmDialog = $("<div id='dialog-confirm' class='dialog-confirm' />").appendTo(document.body);
				var $p = $("<p><span class='ui-icon ui-icon-alert'></span></p>").appendTo($confirmDialog);
				$("<div class='confirm-content' />").appendTo($p);
			}
			
			$(".confirm-content", $confirmDialog).html(content);
			
			var buttons={};
			buttons[options.confirmText] = function()
			{
				if(options.confirm)
					options.confirm();
				
				$(this).dialog("close");
	        };
			buttons[options.cancelText] = function()
			{
				if(options.cancel)
					options.cancel();
				
				$(this).dialog("close");
	        };
			
			$confirmDialog.dialog(
			{
				resizable: false,
				height: "auto",
				width: "auto",
				position: {my: "center top", at: "center top+15%"},
				modal: true,
				buttons: buttons,
				title: options.title,
				close : function(event)
				{
					$(this).dialog("destroy").remove();
				}
			});
		},
		
		/**
		 * 提示成功。
		 */
		tipSuccess : function(content, delayMs)
		{
			content = "<span class='ui-icon ui-icon-circle-check' style='margin-right:0.3em;'></span>" + content;
			return $._tip("ui-state-default", content, (delayMs || 2000));
		},
		
		/**
		 * 提示错误。
		 */
		tipError : function(content, delayMs)
		{
			content = "<span class='ui-icon ui-icon-alert' style='margin-right:0.3em;'></span>" + content;
			return $._tip("ui-state-error", content, (delayMs || 5000));
		},
		
		/**
		 * 提示信息。
		 */
		tipInfo : function(content, delayMs)
		{
			content = "<span class='ui-icon ui-icon-info' style='margin-right:0.3em;'></span>" + content;
			return $._tip("ui-state-highlight", content, (delayMs || 5000));
		},

		/**
		 * 关闭提示框。
		 * @param tooltipId 可选，待关闭的tooltip标识。如果不指定，将直接关闭；如果指定，则会检查关闭。
		 */
		closeTip : function(tooltipId)
		{
			var tooltip = $(".global-tooltip", document.body);
			
			if(tooltip.length > 0)
			{
				if(!tooltipId || tooltipId == tooltip.attr("id"))
					tooltip.tooltip("destroy").remove();
			}
		},
		
		/**
		 * 提示。
		 * 
		 * @param tooltipClass 必选；样式名。
		 * @param content 必选；提示内容。
		 * @param delayMs 可选；延迟关闭的毫秒数，小于0表示不自动关闭。
		 */
		_tip : function(tooltipClass, content, delayMs)
		{
			content = "<div style='padding-left:0.5em;padding-right:0.5em; white-space: nowrap;'>" + content +"</div>";
			
			var tooltip = $(".global-tooltip", document.body);
			if(tooltip.length > 0)
				tooltip.tooltip("destroy").remove();
			
			var preTooltipTimeoutCloseId = $._tooltipTimeoutCloseId();
			if(preTooltipTimeoutCloseId)
				window.clearTimeout(preTooltipTimeoutCloseId);
			
			var tooltipParent = document.body;
			var customTooltipParent = $(".tooltip-parent");
			if(customTooltipParent.length > 0)
				tooltipParent = customTooltipParent[0];
			
			var tooltipId = $.uid("tooltip");
			
			tooltip = $("<div class='global-tooltip' style='display:none;' title=''>").attr("id", tooltipId)
			.prependTo(tooltipParent).tooltip(
			{
				tooltipClass: tooltipClass,
				content: content,
				position: { my: "center top", at: "center top+3", of: tooltipParent, collision: "flipfit" },
				open: function(event, ui)
				{
					if(delayMs >= 0)
					{
						var tooltipTimeoutCloseId = window.setTimeout(function(){ $.closeTip(); }, delayMs);
						$._tooltipTimeoutCloseId(tooltipTimeoutCloseId);
						
						ui.tooltip.mouseover(function()
						{
							window.clearTimeout($._tooltipTimeoutCloseId());
						})
						.mouseleave(function()
						{
							var tooltipTimeoutCloseId = window.setTimeout(function(){ $.closeTip(); }, delayMs);
							$._tooltipTimeoutCloseId(tooltipTimeoutCloseId);
						});
					}
					
					var tw = this;
					$(".ui-icon-close", ui.tooltip).click(function()
					{
						$(tw).tooltip("destroy").remove();
					});
				}
			})
			.tooltip("open");
			
			return tooltipId;
		},
		
		_tooltipTimeoutCloseId : function(id)
		{
			if(id)
				$(document.body).attr("tooltip-timeout-close-id", id);
			else
			{
				return $(document.body).attr("tooltip-timeout-close-id");
			}
		},
		
		/**
		 * 生成一个唯一ID
		 * 
		 * @param prefix
		 * @returns
		 */
		uid : function(prefix)
		{
			var re=(prefix ? prefix : "id") + new Date().getTime();
			
			return re;
		},
		
		/**
		 * 给对象属性名加前缀，并返回新对象。
		 * 
		 * @param obj
		 * @param prefix
		 */
		namePrefix : function(obj, prefix)
		{
			if(! prefix)
				return obj;
			
			var re = {};
			
			for(var name in obj)
			{
				re [prefix + name] = obj[name];
			}
			
			return re;
		},
		
		/**
		 * 给URL添加参数。
		 * 
		 * @param url 待添加参数的URL
		 * @param name 待添加的参数名
		 * @param value 待添加的参数值
		 */
		addParam : function(url, name, value)
		{
			var f = (url.indexOf('?') < 0 ? "?" : "&" );
			
			url = url + f + name + "=" + value;
			
			return url;
		},
		
		/**
		 * 获取对象或者对象数组的属性值参数字符串，例如：“id=1&id=2&id=3”
		 */
		getPropertyParamString : function(objOrArray, propertyName)
		{
			var re = "";
			
			if(!$.isArray(objOrArray))
				objOrArray = [objOrArray];
			
			for(var i=0; i<objOrArray.length; i++)
			{
				var ele = objOrArray[i];
				
				var pv = (ele ? ele[propertyName] : null);
				
				if(pv == undefined || pv == null)
					pv = "";
				
				if(re != "")
					re += "&";
				
				re += propertyName + "=" + encodeURIComponent(pv);
			}
			
			return re;
		},
		
		/**
		 * 如果是字符串且超过指定长度，则将其截断。
		 * 
		 * @param str 必选，待截断的字符串
		 * @param suffix 可选，截断后缀，默认为“...”
		 * @param length 可选，截断长度，默认为47
		 */
		truncateIf : function(str, suffix, length)
		{
			if(suffix == undefined)
				suffix = "...";
			
			if(length == undefined)
				length = 47;
			
			if(typeof(str) == "string" && str.length > length)
				str = str.substr(0, length) + suffix;
			
			return str;
		},
		
		/**
		 * 构建Datatables组件的“columns”选项值。
		 * 
		 * @param model 必选，模型
		 * @param options 选项，格式为：
		 * 			{
		 * 				//可选，忽略的属性名称
		 * 				ignorePropertyNames : undefined,
		 *				
		 * 				//可选，字符串最大显示长度
		 * 				stringDisplayThreshold : 47
		 * 			}
		 * @returns {Array}
		 */
		buildDataTablesColumns : function(model, options)
		{
			options = $.extend({ stringDisplayThreshold : 47 }, options);
			
			var properties = model.properties;
			
			var columns = [];
			for(var i=0; i<properties.length; i++)
			{
				var property = properties[i];
				var propName = property.name;
				
				if($.model.containsOrEquals(options.ignorePropertyNames, propName))
					continue;
				
				var notReadable = $.model.hasFeatureNotReadable(property);
				
				columns.push(
				{
					title: $.model.propertyLabelHtmlOfTitle(property, "a"),
					data: propName,
					propertyIndex: i,
					stringDisplayThreshold : options.stringDisplayThreshold,
					render: function(data, type, row, meta)
					{
						var renderValue = "";
						
						var _this = meta.settings.aoColumns[meta.col];
						
						var propertyIndex = _this.propertyIndex;
						var property = model.properties[propertyIndex];
						var propertyModel = property.model;
						
						renderValue = $.model.tokenProperty(property, data);
						renderValue = $.truncateIf(renderValue, "...", _this.stringDisplayThreshold);
						
						//解决当所有属性值都为null时，行渲染会很细问题
						if(propertyIndex == 0 && renderValue == "")
							renderValue = "&nbsp;";
						
						return renderValue;
					},
					defaultContent: "",
					orderable: !notReadable,
					searchable: !notReadable,
					className: (notReadable ? "ui-state-disabled" : "")
				});
			}
			
			return columns;
		},
		
		/**
		 * 设置表格对话框高度option。
		 */
		setGridPageHeightOption : function(options)
		{
			options.height = $(window).height() * 0.6;
		},
		
		/**
		 * 构建查询条件Autocomplete组件的“source”选项值。
		 * 
		 * @param propertyPathNameLabels
		 */
		buildSearchConditionAutocompleteSource : function(propertyPathNameLabels)
		{
			var source = [];
			
			if(!propertyPathNameLabels)
				return source;
			
			for(var i=0; i<propertyPathNameLabels.length; i++)
			{
				var ppnl = propertyPathNameLabels[i];
				
				source.push({label : ppnl.nameLabel, value : ppnl.propertyPath});
			}
			
			return source;
		},
		
		/**
		 * 调用指定对象的指定函数。
		 * @param obj
		 * @param functionName 函数名称
		 * @param varArgs 可选，可变函数参数
		 */
		callOnObjIf : function(obj, functionName, varArgs)
		{
			if(!obj)
				return undefined;
			
			var fn = obj[functionName];
			
			if(!fn)
				return undefined;
			
			var args = [];
			
			if(arguments.length > 2)
			{
				for(var i=2; i<arguments.length; i++)
					args[i - 2] = arguments[i];
			}
			
			return fn.apply(obj, args);
		},
		
		/**
		 * 调用指定对象的指定函数。
		 * @param obj
		 * @param functionName 函数名称
		 * @param argArray 可选，函数参数数组
		 */
		applyOnObjIf : function(obj, functionName, argArray)
		{
			if(!obj)
				return undefined;
			
			var fn = obj[functionName];
			
			if(!fn)
				return undefined;
			
			return fn.apply(obj, argArray);
		},
		
		/**
		 * 友好格式化大小。
		 */
		prettySize : function(bytes)
		{
			var factor = ["B", "KB", "MB", "GB", "TB", "PB"];
			
			var value = bytes;
			var count = 0;
			
			while(value > 1024 && count < (factor.length-1))
			{
				value = value / 1024;
				count++;
			}
			
			return parseInt(value) + factor[count];
		},
		
		/**
		 * 使用POST方式的getJSON。
		 */
		getJSONOnPost : function(url, data, callback)
		{
			$.ajax(
			{
				url : url,
				data : data,
				success : callback,
				dataType : "json",
				type : "POST",
			});
		},
		
		/**
		 * 单文件上传成功（success）处理函数。
		 */
		fileuploadsuccessHandlerForUploadInfo : function($fileUploadInfo, empty)
		{
			$(".upload-percent", $fileUploadInfo).text("100%");
			
			if(empty)
				$fileUploadInfo.empty();
		},
		
		/**
		 * 单文件上传添加（fileuploadadd）处理函数。
		 */
		fileuploadaddHandlerForUploadInfo : function(e, data, $fileUploadInfo)
		{
			$fileUploadInfo.empty();
			
			var fileName = data.files[0].name;
			var prettySize = $.prettySize(data.files[0].size);
			
			$("<div class='file-name' />").html((fileName.length > 13 ? fileName.substr(0, 10)+"..." : fileName))
				.attr("title", fileName)
				.appendTo($fileUploadInfo);
			$("<div class='file-size' />").html("("+prettySize+")").appendTo($fileUploadInfo);
			$("<div class='upload-percent' />").text("0%").appendTo($fileUploadInfo);
		},
		
		/**
		 * 单文件上传进度（fileuploadprogressall）处理函数。
		 */
		fileuploadprogressallHandlerForUploadInfo : function (e, data, $fileUploadInfo)
		{
			var progress = parseInt(data.loaded / data.total * 100, 10);
			
			//文件大的话，上传完成后success回调会延迟一会，所以100%在success里设置
			if(progress <= 100 && progress >99)
				progress = 99;
			
			$(".upload-percent", $fileUploadInfo).text(progress +"%");
		}
	});
	
	$.fn.extend(
	{
		/**
		 * 两次点击执行。
		 * @param callback 必选，第二次点击处理函数
		 * @param  firstCallback 可选，第一次点击处理函数
		 */
		secondClick : function(callback, firstCallback)
		{
			$(this).click(function()
			{
				var __this = $(this);
				
				//第二次点击
				if(__this.hasClass("second-click-first"))
				{
					__this.removeClass("ui-state-active second-click-first");
					
					callback.call(__this);
				}
				else
				{
					var success = true;
					
					if(firstCallback)
						success = firstCallback.call(__this);
					
					if(success != false)
						__this.addClass("ui-state-active second-click-first");
				}
			})
			.hover(function(){}, 
			function()
			{
				var __this = $(this);
				
				if(__this.hasClass("second-click-first"))
					$(this).removeClass("ui-state-active second-click-first");
			});
		}
	});
	
	//定义fastjson输出的循环引用恢复函数
	$.extend(
	{
		//引用对象的引用属性名
		REF_NAME : "$ref",
		
		//引用根对象
		REF_VALUE_ROOT : "$",
		
		//引用上级对象
		REF_VALUE_PARENT : "..",
		
		//引用对象自身
		REF_VALUE_THIS : "@",
		
		//引用路径前缀
		REF_VALUE_PATH_PREFIX : "$",
		
		//$.unref处理标识
		UNREF_FLAG : "___UNREF_FLAG",
		
		/**
		 * 复制对象，并将对象内的重复引用替换为引用路径，格式为：{ $.REF_NAME : $.REF_VALUE... }。
		 * 
		 * param obj
		 */
		ref : function(obj)
		{
			return $._ref(null, null, obj, $.REF_VALUE_PATH_PREFIX, []);
		},
		
		/**
		 * 复制参数对象，并将参数值内的重复引用替换为“$ref”。
		 * 
		 * @param param 参数对象
		 */
		refParam : function(param)
		{
			if(typeof(param) == "object")
			{
				//数组
				if(Array.isArray(param))
				{
					var copyParam = [];
					
					for(var i=0; i<param.length; i++)
					{
						copyParam[i] = $.ref(param[i]);
					}
					
					param = copyParam;
				}
				else
				{
					var copyParam = {};
					
					for(var propName in param)
					{
						var propValue = param[propName];
						
						copyParam[propName] = $.ref(propValue);
					}
					
					param = copyParam;
				}
			}
			
			return param;
		},
		
		_ref : function(root, parent, obj, path, objPathArray)
		{
			var refObj = undefined;
			
			if(obj && typeof(obj) == "object" && !obj[$.REF_NAME])
			{
				if(obj == root)
				{
					refObj = {};
					refObj[$.REF_NAME] = $.REF_VALUE_ROOT;
				}
				else if(obj == parent)
				{
					refObj = {};
					refObj[$.REF_NAME] = $.REF_VALUE_PARENT;
				}
				else
				{
					for(var i=0; i<objPathArray.length; i++)
					{
						if(objPathArray[i].obj == obj)
						{
							refObj = {};
							refObj[$.REF_NAME] = objPathArray[i].path;
							break;
						}
					}
				}
				
				if(!refObj)
				{
					var myObjPath = { "obj" : obj, "path" : path };
					objPathArray.push(myObjPath);
					
					//数组
					if(Array.isArray(obj))
					{
						var copyArray = [];
						
						for(var i=0; i<obj.length; i++)
						{
							var element = obj[i];
							
							copyArray[i] = $._ref((root ? root : obj), obj, element, path+"["+i+"]", objPathArray);
						}
						
						refObj = copyArray;
					}
					else
					{
						var copyObj = {};
						
						for(var propName in obj)
						{
							var propValue = obj[propName];
							
							if(propValue == obj)
							{
								propValue = {};
								propValue[$.REF_NAME] = $.REF_VALUE_THIS;
							}
							else
								propValue = $._ref((root ? root : obj), obj, propValue, path+"."+propName, objPathArray);
							
							copyObj[propName] = propValue;
						}
						
						refObj = copyObj;
					}
				}
			}
			
			return (refObj ? refObj : obj);
		},

		/**
		 * 判断给定对象是否包含重复引用。
		 */
		isRef : function(obj)
		{
			return $._isRef(obj, []);
		},
		
		_isRef : function(obj, objArray)
		{
			if(obj && typeof(obj) == "object")
			{
				for(var i=0; i<objArray.length; i++)
				{
					if(objArray[i] == obj)
						return true;
				}
				
				objArray.push(obj);
				
				//数组
				if(Array.isArray(obj))
				{
					for(var i=0; i<obj.length; i++)
					{
						var element = obj[i];
						
						if($._isRef(element, objArray))
							return true;
					}
				}
				else
				{
					for(var propName in obj)
					{
						var propValue = obj[propName];
						
						if($._isRef(propValue, objArray))
							return true;
					}
				}
			}
			
			return false;
		},
		
		/**
		 * 恢复由$.ref函数替换的的“$ref”引用。
		 * 
		 * @param obj
		 */
		unref: function(obj)
		{
			$._unref(obj, null, obj);
			$._removeUnrefFlag(obj);
			
			return obj;
		},
		
		/**
		 * 重定向对象中的fastjson关联（'$ref'属性值），这会在所有层级的对象上添加$.UNREF_FLAG属性。
		 * 
		 * @param root
		 * @param parent
		 * @param obj
		 */
		_unref: function(root, parent, obj)
		{
			if(root == undefined)
				return undefined;
			
			var objType=typeof(obj);
			
			if(obj && objType == "object")
			{
				//数组
				if(Array.isArray(obj))
				{
					for(var i=0; i<obj.length; i++)
					{
						var ele=obj[i];
						
						var ref=(ele ? ele[$.REF_NAME] : undefined);
						
						if(ref != undefined)
						{
							val = $._unrefValue(root, parent, obj, ref);
							obj[i] = val;
						}
						else
						{
							var newEle=$._unref(root, obj, ele);
							
							if(newEle != ele)
								obj[i] = newEle;
						}
					}
				}
				//对象
				else
				{
					if(obj[$.UNREF_FLAG])
						return;
					else
						obj[$.UNREF_FLAG]=true;
					
					for(var p in obj)
					{
						var val=obj[p];
						
						if(!val)
							continue;
						
						var ref=val[$.REF_NAME];
						
						if(ref != undefined)
						{
							val = $._unrefValue(root, parent, obj, ref);
							obj[p] = val;
						}
						else
						{
							var newVal=$._unref(root, obj, val);
							
							if(newVal != val)
								obj[p] = newVal;
						}
					}
				}
			}
			
			return obj;
		},
		
		/**
		 * 恢复“$ref”的值
		 */
		_unrefValue : function(root, parent, _this, ref)
		{
			var val = undefined;
			
			if(ref == $.REF_VALUE_ROOT)
			{
				val=root;
			}
			else if(ref == $.REF_VALUE_PARENT)
			{
				val = parent;
			}
			else if(ref == $.REF_VALUE_THIS)
			{
				val = _this;
			}
			else if(ref.indexOf($.REF_VALUE_PATH_PREFIX) == 0)
			{
				val=eval("root"+ref.substring(1));
			}
			else
				throw new Error("Unknown '"+$.REF_NAME+"' value '"+ref+"'");
			
			return val;
		},
		
		/**
		 * 移除对象中的$.UNREF_FLAG标记。
		 * 
		 * @param obj
		 */
		_removeUnrefFlag: function(obj)
		{
			if(obj == undefined)
				return;
			
			var objType=typeof(obj);
			
			if(objType == "object")
			{
				//数组
				if(obj.length != undefined)
				{
					for(var i=0; i<obj.length; i++)
					{
						$._removeUnrefFlag(obj[i]);
					}
				}
				//对象
				else
				{
					if(obj[$.UNREF_FLAG] == undefined)
						return;
					
					delete obj[$.UNREF_FLAG];
					
					for(var p in obj)
					{
						var val=obj[p];
						
						if(!val)
							continue;
						
						$._removeUnrefFlag(val);
					}
				}
			}
		}
	});
	
	/**
	 * 获取响应的JSON对象。
	 * 如果响应不是JSON格式，则返回null。
	 */
	$.getResponseJson = function(jqXHR)
	{
		if(jqXHR.responseJSON)
			return jqXHR.responseJSON;
		else
		{
			var responseContentType = (jqXHR.getResponseHeader("Content-Type") || "").toLowerCase();
			
			if(responseContentType.indexOf("json") > -1 && jqXHR.responseText)
			{
				var responseJSON = $.parseJSON(jqXHR.responseText);
				
				jqXHR.responseJSON = responseJSON;
				
				return responseJSON;
			}
			
			return null;
		}
	};
	
	$.handleAjaxError = function(jqXHR, errorThrown)
	{
		if(!window._showResponseErrorDetail)
		{
			window._showResponseErrorDetail = function()
			{
				$.closeTip();
				
				var $errorDetail = $("#responseError");
				
				var $dialog = $("<div id='dialog-"+new Date().getTime()+"' class='error-dialog'></div>").appendTo(document.body);
				var $errorContent = $("<div class='ui-state-error error-content' />").appendTo($dialog);
				$errorContent.html($(".throwable", $errorDetail).html());
				
				$._dialog($dialog,
						{
							title : $(".message", $errorDetail).text(),
							modal : true,
							height: "60%",
							position: {my: "center top", at: "center top+3"},
							classes:
							{
								"ui-dialog": "ui-corner-all ui-widget-shadow ui-state-error"
							}
						});
				
				var $dialogWidget = $dialog.dialog("widget");
				$(".ui-dialog-titlebar", $dialogWidget).addClass("ui-state-error");
				$(".ui-dialog-title", $dialogWidget).prepend("<span class='ui-icon ui-icon-alert' style='margin-right:0.3em;'></span>");
				$(".ui-dialog-titlebar-close", $dialogWidget).addClass("ui-state-error");
			};
		}
		
		if(jqXHR.status != 200 && jqXHR.responseText)
		{
			var $errorDiv = $("#responseError");
			if($errorDiv.length == 0)
				$errorDiv = $("<div id='responseError' style='display:none;' />").appendTo(document.body);
			
			var operationMessage = $.getResponseJson(jqXHR);
			
			//操作消息的JSON响应
			if(operationMessage)
			{
				$errorDiv.empty();
				
				var $omdiv = $("<div class='operation-message "+operationMessage.type+"' />").appendTo($errorDiv);
				var $mdiv = $("<div class='message' />").appendTo($omdiv).html(operationMessage.message);
				
				if(operationMessage.throwableTrace)
				{
					var $ddiv = $("<div class='throwable' />").appendTo($omdiv);
					var $pre = $("<pre />").appendTo($ddiv).html(operationMessage.throwableTrace);
				}
			}
			//操作消息的HTML响应
			else
				$errorDiv.html(jqXHR.responseText);
			
			var message = $(".message", $errorDiv).html();
			
			if($(".throwable", $errorDiv).length > 0)
				message += "&nbsp;<span class='ui-icon ui-icon-comment error-detail-icon' onclick='_showResponseErrorDetail();'></span>";
			
			$.tipError(message);
		}
		else
		{
			var msg = (jqXHR.statusText || "Error");
			
			if(errorThrown && errorThrown.message)
			{
				if(msg)
					msg += " : ";
				
				msg += errorThrown.message;
			}
			
			$.tipError(msg);
		}
	};
	
	$.ajaxSetup(
	{
		global : true,
		cache : false,
		converters : 
		{
			"* text": window.String,
			"text html": true,
			"text json": function(data)
			{
				data=$.unref(jQuery.parseJSON(data));
				
				return data;
			},
			"text xml": jQuery.parseXML
		},
		
		//避免ajax深度复制参数数据
		flatOptions : {
			context : true,
			url : true,
			data : true,
			type : true,
			success : true
		}
	});
	
	$(document).ajaxError(function(event, jqXHR, ajaxSettings, thrownError)
	{
		$.handleAjaxError(jqXHR, thrownError);
	});
	
	$(document).ajaxSuccess(function(event, jqXHR, ajaxSettings, thrownError)
	{
		var responseJson = $.getResponseJson(jqXHR);
		
		//确定是操作消息JSON
		if(responseJson && responseJson.type && responseJson.code && responseJson.message)
		{
			$.tipSuccess(responseJson.message);
		}
	});
})
(jQuery);
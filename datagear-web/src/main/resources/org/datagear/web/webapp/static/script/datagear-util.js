/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 工具函数集。
 * 
 * 依赖:
 * jquery.js
 * jquery-ui.js
 * datagear-meta.js
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
					},
					options,
					{
						data : (options.data ? options.data : undefined),
						success : function(data, textStatus, jqXHR)
						{
							var container=$(options.target ? options.target : document.body);
							
							var $dialog = $("<div id='dialog-"+new Date().getTime()+"' class='dialog-content-container'></div>").appendTo(container);
							
							if(options.pageParam)
								$.pageParam($dialog, options.pageParam);
							
							$._dialog($dialog, options);
							$dialog.html(data);
							
							if(!options.title)
							{
								var title = $("> title", $dialog).text();
								$dialog.dialog( "option", "title", title);
							}
						},
						type : "POST"
					});
				
				$.ajax(url, options);
			}
		},
		
		/*用于支持$.pageParam函数的元素CSS类名*/
		PAGE_PARAM_BINDER_CLASS : "page-param-binder",
		
		/**
		 * 获取/设置页面参数，设置页面参数，使页面在加载完成后可以在内部获取此参数。
		 * 
		 * @param $dom 必选，任意dom元素
		 * @param param 可选，要设置的参数
		 */
		pageParam : function($dom, param)
		{
			if(param == undefined)
			{
				var dcc = $dom.closest("." + $.PAGE_PARAM_BINDER_CLASS);
				return dcc.data("pageParam");
			}
			else
			{
				$dom.addClass($.PAGE_PARAM_BINDER_CLASS);
				$dom.data("pageParam", param);
			}
		},
		
		/**
		 * 调用页面参数函数。
		 * 如果没有页面参数或者指定的函数，返回undefined。
		 * 
		 * @param $dom 必选，任意dom元素
		 * @param functionName 可选，如果页面参数是对象，则指定页面对象的函数名
		 * @param argArray 可选，函数参数数组
		 */
		pageParamCall : function($dom, functionName, argArray)
		{
			var pageParam = $.pageParam($dom);
			
			//无页面参数
			if(!pageParam)
				return undefined;
			
			//页面参数是函数
			if($.isFunction(pageParam))
				return pageParam.apply(window, arguments[1]);
			
			//页面参数是对象
			var fun = pageParam[arguments[1]];
			return (fun == undefined ? undefined : fun.apply(pageParam, arguments[2]));
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
				
				width : "80%"
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
		 * 判断给定dom元素是否在对话框中或者将要在对话框中显示。
		 * 
		 * @param dom 任意DOM元素
		 */
		isInDialog : function(dom)
		{
			var $dialogFlag = $(dom).closest(".dialog-content-container");
			
			return ($dialogFlag && $dialogFlag.length > 0);
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
			
			if(options.data)
			{
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
		 * 初始化指定元素内的所有按钮。
		 */
		initButtons : function($parent)
		{
			$("input:submit, input:button, input:reset, button", $parent).button();
		},
		
		/**
		 * 构建resizable组件，但是阻止resize事件冒泡，不然会触发window的所有resize事件，而影响resizable组件的响应
		 */
		resizableStopPropagation : function($element, options)
		{
			$element.resizable(options);
			
			$element.on("resize", function(event)
			{
				event.stopPropagation();
			});
		},
		
		/**
		 * 提示成功。
		 */
		tipSuccess : function(content, delayMs)
		{
			content = "<span class='tooltip-icon ui-icon ui-icon-circle-check'></span>"
					+"<div class='content-value'>" + content +"</div>";
			return $._tip("ui-state-default", content, (delayMs || 2500));
		},
		
		/**
		 * 提示错误。
		 */
		tipError : function(content, delayMs)
		{
			content = "<span class='tooltip-icon ui-icon ui-icon-alert'></span>"
				+"<div class='content-value'>" + content +"</div>";
			return $._tip("ui-state-error", content, (delayMs || 4100));
		},
		
		/**
		 * 提示信息。
		 */
		tipInfo : function(content, delayMs)
		{
			content = "<span class='tooltip-icon ui-icon ui-icon-info'></span>"
				+"<div class='content-value'>" + content +"</div>";
			return $._tip("ui-state-highlight", content, (delayMs || 4100));
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
			content = "<div class='content'>" + content +"</div>";
			
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
		
		toJsonString: function(obj)
		{
			return JSON.stringify(obj);
		},
		
		/**
		 * 给URL添加参数。
		 * 
		 * @param url 待添加参数的URL
		 * @param name 待添加的参数名
		 * @param value 待添加的参数值
		 * @param multiple 允许重名，可选，默认为false
		 */
		addParam : function(url, name, value, multiple)
		{
			name = encodeURIComponent(name);
			value = encodeURIComponent(value);
			
			var qidx = url.indexOf('?');
			
			if(multiple == true || qidx < 0)
			{
				var f = (qidx < 0 ? "?" : "&");
				url = url + f + name + "=" + value;
				return url;
			}
			else
			{
				var keyword = name+"=";
				var start = url.indexOf(keyword, qidx+1);
				if(start >= 0)
				{
					var head = url.substring(0, start);
					start = start+keyword.length;
					var end = url.indexOf("&", start);
					var tail = (end >= 0 ? url.substr(end) : "");
					url = head + tail;
				}
				
				url += "&" + name +"=" + value;
			}
			
			return url;
		},
		
		/**
		 * 给URL添加参数字符串。
		 */
		addParamString : function(url, param)
		{
			var f = (url.indexOf('?') < 0 ? "?" : "&" );
			return url + f + param;
		},
		
		/**
		 * 将字符串数组转换为参数字符串。
		 */
		toParamString : function(paramName, strArray)
		{
			var re = "";
			
			for(var i=0; i<strArray.length; i++)
			{
				if(i > 0)
					re += "&";
				re += paramName + "=" + encodeURIComponent(strArray[i]);
			}
			
			return re;
		},
		
		/**
		 * 获取对象或者对象数组的属性值参数字符串，例如：“id=1&id=2&id=3”
		 * 
		 * @param objOrArray
		 * @param propertyName
		 * @param paramName 可选，参数名
		 */
		getPropertyParamString : function(objOrArray, propertyName, paramName)
		{
			var re = "";
			
			paramName = (paramName ? paramName : propertyName);
			paramName = encodeURIComponent(paramName);
			
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
				
				re += paramName + "=" + encodeURIComponent(pv);
			}
			
			return re;
		},

		/**
		 * 获取对象或者对象数组的属性值参数对象，对象格式为：{ name : "...", value : "..."}
		 * 
		 * @param objOrArray
		 * @param propertyName
		 * @param paramName 可选，参数名
		 */
		getPropertyParamObjArray : function(objOrArray, propertyName, paramName)
		{
			if(!paramName)
				paramName = propertyName;
			
			var re = [];
			
			var isArray = $.isArray(objOrArray);
			
			if(!isArray)
				objOrArray = [objOrArray];
			
			for(var i=0; i<objOrArray.length; i++)
			{
				var ele = objOrArray[i];
				
				var pv = (ele ? ele[propertyName] : null);
				
				if(pv == undefined || pv == null)
					pv = "";
				
				re[i] = { name : paramName, value : pv };
			}
			
			return (isArray ? re : re[0]);
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
		 * 转义HTML关键字。
		 * 
		 * @param text 要转义的文本
		 */
		escapeHtml : function(text)
		{
			if(text == null)
				return "";
			
			return $.meta.escapeHtml(text);
		},
		
		/**
		 * 将传入参数转义为路径。
		 * 
		 * @param args 路径元素，元素为true或false，可控制下一个元素是否转义
		 */
		toPath : function(args)
		{
			var re="";
			
			var encode = true;
			for(var i=0; i< arguments.length; i++)
			{
				var element = arguments[i];
				
				//设置下一个元素是否转义
				if(element === true || element === false)
				{
					encode = element;
					continue;
				}
				
				if(encode)
					element = encodeURIComponent(element);
				
				if(re && re.charAt(re.length - 1) != "/")
					re += "/";
				
				re += element;
				encode=true;
			}
			
			return re;
		},
		
		/**
		 * 为DataTables转义列名。
		 * 参考jquery.dataTables.js的_fnSplitObjNotation函数。
		 */
		escapeColumnNameForDataTable : function(columnName)
		{
			return columnName;
			
			/* 后台dbmodel已经限定了propertyName不会包含特殊字符，不再需要此逻辑
			var pn = "";
			
			for(var i=0; i<propertyName.length; i++)
			{
				var c = propertyName.charAt(i);
				
				if(c == '.')
				{
					pn += "\\" + c;
				}
				else
					pn += c;
			}
			
			return pn;
			*/
		},
		
		/**
		 * 反转义由escapeColumnNameForDataTable转义的列名。
		 */
		unescapeColumnNameForDataTable : function(columnName)
		{
			return columnName;
			
			/* 后台dbmodel已经限定了propertyName不会包含特殊字符，不再需要此逻辑
			var pn = "";
			
			for(var i=0; i<propertyName.length; i++)
			{
				var c = propertyName.charAt(i);
				
				if(c == '\\')
				{
					var cin = ((i + 1) < propertyName.length ? propertyName.charAt(i + 1) : 0);
					
					if(cin == '.')
					{
						i += 1;
						pn += cin;
					}
					else
						pn += c;
				}
				else
					pn += c;
			}
			
			return pn;
			*/
		},
		
		buildDataTablesColumnSimpleOption : function(title, data, hidden)
		{
			var option =
			{
				title : title,
				data : data,
				visible : !hidden,
				render: function(data, type, row, meta)
				{
					data = $.truncateIf(data);
					return $.escapeHtml(data);
				},
				defaultContent: "",
			};
			
			return option;
		},
		
		buildDataTablesColumnTitleWithTip : function(titleName, titleTip)
		{
			return "<a title='"+$.escapeHtml(titleTip)+"'>"+$.escapeHtml(titleName)+"</a>";
		},

		buildDataTablesColumnTitleSearchable : function(titleName)
		{
			return "<a class='column-searchable'>"+$.escapeHtml(titleName)+"</a>";
		},
		
		/**
		 * 构建Datatables组件的“columns”选项值。
		 * 
		 * @param table 必选，表
		 * @param options 选项，格式为：
		 * 			{
		 * 				//可选，字符串最大显示长度
		 * 				stringDisplayThreshold : 47,
		 * 
		 * 				//可选，单元格渲染后置处理函数
		 * 				postRender : function(data, type, rowData, meta, rowIndex, renderValue, table, column, dtColumn){}
		 * 			}
		 * @returns {Array}
		 */
		buildDataTablesColumns : function(table, options)
		{
			options = $.extend({ stringDisplayThreshold : 47 }, options);
			
			var columns = table.columns;
			
			var dtColumns = [];
			for(var i=0; i<columns.length; i++)
			{
				var column = columns[i];
				
				dtColumns.push(
				{
					title: $.meta.displayInfoHtml(column, "a"),
					data: $.escapeColumnNameForDataTable(column.name),
					columnIndex: i,
					options : options,
					render: function(data, type, row, meta)
					{
						var renderValue = "";
						
						var _this = meta.settings.aoColumns[meta.col];
						
						var columnIndex = _this.columnIndex;
						var column = $.meta.column(table, columnIndex);
						
						renderValue = $.meta.labelOfLabeledValue(data);
						renderValue = (renderValue == undefined ? data : renderValue);
						renderValue = $.truncateIf(renderValue, "...", _this.options.stringDisplayThreshold);
						renderValue = $.escapeHtml(renderValue);
						
						//解决当所有属性值都为null时，行渲染会很细问题
						if(columnIndex == 0 && renderValue == "")
							renderValue = " ";
						
						if(_this.options.postRender)
						{
							var rowIndex = meta.row;
							if(rowIndex.length)
								rowIndex = rowIndex[0];
							
							return _this.options.postRender(data, type, row, meta, rowIndex, renderValue, table, column, _this);
						}
						else
							return renderValue;
					},
					defaultContent: "",
					orderable: column.sortable,
					searchable: true
				});
			}
			
			return dtColumns;
		},
		
		/**
		 * 获取指定列名在DataTable中的列号。
		 */
		getDataTableColumn : function(settings, columnName)
		{
			var columnInfos = this.getDataTableColumnInfos(settings);
			
			var escapedName = $.escapeColumnNameForDataTable(columnName);
			
			for(var i=0; i<columnInfos.length; i++)
			{
				if(escapedName == columnInfos[i].data)
					return i;
			}
			
			return -1;
		},
		
		/**
		 * 获取DataTable列数。
		 */
		getDataTableColumnCount : function(dataTable, row)
		{
			if(row == undefined)
				row = dataTable.row(0);
			
			var count = $("> td", row.node()).length;
			
			return count;
		},
		
		/**
		 * 获取DataTable行数。
		 */
		getDataTableRowCount : function(dataTable, column)
		{
			if(column == undefined)
				column = dataTable.column(0);
			
			var count = column.nodes().length;
			
			return count;
		},
		
		/**
		 * 获取单元格索引对应的列名
		 */
		getDataTableCellName : function(settings, cellIndex)
		{
			var columnInfos = this.getDataTableColumnInfos(settings);
			return $.unescapeColumnNameForDataTable(columnInfos[cellIndex.column].data);
		},
		
		/**
		 * 获取单元格索引数组的列名-单元格索引数组映射表。
		 */
		getDataTableColumnNameCellIndexes : function(settings, cellIndexes)
		{
			var columnInfos = this.getDataTableColumnInfos(settings);
			
			var nameIndexes = {};
			for(var i=0; i<cellIndexes.length; i++)
			{
				var index = cellIndexes[i];
				var columnName = $.unescapeColumnNameForDataTable(columnInfos[index.column].data);
				
				var indexes = (nameIndexes[columnName] || (nameIndexes[columnName] = []));
				indexes.push(index);
			}
			
			return nameIndexes;
		},
		
		/**
		 * 根据单元格索引获取对应的行号-单元格索引数组映射表。
		 */
		getDataTableRowIndexesMap : function(cellIndexes)
		{
			var rowIndexesMap = {};
			
			for(var i=0; i<cellIndexes.length; i++)
			{
				var index = cellIndexes[i];
				
				var rowIndexes = (rowIndexesMap[index.row] || (rowIndexesMap[index.row] = []));
				rowIndexes.push(index);
			}
			
			return rowIndexesMap;
		},
		
		/**
		 * 如果所有单元格都在一行内则获取行号，如果不在同一行将返回null
		 */
		getDataTableRowIfSingle : function(cellIndexes)
		{
			var row = null;
			
			for(var i=0; i<cellIndexes.length; i++)
			{
				var index = cellIndexes[i];
				var myRow = index.row;
				
				if(row == null)
					row = myRow;
				else if(myRow != row)
				{
					row = null;
					break;
				}
			}
			
			return row;
		},
		
		/**
		 * 获取DataTable的列信息。
		 */
		getDataTableColumnInfos : function(settings)
		{
			var columnInfos = undefined;
			
			//column.render函数中的结构
			if(settings.aoColumns)
				columnInfos = settings.aoColumns;
			//.DataTable().settings()结构
			else if(settings[0])
				columnInfos = settings[0].aoColumns;
			//构造DataTable前的设置
			else if(settings.columns)
				columnInfos =  settings.columns;
			
			return columnInfos;
		},
		
		/**
		 * 设置表格数据。
		 */
		setDataTableData : function(dataTable, data, notDraw)
		{
			$.addDataTableData(dataTable, data, 0, notDraw);
		},
		
		/**
		 * 添加表格数据
		 */
		addDataTableData : function(dataTable, datas, startRowIndex, notDraw)
		{
			var rows = dataTable.rows();
			var removeRowIndexes = [];
			var dataIndex = 0;
			
			if(startRowIndex != null)
			{
				rows.every(function(rowIndex)
				{
					if(rowIndex < startRowIndex)
						return;
					
					if(dataIndex >= datas.length)
						removeRowIndexes.push(rowIndex);
					else
						this.data(datas[dataIndex]);
					
					dataIndex++;
				});
			}
			
			for(; dataIndex<datas.length; dataIndex++)
				var row = dataTable.row.add(datas[dataIndex]);
			
			dataTable.rows(removeRowIndexes).remove();
			
			if(!notDraw)
				dataTable.draw();
		},
		
		/**
		 * 处理点击选中单元格事件。
		 */
		handleCellSelectionForClick : function(dataTable, clickEvent, $clickCell)
		{
			//多选
			if(clickEvent.ctrlKey)
			{
				if($clickCell.hasClass("selected"))
				{
					dataTable.cell($clickCell).deselect();
					dataTable.cells(dataTable.cells(".selected").indexes()).select();
				}
				else
				{
					var indexes = [];
					indexes = indexes.concat($.makeArray(dataTable.cells(".selected").indexes()));
					indexes = indexes.concat(dataTable.cell($clickCell).index());
					
					dataTable.cells(indexes).select();
				}
			}
			//区域选
			else if(clickEvent.shiftKey)
			{
				var indexes = [];
				indexes = indexes.concat($.makeArray(dataTable.cells(".selected").indexes()));
				indexes = indexes.concat(dataTable.cell($clickCell).index());
				
				indexes = $.evalCellIndexesForRange(indexes);
				
				dataTable.cells(indexes).select();
			}
			//单选
			else
			{
				var selectCells = dataTable.cells(".selected");
				
				if(selectCells.nodes().length == 1 && $clickCell.hasClass("selected"))
					dataTable.cell($clickCell).deselect();
				else
				{
					selectCells.deselect();
					dataTable.cell($clickCell).select();
				}
			}
		},
		
		/**
		 * 处理单元格上、下、左、右按键事件导航。
		 */
		handleCellNavigationForKeydown : function(dataTable, keydownEvent)
		{
			var selectedCells = dataTable.cells(".selected");
			
			if(!selectedCells || selectedCells.length == 0)
				return;
			else
			{
				var cellIndexes = selectedCells.indexes();
				var nextCellIndex = {};
				
				if(keydownEvent.keyCode == $.ui.keyCode.DOWN)
				{
					var cellIndex = cellIndexes[cellIndexes.length - 1];
					nextCellIndex.row = cellIndex.row + 1;
					nextCellIndex.column = cellIndex.column;
				}
				else if(keydownEvent.keyCode == $.ui.keyCode.UP)
				{
					var cellIndex = cellIndexes[0];
					nextCellIndex.row = cellIndex.row - 1;
					nextCellIndex.column = cellIndex.column;
				}
				else if(keydownEvent.keyCode == $.ui.keyCode.LEFT)
				{
					var cellIndex = cellIndexes[0];
					nextCellIndex.row = cellIndex.row;
					nextCellIndex.column = cellIndex.column - 1;
				}
				else if(keydownEvent.keyCode == $.ui.keyCode.RIGHT)
				{
					var cellIndex = cellIndexes[cellIndexes.length - 1];
					nextCellIndex.row = cellIndex.row;
					nextCellIndex.column = cellIndex.column + 1;
				}
				else
					nextCellIndex = null;
				
				if(nextCellIndex != null)
				{
					var maxColumnIndex = this.getDataTableColumnCount(dataTable) - 1;
					var maxRowIndex = this.getDataTableRowCount(dataTable) - 1;
					
					if(nextCellIndex.row > maxRowIndex)
						nextCellIndex.row = maxRowIndex;
					if(nextCellIndex.row < 0)
						nextCellIndex.row = 0;
					
					if(nextCellIndex.column > maxColumnIndex)
						nextCellIndex.column = maxColumnIndex;
					if(nextCellIndex.column < 1)
						nextCellIndex.column = 1;
					
					//多选
					if(keydownEvent.ctrlKey)
					{
						var indexes = [];
						indexes = indexes.concat($.makeArray(selectedCells.indexes()));
						indexes = indexes.concat(nextCellIndex);
						
						dataTable.cells(indexes).select();
					}
					//区域选
					else if(keydownEvent.shiftKey)
					{
						var indexes = [];
						indexes = indexes.concat($.makeArray(selectedCells.indexes()));
						indexes = indexes.concat(nextCellIndex);
						
						indexes = $.evalCellIndexesForRange(indexes);
						
						dataTable.cells(indexes).select();
					}
					//单选
					else
					{
						selectedCells.deselect();
						dataTable.cell(nextCellIndex).select();
					}
				}
			}
		},
		
		/**
		 * 计算范围选择单元格index数组。
		 */
		evalCellIndexesForRange : function(cellIndexes, minColumnThreashold)
		{
			if(minColumnThreashold == undefined)
				minColumnThreashold = 0;
				
			var minRow = Number.MAX_VALUE, minColumn = Number.MAX_VALUE, maxRow = 0, maxColumn = 0;
			
			for(var i=0; i< cellIndexes.length; i++)
			{
				var cellIndex = cellIndexes[i];
				var row = cellIndex.row;
				var column = cellIndex.column;
				
				if(minRow > row)
					minRow = row;
				
				if(maxRow < row)
					maxRow = row;
				
				if(minColumn > column)
					minColumn = column;
				
				if(maxColumn < column)
					maxColumn = column;
			}
			
			if(minColumn < minColumnThreashold)
				minColumn = minColumnThreashold;
			
			if(maxColumn< minColumnThreashold)
				maxColumn = minColumnThreashold;
			
			var indexes = [];
			
			for(var i=minRow; i<=maxRow; i++)
			{
				for(var j=minColumn; j<=maxColumn; j++)
				{
					indexes.push({ row : i, column : j });
				}
			}
			
			return indexes;
		},
		
		/**
		 * 设置表格对话框高度option。
		 */
		setGridPageHeightOption : function(options)
		{
			options.height = $(window).height() * 0.75;
		},
		
		updateDataTableHeight : function(dataTableElements, height, adjustWidth)
		{
			if(height == null && adjustWidth == null)
				return;
			
			for(var i=0; i<dataTableElements.length; i++)
			{
				var dataTable = $(dataTableElements[i]).DataTable();
				
				if(height != null)
				{
					var tableParent = $(dataTable.table().body()).parent().parent();
					tableParent.height(height);
				}
				
				if(adjustWidth)
					dataTable.columns.adjust();
				
				if(dataTable.init().fixedColumns)
					dataTable.fixedColumns().relayout();
			}
		},
		
		isResizeDataTableWhenShow : function($tabsPanel)
		{
			return ($tabsPanel.attr("resize-table-when-show") == "1");
		},
		
		setResizeDataTableWhenShow : function($tabsPanel)
		{
			$tabsPanel.attr("resize-table-when-show", "1");
		},
		
		clearResizeDataTableWhenShow : function($tabsPanel)
		{
			$tabsPanel.removeAttr("resize-table-when-show");
		},
		
		/**
		 * 为DataTable绑定window重设大小事件。
		 */
		bindResizeDataTableHandler : function(dataTableElements, calChangedDataTableHeightFunc)
		{
			var resizeHandler = function(event) 
			{
				var $dataTable0 = $(dataTableElements[0]);
				
				//忽略隐藏选项卡中的表格调整，仅在选项卡显示时才调整，
				//一是DataTables对隐藏表格的宽度计算有问题，另外，绑定太多处理函数会影响jquery.resizeable组件的效率
				
				var tabsPanel = $dataTable0.closest(".ui-tabs-panel");
				if(tabsPanel.is(":hidden"))
				{
					$.setResizeDataTableWhenShow(tabsPanel);
					return;
				}
				
				var changedHeight = calChangedDataTableHeightFunc();
				
				if(changedHeight != null)
				{
					clearTimeout($dataTable0.data("resizeTableTimer"));
					
					var timer = setTimeout(function()
					{
						$.updateDataTableHeight(dataTableElements, changedHeight);
					},
					250);
					
					$dataTable0.data("resizeTableTimer", timer);
				}
			};
			
			$(window).bind('resize', resizeHandler);
			
			//如果表格处于选项卡页中，则在选项卡显示时，调整表格大小
			var tabsPanel = $(dataTableElements[0]).closest(".ui-tabs-panel");
			if(tabsPanel.length > 0)
			{
				tabsPanel.data("showCallback", function($tabsPanel)
				{
					if(!$.isResizeDataTableWhenShow(tabsPanel))
						return;
					
					var changedHeight = calChangedDataTableHeightFunc();
					$.updateDataTableHeight(dataTableElements, changedHeight, true);
					
					$.clearResizeDataTableWhenShow(tabsPanel);
				});
			}
			
			return resizeHandler;
		},
		
		isDatatTable : function($table)
		{
			return $table.hasClass("dataTable");
		},
		
		callTabsPanelShowCallback : function($tabsPanel)
		{
			var panelShowCallback = $tabsPanel.data("showCallback");
			if(panelShowCallback)
				panelShowCallback($tabsPanel);
			
			var subTabs = $tabsPanel.find(".ui-tabs");
			if(subTabs.length > 0)
			{
				subTabs.each(function()
				{
					var $this = $(this);
					
					var subTabsNav = $("> .ui-tabs-nav", $this);
					var subShowTab = $("> li.ui-tabs-tab.ui-state-active", subTabsNav);
					
					var subTabId = $("> a.ui-tabs-anchor", subShowTab).attr("href");
					if(subTabId.charAt(0) == "#")
						subTabId = subTabId.substr(1);
					
					var subTabPanel = $("> #"+subTabId, $this);
					
					$.callTabsPanelShowCallback(subTabPanel);
				});
			}
		},
		
		/**
		 * 构建查询条件Autocomplete组件的“source”选项值。
		 */
		buildSearchConditionAutocompleteSource : function(table, sqlIdentifierQuote)
		{
			var source = [];
			
			for(var i=0; i<table.columns.length; i++)
			{
				var column = table.columns[i];
				source.push({label : column.name, value : sqlIdentifierQuote + column.name + sqlIdentifierQuote});
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

			$("<div class='upload-percent' />").text("0%").appendTo($fileUploadInfo);
			$("<div class='file-name' />").html((fileName.length > 13 ? fileName.substr(0, 10)+"..." : fileName))
				.attr("title", fileName)
				.appendTo($fileUploadInfo);
			$("<div class='file-size' />").html("("+prettySize+")").appendTo($fileUploadInfo);
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
		},
		
		/**
		 * 获取映射表的关键字、值数组对象，并可选对KEY进行排序。
		 * 返回对象格式为：{ "keys" : [...], "values" : [...] }
		 * @param map 必选，映射表对象
		 * @param keySortfunction 可选，KEY排序函数
		 */
		getMapKeyValueArray : function(map, keySortfunction)
		{
			var keyArray = [];
			
			for(var key in map)
				keyArray.push(key);
			
			if(keySortfunction != undefined)
				keyArray.sort(keySortfunction);
			
			var valueArray = [];
			
			for(var i=0; i<keyArray.length; i++)
			{
				var value = map[keyArray[i]];
				valueArray.push(value);
			}
			
			return { "keys" : keyArray, "values" : valueArray };
		},
		
		/**
		 * 获取属性个数。
		 */
		getPropertyCount : function(obj)
		{
			if(!obj)
				return 0;
			
			var propertyCount = 0;
			
			for(var p in obj)
				propertyCount++;
			
			return propertyCount;
		},
		
		/**
		 * 如果对象仅包含一个属性，返回属性名；否则返回null。
		 */
		getPropertyNameIfSingle : function(obj)
		{
			if(!obj)
				return null;
			
			var keys = Object.keys(obj);
	    	var length = keys.length;
	    	
	    	if(length != 1)
	    		return null;
	    	
	    	return keys[0];
		},
		
		/**
		 * 判断两个对象是否深度相等。
		 */
		deepEquals : function(a, b, ignorePropertyNames, aStack, bStack)
		{
			if(a == b)
				return true;
			
			if(a == null || b == null)
				return false;
			
			var type = $.type(a);
			
			if($.type(b) != type)
				return false;
			
			//基本类型
			if(type != "array" && type != "object")
				return (a == b);
			
			aStack = aStack || [];
			bStack = bStack || [];
			var length = aStack.length;
			
			 //检查是否有循环引用的部分
		    while(length--)
		    {
		        if (aStack[length] == a)
		            return bStack[length] == b;
		    }
		    
		    aStack.push(a);
		    bStack.push(b);
		    
		    //数组
		    if(type == "array")
		    {
		    	length = a.length;
		    	
		    	if (b.length != length)
		    		return false;
		    	
		    	while(length--)
		    	{
		    		if (!$.deepEquals(a[length], b[length], ignorePropertyNames, aStack, bStack))
		    			return false;
		    	}
		    }
		    //对象
		    else
		    {
		    	var keys = Object.keys(a), key;
		    	length = keys.length;
		    	
		    	if (Object.keys(b).length != length)
		    		return false;
		    	
		    	while (length--)
		    	{
		    		key = keys[length];
		    		
		    		if($.meta.containsOrEquals(ignorePropertyNames, key))
		    			continue;
		    		
		    		if (!(b.hasOwnProperty(key) && $.deepEquals(a[key], b[key], null, aStack, bStack)))
		    			return false;
		    	}
		    }
		    
		    aStack.pop();
		    bStack.pop();
		    
		    return true;
		},
		
		/**
		 * 获取合法文件名称。
		 */
		toValidFileName : function(rawName)
		{
			var re = "";
			
			for(var i=0; i< rawName.length; i++)
			{
				var c = rawName.charAt(i);
				
				if(c == "\\" || c == "/" || c == ":" || c == "*"
					 || c == "?" || c == "\"" || c == "'" || c == "<"
						 || c == ">" || c == "|")
					continue;
				
				re += c;
			}
			
			return re;
		},
		
		/**
		 * 比较两个版本号。
		 * > 1：v0高于v1；= 0：v0等于v1；< 0：v0小于v1。
		 * 
		 * @param v0
		 * @param v1
		 */
		compareVersion : function(v0, v1)
		{
			var vv0 = $.resolveVersion(v0);
			var vv1 = $.resolveVersion(v1);
			
			for(var i=0; i<Math.max(vv0.length,vv1.length); i++)
			{
				if(vv0[i] > vv1[i])
					return 1;
				else if(vv0[i] < vv1[i])
					return -1;
			}
			
			return 0;
		},
		
		/**
		 * 解析版本号字符串（格式为：1.0, 1.1.0, 1.1.0-build），返回包含各分段的数组。
		 */
		resolveVersion : function(version)
		{
			version = (version || "");
			
			var ary = [0, 0, 0, ""];
			
			var bidx = version.indexOf("-");
			if(bidx > -1)
			{
				if((bidx+1) < version.length)
					ary[3] = version.substring(bidx+1);
				version = version.substring(0, bidx);
			}
			
			var vs = version.split(".");
			for(var i=0; i< Math.min(3, vs.length); i++)
			{
				var v = parseInt(vs[i]);
				if(!isNaN(v))
					ary[i] = v;
			}
			
			return ary;
		}
	});
	
	//SQL工作台SQL自动补全支持函数
	$.sqlAutocomplete = ($.sqlAutocomplete || {});
	$.extend($.sqlAutocomplete,
	{
		/**
		 * 解析SQL自动补全信息。
		 * 
		 * @param editor
		 * @param session
		 * @param pos
		 * @param prefix
		 * 
		 * @return { type : "table" } 或者 { type : "column", table : "..." } 或者 null
		 */
		resolveAutocompleteInfo : function(editor, session, pos, prefix, delimiter)
		{
			var autocompleteInfo = {};
			var maxRow = session.getLength() - 1;
			var prependRowMin = (pos.row - 50) < 0 ? 0 : (pos.row - 50);
			var appendRowMax = (pos.row + 50) > maxRow ? maxRow : (pos.row + 50);
			
			var tableAlias = $.sqlAutocomplete.resolveTableAlias(prefix);
			
			try
			{
				var sql = session.getLine(pos.row);
				var myIndex = pos.column - 1;
				
				var prependRow = pos.row - 1, appendRow = pos.row + 1;
				
				//前后最大允许50行
				while(true)
				{
					var tokens = [];
					$.sqlAutocomplete.resolveTokens(sql, tokens);
					
					var token = $.sqlAutocomplete.findTokenBySqlIndex(tokens, myIndex);
					var isInToken = $.sqlAutocomplete.isInToken(token, myIndex);
					
					autocompleteInfo = $.sqlAutocomplete.resolveTokenAutocompleteInfo(tokens, token, isInToken,
							delimiter, tableAlias);
					
					if(autocompleteInfo && autocompleteInfo.type == "prepend")
					{
						if(prependRow >= prependRowMin)
						{
							var prevRowText = session.getLine(prependRow);
							
							sql = prevRowText + "\n" + sql;
							myIndex = prevRowText.length + 1 + myIndex;
						
							prependRow--;
						}
						else
							break;
					}
					else if(autocompleteInfo && autocompleteInfo.type == "append")
					{
						if(appendRow <= appendRowMax)
						{
							var nextRowText = session.getLine(appendRow);
							sql = sql + "\n" + nextRowText;
							
							appendRow++;
						}
						else
							break;
					}
					else
						break;
				}
			}
			catch(e){}
			
			return autocompleteInfo;
		},
		
		/**
		 * 解析SQL自动补全信息。
		 * 
		 * @param tokens
		 * @param token
		 * @param isInToken
		 * @param delimiter 语句分隔符
		 * @param tableAlias 如果是列自动补全信息，则指定要查找的表别名
		 * @return 	{ type : "table" } 表；
		 * 			{ type : "column", table : "..." } 列；
		 * 			{ type : "none" } 无；
		 * 			{ type : "prepend" } 需要前加SQL语句才能解析
		 * 			{ type : "append" } 需要后加SQL语句才能解析
		 */
		resolveTokenAutocompleteInfo : function(tokens, token, isInToken, delimiter, tableAlias)
		{
			if(tokens.length < 1 || !token)
				return { type : "none" };
			
			//注释
			if(isInToken &&
					($.sqlAutocomplete.isTokenComment(token) || $.sqlAutocomplete.isTokenString(token)))
				return { type : "none" };
			
			var prevToken = token;
			if(isInToken)
			{
				prevToken = token.prev;
				//如果没有前置Token，则向上级查找
				if(!prevToken)
					prevToken = token.parent;
			}
			
			var prevToken = $.sqlAutocomplete.findToken(prevToken, false,
					$.sqlAutocomplete.isNotTokenComment, false);
			
			if(!prevToken)
				return { type : "prepend" };
			
			var isTokenDelimiter = function(token)
			{
				return $.sqlAutocomplete.isTokenTextValue(prevToken, delimiter)
						|| $.sqlAutocomplete.isTokenTextValue(prevToken, ";");
			};
			
			//SQL分隔符
			if(isTokenDelimiter(prevToken))
				return { type : "none" };
			
			//别名
			if($.sqlAutocomplete.isTokenTextValue(prevToken, "AS"))
				return { type : "none" };
			
			//表
			if($.sqlAutocomplete.isTokenKeyword(prevToken, $.sqlAutocomplete.keywordsNextIsTable))
				return { type : "table" };
			
			//列
			if($.sqlAutocomplete.isTokenKeyword(prevToken, $.sqlAutocomplete.keywordsNextIsColumn))
			{
				var tableTokenPredicate = function(token)
				{
					if(!$.sqlAutocomplete.isTokenIndentifier(token))
						return false;
					
					if(isTokenDelimiter(token))
						return false;
					
					if(!tableAlias)
						return true;
					
					if($.sqlAutocomplete.isTokenTextValue(token, tableAlias)
							|| $.sqlAutocomplete.isTokenTextValue(token.next, tableAlias))
						return true;
					
					return false;
				};
				
				if($.sqlAutocomplete.isTokenKeyword(prevToken, "SELECT"))
				{
					var fromToken = $.sqlAutocomplete.findToken(prevToken, true,
							function(token)
							{
								return $.sqlAutocomplete.isTokenKeyword(token, "FROM")
									|| isTokenDelimiter(token);
							},
							true);
					
					if(isTokenDelimiter(fromToken))
						return { type : "none" };
					
					var tableToken = $.sqlAutocomplete.findToken(fromToken, true,
							tableTokenPredicate, true);
					
					if(tableToken)
					{
						if(isTokenDelimiter(tableToken))
							return { type : "none" };
						else
							return { type : "column", table : tableToken.value };
					}
					else
						return { type : "append" };
				}
				else if($.sqlAutocomplete.isTokenKeyword(prevToken, "WHERE")
						|| $.sqlAutocomplete.isTokenKeyword(prevToken, "ON")
						|| $.sqlAutocomplete.isTokenKeyword(prevToken, "BY"))
				{
					var fromToken = $.sqlAutocomplete.findToken(prevToken, false,
							function(token)
							{
								return $.sqlAutocomplete.isTokenKeyword(token, "FROM")
										|| $.sqlAutocomplete.isTokenKeyword(token, "UPDATE")
										|| isTokenDelimiter(token);
							},
							true);
					
					if(isTokenDelimiter(fromToken))
						return { type : "none" };
					
					var tableToken = $.sqlAutocomplete.findToken(fromToken, true,
							tableTokenPredicate, true);
					
					if(tableToken)
					{
						if(isTokenDelimiter(tableToken))
							return { type : "none" };
						else
							return { type : "column", table : tableToken.value };
					}
					else
						return { type : "prepend" };
				}
				else if($.sqlAutocomplete.isTokenKeyword(prevToken, "SET"))
				{
					var updateToken = $.sqlAutocomplete.findToken(prevToken, false,
							function(token)
							{
								return $.sqlAutocomplete.isTokenKeyword(token, "UPDATE")
										|| isTokenDelimiter(token);
							},
							true);
					
					if(isTokenDelimiter(updateToken))
						return { type : "none" };
					
					var tableToken = $.sqlAutocomplete.findToken(updateToken, true,
							tableTokenPredicate, true);
					
					if(tableToken)
					{
						if(isTokenDelimiter(tableToken))
							return { type : "none" };
						else
							return { type : "column", table : tableToken.value };
					}
					else
						return { type : "prepend" };
				}
			}
			//列 INTO [table] (...)
			else if($.sqlAutocomplete.isTokenIndentifier(prevToken)
					&& $.sqlAutocomplete.isTokenKeyword(prevToken.prev, "INTO"))
			{
				return { type : "column", table : prevToken.value };
			}
			else
				return $.sqlAutocomplete.resolveTokenAutocompleteInfo(tokens, prevToken, true, delimiter, tableAlias);
			
			return { type : "none" };
		},
		
		/**
		 * 解析表别名。
		 */
		resolveTableAlias : function(columnAccess)
		{
			var tableAlias = null;
			if(columnAccess)
			{
				var tableAliasEndIndex = columnAccess.indexOf(".");
				if(tableAliasEndIndex > 0)
					tableAlias = columnAccess.substring(0, tableAliasEndIndex);
			}
			
			return tableAlias;
		},
		
		buildCompletions : function(names, prefix)
		{
			var completions = [];
			
			if(!names)
				return completions;
			
			for(var i=0; i<names.length; i++)
			{
				completions[i] =
				{
					name : (prefix ? prefix + names[i] : names[i]),
					value : (prefix ? prefix + names[i] : names[i]),
					caption: "",
					meta: ""
				};
			}
			
			return completions;
		},
		
		//SQL自动补全涉及的关键字（必须大写）
		keywords :
		{
			"SELECT" : true, "FROM" : true, "LEFT" : true, "RIGHT" : true, "CROSS" : true, "FULL" : true,
			"INNER" : true, "OUTER" : true, "JOIN" : true, "ON" : true, "WHERE" : true,
			"ORDER" : true, "GROUP" : true, "BY" : true, "HAVING" : true, "UNION" : true,
			"INSERT" : true, "INTO" : true, "VALUES" : true,
			"UPDATE" : true, "SET" : true,
			"DELETE" : true, "DROP" : true,
			"ALTER" : true, "DROP" : true, "TABLE" : true, "ADD" : true, "RENAME" : true, "MODIFY" : true,
			"CREATE" : true, "REPLACE" : true, "VIEW" : true, "INDEX" : true, "PROCEDURE" : true,
			"TRIGGER" : true, "FUNCTION" : true
		},
		
		//SQL语句开始关键字*（必须大写）
		keywordsSqlStart :
		{
			"SELECT" : true, "INSERT" : true, "UPDATE" : true, "DELETE" : true,
			"ALTER" : true, "DROP" : true, "CREATE" : true, "REPLACE" : true, "MERGE" : true,
			"GRANT" : true
		},
		
		//下一个Token（注释除外）必定是表名称的关键字（必须大写）
		keywordsNextIsTable :
		{
			"FROM" : true,
			"JOIN" : true,
			"UPDATE" : true,
			"INTO" : true,
			"TABLE" : true
		},
		
		//下一个Token（注释除外）必定是列名称的关键字（必须大写）
		keywordsNextIsColumn :
		{
			"SELECT" : true,
			"WHERE" : true,
			"ON" : true,
			"BY" : true,
			"SET" : true
		},
		
		isKeyword : function(text, keywords)
		{
			if(!keywords)
				keywords = $.sqlAutocomplete.keywords;
			
			if(!text)
				return false;
			
			if($.sqlAutocomplete.maxKeywordLength == null)
				$.sqlAutocomplete.maxKeywordLength = $.sqlAutocomplete.getMaxKeywordLength($.sqlAutocomplete.keywords);
			
			if(text.length > $.sqlAutocomplete.maxKeywordLength)
				return false;
			
			if(typeof(keywords) == "string")
				return keywords == text.toUpperCase();
			else
				return keywords[text.toUpperCase()];
		},
		
		getMaxKeywordLength : function(keywords)
		{
			var maxLength = 0;
			
			for(var k in keywords)
			{
				if(k.length > maxLength)
					maxLength = k.length;
			}
			
			return maxLength;
		},
		
		TOKEN_KEYWORD : 1,
		
		TOKEN_STRING : 3,
		
		TOKEN_COMMENT_LINE : 6,
		
		TOKEN_COMMENT_BLOCK : 7,
		
		TOKEN_PUNCTUATION : 5,
		
		TOKEN_BRACKET_BLOCK : 4,
		
		TOKEN_IDENTIFIER : 99,
		
		/**
		 * 查找Token。
		 * 
		 * @param token
		 * @param forward true 往后查找；false，往前查找
		 * @param predicate 断言函数，格式为：function(token){ return true || false || null }，true 返回此token；false 继续查找；null 返回null。
		 * @param onlySibling 可选（默认为false），是否仅在本级查找
		 */
		findToken : function(token, forward, predicate, onlySibling)
		{
			if(onlySibling == undefined)
				onlySibling = false;
			
			var tmpToken = token;
			
			while(tmpToken)
			{
				var predicateResult = predicate(tmpToken);
				
				if(predicateResult == null)
					return null;
				
				if(predicateResult == true)
					return tmpToken;
				
				tmpToken = (forward ? tmpToken.next : tmpToken.prev);
			}
			
			if(!onlySibling && token && token.parent)
				return $.sqlAutocomplete.findToken(token.parent, forward, predicate, false);
			else
				return null;
		},
		
		/**
		 * 查找SQL语句中指定位置的Token，如果指定位置不是Token，则返回前一个。
		 * 
		 * @param tokens
		 * @param sqlIndex SQL语句中的位置
		 */
		findTokenBySqlIndex : function(tokens, sqlIndex)
		{
			if(!tokens || tokens.length < 1)
				return undefined;
			
			for(var i=0; i<tokens.length; i++)
			{
				var token = tokens[i];
				
				//在token起始位置
				if(token.startIndex == sqlIndex)
					return token;
				//在token中
				else if(token.startIndex < sqlIndex && (token.endIndex == null || sqlIndex < token.endIndex))
				{
					if($.sqlAutocomplete.isTokenBracketBlockWithValue(token))
					{
						var subToken = $.sqlAutocomplete.findTokenBySqlIndex(token.value, sqlIndex);
						
						return (subToken || token);
					}
					else
						return token;
				}
				//在token和token.next的中间
				else if(token.endIndex <= sqlIndex && token.next && token.next.startIndex > sqlIndex)
				{
					return token;
				}
			}
			
			return null;
		},
		
		/**
		 * 判断SQL语句中的位置是否在指定Token中。
		 * 
		 * @param tokens
		 * @param sqlIndex SQL语句中的位置
		 */
		isInToken : function(token, sqlIndex)
		{
			return (token && token.startIndex <= sqlIndex && (token.endIndex == null || sqlIndex < token.endIndex));
		},
		
		/**
		 * 判断token.value是否是指定字符串（忽略大小写）。
		 * 
		 * @param token
		 * @param textValue 可选，文本值
		 */
		isTokenTextValue : function(token, textValue)
		{
			if(!token || typeof(token.value) != "string")
				return false;
			
			if(textValue == undefined)
				return true;
			
			if(token.value.length != textValue.length)
				return false;
			
			var upperValue = token.value.toUpperCase();
			
			if(upperValue == textValue)
				return true;
			
			return (upperValue == textValue.toUpperCase());
		},
		
		isTokenString : function(token)
		{
			return $.sqlAutocomplete.isTokenType(token, $.sqlAutocomplete.TOKEN_STRING);
		},
		
		/**
		 * 是否是标识符Token。
		 */
		isTokenIndentifier : function(token)
		{
			return $.sqlAutocomplete.isTokenType(token, $.sqlAutocomplete.TOKEN_IDENTIFIER);
		},
		
		/**
		 * 是否是注释Token。
		 */
		isTokenComment : function(token)
		{
			return $.sqlAutocomplete.isTokenType(token, $.sqlAutocomplete.TOKEN_COMMENT_LINE)
					|| $.sqlAutocomplete.isTokenType(token, $.sqlAutocomplete.TOKEN_COMMENT_BLOCK);
		},
		
		/**
		 * 是否不是注释Token。
		 */
		isNotTokenComment : function(token)
		{
			return !$.sqlAutocomplete.isTokenComment(token);
		},
		
		/**
		 * 是否是括弧块，并且有子Token。
		 */
		isTokenBracketBlockWithValue : function(token)
		{
			if(!$.sqlAutocomplete.isTokenBracketBlock(token))
				return false;
			
			return (token.value && token.value.length > 0);
		},
		
		/**
		 * 是否是括弧块。
		 */
		isTokenBracketBlock : function(token)
		{
			return $.sqlAutocomplete.isTokenType(token, $.sqlAutocomplete.TOKEN_BRACKET_BLOCK);
		},
		
		/**
		 * 是否是关键字。
		 * 
		 * @param token
		 * @param keywords 可选，指定关键字
		 */
		isTokenKeyword : function(token, keywords)
		{
			if(!$.sqlAutocomplete.isTokenType(token, $.sqlAutocomplete.TOKEN_KEYWORD))
				return false;
			
			if(keywords == undefined)
				return true;
			
			return $.sqlAutocomplete.isKeyword(token.value, keywords);
		},
		
		/**
		 * 是否是指定类型的Token。
		 * 
		 * @param token
		 * @param type
		 */
		isTokenType : function(token, type)
		{
			return (token && token.type == type);
		},
		
		/**
		 * Token是否已解析完成。
		 */
		isTokenClosed : function(token)
		{
			return (token && token.endIndex != null);
		},
		
		/**
		 * 从指定位置处解析Token数组。
		 * 
		 * @param sql
		 * @param tokens 用于存储解析结果的数组
		 * @param startIndex 可选，起始位置，默认为0
		 * @return 解析结束位置，sql.length 正确解析完成；<sql.length 遇到非法终止符，比如单独的右括弧、块注释结束符
		 */
		resolveTokens : function(sql, tokens, startIndex)
		{
			if(startIndex == undefined)
				startIndex = 0;
			
			for(var i=startIndex; i< sql.length;)
			{
				var token = $.sqlAutocomplete.resolveToken(sql, i);
				
				//直到结束没有任何Token
				if(!token)
					return sql.length;
				
				//Token没有结束
				if(token.endIndex == null)
				{
					$.sqlAutocomplete.addTokenToArray(tokens, token);
					return sql.length;
				}
				
				//既不是Token开始符、也不是关键字/标识符字符，比如')'
				if(token.startIndex == token.endIndex)
					return token.endIndex;
				
				$.sqlAutocomplete.addTokenToArray(tokens, token);
				i = token.endIndex;
			}
			
			return sql.length;
		},
		
		/**
		 * 将Token添加至数组，并建立链表关联。
		 * 
		 * @param tokens 原数组
		 * @param token 待追加的单个元素或者数组
		 */
		addTokenToArray : function(tokens, token)
		{
			var prev = ((tokens.length - 1) >= 0 ? tokens[tokens.length - 1] : null);
			
			if(prev)
			{
				prev.next = token;
				token.prev = prev;
			}
			
			if(token.length)
			{
				for(var i=0; i<token.length; i++)
					$.sqlAutocomplete.addTokenToArray(tokens, token[i]);
			}
			else
				tokens.push(token);
		},
		
		/**
		 * 从指定位置处解析一个Token并返回。
		 * 
		 * @param sql
		 * @param startIndex
		 * @param token 可选，需要继续解析的Token
		 */
		resolveToken : function(sql, startIndex, token)
		{
			if(token)
			{
				if(token.startIndex == null)
					throw new Error("illegal");
				
				if(token.endIndex != null)
					;
				else if($.sqlAutocomplete.TOKEN_KEYWORD == token.type)
				{
					$.sqlAutocomplete.resolveTokenIdentifier(sql, startIndex, token);
					token.type = $.sqlAutocomplete.TOKEN_KEYWORD;
				}
				else if($.sqlAutocomplete.TOKEN_STRING == token.type)
				{
					$.sqlAutocomplete.resolveTokenString(sql, startIndex, token);
				}
				else if($.sqlAutocomplete.TOKEN_COMMENT_LINE == token.type)
				{
					$.sqlAutocomplete.resolveTokenCommentLine(sql, startIndex, token);
				}
				else if($.sqlAutocomplete.TOKEN_COMMENT_BLOCK == token.type)
				{
					$.sqlAutocomplete.resolveTokenCommentBlock(sql, startIndex, token);
				}
				else if($.sqlAutocomplete.TOKEN_PUNCTUATION == token.type)
				{
					token.endIndex = startIndex;
				}
				else if($.sqlAutocomplete.TOKEN_BRACKET_BLOCK == token.type)
				{
					$.sqlAutocomplete.resolveTokenBracketBlock(sql, startIndex, token);
				}
				else if($.sqlAutocomplete.TOKEN_IDENTIFIER == token.type)
				{
					$.sqlAutocomplete.resolveTokenIdentifier(sql, startIndex, token);
				}
				else
					throw new Error("unsupported");
				
				return token;
			}
			
			for(var i = startIndex; i < sql.length;)
			{
				var c = sql.charAt(i);
				var cn = (i+1) >= sql.length ? 0 : sql.charAt(i+1);
				
				var token = null;
				
				//字符串
				if(c == '\'')
				{
					token = { startIndex : i };
					$.sqlAutocomplete.resolveTokenString(sql, i+1, token);
				}
				//行注释
				else if((c == '-' && cn == '-') || (c =='/' && cn == '/'))
				{
					token = { startIndex : i, value : (c == '-' ? "--" : "//") };
					$.sqlAutocomplete.resolveTokenCommentLine(sql, i+2, token);
				}
				//块注释
				else if(c == '/' && cn == '*')
				{
					token = { startIndex : i };
					$.sqlAutocomplete.resolveTokenCommentBlock(sql, i+2, token);
				}
				//括弧块
				else if(c == '(')
				{
					token = { startIndex : i };
					$.sqlAutocomplete.resolveTokenBracketBlock(sql, i+1, token);
				}
				//标点符号
				else if(c == ',' || c == ';')
				{
					token = { type : $.sqlAutocomplete.TOKEN_PUNCTUATION, startIndex : i, endIndex : i+1, value : c };
				}
				//标识符
				else if(!/\s/.test(c))
				{
					token = { startIndex : i };
					$.sqlAutocomplete.resolveTokenIdentifier(sql, i, token);
					
					if($.sqlAutocomplete.isKeyword(token.value))
						token.type = $.sqlAutocomplete.TOKEN_KEYWORD;
				}
				
				if(token)
					return token;
				
				i += 1;
			}
			
			return null;
		},
		
		/**
		 * 解析字符串。
		 * 
		 * TOKEN_STRING 结构：
		 * {
		 * 		startIndex : Number, //起始位置
		 * 		endIndex : Number, //结束位置，undefined表示未结束
		 * 		value : String, //字符串内容
		 * 		parent : Token, //父Token，undefined表示无父Token，否则仅可能为TOKEN_BRACKET_BLOCK
		 * }
		 * 
		 * 如果没有找到字符串结束符，token.endIndex将为undefined。
		 * 
		 * @param sql
		 * @param startIndex
		 * @param token 要解析的Token，结构为：{ startIndex : Number }
		 */
		resolveTokenString : function(sql, startIndex, token)
		{
			token.type = $.sqlAutocomplete.TOKEN_STRING;
			
			if(!token.value)
				token.value = "'";
			
			var chars = [];
			
			for(i = startIndex; i<sql.length; i++)
			{
				var c = sql.charAt(i);
				
				chars.push(c);
				
				if(c == '\'')
				{
					var cn = (i+1) >= sql.length ? 0 : sql.charAt(i+1);
					
					//转义字符
					if(cn == '\'')
					{
						chars.push(cn);
						i = i+1;
					}
					else
					{
						token.value = token.value + chars.join("");
						token.endIndex = i+1;
						chars = [];
						
						break;
					}
				}
			}
			
			//没有结束符
			if(chars.length > 0)
				token.value = token.value + chars.join("");
		},
		
		/**
		 * 解析行注释。
		 * 结构同TOKEN_STRING。
		 * 如果没有找到行注释结束符，token.endIndex将为undefined。
		 * 
		 * @param sql
		 * @param startIndex
		 * @param token 要解析的Token，结构为：{ startIndex : Number }
		 */
		resolveTokenCommentLine : function(sql, startIndex, token)
		{
			token.type = $.sqlAutocomplete.TOKEN_COMMENT_LINE;
			
			if(!token.value)
				token.value = "--";
			
			var chars = [];
			
			for(i = startIndex; i<sql.length; i++)
			{
				var c = sql.charAt(i);
				
				if(c == '\n')
				{
					token.value = token.value + chars.join("");
					token.endIndex = i;
					chars = [];
					
					break;
				}
				else
				{
					chars.push(c);
				}
			}
			
			//没有结束符
			if(chars.length > 0)
				token.value = token.value + chars.join("");
		},
		
		/**
		 * 解析块注释。
		 * 结构同TOKEN_STRING。
		 * 如果没有找到块注释结束符，token.endIndex将为undefined。
		 * 
		 * @param sql
		 * @param startIndex
		 * @param token 要解析的Token，结构为：{ startIndex : Number }
		 */
		resolveTokenCommentBlock : function(sql, startIndex, token)
		{
			token.type = $.sqlAutocomplete.TOKEN_COMMENT_BLOCK;
			
			if(!token.value)
				token.value = "/*";
			
			var chars = [];
			
			for(i = startIndex; i<sql.length; i++)
			{
				var c = sql.charAt(i);
				
				chars.push(c);
				
				if(c == '*')
				{
					var cn = (i+1) >= sql.length ? 0 : sql.charAt(i+1);
					
					if(cn == '/')
					{
						chars.push(cn);
						
						token.value = token.value + chars.join("");
						token.endIndex = i+2;
						chars = [];
						
						break;
					}
				}
			}
			
			//没有结束符
			if(chars.length > 0)
				token.value = token.value + chars.join("");
		},
		
		/**
		 * 解析括弧块。
		 * 
		 * TOKEN_STRING 结构：
		 * {
		 * 		startIndex : Number, //起始位置
		 * 		endIndex : Number, //结束位置，undefined表示未结束
		 * 		value : Token[], //子Token数组
		 * 		parent : Token, //父Token，undefined表示无父Token，否则仅可能为TOKEN_BRACKET_BLOCK
		 * }
		 * 
		 * @param sql
		 * @param startIndex
		 * @param token 要解析的Token，结构为：{ startIndex : Number }
		 */
		resolveTokenBracketBlock : function(sql, startIndex, token)
		{
			token.type = $.sqlAutocomplete.TOKEN_BRACKET_BLOCK;
			
			if(token.value && token.value.length > 0)
			{
				var tailToken = token.value[token.value.length - 1];
				
				if(tailToken.endIndex == null)
					$.sqlAutocomplete.resolveToken(sql, startIndex, tailToken);
				
				for(var i=0; i<token.value.length; i++)
					token.value[i].parent = token;
				
				if(tailToken.endIndex != null && tailToken.endIndex < sql.length && sql.charAt(tailToken.endIndex) == ')')
					token.endIndex = tailToken.endIndex + 1;
				
				return;
			}
			
			token.value = [];
			var endIndex = $.sqlAutocomplete.resolveTokens(sql, token.value, startIndex);
			
			for(var i=0; i<token.value.length; i++)
				token.value[i].parent = token;
			
			if(endIndex < sql.length && sql.charAt(endIndex) == ')')
				token.endIndex = endIndex + 1;
		},
		
		/**
		 * 解析标识符。
		 * 结构同TOKEN_STRING，但是endIndex始终不会为undefined。
		 * 
		 * @param sql
		 * @param startIndex
		 * @param token 要解析的Token，结构为：{ startIndex : Number }
		 */
		resolveTokenIdentifier : function(sql, startIndex, token)
		{
			token.type = $.sqlAutocomplete.TOKEN_IDENTIFIER;
			
			if(token.value == null)
				token.value = "";
			
			var chars = [];
			
			for(i = startIndex; i<sql.length; i++)
			{
				var c = sql.charAt(i);
				
				var isEnd = (/[\s\,\;\(\)]/.test(c));
				
				//遇到“*/”，当sql不合法时可能
				if(!isEnd && c == '*')
				{
					var cn = (i+1) >= sql.length ? 0 : sql.charAt(i+1);
					
					if(cn == '/')
						isEnd = true;
				}
				
				if(isEnd)
				{
					token.value = token.value + chars.join("");
					token.endIndex = i;
					chars = [];
					
					break;
				}
				else
					chars.push(c);
			}
			
			//没有结束符
			if(chars.length > 0)
			{
				token.value = token.value + chars.join("");
				token.endIndex = sql.length;
			}
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

	$.handleAjaxOperationMessage = function(jqXHR, errorThrown)
	{
		if(!window._showAjaxOperationMessageDetail)
		{
			window._showAjaxOperationMessageDetail = function()
			{
				$.closeTip();
				
				var $operationMessageParent = $("#__operationMessageParent");
				
				var isSuccessMessage = ("true" == $operationMessageParent.attr("success"));
				
				var $dialog = $("<div id='dialog-"+new Date().getTime()+"' class='operation-message-dialog'></div>").appendTo(document.body);
				
				var $messageDetail = $("<div class='message-detail' />");
				if(!isSuccessMessage)
					$messageDetail.addClass("ui-state-error");
				$messageDetail.appendTo($dialog);
				$messageDetail.html($(".message-detail", $operationMessageParent).html());
				
				$._dialog($dialog,
						{
							title : $(".message", $operationMessageParent).text(),
							modal : true,
							height: "60%",
							position: {my: "center top", at: "center top+3"},
							classes:
							{
								"ui-dialog": "ui-corner-all ui-widget-shadow" + (isSuccessMessage ? "" : "ui-state-error")
							}
						});
				
				var $dialogWidget = $dialog.dialog("widget");
				
				$(".ui-dialog-title", $dialogWidget).prepend("<span class='ui-icon "+(isSuccessMessage ? "ui-icon-circle-check" : "ui-icon-alert")+"'></span>");
				
				if(!isSuccessMessage)
				{
					$dialogWidget.addClass("ui-state-error");
					$(".ui-dialog-titlebar", $dialogWidget).addClass("ui-state-error");
					$(".ui-dialog-titlebar-close", $dialogWidget).addClass("ui-state-error");
				}
			};
		}
		
		var isSuccessMessage = (jqXHR.status == 200);
		
		if(jqXHR.responseText)
		{
			var $operationMessageParent = $("#__operationMessageParent");
			if($operationMessageParent.length == 0)
				$operationMessageParent = $("<div id='__operationMessageParent' style='display:none;' />").appendTo(document.body);
			
			var operationMessage = $.getResponseJson(jqXHR);
			
			var hasMessage = false;
			
			//操作消息的JSON响应
			if(operationMessage && operationMessage.type && operationMessage.code && operationMessage.message)
			{
				$operationMessageParent.empty();
				
				var $omdiv = $("<div class='operation-message "+operationMessage.type+"' />").appendTo($operationMessageParent);
				var $mdiv = $("<div class='message' />").appendTo($omdiv).html(operationMessage.message);
				
				if(operationMessage.detail)
				{
					var $ddiv = $("<div class='message-detail' />").appendTo($omdiv);
					if(operationMessage.throwableDetail)
						$("<pre />").appendTo($ddiv).html(operationMessage.detail);
					else
						$("<div />").appendTo($ddiv).html(operationMessage.detail);
				}
				
				hasMessage = true;
			}
			else
			{
				if(isSuccessMessage)
					hasMessage = false;
				else
				{
					//操作消息的HTML响应
					$operationMessageParent.html(jqXHR.responseText);
					hasMessage = true;
				}
			}
			
			if(hasMessage)
			{
				$operationMessageParent.attr("success", isSuccessMessage);
				var message = $(".message", $operationMessageParent).html();
				
				if($(".message-detail", $operationMessageParent).length > 0)
					message += "<span class='ui-icon ui-icon-comment message-detail-icon' onclick='_showAjaxOperationMessageDetail();'></span>";
				
				if(isSuccessMessage)
					$.tipSuccess(message);
				else
					$.tipError(message);
			}
		}
		else
		{
			if(isSuccessMessage)
				;
			else
			{
				/* 在firefox中，cometd连接有可能会被用户操作请求中断，导致莫名其妙地弹出错误提示，因此把此处代码禁用
				var msg = (jqXHR.statusText || "Error");
				
				if(errorThrown && errorThrown.message)
				{
					if(msg)
						msg += " : ";
					
					msg += errorThrown.message;
				}
				
				$.tipError(msg);
				*/
			}
		}
	};
	
	/**JSON内容类型常量*/
	$.CONTENT_TYPE_JSON = "application/json";
	
	//如果请求内容类型是JSON，则自动将请求数据对象转换为JSON内容
	$.ajaxPrefilter(function( options, originalOptions, jqXHR )
	{
		if(originalOptions.contentType != $.CONTENT_TYPE_JSON)
			return;
		
		if(originalOptions.data)
			options.data = $.toJsonString(originalOptions.data);
	});
	
	$(document).ajaxError(function(event, jqXHR, ajaxSettings, thrownError)
	{
		$.handleAjaxOperationMessage(jqXHR, thrownError);
	});
	
	$(document).ajaxSuccess(function(event, jqXHR, ajaxSettings, thrownError)
	{
		$.handleAjaxOperationMessage(jqXHR, thrownError);
	});
})
(jQuery);
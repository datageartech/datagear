/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

/**
 * 工具函数集。
 * 
 * 依赖:
 * jquery.js
 * jquery-ui.js
 * tableMeta.js
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
		 *              //当target是页内元素时，是否打开为对话框，默认为：true
		 *              asDialog: true,
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
			options = (options || {});
			
			if(options.target == "_blank")
			{
				if(!options.data)
					window.open(url);
				else
				{
					//使用window.open()会使URL超长导致请求失败，因而改为postOnForm
					$.postOnForm(url, {"data" : options.data, "target" : "_blank"});
				}
			}
			else if(options.target == "_file")
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
				
				$.postOnForm(url, {"data" : options.data, "target" : "_blank"});
			}
			else
			{
				var successCallback = [];
				successCallback[0] = function(response)
				{
					var container=$(options.target ? options.target : document.body);
					
					var $dialog = $("<div id='"+$.uid("dialog")+"' class='dialog-content-container'></div>").appendTo(container);
					
					if(options.pageParam)
						$.pageParam($dialog, options.pageParam);
					
					if(options.asDialog)
						$._dialog($dialog, options);
					
					$dialog.html(response);
					
					if(options.asDialog && !options.title)
					{
						var title = $("> title", $dialog).text();
						$dialog.dialog( "option", "title", title);
					}
					
					$("[autofocus]:first", $dialog).focus();
				};
				
				if(options.success)
					successCallback = successCallback.concat(options.success);
				
				options = $.extend(
					{
						title : undefined, 
						target : document.body,
						asDialog: true,
						pageParam : undefined,
						pinTitleButton : false,
						modal : true,
						classes: { "ui-dialog": "ui-corner-all ui-widget-shadow" }
					},
					options,
					{
						data : options.data,
						success : successCallback,
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
		 * @param ele 必选，任意元素
		 * @param param 可选，要设置的参数
		 */
		pageParam : function(ele, param)
		{
			ele = $(ele);
			
			if(param === undefined)
			{
				var dcc = ele.closest("." + $.PAGE_PARAM_BINDER_CLASS);
				return dcc.data("pageParam");
			}
			else
			{
				ele.addClass($.PAGE_PARAM_BINDER_CLASS);
				ele.data("pageParam", param);
			}
		},
		
		/**
		 * 调用页面参数函数。
		 * 如果没有页面参数或者指定的函数，返回undefined。
		 * 
		 * @param ele 任意元素
		 * @param functionName 可选，如果页面参数是对象，则指定页面对象的函数名
		 * @param argArray 可选，函数参数数组
		 */
		pageParamCall : function(ele, functionName, argArray)
		{
			var pageParam = $.pageParam(ele);
			
			//无页面参数
			if(!pageParam)
				return undefined;
			
			//页面参数是函数
			if($.isFunction(pageParam))
				return pageParam.apply(window, arguments[1]);
			
			//页面参数是对象
			var fun = pageParam[functionName];
			return (fun == null ? undefined : fun.apply(pageParam, argArray));
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
				
				$("<button type='button'></button>")
				.button({
					label: $( "<a>" ).text( "pin" ).html(),
					icon: "ui-icon-pin-s",
					showLabel: false
				})
				.appendTo( titlebar )
				.addClass("dialog-titlebar-pin stated-active")
				.click(function()
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
			var $dialogFlag = $(dom).closest(".dialog-content-container.ui-dialog-content");
			
			return ($dialogFlag && $dialogFlag.length > 0);
		},
		
		/**
		 * 获取元素所处的对话框DOM对象，如果不在对话框中，返回一个空的Jquery对象（长度为0）。
		 */
		getInDialog : function(dom)
		{
			var dialog = $(dom).closest(".dialog-content-container.ui-dialog-content");
			
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
			
			var formId = ($.GLOBAL_POST_ON_FORM_ID || ($.GLOBAL_POST_ON_FORM_ID = $.uid("form")));
			var form = $("#"+formId);
			if(form.length == 0)
			{
				form = $("<form />").attr("id", formId)
					.attr("method", "POST")
					.css("width", "0px").css("height", "0px").appendTo(document.body);
			}
			else
				form.empty();
			
			form.attr("action", url).attr("target", options.target);
			
			if(options.data)
			{
				var param = (typeof(options.data) == "string" ? options.data : $.param(options.data));
				
				//XXX $.param会将" "转换为"+"，而这里的decodeURIComponent并不会将"+"恢复为" "，因此需要在这里预先转换
				param = param.replace(/\+/g, " ");
				
				var paramArray = param.split("&");
				
				for(var i=0; i<paramArray.length; i++)
				{
					var paramPair = paramArray[i].split("=");
					
					var name = decodeURIComponent(paramPair[0]);
					var value = decodeURIComponent(paramPair[1]);
	
					$("<input type='hidden' />").attr("name", name).attr("value", value).appendTo(form);
				}
			}
			
			form.submit();
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
			
			var confirmId = ($.GLOBAL_CONFIRM_DIALOG_ID || ($.GLOBAL_CONFIRM_DIALOG_ID = $.uid("confirm")));
			var confirmDialog = $("#"+confirmId);
			
			if(confirmDialog.length == 0)
			{
				confirmDialog = $("<div id='"+confirmId+"' class='dialog-confirm' />").appendTo(document.body);
				var $p = $("<p><span class='ui-icon ui-icon-alert'></span></p>").appendTo(confirmDialog);
				$("<div class='confirm-content' />").appendTo($p);
			}
			
			$(".confirm-content", confirmDialog).html(content);
			
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
			
			confirmDialog.dialog(
			{
				resizable: false,
				height: "auto",
				width: "auto",
				position: {my: "center top", at: "center top+15%"},
				modal: true,
				buttons: buttons,
				title: options.title,
				close : function()
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
			$("input:submit, input:button, input:reset, button, .button", $parent).button();
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
			
			var tooltipId = ($.GLOBAL_TIP_ID || ($.GLOBAL_TIP_ID = $.uid("tip")));
			var tooltip = $("#"+tooltipId);
			
			if(tooltip.length > 0)
				tooltip.tooltip("destroy").remove();
			
			var preTooltipTimeoutCloseId = $._tooltipTimeoutCloseId();
			if(preTooltipTimeoutCloseId)
				window.clearTimeout(preTooltipTimeoutCloseId);
			
			var tooltipParent = document.body;
			var customTooltipParent = $(".tooltip-parent");
			if(customTooltipParent.length > 0)
				tooltipParent = customTooltipParent[0];
			
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
			if($._uid_seq >= Number.MAX_SAFE_INTEGER)
			{
				$._uid_seq = null;
				$._uid_time = null;
			}
			
			var seq = ($._uid_seq == null ? ($._uid_seq = 0) : $._uid_seq);
			var time = ($._uid_time == null ? ($._uid_time = new Date().getTime().toString(16)) : $._uid_time);
			$._uid_seq++;
			
			return (prefix ? prefix : "uid") + time + seq;
		},
		
		toJsonString: function(obj)
		{
			return JSON.stringify(obj);
		},
		
		/**
		 * 表单输入项转JSON对象。
		 * 转换规则：
		 * 基本：
		 * 名         值
		 * a         v
		 * 转换为
		 * {a: "v"}
		 * 
		 * 嵌套：
		 * 名         值
		 * a.b       v
		 * 转换为
		 * {a: {b: "v"}}
		 * 
		 * 数组：
		 * 名         值
		 * a         v0
		 * a         v1
		 * 转换为
		 * {a: ["v0", "v1"]}
		 * 
		 * 数组：
		 * 名        值
		 * a[]       v
		 * 转换为
		 * {a: ["v"]}
		 * 
		 * 数组：
		 * 名         值
		 * a.b       v0
		 * a.b       v1
		 * 转换为
		 * {a: {b: ["v0", "v1"]}}
		 * 
		 * 数组：
		 * 名         值
		 * a.b[0].c  v0
		 * a.b[0].d  v1
		 * a.b[1].e  v2
		 * a.b[1].f  v3
		 * 转换为
		 * {a: {b: [{c: "v0", d: "v1"}, {e: "v2", f: "v3"}]}}
		 * 
		 * @param form 表单元素、表单JQ对象
		 * @param ignore 可选，忽略的表单输入项名称，格式为：字符串、字符串数组，默认为：null
		 */
		formToJson: function(form, ignore)
		{
			form = $(form);
			ignore = (ignore ? ($.isArray(ignore) ? ignore : [ ignore ]) : []);
			
			var json = {};
			var KeyForArray = $.uid("_keyForArray");
			
			var array = form.serializeArray();			
			$(array).each(function(indexInArray)
			{
				var name = this.name;
				
				if($.inArray(name, ignore) >= 0)
					return;
				
				var value = this.value;
				
				var names = $.splitPropertyPath(name);
				var parent = json;
				for(var i=0; i<names.length; i++)
				{
					name = names[i];
					var isEleKey = (name.length >= 2 && name.charAt(0) == '[' && name.charAt(name.length-1) == ']');
					if(isEleKey)
					{
						// []
						if(name.length == 2)
						{
							if(i != names.length - 1)
								throw new Error("'[]' must be at the tail of '"+this.name+"'");
							
							name = "";
						}
						// [...]
						else
							name = name.substring(1, name.length-1);
					}
					
					if(i == names.length - 1)
					{
						//数组元素
						if(isEleKey)
						{
							if(parent[KeyForArray] == undefined)
								parent[KeyForArray] = true;
							
							// [] 格式，取数组索引作为key，符合数组顺序
							if(name == "")
								name = indexInArray+"";
							
							parent[name] = value;
						}
						else
						{
							var preValue = parent[name];
							
							if(preValue == undefined)
								parent[name] = value;
							else
							{
								//末尾允许重名并设为数组，以支持同名表单项
								if($.isArray(preValue))
									preValue.push(value);
								else
								{
									parent[name] = [];
									parent[name].push(preValue);
									parent[name].push(value);
								}
							}
						}
					}
					else
					{
						if(isEleKey && parent[KeyForArray] == undefined)
							parent[KeyForArray] = true;
							
						var myParent = parent[name];
						if(!myParent)
							myParent = (parent[name] = {});
						parent = myParent;
					}
				}
			});
			
			return $._convertKeyForArrayObj(json, KeyForArray);
		},
		
		_convertKeyForArrayObj: function(obj, keyForArray)
		{
			if(!$.isPlainObject(obj))
				return obj;
			
			var isArrayObj = obj[keyForArray];
			var ps = (isArrayObj ? [] : undefined);
			
			for(var p in obj)
			{
				if(p == keyForArray)
					continue;
				
				if(isArrayObj)
					ps.push(p);
				
				obj[p] = $._convertKeyForArrayObj(obj[p], keyForArray);
			}
			
			if(!isArrayObj)
				return obj;
			
			ps.sort(function(a, b)
			{
				if(a.length > b.length)
					return 1;
				else if(a.length < b.length)
					return -1;
				else
					return (a < b ? -1 : 1);
			});
			
			var re = [];
			
			for(var i=0; i<ps.length; i++)
				re.push(obj[ps[i]]);
			
			return re;
		},
		
		/**
		 * 将json数据填充至form内的输入项。
		 * 
		 * @param form 表单元素、JQ对象
		 * @param json 要填充的json数据对象，格式必须为：{...}，支持嵌套属性
		 * @param options 可选，填充选项，格式为：
		 *					{
		 *					  //可选，转换原始值为输入框值
		 *					  serialize: function(form, name, value){ return ""; },
		 *					  //可选，自定义填充处理函数
		 *					  handlers:
		 *					  {
		 *					    //name 输入项名
		 *					    //value 输入项值
		 *					    //返回false表示它只进行了预处理，而没有填充输入项值，由内置逻辑继续填充
		 *					    name: function(form, value){  }
		 *					  }
		 *					}
		 */
		jsonToForm: function(form, json, options)
		{
			options = $.extend(
			{
				serialize: function(form, name, value)
				{
					if($.isPlainObject(value) || $.isArray(value))
						return JSON.stringify(value);
					else
						return (value == null ? "" : value.toString());
				},
				handlers: []
			},
			options);
			
			//json中可能只包含部分数据，这里先构建包含所有输入项名的基础对象，确保下面能处理到所有输入项
			var base = $._jsonToFormBaseJson(form);
			json = $.extend(base, json);
			
			$._jsonToFormInner(form, json, "", options);
		},
		
		/**
		 * 可用于$.jsonToForm()的自定义数组填充支持函数。
		 * 
		 * @param form 表单元素
		 * @param value 值数组，可能为null
		 * @param wrapperSelector 表单内用于添加数组元素输入项条目的容器元素选择器
		 * @param addItemHandler 添加数组元素输入项条目的处理函数，格式为：function(wrapper, index, addTotal){ ... }，
		 *						 要添加的数组元素输入项必须直接位于wrapper元素内
		 * @param removeItemHandler 可选，删除数组元素输入项条目的处理函数，格式为：function(wrapper, item){}，
		 *						    默认为：删除item DOM元素
		 */
		jsonToFormArrayHandler: function(form, value, wrapperSelector, addItemHandler, removeItemHandler)
		{
			var wrapper = $(wrapperSelector, form);
			var items = $("> *", wrapper);
			var valueLen = (!value ? 0 : value.length);
			
			items.each(function(i)
			{
				if(i >= valueLen)
				{
					if(removeItemHandler)
						removeItemHandler(wrapper, $(this));
					else
						$(this).remove();
				}
			});
			
			var addCount = (valueLen - items.length);
			for(var i=0; i<addCount; i++)
			{
				addItemHandler(wrapper, value[i], i, addCount);
			}
			
			return false;
		},
		
		_jsonToFormBaseJson: function(form)
		{
			var base = {};
			
			$(":input[name]", form).each(function()
			{
				var name = $(this).attr("name");
				if(name)
				{
					name = $.splitPropertyPath(name)[0];
					base[name] = undefined;
				}
			});
			
			return base;
		},
		
		_jsonToFormInner: function(form, value, path, options)
		{
			if(options.handlers[path])
			{
				var fullHandled = options.handlers[path].call(options.handlers, form, value);
				if(fullHandled !== false)
					return;
			}
			
			var isPlainObj = $.isPlainObject(value);
			var isArray = (!isPlainObj && $.isArray(value));
			var maybeArray = (value == null || isArray);
			
			var input = $(":input[name='"+path+"']", form);
			
			if(input.length == 0)
			{
				if(isPlainObj)
				{
					for(var name in value)
						$._jsonToFormInner(form, value[name], (path ? path+"."+name : name), options);
					
					return;
				}
				
				if(maybeArray)
					input = $(":input[name='"+path+"[]']", form);
				
				if(input.length == 0 && isArray)
				{
					for(var i=0; i<value.length; i++)
						$._jsonToFormInner(form, value[i], path+"["+i+"]", options);
					
					return;
				}
			}
			
			if(input.length == 0)
				return;
			
			var inputType = (input.attr("type") || "text").toLowerCase();
			var inputName = input.attr("name");
			var values = (value == null ? [] : (isArray ? value : [ value ]));
			
			//textarea的inputType也是"text"
			if(inputType == "text")
			{
				input.each(function(i)
				{
					var myVal = options.serialize(form, inputName, values[i]);
					$(this).val(myVal);
				});
			}
			else if(inputType == "radio")
			{
				var myVal = options.serialize(form, inputName, values[0]);
				
				input.each(function()
				{
					var $this = $(this);
					var checked = ($this.attr("value") == myVal);
					$this.prop("checked", checked);
					
					var eleWidget = $this.data("ui-checkboxradio");
					if(eleWidget)
						eleWidget.refresh();
				});
			}
			else if(inputType == "checkbox")
			{
				input.each(function(i)
				{
					var $this = $(this);
					var myVal = options.serialize(form, inputName, values[i]);
					var checked = ($this.attr("value") == myVal);
					$this.prop("checked", checked);
					
					var eleWidget = $this.data("ui-checkboxradio");
					if(eleWidget)
						eleWidget.refresh();
				});
			}
			else if(inputType == "select")
			{
				var multiple = input.attr("multiple");
				var val0 = (multiple ? undefined : options.serialize(form, inputName, values[0]));
				
				$("option", input).each(function(i)
				{
					var $this = $(this);
					var myVal = (multiple ? options.serialize(form, inputName, values[i]) : val0);
					var checked = ($this.attr("value") == myVal);
					$this.prop("checked", checked);
				});
				
				var eleWidget = $this.data("ui-selectmenu");
				if(eleWidget)
					eleWidget.refresh();
			}
			else
				throw new Error("Unknown input type :" + inputType);
		},
		
		/**
		 * 将对象转换为属性路径映射表。
		 * 
		 * @param json
		 * @param flattenArray 可选，是否转换数组，默认为：true
		 * @returns { ... }, 不会为null
		 */
		jsonToPropertyPathMap: function(json, flattenArray)
		{
			flattenArray = (flattenArray == null ? true : flattenArray);
			
			var map = {};
			$._jsonToPropertyPathMap(map, json, "", flattenArray);
			return map;
		},
		
		_jsonToPropertyPathMap: function(map, json, parentPath, flattenArray)
		{
			parentPath = (parentPath == null ? "" : parentPath);
			
			if($.isArray(json))
			{
				if(flattenArray)
				{
					for(var i=0; i<json.length; i++)
						$._jsonToPropertyPathMap(map, json[i], parentPath+"["+i+"]", true);
				}
				else
				{
					parentPath = parentPath +"[]";
					map[parentPath] = json;
				}
			}
			else if($.isPlainObject(json))
			{
				for(var p in json)
					$._jsonToPropertyPathMap(map, json[p], parentPath+"."+p, flattenArray);
			}
			else
				map[parentPath] = json;
		},
		
		/**
		 * 拆分属性路径字符串为数组。
		 * @str 属性路径字符串，格式为："a.b[0].c"，拆分为：["a", "b", "[0]", "c"]
		 */
		splitPropertyPath: function(str)
		{
			var array = [];
			
			var ele = "";
			for(var i=0; i<str.length; i++)
			{
				var c = str.charAt(i);
				
				if(c == '\\')
				{
					if((i + 1) < str.length)
						ele += str.charAt(i+1);
					i+=1;
				}
				else if(c == '.')
				{
					if(ele)
						array.push(ele);
					ele = "";
				}
				else if(c == '[')
				{
					if(ele)
						array.push(ele);
					ele = c;
				}
				else if(c == ']')
				{
					if(ele)
						array.push(ele+c);
					ele = "";
				}
				else
					ele += c;
			}
			
			if(ele)
				array.push(ele);
			
			return array;
		},
		
		/**
		 * 获取对象/对象数组指定名称属性值。
		 * 
		 * @param obj 对象、对象数组
		 * @param name 属性名
		 */
		propertyValue: function(obj, name)
		{
			var isArray = $.isArray(obj);
			var array = (isArray? obj : [obj]);
			
			var re = [];
			for(var i=0; i<array.length; i++)
				re[i] = array[i][name];
			
			return (isArray? re : re[0]);
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
			
			return $.tableMeta.escapeHtml(text);
		},
		
		/**
		 * 将字符串按照'/'或'\'路径分隔符拆分。
		 */
		splitAsPath: function(str, keepSeparator)
		{
			str = (str || "");
			keepSeparator = (keepSeparator == null ? true : keepSeparator);
			
			var re = [];
			
			resName = str.replace("\\", "/");
			var rns = str.split("/");
			
			for(var i=0; i<rns.length; i++)
			{
				if(rns[i])
					re.push(rns[i]);
			}
			
			if(keepSeparator)
			{
				for(var i=0; i<re.length; i++)
				{
					if(i < re.length - 1)
						re[i] = re[i] + "/";
					
					if(i == 0 && str.charAt(0) == '/')
						re[i] = "/" + re[i];
					
					if(i == re.length - 1 && str.charAt(str.length - 1) == '/')
						re[i] = re[i] + "/";
				}
			}
			
			return re;
		},
		
		/**
		 * 将路径字符串数组转换为路径树。
		 * ["a/b/c", "a/b/d", "f/g"]
		 * 转换为
		 * [
		 *   {name:'a/', children: [ {name: "b/", children: [ {name: "c"}, {name: "d"} ]} ]},
		 *   {name: "f", children: [ {name: "g"} ]}
		 * ]
		 *
		 * @param strs 路径字符串数组
		 * @param options 配置选项：
		 *				{
		 *				  nameProperty: "name",
		 *				  childrenProperty: "children",
		 *				  fullPathProperty: "fullPath",
		 *				  keepSeparator: true,
		 *				  created: function(node){ ... }
		 *				}
		 */
		toPathTree: function(strs, options)
		{
			strs = (strs || []);
			options = $.extend(
			{
				nameProperty: "name",
				childrenProperty: "children",
				fullPathProperty: "fullPath",
				keepSeparator: true,
				created: undefined
			},
			options);
			
			var re = [];
			
			for(var i=0; i<strs.length; i++)
			{
				var nodes = $.splitAsPath(strs[i], options.keepSeparator);
				
				var parent = re;
				
				for(var j=0; j<nodes.length; j++)
				{
					var ni = nodes[j];
					var idx = $.findInArray(parent, ni, options.nameProperty);
					
					if(j == nodes.length - 1)
					{
						if(idx < 0)
						{
							var p = {};
							p[options.nameProperty] = ni;
							p[options.fullPathProperty] = strs[i];
							if(options.created)
								options.created(p);
							
							parent.push(p);
						}
					}
					else
					{
						if(idx < 0)
						{
							var p = {};
							p[options.nameProperty] = ni;
							p[options.fullPathProperty] = $.concatPathArray(nodes, 0, j+1);
							p[options.childrenProperty] = [];
							if(options.created)
								options.created(p);
								
							parent.push(p);
							parent = p[options.childrenProperty];
						}
						else
						{
							if(!parent[idx][options.childrenProperty])
								parent[idx][options.childrenProperty] = [];
							
							parent = parent[idx][options.childrenProperty];
						}
					}
				}
			}
			
			return re;
		},
		
		concatPathArray: function(paths, start, end)
		{
			start = (start == null ? 0 : start);
			end = (end == null ? paths.length : Math.min(paths.length, end));
			
			var re = "";
			
			for(var i=start; i<end; i++)
			{
				var p = paths[i];
				
				if(!re)
					re = p;
				else if(re.charAt(re.length - 1) != '/' && p.charAt(p.length - 1) != '')
					re += "/" + p;
				else
					re += p;
			}
			
			return re;
		},
		
		/**
		 * 查找数组，返回查找索引，-1表示未找到。
		 * @param array
		 * @param value 要查找的值
		 * @param property 可选，字符串，要匹配的数组元素对象属性名
		 * @param returnElement 可选，布尔值，是否返回找到的元素而非索引，默认为false
		 */
		findInArray: function(array, value, property, returnElement)
		{
			//(array, value, true|false)
			if(arguments.length == 3 && (property === true || property === false))
			{
				returnElement = property;
				property = undefined;
			}
			
			if(!array)
				return (returnElement ? undefined : -1);
			
			for(var i=0; i<array.length; i++)
			{
				var eleVal = array[i];
				
				if(property != null)
					eleVal = (eleVal != null ? eleVal[property] : null);
				
				if(eleVal == value)
					return ((returnElement ? array[i] : i));
			}
			
			return (returnElement ? undefined : -1);
		},
		
		/**
		 * 为DataTables转义列名。
		 * 参考jquery.dataTables.js的_fnSplitObjNotation函数。
		 */
		escapeColumnNameForDataTable : function(columnName)
		{
			return columnName.replace(".", "\\.");
		},
		
		/**
		 * 反转义由escapeColumnNameForDataTable转义的列名。
		 */
		unescapeColumnNameForDataTable : function(columnName)
		{
			return columnName.replace("\\.", ".");
		},
		
		buildDataTablesColumnSimpleOption : function(title, data, hidden, noTruncate)
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
			
			if(noTruncate)
			{
				option.render = function(data, type, row, meta)
				{
					return $.escapeHtml(data);
				};
			}
			
			return option;
		},
		
		buildDataTablesColumnTitleWithTip : function(titleName, titleTip)
		{
			return "<a class='tip-label' title='"+$.escapeHtml(titleTip)+"'>"+$.escapeHtml(titleName)+"</a>";
		},
		
		/**
		 * 构建可通过关键字查询的列标题
		 */
		buildDataTablesColumnTitleSearchable : function(titleName)
		{
			return "<a class='keyword-search-column'>"+$.escapeHtml(titleName)+"</a>";
		},
		
		/**
		 * 构建Datatables组件的“columns”选项值。
		 * 
		 * @param dbTable 必选，表
		 * @param options 选项，格式为：
		 * 			{
		 * 				//可选，字符串最大显示长度
		 * 				stringDisplayThreshold : 47,
		 * 
		 * 				//关键字查询列数
		 * 				keywordQueryColumnCount : undefined,
		 * 
		 * 				//可选，单元格渲染后置处理函数
		 * 				postRender : function(data, type, rowData, meta, rowIndex, renderValue, table, column, dtColumn){}
		 * 			}
		 * @returns {Array}
		 */
		buildDataTablesColumns : function(dbTable, options)
		{
			options = $.extend({ stringDisplayThreshold : 47, keywordQueryColumnCount: -1 }, options);
			
			var columns = dbTable.columns;
			
			var dtColumns = [];
			for(var i=0; i<columns.length; i++)
			{
				var column = columns[i];
				
				var disable = !$.tableMeta.supportsColumn(column);
				var isKeywordSearchCol = (i < options.keywordQueryColumnCount && $.tableMeta.isKeywordSearchColumn(column));
				
				dtColumns.push(
				{
					title: $.tableMeta.displayInfoHtml(column, "a", (isKeywordSearchCol ? "keyword-search-column" : "")),
					data: $.escapeColumnNameForDataTable(column.name),
					columnIndex: i,
					columnName: column.name,
					options : options,
					render: function(data, type, row, meta)
					{
						var renderValue = "";
						
						var _this = meta.settings.aoColumns[meta.col];
						
						var columnIndex = _this.columnIndex;
						var column = $.tableMeta.column(dbTable, columnIndex);
						
						renderValue = $.tableMeta.labelOfLabeledValue(data);
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
							
							return _this.options.postRender(data, type, row, meta, rowIndex, renderValue, dbTable, column, _this);
						}
						else
							return renderValue;
					},
					defaultContent: "",
					orderable: column.sortable,
					searchable: true,
					className: (disable ? "ui-state-disabled" : "")
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
			
			for(var i=0; i<columnInfos.length; i++)
			{
				if(columnName == columnInfos[i].data || columnName == columnInfos[i].columnName)
					return i;
			}
			
			return -1;
		},

		/**
		 * 获取列名
		 */
		getDataTableColumnName : function(settings, column)
		{
			var columnInfos = this.getDataTableColumnInfos(settings);
			var dtColumn = columnInfos[column];
			
			return (dtColumn.columnName || dtColumn.data);
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
			return $.getDataTableColumnName(settings, cellIndex.column);
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
				var columnName = (columnInfos[index.column].columnName || columnInfos[index.column].data);
				
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
			options.height = $(window).height() * 0.8;
		},
		
		isDatatTable : function($table)
		{
			return $table.hasClass("dataTable");
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
					height = height + "px";
					var tableParent = $(dataTable.table().body()).parent().parent();
					tableParent.css("height", height);
					tableParent.css("max-height", height);
				}
				
				if(adjustWidth)
					$.dataTableUtil.adjustColumn(dataTable);
			}
		},
		
		/**可显示、隐藏的包含DataTable的面板样式标识*/
		TOGGLABLE_PANEL_CLASS_NAME : "dg-togglable-panel",
		
		/** 自动计算尺寸元素类名标识 */
		AUTO_RESIZEABLE_ELE_CLASS_NAME: "dg-auto-resizable-ele",
		
		/**
		 * 获取/设置是否在显示时调用$.resizeAutoResizable()函数
		 */
		resizeAutoResizableOnShow: function(panel, autoResize)
		{
			if(autoResize == null)
				return (panel.data("resizeAutoResizable") == true);
			else
				panel.data("resizeAutoResizable", autoResize);
		},
		
		/**
		 * 重新调整（或在显示时调整）元素内所有$.AUTO_RESIZEABLE_ELE_CLASS_NAME元素的尺寸。
		 */
		resizeAutoResizable : function(ele, resizeHandler)
		{
			$("."+$.AUTO_RESIZEABLE_ELE_CLASS_NAME, ele).each(function()
			{
				var thisEle = $(this);
				
				//忽略隐藏选项卡中的尺寸调整，仅在选项卡显示时才调整，
				//一是DataTables对隐藏表格的宽度计算有问题，另外，绑定太多处理函数会影响$.resizeable组件的效率
				var toggablePanel = thisEle.closest(".ui-tabs-panel, ." + $.TOGGLABLE_PANEL_CLASS_NAME);
				if(toggablePanel.length > 0 && toggablePanel.is(":hidden"))
				{
					var prevBindHandler = toggablePanel.data("resizeHandlerPrevBind");
					if(resizeHandler != prevBindHandler)
					{
						toggablePanel.data("resizeHandlerPrevBind", resizeHandler);
						
						$.bindPanelShowCallback(toggablePanel, function(panel)
						{
							if(!$.resizeAutoResizableOnShow(panel))
								return;
							
							$.resizeAutoResizable(panel, resizeHandler);
							$.resizeAutoResizableOnShow(panel, false);
						},
						"resizeAutoResizable");
					}
					
					$.resizeAutoResizableOnShow(toggablePanel, true);
				}
				else
				{
					resizeHandler(this);
				}
			});
		},
		
		/**
		 * 为元素内的所有$.AUTO_RESIZEABLE_ELE_CLASS_NAME元素绑定自动尺寸处理函数。
		 * 
		 * @param eleId 元素ID
		 * @param resizeHandler 计算尺寸回调函数，格式为：function(ele){}
		 */
		bindAutoResizableHandler : function(eleId, resizeHandler)
		{
			var resizeTimoutIdName = $.uid("resizeTimoutId");
			
			$(window).bind('resize', function()
			{
				var ele = $("#"+eleId);
				
				if(ele.length < 1)
					return;
				
				var resizeTimoutId = ele.data(resizeTimoutIdName);
				
				if(resizeTimoutId)
					clearTimeout(resizeTimoutId);
				
				resizeTimoutId = setTimeout(function()
				{
					$.resizeAutoResizable(ele, resizeHandler);
				},
				200);
				
				ele.data(resizeTimoutIdName, resizeTimoutId);
			});
		},
		
		PANEL_SHOW_CALLBACK_NAME: "dg-panel-show-callback",
		
		/**
		 * 为指定面板元素绑定显示时回调函数。
		 */
		bindPanelShowCallback: function(panel, callback, name)
		{
			panel = $(panel);
			name = (name == null ? $.uid("cb") : name);
			
			var callbacks = panel.data($.PANEL_SHOW_CALLBACK_NAME);
			if(callbacks == null)
			{
				callbacks = [];
				panel.data($.PANEL_SHOW_CALLBACK_NAME, callbacks);
			}
			
			var callbackObj = { name: name, value: callback };
			
			var idx = -1;
			for(var i=0; i<callbacks.length; i++)
			{
				if(callbacks[i].name == "name")
				{
					idx = i;
					break;
				}
			}
			
			if(idx >= 0)
				callbacks[idx] = callbackObj;
			else
				callbacks.push(callbackObj);
		},
		
		/**
		 * 调用面板元素绑定的显示时回调函数。
		 */
		callPanelShowCallback : function(panel, recursion)
		{
			panel = $(panel);
			recursion = (recursion == null ? true : recursion);
			
			var callbacks = panel.data($.PANEL_SHOW_CALLBACK_NAME);
			if(callbacks)
			{
				for(var i=0; i<callbacks.length; i++)
					callbacks[i].value(panel);
			}
			
			if(recursion)
			{
				var subTabs = panel.find(".ui-tabs");
				if(subTabs.length > 0)
				{
					subTabs.each(function()
					{
						var subTab = $(this);
						
						var subTabsNav = $("> .ui-tabs-nav", subTab);
						var subShowTab = $("> li.ui-tabs-tab.ui-state-active", subTabsNav);
						
						var subTabId = $("> a.ui-tabs-anchor", subShowTab).attr("href");
						if(subTabId.charAt(0) == "#")
							subTabId = subTabId.substr(1);
						
						var subTabPanel = $("> #"+subTabId, subTab);
						
						$.callPanelShowCallback(subTabPanel, false);
					});
				}
			}
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
		 * 提交JSON数据。
		 */
		postJson: function(url, data, success)
		{
			$.ajaxJson(url, { data: data, success: success });
		},
		
		/**
		 * ajax提交JSON数据。
		 * 
		 * @param url 可选
		 * @param options 必选
		 */
		ajaxJson: function(url, options)
		{
			if(options === undefined)
			{
				options = url;
				options.contentType = $.CONTENT_TYPE_JSON;
				options.type = "POST";
				$.ajax(options);
			}
			else
			{
				options.contentType = $.CONTENT_TYPE_JSON;
				options.type = "POST";
				$.ajax(url, options);
			}
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
						 || c == ">" || c == "|" || c == "`")
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
		},
		
		isHtmlFile: function(fileName)
		{
			var htmlReg = /\.(html|htm)$/gi;
			return (fileName && htmlReg.test(fileName));
		},
		
		isJsFile: function(fileName)
		{
			var jsReg = /\.(js)$/gi;
			return (fileName && jsReg.test(fileName));
		},
		
		isCssFile: function(fileName)
		{
			var cssReg = /\.(css)$/gi;
			return (fileName && cssReg.test(fileName));
		},
		
		isTextFile: function(fileName)
		{
			var reg = /\.(html|htm|css|js|json|xml|txt)$/gi;
			return reg.test(fileName);
		}
	});
	
	//DataTable常用函数
	$.dataTableUtil = ($.dataTableUtil || {});
	$.extend($.dataTableUtil,
	{
		isDisplayType: function(type)
		{
			return type == "display";
		},
		
		renderCheckColumn: function(data, type, row, meta)
		{
			return "<div class='ui-widget ui-widget-content ui-corner-all checkbox'><span class='ui-icon ui-icon-check'></span></div>";
		},
		
		getOrdersOnName: function(dataTable)
		{
			var settings = dataTable.settings();
			var orders = dataTable.order();
			
			var nameOrder = [];
			
			for(var i=0; i<orders.length; i++)
			{
				var name = $.getDataTableColumnName(settings, orders[i][0]);
				nameOrder[i] = { "name" : name, "type" : orders[i][1] };
			}
			
			return nameOrder;
		},
		
		buildCheckCloumn: function(title)
		{
			var column=
			{
				title : title,
				data : null,
				defaultContent: "",
				width : "3em",
				orderable : false,
				render : $.dataTableUtil.renderCheckColumn,
				className : "column-check"
			};
			
			return column;
		},
		
		bindCheckColumnEvent: function(dataTable)
		{
			//表头选中框
			$(".column-check", $(dataTable.table().header())).click(function()
			{
				var $this = $(this);
				var checked = $this.hasClass("all-checked");
				
				var rows = dataTable.rows();
				
				if(checked)
				{
					rows.deselect();
					$this.removeClass("all-checked");
				}
				else
				{
					rows.select();
					$this.addClass("all-checked");
				}
			});
			
			//不加这一行，对话框中的初始空数据客户端表格添加记录后表头“选择”点击不起作用
			$.dataTableUtil.adjustColumn(dataTable);
			
			//行选中框
			$(dataTable.table().body()).on("click", ".column-check", function(event)
			{
				event.stopPropagation();
				
				var $tr = $(this).closest("tr");
				var isSelected = $tr.hasClass("selected");
				
				if(event.shiftKey)
				{
					var myIndex = $tr.index();
					
					var rangeStart = -1;
					var rangeEnd = -1;
					
					var $preTr;
					
					var test = $tr.prevUntil(":not(.selected)");
					
					if(isSelected)
						$preTr = $tr.prevUntil(":not(.selected)").last();
					else
						$preTr = $tr.prevAll(".selected:first");
					
					if($preTr.length > 0)
					{
						rangeStart = $preTr.index();
						rangeEnd = myIndex + 1;
					}
					else
					{
						var $nextTr;
						
						if(isSelected)
							$nextTr = $tr.nextUntil(":not(.selected)").last();
						else
							$nextTr = $tr.nextAll(".selected:first");
						
						if($nextTr.length > 0)
						{
							rangeStart = myIndex;
							rangeEnd = $nextTr.index() + 1;
						}
						else
						{
							rangeStart = myIndex;
							rangeEnd = myIndex + 1;
						}
					}
					
					var selectedIndexes = [];
					
					for(var i=rangeStart; i<rangeEnd; i++)
						selectedIndexes.push(i);
					
					if(isSelected)
						dataTable.rows(selectedIndexes).deselect();
					else
						dataTable.rows(selectedIndexes).select();
				}
				else
				{
					if(isSelected)
						dataTable.row($tr).deselect();
					else
					{
						dataTable.row($tr).select();
					}
				}
			});
		},
		
		executeOnSelect: function(dataTable, illegalTip, callback)
		{
			var rows = dataTable.rows('.selected');
			var rowsData = $.dataTableUtil.getRowsData(dataTable, rows);
			
			if(!rowsData || rowsData.length != 1)
				$.tipInfo(illegalTip);
			else
			{
				callback(rowsData[0], $.dataTableUtil.getRowsIndex(dataTable, rows)[0]);
			}
		},
		
		executeOnSelects: function(dataTable, illegalTip, callback)
		{
			var rows = dataTable.rows('.selected');
			var rowsData = $.dataTableUtil.getRowsData(dataTable, rows);
			
			if(!rowsData || rowsData.length < 1)
				$.tipInfo(illegalTip);
			else
			{
				callback(rowsData, $.dataTableUtil.getRowsIndex(dataTable, rows));
			}
		},
		
		getSelectedData: function(dataTable)
		{
			var rows = dataTable.rows('.selected');
			var rowsData = $.dataTableUtil.getRowsData(dataTable, rows);
			
			return (rowsData || []);
		},
		
		getRowsData: function(dataTable, rows)
		{
			if(rows == undefined)
				rows = dataTable.rows();
			
			var tableRowsData = rows.data();
			
			var rowsData = [];
			for(var i=0; i<tableRowsData.length; i++)
				rowsData[i] = tableRowsData[i];
			
			return rowsData;
		},
		
		getRowsIndex: function(dataTable, rows)
		{
			if(rows == undefined)
				rows = dataTable.rows();
			
			var indexes = rows.indexes();
			return indexes;
		},
		
		addRowData: function(dataTable, data)
		{
			if($.isArray(data))
				dataTable.rows.add(data).draw();
			else
				dataTable.row.add(data).draw();
		},
		
		setRowData: function(dataTable, rowIndex, data)
		{
			if(rowIndex.length != undefined)
			{
				for(var i=0; i< rowIndex.length; i++)
				{
					dataTable.row(rowIndex[i]).data(data[i]).draw();
				}
			}
			else
				dataTable.row(rowIndex).data(data).draw();
		},
		
		deleteRow: function(dataTable, rowIndex)
		{
			if(rowIndex.length != undefined)
				dataTable.rows(rowIndex).remove().draw();
			else
				dataTable.row(rowIndex).remove().draw();
		},
		
		deleteAllRow: function(dataTable)
		{
			dataTable.rows().remove();
		},
		
		deleteSelectedRows: function(dataTable)
		{
			var indexes = dataTable.rows('.selected').indexes();
			dataTable.rows(indexes).remove().draw();
		},
		
		moveSelectedUp: function(dataTable, notDraw)
		{
			dataTable.rows('.selected').every(function(rowIdx, tableLoop, rowLoop)
			{
				if(rowIdx <= 0)
					return;
				
				var prevRow = dataTable.row(rowIdx - 1);
				
				if($(prevRow.node()).hasClass("selected"))
					return;
				
				var prevRowData = prevRow.data();
				var myRowData = this.data();
				prevRow.data(myRowData);
				this.data(prevRowData);
				prevRow.select();
				this.deselect();
			});
			
			if(notDraw != true)
				dataTable.draw();
		},
		
		moveSelectedDown: function(dataTable, notDraw)
		{
			var count = dataTable.rows().indexes().length;
			
			dataTable.rows('.selected').every(function(rowIdx, tableLoop, rowLoop)
			{
				if(rowIdx >= count - 1)
					return;
				
				var nextRow = dataTable.row(rowIdx + 1);
				
				if($(nextRow.node()).hasClass("selected"))
					return;
				
				var nextRowData = nextRow.data();
				var myRowData = this.data();
				nextRow.data(myRowData);
				this.data(nextRowData);
				nextRow.select();
				this.deselect();
			});
			
			if(notDraw != true)
				dataTable.draw();
		},
		
		dataTableParent: function(dataTable)
		{
			var $tableParent = $(dataTable.table().body()).parent().parent();
			return $tableParent;
		},
		
		adjustColumn: function(dataTable)
		{
			dataTable.columns.adjust();
			
			var initOptions = dataTable.init();
			
			if(initOptions.fixedHeader)
				dataTable.fixedHeader.adjust();
			
			/*
			if(initOptions.fixedColumns)
				dataTable.fixedColumns.relayout();
			*/
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

	//ajaxSettings.tipSuccess 是否提示成功操作消息，默认为：true
	//ajaxSettings.tipError 是否提示错误操作消息，默认为：true
	$.handleAjaxOperationMessage = function(event, jqXHR, ajaxSettings, data, thrownError)
	{
		var ompId = ($.GLOBAL_OPT_MSG_ID || ($.GLOBAL_OPT_MSG_ID = $.uid("opt")));
		
		if(!window._showAjaxOperationMessageDetail)
		{
			window._showAjaxOperationMessageDetail = function()
			{
				$.closeTip();
				
				var $omp = $("#"+$.GLOBAL_OPT_MSG_ID);
				
				var isSuccessMessage = ("true" == $omp.attr("success"));
				
				var $dialog = $("<div id='dialog-"+new Date().getTime()+"' class='operation-message-dialog'></div>").appendTo(document.body);
				
				var $messageDetail = $("<div class='message-detail' />");
				if(!isSuccessMessage)
					$messageDetail.addClass("ui-state-error");
				$messageDetail.appendTo($dialog);
				$messageDetail.html($(".message-detail", $omp).html());
				
				$._dialog($dialog,
						{
							title : $(".message", $omp).text(),
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
		
		var $omp = $("#"+ompId);
		if($omp.length == 0)
			$omp = $("<div id='"+ompId+"' style='display:none;' />").appendTo(document.body);
		
		var isSuccessResponse = (jqXHR.status == 200);
		var hasResponseMessage = false;
		
		if(jqXHR.responseText)
		{
			var operationMessage = $.getResponseJson(jqXHR);
			
			//响应为JSON操作消息的
			if(operationMessage && operationMessage.type && operationMessage.code && operationMessage.message)
			{
				$omp.empty();
				
				var $omdiv = $("<div class='operation-message "+operationMessage.type+"' />").appendTo($omp);
				$("<div class='message' />").appendTo($omdiv).html(operationMessage.message);
				
				if(operationMessage.detail)
				{
					var $ddiv = $("<div class='message-detail' />").appendTo($omdiv);
					if(operationMessage.throwableDetail)
						$("<pre />").appendTo($ddiv).html(operationMessage.detail);
					else
						$("<div />").appendTo($ddiv).html(operationMessage.detail);
				}
				
				hasResponseMessage = true;
			}
			else
			{
				var rtPrefix = jqXHR.responseText.substr(0, 100);
				var dpnValue = null;
				
				var dpnToken = "dg-page-name=\"";
				var dpnStartIdx = rtPrefix.indexOf(dpnToken);
				if(dpnStartIdx > -1)
				{
					dpnStartIdx = dpnStartIdx + dpnToken.length;
					var dpnEndIdx = rtPrefix.indexOf("\"", dpnStartIdx);
					dpnValue = (dpnEndIdx > dpnStartIdx ? rtPrefix.substring(dpnStartIdx, dpnEndIdx) : null);
				}
				
				//响应为HTML操作消息的
				if(dpnValue == "error")
				{
					$omp.html(jqXHR.responseText);
					hasResponseMessage = true;
				}
				//当登录超时后，列表页点击【查询】按钮，ajax响应可能会重定向到登录页，这里特殊处理
				else if(dpnValue == "login")
				{
					var url = ajaxSettings.url;
					
					if(url && url.indexOf("/login") < 0)
					{
						thrownError = "Login expired";
						hasResponseMessage = false;
					}
				}
			}
		}
		
		var isTipSuccess = (ajaxSettings.tipSuccess !== false);
		var isTipError = (ajaxSettings.tipError !== false);
		
		if(hasResponseMessage)
		{
			$omp.attr("success", isSuccessResponse);
			var message = $(".message", $omp).html();
			
			if($(".message-detail", $omp).length > 0)
				message += "<span class='ui-icon ui-icon-comment message-detail-icon' onclick='_showAjaxOperationMessageDetail();'></span>";
			
			if(isSuccessResponse && isTipSuccess)
				$.tipSuccess(message);
			else if(isTipError)
				$.tipError(message);
		}
		//客户端处理ajax响应出错
		else if(thrownError)
		{
			if(isTipError)
				$.tipError(thrownError);
		}
		//客户端连接出错
		else if(event && event.type=="ajaxError")
		{
			if(isTipError)
				$.tipError("Error");
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
		$.handleAjaxOperationMessage(event, jqXHR, ajaxSettings, null, thrownError);
	});
	
	$(document).ajaxSuccess(function(event, jqXHR, ajaxSettings, data)
	{
		$.handleAjaxOperationMessage(event, jqXHR, ajaxSettings, data, null);
	});
	
	/**
	 * 创建任务客户端。
	 * 任务客户端接收任务消息，直到任务完成。
	 *
	 * @param url 任务消息响应URL
	 * @param messageHandler 消息处理器，格式为：function(message){ return true || false }，返回true表示任务已完成
	 * @param options 可选，附加选项
	 */
	$.TaskClient = function(url, messageHandler, options)
	{
		this.url = url;
		this.messageHandler = messageHandler;
		this._status = "";
		this.options = $.extend(
				{
					//轮询间隔
					interval: 100,
					//挂起状态时的轮询间隔
					suspendInterval: 3000,
					//当连续接收空消息这些秒数后，自动进入挂起状态，-1 表示不自动挂起
					autoSuspendExpireSeconds: 10,
					//自动挂起状态时的轮询间隔
					autoSuspendInterval: 1500,
					//ajax设置项
					ajaxOptions: {}
				},
				options);
	};
	
	$.TaskClient.prototype =
	{
		//开始轮询接收消息
		start: function()
		{
			if(this.isActive())
				return false;
			
			this._status = "active.run";
			this._receiveAndHandleMessage();
			
			return true;
		},
		
		//挂起，进入慢轮询状态
		suspend: function()
		{
			if(!this.isActive())
				return false;
			
			this._status = "active.suspend";
			
			return true;
		},
		
		//唤醒，从慢轮询状态恢复
		resume: function()
		{
			if(!this.isSuspend())
				return false;
			
			this.stop();
			this.start();
			
			return true;
		},
		
		//停止轮询接收消息，停止后可重新start
		stop: function()
		{
			if(!this.isActive())
				return false;
			
			this._status = "stop";
			if(this._timeoutId)
			{
				clearTimeout(this._timeoutId);
				this._timeoutId = "";
			}
			
			return true;
		},
		
		isActive: function()
		{
			return (this._status && this._status.indexOf("active") == 0);
		},
		
		isSuspend: function()
		{
			return (this._status == "active.suspend");
		},
		
		_receiveAndHandleMessage: function()
		{
			if(!this.isActive())
				return;
			
			var taskClient = this;
			
			var ajaxOptions = $.extend({}, this.options.ajaxOptions,
					{
						type : "POST",
						url : this.url,
						data : this.options.data,
						success : function(messages)
						{
							if(messages == null)
								messages = [];
							else if(!$.isArray(messages))
								messages = [ messages ];
							
							var isFinish = false;
							
							for(var i=0; i<messages.length; i++)
							{
								var myIsFinish = taskClient.messageHandler(messages[i]);
								
								if(!isFinish && myIsFinish === true)
									isFinish = true;
							}
							
							if(isFinish)
								taskClient._status = "stop";
							
							//处理自动挂起
							var autoSuspend = false;
							if(taskClient.options.autoSuspendExpireSeconds > -1)
							{
								if(messages.length > 0)
								{
									autoSuspend = false;
									taskClient._firstEmptyTime = null;
								}
								else
								{
									if(taskClient._firstEmptyTime
											&& (new Date().getTime() - taskClient._firstEmptyTime)
													>= taskClient.options.autoSuspendExpireSeconds*1000)
									{
										autoSuspend = true;
									}
									
									if(!taskClient._prevMessagesEmpty)
										taskClient._firstEmptyTime = new Date().getTime();
								}
								
								taskClient._prevMessagesEmpty = (messages.length == 0);
							}
							
							if(taskClient.isActive())
							{
								var interval = (taskClient.isSuspend() || autoSuspend ?
										taskClient.options.suspendInterval : taskClient.options.interval);
								
								if(autoSuspend)
									interval = taskClient.options.autoSuspendInterval;
								
								taskClient._timeoutId = setTimeout(function()
										{
											taskClient._receiveAndHandleMessage();
										},
										interval);
							}
						}
					});
			
			$.ajax(ajaxOptions);
		}
	};
})
(jQuery);

(function($, undefined)
{
	$.fn.extend(
	{
		/**
		 * 单选、复选框controlgroup，元素应是一个包含单选或复选框的上级元素。
		 * 
		 * @param options 可选，checkboxradio、controlgroup组件的组合选项
		 */
		checkboxradiogroup: function(options)
		{
			options = (options || {});
			//默认隐藏图标
			if(options.icon == null)
				options.icon = false;
			
			var $this = $(this);
			
			//隐藏时初始化的controlgroup在显示时无法圆角，这里可以解决此问题
			$("label:first", $this).addClass("ui-corner-left");
			$("label:last", $this).addClass("ui-corner-right");
			
			$("input", $this).checkboxradio(options);
			var widget = $this.controlgroup(options);
			
			//隐藏时初始化的checkboxradio，带有checked="checked"的元素，在显示后取消选择样式不对，这里可以解决此问题
			$(".ui-checkboxradio-icon.ui-state-hover").removeClass("ui-state-hover");
			
			return widget;
		},
		
		/**
		 * controlgroup组件支持类。
		 * 
		 * @param options 可选，controlgroup组件的选项
		 */
		controlgroupwrapper: function(options)
		{
			options = (options || {});
			
			var $this = $(this);
			var widget = $this.controlgroup(options);
			
			//隐藏时初始化的controlgroup在显示时无法圆角，这里可以解决此问题
			$(":first", $this).addClass("ui-corner-left");
			$(":last", $this).addClass("ui-corner-right");
			
			return widget;
		},
		
		/**
		 * 执行ajax提交表单。
		 * 
		 * @param options 选项，格式为：
		 * 			{
						//可选，是否以JSON数据格式提交，默认为：false
						jsonSubmit: false,
						//可选，提交URL，默认为：表单的action属性值
						url: "...",
						//可选，忽略的表单输入项名称，格式为：字符串、字符串数组
						ignore: ...,
						//可选，表单JSON数据处理回调函数，返回false将阻止表单提交
						handleData: function(data, form){ ... 或者 return ... },
						//其他$.ajax选项
						...
		 * 			}
		 */
		ajaxSubmit: function(options)
		{
			options = (options || {});
			
			var form = $(this);
			var data = null;
			
			if(options.jsonSubmit)
				data = $.formToJson(this, options.ignore);
			else
			{
				data = form.serializeArray();
				
				if(options.ignore)
				{
					var dataFiltered = [];
					var ignore = ($.isArray(options.ignore) ? options.ignore : [ options.ignore ]);
					
					$(data).each(function()
					{
						if($.inArray(this.name, ignore) < 0)
							dataFiltered.push(this);
					});
					
					data = dataFiltered;
				}
			}
			
			options = $.extend(
			{
				type: (form.attr("method") || "POST"),
				data: data,
			},
			options);
			
			if(options.handleData)
			{
				var newData = options.handleData(options.data, this);
				
				if(newData === false)
					return;
				
				if(newData !== undefined)
					options.data = newData;
			}
			
			if(!options.url)
				options.url = form.attr("action");
			
			if(options.jsonSubmit)
				$.ajaxJson(options);
			else
				$.ajax(options);
		},
		
		/**
		 * 以JSON数据格式执行ajax提交表单。
		 */
		ajaxSubmitJson: function(options)
		{
			options = $.extend({ jsonSubmit: true }, options);
			$(this).ajaxSubmit(options);
		},
		
		/**
		 * 上传文件<div>组件，<div>中应包含<div class="fileinput-button"><input type="file"></div>，可选包含".upload-progess"显示上传进度。
		 * 
		 * @param url 上传URL
		 * @param options 选项
		 *				{
		 *				  //上传文件参数名
		 *				  paramName: "file",
		 *				  //添加文件回调函数
		 *				  add: function(e, data){},
		 *				  //上传完成回调函数
		 *				  success: function(response, textStatus, jqXHR){},
		 *				  //上传按钮元素
		 *				  buttonSelector: ".fileinput-button",
		 *				  //进度条元素
		 *				  progessSelector: ".upload-file-info"
		 *				}
		 */
		fileUpload: function(url, options)
		{
			options = $.extend(
			{
				paramName: "file",
				add: function(e, data){},
				success: function(response, textStatus, jqXHR){},
				buttonSelector: ".fileinput-button",
				progessSelector: ".upload-file-info"
			},
			options);
			
			var btn = $(options.buttonSelector, this);
			var progessEle = $(options.progessSelector, this);
			
			btn.fileupload(
			{
				url : url,
				paramName : options.paramName,
				success : function(response, textStatus, jqXHR)
				{
					options.success(response, textStatus, jqXHR);
					$.fileuploadsuccessHandlerForUploadInfo(progessEle, false);
				}
			})
			.bind('fileuploadadd', function (e, data)
			{
				options.add(e, data);
				$.fileuploadaddHandlerForUploadInfo(e, data, progessEle);
			})
			.bind('fileuploadprogressall', function (e, data)
			{
				$.fileuploadprogressallHandlerForUploadInfo(e, data, progessEle);
			});
		},
		
		/**
		 * 点击元素自动关闭其内部的".auto-close-panel"面板。
		 * 元素内的其他子元素可以添加"auto-close-prevent"属性，声明点击它时阻止关闭的面板id、样式类名。
		 *
		 * @param panelSelector 面板选择器，默认为：".auto-close-panel"
		 */
		autoCloseSubPanel: function(panelSelector)
		{
			panelSelector = (panelSelector ? panelSelector : ".auto-close-panel");
			
			$(this).on("click", function(event)
			{
				var $thisEle = $(this);
				var $target = $(event.target);
				var $targetClosest = null;
				
				$(panelSelector, this).each(function()
				{
					var panel = $(this);
					
					if(!panel.is(":hidden"))
					{
						if($targetClosest == null)
							$targetClosest = $target.closest(".auto-close-panel, [auto-close-prevent]", $thisEle);
						
						if($targetClosest.is(panel[0]))
							return;
						
						var prevent = $targetClosest.attr("auto-close-prevent");
						if(prevent)
						{
							var prevents = prevent.split(" ");
							var panelId = panel.attr("id");
							
							for(var pri=0; pri<prevents.length; pri++)
							{
								if(prevents[pri] == panelId || panel.hasClass(prevents[pri]))
									return;
							}
						}
						
						panel.hide();
						
						var autoCloseCallback = panel.data("auto-close-callback");
						if(autoCloseCallback)
							autoCloseCallback();
					}
				});
			});
		}
	});
})
(jQuery);

/**
 * 分页组件。
 * 依赖:
 * jquery.js
 * jquery-ui.js
 * jquery.cookie.js
 */
(function($, undefined)
{
	$.widget("datagear.pagination",
	{
		options:
		{
			//可选，页码
			page: 1,
			
			//可选，页大小
			pageSize: 10,
			
			//可选，总记录数
			total: 0,
			
			//可选，页大小选项
			pageSizeOptions: [[5, 10], [20, 50], [100, 200], [500, 1000]],
			
			//可选，页大小cookie名
			pageSizeCookie: "pagination.pageSize",

			//可选，页大小cookie路径
			pageSizeCookiePath: "/",
			
			pageSizeSetLabel: "确定",
			
			//可选，更新回调函数
			//@param page 待更新的页码
			//@param pageSize 待更新的页大小
			//@param total 待更新的总记录数
			//@return false，不更新组件；true，更新组件
			update: function(page, pageSize, total){},
			
			//可选，标签模版
			labelTemplate: "共#{total}条，每页#{pageSize}条，#{page}/#{pages}",
			
			//可选，跳转页按钮标签
			toPageLabel: "跳转"
		},
		
		_create: function()
		{
			this.element.addClass("pagination");
			
			if(this.options.pageSizeCookie)
				this.options.pageSize = ($.cookie(this.options.pageSizeCookie) || this.options.pageSize);
			
			var thisWidget = this;
			
			var labelTemplate = "<span class='label-content'>" + this.options.labelTemplate
				.replace(/#\{total\}/g, "<span class='label-rt'>-</span>")
				.replace(/#\{pageSize\}/g, "<span class='label-ps'>-</span>")
				.replace(/#\{page\}/g, "<span class='label-cp'>-</span>")
				.replace(/#\{pages\}/g, "<span class='label-tp'>-</span>")
				+ "</span>";
			
			var label = $("<div class='label' title='' />").html(labelTemplate).appendTo(this.element);
			var pss = $("<div class='ui-widget ui-widget-content ui-corner-all page-size-set' style='position: absolute; top: 0px; left: 0px;' />").appendTo(label);
			
			this._createPageSizeSetConent(pss, this.options.pageSizeOptions);
			
			pss.hover(function(){},function(){ $(this).hide(); }).hide();
			
			$(".label-ps", label).click(function()
			{
				var pss = $(".page-size-set", thisWidget.element);
				
				if(pss.is(":hidden"))
				{
					$("td", pss).each(function()
					{
						var _this = $(this);
						var text = _this.text();
						
						if(thisWidget.options.pageSize+"" == text)
							_this.addClass("ui-state-active");
						else
							_this.removeClass("ui-state-active");
					});
					pss.show().position({my:"center bottom", at:"center top", of:$(this)});
				}
				else
					pss.hide();
			});
			
			var firstbtn = $("<div class='ui-state-default ui-corner-all page page-icon page-first' />").html("<span class='ui-icon ui-icon-seek-first'></span>").appendTo(this.element).button();
			var prevbtn = $("<div class='ui-state-default ui-corner-all page page-icon page-prev' />").html("<span class='ui-icon ui-icon-seek-prev'></span>").appendTo(this.element).button();
			var nextbtn = $("<div class='ui-state-default ui-corner-all page page-icon page-next' />").html("<span class='ui-icon ui-icon-seek-next'></span>").appendTo(this.element).button();
			var endbtn = $("<div class='ui-state-default ui-corner-all page page-icon page-end' />").html("<span class='ui-icon ui-icon-seek-end'></span>").appendTo(this.element).button();
			
			firstbtn.click(function()
			{
				thisWidget._updateCallback(1);
			})
			.hover(function(){ $(this).addClass("ui-state-hover"); }, function(){ $(this).removeClass("ui-state-hover"); });
			
			prevbtn.click(function()
			{
				thisWidget._updateCallback(thisWidget.options.page - 1);
			})
			.hover(function(){ $(this).addClass("ui-state-hover"); }, function(){ $(this).removeClass("ui-state-hover"); });
			
			nextbtn.button().click(function()
			{
				thisWidget._updateCallback(thisWidget.options.page + 1);
			})
			.hover(function(){ $(this).addClass("ui-state-hover"); }, function(){ $(this).removeClass("ui-state-hover"); });
			
			endbtn.button().click(function()
			{
				var pages =  ( thisWidget.options.pageSize < 1 ? 0 : Math.ceil(thisWidget.options.total/thisWidget.options.pageSize));
				
				thisWidget._updateCallback(pages);
			})
			.hover(function(){ $(this).addClass("ui-state-hover"); }, function(){ $(this).removeClass("ui-state-hover"); });
			
			var topagediv = $("<div class='to-page' />").appendTo(this.element);
			var topageform = $("<form class='ui-widget ui-widget-content ui-corner-all' />").appendTo(topagediv);
			$("<input type='text' name='topage' class='ui-widget ui-widget-content' />").appendTo(topageform);
			$("<input type='submit' />").val(this.options.toPageLabel).appendTo(topageform).button();
			
			topageform.submit(function()
			{
				var page = $("input[name='topage']", this).val();
				
				thisWidget._updateCallback(page);
				
				return false;
			});
			
			this._render();
		},
		
		/**
		 * 创建页大小设置内容。
		 */
		_createPageSizeSetConent: function($parent, pageSizeOptions)
		{
			var thisWidget = this;
			
			var maxCols = 1;
			
			for(var i=0; i<pageSizeOptions.length; i++)
			{
				var ele = pageSizeOptions[i];
				if(ele.length && maxCols < ele.length)
					maxCols = ele.length;
			}
			
			var table = $("<table />").appendTo($parent);
			
			for(var i=0; i<pageSizeOptions.length; i++)
			{
				var tr = $("<tr />").appendTo(table);
				
				var ele = pageSizeOptions[i];
				
				for(var j=0; j<maxCols; j++)
				{
					if(ele.length)
					{
						if(j < ele.length)
						{
							$("<td />").html(ele[j]).appendTo(tr)
							.click(function(){ thisWidget._updateCallback(null, $(this).text()); $parent.hide(); })
							.hover(function(){ $(this).addClass("ui-state-hover"); }, function(){ $(this).removeClass("ui-state-hover"); });
						}
						else
							$("<td />").appendTo(tr);
					}
					else
					{
						if(j == 0)
						{
							$("<td />").html(ele).appendTo(tr)
							.click(function(){ thisWidget._updateCallback(null, $(this).text()); $parent.hide(); })
							.hover(function(){ $(this).addClass("ui-state-hover"); }, function(){ $(this).removeClass("ui-state-hover"); })
						}
						else
							$("<td />").appendTo(tr);
					}
				}
			}
			
			var customdiv = $("<div class='page-size-custom' />").appendTo($parent);
			var customform = $("<form class='ui-widget ui-widget-content ui-corner-all' />").appendTo(customdiv);
			$("<input type='text' name='pageSize' class='ui-widget ui-widget-content' />").appendTo(customform);
			$("<input type='submit' />").val(this.options.pageSizeSetLabel).appendTo(customform).button();
			customform.submit(function()
			{
				var pageSize = $("input[name='pageSize']", this).val();
				
				thisWidget._updateCallback(null, pageSize);
				$parent.hide();
				
				return false;
			});
		},
		
		_destroy: function()
		{
			
		},
		
		_setOption: function(key, value)
		{
			if ( key === "page" )
				value = this._formatPage(value);
			else if(key == 'pageSize')
			{
				if(this.options.pageSizeCookie)
					$.cookie(this.options.pageSizeCookie, value, { expires : 365*5, path: this.options.pageSizeCookiePath });
			}
			
			this._super(key, value);
		},
		
		_setPageOption: function(page)
		{
			this._setOption("page", page);
		},
		
		_setPageSizeOption: function(pageSize)
		{
			this._setOption("pageSize", pageSize);
		},
		
		/**
		 * 刷新。
		 */
		refresh : function()
		{
			this._render();
		},
		
		/**
		 * 更新处理函数。
		 * @param page
		 * @param pageSize
		 * @param total
		 */
		_updateCallback : function(page, pageSize, total)
		{
			if(!page)
				page = this.options.page;
			if(!pageSize)
				pageSize = this.options.pageSize;
			if(!total)
				total = this.options.total;
			
			page = this._formatPage(page);
			
			var update = this.options.update.call(this, page, pageSize, total);
			
			if(update != false)
			{
				this._setPageOption(page);
				this._setPageSizeOption(pageSize);
				this._setOption("total", total);
				
				this._render();
			}
		},
	
		/**
		 * 绘制。
		 */
		_render : function()
		{
			var thisWidget = this;
			
			var page = this.options.page;
			var total = this.options.total;
			var pageSize = this.options.pageSize;
			
			var pages =  ( pageSize < 1 ? 0 : Math.ceil(total/pageSize));
			
			var label = $(".label", this.element);
			$(".label-rt", label).text(total);
			$(".label-ps", label).text(pageSize);
			$(".label-cp", label).text(page);
			$(".label-tp", label).text(pages);
			
			var firstbtn = $(".page-first", this.element);
			var predbtn = $(".page-prev", this.element);
			var nextbtn = $(".page-next", this.element);
			var endbtn = $(".page-end", this.element);
			
			if(page > 1)
			{
				firstbtn.button( "enable" );
				predbtn.button( "enable" );
			}
			else
			{
				firstbtn.button( "disable" );
				predbtn.button( "disable" );
			}
			
			if(page < pages)
			{
				nextbtn.button( "enable" );
				endbtn.button( "enable" );
			}
			else
			{
				nextbtn.button( "disable" );
				endbtn.button( "disable" );
			}
		},
		
		/**
		 * 格式化页码。
		 */
		_formatPage : function(page)
		{
			var total = this.options.total;
			var pageSize = this.options.pageSize;
			
			page = (typeof(page) == "string" ? parseInt(page) : page);
			var pages =  ( pageSize < 1 ? 0 : Math.ceil(total/pageSize));
			
			if(page < 1 || isNaN(page))
				page = 1;
			
			if(page > pages)
				page = pages;
			
			return page;
		}
	});
	
	//列表调色板
	$.widget("datagear.listpalllet",
	{
		options:
		{
			//颜色指示器元素
			indicator: undefined,
			//可选，组件父元素，默认为：document.body
			container: undefined,
			//可选，点击后自动隐藏此组件的上下文元素，默认为：document.body
			autoCloseContext: undefined,
			//可选，定位
			position: "absolute",
			//设置颜色后自动焦点至元素
			autoFocus: true
		},
		
		_create: function()
		{
			this._widgetId = $.uid();
			this._refElementClass = "ref"+this._widgetId;
			
			this.element.addClass(this._refElementClass);
			if(this.options.indicator)
				$(this.options.indicator).addClass(this._refElementClass);
			
			var thisWidgetObj = this;
			this._showEventHandler = function()
			{
				thisWidgetObj.show();
			};
			this._hideEventHandler = function(event)
			{
				var $target = $(event.target);
				
				if($target.closest("." + thisWidgetObj._refElementClass).length == 0)
					thisWidgetObj.hide();
			};
			
			this._getTriggerEle().on("click", this._showEventHandler);
			this._getAutoCloseContext().on("click", this._hideEventHandler);
		},
		
		show: function()
		{
			var widgetEle = this._getWidgetEle();
			if(widgetEle.length == 0)
				widgetEle = this._render();
			
			var initColor = this.element.val();
			if(initColor)
			{
				$(".color-cell.ui-state-active", widgetEle).removeClass("ui-state-active");
				$(".color-cell", widgetEle).each(function()
				{
					if($(this).attr("color-value") == initColor)
					{
						$(this).addClass("ui-state-active");
						return false;
					}
				});
			}
			
			widgetEle.show().position({ my : "left top", at : "left bottom+2", of : this._getTriggerEle()});
		},
		
		hide: function()
		{
			this._getWidgetEle().hide();
		},
		
		_render: function()
		{
			var base = [ "00", "33", "66", "99", "CC", "FF" ];
			
			var container = (this.options.container ? $(this.options.container) : document.body);
			var $widget = $("<div class='listpalllet ui-widget ui-widget-content ui-corner-all ui-widget-shadow ui-front' />")
								.attr("id", this._widgetId).addClass(this._refElementClass)
								.css("position", this.options.position).css("display", "none").appendTo(container);
			var $row = null;
			var count = 0;
			for(var i=0; i<base.length; i++)
			{
				for(var j=0; j<base.length; j++)
				{
					for(var k=0; k<base.length; k++)
					{
						var color = "#" + base[i] + base[j] + base[k];
						
						if(count%(base.length*3) == 0)
							$row = $("<div class='color-row' />").appendTo($widget);
						
						$("<div class='color-cell ui-widget ui-widget-content' />")
							.attr("color-value", color).css("background-color",color)
							.attr("title", color).appendTo($row);
						
						count++;
					}
				}
			}
			
			var thisWidgetObj = this;
			
			$widget.on("click", ".color-cell", function()
			{
				var colorValue = $(this).attr("color-value");
				
				thisWidgetObj.element.val(colorValue);
				if(thisWidgetObj.options.indicator)
					$(thisWidgetObj.options.indicator).css("background-color", colorValue);
				
				thisWidgetObj.hide();
				
				if(thisWidgetObj.options.autoFocus)
					thisWidgetObj.element.focus();
			});
			
			$widget.on("mouseover", ".color-cell", function()
			{
				$(this).addClass("ui-state-hover");
			})
			.on("mouseout", ".color-cell", function()
			{
				$(this).removeClass("ui-state-hover");
			});
			
			return $widget;
		},
		
		_getAutoCloseContext: function()
		{
			return $(this.options.autoCloseContext || document.body);
		},
		
		_getTriggerEle: function()
		{
			if(this.options.indicator)
				return $(this.options.indicator);
			else
				return this.element;
		},
		
		_getWidgetEle: function()
		{
			return $("#"+this._widgetId, this.options.container);
		},
		
		_destroy: function()
		{
			this.element.removeClass(this._refElementClass);
			if(this.options.indicator)
				$(this.options.indicator).removeClass(this._refElementClass);
			
			this._getTriggerEle().off("click", this._showEventHandler);
			this._getAutoCloseContext().off("click", this._hideEventHandler);
			
			this._getWidgetEle().remove();
		}
	});
})
(jQuery);

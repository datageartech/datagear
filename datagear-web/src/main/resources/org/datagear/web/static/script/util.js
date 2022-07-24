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
 */

(function($, undefined)
{
	$.toChartPluginHtml = function(chartPlugin, contextPath, vertical)
	{
		vertical = (vertical == null ? false : vertical);
		
		var html = "<div class='plugin-info flex align-items-center justify-content-center"
					+(vertical ? " flex-column block " : " flex-row inline ")
					+(!chartPlugin || !chartPlugin.iconUrl ? " no-icon " : "")
					+"'>";
		
		if(chartPlugin)
		{
			if(chartPlugin.iconUrl)
				html += "<div class='plugin-icon' style='background-image:url("+contextPath+$.escapeHtml(chartPlugin.iconUrl)+")'></div>";
			
			var name = (chartPlugin.nameLabel ? (chartPlugin.nameLabel.value || chartPlugin.id) : chartPlugin.id);
			name = $.escapeHtml(name);
			
			html += "<div class='plugin-name'>"+name+"</div>";
		}
		
		html += "</div>"
		
		return html;
	};
	
	/**
	 * 打开给定URL页面。
	 * 
	 * @param url 请求的URL。
	 * @param options 选项，格式如下：
	 * 			{
	 * 				//可选，打开目标：DOM 页面内；"_blank" 新网页；"_file" 文件下载
	 * 				target : document.body,
	 *              //当target是页内元素时，是否打开为对话框，默认为：true
	 *              dialog: true,
	 *              //当dialog=true时，是否作为模态框
	 * 				modal: true,
	 *              //当dialog=true时，对话框标题
	 * 				title: undefined,
	 *              //当dialog=true时，对话框宽度
	 * 				width: "60vw",
	 *				//可选，传递给新页面的参数，可以在目标页面通过$.pageParam(dom)获取
	 * 				pageParam : undefined,
	 * 				//其他$.ajax参数
	 * 				...
	 * 			}
	 */
	$.open = function(url, options)
	{
		options = $.extend(
		{
			target : document.body,
			dialog: true,
			modal : true,
			title : undefined,
			width: "60vw",
			pageParam : undefined
		},
		options);
		
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
			$.postOnForm(url, {"data" : options.data, "target" : "_blank"});
		}
		else
		{
			var successCallback = [];
			successCallback[0] = function(response)
			{
				const container = $(options.target ? options.target : document.body);
				
				const rootEleId = $.uid("app");
				const rootEle = $("<div id='"+rootEleId+"' />").appendTo(container);
				
				if(options.dialog)
				{
					const dialogEleId = rootEleId+"dialog";
					rootEle.addClass("vue-app-dialog");
					$("<p-dialog />").attr("id", dialogEleId).attr("app-ele-id", rootEleId)
								.attr(":header", "model.header").attr("v-model:visible", "model.visible").attr(":modal", options.modal)
								.attr("v-on:show", "setReponseHtml").attr("v-on:after-hide", "destroyDialogEle")
								.attr(":style", "{width: model.width}")
								.attr("class", "ajax-dialog " + $.PAGE_PARAM_BINDER_CLASS)
								.appendTo(rootEle);
					
					var dialogApp =
					{
						setup()
						{
							const model = Vue.reactive(
							{
								header: (options.title || " "),
								visible: true,
								width: options.width
							});
							
							const destroyDialogEle = function()
							{
								rootEle.remove();
							};
							const setReponseHtml = function()
							{
								let dialogEle = $("#"+dialogEleId);
								
								if(options.pageParam)
									$.pageParam(dialogEle, options.pageParam);
								
								let dialogContent = $(" > .p-dialog-content", dialogEle);
								dialogContent.html(response);
								
								if(model.header == " ")
								{
									let title = $("title", dialogContent).text();
									if(title)
										model.header = title;
								}
							};
							
							return {model, destroyDialogEle, setReponseHtml};
						},
						components: { "p-dialog": primevue.dialog }
					};
					
					dialogApp = Vue.createApp(dialogApp).use(primevue.config.default).mount(rootEle[0]);
					rootEle.data("dialogApp", dialogApp);
				}
				else
				{
					rootEle.addClass($.PAGE_PARAM_BINDER_CLASS);
					
					if(options.pageParam)
						$.pageParam(dialogEle, options.pageParam);
					
					rootEle.html(response);
				}
			};
			
			if(options.success)
				successCallback = successCallback.concat(options.success);
			
			options = $.extend(options,
			{
				success : successCallback,
				type : "POST"
			});
			
			$.ajax(url, options);
		}
	};
	
	/**
	 * 判断给定dom元素是否在对话框中或者将要在对话框中显示。
	 * 
	 * @param dom 任意DOM元素
	 */
	$.isInDialog = function(dom)
	{
		var d = $.getInDialog(dom);
		return (d && d.length > 0);
	};
	
	/**
	 * 获取元素所处的对话框DOM对象，如果不在对话框中，返回一个空的Jquery对象（长度为0）。
	 */
	$.getInDialog = function(dom)
	{
		return $(dom).closest(".p-dialog");
	};
	
	/**
	 * 关闭并销毁对话框。
	 */
	$.closeDialog = function(ele)
	{
		var d = $.getInDialog(ele);
		if(d && d.length > 0)
		{
			let appEle = $("#"+ d.attr("app-ele-id"));
			let dialogApp = appEle.data("dialogApp");
			
			if(dialogApp)
			{
				dialogApp.model.visible = false;
			}
		}
	};
	
	/*用于支持$.pageParam函数的元素CSS类名*/
	$.PAGE_PARAM_BINDER_CLASS = "page-param-binder";
	
	/**
	 * 获取/设置页面参数，设置页面参数，使页面在加载完成后可以在内部获取此参数。
	 * 
	 * @param ele 必选，任意元素
	 * @param param 可选，要设置的参数
	 */
	$.pageParam = function(ele, param)
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
	};
	
	/**
	 * 调用页面参数函数。
	 * 如果没有页面参数或者指定的函数，返回undefined。
	 * 
	 * @param ele 任意元素
	 * @param functionName 可选，如果页面参数是对象，则指定页面对象的函数名
	 * @param argArray 可选，函数参数数组
	 */
	$.pageParamCall = function(ele, functionName, argArray)
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
	};
	
	/**
	 * 提示成功。
	 */
	$.tipSuccess = function(msg)
	{
		var tip = $.getGlobalTip();
		
		if(tip)
			tip.showSuccess(typeof(msg) == "string" ? { summary: msg } : msg);
	},
	
	/**
	 * 提示信息。
	 */
	$.tipInfo = function(msg)
	{
		var tip = $.getGlobalTip();
		
		if(tip)
			tip.showInfo(typeof(msg) == "string" ? { summary: msg } : msg);
	};
	
	/**
	 * 提示警告。
	 */
	$.tipWarn = function(msg)
	{
		var tip = $.getGlobalTip();
		
		if(tip)
			tip.showWarn(typeof(msg) == "string" ? { summary: msg } : msg);
	};
	
	/**
	 * 提示错误。
	 */
	$.tipError = function(msg)
	{
		var tip = $.getGlobalTip();
		if(tip)
			tip.showError(typeof(msg) == "string" ? { summary: msg } : msg);
	};
	
	/**
	 * 获取提示组件。
	 */
	$.getGlobalTip = function()
	{
		var appEle = ($.GLOBAL_TIP_APP_ELE_ID ? $("#"+$.GLOBAL_TIP_APP_ELE_ID) : null);
		
		if(!appEle || appEle.length == 0)
			return null;
		
		return appEle.data("tipApp");
	};
	
	/**
	 * 初始化提示。
	 */
	$.initGlobalTip = function()
	{
		var appId = ($.GLOBAL_TIP_APP_ELE_ID || ($.GLOBAL_TIP_APP_ELE_ID = $.uid("app")));
		var appEle = $("#"+appId);
		if(appEle.length == 0)
		{
			appEle = $("<div id='"+appId+"' />").addClass("vue-app-tip").appendTo(document.body);
			$("<p-toast />").attr("position", "top-center").attr("group", "global-tip").appendTo(appEle);
			
			const buildTipOptions = function(builtInOptions, options)
			{
				options = $.extend(builtInOptions, options);
				
				if(!options.detail)
					options.contentStyleClass = "align-items-center empty-detail";
				
				return options;
			};
			
			var tipApp =
			{
				setup()
				{
					const toast = primevue.usetoast.useToast();
					
					const showSuccess = (options) =>
					{
						options = buildTipOptions({ severity: "success", group: "global-tip", life: 2500 }, options);
						toast.add(options);
					};
					const showInfo = (options) =>
					{
						options = buildTipOptions({ severity: "info", group: "global-tip", life: 3000 }, options);
						toast.add(options);
					};
					const showWarn = (options) =>
					{
						options = buildTipOptions({ severity: "warn", group: "global-tip", life: 3000 }, options);
						toast.add(options);
					};
					const showError = (options) =>
					{
						options = buildTipOptions({ severity: "error", group: "global-tip", life: 5000 }, options);
						toast.add(options);
					};
					
					return { showSuccess, showInfo, showWarn, showError };
				},
				components: { "p-toast": primevue.toast }
			};
			
			tipApp = Vue.createApp(tipApp).use(primevue.config.default).use(primevue.toastservice).mount(appEle[0]);
			appEle.data("tipApp", tipApp);
		}
	};
	
	/**
	 * 操作确认。
	 * @param options { header: "", message: "", accept: function(){}, acceptLabel: "", rejectLabel: "" }
	 */
	$.confirm = function(options)
	{
		var confirm = $.getGlobalConfirm();
		if(confirm)
			confirm.showConfirm(options);
	};
	
	/**
	 * 获取确认框组件。
	 */
	$.getGlobalConfirm = function()
	{
		var appEle = ($.GLOBAL_CONFIRM_APP_ELE_ID ? $("#"+$.GLOBAL_CONFIRM_APP_ELE_ID) : null);
		
		if(!appEle || appEle.length == 0)
			return null;
		
		return appEle.data("confirmApp");
	};
	
	/**
	 * 初始化确认框。
	 */
	$.initGlobalConfirm = function()
	{
		var appId = ($.GLOBAL_CONFIRM_APP_ELE_ID || ($.GLOBAL_CONFIRM_APP_ELE_ID = $.uid("app")));
		var appEle = $("#"+appId);
		if(appEle.length == 0)
		{
			appEle = $("<div id='"+appId+"' />").addClass("vue-app-confirm").appendTo(document.body);
			$("<p-confirmdialog />").appendTo(appEle);
			
			const buildConfirmOptions = function(options)
			{
				return $.extend({ position: "center", icon: "pi pi-info-circle", acceptClass: "p-button-danger" }, options);
			};
			
			var confirmApp =
			{
				setup()
				{
					const confirm = primevue.useconfirm.useConfirm();
					
					const showConfirm = (options) =>
					{
						options = buildConfirmOptions(options);
						confirm.require(options);
					};
					
					return { showConfirm };
				},
				components:
				{
					"p-confirmdialog": primevue.confirmdialog,
					"p-button": primevue.button
				}
			};
			
			confirmApp = Vue.createApp(confirmApp).use(primevue.config.default).use(primevue.confirmationservice).mount(appEle[0]);
			appEle.data("confirmApp", confirmApp);
		}
	};
	
	$.TYPEOF_STRING = "string";
	$.TYPEOF_NUMBER = "number";
	$.TYPEOF_BOOLEAN = "boolean";
	$.TYPEOF_TYPE_OBJECT = "object";
	
	$.isTypeString = function(obj)
	{
		return (typeof(obj) == $.TYPEOF_STRING);
	};
	
	$.isTypeNumber = function(obj)
	{
		return (typeof(obj) == $.TYPEOF_NUMBER);
	};
	
	$.isTypeObject = function(obj)
	{
		return (typeof(obj) == $.TYPEOF_TYPE_OBJECT);
	};
	
	$.inArrayById = function(array, idValue, idPropName)
	{
		idPropName = (idPropName == null ? "id" : idPropName);
		
		for(var i=0; i<array.length; i++)
		{
			if(array[i] && array[i][idPropName] == idValue)
				return i;
		}
		
		return -1;
	};
	
	$.removeById = function(array, idValue, idPropName)
	{
		var idx = $.inArrayById(array, idValue, idPropName);
		if(idx >= 0)
			array.splice(idx, 1);
	};
	
	$.addById = function(array, eleOrEles, idPropName)
	{
		var eles = ($.isArray(eleOrEles) ? eleOrEles : [ eleOrEles ]);
		idPropName = (idPropName == null ? "id" : idPropName);
		
		for(var i=0; i<eles.length; i++)
		{
			var ele = eles[i];
			
			var idx = $.inArrayById(array, ele[idPropName], idPropName);
			
			if(idx < 0)
				array.push(ele);
		}
	};
	
	/**
	 * 包装成数组。
	 */
	$.wrapAsArray = function(obj)
	{
		if(obj == null)
			return [];
		
		return ($.isArray(obj) ? obj : [ obj ]);
	};
	
	/**
	 * 生成一个唯一ID
	 * 
	 * @param prefix 可选，前缀
	 * @returns
	 */
	$.uid = function(prefix)
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
	};
	
	/**
	 * 给URL添加参数。
	 * 
	 * @param url 待添加参数的URL
	 * @param name 待添加的参数名
	 * @param value 待添加的参数值
	 * @param multiple 允许重名，可选，默认为false
	 */
	$.addParam = function(url, name, value, multiple)
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
	};
	
	/**
	 * 转义HTML关键字。
	 * 
	 * @param text 要转义的文本
	 */
	$.escapeHtml = function(text)
	{
		if(text == null || !$.isTypeString(text))
			return text;
		
		var epn = "";
		
		for(var i=0; i<text.length; i++)
		{
			var c = text.charAt(i);
			
			switch(c)
			{
				case '<':
				{
					epn += "&lt;";
					break;
				}
				case '>':
				{
					epn += "&gt;";
					break;
				}
				case '"':
				{
					epn += "&quot;";
					break;
				}
				case '&':
				{
					epn += "&amp;";
					break;
				}
				default:
				{
					epn += c;
				}
			}
		}
		
		return epn;
	};
	
	//反转义JSON里的HTML关键字。
	$.unescapeHtmlForJson = function(json)
	{
		if(json == null)
			return null;
		
		var type = typeof(json);
		
		if(type == $.TYPEOF_STRING)
		{
			return $.unescapeHtml(json);
		}
		else if(type == $.TYPEOF_NUMBER || type == $.TYPEOF_BOOLEAN)
		{
			return json;
		}
		else if(type == $.TYPEOF_TYPE_OBJECT)
		{
			if($.isArray(json))
			{
				for(var i=0; i<json.length; i++)
					json[i] = $.unescapeHtmlForJson(json[i]);
			}
			else
			{
				for(var p in json)
					json[p] = $.unescapeHtmlForJson(json[p]);
			}
			
			return json;
		}
		else
			return json;
	};
	
	/**
	 * 反转义HTML关键字。
	 * 
	 * @param text 要转义的文本
	 */
	$.unescapeHtml = function(text)
	{
		if(text == null || !$.isTypeString(text))
			return text;
		
		var epn = "";
		
		for(var i=0; i<text.length; i++)
		{
			var c = text.charAt(i);
			
			switch(c)
			{
				case '&':
				{
					var token = $._unescapeHtmlToMaySemicolon(text, i+1);
					
					if(token == "lt;")
						epn += '<';
					else if(token == "gt;")
						epn += '>';
					else if(token == "quot;")
						epn += '"';
					else if(token == "amp;")
						epn += '&';
					else
						epn += '&' + token;
					
					i += token.length;
					
					break;
				}
				default:
				{
					epn += c;
				}
			}
		}
		
		return epn;
	};
	
	$._unescapeHtmlToMaySemicolon = function(text, startIdx)
	{
		var re = "";
		
		var endIdx = Math.min(text.length, startIdx + "&quot;".length);
		
		for(var i=startIdx; i<endIdx; i++)
		{
			var c = text.charAt(i);
			re += c;
			
			if(c == ';')
				break;
		}
		
		return re;
	};
	
	/**
	 * 获取对象/对象数组指定名称属性值。
	 * 
	 * @param obj 对象、对象数组
	 * @param name 属性名
	 */
	$.propertyValue = function(obj, name)
	{
		var isArray = $.isArray(obj);
		var array = (isArray? obj : [obj]);
		
		var re = [];
		for(var i=0; i<array.length; i++)
			re[i] = array[i][name];
		
		return (isArray? re : re[0]);
	};
	
	$.isHtmlFile = function(fileName)
	{
		var htmlReg = /\.(html|htm)$/gi;
		return (fileName && htmlReg.test(fileName));
	};
	
	$.isJsFile = function(fileName)
	{
		var jsReg = /\.(js)$/gi;
		return (fileName && jsReg.test(fileName));
	};
	
	$.isCssFile = function(fileName)
	{
		var cssReg = /\.(css)$/gi;
		return (fileName && cssReg.test(fileName));
	};
	
	$.isTextFile = function(fileName)
	{
		var reg = /\.(html|htm|css|js|json|xml|txt)$/gi;
		return reg.test(fileName);
	};
	
	$.toJsonString = function(obj)
	{
		return JSON.stringify(obj);
	};
	
	/**ajax内容类型常量*/
	$.CONTENT_TYPE_JSON = "application/json";
	$.CONTENT_TYPE_FORM = "application/x-www-form-urlencoded";
	
	/**
	 * 提交JSON数据。
	 */
	$.postJson = function(url, data, success)
	{
		$.ajaxJson(url, { data: data, success: success });
	};
	
	/**
	 * ajax提交JSON数据。
	 * 
	 * @param url 可选
	 * @param options 必选
	 */
	$.ajaxJson = function(url, options)
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
	};
	
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
			
			if(isSuccessResponse)
			{
				if(isTipSuccess)
					$.tipSuccess(message);
			}
			else
			{
				if(isTipError)
					$.tipError(message);
			}
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
})
(jQuery);

(function($, undefined)
{

//重写支持Vue响数据模型的验证方法
$.validator.addMethod("required", function(value)
{
	if(value == null)
		return false;
	
	var type = typeof(value);
	
	if(type == $.TYPEOF_STRING)
		return (value.length > 0);
	else if(type == $.TYPEOF_NUMBER)
		return true;
	else if($.isArray(value))
		return (value.length > 0);
	else
		return true;
});

$.fn.extend(
{
	/**
	 * 构建带有输入验证功能的表单。
	 * 
	 * @param reactiveFormModel 响应式表单数据模型，对于".validate-proxy"的验证，将从它读取实际的值进行验证
	 * @param options
	 *			{
	 *			  //忽略校验选择器
	 *			  ignore: "...",
	 *			  //自定义校验规则
	 *			  rules: { ... }
	 *			  //自定义提示消息
	 *			  messages
	 *			}
	 *			详细参考：https://jqueryvalidation.org/validate/
	 */
	validateForm: function(reactiveFormModel, options)
	{
		const thisForm = $(this);
		thisForm.data("reactiveFormModel", reactiveFormModel);
		
		options = $.extend(
		{
			ignore: ".ignore-validate",
			onkeyup: false,
			normalizer: function(value)
			{
				var thisEle = $(this);
				
				//代理formModel中的值
				if(thisEle.hasClass("validate-proxy"))
				{
					//代理属性名
					var name = thisEle.attr("name");
					var realValue = Vue.toRaw(reactiveFormModel[name]);
					return realValue;
				}
				else
					return value;
			},
			showErrors: function(errorMap, errorList)
			{
				const successList = (this.successList || []);
				$.each(successList, function(idx, ele)
				{
					const field = $(ele).closest(".field-input");
					$("small.p-error", field).hide();
					$(".input", field).removeClass("p-invalid");
				});
				
				$.each(errorList, function(idx, error)
				{
					const field = $(error.element).closest(".field-input");
					const input = $(".input", field);
					var msg = $(".validate-msg", field);
					if(msg.length == 0)
						msg = $("<div class='validate-msg' />").appendTo(field);
					var errorEle = $(".p-error", msg);
					if(errorEle.length == 0)
						errorEle = $("<small class='p-error' />").appendTo(msg);
					
					input.addClass("p-invalid");
					errorEle.html(error.message).show();
				});
			}
		},
		options);
		
		thisForm.validate(options);
	}
});

})
(jQuery);
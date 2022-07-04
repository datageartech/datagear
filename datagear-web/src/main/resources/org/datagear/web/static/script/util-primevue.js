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
				
				const rootId = $.uid("root");
				const rootEle = $("<div id='"+rootId+"' />").appendTo(container);
				
				if(options.dialog)
				{
					const dialogId = rootId+"dialog";
					rootEle.addClass("dialog-root");
					$("<p-dialog />").attr("id", dialogId)
								.attr(":header", "dialogModel.header").attr("v-model:visible", "dialogModel.visible").attr(":modal", options.modal)
								.attr("v-on:show", "setReponseHtml").attr("v-on:after-hide", "destroyDialogEle")
								.attr(":style", "{width: dialogModel.width}")
								.attr("class", "ajax-dialog " + $.PAGE_PARAM_BINDER_CLASS)
								.appendTo(rootEle);
					
					const dialogApp =
					{
						setup()
						{
							const dialogModel = Vue.reactive(
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
								let dialogEle = $("#"+dialogId);
								dialogEle.data("dialogModel", dialogModel);
								
								if(options.pageParam)
									$.pageParam(dialogEle, options.pageParam);
								
								let dialogContent = $(" > .p-dialog-content", dialogEle);
								dialogContent.html(response);
								
								if(dialogModel.header == " ")
								{
									let title = $("title", dialogContent).text();
									if(title)
										dialogModel.header = title;
								}
							};
							
							return {dialogModel, destroyDialogEle, setReponseHtml};
						},
						components: { "p-dialog": primevue.dialog }
					};
					
					Vue.createApp(dialogApp).use(primevue.config.default).mount(rootEle[0]);
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
			let dialogModel = d.data("dialogModel");
			dialogModel.visible = false;
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
	$.confirm = function(content, options)
	{
		options = (options || {});
		options = $.extend({confirmText : "Confirm", cancelText : "Cancel", title : "Confirm"}, options);
		
		if(options.confirm)
			options.confirm();
	};
	
	/**
	 * 提示成功。
	 */
	$.tipSuccess = function(content, delayMs)
	{
		alert(content);
	},
	
	/**
	 * 提示错误。
	 */
	$.tipError = function(content, delayMs)
	{
		alert(content);
	};
	
	/**
	 * 提示信息。
	 */
	$.tipInfo = function(content, delayMs)
	{
		alert(content);
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
	
	$.toJsonString = function(obj)
	{
		return JSON.stringify(obj);
	};
	
	/**JSON内容类型常量*/
	$.CONTENT_TYPE_JSON = "application/json";
	
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
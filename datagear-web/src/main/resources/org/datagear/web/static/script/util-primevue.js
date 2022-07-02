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
				
				const rooId = $.uid("root");
				const rootEle = $("<div id='"+rooId+"' />").appendTo(container);
				rootEle.addClass($.PAGE_PARAM_BINDER_CLASS);
				
				if(options.pageParam)
					$.pageParam(rootEle, options.pageParam);
				
				if(options.dialog)
				{
					const dialogId = rooId+"dialog";
					rootEle.addClass("dialog-root");
					const dialogEle = $("<p-dialog />").attr("id", dialogId)
										.attr(":header", "header").attr("v-model:visible", "visible").attr(":modal", options.modal)
										.attr("v-on:show", "setReponseHtml").attr("v-on:after-hide", "destroyDialogEle")
										.attr(":style", "{width: '50vw'}")
										.attr("class", "ajax-dialog")
										.appendTo(rootEle);
					
					const dialogApp =
					{
						setup()
						{
							const header = Vue.ref(options.title || " ");
							const visible = Vue.ref(true);
							const destroyDialogEle = function()
							{
								$("#"+rooId).remove();
							};
							const setReponseHtml = function()
							{
								let dialogContent = $("#"+dialogId+" > .p-dialog-content");
								dialogContent.html(response);
								
								if(header.value == " ")
								{
									let title = $("title", dialogContent).text();
									if(title)
										header.value = title;
								}
							};
							
							return { header, visible, setReponseHtml, destroyDialogEle };
						},
						components: { "p-dialog": primevue.dialog }
					};
					
					Vue.createApp(dialogApp).use(primevue.config.default).mount("#"+rooId);
				}
				else
				{
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
})
(jQuery);
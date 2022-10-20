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
	 * 				//可选，打开目标：DOM 页面内；"_blank"、"_self" 新网页
	 * 				target : document.body,
	 *              //当target是页内元素时，是否打开为对话框，默认为：true
	 *              dialog: true,
	 *              //当dialog=true时，是否作为模态框
	 * 				modal: true,
	 *              //当dialog=true时，对话框标题
	 * 				title: undefined,
	 *              //当dialog=true时，对话框宽度
	 * 				width: "60vw",
	 *              //当dialog=true时，对话框样式类
	 * 				styleClass: "",
	 *              //当dialog=true时，对话框位置
	 * 				position: "center",
	 *              //当dialog=true时，对话框位置
	 * 				onShow: function(dialogEle){},
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
			styleClass: "",
			position: "center",
			onShow: null,
			pageParam : undefined
		},
		options);
		
		if(options.target == "_blank" || options.target == "_self")
		{
			if(!options.data)
			{
				if(options.target == "_blank")
				{
					window.open(url);
				}
				else if(options.target == "_self")
				{
					window.location.href = url;
				}
			}
			else
			{
				//使用window.open()会使URL超长导致请求失败，因而改为postOnForm
				$.postOnForm(url, {"data" : options.data, "target" : options.target});
			}
		}
		else
		{
			var successCallback = [];
			successCallback[0] = function(response)
			{
				const container = $(options.target ? options.target : document.body);
				
				if(options.dialog)
				{
					const rootEleId = $.uid("app");
					const dialogEleId = rootEleId+"dialog";
					const rootEle = $("<div id='"+rootEleId+"' dialog-ele-id='"+dialogEleId+"' />").appendTo(container);
					
					rootEle.addClass("vue-app-dialog");
					$("<p-dialog />").attr("id", dialogEleId).attr("app-ele-id", rootEleId)
								.attr(":header", "model.header").attr("v-model:visible", "model.visible").attr(":modal", options.modal)
								.attr("v-on:show", "onDialogShow").attr("v-on:after-hide", "onDialogAfterHide")
								.attr("v-on:hide", "onDialogHide")
								.attr(":close-on-escape", "false")
								.attr(":style", "{width: model.width}")
								.attr("class", "ajax-dialog " + $.PAGE_PARAM_BINDER_CLASS + " " + options.styleClass)
								.attr("position", options.position)
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
							
							const onDialogShow = function()
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
								
								if(options.onShow)
									options.onShow(dialogEle);
							};
							const onDialogHide = function()
							{
								$._callBeforeDialogCloseCallbacks(rootEle);
							};
							const onDialogAfterHide = function()
							{
								$._destroyDialogApp(rootEle);
							};
							
							return {model, onDialogShow, onDialogHide, onDialogAfterHide};
						},
						components: { "p-dialog": primevue.dialog }
					};
					
					dialogApp = Vue.createApp(dialogApp);
					var dialogVm = dialogApp.use(primevue.config.default).mount(rootEle[0]);
					
					rootEle.data("dialogApp", dialogApp).data("dialogVm", dialogVm);
				}
				else
				{
					if(!container.hasClass($.PAGE_PARAM_BINDER_CLASS))
						container.addClass($.PAGE_PARAM_BINDER_CLASS);
					
					if(options.pageParam)
						$.pageParam(container, options.pageParam);
					
					container.html(response);
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
	$.postOnForm = function(url, options)
	{
		options = $.extend({ data : {}, target : ""}, options);
		
		var formId = ($.GLOBAL_POST_ON_FORM_ID || ($.GLOBAL_POST_ON_FORM_ID = $.uid("form")));
		var form = $("#"+formId);
		if(form.length == 0)
		{
			form = $("<form style='display:none;width:0px;height:0px;' />").attr("id", formId).attr("method", "POST")
						.appendTo(document.body);
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
	 * 绑定对话框关闭前回调函数。
	 *
	 * @param ele 对话框内的任意元素
	 * @param name 绑定的回调函数标识名（同名的仅会有一个）
	 * @param callback 回调函数
	 */
	$.bindBeforeCloseDialogCallback = function(ele, name, callback)
	{
		var dialogEle = $.getInDialog(ele);
		
		if(dialogEle && dialogEle.length > 0)
		{
			var callbacks = dialogEle.data("beforeCloseCallbacks");
			if(!callbacks)
			{
				callbacks = {};
				dialogEle.data("beforeCloseCallbacks", callbacks);
			}
			
			callbacks[name] = callback;
		}
	};
	
	/**
	 * 关闭并销毁由$.open()创建的对话框。
	 * 
	 * @param ele 对话框内的任意元素
	 */
	$.closeDialog = function(ele)
	{
		var dialogEle = $.getInDialog(ele);
		if(dialogEle && dialogEle.length > 0)
		{
			var appEle = $("#"+ dialogEle.attr("app-ele-id"));
			$._destroyDialogApp(appEle);
		}
	};
	
	$._callBeforeDialogCloseCallbacks = function(appEle)
	{
		var dialogEleId = appEle.attr("dialog-ele-id");
		
		if(dialogEleId)
		{
			var dialogEle = $("#"+ appEle.attr("dialog-ele-id"));
			var beforeCloseCallbacks = dialogEle.data("beforeCloseCallbacks");
			
			if(beforeCloseCallbacks)
			{
				$.each(beforeCloseCallbacks, function(name, callback)
				{
					callback();
				});
			}
		}
	};
	
	$._destroyDialogApp = function(appEle)
	{
		var dialogApp = appEle.data("dialogApp");
		var dialogVm = appEle.data("dialogVm");
		var dialogEleId = appEle.attr("dialog-ele-id");
		
		$._callBeforeDialogCloseCallbacks(appEle);
		
		if(dialogVm)
			dialogVm.model.visible = false;
		if(dialogApp)
			dialogApp.unmount();
		
		appEle.remove();
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
	
	$.parseIntWithDefault = function(val, dftVal)
	{
		val = parseInt(val);
		return (!isNaN(val) ? val : dftVal);
	};
	
	$.findNameByValue = function(array, value)
	{
		var idx = $.inArrayById(array, value, "value");
		return (idx >= 0 ? array[idx].name : null);
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
	
	$.moveUpById = function(array, idValue, idPropName)
	{
		idValue = [].concat(idValue);
		
		for(var i=0; i<array.length; i++)
		{
			var v = array[i];
			var inIdx = $.inArray(v[idPropName], idValue);
			
			if(inIdx > -1 && i > 0)
			{
				var prev = array[i - 1];
				array[i - 1] = array[i];
				array[i] = prev;
				
				idValue.splice(inIdx, 1);
			}
		}
	};
	
	$.moveDownById = function(array, idValue, idPropName)
	{
		idValue = [].concat(idValue);
		
		for(var i=array.length - 1; i>= 0; i--)
		{
			var v = array[i];
			var inIdx = $.inArray(v[idPropName], idValue);
			
			if(inIdx > -1 && i < (array.length - 1))
			{
				var next = array[i + 1];
				array[i + 1] = array[i];
				array[i] = next;
				
				idValue.splice(inIdx, 1);
			}
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
	 * 比较两个版本号。
	 * > 1：v0高于v1；= 0：v0等于v1；< 0：v0小于v1。
	 * 
	 * @param v0
	 * @param v1
	 */
	$.compareVersion = function(v0, v1)
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
	};
	
	/**
	 * 解析版本号字符串（格式为：1.0, 1.1.0, 1.1.0-build），返回包含各分段的数组。
	 */
	$.resolveVersion = function(version)
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
	};
	
	/**
	 * 如果是字符串且超过指定长度，则将其截断。
	 * 
	 * @param str 必选，待截断的字符串
	 * @param suffix 可选，截断后缀，默认为“...”
	 * @param length 可选，截断长度，默认为47
	 */
	$.truncateIf = function(str, suffix, length)
	{
		if(suffix == undefined)
			suffix = "...";
		
		if(length == undefined)
			length = 47;
		
		if(typeof(str) == "string" && str.length > length)
			str = str.substr(0, length) + suffix;
		
		return str;
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
	 * 将值/值数组转换为{ name: "...", value: 值 }对象/数组。
	 */
	$.toNameValueObj = function(valueOrValues, name)
	{
		var isArray = $.isArray(valueOrValues);
		valueOrValues = (isArray ? valueOrValues : [ valueOrValues ]);
		
		var re = [];
		
		$.each(valueOrValues, function(i, v)
		{
			re.push({ name: name, value: v });
		});
		
		return (isArray ? re : re[0]);
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
	
	//判断两个结构相同的对象是否相等
	$.equalsForSameType = function(a, b)
	{
		if(a == null)
			return (b == null);
		else if(b == null)
			return (a == null);
		
		var typea = typeof(a);
		var typeb = typeof(b);
		
		if(typea != typeb)
		{
			return false;
		}
		else if(typea == $.TYPEOF_TYPE_OBJECT)
		{
			if($.isArray(a))
			{
				if(a.length != b.length)
					return false;
				
				for(var i=0; i<a.length; i++)
				{
					if(!$.equalsForSameType(a[i], b[i]))
						return false;
				}
			}
			else
			{
				for(var p in a)
				{
					if(!$.equalsForSameType(a[p], b[p]))
						return false;
				}
			}
			
			return true;
		}
		else
		{
			return (a == b);
		}
	};
	
	/**
	 * 将字符串按照'/'或'\'路径分隔符拆分。
	 */
	$.splitAsPath = function(str, keepSeparator)
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
	};
	
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
	$.toPathTree = function(strs, options)
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
				var idx = $.inArrayById(parent, ni, options.nameProperty);
				
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
	};
	
	$.concatPathArray = function(paths, start, end)
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
	};
	
	//转换为合法文件名称。
	$.toValidFileName = function(rawName)
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
	};
	
	//常用按键，摘自jquery-ui
	$.keyCode =
	{
		BACKSPACE:8, COMMA:188, DELETE:46, DOWN:40, END:35,
		ENTER:13, ESCAPE:27, HOME:36, LEFT:37, PAGE_DOWN:34,
		PAGE_UP:33, PERIOD:190, RIGHT:39, SPACE:32, TAB:9, UP:38
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
	
	/**
	 * 获取对象或者对象数组的属性值参数字符串，例如：“id=1&id=2&id=3”
	 * 
	 * @param objOrArray
	 * @param propertyName
	 * @param paramName 可选，参数名
	 */
	$.propertyValueParam = function(objOrArray, propertyName, paramName)
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
	};
	
	$.isZipFile = function(fileName)
	{
		var reg = /\.(zip)$/gi;
		return (fileName && reg.test(fileName));
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
	
	$.isDirectoryFile = function(fileName)
	{
		return (fileName && fileName.charAt(fileName.length - 1) == '/');
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
					interval: 500,
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
	
	$.toChartPluginHtml = function(chartPlugin, contextPath, options)
	{
		options = $.extend(
		{
			//是否竖向排版
			vertical: false,
			//横向对齐方式："start"、"center"、"end"
			justifyContent: "center"
		},
		options);
		
		var html = "<div class='plugin-info flex align-items-center justify-content-"+options.justifyContent
					+(options.vertical ? " flex-column block " : " flex-row inline ")
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
	
	$.toSqlEditorColumnCompletions = function(tableName, columns)
	{
		var columnCompletions = [];
		
		$.each(columns, function(i, column)
		{
			var displayComment = tableName;
			if(column.typeName)
				displayComment = column.typeName + " " + displayComment;
			if(column.comment)
				displayComment = $.truncateIf(column.comment, "", 10) + " " + displayComment;
			
			columnCompletions[i] = { name: column.name, displayComment: displayComment };
		});
		
		return columnCompletions;
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
		
		var newOptions = $.extend(
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
				else if(thisEle.hasClass("validate-normalizer"))
				{
					var name = thisEle.attr("name");
					var realValue = options["customNormalizers"][name]();
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
					$(".input:first", field).removeClass("p-invalid");
				});
				
				$.each(errorList, function(idx, error)
				{
					const field = $(error.element).closest(".field-input");
					const input = $(".input:first", field);
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
		
		thisForm.validate(newOptions);
	}
});

})
(jQuery);

(function($, undefined)
{

//填充page_obj.ftl里JS对象的静态逻辑
$.inflatePageObj = function(po)
{
	//获取父页面JS对象
	po.parent = function()
	{
		var parentPage = (this.ppid ? window[this.ppid] : null);
		//父页面DOM元素可能会在回调过程中被删除，这里加一层元素判断
		return (!parentPage || parentPage.element().length == 0 ? null : parentPage);
	};
	
	//获取页面内的元素
	po.element = function(selector, parent)
	{
		return (selector == null ? $("#"+this.pid) : (parent ? $(selector, parent) : $(selector, $("#"+this.pid))));
	};
	
	//获取页面内指定id的元素
	po.elementOfId = function(id, parent)
	{
		return this.element("#"+id, parent);
	};
	
	//获取页面内指定name的元素
	po.elementOfName = function(name, parent)
	{
		return this.element("[name='"+name+"']", parent);
	};
	
	//打开URL
	po.open = function(url, options)
	{
		options = $.extend({ fullUrl: false }, (options || {}));
		
		if(options.fullUrl !== true)
			url = this.concatContextPath(url);
		url = $.addParam(url, this.ppidParamName, this.pid);
		
		$.open(url, (options || {}));
	};
	
	//打开表格对话框
	po.openTableDialog = function(url, options)
	{
		options = $.extend({ width: "80vw" }, options);
		this.open(url, options);
	};
	
	//关闭此页面
	po.close = function()
	{
		$.closeDialog(this.element());
	};
	
	po.beforeClose = function(name, callback)
	{
		$.bindBeforeCloseDialogCallback(this.element(), name, callback);
	};
	
	po.getJson = function(url, data, success)
	{
		var args = $.makeArray(arguments);
		args[0] = this.concatContextPath(url);
		$.getJSON.apply($, args);
	};
	
	po.post = function(url, data, success)
	{
		var args = $.makeArray(arguments);
		args[0] = this.concatContextPath(url);
		$.post.apply($, args);
	};
	
	po.ajaxJson = function(url, options)
	{
		url = this.concatContextPath(url);
		$.ajaxJson(url, options);
	};
	
	po.ajax = function(url, options)
	{
		url = this.concatContextPath(url);
		options = (options || {});
		
		if(options.data && !options.type)
			options = $.extend({ type: "POST" }, options);
		
		$.ajax(url, options);
	};
	
	//页面是否在对话框内
	po.isInDialog = function()
	{
		return $.isInDialog(this.element());
	};
	
	/**
	 * 获取页面参数对象。
	 * @param name 可选，页面参数对象属性名
	 */
	po.pageParam = function(name)
	{
		var ppo = $.pageParam(this.element());
		return (name == null ? ppo : (ppo ? ppo[name] : null));
	};
	
	/**
	 * 调用页面参数对象指定函数。
	 * @param functionName 必选
	 * @param arg,... 可选，函数参数
	 */
	po.pageParamCall = function(functionName, arg)
	{
		var argArray = (arg == undefined ? undefined : $.makeArray(arguments).slice(1));
		return $.pageParamCall(this.element(), functionName, argArray);
	};
	
	//打开确认对话框
	po.confirm = function(options)
	{
		options = $.extend(
		{
			acceptLabel : this.i18n.confirm,
			rejectLabel : this.i18n.cancel,
			header : this.i18n.operationConfirm
		},
		options);
		
		$.confirm(options);
	};
	
	//删除操作确认
	po.confirmDelete = function(acceptHandler, rejectHandler)
	{
		var msg = this.i18n.confirmDeleteAsk;
		this.confirm({ message: msg, accept: acceptHandler, reject: rejectHandler });
	};
	
	//连接应用根路径
	po.concatContextPath = function(path)
	{
		return (path.charAt(0) == "/" ? this.contextPath + path : path);
	};
	
	po.attr = function(name, value)
	{
		var attrs = (this._attrs || (this._attrs = {}));
		
		if(value === undefined)
			return attrs[name];
		else
			attrs[name] = value;
	};
	
	//获取/填充并返回vue页面模型，在vue页面中可以"pm.*"访问模型中的属性
	po.vuePageModel = function(obj)
	{
		return this.vueReactive("pm", obj);
	};
	
	//获取/填充并返回vue的setup响应式对象（自动reactive），对象格式必须为：{...}
	po.vueReactive = function(name, obj)
	{
		if(obj === undefined)
			return this._vueSetup[name];
		else
		{
			var rtvObj = (this._vueSetup[name] || (this._vueSetup[name] = Vue.reactive({})));
			
			for(var p in obj)
				rtvObj[p] = obj[p];
			
			return rtvObj;
		}
	};
	
	//获取/设置（自动ref）vue的setup引用值
	po.vueRef = function(name, value)
	{
		var obj = this._vueSetup[name];
		
		if(value === undefined)
			return obj;
		else
		{
			if(obj == null)	
				this._vueSetup[name] = Vue.ref(value);
			else
				obj.value = value;
		}
	};
	
	//设置vue的setup函数
	po.vueMethod = function(name, method)
	{
		var methodsObj = {};
		
		// ({ a: Function, b: Function)
		if(arguments.length == 1)
			methodsObj = name;
		// (name, Function)
		else if(arguments.length == 2)
			methodsObj[name] = method;
		
		for(var p in methodsObj)
			this._vueSetup[p] = methodsObj[p];
	};
	
	//设置vue的计算属性
	po.vueComputed = function(name, handler)
	{
		var computedObj = {};
		
		// ({ a: Function, b: Function)
		if(arguments.length == 1)
			computedObj = name;
		// (name, Function)
		else if(arguments.length == 2)
			computedObj[name] = handler;
		
		for(var p in computedObj)
			this._vueComputed[p] = computedObj[p];
	};
	
	//获取/设置vue组件
	po.vueComponent = function(name, value)
	{
		if(value === undefined)
			return this._vueComponents[name];
		else
			this._vueComponents[name] = value;
	};
	
	//设置vue监听
	po.vueWatch = function(target, callback)
	{
		this._vueWatch.push({ target: target, callback: callback });
	};
	
	//设置vue挂在后回调函数
	po.vueMounted = function(callback)
	{
		this._vueMounted.push(callback);
	};

	//获取指定名称对象的unref()结果
	po.vueUnref = function(name)
	{
		var obj = this._vueSetup[name];
		return Vue.unref(obj);
	};
	
	//获取toRaw()结果对象
	po.vueRaw = function(reactiveObj)
	{
		if($.isArray(reactiveObj))
		{
			var re = [];
			$.each(reactiveObj, function(idx, item)
			{
				re.push(Vue.toRaw(item));
			});
			
			return re;
		}
		else
			return Vue.toRaw(reactiveObj);
	};
	
	//执行vue的$nextTick操作
	po.vueNextTick = function(callback)
	{
		po.vueApp().$nextTick(callback);
	};
	
	//vue的setup对象
	po._vueSetup = {};
	//vue的watch对象
	po._vueWatch = [];
	//vue的watch对象
	po._vueComputed = {};
	//vue的mounted回调函数
	po._vueMounted = [];
	//vue组件
	po._vueComponents =
	{
		"p-tabmenu": primevue.tabmenu,
		"p-button": primevue.button,
		"p-datatable": primevue.datatable,
		"p-column": primevue.column,
		"p-inputtext": primevue.inputtext,
		"p-checkbox": primevue.checkbox,
		"p-textarea": primevue.textarea,
		"p-card": primevue.card,
		"p-dialog": primevue.dialog,
		"p-password": primevue.password,
		"p-divider": primevue.divider,
		"p-selectbutton": primevue.selectbutton,
		"p-dropdown": primevue.dropdown,
		"p-togglebutton": primevue.togglebutton,
		"p-radiobutton": primevue.radiobutton,
		"p-splitbutton": primevue.splitbutton,
		"p-tree": primevue.tree,
		"p-tabview": primevue.tabview,
		"p-tabpanel": primevue.tabpanel,
		"p-menu": primevue.menu,
		"p-menubar": primevue.menubar,
		"p-tieredmenu": primevue.tieredmenu,
		"p-chip": primevue.chip,
		"p-fileupload": primevue.fileupload,
		"p-inlinemessage": primevue.inlinemessage,
		"p-steps": primevue.steps,
		"p-dataview": primevue.dataview,
		"p-overlaypanel": primevue.overlaypanel,
		"p-panel": primevue.panel,
		"p-fieldset": primevue.fieldset,
		"p-listbox": primevue.listbox,
		"p-colorpicker": primevue.colorpicker,
		"p-splitter": primevue.splitter,
        "p-splitterpanel": primevue.splitterpanel,
        "p-progressbar": primevue.progressbar
	};
	
	//vue挂载
	po.vueMount = function(app)
	{
		const setupObj = this._vueSetup;
		const watchObj = this._vueWatch;
		const computedObj = this._vueComputed;
		const mountedObj = this._vueMounted;
		const componentsObj = this._vueComponents;
		
		app = $.extend((app || {}),
		{
			setup()
			{
				$.each(watchObj, function(idx, wt)
				{
					Vue.watch(wt.target, wt.callback);
				});
				
				for(var cpn in computedObj)
				{
					setupObj[cpn] = Vue.computed(computedObj[cpn]);
				}
				
				Vue.onMounted(function()
				{
					$.each(mountedObj, function(idx, callback)
					{
						callback();
					});
					
					$.initGlobalTip();
					$.initGlobalConfirm();
				});
				
				return setupObj;
			},
			components: componentsObj
		});
		
		this._vueApp = Vue.createApp(app).use(primevue.config.default)
						.directive("tooltip", primevue.tooltip).mount("#"+this.pid);
		return this._vueApp;
	};
	
	//获取挂载后的vue实例
	po.vueApp = function()
	{
		return this._vueApp;
	};
};

//填充page_manager.ftl里JS对象的静态逻辑
$.inflatePageManager = function(po)
{
	po.refresh = function(){ /*需实现*/ };
	po.getSelectedEntities = function(){ /*需实现*/ };
	
	po.setupAction = function()
	{
		po.vuePageModel(
		{
			action: po.action,
			isQueryAction: po.isQueryAction,
			isSelectAction: po.isSelectAction,
			isMultipleSelect: po.isMultipleSelect,
			isReadonlyAction: po.isReadonlyAction
		});
	};
	
	//单选处理函数
	po.executeOnSelect = function(callback)
	{
		var selected = po.getSelectedEntities();
		
		if(!selected || selected.length != 1)
		{
			$.tipInfo(po.i18n.pleaseSelectOnlyOne);
			return;
		}
		
		callback.call(po, selected[0]);
	};
	
	//多选处理函数
	po.executeOnSelects = function(callback)
	{
		var selected = po.getSelectedEntities();
		
		if(!selected || selected.length < 1)
		{
			$.tipInfo(po.i18n.pleaseSelectAtLeastOne);
			return;
		}
		
		callback.call(po, selected);
	};
	
	po.handleAddAction = function(url, options)
	{
		var action = { url: url, options: options };
		po.inflateFormActionPageParam(action);
		po.open(action.url, action.options);
	};
	
	po.handleOpenOfAction = function(url, options)
	{
		po.executeOnSelect(function(entity)
		{
			po.doOpenOfAction(url, entity, options);
		});
	};
	
	po.doOpenOfAction = function(url, entity, options)
	{
		var action = { url: url, options: options };
		po.inflateFormActionPageParam(action);
		po.inflateEntityAction(action, entity);
		po.open(action.url, action.options);
	};
	
	po.handleOpenOfsAction = function(url, options)
	{
		po.executeOnSelects(function(entities)
		{
			po.doOpenOfsAction(url, entities, options);
		});
	};
	
	po.doOpenOfsAction = function(url, entities, options)
	{
		var action = { url: url, options: options };
		po.inflateFormActionPageParam(action);
		po.inflateEntityAction(action, entities);
		po.open(action.url, action.options);
	};
	
	po.handleDeleteAction = function(url, options)
	{
		po.executeOnSelects(function(entities)
		{
			po.confirmDelete(function()
			{
				options = $.extend(
				{
					contentType: $.CONTENT_TYPE_JSON,
					success: function(){ po.refresh(); }
				},
				options);
				
				var action = { url: url, options: options };
				po.inflateEntityAction(action, entities);
				
				po.ajaxJson(action.url, action.options);
			});
		});
	};
	
	po.handleSelectAction = function()
	{
		if(po.isMultipleSelect)
		{
			po.executeOnSelects(function(entities)
			{
				po.pageParamCallSelect(entities);
			});
		}
		else
		{
			po.executeOnSelect(function(entity)
			{
				po.pageParamCallSelect(entity);
			});
		}
	};
	
	//调用页面参数对象的"select"函数
	po.pageParamCallSelect = function(selected, close)
	{
		close = (close == null ? true : close);
		
		var myClose = po.pageParamCall("select", selected);
		
		if(myClose === false)
			return;
		
		if(close)
			po.close();
	};
	
	po.inflateFormActionPageParam = function(action)
	{
		action.options = $.extend(
		{
			pageParam:
			{
				submitSuccess: function()
				{
					po.refresh();
				}
			}
		},
		action.options);
	};
	
	//将单行或多行数据对象转换为操作请求数据
	po.inflateEntityAction = function(action, entityOrArray)
	{
		var id = $.propertyValue(entityOrArray, po.inflateEntityActionIdPropName);
		
		if($.CONTENT_TYPE_JSON == action.options.contentType)
		{
			var options = action.options;
			if(options.data == null)
				options.data = id;
			else
			{
				var data = {};
				data[po.inflateEntityActionIdParamName] = id;
				options.data = $.extend(data, options.data);
			}
		}
		else
		{
			if($.isArray(id))
			{
				for(var i=0; i<id.length; i++)
					action.url = $.addParam(action.url, po.inflateEntityActionIdParamName, id[i], true);
			}
			else
				action.url = $.addParam(action.url, po.inflateEntityActionIdParamName, id);
		}
	};
	
	po.inflateEntityActionIdPropName = "id";
	po.inflateEntityActionIdParamName = "id";
};

//填充page_table.ftl里JS对象的静态逻辑
$.inflatePageTable = function(po)
{
	//重写搜索表单提交处理函数
	po.search = function(formData)
	{
		po.ajaxTableQuery($.extend(formData, { page: 1 }));
		po.loadAjaxTable();
	};
	
	po.refresh = function()
	{
		//兼容搜索表单集成
		if(po.submitSearchForm)
			po.submitSearchForm();
		else
			po.loadAjaxTable();
	};
	
	po.getSelectedEntities = function()
	{
		var pm = po.vuePageModel();
		return $.wrapAsArray(po.vueRaw(pm.selectedItems));
	};
	
	po.rowsPerPageOptions = [10, 20, 50, 100, 200];
	po.rowsPerPage = po.rowsPerPageOptions[1];
	
	po.ajaxTableAttr = function(obj)
	{
		return po.attr("ajaxTableAttr", obj);
	};
	
	po.setupAjaxTable = function(url, options)
	{
		options = $.extend({ multiSortMeta: [], initData: true }, options);
		
		po.setupAction();
		
		var pm = po.vuePageModel(
		{
			items: [],
			paginator: true,
			paginatorTemplate: "CurrentPageReport FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink RowsPerPageDropdown",
			pageReportTemplate: "{first}-{last} / {totalRecords}",
			rowsPerPage: po.rowsPerPage,
			rowsPerPageOptions: po.rowsPerPageOptions,
			totalRecords: 0,
			loading: false,
			selectionMode: ((po.isQueryAction || po.isMultipleSelect) ? "multiple" : "single"),
			multiSortMeta: options.multiSortMeta,
			selectedItems: null
		});
		
		po.vueMethod(
		{
			onPaginator: function(e)
			{
				po.ajaxTableQuery({ page: e.page+1, pageSize: e.rows, orders: po.sortMetaToOrders(e.multiSortMeta) });
				po.loadAjaxTable();
			},
			onSort: function(e)
			{
				po.ajaxTableQuery({ orders: po.sortMetaToOrders(e.multiSortMeta) });
				po.loadAjaxTable();
			}
		});
		
		po.ajaxTableAttr(
		{
			url: url,
			query: { page: 1, pageSize: po.rowsPerPage, orders: po.sortMetaToOrders(options.multiSortMeta) }
		});
		
		if(options.initData)
		{
			po.vueMounted(function()
			{
				po.refresh();
			});
		}
		
		return pm;
	};
	
	po.ajaxTableQuery = function(query)
	{
		var ajaxTableAttr = po.ajaxTableAttr();
		
		if(query === undefined)
			return ajaxTableAttr.query;
		else
			$.extend(ajaxTableAttr.query, query);
	};
	
	po.loadAjaxTable = function(options)
	{
		options = (options || {});
		
		var ajaxTableAttr = po.ajaxTableAttr();
		var pm = po.vuePageModel();
		pm.loading = true;
		
		options = $.extend(
		{
			data: ajaxTableAttr.query,
			success: function(response)
			{
				po.setAjaxTableData(response);
			},
			complete: function()
			{
				pm.loading = false;
			}
		},
		options);
		
		po.ajaxJson(ajaxTableAttr.url, options);
	};
	
	po.sortMetaToOrders = function(sortMeta)
	{
		if(sortMeta == null)
		{
			var pm = po.vuePageModel();
			sortMeta = pm.multiSortMeta;
		}
		
		var orders = [];
		
		$.each(sortMeta, function(idx, sm)
		{
			orders.push({ name: sm.field, type: (sm.order > 0 ? "ASC" : "DESC") });
		});
		
		return orders;
	};
	
	po.setAjaxTableData = function(data)
	{
		var isPagingData = (data.items != null && data.total != null);
		var pm = po.vuePageModel();
		
		pm.items = (isPagingData ? data.items : data);
		pm.totalRecords = (isPagingData ? data.total : data.length);
		pm.selectedItems = null;
	};
};

//填充page_form.ftl里JS对象的静态逻辑
$.inflatePageForm = function(po)
{
	//获取/填充并返回vue表单模型，在vue页面中可以"fm.*"访问模型中的属性
	po.vueFormModel = function(obj)
	{
		return po.vueReactive("fm", obj);
	};
	
	po.setupForm = function(data, ajaxOptions, validateOptions)
	{
		data = (data || {});
		ajaxOptions = (ajaxOptions || {});
		validateOptions = (validateOptions || {});
		
		po.vuePageModel(
		{
			action: po.action,
			isAddAction: po.isAddAction,
			isEditAction: po.isEditAction,
			isViewAction: po.isViewAction,
			isCopyAction: po.isCopyAction,
			isReadonlyAction: po.isReadonlyAction
		});
		
		var fm = po.vueFormModel(data);
		
		po.vueMounted(function()
		{
			po.initValidationMessagesIfNon();
			
			//当需要在options中返回DOM元素时，应定义为函数，因为vue挂载前元素可能不必配
			if($.isFunction(ajaxOptions))
				ajaxOptions = ajaxOptions();
			if($.isFunction(validateOptions))
				validateOptions = validateOptions();
			
			validateOptions = $.extend(
			{
				submitHandler: function(form)
				{
					var submitUrl = ($.isFunction(po.submitUrl) ? po.submitUrl() : po.submitUrl);
					return po.submitForm(submitUrl, ajaxOptions);
				}
			},
			validateOptions);
			
			po.form().validateForm(fm, validateOptions);
		});
		
		return fm;
	};
	
	po.submitForm = function(url, options)
	{
		options = $.extend(
		{
			defaultSuccessCallback: true,
			closeAfterSubmit: true,
			ignoreIfViewAction: true
		},
		options);
		
		if(options.ignoreIfViewAction && (po.isViewAction || url == "#"))
			return;
		
		var fm = po.vueFormModel();
		options = $.extend(true, options, { data: po.vueRaw(fm) });
		
		var successHandlers = (options.success ? [].concat(options.success) : []);
		successHandlers.push(function(response)
		{
			if(options.defaultSuccessCallback && po.defaultSubmitSuccessCallback)
				po.defaultSubmitSuccessCallback(response, options.closeAfterSubmit);
		});
		options.success = successHandlers;
		
		var action = { url: url, options: options };
		
		if(po.beforeSubmitForm(action) !== false)
		{
			var jsonSubmit = (action.options.contentType == null || action.options.contentType == $.CONTENT_TYPE_JSON);
			
			if(jsonSubmit)
				po.ajaxJson(action.url, action.options);
			else
				po.ajax(action.url, action.options);
		}
		
		return false;
	};
	
	//返回false会阻止表单提交
	po.beforeSubmitForm = function(action){};
	
	po.defaultSubmitSuccessCallback = function(response, close)
	{
		close = (close == null ? true : close);
		
		var myClose = po.pageParamCallSubmitSuccess(response);
		
		if(myClose === false)
			return;
		
		if(close)
			po.close();
	};
	
	po.pageParamCallSubmitSuccess = function(response)
	{
		po.pageParamCall("submitSuccess", (response.data ? response.data : response));
	};
	
	po.handleOpenSelectAction = function(url, callback, options)
	{
		options = (options || {});
		options = $.extend(
		{
			pageParam:
			{
				select: callback
			}
		},
		options);
		
		po.openTableDialog(url, options);
	};
};

//填充page_code_editor.ftl里JS对象的静态逻辑
$.inflatePageCodeEditor = function(po)
{
	//停止输入这些毫秒数后才进行提示，避免干扰用户输入
	po.codeEditorHintingDelay = 500;
	
	po.createCodeEditor = function(dom, options)
	{
		dom = $(dom);
		options = (options || {});
		
		dom.on("keydown,keypress,keyup", function(e)
		{
			//阻止TAB键切换焦点
			if(e.keyCode == $.keyCode.TAB)
				e.stopPropagation();
		});
		
		//采用系统切换主题功能模式
		options.theme = "custom";
		
		if(options.lineNumbers == null)
			options.lineNumbers = true;
		
		if(options.smartIndent == null)
			options.smartIndent = false;
		
		if(options.indentWithTabs == null)
			options.indentWithTabs = true;
		
		//强制禁用completeSingle选项，因为如果编辑器hint使用change事件中触发的话，
		//如果这里为true，可能会导致hint死循环，且会导致退格操作无效
		if(options.hintOptions)
			options.hintOptions.completeSingle = false;
		
		//if(options.hintOptions)
		//	options.hintOptions.closeOnUnfocus = false;
		
		var codeEditor = CodeMirror(dom[0], options);
		
		if(options.hintOptions && !options.readOnly)
		{
			codeEditor.on("keyup", function(codeEditor, e)
			{
				if(e.keyCode == $.keyCode.ESCAPE || e.keyCode == $.keyCode.UP
						|| e.keyCode == $.keyCode.DOWN)
				{
					return;
				}
				
				if(codeEditor._timeoutIdForHinting != null)
					clearTimeout(codeEditor._timeoutIdForHinting);
				
				codeEditor._timeoutIdForHinting = setTimeout(function()
				{
					codeEditor.showHint();
					codeEditor._timeoutIdForHinting = null;
				},
				po.codeEditorHintingDelay);
			});
		}
		
		return codeEditor;
	};
	
	po.evalCodeModeByName = function(name)
	{
		var mode = undefined;
		
		if($.isHtmlFile(name))
			mode = "htmlmixed";
		else if($.isJsFile(name))
			mode = "javascript";
		else if($.isCssFile(name))
			mode = "css";
		
		return mode;
	};
	
	po.getCodeText = function(codeEditor)
	{
		var doc = codeEditor.getDoc();
		return doc.getValue();
	};

	po.setCodeText = function(codeEditor, text)
	{
		var doc = codeEditor.getDoc();
		doc.setValue(text || "");
	};
	
	po.setCodeTextTimeout = function(codeEditor, text, focus)
	{
		focus = (focus == null ? false : focus);
		
		//在对话框时，直接初始化代码编辑器会出现行号错位的情况，使用这种方式可解决
		setTimeout(function()
		{
			po.setCodeText(codeEditor, text);
			if(focus)
				codeEditor.focus();
		},
		200);
	};
	
	po.getSelectedCodeText = function(codeEditor)
	{
		var doc = codeEditor.getDoc();
		return (doc.getSelection() || "");
	};
	
	po.getSelectedCodeInfo = function(codeEditor)
	{
		var doc = codeEditor.getDoc();
		var selCodes = doc.getSelections();
		var selRanges = doc.listSelections();
		
		var selText = (selCodes && selCodes[0] ? (selCodes[0] || "") : "");
		var from = (selRanges && selRanges[0] ? selRanges[0].anchor : null);
		var to = (selRanges && selRanges[0] ? selRanges[0].head : null);
		
		if(from && to)
		{
			var swap = ((from.line > to.line) || (from.line == to.line && from.ch > to.ch));
			if(swap)
			{
				var fromTmp = from;
				from = to;
				to = fromTmp;
			}
		}
		
		return { text: selText, from: from, to: to };
	};
	
	po.insertCodeText = function(codeEditor, cursor, text)
	{
		//(codeEditor, text)
		if(arguments.length == 2)
		{
			text = cursor;
			cursor = undefined;
		}
		
		var doc = codeEditor.getDoc();
		cursor = (cursor == null ? doc.getCursor() : cursor);
		
		doc.replaceRange(text, cursor);
	};
	
	//查找补全列表
	//completions : { name: "...", ?value: "...", ?displayName: "...", ?displayComment: "...", ?categories: [ "小写字符串", ... ] }
	po.findCompletionList = function(completions, namePrefix, category)
	{
		var re = [];
		
		if(!completions)
			return re;
		
		namePrefix = (namePrefix ? namePrefix.toLowerCase() : namePrefix);
		category = (category ? category.toLowerCase() : category);
		
		for(var i=0; i<completions.length; i++)
		{
			var comp = completions[i];
			
			//相同时不必列入提示，影响输入
			if(namePrefix && namePrefix.length == comp.name.length)
				continue;
			
			var nameLower = comp.name.toLowerCase();
			
			if(namePrefix && nameLower.indexOf(namePrefix) != 0)
				continue;
			
			if(!category || (category && comp.categories && $.inArray(category, comp.categories) > -1))
			{
				re.push(
				{
					text: (comp.value ? comp.value : comp.name),
					displayText: (comp.displayName ? comp.displayName : comp.name),
					displayComment: comp.displayComment,
					render: po.renderCompletionItem
				});
			}
		}
		
		return re;
	};
	
	po.renderCompletionItem = function(element, self, data)
	{
		//$(element).addClass("code-completion-item");
		
		$("<span class='code-completion-item' />").text(data.displayText ? data.displayText : data.text).appendTo(element);
		if(data.displayComment)
			$("<span class='code-completion-comment' />").text(data.displayComment ? data.displayComment : "").appendTo(element);
	};
	
	po.findPrevTokenOfType = function(codeEditor, doc, cursor, cursorToken, tokenType)
	{
		var tokenInfo = po.findPrevTokenInfoOfType(codeEditor, doc, cursor, cursorToken, tokenType);
		return (tokenInfo ? tokenInfo.token : undefined);
	};
	
	po.findPrevTokenInfoOfType = function(codeEditor, doc, cursor, cursorToken, tokenType)
	{
		return po.findPrevTokenInfo(codeEditor, doc, cursor, cursorToken, function(token){ return (token.type == tokenType); });
	};
	
	po.findPrevTokenInfo = function(codeEditor, doc, cursor, cursorToken, predicate)
	{
		doc = (doc ? doc : codeEditor.getDoc());
		cursor = (cursor ? cursor : doc.getCursor());
		cursorToken = (cursorToken ? cursorToken : (codeEditor.getTokenAt(cursor) || {}));
		var minLine = (cursor.line-100 <= 0 ? 0 : cursor.line-100);
		
		for(var line=cursor.line; line >=minLine; line--)
		{
			var tokens = codeEditor.getLineTokens(line);
			for(var i=tokens.length-1; i>=0; i--)
			{
				var token = tokens[i];
				
				if(line == cursor.line && token.start >= cursorToken.start)
					continue;
				
				if(predicate(token) == true)
					return { token: token, line: line };
			}
		}
		
		return null;
	};
	
	po.findNextTokenInfoOfType = function(codeEditor, doc, cursor, cursorToken, tokenType)
	{
		return po.findNextTokenInfo(codeEditor, doc, cursor, cursorToken, function(token){ return (token.type == tokenType); });
	};
	
	po.findNextTokenInfo = function(codeEditor, doc, cursor, cursorToken, predicate)
	{
		doc = (doc ? doc : codeEditor.getDoc());
		var lastLine = doc.lastLine();
		cursor = (cursor ? cursor : doc.getCursor());
		cursorToken = (cursorToken ? cursorToken : (codeEditor.getTokenAt(cursor) || {}));
		
		for(var line=cursor.line; line<=lastLine; line++)
		{
			var tokens = codeEditor.getLineTokens(line);
			for(var i=0; i<tokens.length; i++)
			{
				var token = tokens[i];
				
				if(line == cursor.line && token.start <= cursorToken.start)
					continue;
				
				if(predicate(token) == true)
					return { token: token, line: line };
			}
		}
		
		return null;
	};
};

//填充page_sql_editor.ftl里JS对象的静态逻辑
$.inflatePageSqlEditor = function(po)
{
	//获取数据源ID
	po.getSqlEditorSchemaId = function(){ /*需实现*/ };
	
	//SQL提示缓存
	po.sqlHintCache =
	{
		//表名 -> 列名
		tableColumnCompletions: {},
		tableNameCompletions: [],
		tableNameCompletionsLoaded: false,
		ajaxRunning: false
	};
	
	po.createSqlEditor = function(dom, options)
	{
		options = po.inflateSqlEditorOptions(options);
		return po.createCodeEditor(dom, options);
	};
	
	po.inflateSqlEditorOptions = function(options)
	{
		options = (options || {});
		options.mode = "sql";
		
		if(!options.readOnly)
		{
			options.hintOptions = (options.hintOptions || {});
			options.hintOptions.hint = po.sqlEditorHintHandler;
			options.hintOptions.hint.async = true;
		}
		
		return options;
	};
	
	po.sqlEditorHintTableAjaxOptions = function(schemaId)
	{
		var options = { url: po.concatContextPath("/sqlEditor/"+schemaId+"/findTableNames") };
		return options;
	};
	
	po.sqlEditorHintColumnAjaxOptions = function(schemaId, tableName)
	{
		var options =
		{
			url: po.concatContextPath("/sqlEditor/"+schemaId+"/findColumns"),
			data: { table: tableName }
		};
		
		return options;
	};
	
	po.sqlEditorHintHandler = function(codeEditor, callback)
	{
		var doc = codeEditor.getDoc();
		var cursor = doc.getCursor();
		var mode = (codeEditor.getModeAt(cursor) || {});
		var token = (codeEditor.getTokenAt(cursor) || {});
		
		var schemaId = po.getSqlEditorSchemaId();
		
		//关键字token、分号token不应提示
		if(!schemaId || token.type == "keyword" || po.isTokenSemicolonOrAfter(codeEditor, doc, cursor, token))
		{
			callback();
			return;
		}
		
		var hintInfo = po.resolveSqlHintInfo(codeEditor, doc, cursor, token);
		
		if(!hintInfo || (hintInfo.type != "table" &&  hintInfo.type != "column"))
		{
			callback();
			return;
		}
		
		var namePrefix = hintInfo.namePrefix;
		
		if(hintInfo.type == "table")
		{
			if(po.sqlHintCache.tableNameCompletionsLoaded)
			{
				var completions =
				{
					list: po.findCompletionList(po.sqlHintCache.tableNameCompletions, namePrefix),
					from: CodeMirror.Pos(cursor.line, (namePrefix ? token.start : token.end)),
					to: CodeMirror.Pos(cursor.line, token.end)
				};
				
				callback(completions);
			}
			else
			{
				if(po.sqlHintCache.ajaxRunning)
					callback();
				else
				{
					po.sqlHintCache.ajaxRunning = true;
					
					var ajaxOptions = $.extend(
					{
						type : "POST",
						success: function(names)
						{
							names = (names || []);
							
							var tableNameCompletions = [];
							
							for(var i=0; i<names.length; i++)
								tableNameCompletions[i] = { name: names[i] };
							
							po.sqlHintCache.tableNameCompletions = tableNameCompletions;
							po.sqlHintCache.tableNameCompletionsLoaded = true;
							
							var completions =
							{
								list: po.findCompletionList(po.sqlHintCache.tableNameCompletions, namePrefix),
								from: CodeMirror.Pos(cursor.line, (namePrefix ? token.start : token.end)),
								to: CodeMirror.Pos(cursor.line, token.end)
							};
							
							callback(completions);
							po.sqlHintCache.ajaxRunning = false;
						},
						error: function()
						{
							callback();
							po.sqlHintCache.ajaxRunning = false;
						}
					},
					po.sqlEditorHintTableAjaxOptions(schemaId));
					
					$.ajax(ajaxOptions);
				}
			}
		}
		else if(hintInfo.type == "column" && hintInfo.tableName)
		{
			if(po.sqlHintCache.tableColumnCompletions[hintInfo.tableName])
			{
				var completions =
				{
					list: po.findCompletionList(po.sqlHintCache.tableColumnCompletions[hintInfo.tableName], namePrefix),
					from: CodeMirror.Pos(cursor.line, (namePrefix ? token.start : token.end)),
					to: CodeMirror.Pos(cursor.line, token.end)
				};
				
				callback(completions);
			}
			else
			{
				if(po.sqlHintCache.ajaxRunning)
					callback();
				else
				{
					po.sqlHintCache.ajaxRunning = true;
					
					var ajaxOptions = $.extend(
					{
						type : "POST",
						success: function(columns)
						{
							var columnCompletions = $.toSqlEditorColumnCompletions(hintInfo.tableName, columns);
							po.sqlHintCache.tableColumnCompletions[hintInfo.tableName] = columnCompletions;
							
							var completions =
							{
								list: po.findCompletionList(po.sqlHintCache.tableColumnCompletions[hintInfo.tableName], namePrefix),
								from: CodeMirror.Pos(cursor.line, (namePrefix ? token.start : token.end)),
								to: CodeMirror.Pos(cursor.line, token.end)
							};
							
							callback(completions);
							po.sqlHintCache.ajaxRunning = false;
						},
						error: function()
						{
							callback();
							po.sqlHintCache.ajaxRunning = false;
						}
					},
					po.sqlEditorHintColumnAjaxOptions(schemaId, hintInfo.tableName));
					
					$.ajax(ajaxOptions);
				}
			}
		}
		else
			callback();
	};
	
	//token是否是分号，或者除空格外的下一个
	po.isTokenSemicolonOrAfter = function(codeEditor, doc, cursor, token)
	{
		if(!token)
			return false;
		
		var scReg = /\;\s*$/;
		
		if(token.string && scReg.test(token.string))
			return true;
		
		var blankReg = /^\s*$/;
		
		var foundTokenInfo = po.findPrevTokenInfo(codeEditor, doc, cursor, token, function(token)
		{
			if(token.string != null && !blankReg.test(token.string))
				return true;
		});
		
		if(!foundTokenInfo || !foundTokenInfo.token || !foundTokenInfo.token.string)
			return false;
		
		return scReg.test(foundTokenInfo.token.string);
	};
	
	po.resolveSqlHintInfo = function(codeEditor, doc, cursor, cursorToken)
	{
		var info = null;
		
		var tokenInfo = null;
		var cursorTmp = cursor;
		var cursorTokenTmp = cursorToken;
		
		while((tokenInfo = po.findPrevTokenInfoOfType(codeEditor, doc, cursorTmp, cursorTokenTmp, "keyword")) != null)
		{
			var keywordToken = tokenInfo.token;
			var keyword = (keywordToken.string || "").toUpperCase();
			
			if(po.sqlKeywords.all[keyword])
			{
				if(po.sqlKeywords.nextIsTable[keyword])
					info = { type: "table", namePrefix: (po.isNormalSqlNameTokenType(cursorToken.type) ? ($.trim(cursorToken.string) || "") : "") };
				else if(po.sqlKeywords.nextIsColumn[keyword])
					info = { type: "column" };
				
				break;
			}
			
			cursorTmp = CodeMirror.Pos(tokenInfo.line, keywordToken.start);
			cursorTokenTmp = keywordToken;
		}
		
		//查找表名
		if(info && info.type == "column" && tokenInfo)
		{
			var columnInfoStr = po.resolveSqlColumnInfoString(codeEditor, doc, cursor, cursorToken);
			
			if(columnInfoStr)
			{
				var columnInfoStrs = columnInfoStr.split(".");
				info.namePrefix = (columnInfoStrs.length > 1 ? columnInfoStrs[1] : columnInfoStrs[0]);
				info.tableName = (columnInfoStrs.length > 1 ? columnInfoStrs[0] : null);
			}
			
			//向上直到SQL语句开头
			while(tokenInfo != null)
			{
				var myToken = tokenInfo.token;
				var myString = (myToken.string || "").toUpperCase();
				
				if(po.sqlKeywords.start[myString])
					break;
				
				tokenInfo = po.findPrevTokenInfoOfType(codeEditor, doc, CodeMirror.Pos(tokenInfo.line, myToken.start), myToken, "keyword");
			}
			
			//向下查找表名的前置关键字token
			while(tokenInfo != null)
			{
				var myToken = tokenInfo.token;
				var myString = (myToken.string || "").toUpperCase();
				
				if(po.sqlKeywords.nextIsTable[myString])
					break;
				
				tokenInfo = po.findNextTokenInfoOfType(codeEditor, doc, CodeMirror.Pos(tokenInfo.line, myToken.start), myToken, "keyword");
			}
			
			//向下解析表名
			if(tokenInfo)
			{
				var prevTokenType = null, prevTokenString = null;
				var prevPrevTokenType = null, prevPrevTokenString = null;
				tokenInfo = po.findNextTokenInfo(codeEditor, doc, CodeMirror.Pos(tokenInfo.line, tokenInfo.token.start), tokenInfo.token,
				function(token)
				{
					//如果有括号，说明是复杂语句，暂不解析
					if(token.type == "bracket")
						return true;
					
					var myString = ($.trim(token.string) || "");
					
					if(!myString)
						return false;
					
					if(po.isNormalSqlNameTokenType(token.type))
					{
						//如果没有表别名，则使用第一个作为表名
						if(!info.tableName)
						{
							info.tableName = myString;
							return true;
						}
						else
						{
							//判断是否表别名
							if(myString == info.tableName)
							{
								//表名 AS 别名
								if(prevTokenType == "keyword" && /as/i.test(prevTokenString)
										&& po.isNormalSqlNameTokenType(prevPrevTokenType) && prevPrevTokenString)
								{
									info.tableName = prevPrevTokenString;
								}
								//表名 别名
								else if(po.isNormalSqlNameTokenType(prevTokenType) && prevTokenString)
								{
									info.tableName = prevTokenString;
								}
								
								return true;
							}
						}
					}
					
					prevPrevTokenType = prevTokenType;
					prevPrevTokenString = prevTokenString;
					prevTokenType = token.type;
					prevTokenString = myString;
				});
			}
		}
		
		return info;
	};
	
	po.resolveSqlColumnInfoString = function(codeEditor, doc, cursor, cursorToken)
	{
		var columnInfoString = "";
		
		if(po.isSqlColumnInputStringPart(cursorToken))
		{
			columnInfoString = cursorToken.string;
			
			po.findPrevTokenInfo(codeEditor, doc, cursor, cursorToken, function(token)
			{
				if(po.isSqlColumnInputStringPart(token))
					columnInfoString = token.string + columnInfoString;
				else
					return true;
			});
		}
		
		return columnInfoString;
	};
	
	po.isSqlColumnInputStringPart = function(cursorToken)
	{
		var str = cursorToken.string;
		
		//","、"("、"空白" 不是列相关输入字符串
		if(/^[\(\,]$/.test(str) || /^\s*$/.test(str))
			return false;
		
		return true;
	};
	
	po.isNormalSqlNameTokenType = function(tokenType)
	{
		return (tokenType == null);
	};
	
	po.sqlKeywords =
	{
		//全部，会由下面关键字合并而得
		all: {},
		
		//SQL语句开始关键字*（必须大写）
		start:
		{
			"SELECT" : true, "INSERT" : true, "UPDATE" : true, "DELETE" : true,
			"ALTER" : true, "DROP" : true, "CREATE" : true, "REPLACE" : true, "MERGE" : true,
			"GRANT" : true
		},
		
		//下一个Token是表名（必须大写）
		nextIsTable:
		{
			"FROM" : true,
			"JOIN" : true,
			"UPDATE" : true,
			"INTO" : true,
			"TABLE" : true
		},
		
		//下一个Token是列名（必须大写）
		nextIsColumn:
		{
			"SELECT" : true,
			"WHERE" : true,
			"ON" : true,
			"BY" : true,
			"SET" : true
		}
	};
	
	po.sqlKeywords.all = $.extend(po.sqlKeywords.all, po.sqlKeywords.start,
									po.sqlKeywords.nextIsTable, po.sqlKeywords.nextIsColumn);
};

})
(jQuery);
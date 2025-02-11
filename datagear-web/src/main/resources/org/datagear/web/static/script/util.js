/*
 * Copyright 2018-present datagear.tech
 *
 * This file is part of DataGear.
 *
 * DataGear is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * DataGear is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with DataGear.
 * If not, see <https://www.gnu.org/licenses/>.
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
	 *              //当dialog=true时，是否可关闭
	 * 				closable: true,
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
	 *              //当dialog=true时，对话框头部自定义HTML模板
	 * 				templateHeader: "",
	 *              //当dialog=true时，对话框setup回调函数
	 * 				onSetup: function(setupObj){},
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
					const pdialogEle = $("<p-dialog></p-dialog>").attr("id", dialogEleId).attr("app-ele-id", rootEleId)
								.attr(":header", "model.header").attr("v-model:visible", "model.visible")
								.attr("v-model:closable", "model.closable").attr(":modal", options.modal)
								.attr("v-on:show", "onDialogShow").attr("v-on:after-hide", "onDialogAfterHide")
								.attr("v-on:hide", "onDialogHide")
								.attr(":close-on-escape", "false")
								.attr(":style", "{width: model.width}")
								.attr("class", "ajax-dialog " + $.PAGE_PARAM_BINDER_CLASS + " " + options.styleClass)
								.attr("position", options.position)
								.appendTo(rootEle);
					
					if(options.templateHeader)
					{
						pdialogEle.prepend("<template #header>"+options.templateHeader+"</template>");
					}
					
					var dialogApp =
					{
						setup()
						{
							const model = Vue.reactive(
							{
								header: (options.title || " "),
								visible: true,
								width: options.width,
								closable: options.closable
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
							};
							const onDialogAfterHide = function()
							{
								$._destroyDialogApp(rootEle);
							};
							
							const setupObj = {model, onDialogShow, onDialogHide, onDialogAfterHide};
							
							if(options.onSetup)
								options.onSetup(setupObj);
							
							return setupObj;
						},
						components: $.vueComponents(),
						
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
		var appEle = $("#"+ dialogEle.attr("app-ele-id"));
		
		if(appEle && appEle.length > 0)
		{
			var callbacks = appEle.data("beforeCloseCallbacks");
			if(!callbacks)
			{
				callbacks = {};
				appEle.data("beforeCloseCallbacks", callbacks);
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
		var beforeCloseCallbacks = appEle.data("beforeCloseCallbacks");
		
		if(beforeCloseCallbacks)
		{
			$.each(beforeCloseCallbacks, function(name, callback)
			{
				callback();
			});
		}
	};
	
	$._destroyDialogApp = function(appEle)
	{
		var dialogApp = appEle.data("dialogApp");
		var dialogVm = appEle.data("dialogVm");
		
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
	
	//在输入框光标位置插入文本
	$.insertAtCaret = function(ele, text, focus)
	{
		ele = (ele ? $(ele) : null);
		text = (text == null ? "" : text);
		focus = (focus == null ? true : focus);
		
		var dom = (ele ? ele[0] : null);
		
		if(!dom)
			return;
		
		var val = ele.val();
		
		if(dom.selectionStart != null)
		{
			var startIdx = dom.selectionStart;
			var endIdx = dom.selectionEnd;
			val = val.substring(0, startIdx) + text + val.substring(endIdx, val.length);
			
			ele.val(val);
			dom.selectionStart = startIdx + text.length;
			dom.selectionEnd = dom.selectionStart;
		}
		else
		{
			val = val + text;
			ele.val(val);
		}
		
		if(focus)
			ele.focus();
		
		return val;
	};
	
	//聚焦至指定元素内的第一个可操作（非只读、非禁用）输入框
	$.focusOnFirstInput = function(ele)
	{
		var input = $(":input:not(:disabled,[readonly]):first", ele); 
		input.focus();
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
	
	$.isTypeBoolean = function(obj)
	{
		return (typeof(obj) == $.TYPEOF_BOOLEAN);
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
	
	$.inTreeArrayById = function(treeArray, idValue, idPropName, childrenPropName)
	{
		idPropName = (idPropName == null ? "id" : idPropName);
		childrenPropName = (childrenPropName == null ? "children" : childrenPropName);
		
		var idx = $.inArrayById(treeArray, idValue, idPropName);
		
		if(idx > -1)
			return true;
			
		for(var i=0; i<treeArray.length; i++)
		{
			var children = (treeArray[i] ?  treeArray[i][childrenPropName] : null);
			
			if(children && $.inTreeArrayById(children, idValue, idPropName, childrenPropName))
				return true;
		}
		
		return false;
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
	
	//整理数组至指定长度
	$.trimArrayLen = function(array, len, initValue)
	{
		len = (len == null ? 0 : len);
		
		while(array.length < len)
		{
			array.push(initValue);
		}
		
		while(array.length > len)
		{
			array.pop();
		}
		
		return array;
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
	
	//是否为空
	$.isEmptyValue = function(value, checkElement, checkProperty)
	{
		checkElement = (checkElement == null ? false : checkElement);
		checkProperty = (checkProperty == null ? false : checkProperty);
		
		if(value == null)
			return true;
		else if($.isTypeString(value))
			return (value.length == 0);
		else if($.isArray(value))
		{
			if(!checkElement)
				return (value.length == 0);
			else
			{
				for(var i=0; i<value.length; i++)
				{
					if($.isEmptyValue(value[i], false, false))
						return true;
				}
				
				return (value.length == 0);
			}
		}
		else if($.isPlainObject(value))
		{
			var pcount = 0;
			
			for(var p in value)
			{
				pcount++;
				
				if(checkProperty && $.isEmptyValue(value[p], false, false))
					return true;
			}
			
			return (pcount == 0);
		}
		else
			return false;
	};
	
	/**
	 * 比较版本号。
	 * 支持版本号格式示例：
	 * 1、1-alpha、1.1、1.1-alpha、1.1.1、1.1.1-alpha、1.1.1.1、1.1.1.1-alpha
	 * 
	 * @param v1
	 * @param v2
	 * @returns -1 v1低于v2；0 v1等于v2；1 v1高于v2
	 */
	$.compareVersion = function(v1, v2)
	{
		if(v1 === v2)
			return 0;
		
		var b1 = "";
		var b2 = "";
		
		var bIdx1 = v1.indexOf("-");
		if(bIdx1 > 0)
		{
			b1 = (bIdx1 >= v1.length - 1 ? "" : v1.substring(bIdx1 + 1));
			v1 = v1.substring(0, bIdx1);
		}
		
		var bIdx2 = v2.indexOf("-");
		if(bIdx2 > 0)
		{
			b2 = (bIdx2 >= v2.length - 1 ? "" : v2.substring(bIdx2 + 1));
			v2 = v2.substring(0, bIdx2);
		}
		
		var v1ds = v1.split(".");
		var v2ds = v2.split(".");
		
		for(var i= 0, len = Math.max(v1ds.length, v2ds.length); i<len; i++)
		{
			var num1 = (v1ds[i] == null ? 0 : parseInt(v1ds[i]));
			var num2 = (v2ds[i] == null ? 0 : parseInt(v2ds[i]));
			
			if(num1 > num2)
			{
				return 1;
			}
			else if(num1 < num2)
			{
				return -1;
			}
		}
		
		if(b1 > b2)
			return 1;
		else if(b1 < b2)
			return -1;
		else
			return 0;
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
		
		var anchor = "";
		var aidx = url.indexOf('#');
		if(aidx >= 0)
		{
			var tmpUrl = url.substring(0, aidx);
			anchor = url.substring(aidx);
			url = tmpUrl;
		}
		
		var qidx = url.indexOf('?');
		
		if(multiple == true || qidx < 0)
		{
			var f = (qidx < 0 ? "?" : "&");
			url = url + f + name + "=" + value;
		}
		else
		{
			var keyword = name+"=";
			var start = url.indexOf(keyword, qidx+1);
			if(start >= 0)
			{
				var head = url.substring(0, start);
				start = start+keyword.length;
				var endIdx = url.indexOf("&", start);
				var tail = (endIdx >= 0 ? url.substr(endIdx) : "");
				url = head + tail;
			}
			
			qidx = url.indexOf('?');
			
			url += (qidx == (url.length-1) ? "" : "&") + name +"=" + value;
		}
		
		return url + anchor;
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
	 * 获取/设置指定属性路径的值。
	 * 
	 * @param obj
	 * @param propPath
	 * @param value
	 */
	$.propPathValue = function(obj, propPath, value)
	{
		var setOpt = (value !== undefined);
		
		if(setOpt && obj == null)
			return;
		
		var propArray = $.splitPropPath(propPath);
		var parent = obj;
		
		for(var i=0; i<propArray.length; i++)
		{
			if(parent == null && !setOpt)
				return null;
			
			var pn = propArray[i];
			var isEle = (pn.length >= 3 && pn.charAt(0) == '[' && pn.charAt(pn.length-1) == ']' );
			var eleIdx = (isEle ? parseInt(pn.substring(1, pn.length-1)) : null);
			var pv = parent[(isEle ? eleIdx : pn)];
			
			if(i == (propArray.length - 1))
			{
				if(!setOpt)
				{
					return pv;
				}
				else
				{
					parent[(isEle ? eleIdx : pn)] = value;
				}
			}
			else
			{
				//设置操作，补全中间对象
				if(setOpt && pv == null)
				{
					var pnNext = propArray[i+1];
					var isPnNextEle = (pnNext.length >= 3 && pnNext.charAt(0) == '[' && pnNext.charAt(pnNext.length-1) == ']' );
					pv = (isPnNextEle ? [] : {});
					parent[(isEle ? eleIdx : pn)] = pv;
				}
				
				parent = pv;
			}
		}
	};
	
	/**
	 * 拆分属性路径字符串为数组。
	 * 
	 * @param str 属性路径字符串，格式为："a.b[0].c"，拆分为：["a", "b", "[0]", "c"]
	 */
	$.splitPropPath = function(str)
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
	
	$.replaceAllSubStr = function(str, subStr, replacement)
	{
		if(str == null)
			return str;
		
		if(str.replaceAll !== undefined)
		{
			str = str.replaceAll(subStr, replacement);
		}
		else
		{
			//兼容旧版浏览器
			while(str.indexOf(subStr) >= 0)
				str = str.replace(subStr, replacement);
		}
		
		return str;
	};
	
	$.trimStr = function(str)
	{
		if(str == null)
			return str;
		
		if(str.trim !== undefined)
		{
			return str.trim();
		}
		else
		{
			//兼容旧版浏览器
			return str.replace(/^\s+|\s+$/gm, "");
		}
	};
	
	/**
	 * 获取/设置本地存储条目
	 */
	$.localStorageItem = function(name, value)
	{
		if(value === undefined)
		{
			if(window.localStorage && window.localStorage.getItem)
				return window.localStorage.getItem(name);
			else
				return undefined;
		}
		else
		{
			if(window.localStorage && window.localStorage.setItem)
			{
				window.localStorage.setItem(name, value);
				return true;
			}
			else
				return false;
		}
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
			
			//删除首尾空格，避免提示信息错行
			message = message.trim();
			
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
			justifyContent: "center",
			showVersion: false,
			showAuthor: false
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
			
			if(options.showVersion)
			{
				html += "<div class='plugin-version text-color-secondary'><small>"+(chartPlugin.version ? $.escapeHtml(chartPlugin.version) : "")+"</small></div>";
			}
			
			if(options.showAuthor)
			{
				html += "<div class='plugin-author text-color-secondary'><small>"+(chartPlugin.author ? $.escapeHtml(chartPlugin.author) : "")+"</small></div>";
			}
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
$.validator.addMethod("required", function(value, ele)
{
	ele = $(ele);
	
	if(ele.hasClass("validate-proxy"))
	{
		var reactiveFormModel = $(this.currentForm).data("reactiveFormModel");
		var name = ele.attr("name");
		
		if(reactiveFormModel && name)
			value = Vue.toRaw(reactiveFormModel[name]);
	}
	
	return !$.isEmptyValue(value, true, false);
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
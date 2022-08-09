<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#--
表单JS片段。

变量：
String action

-->
<#assign AbstractController=statics['org.datagear.web.controller.AbstractController']>
<#include "page_validation_msg.ftl">
<script>
(function(po)
{
	po.action = "${requestAction!''}";
	po.isAddAction = (po.action.indexOf("${AbstractController.REQUEST_ACTION_ADD}") == 0);
	po.isEditAction = (po.action.indexOf("${AbstractController.REQUEST_ACTION_EDIT}") == 0);
	po.isViewAction = (po.action.indexOf("${AbstractController.REQUEST_ACTION_VIEW}") == 0);
	po.isReadonlyAction = (po.isViewAction);
	po.submitAction = "${submitAction!'#'}";
	
	/*需实现，字符串、函数*/
	po.submitUrl = "#";
	
	po.form = function()
	{
		return po.element("form:first");
	};
	
	//获取/填充并返回vue表单模型，在vue页面中可以"fm.*"访问模型中的属性
	po.vueFormModel = function(obj)
	{
		return this.vueReactive("fm", obj);
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
		if(po.isViewAction || url == "#")
			return;
		
		var fm = po.vueFormModel();
		options = $.extend(true,
		{
			defaultSuccessCallback: true,
			closeAfterSubmit: true
		},
		options, { data: po.vueRaw(fm) });
		
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
})
(${pid});
</script>

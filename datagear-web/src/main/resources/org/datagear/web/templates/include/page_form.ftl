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
//操作，不允许为null
String action

-->
<#assign AbstractController=statics['org.datagear.web.controller.AbstractController']>
<script>
(function(po)
{
	po.action = "${requestAction!AbstractController.REQUEST_ACTION_QUERY}";
	po.isAddAction = (po.action == "${AbstractController.REQUEST_ACTION_ADD}");
	po.isEditAction = (po.action == "${AbstractController.REQUEST_ACTION_EDIT}");
	po.isViewAction = (po.action == "${AbstractController.REQUEST_ACTION_VIEW}");
	po.isReadonlyAction = (po.isViewAction);
	po.submitAction = "${submitAction!'#'}";
	
	po.form = function()
	{
		return po.element("form");
	};
	
	po.submitForm = function(url, options)
	{
		if(po.isViewAction)
			return;
		
		var pm = po.vuePageModel();
		options = $.extend(true, { closeAfterSubmit: true }, options, { data: po.vueRaw(pm) });
		
		var successHandlers = (options.success ? [].concat(options.success) : []);
		successHandlers.push(function(response)
		{
			if(po.defaultSubmitSuccessCallback)
				po.defaultSubmitSuccessCallback(response, options.closeAfterSubmit);
		});
		options.success = successHandlers;
		
		var action = { url: po.concatContextPath(url), options: options };
		po.inflateSubmitAction(action);
		
		var jsonSubmit = (action.options.contentType == null || action.options.contentType == $.CONTENT_TYPE_JSON);
		
		if(jsonSubmit)
			$.ajaxJson(action.url, action.options);
		else
			$.ajax(action.url, action.options);
		
		return false;
	};
	
	po.setupForm = function(data, submitUrl, options)
	{
		data = (data || {});
		submitUrl = (submitUrl || "#");
		options = (options || {});
		
		po.vueRef("isReadonlyAction", po.isReadonlyAction);
		
		var pm = po.vuePageModel(data);
		
		po.vueMounted(function()
		{
			po.initGlobalValidationMessagesIfNon();
			
			po.form().validateForm(pm,
			{
				submitHandler: function(form)
				{
					return po.submitForm(submitUrl, options);
				}
			});
		});
		
		return pm;
	};
	
	po.inflateSubmitAction = function(action)
	{
		//po.trimSubmitActionData(action);
	};
	
	//将action.options.data没有在表单项中出现的属性删除
	po.trimSubmitActionData = function(action)
	{
		var data = action.options.data;
		
		if(!$.isTypeObject(data))
			return;
		
		var form = po.form();
		var inputs = $("input[name],textarea[name],select[name]", form);
		var inputNames = {};
		inputs.each(function()
		{
			let name = $(this).attr("name");
			if(name)
				inputNames[name] = true;
		});
		
		var trimData = {};
		
		for(let p in data)
		{
			if(inputNames[p])
				trimData[p] = data[p];
		}
		
		action.options.data = trimData;
	};
	
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
	
	po.initGlobalValidationMessagesIfNon = function()
	{
		if(!po._initGlobalValidationMessages)
		{
			$.extend($.validator.messages, po.globalValidationMessages);
			po._initGlobalValidationMessages = true;
		}
	};
	
	po.globalValidationMessages =
	{
		required: "<@spring.message code='validation.required' />",
		number: "<@spring.message code='validation.number' />",
		digits: "<@spring.message code='validation.digits' />",
		equalTo: "<@spring.message code='validation.equalTo' />",
		min: $.validator.format("<@spring.message code='validation.min' />"),
		max: $.validator.format("<@spring.message code='validation.max' />"),
		maxlength: $.validator.format("<@spring.message code='validation.maxlength' />"),
		minlength: $.validator.format("<@spring.message code='validation.minlength' />"),
		integer: "<@spring.message code='validation.integer' />"
	};
})
(${pageId});
</script>

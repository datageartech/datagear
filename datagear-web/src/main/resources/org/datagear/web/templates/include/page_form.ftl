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
<script>
(function(po)
{
	po.action = "${requestAction!''}";
	po.isAddAction = (po.action.indexOf("${AbstractController.REQUEST_ACTION_ADD}") == 0);
	po.isEditAction = (po.action.indexOf("${AbstractController.REQUEST_ACTION_EDIT}") == 0);
	po.isViewAction = (po.action.indexOf("${AbstractController.REQUEST_ACTION_VIEW}") == 0);
	po.isReadonlyAction = (po.isViewAction);
	po.submitAction = "${submitAction!'#'}";
	
	/*需实现*/
	//po.submitUrl = function(){ return "..."; }
	//或
	//po.submitUrl = "...";
	po.submitUrl = "#";
	
	po.form = function()
	{
		return po.element("form");
	};
	
	po.setupForm = function(data, ajaxOptions, validateOptions)
	{
		data = (data || {});
		ajaxOptions = (ajaxOptions || {});
		validateOptions = (validateOptions || {});
		
		po.vueRef("action", po.action);
		po.vueRef("isAddAction", po.isAddAction);
		po.vueRef("isEditAction", po.isEditAction);
		po.vueRef("isViewAction", po.isViewAction);
		po.vueRef("isReadonlyAction", po.isReadonlyAction);
		
		var pm = po.vuePageModel(data);
		
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
			
			po.form().validateForm(pm, validateOptions);
		});
		
		return pm;
	};
	
	po.submitForm = function(url, options)
	{
		if(po.isViewAction || url == "#")
			return;
		
		var pm = po.vuePageModel();
		options = $.extend(true,
		{
			defaultSuccessCallback: true,
			closeAfterSubmit: true
		},
		options, { data: po.vueRaw(pm) });
		
		var successHandlers = (options.success ? [].concat(options.success) : []);
		successHandlers.push(function(response)
		{
			if(options.defaultSuccessCallback && po.defaultSubmitSuccessCallback)
				po.defaultSubmitSuccessCallback(response, options.closeAfterSubmit);
		});
		options.success = successHandlers;
		
		var action = { url: url, options: options };
		po.inflateSubmitAction(action, action.options.data);
		
		var jsonSubmit = (action.options.contentType == null || action.options.contentType == $.CONTENT_TYPE_JSON);
		
		if(jsonSubmit)
			po.ajaxJson(action.url, action.options);
		else
			po.ajax(action.url, action.options);
		
		return false;
	};
	
	po.inflateSubmitAction = function(action, data){};
	
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
	
	po.initValidationMessagesIfNon = function()
	{
		if(!po._initGlobalValidationMessages)
		{
			$.extend($.validator.messages, po.validationMessages);
			po._initGlobalValidationMessages = true;
		}
	};
	
	po.validationMessages =
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

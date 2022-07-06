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
<#assign PrimveVueTestController=statics['org.datagear.web.controller.PrimveVueTestController']>
<script>
(function(po)
{
	po.action = "${action!PrimveVueTestController.REQUEST_ACTION_QUERY}";
	po.isSaveAddAction = (po.action == "${PrimveVueTestController.REQUEST_ACTION_SAVE_ADD}");
	po.isSaveEditAction = (po.action == "${PrimveVueTestController.REQUEST_ACTION_SAVE_EDIT}");
	po.isSaveAction = (po.isSaveAddAction || po.isSaveEditAction || po.action == "${PrimveVueTestController.REQUEST_ACTION_SAVE}");
	po.isViewAction = (po.action == "${PrimveVueTestController.REQUEST_ACTION_SAVE}");
	
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
		
		$.ajaxJson(action.url, action.options);
		
		return false;
	};
	
	po.setupForm = function(data, submitUrl, options)
	{
		data = (data || {});
		submitUrl = (submitUrl || "#");
		options = (options || {});
		
		var pm = po.vuePageModel(data);
		
		po.vueMounted(function()
		{
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
	
	po.inflateSubmitAction = function(action){};
	
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
})
(${pageId});
</script>

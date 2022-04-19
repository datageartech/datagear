<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#--
表单页面JS片段。

依赖：
page_obj.ftl
-->
<script type="text/javascript">
(function(po)
{
	po.readonly = ("${(readonly!false)?string('true','false')}" == "true");
	
	po.form = function()
	{
		return this.elementOfId("${pageId}-form");
	};
	
	po.initFormBtns = function(parent)
	{
		parent = (parent == null ? po.element() : parent);
		$.initButtons(parent);
	};
	
	po.validateForm = function(options, form)
	{
		form = (form == null ? po.form() : form);
		
		if(!po.extendValidateMessages)
		{
			$.extend($.validator.messages, po.validateMessages);
			po.extendValidateMessages = true;
		}
		
		options = $.extend(
		{
			errorPlacement : function(error, element)
			{
				error.appendTo(element.closest(".form-item-value"));
			}
		},
		options);
		
		form.validate(options);
	};

	po.validateAjaxForm = function(validateOptions, ajaxOptions, form)
	{
		form = (form == null ? po.form() : form);
		
		validateOptions = $.extend(
		{
			submitHandler: function(form)
			{
				ajaxOptions = $.extend({ closeAfterSubmit: true }, ajaxOptions);
				
				var ajaxSuccess = [];
				if(ajaxOptions.success)
					ajaxSuccess = ajaxSuccess.concat(ajaxOptions.success);
				ajaxSuccess.push(function(response)
				{
					if(po.defaultSubmitSuccess)
						po.defaultSubmitSuccess(response, ajaxOptions.closeAfterSubmit);
				});
				ajaxOptions.success = ajaxSuccess;
				
				$(form).ajaxSubmit(ajaxOptions);
			}
		},
		validateOptions);
		
		po.validateForm(validateOptions, form);
	};
	
	po.validateAjaxJsonForm = function(validateOptions, ajaxOptions, form)
	{
		ajaxOptions = $.extend({jsonSubmit: true}, ajaxOptions);
		po.validateAjaxForm(validateOptions, ajaxOptions, form);
	};
	
	po.defaultSubmitSuccess = function(response, close)
	{
		close = (close == null ? true : close);
		
		if(this.refreshParent)
			this.refreshParent();
		
		var myClose = po.pageParamSubmitSuccess(response);
		
		if(myClose === false)
			return;
		
		if(close && !this.isDialogPinned())
			this.close();
	};
	
	po.pageParamSubmitSuccess = function(response)
	{
		this.pageParamCall("submitSuccess", (response.data ? response.data : response));
	};
	
	po.refreshParent = function()
	{
		var parent = po.parent();
		if(parent && parent.refresh && $.isFunction(parent.refresh))
		{
			try
			{
				parent.refresh();
			}
			catch(e){}
		}
	};
	
	po.validateMessages =
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

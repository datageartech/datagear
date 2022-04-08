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
page_js_obj.ftl
-->
<script type="text/javascript">
(function(po)
{
	po.form = function()
	{
		return this.elementOfId("${pageId}form");
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
	
	po.validateAjaxJsonForm = function(validateOptions, ajaxOptions, form)
	{
		form = (form == null ? po.form() : form);
		
		validateOptions = $.extend(
		{
			submitHandler: function(form)
			{
				ajaxOptions = $.extend({}, ajaxOptions);
				
				var ajaxSuccess = [];
				if(ajaxOptions.success)
					ajaxSuccess = ajaxSuccess.concat(ajaxOptions.success);
				ajaxSuccess.push(function(response)
				{
					po.afterSubmitSuccess(response);
				});
				ajaxOptions.success = ajaxSuccess;
				
				$(form).ajaxSubmitJson(ajaxOptions);
			}
		},
		validateOptions);
		
		po.validateForm(validateOptions);
	};
	
	po.afterSubmitSuccess = function(response)
	{
		po.pageParamCallAfterSave(true, response);
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
		maxlength: $.validator.format("<@spring.message code='validation.maxlength' />"),
		minlength: $.validator.format("<@spring.message code='validation.minlength' />"),
		integer: "<@spring.message code='validation.integer' />"
	};
})
(${pageId});
</script>

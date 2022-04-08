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
	
	po.form = function()
	{
		return this.element("#${pageId}-form");
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
	
	po.refreshParent = function()
	{
		var poParent = po.parent();
		if(poParent && poParent.refresh && $.isFunction(poParent.refresh))
		{
			try
			{
				poParent.refresh();
			}
			catch(e){}
		}
	};
})
(${pageId});
</script>

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
		return this.element("#${pageId}-form");
	};
	
	po.initButtons = function(parent)
	{
		parent = (parent == null ? po.element() : parent);
		$.initButtons(parent);
	};
	
	po.validateForm = function(options, form)
	{
		form = (form == null ? po.form() : form);
		
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

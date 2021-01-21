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
page_js_obj.jsp
-->
<script type="text/javascript">
(function(po)
{
	po.form = function()
	{
		return this.element("#${pageId}-form");
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

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
	
	/**
	 * 调用页面参数对象的"afterSave"函数。
	 * @param closeDefault 默认是否关闭
	 * @param arg... 可选，函数参数
	 */
	po.pageParamCallAfterSave = function(closeDefault, arg)
	{
		po.refreshParent();
		
		var close = po.pageParamApply("afterSave", $.makeArray(arguments).slice(1));
		if(close !== true && close !== false)
			close = closeDefault;
		
		if(close && !po.isDialogPinned())
			po.close();
		
		return close;
	};
})
(${pageId});
</script>

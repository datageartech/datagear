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
})
(${pageId});
</script>

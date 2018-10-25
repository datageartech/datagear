<%--
/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%--
查询表单JS片段。

依赖：
page_js_obj.jsp
page_obj_searchform_html.jsp

变量：
//查询回调函数，不允许为null，格式为：function(searchParam){}
pageObj.search = undefined;
--%>
<script type="text/javascript">
(function(pageObj)
{
	pageObj.searchForm = pageObj.element("#${pageId}-searchForm");
	
	pageObj.searchForm.submit(function()
	{
		var searchParam = pageObj.getSearchParam();
		pageObj.search(searchParam);
		
		return false;
	});

	pageObj.getSearchParam = function()
	{
		var param =
		{
			"keyword" : $.trim(pageObj.element("input[name='keyword']", pageObj.searchForm).val())
		};
		
		return param;
	};
	
	pageObj.element("input:submit", pageObj.searchForm).button();
})
(${pageId});
</script>

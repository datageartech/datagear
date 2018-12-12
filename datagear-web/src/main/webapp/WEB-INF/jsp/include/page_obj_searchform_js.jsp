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
po.search = undefined;
--%>
<script type="text/javascript">
(function(po)
{
	po.searchForm = function(){ return this.element("#${pageId}-searchForm"); };
	
	po.searchForm().submit(function()
	{
		var searchParam = po.getSearchParam();
		po.search(searchParam);
		
		return false;
	});

	po.getSearchParam = function()
	{
		var param =
		{
			"keyword" : $.trim(po.element("input[name='keyword']", po.searchForm()).val())
		};
		
		return param;
	};
	
	po.element("input:submit", po.searchForm()).button();
})
(${pageId});
</script>

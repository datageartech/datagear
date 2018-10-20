<%--
/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%--
分页片段。

依赖：
data_page_obj.jsp

变量：
//分页回调函数，不允许为null，格式为：function(pagingParam){}
pageObj.paging = undefined;
--%>
<script type="text/javascript">
(function(pageObj)
{
	pageObj.pagination = pageObj.element("#${pageId}-pagination");
	
	pageObj.getPagingParam = function()
	{
		var pagingParam =
		{
			"page" : pageObj.pagination.pagination("option", "page"),
			"pageSize" : pageObj.pagination.pagination("option", "pageSize")
		};
		
		return pagingParam;
	};
	
	pageObj.initPagination = function()
	{
		pageObj.pagination.pagination(
		{
			pageSizeSetLabel : "<fmt:message key='confirm' />",
			toPageLabel : "<fmt:message key='jumpto' />",
			pageSizeCookie: "<%=org.datagear.web.util.WebUtils.COOKIE_PAGINATION_SIZE%>",
			update: function(page, pageSize, total)
			{
				var pagingParam =
				{
					"page" : page,
					"pageSize" : pageSize
				};
				
				pageObj.paging(pagingParam);
				return false;
			}
		});
	};
	
	pageObj.refreshPagination = function(total, page, pageSize)
	{
		pageObj.pagination
		.pagination("option", "total", total)
		.pagination("option", "pageSize", pageSize)
		.pagination("option", "page", page)
		.pagination("refresh");
	};
})
(${pageId});
</script>

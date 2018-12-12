<%--
/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="org.datagear.web.util.WebUtils" %>
<%--
表单页面JS片段。

依赖：
page_js_obj.jsp
--%>
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

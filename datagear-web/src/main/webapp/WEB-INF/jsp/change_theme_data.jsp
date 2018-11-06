<%--
/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */
--%>
<%@ page language="java" contentType="application/json; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="include/jsp_jstl.jsp" %>
[
	{
		"selector" : "#css_jquery_ui",
		"attr" : "href",
		"value" : "<%=request.getContextPath()%>/static/theme/<spring:theme code='theme' />/jquery-ui-1.12.1/jquery-ui.css"
	},
	{
		"selector" : "#css_jquery_ui_theme",
		"attr" : "href",
		"value" : "<%=request.getContextPath()%>/static/theme/<spring:theme code='theme' />/jquery-ui-1.12.1/jquery-ui.theme.css"
	},
	{
		"selector" : "#css_common",
		"attr" : "href",
		"value" : "<%=request.getContextPath()%>/static/theme/<spring:theme code='theme' />/common.css"
	}
]
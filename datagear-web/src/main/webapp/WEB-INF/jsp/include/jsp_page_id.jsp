<%--
/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */
--%>
<%@ page language="java" pageEncoding="UTF-8" %>
<%
//用于为页面元素定义ID
String pageId = "p" + Long.toHexString(System.currentTimeMillis());
request.setAttribute("pageId", pageId);
%>
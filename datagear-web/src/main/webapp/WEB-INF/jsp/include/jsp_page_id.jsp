<%--
/*
 * Copyright (c) 2018 by datagear.org.
 */
--%>
<%@ page language="java" pageEncoding="UTF-8" %>
<%
//用于为页面元素定义ID
String pageId = "p" + Long.toHexString(System.currentTimeMillis());
request.setAttribute("pageId", pageId);
%>
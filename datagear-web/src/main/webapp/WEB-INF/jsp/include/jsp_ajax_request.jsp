<%--
/*
 * Copyright (c) 2018 by datagear.org.
 */
--%>
<%@ page language="java" pageEncoding="UTF-8" %>
<%
//是否ajax请求，jquery库ajax可以使用此方案判断
boolean ajaxRequest=(request.getHeader("x-requested-with") != null);
request.setAttribute("ajaxRequest", ajaxRequest);
%>
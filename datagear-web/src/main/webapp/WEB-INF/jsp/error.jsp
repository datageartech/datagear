<%--
/*
 * Copyright (c) 2018 by datagear.org.
 */
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="include/jsp_import.jsp" %>
<%@page import="org.springframework.web.context.support.WebApplicationContextUtils"%>
<%@page import="org.springframework.web.context.WebApplicationContext"%>
<%@ page import="org.datagear.web.OperationMessage"%>
<%@ page import="org.springframework.http.HttpStatus" %>
<%@ page import="java.io.Serializable"%>
<%@ include file="include/jsp_ajax_request.jsp" %>
<%@ include file="include/jsp_jstl.jsp" %>
<%@ include file="include/html_doctype.jsp" %>
<%
OperationMessage operationMessage = WebUtils.getOperationMessage(request);

if(operationMessage == null)
{
	WebApplicationContext webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(application);
	
	Integer statusCode = (Integer)request.getAttribute("javax.servlet.error.status_code");
	String message = (String)request.getAttribute("javax.servlet.error.message");
	Throwable throwable = (Throwable)request.getAttribute("javax.servlet.error.exception");
	
	String statusCodeKey = "error.httpError";
	
	if(statusCode != null)
	{
		int sc = statusCode.intValue();
		statusCodeKey += "." + sc;
	}
	
	try
	{
		message = webApplicationContext.getMessage(statusCodeKey, new Object[0], WebUtils.getLocale(request));
	}
	catch(Throwable t){}
	
	operationMessage = OperationMessage.valueOfFail(statusCodeKey, message);
	WebUtils.setOperationMessage(request, operationMessage);
}
%>
<html>
<head>
<%@ include file="include/html_head.jsp" %>
<title><fmt:message key='error.errorOccure' /><%@ include file="include/html_title_app_name.jsp" %></title>
</head>
<body>
<%@ include file="include/jsp_operation_message.jsp" %>
</body>
</html>
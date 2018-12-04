<%--
/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="include/jsp_import.jsp" %>
<%@ page import="org.springframework.web.context.support.WebApplicationContextUtils"%>
<%@ page import="org.springframework.web.context.WebApplicationContext"%>
<%@ page import="org.datagear.web.OperationMessage"%>
<%@ page import="org.datagear.web.util.DeliverContentTypeExceptionHandlerExceptionResolver"%>
<%@ page import="org.springframework.http.HttpStatus" %>
<%@ page import="java.io.Serializable"%>
<%@ include file="include/jsp_ajax_request.jsp" %>
<%@ include file="include/jsp_jstl.jsp" %>
<%@ include file="include/jsp_method_write_json.jsp" %>
<%
String expectedContentType = DeliverContentTypeExceptionHandlerExceptionResolver.getHandlerContentType(request);
if(expectedContentType != null && !expectedContentType.isEmpty())
	response.setContentType(expectedContentType);

OperationMessage operationMessage = WebUtils.getOperationMessage(request);

if(operationMessage == null)
{
	WebApplicationContext webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(application);
	
	Integer statusCode = (Integer)request.getAttribute("javax.servlet.error.status_code");

	if(statusCode == null)
		statusCode = response.getStatus();
	
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

boolean jsonResponse = WebUtils.isJsonResponse(response);
if(jsonResponse)
{
	writeJson(application, out, operationMessage);
}
else
{
%>
<%@ include file="include/html_doctype.jsp" %>
<html>
<head>
<%@ include file="include/html_head.jsp" %>
<title><%@ include file="include/html_title_app_name.jsp" %><fmt:message key='error.errorOccure' /></title>
</head>
<body>
<%if(ajaxRequest){%>
<div class="operation-message <%=operationMessage.getType()%>">
	<div class="message">
		<%=operationMessage.getMessage()%>
	</div>
	<%if(operationMessage.hasDetail()){%>
	<div class="message-detail">
		<pre><%=operationMessage.getDetail()%></pre>
	</div>
	<%}%>
</div>
<%}else{%>
<div>
	<div class="main-page-head">
		<%@ include file="include/html_logo.jsp" %>
		<div class="toolbar">
			<a class="link" href="<c:url value="/" />"><fmt:message key='backToMainPage' /></a>
		</div>
	</div>
	<div class="page-error">
		<div class="operation-message <%=operationMessage.getType()%>">
			<div class="message">
				<%=operationMessage.getMessage()%>
			</div>
			<%if(operationMessage.hasDetail()){%>
			<div class="message-detail">
				<pre><%=operationMessage.getDetail()%></pre>
			</div>
			<%}%>
		</div>
	</div>
</div>
<%}%>
</body>
</html>
<%}%>
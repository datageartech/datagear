<%--
/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */
--%>
<%@ page language="java" pageEncoding="UTF-8" %>
<%@ page import="org.datagear.web.util.WebUtils"%>
<%@ page import="org.datagear.web.OperationMessage"%>
<%
boolean __jom_jsonResponse = WebUtils.isJsonResponse(response);
OperationMessage __jom_operationMessage = WebUtils.getOperationMessage(request);

if(__jom_jsonResponse)
{
	if(__jom_operationMessage == null)
	{
%>
		<%="{}"%>
<%
	}
	else
	{
		writeJson(application, out, __jom_operationMessage);
	}
}
else
{
%>
<div class="operation-message <%=(__jom_operationMessage == null ? "" : __jom_operationMessage.getType())%>">
	<div class="message">
		<%=(__jom_operationMessage == null ? "" : __jom_operationMessage.getMessage())%>
	</div>
	<%if(__jom_operationMessage != null && __jom_operationMessage.hasDetail()){%>
	<div class="message-detail">
		<pre><%=__jom_operationMessage.getDetail()%></pre>
	</div>
	<%}%>
</div>
<%}%>
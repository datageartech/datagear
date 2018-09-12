<%--
/*
 * Copyright (c) 2018 by datagear.org.
 */
--%>
<%@ page language="java" pageEncoding="UTF-8" %>
<%@ page import="org.datagear.web.util.WebUtils"%>
<%@ page import="org.datagear.web.OperationMessage"%>
<%
OperationMessage __operationMessage = WebUtils.getOperationMessage(request);
if(__operationMessage != null){
%>
<div class="operation-message <%=__operationMessage.getType()%>">
	<div class="message">
		<%=__operationMessage.getMessage()%>
	</div>
	<%if(__operationMessage.hasThrowableTrace()){%>
	<div class="throwable">
		<pre><%=__operationMessage.getThrowableTrace()%></pre>
	</div>
	<%}%>
</div>
<%}%>
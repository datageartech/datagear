<%--
/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
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
	<%if(__operationMessage.hasDetail()){%>
	<div class="message-detail">
		<pre><%=__operationMessage.getDetail()%></pre>
	</div>
	<%}%>
</div>
<%}%>
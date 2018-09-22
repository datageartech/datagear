<%--
/*
 * Copyright (c) 2018 by datagear.org.
 */
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="include/jsp_import.jsp" %>
<%@ include file="include/jsp_ajax_request.jsp" %>
<%@ include file="include/jsp_jstl.jsp" %>
<%@ include file="include/jsp_page_id.jsp" %>
<%@ include file="include/html_doctype.jsp" %>
<%
String loginUrl = request.getContextPath() + "/login"; 
%>
<html>
<head>
<%@ include file="include/html_head.jsp" %>
<meta http-equiv="refresh" content="3;url=<%=loginUrl%>">
<title><%@ include file="include/html_title_app_name.jsp" %><fmt:message key='register.registerSuccess' /></title>
</head>
<body>
<div id="${pageId}">
	<div class="main-page-head">
		<%@ include file="include/html_logo.jsp" %>
		<div class="toolbar">
			<a class="link" href="<c:url value="/" />"><fmt:message key='backToMainPage' /></a>
		</div>
	</div>
	<div class="page-register-success">
		<div class="register-success-content">
			<fmt:message key='register.registerSuccessContent'>
				<fmt:param value='<%=loginUrl%>' />
			</fmt:message>
		</div>
	</div>
</div>
</body>
</html>
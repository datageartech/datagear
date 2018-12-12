<%--
/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="include/jsp_import.jsp" %>
<%@ include file="include/jsp_ajax_request.jsp" %>
<%@ include file="include/jsp_jstl.jsp" %>
<%@ include file="include/jsp_page_id.jsp" %>
<%@ include file="include/html_doctype.jsp" %>
<html>
<head>
<%@ include file="include/html_head.jsp" %>
<title><%@ include file="include/html_title_app_name.jsp" %><fmt:message key='about.about' /></title>
</head>
<body>
<div id="${pageId}" class="page page-about">
	<form id="${pageId}-form">
		<div class="form-content">
			<div class="form-item">
				<div class="form-item-label">
					<label><fmt:message key='about.app.name' /></label>
				</div>
				<div class="form-item-value">
					<fmt:message key='app.name' />
				</div>
			</div>
			<div class="form-item">
				<div class="form-item-label">
					<label><fmt:message key='about.app.version' /></label>
				</div>
				<div class="form-item-value">
					<c:out value='${version}' />
				</div>
			</div>
			<div class="form-item">
				<div class="form-item-label">
					<label><fmt:message key='about.app.website' /></label>
				</div>
				<div class="form-item-value">
					<a href="http://www.datagear.tech" target="_blank" class="link">http://www.datagear.tech</a>
				</div>
			</div>
		</div>
	</form>
</div>
<%@ include file="include/page_js_obj.jsp" %>
<%@ include file="include/page_obj_form.jsp" %>
<script type="text/javascript">
(function(po)
{
})
(${pageId});
</script>
</body>
</html>
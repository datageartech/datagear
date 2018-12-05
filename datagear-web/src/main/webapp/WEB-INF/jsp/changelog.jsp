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
<title><%@ include file="include/html_title_app_name.jsp" %><fmt:message key='changelog.changelog' /></title>
</head>
<body>
<div id="${pageId}">
	<%if(!ajaxRequest){%>
	<div class="main-page-head">
		<%@ include file="include/html_logo.jsp" %>
	</div>
	<%}%>
	<div class="page page-changelog">
		<form id="${pageId}-form">
			<div class="form-content">
				<c:forEach var='versionChangelog' items='${versionChangelogs}'>
				<div class="form-item form-item-version">
					<div class="form-item-label">
						<label><fmt:message key='changelog.version' /></label>
					</div>
					<div class="form-item-value">
						<c:out value='${versionChangelog.version}' />
					</div>
				</div>
				<div class="form-item">
					<div class="form-item-label">
						<label></label>
					</div>
					<div class="form-item-value">
						<ul class="changelog-content">
						<c:forEach var='item' items='${versionChangelog.contents}'>
						<li class="changelog-item"><c:out value='${item}' /></li>
						</c:forEach>
						</ul>
					</div>
				</div>
				</c:forEach>
			</div>
			<div class="form-foot">
				<c:if test='${allListed == null || allListed == false}'>
				<a href="<c:url value='/changelogs' />" class="link" target="_blank"><fmt:message key='changelog.viewAll' /></a>
				</c:if>
			</div>
		</form>
	</div>
</div>
<%@ include file="include/page_js_obj.jsp" %>
<script type="text/javascript">
(function(pageObj)
{
	pageObj.form = pageObj.element("#${pageId}-form");
	
	if($.isInDialog(pageObj.form))
	{
		var windowHeight = $(window).height();
		var maxHeight = windowHeight - windowHeight/4;
		pageObj.element(".form-content").css("max-height", maxHeight+"px").css("overflow", "auto");
	}
})
(${pageId});
</script>
</body>
</html>
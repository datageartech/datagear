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
<%@ include file="include/jsp_method_get_string_value.jsp" %>
<%@ include file="include/html_doctype.jsp" %>
<html style="height:100%;">
<head>
<%@ include file="include/html_head.jsp" %>
<title><%@ include file="include/html_title_app_name.jsp" %><fmt:message key='resetPasswordRequestHistory.resetPasswordRequestHistory' /></title>
</head>
<body style="height:100%;">
<%if(!ajaxRequest){%>
<div style="height:99%;">
<%}%>
<div id="${pageId}" class="page-data-grid page-data-grid-reset-password-request-history">
	<div class="head">
		<div class="search">
			<%@ include file="include/page_obj_searchform.html.jsp" %>
		</div>
		<div class="operation">
		</div>
	</div>
	<div class="content">
		<table id="${pageId}-table" width="100%" class="hover stripe">
		</table>
	</div>
	<div class="foot">
		<div id="${pageId}-pagination"></div>
	</div>
</div>
<%if(!ajaxRequest){%>
</div>
<%}%>
<%@ include file="include/page_js_obj.jsp" %>
<%@ include file="include/page_obj_searchform_js.jsp" %>
<%@ include file="include/page_obj_pagination.jsp" %>
<%@ include file="include/page_obj_grid.jsp" %>
<script type="text/javascript">
(function(pageObj)
{
	pageObj.url = function(action)
	{
		return contextPath + "/resetPasswordRequestHistory/" + action;
	};
	
	pageObj.buildTableColumValueOption = function(title, data)
	{
		var option =
		{
			title : title,
			data : data,
			render: function(data, type, row, meta)
			{
				return data;
			},
			defaultContent: "",
		};
		
		return option;
	};
	
	var tableColumns = [
		pageObj.buildTableColumValueOption("<fmt:message key='resetPasswordRequestHistory.time' />", "resetPasswordRequest.time"),
		pageObj.buildTableColumValueOption("<fmt:message key='resetPasswordRequestHistory.principal' />", "resetPasswordRequest.principal"),
		pageObj.buildTableColumValueOption("<fmt:message key='resetPasswordRequestHistory.username' />", "resetPasswordRequest.user.name"),
		pageObj.buildTableColumValueOption("<fmt:message key='resetPasswordRequestHistory.effectiveTime' />", "effectiveTime"),
	];
	
	pageObj.initPagination();
	
	var tableSettings = pageObj.buildDataTableSettingsAjax(tableColumns, pageObj.url("pagingQueryData"));
	tableSettings.order=[[1,"desc"]];
	pageObj.initDataTable(tableSettings);
})
(${pageId});
</script>
</body>
</html>

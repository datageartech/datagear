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
<%@ include file="include/jsp_method_get_string_value.jsp" %>
<%@ include file="include/html_doctype.jsp" %>
<html style="height:100%;">
<head>
<%@ include file="include/html_head.jsp" %>
<title><fmt:message key='resetPasswordRequestHistory.resetPasswordRequestHistory' /><%@ include file="include/html_title_app_name.jsp" %></title>
</head>
<body style="height:100%;">
<%if(!ajaxRequest){%>
<div style="height:99%;">
<%}%>
<div id="${pageId}" class="page-data-grid page-data-grid-reset-password-request-history">
	<div class="head">
		<div class="search">
			<form id="${pageId}-searchForm" class="search-form" action="#">
				<div class="ui-widget ui-widget-content keyword-widget"><input name="keyword" type="text" class="ui-widget ui-widget-content input-keyword" /></div>
				<input name="submit" type="submit" value="<fmt:message key='query' />" />
			</form>
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
<%@ include file="include/page_obj_pagination.jsp" %>
<%@ include file="include/page_obj_grid.jsp" %>
<script type="text/javascript">
(function(pageObj)
{
	$("input:submit, input:button, input:reset, button", pageObj.element(".head")).button();
	
	pageObj.url = function(action)
	{
		return contextPath + "/resetPasswordRequestHistory/" + action;
	};
	
	pageObj.searchForm = pageObj.element(".search-form");
	pageObj.searchForm.submit(function()
	{
		var searchParam = pageObj.getSearchParam();
		
		pageObj.search(searchParam);
		
		return false;
	});
	
	pageObj.search = function(searchParam)
	{
		pageObj.refresh(searchParam, null, null);
	};
	
	pageObj.paging = function(pagingParam)
	{
		pageObj.refresh(null, pagingParam, null);
		return false;
	};
	
	pageObj.sort = function(order)
	{
		pageObj.refresh(null, null, order);
	};
	
	pageObj.refresh = function(searchParam, pagingParam, order)
	{
		if(!searchParam)
			searchParam = pageObj.getSearchParam();
		if(!pagingParam)
			pagingParam = pageObj.getPagingParam();
		if(!order)
			order = pageObj.getOrderTyped();
		
		var url = pageObj.url("pagingQueryData");
		
		var param = {};
		
		$.extend(param, searchParam);
		$.extend(param, pagingParam);
		$.extend(param, { "order" : order });
		
		$.getJSONOnPost(url, param, function(pagingData)
		{
			pageObj.setPagingData(pagingData);
		});
	};
	
	pageObj.getSearchParam = function()
	{
		var param =
		{
			"keyword" : $.trim(pageObj.element("input[name='keyword']", pageObj.searchForm).val())
		};
		
		return param;
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
	var tableSettings = pageObj.getTableSettings(tableColumns);
	tableSettings.order=[[0,"desc"]];
	pageObj.initTable(tableSettings);
	pageObj.initPagination();
	pageObj.refresh();
})
(${pageId});
</script>
</body>
</html>

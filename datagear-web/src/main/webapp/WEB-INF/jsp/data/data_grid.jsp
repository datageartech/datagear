<%--
/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="org.datagear.web.vo.PropertyPathDisplayName"%>
<%@ include file="../include/jsp_import.jsp" %>
<%@ include file="../include/jsp_ajax_request.jsp" %>
<%@ include file="../include/jsp_jstl.jsp" %>
<%@ include file="../include/jsp_page_id.jsp" %>
<%@ include file="../include/jsp_method_get_string_value.jsp" %>
<%@ include file="../include/jsp_method_write_json.jsp" %>
<%@ include file="include/data_jsp_define.jsp" %>
<%@ include file="../include/html_doctype.jsp" %>
<%
//是否只读操作，允许为null
boolean readonly = ("true".equalsIgnoreCase(getStringValue(request, "readonly")));
//可用的查询条件列表，不允许为null
List<PropertyPathDisplayName> conditionSource = (List<PropertyPathDisplayName>)request.getAttribute("conditionSource");
%>
<html style="height:100%;">
<head>
<%@ include file="../include/html_head.jsp" %>
<title>
	<%@ include file="../include/html_title_app_name.jsp" %>
	<fmt:message key='query' />
	<fmt:message key='titleSeparator' />
	<%=WebUtils.escapeHtml(ModelUtils.displayName(model, WebUtils.getLocale(request)))%>
	<fmt:message key='bracketLeft' />
	<%=WebUtils.escapeHtml(schema.getTitle())%>
	<fmt:message key='bracketRight' />
</title>
</head>
<body style="height:100%;">
<%if(!ajaxRequest){%>
<div style="height:99%;">
<%}%>
<div id="${pageId}" class="page-data-grid page-data-grid-query">
	<div class="head">
		<div class="search">
			<%@ include file="include/data_page_obj_searchform_html.jsp" %>
		</div>
		<div class="operation">
			<%if(readonly){%>
				<input name="viewButton" type="button" value="<fmt:message key='view' />" />
			<%}else{%>
				<input name="addButton" type="button" value="<fmt:message key='add' />" />
				<input name="editButton" type="button" value="<fmt:message key='edit' />" />
				<input name="viewButton" type="button" value="<fmt:message key='view' />" />
				<input name="deleteButton" type="button" value="<fmt:message key='delete' />" />
			<%}%>
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
<%@ include file="include/data_page_obj.jsp" %>
<%@ include file="include/data_page_obj_searchform_js.jsp" %>
<%@ include file="../include/page_obj_pagination.jsp" %>
<%@ include file="include/data_page_obj_grid.jsp" %>
<script type="text/javascript">
(function(pageObj)
{
	pageObj.conditionSource = <%writeJson(application, out, conditionSource);%>;
	
	$.initButtons(pageObj.element(".operation"));
	
	pageObj.onModel(function(model)
	{
		<%if(!readonly){%>
			pageObj.element("input[name=addButton]").click(function()
			{
				pageObj.open(pageObj.url("", "add", "batchSet=true"), { pinTitleButton : true });
			});
			
			pageObj.element("input[name=editButton]").click(function()
			{
				pageObj.executeOnSelect(function(row)
				{
					var data = {"data" : row};
					
					pageObj.open(pageObj.url("edit"),
					{
						data : data,
						pinTitleButton : true
					});
				});
			});
		<%}%>

		pageObj.element("input[name=viewButton]").click(function()
		{
			pageObj.executeOnSelect(function(row)
			{
				var data = {"data" : row};
				
				pageObj.open(pageObj.url("view"),
				{
					data : data
				});
			});
		});
		
		<%if(!readonly){%>
			pageObj.element("input[name=deleteButton]").click(function()
			{
				pageObj.executeOnSelects(function(rows)
				{
					pageObj.confirm("<fmt:message key='confirmDelete' />",
					{
						"confirm" : function()
						{
							var data = {"data" : rows};
							
							$.post(pageObj.url("delete"), data, function()
							{
								pageObj.refresh();
							});
						}
					});
				});
			});
		<%}%>
		
		pageObj.conditionAutocompleteSource = $.buildSearchConditionAutocompleteSource(pageObj.conditionSource);
		pageObj.initConditionPanel();
		pageObj.initPagination();
		pageObj.initModelDataTableAjax(pageObj.url("queryData"), model);
	});
})
(${pageId});
</script>
</body>
</html>

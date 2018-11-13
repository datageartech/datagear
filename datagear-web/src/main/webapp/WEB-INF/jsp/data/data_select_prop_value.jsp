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
//初始数据，允许null
Object data = request.getAttribute("data");
//属性名称，不允许null
String propertyPath = request.getParameter("propertyPath");

PropertyPath propertyPathObj = ModelUtils.toPropertyPath(propertyPath);
PropertyPathInfo propertyPathInfoObj = ModelUtils.toPropertyPathInfoConcrete(model, propertyPathObj, data);
//可用的查询条件列表，不允许为null
List<PropertyPathDisplayName> conditionSource = (List<PropertyPathDisplayName>)request.getAttribute("conditionSource");

boolean isMultipleSelect = false;
if(request.getParameter("multiple") != null)
	isMultipleSelect = true;
else
	isMultipleSelect = MU.isMultipleProperty(propertyPathInfoObj.getPropertyTail());
%>
<html style="height:100%;">
<head>
<%@ include file="../include/html_head.jsp" %>
<title>
	<%@ include file="../include/html_title_app_name.jsp" %>
	<fmt:message key='select' /><fmt:message key='titleSeparator' />
	<%=WebUtils.escapeHtml(ModelUtils.displayName(model, propertyPathObj, WebUtils.getLocale(request)))%>
</title>
</head>
<body style="height:100%;">
<%if(!ajaxRequest){%>
<div style="height:99%;">
<%}%>
<div id="${pageId}" class="page-data-grid page-data-grid-spv">
	<div class="head">
		<div class="search">
			<%@ include file="include/data_page_obj_searchform_html.jsp" %>
		</div>
		<div class="operation">
			<input name="confirmButton" type="button" class="recommended" value="<fmt:message key='confirm' />" />
			<input name="addButton" type="button" value="<fmt:message key='add' />" />
			<input name="editButton" type="button" value="<fmt:message key='edit' />" />
			<input name="viewButton" type="button" value="<fmt:message key='view' />" />
		</div>
	</div>
	<div class="content">
		<table id="${pageId}-table" style="width:100%;" class="hover stripe">
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
	pageObj.data = $.unref(<%writeJson(application, out, data);%>);
	pageObj.propertyPath = "<%=WebUtils.escapeJavaScriptStringValue(propertyPath)%>";
	pageObj.isMultipleSelect = <%=isMultipleSelect%>;
	pageObj.conditionSource = <%writeJson(application, out, conditionSource);%>;
	
	$.initButtons(pageObj.element(".operation"));
	
	pageObj.onModel(function(model)
	{
		var propertyInfo = $.model.getTailPropertyInfoConcrete(model, pageObj.propertyPath);
		var property = propertyInfo.property;
		var propertyModel = propertyInfo.model;
		
		pageObj.mappedByWith = $.model.findMappedByWith(property, propertyModel);

		pageObj.dataTableAjaxParamParent = pageObj.dataTableAjaxParam;
		
		pageObj.dataTableAjaxParam = function()
		{
			var param = pageObj.dataTableAjaxParamParent();
			
			$.extend(param, 
			{
				"data" : pageObj.data,
				"propertyPath" : pageObj.propertyPath
			});
			
			return param;
		};
		
		pageObj.element("input[name=confirmButton]").click(function()
		{
			if(pageObj.isMultipleSelect)
			{
				pageObj.executeOnSelects(function(rows)
				{
					var pageParam = pageObj.pageParam();
					
					var close = (pageParam && pageParam.submit ? pageParam.submit(rows) : undefined);
					
					if(close == undefined)
						close = true;
					
					if(close && !$.isDialogPinned($.getInDialog(pageObj.element())))
						pageObj.close();
				});
			}
			else
			{
				pageObj.executeOnSelect(function(row)
				{
					var pageParam = pageObj.pageParam();
					
					var close = (pageParam && pageParam.submit ? pageParam.submit(row) : undefined);
					
					if(close == undefined)
						close = true;
					
					if(close && !$.isDialogPinned($.getInDialog(pageObj.element())))
						pageObj.close();
				});
			}
		});
		
		pageObj.element("input[name=addButton]").click(function()
		{
			var options =
			{
				"data" : { "ignorePropertyName" : $.model.findMappedByWith(property, propertyModel) },
				"pageParam" :
				{
					"afterSave" : function(data)
					{
						var pageParam = pageObj.pageParam();
						
						var close = (pageParam && pageParam.submit ? pageParam.submit(data) : undefined);
						
						//单选默认关闭，多选默认不关闭
						if(close == undefined)
							close = (pageObj.isMultipleSelect ? false : true);
						
						if(close)
							pageObj.close();
					}
				}
			}
			
			pageObj.open(pageObj.url(propertyModel.name, "add"), options);
		});
		
		pageObj.element("input[name=editButton]").click(function()
		{
			pageObj.executeOnSelect(function(row)
			{
				var data = {"data" : row};
				
				pageObj.open(pageObj.url(propertyModel.name, "edit"),
				{
					data : data
				});
			});
		});
		
		pageObj.element("input[name=viewButton]").click(function()
		{
			pageObj.executeOnSelect(function(row)
			{
				var data = {"data" : row};
				
				pageObj.open(pageObj.url(propertyModel.name, "view"),
				{
					data : data
				});
			});
		});
		
		pageObj.conditionAutocompleteSource = $.buildSearchConditionAutocompleteSource(pageObj.conditionSource);
		pageObj.initConditionPanel();
		pageObj.initPagination();
		pageObj.initModelDataTableAjax(pageObj.url("selectPropValueData"), propertyModel);
	});
})
(${pageId});
</script>
</body>
</html>

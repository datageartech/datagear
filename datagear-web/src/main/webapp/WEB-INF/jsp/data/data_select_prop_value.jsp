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
<div id="${pageId}" class="page-grid page-grid-spv">
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
		<div class="pagination-wrapper">
			<div id="${pageId}-pagination" class="pagination"></div>
		</div>
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
(function(po)
{
	po.data = $.unref(<%writeJson(application, out, data);%>);
	po.propertyPath = "<%=WebUtils.escapeJavaScriptStringValue(propertyPath)%>";
	po.isMultipleSelect = <%=isMultipleSelect%>;
	po.conditionSource = <%writeJson(application, out, conditionSource);%>;
	
	$.initButtons(po.element(".operation"));
	
	po.onModel(function(model)
	{
		var propertyInfo = $.model.getTailPropertyInfoConcrete(model, po.propertyPath);
		var property = propertyInfo.property;
		var propertyModel = propertyInfo.model;
		var propertyModelTableName = $.model.featureTableName(propertyModel);
		
		po.mappedByWith = $.model.findMappedByWith(property, propertyModel);

		po.dataTableAjaxParamParent = po.dataTableAjaxParam;
		po.dataTableAjaxParam = function()
		{
			var param = po.dataTableAjaxParamParent();
			
			$.extend(param, 
			{
				"data" : po.data,
				"propertyPath" : po.propertyPath
			});
			
			return param;
		};
		
		po.element("input[name=confirmButton]").click(function()
		{
			if(po.isMultipleSelect)
			{
				po.executeOnSelects(function(rows)
				{
					var close = po.pageParamCall("submit", rows);
					
					if(close == undefined)
						close = true;
					
					if(close && !$.isDialogPinned($.getInDialog(po.element())))
						po.close();
				});
			}
			else
			{
				po.executeOnSelect(function(row)
				{
					var close = po.pageParamCall("submit", row);
					
					if(close == undefined)
						close = true;
					
					if(close && !$.isDialogPinned($.getInDialog(po.element())))
						po.close();
				});
			}
		});
		
		po.element("input[name=addButton]").click(function()
		{
			var options =
			{
				"data" : { "ignorePropertyName" : $.model.findMappedByWith(property, propertyModel) },
				"pageParam" :
				{
					"afterSave" : function(data)
					{
						var close = po.pageParamCall("submit", data);
							
						//单选默认关闭，多选默认不关闭
						if(close == undefined)
							close = (po.isMultipleSelect ? false : true);
						
						if(close)
							po.close();
					}
				}
			}
			
			po.open(po.url(propertyModelTableName, "add"), options);
		});
		
		po.element("input[name=editButton]").click(function()
		{
			po.executeOnSelect(function(row)
			{
				var data = {"data" : row};
				
				po.open(po.url(propertyModelTableName, "edit"),
				{
					data : data
				});
			});
		});
		
		po.element("input[name=viewButton]").click(function()
		{
			po.executeOnSelect(function(row)
			{
				var data = {"data" : row};
				
				po.open(po.url(propertyModelTableName, "view"),
				{
					data : data
				});
			});
		});
		
		po.conditionAutocompleteSource = $.buildSearchConditionAutocompleteSource(po.conditionSource);
		po.initConditionPanel();
		po.initPagination();
		po.initModelDataTableAjax(po.url("selectPropValueData"), propertyModel);
	});
})
(${pageId});
</script>
</body>
</html>

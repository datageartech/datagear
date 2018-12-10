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
String propertyPath = getStringValue(request, "propertyPath");
//标题操作标签I18N关键字，不允许null
String titleOperationMessageKey = getStringValue(request, "titleOperationMessageKey");
//是否是客户端操作，允许为null
boolean clientOperation = ("true".equalsIgnoreCase(getStringValue(request, "clientOperation")));
//是否只读操作，允许为null
boolean readonly = ("true".equalsIgnoreCase(getStringValue(request, "readonly")));
//可用的查询条件列表，不允许为null
List<PropertyPathDisplayName> conditionSource = (List<PropertyPathDisplayName>)request.getAttribute("conditionSource");

PropertyPath propertyPathObj = ModelUtils.toPropertyPath(propertyPath);
PropertyPathInfo propertyPathInfoObj = ModelUtils.toPropertyPathInfoConcrete(model, propertyPathObj, data);
boolean isPrivatePropertyModel = ModelUtils.isPrivatePropertyModelTail(propertyPathInfoObj);

%>
<html style="height:100%;">
<head>
<%@ include file="../include/html_head.jsp" %>
<title>
	<%@ include file="../include/html_title_app_name.jsp" %>
	<fmt:message key='<%=titleOperationMessageKey%>' />
	<fmt:message key='titleSeparator' />
	<%=WebUtils.escapeHtml(ModelUtils.displayName(model, propertyPathObj, WebUtils.getLocale(request)))%>
</title>
</head>
<body style="height:100%;">
<%if(!ajaxRequest){%>
<div style="height:99%;">
<%}%>
<div id="${pageId}" class="page-grid page-grid-empv">
	<div class="head">
		<div class="search">
			<%if(!clientOperation){%>
			<%@ include file="include/data_page_obj_searchform_html.jsp" %>
			<%}%>
		</div>
		<div class="operation">
			<%if(readonly){%>
				<input name="viewButton" type="button" value="<fmt:message key='view' />" />
			<%}else{%>
				<%if(isPrivatePropertyModel){%>
					<input name="addButton" type="button" value="<fmt:message key='add' />" />
					<input name="editButton" type="button" value="<fmt:message key='edit' />" />
				<%}else{%>
					<input name="selectButton" type="button" class="recommended" value="<fmt:message key='select' />" />
				<%}%>
				<input name="viewButton" type="button" value="<fmt:message key='view' />" />
				<input name="deleteButton" type="button" value="<fmt:message key='delete' />" />
			<%}%>
		</div>
	</div>
	<div class="content">
		<table id="${pageId}-table" style="width:100%;" class="hover stripe">
		</table>
	</div>
	<div class="foot foot-edit-grid">
		<%if(!readonly){%>
		<%@ include file="include/data_page_obj_edit_grid_html.jsp" %>
		<%}%>
		<div class="pagination-wrapper">
			<div id="${pageId}-pagination" class="pagination"></div>
		</div>
	</div>
</div>
<%if(!ajaxRequest){%>
</div>
<%}%>
<%@ include file="include/data_page_obj.jsp" %>
<%if(!clientOperation){%>
<%@ include file="include/data_page_obj_searchform_js.jsp" %>
<%@ include file="../include/page_obj_pagination.jsp" %>
<%}%>
<%@ include file="include/data_page_obj_grid.jsp" %>
<%if(!readonly){%>
<%@ include file="include/data_page_obj_edit_grid_js.jsp" %>
<%}%>
<script type="text/javascript">
(function(pageObj)
{
	pageObj.readonly = <%=readonly%>;
	pageObj.data = $.unref(<%writeJson(application, out, data);%>);
	pageObj.propertyPath = "<%=WebUtils.escapeJavaScriptStringValue(propertyPath)%>";
	pageObj.clientOperation = <%=clientOperation%>;
	
	<%if(!clientOperation){%>
	pageObj.conditionSource = <%writeJson(application, out, conditionSource);%>;
	<%}%>
	
	$.initButtons(pageObj.element(".operation"));
	
	pageObj.buildActionOptions = function(property, propertyConcreteModel, extraRequestParams, extraPageParams)
	{
		var requestParams = { "data" : pageObj.data, "propertyPath" : pageObj.propertyPath, "clientOperation" : pageObj.clientOperation };
		if(extraRequestParams)
			$.extend(requestParams, extraRequestParams);
		
		var pageParams = (extraPageParams || {});
		
		var actionParam =
		{
			"data" : requestParams,
			"pageParam" : pageParams
		}
		
		return actionParam;
	};
	
	pageObj.restoreGridData = function()
	{
		var rowsData = pageObj.getRowsData();
		
		$.model.propertyPathValue(pageObj.data, pageObj.propertyPath, rowsData);
		
		var pageParam = pageObj.pageParam();
		
		if(pageParam && pageParam.submit)
			pageParam.submit(rowsData);
	};
	
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
		
		pageObj.element("input[name=viewButton]").click(function()
		{
			pageObj.executeOnSelect(function(row, index)
			{
				var options = undefined;
				
				if(pageObj.clientOperation)
				{
					options = pageObj.buildActionOptions(property, propertyModel,
							{
								"propertyPath" : $.propertyPath.concatElementIndex(pageObj.propertyPath, index)
							});
					
					pageObj.open(pageObj.url("viewMultiplePropValueElement"), options);
				}
				else
				{
					$.model.propertyPathValue(pageObj.data, pageObj.propertyPath, [ row ]);
					var propertyPath = $.propertyPath.concatElementIndex(pageObj.propertyPath, 0);
					
					options = pageObj.buildActionOptions(property, propertyModel,
							{
								"propertyPath" : propertyPath
							});
					
					pageObj.open(pageObj.url("viewMultiplePropValueElement"), options);
					
					$.model.propertyPathValue(pageObj.data, pageObj.propertyPath, []);
				}
			});
		});
		
		<%if(!readonly){%>
			pageObj.addMultiplePropValueElement = function()
			{
				var url = undefined;
				var options = undefined;
				
				if(pageObj.clientOperation)
				{
					url = pageObj.url("addMultiplePropValueElement");
					
					var index = pageObj.table.DataTable().rows().data().length;
					
					options = pageObj.buildActionOptions(property, propertyModel,
							{
								"propertyPath" : $.propertyPath.concatElementIndex(pageObj.propertyPath, index)
							},
							{
								"submit" : function(propValueElement)
								{
									pageObj.addRowData(propValueElement);
									pageObj.restoreGridData();
									
									$.tipSuccess("<fmt:message key='haveAdd' />");
								}
							});
				}
				else
				{
					url = pageObj.url("", "addMultiplePropValueElement", "batchSet=true");
					
					options = pageObj.buildActionOptions(property, propertyModel,
							{
								"propertyPath" : $.propertyPath.concatElementIndex(pageObj.propertyPath, 0)
							},
							null);
				}
				
				options.pinTitleButton = true;
				
				pageObj.open(url, options);
			};
			
			<%if(isPrivatePropertyModel){%>
				pageObj.element("input[name=addButton]").click(function()
				{
					pageObj.addMultiplePropValueElement();
				});
			<%}else{%>
				pageObj.element("input[name=selectButton]").click(function()
				{
					var options = pageObj.buildActionOptions(property, propertyModel, null, 
							{
								"submit" : function(rows)
								{
									if(pageObj.clientOperation)
									{
										pageObj.addRowData(rows);
										pageObj.restoreGridData();
										
										$.tipSuccess("<fmt:message key='haveAdd' />");
									}
									else
									{
										var param = { "data" : pageObj.data, "propertyPath" : pageObj.propertyPath, "propValueElements" : rows };
										
										$.post(pageObj.url("saveAddMultiplePropValueElements"), param, function()
										{
											pageObj.refresh();
										});
									}
								}
							});
		
					$.setGridPageHeightOption(options);
					options.pinTitleButton = true;
					pageObj.open(pageObj.url("selectPropValue")+"?multiple", options);
				});
			<%}%>
			
			pageObj.element("input[name=editButton]").click(function()
			{
				pageObj.executeOnSelect(function(row, index)
				{
					var options = undefined;
					
					if(pageObj.clientOperation)
					{
						options = pageObj.buildActionOptions(property, propertyModel,
								{
									"propertyPath" : $.propertyPath.concatElementIndex(pageObj.propertyPath, index)
								},
								{
									"submit" : function(propValueElement)
									{
										pageObj.setRowData(index, propValueElement);
										pageObj.restoreGridData();
									}
								});
						
						options.pinTitleButton = true;
						
						pageObj.open(pageObj.url("editMultiplePropValueElement"), options);
					}
					else
					{
						$.model.propertyPathValue(pageObj.data, pageObj.propertyPath, [ row ]);
						var propertyPath = $.propertyPath.concatElementIndex(pageObj.propertyPath, 0);
						
						options = pageObj.buildActionOptions(property, propertyModel,
								{
									"propertyPath" : propertyPath
								});
						
						options.pinTitleButton = true;
						
						pageObj.open(pageObj.url("editMultiplePropValueElement"), options);
						
						$.model.propertyPathValue(pageObj.data, pageObj.propertyPath, []);
					}
				});
			});
			
			pageObj.element("input[name=deleteButton]").click(
			function()
			{
				pageObj.executeOnSelects(function(rows, indexes)
				{
					pageObj.confirm("<fmt:message key='data.confirmDelete'><fmt:param>"+rows.length+"</fmt:param></fmt:message>",
					{
						"confirm" : function()
						{
							if(pageObj.clientOperation)
							{
								pageObj.deleteRow(indexes);
								pageObj.restoreGridData();
							}
							else
							{
								var options = pageObj.buildActionOptions(property, propertyModel, {"propValueElements" : rows}, null);
								
								pageObj.ajaxSubmitForHandleDuplication("deleteMultiplePropValueElements", options.data, "<fmt:message key='delete.continueIgnoreDuplicationTemplate' />",
								{
									"success" : function()
									{
										pageObj.refresh();
									}
								});
							}
						}
					});
				});
			});
		<%}%>
		
		<%if(clientOperation){%>
		pageObj.initModelDataTableLocal(propertyModel, $.model.propertyPathValue(pageObj.data, pageObj.propertyPath), pageObj.mappedByWith);
		<%}else{%>
		pageObj.conditionAutocompleteSource = $.buildSearchConditionAutocompleteSource(pageObj.conditionSource);
		pageObj.initConditionPanel();
		pageObj.initPagination();
		pageObj.initModelDataTableAjax(pageObj.url("queryMultiplePropValueData"), propertyModel, pageObj.mappedByWith);
		<%}%>
		
		<%if(!readonly){%>
		pageObj.initEditGrid();
		<%}%>
	});
})
(${pageId});
</script>
</body>
</html>

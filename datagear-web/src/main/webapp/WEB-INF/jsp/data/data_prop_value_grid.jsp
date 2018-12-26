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
//所有表格数据是否都是客户端数据，默认为false
boolean isClientGridData = ("true".equalsIgnoreCase(getStringValue(request, "isClientGridData")));
//标题操作标签I18N关键字，不允许null
String titleOperationMessageKey = getStringValue(request, "titleOperationMessageKey");
//是否只读操作，默认为false
boolean readonly = ("true".equalsIgnoreCase(getStringValue(request, "readonly")));
//可用的查询条件列表，isClientGridData为false时不允许为null
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
			<%if(!isClientGridData){%>
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
<%if(!isClientGridData){%>
<%@ include file="include/data_page_obj_searchform_js.jsp" %>
<%@ include file="../include/page_obj_pagination.jsp" %>
<%}%>
<%@ include file="include/data_page_obj_grid.jsp" %>
<%if(!readonly){%>
<%@ include file="include/data_page_obj_edit_grid_js.jsp" %>
<%}%>
<script type="text/javascript">
(function(po)
{
	po.readonly = <%=readonly%>;
	po.data = $.unref(<%writeJson(application, out, data);%>);
	po.propertyPath = "<%=WebUtils.escapeJavaScriptStringValue(propertyPath)%>";
	po.isClientGridData = <%=isClientGridData%>;
	
	<%if(!isClientGridData){%>
	po.conditionSource = <%writeJson(application, out, conditionSource);%>;
	<%}%>
	
	$.initButtons(po.element(".operation"));
	
	po.buildActionOptions = function(property, propertyConcreteModel, extraRequestParams, extraPageParams)
	{
		var requestParams =
		{
			"data" : po.data,
			"propertyPath" : po.propertyPath,
			"isClientGridData" : po.isClientGridData,
			"isClientFormData" : po.isClientGridData
		};
		
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
	
	po.storeGridData = function()
	{
		var rowsData = po.getRowsData();
		
		$.model.propertyPathValue(po.data, po.propertyPath, rowsData);
		
		po.pageParamCall("submit", rowsData);
	};
	
	po.onModel(function(model)
	{
		var propertyInfo = $.model.getTailPropertyInfoConcrete(model, po.propertyPath);
		var property = propertyInfo.property;
		var propertyModel = propertyInfo.model;
		
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
		
		po.element("input[name=viewButton]").click(function()
		{
			po.executeOnSelect(function(row, index)
			{
				var options = undefined;
				
				if(po.isClientGridData)
				{
					options = po.buildActionOptions(property, propertyModel,
							{
								"propertyPath" : $.propertyPath.concatElementIndex(po.propertyPath, index)
							});
					
					po.open(po.url("viewMultiplePropValueElement"), options);
				}
				else
				{
					$.model.propertyPathValue(po.data, po.propertyPath, [ row ]);
					var propertyPath = $.propertyPath.concatElementIndex(po.propertyPath, 0);
					
					options = po.buildActionOptions(property, propertyModel,
							{
								"propertyPath" : propertyPath
							});
					
					po.open(po.url("viewMultiplePropValueElement"), options);
					
					$.model.propertyPathValue(po.data, po.propertyPath, []);
				}
			});
		});
		
		<%if(!readonly){%>
			po.addMultiplePropValueElement = function()
			{
				var url = undefined;
				var options = undefined;
				
				if(po.isClientGridData)
				{
					url = po.url("addMultiplePropValueElement");
					
					var index = po.table().DataTable().rows().data().length;
					
					options = po.buildActionOptions(property, propertyModel,
							{
								"propertyPath" : $.propertyPath.concatElementIndex(po.propertyPath, index)
							},
							{
								"submit" : function(propValueElement)
								{
									po.addRowData(propValueElement);
									po.storeGridData();
									
									$.tipSuccess("<fmt:message key='haveAdd' />");
								}
							});
				}
				else
				{
					url = po.url("", "addMultiplePropValueElement", "batchSet=true");
					
					options = po.buildActionOptions(property, propertyModel,
							{
								"propertyPath" : $.propertyPath.concatElementIndex(po.propertyPath, 0)
							},
							null);
				}
				
				options.pinTitleButton = true;
				
				po.open(url, options);
			};
			
			<%if(isPrivatePropertyModel){%>
				po.element("input[name=addButton]").click(function()
				{
					po.addMultiplePropValueElement();
				});
			<%}else{%>
				po.element("input[name=selectButton]").click(function()
				{
					var options = po.buildActionOptions(property, propertyModel, null, 
							{
								"submit" : function(rows)
								{
									if(po.isClientGridData)
									{
										po.addRowData(rows);
										po.storeGridData();
										
										$.tipSuccess("<fmt:message key='haveAdd' />");
									}
									else
									{
										var param = { "data" : po.data, "propertyPath" : po.propertyPath, "propValueElements" : rows };
										
										$.post(po.url("saveAddMultiplePropValueElements"), param, function()
										{
											po.refresh();
										});
									}
								}
							});
		
					$.setGridPageHeightOption(options);
					options.pinTitleButton = true;
					po.open(po.url("selectPropValue")+"?multiple", options);
				});
			<%}%>
			
			po.element("input[name=editButton]").click(function()
			{
				po.executeOnSelect(function(row, index)
				{
					var options = undefined;
					
					if(po.isClientGridData)
					{
						options = po.buildActionOptions(property, propertyModel,
								{
									"propertyPath" : $.propertyPath.concatElementIndex(po.propertyPath, index)
								},
								{
									"submit" : function(propValueElement)
									{
										po.setRowData(index, propValueElement);
										po.storeGridData();
									}
								});
						
						options.pinTitleButton = true;
						
						po.open(po.url("editMultiplePropValueElement"), options);
					}
					else
					{
						$.model.propertyPathValue(po.data, po.propertyPath, [ row ]);
						var propertyPath = $.propertyPath.concatElementIndex(po.propertyPath, 0);
						
						options = po.buildActionOptions(property, propertyModel,
								{
									"propertyPath" : propertyPath
								});
						
						options.pinTitleButton = true;
						
						po.open(po.url("editMultiplePropValueElement"), options);
						
						$.model.propertyPathValue(po.data, po.propertyPath, []);
					}
				});
			});
			
			po.element("input[name=deleteButton]").click(
			function()
			{
				po.executeOnSelects(function(rows, indexes)
				{
					po.confirm("<fmt:message key='data.confirmDelete'><fmt:param>"+rows.length+"</fmt:param></fmt:message>",
					{
						"confirm" : function()
						{
							if(po.isClientGridData)
							{
								po.deleteRow(indexes);
								po.storeGridData();
							}
							else
							{
								var options = po.buildActionOptions(property, propertyModel, {"propValueElements" : rows}, null);
								
								po.ajaxSubmitForHandleDuplication("deleteMultiplePropValueElements", options.data, "<fmt:message key='delete.continueIgnoreDuplicationTemplate' />",
								{
									"success" : function()
									{
										po.refresh();
									}
								});
							}
						}
					});
				});
			});
		<%}%>
		
		<%if(isClientGridData){%>
		po.initModelDataTableLocal(propertyModel, $.model.propertyPathValue(po.data, po.propertyPath), po.mappedByWith);
		<%}else{%>
		po.conditionAutocompleteSource = $.buildSearchConditionAutocompleteSource(po.conditionSource);
		po.initConditionPanel();
		po.initPagination();
		po.initModelDataTableAjax(po.url("queryMultiplePropValueData"), propertyModel, po.mappedByWith);
		<%}%>
		
		<%if(!readonly){%>
		po.initEditGrid(propertyModel);
		<%}%>
	});
})
(${pageId});
</script>
</body>
</html>

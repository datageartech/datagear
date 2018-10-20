<%--
/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="org.datagear.web.vo.PropertyPathNameLabel"%>
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
String propName = getStringValue(request, "propName");
//标题操作标签I18N关键字，不允许null
String titleOperationMessageKey = getStringValue(request, "titleOperationMessageKey");
//是否是客户端操作，允许为null
boolean clientOperation = ("true".equalsIgnoreCase(getStringValue(request, "clientOperation")));
//是否只读操作，允许为null
boolean readonly = ("true".equalsIgnoreCase(getStringValue(request, "readonly")));
//可用的查询条件列表，不允许为null
List<PropertyPathNameLabel> conditionSource = (List<PropertyPathNameLabel>)request.getAttribute("conditionSource");

PropertyPath propertyPath = ModelUtils.toPropertyPath(propName);
PropertyPathInfo propertyPathInfo = ModelUtils.toPropertyPathInfoConcrete(model, propertyPath, data);
boolean isPrivatePropertyModel = ModelUtils.isPrivatePropertyModelTail(propertyPathInfo);

%>
<html style="height:100%;">
<head>
<%@ include file="../include/html_head.jsp" %>
<title><%@ include file="../include/html_title_app_name.jsp" %><fmt:message key='<%=titleOperationMessageKey%>' /><fmt:message key='titleSeparator' /><%=ModelUtils.getNameLabelValuePath(model, propertyPath, WebUtils.getLocale(request))%></title>
</head>
<body style="height:100%;">
<%if(!ajaxRequest){%>
<div style="height:99%;">
<%}%>
<div id="${pageId}" class="page-data-grid page-data-grid-empv">
	<div class="head">
		<div class="search">
			<%@ include file="include/data_page_obj_searchform_html.jsp" %>
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
	<div class="content <%if(clientOperation){%>content-hidden-foot<%}%>">
		<table id="${pageId}-table" style="width:100%;" class="hover stripe">
		</table>
	</div>
	<div class="foot <%if(clientOperation){%>hidden-foot<%}%>"">
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
	pageObj.readonly = <%=readonly%>;
	pageObj.data = $.unref(<%writeJson(application, out, data);%>);
	pageObj.propName = "<%=propName%>";
	pageObj.clientOperation = <%=clientOperation%>;
	pageObj.conditionSource = <%writeJson(application, out, conditionSource);%>;

	$("input:submit, input:button, input:reset, button", pageObj.element(".operation")).button();
	
	pageObj.buildActionOptions = function(property, propertyConcreteModel, extraRequestParams, extraPageParams)
	{
		var requestParams = { "data" : pageObj.data, "propName" : pageObj.propName, "clientOperation" : pageObj.clientOperation };
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
		
		$.model.propValue(pageObj.data, pageObj.propName, rowsData);
		
		var pageParam = pageObj.pageParam();
		
		if(pageParam && pageParam.submit)
			pageParam.submit(rowsData);
	};
	
	pageObj.onModel(function(model)
	{
		var propertyInfo = $.model.getTailPropertyInfoConcrete(model, pageObj.propName);
		var property = propertyInfo.property;
		var propertyModel = propertyInfo.model;
		
		pageObj.mappedByWith = $.model.findMappedByWith(property, propertyModel);
		
		pageObj.search = function(searchParam)
		{
			pageObj.refresh(searchParam, null);
		};
		
		pageObj.paging = function(pagingParam)
		{
			pageObj.refresh(null, pagingParam);
			return false;
		};

		pageObj.sort = function(order)
		{
			pageObj.refresh(null, null, order);
		};
		
		pageObj.refresh = function(searchParam, pagingParam, order)
		{
			//客户端操作不支持刷新
			if(pageObj.clientOperation)
				return;
			
			if(!searchParam)
				searchParam = pageObj.getSearchParam();
			if(!pagingParam)
				pagingParam = pageObj.getPagingParam();
			if(!order)
				order = pageObj.getOrderTyped();
			
			var url = pageObj.url("queryMultiplePropValueData");
			
			var param =
			{
				"data" : pageObj.data,
				"propName" : pageObj.propName
			};
			
			$.extend(param, searchParam);
			$.extend(param, pagingParam);
			$.extend(param, { "order" : order });
			
			$.getJSONOnPost(url, param, function(pagingData)
			{
				pageObj.setPagingData(pagingData);
			});
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
								"propName" : pageObj.propName + "["+index+"]"
							});
					
					pageObj.open(pageObj.url("viewMultiplePropValueElement"), options);
				}
				else
				{
					$.model.propValue(pageObj.data, pageObj.propName, [ row ]);
					var propName = pageObj.propName + "[0]";
					
					options = pageObj.buildActionOptions(property, propertyModel,
							{
								"propName" : propName
							});
					
					pageObj.open(pageObj.url("viewMultiplePropValueElement"), options);
					
					$.model.propValue(pageObj.data, pageObj.propName, []);
				}
			});
		});
		
		<%if(!readonly){%>
			pageObj.addMultiplePropValueElement = function()
			{
				var options = undefined;
				
				if(pageObj.clientOperation)
				{
					var index = pageObj.table.DataTable().rows().data().length;
					
					options = pageObj.buildActionOptions(property, propertyModel,
							{
								"propName" : pageObj.propName + "["+index+"]"
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
					options = pageObj.buildActionOptions(property, propertyModel,
							{
								"propName" : pageObj.propName + "[0]"
							},
							null);
				}
				
				options.pinTitleButton = true;
				
				pageObj.open(pageObj.url("addMultiplePropValueElement"), options);
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
										var param = { "data" : pageObj.data, "propName" : pageObj.propName, "propValueElements" : rows };
										
										$.post(pageObj.url("saveAddMultiplePropValueElements"), param, function()
										{
											pageObj.refresh();
										});
									}
								}
							});
		
					$.setGridPageHeightOption(options);
					options.pinTitleButton = true;
					pageObj.open(pageObj.url("selectPropValue?multiple"), options);
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
									"propName" : pageObj.propName + "["+index+"]"
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
						$.model.propValue(pageObj.data, pageObj.propName, [ row ]);
						var propName = pageObj.propName + "[0]";
						
						options = pageObj.buildActionOptions(property, propertyModel,
								{
									"propName" : propName
								});
						
						options.pinTitleButton = true;
						
						pageObj.open(pageObj.url("editMultiplePropValueElement"), options);
						
						$.model.propValue(pageObj.data, pageObj.propName, []);
					}
				});
			});
			
			pageObj.element("input[name=deleteButton]").secondClick(
			function()
			{
				pageObj.executeOnSelects(function(rows, indexes)
				{
					if(pageObj.clientOperation)
					{
						pageObj.deleteRow(indexes);
						pageObj.restoreGridData();
					}
					else
					{
						var options = pageObj.buildActionOptions(property, propertyModel, {"propValueElements" : rows}, null);
						$.post(pageObj.url("deleteMultiplePropValueElements"), options.data, function()
						{
							pageObj.refresh();
						});
					}
				});
			},
			function()
			{
				var re = false;
				pageObj.executeOnSelects(function(row){ re = true; });
				return re;
			});
		<%}%>

		pageObj.conditionAutocompleteSource = $.buildSearchConditionAutocompleteSource(pageObj.conditionSource);
		pageObj.initConditionPanel();
		pageObj.initModelTable(propertyModel, $.model.propValue(pageObj.data, pageObj.propName), pageObj.mappedByWith);
		
		<%if(clientOperation){%>
		
		<%}else{%>
			pageObj.initPagination();
			pageObj.refresh();
		<%}%>
	});
})
(${pageId});
</script>
</body>
</html>

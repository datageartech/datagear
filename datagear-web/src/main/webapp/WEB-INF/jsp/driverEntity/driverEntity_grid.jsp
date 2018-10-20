<%--
/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="org.datagear.web.controller.DriverEntityController" %>
<%@ include file="../include/jsp_import.jsp" %>
<%@ include file="../include/jsp_ajax_request.jsp" %>
<%@ include file="../include/jsp_jstl.jsp" %>
<%@ include file="../include/jsp_page_id.jsp" %>
<%@ include file="../include/jsp_method_get_string_value.jsp" %>
<%@ include file="../include/html_doctype.jsp" %>
<%
//标题标签I18N关键字，不允许null
String titleMessageKey = getStringValue(request, DriverEntityController.KEY_TITLE_MESSAGE_KEY);
//是否选择操作，允许为null
boolean selectonly = ("true".equalsIgnoreCase(getStringValue(request, DriverEntityController.KEY_SELECTONLY)));
%>
<html style="height:100%;">
<head>
<%@ include file="../include/html_head.jsp" %>
<title><%@ include file="../include/html_title_app_name.jsp" %><fmt:message key='<%=titleMessageKey%>' /></title>
</head>
<body style="height:100%;">
<%if(!ajaxRequest){%>
<div style="height:99%;">
<%}%>
<div id="${pageId}" class="page-data-grid page-data-grid-driverEntity">
	<div class="head">
		<div class="search">
			<form id="${pageId}-searchForm" class="search-form" action="#">
				<div class="ui-widget ui-widget-content keyword-widget"><input name="keyword" type="text" class="ui-widget ui-widget-content input-keyword" /></div>
				<input name="submit" type="submit" value="<fmt:message key='query' />" />
			</form>
		</div>
		<div class="operation">
			<%if(selectonly){%>
				<input name="confirmButton" type="button" class="recommended" value="<fmt:message key='confirm' />" />
				<input name="viewButton" type="button" value="<fmt:message key='view' />" />
			<%}else{%>
				<input name="importButton" type="button" value="<fmt:message key='import' />" />
				<input name="exportButton" type="button" value="<fmt:message key='export' />" />
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
<%@ include file="../include/page_js_obj.jsp" %>
<%@ include file="../include/page_obj_grid.jsp" %>
<script type="text/javascript">
(function(pageObj)
{
	$("input:submit, input:button, input:reset, button", pageObj.element(".head")).button();
	
	pageObj.url = function(action)
	{
		return contextPath + "/driverEntity/" + action;
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
		pageObj.refresh(searchParam, null);
	};
	
	pageObj.sort = function(order)
	{
		pageObj.refresh(null, order);
	};
	
	pageObj.refresh = function(searchParam, order)
	{
		if(!searchParam)
			searchParam = pageObj.getSearchParam();
		if(!order)
			order = pageObj.getOrderTyped();
		
		var url = pageObj.url("queryData");
		
		var param = {};
		
		$.extend(param, searchParam);
		$.extend(param, { "order" : order });
		
		$.getJSONOnPost(url, param, function(data)
		{
			pageObj.setTableData(data);
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
	
	<%if(!selectonly){%>
		pageObj.element("input[name=addButton]").click(function()
		{
			pageObj.open(pageObj.url("add"),
			{
				pageParam :
				{
					afterSave : function()
					{
						pageObj.refresh();
					}
				}
			});
		});
		
		pageObj.element("input[name=importButton]").click(function()
		{
			pageObj.open(pageObj.url("import"),
			{
				pageParam :
				{
					afterSave : function()
					{
						pageObj.refresh();
					}
				}
			});
		});

		pageObj.element("input[name=exportButton]").click(function()
		{
			var selectedDatas = pageObj.getSelectedData();
			var param = $.getPropertyParamString(selectedDatas, "id");
			
			var options = {target : "_file"};
			
			pageObj.open(pageObj.url("export?"+param), options);
		});
		
		pageObj.element("input[name=editButton]").click(function()
		{
			pageObj.executeOnSelect(function(row)
			{
				var data = {"id" : row.id};
				
				pageObj.open(pageObj.url("edit"),
				{
					data : data,
					pageParam :
					{
						afterSave : function()
						{
							pageObj.refresh();
						}
					}
				});
			});
		});
	<%}%>

	pageObj.element("input[name=viewButton]").click(function()
	{
		pageObj.executeOnSelect(function(row)
		{
			var data = {"id" : row.id};
			
			pageObj.open(pageObj.url("view"),
			{
				data : data
			});
		});
	});
	
	<%if(!selectonly){%>
		pageObj.element("input[name=deleteButton]").click(
		function()
		{
			pageObj.executeOnSelects(function(rows)
			{
				pageObj.confirm("<fmt:message key='driverEntity.confirmDelete' />",
				{
					"confirm" : function()
					{
						var data = "";
						for(var i=0; i< rows.length; i++)
						{
							if(data != "")
								data += "&";
							
							data += "id=" + rows[i].id;
						}
						
						$.post(pageObj.url("delete"), data, function()
						{
							pageObj.refresh();
						});
					}
				});
			});
		});
	<%}%>
	
	pageObj.element("input[name=confirmButton]").click(function()
	{
		pageObj.executeOnSelect(function(row)
		{
			var pageParam = pageObj.pageParam();
			
			var close = (pageParam && pageParam.submit ? pageParam.submit(row) : undefined);
			
			//单选默认关闭
			if(close == undefined)
				close = true;
			
			if(close)
				pageObj.close();
		});
	});
	
	pageObj.buildTableColumValueOption = function(title, data, hidden)
	{
		var option =
		{
			title : title,
			data : data,
			visible : !hidden,
			render: function(data, type, row, meta)
			{
				return $.truncateIf(data);
			},
			defaultContent: "",
		};
		
		return option;
	};
	
	var tableColumns = [
		pageObj.buildTableColumValueOption("<fmt:message key='driverEntity.id' />", "id", true),
		pageObj.buildTableColumValueOption("<fmt:message key='driverEntity.displayName' />", "displayName"),
		pageObj.buildTableColumValueOption("<fmt:message key='driverEntity.driverClassName' />", "driverClassName"),
		pageObj.buildTableColumValueOption("<fmt:message key='driverEntity.displayDesc' />", "displayDescMore"),
		pageObj.buildTableColumValueOption("", "displayText", true)
	];
	var tableSettings = pageObj.getTableSettings(tableColumns);
	pageObj.initTable(tableSettings);
	pageObj.refresh();
})
(${pageId});
</script>
</body>
</html>

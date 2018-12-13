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
<div id="${pageId}" class="page-grid page-grid-hidden-foot page-grid-driverEntity">
	<div class="head">
		<div class="search">
			<%@ include file="../include/page_obj_searchform.html.jsp" %>
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
		<div class="pagination-wrapper">
			<div id="${pageId}-pagination" class="pagination"></div>
		</div>
	</div>
</div>
<%if(!ajaxRequest){%>
</div>
<%}%>
<%@ include file="../include/page_js_obj.jsp" %>
<%@ include file="../include/page_obj_searchform_js.jsp" %>
<%@ include file="../include/page_obj_grid.jsp" %>
<script type="text/javascript">
(function(po)
{
	$.initButtons(po.element(".operation"));
	
	po.url = function(action)
	{
		return contextPath + "/driverEntity/" + action;
	};
	
	<%if(!selectonly){%>
		po.element("input[name=addButton]").click(function()
		{
			po.open(po.url("add"),
			{
				pageParam :
				{
					afterSave : function()
					{
						po.refresh();
					}
				}
			});
		});
		
		po.element("input[name=importButton]").click(function()
		{
			po.open(po.url("import"),
			{
				pageParam :
				{
					afterSave : function()
					{
						po.refresh();
					}
				}
			});
		});

		po.element("input[name=exportButton]").click(function()
		{
			var selectedDatas = po.getSelectedData();
			var param = $.getPropertyParamString(selectedDatas, "id");
			
			var options = {target : "_file"};
			
			po.open(po.url("export?"+param), options);
		});
		
		po.element("input[name=editButton]").click(function()
		{
			po.executeOnSelect(function(row)
			{
				var data = {"id" : row.id};
				
				po.open(po.url("edit"),
				{
					data : data,
					pageParam :
					{
						afterSave : function()
						{
							po.refresh();
						}
					}
				});
			});
		});
	<%}%>

	po.element("input[name=viewButton]").click(function()
	{
		po.executeOnSelect(function(row)
		{
			var data = {"id" : row.id};
			
			po.open(po.url("view"),
			{
				data : data
			});
		});
	});
	
	<%if(!selectonly){%>
		po.element("input[name=deleteButton]").click(
		function()
		{
			po.executeOnSelects(function(rows)
			{
				po.confirm("<fmt:message key='driverEntity.confirmDelete' />",
				{
					"confirm" : function()
					{
						var data = $.getPropertyParamString(rows, "id");
						
						$.post(po.url("delete"), data, function()
						{
							po.refresh();
						});
					}
				});
			});
		});
	<%}%>
	
	po.element("input[name=confirmButton]").click(function()
	{
		po.executeOnSelect(function(row)
		{
			var close = po.pageParamCall("submit", row);
			
			//单选默认关闭
			if(close == undefined)
				close = true;
			
			if(close)
				po.close();
		});
	});
	
	po.buildTableColumValueOption = function(title, data, hidden)
	{
		var option =
		{
			title : title,
			data : data,
			visible : !hidden,
			render: function(data, type, row, meta)
			{
				return $.escapeHtml($.truncateIf(data));
			},
			defaultContent: "",
		};
		
		return option;
	};
	
	var tableColumns = [
		po.buildTableColumValueOption("<fmt:message key='driverEntity.id' />", "id", true),
		po.buildTableColumValueOption("<fmt:message key='driverEntity.displayName' />", "displayName"),
		po.buildTableColumValueOption("<fmt:message key='driverEntity.driverClassName' />", "driverClassName"),
		po.buildTableColumValueOption("<fmt:message key='driverEntity.displayDesc' />", "displayDescMore"),
		po.buildTableColumValueOption("", "displayText", true)
	];
	var tableSettings = po.buildDataTableSettingsAjax(tableColumns, po.url("queryData"));
	po.initDataTable(tableSettings);
})
(${pageId});
</script>
</body>
</html>

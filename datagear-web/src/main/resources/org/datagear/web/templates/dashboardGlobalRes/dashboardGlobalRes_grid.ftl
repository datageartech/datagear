<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#include "../include/import_global.ftl">
<#include "../include/html_doctype.ftl">
<#--
titleMessageKey 标题标签I18N关键字，不允许null
selectOperation 是否选择操作，允许为null
-->
<#assign selectOperation=(selectOperation!false)>
<html>
<head>
<#include "../include/html_head.ftl">
<title><#include "../include/html_title_app_name.ftl"><@spring.message code='${titleMessageKey}' /></title>
</head>
<body class="fill-parent">
<#if !isAjaxRequest>
<div class="fill-parent">
</#if>
<#include "../include/page_js_obj.ftl">
<div id="${pageId}" class="page-grid page-grid-hidden-foot page-grid-dashboardGlobalRes">
	<div class="head">
		<div class="search">
			<#include "../include/page_obj_searchform.ftl">
		</div>
		<div class="operation">
			<#if selectOperation>
				<input name="confirmButton" type="button" class="recommended" value="<@spring.message code='confirm' />" />
				<input name="viewButton" type="button" value="<@spring.message code='view' />" />
			<#else>
				<input name="addButton" type="button" value="<@spring.message code='add' />" />
				<input name="uploadButton" type="button" value="<@spring.message code='upload' />" />
				<input name="editButton" type="button" value="<@spring.message code='edit' />" />
				<input name="viewButton" type="button" value="<@spring.message code='view' />" />
				<input name="downloadButton" type="button" value="<@spring.message code='download' />" />
				<input name="deleteButton" type="button" value="<@spring.message code='delete' />" />
			</#if>
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
<#if !isAjaxRequest>
</div>
</#if>
<#include "../include/page_obj_grid.ftl">
<script type="text/javascript">
(function(po)
{
	$.initButtons(po.element(".operation"));
	
	po.url = function(action)
	{
		return "${contextPath}/dashboardGlobalRes/" + action;
	};
	
	po.element("input[name=addButton]").click(function()
	{
		po.open(po.url("add"));
	});
	
	po.element("input[name=uploadButton]").click(function()
	{
		po.open(po.url("upload"));
	});
	
	po.element("input[name=editButton]").click(function()
	{
		po.executeOnSelect(function(row)
		{
			if(!$.isTextFile(row.path))
			{
				$.tipInfo("<@spring.message code='dashboardGlobalRes.editResourceUnsupport' />");
		 		return;
			}
			
			var data = {"path" : row.path};
			
			po.open(po.url("edit"),
			{
				data : data
			});
		});
	});
	
	po.element("input[name=downloadButton]").click(function()
	{
		po.executeOnSelect(function(row)
		{
			var data = {"path" : row.path};
			
			po.open(po.url("download"),
			{
				target: "_file",
				data : data
			});
		});
	});
	
	po.element("input[name=viewButton]").click(function()
	{
		po.executeOnSelect(function(row)
		{
			window.open(po.url("view/" + row.path));
		});
	});
	
	po.element("input[name=deleteButton]").click(
	function()
	{
		po.executeOnSelects(function(rows)
		{
			po.confirmDeleteEntities(po.url("delete"), rows, "path");
		});
	});
	
	var tableColumns = [
		$.buildDataTablesColumnSimpleOption("<@spring.message code='id' />", "path", true),
		$.buildDataTablesColumnSimpleOption($.buildDataTablesColumnTitleSearchable("<@spring.message code='dashboardGlobalRes.path' />"), "path", false, true)
	];
	
	var tableSettings = po.buildDataTableSettingsAjax(tableColumns, po.url("queryData"));
	tableSettings.ordering = false;
	po.initDataTable(tableSettings);
	po.bindResizeDataTable();
})
(${pageId});
</script>
</body>
</html>

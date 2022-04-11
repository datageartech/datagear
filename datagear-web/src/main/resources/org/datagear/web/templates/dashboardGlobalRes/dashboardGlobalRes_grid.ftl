<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#include "../include/page_import.ftl">
<#include "../include/html_doctype.ftl">
<#--
titleMessageKey 标题标签I18N关键字，不允许null
selectOperation 是否选择操作，允许为null
-->
<#assign selectOperation=(selectOperation!false)>
<#assign selectPageCss=(selectOperation?string('page-grid-select',''))>
<html>
<head>
<#include "../include/html_head.ftl">
<title><#include "../include/html_title_app_name.ftl"><@spring.message code='${titleMessageKey}' /></title>
</head>
<body class="fill-parent">
<#if !isAjaxRequest>
<div class="fill-parent">
</#if>
<#include "../include/page_obj.ftl">
<div id="${pageId}" class="page-grid ${selectPageCss} page-grid-hidden-foot page-grid-dashboardGlobalRes">
	<div class="head">
		<div class="search">
			<#include "../include/page_obj_searchform.ftl">
		</div>
		<div class="operation">
			<#if selectOperation>
				<button type="button" class="selectButton recommended"><@spring.message code='confirm' /></button>
				<button type="button" class="viewButton"><@spring.message code='view' /></button>
			<#else>
				<button type="button" class="addButton"><@spring.message code='add' /></button>
				<button type="button" class="uploadButton"><@spring.message code='upload' /></button>
				<button type="button" class="editButton"><@spring.message code='edit' /></button>
				<button type="button" class="viewButton"><@spring.message code='view' /></button>
				<button type="button" class="downloadButton"><@spring.message code='download' /></button>
				<button type="button" class="deleteButton"><@spring.message code='delete' /></button>
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
	po.initGridBtns();
	
	po.url = function(action)
	{
		return "${contextPath}/dashboardGlobalRes/" + action;
	};
	
	po.element(".addButton").click(function()
	{
		po.handleAddOperation(po.url("add"));
	});
	
	po.element(".uploadButton").click(function()
	{
		po.open(po.url("upload"));
	});
	
	po.element(".editButton").click(function()
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
	
	po.element(".downloadButton").click(function()
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
	
	po.element(".viewButton").click(function()
	{
		po.executeOnSelect(function(row)
		{
			window.open(po.url("view/" + row.path));
		});
	});
	
	po.element(".deleteButton").click(function()
	{
		po.handleDeleteOperation(po.url("delete"), "path");
	});
	
	var tableColumns = [
		$.buildDataTablesColumnSimpleOption("<@spring.message code='id' />", "path", true),
		$.buildDataTablesColumnSimpleOption($.buildDataTablesColumnTitleSearchable("<@spring.message code='dashboardGlobalRes.path' />"), "path", false, true)
	];
	
	var tableSettings = po.buildAjaxTableSettings(tableColumns, po.url("queryData"));
	tableSettings.ordering = false;
	po.initTable(tableSettings);
})
(${pageId});
</script>
</body>
</html>

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
<#assign isMultipleSelect=(isMultipleSelect!false)>
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
<div id="${pageId}" class="page-grid ${selectPageCss} page-grid-hidden-foot page-grid-schemaGuard">
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
				<button type="button" class="editButton"><@spring.message code='edit' /></button>
				<button type="button" class="viewButton"><@spring.message code='view' /></button>
				<button type="button" class="testButton"><@spring.message code='test' /></button>
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
		return "${contextPath}/schemaGuard/" + action;
	};
	
	po.element(".addButton").click(function()
	{
		po.open(po.url("add"),
		{
			<#if selectOperation>
			pageParam:
			{
				submitSuccess: function(data)
				{
					po.pageParamCallSelect(true, data);
				}
			}
			</#if>
		});
	});
	
	po.element(".editButton").click(function()
	{
		po.executeOnSelect(function(row)
		{
			var data = {"id" : row.id};
			
			po.open(po.url("edit"), { data : data });
		});
	});

	po.element(".viewButton").click(function()
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

	po.element(".testButton").click(function()
	{
		po.open(po.url("test"));
	});
	
	po.element(".deleteButton").click(
	function()
	{
		po.executeOnSelects(function(rows)
		{
			po.confirmDeleteEntities(po.url("delete"), rows);
		});
	});
	
	po.element(".selectButton").click(function()
	{
		<#if isMultipleSelect>
		po.executeOnSelects(function(rows)
		{
			po.pageParamCallSelect(true, rows);
		});
		<#else>
		po.executeOnSelect(function(row)
		{
			po.pageParamCallSelect(true, row);
		});
		</#if>
	});
	
	var columnPermitted = $.buildDataTablesColumnSimpleOption("<@spring.message code='schemaGuard.permitted' />", "permitted");
	columnPermitted.render = function(data, type, row, meta)
	{
		if(data == true)
			data = "<@spring.message code='yes' />";
		else
			data = "<@spring.message code='no' />";
		
		return data;
	};
	
	var columnEnabled = $.buildDataTablesColumnSimpleOption("<@spring.message code='schemaGuard.enabled' />", "enabled");
	columnEnabled.render = function(data, type, row, meta)
	{
		if(data == true)
			data = "<@spring.message code='yes' />";
		else
			data = "<@spring.message code='no' />";
		
		return data;
	};
	
	var tableColumns = [
		$.buildDataTablesColumnSimpleOption("<@spring.message code='id' />", "id", true),
		$.buildDataTablesColumnSimpleOption($.buildDataTablesColumnTitleSearchable("<@spring.message code='schemaGuard.pattern' />"), "pattern"),
		columnPermitted,
		$.buildDataTablesColumnSimpleOption("<@spring.message code='schemaGuard.priority' />", "priority"),
		columnEnabled,
		$.buildDataTablesColumnSimpleOption("<@spring.message code='schemaGuard.createTime' />", "createTime")
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

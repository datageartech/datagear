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
Schema schema 数据库，不允许为null
Table table 模型，不允许为null
String titleDisplayName 页面展示名称，默认为""
String titleDisplayDesc 页面展示描述，默认为""
boolean selectOperation 是否选择操作，允许为null
boolean isMultipleSelect 是否多选，默认为false
boolean readonly 是否只读操作，默认为false
-->
<#assign titleDisplayName=(titleDisplayName!'')>
<#assign titleDisplayDesc=(titleDisplayDesc!'')>
<#assign selectOperation=(selectOperation!false)>
<#assign selectPageCss=(selectOperation?string('page-grid-select',''))>
<#assign isMultipleSelect=(isMultipleSelect!false)>
<#assign readonly=(readonly!false)>
<html>
<head>
<#include "../include/html_head.ftl">
<title>
	<#include "../include/html_title_app_name.ftl">
	<#if selectOperation>
	<@spring.message code='select' />
	<#else>
	<@spring.message code='query' />
	</#if>
	<@spring.message code='titleSeparator' />
	${titleDisplayName}
	<#if titleDisplayDesc != ''>
	<@spring.message code='bracketLeft' />
	${titleDisplayDesc}
	<@spring.message code='bracketRight' />
	</#if>
	<@spring.message code='bracketLeft' />
	${schema.title}
	<@spring.message code='bracketRight' />
</title>
</head>
<body class="fill-parent">
<#if !isAjaxRequest>
<div class="fill-parent">
</#if>
<#include "include/data_page_obj.ftl">
<div id="${pageId}" class="page-grid ${selectPageCss} page-grid-data">
	<div class="head">
		<div class="search">
			<#include "include/data_page_obj_searchform.ftl">
		</div>
		<div class="operation">
			<#if selectOperation>
				<button type="button" class="selectButton recommended"><@spring.message code='confirm' /></button>
			</#if>
			<#if readonly>
				<button type="button" class="viewButton"><@spring.message code='view' /></button>
			<#else>
				<button type="button" class="addButton"><@spring.message code='add' /></button>
				<#if !selectOperation>
				<button type="button" class="editButton"><@spring.message code='edit' /></button>
				</#if>
				<button type="button" class="viewButton"><@spring.message code='view' /></button>
				<#if !selectOperation>
				<button type="button" class="exportButton"><@spring.message code='export' /></button>
				<button type="button" class="deleteButton"><@spring.message code='delete' /></button>
				</#if>
			</#if>
		</div>
	</div>
	<div class="content">
		<table id="${pageId}-table" width="100%" class="hover stripe">
		</table>
	</div>
	<div class="foot foot-edit-grid">
		<#if !readonly>
		<#include "include/data_page_obj_edit_grid_html.ftl">
		</#if>
		<div class="pagination-wrapper">
			<div id="${pageId}-pagination" class="pagination"></div>
		</div>
	</div>
</div>
<#if !isAjaxRequest>
</div>
</#if>
<#include "../include/page_obj_pagination.ftl">
<#include "include/data_page_obj_grid.ftl">
<#if !readonly>
<#include "include/data_page_obj_edit_grid_js.ftl">
</#if>
<#include "../include/page_obj_data_permission.ftl">
<#include "../include/page_obj_data_permission_ds_table.ftl">
<script type="text/javascript">
(function(po)
{
	po.sqlIdentifierQuote = "${sqlIdentifierQuote?js_string?no_esc}";
	po.isMultipleSelect = ${isMultipleSelect?c};
	
	po.initGridBtns();
	
	if(!po.canEditTableData(${schema.dataPermission}))
	{
		po.element(".addButton").attr("disabled", "disabled").hide();
		po.element(".editButton").attr("disabled", "disabled").hide();
	}
	
	if(!po.canDeleteTableData(${schema.dataPermission}))
		po.element(".deleteButton").attr("disabled", "disabled").hide();
	
	if(!po.canReadTableData(${schema.dataPermission}))
	{
		po.element(".viewButton").attr("disabled", "disabled").hide();
		po.element(".exportButton").attr("disabled", "disabled").hide();
	}
	
	po.onDbTable(function(dbTable)
	{
		po.element(".addButton").click(function()
		{
			var url = po.url("add");
			url = $.addParam(url, "batchSet", "true");
			
			po.handleAddOperation(url,
			{
				pinTitleButton: true
			});
		});
		
		po.element(".editButton").click(function()
		{
			po.executeOnSelect(function(row)
			{
				var data = $.tableMeta.uniqueRecordData(dbTable, row);
				
				po.open(po.url("edit"),
				{
					contentType: $.CONTENT_TYPE_JSON,
					data : data,
					pinTitleButton : true
				});
			});
		});
		
		po.element(".deleteButton").click(function()
		{
			po.executeOnSelects(function(rows)
			{
				<#assign messageArgs=['"+rows.length+"'] />
				po.confirm("<@spring.messageArgs code='data.confirmDelete' args=messageArgs />",
				{
					"confirm" : function()
					{
						var data = $.tableMeta.uniqueRecordData(dbTable, rows);
						
						po.ajaxSubmitForHandleDuplication(po.url("delete"), data, "<@spring.message code='delete.continueIgnoreDuplicationTemplate' />",
						{
							contentType: $.CONTENT_TYPE_JSON,
							"success" : function()
							{
								po.refresh();
							}
						});
					}
				});
			});
		});
		
		po.element(".selectButton").click(function()
		{
			po.handleSelectOperation();
		});
		
		po.element(".viewButton").click(function()
		{
			po.executeOnSelect(function(row)
			{
				var data = $.tableMeta.uniqueRecordData(dbTable, row);
				
				po.open(po.url("view"),
				{
					contentType: $.CONTENT_TYPE_JSON,
					data : data
				});
			});
		});
		
		po.element(".exportButton").click(function()
		{
			var query = po.getSearchParam();
			query["orders"] = po.getOrdersOnName();
			
			$.postJson(po.url("getQuerySql"), query, function(response)
			{
				var options = {data: {"initSqls": response.sql}};
				$.setGridPageHeightOption(options);
				po.open("${contextPath}/dataexchange/"+po.schemaId+"/export", options);
			});
		});
		
		po.conditionAutocompleteSource = $.buildSearchConditionAutocompleteSource(dbTable, po.sqlIdentifierQuote);
		po.initConditionPanel();
		po.initPagination();
		po.initTableForDbTable(po.url("queryData"), dbTable);
		
		<#if !readonly>
		if(po.canEditTableData(${schema.dataPermission}))
			po.initEditGrid(dbTable);
		else
		{
			po.elementEditGridSwitch().checkboxradio().checkboxradio("disable");
			po.elementEditGridSwitchWrapper().hide();
		}
		</#if>
	});
})
(${pageId});
</script>
</body>
</html>

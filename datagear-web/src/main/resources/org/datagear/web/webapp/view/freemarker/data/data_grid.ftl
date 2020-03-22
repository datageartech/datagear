<#include "../include/import_global.ftl">
<#include "../include/html_doctype.ftl">
<#--
Schema schema 数据库，不允许为null
Table table 模型，不允许为null
String titleDisplayName 页面展示名称，默认为""
String titleDisplayDesc 页面展示描述，默认为""
boolean readonly 是否只读操作，默认为false
List PropertyPathDisplayName conditionSource 可用的查询条件列表，不允许为null
-->
<#assign titleDisplayName=(titleDisplayName!'')>
<#assign titleDisplayDesc=(titleDisplayDesc!'')>
<#assign readonly=(readonly!false)>
<html>
<head>
<#include "../include/html_head.ftl">
<title>
	<#include "../include/html_title_app_name.ftl">
	<@spring.message code='query' />
	<@spring.message code='titleSeparator' />
	${titleDisplayName?html}
	<#if titleDisplayDesc != ''>
	<@spring.message code='bracketLeft' />
	${titleDisplayDesc?html}
	<@spring.message code='bracketRight' />
	</#if>
	<@spring.message code='bracketLeft' />
	${schema.title?html}
	<@spring.message code='bracketRight' />
</title>
</head>
<body class="fill-parent">
<#if !isAjaxRequest>
<div class="fill-parent">
</#if>
<div id="${pageId}" class="page-grid page-grid-query">
	<div class="head">
		<div class="search">
			<#include "include/data_page_obj_searchform_html.ftl">
		</div>
		<div class="operation">
			<#if readonly>
				<input name="viewButton" type="button" value="<@spring.message code='view' />" />
			<#else>
				<input name="addButton" type="button" value="<@spring.message code='add' />" />
				<input name="editButton" type="button" value="<@spring.message code='edit' />" />
				<input name="viewButton" type="button" value="<@spring.message code='view' />" />
				<input name="deleteButton" type="button" value="<@spring.message code='delete' />" />
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
<#include "include/data_page_obj.ftl">
<#include "include/data_page_obj_searchform_js.ftl">
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
	po.conditionSource = <@writeJson var=conditionSource />;
	
	$.initButtons(po.element(".operation"));
	
	if(!po.canEditTableData(${schema.dataPermission}))
	{
		po.element("input[name=addButton]").attr("disabled", "disabled").hide();
		po.element("input[name=editButton]").attr("disabled", "disabled").hide();
	}
	
	if(!po.canDeleteTableData(${schema.dataPermission}))
		po.element("input[name=deleteButton]").attr("disabled", "disabled").hide();
	
	if(!po.canReadTableData(${schema.dataPermission}))
		po.element("input[name=viewButton]").attr("disabled", "disabled").hide();
	
	po.onTable(function(table)
	{
		<#if !readonly>
		po.element("input[name=addButton]").click(function()
		{
			po.open(po.url("", "add", "batchSet=true"), { pinTitleButton : true });
		});
		
		po.element("input[name=editButton]").click(function()
		{
			po.executeOnSelect(function(row)
			{
				var data = {"data" : row};
				
				po.open(po.url("edit"),
				{
					data : data,
					pinTitleButton : true
				});
			});
		});
		
		po.element("input[name=deleteButton]").click(function()
		{
			po.executeOnSelects(function(rows)
			{
				<#assign messageArgs=['"+rows.length+"'] />
				po.confirm("<@spring.messageArgs code='data.confirmDelete' args=messageArgs />",
				{
					"confirm" : function()
					{
						var data = {"data" : rows};
						
						po.ajaxSubmitForHandleDuplication(po.url("delete"), data, "<@spring.message code='delete.continueIgnoreDuplicationTemplate' />",
						{
							"success" : function()
							{
								po.refresh();
							}
						});
					}
				});
			});
		});
		</#if>
		
		po.element("input[name=viewButton]").click(function()
		{
			po.executeOnSelect(function(row)
			{
				var data = {"data" : row};
				
				po.open(po.url("view"),
				{
					data : data
				});
			});
		});
		
		po.conditionAutocompleteSource = $.buildSearchConditionAutocompleteSource(po.conditionSource);
		po.initConditionPanel();
		po.initPagination();
		po.initDataTableAjax(po.url("queryData"), table);
		po.bindResizeDataTable();
		
		<#if !readonly>
		if(po.canEditTableData(${schema.dataPermission}))
			po.initEditGrid(table);
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

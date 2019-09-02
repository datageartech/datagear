<#include "../include/import_global.ftl">
<#include "../include/html_doctype.ftl">
<#--
Schema schema 数据库，不允许为null
Model model 模型，不允许为null
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
<#include "../include/page_obj_data_permission_ds_table.ftl">
</#if>
<script type="text/javascript">
(function(po)
{
	po.conditionSource = $.unref(<@writeJson var=conditionSource />);
	
	$.initButtons(po.element(".operation"));
	
	po.onModel(function(model)
	{
		<#if !readonly>
		if(po.canEditTableData(${schema.dataPermission}))
		{
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
		}
		else
		{
			po.element("input[name=addButton]").button("disable");
			po.element("input[name=editButton]").button("disable");
		}
		
		if(po.canEditTableData(${schema.dataPermission}))
		{
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
							
							po.ajaxSubmitForHandleDuplication("delete", data, "<@spring.message code='delete.continueIgnoreDuplicationTemplate' />",
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
		}
		else
		{
			po.element("input[name=deleteButton]").button("disable");
		}
		</#if>
		
		if(po.canReadTableData(${schema.dataPermission}))
		{
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
		}
		else
		{
			po.element("input[name=viewButton]").button("disable");
		}
		
		po.conditionAutocompleteSource = $.buildSearchConditionAutocompleteSource(po.conditionSource);
		po.initConditionPanel();
		po.initPagination();
		po.initModelDataTableAjax(po.url("queryData"), model);
		po.bindResizeDataTable();
		
		<#if !readonly>
		if(po.canEditTableData(${schema.dataPermission}))
			po.initEditGrid(model);
		else
			po.elementEditGridSwitch().checkboxradio().checkboxradio("disable");
		</#if>
	});
})
(${pageId});
</script>
</body>
</html>

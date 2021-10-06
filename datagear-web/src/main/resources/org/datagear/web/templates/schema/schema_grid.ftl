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
boolean readonly 是否只读操作，默认为false
-->
<#assign selectOperation=(selectOperation!false)>
<#assign selectPageCss=(selectOperation?string('page-grid-select',''))>
<#assign readonly=(readonly!false)>
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
<div id="${pageId}" class="page-grid ${selectPageCss} page-grid-hidden-foot page-grid-schema">
	<div class="head">
		<div class="search">
			<#include "../include/page_obj_searchform.ftl">
		</div>
		<div class="operation">
			<#if selectOperation>
				<input name="confirmButton" type="button" class="recommended" value="<@spring.message code='confirm' />" />
			</#if>
			<#if readonly>
				<input name="viewButton" type="button" value="<@spring.message code='view' />" />
			<#else>
				<input name="addButton" type="button" value="<@spring.message code='add' />" />
				<#if !selectOperation>
				<input name="editButton" type="button" value="<@spring.message code='edit' />" />
				</#if>
				<input name="viewButton" type="button" value="<@spring.message code='view' />" />
				<#if !selectOperation>
				<input name="deleteButton" type="button" value="<@spring.message code='delete' />" />
				</#if>
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
		return "${contextPath}/schema/" + action;
	};
	
	po.element("input[name=addButton]").click(function()
	{
		po.open(po.url("add"),
		{
			<#if selectOperation>
			pageParam:
			{
				afterSave: function(data)
				{
					po.pageParamCallSelect(true, data);
				}
			}
			</#if>
		});
	});
	
	po.element("input[name=editButton]").click(function()
	{
		po.executeOnSelect(function(row)
		{
			var data = {"id" : row.id};
			
			po.open(po.url("edit"), { data : data });
		});
	});
	
	po.element("input[name=deleteButton]").click(
	function()
	{
		po.executeOnSelects(function(rows)
		{
			po.confirmDeleteEntities(po.url("delete"), rows);
		});
	});
	
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
	
	po.element("input[name=confirmButton]").click(function()
	{
		po.executeOnSelect(function(row)
		{
			po.pageParamCallSelect(true, row);
		});
	});
	
	var tableColumns = [
		$.buildDataTablesColumnSimpleOption("<@spring.message code='id' />", "id", true),
		$.buildDataTablesColumnSimpleOption("<@spring.message code='schema.title' />", "title"),
		$.buildDataTablesColumnSimpleOption("<@spring.message code='schema.url' />", "url"),
		$.buildDataTablesColumnSimpleOption("<@spring.message code='schema.user' />", "user"),
		$.buildDataTablesColumnSimpleOption("<@spring.message code='schema.createUser' />", "createUser.nameLabel"),
		$.buildDataTablesColumnSimpleOption("<@spring.message code='schema.createTime' />", "createTime")
	];
	var tableSettings = po.buildDataTableSettingsAjax(tableColumns, po.url("queryData"));
	po.initDataTable(tableSettings);
	po.bindResizeDataTable();
})
(${pageId});
</script>
</body>
</html>

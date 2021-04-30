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
<#assign DataSetResDirectory=statics['org.datagear.management.domain.DataSetResDirectory']>
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
<div id="${pageId}" class="page-grid page-grid-dataSetResDirectory">
	<div class="head">
		<div class="search search-dataSetResDirectory">
			<#include "../include/page_obj_searchform.ftl">
		</div>
		<div class="operation">
			<#if selectOperation>
				<input name="confirmButton" type="button" class="recommended" value="<@spring.message code='confirm' />" />
				<input name="viewButton" type="button" class="view-button" value="<@spring.message code='view' />" />
			<#else>
				<input name="addButton" type="button" value="<@spring.message code='add' />" />
				<input name="editButton" type="button" value="<@spring.message code='edit' />" />
				<input name="viewButton" type="button" value="<@spring.message code='view' />" />
				<#if !(currentUser.anonymous)>
				<input name="shareButton" type="button" value="<@spring.message code='share' />" />
				</#if>
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
<#include "../include/page_obj_pagination.ftl">
<#include "../include/page_obj_grid.ftl">
<#include "../include/page_obj_data_permission.ftl" >
<script type="text/javascript">
(function(po)
{
	$.initButtons(po.element(".operation"));
	
	po.currentUser = <@writeJson var=currentUser />;
	
	po.url = function(action)
	{
		return "${contextPath}/dataSetResDirectory/" + action;
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
	
	po.element("input[name=shareButton]").click(function()
	{
		po.executeOnSelect(function(row)
		{
			if(!po.canAuthorize(row, po.currentUser))
			{
				$.tipInfo("<@spring.message code='error.PermissionDeniedException' />");
				return;
			}
			
			var options = {};
			$.setGridPageHeightOption(options);
			po.open(contextPath+"/authorization/${DataSetResDirectory.AUTHORIZATION_RESOURCE_TYPE}/query?"
					+"${statics['org.datagear.web.controller.AuthorizationController'].PARAM_ASSIGNED_RESOURCE}="+encodeURIComponent(row.id), options);
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
	
	po.element("input[name=confirmButton]").click(function()
	{
		po.executeOnSelect(function(row)
		{
			po.pageParamCallSelect(true, row);
		});
	});
	
	var tableColumns = [
		$.buildDataTablesColumnSimpleOption("<@spring.message code='id' />", "id", true),
		$.buildDataTablesColumnSimpleOption($.buildDataTablesColumnTitleSearchable("<@spring.message code='dataSetResDirectory.directory' />"), "directory"),
		$.buildDataTablesColumnSimpleOption($.buildDataTablesColumnTitleSearchable("<@spring.message code='dataSetResDirectory.desc' />"), "desc"),
		$.buildDataTablesColumnSimpleOption("<@spring.message code='dataSetResDirectory.createUser' />", "createUser.realName", true),
		$.buildDataTablesColumnSimpleOption("<@spring.message code='dataSetResDirectory.createTime' />", "createTime", true)
	];
	
	po.initPagination();
	
	var tableSettings = po.buildDataTableSettingsAjax(tableColumns, po.url("pagingQueryData"));
	po.initDataTable(tableSettings);
	po.bindResizeDataTable();
})
(${pageId});
</script>
</body>
</html>

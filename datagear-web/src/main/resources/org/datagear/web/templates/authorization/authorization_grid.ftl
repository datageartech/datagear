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
String titleMessageKey 标题标签I18N关键字，不允许null
ResourceMeta resourceMeta 资源元信息，不允许null
-->
<#assign selectOperation=(selectOperation!false)>
<#assign selectPageCss=(selectOperation?string('page-grid-select',''))>
<#assign AuthorizationController=statics['org.datagear.web.controller.AuthorizationController']>
<html>
<head>
<#include "../include/html_head.ftl">
<title><#include "../include/html_title_app_name.ftl"><@spring.message code='${titleMessageKey}' /> - <@spring.message code='${resourceMeta.resouceTypeLabel}' /></title>
</head>
<body class="fill-parent">
<#if !isAjaxRequest>
<div class="fill-parent">
</#if>
<#include "../include/page_js_obj.ftl">
<div id="${pageId}" class="page-grid ${selectPageCss} page-grid-hidden-foot page-grid-authorization">
	<div class="head">
		<div class="search">
			<#include "../include/page_obj_searchform.ftl">
		</div>
		<div class="operation">
			<input name="addButton" type="button" value="<@spring.message code='add' />" />
			<input name="editButton" type="button" value="<@spring.message code='edit' />" />
			<input name="viewButton" type="button" value="<@spring.message code='view' />" />
			<input name="deleteButton" type="button" value="<@spring.message code='delete' />" />
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
<#include "../include/page_obj_data_permission.ftl">
<script type="text/javascript">
(function(po)
{
	po.initGridBtns();
	
	po.url = function(action)
	{
		return "${contextPath}/authorization/${resourceMeta.resourceType}/"
				+ encodeURIComponent("${resource?js_string?no_esc}") + "/" + action;
	};
	
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
	
	po.element("input[name=editButton]").click(function()
	{
		po.executeOnSelect(function(row)
		{
			var data =
			{
				"id" : row.id
			};
			
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
	
	po.element("input[name=viewButton]").click(function()
	{
		po.executeOnSelect(function(row)
		{
			var data =
			{
				"id" : row.id
			};
			
			po.open(po.url("view"),
			{
				data : data
			});
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
	
	var columnEnabled = $.buildDataTablesColumnSimpleOption("<@spring.message code='${resourceMeta.authEnabledLabel}' />", "enabled", ${(resourceMeta.enableSetEnable)?string('false', 'true')});
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
		$.buildDataTablesColumnSimpleOption($.buildDataTablesColumnTitleSearchable("<@spring.message code='${resourceMeta.authPrincipalLabel}' />"), "principalName"),
		$.buildDataTablesColumnSimpleOption("<@spring.message code='${resourceMeta.authPermissionLabel}' />", "permissionLabel", ${(resourceMeta.singlePermission)?string('true', 'false')}),
		columnEnabled
	];
	
	var url = po.url("queryData");
	
	var tableSettings = po.buildDataTableSettingsAjax(tableColumns, url);
	po.initDataTable(tableSettings);
	po.bindResizeDataTable();
})
(${pageId});
</script>
</body>
</html>

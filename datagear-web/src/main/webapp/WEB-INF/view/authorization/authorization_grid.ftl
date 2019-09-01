<#include "../include/import_global.ftl">
<#include "../include/html_doctype.ftl">
<#--
String titleMessageKey 标题标签I18N关键字，不允许null
String authorizationSource 固定授权源，允许为null
String authorizationSourceType 固定授权源类型，允许为null
-->
<#assign selectonly=(selectonly!false)>
<#assign dataModel=(dataModel!"full")>
<html>
<head>
<#include "../include/html_head.ftl">
<title><#include "../include/html_title_app_name.ftl"><@spring.message code='${titleMessageKey}' /></title>
</head>
<body class="fill-parent">
<#if !isAjaxRequest>
<div class="fill-parent">
</#if>
<div id="${pageId}" class="page-grid page-grid-hidden-foot page-grid-authorization">
	<div class="head">
		<div class="search">
			<#include "../include/page_obj_searchform.html.ftl">
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
<#include "../include/page_js_obj.ftl">
<#include "../include/page_obj_searchform_js.ftl">
<#include "../include/page_obj_grid.ftl">
<#include "../include/page_obj_data_permission.ftl">
<#include "../include/page_obj_data_permission_ds_table.ftl">
<script type="text/javascript">
(function(po)
{
	$.initButtons(po.element(".operation"));
	
	po.url = function(action)
	{
		return "${contextPath}/authorization/" + action;
	};
	
	po.element("input[name=addButton]").click(function()
	{
		var data =
		{
			<#if appointResource??>
			"${statics['org.datagear.web.controller.AuthorizationController'].PARAM_APPOINT_RESOURCE}" : "${appointResource}"
			</#if>
		};
		
		po.open(po.url("add"),
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
	
	po.element("input[name=editButton]").click(function()
	{
		po.executeOnSelect(function(row)
		{
			var data =
			{
				<#if appointResource??>
				"${statics['org.datagear.web.controller.AuthorizationController'].PARAM_APPOINT_RESOURCE}" : "${appointResource?js_string}",
				</#if>
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
				<#if appointResource??>
				"${statics['org.datagear.web.controller.AuthorizationController'].PARAM_APPOINT_RESOURCE}" : "${appointResource?js_string}",
				</#if>
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
			po.confirm("<@spring.message code='confirmDelete' />",
			{
				"confirm" : function()
				{
					var data = $.getPropertyParamObjArray(rows, "id");
					
					$.post(po.url("delete"), data, function()
					{
						po.refresh();
					});
				}
			});
		});
	});
	
	var columnEnabled = $.buildDataTablesColumnSimpleOption("<@spring.message code='authorization.enabled' />", "enabled");
	columnEnabled.render = function(data, type, row, meta)
	{
		if(data == true)
			data = "<@spring.message code='yes' />";
		else
			data = "<@spring.message code='no' />";
		
		return data;
	};
	
	var columnPermission = $.buildDataTablesColumnSimpleOption("<@spring.message code='authorization.permission' />", "permission");
	columnPermission.render = function(data, type, row, meta)
	{
		return po.toTableDataPermissionLabel(data);
	};
	
	var tableColumns = [
		$.buildDataTablesColumnSimpleOption("<@spring.message code='id' />", "id", true),
		$.buildDataTablesColumnSimpleOption($.buildDataTablesColumnTitleSearchable("<@spring.message code='authorization.resource' />"), "resourceName"),
		$.buildDataTablesColumnSimpleOption($.buildDataTablesColumnTitleSearchable("<@spring.message code='authorization.principal' />"), "principalName"),
		columnPermission,
		columnEnabled,
		$.buildDataTablesColumnSimpleOption("<@spring.message code='authorization.createUser' />", "createUser.nameLabel")
	];
	
	var url = po.url("queryData");
	<#if appointResource??>
	url = po.url("queryData?${statics['org.datagear.web.controller.AuthorizationController'].PARAM_APPOINT_RESOURCE}="+encodeURIComponent("${appointResource?js_string}"));
	</#if>
	var tableSettings = po.buildDataTableSettingsAjax(tableColumns, url);
	po.initDataTable(tableSettings);
})
(${pageId});
</script>
</body>
</html>

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
			var data = {"id" : row.id};
			
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
			var data = {"id" : row.id};
			
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
					var data = $.getPropertyParamString(rows, "id");
					
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
		if(data == "NONE")
			data = "<@spring.message code='authorization.permission.NONE' />";
		else if(data == "READ")
			data = "<@spring.message code='authorization.permission.READ' />";
		else if(data == "WRITE")
			data = "<@spring.message code='authorization.permission.WRITE' />";
		else
			data = "";
		
		return data;
	};
	
	var tableColumns = [
		$.buildDataTablesColumnSimpleOption("<@spring.message code='authorization.id' />", "id", true),
		$.buildDataTablesColumnSimpleOption("<@spring.message code='authorization.resourceName' />", "resourceName"),
		$.buildDataTablesColumnSimpleOption("<@spring.message code='authorization.principalName' />", "principalName"),
		columnPermission,
		columnEnabled,
		$.buildDataTablesColumnSimpleOption("<@spring.message code='authorization.createUser' />", "createUser.nameLabel")
	];
	var tableSettings = po.buildDataTableSettingsAjax(tableColumns, po.url("queryData"));
	po.initDataTable(tableSettings);
})
(${pageId});
</script>
</body>
</html>

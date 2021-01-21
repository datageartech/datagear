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
<html>
<head>
<#include "../include/html_head.ftl">
<title><#include "../include/html_title_app_name.ftl"><@spring.message code='role.roleEditUser' /></title>
</head>
<body class="fill-parent">
<#if !isAjaxRequest>
<div class="fill-parent">
</#if>
<div id="${pageId}" class="page-grid page-grid-role-user">
	<div class="head">
		<div class="search">
			<#include "../include/page_obj_searchform.html.ftl">
		</div>
		<div class="operation">
			<input name="addButton" type="button" value="<@spring.message code='add' />" />
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
<#include "../include/page_obj_pagination.ftl">
<#include "../include/page_obj_searchform_js.ftl">
<#include "../include/page_obj_grid.ftl">
<script type="text/javascript">
(function(po)
{
	$.initButtons(po.element(".operation"));
	
	po.role = <@writeJson var=role />;
	
	po.url = function(action)
	{
		return "${contextPath}/role/user/" + action;
	};
	
	po.element("input[name=addButton]").click(function()
	{
		var options =
		{
			pageParam :
			{
				select : function(users)
				{
					if(!$.isArray(users))
						users = [users];
					
					var data = {role : po.role, users : users};
					
					$.post(po.url("saveAdd"), data, function()
					{
						po.refresh();
					});
				}
			}
		};
		
		$.setGridPageHeightOption(options);
		
		po.open("${contextPath}/user/select?multiple", options);
	});
	
	po.element("input[name=deleteButton]").click(
	function()
	{
		po.executeOnSelects(function(rows)
		{
			po.confirmDeleteEntities(po.url("delete"), rows);
		});
	});
	
	po.initPagination();
	
	var tableColumns = [
		$.buildDataTablesColumnSimpleOption("<@spring.message code='id' />", "id", true),
		$.buildDataTablesColumnSimpleOption("<@spring.message code='id' />", "user.id", true),
		$.buildDataTablesColumnSimpleOption($.buildDataTablesColumnTitleSearchable("<@spring.message code='user.name' />"), "user.name"),
		$.buildDataTablesColumnSimpleOption($.buildDataTablesColumnTitleSearchable("<@spring.message code='user.realName' />"), "user.realName")
	];
	var tableSettings = po.buildDataTableSettingsAjax(tableColumns, po.url("pagingQueryData?roleId=" + po.role.id));
	po.initDataTable(tableSettings);
})
(${pageId});
</script>
</body>
</html>

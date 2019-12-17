<#include "../../include/import_global.ftl">
<#include "../../include/html_doctype.ftl">
<html>
<head>
<#include "../../include/html_head.ftl">
<title><#include "../../include/html_title_app_name.ftl"><@spring.message code='dataSet.dataSet' /></title>
</head>
<body class="fill-parent">
<#if !isAjaxRequest>
<div class="fill-parent">
</#if>
<div id="${pageId}" class="page-grid page-grid-data-set">
	<div class="head">
		<div class="search">
			<#include "../../include/page_obj_searchform.html.ftl">
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
<#include "../../include/page_js_obj.ftl">
<#include "../../include/page_obj_searchform_js.ftl">
<#include "../../include/page_obj_pagination.ftl">
<#include "../../include/page_obj_grid.ftl">
<script type="text/javascript">
(function(po)
{
	$.initButtons(po.element(".operation"));
	
	po.url = function(action)
	{
		return "${contextPath}/analysis/dataSet/" + action;
	};
	
	po.buildTableColumValueOption = function(title, data)
	{
		var option =
		{
			title : title,
			data : data,
			render: function(data, type, row, meta)
			{
				return data;
			},
			defaultContent: "",
		};
		
		return option;
	};
	
	var tableColumns = [
		po.buildTableColumValueOption("<@spring.message code='dataSet.name' />", "name"),
		po.buildTableColumValueOption("<@spring.message code='dataSet.dataSource' />", "connectionFactory.schema.title"),
		po.buildTableColumValueOption("<@spring.message code='dataSet.sql' />", "sql"),
		po.buildTableColumValueOption("<@spring.message code='dataSet.createUser' />", "createUser.realName"),
		po.buildTableColumValueOption("<@spring.message code='dataSet.createTime' />", "createTime"),
	];
	
	po.initPagination();
	
	var tableSettings = po.buildDataTableSettingsAjax(tableColumns, po.url("pagingQueryData"));
	tableSettings.order=[[1,"desc"]];
	po.initDataTable(tableSettings);
})
(${pageId});
</script>
</body>
</html>

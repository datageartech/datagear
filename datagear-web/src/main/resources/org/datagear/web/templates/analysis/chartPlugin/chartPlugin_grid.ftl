<#include "../../include/import_global.ftl">
<#include "../../include/html_doctype.ftl">
<#--
titleMessageKey 标题标签I18N关键字，不允许null
selectOperation 是否选择操作，允许为null
-->
<#assign selectOperation=(selectOperation!false)>
<html>
<head>
<#include "../../include/html_head.ftl">
<title><#include "../../include/html_title_app_name.ftl"><@spring.message code='${titleMessageKey}' /></title>
</head>
<body class="fill-parent">
<#if !isAjaxRequest>
<div class="fill-parent">
</#if>
<div id="${pageId}" class="page-grid page-grid-chartPlugin">
	<div class="head">
		<div class="search">
			<#include "../../include/page_obj_searchform.html.ftl">
		</div>
		<div class="operation">
			<#if selectOperation>
				<input name="confirmButton" type="button" class="recommended" value="<@spring.message code='confirm' />" />
				<input name="viewButton" type="button" value="<@spring.message code='view' />" />
			<#else>
				<input name="uploadButton" type="button" value="<@spring.message code='upload' />" />
				<input name="downloadButton" type="button" value="<@spring.message code='download' />" />
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
<#include "../../include/page_js_obj.ftl">
<#include "../../include/page_obj_searchform_js.ftl">
<#include "../../include/page_obj_grid.ftl">
<script type="text/javascript">
(function(po)
{
	$.initButtons(po.element(".operation"));
	
	po.url = function(action)
	{
		return "${contextPath}/analysis/chartPlugin/" + action;
	};

	po.element("input[name=uploadButton]").click(function()
	{
		po.open(po.url("upload"));
	});
	
	po.element("input[name=downloadButton]").click(function()
	{
		po.executeOnSelects(function(rows)
		{
			var param = $.getPropertyParamString(rows, "id");
			var options = {target : "_file"};
			
			po.open(po.url("download?"+param), options);
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
	
	var columnIcon = $.buildDataTablesColumnSimpleOption("<@spring.message code='chartPlugin.icon' />", "iconUrl", true);
	columnIcon.render = function(data, type, row, meta)
	{
		if(data)
			data = "<a class=\"plugin-icon\" style=\"background-image: url(${contextPath}/"+data+")\">&nbsp;</a>";
		
		return data;
	};
	
	var tableColumns = [
		$.buildDataTablesColumnSimpleOption("<@spring.message code='id' />", "id", true),
		$.buildDataTablesColumnSimpleOption($.buildDataTablesColumnTitleSearchable("<@spring.message code='chartPlugin.name' />"), "nameLabel.value"),
		$.buildDataTablesColumnSimpleOption($.buildDataTablesColumnTitleSearchable("<@spring.message code='chartPlugin.desc' />"), "descLabel.value"),
		columnIcon,
		$.buildDataTablesColumnSimpleOption("<@spring.message code='chartPlugin.version' />", "version")
	];
	
	var tableSettings = po.buildDataTableSettingsAjax(tableColumns, po.url("queryData"));
	po.initDataTable(tableSettings);
	po.bindResizeDataTable();
})
(${pageId});
</script>
</body>
</html>

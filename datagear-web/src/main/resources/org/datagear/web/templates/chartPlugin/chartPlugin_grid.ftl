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
<#assign selectPageCss=(selectOperation?string('page-grid-select',''))>
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
<div id="${pageId}" class="page-grid page-grid-hidden-foot page-grid-chartPlugin">
	<div class="head">
		<div class="search">
			<#include "../include/page_obj_searchform.ftl">
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
<#include "../include/page_obj_grid.ftl">
<script type="text/javascript">
(function(po)
{
	po.initGridBtns();
	
	po.url = function(action)
	{
		return "${contextPath}/chartPlugin/" + action;
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

	var snColumn = $.buildDataTablesColumnSimpleOption("<@spring.message code='serialNumber' />", "id");
	snColumn.width="4.01em";
	snColumn.orderable=false;
	snColumn.render = function(data, type, row, meta)
	{
		if($.dataTableUtil.isDisplayType(type))
		{
			return (meta.row + 1);
		}
		else
		{
			return data;
		}
	};
	
	var iconColumn = $.buildDataTablesColumnSimpleOption("<@spring.message code='chartPlugin.icon' />", "iconUrl", false);
	iconColumn.render = function(data, type, row, meta)
	{
		if($.dataTableUtil.isDisplayType(type))
		{
			return (data ? "<a class=\"plugin-icon\" style=\"background-image: url(${contextPath}"+data+")\">&nbsp;</a>" : data);
		}
		else
			return data;
	};
	
	var tableColumns = [
		$.buildDataTablesColumnSimpleOption("<@spring.message code='id' />", "id", true),
		snColumn,
		$.buildDataTablesColumnSimpleOption($.buildDataTablesColumnTitleSearchable("<@spring.message code='chartPlugin.name' />"), "nameLabel.value"),
		$.buildDataTablesColumnSimpleOption("<@spring.message code='chartPlugin.version' />", "version"),
		iconColumn,
		$.buildDataTablesColumnSimpleOption($.buildDataTablesColumnTitleSearchable("<@spring.message code='chartPlugin.desc' />"), "descLabel.value")
	];
	
	var tableSettings = po.buildDataTableSettingsAjax(tableColumns, po.url("queryData"));
	tableSettings.ordering = false;
	po.initDataTable(tableSettings);
	po.bindResizeDataTable();
})
(${pageId});
</script>
</body>
</html>

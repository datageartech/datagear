<#include "../../include/import_global.ftl">
<#include "../../include/html_doctype.ftl">
<#--
titleMessageKey 标题标签I18N关键字，不允许null
selectOperation 是否选择操作，允许为null
boolean readonly 是否只读操作，默认为false
-->
<#assign selectOperation=(selectOperation!false)>
<#assign isMultipleSelect=(isMultipleSelect!false)>
<#assign readonly=(readonly!false)>
<#assign HtmlChartWidgetEntity=statics['org.datagear.management.domain.HtmlChartWidgetEntity']>
<html>
<head>
<#include "../../include/html_head.ftl">
<title><#include "../../include/html_title_app_name.ftl"><@spring.message code='${titleMessageKey}' /></title>
</head>
<body class="fill-parent">
<#if !isAjaxRequest>
<div class="fill-parent">
</#if>
<#include "../../include/page_js_obj.ftl">
<div id="${pageId}" class="page-grid page-grid-chart">
	<div class="head">
		<div class="search">
			<#include "../../include/page_obj_searchform_data_filter.ftl">
			<#include "../include/analysisProjectAware_grid_search.ftl">
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
				<input name="showButton" type="button" value="<@spring.message code='chart.show' />" />
				<#if !(currentUser.anonymous)>
				<input name="shareButton" type="button" value="<@spring.message code='share' />" />
				</#if>
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
<#include "../../include/page_obj_pagination.ftl">
<#include "../../include/page_obj_grid.ftl">
<#include "../../include/page_obj_data_permission.ftl" >
<script type="text/javascript">
(function(po)
{
	$.initButtons(po.element(".operation"));
	po.initDataFilter();

	po.currentUser = <@writeJson var=currentUser />;
	
	po.url = function(action)
	{
		return "${contextPath}/analysis/chart/" + action;
	};

	po.element("input[name=addButton]").click(function()
	{
		po.open(po.url("add"),
		{
			width: "85%",
			<#if selectOperation>
			pageParam:
			{
				afterSave: function(data)
				{
					return po.pageParamCallSelect(true, data);
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
			
			po.open(po.url("edit"), { width: "85%", data : data });
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
			po.open(contextPath+"/authorization/${HtmlChartWidgetEntity.AUTHORIZATION_RESOURCE_TYPE}/query?"
					+"${statics['org.datagear.web.controller.AuthorizationController'].PARAM_ASSIGNED_RESOURCE}="+encodeURIComponent(row.id), options);
		});
	});
	
	po.element("input[name=viewButton]").click(function()
	{
		po.executeOnSelect(function(row)
		{
			var data = {"id" : row.id};
			
			po.open(po.url("view"),
			{
				width: "85%",
				data : data
			});
		});
	});
	
	po.element("input[name=showButton]").click(function()
	{
		po.executeOnSelect(function(row)
		{
			window.open(po.url("show/"+row.id+"/"), row.id);
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
		<#if isMultipleSelect>
		po.executeOnSelects(function(rows)
		{
			po.pageParamCallSelect(true, rows);
		});
		<#else>
		po.executeOnSelect(function(row)
		{
			po.pageParamCallSelect(true, row);
		});
		</#if>
	});
	
	var updateIntervalColumn = $.buildDataTablesColumnSimpleOption("<@spring.message code='chart.updateInterval' />", "updateInterval");
	updateIntervalColumn.render = function(data)
	{
		if(data < 0)
			return "<@spring.message code='chart.updateInterval.none' />";
		else if(data == 0)
			return "<@spring.message code='chart.updateInterval.realtime' />";
		else
		{
			<#assign messageArgs=['"+data+"'] />
			return "<@spring.messageArgs code='chart.updateIntervalWithUnit' args=messageArgs />";
		}
	};
	
	var tableColumns = [
		$.buildDataTablesColumnSimpleOption($.buildDataTablesColumnTitleSearchable("<@spring.message code='id' />"), "id"),
		$.buildDataTablesColumnSimpleOption($.buildDataTablesColumnTitleSearchable("<@spring.message code='chart.name' />"), "name"),
		$.buildDataTablesColumnSimpleOption("<@spring.message code='chart.htmlChartPlugin' />", "chartPluginName"),
		updateIntervalColumn,
		$.buildDataTablesColumnSimpleOption($.buildDataTablesColumnTitleSearchable("<@spring.message code='analysisProject.ownerAnalysisProject' />"), "analysisProject.name"),
		$.buildDataTablesColumnSimpleOption("<@spring.message code='chart.createUser' />", "createUser.realName"),
		$.buildDataTablesColumnSimpleOption("<@spring.message code='chart.createTime' />", "createTime")
	];
	
	po.initPagination();
	
	var tableSettings = po.buildDataTableSettingsAjax(tableColumns, po.url("pagingQueryData"));
	tableSettings.order = [[$.getDataTableColumn(tableSettings, "createTime"), "desc"]];
	po.initDataTable(tableSettings);
	po.bindResizeDataTable();
})
(${pageId});
</script>
</body>
</html>

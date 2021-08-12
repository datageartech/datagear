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
<#assign Role=statics['org.datagear.management.domain.Role']>
<#--
titleMessageKey 标题标签I18N关键字，不允许null
selectOperation 是否选择操作，允许为null
boolean readonly 是否只读操作，默认为false
-->
<#assign selectOperation=(selectOperation!false)>
<#assign isMultipleSelect=(isMultipleSelect!false)>
<#assign readonly=(readonly!false)>
<#assign DataSetEntity=statics['org.datagear.management.domain.DataSetEntity']>
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
<#include "../include/page_obj_opt_permission.ftl" >
<div id="${pageId}" class="page-grid page-grid-dataSet">
	<div class="head">
		<div class="search">
			<#include "../include/page_obj_searchform_data_filter.ftl">
			<#include "../include/analysisProjectAware_grid_search.ftl">
		</div>
		<div class="operation" show-any-role="${Role.ROLE_DATA_ADMIN},${Role.ROLE_DATA_ANALYST}">
			<#if selectOperation>
				<input name="confirmButton" type="button" class="recommended" value="<@spring.message code='confirm' />" />
			</#if>
			<#if readonly>
				<input name="viewButton" type="button" value="<@spring.message code='view' />" />
			<#else>
				<div class="add-button-wrapper" show-any-role="${Role.ROLE_DATA_ADMIN}">
					<button class="add-button" type="button">
						<@spring.message code='add' />
						<span class="ui-icon ui-icon-triangle-1-s"></span>
					</button>
					<div class="add-button-panel ui-widget ui-widget-content ui-corner-all ui-widget-shadow ui-front">
						<ul class="add-button-list">
							<li addURL="addForSQL"><div><@spring.message code='dataSet.dataSetType.SQL' /></div></li>
							<li addURL="addForCsvValue"><div><@spring.message code='dataSet.dataSetType.CsvValue' /></div></li>
							<li addURL="addForCsvFile"><div><@spring.message code='dataSet.dataSetType.CsvFile' /></div></li>
							<li addURL="addForExcel"><div><@spring.message code='dataSet.dataSetType.Excel' /></div></li>
							<li addURL="addForHttp"><div><@spring.message code='dataSet.dataSetType.Http' /></div></li>
							<li addURL="addForJsonValue"><div><@spring.message code='dataSet.dataSetType.JsonValue' /></div></li>
							<li addURL="addForJsonFile"><div><@spring.message code='dataSet.dataSetType.JsonFile' /></div></li>
							<li class="ui-widget-header ui-menu-divider ui-widget-content"></li>
							<li addURL="copy"><div><@spring.message code='copy' /></div></li>
						</ul>
					</div>
				</div>
				<#if !selectOperation>
				<input name="editButton" type="button" value="<@spring.message code='edit' />" show-any-role="${Role.ROLE_DATA_ADMIN}" />
				</#if>
				<#if !selectOperation>
				<#if !(currentUser.anonymous)>
				<input name="shareButton" type="button" value="<@spring.message code='share' />" show-any-role="${Role.ROLE_DATA_ADMIN}" />
				</#if>
				</#if>
				<input name="viewButton" type="button" value="<@spring.message code='view' />" />
				<#if !selectOperation>
				<input name="deleteButton" type="button" value="<@spring.message code='delete' />" show-any-role="${Role.ROLE_DATA_ADMIN}" />
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
<#include "../include/page_obj_pagination.ftl">
<#include "../include/page_obj_grid.ftl">
<#include "../include/page_obj_data_permission.ftl" >
<script type="text/javascript">
(function(po)
{
	$.initButtons(po.element(".operation"));
	po.initDataFilter();

	po.currentUser = <@writeJson var=currentUser />;
	
	po.element(".add-button-list").menu(
	{
		select: function(event, ui)
		{
			var item = $(ui.item);
			
			var addURL = item.attr("addURL");
			
			if(addURL == "copy")
			{
				po.executeOnSelect(function(row)
				{
					var data = {"id" : row.id};
					
					po.open(po.url(addURL),
					{
						width: "85%",
						data : data,
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
			}
			else
			{
				po.open(po.url(addURL),
				{
					width: "85%",
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
			}
		}
	});
	
	po.url = function(action)
	{
		return "${contextPath}/dataSet/" + action;
	};

	po.element(".add-button").click(function()
	{
		po.element(".add-button-panel").toggle();
	});
	po.element(".add-button-wrapper").hover(function(){}, function()
	{
		po.element(".add-button-panel").hide();
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
			po.open(contextPath+"/authorization/${DataSetEntity.AUTHORIZATION_RESOURCE_TYPE}/" + row.id +"/query", options);
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
	
	var dataSetTypeColumn = $.buildDataTablesColumnSimpleOption("<@spring.message code='dataSet.dataSetType' />", "dataSetType");
	dataSetTypeColumn.render = function(data)
	{
		if("${DataSetEntity.DATA_SET_TYPE_SQL}" == data)
			return "<@spring.message code='dataSet.dataSetType.SQL' />";
		else if("${DataSetEntity.DATA_SET_TYPE_Excel}" == data)
			return "<@spring.message code='dataSet.dataSetType.Excel' />";
		else if("${DataSetEntity.DATA_SET_TYPE_CsvValue}" == data)
			return "<@spring.message code='dataSet.dataSetType.CsvValue' />";
		else if("${DataSetEntity.DATA_SET_TYPE_CsvFile}" == data)
			return "<@spring.message code='dataSet.dataSetType.CsvFile' />";
		else if("${DataSetEntity.DATA_SET_TYPE_JsonValue}" == data)
			return "<@spring.message code='dataSet.dataSetType.JsonValue' />";
		else if("${DataSetEntity.DATA_SET_TYPE_JsonFile}" == data)
			return "<@spring.message code='dataSet.dataSetType.JsonFile' />";
		else if("${DataSetEntity.DATA_SET_TYPE_Http}" == data)
			return "<@spring.message code='dataSet.dataSetType.Http' />";
		else
			return "";
	};
	
	var tableColumns = [
		$.buildDataTablesColumnSimpleOption("<@spring.message code='id' />", "id", true),
		$.buildDataTablesColumnSimpleOption($.buildDataTablesColumnTitleSearchable("<@spring.message code='dataSet.name' />"), "name"),
		dataSetTypeColumn,
		$.buildDataTablesColumnSimpleOption($.buildDataTablesColumnTitleSearchable("<@spring.message code='analysisProject.ownerAnalysisProject' />"), "analysisProject.name"),
		$.buildDataTablesColumnSimpleOption("<@spring.message code='dataSet.createUser' />", "createUser.realName"),
		$.buildDataTablesColumnSimpleOption("<@spring.message code='dataSet.createTime' />", "createTime")
	];
	
	po.initPagination();
	
	var tableSettings = po.buildDataTableSettingsAjax(tableColumns, po.url("pagingQueryData"));
	tableSettings.order = [[$.getDataTableColumn(tableSettings, "createTime"), "desc"]];
	po.initDataTable(tableSettings);
	po.bindResizeDataTable();
	po.handlePermissionElement();
})
(${pageId});
</script>
</body>
</html>

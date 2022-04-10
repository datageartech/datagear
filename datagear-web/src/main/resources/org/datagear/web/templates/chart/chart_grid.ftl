<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#include "../include/page_import.ftl">
<#include "../include/html_doctype.ftl">
<#assign Role=statics['org.datagear.management.domain.Role']>
<#--
titleMessageKey 标题标签I18N关键字，不允许null
selectOperation 是否选择操作，允许为null
boolean readonly 是否只读操作，默认为false
-->
<#assign selectOperation=(selectOperation!false)>
<#assign selectPageCss=(selectOperation?string('page-grid-select',''))>
<#assign isMultipleSelect=(isMultipleSelect!false)>
<#assign readonly=(readonly!false)>
<#assign HtmlChartWidgetEntity=statics['org.datagear.management.domain.HtmlChartWidgetEntity']>
<html>
<head>
<#include "../include/html_head.ftl">
<title><#include "../include/html_title_app_name.ftl"><@spring.message code='${titleMessageKey}' /></title>
</head>
<body class="fill-parent">
<#if !isAjaxRequest>
<div class="fill-parent">
</#if>
<#include "../include/page_obj.ftl">
<#include "../include/page_obj_opt_permission.ftl" >
<div id="${pageId}" class="page-grid ${selectPageCss} page-grid-chart">
	<div class="head">
		<div class="search">
			<#include "../include/page_obj_searchform_data_filter.ftl">
			<#include "../include/analysisProjectAware_grid_search.ftl">
		</div>
		<div class="operation" show-any-role="${Role.ROLE_DATA_ADMIN},${Role.ROLE_DATA_ANALYST}">
			<#if selectOperation>
				<button type="button" class="selectButton recommended"><@spring.message code='confirm' /></button>
			</#if>
			<#if readonly>
				<button type="button" class="viewButton"><@spring.message code='view' /></button>
			<#else>
				<div class="addGroup" show-any-role="${Role.ROLE_DATA_ADMIN}">
					<button type="button" class="addButton"><@spring.message code='add' /></button>
					<select class="addGroupSelect">
						<option value="copy"><@spring.message code='copy' /></option>
					</select>
				</div>
				<#if !selectOperation>
				<button type="button" class="editButton" show-any-role="${Role.ROLE_DATA_ADMIN}"><@spring.message code='edit' /></button>
				<div class="showGroup">
					<button type="button" class="showButton"><@spring.message code='chart.show' /></button>
					<select class="showGroupSelect">
						<option value="copyShowURL"><@spring.message code='copyShowURL' /></option>
					</select>
					<button type="button" class="copyShowURLDelegation" style="display:none;">&nbsp;</button>
				</div>
				<#if !(currentUser.anonymous)>
				<button type="button" class="shareButton" show-any-role="${Role.ROLE_DATA_ADMIN}"><@spring.message code='share' /></button>
				</#if>
				</#if>
				<button type="button" class="viewButton"><@spring.message code='view' /></button>
				<#if !selectOperation>
				<button type="button" class="deleteButton" show-any-role="${Role.ROLE_DATA_ADMIN}"><@spring.message code='delete' /></button>
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
	po.initGridBtns();
	po.initDataFilter();

	po.currentUser = <@writeJson var=currentUser />;
	
	po.url = function(action)
	{
		return "${contextPath}/chart/" + action;
	};
	
	po.buildShowURL = function(id)
	{
		return po.url("show/"+id+"/");
	};

	po.element(".addButton").click(function()
	{
		po.open(po.url("add"),
		{
			width: "85%",
			<#if selectOperation>
			pageParam:
			{
				submitSuccess: function(data)
				{
					po.pageParamCallSelect(true, data);
				}
			}
			</#if>
		});
	});

	po.element(".addGroupSelect").selectmenu(
	{
		appendTo: po.element(),
		position: { my: "right top", at: "right bottom+2" },
		classes:
		{
	          "ui-selectmenu-button": "ui-button-icon-only",
	          "ui-selectmenu-menu": "ui-widget-shadow ui-widget ui-widget-content add-group-selectmenu"
	    },
		select: function(event, ui)
    	{
    		var action = $(ui.item).attr("value");
    		
    		if(action == "copy")
    		{
    			po.executeOnSelect(function(row)
 				{
    				var id = row.id;
    				
    				po.open(po.url("copy"),
					{
						width: "85%",
						data: { id: id },
						<#if selectOperation>
						pageParam:
						{
							submitSuccess: function(data)
							{
								po.pageParamCallSelect(true, data);
							}
						}
						</#if>
					});
 				});
    		}
    	}
	});
	po.element(".addGroup").controlgroup();
	
	po.element(".editButton").click(function()
	{
		po.executeOnSelect(function(row)
		{
			var data = {"id" : row.id};
			
			po.open(po.url("edit"), { width: "85%", data : data });
		});
	});
	
	po.element(".shareButton").click(function()
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
			po.open(contextPath+"/authorization/${HtmlChartWidgetEntity.AUTHORIZATION_RESOURCE_TYPE}/" + row.id +"/query", options);
		});
	});
	
	po.element(".viewButton").click(function()
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

	po.element(".showButton").click(function()
	{
		po.executeOnSelect(function(row)
		{
			window.open(po.buildShowURL(row.id), row.id);
		});
	});
	
	po.element(".showGroupSelect").selectmenu(
	{
		appendTo: po.element(),
		position: { my: "right top", at: "right bottom+2" },
		classes:
		{
	          "ui-selectmenu-button": "ui-button-icon-only",
	          "ui-selectmenu-menu": "ui-widget-shadow ui-widget ui-widget-content"
	    },
		select: function(event, ui)
    	{
    		var action = $(ui.item).attr("value");
    		
    		if(action == "copyShowURL")
    		{
    			po._currentShowURL = "";
    			po.executeOnSelect(function(row)
 				{
    				po._currentShowURL = "${serverURL}" + po.buildShowURL(row.id);
    				po.element(".copyShowURLDelegation").click();
 				});
    		}
    	}
	});
	po.element(".showGroup").controlgroup();
	
	var copyShowURLButton = po.element(".copyShowURLDelegation");
	if(copyShowURLButton.length > 0)
	{
		var clipboard = new ClipboardJS(copyShowURLButton[0],
		{
			//需要设置container，不然在对话框中打开页面后复制不起作用
			container: po.element()[0],
			text: function(trigger)
			{
				return (po._currentShowURL || "");
			}
		});
		clipboard.on('success', function(e)
		{
			$.tipSuccess("<@spring.message code='copyToClipboardSuccess' />");
		});
	}
	
	po.element(".deleteButton").click(
	function()
	{
		po.executeOnSelects(function(rows)
		{
			po.confirmDeleteEntities(po.url("delete"), rows);
		});
	});
	
	po.element(".selectButton").click(function()
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
	
	//使用ID作为列数据，确保排序可用
	var chartPluginColumnData = "htmlChartPlugin.id";
	var chartPluginColumn = $.buildDataTablesColumnSimpleOption("<@spring.message code='chart.htmlChartPlugin' />", chartPluginColumnData);
	chartPluginColumn.render = function(data, type, row)
	{
		data = row.htmlChartPlugin;
		
		var content = (data.nameLabel ? data.nameLabel.value : data.id);
		if(!content)
			content = data.id;
		
		if(type == "display")
		{
			content = $.truncateIf(content);
			content = $.escapeHtml(content);
			
			if(data.iconUrl)
			{
				content = "<div class='plugin-icon' style='background-image:url(${contextPath}"+$.escapeHtml(data.iconUrl)+")'></div>"
							+ "<div class='plugin-name'>"+content+"</div>";
			}
		}
		
		return content;
	};
	
	var tableColumns = [
		$.buildDataTablesColumnSimpleOption($.buildDataTablesColumnTitleSearchable("<@spring.message code='id' />"), "id"),
		$.buildDataTablesColumnSimpleOption($.buildDataTablesColumnTitleSearchable("<@spring.message code='chart.name' />"), "name"),
		chartPluginColumn,
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
	po.handlePermissionElement();
})
(${pageId});
</script>
</body>
</html>

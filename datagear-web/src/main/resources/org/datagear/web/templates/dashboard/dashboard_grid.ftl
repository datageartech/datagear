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
-->
<#assign selectOperation=(selectOperation!false)>
<#assign selectPageCss=(selectOperation?string('page-grid-select',''))>
<#assign HtmlTplDashboardWidgetEntity=statics['org.datagear.management.domain.HtmlTplDashboardWidgetEntity']>
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
<div id="${pageId}" class="page-grid ${selectPageCss} page-grid-dashboard">
	<div class="head">
		<div class="search">
			<#include "../include/page_obj_searchform_data_filter.ftl">
			<#include "../include/analysisProjectAware_grid_search.ftl">
		</div>
		<div class="operation" show-any-role="${Role.ROLE_DATA_ADMIN},${Role.ROLE_DATA_ANALYST}">
			<#if selectOperation>
				<button type="button" class="selectButton recommended"><@spring.message code='confirm' /></button>
				<button type="button" class="viewButton"><@spring.message code='view' /></button>
			<#else>
				<div class="addGroup" show-any-role="${Role.ROLE_DATA_ADMIN}">
					<button type="button" class="addButton"><@spring.message code='add' /></button>
					<select class="addGroupSelect">
						<option value="addInNewWindow"><@spring.message code='addInNewWindow' /></option>
						<option value="copy"><@spring.message code='copy' /></option>
						<option value="copyInNewWindow"><@spring.message code='copyInNewWindow' /></option>
						<option value="import"><@spring.message code='import' /></option>
					</select>
				</div>
				<div class="editGroup" show-any-role="${Role.ROLE_DATA_ADMIN}">
					<button type="button" class="editButton"><@spring.message code='edit' /></button>
					<select class="editGroupSelect">
						<option value="editInNewWindow"><@spring.message code='editInNewWindow' /></option>
					</select>
				</div>
				<div class="showGroup">
					<button type="button" class="showButton"><@spring.message code='dashboard.show' /></button>
					<select class="showGroupSelect">
						<option value="copyShowURL"><@spring.message code='copyShowURL' /></option>
					</select>
					<button type="button" class="copyShowURLDelegation" style="display:none;">&nbsp;</button>
				</div>
				<#if !(currentUser.anonymous)>
				<div class="shareGroup" show-any-role="${Role.ROLE_DATA_ADMIN}">
					<button type="button" class="shareButton"><@spring.message code='share' /></button>
					<select class="shareGroupSelect">
						<option value="shareSet"><@spring.message code='dashboard.shareSet' /></option>
					</select>
				</div>
				</#if>
				<button type="button" class="viewButton"><@spring.message code='view' /></button>
				<button type="button" class="exportButton" show-any-role="${Role.ROLE_DATA_ADMIN}"><@spring.message code='export' /></button>
				<button type="button" class="deleteButton" show-any-role="${Role.ROLE_DATA_ADMIN}"><@spring.message code='delete' /></button>
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
	po.element(".addGroupSelect").selectmenu(
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
    		
    		if(action == "addInNewWindow")
				po.open(po.url("add"), {target: "_blank"});
    		else if(action == "copy" || action == "copyInNewWindow")
    		{
    			po.executeOnSelect(function(row)
  				{
    				var options = { data: { id: row.id } };
    				if(action == "copyInNewWindow")
    					options.target = "_blank";
    				
  					po.open(po.url("copy"), options);
  				});
    		}
    		else if(action == "import")
    			po.open(po.url("import"));
    	}
	});
	po.element(".addGroup").controlgroup();
	
	po.element(".editGroupSelect").selectmenu(
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
    		
    		if(action == "editInNewWindow")
    		{
    			po.executeOnSelect(function(row)
				{
					po.open(po.url("edit?id=" + row.id), {target: "_blank"});
				});
    		}
    	}
	});
	po.element(".editGroup").controlgroup();
	
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
	
	po.element(".shareGroupSelect").selectmenu(
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
    		
    		if(action == "shareSet")
    		{
    			po.executeOnSelect(function(row)
  				{
    				var options = { data: { id: row.id } };
  					po.open(po.url("shareSet"), options);
  				});
    		}
    	}
	});
	po.element(".shareGroup").controlgroup();
	
	po.initDataFilter();
	
	po.currentUser = <@writeJson var=currentUser />;
	
	po.url = function(action)
	{
		return "${contextPath}/dashboard/" + action;
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
			po.open(contextPath+"/authorization/${HtmlTplDashboardWidgetEntity.AUTHORIZATION_RESOURCE_TYPE}/" + row.id +"/query", options);
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
			var showUrl = po.buildShowURL(row.id);
			window.open(showUrl, row.id);
		});
	});
	
	po.element(".exportButton").click(function()
	{
		po.executeOnSelect(function(row)
		{
			var data = {"id" : row.id};
			
			po.open(po.url("export"), { target: "_blank", data : data });
		});
	});
	
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
		po.executeOnSelect(function(row)
		{
			po.pageParamCallSelect(true, row);
		});
	});
	
	var tableColumns = [
		$.buildDataTablesColumnSimpleOption($.buildDataTablesColumnTitleSearchable("<@spring.message code='id' />"), "id"),
		$.buildDataTablesColumnSimpleOption($.buildDataTablesColumnTitleSearchable("<@spring.message code='dashboard.name' />"), "name"),
		$.buildDataTablesColumnSimpleOption($.buildDataTablesColumnTitleSearchable("<@spring.message code='analysisProject.ownerAnalysisProject' />"), "analysisProject.name"),
		$.buildDataTablesColumnSimpleOption("<@spring.message code='dashboard.createUser' />", "createUser.realName"),
		$.buildDataTablesColumnSimpleOption("<@spring.message code='dashboard.createTime' />", "createTime")
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

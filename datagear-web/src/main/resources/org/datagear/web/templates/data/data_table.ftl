<#--
 *
 * Copyright 2018-present datagear.tech
 *
 * This file is part of DataGear.
 *
 * DataGear is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * DataGear is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with DataGear.
 * If not, see <https://www.gnu.org/licenses/>.
 *
-->
<#include "../include/page_import.ftl">
<#include "../include/html_doctype.ftl">
<html>
<head>
<#include "../include/html_head.ftl">
<title>
	<@spring.message code='module.data' />
	<#include "../include/html_request_action_suffix.ftl">
	<#include "../include/html_app_name_suffix.ftl">
</title>
</head>
<body class="p-card no-border h-screen m-0">
<#include "../include/page_obj.ftl">
<#include "include/data_page_obj.ftl">
<div id="${pid}" class="page page-manager page-table page-schemadata h-full flex flex-column overflow-auto">
	<div class="page-header grid grid-nogutter align-items-center p-1 flex-grow-0">
		<div class="col-12 flex align-items-center mb-1">
			<i class="pi pi-database text-color-secondary text-sm"></i>
			<div class="text-color-secondary text-sm ml-1">${schema.title}</div>
			<i class="pi pi-angle-right text-color-secondary text-sm mx-1"></i>
			<!--<i class="pi pi-file text-color-secondary text-sm"></i>-->
			<div class="text-color-secondary text-sm">${tableName}</div>
		</div>
		<div class="col-12" :class="pm.isSelectAction ? 'md:col-6' : 'md:col-4'">
			<#include "include/data_search_form.ftl">
		</div>
		<div class="operations col-12 flex gap-1 flex-wrap md:justify-content-end" :class="pm.isSelectAction ? 'md:col-6' : 'md:col-8'">
			<p-button label="<@spring.message code='confirm' />" @click="onSelect"
				v-if="pm.isSelectAction">
			</p-button>
			
			<p-button label="<@spring.message code='add' />" @click="onAdd"
				v-if="!pm.isReadonlyAction && pm.canEditTableData">
			</p-button>
			<p-button label="<@spring.message code='edit' />" @click="onEdit"
				v-if="!pm.isReadonlyAction && pm.canEditTableData">
			</p-button>
			<p-button label="<@spring.message code='view' />" @click="onView"
				:class="{'p-button-secondary': pm.isSelectAction}"
				v-if="pm.canReadTableData">
			</p-button>
			<p-button label="<@spring.message code='export' />" @click="onExport"
				v-if="!pm.isReadonlyAction && pm.canDeleteTableData">
			</p-button>
			<p-button label="<@spring.message code='delete' />" @click="onDelete"
				class="p-button-danger"
				v-if="!pm.isReadonlyAction && pm.canDeleteTableData">
			</p-button>
		</div>
	</div>
	<div class="page-content flex-grow-1 overflow-auto">
		<p-datatable :value="pm.items" :scrollable="true" scroll-height="flex"
			:paginator="pm.paginator" :paginator-template="pm.paginatorTemplate" :first="pm.pageRecordIndex"
			:rows="pm.rowsPerPage" :current-page-report-template="pm.pageReportTemplate"
			:rows-per-page-options="pm.rowsPerPageOptions" :loading="pm.loading"
			:lazy="true" :total-records="pm.totalRecords" @page="onPaginator($event)"
			sort-mode="multiple" :multi-sort-meta="pm.multiSortMeta" @sort="onSort($event)"
			:resizable-columns="true" column-resize-mode="expand"
			v-model:selection="pm.selectedItems" :selection-mode="pm.selectionMode" striped-rows>
			<p-column :selection-mode="pm.selectionMode" :frozen="true" class="col-check"></p-column>
			<p-column :field="col.name" v-for="col in pm.dbTable.columns" :header="col.name" :sortable="col.sortable && col.isSupported"
				:key="col.name" :class="{'text-500': !col.isSupported}">
				<template #body="slotProps">
					<div v-html="onRenderColumnValue(col, slotProps)"></div>
				</template>
			</p-column>
		</p-datatable>
	</div>
	<#include "../include/page_foot.ftl">
</div>
<#include "../include/page_manager.ftl">
<#include "../include/page_table.ftl">
<#include "../include/page_code_editor.ftl">
<#include "../include/page_sql_editor.ftl">
<script>
(function(po)
{
	po.isReloadTable = ("${(reloadTable!false)?string('true','false')}" == "true");
	
	po.queryResultBinaryPlaceholder = "${queryDefaultLOBRowMapper.binaryPlaceholder}";
	po.queryResultClobPlacholder = "${queryDefaultLOBRowMapper.clobPlaceholder}";
	po.queryResultSqlXmlPlaceholder = "${queryDefaultLOBRowMapper.sqlXmlPlaceholder}";
	
	po.getSqlEditorSchemaId = function()
	{
		return po.schemaId;
	};
	
	po.onDbTable(function(dbTable)
	{
		po.inflateEntityAction = function(action, entityOrArray)
		{
			var options = action.options;
			
			options.contentType = $.CONTENT_TYPE_JSON;
			var data = $.tableMeta.uniqueRecordData(dbTable, entityOrArray);
			options.data = $.extend(data, options.data);
		};
		
		po.setupTableDataPermission();
		po.setupSearchForm(dbTable);
		
		po.vuePageModel(
		{
			dbTable: dbTable
		});
		
		po.setupAjaxTable(po.dataUrl("pagingQueryData"),
		{
			multiSortMeta: []
		});
		
		po.vueMethod(
		{
			onAdd: function()
			{
				po.handleAddAction(po.dataUrl("add"));
			},
			
			onEdit: function()
			{
				po.handleOpenOfAction(po.dataUrl("edit"));
			},
			
			onView: function()
			{
				po.handleOpenOfAction(po.dataUrl("view"));
			},
			
			onExport: function()
			{
				var query = po.ajaxTableQuery();
				po.ajaxJson(po.dataUrl("getQuerySql"),
				{
					data: query,
					success: function(response)
					{
						var url = "/dataexchange/"+encodeURIComponent(po.schemaId)+"/export";
						url = $.addParam(url, "query", response.sql);
						po.openTableDialog(url);
					}
				});
			},
			
			onDelete: function()
			{
				po.handleDeleteAction(po.dataUrl("delete"));
			},
			
			onSelect: function()
			{
				po.handleSelectAction();
			},
			
			onRenderColumnValue: function(column, slotProps)
			{
				if(!column.isSupported)
					return "";
				
				var value = (slotProps.data ? slotProps.data[column.name] : "");
				
				if($.isEmptyValue(value))
					return "";
				
				if($.tableMeta.isBinaryColumn(column))
				{
					return "<div class='p-tag p-tag-warning inline-block opacity-60'>"+po.queryResultBinaryPlaceholder+"</div>";
				}
				else if($.tableMeta.isClobColumn(column))
				{
					return "<div class='p-tag p-tag-warning inline-block opacity-60'>"+po.queryResultClobPlacholder+"</div>";
				}
				else if($.tableMeta.isSqlxmlColumn(column))
				{
					return "<div class='p-tag p-tag-warning inline-block opacity-60'>"+po.queryResultSqlXmlPlaceholder+"</div>";
				}
				else
					return $.escapeHtml($.truncateIf(value));
			}
		});
		
		po.vueMount();
	},
	po.isReloadTable);
})
(${pid});
</script>
</body>
</html>
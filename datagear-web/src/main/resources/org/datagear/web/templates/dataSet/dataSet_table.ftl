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
<#assign DataSetEntity=statics['org.datagear.management.domain.DataSetEntity']>
<#include "../include/page_import.ftl">
<#include "../include/html_doctype.ftl">
<html>
<head>
<#include "../include/html_head.ftl">
<title>
	<@spring.message code='module.dataSet' />
	<#include "../include/html_request_action_suffix.ftl">
	<#include "../include/html_app_name_suffix.ftl">
</title>
</head>
<body class="p-card no-border h-screen m-0">
<#include "../include/page_obj.ftl">
<div id="${pid}" class="page page-manager page-table page-search-ap-aware h-full flex flex-column overflow-auto">
	<div class="page-header grid grid-nogutter align-items-center p-1 flex-grow-0">
		<div class="col-12 mb-1">
			<#include "../include/page_current_analysis_project.ftl">
		</div>
		<div class="col-12" :class="pm.isSelectAction ? 'md:col-6' : 'md:col-4'">
			<#include "../include/page_search_form_filter.ftl">
		</div>
		<div class="operations col-12 flex gap-1 flex-wrap md:justify-content-end" :class="pm.isSelectAction ? 'md:col-6' : 'md:col-8'">
			<p-button label="<@spring.message code='confirm' />" @click="onSelect" v-if="pm.isSelectAction"></p-button>
			
			<p-button label="<@spring.message code='add' />"
				icon="pi pi-chevron-down" icon-pos="right" aria-haspopup="true" aria-controls="${pid}addMenu"
				@click="onAddMenuToggle" v-if="!pm.isReadonlyAction">
			</p-button>
			<p-tieredmenu id="${pid}addMenu" ref="addMenuEle" :model="pm.addMenuItems" :popup="true" v-if="!pm.isReadonlyAction"></p-tieredmenu>
			
			<p-button label="<@spring.message code='edit' />" @click="onEdit" v-if="!pm.isReadonlyAction"></p-button>
			<p-button label="<@spring.message code='share' />" @click="onShare" v-if="!pm.isReadonlyAction"></p-button>
			<p-button label="<@spring.message code='view' />" @click="onView" :class="{'p-button-secondary': pm.isSelectAction}"></p-button>
			<p-button label="<@spring.message code='delete' />" @click="onDelete" class="p-button-danger" v-if="!pm.isReadonlyAction"></p-button>
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
			v-model:selection="pm.selectedItems" :selection-mode="pm.selectionMode" data-key="id" striped-rows>
			<p-column :selection-mode="pm.selectionMode" :frozen="true" class="col-check"></p-column>
			<p-column field="id" header="<@spring.message code='id' />" :hidden="true"></p-column>
			<p-column field="name" header="<@spring.message code='name' />" :sortable="true" class="col-name"></p-column>
			<p-column field="dataSetType" header="<@spring.message code='type' />" :sortable="true" class="col-name">
				<template #body="{data}">
					{{formatDataSetType(data)}}
				</template>
			</p-column>
			<p-column field="analysisProject.name" header="<@spring.message code='ownerProject' />" :sortable="true" class="col-name"></p-column>
			<p-column field="createUser.realName" header="<@spring.message code='createUser' />" :sortable="true" class="col-user"></p-column>
			<p-column field="createTime" header="<@spring.message code='createTime' />" :sortable="true" class="col-datetime col-last"></p-column>
		</p-datatable>
	</div>
	<#include "../include/page_foot.ftl">
</div>
<#include "../include/page_manager.ftl">
<#include "../include/page_table.ftl">
<script>
(function(po)
{
	po.setupAjaxTable("/dataSet/pagingQueryData",
	{
		multiSortMeta: [ {field: "createTime", order: -1} ]
	});
	
	po.formatDataSetType = function(data)
	{
		var type = data.dataSetType;
		
		if("${DataSetEntity.DATA_SET_TYPE_SQL}" == type)
			return "<@spring.message code='dataSetType.SQL' />";
		else if("${DataSetEntity.DATA_SET_TYPE_Excel}" == type)
			return "<@spring.message code='dataSetType.Excel' />";
		else if("${DataSetEntity.DATA_SET_TYPE_CsvValue}" == type)
			return "<@spring.message code='dataSetType.CsvValue' />";
		else if("${DataSetEntity.DATA_SET_TYPE_CsvFile}" == type)
			return "<@spring.message code='dataSetType.CsvFile' />";
		else if("${DataSetEntity.DATA_SET_TYPE_JsonValue}" == type)
			return "<@spring.message code='dataSetType.JsonValue' />";
		else if("${DataSetEntity.DATA_SET_TYPE_JsonFile}" == type)
			return "<@spring.message code='dataSetType.JsonFile' />";
		else if("${DataSetEntity.DATA_SET_TYPE_Http}" == type)
			return "<@spring.message code='dataSetType.Http' />";
		else
			return "";
	};
	
	po.vuePageModel(
	{
		addMenuItems:
		[
			{
				label: "<@spring.message code='dataSetType.SQL' />",
				command: function()
				{
					po.handleAddAction(po.addCurrentAnalysisProjectIdParam("/dataSet/add/${DataSetEntity.DATA_SET_TYPE_SQL}"), {width: "70vw"});
				}
			},
			{
				label: "<@spring.message code='dataSetType.Http' />",
				command: function()
				{
					po.handleAddAction(po.addCurrentAnalysisProjectIdParam("/dataSet/add/${DataSetEntity.DATA_SET_TYPE_Http}"), {width: "70vw"});
				}
			},
			{
				label: "<@spring.message code='file' />",
				items:
				[
					{
						label: "<@spring.message code='dataSetType.CsvFile' />",
						command: function()
						{
							po.handleAddAction(po.addCurrentAnalysisProjectIdParam("/dataSet/add/${DataSetEntity.DATA_SET_TYPE_CsvFile}"), {width: "70vw"});
						}
					},
					{
						label: "<@spring.message code='dataSetType.Excel' />",
						command: function()
						{
							po.handleAddAction(po.addCurrentAnalysisProjectIdParam("/dataSet/add/${DataSetEntity.DATA_SET_TYPE_Excel}"), {width: "70vw"});
						}
					},
					{
						label: "<@spring.message code='dataSetType.JsonFile' />",
						command: function()
						{
							po.handleAddAction(po.addCurrentAnalysisProjectIdParam("/dataSet/add/${DataSetEntity.DATA_SET_TYPE_JsonFile}"), {width: "70vw"});
						}
					}
				]
			},
			{
				label: "<@spring.message code='text' />",
				items:
				[
					{
						label: "<@spring.message code='dataSetType.CsvValue' />",
						command: function()
						{
							po.handleAddAction(po.addCurrentAnalysisProjectIdParam("/dataSet/add/${DataSetEntity.DATA_SET_TYPE_CsvValue}"), {width: "70vw"});
						}
					},
					{
						label: "<@spring.message code='dataSetType.JsonValue' />",
						command: function()
						{
							po.handleAddAction(po.addCurrentAnalysisProjectIdParam("/dataSet/add/${DataSetEntity.DATA_SET_TYPE_JsonValue}"), {width: "70vw"});
						}
					}
				]
			},
			{ separator: true },
			{
				label: "<@spring.message code='copy' />",
				command: function()
				{
					po.handleOpenOfAction("/dataSet/copy", {width: "70vw"});
				}
			}
		]
	});
	
	po.vueRef("addMenuEle", null);
	
	po.vueMethod(
	{
		formatDataSetType: function(data)
		{
			return po.formatDataSetType(data);
		},
		
		onAddMenuToggle: function(e)
		{
			po.vueUnref("addMenuEle").toggle(e);
		},
		
		onEdit: function()
		{
			po.handleOpenOfAction("/dataSet/edit", {width: "70vw"});
		},
		
		onView: function()
		{
			po.handleOpenOfAction("/dataSet/view", {width: "70vw"});
		},

		onShare: function()
		{
			po.executeOnSelect(function(entity)
			{
				po.openTableDialog("/authorization/${DataSetEntity.AUTHORIZATION_RESOURCE_TYPE}/"+encodeURIComponent(entity.id)+"/manage");
			});
		},
		
		onDelete: function()
		{
			po.handleDeleteAction("/dataSet/delete");
		},
		
		onSelect: function()
		{
			po.handleSelectAction();
		}
	});
})
(${pid});
</script>
<#include "../include/page_vue_mount.ftl">
</body>
</html>
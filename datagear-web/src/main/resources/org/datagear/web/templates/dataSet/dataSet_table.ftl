<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#assign DataSetEntity=statics['org.datagear.management.domain.DataSetEntity']>
<#include "../include/page_import.ftl">
<#include "../include/html_doctype.ftl">
<html>
<head>
<#include "../include/html_head.ftl">
<title>
	<#include "../include/html_app_name_prefix.ftl">
	<@spring.message code='module.dataSet' />
	<#include "../include/html_request_action_suffix.ftl">
</title>
</head>
<body class="p-card no-border">
<#include "../include/page_obj.ftl">
<div id="${pid}" class="page page-manager page-table page-search-ap-aware">
	<div class="page-header grid grid-nogutter align-items-center pb-2">
		<div class="col-12 mb-1">
			<#include "../include/page_current_analysis_project.ftl">
		</div>
		<div class="col-12" :class="pm.isSelectAction ? 'md:col-6' : 'md:col-4'">
			<#include "../include/page_search_form_filter.ftl">
		</div>
		<div class="h-opts col-12 text-right" :class="pm.isSelectAction ? 'md:col-6' : 'md:col-8'">
			<p-button label="<@spring.message code='confirm' />" @click="onSelect" v-if="pm.isSelectAction"></p-button>
			
			<p-button label="<@spring.message code='add' />"
				icon="pi pi-chevron-down" icon-pos="right" aria-haspopup="true" aria-controls="${pid}addMenu"
				@click="onAddMenuToggle" v-if="!pm.isReadonlyAction">
			</p-button>
			<p-menu id="${pid}addMenu" ref="addMenuEle" :model="pm.addMenuItems" :popup="true" v-if="!pm.isReadonlyAction"></p-menu>
			
			<p-button label="<@spring.message code='edit' />" @click="onEdit" v-if="!pm.isReadonlyAction"></p-button>
			<p-button label="<@spring.message code='share' />" @click="onShare" v-if="!pm.isReadonlyAction"></p-button>
			<p-button label="<@spring.message code='view' />" @click="onView" :class="{'p-button-secondary': pm.isSelectAction}"></p-button>
			<p-button label="<@spring.message code='delete' />" @click="onDelete" class="p-button-danger" v-if="!pm.isReadonlyAction"></p-button>
		</div>
	</div>
	<div class="page-content">
		<p-datatable :value="pm.items" :scrollable="true" scroll-height="flex"
			:paginator="pm.paginator" :paginator-template="pm.paginatorTemplate"
			:rows="pm.rowsPerPage" :current-page-report-template="pm.pageReportTemplate"
			:rows-per-page-options="pm.rowsPerPageOptions" :loading="pm.loading"
			:lazy="true" :total-records="pm.totalRecords" @page="onPaginator($event)"
			sort-mode="multiple" :multi-sort-meta="pm.multiSortMeta" @sort="onSort($event)"
			v-model:selection="pm.selectedItems" :selection-mode="pm.selectionMode" dataKey="id" striped-rows>
			<p-column :selection-mode="pm.selectionMode" :frozen="true" class="col-check"></p-column>
			<p-column field="id" header="<@spring.message code='id' />" :hidden="true"></p-column>
			<p-column field="name" header="<@spring.message code='name' />" :sortable="true" class="col-name"></p-column>
			<p-column field="dataSetType" header="<@spring.message code='type' />" :sortable="true" style="min-width:6em;">
				<template #body="{data}">
					{{formatDataSetType(data)}}
				</template>
			</p-column>
			<p-column field="analysisProject.name" header="<@spring.message code='ownerProject' />" :sortable="true" class="col-owner-project"></p-column>
			<p-column field="createUser.realName" header="<@spring.message code='createUser' />" :sortable="true" class="col-user"></p-column>
			<p-column field="createTime" header="<@spring.message code='createTime' />" :sortable="true" class="col-datetime col-last"></p-column>
		</p-datatable>
	</div>
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
	
	po.vuePageModel(
	{
		addMenuItems:
		[
			{
				label: "<@spring.message code='dataSetType.SQL' />",
				command: function()
				{
					po.handleAddAction("/dataSet/addForSQL", {width: "70vw"});
				}
			},
			{
				label: "<@spring.message code='dataSetType.CsvValue' />",
				command: function()
				{
					po.handleAddAction("/dataSet/addForCsvValue", {width: "70vw"});
				}
			},
			{
				label: "<@spring.message code='dataSetType.CsvFile' />",
				command: function()
				{
					po.handleAddAction("/dataSet/addForCsvFile", {width: "70vw"});
				}
			},
			{
				label: "<@spring.message code='dataSetType.Excel' />",
				command: function()
				{
					po.handleAddAction("/dataSet/addForExcel", {width: "70vw"});
				}
			},
			{
				label: "<@spring.message code='dataSetType.Http' />",
				command: function()
				{
					po.handleAddAction("/dataSet/addForHttp", {width: "70vw"});
				}
			},
			{
				label: "<@spring.message code='dataSetType.JsonValue' />",
				command: function()
				{
					po.handleAddAction("/dataSet/addForJsonValue", {width: "70vw"});
				}
			},
			{
				label: "<@spring.message code='dataSetType.JsonFile' />",
				command: function()
				{
					po.handleAddAction("/dataSet/addForJsonFile", {width: "70vw"});
				}
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
				po.openTableDialog("/authorization/${DataSetEntity.AUTHORIZATION_RESOURCE_TYPE}/"+encodeURIComponent(entity.id)+"/query");
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
	
	po.vueMount();
})
(${pid});
</script>
</body>
</html>
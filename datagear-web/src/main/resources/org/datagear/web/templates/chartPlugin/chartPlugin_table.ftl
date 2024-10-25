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
	<@spring.message code='module.chartPlugin' />
	<#include "../include/html_request_action_suffix.ftl">
	<#include "../include/html_app_name_suffix.ftl">
</title>
</head>
<body class="p-card no-border h-screen m-0">
<#include "../include/page_obj.ftl">
<div id="${pid}" class="page page-manager page-table h-full flex flex-column overflow-auto">
	<div class="page-header grid grid-nogutter align-items-center p-1 flex-grow-0">
		<div class="col-12" :class="pm.isSelectAction ? 'md:col-6' : 'md:col-4'">
			<#include "../include/page_search_form.ftl">
		</div>
		<div class="operations col-12 flex gap-1 flex-wrap md:justify-content-end" :class="pm.isSelectAction ? 'md:col-6' : 'md:col-8'">
			<p-button label="<@spring.message code='confirm' />" @click="onSelect" v-if="pm.isSelectAction"></p-button>
			
			<p-button label="<@spring.message code='upload' />" @click="onUpload" v-if="!pm.isReadonlyAction"></p-button>
			<p-button label="<@spring.message code='download' />" @click="onDownload" v-if="!pm.isSelectAction"></p-button>
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
			<!--
			<p-column field="number" header="<@spring.message code='serialNumber' />" :frozen="true" class="col-row-number">
				<template #body="slotProps">
					{{pm.pageRecordIndex + slotProps.index + 1}}
				</template>
			</p-column>
			-->
			<p-column field="id" header="<@spring.message code='id' />" :hidden="true"></p-column>
			<p-column field="order" header="<@spring.message code='order' />" :hidden="true"></p-column>
			<p-column field="nameLabel" header="<@spring.message code='name' />" class="col-name">
				<template #body="{data}">
					<div v-html="formatName(data)"></div>
				</template>
			</p-column>
			<p-column field="version" header="<@spring.message code='version' />" class="col-version"></p-column>
			<p-column field="platformVersion" header="<@spring.message code='platformVersionRequirement' />" class="col-version"></p-column>
			<p-column field="author" header="<@spring.message code='author' />" class="col-name"></p-column>
			<!-- 不重要信息，在查看操作里显示即可
			<p-column field="issueDate" header="<@spring.message code='issueDate' />" class="col-datetime"></p-column>
			-->
			<p-column field="descLabel.value" header="<@spring.message code='desc' />" class="col-last">
				<template #body="{data}">
					<div v-text="formatDesc(data)"></div>
				</template>
			</p-column>
		</p-datatable>
	</div>
	<#include "../include/page_foot.ftl">
</div>
<#include "../include/page_manager.ftl">
<#include "../include/page_table.ftl">
<script>
(function(po)
{
	po.setupAjaxTable("/chartPlugin/pagingQueryData",
	{
		multiSortMeta: [ {field: "order", order: 1} ]
	});
	
	po.vueMethod(
	{
		formatName: function(data)
		{
			return $.toChartPluginHtml(data, po.contextPath, { justifyContent: "start" });
		},
		
		formatDesc: function(data)
		{
			var desc = (data.descLabel ? data.descLabel.value : "");
			return $.truncateIf(desc);
		},
		
		onUpload: function()
		{
			po.handleAddAction("/chartPlugin/upload");
		},
		
		onDownload: function()
		{
			po.handleOpenOfsAction("/chartPlugin/download", { target: "_blank" });
		},
		
		onView: function()
		{
			po.handleOpenOfAction("/chartPlugin/view");
		},
		
		onDelete: function()
		{
			po.handleDeleteAction("/chartPlugin/delete");
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
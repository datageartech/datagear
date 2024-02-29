<#--
 *
 * Copyright 2018-2024 datagear.tech
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
	<@spring.message code='module.schema' />
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
			
			<p-button label="<@spring.message code='add' />" @click="onAdd" v-if="!pm.isReadonlyAction"></p-button>
			<p-button label="<@spring.message code='edit' />" @click="onEdit" v-if="!pm.isReadonlyAction"></p-button>
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
			<p-column field="title" header="<@spring.message code='name' />" :sortable="true" class="col-name"></p-column>
			<p-column field="url" header="<@spring.message code='url' />" :sortable="true" class="col-desc"></p-column>
			<p-column field="user" header="<@spring.message code='user' />" :sortable="true" class="col-user"></p-column>
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
	po.setupAjaxTable("/schema/pagingQueryData",
	{
		multiSortMeta: [ {field: "title", order: 1} ]
	});
	
	po.vueMethod(
	{
		onAdd: function()
		{
			po.handleAddAction("/schema/add");
		},
		
		onEdit: function()
		{
			po.handleOpenOfAction("/schema/edit");
		},
		
		onView: function()
		{
			po.handleOpenOfAction("/schema/view");
		},
		
		onDelete: function()
		{
			po.handleOpenOfsAction("/schema/delete");
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
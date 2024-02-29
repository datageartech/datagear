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
	<@spring.message code='module.schemaGuard' />
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
			<p-button label="<@spring.message code='test' />" @click="onTest" v-if="!pm.isSelectAction"></p-button>
			<p-button label="<@spring.message code='delete' />" @click="onDelete" class="p-button-danger" v-if="!pm.isReadonlyAction"></p-button>
		</div>
	</div>
	<div class="page-content flex-grow-1 overflow-auto">
		<p-datatable :value="pm.items" :scrollable="true" scroll-height="flex"
			:loading="pm.loading" :lazy="true"
			sort-mode="multiple" :multi-sort-meta="pm.multiSortMeta" @sort="onSort($event)"
			:resizable-columns="true" column-resize-mode="expand"
			v-model:selection="pm.selectedItems" :selection-mode="pm.selectionMode" data-key="id" striped-rows>
			<p-column :selection-mode="pm.selectionMode" :frozen="true" class="col-check"></p-column>
			<p-column field="id" header="<@spring.message code='id' />" :hidden="true"></p-column>
			<p-column field="name" header="<@spring.message code='name' />" class="col-name"></p-column>
			<p-column field="permitted" header="<@spring.message code='isPermit' />">
				<template #body="{data}">
					{{formatPermitted(data)}}
				</template>
			</p-column>
			<p-column field="priority" header="<@spring.message code='priority' />"></p-column>
			<p-column field="enabled" header="<@spring.message code='isEnable' />">
				<template #body="{data}">
					{{formatEnabled(data)}}
				</template>
			</p-column>
		</p-datatable>
	</div>
	<div class="flex-grow-0">
		<div class="flex justify-content-center align-items-center text-color-secondary">
			<small><@spring.message code='schemaGuard.table.desc' /></small>
		</div>
	</div>
	<#include "../include/page_foot.ftl">
</div>
<#include "../include/page_manager.ftl">
<#include "../include/page_table.ftl">
<#include "../include/page_boolean_options.ftl">
<script>
(function(po)
{
	po.setupAjaxTable("/schemaGuard/queryData",
	{
		multiSortMeta: []
	});
	
	po.vueMethod(
	{
		formatPermitted: function(data)
		{
			return po.formatBooleanValue(data.permitted);
		},
		formatEnabled: function(data)
		{
			return po.formatBooleanValue(data.enabled);
		},
		onAdd: function()
		{
			po.handleAddAction("/schemaGuard/add");
		},
		
		onEdit: function()
		{
			po.handleOpenOfAction("/schemaGuard/edit");
		},
		
		onView: function()
		{
			po.handleOpenOfAction("/schemaGuard/view");
		},
		
		onDelete: function()
		{
			po.handleDeleteAction("/schemaGuard/delete");
		},
		
		onSelect: function()
		{
			po.handleSelectAction();
		},
		
		onTest: function()
		{
			po.open("/schemaGuard/test");
		}
	});
})
(${pid});
</script>
<#include "../include/page_vue_mount.ftl">
</body>
</html>
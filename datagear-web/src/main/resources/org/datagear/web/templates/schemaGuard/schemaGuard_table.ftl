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
<html>
<head>
<#include "../include/html_head.ftl">
<title>
	<#include "../include/html_app_name_prefix.ftl">
	<@spring.message code='module.schemaGuard' />
	<#include "../include/html_request_action_suffix.ftl">
</title>
</head>
<body class="p-card no-border">
<#include "../include/page_obj.ftl">
<div id="${pid}" class="page page-manager page-table">
	<div class="page-header grid align-items-center">
		<div class="col-12 md:col-3">
			<#include "../include/page_search_form.ftl">
		</div>
		<div class="h-opts col-12 md:col-9 text-right">
			<p-button label="<@spring.message code='confirm' />" @click="onSelect" v-if="isSelectAction"></p-button>
			
			<p-button label="<@spring.message code='add' />" @click="onAdd" v-if="!isSelectAction"></p-button>
			<p-button label="<@spring.message code='edit' />" @click="onEdit" v-if="!isSelectAction"></p-button>
			<p-button label="<@spring.message code='view' />" @click="onView"></p-button>
			<p-button label="<@spring.message code='test' />" @click="onTest" v-if="!isSelectAction"></p-button>
			<p-button label="<@spring.message code='delete' />" @click="onDelete" class="p-button-danger" v-if="!isSelectAction"></p-button>
		</div>
	</div>
	<div class="page-content">
		<p-datatable :value="pm.items" :scrollable="true" scroll-height="flex"
			:loading="pm.loading" :lazy="true"
			sort-mode="multiple" :multi-sort-meta="pm.multiSortMeta" @sort="onSort($event)"
			v-model:selection="pm.selectedItems" :selection-mode="pm.selectionMode" dataKey="id" striped-rows>
			<p-column :selection-mode="pm.selectionMode" :frozen="true" class="col-check"></p-column>
			<p-column field="id" header="<@spring.message code='id' />" :hidden="true"></p-column>
			<p-column field="pattern" header="<@spring.message code='pattern' />" :sortable="true" class="col-name"></p-column>
			<p-column field="permitted" header="<@spring.message code='isPermit' />" :sortable="true">
				<template #body="{data}">
					{{formatPermitted(data)}}
				</template>
			</p-column>
			<p-column field="priority" header="<@spring.message code='priority' />" :sortable="true"></p-column>
			<p-column field="enabled" header="<@spring.message code='isEnable' />" :sortable="true">
				<template #body="{data}">
					{{formatEnabled(data)}}
				</template>
			</p-column>
		</p-datatable>
	</div>
</div>
<#include "../include/page_manager.ftl">
<#include "../include/page_table.ftl">
<#include "../include/page_boolean_options.ftl">
<script>
(function(po)
{
	po.setupAjaxTable("/schemaGuard/queryData",
	{
		multiSortMeta: [ {field: "priority", order: -1} ]
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
	
	po.vueMount();
})
(${pid});
</script>
</body>
</html>
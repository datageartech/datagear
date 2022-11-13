<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#assign Authorization=statics['org.datagear.management.domain.Authorization']>
<#include "../include/page_import.ftl">
<#include "../include/html_doctype.ftl">
<html>
<head>
<#include "../include/html_head.ftl">
<title>
	<@spring.message code='${resourceMeta.authModuleLabel}' />
	<#include "../include/html_app_name_suffix.ftl">
</title>
</head>
<body class="p-card no-border">
<#include "../include/page_obj.ftl">
<div id="${pid}" class="page page-manager page-table">
	<div class="page-header grid align-items-center">
		<div class="col-12" :class="pm.isSelectAction ? 'md:col-6' : 'md:col-4'">
			<#include "../include/page_search_form.ftl">
		</div>
		<div class="h-opts col-12 text-right" :class="pm.isSelectAction ? 'md:col-6' : 'md:col-8'">
			<p-button label="<@spring.message code='confirm' />" @click="onSelect" v-if="pm.isSelectAction"></p-button>
			
			<p-button label="<@spring.message code='add' />" @click="onAdd" v-if="!pm.isReadonlyAction"></p-button>
			<p-button label="<@spring.message code='edit' />" @click="onEdit" v-if="!pm.isReadonlyAction"></p-button>
			<p-button label="<@spring.message code='view' />" @click="onView" :class="{'p-button-secondary': pm.isSelectAction}"></p-button>
			<p-button label="<@spring.message code='delete' />" @click="onDelete" class="p-button-danger" v-if="!pm.isReadonlyAction"></p-button>
		</div>
	</div>
	<div class="page-content">
		<p-datatable :value="pm.items" :scrollable="true" scroll-height="flex"
			:loading="pm.loading" :lazy="true"
			sort-mode="multiple" :multi-sort-meta="pm.multiSortMeta" @sort="onSort($event)"
			v-model:selection="pm.selectedItems" :selection-mode="pm.selectionMode" dataKey="id" striped-rows>
			<p-column :selection-mode="pm.selectionMode" :frozen="true" class="col-check"></p-column>
			<p-column field="id" header="<@spring.message code='id' />" :hidden="true"></p-column>
			<p-column field="principalType" header="<@spring.message code='${resourceMeta.authPrincipalTypeLabel}' />"
				:sortable="true">
				<template #body="{data}">
					{{formatPrincipalType(data)}}
				</template>
			</p-column>
			<p-column field="principalName" header="<@spring.message code='${resourceMeta.authPrincipalLabel}' />"
				:sortable="true">
			</p-column>
			<p-column field="permissionLabel" header="<@spring.message code='${resourceMeta.authPermissionLabel}' />"
				:sortable="true" :hidden="pm.singlePermission">
			</p-column>
			<p-column field="enabled" header="<@spring.message code='${resourceMeta.authEnabledLabel}' />"
				:sortable="true" :hidden="!pm.enableSetEnable">
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
	po.enableSetEnable = ("${resourceMeta.enableSetEnable?string('true', 'false')}" == "true");
	po.singlePermission = ("${resourceMeta.singlePermission?string('true', 'false')}"  == "true");
	
	po.url = function(action)
	{
		return "/authorization/${resourceMeta.resourceType}/" + encodeURIComponent("${resource?js_string?no_esc}") + "/" + action;
	};
	
	po.vuePageModel(
	{
		enableSetEnable: po.enableSetEnable,
		singlePermission: po.singlePermission
	});
	
	po.setupAjaxTable(po.url("queryData"),
	{
		multiSortMeta: [ {field: "principalName", order: 1} ]
	});
	
	po.vueMethod(
	{
		formatPrincipalType: function(data)
		{
			var pt = data.principalType;
			
			if(pt == "${Authorization.PRINCIPAL_TYPE_USER}")
				return "<@spring.message code='authorization.principalType.USER' />";
			else if(pt == "${Authorization.PRINCIPAL_TYPE_ROLE}")
				return "<@spring.message code='authorization.principalType.ROLE' />";
			else if(pt == "${Authorization.PRINCIPAL_TYPE_ANONYMOUS}")
				return "<@spring.message code='authorization.principalType.ANONYMOUS' />";
			else if(pt == "${Authorization.PRINCIPAL_TYPE_ALL}")
				return "<@spring.message code='authorization.principalType.ALL' />";
			else
				return "";
		},
		formatEnabled: function(data)
		{
			return po.formatBooleanValue(data.enabled);
		},
		onAdd: function()
		{
			po.handleAddAction(po.url("add"));
		},
		
		onEdit: function()
		{
			po.handleOpenOfAction(po.url("edit"));
		},
		
		onView: function()
		{
			po.handleOpenOfAction(po.url("view"));
		},
		
		onDelete: function()
		{
			po.handleDeleteAction(po.url("delete"));
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
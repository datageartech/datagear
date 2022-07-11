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
	<@spring.message code='module.chart' />
	<#include "../include/html_request_action_suffix.ftl">
</title>
</head>
<body class="p-card no-border">
<#include "../include/page_obj.ftl">
<div id="${pid}" class="page page-table">
	<div class="page-table-header grid align-items-center">
		<div class="col-12 md:col-3">
			<#include "../include/page_search_form.ftl">
		</div>
		<div class="h-opts col-12 md:col-9 text-right">
			<p-button label="<@spring.message code='confirm' />" @click="onSelect" v-if="isSelectAction"></p-button>
			
			<p-button label="<@spring.message code='add' />" @click="onAdd" v-if="!isSelectAction"></p-button>
			<p-button label="<@spring.message code='edit' />" @click="onEdit" v-if="!isSelectAction"></p-button>
			<p-button label="<@spring.message code='view' />" @click="onView"></p-button>
			<p-button label="<@spring.message code='delete' />" @click="onDelete" class="p-button-danger" v-if="!isSelectAction"></p-button>
		</div>
	</div>
	<div class="page-table-content">
		<p-datatable :value="pm.items" :scrollable="true" scroll-height="flex"
			:paginator="pm.paginator" :paginator-template="pm.paginatorTemplate"
			:rows="pm.rowsPerPage" :current-page-report-template="pm.pageReportTemplate"
			:rows-per-page-options="pm.rowsPerPageOptions" :loading="pm.loading"
			:lazy="true" :total-records="pm.totalRecords" @page="onPaginator($event)"
			sort-mode="multiple" :multi-sort-meta="pm.multiSortMeta" @sort="onSort($event)"
			v-model:selection="pm.selectedItems" :selection-mode="pm.selectionMode" dataKey="id" striped-rows>
			<p-column :selection-mode="pm.selectionMode" :frozen="true" class="col-check"></p-column>
			<p-column field="id" header="<@spring.message code='id' />" class="col-id"></p-column>
			<p-column field="name" header="<@spring.message code='name' />" :sortable="true" class="col-name"></p-column>
			<p-column field="htmlChartPlugin.id" header="<@spring.message code='type' />" :sortable="true" style="min-width:8em;">
				<template #body="{data}">
					<div v-html="formatChartPlugin(data)"></div>
				</template>
			</p-column>
			<p-column field="updateInterval" header="<@spring.message code='updateInterval' />" :sortable="true" style="min-width:8em;max-width:15em;">
				<template #body="{data}">
					{{formatInterval(data)}}
				</template>
			</p-column>
			<p-column field="analysisProject.name" header="<@spring.message code='ownerProject' />" :sortable="true" class="col-owner-project"></p-column>
			<p-column field="createUser.realName" header="<@spring.message code='createUser' />" :sortable="true" class="col-user"></p-column>
			<p-column field="createTime" header="<@spring.message code='createTime' />" :sortable="true" class="col-datetime col-last"></p-column>
		</p-datatable>
	</div>
</div>
<#include "../include/page_table.ftl">
<script>
(function(po)
{
	po.setupAjaxTable("/chart/pagingQueryData",
	{
		multiSortMeta: [ {field: "createTime", order: -1} ]
	});
	
	po.vueMethod(
	{
		formatInterval: function(data)
		{
			var interval = data.updateInterval;
			
			if(interval < 0)
				return "<@spring.message code='noUpdate' />";
			else if(interval == 0)
				return "<@spring.message code='realtime' />";
			else
				return "<@spring.message code='xxxMilliseconds' />".replace("{0}", interval);
		},
		formatChartPlugin: function(data)
		{
			var cp = data.htmlChartPlugin;
			
			var content = (cp.nameLabel ? (cp.nameLabel.value || cp.id) : cp.id);
			content = $.escapeHtml(content);
			if(cp.iconUrl)
			{
				content = "<div class='plugin-info'><div class='plugin-icon' style='background-image:url(${contextPath}"+$.escapeHtml(cp.iconUrl)+")'></div>"
							+ content +" </div>";
			}
			
			return content;
		},
		onAdd: function()
		{
			po.handleAddAction("/chart/add");
		},
		
		onEdit: function()
		{
			po.handleOpenOfAction("/chart/edit");
		},
		
		onView: function()
		{
			po.handleOpenOfAction("/chart/view");
		},
		
		onDelete: function()
		{
			po.handleDeleteAction("/chart/delete");
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
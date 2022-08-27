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
	<@spring.message code='module.chartPlugin' />
	<#include "../include/html_request_action_suffix.ftl">
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
			
			<p-button label="<@spring.message code='upload' />" @click="onUpload" v-if="!pm.isSelectAction"></p-button>
			<p-button label="<@spring.message code='download' />" @click="onDownload" v-if="!pm.isSelectAction"></p-button>
			<p-button label="<@spring.message code='delete' />" @click="onDelete" class="p-button-danger" v-if="!pm.isSelectAction"></p-button>
		</div>
	</div>
	<div class="page-content">
		<p-datatable :value="pm.items" :scrollable="true" scroll-height="flex"
			:loading="pm.loading" :lazy="true"
			sort-mode="multiple" :multi-sort-meta="pm.multiSortMeta" @sort="onSort($event)"
			v-model:selection="pm.selectedItems" :selection-mode="pm.selectionMode" dataKey="id" striped-rows>
			<p-column :selection-mode="pm.selectionMode" :frozen="true" class="col-check"></p-column>
			<p-column field="number" header="<@spring.message code='serialNumber' />" :frozen="true" class="col-row-number">
				<template #body="slotProps">
					{{slotProps.index + 1}}
				</template>
			</p-column>
			<p-column field="id" header="<@spring.message code='id' />" :hidden="true"></p-column>
			<p-column field="order" header="<@spring.message code='order' />" :hidden="true"></p-column>
			<p-column field="nameLabel" header="<@spring.message code='name' />" class="col-name">
				<template #body="{data}">
					<div v-html="formatName(data)"></div>
				</template>
			</p-column>
			<p-column field="version" header="<@spring.message code='version' />" class="col-version"></p-column>
			<p-column field="descLabel.value" header="<@spring.message code='desc' />" class="col-desc" style="max-width:50em;"></p-column>
		</p-datatable>
	</div>
</div>
<#include "../include/page_manager.ftl">
<#include "../include/page_table.ftl">
<script>
(function(po)
{
	po.setupAjaxTable("/chartPlugin/queryData",
	{
		multiSortMeta: [ {field: "order", order: 1} ]
	});
	
	po.vueMethod(
	{
		formatName: function(data)
		{
			return $.toChartPluginHtml(data, po.contextPath);
		},
		
		onUpload: function()
		{
			po.handleAddAction("/chartPlugin/upload");
		},
		
		onDownload: function()
		{
			po.handleOpenOfsAction("/chartPlugin/download", { target: "_blank" });
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
	
	po.vueMount();
})
(${pid});
</script>
</body>
</html>
<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#include "include/page_import.ftl">
<#include "include/html_doctype.ftl">
<html>
<head>
<#include "include/html_head.ftl">
<title><#include "include/html_app_name_prefix.ftl">图表</title>
</head>
<body class="p-card no-border">
<#include "include/page_obj.ftl">
<div id="${pid}" class="page page-manager page-table">
	<div class="page-header grid align-items-center">
		<div class="col-12 md:col-3">
			<#include "include/page_search_form.ftl">
		</div>
		<div class="h-opts col-12 md:col-9 text-right">
			<p-button label="添加" @click="onAdd"></p-button>
			<p-button label="编辑" @click="onEdit"></p-button>
			<p-button label="查看" @click="onView"></p-button>
			<p-button label="删除" @click="onDelete" class="p-button-danger"></p-button>
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
			<p-column :selection-mode="pm.selectionMode" header-style="width:4rem" class="flex-grow-0"></p-column>
			<p-column field="id" header="ID" :sortable="true"></p-column>
			<p-column field="name" header="名称" :sortable="true"></p-column>
			<p-column field="createUser.realName" header="创建用户" :sortable="true"></p-column>
			<p-column field="createTime" header="创建时间" :sortable="true"></p-column>
		</p-datatable>
	</div>
</div>
<#include "include/page_table.ftl">
<script>
(function(po)
{
	po.setupAjaxTable("/chart/pagingQueryData",
	{
		multiSortMeta: [ {field: "createTime", order: -1} ]
	});
	
	po.vueMethod(
	{
		onAdd: function()
		{
			po.handleAddAction("/primevue/addChart");
		},
		
		onEdit: function()
		{
			po.handleOpenOfAction("/primevue/addChart");
		},
		
		onView: function()
		{
			po.handleOpenOfAction("/primevue/addChart");
		},
		
		onDelete: function()
		{
			po.handleDeleteAction("/primevue/addChart");
		}
	});
	
	po.vueMount();
})
(${pid});
</script>
</body>
</html>
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
<div id="${pid}" class="page-table">
	<div class="page-table-header grid align-items-center">
		<div class="col-12 md:col-3">
			<#include "include/page_search_form.ftl">
		</div>
		<div class="h-opts col-12 md:col-9 text-right">
			<p-button label="添加" @click="tableModel.handleAdd"></p-button>
			<p-button label="编辑" @click="tableModel.handleEdit"></p-button>
			<p-button label="查看" @click="tableModel.handleView"></p-button>
			<p-button label="删除" @click="tableModel.handleDelete" class="p-button-danger"></p-button>
		</div>
	</div>
	<div class="page-table-content">
		<p-datatable :value="tableModel.items" :scrollable="true" scroll-height="flex"
			:paginator="tableModel.paginator" :paginator-template="tableModel.paginatorTemplate"
			:rows="tableModel.rowsPerPage" :current-page-report-template="tableModel.pageReportTemplate"
			:rows-per-page-options="tableModel.rowsPerPageOptions" :loading="tableModel.loading"
			:lazy="true" :total-records="tableModel.totalRecords" @page="tableModel.handlePaginator($event)"
			sort-mode="multiple" :multi-sort-meta="tableModel.multiSortMeta" @sort="tableModel.handleSort($event)"
			v-model:selection="tableModel.selectedItems" :selection-mode="tableModel.selectionMode" dataKey="id" striped-rows>
			<p-column selection-mode="multiple" header-style="width:4rem" class="flex-grow-0"></p-column>
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
	var tableModel = po.setupAjaxTable("/chart/pagingQueryData",
	{
		multiSortMeta: [ {field: "createTime", order: -1} ]
	});
	
	tableModel.handleAdd = function()
	{
		po.handleAddAction("/primevue/addChart");
	};
	
	tableModel.handleEdit = function()
	{
		po.handleOpenOfAction("/primevue/addChart");
	};
	
	tableModel.handleView = function()
	{
		po.handleOpenOfAction("/primevue/addChart");
	};
	
	tableModel.handleDelete = function()
	{
		po.handleDeleteAction("/primevue/addChart");
	};
	
	po.vueMount();
})
(${pid});
</script>
</body>
</html>
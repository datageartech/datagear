<#include "include/page_import.ftl">
<#include "include/html_doctype.ftl">
<html>
<head>
<#include "include/html_head.ftl">
<title><#include "include/html_app_name_prefix.ftl">图表</title>
</head>
<body>
<#include "include/page_obj.ftl">
<div id="${pid}" class="page-table">
	<div class="page-table-header grid align-items-center">
		<div class="col-12 md:col-3">
			<#include "include/page_search_form.ftl">
		</div>
		<div class="h-opts col-12 md:col-9 text-right">
			<p-button label="添加" @click="openDialog"></p-button>
			<p-button label="编辑"></p-button>
			<p-button class="p-button-danger">删除</p-button>
		</div>
	</div>
	<div class="page-table-content">
		<p-datatable :value="tableItems" :scrollable="true" scroll-height="flex"
			:paginator="tablePaginator" :paginator-template="tablePaginatorTemplate"
			:rows="tableRowsPerPage" :current-page-report-template="tablePageReportTemplate"
			:rows-per-page-options="tableRowsPerPageOptions" :loading="tableLoading"
			:lazy="true" :total-records="tableTotalRecords" @page="tableHandlePaginator($event)"
			sort-mode="multiple" :multi-sort-meta="tableMultiSortMeta" @sort="tableHandleSort($event)"
			v-model:selection="tableSelectedItems" :selection-mode="tableSelectionMode" dataKey="id" striped-rows>
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
	po.vueSetup("openDialog", function()
	{
		po.open("/primevue/addChart");
	});
	
	po.setupAjaxTable("/chart/pagingQueryData",
	{
		multiSortMeta: [ {field: "createTime", order: -1} ]
	});
	
	po.vueMount();
})
(${pid});
</script>
</body>
</html>
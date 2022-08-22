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
	<@spring.message code='sqlHistory' />
</title>
</head>
<body class="p-card no-border">
<#include "../include/page_obj.ftl">
<div id="${pid}" class="page page-manager page-table">
	<div class="page-header grid align-items-center">
		<div class="col-12 md:col-6">
			<#include "../include/page_search_form.ftl">
		</div>
		<div class="h-opts col-12 md:col-6 text-right">
			<p-button label="<@spring.message code='insert' />" @click="onSelect"></p-button>
			<p-button label="<@spring.message code='copy' />" @click="onCopyToClipboard" class="p-button-secondary"></p-button>
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
			<p-column :selection-mode="pm.selectionMode" :frozen="true" class="col-check"></p-column>
			<p-column field="id" header="<@spring.message code='id' />" :hidden="true"></p-column>
			<p-column field="sql" header="<@spring.message code='sql' />" :sortable="true" style="max-width:100em;"></p-column>
			<p-column field="createTime" header="<@spring.message code='createTime' />" :sortable="true" class="col-datetime col-last"></p-column>
		</p-datatable>
	</div>
	<#include "../include/page_copy_to_clipboard.ftl">
</div>
<#include "../include/page_manager.ftl">
<#include "../include/page_table.ftl">
<script>
(function(po)
{
	po.schemaId = "${schemaId}";
	
	po.setupAjaxTable("/sqlpad/"+encodeURIComponent(po.schemaId)+"/sqlHistoryData",
	{
		multiSortMeta: [ {field: "createTime", order: -1} ]
	});
	
	po.buildClipboardContent = function(sqlHistories)
	{
		var buildCallback = po.pageParam("buildSqls");
		if(buildCallback)
		{
			return buildCallback(sqlHistories);
		}
		else
		{
			var delimiter = ";";
			
			var sql = "";
			
			$.each(sqlHistories, function(i, sh)
			{
				if(sh.sql)
					sql += sh.sql + delimiter + "\n";
			});
			
			return sql;
		}
	};
	
	po.vueMethod(
	{
		onSelect: function()
		{
			po.handleSelectAction();
		},
		
		onCopyToClipboard: function()
		{
			po.executeOnSelects(function(sqlHistories)
			{
				var content = po.buildClipboardContent(sqlHistories);
				po.copyToClipboard(content);
			});
		}
	});
	
	po.vueMount();
})
(${pid});
</script>
</body>
</html>
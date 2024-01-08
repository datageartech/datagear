<#--
 *
 * Copyright 2018-2023 datagear.tech
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
	<@spring.message code='sqlHistory' />
	<#include "../include/html_app_name_suffix.ftl">
</title>
</head>
<body class="p-card no-border h-screen m-0">
<#include "../include/page_obj.ftl">
<div id="${pid}" class="page page-manager page-table h-full flex flex-column overflow-auto">
	<div class="page-header grid grid-nogutter align-items-center p-1 flex-grow-0">
		<div class="col-12 md:col-6">
			<#include "../include/page_search_form.ftl">
		</div>
		<div class="operations col-12 md:col-6 flex gap-1 flex-wrap md:justify-content-end">
			<p-button label="<@spring.message code='insert' />" @click="onSelect"></p-button>
			<p-button label="<@spring.message code='copy' />" @click="onCopyToClipboard" class="p-button-secondary"></p-button>
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
			<p-column field="sql" header="<@spring.message code='sql' />" :sortable="true" class="col-desc"></p-column>
			<p-column field="createTime" header="<@spring.message code='createTime' />" :sortable="true" class="col-datetime col-last"></p-column>
		</p-datatable>
		<#include "../include/page_copy_to_clipboard.ftl">
	</div>
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
})
(${pid});
</script>
<#include "../include/page_vue_mount.ftl">
</body>
</html>
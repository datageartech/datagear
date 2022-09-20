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
	<@spring.message code='module.exportData' />
</title>
</head>
<body class="p-card no-border">
<#include "../include/page_obj.ftl">
<div id="${pid}" class="page page-manager page-dataexchange page-export-data-sql">
	<#include "include/export_head.ftl">
	<div class="page-content">
		<div class="page-form">
			<form id="${pid}form" action="#">
				<div class="page-form-content p-2 overflow-y-auto"
					:class="pm.steps.activeIndex == 0 ? '' : 'hidden'">
					<#include "include/dataexchange_data_format.ftl">
					<div class="field grid">
						<label for="${pid}nullForIllegalColumnValue" class="field-label col-12 mb-2 md:col-3 md:mb-0">
							<@spring.message code='dataExport.nullForIllegalColumnValue' />
						</label>
						<div class="field-input col-12 md:col-9">
							<p-selectbutton id="${pid}nullForIllegalColumnValue" v-model="fm.exportOption.nullForIllegalColumnValue"
								:options="pm.booleanOptions" option-label="name" option-value="value" class="input w-full">
					       	</p-selectbutton>
						</div>
					</div>
					<div class="field grid">
						<label for="${pid}exportCreationSql" class="field-label col-12 mb-2 md:col-3 md:mb-0">
							<@spring.message code='dataExport.exportCreationSql' />
						</label>
						<div class="field-input col-12 md:col-9">
							<p-selectbutton id="${pid}exportCreationSql" v-model="fm.exportOption.exportCreationSql"
								:options="pm.booleanOptions" option-label="name" option-value="value" class="input w-full">
					       	</p-selectbutton>
						</div>
					</div>
				</div>
				<div class="page-form-content p-2 overflow-y-auto"
					:class="pm.steps.activeIndex == 1 ? '' : 'hidden'">
					<#include "include/export_table_head.ftl">
					<div class="subdataexchange-table-wrapper pt-2">
						<p-datatable :value="fm.subDataExchanges" :scrollable="true" scroll-height="flex"
							v-model:selection="pm.selectedSubDataExchanges" selection-mode="multiple"
							:resizable-columns="true" column-resize-mode="expand"
							:sortable="false" dataKey="id" striped-rows class="table-sm">
							<p-column selection-mode="multiple" :frozen="true" class="col-check"></p-column>
							<p-column field="number" header="<@spring.message code='serialNumber' />" :frozen="true" class="col-row-number">
								<template #body="slotProps">
									{{slotProps.index + 1}}
								</template>
							</p-column>
							<p-column header="<@spring.message code='dataExport.tableNameOrQuery' />" style="min-width:16rem;max-width:30%;">
								<template #body="slotProps">
									<p-textarea v-model="slotProps.data.query" class="w-full"
										:disabled="pm.dataExchangeStatus != pm.DataExchangeStatusEnum.edit">
									</p-textarea>
								</template>
							</p-column>
							<p-column header="<@spring.message code='exportTableName' />" class="col-name">
								<template #body="slotProps">
									<p-inputtext v-model="slotProps.data.tableName" class="w-full"
										:disabled="pm.dataExchangeStatus != pm.DataExchangeStatusEnum.edit">
									</p-inputtext>
								</template>
							</p-column>
							<p-column header="<@spring.message code='exportFileName' />" class="col-name">
								<template #body="slotProps">
									<p-inputtext v-model="slotProps.data.fileName" class="w-full"
										:disabled="pm.dataExchangeStatus != pm.DataExchangeStatusEnum.edit">
									</p-inputtext>
								</template>
							</p-column>
							<p-column header="<@spring.message code='exportProgress' />" class="col-last" style="min-width:20rem;">
								<template #body="slotProps">
									<div v-html="slotProps.data.status"></div>
								</template>
							</p-column>
						</p-datatable>
					</div>
				</div>
				<#include "include/export_foot.ftl">
			</form>
		</div>
	</div>
</div>
<#include "../include/page_form.ftl">
<#include "../include/page_simple_form.ftl">
<#include "../include/page_boolean_options.ftl">
<#include "../include/page_format_time.ftl">
<#include "include/dataexchange_js.ftl">
<#include "include/export_js.ftl">
<script>
(function(po)
{
	po.submitUrl = "/dataexchange/"+encodeURIComponent(po.schemaId)+"/export/sql/doExport";

	po.postBuildSubDataExchange = function(subDataExchange)
	{
		subDataExchange.tableName = po.resolveTableName(subDataExchange.query);
	};
	
	po.handleExportFileNameExtension = function(fileName)
	{
		return fileName + ".sql";
	};
	
	po.checkSubmitSubDataExchange = function(subDataExchange, index)
	{
		return (po.checkSubmitSubDataExchangeQuery(subDataExchange, index)
				&& po.checkSubmitSubDataExchangeTableName(subDataExchange, index)
					&& po.checkSubmitSubDataExchangeFileName(subDataExchange, index));
	};
	
	var formModel = $.unescapeHtmlForJson(<@writeJson var=formModel />);
	po.setupDataExchangeForm(formModel);
	
	po.setupDataExchange();
	po.setupExport();
	po.setupExportHead("<@spring.message code='dataExport.exportSqlData' />");
	po.setupExportTableHead();
	
	po.vueMount();
})
(${pid});
</script>
</body>
</html>
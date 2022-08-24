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
	<@spring.message code='module.importData' />
</title>
</head>
<body class="p-card no-border">
<#include "../include/page_obj.ftl">
<div id="${pid}" class="page page-manager page-dataexchange page-import-data-csv">
	<#include "include/import_head.ftl">
	<div class="page-content">
		<div class="page-form">
			<form id="${pid}form" action="#">
				<div class="page-form-content p-2 overflow-y-auto"
					:class="pm.steps.activeIndex == 0 ? '' : 'hidden'">
					<#include "include/dataexchange_data_format.ftl">
					<#include "include/import_value_data_Import_option.ftl">
				</div>
				<div class="page-form-content p-2 overflow-y-auto"
					:class="pm.steps.activeIndex == 1 ? '' : 'hidden'">
					<#include "include/import_table_head.ftl">
					<div class="subdataexchange-table-wrapper pt-2">
						<p-datatable :value="pm.subDataExchanges" :scrollable="true" scroll-height="flex"
							v-model:selection="pm.selectedSubDataExchanges" selection-mode="multiple"
							:sortable="false" dataKey="subDataExchangeId" striped-rows class="table-sm">
							<p-column selection-mode="multiple" :frozen="true" class="col-check"></p-column>
							<p-column field="number" header="<@spring.message code='givenNumber' />" style="width:4rem">
							</p-column>
							<p-column field="displayName" header="<@spring.message code='fileName' />"></p-column>
							<p-column field="size" header="<@spring.message code='fileSize' />"></p-column>
							<p-column field="tableName" header="<@spring.message code='importTableName' />">
								<template #body="slotProps">
									<div class="px-2">
										<p-inputtext v-model="slotProps.data.tableName" class="w-full"></p-inputtext>
									</div>
								</template>
							</p-column>
							<p-column field="dependentNumber" header="<@spring.message code='dependentNumber' />">
								<template #body="slotProps">
									<div class="px-2">
										<p-inputtext v-model="slotProps.data.dependentNumber" class="w-full"
											placeholder="<@spring.message code='none' />">
										</p-inputtext>
									</div>
								</template>
							</p-column>
							<p-column field="importProgress" header="<@spring.message code='importProgress' />"></p-column>
						</p-datatable>
					</div>
				</div>
				<#include "include/import_foot.ftl">
			</form>
		</div>
	</div>
</div>
<#include "../include/page_form.ftl">
<#include "../include/page_simple_form.ftl">
<#include "../include/page_boolean_options.ftl">
<#include "include/dataexchange_js.ftl">
<#include "include/import_js.ftl">
<script>
(function(po)
{
	po.submitUrl = "/dataexchange/"+encodeURIComponent(po.schemaId)+"/import/csv/doImport";
	
	po.postBuildSubDataExchange = function(subDataExchange)
	{
		subDataExchange.dependentNumber = "<@spring.message code='auto' />";
	};
	
	po.beforeSubmitForm = function(action)
	{
		if(!po.isLastStep())
		{
			po.toNextStep();
			return false;
		}
	};
	
	po.uploadUrlHandler = function()
	{
		var fm = po.vueFormModel();
		var url = po.concatContextPath("/dataexchange/"+encodeURIComponent(po.schemaId)+"/import/csv/uploadImportFile");
		url = $.addParam(url, "dataExchangeId", po.dataExchangeId);
		url = $.addParam(url, "zipFileNameEncoding", fm.zipFileNameEncoding);
		
		return url;
	};
	
	var formModel = $.unescapeHtmlForJson(<@writeJson var=formModel />);
	po.setupForm(formModel);
	
	po.setupImportHead("<@spring.message code='dataImport.importCsvData' />");
	po.setupImportTableHead(
			po.concatContextPath("/dataexchange/"+encodeURIComponent(po.schemaId)+"/import/csv/uploadImportFile"),
			function(response)
			{
				po.addSubDataExchangesForFileInfos(response);
			});
	po.setupSteps(po.stepsItems, true);
	
	po.vueMount();
})
(${pid});
</script>
</body>
</html>
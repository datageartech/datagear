<#--
 *
 * Copyright 2018-present datagear.tech
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
	<@spring.message code='module.importData' />
	<#include "../include/html_app_name_suffix.ftl">
</title>
</head>
<body class="p-card no-border h-screen m-0">
<#include "../include/page_obj.ftl">
<div id="${pid}" class="page page-manager page-dataexchange page-import-data-json h-full flex flex-column overflow-auto">
	<#include "include/import_head.ftl">
	<div class="page-content flex-grow-1 overflow-auto">
		<div class="page-form h-full overflow-auto">
			<form id="${pid}form" action="#" class="h-full flex flex-column overflow-auto">
				<div class="page-form-content p-2 flex-grow-1 overflow-auto"
					:class="pm.steps.activeIndex == 0 ? '' : 'hidden'">
					<#include "include/dtbsSourceExchange_data_format.ftl">
					<#include "include/import_value_data_Import_option.ftl">
					<div class="field grid">
						<label for="${pid}jsonDataFormat" class="field-label col-12 mb-2 md:col-3 md:mb-0"
							title="<@spring.message code='dataExchange.JsonDataFormat.desc' />">
							<@spring.message code='jsonDataFormat' />
						</label>
						<div class="field-input col-12 md:col-9">
							<p-selectbutton id="${pid}jsonDataFormat" v-model="fm.importOption.jsonDataFormat"
								:options="pm.jsonDataFormatOptions" option-label="name" option-value="value" class="input w-full">
					       	</p-selectbutton>
						</div>
					</div>
				</div>
				<div class="page-form-content p-2 flex-grow-1 overflow-auto"
					:class="pm.steps.activeIndex == 1 ? 'flex flex-column' : 'hidden'">
					<#include "include/import_table_head.ftl">
					<div class="subdataexchange-table-wrapper flex-grow-1 overflow-auto pt-2">
						<p-datatable :value="fm.subDataExchanges" :scrollable="true" scroll-height="flex"
							v-model:selection="pm.selectedSubDataExchanges" selection-mode="multiple" :meta-key-selection="true"
							:resizable-columns="true" column-resize-mode="expand"
							:sortable="false" data-key="id" striped-rows class="table-sm">
							<p-column selection-mode="multiple" :frozen="true" class="col-check"></p-column>
							<p-column field="number" header="<@spring.message code='givenNumber' />" :frozen="true" class="col-row-number">
							</p-column>
							<p-column field="fileDisplayName" header="<@spring.message code='fileName' />" class="col-name"></p-column>
							<p-column field="fileSize" header="<@spring.message code='fileSize' />" class="col-version"></p-column>
							<p-column field="tableName" header="<@spring.message code='importTableName' />" class="col-name"
								v-if="fm.importOption.jsonDataFormat == pm.JsonDataFormat.ROW_ARRAY">
								<template #body="slotProps">
									<p-inputtext v-model="slotProps.data.tableName" class="w-full"
										:disabled="pm.dataExchangeStatus != pm.DataExchangeStatusEnum.edit">
									</p-inputtext>
								</template>
							</p-column>
							<p-column field="dependentNumber" header="<@spring.message code='dependentNumber' />" class="col-version">
								<template #body="slotProps">
									<p-inputtext v-model="slotProps.data.dependentNumber" class="w-full"
										:disabled="pm.dataExchangeStatus != pm.DataExchangeStatusEnum.edit"
										placeholder="<@spring.message code='none' />">
									</p-inputtext>
								</template>
							</p-column>
							<p-column header="<@spring.message code='importProgress' />" class="col-last" style="min-width:20rem;">
								<template #body="slotProps">
									<div v-html="slotProps.data.status"></div>
								</template>
							</p-column>
						</p-datatable>
					</div>
				</div>
				<#include "include/import_foot.ftl">
			</form>
		</div>
	</div>
	<#include "../include/page_foot.ftl">
</div>
<#include "../include/page_form.ftl">
<#include "../include/page_simple_form.ftl">
<#include "../include/page_boolean_options.ftl">
<#include "../include/page_format_time.ftl">
<#include "include/dtbsSourceExchange_js.ftl">
<#include "include/import_js.ftl">
<script>
(function(po)
{
	po.submitUrl = "/dtbsSourceExchange/"+encodeURIComponent(po.dtbsSourceId)+"/import/json/doImport";
	
	//org.datagear.dataexchange.support.JsonDataFormat
	po.JsonDataFormat =
	{
		TABLE_OBJECT: "TABLE_OBJECT",
		ROW_ARRAY: "ROW_ARRAY"
	};
	
	po.postBuildSubDataExchange = function(subDataExchange)
	{
		var fm = po.vueFormModel();
		
		if(fm.importOption && fm.importOption.jsonDataFormat == po.JsonDataFormat.ROW_ARRAY)
			subDataExchange.dependentNumber = fm.dependentNumberAuto;
	};
	
	po.checkSubmitSubDataExchange = function(subDataExchange, index, action)
	{
		var data = action.options.data;
		
		if(data.importOption.jsonDataFormat == po.JsonDataFormat.ROW_ARRAY)
			return po.checkSubmitSubDataExchangeTableName(subDataExchange);
	};
	
	var formModel = $.unescapeHtmlForJson(<@writeJson var=formModel />);
	po.setupDataExchangeForm(formModel);
	
	po.setupDataExchange();
	po.setupImportHead("<@spring.message code='dataImport.importJsonData' />");
	po.setupImportTableHead(
			po.concatContextPath("/dtbsSourceExchange/"+encodeURIComponent(po.dtbsSourceId)+"/import/json/uploadImportFile"),
			function(response)
			{
				po.addSubDataExchangesForFileInfos(response);
			},
			{
				uploadFileLabelDesc: "<@spring.message code='dataImport.uploadFile.json.desc' />",
			});
	
	po.vuePageModel(
	{
		JsonDataFormat: po.JsonDataFormat,
		jsonDataFormatOptions:
		[
			{
				name: "<@spring.message code='dataExchange.JsonDataFormat.TABLE_OBJECT' />",
				value: po.JsonDataFormat.TABLE_OBJECT
			},
			{
				name: "<@spring.message code='dataExchange.JsonDataFormat.ROW_ARRAY' />",
				value: po.JsonDataFormat.ROW_ARRAY
			}
		]
	});
})
(${pid});
</script>
<#include "../include/page_vue_mount.ftl">
</body>
</html>
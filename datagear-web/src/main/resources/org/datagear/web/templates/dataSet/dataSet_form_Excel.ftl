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
	<@spring.message code='module.dataSet.Excel' />
	<#include "../include/html_request_action_suffix.ftl">
</title>
</head>
<body class="p-card no-border">
<#include "../include/page_obj.ftl">
<div id="${pid}" class="page page-form horizontal page-form-dataSet  page-form-dataSet-Excel">
	<form id="${pid}form" class="flex flex-column" :class="{readonly: pm.isReadonlyAction}">
		<div class="page-form-content flex-grow-1 px-2 py-1 overflow-y-auto">
			<#include "include/dataSet_form_name.ftl">
			<#include "include/dataSet_form_file_source.ftl">
			<div class="field grid">
				<label for="${pid}sheetName" class="field-label col-12 mb-2 md:col-3 md:mb-0"
					title="<@spring.message code='excelDataSet.sheetName.desc' />">
					<@spring.message code='sheetName' />
				</label>
				<div class="field-input col-12 md:col-9">
					<p-inputtext id="${pid}sheetName" v-model="fm.sheetName" type="text" class="input w-full"
						name="sheetName" maxlength="200">
					</p-inputtext>
				</div>
			</div>
			<div class="field grid" v-if="pm.enableSheetIndex">
				<label for="${pid}sheetIndex" class="field-label col-12 mb-2 md:col-3 md:mb-0"
					title="<@spring.message code='excelDataSet.sheetIndex.desc' />">
					<@spring.message code='sheetIndex' />
				</label>
				<div class="field-input col-12 md:col-9">
					<p-inputtext id="${pid}sheetIndex" v-model="fm.sheetIndex" type="text" class="input w-full"
						name="sheetIndex" maxlength="10">
					</p-inputtext>
				</div>
			</div>
			<div class="field grid">
				<label for="${pid}nameRow" class="field-label col-12 mb-2 md:col-3 md:mb-0"
					title="<@spring.message code='dataSet.nameRowNumber.desc' />">
					<@spring.message code='titleRowNumber' />
				</label>
				<div class="field-input col-12 md:col-9">
					<p-inputtext id="${pid}nameRow" v-model="fm.nameRow" type="text" class="input w-full"
						name="nameRow" required maxlength="10">
					</p-inputtext>
				</div>
			</div>
			<div class="field grid">
				<label for="${pid}dataRowExp" class="field-label col-12 mb-2 md:col-3 md:mb-0"
					title="<@spring.message code='excelDataSet.dataRowExp.desc' />">
					<@spring.message code='dataRowRange' />
				</label>
				<div class="field-input col-12 md:col-9">
					<p-inputtext id="${pid}dataRowExp" v-model="fm.dataRowExp" type="text" class="input w-full"
						name="dataRowExp" maxlength="100">
					</p-inputtext>
				</div>
			</div>
			<div class="field grid">
				<label for="${pid}dataColumnExp" class="field-label col-12 mb-2 md:col-3 md:mb-0"
					title="<@spring.message code='excelDataSet.dataColumnExp.desc' />">
					<@spring.message code='dataColumnRange' />
				</label>
				<div class="field-input col-12 md:col-9">
					<p-inputtext id="${pid}dataColumnExp" v-model="fm.dataColumnExp" type="text" class="input w-full"
						name="dataColumnExp" maxlength="100">
					</p-inputtext>
				</div>
			</div>
			<div class="field grid">
				<label for="${pid}forceXls" class="field-label col-12 mb-2 md:col-3 md:mb-0">
					<@spring.message code='forceXlsFormat' />
				</label>
				<div class="field-input col-12 md:col-9">
					<p-selectbutton v-model="fm.forceXls" :options="pm.booleanOptions"
						option-label="name" option-value="value" class="input w-full">
					</p-selectbutton>
				</div>
			</div>
			<#include "include/dataSet_form_param_property.ftl">
		</div>
		<div class="page-form-foot flex-grow-0 pt-3 text-center h-opts">
			<#include "include/dataSet_form_preview.ftl">
			<p-button type="submit" label="<@spring.message code='save' />"></p-button>
		</div>
	</form>
</div>
<#include "../include/page_form.ftl">
<#include "../include/page_boolean_options.ftl">
<#include "include/dataSet_form_js.ftl">
<script>
(function(po)
{
	po.submitUrl = function()
	{
		var url = "/dataSet/"+po.submitAction;
		url = $.addParam(url, "originalFileName", po.originalFileName);
		
		return url;
	};
	
	po.previewUrl = function()
	{
		var url = "/dataSet/previewExcel";
		url = $.addParam(url, "originalFileName", po.originalFileName);
		
		return url;
	};
	
	po.inflatePreviewFingerprint = function(fingerprint, dataSet)
	{
		fingerprint.fileSourceType = dataSet.fileSourceType;
		fingerprint.fileName = dataSet.fileName;
		fingerprint.dataSetResDirectoryId = dataSet.dataSetResDirectory.id;
		fingerprint.dataSetResFileName = dataSet.dataSetResFileName;
		fingerprint.sheetName = dataSet.sheetName;
		fingerprint.sheetIndex = dataSet.sheetIndex;
		fingerprint.nameRow = dataSet.nameRow;
		fingerprint.dataRowExp = dataSet.dataRowExp;
		fingerprint.dataColumnExp = dataSet.dataColumnExp;
		fingerprint.forceXls = dataSet.forceXls;
	};
	
	po.beforeSubmitForm = function(action)
	{
		if(!po.beforeSubmitFormWithPreview(action))
			return false;
	};
	
	var formModel = $.unescapeHtmlForJson(<@writeJson var=formModel />);
	formModel.dataSetResDirectory = (!formModel.dataSetResDirectory ? {} : formModel.dataSetResDirectory);
	po.inflateDataSetModel(formModel);
	
	po.originalFileName = (formModel.fileName || "");
	
	po.setupForm(formModel, {},
	{
		rules:
		{
			"sheetIndex": {"integer": true},
			"nameRow": {"integer": true},
		},
		invalidHandler: function()
		{
			po.handlePreviewInvalidForm();
		}
	});
	
	po.vuePageModel(
	{
		originalFileName: po.originalFileName,
		//用于兼容旧版本的工作表序号功能，参考AbstractExcelDataSet.getSheetIndex()
		enableSheetIndex: (formModel.sheetIndex != null && formModel.sheetIndex > 0)
	});
	
	po.vueMount();
})
(${pid});
</script>
</body>
</html>
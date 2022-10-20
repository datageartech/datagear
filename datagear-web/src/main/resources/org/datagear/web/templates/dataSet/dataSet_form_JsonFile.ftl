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
	<@spring.message code='module.dataSet.JsonFile' />
	<#include "../include/html_request_action_suffix.ftl">
</title>
</head>
<body class="p-card no-border">
<#include "../include/page_obj.ftl">
<div id="${pid}" class="page page-form horizontal page-form-dataSet  page-form-dataSet-JsonFile">
	<form id="${pid}form" class="flex flex-column show-foot" :class="{readonly: pm.isReadonlyAction}">
		<div class="page-form-content flex-grow-1 px-2 py-1 overflow-y-auto">
			<#include "include/dataSet_form_name.ftl">
			<#include "include/dataSet_form_file_source.ftl">
			<div class="field grid">
				<label for="${pid}dataJsonPath" class="field-label col-12 mb-2 md:col-3 md:mb-0"
					title="<@spring.message code='jsonFileDataSet.dataJsonPath.desc' />">
					<@spring.message code='dataJsonPath' />
				</label>
				<div class="field-input col-12 md:col-9">
					<p-inputtext id="${pid}dataJsonPath" v-model="fm.dataJsonPath" type="text" class="input w-full"
						name="dataJsonPath" maxlength="200">
					</p-inputtext>
				</div>
			</div>
			<#include "include/dataSet_form_encoding.ftl">
			<#include "include/dataSet_form_param_property.ftl">
		</div>
		<div class="page-form-foot flex-grow-0 pt-3 text-center h-opts">
			<#include "include/dataSet_form_preview.ftl">
			<p-button type="submit" label="<@spring.message code='save' />" class="hide-if-readonly"></p-button>
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
		var url = "/dataSet/previewJsonFile";
		url = $.addParam(url, "originalFileName", po.originalFileName);
		
		return url;
	};
	
	po.inflatePreviewFingerprint = function(fingerprint, dataSet)
	{
		fingerprint.fileSourceType = dataSet.fileSourceType;
		fingerprint.fileName = dataSet.fileName;
		fingerprint.dataSetResDirectoryId = dataSet.dataSetResDirectory.id;
		fingerprint.dataSetResFileName = dataSet.dataSetResFileName;
		fingerprint.dataJsonPath = dataSet.dataJsonPath;
		fingerprint.encoding = dataSet.encoding;
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
	
	po.setupForm(formModel,
	{
		//查看页面[预览]需要禁用此项
		ignoreIfViewAction: false
	},
	{
		invalidHandler: function()
		{
			po.handlePreviewInvalidForm();
		}
	});
	
	po.vuePageModel(
	{
		originalFileName: po.originalFileName
	});
	
	po.vueMount();
})
(${pid});
</script>
</body>
</html>
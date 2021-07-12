<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#include "../include/import_global.ftl">
<#include "../include/html_doctype.ftl">
<#--
titleMessageKey 标题标签I18N关键字，不允许null
formAction 表单提交action，允许为null
readonly 是否只读操作，允许为null
-->
<#assign formAction=(formAction!'#')>
<#assign readonly=(readonly!false)>
<#assign isAdd=(formAction == 'saveAddForJsonFile')>
<html>
<head>
<#include "../include/html_head.ftl">
<title><#include "../include/html_title_app_name.ftl">
	<@spring.message code='${titleMessageKey}' /> - <@spring.message code='dataSet.dataSetType.JsonFile' />
</title>
</head>
<body>
<#include "../include/page_js_obj.ftl" >
<div id="${pageId}" class="page-form page-form-dataSet page-form-dataSet-jsonFile">
	<form id="${pageId}-form" action="#" method="POST">
		<div class="form-head"></div>
		<div class="form-content">
			<#include "include/dataSet_form_html_name.ftl">
			<div class="workspace">
				<div class="form-item">
					<div class="form-item-label">
						<label><@spring.message code='dataSet.jsonFile' /></label>
					</div>
					<div class="form-item-value form-item-value-file-input">
						<#include "include/dataSet_form_file_input.ftl">
					</div>
				</div>
				<div class="form-item">
					<div class="form-item-label">
						<label title="<@spring.message code='dataSet.jsonFile.dataJsonPath.desc' />">
							<@spring.message code='dataSet.jsonFile.dataJsonPath' />
						</label>
					</div>
					<div class="form-item-value error-newline">
						<input type="text" name="dataJsonPath" value="${(dataSet.dataJsonPath)!''}" class="ui-widget ui-widget-content" />
					</div>
				</div>
				<div class="form-item form-item-encoding">
					<div class="form-item-label">
						<label><@spring.message code='dataSet.jsonFileEncoding' /></label>
					</div>
					<div class="form-item-value">
						<select name="encoding">
							<#list availableCharsetNames as item>
							<option value="${item}" <#if item == dataSet.encoding>selected="selected"</#if>>${item}</option>
							</#list>
						</select>
					</div>
				</div>
				<#include "include/dataSet_form_html_wow.ftl" >
			</div>
		</div>
		<div class="form-foot" style="text-align:center;">
			<#if !readonly>
			<input type="submit" value="<@spring.message code='save' />" class="recommended" />
			<#else>
			<div class="form-foot-placeholder">&nbsp;</div>
			</#if>
		</div>
	</form>
	<#include "include/dataSet_form_html_preview_pvp.ftl" >
</div>
<#include "../include/page_obj_form.ftl">
<#include "include/dataSet_form_js.ftl">
<script type="text/javascript">
(function(po)
{
	po.dataSetProperties = <@writeJson var=dataSetProperties />;
	po.dataSetParams = <@writeJson var=dataSetParams />;
	
	$.initButtons(po.element());
	po.initAnalysisProject("${((dataSet.analysisProject.id)!'')?js_string?no_esc}", "${((dataSet.analysisProject.name)!'')?js_string?no_esc}");
	po.element("select[name='encoding']").selectmenu({ appendTo : po.element(), classes : { "ui-selectmenu-menu" : "encoding-selectmenu-menu" } });
	po.initWorkspaceHeight();
	po.initWorkspaceTabs(true);
	po.initDataSetPropertiesTable(po.dataSetProperties);
	po.initDataSetParamsTable(po.dataSetParams);
	po.initPreviewParamValuePanel();
	
	po.initDataSetFileInput(po.url("uploadFile"), "${((dataSet.fileSourceType)!'')?js_string?no_esc}", ${isAdd?string("true", "false")});
	
	po.updatePreviewOptionsData = function()
	{
		var dataSet = po.previewOptions.data.dataSet;

		dataSet.fileSourceType = po.fileSourceTypeValue();
		dataSet.fileName = po.element("input[name='fileName']").val();
		dataSet.dataSetResDirectory = {};
		dataSet.dataSetResDirectory.id = po.element("input[name='dataSetResDirectory.id']").val();
		dataSet.dataSetResDirectory.directory = po.element("input[name='dataSetResDirectory.directory']").val();
		dataSet.dataSetResFileName = po.element("input[name='dataSetResFileName']").val();
		dataSet.dataJsonPath = po.element("input[name='dataJsonPath']").val();
		dataSet.encoding = po.element("select[name='encoding']").val();
		
		po.previewOptions.data.originalFileName = po.element("#${pageId}-originalFileName").val();
	};
	
	<#if !isAdd>
	//编辑、查看操作应初始化为已完成预览的状态
	po.updatePreviewOptionsData();
	po.previewSuccess(true);
	</#if>
	
	po.isPreviewValueModified = function()
	{
		var fileSourceType = po.fileSourceTypeValue();
		var dataSetResDirectoryId = po.element("input[name='dataSetResDirectory.id']").val();
		var dataSetResFileName = po.element("input[name='dataSetResFileName']").val();
		var fileName = po.element("input[name='fileName']").val();
		var dataJsonPath = po.element("input[name='dataJsonPath']").val();
		var encoding = po.element("select[name='encoding']").val();
		
		var pd = po.previewOptions.data.dataSet;
		var dataSetResDirectory = (pd.dataSetResDirectory || {});
		
		return (pd.fileSourceType != fileSourceType)
				|| (po.isFileSourceTypeUpload(fileSourceType) && pd.fileName != fileName)
				|| (po.isFileSourceTypeServer(fileSourceType) && (dataSetResDirectory.id != dataSetResDirectoryId || pd.dataSetResFileName != dataSetResFileName))
				|| (pd.dataJsonPath != dataJsonPath) || (pd.encoding != encoding);
	};
	
	po.previewOptions.url = po.url("previewJsonFile");
	po.previewOptions.beforePreview = function()
	{
		po.updatePreviewOptionsData();
		return po.isPreviewDataFileValid(this.data);
	};
	po.previewOptions.beforeRefresh = function()
	{
		return po.isPreviewDataFileValid(this.data);
	};
	
	po.initPreviewOperations();
	
	po.form().validate(
	{
		ignore : "",
		rules :
		{
			"name" : "required",
			"displayName" :
			{
				"dataSetUploadFileNameRequired": true,
				"dataSetUploadFilePreviewRequired": true,
				"dataSetUploadFilePropertiesRequired": true
			},
			"dataSetResDirectory.directory":
			{
				"dataSetServerDirectoryRequired": true,
			},
			"dataSetResFileName":
			{
				"dataSetServerFileNameRequired": true,
				"dataSetServerFilePreviewRequired": true,
				"dataSetServerFilePropertiesRequired": true
			},
		},
		messages :
		{
			"name" : "<@spring.message code='validation.required' />",
			"displayName" :
			{
				"dataSetUploadFileNameRequired": "<@spring.message code='validation.required' />",
				"dataSetUploadFilePreviewRequired": "<@spring.message code='dataSet.validation.previewRequired' />",
				"dataSetUploadFilePropertiesRequired": "<@spring.message code='dataSet.validation.propertiesRequired' />"
			},
			"dataSetResDirectory.directory":
			{
				"dataSetServerDirectoryRequired": "<@spring.message code='validation.required' />",
			},
			"dataSetResFileName":
			{
				"dataSetServerFileNameRequired": "<@spring.message code='validation.required' />",
				"dataSetServerFilePreviewRequired": "<@spring.message code='dataSet.validation.previewRequired' />",
				"dataSetServerFilePropertiesRequired": "<@spring.message code='dataSet.validation.propertiesRequired' />"
			}
		},
		submitHandler : function(form)
		{
			var formData = $.formToJson(form);
			formData["properties"] = po.getFormDataSetProperties();
			formData["params"] = po.getFormDataSetParams();
			
			var originalFileName = po.element("#${pageId}-originalFileName").val();
			
			$.postJson("${contextPath}/dataSet/${formAction}?originalFileName="+originalFileName, formData,
			function(response)
			{
				po.pageParamCallAfterSave(true, response.data);
			});
		},
		errorPlacement : function(error, element)
		{
			error.appendTo(element.closest(".form-item-value"));
		}
	});
})
(${pageId});
</script>
</body>
</html>
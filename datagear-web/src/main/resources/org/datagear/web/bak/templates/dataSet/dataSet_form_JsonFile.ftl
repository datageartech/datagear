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
<#include "../include/page_obj.ftl" >
<div id="${pageId}" class="page-form page-form-dataSet page-form-dataSet-jsonFile">
	<form id="${pageId}-form" action="${contextPath}/dataSet/${formAction}" method="POST">
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
				<div class="form-item error-newline">
					<div class="form-item-label">
						<label title="<@spring.message code='dataSet.jsonFile.dataJsonPath.desc' />">
							<@spring.message code='dataSet.jsonFile.dataJsonPath' />
						</label>
					</div>
					<div class="form-item-value">
						<input type="text" name="dataJsonPath" value="${(dataSet.dataJsonPath)!''}" class="ui-widget ui-widget-content ui-corner-all" />
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
		<div class="form-foot">
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
	
	po.initFormBtns();
	po.initMtableModelInput();
	po.element().autoCloseSubPanel();
	po.initAnalysisProject("${((dataSet.analysisProject.id)!'')?js_string?no_esc}", "${((dataSet.analysisProject.name)!'')?js_string?no_esc}");
	po.elementOfName("encoding").selectmenu({ appendTo: po.element(), position: {my: "left bottom", at: "left top"}, classes: { "ui-selectmenu-menu" : "encoding-selectmenu-menu" } });
	po.initWorkspaceHeight();
	po.initWorkspaceTabs(true);
	po.initParamPropertyDataFormat(po.dataSetParams, po.dataSetProperties);
	
	po.initDataSetFileInput(po.url("uploadFile"), "${((dataSet.fileSourceType)!'')?js_string?no_esc}", po.isAddOperation());
	
	po.updatePreviewOptionsData = function()
	{
		var dataSet = po.previewOptions.data.dataSet;

		dataSet.fileSourceType = po.fileSourceTypeValue();
		dataSet.fileName = po.elementOfName("fileName").val();
		dataSet.dataSetResDirectory = {};
		dataSet.dataSetResDirectory.id = po.elementOfName("dataSetResDirectory.id").val();
		dataSet.dataSetResDirectory.directory = po.elementOfName("dataSetResDirectory.directory").val();
		dataSet.dataSetResFileName = po.elementOfName("dataSetResFileName").val();
		dataSet.dataJsonPath = po.elementOfName("dataJsonPath").val();
		dataSet.encoding = po.elementOfName("encoding").val();
		
		po.previewOptions.data.originalFileName = po.elementOfId("${pageId}-originalFileName").val();
	};
	
	<#if !isAdd>
	//编辑、查看操作应初始化为已完成预览的状态
	po.updatePreviewOptionsData();
	po.previewSuccess(true);
	</#if>
	
	po.isPreviewValueModified = function()
	{
		var fileSourceType = po.fileSourceTypeValue();
		var dataSetResDirectoryId = po.elementOfName("dataSetResDirectory.id").val();
		var dataSetResFileName = po.elementOfName("dataSetResFileName").val();
		var fileName = po.elementOfName("fileName").val();
		var dataJsonPath = po.elementOfName("dataJsonPath").val();
		var encoding = po.elementOfName("encoding").val();
		
		var pd = po.previewOptions.data.dataSet;
		var dataSetResDirectory = (pd.dataSetResDirectory || {});
		
		return ((pd.fileSourceType != fileSourceType)
				|| (po.isFileSourceTypeUpload(fileSourceType) && pd.fileName != fileName)
				|| (po.isFileSourceTypeServer(fileSourceType) && (dataSetResDirectory.id != dataSetResDirectoryId || pd.dataSetResFileName != dataSetResFileName))
				|| (pd.dataJsonPath != dataJsonPath) || (pd.encoding != encoding)
				|| po.isPreviewParamPropertyDataFormatModified());
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
	
	po.validateAjaxJsonForm(
	{
		ignore : "",
		rules :
		{
			"displayName" :
			{
				"dataSetUploadFileNameRequired": true,
				"dataSetUploadFilePreviewRequired": true
			},
			"dataSetResDirectory.directory":
			{
				"dataSetServerDirectoryRequired": true,
			},
			"dataSetResFileName":
			{
				"dataSetServerFileNameRequired": true,
				"dataSetServerFilePreviewRequired": true
			},
		},
		messages :
		{
			"displayName" :
			{
				"dataSetUploadFileNameRequired": po.validateMessages.required,
				"dataSetUploadFilePreviewRequired": "<@spring.message code='dataSet.validation.previewRequired' />"
			},
			"dataSetResDirectory.directory":
			{
				"dataSetServerDirectoryRequired": po.validateMessages.required,
			},
			"dataSetResFileName":
			{
				"dataSetServerFileNameRequired": po.validateMessages.required,
				"dataSetServerFilePreviewRequired": "<@spring.message code='dataSet.validation.previewRequired' />"
			}
		}
	},
	{
		handleData: function(data)
		{
			po.setOriginalFileNameParam();
			
			data["properties"] = po.getFormDataSetProperties();
			data["params"] = po.getFormDataSetParams();
		}
	});
})
(${pageId});
</script>
</body>
</html>
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
<#assign isAdd=(formAction == 'saveAddForExcel')>
<html>
<head>
<#include "../include/html_head.ftl">
<title><#include "../include/html_title_app_name.ftl">
	<@spring.message code='${titleMessageKey}' /> - <@spring.message code='dataSet.dataSetType.Excel' />
</title>
</head>
<body>
<#include "../include/page_obj.ftl" >
<div id="${pageId}" class="page-form page-form-dataSet page-form-dataSet-excel">
	<form id="${pageId}form" action="${contextPath}/dataSet/${formAction}" method="POST">
		<div class="form-head"></div>
		<div class="form-content">
			<#include "include/dataSet_form_html_name.ftl">
			<div class="workspace">
				<div class="form-item">
					<div class="form-item-label">
						<label><@spring.message code='dataSet.excelFile' /></label>
					</div>
					<div class="form-item-value form-item-value-file-input">
						<#include "include/dataSet_form_file_input.ftl">
					</div>
				</div>
				<div class="form-item error-newline">
					<div class="form-item-label">
						<label title="<@spring.message code='dataSet.excel.sheetIndex.desc' />">
							<@spring.message code='dataSet.excel.sheetIndex' />
						</label>
					</div>
					<div class="form-item-value">
						<input type="text" name="sheetIndex" value="${(dataSet.sheetIndex)!''}" required="required" class="ui-widget ui-widget-content ui-corner-all" style="width:41%;" />
					</div>
				</div>
				<div class="form-item error-newline">
					<div class="form-item-label">
						<label title="<@spring.message code='dataSet.excel.nameRow.desc' />">
							<@spring.message code='dataSet.excel.nameRow' />
						</label>
					</div>
					<div class="form-item-value">
						<input type="hidden" name="nameRow" value="${(dataSet.nameRow)!''}" class="ui-widget ui-widget-content ui-corner-all" />
						<span class="nameRow-radios">
							<label for="${pageId}-nameRow_0">
								<@spring.message code='dataSet.excel.nameRow.none' />
							</label>
				   			<input type="radio" id="${pageId}-nameRow_0" name="nameRowRadio" value="0" />
							<label for="${pageId}-nameRow_1">
								<@spring.message code='dataSet.excel.nameRow.assign' />
							</label>
				   			<input type="radio" id="${pageId}-nameRow_1" name="nameRowRadio" value="1"  />
						</span>
						&nbsp;
						<input type="text" name="nameRowText" value="${(dataSet.nameRow)!''}" class="ui-widget ui-widget-content ui-corner-all" style="width:4.1em;" />
					</div>
				</div>
				<div class="form-item error-newline">
					<div class="form-item-label">
						<label title="<@spring.message code='dataSet.excel.dataRowExp.desc' />">
							<@spring.message code='dataSet.excel.dataRowExp' />
						</label>
					</div>
					<div class="form-item-value">
						<input type="text" name="dataRowExp" value="${(dataSet.dataRowExp)!''}" class="ui-widget ui-widget-content ui-corner-all" />
					</div>
				</div>
				<div class="form-item error-newline">
					<div class="form-item-label">
						<label title="<@spring.message code='dataSet.excel.dataColumnExp.desc' />">
							<@spring.message code='dataSet.excel.dataColumnExp' />
						</label>
					</div>
					<div class="form-item-value">
						<input type="text" name="dataColumnExp" value="${(dataSet.dataColumnExp)!''}" class="ui-widget ui-widget-content ui-corner-all" />
					</div>
				</div>
				<div class="form-item">
					<div class="form-item-label">
						<label><@spring.message code='dataSet.excel.forceXls' /></label>
					</div>
					<div class="form-item-value no-padding-bottom">
						<#assign forceXls=(dataSet.forceXls)!true>
						<div id="${pageId}-forceXls">
							<label for="${pageId}-forceXls-true"><@spring.message code='yes' /></label>
							<input id="${pageId}-forceXls-true" type="radio" name="forceXls" value="true" <#if forceXls>checked="checked"</#if> />
							<label for="${pageId}-forceXls-false"><@spring.message code='no' /></label>
							<input id="${pageId}-forceXls-false" type="radio" name="forceXls" value="false" <#if !forceXls>checked="checked"</#if> />
						</div>
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
<#include "include/dataSet_form_js_nameRow.ftl">
<script type="text/javascript">
(function(po)
{
	po.dataSetProperties = <@writeJson var=dataSetProperties />;
	po.dataSetParams = <@writeJson var=dataSetParams />;
	
	po.initFormBtns();
	po.initMtableModelInput();
	po.element().autoCloseSubPanel();
	po.initAnalysisProject("${((dataSet.analysisProject.id)!'')?js_string?no_esc}", "${((dataSet.analysisProject.name)!'')?js_string?no_esc}");
	po.element(".nameRow-radios").checkboxradiogroup();
	po.elementOfId("${pageId}-forceXls").checkboxradiogroup();
	po.initWorkspaceHeight();
	po.initWorkspaceTabs(true);
	po.initParamPropertyDataFormat(po.dataSetParams, po.dataSetProperties);
	po.initNameRowOperation(${(dataSet.nameRow)!"1"});

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
		dataSet.sheetIndex = po.elementOfName("sheetIndex").val();
		dataSet.nameRow = po.nameRowValue();
		dataSet.dataRowExp = po.elementOfName("dataRowExp").val();
		dataSet.dataColumnExp = po.elementOfName("dataColumnExp").val();
		dataSet.forceXls = po.element("input[name='forceXls']:checked").val();
		
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
		var sheetIndex = po.elementOfName("sheetIndex").val();
		var nameRow = po.nameRowValue();
		var dataRowExp = po.elementOfName("dataRowExp").val();
		var dataColumnExp = po.elementOfName("dataColumnExp").val();
		var forceXls = po.element("input[name='forceXls']:checked").val();
		
		var pd = po.previewOptions.data.dataSet;
		var dataSetResDirectory = (pd.dataSetResDirectory || {});
		
		return ((pd.fileSourceType != fileSourceType)
				|| (po.isFileSourceTypeUpload(fileSourceType) && pd.fileName != fileName)
				|| (po.isFileSourceTypeServer(fileSourceType) && (dataSetResDirectory.id != dataSetResDirectoryId || pd.dataSetResFileName != dataSetResFileName))
				|| (pd.sheetIndex != sheetIndex)
				|| (pd.nameRow != nameRow) || (pd.dataRowExp != dataRowExp)
				|| (pd.dataColumnExp != dataColumnExp) || (pd.forceXls != forceXls)
				|| po.isPreviewParamPropertyDataFormatModified());
	};
	
	po.previewOptions.url = po.url("previewExcel");
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
	
	$.validator.addMethod("dataSetExcelPreviewRequired", function(value, element)
	{
		return !po.isPreviewValueModified() && po.previewSuccess();
	});
	
	$.validator.addMethod("dataSetExcelDataRowExpRegex", function(value, element)
	{
		if(!value)
			return true;
		
		var regex = /[\d\-\,\s]$/g;
		
		return regex.test(value);
	});

	$.validator.addMethod("dataSetExcelDataColumnExpRegex", function(value, element)
	{
		if(!value)
			return true;
		
		var regex = /([A-Z]|[\-\,\s])$/g;
		
		return regex.test(value);
	});
	
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
			"sheetIndex": {"integer": true, "min": 1},
			"nameRowText": {"integer": true, "min": 1},
			"dataRowExp": {"dataSetExcelDataRowExpRegex": true},
			"dataColumnExp": {"dataSetExcelDataColumnExpRegex": true}
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
			},
			"dataRowExp": {"dataSetExcelDataRowExpRegex": "<@spring.message code='dataSet.validation.excel.dataRowExp.regex' />"},
			"dataColumnExp": {"dataSetExcelDataColumnExpRegex": "<@spring.message code='dataSet.validation.excel.dataColumnExp.regex' />"}
		}
	},
	{
		handleData: function(data)
		{
			po.setOriginalFileNameParam();
			
			data["properties"] = po.getFormDataSetProperties();
			data["params"] = po.getFormDataSetParams();
			data["nameRow"] = po.nameRowValue();
			delete data["nameRowRadio"];
			delete data["nameRowText"];
		}
	});
})
(${pageId});
</script>
</body>
</html>
<#include "../../include/import_global.ftl">
<#include "../../include/html_doctype.ftl">
<#--
titleMessageKey 标题标签I18N关键字，不允许null
formAction 表单提交action，允许为null
readonly 是否只读操作，允许为null
-->
<#assign formAction=(formAction!'#')>
<#assign readonly=(readonly!false)>
<#assign isAdd=(formAction == 'saveAdd')>
<html>
<head>
<#include "../../include/html_head.ftl">
<title><#include "../../include/html_title_app_name.ftl">
	<@spring.message code='${titleMessageKey}' /> - <@spring.message code='dataSet.dataSetType.Excel' />
</title>
</head>
<body>
<div id="${pageId}" class="page-form page-form-dataSet page-form-dataSet-excel">
	<form id="${pageId}-form" action="#" method="POST">
		<div class="form-head"></div>
		<div class="form-content">
			<#include "include/dataSet_form_html_name.ftl">
			<div class="workspace">
				<div class="form-item">
					<div class="form-item-label">
						<label><@spring.message code='dataSet.excelFile' /></label>
					</div>
					<div class="form-item-value">
						<input type="hidden" id="${pageId}-originalFileName" value="${(dataSet.fileName)!''?html}" />
						<input type="hidden" name="fileName" value="${(dataSet.fileName)!''?html}" />
						<input type="text" name="displayName" value="${(dataSet.displayName)!''?html}" class="file-display-name ui-widget ui-widget-content" readonly="readonly" />
						<input type="hidden"  id="${pageId}-workspaceEditor" value="${(dataSet.fileName)!''?html}" />
						<#if !readonly>
						<div class="fileinput-wrapper">
							<div class="ui-widget ui-corner-all ui-button fileinput-button"><@spring.message code='upload' /><input type="file"></div>
							<div class="upload-file-info"></div>
						</div>
						</#if>
					</div>
				</div>
				<div class="form-item">
					<div class="form-item-label">
						<label><@spring.message code='dataSet.excel.sheetIndex' /></label>
					</div>
					<div class="form-item-value">
						<input type="text" name="sheetIndex" value="${(dataSet.sheetIndex)!''?html}" class="ui-widget ui-widget-content" />
					</div>
				</div>
				<div class="form-item">
					<div class="form-item-label">
						<label><@spring.message code='dataSet.excel.nameRow' /></label>
					</div>
					<div class="form-item-value">
						<input type="text" name="nameRow" value="${(dataSet.nameRow)!''?html}" class="ui-widget ui-widget-content" />
					</div>
				</div>
				<div class="form-item">
					<div class="form-item-label">
						<label><@spring.message code='dataSet.excel.dataRowExp' /></label>
					</div>
					<div class="form-item-value">
						<input type="text" name="dataRowExp" value="${(dataSet.dataRowExp)!''?html}" class="ui-widget ui-widget-content" />
					</div>
				</div>
				<div class="form-item">
					<div class="form-item-label">
						<label><@spring.message code='dataSet.excel.dataColumnExp' /></label>
					</div>
					<div class="form-item-value">
						<input type="text" name="dataColumnExp" value="${(dataSet.dataColumnExp)!''?html}" class="ui-widget ui-widget-content" />
					</div>
				</div>
				<div class="form-item">
					<div class="form-item-label">
						<label><@spring.message code='dataSet.excel.forceXls' /></label>
					</div>
					<div class="form-item-value">
						<input type="text" name="forceXls" value="${((dataSet.forceXls)!true)?string('true', 'false')}" class="ui-widget ui-widget-content" />
					</div>
				</div>
				<#include "include/dataSet_form_html_wow.ftl" >
			</div>
		</div>
		<div class="form-foot" style="text-align:center;">
			<#if !readonly>
			<input type="submit" value="<@spring.message code='save' />" class="recommended" />
			&nbsp;&nbsp;
			<input type="reset" value="<@spring.message code='reset' />" />
			<#else>
			<div class="form-foot-placeholder">&nbsp;</div>
			</#if>
		</div>
	</form>
	<#include "include/dataSet_form_html_preview_pvp.ftl" >
</div>
<#include "../../include/page_js_obj.ftl" >
<#include "../../include/page_obj_form.ftl">
<#include "include/dataSet_form_js.ftl">
<script type="text/javascript">
(function(po)
{
	po.dataSetProperties = <@writeJson var=dataSetProperties />;
	po.dataSetParams = <@writeJson var=dataSetParams />;
	
	$.initButtons(po.element());
	po.initWorkspaceHeight();
	
	po.fileNameEditorValue = function(value)
	{
		var $editor = po.element("#${pageId}-workspaceEditor");
		
		if(value === undefined)
			return $editor.val();
		else
			$editor.val(value);
	};
	
	po.isPreviewValueModified = function()
	{
		return po.isFileNameModified();
	};
	
	po.isFileNameModified = function(inputValue, editorValue)
	{
		if(inputValue == undefined)
			inputValue = po.element("input[name='fileName']").val();
		if(editorValue == undefined)
			editorValue = po.fileNameEditorValue();
		
		return po.isModifiedIgnoreBlank(inputValue, editorValue);
	};
	
	po.initWorkspaceTabs();
	
	po.initDataSetPropertiesTable(po.dataSetProperties);
	
	po.initDataSetParamsTable(po.dataSetParams);
	
	po.initPreviewParamValuePanel();
	
	po.previewOptions.url = po.url("previewExcel");
	po.previewOptions.beforePreview = function()
	{
		var fileName = po.fileNameEditorValue();
		
		if(!fileName)
			return false;
		
		this.data.dataSet.fileName = fileName;
		this.data.dataSet.sheetIndex = po.element("input[name='sheetIndex']").val();
		this.data.dataSet.nameRow = po.element("input[name='nameRow']").val();
		this.data.dataSet.dataRowExp = po.element("input[name='dataRowExp']").val();
		this.data.dataSet.dataColumnExp = po.element("input[name='dataColumnExp']").val();
		this.data.dataSet.forceXls = po.element("input[name='forceXls']").val();
		this.data.originalFileName = po.element("#${pageId}-originalFileName").val();
	};
	po.previewOptions.beforeRefresh = function()
	{
		if(!this.data.dataSet || !this.data.dataSet.fileName)
			return false;
	};
	po.previewOptions.success = function(previewResponse)
	{
		po.element("input[name='fileName']").val(this.data.dataSet.fileName);
	};
	
	po.initPreviewOperations();
	
	po.fileUploadInfo = function(){ return this.element(".upload-file-info"); };

	po.element(".fileinput-button").fileupload(
	{
		url : po.url("uploadFile"),
		paramName : "file",
		success : function(uploadResult, textStatus, jqXHR)
		{
			$.fileuploadsuccessHandlerForUploadInfo(po.fileUploadInfo(), false);
			po.element("input[name='displayName']").val(uploadResult.displayName);
			po.element("#${pageId}-workspaceEditor").val(uploadResult.fileName);
		}
	})
	.bind('fileuploadadd', function (e, data)
	{
		po.element("input[name='displayName']").val("");
		$.fileuploadaddHandlerForUploadInfo(e, data, po.fileUploadInfo());
	})
	.bind('fileuploadprogressall', function (e, data)
	{
		$.fileuploadprogressallHandlerForUploadInfo(e, data, po.fileUploadInfo());
	});
	
	$.validator.addMethod("dataSetExcelFilePreviewRequired", function(value, element)
	{
		return !po.isFileNameModified();
	});
	
	po.form().validate(
	{
		ignore : "",
		rules :
		{
			"name" : "required",
			"displayName" : {"required": true, "dataSetExcelFilePreviewRequired": true, "dataSetPropertiesRequired": true}
		},
		messages :
		{
			"name" : "<@spring.message code='validation.required' />",
			"displayName" :
			{
				"required": "<@spring.message code='validation.required' />",
				"dataSetExcelFilePreviewRequired": "<@spring.message code='dataSet.validation.previewRequired' />",
				"dataSetPropertiesRequired": "<@spring.message code='dataSet.validation.propertiesRequired' />"
			}
		},
		submitHandler : function(form)
		{
			var formData = $.formToJson(form);
			formData["properties"] = po.getFormDataSetProperties();
			formData["params"] = po.getFormDataSetParams();
			
			var originalFileName = po.element("#${pageId}-originalFileName").val();
			
			$.postJson("${contextPath}/analysis/dataSet/${formAction}?originalFileName="+originalFileName, formData,
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
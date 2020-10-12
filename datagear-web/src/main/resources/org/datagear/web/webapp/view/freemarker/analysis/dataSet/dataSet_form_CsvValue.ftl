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
	<@spring.message code='${titleMessageKey}' /> - <@spring.message code='dataSet.dataSetType.CsvValue' />
</title>
</head>
<body>
<#include "../../include/page_js_obj.ftl" >
<div id="${pageId}" class="page-form page-form-dataSet">
	<form id="${pageId}-form" action="#" method="POST">
		<div class="form-head"></div>
		<div class="form-content">
			<#include "include/dataSet_form_html_name.ftl">
			<div class="workspace">
				<div class="form-item">
					<div class="form-item-label">
						<label title="<@spring.message code='dataSet.csv.desc' />">
							<@spring.message code='dataSet.csv' />
						</label>
					</div>
					<div class="form-item-value error-newline">
						<textarea name="value" class="ui-widget ui-widget-content" style="display:none;">${(dataSet.value)!''?html}</textarea>
						<div class="workspace-editor-wrapper ui-widget ui-widget-content">
							<div id="${pageId}-workspaceEditor" class="workspace-editor"></div>
						</div>
					</div>
				</div>
				<#include "include/dataSet_form_html_wow.ftl" >
			</div>
			<div class="form-item">
				<div class="form-item-label">
					<label title="<@spring.message code='dataSet.csv.nameRow.desc' />">
						<@spring.message code='dataSet.csv.nameRow' />
					</label>
				</div>
				<div class="form-item-value">
					<input type="hidden" name="nameRow" value="${(dataSet.nameRow)!''?html}" class="ui-widget ui-widget-content" />
					<span class="nameRow-radios">
						<label for="${pageId}-nameRow_0">
							<@spring.message code='dataSet.csv.nameRow.none' />
						</label>
			   			<input type="radio" id="${pageId}-nameRow_0" name="nameRowRadio" value="0" />
						<label for="${pageId}-nameRow_1">
							<@spring.message code='dataSet.csv.nameRow.assign' />
						</label>
			   			<input type="radio" id="${pageId}-nameRow_1" name="nameRowRadio" value="1"  />
					</span>
					&nbsp;
					<input type="text" name="nameRowText" value="${(dataSet.nameRow)!''?html}" class="ui-widget ui-widget-content" style="width:4.1em;" />
				</div>
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
<#include "../../include/page_obj_form.ftl">
<#include "include/dataSet_form_js.ftl">
<#include "include/dataSet_form_js_nameRow.ftl">
<script type="text/javascript">
(function(po)
{
	po.dataSetProperties = <@writeJson var=dataSetProperties />;
	po.dataSetParams = <@writeJson var=dataSetParams />;
	
	$.initButtons(po.element());
	po.initAnalysisProject("${(dataSet.analysisProject.id)!''?js_string}", "${(dataSet.analysisProject.name)!''?js_string}");
	po.element(".nameRow-radios").controlgroup();
	po.initWorkspaceHeight();
	
	po.csvEditor = ace.edit("${pageId}-workspaceEditor");
	po.csvEditor.setShowPrintMargin(false);
	
	po.initWorkspaceEditor(po.csvEditor, po.element("textarea[name='value']").val());
	po.initWorkspaceTabs();
	po.getAddPropertyName = function()
	{
		var selectionRange = po.csvEditor.getSelectionRange();
		return (po.csvEditor.session.getTextRange(selectionRange) || "");
	};
	po.initDataSetPropertiesTable(po.dataSetProperties);
	po.initDataSetParamsTable(po.dataSetParams);
	po.initPreviewParamValuePanel();
	po.initNameRowOperation(${(dataSet.nameRow)!"1"});
	
	po.updatePreviewOptionsData = function()
	{
		var value = po.csvEditor.getValue();
		
		var dataSet = po.previewOptions.data.dataSet;
		
		dataSet.value = value;
		dataSet.nameRow = po.nameRowValue();
	};
	
	<#if formAction != 'saveAddForCsvValue'>
	//编辑、查看操作应初始化为已完成预览的状态
	po.updatePreviewOptionsData();
	po.previewSuccess(true);
	</#if>
	
	po.isPreviewValueModified = function()
	{
		var value = po.csvEditor.getValue();
		var nameRow = po.nameRowValue();
		
		var pd = po.previewOptions.data.dataSet;
		
		return (pd.value != value) || (pd.nameRow != nameRow);
	};
	
	po.previewOptions.url = po.url("previewCsvValue");
	po.previewOptions.beforePreview = function()
	{
		po.updatePreviewOptionsData();
		
		if(!this.data.dataSet.value)
			return false;
	};
	po.previewOptions.beforeRefresh = function()
	{
		if(!this.data.dataSet.value)
			return false;
	};
	
	po.initPreviewOperations();
	
	$.validator.addMethod("dataSetCsvValueRequired", function(value, element)
	{
		var value = po.csvEditor.getValue();
		return value.length > 0;
	});
	
	$.validator.addMethod("dataSetCsvValuePreviewRequired", function(value, element)
	{
		return !po.isPreviewValueModified() && po.previewSuccess();
	});
	
	po.form().validate(
	{
		ignore : "",
		rules :
		{
			"name" : "required",
			"value" : {"dataSetCsvValueRequired": true, "dataSetCsvValuePreviewRequired": true, "dataSetPropertiesRequired": true},
			"nameRowText": {"integer": true, "min": 1},
		},
		messages :
		{
			"name" : "<@spring.message code='validation.required' />",
			"value" :
			{
				"dataSetCsvValueRequired": "<@spring.message code='validation.required' />",
				"dataSetCsvValuePreviewRequired": "<@spring.message code='dataSet.validation.previewRequired' />",
				"dataSetPropertiesRequired": "<@spring.message code='dataSet.validation.propertiesRequired' />"
			},
			"nameRowText":
			{
				"integer": "<@spring.message code='validation.integer' />",
				"min": "<@spring.message code='validation.min' />"
			}
		},
		submitHandler : function(form)
		{
			var formData = $.formToJson(form);
			formData["properties"] = po.getFormDataSetProperties();
			formData["params"] = po.getFormDataSetParams();
			formData["value"] = po.csvEditor.getValue();
			formData["nameRow"] = po.nameRowValue();
			formData["nameRowRadio"] = undefined;
			formData["nameRowText"] = undefined;
			
			$.postJson("${contextPath}/analysis/dataSet/${formAction}", formData,
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
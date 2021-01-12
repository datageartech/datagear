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
	<@spring.message code='${titleMessageKey}' /> - <@spring.message code='dataSet.dataSetType.JsonValue' />
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
						<label title="<@spring.message code='dataSet.json.desc' />">
							<@spring.message code='dataSet.json' />
						</label>
					</div>
					<div class="form-item-value error-newline">
						<textarea name="value" class="ui-widget ui-widget-content" style="display:none;">${(dataSet.value)!''}</textarea>
						<div class="workspace-editor-wrapper ui-widget ui-widget-content">
							<div id="${pageId}-workspaceEditor" class="workspace-editor"></div>
						</div>
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
<#include "../../include/page_obj_form.ftl">
<#include "include/dataSet_form_js.ftl">
<script type="text/javascript">
(function(po)
{
	po.dataSetProperties = <@writeJson var=dataSetProperties />;
	po.dataSetParams = <@writeJson var=dataSetParams />;
	
	$.initButtons(po.element());
	po.initAnalysisProject("${((dataSet.analysisProject.id)!'')?js_string?no_esc}", "${((dataSet.analysisProject.name)!'')?js_string?no_esc}");
	po.initWorkspaceHeight();

	var languageTools = ace.require("ace/ext/language_tools");
	var JsonMode = ace.require("ace/mode/json").Mode;
	po.jsonEditor = ace.edit("${pageId}-workspaceEditor");
	po.jsonEditor.session.setMode(new JsonMode());
	po.jsonEditor.setShowPrintMargin(false);
	
	po.initWorkspaceEditor(po.jsonEditor, po.element("textarea[name='value']").val());
	po.initWorkspaceTabs();
	po.getAddPropertyName = function()
	{
		var selectionRange = po.jsonEditor.getSelectionRange();
		return (po.jsonEditor.session.getTextRange(selectionRange) || "");
	};
	po.initDataSetPropertiesTable(po.dataSetProperties);
	po.initDataSetParamsTable(po.dataSetParams);
	po.initPreviewParamValuePanel();

	po.updatePreviewOptionsData = function()
	{
		var value = po.jsonEditor.getValue();
		
		var dataSet = po.previewOptions.data.dataSet;
		
		dataSet.value = value;
	};
	
	<#if formAction != 'saveAddForJsonValue'>
	//编辑、查看操作应初始化为已完成预览的状态
	po.updatePreviewOptionsData();
	po.previewSuccess(true);
	</#if>
	
	po.isPreviewValueModified = function()
	{
		var value = po.jsonEditor.getValue();
		
		var pd = po.previewOptions.data.dataSet;
		
		return (pd.value != value);
	};
	
	po.previewOptions.url = po.url("previewJsonValue");
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
	
	$.validator.addMethod("dataSetJsonValueRequired", function(value, element)
	{
		var value = po.jsonEditor.getValue();
		return value.length > 0;
	});
	
	$.validator.addMethod("dataSetJsonValuePreviewRequired", function(value, element)
	{
		return !po.isPreviewValueModified() && po.previewSuccess();
	});
	
	po.form().validate(
	{
		ignore : "",
		rules :
		{
			"name" : "required",
			"value" : {"dataSetJsonValueRequired": true, "dataSetJsonValuePreviewRequired": true, "dataSetPropertiesRequired": true}
		},
		messages :
		{
			"name" : "<@spring.message code='validation.required' />",
			"value" :
			{
				"dataSetJsonValueRequired": "<@spring.message code='validation.required' />",
				"dataSetJsonValuePreviewRequired": "<@spring.message code='dataSet.validation.previewRequired' />",
				"dataSetPropertiesRequired": "<@spring.message code='dataSet.validation.propertiesRequired' />"
			}
		},
		submitHandler : function(form)
		{
			var formData = $.formToJson(form);
			formData["properties"] = po.getFormDataSetProperties();
			formData["params"] = po.getFormDataSetParams();
			formData["value"] = po.jsonEditor.getValue();
			
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
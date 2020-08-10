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
	<@spring.message code='${titleMessageKey}' /> - <@spring.message code='dataSet.dataSetType.JSON_VALUE' />
</title>
</head>
<body>
<div id="${pageId}" class="page-form page-form-dataSet">
	<form id="${pageId}-form" action="#" method="POST">
		<div class="form-head"></div>
		<div class="form-content">
			<#include "include/dataSet_form_html_name.ftl">
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='dataSet.json' /></label>
				</div>
				<div class="form-item-value form-item-value-workspace">
					<textarea name="value" class="ui-widget ui-widget-content" style="display:none;">${(dataSet.value)!''?html}</textarea>
					<div class="workspace-editor-wrapper ui-widget ui-widget-content">
						<div id="${pageId}-workspaceEditor" class="workspace-editor"></div>
					</div>
					<#include "include/dataSet_form_html_wow.ftl" >
				</div>
			</div>
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='dataSet.propertyLabelsText.JSON_VALUE' /></label>
				</div>
				<div class="form-item-value">
					<input type="text" name="propertyLabelsText" class="ui-widget ui-widget-content" value="${(dataSet.propertyLabelsText)!''?html}" placeholder="<@spring.message code='dataSet.propertyLabelsTextSplitByComma' />" />
				</div>
			</div>
		</div>
		<div class="form-foot" style="text-align:center;">
			<#if !readonly>
			<input type="submit" value="<@spring.message code='save' />" class="recommended" />
			&nbsp;&nbsp;
			<input type="reset" value="<@spring.message code='reset' />" />
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
	
	po.isValueModified = function(textareaValue, editorValue)
	{
		if(textareaValue == undefined)
			textareaValue = po.element("textarea[name='value']").val();
		if(editorValue == undefined)
			editorValue = po.jsonEditor.getValue();
		
		return po.isModifiedIgnoreBlank(textareaValue, editorValue);
	};
	
	var languageTools = ace.require("ace/ext/language_tools");
	var JsonMode = ace.require("ace/mode/json").Mode;
	po.jsonEditor = ace.edit("${pageId}-workspaceEditor");
	po.jsonEditor.session.setMode(new JsonMode());
	po.jsonEditor.setShowPrintMargin(false);
	
	po.initWorkspaceEditor(po.jsonEditor, po.element("textarea[name='value']").val());
	
	po.isWorkspaceModified = function()
	{
		return po.isValueModified();
	};
	po.initWorkspaceTabs();
	
	po.getAddParamName = function()
	{
		var selectionRange = po.sqlEditor.getSelectionRange();
		return (po.sqlEditor.session.getTextRange(selectionRange) || "");
	};
	po.initDataSetParamsTable(po.dataSetParams);
	
	po.initPreviewParamValuePanel();
	
	po.previewOptions.url = po.url("previewJsonValue");
	po.previewOptions.beforePreview = function()
	{
		var value = po.jsonEditor.getValue();
		
		if(!value)
			return false;
		
		this.data.value = value;
	};
	po.previewOptions.beforeMore = function()
	{
		if(!this.data.value)
			return false;
	};
	po.previewOptions.beforeRefresh = function()
	{
		if(!this.data.value)
			return false;
	};
	po.previewOptions.buildTablesColumns = function(previewResponse)
	{
		return $.buildDataTablesColumns(previewResponse.table);
	};
	po.previewOptions.success = function(previewResponse)
	{
		po.element("textarea[name='value']").val(this.data.value);
		po.dataSetProperties = (previewResponse.dataSetProperties || []);
		po.jsonEditor.focus();
	};
	
	po.initPreviewOperations();
	
	$.validator.addMethod("dataSetJsonValueRequired", function(value, element)
	{
		var value = po.jsonEditor.getValue();
		return value.length > 0;
	});
	
	$.validator.addMethod("dataSetJsonValuePreviewRequired", function(value, element)
	{
		return !po.isValueModified(value);
	});
	
	po.form().validate(
	{
		ignore : "",
		rules :
		{
			"name" : "required",
			"value" : {"dataSetJsonValueRequired": true, "dataSetJsonValuePreviewRequired": true}
		},
		messages :
		{
			"name" : "<@spring.message code='validation.required' />",
			"value" : {"dataSetJsonValueRequired": "<@spring.message code='validation.required' />", "dataSetJsonValuePreviewRequired": "<@spring.message code='dataSet.validation.previewRequired' />"}
		},
		submitHandler : function(form)
		{
			var formData = $.formToJson(form);
			formData["properties"] = po.dataSetProperties;
			formData["params"] = po.getFormDataSetParams();
			
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
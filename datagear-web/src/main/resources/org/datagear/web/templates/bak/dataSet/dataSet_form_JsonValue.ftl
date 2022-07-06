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
<#assign isAdd=(formAction == 'saveAddForJsonValue')>
<html>
<head>
<#include "../include/html_head.ftl">
<title><#include "../include/html_title_app_name.ftl">
	<@spring.message code='${titleMessageKey}' /> - <@spring.message code='dataSet.dataSetType.JsonValue' />
</title>
</head>
<body>
<#include "../include/page_obj.ftl" >
<div id="${pageId}" class="page-form page-form-dataSet">
	<form id="${pageId}-form" action="${contextPath}/dataSet/${formAction}" method="POST">
		<div class="form-head"></div>
		<div class="form-content">
			<#include "include/dataSet_form_html_name.ftl">
			<div class="workspace">
				<div class="form-item error-newline">
					<div class="form-item-label">
						<label title="<@spring.message code='dataSet.json.desc' />">
							<@spring.message code='dataSet.json' />
						</label>
					</div>
					<div class="form-item-value">
						<textarea name="value" class="ui-widget ui-widget-content ui-corner-all" style="display:none;">${(dataSet.value)!''}</textarea>
						<div class="workspace-editor-wrapper ui-widget ui-widget-content ui-corner-all">
							<div id="${pageId}-workspaceEditor" class="workspace-editor code-editor"></div>
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
<#include "../include/page_obj_codeEditor.ftl" >
<script type="text/javascript">
(function(po)
{
	po.dataSetProperties = <@writeJson var=dataSetProperties />;
	po.dataSetParams = <@writeJson var=dataSetParams />;
	
	po.initFormBtns();
	po.initMtableModelInput();
	po.element().autoCloseSubPanel();
	po.initAnalysisProject("${((dataSet.analysisProject.id)!'')?js_string?no_esc}", "${((dataSet.analysisProject.name)!'')?js_string?no_esc}");
	po.initWorkspaceHeight();
	
	po.jsonEditor = po.createWorkspaceEditor(po.elementOfId("${pageId}-workspaceEditor"),
	{
		value: po.elementOfName("value").val(),
		mode: {name: "javascript", json: true},
		readOnly: po.readonly
	});
	
	po.initWorkspaceTabs();
	po.getAddPropertyName = function()
	{
		return po.getSelectedCodeText(po.jsonEditor);
	};
	po.initParamPropertyDataFormat(po.dataSetParams, po.dataSetProperties);

	po.updatePreviewOptionsData = function()
	{
		var value = po.getCodeText(po.jsonEditor);
		
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
		var value = po.getCodeText(po.jsonEditor);
		
		var pd = po.previewOptions.data.dataSet;
		
		return (pd.value != value || po.isPreviewParamPropertyDataFormatModified());
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
		var value = po.getCodeText(po.jsonEditor);
		return value.length > 0;
	});
	
	$.validator.addMethod("dataSetJsonValuePreviewRequired", function(value, element)
	{
		return !po.isPreviewValueModified() && po.previewSuccess();
	});
	
	po.validateAjaxJsonForm(
	{
		ignore : "",
		rules :
		{
			"value" : {"dataSetJsonValueRequired": true, "dataSetJsonValuePreviewRequired": true}
		},
		messages :
		{
			"value" :
			{
				"dataSetJsonValueRequired": po.validateMessages.required,
				"dataSetJsonValuePreviewRequired": "<@spring.message code='dataSet.validation.previewRequired' />"
			}
		}
	},
	{
		handleData: function(data)
		{
			data["properties"] = po.getFormDataSetProperties();
			data["params"] = po.getFormDataSetParams();
			data["value"] = po.getCodeText(po.jsonEditor);
		}
	});
})
(${pageId});
</script>
</body>
</html>
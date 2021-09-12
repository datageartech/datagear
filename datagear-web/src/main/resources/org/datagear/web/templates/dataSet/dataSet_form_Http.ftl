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
<#assign isAdd=(formAction == 'saveAdd')>
<#assign HttpDataSet=statics['org.datagear.analysis.support.HttpDataSet']>
<html>
<head>
<#include "../include/html_head.ftl">
<title><#include "../include/html_title_app_name.ftl">
	<@spring.message code='${titleMessageKey}' /> - <@spring.message code='dataSet.dataSetType.Http' />
</title>
</head>
<body>
<#include "../include/page_js_obj.ftl" >
<div id="${pageId}" class="page-form page-form-dataSet page-form-dataSet-Http">
	<form id="${pageId}-form" action="#" method="POST">
		<div class="form-head"></div>
		<div class="form-content">
			<#include "include/dataSet_form_html_name.ftl">
			<div class="form-item">
				<div class="form-item-label">
					<label title="<@spring.message code='dataSet.http.uri.desc' />">
						<@spring.message code='dataSet.http.uri' />
					</label>
				</div>
				<div class="form-item-value">
					<input type="text" name="uri" value="${(dataSet.uri)!''}" class="ui-widget ui-widget-content" />
					
					<div class="form-item" style="margin-left: 0;" title="<@spring.message code='dataSet.http.requestMethod' />">
						<div class="form-item-label" style="display: none;">
							<label><@spring.message code='dataSet.http.requestMethod' /></label>
						</div>
						<div class="form-item-value form-item-value-requestMethod">
							<select name="requestMethod">
								<option value="${HttpDataSet.REQUEST_METHOD_GET}" <#if HttpDataSet.REQUEST_METHOD_GET == dataSet.requestMethod>selected="selected"</#if>>
									<@spring.message code='dataSet.http.requestMethod.GET' />
								</option>
								<option value="${HttpDataSet.REQUEST_METHOD_POST}" <#if HttpDataSet.REQUEST_METHOD_POST == dataSet.requestMethod>selected="selected"</#if>>
									<@spring.message code='dataSet.http.requestMethod.POST' />
								</option>
								<option value="${HttpDataSet.REQUEST_METHOD_PUT}" <#if HttpDataSet.REQUEST_METHOD_PUT == dataSet.requestMethod>selected="selected"</#if>>
									<@spring.message code='dataSet.http.requestMethod.PUT' />
								</option>
								<option value="${HttpDataSet.REQUEST_METHOD_PATCH}" <#if HttpDataSet.REQUEST_METHOD_PATCH == dataSet.requestMethod>selected="selected"</#if>>
									<@spring.message code='dataSet.http.requestMethod.PATCH' />
								</option>
								<option value="${HttpDataSet.REQUEST_METHOD_DELETE}" <#if HttpDataSet.REQUEST_METHOD_DELETE == dataSet.requestMethod>selected="selected"</#if>>
									<@spring.message code='dataSet.http.requestMethod.DELETE' />
								</option>
							</select>
						</div>
					</div>
				</div>
			</div>
			<div class="form-item">
				<div class="form-item-label">
					<label title="<@spring.message code='dataSet.http.requestContentType.desc' />">
						<@spring.message code='dataSet.http.requestContentType' />
					</label>
				</div>
				<div class="form-item-value form-item-value-requestContentType">
					<select name="requestContentType">
						<option value="${HttpDataSet.REQUEST_CONTENT_TYPE_FORM_URLENCODED}" <#if HttpDataSet.REQUEST_CONTENT_TYPE_FORM_URLENCODED == dataSet.requestContentType>selected="selected"</#if>>
							<@spring.message code='dataSet.http.requestContentType.FORM_URLENCODED' />
						</option>
						<option value="${HttpDataSet.REQUEST_CONTENT_TYPE_JSON}" <#if HttpDataSet.REQUEST_CONTENT_TYPE_JSON == dataSet.requestContentType>selected="selected"</#if>>
							<@spring.message code='dataSet.http.requestContentType.JSON' />
						</option>
					</select>
					
					<div class="form-item">
						<div class="form-item-label">
							<label><@spring.message code='dataSet.http.requestContentCharset' /></label>
						</div>
						<div class="form-item-value form-item-value-requestContentCharset">
							<select name="requestContentCharset">
								<#list availableCharsetNames as item>
								<option value="${item}" <#if item == dataSet.requestContentCharset>selected="selected"</#if>>${item}</option>
								</#list>
							</select>
						</div>
					</div>
				</div>
			</div>
			<div class="workspace">
				<div class="form-item">
					<div class="form-item-label">
						<label><@spring.message code='dataSet.http.responseContentType' /></label>
					</div>
					<div class="form-item-value form-item-value-responseContentType no-padding-bottom">
						<select name="responseContentType">
							<option value="${HttpDataSet.RESPONSE_CONTENT_TYPE_JSON}" <#if HttpDataSet.RESPONSE_CONTENT_TYPE_JSON == dataSet.responseContentType>selected="selected"</#if>>
								<@spring.message code='dataSet.http.responseContentType.JSON' />
							</option>
						</select>
					</div>
				</div>
				<div class="form-item">
					<div class="form-item-label">
						<label title="<@spring.message code='dataSet.http.request.desc' />">
							<@spring.message code='dataSet.http.request' />
						</label>
					</div>
					<div class="form-item-value error-newline">
						<textarea name="requestContent" class="ui-widget ui-widget-content" style="display:none;">${(dataSet.requestContent)!''}</textarea>
						<textarea name="headerContent" class="ui-widget ui-widget-content" style="display:none;">${(dataSet.headerContent)!''}</textarea>
						<div class="workspace-editor-tabs light-tabs">
							<ul class="workspace-editor-nav">
								<li class="editor-requestContent"><a href="#${pageId}-editor-requestContent"><@spring.message code='dataSet.http.requestContent' /></a></li>
								<li class="editor-headerContent"><a href="#${pageId}-editor-headerContent"><@spring.message code='dataSet.http.headerContent' /></a></li>
							</ul>
							<div id="${pageId}-editor-requestContent" class="workspace-editor-wrapper ui-widget ui-widget-content">
								<div id="${pageId}-workspaceEditor-requestContent" class="workspace-editor"></div>
							</div>
							<div id="${pageId}-editor-headerContent" class="workspace-editor-wrapper ui-widget ui-widget-content">
								<div id="${pageId}-workspaceEditor-headerContent" class="workspace-editor"></div>
							</div>
						</div>
					</div>
				</div>
				<div class="form-item form-item-responseDataJsonPath">
					<div class="form-item-label">
						<label title="<@spring.message code='dataSet.http.responseDataJsonPath.desc' />">
							<@spring.message code='dataSet.http.responseDataJsonPath' />
						</label>
					</div>
					<div class="form-item-value error-newline">
						<input type="text" name="responseDataJsonPath" value="${(dataSet.responseDataJsonPath)!''}" class="ui-widget ui-widget-content" />
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
	po.element("select[name='requestMethod']").selectmenu({ appendTo : po.element() });
	po.element("select[name='requestContentType']").selectmenu({ appendTo : po.element() });
	po.element("select[name='requestContentCharset']").selectmenu({ appendTo : po.element(), classes : { "ui-selectmenu-menu" : "encoding-selectmenu-menu" } });
	po.element("select[name='responseContentType']").selectmenu({ appendTo : po.element() });
	po.initWorkspaceHeight();
	po.element(".workspace-editor-tabs").tabs();
	var workspaceEditorGapHeight = po.element(".workspace-editor-nav").outerHeight(true) + 4;
	po.element(".workspace-editor-tabs").height(po.element(".workspace-editor-wrapper").height() - po.element(".form-item-responseDataJsonPath").outerHeight(true));
	po.element(".workspace-editor-wrapper").height(po.element(".workspace-editor-tabs").height() - workspaceEditorGapHeight);
	
	var languageTools = ace.require("ace/ext/language_tools");
	var JsonMode = ace.require("ace/mode/json").Mode;
	po.requestContentEditor = ace.edit("${pageId}-workspaceEditor-requestContent");
	po.requestContentEditor.session.setMode(new JsonMode());
	po.requestContentEditor.setShowPrintMargin(false);
	po.headerContentEditor = ace.edit("${pageId}-workspaceEditor-headerContent");
	po.headerContentEditor.session.setMode(new JsonMode());
	po.headerContentEditor.setShowPrintMargin(false);
	
	po.initWorkspaceEditor(po.requestContentEditor, po.element("textarea[name='requestContent']").val());
	po.initWorkspaceEditor(po.headerContentEditor, po.element("textarea[name='headerContent']").val());
	po.initWorkspaceTabs();
	po.getAddPropertyName = function()
	{
		var currentEditor = null;
		
		if(po.element(".workspace-editor-tabs").tabs("option", "active") == 0)
			currentEditor = po.requestContentEditor;
		else
			currentEditor = po.headerContentEditor;
		
		var selectionRange = currentEditor.getSelectionRange();
		return (currentEditor.session.getTextRange(selectionRange) || "");
	};
	po.initParamPropertyDataFormat(po.dataSetParams, po.dataSetProperties);
	
	po.updatePreviewOptionsData = function()
	{
		var uri = po.element("input[name='uri']").val();
		var requestMethod = po.element("select[name='requestMethod']").val();
		var requestContentType = po.element("select[name='requestContentType']").val();
		var requestContentCharset = po.element("select[name='requestContentCharset']").val();
		var responseContentType = po.element("select[name='responseContentType']").val();
		var requestContent = po.requestContentEditor.getValue();
		var headerContent = po.headerContentEditor.getValue();
		var responseDataJsonPath = po.element("input[name='responseDataJsonPath']").val();
		
		var dataSet = po.previewOptions.data.dataSet;
		
		dataSet.uri = uri;
		dataSet.requestMethod = requestMethod;
		dataSet.requestContentType = requestContentType;
		dataSet.requestContentCharset = requestContentCharset;
		dataSet.responseContentType = responseContentType;
		dataSet.requestContent = requestContent;
		dataSet.headerContent = headerContent;
		dataSet.responseDataJsonPath = responseDataJsonPath;
	};
	
	<#if formAction != 'saveAddForHttp'>
	//编辑、查看操作应初始化为已完成预览的状态
	po.updatePreviewOptionsData();
	po.previewSuccess(true);
	</#if>
	
	po.isPreviewValueModified = function()
	{
		var uri = po.element("input[name='uri']").val();
		var requestMethod = po.element("select[name='requestMethod']").val();
		var requestContentType = po.element("select[name='requestContentType']").val();
		var requestContentCharset = po.element("select[name='requestContentCharset']").val();
		var responseContentType = po.element("select[name='responseContentType']").val();
		var requestContent = po.requestContentEditor.getValue();
		var headerContent = po.headerContentEditor.getValue();
		var responseDataJsonPath = po.element("input[name='responseDataJsonPath']").val();
		
		var pd = po.previewOptions.data.dataSet;
		
		return ((pd.uri != uri) || (pd.requestMethod != requestMethod) || (pd.requestContentType != requestContentType)
				|| (pd.requestContentCharset != requestContentCharset) || (pd.responseContentType != responseContentType)
				|| (pd.requestContent != requestContent) || (pd.headerContent != headerContent)
				|| (pd.responseDataJsonPath != responseDataJsonPath)
				 || po.isPreviewParamPropertyDataFormatModified());
	};
	
	po.previewOptions.url = po.url("previewHttp");
	po.previewOptions.beforePreview = function()
	{
		po.updatePreviewOptionsData();
		
		if(!this.data.dataSet.uri)
			return false;
	};
	po.previewOptions.beforeRefresh = function()
	{
		if(!this.data.dataSet.uri)
			return false;
	};
	po.previewOptions.error = function(operationMessage)
	{
		if(operationMessage && operationMessage.data)
			return operationMessage.data[1];
	};
	
	po.initPreviewOperations();
	
	$.validator.addMethod("dataSetHttpPreviewRequired", function(value, element)
	{
		return !po.isPreviewValueModified() && po.previewSuccess();
	});
	
	po.form().validate(
	{
		ignore : "",
		rules :
		{
			"name" : "required",
			"uri" : "required",
			"requestContent" : {"dataSetHttpPreviewRequired": true, "dataSetPropertiesRequired": true}
		},
		messages :
		{
			"name" : "<@spring.message code='validation.required' />",
			"uri" : "<@spring.message code='validation.required' />",
			"requestContent" :
			{
				"dataSetHttpPreviewRequired": "<@spring.message code='dataSet.validation.previewRequired' />",
				"dataSetPropertiesRequired": "<@spring.message code='dataSet.validation.propertiesRequired' />"
			}
		},
		submitHandler : function(form)
		{
			var formData = $.formToJson(form);
			formData["properties"] = po.getFormDataSetProperties();
			formData["params"] = po.getFormDataSetParams();
			formData["requestContent"] = po.requestContentEditor.getValue();
			formData["headerContent"] = po.headerContentEditor.getValue();
			
			$.postJson("${contextPath}/dataSet/${formAction}", formData,
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
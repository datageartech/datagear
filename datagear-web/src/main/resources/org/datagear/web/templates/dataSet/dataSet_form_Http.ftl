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
<#assign isAdd=(formAction == 'saveAddForHttp')>
<#assign HttpDataSet=statics['org.datagear.analysis.support.HttpDataSet']>
<html>
<head>
<#include "../include/html_head.ftl">
<title><#include "../include/html_title_app_name.ftl">
	<@spring.message code='${titleMessageKey}' /> - <@spring.message code='dataSet.dataSetType.Http' />
</title>
</head>
<body>
<#include "../include/page_obj.ftl" >
<div id="${pageId}" class="page-form page-form-dataSet page-form-dataSet-Http">
	<form id="${pageId}form" action="${contextPath}/dataSet/${formAction}" method="POST">
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
					<input type="text" name="uri" value="${(dataSet.uri)!''}" required="required" maxlength="1000" class="ui-widget ui-widget-content ui-corner-all" />
					
					<div class="item-lv inline" title="<@spring.message code='dataSet.http.requestMethod' />">
						<div class="item-lv-l" style="display: none;">
							<label><@spring.message code='dataSet.http.requestMethod' /></label>
						</div>
						<div class="form-item-value-requestMethod item-lv-v">
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
					
					<div class="item-lv inline">
						<div class="item-lv-l">
							<label><@spring.message code='dataSet.http.requestContentCharset' /></label>
						</div>
						<div class="item-lv-v form-item-value-requestContentCharset">
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
				<div class="form-item error-newline">
					<div class="form-item-label">
						<label title="<@spring.message code='dataSet.http.request.desc' />">
							<@spring.message code='dataSet.http.request' />
						</label>
					</div>
					<div class="form-item-value">
						<textarea name="requestContent" class="ui-widget ui-widget-content ui-corner-all" style="display:none;">${(dataSet.requestContent)!''}</textarea>
						<textarea name="headerContent" class="ui-widget ui-widget-content ui-corner-all" style="display:none;">${(dataSet.headerContent)!''}</textarea>
						<div class="workspace-editor-tabs light-tabs">
							<ul class="workspace-editor-nav">
								<li class="editor-requestContent"><a href="#${pageId}-editor-requestContent"><@spring.message code='dataSet.http.requestContent' /></a></li>
								<li class="editor-headerContent"><a href="#${pageId}-editor-headerContent"><@spring.message code='dataSet.http.headerContent' /></a></li>
							</ul>
							<div id="${pageId}-editor-requestContent" class="workspace-editor-wrapper ui-widget ui-widget-content ui-corner-all">
								<div id="${pageId}-workspaceEditor-requestContent" class="workspace-editor code-editor"></div>
							</div>
							<div id="${pageId}-editor-headerContent" class="workspace-editor-wrapper ui-widget ui-widget-content ui-corner-all">
								<div id="${pageId}-workspaceEditor-headerContent" class="workspace-editor code-editor"></div>
							</div>
						</div>
					</div>
				</div>
				<div class="form-item-responseDataJsonPath form-item error-newline">
					<div class="form-item-label">
						<label title="<@spring.message code='dataSet.http.responseDataJsonPath.desc' />">
							<@spring.message code='dataSet.http.responseDataJsonPath' />
						</label>
					</div>
					<div class="form-item-value">
						<input type="text" name="responseDataJsonPath" value="${(dataSet.responseDataJsonPath)!''}" class="ui-widget ui-widget-content ui-corner-all" />
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
	po.elementOfName("requestMethod").selectmenu({ appendTo : po.element() });
	po.elementOfName("requestContentType").selectmenu({ appendTo : po.element() });
	po.elementOfName("requestContentCharset").selectmenu({ appendTo : po.element(), classes : { "ui-selectmenu-menu" : "encoding-selectmenu-menu" } });
	po.elementOfName("responseContentType").selectmenu({ appendTo : po.element() });
	po.initWorkspaceHeight();
	po.element(".workspace-editor-tabs").tabs(
	{
		activate: function(event, ui)
		{
			var newTab = $(ui.newTab);
			
			if(newTab.hasClass("editor-requestContent"))
				po.requestContentEditor.refresh();
			else if(newTab.hasClass("editor-headerContent"))
				po.headerContentEditor.refresh();
		}
	});
	var workspaceEditorGapHeight = po.element(".workspace-editor-nav").outerHeight(true) + 4;
	po.element(".workspace-editor-tabs").height(po.element(".workspace-editor-wrapper").height() - po.element(".form-item-responseDataJsonPath").outerHeight(true));
	po.element(".workspace-editor-wrapper").height(po.element(".workspace-editor-tabs").height() - workspaceEditorGapHeight);

	po.requestContentEditor = po.createWorkspaceEditor(po.elementOfId("${pageId}-workspaceEditor-requestContent"),
	{
		value: po.elementOfName("requestContent").val(),
		mode: {name: "javascript", json: true}
	});
	po.headerContentEditor = po.createWorkspaceEditor(po.elementOfId("${pageId}-workspaceEditor-headerContent"),
	{
		value: po.elementOfName("headerContent").val(),
		mode: {name: "javascript", json: true}
	});
	po.getAddPropertyName = function()
	{
		var currentEditor = 
			(po.element(".workspace-editor-tabs").tabs("option", "active") == 0 ? po.requestContentEditor : po.headerContentEditor);
		return po.getSelectedCodeText(currentEditor);
	};
	
	po.initWorkspaceTabs();
	po.initParamPropertyDataFormat(po.dataSetParams, po.dataSetProperties);
	
	po.updatePreviewOptionsData = function()
	{
		var uri = po.elementOfName("uri").val();
		var requestMethod = po.elementOfName("requestMethod").val();
		var requestContentType = po.elementOfName("requestContentType").val();
		var requestContentCharset = po.elementOfName("requestContentCharset").val();
		var responseContentType = po.elementOfName("responseContentType").val();
		var requestContent = po.getCodeText(po.requestContentEditor);
		var headerContent = po.getCodeText(po.headerContentEditor);
		var responseDataJsonPath = po.elementOfName("responseDataJsonPath").val();
		
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
		var uri = po.elementOfName("uri").val();
		var requestMethod = po.elementOfName("requestMethod").val();
		var requestContentType = po.elementOfName("requestContentType").val();
		var requestContentCharset = po.elementOfName("requestContentCharset").val();
		var responseContentType = po.elementOfName("responseContentType").val();
		var requestContent = po.getCodeText(po.requestContentEditor);
		var headerContent = po.getCodeText(po.headerContentEditor);
		var responseDataJsonPath = po.elementOfName("responseDataJsonPath").val();
		
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
	
	po.validateAjaxJsonForm(
	{
		ignore : "",
		rules :
		{
			"requestContent" : {"dataSetHttpPreviewRequired": true}
		},
		messages :
		{
			"requestContent" :
			{
				"dataSetHttpPreviewRequired": "<@spring.message code='dataSet.validation.previewRequired' />"
			}
		}
	},
	{
		handleData: function(data)
		{
			data["properties"] = po.getFormDataSetProperties();
			data["params"] = po.getFormDataSetParams();
			data["requestContent"] = po.getCodeText(po.requestContentEditor);
			data["headerContent"] = po.getCodeText(po.headerContentEditor);
		}
	});
})
(${pageId});
</script>
</body>
</html>
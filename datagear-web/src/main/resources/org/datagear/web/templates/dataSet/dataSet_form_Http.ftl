<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#assign HttpDataSet=statics['org.datagear.analysis.support.HttpDataSet']>
<#include "../include/page_import.ftl">
<#include "../include/html_doctype.ftl">
<html>
<head>
<#include "../include/html_head.ftl">
<title>
	<#include "../include/html_app_name_prefix.ftl">
	<@spring.message code='module.dataSet.Http' />
	<#include "../include/html_request_action_suffix.ftl">
</title>
</head>
<body class="p-card no-border">
<#include "../include/page_obj.ftl">
<div id="${pid}" class="page page-form horizontal page-form-dataSet  page-form-dataSet-Http">
	<form class="flex flex-column" :class="{readonly: pm.isReadonlyAction}">
		<div class="page-form-content flex-grow-1 px-2 py-1 overflow-y-auto">
			<#include "include/dataSet_form_name.ftl">
			<div class="field grid">
				<label for="${pid}uri" class="field-label col-12 mb-2 md:col-3 md:mb-0">
					<@spring.message code='requestURI' />
				</label>
				<div class="field-input col-12 md:col-9">
					<p-inputtext id="${pid}uri" v-model="fm.uri" type="text" class="input w-full"
		        		name="uri" required maxlength="1000">
		        	</p-inputtext>
				</div>
			</div>
			<div class="field grid">
				<label for="${pid}requestMethod" class="field-label col-12 mb-2 md:col-3 md:mb-0">
					<@spring.message code='requestMethod' />
				</label>
				<div class="field-input col-12 md:col-9">
					<p-dropdown id="${pid}requestMethod" v-model="fm.requestMethod"
						:options="pm.requestMethodOptions" option-label="name" option-value="value" class="input w-full">
					</p-dropdown>
				</div>
			</div>
			<div class="field grid">
				<label for="${pid}requestContentType" class="field-label col-12 mb-2 md:col-3 md:mb-0"
					title="<@spring.message code='httpDataSet.requestContentType.desc' />">
					<@spring.message code='requestBodyType' />
				</label>
				<div class="field-input col-12 md:col-9">
					<p-dropdown id="${pid}requestContentType" v-model="fm.requestContentType"
						:options="pm.requestContentTypeOptions" option-label="name" option-value="value" class="input w-full">
					</p-dropdown>
				</div>
			</div>
			<div class="field grid">
				<label for="${pid}requestContentCharset" class="field-label col-12 mb-2 md:col-3 md:mb-0">
					<@spring.message code='requestBodyEncoding' />
				</label>
				<div class="field-input col-12 md:col-9">
					<p-dropdown id="${pid}requestContentCharset" v-model="fm.requestContentCharset"
						:options="pm.requestContentCharsetOptions" class="input w-full">
					</p-dropdown>
				</div>
			</div>
			<div class="field grid">
				<label for="${pid}requestContent" class="field-label col-12 mb-2"
					title="<@spring.message code='httpDataSet.requestContent.desc' />">
					<@spring.message code='requestBodyJson' />
				</label>
		        <div class="field-input col-12">
		        	<div id="${pid}requestContent" class="code-editor-wrapper input p-component p-inputtext w-full">
						<div id="${pid}requestContentEditor" class="code-editor"></div>
					</div>
		        	<div class="validate-msg">
		        		<input name="requestContent" maxlength="10000" type="text" class="validate-normalizer" />
		        	</div>
		        </div>
			</div>
			<div class="field grid">
				<label for="${pid}headerContent" class="field-label col-12 mb-2"
					title="<@spring.message code='httpDataSet.headerContent.desc' />">
					<@spring.message code='requestHeaderJson' />
				</label>
		        <div class="field-input col-12">
		        	<div id="${pid}headerContent" class="code-editor-wrapper input p-component p-inputtext w-full">
						<div id="${pid}headerContentEditor" class="code-editor"></div>
					</div>
		        	<div class="validate-msg">
		        		<input name="headerContent" maxlength="10000" type="text" class="validate-normalizer" />
		        	</div>
		        </div>
			</div>
			<div class="field grid">
				<label for="${pid}responseContentType" class="field-label col-12 mb-2 md:col-3 md:mb-0">
					<@spring.message code='responseBodyType' />
				</label>
				<div class="field-input col-12 md:col-9">
					<p-dropdown id="${pid}responseContentType" v-model="fm.responseContentType"
						:options="pm.responseContentTypeOptions" option-label="name" option-value="value" class="input w-full">
					</p-dropdown>
				</div>
			</div>
			<div class="field grid">
				<label for="${pid}responseDataJsonPath" class="field-label col-12 mb-2 md:col-3 md:mb-0"
					title="<@spring.message code='httpDataSet.responseDataJsonPath.desc' />">
					<@spring.message code='responseJsonPath' />
				</label>
				<div class="field-input col-12 md:col-9">
					<p-inputtext id="${pid}responseDataJsonPath" v-model="fm.responseDataJsonPath" type="text" class="input w-full"
						name="responseDataJsonPath" maxlength="200">
					</p-inputtext>
				</div>
			</div>
			<#include "include/dataSet_form_param_property.ftl">
		</div>
		<div class="page-form-foot flex-grow-0 pt-3 text-center h-opts">
			<#include "include/dataSet_form_preview.ftl">
			<p-button type="submit" label="<@spring.message code='save' />"></p-button>
		</div>
	</form>
</div>
<#include "../include/page_form.ftl">
<#include "../include/page_code_editor.ftl">
<#include "../include/page_boolean_options.ftl">
<#include "include/dataSet_form_js.ftl">
<script>
(function(po)
{
	po.submitUrl = "/dataSet/"+po.submitAction;
	po.previewUrl = "/dataSet/previewHttp";
	
	po.inflatePreviewFingerprint = function(fingerprint, dataSet)
	{
		fingerprint.uri = dataSet.uri;
		fingerprint.requestMethod = dataSet.requestMethod;
		fingerprint.requestContentType = dataSet.requestContentType;
		fingerprint.requestContentCharset = dataSet.requestContentCharset;
		fingerprint.requestContent = dataSet.requestContent;
		fingerprint.headerContent = dataSet.headerContent;
		fingerprint.responseContentType = dataSet.responseContentType;
		fingerprint.responseDataJsonPath = dataSet.responseDataJsonPath;
	};
	
	po.beforeSubmitForm = function(action)
	{
		var data = action.options.data;
		data.requestContent = po.getCodeText(po.requestContentEditor);
		data.headerContent = po.getCodeText(po.headerContentEditor);
		
		if(!po.beforeSubmitFormWithPreview(action))
			return false;
	};
	
	var formModel = $.unescapeHtmlForJson(<@writeJson var=formModel />);
	po.inflateDataSetModel(formModel);
	
	po.setupForm(formModel, {},
	{
		customNormalizers:
		{
			requestContent: function()
			{
				return po.getCodeText(po.requestContentEditor);
			},
			headerContent: function()
			{
				return po.getCodeText(po.headerContentEditor);
			}
		},
		invalidHandler: function()
		{
			po.handlePreviewInvalidForm();
		}
	});
	
	po.vuePageModel(
	{
		requestMethodOptions:
		[
			{name: "<@spring.message code='httpDataSet.requestMethod.GET' />", value: "${HttpDataSet.REQUEST_METHOD_GET}"},
			{name: "<@spring.message code='httpDataSet.requestMethod.POST' />", value: "${HttpDataSet.REQUEST_METHOD_POST}"},
			{name: "<@spring.message code='httpDataSet.requestMethod.PUT' />", value: "${HttpDataSet.REQUEST_METHOD_PUT}"},
			{name: "<@spring.message code='httpDataSet.requestMethod.GET' />", value: "${HttpDataSet.REQUEST_METHOD_PATCH}"},
			{name: "<@spring.message code='httpDataSet.requestMethod.DELETE' />", value: "${HttpDataSet.REQUEST_METHOD_DELETE}"}
		],
		requestContentTypeOptions:
		[
			{name: "<@spring.message code='httpDataSet.requestContentType.FORM_URLENCODED' />", value: "${HttpDataSet.REQUEST_CONTENT_TYPE_FORM_URLENCODED}"},
			{name: "<@spring.message code='httpDataSet.requestContentType.JSON' />", value: "${HttpDataSet.REQUEST_CONTENT_TYPE_JSON}"}
		],
		requestContentCharsetOptions: $.unescapeHtmlForJson(<@writeJson var=availableCharsetNames />),
		responseContentTypeOptions:
		[
			{name: "<@spring.message code='httpDataSet.responseContentType.JSON' />", value: "${HttpDataSet.RESPONSE_CONTENT_TYPE_JSON}"}
		]
	});
	
	po.vueMounted(function()
	{
		var fm = po.vueFormModel();
		
		po.requestContentEditor = po.createWorkspaceEditor(po.elementOfId("${pid}requestContentEditor"),
									{mode: {name: "javascript", json: true}});
		po.setCodeTextTimeout(po.requestContentEditor, fm.requestContent);

		po.headerContentEditor = po.createWorkspaceEditor(po.elementOfId("${pid}headerContentEditor"),
									{mode: {name: "javascript", json: true}});
		po.setCodeTextTimeout(po.headerContentEditor, fm.headerContent);
	});
	
	po.vueMount();
})
(${pid});
</script>
</body>
</html>
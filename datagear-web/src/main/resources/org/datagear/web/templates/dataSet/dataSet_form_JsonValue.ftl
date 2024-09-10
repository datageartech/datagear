<#--
 *
 * Copyright 2018-present datagear.tech
 *
 * This file is part of DataGear.
 *
 * DataGear is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * DataGear is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with DataGear.
 * If not, see <https://www.gnu.org/licenses/>.
 *
-->
<#assign DataSetEntity=statics['org.datagear.management.domain.DataSetEntity']>
<#include "../include/page_import.ftl">
<#include "../include/html_doctype.ftl">
<html>
<head>
<#include "../include/html_head.ftl">
<title>
	<@spring.message code='module.dataSet.JsonValue' />
	<#include "../include/html_request_action_suffix.ftl">
	<#include "../include/html_app_name_suffix.ftl">
</title>
</head>
<body class="p-card no-border">
<#include "../include/page_obj.ftl">
<div id="${pid}" class="page page-form horizontal page-form-dataSet page-form-dataSet-JsonValue">
	<form id="${pid}form" class="flex flex-column show-foot" :class="{readonly: pm.isReadonlyAction}">
		<div class="page-form-content flex-grow-1 px-2 py-1 overflow-y-auto">
			<#include "include/dataSet_form_name.ftl">
			<div class="field grid">
				<label for="${pid}value" class="field-label col-12 mb-2"
					title="<@spring.message code='jsonValueDataSetEntity.value.desc' />">
					<@spring.message code='jsonText' />
				</label>
		        <div class="field-input col-12">
		        	<div id="${pid}value" class="code-editor-wrapper input p-component p-inputtext w-full">
						<div id="${pid}codeEditor" class="code-editor"></div>
					</div>
		        	<div class="validate-msg">
		        		<input name="value" required type="text" class="validate-normalizer" />
		        	</div>
		        </div>
			</div>
			<#include "include/dataSet_form_param_field.ftl">
		</div>
		<div class="page-form-foot flex-grow-0 flex justify-content-center gap-2 pt-2">
			<#include "include/dataSet_form_preview.ftl">
			<p-button type="submit" label="<@spring.message code='save' />" class="hide-if-readonly"></p-button>
		</div>
	</form>
	<#include "include/dataSet_form_param_field_form.ftl">
</div>
<#include "../include/page_form.ftl">
<#include "../include/page_simple_form.ftl">
<#include "../include/page_code_editor.ftl">
<#include "../include/page_boolean_options.ftl">
<#include "include/dataSet_form_js.ftl">
<script>
(function(po)
{
	po.submitUrl = "/dataSet/"+po.submitAction;
	po.previewUrl = "/dataSet/preview/${DataSetEntity.DATA_SET_TYPE_JsonValue}";
	
	po.inflatePreviewFingerprint = function(fingerprint, dataSet)
	{
		fingerprint.value = dataSet.value;
	};
	
	po.beforeSubmitForm = function(action)
	{
		var data = action.options.data;
		data.value = po.getCodeText(po.codeEditor);
		
		if(!po.beforeSubmitFormWithPreview(action))
			return false;
	};
	
	var formModel = $.unescapeHtmlForJson(<@writeJson var=formModel />);
	po.inflateDataSetModel(formModel);
	
	po.setupForm(formModel,
	{
		//查看页面[预览]需要禁用此项
		ignoreIfViewAction: false
	},
	{
		customNormalizers:
		{
			value: function()
			{
				return po.getCodeText(po.codeEditor);
			}
		},
		invalidHandler: function()
		{
			po.handlePreviewInvalidForm();
		}
	});
	
	po.vueMounted(function()
	{
		var fm = po.vueFormModel();
		
		po.codeEditor = po.createWorkspaceEditor(po.elementOfId("${pid}codeEditor"));
		po.setCodeTextTimeout(po.codeEditor, fm.value);
	});
})
(${pid});
</script>
<#include "../include/page_vue_mount.ftl">
</body>
</html>
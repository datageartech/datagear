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
<html>
<head>
<#include "../include/html_head.ftl">
<title>
	<#include "../include/html_app_name_prefix.ftl">
	<@spring.message code='module.dataSet.CsvValue' />
	<#include "../include/html_request_action_suffix.ftl">
</title>
</head>
<body class="p-card no-border">
<#include "../include/page_obj.ftl">
<div id="${pid}" class="page page-form horizontal page-form-dataSet  page-form-dataSet-CsvValue">
	<form id="${pid}form" class="flex flex-column" :class="{readonly: pm.isReadonlyAction}">
		<div class="page-form-content flex-grow-1 px-2 py-1 overflow-y-auto">
			<#include "include/dataSet_form_name.ftl">
			<div class="field grid">
				<label for="${pid}value" class="field-label col-12 mb-2"
					title="<@spring.message code='csvValueDataSetEntity.value.desc' />">
					<@spring.message code='csvText' />
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
			<div class="field grid">
				<label for="${pid}nameRow" class="field-label col-12 mb-2 md:col-3 md:mb-0"
					title="<@spring.message code='dataSet.nameRowNumber.desc' />">
					<@spring.message code='titleRowNumber' />
				</label>
				<div class="field-input col-12 md:col-9">
					<p-inputtext id="${pid}nameRow" v-model="fm.nameRow" type="text" class="input w-full"
						name="nameRow" required maxlength="10">
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
	po.previewUrl = "/dataSet/previewCsvValue";
	
	po.inflatePreviewFingerprint = function(fingerprint, dataSet)
	{
		fingerprint.value = dataSet.value;
		fingerprint.nameRow = dataSet.nameRow;
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
	
	po.setupForm(formModel, {},
	{
		rules :
		{
			"nameRow": {"integer": true},
		},
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
	
	po.vueMount();
})
(${pid});
</script>
</body>
</html>
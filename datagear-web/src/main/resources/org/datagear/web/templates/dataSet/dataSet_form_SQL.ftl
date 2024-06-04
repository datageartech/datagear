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
<#include "../include/page_import.ftl">
<#include "../include/html_doctype.ftl">
<html>
<head>
<#include "../include/html_head.ftl">
<title>
	<@spring.message code='module.dataSet.SQL' />
	<#include "../include/html_request_action_suffix.ftl">
	<#include "../include/html_app_name_suffix.ftl">
</title>
</head>
<body class="p-card no-border">
<#include "../include/page_obj.ftl">
<div id="${pid}" class="page page-form horizontal page-form-dataSet  page-form-dataSet-SQL">
	<form id="${pid}form" class="flex flex-column show-foot" :class="{readonly: pm.isReadonlyAction}">
		<div class="page-form-content flex-grow-1 px-2 py-1 overflow-y-auto">
			<#include "include/dataSet_form_name.ftl">
			<div class="field grid">
				<label for="${pid}dataSource" class="field-label col-12 mb-2 md:col-3 md:mb-0">
					<@spring.message code='dataSource' />
				</label>
		        <div class="field-input col-12 md:col-9">
		        	<div class="p-inputgroup">
		        		<p-inputtext id="${pid}dataSource" v-model="fm.dtbsConFactory.schema.title" type="text" class="input"
			        		readonly="readonly" name="dtbsConFactory.schema.title" required maxlength="200">
			        	</p-inputtext>
			        	<p-button type="button" label="<@spring.message code='select' />"
			        		@click="onSelectSchema" v-if="!pm.isReadonlyAction">
			        	</p-button>
		        	</div>
		        </div>
			</div>
			<div class="field grid">
				<label for="${pid}sql" class="field-label col-12 mb-2"
					title="<@spring.message code='sqlDataSet.url.desc' />">
					<@spring.message code='sql' />
				</label>
		        <div class="field-input col-12">
		        	<div id="${pid}sql" class="code-editor-wrapper input p-component p-inputtext w-full">
						<div id="${pid}codeEditor" class="code-editor"></div>
					</div>
		        	<div class="validate-msg">
		        		<input name="sql" required type="text" class="validate-normalizer" />
		        	</div>
		        </div>
			</div>
			<#include "include/dataSet_form_param_property.ftl">
		</div>
		<div class="page-form-foot flex-grow-0 flex justify-content-center gap-2 pt-2">
			<#include "include/dataSet_form_preview.ftl">
			<p-button type="submit" label="<@spring.message code='save' />" class="hide-if-readonly"></p-button>
		</div>
	</form>
	<#include "include/dataSet_form_param_property_form.ftl">
</div>
<#include "../include/page_form.ftl">
<#include "../include/page_simple_form.ftl">
<#include "../include/page_code_editor.ftl">
<#include "../include/page_sql_editor.ftl">
<#include "../include/page_boolean_options.ftl">
<#include "include/dataSet_form_js.ftl">
<script>
(function(po)
{
	po.submitUrl = "/dataSet/"+po.submitAction;
	po.previewUrl = "/dataSet/previewSQL";
	
	po.inflatePreviewFingerprint = function(fingerprint, dataSet)
	{
		fingerprint.schemaId = dataSet.dtbsConFactory.schema.id;
		fingerprint.sql = dataSet.sql;
	};
	
	po.beforeSubmitForm = function(action)
	{
		var data = action.options.data;
		data.sql = po.getCodeText(po.codeEditor);
		data.connectionFactory = undefined;
		
		if(!po.beforeSubmitFormWithPreview(action))
			return false;
	};
	
	po.getSqlEditorSchemaId = function()
	{
		var fm = po.vueFormModel();
		return fm.dtbsConFactory.schema.id;
	};
	
	var formModel = $.unescapeHtmlForJson(<@writeJson var=formModel />);
	po.inflateDataSetModel(formModel);
	formModel.dtbsConFactory = (formModel.dtbsConFactory == null ? { schema: {} } : formModel.dtbsConFactory);
	formModel.dtbsConFactory.schema = (formModel.dtbsConFactory.schema == null ? {} : formModel.dtbsConFactory.schema);
	
	po.setupForm(formModel,
	{
		//查看页面[预览]需要禁用此项
		ignoreIfViewAction: false
	},
	{
		customNormalizers:
		{
			sql: function()
			{
				return po.getCodeText(po.codeEditor);
			}
		},
		invalidHandler: function()
		{
			po.handlePreviewInvalidForm();
		}
	});
	
	po.vueMethod(
	{
		onSelectSchema: function()
		{
			po.handleOpenSelectAction("/schema/select", function(schema)
			{
				var fm = po.vueFormModel();
				fm.dtbsConFactory.schema = schema;
			});
		}
	});
	
	po.vueMounted(function()
	{
		var fm = po.vueFormModel();
		
		po.codeEditor = po.createWorkspaceEditor(po.elementOfId("${pid}codeEditor"),
							po.inflateSqlEditorOptions({ value: "" }));
		po.setCodeTextTimeout(po.codeEditor, fm.sql);
	});
})
(${pid});
</script>
<#include "../include/page_vue_mount.ftl">
</body>
</html>
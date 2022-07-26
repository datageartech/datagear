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
	<@spring.message code='module.dataSet.SQL' />
	<#include "../include/html_request_action_suffix.ftl">
</title>
</head>
<body class="p-card no-border">
<#include "../include/page_obj.ftl">
<div id="${pid}" class="page page-form horizontal page-form-dataSet  page-form-dataSet-sql">
	<form class="flex flex-column" :class="{readonly: isReadonlyAction}">
		<div class="page-form-content flex-grow-1 pr-2 py-1 overflow-y-auto">
			<#include "include/dataSet_input_base.ftl">
			<div class="field grid">
				<label for="${pid}dataSource" class="field-label col-12 mb-2 md:col-3 md:mb-0">
					<@spring.message code='dataSource' />
				</label>
		        <div class="field-input col-12 md:col-9">
		        	<div class="p-inputgroup">
		        		<div class="p-input-icon-right flex-grow-1">
			        		<i class="pi pi-times cursor-pointer" @click="onDeleteSchema" v-if="!isReadonlyAction">
			        		</i>
				        	<p-inputtext id="${pid}dataSource" v-model="pm.shmConFactory.schema.title" type="text" class="input w-full h-full border-noround-right"
				        		readonly="readonly" name="shmConFactory.schema.title" required maxlength="200">
				        	</p-inputtext>
			        	</div>
			        	<p-button type="button" label="<@spring.message code='select' />"
			        		@click="onSelectSchema" class="p-button-secondary"
			        		v-if="!isReadonlyAction">
			        	</p-button>
		        	</div>
		        </div>
			</div>
			<div class="field grid">
				<label for="${pid}sql" class="field-label col-12 mb-2 md:col-3 md:mb-0">
					<@spring.message code='sql' />
				</label>
		        <div class="field-input col-12 md:col-9">
		        	<div id="${pid}sql" class="code-editor-wrapper input p-component p-inputtext w-full">
						<div id="${pid}codeEditor" class="code-editor"></div>
					</div>
		        	<div class="validate-msg">
		        		<input name="sql" required type="text" class="validate-proxy" />
		        	</div>
		        </div>
			</div>
			<#include "include/dataSet_input_param_property.ftl">
		</div>
		<div class="page-form-foot flex-grow-0 pt-3 text-center h-opts">
			<p-button type="button" label="<@spring.message code='preview' />"
        		@click="onPreview" class="p-button-secondary">
        	</p-button>
			<p-button type="submit" label="<@spring.message code='save' />"></p-button>
		</div>
	</form>
</div>
<#include "../include/page_form.ftl">
<#include "../include/page_code_editor.ftl">
<#include "../include/page_sql_editor.ftl">
<#include "../include/page_boolean_options.ftl">
<#include "include/dataSet_form_js.ftl">
<script>
(function(po)
{
	po.submitUrl = "/dataSet/"+po.submitAction;
	
	po.inflateSubmitAction = function(action, data)
	{
		data.sql = po.getCodeText(po.codeEditor);
	};
	
	var formModel = <@writeJson var=formModel />;
	formModel = $.unescapeHtmlForJson(formModel);
	po.inflateDataSetModel(formModel);
	formModel.shmConFactory = (formModel.shmConFactory == null ? { schema: {} } : formModel.shmConFactory);
	formModel.shmConFactory.schema = (formModel.shmConFactory.schema == null ? {} : formModel.shmConFactory.schema);
	
	po.setupForm(formModel, po.submitUrl);
	
	po.vueMethod(
	{
		onDeleteSchema: function()
		{
			var pm = po.vuePageModel();
			pm.shmConFactory.schema = {};
		},
		
		onSelectSchema: function()
		{
			po.handleOpenSelectAction("/schema/select", function(schema)
			{
				var pm = po.vuePageModel();
				pm.shmConFactory.schema = schema;
			});
		}
	});
	
	po.vueMounted(function()
	{
		var pm = po.vuePageModel();
		
		po.codeEditor = po.createWorkspaceEditor(po.elementOfId("${pid}codeEditor"),
							po.inflateSqlEditorOptions({ value: "" }));
		po.setCodeTextTimeout(po.codeEditor, pm.sql);
	});
	
	po.vueMount();
})
(${pid});
</script>
</body>
</html>
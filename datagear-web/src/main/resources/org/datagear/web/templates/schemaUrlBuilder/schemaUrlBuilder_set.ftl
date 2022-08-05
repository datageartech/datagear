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
	<@spring.message code='module.schemaUrlBuilder' />
	<#include "../include/html_request_action_suffix.ftl">
</title>
</head>
<body class="p-card no-border">
<#include "../include/page_obj.ftl">
<div id="${pid}" class="page page-form horizontal page-form-schema-ub">
	<form class="flex flex-column" :class="{readonly: pm.isReadonlyAction}">
		<div class="page-form-content flex-grow-1 px-2 py-1 overflow-y-auto">
			<div class="grid grid-nogutter">
				<div class="col-8">
					<div class="field grid">
						<label for="${pid}code" class="field-label col-12 mb-2 md:col-3 md:mb-0">
							<@spring.message code='code' />
						</label>
				        <div class="field-input col-12 md:col-9">
				        	<div id="${pid}code" class="code-editor-wrapper input p-component p-inputtext w-full">
								<div id="${pid}codeEditor" class="code-editor"></div>
							</div>
				        </div>
					</div>
				</div>
				<div class="col-4">
					<div class="flex flex-column pl-2">
						<div class="flex-grow-0">
							<@spring.message code='example' /> :
						</div>
<pre class="flex-grow-1 overflow-auto p-component p-inputtext">
{
  //<@spring.message code='schemaUrlBuilder.dbType.desc' />
  dbType : "MySQL",
 
  //<@spring.message code='schemaUrlBuilder.template.desc' />
  template : "jdbc:mysql://{host}:{port}/{name}",
  
  //<@spring.message code='schemaUrlBuilder.defaultValue.desc' />
  defaultValue : { host : "", port : "3306", name : "" }
}
</pre>
						<div class="flex-grow-0">
							<@spring.message code='schemaUrlBuilder.code.desc' />
						</div>
					</div>
				</div>
			</div>
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
<script>
(function(po)
{
	po.submitUrl = "/schemaUrlBuilder/saveSet";
	
	po.beforeSubmitForm = function(action)
	{
		action.options.data.code = po.getCodeText(po.codeEditor);
	};
	
	var formModel = $.unescapeHtmlForJson(<@writeJson var=formModel />);
	po.setupForm(formModel, { closeAfterSubmit: false });
	
	po.vueMethod(
	{
		onPreview: function()
		{
			var fm = po.vueFormModel();
			
			po.open("/schemaUrlBuilder/preview",
			{
				data:
				{
					scriptCode: po.getCodeText(po.codeEditor)
				}
			});
		}
	});
	
	po.vueMounted(function()
	{
		var fm = po.vueFormModel();
		
		var codeEditorOptions =
		{
			value: "",
			matchBrackets: true,
			matchTags: true,
			autoCloseTags: true,
			mode: {name: "javascript", json: true}
		};
		
		po.codeEditor = po.createCodeEditor(po.elementOfId("${pid}codeEditor"), codeEditorOptions);
		po.setCodeTextTimeout(po.codeEditor, fm.code, true);
	});
	
	po.vueMount();
})
(${pid});
</script>
</body>
</html>
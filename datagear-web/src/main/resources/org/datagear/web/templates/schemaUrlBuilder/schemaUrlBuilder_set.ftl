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
<div id="${pid}" class="page page-form horizontal">
	<form class="flex flex-column" :class="{readonly: isReadonlyAction}">
		<div class="page-form-content flex-grow-1 pr-2 py-1 overflow-y-auto">
			<div class="grid grid-nogutter">
				<div class="col-8">
					<div class="field grid">
						<label for="${pid}code" class="field-label col-12 mb-2 md:col-3 md:mb-0">
							<@spring.message code='code' />
						</label>
				        <div class="field-input col-12 md:col-9">
				        	<p-textarea id="${pid}code" v-model="pm.code" rows="16" class="input w-full"
				        		name="code" maxlength="10000" autofocus>
				        	</p-textarea>
				        </div>
					</div>
					<div class="field grid">
						<label for="${pid}preview" class="field-label col-12 mb-2 md:col-3 md:mb-0">
							&nbsp;
						</label>
				        <div class="field-input col-12 md:col-9">
				        	<p-button type="button" label="<@spring.message code='preview' />"
				        		@click="onPreview" class="p-button-secondary">
				        	</p-button>
				        </div>
					</div>
				</div>
				<div class="col-4">
					<div class="flex flex-column">
						<div class="flex-grow-0">
							<@spring.message code='example' /> :
						</div>
						<pre class="flex-grow-1 overflow-auto border-round border-solid border-1 surface-border">
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
		<div class="page-form-foot flex-grow-0 pt-3 text-center">
			<p-button type="submit" label="<@spring.message code='save' />"></p-button>
		</div>
	</form>
</div>
<#include "../include/page_form.ftl">
<script>
(function(po)
{
	var formModel = <@writeJson var=formModel />;
	
	po.setupForm(formModel, "/schemaUrlBuilder/saveSet", { closeAfterSubmit: false });
	
	po.vueMethod(
	{
		onPreview: function()
		{
			var pm = po.vuePageModel();
			
			po.open("/schemaUrlBuilder/preview",
			{
				data:
				{
					scriptCode: pm.code
				}
			});
		}
	});
	
	po.vueMount();
})
(${pid});
</script>
</body>
</html>
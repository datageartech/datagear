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
	<@spring.message code='module.schemaGuard' />
	<#include "../include/html_request_action_suffix.ftl">
</title>
</head>
<body class="p-card no-border">
<#include "../include/page_obj.ftl">
<div id="${pid}" class="page page-form horizontal">
	<form class="flex flex-column" :class="{readonly: isReadonlyAction}">
		<div class="page-form-content flex-grow-1 px-2 py-1 overflow-y-auto">
			<div class="field grid">
				<label for="${pid}url" class="field-label col-12 mb-2 md:col-3 md:mb-0"
					title="<@spring.message code='schema.url.desc' />">
					<@spring.message code='url' />
				</label>
		        <div class="field-input col-12 md:col-9">
		        	<div class="p-inputgroup">
			        	<p-inputtext id="${pid}url" v-model="pm.url" type="text" class="input"
			        		name="url" required maxlength="2000" placeholder="jdbc:">
			        	</p-inputtext>
			        	<p-button type="button" label="<@spring.message code='help' />" @click="onBuildSchemaUrl"
			        		class="p-button-secondary">
			        	</p-button>
		        	</div>
		        </div>
			</div>
		</div>
		<div class="page-form-foot flex-grow-0 pt-3 text-center">
			<p-button type="submit" label="<@spring.message code='test' />"></p-button>
		</div>
		<div class="page-form-foot flex-grow-0 pt-3 text-center" style="min-height:6.2rem;">
			<div class="p-component py-1">{{testResult.url}}</div>
			<div class="p-component py-1">
				<p-inlinemessage severity="success" v-if="testResult.result=='true'">
					<@spring.message code='creationPermitted' />
				</p-inlinemessage>
				<p-inlinemessage severity="error" v-if="testResult.result=='false'">
					<@spring.message code='creationDenied' />
				</p-inlinemessage>
			</div>
		</div>
	</form>
</div>
<#include "../include/page_form.ftl">
<script>
(function(po)
{
	po.submitUrl = "/schemaGuard/"+po.submitAction;
	
	po.vueRef("testResult", {});
	
	po.setupForm({ url: "" },
	{
		tipSuccess: false,
		closeAfterSubmit: false,
		success: function(response)
		{
			var pm = po.vuePageModel();
			po.vueRef("testResult", {url: pm.url, result: (response.data ? "true" : "false")});
		}
	},
	{
		rules :
		{
			priority : "integer"
		}
	});
	
	po.vueMethod(
	{
		onBuildSchemaUrl: function()
		{
			var pm = po.vuePageModel();
			
			po.open("/schemaUrlBuilder/build",
			{
				data: {url: pm.url},
				contentType: $.CONTENT_TYPE_FORM,
				pageParam:
				{
					submitSuccess: function(url)
					{
						var pm = po.vuePageModel();
						pm.url = url;
					}
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
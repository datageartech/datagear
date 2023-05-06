<#--
 *
 * Copyright 2018-2023 datagear.tech
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
	<@spring.message code='module.schemaGuard' />
	<#include "../include/html_request_action_suffix.ftl">
	<#include "../include/html_app_name_suffix.ftl">
</title>
</head>
<body class="p-card no-border">
<#include "../include/page_obj.ftl">
<div id="${pid}" class="page page-form horizontal">
	<form id="${pid}form" class="flex flex-column" :class="{readonly: pm.isReadonlyAction}">
		<div class="page-form-content flex-grow-1 px-2 py-1 overflow-y-auto">
			<div class="field grid">
				<label for="${pid}url" class="field-label col-12 mb-2 md:col-3 md:mb-0"
					title="<@spring.message code='schema.url.desc' />">
					<@spring.message code='url' />
				</label>
		        <div class="field-input col-12 md:col-9">
		        	<div class="p-inputgroup">
			        	<p-inputtext id="${pid}url" v-model="fm.url" type="text" class="input"
			        		name="url" required maxlength="2000" placeholder="jdbc:">
			        	</p-inputtext>
			        	<p-button type="button" label="<@spring.message code='help' />" @click="onBuildSchemaUrl"
			        		class="p-button-secondary">
			        	</p-button>
		        	</div>
		        </div>
			</div>
		</div>
		<div class="page-form-foot flex-grow-0 flex justify-content-center gap-2 pt-2">
			<p-button type="submit" label="<@spring.message code='test' />"></p-button>
		</div>
		<div class="page-form-foot flex-grow-0 flex flex-column justify-content-center align-items-center gap-2 pt-2" style="min-height:6.5rem;">
			<div class="p-component py-1">{{pm.testResult.url}}</div>
			<div class="p-component py-1">
				<p-inlinemessage severity="success" v-if="pm.testResult.result=='true'">
					<@spring.message code='creationPermitted' />
				</p-inlinemessage>
				<p-inlinemessage severity="error" v-if="pm.testResult.result=='false'">
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
	
	po.vuePageModel(
	{
		testResult: {}
	});
	
	po.setupForm({ url: "" },
	{
		tipSuccess: false,
		closeAfterSubmit: false,
		success: function(response)
		{
			var fm = po.vueFormModel();
			var pm = po.vuePageModel();
			
			pm.testResult = {url: fm.url, result: (response.data ? "true" : "false")};
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
			var fm = po.vueFormModel();
			
			po.open("/schemaUrlBuilder/build",
			{
				data: {url: fm.url},
				contentType: $.CONTENT_TYPE_FORM,
				pageParam:
				{
					submitSuccess: function(url)
					{
						var fm = po.vueFormModel();
						fm.url = url;
					}
				}
			});
		}
	});
})
(${pid});
</script>
<#include "../include/page_vue_mount.ftl">
</body>
</html>
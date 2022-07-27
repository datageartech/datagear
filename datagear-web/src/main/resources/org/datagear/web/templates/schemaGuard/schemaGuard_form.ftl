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
		<div class="page-form-content flex-grow-1 pr-2 py-1 overflow-y-auto">
			<div class="field grid">
				<label for="${pid}pattern" class="field-label col-12 mb-2 md:col-3 md:mb-0"
					title="<@spring.message code='schemaGuard.pattern.desc' />">
					<@spring.message code='urlPattern' />
				</label>
		        <div class="field-input col-12 md:col-9">
		        	<p-inputtext id="${pid}pattern" v-model="pm.pattern" type="text" class="input w-full"
		        		name="pattern" required maxlength="200" autofocus>
		        	</p-inputtext>
		        </div>
			</div>
			<div class="field grid">
				<label for="${pid}permitted" class="field-label col-12 mb-2 md:col-3 md:mb-0"
					title="<@spring.message code='schemaGuard.permitted.desc' />">
					<@spring.message code='isPermit' />
				</label>
		        <div class="field-input col-12 md:col-9">
		        	<p-selectbutton id="${pid}permitted" v-model="pm.permitted" :options="booleanOptions"
		        		option-label="name" option-value="value" class="input w-full">
		        	</p-selectbutton>
		        </div>
			</div>
			<div class="field grid">
				<label for="${pid}priority" class="field-label col-12 mb-2 md:col-3 md:mb-0"
					title="<@spring.message code='schemaGuard.priority.desc' />">
					<@spring.message code='priority' />
				</label>
		        <div class="field-input col-12 md:col-9">
		        	<p-inputtext id="${pid}priority" v-model="pm.priority" type="text" class="input w-full"
		        		name="priority" required maxlength="10">
		        	</p-inputtext>
		        </div>
			</div>
			<div class="field grid">
				<label for="${pid}enabled" class="field-label col-12 mb-2 md:col-3 md:mb-0">
					<@spring.message code='isEnable' />
				</label>
		        <div class="field-input col-12 md:col-9">
		        	<p-selectbutton id="${pid}enabled" v-model="pm.enabled" :options="booleanOptions"
		        		option-label="name" option-value="value" class="input w-full">
		        	</p-selectbutton>
		        </div>
			</div>
		</div>
		<div class="page-form-foot flex-grow-0 pt-3 text-center">
			<p-button type="submit" label="<@spring.message code='save' />"></p-button>
		</div>
	</form>
</div>
<#include "../include/page_form.ftl">
<#include "../include/page_boolean_options.ftl">
<script>
(function(po)
{
	po.submitUrl = "/schemaGuard/"+po.submitAction;
	
	var formModel = <@writeJson var=formModel />;
	formModel = $.unescapeHtmlForJson(formModel);
	po.setupForm(formModel, {},
	{
		rules :
		{
			priority : "integer"
		}
	});
	
	po.vueMount();
})
(${pid});
</script>
</body>
</html>
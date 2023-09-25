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
				<label for="${pid}pattern" class="field-label col-12 mb-2 md:col-3 md:mb-0"
					title="<@spring.message code='schemaGuard.pattern.desc' />">
					<@spring.message code='urlPattern' />
				</label>
		        <div class="field-input col-12 md:col-9">
		        	<p-inputtext id="${pid}pattern" v-model="fm.pattern" type="text" class="input w-full"
		        		name="pattern" required maxlength="200" autofocus>
		        	</p-inputtext>
		        </div>
			</div>
			<div class="field grid">
				<label for="${pid}userPattern" class="field-label col-12 mb-2 md:col-3 md:mb-0"
					title="<@spring.message code='schemaGuard.userPattern.desc' />">
					<@spring.message code='usernamePattern' />
				</label>
		        <div class="field-input col-12 md:col-9">
		        	<p-inputtext id="${pid}userPattern" v-model="fm.userPattern" type="text" class="input w-full"
		        		name="userPattern" maxlength="200">
		        	</p-inputtext>
		        </div>
			</div>
			<div class="field grid">
				<label for="${pid}permitted" class="field-label col-12 mb-2 md:col-3 md:mb-0"
					title="<@spring.message code='schemaGuard.permitted.desc' />">
					<@spring.message code='isPermit' />
				</label>
		        <div class="field-input col-12 md:col-9">
		        	<p-selectbutton id="${pid}permitted" v-model="fm.permitted" :options="pm.booleanOptions"
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
		        	<p-inputtext id="${pid}priority" v-model="fm.priority" type="text" class="input w-full"
		        		name="priority" required maxlength="10">
		        	</p-inputtext>
		        </div>
			</div>
			<div class="field grid">
				<label for="${pid}enabled" class="field-label col-12 mb-2 md:col-3 md:mb-0">
					<@spring.message code='isEnable' />
				</label>
		        <div class="field-input col-12 md:col-9">
		        	<p-selectbutton id="${pid}enabled" v-model="fm.enabled" :options="pm.booleanOptions"
		        		option-label="name" option-value="value" class="input w-full">
		        	</p-selectbutton>
		        </div>
			</div>
		</div>
		<div class="page-form-foot flex-grow-0 flex justify-content-center gap-2 pt-2">
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
	
	var formModel = $.unescapeHtmlForJson(<@writeJson var=formModel />);
	po.setupForm(formModel, {},
	{
		rules :
		{
			priority : "integer"
		}
	});
})
(${pid});
</script>
<#include "../include/page_vue_mount.ftl">
</body>
</html>
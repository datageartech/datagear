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
	<@spring.message code='module.shareSet' />
	<#include "../include/html_app_name_suffix.ftl">
</title>
</head>
<body class="p-card no-border">
<#include "../include/page_obj.ftl">
<div id="${pid}" class="page page-form horizontal">
	<form id="${pid}form" class="flex flex-column" :class="{readonly: pm.isReadonlyAction}">
		<div class="page-form-content flex-grow-1 px-2 py-1 overflow-y-auto">
			<div class="field grid">
				<label for="${pid}enablePassword" class="field-label col-12 mb-2 md:col-3 md:mb-0">
					<@spring.message code='dashboardShareSet.enablePassword' />
				</label>
		        <div class="field-input col-12 md:col-9">
		        	<p-selectbutton id="${pid}enablePassword" v-model="fm.enablePassword" :options="pm.booleanOptions"
		        		option-label="name" option-value="value" class="input w-full">
		        	</p-selectbutton>
		        </div>
			</div>
			<div class="field grid" :class="fm.enablePassword ? '' : 'opacity-0'">
				<label for="${pid}anonymousPassword" class="field-label col-12 mb-2 md:col-3 md:mb-0"
					title="<@spring.message code='dashboardShareSet.anonymousPassword.desc' />">
					<@spring.message code='dashboardShareSet.anonymousPassword' />
				</label>
		        <div class="field-input col-12 md:col-9">
		        	<p-selectbutton id="${pid}anonymousPassword" v-model="fm.anonymousPassword" :options="pm.booleanOptions"
		        		option-label="name" option-value="value" class="input w-full">
		        	</p-selectbutton>
		        </div>
			</div>
			<div class="field grid" :class="fm.enablePassword ? '' : 'opacity-0'">
				<label for="${pid}password" class="field-label col-12 mb-2 md:col-3 md:mb-0">
					<@spring.message code='dashboardShareSet.password' />
				</label>
		        <div class="field-input col-12 md:col-9">
		        	<p-password id="${pid}password" v-model="fm.password" class="input w-full"
		        		input-class="w-full" toggle-mask :feedback="false"
		        		name="password" :required="fm.enablePassword" maxlength="20" autocomplete="new-password">
		        	</p-password>
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
	po.submitUrl = "/dashboard/"+po.submitAction;
	
	var formModel = $.unescapeHtmlForJson(<@writeJson var=formModel />);
	po.setupForm(formModel);
	
	po.vueMethod(
	{
	});
	
	po.vueMount();
})
(${pid});
</script>
</body>
</html>
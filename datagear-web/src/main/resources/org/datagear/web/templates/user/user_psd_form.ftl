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
	<@spring.message code='module.user' />
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
				<label for="${pid}name" class="field-label col-12 mb-2 md:col-3 md:mb-0">
					<@spring.message code='username' />
				</label>
		        <div class="field-input col-12 md:col-9">
		        	<p-inputtext id="${pid}name" v-model="fm.name" type="text" class="input w-full"
		        		name="name" required maxlength="50" readonly="readonly">
		        	</p-inputtext>
		        </div>
			</div>
			<#if enableOldPassword>
			<div class="field grid">
				<label for="${pid}oldPassword" class="field-label col-12 mb-2 md:col-3 md:mb-0">
					<@spring.message code='oldPassword' />
				</label>
		        <div class="field-input col-12 md:col-9">
		        	<p-password id="${pid}oldPassword" v-model="fm.oldPassword" class="input w-full"
		        		input-class="w-full" toggle-mask :feedback="false" required
		        		:pt="{input:{name:'oldPassword',maxlength:'50',autofocus:'true',autocomplete:'new-password'}}">
		        	</p-password>
		        </div>
			</div>
			</#if>
			<div class="field grid">
				<label for="${pid}password" class="field-label col-12 mb-2 md:col-3 md:mb-0">
					<@spring.message code='newPassword' />
				</label>
		        <div class="field-input col-12 md:col-9">
		        	<p-password id="${pid}password" v-model="fm.password" class="input w-full"
		        		input-class="w-full" toggle-mask :feedback="false" required
		        		:pt="{input:{name:'password',maxlength:'50'<#if !enableOldPassword>,autofocus:'true'</#if>,autocomplete:'new-password'}}">
		        	</p-password>
		        	<div class="desc text-color-secondary" v-if="pm.userPasswordStrengthTip != ''">
		        		<small>{{pm.userPasswordStrengthTip}}</small>
		        	</div>
		        </div>
			</div>
			<div class="field grid">
				<label for="${pid}confirmPassword" class="field-label col-12 mb-2 md:col-3 md:mb-0">
					<@spring.message code='confirmPassword' />
				</label>
		        <div class="field-input col-12 md:col-9">
		        	<p-password id="${pid}confirmPassword" v-model="fm.confirmPassword" class="input w-full"
		        		input-class="w-full" toggle-mask :feedback="false" required
		        		:pt="{input:{name:'confirmPassword',maxlength:'50',autocomplete:'new-password'}}">
		        	</p-password>
		        </div>
			</div>
		</div>
		<div class="page-form-foot flex-grow-0 flex justify-content-center gap-2 pt-2">
			<p-button type="submit" label="<@spring.message code='save' />"></p-button>
		</div>
	</form>
</div>
<#include "../include/page_form.ftl">
<script>
(function(po)
{
	po.submitUrl = "/user/"+po.submitAction;
	po.enableOldPassword = ("${(enableOldPassword!true)?string('true', 'false')}"  == "true");
	po.userPasswordStrengthTip = "${userPasswordStrengthTip}";

	po.beforeSubmitForm = function(action)
	{
		var data = action.options.data;
		data.confirmPassword = undefined;
	};

	po.vuePageModel(
	{
		userPasswordStrengthTip: po.userPasswordStrengthTip
	});
	
	var formModel = $.unescapeHtmlForJson(<@writeJson var=formModel />);
	po.setupForm(formModel, {}, function()
	{
		var options =
		{
			rules:
			{
				"password":
				{
					"pattern" : ${userPasswordStrengthRegex}
				},
				"confirmPassword":
				{
					"equalTo" : po.elementOfName("password")
				}
			}
		};
		
		return options;
	});
})
(${pid});
</script>
<#include "../include/page_vue_mount.ftl">
</body>
</html>
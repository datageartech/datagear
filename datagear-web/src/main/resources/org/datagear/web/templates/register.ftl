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
<#assign RegisterController=statics['org.datagear.web.controller.RegisterController']>
<#include "include/page_import.ftl">
<#include "include/html_doctype.ftl">
<html>
<head>
<#include "include/html_head.ftl">
<title>
	<@spring.message code='module.register' />
	<#include "include/html_app_name_suffix.ftl">
</title>
</head>
<body class="m-0 surface-ground">
<#include "include/page_obj.ftl">
<div id="${pid}" class="page page-form horizontal">
	<div class="flex flex-column h-screen m-0">
		<#include "include/page_main_header.ftl">
		<div class="flex-grow-1 p-0">
			<div class="grid grid-nogutter justify-content-center">
				<p-card class="col-10 md:col-5 p-card mt-6">
					<template #title><@spring.message code='module.register' /></template>
					<template id="${pid}tplDomContent" #content>
					<form id="${pid}form" class="flex flex-column">
						<div class="page-form-content flex-grow-1 px-2 py-1 overflow-y-auto">
							<div class="field grid">
								<label for="${pid}name" class="field-label col-12 mb-2 md:col-3 md:mb-0">
									<@spring.message code='username' />
								</label>
						        <div class="field-input col-12 md:col-9">
						        	<p-inputtext id="${pid}name" v-model="fm.user.name" type="text" class="input w-full"
						        		name="name" required maxlength="50" autofocus>
						        	</p-inputtext>
						        </div>
							</div>
							<div class="field grid">
								<label for="${pid}password" class="field-label col-12 mb-2 md:col-3 md:mb-0">
									<@spring.message code='password' />
								</label>
						        <div class="field-input col-12 md:col-9">
						        	<p-password id="${pid}password" v-model="fm.user.password" class="input w-full"
						        		input-class="w-full" toggle-mask :feedback="false" required
						        		:pt="{input:{name:'password',maxlength:'50',autocomplete:'new-password'}}">
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
						        	<p-password id="${pid}confirmPassword" v-model="fm.user.confirmPassword" class="input w-full"
						        		input-class="w-full" toggle-mask :feedback="false" required
						        		:pt="{input:{name:'confirmPassword',maxlength:'50',autocomplete:'new-password'}}">
						        	</p-password>
						        </div>
							</div>
							<div class="field grid">
								<label for="${pid}realName" class="field-label col-12 mb-2 md:col-3 md:mb-0">
									<@spring.message code='realName' />
								</label>
						        <div class="field-input col-12 md:col-9">
						        	<p-inputtext id="${pid}realName" v-model="fm.user.realName" type="text" class="input w-full"
						        		name="realName" maxlength="50">
						        	</p-inputtext>
						        </div>
							</div>
							<div class="field grid">
								<label for="${pid}checkCode" class="field-label col-12 mb-2 md:col-3 md:mb-0">
									<@spring.message code='checkCode' />
								</label>
						        <div class="field-input col-12 md:col-9">
						        	<p-inputtext id="${pid}checkCode" v-model="fm.checkCode" type="text" class="input w-6"
						        		name="checkCode" required maxlength="10">
						        	</p-inputtext>
						        	<img class="checkCodeImg ml-1 vertical-align-middle" style="height:1.5rem;" />
						        </div>
							</div>
						</div>
						<div class="page-form-foot flex-grow-0 flex justify-content-center gap-2 pt-2">
							<p-button type="submit" label="<@spring.message code='register' />"></p-button>
						</div>
					</form>
					</template>
				</p-card>
			</div>
		</div>
	</div>
</div>
<#include "include/page_form.ftl">
<script>
(function(po)
{
	po.submitUrl = "/register/doRegister";
	po.userPasswordStrengthTip = "${userPasswordStrengthTip}";

	po.beforeSubmitForm = function(action)
	{
		var user = action.options.data.user;
		user.confirmPassword = undefined;
	};

	po.vuePageModel(
	{
		userPasswordStrengthTip: po.userPasswordStrengthTip
	});
	
	po.setupForm({user: {}},
	{
		tipSuccess: false,
		success: function()
		{
			(window.top ? window.top : window).location.href="${contextPath}/register/success";
		},
		error: function(jqXHR)
		{
			po.element(".checkCodeImg").click();
		}
	},
	function()
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
	
	po.vueMounted(function()
	{
		po.element(".checkCodeImg").click(function()
		{
			$(this).attr("src", "${contextPath}/checkCode?_=" + $.uid("rc")+"&m=${RegisterController.CHECK_CODE_MODULE_REGISTER}");
		})
		.click();
	});
})
(${pid});
</script>
<#include "include/page_vue_mount.ftl">
</body>
</html>
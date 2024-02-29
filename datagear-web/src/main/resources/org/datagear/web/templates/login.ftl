<#--
 *
 * Copyright 2018-2024 datagear.tech
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
<#assign LoginController=statics['org.datagear.web.controller.LoginController']>
<#include "include/page_import.ftl">
<#include "include/html_doctype.ftl">
<html>
<head>
<#include "include/html_head.ftl">
<title>
	<@spring.message code='module.login' />
	<#include "include/html_app_name_suffix.ftl">
</title>
</head>
<body class="m-0 surface-ground">
<#include "include/page_obj.ftl">
<div id="${pid}" class="page page-form horizontal page-login">
	<div class="flex flex-column m-0" :class="pm.isNotTopPage ? 'h-auto' : 'h-screen'">
		<#include "include/page_main_header.ftl">
		<div class="flex-grow-1 p-0">
			<div class="grid grid-nogutter justify-content-center">
				<p-card class="p-card col-10" :class="pm.isNotTopPage ? 'md:col-8 mt-1' : 'md:col-5 mt-6'">
					<template #title v-if="!pm.isNotTopPage"><@spring.message code='module.login' /></template>
					<template #content>
					<form id="${pid}form" class="flex flex-column">
						<div class="page-form-content flex-grow-1 px-2 py-1 overflow-y-auto">
							<div class="field grid">
								<label for="${pid}name" class="field-label col-12 mb-2 md:col-3 md:mb-0">
									<@spring.message code='username' />
								</label>
						        <div class="field-input col-12 md:col-9">
						        	<p-inputtext id="${pid}name" v-model="fm.name" type="text" class="input w-full"
						        		name="name" required maxlength="20" autofocus>
						        	</p-inputtext>
						        </div>
							</div>
							<div class="field grid">
								<label for="${pid}password" class="field-label col-12 mb-2 md:col-3 md:mb-0">
									<@spring.message code='password' />
								</label>
						        <div class="field-input col-12 md:col-9">
						        	<p-password id="${pid}password" v-model="fm.password" toggle-mask :feedback="false"
						        		input-class="w-full" class="input w-full" required
						        		:pt="{input:{root:{name:'password',maxlength:'50'}}}">
						        	</p-password>
						        </div>
							</div>
							<div class="field grid" v-if="!pm.disableLoginCheckCode">
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
							<p-button type="submit" label="<@spring.message code='login' />"></p-button>
						</div>
						<div class="page-form-foot flex-grow-0 pt-3 text-right text-color-secondary">
							<p-checkbox input-id="${pid}remremberLogin" v-model="fm.rememberMe" :binary="true" name="remremberLogin"></p-checkbox>
							<label for="${pid}remremberLogin" class="ml-1"><@spring.message code='remremberLogin' /></label>
							
							<a href="${contextPath}/resetPassword" class="link text-color-secondary ml-3"><@spring.message code='forgetPassword' /></a>
						</div>
						<div class="page-form-foot flex-grow-0 pt-3 text-right text-color-secondary" v-if="!pm.disableRegister">
							<a href="${contextPath}/register" class="link text-color-secondary ml-3"><@spring.message code='module.register' /></a>
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
	po.submitUrl = "/login/doLogin";
	
	po.disableLoginCheckCode = ("${(configProperties.disableLoginCheckCode)?string('true','false')}" == "true");
	po.disableRegister = ("${(configProperties.disableRegister)?string('true','false')}" == "true");
	
	po.vuePageModel(
	{
		disableLoginCheckCode: po.disableLoginCheckCode,
		disableRegister: po.disableRegister,
		isNotTopPage: po.isAjaxRequest
	});
	
	var formModel = $.unescapeHtmlForJson(<@writeJson var=formModel />);
	po.setupForm(formModel,
	{
		type: "POST",
		contentType: $.CONTENT_TYPE_FORM,
		tipSuccess: false,
		success: function(response)
		{
			po.handleLoginSuccess(response);
		}
	});
	
	po.handleLoginSuccess = function(response)
	{
		var url = "${contextPath}/";
		
		if(response && response.data && response.data.redirectUrl)
			url = response.data.redirectUrl;
		
		(window.top ? window.top : window).location.href = url;
	};
	
	if(!po.disableLoginCheckCode)
	{
		po.vueMounted(function()
		{
			po.element(".checkCodeImg").click(function()
			{
				$(this).attr("src", "${contextPath}/checkCode?_=" + $.uid("rc")+"&m=${LoginController.CHECK_CODE_MODULE_LOGIN}");
			})
			.click();
		});
	}
})
(${pid});
</script>
<#include "include/page_vue_mount.ftl">
</body>
</html>
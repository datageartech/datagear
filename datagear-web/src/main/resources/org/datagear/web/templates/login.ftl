<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#assign LoginController=statics['org.datagear.web.controller.LoginController']>
<#include "include/page_import.ftl">
<#include "include/html_doctype.ftl">
<html>
<head>
<#include "include/html_head.ftl">
<title>
	<#include "include/html_app_name_prefix.ftl">
	<@spring.message code='module.login' />
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
						        		name="name" required maxlength="20">
						        	</p-inputtext>
						        </div>
							</div>
							<div class="field grid">
								<label for="${pid}password" class="field-label col-12 mb-2 md:col-3 md:mb-0">
									<@spring.message code='password' />
								</label>
						        <div class="field-input col-12 md:col-9">
						        	<p-password id="${pid}password" v-model="fm.password" toggle-mask :feedback="false"
						        		input-class="w-full" class="input w-full"
						        		name="password" required maxlength="50">
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
						<div class="page-form-foot flex-grow-0 pt-3 text-center">
							<p-button type="submit" label="<@spring.message code='login' />"></p-button>
						</div>
						<div class="page-form-foot flex-grow-0 pt-3 text-right text-color-secondary">
							<p-checkbox id="${pid}remremberLogin" v-model="fm.rememberMe" :binary="true" name="remremberLogin"></p-checkbox>
							<label for="${pid}remremberLogin" class="ml-1"><@spring.message code='remremberLogin' /></label>
							
							<a href="${contextPath}/resetPassword" class="link ml-3"><@spring.message code='forgetPassword' /></a>
						</div>
						<div class="page-form-foot flex-grow-0 pt-3 text-right text-color-secondary" v-if="!pm.disableRegister">
							<a href="${contextPath}/register" class="link ml-3"><@spring.message code='module.register' /></a>
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
		success: function()
		{
			(window.top ? window.top : window).location.href="${contextPath}/";
		}
	});
	
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
	
	po.vueMount();
})
(${pid});
</script>
</body>
</html>
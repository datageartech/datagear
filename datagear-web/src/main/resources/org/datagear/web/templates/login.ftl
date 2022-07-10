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
<title><#include "include/html_app_name_prefix.ftl"><@spring.message code='module.login' /></title>
</head>
<body class="m-0 surface-ground">
<#include "include/page_obj.ftl">
<div id="${pid}" class="page page-form horizontal">
	<div class="flex flex-column h-screen m-0">
		<#include "include/page_main_header.ftl">
		<div class="flex-grow-1 p-0">
			<div class="grid grid-nogutter justify-content-center">
				<p-card class="col-10 md:col-5 p-card mt-6">
					<template #title><@spring.message code='module.login' /></template>
					<template #content>
					<form class="flex flex-column">
						<div class="page-form-content flex-grow-1 pr-2 py-1 overflow-y-auto">
							<div class="field grid">
								<label for="${pid}name" class="field-label col-12 mb-2 md:col-3 md:mb-0"><@spring.message code='username' /></label>
						        <div class="field-input col-12 md:col-9">
						        	<p-inputtext id="${pid}name" v-model="pm.name" type="text" class="input w-full"
						        		name="name" required maxlength="20">
						        	</p-inputtext>
						        </div>
							</div>
							<div class="field grid">
								<label for="${pid}password" class="field-label col-12 mb-2 md:col-3 md:mb-0"><@spring.message code='password' /></label>
						        <div class="field-input col-12 md:col-9">
						        	<p-password id="${pid}password" v-model="pm.password" toggle-mask :feedback="false"
						        		input-class="w-full" class="input w-full"
						        		name="password" required maxlength="50">
						        	</p-password>
						        </div>
							</div>
							<div class="field grid">
								<label for="${pid}checkCode" class="field-label col-12 mb-2 md:col-3 md:mb-0"><@spring.message code='checkCode' /></label>
						        <div class="field-input col-12 md:col-9">
						        	<p-inputtext id="${pid}checkCode" v-model="pm.checkCode" type="text" class="input w-6"
						        		name="checkCode" required maxlength="10">
						        	</p-inputtext>
						        	<img class="checkCodeImg ml-1 vertical-align-middle" style="height:1.5rem;" />
						        </div>
							</div>
						</div>
						<div class="page-form-foot flex-grow-0 pt-3 text-center">
							<p-button type="submit" label="<@spring.message code='login' />" />
						</div>
						<div class="page-form-foot flex-grow-0 pt-2 text-right text-sm text-color-secondary">
							<p-checkbox id="${pid}remremberLogin" v-model="pm.rememberMe" :binary="true" name="remremberLogin"></p-checkbox>
							<label for="${pid}remremberLogin" class="ml-1"><@spring.message code='remremberLogin' /></label>
							
							<a href="${contextPath}/resetPassword" class="link ml-3"><@spring.message code='forgetPassword' /></a>
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
	po.disableLoginCheckCode = ("${(disableLoginCheckCode!false)?string('true','false')}" == "true");
	
	po.setupForm(
	{
		name: "",
		password: "",
		checkCode: "",
		rememberMe: false
	},
	"/login/doLogin",
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
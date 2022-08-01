<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#assign RegisterController=statics['org.datagear.web.controller.RegisterController']>
<#include "include/page_import.ftl">
<#include "include/html_doctype.ftl">
<html>
<head>
<#include "include/html_head.ftl">
<title>
	<#include "include/html_app_name_prefix.ftl">
	<@spring.message code='module.register' />
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
					<template #content>
					<form class="flex flex-column">
						<div class="page-form-content flex-grow-1 px-2 py-1 overflow-y-auto">
							<div class="field grid">
								<label for="${pid}name" class="field-label col-12 mb-2 md:col-3 md:mb-0">
									<@spring.message code='username' />
								</label>
						        <div class="field-input col-12 md:col-9">
						        	<p-inputtext id="${pid}name" v-model="pm.user.name" type="text" class="input w-full"
						        		name="name" required maxlength="20">
						        	</p-inputtext>
						        </div>
							</div>
							<div class="field grid">
								<label for="${pid}password" class="field-label col-12 mb-2 md:col-3 md:mb-0">
									<@spring.message code='password' />
								</label>
						        <div class="field-input col-12 md:col-9">
						        	<p-password id="${pid}password" v-model="pm.user.password" class="input w-full"
						        		input-class="w-full" toggle-mask :feedback="false"
						        		name="password" required maxlength="50" autocomplete="new-password">
						        	</p-password>
						        </div>
							</div>
							<div class="field grid">
								<label for="${pid}confirmPassword" class="field-label col-12 mb-2 md:col-3 md:mb-0">
									<@spring.message code='confirmPassword' />
								</label>
						        <div class="field-input col-12 md:col-9">
						        	<p-password id="${pid}confirmPassword" v-model="pm.user.confirmPassword" class="input w-full"
						        		input-class="w-full" toggle-mask :feedback="false"
						        		name="confirmPassword" required maxlength="50" autocomplete="new-password">
						        	</p-password>
						        </div>
							</div>
							<div class="field grid">
								<label for="${pid}realName" class="field-label col-12 mb-2 md:col-3 md:mb-0">
									<@spring.message code='realName' />
								</label>
						        <div class="field-input col-12 md:col-9">
						        	<p-inputtext id="${pid}realName" v-model="pm.user.realName" type="text" class="input w-full"
						        		name="realName" maxlength="50">
						        	</p-inputtext>
						        </div>
							</div>
							<div class="field grid">
								<label for="${pid}checkCode" class="field-label col-12 mb-2 md:col-3 md:mb-0">
									<@spring.message code='checkCode' />
								</label>
						        <div class="field-input col-12 md:col-9">
						        	<p-inputtext id="${pid}checkCode" v-model="pm.checkCode" type="text" class="input w-6"
						        		name="checkCode" required maxlength="10">
						        	</p-inputtext>
						        	<img class="checkCodeImg ml-1 vertical-align-middle" style="height:1.5rem;" />
						        </div>
							</div>
						</div>
						<div class="page-form-foot flex-grow-0 pt-3 text-center">
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
	
	po.setupForm({user: {}},
	{
		tipSuccess: false,
		success: function()
		{
			(window.top ? window.top : window).location.href="${contextPath}/register/success";
		}
	},
	function()
	{
		var options =
		{
			rules:
			{
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
	
	po.vueMount();
})
(${pid});
</script>
</body>
</html>
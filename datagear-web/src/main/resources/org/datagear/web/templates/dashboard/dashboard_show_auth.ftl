<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#assign ShowAuthCheckResponse=statics['org.datagear.web.controller.DashboardController$ShowAuthCheckResponse']>
<#include "../include/page_import.ftl">
<#include "../include/html_doctype.ftl">
<html>
<head>
<#include "../include/html_head.ftl">
<#if authed>
<meta http-equiv="refresh" content="3;url=${redirectPath}">
</#if>
<title>
	<#include "../include/html_app_name_prefix.ftl">
	<@spring.message code='module.dashboardShowAuth' />
</title>
</head>
<body class="m-0 surface-ground">
<#include "../include/page_obj.ftl">
<div id="${pid}" class="page horizontal">
	<div class="flex flex-column h-screen m-0">
		<#include "../include/page_main_header.ftl">
		<div class="flex-grow-1 p-0">
			<div class="grid grid-nogutter justify-content-center">
				<p-card class="col-10 md:col-6 p-card mt-6">
					<template #title><@spring.message code='module.dashboardShowAuth' /></template>
					<template #content>
					<form id="${pid}form" class="flex flex-column">
						<div class="page-form-content flex-grow-1 px-2 py-1 overflow-y-auto">
							<div class="mb-4 text-center text-primary text-lg">
								<a :href="fm.redirectPath" class="link">
									{{fm.dashboardNameMask}}
								</a>
							</div>
							<#if !authed>
							<div class="field grid justify-content-center">
								<label for="${pid}password" class="field-label col-12 mb-2 justify-content-center">
									<@spring.message code='password' />
								</label>
						        <div class="field-input col-10">
						        	<p-password id="${pid}password" v-model="fm.password" toggle-mask :feedback="false"
						        		input-class="w-full" class="input w-full"
						        		name="password" autocomplete="off" autofocus>
						        	</p-password>
						        </div>
							</div>
							<#else>
							<div class="mb-4 text-center text-lg">
								<i class="pi pi-check-circle text-xl mr-2"></i>
								<@spring.message code='dashboard.showAuth.authed' />
							</div>
							</#if>
						</div>
						<#if !authed>
						<div class="page-form-foot flex-grow-0 pt-3 text-center">
							<p-button type="submit" label="<@spring.message code='confirm' />"></p-button>
						</div>
						</#if>
					</form>
					</template>
				</p-card>
			</div>
		</div>
	</div>
</div>
<#include "../include/page_form.ftl">
<script>
(function(po)
{
	po.submitUrl = "/dashboard/"+po.submitAction;
	po.redirectPath = "${redirectPath}";
	
	po.beforeSubmitForm = function(action)
	{
		var data = action.options.data;
		data = { id: data.id, password: data.password };
		action.options.data = data;
	};
	
	var formModel = $.unescapeHtmlForJson(<@writeJson var=formModel />);
	po.setupForm(formModel,
	{
		tipSuccess: false,
		success: function(response)
		{
			var responseData = (response.data || {});
			
			if(responseData.type == "${ShowAuthCheckResponse.TYPE_SUCCESS}")
			{
				window.location.href = po.redirectPath;
			}
			else if(responseData.type == "${ShowAuthCheckResponse.TYPE_FAIL}")
			{
				po.elementOfName("password").focus();
				
				if(responseData.authFailThreshold < 0)
					$.tipError("<@spring.message code='dashboard.showAuth.incorrectPassword' />");
				else
				{
					var msg = $.validator.format("<@spring.message code='dashboard.showAuth.incorrectPasswordWithRemain' />", responseData.authRemain);
					$.tipError(msg);
				}
			}
			else if(responseData.type == "${ShowAuthCheckResponse.TYPE_DENY}")
			{
				po.elementOfName("password").focus();
				$.tipError("<@spring.message code='dashboard.showAuth.authDenied' />");
			}
		}
	});
	
	po.vueMounted(function()
	{
		po.element(".loginLinkWrapper a").attr("href", "${contextPath}/login");
	});
	
	po.vueMount();
})
(${pid});
</script>
</body>
</html>
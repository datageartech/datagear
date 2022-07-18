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
<meta http-equiv="refresh" content="3;url=${contextPath}/login">
<title>
	<#include "include/html_app_name_prefix.ftl">
	<@spring.message code='module.registerSuccess' />
</title>
</head>
<body class="m-0 surface-ground">
<#include "include/page_obj.ftl">
<div id="${pid}" class="page horizontal">
	<div class="flex flex-column h-screen m-0">
		<#include "include/page_main_header.ftl">
		<div class="flex-grow-1 p-0">
			<div class="grid grid-nogutter justify-content-center">
				<p-card class="col-10 md:col-8 mt-6 p-inline-message p-inline-message-success">
					<template #title>
						<div class="text-center">
							<i class="pi pi-check-circle text-xl mr-2"></i>
							<span>
								<@spring.message code='registerSuccessEm' />
							</span>
						</div>
					</template>
					<template #content>
						<div class="loginLinkWrapper text-center">
							<@spring.message code='registerSuccessDetail' />
						</div>
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
	po.setupForm({user: {}}, "/register/doRegister",
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
		po.element(".loginLinkWrapper a").attr("href", "${contextPath}/login");
	});
	
	po.vueMount();
})
(${pid});
</script>
</body>
</html>
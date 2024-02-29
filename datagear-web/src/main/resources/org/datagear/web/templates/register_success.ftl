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
<#assign RegisterController=statics['org.datagear.web.controller.RegisterController']>
<#include "include/page_import.ftl">
<#include "include/html_doctype.ftl">
<html>
<head>
<#include "include/html_head.ftl">
<meta http-equiv="refresh" content="3;url=${contextPath}/login">
<title>
	<@spring.message code='module.registerSuccess' />
	<#include "include/html_app_name_suffix.ftl">
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
<script>
(function(po)
{
	po.vueMounted(function()
	{
		po.element(".loginLinkWrapper a").attr("href", "${contextPath}/login");
	});
})
(${pid});
</script>
<#include "include/page_vue_mount.ftl">
</body>
</html>
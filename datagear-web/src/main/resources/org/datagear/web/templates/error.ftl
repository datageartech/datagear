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
<#include "include/page_import.ftl">
<#assign isJsonResponse=(isJsonResponse!false)>
<#if isJsonResponse>
<@writeJson var=operationMessage escapeHtml=false />
<#else>
<#include "include/html_doctype.ftl">
<html>
<head>
<meta dg-page-name="error" />
<#include "include/html_head.ftl">
<title>
	<@spring.message code='module.error' />
	<#include "include/html_app_name_suffix.ftl">
</title>
</head>
<body class="m-0 surface-ground">
<#if isAjaxRequest>
	<div class="operation-message ${operationMessage.type}">
		<div class="message">
			${operationMessage.message}
		</div>
		<#if (operationMessage.detail)??>
		<div class="message-detail">
			<pre>${operationMessage.detail}</pre>
		</div>
		</#if>
	</div>
<#else>
	<#include "include/page_obj.ftl">
	<div id="${pid}" class="page horizontal">
		<div class="flex flex-column h-screen m-0">
			<#-- 这里不能引用page_main_header.ftl，参考CustomFreeMarkerView类内注释 -->
			<#include "include/page_main_header_simple.ftl">
			<div class="flex-grow-1 p-0">
				<div class="grid grid-nogutter justify-content-center">
					<p-card class="col-10 md:col-8 mt-6 p-inline-message p-inline-message-error">
						<template #content>
							<div class="operation-message ${operationMessage.type}">
								<div class="message flex flex-row align-items-center justify-content-center">
									<span class="pi pi-info-circle text-4xl"></span>
									<div class="text-2xl pl-2">
										${operationMessage.message}
									</div>
								</div>
								<#if (operationMessage.detail)??>
								<div class="message-detail">
									<pre>${operationMessage.detail}</pre>
								</div>
								</#if>
							</div>
						</template>
					</p-card>
				</div>
			</div>
		</div>
	</div>
	<#include "include/page_vue_mount.ftl">
</#if>
</body>
</html>
</#if>
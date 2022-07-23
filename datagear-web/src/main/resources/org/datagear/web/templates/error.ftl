<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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
	<#include "include/html_app_name_prefix.ftl">
	<@spring.message code='module.error' />
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
			<#include "include/page_main_header.ftl">
			<div class="flex-grow-1 p-0">
				<div class="grid grid-nogutter justify-content-center">
					<p-card class="col-10 md:col-8 mt-6 p-inline-message p-inline-message-error">
						<template #content>
							<div class="operation-message ${operationMessage.type}">
								<div class="message text-2xl">
									${operationMessage.message}
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
	<script>
	(function(po)
	{
		po.vueMount();
	})
	(${pid});
	</script>
</#if>
</body>
</html>
</#if>
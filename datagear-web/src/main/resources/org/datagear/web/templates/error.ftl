<#include "include/import_global.ftl">
<#assign isJsonResponse=(isJsonResponse!false)>
<#if isJsonResponse>
<@writeJson var=operationMessage />
<#else>
<#include "include/html_doctype.ftl">
<html>
<head>
<#include "include/html_head.ftl">
<title><#include "include/html_title_app_name.ftl"><@spring.message code='error.errorOccure' /></title>
</head>
<body>
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
<div>
	<div class="main-page-head">
		<#include "include/html_logo.ftl">
		<div class="toolbar">
			<a class="link" href="${contextPath}/"><@spring.message code='backToMainPage' /></a>
		</div>
	</div>
	<div class="page-error">
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
	</div>
</div>
</#if>
</body>
</html>
</#if>
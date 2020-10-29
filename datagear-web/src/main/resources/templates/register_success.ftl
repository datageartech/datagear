<#include "include/import_global.ftl">
<#include "include/html_doctype.ftl">
<html>
<head>
<#include "include/html_head.ftl">
<meta http-equiv="refresh" content="3;url=${contextPath}/login">
<title><#include "include/html_title_app_name.ftl"><@spring.message code='register.registerSuccess' /></title>
</head>
<body>
<div id="${pageId}">
	<div class="main-page-head">
		<#include "include/html_logo.ftl">
		<div class="toolbar">
			<a class="link" href="${contextPath}/"><@spring.message code='backToMainPage' /></a>
		</div>
	</div>
	<div class="page-register-success">
		<div class="register-success-content">
			<#assign messageArgs=['${contextPath}/login'] />
			<@spring.messageArgs code='register.registerSuccessContent' args=messageArgs />
		</div>
	</div>
</div>
</body>
</html>
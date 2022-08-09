<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#assign Global=statics['org.datagear.util.Global']>
<#include "include/page_import.ftl">
<#include "include/html_doctype.ftl">
<html>
<head>
<#include "include/html_head.ftl">
<title>
	<#include "include/html_app_name_prefix.ftl">
	<@spring.message code='module.about' />
</title>
</head>
<body class="p-card no-border">
<#include "include/page_obj.ftl">
<div id="${pid}" class="page page-form horizontal">
	<form id="${pid}form" class="flex flex-column">
		<div class="page-form-content flex-grow-1 px-2 py-1 overflow-y-auto">
			
			<div class="field grid">
				<label for="${pid}name" class="field-label col-12 mb-2 md:col-3 md:mb-0">
					<@spring.message code='name' />
				</label>
		        <div class="field-input col-12 md:col-9">
		        	<div id="${pid}name">
		        		<@spring.message code='app.fullName' />
		        	</div>
		        </div>
			</div>
			<div class="field grid">
				<label for="${pid}version" class="field-label col-12 mb-2 md:col-3 md:mb-0">
					<@spring.message code='version' />
				</label>
		        <div class="field-input col-12 md:col-9">
		        	<div id="${pid}version">
		        		${Global.VERSION}
		        	</div>
		        </div>
			</div>
			<div class="field grid">
				<label for="${pid}officalSite" class="field-label col-12 mb-2 md:col-3 md:mb-0">
					<@spring.message code='officalSite' />
				</label>
		        <div class="field-input col-12 md:col-9">
		        	<div id="${pid}officalSite" class="text-primary">
		        		<a href="${Global.WEB_SITE}" target="_blank" class="link">${Global.WEB_SITE}</a>
		        	</div>
		        </div>
			</div>
			<div class="field grid">
				<label for="${pid}license" class="field-label col-12 mb-2 md:col-3 md:mb-0">
					<@spring.message code='license' />
				</label>
		        <div class="field-input col-12 md:col-9">
		        	<div id="${pid}license" class="text-primary">
		        		<a href="http://www.gnu.org/licenses/lgpl-3.0.html" target="_blank" class="link">
							<@spring.message code='app.license' />
						</a>
		        	</div>
		        </div>
			</div>
			
		</div>
	</form>
</div>
<#include "include/page_form.ftl">
<script>
(function(po)
{
	po.vueMount();
})
(${pid});
</script>
</body>
</html>
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
	<@spring.message code='module.changelog' />
</title>
</head>
<body class="p-card no-border">
<#include "include/page_obj.ftl">
<div id="${pid}" class="page page-form horizontal">
	<form id="${pid}form" class="flex flex-column">
		<div class="page-form-content no-max-height flex-grow-1 pr-2 py-1 overflow-y-auto">
			<#list versionChangelogs as versionChangelog>
				<div class="field grid mb-0">
					<label for="${pid}version" class="field-label col-12 mb-2 md:col-3 md:mb-0 justify-content-center">
						<@spring.message code='version' />
					</label>
			        <div class="field-input col-12 md:col-9">
			        	<div id="${pid}version" class="text-xl font-bold">
			        		${versionChangelog.version}
			        	</div>
			        </div>
				</div>
				<div class="field grid mb-0">
					<label for="${pid}versionContent" class="field-label col-12 mb-2 md:col-3 md:mb-0">
						&nbsp;
					</label>
			        <div class="field-input col-12 md:col-9">
			        	<div id="${pid}versionContent">
			        		<ul class="pl-4">
								<#list versionChangelog.contents as item>
									<li class="py-1">${item}</li>
								</#list>
							</ul>
			        	</div>
			        </div>
				</div>
			</#list>
		</div>
		<div class="page-form-foot flex-grow-0 pt-3 text-center">
			<#if !(allListed??) || allListed == false>
			<div class="text-primary">
				<a href="${contextPath}/changelogs" target="_blank" class="link">
					<@spring.message code='viewAll' />
				</a>
			</div>
			</#if>
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
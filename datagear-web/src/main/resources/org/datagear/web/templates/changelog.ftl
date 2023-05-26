<#--
 *
 * Copyright 2018-2023 datagear.tech
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
<#assign Global=statics['org.datagear.util.Global']>
<#include "include/page_import.ftl">
<#include "include/html_doctype.ftl">
<html>
<head>
<#include "include/html_head.ftl">
<title>
	<@spring.message code='module.changelog' />
	<#include "include/html_app_name_suffix.ftl">
</title>
</head>
<body class="p-card no-border">
<#include "include/page_obj.ftl">
<div id="${pid}" class="page page-form horizontal">
	<form id="${pid}form" class="flex flex-column">
		<div class="page-form-content no-max-height flex-grow-1 pr-2 py-1 overflow-y-auto">
			<#list versionChangelogs as versionChangelog>
				<div class="field grid mb-0">
					<label for="${pid}version" class="field-label col-12 mb-2 md:col-2 md:mb-0 justify-content-center">
						<@spring.message code='version' />
					</label>
			        <div class="field-input col-12 md:col-10">
			        	<div id="${pid}version" class="text-xl font-bold">
			        		${versionChangelog.version}
			        	</div>
			        </div>
				</div>
				<div class="field grid mb-0">
					<label for="${pid}versionContent" class="field-label col-12 mb-2 md:col-2 md:mb-0">
						&nbsp;
					</label>
			        <div class="field-input col-12 md:col-10">
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
		<div class="page-form-foot flex-grow-0 flex justify-content-center gap-2 pt-2">
			<#if !(allListed??) || allListed == false>
			<div class="text-primary">
				<a href="${contextPath}/changelogs" target="_blank" class="link text-primary">
					<@spring.message code='viewAll' />
				</a>
			</div>
			</#if>
		</div>
	</form>
</div>
<#include "include/page_form.ftl">
<#include "include/page_vue_mount.ftl">
</body>
</html>
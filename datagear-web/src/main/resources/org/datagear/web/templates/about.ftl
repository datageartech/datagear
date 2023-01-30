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
	<@spring.message code='module.about' />
	<#include "include/html_app_name_suffix.ftl">
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
			<div class="grid mt-5">
		        <div class="field-input col-12 text-sm">
		        	<div class="text-color-secondary text-center pb-1">
		        		Copyright 2018-2023 datagear.tech
		        	</div>
		        	<div class="text-color-secondary text-center pt-1">
		        		Licensed under the LGPLv3 license, see
		        	</div>
		        	<div class="text-color-secondary text-center pt-1">
		        		<a href="https://www.gnu.org/licenses/lgpl-3.0.html" target="_blank" class="link text-color-secondary">
		        			https://www.gnu.org/licenses/lgpl-3.0.html
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
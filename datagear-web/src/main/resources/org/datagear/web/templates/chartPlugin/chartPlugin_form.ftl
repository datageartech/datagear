<#--
 *
 * Copyright 2018-present datagear.tech
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
<#include "../include/page_import.ftl">
<#include "../include/html_doctype.ftl">
<html>
<head>
<#include "../include/html_head.ftl">
<title>
	<@spring.message code='module.chartPlugin' />
	<#include "../include/html_request_action_suffix.ftl">
	<#include "../include/html_app_name_suffix.ftl">
</title>
</head>
<body class="p-card no-border">
<#include "../include/page_obj.ftl">
<div id="${pid}" class="page page-form horizontal">
	<form id="${pid}form" class="flex flex-column" :class="{readonly: pm.isReadonlyAction}">
		<div class="page-form-content flex-grow-1 px-2 py-1 overflow-y-auto">
			<div class="field grid">
				<label for="${pid}name" class="field-label col-12 mb-2 md:col-3 md:mb-0">
					<@spring.message code='name' />
				</label>
				<div class="field-input col-12 md:col-9">
					<div id="${pid}name" class="input p-component p-inputtext border-round-left flex align-items-center">
						<div class="flex-grow-0" v-html="formatChartPlugin(fm)"></div>
					</div>
				</div>
			</div>
			<div class="field grid">
				<label for="${pid}version" class="field-label col-12 mb-2 md:col-3 md:mb-0">
					<@spring.message code='version' />
				</label>
		        <div class="field-input col-12 md:col-9">
		        	<p-inputtext id="${pid}version" v-model="fm.version" type="text" class="input w-full"
		        		name="version" required maxlength="100">
		        	</p-inputtext>
		        </div>
			</div>
			<div class="field grid">
				<label for="${pid}desc" class="field-label col-12 mb-2 md:col-3 md:mb-0">
					<@spring.message code='desc' />
				</label>
		        <div class="field-input col-12 md:col-9">
		        	<p-textarea id="${pid}desc" v-model="fm.descLabel.value" rows="10" class="input w-full"
		        		name="descLabel.value" maxlength="1000">
		        	</p-textarea>
		        </div>
			</div>
			<div class="field grid">
				<label for="${pid}platformVersion" class="field-label col-12 mb-2 md:col-3 md:mb-0"
					title="<@spring.message code='chartPlugin.platformVersion.desc' />">
					<@spring.message code='platformVersionRequirement' />
				</label>
		        <div class="field-input col-12 md:col-9">
		        	<p-inputtext id="${pid}platformVersion" v-model="fm.platformVersion" type="text" class="input w-full"
		        		name="platformVersion" maxlength="50">
		        	</p-inputtext>
		        </div>
			</div>
			<div class="field grid">
				<label for="${pid}author" class="field-label col-12 mb-2 md:col-3 md:mb-0">
					<@spring.message code='author' />
				</label>
		        <div class="field-input col-12 md:col-9">
		        	<p-inputtext id="${pid}author" v-model="fm.author" type="text" class="input w-full"
		        		name="author" maxlength="100">
		        	</p-inputtext>
		        </div>
			</div>
			<div class="field grid">
				<label for="${pid}contact" class="field-label col-12 mb-2 md:col-3 md:mb-0">
					<@spring.message code='contact' />
				</label>
		        <div class="field-input col-12 md:col-9">
		        	<p-inputtext id="${pid}contact" v-model="fm.contact" type="text" class="input w-full"
		        		name="contact" maxlength="100">
		        	</p-inputtext>
		        </div>
			</div>
			<div class="field grid">
				<label for="${pid}issueDate" class="field-label col-12 mb-2 md:col-3 md:mb-0">
					<@spring.message code='issueDate' />
				</label>
		        <div class="field-input col-12 md:col-9">
		        	<p-inputtext id="${pid}issueDate" v-model="fm.issueDate" type="text" class="input w-full"
		        		name="issueDate" maxlength="100">
		        	</p-inputtext>
		        </div>
			</div>
		</div>
		<div class="page-form-foot flex-grow-0 flex justify-content-center gap-2 pt-2">
			<p-button type="submit" label="<@spring.message code='save' />"></p-button>
		</div>
	</form>
</div>
<#include "../include/page_form.ftl">
<script>
(function(po)
{
	po.submitUrl = "/chartPlugin/"+po.submitAction;

	var formModel = $.unescapeHtmlForJson(<@writeJson var=formModel />);
	formModel.nameLabel = (formModel.nameLabel ? formModel.nameLabel : {});
	formModel.descLabel = (formModel.descLabel ? formModel.descLabel : {});
	
	po.setupForm(formModel);
	
	po.vueMethod(
	{
		formatChartPlugin: function(chartPlugin)
		{
			return $.toChartPluginHtml(chartPlugin, po.contextPath);
		}
	});
})
(${pid});
</script>
<#include "../include/page_vue_mount.ftl">
</body>
</html>
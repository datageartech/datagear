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
	- <@spring.message code='useManual' />
	<#include "../include/html_app_name_suffix.ftl">
</title>
</head>
<body class="p-card no-border h-screen m-0 p-1">
<#include "../include/page_obj.ftl">
<div id="${pid}" class="page page-form page-chart-plugin-manual h-full">
	<form id="${pid}form" class="flex flex-column h-full" :class="{readonly: pm.isReadonlyAction}">
		<div class="page-form-content no-max-height flex-grow-1 px-2 py-1 overflow-y-auto">
			<div class="flex flex-column align-items-center justify-content-center gap-2">
				<div class="flex flex-row align-items-center justify-content-center gap-2">
					<div class="text-lg" v-html="formatChartPlugin(fm)"></div>
				</div>
				<div class="flex flex-row align-items-center justify-content-center gap-1 text-color-secondary text-sm">
					<span class="px-2 py-1 surface-100 border-round-lg" title="<@spring.message code='id' />">
						{{fm.id}}
					</span>
					<span class="px-2 py-1 surface-100 border-round-lg" title="<@spring.message code='author' />"
						v-if="fm.author">
						{{fm.author}}
					</span>
					<span class="px-2 py-1 surface-100 border-round-lg" title="<@spring.message code='contact' />"
						v-if="fm.contact">
						{{fm.contact}}
					</span>
					<span class="px-2 py-1 surface-100 border-round-lg" title="<@spring.message code='issueDate' />"
						v-if="fm.issueDate">
						{{fm.issueDate}}
					</span>
				</div>
			</div>
			<div v-html="pm.manualHtml" class="manual-content"></div>
		</div>
	</form>
</div>
<#include "../include/page_form.ftl">
<script>
(function(po)
{
	po.submitUrl = "/chartPlugin/"+po.submitAction;
	
	po.loadManual = function(id)
	{
		var pm = po.vuePageModel();
		
		po.ajax("/chartPlugin/manualContent/" + encodeURIComponent(id),
		{
			tipError: false,
			success: function(data)
			{
				//不允许任何HTML标签，避免安全风险
				data = $.escapeHtmlTag(data);
				pm.manualHtml = marked.parse(data);
			},
			error: function()
			{
				pm.manualHtml = "<@spring.message code='none' />";
			}
		});
	};
	
	var formModel = $.unescapeHtmlForJson(<@writeJson var=formModel />);
	formModel.nameLabel = (formModel.nameLabel ? formModel.nameLabel : {});
	
	po.setupForm(formModel);
	
	po.vuePageModel(
	{
		manualHtml: marked.parse("# Hello")
	});
	
	po.vueMethod(
	{
		formatChartPlugin: function(chartPlugin)
		{
			return $.toChartPluginHtml(chartPlugin, po.contextPath, {
				justifyContent: "start", showVersion:true, showApiVersion:true,
				showPlatformVersion: true,
				apiVersionDesc: "<@spring.message code='chartPlugin.apiVersion.desc' />",
				platformVersionDesc: "<@spring.message code='chartPlugin.platformVersion.desc' />"
			});
		}
	});
	
	po.vueMounted(function()
	{
		var fm = po.vueFormModel();
		po.loadManual(fm.id);
	});
})
(${pid});
</script>
<#include "../include/page_vue_mount.ftl">
</body>
</html>
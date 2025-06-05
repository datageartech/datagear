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
<#--
看板代码补全列表
-->
<script type="text/javascript">
(function(po)
{
	po.codeEditorCompletionsTagAttr =
	[
		{name: "dg-chart-attr-values", value: "dg-chart-attr-values=",
			displayComment: "<@spring.message code='dashboard.templateEditor.autoComplete.dg-chart-attr-values' />", categories: ["div"]},
		{name: "dg-chart-auto-resize", value: "dg-chart-auto-resize=\"true\"",
			displayComment: "<@spring.message code='dashboard.templateEditor.autoComplete.dg-chart-auto-resize' />", categories: ["body", "div"]},
		{name: "dg-chart-disable-setting", value: "dg-chart-disable-setting=",
			displayComment: "<@spring.message code='dashboard.templateEditor.autoComplete.dg-chart-disable-setting' />", categories: ["body", "div"]},
		{name: "dg-chart-link", value: "dg-chart-link=",
			displayComment: "<@spring.message code='dashboard.templateEditor.autoComplete.dg-chart-link' />", categories: ["div"]},
		{name: "dg-chart-listener", value: "dg-chart-listener=",
			displayComment: "<@spring.message code='dashboard.templateEditor.autoComplete.dg-chart-listener' />", categories: ["body", "div"]},
		{name: "dg-chart-manual-render", value: "dg-chart-manual-render=\"true\"",
			displayComment: "<@spring.message code='dashboard.templateEditor.autoComplete.dg-chart-manual-render' />", categories: ["div"]},
		{name: "dg-chart-map-urls", value: "dg-chart-map-urls=",
			displayComment: "<@spring.message code='dashboard.templateEditor.autoComplete.dg-chart-map-urls' />", categories: ["body"]},
		{name: "dg-chart-on-",
			displayComment: "<@spring.message code='dashboard.templateEditor.autoComplete.dg-chart-on-' />", categories: ["div"]},
		{name: "dg-chart-options", value: "dg-chart-options=",
			displayComment: "<@spring.message code='dashboard.templateEditor.autoComplete.dg-chart-options' />", categories: ["body","div"]},
		{name: "dg-chart-renderer", value: "dg-chart-renderer=",
			displayComment: "<@spring.message code='dashboard.templateEditor.autoComplete.dg-chart-renderer' />", categories: ["div"]},
		{name: "dg-chart-theme", value: "dg-chart-theme=",
			displayComment: "<@spring.message code='dashboard.templateEditor.autoComplete.dg-chart-theme' />", categories: ["body", "div"]},
		{name: "dg-chart-update-group", value: "dg-chart-update-group=",
			displayComment: "<@spring.message code='dashboard.templateEditor.autoComplete.dg-chart-update-group' />", categories: ["body", "div"]},
		{name: "dg-chart-widget", value: "dg-chart-widget=",
			displayComment: "<@spring.message code='dashboard.templateEditor.autoComplete.dg-chart-widget' />", categories: ["div"]},
		{name: "dg-dashboard-form", value: "dg-dashboard-form=",
			displayComment: "<@spring.message code='dashboard.templateEditor.autoComplete.dg-dashboard-form' />", categories: ["form"]},
		{name: "dg-dashboard-listener", value: "dg-dashboard-listener=",
			displayComment: "<@spring.message code='dashboard.templateEditor.autoComplete.dg-dashboard-listener' />", categories: ["body"]},
		{name: "dg-echarts-theme", value: "dg-echarts-theme=",
			displayComment: "<@spring.message code='dashboard.templateEditor.autoComplete.dg-echarts-theme' />", categories: ["body", "div"]},
		{name: "dg-dashboard-unimport", value: "dg-dashboard-unimport=",
				displayComment: "<@spring.message code='dashboard.templateEditor.autoComplete.dg-dashboard-unimport' />", categories: ["html"]},
		{name: "dg-dashboard-var", value: "dg-dashboard-var=",
			displayComment: "<@spring.message code='dashboard.templateEditor.autoComplete.dg-dashboard-var' />", categories: ["html"]},
		{name: "dg-loadable-chart-widgets", value: "dg-loadable-chart-widgets=",
				displayComment: "<@spring.message code='dashboard.templateEditor.autoComplete.dg-loadable-chart-widgets' />", categories: ["html"]},
		{name: "dg-dashboard-code", value: "dg-dashboard-code=",
			displayComment: "<@spring.message code='dashboard.templateEditor.autoComplete.dg-dashboard-code' />", categories: ["html", "script"]}
	];
	
	po.codeEditorCompletionsJsFunction = window.dashboardApiCompletions;
})
(${pid});
</script>

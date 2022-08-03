<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#include "../include/page_import.ftl">
<#include "../include/html_doctype.ftl">
<html>
<head>
<#include "../include/html_head.ftl">
<title>
	<#include "../include/html_app_name_prefix.ftl">
	<@spring.message code='module.chart' />
	<#include "../include/html_request_action_suffix.ftl">
</title>
</head>
<body class="p-card no-border">
<#include "../include/page_obj.ftl">
<div id="${pid}" class="page page-form horizontal page-form-chart">
	<form class="flex flex-column" :class="{readonly: pm.isReadonlyAction}">
		<div class="page-form-content flex-grow-1 px-2 py-1 overflow-y-auto">
			<div class="field grid">
				<label for="${pid}name" class="field-label col-12 mb-2 md:col-3 md:mb-0">
					<@spring.message code='name' />
				</label>
				<div class="field-input col-12 md:col-9">
					<p-inputtext id="${pid}name" v-model="fm.name" type="text" class="input w-full"
						name="name" required maxlength="100" autofocus>
					</p-inputtext>
				</div>
			</div>
			<div class="field grid">
				<label for="${pid}ownerProject" class="field-label col-12 mb-2 md:col-3 md:mb-0">
					<@spring.message code='ownerProject' />
				</label>
				<div class="field-input col-12 md:col-9">
					<div class="p-inputgroup">
						<div class="p-input-icon-right flex-grow-1">
							<i class="pi pi-times cursor-pointer opacity-60" @click="onDeleteAnalysisProject" v-if="!pm.isReadonlyAction">
							</i>
							<p-inputtext id="${pid}ownerProject" v-model="fm.analysisProject.name" type="text" class="input w-full h-full border-noround-right"
								readonly="readonly" name="analysisProject.name" maxlength="200">
							</p-inputtext>
						</div>
						<p-button type="button" label="<@spring.message code='select' />"
							@click="onSelectAnalysisProject" class="p-button-secondary"
							v-if="!pm.isReadonlyAction">
						</p-button>
					</div>
				</div>
			</div>
			<div class="field grid">
				<label for="${pid}htmlChartPlugin" class="field-label col-12 mb-2 md:col-3 md:mb-0">
					<@spring.message code='chartType' />
				</label>
				<div class="field-input col-12 md:col-9">
					<div class="p-inputgroup">
						<div id="${pid}htmlChartPlugin" class="input p-component p-inputtext border-round-left"
							v-html="formatChartPlugin(fm.htmlChartPlugin)">
						</div>
						<p-button type="button" label="<@spring.message code='select' />"
							@click="onSelectChartPlugin" class="p-button-secondary"
							v-if="!pm.isReadonlyAction">
						</p-button>
					</div>
		        	<div class="validate-msg">
		        		<input name="htmlChartPlugin" required type="text" class="validate-proxy" />
		        	</div>
				</div>
			</div>
			<div class="field grid">
				<label for="${pid}chartDataSetVOs" class="field-label col-12 mb-2 md:col-3 md:mb-0">
					<@spring.message code='module.dataSet' />
				</label>
				<div class="field-input col-12 md:col-9">
					<div id="${pid}chartDataSetVOs" class="chart-datasets input p-component p-inputtext w-full overflow-auto p-2">
						<p-panel v-for="cds in fm.chartDataSetVOs" :header="cds.dataSet.name" :toggleable="true" class="p-card mb-2 no-panel-border">
							<template #icons>
								<p-button icon="pi pi-arrow-up" class="p-button-sm p-button-secondary p-button-rounded p-button-text mr-2"></p-button>
								<p-button icon="pi pi-arrow-down" class="p-button-sm p-button-secondary p-button-rounded p-button-text mr-2"></p-button>
								<p-button icon="pi pi-times" class="p-button-sm p-button-secondary p-button-rounded p-button-text p-button-danger mr-5"></p-button>
							</template>
							<div>
								<div v-for="dp in cds.dataSet.properties" :key="dp.name" class="p-card p-2 mb-3">
									<div>
										<i class="pi pi-angle-right"></i>
										{{dp.name}}
									</div>
									<div class="px-4">
										<div class="field grid">
											<label class="field-label col-12 mb-2 md:col-3 md:mb-0">
												标记
											</label>
											<div class="field-input col-12 md:col-9">
												<div class="input p-component p-inputtext w-full overflow-auto" style="height:3rem;">
												</div>
											</div>
										</div>
										<div class="field grid">
											<label class="field-label col-12 mb-2 md:col-3 md:mb-0">
												别名
											</label>
											<div class="field-input col-12 md:col-9">
												<p-inputtext type="text" class="input w-full" maxlength="50">
												</p-inputtext>
											</div>
										</div>
										<div class="field grid">
											<label class="field-label col-12 mb-2 md:col-3 md:mb-0">
												排序
											</label>
											<div class="field-input col-12 md:col-9">
												<p-inputtext type="text" class="input w-full" maxlength="50">
												</p-inputtext>
											</div>
										</div>
									</div>
								</div>
							</div>
						</p-panel>
					</div>
					<div class="mt-1">
						<p-button type="button" label="<@spring.message code='add' />"
							@click="onAddDataSet" class="p-button-secondary"
							v-if="!pm.isReadonlyAction">
						</p-button>
					</div>
				</div>
			</div>
		</div>
		<div class="page-form-foot flex-grow-0 pt-3 text-center">
			<p-button type="submit" label="<@spring.message code='save' />"></p-button>
		</div>
	</form>
</div>
<#include "../include/page_form.ftl">
<script>
(function(po)
{
	po.submitUrl = "/chart/"+po.submitAction;
	
	var formModel = <@writeJson var=formModel />;
	formModel = $.unescapeHtmlForJson(formModel);
	formModel.analysisProject = (formModel.analysisProject == null ? {} : formModel.analysisProject);
	formModel.chartDataSetVOs = (formModel.chartDataSetVOs == null ? [] : formModel.chartDataSetVOs);
	formModel.plugin = undefined;
	formModel.chartDataSets = undefined;
	
	po.setupForm(formModel, {}, {});
	
	po.vueMethod(
	{
		formatChartPlugin: function(chartPlugin)
		{
			return $.toChartPluginHtml(chartPlugin, po.contextPath, {justifyContent: "start"});
		},
		onDeleteAnalysisProject: function()
		{
			var fm = po.vueFormModel();
			fm.analysisProject = {};
		},
		
		onSelectAnalysisProject: function()
		{
			po.handleOpenSelectAction("/analysisProject/select", function(analysisProject)
			{
				var fm = po.vueFormModel();
				fm.analysisProject = analysisProject;
			});
		},
		
		onSelectChartPlugin: function()
		{
			po.handleOpenSelectAction("/chartPlugin/select", function(plugin)
			{
				var fm = po.vueFormModel();
				fm.htmlChartPlugin = plugin;
			});
		},
		
		onAddDataSet: function()
		{
			po.handleOpenSelectAction("/dataSet/select?multiple", function(dataSets)
			{
				var data = $.propertyValueParam(dataSets, "id");
				
				po.getJson("/dataSet/getProfileDataSetByIds", data, function(dataSets)
				{
					var fm = po.vueFormModel();
					
					$.each(dataSets, function(idx, dataSet)
					{
						fm.chartDataSetVOs.push({dataSet: dataSet});
					});
				});
			});
		}
	});
	
	po.vueMount();
})
(${pid});
</script>
</body>
</html>
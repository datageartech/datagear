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
						<p-panel v-for="(cds, cdsIdx) in fm.chartDataSetVOs" :header="cds.dataSet.name" :toggleable="true" class="p-card mb-2 no-panel-border">
							<template #icons>
								<p-button icon="pi pi-arrow-up" class="p-button-sm p-button-secondary p-button-rounded p-button-text mr-2" v-if="!pm.isReadonlyAction"></p-button>
								<p-button icon="pi pi-arrow-down" class="p-button-sm p-button-secondary p-button-rounded p-button-text mr-2" v-if="!pm.isReadonlyAction"></p-button>
								<p-button icon="pi pi-times" class="p-button-sm p-button-secondary p-button-rounded p-button-text p-button-danger mr-5" v-if="!pm.isReadonlyAction"></p-button>
							</template>
							<div>
								<p-fieldset v-for="(dp, dpIdx) in cds.dataSet.properties" :key="dp.name" :legend="dp.name" class="fieldset-sm mb-3">
									<div class="field grid mb-2">
										<label :for="'${pid}cdspidSign_'+cdsIdx+'_'+dpIdx" class="field-label col-12 mb-2 md:col-3 md:mb-0"
											title="<@spring.message code='chart.cds.dataSign.desc' />">
											<@spring.message code='sign' />
										</label>
										<div class="field-input col-12 md:col-9">
											<div class="p-inputgroup">
												<div :id="'${pid}cdspidSign_'+cdsIdx+'_'+dpIdx"
													class="input p-component p-inputtext border-round-left overflow-auto" style="height:4rem;">
													<p-chip v-for="sign in dp.cdsInfo.signs" :key="sign.name" :label="formatDataSignLabel(sign)" class="mb-2"
														:removable="!pm.isReadonlyAction" @remove="onRemoveDataSign(dp, sign.name)">
													</p-chip>
												</div>
												<p-button type="button" icon="pi pi-plus"
													aria:haspopup="true" aria-controls="${pid}dataSignsPanel"
													@click="onShowDataSignPanel($event, dp)" class="p-button-secondary" v-if="!pm.isReadonlyAction">
												</p-button>
											</div>
										</div>
									</div>
									<div class="field grid mb-2">
										<label :for="'${pid}cdspidAlias_'+cdsIdx+'_'+dpIdx" class="field-label col-12 mb-2 md:col-3 md:mb-0"
											title="<@spring.message code='chart.cds.propertyAlias.desc' />">
											<@spring.message code='alias' />
										</label>
										<div class="field-input col-12 md:col-9">
											<p-inputtext :id="'${pid}cdspidAlias_'+cdsIdx+'_'+dpIdx" v-model="dp.cdsInfo.alias" type="text" class="input w-full" maxlength="50" :placeholder="dp.name">
											</p-inputtext>
										</div>
									</div>
									<div class="field grid mb-2">
										<label :for="'${pid}cdspidSort_'+cdsIdx+'_'+dpIdx" class="field-label col-12 mb-2 md:col-3 md:mb-0"
											title="<@spring.message code='chart.cds.propertyOrder.desc' />">
											<@spring.message code='sort' />
										</label>
										<div class="field-input col-12 md:col-9">
											<p-inputtext :id="'${pid}cdspidSort_'+cdsIdx+'_'+dpIdx" v-model="dp.cdsInfo.order" type="text" class="input w-full" maxlength="50" :placeholder="dpIdx">
											</p-inputtext>
										</div>
									</div>
								</p-fieldset>
							</div>
							<p-divider type="dashed"></p-divider>
							<div class="px-2">
								<div class="field grid mb-2">
									<label :for="'${pid}cdsAlias_'+cdsIdx" class="field-label col-12 mb-2 md:col-3 md:mb-0"
										title="<@spring.message code='chart.cds.alias.desc' />">
										<@spring.message code='alias' />
									</label>
									<div class="field-input col-12 md:col-9">
										<p-inputtext :id="'${pid}cdsAlias_'+cdsIdx" v-model="cds.alias" type="text" class="input w-full" maxlength="50" :placeholder="cds.dataSet.name">
										</p-inputtext>
									</div>
								</div>
								<div class="field grid">
									<label for="'${pid}cdsAtchm_'+cdsIdx" class="field-label col-12 mb-2 md:col-3 md:mb-0"
										title="<@spring.message code='chart.cds.attachment.desc' />">
										<@spring.message code='attachment' />
									</label>
									<div class="field-input col-12 md:col-9">
										<p-selectbutton id="'${pid}cdsAtchm_'+cdsIdx" v-model="cds.attachment" :options="pm.booleanOptions"
											option-label="name" option-value="value" class="input w-full">
										</p-selectbutton>
									</div>
								</div>
								<div class="field grid" v-if="cds.dataSet.params.length>0">
									<label for="'${pid}cdsAtchm_'+cdsIdx" class="field-label col-12 mb-2 md:col-3 md:mb-0"
										title="<@spring.message code='chart.cds.paramValue.desc' />">
										<@spring.message code='parameter' />
									</label>
									<div class="field-input col-12 md:col-9">
										<p-button type="button" label="<@spring.message code='edit' />"
											@click="" class="p-button-secondary">
										</p-button>
									</div>
								</div>
							</div>
						</p-panel>
					</div>
					<div class="mt-1">
						<p-button type="button" label="<@spring.message code='select' />"
							@click="onAddDataSet" class="p-button-secondary"
							v-if="!pm.isReadonlyAction">
						</p-button>
					</div>
				</div>
			</div>
			<div class="field grid">
				<label for="${pid}updateInterval" class="field-label col-12 mb-2 md:col-3 md:mb-0"
					title="<@spring.message code='chart.updateInterval.desc' />">
					<@spring.message code='updateInterval' />
				</label>
				<div class="field-input col-12 md:col-9">
					<div class="flex align-content-center">
						<div class="mr-2">
							<p-selectbutton v-model="pm.updateIntervalType" :options="pm.updateIntervalTypeOptions"
								option-label="name" option-value="value" @change="onUpdateIntervalTypeChange">
							</p-selectbutton>
						</div>
						<div class="mr-2" v-if="pm.updateIntervalType == 'interval'">
							<div class="p-inputgroup">
								<p-inputtext id="${pid}updateInterval" v-model="fm.updateInterval" type="text" class="input"
									name="updateInterval" required maxlength="10">
								</p-inputtext>
								<span class="p-inputgroup-addon"><@spring.message code='millisecond' /></span>
							</div>
						</div>
						<div class="flex align-items-center" v-if="pm.updateIntervalType == 'interval'">
							<small class="text-color-secondary"><@spring.message code='chart.updateIntervalValue.desc' /></small>
						</div>
					</div>
				</div>
			</div>
		</div>
		<div class="page-form-foot flex-grow-0 pt-3 text-center">
			<p-button type="submit" label="<@spring.message code='save' />"></p-button>
		</div>
	</form>
	<p-overlaypanel ref="${pid}dataSignsPanelEle" append-to="body"
		:show-close-icon="true" id="${pid}dataSignsPanel">
		<div class="mb-2">
			<label class="text-lg font-bold">
				<@spring.message code='dataSign' />
			</label>
		</div>
		<div class="panel-content-size-xs overflow-auto p-3">
			<div v-for="ds in pm.chartPluginDataSigns" class="mb-2">
				<div class="p-inputgroup">
					<p-button type="button" :label="formatDataSignLabel(ds)" icon="pi pi-plus"
						@click="onAddDataSign($event, ds)">
					</p-button>
					<p-button type="button" icon="pi pi-angle-right"
						aria:haspopup="true" aria-controls="${pid}dataSignDetailPanel"
						@click="onShowDataSignDetail($event, ds)">
					</p-button>
				</div>
			</div>
		</div>
	</p-overlaypanel>
	<p-overlaypanel ref="${pid}dataSignDetailPanelEle" append-to="body" id="${pid}dataSignDetailPanel">
		<div class="mb-2">
			<label class="text-lg font-bold">
				<@spring.message code='desc' />
			</label>
		</div>
		<div class="panel-content-size-xxs flex flex-column p-2">
			<div class="flex-grow-0 font-bold">
				{{pm.dataSignDetail.label}}
			</div>
			<div class="flex-grow-1 overflow-auto p-3">
				{{pm.dataSignDetail.detail}}
			</div>
		</div>
	</p-overlaypanel>
</div>
<#include "../include/page_form.ftl">
<#include "../include/page_boolean_options.ftl">
<script>
(function(po)
{
	po.submitUrl = "/chart/"+po.submitAction;
	
	po.beforeSubmitForm = function(action)
	{
		var data = action.options.data;
		po.unmergeChartCdss(data);
		
		var cdss = (data.chartDataSetVOs || []);
		$.each(cdss, function(idx, cds)
		{
			cds.summaryDataSetEntity = cds.dataSet;
			cds.dataSet = undefined;
		});
	};
	
	po.mergeChartCdss = function(chart)
	{
		var cdss = (chart.chartDataSetVOs || []);
		$.each(cdss, function(idx, cds)
		{
			po.mergeChartDataSet(cds, chart.htmlChartPlugin);
		});
	};

	po.unmergeChartCdss = function(chart)
	{
		var cdss = (chart.chartDataSetVOs || []);
		$.each(cdss, function(idx, cds)
		{
			po.unmergeChartDataSet(cds, chart.htmlChartPlugin);
		});
	};
	
	po.mergeChartDataSet = function(chartDataSet, chartPlugin)
	{
		var dataSet = chartDataSet.dataSet;
		var properties = (dataSet ? dataSet.properties : []);
		var dataSigns = (chartPlugin && chartPlugin.dataSigns ? chartPlugin.dataSigns : []);
		
		$.each(properties, function(idx, property)
		{
			var signs = [];
			
			var propertySigns = (chartDataSet.propertySigns[property.name] || []);
			$.each(propertySigns, function(psIdx, ps)
			{
				var inArrayIdx = $.inArrayById(dataSigns, ps, "name");
				if(inArrayIdx >= 0)
					signs.push(dataSigns[inArrayIdx]);
			});
			
			property.cdsInfo =
			{
				signs: signs,
				alias: chartDataSet.propertyAliases[property.name],
				order: chartDataSet.propertyOrders[property.name]
			};
		});
	};
	
	po.unmergeChartDataSet = function(chartDataSet, chartPlugin)
	{
		var dataSet = chartDataSet.dataSet;
		var properties = (dataSet ? dataSet.properties : []);
		var dataSigns = (chartPlugin && chartPlugin.dataSigns ? chartPlugin.dataSigns : []);
		
		$.each(properties, function(idx, property)
		{
			var cdsInfo = (property.cdsInfo || {});
			var signs = (cdsInfo.signs || []);
			
			var propertySigns = [];
			$.each(signs, function(psIdx, sign)
			{
				var inArrayIdx = $.inArrayById(dataSigns, sign.name, "name");
				if(inArrayIdx >= 0)
					propertySigns.push(sign.name);
			});
			
			if(propertySigns.length > 0)
				chartDataSet.propertySigns[property.name] = propertySigns;
			chartDataSet.propertyAliases[property.name] = cdsInfo.alias;
			chartDataSet.propertyOrders[property.name] = cdsInfo.order;
			
			property.cdsInfo = undefined;
		});
	};
	
	po.formatDataSignLabel = function(dataSign)
	{
		if(dataSign.nameLabel && dataSign.nameLabel.value)
			return dataSign.nameLabel.value + " ("+dataSign.name+")";
		else
			return dataSign.name;
	};
	
	var formModel = <@writeJson var=formModel />;
	formModel = $.unescapeHtmlForJson(formModel);
	formModel.analysisProject = (formModel.analysisProject == null ? {} : formModel.analysisProject);
	formModel.chartDataSetVOs = (formModel.chartDataSetVOs == null ? [] : formModel.chartDataSetVOs);
	formModel.plugin = undefined;
	formModel.chartDataSets = undefined;
	po.mergeChartCdss(formModel);
	
	po.setupForm(formModel, {},
	{
		rules:
		{
			updateInterval: {"integer": true}
		}
	});
	
	po.vuePageModel(
	{
		chartPluginDataSigns: (formModel.htmlChartPlugin ? (formModel.htmlChartPlugin.dataSigns || []) : []),
		dataSignDetail: { label: "", detail: "" },
		dataSetPropertyForSign: null,
		updateIntervalType: (formModel.updateInterval > -1 ? "interval" : "none"),
		updateIntervalTypeOptions:
		[
			{ name: "<@spring.message code='noUpdate' />", value: "none" },
			{ name: "<@spring.message code='interval' />", value: "interval" }
		]
	});
	
	po.vueRef("${pid}dataSignsPanelEle", null);
	po.vueRef("${pid}dataSignDetailPanelEle", null);
	
	po.vueMethod(
	{
		formatChartPlugin: function(chartPlugin)
		{
			return $.toChartPluginHtml(chartPlugin, po.contextPath, {justifyContent: "start"});
		},
		
		formatDataSignLabel: function(dataSign)
		{
			return po.formatDataSignLabel(dataSign);
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
				po.unmergeChartCdss(fm);
				po.mergeChartCdss(fm);
				
				var pm = po.vuePageModel();
				pm.chartPluginDataSigns = (plugin.dataSigns || []);
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
						var cds =
						{
							dataSet: dataSet,
							propertySigns: {},
							propertyAliases: {},
							propertyOrders: {},
							attachment: false
						};
						
						po.mergeChartDataSet(cds);
						fm.chartDataSetVOs.push(cds);
					});
				});
			});
		},
		
		onShowDataSignPanel: function(e, dataSetProperty)
		{
			var pm = po.vuePageModel();
			pm.dataSetPropertyForSign = dataSetProperty;
			
			po.vueUnref("${pid}dataSignsPanelEle").show(e);
		},
		
		onShowDataSignDetail: function(e, dataSign)
		{
			var pm = po.vuePageModel();
			
			pm.dataSignDetail.label = po.formatDataSignLabel(dataSign);
			pm.dataSignDetail.detail = (dataSign.descLabel ? (dataSign.descLabel.value || "") : "");
			
			po.vueUnref("${pid}dataSignDetailPanelEle").show(e);
		},
		
		onAddDataSign: function(e, dataSign)
		{
			var pm = po.vuePageModel();
			
			if(pm.dataSetPropertyForSign)
			{
				var signs = pm.dataSetPropertyForSign.cdsInfo.signs;
				
				if($.inArrayById(signs, dataSign.name, "name") < 0)
					signs.push(dataSign);
			}
		},
		
		onRemoveDataSign: function(dataSetProperty, dataSigName)
		{
			var signs = dataSetProperty.cdsInfo.signs;
			$.removeById(signs, dataSigName, "name");
		},
		
		onUpdateIntervalTypeChange: function(e)
		{
			var fm = po.vueFormModel();
			var pm = po.vuePageModel();
			
			if(e.value == "none")
			{
				po._updateIntervalBackup = fm.updateInterval;
				fm.updateInterval = -1;
			}
			else if(e.value == "interval")
			{
				if(po._updateIntervalBackup != null && po._updateIntervalBackup > -1)
					fm.updateInterval = po._updateIntervalBackup;
				else
					fm.updateInterval = 1000;
			}
		}
	});
	
	po.vueMount();
})
(${pid});
</script>
</body>
</html>
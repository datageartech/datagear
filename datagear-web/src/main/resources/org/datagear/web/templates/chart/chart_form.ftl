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
<#assign ResultDataFormat=statics['org.datagear.analysis.ResultDataFormat']>
<#assign ChartPluginAttributeType=statics['org.datagear.analysis.ChartPluginAttribute$DataType']>
<#assign DataSign=statics['org.datagear.analysis.DataSign']>
<#include "../include/page_import.ftl">
<#include "../include/html_doctype.ftl">
<html>
<head>
<#include "../include/html_head.ftl">
<title>
	<@spring.message code='module.chart' />
	<#include "../include/html_request_action_suffix.ftl">
	<#include "../include/html_app_name_suffix.ftl">
</title>
</head>
<body class="p-card no-border">
<#include "../include/page_obj.ftl">
<div id="${pid}" class="page page-form horizontal page-form-chart">
	<form id="${pid}form" class="flex flex-column" :class="{readonly: pm.isReadonlyAction}">
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
				<label for="${pid}pluginVo" class="field-label col-12 mb-2 md:col-3 md:mb-0">
					<@spring.message code='chartType' />
				</label>
				<div class="field-input col-12 md:col-9">
					<div class="p-inputgroup">
						<div id="${pid}pluginVo" class="input p-component p-inputtext border-round-left flex align-items-center">
							<div class="flex-grow-0" v-html="formatChartPlugin(fm.pluginVo)"></div>
						</div>
						<p-button type="button" label="<@spring.message code='select' />"
							@click="onSelectChartPlugin" v-if="!pm.isReadonlyAction">
						</p-button>
					</div>
		        	<div class="desc text-color-secondary pt-1">
		        		<div class="flex flex-row align-items-center gap-1" v-if="fm.pluginVo && fm.pluginVo.descLabel && fm.pluginVo.descLabel.value">
		        			<div class="white-space-nowrap overflow-hidden text-overflow-ellipsis text-sm" style="max-width:90%;">
		        				{{fm.pluginVo.descLabel.value}}
		        			</div>
	        				<p-button type="button" icon="pi pi-angle-down" size="small" rounded
								@click="onShowChartPluginDesc" class="p-button-secondary p-button-text p-1">
							</p-button>
		        		</div>
		        	</div>
		        	<div class="validate-msg">
		        		<input name="pluginVo" required type="text" class="validate-proxy" />
		        	</div>
				</div>
			</div>
			<div class="field grid">
				<label for="${pid}dataSetBindVOs" class="field-label col-12 mb-2 md:col-3 md:mb-0"
					title="<@spring.message code='chart.dsb.desc' />">
					<@spring.message code='dataSetBind' />
				</label>
				<div class="field-input col-12 md:col-9">
					<div id="${pid}dataSetBindVOs" class="chart-datasets input p-component p-inputtext w-full overflow-auto p-2">
						<p-panel v-for="(dsb, dsbIdx) in fm.dataSetBindVOs" :key="dsbIdx" :header="dsb.dataSet.name" :toggleable="true" class="p-card mb-2 no-panel-border">
							<template #icons>
								<p-button icon="pi pi-arrow-up" class="p-button-sm p-button-secondary p-button-rounded p-button-text mr-2"
									@click="onMoveUpDataSetBind($event, dsbIdx)" v-if="!pm.isReadonlyAction">
								</p-button>
								<p-button icon="pi pi-arrow-down" class="p-button-sm p-button-secondary p-button-rounded p-button-text mr-2"
									@click="onMoveDownDataSetBind($event, dsbIdx)" v-if="!pm.isReadonlyAction">
								</p-button>
								<p-button icon="pi pi-times" class="p-button-sm p-button-secondary p-button-rounded p-button-text p-button-danger mr-5"
									@click="onDeleteDataSetBind($event, dsbIdx)" v-if="!pm.isReadonlyAction">
								</p-button>
							</template>
							<div>
								<div class="px-2" v-if="pm.pluginHasDataSetSign">
									<div class="field grid mb-2">
										<label :for="'${pid}dsbSign_'+dsbIdx" class="field-label col-12 mb-2 md:col-3 md:mb-0"
											title="<@spring.message code='chart.dsb.dataSetSign.desc' />">
											<@spring.message code='sign' />
										</label>
										<div class="field-input col-12 md:col-9">
											<div class="p-inputgroup">
												<div :id="'${pid}dsbSign_'+dsbIdx"
													class="input p-component p-inputtext border-round-left overflow-auto" style="height:4rem;">
													<p-chip v-for="sign in dsb.extSignObjs" :key="sign.extFullname" :label="sign.extLabel" class="mb-2"
														:removable="!pm.isReadonlyAction" @remove="onRemoveDataSetDataSign(dsb, sign.extFullname)">
													</p-chip>
												</div>
												<p-button type="button" icon="pi pi-plus"
													aria:haspopup="true" aria-controls="${pid}dataSignsPanel"
													@click="onShowDataSignPanel($event, dsb)" v-if="!pm.isReadonlyAction">
												</p-button>
											</div>
										</div>
									</div>
								</div>
								
								<p-fieldset v-for="(df, dfIdx) in dsb.dataSet.fields" :key="dfIdx" :legend="formatDspFieldsetName(df)" class="fieldset-sm mb-3">
									<div class="field grid mb-2">
										<label :for="'${pid}dsbpidSign_'+dsbIdx+'_'+dfIdx" class="field-label col-12 mb-2 md:col-3 md:mb-0"
											title="<@spring.message code='chart.dsb.fieldSign.desc' />">
											<@spring.message code='sign' />
										</label>
										<div class="field-input col-12 md:col-9">
											<div class="p-inputgroup">
												<div :id="'${pid}dsbpidSign_'+dsbIdx+'_'+dfIdx"
													class="input p-component p-inputtext border-round-left overflow-auto" style="height:4rem;">
													<p-chip v-for="sign in df.extDsbInfo.signObjs" :key="sign.extFullname" :label="sign.extLabel" class="mb-2"
														:removable="!pm.isReadonlyAction" @remove="onRemoveFieldDataSign(df, sign.extFullname)">
													</p-chip>
												</div>
												<p-button type="button" icon="pi pi-plus"
													aria:haspopup="true" aria-controls="${pid}dataSignsPanel"
													@click="onShowDataSignPanel($event, dsb, df)" v-if="!pm.isReadonlyAction">
												</p-button>
											</div>
										</div>
									</div>
									<div class="field grid mb-2">
										<label :for="'${pid}dsbpidAlias_'+dsbIdx+'_'+dfIdx" class="field-label col-12 mb-2 md:col-3 md:mb-0"
											title="<@spring.message code='chart.dsb.fieldAlias.desc' />">
											<@spring.message code='alias' />
										</label>
										<div class="field-input col-12 md:col-9">
											<p-inputtext :id="'${pid}dsbpidAlias_'+dsbIdx+'_'+dfIdx" v-model="df.extDsbInfo.alias" type="text"
												class="input w-full" maxlength="50" :placeholder="df.label ? df.label : df.name">
											</p-inputtext>
										</div>
									</div>
									<div class="field grid mb-2">
										<label :for="'${pid}dsbpidSort_'+dsbIdx+'_'+dfIdx" class="field-label col-12 mb-2 md:col-3 md:mb-0"
											title="<@spring.message code='chart.dsb.fieldOrder.desc' />">
											<@spring.message code='sort' />
										</label>
										<div class="field-input col-12 md:col-9">
											<p-inputtext :id="'${pid}dsbpidSort_'+dsbIdx+'_'+dfIdx" v-model="df.extDsbInfo.order" type="text" class="input w-full" maxlength="50" :placeholder="dfIdx">
											</p-inputtext>
										</div>
									</div>
								</p-fieldset>
							</div>
							<p-divider type="dashed"></p-divider>
							<div class="px-2">
								<div class="field grid mb-2">
									<label :for="'${pid}dsbAlias_'+dsbIdx" class="field-label col-12 mb-2 md:col-3 md:mb-0"
										title="<@spring.message code='chart.dsb.alias.desc' />">
										<@spring.message code='alias' />
									</label>
									<div class="field-input col-12 md:col-9">
										<p-inputtext :id="'${pid}dsbAlias_'+dsbIdx" v-model="dsb.alias" type="text" class="input w-full" maxlength="50" :placeholder="dsb.dataSet.name">
										</p-inputtext>
									</div>
								</div>
								<div class="field grid">
									<label :for="'${pid}dsbAtchm_'+dsbIdx" class="field-label col-12 mb-2 md:col-3 md:mb-0"
										title="<@spring.message code='chart.dsb.attachment.desc' />">
										<@spring.message code='attachment' />
									</label>
									<div class="field-input col-12 md:col-9">
										<p-selectbutton :id="'${pid}dsbAtchm_'+dsbIdx" v-model="dsb.attachment" :options="pm.booleanOptions"
											option-label="name" option-value="value" class="input w-full">
										</p-selectbutton>
									</div>
								</div>
								<div class="field grid" v-if="dsb.dataSet.params.length > 0">
									<label class="field-label col-12 mb-2 md:col-3 md:mb-0"
										title="<@spring.message code='chart.dsb.paramValue.desc' />">
										<@spring.message code='parameter' />
									</label>
									<div class="field-input col-12 md:col-9 h-opts">
										<p-button type="button" :label="pm.isReadonlyAction ? '<@spring.message code='view' />' : '<@spring.message code='edit' />'"
											aria:haspopup="true" aria-controls="${pid}paramPanel"
											@click="onShowParamPanel($event, dsb)" class="p-button-secondary">
										</p-button>
										<p-button type="button" label="<@spring.message code='clear' />"
											@click="onClearParamValues($event, dsb)" class="p-button-secondary p-button-danger"
											v-if="!pm.isReadonlyAction">
										</p-button>
									</div>
								</div>
							</div>
						</p-panel>
					</div>
					<div class="mt-1">
						<div class="flex justify-content-between">
							<div>
								<p-button type="button" label="<@spring.message code='select' />"
									@click="onAddDataSet" v-if="!pm.isReadonlyAction">
								</p-button>
							</div>
							<div>
								<p-button type="button" label="<@spring.message code='dataFormat' />"
									aria:haspopup="true" aria-controls="${pid}dataFormatPanel"
									@click="onShowDataFormatPanel" class="p-button-secondary">
								</p-button>
							</div>
						</div>
					</div>
		        	<div class="validate-msg">
		        		<input name="dspDataSignCheckVal" type="text" class="validate-normalizer" />
		        		<input name="validateDataSetRangeVal" type="text" class="validate-normalizer" />
		        	</div>
				</div>
			</div>
			<div class="field grid">
				<label for="${pid}attrValues" class="field-label col-12 mb-2 md:col-3 md:mb-0"
					title="<@spring.message code='chart.attrValues.desc' />">
					<@spring.message code='chartAttribute' />
				</label>
				<div class="field-input col-12 md:col-9">
					<div id="${pid}attrValues" class="flex align-items-center">
						<p-button type="button" :label="pm.isReadonlyAction ? '<@spring.message code='view' />' : '<@spring.message code='edit' />'"
							:disabled="!fm.pluginVo || !fm.pluginVo.attributes || fm.pluginVo.attributes.length==0"
							@click="onShowAttrValuesPanel" class="p-button-secondary mr-2">
						</p-button>
			        	<div class="desc text-color-secondary text-sm" v-if="fm.pluginVo && (!fm.pluginVo.attributes || fm.pluginVo.attributes.length==0)">
			        		<@spring.message code='chart.attrValues.noAttrDefined' />
			        	</div>
		        	</div>
		        	<div class="validate-msg">
		        		<input name="chartAttrValuesCheckVal" type="text" class="validate-normalizer" />
		        	</div>
				</div>
			</div>
			<div class="field grid">
				<label for="${pid}options" class="field-label col-12 mb-2 md:col-3 md:mb-0"
					title="<@spring.message code='chart.options.desc' />">
					<@spring.message code='chartOptions' />
				</label>
				<div class="field-input col-12 md:col-9">
					<div id="${pid}options" class="flex align-items-center">
						<p-button type="button" :label="pm.isReadonlyAction ? '<@spring.message code='view' />' : '<@spring.message code='edit' />'"
							aria:haspopup="true" aria-controls="${pid}optionsPanel"
							@click="onShowOptionsPanel" class="p-button-secondary mr-2">
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
		<div class="page-form-foot flex-grow-0 flex justify-content-center gap-2 pt-2">
			<p-button type="submit" label="<@spring.message code='save' />"></p-button>
			<p-button type="button" label="<@spring.message code='saveAndShow' />" @click="onSaveAndShow" v-if="!pm.disableSaveShow"></p-button>
		</div>
	</form>
	<p-overlaypanel ref="${pid}dataSignsPanelEle" append-to="body"
		:show-close-icon="false" id="${pid}dataSignsPanel">
		<div class="pb-2">
			<label class="text-lg font-bold">
				<@spring.message code='dataSign' />
			</label>
		</div>
		<div class="panel-content-size-xs-mwh overflow-auto p-2">
			<div v-for="ds in pm.candidateDataSigns" :key="ds.extFullname" class="mb-2">
				<div class="p-inputgroup">
					<p-button type="button" :label="ds.extLabel" icon="pi pi-plus"
						@click="onAddDataSign($event, ds)">
					</p-button>
					<p-button type="button" icon="pi pi-info-circle"
						aria:haspopup="true" aria-controls="${pid}dataSignDetailPanel"
						@click="onShowDataSignDetail($event, ds)" @mouseover="onUpdateDataSignDetailPanel($event, ds)">
					</p-button>
				</div>
			</div>
		</div>
	</p-overlaypanel>
	<p-overlaypanel ref="${pid}dataSignDetailPanelEle" append-to="body" id="${pid}dataSignDetailPanel"
		@show="onDataSignDetailPanelShow" @hide="onDataSignDetailPanelHide">
		<div class="pb-2">
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
	<p-overlaypanel ref="${pid}paramPanelEle" append-to="body"
		:show-close-icon="false" @show="onParamPanelShow" @hide="onParamPanelHide" id="${pid}paramPanel" class="dataset-paramvalue-panel">
		<div class="pb-2">
			<label class="text-lg font-bold">
				<@spring.message code='parameter' />
			</label>
		</div>
		<div class="paramvalue-form-wrapper panel-content-size-sm overflow-auto p-2"></div>
	</p-overlaypanel>
	<p-overlaypanel ref="${pid}dataFormatPanelEle" append-to="body"
		:show-close-icon="false" id="${pid}dataFormatPanel">
		<div class="pb-2">
			<label class="text-lg font-bold" title="<@spring.message code='chart.rdf.desc' />">
				<@spring.message code='dataFormat' />
			</label>
		</div>
		<div class="panel-content-size-xs overflow-auto p-2">
			<div class="field grid">
				<label for="${pid}rdfEnabled" class="field-label col-12 mb-2"
					title="<@spring.message code='chart.rdf.enabled.desc' />">
					<@spring.message code='isEnable' />
				</label>
				<div class="field-input col-12">
					<p-selectbutton id="${pid}rdfEnabled" v-model="pm.enableResultDataFormat" :options="pm.booleanOptions"
						option-label="name" option-value="value" class="input w-full">
					</p-selectbutton>
				</div>
			</div>
			<div class="field grid" v-if="pm.enableResultDataFormat">
				<label for="${pid}rdfDateType" class="field-label col-12 mb-2">
					<@spring.message code='dateType' />
				</label>
				<div class="field-input col-12">
					<p-selectbutton id="${pid}rdfDateType" v-model="pm.resultDataFormat.dateType" :options="pm.dateOrTimeTypeOptions"
						option-label="name" option-value="value" class="input w-full">
					</p-selectbutton>
				</div>
			</div>
			<div class="field grid" v-if="pm.enableResultDataFormat">
				<label for="${pid}rdfDateFormat" class="field-label col-12 mb-2"
					title="<@spring.message code='chart.rdf.dateFormat.desc' />">
					<@spring.message code='dateFormat' />
				</label>
				<div class="field-input col-12">
					<p-inputtext id="${pid}rdfDateFormat" v-model="pm.resultDataFormat.dateFormat" type="text"
						class="input w-full" maxlength="100">
					</p-inputtext>
				</div>
			</div>
			<div class="field grid" v-if="pm.enableResultDataFormat">
				<label for="${pid}rdfTimeType" class="field-label col-12 mb-2">
					<@spring.message code='timeType' />
				</label>
				<div class="field-input col-12">
					<p-selectbutton id="${pid}rdfTimeType" v-model="pm.resultDataFormat.timeType" :options="pm.dateOrTimeTypeOptions"
						option-label="name" option-value="value" class="input w-full">
					</p-selectbutton>
				</div>
			</div>
			<div class="field grid" v-if="pm.enableResultDataFormat">
				<label for="${pid}rdfTimeFormat" class="field-label col-12 mb-2"
					title="<@spring.message code='chart.rdf.timeFormat.desc' />">
					<@spring.message code='timeFormat' />
				</label>
				<div class="field-input col-12">
					<p-inputtext id="${pid}rdfTimeFormat" v-model="pm.resultDataFormat.timeFormat" type="text"
						class="input w-full" maxlength="100">
					</p-inputtext>
				</div>
			</div>
			<div class="field grid" v-if="pm.enableResultDataFormat">
				<label for="${pid}rdfTimestampType" class="field-label col-12 mb-2">
					<@spring.message code='datetimeType' />
				</label>
				<div class="field-input col-12">
					<p-selectbutton id="${pid}rdfTimestampType" v-model="pm.resultDataFormat.timestampType" :options="pm.dateOrTimeTypeOptions"
						option-label="name" option-value="value" class="input w-full">
					</p-selectbutton>
				</div>
			</div>
			<div class="field grid" v-if="pm.enableResultDataFormat">
				<label for="${pid}rdfTimestampFormat" class="field-label col-12 mb-2"
					title="<@spring.message code='chart.rdf.timestampFormat.desc' />">
					<@spring.message code='datetimeFormat' />
				</label>
				<div class="field-input col-12">
					<p-inputtext id="${pid}rdfTimestampFormat" v-model="pm.resultDataFormat.timestampFormat" type="text"
						class="input w-full" maxlength="100">
					</p-inputtext>
				</div>
			</div>
		</div>
	</p-overlaypanel>
	<p-overlaypanel ref="${pid}pluginVoDescEle" append-to="body" id="${pid}pluginVoDesc">
		<div class="pb-2">
			<label class="text-lg font-bold">
				<@spring.message code='desc' />
			</label>
		</div>
		<div class="panel-content-size-xxs overflow-auto flex flex-column p-2">
			<div class="white-space-pre-wrap" v-text="formatChartPluginDesc(fm.pluginVo)"></div>
		</div>
	</p-overlaypanel>
	<!-- 这里使用对话框组件而非弹出面板组件，因为其内部存在下拉框等组件，使用弹出面板时会出现错位问题 -->
	<p-dialog header="<@spring.message code='chartAttribute' />" append-to="body" position="center" :dismissable-mask="true"
		v-model:visible="pm.attrValuesPanelShown" id="${pid}attrValuesPanel" @show="onAttrValuesPanelShow">
		<div class="page page-form chart-form-chart-attr-values">
			<#include "include/chart_attr_values_form.ftl">
		</div>
	</p-dialog>
	<p-overlaypanel ref="${pid}optionsPanelEle" append-to="body" id="${pid}optionsPanel" @show="onOptionsPanelShow">
		<div class="pb-2">
			<label class="text-lg font-bold">
				<@spring.message code='chartOptions' />
			</label>
		</div>
		<div class="page page-form">
			<form id="${pid}optionsForm" class="flex flex-column" :class="{readonly: pm.isReadonlyAction}">
				<div class="page-form-content flex-grow-1 px-2 py-1 overflow-y-auto">
					<div class="field grid">
						<div class="field-input col-12">
							<div id="${pid}optionsContent" class="code-editor-wrapper input p-component p-inputtext panel-content-size-xxs">
								<div id="${pid}optionsContentCodeEditor" class="code-editor"></div>
							</div>
				        	<div class="desc text-color-secondary">
				        		<small><@spring.message code='chartOptions.formatDesc' /></small>
				        	</div>
						</div>
					</div>
				</div>
				<div class="page-form-foot flex-grow-0 flex justify-content-center gap-2 pt-2">
					<p-button type="submit" label="<@spring.message code='confirm' />"></p-button>
				</div>
			</form>
		</div>
	</p-overlaypanel>
	<#include "../include/page_palette.ftl">
</div>
<#include "../include/page_form.ftl">
<#include "../include/page_simple_form.ftl">
<#include "../include/page_boolean_options.ftl">
<#include "../include/page_code_editor.ftl">
<script>
(function(po)
{
	po.submitUrl = "/chart/"+po.submitAction;
	po.disableSaveShow = ("${(disableSaveShow!false)?string('true', 'false')}"  == "true");
	
	po.inSaveAndShowAction = function(val)
	{
		if(val === undefined)
			return (po._inSaveAndShowAction == true);
		
		po._inSaveAndShowAction = val;
	};
	
	po.beforeSubmitForm = function(action)
	{
		var data = action.options.data;
		po.unmergeChartDsbs(data);
		
		var dsbs = (data.dataSetBindVOs || []);
		$.each(dsbs, function(idx, dsb)
		{
			dsb.summaryDataSetEntity = dsb.dataSet;
			dsb.dataSet = undefined;
		});
		
		//这里必须整理属性值，因为存在切换图表类型而不编辑图表属性的情况
		var cpas = po.trimChartPluginAttributes(data.pluginVo ? data.pluginVo.attributes : null);
		data.attrValues = po.trimChartAttrValues(data.attrValues, cpas);
		
		var pm = po.vuePageModel();
		if(pm.enableResultDataFormat)
			data.resultDataFormat = po.vueRaw(pm.resultDataFormat);
		else
			data.resultDataFormat = undefined;
		
		data.pluginVo = (data.pluginVo ? { id: data.pluginVo.id } : null);
		
		action.options.saveAndShowAction = po.inSaveAndShowAction();
	};
	
	po.validateDataSetBindDataSign = function(chart)
	{
		var chartPlugin = chart.pluginVo;
		var dataSetBinds = (chart.dataSetBindVOs || []);
		var dataSigns = (chartPlugin ? (chartPlugin.dataSigns || []) : []);
		
		if(!dataSigns)
			return true;
		
		var requiredSigns = [];
		
		$.each(dataSigns, function(idx, dataSign)
		{
			if(dataSign.required == true)
				requiredSigns.push(dataSign);
		});
		
		for(var i=0; i<dataSetBinds.length; i++)
		{
			var dataSetBind = dataSetBinds[i];
			
			if(dataSetBind.attachment == true)
				continue;
			
			var fields = (dataSetBind.dataSet.fields || []);
			
			for(var j=0; j<requiredSigns.length; j++)
			{
				var requiredSign = requiredSigns[j];
				var contains = false;
				
				for(var k=0; k<fields.length; k++)
				{
					var field = fields[k];
					var signObjs = (field.extDsbInfo ? (field.extDsbInfo.signObjs || []) : []);
					
					if($.inArrayById(signObjs, requiredSign.extFullname, "extFullname") > -1)
					{
						contains = true;
						break;
					}
				}
				
				if(!contains)
				{
					var invalidInfo = { dataSet: dataSetBind.dataSet, dataSign: requiredSign };
					return invalidInfo;
				}
			}
		}
		
		return true;
	};
	
	po.hasDataSetFieldSigned = function(dataSetBind, dataSign)
	{
		var fields = (dataSetBind.dataSet.fields || []);
		
		for(var i=0; i<fields.length; i++)
		{
			var field = fields[i];
			var signObjs = (field.extDsbInfo ? (field.extDsbInfo.signObjs || []) : []);
			
			if($.inArrayById(signObjs, dataSign.extFullname, "extFullname") > -1)
				return true;
		}
		
		return false;
	};
	
	po.mergeChartDsbs = function(chart)
	{
		var dsbs = (chart.dataSetBindVOs || []);
		$.each(dsbs, function(idx, dsb)
		{
			po.mergeDataSetBind(dsb, chart.pluginVo);
		});
	};

	po.unmergeChartDsbs = function(chart)
	{
		var dsbs = (chart.dataSetBindVOs || []);
		$.each(dsbs, function(idx, dsb)
		{
			po.unmergeDataSetBind(dsb, chart.pluginVo);
		});
	};
	
	po.mergeDataSetBind = function(dataSetBind, chartPlugin)
	{
		var dataSet = dataSetBind.dataSet;
		var fields = (dataSet ? dataSet.fields : []);
		var dataSigns = (chartPlugin && chartPlugin.dataSigns ? chartPlugin.dataSigns : []);
		
		$.each(fields, function(idx, field)
		{
			var signObjs = [];
			
			var fieldSigns = (dataSetBind.fieldSigns[field.name] || []);
			$.each(fieldSigns, function(fsIdx, signName)
			{
				var dataSign = po.findDataSignByFullname(dataSigns, signName);
				if(dataSign != null)
					signObjs.push(dataSign);
			});
			
			field.extDsbInfo =
			{
				signObjs: signObjs,
				alias: dataSetBind.fieldAliases[field.name],
				order: dataSetBind.fieldOrders[field.name]
			};
		});
		
		dataSetBind.extSignObjs = [];
		
		if(dataSetBind.dataSetSigns)
		{
			$.each(dataSetBind.dataSetSigns, function(idx, signName)
			{
				var dataSign = po.findDataSignByFullname(dataSigns, signName, false);
				if(dataSign != null)
					dataSetBind.extSignObjs.push(dataSign);
			});
		}
	};
	
	po.unmergeDataSetBind = function(dataSetBind, chartPlugin)
	{
		var dataSet = dataSetBind.dataSet;
		var fields = (dataSet ? dataSet.fields : []);
		var dataSigns = (chartPlugin && chartPlugin.dataSigns ? chartPlugin.dataSigns : []);
		
		$.each(fields, function(idx, field)
		{
			var extDsbInfo = (field.extDsbInfo || {});
			var signObjs = (extDsbInfo.signObjs || []);
			
			var fieldSigns = [];
			$.each(signObjs, function(fsIdx, signObj)
			{
				if(po.findDataSignByFullname(dataSigns, signObj.extFullname) != null)
					fieldSigns.push(signObj.extFullname);
			});
			
			if(fieldSigns.length > 0)
				dataSetBind.fieldSigns[field.name] = fieldSigns;
			else
				dataSetBind.fieldSigns[field.name] = undefined;
			dataSetBind.fieldAliases[field.name] = extDsbInfo.alias;
			dataSetBind.fieldOrders[field.name] = extDsbInfo.order;
			
			field.extDsbInfo = undefined;
		});
		
		dataSetBind.dataSetSigns = [];
		
		if(dataSetBind.extSignObjs)
		{
			$.each(dataSetBind.extSignObjs, function(idx, signObj)
			{
				var dataSign = po.findDataSignByFullname(dataSigns, signObj.extFullname, false);
				if(dataSign != null)
					dataSetBind.dataSetSigns.push(signObj.extFullname);
			});
		}
		
		dataSetBind.extSignObjs = undefined;
	};
	
	po.extPluginDataSigns = function(plugin)
	{
		if(!plugin)
			return;
		
		plugin.dataSigns = po.extDataSigns(plugin.dataSigns);
	};
	
	po.extDataSigns = function(dataSigns, parent)
	{
		dataSigns = (dataSigns ? dataSigns : []);
		parent = (parent == null ? null : parent);
		
		for(var i=0; i<dataSigns.length; i++)
		{
			var dsn = dataSigns[i];
			
			dsn.extFullname = (parent && parent.extFullname ? (parent.extFullname + chartFactory.DATA_SIGN_FULLNAME_SEPARATOR + dsn.name) : dsn.name);
			dsn.extLabel = (parent && parent.extLabel ? (parent.extLabel + chartFactory.DATA_SIGN_FULLNAME_SEPARATOR + po.formatDataSignLabel(dsn)) : po.formatDataSignLabel(dsn));
			
			if(dsn.children)
				po.extDataSigns(dsn.children, dsn);
		}
		
		return dataSigns;
	};
	
	po.hasDataSetSign = function(plugin)
	{
		var dataSigns = (plugin ? plugin.dataSigns : null);
		
		if(!dataSigns)
			return false;
		
		for(var i=0; i<dataSigns.length; i++)
		{
			if(!po.isDataSignTargetField(dataSigns[i]))
			{
				return true;
			}
		}
		
		return false;
	};
	
	po.findDataSignByFullname = function(dataSigns, fullname, deepSearch)
	{
		deepSearch = (deepSearch == null ? true : deepSearch);
		
		if(!dataSigns)
			return null;
		
		//应该先广度搜索、再深度搜索
		for(var i=0; i<dataSigns.length; i++)
		{
			if(dataSigns[i].extFullname == fullname)
				return dataSigns[i];
		}
		
		for(var i=0; i<dataSigns.length; i++)
		{
			if(dataSigns[i].children)
			{
				var d = po.findDataSignByFullname(dataSigns[i].children, fullname);
				if(d != null)
					return d;
			}
		}
		
		return null;
	};

	po.evalCandidateDataSignsForDataSet = function(dataSigns, dsb)
	{
		var re = [];
		
		for(var i=0; i<dataSigns.length; i++)
		{
			var dsi = dataSigns[i];
			
			if(!po.isDataSignTargetField(dsi))
			{
				re.push(dsi);
			}
		}
		
		return re;
	};
	
	po.evalCandidateDataSignsForField = function(dataSigns, dsb)
	{
		var re = [];
		
		for(var i=0; i<dataSigns.length; i++)
		{
			var dsi = dataSigns[i];
			
			if(po.isDataSignTargetField(dsi))
			{
				re.push(dsi);
			}
		}
		
		if(dsb.extSignObjs)
		{
			$.each(dsb.extSignObjs, function(idx, signObj)
			{
				var signObjChildren = (signObj.children || []);
				for(var i=0; i<signObjChildren.length; i++)
				{
					if(po.isDataSignTargetField(signObjChildren[i]))
					{
						re.push(signObjChildren[i]);
					}
				}
			});
		}
		
		return re;
	};
	
	po.isDataSignTargetField = function(dataSign)
	{
		return (dataSign.target == null || dataSign.target == "" || dataSign.target == "${DataSign.TARGET_FIELD}");
	};
	
	po.formatDataSignLabel = function(dataSign)
	{
		if(dataSign.nameLabel && dataSign.nameLabel.value)
			return dataSign.nameLabel.value + "("+dataSign.name+")";
		else
			return dataSign.name;
	};

	po.inflateParamPanel = function(dataSetBind)
	{
		var wrapper = $(".paramvalue-form-wrapper", po.elementOfId("${pid}paramPanel", document.body));
		var pm = po.vuePageModel();
		
		if(!dataSetBind.query)
			dataSetBind.query = {};
		
		var formOptions = $.extend(
		{
			submitText: "<@spring.message code='confirm' />",
			yesText: "<@spring.message code='yes' />",
			noText: "<@spring.message code='no' />",
			paramValues: po.vueRaw(dataSetBind.query.paramValues),
			readonly: pm.isReadonlyAction,
			render: function()
			{
				$("select, input[type='text'], textarea", this).addClass("p-inputtext p-component w-full");
				$("button", this).addClass("p-button p-component");
				$.focusOnFirstInput(this);
			},
			submit: function()
			{
				var paramValues = chartFactory.chartSetting.getDataSetParamValueObj(this);
				dataSetBind.query.paramValues = paramValues;
				
				po.vueUnref("${pid}paramPanelEle").hide();
			}
		});
		
		chartFactory.chartSetting.removeDatetimePickerRoot();
		wrapper.empty();
		
		var params = $.extend(true, [], po.vueRaw(dataSetBind.dataSet.params));
		chartFactory.chartSetting.renderDataSetParamValueForm(wrapper, params, formOptions);
	};
	
	$.validator.addMethod("dspDataSignRequired", function(chart, element)
	{
		var re = po.validateDataSetBindDataSign(chart);
		
		if(re == true)
		{
			$(element).removeData("invalidMsg");
			return true;
		}
		else
		{
			var msg = $.validator.format("<@spring.message code='chart.checkDataSetBindDataSign.required' />",
						re.dataSet.name, re.dataSign.extLabel);
			$(element).data("invalidMsg", msg);
			
			return false;
		}
	});

	$.validator.addMethod("validateDataSetRange", function(chart, element)
	{
		var re = true;
		
		var dsr = (chart.pluginVo ? chart.pluginVo.dataSetRange : null);
		var dsbs = (chart.dataSetBindVOs || []);
		var mainCount = 0;
		var attachmentCount = 0;
		
		$.each(dsbs, function(i, dsb)
		{
			if(dsb.attachment)
				attachmentCount++;
			else
				mainCount++;
		});
		
		var msg = "";
		var minMsg = "<@spring.message code='noLimit' />";
		var maxMsg = "<@spring.message code='noLimit' />";
		
		if(re && dsr && dsr.main)
		{
			if(dsr.main.min != null)
			{
				minMsg = dsr.main.min;
				re = (re ? (mainCount >= dsr.main.min) : false);
			}
			
			if(dsr.main.max != null)
			{
				maxMsg = dsr.main.max;
				re = (re ? (mainCount <= dsr.main.max) : false);
			}
			
			if(!re)
				msg = $.validator.format("<@spring.message code='chart.validateDataSetRange.main' />", minMsg, maxMsg, mainCount);
		}
		
		if(re && dsr && dsr.attachment)
		{
			if(dsr.attachment.min != null)
			{
				minMsg = dsr.attachment.min;
				re = (re ? (attachmentCount >= dsr.attachment.min) : false);
			}
			
			if(dsr.attachment.max != null)
			{
				maxMsg = dsr.attachment.max;
				re = (re ? (attachmentCount <= dsr.attachment.max) : false);
			}
			
			if(!re)
				msg = $.validator.format("<@spring.message code='chart.validateDataSetRange.attachment' />", minMsg, maxMsg, attachmentCount);
		}
		
		if(re)
			$(element).removeData("invalidMsg");
		else
			$(element).data("invalidMsg", msg);
		
		return re;
	});
	
	$.validator.addMethod("chartAttrValuesRequired", function(chart)
	{
		var cpas = (chart.pluginVo ? chart.pluginVo.attributes : null);
		return po.validateChartAttrValuesRequired(cpas, chart.attrValues);
	});
	
	var formModel = $.unescapeHtmlForJson(<@writeJson var=formModel />);
	formModel.pluginVo = (formModel.pluginVo == null ? {} : formModel.pluginVo);
	po.extPluginDataSigns(formModel.pluginVo);
	formModel.analysisProject = (formModel.analysisProject == null ? {} : formModel.analysisProject);
	formModel.dataSetBindVOs = (formModel.dataSetBindVOs == null ? [] : formModel.dataSetBindVOs);
	formModel.plugin = undefined;
	formModel.dataSetBinds = undefined;
	formModel.attrValues = (formModel.attrValues || {});
	po.mergeChartDsbs(formModel);
	
	po.setupForm(formModel,
	{
		success : function(response)
		{
			var fm = po.vueFormModel();
			var chart = response.data;
			
			fm.id = chart.id;
			
			var options = this;
			if(options.saveAndShowAction)
				window.open(po.concatContextPath("/cv/"+encodeURIComponent(chart.id)+"/"), "show-chart-"+chart.id);
		}
	},
	{
		rules:
		{
			updateInterval: {"integer": true},
			dspDataSignCheckVal: { "dspDataSignRequired": true },
			validateDataSetRangeVal: { "validateDataSetRange": true },
			chartAttrValuesCheckVal: { "chartAttrValuesRequired": true }
		},
		customNormalizers:
		{
			dspDataSignCheckVal: function()
			{
				return po.vueFormModel();
			},
			validateDataSetRangeVal: function()
			{
				return po.vueFormModel();
			},
			chartAttrValuesCheckVal: function()
			{
				return po.vueFormModel();
			}
		},
		messages:
		{
			dspDataSignCheckVal:
			{
				dspDataSignRequired: function(val, element)
				{
					return $(element).data("invalidMsg");
				}
			},
			validateDataSetRangeVal:
			{
				validateDataSetRange: function(val, element)
				{
					return $(element).data("invalidMsg");
				}
			},
			chartAttrValuesCheckVal: "<@spring.message code='chart.attrValues.required' />"
		}
	});
	
	po.vuePageModel(
	{
		disableSaveShow: po.disableSaveShow,
		pluginHasDataSetSign: po.hasDataSetSign(formModel.pluginVo),
		candidateDataSigns: [],
		dataSignDetail: { label: "", detail: "" },
		dataSignDetailShown: false,
		dataSignTarget: "field",
		dataSetBindForSign: null,
		dataSetFieldForSign: null,
		updateIntervalType: (formModel.updateInterval > -1 ? "interval" : "none"),
		updateIntervalTypeOptions:
		[
			{ name: "<@spring.message code='noUpdate' />", value: "none" },
			{ name: "<@spring.message code='interval' />", value: "interval" }
		],
		resultDataFormat: $.unescapeHtmlForJson(<@writeJson var=initResultDataFormat />),
		enableResultDataFormat: ("${enableResultDataFormat?string('true', 'false')}" == "true"),
		dateOrTimeTypeOptions:
		[
			{ name: "<@spring.message code='string' />", value: "${ResultDataFormat.TYPE_STRING}" },
			{ name: "<@spring.message code='number' />", value: "${ResultDataFormat.TYPE_NUMBER}" }
		],
		optionsFormModel: { options: "" },
		attrValuesPanelShown: false
	});
	
	po.vueRef("${pid}dataSignsPanelEle", null);
	po.vueRef("${pid}dataSignDetailPanelEle", null);
	po.vueRef("${pid}paramPanelEle", null);
	po.vueRef("${pid}dataFormatPanelEle", null);
	po.vueRef("${pid}pluginVoDescEle", null);
	po.vueRef("${pid}optionsPanelEle", null);
	
	po.vueMethod(
	{
		formatChartPlugin: function(chartPlugin)
		{
			return $.toChartPluginHtml(chartPlugin, po.contextPath, {justifyContent: "start"});
		},
		
		formatChartPluginDesc: function(chartPlugin)
		{
			if(chartPlugin && chartPlugin.descLabel && chartPlugin.descLabel.value)
				return chartPlugin.descLabel.value;
			else
				return "<@spring.message code='emptyDesc' />";
		},
		
		formatDspFieldsetName: function(dataSetField)
		{
			return "<@spring.message code='fieldWithColon' />" + dataSetField.name;
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
				po.extPluginDataSigns(plugin);
				
				var fm = po.vueFormModel();
				var pm = po.vuePageModel();
				
				fm.pluginVo = plugin;
				po.unmergeChartDsbs(fm);
				po.mergeChartDsbs(fm);
				pm.pluginHasDataSetSign = po.hasDataSetSign(fm.pluginVo);
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
						var dsb =
						{
							dataSet: dataSet,
							dataSetSigns: [],
							fieldSigns: {},
							fieldAliases: {},
							fieldOrders: {},
							attachment: false
						};
						
						po.mergeDataSetBind(dsb);
						fm.dataSetBindVOs.push(dsb);
					});
				});
			});
		},
		
		onMoveUpDataSetBind: function(e, dsbIdx)
		{
			var fm = po.vueFormModel();
			if(dsbIdx > 0)
			{
				var prev = fm.dataSetBindVOs[dsbIdx - 1];
				fm.dataSetBindVOs[dsbIdx - 1] = fm.dataSetBindVOs[dsbIdx];
				fm.dataSetBindVOs[dsbIdx] = prev;
			}
		},
		
		onMoveDownDataSetBind: function(e, dsbIdx)
		{
			var fm = po.vueFormModel();
			if((dsbIdx + 1) < fm.dataSetBindVOs.length)
			{
				var next = fm.dataSetBindVOs[dsbIdx + 1];
				fm.dataSetBindVOs[dsbIdx + 1] = fm.dataSetBindVOs[dsbIdx];
				fm.dataSetBindVOs[dsbIdx] = next;
			}
		},
		
		onDeleteDataSetBind: function(e, dsbIdx)
		{
			var fm = po.vueFormModel();
			fm.dataSetBindVOs.splice(dsbIdx, 1);
		},
		
		onShowDataSignPanel: function(e, dataSetBind, dataSetField)
		{
			var fm = po.vueFormModel();
			var pm = po.vuePageModel();
			
			//直接show会导致面板还停留在上一个元素上
			po.vueUnref("${pid}dataSignsPanelEle").hide();
			po.vueNextTick(function()
			{
				pm.dataSignTarget = (dataSetField != null ? "field" : "dataset");
				pm.dataSetBindForSign = dataSetBind;
				pm.dataSetFieldForSign = (dataSetField != null ? dataSetField : null);
				
				if(dataSetField != null)
					pm.candidateDataSigns = po.evalCandidateDataSignsForField(fm.pluginVo.dataSigns, dataSetBind);
				else
					pm.candidateDataSigns = po.evalCandidateDataSignsForDataSet(fm.pluginVo.dataSigns, dataSetBind);
				
				po.vueUnref("${pid}dataSignsPanelEle").show(e);
			});
		},
		
		onShowDataSignDetail: function(e, dataSign)
		{
			var pm = po.vuePageModel();
			
			//直接show会导致面板还停留在上一个元素上
			po.vueUnref("${pid}dataSignDetailPanelEle").hide();
			po.vueNextTick(function()
			{
				pm.dataSignDetail.label = dataSign.extLabel;
				pm.dataSignDetail.detail = (dataSign.descLabel ? (dataSign.descLabel.value || "") : "");
				
				po.vueUnref("${pid}dataSignDetailPanelEle").show(e);
			});
		},
		
		onDataSignDetailPanelShow: function(e)
		{
			var pm = po.vuePageModel();
			pm.dataSignDetailShown = true;
		},
		
		onDataSignDetailPanelHide: function(e)
		{
			var pm = po.vuePageModel();
			pm.dataSignDetailShown = false;
		},
		
		onUpdateDataSignDetailPanel: function(e, dataSign)
		{
			var pm = po.vuePageModel();
			if(pm.dataSignDetailShown)
			{
				pm.dataSignDetail.label = dataSign.extLabel;
				pm.dataSignDetail.detail = (dataSign.descLabel ? (dataSign.descLabel.value || "") : "");
			}
		},
		
		onAddDataSign: function(e, dataSign)
		{
			var pm = po.vuePageModel();
			
			if(pm.dataSignTarget == "dataset")
			{
				if(pm.dataSetBindForSign)
				{
					var signObjs = pm.dataSetBindForSign.extSignObjs;
					
					if($.inArrayById(signObjs, dataSign.extFullname, "extFullname") < 0)
						signObjs.push(dataSign);
					
					po.vueUnref("${pid}dataSignsPanelEle").hide();
				}
			}
			else if(pm.dataSignTarget == "field")
			{
				if(pm.dataSetBindForSign && pm.dataSetFieldForSign)
				{
					if(!dataSign.multiple && po.hasDataSetFieldSigned(pm.dataSetBindForSign, dataSign))
					{
						var msg = $.validator.format("<@spring.message code='chart.dataSetHasFieldSign' />",
								pm.dataSetBindForSign.dataSet.name, dataSign.extLabel);
						
						$.tipWarn(msg);
						return;
					}
					
					var signObjs = pm.dataSetFieldForSign.extDsbInfo.signObjs;
					
					if($.inArrayById(signObjs, dataSign.extFullname, "extFullname") < 0)
						signObjs.push(dataSign);
					
					po.vueUnref("${pid}dataSignsPanelEle").hide();
				}
			}
		},

		onRemoveDataSetDataSign: function(dataSetBind, dataSigName)
		{
			var signObjs = dataSetBind.extSignObjs;
			$.removeById(signObjs, dataSigName, "extFullname");
		},
		
		onRemoveFieldDataSign: function(dataSetField, dataSigName)
		{
			var signObjs = dataSetField.extDsbInfo.signObjs;
			$.removeById(signObjs, dataSigName, "extFullname");
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
		},
		
		onShowParamPanel: function(e, dataSetBind)
		{
			po._currentDataSetBindForParam = dataSetBind;
			po.vueUnref("${pid}paramPanelEle").toggle(e);
		},
		
		onParamPanelShow: function(e)
		{
			if(po._currentDataSetBindForParam)
				po.inflateParamPanel(po._currentDataSetBindForParam);
		},
		
		onParamPanelHide: function(e)
		{
			var wrapper = $(".paramvalue-form-wrapper", po.elementOfId("${pid}paramPanel", document.body));
			chartFactory.chartSetting.destroyDataSetParamValueForm(wrapper);
		},
		
		onClearParamValues: function(e, dataSetBind)
		{
			dataSetBind.query.paramValues = {};
		},
		
		onShowDataFormatPanel: function(e)
		{
			po.vueUnref("${pid}dataFormatPanelEle").toggle(e);
		},
		
		onShowChartPluginDesc: function(e)
		{
			po.vueUnref("${pid}pluginVoDescEle").toggle(e);
		},
		
		onShowAttrValuesPanel: function(e)
		{
			var pm = po.vuePageModel();
			pm.attrValuesPanelShown = true;
		},
		
		onAttrValuesPanelShow: function()
		{
			var fm = po.vueFormModel();
			var pm = po.vuePageModel();
			var chartPluginAttrs = po.vueRaw(fm.pluginVo ? (fm.pluginVo.attributes || []) : []);
			var attrValues = po.vueRaw(fm.attrValues);
			po.setupChartAttrValuesForm(chartPluginAttrs, attrValues,
			{
				submitHandler: function(avs)
				{
					fm.attrValues = avs;
					pm.attrValuesPanelShown = false;
				},
				readonly: pm.isReadonlyAction
			});
		},

		onShowOptionsPanel: function(e)
		{
			po.vueUnref("${pid}optionsPanelEle").toggle(e);
		},
		
		onOptionsPanelShow: function()
		{
			var fm = po.vueFormModel();
			var pm = po.vuePageModel();
			var options = po.vueRaw(fm.options);
			
			var form = po.elementOfId("${pid}optionsForm", document.body);
			var codeEditorEle = po.elementOfId("${pid}optionsContentCodeEditor", form);
			
			var editorOptions =
			{
				value: "",
				matchBrackets: true,
				autoCloseBrackets: true,
				mode: {name: "javascript", json: true}
			};
			
			codeEditorEle.empty();
			var codeEditor = po.createCodeEditor(codeEditorEle, editorOptions);
			po.setCodeTextTimeout(codeEditor, (options || ""), true);
			
			po.setupSimpleForm(form, pm.optionsFormModel, function()
			{
				pm.optionsFormModel.options = po.getCodeText(codeEditor);
				fm.options = pm.optionsFormModel.options;
				po.vueUnref("${pid}optionsPanelEle").hide();
			});
		},
		
		onSaveAndShow: function(e)
		{
			try
			{
				po.inSaveAndShowAction(true);
				po.form().submit();
			}
			finally
			{
				po.inSaveAndShowAction(false);
			}
		}
	});
	
	po.setupPalette();
})
(${pid});
</script>
<#include "../include/page_vue_mount.ftl">
</body>
</html>
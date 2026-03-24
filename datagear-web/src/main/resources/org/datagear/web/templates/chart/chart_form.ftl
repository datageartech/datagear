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
<#assign DataSign=statics['org.datagear.analysis.DataSign']>
<#assign DashboardApiVersion=statics['org.datagear.analysis.support.html.DashboardApiVersion']>
<#assign FieldDataType=statics['org.datagear.analysis.DataSetField$DataType']>
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
<body class="p-card no-border h-screen m-0 p-1">
<#include "../include/page_obj.ftl">
<div id="${pid}" class="page page-form h-full page-form-chart">
	<form id="${pid}form" class="flex flex-column h-full" :class="{readonly: pm.isReadonlyAction}">
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
		        		<input name="pluginVo.id" required type="text" class="validate-proxy" />
		        	</div>
				</div>
			</div>
			<div class="field grid align-items-start">
				<label for="${pid}dataSetBindVOs" class="field-label col-12 mb-2 md:col-3 md:mb-0"
					title="<@spring.message code='chart.dsb.desc' />">
					<@spring.message code='dataSetBind' />
				</label>
				<div class="field-input col-12 md:col-9">
					<div id="${pid}dataSetBindVOs" class="chart-datasets input p-component p-inputtext w-full overflow-auto p-2">
						<p-panel v-for="(dsb, dsbIdx) in fm.dataSetBindVOs" :key="dsbIdx" :toggleable="true" class="mb-2">
							<template #header>
								<label>
									<span class="font-bold">{{dsb.dataSet.name}}</span>
									<span class="text-color-secondary text-sm ml-1">
										{{(dsbIdx+1)+'/'+fm.dataSetBindVOs.length}}
									</span>
								</label>
							</template>
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
								<div class="field grid mb-2" v-if="pm.pluginHasDataSetSign">
									<label class="field-label col-12 mb-2 md:col-3 md:mb-0"
										title="<@spring.message code='chart.dsb.dataSetSign.desc' />">
										<@spring.message code='dataSetSign' />
									</label>
									<div class="field-input col-12 md:col-9">
										<div class="p-inputgroup">
											<div class="input p-component p-inputtext border-round-left overflow-auto flex flex-wrap align-items-start gap-1" style="height:4rem;">
												<p-chip v-for="sign in dsb.bindDataSigns" :key="sign.fullname" :label="sign.extLabel" class="text-sm"
													:removable="!pm.isReadonlyAction" @remove="onRemoveDataSetSign(dsb, sign.fullname)">
												</p-chip>
											</div>
											<p-button type="button" icon="pi pi-plus"
												aria:haspopup="true" aria-controls="${pid}dataSignsPanel"
												@click="onShowDataSignPanel($event, dsb)" v-if="!pm.isReadonlyAction">
											</p-button>
										</div>
									</div>
								</div>
								<div class="field grid mb-3">
									<label class="field-label col-12 mb-2" title="<@spring.message code='chart.dsb.fieldSign.desc' />">
										<@spring.message code='fieldSign' />
									</label>
									<div class="field-input col-12">
										<p-treetable :value="dsb.fieldNodes" :scrollable="true"
											:resizable-columns="true" column-resize-mode="expand"
											selection-mode="multiple" :meta-key-selection="true" data-key="key" class="table-sm p-component p-inputtext">
											<p-column header="<@spring.message code='name' />" expander>
												<template #body="{node}">
													<div class="flex gap-1">
														<div>{{node.data.name}}</div>
														<div class="flex opacity-70" style="gap:1px;">
															<p-badge :value="formatDataSetFieldType(node.data.type)" severity="secondary" class="p-badge-secondary text-xs font-normal"></p-badge>
															<p-badge value="<@spring.message code='array' />" severity="secondary" class="p-badge-warning text-xs font-normal"
																v-if="node.data.array">
															</p-badge>
														</div>
													</div>
												</template>
											</p-column>
											<p-column header="<@spring.message code='dataSign' />" class="col-20">
												<template #body="{node}">
													<div class="p-inputgroup">
														<div class="input p-component p-inputtext border-round-left overflow-auto flex flex-wrap align-items-start gap-1" style="height:4rem;">
															<p-chip v-for="sign in node.bindDataSigns" :key="sign.fullname" :label="sign.extLabel" class="text-sm"
																:removable="!pm.isReadonlyAction" @remove="onRemoveDataSetFieldSign(node, sign.fullname)">
															</p-chip>
														</div>
														<p-button type="button" icon="pi pi-plus"
															aria:haspopup="true" aria-controls="${pid}dataSignsPanel"
															@click="onShowDataSignPanel($event, dsb, node)" v-if="!pm.isReadonlyAction">
														</p-button>
													</div>
												</template>
											</p-column>
											<p-column header="<@spring.message code='more' />" style="flex:0 0 6rem;">
												<template #body="{node}">
													<p-button type="button" icon="pi pi-ellipsis-h" severity="secondary" text rounded
														aria:haspopup="true" aria-controls="${pid}fieldMorePanelEle"
														@click="onShowFieldMorePanel($event, node)" v-if="!pm.isReadonlyAction">
													</p-button>
												</template>
											</p-column>
										</p-treetable>
									</div>
								</div>
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
									<div class="field-input col-12 md:col-9 flex align-items-center gap-1">
										<p-button type="button" :label="pm.isReadonlyAction ? '<@spring.message code='view' />' : '<@spring.message code='edit' />'"
											aria:haspopup="true" aria-controls="${pid}paramPanel"
											@click="onShowParamPanel($event, dsb)" class="p-button-secondary">
										</p-button>
										<p-button type="button" label="<@spring.message code='clear' />"
											@click="onClearParamValues($event, dsb)" class="p-button-secondary p-button-danger"
											:disabled="dsbParamValuesCount(dsb) == 0" v-if="!pm.isReadonlyAction">
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
		        		<input name="dataSetSignCheckVal" type="text" class="validate-normalizer" />
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
							:disabled="isEmptyPluginAttributeForm(fm.pluginVo)"
							@click="onShowAttrValuesPanel" class="p-button-secondary mr-2">
						</p-button>
			        	<div class="desc text-color-secondary text-sm" v-if="fm.pluginVo && isEmptyPluginAttributeForm(fm.pluginVo)">
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
			<div class="field grid">
				<label for="${pid}description" class="field-label col-12 mb-2 md:col-3 md:mb-0">
					<@spring.message code='description' />
				</label>
				<div class="field-input col-12 md:col-9">
					<p-textarea id="${pid}description" v-model="fm.description" rows="4" class="input w-full"
			       		name="description" maxlength="500">
			       	</p-textarea>
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
				{{pm.candidateDataSignTarget == "dataset" ? "<@spring.message code='dataSetSign' />" : "<@spring.message code='fieldSign' />"}}
			</label>
		</div>
		<div class="panel-content-size-xs-mwh overflow-auto p-2">
			<div class="flex flex-column gap-1" v-if="pm.candidateDataSignInfos.length > 0">
				<div v-for="dsInfo in pm.candidateDataSignInfos" :key="dsInfo.dataSign.fullname">
					<div class="flex align-items-center gap-1">
						<div>
							<div class="p-inputgroup">
								<p-button type="button" :label="dsInfo.dataSign.extLabel" icon="pi pi-plus" :disabled="!dsInfo.matches"
									@click="onAddDataSign($event, dsInfo.dataSign)" class="white-space-nowrap">
								</p-button>
								<p-button type="button" icon="pi pi-info-circle"
									aria:haspopup="true" aria-controls="${pid}dataSignDetailPanel"
									@click="onShowDataSignDetail($event, dsInfo.dataSign)">
								</p-button>
							</div>
						</div>
						<div class="flex align-items-center" style="gap:1px;">
							<p-badge severity="info" class="font-normal white-space-nowrap">
								{{dsInfo.dataSign.multiple ? "<@spring.message code='multipleSelect' />" : "<@spring.message code='singleSelect' />"}}
							</p-badge>
							<p-badge :severity="dsInfo.dataSign.required ? 'danger' : 'info'" class="font-normal white-space-nowrap">
								{{dsInfo.dataSign.required ? "<@spring.message code='requiredInput' />" : "<@spring.message code='optionalInput' />"}}
							</p-badge>
						</div>
					</div>
				</div>
			</div>
			<div class="flex flex-column gap-2" v-if="pm.candidateDataSignInfos.length == 0">
				<div class="flex align-items-center gap-1">
					<i class="pi pi-info-circle"></i>
					<span><@spring.message code='chart.noAvaliableDataSign' /></span>
				</div>
				<div class="text-color-secondary text-sm">
					<span v-if="fm.pluginVo == null">
						<@spring.message code='chart.noAvaliableDataSign.desc1' />
					</span>
					<span v-else>
						<@spring.message code='chart.noAvaliableDataSign.desc2' />
					</span>
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
	<p-dialog header="<@spring.message code='chartAttribute' />" append-to="body" position="center" :modal="true"
		v-model:visible="pm.attrValuesPanelShown" id="${pid}attrValuesPanel" @show="onAttrValuesPanelShow">
		<div class="page page-form page-chart-attr-values chart-form-chart-attr-values">
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
							<div id="${pid}optionsContent" class="code-editor-wrapper input p-component p-inputtext size-40vw size-40vh">
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
	<p-overlaypanel ref="${pid}fieldMorePanelEle" append-to="body" id="${pid}fieldMorePanel" @show="onFieldMorePanelShow">
		<div class="pb-2">
			<label class="text-lg font-bold">
				<@spring.message code='more' />
			</label>
		</div>
		<div class="page page-form">
			<form id="${pid}fielMoreForm" class="flex flex-column" :class="{readonly: pm.isReadonlyAction}">
				<div class="page-form-content flex-grow-1 px-2 py-1 overflow-y-auto">
					<div class="field grid mb-2">
						<label for="${pid}fielMoreAlias" class="field-label col-12"
							title="<@spring.message code='chart.dsb.fieldAlias.desc' />">
							<@spring.message code='alias' />
						</label>
						<div class="field-input col-12">
							<p-inputtext id="${pid}fielMoreAlias" v-model="pm.dataSetFieldNodeForSign.alias" type="text"
								class="input w-full" maxlength="50"
								:placeholder="pm.dataSetFieldNodeForSign.data.label ? pm.dataSetFieldNodeForSign.data.label : pm.dataSetFieldNodeForSign.data.name">
							</p-inputtext>
						</div>
					</div>
					<div class="field grid mb-2">
						<label for="${pid}fielMoreOrder" class="field-label col-12"
							title="<@spring.message code='chart.dsb.fieldOrder.desc' />">
							<@spring.message code='sort' />
						</label>
						<div class="field-input col-12">
							<p-inputtext id="${pid}fielMoreOrder" v-model="pm.dataSetFieldNodeForSign.order" type="text" class="input w-full"
								maxlength="50" integer="true">
							</p-inputtext>
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
	po.formModel = $.unescapeHtmlForJson(<@writeJson var=formModel />);
	po.initResultDataFormat = $.unescapeHtmlForJson(<@writeJson var=initResultDataFormat />);
	po.enableResultDataFormat = ("${enableResultDataFormat?string('true', 'false')}" == "true");
	po.dateOrTimeTypeOptions =
	[
		{ name: "<@spring.message code='string' />", value: "${ResultDataFormat.TYPE_STRING}" },
		{ name: "<@spring.message code='number' />", value: "${ResultDataFormat.TYPE_NUMBER}" }
	];
	po.bakPluginAttrValuesMap = {};
	po.DS_TARGET_FIELD = "${DataSign.TARGET_FIELD}";
	po.DS_TARGET_DATASET = "${DataSign.TARGET_DATASET}";
	po.DashboardApiVersion = { LATEST_VERSION: "${DashboardApiVersion.LATEST_VERSION}" };
	po.DataSetFieldTypes =
	[
		{name: "<@spring.message code='dataSetField.DataType.STRING' />", value: "${FieldDataType.STRING}"},
		{name: "<@spring.message code='dataSetField.DataType.NUMBER' />", value: "${FieldDataType.NUMBER}"},
		{name: "<@spring.message code='dataSetField.DataType.INTEGER' />", value: "${FieldDataType.INTEGER}"},
		{name: "<@spring.message code='dataSetField.DataType.DATE' />", value: "${FieldDataType.DATE}"},
		{name: "<@spring.message code='dataSetField.DataType.TIME' />", value: "${FieldDataType.TIME}"},
		{name: "<@spring.message code='dataSetField.DataType.TIMESTAMP' />", value: "${FieldDataType.TIMESTAMP}"},
		{name: "<@spring.message code='dataSetField.DataType.BOOLEAN' />", value: "${FieldDataType.BOOLEAN}"},
		{name: "<@spring.message code='dataSetField.DataType.OBJECT' />", value: "${FieldDataType.OBJECT}"},
		{name: "<@spring.message code='dataSetField.DataType.UNKNOWN' />", value: "${FieldDataType.UNKNOWN}"}
	];
	po.updateIntervalTypeOptions =
	[
		{ name: "<@spring.message code='noUpdate' />", value: "none" },
		{ name: "<@spring.message code='interval' />", value: "interval" }
	],
	
	po.i18n.yes = "<@spring.message code='yes' />";
	po.i18n.no = "<@spring.message code='no' />";
	po.i18n["chart.confirmDelThisDsb"] = "<@spring.message code='chart.confirmDelThisDsb' />";
	po.i18n["chart.dataSetSign.required"] = "<@spring.message code='chart.dataSetSign.required' />";
	po.i18n["chart.fieldSign.required"] = "<@spring.message code='chart.fieldSign.required' />";
	po.i18n.noLimit = "<@spring.message code='noLimit' />";
	po.i18n["chart.validateDataSetRange.main"] = "<@spring.message code='chart.validateDataSetRange.main' />";
	po.i18n["chart.validateDataSetRange.attachment"] = "<@spring.message code='chart.validateDataSetRange.attachment' />";
	po.i18n["chart.attrValues.editRequired"] = "<@spring.message code='chart.attrValues.editRequired' />";
	po.i18n["chartPlugin.apiVersion.desc"] = "<@spring.message code='chartPlugin.apiVersion.desc' />";
	po.i18n["chartPlugin.platformVersion.desc"] = "<@spring.message code='chartPlugin.platformVersion.desc' />";
	po.i18n.emptyDesc = "<@spring.message code='emptyDesc' />";
	po.i18n.dataSetOfColon = "<@spring.message code='dataSetOfColon' />";
	po.i18n["chart.plugin.apiVersion.deprecated"] = "<@spring.message code='chart.plugin.apiVersion.deprecated' />";
	po.i18n["chart.dataSetWithSignExist"] = "<@spring.message code='chart.dataSetWithSignExist' />";
	po.i18n["chart.fieldWithSignExist"] = "<@spring.message code='chart.fieldWithSignExist' />";
	
	$.inflateChartForm(po);
})
(${pid});
</script>
<#include "../include/page_vue_mount.ftl">
</body>
</html>
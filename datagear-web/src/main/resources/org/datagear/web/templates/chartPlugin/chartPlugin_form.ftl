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
<#assign DataSign=statics['org.datagear.analysis.DataSign']>
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
<body class="p-card no-border h-screen m-0 p-1">
<#include "../include/page_obj.ftl">
<div id="${pid}" class="page page-form h-full">
	<form id="${pid}form" class="flex flex-column h-full" :class="{readonly: pm.isReadonlyAction}">
		<div class="page-form-content full-height flex-grow-1 px-2 py-1 overflow-y-auto">
			<p-tabview class="xs-tabview" @tab-change="onTabViewChange">
				<p-tabpanel header="<@spring.message code='basicInfo' />">
					<div class="py-2">
						<div class="field grid">
							<label for="${pid}id" class="field-label col-12 mb-2 md:col-3 md:mb-0">
								<@spring.message code='id' />
							</label>
					        <div class="field-input col-12 md:col-9">
					        	<p-inputtext id="${pid}id" v-model="fm.id" type="text" class="input w-full"
					        		name="version" required maxlength="100">
					        	</p-inputtext>
					        </div>
						</div>
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
					        		name="version" maxlength="100">
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
							<label for="${pid}apiVersion" class="field-label col-12 mb-2 md:col-3 md:mb-0"
								title="<@spring.message code='chartPlugin.apiVersion.desc' />">
								<@spring.message code='apiVersion' />
							</label>
					        <div class="field-input col-12 md:col-9">
					        	<p-inputtext id="${pid}apiVersion" v-model="fm.apiVersion" type="text" class="input w-full"
					        		name="apiVersion" maxlength="100">
					        	</p-inputtext>
					        </div>
						</div>
						<div class="field grid">
							<label for="${pid}platformVersion" class="field-label col-12 mb-2 md:col-3 md:mb-0"
								title="<@spring.message code='chartPlugin.platformVersion.desc' />">
								<@spring.message code='platformVersion' />
							</label>
					        <div class="field-input col-12 md:col-9">
					        	<p-inputtext id="${pid}platformVersion" v-model="fm.platformVersion" type="text" class="input w-full"
					        		name="platformVersion" maxlength="100">
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
				</p-tabpanel>
				<p-tabpanel header="<@spring.message code='dataSign' />">
					<div class="py-2" v-if="pm.dataSignTreeNodes && pm.dataSignTreeNodes.length > 0">
						<p-treetable :value="pm.dataSignTreeNodes" :scrollable="true"
							:resizable-columns="true" column-resize-mode="expand"
							data-key="fullname" class="table-sm p-component p-inputtext">
							<p-column field="name" header="<@spring.message code='name' />" expander>
							</p-column>
							<p-column header="<@spring.message code='label' />">
								<template #body="{node}">
									{{node.data.nameLabel && node.data.nameLabel.value ? node.data.nameLabel.value : ''}}
								</template>
							</p-column>
							<p-column field="fullname" header="<@spring.message code='fullname' />">
							</p-column>
							<p-column header="<@spring.message code='target' />">
								<template #body="{node}">
									<div class="flex align-items-center" style="gap:1px;" v-if="node.data.targets">
										<p-badge severity="info" class="font-normal white-space-nowrap"
											v-for="(tgt, tgtIdx) in node.data.targets" :key="tgtIdx">
											<span v-if="tgt == '${DataSign.TARGET_FIELD}'">
												<@spring.message code='field' />
											</span>
											<span v-else-if="tgt == '${DataSign.TARGET_DATASET}'">
												<@spring.message code='dataSet' />
											</span>
										</p-badge>
									</div>
								</template>
							</p-column>
							<p-column header="<@spring.message code='requiredInput' />">
								<template #body="{node}">
									<p-badge :severity="node.data.required ? 'danger' : 'info'" class="font-normal white-space-nowrap">
										{{node.data.required ? "<@spring.message code='requiredInput' />" : "<@spring.message code='optionalInput' />"}}
									</p-badge>
								</template>
							</p-column>
							<p-column header="<@spring.message code='multipleSelect' />">
								<template #body="{node}">
									<p-badge severity="info" class="font-normal white-space-nowrap">
										{{node.data.multiple ? "<@spring.message code='multipleSelect' />" : "<@spring.message code='singleSelect' />"}}
									</p-badge>
								</template>
							</p-column>
							<p-column header="<@spring.message code='desc' />">
								<template #body="{node}">
									<p-button type="button" icon="pi pi-info-circle" severity="info" text rounded
										aria:haspopup="true" aria-controls="${pid}dataSignDetailPanel"
										:disabled="!node.data.descLabel || !node.data.descLabel.value"
										@click="onShowDataSignDetail($event, node.data)">
									</p-button>
								</template>
							</p-column>
						</p-treetable>
					</div>
					<div class="py-2" v-else>
						<@spring.message code='none' />
					</div>
				</p-tabpanel>
				<p-tabpanel header="<@spring.message code='useManual' />">
					<div class="py-2" v-if="pm.manualHtml">
						<div class="flex justify-content-end relative">
							<p-button type="button" icon="pi pi-external-link" size="small" rounded text severity="secondary"
								@click="onOpenManualInNewWindow" class="p-1 absolute" style="top:-0.3rem;">
							</p-button>
						</div>
						<div v-html="pm.manualHtml" class="chart-plugin-manual"></div>
					</div>
					<div class="py-2" v-else>
						<@spring.message code='none' />
					</div>
				</p-tabpanel>
			</p-tabview>
		</div>
		<div class="page-form-foot flex-grow-0 flex justify-content-center gap-2 pt-2">
			<p-button type="submit" label="<@spring.message code='save' />"></p-button>
		</div>
	</form>
	<p-overlaypanel ref="${pid}dataSignDetailPanelEle" :show-close-icon="true" append-to="body" id="${pid}dataSignDetailPanel"
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
				data = (data ? data : "");
				//不允许任何HTML标签，避免安全风险
				data = $.escapeHtmlTag(data);
				pm.manualHtml = marked.parse(data);
			}
		});
	};
	
	po.toDataSignTreeNodes = function(dataSigns)
	{
		if(dataSigns == null)
			return dataSigns;

		var re = [];
		
		for(var i=0; i<dataSigns.length; i++)
		{
			var dataSign = dataSigns[i];
			var children = dataSign.children;
			
			re[i] = { key: dataSign.fullname, data: dataSign, leaf: (children == null || children.length == 0) };
			re[i].children = po.toDataSignTreeNodes(children);
		}
		
		return re;
	};
	
	var formModel = $.unescapeHtmlForJson(<@writeJson var=formModel />);
	formModel.nameLabel = (formModel.nameLabel ? formModel.nameLabel : {});
	formModel.descLabel = (formModel.descLabel ? formModel.descLabel : {});
	
	po.setupForm(formModel);
	
	po.vuePageModel(
	{
		dataSignTreeNodes: po.toDataSignTreeNodes(formModel.dataSigns),
		manualHtml: null,
		dataSignDetail: {}
	});
	
	po.vueMethod(
	{
		formatChartPlugin: function(chartPlugin)
		{
			return $.toChartPluginHtml(chartPlugin, po.contextPath);
		},
		
		openManual: function(id)
		{
			po.open("/chartPlugin/manual/" + encodeURIComponent(id), { target: "_blank" });
		},
		
		onTabViewChange: function(e)
		{
			var pm = po.vuePageModel();
			var fm = po.vueFormModel();
			
			if(e.index == 2)
			{
				if(pm.manualHtml == null)
					po.loadManual(fm.id);
			}
		},
		
		onOpenManualInNewWindow: function(e)
		{
			var fm = po.vueFormModel();
			po.open("/chartPlugin/manual/" + encodeURIComponent(fm.id), { target: "_blank" });
		},

		onShowDataSignDetail: function(e, dataSign)
		{
			var pm = po.vuePageModel();
			
			//直接show会导致面板还停留在上一个元素上
			po.vueUnref(po.concatPid("dataSignDetailPanelEle")).hide();
			po.vueNextTick(function()
			{
				pm.dataSignDetail.label = (dataSign.nameLabel && dataSign.nameLabel.value ? dataSign.nameLabel.value : dataSign.name);
				pm.dataSignDetail.detail = (dataSign.descLabel ? (dataSign.descLabel.value || "") : "");
				
				po.vueUnref(po.concatPid("dataSignDetailPanelEle")).show(e);
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
		}
	});
	
	po.vueRef(po.concatPid("dataSignDetailPanelEle"), null);
})
(${pid});
</script>
<#include "../include/page_vue_mount.ftl">
</body>
</html>
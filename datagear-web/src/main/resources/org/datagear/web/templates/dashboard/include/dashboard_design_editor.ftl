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
看板资源编辑器

依赖：

-->
<#assign AbstractController=statics['org.datagear.web.controller.AbstractController']>
<p-tabview v-model:active-index="pm.resContentTabs.activeIndex"
	:scrollable="true" @tab-change="onResourceContentTabChange"
	@tab-click="onResourceContentTabClick" class="contextmenu-tabview light-tabview hide-default-title flex flex-column h-full"
	:class="{'opacity-0': pm.resContentTabs.items.length == 0}" :pt="{navContainer:{'class':'flex-grow-0'},panelContainer:{'class':'flex-grow-1'}}">
	<!--
	注意：p-tabpanel组件必须设置header属性，且值不能重复，否则，删除前置卡片会导致后后置卡片重绘。
	所以，这里将header设置为id，并使用hide-default-title样式类自定义标题
	-->
	<p-tabpanel v-for="tab in pm.resContentTabs.items" :key="tab.id" :header="tab.id" :pt="{root:{'class':'h-full'}}">
		<template #header>
			<span class="p-tabview-title custom-title">{{tab.title}}</span>
			<p-button type="button" icon="pi pi-angle-down"
				class="context-menu-btn p-button-xs p-button-secondary p-button-text p-button-rounded"
				@click="onResourceContentTabMenuToggle($event, tab)"
				aria-haspopup="true" aria-controls="${pid}resourceContentTabMenu">
			</p-button>
		</template>
		<div :id="tab.id" class="flex flex-column h-full">
			<div class="flex-grow-0 flex align-items-center justify-content-between">
				<div>
					<p-selectbutton v-model="tab.editMode" :options="pm.templateEditModeOptions"
						option-label="name" option-value="value" class="text-sm" @change="onChangeEditMode($event, tab)"
						v-if="tab.isTemplate">
					</p-selectbutton>
				</div>
				<div class="flex" v-if="!pm.isReadonlyAction && tab.editMode == 'code'">
					<div class="p-buttonset flex align-items-stretch">
						<p-button icon="pi pi-list" label="<@spring.message code='selectChart' />" severity="secondary" class="p-button-sm for-open-chart-panel" title="<@spring.message code='dashboard.insertChart.select' />"
							@click="onInsertCodeEditorChart($event, tab, false)" v-if="tab.isTemplate">
						</p-button>
						<p-button icon="pi pi-plus" label="<@spring.message code='createChart' />" severity="secondary" class="p-button-sm" title="<@spring.message code='dashboard.insertChart.create' />"
							@click="onInsertCodeEditorChart($event, tab, true)" v-if="tab.isTemplate && pm.enableInsertNewChart">
						</p-button>
					</div>
					<p-menubar :model="pm.codeEditMenuItems" class="ve-menubar light-menubar no-root-icon-menubar border-none pl-2 text-sm z-99">
						<template #end>
							<div class="p-inputgroup pl-2">
								<p-inputtext type="text" v-model="tab.searchCodeKeyword" class="text-sm p-0 px-1" style="width:9rem;" @keydown.enter.prevent="onSearchInCodeEditor($event, tab)"></p-inputtext>
								<p-button type="button" icon="pi pi-search" class="p-button-secondary p-button-sm" @click="onSearchInCodeEditor($event, tab)"></p-button>
							</div>
						</template>
					</p-menubar>
				</div>
				<div class="flex" v-if="!pm.isReadonlyAction && tab.editMode == 'visual'" v-if="tab.isTemplate">
					<p-button label="<@spring.message code='quickExecute' />" @click="pm.onQuickExecute($event, tab)"
						class="p-button-sm" :disabled="pm.quickExecuteMenuItem == null" v-tooltip.top="pm.quickExecuteTooltip">
					</p-button>
					<p-menubar :model="pm.tplVisualEditMenuItems" class="ve-menubar light-menubar no-root-icon-menubar border-none pl-2 text-sm z-99">
					</p-menubar>
				</div>
			</div>
			<div class="flex-grow-1 pt-1 relative">
				<div class="code-editor-wrapper res-editor-wrapper p-component p-inputtext p-0 w-full h-full absolute">
					<div :id="resCodeEditorEleId(tab)" class="code-editor"></div>
				</div>
				<div class="visual-editor-wrapper res-editor-wrapper opacity-hide p-component p-inputtext p-0 w-full h-full absolute">
					<div class="visual-editor-ele-path-wrapper text-color-secondary text-sm">
						<div class="ele-path white-space-nowrap">
							<span v-for="(ep, epIdx) in tab.veElementPath" :key="epIdx">
								<span class="info-separator p-1 opacity-50" v-if="epIdx > 0">&gt;</span>
								<span class="ele-info cursor-pointer" :title="ep.displayName"
									@click="onVeSelectByElePath($event, ep)">
									{{formatVeElePathDisplayName(ep)}}
								</span>
							</span>
						</div>
					</div>
					<div class="visual-editor-iframe-wrapper">
						<iframe class="visual-editor-iframe shadow-4 border-none" :id="resVisualEditorEleId(tab)"
							:name="resVisualEditorEleId(tab)" @load="onVisualEditorIframeLoad($event, tab)">
						</iframe>
					</div>
				</div>
			</div>
		</div>
	</p-tabpanel>
</p-tabview>
<p-menu id="${pid}resourceContentTabMenu" ref="${pid}resourceContentTabMenuEle"
	:model="pm.resContentTabMenuItems" :popup="true" class="text-sm">
</p-menu>
<script>
(function(po)
{
	po.currentUserId = "${currentUser.id!}";
	po.defaultTemplateName = "${defaultTempalteName}";
	po.enableInsertNewChart = ("${(enableInsertNewChart!true)?string('true', 'false')}"  == "true");
	po.currentAnalysisProjectIdParam = "${AbstractController.KEY_ANALYSIS_PROJECT_ID}";
	po.i18n = (po.i18n || (po.i18n = {}));
	
	po.i18n.insertInsideChartOnChartEleDenied="<@spring.message code='dashboard.opt.tip.insertInsideChartOnChartEleDenied' />";
	po.i18n.selectElementForSetChart="<@spring.message code='dashboard.opt.tip.selectElementForSetChart' />";
	po.i18n.canEditOnlyTextElement="<@spring.message code='dashboard.opt.tip.canOnlyEditTextElement' />";
	po.i18n.selectedElementRequired="<@spring.message code='dashboard.opt.tip.selectedElementRequired' />";
	po.i18n.selectedNotChartElement="<@spring.message code='dashboard.opt.tip.selectedNotChartElement' />";
	po.i18n.selectedNotHasChartElement="<@spring.message code='dashboard.opt.tip.selectedNotHasChartElement' />";
	po.i18n.noSelectableNextElement="<@spring.message code='dashboard.opt.tip.noSelectableNextElement' />";
	po.i18n.noSelectablePrevElement="<@spring.message code='dashboard.opt.tip.noSelectablePrevElement' />";
	po.i18n.noSelectableChildElement="<@spring.message code='dashboard.opt.tip.noSelectableChildElement' />";
	po.i18n.noSelectableParentElement="<@spring.message code='dashboard.opt.tip.noSelectableParentElement' />";
	po.i18n.imgEleRequired = "<@spring.message code='dashboard.opt.tip.imgEleRequired' />";
	po.i18n.hyperlinkEleRequired = "<@spring.message code='dashboard.opt.tip.hyperlinkEleRequired' />";
	po.i18n.videoEleRequired = "<@spring.message code='dashboard.opt.tip.videoEleRequired' />";
	po.i18n.iframeEleRequired = "<@spring.message code='dashboard.opt.tip.iframeEleRequired' />";
	po.i18n.labelEleRequired = "<@spring.message code='dashboard.opt.tip.labelEleRequired' />";
	po.i18n.chartPluginNoAttrDefined = "<@spring.message code='dashboard.opt.tip.chartPluginNoAttrDefined' />";
	po.i18n.bindChartElementMustBeDiv = "<@spring.message code='dashboard.opt.tip.bindChartElementMustBeDiv' />";
	po.i18n.chart = "<@spring.message code='chart' />";
	po.i18n.select = "<@spring.message code='select' />";
	po.i18n.insertNoPermissionChart = "<@spring.message code='dashboard.insertNoPermissionChart' />";
	po.i18n["dashboard.opt.edit.eleAttr.eleRequired"] = "<@spring.message code='dashboard.opt.edit.eleAttr.eleRequired' />";
	po.i18n.chartTipSelect = "<@spring.message code='chartTipSelect' />";
	po.i18n.chartTipCreate = "<@spring.message code='chartTipCreate' />";
	po.i18n.gridLayout = "<@spring.message code='gridLayout' />";
	po.i18n.flexLayout = "<@spring.message code='flexLayout' />";
	po.i18n.divElement = "<@spring.message code='divElement' />";
	po.i18n.titleElement = "<@spring.message code='titleElement' />";
	po.i18n.textElement = "<@spring.message code='textElement' />";
	po.i18n.image = "<@spring.message code='image' />";
	po.i18n.hyperlink = "<@spring.message code='hyperlink' />";
	po.i18n.video = "<@spring.message code='video' />";
	po.i18n.iframe = "<@spring.message code='iframe' />";
	po.i18n["dashboard.templateEditMode.code"] = "<@spring.message code='dashboard.templateEditMode.code' />";
	po.i18n["dashboard.templateEditMode.visual"] = "<@spring.message code='dashboard.templateEditMode.visual' />";
	po.i18n.close = "<@spring.message code='close' />";
	po.i18n.closeOther = "<@spring.message code='closeOther' />";
	po.i18n.closeRight = "<@spring.message code='closeRight' />";
	po.i18n.closeLeft = "<@spring.message code='closeLeft' />";
	po.i18n.closeAll = "<@spring.message code='closeAll' />";
	po.i18n.save = "<@spring.message code='save' />";
	po.i18n.nextElement = "<@spring.message code='nextElement' />";
	po.i18n.prevElement = "<@spring.message code='prevElement' />";
	po.i18n.subElement = "<@spring.message code='subElement' />";
	po.i18n.parentElement = "<@spring.message code='parentElement' />";
	po.i18n.cancelSelect = "<@spring.message code='cancelSelect' />";
	po.i18n.insert = "<@spring.message code='insert' />";
	po.i18n.bindOrReplaceChartTipSelect = "<@spring.message code='bindOrReplaceChartTipSelect' />";
	po.i18n.bindOrReplaceChartTipCreate = "<@spring.message code='bindOrReplaceChartTipCreate' />";
	po.i18n.outerInsertAfter = "<@spring.message code='outerInsertAfter' />";
	po.i18n.outerInsertBefore = "<@spring.message code='outerInsertBefore' />";
	po.i18n.innerInsertAfter = "<@spring.message code='innerInsertAfter' />";
	po.i18n.innerInsertBefore = "<@spring.message code='innerInsertBefore' />";
	po.i18n.edit = "<@spring.message code='edit' />";
	po.i18n.globalStyle = "<@spring.message code='globalStyle' />";
	po.i18n.globalChartTheme = "<@spring.message code='globalChartTheme' />";
	po.i18n.globalChartOptions = "<@spring.message code='globalChartOptions' />";
	po.i18n.style = "<@spring.message code='style' />";
	po.i18n.chartTheme = "<@spring.message code='chartTheme' />";
	po.i18n.chartOptions = "<@spring.message code='chartOptions' />";
	po.i18n.chartAttribute = "<@spring.message code='chartAttribute' />";
	po.i18n.textContent = "<@spring.message code='textContent' />";
	po.i18n.elementSetting = "<@spring.message code='elementSetting' />";
	po.i18n.elementId = "<@spring.message code='elementId' />";
	po.i18n["delete"] = "<@spring.message code='delete' />";
	po.i18n.deleteElement = "<@spring.message code='deleteElement' />";
	po.i18n.unbindChart = "<@spring.message code='unbindChart' />";
	po.i18n.more = "<@spring.message code='more' />";
	po.i18n.dashboardSize = "<@spring.message code='dashboardSize' />";
	po.i18n.elementBoundary = "<@spring.message code='elementBoundary' />";
	po.i18n.refresh = "<@spring.message code='refresh' />";
	po.i18n.confirmCloseWithUnsaved= "<@spring.message code='confirmCloseWithUnsaved' />";
	
	//dashboardDesign.js
	$.inflateDashboardDesignEditor(po);
})
(${pid});
</script>
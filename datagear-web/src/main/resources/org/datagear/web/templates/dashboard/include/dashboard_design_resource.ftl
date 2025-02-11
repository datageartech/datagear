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
看板资源列表

依赖：

-->
<p-tabview class="light-tabview flex flex-column h-full" @tab-change="onResourceTabChange" :pt="{navContainer:{'class':'flex-grow-0'},panelContainer:{'class':'flex-grow-1'}}">
	<p-tabpanel :pt="{root:{'class':'h-full'}}">
		<template #header>
			<span class="p-tabview-title">
				<span><@spring.message code='localResource' /></span>
				<span class="pl-3 text-color for-open-global-res-panel" @click="onShowGlobalRes"
					title="<@spring.message code='dashboard.viewAndCopyGlobalResNameToClipboard' />" v-if="!pm.isReadonlyAction">
					<@spring.message code='globalResource' />（${dashboardGlobalResUrlPrefix}）
				</span>
			</span>
		</template>
		<div class="local-resource-list-wrapper resource-list-wrapper flex flex-column p-component p-inputtext h-full">
			<div class="flex-grow-0 text-xs">
				<div class="flex justify-content-between">
					<div>
						<div class="p-buttonset" v-if="!pm.isReadonlyAction">
							<p-button type="button" icon="pi pi-plus" class="p-button-secondary"
								aria:haspopup="true" aria-controls="${pid}addResPanelPanel"
								@click="onToggleAddResPanel" title="<@spring.message code='dashboard.addResource.desc' />">
							</p-button>
							<p-button type="button" icon="pi pi-upload" class="p-button-secondary"
								aria:haspopup="true" aria-controls="${pid}uploadResPanel"
								@click="onToggleUploadResPanel" title="<@spring.message code='dashboard.uploadResource.desc' />">
							</p-button>
							<p-button type="button" icon="pi pi-pencil" class="p-button-secondary"
								@click="onEditSelectedLocalRes"
								title="<@spring.message code='dashboard.editResource.desc' />">
							</p-button>
						</div>
					</div>
					<div class="relative">
						<div class="p-buttonset">
							<p-button type="button" icon="pi pi-copy" class="p-button-secondary"
								@click="onCopyLocalResToClipboard"
								title="<@spring.message code='dashboard.copyResourceNameToClipboard' />"
								v-if="!pm.isReadonlyAction">
							</p-button>
							<p-button type="button" icon="pi pi-external-link" class="p-button-secondary"
								@click="onOpenSelectedLocalRes"
								title="<@spring.message code='dashboard.openResource.desc' />">
							</p-button>
							<p-button type="button" icon="pi pi-ellipsis-h" class="p-button-secondary"
								 aria-haspopup="true" aria-controls="${pid}localResMenu"
								 @click="toggleLocalResMenu" v-if="!pm.isReadonlyAction">
							</p-button>
							<p-menu id="${pid}localResMenu" ref="${pid}localResMenuEle" class="text-sm"
								:model="pm.localResMenuItems" :popup="true"
								v-if="!pm.isReadonlyAction">
							</p-menu>
						</div>
						<div class="opacity-hide-absolute" style="top:0;left:0;width:1px;height:1px;">
							<p-button id="${pid}renameResBtn" type="button" class="p-button-secondary"
								@click="onToggleRenameResPanel">
								<@spring.message code='rename' />
							</p-button>
						</div>
					</div>
				</div>
			</div>
			<p-divider align="left" class="flex-grow-0 my-2 divider-z-0 text-sm"><@spring.message code='template' /></p-divider>
			<div class="flex-grow-0" style="height:6rem;">
				<p-listbox v-model="pm.localRes.selectedTemplate" :options="fm.templates"
					empty-message="<@spring.message code='none' />"
					@change="onChangeTemplateListItem" class="h-full overflow-auto border-none bg-none">
				</p-listbox>
			</div>
			<p-divider align="left" class="flex-grow-0 my-2 divider-z-0 text-sm"><@spring.message code='allResources' /></p-divider>
			<div class="flex-grow-1 overflow-auto relative">
				<p-tree :value="pm.localRes.resourceNodes"
					selection-mode="single" v-model:selection-keys="pm.localRes.selectedNodeKeys"
					@node-select="onLocalResNodeSelect"
					class="border-none white-space-nowrap overflow-auto bg-none absolute w-full">
				</p-tree>
			</div>
		</div>
	</p-tabpanel>
</p-tabview>
<script>
(function(po)
{
	po.dashboardGlobalResUrlPrefix = "${dashboardGlobalResUrlPrefix}";
	po.availableCharsetNames = $.unescapeHtmlForJson(<@writeJson var=availableCharsetNames />);
	po.zipFileNameEncodingDefault = "${zipFileNameEncodingDefault}";
	po.i18n = (po.i18n || (po.i18n = {}));
	
	po.i18n.dashboardGlobalRes = "<@spring.message code='module.dashboardGlobalRes' />";
	po.i18n.select = "<@spring.message code='select' />";
	po.i18n.illegalSaveAddResourceName = "<@spring.message code='dashboard.illegalSaveAddResourceName' />";
	po.i18n.setResAsTemplateUnsupport = "<@spring.message code='dashboard.setResAsTemplateUnsupport' />";
	po.i18n.atLeastOneTemplateRequired = "<@spring.message code='dashboard.atLeastOneTemplateRequired' />";
	po.i18n.setAsTemplate = "<@spring.message code='setAsTemplate' />";
	po.i18n.setAsHomeTemplate = "<@spring.message code='setAsHomeTemplate' />";
	po.i18n.unsetAsTemplate = "<@spring.message code='unsetAsTemplate' />";
	po.i18n.rename = "<@spring.message code='rename' />";
	po.i18n.refresh = "<@spring.message code='refresh' />";
	po.i18n["delete"] = "<@spring.message code='delete' />";
	po.i18n.editResUnsupport = "<@spring.message code='dashboard.editResUnsupport' />";
	
	//dashboardDesign.js
	$.inflateDashboardDesignResource(po);
})
(${pid});
</script>
<#--
 *
 * Copyright 2018-2023 datagear.tech
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
<p-tabview class="light-tabview" @tab-change="onResourceTabChange">
	<p-tabpanel header="<@spring.message code='localResource' />">
		<div class="local-resource-list-wrapper resource-list-wrapper flex flex-column p-component p-inputtext">
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
			<div class="flex-grow-0" style="height:5rem;">
				<p-listbox v-model="pm.localRes.selectedTemplate" :options="fm.templates"
					empty-message="<@spring.message code='none' />"
					@change="onChangeTemplateListItem" class="h-full overflow-auto border-none bg-none">
				</p-listbox>
			</div>
			<p-divider align="left" class="flex-grow-0 my-2 divider-z-0 text-sm"><@spring.message code='allResources' /></p-divider>
			<div class="flex-grow-1 overflow-auto">
				<p-tree :value="pm.localRes.resourceNodes"
					selection-mode="single" v-model:selection-keys="pm.localRes.selectedNodeKeys"
					@node-select="onLocalResNodeSelect"
					class="border-none white-space-nowrap overflow-x-auto bg-none">
				</p-tree>
			</div>
		</div>
	</p-tabpanel>
	<p-tabpanel header="<@spring.message code='globalResource' />">
		<div class="global-resource-list-wrapper resource-list-wrapper flex flex-column p-component p-inputtext">
			<div class="flex-grow-0 text-xs">
				<div class="flex justify-content-between">
					<div>
						<div class="p-inputgroup">
							<p-inputtext type="text" v-model="pm.globalRes.searchKeyword" class="text-sm p-0 px-1" style="width:5rem;"
								@keydown.enter.prevent="onSearchGlobalRes">
							</p-inputtext>
							<p-button type="button" icon="pi pi-search" class="p-button-secondary p-button-sm"
								@click="onSearchGlobalRes"></p-button>
						</div>
					</div>
					<div>
						<div class="p-buttonset">
							<p-button type="button" icon="pi pi-copy" class="p-button-secondary"
								@click="onCopyGlobalResToClipboard"
								title="<@spring.message code='dashboard.copyResourceNameToClipboard' />"
								v-if="!pm.isReadonlyAction">
							</p-button>
							<p-button type="button" icon="pi pi-external-link" class="p-button-secondary"
								@click="onOpenSelectedGlobalRes"
								title="<@spring.message code='dashboard.openResource.desc' />">
							</p-button>
						</div>
					</div>
				</div>
			</div>
			<p-divider align="left" class="flex-grow-0 my-2 divider-z-0 text-sm"><b>${dashboardGlobalResUrlPrefix}</b></p-divider>
			<div class="flex-grow-1 overflow-auto">
				<p-tree :value="pm.globalRes.resourceNodes"
					selection-mode="single" v-model:selection-keys="pm.globalRes.selectedNodeKeys"
					@node-select="onGlobalResNodeSelect"
					class="border-none white-space-nowrap overflow-x-auto bg-none">
				</p-tree>
			</div>
		</div>
	</p-tabpanel>
</p-tabview>
<script>
(function(po)
{
	po.dashboardGlobalResUrlPrefix = "${dashboardGlobalResUrlPrefix}";
	
	var availableCharsetNames = $.unescapeHtmlForJson(<@writeJson var=availableCharsetNames />);
	
	po.isResTemplate = function(name)
	{
		var fm = po.vueFormModel();
		return ($.inArray(name, fm.templates) > -1);
	};
	
	po.resNamesToTree = function(names)
	{
		var tree = $.toPathTree(names,
		{
			created: function(node)
			{
				node.key = node.fullPath;
				node.label = node.name;
			}
		});
		
		return tree;
	};
	
	po.refreshLocalRes = function()
	{
		var fm = po.vueFormModel();
		
		if(!fm.id)
			return;
		
		po.getJson("/dashboard/listResources", { id: fm.id }, function(response)
		{
			var pm = po.vuePageModel();
			pm.localRes.resourceNodes = po.resNamesToTree(response);
			pm.localRes.selectedNodeKeys = null;
			pm.localRes.selectedNode = null;
		});
	};
	
	po.refreshGlobalRes = function()
	{
		if(!po.checkPersistedDashboard())
			return;
		
		var pm = po.vuePageModel();
		
		po.ajaxJson("/dashboardGlobalRes/queryData", 
		{
			data: { keyword: pm.globalRes.searchKeyword },
			success: function(response)
			{
				var resNames = [];
				$.each(response, function(idx, gr)
				{
					resNames.push(gr.path);
				});
				
				pm.globalRes.resourceNodes = po.resNamesToTree(resNames);
				pm.globalRes.selectedNodeKeys = null;
				pm.globalRes.selectedNode = null;
			}
		});
	};
	
	po.openSelectedLocalRes = function()
	{
		var fm = po.vueFormModel();
		
		if(!fm.id)
			return;
		
		var sr = po.getSelectedLocalRes();
		if(sr)
			window.open(po.showUrl(sr), fm.id+"/"+sr);
	};

	po.openSelectedGlobalRes = function()
	{
		var gr = po.getSelectedGlobalRes();
		if(gr)
			window.open(po.showUrl(gr));
	};
	
	po.getSelectedLocalRes = function()
	{
		var pm = po.vuePageModel();
		var localRes = pm.localRes;
		
		if(localRes.selectedTemplate)
			return localRes.selectedTemplate;
		else if(localRes.selectedNodeKeys && localRes.selectedNode)
			return localRes.selectedNode.fullPath;
		else
			return null;
	};
	
	po.getSelectedGlobalRes = function()
	{
		var pm = po.vuePageModel();
		
		if(pm.globalRes.selectedNode)
			return po.dashboardGlobalResUrlPrefix + pm.globalRes.selectedNode.fullPath;
		else
			return null;
	};
	
	po.addRes = function(name)
	{
		if(!name)
			return false;
		
		if($.isDirectoryFile(name))
		{
			$.tipInfo("<@spring.message code='dashboard.illegalSaveAddResourceName' />");
			return false;
		}
		
		var isTemplate = $.isHtmlFile(name);
		po.showResContentTab(name, isTemplate);
		
		return true;
	};
	
	po.uploadRes = function(uploadModel)
	{
		if(!uploadModel.filePath)
			return false;
		
		var fm = po.vueFormModel();
		
		po.post("/dashboard/saveUploadResourceFile",
		{
			id: fm.id,
			resourceFilePath: uploadModel.filePath,
			resourceName: (uploadModel.savePath || ""),
			autoUnzip: uploadModel.autoUnzip,
			zipFileNameEncoding: uploadModel.zipFileNameEncoding
		},
		function(response)
		{
			po.vueUnref("${pid}uploadResPanelEle").hide();
			
			var pm = po.vuePageModel();
			pm.uploadResModel.savePath = "";
			pm.uploadResModel.filePath = "";
			pm.uploadResModel.autoUnzip = false;
			po.clearFileuploadInfo();
			po.refreshLocalRes();
		});
	};
	
	po.updateTemplateList = function(templates)
	{
		var fm = po.vueFormModel();
		fm.templates = templates;
	};
	
	po.setResAsTemplate = function(name)
	{
		if(!name || !po.checkPersistedDashboard())
			return;
		
		if(!$.isHtmlFile(name))
		{
	 		$.tipInfo("<@spring.message code='dashboard.setResAsTemplateUnsupport' />");
	 		return;
		}
		
		var fm = po.vueFormModel();
		var templates = po.vueRaw(fm.templates);
		
		if($.inArray(name, templates) < 0)
		{
			templates.push(name);
			po.saveTemplateNames(templates);
		}
	};
	
	po.setResAsFirstTemplate = function(name)
	{
		if(!name || !po.checkPersistedDashboard())
			return;
		
		if(!$.isHtmlFile(name))
		{
	 		$.tipInfo("<@spring.message code='dashboard.setResAsTemplateUnsupport' />");
	 		return;
		}
		
		var fm = po.vueFormModel();
		var templates = po.vueRaw(fm.templates);
		var idx = $.inArray(name, templates);
		
		if(idx == 0)
			return;
		else
		{
			if(idx > 0)
				templates.splice(idx, 1);
			
			templates.unshift(name);
			po.saveTemplateNames(templates);
		}
	};
	
	po.setTemplateAsNormalRes = function(name)
	{
		if(!name || !po.checkPersistedDashboard())
			return;
		
		var fm = po.vueFormModel();
		var templates = po.vueRaw(fm.templates);
		var idx = $.inArray(name, templates);
		
		if(idx > -1)
		{
			if(templates.length < 2)
			{
				$.tipWarn("<@spring.message code='dashboard.atLeastOneTemplateRequired' />");
				return;
			}
			
			templates.splice(idx, 1);
			po.saveTemplateNames(templates);
		}
	};
	
	po.saveTemplateNames = function(templates)
	{
		var fm = po.vueFormModel();
		
		po.ajaxJson("/dashboard/saveTemplateNames?id="+encodeURIComponent(fm.id),
		{
			data: templates,
			success: function(response)
			{
				po.updateTemplateList(response.data.templates);
			}
		});
	};
	
	po.renameRes = function(srcName, destName)
	{
		if(!srcName || !destName || srcName == destName || !po.checkPersistedDashboard())
			return;
		
		var fm = po.vueFormModel();
		
		po.post("/dashboard/renameResource", { id: fm.id, srcName: srcName, destName: destName},
		function(response)
		{
			po.updateTemplateList(response.data.templates);
			po.refreshLocalRes();
			
			if(po.updateEditorResNames)
				po.updateEditorResNames(response.data.renames);
		});
	};
	
	po.deleteRes = function(name)
	{
		if(!name || !po.checkPersistedDashboard())
			return;
		
		var fm = po.vueFormModel();
		var templates = fm.templates;
		var idx = $.inArray(name, templates);
		
		if(idx > -1 && templates.length < 2)
		{
			$.tipWarn("<@spring.message code='dashboard.atLeastOneTemplateRequired' />");
			return;
		}
		
		po.post("/dashboard/deleteResource", { id: fm.id, name: name},
		function(response)
		{
			po.updateTemplateList(response.data.templates);
			po.refreshLocalRes();
		});
	};
	
	po.setupResourceList = function()
	{
		po.vuePageModel(
		{
			availableCharsetNames: availableCharsetNames,
			localRes:
			{
				selectedTemplate: null,
				resourceNodes: null,
				selectedNodeKeys: null,
				selectedNode: null
			},
			globalRes:
			{
				resourceNodes: null,
				selectedNodeKeys: null,
				selectedNode: null,
				searchKeyword: ""
			},
			localResMenuItems:
			[
				{
					label: "设为模板",
					command: function()
					{
						var resName = po.getSelectedLocalRes();
						po.setResAsTemplate(resName);
					}
				},
				{
					label: "设为主页模板",
					command: function()
					{
						var resName = po.getSelectedLocalRes();
						po.setResAsFirstTemplate(resName);
					}
				},
				{
					label: "取消模板",
					command: function()
					{
						var resName = po.getSelectedLocalRes();
						po.setTemplateAsNormalRes(resName);
					}
				},
				{
					label: "<@spring.message code='refresh' />",
					command: function()
					{
						po.refreshLocalRes();
					}
				},
				{
					label: "<@spring.message code='rename' />",
					command: function(e)
					{
						var resName = po.getSelectedLocalRes();
						if(resName)
						{
							var pm = po.vuePageModel();
							pm.renameResModel.srcName = resName;
							pm.renameResModel.destName = resName;
							
							e.originalEvent.stopPropagation();
							po.elementOfId("${pid}renameResBtn").click();
						}
					}
				},
				{ separator: true },
				{
					label: "<@spring.message code='delete' />",
					class: "p-error",
					command: function()
					{
						var resName = po.getSelectedLocalRes();
						
						if(resName)
						{
							po.confirmDelete(function()
							{
								po.deleteRes(resName);
							});
						}
					}
				}
			],
			addResModel:
			{
				resName: null
			},
			uploadResModel:
			{
				url: po.concatContextPath("/dashboard/uploadResourceFile"),
				filePath: null,
				savePath: null,
				autoUnzip: false,
				zipFileNameEncoding: "${zipFileNameEncodingDefault}"
			},
			renameResModel:
			{
				srcName: null,
				destName: null
			}
		});
		
		po.vueMethod(
		{
			onResourceTabChange: function(e)
			{
				if(e.index == 1)
				{
					var pm = po.vuePageModel();
					
					if(!pm.globalRes.resourceNodes)
						po.refreshGlobalRes();
				}
			},
			
			onChangeTemplateListItem: function(e)
			{
				var pm = po.vuePageModel();
				pm.localRes.selectedNodeKeys = null;
				pm.localRes.selectedNode = null;
			},
			
			onLocalResNodeSelect: function(node)
			{
				var pm = po.vuePageModel();
				pm.localRes.selectedNode = node;
				pm.localRes.selectedTemplate = null;
			},
			
			onCopyLocalResToClipboard: function(e)
			{
				po.copyToClipboard(po.getSelectedLocalRes());
			},
			
			onOpenSelectedLocalRes: function(e)
			{
				po.openSelectedLocalRes();
			},
			
			onEditSelectedLocalRes: function(e)
			{
				var res = po.getSelectedLocalRes();
				if(res)
				{
				 	if(!$.isTextFile(res))
				 	{
				 		$.tipInfo("<@spring.message code='dashboard.editResUnsupport' />");
				 		return;
				 	}
				 	else
						po.showResContentTab(res);
				}
			},
			
			toggleLocalResMenu: function(e)
			{
				po.vueUnref("${pid}localResMenuEle").toggle(e);
			},
			
			onSearchGlobalRes: function()
			{
				po.refreshGlobalRes();
			},

			onGlobalResNodeSelect: function(node)
			{
				var pm = po.vuePageModel();
				pm.globalRes.selectedNode = node;
			},
			
			onCopyGlobalResToClipboard: function(e)
			{
				po.copyToClipboard(po.getSelectedGlobalRes());
			},
			
			onOpenSelectedGlobalRes: function(e)
			{
				po.openSelectedGlobalRes();
			},
			
			onToggleAddResPanel: function(e)
			{
				var pm = po.vuePageModel();
				po.vueUnref("${pid}addResPanelEle").toggle(e);
			},
			
			onAddResPanelShow: function(e)
			{
				var pm = po.vuePageModel();
				var panel = po.elementOfId("${pid}addResPanel", document.body);
				var form = po.elementOfId("${pid}addResForm", panel);
				po.elementOfId("${pid}addResName", form).focus();
				
				po.setupSimpleForm(form, pm.addResModel, function()
				{
					if(po.addRes(pm.addResModel.resName))
					{
						po.vueUnref("${pid}addResPanelEle").hide();
						pm.addResModel.resName = "";
					}
				});
			},
			
			onToggleUploadResPanel: function(e)
			{
				if(!po.checkPersistedDashboard())
					return;
				
				po.vueUnref("${pid}uploadResPanelEle").toggle(e);
			},
			
			onUploadResPanelShow: function(e)
			{
				var pm = po.vuePageModel();
				var panel = po.elementOfId("${pid}uploadResPanel", document.body);
				var form = po.elementOfId("${pid}uploadResForm", panel);
				
				po.setupSimpleForm(form, pm.uploadResModel,
				{
					customNormalizers:
					{
						savePath: function()
						{
							if(pm.uploadResModel.autoUnzip && $.isZipFile(pm.uploadResModel.filePath))
								return (pm.uploadResModel.savePath || "savePathValidatePlaceholder");
							else
								return pm.uploadResModel.savePath;
						}
					},
					submitHandler: function()
					{
						po.uploadRes(pm.uploadResModel);
					}
				});
			},
			
			onResUploaded: function(e)
			{
				var pm = po.vuePageModel();
				var response = $.getResponseJson(e.xhr);
				
				po.uploadFileOnUploaded(e);
				
				var sr = po.getSelectedLocalRes();
				
				pm.uploadResModel.savePath = ($.isDirectoryFile(sr) ? sr + response.fileName : response.fileName);
				pm.uploadResModel.filePath = response.uploadFilePath;
			},
			
			onToggleRenameResPanel: function(e)
			{
				if(!po.checkPersistedDashboard())
					return;
				
				po.vueUnref("${pid}renameResPanelEle").toggle(e);
			},
			
			onRenameResPanelShow: function(e)
			{
				var pm = po.vuePageModel();
				var panel = po.elementOfId("${pid}renameResPanel", document.body);
				var form = po.elementOfId("${pid}renameResForm", panel);
				po.elementOfId("${pid}destResName", form).focus();
				
				po.setupSimpleForm(form, pm.renameResModel, function()
				{
					po.renameRes(pm.renameResModel.srcName, pm.renameResModel.destName);
					po.vueUnref("${pid}renameResPanelEle").hide();
				});
			}
		});
		
		po.vueMounted(function()
		{
			if(po.isPersistedDashboard())
				po.refreshLocalRes();
		});
		
		po.vueRef("${pid}localResMenuEle", null);
		po.vueRef("${pid}addResPanelEle", null);
		po.vueRef("${pid}uploadResPanelEle", null);
		po.vueRef("${pid}renameResPanelEle", null);
	};
})
(${pid});
</script>
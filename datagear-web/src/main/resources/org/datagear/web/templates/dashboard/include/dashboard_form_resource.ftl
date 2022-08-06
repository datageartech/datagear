<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#--
看板资源列表

依赖：

-->
<p-tabview class="light-tabview">
	<p-tabpanel header="<@spring.message code='localResource' />">
		<div class="local-resource-list-wrapper resource-list-wrapper flex flex-column p-component p-inputtext">
			<div class="flex-grow-0">
				<p-button type="button" icon="pi pi-plus" class="p-button-secondary p-button-sm mr-1"
					title="<@spring.message code='dashboard.addResource.desc' />">
				</p-button>
				<p-button type="button" icon="pi pi-upload" class="p-button-secondary p-button-sm mr-1"
					title="<@spring.message code='dashboard.uploadResource.desc' />">
				</p-button>
				<p-button type="button" icon="pi pi-pencil" class="p-button-secondary p-button-sm mr-1"
					title="<@spring.message code='dashboard.editResource.desc' />">
				</p-button>
				<p-button type="button" icon="pi pi-copy" class="p-button-secondary p-button-sm mr-1"
					title="<@spring.message code='dashboard.copyResourceNameToClipboard' />">
				</p-button>
				<p-button type="button" icon="pi pi-external-link" class="p-button-secondary p-button-sm mr-1"
					@click="onOpenSelectedResourceUrl"
					title="<@spring.message code='dashboard.openResource.desc' />">
				</p-button>
			</div>
			<p-divider align="left" class="flex-grow-0 my-2 divider-z-0 text-sm"><@spring.message code='template' /></p-divider>
			<div class="flex-grow-0" style="height:5rem;">
				<p-listbox v-model="pm.localResources.selectedTemplate" :options="fm.templates"
					@change="onChangeTemplateListItem" class="h-full overflow-auto border-none">
				</p-listbox>
			</div>
			<p-divider align="left" class="flex-grow-0 my-2 divider-z-0 text-sm"><@spring.message code='allResources' /></p-divider>
			<div class="flex-grow-1 overflow-auto">
				<p-tree :value="pm.localResources.resourceNodes"
					selection-mode="single" v-model:selection-keys="pm.localResources.selectedResourceKeys"
					@node-select="onLocalResourceNodeSelect"
					class="border-none white-space-nowrap overflow-x-auto">
				</p-tree>
			</div>
		</div>
	</p-tabpanel>
	<p-tabpanel header="<@spring.message code='globalResource' />">
		<div class="global-resource-list-wrapper resource-list-wrapper flex p-component p-inputtext">
			<div class="flex-grow-0"></div>
			<div class="flex-grow-1"></div>
			<div class="flex-grow-2"></div>
		</div>
	</p-tabpanel>
</p-tabview>
<script>
(function(po)
{
	po.resourceNamesToTree = function(names)
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
	
	po.refreshLocalResources = function()
	{
		var fm = po.vueFormModel();
		
		if(!fm.id)
			return;
		
		po.getJson("/dashboard/listResources", { id: fm.id }, function(response)
		{
			var pm = po.vuePageModel();
			pm.localResources.resourceNodes = po.resourceNamesToTree(response);
		});
	};
	
	po.openSelectedResource = function()
	{
		var fm = po.vueFormModel();
		var sr = po.getSelectedResource();
		if(sr && fm.id)
		{
			var url = po.concatContextPath("/dashboard/show/"+encodeURIComponent(fm.id)+"/"+sr);
			window.open(url);
		}
	};
	
	po.getSelectedResource = function()
	{
		var pm = po.vuePageModel();
		var localResources = pm.localResources;
		
		if(localResources.selectedTemplate)
			return localResources.selectedTemplate;
		else if(localResources.selectedResourceKeys && localResources.selectedResourceNode)
			return localResources.selectedResourceNode.fullPath;
		else
			return null;
	};
	
	po.setUpResourceList = function()
	{
		po.vuePageModel(
		{
			localResources:
			{
				selectedTemplate: null,
				resourceNodes: [],
				selectedResourceKeys: null,
				selectedResourceNode: null
			}
		});
		
		po.vueMethod(
		{
			onChangeTemplateListItem: function(e)
			{
				var pm = po.vuePageModel();
				pm.localResources.selectedResourceKeys = null;
				pm.localResources.selectedResourceNode = null;
			},
			
			onLocalResourceNodeSelect: function(node)
			{
				var pm = po.vuePageModel();
				pm.localResources.selectedResourceNode = node;
				pm.localResources.selectedTemplate = null;
			},
			
			onOpenSelectedResourceUrl: function(e)
			{
				po.openSelectedResource();
			}
		});
		
		po.vueMounted(function()
		{
			po.refreshLocalResources();
		});
	};
})
(${pid});
</script>
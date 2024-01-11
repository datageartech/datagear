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
<#assign Schema=statics['org.datagear.management.domain.Schema']>
<#include "../include/page_import.ftl">
<#include "../include/html_doctype.ftl">
<html>
<head>
<#include "../include/html_head.ftl">
<title>
	<@spring.message code='module.schema' />
	<#include "../include/html_request_action_suffix.ftl">
	<#include "../include/html_app_name_suffix.ftl">
</title>
</head>
<body class="p-card no-border h-screen m-0">
<#include "../include/page_obj.ftl">
<div id="${pid}" class="page page-manager page-manager-schema h-full flex flex-column overflow-auto">
	<div class="grid grid-nogutter m-0 flex-nowrap h-full overflow-auto">
		<div class="col-3">
			<div class="flex flex-column h-full">
				<div class="page-header grid grid-nogutter align-items-center p-1 flex-grow-0 gap-2">
					<div class="col">
						<form @submit.prevent="onSearch" class="py-1">
							<div class="p-inputgroup">
								<div class="p-input-icon-left flex-grow-1">
									<i class="cursor-pointer" @click="onToggleSearchType"
										:class="pm.searchType=='schema' ? 'pi pi-database' : 'pi pi-file'"
										title="<@spring.message code='switchSearchMode' />">
									</i>
									<p-inputtext type="text" v-model="pm.searchForm.keyword" class="w-full h-full border-noround-right"></p-inputtext>
								</div>
								<p-button type="submit" icon="pi pi-search" class="px-4"></p-button>
							</div>
						</form>
					</div>
					<div class="col-fixed text-right">
						<div class="p-buttonset" v-if="!pm.isReadonlyAction">
							<p-button type="button" icon="pi pi-plus" @click="onAdd"></p-button>
							<p-button type="button" icon="pi pi-angle-down"
								aria-haspopup="true" aria-controls="${pid}schemaOptMenu"
								@click="onToggleSchemaOptMenu">
							</p-button>
						</div>
						<p-button type="button" icon="pi pi-angle-down"
							aria-haspopup="true" aria-controls="${pid}schemaOptMenu"
							@click="onToggleSchemaOptMenu" v-if="pm.isReadonlyAction">
						</p-button>
						<p-menu id="${pid}schemaOptMenu" ref="${pid}schemaOptMenuEle" :model="pm.schemaOptMenuItems" :popup="true"></p-menu>
					</div>
				</div>
				<div class="page-content flex-grow-1 overflow-auto">
					<p-tree :value="pm.schemaNodes" selection-mode="multiple" :meta-key-selection="true"
						v-model:selection-keys="pm.selectedNodeKeys"
						@node-expand="onSchemaNodeExpand" @node-select="onSchemaNodeSelect"
						:loading="pm.loadingSchema" class="schema-tree h-full overflow-auto">
					</p-tree>
				</div>
			</div>
		</div>
		<div class="schema-tabs-wrapper col-9 pl-3 pt-1 overflow-auto">
			<p-tabview v-model:active-index="pm.schemaTabs.activeIndex" :scrollable="true" @tab-change="onSchemaTabChange"
				@tab-click="onSchemaTabClick" class="contextmenu-tabview light-tabview h-full flex-tabview flex flex-column overflow-auto" :class="{'opacity-0': pm.schemaTabs.items.length == 0}">
				<p-tabpanel v-for="tab in pm.schemaTabs.items" :key="tab.id">
					<template #header>
						<span class="p-tabview-title" :title="tab.desc">{{tab.title}}</span>
						<p-button type="button" icon="pi pi-angle-down"
							class="context-menu-btn p-button-xs p-button-secondary p-button-text p-button-rounded"
							@click="onSchemaTabMenuToggle($event, tab)" aria-haspopup="true" aria-controls="${pid}schemaTabMenu">
						</p-button>
					</template>
					<div :id="tab.id" class=" h-full overflow-auto"></div>
				</p-tabpanel>
			</p-tabview>
			<p-menu id="${pid}schemaTabMenu" ref="${pid}schemaTabMenuEle" :model="pm.schemaTabMenuItems" :popup="true" class="text-sm"></p-menu>
		</div>
	</div>
	<#include "../include/page_foot.ftl">
</div>
<#include "../include/page_manager.ftl">
<#include "../include/page_tabview.ftl">
<script>
(function(po)
{
	po.currentUserId = "${currentUser.id}";

	po.i18n.pleaseSelectOnlyOne = "<@spring.message code='schema.pleaseSelectOnlyOne' />";
	po.i18n.pleaseSelectAtLeastOne = "<@spring.message code='schema.pleaseSelectAtLeastOne' />";
	po.i18n.confirmDeleteAsk = "<@spring.message code='schema.confirmDeleteAsk' />";
	
	po.schemaTabTypeTable = "table";
	po.schemaTabTypeSqlpad = "sqlpad";
	po.schemaTabTypeImportData = "importData";
	po.schemaTabTypeExportData = "exportData";
	
	po.refresh = function()
	{
		po.loadSchemaNodes();
	};
	
	po.getSelectedEntities = function()
	{
		var pm = po.vuePageModel();
		var schemaNodes = (pm.schemaNodes || []);
		var selectedNodeKeys = po.vueRaw(pm.selectedNodeKeys);
		
		var re = [];
		
		if(!selectedNodeKeys)
			return re;
		
		for(var i=0; i<schemaNodes.length; i++)
		{
			var schemaId = schemaNodes[i].schemaId;
			for(var selectedKey in selectedNodeKeys)
			{
				if(selectedKey == schemaId)
					re.push(po.vueRaw(schemaNodes[i].schema));
			}
		}
		
		return re;
	};
	
	po.loadSchemaNodes = function()
	{
		var pm = po.vuePageModel();
		var keyword = pm.searchForm.keyword;
		
		pm.loadingSchema = true;
		po.ajaxJson("/schema/list",
		{
			data: { keyword: keyword },
			success: function(response)
			{
				pm.schemaNodes = po.schemasToNodes(response);
				pm.selectedNodeKeys = null;
			},
			complete: function()
			{
				pm.loadingSchema = false;
			}
		});
	};
	
	po.loadTableNodes = function(schemaNode, page)
	{
		page = (page == null ? 1 : page);
		
		if(!schemaNode)
			return;
		
		var pm = po.vuePageModel();
		var keyword = pm.searchForm.keyword;
		
		pm.loadingSchema = true;
		po.ajaxJson("/schema/"+encodeURIComponent(schemaNode.schemaId)+"/pagingQueryTable",
		{
			data: { keyword: keyword, pageSize: 100, page: page },
			success: function(response)
			{
				var loadedNodes = po.tablePagingDataToNodes(schemaNode.schemaId, response);
				
				if(page > 1)
				{
					var children = schemaNode.children;
					if(children[children.length-1].dataType = "loadMore")
						children.pop();
					
					schemaNode.children = children.concat(loadedNodes);
				}
				else
					schemaNode.children = loadedNodes;
			},
			complete: function()
			{
				pm.loadingSchema = false;
			}
		});
	};
	
	po.evalFirstAwareSchemaNode = function(schemaNode)
	{
		if(schemaNode != null)
			return schemaNode;
		
		var pm = po.vuePageModel();
		var schemaNodes = (pm.schemaNodes || []);
		var selectedNodeKeys = po.vueRaw(pm.selectedNodeKeys);
		
		if(!selectedNodeKeys)
			return null;
		
		for(var i=0; i<schemaNodes.length; i++)
		{
			var schemaId = schemaNodes[i].schemaId;
			for(var selectedKey in selectedNodeKeys)
			{
				if(selectedKey == schemaId || selectedKey.indexOf(schemaId) == 0)
					return schemaNodes[i];
			}
		}
		
		return null;
	};
	
	po.executeOnFirstAwareSchemaNode = function(callback)
	{
		var schemaNode = po.evalFirstAwareSchemaNode();
		if(!schemaNode)
			$.tipInfo("<@spring.message code='pleaseSelectOneSchema' />");
		else
			callback(schemaNode);
	};
	
	po.schemasToNodes = function(schemas)
	{
		var re = [];
		
		$.each(schemas, function(idx, schema)
		{
			var label = schema.title;
			
			if(schema.createUser && schema.createUser.id != po.currentUserId)
				label += " ("+schema.createUser.nameLabel+")";
			
			re.push(
			{
				key: schema.id,
				label: label,
				icon: "pi pi-database",
				leaf: false,
				dataType: "schema",
				schemaId: schema.id,
				schema: schema
			});
		});
		
		return re;
	};
	
	po.findSchemaNode = function(schemaId)
	{
		var pm = po.vuePageModel();
		var schemaNodes = (pm.schemaNodes || []);
		var idx = $.inArrayById(schemaNodes, schemaId, "schemaId");
		
		return (idx > -1 ? schemaNodes[idx] : null);
	};
	
	po.tablePagingDataToNodes = function(schemaId, pagingData)
	{
		var re = [];
		
		$.each(pagingData.items, function(idx, table)
		{
			re.push(
			{
				key: schemaId + "-" + table.name,
				label: table.name,
				icon: "pi pi-file",
				leaf: true,
				dataType: "table",
				schemaId: schemaId,
				tableName: table.name
			});
		});
		
		//添加下一页节点
		if(pagingData.page < pagingData.pages)
		{
			var showCount = (pagingData.page-1) * pagingData.pageSize + pagingData.items.length;
			
			re.push(
			{
				key: "next-page-for-"+pagingData.page,
				label : "<@spring.message code='loadMore' /> (" + showCount + "/" + pagingData.total+")",
				icon: "pi pi-arrow-down",
				leaf: true,
				styleClass: "font-bold",
				dataType: "loadMore",
				schemaId: schemaId,
				nextPage: pagingData.page + 1
			});
		}
		
		return re;
	};
	
	po.getSelectedTableNodes = function()
	{
		var pm = po.vuePageModel();
		var schemaNodes = (pm.schemaNodes || []);
		var selectedNodeKeys = po.vueRaw(pm.selectedNodeKeys);
		
		var re = [];
		
		if(!selectedNodeKeys)
			return re;
		
		for(var i=0; i<schemaNodes.length; i++)
		{
			var children = (schemaNodes[i].children || []);
			for(var j=0; j<children.length; j++)
			{
				var tableNode = children[j];
				
				for(var selectedKey in selectedNodeKeys)
				{
					if(selectedKey == tableNode.key)
						re.push(tableNode);
				}
			}
		}
		
		return re;
	};
	
	po.showSchemaTab = function(schema, name, title, type)
	{
		var pm = po.vuePageModel();
		var schemaId = schema.id;
		
		var idx = po.getSchemaTabIndex(schemaId, name);
		if(idx > -1)
			pm.schemaTabs.activeIndex = idx;
		else
		{
			var tab =
			{
				id: po.toSchemaTabId(schemaId, name),
				title: $.truncateIf(title, "...", 22),
				schemaId: schemaId,
				name: name,
				type: type,
				desc: schema.title + " > " + title
			};
			
			//需转换为绝对路径，因为要支持在新窗口打开
			tab.url = po.concatContextPath(po.toSchemaTabUrl(tab));
			
			pm.schemaTabs.items.push(tab);
			
			//直接设置activeIndex不会滚动到新加的卡片，所以采用此方案
			po.vueNextTick(function()
			{
				pm.schemaTabs.activeIndex = pm.schemaTabs.items.length - 1;
			});
		}
	};
	
	po.loadSchemaTab = function(tab, forceLoad, loadUrl)
	{
		var panel = po.elementOfId(tab.id);
		var expectLoad = (panel.prop("loaded") !== true || forceLoad);
		
		if(expectLoad && panel.prop("loading") !== true)
		{
			panel.prop("loading", true);
			panel.empty();
			
			var loadingDiv = null;
			
			if(tab.type == po.schemaTabTypeTable)
			{
				loadingDiv = $("<div class='flex justify-content-center align-items-center h-6rem text-color-secondary' />").appendTo(panel);
				$("<i class='pi pi-spin pi-spinner text-lg'></i>").appendTo(loadingDiv);
				$("<span class='ml-2 text-lg'><@spring.message code='loading' /></span>").appendTo(loadingDiv);
			}
			
			var url = (forceLoad && loadUrl ? loadUrl : tab.url);
			
			if(url)
			{
				po.open(url,
				{
					fullUrl: true,
					target: panel,
					dialog: false,
					success: function()
					{
						panel.prop("loaded", true);
					},
					complete: function()
					{
						panel.prop("loading", false);
						
						if(loadingDiv)
							loadingDiv.remove();
					}
				});
			}
		}
	};
	
	po.toSchemaTabUrl = function(tab)
	{
		if(tab.type == po.schemaTabTypeTable)
			return "/data/"+encodeURIComponent(tab.schemaId)+"/"+encodeURIComponent(tab.name)+"/pagingQuery";
		else if(tab.type == po.schemaTabTypeSqlpad)
			return "/sqlpad/"+encodeURIComponent(tab.schemaId);
		else if(tab.type == po.schemaTabTypeImportData)
			return "/dataexchange/"+encodeURIComponent(tab.schemaId)+"/import";
		else if(tab.type == po.schemaTabTypeExportData)
			return "/dataexchange/"+encodeURIComponent(tab.schemaId)+"/export";
		else
			return null;
	};
	
	po.toSchemaReloadTableUrl = function(tableUrl)
	{
		return tableUrl += "?reloadTable=true";
	};
	
	po.getSchemaTabIndex = function(schemaId, name)
	{
		var pm = po.vuePageModel();
		var items = pm.schemaTabs.items;
		
		return $.inArrayById(items, po.toSchemaTabId(schemaId, name));
	};
	
	po.toSchemaTabId = function(schemaId, name)
	{
		var map = (po._schemaTabIdMap || (po._schemaTabIdMap = {}));
		
		//不直接使用name作为元素ID，因为name中可能存在与jquery冲突的字符，比如'$'
		var key = (schemaId + name);
		var value = map[key];
		
		if(value == null)
		{
			value = $.uid("schemaTab");
			map[key] = value;
		}
		
		return value;
	};
	
	po.setupAction();
	
	po.vuePageModel(
	{
		searchForm:{ keyword: "" },
		searchType: "schema",
		loadingSchema: false,
		schemaNodes: null,
		selectedNodeKeys: null,
		schemaTabs:
		{
			items: [],
			activeIndex: 0
		},
		schemaOptMenuItems:
		[
			{
				label: "<@spring.message code='edit' />",
				visible: function()
				{
					return !po.isReadonlyAction;
				},
				command: function()
				{
					po.handleOpenOfAction("/schema/edit");
				}
			},
			{
				label: "<@spring.message code='copy' />",
				visible: function()
				{
					return !po.isReadonlyAction;
				},
				command: function()
				{
					po.handleOpenOfAction("/schema/copy");
				}
			},
			{
				label: "<@spring.message code='view' />",
				command: function()
				{
					po.handleOpenOfAction("/schema/view");
				}
			},
			{
				label: "<@spring.message code='delete' />",
				visible: function()
				{
					return !po.isReadonlyAction;
				},
				command: function()
				{
					po.handleDeleteAction("/schema/delete");
				}
			},
			{
				label: "<@spring.message code='autherization' />",
				visible: function()
				{
					return !po.isReadonlyAction;
				},
				command: function()
				{
					po.executeOnSelect(function(schema)
					{
						po.openTableDialog("/authorization/${Schema.AUTHORIZATION_RESOURCE_TYPE}/"+encodeURIComponent(schema.id)+"/query");
					});
				}
			},
			{ separator: true },
			{
				label: "<@spring.message code='module.sqlpad' />",
				command: function()
				{
					po.executeOnFirstAwareSchemaNode(function(schemaNode)
					{
						var tabName = (po.schemaTabNameSqlpad ? po.schemaTabNameSqlpad : (po.schemaTabNameSqlpad = $.uid("schemaTabNameSqlpad")));
						po.showSchemaTab(schemaNode.schema, tabName, "<@spring.message code='module.sqlpad' />", po.schemaTabTypeSqlpad);
					});
				}
			},
			{
				label: "<@spring.message code='module.importData' />",
				command: function()
				{
					po.executeOnFirstAwareSchemaNode(function(schemaNode)
					{
						var tabName = (po.schemaTabNameImportData ? po.schemaTabNameImportData : (po.schemaTabNameImportData = $.uid("schemaTabNameImportData")));
						po.showSchemaTab(schemaNode.schema, tabName, "<@spring.message code='module.importData' />", po.schemaTabTypeImportData);
					});
				}
			},
			{
				label: "<@spring.message code='module.exportData' />",
				command: function()
				{
					po.executeOnFirstAwareSchemaNode(function(schemaNode)
					{
						var tabName = (po.schemaTabNameExportData ? po.schemaTabNameExportData : (po.schemaTabNameExportData = $.uid("schemaTabNameExportData")));
						po.showSchemaTab(schemaNode.schema, tabName, "<@spring.message code='module.exportData' />", po.schemaTabTypeExportData);
					});
				}
			}
		],
		schemaTabMenuItems:
		[
			{
				label: "<@spring.message code='close' />",
				command: function()
				{
					po.tabviewClose(po.vuePageModel().schemaTabs, po.schemaTabMenuOnTabId);
				}
			},
			{
				label: "<@spring.message code='closeOther' />",
				command: function()
				{
					po.tabviewCloseOther(po.vuePageModel().schemaTabs, po.schemaTabMenuOnTabId);
				}
			},
			{
				label: "<@spring.message code='closeRight' />",
				command: function()
				{
					po.tabviewCloseRight(po.vuePageModel().schemaTabs, po.schemaTabMenuOnTabId);
				}
			},
			{
				label: "<@spring.message code='closeLeft' />",
				command: function()
				{
					po.tabviewCloseLeft(po.vuePageModel().schemaTabs, po.schemaTabMenuOnTabId);
				}
			},
			{
				label: "<@spring.message code='closeAll' />",
				command: function()
				{
					po.tabviewCloseAll(po.vuePageModel().schemaTabs);
				}
			},
			{ separator: true },
			{
				label: "<@spring.message code='openInNewWindow' />",
				command: function()
				{
					po.tabviewOpenInNewWindow(po.vuePageModel().schemaTabs, po.schemaTabMenuOnTabId);
				}
			},
			{
				label: "<@spring.message code='viewTableStructure' />",
				visible: function()
				{
					var tab = po.tabviewTab(po.vuePageModel().schemaTabs, po.schemaTabMenuOnTabId);
					return (tab && tab.type == po.schemaTabTypeTable);
				},
				command: function()
				{
					var tab = po.tabviewTab(po.vuePageModel().schemaTabs, po.schemaTabMenuOnTabId);
					if(tab && tab.type == po.schemaTabTypeTable)
					{
						po.open("/schema/"+encodeURIComponent(tab.schemaId)+"/tableMeta/"+encodeURIComponent(tab.name));
					}
				}
			},
			{
				label: "<@spring.message code='refreshTableStructure' />",
				visible: function()
				{
					var tab = po.tabviewTab(po.vuePageModel().schemaTabs, po.schemaTabMenuOnTabId);
					return (tab && tab.type == po.schemaTabTypeTable);
				},
				command: function()
				{
					var tab = po.tabviewTab(po.vuePageModel().schemaTabs, po.schemaTabMenuOnTabId);
					if(tab && tab.type == po.schemaTabTypeTable)
					{
						var url = po.toSchemaReloadTableUrl(tab.url);
						po.loadSchemaTab(tab, true, url);
					}
				}
			}
		]
	});
	
	po.vueRef("${pid}schemaTabMenuEle", null);
	po.vueRef("${pid}schemaOptMenuEle", null);
	
	po.vueMethod(
	{
		onSearch: function()
		{
			var pm = po.vuePageModel();
			
			if(pm.searchType == "schema")
				po.loadSchemaNodes();
			else if(pm.searchType == "table")
			{
				po.executeOnFirstAwareSchemaNode(function(schemaNode)
				{
					po.loadTableNodes(schemaNode);
				});
			}
		},
		
		onSchemaNodeExpand: function(node)
		{
			if(node.children == null)
			{
				po.loadTableNodes(node);
			}
		},
		
		onSchemaNodeSelect: function(node)
		{
			if(node.dataType == "loadMore")
			{
				var mySchemaNode = po.findSchemaNode(node.schemaId);
				po.loadTableNodes(mySchemaNode, node.nextPage);
			}
			else if(node.dataType == "table")
			{
				var mySchemaNode = po.findSchemaNode(node.schemaId);
				po.showSchemaTab(mySchemaNode.schema, node.tableName, node.tableName, po.schemaTabTypeTable);
			}
		},
		
		onSchemaTabChange: function(e){},
		
		onSchemaTabClick: function(e){},
		
		onToggleSearchType: function()
		{
			var pm = po.vuePageModel();
			pm.searchType = (pm.searchType == "schema" ? "table" : "schema");
		},
		
		onToggleSchemaOptMenu: function(e)
		{
			po.vueUnref("${pid}schemaOptMenuEle").toggle(e);
		},
		
		onSchemaTabMenuToggle: function(e, tab)
		{
			e.stopPropagation();
			po.vueUnref("${pid}schemaTabMenuEle").hide();

			//直接show会导致面板还停留在上一个元素上
			po.vueNextTick(function()
			{
				po.schemaTabMenuOnTabId = tab.id;
				po.vueUnref("${pid}schemaTabMenuEle").show(e);
			});
		},
		
		onAdd: function()
		{
			po.handleAddAction("/schema/add");
		},
		
		onEdit: function()
		{
			po.handleOpenOfAction("/schema/edit");
		},
		
		onView: function()
		{
			po.handleOpenOfAction("/schema/view");
		},
		
		onDelete: function()
		{
			po.handleDeleteAction("/schema/delete");
		}
	});
	
	//po.showSchemaTab()里不能里可获取到创建的DOM元素，所以采用此方案
	po.vueWatch(po.vuePageModel().schemaTabs, function(oldVal, newVal)
	{
		var items = newVal.items;
		var activeIndex = newVal.activeIndex;
		var activeTab = items[activeIndex];
		
		if(activeTab)
		{
			po.vueNextTick(function()
			{
				po.loadSchemaTab(activeTab);
			});
		}
	});
	
	po.vueMounted(function()
	{
		po.loadSchemaNodes();
	});
})
(${pid});
</script>
<#include "../include/page_vue_mount.ftl">
</body>
</html>
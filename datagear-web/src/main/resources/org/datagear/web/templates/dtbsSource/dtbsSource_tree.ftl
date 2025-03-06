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
<#assign Global=statics['org.datagear.util.Global']>
<#assign DtbsSource=statics['org.datagear.management.domain.DtbsSource']>
<#include "../include/page_import.ftl">
<#include "../include/html_doctype.ftl">
<html>
<head>
<#include "../include/html_head.ftl">
<title>
	<@spring.message code='module.dtbsSource' />
	<#include "../include/html_request_action_suffix.ftl">
	<#include "../include/html_app_name_suffix.ftl">
</title>
</head>
<body class="p-card no-border h-screen m-0">
<#include "../include/page_obj.ftl">
<div id="${pid}" class="page page-manager page-manager-dtbsSource h-full flex flex-column overflow-auto">
	<div class="grid grid-nogutter m-0 flex-nowrap h-full overflow-auto">
		<div class="col-3">
			<div class="flex flex-column h-full">
				<div class="page-header grid grid-nogutter align-items-center p-1 flex-grow-0 gap-2">
					<div class="col">
						<form @submit.prevent="onSearch" class="py-1">
							<div class="p-inputgroup">
								<p-inputtext type="text" v-model="pm.searchForm.keyword" class="flex-grow-1"></p-inputtext>
								<p-button type="button" :icon="pm.searchType=='dtbsSource' ? 'pi pi-database' : 'pi pi-file'"
									aria-haspopup="true" aria-controls="${pid}searchTypeMenu" @click="onToggleSearchTypeMenu"></p-button>
								<p-button type="submit" icon="pi pi-search"></p-button>
							</div>
							<p-menu id="${pid}searchTypeMenu" ref="${pid}searchTypeMenuEle" :model="pm.searchTypeMenuItems" :popup="true"></p-menu>
						</form>
					</div>
					<div class="col-fixed text-right">
						<div class="p-buttonset" v-if="!pm.isReadonlyAction">
							<p-button type="button" icon="pi pi-plus" @click="onAdd"></p-button>
							<p-button type="button" icon="pi pi-angle-down"
								aria-haspopup="true" aria-controls="${pid}dtbsSourceOptMenu"
								@click="onToggleDtbsSourceOptMenu">
							</p-button>
						</div>
						<p-button type="button" icon="pi pi-angle-down"
							aria-haspopup="true" aria-controls="${pid}dtbsSourceOptMenu"
							@click="onToggleDtbsSourceOptMenu" v-if="pm.isReadonlyAction">
						</p-button>
						<p-menu id="${pid}dtbsSourceOptMenu" ref="${pid}dtbsSourceOptMenuEle" :model="pm.dtbsSourceOptMenuItems" :popup="true"></p-menu>
					</div>
				</div>
				<div class="page-content flex-grow-1 overflow-auto">
					<p-tree :value="pm.dtbsSourceNodes" selection-mode="multiple" :meta-key-selection="true"
						v-model:selection-keys="pm.selectedNodeKeys" v-model:expanded-keys="pm.expandedNodeKeys"
						@node-expand="onDtbsSourceNodeExpand" @node-select="onDtbsSourceNodeSelect"
						:loading="pm.loadingDtbsSource" class="dtbsSource-tree h-full overflow-auto">
					</p-tree>
				</div>
			</div>
		</div>
		<div class="dtbsSource-tabs-wrapper col-9 pl-3 pt-1 overflow-auto">
			<p-tabview v-model:active-index="pm.dtbsSourceTabs.activeIndex" :scrollable="true" @tab-change="onDtbsSourceTabChange"
				@tab-click="onDtbsSourceTabClick" class="contextmenu-tabview light-tabview hide-default-title h-full flex-tabview flex flex-column overflow-auto" :class="{'opacity-0': pm.dtbsSourceTabs.items.length == 0}">
				<!--
				注意：p-tabpanel组件必须设置header属性，且值不能重复，否则，删除前置卡片会导致后后置卡片重绘。
				所以，这里将header设置为id，并使用hide-default-title样式类自定义标题
				-->
				<p-tabpanel v-for="tab in pm.dtbsSourceTabs.items" :key="tab.id" :header="tab.id">
					<template #header>
						<span class="p-tabview-title custom-title" :title="tab.desc">{{tab.title}}</span>
						<p-button type="button" icon="pi pi-angle-down"
							class="context-menu-btn p-button-xs p-button-secondary p-button-text p-button-rounded"
							@click="onDtbsSourceTabMenuToggle($event, tab)" aria-haspopup="true" aria-controls="${pid}dtbsSourceTabMenu">
						</p-button>
					</template>
					<div :id="tab.id" class=" h-full overflow-auto"></div>
				</p-tabpanel>
			</p-tabview>
			<p-menu id="${pid}dtbsSourceTabMenu" ref="${pid}dtbsSourceTabMenuEle" :model="pm.dtbsSourceTabMenuItems" :popup="true" class="text-sm"></p-menu>
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

	po.i18n.pleaseSelectOnlyOne = "<@spring.message code='dtbsSource.pleaseSelectOnlyOne' />";
	po.i18n.pleaseSelectAtLeastOne = "<@spring.message code='dtbsSource.pleaseSelectAtLeastOne' />";
	po.i18n.confirmDeleteAsk = "<@spring.message code='dtbsSource.confirmDeleteAsk' />";
	
	po.dtbsSourceTabTypeTable = "table";
	po.dtbsSourceTabTypeSqlpad = "sqlpad";
	po.dtbsSourceTabTypeImportData = "importData";
	po.dtbsSourceTabTypeExportData = "exportData";
	
	po.dtbsSourcePageSizeName = "${Global.NAME_SHORT_UCUS}DTBS_SOURCE_PAGE_SIZE";
	po.dtbsSourceTablePageSizeName = "${Global.NAME_SHORT_UCUS}DTBS_SOURCE_TABLE_PAGE_SIZE";
	
	po.refresh = function()
	{
		po.loadDtbsSourceNodes();
	};
	
	po.getSelectedEntities = function()
	{
		var pm = po.vuePageModel();
		var dtbsSourceNodes = (pm.dtbsSourceNodes || []);
		var selectedNodeKeys = po.vueRaw(pm.selectedNodeKeys);
		
		var re = [];
		
		if(!selectedNodeKeys)
			return re;
		
		for(var i=0; i<dtbsSourceNodes.length; i++)
		{
			var dtbsSourceId = dtbsSourceNodes[i].dtbsSourceId;
			for(var selectedKey in selectedNodeKeys)
			{
				if(selectedKey == dtbsSourceId)
					re.push(po.vueRaw(dtbsSourceNodes[i].dtbsSource));
			}
		}
		
		return re;
	};
	
	po.searchByType = function(type)
	{
		if(type === undefined)
		{
			var pm = po.vuePageModel();
			type = pm.searchType;
		}
		
		if(type == "dtbsSource")
		{
			po.loadDtbsSourceNodes();
		}
		else if(type == "table")
		{
			po.executeOnFirstAwareDtbsSourceNode(function(dtbsSourceNode)
			{
				po.loadTableNodes(dtbsSourceNode, 1, true);
			});
		}
	};
	
	po.loadDtbsSourceNodes = function(page)
	{
		page = (page == null ? 1 : page);
		
		var pm = po.vuePageModel();
		var keyword = (pm.searchType == "dtbsSource" ? pm.searchForm.keyword : undefined);
		
		pm.loadingDtbsSource = true;
		po.ajaxJson("/dtbsSource/pagingQueryData",
		{
			data: { keyword: keyword, pageSize: ($.localStorageItem(po.dtbsSourcePageSizeName) || 50), page: page, orders: [ { name: "title", type: "ASC"} ] },
			success: function(response)
			{
				var loadedNodes = po.dtbsSourcePagingDataToNodes(response);
				
				if(page > 1)
				{
					po.removeIfTailNextPageNode(pm.dtbsSourceNodes);
					pm.dtbsSourceNodes = pm.dtbsSourceNodes.concat(loadedNodes);
				}
				else
					pm.dtbsSourceNodes = loadedNodes;
				
				pm.selectedNodeKeys = null;
				pm.expandedNodeKeys = {};
			},
			complete: function()
			{
				pm.loadingDtbsSource = false;
			}
		});
	};
	
	po.loadTableNodes = function(dtbsSourceNode, page, autoExpand)
	{
		page = (page == null ? 1 : page);
		autoExpand = (autoExpand == null ? false : autoExpand);
		
		if(!dtbsSourceNode)
			return;
		
		var pm = po.vuePageModel();
		var keyword = (pm.searchType != "dtbsSource" ? pm.searchForm.keyword : undefined);
		
		pm.loadingDtbsSource = true;
		po.ajaxJson("/dtbsSource/"+encodeURIComponent(dtbsSourceNode.dtbsSourceId)+"/pagingQueryTable",
		{
			data: { keyword: keyword, pageSize: ($.localStorageItem(po.dtbsSourceTablePageSizeName) || 100), page: page },
			success: function(response)
			{
				var loadedNodes = po.tablePagingDataToNodes(dtbsSourceNode.dtbsSourceId, response);
				
				if(page > 1)
				{
					po.removeIfTailNextPageNode(dtbsSourceNode.children);
					dtbsSourceNode.children = dtbsSourceNode.children.concat(loadedNodes);
				}
				else
					dtbsSourceNode.children = loadedNodes;
				
				if(autoExpand)
					pm.expandedNodeKeys[dtbsSourceNode.key] = true;
			},
			complete: function()
			{
				pm.loadingDtbsSource = false;
			}
		});
	};
	
	po.evalFirstAwareDtbsSourceNode = function(dtbsSourceNode)
	{
		if(dtbsSourceNode != null)
			return dtbsSourceNode;
		
		var pm = po.vuePageModel();
		var dtbsSourceNodes = (pm.dtbsSourceNodes || []);
		var selectedNodeKeys = po.vueRaw(pm.selectedNodeKeys);
		
		if(!selectedNodeKeys)
			return null;
		
		for(var i=0; i<dtbsSourceNodes.length; i++)
		{
			var dtbsSourceId = dtbsSourceNodes[i].dtbsSourceId;
			for(var selectedKey in selectedNodeKeys)
			{
				if(selectedKey == dtbsSourceId || selectedKey.indexOf(dtbsSourceId) == 0)
					return dtbsSourceNodes[i];
			}
		}
		
		return null;
	};
	
	po.executeOnFirstAwareDtbsSourceNode = function(callback)
	{
		var dtbsSourceNode = po.evalFirstAwareDtbsSourceNode();
		if(!dtbsSourceNode)
			$.tipInfo("<@spring.message code='pleaseSelectOneDtbsSource' />");
		else
			callback(dtbsSourceNode);
	};
	
	po.dtbsSourcePagingDataToNodes = function(pagingData)
	{
		var re = [];
		
		$.each(pagingData.items, function(idx, dtbsSource)
		{
			var label = dtbsSource.title;
			
			if(dtbsSource.createUser && dtbsSource.createUser.id != po.currentUserId)
				label += " ("+dtbsSource.createUser.nameLabel+")";
			
			re.push(
			{
				key: dtbsSource.id,
				label: label,
				icon: "pi pi-database",
				leaf: false,
				dataType: "dtbsSource",
				dtbsSourceId: dtbsSource.id,
				dtbsSource: dtbsSource
			});
		});
		
		//添加下一页节点
		var nextPageNode = po.evalNextPageNode(pagingData, "dtbsSource", function(node)
		{
			node.dataType = "dtbsSourceLoadMore";
		});
		
		if(nextPageNode)
			re.push(nextPageNode);
		
		return re;
	};
	
	po.findDtbsSourceNode = function(dtbsSourceId)
	{
		var pm = po.vuePageModel();
		var dtbsSourceNodes = (pm.dtbsSourceNodes || []);
		var idx = $.inArrayById(dtbsSourceNodes, dtbsSourceId, "dtbsSourceId");
		
		return (idx > -1 ? dtbsSourceNodes[idx] : null);
	};
	
	po.tablePagingDataToNodes = function(dtbsSourceId, pagingData)
	{
		var re = [];
		
		$.each(pagingData.items, function(idx, table)
		{
			re.push(
			{
				key: dtbsSourceId + "-" + table.name,
				label: table.name,
				icon: "pi pi-file",
				leaf: true,
				dataType: "table",
				dtbsSourceId: dtbsSourceId,
				tableName: table.name
			});
		});
		
		//添加下一页节点
		var nextPageNode = po.evalNextPageNode(pagingData, "table", function(node)
		{
			node.dataType = "tableLoadMore";
			node.dtbsSourceId = dtbsSourceId;
		});
		
		if(nextPageNode)
			re.push(nextPageNode);
		
		return re;
	};
	
	po.evalNextPageNode = function(pagingData, keyPrefix, callback)
	{
		if(pagingData.page >= pagingData.pages)
			return null;
		
		var showCount = (pagingData.page-1) * pagingData.pageSize + pagingData.items.length;
		
		var node =
		{
			key: keyPrefix+"-next-page-for-"+pagingData.page,
			label : "<@spring.message code='loadMore' /> (" + showCount + "/" + pagingData.total+")",
			icon: "pi pi-arrow-down",
			leaf: true,
			styleClass: "font-bold",
			nextPageNode: true,
			nextPage: pagingData.page + 1
		};
		
		if(callback)
			callback(node);
		
		return node;
	};
	
	po.removeIfTailNextPageNode = function(nodes)
	{
		if(!nodes || nodes.length < 1)
			return;
		
		if(nodes[nodes.length-1].nextPageNode === true)
			nodes.pop();
	};
	
	po.getSelectedTableNodes = function()
	{
		var pm = po.vuePageModel();
		var dtbsSourceNodes = (pm.dtbsSourceNodes || []);
		var selectedNodeKeys = po.vueRaw(pm.selectedNodeKeys);
		
		var re = [];
		
		if(!selectedNodeKeys)
			return re;
		
		for(var i=0; i<dtbsSourceNodes.length; i++)
		{
			var children = (dtbsSourceNodes[i].children || []);
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
	
	po.showDtbsSourceTab = function(dtbsSource, name, title, type)
	{
		var pm = po.vuePageModel();
		var dtbsSourceId = dtbsSource.id;
		
		var idx = po.getDtbsSourceTabIndex(dtbsSourceId, name);
		if(idx > -1)
			pm.dtbsSourceTabs.activeIndex = idx;
		else
		{
			var tab =
			{
				id: po.toDtbsSourceTabId(dtbsSourceId, name),
				title: $.truncateIf(title, "...", 22),
				dtbsSourceId: dtbsSourceId,
				name: name,
				type: type,
				desc: dtbsSource.title + " > " + title
			};
			
			//需转换为绝对路径，因为要支持在新窗口打开
			tab.url = po.concatContextPath(po.toDtbsSourceTabUrl(tab));
			
			pm.dtbsSourceTabs.items.push(tab);
			
			//直接设置activeIndex不会滚动到新加的卡片，所以采用此方案
			po.vueNextTick(function()
			{
				pm.dtbsSourceTabs.activeIndex = pm.dtbsSourceTabs.items.length - 1;
			});
		}
	};
	
	po.loadDtbsSourceTab = function(tab, forceLoad, loadUrl)
	{
		var loadStatus = tab.loadStatus;
		var needLoad = (forceLoad || (loadStatus != "loaded" && loadStatus != "loading"));
		
		if(needLoad)
		{
			tab.loadStatus = "loading";
			
			var panel = po.elementOfId(tab.id);
			panel.empty();
			
			var loadingDiv = null;
			
			if(tab.type == po.dtbsSourceTabTypeTable)
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
					success: function(){},
					complete: function()
					{
						tab.loadStatus = "loaded";
						
						if(loadingDiv)
							loadingDiv.remove();
					}
				});
			}
		}
	};
	
	po.toDtbsSourceTabUrl = function(tab)
	{
		if(tab.type == po.dtbsSourceTabTypeTable)
			return "/dtbsSourceData/"+encodeURIComponent(tab.dtbsSourceId)+"/"+encodeURIComponent(tab.name)+"/manage";
		else if(tab.type == po.dtbsSourceTabTypeSqlpad)
			return "/dtbsSourceSqlpad/"+encodeURIComponent(tab.dtbsSourceId);
		else if(tab.type == po.dtbsSourceTabTypeImportData)
			return "/dtbsSourceExchange/"+encodeURIComponent(tab.dtbsSourceId)+"/import";
		else if(tab.type == po.dtbsSourceTabTypeExportData)
			return "/dtbsSourceExchange/"+encodeURIComponent(tab.dtbsSourceId)+"/export";
		else
			return null;
	};
	
	po.toDtbsSourceReloadTableUrl = function(tableUrl)
	{
		return tableUrl += "?reloadTable=true";
	};
	
	po.getDtbsSourceTabIndex = function(dtbsSourceId, name)
	{
		var pm = po.vuePageModel();
		var items = pm.dtbsSourceTabs.items;
		
		return $.inArrayById(items, po.toDtbsSourceTabId(dtbsSourceId, name));
	};
	
	po.toDtbsSourceTabId = function(dtbsSourceId, name)
	{
		var map = (po._dtbsSourceTabIdMap || (po._dtbsSourceTabIdMap = {}));
		
		//不直接使用name作为元素ID，因为name中可能存在与jquery冲突的字符，比如'$'
		var key = (dtbsSourceId + name);
		var value = map[key];
		
		if(value == null)
		{
			value = $.uid("dtbsSourceTab");
			map[key] = value;
		}
		
		return value;
	};
	
	po.setupAction();
	
	po.vuePageModel(
	{
		searchForm:{ keyword: "" },
		searchType: "dtbsSource",
		searchTypeMenuItems:
		[
			{
				label: "<@spring.message code='dtbsSource.searchSource' />",
				icon: "pi pi-database",
				command: function(e)
				{
					var pm = po.vuePageModel();
					pm.searchType = "dtbsSource";
					po.searchByType();
				}
			},
			{
				label: "<@spring.message code='dtbsSource.searchTable' />",
				icon: "pi pi-file",
				command: function(e)
				{
					var pm = po.vuePageModel();
					pm.searchType = "table";
					po.searchByType();
				}
			}
		],
		loadingDtbsSource: false,
		dtbsSourceNodes: null,
		selectedNodeKeys: null,
		expandedNodeKeys: {},
		dtbsSourceTabs:
		{
			items: [],
			activeIndex: 0
		},
		dtbsSourceOptMenuItems:
		[
			{
				label: "<@spring.message code='edit' />",
				visible: function()
				{
					return !po.isReadonlyAction;
				},
				command: function()
				{
					po.handleOpenOfAction("/dtbsSource/edit");
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
					po.handleOpenOfAction("/dtbsSource/copy");
				}
			},
			{
				label: "<@spring.message code='view' />",
				command: function()
				{
					po.handleOpenOfAction("/dtbsSource/view");
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
					po.executeOnSelect(function(dtbsSource)
					{
						po.openTableDialog("/authorization/${DtbsSource.AUTHORIZATION_RESOURCE_TYPE}/"+encodeURIComponent(dtbsSource.id)+"/manage");
					});
				}
			},
			{
				label: "<@spring.message code='delete' />",
				visible: function()
				{
					return !po.isReadonlyAction;
				},
				class: "danger-menuitem",
				command: function()
				{
					po.handleDeleteAction("/dtbsSource/delete");
				}
			},
			{ separator: true },
			{
				label: "<@spring.message code='module.sqlpad' />",
				command: function()
				{
					po.executeOnFirstAwareDtbsSourceNode(function(dtbsSourceNode)
					{
						var tabName = (po.dtbsSourceTabNameSqlpad ? po.dtbsSourceTabNameSqlpad : (po.dtbsSourceTabNameSqlpad = $.uid("dtbsSourceTabNameSqlpad")));
						po.showDtbsSourceTab(dtbsSourceNode.dtbsSource, tabName, "<@spring.message code='module.sqlpad' />", po.dtbsSourceTabTypeSqlpad);
					});
				}
			},
			{
				label: "<@spring.message code='module.importData' />",
				command: function()
				{
					po.executeOnFirstAwareDtbsSourceNode(function(dtbsSourceNode)
					{
						var tabName = (po.dtbsSourceTabNameImportData ? po.dtbsSourceTabNameImportData : (po.dtbsSourceTabNameImportData = $.uid("dtbsSourceTabNameImportData")));
						po.showDtbsSourceTab(dtbsSourceNode.dtbsSource, tabName, "<@spring.message code='module.importData' />", po.dtbsSourceTabTypeImportData);
					});
				}
			},
			{
				label: "<@spring.message code='module.exportData' />",
				command: function()
				{
					po.executeOnFirstAwareDtbsSourceNode(function(dtbsSourceNode)
					{
						var tabName = (po.dtbsSourceTabNameExportData ? po.dtbsSourceTabNameExportData : (po.dtbsSourceTabNameExportData = $.uid("dtbsSourceTabNameExportData")));
						po.showDtbsSourceTab(dtbsSourceNode.dtbsSource, tabName, "<@spring.message code='module.exportData' />", po.dtbsSourceTabTypeExportData);
					});
				}
			},
			{ separator: true },
			{
				label: "<@spring.message code='refresh' />",
				command: function()
				{
					po.executeOnFirstAwareDtbsSourceNode(function(dtbsSourceNode)
					{
						po.loadTableNodes(dtbsSourceNode, 1, true);
					});
				}
			},
			{
				label: "<@spring.message code='reload' />",
				command: function()
				{
					po.loadDtbsSourceNodes();
				}
			},
			{
				label: "<@spring.message code='dtbsSource.dbinfo' />",
				command: function()
				{
					po.executeOnFirstAwareDtbsSourceNode(function(dtbsSourceNode)
					{
						po.handleAddAction("/dtbsSource/dbinfo?id="+encodeURIComponent(dtbsSourceNode.dtbsSourceId));
					});
				}
			}
		],
		dtbsSourceTabMenuItems:
		[
			{
				label: "<@spring.message code='close' />",
				command: function()
				{
					po.tabviewClose(po.vuePageModel().dtbsSourceTabs, po.dtbsSourceTabMenuOnTabId);
				}
			},
			{
				label: "<@spring.message code='closeOther' />",
				command: function()
				{
					po.tabviewCloseOther(po.vuePageModel().dtbsSourceTabs, po.dtbsSourceTabMenuOnTabId);
				}
			},
			{
				label: "<@spring.message code='closeRight' />",
				command: function()
				{
					po.tabviewCloseRight(po.vuePageModel().dtbsSourceTabs, po.dtbsSourceTabMenuOnTabId);
				}
			},
			{
				label: "<@spring.message code='closeLeft' />",
				command: function()
				{
					po.tabviewCloseLeft(po.vuePageModel().dtbsSourceTabs, po.dtbsSourceTabMenuOnTabId);
				}
			},
			{
				label: "<@spring.message code='closeAll' />",
				command: function()
				{
					po.tabviewCloseAll(po.vuePageModel().dtbsSourceTabs);
				}
			},
			{ separator: true },
			{
				label: "<@spring.message code='openInNewWindow' />",
				command: function()
				{
					po.tabviewOpenInNewWindow(po.vuePageModel().dtbsSourceTabs, po.dtbsSourceTabMenuOnTabId);
				}
			},
			{
				label: "<@spring.message code='viewTableStructure' />",
				visible: function()
				{
					var tab = po.tabviewTab(po.vuePageModel().dtbsSourceTabs, po.dtbsSourceTabMenuOnTabId);
					return (tab && tab.type == po.dtbsSourceTabTypeTable);
				},
				command: function()
				{
					var tab = po.tabviewTab(po.vuePageModel().dtbsSourceTabs, po.dtbsSourceTabMenuOnTabId);
					if(tab && tab.type == po.dtbsSourceTabTypeTable)
					{
						po.open("/dtbsSource/"+encodeURIComponent(tab.dtbsSourceId)+"/tableMeta/"+encodeURIComponent(tab.name));
					}
				}
			},
			{
				label: "<@spring.message code='refreshTableStructure' />",
				visible: function()
				{
					var tab = po.tabviewTab(po.vuePageModel().dtbsSourceTabs, po.dtbsSourceTabMenuOnTabId);
					return (tab && tab.type == po.dtbsSourceTabTypeTable);
				},
				command: function()
				{
					var tab = po.tabviewTab(po.vuePageModel().dtbsSourceTabs, po.dtbsSourceTabMenuOnTabId);
					if(tab && tab.type == po.dtbsSourceTabTypeTable)
					{
						var url = po.toDtbsSourceReloadTableUrl(tab.url);
						po.loadDtbsSourceTab(tab, true, url);
					}
				}
			}
		]
	});
	
	po.vueRef("${pid}searchTypeMenuEle", null);
	po.vueRef("${pid}dtbsSourceTabMenuEle", null);
	po.vueRef("${pid}dtbsSourceOptMenuEle", null);
	
	po.vueMethod(
	{
		onSearch: function()
		{
			po.searchByType();
		},
		
		onDtbsSourceNodeExpand: function(node)
		{
			if(node.children == null)
			{
				po.loadTableNodes(node);
			}
		},
		
		onDtbsSourceNodeSelect: function(node)
		{
			if(node.dataType == "dtbsSourceLoadMore")
			{
				po.loadDtbsSourceNodes(node.nextPage);
			}
			else if(node.dataType == "tableLoadMore")
			{
				var myDtbsSourceNode = po.findDtbsSourceNode(node.dtbsSourceId);
				po.loadTableNodes(myDtbsSourceNode, node.nextPage);
			}
			else if(node.dataType == "table")
			{
				var myDtbsSourceNode = po.findDtbsSourceNode(node.dtbsSourceId);
				po.showDtbsSourceTab(myDtbsSourceNode.dtbsSource, node.tableName, node.tableName, po.dtbsSourceTabTypeTable);
			}
		},
		
		onDtbsSourceTabChange: function(e){},
		
		onDtbsSourceTabClick: function(e){},
		
		onToggleSearchTypeMenu: function(e)
		{
			po.vueUnref("${pid}searchTypeMenuEle").toggle(e);
		},
		
		onToggleDtbsSourceOptMenu: function(e)
		{
			po.vueUnref("${pid}dtbsSourceOptMenuEle").toggle(e);
		},
		
		onDtbsSourceTabMenuToggle: function(e, tab)
		{
			e.stopPropagation();
			po.vueUnref("${pid}dtbsSourceTabMenuEle").hide();

			//直接show会导致面板还停留在上一个元素上
			po.vueNextTick(function()
			{
				po.dtbsSourceTabMenuOnTabId = tab.id;
				po.vueUnref("${pid}dtbsSourceTabMenuEle").show(e);
			});
		},
		
		onAdd: function()
		{
			po.handleAddAction("/dtbsSource/add");
		},
		
		onEdit: function()
		{
			po.handleOpenOfAction("/dtbsSource/edit");
		},
		
		onView: function()
		{
			po.handleOpenOfAction("/dtbsSource/view");
		},
		
		onDelete: function()
		{
			po.handleDeleteAction("/dtbsSource/delete");
		}
	});
	
	//po.showDtbsSourceTab()里不能里可获取到创建的DOM元素，所以采用此方案
	po.vueWatch(po.vuePageModel().dtbsSourceTabs, function(newVal, oldVal)
	{
		var items = newVal.items;
		var activeIndex = newVal.activeIndex;
		var activeTab = items[activeIndex];
		
		if(activeTab)
		{
			po.vueNextTick(function()
			{
				po.loadDtbsSourceTab(activeTab);
			});
		}
	});
	
	po.vueMounted(function()
	{
		po.loadDtbsSourceNodes();
	});
})
(${pid});
</script>
<#include "../include/page_vue_mount.ftl">
</body>
</html>
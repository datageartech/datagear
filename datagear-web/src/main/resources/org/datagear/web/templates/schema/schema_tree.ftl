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
	<@spring.message code='module.schema' />
	<#include "../include/html_request_action_suffix.ftl">
</title>
</head>
<body class="p-card no-border">
<#include "../include/page_obj.ftl">
<div id="${pid}" class="page page-query">
	<div class="grid grid-nogutter m-0 flex-nowrap">
		<div class="col-3">
			<div class="flex flex-column m-0">
				<div class="page-header">
					<div class="grid align-items-center flex-nowrap">
						<div class="col">
							<form @submit.prevent="onSearch" class="py-1">
								<div class="p-inputgroup">
									<p-inputtext type="text" v-model="pm.searchForm.keyword"></p-inputtext>
									<p-button type="submit" icon="pi pi-search" />
								</div>
							</form>
						</div>
						<div class="col-fixed text-right">
							<p-splitbutton icon="pi pi-plus" @click="onAdd" :model="schemaOptItems"></p-splitbutton>
						</div>
					</div>
				</div>
				<div class="page-content flex-grow-1 p-0 overflow-auto">
					<p-tree :value="pm.schemaNodes"
						selection-mode="multiple" v-model:selection-keys="pm.selectedNodes"
						@node-expand="onSchemaNodeExpand" @node-select="ononSchemaNodeSelect"
						:loading="pm.loadingSchema">
					</p-tree>
				</div>
			</div>
		</div>
		<div class="schema-table-wrapper col-9 overflow-auto">
		</div>
	</div>
</div>
<#include "../include/page_table.ftl">
<script>
(function(po)
{
	po.vuePageModel(
	{
		searchForm:{ keyword: "" },
		searchType: "schema",
		loadingSchema: false,
		schemaNodes: null,
		selectedNodes: null
	});

	po.vueReactive("schemaOptItems",
	[
		{
			label: "<@spring.message code='edit' />"
		},
		{
			label: "<@spring.message code='delete' />"
		}
	]);
	
	po.loadSchemaNodes = function()
	{
		var pm = po.vuePageModel();
		var keyword = pm.searchForm.keyword;
		
		pm.loadingSchema = true;
		$.ajaxJson(po.concatContextPath("/schema/queryData"),
		{
			data: { keyword: keyword },
			success: function(response)
			{
				pm.schemaNodes = po.schemasToNodes(response);
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
		
		var pm = po.vuePageModel();
		var keyword = pm.searchForm.keyword;
		
		pm.loadingSchema = true;
		$.ajaxJson(po.concatContextPath("/schema/"+schemaNode.schemaId+"/pagingQueryTable"),
		{
			data: { keyword: "", pageSize: 100, page: page },
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
	
	po.schemasToNodes = function(schemas)
	{
		var re = [];
		
		schemas.forEach(function(schema)
		{
			re.push(
			{
				key: schema.id,
				label: schema.title,
				icon: "pi pi-database",
				leaf: false,
				dataType: "schema",
				schemaId: schema.id
			});
		});
		
		return re;
	};
	
	po.findSchemaNode = function(schemaId)
	{
		var pm = po.vuePageModel();
		var schemaNodes = (pm.schemaNodes || []);
		for(var i=0; i<schemaNodes.length; i++)
		{
			if(schemaNodes[i].schemaId == schemaId)
				return schemaNodes[i];
		}
		
		return null;
	};
	
	po.tablePagingDataToNodes = function(schemaId, pagingData)
	{
		var re = [];
		
		pagingData.items.forEach(function(table)
		{
			re.push(
			{
				key: table.name,
				label: table.name,
				icon: "pi pi-table",
				leaf: true,
				dataType: "table",
				schemaId: schemaId
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
	
	po.vueMethod(
	{
		onSearch: function()
		{
			var pm = po.vuePageModel();
			
			if(pm.searchType == "schema")
				po.loadSchemaNodes();
			else if(pm.searchType == "table")
				po.loadSchemaNodes();
		},
		onSchemaNodeExpand: function(node)
		{
			if(node.children == null)
			{
				po.loadTableNodes(node);
			}
		},
		ononSchemaNodeSelect: function(node)
		{
			if(node.dataType == "loadMore")
			{
				var mySchemaNode = po.findSchemaNode(node.schemaId);
				po.loadTableNodes(mySchemaNode, node.nextPage);
			}
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
	
	po.vueMounted(function()
	{
		po.loadSchemaNodes();
	});
	
	po.vueMount();
})
(${pid});
</script>
</body>
</html>
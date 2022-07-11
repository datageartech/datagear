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
							<#include "../include/page_search_form.ftl">
						</div>
						<div class="col-fixed text-right">
							<p-splitbutton icon="pi pi-plus" @click="onAdd" :model="pm.schemaOptItems"></p-splitbutton>
						</div>
					</div>
				</div>
				<div class="page-content flex-grow-1 p-0">
					<p-tree :value="pm.schemaNodes" @node-expand="onExpandSchemaNode" :loading="pm.loading" class="h-full overflow-auto"></p-tree>
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
	po.loadSchemaList = function()
	{
		var pm = po.vuePageModel();
		pm.loading = true;
		
		var options =
		{
			success: function(response)
			{
				pm.schemaNodes = [];
				response.forEach(function(schema)
				{
					pm.schemaNodes.push(po.schemaToNode(schema));
				});
			},
			complete: function()
			{
				pm.loading = false;
			}
		};
		
		$.ajaxJson(po.concatContextPath("/schema/queryData"), options);
	};
	
	po.loadTableList = function(schemaNode)
	{
		var pm = po.vuePageModel();
		pm.loading = true;
		
		var options =
		{
			data: { keyword: "" },
			success: function(response)
			{
				var items = response.items;
				
				schemaNode.children = [];
				items.forEach(function(item)
				{
					schemaNode.children.push(po.schemaTableToNode(item));
				});
			},
			complete: function()
			{
				pm.loading = false;
			}
		};
		
		$.ajaxJson(po.concatContextPath("/schema/"+schemaNode.key+"/pagingQueryTable"), options);
	};
	
	po.schemaToNode = function(schema)
	{
		var node =
		{
			key: schema.id,
			label: schema.title,
			icon: "pi pi-database",
			leaf: false
		};
		
		return node;
	};
	
	po.schemaTableToNode = function(table)
	{
		var node =
		{
			key: table.name,
			label: table.name,
			icon: "pi pi-table",
			leaf: true
		};
		
		return node;
	};
	
	po.vuePageModel(
	{
		schemaOptItems:
		[
			{
				label: "<@spring.message code='edit' />"
			},
			{
				label: "<@spring.message code='delete' />"
			}
		],
		
		loading: false,
		
		schemaNodes: null
	});
	
	po.vueMethod(
	{
		onExpandSchemaNode: function(node)
		{
			if(node.children == null)
			{
				po.loadTableList(node);
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
		po.loadSchemaList();
	});
	
	po.vueMount();
})
(${pid});
</script>
</body>
</html>
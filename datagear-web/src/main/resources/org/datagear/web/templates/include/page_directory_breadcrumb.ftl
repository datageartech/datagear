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
目录层级导航栏。
-->
<div id="${pid}dirBreadcrumbWrapper" class="flex align-items-center" style="min-height:2em;">
	<p-breadcrumb :home="pm.dirBreadcrumb.home" :model="pm.dirBreadcrumb.items" class="py-1 border-0">
		<template #separator> / </template>
	</p-breadcrumb>
</div>
<script>
(function(po)
{
	po.handleDirBreadcrumbCommand = function(item){ /*需实现*/ };
	
	po.updateDirBreadcrumb = function(path)
	{
		var items = [];
		var nodes = $.splitAsPath(path, false);
		
		var nodePath = "";
		for(var i=0; i<nodes.length; i++)
		{
			nodePath = nodePath + (nodePath == "" ? "" : "/") + nodes[i];
			items[i] = { label: nodes[i], path: nodePath, command: function(e){ po.handleDirBreadcrumbCommand(e.item); } };
		}
		
		var pm = po.vuePageModel();
		pm.dirBreadcrumb.items = items;
	};
	
	po.vuePageModel(
	{
		dirBreadcrumb:
		{
			home:
			{
				label: "<@spring.message code='rootDirectory' />",
				icon: "pi pi-home pr-1",
				path: "",
				command: function(e){ po.handleDirBreadcrumbCommand(e.item); }
			},
			items: []
		}
	});
	
	po.vueMethod(
	{
		
	});
})
(${pid});
</script>

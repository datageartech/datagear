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
文件目录搜索表单。
-->
<#assign AbstractController=statics['org.datagear.web.controller.AbstractController']>
<#assign DirectoryQuery=statics['org.datagear.util.dirquery.DirectoryQuery']>
<form id="${pid}searchForm" @submit.prevent="onSearchFormSubmit" class="py-1">
	<div class="p-inputgroup">
		<p-inputtext type="text" v-model="pm.searchForm.keyword" maxlength="100"></p-inputtext>
		<p-button type="button" :label="pm.dirQueryRangeLabel"
			 aria-haspopup="true" aria-controls="${pid}dirQueryRangeMenu"
			@click="onToggleDirQueryRangeMenu">
		</p-button>
		<p-button type="submit" icon="pi pi-search" class="px-4"></p-button>
	</div>
	<p-menu id="${pid}dirQueryRangeMenu" ref="${pid}dirQueryRangeMenuEle" :model="pm.dirQueryRangeMenuItems" :popup="true"></p-menu>
</form>
<script>
(function(po)
{
	po.search = function(formData){ /*需实现*/ };
	
	//整合page_directory_breadcrumb.ftl功能
	po.handleDirBreadcrumbCommand = function(item)
	{
		po.submitSearchFormForQueryPath(item.path);
	};
	
	po.submitSearchForm = function()
	{
		var param = po.vueRaw(po.vuePageModel().searchForm);
		po.search(param);
	};
	
	po.submitSearchFormForQueryPath = function(path)
	{
		po.updateDirQueryPath(path);
		po.submitSearchForm();
	};
	
	po.getDirQueryPath = function()
	{
		var pm = po.vuePageModel();
		return (pm.searchForm["path"] || "");
	};
	
	po.updateDirQueryRangeForMenuItem = function(menuItem)
	{
		var pm = po.vuePageModel();
		pm.searchForm["queryRange"] = menuItem.value;
		pm.dirQueryRangeLabel = menuItem.label;
	};
	
	po.updateDirQueryPath = function(path)
	{
		var pm = po.vuePageModel();
		pm.searchForm["path"] = path;
		
		//整合page_directory_breadcrumb.ftl功能
		if(po.updateDirBreadcrumb)
			po.updateDirBreadcrumb(path);
	};
	
	po.vuePageModel(
	{
		searchForm: { keyword: "", "queryRange": "${DirectoryQuery.QUERY_RANGE_CHILDREN}" },
		dirQueryRangeLabel: "<@spring.message code='selfLevel' />",
		dirQueryRangeMenuItems:
		[
			{
				label: "<@spring.message code='selfLevel' />",
				value: "${DirectoryQuery.QUERY_RANGE_CHILDREN}",
				command: function(e)
				{
					po.updateDirQueryRangeForMenuItem(e.item);
					po.submitSearchForm();
				}
			},
			{
				label: "<@spring.message code='subLevel' />",
				value: "${DirectoryQuery.QUERY_RANGE_DESCENDANT}",
				command: function(e)
				{
					po.updateDirQueryRangeForMenuItem(e.item);
					po.submitSearchForm();
				}
			}
		]
	});
	
	po.vueRef("${pid}dirQueryRangeMenuEle", null);
	
	po.vueMethod(
	{
		onSearchFormSubmit: function()
		{
			po.submitSearchForm();
		},
		
		submitSearchFormForQueryPath: function(path)
		{
			po.submitSearchFormForQueryPath(path);
		},
		
		onToggleDirQueryRangeMenu: function(e)
		{
			po.vueUnref("${pid}dirQueryRangeMenuEle").toggle(e);
		}
	});
})
(${pid});
</script>

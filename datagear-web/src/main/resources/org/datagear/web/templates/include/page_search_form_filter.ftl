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
"我的"、"分享的"、"全部"过滤搜索表单。
-->
<#assign AbstractController=statics['org.datagear.web.controller.AbstractController']>
<#assign DataPermissionEntityService=statics['org.datagear.management.service.DataPermissionEntityService']>
<form id="${pid}searchForm" @submit.prevent="onSearchFormSubmit" class="py-1">
	<div class="p-inputgroup">
		<p-inputtext type="text" v-model="pm.searchForm.keyword" maxlength="100"></p-inputtext>
		<p-button type="button" :label="pm.searchFilterLabel"
			 aria-haspopup="true" aria-controls="${pid}searchFilterMenu"
			@click="onToggleSearchFilterMenu">
		</p-button>
		<p-button type="submit" icon="pi pi-search" class="px-4"></p-button>
	</div>
	<p-menu id="${pid}searchFilterMenu" ref="${pid}searchFilterMenuEle" :model="pm.searchFilterMenuItems" :popup="true"></p-menu>
</form>
<script>
(function(po)
{
	po.search = function(formData){ /*需实现*/ };
	
	po.submitSearchForm = function()
	{
		var param = po.vueRaw(po.vuePageModel().searchForm);
		po.search(param);
	};
	
	po.updateSearchFilterForMenuItem = function(menuItem)
	{
		var pm = po.vuePageModel();
		pm.searchForm["${AbstractController.DATA_FILTER_PARAM}"] = menuItem.value;
		pm.searchFilterLabel = menuItem.label;
	};
	
	po.vuePageModel(
	{
		searchForm: { keyword: "", "${AbstractController.DATA_FILTER_PARAM}": "${DataPermissionEntityService.DATA_FILTER_VALUE_ALL}" },
		searchFilterLabel: "<@spring.message code='searchFilter.all' />",
		searchFilterMenuItems:
		[
			{
				label: "<@spring.message code='searchFilter.all' />",
				value: "${DataPermissionEntityService.DATA_FILTER_VALUE_ALL}",
				command: function()
				{
					po.updateSearchFilterForMenuItem(this);
					po.submitSearchForm();
				}
			},
			{
				label: "<@spring.message code='searchFilter.mine' />",
				value: "${DataPermissionEntityService.DATA_FILTER_VALUE_MINE}",
				command: function()
				{
					po.updateSearchFilterForMenuItem(this);
					po.submitSearchForm();
				}
			},
			{
				label: "<@spring.message code='searchFilter.other' />",
				value: "${DataPermissionEntityService.DATA_FILTER_VALUE_OTHER}",
				command: function()
				{
					po.updateSearchFilterForMenuItem(this);
					po.submitSearchForm();
				}
			}
		]
	});
	
	po.vueRef("${pid}searchFilterMenuEle", null);
	
	po.vueMethod(
	{
		onSearchFormSubmit: function()
		{
			po.submitSearchForm();
		},
		
		onToggleSearchFilterMenu: function(e)
		{
			po.vueUnref("${pid}searchFilterMenuEle").toggle(e);
		}
	});
})
(${pid});
</script>

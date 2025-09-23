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
带有下拉过滤条件的搜索表单。
-->
<#assign AbstractController=statics['org.datagear.web.controller.AbstractController']>
<form id="${pid}searchForm" @submit.prevent="onSearchFormSubmit" class="py-1">
	<div class="p-inputgroup">
		<p-inputtext type="text" v-model="pm.searchForm.keyword" maxlength="100"></p-inputtext>
		<p-button type="button" :label="pm.searchFilterLabel" :title="pm.earchDropdownBtnTitle"
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
	
	po.searchFormFilterName = "${AbstractController.DATA_FILTER_PARAM}";
	
	po.submitSearchForm = function()
	{
		var pm = po.vuePageModel();
		var param = po.vueRaw(pm.searchForm);
		po.search(param);
	};
	
	po.updateSearchFilterForMenuItem = function(menuItem)
	{
		var pm = po.vuePageModel();
		pm.searchForm[po.searchFormFilterName] = menuItem.value;
		pm.searchFilterLabel = menuItem.label;
	};
	
	po.initDropdownFilterSearchForm = function(filterMenuItems, options)
	{
		options = (options || {});
		options.dftItemIndex = (options.dftItemIndex == null || options.dftItemIndex < 0 ? 0 : options.dftItemIndex);
		options.dropdownBtnTitle = (options.dropdownBtnTitle ? options.dropdownBtnTitle : "");
		
		filterMenuItems.forEach((item) =>
		{
			if(item.command != null)
				return;
			
			item.command = function(e)
			{
				po.updateSearchFilterForMenuItem(e.item);
				po.submitSearchForm();
			}
		});
		
		var dftMenuItem = filterMenuItems[options.dftItemIndex];
		var searchForm = { keyword: "" };
		searchForm[po.searchFormFilterName] = dftMenuItem.value;
		
		po.vuePageModel(
		{
			searchForm: searchForm,
			searchFilterLabel: dftMenuItem.label,
			searchFilterMenuItems: filterMenuItems,
			earchDropdownBtnTitle: options.dropdownBtnTitle
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
	};
})
(${pid});
</script>

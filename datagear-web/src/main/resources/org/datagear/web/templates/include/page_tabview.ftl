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
选项卡JS片段。
-->
<script>
(function(po)
{
	po.tabviewTabActive = function(tabViewModel, tabId)
	{
		var items = tabViewModel.items;
		var idx = $.inArrayById(items, tabId);
		
		if(idx >= 0)
			tabViewModel.activeIndex = idx;
		
		return idx;
	};
	
	po.tabviewTabIndex = function(tabViewModel, tabId)
	{
		var items = tabViewModel.items;
		var idx = $.inArrayById(items, tabId);
		return idx;
	};
	
	po.tabviewTab = function(tabViewModel, tabId)
	{
		var items = tabViewModel.items;
		var idx = $.inArrayById(items, tabId);
		
		if(idx < 0)
			return null;
		
		return items[idx];
	};
	
	po.tabviewClose = function(tabViewModel, tabId)
	{
		var items = tabViewModel.items;
		var idx = $.inArrayById(items, tabId);
		
		if(idx < 0)
			return;
		
		po.removeTabItems(tabViewModel, idx, 1);
		
		if(idx <= tabViewModel.activeIndex)
			tabViewModel.activeIndex = (tabViewModel.activeIndex > 0 ? tabViewModel.activeIndex - 1 : 0);
	};
	
	po.tabviewCloseOther = function(tabViewModel, tabId)
	{
		po.tabviewCloseLeft(tabViewModel, tabId);
		po.tabviewCloseRight(tabViewModel, tabId);
		
		tabViewModel.activeIndex = 0;
	};
	
	po.tabviewCloseRight = function(tabViewModel, tabId)
	{
		var items = tabViewModel.items;
		var idx = $.inArrayById(items, tabId);
		
		if(idx < 0)
			return;
		
		var count = ((items.length - idx - 1) > 0 ? (items.length - idx - 1) : 0);
		po.removeTabItems(tabViewModel, idx+1, count);
		
		tabViewModel.activeIndex = idx;
	};
	
	po.tabviewCloseLeft = function(tabViewModel, tabId)
	{
		var items = tabViewModel.items;
		var idx = $.inArrayById(items, tabId);
		
		if(idx < 0)
			return;
		
		po.removeTabItems(tabViewModel, 0, idx);
		tabViewModel.activeIndex = tabViewModel.activeIndex - idx;
	};
	
	po.tabviewCloseAll = function(tabViewModel)
	{
		po.removeTabItems(tabViewModel, 0, tabViewModel.items.length);
		tabViewModel.activeIndex = 0;
	};
	
	po.tabviewOpenInNewWindow = function(tabViewModel, tabId)
	{
		var items = tabViewModel.items;
		var idx = $.inArrayById(items, tabId);
		
		if(idx < 0)
			return;
		
		if(items[idx] && items[idx].url)
			window.open(items[idx].url);
	};
	
	po.removeTabItems = function(tabViewModel, index, count)
	{
		var items = tabViewModel.items;
		
		var removeIdx = index;
		for(var i=0; i<count; i++)
		{
			var item = items[removeIdx];
			if(item.closeable !== false)
				items.splice(removeIdx, 1);
			else
				removeIdx += 1;
		}
	};
})
(${pid});
</script>

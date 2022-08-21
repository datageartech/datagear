<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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

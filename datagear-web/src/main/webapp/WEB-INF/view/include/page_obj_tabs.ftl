<#--
选项卡JS片段。

依赖：
page_js_obj.jsp
-->
<script type="text/javascript">
(function(po)
{
	po.getTabsNav = function($tabs)
	{
		return $("> .ui-tabs-nav", $tabs);
	};
	
	po.getTabsTabByTabId = function($tabs, $tabsNav, tabId)
	{
		var $a = $("a.ui-tabs-anchor[href='#"+tabId+"']", $tabsNav);
		return $a.parent();
	};
	
	po.getTabsTabId = function($tabs, $tabsNav, $tab)
	{
		var tabId = $("> a.ui-tabs-anchor", $tab).attr("href");
		
		if(tabId.charAt(0) == "#")
			tabId = tabId.substr(1);
		
		return tabId;
	};
	
	po.getTabsTabPanelByTabId = function($tabs, tabId)
	{
		return $("> #"+tabId, $tabs);
	};
	
	po.getTabsTabMoreOperationMenuWrapper = function($tabs)
	{
		return $("> .tabs-more-operation-menu-wrapper", $tabs);
	};
	
	po.getTabsTabMoreOperationMenu = function($tabs)
	{
		var wrapper = po.getTabsTabMoreOperationMenuWrapper($tabs);
		return $("> .tabs-more-operation-menu", wrapper);
	};
	
	po.getTabsMoreTabMenuWrapper = function($tabs)
	{
		return $("> .tabs-more-tab-menu-wrapper", $tabs);
	};
	
	po.getTabsMoreTabMenu = function($tabs)
	{
		var wrapper = po.getTabsMoreTabMenuWrapper($tabs);
		return $("> .tabs-more-tab-menu", wrapper);
	};
	
	po.closeTab = function($tabs, $tabsNav, $tab)
	{
		var tabId = po.getTabsTabId($tabs, $tabsNav, $tab);
    	
		po.getTabsTabPanelByTabId($tabs, tabId).remove();
    	$tab.remove();
    	
    	$tabs.tabs("refresh");
    	
    	po.refreshTabsNavForHidden($tabs, $tabsNav);
    	
		if($("> li.ui-tabs-tab", $tabsNav).length == 0)
			$tabsNav.hide();
	};
	
	po.showTabMoreOperationMenu = function($tabs, $tabsNav, $tab, $positionOf)
	{
		var menuWrapper = po.getTabsTabMoreOperationMenuWrapper($tabs);
		var menu = $("> ul", menuWrapper);
		
		var tabId = po.getTabsTabId($tabs, $tabsNav, $tab);
		menu.attr("tab-id", tabId);
		
		menuWrapper.show().css("left", "0px").css("top", "0px")
    		.position({"my" : "left top+1", "at": "right bottom", "of" : $positionOf, "collision": "flip flip"});
    	
    	var menuItemDisabled = {};
    	
    	var hasPrev = ($tab.prev().length > 0);
    	var hasNext = ($tab.next().length > 0);
    	
    	menuItemDisabled[".tab-operation-close-left"] = !hasPrev;
    	menuItemDisabled[".tab-operation-close-right"] = !hasNext;
    	menuItemDisabled[".tab-operation-close-other"] = !hasPrev && !hasNext;
    	
    	for(var selector in menuItemDisabled)
    	{
    		if(menuItemDisabled[selector])
    			$(selector, menu).addClass("ui-state-disabled");
    		else
    			$(selector, menu).removeClass("ui-state-disabled");
    	}
    	
    	return menu;
	};
	
	po.handleTabMoreOperationMenuSelect = function($menu, $menuItem, $tabs)
	{
		var tabId = $menu.attr("tab-id");
		
		var tabsNav = po.getTabsNav($tabs);
		var tab = po.getTabsTabByTabId($tabs, tabsNav, tabId);
		
		if($menuItem.hasClass("tab-operation-close-left"))
		{
			var prev;
			while((prev = tab.prev()).length > 0)
			{
				var preTabId = po.getTabsTabId($tabs, tabsNav, prev);
				
				po.getTabsTabPanelByTabId($tabs, preTabId).remove();
				prev.remove();
			}
			
			$tabs.tabs("refresh");
		}
		else if($menuItem.hasClass("tab-operation-close-right"))
		{
			var next;
			while((next = tab.next()).length > 0)
			{
				var nextTabId = po.getTabsTabId($tabs, tabsNav, next);
				
				po.getTabsTabPanelByTabId($tabs, nextTabId).remove();
				next.remove();
			}
			
			$tabs.tabs("refresh");
		}
		else if($menuItem.hasClass("tab-operation-close-other"))
		{
			$("> li.ui-tabs-tab", tabsNav).each(function()
			{
				if(tab[0] == this)
					return;
				
				var li = $(this);
				
				var tabId = po.getTabsTabId($tabs, tabsNav, li);
				
				po.getTabsTabPanelByTabId($tabs, tabId).remove();
				li.remove();
			});
			
			$tabs.tabs("refresh");
		}
		else if($menuItem.hasClass("tab-operation-close-all"))
		{
			$("li", tabsNav).each(function()
			{
				var li = $(this);
				
				var tabId = po.getTabsTabId($tabs, tabsNav, li);

				po.getTabsTabPanelByTabId($tabs, tabId).remove();
				li.remove();
			});
			
			$tabs.tabs("refresh");
		}
		
    	po.refreshTabsNavForHidden($tabs, tabsNav);
    	
		if($("> li.ui-tabs-tab", tabsNav).length == 0)
			tabsNav.hide();
	};
	
	po.refreshTabsNavForHidden = function($tabs, $tabsNav, $activeTab)
	{
		if($activeTab == undefined)
			$activeTab = $("> li.ui-tabs-active", $tabsNav);
		
		$("> li.ui-tabs-tab", $tabsNav).show();
		
		if($activeTab && $activeTab.length > 0)
		{
			//如果卡片不可见，则向前隐藏卡片，直到此卡片可见
			
			var tabsNavHeight = $tabsNav.height();
			
			var activeTabPosition;
			var prevHidden = $activeTab.prev();
			while((activeTabPosition = $activeTab.position()).top >= tabsNavHeight)
			{
				prevHidden.hide();
				prevHidden = prevHidden.prev();
			}
		}
		
		var showHiddenButton = $("> .tabs-more-tab-button", $tabs);
		
		if(po.getTabsHiddens($tabs, $tabsNav).length > 0)
		{
			if(showHiddenButton.length == 0)
			{
				showHiddenButton = $("<button class='ui-button ui-corner-all ui-widget ui-button-icon-only tabs-more-tab-button'><span class='ui-icon ui-icon-triangle-1-s'></span></button>")
					.appendTo($tabs);
				
				showHiddenButton.click(function()
				{
					var $this= $(this);
					
					var $tabs = $this.parent();
					var tabsNav = po.getTabsNav($tabs);
					
					var hiddens = po.getTabsHiddens($tabs, $tabsNav);
					
					var menuWrapper = $("> .tabs-more-tab-menu-wrapper", $tabs);
					var menu = $("> .tabs-more-tab-menu", menuWrapper);
					
					menu.empty();
					
					for(var i=0; i<hiddens.length; i++)
					{
						var tab = hiddens[i];
						
						var mi = $("<li />").appendTo(menu);
						mi.attr("nav-item-id", tab.attr("id"));
						$("<div />").html($(".ui-tabs-anchor", tab).text()).attr("title", tab.attr("title")).appendTo(mi);
					}
					
					menuWrapper.show().css("left", "0px").css("top", "0px")
	    	    		.position({"my" : "left top+1", "at": "right bottom", "of" : $this, "collision": "flip flip"});
	    	    	
					menu.menu("refresh");
				});
			}
			
			showHiddenButton.show();
		}
		else
			showHiddenButton.hide();
	};
	
	po.getTabsHiddens = function($tabs, $tabsNav)
	{
		var tabsNavHeight = $tabsNav.height();
		
		var hiddens = [];
		
		$("> li.ui-tabs-tab", $tabsNav).each(function()
		{
			var li = $(this);
			
			if(li.is(":hidden") || li.position().top >= tabsNavHeight)
				hiddens.push(li);
		});
		
		return hiddens;
	};
	
	po.handleTabsMoreTabMenuSelect = function($menu, $menuItem, $tabs)
	{
		var navItemId = $menuItem.attr("nav-item-id");
		
		var tabsNav = po.getTabsNav($tabs);
		
		var myIndex = po.element("> li[id='"+navItemId+"']", tabsNav).index();
		$tabs.tabs("option", "active",  myIndex);
	};
	
	po.bindTabsMenuHiddenEvent = function($tabs)
	{
		$(document.body).click(function(e)
		{
			var moreOperationMenuWrapper = po.getTabsTabMoreOperationMenuWrapper($tabs);
			var moreTabMenuWrapper = po.getTabsMoreTabMenuWrapper($tabs);
			
			var moreOperationMenuWrapperHidden = moreOperationMenuWrapper.is(":hidden");
			var moreTabMenuWrapperHidden = moreTabMenuWrapper.is("hidden");
			
			if(moreOperationMenuWrapperHidden && moreTabMenuWrapperHidden)
				return;
			else
			{
				var moreOperationMenuWrapperNotHide = false;
				var moreTabMenuWrapperNotHide = false;
				
				var target = $(e.target);
				
				while(target && target.length != 0)
				{
					if(target.hasClass("tabs-more-operation-button") || target.hasClass("tabs-more-operation-menu-wrapper"))
						moreOperationMenuWrapperNotHide = true;
					else if(target.hasClass("tabs-more-tab-button") || target.hasClass("tabs-more-tab-menu-wrapper"))
						moreTabMenuWrapperNotHide = true;
					
					target = target.parent();
				};
				
				if(!moreOperationMenuWrapperHidden && !moreOperationMenuWrapperNotHide)
					moreOperationMenuWrapper.hide();
				
				if(!moreTabMenuWrapperHidden && !moreTabMenuWrapperNotHide)
					moreTabMenuWrapper.hide();
			}
		});
	};
})
(${pageId});
</script>

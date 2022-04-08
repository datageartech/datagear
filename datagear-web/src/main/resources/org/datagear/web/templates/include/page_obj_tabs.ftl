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

依赖：
page_obj.ftl
-->
<script type="text/javascript">
(function(po)
{
	po.tabsGetNav = function($tabs)
	{
		return $("> .ui-tabs-nav", $tabs);
	};
	
	po.tabsGetTabCount = function($tabs)
	{
		var $tabsNav = po.tabsGetNav($tabs);
		return $("> li.ui-tabs-tab", $tabsNav).length;
	};
	
	po.tabsGetTabId = function($tabs, $tab)
	{
		var tabId = $("> a.ui-tabs-anchor", $tab).attr("href");
		
		if(tabId && tabId.charAt(0) == "#")
			tabId = tabId.substr(1);
		
		return tabId;
	};
	
	po.tabsGetPaneByTabId = function($tabs, tabId)
	{
		return $("> #"+tabId, $tabs);
	};
	
	po.tabsGetTabById = function($tabs, tabId)
	{
		var $tabsNav = po.tabsGetNav($tabs);
		var $a = $("a.ui-tabs-anchor[href='#"+tabId+"']", $tabsNav);
		return $a.parent();
	};
	
	po.tabsGetActiveTab = function($tabs)
	{
		var $tabsNav = po.tabsGetNav($tabs);
		return $("> li.ui-tabs-tab.ui-state-active", $tabsNav);
	};
	
	po.tabsGetActivePane = function($tabs)
	{
		var tab = po.tabsGetActiveTab($tabs);
		var tabId = po.tabsGetTabId($tabs, tab);
		
		return po.tabsGetPaneByTabId($tabs, tabId);
	};
	
	po.tabsGetTabMoreOptMenuWrapper = function($tabs)
	{
		return $("> .tabs-more-operation-menu-wrapper", $tabs);
	};
	
	po.tabsGetTabMoreOptMenu = function($tabs)
	{
		var wrapper = po.tabsGetTabMoreOptMenuWrapper($tabs);
		return $("> .tabs-more-operation-menu", wrapper);
	};
	
	po.tabsGetMoreTabMenuWrapper = function($tabs)
	{
		return $("> .tabs-more-tab-menu-wrapper", $tabs);
	};
	
	po.tabsGetMoreTabMenu = function($tabs)
	{
		var wrapper = po.tabsGetMoreTabMenuWrapper($tabs);
		return $("> .tabs-more-tab-menu", wrapper);
	};
	
	po.tabsCloseTab = function($tabs, $tab)
	{
		var $tabsNav = po.tabsGetNav($tabs);
		var tabId = po.tabsGetTabId($tabs, $tab);
    	
		po.tabsGetPaneByTabId($tabs, tabId).remove();
    	$tab.remove();
    	
    	$tabs.tabs("refresh");
    	
    	po.tabsRefreshNavForHidden($tabs);
    	
		if($("> li.ui-tabs-tab", $tabsNav).length == 0)
		{
			if(!$tabsNav.hasClass("always-show"))
				$tabsNav.hide();
		}
	};
	
	po.tabsShowMoreOptMenu = function($tabs, $tab, $positionOf)
	{
		var menuWrapper = po.tabsGetTabMoreOptMenuWrapper($tabs);
		var menu = $("> ul", menuWrapper);
		
		var tabId = po.tabsGetTabId($tabs, $tab);
		menu.attr("tab-id", tabId);
		
		menuWrapper.show().css("left", "0px").css("top", "0px")
    		.position({"my" : "left top+1", "at": "right bottom", "of" : $positionOf, "collision": "flip flip"});
    	
    	var menuItemDisabled = {};
    	
    	var hasPrev = ($tab.prevAll(":not(.not-closable)").length > 0);
    	var hasNext = ($tab.nextAll(":not(.not-closable)").length > 0);
    	
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
	
	po.tabsHandleMoreOptMenuSelect = function($menu, $menuItem, $tabs)
	{
		var tabId = $menu.attr("tab-id");
		
		var tabsNav = po.tabsGetNav($tabs);
		var tab = po.tabsGetTabById($tabs, tabId);
		
		if($menuItem.hasClass("tab-operation-close-left"))
		{
			tab.prevAll().each(function()
			{
				var prev = $(this);
				
				if(!prev.hasClass("not-closable"))
				{
					var preTabId = po.tabsGetTabId($tabs, prev);
					
					po.tabsGetPaneByTabId($tabs, preTabId).remove();
					prev.remove();
				}
			});
			
			$tabs.tabs("refresh");
		}
		else if($menuItem.hasClass("tab-operation-close-right"))
		{
			tab.nextAll().each(function()
			{
				var next = $(this);
				
				if(!next.hasClass("not-closable"))
				{
					var nextTabId = po.tabsGetTabId($tabs, next);
					
					po.tabsGetPaneByTabId($tabs, nextTabId).remove();
					next.remove();
				}
			});
			
			$tabs.tabs("refresh");
		}
		else if($menuItem.hasClass("tab-operation-close-other"))
		{
			$("> li.ui-tabs-tab", tabsNav).each(function()
			{
				if(tab[0] == this)
					return;
				
				var li = $(this);
				
				if(!li.hasClass("not-closable"))
				{
					var tabId = po.tabsGetTabId($tabs, li);
					
					po.tabsGetPaneByTabId($tabs, tabId).remove();
					li.remove();
				}
			});
			
			$tabs.tabs("refresh");
		}
		else if($menuItem.hasClass("tab-operation-close-all"))
		{
			$("> li", tabsNav).each(function()
			{
				var li = $(this);
				
				if(!li.hasClass("not-closable"))
				{
					var tabId = po.tabsGetTabId($tabs, li);
	
					po.tabsGetPaneByTabId($tabs, tabId).remove();
					li.remove();
				}
			});
			
			$tabs.tabs("refresh");
		}
		
    	po.tabsRefreshNavForHidden($tabs);
    	
		if($("> li.ui-tabs-tab", tabsNav).length == 0)
		{
			if(!tabsNav.hasClass("always-show"))
				tabsNav.hide();
		}
	};
	
	po.tabsRefreshNavForHidden = function($tabs, $activeTab)
	{
		var $tabsNav = po.tabsGetNav($tabs);
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
		
		if(po.tabsGetHideTabs($tabs).length > 0)
		{
			if(showHiddenButton.length == 0)
			{
				showHiddenButton = $("<button type='button' class='tabs-more-tab-button ui-button ui-corner-all ui-widget ui-button-icon-only'><span class='ui-icon ui-icon-triangle-1-s'></span></button>")
					.appendTo($tabs);
				
				showHiddenButton.click(function()
				{
					var $this= $(this);
					
					var $tabs = $this.parent();
					var tabsNav = po.tabsGetNav($tabs);
					
					var hiddens = po.tabsGetHideTabs($tabs);
					
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
	    	    		.position({"my" : "left top+1", "at": "right bottom", "of" : $this});
	    	    	
					menu.menu("refresh");
				});
			}
			
			showHiddenButton.show();
		}
		else
			showHiddenButton.hide();
	};
	
	po.tabsGetHideTabs = function($tabs)
	{
		var $tabsNav = po.tabsGetNav($tabs);
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
	
	po.tabsHandleMoreTabMenuSelect = function($menu, $menuItem, $tabs)
	{
		var navItemId = $menuItem.attr("nav-item-id");
		
		var tabsNav = po.tabsGetNav($tabs);
		
		var myIndex = po.element("> li[id='"+navItemId+"']", tabsNav).index();
		$tabs.tabs("option", "active",  myIndex);
	};
	
	po.tabsBindMenuHiddenEvent = function($tabs)
	{
		$(document.body).click(function(e)
		{
			var moreOperationMenuWrapper = po.tabsGetTabMoreOptMenuWrapper($tabs);
			var moreTabMenuWrapper = po.tabsGetMoreTabMenuWrapper($tabs);
			
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

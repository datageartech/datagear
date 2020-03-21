<#include "include/import_global.ftl">
<#include "include/html_doctype.ftl">
<#assign Global=statics['org.datagear.util.Global']>
<html>
<head>
<#include "include/html_head.ftl">
${detectNewVersionScript}
<title><@spring.message code='app.name' /></title>
<#include "include/page_js_obj.ftl" >
<#include "include/page_obj_tabs.ftl" >
<#include "include/page_obj_data_permission.ftl" >
<#include "include/page_obj_data_permission_ds_table.ftl" >
<script type="text/javascript">
(function(po)
{
	po.currentVersion = "${currentVersion?js_string}";
	po.currentUser = <@writeJson var=currentUser />;
	
	//将在document.ready中初始化
	po.mainTabs = null;
	
	po.workTabTemplate = "<li style='vertical-align:middle;'><a href='"+'#'+"{href}'>"+'#'+"{label}</a>"
			+"<div class='tab-operation'>"
			+"<span class='ui-icon ui-icon-close' title='<@spring.message code='close' />'>close</span>"
			+"<div class='tabs-more-operation-button' title='<@spring.message code='moreOperation' />'><span class='ui-icon ui-icon-caret-1-se'></span></div>"
			+"</div>"
			+"</li>";

	po.workTabTemplateWithSchema = "<li style='vertical-align:middle;'><a href='"+'#'+"{href}'>"+'#'+"{label}</a>"
			+"<div class='tab-operation'>"
			+"<span class='ui-icon ui-icon-close' title='<@spring.message code='close' />'>close</span>"
			+"<div class='tabs-more-operation-button' title='<@spring.message code='moreOperation' />'><span class='ui-icon ui-icon-caret-1-se'></span></div>"
			+"</div>"
			+"<div class='category-bar category-bar-"+'#'+"{schemaId}'></div>"
			+"</li>";
			
	po.activeWorkTab = function(tabId, tabLabel, tabTitle, url, schema)
	{
		tabLabel = $.truncateIf(tabLabel, "..", 20);
		
		var mainTabsNav = po.getTabsNav(po.mainTabs);
		var tab = po.getTabsTabByTabId(po.mainTabs, mainTabsNav, tabId);
		
	    if(tab.length > 0)
	    {
	    	po.mainTabs.tabs("option", "active",  tab.index());
	    }
	    else
	    {
	    	var tooltipId = $.tipInfo("<@spring.message code='loading' />", -1);
	    	
	    	$.ajax(
			{
				url : url, 
	    		success:function(data)
		    	{
	    			mainTabsNav.show();
		    		
	    			tab = po.getTabsTabByTabId(po.mainTabs, mainTabsNav, tabId);
	    			
		    		var tabPanel = po.getTabsTabPanelByTabId(po.mainTabs, tabId);
		    		
		    		//防止双击导致创建两次而引起界面错乱
		    		if(tab.length == 0)
		    		{
		    			if(schema != null)
		    			{
		    				tab = $(po.workTabTemplateWithSchema.replace( /#\{href\}/g, "#" + tabId)
			    					.replace(/#\{label\}/g, tabLabel).replace(/#\{schemaId\}/g, schema.id))
			    					.appendTo(mainTabsNav);
			    			tab.attr("schema-id", schema.id);
		    			}
		    			else
			    			tab = $(po.workTabTemplate.replace( /#\{href\}/g, "#" + tabId).replace(/#\{label\}/g, tabLabel))
			    					.appendTo(mainTabsNav);
		    			
		    			if(!tab.attr("id"))
		    				tab.attr("id", $.uid("${pageId}-mainTabs-li-"));
		    			
		    			tab.attr("tab-url", url);
		    			tab.attr("title", tabTitle);
		    		}
		    		
		    	    if(tabPanel.length == 0)
		    	    	tabPanel = $("<div id='" + tabId + "'></div>").appendTo(po.mainTabs);
		    	    
		    	    po.mainTabs.tabs("refresh").tabs( "option", "active",  tab.index());
		    	    
		    	    tabPanel.css("top", po.evalTabPanelTop(po.mainTabs));
		    	    tabPanel.html(data);
		    	    
		    	    $(".tab-operation .ui-icon-close", tab).click(function()
		    	    {
		    	    	po.closeTab(po.mainTabs, mainTabsNav, $(this).parent().parent());
		    	    });
		    	    
		    	    $(".tab-operation .tabs-more-operation-button", tab).click(function()
		    	    {
		    	    	var tab = $(this).parent().parent();
		    	    	var tabId = po.getTabsTabId(po.mainTabs, mainTabsNav, tab);
		    	    	
		    	    	var menu = po.showTabMoreOperationMenu(po.mainTabs, mainTabsNav, tab, $(this));
		    	    	
		    	    	menu.attr("tab-url", tab.attr("tab-url"))
		    	    		.attr("schema-id", tab.attr("schema-id"));
		    	    });
    			},
	        	complete : function()
	        	{
	        		$.closeTip(tooltipId);
	        	}
			});
	    }
	};
	
	po.toMainTabId = function(name)
	{
		var map = (po.genTabIdMap || (po.genTabIdMap = {}));
		
		//不直接使用name作为元素ID，因为name中可能存在与jquery冲突的字符，比如'$'
		var key = name;
		var value = map[key];
		
		if(value == undefined)
		{
			var nextNumber = (po.genTabIdNextNumber != undefined 
					? (po.genTabIdNextNumber = po.genTabIdNextNumber + 1) : (po.genTabIdNextNumber = 0));
			
			value = "${pageId}-mainTabs-tab-" + nextNumber;
			map[key] = value;
		}
		
		return value;
	};
	
	po.toMainTabIdForSchemaName = function(schemaId, tableName)
	{
		return po.toMainTabId(schemaId +"_" + tableName);
	};
	
	po.isSchemaNode = function(node)
	{
		if(!node)
			return false;
		
		var original = node.original;
		
		if(!original)
			return false;
		
		return (original.id != undefined && original.url != undefined);
	};
	
	po.getSchemaNode = function(jstree, node)
	{
		if(!node)
			return null;
		else if(po.isSchemaNode(node))
			return node;
		else
			return po.getSchemaNode(jstree, jstree.get_node(node.parent));
	};
	
	po.schemaToJstreeNode = function(schema)
	{
		schema.text = $.escapeHtml(schema.title);
		
		var tempSchema = (schema.createUser && schema.createUser.anonymous);
		
		if(schema.createUser)
		{
			if(po.currentUser.id == schema.createUser.id)
			{
				if(tempSchema)
					schema.text += " <span class='ui-icon ui-icon-notice' title='<@spring.message code='main.tempSchema' />'></span>";
			}
			else
			{
				schema.text += " <span class='schema-tree-create-user-label small-text ui-state-disabled' title='<@spring.message code='main.schemaCreateUser' />'>" + $.escapeHtml(schema.createUser.nameLabel) + "</span>";
			}
		}
		
		schema.children = true;
		
		return schema;
	};
	
	po.schemaToJstreeNodes = function(schemas)
	{
		for(var i=0; i<schemas.length; i++)
			po.schemaToJstreeNode(schemas[i]);
		
		return schemas;
	};

	po.isTableNode = function(node)
	{
		var original = node.original;
		
		return (original.name != undefined && original.type != undefined);
	};
	
	po.isTableView = function(tableInfo)
	{
		return (tableInfo.type == "VIEW");
	};
	
	po.tableToJstreeNode = function(table)
	{
		var text = table.name;
		
		table.text = $.escapeHtml(text);
		table.children = false;
		
		var licss = (po.isTableView(table) ? "view-node" : "table-node");
		table.li_attr = { "class" : licss };
		
		var atitle = (po.isTableView(table) ? "<@spring.message code='main.tableType.view' />" : "<@spring.message code='main.tableType.table' />")
					+"<@spring.message code='colon' />" + table.name;
		if(table.comment)
			atitle += "<@spring.message code='bracketLeft' />" + table.comment + "<@spring.message code='bracketRight' />";
		
		table.a_attr = { "title" :  atitle};
		
		return table;
	};
	
	po.tableToJstreeNodes = function(tables)
	{
		for(var i=0; i<tables.length; i++)
			po.tableToJstreeNode(tables[i]);
		
		return tables;
	};
	
	po.createNextPageNode = function(pagingData)
	{
		var showCount = (pagingData.page > 0 ? pagingData.page-1 : 0) * pagingData.pageSize
							+ (pagingData.items ? pagingData.items.length : 0);
		
		var nextPageNode =
		{
			"text" : "<span class='more-table'><#assign messageArgs=['"+showCount+"','"+pagingData.total+"']><@spring.messageArgs code='main.moreTable' args=messageArgs /></span>",
			"children" : false,
			"li_attr" : { "class" : "next-page-node" },
			"nextPageInfo" :
			{
				"page" : pagingData.page + 1,
				"pageSize" : pagingData.pageSize
			}
		};
		
		return nextPageNode;
	};
	
	po.isNextPageNode = function(node)
	{
		var original = node.original;
		
		return (original.nextPageInfo != undefined);
	};

	po.toJstreeNodePagingData = function(pagingData)
	{
		po.tableToJstreeNodes(pagingData.items);
		
		//添加下一页节点
		if(pagingData.page < pagingData.pages)
		{
			var nextPageNode = po.createNextPageNode(pagingData);
			
			pagingData.items.push(nextPageNode);
		}

		//jstree的_append_json_data方法有“if(data.d){data = data.d;...}”的逻辑，可以用来适配数据
		pagingData.d = pagingData.items;
	};
	
	po.toDraggableNode = function(tree, node)
	{
		var $node = tree.get_node(node.id, true);
		var $anchor = $(".jstree-anchor", $node);
		
		if($anchor.hasClass("ui-draggable"))
			return;
		
		$anchor.draggable(
		{
			helper: "clone",
			appendTo: ".main-page-content",
			classes:
			{
				"ui-draggable" : "table-draggable",
				"ui-draggable-dragging" : "ui-widget ui-widget-content ui-corner-all ui-widget-shadow table-draggable-helper"
			}
		});
	};

	po.getSelTables = function(jstree, selNodes)
	{
		var tables = [];
		
		if(!selNodes)
			return tables;
		
		for(var i=0; i<selNodes.length; i++)
		{
			if(!po.isTableNode(selNodes[i]))
				continue;
			
			tables.push(selNodes[i].original);
		}
		
		return tables;
	};
	
	po.isSearchTable = function()
	{
		var $icon = po.element("#schemaSearchSwitch > .ui-icon");
		
		return $icon.hasClass("ui-icon-calculator");
	};
	
	po.getSearchSchemaFormData = function()
	{
		var form = po.element("#schemaSearchForm");
		var keyword = $("input[name='keyword']", form).val();
		var pageSize = $("input[name='pageSize']", form).val();
		return {"keyword" : keyword, "pageSize" : pageSize};
	};
	
	po.getSearchSchemaFormDataForSchema = function()
	{
		if(po.isSearchTable())
			return {};
		else
			return po.getSearchSchemaFormData();
	};
	
	po.getSearchSchemaFormDataForTable = function()
	{
		var data = po.getSearchSchemaFormData();
		
		if(!po.isSearchTable())
			data["keyword"] = "";
		
		return data;
	};
	
	po.evalTabPanelTop = function($tab)
	{
		var $nav = $("> ul", $tab);
		
		var top = 0;
		
		top += parseInt($tab.css("padding-top"));
		top += parseInt($nav.css("margin-top")) + parseInt($nav.css("margin-bottom")) + $nav.outerHeight() + 5;
		
		return top;
	};
	
	po.refreshSchemaTree = function()
	{
		var $tree = po.element(".schema-panel-content");
		$tree.jstree(true).refresh(true);
	};
	
	po.initSchemaPanelContent = function($element)
	{
		$element.jstree
		(
			{
				"core" :
				{
					"data" :
					{
						"type" : "POST",
						"url" : function(node)
						{
							//根节点
							if(node.id == "#")
								return contextPath+"/schema/list";
							else if(po.isSchemaNode(node))
							{
								return contextPath + $.toPath("schema", node.id, "pagingQueryTable");
							}
						},
						"data" : function(node)
						{
							if(node.id == "#")
								return po.getSearchSchemaFormDataForSchema();
							else if(po.isSchemaNode(node))
								return po.getSearchSchemaFormDataForTable();
						},
						"success" : function(data, textStatus, jqXHR)
						{
							var url = this.url;
							
							if(url.indexOf("/schema/list") > -1)
								po.schemaToJstreeNodes(data);
							else if(url.indexOf("/pagingQueryTable") > -1)
							{
								po.toJstreeNodePagingData(data);
							}
						}
					},
					"themes" : {"dots": false, icons: true},
					"check_callback" : true
				}
			}
		)
		.bind("select_node.jstree", function(e, data)
		{
			var tree = $(this).jstree(true);
			
			if(data.selected && data.selected.length > 1)
				return;
			
			if(po.isTableNode(data.node))
			{
				var schema = tree.get_node(data.node.parent).original;
				var tableInfo = data.node.original;
				
	        	var tabTitle = (po.isTableView(tableInfo) ? "<@spring.message code='main.tableType.view' />" : "<@spring.message code='main.tableType.table' />") + "<@spring.message code='colon' />" + tableInfo.name;
				if(tableInfo.comment)
					tabTitle += "<@spring.message code='bracketLeft' />" + tableInfo.comment + "<@spring.message code='bracketRight' />";
				tabTitle += "<@spring.message code='bracketLeft' />" + schema.title + "<@spring.message code='bracketRight' />";
    			
				var tabUrl = contextPath + $.toPath("data", schema.id, tableInfo.name, "query");
				
				po.activeWorkTab(po.toMainTabIdForSchemaName(schema.id, tableInfo.name), data.node.text, tabTitle, tabUrl, schema);
			}
			else if(po.isNextPageNode(data.node))
			{
				if(!data.node.state.loadingNextPage)
				{
					data.node.state.loadingNextPage = true;
					
					var schemaNode = tree.get_node(data.node.parent);
					
					var schemaId = schemaNode.id;
					
					var $moreTableNode = tree.get_node(data.node, true);
					$(".more-table", $moreTableNode).html("<@spring.message code='main.loadingTable' />");
					
					var param = po.getSearchSchemaFormDataForTable();
					param = $.extend({}, data.node.original.nextPageInfo, param);
					
					$.ajax(contextPath+$.toPath("schema", schemaId, "pagingQueryTable"),
					{
						data : param,
						success : function(pagingData)
						{
							tree.delete_node(data.node);
							po.toJstreeNodePagingData(pagingData);
							
							var nodes = pagingData.items;
							
							for(var i=0; i<nodes.length; i++)
							{
								tree.create_node(schemaNode, nodes[i]);
							}
						},
						error : function(XMLHttpResponse, textStatus, errorThrown)
						{
							data.node.state.loadingNextPage = false;
							$(".more-table", $moreTableNode).html("<@spring.message code='main.moreTable' />");
						}
					});
				}
			}
		})
		.bind("load_node.jstree", function(e, data)
		{
			var tree = $(this).jstree(true);
			
			if(po.selectNodeAfterLoad)
			{
				po.selectNodeAfterLoad = false;
				
				tree.select_node(data.node);
			}
		})
		.bind("hover_node.jstree", function(event, data)
		{
			if($.enableTableNodeDraggable && po.isTableNode(data.node))
			{
				var tree = $(this).jstree(true);
				po.toDraggableNode(tree, data.node);
			}
		});
	};
	
	po.initDataAnalysisPanelContent = function($element)
	{
		$element.jstree
		(
			{
				"core" :
				{
					"themes" : {"dots": false, icons: true},
					"check_callback" : true
				}
			}
		)
		.bind("select_node.jstree", function(e, data)
		{
			var tree = $(this).jstree(true);
			
			var $node = tree.get_node(data.node, true);
			
			var tabId = "dataAnalysis-";
			var tabName = $node.text();
			var tabUrl = "${contextPath}/analysis/";
			
			if($node.hasClass("item-dataset"))
			{
				tabId += "dataset";
				tabUrl += "dataSet/pagingQuery";
			}
			else if($node.hasClass("item-chart"))
			{
				tabId += "chart";
				tabUrl += "chart/pagingQuery";
			}
			else if($node.hasClass("item-dashboard"))
			{
				tabId += "dashboard";
				tabUrl += "dashboard/pagingQuery";
			}
			
			po.activeWorkTab(po.toMainTabId(tabId), tabName, "", tabUrl);
		});
	};

	po.evalSchemaTreeSelState = function(jstree, selNodes)
	{
		var state=
		{
			selCount: 0,
			selSchemaCount: 0,
			selTableCount: 0,
			selTableSameSchema: true
		};
		
		if(!selNodes || !selNodes.length)
			return state;
		
		state.selCount = selNodes.length;
		
		var prevTableSchemaNode = null;
		for(var i=0; i<selNodes.length; i++)
		{
			var selNode = selNodes[i];
			
			if(po.isSchemaNode(selNode))
				state.selSchemaCount++;
			else if(po.isTableNode(selNode))
			{
				state.selTableCount++;
				
				if(state.selTableSameSchema)
				{
					var mySchemaNode = po.getSchemaNode(jstree, selNode);
					
					if(prevTableSchemaNode && mySchemaNode != prevTableSchemaNode)
						state.selTableSameSchema = false;
					
					prevTableSchemaNode = mySchemaNode;
				}
			}
		}
		
		return state;
	};
	
	po.newVersionDetected = function()
	{
		var detectedVersion = $.cookie("DETECTED_VERSION");
		if(typeof(DATA_GEAR_LATEST_VERSION) != "undefined")
		{
			if(DATA_GEAR_LATEST_VERSION != detectedVersion)
			{
				detectedVersion = DATA_GEAR_LATEST_VERSION;
				$.cookie("DETECTED_VERSION", detectedVersion, {expires : 365, path : "${contextPath}"});
			}
		}
		
		if(!detectedVersion)
			return false;
		
		return ($.compareVersion(detectedVersion, po.currentVersion) > 0);
	};
	
	$(document).ready(function()
	{
		var westMinSize = po.element(".schema-panel-head").css("min-width");
		
		if(westMinSize)
		{
			var pxIndex = westMinSize.indexOf("px");
			if(pxIndex > -1)
				westMinSize = westMinSize.substring(0, pxIndex);
		}
		
		westMinSize = parseInt(westMinSize);
		
		if(isNaN(westMinSize) || westMinSize < 245)
			westMinSize = 245;
		
		po.element(".main-page-content").layout(
		{
			west :
			{
				size : "18%",
				minSize : westMinSize,
				maxSize : "40%"
			},
			onresize_end : function()
			{
				//使"#schemaOperationMenu"可以最上层展示
				po.element(".ui-layout-west").css("z-index", 3);
				
				$(window).resize();/*触发page_obj_grid.jsp表格resize*/
			}
		});
		
		//使"#schemaOperationMenu"可以最上层展示
		po.element(".ui-layout-west").css("z-index", 3);
		
		po.element("#systemSetMenu").menu(
		{
			position : {my:"right top", at: "right bottom-1"},
			select : function(event, ui)
			{
				var $item = $(ui.item);
				
				if($item.hasClass("ui-state-disabled"))
					return;
				
				if($item.hasClass("system-set-schema-url-builder"))
				{
					po.open(contextPath+"/schemaUrlBuilder/editScriptCode");
				}
				else if($item.hasClass("system-set-driverEntity-add"))
				{
					po.open(contextPath+"/driverEntity/add");
				}
				else if($item.hasClass("system-set-driverEntity-manage"))
				{
					var options = {};
					$.setGridPageHeightOption(options);
					po.open(contextPath+"/driverEntity/query", options);
				}
				else  if($item.hasClass("system-set-user-add"))
				{
					po.open(contextPath+"/user/add");
				}
				else if($item.hasClass("system-set-user-manage"))
				{
					var options = {};
					$.setGridPageHeightOption(options);
					po.open(contextPath+"/user/query", options);
				}
				else if($item.hasClass("system-set-rold-manage"))
				{
					var options = {};
					$.setGridPageHeightOption(options);
					po.open(contextPath+"/role/query", options);
				}
				else if($item.hasClass("system-set-authorization-manage"))
				{
					var options = {};
					$.setGridPageHeightOption(options);
					po.open(contextPath+"/authorization/${statics['org.datagear.management.domain.Schema'].AUTHORIZATION_RESOURCE_TYPE}/query", options);
				}
				else if($item.hasClass("system-set-chartPlugin-manage"))
				{
					var options = {};
					$.setGridPageHeightOption(options);
					po.open(contextPath+"/analysis/chartPlugin/query", options);
				}
				else if($item.hasClass("system-set-chartPlugin-upload"))
				{
					po.open(contextPath+"/analysis/chartPlugin/upload");
				}
				else if($item.hasClass("system-set-personal-set"))
				{
					po.open(contextPath+"/user/personalSet");
				}
				else if($item.hasClass("theme-item"))
				{
					var theme = $item.attr("theme");
					
					$.getJSON(contextPath+"/changeThemeData?theme="+theme, function(data)
					{
						for(var i=0; i<data.length; i++)
							$(data[i].selector).attr(data[i].attr, data[i].value);
					});
				}
				else if($item.hasClass("about"))
				{
					po.open(contextPath+"/about", { width : "50%" });
				}
				else if($item.hasClass("documentation"))
				{
					window.open("${Global.WEB_SITE}/documentation/");
				}
				else if($item.hasClass("changelog"))
				{
					po.open(contextPath+"/changelog");
				}
				else if($item.hasClass("downloadLatestVersion"))
				{
					window.open("${Global.WEB_SITE}");
				}
			}
		});
		
		po.element("#${pageId}-nav").tabs(
		{
			event: "click",
			active: false,
			collapsible: true,
			activate: function(event, ui)
			{
				var $this = $(this);
				var newTab = $(ui.newTab);
				var newPanel = $(ui.newPanel);
				
				if(newPanel.hasClass("schema-panel"))
				{
					var $element = po.element(".schema-panel-content");
					
					if(!$element.hasClass("jstree"))
						po.initSchemaPanelContent($element);
				}
				else if(newPanel.hasClass("dataAnalysis-panel"))
				{
					var $element = po.element(".dataAnalysis-panel-content");
					
					if(!$element.hasClass("jstree"))
						po.initDataAnalysisPanelContent($element);
				}
				
				var newTabIndex = newTab.index();
				$.cookie("MAIN_NAV_ACTIVE_TAB_INDEX", newTabIndex, {expires : 365*5, path : "${contextPath}"});
			}
		});
		
		var mainNavActiveTabIndex = $.cookie("MAIN_NAV_ACTIVE_TAB_INDEX");
		if(!mainNavActiveTabIndex)
			mainNavActiveTabIndex = "0";
		mainNavActiveTabIndex = parseInt(mainNavActiveTabIndex);
		po.element("#${pageId}-nav").tabs("option", "active", mainNavActiveTabIndex).tabs("option", "collapsible", false);

		po.element("#schemaSearchSwitch").click(function()
		{
			var $icon = $(".ui-icon", this);
			
			if($icon.hasClass("ui-icon-calculator"))
				$icon.removeClass("ui-icon-calculator").addClass("ui-icon-folder-collapsed").attr("title", "<@spring.message code='main.searchSchema' />");
			else
				$icon.removeClass("ui-icon-folder-collapsed").addClass("ui-icon-calculator").attr("title", "<@spring.message code='main.searchTable' />");
		});
		
		po.element("#schemaOperationMenu").menu(
		{
			position : {my:"left top", at: "left bottom-1"},
			focus : function(event, ui)
			{
				var $item = $(ui.item);
				
				if($item.hasClass("schema-operation-root"))
				{
					var menuItemEnables =
					{
						"edit" : false,
						"delete" : false,
						"view" : false,
						"refresh" : false,
						"authorize" : false,
						"reload" : true,
						"sqlpad" : false,
						"dataimport" : false,
						"dataexport" : false
					};
					
					var jstree = po.element(".schema-panel-content").jstree(true);
					var selNodes = jstree.get_selected(true);
					var selState = po.evalSchemaTreeSelState(jstree, selNodes);

					//只要选中表，则禁用重载
					if(selState.selTableCount > 0)
						menuItemEnables["reload"] = false;
					
					//仅选中数据库
					if(selState.selSchemaCount > 0 && selState.selTableCount == 0)
					{
						if(selState.selSchemaCount == 1)
						{
							var schema = selNodes[0].original;
							
							if(po.canEdit(schema))
								menuItemEnables["edit"] = true;
							
							if(po.canDelete(schema))
								menuItemEnables["delete"] = true;
							
							menuItemEnables["view"] = true;
							menuItemEnables["refresh"] = true;

							if(po.canAuthorize(schema, po.currentUser))
								menuItemEnables["authorize"] = true;
							
							menuItemEnables["sqlpad"] = true;
							
							if(po.canDeleteTableData(schema))
								menuItemEnables["dataimport"] = true;
							
							menuItemEnables["dataexport"] = true;
						}
						else
						{
							menuItemEnables["delete"] = true;
							menuItemEnables["refresh"] = true;
							
							for(var i=0; i<selNodes.length; i++)
							{
								var schema = selNodes[i].original;
								if(!po.canDelete(schema))
								{
									menuItemEnables["delete"] = false;
									break;
								}
							}
						}
					}
					//仅选中表
					else if(selState.selSchemaCount == 0 && selState.selTableCount > 0)
					{
						menuItemEnables["refresh"] = true;
						
						if(selState.selTableSameSchema)
						{
							var schema = po.getSchemaNode(jstree, selNodes[0]).original;
							
							menuItemEnables["sqlpad"] = true;
							
							if(po.canDeleteTableData(schema))
								menuItemEnables["dataimport"] = true;
							
							menuItemEnables["dataexport"] = true;
						}
					}
					
					var $menu = $(this);
					
					for(var itemClass in menuItemEnables)
					{
						if(menuItemEnables[itemClass])
							$(".schema-operation-" + itemClass, this).removeClass("ui-state-disabled");
						else
							$(".schema-operation-" + itemClass, this).addClass("ui-state-disabled");
					}
				}
			},
			select : function(event, ui)
			{
				var $item = $(ui.item);
				
				if($item.hasClass("ui-state-disabled"))
					return;
				
				var jstree = po.element(".schema-panel-content").jstree(true);
				var selNodes = jstree.get_selected(true);
				
				if($item.hasClass("schema-operation-edit") || $item.hasClass("schema-operation-view"))
				{
					if(!selNodes || selNodes.length != 1)
						return;
					
					var selNode = selNodes[0];
					
					if(!po.isSchemaNode(selNode))
						return;
					
					var schemaId = selNode.original.id;
					
					po.open(contextPath+$.toPath("schema", ($item.hasClass("schema-operation-edit") ? "edit" : "view"))+"?id="+encodeURIComponent(schemaId), 
					{
						"pageParam" :
						{
							"afterSave" : function()
							{
								jstree.refresh(true);
							}
						}
					});
				}
				else if($item.hasClass("schema-operation-delete"))
				{
					if(!selNodes.length)
						return;
					
					po.confirm("<@spring.message code='main.confirmDeleteSchema' />",
					{
						"confirm" : function()
						{
							var schemaIdParam = "";
							
							for(var i=0; i<selNodes.length; i++)
							{
								if(po.isSchemaNode(selNodes[i]))
								{
									if(schemaIdParam != "")
										schemaIdParam += "&";
									
									schemaIdParam += "id=" + selNodes[i].original.id;
								}
							}
							
							$.post(contextPath+"/schema/delete", schemaIdParam, function()
							{
								jstree.refresh(true);
							});
						}
					});
				}
				else if($item.hasClass("schema-operation-refresh"))
				{
					if(!selNodes.length || selNodes.length < 1)
						return;
					
					if(po.isTableNode(selNodes[0]))
					{
						if(selNodes.length != 1)
						{
							$.tipInfo("<@spring.message code='pleaseSelectOnlyOneRow' />");
						}
						else
						{
							var selNode = selNodes[0];
							var schema = jstree.get_node(selNode.parent).original;
							
							var schemaId = schema.id;
				        	var schemaTitle = schema.title;
				        	var tableName = selNode.original.name;
				        	
				        	var tooltipId;
				        	$.meta.load(schemaId, tableName,
				    	    {
				        		beforeSend : function beforeSend(XHR)
				        		{
				        			tooltipId = $.tipInfo("<@spring.message code='loading' />", -1);
				        		},
				        		success :  function(model)
				        		{
					        		var tabId = po.toMainTabIdForSchemaName(schemaId, tableName);
					        		
					        		var uiTabsNav = po.mainTabs.find(".ui-tabs-nav");
					        		
					        	    var prelia = $("> li > a[href='#"+tabId+"']", uiTabsNav);
					        	    if(prelia.length > 0)
					        	    {
					        	    	$.get(contextPath + $.toPath("data", schemaId, tableName, "query"), function(data)
					        	    	{
					        	    	    uiTabsNav.show();
					        	    	    
					        	    	    $("#"+tabId, po.mainTabs).html(data);
					        	    	    
						        	    	var myidx = prelia.parent().index();
						        	    	po.mainTabs.tabs("option", "active",  myidx);
					        	    	 });
					        	    }
				        		},
					        	complete : function()
					        	{
					        		$.closeTip(tooltipId);
					        	}
				        	});
						}
					}
					else if(po.isSchemaNode(selNodes[0]))
					{
						for(var i=0; i<selNodes.length; i++)
						{
							if(po.isSchemaNode(selNodes[i]))
								jstree.refresh_node(selNodes[i]);
						}
					}
				}
				else if($item.hasClass("schema-operation-authorize"))
				{
					if(!selNodes || selNodes.length != 1)
						return;
					
					var selNode = selNodes[0];
					
					if(!po.isSchemaNode(selNode))
						return;
					
					var schemaId = selNode.original.id;
					
					var options = {};
					$.setGridPageHeightOption(options);
					po.open(contextPath+"/authorization/${statics['org.datagear.management.domain.Schema'].AUTHORIZATION_RESOURCE_TYPE}/query?${statics['org.datagear.web.controller.AuthorizationController'].PARAM_ASSIGNED_RESOURCE}="+encodeURIComponent(schemaId), options);
				}
				else if($item.hasClass("schema-operation-reload"))
				{
					jstree.refresh(true);
				}
				else if($item.hasClass("schema-operation-sqlpad"))
				{
					if(!selNodes || selNodes.length < 1)
						return;
					
					var schemaNode = po.getSchemaNode(jstree, selNodes[0]);
					var schema = (schemaNode ? schemaNode.original : null);
					
					if(schema)
					{
						var tables = po.getSelTables(jstree, selNodes);
						var initSql = "";
						for(var i=0; i<tables.length; i++)
						{
							if(i > 0)
								initSql += "\n";
							initSql += "SELECT * FROM " + tables[i].name+";";
						}
						var tabTitle = "<@spring.message code='main.sqlpad' /><@spring.message code='bracketLeft' />" + schema.title + "<@spring.message code='bracketRight' />";
						var tabUrl = "${contextPath}/sqlpad/" + schema.id + (initSql ? "?initSql=" + encodeURIComponent(initSql) : "");
						po.activeWorkTab(po.toMainTabIdForSchemaName(schema.id, "sqlpad"), "<@spring.message code='main.sqlpad' />", tabTitle, tabUrl, schema);
					}
				}
				else if($item.hasClass("schema-operation-dataimport") || $item.hasClass("schema-operation-dataexport"))
				{
					if(!selNodes || selNodes.length < 1)
						return;
					
					var isImport = $item.hasClass("schema-operation-dataimport");
					
					var schemaNode = po.getSchemaNode(jstree, selNodes[0]);
					var schema = (schemaNode ? schemaNode.original : null);
					
					if(schema)
					{
						var tabId = "";
						var tabLabel = "";
						var tabTitle = "";
						var tabUrl = "";
						
						if(isImport)
						{
							tabId = po.toMainTabIdForSchemaName(schema.id, "dataimport");
							tabLabel = "<@spring.message code='main.dataimport' />";
							tabTitle = "<@spring.message code='main.dataimport' /><@spring.message code='bracketLeft' />" + schema.title + "<@spring.message code='bracketRight' />";
							tabUrl = "${contextPath}/dataexchange/" + schema.id+"/import";
						}
						else
						{
							var tables = po.getSelTables(jstree, selNodes);
							var initSqls = $.getPropertyParamString(tables, "name", "initSqls");
							
							tabId = po.toMainTabIdForSchemaName(schema.id, "dataexport");
							tabLabel = "<@spring.message code='main.dataexport' />";
							tabTitle = "<@spring.message code='main.dataexport' /><@spring.message code='bracketLeft' />" + schema.title + "<@spring.message code='bracketRight' />";
							tabUrl = "${contextPath}/dataexchange/" + schema.id+"/export" + (initSqls ? "?" + initSqls : "");
						}
						
						po.activeWorkTab(tabId, tabLabel, tabTitle, tabUrl, schema);
					}
				}
			}
		});
		
		po.element("#addSchemaButton").click(function()
		{
			var jstree = po.element(".schema-panel-content").jstree(true);
			var selNodes = jstree.get_selected(true);
			
			var copyId = undefined;
			
			if(selNodes.length > 0)
			{
				var selNode = selNodes[0];
				
				if(po.isSchemaNode(selNode))
					copyId = selNode.original.id;
			}
			
			po.open(contextPath+"/schema/add" + (copyId != undefined ? "?copyId="+copyId : ""),
			{
				"pageParam" :
				{
					"afterSave" : function()
					{
						po.refreshSchemaTree();
					}
				}
			});
		});
		
		po.element("#schemaSearchForm").submit(function()
		{
			var jstree = po.element(".schema-panel-content").jstree(true);
			
			if(po.isSearchTable())
			{
				po.selectNodeAfterLoad = true;
				
				var searchSchemaNodes = [];
				
				var selNodes = jstree.get_selected(true);
				for(var i=0; i<selNodes.length; i++)
				{
					var selNode = selNodes[i];
					
					while(selNode && !po.isSchemaNode(selNode))
						selNode = jstree.get_node(selNode.parent);
					
					if(selNode)
						searchSchemaNodes.push(selNode);
				}
				
				//没有选中的话则取第一个
				if(searchSchemaNodes.length == 0)
				{
					var rootNode = jstree.get_node($.jstree.root);
					var firstSchemaNode = (rootNode.children && rootNode.children.length > 0 ? jstree.get_node(rootNode.children[0]) : undefined);
					
					if(firstSchemaNode)
						searchSchemaNodes.push(firstSchemaNode);
				}
				
				for(var i=0; i<searchSchemaNodes.length; i++)
				{
					var searchSchemaNode = searchSchemaNodes[i];
					
					//如果这次的搜索结果为空，下载再搜索的话，节点不会自动打开，
					//使用load_node.jstree事件处理来解决此问题，则会让节点闪烁，效果不好
					//因此这里设置state.opened=true，不会有上述问题
					if(!searchSchemaNode.state.opened)
						searchSchemaNode.state.opened = true;
					
					jstree.refresh_node(searchSchemaNode);
				}
			}
			else
			{
				po.refreshSchemaTree();
			}
		});
		
		po.mainTabs = po.element("#${pageId}-mainTabs");
		
		po.mainTabs.tabs(
		{
			event: "click",
			activate: function(event, ui)
			{
				var $this = $(this);
				var newTab = $(ui.newTab);
				var newPanel = $(ui.newPanel);
				var tabsNav = po.getTabsNav($this);
				
				po.refreshTabsNavForHidden($this, tabsNav, newTab);
				
				$(".category-bar", tabsNav).removeClass("ui-state-active");
				
				var newSchemaId = newTab.attr("schema-id");
				if(newSchemaId)
					$(".category-bar.category-bar-"+newSchemaId, tabsNav).addClass("ui-state-active");
				
				$.callTabsPanelShowCallback(newPanel);
			}
		});
		
		po.getTabsNav(po.mainTabs).hide();
		
		po.getTabsTabMoreOperationMenu(po.mainTabs).menu(
		{
			select: function(event, ui)
			{
				var $this = $(this);
				var item = ui.item;
				
				if(item.hasClass("tab-operation-newwin"))
				{
					var tabUrl = $this.attr("tab-url");
					window.open(tabUrl);
				}
				else
					po.handleTabMoreOperationMenuSelect($this, item, po.mainTabs);
				
				po.getTabsTabMoreOperationMenuWrapper(po.mainTabs).hide();
			}
		});
		
		po.getTabsMoreTabMenu(po.mainTabs).menu(
		{
			select: function(event, ui)
			{
				po.handleTabsMoreTabMenuSelect($(this), ui.item, po.mainTabs);
		    	po.getTabsMoreTabMenuWrapper(po.mainTabs).hide();
			}
		});
		
		po.bindTabsMenuHiddenEvent(po.mainTabs);
		
		<#if !disableDetectNewVersion>
		if(po.newVersionDetected())
			$(".new-version-tip").css("display", "inline-block");
		</#if>
	});
})
(${pageId});
</script>
</head>
<body id="${pageId}">
<div class="main-page-head">
	<#include "include/html_logo.ftl">
	<div class="toolbar">
		<ul id="systemSetMenu" class="lightweight-menu">
			<li class="system-set-root"><span><span class="ui-icon ui-icon-gear"></span><span class="new-version-tip"></span></span>
				<ul style="display:none;" class="ui-widget-shadow">
					<#if !currentUser.anonymous>
					<#if currentUser.admin>
					<li class="system-set-driverEntity-manage"><a href="javascript:void(0);"><@spring.message code='main.manageDriverEntity' /></a></li>
					<li class="system-set-driverEntity-add"><a href="javascript:void(0);"><@spring.message code='main.addDriverEntity' /></a></li>
					<li class="ui-widget-header"></li>
					<li class="system-set-schema-url-builder"><a href="javascript:void(0);"><@spring.message code='schemaUrlBuilder.schemaUrlBuilder' /></a></li>
					<li class="system-set-authorization-manage"><a href="javascript:void(0);"><@spring.message code='main.manageSchemaAuth' /></a></li>
					<li class="ui-widget-header"></li>
					<li class="system-set-chartPlugin-manage"><a href="javascript:void(0);"><@spring.message code='main.manageChartPlugin' /></a></li>
					<li class="system-set-chartPlugin-upload"><a href="javascript:void(0);"><@spring.message code='main.uploadChartPlugin' /></a></li>
					<li class="ui-widget-header"></li>
					<li class="system-set-user-manage"><a href="javascript:void(0);"><@spring.message code='main.manageUser' /></a></li>
					<li class="system-set-user-add"><a href="javascript:void(0);"><@spring.message code='main.addUser' /></a></li>
					<li class="system-set-rold-manage"><a href="javascript:void(0);"><@spring.message code='main.manageRole' /></a></li>
					</#if>
					<li class="system-set-personal-set"><a href="javascript:void(0);"><@spring.message code='main.personalSet' /></a></li>
					<li class="ui-widget-header"></li>
					</#if>
					<li class=""><a href="javascript:void(0);"><@spring.message code='main.changeTheme' /></a>
						<ul class="ui-widget-shadow">
							<li class="theme-item" theme="lightness"><a href="javascript:void(0);"><@spring.message code='main.changeTheme.lightness' /><span class="ui-widget ui-widget-content theme-sample theme-sample-lightness"></span></a></li>
							<li class="theme-item" theme="dark"><a href="javascript:void(0);"><@spring.message code='main.changeTheme.dark' /><span class="ui-widget ui-widget-content theme-sample theme-sample-dark"></span></a></li>
							<li class="theme-item" theme="green"><a href="javascript:void(0);"><@spring.message code='main.changeTheme.green' /><span class="ui-widget ui-widget-content theme-sample theme-sample-green"></span></a></li>
						</ul>
					</li>
					<li><a href="javascript:void(0);"><@spring.message code='help' /><span class="new-version-tip"></span></a>
						<ul class="ui-widget-shadow">
							<li class="about"><a href="javascript:void(0);"><@spring.message code='main.about' /></a></li>
							<li class="documentation"><a href="javascript:void(0);"><@spring.message code='main.documentation' /></a></li>
							<li class="changelog"><a href="javascript:void(0);"><@spring.message code='main.changelog' /></a></li>
							<li class="downloadLatestVersion">
								<a href="javascript:void(0);"><@spring.message code='main.downloadLatestVersion' /><span class="new-version-tip"></span></a>
							</li>
						</ul>
					</li>
				</ul>
			</li>
		</ul>
		<#if !currentUser.anonymous>
		<div class="user-name">
		${currentUser.nameLabel?html}
		</div>
		<a class="link" href="${contextPath}/logout"><@spring.message code='main.logout' /></a>
		<#else>
		<a class="link" href="${contextPath}/login"><@spring.message code='main.login' /></a>
		<#if !disableRegister>
		<a class="link" href="${contextPath}/register"><@spring.message code='main.register' /></a>
		</#if>
		</#if>
	</div>
</div>
<div class="main-page-content">
	<div class="ui-layout-west">
		<div id="${pageId}-nav" class="main-nav">
			<ul>
				<li><a href="#${pageId}-nav-dataSource"><@spring.message code='main.dataSource' /></a></li>
				<li><a href="#${pageId}-nav-dataAnalysis"><@spring.message code='main.dataAnalysis' /></a></li>
			</ul>
			<div id="${pageId}-nav-dataSource" class="ui-widget ui-widget-content schema-panel">
				<div class="schema-panel-head">
					<div class="schema-panel-operation">
						<div class="ui-widget ui-widget-content ui-corner-all search">
							<form id="schemaSearchForm" action="javascript:void(0);">
								<div id="schemaSearchSwitch" class="schema-search-switch"><span class="ui-icon ui-icon-calculator search-switch-icon" title="<@spring.message code='main.searchTable' />"></span></div>
								<div class="keyword-input-parent"><input name="keyword" type="text" value="" class="ui-widget ui-widget-content keyword-input" /></div>
								<button type="submit" class="ui-button ui-corner-all ui-widget ui-button-icon-only search-button"><span class="ui-icon ui-icon-search"></span><span class="ui-button-icon-space"> </span><@spring.message code='find' /></button>
								<input name="pageSize" type="hidden" value="100" />
							</form>
						</div>
						<button id="addSchemaButton" class="ui-button ui-corner-all ui-widget ui-button-icon-only add-schema-button" title="<@spring.message code='main.addSchema' />"><span class="ui-button-icon ui-icon ui-icon-plus"></span><span class="ui-button-icon-space"> </span><@spring.message code='add' /></button>
						<ul id="schemaOperationMenu" class="lightweight-menu">
							<li class="schema-operation-root"><span><span class="ui-icon ui-icon-triangle-1-s"></span></span>
								<ul class="ui-widget-shadow">
									<li class="schema-operation-edit"><a href="javascript:void(0);"><@spring.message code='edit' /></a></li>
									<li class="schema-operation-delete"><a href="javascript:void(0);"><@spring.message code='delete' /></a></li>
									<li class="schema-operation-view"><a href="javascript:void(0);"><@spring.message code='view' /></a></li>
									<li class="schema-operation-refresh" title="<@spring.message code='main.schemaOperationMenuRefreshComment' />"><a href="javascript:void(0);"><@spring.message code='refresh' /></a></li>
									<li class="schema-operation-authorize"><a href="javascript:void(0);"><@spring.message code='authorize' /></a></li>
									<li class="ui-widget-header"></li>
									<li class="schema-operation-reload" title="<@spring.message code='main.schemaOperationMenuReloadComment' />"><a href="javascript:void(0);"><@spring.message code='reload' /></a></li>
									<li class="ui-widget-header"></li>
									<li class="schema-operation-sqlpad"><a href="javascript:void(0);"><@spring.message code='main.sqlpad' /></a></li>
									<li class="ui-widget-header"></li>
									<li class="schema-operation-dataimport"><a href="javascript:void(0);"><@spring.message code='main.dataimport' /></a></li>
									<li class="schema-operation-dataexport"><a href="javascript:void(0);"><@spring.message code='main.dataexport' /></a></li>
								</ul>
							</li>
						</ul>
					</div>
				</div>
				<div class="schema-panel-content">
				</div>
			</div>
			<div id="${pageId}-nav-dataAnalysis" class="ui-widget ui-widget-content dataAnalysis-panel">
				<div class="dataAnalysis-panel-content">
					<ul>
						<li class="item-dataset"><@spring.message code='main.dataAnalysis.dataSet' /></li>
						<li class="item-chart"><@spring.message code='main.dataAnalysis.chart' /></li>
						<li class="item-dashboard"><@spring.message code='main.dataAnalysis.dashboard' /></li>
					</ul>
				</div>
			</div>
		</div>
	</div>
	<div class="ui-layout-center">
		<div id="${pageId}-mainTabs" class="main-tabs">
			<ul>
			</ul>
			<div class="tabs-more-operation-menu-wrapper ui-widget ui-front ui-widget-content ui-corner-all ui-widget-shadow" style="position: absolute; left:0px; top:0px; display: none;">
				<ul class="tabs-more-operation-menu">
					<li class="tab-operation-close-left"><div><@spring.message code='main.closeLeft' /></div></li>
					<li class="tab-operation-close-right"><div><@spring.message code='main.closeRight' /></div></li>
					<li class="tab-operation-close-other"><div><@spring.message code='main.closeOther' /></div></li>
					<li class="tab-operation-close-all"><div><@spring.message code='main.closeAll' /></div></li>
					<li class="ui-widget-header"></li>
					<li class="tab-operation-newwin"><div><@spring.message code='main.openInNewWindow' /></div></li>
				</ul>
			</div>
			<div class="tabs-more-tab-menu-wrapper ui-widget ui-front ui-widget-content ui-corner-all ui-widget-shadow" style="position: absolute; left:0px; top:0px; display: none;">
				<ul class="tabs-more-tab-menu">
				</ul>
			</div>
		</div>
	</div>
</div>
</body>
</html>
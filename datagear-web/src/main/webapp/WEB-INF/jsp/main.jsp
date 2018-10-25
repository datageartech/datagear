<%--
/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="include/jsp_import.jsp" %>
<%@ include file="include/jsp_ajax_request.jsp" %>
<%@ include file="include/jsp_jstl.jsp" %>
<%@ include file="include/jsp_page_id.jsp" %>
<%@ include file="include/html_doctype.jsp" %>
<html>
<head>
<%@ include file="include/html_head.jsp" %>
<title><%@ include file="include/html_title_app_name.jsp" %><fmt:message key='main.mainPage' /></title>
<%@ include file="include/page_js_obj.jsp" %>
<script type="text/javascript">
(function(pageObj)
{
	<%org.datagear.management.domain.User user = WebUtils.getUser(request, response);%>
	pageObj.userId = "<%=user.getId()%>";
	pageObj.isAnonymous = <%=user.isAnonymous()%>;
	pageObj.isAdmin = <%=user.isAdmin()%>;
	
	pageObj.workTabTemplate = "<li style='vertical-align:middle;'><a href='"+'#'+"{href}'>"+'#'+"{label}</a>"
			+"<div class='tab-operation'>"			
			+"<span class='ui-icon ui-icon-close' title='<fmt:message key='close' />'>close</span>"
			+"<div class='tab-operation-more' title='<fmt:message key='moreOperation' />'></div>"
			+"</div>"
			+"<div class='category-bar category-bar-"+'#'+"{schemaId}'></div>"
			+"</li>";
	
	pageObj.addWorkTab = function(tabId, label, schema, modelName)
	{
		var schemaId = schema.id;
		var schemaTitle = schema.title;
		
		var mainTabs = pageObj.element("#mainTabs");
		var uiTabsNav = mainTabs.find(".ui-tabs-nav");
		
	    var prelia = $("> li > a[href='#"+tabId+"']", uiTabsNav);
	    if(prelia.length > 0)
	    {
	    	var myidx = prelia.parent().index();
	    	mainTabs.tabs("option", "active",  myidx);
	    }
	    else
	    {
	    	$.get(contextPath +"/data/"+schemaId+"/"+modelName+"/query", function(data)
	    	{
	    		uiTabsNav.show();
	    		
	    		var li = $("> li > a[href='#"+tabId+"']", uiTabsNav);
	    		var tabContentDiv = $("#"+tabId, mainTabs);
	    		
	    		//防止双击导致创建两次而引起界面错乱
	    		if(li.length == 0)
	    		{
	    			li = $(pageObj.workTabTemplate.replace( /#\{href\}/g, "#" + tabId).replace(/#\{label\}/g, label).replace(/#\{schemaId\}/g, schemaId)).appendTo(uiTabsNav);
	    			
	    			if(!li.attr("id"))
	    				li.attr("id", $.uid("main-tab-"));
	    			
	    			li.attr("schema-id", schemaId);
	    			li.attr("model-name", modelName);
	    			li.attr("title", schemaTitle +" : " + modelName);
	    		}
	    	    if(tabContentDiv.length == 0)
	    	    	tabContentDiv = $("<div id='" + tabId + "'></div>").appendTo(mainTabs);
	    	    
	    	    mainTabs.tabs("refresh").tabs( "option", "active",  $("> li", uiTabsNav).length - 1);
	    	    
	    	    tabContentDiv.css("top", pageObj.evalTabPanelTop(mainTabs));
	    	    tabContentDiv.html(data);
	    	    
	    	    $(".tab-operation .ui-icon-close", li).click(function()
	    	    {
	    	    	var li = $(this).parent().parent();
	    	    	var tabId = $("a", li).attr("href");
	    	    	
	    	    	$(tabId, mainTabs).remove();
	    	    	li.remove();
	    	    	
	    	    	mainTabs.tabs("refresh");
	    	    	pageObj.refreshTabsNav(mainTabs);
	    	    	 
	 				if($("li", uiTabsNav).length == 0)
	 					uiTabsNav.hide();
	    	    });
	    	    
	    	    $(".tab-operation .tab-operation-more", li).click(function()
	    	    {
	    	    	var li = $(this).parent().parent();
	    	    	var tabId = $("a", li).attr("href");
	    	    	
	    	    	pageObj.element("#tabMoreOperationMenuParent").show().css("left", "0px").css("top", "0px")
	    	    		.position({"my" : "left top+1", "at": "right bottom", "of" : $(this), "collision": "flip flip"});

	    	    	var menuItemDisabled = {};
	    	    	
	    	    	var hasPrev = (li.prev().length > 0);
	    	    	var hasNext = (li.next().length > 0);
	    	    	
	    	    	menuItemDisabled[".tab-operation-close-left"] = !hasPrev;
	    	    	menuItemDisabled[".tab-operation-close-right"] = !hasNext;
	    	    	menuItemDisabled[".tab-operation-close-other"] = !hasPrev && !hasNext;
	    	    	
	    	    	var menu = pageObj.element("#tabMoreOperationMenu");
	    	    	
	    	    	for(var selector in menuItemDisabled)
	    	    	{
	    	    		if(menuItemDisabled[selector])
	    	    			$(selector, menu).addClass("ui-state-disabled");
	    	    		else
	    	    			$(selector, menu).removeClass("ui-state-disabled");
	    	    	}
	    	    	
	    	    	menu.attr("tab-id", tabId)
	    	    		.attr("schema-id", li.attr("schema-id")).attr("model-name", li.attr("model-name"));
	    	    });
	    	});
	    }
	};
	
	pageObj.genTabId = function(schemaId, modelName)
	{
		var map = (pageObj.genTabIdMap || (pageObj.genTabIdMap = {}));
		
		//不能直接使用这个key作为元素ID，因为modelName中可能存在与jquery冲突的字符，比如'$'
		var key = schemaId +"_" + modelName;
		var value = map[key];
		
		if(value == undefined)
		{
			var nextNumber = (pageObj.genTabIdNextNumber != undefined 
					? (pageObj.genTabIdNextNumber = pageObj.genTabIdNextNumber + 1) : (pageObj.genTabIdNextNumber = 0));
			
			value = "mainTabs-" + nextNumber;
			map[key] = value;
		}
		
		return value;
	};

	pageObj.isSchemaNode = function(node)
	{
		if(!node)
			return false;
		
		var original = node.original;
		
		if(!original)
			return false;
		
		return (original.id != undefined && original.url != undefined);
	};
	
	pageObj.schemaToJstreeNode = function(schema)
	{
		var tempSchema = (schema.createUser && schema.createUser.anonymous);
		schema.text = schema.title + (tempSchema ? " <span class='ui-icon ui-icon-notice' title='<fmt:message key='main.tempSchema' />'></span>" : "");
		schema.children = true;
		
		return schema;
	};
	
	pageObj.schemaToJstreeNodes = function(schemas)
	{
		for(var i=0; i<schemas.length; i++)
			pageObj.schemaToJstreeNode(schemas[i]);
		
		return schemas;
	};

	pageObj.isTableNode = function(node)
	{
		var original = node.original;
		
		return (original.name != undefined && original.type != undefined);
	};
	
	pageObj.tableToJstreeNode = function(table)
	{
		var text = table.name;
		
		if(table.comment)
			text = text + "("+table.comment+")";
		
		table.text = text;
		table.children = false;
		
		return table;
	};
	
	pageObj.tableToJstreeNodes = function(tables)
	{
		for(var i=0; i<tables.length; i++)
			pageObj.tableToJstreeNode(tables[i]);
		
		return tables;
	};
	
	pageObj.createNextPageNode = function(pagingData)
	{
		var nextPageNode = {"text" : "<span class='more-table'><fmt:message key='main.moreTable' /></span>", "children" : false, "nextPageInfo" : { "page" : pagingData.page + 1, "pageSize" : pagingData.pageSize} };
		
		return nextPageNode;
	};
	
	pageObj.isNextPageNode = function(node)
	{
		var original = node.original;
		
		return (original.nextPageInfo != undefined);
	};

	pageObj.toJstreeNodePagingData = function(pagingData)
	{
		pageObj.tableToJstreeNodes(pagingData.items);
		
		//添加下一页节点
		if(pagingData.page < pagingData.pages)
		{
			var nextPageNode = pageObj.createNextPageNode(pagingData);
			
			pagingData.items.push(nextPageNode);
		}

		//jstree的_append_json_data方法有“if(data.d){data = data.d;...}”的逻辑，可以用来适配数据
		pagingData.d = pagingData.items;
	};
	
	pageObj.getSearchSchemaFormData = function()
	{
		var form = pageObj.element("#schemaSearchForm");
		var keyword = $("input[name='keyword']", form).val();
		var pageSize = $("input[name='pageSize']", form).val();
		return {"keyword" : keyword, "pageSize" : pageSize};
	};
	
	pageObj.evalTabPanelTop = function($tab)
	{
		var $nav = $("> ul", $tab);
		
		var top = 0;
		
		top += parseInt($tab.css("padding-top"));
		top += parseInt($nav.css("margin-top")) + parseInt($nav.css("margin-bottom")) + $nav.outerHeight() + 5;
		
		return top;
	};
	
	pageObj.refreshSchemaTree = function()
	{
		$.get(contextPath+"/schema/list", function(schemas)
		{
			schemas = pageObj.schemaToJstreeNode(schemas);
			
			var $tree = pageObj.element(".schema-panel-content");
			$tree.jstree(true).refresh(true);
		});
	};
	
	$(document).ready(function()
	{
		pageObj.element(".main-page-content").layout(
		{
			west :
			{
				size : "18%"
			}
		});
		
		pageObj.element("#systemSetMenu").menu(
		{
			position : {my:"right top", at: "right bottom-1"},
			select : function(event, ui)
			{
				var $item = $(ui.item);
				
				if($item.hasClass("ui-state-disabled"))
					return;
				
				if($item.hasClass("system-set-global-setting"))
				{
					pageObj.open(contextPath+"/globalSetting");
				}
				else if($item.hasClass("system-set-schema-url-builder"))
				{
					pageObj.open(contextPath+"/schemaUrlBuilder/editScriptCode");
				}
				else if($item.hasClass("system-set-driverEntity-add"))
				{
					pageObj.open(contextPath+"/driverEntity/add");
				}
				else if($item.hasClass("system-set-driverEntity-manage"))
				{
					var options = {};
					$.setGridPageHeightOption(options);
					pageObj.open(contextPath+"/driverEntity/query", options);
				}
				else  if($item.hasClass("system-set-user-add"))
				{
					pageObj.open(contextPath+"/user/add");
				}
				else if($item.hasClass("system-set-user-manage"))
				{
					var options = {};
					$.setGridPageHeightOption(options);
					pageObj.open(contextPath+"/user/query", options);
				}
				else if($item.hasClass("system-set-personal-set"))
				{
					pageObj.open(contextPath+"/user/personalSet");
				}
				else if($item.hasClass("about"))
				{
					pageObj.open(contextPath+"/about", { width : "50%" });
				}
			}
		});
		
		pageObj.element("#schemaOperationMenu").menu(
		{
			position : {my:"right top", at: "right bottom-1"},
			focus : function(event, ui)
			{
				var $item = $(ui.item);
				
				if($item.hasClass("schema-operation-root"))
				{
					var menuItemEnables =
					{
						"schema-operation-edit" : true,
						"schema-operation-delete" : true,
						"schema-operation-view" : true,
						"schema-operation-refresh" : true,
						"schema-operation-reload" : true,
					};
					
					var jstree = pageObj.element(".schema-panel-content").jstree(true);
					var selNodes = jstree.get_selected(true);
					
					var disableCRUD = false;
					
					//未选中数据库，则禁用CRUD按钮
					if(!selNodes.length)
						disableCRUD = true;
					else
					{
						for(var i=0; i<selNodes.length; i++)
						{
							if(!pageObj.isSchemaNode(selNodes[i]))
							{
								disableCRUD = true;
								break;
							}
						}
					}
					
					if(disableCRUD)
					{
						menuItemEnables["schema-operation-edit"] = false;
						menuItemEnables["schema-operation-delete"] = false;
						menuItemEnables["schema-operation-view"] = false;
					}
					
					var diableEditAndDelete = false;
					
					//管理员、创建用户才能编辑和删除数据库
					for(var i=0; i<selNodes.length; i++)
					{
						if(!pageObj.isSchemaNode(selNodes[i]))
						{
							diableEditAndDelete = true;
							break;
						}
						
						var schema = selNodes[i].original;
						
						if(!pageObj.isAdmin && schema.createUser != undefined && schema.createUser.id != pageObj.userId)
						{
							diableEditAndDelete = true;
							break;
						}
					}
					
					if(diableEditAndDelete)
					{
						menuItemEnables["schema-operation-edit"] = false;
						menuItemEnables["schema-operation-delete"] = false;
					}
					
					//如果有选中，且全都是数据库或者全都是表，则启用刷新按钮
					menuItemEnables["schema-operation-refresh"] = false;
					if(selNodes.length)
					{
						var selSchemaCount = 0, selTableCount = 0;
						for(var i=0; i<selNodes.length; i++)
						{
							if(pageObj.isTableNode(selNodes[i]))
							{
								selTableCount++;
							}
							else if(pageObj.isSchemaNode(selNodes[i]))
							{
								selSchemaCount++;
							}
						}
						
						if(selSchemaCount == 0 || selTableCount == 0)
							menuItemEnables["schema-operation-refresh"] = true;
					}
					
					//只要选中了表，就禁用重载按钮
					for(var i=0; i<selNodes.length; i++)
					{
						if(pageObj.isTableNode(selNodes[i]))
						{
							menuItemEnables["schema-operation-reload"] = false;
							break;
						}
					}
					
					var $menu = $(this);
					
					for(var itemClass in menuItemEnables)
					{
						if(menuItemEnables[itemClass])
							$("." + itemClass, this).removeClass("ui-state-disabled");
						else
							$("." + itemClass, this).addClass("ui-state-disabled");
					}
				}
			},
			select : function(event, ui)
			{
				var $item = $(ui.item);
				
				if($item.hasClass("ui-state-disabled"))
					return;
				
				var jstree = pageObj.element(".schema-panel-content").jstree(true);
				var selNodes = jstree.get_selected(true);
				
				if($item.hasClass("schema-operation-edit") || $item.hasClass("schema-operation-view"))
				{
					if(selNodes.length != 1)
					{
						$.tipInfo("<fmt:message key='pleaseSelectOnlyOneRow' />");
						return;
					}
					
					var selNode = selNodes[0];
					
					if(!pageObj.isSchemaNode(selNode))
						return;
					
					var schemaId = selNode.original.id;
					
					pageObj.open(contextPath+"/schema/"+($item.hasClass("schema-operation-edit") ? "edit" : "view")+"?id=" + schemaId, 
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
					
					pageObj.confirm("<fmt:message key='main.confirmDeleteSchema' />",
					{
						"confirm" : function()
						{
							var schemaIdParam = "";
							
							for(var i=0; i<selNodes.length; i++)
							{
								if(pageObj.isSchemaNode(selNodes[i]))
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
					
					if(pageObj.isTableNode(selNodes[0]))
					{
						if(selNodes.length != 1)
						{
							$.tipInfo("<fmt:message key='pleaseSelectOnlyOneRow' />");
						}
						else
						{
							var selNode = selNodes[0];
							var schema = jstree.get_node(selNode.parent).original;
							
							var schemaId = schema.id;
				        	var schemaTitle = schema.title;
				        	var modelName = selNode.original.name;
				        	
				        	var tooltipId;
				        	$.model.load(schemaId, modelName,
				    	    {
				        		beforeSend : function beforeSend(XHR)
				        		{
				        			tooltipId = $.tipInfo("<fmt:message key='loading' />", -1);
				        		},
				        		success :  function(model)
				        		{
					        		var tabId = pageObj.genTabId(schemaId, modelName);
					        		
					        		var mainTabs = pageObj.element("#mainTabs");
					        		var uiTabsNav = mainTabs.find(".ui-tabs-nav");
					        		
					        	    var prelia = $("> li > a[href='#"+tabId+"']", uiTabsNav);
					        	    if(prelia.length > 0)
					        	    {
					        	    	$.get(contextPath +"/data/"+schemaId+"/"+modelName+"/query", function(data)
					        	    	{
					        	    	    uiTabsNav.show();
					        	    	    
					        	    	    $("#"+tabId, mainTabs).html(data);
					        	    	    
						        	    	var myidx = prelia.parent().index();
						        	    	mainTabs.tabs("option", "active",  myidx);
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
					else if(pageObj.isSchemaNode(selNodes[0]))
					{
						for(var i=0; i<selNodes.length; i++)
						{
							if(pageObj.isSchemaNode(selNodes[i]))
								jstree.refresh_node(selNodes[i]);
						}
					}
				}
				else if($item.hasClass("schema-operation-reload"))
				{
					jstree.refresh(true);
				}
			}
		});
		
		pageObj.element("#addSchemaButton").click(function()
		{
			var jstree = pageObj.element(".schema-panel-content").jstree(true);
			var selNodes = jstree.get_selected(true);
			
			var copyId = undefined;
			
			if(selNodes.length > 0)
			{
				var selNode = selNodes[0];
				
				if(pageObj.isSchemaNode(selNode))
					copyId = selNode.original.id;
			}
			
			pageObj.open(contextPath+"/schema/add" + (copyId != undefined ? "?copyId="+copyId : ""),
			{
				"pageParam" :
				{
					"afterSave" : function()
					{
						pageObj.refreshSchemaTree();
					}
				}
			});
		});
		
		pageObj.element(".schema-panel-content").jstree
		(
			{
				"core" :
				{
					"data" :
					{
						"url" : function(node)
						{
							//根节点
							if(node.id == "#")
								return contextPath+"/schema/list";
							//方案节点
							else if(pageObj.isSchemaNode(node))
							{
								return contextPath+"/schema/"+node.id+"/pagingQueryTable"
							}
						},
						"data" : function(node)
						{
							return pageObj.getSearchSchemaFormData();
						},
						"success" : function(data, textStatus, jqXHR)
						{
							var url = this.url;
							
							if(url.indexOf("/schema/list") > -1)
								pageObj.schemaToJstreeNodes(data);
							else if(url.indexOf("/pagingQueryTable") > -1)
							{
								pageObj.toJstreeNodePagingData(data);
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
			
			if(pageObj.isTableNode(data.node))
			{
				var schema = tree.get_node(data.node.parent).original;
				
				var schemaId = schema.id;
	        	var schemaTitle = schema.title;
	        	var modelName = data.node.original.name;
	        	
	        	var tooltipId;
	        	$.model.on(schemaId, modelName,
	        	{
	        		beforeSend : function beforeSend(XHR)
	        		{
	        			tooltipId = $.tipInfo("<fmt:message key='loading' />", -1);
	        		},
	        		success : function(model)
		        	{
		        		pageObj.addWorkTab(pageObj.genTabId(schemaId, modelName), data.node.text, schema, modelName);
		        	},
		        	complete : function()
		        	{
		        		$.closeTip(tooltipId);
		        	}
	        	});
			}
			else if(pageObj.isNextPageNode(data.node))
			{
				if(!data.node.state.loadingNextPage)
				{
					data.node.state.loadingNextPage = true;
					
					var schemaNode = tree.get_node(data.node.parent);
					
					var schemaId = schemaNode.id;
					
					var $moreTableNode = tree.get_node(data.node, true);
					$(".more-table", $moreTableNode).html("<fmt:message key='main.loadingTable' />");
					
					var param = pageObj.getSearchSchemaFormData();
					$.extend(param, data.node.original.nextPageInfo);
					
					$.ajax(contextPath+"/schema/"+schemaId+"/pagingQueryTable",
					{
						data : param,
						success : function(pagingData)
						{
							tree.delete_node(data.node);
							pageObj.toJstreeNodePagingData(pagingData);
							
							var nodes = pagingData.items;
							
							for(var i=0; i<nodes.length; i++)
							{
								tree.create_node(schemaNode, nodes[i]);
							}
						},
						error : function(XMLHttpResponse, textStatus, errorThrown)
						{
							data.node.state.loadingNextPage = false;
							$(".more-table", $moreTableNode).html("<fmt:message key='main.moreTable' />");
						}
					});
				}
			}
		})
		.bind("load_node.jstree", function(e, data)
		{
			var tree = $(this).jstree(true);
			
			if(pageObj.selectNodeAfterLoad)
			{
				pageObj.selectNodeAfterLoad = false;
				
				tree.select_node(data.node);
			}
		});
		
		pageObj.element("#schemaSearchForm").submit(function()
		{
			pageObj.selectNodeAfterLoad = true;
			
			var jstree = pageObj.element(".schema-panel-content").jstree(true);
			
			var searchSchemaNodes = [];
			
			var selNodes = jstree.get_selected(true);
			for(var i=0; i<selNodes.length; i++)
			{
				var selNode = selNodes[i];
				
				while(selNode && !pageObj.isSchemaNode(selNode))
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
		});
		
		pageObj.element("#mainTabs").tabs(
		{
			event: "click",
			activate: function(event, ui)
			{
				var newTab = $(ui.newTab);
				var newPanel = $(ui.newPanel);
				
				pageObj.refreshTabsNav($(this), newTab);
				
				var newSchemaId = newTab.attr("schema-id");
				
				$(".ui-tabs-nav .category-bar", this).removeClass("ui-state-active");
				$(".ui-tabs-nav .category-bar.category-bar-"+newSchemaId, this).addClass("ui-state-active");
			}
		});
		
		pageObj.element("#mainTabs .ui-tabs-nav").hide();
		
		pageObj.getTabsHiddens = function(tabsNav)
		{
			var tabsNavHeight = tabsNav.height();
			
			var hiddens = [];
			
			$("li.ui-tabs-tab", tabsNav).each(function()
			{
				var li = $(this);
				
				if(li.is(":hidden") || li.position().top >= tabsNavHeight)
					hiddens.push(li);
			});
			
			return hiddens;
		};
		
		pageObj.refreshTabsNav = function(tabs, activeTab)
		{
			var tabsNav = pageObj.element(".ui-tabs-nav", tabs);
			
			if(activeTab == undefined)
				activeTab = $("li.ui-tabs-active", tabsNav);
			
			$("li.ui-tabs-tab", tabsNav).show();
			
			if(activeTab && activeTab.length > 0)
			{
				//如果卡片不可见，则向前隐藏卡片，直到此卡片可见
				
				var tabsNavHeight = tabsNav.height();
				
				var activeTabPosition;
				var prevHidden = activeTab.prev();
				while((activeTabPosition = activeTab.position()).top >= tabsNavHeight)
				{
					prevHidden.hide();
					prevHidden = prevHidden.prev();
				}
			}
			
			var showHiddenButton = $(".tab-show-hidden", tabs);
			
			if(pageObj.getTabsHiddens(tabsNav).length > 0)
			{
				if(showHiddenButton.length == 0)
				{
					showHiddenButton = $("<button class='ui-button ui-corner-all ui-widget ui-button-icon-only tab-show-hidden'><span class='ui-icon ui-icon-triangle-1-s'></span></button>").appendTo(tabs);
					showHiddenButton.click(function()
					{
						var tabs = pageObj.element("#mainTabs");
						var tabsNav = pageObj.element(".ui-tabs-nav", tabs);
						
						var hiddens = pageObj.getTabsHiddens(tabsNav);
						
						var menu = pageObj.element("#tabMoreTabMenu");
						menu.empty();
						
						for(var i=0; i<hiddens.length; i++)
						{
							var tab = hiddens[i];
							
							var mi = $("<li />").appendTo(menu);
							mi.attr("tab-id", tab.attr("id"));
							$("<div />").html($(".ui-tabs-anchor", tab).text()).appendTo(mi);
						}
						
		    	    	pageObj.element("#tabMoreTabMenuParent").show().css("left", "0px").css("top", "0px")
		    	    		.position({"my" : "left top+1", "at": "right bottom", "of" : $(this), "collision": "flip flip"});
		    	    	
						menu.menu("refresh");
					});
				}
				
				showHiddenButton.show();
			}
			else
				showHiddenButton.hide();
		};
		
		pageObj.element("#tabMoreOperationMenu").menu(
		{
			select: function(event, ui)
			{
				var item = ui.item;
				var schemaId = $(this).attr("schema-id");
				var modelName = $(this).attr("model-name");
				var tabId = $(this).attr("tab-id");
				
				var mainTabs = pageObj.element("#mainTabs");
				var uiTabsNav = mainTabs.find(".ui-tabs-nav");
				var tabLink = $("a[href='"+tabId+"']", uiTabsNav);
				var tabLi = tabLink.parent();
				
				if(item.hasClass("tab-operation-newwin"))
				{
					window.open(contextPath +"/data/"+schemaId+"/"+modelName+"/query");
				}
				else if(item.hasClass("tab-operation-close-left"))
				{
					var prev;
					while((prev = tabLi.prev()).length > 0)
					{
						var preTabId = $("a", prev).attr("href");
						
						$(preTabId, mainTabs).remove();
						prev.remove();
					}
					
					mainTabs.tabs("refresh");
					pageObj.refreshTabsNav(mainTabs);
				}
				else if(item.hasClass("tab-operation-close-right"))
				{
					var next;
					while((next = tabLi.next()).length > 0)
					{
						var nextTabId = $("a", next).attr("href");
						
						$(nextTabId, mainTabs).remove();
						next.remove();
					}
					
					mainTabs.tabs("refresh");
					pageObj.refreshTabsNav(mainTabs);
				}
				else if(item.hasClass("tab-operation-close-other"))
				{
					$("li", uiTabsNav).each(function()
					{
						if(tabLi[0] == this)
							return;
						
						var li = $(this);
						
						var tabId = $("a", li).attr("href");

						$(tabId, mainTabs).remove();
						li.remove();
					});
					
					mainTabs.tabs("refresh");
					pageObj.refreshTabsNav(mainTabs);
				}
				else if(item.hasClass("tab-operation-close-all"))
				{
					$("li", uiTabsNav).each(function()
					{
						var li = $(this);
						
						var tabId = $("a", li).attr("href");

						$(tabId, mainTabs).remove();
						li.remove();
					});
					
					mainTabs.tabs("refresh");
					pageObj.refreshTabsNav(mainTabs);
				}
				
				if($("li", uiTabsNav).length == 0)
					uiTabsNav.hide();
				
				pageObj.element("#tabMoreOperationMenuParent").hide();
			}
		});
		
		pageObj.element("#tabMoreTabMenu").menu(
		{
			select: function(event, ui)
			{
				var item = ui.item;
				var tabId = item.attr("tab-id");
				
				var mainTabs = pageObj.element("#mainTabs");
				var myIndex = pageObj.element(".ui-tabs-nav li[id='"+tabId+"']", mainTabs).index();
		    	mainTabs.tabs("option", "active",  myIndex);
				
				pageObj.element("#tabMoreTabMenuParent").hide();
			}
		});
		
		$(document.body).click(function(e)
		{
			var target = $(e.target);
			
			var hide = true;
			
			while(target && target.length != 0)
			{
				if(target.hasClass("tab-operation-more") || target.hasClass("tab-more-operation-menu-parent"))
				{
					hide = false;
					break;
				}
				
				target = target.parent();
			};
			
			if(hide)
				pageObj.element("#tabMoreOperationMenuParent").hide();
		});

		$(document.body).click(function(e)
		{
			var target = $(e.target);
			
			var hide = true;
			
			while(target && target.length != 0)
			{
				if(target.hasClass("tab-show-hidden") || target.hasClass("tab-more-tab-menu-parent"))
				{
					hide = false;
					break;
				}
				
				target = target.parent();
			};
			
			if(hide)
				pageObj.element("#tabMoreTabMenuParent").hide();
		});
		
		//系统通知
		$.get("<c:url value='/notification/list' />", function(data)
		{
			if(data && data.length)
			{
				for(var i=0; i< data.length; i++)
				{
					$.tipInfo(data[i].content);
				}
			}
		});
	});
})
(${pageId});
</script>
</head>
<body id="${pageId}">
<div class="main-page-head">
	<%@ include file="include/html_logo.jsp" %>
	<div class="toolbar">
		<ul id="systemSetMenu" class="lightweight-menu">
			<li class="system-set-root"><span><span class="ui-icon ui-icon-gear"></span></span>
				<ul style="display:none;">
					<%if(!user.isAnonymous()){ %>
					<%if(user.isAdmin()){ %>
					<li class="system-set-driverEntity-manage"><a href="javascript:void(0);"><fmt:message key='main.manageDriverEntity' /></a></li>
					<li class="system-set-driverEntity-add"><a href="javascript:void(0);"><fmt:message key='main.addDriverEntity' /></a></li>
					<li class="ui-widget-header"></li>
					<li class="system-set-user-manage"><a href="javascript:void(0);"><fmt:message key='main.manageUser' /></a></li>
					<li class="system-set-user-add"><a href="javascript:void(0);"><fmt:message key='main.addUser' /></a></li>
					<li class="ui-widget-header"></li>
					<%}%>
					<li class="system-set-personal-set"><a href="javascript:void(0);"><fmt:message key='main.personalSet' /></a></li>
					<%if(user.isAdmin()){ %>
					<li class=""><a href="javascript:void(0);"><fmt:message key='main.globalSetting' /></a>
						<ul>
							<li class="system-set-global-setting"><a href="javascript:void(0);"><fmt:message key='globalSetting.smtpSetting' /></a></li>
							<li class="system-set-schema-url-builder"><a href="javascript:void(0);"><fmt:message key='schemaUrlBuilder.schemaUrlBuilder' /></a></li>
						</ul>
					</li>
					<%}%>
					<li class="ui-widget-header"></li>
					<%}%>
					<li class=""><a href="javascript:void(0);"><fmt:message key='main.changeTheme' /></a>
						<ul>
							<li class=""><a href="<%=request.getContextPath()%>/?theme=lightness"><fmt:message key='main.changeTheme.lightness' /><span class="ui-widget ui-widget-content theme-sample theme-sample-lightness"></span></a></li>
							<li class=""><a href="<%=request.getContextPath()%>/?theme=dark"><fmt:message key='main.changeTheme.dark' /><span class="ui-widget ui-widget-content theme-sample theme-sample-dark"></span></a></li>
							<li class=""><a href="<%=request.getContextPath()%>/?theme=green"><fmt:message key='main.changeTheme.green' /><span class="ui-widget ui-widget-content theme-sample theme-sample-green"></span></a></li>
						</ul>
					</li>
					<li class="about">
						<a href="javascript:void(0);"><fmt:message key='main.about' /></a>
					</li>
				</ul>
			</li>
		</ul>
		<%if(!user.isAnonymous()){ %>
		<div class="user-name">
		<%=user.getNameLabel()%>
		</div>
		<a class="link" href="<c:url value="/logout" />"><fmt:message key='main.logout' /></a>
		<%}else{%>
		<a class="link" href="<c:url value="/login" />"><fmt:message key='main.login' /></a>
		<a class="link" href="<c:url value="/register" />"><fmt:message key='main.register' /></a>
		<%}%>
	</div>
</div>
<div class="main-page-content">
	<div class="ui-layout-west">
		<div class="ui-widget ui-widget-content schema-panel">
			<div class="schema-panel-head">
				<div class="schema-panel-title"><fmt:message key='main.schema' /></div>
				<div class="schema-panel-operation">
					<div class="ui-widget ui-widget-content ui-corner-all search">
						<form id="schemaSearchForm" action="javascript:void(0);">
						<input name="keyword" type="text" value="" class="ui-widget ui-widget-content" title="<fmt:message key='main.searchTable' />" /><button type="submit" class="ui-button ui-corner-all ui-widget ui-button-icon-only search-button"><span class="ui-icon ui-icon-search"></span><span class="ui-button-icon-space"> </span><fmt:message key='find' /></button>
						<input name="pageSize" type="hidden" value="100" />
						</form>
					</div>
					<button id="addSchemaButton" class="ui-button ui-corner-all ui-widget ui-button-icon-only add-schema-button" title="<fmt:message key='main.addSchema' />"><span class="ui-button-icon ui-icon ui-icon-plus"></span><span class="ui-button-icon-space"> </span><fmt:message key='add' /></button>
					<ul id="schemaOperationMenu" class="lightweight-menu">
						<li class="schema-operation-root"><span><span class="ui-icon ui-icon-triangle-1-s"></span></span>
							<ul>
								<li class="schema-operation-edit"><div><fmt:message key='edit' /></div></li>
								<li class="schema-operation-delete"><div><fmt:message key='delete' /></div></li>
								<li class="schema-operation-view"><div><fmt:message key='view' /></div></li>
								<li class="schema-operation-refresh" title="<fmt:message key='main.schemaOperationMenuRefreshComment' />"><div><fmt:message key='refresh' /></div></li>
								<li class="ui-widget-header"></li>
								<li class="schema-operation-reload" title="<fmt:message key='main.schemaOperationMenuReloadComment' />"><div><fmt:message key='reload' /></div></li>
							</ul>
						</li>
					</ul>
				</div>
			</div>
			<div class="schema-panel-content">
			</div>
		</div>
	</div>
	<div class="ui-layout-center">
		<div id="mainTabs" class="main-tabs">
			<ul>
			</ul>
		</div>
		<div id="tabMoreOperationMenuParent" class="ui-widget ui-front ui-widget-content ui-corner-all ui-widget-shadow tab-more-operation-menu-parent" style="position: absolute; left:0px; top:0px; display: none;">
			<ul id="tabMoreOperationMenu" class="tab-more-operation-menu">
				<li class="tab-operation-close-left"><div><fmt:message key='main.closeLeft' /></div></li>
				<li class="tab-operation-close-right"><div><fmt:message key='main.closeRight' /></div></li>
				<li class="tab-operation-close-other"><div><fmt:message key='main.closeOther' /></div></li>
				<li class="tab-operation-close-all"><div><fmt:message key='main.closeAll' /></div></li>
				<li class="ui-widget-header"></li>
				<li class="tab-operation-newwin"><div><fmt:message key='main.openInNewWindow' /></div></li>
			</ul>
		</div>
		<div id="tabMoreTabMenuParent" class="ui-widget ui-front ui-widget-content ui-corner-all ui-widget-shadow tab-more-tab-menu-parent" style="position: absolute; left:0px; top:0px; display: none;">
			<ul id="tabMoreTabMenu" class="tab-more-tab-menu">
			</ul>
		</div>
	</div>
</div>
</body>
</html>
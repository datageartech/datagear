<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#--
看板表单资源功能片段
-->
<script type="text/javascript">
(function(po)
{
	po.initDashboardResources = function()
	{
		po.resizeResourcesWorkspace();
		
		//初始化资源选项卡
		
		po.resourceListTabs().tabs(
		{
			activate: function(event, ui)
			{
				var $this = $(this);
				var newTab = $(ui.newTab);
				var newPanel = $(ui.newPanel);
				
				if(newTab.hasClass("nav-item-global"))
					po.initResListGlobalIfNon();
			}
		});
		
		//初始化资源编辑器选项卡
		
		po.resourceEditorTabs().tabs(
		{
			event: "click",
			activate: function(event, ui)
			{
				var $this = $(this);
				var newTab = $(ui.newTab);
				var newPanel = $(ui.newPanel);
				
				po.tabsRefreshNavForHidden($this, newTab);
			}
		});
		
		po.tabsGetTabMoreOptMenu(po.resourceEditorTabs()).menu(
		{
			select: function(event, ui)
			{
				var $this = $(this);
				var item = ui.item;
				
				po.tabsHandleMoreOptMenuSelect($this, item, po.resourceEditorTabs());
				po.tabsGetTabMoreOptMenuWrapper(po.resourceEditorTabs()).hide();
			}
		});
		
		po.tabsGetMoreTabMenu(po.resourceEditorTabs()).menu(
		{
			select: function(event, ui)
			{
				po.tabsHandleMoreTabMenuSelect($(this), ui.item, po.resourceEditorTabs());
		    	po.tabsGetMoreTabMenuWrapper(po.resourceEditorTabs()).hide();
			}
		});
		
		po.tabsBindMenuHiddenEvent(po.resourceEditorTabs());
		
		//初始化模板列表
		
		po.elementResListLocal(".resource-list-template").selectable
		({
			classes: {"ui-selected": "ui-state-active"},
			filter: ".resource-item",
			selected: function()
			{
				po.deselectResourceNameForTree(po.elementResListLocal(".resource-list-content"));
			}
		})
		.on("mouseenter", ".resource-item", function()
		{
			var $this = $(this);
			$this.addClass("ui-state-default");
		})
		.on("mouseleave", ".resource-item", function()
		{
			var $this = $(this);
			$this.removeClass("ui-state-default");
		});
		
		//初始化本地资源树
		
		po.elementResListLocal(".resource-list-content").jstree(
		{
			core:
			{
				data: function(node, callback)
				{
					var _this = this;
					
					//根节点
					if(node.id == "#")
					{
						if(po.checkDashboardUnSaved(false))
						{
							callback.call(_this, []);
							return;
						}
						
						var id = po.getDashboardId();
						
						$.get(po.url("listResources?id="+id), function(resources)
						{
							resources = (resources || []);
							
							var $templates = po.elementResListLocal(".resource-list-template");
							$templates.empty();
							
							for(var i=0; i<po.templates.length; i++)
							{
								for(var j=0; j<resources.length; j++)
								{
									if(po.templates[i] == resources[j])
										po.addDashboardResourceItemTemplate($templates, resources[j]);
								}
							}
							
							var treeData = po.resourceNamesToTreeData(resources, "resLocal-");
							callback.call(_this, treeData);
						});
					}
				},
				check_callback: true,
				themes: {dots:false, icons: true}
			}
		})
		.bind("select_node.jstree", function()
		{
			po.deselectResourceNameForSelectable();
		})
		.bind("select_all.jstree", function()
		{
			po.deselectResourceNameForSelectable();
		});

		//初始化编辑器尺寸调节按钮
		
		po.element(".resize-editor-button-left").click(function()
		{
			var $ele = po.element();
			var $icon = $(".ui-icon", this);
			
			if($ele.hasClass("max-resource-editor-left"))
			{
				$ele.removeClass("max-resource-editor-left");
				$icon.removeClass("ui-icon-arrowstop-1-e").addClass("ui-icon-arrowstop-1-w");
			}
			else
			{
				$ele.addClass("max-resource-editor-left");
				$icon.removeClass("ui-icon-arrowstop-1-w").addClass("ui-icon-arrowstop-1-e");
			}
		});
		
		//初始化本地资源操作
		
		var copyResNameButton = po.elementResListLocal(".copyResNameButton");
		if(copyResNameButton.length > 0)
		{
			var clipboard = new ClipboardJS(copyResNameButton[0],
			{
				//需要设置container，不然在对话框中打开页面后复制不起作用
				container: po.element()[0],
				text: function(trigger)
				{
					var text = po.getSelectedResourceName();
					if(!text)
						text = "";
					
					return text;
				}
			});
			clipboard.on('success', function(e)
			{
				$.tipSuccess("<@spring.message code='copyToClipboardSuccess' />");
			});
		}
		
		//初始化添加资源面板
		
		var addResPanel = po.element(".add-resource-panel");
		addResPanel.draggable({ handle : ".panel-head" });
		po.element("form", addResPanel).submit(function()
		{
			var name = po.elementOfName("addResName", this).val();
			if(!name)
				return false;
			
			if(po.isResourceNameDirectroy(name))
			{
				$.tipInfo("<@spring.message code='dashboard.illegalSaveAddResourceName' />");
				return false;
			}
			
			var content = "";
			var isHtml = $.isHtmlFile(name);
			
			if(isHtml)
				content = po.elementOfId("${pageId}-defaultTemplateContent").val();
			
			po.newResourceEditorTab(name, content, isHtml);
			po.element(".add-resource-panel").hide();
			
			return false;
		});
		
		//初始化上传资源面板
		
		var uploadResPanel = po.element(".upload-resource-panel");
		uploadResPanel.draggable({ handle : ".panel-head" });
		po.element("form", uploadResPanel).submit(function()
		{
			if(po.checkDashboardUnSaved())
				return false;
			
			var panel = po.element(".upload-resource-panel");
			
			var id = po.getDashboardId();
			var resourceName = po.elementOfName("uploadResName", panel).val();
			var resourceFilePath = po.elementOfName("uploadResFilePath", panel).val();
			
			if(!id || !resourceName || !resourceFilePath)
				return false;
			
			$.post(po.url("saveUploadResourceFile"),
			{"id": id, "resourceName": resourceName, "resourceFilePath": resourceFilePath},
			function()
			{
				po.refreshResourceListLocal();
				panel.hide();
			});
			
			return false;
		});
		
		po.element(".uploadResourceWrapper").fileUpload(po.url("uploadResourceFile"),
		{
			success: function(response)
			{
				var parent = po.getSelectedResourceName();
				if(!po.isResourceNameDirectroy(parent))
					parent = "";
				
				var panel = po.element(".upload-resource-panel");
				
				po.elementOfName("uploadResName", panel).val(parent + response.fileName);
				po.elementOfName("uploadResFilePath", panel).val(response.uploadFilePath);
			}
		});
		
		po.elementResListLocal(".resource-more-button-wrapper").hover(
		function()
		{
			po.elementResListLocal(".resource-more-button-panel").show();
		},
		function()
		{
			po.elementResListLocal(".resource-more-button-panel").hide();
		});

		po.elementResListLocal(".addResBtn").click(function()
		{
			var initVal = po.getSelectedResourceName();
			if(!po.isResourceNameDirectroy(initVal))
				initVal = "";
			
			var panel = po.element(".add-resource-panel");
			
			panel.show()
				.position({ my : "right top", at : "right bottom", of : this});
			po.elementOfName("addResName", panel).val(initVal).focus();
		});
		
		po.elementResListLocal(".editResBtn").click(function()
		{
			if(po.checkDashboardUnSaved())
				return;
			
			var resName = po.getSelectedResourceName();
			
			if(!resName)
				return;
			
		 	if(!$.isTextFile(resName))
		 	{
		 		$.tipInfo("<@spring.message code='dashboard.editResUnsupport' />");
		 		return;
		 	}
		 	
		 	var editIndex = -1;
		 	$(".resource-editor-tab", po.tabsGetNav(po.resourceEditorTabs()))
		 	.each(function(index)
		 	{
		 		if($(this).attr("resourceName") == resName)
		 		{
		 			editIndex = index;
		 			return false;
		 		}
		 	});
		 	
		 	if(editIndex > -1)
		 	{
		 		po.resourceEditorTabs().tabs( "option", "active",  editIndex);
		 	}
		 	else
		 	{
			 	$.get(po.url("getResourceContent"), {"id": po.getDashboardId(), "resourceName": resName}, function(data)
			 	{
			 		var isTemplate = (po.getTemplateIndex(data.resourceName) > -1);
			 		po.newResourceEditorTab(data.resourceName, data.resourceContent, isTemplate);
			 	});
		 	}
		});
		
		po.elementResListLocal(".uploadResBtn").click(function()
		{
			if(po.checkDashboardUnSaved())
				return;
			
			var panel = po.element(".upload-resource-panel");
			
			po.elementOfName("uploadResName", panel).val("");
			po.elementOfName("uploadResFilePath", panel).val("");
			po.element(".upload-file-info", panel).text("");
			
			panel.show()
				.position({ my : "right top", at : "right bottom", of : this});
		});
		
		po.elementResListLocal(".viewResButton").click(function(e)
		{
			if(po.checkDashboardUnSaved())
				return;
			
			var id = po.getDashboardId();
			var path = po.getSelectedResourceName();
			
			if(!path)
				return;
			
			window.open(po.showUrl(id, path));
		});
		
		po.elementResListLocal(".asTemplateBtn").click(function()
		{
			if(po.checkDashboardUnSaved())
				return;
			
			var resName = po.getSelectedResourceNameForTree(po.elementResListLocal(".resource-list-content"));
			
			if(!resName)
				return;
			
			if(!$.isHtmlFile(resName))
			{
		 		$.tipInfo("<@spring.message code='dashboard.resAsTemplateUnsupport' />");
		 		return;
			}
			
			var $templates = po.elementResListLocal(".resource-item-template");
			for(var i=0; i<$templates.length; i++)
			{
				if($($templates[i]).attr("resource-name") == resName)
					return;
			}
			
			var templates = po.templates.concat([]);
			templates.push(resName);
			
			po.saveTemplateNames(templates);
		});
		
		po.elementResListLocal(".asNormalResBtn").click(function()
		{
			if(po.checkDashboardUnSaved())
				return;
			
			var resName = po.getSelectedResourceNameForTemplate();
			
			if(!resName)
				return;
			
			var templates = po.templates.concat([]);
			var idx = po.getTemplateIndex(resName, templates);
			if(idx > -1)
				templates.splice(idx, 1);
			
			po.saveTemplateNames(templates);
		});
		
		po.elementResListLocal(".asFirstTemplateBtn").click(function()
		{
			if(po.checkDashboardUnSaved())
				return;
			
			var resName = po.getSelectedResourceNameForTemplate();
			
			if(!resName)
				return;
			
			var templates = po.templates.concat([]);
			var idx = po.getTemplateIndex(resName, templates);
			if(idx > -1)
				templates.splice(idx, 1);
			templates.unshift(resName);
			
			po.saveTemplateNames(templates);
		});

		po.elementResListLocal(".refreshResListBtn").click(function()
		{
			if(po.checkDashboardUnSaved())
				return;
			
			po.refreshResourceListLocal();
		});
		
		po.elementResListLocal(".deleteResBtn").click(function()
		{
			if(po.checkDashboardUnSaved())
				return;
			
			var id = po.getDashboardId();
			var name = po.getSelectedResourceName();
			
			if(!name)
				return;
			
			po.confirm("<@spring.message code='dashboard.confirmDeleteSelectedResource' />",
			{
				"confirm" : function()
				{
					$.post(po.url("deleteResource"), {"id": id, "name" : name},
					function(response)
					{
						po.templates = response.data.templates;
						po.refreshResourceListLocal();
					});
				}
			});
		});
		
		//初始化全局资源操作

		po.elementResListGlobal(".search-input").on("keydown", function(e)
		{
			if(e.keyCode == $.ui.keyCode.ENTER)
			{
				po.elementResListGlobal(".search-button").click();
				//防止提交表单
				return false;
			}
		});
		
		po.elementResListGlobal(".search-button").click(function(e)
		{
			po.refreshResourceListGlobal();
		});
		
		po.elementResListGlobal(".viewResButton").click(function(e)
		{
			if(po.checkDashboardUnSaved())
				return;
			
			var id = po.getDashboardId();
			var path = po.getSelectedResourceGlobalName();
			
			if(!path)
				return;
			
			window.open(po.showUrl(id, path));
		});

		po.elementResListGlobal(".refreshResListBtn").click(function()
		{
			po.refreshResourceListGlobal();
		});

		var copyResGlobalNameButton = po.elementResListGlobal(".copyResNameButton");
		if(copyResGlobalNameButton.length > 0)
		{
			var clipboard = new ClipboardJS(copyResGlobalNameButton[0],
			{
				//需要设置container，不然在对话框中打开页面后复制不起作用
				container: po.element()[0],
				text: function(trigger)
				{
					var text = po.getSelectedResourceGlobalName();
					if(!text)
						text = "";
					
					return text;
				}
			});
			clipboard.on('success', function(e)
			{
				$.tipSuccess("<@spring.message code='copyToClipboardSuccess' />");
			});
		}
		
	};
	
	po.resizeResourcesWorkspace = function()
	{
		var height;
		
		if(po.isInDialog())
			height = $(window).height()*3/5;
		else
		{
			var gapHeight = 20;
			var th = $(window).height() - po.element(".form-item-analysisProjectAware").outerHeight(true);
			th = th - po.element(".form-foot").outerHeight(true) - gapHeight;
			height = th;
		}
		
		po.element(".form-item-value-resources").height(height);
	};
	
	po.resourceEditorTabs = function()
	{
		return po.elementOfId("${pageId}-resourceEditorTabs");
	};
	
	po.resourceListTabs = function()
	{
		return po.element(".resource-list-tabs");
	};
	
	po.elementResListLocal = function(selector)
	{
		var rll = po.element(".resource-list-local-wrapper");
		
		if(!selector)
			return rll;
		else
			return $(selector, rll);
	};
	
	po.elementResListGlobal = function(selector)
	{
		var rlg = po.element(".resource-list-global-wrapper");
		
		if(!selector)
			return rlg;
		else
			return $(selector, rlg);
	};

	po.getSelectedResourceNameForTree = function($tree)
	{
		var tree = $tree.jstree(true);
		var sel = tree.get_selected(true);
		
		if(sel && sel.length > 0)
			return sel[0].original.fullPath;
		else
			return undefined;
	};
	
	po.deselectResourceNameForTree = function($tree)
	{
		var tree = $tree.jstree(true);
		var sel = tree.get_selected();
		tree.deselect_node(sel);
	};
	
	po.getSelectedResourceNameForTemplate = function()
	{
		var $template = po.elementResListLocal(".resource-list-template > .resource-item.ui-state-active");
		
		if($template.length > 0)
			return $template.attr("resource-name");
		
		return undefined;
	};
	
	po.deselectResourceNameForSelectable = function()
	{
		var $template = po.elementResListLocal(".resource-list-template > .resource-item.ui-state-active");
		$template.removeClass("ui-state-active");
	};
	
	po.getSelectedResourceName = function()
	{
		var resName = po.getSelectedResourceNameForTemplate();
		
		if(resName)
			return resName;
		else
			return po.getSelectedResourceNameForTree(po.elementResListLocal(".resource-list-content"));
	};
	
	po.addDashboardResourceItemTemplate = function($parent, templateName, prepend)
	{
		var $res = $("<div class='resource-item resource-item-template ui-corner-all'></div>").attr("resource-name", templateName).text(templateName);
		$res.prepend($("<span class='ui-icon ui-icon-contact'></span>").attr("title", "<@spring.message code='dashboard.dashboardTemplateResource' />"));
		$("<input type='hidden' name='templates[]' />").attr("value", templateName).appendTo($res);
		
		if(prepend == true)
			$parent.prepend($res);
		else
		{
			var last = $(".resource-item-template", $parent).last();
			if(last.length == 0)
				$parent.prepend($res);
			else
				last.after($res);
		}
	};
	
	po.getTemplateIndex = function(templateName, templates)
	{
		templates = (templates || po.templates);
		
		for(var i=0; i<templates.length; i++)
		{
			if(templates[i] == templateName)
				return i;
		}
		
		return -1;
	};
	
	po.isResourceNameDirectroy = function(resName)
	{
		return (resName && resName.charAt(resName.length - 1) == '/');
	};
	
	po.resourceNamesToTreeData = function(resourceNames, idPrefix)
	{
		if(idPrefix == null)
			idPrefix = "";
		
		return $.toPathTree(resourceNames,
				{
					nameProperty: "text", childrenProperty: "children",
					fullPathProperty: "fullPath",
					created: function(node)
					{
						node.id = idPrefix + node.fullPath;
					}
				});
	};
	
	po.refreshResourceListLocal = function()
	{
		if(po.checkDashboardUnSaved(false))
			return;
		
		po.elementResListLocal(".resource-list-content").jstree(true).refresh(true);
	};

	po.saveTemplateNames = function(templateNames, success)
	{
		if(templateNames == null || templateNames.length == 0)
		{
			$.tipInfo("<@spring.message code='dashboard.atLeastOneTemplateRequired' />");
			return;
		}
		
		var id = po.getDashboardId();
		
		$.ajaxJson(po.url("saveTemplateNames?id="+id),
		{
			data: templateNames,
			success : function(response)
			{
				po.templates = response.data.templates;
				po.refreshResourceListLocal();
				
				if(success)
					success();
			}
		});
	};

	po.initResListGlobalIfNon = function()
	{
		var $tree = po.elementResListGlobal(".resource-list-content");
		var tree = $.jstree.reference($tree);
		
		if(tree != null)
			return;
		
		$tree.jstree(
		{
			core:
			{
				data: function(node, callback)
				{
					var _this = this;
					
					//根节点
					if(node.id == "#")
					{
						var keyword = po.elementResListGlobal(".search-input").val();
						
						$.postJson("${contextPath}/dashboardGlobalRes/queryData", { "keyword": keyword }, function(resources)
						{
							resources = (resources || []);
							
							if(!resources || resources.length == 0)
							{
								po.elementResListGlobal(".resource-none").show();
								po.elementResListGlobal(".resource-list-content").hide();
							}
							else
							{
								po.elementResListGlobal(".resource-none").hide();
								po.elementResListGlobal(".resource-list-content").show();
							}
							
							var resNames = [];
							for(var i=0; i<resources.length; i++)
								resNames[i] = resources[i].path;
							
							var treeData = po.resourceNamesToTreeData(resNames, "resGlobal-");
							callback.call(_this, treeData);
						});
					}
				},
				check_callback: true,
				themes: {dots:false, icons: true}
			}
		});
	};
	
	po.refreshResourceListGlobal = function()
	{
		po.elementResListGlobal(".resource-list-content").jstree(true).refresh(true);
	};

	po.getSelectedResourceGlobalName = function()
	{
		var name = po.getSelectedResourceNameForTree(po.elementResListGlobal(".resource-list-content"));
		
		if(name)
			name = po.dashboardGlobalResUrlPrefix + name;
		
		return name;
	};
})
(${pageId});
</script>

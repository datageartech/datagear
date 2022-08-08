<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#--
看板资源编辑器

依赖：

-->
<p-tabview v-model:active-index="pm.resourceContentTabs.activeIndex"
	:scrollable="true" @tab-change="onResourceContentTabChange"
	@tab-click="onResourceContentTabClick" class="contextmenu-tabview light-tabview"
	:class="{'opacity-0': pm.resourceContentTabs.items.length == 0}">
	<p-tabpanel v-for="tab in pm.resourceContentTabs.items" :key="tab.key" :header="tab.title">
		<template #header>
			<p-button type="button" icon="pi pi-angle-down"
				class="context-menu-btn p-button-xs p-button-secondary p-button-text p-button-rounded"
				@click="onResourceContentTabMenuToggle($event, tab.id)"
				aria-haspopup="true" aria-controls="${pid}resourceContentTabMenu">
			</p-button>
		</template>
		<div :id="tab.id">
			<div class="flex align-content-center justify-content-between">
				<p-selectbutton v-model="tab.editMode" :options="pm.templateEditModeOptions"
					option-label="name" option-value="value" class="text-sm">
				</p-selectbutton>
				<div class="flex" v-if="tab.editMode == 'code'">
					<p-button label="<@spring.message code='insertChart' />" class="p-button-sm"></p-button>
					<p-menubar :model="pm.tplCodeEditMenuItems" class="light-menubar no-root-icon-menubar border-none pl-2 text-sm z-99">
						<template #end>
							<div class="p-inputgroup pl-2">
								<p-inputtext type="text" class="text-sm p-0 px-1" style="width:10rem;"></p-inputtext>
								<p-button type="button" icon="pi pi-search" class="p-button-secondary p-button-sm"></p-button>
							</div>
						</template>
					</p-menubar>
				</div>
				<div class="flex" v-if="tab.editMode == 'visual'">
					<p-button label="<@spring.message code='quickExecute' />" class="p-button-sm"></p-button>
					<p-menubar :model="pm.tplVisualEditMenuItems" class="light-menubar no-root-icon-menubar border-none pl-2 text-sm z-99">
					</p-menubar>
				</div>
			</div>
			<div class="pt-1">
				<div class="code-editor-wrapper template-editor-wrapper p-component p-inputtext">
					<div :id="resourceCodeEditorEleId(tab.resourceName)" class="code-editor"></div>
				</div>
			</div>
		</div>
	</p-tabpanel>
</p-tabview>
<p-contextmenu id="${pid}resourceContentTabMenu" ref="${pid}resourceContentTabMenuEle"
	:model="pm.resourceContentTabMenuItems" :popup="true" class="text-sm">
</p-contextmenu>
<script>
(function(po)
{
	po.resourceContentTabId = function(name)
	{
		var map = (po.resourceContentTabIdMap || (po.resourceContentTabIdMap = {}));
		
		//不直接使用tableName作为元素ID，因为name中可能存在与jquery冲突的字符，比如'$'
		var value = map[name];
		
		if(value == null)
		{
			value = $.uid("resCntTab");
			map[name] = value;
		}
		
		return value;
	};
	
	po.resourceCodeEditorEleId = function(name)
	{
		var tabId = po.resourceContentTabId(name);
		return tabId+"codeEditor";
	};

	po.resourceVisualEditorEleId = function(name)
	{
		var tabId = po.resourceContentTabId(name);
		return tabId+"visualEditor";
	};
	
	po.toResourceContentTabItem = function(name)
	{
		var re =
		{
			id: po.resourceContentTabId(name),
			key: name,
			title: name,
			editMode: "code",
			resourceName: name
		};
		
		return re;
	};
	
	po.buildTplVisualInsertMenuItems = function(insertType)
	{
		var items =
		[
			{
				label: "<@spring.message code='gridLayout' />",
				class: "insert-type-" + insertType,
				command: function()
				{
				}
			},
			{
				label: "<@spring.message code='divElement' />",
				class: "insert-type-" + insertType,
				command: function()
				{
				}
			},
			{
				label: "<@spring.message code='textElement' />",
				class: "insert-type-" + insertType,
				command: function()
				{
				}
			},
			{
				label: "<@spring.message code='image' />",
				class: "insert-type-" + insertType,
				command: function()
				{
				}
			},
			{
				label: "<@spring.message code='hyperlink' />",
				class: "insert-type-" + insertType,
				command: function()
				{
				}
			},
			{
				label: "<@spring.message code='video' />",
				class: "insert-type-" + insertType,
				command: function()
				{
				}
			},
			{ separator: true },
			{
				label: "<@spring.message code='chart' />",
				class: "insert-type-" + insertType,
				command: function()
				{
				}
			}
		];
		
		return items;
	};
	
	po.getResourceContentTabIndex = function(name)
	{
		var pm = po.vuePageModel();
		var items = pm.resourceContentTabs.items;
		
		return $.inArrayById(items, po.resourceContentTabId(name));
	};
	
	po.showResourceContentTab = function(name)
	{
		var pm = po.vuePageModel();
		
		var idx = po.getResourceContentTabIndex(name);
		if(idx > -1)
			pm.resourceContentTabs.activeIndex = idx;
		else
		{
			var tabItem = po.toResourceContentTabItem(name);
			pm.resourceContentTabs.items.push(tabItem);
			
			//XXX vueMounted()回调函数中vueApp()为null？
			if(po.vueApp())
			{
				//直接设置activeIndex不会滚动到新加的卡片，所以采用此方案
				po.vueApp().$nextTick(function()
				{
					pm.resourceContentTabs.activeIndex = pm.resourceContentTabs.items.length - 1;
				});
			}
			else
				pm.resourceContentTabs.activeIndex = pm.resourceContentTabs.items.length - 1;
		}
	};
	
	po.loadResourceContent = function(name)
	{
		var tabId = po.resourceContentTabId(name);
		var panel = po.elementOfId(tabId);
		
		var expectLoad = (panel.prop("loaded") !== true);
		
		if(expectLoad && panel.prop("loading") !== true)
		{
			panel.prop("loading", true);
			
			var fm = po.vueFormModel();
			
			po.ajax("/dashboard/getResourceContent",
			{
				data:
				{
					id: fm.id,
					resourceName: name
				},
				success: function(response)
				{
					var isTemplate = po.isResTemplate(name);
					
					var resourceContent = (response.resourceExists ? response.resourceContent : "");
					if(isTemplate && !resourceContent)
						resourceContent = (response.defaultTemplateContent || "");
					
					po.setResourceContent(name, resourceContent, isTemplate);
				},
				complete: function()
				{
					panel.prop("loading", false);
				}
			});
		}
	};
	
	po.setResourceContent = function(name, content, isTemplate)
	{
		isTemplate = (isTemplate == null ? po.isResTemplate(name) : isTemplate);
		
		var editorEle = po.elementOfId(po.resourceCodeEditorEleId(name));
		var codeEditor = editorEle.data("codeEditorInstance");
		if(!codeEditor)
		{
			var codeEditorOptions =
			{
				value: content,
				matchBrackets: true,
				matchTags: true,
				autoCloseTags: true,
				autoCloseBrackets: true,
				readOnly: po.isReadonlyAction,
				foldGutter: true,
				gutters: ["CodeMirror-linenumbers", "CodeMirror-foldgutter"],
				mode: po.evalCodeModeByName(name)
			};
			
			if(isTemplate && !codeEditorOptions.readOnly)
			{
				codeEditorOptions.hintOptions =
				{
					hint: po.codeEditorHintHandler
				};
			}
			
			codeEditor = po.createCodeEditor(editorEle, codeEditorOptions);
			editorEle.data("codeEditorInstance", codeEditor);
		}
		else
			po.setCodeText(codeEditor, content);
	};

	po.codeEditorHintHandler = function(codeEditor)
	{
		var doc = codeEditor.getDoc();
		var cursor = doc.getCursor();
		var mode = (codeEditor.getModeAt(cursor) || {});
		var token = (codeEditor.getTokenAt(cursor) || {});
		var tokenString = (token ? $.trim(token.string) : "");
		
		//"dg*"的HTML元素属性
		if("xml" == mode.name && "attribute" == token.type && /^dg/i.test(tokenString))
		{
			var myTagToken = po.findPrevTokenOfType(codeEditor, doc, cursor, token, "tag");
			var myCategory = (myTagToken ? myTagToken.string : null);
			
			var completions =
			{
				list: po.findCompletionList(po.codeEditorCompletionsTagAttr, tokenString, myCategory),
				from: CodeMirror.Pos(cursor.line, token.start),
				to: CodeMirror.Pos(cursor.line, token.end)
			};
			
			return completions;
		}
		//javascript函数
		else if("javascript" == mode.name && (tokenString == "." || "property" == token.type))
		{
			var myVarTokenInfo = po.findPrevTokenInfo(codeEditor, doc, cursor, token,
					function(token){ return (token.type == "variable" || token.type == "variable-2"); });
			var myVarToken = (myVarTokenInfo ? myVarTokenInfo.token : null);
			var myCategory = (myVarToken ? myVarToken.string : "");
			
			//无法确定要补全的是看板还是图表对象，所以这里采用：完全匹配变量名，否则就全部提示
			// *dashboard*
			if(/dashboard/i.test(myCategory))
				myCategory = "dashboard";
			// *chart*
			else if(/chart/i.test(myCategory))
				myCategory = "chart";
			else
				myCategory = null;
			
			var completions =
			{
				list: po.findCompletionList(po.codeEditorCompletionsJsFunction, (tokenString == "." ? "" : tokenString), myCategory),
				from: CodeMirror.Pos(cursor.line, (tokenString == "." ? token.start + 1 : token.start)),
				to: CodeMirror.Pos(cursor.line, token.end)
			};
			
			return completions;
		}
	};
	
	po.setupResourceEditor = function()
	{
		var fm = po.vueFormModel();
		var pm = po.vuePageModel();
		
		po.vuePageModel(
		{
			templateEditModeOptions:
			[
				{ name: "<@spring.message code='dashboard.templateEditMode.code' />", value: "code" },
				{ name: "<@spring.message code='dashboard.templateEditMode.visual' />", value: "visual" }
			],
			resourceContentTabs:
			{
				items: [],
				activeIndex: 0
			},
			resourceContentTabMenuItems:
			[
				{
					label: "<@spring.message code='close' />",
					command: function()
					{
						po.tabviewClose(po.vuePageModel().resourceContentTabs, po.resourceContentTabMenuTargetId);
					}
				},
				{
					label: "<@spring.message code='closeOther' />",
					command: function()
					{
						po.tabviewCloseOther(po.vuePageModel().resourceContentTabs, po.resourceContentTabMenuTargetId);
					}
				},
				{
					label: "<@spring.message code='closeRight' />",
					command: function()
					{
						po.tabviewCloseRight(po.vuePageModel().resourceContentTabs, po.resourceContentTabMenuTargetId);
					}
				},
				{
					label: "<@spring.message code='closeLeft' />",
					command: function()
					{
						po.tabviewCloseLeft(po.vuePageModel().resourceContentTabs, po.resourceContentTabMenuTargetId);
					}
				},
				{
					label: "<@spring.message code='closeAll' />",
					command: function()
					{
						po.tabviewCloseAll(po.vuePageModel().resourceContentTabs);
					}
				}
			],
			tplCodeEditMenuItems:
			[
				{
					label: "<@spring.message code='save' />"
				}
			],
			tplVisualEditMenuItems:
			[
				{
					label: "<@spring.message code='select' />",
					items:
					[
						{
							label: "<@spring.message code='nextElement' />",
							command: function()
							{
							}
						},
						{
							label: "<@spring.message code='prevElement' />",
							command: function()
							{
							}
						},
						{
							label: "<@spring.message code='subElement' />",
							command: function()
							{
							}
						},
						{
							label: "<@spring.message code='parentElement' />",
							command: function()
							{
							}
						},
						{ separator: true },
						{
							label: "<@spring.message code='cancelSelect' />",
							command: function()
							{
							}
						}
					]
				},
				{
					label: "<@spring.message code='insert' />",
					items:
					[
						{ label: "<@spring.message code='outerInsertAfter' />", items: po.buildTplVisualInsertMenuItems("after") },
						{ label: "<@spring.message code='outerInsertBefore' />", items: po.buildTplVisualInsertMenuItems("before") },
						{ label: "<@spring.message code='innerInsertAfter' />", items: po.buildTplVisualInsertMenuItems("append") },
						{ label: "<@spring.message code='innerInsertBefore' />", items: po.buildTplVisualInsertMenuItems("prepend") },
						{ separator: true },
						{ label: "<@spring.message code='bindOrReplaceChart' />" }
					]
				},
				{
					label: "<@spring.message code='edit' />",
					items:
					[
						{ label: "<@spring.message code='globalStyle' />" },
						{ label: "<@spring.message code='globalChartTheme' />" },
						{ label: "<@spring.message code='globalChartOptions' />" },
						{ separator: true },
						{ label: "<@spring.message code='style' />" },
						{ label: "<@spring.message code='chartTheme' />" },
						{ label: "<@spring.message code='chartOptions' />" },
						{ label: "<@spring.message code='elementAttribute' />" },
						{ label: "<@spring.message code='textContent' />" }
					]
				},
				{
					label: "<@spring.message code='delete' />",
					items:
					[
						{ label: "<@spring.message code='deleteElement' />" },
						{ separator: true },
						{ label: "<@spring.message code='unbindChart' />" }
					]
				},
				{
					label: "<@spring.message code='save' />"
				},
				{
					label: "<@spring.message code='more' />",
					items:
					[
						{
							label: "<@spring.message code='dashboardSize' />",
							command: function()
							{
							}
						},
						{
							label: "<@spring.message code='elementBorderLine' />",
							command: function()
							{
							}
						},
						{
							label: "<@spring.message code='refresh' />",
							command: function()
							{
							}
						}
					]
				}
			]
		});
		
		po.vueMethod(
		{
			onResourceContentTabMenuToggle: function(e, tabId)
			{
				po.resourceContentTabMenuTargetId = tabId;
				po.vueUnref("${pid}resourceContentTabMenuEle").show(e);
			},
			
			resourceCodeEditorEleId: function(name)
			{
				return po.resourceCodeEditorEleId(name);
			},
			
			resourceVisualEditorEleId: function(name)
			{
				return po.resourceVisualEditorEleId(name);
			}
		});
		
		po.vueRef("${pid}resourceContentTabMenuEle", null);
		
		//po.showResourceContentTab()里不能里可获取到创建的DOM元素，所以采用此方案
		po.vueWatch(pm.resourceContentTabs, function(oldVal, newVal)
		{
			var items = newVal.items;
			var activeIndex = newVal.activeIndex;
			var activeTab = items[activeIndex];
			
			if(activeTab)
			{
				//XXX vueMounted()回调函数中vueApp()为null？
				if(po.vueApp())
				{
					po.vueApp().$nextTick(function()
					{
						po.loadResourceContent(activeTab.resourceName);
					});
				}
				else
					po.loadResourceContent(activeTab.resourceName);
			}
		});
		
		po.vueMounted(function()
		{
			var fm = po.vueFormModel();
			if(fm.templates && fm.templates.length > 0)
				po.showResourceContentTab(fm.templates[0]);
		});
	};
})
(${pid});
</script>
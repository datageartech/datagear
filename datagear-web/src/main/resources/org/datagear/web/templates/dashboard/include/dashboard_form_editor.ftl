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
				<p-menubar :model="pm.tplVisualEditMenuItems" class="light-menubar no-root-icon-menubar text-sm">
				</p-menubar>
			</div>
			<div class="pt-1">
				<div class="code-editor-wrapper template-editor-wrapper p-component p-inputtext">
					<div :id="tab.id + 'codeEditor'" class="code-editor"></div>
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
	po.templateNameTabId = function(name)
	{
		var map = (po.templateNameTabIdMap || (po.templateNameTabIdMap = {}));
		
		//不直接使用tableName作为元素ID，因为name中可能存在与jquery冲突的字符，比如'$'
		var value = map[name];
		
		if(value == null)
		{
			value = $.uid("templateNameTab");
			map[name] = value;
		}
		
		return value;
	};
	
	po.templateNameToTabItem = function(name)
	{
		var re =
		{
			id: po.templateNameTabId(name),
			key: name,
			title: name,
			editMode: "code"
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
	
	po.setupResourceEditor = function()
	{
		var fm = po.vueFormModel();
		
		po.vuePageModel(
		{
			templateEditModeOptions:
			[
				{ name: "<@spring.message code='dashboard.templateEditMode.code' />", value: "code" },
				{ name: "<@spring.message code='dashboard.templateEditMode.visual' />", value: "visual" }
			],
			resourceContentTabs:
			{
				items: (fm.templates.length > 0 ? [ po.templateNameToTabItem(fm.templates[0]) ] : []),
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
			tplVisualEditMenuItems:
			[
				{
					label: "<@spring.message code='quickExecute' />",
				},
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
			}
		});
		
		po.vueRef("${pid}resourceContentTabMenuEle", null);
	};
})
(${pid});
</script>
</body>
</html>
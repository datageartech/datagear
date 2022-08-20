<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#include "../include/page_import.ftl">
<#include "../include/html_doctype.ftl">
<html>
<head>
<#include "../include/html_head.ftl">
<title>
	<#include "../include/html_app_name_prefix.ftl">
	<@spring.message code='module.sqlpad' />
</title>
</head>
<body class="p-card no-border">
<#include "../include/page_obj.ftl">
<div id="${pid}" class="page page-manager page-sqlpad">
	<div class="page-header grid align-items-center">
		<div class="col-12 flex">
			<div class="flex-grow-1 h-opts">
				<p-button type="button" icon="pi pi-play" class="px-4"></p-button>
				<p-button type="button" icon="pi pi-stop" class="px-4"></p-button>
				<p-button type="button" icon="pi pi-check" class="p-button-secondary px-4 ml-4"></p-button>
				<p-button type="button" icon="pi pi-undo" class="p-button-secondary px-4"></p-button>
				<span class="p-inputgroup inline-flex w-auto">
					<p-inputtext class="ml-4" style="width:6rem;"></p-inputtext>
					<p-button type="button" icon="pi pi-align-left" class="p-button-secondary px-4"></p-button>
					<p-button type="button" icon="pi pi-align-right" class="p-button-secondary px-4"></p-button>
				</span>
				<p-button type="button" icon="pi pi-trash" class="p-button-secondary px-4 ml-4"></p-button>
			</div>
			<div class="flex-grow-0 text-right h-opts">
				<p-button type="button" icon="pi pi-history" class="p-button-secondary px-4"></p-button>
				<p-button type="button" icon="pi pi-ellipsis-h" class="p-button-secondary px-4"></p-button>
			</div>
		</div>
	</div>
	<div class="page-content">
		<p-splitter layout="vertical" class="h-full">
			<p-splitterpanel :size="60" :min-size="20" class="overflow-auto">
				<div id="${pid}sql" class="code-editor-wrapper input p-component p-inputtext w-full h-full border-0">
					<div id="${pid}codeEditor" class="code-editor"></div>
				</div>
			</p-splitterpanel>
			<p-splitterpanel :size="40" :min-size="20" class="overflow-auto">
				<div class="sqlpad-tabs-wrapper w-full h-full">
					<p-tabview v-model:active-index="pm.sqlpadTabs.activeIndex" :scrollable="true" @tab-change="onSqlpadTabChange"
						@tab-click="onSqlpadTabClick" class="contextmenu-tabview light-tabview" :class="{'opacity-0': pm.sqlpadTabs.items.length == 0}">
						<p-tabpanel v-for="tab in pm.sqlpadTabs.items" :key="tab.id" :header="tab.title">
							<template #header>
								<p-button type="button" icon="pi pi-angle-down"
									class="context-menu-btn p-button-xs p-button-secondary p-button-text p-button-rounded"
									@click="onSqlpadTabMenuToggle($event, tab)" aria-haspopup="true" aria-controls="${pid}sqlpadTabMenu"
									v-if="tab.closeable !== false">
								</p-button>
							</template>
							<div :id="tab.id"></div>
						</p-tabpanel>
					</p-tabview>
					<p-menu id="${pid}sqlpadTabMenu" ref="${pid}sqlpadTabMenuEle" :model="pm.sqlpadTabMenuItems" :popup="true" class="text-sm"></p-menu>
				</div>
			</p-splitterpanel>
		</p-splitter>
	</div>
</div>
<#include "../include/page_code_editor.ftl">
<#include "../include/page_sql_editor.ftl">
<#include "../include/page_tabview.ftl">
<script>
(function(po)
{
	po.schemaId = "${schema.id}";
	po.sqlpadId = "${sqlpadId}";
	
	po.getSqlEditorSchemaId = function()
	{
		return po.schemaId;
	};
	
	po.vuePageModel(
	{
		sqlpadTabs:
		{
			items:
			[
				{
					id: $.uid("sqlpadmsg"),
					title: "<@spring.message code='message' />",
					closeable: false
				}
			],
			activeIndex: 0
		},
		sqlpadTabMenuItems:
		[
			{
				label: "<@spring.message code='close' />",
				command: function()
				{
					po.tabviewClose(po.vuePageModel().sqlpadTabs, po.sqlpadTabMenuOnTabId);
				}
			},
			{
				label: "<@spring.message code='closeOther' />",
				command: function()
				{
					po.tabviewCloseOther(po.vuePageModel().sqlpadTabs, po.sqlpadTabMenuOnTabId);
				}
			},
			{
				label: "<@spring.message code='closeRight' />",
				command: function()
				{
					po.tabviewCloseRight(po.vuePageModel().sqlpadTabs, po.sqlpadTabMenuOnTabId);
				}
			},
			{
				label: "<@spring.message code='closeLeft' />",
				command: function()
				{
					po.tabviewCloseLeft(po.vuePageModel().sqlpadTabs, po.sqlpadTabMenuOnTabId);
				}
			},
			{
				label: "<@spring.message code='closeAll' />",
				command: function()
				{
					po.tabviewCloseAll(po.vuePageModel().sqlpadTabs);
				}
			}
		]
	});

	po.vueRef("${pid}sqlpadTabMenuEle", null);
	
	po.vueMethod(
	{
		onSqlpadTabChange: function(e){},
		
		onSqlpadTabClick: function(e){},
		
		onSqlpadTabMenuToggle: function(e, tab)
		{
			po.sqlpadTabMenuOnTabId = tab.id;
			po.vueUnref("${pid}sqlpadTabMenuEle").show(e);
		}
	});
	
	po.vueMounted(function()
	{
		po.codeEditor = po.createSqlEditor(po.elementOfId("${pid}codeEditor"));
		po.setCodeTextTimeout(po.codeEditor, "", true);
	});
	
	po.vueMount();
})
(${pid});
</script>
</body>
</html>
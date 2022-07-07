<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#include "include/page_import.ftl">
<#include "include/html_doctype.ftl">
<html>
<head>
<#include "include/html_head.ftl">
<title><#include "include/html_app_name_prefix.ftl"><@spring.message code='module.main' /></title>
</head>
<body class="m-0 surface-ground">
<#include "include/page_obj.ftl">
<div id="${pid}">
	<div class="flex flex-column h-screen m-0">
		<#include "include/page_main_header.ftl">
		<div class="page-main-content flex-grow-1 p-0">
			<div class="grid h-full m-0 flex-nowrap">
				<div class="page-main-menu col-fixed px-0">
					<div class="grid grid-nogutter flex-column align-items-center p-card h-full">
						<div class="col-fixed">
							<p-button @click="onMainMenuToggle" icon="pi pi-align-justify" class="p-button-secondary p-button-text p-button-sm opacity-40 my-1 p-1"></p-button>
						</div>
						<div class="col">
							<p-tabmenu :model="pm.mainMenu.items" v-model:active-index="pm.mainMenu.active"
										@tab-change="onMainMenuTabChange"
										class="vertical-tabmenu" :class="{collapse: pm.mainMenu.collapse}">
							</p-tabmenu>
						</div>
					</div>
				</div>
				<div id="${pid}-mainPanels" class="page-main-panels col overflow-auto"></div>
			</div>
		</div>
	</div>
</div>
<script>
(function(po)
{
	po.vuePageModel(
	{
		mainMenu:
		{
			active: -1,
			collapse: false,
			items:
			[
				{
					label: "<@spring.message code='module.dataSource' />",
					icon: 'pi pi-fw pi-database',
					url: "${contextPath}/primevue/chartList"
				},
				{
					label: "<@spring.message code='module.analysisProject' />",
					icon: 'pi pi-fw pi-folder',
					url: "${contextPath}/analysisProject/pagingQuery"
				},
				{
					label: "<@spring.message code='module.dataSet' />",
					icon: 'pi pi-fw pi-table',
					url: "${contextPath}/dataSet/pagingQuery"
				},
				{
					label: "<@spring.message code='module.chart' />",
					icon: 'pi pi-fw pi-chart-line',
					url: "${contextPath}/chart/pagingQuery"
				},
				{
					label: "<@spring.message code='module.dashboard' />",
					icon: 'pi pi-fw pi-images',
					url: "${contextPath}/dashboard/pagingQuery"
				}
			]
		}
	});
	
	po.vueMethod(
	{
		onMainMenuToggle: function()
		{
			var pm = po.vuePageModel();
			pm.mainMenu.collapse = !pm.mainMenu.collapse;
		},
		onMainMenuTabChange: function(e)
		{
			e.originalEvent.preventDefault();
			
			var mainMenu = po.vuePageModel().mainMenu;
			var item = mainMenu.items[e.index];
			
			po.showMainPanel("mainMenuTab"+item.label, item.url, item.label);
		}
	});
	
	po.showMainPanel = function(name, url)
	{
		const parent = po.elementOfId("${pid}-mainPanels");
		const panelId = "${pid}mainPanel"+name;
		var panel = po.elementOfId(panelId, parent);
		
		po.element("> .page-main-panel", parent).addClass("hidden");
		
		if(panel.length == 0)
		{
			panel = $("<div id='"+panelId+"' class='page-main-panel p-card w-full h-full p-3' />")
						.prop("unloaded", true).appendTo(parent);
		}
		
		panel.removeClass("hidden");
		
		if(panel.prop("unloaded"))
		{
			panel.empty();
			
			po.open(url,
			{
				target: panel,
				dialog: false,
				success: function()
				{
					panel.removeProp("unloaded");
				}
			});
		}
	};
	
	po.vueMount();
})
(${pid});
</script>
</body>
</html>
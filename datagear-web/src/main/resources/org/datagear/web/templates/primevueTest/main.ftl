<#include "include/page_import.ftl">
<#include "include/html_doctype.ftl">
<html>
<head>
<#include "include/html_head.ftl">
<title><#include "include/html_app_name_prefix.ftl">主页</title>
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
							<p-button @click="toggleMainMenu" icon="pi pi-align-justify" class="p-button-secondary p-button-text p-button-sm opacity-40 my-1 p-1"></p-button>
						</div>
						<div class="col">
							<p-tabmenu :model="mainMenu.items" v-model:active-index="mainMenu.active"
										@tab-change="handleMainMenuChange"
										class="vertical-tabmenu" :class="{collapse: mainMenu.collapse}">
							</p-tabmenu>
						</div>
					</div>
				</div>
				<div id="${pid}mainPanels" class="page-main-panels col overflow-auto"></div>
			</div>
		</div>
	</div>
</div>
<script>
(function(po)
{
	po.vueRef("mainMenu",
	{
		active: -1,
		collapse: false,
		items:
		[
			{
				label: '数据源',
				icon: 'pi pi-fw pi-database',
				url: "${contextPath}/primevue/chartList"
			},
			{
				label: '项目',
				icon: 'pi pi-fw pi-folder',
				url: "${contextPath}/primevue/chartList"
			},
			{
				label: '数据集',
				icon: 'pi pi-fw pi-table',
				url: "${contextPath}/primevue/chartList"
			},
			{
				label: '图表',
				icon: 'pi pi-fw pi-chart-line',
				url: "${contextPath}/primevue/chartList"
			},
			{
				label: '看板',
				icon: 'pi pi-fw pi-images',
				url: "${contextPath}/primevue/dashboardList"
			}
		]
	});
	
	po.vueSetup("toggleMainMenu", function()
	{
		var mainMenu = po.vueRef("mainMenu");
		mainMenu.collapse = !mainMenu.collapse;
	});
	
	po.showMainPanel = function(name, url)
	{
		const parent = $("#${pid}mainPanels");
		const panelId = "${pid}mainPanel"+name;
		var panel = po.elementOfId(panelId, parent);
		
		po.element("> .page-main-panel", parent).addClass("hidden");
		
		if(panel.length == 0)
		{
			panel = $("<div id='"+panelId+"' class='page-main-panel p-card w-full h-full p-3' />").prop("unloaded", true).appendTo(parent);
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
	
	po.vueSetup("handleMainMenuChange", function(e)
	{
		e.originalEvent.preventDefault();
		
		var mainMenu = po.vueRef("mainMenu");
		var item = mainMenu.items[e.index];
		po.showMainPanel("mainMenuTab"+item.label, item.url, item.label);
	});
	
	po.vueMount();
})
(${pid});
</script>
</body>
</html>
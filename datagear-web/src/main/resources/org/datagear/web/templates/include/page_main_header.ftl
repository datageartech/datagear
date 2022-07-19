<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#--
页头。

变量：
//当前用户，不允许为null
User currentUser

-->
<#assign Themes=statics['org.datagear.web.util.Themes']>
<div class="page-main-header flex-grow-0 p-card no-border text-primary py-1">
	<div class="grid grid-nogutter align-items-center">
		<div class="logo col-fixed pl-1">
			<#include "html_logo.ftl">
		</div>
		<div class="col text-right pr-2">
			<div class="flex justify-content-end align-items-center">
				<div class="mr-1">
					<#if currentUser.anonymous>
						<a href="${contextPath}/login" class="link px-1"><@spring.message code='module.login' /></a>
						<a href="${contextPath}/" class="link px-1"><@spring.message code='module.main' /></a>
					<#else>
						${currentUser.nameLabel}
					</#if>
				</div>
				<div>
					<p-button type="button" @click="onSysMenuToggle" aria-haspopup="true" aria-controls="${pid}-sysMenu" icon="pi pi-cog" class="p-button-text p-button-rounded p-button-sm text-primary"></p-button>
					<p-contextmenu id="${pid}-sysMenu" ref="sysMenuEle" :model="sysMenu.items" :popup="true"></p-contextmenu>
				</div>
			</div>
		</div>
	</div>
</div>
<script>
(function(po)
{
	po.isUserAnonymous = ("${currentUser.anonymous?string('true','false')}" == "true");
	po.isUserAdmin = ("${currentUser.admin?string('true','false')}" == "true");
	
	po.openSysMenuDialog = function(e, tableDialog)
	{
		tableDialog = (tableDialog == null ? true : tableDialog);
		
		e.originalEvent.preventDefault();
		
		if(tableDialog)
			po.openTableDialog(e.item.url);
		else
			po.open(e.item.url);
	};
	
	po.changeTheme = function(themeName)
	{
		po.getJson("/changeThemeData?theme=" + themeName, function(data)
		{
			$.each(data, function(idx, item)
			{
				$("#"+item.cssId).attr("href", item.href);
			});
		});
	};
	
	var sysMenuItems = [];
	
	if(!po.isUserAnonymous)
	{
		sysMenuItems = sysMenuItems.concat(
		[
			{
				label: "<@spring.message code='module.personalSet' />",
				url: "${contextPath}/user/personalSet",
				command: function(e){ po.openSysMenuDialog(e, false); }
				
			},
			{ separator: true }
		]);
	}
	
	if(po.isUserAdmin)
	{
		sysMenuItems = sysMenuItems.concat(
		[
			{
				label: "<@spring.message code='dataSource' />",
				items:
				[
					{
						label: "<@spring.message code='module.driverEntity' />",
						url: "${contextPath}/driverEntity/query",
						command: function(e){ po.openSysMenuDialog(e); }
					},
					{
						label: "<@spring.message code='module.schemaUrlBuilder' />",
						url: "${contextPath}/schemaUrlBuilder/set",
						command: function(e){ po.openSysMenuDialog(e); }
					},
					{
						label: "<@spring.message code='module.schemaGuard' />",
						url: "${contextPath}/schemaGuard/query",
						command: function(e){ po.openSysMenuDialog(e); }
					}
				]
			},
			{
				label: "<@spring.message code='dataAnalysis' />",
				items:
				[
					{
						label: "<@spring.message code='module.dataSetResDirectory' />",
						url: "${contextPath}/dataSetResDirectory/pagingQuery",
						command: function(e){ po.openSysMenuDialog(e); }
					},
					{
						label: "<@spring.message code='module.chartPlugin' />",
						url: "${contextPath}/chartPlugin/query",
						command: function(e){ po.openSysMenuDialog(e); }
					},
					{
						label: "<@spring.message code='module.dashboardGlobalRes' />",
						url: "${contextPath}/dashboardGlobalRes/query",
						command: function(e){ po.openSysMenuDialog(e); }
					}
				]
			},
			{
				label: "<@spring.message code='systemManager' />",
				items:
				[
					{
						label: "<@spring.message code='module.user' />",
						url: "${contextPath}/user/pagingQuery",
						command: function(e){ po.openSysMenuDialog(e); }
					},
					{
						label: "<@spring.message code='module.role' />",
						url: "${contextPath}/role/pagingQuery",
						command: function(e){ po.openSysMenuDialog(e); }
					}
				]
			},
			{ separator: true }
		]);
	}
	
	sysMenuItems = sysMenuItems.concat(
	[
		{
			label: "<@spring.message code='module.changeTheme' />",
			items:
			[
				{
					label: "<@spring.message code='module.changeTheme.blue' />",
					command: function(e)
					{
						po.changeTheme("${Themes.BLUE}");
					}
				},
				{
					label: "<@spring.message code='module.changeTheme.blueDark' />",
					command: function(e)
					{
						po.changeTheme("${Themes.BLUE_DARK}");
					}
				}
			]
		},
		{
			label: "<@spring.message code='module.changeLanguage' />",
			items:
			[
				{ label: "<@spring.message code='module.changeLanguage.cn' />" },
				{ label: "<@spring.message code='module.changeLanguage.en' />" }
			]
		},
		{
			label: "<@spring.message code='help' />",
			items:
			[
				{
					label: "<@spring.message code='module.about' />",
					url: "${contextPath}/about",
					command: function(e){ po.openSysMenuDialog(e); }
				},
				{
					label: "<@spring.message code='module.documentation' />",
					url: "${statics['org.datagear.util.Global'].WEB_SITE}/documentation/",
					target: "_blank"
				},
				{
					label: "<@spring.message code='module.changelog' />",
					url: "${contextPath}/changelog",
					command: function(e){ po.openSysMenuDialog(e); }
				},
				{
					label: "<@spring.message code='module.downloadLatestVersion' />",
					url: "${statics['org.datagear.util.Global'].WEB_SITE}",
					target: "_blank"
				}
			]
		},
		{ separator: true },
		{
			label: "<@spring.message code='module.logout' />",
			url: "${contextPath}/logout",
			class: "p-error"
		}
	]);
	
	po.vueReactive("sysMenu",
	{
		items: sysMenuItems
	});
	
	po.vueMethod(
	{
		onSysMenuToggle: function(e)
		{
			po.vueRef("sysMenuEle").toggle(e);
		}
	});
	
	po.vueRef("sysMenuEle", null);
})
(${pid});
</script>

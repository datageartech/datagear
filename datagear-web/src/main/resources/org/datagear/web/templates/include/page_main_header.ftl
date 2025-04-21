<#--
 *
 * Copyright 2018-present datagear.tech
 *
 * This file is part of DataGear.
 *
 * DataGear is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * DataGear is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with DataGear.
 * If not, see <https://www.gnu.org/licenses/>.
 *
-->
<#--
页头。

变量：
//当前用户，不允许为null
User currentUser
//检测新版本结果，不允许为null
DetectResult detectNewVersionResult

-->
<#assign ThemeSpec=statics['org.datagear.web.util.ThemeSpec']>
<#assign Global=statics['org.datagear.util.Global']>
<#assign WebUtils=statics['org.datagear.web.util.WebUtils']>
<div id="${pid}mainHeader" class="page-main-header flex-grow-0 p-card no-border text-primary py-1 border-noround-top border-noround-bottom">
	<div class="grid grid-nogutter align-items-center">
		<div id="sysLogoWrapper" class="logo-wrapper header-left col-fixed flex align-items-center pl-1">
			<#include "html_logo.ftl">
		</div>
		<div class="col text-right pr-2">
			<div class="header-right flex justify-content-end align-items-center">
				<div class="mr-1">
					<#if currentUser.anonymous>
						<a href="${contextPath}/login" class="link text-primary px-1"><@spring.message code='module.login' /></a>
						<a href="${contextPath}/" class="link text-primary px-1"><@spring.message code='module.main' /></a>
					<#else>
						<span class="text-color-secondary">
							<i class="pi pi-user text-sm"></i>
							${currentUser.nameLabel}
						</span>
					</#if>
				</div>
				<div>
					<p-button type="button" @click="onSysMenuToggle" aria-haspopup="true" aria-controls="${pid}sysMenu" icon="pi pi-cog"
						class="p-button-sm p-button-text p-button-rounded text-primary"
						:class="pm.newVersionDetectedTipClassName">
					</p-button>
					<p-tieredmenu id="${pid}sysMenu" ref="${pid}sysMenuEle" :model="pm.sysMenuItems" :popup="true" @show="onSysMenuShow"
						class="left-submenu-list">
					</p-tieredmenu>
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
	
	po.detectedVersionInfo =
	{
		latestVersionVar : "${detectNewVersionResult.latestVersionVar}",
		detectedVersionCookieName : "${detectNewVersionResult.versionCookieName}",
		detectedVersionCookieExpDays : parseInt("${detectNewVersionResult.versionCookieExpDays}"),
		currentVersion : "${detectNewVersionResult.currentVersion}"
	};
	
	po.detectedVersionInfo.detectedVersion = $.cookie(po.detectedVersionInfo.detectedVersionCookieName);
	po.detectedVersionInfo.latestVersion = ($.localStorageItem(po.detectedVersionInfo.latestVersionVar) || "");
	
	po.newVersionDetected = function()
	{
		var dvi = po.detectedVersionInfo;
		var detectedVersion = dvi.detectedVersion;
		
		if(detectedVersion != dvi.latestVersion)
		{
			detectedVersion = dvi.latestVersion;
			$.cookie(dvi.detectedVersionCookieName, detectedVersion, {expires : dvi.detectedVersionCookieExpDays, path : po.concatContextPath("/")});
		}
		
		if(!detectedVersion)
			return false;
		
		return ($.compareVersion(detectedVersion, dvi.currentVersion) > 0);
	};
	
	po.isNewVersionDetected = po.newVersionDetected();
	po.newVersionDetectedTipClassName = (po.isNewVersionDetected ? "new-version-tip" : "");
	
	po.openSysMenuDialog = function(e, tableDialog)
	{
		tableDialog = (tableDialog == null ? true : tableDialog);
		
		e.originalEvent.preventDefault();
		
		if(tableDialog)
			po.openTableDialog(e.item.url, {fullUrl: true});
		else
			po.open(e.item.url, {fullUrl: true});
	};
	
	po.changeTheme = function(themeName)
	{
		//WebMvcConfigurerConfigSupport.THEME_PARAM
		po.getJson("/changeThemeData?THEME=" + themeName, function(data)
		{
			$.each(data, function(idx, item)
			{
				if(item.changeAttr)
				{
					$(item.changeElement).attr(item.changeAttr, item.changeValue);
				}
				else if(item.changeHtml)
				{
					$(item.changeElement).html(item.changeValue);
				}
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
				url: po.concatContextPath("/user/personalSet"),
				command: function(e){ po.openSysMenuDialog(e, false); }
			},
			{
				label: "<@spring.message code='module.editPassword' />",
				url: po.concatContextPath("/user/personalPsd"),
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
						url: po.concatContextPath("/driverEntity/manage"),
						command: function(e){ po.openSysMenuDialog(e); }
					},
					{
						label: "<@spring.message code='module.dtbsSourceUrlBuilder' />",
						url: po.concatContextPath("/dtbsSourceUrlBuilder/set"),
						command: function(e){ po.openSysMenuDialog(e); }
					},
					{
						label: "<@spring.message code='module.dtbsSourceGuard' />",
						url: po.concatContextPath("/dtbsSourceGuard/manage"),
						command: function(e){ po.openSysMenuDialog(e); }
					}
				]
			},
			{
				label: "<@spring.message code='dataAnalysis' />",
				items:
				[
					{
						label: "<@spring.message code='module.fileSource' />",
						url: po.concatContextPath("/fileSource/manage"),
						command: function(e){ po.openSysMenuDialog(e); }
					},
					{
						label: "<@spring.message code='module.chartPlugin' />",
						url: po.concatContextPath("/chartPlugin/manage"),
						command: function(e){ po.openSysMenuDialog(e); }
					},
					{
						label: "<@spring.message code='module.dashboardGlobalRes' />",
						url: po.concatContextPath("/dashboardGlobalRes/manage"),
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
						url: po.concatContextPath("/user/manage"),
						command: function(e){ po.openSysMenuDialog(e); }
					},
					{
						label: "<@spring.message code='module.role' />",
						url: po.concatContextPath("/role/manage"),
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
						po.changeTheme("${ThemeSpec.BLUE}");
					}
				},
				{
					label: "<@spring.message code='module.changeTheme.blueDark' />",
					command: function(e)
					{
						po.changeTheme("${ThemeSpec.BLUE_DARK}");
					}
				}
			]
		},
		/*
		{
			label: "<@spring.message code='module.changeLanguage' />",
			items:
			[
				{ label: "<@spring.message code='module.changeLanguage.cn' />" },
				{ label: "<@spring.message code='module.changeLanguage.en' />" }
			]
		},
		*/
		{
			label: "<@spring.message code='help' />",
			class: po.newVersionDetectedTipClassName,
			items:
			[
				{
					label: "<@spring.message code='module.about' />",
					url: po.concatContextPath("/about"),
					command: function(e){ po.openSysMenuDialog(e, false); }
				},
				{
					label: "<@spring.message code='module.documentation' />",
					url: "${Global.WEB_SITE}/documentation/",
					target: "_blank"
				},
				{
					label: "<@spring.message code='module.changelog' />",
					url: po.concatContextPath("/changelog"),
					command: function(e){ po.openSysMenuDialog(e); }
				},
				{
					label: "<@spring.message code='module.downloadLatestVersion' />",
					class: "item-download-latest-version " + po.newVersionDetectedTipClassName,
					url: "${Global.WEB_SITE}",
					target: "_blank"
				}
			]
		},
		{
			label: "<@spring.message code='enterpriseVersion' />",
			class: "enterprise-menuitem",
			url: "${Global.WEB_SITE}/pro/",
			target: "_blank"
		}
	]);
	
	if(!po.isUserAnonymous)
	{
		sysMenuItems = sysMenuItems.concat(
		[
			{ separator: true },
			{
				label: "<@spring.message code='module.logout' />",
				url: po.concatContextPath("/logout"),
				class: "danger-menuitem"
			}
		]);
	}
	
	po.vuePageModel(
	{
		sysMenuItems: sysMenuItems,
		newVersionDetectedTipClassName: po.newVersionDetectedTipClassName
	});
	
	po.vueMethod(
	{
		onSysMenuToggle: function(e)
		{
			po.vueUnref("${pid}sysMenuEle").toggle(e);
		},
		onSysMenuShow: function()
		{
			if(po.isNewVersionDetected)
			{
				var sysMenu = po.elementOfId("${pid}sysMenu", document.body);
				var downloadLatestVersionItem = po.element(".item-download-latest-version", sysMenu);
				if(downloadLatestVersionItem.length > 0)
				{
					var label = po.element(".p-menuitem-link > span", downloadLatestVersionItem);
					var tipVersion = po.element(".tip-latest-version", label);
					
					if(tipVersion.length == 0)
						tipVersion = $("<i class='tip-latest-version' />").appendTo(label);
					
					tipVersion.text("v"+po.detectedVersionInfo.latestVersion);
				}
			}
		}
	});
	
	po.vueRef("${pid}sysMenuEle", null);
})
(${pid});
</script>
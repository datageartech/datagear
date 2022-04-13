<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#--
系统设置菜单。

依赖：
page_obj.ftl

变量：
//当前用户，不允许为null
User currentUser
//检测新版本的script脚本
String detectNewVersionScript
-->
<ul id="systemSetMenu" class="lightweight-menu">
	<li><span><span class="ui-icon ui-icon-gear"></span><span class="new-version-tip"></span></span>
		<ul style="display:none;" class="ui-widget-shadow">
			<#if !currentUser.anonymous>
			<#if currentUser.admin>
			<li menu-name="driverEntity"><a href="${contextPath}/driverEntity/query"><@spring.message code='main.manageDriverEntity' /></a></li>
			<li menu-name="schemaUrlBuilder"><a href="${contextPath}/schemaUrlBuilder/editScriptCode"><@spring.message code='schemaUrlBuilder.schemaUrlBuilder' /></a></li>
			<li menu-name="schemaGuard"><a href="${contextPath}/schemaGuard/query"><@spring.message code='main.manageSchemaGuard' /></a></li>
			<li class="ui-widget-header"></li>
			<li menu-name="dataSetResDirectory"><a href="${contextPath}/dataSetResDirectory/pagingQuery"><@spring.message code='main.manageDataSetResDirectory' /></a></li>
			<li menu-name="chartPlugin"><a href="${contextPath}/chartPlugin/query"><@spring.message code='main.manageChartPlugin' /></a></li>
			<li menu-name="dashboardGlobalRes"><a href="${contextPath}/dashboardGlobalRes/query"><@spring.message code='main.manageDashboardGlobalRes' /></a></li>
			<li class="ui-widget-header"></li>
			<li menu-name="user"><a href="${contextPath}/user/pagingQuery"><@spring.message code='main.manageUser' /></a></li>
			<li menu-name="role"><a href="${contextPath}/role/pagingQuery"><@spring.message code='main.manageRole' /></a></li>
			</#if>
			<li menu-name="personalSet"><a href="${contextPath}/user/personalSet"><@spring.message code='main.personalSet' /></a></li>
			<li class="ui-widget-header"></li>
			</#if>
			<li class=""><a href="javascript:void(0);"><@spring.message code='main.changeTheme' /></a>
				<ul class="ui-widget-shadow">
					<li menu-name="theme" theme="${statics['org.datagear.web.util.Themes'].LIGHT}"><a href="javascript:void(0);"><span class="ui-widget ui-widget-content theme-sample theme-sample-light"></span><@spring.message code='main.changeTheme.light' /></a></li>
					<li menu-name="theme" theme="${statics['org.datagear.web.util.Themes'].DARK}"><a href="javascript:void(0);"><span class="ui-widget ui-widget-content theme-sample theme-sample-dark"></span><@spring.message code='main.changeTheme.dark' /></a></li>
					<li menu-name="theme" theme="${statics['org.datagear.web.util.Themes'].GREEN}"><a href="javascript:void(0);"><span class="ui-widget ui-widget-content theme-sample theme-sample-green"></span><@spring.message code='main.changeTheme.green' /></a></li>
				</ul>
			</li>
			<li class=""><a href="javascript:void(0);"><@spring.message code='main.changeLocale' /></a>
				<ul class="ui-widget-shadow">
					<li menu-name="locale" locale="zh"><a href="javascript:void(0);"><@spring.message code='main.changeLocale.zh' /></a></li>
					<li menu-name="locale" locale="en"><a href="javascript:void(0);"><@spring.message code='main.changeLocale.en' /></a></li>
				</ul>
			</li>
			<li><a href="javascript:void(0);"><@spring.message code='help' /><span class="new-version-tip"></span></a>
				<ul class="ui-widget-shadow">
					<li menu-name="about"><a href="${contextPath}/about"><@spring.message code='main.about' /></a></li>
					<li menu-name="documentation"><a href="${statics['org.datagear.util.Global'].WEB_SITE}/documentation/"><@spring.message code='main.documentation' /></a></li>
					<li menu-name="changelog"><a href="${contextPath}/changelog"><@spring.message code='main.changelog' /></a></li>
					<li menu-name="downloadLatestVersion">
						<a href="${statics['org.datagear.util.Global'].WEB_SITE}"><@spring.message code='main.downloadLatestVersion' /><span class="new-version-tip"></span></a>
					</li>
				</ul>
			</li>
		</ul>
	</li>
</ul>
<script type="text/javascript">
(function(po)
{
	var currentVersion = "${statics['org.datagear.util.Global'].VERSION?js_string}";
	
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
		
		return ($.compareVersion(detectedVersion, currentVersion) > 0);
	};
	
	po.initSysMenu = function()
	{
		po.elementOfId("systemSetMenu").menu(
		{
			position : {my:"right top", at: "right bottom-1"},
			select : function(event, ui)
			{
				event.preventDefault();
				
				var $item = $(ui.item);
				var menuName = $item.attr("menu-name");
				
				if(!menuName || $item.hasClass("ui-state-disabled"))
					return;
				
				var href = $("a", $item).attr("href");
				
				if(menuName == "driverEntity" || menuName == "user"
						|| menuName == "dataSetResDirectory" || menuName == "dashboardGlobalRes"
						|| menuName == "role" || menuName == "schemaGuard"
						|| menuName == "chartPlugin")
				{
					var options = {};
					$.setGridPageHeightOption(options);
					po.open(href, options);
				}
				else if(menuName == "schemaUrlBuilder" || menuName == "personalSet"
						|| menuName == "changelog")
				{
					po.open(href);
				}
				else if(menuName == "theme")
				{
					var theme = $item.attr("theme");
					
					$.getJSON(contextPath+"/changeThemeData?theme="+theme, function(data)
					{
						for(var i=0; i<data.length; i++)
						{
							var di = data[i];
							
							if(di.type == "css")
								$(di.selector).attr(di.attr, di.value);
							else if(di.type == "var")
								window[di.name] = di.value;
						}
					});
				}
				else if(menuName == "locale")
				{
					po.confirm("<@spring.message code='main.changeLocaleConfirm' />",
					{
						confirm: function()
						{
							var locale = $item.attr("locale");
							
							$.getJSON(contextPath+"/changeLocale?locale="+locale, function()
							{
								window.location.href = contextPath;
							});
						}
					});
				}
				else if(menuName == "about")
				{
					po.open(href, { width : "50%" });
				}
				else if(menuName == "documentation" || menuName == "downloadLatestVersion")
				{
					window.open(href);
				}
			}
		});
		
		if(po.newVersionDetected())
			$(".new-version-tip").css("display", "inline-block");
	};
})
(${pageId});
</script>

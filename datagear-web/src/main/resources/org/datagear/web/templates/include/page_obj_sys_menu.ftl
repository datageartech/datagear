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
page_js_obj.ftl

变量：
//当前用户，不允许为null
User currentUser
//检测新版本的script脚本
String detectNewVersionScript
-->
<ul id="systemSetMenu" class="lightweight-menu">
	<li class="system-set-root"><span><span class="ui-icon ui-icon-gear"></span><span class="new-version-tip"></span></span>
		<ul style="display:none;" class="ui-widget-shadow">
			<#if !currentUser.anonymous>
			<#if currentUser.admin>
			<li class="system-set-driverEntity"><a href="javascript:void(0);"><@spring.message code='main.manageDriverEntity' /></a></li>
			<li class="system-set-schemaUrlBuilder"><a href="javascript:void(0);"><@spring.message code='schemaUrlBuilder.schemaUrlBuilder' /></a></li>
			<li class="system-set-schemaGuard"><a href="javascript:void(0);"><@spring.message code='main.manageSchemaGuard' /></a></li>
			<li class="ui-widget-header"></li>
			<li class="system-set-dataSetResDirectory"><a href="javascript:void(0);"><@spring.message code='main.manageDataSetResDirectory' /></a></li>
			<li class="system-set-chartPlugin"><a href="javascript:void(0);"><@spring.message code='main.manageChartPlugin' /></a></li>
			<li class="system-set-dashboardGlobalRes"><a href="javascript:void(0);"><@spring.message code='main.manageDashboardGlobalRes' /></a></li>
			<li class="ui-widget-header"></li>
			<li class="system-set-user"><a href="javascript:void(0);"><@spring.message code='main.manageUser' /></a></li>
			<li class="system-set-role"><a href="javascript:void(0);"><@spring.message code='main.manageRole' /></a></li>
			</#if>
			<li class="system-set-personalSet"><a href="javascript:void(0);"><@spring.message code='main.personalSet' /></a></li>
			<li class="ui-widget-header"></li>
			</#if>
			<li class=""><a href="javascript:void(0);"><@spring.message code='main.changeTheme' /></a>
				<ul class="ui-widget-shadow">
					<li class="theme-item" theme="${statics['org.datagear.web.util.Themes'].LIGHT}"><a href="javascript:void(0);"><span class="ui-widget ui-widget-content theme-sample theme-sample-light"></span><@spring.message code='main.changeTheme.light' /></a></li>
					<li class="theme-item" theme="${statics['org.datagear.web.util.Themes'].DARK}"><a href="javascript:void(0);"><span class="ui-widget ui-widget-content theme-sample theme-sample-dark"></span><@spring.message code='main.changeTheme.dark' /></a></li>
					<li class="theme-item" theme="${statics['org.datagear.web.util.Themes'].GREEN}"><a href="javascript:void(0);"><span class="ui-widget ui-widget-content theme-sample theme-sample-green"></span><@spring.message code='main.changeTheme.green' /></a></li>
				</ul>
			</li>
			<li class=""><a href="javascript:void(0);"><@spring.message code='main.changeLocale' /></a>
				<ul class="ui-widget-shadow">
					<li class="locale-item" locale="zh"><a href="javascript:void(0);"><@spring.message code='main.changeLocale.zh' /></a></li>
					<li class="locale-item" locale="en"><a href="javascript:void(0);"><@spring.message code='main.changeLocale.en' /></a></li>
				</ul>
			</li>
			<li><a href="javascript:void(0);"><@spring.message code='help' /><span class="new-version-tip"></span></a>
				<ul class="ui-widget-shadow">
					<li class="about"><a href="javascript:void(0);"><@spring.message code='main.about' /></a></li>
					<li class="documentation"><a href="javascript:void(0);"><@spring.message code='main.documentation' /></a></li>
					<li class="changelog"><a href="javascript:void(0);"><@spring.message code='main.changelog' /></a></li>
					<li class="downloadLatestVersion">
						<a href="javascript:void(0);"><@spring.message code='main.downloadLatestVersion' /><span class="new-version-tip"></span></a>
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
		po.element("#systemSetMenu").menu(
		{
			position : {my:"right top", at: "right bottom-1"},
			select : function(event, ui)
			{
				var $item = $(ui.item);
				
				if($item.hasClass("ui-state-disabled"))
					return;
				
				if($item.hasClass("system-set-schemaUrlBuilder"))
				{
					po.open(contextPath+"/schemaUrlBuilder/editScriptCode");
				}
				else if($item.hasClass("system-set-driverEntity"))
				{
					var options = {};
					$.setGridPageHeightOption(options);
					po.open(contextPath+"/driverEntity/query", options);
				}
				else if($item.hasClass("system-set-user"))
				{
					var options = {};
					$.setGridPageHeightOption(options);
					po.open(contextPath+"/user/pagingQuery", options);
				}
				else if($item.hasClass("system-set-dataSetResDirectory"))
				{
					var options = {};
					$.setGridPageHeightOption(options);
					po.open(contextPath+"/dataSetResDirectory/pagingQuery", options);
				}
				else if($item.hasClass("system-set-dashboardGlobalRes"))
				{
					var options = {};
					$.setGridPageHeightOption(options);
					po.open(contextPath+"/dashboardGlobalRes/query", options);
				}
				else if($item.hasClass("system-set-role"))
				{
					var options = {};
					$.setGridPageHeightOption(options);
					po.open(contextPath+"/role/pagingQuery", options);
				}
				else if($item.hasClass("system-set-schemaGuard"))
				{
					var options = {};
					$.setGridPageHeightOption(options);
					po.open(contextPath+"/schemaGuard/query", options);
				}
				else if($item.hasClass("system-set-chartPlugin"))
				{
					var options = {};
					$.setGridPageHeightOption(options);
					po.open(contextPath+"/chartPlugin/query", options);
				}
				else if($item.hasClass("system-set-personalSet"))
				{
					po.open(contextPath+"/user/personalSet");
				}
				else if($item.hasClass("theme-item"))
				{
					var theme = $item.attr("theme");
					
					$.getJSON(contextPath+"/changeThemeData?theme="+theme, function(data)
					{
						for(var i=0; i<data.length; i++)
							$(data[i].selector).attr(data[i].attr, data[i].value);
					});
				}
				else if($item.hasClass("locale-item"))
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
				else if($item.hasClass("about"))
				{
					po.open(contextPath+"/about", { width : "50%" });
				}
				else if($item.hasClass("documentation"))
				{
					window.open("${statics['org.datagear.util.Global'].WEB_SITE}/documentation/");
				}
				else if($item.hasClass("changelog"))
				{
					po.open(contextPath+"/changelog");
				}
				else if($item.hasClass("downloadLatestVersion"))
				{
					window.open("${statics['org.datagear.util.Global'].WEB_SITE}");
				}
			}
		});
		
		if(po.newVersionDetected())
			$(".new-version-tip").css("display", "inline-block");
	};
})
(${pageId});
</script>

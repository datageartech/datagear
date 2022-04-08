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
<#assign selectOperation=true>
<#assign selectPageCss=(selectOperation?string('page-grid-select',''))>
<html>
<head>
<#include "../include/html_head.ftl">
<title><#include "../include/html_title_app_name.ftl"><@spring.message code='chartPlugin.selectChartPlugin' /></title>
</head>
<body class="fill-parent">
<#if !isAjaxRequest>
<div class="fill-parent">
</#if>
<#include "../include/page_obj.ftl">
<div id="${pageId}" class="page-grid ${selectPageCss} page-grid-chartPlugin-select">
	<div class="head">
		<div class="search">
			<#include "../include/page_obj_searchform.ftl">
		</div>
		<div class="operation">
			<input name="confirmButton" type="button" class="recommended" value="<@spring.message code='confirm' />" />
		</div>
	</div>
	<div class="content">
		<div class="chart-plugin-nav ui-widget ui-widget-content ui-corner-all">
			<div class="chart-plugin-nav-content"></div>
			<div class="chart-plugin-nav-foot">
				<span><@spring.message code='total' /><@spring.message code='colon' /></span>
				<span class="plugin-total"></span>
			</div>
		</div>
		<div class="chart-plugin-content"></div>
	</div>
	<div class="foot">
	</div>
</div>
<#if !isAjaxRequest>
</div>
</#if>
<#include "../include/page_obj_grid.ftl">
<script type="text/javascript">
(function(po)
{
	po.initGridBtns();
	
	po.categorizations = <@writeJson var=categorizations />;
	
	po.findChartPlugin = function(id)
	{
		for(var i=0; i<po.categorizations.length; i++)
		{
			var category = po.categorizations[i].category;
			var chartPlugins = po.categorizations[i].chartPlugins;
			
			for(var j=0; j<chartPlugins.length; j++)
			{
				if(chartPlugins[j].id == id)
					return chartPlugins[j];
				
			}
		}
		
		return null;
	};
	
	po.initChartPlugins = function(categorizations)
	{
		categorizations = (categorizations || []);
		
		po.categorizations = categorizations;
		
		var $nav = po.element(".chart-plugin-nav-content");
		var $content = po.element(".chart-plugin-content");
		
		$nav.empty();
		$content.empty();
		
		var $navul = $("<ul />").appendTo($nav);
		var $contentul = $("<ul />").appendTo($content);
		
		var pluginTotal = 0;
		
		for(var i=0; i<categorizations.length; i++)
		{
			var category = categorizations[i].category;
			var pluginCount = (categorizations[i].chartPlugins ? categorizations[i].chartPlugins.length : 0);
			pluginTotal += pluginCount;
			var categoryId = (category.name ? "${pageId}-category-"+category.name : "${pageId}-uncategorized");
			var label = (category.name ? (category.nameLabel && category.nameLabel.value ? category.nameLabel.value : category.name)
							: "<@spring.message code='chartPlugin.uncategorized' />");
			pluginCount = "<@spring.message code='bracketLeft' />" + pluginCount + "<@spring.message code='bracketRight' />";
			
			var $li = $("<li />").attr("categoryId", categoryId).appendTo($navul);
			$("<a />").html(label + pluginCount).appendTo($li);
		}
		
		po.element(".plugin-total").html(pluginTotal);
		
		for(var i=0; i<categorizations.length; i++)
		{
			var category = categorizations[i].category;
			var chartPlugins = categorizations[i].chartPlugins;
			
			var categoryId = (category.name ? "${pageId}-category-"+category.name : "${pageId}-uncategorized");
			var label = (category.name ? (category.nameLabel && category.nameLabel.value ? category.nameLabel.value : category.name)
							: "<@spring.message code='chartPlugin.uncategorized' />");
			var pluginCount = "<@spring.message code='bracketLeft' />" + chartPlugins.length + "<@spring.message code='bracketRight' />";
			
			var $li = $("<li />").attr("id", categoryId).appendTo($contentul);
			$("<div class='category-header ui-widget-header ui-corner-all' />").html(label + pluginCount).appendTo($li);
			
			var $liul = $("<ul />").appendTo($li);
			
			for(var j=0; j<chartPlugins.length; j++)
			{
				var chartPlugin = chartPlugins[j];
				
				var $liulli = $("<li />").appendTo($liul);
				
				var $item = $("<div class='plugin-item ui-widget ui-corner-all ui-state-default "+(chartPlugin.iconUrl ? "" : "no-icon")+"' />")
								.attr("chart-plugin-id", chartPlugin.id).attr("title", chartPlugin.nameLabel.value).appendTo($liulli);
				
				if(chartPlugin.iconUrl)
					$("<div class='plugin-icon'>&nbsp;</div>").css("background-image", "url(${contextPath}"+chartPlugin.iconUrl+")").appendTo($item);
				
				$("<div class='plugin-name'></div>").text(chartPlugin.nameLabel.value).appendTo($item);
			}
		}
		
		$navul.menu(
		{
			select: function(event, ui)
			{
				var item = $(ui.item);
				
				var $category = po.element("#" + item.attr("categoryId"));
				var top = $category.position().top;
				po.element(".chart-plugin-content").animate({scrollTop:top},'fast');
			}
		});
		
		$contentul.selectable(
		{
			filter: ".plugin-item",
			selected: function(event, ui)
			{
				var item = $(ui.selected);
				$(".plugin-item", this).removeClass("ui-state-active");
				item.addClass("ui-state-active");
			}
		});
	};
	
	po.element("input[name=confirmButton]").click(function()
	{
		var selectId = po.element(".plugin-item.ui-state-active").attr("chart-plugin-id");
		if(!selectId)
			return;
		
		var chartPlugin = po.findChartPlugin(selectId);
		
		if(!chartPlugin)
			return;
		
		po.pageParamCallSelect(true, chartPlugin);
	});
	
	po.search = function(searchParam)
	{
		$.postJson("${contextPath}/chartPlugin/selectData", searchParam, function(categorizations)
		{
			po.initChartPlugins(categorizations);
		});
	};
	
	po.initChartPlugins(po.categorizations);
})
(${pageId});
</script>
</body>
</html>

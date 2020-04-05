<#include "../../include/import_global.ftl">
<#include "../../include/html_doctype.ftl">
<html>
<head>
<#include "../../include/html_head.ftl">
<title><#include "../../include/html_title_app_name.ftl"><@spring.message code='chartPlugin.selectChartPlugin' /></title>
</head>
<body class="fill-parent">
<#if !isAjaxRequest>
<div class="fill-parent">
</#if>
<div id="${pageId}" class="page-grid page-grid-chartPlugin-select">
	<div class="head">
		<div class="search">
			<#include "../../include/page_obj_searchform.html.ftl">
		</div>
		<div class="operation">
			<input name="confirmButton" type="button" class="recommended" value="<@spring.message code='confirm' />" />
		</div>
	</div>
	<div class="content">
		<div class="chart-plugin-nav ui-widget ui-widget-content ui-corner-all"></div>
		<div class="chart-plugin-content"></div>
	</div>
	<div class="foot">
	</div>
</div>
<#if !isAjaxRequest>
</div>
</#if>
<#include "../../include/page_js_obj.ftl">
<#include "../../include/page_obj_searchform_js.ftl">
<script type="text/javascript">
(function(po)
{
	$.initButtons(po.element(".operation"));
	
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
		
		var $nav = po.element(".chart-plugin-nav");
		var $content = po.element(".chart-plugin-content");
		
		$nav.empty();
		$content.empty();
		
		var $navul = $("<ul />").appendTo($nav);
		var $contentul = $("<ul />").appendTo($content);
		
		for(var i=0; i<categorizations.length; i++)
		{
			var category = categorizations[i].category;
			var categoryId = (category.name ? "${pageId}-category-"+category.name : "${pageId}-uncategorized");
			var label = (category.name ? (category.nameLabel && category.nameLabel.value ? category.nameLabel.value : category.name)
							: "<@spring.message code='chartPlugin.uncategorized' />");
			
			var $li = $("<li />").attr("categoryId", categoryId).appendTo($navul);
			$("<a />").html(label).appendTo($li);
		}
		
		for(var i=0; i<categorizations.length; i++)
		{
			var category = categorizations[i].category;
			var chartPlugins = categorizations[i].chartPlugins;
			
			var categoryId = (category.name ? "${pageId}-category-"+category.name : "${pageId}-uncategorized");
			var label = (category.name ? (category.nameLabel && category.nameLabel.value ? category.nameLabel.value : category.name)
							: "<@spring.message code='chartPlugin.uncategorized' />");
			
			var $li = $("<li />").attr("id", categoryId).appendTo($contentul);
			$("<div class='category-header ui-widget-header ui-corner-all' />").html(label).appendTo($li);
			
			var $liul = $("<ul />").appendTo($li);
			
			for(var j=0; j<chartPlugins.length; j++)
			{
				var chartPlugin = chartPlugins[j];
				
				var $liulli = $("<li />").appendTo($liul);
				
				var $item = $("<div class='plugin-item ui-widget ui-corner-all ui-state-default "+(chartPlugin.iconUrl ? "" : "no-icon")+"' />")
								.attr("chart-plugin-id", chartPlugin.id).attr("title", chartPlugin.nameLabel.value).appendTo($liulli);
				
				if(chartPlugin.iconUrl)
					$("<div class='plugin-icon'>&nbsp;</div>").css("background-image", "url(${contextPath}/"+chartPlugin.iconUrl+")").appendTo($item);
				else
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
		$.postJson("${contextPath}/analysis/chartPlugin/selectData", searchParam, function(categorizations)
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

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
<#include "../include/page_import.ftl">
<#include "../include/html_doctype.ftl">
<html>
<head>
<#include "../include/html_head.ftl">
<title>
	<@spring.message code='module.chartPlugin' />
	<#include "../include/html_request_action_suffix.ftl">
	<#include "../include/html_app_name_suffix.ftl">
</title>
</head>
<body class="p-card no-border h-screen m-0">
<#include "../include/page_obj.ftl">
<div id="${pid}" class="page page-manager page-chartPlugin-select h-full flex flex-column overflow-auto">
	<div class="page-header grid grid-nogutter align-items-center p-1 flex-grow-0">
		<div class="col-12" :class="pm.isSelectAction ? 'md:col-6' : 'md:col-4'">
			<#include "include/search_form_filter.ftl">
		</div>
		<div class="operations col-12 flex gap-1 flex-wrap md:justify-content-end" :class="pm.isSelectAction ? 'md:col-6' : 'md:col-8'">
			<p-button label="<@spring.message code='confirm' />" @click="onSelect"></p-button>
			<p-button label="<@spring.message code='view' />" @click="onView" class="p-button-secondary"></p-button>
		</div>
	</div>
	<div class="page-content flex-grow-1 overflow-auto">
		<div class="grid grid-nogutter m-0 flex-nowrap h-full">
			<div class="col-3 p-2 p-card">
				<div class="flex flex-column h-full">
					<div class="flex-grow-1 p-0 overflow-auto">
						<p-tabmenu :model="pm.categoryMenuItems" v-model:active-index="pm.categoryMenuActiveIndex"
							@tab-change="onCategoryMenuItemChange" class="vertical-tabmenu">
						</p-tabmenu>
					</div>
					<div class="flex-grow-0 flex justify-content-center py-2 pr-2" v-if="pm.uncategorizedMenuItem != null">
						<p-button type="button" class="mr-1" :label="pm.uncategorizedMenuItem.label" severity="secondary" text
							@click="onScrollToCategoryDataview(pm.uncategorizedMenuItem.id)">
						</p-button>
					</div>
					<div class="flex-grow-0 text-right">
						<@spring.message code='totalWithColon' />
						<span class="px-2">{{pm.pluginTotal}}</span>
					</div>
				</div>
			</div>
			<div class="col-9 pl-3">
				<div class="chart-plugins-scroller h-full overflow-auto">
					<div class="chart-plugins-wrapper relative">
						<div v-for="(ctrz, index) in pm.categorizations" :key="ctrz.category.name" :id="toCategorizationEleId(ctrz)" class="mb-3">
							<p-dataview :value="ctrz.chartPlugins" layout="grid">
								<template #header>
									{{formatCategoryNameLabel(ctrz)}}
								</template>
								<template #grid="slotProps">
									<div class="col-12 md:col-4 p-2">
										<div class="p-card p-3 cursor-pointer hover:surface-50"
											@click="onSelectChartPlugin(slotProps.data)"
											:class="{'state-active surface-50 text-color': slotProps.data.id == pm.selectedChartPluginId }">
											<div v-html="formatChartPlugin(slotProps.data)"></div>
										</div>
									</div>
								</template>
							</p-dataview>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
</div>
<#include "../include/page_manager.ftl">
<script>
(function(po)
{
	po.isForLocal = ("${(local!false)?string('true', 'false')}" == "true");
	
	po.refresh = function()
	{
		po.submitSearchForm();
	};
	
	po.getSelectedEntities = function()
	{
		var pm = po.vuePageModel();
		return $.wrapAsArray(po.vueRaw(pm.selectedChartPlugin));
	};
	
	//重写搜索表单提交处理函数
	po.search = function(formData)
	{
		po.updatePageData([]);
		
		po.ajaxJson("/chartPlugin/selectData" + (po.isForLocal ? "?local=true" : ""),
		{
			data: formData,
			success: function(response)
			{
				po.updatePageData(response);
			}
		});
	};
	
	po.updatePageData = function(categorizations)
	{
		var pm = po.vuePageModel();
		
		pm.categorizations = categorizations;
		pm.categoryMenuItems = po.toCategoryMenuItems(categorizations);
		pm.uncategorizedMenuItem = po.deleteUncategorizedMenuItem(pm.categoryMenuItems);
		pm.categoryMenuActiveIndex = 0;
		pm.selectedChartPlugin = null;
		pm.selectedChartPluginId = null;
		
		var pluginTotal = 0;
		var pluginIdMap = {};
		$.each(categorizations, function(idx, ct)
		{
			$.each(ct.chartPlugins, function(iidx, cp)
			{
				if(!pluginIdMap[cp.id])
				{
					pluginIdMap[cp.id] = true;
					pluginTotal++;
				}
			});
		});
		
		pm.pluginTotal = pluginTotal;
		
		po.element(".chart-plugins-scroller").animate({scrollTop:0}, 'fast');
	};
	
	po.toCategoryMenuItems = function(categorizations)
	{
		var re = [];
		
		$.each(categorizations, function(idx, ct)
		{
			re.push(
			{
				label: po.formatCategoryNameLabel(ct),
				id: po.toCategorizationEleId(ct),
				categoryName: ct.category.name
			});
		});
		
		return re;
	};
	
	po.deleteUncategorizedMenuItem = function(categoryMenuItems)
	{
		var idx = -1;
		for(var i=0; i<categoryMenuItems.length; i++)
		{
			if($.isEmpty(categoryMenuItems[i].categoryName))
			{
				idx = i;
				break;
			}
		}
		
		var re = null;
		
		if(idx >= 0)
		{
			re = categoryMenuItems[idx];
			categoryMenuItems.splice(idx, 1);
		}
		
		return re;
	};
	
	po.formatCategoryNameLabel = function(categorization)
	{
		var ctc = categorization.category;
		var count = (categorization.chartPlugins ? categorization.chartPlugins.length : 0);
		return (ctc.nameLabel && ctc.nameLabel.value ? ctc.nameLabel.value : (ctc.name || "<@spring.message code='uncategorized' />")) + " ("+count+")";
	};
	
	po.toCategorizationEleId = function(categorization)
	{
		var ctc = categorization.category;
		return po.pid + (ctc.name || "uncategorized");
	};
	
	po.scrollToCategoryDataview = function(id)
	{
		var ctEle = po.elementOfId(id);
		var top = ctEle.position().top;
		po.element(".chart-plugins-scroller").animate({scrollTop:top}, 'fast');
	};
	
	po.vuePageModel(
	{
		categorizations: [],
		pluginTotal: 0,
		categoryMenuItems: [],
		uncategorizedMenuItem: null,
		categoryMenuActiveIndex: 0,
		selectedChartPlugin: null,
		selectedChartPluginId: null
	});
	
	po.vueMethod(
	{
		formatCategoryNameLabel: function(categorization)
		{
			return po.formatCategoryNameLabel(categorization);
		},
		
		toCategorizationEleId: function(categorization)
		{
			return po.toCategorizationEleId(categorization);
		},
		
		formatChartPlugin: function(chartPlugin)
		{
			return $.toChartPluginHtml(chartPlugin, po.contextPath,
						{
							vertical:true, smallName:true, showVersion:true, showApiVersion:true, showPlatformVersion:true, showAuthor:true,
							apiVersionDesc: "<@spring.message code='chartPlugin.apiVersion.desc' />",
							platformVersionDesc: "<@spring.message code='chartPlugin.platformVersion.desc' />"
						});
		},
		
		onCategoryMenuItemChange: function(e)
		{
			var items = po.vuePageModel().categoryMenuItems;
			var item = items[e.index];
			po.scrollToCategoryDataview(item.id);
		},
		
		onScrollToCategoryDataview: function(id)
		{
			po.scrollToCategoryDataview(id);
		},
		
		onSelectChartPlugin: function(chartPlugin)
		{
			var pm = po.vuePageModel();
			pm.selectedChartPlugin = chartPlugin;
			pm.selectedChartPluginId = (chartPlugin ? chartPlugin.id : null);
		},
		
		onSelect: function()
		{
			po.handleSelectAction();
		},
		
		onView: function()
		{
			po.handleOpenOfAction("/chartPlugin/view");
		}
	});

	po.vueMounted(function()
	{
		po.refresh();
	});

	po.setupAction();
})
(${pid});
</script>
<#include "../include/page_vue_mount.ftl">
</body>
</html>
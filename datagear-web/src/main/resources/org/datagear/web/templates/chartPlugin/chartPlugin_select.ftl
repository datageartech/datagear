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
	<@spring.message code='module.chartPlugin' />
	<#include "../include/html_request_action_suffix.ftl">
</title>
</head>
<body class="p-card no-border">
<#include "../include/page_obj.ftl">
<div id="${pid}" class="page page-manager page-chartPlugin-select">
	<div class="page-header grid align-items-center">
		<div class="col-12" :class="pm.isSelectAction ? 'md:col-5' : 'md:col-3'">
			<#include "../include/page_search_form.ftl">
		</div>
		<div class="h-opts col-12 text-right" :class="pm.isSelectAction ? 'md:col-7' : 'md:col-9'">
			<p-button label="<@spring.message code='confirm' />" @click="onSelect"></p-button>
		</div>
	</div>
	<div class="page-content">
		<div class="grid grid-nogutter m-0 flex-nowrap h-full">
			<div class="col-3 p-2 p-card">
				<div class="flex flex-column h-full">
					<div class="flex-grow-1 p-0 overflow-auto">
						<p-tabmenu :model="pm.categoryMenuItems" v-model:active-index="pm.categoryMenuActiveIndex"
							@tab-change="onCategoryMenuItemChange" class="vertical-tabmenu">
						</p-tabmenu>
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
							<p-dataview :value="pm.categorizations[index].chartPlugins" layout="grid">
								<template #header>
									{{formatCategoryNameLabel(ctrz)}}
								</template>
								<template #grid="slotProps">
									<div class="col-12 md:col-4 p-3">
										<div class="p-card p-3 cursor-pointer"
											@click="onSelectChartPlugin(slotProps.data)"
											:class="{'state-active': slotProps.data.id == pm.selectedChartPluginId }">
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
	po.refresh = function()
	{
		po.loadCategorizations();
	};
	
	po.getSelectedEntities = function()
	{
		var pm = po.vuePageModel();
		return $.wrapAsArray(po.vueRaw(pm.selectedChartPlugin));
	};
	
	po.search = function(formData)
	{
		po.loadCategorizations(formData);
	};
	
	po.loadCategorizations = function(data)
	{
		var pm = po.vuePageModel();
		data = (data ? data : { keyword: pm.searchForm.keyword });
		
		po.ajaxJson("/chartPlugin/selectData",
		{
			data: data,
			success: function(response)
			{
				pm.categorizations = response;
				pm.categoryMenuItems = po.toCategoryMenuItems(response);
				pm.categoryMenuActiveIndex = 0;
				pm.selectedChartPlugin = null;
				pm.selectedChartPluginId = null;
				
				var pluginTotal = 0;
				var pluginIdMap = {};
				$.each(response, function(idx, ct)
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
			}
		});
	};
	
	po.toCategoryMenuItems = function(categorizations)
	{
		var re = [];
		
		$.each(categorizations, function(idx, ct)
		{
			re.push(
			{
				label: po.formatCategoryNameLabel(ct),
				id: po.toCategorizationEleId(ct)
			});
		});
		
		return re;
	};
	
	po.formatCategoryNameLabel = function(categorization)
	{
		var ctc = categorization.category;
		var count = (categorization.chartPlugins ? categorization.chartPlugins.length : 0);
		return (ctc.nameLabel && ctc.nameLabel.value ? ctc.nameLabel.value : (ctc.name || "<@spring.message code='other' />")) + " ("+count+")";
	};
	
	po.toCategorizationEleId = function(categorization)
	{
		var ctc = categorization.category;
		return po.pid + (ctc.name || "other");
	};
	
	po.vuePageModel(
	{
		searchForm:{ keyword: "" },
		categorizations: [],
		pluginTotal: 0,
		categoryMenuItems: [],
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
			return $.toChartPluginHtml(chartPlugin, po.contextPath, {vertical:true});
		},
		
		onCategoryMenuItemChange: function(e)
		{
			var items = po.vuePageModel().categoryMenuItems;
			var item = items[e.index];
			
			var ctEle = po.elementOfId(item.id);
			var top = ctEle.position().top;
			po.element(".chart-plugins-scroller").animate({scrollTop:top}, 'fast');
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
		}
	});

	po.vueMounted(function()
	{
		po.loadCategorizations();
	});

	po.setupAction();
	po.vueMount();
})
(${pid});
</script>
</body>
</html>
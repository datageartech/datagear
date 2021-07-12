<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#--
数据分析项目相关表格页：扩展数据分析项目查询参数

依赖：
page_obj_searchform.ftl 或者 page_obj_searchform_data_filter.ftl
-->
<script type="text/javascript">
(function(po)
{
	po.getSearchParamSuper = po.getSearchParam;
	po.getSearchParam = function()
	{
		var param = po.getSearchParamSuper();
		
		param["${statics['org.datagear.web.vo.APIDDataFilterPagingQuery'].PROPERTY_APID}"] = po.currentAnalysisProjectId();
		
		return param;
	};
	
	po.currentAnalysisProjectId = function()
	{
		var id = null;
		
		//页面处于主页选项卡内
		if($.analysisProjectContext)
		{
			id = $.analysisProjectContext.valueId();
		}
		//页面处于新打开窗口内
		else
		{
			//新窗口打开时不应使用cookie中的当前值，因为不符合直观感受，页面中并无相关展示信息
			//id = $.cookie("${statics['org.datagear.web.controller.AbstractController'].KEY_ANALYSIS_PROJECT_ID}");
			id = null;
		}
		
		return id;
	};
	
	//页面处于主页选项卡内
	if($.analysisProjectContext)
	{
		po.currentAnalysisProject = $.analysisProjectContext.value();
		
		$.analysisProjectContext.addListener(function(analysisProject)
		{
			var refresh = true;
			
			var myTabsPanel = po.element().closest(".ui-tabs-panel");
			
			//所在选项卡隐藏时先不刷新，待显示时再刷新
			if(myTabsPanel.length > 0 && myTabsPanel.is(":hidden"))
				refresh = false;
			
			if(refresh)
			{
				po.searchForm().submit();
				po.currentAnalysisProject = analysisProject;
			}
		});
		
		//选项卡显示时刷新
		var myTabsPanel = po.element().closest(".ui-tabs-panel");
		$.bindPanelShowCallback(myTabsPanel, function()
		{
			if(!$.analysisProjectContext.isValue(po.currentAnalysisProject))
			{
				po.searchForm().submit();
				po.currentAnalysisProject = $.analysisProjectContext.value();
			}
		});
	}
})
(${pageId});
</script>
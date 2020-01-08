/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 看板渲染理器：window.dashboardRenderer。
 * 
 * 依赖:
 * jquery.js
 */
(function(window)
{
	var renderer = (window.dashboardRenderer || (window.dashboardRenderer = {}));
	
	renderer.render = function(dashboard)
	{
		var doRender = true;
		
		if(this.listener && this.listener.onRender)
		  doRender=this.listener.onRender(dashboard, this);
		
		if(doRender != false)
			this.renderDashboard(dashboard);
	};
	
	renderer.renderDashboard = function(dashboard)
	{
		for(var i=0; i<dashboard.charts.length; i++)
			dashboard.charts[i].render();
		
		var doUpdate = true;
		
		if(this.listener && this.listener.onUpdate)
			doUpdate=this.listener.onUpdate(dashboard, this);
		
		if(doUpdate != false)
			this.updateDashboard(dashboard);
	};
	
	renderer.updateDashboard = function(dashboard, chartIds)
	{
		var webContext = dashboard.renderContext.webContext;
		
		var data = this.buildUpdateDashboardAjaxData(dashboard, chartIds);
		
		var renderer = this;
		
		$.ajax({
			url : webContext.updateDashboardURL,
			data : data,
			success : function(dataSetsMap)
			{
				for(var chartId in dataSetsMap)
				{
					var chart = renderer.getChart(dashboard, chartId);
					
					if(!chart)
						continue;
					
					var dataSets = dataSetsMap[chartId];
					
					var doUpdate = true;
					
					if(renderer.listener && renderer.listener.onUpdateChart)
						doUpdate=renderer.listener.onUpdateChart(dashboard, chart, dataSets, renderer);
					
					if(doUpdate)
						chart.update(dataSets);
				}
			},
			error : function()
			{
				
			}
		});
	};
	
	/**
	 * 获取图表。
	 * 
	 * @param dashboard
	 * @param chartInfo 图表信息：图表元素ID、图表ID
	 */
	renderer.getChart = function(dashboard, chartInfo)
	{
		var index = this.getChartIndex(dashboard, chartInfo);
		
		return (index < 0 ? null : dashboard.charts[index]);
	};
	
	/**
	 * 获取图表索引号。
	 * 
	 * @param dashboard
	 * @param chartInfo 图表信息：图表对象、图表元素ID、图表ID
	 */
	renderer.getChartIndex = function(dashboard, chartInfo)
	{
		for(var i=0; i<dashboard.charts.length; i++)
		{
			if(dashboard.charts[i] === chartInfo
					|| dashboard.charts[i].elementId === chartInfo
					|| dashboard.charts[i].id === chartInfo)
				return i;
		}
		
		return -1;
	};
	
	renderer.buildUpdateDashboardAjaxData = function(dashboard, chartIds)
	{
		var webContext = dashboard.renderContext.webContext;
		
		var data = webContext.dashboardIdParam + "=" + encodeURIComponent(dashboard.id);
		
		if(chartIds && chartIds.length)
		{
			for(var i=0; i<chartIds.length; i++)
				data += "&" + webContext.chartsIdParam +"=" + encodeURIComponent(chartIds[i]);
		}
		
		return data;
	};
})
(window);
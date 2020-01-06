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
		
		var data = webContext.dashboardIdParam + "=" + dashboard.id;
		
		if(chartIds && chartIds.length)
		{
			for(var i=0; i<chartIds.length; i++)
				data += "&" + webContext.chartsIdParam +"=" + chartIds[i];
		}
		
		var renderer = this;
		
		$.ajax({
			url : webContext.updateDashboardURL,
			data : data,
			success : function(dataSetsMap)
			{
				for(var chartId in dataSetsMap)
				{
					var chart = renderer.getChartById(dashboard, chartId);
					
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
	
	renderer.getChartById = function(dashboard, chartId)
	{
		var index = this.getChartIndexById(dashboard, chartId);
		
		return (index < 0 ? null : dashboard.charts[index]);
	};
	
	renderer.getChartIndexById = function(dashboard, chartId)
	{
		for(var i=0; i<dashboard.charts.length; i++)
		{
			if(dashboard.charts[i].id == chartId)
				return i;
		}
		
		return -1;
	}
})
(window);
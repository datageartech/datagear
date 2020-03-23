/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 看板渲染理器：window.dashboardRenderer。
 * 
 * 依赖:
 * jquery.js
 * 
 * @deprecated 此模块已被datagear-dashboardFactory.js代替，未来版本会移除
 */
(function(window)
{
	var renderer = (window.dashboardRenderer || (window.dashboardRenderer = {}));
	
	/**
	 * 渲染看板。
	 * 
	 * @param dashboard 看板对象
	 */
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
		var charts = (dashboard.charts || []);
		
		for(var i=0; i<charts.length; i++)
		{
			var chart = charts[i];
			
			try
			{
				this.renderChart(dashboard, chart, i);
			}
			catch(e)
			{
				this.handleError(e);
			}
		}
		
		var doUpdate = true;
		
		if(this.listener && this.listener.onUpdate)
			doUpdate=this.listener.onUpdate(dashboard, this);
		
		if(doUpdate != false)
			this.updateDashboard(dashboard);
	};
	
	renderer.renderChart = function(dashboard, chart, chartIndex)
	{
		var doRender = true;
		
		if(this.listener && this.listener.onRenderChart)
			doRender=this.listener.onRenderChart(dashboard, chart, chartIndex, this);
		
		if(doRender != false)
			chart.plugin.chartRenderer.render(chart);
	};
	
	/**
	 * 更新整个看板的所有、或者指定图表集。
	 * 
	 * @param dashboard
	 * @param charts 可选，指定更新的图表数组，不设置表明更新所有
	 */
	renderer.updateDashboard = function(dashboard, charts)
	{
		if(this._UPDATING_DASHBOARD)
			return false;
		
		var webContext = dashboard.renderContext.webContext;
		
		var data = this.buildUpdateDashboardAjaxData(dashboard, charts);
		
		this._UPDATING_DASHBOARD = true;
		
		var renderer = this;
		
		$.ajax({
			url : webContext.updateDashboardURL,
			data : data,
			success : function(resultsMap)
			{
				renderer.updateCharts(dashboard, resultsMap);
			},
			error : function()
			{
				var updateTime = new Date().getTime();
				
				for(var i=0; i<dashboard.charts.length; i++)
					renderer.updateTime(dashboard.charts[i], updateTime);
			},
			complete : function()
			{
				renderer._UPDATING_DASHBOARD = false;
				
				var intervalId = setInterval(function()
				{
					var needUpdateCharts = renderer.getNeedUpdateCharts(dashboard);
					
					if(needUpdateCharts.length > 0)
					{
						renderer.updateUpdatingDashboardIntervalId();
						renderer.updateDashboard(dashboard, needUpdateCharts);
					}
				}, 1);
				
				renderer.updateUpdatingDashboardIntervalId(intervalId);
			}
		});
		
		return true;
	};
	
	renderer.updateUpdatingDashboardIntervalId = function(intervalId)
	{
		if(this._UPDATING_DASHBOARD_INTERVAL_ID)
			clearInterval(this._UPDATING_DASHBOARD_INTERVAL_ID);
		
		this._UPDATING_DASHBOARD_INTERVAL_ID = intervalId;
	};
	
	/**
	 * 获取当前需要更新的图表数组，没有则返回空数组。
	 * 
	 * @param dashboard
	 */
	renderer.getNeedUpdateCharts = function(dashboard)
	{
		var nexts = [];
		
		var charts = dashboard.charts;
		
		if(!charts || !charts.length)
			return nexts;
		
		var time = new Date().getTime();
		
		for(var i=0; i<charts.length; i++)
		{
			var chart = charts[i];
			
			//不需更新
			if(chart.updateInterval < 0)
				continue;
			
			var prevUpdateTime = this.updateTime(chart);
			
			if(prevUpdateTime == null || (prevUpdateTime + chart.updateInterval) <= time)
				nexts.push(chart);
		}
		
		return nexts;
	};
	
	renderer.updateCharts = function(dashboard, resultsMap)
	{
		var updateTime = new Date().getTime();
		
		for(var chartId in resultsMap)
		{
			var chart = this.getChart(dashboard, chartId);
			
			if(!chart)
				continue;
			
			this.updateTime(chart, updateTime);
			
			var results = resultsMap[chartId];
			
			try
			{
				this.updateChart(dashboard, chart, results);
			}
			catch(e)
			{
				this.handleError(e);
			}
		}
	};
	
	renderer.updateChart = function(dashboard, chart, results)
	{
		var doUpdate = true;
		
		if(this.listener && this.listener.onUpdateChart)
			doUpdate=this.listener.onUpdateChart(dashboard, chart, results, this);
		
		if(doUpdate != false)
			chart.plugin.chartRenderer.update(chart, results);
	};
	
	renderer.handleError = function(e)
	{
		if(typeof console != "undefined")
		{
			if(console.error)
				console.error(e);
			else if(console.warn)
				console.warn(e);
			else if(console.info)
				console.info(e);
		}
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
	
	/**
	 * 获取/设置图表更新时间。
	 * 
	 * @param chart
	 * @param updateTime 要设置的更新时间
	 */
	renderer.updateTime = function(chart, updateTime)
	{
		if(updateTime == undefined)
			return chart._UPDATE_TIME;
		
		chart._UPDATE_TIME = updateTime;
	};
	
	renderer.buildUpdateDashboardAjaxData = function(dashboard, charts)
	{
		var webContext = dashboard.renderContext.webContext;
		
		var data = webContext.dashboardIdParam + "=" + encodeURIComponent(dashboard.id);
		
		if(charts && charts.length)
		{
			for(var i=0; i<charts.length; i++)
				data += "&" + webContext.chartsIdParam +"=" + encodeURIComponent(charts[i].id);
		}
		
		return data;
	};
})
(window);
/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 看板工厂，用于初始化看板对象，为看板对象添加功能函数。
 * 全局变量名：window.dashboardFactory。
 * 
 * 加载时依赖：
 *   无
 * 
 * 运行时依赖:
 *   jquery.js
 *   datagear-chartFactory.js
 */
(function(global)
{
	var dashboardFactory = (global.dashboardFactory || (global.dashboardFactory = {}));
	var dashboardBase = (dashboardFactory.dashboardBase || (dashboardFactory.dashboardBase = {}));
	var chartStatus = (dashboardFactory.chartStatus || (dashboardFactory.chartStatus = {}));
	
	/**
	 * 初始化指定看板对象。
	 * 
	 * @param dashboard 看板对象
	 */
	dashboardFactory.init = function(dashboard)
	{
		this.initCharts(dashboard);
		$.extend(dashboard, this.dashboardBase);
		this.initListener(dashboard);
	};
	
	/**
	 * 初始化看板的所有图表。
	 * 
	 * @param dashboard 看板对象
	 */
	dashboardFactory.initCharts = function(dashboard)
	{
		if(!dashboard || !dashboard.charts)
			return;
		
		for(var i=0; i<dashboard.charts.length; i++)
			global.chartFactory.init(dashboard.charts[i]);
	};
	
	/**
	 * 初始化看板的监听器。
	 * 它将body元素的"dg-dashboard-listener"属性值设置为看板的监听器。
	 * 
	 * @param dashboard 看板对象
	 */
	dashboardFactory.initListener = function(dashboard)
	{
		if(!dashboard || dashboard.listener)
			return;
		
		var listener = $(document.body).attr("dg-dashboard-listener");
		
		if(listener)
			listener = global.chartFactory.evalSilently(listener);
		//@deprecated 用于兼容1.5.0版本的dashboardRenderer设计，未来版本会移除
		else if(typeof(dashboardRenderer) != "undefined")
			listener = dashboardRenderer.listener;
		
		if(listener)
			dashboard.listener = listener;
	};
	
	/**
	 * 渲染看板。
	 */
	dashboardBase.render = function()
	{
		var doRender = true;
		
		if(this.listener && this.listener.onRender)
		  doRender=this.listener.onRender(this);
		
		if(doRender != false)
			this.doHandleCharts();
	};
	
	/**
	 * 处理看板所有图表，根据其状态执行render或者update。
	 */
	dashboardBase.doHandleCharts = function()
	{
		var charts = (this.charts || []);
		
		for(var i=0; i<charts.length; i++)
		{
			var chart = charts[i];
			
			if(chart.statusPreRender())
				this.renderChart(chart, i);
		}
		
		var preUpdates = [];
		var time = new Date().getTime();
		
		for(var i=0; i<charts.length; i++)
		{
			var chart = charts[i];
			var updateInterval = chart.updateIntervalNonNull();
			
			if(chart.statusPreUpdate() || (chart.statusUpdated() && updateInterval > -1))
			{
				var prevUpdateTime = this.chartUpdateTime(chart);
				
				if(prevUpdateTime == null || (prevUpdateTime + updateInterval) <= time)
					preUpdates.push(chart);
			}
		}
		
		if(preUpdates.length == 0)
		{
			var dashboard = this;
			setTimeout(function(){ dashboard.doHandleCharts(); }, 1);
		}
		else
		{
			var webContext = this.renderContext.webContext;
			
			var data = this.buildUpdateDashboardAjaxData(preUpdates);
			
			var dashboard = this;
			
			$.ajax({
				url : webContext.updateDashboardURL,
				data : data,
				success : function(resultsMap)
				{
					dashboard.updateCharts(resultsMap);
				},
				error : function()
				{
					var updateTime = new Date().getTime();
					
					for(var i=0; i<dashboard.charts.length; i++)
						dashboard.chartUpdateTime(dashboard.charts[i], updateTime);
				},
				complete : function()
				{
					dashboard.doHandleCharts();
				}
			});
		}
	};
	
	/**
	 * 渲染指定图表。
	 * 
	 * @param chart 图表对象
	 * @param chartIndex 图表索引
	 */
	dashboardBase.renderChart = function(chart, chartIndex)
	{
		var doRender = true;
		
		if(this.listener && this.listener.onRenderChart)
			doRender=this.listener.onRenderChart(this, chart, chartIndex);
		
		if(doRender != false)
			this.doRenderChart(chart, chartIndex)
	};
	
	/**
	 * 执行渲染指定图表。
	 * 
	 * @param chart 图表对象
	 * @param chartIndex 图表索引
	 */
	dashboardBase.doRenderChart = function(chart, chartIndex)
	{
		return chart.render();
	};
	
	/**
	 * 更新看板的图表数据。
	 * 
	 * @param resultsMap 图表ID - 图表数据集结果数组
	 */
	dashboardBase.updateCharts = function(resultsMap)
	{
		var updateTime = new Date().getTime();
		
		for(var chartId in resultsMap)
		{
			var chart = this.getChart(chartId);
			
			if(!chart)
				continue;
			
			this.chartUpdateTime(chart, updateTime);
			
			var results = resultsMap[chartId];
			
			try
			{
				this.updateChart(chart, results);
			}
			catch(e)
			{
				global.chartFactory.logException(e);
			}
		}
	};
	
	/**
	 * 更新指定图表。
	 * 
	 * @param chart 图表对象
	 * @param results 图表数据集结果数组
	 */
	dashboardBase.updateChart = function(chart, results)
	{
		var doUpdate = true;
		
		if(this.listener && this.listener.onUpdateChart)
			doUpdate=this.listener.onUpdateChart(this, chart, results);
		
		if(doUpdate != false)
			this.doUpdateChart(chart, results);
	};
	
	/**
	 * 更新指定图表。
	 * 
	 * @param chart 图表对象
	 * @param results 图表数据集结果数组
	 */
	dashboardBase.doUpdateChart = function(chart, results)
	{
		return chart.update(results);
	};
	
	/**
	 * 获取图表，没有则返回undefined。
	 * 
	 * @param chartInfo 图表信息：图表元素ID、图表ID
	 */
	dashboardBase.getChart = function(chartInfo)
	{
		var index = this.getChartIndex(chartInfo);
		
		return (index < 0 ? undefined : this.charts[index]);
	};
	
	/**
	 * 获取图表索引号。
	 * 
	 * @param chartInfo 图表信息：图表对象、图表元素ID、图表ID
	 */
	dashboardBase.getChartIndex = function(chartInfo)
	{
		if(!this.charts)
			return -1;
		
		for(var i=0; i<this.charts.length; i++)
		{
			if(this.charts[i] === chartInfo
					|| this.charts[i].elementId === chartInfo
					|| this.charts[i].id === chartInfo)
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
	dashboardBase.chartUpdateTime = function(chart, updateTime)
	{
		if(updateTime == undefined)
			return chart._update_time;
		
		chart._update_time = updateTime;
	};
	
	/**
	 * 构建更新看板的ajax请求数据。
	 */
	dashboardBase.buildUpdateDashboardAjaxData = function(charts)
	{
		var webContext = this.renderContext.webContext;
		
		var data = webContext.dashboardIdParam + "=" + encodeURIComponent(this.id);
		
		if(charts && charts.length)
		{
			for(var i=0; i<charts.length; i++)
				data += "&" + webContext.chartsIdParam +"=" + encodeURIComponent(charts[i].id);
		}
		
		return data;
	};
})
(this);
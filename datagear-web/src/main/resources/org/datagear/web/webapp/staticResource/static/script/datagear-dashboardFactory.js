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
	
	var chartFactory = global.chartFactory;
	
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
			chartFactory.init(dashboard.charts[i]);
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
			listener = chartFactory.evalSilently(listener);
		
		if(listener)
			dashboard.listener = listener;
	};
	
	/**图表状态：等待render*/
	dashboardFactory.CHART_STATUS_WAIT_RENDER = "WAIT_RENDER";
	
	/**图表状态：完成render*/
	dashboardFactory.CHART_STATUS_FINISH_RENDER = "FINISH_RENDER";
	
	/**图表状态：等待update*/
	dashboardFactory.CHART_STATUS_WAIT_UPDATE = "WAIT_UPDATE";
	
	/**图表状态：完成update*/
	dashboardFactory.CHART_STATUS_FINISH_UPDATE = "FINISH_UPDATE";
	
	/**图表状态：终止*/
	dashboardFactory.CHART_STATUS_TERMINATE = "TERMINATE";
	
	/**
	 * 渲染看板。
	 */
	dashboardBase.render = function()
	{
		var doRender = true;
		
		if(this.listener && this.listener.onRender)
		  doRender=this.listener.onRender(this);
		
		if(doRender != false)
			this.doRender(dashboard);
	};
	
	/**
	 * 执行看板渲染。
	 */
	dashboardBase.doRender = function()
	{
		this.renderCharts();
		this.update();
	};
	
	/**
	 * 渲染看板的所有图表。
	 */
	dashboardBase.renderCharts = function()
	{
		var charts = (this.charts || []);
		
		for(var i=0; i<charts.length; i++)
		{
			var chart = charts[i];
			
			try
			{
				this.renderChart(chart, i);
			}
			catch(e)
			{
				chartFactory.logException(e);
			}
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
		chart.render();
	};
	
	/**
	 * 更新看板所有图表。
	 */
	dashboardBase.update = function()
	{
		var doUpdate = true;
		
		if(this.listener && this.listener.onUpdate)
			doUpdate=this.listener.onUpdate(this);
		
		if(doUpdate != false)
			this.doUpdate();
	};
	
	/**
	 * 执行更新看板的所有、或者指定图表。
	 * 
	 * @param charts 指定更新的图表数组，可选，默认为所有
	 */
	dashboardBase.doUpdate = function(charts)
	{
		if(this._UPDATING_DASHBOARD)
			return false;
		
		var webContext = this.renderContext.webContext;
		
		var data = this.buildUpdateDashboardAjaxData(charts);
		
		this._UPDATING_DASHBOARD = true;
		
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
					dashboard.updateTime(dashboard.charts[i], updateTime);
			},
			complete : function()
			{
				dashboard._UPDATING_DASHBOARD = false;
				
				var intervalId = setInterval(function()
				{
					var needUpdateCharts = dashboard.getNeedUpdateCharts();
					
					if(needUpdateCharts.length > 0)
					{
						dashboard.updateUpdatingDashboardIntervalId();
						dashboard.doUpdate(needUpdateCharts);
					}
				}, 1);
				
				dashboard.updateUpdatingDashboardIntervalId(intervalId);
			}
		});
		
		return true;
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
			
			this.updateTime(chart, updateTime);
			
			var results = resultsMap[chartId];
			
			try
			{
				this.updateChart(chart, results);
			}
			catch(e)
			{
				chartFactory.logException(e);
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
		chart.update(results);
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
	 * 图表状态是否为/设置为：等待render。
	 * 
	 * @param chart 图表对象
	 * @param set 为undefined时执行读取操作，否则执行设置操作
	 */
	dashboardBase.chartStatusWaitRender = function(chart, set)
	{
		if(set == undefined)
			return (chart._CHART_STATUS == dashboardFactory.CHART_STATUS_WAIT_RENDER);
		else
			chart._CHART_STATUS = dashboardFactory.CHART_STATUS_WAIT_RENDER;
	};
	
	/**
	 * 图表状态是否为/设置为：完成render。
	 * 
	 * @param chart 图表对象
	 * @param set 为undefined时执行读取操作，否则执行设置操作
	 */
	dashboardBase.chartStatusFinishRender = function(chart, set)
	{
		if(set == undefined)
			return (chart._CHART_STATUS == dashboardFactory.CHART_STATUS_FINISH_RENDER);
		else
			chart._CHART_STATUS = dashboardFactory.CHART_STATUS_FINISH_RENDER;
	};
	
	/**
	 * 图表状态是否为/设置为：等待update。
	 * 
	 * @param chart 图表对象
	 * @param set 为undefined时执行读取操作，否则执行设置操作
	 */
	dashboardBase.chartStatusWaitUpdate = function(chart, set)
	{
		if(set == undefined)
			return (chart._CHART_STATUS == dashboardFactory.CHART_STATUS_WAIT_UPDATE);
		else
			chart._CHART_STATUS = dashboardFactory.CHART_STATUS_WAIT_UPDATE;
	};

	/**
	 * 图表状态是否为/设置为：完成update。
	 * 
	 * @param chart 图表对象
	 * @param set 为undefined时执行读取操作，否则执行设置操作
	 */
	dashboardBase.chartStatusFinishUpdate = function(chart, set)
	{
		if(set == undefined)
			return (chart._CHART_STATUS == dashboardFactory.CHART_STATUS_FINISH_UPDATE);
		else
			chart._CHART_STATUS = dashboardFactory.CHART_STATUS_FINISH_UPDATE;
	};

	/**
	 * 图表状态是否为/设置为：终止。
	 * 
	 * @param chart 图表对象
	 * @param set 为undefined时执行读取操作，否则执行设置操作
	 */
	dashboardBase.chartStatusTerminate = function(chart, set)
	{
		if(set == undefined)
			return (chart._CHART_STATUS == dashboardFactory.CHART_STATUS_TERMINATE);
		else
			chart._CHART_STATUS = dashboardFactory.CHART_STATUS_TERMINATE;
	};
	
	/**
	 * 获取/设置图表状态。
	 * 
	 * @param chart 图表对象
	 * @param status 要设置的状态，可选，不设置则执行获取操作
	 */
	dashboardBase.chartStatus = function(chart, status)
	{
		if(status == undefined)
			return (chart._CHART_STATUS || dashboardFactory.CHART_STATUS_WAIT_RENDER);
		else
			chart._CHART_STATUS = (status || dashboardFactory.CHART_STATUS_WAIT_RENDER);
	};
	
	/**
	 * 获取当前需要更新的图表数组，没有则返回空数组。
	 */
	dashboardBase.getNeedUpdateCharts = function()
	{
		var nexts = [];
		
		var charts = this.charts;
		
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
	
	/**
	 * 更新定时器ID。
	 */
	dashboardBase.updateUpdatingDashboardIntervalId = function(intervalId)
	{
		if(this._UPDATING_DASHBOARD_INTERVAL_ID)
			clearInterval(this._UPDATING_DASHBOARD_INTERVAL_ID);
		
		this._UPDATING_DASHBOARD_INTERVAL_ID = intervalId;
	};
	
	/**
	 * 获取/设置图表更新时间。
	 * 
	 * @param chart
	 * @param updateTime 要设置的更新时间
	 */
	dashboardBase.updateTime = function(chart, updateTime)
	{
		if(updateTime == undefined)
			return chart._UPDATE_TIME;
		
		chart._UPDATE_TIME = updateTime;
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
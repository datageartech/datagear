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
	
	/**
	 * 初始化指定看板对象。
	 * 
	 * @param dashboard 看板对象
	 */
	dashboardFactory.init = function(dashboard)
	{
		this.initEchartsMapURLs();
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
	 * 初始化chartFactory.echartsMapURLs。
	 * 它将body元素的"dg-chart-map-urls"属性值设置为自定义地图JSON地址映射表。
	 */
	dashboardFactory.initEchartsMapURLs = function()
	{
		for(var i=0; i<this.builtInEchartsMaps.length; i++)
		{
			var urlNames = this.builtInEchartsMaps[i];
			for(var j=0; j<urlNames.names.length; j++)
				global.chartFactory.echartsMapURLs[urlNames.names[j]] = this.builtInEchartsMapBaseURL + urlNames.url;
		}
		
		var mapUrls = $(document.body).attr("dg-chart-map-urls");
		
		if(mapUrls)
			mapUrls = global.chartFactory.evalSilently(mapUrls);
		
		$.extend(global.chartFactory.echartsMapURLs, mapUrls);
	};
	
	//----------------------------------------
	// dashboardBase start
	//----------------------------------------
	
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
				contentType : "application/json",
				type : "POST",
				url : webContext.updateDashboardURL,
				data : JSON.stringify(data),
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
		
		var data = {};
		data[webContext.dashboardIdParam] = this.id;
		
		if(charts && charts.length)
		{
			var chartIds = [];
			var chartsParamValues = {};
			
			for(var i=0; i<charts.length; i++)
			{
				chartIds[i] = charts[i].id;
				var chartDataSets = (charts[i].chartDataSets || []);
				var myParamValuess = [];
				for(var j=0; j<chartDataSets.length; j++)
					myParamValuess.push({});
				
				chartsParamValues[charts[i].id] = myParamValuess;
			}
			
			data[webContext.chartIdsParam] = chartIds;
			data[webContext.chartsParamValuesParam] = chartsParamValues;
		}
		
		return data;
	};
	
	//----------------------------------------
	// dashboardBase end
	//----------------------------------------
	
	/**
	 * 内置地图JSON地址配置。
	 */
	dashboardFactory.builtInEchartsMapBaseURL = "/static/script/echarts-map";
	dashboardFactory.builtInEchartsMaps =
	[
		{names: ["china", "中国"], url: "/china.json"},
		{names: ["china-contour", "中国轮廓"], url: "/china-contour.json"},
		{names: ["china-cities", "中国城市"], url: "/china-cities.json"},
		{names: ["world", "世界"], url: "/world.json"},
		{names: ["anhui", "安徽"], url: "/province/anhui.json"},
		{names: ["aomen", "澳门"], url: "/province/aomen.json"},
		{names: ["beijing", "北京"], url: "/province/beijing.json"},
		{names: ["chongqing", "重庆"], url: "/province/chongqing.json"},
		{names: ["fujian", "福建"], url: "/province/fujian.json"},
		{names: ["gansu", "甘肃"], url: "/province/gansu.json"},
		{names: ["guangdong", "广东"], url: "/province/guangdong.json"},
		{names: ["guangxi", "广西"], url: "/province/guangxi.json"},
		{names: ["guizhou", "贵州"], url: "/province/guizhou.json"},
		{names: ["hainan", "海南"], url: "/province/hainan.json"},
		{names: ["hebei", "河北"], url: "/province/hebei.json"},
		{names: ["heilongjiang", "黑龙江"], url: "/province/heilongjiang.json"},
		{names: ["henan", "河南"], url: "/province/henan.json"},
		{names: ["hubei", "湖北"], url: "/province/hubei.json"},
		{names: ["hunan", "湖南"], url: "/province/hunan.json"},
		{names: ["jiangsu", "江苏"], url: "/province/jiangsu.json"},
		{names: ["jiangxi", "江西"], url: "/province/jiangxi.json"},
		{names: ["jilin", "吉林"], url: "/province/jilin.json"},
		{names: ["liaoning", "辽宁"], url: "/province/liaoning.json"},
		{names: ["neimenggu", "内蒙古"], url: "/province/neimenggu.json"},
		{names: ["ningxia", "宁夏"], url: "/province/ningxia.json"},
		{names: ["qinghai", "青海"], url: "/province/qinghai.json"},
		{names: ["shandong", "山东"], url: "/province/shandong.json"},
		{names: ["shanghai", "上海"], url: "/province/shanghai.json"},
		{names: ["shanxi", "山西"], url: "/province/shanxi.json"},
		{names: ["shanxi1", "陕西"], url: "/province/shanxi1.json"},
		{names: ["sichuan", "四川"], url: "/province/sichuan.json"},
		{names: ["taiwan", "台湾"], url: "/province/taiwan.json"},
		{names: ["tianjin", "天津"], url: "/province/tianjin.json"},
		{names: ["xianggang", "香港"], url: "/province/xianggang.json"},
		{names: ["xinjiang", "新疆"], url: "/province/xinjiang.json"},
		{names: ["xizang", "西藏"], url: "/province/xizang.json"},
		{names: ["yunnan", "云南"], url: "/province/yunnan.json"},
		{names: ["zhejiang", "浙江"], url: "/province/zhejiang.json"}
	];
})
(this);
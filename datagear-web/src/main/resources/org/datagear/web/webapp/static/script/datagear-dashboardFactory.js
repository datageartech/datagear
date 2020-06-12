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
 * 
 * 
 * 此看板工厂支持为<body>元素添加"dg-dashboard-listener"属性，用于指定看板监听器JS对象名，看板监听器格式为：
 * {
 *   onRender: function(dashboard){ ... },
 *   onRenderChart: function(dashboard, chart){ ... },
 *   onUpdateChart: function(dashboard, chart, results){ ... }
 * }
 * 
 * 此看板工厂支持为<body>元素添加"dg-chart-map-urls"属性，用于扩展或替换内置地图，格式为：
 * {customMap:'map/custom.json', china: 'map/myChina.json'}
 * 
 * 此看板工厂支持为<div>图表元素添加"dg-chart-disable-setting"属性，用于禁用图表交互设置功能，
 * 值为"true"表示禁用，其他表示启用。
 * 
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
		
		var dashboardTheme = global.chartFactory.renderContextAttr(dashboard.renderContext, "dashboardTheme");
		global.chartFactory.renderContextAttr(dashboard.renderContext,
				global.chartFactory.renderContextAttrs.chartTheme,
				dashboardTheme.chartTheme);
		
		for(var i=0; i<dashboard.charts.length; i++)
			this.initChart(dashboard.charts[i]);
	};
	
	/**
	 * 初始化指定图表。
	 * 
	 * @param chart 图表对象
	 */
	dashboardFactory.initChart = function(chart)
	{
		global.chartFactory.init(chart);
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

	/**
	 * 更新看板数据配置，需与后台保持一致。
	 */
	dashboardFactory.updateDashboardConfig = (dashboardFactory.updateDashboardConfig ||
			{
				//org.datagear.web.controller.AbstractDataAnalysisController.UPDATE_DASHBOARD_PARAM_DASHBOARD_ID
				dashboardIdParamName: "dashboardId",
				//org.datagear.web.controller.AbstractDataAnalysisController.UPDATE_DASHBOARD_PARAM_CHART_IDS
				chartIdsParamName: "chartIds",
				//org.datagear.web.controller.AbstractDataAnalysisController.UPDATE_DASHBOARD_PARAM_CHARTS_PARAM_VALUES
				chartsParamValuesParamName: "chartsParamValues"
			});
	
	/**
	 * 异步加载图表配置，需与后台保持一致。
	 */
	dashboardFactory.loadChartConfig = (dashboardFactory.loadChartConfig ||
			{
				//org.datagear.web.controller.DashboardController.LOAD_CHART_PARAM_DASHBOARD_ID
				dashboardIdParamName: "dashboardId",
				//org.datagear.web.controller.DashboardController.LOAD_CHART_PARAM_CHART_WIDGET_ID
				chartWidgetIdParamName: "chartWidgetId",
				//org.datagear.web.controller.DashboardController.LOAD_CHART_PARAM_CHART_ELEMENT_ID
				chartElementIdParamName: "chartElementId"
			});
	
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
				this.renderChart(chart);
		}
		
		var preUpdates = [];
		var time = new Date().getTime();
		
		for(var i=0; i<charts.length; i++)
		{
			var chart = charts[i];
			
			if(!chart.isDataSetParamValueReady())
				continue;
			
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
			var webContext = this.renderContextAttr("webContext");
			
			var data = this.buildUpdateDashboardAjaxData(preUpdates);
			
			var dashboard = this;
			
			$.ajax({
				contentType : "application/json",
				type : "POST",
				url : webContext.updateDashboardURL,
				data : JSON.stringify(data),
				success : function(resultsMap)
				{
					try
					{
						dashboard.updateCharts(resultsMap);
					}
					catch(e)
					{
						global.chartFactory.logException(e);
					}
					
					dashboard.doHandleCharts();
				},
				error : function()
				{
					var updateTime = new Date().getTime();
					
					try
					{
						for(var i=0; i<dashboard.charts.length; i++)
							dashboard.chartUpdateTime(dashboard.charts[i], updateTime);
					}
					catch(e)
					{
						global.chartFactory.logException(e);
					}
					
					dashboard.doHandleCharts();
				}
			});
		}
	};
	
	/**
	 * 渲染指定图表。
	 * 
	 * @param chart 图表对象
	 */
	dashboardBase.renderChart = function(chart)
	{
		try
		{
			var doRender = true;
			
			if(this.listener && this.listener.onRenderChart)
				doRender=this.listener.onRenderChart(this, chart);
			
			if(doRender != false)
			{
				this.doRenderChart(chart);
				this.renderChartSetting(chart);
			}
		}
		catch(e)
		{
			global.chartFactory.logException(e);
		}
	};
	
	/**
	 * 执行渲染指定图表。
	 * 
	 * @param chart 图表对象
	 */
	dashboardBase.doRenderChart = function(chart)
	{
		return chart.render();
	};
	
	/**
	 * 渲染图表设置表单。
	 */
	dashboardBase.renderChartSetting = function(chart)
	{
		var $chart = chart.elementJquery();
		
		//禁用设置表单，比如当不想让用户交互设置图表参数时
		if($chart.attr("dg-chart-disable-setting") == "true")
			return false;
		
		if(global.chartForm && global.chartForm.bindChartSettingPanelEvent)
		{
			global.chartForm.bindChartSettingPanelEvent(chart);
			return true;
		}
		
		return false;
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
			
			this.updateChart(chart, results);
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
		try
		{
			var doUpdate = true;
			
			if(this.listener && this.listener.onUpdateChart)
				doUpdate=this.listener.onUpdateChart(this, chart, results);
			
			if(doUpdate != false)
				this.doUpdateChart(chart, results);
		}
		catch(e)
		{
			global.chartFactory.logException(e);
		}
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
	 * @param chartInfo 图表信息：图表HTML元素ID、图表ID、图表索引
	 */
	dashboardBase.getChart = function(chartInfo)
	{
		var index = this.getChartIndex(chartInfo);
		
		return (index < 0 ? undefined : this.charts[index]);
	};
	
	/**
	 * 添加图表。
	 * 如果chart.statusPreRender()返回true，那么它将被立即渲染。
	 * 
	 * @param chart 图表对象
	 */
	dashboardBase.addChart = function(chart)
	{
		var charts = (this.charts || []);
		this.charts = charts.concat(chart);
	};
	
	/**
	 * 删除图表。
	 * 
	 * @param chartInfo 图表对象、图表HTML元素ID、图表ID、图表索引
	 * @param notDestory 选填参数，是否不销毁图表，默认为false
	 * @return 移除的图表对象或者undefined
	 */
	dashboardBase.removeChart = function(chartInfo, notDestory)
	{
		var newCharts = (this.charts ? [].concat(this.charts) : []);
		var index = this.getChartIndex(chartInfo, newCharts);
		
		if(index < 0)
			return undefined;
		
		var removeds = newCharts.splice(index, 1);
		this.charts = newCharts;
		
		if(notDestory != true)
			this.destroyChart(removeds[0]);
		
		return removeds[0];
	};
	
	/**
	 * 重新调整指定图表尺寸。
	 * 
	 * @param chartInfo 图表对象、图表HTML元素ID、图表ID、图表索引
	 */
	dashboardBase.resizeChart = function(chartInfo)
	{
		var chart = this.getChart(chartInfo);
		
		if(!chart)
			return false;
		
		chart.resize();
	};
	
	/**
	 * 重新调整所有图表尺寸。
	 */
	dashboardBase.resizeAllCharts = function()
	{
		var charts = (this.charts || []);
		
		for(var i=0; i<charts.length; i++)
			charts[i].resize();
	};
	
	/**
	 * 销毁图表。
	 * 
	 * @param chart 图表对象
	 */
	dashboardBase.destroyChart = function(chart)
	{
		try
		{
			this.destroyChartSetting(chart);
		}
		catch(e)
		{
			global.chartFactory.logException(e);
		}
		
		chart.destroy();
	};
	
	/**
	 * 销毁图表设置表单。
	 */
	dashboardBase.destroyChartSetting = function(chart)
	{
		if(global.chartForm && global.chartForm.unbindChartSettingPanelEvent)
		{
			global.chartForm.unbindChartSettingPanelEvent(chart);
			return true;
		}
		
		return false;
	};
	
	/**
	 * 获取图表索引号。
	 * 
	 * @param chartInfo 图表信息：图表对象、图表HTML元素ID、图表ID、图表索引
	 * @param charts 选填，查找的图表数组，如果不设置，则取this.charts
	 */
	dashboardBase.getChartIndex = function(chartInfo, charts)
	{
		if(charts == undefined)
			charts = this.charts;
		
		if(!charts)
			return -1;
		
		for(var i=0; i<charts.length; i++)
		{
			if(charts[i] === chartInfo
					|| charts[i].elementId === chartInfo
					|| charts[i].id === chartInfo
					|| i === chartInfo)
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
		var updateDashboardConfig = dashboardFactory.updateDashboardConfig;
		
		var data = {};
		data[updateDashboardConfig.dashboardIdParamName] = this.id;
		
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
					myParamValuess.push(chartDataSets[j].paramValues || {});
				
				chartsParamValues[charts[i].id] = myParamValuess;
			}
			
			data[updateDashboardConfig.chartIdsParamName] = chartIds;
			data[updateDashboardConfig.chartsParamValuesParamName] = chartsParamValues;
		}
		
		return data;
	};
	
	/**
	 * 异步加载图表。
	 * 如果指定的HTML元素已经是图表组件，则不加载图表而直接返回false。
	 * 
	 * @param element 用于渲染图表的HTML元素、Jquery对象
	 * @param chartWidgetId 要加载的图表部件ID
	 * @param ajaxOptions 选填参数，参数格式可以是ajax配置项：{...}、也可以是图表加载成功回调函数：function(chart){ ... }。
	 * 					  如果ajax配置项的success函数、图表加载成功回调函数返回false，则后续不会自动调用dashboardBase.addChart函数。
	 */
	dashboardBase.loadChart = function(element, chartWidgetId, ajaxOptions)
	{
		if(global.chartFactory.isChartElement(element))
			return false;
		
		var chartElementId = $(element).attr("id");
		if(!chartElementId)
		{
			chartElementId = "chart" + new Date().getTime();
			$(element).attr("id", chartElementId);
		}
		
		var webContext = this.renderContextAttr("webContext");
		var loadChartConfig = dashboardFactory.loadChartConfig;
		var _this = this;
		
		if(!ajaxOptions)
			ajaxOptions = {};
		else if(typeof(ajaxOptions) == "function")
		{
			var successHandler = ajaxOptions;
			ajaxOptions =
			{
				success: successHandler
			};
		}
		
		var data = {};
		data[loadChartConfig.dashboardIdParamName] = _this.id;
		data[loadChartConfig.chartWidgetIdParamName] = chartWidgetId;
		data[loadChartConfig.chartElementIdParamName] = chartElementId;
		
		var myAjaxOptions = $.extend(
		{
			url: webContext.loadChartURL,
			data: data,
			error: function(jqXHR, textStatus, errorThrown)
			{
				var msg = (jqXHR.responseJSON ? jqXHR.responseJSON.message : undefined);
				if(!msg)
					msg = textStatus;
				
				$(element).html(msg);
			}
		},
		ajaxOptions);
		
		if(!myAjaxOptions.success)
		{
			myAjaxOptions.success = function(chart, textStatus, jqXHR)
			{
				_this.initLoadedChart(chart);
				_this.addChart(chart);
			};
		}
		else
		{
			var successHandler = myAjaxOptions.success;
			
			myAjaxOptions.success = function(chart, textStatus, jqXHR)
			{
				_this.initLoadedChart(chart);
				var re = successHandler.call(this, chart, textStatus, jqXHR);
				
				if(re != false)
					_this.addChart(chart);
			};
		}
		
		$.ajax(myAjaxOptions);
	};
	
	/**
	 * 初始化异步加载的图表。
	 * 
	 * @param chart 图表JSON对象
	 */
	dashboardBase.initLoadedChart = function(chart)
	{
		chart.plugin = global.chartPluginManager.get(chart.plugin.id);
		chart.renderContext = this.renderContext;
		dashboardFactory.initChart(chart);
	};

	/**
	 * 获取/设置渲染上下文的属性值。
	 * 
	 * @param attrName
	 * @param attrValue 要设置的属性值，可选，不设置则执行获取操作
	 */
	dashboardBase.renderContextAttr = function(attrName, attrValue)
	{
		return global.chartFactory.renderContextAttr(this.renderContext, attrName, attrValue);
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
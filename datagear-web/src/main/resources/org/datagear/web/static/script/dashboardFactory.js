/*
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
 */

/**
 * 看板工厂，用于初始化看板对象，为看板对象添加功能函数。
 * 全局变量名：window.dashboardFactory
 * 
 * 加载时依赖：
 *   无
 * 
 * 运行时依赖:
 *   jquery.js
 *   chartFactory.js
 *   chartSetting.js
 * 
 * 
 * 此看板工厂支持为<body>元素添加elementAttrConst.DASHBOARD_LISTENER属性，用于指定看板监听器JS对象名，
 * 看板监听器格式参考dashboardBase.listener()函数说明。
 * 
 * 此看板工厂支持为<body>元素添加elementAttrConst.MAP_URLS属性，用于扩展或替换内置地图，格式为：
 * {customMap:'map/custom.json', china: 'map/myChina.json'}
 * 
 * 此看板工厂支持为图表元素添加elementAttrConst.LINK属性，用于设置图表联动，具体格式参考chartBase.links函数说明。
 * 
 * 此看板工厂支持为<body>元素、图表元素添加elementAttrConst.AUTO_RESIZE属性，用于设置图表是否自动调整大小。
 * 
 * 此看板工厂支持为<body>元素、图表元素添加elementAttrConst.UPDATE_GROUP属性，用于设置图表更新ajax分组。
 * 
 * 此看板工厂扩展了图表监听器功能，支持为图表监听器添加如下处理函数：
 * {
 *   //可选，加载数据前置回调函数
 *   onFetch: function(chart, chartQuery){ ... },
 *   //可选，更新数据出错回调函数
 *   updateError: function(chart, error){ ... }
 * }
 * 
 * 此看板工厂支持将页面内添加了elementAttrConst.DASHBOARD_FORM属性的<form>元素构建为看板表单，具体参考dashboardBase._renderForms函数说明。
 * 
 */
(function(global, window)
{
	/**图表工厂*/
	var chartFactory = (global.chartFactory || (global.chartFactory = {}));
	
	/**图表对象基类*/
	var chartBase = (chartFactory.chartBase || (chartFactory.chartBase = {}));
	
	/**图表状态常量*/
	var chartStatusConst = (chartFactory.chartStatusConst || (chartFactory.chartStatusConst = {}));
	
	/**HTML元素属性常量*/
	var elementAttrConst = (chartFactory.elementAttrConst || (chartFactory.elementAttrConst = {}));
	
	/** 渲染上下文属性名常量 */
	var renderContextAttrConst = (chartFactory.renderContextAttrConst || (chartFactory.renderContextAttrConst = {}));
	
	/**看板工厂*/
	var dashboardFactory = (global.dashboardFactory || (global.dashboardFactory = {}));
	
	/**看板状态常量*/
	var dashboardStatusConst = (dashboardFactory.dashboardStatusConst || (dashboardFactory.dashboardStatusConst = {}));
	
	/**看板对象基类*/
	var dashboardBase = (dashboardFactory.dashboardBase || (dashboardFactory.dashboardBase = {}));
	
	/** 内置地图 */
	var builtinChartMaps = (dashboardFactory.builtinChartMaps || (dashboardFactory.builtinChartMaps = []));
	
	var builtinChartMapBaseURL = (dashboardFactory.builtinChartMapBaseURL || (dashboardFactory.builtinChartMapBaseURL = "/static/lib/geojson/"));
	
	/** 看板版本常量，参考：org.datagear.web.analysis.DashboardVersion */
	var dashboardVersion = (dashboardFactory.dashboardVersion || (dashboardFactory.dashboardVersion = { V_1_0: "1.0" }));
	
	//----------------------------------------
	// chartStatusConst开始
	//----------------------------------------
	
	/**图表状态：需要参数值*/
	chartStatusConst.PARAM_VALUE_REQUIRED = "PARAM_VALUE_REQUIRED";
	
	/**图表状态：渲染出错*/
	chartStatusConst.RENDER_ERROR = "RENDER_ERROR";
	
	/**图表状态：更新出错*/
	chartStatusConst.UPDATE_ERROR = "UPDATE_ERROR";
	
	/**图表状态：正在处理更新*/
	chartStatusConst.HANDLING_UPDATE = "HANDLING_UPDATE";
	
	//----------------------------------------
	// chartStatusConst结束
	//----------------------------------------
	
	//----------------------------------------
	// elementAttrConst开始
	//----------------------------------------
	
	/**看板监听器*/
	elementAttrConst.DASHBOARD_LISTENER = "dg-dashboard-listener";
	
	/**看板表单*/
	elementAttrConst.DASHBOARD_FORM = "dg-dashboard-form";
	
	/**图表地图URL映射表*/
	elementAttrConst.MAP_URLS = "dg-chart-map-urls";
	
	/**图表联动*/
	elementAttrConst.LINK = "dg-chart-link";
	
	/**图表自动调整尺寸*/
	elementAttrConst.AUTO_RESIZE = "dg-chart-auto-resize";
	
	/**图表更新分组*/
	elementAttrConst.UPDATE_GROUP = "dg-chart-update-group";
	
	/**图表手动渲染*/
	elementAttrConst.MANUAL_RENDER = "dg-chart-manual-render";
	
	//----------------------------------------
	// elementAttrConst结束
	//----------------------------------------
	
	//----------------------------------------
	// renderContextAttrConst开始
	//----------------------------------------
	
	//渲染上下文属性名：看板主题，同：
	//AbstractDataAnalysisController.DASHBOARD_BUILTIN_RENDER_CONTEXT_ATTR_DASHBOARD_THEME
	renderContextAttrConst.dashboardTheme = "DG_DASHBOARD_THEME";
	
	//渲染上下文属性名：图表主题，同：
	//AbstractDataAnalysisController.DASHBOARD_BUILTIN_RENDER_CONTEXT_ATTR_CHART_THEME
	renderContextAttrConst.chartTheme = "DG_CHART_THEME";
	
	//渲染上下文属性名：当前用户，同：
	//AbstractDataAnalysisController.DASHBOARD_BUILTIN_RENDER_CONTEXT_ATTR_USER
	renderContextAttrConst.user = "DG_USER";
	
	//----------------------------------------
	// renderContextAttrConst结束
	//----------------------------------------
	
	//----------------------------------------
	// dashboardStatusConst开始
	//----------------------------------------
	
	/**看板状态：准备init*/
	dashboardStatusConst.PRE_INIT = "PRE_INIT";
	
	/**看板状态：正在init*/
	dashboardStatusConst.INITING = "INITING";
	
	/**看板状态：完成init*/
	dashboardStatusConst.INITED = "INITED";
	
	/**看板状态：正在render*/
	dashboardStatusConst.RENDERING = "RENDERING";
	
	/**看板状态：完成render*/
	dashboardStatusConst.RENDERED = "RENDERED";
	
	/**看板状态：正在destroy*/
	dashboardStatusConst.DESTROYING = "DESTROYING";
	
	/**看板状态：完成destroy*/
	dashboardStatusConst.DESTROYED = "DESTROYED";
	
	//----------------------------------------
	// dashboardStatusConst结束
	//----------------------------------------
	
	/**
	 * 更新看板数据配置，需与后台保持一致，具体参考：
	 * org.datagear.web.controller.AbstractDataAnalysisController.DashboardQueryForm
	 */
	dashboardFactory.updateDashboardConfig = (dashboardFactory.updateDashboardConfig ||
			{
				dashboardIdParamName: "dashboardId",
				dashboardQueryParamName: "dashboardQuery",
			});
	
	/**
	 * 异步加载图表配置，需与后台保持一致。
	 */
	dashboardFactory.loadChartConfig = (dashboardFactory.loadChartConfig ||
			{
				//org.datagear.web.controller.DashboardVisualController.LOAD_CHART_PARAM_DASHBOARD_ID
				dashboardIdParamName: "dashboardId",
				//org.datagear.web.controller.DashboardVisualController.LOAD_CHART_PARAM_CHART_WIDGET_ID
				chartWidgetIdParamName: "chartWidgetId"
			});
	
	/**
	 * 心跳配置，需与后台保持一致。
	 */
	dashboardFactory.heartbeatConfig = (dashboardFactory.heartbeatConfig ||
			{
				//org.datagear.web.controller.DashboardVisualController.HEARTBEAT_PARAM_DASHBOARD_ID
				dashboardIdParamName: "dashboardId",
				//org.datagear.web.controller.AbstractDataAnalysisController.HEARTBEAT_INTERVAL_MS
				interval: 1000 * 60 * 5
			});
	
	/**
	 * 卸载配置，需与后台保持一致。
	 */
	dashboardFactory.unloadConfig = (dashboardFactory.unloadConfig ||
			{
				//org.datagear.web.controller.AbstractDataAnalysisController.DASHBOARD_UNLOAD_URL_NAME
				urlAttrName: "unloadURL",
				//org.datagear.web.controller.DashboardVisualController.UNLOAD_PARAM_DASHBOARD_ID
				dashboardIdParamName: "dashboardId"
			});
	
	/**
	 * 循环监视处理图表状态间隔毫秒数。
	 */
	dashboardFactory.HANDLE_CHART_INTERVAL_MS = 1;
	
	/**
	 * 浏览器初始化到此看板工厂JS的时间戳。
	 */
	dashboardFactory.LOAD_TIME = new Date().getTime();
	
	/**图表主题关联的看板表单实体ID*/
	dashboardFactory._THEME_REF_DASHBOARD_FORM_ID = "DG_REF_DASHBOARD_FORM_ID";
	
	/**
	 * 初始化看板JSON对象，为其添加看板API，为看版内的图表JSON对象添加图表API，并设置状态：dashboard.statusPreInit(true)。
	 * 
	 * @param dashboard 看板JSON对象，格式应为：
	 *				{
	 *				  //唯一ID
	 *				  id: "...",
	 *				  //渲染上下文
	 *				  renderContext: {...},
	 *				  //可选，图表JSON对象数组
	 *				  charts: [ 图表JSON对象, ... ]
	 *				}
	 *				
	 *				另参考：org.datagear.analysis.Dashboard
	 */
	dashboardFactory.init = function(dashboard)
	{
		this._initDashboardBaseProperties(dashboard);
		$.extend(dashboard, this.dashboardBase);
		this._initStartHeartBeatIfNot(dashboard);
		
		this._initRenderContext(dashboard);
		
		var charts = dashboard.charts;
		for(var i=0; i<charts.length; i++)
			this._initChart(dashboard, charts[i]);
		
		dashboard.statusPreInit(true);
	};
	
	dashboardFactory._initRenderContext = function(dashboard)
	{
		var dashboardTheme = dashboard.renderContextAttr(renderContextAttrConst.dashboardTheme);
		var chartTheme = (dashboardTheme && dashboardTheme.chartTheme ? dashboardTheme.chartTheme : {});
		dashboard.renderContextAttr(renderContextAttrConst.chartTheme, chartTheme);
	};
	
	dashboardFactory._initChart = function(dashboard, chart)
	{
		chart.dashboard = dashboard;
		chart.renderContext = dashboard.renderContext;
		chartFactory.init(chart);
		this._initChartOverwriteIfNone(chart);
	};
	
	dashboardFactory._initChartOverwriteIfNone = function(chart)
	{
		//确保只会执行一次
		if(chart._contextChartsSuperByDbd == null)
		{
			chart._contextChartsSuperByDbd = chart._contextCharts;
			chart._contextCharts = function()
			{
				return this.dashboard.charts;
			};
		}
		
		//确保只会执行一次
		if(chart._initForPostSuperByDbd == null)
		{
			chart._initForPostSuperByDbd = chart._initForPost;
			chart._initForPost = function()
			{
				this._initLinks();
				this._initAutoResize();
				this._initUpdateGroup();
				this._initForPostSuperByDbd();
				
				var chartListener = this.listener();
				
				// < @deprecated 兼容4.3.1版本的dg-dashboard-listener中监听图表相关功能，将在未来版本移除
				//图表监听器不继承看板监听器功能，所以只有图表没有定义监听器时，才使用代理看板监听器
				if(!chartListener)
				{
					this.listener(this.dashboard._getDelegateChartListener());
				}
				// > @deprecated 兼容4.3.1版本的dg-dashboard-listener中监听图表相关功能，将在未来版本移除
				
				if(chartListener)
				{
					//由元素图表监听器属性生成的内部代理图表监听器，应为其添加处理函数
					if(chartListener._proxyChartListenerFromEleAttr)
					{
						chartListener.onFetch = function(chart, chartQuery)
						{
							var dl = this._findListenerOfFunc("onFetch");
							
							if(dl)
								return dl.onFetch(chart, chartQuery);
						};
						
						chartListener.updateError = function(chart, error)
						{
							var dl = this._findListenerOfFunc("updateError");
							
							if(dl)
								return dl.updateError(chart, error);
						};
					}
				}
			};
		}
		
		//确保只会执行一次
		if(chart._postProcessRenderedSuperByDbd == null)
		{
			chart._postProcessRenderedSuperByDbd = chart._postProcessRendered;
			chart._postProcessRendered = function()
			{
				this.bindLinksEventHanders(this.links());
				this._postProcessRenderedSuperByDbd();
			};
		}
	};
	
	dashboardFactory._initDashboardBaseProperties = function(dashboard)
	{
		dashboard.charts = (dashboard.charts || []);
		
		//私有化属性，便于后续定义相关同名函数
		dashboard._widget = dashboard.widget;
		dashboard._template = dashboard.template;
		dashboard._varName = dashboard.varName;
		dashboard._loadableChartWidgets = dashboard.loadableChartWidgets;
		dashboard._version = dashboard.version;
		
		delete dashboard.widget;
		delete dashboard.template;
		delete dashboard.varName;
		delete dashboard.loadableChartWidgets;
		delete dashboard.version;
	};
	
	dashboardFactory._initStartHeartBeatIfNot = function(dashboard)
	{
		if(dashboardFactory._initStartHeartBeat)
			return;
		
		//开启心跳，避免会话超时
		var webContext = chartFactory.renderContextAttrWebContext(dashboard.renderContext);
		var heartbeatURL = dashboard.contextURL(webContext.attributes.heartbeatURL);
		dashboardFactory.startHeartBeat(heartbeatURL, dashboard.id);
		
		dashboardFactory._initStartHeartBeat = true;
	};
	
	/**
	 * 开始执行心跳请求。
	 * @param heartbeatURL 心跳URL，可选，初次调用时需设置
	 * @param dashboardId 看板ID
	 */
	dashboardFactory.startHeartBeat = function(heartbeatURL, dashboardId)
	{
		if(this._heartbeatStatus == "run")
			return false;
		
		this._heartbeatStatus = "run";
		
		this.heartbeatURL = (heartbeatURL == undefined ? this.heartbeatURL : heartbeatURL);
		this._heartBeatAjaxRequestTimeout(dashboardId);
		
		return true;
	};
	
	/**
	 * 停止执行心跳请求。
	 */
	dashboardFactory.stopHeartBeat = function()
	{
		this._heartbeatStatus = "stop";
		
		if(this._heartbeatTimeoutId != null)
			clearTimeout(this._heartbeatTimeoutId);
	};
	
	dashboardFactory._heartBeatAjaxRequestTimeout = function(dashboardId)
	{
		var interval = dashboardFactory.heartbeatConfig.interval;
		
		var _thisFactory = this;
		
		this._heartbeatTimeoutId = setTimeout(function()
		{
			if(_thisFactory._heartbeatStatus == "run")
			{
				var url = _thisFactory.heartbeatURL;
				
				if(url == null)
					throw new Error("[dashboardFactory.heartbeatURL] must be set");
				
				var data = {};
				data[dashboardFactory.heartbeatConfig.dashboardIdParamName] = dashboardId;
				
				$.ajax({
					type : "GET",
					cache: false,
					url : url,
					data: data,
					complete : function()
					{
						if(_thisFactory._heartbeatStatus == "run")
							_thisFactory._heartBeatAjaxRequestTimeout(dashboardId);
					}
				});
			}
		},
		interval);
	};
	
	//----------------------------------------
	// chartBase扩展开始
	//----------------------------------------
	
	/**
	 * 初始化图表联动设置。
	 * 此方法从图表元素的elementAttrConst.LINK属性获取联动设置。
	 */
	chartBase._initLinks = function()
	{
		var links = this.elementJquery().attr(elementAttrConst.LINK);
		links = (links ? chartFactory.evalSilently(links) : null);
		
		this.links(links);
	};
	
	/**
	 * 初始化图表自动调整大小设置。
	 * 此方法从body元素、图表元素的elementAttrConst.AUTO_RESIZE属性获取联动设置。
	 */
	chartBase._initAutoResize = function()
	{
		var autoResize = this.elementJquery().attr(elementAttrConst.AUTO_RESIZE);
		
		if(autoResize == null)
			autoResize = $(document.body).attr(elementAttrConst.AUTO_RESIZE);
		
		this.autoResize(autoResize == "true");
	};
	
	/**
	 * 初始化图表更新分组。
	 * 此方法从body元素、图表元素的elementAttrConst.UPDATE_GROUP属性获取更新分组设置。
	 */
	chartBase._initUpdateGroup = function()
	{
		var updateGroup = this.elementJquery().attr(elementAttrConst.UPDATE_GROUP);
		
		if(updateGroup == null)
			updateGroup = $(document.body).attr(elementAttrConst.UPDATE_GROUP);
		
		this.updateGroup(updateGroup);
	};
	
	/**
	 * 获取/设置初始图表联动设置对象数组。
	 * 联动设置对象格式为：
	 * {
	 *   //可选，联动触发事件类型、事件类型数组，默认为"click"
	 *   trigger: "..."、["...", ...],
	 *   
	 *   //必选，联动目标图表元素ID、ID数组
	 *   target: "..."、["...", ...],
	 *   
	 *   //可选，联动数据参数映射表
	 *   data:
	 *   {
	 *     //ChartEvent对象的"data"、"orginalData"对象的属性名 : 目标数据集参数的映射索引、映射索引数组
	 *     "..." : 图表数据集参数索引对象、[ 图表数据集参数索引对象, ... ],
	 *     ...
	 *   }
	 * }
	 * 
	 * 图表数据集参数索引对象格式参考dashboardBase._batchSetDataSetParamValues函数相关说明，
	 * 其中value函数的sourceValueContext参数为图表事件对象（chartEvent）对象。
	 * 
	 * 图表初始化时会使用图表元素的"dg-chart-link"属性值执行设置操作。
	 * 
	 * 图表渲染器实现相关：
	 * 图表渲染器应实现on函数，以支持此特性。
	 * 
	 * @param links 可选，要设置的图表联动设置对象、数组，没有则执行获取操作。
	 */
	chartBase.links = function(links)
	{
		if(links === undefined)
			return this._links;
		else
		{
			if(links && !$.isArray(links))
				links = [ links ];
			
			this._links = links;
		}
	};
	
	/**
	 * 获取/设置图表是否自动调整大小。
	 * 
	 * 图表初始化时会使用图表元素的"dg-chart-auto-resize"属性值执行设置操作。
	 * 
	 * 图表渲染器实现相关：
	 * 图表渲染器应实现resize函数，以支持此特性。
	 * 
	 * @param autoResize 可选，设置为是否自动调整大小，没有则执行获取操作。
	 */
	chartBase.autoResize = function(autoResize)
	{
		if(autoResize === undefined)
			return (this._autoResize == true);
		else
			this._autoResize = autoResize;
	};
	
	/**
	 * 获取/设置图表更新分组。
	 * 如果图表从服务端加载数据比较耗时，可以为其指定一个分组标识，让其使用单独的ajax请求加载数据。
	 * 注意：相同分组的图表将使用同一个ajax请求。
	 * 
	 * 图表初始化时会使用图表元素的"dg-chart-update-group"属性值执行设置操作。
	 * 
	 * @param group 可选，设置更新分组，没有则执行获取操作返回非null值。
	 */
	chartBase.updateGroup = function(group)
	{
		if(group === undefined)
			return (this._updateGroup == null ? "" : this._updateGroup);
		else
			this._updateGroup = group;
	};
	
	/**
	 * 为指定图表联动设置绑定事件处理函数。
	 * 
	 * 图表渲染器实现相关：
	 * 图表渲染器应实现on函数，以支持此特性。
	 * 
	 * @param links 图表联动设置对象、数组，格式参考chartBase.links函数说明
	 * @return 绑定的事件处理函数对象数组，格式为：[ { eventType: "...", eventHandler: function(chartEvent){ ... } }, ... ]
	 */
	chartBase.bindLinksEventHanders = function(links)
	{
		this._assertActive();
		
		if(!links)
			return [];
		
		if(!$.isArray(links))
			links = [ links ];
		
		var ehs = [];
		
		var triggers = this._resolveLinksTriggers(links);
		var _thisChart = this;
		
		for(var i=0; i<triggers.length; i++)
		{
			var eh =
			{
				eventType: triggers[i],
				eventHandler: function(chartEvent)
				{
					_thisChart.handleChartEventLink(chartEvent, links);
				}
			};
			
			this.on(eh.eventType, eh.eventHandler);
			
			ehs.push(eh);
		}
		
		return ehs;
	};
	
	/**
	 * 解析不重复的联动设置触发事件数组。
	 */
	chartBase._resolveLinksTriggers = function(links)
	{
		var triggers = [];
		
		for(var i=0; i<links.length; i++)
		{
			var myTriggers = (links[i].trigger || "click");
			if(!$.isArray(myTriggers))
				myTriggers = [ myTriggers ];
			
			for(var j=0; j<myTriggers.length; j++)
			{
				if($.inArray(myTriggers[j], triggers) < 0)
					triggers.push(myTriggers[j]);
			}
		}
		
		return triggers;
	};
	
	/**
	 * 处理指定图表事件的图表联动操作。
	 * 此方法根据图表联动设置对象，将图表事件数据传递至目标图表数据集参数值，然后请求刷新图表数据。
	 * 
	 * @param chartEvent 图表事件对象
	 * @param links 图表联动设置对象、数组，格式参考chartBase.links函数说明
	 */
	chartBase.handleChartEventLink = function(chartEvent, links)
	{
		this._assertActive();
		
		if(!links)
			return false;
		
		if(!$.isArray(links))
			links = [ links ];
		
		var dashboard = this.dashboard;
		var targetCharts = [];
		
		var batchSource =
		{
			data: this.eventData(chartEvent),
			originalData: this.eventOriginalData(chartEvent),
			getValue: function(name)
			{
				//需支持属性路径格式的name
				var val = dashboardFactory.getPropertyPathValue(this.data, name);
				if(val === undefined && this.originalData != null)
					val = dashboardFactory.getPropertyPathValue(this.originalData, name);
				
				return val;
			}
		};
		
		for(var i=0; i<links.length; i++)
		{
			var link = links[i];
			
			if(!this._isLinkTriggerableByEvent(link, chartEvent))
				continue;
			
			var myTargetCharts = dashboard._batchSetDataSetParamValues(batchSource, link, chartEvent);
			
			for(var j=0; j<myTargetCharts.length; j++)
			{
				if($.inArray(myTargetCharts[j], targetCharts) < 0)
					targetCharts.push(myTargetCharts[j]);
			}
		}
		
		for(var i=0; i<targetCharts.length; i++)
			targetCharts[i].refreshData();
	};
	
	chartBase._isLinkTriggerableByEvent = function(link, chartEvent)
	{
		var eventType = chartEvent.type;
		
		if(!eventType)
		{
			return false;
		}
		else if(!link.trigger)
		{
			//默认为点击事件
			return (eventType == "click");
		}
		else if($.isArray(link.trigger))
		{
			return ($.inArray(eventType, link.trigger) >= 0);
		}
		else
			return (link.trigger == eventType);
	};
	
	/**
	 * 从服务端获取并更新图表数据。
	 * 此函数是基于状态实现的，在一个请求内的多次重复调用只会刷新一次。
	 */
	chartBase.refreshData = function()
	{
		this._assertActive();
		
		var msg = {};
		if(!this.isDataSetParamValueReady(msg))
		{
			chartFactory.logException("chart '#"+this.elementId+"' dataSetBinds["+msg.dataSetBindIndex+"] "
										+"'s ["+msg.paramName+"] param value required");
			return;
		}
		else
		{
			//这里不能使用this.statusPreUpdate(true)的方式实现
			//当在A图表监听器的update函数中调用参数化B图表的refreshData()时，
			//可能会出现已设置的statusPreUpdate()状态被PARAM_VALUE_REQUIRED状态覆盖的情况，
			//而导致refreshData()失效
			
			this._requestRefreshData();
		}
	};
	
	chartBase._updateTime = function(time)
	{
		return chartFactory.extValueBuiltin(this, "updateTime", time);
	};
	
	chartBase._requestRefreshData = function()
	{
		var requestIdx = chartFactory.extValueBuiltin(this, "requestRefreshDataIdx");
		if(requestIdx == null || requestIdx < 0)
			requestIdx = 0;
		
		requestIdx = requestIdx + 1;
		
		chartFactory.extValueBuiltin(this, "requestRefreshDataIdx", requestIdx);
	};
	
	chartBase._isRequestRefreshData = function()
	{
		var requestIdx = chartFactory.extValueBuiltin(this, "requestRefreshDataIdx");
		return (requestIdx != null && requestIdx > 0);
	};
	
	chartBase._startRefreshData = function()
	{
		var requestIdx = chartFactory.extValueBuiltin(this, "requestRefreshDataIdx");
		this._handleRefreshDataIdx = requestIdx;
	};
	
	chartBase._finishRefreshDataIfMatch = function()
	{
		var requestIdxNow = chartFactory.extValueBuiltin(this, "requestRefreshDataIdx");
		
		if(this._handleRefreshDataIdx == requestIdxNow)
			chartFactory.extValueBuiltin(this, "requestRefreshDataIdx", 0);
	};
	
	/**
	 * 获取/设置图表是否手动渲染。
	 * 
	 * @param manualRender 可选，设置是否手动渲染，默认为：false
	 * @returns true 是；false 否
	 * 
	 * @since 4.4.0
	 */
	chartBase.manualRender = function(manualRender)
	{
		if(manualRender === undefined)
		{
			//注意：此属性不应以chartBase._initManualRender()的方式初始化
			//因为看板需要在chart.init()之前就读取它的值
			
			if(this._manualRender != null)
				return (this._manualRender == true);
			else
			{
				var eleValue = this.elementJquery().attr(elementAttrConst.MANUAL_RENDER);
				return (eleValue == true || eleValue == "true");
			}
		}
		else
			this._manualRender = manualRender;
	};
	
	//重写函数，用于兼容看板版本，此函数不应做非null校验和处理，避免干扰原始参数
	chartBase._toApiStdResult = function(chartResult)
	{
		return this.results(chartResult);
	};
	
	//----------------------------------------
	// chartBase扩展结束
	//----------------------------------------
	
	
	//----------------------------------------
	// dashboardBase start
	//----------------------------------------
	
	/**
	 * 初始化看板，使用<body>元素上的dg-*属性值初始化看板，使用图表元素上的dg-*属性值初始化看板内所有图表。
	 * 看板初始化后处于this.statusInited()状态。
	 * 此函数在看板生命周期内仅允许调用一次，在dashboard.destroy()后允许再次调用。 
	 * 
	 * 由于直到此函数调用时，才会读取元素上的dg-*属性，因而元素dg-*属性值引用的变量仅需在此函数调用前定义即可。
	 * 
	 * 注意：只有this.statusPreInit()或者this.statusDestroyed()为true时，此函数才允许执行。
	 * 
	 * 看板生命周期：
	 * dashboard.init() -->-- dashboard.render() -->-- dashboard.destroy() -->--|
	 *       |                       |                                          |
	 *       |                       |---------------------<--------------------| 
	 *       |------------------------------<-----------------------------------| 
	 */
	dashboardBase.init = function()
	{
		if(!this.id)
			throw new Error("[dashboard.id] required");
		if(!this.renderContext)
			throw new Error("[dashboard.renderContext] required");
		
		if(!this.statusPreInit() && !this.statusDestroyed())
			throw new Error("dashboard is illegal state for init()");
		
		this.statusIniting(true);
		
		this._initRenderContext();
		this._initListener();
		this._initMapURLs();
		this._initChartResizeHandler();
		this._initUnloadDashboardHandler();
		this._initCharts();
		
		this.statusInited(true);
	};
	
	/**
	 * 初始化渲染上下文。
	 */
	dashboardBase._initRenderContext = function()
	{
		var dashboardTheme = this.renderContextAttr(renderContextAttrConst.dashboardTheme);
		var webContext = this.renderContextAttr(renderContextAttrConst.webContext);
		var chartTheme = this.renderContextAttr(renderContextAttrConst.chartTheme);
		
		chartFactory.initRenderContext(this.renderContext, webContext, chartTheme);
		
		// < @deprecated 兼容2.9.0版本的渲染上下文属性：dashboardTheme、webContext、chartTheme，将在未来版本移除，已被新名称取代
		this.renderContextAttr("dashboardTheme", dashboardTheme);
		this.renderContextAttr("webContext", chartFactory.renderContextAttrWebContext(this.renderContext));
		this.renderContextAttr("chartTheme", chartFactory.renderContextAttrChartTheme(this.renderContext));
		// > @deprecated 兼容2.9.0版本的渲染上下文属性：dashboardTheme、webContext、chartTheme，将在未来版本移除，已被新名称取代
	};
	
	/**
	 * 初始化地图URL映射表。
	 * 它将body元素的elementAttrConst.MAP_URLS属性值设置为地图URL映射表。
	 */
	dashboardBase._initMapURLs = function()
	{
		var mapURLs = {};
		
		for(var i=0; i<builtinChartMaps.length; i++)
		{
			var namesMap = builtinChartMaps[i];
			for(var j=0; j<namesMap.names.length; j++)
				mapURLs[namesMap.names[j]] = builtinChartMapBaseURL + namesMap.map;
		}
		
		var mapURLsBody = $(document.body).attr(elementAttrConst.MAP_URLS);
		
		if(mapURLsBody)
			mapURLs = $.extend(mapURLs, chartFactory.evalSilently(mapURLsBody, {}));
		
		this.mapURLs(mapURLs);
	};
	
	/**
	 * 初始化看板的监听器。
	 * 它将body元素的elementAttrConst.DASHBOARD_LISTENER属性值设置为看板的监听器。
	 */
	dashboardBase._initListener = function()
	{
		var listener = $(document.body).attr(elementAttrConst.DASHBOARD_LISTENER);
		
		if(listener)
		{
			listener = chartFactory.evalSilently(listener);
		}
		// < @deprecated 用于兼容1.5.0版本的dashboardRenderer设计，未来版本会移除
		else if(typeof(dashboardRenderer) != "undefined")
		{
			listener = dashboardRenderer.listener;
		}
		// > @deprecated 用于兼容1.5.0版本的dashboardRenderer设计，未来版本会移除
		
		this.listener(listener);
	};
	
	/**
	 * 初始化自动调整图表大小处理器。
	 */
	dashboardBase._initChartResizeHandler = function()
	{
		var $window = $(window);
		
		//解绑之前的，确保此函数可重复调用
		if(this._windowResizeHandler)
			$window.off("resize", this._windowResizeHandler);
		
		var thisDashboard = this;
		this._windowResizeHandler = function()
		{
			setTimeout(function()
			{
				if(thisDashboard.statusRendered())
				{
					var charts = thisDashboard.charts;
					
					for(var i =0; i<charts.length; i++)
					{
						var chart = charts[i];
						
						if(chart.autoResize() && chart.isActive())
							chart.resize();
					}
				}
			},
			300);
		};
		
		$window.on("resize", this._windowResizeHandler);
	};
	
	dashboardBase._initUnloadDashboardHandler = function()
	{
		var $window = $(window);
		
		//解绑之前的，确保此函数可重复调用
		if(this._windowBeforeunloadHandler)
			$window.off("beforeunload", this._windowBeforeunloadHandler);
		
		var thisDashboard = this;
		this._windowBeforeunloadHandler = function()
		{
			var renderContext = thisDashboard.renderContext;
			var webContext = chartFactory.renderContextAttrWebContext(renderContext);
			var unloadURL = webContext.attributes[dashboardFactory.unloadConfig.urlAttrName];
			unloadURL = thisDashboard.contextURL(unloadURL);
			var data = {};
			data[dashboardFactory.unloadConfig.dashboardIdParamName] = thisDashboard.id;
			
			$.post(unloadURL, data);
		}
		
		$window.on("beforeunload", this._windowBeforeunloadHandler);
	};
	
	dashboardBase._initCharts = function()
	{
		if(!this.charts)
			return;
		
		for(var i=0; i<this.charts.length; i++)
		{
			var chart = this.charts[i];
			
			if(chart.manualRender())
				continue;
			
			//如果图表元素不存在（比如在<template></template>里），应忽略初始化
			var chartEle = chart.element();
			if(chartEle == null)
			{
				chartFactory.logWarn("chart '#"+chart.elementId+"' element not found, init() ignored");
				continue;
			}
			
			if(chart.statusPreInit() || chart.statusDestroyed())
			{
				this._initChart(chart);
			}
		}
	};
	
	dashboardBase._initChart = function(chart)
	{
		try
		{
			chart.init();
		}
		catch(e)
		{
			chartFactory.logException(e);
		}
	};
	
	/**
	 * 获取/设置初始看板监听器。
	 * 看板监听器格式为：
	 * {
	 *   //可选，渲染看板后置回调函数
	 *   render: function(dashboard){ ... },
	 *   //可选，销毁看板后置回调函数
	 *   destroy: function(dashboard){ ... },
	 *   //可选，渲染看板前置回调函数，返回false将阻止渲染看板
	 *   onRender: function(dashboard){ ... },
	 *   //可选，销毁看板前置回调函数，返回false将阻止销毁看板
	 *   onDestroy: function(dashboard){ ... },
	 *   //可选，从服务端加载数据前置回调函数
	 *   //暂不启用，很难覆盖完整周期，且暴露过多内部结构
	 *   onFetch: function(dashboard, fetchContext){ ... },
	 *   //可选，从服务端加载数据成功回调函数，将在相关图表逻辑执行前调用
	 *   //暂不启用，很难覆盖完整周期，且暴露过多内部结构
	 *   fetchSuccess: function(dashboard, result, fetchContext){ ... },
	 *   //可选，从服务端加载数据出错回调函数，将在相关图表逻辑执行前调用
	 *   //暂不启用，很难覆盖完整周期，且暴露过多内部结构
	 *   fetchError: function(dashboard, error, fetchContext){ ... },
	 *   //可选，从服务端加载数据完成回调函数，将在fetchSuccess/fetchError后、且相关图表逻辑都执行完后调用
	 *   //暂不启用，很难覆盖完整周期，且暴露过多内部结构
	 *   fetchComplete: function(dashboard, fetchContext){ ... }
	 * }
	 * 
	 * 看板初始化时会使用<body>元素的"dg-dashboard-listener"属性值执行设置操作。
	 * 
	 * @param listener 可选，要设置的监听器对象，没有则执行获取操作
	 */
	dashboardBase.listener = function(listener)
	{
		if(listener === undefined)
			return this._listener;
		
		this._listener = listener;
		
		// < @deprecated 兼容4.3.1版本的dg-dashboard-listener中监听图表相关功能，将在未来版本移除
		//需要同步设置全局图表监听器
		var chartListener = this._getDelegateChartListener();
		
		if(listener && listener.renderChart)
			chartListener.render = function(chart){ listener.renderChart(chart.dashboard, chart); };
		else
			chartListener.render = undefined;
		
		if(listener && listener.updateChart)
			chartListener.update = function(chart, chartResult){ listener.updateChart(chart.dashboard, chart, chartResult); };
		else
			chartListener.update = undefined;
		
		if(listener && listener.onRenderChart)
			chartListener.onRender = function(chart){ return listener.onRenderChart(chart.dashboard, chart); };
		else
			chartListener.onRender = undefined;
		
		if(listener && listener.onUpdateChart)
			chartListener.onUpdate = function(chart, chartResult){ return listener.onUpdateChart(chart.dashboard, chart, chartResult); };
		else
			chartListener.onUpdate = undefined;
		
		if(listener && listener.updateChartError)
			chartListener.updateError = function(chart, error){ return listener.updateChartError(chart.dashboard, chart, error); };
		else
			chartListener.updateError = undefined;
		// > @deprecated 兼容4.3.1版本的dg-dashboard-listener中监听图表相关功能，将在未来版本移除
	};
	
	/**
	 * 获取/设置地图URL映射表。
	 *
	 * @param mapURLs 可选，要设置的地图URL映射表，仅会覆盖同名的地图URL映射，格式为参考chartFactory.chartMapURLs说明
	 * @returns 要获取的地图URL映射表
	 */
	dashboardBase.mapURLs = function(mapURLs)
	{
		if(!chartFactory.chartMapURLs)
			chartFactory.chartMapURLs = {};
		
		if(mapURLs === undefined)
			return chartFactory.chartMapURLs;
		
		$.extend(chartFactory.chartMapURLs, mapURLs);
	};
	
	/**
	 * 获取指定标识的图表，没有则返回undefined。
	 * 
	 * @param chartInfo 图表标识信息：图表Jquery对象、图表HTML元素、图表HTML元素ID、图表对象、图表ID、图表索引数值
	 */
	dashboardBase.chartOf = function(chartInfo)
	{
		var index = this.chartIndex(chartInfo);
		
		return (index < 0 ? undefined : this.charts[index]);
	};
	
	/**
	 * 获取指定图表在看板图表数组中的索引号，返回-1表示未找到。
	 * 
	 * @param chartInfo 图表标识信息：图表Jquery对象、图表HTML元素、图表HTML元素ID、图表对象、图表ID、图表索引数值
	 */
	dashboardBase.chartIndex = function(chartInfo)
	{
		return this._chartIndex(this.charts, chartInfo);
	};
	
	/**
	 * 获取图表索引，返回-1表示未找到。
	 * 
	 * @param charts 待查找的图表数组
	 * @param chartInfo 图表标识信息：图表Jquery对象、图表HTML元素、图表HTML元素ID、图表对象、图表ID、图表索引数值
	 */
	dashboardBase._chartIndex = function(charts, chartInfo)
	{
		if(!charts)
			return -1;
		
		//jQuery对象，取第一个元素
		if(chartInfo instanceof jQuery)
		{
			chartInfo = (chartInfo.length > 0 ? chartInfo[0] : null);
		}
		
		for(var i=0; i<charts.length; i++)
		{
			if(charts[i] === chartInfo
					|| charts[i].elementId === chartInfo
					|| charts[i].id === chartInfo
					|| charts[i].element() === chartInfo
					|| i === chartInfo)
				return i;
		}
		
		return -1;
	};
	
	/**
	 * 添加已经初始化的图表。
	 * 如果图表已添加至看板，或者图表HTML元素已被看板中的其他图表使用，将不会再次添加，直接返回false。
	 * 
	 * @param chart 图表对象
	 */
	dashboardBase.addChart = function(chart)
	{
		var exists = this.chartOf(chart);
		
		if(exists != null)
			return false;
		
		exists = this.chartOf(chart.elementId);
		
		if(exists != null)
			return false;
		
		//这里不应限制仅能添加未渲染的图表，因为应允许已完成渲染的图表先从看板移除，后续再加入看板
		
		this.charts = this.charts.concat(chart);
		
		return true;
	};
	
	/**
	 * 删除图表。
	 * 
	 * @param chartInfo 图表标识信息：图表Jquery对象、图表HTML元素、图表HTML元素ID、图表对象、图表ID、图表索引数值
	 * @param doDestroy 选填参数，是否销毁图表，默认为true
	 * @return 移除的图表对象，或者图表未找到时为undefined
	 */
	dashboardBase.removeChart = function(chartInfo, doDestroy)
	{
		var newCharts = [].concat(this.charts);
		var index = this._chartIndex(newCharts, chartInfo);
		
		if(index < 0)
			return undefined;
		
		var removeds = newCharts.splice(index, 1);
		this.charts = newCharts;
		
		if(doDestroy != false)
			this._destroyChart(removeds[0]);
		
		return removeds[0];
	};
	
	/**
	 * 获取当前在指定HTML元素上渲染的图表对象，返回null表示元素上并未渲染图表。
	 * 
	 * @param element HTML元素、HTML元素ID、Jquery对象
	 */
	dashboardBase.renderedChart = function(element)
	{
		return chartFactory.renderedChart(element);
	};
	
	/**
	 * 获取/设置渲染上下文的属性值。
	 * 
	 * @param attrName
	 * @param attrValue 要设置的属性值，可选，不设置则执行获取操作
	 */
	dashboardBase.renderContextAttr = function(attrName, attrValue)
	{
		return chartFactory.renderContextAttr(this.renderContext, attrName, attrValue);
	};
	
	/**
	 * 获取/设置看板级的结果数据格式。
	 * 如果某个图表的resultDataFormat()返回null，将会使用这个看板级的结果数据格式。
	 * 设置了新的结果数据格式后，下一次图表刷新数据将采用这个新格式。
	 * 
	 * @param resultDataFormat 可选，要设置的结果数据格式，结构参考：org.datagear.analysis.ResultDataFormat
	 * @returns 要获取的结果数据格式，没有则返回null
	 */
	dashboardBase.resultDataFormat = function(resultDataFormat)
	{
		if(resultDataFormat === undefined)
			return this._resultDataFormat;
		else
			this._resultDataFormat = resultDataFormat;
	};
	
	/**
	 * 渲染看板。
	 * 渲染中的看板处于this.statusRendering()状态，渲染完成后处于this.statusRendered()状态。 
	 * 此函数在看板生命周期内仅允许调用一次，在dashboard.destroy()后允许再次调用。
	 * 
	 * 注意：
	 * 只有this.statusPreInit()或者this.statusInited()或者this.statusDestroyed()为true时，此函数才允许执行。
	 * 特别地，当处于this.statusPreInit()时，此函数内部会先调用this.init()函数。
	 */
	dashboardBase.render = function()
	{
		if(this.statusPreInit())
			this.init();
		
		if(!this.statusInited() && !this.statusDestroyed())
			throw new Error("dashboard is illegal state for render()");
		
		this.statusRendering(true);
		
		var doRender = true;
		
		var listener = this.listener();
		if(listener && listener.onRender)
			doRender = listener.onRender(this);
		
		if(doRender != false)
		{
			this.doRender();
		}
	};
	
	/**
	 * 执行看板渲染，渲染所有看板表单元素，渲染所有符合状态的图表元素；
	 * 渲染看版内所有处于chart.statusPreRender()或者chart.statusDestroyed()状态的图表；
	 */
	dashboardBase.doRender = function()
	{
		if(!this.statusRendering())
			throw new Error("dashboard is illegal state for doRender()");
		
		this._renderForms();
		this._prepareDoRenderCharts();
		this.startHandleCharts();
		
		this.statusRendered(true);
	};
	
	dashboardBase._prepareDoRenderCharts = function()
	{
		if(!this.charts)
			return;
		
		for(var i=0; i<this.charts.length; i++)
		{
			var chart = this.charts[i];
			
			if(chart.manualRender())
				continue;
			
			if(chart.statusInited() || chart.statusDestroyed())
			{
				chart.statusPreRender(true);
			}
		}
	};
	
	/**
	 * 渲染你看板表单。
	 */
	dashboardBase._renderForms = function()
	{
		var $forms = $("form[dg-dashboard-form]", document.body);
		
		var dashboard = this;
		$forms.each(function()
		{
			dashboard.renderForm(this);
		});
	};
	
	/**
	 * 校验看板是否是活着的。
	 */
	dashboardBase._assertAlive = function()
	{
		if(!this.isAlive())
			throw new Error("dashboard not alive");
	};
	
	/**
	 * 校验看板是否处于活跃状态。
	 */
	dashboardBase._assertActive = function()
	{
		if(!this.isActive())
			throw new Error("dashboard not active");
	};
	
	/**
	 * 渲染看板表单。
	 * 看板表单提交时会自动将表单输入值设置为目标图表的数据集参数值，并刷新图表。
	 * 
	 * 表单配置对象格式为：
	 * {
	 *   //必选，表单输入项对象、数组
	 *   items: 表单输输入项对象 或者 [ 表单输输入项对象, ... ],
	 *   //可选，表单提交操作时执行的联动图表设置
	 *   link: 图表联动设置对象,
	 *   //可选，表单提交按钮文本
	 *   submitText: "...",
	 *   //表单渲染完成回调函数
	 *   render: function(form){ ... }
	 * }
	 * 
	 * 表单输输入项对象格式为：
	 * {
	 *   //必选，输入项名称
	 *   name: "...",
	 *   //可选，默认值
	 *   value: ...,
	 *   //可选，输入项标签
	 *   label: "...",
	 *   //可选，输入项类型，参考chartSetting.DataSetParamDataType，默认值为：chartSetting.DataSetParamDataType.STRING
	 *   type: "...",
	 *   //可选，是否必须，默认为false
	 *   required: true || false,
	 *   //可选，输入框类型，参考chartSetting.DataSetParamInputType，默认值为：chartSetting.DataSetParamInputType.TEXT
	 *   inputType: "...",
	 *   //可选，输入框配置，参考chartSetting.renderDataSetParamValueForm函数说明
	 *   inputPayload: ...,
	 *   //可选，输入项的联动数据映射设置
	 *   link: 图表数据集参数索引对象、[ 图表数据集参数索引对象, ... ]
	 * }
	 * 或者，简写为其name属性值。
	 * 
	 * 图表联动设置对象格式为：
	 * {
	 *   //必选，联动目标图表元素ID、ID数组
	 *   target: "..."、["...", ...],
	 *   //可选，联动数据参数映射表
	 *   data:
	 *   {
	 *     表单输入项名称 : 图表数据集参数索引对象、[ 图表数据集参数索引对象, ... ],
	 *     ...
	 * }
	 * 或者，简写为图表联动设置对象的target属性值。
	 * 
	 * 图表数据集参数索引对象格式参考dashboardBase._batchSetDataSetParamValues函数相关说明，其中value函数的sourceValueContext参数为：表单数据对象、表单HTML元素。
	 * 
	 * @param form 要渲染的<form>表单元素、Jquery选择器、Jquery对象，表单结构允许灵活自定义，具体参考chartSetting.renderDataSetParamValueForm
	 * @param config 可选，表单配置对象，默认为表单元素的elementAttrConst.DASHBOARD_FORM属性值
	 */
	dashboardBase.renderForm = function(form, config)
	{
		this._assertAlive();
		
		form = chartFactory.toJqueryObj(form)
		form.addClass("dg-dashboard-form");
		
		if(!config)
			config = chartFactory.evalSilently(form.attr(elementAttrConst.DASHBOARD_FORM), {});
		
		var dashboard = this;
		var globalTheme = chartFactory.renderContextAttrChartTheme(this.renderContext);
		var bindBatchSetName = chartFactory.builtinPropName("batchSet");
		
		config = $.extend(
		{
			submit: function(formData)
			{
				var thisForm = this;
				var batchSet = $(thisForm).data(bindBatchSetName);
				
				if(batchSet)
				{
					var charts = dashboard._batchSetDataSetParamValues(formData, batchSet, [ formData, thisForm ]);
					
					for(var i=0; i<charts.length; i++)
					{
						try
						{
							charts[i].refreshData();
						}
						catch(e)
						{
							chartFactory.logException(e);
						}
					}
				}
			}
		},
		config);
		
		//构建用于批量设置数据集参数值的对象
		var batchSet = { target: [], data: {} };
		
		if(config.link)
		{
			var link = config.link;
			
			//转换简写格式
			if(chartFactory.isString(link) || $.isArray(link))
				link = { target: link };
			
			batchSet.target = link.target;
			//新构建data对象，因为可能会在下面被修改
			batchSet.data = (link.data ? $.extend({}, link.data) : {});
		}
		
		var items = [];
		var defaultValues = {};
		
		var sourceItems = (config.items || []);
		if(!$.isArray(sourceItems))
			sourceItems = [ sourceItems ];
		
		for(var i=0; i<sourceItems.length; i++)
		{
			var item = sourceItems[i];
			
			if(chartFactory.isString(item))
				item = { name: item };
			else
				//确保不影响初始对象
				item = $.extend({}, item);
			
			if(!item.type)
				item.type = chartFactory.chartSetting.DataSetParamDataType.STRING;
			
			items.push(item);
			
			if(item.value != null)
				defaultValues[item.name] = item.value;
			
			//合并输入项的link设置
			if(item.link != null)
				batchSet.data[item.name] = item.link;
		}
		
		form.data(bindBatchSetName, batchSet);
		
		config.paramValues = defaultValues;
		config.chartTheme = globalTheme;
		
		chartFactory.addThemeRefEntity(globalTheme, dashboardFactory._THEME_REF_DASHBOARD_FORM_ID);
		chartFactory.chartSetting.renderDataSetParamValueForm(form, items, config);
	};
	
	/**
	 * 销毁看板表单。
	 * 
	 * @param form 看板表单元素
	 */
	dashboardBase.destroyForm = function(form)
	{
		this._destroyForm(form);
	};
	
	/**
	 * 重新调整指定图表尺寸。
	 * 
	 * @param chartInfo 图表标识信息：图表Jquery对象、图表HTML元素、图表HTML元素ID、图表对象、图表ID、图表索引数值
	 */
	dashboardBase.resizeChart = function(chartInfo)
	{
		this._assertActive();
		
		var chart = this.chartOf(chartInfo);
		chart.resize();
	};
	
	/**
	 * 重新调整所有活跃图表尺寸。
	 */
	dashboardBase.resizeAllCharts = function()
	{
		this._assertActive();
		
		for(var i=0; i<this.charts.length; i++)
		{
			var chart = this.charts[i];
			
			if(chart.isActive())
			{
				chart.resize();
			}
		}
	};
	
	/**
	 * 从服务端获取并更新图表数据。
	 * 
	 * @param chartInfo 图表标识信息：图表Jquery对象、图表HTML元素、图表HTML元素ID、图表对象、图表ID、图表索引数值
	 */
	dashboardBase.refreshData = function(chartInfo)
	{
		this._assertActive();
		
		var chart = this.chartOf(chartInfo);
		chart.refreshData();
	};
	
	/**
	 * 是否正在监视处理看板图表。
	 */
	dashboardBase.isHandlingCharts = function()
	{
		return (this._doHandlingCharts == true);
	};
	
	/**
	 * 开始监视处理看板图表，循环查看它们的状态，执行相应操作：
	 * 如果图表需要渲染，则执行chart.render()；如果图表需要更新，则执行chart.update()。
	 */
	dashboardBase.startHandleCharts = function()
	{
		this._assertAlive();
		
		if(this._doHandlingCharts == true)
			return false;
		
		this._doHandlingCharts = true;
		this._doHandleCharts();
		
		return true;
	};
	
	/**
	 * 停止监视处理看板图表。
	 */
	dashboardBase.stopHandleCharts = function()
	{
		this._doHandlingCharts = false;
	};
	
	/**
	 * 开始循环处理看板所有图表，根据其状态执行render或者update。
	 */
	dashboardBase._doHandleCharts = function()
	{
		if(this._doHandlingCharts != true)
			return;
		
		var charts = this.charts;
		
		for(var i=0; i<charts.length; i++)
		{
			var chart = charts[i];
			
			if(this._isWaitForRender(chart))
				this._renderChart(chart);
		}
		
		var preUpdateGroups = {};
		var preUpdateLocals = [];
		var time = chartFactory.currentDateMs();
		
		for(var i=0; i<charts.length; i++)
		{
			var chart = charts[i];
			
			if(this._isWaitForUpdate(chart, time))
			{
				if(this._isLocalChart(chart))
				{
					preUpdateLocals.push(chart);
				}
				else
				{
					var group = chart.updateGroup();
					var preUpdates = preUpdateGroups[group];
					
					if(preUpdates == null)
					{
						preUpdates = [];
						preUpdateGroups[group] = preUpdates;
					}
					
					preUpdates.push(chart);
				}
			}
		}
		
		this._doHandleChartsLocal(preUpdateLocals);
		
		var dashboard = this;
		var webContext = chartFactory.renderContextAttrWebContext(this.renderContext);
		var url = this.contextURL(webContext.attributes.updateDashboardURL);
		var groupContext =
		{
			groups: preUpdateGroups
		};
		
		for(var group in preUpdateGroups)
		{
			chartFactory.executeSilently(function()
			{
				dashboard._doHandleChartsAjax(url, group, preUpdateGroups[group], groupContext);
			});
		}
		
		setTimeout(function()
		{
			dashboard._doHandleCharts();
		},
		dashboardFactory.HANDLE_CHART_INTERVAL_MS);
	};
	
	dashboardBase._isLocalChart = function(chart)
	{
		var dataSetBinds = chart.dataSetBinds();
		return (dataSetBinds.length == 0);
	};
	
	dashboardBase._doHandleChartsLocal = function(preUpdateCharts)
	{
		if(!preUpdateCharts || preUpdateCharts.length == 0)
			return;
		
		var updateTime = chartFactory.currentDateMs();
		this._startChartsRefreshData(preUpdateCharts);
		
		var dashboard = this;
		var dashboardQueryForm = this._buildUpdateDashboardAjaxData(preUpdateCharts);
		var dashboardQuery = this._dashboardQueryOfForm(dashboardQueryForm);
		// 加载上下文对象，使用此上下文对象可以简化回调函数参数，也易于扩展
		var fetchContext =
		{
			charts: preUpdateCharts,
			query: dashboardQuery
		};
		
		//这里不允许异常中断
		chartFactory.executeSilently(function()
		{
			dashboard._execListenerOnFetch(fetchContext);
		});
		
		try
		{
			for(var i=0; i<preUpdateCharts.length; i++)
			{
				var chart = preUpdateCharts[i];
				var chartResult = {};
				//设置空数据集结果数组，避免后续出现空指针异常
				chart.results(chartResult, []);
				
				this._updateChart(chart, chartResult, true);
			}
		}
		finally
		{
			this._setChartsUpdateTime(preUpdateCharts, updateTime);			
			this._finishChartsRefreshDataIfMatch(preUpdateCharts);
		}
	};
	
	/**
	 * 图表是否在等待渲染。
	 */
	dashboardBase._isWaitForRender = function(chart)
	{
		return chart.statusPreRender();
	};
	
	/**
	 * 给定图表是否在等待更新数据。
	 * 如果返回true，图表状态将被设置为chartStatusConst.HANDLING_UPDATE，后续除非图表更新完成，都将返回false。
	 */
	dashboardBase._isWaitForUpdate = function(chart, currentTime)
	{
		var wait = false;
		
		if(currentTime == null)
			currentTime = chartFactory.currentDateMs();
		
		var status = chart.status();
		
		if(status == chartStatusConst.HANDLING_UPDATE)
		{
			wait = false;
		}
		else if(chart.statusRendered() || chart.statusPreUpdate())
		{
			wait = true;
		}
		else if(chart._isRequestRefreshData())
		{
			wait = true;
		}
		else if(chart.updateInterval > -1
					&& (chart.statusUpdated() || status == chartStatusConst.UPDATE_ERROR))
		{
			var updateInterval = chart.updateInterval;
			var prevUpdateTime = chart._updateTime();
			
			if(prevUpdateTime == null || (currentTime - prevUpdateTime) >= updateInterval)
				wait = true;
		}
		
		if(wait && !chart.isDataSetParamValueReady())
		{
			//标记为需要参数输入，避免参数准备好时会立即自动更新，实际应该由API控制是否更新
			chart.status(chartStatusConst.PARAM_VALUE_REQUIRED);
			wait = false;
		}
		
		if(wait)
		{
			//应立即设置为HANDLING_UPDATE状态，避免并发逻辑出错
			chart.status(chartStatusConst.HANDLING_UPDATE);
		}
		
		return wait;
	};
	
	/**
	 * 渲染指定图表。
	 * 
	 * @param chart 图表对象
	 */
	dashboardBase._renderChart = function(chart)
	{
		try
		{
			chart.render();
		}
		catch(e)
		{
			//设置为渲染出错状态，避免渲染失败后会_doHandleCharts中会无限尝试渲染
			chart.status(chartStatusConst.RENDER_ERROR);
			chartFactory.logException(e);
		}
	};
	
	dashboardBase._doHandleChartsAjax = function(url, group, preUpdateCharts, groupContext)
	{
		if(!preUpdateCharts || preUpdateCharts.length == 0)
			return;
		
		this._startChartsRefreshData(preUpdateCharts);
		
		var dashboard = this;
		var dashboardQueryForm = this._buildUpdateDashboardAjaxData(preUpdateCharts);
		var dashboardQuery = this._dashboardQueryOfForm(dashboardQueryForm);
		// 加载上下文对象，使用此上下文对象可以简化回调函数参数，也易于扩展
		var fetchContext =
		{
			group: group,
			charts: preUpdateCharts,
			query: dashboardQuery,
			groupContext: groupContext,
			//此次请求的XMLHttpRequest，将在后续设置
			xhr: undefined,
			//此次请求是否成功，将在后续设置
			success: undefined
		};
		
		//这里不允许异常中断
		chartFactory.executeSilently(function()
		{
			dashboard._execListenerOnFetch(fetchContext);
		});
		
		$.ajax({
			contentType : "application/json",
			type : "POST",
			url : url,
			data : JSON.stringify(dashboardQueryForm),
			success : function(dashboardResult, textStatus, jqXHR)
			{
				dashboardResult = (dashboardResult ? dashboardResult : {});
				dashboardResult.chartResults = (dashboardResult.chartResults ? dashboardResult.chartResults : {});
				dashboardResult.chartErrors = (dashboardResult.chartErrors ? dashboardResult.chartErrors : {});
				
				var chartResults = dashboardResult.chartResults;
				
				// < @deprecated 用于兼容1.10.1版本的DataSetResult.datas结构，未来版本会移除
				for(var chartId in chartResults)
				{
					var chartResult = (chartResults[chartId] || {});
					var dataSetResults = (chartResult ? chartResult.dataSetResults : []);
					
					for(var i=0; i<dataSetResults.length; i++)
					{
						if(dataSetResults[i] && dataSetResults[i].data != null)
						{
							var resultDatas = dataSetResults[i].data;
							if(resultDatas != null && !$.isArray(resultDatas))
								resultDatas = [ resultDatas ];
							
							dataSetResults[i].datas = resultDatas;
						}
					}
				}
				//> @deprecated 用于兼容1.10.1版本的DataSetResult.datas结构，未来版本会移除
				
				var updateTime = chartFactory.currentDateMs();
				
				try
				{
					dashboard._handleChartsAjaxSuccess(fetchContext, dashboardResult, jqXHR);
				}
				finally
				{
					dashboard._setChartsUpdateTime(preUpdateCharts, updateTime);				
					dashboard._finishChartsRefreshDataIfMatch(preUpdateCharts);
				}
			},
			error : function(jqXHR, textStatus, errorThrown)
			{
				var updateTime = chartFactory.currentDateMs();
				
				try
				{
					dashboard._handleChartsAjaxError(fetchContext, jqXHR, textStatus, errorThrown)
				}
				finally
				{
					dashboard._setChartsUpdateTime(preUpdateCharts, updateTime);
					dashboard._finishChartsRefreshDataIfMatch(preUpdateCharts);
				}
			}
		});
	};
	
	//执行监听器的onFetch回调函数
	dashboardBase._execListenerOnFetch = function(fetchContext)
	{
		var charts = fetchContext.charts;
		var dashboardQuery = fetchContext.query;
		var chartQueries = dashboardQuery.chartQueries;
		
		/* 暂不启用，很难覆盖完整周期，且暴露过多内部结构
		var dashboard = this;
		var listener = this.listener();
		if(listener && listener.onFetch)
		{
			chartFactory.executeSilently(function()
			{
				listener.onFetch(dashboard, fetchContext);
			});
		}
		*/
		
		for(var i=0; i<charts.length; i++)
		{
			var chart = charts[i];
			var chartListener = chart.listener();
			
			if(chartListener && chartListener.onFetch)
			{
				var chartId = chart.id;
				var chartQuery = (chartQueries[chartId] || {});
				
				chartFactory.executeSilently(function()
				{
					chartListener.onFetch(chart, chartQuery);
				});
			}
		}
	};
	
	dashboardBase._handleChartsAjaxSuccess = function(fetchContext, dashboardResult, xhr)
	{
		fetchContext.xhr = xhr;
		fetchContext.success = true;
		
		var dashboard = this;
		var chartResults = dashboardResult.chartResults;
		var chartErrors = dashboardResult.chartErrors;
		
		/* 暂不启用，很难覆盖完整周期，且暴露过多内部结构
		var listener = this.listener();
		if(listener && listener.fetchSuccess)
		{
			chartFactory.executeSilently(function()
			{
				listener.fetchSuccess(dashboard, dashboardResult, fetchContext);
			});
		}
		*/
		
		for(var chartId in chartResults)
		{
			var chart = this.chartOf(chartId);
			
			if(!chart)
				continue;
			
			chartFactory.executeSilently(function()
			{
				var chartResult = (chartResults[chartId] || {});
				dashboard._updateChart(chart, chartResult, true);
			});
		}
		
		for(var chartId in chartErrors)
		{
			var chart = this.chartOf(chartId);
			
			if(!chart)
				continue;
			
			chartFactory.executeSilently(function()
			{
				var error = (chartErrors[chartId] || {});
				dashboard._handleChartAjaxError(chart, error, true);
			});
		}
		
		/* 暂不启用，很难覆盖完整周期，且暴露过多内部结构
		if(listener && listener.fetchComplete)
		{
			chartFactory.executeSilently(function()
			{
				listener.fetchComplete(dashboard, fetchContext);
			});
		}
		*/
	};
	
	dashboardBase._handleChartsAjaxError = function(fetchContext, xhr, textStatus, errorThrown)
	{
		fetchContext.xhr = xhr;
		fetchContext.success = false;
		
		var dashboard = this;
		var charts = fetchContext.charts;
		//结构参考：org.datagear.analysis.support.ChartResultErrorMessage
		var error = { type: "Error", message: (errorThrown ? errorThrown : (textStatus ? textStatus : "error")) };
		var logException = true;
		
		/* 暂不启用，很难覆盖完整周期，且暴露过多内部结构
		var listener = this.listener();
		if(listener && listener.fetchError)
		{
			logException = false;
			
			chartFactory.executeSilently(function()
			{
				listener.fetchError(dashboard, error, fetchContext);
			});
		}
		else
		{
			logException = true;
		}
		*/
		
		for(var i=0; i<charts.length; i++)
		{
			var chart = charts[i];
			
			chartFactory.executeSilently(function()
			{
				dashboard._handleChartAjaxError(chart, error, false);
			});
		}
		
		/* 暂不启用，很难覆盖完整周期，且暴露过多内部结构
		if(listener && listener.fetchComplete)
		{
			chartFactory.executeSilently(function()
			{
				listener.fetchComplete(dashboard, fetchContext);
			});
		}
		*/
		
		if(logException)
		{
			chartFactory.logException("Fetch charts data error : " + error.message);
		}
	};
	
	dashboardBase._handleChartAjaxError = function(chart, error, logIfNone)
	{
		this._handleChartResultError(chart, error, true, logIfNone);
	};
	
	/**
	 * 处理图表结果错误。
	 * 
	 * @param chart 图表对象
	 * @param error 图表结果错误信息对象，结构参考：org.datagear.analysis.support.ChartResultErrorMessage
	 * @param setErrorStatus 是否将图表状态更新为：chartStatusConst.UPDATE_ERROR
	 * @param logIfNone 可选，如果chart.listener()没有定义updateError，是否输出默认日志，默认为：true
	 */
	dashboardBase._handleChartResultError = function(chart, error, setErrorStatus, logIfNone)
	{
		logIfNone = (logIfNone == null ? true : logIfNone);
		
		if(!chart)
			return;
		
		if(setErrorStatus)
		{
			chart.status(chartStatusConst.UPDATE_ERROR);
		}
		
		var chartListener = chart.listener();
		
		if(chartListener && chartListener.updateError)
		{
			chartListener.updateError(chart, error);
			return;
		}
		
		if(logIfNone)
		{
			var type = (error ? error.type : "Error");
			var message = (error ? error.message : "Chart result error");
			chartFactory.logException("["+chart.name+"]["+chart.elementWidgetId()+"] " + type + " : " + message);
		}
	};
	
	/**
	 * 更新指定图表。
	 * 
	 * @param chart 图表对象
	 * @param chartResult 图表结果对象，参考：org.datagear.analysis.ChartResult
	 * @param force 可选，是否强制更新，默认值：false
	 */
	dashboardBase._updateChart = function(chart, chartResult, force)
	{
		force = (force === true);
		
		try
		{
			if(chart.isActive())
			{
				if(force)
				{
					if(!chart.statusRendered() && !chart.statusPreUpdate() && !chart.statusUpdated())
					{
						chart.statusPreUpdate(true);
					}
				}
				
				this._doUpdateChart(chart, chartResult);
			}
			else
				chartFactory.logException("chart '#"+chart.elementId+"' not active");
		}
		catch(e)
		{
			//设置为更新出错状态，避免更新失败后会_doHandleCharts中会无限尝试更新
			chart.status(chartStatusConst.UPDATE_ERROR);
			chartFactory.logException(e);
		}
	};
	
	dashboardBase._doUpdateChart = function(chart, chartResult)
	{
		chartResult = chart._toApiStdResult(chartResult);
		chart.update(chartResult);
	};
	
	dashboardBase._setChartsUpdateTime = function(charts, time)
	{
		chartFactory.executeSilently(function()
		{
			for(var i=0; i<charts.length; i++)
			{
				charts[i]._updateTime(time);
			}
		});
	};
	
	dashboardBase._startChartsRefreshData = function(charts)
	{
		for(var i=0; i<charts.length; i++)
		{
			charts[i]._startRefreshData();
		}
	};
	
	dashboardBase._finishChartsRefreshDataIfMatch = function(charts)
	{
		chartFactory.executeSilently(function()
		{
			for(var i=0; i<charts.length; i++)
			{
				charts[i]._finishRefreshDataIfMatch();
			}
		});
	};
	
	/**
	 * 构建更新看板的ajax请求数据。
	 */
	dashboardBase._buildUpdateDashboardAjaxData = function(charts)
	{
		var updateDashboardConfig = dashboardFactory.updateDashboardConfig;
		
		var globalResultDataFormat = this.resultDataFormat();
		
		//这里需要深度拷贝，因为后续可能会被修改
		if(globalResultDataFormat)
			globalResultDataFormat = $.extend(true, {}, globalResultDataFormat);
		
		var dashboardQueryForm = {};
		var dashboardQuery = { chartQueries: {}, resultDataFormat: globalResultDataFormat, suppressChartError: true };
		
		dashboardQueryForm[updateDashboardConfig.dashboardIdParamName] = this.id;
		this._dashboardQueryOfForm(dashboardQueryForm, dashboardQuery);
		
		if(charts && charts.length)
		{
			for(var i=0; i<charts.length; i++)
			{
				var chart = charts[i];
				var chartId = chart.id;
				
				var chartQuery = { dataSetQueries: [], resultDataFormat: chart.resultDataFormat() };
				
				if(chartQuery.resultDataFormat)
				{
					//这里需要深度拷贝，因为后续可能会被修改
					chartQuery.resultDataFormat = $.extend(true, {}, chartQuery.resultDataFormat);
				}
				else
				{
					chartQuery.resultDataFormat = globalResultDataFormat;
				}
				
				var dataSetBinds = chart.dataSetBinds();
				for(var j=0; j<dataSetBinds.length; j++)
				{
					//这里需要深度拷贝，因为后续可能会被修改
					var dataSetQuery = $.extend(true, {}, dataSetBinds[j].query);
					chartQuery.dataSetQueries.push(dataSetQuery);
				}
				
				dashboardQuery.chartQueries[chartId] = chartQuery;
			}
		}
		
		return dashboardQueryForm;
	};
	
	dashboardBase._dashboardQueryOfForm = function(dashboardQueryForm, dashboardQuery)
	{
		var dashboardQueryParamName = dashboardFactory.updateDashboardConfig.dashboardQueryParamName;
		
		if(dashboardQuery === undefined)
		{
			return dashboardQueryForm[dashboardQueryParamName];
		}
		else
		{
			dashboardQueryForm[dashboardQueryParamName] = dashboardQuery;
		}
	};
	
	/**
	 * 异步加载单个图表，并将其加入此看板。
	 * 
	 * 支持调用方式：
	 * dashboard.loadChart(element);
	 * dashboard.loadChart(element, chartWidgetId);
	 * dashboard.loadChart(element, ajaxOptions);
	 * dashboard.loadChart(element, chartWidgetId, ajaxOptions);
	 * 
	 * @param element 用于渲染图表的HTML元素、Jquery选择器、Jquery对象
	 * @param chartWidgetId 选填参数，要加载的图表部件ID，如果不设置，将从元素的"dg-chart-widget"属性取
	 * @param ajaxOptions 选填参数，参数格式可以是图表加载成功回调函数：function(chart){ ... }，也可以是ajax配置项：{...}。
	 * 					  如果图表加载成功回调函数、ajax配置项的success函数返回false，则这个图表不会加入此看板。
	 */
	dashboardBase.loadChart = function(element, chartWidgetId, ajaxOptions)
	{
		//异步加载无需看板已渲染
		//this._assertAlive();
		
		element = chartFactory.toJqueryObj(element);
		
		if(this._loadingChartElement(element))
			throw new Error("the element is loading chart");
		
		if(this.renderedChart(element) != null)
			throw new Error("the element has been rendered as chart");
		
		//看板中可能存在已初始化但是未渲染的图表，也不应允许异步加载
		if(this.chartOf(element) != null)
			throw new Error("there is a chart on this element");
		
		if(!chartFactory.isString(chartWidgetId))
		{
			ajaxOptions = chartWidgetId;
			chartWidgetId = null;
		}
		
		if(!chartWidgetId)
			chartWidgetId = chartFactory.elementWidgetId(element);
		
		if(!chartWidgetId)
			throw new Error("[chartWidgetId] argument or ["+chartFactory.elementAttrConst.WIDGET
				+"] attribute must be set for HTML element");
		
		if(!ajaxOptions)
			ajaxOptions = {};
		else if($.isFunction(ajaxOptions))
		{
			var successHandler = ajaxOptions;
			ajaxOptions =
			{
				success: successHandler
			};
		}
		
		var dashboard = this;
		
		var myAjaxOptions = $.extend({}, ajaxOptions);
		
		var successHandler = myAjaxOptions.success;
		myAjaxOptions.success = function(chart, textStatus, jqXHR)
		{
			dashboard._initLoadedChart(chart, element, chartWidgetId);
			
			var re = true;
			
			if(successHandler)
				re = successHandler.call(this, chart, textStatus, jqXHR);
			
			if(re != false)
				dashboard._addLoadedChart(chart);
		};
		
		var completeHandler = myAjaxOptions.complete;
		myAjaxOptions.complete = function(jqXHR, textStatus)
		{
			dashboard._loadingChartElement(element, false);
			
			if(completeHandler)
				re = completeHandler.call(this, jqXHR, textStatus);
		};
		
		this._loadingChartElement(element, true);
		
		this._loadChartJson(chartWidgetId, myAjaxOptions);
	};
	
	/**
	 * 异步加载多个图表，并将它们加入此看板。
	 * 
	 * 支持调用方式：
	 * dashboard.loadCharts(element);
	 * dashboard.loadCharts(element, chartWidgetId);
	 * dashboard.loadCharts(element, ajaxOptions);
	 * dashboard.loadCharts(element, chartWidgetId, ajaxOptions);
	 * 
	 * @param element 用于渲染图表的HTML元素、HTML元素数组、Jquery选择器、Jquery对象
	 * @param chartWidgetId 可选，要加载的图表部件ID、图表部件ID数组，如果不设置，将从元素的"dg-chart-widget"属性取
	 * @param ajaxOptions 可选，参数格式可以是图表数组加载成功回调函数：function(charts){ ... }，也可以是ajax配置项：{...}。
	 * 					  如果图表数组加载成功回调函数、ajax配置项的success函数返回false，则这些图表不会加入此看板。
	 */
	dashboardBase.loadCharts = function(element, chartWidgetId, ajaxOptions)
	{
		//异步加载无需看板已渲染
		//this._assertAlive();
		
		element = chartFactory.toJqueryObj(element);
		
		for(var i=0; i<element.length; i++)
		{
			if(this._loadingChartElement(element[i]))
				throw new Error("the "+i+"-th element is loading chart");
			
			if(this.renderedChart(element[i]) != null)
				throw new Error("the "+i+"-th element has been rendered as chart");
			
			//看板中可能存在已初始化但是未渲染的图表，也不应允许异步加载
			if(this.chartOf(element[i]) != null)
				throw new Error("there is a chart on the "+i+"-th element");
		}
		
		if(!chartFactory.isString(chartWidgetId) && !$.isArray(chartWidgetId))
		{
			ajaxOptions = chartWidgetId;
			chartWidgetId = null;
		}
		
		if(chartWidgetId == null)
			chartWidgetId = [];
		else if(chartFactory.isString(chartWidgetId))
			chartWidgetId = [ chartWidgetId ];
		
		if(!ajaxOptions)
			ajaxOptions = {};
		else if($.isFunction(ajaxOptions))
		{
			var successHandler = ajaxOptions;
			ajaxOptions =
			{
				success: successHandler
			};
		}
		
		var chartWidgetIds = [];
		
		element.each(function(index)
		{
			var $thisEle = $(this);
			
			var widgetId = (index < chartWidgetId.length ? chartWidgetId[index] : null);
			if(!widgetId)
				widgetId = chartFactory.elementWidgetId($thisEle);
			
			if(!widgetId)
				throw new Error("[chartWidgetId] argument or ["+chartFactory.elementAttrConst.WIDGET
					+"] attribute must be set for "+index+"-th element");
			
			chartWidgetIds.push(widgetId);
		});
		
		var dashboard = this;
		
		var myAjaxOptions = $.extend({}, ajaxOptions);
		
		var successHandler = myAjaxOptions.success;
		myAjaxOptions.success = function(charts, textStatus, jqXHR)
		{
			for(var i=0; i<charts.length; i++)
				dashboard._initLoadedChart(charts[i], element[i], chartWidgetIds[i]);
			
			var re = true;
			
			if(successHandler)
				re = successHandler.call(this, charts, textStatus, jqXHR);
			
			if(re != false)
			{
				for(var i=0; i<charts.length; i++)
					dashboard._addLoadedChart(charts[i]);
			}
		};
		
		var completeHandler = myAjaxOptions.complete;
		myAjaxOptions.complete = function(jqXHR, textStatus)
		{
			dashboard._loadingChartElement(element, false);
			
			if(completeHandler)
				re = completeHandler.call(this, jqXHR, textStatus);
		};
		
		this._loadingChartElement(element, true);
		
		this._loadChartJson(chartWidgetIds, myAjaxOptions);
	};
	
	/**
	 * 将元素内（包括自身）所有设置了"dg-chart-widget"属性，且未初始化为图表的HTML元素异步加载为图表。
	 * 如果没有需要加载的元素，将不会执行异步请求。
	 * 
	 * 支持调用方式：
	 * dashboard.loadUnsolvedCharts();
	 * dashboard.loadUnsolvedCharts(element);
	 * dashboard.loadUnsolvedCharts(ajaxOptions);
	 * dashboard.loadUnsolvedCharts(element, ajaxOptions);
	 * 
	 * @param element 可选，限定查找的根HTML元素、Jquery选择器、Jquery对象，默认为：<body>元素
	 * @param ajaxOptions 可选，参数格式可以是图表数组加载成功回调函数：function(charts){ ... }，也可以是ajax配置项：{...}。
	 * 					  如果图表数组加载成功回调函数、ajax配置项的success函数返回false，则这些图表不会加入此看板。
	 * @return 要异步加载的HTML元素数组
	 */
	dashboardBase.loadUnsolvedCharts = function(element, ajaxOptions)
	{
		//异步加载无需看板已渲染
		//this._assertAlive();
		
		//(ajaxOptions)
		if(arguments.length == 1 && !chartFactory.isString(element) && !chartFactory.isDomOrJquery(element))
		{
			ajaxOptions = element;
			element = undefined;
		}
		
		element = chartFactory.toJqueryObj(element == null ? document.body : element);
		
		var widgetEles = $(chartFactory.domsWithWidgetId(element));
		var unsolved = [];
		
		var dashboard = this;
		widgetEles.each(function()
		{
			if(dashboard._loadingChartElement(this))
				return;
			
			if($(this).attr(chartFactory.elementAttrConst.WIDGET)
				&& dashboard.renderedChart(this) == null
				//看板中可能存在对应此元素的已初始化但是未渲染的图表，这里也要排除
				&& dashboard.chartOf(this) == null)
			{
				unsolved.push(this);
			}
		});
		
		if(unsolved.length > 0)
			this.loadCharts(unsolved, ajaxOptions);
		
		return unsolved;
	};
	
	/**
	 * 获取单个/设置多个元素是否正在加载图表。
	 */
	dashboardBase._loadingChartElement = function(element, set)
	{
		element = $(element);
		
		var name = chartFactory.builtinPropName("loadingChart");
		
		if(set === undefined)
		{
			return (element.data(name) == true);
		}
		else
		{
			element.each(function()
			{
				var $this = $(this);
				
				if(set == true)
					$this.data(name, true);
				else
					$this.removeData(name);
			});
		}
	};
	
	/**
	 * 初始化异步加载的图表。
	 * 
	 * @param chart 图表JSON对象
	 * @param element 图表HTML元素、Jquery对象
	 * @param chartWidgetId 要异步加载的图表部件ID
	 */
	dashboardBase._initLoadedChart = function(chart, element, chartWidgetId)
	{
		element = $(element);
		
		//这里不应设置"dg-chart-widget"属性而破坏了元素的原生结构
		//chartFactory.elementWidgetId(element, chartWidgetId);
		
		chartFactory.checkSetChartElementId(element, chart);
		dashboardFactory._initChart(this, chart);
	};
	
	dashboardBase._addLoadedChart = function(chart)
	{
		this.addChart(chart);
		
		if(chart.manualRender())
			return;
		
		if(chart.statusPreInit())
		{
			//应设为与看板状态保持一致
			if(this.statusInited())
			{
				chart.init();
			}
			else if(this.isAlive())
			{
				chart.init();
				chart.statusPreRender(true);
			}
		}
	};
	
	/**
	 * 异步加载图表JSON对象。
	 * 图表JSON对象仅是简单的JSON数据，没有初始化为实际可用的图表对象，也不会加入此看板。
	 * 
	 * @param chartWidgetId 图表部件ID、图表部件ID数组
	 * @param ajaxOptions 选填参数，参数格式可以是ajax配置项的success回调函数：function(data){ ... }，也可以是ajax配置项：{...}。
						  注意：当chartWidgetId是单个字符串时，success函数的data参数将是单个JSON对象；
						  当chartWidgetId是数组时，success函数的data参数将是JSON对象数组。
	 */
	dashboardBase._loadChartJson = function(chartWidgetId, ajaxOptions)
	{
		var isFetchSingle = (!$.isArray(chartWidgetId));
		
		var chartWidgetIds = chartWidgetId;
		
		if(!$.isArray(chartWidgetIds))
			chartWidgetIds = [ chartWidgetIds ];
		
		if(!ajaxOptions)
			ajaxOptions = {};
		else if($.isFunction(ajaxOptions))
		{
			var successHandler = ajaxOptions;
			ajaxOptions =
			{
				success: successHandler
			};
		}
		
		var webContext = chartFactory.renderContextAttrWebContext(this.renderContext);
		var url = this.contextURL(webContext.attributes.loadChartURL);
		var loadChartConfig = dashboardFactory.loadChartConfig;
		
		var dashboard = this;
		
		var data = [];
		data[0] = { name: loadChartConfig.dashboardIdParamName, value: dashboard.id };
		for(var i=0; i<chartWidgetIds.length; i++)
		{
			data.push({ name: loadChartConfig.chartWidgetIdParamName, value: chartWidgetIds[i] });
		}
		
		var myAjaxOptions = $.extend(
		{
			url: url,
			data: data,
			type: "POST"
		},
		ajaxOptions);
		
		var successHandler = myAjaxOptions.success;
		myAjaxOptions.success = function(charts, textStatus, jqXHR)
		{
			charts = (charts || []);
			
			if(successHandler)
			{
				var handlerChart = (isFetchSingle ? (charts.length > 0 ? charts[0] : null) : charts);
				successHandler.call(this, handlerChart, textStatus, jqXHR);
			}
		};
		
		$.ajax(myAjaxOptions);
	};
	
	/**
	 * 批量设置图表数据集参数值。
	 * 
	 * 批量设置对象格式为：
	 * {
	 *   //可选，要设置的目标图表元素ID、图表ID、看板图表数组索引，或者它们的数组
	 *   target: "..."、["...", ...],
	 *   
	 *   //可选，要设置的参数值映射表，没有则不设置任何参数值
	 *   data:
	 *   {
	 *     源参数名 : 图表数据集参数索引对象、[ 图表数据集参数索引对象, ... ],
	 *     ...
	 *   }
	 * }
	 * 
	 * 上述【源参数名】可以是简单参数名，例如："name"、"value"，也可以是源参数对象的属性路径，例如："order.name"、"[0].name"、"['order'].product.name"
	 * 
	 * 图表数据集参数索引对象用于确定源参数值要设置到的目标图表数据集参数，格式为：
	 * {
	 *   //可选，可以是上述批量设置对象的target数组中的索引，也可以是图表元素ID、图表ID、看板图表数组索引，默认值为：0
	 *   chart: 数值、"...",
	 *   
	 *   //可选，目标图表数据集数组的索引数值，默认为：0
	 *   dataSet: ...,
	 *   
	 *   //可选，目标图表数据集的参数数组索引/参数名，默认为：0
	 *   param: ...,
	 *   
	 *   //可选，自定义源参数值处理函数，返回要设置的目标参数值
	 *   //sourceValue 源参数值
	 *   //sourceValueContext 源参数值上下文对象
	 *   value: function(sourceValue, sourceValueContext){ return ...; }
	 * }
	 * 或者，可简写为上述图表数据集参数索引对象的"param"属性值
	 * 
	 * @param sourceData 源参数值对象，格式为：{ 源参数名 : 源参数值, ...} 或者 { getValue: function(name){ return ...; } }（需支持属性路径）
	 * @param batchSet 批量设置对象
	 * @param sourceValueContext 可选，传递给图表数据集参数索引对象的value函数sourceValueContext参数的对象，如果为数组，则传递多个参数，默认为sourceData
	 * @return 批量设置的图表对象数组
	 */
	dashboardBase._batchSetDataSetParamValues = function(sourceData, batchSet, sourceValueContext)
	{
		sourceValueContext = (sourceValueContext === undefined ? sourceData : sourceValueContext);
		
		var targets = (batchSet.target == null ? [] : ($.isArray(batchSet.target) ? batchSet.target : [ batchSet.target ]));
		var targetCharts = [];
		
		for(var i=0; i<targets.length; i++)
		{
			targetCharts[i] = this.chartOf(targets[i]);
			
			if(targetCharts[i] == null)
				throw new Error("no chart found for : " + targets[i]);
		}
		
		var dataMap = (batchSet.data || {});
		var hasGetValueFunc = $.isFunction(sourceData.getValue);
		
		var sourceValueContextArgs = [ "place-holder-for-source-value" ];
		sourceValueContextArgs = sourceValueContextArgs.concat($.isArray(sourceValueContext) ? sourceValueContext : [ sourceValueContext ]);
		
		for(var name in dataMap)
		{
			var dataValue = (hasGetValueFunc ? sourceData.getValue(name)
					: dashboardFactory.getPropertyPathValue(sourceData, name));
			
			var indexes = dataMap[name];
			
			if(!$.isArray(indexes))
				indexes = [ indexes ];
			
			for(var i=0; i<indexes.length; i++)
			{
				var indexObj = indexes[i];
				
				var chartIdx = 0;
				var dataSetIdx = 0;
				var param = 0;
				var paramValue = null;
				
				//参数名/索引号
				if(chartFactory.isStringOrNumber(indexObj))
				{
					param = indexObj;
					paramValue = dataValue;
				}
				else
				{
					chartIdx = (indexObj.chart != null ? indexObj.chart : chartIdx);
					dataSetIdx = (indexObj.dataSet != null ? indexObj.dataSet : dataSetIdx);
					param = (indexObj.param != null ? indexObj.param : param);
					
					if(indexObj.value)
					{
						sourceValueContextArgs[0] = dataValue;
						paramValue = indexObj.value.apply(indexObj, sourceValueContextArgs);
					}
					else
						paramValue = dataValue;
				}
				
				var targetChart = null;
				
				//优先使用batchSet.target中的索引号
				if(chartFactory.isNumber(chartIdx) && targets[chartIdx] != null)
				{
					targetChart = targetCharts[chartIdx];
				}
				else
				{
					targetChart = this.chartOf(chartIdx);
					
					if(targetChart == null)
						throw new Error("no chart found for : " + chartIdx);
					
					if(chartFactory.indexInArray(targetCharts, targetChart) < 0)
						targetCharts.push(targetChart);
				}
				
				targetChart.dataSetParamValue(dataSetIdx, param, paramValue);
			}
		}
		
		return targetCharts;
	};
	
	/**
	 * 获取服务端当前日期。
	 * 服务端当前日期 = 网页加载时的服务端日期 + (客户端当前日期 - 网页加载时客户端日期) 
	 * 因此，返回的并不是精确的服务端当前日期，通常是偏差数十至数百毫秒。
	 * 
	 * @param asMillisecond 可选，是否返回毫秒数值而非Date对象，默认为：false 
	 * @return Date对象，或者毫秒数值
	 */
	dashboardBase.serverDate = function(asMillisecond)
	{
		//参考org.datagear.web.controller.ServerTimeJsController.SERVERTIME_JS_VAR
		if(global.DATAGEAR_SERVER_TIME == null)
			throw new Error("get current server date is not supported");
		
		var cct = chartFactory.currentDateMs();
		var cst = global.DATAGEAR_SERVER_TIME + (cct - dashboardFactory.LOAD_TIME);
		
		if(asMillisecond == true)
			return cst;
		
		var csd = new Date();
		csd.setTime(cst);
		
		return csd;
	};
	
	/**
	 * 获取当前用户信息。
	 * 
	 * @returns 用户信息，格式参考：org.datagear.web.util.WebDashboardQueryConverter.AnalysisUser
	 */
	dashboardBase.user = function()
	{
		var user = this.renderContextAttr(renderContextAttrConst.user);
		
		if(user == null)
			throw new Error("get user not support");
		
		return user;
	};
	
	/**
	 * 销毁看板，销毁所有看板表单、所有图表。
	 * 销毁中的看板处于this.statusDestroying()状态，看板完成后处于this.statusDestroyed()状态。
	 * 
	 * @returns true 正常执行销毁；false 未执行销毁，因为看板处于销毁非法状态
	 */
	dashboardBase.destroy = function()
	{
		if(!this.isAlive() || this.statusDestroying() || this.statusDestroyed())
			return false;
		
		this.statusDestroying(true);
		
		var doDestroy = true;
		
		var listener = this.listener();
		if(listener && listener.onDestroy)
			doDestroy = listener.onDestroy(this);
		
		if(doDestroy != false)
		{
			this.doDestroy();
		}
		
		return true;
	};
	
	dashboardBase._destroyCharts = function()
	{
		var charts = (this.charts || []);
		
		for(var i=0; i<charts.length; i++)
			this._destroyChart(charts[i]);
	};
	
	dashboardBase._destroyChart = function(chart)
	{
		try
		{
			chart.destroy();
		}
		catch(e)
		{
			chartFactory.logException(e);
		}
	};
	
	dashboardBase._destroyForms = function()
	{
		var $forms = $("form[dg-dashboard-form]", document.body);
		
		var thisDashboard = this;
		$forms.each(function()
		{
			thisDashboard._destroyForm(this);
		});
		
		var globalTheme = chartFactory.renderContextAttrChartTheme(this.renderContext);
		chartFactory.removeThemeRefEntity(globalTheme, dashboardFactory._THEME_REF_DASHBOARD_FORM_ID);
	};
	
	dashboardBase._destroyForm = function(form)
	{
		try
		{
			chartFactory.chartSetting.destroyDataSetParamValueForm(form);
		}
		catch(e)
		{
			chartFactory.logException(e);
		}
	};
	
	/**
	 * 获取图表展示数据的原始数据索引，应是已由chartBase.originalDataIndex()、chartBase.originalDataIndexes()函数设置过。
	 * 
	 * @param data 图表展示数据、或其数组，格式为：{ ... }、[ ... ]、[ { ... }, ... ]、、[ [ ... ], ... ]
	 * @param inflate 可选，是否在返回原始数据索引对象的复本并填充图表对象、原始数据集结果数据，默认值为：true
	 * @returns 原始数据索引(可能为null）、或其数组(元素可能为null），格式为：
	 *									{
	 *										//同chartBase.originalDataIndexes()函数返回的原始数据索引对象结构
	 *										...,
	 *										//当inflate为true时，chartId对应的图表对象
	 *										chart: 图表对象,
	 *										//当inflate为true时，resultDataIndex对应的原始结果数据，格式为：
	 *                                      //当dataSetBindIndex是数值时：
	 *                                      //对象、对象数组
	 *                                      //当dataSetBindIndex是数值数组时：
	 *                                      //数组（元素可能是对象、对象数组）
	 *										resultData: 结果数据
	 *									}
	 *									当data是图表展示数据数组时，将返回此结构的数组。
	 * @since 3.1.0
	 */
	dashboardBase.originalDataIndex = function(data, inflate)
	{
		if(!data)
			return undefined;
		
		var odIdx = chartFactory.originalDataIndex(data);
		
		if(odIdx == null && $.isArray(data))
		{
			odIdx = [];
			
			for(var i=0; i<data.length; i++)
				odIdx[i] = chartFactory.originalDataIndex(data[i]);
		}
		
		if(odIdx == null || inflate === false)
			return odIdx;
		
		var isArray = $.isArray(odIdx);
		var odIdxAry = (isArray ? odIdx : [ odIdx ]);
		var odIdxAryClone = [];
		
		for(var i=0; i<odIdxAry.length; i++)
		{
			var odIdxMy = (odIdxAry[i] ? $.extend({}, odIdxAry[i]) : null);
			odIdxAryClone[i] = odIdxMy;
			
			if(odIdxMy)
			{
				var chart = this.chartOf(odIdxMy.chartId);
				odIdxMy["chart"] = chart;
				if(chart)
					odIdxMy["resultData"] = chartFactory.originalData(chart, odIdxMy);
			}
		}
		
		return (isArray ? odIdxAryClone : odIdxAryClone[0]);
	};
	
	/**
	 * 看板是否是活着的（已执行渲染且未完成销毁）。
	 * 
	 * @since 4.4.0
	 */
	dashboardBase.isAlive = function()
	{
		return (this._isAlive == true);
	};
	
	/**
	 * 看板是否处于活跃可用的状态（已完成渲染且未执行销毁）。
	 * 
	 * @since 4.4.0
	 */
	dashboardBase.isActive = function()
	{
		return (this._isActive == true);
	};
	
	/**
	 * 获取/设置看板状态。
	 * 注意：此函数的设置操作仅设置状态值，不执行任何其他逻辑，设置看板生命周期状态应使用具体的this.status*(true)函数。
	 * 
	 * @param status 可选，要设置的状态，不设置则执行获取操作
	 */
	dashboardBase.status = function(status)
	{
		if(status === undefined)
			return (this._status || "");
		else
			this._status = status;
	};
	
	/**
	 * 看板是否为/设置为：准备初始化。
	 * 
	 * @param set 可选，为true时设置状态；否则，判断状态
	 * 
	 * @since 4.4.0
	 */
	dashboardBase.statusPreInit = function(set)
	{
		if(set === true)
		{
			this._isActive = false;
			this._isAlive = false;
			this.status(dashboardStatusConst.PRE_INIT);
		}
		else
			return (this.status() == dashboardStatusConst.PRE_INIT);
	};
	
	/**
	 * 看板是否为/设置为：正在初始化。
	 * 
	 * @param set 可选，为true时设置状态；否则，判断状态
	 * 
	 * @since 4.4.0
	 */
	dashboardBase.statusIniting = function(set)
	{
		if(set === true)
		{
			this._isActive = false;
			this._isAlive = false;
			this.status(dashboardStatusConst.INITING);
		}
		else
			return (this.status() == dashboardStatusConst.INITING);
	};
	
	/**
	 * 看板是否为/设置为：完成初始化。
	 * 
	 * @param set 可选，为true时设置状态；否则，判断状态
	 * 
	 * @since 4.4.0
	 */
	dashboardBase.statusInited = function(set)
	{
		if(set === true)
		{
			this._isActive = false;
			this._isAlive = false;
			this.status(dashboardStatusConst.INITED);
		}
		else
			return (this.status() == dashboardStatusConst.INITED);
	};
	
	/**
	 * 看板是否为/设置为：渲染中。
	 * 
	 * @param set 可选，为true时设置状态；否则，判断状态
	 * 
	 * @since 4.4.0
	 */
	dashboardBase.statusRendering = function(set)
	{
		if(set === true)
		{
			this._isActive = false;
			this._isAlive = true;
			this.status(dashboardStatusConst.RENDERING);
		}
		else
			return (this.status() == dashboardStatusConst.RENDERING);
	};
	
	/**
	 * 看板是否为/设置为：完成渲染。
	 * 
	 * @param set 可选，为true时设置状态；否则，判断状态
	 * @param postProcess 可选，当是设置操作时，是否执行后置操作，比如调用监听器的render函数，默认为true
	 * 
	 * @since 4.4.0
	 */
	dashboardBase.statusRendered = function(set, postProcess)
	{
		if(set === true)
		{
			this._isActive = true;
			this._isAlive = true;
			this.status(dashboardStatusConst.RENDERED);
			
			if(postProcess == null || postProcess == true)
				this._postProcessRendered();
		}
		else
			return (this.status() == dashboardStatusConst.RENDERED);
	};
	
	/**
	 * 渲染完成后置处理。
	 */
	dashboardBase._postProcessRendered = function()
	{
		var listener = this.listener();
		if(listener && listener.render)
			listener.render(this);
	};
	
	/**
	 * 看板是否为/设置为：正在销毁。
	 * 
	 * @param set 可选，为true时设置状态；否则，判断状态
	 * 
	 * @since 4.4.0
	 */
	dashboardBase.statusDestroying = function(set)
	{
		if(set === true)
		{
			this._isActive = false;
			this._isAlive = true;
			this.status(dashboardStatusConst.DESTROYING);
		}
		else
			return (this.status() == dashboardStatusConst.DESTROYING);
	};
	
	/**
	 * 看板是否为/设置为：完成销毁。
	 * 
	 * @param set 可选，为true时设置状态；否则，判断状态
	 * @param postProcess 可选，当是设置操作时，是否执行后置操作，比如调用监听器的destroy函数，默认为true
	 * 
	 * @since 4.4.0
	 */
	dashboardBase.statusDestroyed = function(set, postProcess)
	{
		if(set === true)
		{
			this._isActive = false;
			this._isAlive = false;
			this.status(dashboardStatusConst.DESTROYED);
			
			if(postProcess == null || postProcess == true)
				this._postProcessDestroyed();
		}
		else
			return (this.status() == dashboardStatusConst.DESTROYED);
	};
	
	dashboardBase._postProcessDestroyed = function()
	{
		var listener = this.listener();
		if(listener && listener.destroy)
			listener.destroy(this);
	};
	
	/**
	 * 执行看板销毁。
	 * 
	 * @since 4.4.0
	 */
	dashboardBase.doDestroy = function()
	{
		if(!this.statusDestroying())
			throw new Error("dashboard is illegal state for doDestroy()");
		
		this.stopHandleCharts();
		this._destroyCharts();
		this._destroyForms();
		
		this.statusDestroyed(true);
	};
	
	/**
	 * 为以"/"开头的URL添加系统根路径前缀，否则，将直接返回原URL。
	 * 当需要访问系统内其他功能模块的资源时，应为其URL添加系统根路径前缀。
	 * 
	 * @param url 可选，要处理的URL
	 * @return 添加后的新URL，如果没有url参数，将返回系统根路径
	 * @since 5.0.0
	 */
	dashboardBase.contextURL = function(url)
	{
		var renderContext = this.renderContext;
		var webContext = chartFactory.renderContextAttrWebContext(renderContext);
		
		if(!webContext)
		{
			throw new Error("dashboard is illegal state for contextURL(url)");
		}
		
		return chartFactory.toWebContextPathURL(webContext, url);
	};
	
	/**
	 * 获取版本。
	 * 具体参考：org.datagear.web.analysis.DashboardVersion
	 * 
	 * @return 版本号，目前只有：1.0、2.0
	 * @since 5.3.0
	 */
	/*
	dashboardBase.version = function()
	{
		return this._version;
	};
	*/
	
	/**
	 * 获取指定元素内（包括自身）包含的所有图表。
	 *
	 * @param element DOM元素、Jquery对象
	 * @param active 可选，是否仅返回已完成渲染且未执行销毁的图表，true 是；false 否。默认值：false
	 * @return [ ... ]
	 * @since 5.3.0
	 */
	dashboardBase.chartsIn = function(element, active)
	{
		element = $(element);
		active = (active == null ? false : active);
		
		element = element.add($("[id]", element));
		
		var re = [];
		
		var dashboard = this;
		element.each(function()
		{
			var id = $(this).attr("id");
			var chart = (id ? dashboard.chartOf(id) : null);
			
			if(!chart)
				return;
			
			if(!active || (active && chart.isActive()))
				re.push(chart);
		});
		
		return re;
	};
	
	/**
	 * 重新调整指定元素内（包括自身）包含的所有图表尺寸。
	 * 
	 * @param element DOM元素、Jquery对象
	 * @return 已调整尺寸的图表数组：[ ... ]
	 * @since 5.3.0
	 */
	dashboardBase.resizeChartsIn = function(element)
	{
		var charts = this.chartsIn(element, true);
		
		for(var i=0; i<charts.length; i++)
		{
			charts[i].resize();
		}
		
		return charts;
	};
	
	//-------------
	// < 已弃用函数 start
	//-------------
	
	// < @deprecated 兼容4.3.1版本的API，此函数没有必要作为开放API，因为并不如直接chart.dataSetParamValue*()好用，将在未来版本移除
	/**
	 * 批量设置图表数据集参数值。
	 * 
	 * @param sourceData 源参数值对象，格式为：{ 源参数名 : 源参数值, ...} 或者 { getValue: function(name){ return ...; } }（需支持属性路径）
	 * @param batchSet 批量设置对象
	 * @param sourceValueContext 可选，传递给图表数据集参数索引对象的value函数sourceValueContext参数的对象，如果为数组，则传递多个参数，默认为sourceData
	 * @return 批量设置的图表对象数组
	 */
	dashboardBase.batchSetDataSetParamValues = function(sourceData, batchSet, sourceValueContext)
	{
		return this._batchSetDataSetParamValues(sourceData, batchSet, sourceValueContext);
	};
	// > @deprecated 兼容4.3.1版本的API，此函数没有必要作为开放API，因为并不如直接chart.dataSetParamValue*()好用，将在未来版本移除
	
	// < @deprecated 兼容4.3.1版本的dg-dashboard-listener中监听图表相关功能，将在未来版本移除
	/**
	 * 获取看板的代理图表监听器。
	 * 为了确保任意时刻设置看板监听器（dashboard.listener(...)）都能传递至图表，所以此方法应始终返回不为null且引用不变的对象。
	 */
	dashboardBase._getDelegateChartListener = function()
	{
		var chartListener = (this._delegateChartListener || (this._delegateChartListener = {}));
		return chartListener;
	};
	// > @deprecated 兼容4.3.1版本的dg-dashboard-listener中监听图表相关功能，将在未来版本移除
	
	// < @deprecated 兼容3.0.1版本的API，将在未来版本移除，请使用dashboardBase.originalDataIndex()函数
	/**
	 * 获取指定数据对象的原始信息，这些信息是由chartBase.originalInfo函数设置过的。
	 * 
	 * @param data 数据对象、数据对象数组，格式为：{ ... }、[ { ... }, ... ]
	 * @param inflate 可选，是否在返回原始信息对象中填充图表对象、原始数据信息，默认值为：true
	 * @returns 原始信息对象(可能为null）、或其数组，格式为：
	 *									{
	 *										//图表ID
	 *										chartId: "...",
	 *										//图表数据集索引数值、数值数组
	 *										chartDataSetIndex: ...,
	 *										//结果数据索引，格式为：
	 *                                      //当chartDataSetIndex不是数组时：
	 *                                      //数值、数值数组
	 *                                      //当chartDataSetIndex是数组时：
	 *                                      //数组（元素可能是数值、数值数组）
	 *										resultDataIndex: ...,
	 *										//当inflate为true时，chartId对应的图表对象
	 *										chart: 图表对象,
	 *										//当inflate为true时，resultDataIndex对应的原始结果数据，格式为：
	 *                                      //当chartDataSetIndex不是数组时：
	 *                                      //对象、对象数组
	 *                                      //当chartDataSetIndex是数组时：
	 *                                      //数组（元素可能是对象、对象数组）
	 *										resultData: 结果数据
	 *									}
	 *									当data是数组时，将返回此结构的数组。
	 */
	dashboardBase.originalInfo = function(data, inflate)
	{
		inflate = (inflate === undefined ? true : inflate);
		var isArray = $.isArray(data);
		
		if(!isArray)
			data = [ data ];
		
		var re = [];
		
		var pname = chartFactory._ORIGINAL_DATA_INDEX_PROP_NAME;
		
		for(var i=0; i<data.length; i++)
		{
			var originalInfo = (data[i] == null ? null : data[i][pname]);
			
			if(inflate && originalInfo != null)
			{
				//不能修改原对象
				originalInfo = $.extend(true, {}, originalInfo);
				var dataSetBindIndex = originalInfo.chartDataSetIndex;
				var resultDataIndex = originalInfo.resultDataIndex;
				
				var chart = this.chartOf(originalInfo.chartId);
				var resultData = undefined;
				
				if(chart != null && dataSetBindIndex != null)
				{
					if($.isArray(dataSetBindIndex))
					{
						resultData = [];
						
						for(var j=0; j<dataSetBindIndex.length; j++)
						{
							var result = chart.resultOf(chart.updateResult(), dataSetBindIndex[j]);
							resultData[j] = chart.resultDataElement(result, (resultDataIndex ? resultDataIndex[j] : null));
						}
					}
					else
					{
						var result = chart.resultOf(chart.updateResult(), dataSetBindIndex);
						resultData = chart.resultDataElement(result, resultDataIndex);
					}
				}
				
				originalInfo.chart = chart;
				originalInfo.resultData = resultData;
			}
			
			re[i] = originalInfo;
		}
		
		return (isArray ? re : re[0]);
	};
	// > @deprecated 兼容3.0.1版本的API，将在未来版本移除，请使用dashboardBase.originalDataIndex()函数
	
	// < @deprecated 兼容2.6.0版本的API，将在未来版本移除，已被私有函数dashboardBase._isWaitForRender取代
	/**
	 * 给定图表是否在等待渲染。
	 * 等待渲染的判断条件：
	 * chart.statusPreRender()为true。
	 * 
	 * @param chart 图表对象
	 */
	dashboardBase.isWaitForRender = function(chart)
	{
		return this._isWaitForRender(chart);
	};
	// > @deprecated 兼容2.6.0版本的API，将在未来版本移除，已被私有函数dashboardBase._isWaitForRender取代
	
	// < @deprecated 兼容2.6.0版本的API，将在未来版本移除，已被私有函数dashboardBase._isWaitForUpdate取代
	/**
	 * 给定图表是否在等待更新数据。
	 * 
	 * @param chart 图表对象
	 * @param currentTime 可选，当前时间毫秒数，默认取当前时间
	 */
	dashboardBase.isWaitForUpdate = function(chart, currentTime)
	{
		return this._isWaitForUpdate(chart, currentTime);
	};
	// > @deprecated 兼容2.6.0版本的API，将在未来版本移除，已被私有函数dashboardBase._isWaitForUpdate取代
	
	// < @deprecated 兼容2.3.0版本的API，将在未来版本移除，已被dashboardBase.chartOf取代
	/**
	 * 获取图表，没有则返回undefined。
	 * 
	 * @param chartInfo 图表标识信息：图表Jquery对象、图表HTML元素、图表HTML元素ID、图表对象、图表ID、图表索引数值
	 */
	dashboardBase.getChart = function(chartInfo)
	{
		return this.chartOf(chartInfo);
	};
	// > @deprecated 兼容2.3.0版本的API，将在未来版本移除，已被dashboardBase.chartOf取代
	
	// < @deprecated 兼容2.3.0版本的API，将在未来版本移除，已被dashboardBase.charts取代
	/**
	 * 获取所有图表数组。
	 */
	dashboardBase.getAllCharts = function()
	{
		return this.charts;
	};
	// > @deprecated 兼容2.3.0版本的API，将在未来版本移除，已被dashboardBase.charts取代
	
	// < @deprecated 兼容2.3.0版本的API，将在未来版本移除，已被dashboardBase.chartIndex取代
	/**
	 * 获取图表索引号。
	 * 
	 * @param chartInfo 图表标识信息：图表Jquery对象、图表HTML元素、图表HTML元素ID、图表对象、图表ID、图表索引数值
	 */
	dashboardBase.getChartIndex = function(chartInfo)
	{
		return this.chartIndex(chartInfo);
	};
	// > @deprecated 兼容2.3.0版本的API，将在未来版本移除，已被dashboardBase.chartIndex取代
	
	//-------------
	// > 已弃用函数 end
	//-------------
	
	//----------------------------------------
	// dashboardBase end
	//----------------------------------------
	
	/**
	 * 获取对象的指定属性路径的值。
	 * 
	 * @param obj
	 * @param propertyPath 属性路径，示例：order、order.product、[0].name、order['product'].name
	 * @return 属性路径值，属性路径不存在则返回undefined
	 */
	dashboardFactory.getPropertyPathValue = function(obj, propertyPath)
	{
		if(obj == null)
			return undefined;
		
		var value = undefined;
		
		//简单属性值
		value = obj[propertyPath];
		
		if(value !== undefined)
			return value;
		
		//构建eval表达式
		if(propertyPath.charAt(0) == '[')
			propertyPath = "obj" + propertyPath;
		else
			propertyPath = "obj." + propertyPath;
		
		try
		{
			value = eval(propertyPath);
		}
		catch(e)
		{
			value = undefined;
		}
		
		return value;
	};
	
	/**
	 * 添加内置图表地图集。
	 * 
	 * @param chartMaps 内置图表地图，格式为：[
						{
							//地图名数组
						  	names: ["...", ...],
							//地图文件
							map: "...",
							//可选，行政区划名称
							adname: "...",
							//可选，行政区划编码
							"adcode": "...",
							//可选，上级行政区划编码
							"parent": "..." 
						},
						...]
	 */
	dashboardFactory.addBuiltinChartMaps = function(chartMaps)
	{
		var ukChartMapNames = (dashboardFactory._uniqueBuiltinChartMapNames || (dashboardFactory._uniqueBuiltinChartMapNames = {}));
		
		for(var i=0; i<chartMaps.length; i++)
		{
			var cm = chartMaps[i];
			var names = cm.names;
			var adcodeInNames = (cm.adcode ? false : true);
			
			for(var j=0; j<names.length; j++)
			{
				var name = names[j];
				
				if(ukChartMapNames[name])
					throw new Error("duplicate built-in chart map name : " + name);
				
				ukChartMapNames[name] = true;
				
				if(!adcodeInNames && name == cm.adcode)
					adcodeInNames = true;
			}
			
			if(!adcodeInNames)
				throw new Error("the adcode ["+cm.adcode+"] must be added to [names]");
			
			builtinChartMaps.push(cm);
		}
	};
	
	/**
	 * 获取标准内置图表地图树形结构。
	 * 返回一个数组，其中每个元素都可能是树形结构根节点，节点格式为：
	 * {
	 *   //地图名，可用于chartSupport中的builtinOptionNames.mapName图表选项的名称
	 *   mapName: "...",
	 *   //显示标签
	 *   mapLabel: "...",
	 *   //子节点，为null表示没有
	 *   mapChildren: [ ... ],
	 * }
	 * 
	 * @param listener 可选，节点监听器，格式为：
	 * {
	 *   //节点添加后置处理函数，parent为null表明节点添加到了rootArray中
	 *   added: function(node, parent, rootArray){}
	 * }
	 */
	dashboardFactory.getStdBuiltinChartMapTree = function(listener)
	{
		var re = [];
		
		var nodeCache = {};
		
		for(var i=0; i<builtinChartMaps.length; i++)
		{
			var bcm = builtinChartMaps[i];
			
			if(!bcm.adname || !bcm.adcode)
				continue;
			
			//dashboardFactory.addBuiltinChartMaps()函数已经确保了adcode可以用作地图名
			//而且它是全局唯一的，最合适
			var node = { mapName: bcm.adcode, mapLabel: bcm.adname };
			var parentNode = (bcm.parent ? nodeCache[bcm.parent] : null);
			
			if(parentNode)
			{
				if(!parentNode.mapChildren)
					parentNode.mapChildren = [];
				
				parentNode.mapChildren.push(node);
			}
			else
			{
				re.push(node);
			}
			
			if(listener && listener.added)
				listener.added(node, parentNode, re);
			
			nodeCache[bcm.adcode] = node;
		}
		
		return re;
	};
	
	/**
	 * 获取标准内置图表地图平铺数组。
	 * 返回一个数组，其中元素格式为：
	 * {
	 *   //地图名，可用于chartSupport中的builtinOptionNames.mapName图表选项的名称
	 *   mapName: "...",
	 *   //显示标签
	 *   mapLabel: "..."
	 * }
	 * 
	 * @param listener 可选，节点监听器，格式为：
	 * {
	 *   //节点添加后置处理函数
	 *   added: function(node, rootArray){}
	 * }
	 */
	dashboardFactory.getStdBuiltinChartMapArray = function(listener)
	{
		var re = [];
		
		for(var i=0; i<builtinChartMaps.length; i++)
		{
			var bcm = builtinChartMaps[i];
			
			if(!bcm.adname || !bcm.adcode)
				continue;
			
			//dashboardFactory.addBuiltinChartMaps()函数已经确保了adcode可以用作地图名
			//而且它是全局唯一的，最合适
			var node = { mapName: bcm.adcode, mapLabel: bcm.adname };
			re.push(node);
			
			if(listener && listener.added)
				listener.added(node, re);
		}
		
		return re;
	};
	
	/**
	 * 内置地图JSON地址配置：省级及以上。
	 */
	var dftBuiltinChartMaps =
	[
		{
			"names":["100000","中国","中华人民共和国","china","China"],
			//标准中国地图南海诸岛太占空间，所以采用下面南海诸岛在右侧的中国地图
			//"map" : "100000_full.json"
			"map" : "china_nhzd.json",
			"adname":"中国","adcode":"100000","parent":null
		},
		{"names":["110000","北京市","北京","京","beijing","Beijing"],"map":"110000_full.json","adname":"北京市","adcode":"110000","parent":"100000"},
		{"names":["120000","天津市","天津","津","tianjin","Tianjin"],"map":"120000_full.json","adname":"天津市","adcode":"120000","parent":"100000"},
		{"names":["130000","河北省","河北","冀","hebei","Hebei"],"map":"130000_full.json","adname":"河北省","adcode":"130000","parent":"100000"},
		{"names":["140000","山西省","山西","晋","shanxi","Shanxi"],"map":"140000_full.json","adname":"山西省","adcode":"140000","parent":"100000"},
		{"names":["150000","内蒙古自治区","内蒙古","蒙","neimenggu","Neimenggu"],"map":"150000_full.json","adname":"内蒙古自治区","adcode":"150000","parent":"100000"},
		{"names":["210000","辽宁省","辽宁","辽","liaoning","Liaoning"],"map":"210000_full.json","adname":"辽宁省","adcode":"210000","parent":"100000"},
		{"names":["220000","吉林省","吉林","吉","jilin","Jilin"],"map":"220000_full.json","adname":"吉林省","adcode":"220000","parent":"100000"},
		{"names":["230000","黑龙江省","黑龙江","黑","heilongjiang","Heilongjiang"],"map":"230000_full.json","adname":"黑龙江省","adcode":"230000","parent":"100000"},
		{"names":["310000","上海市","上海","沪","shanghai","Shanghai"],"map":"310000_full.json","adname":"上海市","adcode":"310000","parent":"100000"},
		{"names":["320000","江苏省","江苏","苏","jiangsu","Jiangsu"],"map":"320000_full.json","adname":"江苏省","adcode":"320000","parent":"100000"},
		{"names":["330000","浙江省","浙江","浙","zhejiang","Zhejiang"],"map":"330000_full.json","adname":"浙江省","adcode":"330000","parent":"100000"},
		{"names":["340000","安徽省","安徽","皖","Anhui","anhui"],"map":"340000_full.json","adname":"安徽省","adcode":"340000","parent":"100000"},
		{"names":["350000","福建省","福建","闽","fujian","Fujian"],"map":"350000_full.json","adname":"福建省","adcode":"350000","parent":"100000"},
		{"names":["360000","江西省","江西","赣","jiangxi","Jiangxi"],"map":"360000_full.json","adname":"江西省","adcode":"360000","parent":"100000"},
		{"names":["370000","山东省","山东","鲁","shandong","Shandong"],"map":"370000_full.json","adname":"山东省","adcode":"370000","parent":"100000"},
		{"names":["410000","河南省","河南","豫","henan","Henan"],"map":"410000_full.json","adname":"河南省","adcode":"410000","parent":"100000"},
		{"names":["420000","湖北省","湖北","鄂","hubei","Hubei"],"map":"420000_full.json","adname":"湖北省","adcode":"420000","parent":"100000"},
		{"names":["430000","湖南省","湖南","湘","hunan","Hunan"],"map":"430000_full.json","adname":"湖南省","adcode":"430000","parent":"100000"},
		{"names":["440000","广东省","广东","粤","guangdong","Guangdong"],"map":"440000_full.json","adname":"广东省","adcode":"440000","parent":"100000"},
		{"names":["450000","广西壮族自治区","广西","桂","guangxi","Guangxi"],"map":"450000_full.json","adname":"广西壮族自治区","adcode":"450000","parent":"100000"},
		{"names":["460000","海南省","海南","琼","hainan","Hainan"],"map":"460000_full.json","adname":"海南省","adcode":"460000","parent":"100000"},
		{"names":["500000","重庆市","重庆","渝","chongqing","Chongqing"],"map":"500000_full.json","adname":"重庆市","adcode":"500000","parent":"100000"},
		{"names":["510000","四川省","四川","川","sichuan","Sichuan"],"map":"510000_full.json","adname":"四川省","adcode":"510000","parent":"100000"},
		{"names":["520000","贵州省","贵州","黔","guizhou","Guizhou"],"map":"520000_full.json","adname":"贵州省","adcode":"520000","parent":"100000"},
		{"names":["530000","云南省","云南","滇","yunnan","Yunnan"],"map":"530000_full.json","adname":"云南省","adcode":"530000","parent":"100000"},
		{"names":["540000","西藏自治区","西藏","藏","xizang","Xizang"],"map":"540000_full.json","adname":"西藏自治区","adcode":"540000","parent":"100000"},
		{"names":["610000","陕西省","陕西","陕","shanxi1","shaanxi","Shaanxi"],"map":"610000_full.json","adname":"陕西省","adcode":"610000","parent":"100000"},
		{"names":["620000","甘肃省","甘肃","甘","gansu","Gansu"],"map":"620000_full.json","adname":"甘肃省","adcode":"620000","parent":"100000"},
		{"names":["630000","青海省","青海","青","qinghai","Qinghai"],"map":"630000_full.json","adname":"青海省","adcode":"630000","parent":"100000"},
		{"names":["640000","宁夏回族自治区","宁夏","宁","ningxia","Ningxia"],"map":"640000_full.json","adname":"宁夏回族自治区","adcode":"640000","parent":"100000"},
		{"names":["650000","新疆维吾尔自治区","新疆","新","xinjiang","Xinjiang"],"map":"650000_full.json","adname":"新疆维吾尔自治区","adcode":"650000","parent":"100000"},
		{"names":["710000","台湾省","台湾","taiwan","Taiwan"],"map":"710000.json","adname":"台湾省","adcode":"710000","parent":"100000"},
		{"names":["810000","香港特别行政区","香港","港","xianggang","Xianggang","HongKong","Hongkong"],"map":"810000_full.json","adname":"香港特别行政区","adcode":"810000","parent":"100000"},
		{"names":["820000","澳门特别行政区","澳门","澳","aomen","Aomen","Macao"],"map":"820000_full.json","adname":"澳门特别行政区","adcode":"820000","parent":"100000"}
		
		//世界地图
		,
		{"names":["ext-world","world", "世界"],"map":"world.json","adname":"世界","adcode":"ext-world","parent":null},
		
		//旧版遗留地图
		{"names":["ext-china-contour","china-contour", "中国轮廓"],"map":"china-contour.json"},
		{"names":["ext-china-cities","china-cities", "中国城市"],"map":"china-cities.json"}
	];
	
	dashboardFactory.addBuiltinChartMaps(dftBuiltinChartMaps);
})
(this, window);
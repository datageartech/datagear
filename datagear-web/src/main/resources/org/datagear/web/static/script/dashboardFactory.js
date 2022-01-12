/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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
 * 此看板工厂扩展了图表监听器功能，支持为图表监听器添加图表更新数据出错处理函数：{ updateError: function(chart, error){ ... } }
 * 
 * 此看板工厂支持将页面内添加了elementAttrConst.DASHBOARD_FORM属性的<form>元素构建为看板表单，具体参考dashboardBase._renderForms函数说明。
 * 
 */
(function(global)
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
	
	//----------------------------------------
	// chartStatusConst开始
	//----------------------------------------
	
	/**图表状态：需要参数值*/
	chartStatusConst.PARAM_VALUE_REQUIRED = "PARAM_VALUE_REQUIRED";
	
	/**图表状态：渲染出错*/
	chartStatusConst.RENDER_ERROR = "RENDER_ERROR";
	
	/**图表状态：更新出错*/
	chartStatusConst.UPDATE_ERROR = "UPDATE_ERROR";
	
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
	
	//----------------------------------------
	// elementAttrConst结束
	//----------------------------------------
	
	//----------------------------------------
	// renderContextAttrConst开始
	//----------------------------------------
	
	//可选，看板主题，同：
	//AbstractDataAnalysisController.DASHBOARD_BUILTIN_RENDER_CONTEXT_ATTR_DASHBOARD_THEME
	renderContextAttrConst.dashboardTheme = "DG_DASHBOARD_THEME";
	
	//可选，看板主题，同：
	//AbstractDataAnalysisController.DASHBOARD_BUILTIN_RENDER_CONTEXT_ATTR_USER
	renderContextAttrConst.user = "DG_USER";
	
	//----------------------------------------
	// renderContextAttrConst结束
	//----------------------------------------
	
	//----------------------------------------
	// dashboardStatusConst开始
	//----------------------------------------
	
	/**看板状态：完成init*/
	dashboardStatusConst.INITED = "INITED";
	
	/**看板状态：正在render*/
	dashboardStatusConst.RENDERING = "RENDERING";
	
	/**看板状态：完成render*/
	dashboardStatusConst.RENDERED = "RENDERED";
	
	/**看板状态：完成render*/
	dashboardStatusConst.RENDERED = "RENDERED";
	
	/**看板状态：已销毁*/
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
				//org.datagear.web.controller.DashboardController.LOAD_CHART_PARAM_DASHBOARD_ID
				dashboardIdParamName: "dashboardId",
				//org.datagear.web.controller.DashboardController.LOAD_CHART_PARAM_CHART_WIDGET_ID
				chartWidgetIdParamName: "chartWidgetId"
			});
	
	/**
	 * 更新图表数据ajax请求的重试秒数，当更新图表数据ajax请求出错后，会在过这些秒后重试请求。
	 */
	dashboardFactory.UPDATE_AJAX_RETRY_SECONDS = 5;
	
	/**
	 * 循环监视处理图表状态间隔毫秒数。
	 */
	dashboardFactory.HANDLE_CHART_INTERVAL_MS = 1;
	
	/**
	 * 浏览器初始化到此看板工厂JS的时间戳。
	 */
	dashboardFactory.LOAD_TIME = new Date().getTime();
	
	/**
	 * 初始化原始看板对象，为其添加看板API，可选调用dashboard.init()函数。
	 * 
	 * @param dashboard 看板对象，格式应为：
	 *				{
	 *				  //唯一ID
	 *				  id: "...",
	 *				  //渲染上下文
	 *				  renderContext: {...},
	 *				  //可选，图表元信息，参考chartFactory.Chart函数的chartMeta参数说明
	 *				  charts: [ 图表元信息, ... ]
	 *				}
	 *				另参考：org.datagear.analysis.Dashboard
	 * @param instanceInit 可选，是否调用dashboard.init()函数，默认为：true
	 */
	dashboardFactory.init = function(dashboard, instanceInit)
	{
		instanceInit = (instanceInit == null ? true : instanceInit);
		
		dashboardFactory._initOverwriteChartBaseIfNot();
		dashboardFactory._initStartHeartBeatIfNot(dashboard.renderContext);
		
		$.extend(dashboard, this.dashboardBase);
		
		var charts = (dashboard.charts || []);
		for(var i=0; i<charts.length; i++)
			chartFactory.init(charts[i]);
		
		if(instanceInit)
			dashboard.init();
	};
	
	dashboardFactory._initOverwriteChartBaseIfNot = function()
	{
		//此方法不能重复执行，这里确保只执行一次
		if(chartBase._initSuper != null)
			return false;
		
		chartBase._initSuper = chartBase.init;
		chartBase._postProcessRenderedSuper = chartBase._postProcessRendered;
		
		chartBase.init = function()
		{
			this._initLinks();
			this._initAutoResize();
			this._initUpdateGroup();
			this._initSuper();
		};
		
		chartBase._postProcessRendered = function()
		{
			this.bindLinksEventHanders(this.links());
			this._postProcessRenderedSuper();
		};
	};
	
	dashboardFactory._initStartHeartBeatIfNot = function(renderContext)
	{
		if(dashboardFactory._initStartHeartBeat)
			return;
		
		//开启心跳，避免会话超时
		var webContext = chartFactory.renderContextAttrWebContext(renderContext);
		var heartbeatURL = chartFactory.toWebContextPathURL(webContext, webContext.attributes.heartbeatURL);
		this.startHeartBeat(heartbeatURL);
		
		dashboardFactory._initStartHeartBeat = true;
	};
	
	/**
	 * 开始执行心跳请求。
	 * @param heartbeatURL 心跳URL，可选，初次调用时需设置
	 */
	dashboardFactory.startHeartBeat = function(heartbeatURL)
	{
		if(this._heartbeatStatus == "run")
			return false;
		
		this._heartbeatStatus = "run";
		
		this.heartbeatURL = (heartbeatURL == undefined ? this.heartbeatURL : heartbeatURL);
		this._heartBeatAjaxRequestTimeout();
		
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
	
	dashboardFactory._heartBeatAjaxRequestTimeout = function()
	{
		var interval = (this.heartbeatInterval || 1000*60*5);
		
		var _thisFactory = this;
		
		this._heartbeatTimeoutId = setTimeout(function()
		{
			if(_thisFactory._heartbeatStatus == "run")
			{
				var url = _thisFactory.heartbeatURL;
				
				if(url == null)
					throw new Error("[dashboardFactory.heartbeatURL] must be set");
				
				$.ajax({
					type : "GET",
					cache: false,
					url : url,
					complete : function()
					{
						if(_thisFactory._heartbeatStatus == "run")
							_thisFactory._heartBeatAjaxRequestTimeout();
					}
				});
			}
		},
		interval);
	};
	
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
		
		if(!links)
			return;
		
		links = chartFactory.evalSilently(links);
		
		if(!links)
			return;
		
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
	 * 图表数据集参数索引对象格式参考dashboardBase.batchSetDataSetParamValues函数相关说明，
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
			
			var myTargetCharts = dashboard.batchSetDataSetParamValues(batchSource, link, chartEvent);
			
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
	 * 从服务端获取并刷新图表数据。
	 */
	chartBase.refreshData = function()
	{
		this._assertActive();
		
		if(!this.isDataSetParamValueReady())
			chartFactory.logException("Chart '"+this.elementId+"' has required but unset data set param value");
		
		//不能使用如下方式实现，当在A图表监听器的update函数中调用参数化B图表的refreshData()时，
		//可能会出现已设置的statusPreUpdate()状态被PARAM_VALUE_REQUIRED状态覆盖的情况，
		//而导致refreshData()失效
		//this._updateAjaxErrorTime(null);
		//this.statusPreUpdate(true);
		
		this._inRequestRefreshData(true);
	};
	
	chartBase._updateTime = function(time)
	{
		return chartFactory.extValueBuiltin(this, "updateTime", time);
	};
	
	chartBase._inUpdateAjax = function(inAjax)
	{
		return chartFactory.extValueBuiltin(this, "inUpdateAjax", inAjax);
	};
	
	chartBase._updateAjaxErrorTime = function(time)
	{
		return chartFactory.extValueBuiltin(this, "updateAjaxErrorTime", time);
	};
	
	chartBase._inUpdateAjaxErrorTime = function(time)
	{
		var errorTime = this._updateAjaxErrorTime();
		
		if(errorTime == null)
			return false;
		
		return ((time - errorTime) <= dashboardFactory.UPDATE_AJAX_RETRY_SECONDS*1000);
	};
	
	chartBase._inRequestRefreshData = function(inRequest)
	{
		return chartFactory.extValueBuiltin(this, "inRequestRefreshData", inRequest);
	};
	
	//----------------------------------------
	// chartBase扩展结束
	//----------------------------------------
	
	
	//----------------------------------------
	// dashboardBase start
	//----------------------------------------
	
	/**
	 * 初始化看板。
	 * 
	 * 此函数在看板生命周期内仅允许调用一次，在看板destroy后可重新调用。
	 * 
	 * 看板生命周期：
	 * dashboard.init()     初始化
	 * dashboard.render()   渲染
	 * dashboard.destroy()  销毁
	 */
	dashboardBase.init = function()
	{
		if(!this.id)
			throw new Error("[dashboard.id] required");
		if(!this.renderContext)
			throw new Error("[dashboard.renderContext] required");
		
		if(this._isInitialized())
			throw new Error("Dashboard is illegal state for init");
		
		this.charts = (this.charts || []);
		
		this._initRenderContext();
		this._initListener();
		this._initMapURLs();
		this._initChartResizeHandler();
		this._initCharts();
		
		this._status = dashboardStatusConst.INITED;
	};
	
	/**
	 * 看板是否已完成初始化但未销毁。
	 */
	dashboardBase._isInitialized = function()
	{
		return (this._status == dashboardStatusConst.INITED
				 || this._status == dashboardStatusConst.RENDERING
				 || this._status == dashboardStatusConst.RENDERED);
	};
	
	/**
	 * 校验看板已完成初始化。
	 */
	dashboardBase._assertInitialized = function()
	{
		if(!this._isInitialized())
			throw new Error("Dashboard not initialized");
	};
	
	/**
	 * 校验看板已完成render。
	 */
	dashboardBase._assertRendered = function()
	{
		if(this._status != dashboardStatusConst.RENDERED)
			throw new Error("Dashboard not rendered");
	};
	
	/**
	 * 初始化渲染上下文。
	 */
	dashboardBase._initRenderContext = function()
	{
		var dashboardTheme = this.renderContextAttr(renderContextAttrConst.dashboardTheme);
		var webContext = chartFactory.renderContextAttrWebContext(this.renderContext);
		var chartTheme = (dashboardTheme ? dashboardTheme.chartTheme : null);
		
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
		var builtinChartMaps = dashboardFactory.builtinChartMaps;
		var builtinChartMapBaseURL = dashboardFactory.builtinChartMapBaseURL;
		
		var mapURLs = {};
		
		for(var i=0; i<builtinChartMaps.length; i++)
		{
			var urlNames = builtinChartMaps[i];
			for(var j=0; j<urlNames.names.length; j++)
				mapURLs[urlNames.names[j]] = builtinChartMapBaseURL + urlNames.url;
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
		
		if(listener)
			this.listener(listener);
	};
	
	/**
	 * 初始化看板的所有图表。
	 */
	dashboardBase._initCharts = function()
	{
		for(var i=0; i<this.charts.length; i++)
			this._initChart(this.charts[i]);
	};
	
	/**
	 * 初始化看板的单个图表。
	 */
	dashboardBase._initChart = function(chart)
	{
		chart.renderContext = this.renderContext;
		chart.dashboard = this;
		
		chart.init();
		
		var chartListener = chart.listener();
		
		//图表监听器不继承看板监听器功能，所有只有图表没有定义监听器时，才使用代理看板监听器
		if(!chartListener)
		{
			chart.listener(this._getDelegateChartListener());
		}
		else
		{
			//由元素图表监听器属性生成的内部代理图表监听器，应为其添加updateError处理函数
			if(chartListener._proxyChartListenerFromEleAttr)
			{
				chartListener.updateError = function(chart, error)
				{
					var dl = this._findListenerOfFunc("updateError");
					
					if(dl)
						return dl.updateError(chart, error);
				};
			}
		}
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
				var charts = thisDashboard.charts;
				
				for(var i =0; i<charts.length; i++)
				{
					var chart = charts[i];
					
					if(chart.autoResize() && chart.isActive())
						chart.resize();
				}
			},
			100);
		};
		
		$window.on("resize", this._windowResizeHandler);
	};
	
	/**
	 * 获取/设置初始看板监听器。
	 * 看板监听器格式为：
	 * {
	 *   //可选，渲染看板完成回调函数
	 *   render: function(dashboard){ ... },
	 *   //可选，渲染图表完成回调函数
	 *   renderChart: function(dashboard, chart){ ... },
	 *   //可选，更新图表数据完成回调函数
	 *   updateChart: function(dashboard, chart, results){ ... },
	 *   //可选，渲染看板前置回调函数，返回false将阻止渲染看板
	 *   onRender: function(dashboard){ ... },
	 *   //可选，渲染图表前置回调函数，返回false将阻止渲染图表
	 *   onRenderChart: function(dashboard, chart){ ... },
	 *   //可选，更新图表数据前置回调函数，返回false将阻止更新图表数据
	 *   onUpdateChart: function(dashboard, chart, results){ ... },
	 *   //可选，更新图表数据出错处理函数
	 *   updateChartError: function(dashboard, chart, error){ ... }
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
		
		//需要同步设置全局图表监听器
		var chartListener = this._getDelegateChartListener();
		
		var dashboard = this;
		
		if(listener && listener.renderChart)
			chartListener.render = function(chart){ listener.renderChart(dashboard, chart); };
		else
			chartListener.render = undefined;
		
		if(listener && listener.updateChart)
			chartListener.update = function(chart, results){ listener.updateChart(dashboard, chart, results); };
		else
			chartListener.update = undefined;
		
		if(listener && listener.onRenderChart)
			chartListener.onRender = function(chart){ return listener.onRenderChart(dashboard, chart); };
		else
			chartListener.onRender = undefined;
		
		if(listener && listener.onUpdateChart)
			chartListener.onUpdate = function(chart, results){ return listener.onUpdateChart(dashboard, chart, results); };
		else
			chartListener.onUpdate = undefined;
		
		if(listener && listener.updateChartError)
			chartListener.updateError = function(chart, error){ return listener.updateChartError(dashboard, chart, error); };
		else
			chartListener.updateError = undefined;
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
	 * 获取看板的代理图表监听器。
	 * 为了确保任意时刻设置看板监听器（dashboard.listener(...)）都能传递至图表，所以此方法应始终返回不为null且引用不变的对象。
	 */
	dashboardBase._getDelegateChartListener = function()
	{
		var chartListener = (this._delegateChartListener || (this._delegateChartListener = {}));
		return chartListener;
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
	 * @param doDestory 选填参数，是否销毁图表，默认为true
	 * @return 移除的图表对象，或者图表未找到时为undefined
	 */
	dashboardBase.removeChart = function(chartInfo, doDestory)
	{
		var newCharts = [].concat(this.charts);
		var index = this._chartIndex(newCharts, chartInfo);
		
		if(index < 0)
			return undefined;
		
		var removeds = newCharts.splice(index, 1);
		this.charts = newCharts;
		
		if(doDestory != false)
			this._destroyChart(removeds[0]);
		
		return removeds[0];
	};
	
	/**
	 * 重新调整指定图表尺寸。
	 * 
	 * @param chartInfo 图表标识信息：图表Jquery对象、图表HTML元素、图表HTML元素ID、图表对象、图表ID、图表索引数值
	 */
	dashboardBase.resizeChart = function(chartInfo)
	{
		this._assertRendered();
		
		var chart = this.chartOf(chartInfo);
		chart.resize();
	};
	
	/**
	 * 重新调整所有图表尺寸。
	 */
	dashboardBase.resizeAllCharts = function()
	{
		this._assertRendered();
		
		for(var i=0; i<this.charts.length; i++)
			this.charts[i].resize();
	};
	
	/**
	 * 刷新图表数据。
	 * 
	 * @param chartInfo 图表标识信息：图表Jquery对象、图表HTML元素、图表HTML元素ID、图表对象、图表ID、图表索引数值
	 */
	dashboardBase.refreshData = function(chartInfo)
	{
		this._assertRendered();
		
		var chart = this.chartOf(chartInfo);
		chart.refreshData();
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
	 * 图表数据集参数索引对象格式参考dashboardBase.batchSetDataSetParamValues函数相关说明，其中value函数的sourceValueContext参数为：表单数据对象、表单HTML元素。
	 * 
	 * @param form 要渲染的<form>表单元素、Jquery对象，表单结构允许灵活自定义，具体参考chartSetting.renderDataSetParamValueForm
	 * @param config 可选，表单配置对象，默认为表单元素的elementAttrConst.DASHBOARD_FORM属性值
	 */
	dashboardBase.renderForm = function(form, config)
	{
		this._assertInitialized();
		
		form = $(form);
		
		form.addClass("dg-dashboard-form");
		
		if(!config)
			config = chartFactory.evalSilently(form.attr(elementAttrConst.DASHBOARD_FORM), {});
		
		var dashboard = this;
		var bindBatchSetName = "dataGearBatchSet";
		
		config = $.extend(
		{
			submit: function(formData)
			{
				var thisForm = this;
				var batchSet = $(thisForm).data(bindBatchSetName);
				
				if(batchSet)
				{
					var charts = dashboard.batchSetDataSetParamValues(formData, batchSet, [ formData, thisForm ]);
					
					for(var i=0; i<charts.length; i++)
						charts[i].refreshData();
				}
			}
		},
		config);
		
		//构建用于批量设置数据集参数值的对象
		var batchSet = undefined;
		if(config.link)
		{
			var link = config.link;
			
			//转换简写格式
			if(typeof(link) == "string" || $.isArray(link))
				link = { target: link };
			
			batchSet =
			{
				target: link.target,
				//新构建data对象，因为可能会在下面被修改
				data: (link.data ? $.extend({}, link.data) : {})
			};
		}
		
		var items = [];
		var defaultValues = {};
		
		var sourceItems = (config.items || []);
		if(!$.isArray(sourceItems))
			sourceItems = [ sourceItems ];
		
		for(var i=0; i<sourceItems.length; i++)
		{
			var item = sourceItems[i];
			
			if(typeof(item) == "string")
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
			if(item.link != null && batchSet && batchSet.data)
				batchSet.data[item.name] = item.link;
		}
		
		if(batchSet)
			form.data(bindBatchSetName, batchSet);
		
		config.paramValues = defaultValues;
		config.chartTheme = this.renderContextAttr(renderContextAttrConst.chartTheme);
		
		chartFactory.chartSetting.renderDataSetParamValueForm(form, items, config);
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
	 * 如果看板还未执行初始化，此函数内部会先执行dashboard.init()函数，确保生命周期完整。
	 *
	 * 此函数在看板生命周期内仅允许调用一次，在看板destroy后可重新调用。 
	 */
	dashboardBase.render = function()
	{
		if(this._status == dashboardStatusConst.RENDERING
				|| this._status == dashboardStatusConst.RENDERED)
		{
			throw new Error("Dashboard is illegal state for render");
		}
		
		//如果还未执行初始化，则应先执行，确保生命周期完整
		if(this._status != dashboardStatusConst.INITED)
			this.init();
		
		this._status = dashboardStatusConst.RENDERING;
		
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
	 * 执行看板渲染。
	 */
	dashboardBase.doRender = function()
	{
		if(this._status != dashboardStatusConst.RENDERING)
			throw new Error("Dashboard is illegal state for doRender");
		
		this._renderForms();
		this.startHandleCharts();
		
		this._status = dashboardStatusConst.RENDERED;
		
		var listener = this.listener();
		if(listener && listener.render)
			  listener.render(this);
	};
	
	/**
	 * 渲染你看板表单。
	 * 它将看板页面内的所有<form dg-dashboard-form="...">元素渲染为看板表单。
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
		this._assertInitialized();
		
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
		var time = new Date().getTime();
		
		for(var i=0; i<charts.length; i++)
		{
			var chart = charts[i];
			
			if(this._isWaitForUpdate(chart, time))
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
		
		var webContext = chartFactory.renderContextAttrWebContext(this.renderContext);
		var url = chartFactory.toWebContextPathURL(webContext, webContext.attributes.updateDashboardURL);
		
		for(var group in preUpdateGroups)
		{
			this._doHandleChartsAjax(url, preUpdateGroups[group]);
		}
		
		var dashboard = this;
		setTimeout(function()
		{
			dashboard._doHandleCharts();
		},
		dashboardFactory.HANDLE_CHART_INTERVAL_MS);
	};
	
	dashboardBase._doHandleChartsAjax = function(url, preUpdateCharts)
	{
		if(!preUpdateCharts || preUpdateCharts.length == 0)
			return;
		
		var data = this._buildUpdateDashboardAjaxData(preUpdateCharts);
		var dashboard = this;
		
		dashboard._setInUpdateAjax(preUpdateCharts, true);
		
		$.ajax({
			contentType : "application/json",
			type : "POST",
			url : url,
			data : JSON.stringify(data),
			success : function(dashboardResult)
			{
				var chartResults = (dashboardResult.chartResults || {});
				var chartResultErrorMessages = (dashboardResult.chartResultErrorMessages || {});
				
				// < @deprecated 用于兼容1.10.1版本的DataSetResult.datas结构，未来版本会移除
				if(chartResults)
				{
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
				}
				//> @deprecated 用于兼容1.10.1版本的DataSetResult.datas结构，未来版本会移除
				
				var updateTime = new Date().getTime();
				
				dashboard._updateCharts(chartResults);
				dashboard._handleChartResultErrors(chartResultErrorMessages);
				
				dashboard._setUpdateTime(preUpdateCharts, updateTime);				
				dashboard._setInRequestRefreshData(preUpdateCharts, false);
				dashboard._setInUpdateAjax(preUpdateCharts, false);
			},
			error : function()
			{
				var updateTime = new Date().getTime();
				
				dashboard._setUpdateTime(preUpdateCharts, updateTime);
				dashboard._setUpdateAjaxErrorTime(preUpdateCharts, updateTime);
				dashboard._setInRequestRefreshData(preUpdateCharts, false);
				dashboard._setInUpdateAjax(preUpdateCharts, false);
			}
		});
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
	 */
	dashboardBase._isWaitForUpdate = function(chart, currentTime)
	{
		var wait = false;
		
		if(currentTime == null)
			currentTime = new Date().getTime();
		
		//图表正处于更新数据ajax中
		if(chart._inUpdateAjax())
		{
			wait = false;
		}
		else if(chart._inRequestRefreshData())
		{
			wait = true;
		}
		//图表更新ajax请求出错后，应等待一段时间后再尝试，避免频繁发送ajax请求
		else if(chart._inUpdateAjaxErrorTime(currentTime))
		{
			wait = false;
		}
		else if(chart.statusRendered() || chart.statusPreUpdate())
		{
			wait = true;
		}
		else if(chart.updateInterval > -1
					&& (chart.statusUpdated() || chart.status() == chartStatusConst.UPDATE_ERROR))
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
			//wait为true时，图表状态可能并不符合chart.update()要求（比如chartStatusConst.UPDATE_ERROR），
			//所以这里需要校验设置
			if(!chart.statusRendered() && !chart.statusPreUpdate() && !chart.statusUpdated())
				chart.statusPreUpdate(true);
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
			this._doRenderChart(chart);
		}
		catch(e)
		{
			//设置为渲染出错状态，避免渲染失败后会_doHandleCharts中会无限尝试渲染
			chart.status(chartStatusConst.RENDER_ERROR);
			
			chartFactory.logException(e);
		}
	};
	
	/**
	 * 执行渲染指定图表。
	 * 
	 * @param chart 图表对象
	 */
	dashboardBase._doRenderChart = function(chart)
	{
		return chart.render();
	};
	
	/**
	 * 处理看板图表结果错误。
	 * 
	 * @param chartResultErrorMessages [图表ID-图表结果错误]映射表
	 */
	dashboardBase._handleChartResultErrors = function(chartResultErrorMessages)
	{
		if(!chartResultErrorMessages)
			return;
		
		for(var chartId in chartResultErrorMessages)
		{
			var chart = this.chartOf(chartId);
			
			if(!chart)
				continue;
			
			try
			{
				//设置为更新出错状态，避免更新失败后会_doHandleCharts中会无限尝试更新
				chart.status(chartStatusConst.UPDATE_ERROR);
				
				this._handleChartResultError(chart, chartResultErrorMessages[chartId]);
			}
			catch(e)
			{
				chartFactory.logException(e);
			}
		}
	};
	
	/**
	 * 处理看板图表结果错误。
	 * 
	 * @param chart 图表对象
	 * @param chartResultErrorMessage 图表结果错误信息对象
	 */
	dashboardBase._handleChartResultError = function(chart, chartResultErrorMessage)
	{
		var chartListener = chart.listener();
		
		if(chartListener && chartListener.updateError)
		{
			chartListener.updateError(chart, chartResultErrorMessage);
		}
		else
		{
			var errorType = (chartResultErrorMessage ? chartResultErrorMessage.type : "Error");
			var errorMessage = (chartResultErrorMessage ? chartResultErrorMessage.message : "Chart result error");
			
			chartFactory.logException(errorType + " : " + errorMessage);
		}
	};
	
	/**
	 * 更新看板的图表数据。
	 * 
	 * @param chartResults [图表ID-图表结果]映射表
	 */
	dashboardBase._updateCharts = function(chartResults)
	{
		if(!chartResults)
			return;
		
		for(var chartId in chartResults)
		{
			var chart = this.chartOf(chartId);
			
			if(!chart)
				continue;
			
			try
			{
				this._updateChart(chart, chartResults[chartId]);
			}
			catch(e)
			{
				//设置为更新出错状态，避免更新失败后会_doHandleCharts中会无限尝试更新
				chart.status(chartStatusConst.UPDATE_ERROR);
				
				chartFactory.logException(e);
			}
		}
	};
	
	/**
	 * 更新指定图表。
	 * 
	 * @param chart 图表对象
	 * @param chartResult 图表结果对象
	 */
	dashboardBase._updateChart = function(chart, chartResult)
	{
		var dataSetResults = (chartResult ? chartResult.dataSetResults : []);
		
		chart.update(dataSetResults);
	};
	
	dashboardBase._setUpdateTime = function(chart, time)
	{
		try
		{
			chart = ($.isArray(chart) ? chart : [ chart ]);
			
			for(var i=0; i<chart.length; i++)
				chart[i]._updateTime(time);
		}
		catch(e)
		{
			chartFactory.logException(e);
		}
	};
	
	dashboardBase._setInUpdateAjax = function(chart, inAjax)
	{
		try
		{
			chart = ($.isArray(chart) ? chart : [ chart ]);
			
			for(var i=0; i<chart.length; i++)
				chart[i]._inUpdateAjax(inAjax);
		}
		catch(e)
		{
			chartFactory.logException(e);
		}
	};
	
	dashboardBase._setInRequestRefreshData = function(chart, inRequest)
	{
		try
		{
			chart = ($.isArray(chart) ? chart : [ chart ]);
			
			for(var i=0; i<chart.length; i++)
				chart[i]._inRequestRefreshData(inRequest);
		}
		catch(e)
		{
			chartFactory.logException(e);
		}
	};
	
	dashboardBase._setUpdateAjaxErrorTime = function(chart, errorTime)
	{
		try
		{
			chart = ($.isArray(chart) ? chart : [ chart ]);
			
			for(var i=0; i<chart.length; i++)
				chart[i]._updateAjaxErrorTime(errorTime);
		}
		catch(e)
		{
			chartFactory.logException(e);
		}
	};
	
	/**
	 * 构建更新看板的ajax请求数据。
	 */
	dashboardBase._buildUpdateDashboardAjaxData = function(charts)
	{
		var updateDashboardConfig = dashboardFactory.updateDashboardConfig;
		
		var dashboardQueryForm = {};
		var dashboardQuery = { chartQueries: {}, resultDataFormat: this.resultDataFormat(), suppressChartError: true };
		
		dashboardQueryForm[updateDashboardConfig.dashboardIdParamName] = this.id;
		dashboardQueryForm[updateDashboardConfig.dashboardQueryParamName] = dashboardQuery;
		
		if(charts && charts.length)
		{
			for(var i=0; i<charts.length; i++)
			{
				var chart = charts[i];
				var chartId = chart.id;
				
				var chartQuery = { dataSetQueries: [], resultDataFormat: chart.resultDataFormat() };
				
				if(chartQuery.resultDataFormat == null)
					chartQuery.resultDataFormat = this.resultDataFormat();
				
				var chartDataSets = (chart.chartDataSets || []);
				for(var j=0; j<chartDataSets.length; j++)
				{
					var dataSetQuery = (chartDataSets[j].query || {});
					chartQuery.dataSetQueries.push(dataSetQuery);
				}
				
				dashboardQuery.chartQueries[chartId] = chartQuery;
			}
		}
		
		return dashboardQueryForm;
	};
	
	/**
	 * 异步加载单个图表，并将其加入此看板。
	 * 
	 * @param element 用于渲染图表的HTML元素、Jquery对象
	 * @param chartWidgetId 选填参数，要加载的图表部件ID，如果不设置，将从元素的"dg-chart-widget"属性取
	 * @param ajaxOptions 选填参数，参数格式可以是图表加载成功回调函数：function(chart){ ... }，也可以是ajax配置项：{...}。
	 * 					  如果图表加载成功回调函数、ajax配置项的success函数返回false，则这个图表不会加入此看板。
	 */
	dashboardBase.loadChart = function(element, chartWidgetId, ajaxOptions)
	{
		this._assertInitialized();
		
		element = $(element);
		
		if(this._loadingChartElement(element))
			throw new Error("The element is loading chart");
		
		if(this.renderedChart(element) != null)
			throw new Error("The element has been rendered as chart");
		
		//看板中可能存在已初始化但是未渲染的图表，也不应允许异步加载
		if(this.chartOf(element) != null)
			throw new Error("There is a chart for this element");
		
		if(typeof(chartWidgetId) != "string")
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
				dashboard.addChart(chart);
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
	 * @param element 用于渲染图表的HTML元素、HTML元素数组、Jquery对象
	 * @param chartWidgetId 选填参数，要加载的图表部件ID、图表部件ID数组，如果不设置，将从元素的"dg-chart-widget"属性取
	 * @param ajaxOptions 选填参数，参数格式可以是图表数组加载成功回调函数：function(charts){ ... }，也可以是ajax配置项：{...}。
	 * 					  如果图表数组加载成功回调函数、ajax配置项的success函数返回false，则这些图表不会加入此看板。
	 */
	dashboardBase.loadCharts = function(element, chartWidgetId, ajaxOptions)
	{
		this._assertInitialized();
		
		element = $(element);
		
		for(var i=0; i<element.length; i++)
		{
			if(this._loadingChartElement(element[i]))
				throw new Error("The "+i+"-th element is loading chart");
			
			if(this.renderedChart(element[i]) != null)
				throw new Error("The "+i+"-th element has been rendered as chart");
			
			//看板中可能存在已初始化但是未渲染的图表，也不应允许异步加载
			if(this.chartOf(element) != null)
				throw new Error("The is a chart for the "+i+"-th element");
		}
		
		if(typeof(chartWidgetId) != "string" && !$.isArray(chartWidgetId))
		{
			ajaxOptions = chartWidgetId;
			chartWidgetId = null;
		}
		
		if(chartWidgetId == null)
			chartWidgetId = [];
		else if(typeof(chartWidgetId) == "string")
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
					dashboard.addChart(charts[i]);
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
	 * 将页面中所有设置了"dg-chart-widget"属性，且未初始化为图表的HTML元素异步加载为图表。
	 * 如果没有需要加载的元素，将不会执行异步请求。
	 *
	 * @param ajaxOptions 选填参数，参数格式可以是图表数组加载成功回调函数：function(charts){ ... }，也可以是ajax配置项：{...}。
	 * 					  如果图表数组加载成功回调函数、ajax配置项的success函数返回false，则这些图表不会加入此看板。
	 * @return 要异步加载的HTML元素数组
	 */
	dashboardBase.loadUnsolvedCharts = function(ajaxOptions)
	{
		this._assertInitialized();
		
		var unsolved = [];
		
		var allElements = $("["+chartFactory.elementAttrConst.WIDGET+"]");
		var dashboard = this;
		
		allElements.each(function()
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
		
		if(set === undefined)
		{
			return (element.attr("_datagear_loadingChart") == "true");
		}
		else
		{
			element.each(function()
			{
				var $this = $(this);
				
				if(set == true)
					$this.attr("_datagear_loadingChart", "true");
				else
					$this.removeAttr("_datagear_loadingChart");
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
		
		var elementId = element.attr("id");
		if(!elementId)
		{
			elementId = chartFactory.nextElementId();
			element.attr("id", elementId);
		}
		chart.elementId = elementId;
		
		chartFactory.init(chart);
		this._initChart(chart);
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
		var url = chartFactory.toWebContextPathURL(webContext, webContext.attributes.loadChartURL);
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
			data: data
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
	 *   //必选，要设置的目标图表元素ID、图表ID、看板图表数组索引，或者它们的数组
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
	 *   //可选，目标图表在批量设置对象的target数组中的索引数值，默认为：0
	 *   chart: ...,
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
	dashboardBase.batchSetDataSetParamValues = function(sourceData, batchSet, sourceValueContext)
	{
		this._assertInitialized();
		
		sourceValueContext = (sourceValueContext === undefined ? sourceData : sourceValueContext);
		
		var targetCharts = [];
		
		var targets = ($.isArray(batchSet.target) ? batchSet.target : [ batchSet.target ]);
		for(var i=0; i<targets.length; i++)
			targetCharts[i] = this.chartOf(targets[i]);
		
		var map = (batchSet.data || {});
		var hasGetValueFunc = (typeof(sourceData.getValue) == "function");
		
		var sourceValueContextArgs = [ "place-holder-for-source-value" ];
		sourceValueContextArgs = sourceValueContextArgs.concat($.isArray(sourceValueContext) ? sourceValueContext : [ sourceValueContext ]);
		
		for(var name in map)
		{
			var dataValue = (hasGetValueFunc ? sourceData.getValue(name)
					: dashboardFactory.getPropertyPathValue(sourceData, name));
			
			var indexes = map[name];
			if(!$.isArray(indexes))
				indexes = [ indexes ];
			
			for(var i=0; i<indexes.length; i++)
			{
				var indexObj = indexes[i];
				var indexObjType = typeof(indexObj);
				
				var chartIdx = 0;
				var dataSetIdx = 0;
				var param = 0;
				var paramValue = null;
				
				if(indexObjType == "number" || indexObjType == "string")
				{
					param = indexObj;
					paramValue = dataValue;
				}
				else
				{
					chartIdx = (indexObj.chart != null ? indexObj.chart : 0);
					dataSetIdx = (indexObj.dataSet != null ? indexObj.dataSet : 0);
					param = (indexObj.param != null ? indexObj.param : 0);
					
					if(indexObj.value)
					{
						sourceValueContextArgs[0] = dataValue;
						paramValue = indexObj.value.apply(indexObj, sourceValueContextArgs);
					}
					else
						paramValue = dataValue;
				}
				
				targetCharts[chartIdx].dataSetParamValue(dataSetIdx, param, paramValue);
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
		//参考org.datagear.web.controller.DashboardController.SERVERTIME_JS_VAR
		if(global._DataGearServerTime == null)
			throw new Error("Get current server time is not supported");
		
		var cct = new Date().getTime();
		var cst = global._DataGearServerTime + (cct - dashboardFactory.LOAD_TIME);
		
		if(asMillisecond == true)
			return cst;
		
		var csd = new Date();
		csd.setTime(cst);
		
		return csd;
	};
	
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
		
		var pname = chartFactory._DATA_ORIGINAL_INFO_PROP_NAME;
		
		for(var i=0; i<data.length; i++)
		{
			var originalInfo = (data[i] == null ? null : data[i][pname]);
			
			if(inflate && originalInfo != null)
			{
				//不能修改原对象
				originalInfo = $.extend(true, {}, originalInfo);
				var chartDataSetIndex = originalInfo.chartDataSetIndex;
				var resultDataIndex = originalInfo.resultDataIndex;
				
				var chart = this.chartOf(originalInfo.chartId);
				var resultData = undefined;
				
				if(chart != null && chartDataSetIndex != null)
				{
					if($.isArray(chartDataSetIndex))
					{
						resultData = [];
						
						for(var j=0; j<chartDataSetIndex.length; j++)
						{
							var result = chart.resultAt(chart.updateResults(), chartDataSetIndex[j]);
							resultData[j] = chart.resultDataElement(result, (resultDataIndex ? resultDataIndex[j] : null));
						}
					}
					else
					{
						var result = chart.resultAt(chart.updateResults(), chartDataSetIndex);
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
	
	/**
	 * 获取当前用户信息。
	 * 
	 * @returns 用户信息，格式参考：org.datagear.web.controller.AbstractDataAnalysisController.AnalysisUser
	 */
	dashboardBase.user = function()
	{
		var user = this.renderContextAttr(renderContextAttrConst.user);
		
		if(user == null)
			throw new Error("Get user is not supported");
		
		return user;
	};
	
	/**
	 * 销毁看板。
	 * 销毁后，可以重新调用dashboard.init()、dashboard.render()函数。
	 */
	dashboardBase.destroy = function()
	{
		if(this._status == dashboardStatusConst.DESTROYED)
			return;
		
		this._status = dashboardStatusConst.DESTROYED;
		
		this.stopHandleCharts();
		this._destroyCharts();
		this._destroyForms();
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
		
		$forms.each(function()
		{
			chartFactory.chartSetting.destroyDataSetParamValueForm(this);
		});
	};
	
	//-------------
	// < 已弃用函数 start
	//-------------
	
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
	 * 内置地图JSON地址配置。
	 */
	dashboardFactory.builtinChartMapBaseURL = "/static/lib/echarts-map";
	dashboardFactory.builtinChartMaps =
	[
		{names: ["中国", "中华人民共和国", "china", "100000"], url: "/china.json"},
		{names: ["安徽", "安徽省", "anhui", "340000"], url: "/province/anhui.json"},
		{names: ["澳门", "澳门特别行政区", "aomen", "820000"], url: "/province/aomen.json"},
		{names: ["北京", "北京市", "beijing", "110000"], url: "/province/beijing.json"},
		{names: ["重庆", "重庆市", "chongqing", "500000"], url: "/province/chongqing.json"},
		{names: ["福建", "福建省", "fujian", "350000"], url: "/province/fujian.json"},
		{names: ["甘肃", "甘肃省", "gansu", "620000"], url: "/province/gansu.json"},
		{names: ["广东", "广东省", "guangdong", "440000"], url: "/province/guangdong.json"},
		{names: ["广西", "广西壮族自治区", "guangxi", "450000"], url: "/province/guangxi.json"},
		{names: ["贵州", "贵州省", "guizhou", "520000"], url: "/province/guizhou.json"},
		{names: ["海南", "海南省", "hainan", "460000"], url: "/province/hainan.json"},
		{names: ["河北", "河北省", "hebei", "130000"], url: "/province/hebei.json"},
		{names: ["黑龙江", "黑龙江省", "heilongjiang", "230000"], url: "/province/heilongjiang.json"},
		{names: ["河南", "河南省", "henan", "410000"], url: "/province/henan.json"},
		{names: ["湖北", "湖北省", "hubei", "420000"], url: "/province/hubei.json"},
		{names: ["湖南", "湖南省", "hunan", "430000"], url: "/province/hunan.json"},
		{names: ["江苏", "江苏省", "jiangsu", "320000"], url: "/province/jiangsu.json"},
		{names: ["江西", "江西省", "jiangxi", "360000"], url: "/province/jiangxi.json"},
		{names: ["吉林", "吉林省", "jilin", "220000"], url: "/province/jilin.json"},
		{names: ["辽宁", "辽宁省", "liaoning", "210000"], url: "/province/liaoning.json"},
		{names: ["内蒙古", "内蒙古自治区", "neimenggu", "150000"], url: "/province/neimenggu.json"},
		{names: ["宁夏", "宁夏回族自治区", "ningxia", "640000"], url: "/province/ningxia.json"},
		{names: ["青海", "青海省", "qinghai", "630000"], url: "/province/qinghai.json"},
		{names: ["山东", "山东省", "shandong", "370000"], url: "/province/shandong.json"},
		{names: ["上海", "上海市", "shanghai", "310000"], url: "/province/shanghai.json"},
		{names: ["山西", "山西省", "shanxi", "140000"], url: "/province/shanxi.json"},
		{names: ["陕西", "陕西省", "shanxi1", "610000"], url: "/province/shanxi1.json"},
		{names: ["四川", "四川省", "sichuan", "510000"], url: "/province/sichuan.json"},
		{names: ["台湾", "台湾省", "taiwan", "710000"], url: "/province/taiwan.json"},
		{names: ["天津", "天津市", "tianjin", "120000"], url: "/province/tianjin.json"},
		{names: ["香港", "香港特别行政区", "xianggang", "810000"], url: "/province/xianggang.json"},
		{names: ["新疆", "新疆维吾尔自治区", "xinjiang", "650000"], url: "/province/xinjiang.json"},
		{names: ["西藏", "西藏自治区", "xizang", "540000"], url: "/province/xizang.json"},
		{names: ["云南", "云南省", "yunnan", "530000"], url: "/province/yunnan.json"},
		{names: ["浙江", "浙江省", "zhejiang", "330000"], url: "/province/zhejiang.json"},
		
		//旧版遗留地图
		{names: ["中国轮廓", "china-contour"], url: "/china-contour.json"},
		{names: ["中国城市", "china-cities"], url: "/china-cities.json"},
		{names: ["世界", "world"], url: "/world.json"}
	];
})
(this);
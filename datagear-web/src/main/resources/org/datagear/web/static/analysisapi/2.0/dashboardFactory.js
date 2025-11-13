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
 *   chartFactory.js
 * 
 * 运行时依赖:
 *   chartTool.js
 * 
 * 
 * 此看板工厂支持为<body>元素添加elementAttrConst.DASHBOARD_LISTENER属性，用于指定看板监听器JS对象名，
 * 看板监听器格式参考dashboard.listener()函数说明。
 * 
 * 此看板工厂支持为<body>元素添加elementAttrConst.MAP_URLS属性，用于扩展或替换内置地图，格式为：
 * {customMap:'map/custom.json', china: 'map/myChina.json'}
 * 
 * 此看板工厂支持为图表元素添加elementAttrConst.LINK属性，用于设置图表联动，具体格式参考chart.links()函数说明。
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
 * 此看板工厂扩展了图表渲染器格式：
 * {
 *   //图表联动源数据处理函数，默认值为：0
 *   //其中：
 *   //索引数值，表示图表事件处理函数对应索引数值的参数是联动源数据
 *   //图表事件处理函数，表示此函数的返回值是图表联动源数据，返回值格式应为：{ ... }、[ {...}, ... ]
 *   linkDataHander: 索引数值、function(type){ return 索引数值、图表事件处理函数; }
 * }
 * 
 * 此看板工厂支持将页面内添加了elementAttrConst.DASHBOARD_FORM属性的<form>元素构建为看板表单，具体参考dashboard._renderForms()函数说明。
 * 
 */
(function(global, window)
{

/**图表工厂*/
var CF = global.chartFactory;

/**看板工厂*/
var DF = (global.dashboardFactory || (global.dashboardFactory = {}));

/**图表状态常量*/
var chartStatusConst = (CF.chartStatusConst || (CF.chartStatusConst = {}));

/**HTML元素属性常量*/
var elementAttrConst = (CF.elementAttrConst || (CF.elementAttrConst = {}));

/** 图表地图映射表，详细格式参考CF.chartMapURLs */
var chartMapURLs = (CF.chartMapURLs || (CF.chartMapURLs = {}));

/** 渲染上下文属性名常量 */
var renderContextAttrConst = (CF.renderContextAttrConst || (CF.renderContextAttrConst = {}));

/**看板状态常量*/
var dashboardStatusConst = (DF.dashboardStatusConst || (DF.dashboardStatusConst = {}));

/** 内置地图 */
var builtinChartMaps = (DF.builtinChartMaps || (DF.builtinChartMaps = []));

var builtinChartMapBaseURL = (DF.builtinChartMapBaseURL || (DF.builtinChartMapBaseURL = "/static/analysislib/geojson/"));

/** 看板版本常量，参考：org.datagear.web.analysis.DashboardApiVersion */
var apiVersion = (dashboardFactory.apiVersion || (dashboardFactory.apiVersion = { V1: "1.0", V2: "2.0" }));

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
DF.updateDashboardConfig = (DF.updateDashboardConfig ||
		{
			dashboardIdParamName: "dashboardId",
			dashboardQueryParamName: "dashboardQuery",
		});

/**
 * 异步加载图表配置，需与后台保持一致。
 */
DF.loadChartConfig = (DF.loadChartConfig ||
		{
			//org.datagear.web.controller.DashboardVisualController.LOAD_CHART_PARAM_DASHBOARD_ID
			dashboardIdParamName: "dashboardId",
			//org.datagear.web.controller.DashboardVisualController.LOAD_CHART_PARAM_CHART_WIDGET_ID
			chartWidgetIdParamName: "chartWidgetId"
		});

/**
 * 心跳配置，需与后台保持一致。
 */
DF.heartbeatConfig = (DF.heartbeatConfig ||
		{
			//org.datagear.web.controller.DashboardVisualController.HEARTBEAT_PARAM_DASHBOARD_ID
			dashboardIdParamName: "dashboardId",
			//org.datagear.web.controller.AbstractDataAnalysisController.HEARTBEAT_INTERVAL_MS
			interval: 1000 * 60 * 5
		});

/**
 * 卸载配置，需与后台保持一致。
 */
DF.unloadConfig = (DF.unloadConfig ||
		{
			//org.datagear.web.controller.AbstractDataAnalysisController.DASHBOARD_UNLOAD_URL_NAME
			urlAttrName: "unloadURL",
			//org.datagear.web.controller.DashboardVisualController.UNLOAD_PARAM_DASHBOARD_ID
			dashboardIdParamName: "dashboardId"
		});

/**
 * 循环监视处理图表状态间隔毫秒数。
 */
DF.HANDLE_CHART_INTERVAL_MS = 10;

/**
 * 自动调整图表尺寸延迟毫秒数。
 */
DF.RESIZE_CHART_TIMEOUT_MS = 300;

/**
 * 浏览器初始化到此看板工厂JS的时间戳。
 */
DF.LOAD_TIME = new Date().getTime();

/**图表主题关联的看板表单实体ID*/
DF.THEME_REF_DASHBOARD_FORM_ID = "DG_REF_DASHBOARD_FORM_ID";

/**图表渲染器附加属性：默认联动事件类型，默认值为："click" */
DF.RENDERER_ADDITION_DTF_LINK_EVENT_TYPE = "defaultLinkEventType";

/**
 * 创建看板实例，为其添加看板API，并设置状态：dashboard.statusPreInit(true)。
 * 
 * @param root 看板根对象，格式参考DF.Dashboard()
 * @returns 新看板实例
 */
DF.init = function(root)
{
	if(CF.isEmpty(root.id))
		throw new Error("[id] required");
	
	if(root.renderContext == null)
		throw new Error("[renderContext] required");
	
	DF.initRenderContext(root.renderContext);
	DF.startHeartBeat(root.renderContext, root.id);
	
	var dashboard = new DF.Dashboard(root);
	dashboard.statusPreInit(true);
	
	return dashboard;
};

DF.initRenderContext = function(renderContext)
{
	var chartTheme = CF.renderContextChartTheme(renderContext);
	if(!chartTheme)
	{
		var dashboardTheme = CF.renderContextValue(renderContext, renderContextAttrConst.dashboardTheme);
		chartTheme = (dashboardTheme && dashboardTheme.chartTheme ? dashboardTheme.chartTheme : {});
		CF.renderContextChartTheme(renderContext, chartTheme);
	}
	
	CF.initRenderContext(renderContext);
};

/**
 * 创建看板实例
 * 
 * @param root 看板根对象，格式应为：
 *				{
 *				  //唯一ID
 *				  id: "...",
 *				  //渲染上下文
 *				  renderContext: {...},
 *				  //可选，图表数组
 *				  charts: [ 图表跟对象, ... ]
 *				}
 *				
 *				另参考：org.datagear.analysis.Dashboard
 */
DF.Dashboard = function(root)
{
	root.charts = (root.charts || []);
	
	this._root = root;
	
	var charts = this._root.charts;
	for(let i=0; i<charts.length; i++)
	{
		charts[i] = DF.initChart(charts[i], root.renderContext, this);
	}
};

//Dashboard类原型
var dashboardProto = DF.Dashboard.prototype;

DF.initChart = function(chartRoot, renderContext, dashboard)
{
	chartRoot.renderContext = renderContext;
	var chart = CF.init(chartRoot);
	chart.dashboard(dashboard);
	
	return chart;
};

//开始心跳，避免看板会话超时
DF.startHeartBeat = function(renderContext, dashboardId)
{
	if(DF._heartbeatIntervalId != null)
	{
		clearInterval(DF._heartbeatIntervalId);
		DF._heartbeatIntervalId = null;
	}
	
	var contextPath = CF.renderContextWebContextPath(renderContext);
	var heartbeatURL = CF.renderContextWebContextAttr(renderContext, "heartbeatURL");
	
	heartbeatURL = CF.toWebContextPathURL(contextPath, heartbeatURL);
	
	DF._heartbeatIntervalId = setInterval(function()
	{
		var formData = new FormData();
		formData.append(DF.heartbeatConfig.dashboardIdParamName, dashboardId);
		
		fetch(heartbeatURL, DF.fetchOptsOfPostForm(formData));
	},
	DF.heartbeatConfig.interval);
};

//停止心跳
DF.stopHeartBeat = function()
{
	if(DF._heartbeatIntervalId != null)
	{
		clearInterval(DF._heartbeatIntervalId);
		DF._heartbeatIntervalId = null;
	}
};

//----------------------------------------
// Chart prototype start
//----------------------------------------

//Chart类原型
var chartProto = CF.Chart.prototype;

/**
 * 获取/设置图表所属的看板。
 * 
 * @param dashboard 可选，要设置的看板
 */
chartProto.dashboard = function(dashboard)
{
	if(dashboard === undefined)
		return this._dashboard;
	else
		this._dashboard = dashboard;
};

//重写chart._contextCharts()函数
chartProto._contextCharts = function()
{
	return this.dashboard().charts();
};

//重写chart._initForPost()函数
chartProto._initForPostSuper = chartProto._initForPost;
chartProto._initForPost = function()
{
	this._initLinks();
	this._initAutoResize();
	this._initUpdateGroup();
	this._initForPostSuper();
};

//重写chart._postProcessRendered()函数
chartProto._postProcessRenderedSuper = chartProto._postProcessRendered;
chartProto._postProcessRendered = function()
{
	this.bindLinkEventHanders(this.links());
	this._postProcessRenderedSuper();
};

/**
 * 初始化图表联动设置。
 * 此方法从图表元素的elementAttrConst.LINK属性获取联动设置。
 */
chartProto._initLinks = function()
{
	var links = CF.eleAttr(this.element(), elementAttrConst.LINK);
	links = (links ? CF.evalSilently(links) : null);
	
	this.links(links);
};

/**
 * 初始化图表自动调整尺寸设置。
 * 此方法从body元素、图表元素的elementAttrConst.AUTO_RESIZE属性获取联动设置。
 */
chartProto._initAutoResize = function()
{
	var autoResize = CF.eleAttr(this.element(), elementAttrConst.AUTO_RESIZE);
	
	if(!CF.isEmpty(autoResize))
	{
		//使用eval可以支持变量而非仅字面值
		autoResize = CF.evalSilently(autoResize);
	}
	else
	{
		autoResize = this._bodyAutoResize();
	}
	
	this.autoResize(autoResize !== false);
};

chartProto._bodyAutoResize = function()
{
	var autoResizeStr = CF.eleAttr(document.body, elementAttrConst.AUTO_RESIZE);
	
	if(autoResizeStr !== CF._PREV_BODY_AUTO_RESIZE_STR)
	{
		CF._PREV_BODY_AUTO_RESIZE_STR = autoResizeStr;
		CF._PREV_BODY_AUTO_RESIZE = CF.evalSilently(autoResizeStr);
	}
	
	return CF._PREV_BODY_AUTO_RESIZE;
};

/**
 * 初始化图表更新分组。
 * 此方法从body元素、图表元素的elementAttrConst.UPDATE_GROUP属性获取更新分组设置。
 */
chartProto._initUpdateGroup = function()
{
	var updateGroup = CF.eleAttr(this.element(), elementAttrConst.UPDATE_GROUP);
	
	if(CF.isEmpty(updateGroup))
		updateGroup = CF.eleAttr(document.body, elementAttrConst.UPDATE_GROUP);
	
	this.updateGroup(updateGroup);
};

/**
 * 获取/设置初始图表联动设置对象数组。
 * 联动设置对象格式为：
 * {
 *   //可选，联动触发事件类型、事件类型数组，格式参考chart.on()函数的type参数，
 *   //默认值参考DF.RENDERER_ADDITION_DTF_LINK_EVENT_TYPE说明
 *   trigger: ...、[ ... ],
 *   
 *   //可选，联动目标图表元素ID、ID数组
 *   target: "..."、["...", ...],
 *   
 *   //可选，联动数据参数映射表
 *   data:
 *   {
 *     //数据属性名：图表渲染器的linkDataHander所决定的联动源数据的属性访问路径，比如："name"、"data.value"、"[0].name"
 *     //图表数据集参数索引对象：格式同dashboard._batchSetDataSetParamValues()函数的图表数据集参数索引对象
 *     "数据属性名" : 图表数据集参数索引对象、[ 图表数据集参数索引对象, ... ],
 *     ...
 *   }
 * }
 * 
 * 图表初始化时会使用图表元素的"dg-chart-link"属性值执行设置操作。
 * 
 * 图表渲染器实现相关：
 * 图表渲染器应实现on函数，以支持此特性。
 * 
 * @param links 可选，要设置的图表联动设置对象、数组，没有则执行获取操作。
 */
chartProto.links = function(links)
{
	if(links === undefined)
	{
		if(this._links == null)
			this._links = [];
		
		return this._links;
	}
	else
	{
		if(links == null)
			links = [];
		
		if(!CF.isArray(links))
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
chartProto.autoResize = function(autoResize)
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
chartProto.updateGroup = function(group)
{
	if(group === undefined)
	{
		if(this._updateGroup == null)
			this._updateGroup = "";
		
		return this._updateGroup;
	}
	else
	{
		if(group == null)
			group = "";
		
		this._updateGroup = group;
	}
};

/**
 * 为指定图表联动设置绑定事件处理函数。
 * 
 * 图表渲染器实现相关：
 * 图表渲染器应实现on()函数、linkDataHander（可选），以支持此特性。
 * 
 * @param links 图表联动设置对象、数组，格式参考chart.links()函数说明
 * @return 绑定的事件处理函数对象数组，格式为：[ { type: ..., handler: function(...){ ... } }, ... ]
 */
chartProto.bindLinkEventHanders = function(links)
{
	this._assertActive();
	
	if(links == null)
		return [];
	
	if(!CF.isArray(links))
		links = [ links ];
	
	var ehs = [];
	
	var triggers = this._resolveLinksTriggers(links);
	var thisChart = this;
	
	var renderer = this.renderer();
	if(renderer == null || renderer.linkDataHander == null)
	{
		let pluginRenderer = this._pluginRenderer();
		if(pluginRenderer && pluginRenderer.linkDataHander != null)
			renderer = pluginRenderer;
	}
	
	for(let i=0; i<triggers.length; i++)
	{
		let type = triggers[i];
		//默认
		let dataHandler = 0;
		
		if(renderer != null && renderer.linkDataHander != null)
		{
			dataHandler = (CF.isFunction(renderer.linkDataHander) ?
							renderer.linkDataHander(type) : renderer.linkDataHander);
		}
		
		let handler = function()
		{
			let linkSrcData = (CF.isFunction(dataHandler) ? dataHandler.apply(this, arguments) : arguments[dataHandler]);
			thisChart._handleChartEventLink(type, linkSrcData, links);
		};
		
		this.on(type, handler);
		ehs.push({ type: type, handler: handler });
	}
	
	return ehs;
};

//解析不重复的联动设置触发事件数组。
chartProto._resolveLinksTriggers = function(links)
{
	var triggers = [];
	
	for(let i=0; i<links.length; i++)
	{
		let myTriggers = this._resolveLinkTriggers(links[i]);
		
		for(let j=0; j<myTriggers.length; j++)
		{
			if(CF.indexInArray(triggers, myTriggers[j]) < 0)
				triggers.push(myTriggers[j]);
		}
	}
	
	return triggers;
};

chartProto._resolveLinkTriggers = function(link)
{
	var triggers = link.trigger;
	
	//从图表渲染器附加属性中取默认值
	if(!triggers)
		triggers = this.rendererAddition(DF.RENDERER_ADDITION_DTF_LINK_EVENT_TYPE);
	
	//默认值设为"click"
	if(!triggers)
		triggers = "click";
	
	if(!CF.isArray(triggers))
		triggers = [ triggers ];
	
	return triggers;
};

/**
 * 处理指定图表事件的图表联动操作。
 * 此方法根据图表联动设置对象，将图表联动源数据传递至目标图表数据集参数值，然后请求刷新图表数据。
 * 
 * @param type 事件类型
 * @param linkSrcData 联动数据
 * @param links 图表联动设置对象、数组，格式参考chart.links()函数说明
 */
chartProto._handleChartEventLink = function(type, linkSrcData, links)
{
	this._assertActive();
	
	if(!links)
		return false;
	
	if(!CF.isArray(links))
		links = [ links ];
	
	var dashboard = this.dashboard();
	var targetCharts = [];
	
	var batchSource =
	{
		data: linkSrcData,
		getValue: function(name)
		{
			var val = undefined;
			
			//当name为空时，直接使用this.data
			if(name == null || name == "")
			{
				val = this.data;
			}
			else if(CF.isArray(this.data))
			{
				for(let i=0; i<this.data.length; i++)
				{
					//需支持属性路径格式的name
					val = CF.propertyPathValue(this.data[i], name);
					
					if(val !== undefined)
						break;
				}
			}
			else
			{
				//需支持属性路径格式的name
				val = CF.propertyPathValue(this.data, name);
			}
			
			return val;
		}
	};
	
	for(let i=0; i<links.length; i++)
	{
		let link = links[i];
		
		if(!this._isLinkByEventType(link, type))
			continue;
		
		let myTargetCharts = dashboard._batchSetDataSetParamValues(batchSource, link, linkSrcData);
		
		for(let j=0; j<myTargetCharts.length; j++)
		{
			if(CF.indexInArray(targetCharts, myTargetCharts[j]) < 0)
				targetCharts.push(myTargetCharts[j]);
		}
	}
	
	for(let i=0; i<targetCharts.length; i++)
	{
		CF.executeSilently(function()
		{
			targetCharts[i].refresh();
		});
	}
};

chartProto._isLinkByEventType = function(link, type)
{
	var triggers = this._resolveLinkTriggers(link);
	return (CF.indexInArray(triggers, type) >= 0);
};

/**
 * 从服务端获取并更新图表数据。
 * 此函数是基于状态实现的，在一个请求内的多次重复调用只会刷新一次。
 */
chartProto.refresh = function()
{
	this._assertActive();
	
	var unreadys = this.unreadyDataSetParams(true);
	if(unreadys.length > 0)
	{
		throw new Error("chart '#"+this.elementId()+"' dataSetBinds["+unreadys[0].dataSetBindIndex
									+"] DataSetParam["+unreadys[0].paramIndex+"]('"+unreadys[0].param.name+"') value required");
	}
	
	//这里不能使用this.statusPreUpdate(true)的方式实现
	//当在A图表监听器的update函数中调用参数化B图表的refresh()时，
	//可能会出现已设置的statusPreUpdate()状态被PARAM_VALUE_REQUIRED状态覆盖的情况，
	//而导致refresh()失效
	
	this._requestRefresh();
};

var UPDATE_TIME_LIVE_DATA_NAME = CF.BUILTIN_PROP_PREFIX + "UpdateTime";

chartProto._updateTime = function(time)
{
	if(time === undefined)
		return this.liveData(UPDATE_TIME_LIVE_DATA_NAME);
	else
		this.liveData(UPDATE_TIME_LIVE_DATA_NAME, time);
};

var REQUEST_REFRESH_LIVE_DATA_NAME = CF.BUILTIN_PROP_PREFIX + "ReqRefreshes";

chartProto._requestRefresh = function()
{
	var chartQuery = this.dashboard()._buildChartQuery(this);
	var rrds = this.liveData(REQUEST_REFRESH_LIVE_DATA_NAME);
	if(rrds == null)
	{
		rrds = [];
		this.liveData(REQUEST_REFRESH_LIVE_DATA_NAME, rrds);
	}
	
	rrds.push(chartQuery);
};

chartProto._isRequestRefresh = function()
{
	var rrds = this.liveData(REQUEST_REFRESH_LIVE_DATA_NAME);
	return (rrds != null && rrds.length > 0);
};

/**
 * 获取/设置图表是否手动渲染。
 * 
 * @param manualRender 可选，设置是否手动渲染，默认为：false
 * @returns true 是；false 否
 */
chartProto.manualRender = function(manualRender)
{
	if(manualRender === undefined)
	{
		//注意：此属性不应以chart._initManualRender()的方式初始化，
		//因为看板需要在chart.init()之前就读取它的值
		
		if(this._manualRender != null)
			return (this._manualRender == true);
		else
		{
			var eleValue = CF.eleAttr(this.element(), elementAttrConst.MANUAL_RENDER);
			return (eleValue == true || eleValue == "true");
		}
	}
	else
		this._manualRender = manualRender;
};

//----------------------------------------
// Chart prototype end
//----------------------------------------


//----------------------------------------
// Dashboard prototype start
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
dashboardProto.init = function()
{
	if(!this.id())
		throw new Error("dashboard id required");
	if(!this.renderContext())
		throw new Error("dashboard renderContext required");
	
	if(!this.statusPreInit() && !this.statusDestroyed())
		throw new Error("dashboard is illegal state for : init()");
	
	this.statusIniting(true);
	
	this._initListener();
	this._initMapURLs();
	this._initChartResizeHandler();
	this._initUnloadDashboardHandler();
	this._initCharts();
	
	this.statusInited(true);
};

/**
 * 初始化地图URL映射表。
 * 它将body元素的elementAttrConst.MAP_URLS属性值设置为地图URL映射表。
 */
dashboardProto._initMapURLs = function()
{
	var mapURLs = {};
	
	for(let i=0; i<builtinChartMaps.length; i++)
	{
		let namesMap = builtinChartMaps[i];
		for(let j=0; j<namesMap.names.length; j++)
			mapURLs[namesMap.names[j]] = builtinChartMapBaseURL + namesMap.map;
	}
	
	var mapURLsBody = CF.eleAttr(document.body, elementAttrConst.MAP_URLS);
	
	if(mapURLsBody)
		mapURLs = CF.extend(mapURLs, CF.evalSilently(mapURLsBody, {}));
	
	this.mapURLs(mapURLs);
};

/**
 * 初始化看板的监听器。
 * 它将body元素的elementAttrConst.DASHBOARD_LISTENER属性值设置为看板的监听器。
 */
dashboardProto._initListener = function()
{
	var listener = CF.eleAttr(document.body, elementAttrConst.DASHBOARD_LISTENER);
	
	if(listener)
		listener = CF.evalSilently(listener);
	
	this.listener(listener);
};

/**
 * 初始化自动调整图表大小处理器。
 */
dashboardProto._initChartResizeHandler = function()
{
	//解绑之前的，确保此函数可重复调用
	if(this._windowResizeHandler != null)
		window.removeEventListener("resize", this._windowResizeHandler);
	
	var thisDashboard = this;
	this._windowResizeHandler = function()
	{
		if(thisDashboard._resizeChartTimeoutId != null)
			clearTimeout(thisDashboard._resizeChartTimeoutId);
		
		thisDashboard._resizeChartTimeoutId = setTimeout(function()
		{
			if(thisDashboard.statusRendered())
			{
				var charts = thisDashboard.charts();
				
				for(let i =0; i<charts.length; i++)
				{
					let chart = charts[i];
					
					if(chart.autoResize() && chart.isActive())
						chart.resize();
				}
			}
		},
		DF.RESIZE_CHART_TIMEOUT_MS);
	};
	
	window.addEventListener("resize", this._windowResizeHandler);
};

dashboardProto._initUnloadDashboardHandler = function()
{
	//解绑之前的，确保此函数可重复调用
	if(this._windowBeforeunloadHandler)
		window.removeEventListener("beforeunload", this._windowBeforeunloadHandler);
	
	var thisDashboard = this;
	this._windowBeforeunloadHandler = function()
	{
		var renderContext = thisDashboard.renderContext();
		var unloadURL = CF.renderContextWebContextAttr(renderContext, DF.unloadConfig.urlAttrName);
		unloadURL = thisDashboard.contextURL(unloadURL);
		var formData = new FormData();
		formData.append(DF.unloadConfig.dashboardIdParamName, thisDashboard.id());
		
		fetch(unloadURL, DF.fetchOptsOfPostForm(formData));
	}
	
	window.addEventListener("beforeunload", this._windowBeforeunloadHandler);
};

dashboardProto._initCharts = function()
{
	var charts = this.charts();
	
	for(let i=0; i<charts.length; i++)
	{
		let chart = charts[i];
		
		if(chart.manualRender())
			continue;
		
		//如果图表元素不存在（比如在<template></template>里），应忽略初始化
		if(chart.element() == null)
		{
			CF.logWarn("chart '#"+chart.elementId()+"' element not found, init() ignored");
			continue;
		}
		
		if(chart.statusPreInit() || chart.statusDestroyed())
		{
			this._initChart(chart);
		}
	}
};

dashboardProto._initChart = function(chart)
{
	try
	{
		chart.init();
	}
	catch(e)
	{
		CF.logException(e);
	}
};

/**
 * 获取看板ID
 */
dashboardProto.id = function()
{
	return this._root.id;
};

/**
 * 获取全部图表
 */
dashboardProto.charts = function()
{
	return this._root.charts;
};

/**
 * 获取看板渲染上下文。
 */
dashboardProto.renderContext = function()
{
	return this._root.renderContext;
};

/**
 * 获取/设置看板监听器。
 * 看板监听器格式为：
 * {
 *   //可选，渲染看板后置回调函数
 *   rendered: function(dashboard){ ... },
 *   //可选，销毁看板后置回调函数
 *   destroyed: function(dashboard){ ... },
 *   //可选，渲染看板前置回调函数，返回false将阻止渲染看板
 *   onRender: function(dashboard){ ... },
 *   //可选，销毁看板前置回调函数，返回false将阻止销毁看板
 *   onDestroy: function(dashboard){ ... }
 * }
 * 
 * 看板初始化时会使用<body>元素的"dg-dashboard-listener"属性值执行设置操作。
 * 
 * @param listener 可选，要设置的监听器对象，没有则执行获取操作
 */
dashboardProto.listener = function(listener)
{
	if(listener === undefined)
		return this._listener;
	else
		this._listener = listener;
};

/**
 * 获取/设置地图URL映射表。
 *
 * @param mapURLs 可选，要设置的地图URL映射表，仅会覆盖同名的地图URL映射，格式为参考CF.chartMapURLs说明
 * @returns 要获取的地图URL映射表
 */
dashboardProto.mapURLs = function(mapURLs)
{	
	if(mapURLs === undefined)
		return chartMapURLs;
	else
		CF.extend(chartMapURLs, mapURLs);
};

/**
 * 获取指定标识的图表，没有则返回undefined。
 * 
 * @param chartInfo 图表标识信息：图表HTML元素、图表HTML元素ID、图表对象、图表ID、图表索引数值
 */
dashboardProto.chartOf = function(chartInfo)
{
	var charts = this.charts();
	var index = this._chartIndex(charts, chartInfo);
	
	return (index < 0 ? null : charts[index]);
};

/**
 * 获取指定图表在看板图表数组中的索引号，返回-1表示未找到。
 * 
 * @param chartInfo 图表标识信息：图表HTML元素、图表HTML元素ID、图表对象、图表ID、图表索引数值
 */
dashboardProto.chartIndex = function(chartInfo)
{
	var charts = this.charts();
	return this._chartIndex(charts, chartInfo);
};

/**
 * 获取图表索引，返回-1表示未找到。
 * 
 * @param charts 待查找的图表数组
 * @param chartInfo 图表标识信息：图表HTML元素ID、图表对象、图表ID、图表索引数值、图表HTML元素
 */
dashboardProto._chartIndex = function(charts, chartInfo)
{
	if(charts == null || chartInfo == null)
		return -1;
	
	if(CF.isHtmlEle(chartInfo))
		chartInfo = CF.eleAttr(chartInfo, "id");
	
	for(let i=0; i<charts.length; i++)
	{
		let chart = charts[i];
		
		if(chart === chartInfo || chart.elementId() === chartInfo
				|| i === chartInfo || chart.id() === chartInfo)
		{
			return i;
		}
	}
	
	return -1;
};

/**
 * 添加已经初始化的图表。
 * 如果图表已添加至看板，或者图表HTML元素已被看板中的其他图表使用，将不会再次添加，直接返回false。
 * 
 * @param chart 图表对象
 */
dashboardProto.addChart = function(chart)
{
	var exists = this.chartOf(chart);
	
	if(exists != null)
		return false;
	
	exists = this.chartOf(chart.elementId());
	
	if(exists != null)
		return false;
	
	//这里不应限制仅能添加未渲染的图表，因为应允许已完成渲染的图表先从看板移除，后续再加入看板
	
	var charts = this.charts();
	charts.push(chart);
	
	return true;
};

/**
 * 删除图表。
 * 
 * @param chartInfo 图表标识信息：图表HTML元素ID、图表对象、图表ID、图表索引数值、图表HTML元素
 * @param doDestroy 选填参数，是否销毁图表，默认为true
 * @return 移除的图表对象，或者图表未找到时为null
 */
dashboardProto.removeChart = function(chartInfo, doDestroy)
{
	var charts = this.charts();
	var index = this._chartIndex(charts, chartInfo);
	
	if(index < 0)
		return null;
	
	var removed = charts.splice(index, 1)[0];
	
	if(doDestroy !== false)
		this._destroyChart(removed);
	
	return removed;
};

/**
 * 获取当前在指定HTML元素上渲染的图表对象，返回null表示元素上并未渲染图表。
 * 
 * @param element HTML元素、HTML元素ID
 */
dashboardProto.renderedChart = function(element)
{
	return CF.renderedChart(element);
};

/**
 * 获取/设置渲染上下文的属性值。
 * 
 * @param name
 * @param value 要设置的属性值，可选，不设置则执行获取操作
 */
dashboardProto.renderContextValue = function(name, value)
{
	var rc = this.renderContext();
	return CF.renderContextValue(rc, name, value);
};

/**
 * 获取/设置看板级的结果数据格式。
 * 如果某个图表的resultDataFormat()返回null，将会使用这个看板级的结果数据格式。
 * 设置了新的结果数据格式后，下一次图表刷新数据将采用这个新格式。
 * 
 * @param resultDataFormat 可选，要设置的结果数据格式，结构参考：org.datagear.analysis.ResultDataFormat
 * @returns 要获取的结果数据格式，没有则返回null
 */
dashboardProto.resultDataFormat = function(resultDataFormat)
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
dashboardProto.render = function()
{
	if(this.statusPreInit())
		this.init();
	
	if(!this.statusInited() && !this.statusDestroyed())
		throw new Error("dashboard is illegal state for : render()");
	
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
dashboardProto.doRender = function()
{
	if(!this.statusRendering())
		throw new Error("dashboard is illegal state for : doRender()");
	
	this._renderForms();
	this._prepareDoRenderCharts();
	this.startHandleCharts();
	
	this.statusRendered(true);
};

dashboardProto._prepareDoRenderCharts = function()
{
	var charts = this.charts();
	
	for(let i=0; i<charts.length; i++)
	{
		let chart = charts[i];
		
		if(chart.manualRender())
			continue;
		
		if(chart.statusInited() || chart.statusDestroyed())
		{
			chart.statusPreRender(true);
		}
	}
};

//渲染看板表单
dashboardProto._renderForms = function()
{
	var forms = CF.elesOfSelector("form[dg-dashboard-form]");
	
	var dashboard = this;
	forms.forEach(function(form)
	{
		dashboard.renderForm(form);
	});
};

//校验看板是否是活着的
dashboardProto._assertAlive = function()
{
	if(!this.isAlive())
		throw new Error("dashboard not alive");
};

//校验看板是否处于活跃状态
dashboardProto._assertActive = function()
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
 *   //可选，输入项类型，参考CF.DataSetParamType，默认值为：CF.DataSetParamType.STRING
 *   type: "...",
 *   //可选，是否必须，默认为false
 *   required: true || false,
 *   //可选，输入框类型，参考chartTool.DataSetParamInputType，默认值为：chartTool.DataSetParamInputType.TEXT
 *   inputType: "...",
 *   //可选，输入框配置，参考chartTool.renderDataSetParamForm函数说明
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
 * 图表数据集参数索引对象格式参考dashboard._batchSetDataSetParamValues()函数相关说明，其中value函数的sourceValueContext参数为：表单数据对象、表单HTML元素。
 * 
 * @param form 要渲染的<form>表单HTML元素、HTML元素ID，表单结构允许灵活自定义，具体参考chartTool.renderDataSetParamForm
 * @param config 可选，表单配置对象，默认为表单元素的elementAttrConst.DASHBOARD_FORM属性值
 */
dashboardProto.renderForm = function(form, config)
{
	this._assertAlive();
	
	form = (CF.isString(form) ? CF.eleOfId(form) : form);
	
	CF.eleAddClass(form, "dg-dashboard-form");
	
	if(config == null)
		config = CF.evalSilently(CF.eleAttr(form, elementAttrConst.DASHBOARD_FORM), {});
	
	var dashboard = this;
	var globalTheme = CF.renderContextChartTheme(this.renderContext());
	//构建用于批量设置数据集参数值的对象
	var batchSet = { target: [], data: {} };
	
	config = CF.extend(
	{
		submit: function(formData)
		{
			let thisForm = this;
			let charts = dashboard._batchSetDataSetParamValues(formData, batchSet, [ formData, thisForm ]);
			
			for(let i=0; i<charts.length; i++)
			{
				CF.executeSilently(function()
				{
					charts[i].refresh();
				});
			}
		}
	},
	config);
	
	if(config.link)
	{
		let link = config.link;
		
		//转换简写格式
		if(CF.isString(link) || CF.isArray(link))
			link = { target: link };
		
		batchSet.target = link.target;
		//新构建data对象，因为可能会在下面被修改
		batchSet.data = (link.data ? CF.extend({}, link.data) : {});
	}
	
	var items = [];
	var defaultValues = {};
	
	var sourceItems = (config.items || []);
	if(!CF.isArray(sourceItems))
		sourceItems = [ sourceItems ];
	
	for(let i=0; i<sourceItems.length; i++)
	{
		let item = sourceItems[i];
		
		if(CF.isString(item))
			item = { name: item };
		else
			//确保不影响初始对象
			item = CF.extend({}, item);
		
		if(!item.type)
			item.type = CF.DataSetParamType.STRING;
		
		items.push(item);
		
		if(item.value != null)
			defaultValues[item.name] = item.value;
		
		//合并输入项的link设置
		if(item.link != null)
			batchSet.data[item.name] = item.link;
	}
	
	config.paramValues = defaultValues;
	config.chartTheme = globalTheme;
	
	CF.addThemeRefEntity(globalTheme, DF.THEME_REF_DASHBOARD_FORM_ID);
	CF.chartTool.renderDataSetParamForm(form, items, config);
};

/**
 * 重新调整指定图表尺寸。
 * 
 * @param chartInfo 图表标识信息：图表HTML元素ID、图表对象、图表ID、图表索引数值、图表HTML元素
 * @returns 图表对象
 */
dashboardProto.resizeChart = function(chartInfo)
{
	this._assertActive();
	
	var chart = this.chartOf(chartInfo);
	chart.resize();
	
	return chart;
};

/**
 * 重新调整所有活跃图表尺寸。
 */
dashboardProto.resizeAllCharts = function()
{
	this._assertActive();
	
	this.charts().forEach(function(chart)
	{
		if(chart.isActive())
			chart.resize();
	});
};

/**
 * 是否正在监视处理看板图表。
 */
dashboardProto.isHandlingCharts = function()
{
	return (this._handlingChartsIntervalId != null);
};

/**
 * 开始监视处理看板图表，循环查看它们的状态，执行相应操作：
 * 如果图表需要渲染，则执行chart.render()函数；如果图表需要更新，则执行chart.update()函数。
 */
dashboardProto.startHandleCharts = function()
{
	this._assertAlive();
	this.stopHandleCharts();
	this._handlingChartsIntervalId = setInterval(() =>
	{
		this._exeDoHandleChartsMutex();
	},
	DF.HANDLE_CHART_INTERVAL_MS);
};

dashboardProto._exeDoHandleChartsMutex = function()
{
	if(this._inHandlingCharts === true)
		return false;
	
	this._inHandlingCharts = true;
	
	CF.executeSilently(() =>
	{
		this._doHandleCharts();
	});
	
	this._inHandlingCharts = false;
	
	return true;
};

/**
 * 停止监视处理看板图表
 */
dashboardProto.stopHandleCharts = function()
{
	if(this._handlingChartsIntervalId != null)
	{
		clearInterval(this._handlingChartsIntervalId);
		this._handlingChartsIntervalId = null;
	}
};

/**
 * 开始循环处理看板所有图表，根据其状态执行render或者update。
 */
dashboardProto._doHandleCharts = function()
{
	var charts = this.charts();
	
	for(let i=0; i<charts.length; i++)
	{
		let chart = charts[i];
		
		if(this._isWaitForRender(chart))
			this._renderChart(chart);
	}
	
	var preUpdateGroups = {};
	var preUpdateLocals = [];
	var time = CF.currentDateMs();
	
	for(let i=0; i<charts.length; i++)
	{
		let chart = charts[i];
		
		let wait = this._isWaitForUpdate(chart, time);
		if(wait > 0)
		{
			//应立即设置为HANDLING_UPDATE状态
			chart.status(chartStatusConst.HANDLING_UPDATE);
			
			let chartQuery = null;
			
			//由chart.refresh()函数触发
			if(wait == 2)
			{
				let rrds = chart.liveData(REQUEST_REFRESH_LIVE_DATA_NAME);
				chartQuery = (rrds == null || rrds.length == 0 ? null : rrds.shift());
			}
			
			chartQuery = (chartQuery == null ? this._buildChartQuery(chart) : chartQuery);
			
			if(this._isLocalChart(chart))
			{
				preUpdateLocals.push({chart: chart, query: chartQuery});
			}
			else
			{
				let group = chart.updateGroup();
				let preUpdates = preUpdateGroups[group];
				
				if(preUpdates == null)
				{
					preUpdates = [];
					preUpdateGroups[group] = preUpdates;
				}
				
				preUpdates.push({chart: chart, query: chartQuery});
			}
		}
	}
	
	CF.executeSilently(() =>
	{
		this._doHandleChartsLocal(preUpdateLocals);
	});
	
	var updateDashboardURL = CF.renderContextWebContextAttr(this.renderContext(), "updateDashboardURL");
	var url = this.contextURL(updateDashboardURL);
	
	for(let group in preUpdateGroups)
	{
		CF.executeSilently(() =>
		{
			this._doHandleChartsAjax(url, group, preUpdateGroups[group]);
		});
	}
};

dashboardProto._isWaitForRender = function(chart)
{
	return chart.statusPreRender();
};

/**
 * 给定图表是否在等待更新数据。
 * @param chart
 * @param currentTime
 * @returns 0 否；1 是，但不是refresh()触发；2 是，并且由refresh()触发
 */
dashboardProto._isWaitForUpdate = function(chart, currentTime)
{
	if(!chart.isActive())
		return 0;
	
	var wait = 0;
	
	var status = chart.status();
	
	if(status == chartStatusConst.HANDLING_UPDATE)
	{
		wait = 0;
	}
	else
	{
		var updateInterval = chart.updateInterval();
		var isRequestRefresh = chart._isRequestRefresh();
		
		if(isRequestRefresh)
		{
			wait = 2;
		}
		else if(chart.statusRendered() || chart.statusPreUpdate())
		{
			wait = 1;
		}
		else if(updateInterval > -1 && (chart.statusUpdated() || status == chartStatusConst.UPDATE_ERROR))
		{
			var prevUpdateTime = chart._updateTime();
			if(prevUpdateTime == null || (currentTime - prevUpdateTime) >= updateInterval)
			{
				wait = 1;
			}
		}
		
		if(wait == 1)
		{
			//应升级为优先级更高的刷新操作，且无需判断参数是否准备好
			if(isRequestRefresh)
			{
				wait = 2;
			}
			else if(chart.unreadyDataSetParams(true).length > 0)
			{
				//标记为需要参数输入，避免参数准备好时会立即自动更新，实际应该由API控制是否更新
				chart.status(chartStatusConst.PARAM_VALUE_REQUIRED);
				wait = 0;
			}
		}
	}
	
	return wait;
};

dashboardProto._isLocalChart = function(chart)
{
	var dataSetBinds = chart.dataSetBinds();
	return (dataSetBinds.length == 0);
};

dashboardProto._renderChart = function(chart)
{
	try
	{
		chart.render();
	}
	catch(e)
	{
		//设置为渲染出错状态，避免渲染失败后会_doHandleCharts中会无限尝试渲染
		chart.status(chartStatusConst.RENDER_ERROR);
		CF.logException(e);
	}
};

dashboardProto._chartsOfChartQueryPairs = function(chartQueryPairs)
{
	var re = [];
	
	for(let i=0; i<chartQueryPairs.length; i++)
	{
		re.push(chartQueryPairs[i].chart);
	}
	
	return re;
};

dashboardProto._doHandleChartsLocal = function(chartQueryPairs)
{
	if(!chartQueryPairs || chartQueryPairs.length == 0)
		return;
	
	var charts = this._chartsOfChartQueryPairs(chartQueryPairs);
	var updateTime = CF.currentDateMs();
	
	var dashboardQueryForm = this._buildDashboardQueryForm(chartQueryPairs);
	var dashboardQuery = this._dashboardQueryOfForm(dashboardQueryForm);
	// 加载上下文对象，使用此上下文对象可以简化回调函数参数，也易于扩展
	var fetchContext =
	{
		charts: charts,
		query: dashboardQuery
	};
	
	//这里不允许异常中断
	CF.executeSilently(() =>
	{
		this._execListenerOnFetch(fetchContext);
	});
	
	try
	{
		for(let i=0; i<charts.length; i++)
		{
			let chart = charts[i];
			let chartQuery = this._chartQueryOfDashboardQuery(dashboardQuery, chart.id());
			let chartResult = {};
			//设置空数据集结果数组，避免后续出现空指针异常
			chart.results(chartResult, []);
			
			this._updateChart(chart, chartResult, chartQuery, true);
		}
	}
	finally
	{
		this._setChartsUpdateTime(charts, updateTime);			
	}
};

dashboardProto._doHandleChartsAjax = function(url, group, chartQueryPairs)
{
	if(!chartQueryPairs || chartQueryPairs.length == 0)
		return;
	
	var charts = this._chartsOfChartQueryPairs(chartQueryPairs);
	var dashboardQueryForm = this._buildDashboardQueryForm(chartQueryPairs);
	var dashboardQuery = this._dashboardQueryOfForm(dashboardQueryForm);
	// 加载上下文对象，使用此上下文对象可以简化回调函数参数，也易于扩展
	var fetchContext =
	{
		group: group,
		charts: charts,
		query: dashboardQuery,
		//此次请求是否成功，将在后续设置
		success: undefined
	};
	
	//这里不允许异常中断
	CF.executeSilently(() =>
	{
		this._execListenerOnFetch(fetchContext);
	});
	
	fetch(url, DF.fetchOptsOfPostJson(dashboardQueryForm))
	.then((response) =>
	{
		if(!response.ok)
			throw new Error(DF.msgOfResponse(response));
		
		let re = response.json();
		
		re.then((data) =>
		{
			let dashboardResult = (data ? data : {});
			dashboardResult.chartResults = (dashboardResult.chartResults ? dashboardResult.chartResults : {});
			dashboardResult.chartErrors = (dashboardResult.chartErrors ? dashboardResult.chartErrors : {});
			
			this._handleChartsAjaxSuccess(fetchContext, dashboardResult);
		});
		
		return re;
	})
	.catch((error) =>
	{
		this._handleChartsAjaxError(fetchContext, error);
	})
	.finally(() =>
	{
		let updateTime = CF.currentDateMs();
		this._setChartsUpdateTime(charts, updateTime);
	});
};

//执行监听器的onFetch回调函数
dashboardProto._execListenerOnFetch = function(fetchContext)
{
	var charts = fetchContext.charts;
	var dashboardQuery = fetchContext.query;
	
	for(let i=0; i<charts.length; i++)
	{
		let chart = charts[i];
		let chartListener = chart.listener();
		
		if(chartListener && chartListener.onFetch)
		{
			let chartQuery = (this._chartQueryOfDashboardQuery(dashboardQuery, chart.id()) || {});
			
			CF.executeSilently(function()
			{
				chartListener.onFetch(chart, chartQuery);
			});
		}
	}
};

dashboardProto._handleChartsAjaxSuccess = function(fetchContext, dashboardResult)
{
	fetchContext.success = true;
	
	var chartResults = dashboardResult.chartResults;
	var chartErrors = dashboardResult.chartErrors;
	var dashboardQuery = fetchContext.query;
	
	for(let chartId in chartResults)
	{
		let chart = this.chartOf(chartId);
		
		if(!chart)
			continue;
		
		CF.executeSilently(() =>
		{
			let chartResult = (chartResults[chartId] || {});
			let chartQuery = this._chartQueryOfDashboardQuery(dashboardQuery, chartId);
			this._updateChart(chart, chartResult, chartQuery, true);
		});
	}
	
	for(let chartId in chartErrors)
	{
		let chart = this.chartOf(chartId);
		
		if(!chart)
			continue;
		
		CF.executeSilently(() =>
		{
			let error = (chartErrors[chartId] || { type: "Error", message: "error" });
			let chartQuery = this._chartQueryOfDashboardQuery(dashboardQuery, chartId);
			this._handleChartAjaxError(chart, error, chartQuery, true);
		});
	}
};

dashboardProto._handleChartsAjaxError = function(fetchContext, error)
{
	fetchContext.success = false;
	
	var charts = fetchContext.charts;
	var dashboardQuery = fetchContext.query;
	var errorMsg = (error && error.message ? error.message : "error");
	var logException = true;
	
	for(let i=0; i<charts.length; i++)
	{
		let chart = charts[i];
		
		CF.executeSilently(() =>
		{
			//结构同：org.datagear.analysis.support.ChartResultErrorMessage
			let error = { type: "Error", message: errorMsg };
			let chartQuery = this._chartQueryOfDashboardQuery(dashboardQuery, chart.id());
			this._handleChartAjaxError(chart, error, chartQuery, false);
		});
	}
	
	if(logException)
	{
		CF.logException("fetch charts data error : " + errorMsg);
	}
};

dashboardProto._handleChartAjaxError = function(chart, error, chartQuery, logIfNone)
{
	this._handleChartResultError(chart, error, chartQuery, true, logIfNone);
};

/**
 * 处理图表结果错误。
 * 
 * @param chart 图表对象
 * @param error 图表结果错误信息对象，结构参考：org.datagear.analysis.support.ChartResultErrorMessage
 * @param chartQuery 结果错误对应的图表查询，可能null
 * @param setErrorStatus 是否将图表状态更新为：chartStatusConst.UPDATE_ERROR
 * @param logIfNone 可选，如果chart.listener()没有定义updateError，是否输出默认日志，默认为：true
 */
dashboardProto._handleChartResultError = function(chart, error, chartQuery, setErrorStatus, logIfNone)
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
		let type = (error ? error.type : "Error");
		let message = (error ? error.message : "chart result error");
		CF.logException("chart '#"+chart.elementId()+"' " + type + " : " + message);
	}
};

/**
 * 更新指定图表。
 * 
 * @param chart 图表对象
 * @param chartResult 图表结果对象，参考：org.datagear.analysis.ChartResult
 * @param chartQuery 图表结果对应的查询信息，可能null
 * @param force 可选，是否强制更新，默认值：false
 */
dashboardProto._updateChart = function(chart, chartResult, chartQuery, force)
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
			
			this._doUpdateChart(chart, chartResult, chartQuery);
		}
		else
			throw new Error("chart '#"+chart.elementId()+"' not active");
	}
	catch(e)
	{
		//设置为更新出错状态，避免更新失败后会_doHandleCharts中会无限尝试更新
		chart.status(chartStatusConst.UPDATE_ERROR);
		CF.logException(e);
	}
};

dashboardProto._doUpdateChart = function(chart, chartResult, chartQuery)
{
	chart.update(chartResult);
};

dashboardProto._setChartsUpdateTime = function(charts, time)
{
	CF.executeSilently(() =>
	{
		for(let i=0; i<charts.length; i++)
		{
			charts[i]._updateTime(time);
		}
	});
};

//构建更新看板的ajax请求数据
dashboardProto._buildDashboardQueryForm = function(chartQueryPairs)
{
	var updateDashboardConfig = DF.updateDashboardConfig;
	var globalResultDataFormat = this.resultDataFormat();
	
	//这里需要深度拷贝，因为后续可能会被修改
	if(globalResultDataFormat)
		globalResultDataFormat = CF.extend(true, {}, globalResultDataFormat);
	
	var dashboardQueryForm = {};
	//结构同：org.datagear.analysis.DashboardQuery
	var dashboardQuery = { chartQueries: {}, resultDataFormat: globalResultDataFormat, suppressChartError: true };
	
	dashboardQueryForm[updateDashboardConfig.dashboardIdParamName] = this.id();
	this._dashboardQueryOfForm(dashboardQueryForm, dashboardQuery);
	
	if(chartQueryPairs && chartQueryPairs.length > 0)
	{
		for(let i=0; i<chartQueryPairs.length; i++)
		{
			let chart = chartQueryPairs[i].chart;
			let chartQuery = chartQueryPairs[i].query;
			this._chartQueryOfDashboardQuery(dashboardQuery, chart.id(), chartQuery);
		}
	}
	
	return dashboardQueryForm;
};

//获取/设置看板查询对象中的图表查询对象
dashboardProto._chartQueryOfDashboardQuery = function(dashboardQuery, chartId, chartQuery)
{
	var chartQueries = dashboardQuery.chartQueries;
	
	if(chartQuery === undefined)
	{
		return (chartQueries ? chartQueries[chartId] : null);
	}
	else
	{
		if(chartQueries == null)
		{
			chartQueries = {};
			dashboardQuery.chartQueries = chartQueries;
		}
		
		chartQueries[chartId] = chartQuery;
	}
};

//构建图表查询对象，结构同：org.datagear.analysis.ChartQuery
dashboardProto._buildChartQuery = function(chart)
{
	var globalResultDataFormat = this.resultDataFormat();
	
	//这里需要深度拷贝，因为后续可能会被修改
	if(globalResultDataFormat)
		globalResultDataFormat = CF.extend(true, {}, globalResultDataFormat);
	
	var chartQuery = { dataSetQueries: [], resultDataFormat: chart.resultDataFormat() };
	
	if(chartQuery.resultDataFormat)
	{
		//这里需要深度拷贝，因为后续可能会被修改
		chartQuery.resultDataFormat = CF.extend(true, {}, chartQuery.resultDataFormat);
	}
	else
	{
		chartQuery.resultDataFormat = globalResultDataFormat;
	}
	
	var dataSetBinds = chart.dataSetBinds();
	for(let i=0; i<dataSetBinds.length; i++)
	{
		//这里无需处理是否忽略获取结果（ignoreFetch），后台会处理
		//这里需要深度拷贝，因为后续可能会被修改
		let dataSetQuery = CF.extend(true, {}, dataSetBinds[i].query);
		chartQuery.dataSetQueries.push(dataSetQuery);
	}
	
	return chartQuery;
};

dashboardProto._dashboardQueryOfForm = function(dashboardQueryForm, dashboardQuery)
{
	var dashboardQueryParamName = DF.updateDashboardConfig.dashboardQueryParamName;
	
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
 * dashboard.loadChart(element, successCallback);
 * dashboard.loadChart(element, chartWidgetId, successCallback);
 * 
 * @param element 用于渲染图表的HTML元素、HTML元素ID
 * @param chartWidgetId 选填，要加载的图表部件ID，如果不设置，将从元素的"dg-chart-widget"属性取
 * @param successCallback 选填，图表加载成功回调函数：function(chart){ ... }
 * @param errorCallback 选填，图表加载失败回调函数：function(error){ ... }
 */
dashboardProto.loadChart = function(element, chartWidgetId, successCallback, errorCallback)
{
	//异步加载无需看板已渲染
	//this._assertAlive();
	
	element = (CF.isString(element) ? CF.eleOfId(element) : element);
	
	// (element, successCallback)
	if(CF.isFunction(chartWidgetId))
	{
		errorCallback = successCallback
		successCallback = chartWidgetId;
		chartWidgetId = null;
	}
	
	if(!chartWidgetId)
		chartWidgetId = CF.elementWidgetId(element);
	
	this._loadCharts([ element ], [ chartWidgetId ],
	(charts) =>
	{
		if(successCallback)
			return successCallback(charts[0]);
	},
	errorCallback);
};

/**
 * 异步加载多个图表，并将它们加入此看板。
 * 
 * 支持调用方式：
 * dashboard.loadCharts(elements);
 * dashboard.loadCharts(elements, chartWidgetIds);
 * dashboard.loadCharts(elements, successCallback);
 * dashboard.loadCharts(elements, chartWidgetIds, successCallback);
 * 
 * @param elements 用于渲染图表的CSS元素选择器字符串、HTML元素数组
 * @param chartWidgetIds 可选，要加载的图表部件ID数组，如果不设置，将从元素的"dg-chart-widget"属性取
 * @param successCallback 选填，图表加载成功回调函数：function(charts){ ... }
 * @param errorCallback 选填，图表加载失败回调函数：function(error){ ... }
 */
dashboardProto.loadCharts = function(elements, chartWidgetIds, successCallback, errorCallback)
{
	//异步加载无需看板已渲染
	//this._assertAlive();
	
	elements = (CF.isString(elements) ? CF.elesOfSelector(elements) : elements);
	
	// (elements, successCallback)
	if(CF.isFunction(chartWidgetIds))
	{
		errorCallback = successCallback
		successCallback = chartWidgetIds;
		chartWidgetIds = null;
	}
	
	var newChartWidgetIds = [];
	
	for(let i=0; i<elements.length; i++)
	{
		newChartWidgetIds[i] = (chartWidgetIds == null ? null : chartWidgetIds[i]);
		
		if(CF.isEmpty(newChartWidgetIds[i]))
			newChartWidgetIds[i] = CF.elementWidgetId(elements[i]);
	}
	
	this._loadCharts(elements, newChartWidgetIds, successCallback, errorCallback);
};

/**
 * 将元素内（包括自身）所有设置了"dg-chart-widget"属性，且未初始化为图表的HTML元素异步加载为图表。
 * 如果没有需要加载的元素，将不会执行异步请求。
 * 
 * 支持调用方式：
 * dashboard.loadUnsolvedCharts();
 * dashboard.loadUnsolvedCharts(element);
 * dashboard.loadUnsolvedCharts(successCallback);
 * dashboard.loadUnsolvedCharts(element, successCallback);
 * 
 * @param element 可选，限定查找的根HTML元素、HTML元素ID，默认为：<body>元素
 * @param successCallback 选填，图表加载成功回调函数：function(charts){ ... }
 * @param errorCallback 选填，图表加载失败回调函数：function(error){ ... }
 * @return 要异步加载的HTML元素数组
 */
dashboardProto.loadUnsolvedCharts = function(element, successCallback, errorCallback)
{
	//异步加载无需看板已渲染
	//this._assertAlive();
	
	// (elements, successCallback)
	if(CF.isFunction(element))
	{
		errorCallback = successCallback
		successCallback = element;
		element = null;
	}
	
	element = (element == null ? document.body : (CF.isString(element) ? CF.eleOfId(element) : element));
	
	var eleWidgetIdInfo = CF.elesWithWidgetId(element);
	var unsolvedEles = [];
	var unsolvedWidgetIds = [];
	
	for(let i=0; i<eleWidgetIdInfo.elements.length; i++)
	{
		let ele = eleWidgetIdInfo.elements[i];
		let widgetId = eleWidgetIdInfo.widgetIds[i];
		
		if(this._loadingChartElement(ele))
			continue;
		
		if(this.renderedChart(ele) != null)
			continue;
		
		//看板中可能存在对应此元素的已初始化但是未渲染的图表，这里也要排除
		if(this.chartOf(ele) != null)
			continue;
		
		unsolvedEles.push(ele);
		unsolvedWidgetIds.push(widgetId);
	}
	
	if(unsolvedEles.length > 0)
	{
		this._loadCharts(unsolvedEles, unsolvedWidgetIds, successCallback, errorCallback);
	}
	
	return unsolvedEles;
};

/**
 * 异步加载图表。
 * 
 * @param elements HTML元素数组
 * @param chartWidgetIds 图表部件ID数组，与上面一一对应
 * @param successCallback 选填，加载成功回调函数：function(charts){ ... }
 * @param errorCallback 选填，加载失败回调函数：function(error){ ... }
 */
dashboardProto._loadCharts = function(elements, chartWidgetIds, successCallback, errorCallback)
{
	var elementsLen = elements.length;
	for(let i=0; i<elementsLen; i++)
	{
		let element = elements[i];
		let chartWidgetId = chartWidgetIds[i];
		
		if(!chartWidgetId)
			throw new Error((elementsLen > 1 ? "the "+(i+1)+"-th " : "")
				+ "[chartWidgetId] required");
		
		if(this._loadingChartElement(element))
			throw new Error((elementsLen > 1 ? "the "+(i+1)+"-th " : "")
				+ "element is loading chart");
		
		if(this.renderedChart(element) != null)
			throw new Error((elementsLen > 1 ? "the "+(i+1)+"-th " : "")
				+ "element has a rendered chart");
		
		//看板中可能存在已初始化但是未渲染的图表，也不应允许异步加载
		if(this.chartOf(element) != null)
			throw new Error((elementsLen > 1 ? "the "+(i+1)+"-th " : "")
				+ "element has a bounded chart");
	}
	
	var loadChartURL = CF.renderContextWebContextAttr(this.renderContext(), "loadChartURL");
	var url = this.contextURL(loadChartURL);
	var loadChartConfig = DF.loadChartConfig;
	
	var formData = new FormData();
	formData.append(loadChartConfig.dashboardIdParamName, this.id());
	for(let i=0; i<chartWidgetIds.length; i++)
		formData.append(loadChartConfig.chartWidgetIdParamName, chartWidgetIds[i]);
	
	this._loadingChartElement(elements, true);
	
	fetch(url, DF.fetchOptsOfPostForm(formData))
	.then((response) =>
	{
		if(!response.ok)
			throw new Error(DF.msgOfResponse(response));
		
		this._loadingChartElement(elements, false);
		
		let re = response.json();
		
		re.then((data) =>
		{
			let chartRoots = (data || []);
			let charts = [];
			
			for(let i=0; i<chartRoots.length; i++)
			{
				charts[i] = this._initLoadedChart(chartRoots[i], elements[i], chartWidgetIds[i]);
				this._addLoadedChart(charts[i]);
			}
			
			if(successCallback)
				successCallback(charts);
		});
		
		return re;
	})
	.catch((error) =>
	{
		this._loadingChartElement(elements, false);
		
		if(errorCallback != null)
			errorCallback(error);
	});
};

//获取单个/设置多个HTML元素是否正在加载图表
dashboardProto._loadingChartElement = function(element, set)
{
	var name = CF.builtinPropName("loadingChart");
	
	if(set === undefined)
	{
		return (CF.eleData(element, name) === true);
	}
	else
	{
		if(!CF.isArray(element))
			element = [ element ];
		
		for(let i=0; i<element.length; i++)
		{
			if(set === true)
				CF.eleData(element[i], name, true);
			else
				CF.eleRemoveData(element[i], name);
		}
	}
};

/**
 * 初始化异步加载的图表。
 * 
 * @param chartRoot 图表根对象
 * @param element 图表HTML元素
 * @param chartWidgetId 图表部件ID
 */
dashboardProto._initLoadedChart = function(chartRoot, element, chartWidgetId)
{
	//这里不应设置"dg-chart-widget"属性而破坏了元素的原生结构
	//CF.elementWidgetId(element, chartWidgetId);
	
	var eleId = CF.checkSetChartElementId(element);
	chartRoot.elementId = eleId;
	
	return DF.initChart(chartRoot, this.renderContext(), this);
};

dashboardProto._addLoadedChart = function(chart)
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
 * 批量设置图表数据集参数值。
 * 
 * @param sourceData 源参数值对象，格式为：{ 源参数名 : 源参数值, ...} 或者 { getValue: function(name){ return ...; } }（需支持属性路径）
 * @param batchSet 批量设置对象，格式为：
 * 					{
 * 					  //可选，要设置的目标图表元素ID、图表ID、看板图表数组索引，或者它们的数组
 * 					  target: "..."、["...", ...],
 * 					  
 * 					  //可选，要设置的参数值映射表，没有则不设置任何参数值
 * 					  data:
 * 					  {
 * 					    源参数名 : 图表数据集参数索引对象、[ 图表数据集参数索引对象, ... ],
 * 					    ...
 * 					  }
 * 					}
 * 					上述【源参数名】可以是简单参数名，例如："name"、"value"，也可以是源参数对象的属性路径，
 * 					例如："order.name"、"[0].name"、"['order'].product.name"。
 * 					图表数据集参数索引对象用于确定源参数值要设置到的目标图表数据集参数，格式为：
 * 					{
 *                    //可选，可以是上述批量设置对象的target数组中的索引，也可以是图表元素ID、图表ID、看板图表数组索引，默认值为：0
 * 					  chart: 数值、"...",
 * 					  
 * 					  //可选，目标图表数据集数组的索引数值，默认为：0
 * 					  dataSet: ...,
 * 					  
 * 					  //可选，目标图表数据集的参数数组索引/参数名，默认为：0
 * 					  param: ...,
 * 					  
 * 					  //可选，自定义源参数值处理函数，返回要设置的目标参数值
 * 					  //sourceValue 源参数值
 * 					  //sourceValueContext 源参数值上下文对象
 * 					  value: function(sourceValue, sourceValueContext){ return ...; }
 * 					}
 * 					或者，可简写为上述图表数据集参数索引对象的"param"属性值
 * @param sourceValueContext 可选，传递给图表数据集参数索引对象的value函数sourceValueContext参数的对象，如果为数组，则传递多个参数，默认为sourceData
 * @return 批量设置的图表对象数组
 */
dashboardProto._batchSetDataSetParamValues = function(sourceData, batchSet, sourceValueContext)
{
	sourceValueContext = (sourceValueContext === undefined ? sourceData : sourceValueContext);
	
	var targets = (batchSet.target == null ? [] : (CF.isArray(batchSet.target) ? batchSet.target : [ batchSet.target ]));
	var targetCharts = [];
	
	for(let i=0; i<targets.length; i++)
	{
		targetCharts[i] = this.chartOf(targets[i]);
		
		if(targetCharts[i] == null)
			throw new Error("no chart found for : " + targets[i]);
	}
	
	var dataMap = (batchSet.data || {});
	var hasGetValueFunc = CF.isFunction(sourceData.getValue);
	
	var sourceValueContextArgs = [ "place-holder-for-source-value" ];
	sourceValueContextArgs = sourceValueContextArgs.concat(CF.isArray(sourceValueContext) ? sourceValueContext : [ sourceValueContext ]);
	
	for(let name in dataMap)
	{
		let dataValue = undefined;
		
		if(hasGetValueFunc)
		{
			dataValue = sourceData.getValue(name);
		}
		else
		{
			//当name为空时，应直接使用sourceData
			if(name == null || name == "")
				dataValue = sourceData;
			else
				dataValue = CF.propertyPathValue(sourceData, name);
		}
		
		let indexes = dataMap[name];
		
		if(!CF.isArray(indexes))
			indexes = [ indexes ];
		
		for(let i=0; i<indexes.length; i++)
		{
			let indexObj = indexes[i];
			
			let chartIdx = 0;
			let dataSetIdx = 0;
			let param = 0;
			let paramValue = null;
			
			//参数名/索引号
			if(CF.isStringOrNumber(indexObj))
			{
				param = indexObj;
				paramValue = dataValue;
			}
			else
			{
				chartIdx = (indexObj.chart != null ? indexObj.chart : chartIdx);
				dataSetIdx = (indexObj.dataSet != null ? indexObj.dataSet : dataSetIdx);
				param = (indexObj.param != null ? indexObj.param : param);
				
				if(indexObj.value != null)
				{
					sourceValueContextArgs[0] = dataValue;
					paramValue = indexObj.value.apply(indexObj, sourceValueContextArgs);
				}
				else
					paramValue = dataValue;
			}
			
			let targetChart = null;
			
			//优先使用batchSet.target中的索引号
			if(CF.isNumber(chartIdx) && targets[chartIdx] != null)
				targetChart = targetCharts[chartIdx];
			else
			{
				targetChart = this.chartOf(chartIdx);
				
				if(targetChart == null)
					throw new Error("no chart found for : " + chartIdx);
				
				if(CF.indexInArray(targetCharts, targetChart) < 0)
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
dashboardProto.serverDate = function(asMillisecond)
{
	//参考org.datagear.web.controller.ServerTimeJsController.SERVERTIME_JS_VAR
	if(global.DATAGEAR_SERVER_TIME == null)
		throw new Error("get current server date unsupported");
	
	var cct = CF.currentDateMs();
	var cst = global.DATAGEAR_SERVER_TIME + (cct - DF.LOAD_TIME);
	
	if(asMillisecond === true)
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
dashboardProto.user = function()
{
	var user = this.renderContextValue(renderContextAttrConst.user);
	
	if(user == null)
		throw new Error("get user unsupport");
	
	return user;
};

/**
 * 销毁看板，销毁所有看板表单、所有图表。
 * 销毁中的看板处于this.statusDestroying()状态，看板完成后处于this.statusDestroyed()状态。
 * 
 * @returns true 正常执行销毁；false 未执行销毁，因为看板处于销毁非法状态
 */
dashboardProto.destroy = function()
{
	if(!this.isAlive() || this.statusDestroying() || this.statusDestroyed())
		return false;
	
	this.statusDestroying(true);
	
	var doDestroy = true;
	
	var listener = this.listener();
	if(listener && listener.onDestroy)
		doDestroy = listener.onDestroy(this);
	
	if(doDestroy !== false)
	{
		this.doDestroy();
	}
	
	return true;
};

/**
 * 执行看板销毁。
 */
dashboardProto.doDestroy = function()
{
	if(!this.statusDestroying())
		throw new Error("dashboard is illegal state for : doDestroy()");
	
	this.stopHandleCharts();
	this._destroyCharts();
	this._destroyForms();
	
	this.statusDestroyed(true);
};

dashboardProto._destroyCharts = function()
{
	var charts = this.charts();
	
	for(let i=0; i<charts.length; i++)
	{
		this._destroyChart(charts[i]);
	}
};

dashboardProto._destroyChart = function(chart)
{
	try
	{
		chart.destroy();
	}
	catch(e)
	{
		CF.logException(e);
	}
};

dashboardProto._destroyForms = function()
{
	var forms = CF.elesOfSelector("form[dg-dashboard-form]");
	
	forms.forEach((form) =>
	{
		this._destroyForm(form);
	});
	
	var globalTheme = CF.renderContextChartTheme(this.renderContext());
	CF.removeThemeRefEntity(globalTheme, DF.THEME_REF_DASHBOARD_FORM_ID);
};

dashboardProto._destroyForm = function(form)
{
	try
	{
		CF.chartTool.destroyDataSetParamForm(form);
	}
	catch(e)
	{
		CF.logException(e);
	}
};

/**
 * 看板是否是活着的（已执行渲染且未完成销毁）。
 */
dashboardProto.isAlive = function()
{
	return (this._isAlive == true);
};

/**
 * 看板是否处于活跃可用的状态（已完成渲染且未执行销毁）。
 */
dashboardProto.isActive = function()
{
	return (this._isActive == true);
};

/**
 * 获取/设置看板状态。
 * 注意：此函数的设置操作仅设置状态值，不执行任何其他逻辑，设置看板生命周期状态应使用具体的this.status*(true)函数。
 * 
 * @param status 可选，要设置的状态，不设置则执行获取操作
 */
dashboardProto.status = function(status)
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
 */
dashboardProto.statusPreInit = function(set)
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
 */
dashboardProto.statusIniting = function(set)
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
 */
dashboardProto.statusInited = function(set)
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
 */
dashboardProto.statusRendering = function(set)
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
 * @param postProcess 可选，当是设置操作时，是否执行后置操作，比如调用监听器的render函数，默认为：true
 */
dashboardProto.statusRendered = function(set, postProcess)
{
	if(set === true)
	{
		this._isActive = true;
		this._isAlive = true;
		this.status(dashboardStatusConst.RENDERED);
		
		if(postProcess !== false)
			this._postProcessRendered();
	}
	else
		return (this.status() == dashboardStatusConst.RENDERED);
};

/**
 * 渲染完成后置处理。
 */
dashboardProto._postProcessRendered = function()
{
	var listener = this.listener();
	if(listener && listener.rendered)
		listener.rendered(this);
};

/**
 * 看板是否为/设置为：正在销毁。
 * 
 * @param set 可选，为true时设置状态；否则，判断状态
 */
dashboardProto.statusDestroying = function(set)
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
 * @param postProcess 可选，当是设置操作时，是否执行后置操作，比如调用监听器的destroy函数，默认为：true
 */
dashboardProto.statusDestroyed = function(set, postProcess)
{
	if(set === true)
	{
		this._isActive = false;
		this._isAlive = false;
		this.status(dashboardStatusConst.DESTROYED);
		
		if(postProcess !== false)
			this._postProcessDestroyed();
	}
	else
		return (this.status() == dashboardStatusConst.DESTROYED);
};

dashboardProto._postProcessDestroyed = function()
{
	var listener = this.listener();
	if(listener && listener.destroyed)
		listener.destroyed(this);
};

/**
 * 为以"/"开头的URL添加系统根路径前缀，否则，将直接返回原URL。
 * 当需要访问系统内其他功能模块的资源时，应为其URL添加系统根路径前缀。
 * 
 * @param url 可选，要处理的URL
 * @return 添加后的新URL，如果没有url参数，将返回系统根路径
 */
dashboardProto.contextURL = function(url)
{
	var renderContext = this.renderContext();
	var contextPath = CF.renderContextWebContextPath(renderContext);
	return CF.toWebContextPathURL(contextPath, url);
};

/**
 * 销毁元素内（包括自身）包含的所有看板表单。
 * 
 * @param form HTML元素
 */
dashboardProto.destroyForm = function(form)
{
	this._destroyForm(form);
};

/**
 * 获取版本。
 * 具体参考：org.datagear.web.analysis.DashboardApiVersion
 * 
 * @return 版本号，目前只有：1.0、2.0
 */
dashboardProto.version = function()
{
	return this._root.version;
};

/**
 * 获取指定元素内（包括自身）包含的所有图表。
 *
 * @param element HTML元素、HTML元素ID
 * @param active 可选，是否仅返回已完成渲染且未执行销毁的图表，true 是；false 否。默认值：false
 * @return [ ... ]
 */
dashboardProto.chartsIn = function(element, active)
{
	element = (CF.isString(element) ? CF.eleOfId(element) : element);
	active = (active == null ? false : active);
	
	var re = [];
	
	var eles = CF.elesOfSelector("[id]", element);
	eles.unshift(element);
	
	eles.forEach((ele) =>
	{
		let id = CF.eleAttr(ele, "id");
		let chart = (CF.isEmpty(id) ? null : this.chartOf(id));
		
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
 * @param element HTML元素、HTML元素ID
 * @return 已调整尺寸的图表数组：[ ... ]
 */
dashboardProto.resizeChartsIn = function(element)
{
	var charts = this.chartsIn(element, true);
	
	charts.forEach((chart) =>
	{
		chart.resize();
	});
	
	return charts;
};

//-------------
// < 已弃用函数 start
//-------------

//-------------
// > 已弃用函数 end
//-------------

//----------------------------------------
// Dashboard prototype end
//----------------------------------------

/**
 * 获取POST JSON的fetch选项
 */
DF.fetchOptsOfPostJson = function(data)
{
	var re =
	{
		method: "POST",
		cache: "no-cache",
		credentials: "same-origin",
		headers: { "Content-Type": "application/json" },
		body: (data == null ? undefined : JSON.stringify(data))
	};
	
	return re;
};

/**
 * 获取POST FormData的fetch选项
 */
DF.fetchOptsOfPostForm = function(formData)
{
	formData = (formData == null ? new FormData() : formData);
	
	var re =
	{
		method: "POST",
		cache: "no-cache",
		credentials: "same-origin",
		body: formData
	};
	
	return re;
};

DF.msgOfResponse = function(response)
{
	return (response.statusText ? response.statusText : response.status+"");
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
DF.addBuiltinChartMaps = function(chartMaps)
{
	var ukChartMapNames = (DF._uniqueBuiltinChartMapNames || (DF._uniqueBuiltinChartMapNames = {}));
	
	for(let i=0; i<chartMaps.length; i++)
	{
		let cm = chartMaps[i];
		let names = cm.names;
		let adcodeInNames = (cm.adcode ? false : true);
		
		for(let j=0; j<names.length; j++)
		{
			let name = names[j];
			
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
 *   //地图名，可用于chartSupport中的MAP_NAME_OPTION_NAME图表选项的名称
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
DF.getStdBuiltinChartMapTree = function(listener)
{
	var re = [];
	
	var nodeCache = {};
	
	for(let i=0; i<builtinChartMaps.length; i++)
	{
		let bcm = builtinChartMaps[i];
		
		if(!bcm.adname || !bcm.adcode)
			continue;
		
		//DF.addBuiltinChartMaps()函数已经确保了adcode可以用作地图名
		//而且它是全局唯一的，最合适
		let node = { mapName: bcm.adcode, mapLabel: bcm.adname };
		let parentNode = (bcm.parent ? nodeCache[bcm.parent] : null);
		
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
 *   //地图名，可用于chartSupport中的MAP_NAME_OPTION_NAME图表选项的名称
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
DF.getStdBuiltinChartMapArray = function(listener)
{
	var re = [];
	
	for(let i=0; i<builtinChartMaps.length; i++)
	{
		let bcm = builtinChartMaps[i];
		
		if(!bcm.adname || !bcm.adcode)
			continue;
		
		//DF.addBuiltinChartMaps()函数已经确保了adcode可以用作地图名
		//而且它是全局唯一的，最合适
		let node = { mapName: bcm.adcode, mapLabel: bcm.adname };
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

DF.addBuiltinChartMaps(dftBuiltinChartMaps);

})(this, window);
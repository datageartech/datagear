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
 * 此看板工厂支持为<body>元素、图表元素添加elementAttrConst.UPDATE_GROUP属性，用于设置图表更新ajax分组。
 * 
 * 此看板工厂扩展了图表监听器功能，支持为图表监听器添加如下处理函数：
 * {
 *   //可选，加载数据前置回调函数
 *   onFetch: function(chart, chartQuery){ ... },
 *   //可选，更新数据出错回调函数
 *   fetchError: function(chart, error){ ... }
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
var builtinMaps = (DF.builtinMaps || (DF.builtinMaps = []));

var builtinMapBaseURL = (DF.builtinMapBaseURL || (DF.builtinMapBaseURL = "/static/analysislib/geojson/"));

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

/**图表更新分组*/
elementAttrConst.UPDATE_GROUP = "dg-chart-update-group";

/**图表手动渲染*/
elementAttrConst.MANUAL_RENDER = "dg-chart-manual-render";

/**本地图表*/
elementAttrConst.LOCAL = "dg-chart-local";

//----------------------------------------
// elementAttrConst结束
//----------------------------------------

//----------------------------------------
// renderContextAttrConst开始
//----------------------------------------

//org.datagear.web.analysis.RenderContextAttrs
renderContextAttrConst.DASHBOARD_THEME = "DG_DASHBOARD_THEME";
renderContextAttrConst.USER = "DG_USER";
renderContextAttrConst.LOCALE = "DG_LOCALE";
renderContextAttrConst.FETCH_DATA_URL ="DG_FETCH_DATA_URL";
renderContextAttrConst.LOAD_CHART_URL = "DG_LOAD_CHART_URL";
renderContextAttrConst.HEARTBEAT_URL = "DG_HEARTBEAT_URL";
renderContextAttrConst.UNLOAD_URL = "DG_UNLOAD_URL";
renderContextAttrConst.SESSION_NAME = "DG_SESSION_NAME";
renderContextAttrConst.SESSION_VALUE = "DG_SESSION_VALUE";

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
			interval: 1000 * 60 * 5
		});

/**
 * 卸载配置，需与后台保持一致。
 */
DF.unloadConfig = (DF.unloadConfig ||
		{
			//org.datagear.web.controller.DashboardVisualController.UNLOAD_PARAM_DASHBOARD_ID
			dashboardIdParamName: "dashboardId"
		});

/**
 * 循环监视处理图表状态间隔毫秒数。
 */
DF.HANDLE_CHART_INTERVAL_MS = 10;

/**
 * 浏览器初始化到此看板工厂JS的时间戳。
 */
DF.LOAD_TIME = new Date().getTime();

/**图表主题关联的看板表单实体ID*/
DF.THEME_REF_DASHBOARD_FORM_ID = "DG_REF_DASHBOARD_FORM_ID";

/**图表渲染器附加属性：默认联动事件类型，默认值为："click" */
DF.RENDERER_ADDITION_DTF_LINK_EVENT_TYPE = "defaultLinkEventType";

/**
 * 图表渲染器附加属性：图表联动源数据处理函数，支持格式：
 * 索引数值、function(type){ return 索引数值、图表事件处理函数; }
 * 
 * 其中：
 * 索引数值，表示图表事件处理函数对应索引数值的参数是联动源数据
 * 图表事件处理函数，表示此函数的返回值是图表联动源数据，返回值格式应为：{ ... }、[ {...}, ... ]
 * 
 * 默认值为：DF.resolveChartLinkData()函数
 */
DF.RENDERER_ADDITION_LINK_DATA_HANDER = "linkDataHander";

/**
 * 创建看板实例，为其添加看板API。
 * 
 * @param root 看板根对象，格式参考DF.Dashboard()
 * @returns 新看板实例
 */
DF.create = function(root)
{
	if(CF.isEmpty(root.id))
		throw new Error("[id] required");
	
	if(root.renderContext == null)
		throw new Error("[renderContext] required");
	
	DF.initRenderContext(root.renderContext);
	DF.startHeartBeat(root.renderContext, root.id);
	
	var dashboard = new DF.Dashboard(root);
	dashboard._statusPreInit(true);
	
	return dashboard;
};

DF.initRenderContext = function(renderContext)
{
	var chartTheme = CF.renderContextChartTheme(renderContext);
	if(!chartTheme)
	{
		var dashboardTheme = CF.renderContextValue(renderContext, renderContextAttrConst.DASHBOARD_THEME);
		chartTheme = (dashboardTheme && dashboardTheme.chartTheme ? dashboardTheme.chartTheme : {});
		CF.renderContextChartTheme(renderContext, chartTheme);
	}
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
		charts[i] = DF.createChart(charts[i], root.renderContext, this);
	}
	
	var localCharts = DF.createLocalCharts(root.renderContext, this);
	for(let i=0; i<localCharts.length; i++)
	{
		charts.push(localCharts[i]);
	}
};

//Dashboard类原型
var dashboardProto = DF.Dashboard.prototype;

DF.createChart = function(chartRoot, renderContext, dashboard)
{
	chartRoot.renderContext = renderContext;
	var chart = CF.create(chartRoot);
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
	
	var contextPath = CF.renderContextContextPath(renderContext);
	var heartbeatURL = CF.renderContextValNonNull(renderContext, renderContextAttrConst.HEARTBEAT_URL);
	heartbeatURL = CF.toContextPathURL(contextPath, heartbeatURL);
	
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

//创建页面内的全部本地图表
DF.createLocalCharts = function(renderContext, dashboard)
{
	var re = [];
	
	var elesWithLocal = DF.elesWithLocal(document.body);
	var eles = elesWithLocal.elements;
	var locals = elesWithLocal.locals;
	
	for(let i=0; i<eles.length; i++)
	{
		let ele = eles[i];
		let chartRoot = locals[i];
		let localChart = DF.createLocalChart(ele, chartRoot, renderContext, dashboard);
		re.push(localChart);
	}
	
	return re;
};

//创建本地图表
DF.createLocalChart = function(ele, chartRoot, renderContext, dashboard)
{
	let elementId = CF.eleAttr(ele, "id");
	if(CF.isEmpty(elementId))
	{
		elementId = "localchart" + CF.uid();
		CF.eleAttr(ele, "id", elementId);
	}
	
	if(CF.isEmpty(chartRoot.id))
		chartRoot.id = elementId;
	
	if(CF.isEmpty(chartRoot.name))
		chartRoot.name = "";
		
	chartRoot.elementId = elementId;
	
	var chart = DF.createChart(chartRoot, renderContext, dashboard);
	chart._local = true;
	
	return chart;
};

//----------------------------------------
// Chart prototype start
//----------------------------------------

//Chart类原型
var chartProto = CF.Chart.prototype;

/**
 * 是否是本地图表。
 */
chartProto.isLocal = function()
{
	return (this._local == true);
};

/**
 * 获取/设置图表所属的看板。
 * 
 * @param dashboard 可选，要设置的看板
 */
chartProto.dashboard = function(dashboard)
{
	if(arguments.length == 0)
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
	this._initUpdateGroup();
	this._initForPostSuper();
};

//重写chart._mergeListener()函数
chartProto._mergeListenerSuper = chartProto._mergeListener;
chartProto._mergeListener = function(localListener, globalListener)
{
	var mergedListener = this._mergeListenerSuper(localListener, globalListener);
	mergedListener._addFunc("onFetch");
	mergedListener._addFunc("fetchError");
	
	return mergedListener;
};

//重写chart._postProcessRendered()函数
chartProto._postProcessRenderedSuper = chartProto._postProcessRendered;
chartProto._postProcessRendered = function()
{
	this.bindLinkEventHanders(this.links());
	this._postProcessRenderedSuper();
};

/**
 * 初始化图表联动配置。
 * 此方法从图表元素的elementAttrConst.LINK属性获取联动配置。
 */
chartProto._initLinks = function()
{
	var links = CF.eleAttr(this._eleNonNull(), elementAttrConst.LINK);
	links = (links ? CF.evalSilently(links) : null);
	
	this.links(links);
};

/**
 * 初始化图表取数分组。
 * 此方法从body元素、图表元素的elementAttrConst.UPDATE_GROUP属性获取更新分组设置。
 */
chartProto._initUpdateGroup = function()
{
	var updateGroup = CF.eleAttr(this._eleNonNull(), elementAttrConst.UPDATE_GROUP);
	
	if(CF.isEmpty(updateGroup))
		updateGroup = CF.eleAttr(document.body, elementAttrConst.UPDATE_GROUP);
	
	this.updateGroup(updateGroup);
};

/**
 * 获取/设置初始图表联动配置对象数组。
 * 联动配置对象格式为：
 * {
 *   //可选，联动触发事件类型、事件类型数组，格式参考chart.on()函数的type参数，
 *   //默认值参考DF.RENDERER_ADDITION_DTF_LINK_EVENT_TYPE说明
 *   trigger: ...、[ ... ],
 *   
 *   //可选，联动目标图表元素ID、ID数组
 *   target: "..."、["...", ...],
 * 	 
 * 	 //可选，原始联动源数据（由图表渲染器的DF.RENDERER_ADDITION_LINK_DATA_HANDER所决定）的属性路径，作为下述【数据属性名】的统一根属性名
 * 	 root: "..."、[ "...", ... ],
 *   
 *   //可选，联动数据参数映射表
 *   data:
 *   {
 *     //数据属性名：图表渲染器的DF.RENDERER_ADDITION_LINK_DATA_HANDER所决定的联动源数据（受root配置影响）的属性路径（比如："name"、"data.value"、"[0].name"），其值将作为目标图表数据集参数值
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
 * @param links 可选，要设置的图表联动配置对象、数组，没有则执行获取操作。
 */
chartProto.links = function(links)
{
	if(arguments.length == 0)
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
 * 获取/设置图表取数分组。
 * 如果图表从服务端加载数据比较耗时，可以为其指定一个分组标识，让其使用单独的ajax请求加载数据。
 * 注意：相同分组的图表将使用同一个ajax请求。
 * 
 * 图表初始化时会使用图表元素的"dg-chart-update-group"属性值执行设置操作。
 * 
 * @param group 可选，设置取数分组，没有则执行获取操作返回非null值。
 */
chartProto.updateGroup = function(group)
{
	if(arguments.length == 0)
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
 * 为指定图表联动配置绑定事件处理函数。
 * 
 * 图表渲染器实现相关：
 * 图表渲染器应实现on()函数、DF.RENDERER_ADDITION_LINK_DATA_HANDER附加属性（可选），以支持此特性。
 * 
 * @param links 图表联动配置对象、数组，格式参考chart.links()函数说明
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
	
	var thisChart = this;
	var triggers = this._resolveLinksTriggers(links);
	var linkDataHander = this.rendererAddition(DF.RENDERER_ADDITION_LINK_DATA_HANDER);
	var isLinkDataHanderFn = (linkDataHander != null && CF.isFunction(linkDataHander));
	
	for(let i=0; i<triggers.length; i++)
	{
		let type = triggers[i];
		let dataHandler = (linkDataHander == null ? null : (isLinkDataHanderFn ? linkDataHander(type) : linkDataHander));
		
		//取默认
		if(dataHandler == null)
			dataHandler = DF.resolveChartLinkData;
		
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

//解析不重复的联动配置触发事件数组。
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
 * 此方法根据图表联动配置对象，将图表联动源数据传递至目标图表数据集参数值，然后请求刷新图表数据。
 * 
 * @param type 事件类型
 * @param linkSrcData 联动数据，格式应为：{...}、[ {...}, ... ]
 * @param links 图表联动配置对象、数组，格式参考chart.links()函数说明
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
		_linkSrcData: linkSrcData,
		getValue: function(name)
		{
			var val = null;
			
			//当name为空时，直接使用this._linkSrcData
			if(CF.isEmpty(name))
			{
				val = this._linkSrcData;
			}
			else if(CF.isArray(this._linkSrcData))
			{
				for(let i=0; i<this._linkSrcData.length; i++)
				{
					//需支持属性路径格式的name
					val = CF.propertyPathValue(this._linkSrcData[i], name);
					
					if(val !== undefined)
						break;
				}
			}
			else
			{
				//需支持属性路径格式的name
				val = CF.propertyPathValue(this._linkSrcData, name);
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
 * 请求从服务端获取并更新图表数据。
 */
chartProto.refresh = function()
{
	this._assertActive();
	
	var unreadys = this.unreadyDataSetParams(true);
	if(unreadys.length > 0)
	{
		throw new Error(
			CF.chartLogInfo(this) + " dataSetBinds["+unreadys[0].dataSetBindIndex
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
	if(arguments.length == 0)
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
	if(arguments.length == 0)
	{
		//注意：此属性不应以chart._initManualRender()的方式初始化，
		//因为看板需要在chart.init()之前就读取它的值
		
		if(this._manualRender != null)
			return (this._manualRender == true);
		else
		{
			var eleValue = CF.eleAttr(this._eleNonNull(), elementAttrConst.MANUAL_RENDER);
			return CF.isLiteralTrue(eleValue);
		}
	}
	else
	{
		this._manualRender = manualRender;
	}
};

//----------------------------------------
// Chart prototype end
//----------------------------------------


//----------------------------------------
// Dashboard prototype start
//----------------------------------------

/**
 * 初始化看板，使用<body>元素上的dg-*属性值初始化看板，使用图表元素上的dg-*属性值初始化看板内所有图表。
 * 此函数在看板生命周期内仅允许调用一次，在dashboard.destroy()后允许再次调用。 
 * 
 * 由于直到此函数调用时，才会读取元素上的dg-*属性，因而元素dg-*属性值引用的变量仅需在此函数调用前定义即可。
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
	
	if(!this._statusPreInit() && !this._statusDestroyed())
		throw new Error("dashboard is illegal state for : init()");
	
	this._statusIniting(true);
	
	this._initRenderContext();
	this._initListener();
	this._initMapURLs();
	this._initChartResizeHandler();
	this._initUnloadDashboardHandler();
	this._initCharts();
	
	this._statusInited(true);
};

/**
 * 初始化渲染上下文。
 */
dashboardProto._initRenderContext = function()
{
	CF.initRenderContext(this.renderContext());
};

/**
 * 初始化地图URL映射表。
 * 它将body元素的elementAttrConst.MAP_URLS属性值设置为地图URL映射表。
 */
dashboardProto._initMapURLs = function()
{
	var mapURLs = {};
	
	for(let i=0; i<builtinMaps.length; i++)
	{
		let bm = builtinMaps[i];
		let names = (CF.isArray(bm.name) ? bm.name : [ bm.name ]);
		let map = bm.map;
		let isRelativeMap = (map.charAt(0) !== '/');
		
		for(let j=0; j<names.length; j++)
		{
			mapURLs[names[j]] = (isRelativeMap ? builtinMapBaseURL+map : map);
		}
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
	if(CF.supportObserveResizeChart())
		return;
	
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
			if(thisDashboard._statusRendered())
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
		CF.RESIZE_CHART_TIMEOUT_MS);
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
		var unloadURL = CF.renderContextValNonNull(renderContext, renderContextAttrConst.UNLOAD_URL);
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
	CF.executeSilently(() =>
	{
		chart.init();
	});
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
	if(arguments.length == 0)
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
	if(arguments.length == 0)
		return chartMapURLs;
	else
		CF.extend(chartMapURLs, mapURLs);
};

/**
 * 获取指定标识的图表，没有则返回null。
 * 
 * @param identity 图表标识信息：图表HTML元素、图表HTML元素ID、图表对象、图表ID、图表索引数值
 */
dashboardProto.chart = function(identity)
{
	var charts = this.charts();
	var index = this._chartIndex(charts, identity);
	
	return (index < 0 ? null : charts[index]);
};

/**
 * 获取指定图表在看板图表数组中的索引号，返回-1表示未找到。
 * 
 * @param identity 图表标识信息：图表HTML元素、图表HTML元素ID、图表对象、图表ID、图表索引数值
 */
dashboardProto.chartIndex = function(identity)
{
	var charts = this.charts();
	return this._chartIndex(charts, identity);
};

/**
 * 获取图表索引，返回-1表示未找到。
 * 
 * @param charts 待查找的图表数组
 * @param identity 图表标识信息：图表HTML元素ID、图表对象、图表ID、图表索引数值、图表HTML元素
 */
dashboardProto._chartIndex = function(charts, identity)
{
	if(charts == null || identity == null)
		return -1;
	
	if(CF.isHtmlEle(identity))
		identity = CF.eleAttr(identity, "id");
	
	for(let i=0; i<charts.length; i++)
	{
		let chart = charts[i];
		
		if(chart === identity || chart.elementId() === identity
				|| i === identity || chart.id() === identity)
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
	var exists = this.chart(chart);
	
	if(exists != null)
		return false;
	
	exists = this.chart(chart.elementId());
	
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
 * @param identity 图表标识信息：图表HTML元素ID、图表对象、图表ID、图表索引数值、图表HTML元素
 * @param doDestroy 选填参数，是否销毁图表，默认为true
 * @return 移除的图表对象，或者图表未找到时为null
 */
dashboardProto.removeChart = function(identity, doDestroy)
{
	var charts = this.charts();
	var index = this._chartIndex(charts, identity);
	
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
	
	if(arguments.length <= 1)
		return CF.renderContextValue(rc, name);
	else
		CF.renderContextValue(rc, name, value);
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
	if(arguments.length == 0)
		return this._resultDataFormat;
	else
		this._resultDataFormat = resultDataFormat;
};

/**
 * 渲染看板。
 * 此函数在看板生命周期内仅允许调用一次，在dashboard.destroy()后允许再次调用。
 * 
 * 注意：当处于this._statusPreInit()时，此函数内部会先调用this.init()函数。
 */
dashboardProto.render = function()
{
	if(this._statusPreInit())
		this.init();
	
	if(!this._statusInited() && !this._statusDestroyed())
		throw new Error("dashboard is illegal state for : render()");
	
	this._statusRendering(true);
	
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
	if(!this._statusRendering())
		throw new Error("dashboard is illegal state for : doRender()");
	
	this._renderForms();
	this._prepareDoRenderCharts();
	this.startHandleCharts();
	
	this._statusRendered(true);
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
	
	forms.forEach((form) =>
	{
		CF.executeSilently(() =>
		{
			this.renderForm(form);
		});
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
 *   link: 图表联动配置对象,
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
 * 图表联动配置对象格式为：
 * {
 *   //必选，联动目标图表元素ID、ID数组
 *   target: "..."、["...", ...],
 *   //可选，联动数据参数映射表
 *   data:
 *   {
 *     表单输入项名称 : 图表数据集参数索引对象、[ 图表数据集参数索引对象, ... ],
 *     ...
 * }
 * 或者，简写为图表联动配置对象的target属性值。
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
 * @param identity 图表标识信息：图表HTML元素ID、图表对象、图表ID、图表索引数值、图表HTML元素
 * @returns 图表对象
 */
dashboardProto.resizeChart = function(identity)
{
	this._assertActive();
	
	var chart = this.chart(identity);
	chart.resize();
	
	return chart;
};

/**
 * 重新调整活跃图表尺寸。
 * 
 * @param charts 可选，要调整的图表数组，如果未设置，则是全部看板图表
 */
dashboardProto.resizeCharts = function(charts)
{
	this._assertActive();
	
	if(charts === undefined)
		charts = this.charts();
	
	if(charts != null)
	{
		this.charts().forEach((chart) =>
		{
			if(chart.isActive())
				chart.resize();
		});
	}
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
	
	var time = CF.currentDateMs();
	var localGroupBundle = { groups: [], groupValues: {} };
	var ajaxGroupBundle = { groups: [], groupValues: {} };
	
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
				this._groupChartQueries(chart, chartQuery, localGroupBundle);
			}
			else
			{
				this._groupChartQueries(chart, chartQuery, ajaxGroupBundle);
			}
		}
	}
	
	for(let i=0; i<localGroupBundle.groups.length; i++)
	{
		let group = localGroupBundle.groups[i];
		let chartQueryPairs = localGroupBundle.groupValues[group];
		
		CF.executeSilently(() =>
		{
			this._doHandleChartsLocal(group, chartQueryPairs);
		});
	}
	
	var fetchDataURL = CF.renderContextValNonNull(this.renderContext(), renderContextAttrConst.FETCH_DATA_URL);
	fetchDataURL = this.contextURL(fetchDataURL);
	
	for(let i=0; i<ajaxGroupBundle.groups.length; i++)
	{
		let group = ajaxGroupBundle.groups[i];
		let chartQueryPairs = ajaxGroupBundle.groupValues[group];
		
		CF.executeSilently(() =>
		{
			this._doHandleChartsAjax(fetchDataURL, group, chartQueryPairs);
		});
	}
};

dashboardProto._groupChartQueries = function(chart, chartQuery, groupBundle)
{
	if(groupBundle.groups == null)
		groupBundle.groups = [];
	
	if(groupBundle.groupValues == null)
		groupBundle.groupValues = {};
	
	let group = chart.updateGroup();
	
	if(CF.indexInArray(groupBundle.groups, group) < 0)
		groupBundle.groups.push(group);
	
	let myCharts = groupBundle.groupValues[group];
	
	if(myCharts == null)
	{
		myCharts = [];
		groupBundle.groupValues[group] = myCharts;
	}
	
	myCharts.push({chart: chart, query: chartQuery});
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
	if(chart.isLocal())
		return true;
	
	//空数据集绑定的也认为是本地图表
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

dashboardProto._doHandleChartsLocal = function(group, chartQueryPairs)
{
	if(CF.isEmpty(chartQueryPairs))
		return;
	
	var updateTime = CF.currentDateMs();
	var charts = this._chartsOfChartQueryPairs(chartQueryPairs);
	var dashboardQueryForm = this._buildDashboardQueryForm(chartQueryPairs);
	var dashboardQuery = this._dashboardQueryOfForm(dashboardQueryForm);
	// 加载上下文对象，使用此上下文对象可以简化回调函数参数，也易于扩展
	var fetchContext =
	{
		group: group,
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
	if(CF.isEmpty(chartQueryPairs))
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
		let chart = this.chart(chartId);
		
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
		let chart = this.chart(chartId);
		
		if(!chart)
			continue;
		
		CF.executeSilently(() =>
		{
			let error = (chartErrors[chartId] || { type: "Error", message: "error" });
			this._handleChartAjaxError(chart, error, true);
		});
	}
};

dashboardProto._handleChartsAjaxError = function(fetchContext, error)
{
	fetchContext.success = false;
	
	var charts = fetchContext.charts;
	var errorMsg = (error && error.message ? error.message : "error");
	var logException = true;
	
	for(let i=0; i<charts.length; i++)
	{
		let chart = charts[i];
		
		CF.executeSilently(() =>
		{
			//结构同：org.datagear.analysis.support.ChartResultErrorMessage
			let error = { type: "Error", message: errorMsg };
			this._handleChartAjaxError(chart, error, false);
		});
	}
	
	if(logException)
	{
		CF.logException("fetch charts data error : " + errorMsg);
	}
};

dashboardProto._handleChartAjaxError = function(chart, error, logIfNone)
{
	this._handleChartResultError(chart, error, true, logIfNone);
};

/**
 * 处理图表结果错误。
 * 
 * @param chart 图表对象
 * @param error 图表结果错误信息对象，结构参考：org.datagear.analysis.support.ChartResultErrorMessage
 * @param setErrorStatus 是否将图表状态更新为：chartStatusConst.UPDATE_ERROR
 * @param logIfNone 可选，是否在chart.listener()未定义fetchError()函数时打印错误日志，默认为：true
 */
dashboardProto._handleChartResultError = function(chart, error, setErrorStatus, logIfNone)
{
	logIfNone = (logIfNone === undefined ? true : logIfNone);
	
	if(!chart)
		return;
	
	if(setErrorStatus)
	{
		chart.status(chartStatusConst.UPDATE_ERROR);
	}
	
	var chartListener = chart.listener();
	if(chartListener && chartListener.fetchError)
	{
		chartListener.fetchError(chart, error);
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
 * @param chartQuery 图表结果对应的查询信息
 * @param force 可选，是否强制更新，默认值：false
 */
dashboardProto._updateChart = function(chart, chartResult, chartQuery, force)
{
	force = (force === undefined ? false : force);
	
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
			throw new Error(CF.chartLogInfo(chart) + " not active");
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
	
	if(arguments.length <= 2)
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
	
	if(arguments.length <= 1)
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
 * @param element 用于渲染图表的<div>元素、元素ID
 * @param chartWidgetId 选填，要加载的图表部件ID，如果不设置，将从元素的"dg-chart-widget"属性取
 * @param successCallback 选填，图表加载成功回调函数：function(chart){ ... }，返回false图表将不会加入看板
 * @param errorCallback 选填，图表加载失败回调函数：function(error){ ... }
 */
dashboardProto.loadChart = function(element, chartWidgetId, successCallback, errorCallback)
{
	//异步加载无需看板已渲染
	//this._assertAlive();
	
	element = (CF.isString(element) ? CF.eleOfId(element) : element);
	
	if(!CF.isChartTagName(element))
		throw new Error("load chart element must be : <"+CF.CHART_TAG_NAME+">");
	
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
 * @param elements 用于渲染图表的<div>元素选择器字符串、<div>元素数组
 * @param chartWidgetIds 可选，要加载的图表部件ID数组，如果不设置，将从元素的"dg-chart-widget"属性取
 * @param successCallback 选填，图表加载成功回调函数：function(charts){ ... }，返回false图表将不会加入看板
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
		if(!CF.isChartTagName(elements[i]))
			throw new Error("load chart "+(i+1)+"-th element must be : <"+CF.CHART_TAG_NAME+">");
		
		newChartWidgetIds[i] = (chartWidgetIds == null ? null : chartWidgetIds[i]);
		
		if(CF.isEmpty(newChartWidgetIds[i]))
			newChartWidgetIds[i] = CF.elementWidgetId(elements[i]);
	}
	
	this._loadCharts(elements, newChartWidgetIds, successCallback, errorCallback);
};

/**
 * 将元素内（包括<div>自身）所有设置了"dg-chart-widget"属性，且未初始化为图表的<div>元素异步加载为图表。
 * 如果没有需要加载的元素，将不会执行异步请求。
 * 
 * 支持调用方式：
 * dashboard.loadUnsolvedCharts();
 * dashboard.loadUnsolvedCharts(element);
 * dashboard.loadUnsolvedCharts(successCallback);
 * dashboard.loadUnsolvedCharts(element, successCallback);
 * 
 * @param elements 可选，限定查找的根HTML元素选择器字符串、根HTML元素数组、根HTML元素，默认为：<body>元素
 * @param successCallback 选填，图表加载成功回调函数：function(charts){ ... }，返回false图表将不会加入看板
 * @param errorCallback 选填，图表加载失败回调函数：function(error){ ... }
 * @return 要异步加载的HTML元素数组
 */
dashboardProto.loadUnsolvedCharts = function(elements, successCallback, errorCallback)
{
	//异步加载无需看板已渲染
	//this._assertAlive();
	
	// (successCallback)
	if(CF.isFunction(elements))
	{
		errorCallback = successCallback
		successCallback = elements;
		elements = null;
	}
	
	elements = (elements == null ? [ document.body ] :
				(CF.isString(elements) ? CF.elesOfSelector(elements) :
					(CF.isArray(elements) ? elements : [ elements ])));
	
	var unsolvedEles = [];
	var unsolvedWidgetIds = [];
	
	for(let i=0; i<elements.length; i++)
	{
		let eleWidgetIdInfo = CF.elesWithWidgetId(elements[i]);
		for(let j=0; j<eleWidgetIdInfo.elements.length; j++)
		{
			let ele = eleWidgetIdInfo.elements[j];
			let widgetId = eleWidgetIdInfo.widgetIds[j];
			
			if(this._loadingChartElement(ele))
				continue;
			
			if(this.renderedChart(ele) != null)
				continue;
			
			//看板中可能存在对应此元素的已初始化但是未渲染的图表，这里也要排除
			if(this.chart(ele) != null)
				continue;
			
			unsolvedEles.push(ele);
			unsolvedWidgetIds.push(widgetId);
		}
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
 * @param successCallback 选填，加载成功回调函数：function(charts){ ... }，返回false图表将不会加入看板
 * @param errorCallback 选填，加载失败回调函数：function(error){ ... }
 */
dashboardProto._loadCharts = function(elements, chartWidgetIds, successCallback, errorCallback)
{
	var elementsLen = elements.length;
	for(let i=0; i<elementsLen; i++)
	{
		let element = elements[i];
		let chartWidgetId = chartWidgetIds[i];
		
		if(CF.isEmpty(chartWidgetId))
			throw new Error((elementsLen > 1 ? "the "+(i+1)+"-th " : "")
				+ "[chartWidgetId] required");
		
		if(this._loadingChartElement(element))
			throw new Error((elementsLen > 1 ? "the "+(i+1)+"-th " : "")
				+ "element is loading chart");
		
		if(this.renderedChart(element) != null)
			throw new Error((elementsLen > 1 ? "the "+(i+1)+"-th " : "")
				+ "element has a rendered chart");
		
		//看板中可能存在已初始化但是未渲染的图表，也不应允许异步加载
		if(this.chart(element) != null)
			throw new Error((elementsLen > 1 ? "the "+(i+1)+"-th " : "")
				+ "element has a bounded chart");
	}
	
	var loadChartURL = CF.renderContextValNonNull(this.renderContext(), renderContextAttrConst.LOAD_CHART_URL);
	loadChartURL = this.contextURL(loadChartURL);
	var loadChartConfig = DF.loadChartConfig;
	
	var formData = new FormData();
	formData.append(loadChartConfig.dashboardIdParamName, this.id());
	for(let i=0; i<chartWidgetIds.length; i++)
		formData.append(loadChartConfig.chartWidgetIdParamName, chartWidgetIds[i]);
	
	this._loadingChartElement(elements, true);
	
	fetch(loadChartURL, DF.fetchOptsOfPostForm(formData))
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
				charts[i] = this._createLoadedChart(chartRoots[i], elements[i], chartWidgetIds[i]);
			}
			
			let add = true;
			
			if(successCallback != null)
				add = successCallback(charts);
			
			if(add !== false)
			{
				for(let i=0; i<chartRoots.length; i++)
					this._addLoadedChart(charts[i]);
			}
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
	
	if(arguments.length <= 1)
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
 * 创建异步加载的图表。
 * 
 * @param chartRoot 图表根对象
 * @param element 图表HTML元素
 * @param chartWidgetId 图表部件ID
 */
dashboardProto._createLoadedChart = function(chartRoot, element, chartWidgetId)
{
	//这里不应设置"dg-chart-widget"属性而破坏了元素的原生结构
	//CF.elementWidgetId(element, chartWidgetId);
	
	var eleId = CF.checkSetChartElementId(element);
	chartRoot.elementId = eleId;
	
	return DF.createChart(chartRoot, this.renderContext(), this);
};

dashboardProto._addLoadedChart = function(chart)
{
	this.addChart(chart);
	
	if(chart.manualRender())
		return;
	
	if(chart.statusPreInit())
	{
		//应设为与看板状态保持一致
		if(this._statusInited())
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
 * @param sourceData 源数据，格式支持：{ ... }、[ ... ]、{ getValue: function(name){ return ...; } }（需支持属性路径）
 * @param batchSet 批量设置对象，格式为：
 * 					{
 * 					  //可选，要设置的目标图表元素ID、图表ID、看板图表数组索引，或者它们的数组
 * 					  target: "..."、["...", ...],
 * 					  
 * 					  //可选，下述【源数据属性名】的统一根前缀，末尾无需带'.'字符
 * 					  root: "..."、[ "...", ... ],
 * 					  
 * 					  //可选，要设置的参数值映射表，没有则不设置任何参数值
 * 					  data:
 * 					  {
 * 					    源数据属性名: 图表数据集参数索引对象、[ 图表数据集参数索引对象, ... ],
 * 					    ...
 * 					  }
 * 					}
 * 					上述【源数据属性名】可以是源数据中的简单属性名（比如："name"、"value"），也可以是属性路径（比如："order.name"、"[0].name"、"['order'].product.name"）。
 * 					【图表数据集参数索引对象】用于确定【源数据属性名】对应值要设置到的目标图表数据集参数，格式为：
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
		targetCharts[i] = this.chart(targets[i]);
		
		if(targetCharts[i] == null)
			throw new Error("no chart found for : " + targets[i]);
	}
	
	var dataMap = (batchSet.data || {});
	var hasGetValueFunc = CF.isFunction(sourceData.getValue);
	var propPathRoots = (CF.isEmpty(batchSet.root) ? [""] : (CF.isArray(batchSet.root) ? batchSet.root : [ batchSet.root ]));
	
	var sourceValueContextArgs = [ "place-holder-for-source-value" ];
	sourceValueContextArgs = sourceValueContextArgs.concat(CF.isArray(sourceValueContext) ? sourceValueContext : [ sourceValueContext ]);
	
	for(let name in dataMap)
	{
		let dataValue = null;
		
		for(let i=0; i<propPathRoots.length; i++)
		{
			let propPath = CF.concatPropertyPath(propPathRoots[i], name);
			
			if(hasGetValueFunc)
			{
				dataValue = sourceData.getValue(propPath);
			}
			else
			{
				//当name为空时，应直接使用sourceData
				if(CF.isEmpty(propPath))
					dataValue = sourceData;
				else
					dataValue = CF.propertyPathValue(sourceData, propPath);
			}
			
			if(dataValue != null)
				break;
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
				targetChart = this.chart(chartIdx);
				
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
 * @returns 用户信息，格式参考：org.datagear.web.analysis.AnalysisUser
 */
dashboardProto.user = function()
{
	var user = this.renderContextValue(renderContextAttrConst.USER);
	
	if(user == null)
		throw new Error("get user unsupport");
	
	return user;
};

/**
 * 销毁看板，销毁所有看板表单、所有图表。
 * 
 * @returns true 正常执行销毁；false 未执行销毁，因为看板处于销毁非法状态
 */
dashboardProto.destroy = function()
{
	if(!this.isAlive() || this._statusDestroying() || this._statusDestroyed())
		return false;
	
	this._statusDestroying(true);
	
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
	if(!this._statusDestroying())
		throw new Error("dashboard is illegal state for : doDestroy()");
	
	this.stopHandleCharts();
	this._destroyCharts();
	this._destroyForms();
	
	this._statusDestroyed(true);
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
	CF.executeSilently(() =>
	{
		chart.destroy();
	});
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
	CF.executeSilently(() =>
	{
		CF.chartTool.destroyDataSetParamForm(form);
	});
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
 * 注意：此函数的设置操作仅设置状态值，不执行任何其他逻辑，设置看板生命周期状态应使用具体的this._status*(true)函数。
 * 
 * @param status 可选，要设置的状态，不设置则执行获取操作
 */
dashboardProto._status = function(status)
{
	if(arguments.length == 0)
	{
		if(this.__status == null)
			this.__status = "";
		
		return this.__status;
	}
	else
	{
		if(status == null)
			throw new Error("[status] required");
		
		this.__status = status;
	}
};

/**
 * 看板是否为/设置为：准备初始化。
 * 
 * @param set 可选，为true时设置状态；否则，判断状态
 */
dashboardProto._statusPreInit = function(set)
{
	if(set === true)
	{
		this._isActive = false;
		this._isAlive = false;
		this._status(dashboardStatusConst.PRE_INIT);
	}
	else
		return (this._status() == dashboardStatusConst.PRE_INIT);
};

/**
 * 看板是否为/设置为：正在初始化。
 * 
 * @param set 可选，为true时设置状态；否则，判断状态
 */
dashboardProto._statusIniting = function(set)
{
	if(set === true)
	{
		this._isActive = false;
		this._isAlive = false;
		this._status(dashboardStatusConst.INITING);
	}
	else
		return (this._status() == dashboardStatusConst.INITING);
};

/**
 * 看板是否为/设置为：完成初始化。
 * 
 * @param set 可选，为true时设置状态；否则，判断状态
 */
dashboardProto._statusInited = function(set)
{
	if(set === true)
	{
		this._isActive = false;
		this._isAlive = false;
		this._status(dashboardStatusConst.INITED);
	}
	else
		return (this._status() == dashboardStatusConst.INITED);
};

/**
 * 看板是否为/设置为：渲染中。
 * 
 * @param set 可选，为true时设置状态；否则，判断状态
 */
dashboardProto._statusRendering = function(set)
{
	if(set === true)
	{
		this._isActive = false;
		this._isAlive = true;
		this._status(dashboardStatusConst.RENDERING);
	}
	else
		return (this._status() == dashboardStatusConst.RENDERING);
};

/**
 * 看板是否为/设置为：完成渲染。
 * 
 * @param set 可选，为true时设置状态；否则，判断状态
 * @param postProcess 可选，当是设置操作时，是否执行后置操作，比如调用监听器的render函数，默认为：true
 */
dashboardProto._statusRendered = function(set, postProcess)
{
	if(set === true)
	{
		this._isActive = true;
		this._isAlive = true;
		this._status(dashboardStatusConst.RENDERED);
		
		if(postProcess !== false)
			this._postProcessRendered();
	}
	else
		return (this._status() == dashboardStatusConst.RENDERED);
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
dashboardProto._statusDestroying = function(set)
{
	if(set === true)
	{
		this._isActive = false;
		this._isAlive = true;
		this._status(dashboardStatusConst.DESTROYING);
	}
	else
		return (this._status() == dashboardStatusConst.DESTROYING);
};

/**
 * 看板是否为/设置为：完成销毁。
 * 
 * @param set 可选，为true时设置状态；否则，判断状态
 * @param postProcess 可选，当是设置操作时，是否执行后置操作，比如调用监听器的destroy函数，默认为：true
 */
dashboardProto._statusDestroyed = function(set, postProcess)
{
	if(set === true)
	{
		this._isActive = false;
		this._isAlive = false;
		this._status(dashboardStatusConst.DESTROYED);
		
		if(postProcess !== false)
			this._postProcessDestroyed();
	}
	else
		return (this._status() == dashboardStatusConst.DESTROYED);
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
	var contextPath = CF.renderContextContextPath(renderContext);
	return CF.toContextPathURL(contextPath, url);
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
 * 获取API版本。
 * 返回值规则同：org.datagear.web.analysis.DashboardApiVersion.trimVersion(String)
 * 
 * @return 版本号
 */
dashboardProto.apiVersion = function()
{
	var v = (this._root.apiVersion == null ? null : CF.trim(this._root.apiVersion));
	
	if(v == null || v == "")
		return "1.0";
	else
		return v;
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
	active = (active === undefined ? false : active);
	
	var re = [];
	
	var eles = CF.elesOfSelector("[id]", element);
	eles.unshift(element);
	
	eles.forEach((ele) =>
	{
		let id = CF.eleAttr(ele, "id");
		let chart = (CF.isEmpty(id) ? null : this.chart(id));
		
		if(!chart)
			return;
		
		if(!active || (active && chart.isActive()))
			re.push(chart);
	});
	
	return re;
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
 * 获取<div>元素自身或其子孙<div>元素中带有非空本地图表属性（"dg-chart-local"）的全部元素。
 * 
 * @param ele HTML元素
 * @returns { elements: [ HTML元素, ... ], locals: [ ..., ... ] }
 */
DF.elesWithLocal = function(ele)
{
	var re = { elements: [], locals: [] };
	
	if(ele == null)
		return re;
	
	var local = DF.elementLocalAttr(ele);
	
	if(!CF.isEmpty(local) && CF.isChartTagName(ele))
	{
		local = DF.evalChartLocalValue(local);
		
		if(local != null)
		{
			re.elements.push(ele);
			re.locals.push(local);
		}
	}
	
	var children = CF.elesOfSelector(CF.CHART_TAG_NAME + "["+elementAttrConst.LOCAL+"]", ele);
	
	children.forEach(function(child)
	{
		let childLocal = DF.elementLocalAttr(child);
		if(!CF.isEmpty(childLocal))
		{
			childLocal = DF.evalChartLocalValue(childLocal);
			
			if(childLocal != null)
			{
				re.elements.push(child);
				re.locals.push(childLocal);
			}
		}
	});
	
	return re;
};

/**
 * 获取/设置HTML元素上的本地图表（"dg-chart-local"）属性值。
 * 
 * @param ele HTML元素
 * @param local 选填参数，要设置的本地图表root字符串形式，不设置则执行获取操作
 */
DF.elementLocalAttr = function(ele, local)
{
	if(arguments.length == 1)
	{
		return CF.eleAttr(ele, elementAttrConst.LOCAL);
	}
	else
	{
		CF.eleAttr(ele, elementAttrConst.WIDGET, local);
	}
};

DF.evalChartLocalValue = function(value)
{
	return CF.evalSilently(value);
};

/**
 * 从参数中解析图表联动源数据
 */
DF.resolveChartLinkData = function(event)
{
	var re = null;
	
	if(arguments.length === 0)
	{
		re = null;
	}
	else if(arguments.length === 1)
	{
		re = arguments[0];
	}
	else
	{
		for(let i=0; i<arguments.length; i++)
		{
			let arg = arguments[i];
			
			if(arg != null && typeof(arg) === "object")
			{
				re = arg;
				break;
			}
		}
		
		if(re == null)
			re = arguments[0];
	}
	
	return re;
};

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
 * 注册内置地图。
 * 
 * @param builtinMap 内置地图，格式支持：
					{
						//地图名、数组
					  	name: "..."、["...", ...],
						//地图文件路径，以'/'开头表示绝对路径，否则，表示相对于builtinMapBaseURL内路径
						map: "...",
						//可选，行政区划名称
						adname: "...",
						//可选，行政区划编码
						"adcode": "...",
						//可选，上级行政区划编码
						"parent": "..." 
					}、
					[ { ... }, ... ]
 */
DF.registerBuiltinMap = function(builtinMap)
{
	builtinMap = (CF.isArray(builtinMap) ? builtinMap : [ builtinMap ]);
	
	for(let i=0; i<builtinMap.length; i++)
	{
		let cm = builtinMap[i];
		
		if(cm == null || CF.isEmpty(cm.name) || CF.isEmpty(cm.map))
			continue;
		
		cm = CF.extend(true, {}, builtinMap[i]);
		
		if(cm.adcode != null && cm.adname == null)
			cm.adname = cm.adcode;
		
		//确保cm.name中包含cm.adcode
		if(cm.adcode != null)
		{
			if(CF.isArray(cm.name))
			{
				if(CF.indexInArray(cm.name, cm.adcode) < 0)
					cm.name.push(cm.adcode);
			}
			else
			{
				if(cm.name != cm.adcode)
				{
					cm.name = [ cm.name ];
					cm.name.push(cm.adcode);
				}
			}
		}
		
		builtinMaps.push(cm);
	}
};

/**
 * 获取标准内置地图信息树形结构。
 * 返回一个数组，其中每个元素都可能是树形结构根节点，节点格式为：
 * {
 *   //地图名
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
DF.getStdBuiltinMapTree = function(listener)
{
	var re = [];
	
	var nodeCache = {};
	
	for(let i=0; i<builtinMaps.length; i++)
	{
		let bm = builtinMaps[i];
		
		if(!bm.adname || !bm.adcode)
			continue;
		
		//DF.registerBuiltinMap()函数已经确保了adcode可以用作地图名
		//而且它是全局唯一的，最合适
		let node = { mapName: bm.adcode, mapLabel: bm.adname };
		let parentNode = (bm.parent ? nodeCache[bm.parent] : null);
		let addTo = re;
		
		if(parentNode)
		{
			if(!parentNode.mapChildren)
				parentNode.mapChildren = [];
			
			addTo = parentNode.mapChildren;
		}
		
		//删除旧的
		let existIdx = CF.indexInArrayOfProp(addTo, node.mapName, "mapName");
		if(existIdx >= 0)
			addTo.splice(existIdx, 1);
		
		addTo.push(node);
		
		if(listener && listener.added)
			listener.added(node, parentNode, re);
		
		nodeCache[bm.adcode] = node;
	}
	
	return re;
};

/**
 * 获取标准内置图表地图平铺数组。
 * 返回一个数组，其中元素格式为：
 * {
 *   //地图名
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
DF.getStdBuiltinMapArray = function(listener)
{
	var re = [];
	
	for(let i=0; i<builtinMaps.length; i++)
	{
		let bm = builtinMaps[i];
		
		if(!bm.adname || !bm.adcode)
			continue;
		
		//DF.registerBuiltinMap()函数已经确保了adcode可以用作地图名
		//而且它是全局唯一的，最合适
		let node = { mapName: bm.adcode, mapLabel: bm.adname };
		
		//删除旧的
		let existIdx = CF.indexInArrayOfProp(re, node.mapName, "mapName");
		if(existIdx >= 0)
			re.splice(existIdx, 1);
		
		re.push(node);
		
		if(listener && listener.added)
			listener.added(node, re);
	}
	
	return re;
};

})(this, window);
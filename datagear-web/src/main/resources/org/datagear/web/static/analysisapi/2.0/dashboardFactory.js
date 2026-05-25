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
 * 此看板工厂支持为<body>元素添加elementAttrConst.MAP_HANDLER属性，用于扩展或替换内置地图，格式为：
 * { values: { customMap:'map/custom.json', china: 'map/myChina.json' } }
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

/** 渲染上下文属性名常量 */
var renderContextAttrConst = (CF.renderContextAttrConst || (CF.renderContextAttrConst = {}));

/**看板状态常量*/
var dashboardStatusConst = (DF.dashboardStatusConst || (DF.dashboardStatusConst = {}));

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

/**地图处理器*/
elementAttrConst.MAP_HANDLER = "dg-map-handler";

/**图表联动*/
elementAttrConst.LINK = "dg-chart-link";

/**图表更新分组*/
elementAttrConst.UPDATE_GROUP = "dg-chart-update-group";

/**图表手动渲染*/
elementAttrConst.MANUAL_RENDER = "dg-chart-manual-render";

/**本地图表*/
elementAttrConst.LOCAL = "dg-chart-local";

/**图表数据获取器*/
elementAttrConst.FETCHER = "dg-chart-fetcher";

/**看板数据获取器*/
elementAttrConst.DASHBOARD_FETCHER = "dg-dashboard-fetcher";

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
 * 图表数据获取器值常量：全局
 * 表示使用全局的看板数据获取器处理图表更新
 */
DF.CHART_FETCHER_GLOBAL = "global";

/**
 * 图表数据获取器值常量：空
 * 表示使用空图表结果更新图表，空图表结果为：{ dataSetResults: [] }
 */
DF.CHART_FETCHER_EMPTY = "empty";

/**
 * 图表数据获取器值常量：默认
 * 表示使用看板默认逻辑处理图表更新
 */
DF.CHART_FETCHER_DEFAULT = "default";

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
	
	var heartbeatURL = CF.renderContextValNonNull(renderContext, renderContextAttrConst.HEARTBEAT_URL);
	heartbeatURL = CF.toRenderContextPathURL(renderContext, heartbeatURL);
	
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
	
	var elesWithLocal = DF.elesWithLocal(document.body, true);
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
		elementId = CF.uid() + "ele";
		CF.eleAttr(ele, "id", elementId);
	}
	
	if(CF.isEmpty(chartRoot.id))
		chartRoot.id = CF.uid() + "lc";
	
	if(chartRoot.name == null)
		chartRoot.name = "";
	
	chartRoot.elementId = elementId;
	
	var dataSetBinds = chartRoot.dataSetBinds;
	if(dataSetBinds != null)
	{
		for(let i=0; i<dataSetBinds.length; i++)
		{
			let dsb = dataSetBinds[i];
			let dataSet = (dsb == null ? null : dsb.dataSet);
			
			if(dataSet != null)
			{
				if(CF.isEmpty(dataSet.id))
					dataSet.id = CF.uid() + "dst";
				
				if(dataSet.name == null)
					dataSet.name = "";
			}
		}
	}
	
	var chart = DF.createChart(chartRoot, renderContext, dashboard);
	chart._local = true;
	
	return chart;
};

//异步加载和填充图表插件，读取chart._root.plugin信息（参考org.datagear.analysis.support.html.HtmlChart类），
//加载插件，调用chart.plugin(plugin)设置
DF.inflateChartPlugin = function(charts)
{
	var chartArray = (CF.isArray(charts) ? charts : [ charts ]);
	
	var promise = new Promise((resolve) =>
	{
		//TODO 图表插件改为在这里按需异步加载，以解决目前预先加载全部插件可能带来的问题，
		//比如：某些插件体积过大导致页面加载缓慢、未来添加用户私有插件管理功能而无法预先全部加载等等。
		//注意：图表插件用途为"lib"的仍应预先全部加载，因为chart.loadLib()内部逻辑需要。
		
		for(let i=0; i<chartArray.length; i++)
		{
			let chart = chartArray[i];
			
			if(chart.plugin() == null)
			{
				let chartRoot = chart._root;
				let plugin = chartRoot.plugin;
				let pluginId = (plugin == null ? null : (CF.isString(plugin) ? plugin : plugin.id));
				plugin = (pluginId == null ? null : CF.findPluginById(pluginId));
				chart.plugin(plugin);
			}
		}
		
		resolve(charts);
	});
	
	return promise;
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
	this._initFetcher();
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
 * 初始化图表更新分组。
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
 * 初始化图表数据获取器。
 * 此方法从图表元素的elementAttrConst.FETCHER属性获取图表数据获取器。
 */
chartProto._initFetcher = function()
{
	var fetcher = CF.eleAttr(this._eleNonNull(), elementAttrConst.FETCHER);
	
	if(CF.isEmpty(fetcher))
	{
		fetcher = null;
	}
	else if(fetcher == DF.CHART_FETCHER_GLOBAL || fetcher == DF.CHART_FETCHER_EMPTY
			|| fetcher == DF.CHART_FETCHER_DEFAULT)
	{
		fetcher = fetcher;
	}
	else
	{
		fetcher = CF.evalSilently(fetcher);
	}
	
	this.fetcher(fetcher);
};

/**
 * 获取/设置初始图表联动配置对象数组。
 * 联动配置对象格式为：
 * {
 *   //可选，联动触发事件类型、事件类型数组，格式参考chart.on()函数的type参数，
 *   //默认值参考DF.RENDERER_ADDITION_DTF_LINK_EVENT_TYPE说明
 *   trigger: ...、[ ... ],
 *   
 *   //同dashboard.dataSetParamValueBatched()函数的batchConfig参数的target属性
 *   target: ...,
 * 	 
 * 	 //同dashboard.dataSetParamValueBatched()函数的batchConfig参数的root属性
 * 	 root: ...,
 *   
 *   //同dashboard.dataSetParamValueBatched()函数的batchConfig参数的data属性
 *   //其中的sourceData参考dashboard.bindLinkEventHanders()函数
 *   data: ...
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
	
	if(links == null)
		return false;
	
	if(!CF.isArray(links))
		links = [ links ];
	
	var dashboard = this.dashboard();
	var targetCharts = [];
	var batchSource = function(propPath)
	{
		var val = null;
		
		//当propPath为空时，应直接使用linkSrcData
		if(CF.isEmpty(propPath))
			val = linkSrcData;
		else
			val = CF.propertyPathValue(linkSrcData, propPath);
		
		return val;
	};
	
	for(let i=0; i<links.length; i++)
	{
		let link = links[i];
		
		if(!this._isLinkByEventType(link, type))
			continue;
		
		let myTargetCharts = dashboard.dataSetParamValueBatched(batchSource, link, linkSrcData);
		
		for(let j=0; j<myTargetCharts.length; j++)
		{
			if(CF.indexInArray(targetCharts, myTargetCharts[j]) < 0)
				targetCharts.push(myTargetCharts[j]);
		}
	}
	
	for(let i=0; i<targetCharts.length; i++)
	{
		let chart = targetCharts[i];
		
		CF.executeSilently(function()
		{
			chart.refresh();
		});
	}
};

chartProto._isLinkByEventType = function(link, type)
{
	var triggers = this._resolveLinkTriggers(link);
	return (CF.indexInArray(triggers, type) >= 0);
};

/**
 * 请求一次获取数据并刷新图表。
 * 此函数可以在图表处于任何状态时调用，等到图表处于活跃状态时，会真正发送请求获取数据后更新图表。
 * 调用chart.destroy()后将清除所有等待的刷新请求。
 */
chartProto.refresh = function()
{
	//不限制必须处于激活状态，以支持更多场景
	//this._assertActive();
	
	var unreadys = this.unreadyDataSetParams(true);
	if(unreadys.length > 0)
	{
		let unready0 = unreadys[0];
		throw new Error(
			CF.chartLogInfo(this) + " dataSetBinds["+unready0.dataSetBindIndex
			+"] DataSetParam["+unready0.paramIndex+"]('"+unready0.param.name+"') value required");
	}
	
	//这里不能使用this.statusPreUpdate(true)的方式实现
	//当在A图表监听器的update函数中调用参数化B图表的refresh()时，
	//可能会出现已设置的statusPreUpdate()状态被PARAM_VALUE_REQUIRED状态覆盖的情况，
	//而导致refresh()失效
	
	var chartQuery = this.dashboard()._buildChartQuery(this);
	this._requestRefresh(chartQuery);
};

var REQUEST_REFRESH_LIVE_VALUE_NAME = CF.BUILTIN_PROP_PREFIX + "ReqRefreshes";

chartProto._requestRefresh = function(chartQuery)
{
	if(chartQuery == null)
		return;
	
	var rrds = this.liveValue(REQUEST_REFRESH_LIVE_VALUE_NAME);
	if(rrds == null)
	{
		rrds = [];
		this.liveValue(REQUEST_REFRESH_LIVE_VALUE_NAME, rrds);
	}
	
	rrds.push(chartQuery);
};

chartProto._pollRequestRefreshQuery = function()
{
	let rrds = this.liveValue(REQUEST_REFRESH_LIVE_VALUE_NAME);
	return (rrds == null || rrds.length == 0 ? null : rrds.shift());
};

var UPDATE_TIME_LIVE_VALUE_NAME = CF.BUILTIN_PROP_PREFIX + "UpdateTime";

chartProto._updateTime = function(time)
{
	if(arguments.length == 0)
		return this.liveValue(UPDATE_TIME_LIVE_VALUE_NAME);
	else
		this.liveValue(UPDATE_TIME_LIVE_VALUE_NAME, time);
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

/**
 * 是否是本地图表。
 */
chartProto.isLocal = function()
{
	return (this._local == true);
};

/**
 * 获取/设置图表数据获取器。
 * 
 * @param fetcher 可选，要设置的获取器，格式允许：
 * 					DF.CHART_FETCHER_GLOBAL、
 * 					DF.CHART_FETCHER_EMPTY、
 * 					DF.CHART_FETCHER_DEFAULT、
 * 					//获取函数
 * 					//context 获取上下文，格式为：
 * 					//{
 * 					//  dashboard: 看板对象,
 * 					//  chart: 图表对象,
 * 					//  query: { ... }
 * 					//}
 * 					//其中：
 * 					//query 格式同org.datagear.analysis.ChartQuery
 * 					//此函数应返回ChartResult对象，或者兑现值为ChartResult对象的Promise对象
 * 					function(context){ ... }
 */
chartProto.fetcher = function(fetcher)
{
	if(arguments.length == 0)
	{
		return this._fetcher;
	}
	else
	{
		this._fetcher = fetcher;
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
 * 
 * @returns Promise 兑现时表示初始化已完成，无兑现值
 */
dashboardProto.init = function()
{
	if(!this.id())
		throw new Error("dashboard id required");
	if(!this.renderContext())
		throw new Error("dashboard renderContext required");
	
	var validStatus = (this._statusPreInit() || this._statusDestroyed());
	
	if(!validStatus)
		throw new Error("dashboard is illegal state for : init()");
	
	this._statusIniting(true);
	
	var initPromise = new Promise((resolve) =>
	{
		this._initRenderContext();
		this._initListener();
		this._initMapHandler();
		this._initFetcher();
		this._initChartResizeHandler();
		this._initUnloadDashboardHandler();
		
		var initChartsPromise = this._initCharts();
		initChartsPromise = initChartsPromise.then(() =>
		{
			this._statusInited(true);
		});
		
		resolve(initChartsPromise);
	});
	
	this._initPromise = initPromise;
	return initPromise;
};

/**
 * 初始化渲染上下文。
 */
dashboardProto._initRenderContext = function()
{
	CF.initRenderContext(this.renderContext());
};

/**
 * 初始化地图处理器。
 * 它将body元素的elementAttrConst.MAP_HANDLER属性值设置为地图处理器。
 */
dashboardProto._initMapHandler = function()
{
	var mapHandler = CF.eleAttr(document.body, elementAttrConst.MAP_HANDLER);
	
	if(mapHandler)
		mapHandler = CF.evalSilently(mapHandler, {});
	
	this.mapHandler(mapHandler);
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
 * 初始化看板数据获取器。
 * 此方法从<body>元素的elementAttrConst.DASHBOARD_FETCHER属性获取看板数据获取器。
 */
dashboardProto._initFetcher = function()
{
	var fetcher = CF.eleAttr(document.body, elementAttrConst.DASHBOARD_FETCHER);
	
	if(CF.isEmpty(fetcher))
		fetcher = null;
	else
		fetcher = CF.evalSilently(fetcher);
	
	this.fetcher(fetcher);
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
	var promise = this._inflateChartPlugin(this.charts());
	
	promise = promise.then((charts) =>
	{
		for(let i=0; i<charts.length; i++)
		{
			let chart = charts[i];
			
			if(chart.statusPreInit() || chart.statusDestroyed())
			{
				if(!this._isChartRejectInit(chart))
				{
					this._initChart(chart);
				}
			}
		}
		
		return charts;
	});
	
	return promise;
};

dashboardProto._isChartRejectInit = function(chart)
{
	if(chart == null)
		return true;
	
	//图表元素不存在，比如在<template></template>里
	if(chart.element() == null)
		return true;
	
	if(chart.manualRender())
		return true;
	
	return false;
};

dashboardProto._initChart = function(chart)
{
	CF.executeSilently(() =>
	{
		chart.init();
	});
};

dashboardProto._inflateChartPlugin = function(charts)
{
	return DF.inflateChartPlugin(charts);
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
 * 获取/设置地图处理器。
 * 
 * @param mapHandler 可选，要设置的地图处理器，格式参考CF.registerMapHandler()说明
 * @returns 要获取的地图处理器
 */
dashboardProto.mapHandler = function(mapHandler)
{
	if(arguments.length == 0)
		return CF.mapHandler;
	else
	{
		CF.registerMapHandler(mapHandler);
	}
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
 * 将单个图表添加至看板。
 * 如果图表已添加至看板，将不会再次添加，直接返回false。
 * 
 * @param chart 图表对象
 * @param syncStatus 可选，是否同步图表状态，默认值为：false
 * @returns true 已添加；false 未添加
 */
dashboardProto.addChart = function(chart, syncStatus)
{
	var idx = this.chartIndex(chart);
	
	if(idx >= 0)
		return false;
	
	var charts = this.charts();
	charts.push(chart);
	
	if(syncStatus === true)
		this._syncAddedChartsStatus(chart);
	
	return true;
};

/**
 * 将多个图表添加至看板。
 * 如果某个图表已添加至看板，将不会再次添加。
 * 
 * @param charts 图表对象、数组
 * @param syncStatus 可选，是否同步图表状态，默认值为：false
 * @returns [ ... ]，是否添加布尔值数组，其中：true 已添加；false 未添加
 */
dashboardProto.addCharts = function(charts, syncStatus)
{
	charts = (CF.isArray(charts) ? charts : [ charts ]);
	
	var re = [];
	var addedCharts = [];
	
	for(let i=0; i<charts.length; i++)
	{
		let chart = charts[i];
		
		if(this.addChart(chart, false))
		{
			re.push(true);
			addedCharts.push(chart);
		}
		else
		{
			re.push(false);
		}
	}
	
	//应先全部加入看板后再进行渲染，确保依赖库加载逻辑有全量的参考依赖库
	if(syncStatus === true)
		this._syncAddedChartsStatus(addedCharts);
	
	return re;
};

//将添加的图表状态与看板状态同步
dashboardProto._syncAddedChartsStatus = function(charts)
{
	if(charts == null)
		return;
	
	charts = (CF.isArray(charts) ? charts : [ charts ]);
	
	for(let i=0; i<charts.length; i++)
	{
		let chart = charts[i];
		
		if(chart.statusPreInit())
		{
			if(!this._isChartRejectInit(chart))
			{
				CF.executeSilently(() =>
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
				});
			}
		}
	}
};

/**
 * 删除图表。
 * 
 * @param identity 图表标识信息：图表HTML元素ID、图表对象、图表ID、图表索引数值、图表HTML元素
 * @param doDestroy 选填，是否销毁图表，默认为：true
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
 * 获取/设置看板数据获取器。
 * 
 * @param fetcher 可选，要设置的获取器，格式允许：
 * 					//context 更新上下文，格式为：
 * 					//{
 * 					//  dashboard: 看板对象,
 * 					//  group: "...",
 * 					//  chartQueries: [ { chart: 图表对象, query: chartQuery }, ... ]
 * 					//}
 * 					//其中：
 * 					//chartQuery 格式同org.datagear.analysis.ChartQuery
 * 					//此函数应返回DashboardResult对象，或者兑现值为DashboardResult对象的Promise对象
 * 					function(context){ ... }
 */
dashboardProto.fetcher = function(fetcher)
{
	if(arguments.length == 0)
	{
		return this._fetcher;
	}
	else
	{
		this._fetcher = fetcher;
	}
};

/**
 * 渲染看板。
 * 此函数在看板生命周期内仅允许调用一次，在dashboard.destroy()后允许再次调用。
 * 
 * 注意：当处于this._statusPreInit()时，此函数内部会先调用this.init()函数。
 * 
 * @returns Promise 兑现时表示渲染已完成，无兑现值
 */
dashboardProto.render = function()
{
	var validStatus = (this._statusPreInit() || this._statusIniting()
			|| this._statusInited() || this._statusDestroyed());
	
	if(!validStatus)
		throw new Error("dashboard is illegal state for : render()");
	
	var initPromise = null;
	
	if(this._statusPreInit())
		initPromise = this.init();
	else
		initPromise = this._initPromise;
	
	if(initPromise == null || this._renderExecuting)
		throw new Error("dashboard is illegal state for : render()");
	
	this._renderExecuting = true;
	
	var renderPromise = initPromise.then(() =>
	{
		this._statusRendering(true);
		
		//确保在listener.onRender()之前所有必要的图表都已初始化完（比如在dashboard.destroy()后添加的图表），
		//使得在listener.onRender()可以最终修改已初始化的图表信息
		this._checkAndInitChartsBeforeRender();
		
		var doRender = true;
		
		var listener = this.listener();
		if(listener && listener.onRender)
			doRender = listener.onRender(this);
		
		//如果listener.onRender()返回false，表示在其内部已执行了this.doRender()函数，这里不应再执行
		if(doRender !== false)
			this.doRender();
	});
	
	this._renderPromise = renderPromise;
	return renderPromise;
};

dashboardProto._checkAndInitChartsBeforeRender = function()
{
	var charts = this.charts();
	
	for(let i=0; i<charts.length; i++)
	{
		let chart = charts[i];
		
		if(chart.statusPreInit())
		{
			if(!this._isChartRejectInit(chart))
			{
				this._initChart(chart);
			}
		}
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
	
	try
	{
		this._renderForms();
		this._prepareDoRenderCharts();
		this.startHandleCharts();
		
		this._statusRendered(true);
	}
	finally
	{
		this._renderExecuting = false;
	}
};

dashboardProto._prepareDoRenderCharts = function()
{
	var charts = this.charts();
	
	for(let i=0; i<charts.length; i++)
	{
		let chart = charts[i];
		
		if(chart.statusInited() || chart.statusDestroyed())
		{
			if(!this._isChartRejectInit(chart))
			{
				chart.statusPreRender(true);
			}
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
 *   //可选，表单提交操作时执行的联动图表设置，格式同dashboard.dataSetParamValueBatched()函数的batchConfig参数，
 *   //其中：name即是表单输入项名称；value函数的sourceContext参数为：[ 表单数据对象、表单HTML元素 ]
 *   link: 图表联动配置对象,
 *   //可选，表单提交按钮文本
 *   submitText: "...",
 *   //可选，表单渲染完成回调函数
 *   rendered: function(form){ ... },
 *   //可选，表单提交完成回调函数，返回false将阻止图表联动
 *   submit: function(formData){ ... }
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
 *   //可选，输入项的联动设置，【图表数据集参数索引对象】同dashboard.dataSetParamValueBatched()函数中的格式
 *   link: 图表数据集参数索引对象、[ 图表数据集参数索引对象, ... ]
 * }
 * 或者，简写为其name属性值。
 * 
 * @param form 要渲染的<form>表单HTML元素、HTML元素ID，表单结构允许灵活自定义，具体参考chartTool.renderDataSetParamForm
 * @param config 可选，表单配置对象，默认为表单元素的elementAttrConst.DASHBOARD_FORM属性值
 */
dashboardProto.renderForm = function(form, config)
{
	this._assertAlive();
	
	form = this._toElementCareId(form);
	
	CF.eleAddClass(form, "dg-dashboard-form");
	
	if(config == null)
		config = CF.evalSilently(CF.eleAttr(form, elementAttrConst.DASHBOARD_FORM), {});
	
	//必须深度复制，因为元素上设置的可能是变量
	config = CF.extend(true, {}, config);
	
	var dashboard = this;
	var globalTheme = CF.renderContextChartTheme(this.renderContext());
	var batchConfig = (config.link || {});
	
	if(batchConfig.data == null)
		batchConfig.data = [];
	
	config._originalSubmit = config.submit;
	config.submit = function(formData)
	{
		let thisForm = this;
		let doLink = true;
		
		if(config._originalSubmit != null)
			doLink = config._originalSubmit.call(thisForm, formData);
		
		if(doLink !== false)
		{
			let charts = dashboard.dataSetParamValueBatched(formData, batchConfig, [ formData, thisForm ]);
			
			for(let i=0; i<charts.length; i++)
			{
				let chart = charts[i];
				
				CF.executeSilently(function()
				{
					chart.refresh();
				});
			}
		}
	};
	
	var items = (config.items == null ? [] : (CF.isArray(config.items) ? config.items : [ config.items ]));
	var defaultValues = {};
	var batchConfigData = batchConfig.data;
	var batchConfigDataArray = CF.isArray(batchConfigData);
	
	for(let i=0; i<items.length; i++)
	{
		let item = items[i];
		
		if(CF.isString(item))
		{
			item = { name: item };
			items[i] = item;
		}
		
		if(!item.type)
			item.type = CF.DataSetParamType.STRING;
		
		if(item.value != null)
			defaultValues[item.name] = item.value;
		
		if(item.link != null)
		{
			if(batchConfigDataArray)
				batchConfigData.push({ name: item.name, index: item.link });
			else
				batchConfigData[item.name] = item.link;
		}
	}
	
	config.paramValues = defaultValues;
	config.chartTheme = globalTheme;
	
	CF.addThemeRefEntity(globalTheme, DF.THEME_REF_DASHBOARD_FORM_ID);
	CF.chartTool.renderDataSetParamForm(form, items, config);
};

/**
 * 如果图表处于活跃状态，则重新调整其尺寸。
 * 
 * @param identity 图表标识信息：图表HTML元素ID、图表对象、图表ID、图表索引数值、图表HTML元素
 * @returns 图表对象
 */
dashboardProto.resizeChart = function(identity)
{
	var chart = this.chart(identity);
	
	if(chart.isActive())
		chart.resize();
	
	return chart;
};

/**
 * 重新调整给定图表数组中活跃图表的尺寸。
 * 
 * @param charts 可选，要调整的图表数组，如果未设置，则是全部看板图表
 * @return 已调整尺寸的图表数组：[ ... ]
 */
dashboardProto.resizeCharts = function(charts)
{
	var re = [];
	
	if(charts === undefined)
		charts = this.charts();
	
	if(charts != null)
	{
		charts.forEach((chart) =>
		{
			if(chart.isActive())
			{
				chart.resize();
				re.push(chart);
			}
		});
	}
	
	return re;
};

/**
 * 重新调整指定元素内（不包括元素自身）包含的所有已加入看板的活跃图表的尺寸。
 * 
 * @param element HTML元素、HTML元素ID
 * @return 已调整尺寸的图表数组：[ ... ]
 */
dashboardProto.resizeChartsIn = function(element)
{
	var charts = this.chartsIn(element);
	return this.resizeCharts(charts);
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
	var ajaxGroupBundle = { groups: [], groupQueryMap: {} };
	var localGroupBundle = { groups: [], groupQueryMap: {} };
	var globalFetcherGroupBundle = { groups: [], groupQueryMap: {} };
	var funcFetcherGroupBundle = { groups: [], groupQueryMap: {} };
	
	for(let i=0; i<charts.length; i++)
	{
		let chart = charts[i];
		let chartQuery = this._waitForUpdateQuery(chart, time);
		
		if(chartQuery != null)
		{
			//应立即设置为HANDLING_UPDATE状态
			chart.status(chartStatusConst.HANDLING_UPDATE);
			
			let chartFetcher = chart.fetcher();
			chartFetcher = (CF.isEmpty(chartFetcher) ? DF.CHART_FETCHER_DEFAULT : chartFetcher);
			
			if(DF.CHART_FETCHER_GLOBAL === chartFetcher)
			{
				this._groupChartQueries(chart, chartQuery, globalFetcherGroupBundle);
			}
			else if(DF.CHART_FETCHER_EMPTY === chartFetcher)
			{
				this._groupChartQueries(chart, chartQuery, localGroupBundle);
			}
			else if(CF.isFunction(chartFetcher))
			{
				this._groupChartQueries(chart, chartQuery, funcFetcherGroupBundle);
			}
			else
			{
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
	}
	
	if(!CF.isEmpty(ajaxGroupBundle.groups))
	{
		let fetchDataURL = CF.renderContextValNonNull(this.renderContext(), renderContextAttrConst.FETCH_DATA_URL);
		fetchDataURL = this.contextURL(fetchDataURL);
		
		this._handleGroupBundle(ajaxGroupBundle, (groupQuery) =>
		{
			this._doHandleChartsAjax(fetchDataURL, groupQuery);
		});
	}
	
	if(!CF.isEmpty(localGroupBundle.groups))
	{
		this._handleGroupBundle(localGroupBundle, (groupQuery) =>
		{
			this._doHandleChartsLocal(groupQuery);
		});
	}
	
	if(!CF.isEmpty(globalFetcherGroupBundle.groups))
	{
		this._handleGroupBundle(globalFetcherGroupBundle, (groupQuery) =>
		{
			this._doHandleChartsGlobalFetcher(groupQuery);
		});
	}
	
	if(!CF.isEmpty(funcFetcherGroupBundle.groups))
	{
		this._handleGroupBundle(funcFetcherGroupBundle, (groupQuery) =>
		{
			this._doHandleChartsFuncFetcher(groupQuery);
		});
	}
};

dashboardProto._handleGroupBundle = function(groupBundle, eachHandler)
{
	for(let i=0; i<groupBundle.groups.length; i++)
	{
		let group = groupBundle.groups[i];
		let groupQuery = groupBundle.groupQueryMap[group];
		
		CF.executeSilently(() =>
		{
			eachHandler(groupQuery);
		});
	}
};

dashboardProto._groupChartQueries = function(chart, chartQuery, groupBundle)
{
	if(groupBundle.groups == null)
		groupBundle.groups = [];
	
	if(groupBundle.groupQueryMap == null)
		groupBundle.groupQueryMap = {};
	
	let group = chart.updateGroup();
	
	if(CF.indexInArray(groupBundle.groups, group) < 0)
		groupBundle.groups.push(group);
	
	let groupQuery = groupBundle.groupQueryMap[group];
	
	if(groupQuery == null)
	{
		groupQuery = { group: group, chartQueries: [] };
		groupBundle.groupQueryMap[group] = groupQuery;
	}
	
	groupQuery.chartQueries.push({chart: chart, query: chartQuery});
};

dashboardProto._isWaitForRender = function(chart)
{
	return chart.statusPreRender();
};

/**
 * 获取正在等在等待更新查询。
 * 
 * @param chart
 * @param currentTime
 * @returns null表示没有
 */
dashboardProto._waitForUpdateQuery = function(chart, currentTime)
{
	if(!chart.isActive())
		return null;
	
	var chartQuery = null;
	var status = chart.status();
	
	if(status == chartStatusConst.HANDLING_UPDATE)
	{
		chartQuery = null;
	}
	else
	{
		//刷新操作是主动请求的，应优先
		chartQuery = chart._pollRequestRefreshQuery();
		
		if(chartQuery == null)
		{
			let wait = false;
			var updateInterval = chart.updateInterval();
			
			if(chart.statusRendered() || chart.statusPreUpdate())
			{
				wait = true;
			}
			else if(updateInterval > -1 && (chart.statusUpdated() || status == chartStatusConst.UPDATE_ERROR))
			{
				var prevUpdateTime = chart._updateTime();
				if(prevUpdateTime == null || (currentTime - prevUpdateTime) >= updateInterval)
				{
					wait = true;
				}
			}
			
			if(wait)
			{
				if(chart.unreadyDataSetParams(true).length > 0)
				{
					//标记为需要参数输入，避免参数准备好时会立即自动更新，实际应该由API控制是否更新
					chart.status(chartStatusConst.PARAM_VALUE_REQUIRED);
					wait = false;
				}
			}
			
			if(wait)
				chartQuery = this._buildChartQuery(chart);
		}
	}
	
	return chartQuery;
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

dashboardProto._doHandleChartsGlobalFetcher = function(groupQuery)
{
	var chartQueries = groupQuery.chartQueries;
	var fetchContext = this._groupQueryToFetchContext(groupQuery);
	
	if(CF.isEmpty(chartQueries) || fetchContext == null)
		return;
	
	var fetcher = this.fetcher();
	
	if(fetcher == null)
		throw new Error("dashboard fetcher required");
	
	//这里不允许异常中断
	CF.executeSilently(() =>
	{
		this._execListenerOnFetch(fetchContext);
	});
	
	var thisDashboard = this;
	var context =
	{
		dashboard: thisDashboard,
		group: groupQuery.group,
		chartQueries: groupQuery.chartQueries
	};
	
	var fetchPromise = new Promise((resolve) =>
	{
		var fetchResult = fetcher(context);
		
		if(fetchResult == null)
			throw new Error("dashboard fetcher return value required");
		
		//fetchResult可以是DashboardResult对象、或兑现值为DashboardResult的Promise对象
		resolve(fetchResult);
	});
	
	fetchPromise.then((dashboardResult) =>
	{
		//不应抛出异常
		CF.executeSilently(() =>
		{
			try
			{
				thisDashboard._handleChartsFetchSuccess(fetchContext, dashboardResult);
			}
			finally
			{
				thisDashboard._setChartsUpdateTime(fetchContext.charts, CF.currentDateMs());
			}
		});
	})
	.catch((error) =>
	{
		//不应抛出异常
		CF.executeSilently(() =>
		{
			try
			{
				thisDashboard._handleChartsFetchError(fetchContext, error);
			}
			finally
			{
				thisDashboard._setChartsUpdateTime(fetchContext.charts, CF.currentDateMs());
			}
		});
	});
};

dashboardProto._doHandleChartsFuncFetcher = function(groupQuery)
{
	var chartQueries = groupQuery.chartQueries;
	var fetchContext = this._groupQueryToFetchContext(groupQuery);
	
	if(CF.isEmpty(chartQueries) || fetchContext == null)
		return;
	
	//这里不允许异常中断
	CF.executeSilently(() =>
	{
		this._execListenerOnFetch(fetchContext);
	});
	
	var thisDashboard = this;
	
	for(let i=0; i<chartQueries.length; i++)
	{
		let chart = chartQueries[i].chart;
		let query = chartQueries[i].query;
		let fetcher = chart.fetcher();
		let context =
		{
			dashboard: thisDashboard,
			chart: chart,
			query: query
		};
		
		let fetchPromise = new Promise((resolve) =>
		{
			var fetchResult = fetcher(context);
			
			if(fetchResult == null)
				throw new Error(CF.chartLogInfo(chart) + " fetcher return value required");
			
			//fetchResult可以是ChartResult对象、或兑现值为ChartResult的Promise对象
			resolve(fetchResult);
		});
		
		fetchPromise.then((chartResult) =>
		{
			//不应抛出异常
			CF.executeSilently(() =>
			{
				try
				{
					thisDashboard._updateChart(chart, chartResult, query, true);
				}
				finally
				{
					chart._updateTime(CF.currentDateMs());
				}
			});
		})
		.catch((error) =>
		{
			//不应抛出异常
			CF.executeSilently(() =>
			{
				try
				{
					thisDashboard._handleChartFetchError(chart, error, true);
				}
				finally
				{
					chart._updateTime(CF.currentDateMs());
				}
			});
		});
	}
};

dashboardProto._doHandleChartsLocal = function(groupQuery)
{
	var fetchContext = this._groupQueryToFetchContext(groupQuery);
	
	if(fetchContext == null)
		return;
	
	//这里不允许异常中断
	CF.executeSilently(() =>
	{
		this._execListenerOnFetch(fetchContext);
	});
	
	var charts = fetchContext.charts;
	
	try
	{
		//org.datagear.analysis.DashboardResult
		let dashboardResult = { chartResults: {} };
		
		for(let i=0; i<charts.length; i++)
		{
			let chart = charts[i];
			let chartId = chart.id();
			dashboardResult.chartResults[chartId] = this._buildEmptyChartResult(chart);
		}
		
		this._handleChartsFetchSuccess(fetchContext, dashboardResult);
	}
	finally
	{
		this._setChartsUpdateTime(charts, CF.currentDateMs());
	}
};

dashboardProto._buildEmptyChartResult = function(chart)
{
	var  re = {};
	
	//设置空数据集结果数组，避免后续出现空指针异常
	chart.results(re, []);
	
	return re;
};

dashboardProto._doHandleChartsAjax = function(url, groupQuery)
{
	var fetchContext = this._groupQueryToFetchContext(groupQuery);
	
	if(fetchContext == null)
		return;
	
	//这里不允许异常中断
	CF.executeSilently(() =>
	{
		this._execListenerOnFetch(fetchContext);
	});
	
	fetch(url, DF.fetchOptsOfPostJson(fetchContext.queryForm))
	.then((response) =>
	{
		if(!response.ok)
			throw new Error(DF.msgOfResponse(response));
		
		let re = response.json();
		
		re.then((data) =>
		{
			let dashboardResult = (data ? data : {});
			this._handleChartsFetchSuccess(fetchContext, dashboardResult);
		});
		
		return re;
	})
	.catch((error) =>
	{
		this._handleChartsFetchError(fetchContext, error);
	})
	.finally(() =>
	{
		this._setChartsUpdateTime(fetchContext.charts, CF.currentDateMs());
	});
};

dashboardProto._groupQueryToFetchContext = function(groupQuery)
{
	var chartQueryPairs = (groupQuery ? groupQuery.chartQueries : null);
	
	if(CF.isEmpty(chartQueryPairs))
		return null;
	
	var charts = this._chartsOfChartQueryPairs(chartQueryPairs);
	var dashboardQueryForm = this._buildDashboardQueryForm(chartQueryPairs);
	var dashboardQuery = this._dashboardQueryOfForm(dashboardQueryForm);
	
	var fetchContext =
	{
		group: groupQuery.group,
		charts: charts,
		query: dashboardQuery,
		queryForm: dashboardQueryForm
	};
	
	return fetchContext;
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

dashboardProto._handleChartsFetchSuccess = function(fetchContext, dashboardResult)
{
	fetchContext.success = true;
	fetchContext.error = false;
	
	var charts = fetchContext.charts;
	var chartResults = dashboardResult.chartResults;
	var chartErrors = dashboardResult.chartErrors;
	var dashboardQuery = fetchContext.query;
	
	for(let i=0; i<charts.length; i++)
	{
		let chart = charts[i];
		let chartId = chart.id();
		
		CF.executeSilently(() =>
		{
			let error = (chartErrors == null ? null : chartErrors[chartId]);
			let chartResult = (chartResults == null ? null : chartResults[chartId]);
			
			if(error != null)
			{
				this._handleChartFetchError(chart, error, true);
			}
			else
			{
				chartResult = (chartResult == null ? {} : chartResult);
				let chartQuery = this._chartQueryOfDashboardQuery(dashboardQuery, chartId);
				this._updateChart(chart, chartResult, chartQuery, true);
			}
		});
	}
};

dashboardProto._handleChartsFetchError = function(fetchContext, error)
{
	fetchContext.success = false;
	fetchContext.error = true;
	
	var charts = fetchContext.charts;
	var logException = true;
	
	for(let i=0; i<charts.length; i++)
	{
		let chart = charts[i];
		
		CF.executeSilently(() =>
		{
			this._handleChartFetchError(chart, error, false);
		});
	}
	
	if(logException)
	{
		CF.logException(error);
	}
};

dashboardProto._handleChartFetchError = function(chart, error, logIfNone)
{
	this._handleChartResultError(chart, error, true, logIfNone);
};

/**
 * 处理图表结果错误。
 * 
 * @param chart 图表对象
 * @param error 错误信息字符串、Error对象、错误信息对象：{ message: "..." }
 * @param setErrorStatus 是否将图表状态更新为：chartStatusConst.UPDATE_ERROR
 * @param logIfNone 可选，是否在chart.listener()未定义fetchError()函数时打印错误日志，默认为：true
 */
dashboardProto._handleChartResultError = function(chart, error, setErrorStatus, logIfNone)
{
	error = this._toStdError(error);
	logIfNone = (logIfNone === undefined ? true : logIfNone);
	
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
		CF.logException("chart '#"+chart.elementId()+"' error : " + error.message);
	}
};

dashboardProto._toStdError = function(error)
{
	if(error == null)
	{
		error = new Error("error");
	}
	else if(error instanceof Error)
	{
	}
	else if(CF.isString(error))
	{
		error = new Error(error);
	}
	else
	{
		//org.datagear.analysis.support.ChartResultErrorMessage
		let message = (error.message || "error");
		error = new Error(message);
	}
	
	return error;
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
 * dashboard.loadChart(element, add);
 * dashboard.loadChart(element, chartWidgetId, add);
 * 
 * @param element 用于渲染图表的<div>元素、元素ID
 * @param chartWidgetId 选填，要加载的图表部件ID，如果不设置，将从元素的"dg-chart-widget"属性取
 * @param add 可选，是否在加载完成后加入看板，默认值为：true
 * @returns Promise 兑现时表示已加载完成，兑现值为：已加载的图表
 */
dashboardProto.loadChart = function(element, chartWidgetId, add)
{
	//(element, add)
	if(chartWidgetId === true || chartWidgetId === false)
	{
		add = chartWidgetId;
		chartWidgetId = undefined;
	}
	
	element = this._toElementCareId(element);
	
	if(!CF.isChartTagName(element))
		throw new Error("load chart element must be : <"+CF.CHART_TAG_NAME+">");
	
	if(!chartWidgetId)
		chartWidgetId = CF.elementWidgetId(element);
	
	var promise = this._loadCharts([ element ], [ chartWidgetId ], add);
	
	promise = promise.then((charts) =>
	{
		return charts[0];
	});
	
	return promise;
};

/**
 * 异步加载多个图表，并将它们加入此看板。
 * 
 * 支持调用方式：
 * dashboard.loadCharts(elements);
 * dashboard.loadCharts(elements, chartWidgetIds);
 * dashboard.loadCharts(elements, add);
 * dashboard.loadCharts(elements, chartWidgetIds, add);
 * 
 * @param elements 用于渲染图表的<div>元素选择器字符串、<div>元素数组
 * @param chartWidgetIds 可选，要加载的图表部件ID数组，如果不设置，将从元素的"dg-chart-widget"属性取
 * @param add 可选，是否在加载完成后加入看板，默认值为：true
 * @returns Promise 兑现时表示已加载完成，兑现值为：已加载的图表数组
 */
dashboardProto.loadCharts = function(elements, chartWidgetIds, add)
{
	//(elements, add)
	if(chartWidgetIds === true || chartWidgetIds === false)
	{
		add = chartWidgetIds;
		chartWidgetIds = undefined;
	}
	
	elements = this._toElementArray(elements);
	
	var newChartWidgetIds = [];
	
	for(let i=0; i<elements.length; i++)
	{
		if(!CF.isChartTagName(elements[i]))
			throw new Error("load chart "+(i+1)+"-th element must be : <"+CF.CHART_TAG_NAME+">");
		
		newChartWidgetIds[i] = (chartWidgetIds == null ? null : chartWidgetIds[i]);
		
		if(CF.isEmpty(newChartWidgetIds[i]))
			newChartWidgetIds[i] = CF.elementWidgetId(elements[i]);
	}
	
	var promise = this._loadCharts(elements, newChartWidgetIds, add);
	return promise;
};

dashboardProto._toElementCareId = function(element)
{
	if(element == null)
		return null;
	
	//元素ID
	if(CF.isString(element))
		return CF.eleOfId(element);
	
	//元素对象
	return element;
};

dashboardProto._toElementArray = function(elements)
{
	if(elements == null)
		return null;
	
	//元素选择器字符串
	if(CF.isString(elements))
		return CF.elesOfSelector(elements);
	
	//元素数组
	if(CF.isArray(elements))
		return elements;
	
	//元素
	if(CF.isHtmlEle(elements))
		return [ elements ];
	
	//类数组对象，比如：NodeList
	if(elements.length !== undefined)
		return Array.from(elements);
	
	//其他
	var re = [ elements ];
	return re;
};

/**
 * 将元素自身或其包含的元素中所有设置了"dg-chart-widget"属性、且未初始化为图表的<div>元素异步加载为图表。
 * 如果没有需要加载的元素，将不会执行异步请求。
 * 
 * 支持调用方式：
 * dashboard.loadUnsolvedCharts();
 * dashboard.loadUnsolvedCharts(element);
 * dashboard.loadUnsolvedCharts(add);
 * dashboard.loadUnsolvedCharts(element, add);
 * 
 * @param elements 可选，限定查找的根HTML元素选择器字符串、根HTML元素数组、根HTML元素，默认为：<body>元素
 * @param add 可选，是否在加载完成后加入看板，默认值为：true
 * @returns Promise 兑现时表示已加载完成，兑现值为：已加载的图表数组
 */
dashboardProto.loadUnsolvedCharts = function(elements, add)
{
	//(add)
	if(elements === true || elements === false)
	{
		add = elements;
		elements = undefined;
	}
	
	elements = this._toElementArray(elements);
	elements = (elements == null ? [ document.body ] : elements);
	
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
	
	var promise = this._loadCharts(unsolvedEles, unsolvedWidgetIds, add);
	return promise;
};

/**
 * 异步加载图表。
 * 
 * @param elements HTML元素数组
 * @param chartWidgetIds 图表部件ID数组，与上面一一对应
 * @param add 可选，是否在加载完成后加入看板，默认值为：true
 * @returns Promise 兑现时表示已加载完成，兑现值为：已加载的图表数组
 */
dashboardProto._loadCharts = function(elements, chartWidgetIds, add)
{
	if(CF.isEmpty(elements))
	{
		let promise = new Promise((resolve) => { resolve([]); });
		return promise;
	}
	
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
	
	var promise = fetch(loadChartURL, DF.fetchOptsOfPostForm(formData))
	.then((response) =>
	{
		if(!response.ok)
			throw new Error(DF.msgOfResponse(response));
		
		return response.json();
	})
	.then((data) =>
	{
		let chartRoots = (data || []);
		let charts = [];
		
		for(let i=0; i<chartRoots.length; i++)
		{
			charts[i] = this._createLoadedChart(chartRoots[i], elements[i], chartWidgetIds[i]);
		}
		
		this._loadingChartElement(elements, false);
		
		return charts;
	})
	.then((charts) =>
	{
		var icpPromise = this._inflateChartPlugin(charts);
		
		if(add !== false)
		{
			icpPromise = icpPromise.then((charts) =>
			{
				//应同步图表状态
				this.addCharts(charts, true);
				return charts;
			});
		}
		
		return icpPromise;
	})
	.catch((error) =>
	{
		this._loadingChartElement(elements, false);
		return error;
	});
	
	return promise;
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

/**
 * 创建本地图表。
 * 
 * 支持调用方式：
 * dashboard.createChart(element);
 * dashboard.createChart(element, chartRoot);
 * dashboard.createChart(element, add);
 * dashboard.createChart(element, chartRoot, add);
 * 
 * @param element 用于渲染图表的<div>元素、元素ID
 * @param chartRoot 选填，要创建的图表根对象，结构同DF.createLocalChart()函数的同名参数，如果不设置，将从元素的"dg-chart-local"属性取
 * @param add 可选，是否在创建完成后加入看板，默认值为：true
 * @returns Promise，兑现时表示已创建完成、且已设置其插件，兑现值为：创建的图表对象
 */
dashboardProto.createChart = function(element, chartRoot, add)
{
	element = this._toElementCareId(element);
	
	if(!CF.isChartTagName(element))
		throw new Error("chart element must be : <"+CF.CHART_TAG_NAME+">");
	
	if(this.renderedChart(element) != null)
		throw new Error("element has a bounded chart");
	
	//看板中可能存在已初始化但是未渲染的图表，也不应允许异步加载
	if(this.chart(element) != null)
		throw new Error("element has a bounded chart");
	
	//(element, add)
	if(chartRoot === true || chartRoot === false)
	{
		add = chartRoot;
		chartRoot = undefined;
	}
	
	if(CF.isEmpty(chartRoot))
	{
		chartRoot = DF.elementLocalAttr(element);
		
		if(!CF.isEmpty(chartRoot))
		{
			chartRoot = DF.evalChartLocalValue(chartRoot);
		}
	}
	
	if(CF.isEmpty(chartRoot))
		throw new Error("[chartRoot] required");
	
	var chart = DF.createLocalChart(element, chartRoot, this.renderContext(), this);
	var promise = this._inflateChartPlugin(chart);
	
	if(add !== false)
	{
		promise = promise.then((chart) =>
		{
			//应同步图表状态
			this.addChart(chart, true);
			return chart;
		});
	}
	
	return promise;
};

/**
 * 创建本地图表。
 * 
 * 支持调用方式：
 * dashboard.createCharts(element);
 * dashboard.createCharts(element, chartRoots);
 * dashboard.createCharts(element, add);
 * dashboard.createCharts(element, chartRoots, add);
 * 
 * @param elements 用于渲染图表的<div>元素选择器字符串、<div>元素数组
 * @param chartRoots 选填，要创建的图表根对象数组，结构同DF.createLocalChart()函数的同名参数，如果不设置，将从元素的"dg-chart-local"属性取
 * @param add 可选，是否在创建完成后加入看板，默认值为：true
 * @returns Promise，兑现时表示已创建完成、且已设置其插件，兑现值为：创建的图表对象数组
 */
dashboardProto.createCharts = function(elements, chartRoots, add)
{
	elements = this._toElementArray(elements);
	
	//(elements, add)
	if(chartRoots === true || chartRoots === false)
	{
		add = chartRoots;
		chartRoots = undefined;
	}
	
	var newChartRoots = [];
	
	for(let i=0; i<elements.length; i++)
	{
		let element = elements[i];
		let chartRoot = (chartRoots == null ? null : chartRoots[i]);
		
		if(!CF.isChartTagName(element))
			throw new Error("chart elements["+i+"] must be : <"+CF.CHART_TAG_NAME+">");
		
		if(this.renderedChart(element) != null)
			throw new Error("elements["+i+"] has a rendered chart");
		
		//看板中可能存在已初始化但是未渲染的图表，也不应允许异步加载
		if(this.chart(element) != null)
			throw new Error("elements["+i+"] has a bounded chart");
		
		if(CF.isEmpty(chartRoot))
		{
			chartRoot = DF.elementLocalAttr(element);
			if(!CF.isEmpty(chartRoot))
			{
				chartRoot = DF.evalChartLocalValue(chartRoot);
			}
		}
		
		if(CF.isEmpty(chartRoot))
			throw new Error("chartRoots["+i+"] required");
		
		newChartRoots.push(chartRoot);
	}
	
	var charts = [];
	
	for(let i=0; i<elements.length; i++)
	{
		let element = elements[i];
		let chartRoot = newChartRoots[i];
		let chart = DF.createLocalChart(element, chartRoot, this.renderContext(), this);
		charts.push(chart);
	}
	
	var promise = this._inflateChartPlugin(charts);
	
	if(add !== false)
	{
		promise = promise.then((charts) =>
		{
			//应同步图表状态
			this.addCharts(charts, true);
			return charts;
		});
	}
	
	return promise;
};

/**
 * 将元素自身或其包含的元素中所有设置了"dg-chart-local"属性、且未初始化为图表的<div>元素创建为本地图表。
 * 
 * 支持调用方式：
 * dashboard.createUnsolvedCharts();
 * dashboard.createUnsolvedCharts(elements);
 * dashboard.createUnsolvedCharts(add);
 * dashboard.createUnsolvedCharts(elements, add);
 * 
 * @param elements 可选，限定查找的根HTML元素选择器字符串、根HTML元素数组、根HTML元素，默认为：<body>元素
 * @param add 可选，是否在创建完成后加入看板，默认值为：true
 * @returns Promise，兑现时表示已创建完成、且已设置其插件，兑现值为：创建的图表对象数组
 */
dashboardProto.createUnsolvedCharts = function(elements, add)
{
	//(add)
	if(elements === true || elements === false)
	{
		add = elements;
		elements = undefined;
	}
	
	elements = this._toElementArray(elements);
	elements = (elements == null ? [ document.body ] : elements);
	
	var charts = [];
	
	for(let i=0; i<elements.length; i++)
	{
		let elesWithLocal = DF.elesWithLocal(elements[i]);
		let eles = elesWithLocal.elements;
		let locals = elesWithLocal.locals;
		
		for(let j=0; j<eles.length; j++)
		{
			let ele = eles[j];
			
			if(this.renderedChart(ele) != null)
				continue;
			
			//看板中可能存在对应此元素的已初始化但是未渲染的图表，这里也要排除
			if(this.chart(ele) != null)
				continue;
			
			let chartRoot = locals[j];
			
			if(!CF.isEmpty(chartRoot))
				chartRoot = DF.evalChartLocalValue(chartRoot);
			
			if(CF.isEmpty(chartRoot))
				continue;
			
			let localChart = DF.createLocalChart(ele, chartRoot, this.renderContext(), this);
			charts.push(localChart);
		}
	}
	
	var promise = this._inflateChartPlugin(charts);
	
	if(add !== false)
	{
		promise = promise.then((charts) =>
		{
			//应同步图表状态
			this.addCharts(charts, true);
			return charts;
		});
	}
	
	return promise;
};

/**
 * 批量设置图表数据集参数值。
 * 
 * @param sourceData 源数据，格式支持：
 * 					{ ... }、
 * 					[ ... ]、
 * 					//获取指定属性路径值的函数，当propPath为null或""时，应返回函数底层的源数据，
 * 					//对于不存在的属性路径，应返回null
 * 					function(propPath){ return ...; }
 * @param batchConfig 批量配置，格式为：
 * 					{
 * 					  //可选，要设置的目标图表元素ID、图表ID、看板图表数组索引、图表对象，或者它们的数组
 * 					  target: ...,
 * 					  
 * 					  //可选，下述【源数据属性名】的统一根前缀，末尾不应带'.'字符，
 * 					  //当是数组时，表示取第一个不为null的值
 * 					  root: "..."、[ "...", ... ],
 * 					  
 * 					  //可选，要设置的参数值映射表，没有则不设置任何参数值
 * 					  data:
 * 					  {
 * 					    源数据属性名: 图表数据集参数索引对象、[ 图表数据集参数索引对象, ... ],
 * 					    ...
 * 					  }、
 * 					  [
 * 					    {
 * 					      //源数据属性名
 * 					      name: "...",
 * 					      
 * 					      index: 图表数据集参数索引对象、[ 图表数据集参数索引对象, ... ],
 * 					      
 * 					      //可选，自定义源参数值处理函数，返回要设置的目标参数值
 * 					      //sourceValue 源参数值
 * 					      //sourceContext 源参数值上下文对象
 * 					      value: function(sourceValue, sourceContext){ return ...; }
 * 					    },
 * 					    ...
 * 					  ]
 * 					}
 * 					上述【源数据属性名】可以是源数据中的简单属性名（比如："name"、"value"），
 * 					也可以是属性路径（比如："order.name"、"[0].name"、"['order'].product.name"）。
 * 					【图表数据集参数索引对象】用于确定【源数据属性名】对应值要设置到的目标图表数据集参数，格式为：
 * 					{
 *                    //可选，可以是上述批量配置对象的target数组中的索引，也可以是图表元素ID、图表ID、看板图表数组索引、图表对象，默认值为：0
 * 					  chart: 数值、...,
 * 					  
 * 					  //可选，目标图表数据集数组的索引数值，默认为：0
 * 					  dataSet: ...,
 * 					  
 * 					  //可选，目标图表数据集的参数数组索引/参数名，默认为：0
 * 					  param: ...
 * 					}
 * 					或者，可简写为上述图表数据集参数索引对象的"param"属性值
 * @param sourceContext 可选，传递给图表数据集参数索引对象的value函数sourceContext参数的对象，如果为数组，则传递多个参数，默认为sourceData
 * @return 批量设置的图表对象数组
 */
dashboardProto.dataSetParamValueBatched = function(sourceData, batchConfig, sourceContext)
{
	sourceContext = (sourceContext === undefined ? sourceData : sourceContext);
	
	var targets = (batchConfig.target == null ? [] : (CF.isArray(batchConfig.target) ? batchConfig.target : [ batchConfig.target ]));
	var targetCharts = [];
	
	for(let i=0; i<targets.length; i++)
	{
		targetCharts[i] = (targets[i] instanceof CF.Chart ? targets[i] : this.chart(targets[i]));
		
		if(targetCharts[i] == null)
			throw new Error("no chart found for : " + targets[i]);
	}
	
	var re = Array.from(targetCharts);
	
	var dataMaps = (batchConfig.data || []);
	//{ "...": ..., ... } 转换为 [ {}, ... ]
	if(!CF.isArray(dataMaps))
	{
		let dataMapArray = [];
		
		for(let name in dataMaps)
			dataMapArray.push({ name: name, index: dataMaps[name] });
		
		dataMaps = dataMapArray;
	}
	
	var isTargetsEmpty = CF.isEmpty(targets);
	var isSourceDataFunc = CF.isFunction(sourceData);
	var propPathRoots = (CF.isEmpty(batchConfig.root) ? [""] : (CF.isArray(batchConfig.root) ? batchConfig.root : [ batchConfig.root ]));
	
	var sourceContextArgs = [ "place-holder-for-source-value" ];
	sourceContextArgs = sourceContextArgs.concat(CF.isArray(sourceContext) ? sourceContext : [ sourceContext ]);
	
	for(let i=0; i<dataMaps.length; i++)
	{
		let dataMap = dataMaps[i];
		let name = dataMap.name;
		let index = (dataMap.index == null ? null : (CF.isArray(dataMap.index) ? dataMap.index : [ dataMap.index ]));
		let dataValueHandler = dataMap.value;
		let dataValue = null;
		
		if(CF.isEmpty(index))
			continue;
		
		for(let j=0; j<propPathRoots.length; j++)
		{
			let propPath = CF.concatPropertyPath(propPathRoots[j], name);
			
			if(isSourceDataFunc)
				dataValue = sourceData(propPath);
			//当propPath为空时，应直接使用sourceData
			else if(CF.isEmpty(propPath))
				dataValue = sourceData;
			else
				dataValue = CF.propertyPathValue(sourceData, propPath);
			
			if(dataValue != null)
				break;
		}
		
		if(dataValueHandler != null)
		{
			sourceContextArgs[0] = dataValue;
			dataValue = dataValueHandler.apply(dataMap, sourceContextArgs);
		}
		
		for(let j=0; j<index.length; j++)
		{
			let indexEle = index[j];
			let chartIdx = 0;
			let dataSetIdx = 0;
			let param = 0;
			
			//参数名/索引号
			if(CF.isStringOrNumber(indexEle))
			{
				param = indexEle;
			}
			else
			{
				chartIdx = (indexEle.chart != null ? indexEle.chart : chartIdx);
				dataSetIdx = (indexEle.dataSet != null ? indexEle.dataSet : dataSetIdx);
				param = (indexEle.param != null ? indexEle.param : param);
			}
			
			let targetChart = null;
			
			//优先使用batchConfig.target中的索引号
			if(CF.isNumber(chartIdx) && !isTargetsEmpty)
			{
				targetChart = targetCharts[chartIdx];
				
				if(targetChart == null)
					throw new Error("no chart found in [batchConfig.targets] for : " + chartIdx);
			}
			else
			{
				targetChart = (chartIdx instanceof CF.Chart ? chartIdx : this.chart(chartIdx));
				
				if(targetChart == null)
					throw new Error("no chart found for : " + chartIdx);
				
				if(CF.indexInArray(re, targetChart) < 0)
					re.push(targetChart);
			}
			
			targetChart.dataSetParamValue(dataSetIdx, param, dataValue);
		}
	}
	
	return re;
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
 * @returns true 已销毁；false 未执行，因为看板处于销毁非法状态
 */
dashboardProto.destroy = function()
{
	if(this._statusDestroyed())
		return true;
	
	if(!this.isActive() || this._statusDestroying())
		return false;
	
	this._statusDestroying(true);
	
	var doDestroy = true;
	
	var listener = this.listener();
	if(listener && listener.onDestroy)
		doDestroy = listener.onDestroy(this);
	
	//为false表示listener.onDestroy()内部已执行了this.doDestroy()函数
	if(doDestroy !== false)
		this.doDestroy();
	
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
	this._destroyAllForms();
	
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

dashboardProto._destroyAllForms = function()
{
	var forms = CF.elesOfSelector("form.dg-dashboard-form");
	
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
		CF.eleRemoveClass(form, "dg-dashboard-form");
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
	return CF.toRenderContextPathURL(renderContext, url);
};

/**
 * 销毁看板表单。
 * 
 * @param form 表单HTML元素、HTML元素ID
 */
dashboardProto.destroyForm = function(form)
{
	form = this._toElementCareId(form);
	
	if(CF.isEleMatches(form, "form.dg-dashboard-form"))
	{
		this._destroyForm(form);
	}
};

/**
 * 获取API版本。
 * 返回值规则同：org.datagear.analysis.support.html.DashboardApiVersion.trimVersion(String)
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
 * 获取指定元素内（不包括元素自身）包含的所有已加入看板的图表。
 *
 * @param element HTML元素、HTML元素ID
 * @return [ ... ]
 */
dashboardProto.chartsIn = function(element)
{
	element = this._toElementCareId(element);
	
	var re = [];
	
	var eles = CF.elesOfSelector("[id]", element);
	
	eles.forEach((ele) =>
	{
		let id = CF.eleAttr(ele, "id");
		let chart = (CF.isEmpty(id) ? null : this.chart(id));
		
		if(chart != null)
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
 * @param eval 可选，是否转换为对象，默认值为：false
 * @returns { elements: [ HTML元素, ... ], locals: [ ..., ... ] }
 */
DF.elesWithLocal = function(ele, eval)
{
	eval = (eval === undefined ? false : eval);
	
	var re = { elements: [], locals: [] };
	
	if(ele == null)
		return re;
	
	var local = DF.elementLocalAttr(ele);
	
	if(!CF.isEmpty(local) && CF.isChartTagName(ele))
	{
		if(eval)
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
			if(eval)
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

DF.chartWidgetToEleLocalAttrVal = function(chartWidget)
{
	var pluginId = null;
	
	//org.datagear.management.domain.HtmlChartWidgetEntity
	if(chartWidget.pluginVo)
	{
		pluginId = chartWidget.pluginVo.id;
	}
	//org.datagear.analysis.support.ChartWidget
	else if(chartWidget.plugin)
	{
		pluginId = chartWidget.plugin.id;
	}
	
	var obj = { plugin: pluginId };
	
	return CF.serializeBySingleQuote(obj);
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

})(this, window);
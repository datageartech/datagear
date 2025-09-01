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
 * 图表工厂，用于初始化图表对象，为图表对象添加功能函数。
 * 全局变量名：window.chartFactory
 * 
 * 加载时依赖：
 *   无
 * 
 * 运行时依赖:
 *   jquery.js
 *   echarts.js
 *   chartSetting.js
 * 
 * 
 * 此图表工厂支持为<body>元素、图表元素添加elementAttrConst.OPTIONS属性来设置图表选项，格式为：
 * { title: { show: false },... }
 * 
 * 此图表工厂支持为<body>元素、图表元素添加elementAttrConst.THEME属性来设置图表主题，格式为：
 * { color:'...', backgroundColor:'...', ... }
 * 
 * 此图表工厂支持为<body>元素、图表元素添加elementAttrConst.LISTENER属性来设置图表监听器，格式参考chart.listener函数参数说明。
 * 
 * 此图表工厂支持为<body>元素、图表元素添加elementAttrConst.ECHARTS_THEME属性来设置图表ECharts主题名。
 * 
 * 此图表工厂支持为<body>元素、图表元素添加elementAttrConst.DISABLE_SETTING属性，用于禁用图表交互设置功能，
 * 值为"true"表示禁用，其他表示启用。
 * 
 * 此图表工厂支持为图表元素添加"dg-chart-on-*"属性来设置图表事件处理函数。
 * 
 * 此图表工厂支持为图表元素添加elementAttrConst.RENDERER属性来自定义、扩展图表渲染器，具体参考chart._initRenderer函数说明。
 * 
 * 此图表工厂要求图表插件的图表渲染器（chart.plugin().renderer）格式为：
 * {
 *   //可选，渲染器依赖库，具体结构参考chartFactory.loadLib()函数说明
 *   //注意库源URL规范不同，具体参考chartFactory.trimPluginRendererLibSourceUrl()函数说明
 *   depend: { ... }、[ {...}, ... ]、function(){ return { ... }、[ {...}, ... ]; }
 *   //可选，渲染图表函数是否是异步函数，默认为false
 *   asyncRender: true、false、function(chart){ ...; return true 或者 false; }
 *   //必选，渲染图表函数
 *   render: function(chart){ ... },
 *   //可选，更新图表数据函数是否是异步函数，默认为false
 *   asyncUpdate: true、false、function(chart, chartResult){ ...; return true 或者 false; }
 *   //必选，更新图表数据函数
 *   //chartResult 要更新的图表结果
 *   update: function(chart, chartResult){ ... },
 *   //可选，调整图表尺寸函数
 *   resize: function(chart){ ... },
 *   //可选，绑定图表事件处理函数
 *   //eventType 事件类型，比如："click"、"mouseover"等
 *   //handler 图表事件处理函数，格式为：function(chartEvent){ ... }
 *   on: function(chart, eventType, handler){ ... },
 *   //可选，解绑图表事件处理函数
 *   //eventType 事件类型
 *   //handler 图表事件处理函数引用
 *   off: function(chart, eventType, handler){ ... },
 *   //可选，销毁图表函数
 *   destroy: function(chart){ ... },
 *   //可选，渲染器附加数据
 *   additions: { 名: 值, ... }、function(chart){ return { 名: 值, ... }; };
 * }
 * 
 * 此图表工厂和dashboardFactory.js一起可以支持异步图表插件，示例如下：
 * {
 *   asyncRender: true,
 *   
 *   render: function(chart)
 *   {
 *     jQuery.get("...", function()
 *     {
 *       ...;
 *       
 *       //将图表状态设置为已完成render
 *       chart.statusRendered(true);
 *     });
 *   },
 *   
 *   asyncUpdate: true,
 *   
 *   update: function(chart, chartResult)
 *   {
 *     jQuery.get("...", function()
 *     {
 *       ...;
 *       
 *       //将图表状态设置为已完成update
 *       chart.statusUpdated(true);
 *     });
 *   }
 * }
 */
(function(global)
{

/**图表工厂*/
var CF = (global.chartFactory || (global.chartFactory = {}));

/**图表状态常量*/
var chartStatusConst = (CF.chartStatusConst || (CF.chartStatusConst = {}));

/**HTML元素属性常量*/
var elementAttrConst = (CF.elementAttrConst || (CF.elementAttrConst = {}));

/**
 * 图表地图映射表。
 * 地图类图表的地图名称与其地图数据地址映射表，用于为chart.mapURL()函数提供支持。
 * 此映射表默认为空，用户可以填充它以扩展地图名映射。
 * 映射表格式示例：
 * {
 *   //绝对路径映射
 *   china: "/map/china.json",
 *   //相对路径映射
 *   beijing: "map/beijing.json",
 *   //相对路径映射
 *   shanghai: "../map/shanghai.json",
 *   //自定义映射逻辑函数，用于处理未设置对应关系的映射
 *   mapURL: function(name)
 *   {
 *     return "...";
 *   }
 * }
 */
var chartMapURLs = (CF.chartMapURLs || (CF.chartMapURLs = {}));

/** 渲染上下文属性名常量 */
var renderContextAttrConst = (CF.renderContextAttrConst || (CF.renderContextAttrConst = {}));

/**内置图表选项名定义，所有内置图表选项名都应定义于此，便于因名字冲突需要重新定义*/
var builtinOptionNames = (CF.builtinOptionNames || (CF.builtinOptionNames = {}));

//----------------------------------------
// chartStatusConst开始
//----------------------------------------

/**图表状态：准备init*/
chartStatusConst.PRE_INIT = "PRE_INIT";

/**图表状态：正在init*/
chartStatusConst.INITING = "INITING";

/**图表状态：完成init*/
chartStatusConst.INITED = "INITED";

/**图表状态：准备render*/
chartStatusConst.PRE_RENDER = "PRE_RENDER";

/**图表状态：正在render*/
chartStatusConst.RENDERING = "RENDERING";

/**图表状态：完成render*/
chartStatusConst.RENDERED = "RENDERED";

/**图表状态：准备update*/
chartStatusConst.PRE_UPDATE = "PRE_UPDATE";

/**图表状态：正在update*/
chartStatusConst.UPDATING = "UPDATING";

/**图表状态：完成update*/
chartStatusConst.UPDATED = "UPDATED";

/**图表状态：正在销毁*/
chartStatusConst.DESTROYING = "DESTROYING";

/**图表状态：已销毁*/
chartStatusConst.DESTROYED = "DESTROYED";

//----------------------------------------
// chartStatusConst结束
//----------------------------------------

//----------------------------------------
// elementAttrConst开始
//----------------------------------------

/**图表部件*/
elementAttrConst.WIDGET = "dg-chart-widget";

/**图表选项*/
elementAttrConst.OPTIONS = "dg-chart-options";

/**图表主题*/
elementAttrConst.THEME = "dg-chart-theme";

/**图表监听器*/
elementAttrConst.LISTENER = "dg-chart-listener";

/**图表ECharts主题*/
elementAttrConst.ECHARTS_THEME = "dg-echarts-theme";

/**图表禁用设置*/
elementAttrConst.DISABLE_SETTING = "dg-chart-disable-setting";

/**图表事件处理（前缀）*/
elementAttrConst.ON = "dg-chart-on-";

/**图表渲染器*/
elementAttrConst.RENDERER = "dg-chart-renderer";

/**图表属性值*/
elementAttrConst.ATTR_VALUES = "dg-chart-attr-values";

//----------------------------------------
// elementAttrConst结束
//----------------------------------------

//----------------------------------------
// renderContextAttrConst开始
//----------------------------------------

//渲染上下文属性名：Web上下文，同：
//AbstractDataAnalysisController.DASHBOARD_BUILTIN_RENDER_CONTEXT_ATTR_WEB_CONTEXT
renderContextAttrConst.webContext = "DG_WEB_CONTEXT";

//渲染上下文属性名：已填充图表主题
renderContextAttrConst.inflatedChartTheme = "DG_INFLATED_CHART_THEME";

//----------------------------------------
// renderContextAttrConst结束
//----------------------------------------

/** 内置图表选项名：自定义选项名 */
builtinOptionNames.customOptionNames = "customOptionNames";
/** 内置图表选项名：是否美化滚动条 */
builtinOptionNames.beautifyScrollbar = "beautifyScrollbar";
/** 内置图表选项名：处理图表渲染选项 */
builtinOptionNames.processRenderOptions = "processRenderOptions";
/** 内置图表选项名：处理图表更新选项 */
builtinOptionNames.processUpdateOptions = "processUpdateOptions";
/** 内置图表选项名：更新追加模式 */
builtinOptionNames.updateAppendMode = "updateAppendMode";
/** 内置图表选项名：是否禁用内置设置（参数/数据透视表） */
builtinOptionNames.disableSetting = "disableSetting";
/** 内置图表选项名：内置设置（参数/数据透视表） */
builtinOptionNames.builtinSetting = "builtinSetting";

/** 图表标识样式名，所有已绘制的图表元素都会添加此样式名 */
CF.CHART_STYLE_NAME_FOR_INDICATION = "dg-chart-for-indication";

/** 关键字：可作为定位父元素的样式类名 */
CF.CHART_STYLE_NAME_FOR_RELATIVE = "dg-position-relative";

/** 看板引入库标识属性，同：org.datagear.analysis.support.html.HtmlTplDashboardWidgetRenderer.DASHBOARD_LIB_NAME_ATTR */
CF.LIB_ATTR_NAME = "dg-lib-name";

/**
 * 图表属性值集中图表选项名，同：org.datagear.management.domain.HtmlChartWidgetEntity.ATTR_CHART_OPTIONS
 */
CF.CHART_ATTR_NAME_OPTIONS = "DG_CHART_OPTIONS";

/**
 * 图表属性值集中图表部件名，同：org.datagear.analysis.support.ChartWidget.ATTR_CHART_WIDGET
 */
CF.CHART_ATTR_NAME_WIDGET = "DG_CHART_WIDGET";

/**
 * 数据标记全名分隔符
 */
CF.DATA_SIGN_FULLNAME_SEPARATOR = ".";

/**图表渲染器附加属性：是否支持忽略获取结果，默认值为：false */
CF.RENDERER_ADDITION_SUPPORT_IGNORE_FETCH = "supportIgnoreFetch";

/** HTML元素上已渲染的图表对象KEY */
CF.ELE_RENDERED_CHART_NAME = "RENDERED_CHART";

/**内置名字标识片段*/
CF.BUILTIN_NAME_PART = "datagear";

/**内置对象属性名前缀*/
CF.BUILTIN_PROP_PREFIX = "_" + CF.BUILTIN_NAME_PART;

//org.datagear.analysis.DataSetParam.DataType
CF.DataSetParamType =
{
	STRING: "STRING",
	BOOLEAN: "BOOLEAN",
	NUMBER: "NUMBER"
};

/**
 * 初始化渲染上下文。
 * 将webContext直接存入渲染上下文，复制chartTheme后使用<body>上的dg-chart-theme填充相关属性后存入渲染上下文，
 * 之后可以通过:
 * CF.renderContextAttrWebContext(renderContext)
 * CF.renderContextAttrChartTheme(renderContext)
 * 获取它们。
 * 
 * 注意：此函数应在初始化图表前（chart.init()函数调用前）且<body>后调用。
 * 
 * @param renderContext
 * @param webContext Web上下文
 * @param chartTheme 图表主题
 */
CF.initRenderContext = function(renderContext, webContext, chartTheme)
{
	if(!webContext)
		throw new Error("[webContext] required");
	if(!chartTheme)
		throw new Error("[chartTheme] required");
	
	if(CF._themeInflated(chartTheme))
		throw new Error("[chartTheme] must not inflated");
	
	chartTheme = CF.extend(true, {}, chartTheme);
	
	CF._inflateGlobalChartTheme(chartTheme);
	
	CF.renderContextAttrWebContext(renderContext, webContext);
	CF.renderContextAttrChartTheme(renderContext, chartTheme);
};

/**
 * 判断CF.initRenderContext()函数是否已执行。
 */
CF.isRenderContextInited = function(renderContext)
{
	if(!renderContext)
		return false;
	
	var webContext = CF.renderContextAttrWebContext(renderContext);
	var chartTheme = CF.renderContextAttrChartTheme(renderContext);
	var inflated = CF._themeInflated(chartTheme);
	
	return (webContext && chartTheme && inflated);
};

/**
 * 创建图表实例，为其添加图表API，并设置chart.statusPreInit(true)状态，但不调用chart.init()函数。
 * 
 * @param chart 图表JSON对象，格式应为：
 *				{
 *				  //唯一ID
 *				  id: "...",
 *				  //HTML元素ID
 *				  elementId: "...",
 *				  //渲染上下文
 *				  renderContext: {...},
 *				  //图表插件
 *				  plugin: {...},
 *				  //可选，名称
 *				  name: "...",
 *				  //可选，数据集绑定数组
 *				  dataSetBinds: [...],
 *				  //可选，更新间隔
 *				  updateInterval: 数值,
 *				  //可选，图表结果数据格式
 *				  resultDataFormat: {...},
 *				  //图表属性
 *				  attrValues: {...}
 *				}
 *				
 *				另参考：org.datagear.analysis.support.html.HtmlChart
 * @returns 图表实例
 */
CF.init = function(chart)
{
	var instance = new CF.Chart(chart);
	instance.statusPreInit(true);
	return instance;
};

/**
 * Chart类
 */
CF.Chart = function(chart)
{
	CF.initChartProps(chart);
	this._root = chart;
	this._plugin = CF.findPluginById(chart.plugin ? chart.plugin.id : null);
};

/**
 * Chart类原型
 */
var chartProto = CF.Chart.prototype;

/**
 * 初始化图表对象基础属性。
 */
CF.initChartProps = function(chart)
{
	chart.name = (chart.name || "");
	chart.updateInterval = (chart.updateInterval == null ? -1 : chart.updateInterval);
	chart.dataSetBinds = (chart.dataSetBinds || []);
	for(var i=0; i<chart.dataSetBinds.length; i++)
	{
		var dsb = chart.dataSetBinds[i];
		dsb.dataSetSigns = (dsb.dataSetSigns || []);
		dsb.fieldSigns = (dsb.fieldSigns || {});
		dsb.alias = (dsb.alias == null ?  "" : dsb.alias);
		dsb.attachment = (dsb.attachment === true ? true : false);
		dsb.query = (dsb.query || {});
		dsb.query.paramValues = (dsb.query.paramValues || {});
		//为dataSetBinds元素添加index属性，便于后续根据其索引获取结果集等信息
		dsb.index = i;
	}
	
	chart.attrValues = (chart.attrValues || {});
	chart.options = (chart.options || {});
	
	//将内置属性值提取出来，避免被chart.attrValues()设置操作清除
	chart.widget = chart.attrValues[CF.CHART_ATTR_NAME_WIDGET];
	chart.optionsOrigin = chart.attrValues[CF.CHART_ATTR_NAME_OPTIONS];
	delete chart.attrValues[CF.CHART_ATTR_NAME_WIDGET];
	delete chart.attrValues[CF.CHART_ATTR_NAME_OPTIONS];
	
	//保留原始属性值集，看板可视编辑需要使用
	//注意，初始化attrValuesOrigin的逻辑不能在chart.render()中执行，
	//因为chart.render()可以被多次调用，chart.attrValues可能已被修改
	chart.attrValuesOrigin = CF.extend(true, {}, chart.attrValues);
};

CF.findPluginById = function(pluginId)
{
	if(pluginId == null)
		return null;
	
	var plugin = null;
	
	if(CF.chartPluginManager && CF.chartPluginManager.get)
		plugin = CF.chartPluginManager.get(pluginId);
	
	return plugin;
};

//----------------------------------------
// Chart prototype start
//----------------------------------------

/**
 * 初始化图表，使用图表元素上的dg-*属性值初始化图表。
 * 图表初始化后处于this.statusInited()状态。
 * 此函数在图表生命周期内仅允许调用一次，在this.destroy()后允许再次调用。 
 * 
 * 注意：只有this.statusPreInit()或者this.statusDestroyed()为true，此函数才允许执行。
 * 注意：初始化图表前应确保已调用CF.initRenderContext(this.renderContext)。
 * 注意：此函数内不应执行渲染相关逻辑，而应仅执行初始化图表属性的相关逻辑，因为chart.destroy()后可再次调用chart.init()。
 * 
 * 图表生命周期：
 * chart.init() -->-- chart.render() -->-- chart.update() -->-- chart.destroy() -->--|
 *       |                  |                    |           |                       |
 *       |                  |                    |-----<-----|                       |
 *       |                  |--------------------------<-----------------------------| 
 *       |-----------------------------------<---------------------------------------| 
 */
chartProto.init = function()
{
	if(!this.id())
		throw new Error("chart id required");
	
	if(!this.elementId())
		throw new Error("chart elementId required");
	
	if(!this.renderContext())
		throw new Error("chart renderContext required");
	
	if(this.element() == null)
		throw new Error("chart element '#"+this.elementId()+"' required");
	
	if(!this.statusPreInit() && !this.statusDestroyed())
		throw new Error("chart is illegal state for : init()");
	
	if(!this._isRenderContextInited())
		throw new Error("chart is illegal state for : init()");
	
	this.statusIniting(true);
	
	this._initForPre();
	
	this._initOptions();
	this._initTheme();
	this._initListener();
	this._initDisableSetting();
	this._initRenderer();
	this._initAttrValues();
	this._initUpdateAppendMode();
	
	this._initForPost();
	
	this.statusInited(true);
};

chartProto._isRenderContextInited = function()
{
	return CF.isRenderContextInited(this.renderContext());
};

chartProto._renderContextAttrChartTheme = function()
{
	return CF.renderContextAttrChartTheme(this.renderContext());
};

chartProto._renderContextAttrWebContext = function()
{
	return CF.renderContextAttrWebContext(this.renderContext());
};

/**
 * 初始化图表选项。
 * 此函数依次从<body>元素、图表元素的elementAttrConst.OPTIONS属性读取、合并图表选项。
 */
chartProto._initOptions = function()
{
	var options = {};
	
	var optionsOrigin = this.optionsOrigin(true);
	if(optionsOrigin)
		options = CF.extend(true, options, optionsOrigin);
	
	var eleOptions = CF.eleAttr(this.element(), elementAttrConst.OPTIONS);
	
	var bodyOptions = this._bodyOptions();
	if(bodyOptions)
		options = CF.extend(true, options, bodyOptions);
	
	if(eleOptions)
		options = CF.extend(true, options, CF.evalSilently(eleOptions, {}));
	
	this.options(options);
};

chartProto._bodyOptions = function()
{
	var optionsStr = CF.eleAttr(document.body, elementAttrConst.OPTIONS);
	
	if(optionsStr !== CF._PREV_BODY_OPTIONS_STR)
	{
		CF._PREV_BODY_OPTIONS_STR = optionsStr;
		CF._PREV_BODY_OPTIONS = CF.evalSilently(optionsStr);
	}
	
	return CF._PREV_BODY_OPTIONS;
};

/**
 * 初始化图表主题。
 * 此函数依次从this.renderContext()中的renderContextAttrConst.inflatedChartTheme属性值、
 * <body>元素、图表元素的elementAttrConst.THEME属性读取、合并图表主题。
 */
chartProto._initTheme = function()
{
	var eleTheme = CF.eleAttr(this.element(), elementAttrConst.THEME);
	
	if(eleTheme)
	{
		eleTheme = CF.evalSilently(eleTheme, {});
		this.theme(eleTheme);
	}
	else
	{
		var globalTheme = this._renderContextAttrChartTheme();
		this.theme(globalTheme);
	}
};

/**
 * 初始化图表监听器。
 * 此函数依次从图表元素、<body>元素的elementAttrConst.LISTENER属性获取监听器对象。
 */
chartProto._initListener = function()
{
	var listener = CF.eleAttr(this.element(), elementAttrConst.LISTENER);
	
	if(listener)
		listener = CF.evalSilently(listener);
	else
		listener = this._bodyListener();
	
	this.listener(listener);
};

chartProto._bodyListener = function()
{
	var listenerStr = CF.eleAttr(document.body, elementAttrConst.LISTENER);
	
	if(listenerStr !== CF._PREV_BODY_LISTENER_STR)
	{
		CF._PREV_BODY_LISTENER_STR = listenerStr;
		CF._PREV_BODY_LISTENER = CF.evalSilently(listenerStr);
	}
	
	return CF._PREV_BODY_LISTENER;
};

/**
 * 初始化图表是否禁用交互设置。
 * 此函数从图表元素的elementAttrConst.DISABLE_SETTING属性获取是否禁用值。
 */
chartProto._initDisableSetting = function()
{
	var setting;
	
	var options = this.options();
	var optionValue = CF.builtinOptionValue(options, builtinOptionNames.disableSetting);
	
	//图表选项里的优先级应最高，不然图表展示页的选项不起效
	if(!CF.isNullOrEmpty(optionValue))
	{
		setting = this._evalDisableSettingAttr(optionValue);
	}
	else
	{
		var globalSetting = this._bodyDisableSetting();
		setting = CF.eleAttr(this.element(), elementAttrConst.DISABLE_SETTING);
		
		if(!CF.isNullOrEmpty(setting))
		{
			setting = this._evalDisableSettingAttr(setting);
			setting = CF.extend({}, globalSetting, setting);
		}
		else
		{
			setting = globalSetting;
		}
	}
	
	this.disableSetting(setting);
};

chartProto._bodyDisableSetting = function()
{
	var settingStr = CF.eleAttr(document.body, elementAttrConst.DISABLE_SETTING);
	
	if(settingStr !== CF._PREV_BODY_DISABLESETTING_STR)
	{
		CF._PREV_BODY_DISABLESETTING_STR = settingStr;
		CF._PREV_BODY_DISABLESETTING = this._evalDisableSettingAttr(settingStr);
	}
	
	return CF._PREV_BODY_DISABLESETTING;
};

chartProto._evalDisableSettingAttr = function(settingAttr)
{
	var setting = {};
	
	if(CF.isNullOrEmpty(settingAttr))
		settingAttr == "false";
	
	if(settingAttr == "false" || settingAttr == false)
	{
		setting.param = false;
		setting.data = false;
	}
	else if(settingAttr == "true" || settingAttr == true)
	{
		setting.param = true;
		setting.data = true;
	}
	//字符串
	else if(CF.isString(settingAttr))
	{
		var evalSetting = CF.evalSilently(settingAttr, {});
		setting = CF.extend(setting, evalSetting);
	}
	//对象
	else
	{
		setting = CF.extend(setting, settingAttr);
	}
	
	return setting;
};

/**
 * 初始化自定义图表渲染器。
 * 此函数从图表元素的elementAttrConst.RENDERER属性获取自定义图表渲染器。
 */
chartProto._initRenderer = function()
{
	var renderer = CF.eleAttr(this.element(), elementAttrConst.RENDERER);
	renderer = (renderer ? CF.evalSilently(renderer) : null);
	
	this.renderer(renderer);
};

/**
 * 初始化图表属性值集。
 * 此函数从图表元素的elementAttrConst.ATTR_VALUES属性获取图表属性值集。
 */
chartProto._initAttrValues = function()
{
	var attrValues = CF.eleAttr(this.element(), elementAttrConst.ATTR_VALUES);
	attrValues = (attrValues ? CF.evalSilently(attrValues) : null);
	//注意：应该使用this.attrValuesOrigin()作为合并基础，因为可能this.attrValues()执行修改操作，
	//比如修改后chart.destroy()后再chart.render()
	attrValues = CF.extend(true, {}, this.attrValuesOrigin(), attrValues);
	
	this.attrValues(attrValues);
};

/**
 * 初始化更新追加模式。
 */
chartProto._initUpdateAppendMode = function()
{
	var options = this.options();
	var mode = CF.builtinOptionValue(options, builtinOptionNames.updateAppendMode);
	
	this.updateAppendMode(mode);
};

/**
 * 初始化开始扩展函数，默认什么也不做，留作扩展使用。
 */
chartProto._initForPre = function(){};

/**
 * 初始化完成扩展函数，默认什么也不做，留作扩展使用。
 */
chartProto._initForPost = function(){};

/**
 * 获取图表ID。
 */
chartProto.id = function()
{
	return this._root.id;
};

/**
 * 获取/设置图表对应的HTML元素ID。
 * 注意：设置操作仅应在图表未渲染、或者选然后图表元素ID有变更的情况下执行。
 * 
 * @param elementId 可选，要设置的元素ID
 */
chartProto.elementId = function(elementId)
{
	if(elementId === undefined)
		return this._root.elementId;
	else
	{
		if(CF.isNullOrEmpty(elementId))
			throw new Error("[elementId] required");
		
		this._root.elementId = elementId;
	}
};

/**
 * 获取/设置图表插件，可能null。
 * 设置操作应在图表渲染器执行。
 * 
 * @param plugin 可选，要设置的图表插件
 */
chartProto.plugin = function(plugin)
{
	if(plugin === undefined)
		return this._plugin;
	else
	{
		this._plugin = plugin;
	}
};

chartProto._pluginRenderer = function()
{
	var plugin = this.plugin();
	return (plugin ? plugin.renderer : null);
};

chartProto._pluginNonNull = function()
{
	var plugin = this.plugin();
	
	if(plugin == null)
		throw new Error("chart plugin required");
	
	return plugin;
};

/**
 * 获取/设置图表渲染上下文。
 * 
 * @param renderContext 可选，要设置的图表插件
 */
chartProto.renderContext = function(renderContext)
{
	if(renderContext === undefined)
		return this._root.renderContext;
	else
	{
		if(renderContext == null)
			throw new Error("[renderContext] required");
		
		this._root.renderContext = renderContext;
	}
};

/**
 * 获取/设置图表选项，这些选项通常用于控制图表展示、交互效果，格式为：{ ... }。
 * 
 * 图表初始化时会使用图表元素的"dg-chart-options"属性值执行设置操作。
 * 
 * 图表渲染器实现相关：
 * 图表渲染器应使用此函数获取并应用图表选项，另参考chart.inflateRenderOptions()、chart.inflateUpdateOptions()。
 * 
 * @param options 可选，要设置的图表选项，没有则执行获取操作
 * @returns 要获取的图表选项，格式为：{ ... }，不会为null
 */
chartProto.options = function(options)
{
	if(options === undefined)
	{
		return (this._root.options || (this._root.options = {}));
	}
	else
	{
		if(options == null)
			options = {};
		
		this._root.options = options;
	}
};

/**
 * 获取/设置图表主题，格式参考：org.datagear.analysis.ChartTheme。
 * 
 * 图表初始化时会使用图表元素的"dg-chart-theme"属性值执行设置操作。
 * 
 * 图表渲染器实现相关：
 * 图表渲染器应使用此函数获取并应用图表主题，另参考：chart.themeGradualColor()。
 * 
 * @param theme 可选，要设置的图表主题，会被此函数修改，没有则执行获取操作
 * @returns 要获取的主题，不会为null
 */
chartProto.theme = function(theme)
{
	if(!this._isRenderContextInited())
		throw new Error("chart is illegal state for : theme()");
	
	if(theme === undefined)
	{
		return (this._theme || (this._theme = this._renderContextAttrChartTheme()));
	}
	else
	{
		if(theme == null)
			theme = {};
		
		var globalTheme = this._renderContextAttrChartTheme();
		
		//这里不应采用复制一个新图表主题对象的方式，因为图表主题对象后续会关联创建很多<style>元素，
		//如果采用复制方式的话，也会重复创建<style>元素，导致不必要的资源占用
		
		if(theme !== globalTheme && !CF._themeInflated(theme))
		{
			CF._inflateChartThemeIf(theme);
			var extTheme = CF.extend(true, {}, globalTheme._RAW_CHART_THEME, theme);
			CF._inflateChartThemeIf(extTheme);
			CF.extend(theme, extTheme);
			CF._themeInflated(theme, true);
		}
		
		this._theme = theme;
	}
};

/**
 * 获取/设置图表监听器。
 * 图表监听器格式为：
 * {
 *   //可选，渲染图表完成回调函数
 *   render: function(chart){ ... },
 *   //可选，更新图表数据完成回调函数
 *   update: function(chart, chartResult){ ... },
 *   //可选，销毁图表完成回调函数
 *   destroy: function(chart){ ... },
 *   //可选，渲染图表前置回调函数，返回false将阻止渲染图表
 *   onRender: function(chart){ ... },
 *   //可选，更新图表数据前置回调函数，返回false将阻止更新图表数据
 *   onUpdate: function(chart, chartResult){ ... },
 *   //可选，销毁图表前置回调函数，返回false将阻止销毁图表
 *   onDestroy: function(chart){ ... }
 * }
 * 
 * 图表初始化时会使用图表元素的"dg-chart-listener"属性值执行设置操作。
 * 
 * @param listener 可选，要设置的监听器对象，没有则执行获取操作
 * @returns 要获取的监听器、null
 */
chartProto.listener = function(listener)
{
	if(listener === undefined)
		return this._listener;
	else
		this._listener = listener;
};

/**
 * 获取/设置图表是否禁用设置。
 * 
 * 图表初始化时会使用图表元素的"dg-chart-disable-setting"属性值执行设置操作。
 * 
 * @param setting 可选，禁用设置，格式为：
 * 					//全部禁用
 * 					true、"true"、
 * 					//全部启用
 * 					false、"false"、
 * 					//详细设置
 *					{
 *						//可选，是否禁用参数
 *						param: false || true,
 *						//可选，是否禁用数据透视表
 *						data: true || false
 *					}
 * @returns 要获取的禁用设置，格式为：{param: true、false, data: true、false}，不会为null
 */
chartProto.disableSetting = function(setting)
{
	var defaultSetting =
	{
		//影响图表主体功能，因此默认启用
		param: false,
		//不影响图表主体功能，因此默认禁用
		data: true
	};
	
	if(setting === undefined)
	{
		return (this._disableSetting || (this._disableSetting = defaultSetting));
	}
	else
	{
		if(setting == null)
		{
			setting = {};
		}
		else if(setting == true || setting == "true")
		{
			setting = {param: true, data: true};
		}
		else if(setting == false || setting == "false")
		{
			setting = {param: false, data: false};
		}
		
		this._disableSetting = CF.extend(defaultSetting, setting);
	}
};

/**
 * 获取/设置自定义图表渲染器。
 * 
 * 图表初始化时会使用图表元素的"dg-chart-renderer"属性值执行设置操作。
 * 
 * @param renderer 可选，要设置的自定义图表渲染器，自定义图表渲染器允许仅定义要重写的图表插件渲染器函数
 * @returns 要获取的自定义图表渲染器，没有则返回null
 */
chartProto.renderer = function(renderer)
{
	if(renderer === undefined)
		return this._renderer;
	else
		this._renderer = renderer;
};

/**
 * 获取/设置结果数据格式。
 * 设置了新的结果数据格式后，下一次图表刷新数据将采用这个新格式。
 * 
 * @param resultDataFormat 可选，要设置的结果数据格式，结构参考：org.datagear.analysis.ResultDataFormat
 * @returns 要获取的结果数据格式，没有则返回null
 */
chartProto.resultDataFormat = function(resultDataFormat)
{
	if(resultDataFormat === undefined)
		return this._root.resultDataFormat;
	else
		this._root.resultDataFormat = resultDataFormat;
};

/**
 * 渲染图表。
 * 此函数在图表生命周期内仅允许调用一次。
 * 渲染中的图表处于this.statusRendering()状态，渲染完成后处于this.statusRendered()状态。 
 * 
 * 注意：
 * 只有this.statusPreInit()或者this.statusInited()或者this.statusPreRender()或者statusDestroyed()为true，此函数才允许执行。
 * 特别地，当处于this.statusPreInit()时，此函数内部会先调用this.init()函数。
 */
chartProto.render = function()
{
	if(this.statusPreInit())
		this.init();
	
	if(!this.statusInited() && !this.statusPreRender() && !this.statusDestroyed())
		throw new Error("chart is illegal state for : render()");
	
	if(CF.renderedChart(this.elementJquery()) != null)
		throw new Error("element '#"+this.elementId()+"' has been rendered as chart");
	
	this.statusRendering(true);
	
	var lib = this._rendererLib();
	
	if(lib)
	{
		var contextCharts = this._contextCharts();
		var thisChart = this;
		
		CF.loadLib(lib, function()
		{
			thisChart._renderInner();
		},
		contextCharts);
	}
	else
	{
		this._renderInner();
	}
};

chartProto._contextCharts = function()
{
	return [];
};

chartProto._renderInner = function()
{
	var doRender = true;
	
	var listener = this.listener();
	if(listener && listener.onRender)
		doRender = listener.onRender(this);
	
	if(doRender !== false)
	{
		this.doRender();
	}
};

chartProto._rendererLib = function()
{
	//优先
	var lib = CF.rendererLib(this.renderer());
	
	//其次
	if(lib == null)
	{
		lib = CF.rendererLib(this._pluginRenderer());
		lib = CF.convertPluginRendererLib(this, lib);
	}
	
	return lib;
};

/**
 * 调用底层图表渲染器的render函数，执行渲染。
 */
chartProto.doRender = function()
{
	if(!this.statusRendering())
		throw new Error("chart is illegal state for : doRender()");
	
	var ele = this.element();
	var theme = this.theme();
	var options = this.options();
	
	CF.eleAddClass(ele, CF.CHART_STYLE_NAME_FOR_INDICATION);
	//必须添加相对定位样式
	CF.eleAddClass(ele, CF.CHART_STYLE_NAME_FOR_RELATIVE);
	CF.addThemeRefEntity(theme, this.id());
	this._createChartThemeCssIfNon();
	CF.eleAddClass(ele, this.themeStyleName());
	
	if(CF.builtinOptionValue(options, builtinOptionNames.beautifyScrollbar) !== false)
		CF.eleAddClass(ele, "dg-chart-beautify-scrollbar");
	
	CF.eleData(ele, CF.ELE_RENDERED_CHART_NAME, this);
	
	var async = this.isAsyncRender();
	var renderer = this.renderer();
	var pluginRenderer = this._pluginRenderer();
	
	if(renderer && renderer.render)
	{
		renderer.render(this);
	}
	else if(pluginRenderer)
	{
		pluginRenderer.render(this);
	}
	else
		throw new Error("chart renderer required");
	
	if(!async)
		this.statusRendered(true);
};

var CHART_STYLE_SHEET_NAME = CF.BUILTIN_PROP_PREFIX + "ChartBasicStyle";

chartProto._createChartThemeCssIfNon = function()
{
	var theme = this.theme();
	var thumbBgColor = this.themeGradualColor(0.2);
	
	this.themeStyleSheet(CHART_STYLE_SHEET_NAME, function()
	{
		var css=
		[
			{
				name: "",
				value:
				{
					"color": theme.color,
					"background-color": theme.backgroundColor,
					"border-color": theme.borderColor,
					"font-size": CF.toCssFontSize(theme.fontSize)
				}
			},
			{
				name:
				[
					".dg-chart-beautify-scrollbar::-webkit-scrollbar",
					".dg-chart-beautify-scrollbar *::-webkit-scrollbar"
				],
				value:
				{
					"width": "0.8rem",
					"height": "0.8rem"
				}
			},
			{
				name:
				[
					".dg-chart-beautify-scrollbar::-webkit-scrollbar-thumb",
					".dg-chart-beautify-scrollbar *::-webkit-scrollbar-thumb"
				],
				value:
				{
					"border-radius": "3px",
					"background": thumbBgColor
				}
			},
			{
				name:
				[
					".dg-chart-beautify-scrollbar::-webkit-scrollbar-track",
					".dg-chart-beautify-scrollbar::-webkit-scrollbar-corner",
					".dg-chart-beautify-scrollbar *::-webkit-scrollbar-track",
					".dg-chart-beautify-scrollbar *::-webkit-scrollbar-corner"
				],
				value:
				{
					"background": theme.backgroundColor
				}
			}
		];
		
		if(theme.borderWidth)
		{
			//边框宽度和样式应该限定图表元素本身，而非其它也使用了相同样式类名的元素
			css.push(
			{
				name: "."+CF.CHART_STYLE_NAME_FOR_INDICATION,
				value:
				{
					"border-width": theme.borderWidth,
					"border-style": "solid"
				}
			});
		}
		
		return css;
	});
};

/**
 * 更新图表。
 * 此函数在图表生命周期内允许被调用多次。 
 * 更新中的图表处于this.statusUpdating()状态，更新完成后处于this.statusUpdated()状态。 
 * 
 * 注意：只有this.statusRendered()或者this.statusPreUpdate()或者this.statusUpdated()为true，此函数才会执行。
 * 
 * @param chartResult 可选，图表结果，如果不设置，将使用this.updateResult()的返回值
 */
chartProto.update = function(chartResult)
{
	if(!this.statusRendered() && !this.statusPreUpdate() && !this.statusUpdated())
		throw new Error("chart is illegal state for : update()");
	
	chartResult = (chartResult === undefined ? this.updateResult() : chartResult);
	
	this.statusUpdating(true);
	
	var appendMode = this.updateAppendMode();
	if(appendMode && appendMode.beforeListener)
	{
		chartResult = this._appendUpdateResult(chartResult, appendMode);
	}
	
	var doUpdate = true;
	
	var listener = this.listener();
	if(listener && listener.onUpdate)
		doUpdate = listener.onUpdate(this, chartResult);
	
	if(doUpdate != false)
	{
		this.doUpdate(chartResult);
	}
};

/**
 * 调用底层图表渲染器的update函数，执行更新数据。
 * 
 * @param chartResult 图表结果
 */
chartProto.doUpdate = function(chartResult)
{
	if(!this.statusUpdating())
		throw new Error("chart is illegal state for : doUpdate()");
	
	var appendMode = this.updateAppendMode();
	if(appendMode && !appendMode.beforeListener)
	{
		chartResult = this._appendUpdateResult(chartResult, appendMode);
	}
	
	//先保存结果，确保updateResult()在渲染器的update函数作用域内可用
	this.updateResult(chartResult);
	
	var async = this.isAsyncUpdate(chartResult);
	var renderer = this.renderer();
	var pluginRenderer = this._pluginRenderer();
	
	if(renderer && renderer.update)
	{
		renderer.update(this, chartResult);
	}
	else if(pluginRenderer)
	{
		pluginRenderer.update(this, chartResult);
	}
	else
		throw new Error("chart renderer required");
	
	if(!async)
		this.statusUpdated(true);
};

//将上次更新结果前置合并至给定图表的结果
//只要设置了appendMode，此方法旧不会返回null，确保后续不会出现空指针问题
chartProto._appendUpdateResult = function(chartResult, appendMode)
{
	if(!appendMode)
		return chartResult;
	
	var oldChartResult = this.updateResult();
	
	var olds = (this.results(oldChartResult) || []);
	var nows = (this.results(chartResult) || []);
	
	var merges = [];
	var mergeDataSize = (CF.isFunction(appendMode.size) ? appendMode.size(this, chartResult) : appendMode.size);
	var mergesLength = Math.max(olds.length, nows.length);
	
	for(var i=0; i<mergesLength; i++)
	{
		var oldData = this.resultDatas(olds[i]);
		var nowData = this.resultDatas(nows[i]);
		
		var mergeData = oldData.concat(nowData);
		
		if(mergeData.length > mergeDataSize)
		{
			mergeData = mergeData.slice(mergeData.length - mergeDataSize);
		}
		
		//采用CF.extend()方式，可保留nows[i]的其他属性
		merges[i] = CF.extend({}, nows[i]);
		merges[i].data = mergeData;
	}
	
	if(chartResult == null)
		chartResult = {};
	
	this.results(chartResult, merges);
	
	return chartResult;
};

/**
 * 重新调整图表尺寸。
 * 
 * 图表渲染器实现相关：
 * 图表渲染器可选实现resize函数，以支持此特性。
 */
chartProto.resize = function()
{
	this._assertActive();

	var renderer = this.renderer();
	var pluginRenderer = this._pluginRenderer();
	
	if(renderer && renderer.resize)
	{
		renderer.resize(this);
	}
	else if(pluginRenderer && pluginRenderer.resize)
	{
		pluginRenderer.resize(this);
	}
	//什么也不做
	else {}
};

/**
 * 销毁图表，释放图表占用的资源、恢复图表HTML元素初值。
 * 销毁中的图表处于this.statusDestroying()状态，销毁完成后处于this.statusDestroyed()状态。
 * 
 * 图表渲染器实现相关：
 * 图表渲染器应实现destroy函数，以支持此特性。
 * 
 * @returns true 正常执行销毁；false 未执行销毁，因为图表处于销毁非法状态
 */
chartProto.destroy = function()
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

/**
 * 调用底层图表渲染器的destroy函数，执行更新数据。
 */
chartProto.doDestroy = function()
{
	if(!this.statusDestroying())
		throw new Error("chart is illegal state for : doDestroy()");
	
	this._doDestroy();
	
	var renderer = this.renderer();
	var pluginRenderer = this._pluginRenderer();
	
	if(renderer && renderer.destroy)
	{
		renderer.destroy(this);
	}
	else if(pluginRenderer && pluginRenderer.destroy)
	{
		pluginRenderer.destroy(this);
	}
	else
	{
		CF.eleEmpty(this.element());
	}
	
	this.internal(null);
	//最后清空，因为上面逻辑可能会使用到
	this._clearLiveData();
	
	this.statusDestroyed(true);
};

chartProto._doDestroy = function()
{
	var ele = this.element();
	var theme = this.theme();
	
	var classes =
	[
		this.themeStyleName(),
		CF.CHART_STYLE_NAME_FOR_RELATIVE,
		"dg-chart-beautify-scrollbar",
		CF.CHART_STYLE_NAME_FOR_INDICATION
	];
	
	CF.eleRemoveClass(ele, classes);
	CF.eleRemoveData(ele, CF.ELE_RENDERED_CHART_NAME);
	
	//应在这里先销毁图表元素内部创建的元素，因为renderer.destroy()可能会清空图表元素
	this._doDestroySetting();
	CF.removeThemeRefEntity(theme, this.id());
};

/**
 * 销毁图表交互设置。
 */
chartProto._doDestroySetting = function()
{
	if(CF.chartSetting && CF.chartSetting.unbindChartSettingPanelEvent)
		CF.chartSetting.unbindChartSettingPanelEvent(this);
};

/**
 * 图表的render函数是否是异步的。
 */
chartProto.isAsyncRender = function()
{
	var renderer = this.renderer();
	var pluginRenderer = this._pluginRenderer();
	
	if(renderer && renderer.asyncRender !== undefined)
	{
		if(CF.isFunction(renderer.asyncRender))
		{
			return (renderer.asyncRender(this) === true);
		}
		else
			return (renderer.asyncRender === true);
	}
	
	if(!pluginRenderer)
		return false;
	
	if(CF.isFunction(pluginRenderer.asyncRender))
	{
		return (pluginRenderer.asyncRender(this) === true);
	}
	else
		return (pluginRenderer.asyncRender === true);
};

/**
 * 图表的update函数是否是异步的。
 * 
 * @param chartResult 图表结果
 */
chartProto.isAsyncUpdate = function(chartResult)
{
	var renderer = this.renderer();
	var pluginRenderer = this._pluginRenderer();
	
	if(renderer && renderer.asyncUpdate !== undefined)
	{
		if(CF.isFunction(renderer.asyncUpdate))
		{
			return (renderer.asyncUpdate(this, chartResult) === true);
		}
		else
			return (renderer.asyncUpdate === true);
	}
	
	if(!pluginRenderer)
		return false;
	
	if(CF.isFunction(pluginRenderer.asyncUpdate))
	{
		return (pluginRenderer.asyncUpdate(this, chartResult) === true);
	}
	else
	{
		return (pluginRenderer.asyncUpdate === true);
	}
};

/**
 * 图表是否处于活跃可用的状态（已完成渲染且未执行销毁）。
 */
chartProto.isActive = function()
{
	return (this._isActive === true);
};

/**
 * 断言图表处于活跃的状态。
 */
chartProto._assertActive = function()
{
	if(this.isActive())
		return;
	
	throw new Error("chart not active");
};

/**
 * 图表是否是活着的（已执行渲染且未完成销毁）。
 */
chartProto.isAlive = function()
{
	return (this._isAlive === true);
};

/**
 * 断言图表处于活着的状态。
 */
chartProto._assertAlive = function()
{
	if(this.isAlive())
		return;
	
	throw new Error("chart not alive");
};

/**
 * 图表是否为/设置为：准备render。
 * 
 * @param set 可选，为true时设置状态；否则，判断状态
 */
chartProto.statusPreRender = function(set)
{
	if(set === true)
	{
		this._isActive = false;
		this._isAlive = false;
		this.status(chartStatusConst.PRE_RENDER);
	}
	else
		return (this.status() == chartStatusConst.PRE_RENDER);
};

/**
 * 图表是否为/设置为：正在render。
 * 
 * @param set 可选，为true时设置状态；否则，判断状态
 */
chartProto.statusRendering = function(set)
{
	if(set === true)
	{
		this._isActive = false;
		this._isAlive = true;
		this.status(chartStatusConst.RENDERING);
	}
	else
		return (this.status() == chartStatusConst.RENDERING);
};

/**
 * 图表是否为/设置为：完成render。
 * 
 * @param set 可选，为true时设置状态；否则，判断状态
 * @param postProcess 可选，当是设置操作时，是否执行后置操作，比如调用监听器的render函数，默认为true
 */
chartProto.statusRendered = function(set, postProcess)
{
	if(set === true)
	{
		this._isActive = true;
		this._isAlive = true;
		this.status(chartStatusConst.RENDERED);
		
		if(postProcess == null || postProcess == true)
			this._postProcessRendered();
	}
	else
		return (this.status() == chartStatusConst.RENDERED);
};

/**
 * 渲染完成后置处理。
 */
chartProto._postProcessRendered = function()
{
	this._renderSetting();
	this._bindEventHandlers();
	
	var listener = this.listener();
	if(listener && listener.render)
		listener.render(this);
};

/**
 * 渲染图表交互设置项。
 */
chartProto._renderSetting = function()
{
	var disableSetting = this.disableSetting();
	
	if(disableSetting.param && disableSetting.data)
		return;
	
	if(CF.chartSetting && CF.chartSetting.bindChartSettingPanelEvent)
		CF.chartSetting.bindChartSettingPanelEvent(this);
};

/**
 * 绑定初始图表事件处理函数。
 */
chartProto._bindEventHandlers = function()
{
	var ehs = this._eventHandlers();
	
	for(var i=0; i<ehs.length; i++)
		this.on(ehs[i].eventType, ehs[i].eventHandler);
};

/**
 * 解析元素上的全部图表事件处理函数。
 * 此函数从图表元素的所有以elementAttrConst.ON开头的属性获取事件处理函数。
 * 例如：
 * <div dg-chart-on-click="clickHandler"></div> 						定义"click"事件处理函数
 * <div dg-chart-on-mouseover="function(chartEvent){ ... }"></div>		定义"mouseover"事件处理函数
 */
chartProto._eventHandlers = function()
{
	var ehs = [];
	var prefix = elementAttrConst.ON;
	var ele = this.element();
	
	if(ele.hasAttributes())
	{
		var attrs = ele.attributes;
		for(var i=0; i<attrs.length; i++)
		{
			var item = attrs.item(i);
			var name = item.name;
			var value = item.value;
			
			if(name && name.indexOf(prefix) == 0 && name.length > prefix.length)
			{
				var eventType = name.substr(prefix.length);
				var eventHandler = CF.evalSilently(value);
				
				if(eventHandler)
					ehs.push({ eventType: eventType, eventHandler: eventHandler });
			}
		}
	}
	
	return ehs;
};

/**
 * 图表是否为/设置为：准备update。
 * 
 * @param set 可选，为true时设置状态；否则，判断状态
 */
chartProto.statusPreUpdate = function(set)
{
	if(set === true)
	{
		this._isActive = true;
		this._isAlive = true;
		this.status(chartStatusConst.PRE_UPDATE);
	}
	else
		return (this.status() == chartStatusConst.PRE_UPDATE);
};

/**
 * 图表是否为/设置为：正在update。
 * 
 * @param set 可选，为true时设置状态；否则，判断状态
 */
chartProto.statusUpdating = function(set)
{
	if(set === true)
	{
		this._isActive = true;
		this._isAlive = true;
		this.status(chartStatusConst.UPDATING);
	}
	else
		return (this.status() == chartStatusConst.UPDATING);
};

/**
 * 图表是否为/设置为：完成update。
 * 
 * @param set 可选，为true时设置状态；否则，判断状态
 * @param postProcess 可选，当是设置操作时，是否执行后置操作，比如调用监听器的update函数，默认为true
 */
chartProto.statusUpdated = function(set, postProcess)
{
	if(set === true)
	{
		this._isActive = true;
		this._isAlive = true;
		this.status(chartStatusConst.UPDATED);
		
		if(postProcess == null || postProcess == true)
			this._postProcessUpdated();
	}
	else
		return (this.status() == chartStatusConst.UPDATED);
};

/**
 * 执行更新完成后置处理。
 */
chartProto._postProcessUpdated = function()
{
	var listener = this.listener();
	if(listener && listener.update)
	{
		listener.update(this, this.updateResult());
	}
};

/**
 * 图表是否为/设置为：正在销毁。
 * 
 * @param set 可选，为true时设置状态；否则，判断状态
 */
chartProto.statusDestroying = function(set)
{
	if(set === true)
	{
		this._isActive = false;
		this._isAlive = true;
		this.status(chartStatusConst.DESTROYING);
	}
	else
		return (this.status() == chartStatusConst.DESTROYING);
};

/**
 * 图表是否为/设置为：已销毁。
 * 
 * @param set 可选，为true时设置状态；否则，判断状态
 * @param postProcess 可选，当是设置操作时，是否执行后置操作，比如调用监听器的destroy函数，默认为true
 */
chartProto.statusDestroyed = function(set, postProcess)
{
	if(set === true)
	{
		this._isActive = false;
		this._isAlive = false;
		this.status(chartStatusConst.DESTROYED);
		
		if(postProcess == null || postProcess == true)
			this._postProcessDestroyed();
	}
	else
		return (this.status() == chartStatusConst.DESTROYED);
};

chartProto._postProcessDestroyed = function()
{
	var listener = this.listener();
	if(listener && listener.destroy)
		listener.destroy(this);
};

/**
 * 获取/设置图表状态。
 * 注意：此函数的设置操作仅设置状态值，不执行任何其他逻辑，设置图表生命周期状态应使用具体的this.status*(true)函数。
 * 
 * @param status 可选，要设置的状态，不设置则执行获取操作
 */
chartProto.status = function(status)
{
	if(status === undefined)
		return (this._status || "");
	else
		this._status = status;
};

/**
 * 绑定事件处理函数。
 * 
 * 图表渲染器实现相关：
 * 图表渲染器应实现on函数，以支持此特性。
 * 
 * @param eventType 事件类型：click、dblclick、mousedown、mouseup、mouseover、mouseout
 * @param handler 事件处理函数：function(chartEvent){ ... }
 */
chartProto.on = function(eventType, handler)
{
	this._assertActive();
	
	var renderer = this.renderer();
	var pluginRenderer = this._pluginRenderer();
	
	if(renderer && renderer.on)
	{
		renderer.on(this, eventType, handler);
	}
	else if(pluginRenderer && pluginRenderer.on)
	{
		pluginRenderer.on(this, eventType, handler);
	}
	else
		throw new Error("chart renderer.on required");
};

/**
 * 解绑事件处理函数。
 * 注意：此函数在图表渲染完成后才可调用。
 * 
 * 图表渲染器实现相关：
 * 图表渲染器应实现off函数，以支持此特性。
 * 
 * @param eventType 事件类型：click、dblclick、mousedown、mouseup、mouseover、mouseout
 * @param handler 解绑的事件处理函数
 */
chartProto.off = function(eventType, handler)
{
	this._assertAlive();
	
	var renderer = this.renderer();
	var pluginRenderer = this._pluginRenderer();
	
	if(renderer && renderer.off)
	{
		renderer.off(this, eventType, handler);
	}
	else if(pluginRenderer && pluginRenderer.off)
	{
		pluginRenderer.off(this, eventType, handler);
	}
	else
		throw new Error("chart renderer.off required");
};

chartProto._dataSetBindOf = function(dataSetBind, nullable)
{
	nullable = (nullable == null ? false : nullable);
	
	//数据集绑定对象
	if(dataSetBind && dataSetBind.dataSet !== undefined)
		return dataSetBind;
	
	var re;
	
	//索引数值
	if(CF.isNumber(dataSetBind))
	{
		re = this.dataSetBindAt(dataSetBind);
	}
	else
	{
		//其他情况应直接赋值且不校验合法性
		re = dataSetBind;
	}
	
	if(!nullable && re == null)
		throw new Error("no DataSetBind found for : " + dataSetBind);
	
	return re;
};

/**
 * 获取/设置指定第一个数据集单个参数值。
 * 
 * @param name 参数名、参数索引
 * @param value 可选，要设置的参数值，不设置则执行获取操作
 * @param convert 可选，设置操作时是否将value转换为符合参数类型，默认值为：false
 */
chartProto.dataSetParamValueFirst = function(name, value, convert)
{
	return this.dataSetParamValue(0, name, value, convert);
};

/**
 * 获取/设置指定数据集单个参数值。
 * 
 * @param dataSetBind 指定数据集绑定或其索引
 * @param name 参数名、参数索引
 * @param value 可选，要设置的参数值，不设置则执行获取操作
 * @param convert 可选，设置操作时是否将value转换为符合参数类型，默认值为：false
 */
chartProto.dataSetParamValue = function(dataSetBind, name, value, convert)
{
	dataSetBind = this._dataSetBindOf(dataSetBind);
	
	if(CF.isString(name))
	{
		//name是字符串时不应使用下面的this._dataSetParamOf()函数逻辑，以允许获取/设置未定义的参数值，从而支持隐式参数
	}
	else
	{
		var param = this._dataSetParamOf(dataSetBind, name);
		name = param.name;
	}
	
	if(value === undefined)
	{
		var paramValues = this.dataSetParamValues(dataSetBind);
		return paramValues[name];
	}
	else
	{
		var myParamValues = {};
		myParamValues[name] = value;
		
		this.dataSetParamValues(dataSetBind, myParamValues, true, convert);
	}
};

/**
 * 获取/设置第一个数据集参数值集。
 * 
 * @param paramValues 可选，要设置的参数名/值集对象，或者是与数据集参数数组元素一一对应的参数值数组，不设置则执行获取操作
 * @param increment 可选，是否增量设置，保留未在paramValues中出现的参数值，默认值为：false
 * @param convert 可选，设置操作时是否将value转换为符合参数类型，默认值为：false
 */
chartProto.dataSetParamValuesFirst = function(paramValues, increment, convert)
{
	return this.dataSetParamValues(0, paramValues, increment, convert);
};

/**
 * 获取/设置指定数据集参数值集。
 * 
 * @param dataSetBind 指定数据集绑定或其索引
 * @param paramValues 可选，要设置的参数值集对象，或者是与数据集参数数组元素一一对应的参数值数组，不设置则执行获取操作
 * @param increment 可选，是否增量设置，保留未在paramValues中出现的参数值，默认值为：false
 * @param convert 可选，设置操作时是否将value转换为符合参数类型，默认值为：false
 * @returns 要获取的参数值集，不会null
 */
chartProto.dataSetParamValues = function(dataSetBind, paramValues, increment, convert)
{
	dataSetBind = this._dataSetBindOf(dataSetBind);
	var paramValuesCurrent = (dataSetBind.query.paramValues || (dataSetBind.query.paramValues = {}));
	
	if(paramValues === undefined)
		return paramValuesCurrent;
	else
	{
		paramValues = (paramValues || {});
		increment = (increment == null ? false : increment);
		convert = (convert == null ? false : convert);
		
		var params;
		
		if(CF.isArray(paramValues))
		{
			params = this.dataSetParams(dataSetBind);
			var len = Math.min(params.length, paramValues.length);
			var paramValuesObj = {};
			
			for(var i=0; i<len; i++)
			{
				var name = params[i].name;
				paramValuesObj[name] = (convert ? CF.convertDataSetParamValue(params[i], paramValues[i]) : paramValues[i]);
			}
			
			paramValues = paramValuesObj;
		}
		else
		{
			if(convert)
			{
				params = this.dataSetParams(dataSetBind);
				for(var i=0; i<params.length; i++)
				{
					var name = params[i].name;
					if(paramValues[name] !== undefined)
					{
						paramValues[name] = CF.convertDataSetParamValue(params[i], paramValues[name]);
					}
				}
			}
		}
		
		if(increment)
		{
			CF.extend(paramValuesCurrent, paramValues);
		}
		else
		{
			dataSetBind.query.paramValues = paramValues;
		}
	}
};

/**
 * 获取渲染此图表的图表部件ID。
 * 正常来说，此函数的返回值与期望渲染的图表部件ID相同（通常是this.elementWidgetId()的返回值），
 * 当不同时，表明服务端因加载图表异常（未找到或出现错误）而使用了一个备用图表，用于在页面展示异常信息。
 */
chartProto.widgetId = function()
{
	var chartWidget = this._root.widget;
	return (chartWidget ? chartWidget.id : null);
};

/**
 * 获取图表HTML元素。
 */
chartProto.element = function()
{
	var eleId = this.elementId();
	return document.getElementById(eleId);
};

/**
 * 获取图表HTML元素上的图表部件ID（"dg-chart-widget"属性值）。
 * 如果图表HTML元素上未设置过图表部件ID，将返回null。
 */
chartProto.elementWidgetId = function()
{
	return CF.elementWidgetId(this.element());
};

/**
 * 判断此图表是否由指定ID的图表部件渲染。
 * 
 * @param chartWidgetId 图表部件ID，通常是图表元素的"dg-chart-widget"值
 */
chartProto.isInstance = function(chartWidgetId)
{
	return (this.widgetId() == chartWidgetId);
};

var INTERNAL_LIVE_DATA_NAME = CF.BUILTIN_PROP_PREFIX + "Internal";

/**
 * 获取/设置图表底层组件。
 * 图表底层组件是用于为渲染图表提供底层支持的组件，比如：ECharts实例、表格组件、DOM元素等。
 * 
 * 图表渲染器实现相关：
 * 图表渲染器应在其render()函数内部使用此函数设置底层组件。
 * 
 * @param internal 可选，要设置的底层组件，不设置则执行获取操作。
 * @returns 要获取的底层组件，没有则返回null
 */
chartProto.internal = function(internal)
{
	if(internal === undefined)
		return this.liveData(INTERNAL_LIVE_DATA_NAME);
	else
		this.liveData(INTERNAL_LIVE_DATA_NAME, internal);
};

/**
 * 获取/设置图表渲染上下文的属性值。
 * 
 * @param attrName
 * @param attrValue 要设置的属性值，可选，不设置则执行获取操作
 */
chartProto.renderContextAttr = function(attrName, attrValue)
{
	return CF.renderContextAttr(this.renderContext(), attrName, attrValue);
};

/**
 * 获取/设置生命周期数据。
 * 所有生命周期数据都将在图表销毁后被清除。
 * 
 * @param name 名称
 * @param value 要设置的数据，可选，不设置则执行获取操作
 */
chartProto.liveData = function(name, data)
{
	if(data === undefined)
		return (this._liveDatas ? this._liveDatas[name] : undefined);
	else
	{
		if(this._liveDatas == null)
			this._liveDatas = {};
		
		this._liveDatas[name] = data;
	}
};

chartProto._clearLiveData = function()
{
	this._liveDatas = {};
};

/**
 * 获取指定标记的数据集字段，没有则返回null。
 * 
 * @param dataSetBind 数据集绑定或其索引
 * @param dataSign 与this.dataSignFullname()函数参数相同，为null表示筛选无任何标记的数据集字段
 * @return 数据集字段、null
 */
chartProto.dataSetFieldOfSign = function(dataSetBind, dataSign)
{
	var re = this._dataSetFieldsOfSign(dataSetBind, dataSign, 1, false);
	return (re.length > 0 ? re[0] : null);
};

/**
 * 获取指定标记的数据集字段数组。
 * 
 * @param dataSetBind 数据集绑定或其索引
 * @param dataSign 与this.dataSignFullname()函数参数相同，为null表示筛选无任何标记的数据集字段
 * @param sort 可选，是否对返回结果进行重排序，true 是；false 否。默认值为：true
 * @return []
 */
chartProto.dataSetFieldsOfSign = function(dataSetBind, dataSign, sort)
{
	return this._dataSetFieldsOfSign(dataSetBind, dataSign, -1, sort);
};

chartProto._dataSetFieldsOfSign = function(dataSetBind, dataSign, count, sort)
{
	dataSetBind = this._dataSetBindOf(dataSetBind);
	sort = (sort === undefined ? true : sort);
	
	var re = [];
	
	if(dataSign == null)
		return re;
	
	var fields = this.dataSetFields(dataSetBind, sort);
	var dataSignName = this.dataSignFullname(dataSign);
	
	for(var i=0; i<fields.length; i++)
	{
		if(this.isDataSetFieldSigned(dataSetBind, fields[i], dataSignName))
		{
			re.push(fields[i]);
			
			if(count > -1 && re.length >= count)
				break;
		}
	}
	
	return re;
};

/**
 * 获取/设置图表结果包含的指定数据集绑定对应的数据集结果。
 * 
 * @param chartResult 图表结果
 * @param dataSetBind 数据集绑定、索引数值
 * @param dataSetResult 可选，要设置的数据集结果
 * @return 要获取的数据集结果，没有则返回null
 */
chartProto.resultOf = function(chartResult, dataSetBind, dataSetResult)
{
	var dataSetResults = this.results(chartResult);
	var index = (CF.isNumber(dataSetBind) ? dataSetBind : (dataSetBind != null ? dataSetBind.index : null));
	
	if(dataSetResult === undefined)
	{
		return (dataSetResults ? dataSetResults[index] : null);
	}
	else
	{
		//是图表结果，检查并初始化结构
		if(dataSetResults == null)
		{
			dataSetResults = [];
			this.results(chartResult, dataSetResults);
		}
		
		dataSetResults[index] = dataSetResult;
	}
};

/**
 * 获取/设置数据集结果对象包含的数据。
 * 
 * @param dataSetResult 数据集结果
 * @param data 可选，要设置的数据，通常是：{ ... }、[ { ... }, ... ]，不设置则执行获取操作
 * @return 要获取的数据集结果数据，没有则返回null
 */
chartProto.resultData = function(dataSetResult, data)
{
	if(data === undefined)
		return (dataSetResult ? dataSetResult.data : null);
	else
		dataSetResult.data = data;
};

/**
 * 获取/设置指定数据集绑定对应的数据集结果对象包含的数据。
 * 
 * @param chartResult 图表结果、数据集结果数组
 * @param dataSetBind 数据集绑定、索引数值
 * @param data 可选，要设置的数据，通常是：{ ... }、[ { ... }, ... ]，不设置则执行获取操作
 * @return 要获取的数据集结果数据，没有则返回null
 */
chartProto.resultDataOf = function(chartResult, dataSetBind, data)
{
	var dataSetResult = this.resultOf(chartResult, dataSetBind);
	
	if(data === undefined)
	{
		return this.resultData(dataSetResult);
	}
	else
	{
		//中间对象为null时，应该先初始化
		if(dataSetResult == null)
		{
			dataSetResult = {};
			this.resultOf(chartResult, dataSetBind, dataSetResult);
		}
		
		this.resultData(dataSetResult, data);
	}
};

/**
 * 获取数据集结果包含的数据对象数组。
 * 如果dataSetResult为null，返回空数组：[]；如果数据对象是数组，则直接返回；否则，返回：[ 数据对象 ]。
 * 
 * @param dataSetResult 数据集结果
 * @return 不会为null的数组
 */
chartProto.resultDatas = function(dataSetResult)
{
	if(dataSetResult == null || dataSetResult.data == null)
		return [];
	
	if(CF.isArray(dataSetResult.data))
		return dataSetResult.data;
	
	return [ dataSetResult.data ];
};

/**
 * 获取指定数据集绑定对应的数据集结果对象包含的数据对象数组。
 * 
 * @param chartResult 图表结果、数据集结果数组
 * @param dataSetBind 数据集绑定、索引数值
 * @return 不会为null的数组
 */
chartProto.resultDatasOf = function(chartResult, dataSetBind)
{
	var dataSetResult = this.resultOf(chartResult, dataSetBind);
	return this.resultDatas(dataSetResult);
};

/**
 * 获取指定地图名对应的地图数据地址。
 * 此函数先从CF.chartMapURLs查找对应的地址，如果没有，则直接返回name作为地址。
 * 
 * @param name 地图名称
 */
chartProto.mapURL = function(name)
{
	var url = chartMapURLs[name];
	
	if(!url && CF.isFunction(chartMapURLs.mapURL))
		url = chartMapURLs.mapURL(name);
	
	url = this.contextURL(url || name);
	
	return url;
};

var RENDER_OPTIONS_LIVE_DATA_NAME = CF.BUILTIN_PROP_PREFIX + "RenderOptions";

/**
 * 获取/设置图表渲染选项。
 * 
 * 图表渲染器可在其render()中使用此函保存图表渲染选项，然后在其update()中获取渲染选项。
 * 调用chart.processRenderOptions()后，默认会自动调用此函数设置图表渲染选项。
 * 
 * @param renderOptions 可选，要设置的渲染选项对象，通常由图表渲染器内部渲染选项、chart.options()合并而成，格式应为：{ ... }
 * @returns 要获取的图表渲染选项，没有则返回null
 */
chartProto.renderOptions = function(renderOptions)
{
	if(renderOptions === undefined)
		return this.liveData(RENDER_OPTIONS_LIVE_DATA_NAME);
	else
		return this.liveData(RENDER_OPTIONS_LIVE_DATA_NAME, renderOptions);
};

var UPDATE_OPTIONS_LIVE_DATA_NAME = CF.BUILTIN_PROP_PREFIX + "UpdateOptions";

/**
 * 获取/设置图表更新选项。
 * 
 * 图表渲染器可在其update()中使用此函保存图表更新选项，供后续图表监听器使用。
 * 调用chart.processUpdateOptions()后，默认会自动调用此函数设置图表更新选项。
 * 
 * @param updateOptions 可选，要设置的渲染选项对象，格式应为：{ ... }
 * @returns 要获取的图表更新选项，没有则返回null
 */
chartProto.updateOptions = function(updateOptions)
{
	if(updateOptions === undefined)
		return this.liveData(UPDATE_OPTIONS_LIVE_DATA_NAME);
	else
		this.liveData(UPDATE_OPTIONS_LIVE_DATA_NAME, updateOptions);
};

/**
 * 处理图表渲染选项。
 * 如果this.options()中有定义processRenderOptions选项函数（格式为：function(renderOptions, chart){ ... }），则调用它；否则，什么也不做。
 * 
 * @param renderOptions 待处理的渲染选项，通常由图表渲染器render()函数内部生成，格式应为：{ ... }
 * @param set 可选，是否在处理完后调用chart.renderOptions()设置图表渲染选项，默认值为：true 
 * @returns renderOptions
 */
chartProto.processRenderOptions = function(renderOptions, set)
{
	set = (set == null ? true : set);
	
	var options = this.options();
	var handler = CF.builtinOptionValue(options, builtinOptionNames.processRenderOptions);
	if(handler)
	{
		handler.call(options, renderOptions, this);
	}
	
	if(set)
	{
		this.renderOptions(renderOptions);
	}
	
	return renderOptions;
};

/**
 * 处理图表更新选项。
 * 如果this.options()中有定义processUpdateOptions选项函数（格式为：function(updateOptions, chart){ ... }），则调用它；否则，什么也不做。
 * 
 * @param updateOptions 待处理的更新选项，通常由图表渲染器update()函数内部生成，格式应为：{ ... }
 * @param set 可选，是否在处理完后调用chart.updateOptions()设置图表更新选项，默认值为：true
 * @returns updateOptions
 */
chartProto.processUpdateOptions = function(updateOptions, set)
{
	set = (set == null ? true : set);
	
	var options = this.options();
	var handler = CF.builtinOptionValue(options, builtinOptionNames.processUpdateOptions);
	if(handler)
	{
		handler.call(options, updateOptions, this);
	}
	
	if(set)
	{
		this.updateOptions(updateOptions);
	}
	
	return updateOptions;
};

/**
 * 将源选项对象深度合并至目标选项中。
 * 此函数支持如下调用方式：
 * inflateOptions(target);
 * inflateOptions(target, filter);
 * inflateOptions(target, source);
 * inflateOptions(target, source, filter);
 * 
 * @param target 合并目标选项对象，格式应为：{ ... }
 * @param source 合并源选项对象，格式应为：{ ... }，默认为：this.options()
 * @param filter 可选，合并过滤器，true 全部合并；false 仅合并在target中存在的属性（仅顶级属性）；
 * 				 源选项对象过滤函数，格式为：function(name, value){ return true、false; }，返回false将不合并source中的对应属性
 * @returns target
 */
chartProto.inflateOptions = function(target, source, filter)
{
	// (target)
	if(arguments.length == 1)
	{
		source = this.options();
		filter = null;
	}
	// (target, filter)
	else if(arguments.length == 2 && (source === true || source === false || CF.isFunction(source)))
	{
		filter = source;
		source = this.options();
	}
	// (target, source)、(target, source, filter)
	
	filter = (filter == null ? true : filter);
	
	if(source != null)
	{
		if(filter === true)
		{
			CF.extend(true, target, source);
		}
		else
		{
			var srcOptions = {};
			
			//仅合并target中出现的同名项
			if(filter === false)
			{
				for(var name in target)
				{
					srcOptions[name] = source[name];
				}
			}
			//自定义
			else if(CF.isFunction(filter))
			{
				for(var name in source)
				{
					var value = source[name];
					
					if(filter(name, value) !== false)
					{
						srcOptions[name] = value;
					}
				}
			}
			else
				throw new Error("[filter] illegal");
			
			CF.extend(true, target, srcOptions);
		}
	}
	
	return target;
};

/**
 * 获取此图表主题对应的CSS类名。
 * 这个CSS类名是全局唯一的，可添加至HTML元素的"class"属性。
 * 
 * 图表在渲染前会自动为this.element()图表元素添加此CSS类，
 * 使得通过chart.themeStyleSheet(name, css)函数创建的样式表可自动应用于图表元素。
 * 
 * @returns CSS类名，不会为null
 */
chartProto.themeStyleName = function()
{
	var theme = this.theme();
	return CF.themeStyleName(theme);
};

/**
 * 判断/设置此图表主题和名称关联的CSS样式表，详细参考CF.themeStyleSheet()函数说明。
 * 
 * 使用方式：
 * 判断与此图表主题和名称关联的CSS样式表是否已设置（返回true或者false）：
 * chart.themeStyleSheet(name)
 * 如果未设置过，则设置此图表主题和名称关联的CSS样式表（返回chart.themeStyleName()函数结果）：
 * chart.themeStyleSheet(name, css)
 * 强制设置此图表主题和名称关联的CSS样式表（返回chart.themeStyleName()函数结果）：
 * chart.themeStyleSheet(name, css, true)
 * 判断与指定图表主题和名称关联的CSS样式表是否已设置（返回true或者false）
 * 
 * 图表渲染器在绘制HTML图表时，可以使用此函数设置与此图表主题对应的子元素CSS样式表，例如：
 * 假设有用于显示数据数目的HTML图表渲染器，它将绘制如下HTML图表：
 * <div dg-chart-widget="...">
 *   <span class="result-data-count">数目</span>
 * </div>
 * 可采用如下方式设置其CSS样式表：
 * {
 *   render: function(chart)
 *   {
 *     jQuery("<span class='result-data-count'>").appendTo(chart.element());
 *     //使用相同图表主题的多个图表将仅创建一个CSS样式表
 *     chart.themeStyleSheet("myChartTextStyle", function()
 *     {
 *       return { name: " .result-data-count", value: { color: chart.theme().color } };
 *     });
 *   },
 *   update: function(chart, chartResult)
 *   {
 *     jQuery(".result-data-count", chart.elementJquery()).text(chart.resultDatas(...).length);
 *   }
 * }
 * 
 * @param name 参考CF.themeStyleSheet()的name参数
 * @param css 参考CF.themeStyleSheet()的css参数
 * @param force 参考CF.themeStyleSheet()的force参数
 * 
 * @returns 参考CF.themeStyleSheet()的返回值
 */
chartProto.themeStyleSheet = function(name, css, force)
{
	var theme = this.theme();
	return CF.themeStyleSheet(theme, name, css, force);
};

/**
 * 获取/设置HTML元素的style样式。
 * 具体参考CF.eleStyle()函数。
 */
chartProto.elementStyle = function(element, css)
{
	return CF.eleStyle(element, css);
};

/**
 * 拼接CSS样式字符串。
 * 
 * @param css 同CF.styleString()函数参数。
 */
chartProto.styleString = function(css)
{
	var cssArray = [];
	
	for(var i=0; i<arguments.length; i++)
		cssArray.push(arguments[i]);
	
	return CF.styleString.apply(CF, cssArray);
};

/**
 * 获取/设置数据集别名。
 * 
 * @param dataSetBind 数据集绑定或其索引
 * @param alias 可选，要设置的别名，不设置则执行获取操作
 * @returns 要获取的别名，不会为null
 */
chartProto.dataSetAlias = function(dataSetBind, alias)
{
	dataSetBind = this._dataSetBindOf(dataSetBind);
	
	if(alias === undefined)
	{
		if(dataSetBind.alias)
			return dataSetBind.alias;
		
		var dataSet = (dataSetBind.dataSet || dataSetBind);
		
		return (dataSet ? (dataSet.name || "") : "");
	}
	else
	{
		dataSetBind.alias = alias;
	}
};

/**
 * 获取数据集字段数组。
 * 返回数组排序遵循如下规则：
 * 排序值越小越靠前；
 * 属性默认具有与其索引相同的排序值；
 * 当两个属性具有相同排序值时，设置了fieldOrders中排序值的那个属性靠前排（前置插入），否则，属性索引小的那个靠前排。
 * 
 * @param dataSetBind 数据集绑定或其索引、数据集
 * @param sort 可选，当dataSetBind是数据集绑定时，是否依据其fieldOrders对返回结果进行重排序，true 是；false 否。默认值为：true
 * @returns 数据集字段数组，返回空数组表示没有
 */
chartProto.dataSetFields = function(dataSetBind, sort)
{
	sort = (sort === undefined ? true : sort);
	
	var dataSet;
	var isDataSet;
	
	//数据集
	if(dataSetBind && dataSetBind.params !== undefined)
	{
		dataSet = dataSetBind;
		isDataSet = true;
	}
	//数据集绑定、索引数值
	else
	{
		dataSetBind = this._dataSetBindOf(dataSetBind);
		dataSet = dataSetBind.dataSet;
		isDataSet = false;
	}
	
	var fields = (dataSet && dataSet.fields ? dataSet.fields : []);
	
	if(isDataSet || !sort)
		return fields;
	
	var fieldOrders = dataSetBind.fieldOrders;
	
	if(!fieldOrders)
		return fields;
	
	var pos = [];
	
	for(var i=0; i<fields.length; i++)
	{
		var p = fields[i];
		pos[i] = { field: p, order: fieldOrders[p.name], index: i };
	}
	
	pos.sort(function(a, b)
	{
		var oa = (a.order != null ? a.order : a.index);
		var ob = (b.order != null ? b.order : b.index);
		
		var re = (oa - ob);
		
		if(re == 0)
		{
			//上面逻辑已保证这里不会出现(a.order == null && b.order == null)的情况
			if(a.order == null)
				re = 1;
			else if(b.order == null)
				re = -1;
			else
				re = a.index - b.index;
		}
		
		return re;
	});
	
	var re = [];
	
	for(var i=0; i<pos.length; i++)
		re[i] = pos[i].field;
	
	return re;
};

/**
 * 获取指定标识的数据集字段。
 * 
 * @param dataSetBind 数据集绑定或其索引、数据集
 * @param fieldInfo 数据集字段名、字段索引、字段对象
 * @returns 数据集字段，没有找到则返回null
 */
chartProto.dataSetField = function(dataSetBind, fieldInfo)
{
	return this._dataSetFieldOf(dataSetBind, fieldInfo, true);
};

chartProto._dataSetFieldOf = function(dataSetBind, fieldInfo, nullable)
{
	nullable = (nullable == null ? false : nullable);
	
	//字段对象
	if(fieldInfo && fieldInfo.name !== undefined)
		return fieldInfo;
	
	var re = null;
	
	var fields = this.dataSetFields(dataSetBind, false);
	
	//索引数值
	if(CF.isNumber(fieldInfo))
	{
		re = fields[fieldInfo];
	}
	else
	{
		//字段名
		for(var i=0; i<fields.length; i++)
		{
			if(fields[i].name == fieldInfo)
			{
				re = fields[i];
				break;
			}
		}
	}
	
	if(!nullable && re == null)
		throw new Error("no DataSetField found for : " + fieldInfo);
	
	return re;
};

/**
 * 获取/设置数据集字段别名。
 * 
 * @param dataSetBind 数据集绑定或其索引
 * @param field 数据集字段、字段名、字段索引
 * @param alias 可选，要设置的别名，不设置则执行获取操作
 * @returns 要获取的别名，不会为null
 */
chartProto.dataSetFieldAlias = function(dataSetBind, field, alias)
{
	dataSetBind = this._dataSetBindOf(dataSetBind);
	field = this._dataSetFieldOf(dataSetBind, field);
	
	if(alias === undefined)
	{
		alias =  (dataSetBind.fieldAliases ?
						dataSetBind.fieldAliases[field.name] : null);
		
		if(!alias)
			alias = (field.label ||  field.name);
		
		return (alias || "");
	}
	else
	{
		if(!dataSetBind.fieldAliases)
			dataSetBind.fieldAliases = {};
		
		dataSetBind.fieldAliases[field.name] = alias;
	}
};

/**
 * 获取/设置数据集字段排序值。
 * 
 * @param dataSetBind 数据集绑定或其索引
 * @param field 数据集字段、字段名、字段索引
 * @param order 可选，要设置的排序数值，不设置则执行获取操作
 * @returns 要获取的排序数值，没有设置过则返回null
 */
chartProto.dataSetFieldOrder = function(dataSetBind, field, order)
{
	dataSetBind = this._dataSetBindOf(dataSetBind);
	field = this._dataSetFieldOf(dataSetBind, field);
	
	if(order === undefined)
	{
		return (dataSetBind.fieldOrders ?
						dataSetBind.fieldOrders[field.name] : null);
	}
	else
	{
		if(!dataSetBind.fieldOrders)
			dataSetBind.fieldOrders = {};
		
		dataSetBind.fieldOrders[field.name] = order;
	}
};

/**
 * 获取数据集参数数组。
 * 
 * @param dataSetBind 数据集绑定或其索引、数据集
 * @returns 数据集参数数组，空数组表示没有参数
 */
chartProto.dataSetParams = function(dataSetBind)
{
	var dataSet;
	
	//数据集
	if(dataSetBind && dataSetBind.params !== undefined)
	{
		dataSet = dataSetBind;
	}
	//数据集绑定、索引数值
	else
	{
		dataSetBind = this._dataSetBindOf(dataSetBind);
		dataSet = dataSetBind.dataSet;
	}
	
	return (dataSet && dataSet.params ? dataSet.params : []);
};

/**
 * 获取指定标识的数据集参数。
 * 
 * @param dataSetBind 数据集绑定或其索引、数据集
 * @param paramInfo 数据集参数名、参数索引、参数对象
 * @returns 数据集参数，没有找到则返回null
 */
chartProto.dataSetParam = function(dataSetBind, paramInfo)
{
	return this._dataSetParamOf(dataSetBind, paramInfo, true);
};

chartProto._dataSetParamOf = function(dataSetBind, paramInfo, nullable)
{
	nullable = (nullable == null ? false : nullable);
	
	//参数对象
	if(paramInfo && paramInfo.name !== undefined)
		return paramInfo;
	
	var re = null;
	
	var params = this.dataSetParams(dataSetBind);
	
	if(!params)
	{
		re =  null;
	}
	//索引数值
	else if(CF.isNumber(paramInfo))
	{
		re = params[paramInfo];
	}
	else
	{
		//参数名
		for(var i=0; i<params.length; i++)
		{
			if(params[i].name == paramInfo)
			{
				re = params[i];
				break;
			}
		}
	}
	
	if(!nullable && re == null)
		throw new Error("no DataSetParam found for : " + paramInfo);
	
	return re;
};

/**
 * 判断是否有数据集参数。
 * 
 * @param dataSetBinds 可选，要判断的数据集绑定、索引数值，或者它们的数组，默认为：this.dataSetBinds()
 * @return true、false
 */
chartProto.hasDataSetParam = function(dataSetBinds)
{
	dataSetBinds = (dataSetBinds === undefined ? this.dataSetBinds() :
						(CF.isArray(dataSetBinds) ? dataSetBinds : [ dataSetBinds ]));
	
	for(var i=0; i<dataSetBinds.length; i++)
	{
		var dsb = this._dataSetBindOf(dataSetBinds[i]);
		var params = this.dataSetParams(dsb);
		
		if(params && params.length > 0)
			return true;
	}
	
	return false;
};

/**
 * 获取/设置指定数据集字段标记。
 * 
 * @param dataSetBind 数据集绑定或其索引
 * @param field 数据集字段名、字段索引、字段对象
 * @param sign 可选，不设置则执行获取操作，与this.dataSignFullname()函数参数相同、或者其数组
 * @returns 数据标记名字符串数组，空数组表示没有
 */
chartProto.dataSetFieldSigns = function(dataSetBind, field, sign)
{
	dataSetBind = this._dataSetBindOf(dataSetBind);
	field = this._dataSetFieldOf(dataSetBind, field);
	var fieldName = field.name;
	
	if(sign === undefined)
	{
		var re = (dataSetBind.fieldSigns ? dataSetBind.fieldSigns[fieldName] : null);
		return (re == null ? [] : re);
	}
	else
	{
		if(!dataSetBind.fieldSigns)
			dataSetBind.fieldSigns = {};
		
		sign = this._toDataSignValues(sign);
		dataSetBind.fieldSigns[fieldName] = sign;
	}
};

chartProto._toDataSignValues = function(dataSigns)
{
	if(dataSigns == null)
		return [];
	
	//字段标记值应是数组
	if(!CF.isArray(dataSigns))
		dataSigns = [ dataSigns ];
	
	var re = [];
	
	for(var i=0; i<dataSigns.length; i++)
	{
		var dsi = dataSigns[i];
		var value = this.dataSignFullname(dsi);
		
		//标记数组不应包含null，也不应有重复项
		if(value != null && CF.inArray(value, re) < 0)
		{
			re.push(value);
		}
	}
	
	return re;
};

/**
 * 判断给定数据集绑定是否是易变模型的。
 * 
 * @param dataSetBind 数据集绑定或其索引
 * @returns true、false
 */
chartProto.isMutableModel = function(dataSetBind)
{
	dataSetBind = this._dataSetBindOf(dataSetBind);
	return (dataSetBind.dataSet.mutableModel == true);
};

/**
 * 获取图表插件的所有资源列表。
 * 
 * @returns 插件资源列表，格式为：[ { name: "..." }, ... ]
 */
chartProto.pluginResources = function()
{
	var plugin = this._pluginNonNull();
	return (plugin.resources ? plugin.resources : []);
};

/**
 * 获取图表插件指定名称资源的URL。
 * 使用此URL可从服务端加载资源。
 * 
 * @param name 插件资源名，this.pluginResources()函数返回的其中一个资源名
 * @returns 
 */
chartProto.pluginResourceURL = function(name)
{
	name = (name || "");
	
	var plugin = this._pluginNonNull();
	var webContext = this._renderContextAttrWebContext();
	
	if(webContext == null)
		throw new Error("chart is illegal state for : pluginResourceURL(name)");
	
	var urlPrefix = webContext.attributes.pluginResUrlPrefix;
	var url = urlPrefix+"/"+encodeURIComponent(plugin.id)+"/"+name;
	url = this.contextURL(url);
	
	return url;
};

/**
 * 获取/设置指定图表属性值。
 * 注意：org.datagear.analysis.support.html.AttributeValueHtmlChartPlugin需要此函数名。
 * 
 * @param name 插件属性、名称
 * @param value 可选，要设置的属性值
 * @returns 
 */
chartProto.attrValue = function(name, value)
{
	name = (name && name.name != null ? name.name : name);
	
	if(value === undefined)
		return this._root.attrValues[name];
	else
		this._root.attrValues[name] = value;
};

/**
 * 获取/设置全部图表属性值。
 * 
 * @param values 可选，要设置的属性值映射表，格式为：{ 名称: 值, ... }
 * @returns { ... }
 */
chartProto.attrValues = function(values)
{
	if(values === undefined)
		return this._root.attrValues;
	else
		this._root.attrValues = (values ? values : {});
};

/**
 * 获取全部原始图表属性值，通常是在定义图表时设置的，未与"dg-chart-attr-values"合并。
 * 
 * @returns { ... }
 */
chartProto.attrValuesOrigin = function()
{
	return this._root.attrValuesOrigin;
};

/**
 * 获取所有插件属性。
 * 
 * @returns []
 */
chartProto.pluginAttributes = function()
{
	var plugin = this._pluginNonNull();
	return (plugin.attributes ? plugin.attributes : []);
};

/**
 * 获取原始图表选项，即在定义图表时设置的图表选项。
 * 
 * @param eval 可选，可选，是否返回选项对象而非字符串，默认为：false
 * @returns 字符串、{ ... }、null
 */
chartProto.optionsOrigin = function(eval)
{
	eval = (eval == null ? false : eval);
	
	var options = this._root.optionsOrigin;
	
	if(eval)
	{
		if(CF.isNullOrEmpty(options))
			options = null;
		else
			options = CF.evalSilently(options, {});
	}
	
	return options;
};

/**
 * 图表是否为/设置为：准备init。
 * 
 * @param set 可选，为true时设置状态；否则，判断状态
 */
chartProto.statusPreInit = function(set)
{
	if(set === true)
	{
		this._isActive = false;
		this._isAlive = false;
		this.status(chartStatusConst.PRE_INIT);
	}
	else
		return (this.status() == chartStatusConst.PRE_INIT);
};

/**
 * 图表是否为/设置为：正在init。
 * 
 * @param set 可选，为true时设置状态；否则，判断状态
 */
chartProto.statusIniting = function(set)
{
	if(set === true)
	{
		this._isActive = false;
		this._isAlive = false;
		this.status(chartStatusConst.INITING);
	}
	else
		return (this.status() == chartStatusConst.INITING);
};

/**
 * 图表是否为/设置为：完成init。
 * 
 * @param set 可选，为true时设置状态；否则，判断状态
 */
chartProto.statusInited = function(set)
{
	if(set === true)
	{
		this._isActive = false;
		this._isAlive = false;
		this.status(chartStatusConst.INITED);
	}
	else
		return (this.status() == chartStatusConst.INITED);
};

/**
 * 获取此图表的图表主题指定渐变因子的颜色。
 * 这个颜色是图表主题的实际背景色（actualBackgroundColor）与前景色（color）之间的某个颜色。
 * 
 * 图表渲染器在绘制图表时，可以使用此函数获取的颜色来设置图表配色。
 * 
 * @param factor 可选，渐变因子，0-1之间的小数，其中0表示最接近实际背景色的颜色、1表示最接近前景色的颜色
 * @returns 与factor匹配的颜色字符串，格式类似："#FFFFFF"，如果未设置factor，将返回一个包含所有渐变颜色的数组
 */
chartProto.themeGradualColor = function(factor)
{
	var theme = this.theme();
	return CF.themeGradualColor(theme, factor);
};

/**
 * 获取/设置更新追加模式。
 * 更新追加模式是指：每次调用chart.update()更新图表时，使用上次的数据追加合并新数据更新图表。
 * 图表初始化时，会使用图表选项里的updateAppendMode选项设置。
 * 
 * @param appendMode 可选，要设置的追加模式，格式为：
 * 					//等同于下面的：{ size: 10, beforeListener: false }
 * 					true、
 * 					//等同于下面的：{ size: 数值, beforeListener: false }
 * 					数值、
 * 					//等同于下面的：{ size: 函数, beforeListener: false }
 * 					function(chart, chartResult){ return 数值; }、
 * 					//具体追加模式
 * 					//size：数据窗口大小，追加后保留的最大数据数目（新数据优先），
 * 					//      可以是具体数值，也可以是数值计算函数：function(chart, chartResult){ return 数值; }
 * 					//beforeListener：是否在图表监听器的onUpdate前追加，否则，将在之后追加
 * 					{ size: 数值或者函数, beforeListener: false }
 * @returns 更新追加模式，格式为：{ size: 数值, beforeListener: true、false }、null 表示没有开启追加模式
 */
chartProto.updateAppendMode = function(appendMode)
{
	if(appendMode === undefined)
	{
		return this._updateAppendMode;
	}
	else
	{
		if(appendMode === true)
		{
			appendMode = { size: 10, beforeListener: false };
		}
		else if(CF.isNumber(appendMode))
		{
			appendMode = { size: appendMode, beforeListener: false };
		}
		else if(CF.isFunction(appendMode))
		{
			appendMode = { size: appendMode, beforeListener: false };
		}
		
		this._updateAppendMode = appendMode;
	}
};

/**
 * 获取全部数据集绑定数组。
 * 
 * @returns []，空数组表示没有数据集绑定
 */
chartProto.dataSetBinds = function()
{
	return (this._root.dataSetBinds || (this._root.dataSetBinds = []));
};

/**
 * 获取指定索引的数据集绑定。
 * 
 * @param index
 * @returns 数据集绑定，null表示没有
 */
chartProto.dataSetBindAt = function(index)
{
	var dsbs = this.dataSetBinds();
	return dsbs[index];
};

/**
 * 获取全部主件数据集绑定，或者设置了指定数据标记的全部主件数据集绑定。
 * 主件数据集绑定的用途是绘制图表。
 * 
 * @param dataSign 可选，要筛选的数据集标记，与this.dataSignFullname()函数参数相同，为null表示筛选无任何标记的数据集绑定
 * @returns []，空数组表示没有主件数据集绑定
 */
chartProto.dataSetBindsMain = function(dataSign)
{
	return this._dataSetBindsOf(-1, false, dataSign);
};

/**
 * 获取第一个主件数据集绑定，或者设置了指定数据标记的第一个主件数据集绑定。
 * 主件数据集绑定的用途是绘制图表。
 * 
 * @param dataSign 可选，要筛选的数据集标记，与this.dataSignFullname()函数参数相同，为null表示筛选无任何标记的数据集绑定
 * @returns 数据集绑定、null
 */
chartProto.dataSetBindMain = function(dataSign)
{
	var re = this._dataSetBindsOf(1, false, dataSign);
	return (re.length > 0 ? re[0] : null);
};

/**
 * 获取全部附件数据集绑定，或者设置了指定数据标记的全部附件数据集绑定。
 * 附件数据集绑定的用途不是绘制图表。
 * 
 * @param dataSign 可选，要筛选的数据集标记，与this.dataSignFullname()函数参数相同，为null表示筛选无任何标记的数据集绑定
 * @returns []，空数组表示没有附件数据集绑定
 */
chartProto.dataSetBindsAttachment = function(dataSign)
{
	return this._dataSetBindsOf(-1, true, dataSign);
};

/**
 * 获取第一个附件数据集绑定，或者设置了指定数据标记的第一个附件数据集绑定。
 * 附件数据集绑定的用途不是绘制图表。
 * 
 * @param dataSign 可选，要筛选的数据集标记，与this.dataSignFullname()函数参数相同，为null表示筛选无任何标记的数据集绑定
 * @returns 数据集绑定、null
 */
chartProto.dataSetBindAttachment = function(dataSign)
{
	var re = this._dataSetBindsOf(1, true, dataSign);
	return (re.length > 0 ? re[0] : null);
};

chartProto._dataSetBindsOf = function(count, attachment, dataSign)
{
	var re = [];
	
	var signFullname = (dataSign === undefined ? undefined : this.dataSignFullname(dataSign));
	
	var dataSetBinds = this.dataSetBinds();
	for(var i=0; i<dataSetBinds.length; i++)
	{
		if(count > -1 && re.length >= count)
			break;
		
		var dsb = dataSetBinds[i];
		var dsbAttachment = this.dataSetAttachment(dsb);
		
		if((!attachment && dsbAttachment) || (attachment && !dsbAttachment))
			continue;
		
		if(dataSign !== undefined && !this.isDataSetSigned(dsb, signFullname))
			continue;
		
		re.push(dsb);
	}
	
	return re;
};

/**
 * 为以"/"开头的URL添加系统根路径前缀，否则，将直接返回原URL。
 * 当需要访问系统内其他功能模块的资源时，应为其URL添加系统根路径前缀。
 * 
 * @param url 可选，要处理的URL
 * @returns 添加后的新URL，如果没有url参数，将返回系统根路径
 */
chartProto.contextURL = function(url)
{
	var webContext = this._renderContextAttrWebContext();
	
	if(webContext == null)
		throw new Error("chart is illegal state for : contextURL(url)");
	
	return CF.toWebContextPathURL(webContext, url);
};

/**
 * 加载库，并在全部加载完成后（无论是否成功）执行回调函数。
 * 注意：如果在图表渲染器的render/update函数中调用此函数，应该首先设置其asyncRender/asyncUpdate为true，
 * 并在callback中调用chart.statusRendered(true)/chart.statusUpdated(true)，具体参考此文件顶部的注释。
 * 
 * @param lib 库对象、数组，结构参考CF.loadLib()函数说明，注意，其中库源URL应是可以直接加载的
 * @param callback 可选，加载完成后回调函数（无论是否成功都将执行），格式参考CF.loadLib()函数说明
 */
chartProto.loadLib = function(lib, callback)
{
	callback = (callback ? callback : function(){});
	
	var contextCharts = this._contextCharts();
	CF.loadLib(lib,  callback, contextCharts);
};

var UPDATE_RESULT_LIVE_DATA_NAME = CF.BUILTIN_PROP_PREFIX + "UpdateResult";

/**
 * 获取/设置此次图表结果。
 * 图表更新前会自动执行设置操作（通过this.doUpdate()函数）。
 * 
 * @param chartResult 可选，要设置的图表结果
 * @returns 要获取的图表结果，没有则返回null
 */
chartProto.updateResult = function(chartResult)
{
	if(chartResult === undefined)
		return this.liveData(UPDATE_RESULT_LIVE_DATA_NAME);
	else
		this.liveData(UPDATE_RESULT_LIVE_DATA_NAME, chartResult);
};

/**
 * 获取/设置图表结果包含的数据集结果数组。
 * 
 * @param chartResult 图表结果
 * @param dataSetResults 可选，要设置的数据集结果数组
 * @returns 要获取的数据集结果数组，没有则返回null
 */
chartProto.results = function(chartResult, dataSetResults)
{
	if(dataSetResults === undefined)
	{
		return (chartResult == null ? null : chartResult.dataSetResults);
	}
	else
	{
		chartResult.dataSetResults = dataSetResults;
	}
};

/**
 * 获取图表插件指定附加属性值。
 * 
 * @param name 附加属性名
 * @returns 要获取的附加属性值，没有则返回null
 */
chartProto.pluginAddition = function(name)
{
	var plugin = this._pluginNonNull();
	return (plugin.additions ? plugin.additions[name] : null);
};

/**
 * 获取图表插件所有数据标记。
 * 
 * @returns []，空数组表示没有
 */
chartProto.pluginDataSigns = function()
{
	var plugin = this._pluginNonNull();
	return (plugin.dataSigns ? plugin.dataSigns : []);
};

/**
 * 获取图表插件指定数据标记。
 * 
 * @param name 数据标记名称、索引数字、数据标记对象
 * @param dataSigns 可选，要查找的数据标记数组，默认为：this.pluginDataSigns()
 * @returns 数据标记，没有则是null
 */
chartProto.pluginDataSign = function(name, dataSigns)
{
	dataSigns = (dataSigns === undefined ? this.pluginDataSigns() : dataSigns);
	
	if(dataSigns == null)
		return null;
	
	if(CF.isNumber(name))
	{
		return dataSigns[name];
	}
	else
	{
		//数据标记对象
		name = (name && name.name !== undefined ? name.name : name);
		
		for(var i=0; i<dataSigns.length; i++)
		{
			if(dataSigns[i] && dataSigns[i].name == name)
			{
				return dataSigns[i];
			}
		}
		
		return null;
	}
};

/**
 * 获取数据标记全名。
 * 
 * @param name 字符串全名、数据标记数组索引数值、数据标记对象，或者由数据标记名/索引数值/对象组成的层级数组（数组索引表示查找层级）
 * @param dataSigns 可选，要查找的数据标记数组，默认为：this.pluginDataSigns()
 * @returns 标记全名，层级间以'.'分隔，可能是null
 */
chartProto.dataSignFullname = function(name, dataSigns)
{
	if(name == null)
		return null;
	
	//字符串全名，直接返回
	if(CF.isString(name))
		return name;
	
	var isArray = CF.isArray(name);
	
	if(!isArray)
	{
		var dataSign = null;
		
		//插件数据标记数组索引数值
		if(CF.isNumber(name))
		{
			dataSign = this.pluginDataSign(name, dataSigns);
			
			if(dataSign == null)
				throw new Error("no DataSign found for : " + name);
			
			return dataSign.name;
		}
		//数据标记对象
		else if(name.name !== undefined)
		{
			dataSign = name;
		}
		
		return (dataSign ? dataSign.name : null);
	}
	else
	{
		var re = "";
		
		//默认查找数据标记数组，不能设为undefined，纤细参考this.pluginDataSign()函数
		var dftDataSigns = [];
		
		for(var i=0; i<name.length; i++)
		{
			var myPart = null;
			
			var ni = name[i];
			var dataSign = this.pluginDataSign(ni, dataSigns);
			
			if(dataSign == null)
			{
				if(ni == null || CF.isString(ni))
				{
					myPart = ni;
				}
				//数据标记对象
				else if(ni.name !== undefined)
				{
					myPart = ni.name;
					dataSign = ni;
				}
				else
				{
					throw new Error("no DataSign found for : name["+i+"]");
				}
			}
			else
			{
				myPart = dataSign.name;
			}
			
			re += (re ? (CF.DATA_SIGN_FULLNAME_SEPARATOR + myPart) : myPart);
			dataSigns = (dataSign && dataSign.children ? dataSign.children : dftDataSigns);
		}
		
		return re;
	}
};

/**
 * 获取/设置是否附件数据集。
 * 
 * @param dataSetBind 数据集绑定或其索引
 * @param attachment 可选，要设置的值
 * @returns true、false
 */
chartProto.dataSetAttachment = function(dataSetBind, attachment)
{
	dataSetBind = this._dataSetBindOf(dataSetBind);
	
	if(attachment === undefined)
	{
		return (dataSetBind.attachment ? true : false);
	}
	else
	{
		dataSetBind.attachment = attachment;
	}
};

/**
 * 判断数据集是否有指定数据标记。
 * 
 * @param dataSetBind 数据集绑定或其索引
 * @param dataSign 与this.dataSignFullname()函数参数相同
 * @returns true、false
 */
chartProto.isDataSetSigned = function(dataSetBind, dataSign)
{
	dataSetBind = this._dataSetBindOf(dataSetBind);
	dataSign = this.dataSignFullname(dataSign);
	
	var dss = this.dataSetSigns(dataSetBind);
	
	//此情况应返回true，用于支持查找没有任何标记的数据集绑定
	if(dataSign == null && (dss == null || dss.length == 0))
		return true;
	
	return (CF.inArray(dataSign, dss) >= 0);
};

/**
 * 判断数据集字段是否有指定数据标记。
 * 
 * @param dataSetBind 数据集绑定或其索引
 * @param field 数据集字段名、字段索引、字段对象
 * @param dataSign 与this.dataSignFullname()函数参数相同
 * @returns true、false
 */
chartProto.isDataSetFieldSigned = function(dataSetBind, field, dataSign)
{
	dataSign = this.dataSignFullname(dataSign);
	var fieldSigns = this.dataSetFieldSigns(dataSetBind, field);
	
	//此情况应返回true，用于支持查找没有任何标记的数据集字段
	if(dataSign == null && (fieldSigns == null || fieldSigns.length == 0))
		return true;
	
	return (CF.inArray(dataSign, fieldSigns) >= 0);
};

/**
 * 获取/设置数据集数据标记。
 * 
 * @param dataSetBind 数据集绑定或其索引
 * @param dataSigns 可选，要设置的标记，与this.dataSignFullname()函数参数相同、或者其数组
 * @returns 标记数组，空数组表示没有
 */
chartProto.dataSetSigns = function(dataSetBind, dataSigns)
{
	dataSetBind = this._dataSetBindOf(dataSetBind);
	
	if(dataSigns === undefined)
	{
		return (dataSetBind.dataSetSigns || (dataSetBind.dataSetSigns = []));
	}
	else
	{
		dataSigns = this._toDataSignValues(dataSigns);
		dataSetBind.dataSetSigns = dataSigns;
	}
};

/**
 * 获取/设置数据集字段标记映射表。
 * 
 * @param dataSetBind 数据集绑定或其索引
 * @param dataSigns 可选，要设置的数据标记映射表，格式为：{ 数据集字段名: 与this.dataSignFullname()函数参数相同、或者其数组, ... }，不设置则执行获取操作
 * @param increment 可选，设置操作时是否执行增量设置，仅设置signs中出现的项，true 是；false 否。默认值为：false
 * @returns 要获取的标记映射表，格式为：{ 数据集字段名: 标记名字符串数组、null, ... }，不会为null
 */
chartProto.dataSetFieldsSigns = function(dataSetBind, dataSigns, increment)
{
	dataSetBind = this._dataSetBindOf(dataSetBind);
	increment = (increment == null ? false : increment);
	
	if(dataSigns === undefined)
	{
		return (dataSetBind.fieldSigns || (dataSetBind.fieldSigns = {}));
	}
	else
	{
		var trimSigns = {};
		
		if(dataSigns)
		{
			for(var p in dataSigns)
			{
				var ps = this._toDataSignValues(dataSigns[p]);
				trimSigns[p] = ps;
			}
		}
		
		if(!dataSetBind.fieldSigns || !increment)
		{
			dataSetBind.fieldSigns = trimSigns;
		}
		else
		{
			for(var p in trimSigns)
			{
				dataSetBind.fieldSigns[p] = trimSigns[p];
			}
		}
	}
};

/**
 * 获取/设置数据集结果指定名称的附加数据。
 * 
 * @param dataSetResult 数据集结果
 * @param name 名称
 * @param value 可选，要设置的附加数据
 * @returns 附加数据，可能null
 */
chartProto.resultAddition = function(dataSetResult, name, value)
{
	var additions = (dataSetResult ? dataSetResult.additions : null);
	
	if(value === undefined)
	{
		return (additions ? additions[name] : null);
	}
	else
	{
		if(additions == null)
		{
			additions = {};
			dataSetResult.additions = additions;
		}
		
		additions[name] = value;
	}
};

/**
 * 获取未准备好（必填但值为null）的数据集参数信息。
 * 此函数支持的调用格式：
 * chart.unreadyDataSetParams();
 * chart.unreadyDataSetParams(stopOnFirst);
 * chart.unreadyDataSetParams(stopOnFirst, checkIgnoreFetch);
 * chart.unreadyDataSetParams(dataSetBinds);
 * chart.unreadyDataSetParams(dataSetBinds, stopOnFirst);
 * chart.unreadyDataSetParams(dataSetBinds, stopOnFirst, checkIgnoreFetch);
 * 
 * @param dataSetBinds 可选，要查找的数据集绑定、索引数值，或者它们的数组，默认为：this.dataSetBinds()
 * @param stopOnFirst 可选，是否在找到第一个后就返回，默认值为：false
 * @param checkIgnoreFetch 可选，是否校验忽略获取结果的数据集，默认值为：false
 * @returns 未准备好的数据集参数信息数组，格式为：
 * 				[
 * 					{ dataSetBind: 数据集绑定, dataSetBindIndex: 数据集绑定索引, param: 数据集参数对象, paramIndex: 参数索引 },
 * 					...
 * 				]，空数组表示都已准备好
 */
chartProto.unreadyDataSetParams = function(dataSetBinds, stopOnFirst, checkIgnoreFetch)
{
	//(true, ...)、(false, ...)
	if(dataSetBinds === true || dataSetBinds === false)
	{
		checkIgnoreFetch = stopOnFirst;
		stopOnFirst = dataSetBinds;
		dataSetBinds = undefined;
	}
	
	dataSetBinds = (dataSetBinds === undefined ? this.dataSetBinds() :
						(CF.isArray(dataSetBinds) ? dataSetBinds : [ dataSetBinds ]));
	stopOnFirst = (stopOnFirst == null ? false : stopOnFirst);
	checkIgnoreFetch = (checkIgnoreFetch == null ? false: checkIgnoreFetch);
	
	var re = [];
	
	for(var i=0; i<dataSetBinds.length; i++)
	{
		var dsb = this._dataSetBindOf(dataSetBinds[i]);
		
		if(!checkIgnoreFetch && this.dataSetIgnoreFetch(dsb))
			continue;
		
		var params = this.dataSetParams(dsb);
		
		if(!params || params.length == 0)
			continue;
		
		var paramValues = (this.dataSetParamValues(dsb) || {});
		
		for(var j=0; j<params.length; j++)
		{
			var param = params[j];
			
			if(this._isDataSetParamUnready(param, paramValues))
			{
				var info = { dataSetBind: dsb, dataSetBindIndex: i, param: param, paramIndex: j };
				re.push(info);
				
				if(stopOnFirst === true)
					return re;
			}
		}
	}
	
	return re;
};

chartProto._isDataSetParamUnready = function(dataSetParam, paramValues)
{
	var required = (dataSetParam.required == true || dataSetParam.required == "true");
	return (required && (paramValues == null || paramValues[dataSetParam.name] == null));
};

/**
 * 获取数据集结果数据指定字段、指定行的单元格值，没有则返回null。
 * 
 * @param dataSetResult 数据集结果
 * @param field 数据集字段对象、字段名
 * @param row 行索引，可选，默认为：0
 */
chartProto.resultDataCell = function(dataSetResult, field, row)
{
	row = (row == null ? 0 : row);
	
	var re = this.resultRowArrayDatas(dataSetResult, field, row, 1);
	return (re.length > 0 ? re[0] : null);
};

/**
 * 将数据集结果数据的行对象按照指定fields顺序转换为列值数组。
 * 
 * @param dataSetResult 数据集结果
 * @param fields 数据集字段对象数组、字段名数组、字段对象、字段名
 * @param row 行索引，以0开始，可选，默认值为：0
 * @param count 获取的最多行数，可选，默认为全部
 * @returns fields为数组时：[[..., ...], ...]；fields非数组时：[..., ...]
 */
chartProto.resultColumnArrayDatas = function(dataSetResult, fields, row, count)
{
	var re = [];

	if(!dataSetResult || !fields)
		return re;
	
	var datas = this.resultDatas(dataSetResult);
	
	row = (row == null ? 0 : row);
	var endIdx = (count == null ? datas.length : (row + count));
	endIdx = (endIdx > datas.length ? datas.length : endIdx);
	
	if(CF.isArray(fields))
	{
		for(var i=0; i<fields.length; i++)
		{
			var p = fields[i];
			
			var name = (p ? (p.name || p) : undefined);
			if(!name)
				continue;
			
			var column = [];
			
			for(var j=row; j<endIdx; j++)
				column.push(datas[j][name]);
			
			re[i] = column;
		}
	}
	else
	{
		var name = (fields ? (fields.name || fields) : undefined);

		if(name)
		{
			for(var i=row; i<endIdx; i++)
			{
				var rowObj = datas[i];
				re.push(rowObj[name]);
			}
		}
	}
	
	return re;
};

/**
 * 获取数据集结果数据指定行索引的元素。
 * 
 * @param dataSetResult 数据集结果
 * @param row 行索引数值、数值数组
 * @returns 数据对象、据对象数组，当result、row为null时，将返回null
 */
chartProto.resultDataRow = function(dataSetResult, row)
{
	if(dataSetResult == null || dataSetResult.data == null || row == null)
		return null;
	
	var datas = this.resultDatas(dataSetResult);
	
	if(!CF.isArray(row))
	{
		return datas[row];
	}
	else
	{
		var re = [];
		
		for(var i=0; i<row.length; i++)
			re.push(datas[row[i]]);
		
		return re;
	}
};

/**
 * 获取数据集结果数据经字段映射后的数据对象数组。
 * 
 * @param dataSetResult 数据集结果
 * @param fieldMap 返回字段映射表，格式为：{ 返回对象字段名: 数据集字段对象、字段名、字段数组、字段名数组 }
 * @param row 可选，行索引，以0开始，默认为：0
 * @param count 可选，获取结果数据的最多行数，默认为全部
 * @returns [{"...": ..., "...": ...}, ...]
 */
chartProto.resultMapDatas = function(dataSetResult, fieldMap, row, count)
{
	var re = [];
	
	var datas = this.resultDatas(dataSetResult);
	row = (row == null ? 0 : row);
	var endIdx = (count == null ? datas.length : (row + count));
	endIdx = (endIdx > datas.length ? datas.length : endIdx);
	
	var propIsArray = {};
	for(var opn in fieldMap)
		propIsArray[opn] = CF.isArray(fieldMap[opn]);
	
	for(var i=row; i<endIdx; i++)
	{
		var di = datas[i];
		var obj = (di == null ? null : {});
		
		for(var opn in fieldMap)
		{
			var dp = fieldMap[opn];
			
			if(dp == null){}
			else if(propIsArray[opn])
			{
				obj[opn] = [];
				
				for(var j=0; j<dp.length; j++)
				{
					var dpn = (dp[j].name || dp[j]);
					obj[opn][j] = di[dpn];
				}
			}
			else
			{
				var dpn = (dp.name || dp);
				obj[opn] = di[dpn];
			}
		}
		
		re.push(obj);
	}
	
	return re;
};

/**
 * 获取数据集结果数据的名/值对象数组。
 * 
 * @param dataSetResult 数据集结果
 * @param nameField 名称数据集字段对象、字段名
 * @param valueField 值数据集字段对象、字段名、数组
 * @param row 可选，行索引，以0开始，默认为：0
 * @param count 可选，获取结果数据的最多行数，默认为全部
 * @returns [{name: ..., value: ...}, ...]
 */
chartProto.resultNameValueDatas = function(dataSetResult, nameField, valueField, row, count)
{
	var fieldMap ={ "name": nameField, "value": valueField };
	return this.resultMapDatas(dataSetResult, fieldMap, row, count);
};

/**
 * 将数据集结果数据的行对象按照指定fields顺序转换为行值数组。
 * 
 * @param dataSetResult 数据集结果
 * @param fields 数据集字段对象数组、字段名数组、字段对象、字段名
 * @param row 可选，行索引，默认为：0
 * @param count 可选，获取的最多行数，默认为全部
 * @returns fields为数组时：[[..., ...], ...]；fields非数组时：[..., ...]
 */
chartProto.resultRowArrayDatas = function(dataSetResult, fields, row, count)
{
	var re = [];
	
	if(!dataSetResult || !fields)
		return re;
	
	var datas = this.resultDatas(dataSetResult);
	
	row = (row == null ? 0 : row);
	var endIdx = (count == null ? datas.length : (row + count));
	endIdx = (endIdx > datas.length ? datas.length : endIdx);
	
	if(CF.isArray(fields))
	{
		for(var i=row; i<endIdx; i++)
		{
			var rowObj = datas[i];
			var rowVal = [];
			
			for(var j=0; j<fields.length; j++)
			{
				var p = fields[j];
				
				var name = (p ? (p.name || p) : undefined);
				if(!name)
					continue;
				
				rowVal[j] = rowObj[name];
			}
			
			re.push(rowVal);
		}
	}
	else
	{
		var name = (fields ? (fields.name || fields) : undefined);
		
		if(name)
		{
			for(var i=row; i<endIdx; i++)
			{
				var rowObj = datas[i];
				re.push(rowObj[name]);
			}
		}
	}
	
	return re;
};

/**
 * 获取数据集结果数据的行对象指定字段值。
 * 
 * @param rowObj 行对象，格式为：{ ... }
 * @param field 数据集字段对象、字段名
 */
chartProto.resultDataRowCell = function(rowObj, field)
{
	if(!rowObj || !field)
		return null;
	
	var name = (field.name || field);
	return rowObj[name];
};

/**
 * 获取数据集结果数据的值对象数组。
 * 
 * @param dataSetResult 数据集结果
 * @param valueField 值数据集字段对象、字段名、数组
 * @param row 可选，行索引，以0开始，默认为：0
 * @param count 可选，获取结果数据的最多行数，默认为全部
 * @returns [{value: ...}, ...]
 */
chartProto.resultValueDatas = function(dataSetResult, valueField, row, count)
{
	var fieldMap ={ "value": valueField };
	return this.resultMapDatas(dataSetResult, fieldMap, row, count);
};

/**
 * 获取/设置指定数据集是否忽略获取结果，忽略后下次将不会加载结果数据。
 * 如果图表渲染器附加属性没有定义{ supportIgnoreFetch: true }，对于设置操作，将在控制台警告提示。
 * 
 * @param dataSetBind 指定数据集绑定或其索引
 * @param ignoreFetch 可选，要设置的值，true 忽略；false 不忽略
 * @returns true、false
 */
chartProto.dataSetIgnoreFetch = function(dataSetBind, ignoreFetch)
{
	dataSetBind = this._dataSetBindOf(dataSetBind);
	
	if(ignoreFetch === undefined)
	{
		return this._dataSetIgnoreFetch(dataSetBind);
	}
	else
	{
		this._checkSupportIgnoreFetch();
		this._dataSetIgnoreFetch(dataSetBind, ignoreFetch);
	}
};

/**
 * 获取/设置全部数据集是否忽略获取结果，忽略后下次将不会加载结果数据。
 * 如果图表渲染器附加属性没有定义{ supportIgnoreFetch: true }，对于设置操作，将在控制台警告提示。
 * 
 * @param ignoreFetch 可选，要设置的值，true 全部忽略；false 全部不忽略；[ ... ] 指定元素值
 * @returns [ true、false, ... ]
 */
chartProto.dataSetIgnoreFetches = function(ignoreFetch)
{
	var dataSetBinds = this.dataSetBinds();
	
	if(ignoreFetch === undefined)
	{
		var re = [];
		
		for(var i=0; i<dataSetBinds.length; i++)
			re[i] = this._dataSetIgnoreFetch(dataSetBinds[i]);
		
		return re;
	}
	else
	{
		this._checkSupportIgnoreFetch();
		
		var isArray = CF.isArray(ignoreFetch);
		var len = (isArray ? Math.min(dataSetBinds.length, ignoreFetch.length) : dataSetBinds.length);
		
		for(var i=0; i<len; i++)
		{
			var myVal = (isArray ? ignoreFetch[i] : ignoreFetch);
			this._dataSetIgnoreFetch(dataSetBinds[i], myVal);
		}
	}
};

chartProto._dataSetIgnoreFetch = function(dataSetBind, ignoreFetch)
{
	var query = dataSetBind.query;
	
	if(ignoreFetch === undefined)
	{
		return (!query || query.ignoreFetch == null ? false : query.ignoreFetch);
	}
	else
	{
		query.ignoreFetch = ignoreFetch;
	}
};

chartProto._checkSupportIgnoreFetch = function()
{
	var support = this.rendererAddition(CF.RENDERER_ADDITION_SUPPORT_IGNORE_FETCH);
	
	//这里不必抛出异常，因为后端没有禁用逻辑，只警告即可
	if(support == null)
	{
		CF.logWarn("chart '#"+this.elementId()+"' renderer ["+CF.RENDERER_ADDITION_SUPPORT_IGNORE_FETCH+"] addition undefined, feature may unsupported");
	}
	else if(support == false)
	{
		CF.logWarn("chart '#"+this.elementId()+"' renderer ["+CF.RENDERER_ADDITION_SUPPORT_IGNORE_FETCH+"] feature unsupported");
	}
};

/**
 * 获取/设置数据集结果是否是忽略获取的。
 * 如果图表渲染器附加属性没有定义{ supportIgnoreFetch: true }，对于设置操作，将在控制台警告提示。
 * 
 * @param dataSetResult 数据集结果
 * @param ignoreFetch 可选，要设置的值，true 忽略；false 不忽略
 * @returns true、false
 */
chartProto.resultIgnoreFetch = function(dataSetResult, ignoreFetch)
{
	if(ignoreFetch === undefined)
	{
		return (dataSetResult && dataSetResult.ignoreFetch != null ? dataSetResult.ignoreFetch : false);
	}
	else
	{
		this._checkSupportIgnoreFetch();
		dataSetResult.ignoreFetch = ignoreFetch;
	}
};

/**
 * 获取/设置指定数据集绑定对应的数据集结果是否是忽略获取的。
 * 如果图表渲染器附加属性没有定义{ supportIgnoreFetch: true }，对于设置操作，将在控制台警告提示。
 * 
 * @param chartResult 图表结果、数据集结果数组
 * @param dataSetBind 数据集绑定、索引数值
 * @param ignoreFetch 可选，要设置的值，true 忽略；false 不忽略
 * @returns true、false
 */
chartProto.resultIgnoreFetchOf = function(chartResult, dataSetBind, ignoreFetch)
{
	var dataSetResult = this.resultOf(chartResult, dataSetBind);
	
	if(ignoreFetch === undefined)
	{
		return this.resultIgnoreFetch(dataSetResult);
	}
	else
	{
		this.resultIgnoreFetch(dataSetResult, ignoreFetch);
	}
};

/**
 * 获取图表渲染器指定附加属性值。
 * 
 * @param name 附加属性名
 * @returns 要获取的附加属性值，没有则返回null
 */
chartProto.rendererAddition = function(name)
{
	var re = null;
	
	var additions = null;
	var renderer = this.renderer();
	
	//优先取自定义渲染器中的
	if(renderer && renderer.additions)
	{
		additions = (CF.isFunction(renderer.additions) ? renderer.additions(this) : renderer.additions);
		re = (additions ? additions[name] : undefined);
		
		if(re !== undefined)
			return re;
	}
	
	renderer = this._pluginRenderer();
	
	if(renderer && renderer.additions)
	{
		additions = (CF.isFunction(renderer.additions) ? renderer.additions(this) : renderer.additions);
		re = (additions ? additions[name] : undefined);
	}
	
	return re;
};

/**
 * 获取未忽略结果的数据集绑定数组。
 * 
 * @param dataSetBinds 数据集绑定、数组
 * @param chartResult 可选，用于校验的图表结果、数据集结果数组，如果未设置，则使用this.dataSetIgnoreFetch()匹配
 * @returns [ ... ]，空数组表示没有
 */
chartProto.dataSetBindsFetched = function(dataSetBinds, chartResult)
{
	dataSetBinds = (dataSetBinds == null ? [] : (CF.isArray(dataSetBinds) ? dataSetBinds : [ dataSetBinds ]));
	
	var re = [];
	
	for(var i=0; i<dataSetBinds.length; i++)
	{
		var dsb = dataSetBinds[i];
		var ignore = (chartResult === undefined ? this.dataSetIgnoreFetch(dsb) : this.resultIgnoreFetchOf(chartResult, dsb));
		
		if(ignore)
			continue;
		
		re.push(dataSetBinds[i]);
	}
	
	return re;
};


//-------------
// < 已弃用函数 start
//-------------

//-------------
// > 已弃用函数 end
//-------------

//----------------------------------------
// Chart prototype end
//----------------------------------------

var THEME_STYLE_NAME_PROP = CF.BUILTIN_PROP_PREFIX + "StyleName";

/**
 * 获取指定主题对象对应的CSS类名。
 * 这个CSS类名是全局唯一的，可添加至HTML元素的"class"属性。
 * 
 * @param theme 主题对象，格式为：{ ... }
 * @returns CSS类名，不会为null
 */
CF.themeStyleName = function(theme)
{
	var sn = theme[THEME_STYLE_NAME_PROP];
	
	if(!sn)
		sn = (theme[THEME_STYLE_NAME_PROP] = CF.uid());
	
	return sn;
};

var THEME_STYLE_SHEET_INFO_NAME = CF.BUILTIN_PROP_PREFIX + "StyleSheetInfo";

/**
 * 判断/设置与指定主题和名称关联的CSS样式表。
 * 对于设置操作，最终生成的样式表都会添加CF.themeStyleName(theme)CSS类名选择器前缀，
 * 确保样式表只会影响添加了CF.themeStyleName(theme)样式类的HTML元素。
 * 
 * 同一主题和名称的CSS样式表，通常仅需创建一次，因此，当需要为某个HTML元素应用与主题相关的样式表时，通常使用方式如下：
 * 
 * var styleName = CF.themeStyleSheet(theme, "myName", function(){ return CSS样式表对象、数组; });
 * jQuery(element).addClass(styleName);
 * 
 * 或者
 * 
 * if(!CF.themeStyleSheet(theme, "myName"))
 *   jQuery(element).addClass(CF.themeStyleSheet(theme, "myName", CSS样式表对象、数组));
 * 
 * @param theme 主题对象，格式为：{ ... }
 * @param name 名称
 * @param css 可选，要设置的CSS，格式为：
 * 					function(){ return CSS样式表对象、[ CSS样式表对象, ... ] }
 * 					或者
 * 					CSS样式表对象
 * 					或者
 * 					[ CSS样式表对象, ... ]
 * 					其中，CSS样式表对象格式为：
 * 					{
 * 					  //CSS选择器，例如：" .success"、".success"、" .error"、[ ".success", " .error" ]
 * 					  //注意：前面加空格表示子元素、不加则表示元素本身
 * 					  name: "..."、["...", ...],
 * 					  //CSS属性对象、CSS属性字符串，例如：
 *                    //{ "color": "red", "background-color": "blue", "border-color": "red" }、
 *                    //"color:red;background-color:blue;"
 * 					  value: { CSS属性名 : CSS属性值, ... }、"..."
 * 					}
 * @param force 可选，当指定了css时，是否强制执行设置，true 强制设置；false 只有name对应的样式表不存在时才设置，默认值为：false
 * @returns 判断操作：true 已设置过；false 未设置过；设置操作：theme主题对应的CSS类名，即CF.themeStyleName(theme)的返回值
 */
CF.themeStyleSheet = function(theme, name, css, force)
{
	var infoMap = theme[THEME_STYLE_SHEET_INFO_NAME];
	if(infoMap == null)
		infoMap = (theme[THEME_STYLE_SHEET_INFO_NAME] = {});
	
	var info = infoMap[name];
	
	if(css === undefined)
		return (info != null);
	
	var styleName = CF.themeStyleName(theme);
	
	if(info && (force != true))
		return styleName;
	
	if(info == null)
		info = (infoMap[name] = { styleId: CF.uid() });
	
	var cssText = "";
	
	if(CF.isFunction(css))
		css = css();
	
	if(!CF.isArray(css))
		css = [ css ];
	
	var styleNameSelector = "." + styleName;
	
	for(var i=0; i<css.length; i++)
	{
		var cssName = css[i].name;
		var cssValue = css[i].value;
		
		if(cssName == null)
			continue;
		
		if(!CF.isArray(cssName))
			cssName = [ cssName ];
		
		for(var j=0; j<cssName.length; j++)
		{
			cssText += styleNameSelector + cssName[j];
			
			if(j < (cssName.length - 1))
				cssText += ",\n";
		}
		
		cssText += "{\n";
		cssText += CF.styleString(cssValue);
		cssText += "\n}\n";
	}
	
	CF.styleSheetText(info.styleId, cssText);
	
	return styleName;
};

var THEME_REF_ENTITY_IDS_NAME = CF.BUILTIN_PROP_PREFIX + "RefEntityIds";

/**
 * 添加主题关联实体。
 */
CF.addThemeRefEntity = function(theme, entityId)
{
	if(!theme)
		return;
	
	var entityIds = (theme[THEME_REF_ENTITY_IDS_NAME]
						|| (theme[THEME_REF_ENTITY_IDS_NAME] = {}));
	
	entityIds[entityId] = true;
};

/**
 * 移除主题关联实体，并在没有关联时销毁样式表。
 */
CF.removeThemeRefEntity = function(theme, entityId, destroyCss)
{
	destroyCss = (destroyCss == null ? true : destroyCss);
	
	if(!theme)
		return;
	
	var entityIds = (theme[THEME_REF_ENTITY_IDS_NAME] || {});
	
	if(entityIds[entityId] == true)
		entityIds[entityId] = false;
	
	if(destroyCss)
	{
		var refCount = 0;
		
		for(var p in entityIds)
		{
			if(entityIds[p] == true)
			{
				refCount++;
			}
		}
		
		//只有主题没有被使用了，才销毁样式表
		if(refCount == 0)
		{
			CF.destroyThemeStyleSheet(theme);
		}
	}
};

/**
 * 销毁主题关联创建的样式表。
 */
CF.destroyThemeStyleSheet = function(theme)
{
	if(!theme)
		return;
	
	var infoMap = theme[THEME_STYLE_SHEET_INFO_NAME];
	
	if(infoMap != null)
	{
		theme[THEME_STYLE_SHEET_INFO_NAME] = null;
		
		for(var name in infoMap)
		{
			var info = infoMap[name];
			var styleId = (info ? info.styleId : null);
			
			if(styleId)
				CF.eleRemove(CF.eleOfId(styleId));
		}
	}
};

/**
 * 获取指定ID的元素
 * 
 * @param id HTML元素ID
 */
CF.eleOfId = function(id)
{
	return document.getElementById(id);
};

/**
 * 获取匹配选择器的全部后代元素
 * 
 * @param selector CSS选择器
 * @param rootEle 可选，查找根元素（不包含），默认为：document
 * @returns HTML元素数组
 */
CF.elesOfSelector = function(selector, rootEle)
{
	rootEle = (rootEle == null ? document : rootEle);
	
	var re = [];
	
	var nodeList = rootEle.querySelectorAll(selector);
	if(nodeList)
	{
		for(var i=0; i<nodeList.length; i++)
			re.push(nodeList[i]);
	}
	
	return re;
};

/**
 * 删除元素
 * 
 * @param ele HTML元素
 */
CF.eleRemove = function(ele)
{
	if(!ele)
		return;
	
	ele.remove();
};

/**
 * 在HTML元素内部末尾插入子元素
 * 
 * @param ele HTML元素
 * @param child HTML元素、HTML字符串
 */
CF.eleAppend = function(ele, child)
{
	if(CF.isString(child))
	{
		ele.insertAdjacentHTML("beforeend", child);
	}
	else
	{
		ele.appendChild(child);
	}
};

/**
 * 在HTML元素内部开头插入子元素
 * 
 * @param ele HTML元素
 * @param child HTML元素、HTML字符串
 */
CF.elePrepend = function(ele, child)
{
	if(CF.isString(child))
	{
		ele.insertAdjacentHTML("afterbegin", child);
	}
	else
	{
		ele.insertBefore(child, ele.firstChild);
	}
};

/**
 * 在HTML元素之前插入元素
 * 
 * @param ele HTML元素
 * @param sibling HTML元素、HTML字符串
 */
CF.eleBefore = function(ele, sibling)
{
	if(CF.isString(child))
	{
		ele.insertAdjacentHTML("beforebegin", sibling);
	}
	else
	{
		ele.before(sibling);
	}
};

/**
 * 在HTML元素之之后插入元素
 * 
 * @param ele HTML元素
 * @param sibling HTML元素、HTML字符串
 */
CF.eleAfter = function(ele, sibling)
{
	if(CF.isString(child))
	{
		ele.insertAdjacentHTML("afterend", sibling);
	}
	else
	{
		ele.after(sibling);
	}
};

/**
 * 获取/设置元素属性。
 * 
 * @param ele HTML元素
 * @param name 属性名
 * @param value 可选，要设置的值，为null时将删除属性
 */
CF.eleAttr = function(ele, name, value)
{
	if(value === undefined)
	{
		return ele.getAttribute(name);
	}
	else
	{
		if(value == null)
			ele.removeAttribute(name);
		else
			ele.setAttribute(name, value);
	}
};

/**
 * 添加元素样式类。
 * 
 * @param ele HTML元素
 * @param classes 样式类数组、以空格分隔的字符串
 */
CF.eleAddClass = function(ele, classes)
{
	classes = (CF.isArray(classes) ? classes : CF.splitByWhitespace(classes));
	
	var classList = ele.classList;
	for(var i=0; i<classes.length; i++)
	{
		classList.add(classes[i]);
	}
};

/**
 * 删除元素样式类。
 * 
 * @param ele HTML元素
 * @param classes 样式类数组、以空格分隔的字符串
 */
CF.eleRemoveClass = function(ele, classes)
{
	classes = (CF.isArray(classes) ? classes : CF.splitByWhitespace(classes));
	
	var classList = ele.classList;
	for(var i=0; i<classes.length; i++)
	{
		classList.remove(classes[i]);
	}
};

/**
 * 获取/设置元素的CSS样式。
 * 
 * @param ele HTML元素
 * @param name CSS样式名
 * @param value 可选，要设置的CSS样式值
 */
CF.eleCss = function(ele, name, value)
{
	if(value === undefined)
	{
		return window.getComputedStyle(ele, null).getPropertyValue(name);
	}
	else
	{
		ele.style[name] = value;
	}
};

CF.ELE_DATA_CACHE = new WeakMap();

/**
 * 获取/绑定元素数据，值会在删除元素后自动删除。
 * 
 * @param ele HTML元素
 * @param name 名称
 * @param value 可选，要设置的值
 */
CF.eleData = function(ele, name, value)
{
	var map = CF.ELE_DATA_CACHE.get(ele);
	
	if(value === undefined)
	{
		return (map == null ? undefined : map[name]);
	}
	else
	{
		if(map == null)
		{
			map = {};
			CF.ELE_DATA_CACHE.set(ele, map);
		}
		
		map[name] = value;
	}
};

/**
 * 删除元素数据。
 * 
 * @param ele HTML元素
 * @param name 可选，名称，不设置则删除全部
 */
CF.eleRemoveData = function(ele, name)
{
	if(name === undefined)
	{
		CF.ELE_DATA_CACHE.delete(ele);
	}
	else
	{
		var map = CF.ELE_DATA_CACHE.get(ele);
		
		if(map == null)
			return;
		
		delete map[name];
	}
};

/**
 * 清空元素内容。
 * 
 * @param ele HTML元素
 */
CF.eleEmpty = function(ele)
{
	if(!ele)
		return;
	
	while(ele.firstChild)
		ele.removeChild(ele.firstChild);
};

/**
 * 获取/设置HTML元素的CSS样式字符串（元素的style属性）。
 * 
 * 使用方式：
 * CF.eleStyle(element)
 * CF.eleStyle(element, "color:red;font-size:1.5em")
 * CF.eleStyle(element, {border:"1px solid red"}, "color:red;font-size:1.5em")
 * CF.eleStyle(element, "color:red;font-size:1.5em", {border:"1px solid red"}, "background:blue")
 * CF.eleStyle(element, ["color:red;font-size:1.5em", {border:"1px solid red"}], "background:blue")
 * 
 * @param ele HTML元素
 * @param css 可选，要设置的CSS样式，格式为：同CF.styleString()函数参数
 * @return 要获取的CSS样式字符串
 */
CF.eleStyle = function(ele, css)
{
	if(css === undefined)
		return CF.eleAttr(ele, "style");
	
	var cssArray = [];
	
	for(var i=1; i<arguments.length; i++)
		cssArray.push(arguments[i]);
	
	var cssText = CF.styleString.apply(CF, cssArray);
	CF.eleAttr(ele, "style", cssText);
};

/**
 * 拼接CSS样式字符串。
 * 
 * 使用方式：
 * CF.styleString({color:"red", border:"1px solid red"})
 * CF.styleString({border:"1px solid red", padding:"1em 1em"}, "color:red;font-size:1.5em")
 * CF.styleString("color:red;font-size:1.5em", {border:"1px solid red", padding:"1em 1em"}, "background:blue")
 * CF.styleString(["color:red;font-size:1.5em", {border:"1px solid red", padding:"1em 1em"}], "background:blue")
 * 
 * @param css 要拼接的CSS样式，格式为：
 *            字符串，例如："color:red;font-size:1.5em"
 *            CSS属性对象，例如：{ color: "...", "backgroundColor": "...", "font-size": "...", ...  }，
 *            数组，元素可以是字符串、CSS属性对象
 *            或者是包含字符串、CSS属性对象、数组的变长参数
 * @return 拼接后的CSS样式字符串，例如："color:red;background-color:red;font-size:1px;"
 */
CF.styleString = function(css)
{
	var cssText = "";
	
	for(var i=0; i<arguments.length; i++)
	{
		var cssi = arguments[i];
		
		if(CF.isNullOrEmpty(cssi))
			continue;
		
		var cssiText = "";
		
		if(CF.isString(cssi))
		{
			cssiText = cssi;
		}
		else if(CF.isArray(cssi))
		{
			cssiText = CF.styleString.apply(CF, cssi);
		}
		else
		{
			for(var name in cssi)
			{
				var value = cssi[name];
				
				if(!CF.isNullOrEmpty(value))
				{
					cssiText += name + ":" + value + ";";
				}
			}
		}
		
		if(CF.isNullOrEmpty(cssiText))
			continue;
		
		if(cssText && cssText.charAt(cssText.length - 1) != ";")
			cssText += ";";
		
		cssText += cssiText;
	}
	
	return cssText;
};

/**
 * 将css字符串转换为对象。
 */
CF.styleStringToObj = function(styleStr)
{
	var re = {};
	
	if(styleStr)
	{
		var strs = styleStr.split(";");
		for(var i=0; i<strs.length; i++)
		{
			var str = CF.trim(strs[i]);
			if(str)
			{
				var nv = str.split(":");
				var n = CF.trim(nv[0] || "");
				var v = CF.trim(nv[1] || "");
				
				if(n)
					re[n] = v;
			}
		}
	}
	
	return re;
};

/**
 * 获取内置属性名（添加内置前缀）。
 * 内置属性名以'_'开头。
 */
CF.builtinPropName = function(name)
{
	return CF.BUILTIN_PROP_PREFIX + name;
};

/**
 * 获取/设置渲染上下文的属性值。
 * 
 * @param renderContext
 * @param attrName
 * @param attrValue 要设置的属性值，可选，不设置则执行获取操作
 */
CF.renderContextAttr = function(renderContext, attrName, attrValue)
{
	if(attrValue === undefined)
		return renderContext.attributes[attrName];
	else
		return renderContext.attributes[attrName] = attrValue;
};

/**
 * 获取/设置渲染上下文中的WebContext对象。
 * 
 * @param renderContext
 * @param webContext 可选，要设置的WebContext
 */
CF.renderContextAttrWebContext = function(renderContext, webContext)
{
	return CF.renderContextAttr(renderContext, renderContextAttrConst.webContext, webContext);
};

/**
 * 获取/设置渲染上下文中的ChartTheme对象。
 * 
 * @param renderContext
 * @param chartTheme 可选，要设置的ChartTheme
 */
CF.renderContextAttrChartTheme = function(renderContext, chartTheme)
{
	return CF.renderContextAttr(renderContext, renderContextAttrConst.inflatedChartTheme, chartTheme);
};

/**
 * 为指定URL添加系统根路径前缀。
 * 只有当URL以"/"开头时才会添加系统根路径前缀，否则，将直接返回原URL。
 * 当需要访问系统内其他功能模块的资源时，应为其URL添加系统根路径前缀。
 * 
 * @param webContext web上下文
 * @param url 可选，要处理的URL
 * @return 添加后的新URL，如果未设置url参数，将返回系统根路径
 */
CF.toWebContextPathURL = function(webContext, url)
{
	var contextPath = webContext.contextPath;
	
	// (webContext)
	if(url === undefined)
	{
		return contextPath;
	}
	// (webContext, url)
	else
	{
		if(url != null && url !== "" && url.charAt(0) == "/")
		{
			url = contextPath + url;
		}
		
		return url;
	}
};

/**
 * 获取/设置HTML元素上的图表部件ID（"dg-chart-widget"属性值）。
 * 
 * @param ele HTML元素
 * @param widgetId 选填参数，要设置的图表部件ID，不设置则执行获取操作
 */
CF.elementWidgetId = function(ele, widgetId)
{
	if(widgetId === undefined)
	{
		return CF.eleAttr(ele, CF.elementAttrConst.WIDGET);
	}
	else
	{
		CF.eleAttr(ele, CF.elementAttrConst.WIDGET, widgetId);
	}
};

/**
 * 获取HTML元素自身或其子孙元素中带有非空图表部件ID属性（"dg-chart-widget"）的全部元素。
 * 
 * @param ele HTML元素
 * @returns HTML元素数组
 */
CF.elesWithWidgetId = function(ele)
{
	var re = [];
	
	if(ele == null)
		return re;
	
	if(!CF.isNullOrEmpty(CF.elementWidgetId(ele)))
		re.push(ele);
	
	var children = CF.elesOfSelector("["+CF.elementAttrConst.WIDGET+"]", ele);
	children.forEach(function(child)
	{
		if(!CF.isNullOrEmpty(CF.elementWidgetId(child)))
		{
			re.push(child);
		}
	});
	
	return re;
};

/**
 * 获取当前在指定HTML元素上渲染的图表对象。
 * 
 * @param ele HTML元素
 * @returns 图表对象，null表示元素上并未渲染图表
 */
CF.renderedChart = function(ele)
{
	return CF.eleData(ele, CF.ELE_RENDERED_CHART_NAME);
};

/**
 * 校验设置图表元素ID。
 * 图表元素必须有ID，且要与图表中的元素ID同步。
 * 
 * @param ele HTML元素
 * @param chart 可选，要同步的图表
 */
CF.checkSetChartElementId = function(ele, chart)
{
	var elementId = CF.eleAttr(ele, "id");
	
	if(CF.isNullOrEmpty(elementId))
	{
		elementId = CF.uid();
		CF.eleAttr(ele, "id", elementId);
	}
	
	if(chart)
		chart.elementId(elementId);
	
	return elementId;
};

/**
 * 给URL追加参数。
 * 
 * @param url
 * @param name 参数名
 * @param value 参数值
 */
CF.appendUrlParam = function(url, name, value)
{
	name = encodeURIComponent(name);
	value = encodeURIComponent(value);
	
	var anchor = "";
	var aidx = url.indexOf('#');
	if(aidx >= 0)
	{
		var tmpUrl = url.substring(0, aidx);
		anchor = url.substring(aidx);
		url = tmpUrl;
	}
	
	var qidx = url.indexOf('?');
	url += (qidx < 0 ? "?" : "&") + name + "=" + value;
	
	return url + anchor;
};

/**
 * 执行JS代码。
 * 
 * @param str JS代码
 * @param defaultValue 默认返回值，可选，默认为：undefined
 */
CF.evalSilently = function(str, defaultValue)
{
	var re = undefined;
	
	try
	{
		re = Function("return ("+str+");")();
	}
	catch(e)
	{
		CF.logException(e);
	}
	
	return (re || defaultValue);
};

/**
 * 静默执行函数。
 * 
 * @param func 函数
 * @param exceptionHandler 可选，异常处理函数
 */
CF.executeSilently = function(func, exceptionHandler)
{
	try
	{
		return func();
	}
	catch(e)
	{
		if(exceptionHandler)
		{
			return exceptionHandler(e);
		}
		else
		{
			CF.logException(e);
		}
	}
};

/**
 * 将指定名称转换为合法的CSS样式属性名
 * 例如："backgroundColor" 将被转换为 "background-color"
 */
CF.toLegalStyleName = function(name)
{
	var re = "";
	
	for(var i=0; i<name.length; i++)
	{
		var c = name.charAt(i);
		
		if(c >= 'A' && c <= 'Z')
		{
			re += "-";
			re += c.toLowerCase();
		}
		else
			re += c;
	}
	
	return re;
};

var THEME_GRADUAL_COLORS_NAME = CF.BUILTIN_PROP_PREFIX + "GradualColors";

/**
 * 获取主题从背景色（actualBackgroundColor）到前景色（color）之间的渐变因子对应的颜色。
 * 这个颜色是实际背景色（actualBackgroundColor）与前景色（color）之间的某个颜色。
 * 
 * @param theme 主题对象，格式为：{ color: "...", actualBackgroundColor: "..." }
 * @param factor 可选，渐变因子，0-1之间的小数，其中0表示最接近实际背景色的颜色、1表示最接近前景色的颜色
 * @returns 与factor匹配的颜色字符串，格式类似："#FFFFFF"，如果未设置factor，将返回一个包含所有渐变颜色的数组
 */
CF.themeGradualColor = function(theme, factor)
{
	var gcs = theme[THEME_GRADUAL_COLORS_NAME];
	
	if(!gcs || gcs.length == 0)
	{
		gcs = this.evalGradualColors(theme.actualBackgroundColor, theme.color, (theme.gradient || 20));
		theme[THEME_GRADUAL_COLORS_NAME] = gcs;
	}
	
	if(factor == null)
		return gcs;
	else
	{
		var index = parseInt((gcs.length-1) * factor);
		
		index = (index < 0 ? 0 : index);
		index = (index >= gcs.length ? gcs.length - 1 : index);
		
		if(index == 0 && factor > 0)
			index = 1;
		
		if(index == gcs.length - 1 && factor < 1)
			index == gcs.length - 2;
		
		return gcs[index];
	}
};

/**
 * 计算起始颜色和终止颜色之间的渐变颜色数组，数组中不包含起始颜色、也不包含结束颜色。
 * 
 * @param start 起始颜色
 * @param end 终止颜色
 * @param count 要计算的渐变颜色数目
 * @param rgb true 返回"rgb(...)"格式；fasle 返回"#FFFFFF"格式，默认为false
 * @returns 渐变颜色数组
 */
CF.evalGradualColors = function(start, end, count, rgb)
{
	var colors = [];
	
	start = this.parseColor(start);
	end = this.parseColor(end);
	
	count = count + 1;
	
	for(var i=1; i<count; i++)
	{
		var color = {};
		
		color.r = parseInt(start.r + (end.r - start.r)/count*i);
		color.g = parseInt(start.g + (end.g - start.g)/count*i);
		color.b = parseInt(start.b + (end.b - start.b)/count*i);
		
		if(rgb)
			color = "rgb("+color.r+","+color.g+","+color.b+")";
		else
			color = CF.colorToHexStr(color, true);
		
		colors.push(color);
	}
	
	return colors;
};

/**
 * 将颜色转换为6位HEX字符串。
 * 
 * @param color 颜色字符串，格式为："#FFF"、"#FFFFFF"、"#FFFFFF80"、"rgb(255,255,255)"、"rgba(255,255,255, 0.5)"
 * @param prefix 可选，是否添加"#"前缀
 * @returns 6位HEX字符串，格式示例："FFFFFF"
 */
CF.colorToHexStr = function(color, prefix)
{
	if(color == null)
		return "";
	
	if(CF.isString(color))
	{
		color = CF.parseColor(color);
	}
	prefix = (prefix == null ? false : prefix);
	
	var r = new Number(color.r).toString(16);
	var g = new Number(color.g).toString(16);
	var b = new Number(color.b).toString(16);
	var a = (color.a != null ? new Number(parseInt(color.a*255)).toString(16) : undefined);
	
	color = (prefix ? "#" : "") + (r.length == 1 ? "0"+r : r)
				 + (g.length == 1 ? "0"+g : g)
				 + (b.length == 1 ? "0"+b : b)
				 + (a != null ? (a.length == 1 ? "0"+a : a) : "");
	
	return color;
};

//RGB颜色字符串前缀正则
var RGB_COLOR_PREFIX_REGEX = /^\s*rgb/i;

//HSL颜色字符串前缀正则
var HSL_COLOR_PREFIX_REGEX = /^\s*hsl/i;

//HEX颜色字符串前缀正则
var HEX_COLOR_PREFIX_REGEX = /^\s*\#/i;

//名称颜色字符串前缀正则
var NAME_COLOR_PREFIX_REGEX = /^\s*\w*\s*$/i;

CF._COMPUTED_NAME_COLORS = {};

var ELE_ID_FOR_PARSE_COLOR = CF.BUILTIN_NAME_PART + "EleForParseColor";

/**
 * 解析颜色对象。
 * 将颜色字符串解析为{r: number, g: number, b: number, a: number}格式的对象。
 * 
 * @param color 颜色字符串，格式为："red"、#FFF"、"#FFFFFF"、"#FFFFFF80"、"rgb(...)"、"rgba(...)"、"hsl(...)"、"hsla(...)"
 */
CF.parseColor = function(color)
{
	//默认a值应为undefined
	var re = {r: 0, g: 0, b: 0, a: undefined};
	
	if(!color)
		return re;
	
	//颜色名称（red、green、yellow等），通过元素css函数转换
	if(NAME_COLOR_PREFIX_REGEX.test(color))
	{
		var computedColor = CF._COMPUTED_NAME_COLORS[color];
		
		if(computedColor != null)
		{
			color = computedColor;
		}
		else
		{
			var ele = CF.eleOfId(ELE_ID_FOR_PARSE_COLOR);
			if(ele == null)
			{
				CF.eleAppend(document.body,
					"<div id='"+ELE_ID_FOR_PARSE_COLOR+"' style='display:none;position:absolute;left:0;bottom:0;width:0;height:0;z-index:-999;'></div>");
				ele = CF.eleOfId(ELE_ID_FOR_PARSE_COLOR);
			}
			
			CF.eleCss(ele, "color", color);
			computedColor = CF.eleCss(ele, "color");
		}
	}
	
	// #FFF、#FFFFFF、#FFFFFFFF
	if(HEX_COLOR_PREFIX_REGEX.test(color))
	{
		color = color.substring(1);
		
		if(color.length == 3)
			color = color + color;
		
		if(color.length >= 2)
			re.r = parseInt(color.substr(0, 2), 16);
		if(color.length >= 4)
			re.g = parseInt(color.substr(2, 2), 16);
		if(color.length >= 6)
			re.b = parseInt(color.substr(4, 2), 16);
		if(color.length >= 8)
			re.a = parseInt(color.substr(6, 2), 16)/255;
	}
	else
	{
		// rgb()、rgba()
		var isRgb = RGB_COLOR_PREFIX_REGEX.test(color);
		// hsl()、hsla()
		var isHsl = HSL_COLOR_PREFIX_REGEX.test(color);
		
		if(isRgb || isHsl)
		{
			var si = color.indexOf("(");
			var ei = (si >= 0 ? color.lastIndexOf(")") : -1);
			
			if(ei > si)
			{
				color = CF.trim(color.substring(si+1, ei));
				
				//以逗号分隔
				if(color.indexOf(",") >= 0)
				{
					color = color.split(",");
				}
				//以空格分隔
				else if(color.indexOf(" ") >= 0)
				{
					color = color.split(" ");
					
					//rbg(r g b / a)
					if(color.length >= 4 && color[3] == "/")
					{
						color[3] = color[4];
						color[4] = null;
					}
				}
				else
					color = [];
			}
			else
				color = [];
			
			if(isRgb)
			{
				if(color.length >= 1)
					re.r = parseInt(color[0]);
				if(color.length >= 2)
					re.g = parseInt(color[1]);
				if(color.length >= 3)
					re.b = parseInt(color[2]);
				if(color[3] != null)
					re.a = parseFloat(color[3]);
			}
			else if(isHsl)
			{
				CF._hslArrayToRgb(color, re);
			}
		}
	}
	
	return re;
};

CF._hslArrayToRgb = function(hsl, rgbObj)
{
	if(hsl.length < 3)
		return;
	
	var h = parseFloat(color[0]);
	var s = parseFloat(color[1]);
	var l = parseFloat(color[2]);
	var a = 1.0;
	
	if(color[3] != null)
	{
		if (color[3].endsWith('%'))
			a = parseFloat(color[3]) / 100.0;
		else
			a = parseFloat(color[3]);
	}
	
	//TODO
};

/**
 * 设置指定ID的样式表css文本。
 * 如果样式表不存在，将会自动创建，并插入至<head>中。
 * 插入规则：
 * 一级优先：插入在最后一个生成样式表之后，确保新生成样式表可以覆盖全部旧生成样式表；
 * 二级优先：插入在最后一个看板引入库（dg-lib-name）之后，确保全部生成样式表可以覆盖全部引入库中的样式表；
 * 三级优先：插入在第一个用户引入<link>元素之前，确保看板内用户引入的<link>样式表可以覆盖全部生成样式表；
 * 四级优先：插入在第一个用户定义<style>元素之前，确保看板内用户定义的<style>样式表可以覆盖全部生成样式表；
 * 五级优先：插入在<head>末尾。
 * 
 * @param styleId 样式表元素ID
 * @param cssText css文本内容
 */
CF.styleSheetText = function(styleId, cssText)
{
	var $style = jQuery("#" + styleId);
	
	if($style.length > 0)
	{
		$style.text(cssText);
		return;
	}
	
	$style = jQuery("<style />").attr("id", styleId)
		.attr("dg-generated-style", "true").attr("type", "text/css").text(cssText);
	
	var $head = jQuery("head:first");
	
	var $lastGenStyle = jQuery("style[dg-generated-style]:last", $head);
	if($lastGenStyle.length > 0)
	{
		$lastGenStyle.after($style);
		return;
	}
	
	var $lastImport = jQuery("["+CF.LIB_ATTR_NAME+"]:last", $head);
	
	if($lastImport.length > 0)
	{
		$lastImport.after($style);
		return;
	}
	
	var $firstLink = jQuery("link:first", $head);
	
	if($firstLink.length > 0)
	{
		$firstLink.before($style);
		return;
	}
	
	var $firstStyle = jQuery("style:first", $head);
	
	if($firstStyle.length > 0)
	{
		$firstStyle.before($style);
		return;
	}
	
	$head.append($style);
};

/**
 * 判断给定CSS样式表是否已创建。
 * 
 * @param id 样式表元素ID
 */
CF.isStyleSheetCreated = function(id)
{
	var style = document.getElementById(id);
	
	return (style != null && style.type == "text/css");
};

/**
 * 生成一个新的页面元素ID。
 * 这个ID仅包含[a-z]、[A-Z]、[0-9]，且以字母开头。
 */
CF.uid = function()
{
	if(this._uid_seq >= Number.MAX_SAFE_INTEGER)
	{
		this._uid_seq = null;
		this._uid_time = null;
	}
	
	var seq = (this._uid_seq == null ? (this._uid_seq = 0) : this._uid_seq);
	var time = (this._uid_time == null ? (this._uid_time = CF.currentDateMs().toString(16)) : this._uid_time);
	this._uid_seq++;
	
	return "dgid" + time + seq;
};

/**
 * 获取当前日期毫秒数。
 */
CF.currentDateMs = function()
{
	return new Date().getTime();
};

var DERIVED_ELEMENTS_NAME = CF.BUILTIN_PROP_PREFIX + "derivedElements";

/**
 * 获取/设置父元素的派生子元素DOM数组，派生子元素并不是父元素的直接子孙元素，但是从属于父元素生命周期，随父元素创建，也应随父元素删除。
 *
 * @param parent 父DOM元素、JQ对象
 * @param derived 可选，要设置的派生子元素DOM、DOM数组、JQ对象、null
 * @param append 可选，当执行设置操作时，是否追加而非覆盖，默认为：true
 */
CF.derivedElements = function(parent, derived, append)
{
	parent = jQuery(parent);
	
	if(derived === undefined)
		return parent.data(DERIVED_ELEMENTS_NAME);
	
	append = (append == null ? true : append);
	
	if(derived == null)
		parent.removeData(DERIVED_ELEMENTS_NAME);
	else
	{
		derived = jQuery(derived);
		
		var des = parent.data(DERIVED_ELEMENTS_NAME);
		if(des == null || !append)
		{
			des = [];
			parent.data(DERIVED_ELEMENTS_NAME, des);
		}
		
		derived.each(function()
		{
			des.push(this);
		});
	}
};

/**
 * 删除元素，同时删除通过CF.derivedElements()设置的派生子元素。
 * 
 * @param ele 要删除的DOM元素、DOM元素数组、JQ对象
 */
CF.removeElementWithDerived = function(ele)
{
	ele = jQuery(ele);
	
	ele.each(function()
	{
		var des = (CF.derivedElements(this) || []);
		
		for(var i=0; i<des.length; i++)
			CF.removeElementWithDerived(des[i]);
		
		jQuery(this).remove();
	});
};

/**
 * 将给定值按照HTML规范转义，如果不是字符串，直接返回原值。
 */
CF.escapeHtml = function(value)
{
	if(typeof(value) != "string")
		return value;
	
	var epn = "";
	
	for(var i=0; i<value.length; i++)
	{
		var c = value.charAt(i);
		
		if(c == '<')
			epn += '&lt;';
		else if(c == '>')
			epn += '&gt;';
		else if(c == '&')
			epn += '&amp;';
		else if(c == '"')
			epn += '&quot;';
		else if(c == '\'')
			epn += '&#39;';
		else
			epn += c;
	}
	
	return epn;
};

CF.toCssFontSize = function(fontSize)
{
	if(CF.isNullOrEmpty(fontSize))
	{
		//返回一个无效的css字号值，使其不影响其他层级字号设置
		return "null";
	}
	else if(jQuery.isNumeric(fontSize))
	{
		return fontSize + "px";
	}
	else
	{
		return fontSize;
	}
};

/**
 * 记录异常日志。
 * 
 * @param exception 异常对象、异常消息字符串
 */
CF.logException = function(exception)
{
	if(typeof(console) != "undefined")
	{
		if(console.error)
			console.error(exception);
		else
			CF.logWarn(exception);
	}
};

/**
 * 记录警告日志。
 * 
 * @param msg 警告消息字符串
 */
CF.logWarn = function(msg)
{
	if(typeof(console) != "undefined")
	{
		if(console.warn)
			console.warn(msg);
		else if(console.info)
			console.info(msg);
		else if(console.log)
			console.log(msg);
	}
};

CF.isBoolean = function(v)
{
	return (typeof(v) === "boolean");
};

CF.isString = function(v)
{
	return (typeof(v) === "string");
};

CF.isNumber = function(v)
{
	return (typeof(v) === "number");
};

CF.isArray = function(v)
{
	return Array.isArray(v);
};

CF.isFunction = function(v)
{
	return (typeof(v) === "function");
};

CF.isStringOrNumber = function(v)
{
	var type = typeof(v);
	return (type === "string" || type === "number");
};

//是否为null、undefined、空字符串、空数组
CF.isNullOrEmpty = function(v)
{
	return (v == null || v === "" || (v.length !== undefined && v.length === 0));
};

//删除字符串两端空格。
CF.trim = function(str)
{
	return (str == null ? "" : str.trim());
};

CF.toJsonString = function(obj)
{
	return JSON.stringify(obj);
};

CF.isJsonString = function(str)
{
	//以'{'或'['开头
	return (CF.isString(str) && /^\s*[\{\[]/.test(str));
};

//按空白拆分字符串的正则表达式（参考自3.7.1版本的jQuery的rnothtmlwhite变量）
CF.SPLIT_WHITESPACE_REGEX = ( /[^\x20\t\r\n\f]+/g );

/**
 * 将字符串按照空白符分隔为数组。
 */
CF.splitByWhitespace = function(str)
{
	if(str == nul)
		return [];
	else
		return (str.match(CF.SPLIT_WHITESPACE_REGEX) || []);
};

//是否是DOM元素或Jquery对象
CF.isDomOrJquery = function(obj)
{
	return (obj && ((obj.nodeType != null && obj.nodeName != null) || (obj instanceof jQuery)));
};

/* 移植jQuery函数需要使用的变量 */
var arr = [];
var indexOf = arr.indexOf;
var getProto = Object.getPrototypeOf;
var class2type = {};
var toString = class2type.toString;
var hasOwn = class2type.hasOwnProperty;
var fnToString = hasOwn.toString;
var ObjectFunctionString = fnToString.call(Object);
var toType = function( obj ) {
	if ( obj == null ) {
		return obj + "";
	}

	return typeof obj === "object" || typeof obj === "function" ?
		class2type[ toString.call( obj ) ] || "object" :
		typeof obj;
};
var isWindow = function isWindow( obj ) {
	return obj != null && obj === obj.window;
};
var isArrayLike =function(obj) {
	var length = !!obj && "length" in obj && obj.length,
		type = toType( obj );

	if ( CF.isFunction( obj ) || isWindow( obj ) ) {
		return false;
	}

	return type === "array" || length === 0 ||
		typeof length === "number" && length > 0 && ( length - 1 ) in obj;
};

/**
 * 获取元素在数组中的索引（修改自3.7.1版本的jQuery.inArray函数）
 * 
 * @param ele 要查找的元素
 * @param array 数组
 * @param index 可选，查找位置
 */
CF.inArray = function(ele, array, index)
{
	return (array == null ? -1 : indexOf.call(array, ele, index));
};

/**
 * 是否是纯JS对象（修改自3.7.1版本的jQuery.isPlainObject函数）。
 */
CF.isPlainObject = function(obj)
{
	var proto, Ctor;

	if ( !obj || toString.call( obj ) !== "[object Object]" ) {
		return false;
	}

	proto = getProto( obj );

	if ( !proto ) {
		return true;
	}

	Ctor = hasOwn.call( proto, "constructor" ) && proto.constructor;
	return typeof Ctor === "function" && fnToString.call( Ctor ) === ObjectFunctionString;
};

/**
 * 是否是空对象（修改自3.7.1版本的jQuery.isEmptyObject函数）。
 */
CF.isEmptyObject = function(obj)
{
	var name;

	for (name in obj){
		return false;
	}
	
	return true;
};

/**
 * 遍历数组/对象（修改自3.7.1版本的jQuery.each函数）。
 */
CF.each = function(obj, callback)
{
	var length, i = 0;

	if ( isArrayLike( obj ) ) {
		length = obj.length;
		for ( ; i < length; i++ ) {
			if ( callback.call( obj[ i ], i, obj[ i ] ) === false ) {
				break;
			}
		}
	} else {
		for ( i in obj ) {
			if ( callback.call( obj[ i ], i, obj[ i ] ) === false ) {
				break;
			}
		}
	}
	
	return obj;
};

/**
 * 合并对象并返回（修改自3.7.1版本的jQuery.extend函数）。
 * 调用方式：
 * 浅合并：CF.extend(target, src1, src2, ...)
 * 深合并：CF.extend(true, target, src1, src2, ...)
 */
CF.extend = function()
{
	var options, name, src, copy, copyIsArray, clone,
		target = arguments[ 0 ] || {},
		i = 1,
		length = arguments.length,
		deep = false;

	if ( typeof target === "boolean" ) {
		deep = target;
		target = arguments[ i ] || {};
		i++;
	}

	if ( typeof target !== "object" && !CF.isFunction( target ) ) {
		target = {};
	}

	for ( ; i < length; i++ ) {

		if ( ( options = arguments[ i ] ) != null ) {

			for ( name in options ) {
				copy = options[ name ];

				if ( name === "__proto__" || target === copy ) {
					continue;
				}

				if ( deep && copy && ( jQuery.isPlainObject( copy ) ||
					( copyIsArray = CF.isArray( copy ) ) ) ) {
					src = target[ name ];

					if ( copyIsArray && !CF.isArray( src ) ) {
						clone = [];
					} else if ( !copyIsArray && !jQuery.isPlainObject( src ) ) {
						clone = {};
					} else {
						clone = src;
					}
					copyIsArray = false;

					target[ name ] = CF.extend( deep, clone, copy );

				} else if ( copy !== undefined ) {
					target[ name ] = copy;
				}
			}
		}
	}

	return target;
};

/**
 * 比较版本号。
 * 支持版本号格式示例：
 * 1、1-alpha、1.1、1.1-alpha、1.1.1、1.1.1-alpha、1.1.1.1、1.1.1.1-alpha
 * 
 * 此函数原封不动地拷贝自util.js中的jQuery.compareVersion函数
 * 
 * @param v1
 * @param v2
 * @returns -1 v1低于v2；0 v1等于v2；1 v1高于v2
 */
CF.compareVersion = function(v1, v2)
{
	if(v1 === v2)
		return 0;
	
	var b1 = "";
	var b2 = "";
	
	var bIdx1 = v1.indexOf("-");
	if(bIdx1 > 0)
	{
		b1 = (bIdx1 >= v1.length - 1 ? "" : v1.substring(bIdx1 + 1));
		v1 = v1.substring(0, bIdx1);
	}
	
	var bIdx2 = v2.indexOf("-");
	if(bIdx2 > 0)
	{
		b2 = (bIdx2 >= v2.length - 1 ? "" : v2.substring(bIdx2 + 1));
		v2 = v2.substring(0, bIdx2);
	}
	
	var v1ds = v1.split(".");
	var v2ds = v2.split(".");
	
	for(var i= 0, len = Math.max(v1ds.length, v2ds.length); i<len; i++)
	{
		var num1 = (v1ds[i] == null ? 0 : parseInt(v1ds[i]));
		var num2 = (v2ds[i] == null ? 0 : parseInt(v2ds[i]));
		
		if(num1 > num2)
		{
			return 1;
		}
		else if(num1 < num2)
		{
			return -1;
		}
	}
	
	if(b1 > b2)
		return 1;
	else if(b1 < b2)
		return -1;
	else
		return 0;
};

/**
 * 获取/设置内置图表选项名的选项值。
 * 内置选项名是公用的，可能会出现未知的命名冲突问题，使用此函数获取/设置可以避免此问题，
 * 因为此函数支持在图表选项中定义"customOptionNames"选项自定义选项名。
 * 
 * @param options 获取时可为null，图表选项对象，格式为：{ ... }
 * @param name 内置选项名
 * @param value 要设置的选项值
 * @returns 要获取的选项值
 */
CF.builtinOptionValue = function(options, name, value)
{
	var customNames = (options == null ? null : options[builtinOptionNames.customOptionNames]);
	name = (customNames && customNames[name] ? customNames[name] : name);
	
	if(value === undefined)
	{
		return (options ? options[name] : null);
	}
	else
	{
		options[name] = value;
	}
};

/**
 * 获取/设置选项值
 */
CF.optionValue = function(options, name, value)
{
	if(value === undefined)
	{
		return (options ? options[name] : null);
	}
	else
	{
		options[name] = value;
	}
};

/**图表展示数据对象的原始信息属性名*/
CF._ORIGINAL_DATA_INDEX_PROP_NAME = CF.BUILTIN_PROP_PREFIX + "OriginalDataIndex";

/** 关键字：注册得ECharts主题名 */
CF._KEY_REGISTERED_ECHARTS_THEME_NAME = CF.BUILTIN_PROP_PREFIX + "RegisteredEchartsThemeName";

/**
 * 将指定图表主题填充为全局图表主题，即使用<body>上的dg-chart-theme属性值填充。
 * 如果图表主题已经被此函数填充过，不会再次处理。
 * 
 * @param theme 图表主题，会被此函数修改
 */
CF._inflateGlobalChartTheme = function(theme)
{
	if(CF._themeInflated(theme))
		return false;
	
	CF._inflateActualBgColorIf(theme);
	
	var rawTheme = null;
	
	//默认值
	if(!theme.name)
		theme.name = "chartTheme";
	if(!theme.color)
		theme.color = "#333";
	//默认背景色应设为"transparent"，使得图表背景由其所在元素决定
	if(!theme.backgroundColor)
		theme.backgroundColor = "transparent";
	if(!theme.actualBackgroundColor)
		theme.actualBackgroundColor = "#FFF";
	if(!theme.gradient)
		theme.gradient = 20;
	if(!theme.graphColors || theme.graphColors.length == 0)
		theme.graphColors = ["#5470C6", "#91CC75", "#FAC858", "#EE6666", "#73C0DE", "#3BA272", "#FC8452",
						"#9A60B4", "#EA7CCC", "#B6A2DE"];
	if(!theme.graphRangeColors || theme.graphRangeColors.length == 0)
		theme.graphRangeColors = ["#58A52D", "#FFD700", "#FF4500"];
	
	CF._inflateActualBgColorIf(theme);
	
	var bodyThemeValue = jQuery(document.body).attr(elementAttrConst.THEME);
	if(bodyThemeValue)
	{
		var bodyThemeObj = CF.evalSilently(bodyThemeValue, {});
		
		//如果是引用变量，不应被修改
		if(!CF.isJsonString(bodyThemeValue))
			bodyThemeObj = CF.extend(true, {}, bodyThemeObj);
		
		CF._inflateActualBgColorIf(bodyThemeObj);
		
		rawTheme = CF.extend(true, {}, theme, bodyThemeObj);
		
		CF._inflateChartThemeIf(bodyThemeObj);
		CF.extend(true, theme, bodyThemeObj);
	}
	
	if(rawTheme == null)
		rawTheme = CF.extend(true, {}, theme);
	
	CF._inflateChartThemeIf(theme);
	
	theme._RAW_CHART_THEME = rawTheme;
	CF._themeInflated(theme, true);
	
	return true;
};

CF._themeInflated = function(theme, inflated)
{
	if(inflated === undefined)
		return (theme._INFLATED == true);
	else
		theme._INFLATED = inflated;
};

CF._inflateActualBgColorIf = function(theme)
{
	//如果设置了非透明backgroundColor，那么也应同时设置actualBackgroundColor
	if(theme.backgroundColor && theme.backgroundColor != "transparent")
	{
		theme.actualBackgroundColor = theme.backgroundColor;
		return true;
	}
	
	return false;
};

//填充图表主题，如果图表主题已设置了color、backgroundColor、actualBackgroundColor、fontSize，则尝试自动填充其他相关的主题属性。
CF._inflateChartThemeIf = function(theme)
{
	if(!theme.actualBackgroundColor)
		CF._inflateActualBgColorIf(theme);
	
	if(theme.color && theme.actualBackgroundColor)
	{
		if(!theme.borderColor)
			theme.borderColor = CF.themeGradualColor(theme, 0.3);
		
		var titleThemeGen =
		{
			name: "titleTheme",
			color: theme.color,
			backgroundColor: "transparent",
			borderColor: theme.borderColor,
			borderWidth: 0
		};
		
		theme.titleTheme = (!theme.titleTheme ? titleThemeGen : CF.extend(true, titleThemeGen, theme.titleTheme));
		
		var legendThemeGen =
		{
			name: "legendTheme",
			color: CF.themeGradualColor(theme, 0.9),
			backgroundColor: "transparent",
			borderColor: theme.borderColor,
			borderWidth: 0
		};
		
		theme.legendTheme = (!theme.legendTheme ? legendThemeGen : CF.extend(true, legendThemeGen, theme.legendTheme));
		
		var tooltipThemeGen =
		{
			name: "tooltipTheme",
			color: theme.actualBackgroundColor,
			backgroundColor: CF.themeGradualColor(theme, 0.7),
			borderColor: CF.themeGradualColor(theme, 0.9),
			borderWidth: 1
		};
		
		theme.tooltipTheme = (!theme.tooltipTheme ? tooltipThemeGen : CF.extend(true, tooltipThemeGen, theme.tooltipTheme));
		
		var highlightThemeGen =
		{
			name: "highlightTheme",
			color: theme.actualBackgroundColor,
			backgroundColor: CF.themeGradualColor(theme, 0.8),
			borderColor: CF.themeGradualColor(theme, 1),
			borderWidth: 1
		};
		
		theme.highlightTheme = (!theme.highlightTheme ? highlightThemeGen : CF.extend(true, highlightThemeGen, theme.highlightTheme));
	}
	else if(theme.color)
	{
		var titleThemeGen =
		{
			name: "titleTheme",
			color: theme.color,
			backgroundColor: "transparent",
			borderColor: theme.borderColor,
			borderWidth: 0
		};
		
		theme.titleTheme = (!theme.titleTheme ? titleThemeGen : CF.extend(true, titleThemeGen, theme.titleTheme));
		
		var legendThemeGen =
		{
			name: "legendTheme",
			color: theme.color,
			backgroundColor: "transparent",
			borderColor: theme.borderColor,
			borderWidth: 0
		};
		
		theme.legendTheme = (!theme.legendTheme ? legendThemeGen : CF.extend(true, legendThemeGen, theme.legendTheme));
	}
	
	if(theme.fontSize)
	{
		theme.titleTheme = (theme.titleTheme ? theme.titleTheme : {});
		if(!theme.titleTheme.fontSize)
			theme.titleTheme.fontSize = theme.fontSize;
		
		theme.legendTheme = (theme.legendTheme ? theme.legendTheme : {});
		if(!theme.legendTheme.fontSize)
			theme.legendTheme.fontSize = theme.fontSize;
		
		theme.tooltipTheme = (theme.tooltipTheme ? theme.tooltipTheme : {});
		if(!theme.tooltipTheme.fontSize)
			theme.tooltipTheme.fontSize = theme.fontSize;
		
		theme.highlightTheme = (theme.highlightTheme ? theme.highlightTheme : {});
		if(!theme.highlightTheme.fontSize)
			theme.highlightTheme.fontSize = theme.fontSize;
	}
	
	if(theme.borderWidth && !theme.borderStyle)
		theme.borderStyle = "solid";
};

/**
 * 由图表主题构建ECharts主题。
 * 
 * @param chartTheme 图表主题对象：org.datagear.analysis.ChartTheme
 */
CF.buildEchartsTheme = function(chartTheme)
{
	var axisColor = CF.themeGradualColor(chartTheme, 0.7);
	var axisScaleLineColor = CF.themeGradualColor(chartTheme, 0.35);
	var areaColor0 = CF.themeGradualColor(chartTheme, 0.1);
	var areaBorderColor0 = CF.themeGradualColor(chartTheme, 0.3);
	var areaColor1 = CF.themeGradualColor(chartTheme, 0.25);
	var areaBorderColor1 = CF.themeGradualColor(chartTheme, 0.5);
	var shadowColor = CF.themeGradualColor(chartTheme, 0.9);
	
	var theme =
	{
		"color" : chartTheme.graphColors,
		"backgroundColor" : chartTheme.backgroundColor,
		"textStyle" : {},
		"title" : {
	        "left" : "center",
			"textStyle" : {
				"color" : chartTheme.titleTheme.color
			},
			"subtextStyle" : {
				"color" : chartTheme.titleTheme.color
			},
			"backgroundColor" : chartTheme.titleTheme.backgroundColor
		},
		"line" : {
			"itemStyle" : {
				"borderWidth" : 2
			},
			"lineStyle" : {
				"width" : 2
			},
			"label": {
				"color": chartTheme.color
			},
			"symbol" : "circle",
			"symbolSize" : 8,
			"smooth" : false,
			"emphasis" :
			{
				"lineStyle" :
				{
					"width" : 4
				}
			}
		},
		"radar" : {
			"name" : { "textStyle" : { "color" : chartTheme.legendTheme.color } },
			"axisLine" : { "lineStyle" : { "color" : areaBorderColor0 } },
			"splitLine" : { "lineStyle" : { "color" : areaBorderColor0 } },
			"splitArea" : { "areaStyle" : { "color" : [ areaColor0, chartTheme.backgroundColor ] } },
			"itemStyle" : {
				"borderWidth" : 1
			},
			"lineStyle" : {
				"width" : 2
			},
			"emphasis" :
			{
				"lineStyle" : {
					"width" : 4,
					"shadowBlur" : 5,
					"shadowOffsetX" : 0,
					"shadowColor" : shadowColor
				}
			},
			"symbolSize" : 6,
			"symbol" : "circle",
			"smooth" : false
		},
		"bar" : {
			"itemStyle" : {
				"barBorderWidth" : 0,
				"barBorderColor" : chartTheme.borderColor
			},
			"label": {
				"color": chartTheme.color
			},
			"emphasis" : {
				"itemStyle" : {
					"barBorderWidth" : 0,
					"barBorderColor" : chartTheme.borderColor,
					"shadowBlur" : 10,
					"shadowOffsetX" : 0,
					"shadowColor" : shadowColor,
			        "shadowOffsetY" : 0
				}
			}
		},
		"pie" : {
			"itemStyle" : {
				"borderWidth" : 0,
				"borderColor" : chartTheme.borderColor
			},
			"label": {
				"color": chartTheme.color
			},
			"emphasis" :
			{
				"itemStyle":
				{
					"shadowBlur" : 10,
					"shadowOffsetX" : 0,
					"shadowColor" : shadowColor,
					"borderWidth" : 0,
					"borderColor" : chartTheme.borderColor
				}
			},
			"emptyCircleStyle":
			{
				"color": CF.themeGradualColor(chartTheme, 0),
				"borderColor": CF.themeGradualColor(chartTheme, 0.1)
			}
		},
		"scatter" : {
			"itemStyle" : {
				"borderWidth" : 0,
				"borderColor" : chartTheme.borderColor,
				"shadowBlur" : 3,
				"shadowColor" : shadowColor
			},
			"label": {
				"color": chartTheme.color
			},
			"emphasis" : {
				"itemStyle" : {
					"borderWidth" : 0,
					"borderColor" : chartTheme.borderColor,
					"shadowBlur" : 10,
					"shadowOffsetX" : 0,
					"shadowColor" : shadowColor
				}
			}
		},
		"effectScatter":
		{
			"itemStyle" : {
				"borderWidth" : 0,
				"borderColor" : chartTheme.borderColor,
				"shadowBlur" : 0,
				"shadowColor" : shadowColor
			},
			"emphasis" : {
				"itemStyle" : {
					"borderWidth" : 0,
					"borderColor" : chartTheme.borderColor,
					"shadowBlur" : 10,
					"shadowOffsetX" : 0,
					"shadowColor" : shadowColor
				}
			}
		},
		"boxplot" : {
			"itemStyle" : {
				"color": "transparent"
			},
			"emphasis" : {
				"itemStyle" : {
					"color": "transparent"
				}
			}
		},
		"parallel" : {
			"left": "10%",
            "top": "24%",
            "right": "10%",
            "bottom": "10%",
			"lineStyle" : {
				"width": 2,
				"shadowBlur" : 0,
				"shadowColor" : shadowColor
			},
			"emphasis" : {
				"lineStyle" : {
					"shadowBlur" : 4,
					"shadowOffsetX" : 0,
					"shadowColor" : shadowColor
				}
			}
		},
		"sankey" : {
			"label":
			{
				"color": chartTheme.color
			},
			"itemStyle" : {
				"borderWidth" : 0,
				"borderColor" : chartTheme.borderColor
			},
			"lineStyle":
			{
				"color": areaColor1,
				"opacity": 1
			},
			"emphasis" : {
				"itemStyle" : {
					"borderWidth" : 0,
					"borderColor" : chartTheme.borderColor
				},
				"lineStyle":
				{
					"color": axisColor,
					"opacity": 0.6
				},
				"focus": "adjacency"
			}
		},
		"funnel" :
		{
			"left": "10%",
            "top": "20%",
            "right": "10%",
            "bottom": "10%",
            "minSize": "0%",
            "maxSize": "100%",
			"label" : {
				"color" : chartTheme.color,
				"show": true,
                "position": "inside"
            },
			"itemStyle" : {
				"borderColor" : chartTheme.borderColor,
				"borderWidth" : 0
			},
			"emphasis" : {
				"label" : {
                    "fontSize" : 20
                },
				"itemStyle" : {
					"shadowBlur" : 10,
					"shadowOffsetX" : 0,
					"shadowColor" : shadowColor,
					"borderWidth" : 0,
					"borderColor" : chartTheme.borderColor
				}
			}
		},
		"gauge" : {
			"title" : { "color" : chartTheme.legendTheme.color },
			"detail":
			{
				"color": chartTheme.legendTheme.color
			},
			"progress":
			{
				"show": true
	        },
			"axisLine":
			{
				"show": true,
				"lineStyle":
				{
					"color" : [ [ 1, areaColor1 ] ]
				}
	        },
			"axisLabel":
			{
				"color" : axisColor
			},
			"splitLine":
			{
				"lineStyle":
				{
					"color": chartTheme.actualBackgroundColor
				}
			},
			"axisTick":
			{
				"lineStyle":
				{
					"color": chartTheme.actualBackgroundColor
				}
			},
			"itemStyle" : {
				"borderColor" : chartTheme.borderColor,
				"borderWidth" : 0
			},
			"emphasis" : {
				"itemStyle" : {
					"shadowBlur" : 10,
					"shadowOffsetX" : 0,
					"shadowColor" : shadowColor,
					"borderWidth" : 0,
					"borderColor" : chartTheme.borderColor
				}
			}
		},
		"candlestick" : {
			"itemStyle" : {
				"borderWidth" : 1
			},
			"emphasis" : {
				"itemStyle" : {
					"shadowBlur" : 10,
					"shadowOffsetX" : 0,
					"shadowColor" : shadowColor
				}
			}
		},
		"heatmap":
		{
			"label":
			{
				"show": true
			},
			"emphasis" :
			{
				"itemStyle" :
				{
					"shadowBlur" : 5
				}
			}
		},
		"tree":
		{
			"expandAndCollapse": true,
			"label":
			{
				"color": chartTheme.color
			},
			"itemStyle":
			{
				"color": chartTheme.color
			},
			"lineStyle": { "color": areaBorderColor0 },
			"emphasis" :
			{
				"itemStyle" : {
					"shadowBlur" : 10,
					"shadowOffsetX" : 0,
					"shadowColor" : shadowColor
				}
			}
		},
		"treemap":
		{
			"itemStyle" :
			{
				"borderWidth": 0.5,
				"borderColor": chartTheme.backgroundColor
			},
			"emphasis" :
			{
				"itemStyle" : {
					"shadowBlur" : 10,
					"shadowOffsetX" : 0,
					"shadowColor" : shadowColor,
					"borderWidth" : 0,
					"borderColor" : chartTheme.borderColor
				}
			},
			"breadcrumb":
			{
				"itemStyle":
				{
					"color": chartTheme.backgroundColor,
					"borderColor": chartTheme.borderColor,
					"shadowBlur": 0,
					"textStyle": { color: chartTheme.color }
				}
			}
		},
		"sunburst":
		{
			"itemStyle" :
			{
				"borderWidth" : 1,
				"borderColor" : chartTheme.backgroundColor
			},
			"emphasis" :
			{
				"itemStyle" :
				{
					"shadowBlur" : 10,
					"shadowColor" : shadowColor,
					"borderColor" : chartTheme.borderColor
				}
			}
		},
		"graph" :
		{
			"left": "12%",
            "right": "12%",
            "top": "20%",
            "bottom": "12%",
			"roam": true,
			"itemStyle" : {
				"borderWidth" : 0,
				"borderColor" : chartTheme.borderColor,
				"shadowBlur" : 2,
				"shadowColor" : shadowColor
			},
			"lineStyle" : {
                "color": "source",
                "curveness": 0.3
			},
			"label" : {
				"color" : chartTheme.color
			},
			"emphasis" : {
				"itemStyle" : {
					"borderWidth" : 0,
					"borderColor" : chartTheme.borderColor,
					"shadowBlur" : 10,
					"shadowOffsetX" : 0,
					"shadowColor" : shadowColor
				},
				"lineStyle" : {
					"width": 4
				},
				"focus": "adjacency",
				"legendHoverLink": true,
				"label": { "position": "right" }
			}
		},
		"map" : {
			"roam" : true,
			"itemStyle" : {
				"areaColor" : areaBorderColor0,
				"borderColor" : areaBorderColor1,
				"borderWidth" : 0.5
			},
			"label" : {
				"show": true,
				"color" : chartTheme.color
			},
			"emphasis" :
			{
				"label":
				{
					"color" : chartTheme.highlightTheme.color
				},
				"itemStyle":
				{
					"areaColor" : chartTheme.highlightTheme.backgroundColor,
					"borderColor" : chartTheme.highlightTheme.borderColor,
					"borderWidth" : 1
				}
			}
		},
		"lines":
		{
			"lineStyle":
			{
				"width": 2
			},
			"emphasis":
			{
				"lineStyle":
				{
					"shadowBlur" : 4,
					"shadowOffsetX" : 0,
					"shadowColor" : shadowColor
				}
			}
		},
		"geo" : {
			"itemStyle" : {
				"areaColor" : areaBorderColor0,
				"borderColor" : areaBorderColor1,
				"borderWidth" : 0.5
			},
			"label" : {
				"color" : chartTheme.color
			},
			"emphasis" :
			{
				"label":
				{
					"color" : chartTheme.highlightTheme.color
				},
				"itemStyle":
				{
					"areaColor" : chartTheme.highlightTheme.backgroundColor,
					"borderColor" : chartTheme.highlightTheme.borderColor,
					"borderWidth" : 1
				}
			}
		},
		"themeRiver":
		{
			/*ECharts-5.3.2版本这里配置不起作用
			"left": "10%",
            "top": "24%",
            "right": "10%",
            "bottom": "10%",
			*/
			"label":
			{
				"show": true 
			},
			"emphasis":
			{
				"itemStyle":
				{
					"shadowBlur": 10,
					"shadowColor": shadowColor
				}
			}
		},
		"categoryAxis" : {
			"axisLine" : {
				"show" : true,
				"lineStyle" : {
					"color" : axisColor
				}
			},
			"axisTick" : {
				"show" : true,
				"lineStyle" : {
					"color" : axisColor
				}
			},
			"axisLabel" : {
				"show" : true,
				"textStyle" : {
					"color" : axisColor
				}
			},
			"splitLine" : {
				"show" : true,
				"lineStyle" : {
					"type" : "dotted",
					"color" : [ axisScaleLineColor ]
				}
			},
			"splitArea" : {
				"show" : false,
				"areaStyle" : {
					"color" : [ axisScaleLineColor ]
				}
			}
		},
		"valueAxis" : {
			"axisLine" : {
				"show" : true,
				"lineStyle" : {
					"color" : axisColor
				}
			},
			"axisTick" : {
				"show" : true,
				"lineStyle" : {
					"color" : axisColor
				}
			},
			"axisLabel" : {
				"show" : true,
				"textStyle" : {
					"color" : axisColor
				}
			},
			"splitLine" : {
				"show" : true,
				"lineStyle" : {
					"type" : "dotted",
					"color" : [ axisScaleLineColor ]
				}
			},
			"splitArea" : {
				"show" : false,
				"areaStyle" : {
					"color" : [ axisScaleLineColor ]
				}
			}
		},
		"logAxis" : {
			"axisLine" : {
				"show" : true,
				"lineStyle" : {
					"color" : axisColor
				}
			},
			"axisTick" : {
				"show" : true,
				"lineStyle" : {
					"color" : axisColor
				}
			},
			"axisLabel" : {
				"show" : true,
				"textStyle" : {
					"color" : axisColor
				}
			},
			"splitLine" : {
				"show" : true,
				"lineStyle" : {
					"type" : "dotted",
					"color" : [ axisScaleLineColor ]
				}
			},
			"splitArea" : {
				"show" : false,
				"areaStyle" : {
					"color" : [ axisScaleLineColor ]
				}
			}
		},
		"timeAxis" : {
			"axisLine" : {
				"show" : true,
				"lineStyle" : {
					"color" : axisColor
				}
			},
			"axisTick" : {
				"show" : true,
				"lineStyle" : {
					"color" : axisColor
				}
			},
			"axisLabel" : {
				"show" : true,
				"textStyle" : {
					"color" : axisColor
				}
			},
			"splitLine" : {
				"show" : true,
				"lineStyle" : {
					"type" : "dotted",
					"color" : [ axisScaleLineColor ]
				}
			},
			"splitArea" : {
				"show" : false,
				"areaStyle" : {
					"color" : [ axisScaleLineColor ]
				}
			}
		},
		/*ECharts-5.3.2版本这里配置不起作用（主题河流图）
		"singleAxis":
		{
			"left": "30%",
            "top": "54%",
            "right": "30%",
            "bottom": "40%"
		},
		*/
		"toolbox" : {
			"iconStyle" : {
				"normal" : {
					"borderColor" : chartTheme.borderColor
				},
				"emphasis" : {
					"borderColor" : axisColor
				}
			}
		},
		"grid":
		{
			"left": "6%",
			"right": "10%",
			"top": "20%",
			"bottom": "8%",
			"containLabel": true
		},
		"legend" : {
			"orient": "horizontal",
			"top": 25,
			"textStyle" : {
				"color" : chartTheme.legendTheme.color
			},
			"inactiveColor" : axisScaleLineColor,
			"backgroundColor" : chartTheme.legendTheme.backgroundColor
		},
		"tooltip" : {
			"backgroundColor" : chartTheme.tooltipTheme.backgroundColor,
			"borderColor" : chartTheme.tooltipTheme.borderColor,
			"borderWidth" : chartTheme.tooltipTheme.borderWidth,
			"textStyle" : { color: chartTheme.tooltipTheme.color },
			"axisPointer" : {
				"lineStyle" : {
					"color" : axisColor,
					"width" : "1"
				},
				"crossStyle" : {
					"color" : axisColor,
					"width" : "1"
				}
			}
		},
		"timeline" : {
			"lineStyle" : {
				"color" : axisColor,
				"width" : 1
			},
			"itemStyle" : {
				"normal" : {
					"color" : chartTheme.color,
					"borderWidth" : 1
				},
				"emphasis" : {
					"color" : chartTheme.color
				}
			},
			"controlStyle" : {
				"normal" : {
					"color" : chartTheme.color,
					"borderColor" : chartTheme.borderColor,
					"borderWidth" : 0.5
				},
				"emphasis" : {
					"color" : chartTheme.color,
					"borderColor" : chartTheme.borderColor,
					"borderWidth" : 0.5
				}
			},
			"checkpointStyle" : {
				"color" : chartTheme.highlightTheme.backgroundColor,
				"borderColor" : chartTheme.highlightTheme.borderColor
			},
			"label" : {
				"normal" : {
					"textStyle" : {
						"color" : axisColor
					}
				},
				"emphasis" : {
					"textStyle" : {
						"color" : chartTheme.color
					}
				}
			}
		},
		"visualMap" : {
			"inRange" :
			{
				"color" : chartTheme.graphRangeColors
			},
			"backgroundColor" : "transparent",
			"textStyle" :
			{
				"color" : axisColor
			}
		},
		"dataZoom" : {
			"backgroundColor" : "transparent",
			"dataBackgroundColor" : axisScaleLineColor,
			"fillerColor" : axisScaleLineColor,
			"handleColor" : axisScaleLineColor,
			"handleSize" : "100%",
			"textStyle" : {
				"color" : axisColor
			}
		},
		"markPoint" : {
			"label" : {
				"normal" : {
					"textStyle" : {
						"color" : axisColor
					}
				},
				"emphasis" : {
					"textStyle" : {
						"color" : axisColor
					}
				}
			}
		}
	};
	
	//不能在上述theme中直接设置fontSize，因为即时值为null，仍然会改变默认字体
	
	if(chartTheme.fontSize)
	{
		theme.textStyle = (theme.textStyle || {});
		theme.textStyle.fontSize = chartTheme.fontSize;
		
		theme.categoryAxis.axisLabel.textStyle.fontSize = chartTheme.fontSize;
		theme.valueAxis.axisLabel.textStyle.fontSize = chartTheme.fontSize;
		theme.logAxis.axisLabel.textStyle.fontSize = chartTheme.fontSize;
		theme.timeAxis.axisLabel.textStyle.fontSize = chartTheme.fontSize;
		theme.gauge.title.fontSize = chartTheme.fontSize;
		theme.gauge.detail.fontSize = chartTheme.fontSize;
		theme.gauge.axisLabel.fontSize = chartTheme.fontSize;
		theme.sankey.label.fontSize = chartTheme.fontSize;
		theme.themeRiver.label.fontSize = chartTheme.fontSize;
	}
	if(chartTheme.titleTheme.fontSize)
		theme.title.textStyle.fontSize = chartTheme.titleTheme.fontSize;
	if(chartTheme.legendTheme.fontSize)
		theme.legend.textStyle.fontSize = chartTheme.legendTheme.fontSize;
	if(chartTheme.tooltipTheme.fontSize)
		theme.tooltip.textStyle.fontSize = chartTheme.tooltipTheme.fontSize;
	
	return theme;
};

/**
 * 加载库，并在全部加载完成后（无论是否成功）执行回调函数。
 * 库对象结构为：
 * {
 *   //库名称，应尽量使用库本身定义的全局名称
 *   name: "..."、[ "...", ... ],
 *   //版本号，应符合语义化版本规范："X.Y.Z"、"X.Y.Z-BUILD"
 *   version: "...",
 *   //库源
 *   source:
 *   //库源URL
 *   "..."、
 *   //库源对象
 *   {
 *     //库源URL，应是可直接加载的URL
 *     url: "lib0/b.css",
 *     //可选，库源类型，自动识别JS、CSS
 *     type: "css"
 *   }、
 *   //库源URL/对象数组
 *   [ "...", { ... }, ... ],
 *   //可选，依赖库名称/数组
 *   depend: "..."、[ "..."、... ],
 *   //可选，检查当前环境是否已经加载了这个名称的库，返回值：true 是；其他 否。
 *   //默认值是：如果this.name已在window下已定义，返回true；否则，返回false。
 *   loaded: function(){ ... }
 * }
 * 
 * @param lib 库对象、数组
 * @param callback 加载完成后回调函数（无论是否成功都将执行），格式为：function(){ ... }
 * @param contextCharts 可选，上下文图表数组，对于相同名称的库，将在contextCharts中加载最新版本那个，默认值：[]
 */
CF.loadLib = function(lib, callback, contextCharts)
{
	contextCharts = (contextCharts == null ? [] : contextCharts);
	
	if(!lib)
	{
		callback();
	}
	
	if(!CF.isArray(lib))
		lib = [ lib ];
	
	var unloadeds = [];
	CF.inflateUnloadedLibs(contextCharts, lib, unloadeds);
	
	if(unloadeds.length == 0)
	{
		callback();
	}
	else
	{
		var stateObjs = [];
		var deferreds = [];
		var loadedCallback = function()
		{
			CF.loadLibInner(unloadeds, stateObjs);
		};
		
		for(var i=0; i<unloadeds.length; i++)
		{
			var stateObj = CF.libState(unloadeds[i], true, CF.LIB_STATE_INIT, false, loadedCallback);
			stateObjs.push(stateObj);
			deferreds.push(stateObj.loadedDeferred);
		}
		
		jQuery.when.apply(jQuery, deferreds).always(function(){ callback(); });
		
		for(var i=0; i<stateObjs.length; i++)
		{
			CF.triggerLibStateResolvedIfLoaded(stateObjs[i]);
		}
		
		CF.loadLibInner(unloadeds, stateObjs);
	}
};

//填充所有待加载库，填充后，unloadeds中都是最新版本库，且都包含依赖库
CF.inflateUnloadedLibs = function(contextCharts, libs, unloadeds)
{
	for(var i=0; i<libs.length; i++)
	{
		var lib = libs[i];
		
		if(CF.isLibLoadedInEnv(lib))
		{
			continue;
		}
		
		var stateObj = CF.libState(lib);
		if(stateObj && stateObj.state == CF.LIB_STATE_LOADED)
		{
			continue;
		}
		
		var latestLib = CF.findLatestLibInCharts(contextCharts, lib);
		
		if(latestLib !== lib)
		{
			if(CF.isLibLoadedInEnv(latestLib))
			{
				//如果最新版已在环境中加载，应将其状态设为loaded，以减少后续加载操作的搜索步骤
				CF.libState(latestLib, true, CF.LIB_STATE_LOADED, true);
				continue;
			}
			
			stateObj = CF.libState(latestLib);
			if(stateObj && stateObj.state == CF.LIB_STATE_LOADED)
			{
				continue;
			}
		}
		
		if(CF.libIndex(unloadeds, latestLib.name) > -1)
			continue;
		
		unloadeds.push(latestLib);
		
		//处理依赖
		if(latestLib.depend)
		{
			var depend = latestLib.depend;
			var dependLibs = [];
			
			if(!CF.isArray(depend))
				depend = [ depend ];
			
			for(var j=0; j<depend.length; j++)
			{
				var dependName = depend[j];
				
				if(CF.libIndex(unloadeds, dependName) > -1)
					continue;
				
				if(CF.libIndex(libs, dependName) > -1)
					continue;
				
				if(CF.libIndex(dependLibs, dependName) > -1)
					continue;
				
				var dependLib = CF.findFirstLibInCharts(contextCharts, dependName);
				
				if(dependLib != null)
				{
					dependLibs.push(dependLib);
				}
				else
				{
					CF.logException("no lib found for name : '"+dependName+"', load ignored");
				}
			}
			
			if(dependLibs.length > 0)
			{
				CF.inflateUnloadedLibs(contextCharts, dependLibs, unloadeds);
			}
		}
	}
};

CF.loadLibInner = function(libs, stateObjs)
{
	for(var i=0; i<libs.length; i++)
	{
		var lib = libs[i];
		var stateObj = stateObjs[i];
		
		if(stateObj.state === CF.LIB_STATE_INIT && CF.isLibReadyForLoad(lib))
		{
			stateObj.state = CF.LIB_STATE_LOADING;
			
			var source = stateObj.lib.source;
			var srcDfds = stateObj.sourceLoadedDeferreds;
			
			if(source != null)
			{
				if(!CF.isArray(source))
					source = [ source ];
				
				for(var j=0; j<source.length; j++)
				{
					CF.loadSingleLibSource(lib, source[j], srcDfds[j]);
				}
			}
		}
	}
};

CF.isLibReadyForLoad = function(lib)
{
	var depend = lib.depend;
	
	if(CF.isNullOrEmpty(depend))
		return true;
	
	if(!CF.isArray(depend))
		depend = [ depend ];
	
	var ready = true;
	
	for(var i=0; i<depend.length; i++)
	{
		var dependName = depend[i];
		var dependStateObj = CF.libStateByName(dependName);
		//没有找到依赖库也应认为已ready，因为通过HTML的<script>标签引入的库这里dependStateObj为null
		ready = (dependStateObj == null || dependStateObj.state == CF.LIB_STATE_LOADED);
		
		if(!ready)
		{
			break;
		}
	}
	
	return ready;
};

CF.loadSingleLibSource = function(lib, source, deferred)
{
	if(deferred.state() !== "pending")
		return;
	
	if(CF.isString(source))
	{
		source = { url: source, type: CF.resolveLibSourceType(source) };
	}
	
	if(source.type == "js")
	{
		CF.loadSingleJsLibSource(lib, source, deferred);
	}
	else if(source.type == "css")
	{
		CF.loadSingleCssLibSource(lib, source, deferred);
	}
	else
	{
		deferred.resolve();
		CF.logException("Unknown lib source type '"+source.type+"', load ignored");
	}
};

CF.loadSingleJsLibSource = function(lib, source, deferred)
{
	var ele = document.createElement("script");
	
	ele.src = source.url;
	ele.type = "text/javascript";
	ele.onload = function(){ deferred.resolve(); };
	ele.onerror = function(){ deferred.resolve(); };
	
	CF.addLibSourceEleToDoc(lib, ele);
};

CF.loadSingleCssLibSource = function(lib, source, deferred)
{
	var ele = document.createElement("link");
	
	ele.href = source.url;
	ele.type = "text/css";
	ele.rel = "stylesheet";
	ele.onload = function(){ deferred.resolve(); };
	ele.onerror = function(){ deferred.resolve(); };
	
	CF.addLibSourceEleToDoc(lib, ele);
};

/**
 * 在DOM中插入依赖库源。
 * 插入规则：
 * 一级优先：插入在最后一个看板引入库（dg-lib-name）之后、且为其添加dg-lib-name属性，
 * 			确保其可以使用之前依赖库和内置引入库、且可以被全部生成样式表覆盖（参考CF.styleSheetText()函数说明）；
 * 二级优先：插入在<head>末尾。
 * 
 * @param lib 库对象
 * @param ele 库对应的DOM对象
 */
CF.addLibSourceEleToDoc = function(lib, ele)
{
	jQuery(ele).attr(CF.LIB_ATTR_NAME, lib.name);
	
	var $head = jQuery("head:first");
	var headEle = $head[0];
	var beforeEle = null;
	
	var $lastImport = jQuery("["+CF.LIB_ATTR_NAME+"]:last", $head);
	if($lastImport.length > 0)
	{
		var $next = $lastImport.next();
		if($next.length > 0)
		{
			beforeEle = $next[0];
		}
	}
	
	//这里不能使用jQuery的API，会无法正常执行绑定事件
	if(beforeEle != null)
		headEle.insertBefore(ele, beforeEle);
	else
		headEle.appendChild(ele);
};

CF.resolveLibSourceType = function(url)
{
	var qsIdx = url.indexOf("?");
	if(qsIdx < 0)
		qsIdx = url.indexOf("#");
	
	if(qsIdx > 0)
		url = url.substring(0, qsIdx);
	
	var type = "";
	
	if(CF.LIB_JS_SOURCE_REGEX.test(url))
	{
		type = "js";
	}
	else if(CF.LIB_CSS_SOURCE_REGEX.test(url))
	{
		type = "css";
	}
	else
	{
		var didx = url.lastIndexOf(".");
		
		if(didx > -1 && didx < url.length - 1)
			type = url.substring(didx+1);
	}
	
	return type;
};

CF.LIB_JS_SOURCE_REGEX = /\.(js)$/i;
CF.LIB_CSS_SOURCE_REGEX = /\.(css)$/i;

//查找最新版的库
CF.findLatestLibInCharts = function(charts, lib)
{
	if(charts == null)
		return lib;
	
	var rendererLatestLib = lib;
	var pluginLatestLib = lib;
	var pluginLatestLibChart = null;
	
	for(var i=0; i<charts.length; i++)
	{
		var chart = charts[i];
		var renderer = chart.renderer();
		var rendererLib = CF.rendererLib(renderer);
		rendererLatestLib = CF.findLatestLibInLibs(rendererLib, rendererLatestLib);
	}
	
	for(var i=0; i<charts.length; i++)
	{
		var chart = charts[i];
		var pluginRenderer = (chart.plugin ? chart.plugin.renderer : null);
		var rendererLib = CF.rendererLib(pluginRenderer);
		var myPluginLatestLib = CF.findLatestLibInLibs(rendererLib, pluginLatestLib);
		
		if(myPluginLatestLib !== pluginLatestLib)
		{
			pluginLatestLib = myPluginLatestLib;
			pluginLatestLibChart = chart;
		}
	}
	
	//图表渲染器在看板页面定义，所以其依赖库应该优先使用
	var latestLib = CF.resolveLatestLibByBase(rendererLatestLib, pluginLatestLib);
	
	//如果是插件依赖库，需要转换为可用依赖库
	if(latestLib !== lib && latestLib === pluginLatestLib && pluginLatestLibChart != null)
	{
		latestLib = CF.convertPluginRendererLib(pluginLatestLibChart, latestLib);
	}
	
	return latestLib;
};

CF.findLatestLibInLibs = function(libs, lib)
{
	if(libs == null)
		return lib;
	
	var latestLib = lib;
	
	if(CF.isArray(libs))
	{
		for(var i=0; i<libs.length; i++)
		{
			latestLib = CF.resolveLatestLibByBase(latestLib, libs[i]);
		}
	}
	else
	{
		latestLib = CF.resolveLatestLibByBase(latestLib, libs);
	}
	
	return latestLib;
};

//如果compareLib与baseLib同名，且版本更高，返回compareLib；否则，返回baseLib
CF.resolveLatestLibByBase = function(baseLib, compareLib)
{
	if(compareLib == null)
		return baseLib;
	
	var latestLib = baseLib;
	
	var name = CF.resolveSameLibName(baseLib.name, compareLib.name);
	
	if(name != null)
	{
		//只有找到更高版本号的才替换，否则应该优先使用传入的lib参数
		var lower = (CF.compareLibVersion(name, baseLib.version, compareLib.version) < 0);
		
		if(lower)
		{
			latestLib = compareLib;
		}
	}
	
	return latestLib;
};

//查找第一个库
CF.findFirstLibInCharts = function(charts, name)
{
	if(charts == null)
		return null;
	
	for(var i=0; i<charts.length; i++)
	{
		var chart = charts[i];
		var renderer = chart.renderer();
		var rendererLib = CF.rendererLib(renderer);
		var firstLib = CF.findFirstLibInLibs(rendererLib, name);
		
		if(firstLib != null)
			return firstLib;
		
		var pluginRenderer = (chart.plugin ? chart.plugin.renderer : null);
		rendererLib = CF.rendererLib(pluginRenderer);
		firstLib = CF.findFirstLibInLibs(rendererLib, name);
		
		//插件依赖库需要转换为可用依赖库
		if(firstLib != null)
			return CF.convertPluginRendererLib(chart, firstLib);
	}
	
	return null;
};

//查找第一个库
CF.findFirstLibInLibs = function(libs, name)
{
	if(libs == null)
		return null;
	
	if(CF.isArray(libs))
	{
		for(var i=0; i<libs.length; i++)
		{
			if(CF.resolveSameLibName(libs[i].name, name))
				return libs[i];
		}
	}
	else
	{
		if(CF.resolveSameLibName(libs.name, name))
				return libs;
	}
	
	return null;
};

/**
 * 比较库版本号。
 * 
 * @param name 库名
 * @param v1
 * @param v2
 * @returns -1 v1低于v2；0 v1等于v2；1 v1高于v2
 */
CF.compareLibVersion = function(name, v1, v2)
{
	return CF.compareVersion(v1, v2);
};

//查找第一个同名的库索引
CF.libIndex = function(libs, name)
{
	for(var i=0; i<libs.length; i++)
	{
		if(CF.resolveSameLibName(libs[i].name, name))
			return i;
	}
	
	return -1;
};

//当前环境是否已加载了指定库
CF.isLibLoadedInEnv = function(lib)
{
	if(lib.loaded != null)
	{
		return lib.loaded();
	}
	else
	{
		if(CF.isString(lib.name))
		{
			return (window[lib.name] !== undefined);
		}
		else
		{
			for(var i=0; i<lib.name.length; i++)
			{
				if(window[lib.name[i]] !== undefined)
				{
					return true;
				}
			}
		}
		
		return false;
	}
};

//解析库名称交集第一个，返回null表示无交集
CF.resolveSameLibName = function(baseLibName, compareLibName)
{
	if(baseLibName == null || baseLibName.length == 0
		|| compareLibName == null || compareLibName.length == 0)
	{
		return null;
	}
	
	var baseNameArray = (!CF.isString(baseLibName));
	
	if(baseLibName === compareLibName)
	{
		if(!baseNameArray)
			return baseLibName;
		else
			return baseLibName[0];
	}
	
	var compareNameArray = (!CF.isString(compareLibName));
	
	if(!baseNameArray && !compareNameArray)
	{
		return null;
	}
	else if(!baseNameArray)
	{
		var idx = CF.inArray(baseLibName, compareLibName);
		return (idx > -1 ? baseLibName : null);
	}
	else if(!compareNameArray)
	{
		var idx = CF.inArray(compareLibName, baseLibName);
		return (idx > -1 ? compareLibName : null);
	}
	else
	{
		for(var i=0; i<baseLibName; i++)
		{
			var idx = CF.inArray(baseLibName[i], compareLibName);
			if(idx > -1)
			{
				return baseLibName[i];
			}
		}
		
		return null;
	}
};

/**
 * 获取库状态信息。
 * 
 * @param lib 库对象
 * @param nonNull 可选，是否返回非null，默认为：false
 * @param createState 可选，当要返回nonNull时，需要创建的状态，默认为：LIB_STATE_INIT
 * @param resolvedIfLoaded 可选，当要返回nonNull时，如果库状态为已加载、或者没有需要加载的库，是否触发resolve逻辑
 * @param loadedCallback 可选，当要返回nonNull时，加载完成回调函数
 */
CF.libState = function(lib, nonNull, createState, resolvedIfLoaded, loadedCallback)
{
	if(nonNull !== true)
	{
		return CF.libStateByName(lib.name);
	}
	else
	{
		var stateObj = CF.libState(lib);
		
		if(stateObj == null)
		{
			var states = CF.LIB_STATES;
			stateObj = CF.createLibState(lib, createState, resolvedIfLoaded, loadedCallback);
			
			if(CF.isString(lib.name))
			{
				states[lib.name] = stateObj;
			}
			else
			{
				for(var i=0; i<lib.name.length; i++)
				{
					states[lib.name[i]] = stateObj;
				}
			}
		}
		
		return stateObj;
	}
};

/**
 * 获取指定名称的库状态，没有则返回null
 */
CF.libStateByName = function(name)
{
	if(name == null)
		return null;
	
	var states = CF.LIB_STATES;
	
	if(CF.isString(name))
	{
		return states[name];
	}
	else
	{
		for(var i=0; i<name.length; i++)
		{
			if(states[name[i]])
			{
				return states[name[i]];
			}
		}
	}
	
	return null;
};

CF.createLibState = function(lib, state, resolvedIfLoaded, loadedCallback)
{
	//应深度复制lib，避免可能的修改导致状态错乱
	lib = CF.deepCloneLib(lib);
	state = (state == null ? CF.LIB_STATE_INIT : state);
	resolvedIfLoaded = (resolvedIfLoaded == null ? false : resolvedIfLoaded);
	loadedCallback = (loadedCallback == null ? null : loadedCallback);
	
	//无论state是何状态，都应设置loadedDeferred、sourceLoadedDeferreds，
	//确保其在异步调用中结构完整
	var stateObj =
	{
		//库对象
		lib: lib,
		//库状态，参考：CF.LIB_STATE_*
		state: state,
		//库加载完成后的回调函数
		loadedDeferred: jQuery.Deferred(),
		//库中source对应的加载完成后回调函数
		sourceLoadedDeferreds: []
	};
	
	stateObj.loadedDeferred.always(function()
	{
		stateObj.state = CF.LIB_STATE_LOADED;
		
		if(loadedCallback != null)
		{
			CF.executeSilently(loadedCallback);
		}
	});
	
	var source = stateObj.lib.source;
	var sourceLen = (source == null ? 0 : (CF.isArray(source) ? source.length : 1));
	
	if(sourceLen > 0)
	{
		for(var i=0; i<sourceLen; i++)
		{
			stateObj.sourceLoadedDeferreds[i] = jQuery.Deferred();
		}
		
		jQuery.when.apply(jQuery, stateObj.sourceLoadedDeferreds).always(function(){ stateObj.loadedDeferred.resolve(); });
	}
	
	if(resolvedIfLoaded)
	{
		CF.triggerLibStateResolvedIfLoaded(stateObj);
	}
	
	return stateObj;
};

CF.triggerLibStateResolvedIfLoaded = function(stateObj)
{
	var source = stateObj.lib.source;
	var sourceLen = (source == null ? 0 : (CF.isArray(source) ? source.length : 1));
	
	if(sourceLen == 0)
	{
		stateObj.state = CF.LIB_STATE_LOADED;
		stateObj.loadedDeferred.resolve();
	}
	
	if(stateObj.state == CF.LIB_STATE_LOADED)
	{
		for(var i=0; i<sourceLen; i++)
		{
			stateObj.sourceLoadedDeferreds[i].resolve();
		}
	}
};

CF.deepCloneLib = function(lib)
{
	if(!lib)
		return lib;
	
	if(CF.isArray(lib))
	{
		var newLibs = [];
		
		for(var i=0; i<lib.length; i++)
		{
			var newLib = CF.extend(true, {}, lib[i]);
			newLibs.push(newLib);
		}
		
		return newLibs;
	}
	else
	{
		var newLib = CF.extend(true, {}, lib);
		return newLib;
	}
};

//库及其状态，键值结构：库名 -> 库信息。
CF.LIB_STATES = {};

//库状态：初始化
CF.LIB_STATE_INIT = "init";
//库状态：加载中
CF.LIB_STATE_LOADING = "loading";
//库状态：加载完成
CF.LIB_STATE_LOADED = "loaded";

CF.convertPluginRendererLib = function(chart, lib)
{
	if(!lib)
		return lib;
	
	lib = CF.deepCloneLib(lib);
	
	if(CF.isArray(lib))
	{
		for(var i=0; i<lib.length; i++)
		{
			CF.trimPluginRendererLibSource(chart, lib[i]);
		}
	}
	else
	{
		CF.trimPluginRendererLibSource(chart, lib);
	}
	
	return lib;
};

CF.trimPluginRendererLibSource = function(chart, lib)
{
	if(!lib.source)
		return;
	
	if(CF.isArray(lib.source))
	{
		for(var i=0; i<lib.source.length; i++)
		{
			lib.source[i] = CF.trimPluginRendererLibSourceUrl(chart, lib.source[i]);
		}
	}
	else
	{
		lib.source = CF.trimPluginRendererLibSourceUrl(chart, lib.source);
	}
};

//将图表插件的依赖库url解析为可直接加载的绝对路径
CF.trimPluginRendererLibSourceUrl = function(chart, singleSource)
{
	var isStr = CF.isString(singleSource);
	var url = (isStr ? singleSource : singleSource.url);
	
	if(!url)
		return singleSource;
	
	//相对应用根路径
	if(url.indexOf("/") == 0)
	{
		url = chart.contextURL(url);
	}
	//绝对路径
	else if(CF.HTTP_S_PREFIX_REGEX.test(url))
	{
		url = url;
	}
	//插件内路径
	else
	{
		url = chart.pluginResourceURL(url);
	}
	
	if(isStr)
		singleSource = url;
	else
		singleSource.url = url;
	
	return singleSource;
};

/**
 * 获取插件渲染器依赖库：renderer.depend，
 * 如果renderer.depend是函数，将返回renderer.depend()的执行结果。
 * 
 * @returns 返回undefined表示未定义
 */
CF.rendererLib = function(renderer)
{
	if(!renderer || renderer.depend === undefined)
	{
		return undefined;
	}
	
	if(renderer.depend == null)
	{
		return null;
	}
	else if(CF.isFunction(renderer.depend))
	{
		return renderer.depend();
	}
	else
	{
		return renderer.depend;
	}
};

//以http://或者https://开头的正则表达式
CF.HTTP_S_PREFIX_REGEX = /^(http:\/\/|https:\/\/)/i;

/**
 * 获取/设置指定对象的"query"字段值
 */
CF.queryOfObject = function(obj, query)
{
	if(query === undefined)
	{
		return (obj ? obj.query : null);
	}
	else
	{
		obj.query = query;
	}
};

/**
 * 获取/设置图表结果对象的查询信息。
 */
CF.chartQueryOfChartResult = function(chartResult, chartQuery)
{
	if(chartQuery === undefined)
	{
		return CF.queryOfObject(chartResult);
	}
	else
	{
		if(!chartResult)
			return;
		
		CF.queryOfObject(chartResult, chartQuery);
		// 这里不必再为每个数据集结果设置数据集查询，增加复杂性，后续看板2.0将直接开放图表结果对象，从中可以获取数据集查询信息
	}
};

/**
 * 获取/设置图表错误对象的查询信息。
 */
CF.chartQueryOfChartError = function(chartError, chartQuery)
{
	if(chartQuery === undefined)
	{
		return CF.queryOfObject(chartError);
	}
	else
	{
		if(!chartError)
			return;
		
		CF.queryOfObject(chartError, chartQuery);
	}
};

/**
 * 尝试将给定值转换为符合数据集参数类型
 */
CF.convertDataSetParamValue = function(dataSetParam, value)
{
	if(!dataSetParam || value == null)
		return value;
	
	var re = value;
	
	if(CF.isArray(value))
	{
		re = [];
		
		for(var i=0; i<value.length; i++)
		{
			re[i] = CF.convertDataSetParamValue(dataSetParam, value[i]);
		}
	}
	else if(CF.DataSetParamType.STRING == dataSetParam.type)
	{
		re = (CF.isString(value) ? value : value.toString());
	}
	else if(CF.DataSetParamType.BOOLEAN == dataSetParam.type)
	{
		if(value === true || value === false)
		{
			re = value;
		}
		else if(CF.isString(value))
		{
			//与后台DataSetParamValueConverter规则一致
			re = (value == "true" || value == "1");
		}
		else
			re = (value ? true : false);
	}
	else if(CF.DataSetParamType.NUMBER == dataSetParam.type)
	{
		if(CF.isNumber(value))
		{
			re = value;
		}
		else
		{
			re = Number(value);
			
			//如果由字符串转数值丢失精度，则撤销转换，交由后台处理
			if(CF.isString(value) && re.toString() != value)
			{
				re = value;
			}
		}
	}
	
	return re;
};


//-------------
// < 已弃用函数 start
//-------------

//-------------
// > 已弃用函数 end
//-------------

})(this);
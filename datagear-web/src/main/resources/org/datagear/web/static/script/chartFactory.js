/*
 * Copyright 2018-2023 datagear.tech
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
 * 此图表工厂支持为<body>元素、图表元素添加elementAttrConst.LISTENER属性来设置图表监听器，格式参考chartBase.listener函数参数说明。
 * 
 * 此图表工厂支持为图表元素添加elementAttrConst.MAP属性来设置地图图表的地图名。
 * 
 * 此图表工厂支持为<body>元素、图表元素添加elementAttrConst.ECHARTS_THEME属性来设置图表ECharts主题名。
 * 
 * 此图表工厂支持为<body>元素、图表元素添加elementAttrConst.DISABLE_SETTING属性，用于禁用图表交互设置功能，
 * 值为"true"表示禁用，其他表示启用。
 * 
 * 此图表工厂支持为图表元素添加"dg-chart-on-*"属性来设置图表事件处理函数，具体参考chartBase._initEventHandlers函数说明。
 * 
 * 此图表工厂支持为图表元素添加elementAttrConst.RENDERER属性来自定义、扩展图表渲染器，具体参考chartBase._initRenderer函数说明。
 * 
 * 此图表工厂要求图表插件的图表渲染器（chartBase.plugin.renderer）格式为：
 * {
 *   //可选，渲染图表函数是否是异步函数，默认为false
 *   asyncRender: true、false、function(chart){ ...; return true 或者 false; }
 *   //必选，渲染图表函数
 *   render: function(chart){ ... },
 *   //可选，更新图表数据函数是否是异步函数，默认为false
 *   asyncUpdate: true、false、function(chart, results){ ...; return true 或者 false; }
 *   //必选，更新图表数据函数
 *   //results 要更新的图表数据
 *   update: function(chart, results){ ... },
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
 *   destroy: function(chart){ ... }
 * }
 * 
 * 此图表工厂和dashboardFactory.js一起可以支持异步图表插件，示例如下：
 * {
 *   asyncRender: true,
 *   
 *   render: function(chart)
 *   {
 *     $.get("...", function()
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
 *   update: function(chart, results)
 *   {
 *     $.get("...", function()
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
	var chartFactory = (global.chartFactory || (global.chartFactory = {}));
	
	/**图表对象基类*/
	var chartBase = (chartFactory.chartBase || (chartFactory.chartBase = {}));
	
	/**图表状态常量*/
	var chartStatusConst = (chartFactory.chartStatusConst || (chartFactory.chartStatusConst = {}));
	
	/**HTML元素属性常量*/
	var elementAttrConst = (chartFactory.elementAttrConst || (chartFactory.elementAttrConst = {}));
	
	/**
	 * 图表地图映射表。
	 * 地图类图表的地图名称与其地图数据地址映射表，用于为chartBase.mapURL函数提供支持。
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
	var chartMapURLs = (chartFactory.chartMapURLs || (chartFactory.chartMapURLs = {}));
	
	/** 渲染上下文属性名常量 */
	var renderContextAttrConst = (chartFactory.renderContextAttrConst || (chartFactory.renderContextAttrConst = {}));
	
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
	
	/**图表地图*/
	elementAttrConst.MAP = "dg-chart-map";
	
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
	
	/** 内置图表选项：是否美化滚动条 */
	chartFactory.OPTION_BEAUTIFY_SCROLLBAR = "beautifyScrollbar";
	
	/** 内置图表选项：处理图表渲染选项 */
	chartFactory.OPTION_PROCESS_RENDER_OPTIONS = "processRenderOptions";
	
	/** 内置图表选项：处理图表更新选项 */
	chartFactory.OPTION_PROCESS_UPDATE_OPTIONS = "processUpdateOptions";
	
	/** 图表标识样式名，所有已绘制的图表元素都会添加此样式名 */
	chartFactory.CHART_STYLE_NAME_FOR_INDICATION = "dg-chart-for-indication";
	
	/**
	 * 图表属性值集中图表选项名，同：org.datagear.management.domain.HtmlChartWidgetEntity.ATTR_CHART_OPTIONS
	 */
	chartFactory._CHART_ATTR_VALUE_NAME_OPTIONS = "DG_CHART_OPTIONS";
	
	/**
	 * 图表属性值集中图表部件名，同：org.datagear.analysis.support.ChartWidget.ATTR_CHART_WIDGET
	 */
	chartFactory._CHART_ATTR_VALUE_NAME_WIDGET = "DG_CHART_WIDGET";
	
	/**
	 * 初始化渲染上下文。
	 * 将webContext直接存入渲染上下文，复制chartTheme后使用<body>上的dg-chart-theme填充相关属性后存入渲染上下文，
	 * 之后可以通过:
	 * chartFactory.renderContextAttrWebContext(renderContext)
	 * chartFactory.renderContextAttrChartTheme(renderContext)
	 * 获取它们。
	 * 
	 * 注意：此函数应在初始化图表前（chart.init()函数调用前）且<body>后调用。
	 * 
	 * @param renderContext
	 * @param webContext Web上下文
	 * @param chartTheme 图表主题
	 */
	chartFactory.initRenderContext = function(renderContext, webContext, chartTheme)
	{
		if(!webContext)
			throw new Error("[webContext] required");
		if(!chartTheme)
			throw new Error("[chartTheme] required");
		
		if(chartFactory._themeInflated(chartTheme))
			throw new Error("[chartTheme] must not inflated");
		
		chartTheme = $.extend(true, {}, chartTheme);
		
		chartFactory._inflateGlobalChartTheme(chartTheme);
		
		chartFactory.renderContextAttrWebContext(renderContext, webContext);
		chartFactory.renderContextAttrChartTheme(renderContext, chartTheme);
	};
	
	/**
	 * 判断chartFactory.initRenderContext()函数是否已执行。
	 */
	chartFactory.isRenderContextInited = function(renderContext)
	{
		if(!renderContext)
			return false;
		
		var webContext = chartFactory.renderContextAttrWebContext(renderContext);
		var chartTheme = chartFactory.renderContextAttrChartTheme(renderContext);
		var inflated = chartFactory._themeInflated(chartTheme);
		
		return (webContext && chartTheme && inflated);
	};
	
	/**
	 * 初始化图表JSON对象，为其添加图表API，并设置chart.statusPreInit(true)状态，但不调用chart.init()函数。
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
	 *				  //可选，图表数据集数组
	 *				  chartDataSets: [...],
	 *				  //可选，更新间隔
	 *				  updateInterval: 数值,
	 *				  //可选，图表结果数据格式
	 *				  resultDataFormat: {...},
	 *				  //图表属性
	 *				  attrValues: {...}
	 *				}
	 *				
	 *				另参考：org.datagear.analysis.support.html.HtmlChart
	 */
	chartFactory.init = function(chart)
	{
		this._initChartBaseProperties(chart);
		$.extend(chart, this.chartBase);
		
		chart.statusPreInit(true);
	};
	
	/**
	 * 初始化图表对象基础属性。
	 */
	chartFactory._initChartBaseProperties = function(chart)
	{
		chart.name = (chart.name || "");
		chart.chartDataSets = (chart.chartDataSets || []);
		chart.updateInterval = (chart.updateInterval == null ? -1 : chart.updateInterval);
		for(var i=0; i<chart.chartDataSets.length; i++)
		{
			var cds = chart.chartDataSets[i];
			cds.propertySigns = (cds.propertySigns || {});
			cds.alias = (cds.alias == null ?  "" : cds.alias);
			cds.attachment = (cds.attachment == true ? true : false);
			cds.query = (cds.query || {});
			cds.query.paramValues = (cds.query.paramValues || {});
			//为chartDataSets元素添加index属性，便于后续根据其索引获取结果集等信息
			cds.index = i;
			
			// < @deprecated 兼容2.4.0版本的chartDataSet.paramValues，将在未来版本移除，已被chartDataSet.query.paramValues取代
			cds.paramValues = cds.query.paramValues;
			// > @deprecated 兼容2.4.0版本的chartDataSet.paramValues，将在未来版本移除，已被chartDataSet.query.paramValues取代
		}
		
		chart._attrValues = (chart.attrValues || {});
		
		//将内置属性值提取出来，避免被chart.attrValues()设置操作清除
		chart._widget = chart._attrValues[chartFactory._CHART_ATTR_VALUE_NAME_WIDGET];
		chart._optionsOrigin = chart._attrValues[chartFactory._CHART_ATTR_VALUE_NAME_OPTIONS];
		delete chart._attrValues[chartFactory._CHART_ATTR_VALUE_NAME_WIDGET];
		delete chart._attrValues[chartFactory._CHART_ATTR_VALUE_NAME_OPTIONS];
		
		//保留原始属性值集，看板可视编辑需要使用
		//注意，初始化_attrValuesOrigin的逻辑不能在chartBase.render()中执行，
		//因为chartBase.render()可以被多次调用，chart._attrValues可能已被修改
		chart._attrValuesOrigin = $.extend(true, {}, chart._attrValues);
		//chart.resultDataFormat属性与后面的chart.resultDataFormat()冲突，因此这里重构一下
		chart._resultDataFormat = chart.resultDataFormat;
		
		delete chart.attrValues;
		delete chart.resultDataFormat;
		
		if(chartFactory.chartPluginManager && chartFactory.chartPluginManager.get)
		{
			var pluginId = (chart.plugin ? chart.plugin.id : null);
			var plugin = (pluginId ? chartFactory.chartPluginManager.get(pluginId) : null);
			
			if(plugin)
				chart.plugin = plugin;
		}
	};
	
	//----------------------------------------
	// chartBase start
	//----------------------------------------
	
	/**
	 * 初始化图表，使用图表元素上的dg-*属性值初始化图表。
	 * 图表初始化后处于this.statusInited()状态。
	 * 此函数在图表生命周期内仅允许调用一次，在this.destroy()后允许再次调用。 
	 * 
	 * 注意：只有this.statusPreInit()或者this.statusDestroyed()为true，此函数才允许执行。
	 * 注意：初始化图表前应确保已调用chartFactory.initRenderContext(this.renderContext)。
	 * 注意：此函数内不应执行渲染相关逻辑，而应仅执行初始化图表属性的相关逻辑，因为chart.destroy()后可再次调用chart.init()。
	 * 
	 * 图表生命周期：
	 * chart.init() -->-- chart.render() -->-- chart.update() -->-- chart.destroy() -->--|
	 *       |                  |                    |           |                       |
	 *       |                  |                    |-----<-----|                       |
	 *       |                  |--------------------------<-----------------------------| 
	 *       |-----------------------------------<---------------------------------------| 
	 */
	chartBase.init = function()
	{
		if(!this.id)
			throw new Error("[chart.id] required");
		if(!this.elementId)
			throw new Error("[chart.elementId] required");
		if(!this.renderContext)
			throw new Error("[chart.renderContext] required");
		if(!this.plugin)
			throw new Error("[chart.plugin] required");
		
		if(this.element() == null)
			throw new Error("chart element '#"+this.elementId+"' required");
		
		if(!this.statusPreInit() && !this.statusDestroyed())
			throw new Error("chart is illegal state for init()");
		
		if(!this._isRenderContextInited())
			throw new Error("chart is illegal state for init()");
		
		this.statusIniting(true);
		
		this._initForPre();
		this._initOptions();
		this._initTheme();
		this._initListener();
		this._initMap();
		this._initEchartsThemeName();
		this._initDisableSetting();
		this._initEventHandlers();
		this._initRenderer();
		this._initAttrValues();
		this._initForPost();
		
		this.statusInited(true);
	};
	
	chartBase._isRenderContextInited = function()
	{
		return chartFactory.isRenderContextInited(this.renderContext);
	};
	
	chartBase._renderContextAttrChartTheme = function()
	{
		return chartFactory.renderContextAttrChartTheme(this.renderContext);
	};
	
	chartBase._renderContextAttrWebContext = function()
	{
		return chartFactory.renderContextAttrWebContext(this.renderContext);
	};
	
	/**
	 * 初始化图表选项。
	 * 此函数依次从<body>元素、图表元素的elementAttrConst.OPTIONS属性读取、合并图表选项。
	 */
	chartBase._initOptions = function()
	{
		var options = {};
		
		var optionsOrigin = this.optionsOrigin(true);
		if(optionsOrigin)
			options = $.extend(true, options, optionsOrigin);
		
		var $ele = this.elementJquery();
		
		var eleOptions = $ele.attr(elementAttrConst.OPTIONS);
		
		var bodyOptions = this._getBodyOptions();
		if(bodyOptions)
			options = $.extend(true, options, bodyOptions);
		
		if(eleOptions)
			options = $.extend(true, options, chartFactory.evalSilently(eleOptions, {}));
		
		this.options(options);
	};
	
	chartBase._getBodyOptions = function()
	{
		var bodyOptionsStr = $(document.body).attr(elementAttrConst.OPTIONS);
		
		if(bodyOptionsStr != chartFactory._PREV_BODY_OPTIONS_STR)
		{
			chartFactory._PREV_BODY_OPTIONS_STR = bodyOptionsStr;
			chartFactory._PREV_BODY_OPTIONS = chartFactory.evalSilently(bodyOptionsStr);
		}
		
		return chartFactory._PREV_BODY_OPTIONS;
	};
	
	/**
	 * 初始化图表主题。
	 * 此函数依次从this.renderContext中的renderContextAttrConst.inflatedChartTheme属性值、
	 * <body>元素、图表元素的elementAttrConst.THEME属性读取、合并图表主题。
	 * 
	 * @return {...}
	 */
	chartBase._initTheme = function()
	{
		var eleThemeValue = this.elementJquery().attr(elementAttrConst.THEME);
		
		if(eleThemeValue)
		{
			var eleTheme = chartFactory.evalSilently(eleThemeValue, {});
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
	chartBase._initListener = function()
	{
		var globalListener = this._getBodyListener();
		
		var localListener = this.elementJquery().attr(elementAttrConst.LISTENER);
		if(localListener)
			localListener = chartFactory.evalSilently(localListener);
		
		var myListener = null;
		
		if(!localListener && !globalListener)
		{
			myListener = null;
		}
		else if(!localListener)
		{
			myListener = globalListener;
		}
		else if(!globalListener)
		{
			myListener = localListener;
		}
		else
		{
			//实现局部图表监听器继承全局图表监听器功能
			myListener =
			{
				//标识这是一个由元素图表监听器属性生成的内部代理图表监听器
				_proxyChartListenerFromEleAttr: true,
				_listeners: [localListener, globalListener],
				render: function(chart)
				{
					var dl = this._findListenerOfFunc("render");
					
					if(dl)
						return dl.render(chart);
				},
				update: function(chart, results)
				{
					var dl = this._findListenerOfFunc("update");
					
					if(dl)
						return dl.update(chart, results);
				},
				destroy: function(chart)
				{
					var dl = this._findListenerOfFunc("destroy");
					
					if(dl)
						return dl.destroy(chart);
				},
				onRender: function(chart)
				{
					var dl = this._findListenerOfFunc("onRender");
					
					if(dl)
						return dl.onRender(chart);
				},
				onUpdate: function(chart, results)
				{
					var dl = this._findListenerOfFunc("onUpdate");
					
					if(dl)
						return dl.onUpdate(chart, results);
				},
				onDestroy: function(chart)
				{
					var dl = this._findListenerOfFunc("onDestroy");
					
					if(dl)
						return dl.onDestroy(chart);
				},
				_findListenerOfFunc: function(funcName)
				{
					for(var i=0; i<this._listeners.length; i++)
					{
						if(this._listeners[i] && this._listeners[i][funcName])
							return this._listeners[i];
					}
					
					return null;
				}
			};
		}
		
		this.listener(myListener);
	};
	
	chartBase._getBodyListener = function()
	{
		var bodyListenerStr = $(document.body).attr(elementAttrConst.LISTENER);
		
		if(bodyListenerStr != chartFactory._PREV_BODY_LISTENER_STR)
		{
			chartFactory._PREV_BODY_LISTENER_STR = bodyListenerStr;
			chartFactory._PREV_BODY_LISTENER = chartFactory.evalSilently(bodyListenerStr);
		}
		
		return chartFactory._PREV_BODY_LISTENER;
	};
	
	/**
	 * 初始化图表的地图名。
	 * 此函数从图表元素的elementAttrConst.MAP属性获取图表地图名。
	 */
	chartBase._initMap = function()
	{
		var map = this.elementJquery().attr(elementAttrConst.MAP);
		
		this.map(map);
	};
	
	/**
	 * 初始化图表的ECharts主题名。
	 * 此函数依次从图表元素、<body>元素的elementAttrConst.ECHARTS_THEME属性获取ECharts主题名。
	 */
	chartBase._initEchartsThemeName = function()
	{
		var themeName = this.elementJquery().attr(elementAttrConst.ECHARTS_THEME);
		if(!themeName)
			themeName = $(document.body).attr(elementAttrConst.ECHARTS_THEME);
		
		this.echartsThemeName(themeName);
	};
	
	/**
	 * 初始化图表是否禁用交互设置。
	 * 此函数从图表元素的elementAttrConst.DISABLE_SETTING属性获取是否禁用值。
	 */
	chartBase._initDisableSetting = function()
	{
		var globalSetting = $(document.body).attr(elementAttrConst.DISABLE_SETTING);
		var localSetting = this.elementJquery().attr(elementAttrConst.DISABLE_SETTING);
		
		globalSetting = this._evalDisableSettingAttr(globalSetting);
		
		if(localSetting != null && localSetting != "")
		{
			localSetting = this._evalDisableSettingAttr(localSetting);
			localSetting = $.extend({}, globalSetting, localSetting);
		}
		else
			localSetting = globalSetting;
		
		this.disableSetting(localSetting);
	};
	
	chartBase._evalDisableSettingAttr = function(settingAttr)
	{
		var setting = {};
		
		if(settingAttr == null || settingAttr == "")
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
		else
		{
			var tmpSetting = chartFactory.evalSilently(settingAttr, {});
			setting = $.extend(setting, tmpSetting);
		}
		
		return setting;
	};
	
	/**
	 * 初始化图表事件处理函数。
	 * 此函数从图表元素的所有以elementAttrConst.ON开头的属性获取事件处理函数。
	 * 例如：
	 * dg-chart-on-click="clickHandler" 						定义"click"事件处理函数；
	 * dg-chart-on-mouseover="function(chartEvent){ ... }"		定义"mouseover"事件处理函数。
	 */
	chartBase._initEventHandlers = function()
	{
		var dom = this.element();
		var attrs = dom.attributes;
		
		var ehs = [];
		
		var prefix = elementAttrConst.ON;
		
		for(var i=0; i<attrs.length; i++)
		{
			var an = attrs[i];
			
			if(an.nodeName.indexOf(prefix) == 0 && an.nodeName.length > prefix.length)
			{
				var eventType = an.nodeName.substr(prefix.length);
				var eventHandler = chartFactory.evalSilently(an.nodeValue);
				
				if(eventHandler)
					ehs.push({ eventType: eventType, eventHandler: eventHandler });
			}
		}
		
		this.eventHandlers(ehs);
	};
	
	/**
	 * 初始化自定义图表渲染器。
	 * 此函数从图表元素的elementAttrConst.RENDERER属性获取自定义图表渲染器。
	 */
	chartBase._initRenderer = function()
	{
		var renderer = this.elementJquery().attr(elementAttrConst.RENDERER);
		renderer = (renderer ? chartFactory.evalSilently(renderer) : null);
		
		this.renderer(renderer);
	};
	
	/**
	 * 初始化图表属性值集。
	 * 此函数从图表元素的elementAttrConst.ATTR_VALUES属性获取图表属性值集。
	 */
	chartBase._initAttrValues = function()
	{
		var attrValues = this.elementJquery().attr(elementAttrConst.ATTR_VALUES);
		attrValues = (attrValues ? chartFactory.evalSilently(attrValues) : null);
		//注意：应该使用this.attrValuesOrigin()作为合并基础，因为可能this.attrValues()执行修改操作，
		//比如修改后chart.destroy()后再chart.render()
		attrValues = $.extend(true, {}, this.attrValuesOrigin(), attrValues);
		
		this.attrValues(attrValues);
	};
	
	/**
	 * 初始化开始扩展函数，默认什么也不做，留作扩展使用。
	 */
	chartBase._initForPre = function(){};
	
	/**
	 * 初始化完成扩展函数，默认什么也不做，留作扩展使用。
	 */
	chartBase._initForPost = function(){};
	
	/**
	 * 获取/设置图表选项，这些选项通常用于控制图表展示、交互效果，格式为：{ ... }。
	 * 
	 * 图表初始化时会使用图表元素的"dg-chart-options"属性值执行设置操作。
	 * 
	 * 图表渲染器实现相关：
	 * 图表渲染器应使用此函数获取并应用图表选项，另参考chart.inflateRenderOptions()、chart.inflateUpdateOptions()。
	 * 
	 * @param options 可选，要设置的图表选项，没有则执行获取操作
	 */
	chartBase.options = function(options)
	{
		if(options === undefined)
			return this._options;
		else
			this._options = options;
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
	 */
	chartBase.theme = function(theme)
	{
		if(theme === undefined)
			return this._theme;
		else
		{
			if(theme == null)
				throw new Error("[theme] required");
			
			if(!this._isRenderContextInited())
				throw new Error("chart is illegal state for theme(theme)");
			
			var globalTheme = this._renderContextAttrChartTheme();
			
			//这里不应采用复制一个新图表主题对象的方式，因为图表主题对象后续会关联创建很多<style>元素，
			//如果采用复制方式的话，也会重复创建<style>元素，导致不必要的资源占用
			
			if(theme !== globalTheme && !chartFactory._themeInflated(theme))
			{
				chartFactory._inflateChartThemeIf(theme);
				
				var extTheme = $.extend(true, {}, globalTheme._RAW_CHART_THEME, theme);
				chartFactory._inflateChartThemeIf(extTheme);
				
				$.extend(theme, extTheme);
				
				chartFactory._themeInflated(theme, true);
			}
			
			this._theme = theme;
		}
	};
	
	/**
	 * 获取非空图表主题。
	 */
	chartBase._themeNonNull = function()
	{
		var theme = this.theme();
		
		if(theme == null)
			throw new Error("[chart.theme()] required");
		
		return theme;
	};
	
	/**
	 * 获取/设置图表监听器。
	 * 图表监听器格式为：
	 * {
	 *   //可选，渲染图表完成回调函数
	 *   render: function(chart){ ... },
	 *   //可选，更新图表数据完成回调函数
	 *   update: function(chart, results){ ... },
	 *   //可选，销毁图表完成回调函数
	 *   destroy: function(chart){ ... },
	 *   //可选，渲染图表前置回调函数，返回false将阻止渲染图表
	 *   onRender: function(chart){ ... },
	 *   //可选，更新图表数据前置回调函数，返回false将阻止更新图表数据
	 *   onUpdate: function(chart, results){ ... },
	 *   //可选，销毁图表前置回调函数，返回false将阻止销毁图表
	 *   onDestroy: function(chart){ ... }
	 * }
	 * 
	 * 图表初始化时会使用图表元素的"dg-chart-listener"属性值执行设置操作。
	 * 
	 * @param listener 可选，要设置的监听器对象，没有则执行获取操作
	 */
	chartBase.listener = function(listener)
	{
		if(listener === undefined)
			return this._listener;
		else
			this._listener = listener;
	};
	
	/**
	 * 获取/设置图表地图名。
	 * 此函数用于为地图类图表提供支持，如果不是地图类图表，则不必设置此项。
	 * 
	 * 图表初始化时会使用图表元素的"dg-chart-map"属性值执行设置操作。
	 * 
	 * 图表渲染器实现相关：
	 * 图表渲染器应使用此函数获取并应用图表地图。
	 * 
	 * @param map 可选，要设置的地图名，没有则执行获取操作
	 */
	chartBase.map = function(map)
	{
		if(map === undefined)
			return this._map;
		else
			this._map = map;
	};
	
	/**
	 * ECharts图表支持函数：获取/设置图表的ECharts主题名。
	 * 此函数用于为ECharts图表提供支持，如果不是ECharts图表，则不必设置此项。
	 * 
	 * 图表初始化时会使用图表元素的"dg-echarts-theme"属性值执行设置操作。
	 * 
	 * 图表渲染器实现相关：
	 * 图表渲染器应使用此函数获取并应用ECharts主题。
	 * 
	 * @param themeName 可选，要设置的且已注册的ECharts主题名，没有则执行获取操作
	 */
	chartBase.echartsThemeName = function(themeName)
	{
		if(themeName === undefined)
			return this._echartsThemeName;
		else
			this._echartsThemeName = themeName;
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
	chartBase.disableSetting = function(setting)
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
			return (this._disableSetting == null ? defaultSetting : this._disableSetting);
		}
		else
		{
			if(setting == true || setting == "true")
			{
				setting = {param: true, data: true};
			}
			else if(setting == false || setting == "false")
			{
				setting = {param: false, data: false};
			}
			
			this._disableSetting = $.extend(defaultSetting, setting);
		}
	};
	
	/**
	 * 获取/设置图表事件处理函数数组。
	 * 
	 * 图表初始化时会使用图表元素的"dg-chart-on-*"属性值执行设置操作。
	 * 
	 * 图表渲染器实现相关：
	 * 图表渲染器应实现on函数，以支持此特性。
	 * 
	 * @param eventHandlers 可选，要设置的初始事件处理函数数组，没有则执行获取操作。数组元素格式为：
	 * 						{ eventType: "...", eventHandler: function(chartEvent){ ... } }
	 */
	chartBase.eventHandlers = function(eventHandlers)
	{
		if(eventHandlers === undefined)
			return this._eventHandlers;
		else
			this._eventHandlers = eventHandlers;
	};
	
	/**
	 * 获取/设置自定义图表渲染器。
	 * 
	 * 图表初始化时会使用图表元素的"dg-chart-renderer"属性值执行设置操作。
	 * 
	 * @param renderer 可选，要设置的自定义图表渲染器，自定义图表渲染器允许仅定义要重写的图表插件渲染器函数
	 * @returns 要获取的自定义图表渲染器，没有则返回null
	 */
	chartBase.renderer = function(renderer)
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
	chartBase.resultDataFormat = function(resultDataFormat)
	{
		if(resultDataFormat === undefined)
			return this._resultDataFormat;
		else
			this._resultDataFormat = resultDataFormat;
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
	chartBase.render = function()
	{
		if(this.statusPreInit())
			this.init();
		
		if(!this.statusInited() && !this.statusPreRender() && !this.statusDestroyed())
			throw new Error("chart is illegal state for render()");
		
		if(chartFactory.renderedChart(this.elementJquery()) != null)
			throw new Error("element '#"+this.elementId+"' has been rendered as chart");
		
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
	 * 调用底层图表渲染器的render函数，执行渲染。
	 */
	chartBase.doRender = function()
	{
		if(!this.statusRendering())
			throw new Error("chart is illegal state for doRender()");
		
		var $element = this.elementJquery();
		var theme = this._themeNonNull();
		
		$element.addClass(chartFactory.CHART_STYLE_NAME_FOR_INDICATION);
		chartFactory.addThemeRefEntity(theme, this.id);
		this._createChartThemeCssIfNon();
		//如果图表元素不可作为相对定位的父元素，则设置，便于子元素在图表元素内处理定位
		if(chartFactory.isStaticPosition($element))
			$element.addClass(chartFactory._KEY_CHART_ELEMENT_STYLE_FOR_RELATIVE);
		$element.addClass(this.themeStyleName());
		
		var options = this.options();
		if(!options || options[chartFactory.OPTION_BEAUTIFY_SCROLLBAR] != false)
			$element.addClass("dg-chart-beautify-scrollbar");
		
		$element.data(chartFactory._KEY_ELEMENT_RENDERED_CHART, this);
		
		var async = this.isAsyncRender();
		
		var renderer = this.renderer();
		
		if(renderer && renderer.render)
		{
			renderer.render(this);
		}
		else
		{
			this.plugin.renderer.render(this);
		}
		
		if(!async)
			this.statusRendered(true);
	};
	
	chartBase._createChartThemeCssIfNon = function()
	{
		var theme = this._themeNonNull();
		var thumbBgColor = this.themeGradualColor(0.2);
		
		this.themeStyleSheet(chartFactory.builtinPropName("Chart"), function()
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
						"font-size": chartFactory.toCssFontSize(theme.fontSize)
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
						"width": "10px",
						"height": "10px"
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
						"border-radius": "4px",
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
					name: "."+chartFactory.CHART_STYLE_NAME_FOR_INDICATION,
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
	 * @param results 可选，图表数据集结果，如果不设置，将使用this.updateResults()的返回值
	 */
	chartBase.update = function(results)
	{
		if(!this.statusRendered() && !this.statusPreUpdate() && !this.statusUpdated())
			throw new Error("chart is illegal state for update()");
		
		if(arguments.length == 0)
			results = this.updateResults();
		
		if(results == null)
			throw new Error("[results] required");
		
		this.statusUpdating(true);
		
		var doUpdate = true;
		
		var listener = this.listener();
		if(listener && listener.onUpdate)
			doUpdate = listener.onUpdate(this, results);
		
		if(doUpdate != false)
		{
			this.doUpdate(results);
		}
	};
	
	/**
	 * 调用底层图表渲染器的update函数，执行更新数据。
	 */
	chartBase.doUpdate = function(results)
	{
		if(!this.statusUpdating())
			throw new Error("chart is illegal state for doUpdate()");
		
		//先保存结果，确保updateResults()在渲染器的update函数作用域内可用
		this.updateResults(results);
		
		var async = this.isAsyncUpdate(results);
		
		var renderer = this.renderer();
		
		if(renderer && renderer.update)
		{
			renderer.update(this, results);
		}
		else
		{
			this.plugin.renderer.update(this, results);
		}
		
		if(!async)
			this.statusUpdated(true);
	};
	
	/**
	 * 获取/设置图表此次更新的结果数据。
	 * 图表更新前会自动执行设置操作（通过chartBase.doUpdate()函数）。
	 * 
	 * @param results 可选，要设置的更新结果数据
	 * @returns 要获取的更新结果数据，没有则返回null
	 */
	chartBase.updateResults = function(results)
	{
		return chartFactory.extValueBuiltin(this, "updateResults", results);
	};
	
	/**
	 * 重新调整图表尺寸。
	 * 
	 * 图表渲染器实现相关：
	 * 图表渲染器应实现resize函数，以支持此特性。
	 */
	chartBase.resize = function()
	{
		this._assertActive();

		var renderer = this.renderer();
		
		if(renderer && renderer.resize)
		{
			renderer.resize(this);
		}
		else if(this.plugin.renderer.resize)
		{
			this.plugin.renderer.resize(this);
		}
		else
		{
			//为ECharts图表提供默认resize支持
			var internal = this.internal();
			if(this._isEchartsInstance(internal))
				internal.resize();
		}
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
	chartBase.destroy = function()
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
	chartBase.doDestroy = function()
	{
		if(!this.statusDestroying())
			throw new Error("chart is illegal state for doDestroy()");
		
		var $element = this.elementJquery();
		
		$element.removeClass(this.themeStyleName());
		$element.removeClass(chartFactory._KEY_CHART_ELEMENT_STYLE_FOR_RELATIVE);
		$element.removeClass("dg-chart-beautify-scrollbar");
		$element.removeClass(chartFactory.CHART_STYLE_NAME_FOR_INDICATION);
		$element.data(chartFactory._KEY_ELEMENT_RENDERED_CHART, null);
		
		//应在这里先销毁图表元素内部创建的元素，
		//因为renderer.destroy()可能会清空图表元素（比如echarts.dispose()函数）
		this._destroySetting();
		
		var theme = this._themeNonNull();
		chartFactory.removeThemeRefEntity(theme, this.id);
		
		var renderer = this.renderer();
		
		if(renderer && renderer.destroy)
		{
			renderer.destroy(this);
		}
		else if(this.plugin.renderer.destroy)
		{
			this.plugin.renderer.destroy(this);
		}
		else
		{
			//为ECharts图表提供默认destroy支持
			var internal = this.internal();
			if(this._isEchartsInstance(internal) && !internal.isDisposed())
			{
				internal.dispose();
			}
			
			this.elementJquery().empty();
		}
		
		this.internal(null);
		
		//最后清空扩展属性值，因为上面逻辑可能会使用到
		this._clearExtValue();
		
		this.statusDestroyed(true);
	};
	
	/**
	 * 销毁图表交互设置。
	 */
	chartBase._destroySetting = function()
	{
		if(chartFactory.chartSetting && chartFactory.chartSetting.unbindChartSettingPanelEvent)
			chartFactory.chartSetting.unbindChartSettingPanelEvent(this);
	};
	
	/**
	 * 图表的render函数是否是异步的。
	 */
	chartBase.isAsyncRender = function()
	{
		var renderer = this.renderer();
		
		if(renderer && renderer.asyncRender !== undefined)
		{
			if(typeof(renderer.asyncRender) == "function")
				return renderer.asyncRender(this);
			
			return (renderer.asyncRender == true);
		}
		
		if(this.plugin.renderer.asyncRender == undefined)
			return false;
		
		if(typeof(this.plugin.renderer.asyncRender) == "function")
			return this.plugin.renderer.asyncRender(this);
		
		return (this.plugin.renderer.asyncRender == true);
	};
	
	/**
	 * 图表的update函数是否是异步的。
	 * 
	 * @param results 图表数据集结果
	 */
	chartBase.isAsyncUpdate = function(results)
	{
		var renderer = this.renderer();
		
		if(renderer && renderer.asyncUpdate !== undefined)
		{
			if(typeof(renderer.asyncUpdate) == "function")
				return renderer.asyncUpdate(this, results);
			
			return (renderer.asyncUpdate == true);
		}
		
		if(this.plugin.renderer.asyncUpdate == undefined)
			return false;
		
		if(typeof(this.plugin.renderer.asyncUpdate) == "function")
			return this.plugin.renderer.asyncUpdate(this, results);
		
		return (this.plugin.renderer.asyncUpdate == true);
	};
	
	/**
	 * 图表是否处于活跃可用的状态（已完成渲染且未执行销毁）。
	 */
	chartBase.isActive = function()
	{
		return (this._isActive == true);
	};
	
	/**
	 * 断言图表处于活跃的状态。
	 */
	chartBase._assertActive = function()
	{
		if(this.isActive())
			return;
		
		throw new Error("chart not active");
	};
	
	/**
	 * 断言图表处于活着的状态。
	 */
	chartBase._assertAlive = function()
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
	chartBase.statusPreRender = function(set)
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
	chartBase.statusRendering = function(set)
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
	chartBase.statusRendered = function(set, postProcess)
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
	chartBase._postProcessRendered = function()
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
	chartBase._renderSetting = function()
	{
		var disableSetting = this.disableSetting();
		
		if(disableSetting.param && disableSetting.data)
			return;
		
		if(chartFactory.chartSetting && chartFactory.chartSetting.bindChartSettingPanelEvent)
			chartFactory.chartSetting.bindChartSettingPanelEvent(this);
	};
	
	/**
	 * 绑定初始图表事件处理函数。
	 */
	chartBase._bindEventHandlers = function()
	{
		var ehs = this.eventHandlers();
		
		for(var i=0; i<ehs.length; i++)
			this.on(ehs[i].eventType, ehs[i].eventHandler);
	};
	
	/**
	 * 图表是否为/设置为：准备update。
	 * 
	 * @param set 可选，为true时设置状态；否则，判断状态
	 */
	chartBase.statusPreUpdate = function(set)
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
	chartBase.statusUpdating = function(set)
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
	chartBase.statusUpdated = function(set, postProcess)
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
	chartBase._postProcessUpdated = function()
	{
		var listener = this.listener();
		if(listener && listener.update)
			listener.update(this, this.updateResults());
	};
	
	/**
	 * 图表是否为/设置为：正在销毁。
	 * 
	 * @param set 可选，为true时设置状态；否则，判断状态
	 */
	chartBase.statusDestroying = function(set)
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
	chartBase.statusDestroyed = function(set, postProcess)
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
	
	chartBase._postProcessDestroyed = function()
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
	chartBase.status = function(status)
	{
		if(status === undefined)
			return (this._status || "");
		else
			this._status = status;
	};
	
	/**
	 * 绑定"click"事件处理函数。
	 * 
	 * 图表渲染器实现相关：
	 * 图表渲染器应实现on函数，以支持此特性。
	 * 
	 * @param handler 事件处理函数：function(chartEvent){}
	 */
	chartBase.onClick = function(handler)
	{
		this.on("click", handler);
	};
	
	/**
	 * 绑定"dblclick"事件处理函数。
	 * 
	 * 图表渲染器实现相关：
	 * 图表渲染器应实现on函数，以支持此特性。
	 * 
	 * @param handler 事件处理函数：function(chartEvent){}
	 */
	chartBase.onDblclick = function(handler)
	{
		this.on("dblclick", handler);
	};
	
	/**
	 * 绑定"mousedown"事件处理函数。
	 * 
	 * 图表渲染器实现相关：
	 * 图表渲染器应实现on函数，以支持此特性。
	 * 
	 * @param handler 事件处理函数：function(chartEvent){}
	 */
	chartBase.onMousedown = function(handler)
	{
		this.on("mousedown", handler);
	};
	
	/**
	 * 绑定"mouseup"事件处理函数。
	 * 
	 * 图表渲染器实现相关：
	 * 图表渲染器应实现on函数，以支持此特性。
	 * 
	 * @param handler 事件处理函数：function(chartEvent){}
	 */
	chartBase.onMouseup = function(handler)
	{
		this.on("mouseup", handler);
	};
	
	/**
	 * 绑定"mouseover"事件处理函数。
	 * 
	 * 图表渲染器实现相关：
	 * 图表渲染器应实现on函数，以支持此特性。
	 * 
	 * @param handler 事件处理函数：function(chartEvent){}
	 */
	chartBase.onMouseover = function(handler)
	{
		this.on("mouseover", handler);
	};
	
	/**
	 * 绑定"mouseout"事件处理函数。
	 * 
	 * 图表渲染器实现相关：
	 * 图表渲染器应实现on函数，以支持此特性。
	 * 
	 * @param handler 事件处理函数：function(chartEvent){}
	 */
	chartBase.onMouseout = function(handler)
	{
		this.on("mouseout", handler);
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
	chartBase.on = function(eventType, handler)
	{
		this._assertActive();
		
		var renderer = this.renderer();
		
		if(renderer && renderer.on)
		{
			renderer.on(this, eventType, handler);
		}
		else if(this.plugin.renderer.on)
		{
			this.plugin.renderer.on(this, eventType, handler);
		}
		else
			throw new Error("chart '#"+this.elementId+"' [renderer.on] undefined");
	};
	
	/**
	 * 解绑事件处理函数。
	 * 注意：此函数在图表渲染完成后才可调用。
	 * 
	 * 图表渲染器实现相关：
	 * 图表渲染器应实现off函数，以支持此特性。
	 * 
	 * @param eventType 事件类型：click、dblclick、mousedown、mouseup、mouseover、mouseout
	 * @param handler 可选，解绑的事件处理函数，不设置则解绑所有此事件类型的处理函数
	 */
	chartBase.off = function(eventType, handler)
	{
		this._assertAlive();
		
		var renderer = this.renderer();
		
		if(renderer && renderer.off)
		{
			renderer.off(this, eventType, handler);
		}
		else if(this.plugin.renderer.off)
		{
			this.plugin.renderer.off(this, eventType, handler);
		}
		//为ECharts图表提供默认off支持
		else if(this._isEchartsInstance(this.internal()))
		{
			this.echartsOffEventHandler(eventType, handler);
		}
		else
			throw new Error("chart '#"+this.elementId+"' [renderer.off] undefined");
	};
	
	/**
	 * 判断图表的所有数据集参数值是否准备就绪，即：所有必填参数值都不为null。
	 * 
	 * @param msg 可选，格式应为：{}, 当校验参数值未齐备时用于写入必填参数信息：{ chartDataSetIndex: 数值, paramName: 参数名 }
	 */
	chartBase.isDataSetParamValueReady = function(msg)
	{
		var chartDataSets = (this.chartDataSets || []);
		
		for(var i=0; i<chartDataSets.length; i++)
		{
			var dataSet = chartDataSets[i].dataSet;
			
			if(!dataSet.params || dataSet.params.length == 0)
				continue;
			
			var paramValues = chartDataSets[i].query.paramValues;
			
			for(var j=0; j<dataSet.params.length; j++)
			{
				var dsp = dataSet.params[j];
				
				if((dsp.required == true || dsp.required == "true") && paramValues[dsp.name] == null)
				{
					if(msg != null)
					{
						msg.chartDataSetIndex = i;
						msg.paramName = dsp.name;
					}
					
					return false;
				}
			}
		}
		
		return true;
	};
	
	chartBase._chartDataSetOf = function(chartDataSet, nullable)
	{
		nullable = (nullable == null ? false : nullable);
		
		var re = (chartFactory.isNumber(chartDataSet) ? this.chartDataSetAt(chartDataSet) : chartDataSet);
		
		if(!nullable && re == null)
			throw new Error("chart data set not found for : " + chartDataSet);
		
		return re;
	};
	
	/**
	 * 获取/设置指定第一个数据集单个参数值。
	 * 
	 * @param name 参数名、参数索引
	 * @param value 可选，要设置的参数值，不设置则执行获取操作
	 */
	chartBase.dataSetParamValueFirst = function(name, value)
	{
		return this.dataSetParamValue(0, name, value);
	};
	
	/**
	 * 获取/设置指定数据集单个参数值。
	 * 
	 * @param chartDataSet 指定图表数据集对象、图表数据集索引
	 * @param name 参数名、参数索引
	 * @param value 可选，要设置的参数值，不设置则执行获取操作
	 */
	chartBase.dataSetParamValue = function(chartDataSet, name, value)
	{
		chartDataSet = this._chartDataSetOf(chartDataSet);
		
		//参数索引
		if(typeof(name) == "number")
		{
			var dataSet = chartDataSet.dataSet;
			
			if(!dataSet.params || dataSet.params.length <= name)
				throw new Error("no data set param defined at index : "+name);
			
			name = dataSet.params[name].name;
		}
		
		var paramValues = chartDataSet.query.paramValues;
		
		if(chartDataSet._originalParamValues == null)
			chartDataSet._originalParamValues = $.extend({}, paramValues);
		
		if(value === undefined)
			return paramValues[name];
		else
			paramValues[name] = value;
	};
	
	/**
	 * 获取/设置第一个数据集参数值集。
	 * 
	 * @param paramValues 可选，要设置的参数名/值集对象，或者是与数据集参数数组元素一一对应的参数值数组，不设置则执行获取操作
	 */
	chartBase.dataSetParamValuesFirst = function(paramValues)
	{
		return this.dataSetParamValues(0, paramValues);
	};
	
	/**
	 * 获取/设置指定数据集参数值集。
	 * 
	 * @param chartDataSet 指定图表数据集或其索引
	 * @param paramValues 可选，要设置的参数值集对象，或者是与数据集参数数组元素一一对应的参数值数组，不设置则执行获取操作
	 */
	chartBase.dataSetParamValues = function(chartDataSet, paramValues)
	{
		chartDataSet = this._chartDataSetOf(chartDataSet);
		
		var paramValuesCurrent = chartDataSet.query.paramValues;
		
		if(chartDataSet._originalParamValues == null)
			chartDataSet._originalParamValues = $.extend({}, paramValuesCurrent);
		
		if(paramValues === undefined)
			return paramValuesCurrent;
		else
		{
			if($.isArray(paramValues))
			{
				var params = (chartDataSet.dataSet.params || []);
				var len = Math.min(params.length, paramValues.length);
				var paramValuesObj = {};
				
				for(var i=0; i<len; i++)
				{
					var name = params[i].name;
					paramValuesObj[name] = paramValues[i];
				}
				
				paramValues = paramValuesObj;
			}
			
			chartDataSet.query.paramValues = paramValues;
			
			// < @deprecated 兼容2.4.0版本的chartDataSet.paramValues，将在未来版本移除，已被chartDataSet.query.paramValues取代
			chartDataSet.paramValues = chartDataSet.query.paramValues;
			// > @deprecated 兼容2.4.0版本的chartDataSet.paramValues，将在未来版本移除，已被chartDataSet.query.paramValues取代
		}
	};
	
	/**
	 * 重置第一个数据集参数值集。
	 */
	chartBase.resetDataSetParamValuesFirst = function()
	{
		return this.resetDataSetParamValues(0);
	};
	
	/**
	 * 重置指定数据集参数值集。
	 * 
	 * @param chartDataSet 指定图表数据集或其索引
	 */
	chartBase.resetDataSetParamValues = function(chartDataSet)
	{
		chartDataSet = this._chartDataSetOf(chartDataSet);
		
		if(chartDataSet._originalParamValues == null)
			return;
		
		chartDataSet.query.paramValues = $.extend({}, chartDataSet._originalParamValues);
		
		// < @deprecated 兼容2.4.0版本的chartDataSet.paramValues，将在未来版本移除，已被chartDataSet.query.paramValues取代
		chartDataSet.paramValues = chartDataSet.query.paramValues;
		// > @deprecated 兼容2.4.0版本的chartDataSet.paramValues，将在未来版本移除，已被chartDataSet.query.paramValues取代
	};
	
	/**
	 * 获取渲染此图表的图表部件ID。
	 * 正常来说，此函数的返回值与期望渲染的图表部件ID相同（通常是chartBase.elementWidgetId()的返回值），
	 * 当不同时，表明服务端因加载图表异常（未找到或出现错误）而使用了一个备用图表，用于在页面展示异常信息。
	 */
	chartBase.widgetId = function()
	{
		var chartWidget = this._widget;
		return (chartWidget ? chartWidget.id : null);
	};
	
	/**
	 * 获取图表HTML元素。
	 */
	chartBase.element = function()
	{
		return document.getElementById(this.elementId);
	};
	
	/**
	 * 获取图表HTML元素的Jquery对象。
	 */
	chartBase.elementJquery = function()
	{
		return $("#" + this.elementId);
	};
	
	/**
	 * 获取图表HTML元素上的图表部件ID（"dg-chart-widget"属性值）。
	 * 如果图表HTML元素上未设置过图表部件ID，将返回null。
	 */
	chartBase.elementWidgetId = function()
	{
		return chartFactory.elementWidgetId(this.element());
	};
	
	/**
	 * 判断此图表是否由指定ID的图表部件渲染。
	 * 
	 * @param chartWidgetId 图表部件ID，通常是图表元素的"dg-chart-widget"值
	 */
	chartBase.isInstance = function(chartWidgetId)
	{
		return (this.widgetId() == chartWidgetId);
	};
	
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
	chartBase.internal = function(internal)
	{
		return chartFactory.extValueBuiltin(this, "internal", internal);
	};
	
	/**
	 * 获取/设置图表渲染上下文的属性值。
	 * 
	 * @param attrName
	 * @param attrValue 要设置的属性值，可选，不设置则执行获取操作
	 */
	chartBase.renderContextAttr = function(attrName, attrValue)
	{
		return chartFactory.renderContextAttr(this.renderContext, attrName, attrValue);
	};
	
	/**
	 * 获取/设置扩展属性值。
	 * 所有扩展属性值都将在图表销毁后被清除。
	 * 
	 * @param name 扩展属性名
	 * @param value 要设置的扩展属性值，可选，不设置则执行获取操作
	 */
	chartBase.extValue = function(name, value)
	{
		if(value === undefined)
			return (this._extValues ? this._extValues[name] : undefined);
		else
		{
			if(!this._extValues)
				this._extValues = {};
			
			this._extValues[name] = value;
		}
	};
	
	chartBase._clearExtValue = function()
	{
		this._extValues = {};
	};
	
	/**
	 * 获取主件图表数据集对象数组，它们的用途是绘制图表。
	 * 
	 * @return []，空数组表示没有主件图表数据集
	 */
	chartBase.chartDataSetsMain = function()
	{
		var re = [];
		
		var chartDataSets = this.chartDataSets;
		for(var i=0; i<chartDataSets.length; i++)
		{
			if(chartDataSets[i].attachment)
				continue;
			
			re.push(chartDataSets[i]);
		}
		
		return re;
	};
	
	/**
	 * 获取附件图表数据集对象数组，它们的用途不是绘制图表。
	 * 
	 * @return []，空数组表示没有附件图表数据集
	 */
	chartBase.chartDataSetsAttachment = function()
	{
		var re = [];
		
		var chartDataSets = this.chartDataSets;
		for(var i=0; i<chartDataSets.length; i++)
		{
			if(chartDataSets[i].attachment)
			{
				re.push(chartDataSets[i]);
			}
		}
		
		return re;
	};
	
	/**
	 * 获取指定索引的图表数据集对象，没有则返回undefined。
	 * 
	 * @param index
	 */
	chartBase.chartDataSetAt = function(index)
	{
		return (!this.chartDataSets || this.chartDataSets.length <= index ? undefined : this.chartDataSets[index]);
	};
	
	/**
	 * 获取第一个主件图表数据集对象。
	 * 主件图表数据集的用途是绘制图表。
	 * 
	 * @return 未找到时返回null
	 * @since 3.0.0
	 */
	chartBase.chartDataSetMain = function()
	{
		var re = undefined;
		
		var chartDataSets = this.chartDataSets;
		for(var i=0; i<chartDataSets.length; i++)
		{
			if(!chartDataSets[i].attachment)
			{
				re = chartDataSets[i];
				break;
			}
		}
		
		return re;
	};
	
	/**
	 * 获取第一个附件图表数据集对象。
	 * 附件图表数据集的用途不是绘制图表。
	 * 
	 * @return 未找到时返回null
	 * @since 3.0.0
	 */
	chartBase.chartDataSetAttachment = function()
	{
		var re = undefined;
		
		var chartDataSets = this.chartDataSets;
		for(var i=0; i<chartDataSets.length; i++)
		{
			if(chartDataSets[i].attachment)
			{
				re = chartDataSets[i];
				break;
			}
		}
		
		return re;
	};
	
	/**
	 * 获取指定标记的数据集属性，没有则返回undefined。
	 * 
	 * @param chartDataSet 图表数据集、索引
	 * @param dataSign 数据标记对象、标记名称
	 * @param nonEmpty 可选，参考chartBase.dataSetPropertiesOfSign的nonEmpty参数
	 * @return {...}、undefined
	 */
	chartBase.dataSetPropertyOfSign = function(chartDataSet, dataSign, nonEmpty)
	{
		var properties = this.dataSetPropertiesOfSign(chartDataSet, dataSign, false, nonEmpty);
		return (properties.length > 0 ? properties[0] : undefined);
	};
	
	/**
	 * 获取指定标记的数据集属性数组。
	 * 
	 * @param chartDataSet 图表数据集、索引
	 * @param dataSign 数据标记对象、标记名称
	 * @param sort 可选，是否对返回结果进行重排序，true 是；false 否。默认值为：true
	 * @param nonEmpty 可选（设置时需指定sort参数），是否要求返回数组非空并且在为空时抛出异常，
	 * 					   "auto" 依据dataSign的required判断，为true则要求非空，否则不要求；
	 * 					   true 要求非空；false 不要求非空。默认为："auto"。
	 * @return [...]
	 */
	chartBase.dataSetPropertiesOfSign = function(chartDataSet, dataSign, sort, nonEmpty)
	{
		chartDataSet = this._chartDataSetOf(chartDataSet);
		sort = (sort === undefined ? true : sort);
		nonEmpty = (nonEmpty == null ? "auto" : nonEmpty);
		
		var re = [];
		
		if(dataSign == null)
			return re;
		
		dataSetProperties = this.dataSetProperties(chartDataSet, sort);
		var dataSignName = (chartFactory.isString(dataSign) ? dataSign : dataSign.name);
		var propertySigns = (chartDataSet.propertySigns || {});
		
		var signPropertyNames = [];
		
		for(var pname in propertySigns)
		{
			var mySigns = (propertySigns[pname] || []);
			
			for(var i=0; i<mySigns.length; i++)
			{
				if(mySigns[i] == dataSignName)
				{
					signPropertyNames.push(pname);
					break;
				}
			}
		}
		
		for(var i=0; i<dataSetProperties.length; i++)
		{
			for(var j=0; j<signPropertyNames.length; j++)
			{
				if(dataSetProperties[i].name == signPropertyNames[j])
					re.push(dataSetProperties[i]);
			}
		}
		
		if(nonEmpty == "auto")
		{
			var dataSignObj = (chartFactory.isString(dataSign) ? this._dataSignOfName(dataSign) : dataSign);
			nonEmpty = (dataSignObj ? dataSignObj.required : false);
		}
		
		if(nonEmpty && re.length == 0)
			throw new Error("data set property with '"+dataSignName+"' sign required");
		
		return re;
	};
	
	chartBase._dataSignOfName = function(dataSignName)
	{
		var dataSigns = (this.plugin && this.plugin.dataSigns ? this.plugin.dataSigns : []);
		
		for(var i=0; i<dataSigns.length; i++)
		{
			if(dataSigns[i] && dataSigns[i].name == dataSignName)
				return dataSigns[i];
		}
		
		return undefined;
	};
	
	/**
	 * 返回指定索引的数据集结果，没有则返回undefined。
	 * 
	 * @param results
	 * @param index
	 */
	chartBase.resultAt = function(results, index)
	{
		return (!results || results.length <= index ? undefined : results[index]);
	};
	
	/**
	 * 返回指定图表数据集对应的数据集结果，没有则返回undefined。
	 * 
	 * @param results
	 * @param chartDataSet
	 */
	chartBase.resultOf = function(results, chartDataSet)
	{
		return this.resultAt(results, chartDataSet.index);
	};
	
	/**
	 * 获取/设置数据集结果对象包含的数据。
	 * 
	 * @param result 数据集结果对象
	 * @param data 可选，要设置的数据，通常是：{ ... }、[ { ... }, ... ]，不设置则执行获取操作
	 * @return 要获取的数据集结果数据，没有则返回null
	 */
	chartBase.resultData = function(result, data)
	{
		if(data === undefined)
			return (result ? result.data : undefined);
		else
			result.data = data;
	};
	
	/**
	 * 获取/设置指定图表数据集对应的数据集结果对象包含的数据。
	 * 
	 * @param results
	 * @param chartDataSet
	 * @param data 可选，要设置的数据，通常是：{ ... }、[ { ... }, ... ]，不设置则执行获取操作
	 * @return 要获取的数据集结果数据，没有则返回null
	 * @since 3.0.0
	 */
	chartBase.resultDataOf = function(results, chartDataSet, data)
	{
		var result = this.resultOf(results, chartDataSet);
		return this.resultData(result, data);
	};
	
	/**
	 * 获取数据集结果的数据对象数组。
	 * 如果数据对象是null，返回空数组：[]；如果数据对象是数组，则直接返回；否则，返回：[ 数据对象 ]。
	 * 
	 * @param result 数据集结果对象
	 * @return 不会为null的数组
	 */
	chartBase.resultDatas = function(result)
	{
		if(result == null || result.data == null)
			return [];
		
		if($.isArray(result.data))
			return result.data;
		
		return [ result.data ];
	};
	
	/**
	 * 获取指定图表数据集对应的数据集结果对象包含的数据对象数组。
	 * 
	 * @param results
	 * @param chartDataSet
	 * @return 不会为null的数组
	 * @since 3.0.0
	 */
	chartBase.resultDatasOf = function(results, chartDataSet)
	{
		var result = this.resultOf(results, chartDataSet);
		return this.resultDatas(result);
	};
	
	/**
	 * 获取数据集结果数据的行对象指定属性值。
	 * 
	 * @param rowObj 行对象
	 * @param property 属性对象、属性名
	 */
	chartBase.resultRowCell = function(rowObj, property)
	{
		if(!rowObj || !property)
			return undefined;
		
		var name = (property.name || property);
		return rowObj[name];
	};
	
	/**
	 * 将数据集结果数据的行对象按照指定properties顺序转换为行值数组。
	 * 
	 * @param result 数据集结果对象
	 * @param properties 数据集属性对象数组、属性名数组、属性对象、属性名
	 * @param row 可选，行索引，默认为0
	 * @param count 可选，获取的最多行数，默认为全部
	 * @return properties为数组时：[[..., ...], ...]；properties非数组时：[..., ...]
	 */
	chartBase.resultRowArrays = function(result, properties, row, count)
	{
		var re = [];
		
		if(!result || !properties)
			return re;
		
		var datas = this.resultDatas(result);
		
		row = (row || 0);
		var getCount = datas.length;
		if(count != null && count < getCount)
			getCount = count;
		
		if($.isArray(properties))
		{
			for(var i=row; i< getCount; i++)
			{
				var rowObj = datas[i];
				var rowVal = [];
				
				for(var j=0; j<properties.length; j++)
				{
					var p = properties[j];
					
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
			var name = (properties ? (properties.name || properties) : undefined);
			
			if(name)
			{
				for(var i=row; i< getCount; i++)
				{
					var rowObj = datas[i];
					re.push(rowObj[name]);
				}
			}
		}
		
		return re;
	};
	
	/**
	 * 将数据集结果数据的行对象按照指定properties顺序转换为列值数组。
	 * 
	 * @param result 数据集结果对象
	 * @param properties 数据集属性对象数组、属性名数组、属性对象、属性名
	 * @param row 行索引，以0开始，可选，默认为0
	 * @param count 获取的最多行数，可选，默认为全部
	 * @return properties为数组时：[[..., ...], ...]；properties非数组时：[..., ...]
	 */
	chartBase.resultColumnArrays = function(result, properties, row, count)
	{
		var re = [];

		if(!result || !properties)
			return re;
		
		var datas = this.resultDatas(result);
		
		row = (row || 0);
		var getCount = datas.length;
		if(count != null && count < getCount)
			getCount = count;
		
		if($.isArray(properties))
		{
			for(var i=0; i<properties.length; i++)
			{
				var p = properties[i];
				
				var name = (p ? (p.name || p) : undefined);
				if(!name)
					continue;
				
				var column = [];
				
				for(var j=row; j< getCount; j++)
					column.push(datas[j][name]);
				
				re[i] = column;
			}
		}
		else
		{
			var name = (properties ? (properties.name || properties) : undefined);

			if(name)
			{
				for(var i=row; i< getCount; i++)
				{
					var rowObj = datas[i];
					re.push(rowObj[name]);
				}
			}
		}
		
		return re;
	};
	
	/**
	 * 获取数据集结果数据的名称/值对象数组。
	 * 
	 * @param result 数据集结果对象、对象数组
	 * @param nameProperty 名称属性对象、属性名
	 * @param valueProperty 值属性对象、属性名、数组
	 * @param row 可选，行索引，以0开始，默认为0
	 * @param count 可选，获取结果数据的最多行数，默认为全部
	 * @return [{name: ..., value: ...}, ...]
	 */
	chartBase.resultNameValueObjects = function(result, nameProperty, valueProperty, row, count)
	{
		var propertyMap ={ "name": nameProperty, "value": valueProperty };
		return this.resultMapObjects(result, propertyMap, row, count);
	};
	
	/**
	 * 获取数据集结果数据的值对象数组。
	 * 
	 * @param result 数据集结果对象、对象数组
	 * @param valueProperty 值属性对象、属性名、数组
	 * @param row 可选，行索引，以0开始，默认为0
	 * @param count 可选，获取结果数据的最多行数，默认为全部
	 * @return [{value: ...}, ...]
	 */
	chartBase.resultValueObjects = function(result, valueProperty, row, count)
	{
		var propertyMap ={ "value": valueProperty };
		return this.resultMapObjects(result, propertyMap, row, count);
	};
	
	/**
	 * 获取数据集结果数据指定属性、指定行的单元格值，没有则返回undefined。
	 * 
	 * @param result 数据集结果对象
	 * @param property 数据集属性对象、属性名
	 * @param row 行索引，可选，默认为0
	 */
	chartBase.resultCell = function(result, property, row)
	{
		row = (row || 0);
		
		var re = this.resultRowArrays(result, property, row, 1);
		
		return (re.length > 0 ? re[0] : undefined);
	};
	
	/**
	 * 获取指定地图名对应的地图数据地址。
	 * 此函数先从chartFactory.chartMapURLs查找对应的地址，如果没有，则直接返回name作为地址。
	 * 
	 * @param name 地图名称
	 */
	chartBase.mapURL = function(name)
	{
		if(!this._isRenderContextInited())
			throw new Error("chart is illegal state for mapURL(name)");
		
		var url = chartMapURLs[name];
		
		if(!url && typeof(chartMapURLs.mapURL) == "function")
			url = chartMapURLs.mapURL(name);
		
		url = (url || name);
		
		var webContext = this._renderContextAttrWebContext();
		url = chartFactory.toWebContextPathURL(webContext, url);
		
		return url;
	};
	
	/**
	 * 加载指定名称的地图资源（通常是*.json、*.svg）。
	 * 注意：如果地图类图表插件的render/update函数中调用此函数，应该首先设置插件的asyncRender/asyncUpdate为true，
	 * 并在callback中调用chart.statusRendered(true)/chart.statusUpdated(true)，具体参考此文件顶部的注释。
	 * 
	 * @param name 地图名称
	 * @param callback 可选，加载成功回调函数，格式为：function(name, map, jqXHR){ ... }，或者也可以是JQuery的ajax配置项：{...}
	 */
	chartBase.loadMap = function(name, callback)
	{
		if(!name)
			throw new Error("[name] required");
		
		var url = this.mapURL(name);
		
		var thisChart = this;
		
		var settings =
		{
			url: url
		};
		
		if(callback == null)
			;
		else if($.isFunction(callback))
		{
			settings.success = function(map, textStatus, jqXHR)
			{
				callback.call(thisChart, name, map, jqXHR);
			}
		}
		else
		{
			settings = $.extend(settings, callback);
		}
		
		$.ajax(settings);
	};
	
	/**
	 * ECharts图表支持函数：将图表初始化为ECharts图表，设置其选项。
	 * 此函数会自动应用chartBase.echartsGetThemeName()至初始化的ECharts图表。
	 * 此函数会自动调用chartBase.internal()将初始化的ECharts实例对象设置为图表底层组件。
	 * 
	 * @param options 要设置的ECharts选项
	 * @param opts 可选，ECharts的init函数附加参数，具体参考ECharts.init()函数的opts参数
	 * @returns ECharts实例对象
	 */
	chartBase.echartsInit = function(options, opts)
	{
		var instance = echarts.init(this.element(), this.echartsGetThemeName(), opts);
		instance.setOption(options);
		
		this.internal(instance);
		
		return instance;
	};
	
	/**
	 * ECharts图表支持函数：设置图表的ECharts实例的选项。
	 * 
	 * @param options
	 * @param opts 可选，ECharts的setOption函数附加参数，具体参考ECharts.setOption()函数的opts参数
	 */
	chartBase.echartsOptions = function(options, opts)
	{
		var internal = this.internal();
		
		if(!this._isEchartsInstance(internal))
			throw new Error("chart not ECharts");
		
		internal.setOption(options, opts);
	};
	
	/**
	 * 给定对象是否是ECharts实例。
	 */
	chartBase._isEchartsInstance = function(obj)
	{
		return (obj && obj.setOption && obj.isDisposed && obj.dispose && obj.off);
	};
	
	/**
	 * ECharts图表支持函数：判断指定名称的ECharts地图是否已经注册过而无需再加载。
	 * 
	 * @param name ECharts地图名称
	 */
	chartBase.echartsMapRegistered = function(name)
	{
		return (echarts.getMap(name) != null);
	};
	
	/**
	 * ECharts图表支持函数：加载并注册指定名称的ECharts地图（GeoJSON、SVG），并在注册完成后执行回调函数。
	 * 注意：如果地图图表插件的render/update函数中调用此函数，应该首先设置插件的asyncRender/asyncUpdate，
	 * 并在callback中调用chart.statusRendered(true)/chart.statusUpdated(true)，具体参考此文件顶部的注释。
	 * 
	 * @param name 地图名称
	 * @param callback 可选，加载并注册完成后的回调函数，格式为：function(name, map, jqXHR){ ... }，或者也可以是JQuery的ajax配置项：{...}
	 */
	chartBase.echartsLoadMap = function(name, callback)
	{
		var registerMap = function(name, map, jqXHR)
		{
			var contentType = (jqXHR.getResponseHeader("Content-Type") || "");
			
			//SVG地图
			if(/svg/i.test(contentType))
			{
				echarts.registerMap(name, {svg: map});
			}
			//其他都认为是GeoJSON地图
			else
			{
				echarts.registerMap(name, {geoJSON: map});
			}
		};
		
		if(callback == null)
			;
		else if($.isFunction(callback))
		{
			var originalCallback = callback;
			callback = function(name, map, jqXHR)
			{
				registerMap(name, map, jqXHR);
				originalCallback.call(this, name, map, jqXHR);
			};
		}
		//ajax配置项：{...}
		else
		{
			var settings = $.extend({}, callback);
			var originalCallback = settings.success;
			settings.success = function(map, textStatus, jqXHR)
			{
				registerMap(name, map, jqXHR);
				
				if(originalCallback)
					originalCallback.call(this, map, textStatus, jqXHR);
			};
			
			callback = settings;
		}
		
		this.loadMap(name, callback);
	};
	
	/**
	 * 图表事件支持函数：获取/设置图表事件的数据（chartEvent.data）。
	 * 
	 * 对于图表插件关联的图表渲染器，构建的图表事件数据应该以数据标记作为数据属性：
	 * { 数据标记名 : 数据值, ... }
	 * 使得图表事件数据的格式是固定的，便于事件处理函数读取。
	 * 
	 * @param chartEvent 图表事件对象，格式应为：{ ... }
	 * @param data 可选，要设置的数据，通常是绘制图表条目的数据，或由其转换而得，格式应为：
	 *             { ... }、[ { ... }, ... ]
	 * @returns 要获取的图表事件数据，未设置则返回null
	 */
	chartBase.eventData = function(chartEvent, data)
	{
		if(data === undefined)
			return chartEvent["data"];
		else
			chartEvent["data"] = data;
	};
	
	/**
	 * 图表事件支持函数：获取/设置图表事件数据（chartBase.eventData(chartEvent)返回值）对应的原始数据集结果数据（chartEvent.originalData）。
	 * 
	 * @param chartEvent 图表事件对象，格式应为：{ ... }
	 * @param originalData 要设置的原始数据集结果数据，通常与chartBase.eventOriginalDataIndex(chartEvent)返回值结构一致，
	 *                     只是每一个数据集结果数据索引数值对应一个数据集结果数据对象
	 * @returns 要获取的原始数据，未设置则返回null
	 */
	chartBase.eventOriginalData = function(chartEvent, originalData)
	{
		if(originalData === undefined)
			return chartEvent["originalData"];
		else
			chartEvent["originalData"] = originalData;
	};
	
	/**
	 * 获取/设置图表渲染选项。
	 * 
	 * 图表渲染器可在其render()中使用此函保存图表渲染选项，然后在其update()中获取渲染选项。
	 * 调用chart.inflateRenderOptions()后，会自动调用此函数设置图表渲染选项。
	 * 
	 * @param renderOptions 可选，要设置的渲染选项对象，通常由图表渲染器内部渲染选项、chart.options()合并而成，格式应为：{ ... }
	 * @returns 要获取的图表渲染选项，没有则返回null
	 */
	chartBase.renderOptions = function(renderOptions)
	{
		return chartFactory.extValueBuiltin(this, "renderOptions", renderOptions);
	};
	
	/**
	 * 填充指定图表渲染选项。
	 * 
	 * 此函数先将chart.options()高优先级深度合并至renderOptions，然后调用可选的beforeProcessHandler，
	 * 最后，如果renderOptions中有定义processRenderOptions函数（格式为：function(renderOptions, chart){ ... }），则调用它。
	 * 
	 * 此函数会自动调用chart.renderOptions()设置填充后的图表渲染选项。 
	 * 
	 * 图表渲染器应该在其render()中使用此函数构建图表渲染选项，然后使用它执行图表渲染逻辑，以符合图表API规范。
	 * 
	 * @param renderOptions 可选，待填充的渲染选项，通常由图表渲染器render函数内部生成，格式为：{ ... }，默认为空对象：{}
	 * @param beforeProcessHandler 可选，renderOptions.processRenderOptions调用前处理函数，
								   格式为：function(renderOptions, chart){ ... }, 默认为：undefined
	 * @returns renderOptions
	 */
	chartBase.inflateRenderOptions = function(renderOptions, beforeProcessHandler)
	{
		if(arguments.length == 1)
		{
			//(beforeProcessHandler)
			if($.isFunction(renderOptions))
			{
				beforeProcessHandler = renderOptions;
				renderOptions = undefined;
			}
		}
		
		if(renderOptions == null)
			renderOptions = {};
		
		$.extend(true, renderOptions, this.options());
		
		if(beforeProcessHandler != null)
			beforeProcessHandler(renderOptions, this);
		
		//最后调用processRenderOptions
		if(renderOptions[chartFactory.OPTION_PROCESS_RENDER_OPTIONS])
			renderOptions[chartFactory.OPTION_PROCESS_RENDER_OPTIONS](renderOptions, this);
		
		this.renderOptions(renderOptions);
		
		return renderOptions;
	};
	
	/**
	 * 填充指定图表更新选项。
	 * 
	 * 此函数先将renderOptions中与updateOptions的同名项高优先级深度合并至updateOptions，然后调用可选的beforeProcessHandler，
	 * 最后，如果renderOptions或者chart.renderOptions()中有定义processUpdateOptions函数（格式为：function(updateOptions, chart, results){ ... }），
	 * 则调用它们两个的其中一个（renderOptions优先）。
	 * 
	 * 图表渲染器应该在其update()中使用此函数构建图表更新选项，然后使用它执行图表更新逻辑，以符合图表API规范。
	 * 
	 * @param results 图表更新结果
	 * @param updateOptions 可选，待填充的更新选项，通常由图表渲染器update函数内部生成，格式为：{ ... }，默认为空对象：{}
	 * @param renderOptions 可选，图表的渲染选项，格式为：{ ... }，默认为：chart.renderOptions()
	 * @param beforeProcessHandler 可选，renderOptions.processUpdateOptions调用前处理函数，
								   格式为：function(updateOptions, chart, results){ ... }, 默认为：undefined
	 * @returns updateOptions
	 */
	chartBase.inflateUpdateOptions = function(results, updateOptions, renderOptions, beforeProcessHandler)
	{
		//(results)
		if(arguments.length == 1)
			;
		else if(arguments.length == 2)
		{
			//(results, beforeProcessHandler)
			if($.isFunction(updateOptions))
			{
				beforeProcessHandler = updateOptions;
				updateOptions = undefined;
				renderOptions = undefined;
			}
			//(results, updateOptions)
			else
				;
		}
		else if(arguments.length == 3)
		{
			//(results, updateOptions, beforeProcessHandler)
			if($.isFunction(renderOptions))
			{
				beforeProcessHandler = renderOptions;
				renderOptions = undefined;
			}
			//(results, updateOptions, renderOptions)
			else
				;
		}
		
		var chartRenderOptions = this.renderOptions();
		
		if(updateOptions == null)
			updateOptions = {};
		if(renderOptions == null)
			renderOptions = (chartRenderOptions || {});
		
		//提取renderOptions中的待合并项
		//这些待合并项应该比updateOptions有更高的优先级，因为它们包含由用户定义的有最高优先级的chart.options()
		var srcRenderOptions = {};
		for(var uop in updateOptions)
			srcRenderOptions[uop] = renderOptions[uop];
		
		// < @deprecated 兼容2.6.0版本的chart.optionsUpdate()
		// 待chart.optionsUpdate()移除后应改为：
		// $.extend(true, updateOptions, srcRenderOptions);
		$.extend(true, updateOptions, srcRenderOptions, this.optionsUpdate());
		// > @deprecated 兼容2.6.0版本的chart.optionsUpdate()
		
		if(beforeProcessHandler != null)
			beforeProcessHandler(updateOptions, this, results);
		
		//最后调用processUpdateOptions
		if(renderOptions[chartFactory.OPTION_PROCESS_UPDATE_OPTIONS])
		{
			renderOptions[chartFactory.OPTION_PROCESS_UPDATE_OPTIONS](updateOptions, this, results);
		}
		//renderOptions可能不是chartRenderOptions，此时要确保chartRenderOptions.processUpdateOptions被调用
		else if(chartRenderOptions && renderOptions !== chartRenderOptions
					&& chartRenderOptions[chartFactory.OPTION_PROCESS_UPDATE_OPTIONS])
		{
			chartRenderOptions[chartFactory.OPTION_PROCESS_UPDATE_OPTIONS](updateOptions, this, results);
		}
		
		return updateOptions;
	};
	
	/**
	 * 调用指定图表事件处理函数。
	 * 图表渲染器在实现其on函数逻辑时，可以使用此函数。
	 * 
	 * @param eventHanlder 图表事件处理函数，格式为：function(chartEvent){ ... }
	 * @param chartEvent 传递给上述eventHanlder的图表事件参数
	 * @returns eventHanlder执行结果
	 */
	chartBase.callEventHandler = function(eventHanlder, chartEvent)
	{
		return eventHanlder.call(this, chartEvent);
	};
	
	/**
	 * 注册图表事件处理函数代理。
	 * 图表渲染器on函数的实现逻辑通常是：先构建适配底层组件的图表事件处理函数代理（handlerDelegation），
	 * 在代理中构建图表事件对象，然后调用图表事件处理函数（eventHanlder）。
	 * 此函数用于注册这些信息，使得在实现图表渲染器的off函数时，可以获取对应底层组件的图表事件处理函数代理，进而实现底层组件的解绑逻辑。
	 * 
	 * @param eventType 图表事件类型
	 * @param eventHanlder 图表事件处理函数，格式为：function(chartEvent){ ... }
	 * @param handlerDelegation 图表事件处理函数代理，通常是图表底层组件事件处理函数
	 * @returns 已注册的图表事件处理函数代理信息对象，格式为：{ eventType: "...", eventHanlder: ..., handlerDelegation: ... }
	 */
	chartBase.registerEventHandlerDelegation = function(eventType, eventHanlder, handlerDelegation)
	{
		var delegations = chartFactory.extValueBuiltin(this, "eventHandlerDelegations");
		if(delegations == null)
		{
			delegations = [];
			chartFactory.extValueBuiltin(this, "eventHandlerDelegations", delegations);
		}
		
		var di = { "eventType": eventType, "eventHanlder": eventHanlder, "handlerDelegation": handlerDelegation };
		delegations.push(di);
		
		return di;
	};
	
	/**
	 * 删除图表事件处理函数代理，并返回已删除的代理信息对象数组。
	 * 图表渲染器off函数的实现逻辑通常是：使用此函数移除由registerEventHandlerDelegation注册的图表事件处理函数代理信息对象，
	 * 然后调用底层组件的事件解绑函数，解绑代理信息对象的handlerDelegation。
	 * 
	 * @param eventType 图表事件类型
	 * @param eventHanlder 可选，图表事件处理函数，格式为：function(chartEvent){ ... }，当为undefined时，表示全部
	 * @param eachCallback 可选，对于删除的每个代理信息对象，执行此回调函数（通常包含底层组件的事件解绑逻辑），格式为：function(eventType, eventHandler, handlerDelegation){ ... }
	 * @param returns 匹配给定图表事件类型、图表事件处理函数（可选）的代理信息对象数组，格式为：
	 *						[ { eventType: "...", eventHanlder: ..., handlerDelegation: ... }, ... ]
	 */
	chartBase.removeEventHandlerDelegation = function(eventType, eventHanlder, eachCallback)
	{
		var re = [];
		
		var delegations = chartFactory.extValueBuiltin(this, "eventHandlerDelegations");
		if(delegations == null)
			return re;
		
		var delegationsNew = [];
		
		for(var i=0; i<delegations.length; i++)
		{
			var d = delegations[i];
			
			var remove = (d.eventType == eventType);
			remove = (remove ? (eventHanlder === undefined || d.eventHanlder == eventHanlder) : false);
			
			if(remove)
				re.push(d);
			else
				delegationsNew.push(d);
		}
		
		chartFactory.extValueBuiltin(this, "eventHandlerDelegations", delegationsNew);
		
		if(eachCallback != null)
		{
			for(var i=0; i<re.length; i++)
			{
				eachCallback.call(re[i], re[i].eventType, re[i].eventHandler, re[i].handlerDelegation);
			}
		}
		
		return re;
	};
	
	/**
	 * ECharts图表支持函数：解绑指定图表事件处理函数。
	 * ECharts相关的图表渲染器可以在其off函数中调用此函数，以实现底层事件解绑功能。
	 * 
	 * @param eventType 图表事件类型
	 * @param eventHanlder 可选，图表事件处理函数，格式为：function(chartEvent){ ... }，不设置则解绑所有此类型的图表事件处理函数
	 * @returns 同chartBase.removeEventHandlerDelegation返回值
	 */
	chartBase.echartsOffEventHandler = function(eventType, eventHanlder)
	{
		var internal = this.internal();
		
		return this.removeEventHandlerDelegation(eventType, eventHanlder, function(et, eh, ehd)
		{
			if(internal)
				internal.off(et, ehd);
		});
	};
	
	/**
	 * 获取数据集结果数据指定索引的元素。
	 * 
	 * @param result 数据集结果对象
	 * @param index 索引数值、数值数组
	 * @return 数据对象、据对象数组，当result、index为null时，将返回null
	 */
	chartBase.resultDataElement = function(result, index)
	{
		if(result == null || result.data == null || index == null)
			return undefined;
		
		var datas = this.resultDatas(result);
		
		if(!$.isArray(index))
			return datas[index];
		else
		{
			var re = [];
			
			for(var i=0; i<index.length; i++)
				re.push(datas[index[i]]);
			
			return re;
		}
	};
	
	/**
	 * 获取此图表主题对应的CSS类名。
	 * 这个CSS类名是全局唯一的，可添加至HTML元素的"class"属性。
	 * 
	 * 图表在渲染前会自动为chart.element()图表元素添加chart.themeStyleName()返回的CSS类，
	 * 使得通过chart.themeStyleSheet(name, css)函数创建的样式表可自动应用于图表元素或子元素。
	 * 
	 * @returns CSS类名，不会为null
	 */
	chartBase.themeStyleName = function()
	{
		var theme = this._themeNonNull();
		
		// < @deprecated 兼容4.3.1版本的chartBase.themeStyleName(theme)格式，将在未来版本移除
		if(arguments[0] != null)
			theme = arguments[0];
		// > @deprecated 兼容4.3.1版本的chartBase.themeStyleName(theme)格式，将在未来版本移除
		
		return chartFactory.themeStyleName(theme);
	};
	
	/**
	 * 判断/设置此图表主题和名称关联的CSS样式表，详细参考chartFactory.themeStyleSheet()函数说明。
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
	 *     $("<span class='result-data-count'>").appendTo(chart.elementJquery());
	 *     //使用相同图表主题的多个图表将仅创建一个CSS样式表
	 *     chart.themeStyleSheet("myChartTextStyle", function()
	 *     {
	 *       return { name: " .result-data-count", value: { color: chart.theme().color } };
	 *     });
	 *   },
	 *   update: function(chart, results)
	 *   {
	 *     $(".result-data-count", chart.elementJquery()).text(chart.resultDatas(...).length);
	 *   }
	 * }
	 * 
	 * @param name 参考chartFactory.themeStyleSheet()的name参数
	 * @param css 参考chartFactory.themeStyleSheet()的css参数
	 * @param force 参考chartFactory.themeStyleSheet()的force参数
	 * 
	 * @returns 参考chartFactory.themeStyleSheet()的返回值
	 */
	chartBase.themeStyleSheet = function(name, css, force)
	{
		var theme = this._themeNonNull();
		
		// < @deprecated 兼容4.3.1版本的chartBase.themeStyleSheet(theme, ...)格式，将在未来版本移除
		if(name != null && !chartFactory.isString(name))
		{
			theme = arguments[0];
			name = arguments[1];
			css = arguments[2];
			force = arguments[3];
		}
		// > @deprecated 兼容4.3.1版本的chartBase.themeStyleSheet(theme, ...)格式，将在未来版本移除
		
		return chartFactory.themeStyleSheet(theme, name, css, force);
	};
	
	/**
	 * 获取/设置HTML元素的CSS样式字符串（元素的style属性）。
	 * 具体参考chartFactory.elementStyle()函数。
	 */
	chartBase.elementStyle = function(element, css)
	{
		return chartFactory.elementStyle(element, css);
	};
	
	/**
	 * 拼接CSS样式字符串。
	 * 具体参考chartFactory.styleString()函数。
	 */
	chartBase.styleString = function(css)
	{
		var cssArray = [];
		
		for(var i=0; i<arguments.length; i++)
		{
			var cssi = arguments[i];
			
			if(!cssi)
				continue;
			
			cssArray = cssArray.concat(cssi);
		}
		
		return chartFactory.styleString(cssArray);
	};
	
	/**
	 * 获取/设置数据集别名。
	 * 
	 * @param chartDataSet 图表数据集、图表数据集索引数值
	 * @param alias 可选，要设置的别名，不设置则执行获取操作
	 * @returns 要获取的别名，不会为null
	 * @since 2.10.0
	 */
	chartBase.dataSetAlias = function(chartDataSet, alias)
	{
		chartDataSet = this._chartDataSetOf(chartDataSet);
		
		if(alias === undefined)
		{
			if(chartDataSet.alias)
				return chartDataSet.alias;
			
			var dataSet = (chartDataSet.dataSet || chartDataSet);
			
			return (dataSet ? (dataSet.name || "") : "");
		}
		else
		{
			chartDataSet.alias = alias;
		}
	};
	
	/**
	 * 获取数据集属性数组。
	 * 返回数组排序遵循如下规则：
	 * 排序值越小越靠前；
	 * 属性默认具有与其索引相同的排序值；
	 * 当两个属性具有相同排序值时，设置了propertyOrders中排序值的那个属性靠前排（前置插入），否则，属性索引小的那个靠前排。
	 * 
	 * @param chartDataSet 图表数据集、图表数据集索引数值、数据集
	 * @param sort 可选，当chartDataSet是图表数据集时，是否依据其propertyOrders对返回结果进行重排序，true 是；false 否。默认值为：true
	 * @returns 数据集属性数组，返回空数组表示没有属性
	 * @since 2.10.0
	 */
	chartBase.dataSetProperties = function(chartDataSet, sort)
	{
		chartDataSet = this._chartDataSetOf(chartDataSet);
		sort = (sort === undefined ? true : sort);
		
		var properties = null;
		var isDataSet = (chartDataSet.properties !== undefined);
		
		if(isDataSet)
			properties = chartDataSet.properties;
		else
			properties = (chartDataSet.dataSet ? chartDataSet.dataSet.properties : null);
		
		properties = (properties || []);
		
		if(isDataSet || !sort)
			return properties;
		
		var propertyOrders = chartDataSet.propertyOrders;
		
		if(!propertyOrders)
			return properties;
		
		var pos = [];
		
		for(var i=0; i<properties.length; i++)
		{
			var p = properties[i];
			pos[i] = { property: p, order: propertyOrders[p.name], index: i };
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
			re[i] = pos[i].property;
		
		return re;
	};
	
	/**
	 * 获取指定标识的数据集属性。
	 * 
	 * @param chartDataSet 图表数据集、图表数据集索引数值、数据集
	 * @param info 数据集属性标识，可以是属性名、属性索引
	 * @returns 数据集属性，没有找到则返回undefined
	 * @since 2.10.0
	 */
	chartBase.dataSetProperty = function(chartDataSet, info)
	{
		var properties = this.dataSetProperties(chartDataSet, false);
		
		if(!properties)
			return undefined;
		
		if(chartFactory.isNumber(info))
			return properties[info];
		
		for(var i=0; i<properties.length; i++)
		{
			if(properties[i].name == info)
				return properties[i];
		}
		
		return undefined;
	};
	
	/**
	 * 获取/设置数据集属性别名。
	 * 
	 * @param chartDataSet 图表数据集、图表数据集索引数值
	 * @param dataSetProperty 数据集属性、属性名、属性索引
	 * @param alias 可选，要设置的别名，不设置则执行获取操作
	 * @returns 要获取的别名，不会为null
	 * @since 2.10.0
	 */
	chartBase.dataSetPropertyAlias = function(chartDataSet, dataSetProperty, alias)
	{
		chartDataSet = this._chartDataSetOf(chartDataSet);
		
		if(chartFactory.isStringOrNumber(dataSetProperty))
			dataSetProperty = this.dataSetProperty(chartDataSet, dataSetProperty);
		
		if(alias === undefined)
		{
			if(!dataSetProperty)
				return "";
			
			alias =  (chartDataSet.propertyAliases ?
							chartDataSet.propertyAliases[dataSetProperty.name] : null);
			
			if(!alias)
				alias = (dataSetProperty.label ||  dataSetProperty.name);
			
			return (alias || "");
		}
		else
		{
			if(!chartDataSet.propertyAliases)
				chartDataSet.propertyAliases = {};
			
			chartDataSet.propertyAliases[dataSetProperty.name] = alias;
		}
	};
	
	/**
	 * 获取/设置数据集属性排序值。
	 * 
	 * @param chartDataSet 图表数据集、图表数据集索引数值
	 * @param dataSetProperty 数据集属性、属性名、属性索引
	 * @param order 可选，要设置的排序数值，不设置则执行获取操作
	 * @returns 要获取的排序数值，没有设置过则返回null
	 * @since 2.10.0
	 */
	chartBase.dataSetPropertyOrder = function(chartDataSet, dataSetProperty, order)
	{
		chartDataSet = this._chartDataSetOf(chartDataSet);
		
		var name = null;
		
		if(chartFactory.isString(dataSetProperty))
			name = dataSetProperty;
		else
		{
			if(chartFactory.isNumber(dataSetProperty))
				dataSetProperty = this.dataSetProperty(chartDataSet, dataSetProperty);
			
			name = (dataSetProperty ? dataSetProperty.name : null);
		}
		
		if(order === undefined)
		{
			return (chartDataSet.propertyOrders ?
							chartDataSet.propertyOrders[name] : undefined);
		}
		else
		{
			if(!chartDataSet.propertyOrders)
				chartDataSet.propertyOrders = {};
			
			chartDataSet.propertyOrders[name] = order;
		}
	};
	
	/**
	 * 获取数据集参数数组。
	 * 
	 * @param chartDataSet 图表数据集、图表数据集索引数值、数据集
	 * @returns 数据集参数数组，空数组表示没有参数
	 * @since 2.10.0
	 */
	chartBase.dataSetParams = function(chartDataSet)
	{
		chartDataSet = this._chartDataSetOf(chartDataSet);
		
		var params = null;
		
		if(chartDataSet.params !== undefined)
			params = chartDataSet.params;
		else
			params = (chartDataSet.dataSet ? chartDataSet.dataSet.params : null);
		
		return (params || []);
	};
	
	/**
	 * 获取指定标识的数据集参数。
	 * 
	 * @param chartDataSet 图表数据集、图表数据集索引数值、数据集
	 * @param info 数据集参数标识，可以是参数名、参数索引
	 * @returns 数据集参数，没有找到则返回undefined
	 * @since 2.10.0
	 */
	chartBase.dataSetParam = function(chartDataSet, info)
	{
		var params = this.dataSetParams(chartDataSet);
		
		if(!params)
			return undefined;
		
		if(chartFactory.isNumber(info))
			return params[info];
		
		for(var i=0; i<params.length; i++)
		{
			if(params[i].name == info)
				return params[i];
		}
		
		return undefined;
	};
	
	/**
	 * 判断是否有数据集参数。
	 * 
	 * @since 2.10.0
	 */
	chartBase.hasDataSetParam = function()
	{
		var chartDataSets = this.chartDataSets;
		for(var i=0; i<chartDataSets.length; i++)
		{
			var params = chartDataSets[i].dataSet.params;
			
			if(params && params.length > 0)
				return true;
		}
		
		return false;
	};
	
	/**
	 * 获取数据集结果数据经属性映射后的对象数组。
	 * 
	 * @param result 数据集结果对象、对象数组
	 * @param propertyMap 返回对象属性映射表，格式为：{ 返回对象属性名: 数据集结果数据属性对象、属性名、属性数组、属性名数组 }
	 * @param row 可选，行索引，以0开始，默认为0
	 * @param count 可选，获取结果数据的最多行数，默认为全部
	 * @return [{"...": ..., "...": ...}, ...]
	 * @since 2.10.0
	 */
	chartBase.resultMapObjects = function(result, propertyMap, row, count)
	{
		var re = [];
		
		var datas = this.resultDatas(result);
		row = (row == null ? 0 : row);
		count = (count == null ? datas.length : (count < datas.length ? count : datas.length));
		
		var propIsArray = {};
		for(var opn in propertyMap)
			propIsArray[opn] = $.isArray(propertyMap[opn]);
		
		for(var i=row; i<count; i++)
		{
			var di = datas[i];
			var obj = (di == null ? null : {});
			
			for(var opn in propertyMap)
			{
				var dp = propertyMap[opn];
				
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
	 * ECharts图表支持函数：获取可用于此图表的且已注册的ECharts主题名。
	 * 此函数优先返回chartBase.echartsThemeName()函数的结果，
	 * 当其为null时，则使用chartBase.theme()构建和注册ECharts主题（仅第一次），并返回注册后的主题名。
	 * 
	 * @since 2.11.0
	 */
	chartBase.echartsGetThemeName = function()
	{
		var themeName = this.echartsThemeName();
		
		//从ChartTheme构建ECharts主题
		if(!themeName)
		{
			var theme = this._themeNonNull();
			themeName = theme[chartFactory._KEY_REGISTERED_ECHARTS_THEME_NAME];
			
			if(!themeName)
			{
				themeName = (theme[chartFactory._KEY_REGISTERED_ECHARTS_THEME_NAME] = chartFactory.uid());
				
				var echartsTheme = chartFactory.buildEchartsTheme(theme);
				echarts.registerTheme(themeName, echartsTheme);
			}
		}
		
	    return themeName;
	};
	
	/**
	 * 获取/设置指定数据集属性标记。
	 * 
	 * @param chartDataSet 图表数据集、图表数据集索引数值
	 * @param dataSetProperty 数据集属性、属性名、属性索引
	 * @param dataSign 可选，要设置的数据标记对象、对象数组，或者名称字符串、字符串数组，或者null，不设置则执行获取操作
	 * @returns 要获取的标记名字符串数组、null
	 * @since 2.11.0
	 */
	chartBase.dataSetPropertySign = function(chartDataSet, dataSetProperty, dataSign)
	{
		chartDataSet = this._chartDataSetOf(chartDataSet);
		
		var name = null;
		
		if(chartFactory.isString(dataSetProperty))
			name = dataSetProperty;
		else
		{
			if(chartFactory.isNumber(dataSetProperty))
				dataSetProperty = this.dataSetProperty(chartDataSet, dataSetProperty);
			
			name = (dataSetProperty ? dataSetProperty.name : null);
		}
		
		if(dataSign === undefined)
		{
			return (chartDataSet.propertySigns ?
							chartDataSet.propertySigns[name] : undefined);
		}
		else
		{
			if(!chartDataSet.propertySigns)
				chartDataSet.propertySigns = {};
			
			dataSign = this._trimDataSetPropertySign(dataSign);
			chartDataSet.propertySigns[name] = dataSign;
		}
	};
	
	/**
	 * 获取/设置数据集属性标记映射表。
	 * 
	 * @param chartDataSet 图表数据集、图表数据集索引数值
	 * @param signs 可选，要设置的数据标记映射表，格式为：{ 数据集属性名: 数据标记对象、对象数组，或者名称字符串、字符串数组，或者null, ... }，不设置则执行获取操作
	 * @param increment 可选，设置操作时是否执行增量设置，仅设置signs中出现的项，true 是；false 否。默认值为：true
	 * @returns 要获取的标记映射表，格式为：{ 数据集属性名: 标记名字符串数组、null, ... }，不会为null
	 * @since 2.11.0
	 */
	chartBase.dataSetPropertySigns = function(chartDataSet, signs, increment)
	{
		chartDataSet = this._chartDataSetOf(chartDataSet);
		increment = (increment == null ? true : increment);
		
		if(signs === undefined)
		{
			return (chartDataSet.propertySigns || {});
		}
		else
		{
			var trimSigns = {};
			
			if(signs)
			{
				for(var p in signs)
				{
					var ps = this._trimDataSetPropertySign(signs[p]);
					trimSigns[p] = ps;
				}
			}
			
			if(!chartDataSet.propertySigns || !increment)
				chartDataSet.propertySigns = trimSigns;
			else
			{
				for(var p in trimSigns)
					chartDataSet.propertySigns[p] = trimSigns[p];
			}
		}
	};
	
	chartBase._trimDataSetPropertySign = function(dataSign)
	{
		if(dataSign == null)
			return null;
		
		if(!$.isArray(dataSign))
			dataSign = [ dataSign ];
		
		var signNames = [];
		
		for(var i=0; i<dataSign.length; i++)
		{
			var signName = (dataSign[i] && dataSign[i].name !== undefined ? dataSign[i].name : dataSign[i]);
			
			if(signName != null)
				signNames.push(signName);
		}
		
		return signNames;
	};
	
	/**
	 * 判断给定图表数据集是否是可变模型的。
	 * 
	 * @param chartDataSet 图表数据集、图表数据集索引数值
	 * @returns true、false
	 * @since 3.0.0
	 */
	chartBase.isMutableModel = function(chartDataSet)
	{
		chartDataSet = this._chartDataSetOf(chartDataSet);
		return (chartDataSet.dataSet.mutableModel == true);
	};
	
	/**
	 * 获取/设置多条图表展示数据的原始数据索引（图表ID、图表数据集索引、结果数据索引）。
	 * 图表展示数据是指由图表数据集结果数据转换而得，用于渲染图表的数据。
	 * 图表渲染器在构建图表展示数据时，应使用此函数设置其原始数据索引信息，以支持在后续的交互、事件处理中获取它们。
	 * 
	 * @param data 展示数据数组，格式为：[ ... ]，元素格式允许为：{ ... }、[ ... ]，对于设置操作，当元素是对象时，将为其添加一个额外属性；
	 * 			   当元素是数组时，如果末尾元素已是原始数据索引对象，替换；否则，追加
	 * @param chartDataSetIndex 要设置的图表数据集对象（自动取其索引）、图表数据集对象数组（自动取其索引）、图表数据集索引数值、索引数值数组
	 * @param resultDataIndex 要设置的结果数据索引，格式为：
	 *                        当chartDataSetIndex是图表数据集对象、索引数值时：
	 *                        数值、数值数组
	 *                        当chartDataSetIndex是图表数据集对象数组、索引数值数组时：
	 *                        数值，表示chartDataSetIndex数组每个元素的结果数据索引都是此数值
	 *                        数组（元素可以是数值、数值数组），与chartDataSetIndex数组元素一一对应
	 *                        默认值为：0
	 * @param autoIncrement 可选，
	 *                      当chartDataSetIndex是图表数据集对象、图表数据集索引数值且resultDataIndex是数值时，是否自动递增resultDataIndex；
	 *                      当chartDataSetIndex是图表数据集对象数组、图表数据集索引数值数组且其元素对应位置的resultDataIndex是数值时，是否自动递增resultDataIndex。
	 *                      默认值为：true
	 * @returns 要获取的原始数据索引数组(元素可能为null），原始数据索引对象格式为：
	 *									{
	 *										//图表ID
	 *										chartId: "...",
	 *										//图表数据集索引，格式为：数值、数值数组
	 *										chartDataSetIndex: ...,
	 *										//结果数据索引，格式为：
	 *                                      //当chartDataSetIndex是数值时：
	 *                                      //数值、数值数组
	 *                                      //当chartDataSetIndex是数值数组时：
	 *                                      //数组（元素可能是数值、数值数组）
	 *										resultDataIndex: ...
	 *									}
	 * @since 3.1.0
	 */
	chartBase.originalDataIndexes = function(data, chartDataSetIndex, resultDataIndex, autoIncrement)
	{
		//获取
		if(arguments.length <= 1)
		{
			var re = [];
			
			for(var i=0; i<data.length; i++)
				re[i] = this._originalDataIndex(data[i]);
			
			return re;
		}
		//设置
		else
		{
			if(data == null)
				return;
			
			//(data, chartDataSetIndex, true)、(data, chartDataSetIndex, false)
			if(resultDataIndex === true || resultDataIndex === false)
			{
				autoIncrement = resultDataIndex;
				resultDataIndex = undefined;
			}
			resultDataIndex = (resultDataIndex === undefined ? 0 : resultDataIndex);
			
			var isCdsiArray = $.isArray(chartDataSetIndex);
			
			if(isCdsiArray)
			{
				var cdsiNew = [];
				
				for(var i=0; i<chartDataSetIndex.length; i++)
				{
					cdsiNew[i] = (chartDataSetIndex[i] != null && chartDataSetIndex[i].index !== undefined ?
									chartDataSetIndex[i].index : chartDataSetIndex[i]);
				}
				
				chartDataSetIndex = cdsiNew;
				
				if(!$.isArray(resultDataIndex))
				{
					var rdiNew = [];
					
					for(var i=0; i<chartDataSetIndex.length; i++)
						rdiNew[i] = resultDataIndex;
					
					resultDataIndex = rdiNew;
				}
			}
			else
			{
				chartDataSetIndex = (chartDataSetIndex != null && chartDataSetIndex.index !== undefined ?
										chartDataSetIndex.index : chartDataSetIndex);
			}
			
			autoIncrement = (autoIncrement === undefined ? true : autoIncrement);
			var isRdiNumber = (typeof(resultDataIndex) == "number");
			
			var needAutoIncrementEle = (autoIncrement == true && $.isArray(resultDataIndex));
			if(needAutoIncrementEle == true)
			{
				needAutoIncrementEle = false;
				
				//任一元素是数值的话，才需要自增处理
				for(var i=0; i<resultDataIndex.length; i++)
				{
					if(typeof(resultDataIndex[i]) == "number")
					{
						needAutoIncrementEle = true;
						break;
					}
				}
			}
			
			for(var i=0; i<data.length; i++)
			{
				var resultDataIndexMy;
				
				if(!autoIncrement)
				{
					resultDataIndexMy = resultDataIndex;
				}
				else
				{
					resultDataIndexMy = resultDataIndex;
					
					if(isRdiNumber)
					{
						resultDataIndexMy = resultDataIndex + i;
					}
					else if(needAutoIncrementEle)
					{
						resultDataIndexMy = [];
						for(var j=0; j<resultDataIndex.length; j++)
						{
							resultDataIndexMy[j] = resultDataIndex[j];
							if(typeof(resultDataIndexMy[j]) == "number")
								resultDataIndexMy[j] = resultDataIndexMy[j] + i;
						}
					}
				}
				
				this._originalDataIndex(data[i], chartDataSetIndex, resultDataIndexMy);
			}
		}
	};
	
	/**
	 * 获取/设置单条图表展示数据的原始数据索引（图表ID、图表数据集索引、结果数据索引）。
	 * 图表展示数据是指由图表数据集结果数据转换而得，用于渲染图表的数据。
	 * 图表渲染器在构建图表展示数据时，应使用此函数设置其原始数据索引，以支持在后续的交互、事件处理中获取它们。
	 * 
	 * @param data 展示数据，格式为：{ ... }、[ ... ]，对于设置操作，当展示数据是对象时，将为其添加一个额外属性；
	 * 			   当展示数据是数组时，如果末尾元素已是索引信息对象，则替换；否则，追加一个元素
	 * @param chartDataSetIndex 同chartBase.originalDataIndexes()函数的chartDataSetIndex参数
	 * @param resultDataIndex 同chartBase.originalDataIndexes()函数的resultDataIndex参数
	 * @returns 要获取的原始数据索引(可能为null），格式参考chartBase.originalDataIndexes()函数返回值
	 * @since 3.1.0
	 */
	chartBase.originalDataIndex = function(data, chartDataSetIndex, resultDataIndex)
	{
		//获取
		if(arguments.length <= 1)
		{
			return this._originalDataIndex(data);
		}
		else
		{
			data = [ data ];
			this.originalDataIndexes(data, chartDataSetIndex, resultDataIndex, false);
		}
	};
	
	//获取/设置单条图表展示数据的原始数据索引
	chartBase._originalDataIndex = function(data, chartDataSetIndex, resultDataIndex)
	{
		if(arguments.length <= 1)
			return chartFactory.originalDataIndex(data);
		else
			chartFactory.originalDataIndex(data, this.id, chartDataSetIndex, resultDataIndex);
	};
	
	/**
	 * 图表事件支持函数：获取/设置图表事件对象的原始数据索引（图表ID、图表数据集索引、结果数据索引），即：chartEvent.originalDataIndex。
	 * 
	 * @param chartEvent 图表事件对象，格式应为：{ ... }
	 * @param originalDataIndex 要设置的原始数据索引对象、数组，通常是chartBase.originalDataIndex()或chartBase.originalDataIndexes()函数的返回值
	 * @param inflateOriginalData 可选，设置操作时是否根据上述originalDataIndex设置图表事件对象的原始图表数据集结果数据。默认值为：true
	 * @returns 要获取的原始结果数据索引，未设置过为null
	 * @since 3.1.0
	 */
	chartBase.eventOriginalDataIndex = function(chartEvent, originalDataIndex, inflateOriginalData)
	{
		if(originalDataIndex === undefined)
			return chartEvent["originalDataIndex"];
		else
		{
			chartEvent["originalDataIndex"] = originalDataIndex;
			
			if(inflateOriginalData !== false && originalDataIndex)
			{
				var originalData = chartFactory.originalData(this, originalDataIndex);
				this.eventOriginalData(chartEvent, originalData);
			}
			
			// < @deprecated 兼容3.0.1版本的chartEvent.originalChartDataSetIndex、originalResultDataIndex结构，将在未来版本移除
			var isArray = $.isArray(originalDataIndex);
			var originalDataIndexAry = (isArray ? originalDataIndex : [ originalDataIndex ]);
			var originalChartDataSetIndex = [];
			var originalResultDataIndex = [];
			for(var i=0; i<originalDataIndexAry.length; i++)
			{
				originalChartDataSetIndex[i] = originalDataIndexAry[i].chartDataSetIndex;
				originalResultDataIndex[i] = originalDataIndexAry[i].resultDataIndex;
			}
			chartEvent["originalChartDataSetIndex"] = (isArray ? originalChartDataSetIndex : originalChartDataSetIndex[0]);
			chartEvent["originalResultDataIndex"] = (isArray ? originalResultDataIndex : originalResultDataIndex[0]);
			// > @deprecated 兼容3.0.1版本的chartEvent.originalChartDataSetIndex、originalResultDataIndex结构，将在未来版本移除
		}
	};
	
	/**
	 * 图表事件支持函数：创建图表事件对象。
	 * 
	 * 图表渲染器实现相关：
	 * 图表渲染器应在后续应通过
	 * chartBase.eventData()、chartBase.eventOriginalDataIndex()、
	 * chartBase.eventOriginalData()
	 * 填充图表事件对象。
	 * 
	 * @param type 事件类型
	 * @param originalEvent 底层原始事件
	 * @returns 图表事件对象，格式为：
	 *							{
	 *								//事件类型，比如："click"、"dblclick"、"mousedown"
	 *								type: "...",
	 *								//事件数据，格式由各图表类型决定，通常是：{ ... }、[ ... ]、[ {...}, ... ]、[ [...], ... ]
	 *								data: ...,
	 *								//事件原始数据索引（图表数据集结果数据索引），格式为：{ ... }、[ {...}, ... ]
	 *								originalDataIndex: ...,
	 *								//事件原始数据（图表数据集结果数据），格式由各图表类型决定，通常是：{ ... }、[ ... ]、[ {...}, ... ]、[ [...], ... ]
	 *								originalData: ...,
	 *								//底层原始事件，通常是图表底层组件事件对象
	 *								originalEvent: ...,
	 *								//事件图表对象
	 *								chart: ...
	 * 							}
	 * @since 3.1.0
	 */
	chartBase.eventNew = function(type, originalEvent)
	{
		var event =
		{
			"type": type,
			"originalEvent": originalEvent,
			"chart": this
		};
		
		return event;
	};
	
	/**
	 * 获取图表插件的所有资源列表。
	 * 
	 * @returns 插件资源列表，格式为：[ { name: "..." }, ... ]
	 * @since 4.1.0
	 */
	chartBase.pluginResources = function()
	{
		return (this.plugin.resources || []);
	};
	
	/**
	 * 获取图表插件指定名称资源的URL。
	 * 使用此URL可从服务端加载资源。
	 * 
	 * @param name chartBase.pluginResources()函数返回的其中一个资源名
	 * @returns 
	 * @since 4.1.0
	 */
	chartBase.pluginResourceURL = function(name)
	{
		if(!this._isRenderContextInited())
			throw new Error("chart is illegal state for pluginResourceURL(name)");
		
		name = (name || "");
		
		var webContext = this._renderContextAttrWebContext();
		
		var url = "/chartPlugin/resource/"+encodeURIComponent(this.plugin.id)+"/"+name;
		url = chartFactory.toWebContextPathURL(webContext, url);
		
		return url;
	};
	
	/**
	 * 获取/设置指定图表属性值。
	 * 注意：org.datagear.analysis.support.html.AttributeValueHtmlChartPlugin需要此函数名。
	 * 
	 * @param name 插件属性、名称
	 * @param value 可选，要设置的属性值
	 * @returns 
	 * @since 4.2.0
	 */
	chartBase.attrValue = function(name, value)
	{
		name = (name && name.name != null ? name.name : name);
		
		if(value === undefined)
			return this._attrValues[name];
		else
			this._attrValues[name] = value;
	};
	
	/**
	 * 获取/设置全部图表属性值。
	 * 
	 * @param values 可选，要设置的属性值映射表，格式为：{ 名称: 值, ... }
	 * @returns { ... }
	 * @since 4.2.0
	 */
	chartBase.attrValues = function(values)
	{
		if(values === undefined)
			return this._attrValues;
		else
			this._attrValues = (values ? values : {});
	};
	
	/**
	 * 获取全部原始图表属性值，通常是在定义图表时设置的，未与"dg-chart-attr-values"合并。
	 * 
	 * @returns { ... }
	 * @since 4.2.0
	 */
	chartBase.attrValuesOrigin = function()
	{
		return this._attrValuesOrigin;
	};
	
	/**
	 * 获取所有插件属性。
	 * 
	 * @returns [ ]
	 * @since 4.2.0
	 */
	chartBase.pluginAttributes = function()
	{
		return (this.plugin && this.plugin.attributes ? this.plugin.attributes : []);
	};
	
	/**
	 * 获取原始图表选项，即在定义图表时设置的图表选项。
	 * 
	 * @param eval 可选，可选，是否返回选项对象而非字符串，默认为：false
	 * @returns 字符串、{ ... }、null
	 * @since 4.2.0
	 */
	chartBase.optionsOrigin = function(eval)
	{
		eval = (eval == null ? false : eval);
		
		var options = this._optionsOrigin;
		
		if(eval)
		{
			if(!options || options == "")
				options = null;
			else
				options = chartFactory.evalSilently(options, {});
		}
		
		return options;
	};
	
	/**
	 * 图表是否是活着的（已执行渲染且未完成销毁）。
	 * 
	 * @since 4.4.0
	 */
	chartBase.isAlive = function()
	{
		return (this._isAlive == true);
	};
	
	/**
	 * 图表是否为/设置为：准备init。
	 * 
	 * @param set 可选，为true时设置状态；否则，判断状态
	 * 
	 * @since 4.4.0
	 */
	chartBase.statusPreInit = function(set)
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
	 * 
	 * @since 4.4.0
	 */
	chartBase.statusIniting = function(set)
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
	 * 
	 * @since 4.4.0
	 */
	chartBase.statusInited = function(set)
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
	 * 
	 * @since 4.4.0
	 */
	chartBase.themeGradualColor = function(factor)
	{
		var theme = this._themeNonNull();
		return chartFactory.themeGradualColor(theme, factor);
	};
	
	//-------------
	// < 已弃用函数 start
	//-------------
	
	// < @deprecated 兼容4.3.1版本的API，将在未来版本移除，请使用chartBase.themeGradualColor()
	/**
	 * 获取图表主题指定渐变因子的颜色。
	 * 这个颜色是图表主题的实际背景色（actualBackgroundColor）与前景色（color）之间的某个颜色。
	 * 
	 * 图表渲染器在绘制图表时，可以使用此函数获取的颜色来设置图表配色。
	 * 
	 * @param factor 可选，渐变因子，0-1之间的小数，其中0表示最接近实际背景色的颜色、1表示最接近前景色的颜色
	 * @param theme 可选，用于获取颜色的主题，默认为：chart.theme()
	 * @returns 与factor匹配的颜色字符串，格式类似："#FFFFFF"，如果未设置factor，将返回一个包含所有渐变颜色的数组
	 */
	chartBase.gradualColor = function(factor, theme)
	{
		//gradualColor(theme)
		if(arguments.length == 1 && typeof(factor) != "number")
		{
			theme = factor;
			factor = undefined;
		}
		
		theme = (theme == null ? this._themeNonNull() : theme);
		
		return chartFactory.themeGradualColor(theme, factor);
	};
	// > @deprecated 兼容4.3.1版本的API，将在未来版本移除，请使用chartBase.themeGradualColor()
	
	// < @deprecated 兼容3.0.1版本的API，将在未来版本移除，请使用chartBase.eventNew()
	/**
	 * 图表事件支持函数：创建ECharts图表的事件对象。
	 * 
	 * 图表渲染器实现相关：
	 * 图表渲染器应在后续应通过
	 * chartBase.eventData()、chartBase.eventOriginalDataIndex()、
	 * chartBase.eventOriginalData()
	 * 填充图表事件对象。
	 * 
	 * @param type 事件类型
	 * @param echartsEventParams ECharts事件处理函数的参数对象
	 * @returns 图表事件对象，格式参考chartBase.eventNew()函数返回值
	 */
	chartBase.eventNewEcharts = function(type, echartsEventParams)
	{
		var event = this.eventNew(type, echartsEventParams);
		event.chartType = "echarts";
		
		return event;
	};
	
	/**
	 * 图表事件支持函数：创建HTML图表的事件对象。
	 * 
	 * 图表渲染器实现相关：
	 * 图表渲染器应在后续应通过
	 * chartBase.eventData()、chartBase.eventOriginalDataIndex()、
	 * chartBase.eventOriginalData()
	 * 填充图表事件对象。
	 * 
	 * @param type 事件类型
	 * @param htmlEvent HTML事件对象
	 * @returns 图表事件对象，格式参考chartBase.eventNew()函数返回值
	 */
	chartBase.eventNewHtml = function(type, htmlEvent)
	{
		var event = this.eventNew(type, htmlEvent);
		event.chartType = "html";
		
		return event;
	};
	// > @deprecated 兼容3.0.1版本的API，将在未来版本移除，请使用chartBase.eventNew()
	
	// < @deprecated 兼容3.0.1版本的API，将在未来版本移除，请使用chartBase.eventOriginalDataIndex()
	/**
	 * 图表事件支持函数：获取/设置图表事件数据（chartBase.eventData(chartEvent)返回值）对应的原始图表数据集索引（chartEvent.originalChartDataSetIndex）。
	 * 
	 * @param chartEvent 图表事件对象，格式应为：{ ... }
	 * @param originalChartDataSetIndex 可选，要设置的原始图表数据集索引，格式应为：
	 *                                  当图表事件数据是对象时：图表数据集索引数值、图表数据集索引数值数组
	 *                                  当图表事件数据是对象数组时：数组，其元素可能为图表数据集索引数值、图表数据集索引数值数组
	 *                                  其中，图表数据集索引数值允许为null，因为图表事件数据可能并非由图表结果数据构建
	 * @returns 要获取的原始图表数据集索引，未设置则返回null
	 */
	chartBase.eventOriginalChartDataSetIndex = function(chartEvent, originalChartDataSetIndex)
	{
		if(originalChartDataSetIndex === undefined)
			return chartEvent["originalChartDataSetIndex"];
		else
			chartEvent["originalChartDataSetIndex"] = originalChartDataSetIndex;
	};
	
	/**
	 * 图表事件支持函数：获取/设置图表事件数据（chartBase.eventData(chartEvent)返回值）对应的原始数据集结果数据索引（chartEvent.originalResultDataIndex）。
	 * 
	 * @param chartEvent 图表事件对象，格式应为：{ ... }
	 * @param originalResultDataIndex 可选，要设置的原始数据集结果数据索引，格式应为：
	 *                                与chartBase.eventOriginalChartDataSetIndex(chartEvent)返回值格式一致，
	 *                                只是每一个图表数据集索引数值可能对应一个数据集结果数据索引数值、也可能对应一个数据集结果数据索引数值数组
	 * @returns 要获取的原始数据集结果数据索引，未设置则返回null
	 */
	chartBase.eventOriginalResultDataIndex = function(chartEvent, originalResultDataIndex)
	{
		if(originalResultDataIndex === undefined)
			return chartEvent["originalResultDataIndex"];
		else
			chartEvent["originalResultDataIndex"] = originalResultDataIndex;
	};
	
	/**
	 * 图表事件支持函数：设置图表事件对象的原始图表数据集索引、原始数据、原始结果数据索引。
	 * 
	 * @param chartEvent 图表事件对象，格式应为：{ ... }
	 * @param originalInfo 图表数据对象、数组，或者原始信息对象、数组（格式参考：chartBase.originalInfo函数返回值），或者原始图表数据集索引数值（用于兼容旧版API）
	 * @param originalResultDataIndex 可选，当originalInfo是索引数值时的原始数据索引，格式可以是：数值、数值数组
	 */
	chartBase.eventOriginalInfo = function(chartEvent, originalInfo, originalResultDataIndex)
	{
		var ocdsi = null;
		var ordi = null;
		var odata = null;
		
		var updateResults = this.updateResults();
		
		if(originalInfo == null)
		{
		}
		else if(typeof(originalInfo) == "number")
		{
			ocdsi = originalInfo;
			ordi = originalResultDataIndex;
			
			odata = this.resultDataElement(this.resultAt(updateResults, ocdsi), ordi);
		}
		else
		{
			var isArray = $.isArray(originalInfo);
			
			if(!isArray)
				originalInfo = [ originalInfo ];
			
			ocdsi = [];
			ordi = [];
			odata = [];
			
			for(var i=0; i<originalInfo.length; i++)
			{
				//先认为是图表数据对象
				var myOi = this.originalInfo(originalInfo[i]);
				if(!myOi)
					myOi = originalInfo[i];
				
				var myOcdsi = (myOi ? myOi.chartDataSetIndex : null);
				var myOrdi = (myOi ? myOi.resultDataIndex : null);
				
				ocdsi[i] = myOcdsi;
				ordi[i] = myOrdi;
				
				if($.isArray(myOcdsi))
				{
					odata[i] = [];
					
					for(var j=0; j<myOcdsi.length; j++)
						odata[i][j] = this.resultDataElement(this.resultAt(updateResults, myOcdsi[j]), (myOrdi ? myOrdi[j] : null));
				}
				else
				{
					odata[i] = this.resultDataElement(this.resultAt(updateResults, myOcdsi), myOrdi);
				}
			}
			
			if(!isArray)
			{
				ocdsi = ocdsi[0];
				ordi = ordi[0];
				odata = odata[0];
			}
		}
		
		this.eventOriginalChartDataSetIndex(chartEvent, ocdsi);
		this.eventOriginalResultDataIndex(chartEvent, ordi);
		this.eventOriginalData(chartEvent, odata);
	};
	// > @deprecated 兼容3.0.1版本的API，将在未来版本移除，请使用chartBase.eventOriginalDataIndex()
	
	// < @deprecated 兼容3.0.1版本的API，将在未来版本移除，请使用chartBase.originalDataIndex()、chartBase.originalDataIndexes()
	/**
	 * 获取/设置指定数据对象的原始信息属性值，包括：图表ID、图表数据集索引、结果数据索引。
	 * 图表渲染器在构建用于渲染图表的内部数据对象时，应使用此函数设置其原始信息，以支持在后续的交互、事件处理中获取这些原始信息。
	 * 
	 * @param data 数据对象、数据对象数组，格式为：{ ... }、[ { ... }, ... ]，当是数组时，设置操作将为每个元素单独设置原始信息
	 * @param chartDataSetIndex 要设置的图表数据集索引数值、图表数据集对象（自动取其索引数值），或者它们的数组
	 * @param resultDataIndex 可选，要设置的结果数据索引，格式为：
	 *                        当chartDataSetIndex不是数组时：
	 *                        数值、数值数组
	 *                        当chartDataSetIndex是数组时：
	 *                        数值，表示chartDataSetIndex数组每个元素的结果数据索引都是此数值
	 *                        数组（元素可以是数值、数值数组），表示chartDataSetIndex数组每个元素的结果数据索引是此数组对应位置的元素
	 *                        默认值为：0
	 * @param autoIncrement 可选，当data是数组时：
	 *                      当chartDataSetIndex不是数组且resultDataIndex是数值时，设置时是否自动递增resultDataIndex；
	 *                      当chartDataSetIndex是数组且其元素对应位置的结果数据索引是数值时，是否自动递增这个结果数据索引是数值。
	 *                      默认值为：true
	 * @returns 要获取的原始信息属性值(可能为null），格式为：
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
	 *										resultDataIndex: ...
	 *									}
	 *									当data是数组时，将返回此结构的数组
	 */
	chartBase.originalInfo = function(data, chartDataSetIndex, resultDataIndex, autoIncrement)
	{
		var pname = chartFactory._ORIGINAL_DATA_INDEX_PROP_NAME;
		
		var isDataArray = $.isArray(data);
		
		//获取
		if(arguments.length == 1)
		{
			if(isDataArray)
			{
				var re = [];
				
				for(var i=0; i<data.length; i++)
					re.push(data[i][pname]);
				
				return re;
			}
			else
				return (data == null ? undefined : data[pname]);
		}
		//设置
		else
		{
			if(data == null)
				return;
			
			//(data, chartDataSetIndex, true)、(data, chartDataSetIndex, false)
			if(resultDataIndex === true || resultDataIndex === false)
			{
				autoIncrement = resultDataIndex;
				resultDataIndex = undefined;
			}
			
			resultDataIndex = (resultDataIndex === undefined ? 0 : resultDataIndex);
			
			var isCdsiArray = $.isArray(chartDataSetIndex);
			
			if(isCdsiArray)
			{
				var cdsiNew = [];
				
				for(var i=0; i<chartDataSetIndex.length; i++)
				{
					cdsiNew[i] = (chartDataSetIndex[i] != null && chartDataSetIndex[i].index !== undefined ?
									chartDataSetIndex[i].index : chartDataSetIndex[i]);
				}
				
				chartDataSetIndex = cdsiNew;
				
				if(!$.isArray(resultDataIndex))
				{
					var rdiNew = [];
					
					for(var i=0; i<chartDataSetIndex.length; i++)
						rdiNew[i] = resultDataIndex;
					
					resultDataIndex = rdiNew;
				}
			}
			else
			{
				chartDataSetIndex = (chartDataSetIndex != null && chartDataSetIndex.index !== undefined ?
										chartDataSetIndex.index : chartDataSetIndex);
			}
			
			if(isDataArray)
			{
				autoIncrement = (autoIncrement === undefined ? true : autoIncrement);
				var isRdiNumber = (typeof(resultDataIndex) == "number");
				
				var needAutoIncrementEle = (autoIncrement == true && $.isArray(resultDataIndex));
				if(needAutoIncrementEle == true)
				{
					needAutoIncrementEle = false;
					
					//任一元素是数值的话，才需要自增处理
					for(var i=0; i<resultDataIndex.length; i++)
					{
						if(typeof(resultDataIndex[i]) == "number")
						{
							needAutoIncrementEle = true;
							break;
						}
					}
				}
				
				for(var i=0; i<data.length; i++)
				{
					var originalInfo =
					{
						"chartId": this.id,
						"chartDataSetIndex": chartDataSetIndex
					};
					
					if(!autoIncrement)
					{
						originalInfo["resultDataIndex"] = resultDataIndex;
					}
					else
					{
						var resultDataIndexMy = resultDataIndex;
						
						if(isRdiNumber)
						{
							resultDataIndexMy = resultDataIndex + i;
						}
						else if(needAutoIncrementEle)
						{
							resultDataIndexMy = [];
							for(var j=0; j<resultDataIndex.length; j++)
							{
								resultDataIndexMy[j] = resultDataIndex[j];
								if(typeof(resultDataIndexMy[j]) == "number")
									resultDataIndexMy[j] = resultDataIndexMy[j] + i;
							}
						}
						
						originalInfo["resultDataIndex"] = resultDataIndexMy;
					}
					
					data[i][pname] = originalInfo;
				}
			}
			else
			{
				var originalInfo =
				{
					"chartId": this.id,
					"chartDataSetIndex": chartDataSetIndex,
					"resultDataIndex": resultDataIndex
				};
				
				data[pname] = originalInfo;
			}
		}
	};
	// > @deprecated 兼容3.0.1版本的API，将在未来版本移除，请使用chartBase.originalDataIndex()、chartBase.originalDataIndexes()
	
	// < @deprecated 兼容2.13.0版本的API，将在未来版本移除，请使用chartBase.resultOf()
	/**
	 * 返回第一个主件或者附件数据集结果，没有则返回undefined。
	 * 
	 * @param results
	 * @param attachment 可选，true 获取第一个附件图表数据集结果；false 获取第一个主件图表数据集结果。默认值为：false
	 */
	chartBase.resultFirst = function(results, attachment)
	{
		attachment = (attachment == null ? false : attachment);
		
		var index = undefined;
		
		var chartDataSets = this.chartDataSets;
		for(var i=0; i<chartDataSets.length; i++)
		{
			var isAttachment = chartDataSets[i].attachment;
			
			if((isAttachment && attachment == true) || (!isAttachment && attachment != true))
			{
				index = i;
				break;
			}
		}
		
		return (index == null ? undefined : this.resultAt(results, index));
	};
	// > @deprecated 兼容2.13.0版本的API，将在未来版本移除，请使用chartBase.resultOf()
	
	// < @deprecated 兼容2.13.0版本的API，将在未来版本移除，请使用chartBase.resultDatasOf()
	/**
	 * 获取第一个主件或者附件数据集结果的数据对象数组。
	 * 如果数据对象是null，返回空数组：[]；如果数据对象是数组，则直接返回；否则，返回：[ 数据对象 ]。
	 * 
	 * @param results 数据集结果数组
	 * @param attachment 可选，true 获取第一个附件图表数据集结果；false 获取第一个主件图表数据集结果。默认值为：false
	 * @return 不会为null的数组
	 */
	chartBase.resultDatasFirst = function(results, attachment)
	{
		var result = this.resultFirst(results, attachment);
		return this.resultDatas(result);
	};
	// > @deprecated 兼容2.13.0版本的API，将在未来版本移除，请使用chartBase.resultDatasOf()
	
	// < @deprecated 兼容2.13.0版本的API，将在未来版本移除，已被chartBase.chartDataSetMain()、chartDataSetAttachment()取代
	/**
	 * 获取第一个主件或者附件图表数据集对象。
	 * 
	 * @param attachment 可选，true 获取第一个附件图表数据集；false 获取第一个主件图表数据集。默认值为：false
	 * @return {...} 或  undefined
	 */
	chartBase.chartDataSetFirst = function(attachment)
	{
		attachment = (attachment == null ? false : attachment);
		
		var re = undefined;
		
		var chartDataSets = this.chartDataSets;
		for(var i=0; i<chartDataSets.length; i++)
		{
			var isAttachment = chartDataSets[i].attachment;
			
			if((isAttachment && attachment == true) || (!isAttachment && attachment != true))
			{
				re = chartDataSets[i];
				break;
			}
		}
		
		return re;
	};
	// > @deprecated 兼容2.13.0版本的API，将在未来版本移除，已被chartBase.chartDataSetMain()、chartDataSetAttachment()取代
	
	// < @deprecated 兼容2.9.0版本的API，将在未来版本移除，已被chartBase.hasDataSetParam()取代
	/**
	 * 判断此图表是否有参数化数据集。
	 */
	chartBase.hasParamDataSet = function()
	{
		return this.hasDataSetParam();
	};
	// > @deprecated 兼容2.9.0版本的API，将在未来版本移除，已被chartBase.hasDataSetParam()取代
	
	// < @deprecated 兼容2.9.0版本的API，将在未来版本移除，已被chartBase.dataSetPropertyAlias()取代
	/**
	 * 获取数据集属性标签，它不会返回null。
	 *  
	 * @param dataSetProperty
	 * @returns "..."
	 */
	chartBase.dataSetPropertyLabel = function(dataSetProperty)
	{
		if(!dataSetProperty)
			return "";
		
		var label = (dataSetProperty.label ||  dataSetProperty.name);
		
		return (label || "");
	};
	// > @deprecated 兼容2.9.0版本的API，将在未来版本移除，已被chartBase.dataSetPropertyAlias()取代
	
	// < @deprecated 兼容2.9.0版本的API，将在未来版本移除，已被chartBase.dataSetAlias()取代
	/**
	 * 获取指定图表数据集对象名称，它不会返回null。
	 * 
	 * @param chartDataSet 图表数据集对象
	 */
	chartBase.chartDataSetName = function(chartDataSet)
	{
		return this.dataSetAlias(chartDataSet);
	};
	// > @deprecated 兼容2.9.0版本的API，将在未来版本移除，已被chartBase.dataSetAlias()取代
	
	// < @deprecated 兼容2.7.0版本的API，将在未来版本移除，已被chartBase.registerEventHandlerDelegation()取代
	/**
	 * 图表事件支持函数：绑定图表事件处理函数代理。
	 * 注意：此函数在图表渲染完成后才可调用。
	 * 
	 * 图表事件处理通常由内部组件的事件处理函数代理（比如ECharts），并在代理函数中调用图表事件处理函数。
	 * 
	 * @param eventType
	 * @param eventHanlder 图表事件处理函数：function(chartEvent){ ... }
	 * @param eventHandlerDelegation 图表事件处理函数代理，负责构建chartEvent对象并调用eventHanlder
	 * @param delegationBinder 代理事件绑定器，格式为：{ bind: function(chart, eventType, eventHandlerDelegation){ ... } }
	 */
	chartBase.eventBindHandlerDelegation = function(eventType, eventHanlder,
			eventHandlerDelegation, delegationBinder)
	{
		this._assertActive();
		
		var delegations = chartFactory.extValueBuiltin(this, "eventHandlerDelegationsDeprecated");
		if(delegations == null)
		{
			delegations = [];
			chartFactory.extValueBuiltin(this, "eventHandlerDelegationsDeprecated", delegations);
		}
		
		delegationBinder.bind(this, eventType, eventHandlerDelegation);
		
		delegations.push({ eventType: eventType , eventHanlder: eventHanlder, eventHandlerDelegation: eventHandlerDelegation });
	};
	// > @deprecated 兼容2.7.0版本的API，将在未来版本移除，已被chartBase.registerEventHandlerDelegation()取代
	
	// < @deprecated 兼容2.7.0版本的API，将在未来版本移除，已被chartBase.removeEventHandlerDelegation()取代
	/**
	 * 图表事件支持函数：为图表解绑事件处理函数代理。
	 * 注意：此函数在图表渲染完成后才可调用。
	 * 
	 * @param eventType 事件类型
	 * @param eventHanlder 可选，要解绑的图表事件处理函数，不设置则解除所有指定事件类型的处理函数
	 * @param delegationUnbinder 代理事件解绑器，格式为：{ unbind: function(chart, eventType, eventHandlerDelegation){ ... } }
	 */
	chartBase.eventUnbindHandlerDelegation = function(eventType, eventHanlder, delegationUnbinder)
	{
		this._assertAlive();
		
		if(delegationUnbinder == undefined)
		{
			delegationUnbinder = eventHanlder;
			eventHanlder = undefined;
		}
		
		var delegations = chartFactory.extValueBuiltin(this, "eventHandlerDelegationsDeprecated");
		
		if(delegations == null)
			return;
		
		var unbindCount = 0;
		
		for(var i=0; i<delegations.length; i++)
		{
			var eh = delegations[i];
			var unbind = false;
			
			if(eventType == eh.eventType)
				unbind = (eventHanlder == undefined || (eh.eventHanlder == eventHanlder));
			
			if(unbind)
			{
				delegationUnbinder.unbind(this, eventType, eh.eventHandlerDelegation);
				delegations[i] = null;
				unbindCount++;
			}
		}
		
		if(unbindCount > 0)
		{
			var delegationsTmp = [];
			for(var i=0; i<delegations.length; i++)
			{
				if(delegations[i] != null)
					delegationsTmp.push(delegations[i]);
			}
			
			chartFactory.extValueBuiltin(this, "eventHandlerDelegationsDeprecated", delegationsTmp);
		}
	};
	// > @deprecated 兼容2.7.0版本的API，将在未来版本移除，已被chartBase.removeEventHandlerDelegation()取代
	
	// < @deprecated 兼容2.6.0版本的API，将在未来版本移除，已被chartBase.internal()取代
	/**
	 * ECharts图表支持函数：获取/设置图表的ECharts实例对象。
	 * 
	 * @param instance 可选，要设置的ECharts实例，不设置则执行获取操作
	 */
	chartBase.echartsInstance = function(instance)
	{
		return this.internal(instance);
	};
	// > @deprecated 兼容2.6.0版本的API，将在未来版本移除，已被chartBase.internal()取代
	
	// < @deprecated 兼容2.6.0版本的API，将在未来版本移除，已被renderOptions.processUpdateOptions取代（参考chart.inflateUpdateOptions()函数）
	/**
	 * 获取/设置图表更新时的图表选项，格式为： { ... }
	 * 当希望根据图表更新数据动态自定义图表选项时，可以在图表监听器的onUpdate函数中调用此函数设置更新图表选项。
	 * 
	 * 图表渲染器实现相关：
	 * 图表渲染器应使用此函数获取并应用更新图表选项（在其update函数中），另参考chart.inflateUpdateOptions()。
	 * 
	 * @param options 可选，要设置的图表选项，没有则执行获取操作
	 */
	chartBase.optionsUpdate = function(options)
	{
		return chartFactory.extValueBuiltin(this, "optionsUpdate", options);
	};
	// > @deprecated 兼容2.6.0版本的API，将在未来版本移除，已被renderOptions.processUpdateOptions取代（参考chart.inflateUpdateOptions()函数）
	
	// < @deprecated 兼容2.4.0版本的API，将在未来版本移除，已被chartBase.updateResults取代
	/**
	 * 获取用于此次更新图表的结果数据，没有则返回null。
	 */
	chartBase.getUpdateResults = function()
	{
		return this.updateResults();
	};
	// > @deprecated 兼容2.4.0版本的API，将在未来版本移除，已被chartBase.updateResults取代
	
	// < @deprecated 兼容2.3.0版本的API，将在未来版本移除，已被chartBase.renderer取代
	/**
	 * 获取/设置自定义图表渲染器。
	 * 
	 * @param customChartRenderer 可选，要设置的自定义图表渲染器，自定义图表渲染器允许仅定义要重写的内置图表插件渲染器函数
	 */
	chartBase.customChartRenderer = function(customChartRenderer)
	{
		return this.renderer(customChartRenderer);
	};
	// > @deprecated 兼容2.3.0版本的API，将在未来版本移除，已被chartBase.renderer取代
	
	// < @deprecated 兼容2.3.0版本的API，将在未来版本移除，已被chartBase.chartDataSets取代
	/**
	 * 获取所有图表数据集对象数组。
	 */
	chartBase.chartDataSetsNonNull = function()
	{
		return (this.chartDataSets || []);
	};
	// > @deprecated 兼容2.3.0版本的API，将在未来版本移除，已被chartBase.chartDataSets取代
	
	// < @deprecated 兼容2.3.0版本的API，将在未来版本移除，已被chartBase.name取代
	/**
	 * 获取图表名称。
	 */
	chartBase.nameNonNull = function()
	{
		return (this.name || "");
	};
	// > @deprecated 兼容2.3.0版本的API，将在未来版本移除，已被chartBase.name取代
	
	// < @deprecated 兼容2.3.0版本的API，将在未来版本移除，已被chartBase.updateInterval取代
	/**
	 * 获取图表的更新间隔。
	 */
	chartBase.updateIntervalNonNull = function()
	{
		if(this.updateInterval != null)
			return this.updateInterval;
		
		return -1;
	},
	// > @deprecated 兼容2.3.0版本的API，将在未来版本移除，已被chartBase.updateInterval取代
	
	// < @deprecated 兼容1.8.1版本的API，将在未来版本移除，已被chartBase.dataSetParamValues取代
	/**
	 * 获取指定图表数据集参数值对象。
	 */
	chartBase.getDataSetParamValues = function(chartDataSet)
	{
		return this.dataSetParamValues(chartDataSet);
	};
	// > @deprecated 兼容1.8.1版本的API，将在未来版本移除，已被chartBase.dataSetParamValues取代
	
	// < @deprecated 兼容1.8.1版本的API，将在未来版本移除，已被chartBase.dataSetParamValues取代
	/**
	 * 设置指定图表数据集多个参数值。
	 */
	chartBase.setDataSetParamValues = function(chartDataSet, paramValues)
	{
		this.dataSetParamValues(chartDataSet, paramValues);
	};
	// > @deprecated 兼容1.8.1版本的API，将在未来版本移除，已被chartBase.dataSetParamValues取代
	
	//-------------
	// > 已弃用函数 end
	//-------------
	
	//----------------------------------------
	// chartBase end
	//----------------------------------------
	
	/**
	 * 获取原始数据索引的原始数据。
	 * 
	 * @param chart 图表对象
	 * @param originalDataIndex 原始数据索引，格式为：{ ... }、[ { ... }, ... ]，具体参考chartBase.originalDataIndexes()函数返回值
	 * @since 3.1.0
	 */
	chartFactory.originalData = function(chart, originalDataIndex)
	{
		if(!originalDataIndex)
			return undefined;
		
		var originalData = [];
		
		var isArray = $.isArray(originalDataIndex);
		var originalDataIndexAry = (isArray ? originalDataIndex : [ originalDataIndex ]);
		
		for(var i=0; i<originalDataIndexAry.length; i++)
		{
			var odi = originalDataIndexAry[i];
			var chartDataSetIndex = odi.chartDataSetIndex;
			var resultDataIndex = odi.resultDataIndex;
			var results = chart.updateResults();
			var originalDataMy = null;
			
			if($.isArray(chartDataSetIndex))
			{
				originalDataMy = [];
				
				for(var j=0; j<chartDataSetIndex.length; j++)
				{
					var result = chart.resultAt(results, chartDataSetIndex[j]);
					originalDataMy[j] = chart.resultDataElement(result, (resultDataIndex != null ? resultDataIndex[j] : null));
				}
			}
			else
			{
				var result = chart.resultAt(results, chartDataSetIndex);
				originalDataMy = chart.resultDataElement(result, resultDataIndex);
			}
			
			originalData[i] = originalDataMy;
		}
		
		return (isArray ? originalData : originalData[0]);
	};
	
	/**
	 * 获取/设置单条图表展示数据的原始数据索引（图表ID、图表数据集索引、结果数据索引）。
	 * 
	 * @param data 展示数据，格式为：{ ... }、[ ... ]，对于设置操作，当展示数据是对象时，将为其添加一个额外属性；
	 * 			   当展示数据是数组时，如果末尾元素已是索引信息对象，则替换；否则，追加一个元素
	 * @param chartDataSetIndex 图表数据集索引数值、数值数组
	 * @param resultDataIndex 图表数据集结果数据索引数值、数值数组、数值数组的数组
	 * @returns 要获取的原始数据索引(可能为null），格式参考chartBase.originalDataIndexes()函数返回值
	 * @since 3.1.0
	 */
	chartFactory.originalDataIndex = function(data, chartId, chartDataSetIndex, resultDataIndex)
	{
		var pname = chartFactory._ORIGINAL_DATA_INDEX_PROP_NAME;
		var isArray = $.isArray(data);
		
		//获取
		if(arguments.length <= 1)
		{
			if(isArray)
			{
				var tailEle = (data.length > 0 ? data[data.length - 1] : null);
				return (tailEle && tailEle["chartId"] !== undefined && tailEle["chartDataSetIndex"] !== undefined 
							? tailEle : undefined);
			}
			else
				return (data == null ? undefined : data[pname]);
		}
		else
		{
			var originalIdx =
			{
				"chartId": chartId,
				"chartDataSetIndex": chartDataSetIndex,
				"resultDataIndex": resultDataIndex
			};
			
			if(isArray)
			{
				var tailEle = (data.length > 0 ? data[data.length - 1] : null);
				
				//替换
				if(tailEle && tailEle["chartId"] !== undefined && tailEle["chartDataSetIndex"] !== undefined)
				{
					data[data.length - 1] = originalIdx;
				}
				else
					data.push(originalIdx);
			}
			else
				data[pname] = originalIdx;
		}
	};
	
	/**
	 * 获取指定主题对象对应的CSS类名。
	 * 这个CSS类名是全局唯一的，可添加至HTML元素的"class"属性。
	 * 
	 * @param theme 主题对象，格式为：{ ... }
	 * @returns CSS类名，不会为null
	 */
	chartFactory.themeStyleName = function(theme)
	{
		var pn = chartFactory.builtinPropName("StyleName");
		var sn = theme[pn];
		
		if(!sn)
			sn = (theme[pn] = chartFactory.uid());
		
		return sn;
	};
	
	/**
	 * 判断/设置与指定主题和名称关联的CSS样式表。
	 * 对于设置操作，最终生成的样式表都会添加chartFactory.themeStyleName(theme)CSS类名选择器前缀，
	 * 确保样式表只会影响添加了chartFactory.themeStyleName(theme)样式类的HTML元素。
	 * 
	 * 同一主题和名称的CSS样式表，通常仅需创建一次，因此，当需要为某个HTML元素应用与主题相关的样式表时，通常使用方式如下：
	 * 
	 * var styleName = chartFactory.themeStyleSheet(theme, "myName", function(){ return CSS样式表对象、数组; });
	 * $(element).addClass(styleName);
	 * 
	 * 或者
	 * 
	 * if(!chartFactory.themeStyleSheet(theme, "myName"))
	 *   $(element).addClass(chartFactory.themeStyleSheet(theme, "myName", CSS样式表对象、数组));
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
	 * @returns 判断操作：true 已设置过；false 未设置过；设置操作：theme主题对应的CSS类名，即chartFactory.themeStyleName(theme)的返回值
	 */
	chartFactory.themeStyleSheet = function(theme, name, css, force)
	{
		var infoMap = theme[chartFactory._KEY_THEME_STYLE_SHEET_INFO];
		if(infoMap == null)
			infoMap = (theme[chartFactory._KEY_THEME_STYLE_SHEET_INFO] = {});
		
		var info = infoMap[name];
		
		if(css === undefined)
			return (info != null);
		
		var styleName = chartFactory.themeStyleName(theme);
		
		if(info && (force != true))
			return styleName;
		
		if(info == null)
			info = (infoMap[name] = { styleId: chartFactory.uid() });
		
		var cssText = "";
		
		if($.isFunction(css))
			css = css();
		
		if(!$.isArray(css))
			css = [ css ];
		
		var styleNameSelector = "." + styleName;
		
		for(var i=0; i<css.length; i++)
		{
			var cssName = css[i].name;
			var cssValue = css[i].value;
			
			if(cssName == null)
				continue;
			
			if(!$.isArray(cssName))
				cssName = [ cssName ];
			
			for(var j=0; j<cssName.length; j++)
			{
				cssText += styleNameSelector + cssName[j];
				
				if(j < (cssName.length - 1))
					cssText += ",\n";
			}
			
			cssText += "{\n";
			cssText += chartFactory.styleString(cssValue);
			cssText += "\n}\n";
		}
		
		chartFactory.styleSheetText(info.styleId, cssText);
		
		return styleName;
	};
	
	/**
	 * 添加主题关联实体。
	 */
	chartFactory.addThemeRefEntity = function(theme, entityId)
	{
		if(!theme)
			return;
		
		var entityIds = (theme[chartFactory._KEY_THEME_REF_ENTITY_IDS]
							|| (theme[chartFactory._KEY_THEME_REF_ENTITY_IDS] = {}));
		
		entityIds[entityId] = true;
	};
	
	/**
	 * 移除主题关联实体，并在没有关联时销毁样式表。
	 */
	chartFactory.removeThemeRefEntity = function(theme, entityId, destroyCss)
	{
		destroyCss = (destroyCss == null ? true : destroyCss);
		
		if(!theme)
			return;
		
		var entityIds = (theme[chartFactory._KEY_THEME_REF_ENTITY_IDS] || {});
		
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
				chartFactory.destroyThemeStyleSheet(theme);
			}
		}
	};
	
	/**
	 * 销毁主题关联创建的样式表。
	 */
	chartFactory.destroyThemeStyleSheet = function(theme)
	{
		if(!theme)
			return;
		
		var infoMap = theme[chartFactory._KEY_THEME_STYLE_SHEET_INFO];
		
		if(infoMap != null)
		{
			theme[chartFactory._KEY_THEME_STYLE_SHEET_INFO] = null;
			
			for(var name in infoMap)
			{
				var info = infoMap[name];
				var styleId = (info ? info.styleId : null);
				
				if(styleId)
					$("#" + styleId).remove();
			}
		}
	};
	
	/**
	 * 获取/设置HTML元素的CSS样式字符串（元素的style属性）。
	 * 
	 * 使用方式：
	 * chartFactory.elementStyle(element)
	 * chartFactory.elementStyle(element, "color:red;font-size:1.5em")
	 * chartFactory.elementStyle(element, {border:"1px solid red"}, "color:red;font-size:1.5em")
	 * chartFactory.elementStyle(element, "color:red;font-size:1.5em", {border:"1px solid red"}, "background:blue")
	 * chartFactory.elementStyle(element, ["color:red;font-size:1.5em", {border:"1px solid red"}], "background:blue")
	 * 
	 * @param element HTML元素、Jquery对象
	 * @param css 可选，要设置的CSS样式，格式为：同chartBase.styleString()函数参数
	 * @return 要获取的CSS样式字符串
	 */
	chartFactory.elementStyle = function(element, css)
	{
		element = $(element);
		
		if(css === undefined)
			return element.attr("style");
		
		var cssArray = [];
		
		for(var i=1; i<arguments.length; i++)
		{
			var cssi = arguments[i];
			
			if(!cssi)
				continue;
			
			cssArray = cssArray.concat(cssi);
		}
		
		var cssText = chartFactory.styleString(cssArray);
		
		element.attr("style", cssText);
	};
	
	/**
	 * 拼接CSS样式字符串。
	 * 
	 * 使用方式：
	 * chartFactory.styleString({color:"red", border:"1px solid red"})
	 * chartFactory.styleString({border:"1px solid red", padding:"1em 1em"}, "color:red;font-size:1.5em")
	 * chartFactory.styleString("color:red;font-size:1.5em", {border:"1px solid red", padding:"1em 1em"}, "background:blue")
	 * chartFactory.styleString(["color:red;font-size:1.5em", {border:"1px solid red", padding:"1em 1em"}], "background:blue")
	 * 
	 * @param css 要拼接的CSS样式，格式为：
	 *            字符串，例如："color:red;font-size:1.5em"
	 *            CSS属性对象，例如：{ color: "...", "backgroundColor": "...", "font-size": "...", ...  }，
	 *            数组，元素可以是字符串、CSS属性对象
	 *            或者是上述格式的变长参数
	 * @return 拼接后的CSS样式字符串，例如："color:red;background-color:red;font-size:1px;"
	 */
	chartFactory.styleString = function(css)
	{
		var cssText = "";
		
		var cssArray = [];
		
		for(var i=0; i<arguments.length; i++)
		{
			var cssi = arguments[i];
			
			if(!cssi)
				continue;
			
			cssArray = cssArray.concat(cssi);
		}
		
		for(var i=0; i<cssArray.length; i++)
		{
			var cssi = cssArray[i];
			var cssiText = "";
			
			if(!cssi)
				continue;
			
			if(typeof(cssi) == "string")
				cssiText = cssi;
			else
			{
				for(var name in cssi)
				{
					var value = cssi[name];
					var valueType = typeof(value);
					
					if(valueType == "string" || valueType == "number" || valueType == "boolean")
					{
						// < @deprecated 兼容2.8.0版本的相关驼峰命名的功能，将在未来版本移除
						name = chartFactory.toLegalStyleName(name);
						// > @deprecated 兼容2.8.0版本的相关驼峰命名的功能，将在未来版本移除
						
						cssiText += name + ":" + value + ";";
					}
				}
			}
			
			if(cssiText && cssText && cssText.charAt(cssText.length - 1) != ";")
				cssText += ";" + cssiText;
			else
				cssText += cssiText;
		}
		
		return cssText;
	};
	
	/**
	 * 将css字符串转换为对象。
	 */
	chartFactory.styleStringToObj = function(styleStr)
	{
		var re = {};
		
		if(styleStr)
		{
			var strs = styleStr.split(";");
			for(var i=0; i<strs.length; i++)
			{
				var str = $.trim(strs[i]);
				if(str)
				{
					var nv = str.split(":");
					var n = $.trim(nv[0] || "");
					var v = $.trim(nv[1] || "");
					
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
	chartFactory.builtinPropName = function(name)
	{
		return chartFactory._BUILT_IN_NAME_UNDERSCORE_PREFIX + name;
	};
	
	/**
	 * 获取/设置图表的内置扩展属性值。
	 * chart.extValue()是允许用户级使用的，此函数应用于内置设置/获取操作，可避免属性名冲突。
	 * 
	 * @param chart 图表对象
	 * @param name 扩展属性名
	 * @param value 可选，要设置的扩展属性值，不设置则执行获取操作
	 */
	chartFactory.extValueBuiltin = function(chart, name, value)
	{
		name = chartFactory.builtinPropName(name);
		return chart.extValue(name, value);
	};
	
	/**
	 * 获取/设置渲染上下文的属性值。
	 * 
	 * @param renderContext
	 * @param attrName
	 * @param attrValue 要设置的属性值，可选，不设置则执行获取操作
	 */
	chartFactory.renderContextAttr = function(renderContext, attrName, attrValue)
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
	chartFactory.renderContextAttrWebContext = function(renderContext, webContext)
	{
		return chartFactory.renderContextAttr(renderContext, renderContextAttrConst.webContext, webContext);
	};
	
	/**
	 * 获取/设置渲染上下文中的ChartTheme对象。
	 * 
	 * @param renderContext
	 * @param chartTheme 可选，要设置的ChartTheme
	 */
	chartFactory.renderContextAttrChartTheme = function(renderContext, chartTheme)
	{
		return chartFactory.renderContextAttr(renderContext, renderContextAttrConst.inflatedChartTheme, chartTheme);
	};
	
	/**
	 * 将给定URL转换为web上下文路径URL。
	 * 
	 * @param webContext web上下文
	 * @param url 待转换的URL
	 */
	chartFactory.toWebContextPathURL = function(webContext, url)
	{
		var contextPath = webContext.contextPath;
		
		if(url.indexOf("/") == 0)
			url = contextPath + url;
		
		return url;
	};
	
	/**
	 * 获取/设置HTML元素上的图表部件ID（"dg-chart-widget"属性值）。
	 * 
	 * @param element HTML元素、Jquery对象
	 * @param widgetId 选填参数，要设置的图表部件ID，不设置则执行获取操作
	 */
	chartFactory.elementWidgetId = function(element, widgetId)
	{
		element = $(element);
		
		if(widgetId === undefined)
		{
			return element.attr(chartFactory.elementAttrConst.WIDGET);
		}
		else
		{
			element.attr(chartFactory.elementAttrConst.WIDGET, widgetId);
		}
	};
	
	/**
	 * 获取当前在指定HTML元素上渲染的图表对象，返回null表示元素上并未渲染图表。
	 * 
	 * @param element HTML元素、Jquery选择器、Jquery对象
	 */
	chartFactory.renderedChart = function(element)
	{
		element = chartFactory.toJqueryObj(element);
		return element.data(chartFactory._KEY_ELEMENT_RENDERED_CHART);
	};
	
	/**
	 * 获取Jquery对象。
	 * 
	 * @param element HTML元素、HTML元素数组、Jquery选择器、Jquery对象
	 */
	chartFactory.toJqueryObj = function(element)
	{
		return $(element);
	};
	
	/**
	 * 执行JS代码。
	 * 
	 * @param str JS代码
	 * @param defaultValue 默认返回值，可选，默认为：undefined
	 */
	chartFactory.evalSilently = function(str, defaultValue)
	{
		var re = undefined;
		
		try
		{
			re = Function("return ("+str+");")();
		}
		catch(e)
		{
			chartFactory.logException(e);
		}
		
		return (re || defaultValue);
	};
	
	/**
	 * 将指定名称转换为合法的CSS样式属性名
	 * 例如："backgroundColor" 将被转换为 "background-color"
	 */
	chartFactory.toLegalStyleName = function(name)
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
	
	/**
	 * 获取主题从背景色（actualBackgroundColor）到前景色（color）之间的渐变因子对应的颜色。
	 * 这个颜色是实际背景色（actualBackgroundColor）与前景色（color）之间的某个颜色。
	 * 
	 * @param theme 主题对象，格式为：{ color: "...", actualBackgroundColor: "..." }
	 * @param factor 可选，渐变因子，0-1之间的小数，其中0表示最接近实际背景色的颜色、1表示最接近前景色的颜色
	 * @returns 与factor匹配的颜色字符串，格式类似："#FFFFFF"，如果未设置factor，将返回一个包含所有渐变颜色的数组
	 */
	chartFactory.themeGradualColor = function(theme, factor)
	{
		var gcs = theme[chartFactory._KEY_GRADUAL_COLORS];
		
		if(!gcs || gcs.length == 0)
		{
			gcs = this.evalGradualColors(theme.actualBackgroundColor, theme.color, (theme.gradient || 20));
			theme[chartFactory._KEY_GRADUAL_COLORS] = gcs;
		}
		
		if(factor == null)
			return gcs;
		else
		{
			var index = parseInt((gcs.length-1) * factor);
			
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
	chartFactory.evalGradualColors = function(start, end, count, rgb)
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
				color = chartFactory.colorToHexStr(color, true);
			
			colors.push(color);
		}
		
		return colors;
	};
	
	/**
	 * 将颜色转换为6位HEX字符串。
	 * 
	 * @param color 颜色字符串，格式为："#FFF"、"#FFFFFF"、"rgb(255,255,255)"
	 * @param prefix 可选，是否添加"#"前缀
	 * @returns 6位HEX字符串，格式示例："FFFFFF"
	 */
	chartFactory.colorToHexStr = function(color, prefix)
	{
		if(color == null)
			return "";
		
		if(chartFactory.isString(color))
		{
			color = chartFactory.parseColor(color);
		}
		prefix = (prefix == null ? false : prefix);
		
		var r = new Number(color.r).toString(16);
		var g = new Number(color.g).toString(16);
		var b = new Number(color.b).toString(16);
		
		color = (prefix ? "#" : "") + (r.length == 1 ? "0"+r : r)
					 + (g.length == 1 ? "0"+g : g)
					  + (b.length == 1 ? "0"+b : b);
		
		return color;
	};
	
	/**
	 * 解析颜色对象。
	 * 将颜色字符串解析为{r: number, g: number, b: number}格式的对象。
	 * 
	 * @param color 颜色字符串，格式为："#FFF"、"#FFFFFF"、"rgb(255,255,255)"
	 */
	chartFactory.parseColor = function(color)
	{
		var re = {r: 0, g: 0, b: 0};
		
		if(!color)
			return re;
		
		//是颜色名称，则通过元素css函数转换
		if((color.charAt(0) != '#') && (color.indexOf("(") < 0))
		{
			var elementId = (chartFactory._ELEMENT_ID_FOR_CVT_COLOR == null ?
								(chartFactory._ELEMENT_ID_FOR_CVT_COLOR = chartFactory.uid()) : chartFactory._ELEMENT_ID_FOR_CVT_COLOR);
			
			var $colorEle = $("#"+elementId);
			if($colorEle.length == 0)
				$colorEle = $("<div id='"+elementId+"' style='display:none;position:absolute;left:0;bottom:0;width:0;height:0;z-index:-999;'></div>")
								.appendTo(document.body);
			
			$colorEle.css("color", color);
			color = $colorEle.css("color");
		}
		
		// #FFF、#FFFFFF
		if(color.charAt(0) == '#')
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
		}
		// rgb()
		else
		{
			var si = color.indexOf("(");
			var ei = (si >= 0 ? color.indexOf(")", si+1) : -1);
			
			if(ei > si)
			{
				color = color.substring(si+1, ei).split(",");
				
				if(color.length >= 1)
					re.r = parseInt(color[0]);
				if(color.length >= 2)
					re.g = parseInt(color[1]);
				if(color.length >= 3)
					re.b = parseInt(color[2]);
			}
		}
		
		return re;
	};
	
	/**
	 * 设置指定ID的样式表css文本。
	 * 如果样式表不存在，将会自动创建，且会插入<head>中的靠前位置，确保其css效果优先级低于用户定义的css。
	 *
	 * @param styleId 样式表元素ID
	 * @param cssText css文本内容
	 */
	chartFactory.styleSheetText = function(styleId, cssText)
	{
		var $style = $("#" + styleId);
		
		if($style.length > 0)
		{
			$style.text(cssText);
			return;
		}
		
		$style = $("<style />").attr("id", styleId)
			.attr("dg-generated-style", "true").attr("type", "text/css").text(cssText);
		
		var $head = $("head:first");
		
		var $lastGenStyle = $("style[dg-generated-style]:last", $head);
		
		//后插入的优先级应高于先插入的
		if($lastGenStyle.length > 0)
		{
			$lastGenStyle.after($style);
			return;
		}
		
		var $lastImport = $("[dg-import-name]:last", $head);
		
		//优先级应高于导入的资源
		if($lastImport.length > 0)
		{
			$lastImport.after($style);
			return;
		}
		
		var $lastLink = $("link:last", $head);
		
		//优先级应高于link的css
		if($lastLink.length > 0)
		{
			$lastLink.after($style);
			return;
		}
		
		$head.prepend($style);
	};
	
	/**
	 * 判断给定CSS样式表是否已创建。
	 * 
	 * @param id 样式表元素ID
	 */
	chartFactory.isStyleSheetCreated = function(id)
	{
		var style = document.getElementById(id);
		
		return (style != null && style.type == "text/css");
	};
	
	/**
	 * 生成一个新的页面元素ID。
	 * 这个ID仅包含[a-z]、[A-Z]、[0-9]，且以字母开头。
	 */
	chartFactory.uid = function()
	{
		if(this._uid_seq >= Number.MAX_SAFE_INTEGER)
		{
			this._uid_seq = null;
			this._uid_time = null;
		}
		
		var seq = (this._uid_seq == null ? (this._uid_seq = 0) : this._uid_seq);
		var time = (this._uid_time == null ? (this._uid_time = chartFactory.currentDateMs().toString(16)) : this._uid_time);
		this._uid_seq++;
		
		return this._BUILT_IN_NAME_PART + time + seq;
	};
	
	/**
	 * 获取当前日期毫秒数。
	 */
	chartFactory.currentDateMs = function()
	{
		return new Date().getTime();
	};
	
	/**
	 * 获取/设置父元素的派生子元素DOM数组，派生子元素并不是父元素的直接子孙元素，但是从属于父元素生命周期，随父元素创建，也应随父元素删除。
	 *
	 * @param parent 父DOM元素、JQ对象
	 * @param derived 可选，要设置的派生子元素DOM、DOM数组、JQ对象、null
	 * @param append 可选，当执行设置操作时，是否追加而非覆盖，默认为：true
	 */
	chartFactory.derivedElements = function(parent, derived, append)
	{
		parent = $(parent);
		
		var name = chartFactory.builtinPropName("derivedElements");
		
		if(derived === undefined)
			return parent.data(name);
		
		append = (append == null ? true : append);
		
		if(derived == null)
			parent.removeData(name);
		else
		{
			derived = $(derived);
			
			var des = parent.data(name);
			if(des == null || !append)
			{
				des = [];
				parent.data(name, des);
			}
			
			derived.each(function()
			{
				des.push(this);
			});
		}
	};
	
	/**
	 * 删除元素，同时删除通过chartFactory.derivedElements()设置的派生子元素。
	 * 
	 * @param ele 要删除的DOM元素、DOM元素数组、JQ对象
	 */
	chartFactory.removeElementWithDerived = function(ele)
	{
		ele = $(ele);
		
		ele.each(function()
		{
			var des = (chartFactory.derivedElements(this) || []);
			
			for(var i=0; i<des.length; i++)
				chartFactory.removeElementWithDerived(des[i]);
			
			$(this).remove();
		});
	};
	
	/**
	 * 元素是否是"position:static"的。
	 */
	chartFactory.isStaticPosition = function(ele)
	{
		ele = $(ele);
		
		var p = ele.css("position");
		
		if(!p || p == "static")
			return true;
		else if(p == "inherit")
			return this.isStaticPosition(ele.parent());
		else
			return false;
	};
	
	/**
	 * 将给定值按照HTML规范转义，如果不是字符串，直接返回原值。
	 */
	chartFactory.escapeHtml = function(value)
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
	
	chartFactory.toCssFontSize = function(fontSize)
	{
		if(fontSize == null || fontSize == "")
		{
			//返回一个无效的css字号值，使其不影响其他层级字号设置
			return "null";
		}
		else if($.isNumeric(fontSize))
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
	chartFactory.logException = function(exception)
	{
		if(typeof(console) != "undefined")
		{
			if(console.error)
				console.error(exception);
			else if(console.warn)
				console.warn(exception);
			else if(console.info)
				console.info(exception);
		}
	};
	
	/**
	 * 记录警告日志。
	 * 
	 * @param exception 警告消息字符串
	 */
	chartFactory.logWarn = function(exception)
	{
		if(typeof(console) != "undefined")
		{
			if(console.warn)
				console.warn(exception);
			else if(console.info)
				console.info(exception);
		}
	};
	
	chartFactory.isString = function(v)
	{
		return (typeof(v) == "string");
	};
	
	chartFactory.isNumber = function(v)
	{
		return (typeof(v) == "number");
	};
	
	chartFactory.isStringOrNumber = function(v)
	{
		var type = typeof(v);
		return (type == "string" || type == "number");
	};
	
	chartFactory.toJsonString = function(obj)
	{
		return JSON.stringify(obj);
	};
	
	chartFactory.isJsonString = function(str)
	{
		//以'{'或'['开头
		return (chartFactory.isString(str) && /^\s*[\{\[]/.test(str));
	};
	
	//是否是DOM元素或Jquery对象
	chartFactory.isDomOrJquery = function(obj)
	{
		return (obj && ((obj.nodeType != null && obj.nodeName != null) || (obj instanceof jQuery)));
	};
	
	/**内置名字标识片段*/
	chartFactory._BUILT_IN_NAME_PART = "datagear";
	
	/**内置名字标识片段*/
	chartFactory._BUILT_IN_NAME_UNDERSCORE_PREFIX = "_" + chartFactory._BUILT_IN_NAME_PART;
	
	/**图表展示数据对象的原始信息属性名*/
	chartFactory._ORIGINAL_DATA_INDEX_PROP_NAME = chartFactory._BUILT_IN_NAME_UNDERSCORE_PREFIX + "OriginalDataIndex";
	
	/**图表主题关联的实体ID属性名*/
	chartFactory._KEY_THEME_REF_ENTITY_IDS = chartFactory._BUILT_IN_NAME_UNDERSCORE_PREFIX + "RefEntityIds";
	
	/**图表主题的CSS信息属性名*/
	chartFactory._KEY_THEME_STYLE_SHEET_INFO = chartFactory._BUILT_IN_NAME_UNDERSCORE_PREFIX + "StyleSheetInfo";
	
	/** 关键字：注册得ECharts主题名 */
	chartFactory._KEY_REGISTERED_ECHARTS_THEME_NAME = chartFactory._BUILT_IN_NAME_UNDERSCORE_PREFIX + "RegisteredEchartsThemeName";
	
	/** 关键字：可作为定位父元素的样式类名 */
	chartFactory._KEY_CHART_ELEMENT_STYLE_FOR_RELATIVE = "dg-position-relative";
	
	/** HTML元素上已渲染的图表对象KEY */
	chartFactory._KEY_ELEMENT_RENDERED_CHART = chartFactory._BUILT_IN_NAME_UNDERSCORE_PREFIX + "RenderedChart";
	
	/** 关键字：渐变色数组 */
	chartFactory._KEY_GRADUAL_COLORS = chartFactory._BUILT_IN_NAME_UNDERSCORE_PREFIX + "GradualColors";
	
	/**
	 * 将指定图表主题填充为全局图表主题，即使用<body>上的dg-chart-theme属性值填充。
	 * 如果图表主题已经被此函数填充过，不会再次处理。
	 * 
	 * @param theme 图表主题，会被此函数修改
	 */
	chartFactory._inflateGlobalChartTheme = function(theme)
	{
		if(chartFactory._themeInflated(theme))
			return false;
		
		chartFactory._inflateActualBgColorIf(theme);
		
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
		
		chartFactory._inflateActualBgColorIf(theme);
		
		var bodyThemeValue = $(document.body).attr(elementAttrConst.THEME);
		if(bodyThemeValue)
		{
			var bodyThemeObj = chartFactory.evalSilently(bodyThemeValue, {});
			
			//如果是引用变量，不应被修改
			if(!chartFactory.isJsonString(bodyThemeValue))
				bodyThemeObj = $.extend(true, {}, bodyThemeObj);
			
			chartFactory._inflateActualBgColorIf(bodyThemeObj);
			
			rawTheme = $.extend(true, {}, theme, bodyThemeObj);
			
			chartFactory._inflateChartThemeIf(bodyThemeObj);
			
			// < @deprecated 兼容1.5.0版本的自定义ChartTheme结构，未来版本会移除
			if(bodyThemeObj.colorSecond)
			{
				bodyThemeObj.color = bodyThemeObj.colorSecond;
				bodyThemeObj.titleColor = bodyThemeObj.color;
				bodyThemeObj.legendColor = bodyThemeObj.colorSecond;
			}
			// > @deprecated 兼容1.5.0版本的自定义ChartTheme结构，未来版本会移除
			
			$.extend(true, theme, bodyThemeObj);
		}
		
		if(rawTheme == null)
			rawTheme = $.extend(true, {}, theme);
		
		chartFactory._inflateChartThemeIf(theme);
		
		theme._RAW_CHART_THEME = rawTheme;
		chartFactory._themeInflated(theme, true);
		
		return true;
	};
	
	chartFactory._themeInflated = function(theme, inflated)
	{
		if(inflated === undefined)
			return (theme._INFLATED == true);
		else
			theme._INFLATED = inflated;
	};
	
	chartFactory._inflateActualBgColorIf = function(theme)
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
	chartFactory._inflateChartThemeIf = function(theme)
	{
		if(!theme.actualBackgroundColor)
			chartFactory._inflateActualBgColorIf(theme);
		
		if(theme.color && theme.actualBackgroundColor)
		{
			// < @deprecated 兼容2.13.0版本ChartTheme的titleColor、legendColor结构，未来版本会移除
			if(!theme.titleColor)
				theme.titleColor = theme.color;
			if(!theme.legendColor)
				theme.legendColor = chartFactory.themeGradualColor(theme, 0.9);
			// > @deprecated 兼容2.13.0版本ChartTheme的titleColor、legendColor结构，未来版本会移除
			
			if(!theme.borderColor)
				theme.borderColor = chartFactory.themeGradualColor(theme, 0.3);
			
			var titleThemeGen =
			{
				name: "titleTheme",
				color: theme.color,
				backgroundColor: "transparent",
				borderColor: theme.borderColor,
				borderWidth: 0
			};
			
			// < @deprecated 兼容2.13.0版本ChartTheme的titleColor结构，未来版本会移除
			if(theme.titleColor)
				titleThemeGen.color = theme.titleColor;
			// > @deprecated 兼容2.13.0版本ChartTheme的titleColor结构，未来版本会移除
			
			theme.titleTheme = (!theme.titleTheme ? titleThemeGen : $.extend(true, titleThemeGen, theme.titleTheme));
			
			// < @deprecated 兼容2.13.0版本ChartTheme的titleColor结构，未来版本会移除
			theme.titleColor = theme.titleTheme.color;
			// > @deprecated 兼容2.13.0版本ChartTheme的titleColor结构，未来版本会移除
			
			var legendThemeGen =
			{
				name: "legendTheme",
				color: chartFactory.themeGradualColor(theme, 0.9),
				backgroundColor: "transparent",
				borderColor: theme.borderColor,
				borderWidth: 0
			};
			
			// < @deprecated 兼容2.13.0版本ChartTheme的legendColor结构，未来版本会移除
			if(theme.legendColor)
				legendThemeGen.color = theme.legendColor;
			// > @deprecated 兼容2.13.0版本ChartTheme的legendColor结构，未来版本会移除
			
			theme.legendTheme = (!theme.legendTheme ? legendThemeGen : $.extend(true, legendThemeGen, theme.legendTheme));
			
			// < @deprecated 兼容2.13.0版本ChartTheme的legendColor结构，未来版本会移除
			theme.legendColor = theme.legendTheme.color;
			// > @deprecated 兼容2.13.0版本ChartTheme的legendColor结构，未来版本会移除
			
			var tooltipThemeGen =
			{
				name: "tooltipTheme",
				color: theme.actualBackgroundColor,
				backgroundColor: chartFactory.themeGradualColor(theme, 0.7),
				borderColor: chartFactory.themeGradualColor(theme, 0.9),
				borderWidth: 1
			};
			
			theme.tooltipTheme = (!theme.tooltipTheme ? tooltipThemeGen : $.extend(true, tooltipThemeGen, theme.tooltipTheme));
			
			var highlightThemeGen =
			{
				name: "highlightTheme",
				color: theme.actualBackgroundColor,
				backgroundColor: chartFactory.themeGradualColor(theme, 0.8),
				borderColor: chartFactory.themeGradualColor(theme, 1),
				borderWidth: 1
			};
			
			theme.highlightTheme = (!theme.highlightTheme ? highlightThemeGen : $.extend(true, highlightThemeGen, theme.highlightTheme));
		}
		else if(theme.color)
		{
			// < @deprecated 兼容2.13.0版本ChartTheme的titleColor、legendColor结构，未来版本会移除
			if(!theme.titleColor)
				theme.titleColor = theme.color;
			if(!theme.legendColor)
				theme.legendColor = theme.color;
			// > @deprecated 兼容2.13.0版本ChartTheme的titleColor、legendColor结构，未来版本会移除
			
			var titleThemeGen =
			{
				name: "titleTheme",
				color: theme.color,
				backgroundColor: "transparent",
				borderColor: theme.borderColor,
				borderWidth: 0
			};
			
			// < @deprecated 兼容2.13.0版本ChartTheme的titleColor结构，未来版本会移除
			if(theme.titleColor)
				titleThemeGen.color = theme.titleColor;
			// > @deprecated 兼容2.13.0版本ChartTheme的titleColor结构，未来版本会移除
			
			theme.titleTheme = (!theme.titleTheme ? titleThemeGen : $.extend(true, titleThemeGen, theme.titleTheme));
			
			// < @deprecated 兼容2.13.0版本ChartTheme的titleColor结构，未来版本会移除
			theme.titleColor = theme.titleTheme.color;
			// > @deprecated 兼容2.13.0版本ChartTheme的titleColor结构，未来版本会移除
			
			var legendThemeGen =
			{
				name: "legendTheme",
				color: theme.color,
				backgroundColor: "transparent",
				borderColor: theme.borderColor,
				borderWidth: 0
			};
			
			// < @deprecated 兼容2.13.0版本ChartTheme的legendColor结构，未来版本会移除
			if(theme.legendColor)
				legendThemeGen.color = theme.legendColor;
			// > @deprecated 兼容2.13.0版本ChartTheme的legendColor结构，未来版本会移除
			
			theme.legendTheme = (!theme.legendTheme ? legendThemeGen : $.extend(true, legendThemeGen, theme.legendTheme));
			
			// < @deprecated 兼容2.13.0版本ChartTheme的legendColor结构，未来版本会移除
			theme.legendColor = theme.legendTheme.color;
			// > @deprecated 兼容2.13.0版本ChartTheme的legendColor结构，未来版本会移除
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
	chartFactory.buildEchartsTheme = function(chartTheme)
	{
		var axisColor = chartFactory.themeGradualColor(chartTheme, 0.7);
		var axisScaleLineColor = chartFactory.themeGradualColor(chartTheme, 0.35);
		var areaColor0 = chartFactory.themeGradualColor(chartTheme, 0.15);
		var areaBorderColor0 = chartFactory.themeGradualColor(chartTheme, 0.3);
		var areaColor1 = chartFactory.themeGradualColor(chartTheme, 0.25);
		var areaBorderColor1 = chartFactory.themeGradualColor(chartTheme, 0.5);
		var shadowColor = chartFactory.themeGradualColor(chartTheme, 0.9);
		
		// < @deprecated 兼容1.8.1版本有ChartTheme.axisColor的结构
		if(chartTheme.axisColor)
			axisColor = chartTheme.axisColor;
		// > @deprecated 兼容1.8.1版本有ChartTheme.axisColor的结构
		
		// < @deprecated 兼容1.8.1版本有ChartTheme.axisScaleLineColor的结构
		if(chartTheme.axisScaleLineColor)
			axisScaleLineColor = chartTheme.axisScaleLineColor;
		// > @deprecated 兼容1.8.1版本有ChartTheme.axisScaleLineColor的结构
		
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
				"label":
				{
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
				"label":
				{
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
					"color": chartFactory.themeGradualColor(chartTheme, 0),
					"borderColor": chartFactory.themeGradualColor(chartTheme, 0.1)
				}
			},
			"scatter" : {
				"itemStyle" : {
					"borderWidth" : 0,
					"borderColor" : chartTheme.borderColor,
					"shadowBlur" : 3,
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
					"areaColor" : areaColor1,
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
					"areaColor" : areaColor1,
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
	
	//-------------
	// < 已弃用函数 start
	//-------------
	
	// < @deprecated 兼容2.3.0版本的API，将在未来版本移除，已被dashboardBase.renderedChart取代
	/**
	 * 判断指定HTML元素是否是已渲染为图表。
	 * 
	 * @param element HTML元素、Jquery对象
	 */
	chartFactory.isChartElement = function(element)
	{
		return (this.renderedChart(element) != null);
	};
	// > @deprecated 兼容2.3.0版本的API，将在未来版本移除，已被dashboardBase.renderedChart取代
	
	//-------------
	// > 已弃用函数 end
	//-------------
})
(this);
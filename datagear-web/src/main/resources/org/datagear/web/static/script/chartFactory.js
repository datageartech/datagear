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
 * 此图表工厂支持为<body>元素、图表元素添加elementAttrConst.LISTENER属性来设置图表监听器，格式参考chartBase.listener函数参数说明。
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
 *   update: function(chart, chartResult)
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
	
	/**内置图表选项名定义，所有内置图表选项名都应定义于此，便于因名字冲突需要重新定义*/
	var builtinOptionNames = (chartFactory.builtinOptionNames || (chartFactory.builtinOptionNames = {}));
	
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
	
	/** 内置图表选项名：是否美化滚动条 */
	builtinOptionNames.beautifyScrollbar = "beautifyScrollbar";
	/** 内置图表选项名：处理图表渲染选项 */
	builtinOptionNames.processRenderOptions = "processRenderOptions";
	/** 内置图表选项名：处理图表更新选项 */
	builtinOptionNames.processUpdateOptions = "processUpdateOptions";
	/** 内置图表选项名：更新追加模式 */
	builtinOptionNames.updateAppendMode = "updateAppendMode";
	
	/** 图表标识样式名，所有已绘制的图表元素都会添加此样式名 */
	chartFactory.CHART_STYLE_NAME_FOR_INDICATION = "dg-chart-for-indication";
	
	/** 看板引入库标识属性，同：org.datagear.analysis.support.html.HtmlTplDashboardWidgetRenderer.DASHBOARD_LIB_NAME_ATTR */
	chartFactory.LIB_ATTR_NAME = "dg-lib-name";
	
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
		chart.updateInterval = (chart.updateInterval == null ? -1 : chart.updateInterval);
		chart.dataSetBinds = (chart.dataSetBinds || []);
		for(var i=0; i<chart.dataSetBinds.length; i++)
		{
			var cds = chart.dataSetBinds[i];
			cds.fieldSigns = (cds.fieldSigns || {});
			cds.alias = (cds.alias == null ?  "" : cds.alias);
			cds.attachment = (cds.attachment == true ? true : false);
			cds.query = (cds.query || {});
			cds.query.paramValues = (cds.query.paramValues || {});
			//为dataSetBinds元素添加index属性，便于后续根据其索引获取结果集等信息
			cds.index = i;
			
			// < @deprecated 兼容2.4.0版本的dataSetBinds.paramValues，将在未来版本移除，已被dataSetBinds.query.paramValues取代
			cds.paramValues = cds.query.paramValues;
			// > @deprecated 兼容2.4.0版本的dataSetBinds.paramValues，将在未来版本移除，已被dataSetBinds.query.paramValues取代
			
			// < @deprecated 兼容5.0.0版本的DataSetBind.propertySigns，将在未来版本移除，已被DataSetBind.fieldSigns取代
			cds.propertySigns = cds.fieldSigns;
			// > @deprecated 兼容5.0.0版本的DataSetBind.propertySigns，将在未来版本移除，已被DataSetBind.fieldSigns取代
			
			// < @deprecated 兼容5.0.0版本的DataSetBind.propertyAliases，将在未来版本移除，已被DataSetBind.fieldAliases取代
			cds.propertyAliases = cds.fieldAliases;
			// > @deprecated 兼容5.0.0版本的DataSetBind.propertyAliases，将在未来版本移除，已被DataSetBind.fieldAliases取代
			
			// < @deprecated 兼容5.0.0版本的DataSetBind.propertyOrders，将在未来版本移除，已被DataSetBind.fieldOrders取代
			cds.propertyOrders = cds.fieldOrders;
			// > @deprecated 兼容5.0.0版本的DataSetBind.propertyOrders，将在未来版本移除，已被DataSetBind.fieldOrders取代
			
			// < @deprecated 兼容5.0.0版本的DataSetBind.dataSet.properties，将在未来版本移除，已被DataSetBind.dataSet.fields取代
			if(cds.dataSet)
			{
				cds.dataSet.properties = cds.dataSet.fields
			}
			// > @deprecated 兼容5.0.0版本的DataSetBind.dataSet.properties，将在未来版本移除，已被DataSetBind.dataSet.fields取代
		}
		
		chart._dataSetBinds = (chart.dataSetBinds || []);
		delete chart.dataSetBinds;
		
		// < @deprecated 兼容4.7.0版本的chart.chartDataSets，将在未来版本移除，已被chart._dataSetBinds取代
		chart.chartDataSets = chart._dataSetBinds;
		// > @deprecated 兼容4.7.0版本的chart.chartDataSets，将在未来版本移除，已被chart._dataSetBinds取代
		
		chart._attrValues = (chart.attrValues || {});
		chart._options = (chart._options || {});
		
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
		
		// < @deprecated 兼容4.7.0版本的dg-chart-map功能，将在未来版本移除，请使用chartSupport中的builtinOptionNames.mapName图表选项
		this._initMap();
		// > @deprecated 兼容4.7.0版本的dg-chart-map功能，将在未来版本移除，请使用chartSupport中的builtinOptionNames.mapName图表选项
		
		this._initEchartsThemeName();
		this._initDisableSetting();
		this._initEventHandlers();
		this._initRenderer();
		this._initAttrValues();
		this._initUpdateAppendMode();
		
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
				update: function(chart, chartResult)
				{
					var dl = this._findListenerOfFunc("update");
					
					if(dl)
						return dl.update(chart, chartResult);
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
				onUpdate: function(chart, chartResult)
				{
					var dl = this._findListenerOfFunc("onUpdate");
					
					if(dl)
						return dl.onUpdate(chart, chartResult);
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
		
		if(chartFactory.isNullOrEmpty(settingAttr))
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
	 * 初始化更新追加模式。
	 */
	chartBase._initUpdateAppendMode = function()
	{
		var options = this.options();
		var mode = (options ? options[builtinOptionNames.updateAppendMode] : null);
		
		// < @deprecated 兼容5.2.0版本的dgUpdateAppendMode选项，将在未来版本移除
		if(mode == null)
		{
			mode = (options ? options["dgUpdateAppendMode"] : null);
		}
		// > @deprecated 兼容5.2.0版本的dgUpdateAppendMode选项，将在未来版本移除
		
		this.updateAppendMode(mode);
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
	 * @returns 要获取的图表选项，格式为：{ ... }，不会为null
	 */
	chartBase.options = function(options)
	{
		if(options === undefined)
		{
			if(this._options == null)
				this._options = {};
			
			return this._options;
		}
		else
		{
			if(options == null)
			{
				throw new Error("[options] required");
			}
			
			this._options = options;
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
	 */
	chartBase.theme = function(theme)
	{
		if(theme === undefined)
		{
			return this._theme;
		}
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
	 */
	chartBase.listener = function(listener)
	{
		if(listener === undefined)
			return this._listener;
		else
			this._listener = listener;
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
		
		var lib = this._rendererLib();
		
		if(lib)
		{
			var contextCharts = this._contextCharts();
			var thisChart = this;
			
			chartFactory.loadLib(lib, function()
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
	
	chartBase._contextCharts = function()
	{
		return [];
	};
	
	chartBase._renderInner = function()
	{
		var doRender = true;
		
		var listener = this.listener();
		if(listener && listener.onRender)
			doRender = listener.onRender(this);
		
		if(doRender != false)
		{
			this.doRender();
		}
	};
	
	chartBase._rendererLib = function()
	{
		//优先
		var lib = chartFactory.rendererLib(this.renderer());
		
		//其次
		if(lib == null)
		{
			lib = chartFactory.rendererLib(this.plugin.renderer);
			lib = chartFactory.convertPluginRendererLib(this, lib);
		}
		
		return lib;
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
		//必须添加相对定位样式
		$element.addClass(chartFactory._KEY_CHART_ELEMENT_STYLE_FOR_RELATIVE);
		chartFactory.addThemeRefEntity(theme, this.id);
		this._createChartThemeCssIfNon();
		$element.addClass(this.themeStyleName());
		
		var options = this.options();
		if(!options || options[builtinOptionNames.beautifyScrollbar] != false)
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
	 * @param chartResult 可选，图表结果、数据集结果数组，如果不设置，将使用this.updateResult()的返回值
	 */
	chartBase.update = function(chartResult)
	{
		if(!this.statusRendered() && !this.statusPreUpdate() && !this.statusUpdated())
			throw new Error("chart is illegal state for update()");
		
		// < @deprecated 兼容5.3.1版本的逻辑，将在未来版本移除，chartResult可能在后续逻辑中被修改，不应再次重复使用
		if(chartResult === undefined)
		{
			chartResult = this.updateResult();
		}
		// > @deprecated 兼容5.3.1版本的逻辑，将在未来版本移除，chartResult可能在后续逻辑中被修改，不应再次重复使用
		
		//内部统一结构
		chartResult = this._toChartResult(chartResult);
		
		this.statusUpdating(true);
		
		var appendMode = this.updateAppendMode();
		if(appendMode && appendMode.beforeListener)
		{
			chartResult = this._appendUpdateResult(chartResult, appendMode);
		}
		
		var doUpdate = true;
		
		var listener = this.listener();
		if(listener && listener.onUpdate)
			doUpdate = listener.onUpdate(this, this._toApiStdResult(chartResult));
		
		if(doUpdate != false)
		{
			this.doUpdate(chartResult);
		}
	};
	
	/**
	 * 调用底层图表渲染器的update函数，执行更新数据。
	 * 
	 * @param chartResult 图表结果、数据集结果数组
	 */
	chartBase.doUpdate = function(chartResult)
	{
		if(!this.statusUpdating())
			throw new Error("chart is illegal state for doUpdate()");
		
		//内部统一结构
		chartResult = this._toChartResult(chartResult);
		
		var appendMode = this.updateAppendMode();
		if(appendMode && !appendMode.beforeListener)
		{
			chartResult = this._appendUpdateResult(chartResult, appendMode);
		}
		
		//先保存结果，确保updateResult()在渲染器的update函数作用域内可用
		this.updateResult(chartResult);
		
		var async = this.isAsyncUpdate(chartResult);
		var renderer = this.renderer();
		
		if(renderer && renderer.update)
		{
			renderer.update(this, this._toApiStdResult(chartResult));
		}
		else
		{
			this.plugin.renderer.update(this, this._toApiStdResult(chartResult));
		}
		
		if(!async)
			this.statusUpdated(true);
	};
	
	//将上次更新结果前置合并至给定图表的结果
	//只要设置了appendMode，此方法旧不会返回null，确保后续不会出现空指针问题
	chartBase._appendUpdateResult = function(chartResult, appendMode)
	{
		if(!appendMode)
			return chartResult;
		
		var oldChartResult = this.updateResult();
		
		var olds = (this.results(oldChartResult) || []);
		var nows = (this.results(chartResult) || []);
		
		var merges = [];
		var mergeDataSize = ($.isFunction(appendMode.size) ? appendMode.size(this, this._toApiStdResult(chartResult)) : appendMode.size);
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
			
			//采用$.extend()方式，可保留nows[i]的其他属性
			merges[i] = $.extend({}, nows[i]);
			merges[i].data = mergeData;
		}
		
		if(chartResult == null)
			chartResult = {};
		
		this.results(chartResult, merges);
		
		return chartResult;
	};
	
	/**
	 * 获取/设置图表此次更新的数据集结果数组。
	 * 图表更新前会自动执行设置操作（通过chartBase.doUpdate()函数）。
	 * 
	 * @param dataSetResults 可选，要设置的数据集结果数组
	 * @returns 要获取的数据集结果数组，没有则返回null
	 */
	chartBase.updateResults = function(dataSetResults)
	{
		if(dataSetResults === undefined)
		{
			var chartResult = this.updateResult();
			return this.results(chartResult);
		}
		else
		{
			this.updateResult(dataSetResults);
		}
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
		
		this._doDestroy();
		
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
	
	chartBase._doDestroy = function()
	{
		var $element = this.elementJquery();
		
		$element.removeClass(this.themeStyleName());
		$element.removeClass(chartFactory._KEY_CHART_ELEMENT_STYLE_FOR_RELATIVE);
		$element.removeClass("dg-chart-beautify-scrollbar");
		$element.removeClass(chartFactory.CHART_STYLE_NAME_FOR_INDICATION);
		$element.data(chartFactory._KEY_ELEMENT_RENDERED_CHART, null);
		
		//应在这里先销毁图表元素内部创建的元素，
		//因为renderer.destroy()可能会清空图表元素（比如echarts.dispose()函数）
		this._doDestroySetting();
		
		var theme = this._themeNonNull();
		chartFactory.removeThemeRefEntity(theme, this.id);
	};
	
	/**
	 * 销毁图表交互设置。
	 */
	chartBase._doDestroySetting = function()
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
			if($.isFunction(renderer.asyncRender))
			{
				return renderer.asyncRender(this);
			}
			else
				return (renderer.asyncRender == true);
		}
		
		if(this.plugin.renderer.asyncRender === undefined)
		{
			return false;
		}
		
		if($.isFunction(this.plugin.renderer.asyncRender))
		{
			return this.plugin.renderer.asyncRender(this);
		}
		else
			return (this.plugin.renderer.asyncRender == true);
	};
	
	/**
	 * 图表的update函数是否是异步的。
	 * 
	 * @param chartResult 图表结果、数据集结果数组
	 */
	chartBase.isAsyncUpdate = function(chartResult)
	{
		//内部统一结构
		chartResult = this._toChartResult(chartResult);
		
		var renderer = this.renderer();
		
		if(renderer && renderer.asyncUpdate !== undefined)
		{
			if($.isFunction(renderer.asyncUpdate))
			{
				return renderer.asyncUpdate(this, this._toApiStdResult(chartResult));
			}
			else
				return (renderer.asyncUpdate == true);
		}
		
		if(this.plugin.renderer.asyncUpdate === undefined)
		{
			return false;
		}
		
		if($.isFunction(this.plugin.renderer.asyncUpdate))
		{
			return this.plugin.renderer.asyncUpdate(this, this._toApiStdResult(chartResult));
		}
		else
		{
			return (this.plugin.renderer.asyncUpdate == true);
		}
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
		{
			listener.update(this, this._toApiStdResult(this.updateResult()));
		}
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
	 * @param msg 可选，格式应为：{}, 当校验参数值未齐备时用于写入必填参数信息：{ dataSetBindIndex: 数值, paramName: 参数名 }
	 */
	chartBase.isDataSetParamValueReady = function(msg)
	{
		var dataSetBinds = this.dataSetBinds();
		
		for(var i=0; i<dataSetBinds.length; i++)
		{
			var dataSet = dataSetBinds[i].dataSet;
			
			if(!dataSet.params || dataSet.params.length == 0)
				continue;
			
			var paramValues = dataSetBinds[i].query.paramValues;
			
			for(var j=0; j<dataSet.params.length; j++)
			{
				var dsp = dataSet.params[j];
				
				if((dsp.required == true || dsp.required == "true") && paramValues[dsp.name] == null)
				{
					if(msg != null)
					{
						msg.dataSetBindIndex = i;
						msg.paramName = dsp.name;
					}
					
					return false;
				}
			}
		}
		
		return true;
	};
	
	chartBase._dataSetBindOf = function(dataSetBind, nullable)
	{
		nullable = (nullable == null ? false : nullable);
		
		var re = (chartFactory.isNumber(dataSetBind) ? this.dataSetBindAt(dataSetBind) : dataSetBind);
		
		if(!nullable && re == null)
			throw new Error("no dataSetBind found for : " + dataSetBind);
		
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
	 * @param dataSetBind 指定数据集绑定或其索引
	 * @param name 参数名、参数索引
	 * @param value 可选，要设置的参数值，不设置则执行获取操作
	 */
	chartBase.dataSetParamValue = function(dataSetBind, name, value)
	{
		dataSetBind = this._dataSetBindOf(dataSetBind);
		
		//参数索引
		if(chartFactory.isNumber(name))
		{
			var dataSet = dataSetBind.dataSet;
			
			if(!dataSet.params || dataSet.params.length <= name)
				throw new Error("chart '#"+this.elementId+"' "+dataSetBind.index+"-th dataSetBind has no param defined at index : "+name);
			
			name = dataSet.params[name].name;
		}
		
		var paramValues = dataSetBind.query.paramValues;
		
		if(dataSetBind._originalParamValues == null)
			dataSetBind._originalParamValues = $.extend({}, paramValues);
		
		if(value === undefined)
			return paramValues[name];
		else
			paramValues[name] = value;
	};
	
	/**
	 * 获取/设置第一个数据集参数值集。
	 * 
	 * @param paramValues 可选，要设置的参数名/值集对象，或者是与数据集参数数组元素一一对应的参数值数组，不设置则执行获取操作
	 * @param inflate 可选，设置操作是否仅填充在paramValues中出现的参数值，而保留旧参数值，默认值为：false
	 */
	chartBase.dataSetParamValuesFirst = function(paramValues, inflate)
	{
		return this.dataSetParamValues(0, paramValues, inflate);
	};
	
	/**
	 * 获取/设置指定数据集参数值集。
	 * 
	 * @param dataSetBind 指定数据集绑定或其索引
	 * @param paramValues 可选，要设置的参数值集对象，或者是与数据集参数数组元素一一对应的参数值数组，不设置则执行获取操作
	 * @param inflate 可选，设置操作是否仅填充在paramValues中出现的参数值，而保留旧参数值，默认值为：false
	 */
	chartBase.dataSetParamValues = function(dataSetBind, paramValues, inflate)
	{
		dataSetBind = this._dataSetBindOf(dataSetBind);
		
		var paramValuesCurrent = dataSetBind.query.paramValues;
		
		if(dataSetBind._originalParamValues == null)
			dataSetBind._originalParamValues = $.extend({}, paramValuesCurrent);
		
		if(paramValues === undefined)
			return paramValuesCurrent;
		else
		{
			inflate = (inflate == null ? false : inflate);
			
			if($.isArray(paramValues))
			{
				var params = (dataSetBind.dataSet.params || []);
				var len = Math.min(params.length, paramValues.length);
				var paramValuesObj = {};
				
				for(var i=0; i<len; i++)
				{
					var name = params[i].name;
					paramValuesObj[name] = paramValues[i];
				}
				
				paramValues = paramValuesObj;
			}
			
			if(inflate)
			{
				$.extend(paramValuesCurrent, paramValues);
			}
			else
			{
				dataSetBind.query.paramValues = paramValues;
			}
			
			// < @deprecated 兼容2.4.0版本的dataSetBind.paramValues，将在未来版本移除，已被dataSetBind.query.paramValues取代
			dataSetBind.paramValues = dataSetBind.query.paramValues;
			// > @deprecated 兼容2.4.0版本的dataSetBind.paramValues，将在未来版本移除，已被dataSetBind.query.paramValues取代
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
	 * @param dataSetBind 指定数据集绑定或其索引
	 */
	chartBase.resetDataSetParamValues = function(dataSetBind)
	{
		dataSetBind = this._dataSetBindOf(dataSetBind);
		
		if(dataSetBind._originalParamValues == null)
			return;
		
		dataSetBind.query.paramValues = $.extend({}, dataSetBind._originalParamValues);
		
		// < @deprecated 兼容2.4.0版本的dataSetBind.paramValues，将在未来版本移除，已被dataSetBind.query.paramValues取代
		dataSetBind.paramValues = dataSetBind.query.paramValues;
		// > @deprecated 兼容2.4.0版本的dataSetBind.paramValues，将在未来版本移除，已被dataSetBind.query.paramValues取代
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
	 * 获取指定标记的数据集字段，没有则返回undefined。
	 * 
	 * @param dataSetBind 数据集绑定或其索引
	 * @param dataSign 数据标记对象、标记名称
	 * @param nonEmpty 可选，参考chartBase.dataSetFieldsOfSign的nonEmpty参数
	 * @return {...}、undefined
	 */
	chartBase.dataSetFieldOfSign = function(dataSetBind, dataSign, nonEmpty)
	{
		var fields = this.dataSetFieldsOfSign(dataSetBind, dataSign, false, nonEmpty);
		return (fields.length > 0 ? fields[0] : undefined);
	};
	
	/**
	 * 获取指定标记的数据集字段数组。
	 * 
	 * @param dataSetBind 数据集绑定或其索引
	 * @param dataSign 数据标记对象、标记名称
	 * @param sort 可选，是否对返回结果进行重排序，true 是；false 否。默认值为：true
	 * @param nonEmpty 可选（设置时需指定sort参数），是否要求返回数组非空并且在为空时抛出异常，
	 * 					   "auto" 依据dataSign的required判断，为true则要求非空，否则不要求；
	 * 					   true 要求非空；false 不要求非空。默认为："auto"。
	 * @return [...]
	 */
	chartBase.dataSetFieldsOfSign = function(dataSetBind, dataSign, sort, nonEmpty)
	{
		dataSetBind = this._dataSetBindOf(dataSetBind);
		sort = (sort === undefined ? true : sort);
		nonEmpty = (nonEmpty == null ? "auto" : nonEmpty);
		
		var re = [];
		
		if(dataSign == null)
			return re;
		
		var dataSetFields = this.dataSetFields(dataSetBind, sort);
		var dataSignName = (chartFactory.isString(dataSign) ? dataSign : dataSign.name);
		var fieldSigns = (dataSetBind.fieldSigns || {});
		
		var signFieldNames = [];
		
		for(var pname in fieldSigns)
		{
			var mySigns = (fieldSigns[pname] || []);
			
			for(var i=0; i<mySigns.length; i++)
			{
				if(mySigns[i] == dataSignName)
				{
					signFieldNames.push(pname);
					break;
				}
			}
		}
		
		for(var i=0; i<dataSetFields.length; i++)
		{
			for(var j=0; j<signFieldNames.length; j++)
			{
				if(dataSetFields[i].name == signFieldNames[j])
					re.push(dataSetFields[i]);
			}
		}
		
		if(nonEmpty == "auto")
		{
			var dataSignObj = (chartFactory.isString(dataSign) ? this._dataSignOfName(dataSign) : dataSign);
			nonEmpty = (dataSignObj ? dataSignObj.required : false);
		}
		
		if(nonEmpty && re.length == 0)
			throw new Error("no dataSetField found for sign '"+dataSignName+"'");
		
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
	 * 转换为图表结果（org.datagear.analysis.ChartResult）
	 * 
	 * @param chartResult 图表结果（org.datagear.analysis.ChartResult）、数据集结果数组（org.datagear.analysis.DataSetResult）
	 */
	chartBase._toChartResult = function(chartResult)
	{
		if(chartResult == null)
			return chartResult;
		
		// 数据集结果数组
		if($.isArray(chartResult))
		{
			var re = {};
			this.results(re, chartResult);
			
			return re;
		}
		
		return chartResult;
	};
	
	/**
	 * 将图表结果（org.datagear.analysis.ChartResult）转换为兼容此版本API规范的结构。
	 * 在dashboardFactory.js将重写此函数，通过看板本版标识实现API规范兼容。
	 * 
	 * @param chartResult 图表结果（org.datagear.analysis.ChartResult）、数据集结果数组（org.datagear.analysis.DataSetResult）
	 */
	chartBase._toApiStdResult = function(chartResult)
	{
		return chartResult;
	};
	
	/**
	 * 获取/设置图表结果包含的指定数据集绑定对应的数据集结果。
	 * 
	 * @param chartResult 图表结果、数据集结果数组
	 * @param dataSetBind 数据集绑定、索引数值
	 * @param dataSetResult 可选，要设置的数据集结果
	 * @return 要获取的数据集结果，没有则返回undefined
	 */
	chartBase.resultOf = function(chartResult, dataSetBind, dataSetResult)
	{
		var dataSetResults = this.results(chartResult);
		var index = (chartFactory.isNumber(dataSetBind) ? dataSetBind : (dataSetBind != null ? dataSetBind.index : undefined));
		
		if(dataSetResult === undefined)
		{
			return (dataSetResults ? dataSetResults[index] : undefined);
		}
		else
		{
			//是图表结果，检查并初始化结构
			if(chartResult && !$.isArray(chartResult) && dataSetResults == null)
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
	chartBase.resultData = function(dataSetResult, data)
	{
		if(data === undefined)
			return (dataSetResult ? dataSetResult.data : undefined);
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
	 * @since 3.0.0
	 */
	chartBase.resultDataOf = function(chartResult, dataSetBind, data)
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
	chartBase.resultDatas = function(dataSetResult)
	{
		if(dataSetResult == null || dataSetResult.data == null)
			return [];
		
		if($.isArray(dataSetResult.data))
			return dataSetResult.data;
		
		return [ dataSetResult.data ];
	};
	
	/**
	 * 获取指定数据集绑定对应的数据集结果对象包含的数据对象数组。
	 * 
	 * @param chartResult 图表结果、数据集结果数组
	 * @param dataSetBind 数据集绑定、索引数值
	 * @return 不会为null的数组
	 * @since 3.0.0
	 */
	chartBase.resultDatasOf = function(chartResult, dataSetBind)
	{
		var dataSetResult = this.resultOf(chartResult, dataSetBind);
		return this.resultDatas(dataSetResult);
	};
	
	/**
	 * 获取数据集结果数据的行对象指定属性值。
	 * 
	 * @param rowObj 行对象，格式为：{ ... }
	 * @param field 数据集字段对象、字段名
	 */
	chartBase.resultRowCell = function(rowObj, field)
	{
		if(!rowObj || !field)
			return undefined;
		
		var name = (field.name || field);
		return rowObj[name];
	};
	
	/**
	 * 将数据集结果数据的行对象按照指定fields顺序转换为行值数组。
	 * 
	 * @param dataSetResult 数据集结果
	 * @param fields 数据集字段对象数组、字段名数组、字段对象、字段名
	 * @param row 可选，行索引，默认为0
	 * @param count 可选，获取的最多行数，默认为全部
	 * @return fields为数组时：[[..., ...], ...]；fields非数组时：[..., ...]
	 */
	chartBase.resultRowArrays = function(dataSetResult, fields, row, count)
	{
		var re = [];
		
		if(!dataSetResult || !fields)
			return re;
		
		var datas = this.resultDatas(dataSetResult);
		
		row = (row == null ? 0 : row);
		var endIdx = (count == null ? datas.length : (row + count));
		endIdx = (endIdx > datas.length ? datas.length : endIdx);
		
		if($.isArray(fields))
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
	 * 将数据集结果数据的行对象按照指定fields顺序转换为列值数组。
	 * 
	 * @param dataSetResult 数据集结果
	 * @param fields 数据集字段对象数组、字段名数组、字段对象、字段名
	 * @param row 行索引，以0开始，可选，默认为0
	 * @param count 获取的最多行数，可选，默认为全部
	 * @return fields为数组时：[[..., ...], ...]；fields非数组时：[..., ...]
	 */
	chartBase.resultColumnArrays = function(dataSetResult, fields, row, count)
	{
		var re = [];

		if(!dataSetResult || !fields)
			return re;
		
		var datas = this.resultDatas(dataSetResult);
		
		row = (row == null ? 0 : row);
		var endIdx = (count == null ? datas.length : (row + count));
		endIdx = (endIdx > datas.length ? datas.length : endIdx);
		
		if($.isArray(fields))
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
	 * 获取数据集结果数据的名称/值对象数组。
	 * 
	 * @param dataSetResult 数据集结果
	 * @param nameField 名称数据集字段对象、字段名
	 * @param valueField 值数据集字段对象、字段名、数组
	 * @param row 可选，行索引，以0开始，默认为0
	 * @param count 可选，获取结果数据的最多行数，默认为全部
	 * @return [{name: ..., value: ...}, ...]
	 */
	chartBase.resultNameValueObjects = function(dataSetResult, nameField, valueField, row, count)
	{
		var fieldMap ={ "name": nameField, "value": valueField };
		return this.resultMapObjects(dataSetResult, fieldMap, row, count);
	};
	
	/**
	 * 获取数据集结果数据的值对象数组。
	 * 
	 * @param dataSetResult 数据集结果
	 * @param valueField 值数据集字段对象、字段名、数组
	 * @param row 可选，行索引，以0开始，默认为0
	 * @param count 可选，获取结果数据的最多行数，默认为全部
	 * @return [{value: ...}, ...]
	 */
	chartBase.resultValueObjects = function(dataSetResult, valueField, row, count)
	{
		var fieldMap ={ "value": valueField };
		return this.resultMapObjects(dataSetResult, fieldMap, row, count);
	};
	
	/**
	 * 获取数据集结果数据指定字段、指定行的单元格值，没有则返回undefined。
	 * 
	 * @param dataSetResult 数据集结果
	 * @param field 数据集字段对象、字段名
	 * @param row 行索引，可选，默认为0
	 */
	chartBase.resultCell = function(dataSetResult, field, row)
	{
		row = (row == null ? 0 : row);
		
		var re = this.resultRowArrays(dataSetResult, field, row, 1);
		
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
		var url = chartMapURLs[name];
		
		if(!url && typeof(chartMapURLs.mapURL) == "function")
			url = chartMapURLs.mapURL(name);
		
		url = this.contextURL(url || name);
		
		return url;
	};
	
	/**
	 * 加载指定名称的地图资源（通常是*.json、*.svg）。
	 * 注意：如果在图表渲染器的render/update函数中调用此函数，应该首先设置其的asyncRender/asyncUpdate为true，
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
		if(renderOptions[builtinOptionNames.processRenderOptions])
			renderOptions[builtinOptionNames.processRenderOptions](renderOptions, this);
		
		this.renderOptions(renderOptions);
		
		return renderOptions;
	};
	
	/**
	 * 填充指定图表更新选项。
	 * 
	 * 此函数先将renderOptions中与updateOptions的同名项高优先级深度合并至updateOptions，然后调用可选的beforeProcessHandler，
	 * 最后，如果renderOptions或者chart.renderOptions()中有定义processUpdateOptions函数（格式为：function(updateOptions, chart, chartResult){ ... }），
	 * 则调用它们两个的其中一个（renderOptions优先）。
	 * 
	 * 图表渲染器应该在其update()中使用此函数构建图表更新选项，然后使用它执行图表更新逻辑，以符合图表API规范。
	 * 
	 * @param chartResult 图表结果、数据集结果数组
	 * @param updateOptions 可选，待填充的更新选项，通常由图表渲染器update函数内部生成，格式为：{ ... }，默认为空对象：{}
	 * @param renderOptions 可选，图表的渲染选项，格式为：{ ... }，默认为：chart.renderOptions()
	 * @param beforeProcessHandler 可选，renderOptions.processUpdateOptions调用前处理函数，
								   格式为：function(updateOptions, chart, chartResult){ ... }, 默认为：undefined
	 * @returns updateOptions
	 */
	chartBase.inflateUpdateOptions = function(chartResult, updateOptions, renderOptions, beforeProcessHandler)
	{
		//(chartResult)
		if(arguments.length == 1)
			;
		else if(arguments.length == 2)
		{
			//(chartResult, beforeProcessHandler)
			if($.isFunction(updateOptions))
			{
				beforeProcessHandler = updateOptions;
				updateOptions = undefined;
				renderOptions = undefined;
			}
			//(chartResult, updateOptions)
			else
				;
		}
		else if(arguments.length == 3)
		{
			//(chartResult, updateOptions, beforeProcessHandler)
			if($.isFunction(renderOptions))
			{
				beforeProcessHandler = renderOptions;
				renderOptions = undefined;
			}
			//(chartResult, updateOptions, renderOptions)
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
			beforeProcessHandler(updateOptions, this, chartResult);
		
		//最后调用processUpdateOptions
		if(renderOptions[builtinOptionNames.processUpdateOptions])
		{
			var chartResultMy = this._toApiStdResult(chartResult);
			renderOptions[builtinOptionNames.processUpdateOptions](updateOptions, this, chartResultMy);
		}
		//renderOptions可能不是chartRenderOptions，此时要确保chartRenderOptions.processUpdateOptions被调用
		else if(chartRenderOptions && renderOptions !== chartRenderOptions
					&& chartRenderOptions[builtinOptionNames.processUpdateOptions])
		{
			var chartResultMy = this._toApiStdResult(chartResult);
			chartRenderOptions[builtinOptionNames.processUpdateOptions](updateOptions, this, chartResultMy);
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
	 * @param dataSetResult 数据集结果
	 * @param index 索引数值、数值数组
	 * @return 数据对象、据对象数组，当result、index为null时，将返回null
	 */
	chartBase.resultDataElement = function(dataSetResult, index)
	{
		if(dataSetResult == null || dataSetResult.data == null || index == null)
			return undefined;
		
		var datas = this.resultDatas(dataSetResult);
		
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
	 *   update: function(chart, chartResult)
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
	 * @param dataSetBind 数据集绑定或其索引
	 * @param alias 可选，要设置的别名，不设置则执行获取操作
	 * @returns 要获取的别名，不会为null
	 * @since 2.10.0
	 */
	chartBase.dataSetAlias = function(dataSetBind, alias)
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
	 * @since 2.10.0
	 */
	chartBase.dataSetFields = function(dataSetBind, sort)
	{
		dataSetBind = this._dataSetBindOf(dataSetBind);
		sort = (sort === undefined ? true : sort);
		
		var fields = null;
		var isDataSet = (dataSetBind.fields !== undefined);
		
		if(isDataSet)
			fields = dataSetBind.fields;
		else
			fields = (dataSetBind.dataSet ? dataSetBind.dataSet.fields : null);
		
		fields = (fields || []);
		
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
	 * @param info 数据集字段标识，可以是字段名、字段索引
	 * @returns 数据集字段，没有找到则返回undefined
	 * @since 2.10.0
	 */
	chartBase.dataSetField = function(dataSetBind, info)
	{
		var fields = this.dataSetFields(dataSetBind, false);
		
		if(!fields)
			return undefined;
		
		if(chartFactory.isNumber(info))
			return fields[info];
		
		for(var i=0; i<fields.length; i++)
		{
			if(fields[i].name == info)
				return fields[i];
		}
		
		return undefined;
	};
	
	/**
	 * 获取/设置数据集字段别名。
	 * 
	 * @param dataSetBind 数据集绑定或其索引
	 * @param dataSetField 数据集字段、字段名、字段索引
	 * @param alias 可选，要设置的别名，不设置则执行获取操作
	 * @returns 要获取的别名，不会为null
	 * @since 2.10.0
	 */
	chartBase.dataSetFieldAlias = function(dataSetBind, dataSetField, alias)
	{
		dataSetBind = this._dataSetBindOf(dataSetBind);
		
		if(chartFactory.isStringOrNumber(dataSetField))
			dataSetField = this.dataSetField(dataSetBind, dataSetField);
		
		if(alias === undefined)
		{
			if(!dataSetField)
				return "";
			
			alias =  (dataSetBind.fieldAliases ?
							dataSetBind.fieldAliases[dataSetField.name] : null);
			
			if(!alias)
				alias = (dataSetField.label ||  dataSetField.name);
			
			return (alias || "");
		}
		else
		{
			if(!dataSetBind.fieldAliases)
				dataSetBind.fieldAliases = {};
			
			dataSetBind.fieldAliases[dataSetField.name] = alias;
		}
	};
	
	/**
	 * 获取/设置数据集字段排序值。
	 * 
	 * @param dataSetBind 数据集绑定或其索引
	 * @param dataSetField 数据集字段、字段名、字段索引
	 * @param order 可选，要设置的排序数值，不设置则执行获取操作
	 * @returns 要获取的排序数值，没有设置过则返回null
	 * @since 2.10.0
	 */
	chartBase.dataSetFieldOrder = function(dataSetBind, dataSetField, order)
	{
		dataSetBind = this._dataSetBindOf(dataSetBind);
		
		var name = null;
		
		if(chartFactory.isString(dataSetField))
			name = dataSetField;
		else
		{
			if(chartFactory.isNumber(dataSetField))
				dataSetField = this.dataSetField(dataSetBind, dataSetField);
			
			name = (dataSetField ? dataSetField.name : null);
		}
		
		if(order === undefined)
		{
			return (dataSetBind.fieldOrders ?
							dataSetBind.fieldOrders[name] : undefined);
		}
		else
		{
			if(!dataSetBind.fieldOrders)
				dataSetBind.fieldOrders = {};
			
			dataSetBind.fieldOrders[name] = order;
		}
	};
	
	/**
	 * 获取数据集参数数组。
	 * 
	 * @param dataSetBind 数据集绑定或其索引、数据集
	 * @returns 数据集参数数组，空数组表示没有参数
	 * @since 2.10.0
	 */
	chartBase.dataSetParams = function(dataSetBind)
	{
		dataSetBind = this._dataSetBindOf(dataSetBind);
		
		var params = null;
		
		if(dataSetBind.params !== undefined)
			params = dataSetBind.params;
		else
			params = (dataSetBind.dataSet ? dataSetBind.dataSet.params : null);
		
		return (params || []);
	};
	
	/**
	 * 获取指定标识的数据集参数。
	 * 
	 * @param dataSetBind 数据集绑定或其索引、数据集
	 * @param info 数据集参数标识，可以是参数名、参数索引
	 * @returns 数据集参数，没有找到则返回undefined
	 * @since 2.10.0
	 */
	chartBase.dataSetParam = function(dataSetBind, info)
	{
		var params = this.dataSetParams(dataSetBind);
		
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
		var dataSetBinds = this.dataSetBinds();
		for(var i=0; i<dataSetBinds.length; i++)
		{
			var params = dataSetBinds[i].dataSet.params;
			
			if(params && params.length > 0)
				return true;
		}
		
		return false;
	};
	
	/**
	 * 获取数据集结果数据经属性映射后的对象数组。
	 * 
	 * @param dataSetResult 数据集结果
	 * @param fieldMap 返回对象属性映射表，格式为：{ 返回对象属性名: 数据集结果数据属性对象、属性名、属性数组、属性名数组 }
	 * @param row 可选，行索引，以0开始，默认为0
	 * @param count 可选，获取结果数据的最多行数，默认为全部
	 * @return [{"...": ..., "...": ...}, ...]
	 * @since 2.10.0
	 */
	chartBase.resultMapObjects = function(dataSetResult, fieldMap, row, count)
	{
		var re = [];
		
		var datas = this.resultDatas(dataSetResult);
		row = (row == null ? 0 : row);
		var endIdx = (count == null ? datas.length : (row + count));
		endIdx = (endIdx > datas.length ? datas.length : endIdx);
		
		var propIsArray = {};
		for(var opn in fieldMap)
			propIsArray[opn] = $.isArray(fieldMap[opn]);
		
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
	 * 获取/设置指定数据集字段标记。
	 * 
	 * @param dataSetBind 数据集绑定或其索引
	 * @param dataSetField 数据集字段、字段名、字段索引
	 * @param dataSign 可选，要设置的数据标记对象、对象数组，或者名称字符串、字符串数组，或者null，不设置则执行获取操作
	 * @returns 要获取的标记名字符串数组、null
	 * @since 2.11.0
	 */
	chartBase.dataSetFieldSign = function(dataSetBind, dataSetField, dataSign)
	{
		dataSetBind = this._dataSetBindOf(dataSetBind);
		
		var name = null;
		
		if(chartFactory.isString(dataSetField))
			name = dataSetField;
		else
		{
			if(chartFactory.isNumber(dataSetField))
				dataSetField = this.dataSetField(dataSetBind, dataSetField);
			
			name = (dataSetField ? dataSetField.name : null);
		}
		
		if(dataSign === undefined)
		{
			return (dataSetBind.fieldSigns ?
							dataSetBind.fieldSigns[name] : undefined);
		}
		else
		{
			if(!dataSetBind.fieldSigns)
				dataSetBind.fieldSigns = {};
			
			dataSign = this._trimDataSetFieldSign(dataSign);
			dataSetBind.fieldSigns[name] = dataSign;
		}
	};
	
	/**
	 * 获取/设置数据集字段标记映射表。
	 * 
	 * @param dataSetBind 数据集绑定或其索引
	 * @param signs 可选，要设置的数据标记映射表，格式为：{ 数据集字段名: 数据标记对象、对象数组，或者名称字符串、字符串数组，或者null, ... }，不设置则执行获取操作
	 * @param increment 可选，设置操作时是否执行增量设置，仅设置signs中出现的项，true 是；false 否。默认值为：true
	 * @returns 要获取的标记映射表，格式为：{ 数据集字段名: 标记名字符串数组、null, ... }，不会为null
	 * @since 2.11.0
	 */
	chartBase.dataSetFieldSigns = function(dataSetBind, signs, increment)
	{
		dataSetBind = this._dataSetBindOf(dataSetBind);
		increment = (increment == null ? true : increment);
		
		if(signs === undefined)
		{
			return (dataSetBind.fieldSigns || {});
		}
		else
		{
			var trimSigns = {};
			
			if(signs)
			{
				for(var p in signs)
				{
					var ps = this._trimDataSetFieldSign(signs[p]);
					trimSigns[p] = ps;
				}
			}
			
			if(!dataSetBind.fieldSigns || !increment)
				dataSetBind.fieldSigns = trimSigns;
			else
			{
				for(var p in trimSigns)
					dataSetBind.fieldSigns[p] = trimSigns[p];
			}
		}
	};
	
	chartBase._trimDataSetFieldSign = function(dataSign)
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
	 * 判断给定数据集绑定是否是易变模型的。
	 * 
	 * @param dataSetBind 数据集绑定或其索引
	 * @returns true、false
	 * @since 3.0.0
	 */
	chartBase.isMutableModel = function(dataSetBind)
	{
		dataSetBind = this._dataSetBindOf(dataSetBind);
		return (dataSetBind.dataSet.mutableModel == true);
	};
	
	/**
	 * 获取/设置多条图表展示数据的原始数据索引（图表ID、数据集绑定索引、结果数据索引）。
	 * 图表展示数据是指由图表数据集结果数据转换而得，用于渲染图表的数据。
	 * 图表渲染器在构建图表展示数据时，应使用此函数设置其原始数据索引信息，以支持在后续的交互、事件处理中获取它们。
	 * 
	 * @param data 展示数据数组，格式为：[ ... ]，元素格式允许为：{ ... }、[ ... ]
	 * @param dataSetBindIndex 要设置的数据集绑定对象（自动取其索引）、数据集绑定对象数组（自动取其索引）、数据集绑定索引数值、索引数值数组
	 * @param resultDataIndex 要设置的结果数据索引，格式为：
	 *                        当dataSetBindIndex是数据集绑定对象、索引数值时：
	 *                        数值、数值数组
	 *                        当dataSetBindIndex是数据集绑定对象数组、索引数值数组时：
	 *                        数值，表示dataSetBindIndex数组每个元素的结果数据索引都是此数值
	 *                        数组（元素可以是数值、数值数组），与dataSetBindIndex数组元素一一对应
	 *                        默认值为：0
	 * @param autoIncrement 可选，
	 *                      当dataSetBindIndex是数据集绑定对象、数据集绑定索引数值且resultDataIndex是数值时，是否自动递增resultDataIndex；
	 *                      当dataSetBindIndex是数据集绑定对象数组、数据集绑定索引数值数组且其元素对应位置的resultDataIndex是数值时，是否自动递增resultDataIndex。
	 *                      默认值为：true
	 * @returns 要获取的原始数据索引数组(元素可能为null），原始数据索引对象格式为：
	 *									{
	 *										//图表ID
	 *										chartId: "...",
	 *										//数据集绑定索引，格式为：数值、数值数组
	 *										dataSetBindIndex: ...,
	 *										//结果数据索引，格式为：
	 *                                      //当dataSetBindIndex是数值时：
	 *                                      //数值、数值数组
	 *                                      //当dataSetBindIndex是数值数组时：
	 *                                      //数组（元素可能是数值、数值数组）
	 *										resultDataIndex: ...
	 *									}
	 * @since 3.1.0
	 */
	chartBase.originalDataIndexes = function(data, dataSetBindIndex, resultDataIndex, autoIncrement)
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
			
			//(data, dataSetBindIndex, true)、(data, dataSetBindIndex, false)
			if(resultDataIndex === true || resultDataIndex === false)
			{
				autoIncrement = resultDataIndex;
				resultDataIndex = undefined;
			}
			resultDataIndex = (resultDataIndex === undefined ? 0 : resultDataIndex);
			
			var isCdsiArray = $.isArray(dataSetBindIndex);
			
			if(isCdsiArray)
			{
				var cdsiNew = [];
				
				for(var i=0; i<dataSetBindIndex.length; i++)
				{
					cdsiNew[i] = (dataSetBindIndex[i] != null && dataSetBindIndex[i].index !== undefined ?
									dataSetBindIndex[i].index : dataSetBindIndex[i]);
				}
				
				dataSetBindIndex = cdsiNew;
				
				if(!$.isArray(resultDataIndex))
				{
					var rdiNew = [];
					
					for(var i=0; i<dataSetBindIndex.length; i++)
						rdiNew[i] = resultDataIndex;
					
					resultDataIndex = rdiNew;
				}
			}
			else
			{
				dataSetBindIndex = (dataSetBindIndex != null && dataSetBindIndex.index !== undefined ?
										dataSetBindIndex.index : dataSetBindIndex);
			}
			
			autoIncrement = (autoIncrement === undefined ? true : autoIncrement);
			var isRdiNumber = chartFactory.isNumber(resultDataIndex);
			
			var needAutoIncrementEle = (autoIncrement == true && $.isArray(resultDataIndex));
			if(needAutoIncrementEle == true)
			{
				needAutoIncrementEle = false;
				
				//任一元素是数值的话，才需要自增处理
				for(var i=0; i<resultDataIndex.length; i++)
				{
					if(chartFactory.isNumber(resultDataIndex[i]))
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
							if(chartFactory.isNumber(resultDataIndexMy[j]))
								resultDataIndexMy[j] = resultDataIndexMy[j] + i;
						}
					}
				}
				
				this._originalDataIndex(data[i], dataSetBindIndex, resultDataIndexMy);
			}
		}
	};
	
	/**
	 * 获取/设置单条图表展示数据的原始数据索引（图表ID、数据集绑定索引、结果数据索引）。
	 * 图表展示数据是指由图表数据集结果数据转换而得，用于渲染图表的数据。
	 * 图表渲染器在构建图表展示数据时，应使用此函数设置其原始数据索引，以支持在后续的交互、事件处理中获取它们。
	 * 
	 * @param data 展示数据，格式为：{ ... }、[ ... ]
	 * @param dataSetBindIndex 同chartBase.originalDataIndexes()函数的dataSetBindIndex参数
	 * @param resultDataIndex 同chartBase.originalDataIndexes()函数的resultDataIndex参数
	 * @returns 要获取的原始数据索引(可能为null），格式参考chartBase.originalDataIndexes()函数返回值
	 * @since 3.1.0
	 */
	chartBase.originalDataIndex = function(data, dataSetBindIndex, resultDataIndex)
	{
		//获取
		if(arguments.length <= 1)
		{
			return this._originalDataIndex(data);
		}
		else
		{
			data = [ data ];
			this.originalDataIndexes(data, dataSetBindIndex, resultDataIndex, false);
		}
	};
	
	//获取/设置单条图表展示数据的原始数据索引
	chartBase._originalDataIndex = function(data, dataSetBindIndex, resultDataIndex)
	{
		if(arguments.length <= 1)
			return chartFactory.originalDataIndex(data);
		else
			chartFactory.originalDataIndex(data, this.id, dataSetBindIndex, resultDataIndex);
	};
	
	/**
	 * 图表事件支持函数：获取/设置图表事件对象的原始数据索引（图表ID、数据集绑定索引、结果数据索引），即：chartEvent.originalDataIndex。
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
			var originalDataSetBindIndex = [];
			var originalResultDataIndex = [];
			for(var i=0; i<originalDataIndexAry.length; i++)
			{
				originalDataSetBindIndex[i] = originalDataIndexAry[i].dataSetBindIndex;
				originalResultDataIndex[i] = originalDataIndexAry[i].resultDataIndex;
			}
			chartEvent["originalChartDataSetIndex"] = (isArray ? originalDataSetBindIndex : originalDataSetBindIndex[0]);
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
		name = (name || "");
		
		var webContext = this._renderContextAttrWebContext();
		
		if(!webContext)
			throw new Error("chart is illegal state for pluginResourceURL(name)");
		
		var urlPrefix = webContext.attributes.pluginResUrlPrefix;
		var url = urlPrefix+"/"+encodeURIComponent(this.plugin.id)+"/"+name;
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
			if(chartFactory.isNullOrEmpty(options))
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
	 * 
	 * @since 5.0.0
	 */
	chartBase.updateAppendMode = function(appendMode)
	{
		if(arguments.length == 0)
		{
			return this._updateAppendMode;
		}
		else
		{
			if(appendMode === true)
			{
				appendMode = { size: 10, beforeListener: false };
			}
			else if(chartFactory.isNumber(appendMode))
			{
				appendMode = { size: appendMode, beforeListener: false };
			}
			else if($.isFunction(appendMode))
			{
				appendMode = { size: appendMode, beforeListener: false };
			}
			
			this._updateAppendMode = appendMode;
		}
	};
	
	/**
	 * 获取全部数据集绑定数组。
	 * 
	 * @return []，空数组表示没有数据集绑定
	 * @since 5.0.0
	 */
	chartBase.dataSetBinds = function()
	{
		return (this._dataSetBinds || []);
	};
	
	/**
	 * 获取主件数据集绑定数组，它们的用途是绘制图表。
	 * 
	 * @return []，空数组表示没有主件数据集绑定
	 * @since 5.0.0
	 */
	chartBase.dataSetBindsMain = function()
	{
		var re = [];
		
		var dataSetBinds = this.dataSetBinds();
		for(var i=0; i<dataSetBinds.length; i++)
		{
			if(dataSetBinds[i].attachment)
				continue;
			
			re.push(dataSetBinds[i]);
		}
		
		return re;
	};
	
	/**
	 * 获取附件数据集绑定数组，它们的用途不是绘制图表。
	 * 
	 * @return []，空数组表示没有附件数据集绑定
	 * @since 5.0.0
	 */
	chartBase.dataSetBindsAttachment = function()
	{
		var re = [];
		
		var dataSetBinds = this.dataSetBinds();
		for(var i=0; i<dataSetBinds.length; i++)
		{
			if(dataSetBinds[i].attachment)
			{
				re.push(dataSetBinds[i]);
			}
		}
		
		return re;
	};
	
	/**
	 * 获取指定索引的数据集绑定。
	 * 
	 * @param index
	 * @return 数据集绑定，null表示没有
	 * @since 5.0.0
	 */
	chartBase.dataSetBindAt = function(index)
	{
		var dataSetBinds = this.dataSetBinds();
		return dataSetBinds[index];
	};
	
	/**
	 * 获取第一个主件数据集绑定。
	 * 主件数据集绑定的用途是绘制图表。
	 * 
	 * @return 未找到时返回null
	 * @since 5.0.0
	 */
	chartBase.dataSetBindMain = function()
	{
		var re = undefined;
		
		var dataSetBinds = this.dataSetBinds();
		for(var i=0; i<dataSetBinds.length; i++)
		{
			if(!dataSetBinds[i].attachment)
			{
				re = dataSetBinds[i];
				break;
			}
		}
		
		return re;
	};
	
	/**
	 * 获取第一个附件数据集绑定对象。
	 * 附件数据集绑定的用途不是绘制图表。
	 * 
	 * @return 未找到时返回null
	 * @since 5.0.0
	 */
	chartBase.dataSetBindAttachment = function()
	{
		var re = undefined;
		
		var dataSetBinds = this.dataSetBinds();
		for(var i=0; i<dataSetBinds.length; i++)
		{
			if(dataSetBinds[i].attachment)
			{
				re = dataSetBinds[i];
				break;
			}
		}
		
		return re;
	};
	
	/**
	 * 为以"/"开头的URL添加系统根路径前缀，否则，将直接返回原URL。
	 * 当需要访问系统内其他功能模块的资源时，应为其URL添加系统根路径前缀。
	 * 
	 * @param url 可选，要处理的URL
	 * @return 添加后的新URL，如果没有url参数，将返回系统根路径
	 * @since 5.0.0
	 */
	chartBase.contextURL = function(url)
	{
		var webContext = this._renderContextAttrWebContext();
		
		if(!webContext)
		{
			throw new Error("chart is illegal state for contextURL(url)");
		}
		
		return chartFactory.toWebContextPathURL(webContext, url);
	};
	
	/**
	 * 加载库，并在全部加载完成后（无论是否成功）执行回调函数。
	 * 注意：如果在图表渲染器的render/update函数中调用此函数，应该首先设置其asyncRender/asyncUpdate为true，
	 * 并在callback中调用chart.statusRendered(true)/chart.statusUpdated(true)，具体参考此文件顶部的注释。
	 * 
	 * @param lib 库对象、数组，结构参考chartFactory.loadLib()函数说明，注意，其中库源URL应是可以直接加载的
	 * @param callback 可选，加载完成后回调函数（无论是否成功都将执行），格式参考chartFactory.loadLib()函数说明
	 * @since 5.2.0
	 */
	chartBase.loadLib = function(lib, callback)
	{
		callback = (callback ? callback : function(){});
		
		var contextCharts = this._contextCharts();
		chartFactory.loadLib(lib,  callback, contextCharts);
	};
	
	/**
	 * 获取/设置图表此次更新的结果数据。
	 * 图表更新前会自动执行设置操作（通过chartBase.doUpdate()函数）。
	 * 
	 * @param chartResult 可选，要设置的图表结果、数据集结果数组
	 * @returns 要获取的图表结果，没有则返回null
	 * @since 5.3.0 此API暂不开放，因为5.3.0版本的开放API中没有用到chartResult设计概念
	 */
	chartBase.updateResult = function(chartResult)
	{
		if(chartResult === undefined)
			return chartFactory.extValueBuiltin(this, "updateResult");
		else
		{
			chartResult = this._toChartResult(chartResult);
			chartFactory.extValueBuiltin(this, "updateResult", chartResult);
		}
	};
	
	/**
	 * 获取/设置图表结果包含的数据集结果数组。
	 * 
	 * @param chartResult 图表结果、数据集结果数组（仅获取时）
	 * @param dataSetResults 可选，要设置的数据集结果数组
	 * @returns 要获取的数据集结果数组，没有则返回null
	 * @since 5.3.0 此API暂不开放，因为5.3.0版本的开放API中没有用到chartResult设计概念
	 */
	chartBase.results = function(chartResult, dataSetResults)
	{
		if(dataSetResults === undefined)
		{
			if(chartResult == null)
			{
				return undefined;
			}
			// 数据集结果数组
			else if($.isArray(chartResult))
			{
				return chartResult;
			}
			else
			{
				return chartResult.dataSetResults;
			}
		}
		else
		{
			//应禁止此操作，因为会引起读取操作歧义
			if(chartResult && $.isArray(chartResult))
				throw new Error("set results for array unsupported");
			
			chartResult.dataSetResults = dataSetResults;
		}
	};
	
	/**
	 * 获取图表插件附加属性值。
	 * 
	 * @param name 附加属性名
	 * @returns 要获取的附加属性值，没有则返回null
	 * @since 5.4.0
	 */
	chartBase.pluginAddition = function(name)
	{
		return (this.plugin && this.plugin.additions ? this.plugin.additions[name] : undefined);
	};
	
	//-------------
	// < 已弃用函数 start
	//-------------
	
	// < @deprecated 兼容5.3.1版本的API，将在未来版本移除，请使用chartBase.on()
	
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
	
	// > @deprecated 兼容5.3.1版本的API，将在未来版本移除，请使用chartBase.on()
	
	// < @deprecated 兼容5.2.0版本的API，将在未来版本移除，请使用chartBase.resultOf()
	
	/**
	 * 获取/设置图表结果包含的指定索引的数据集结果。
	 * 
	 * @param chartResult 图表结果、数据集结果数组
	 * @param index 索引数值
	 * @param dataSetResult 可选，要设置的数据集结果
	 * @return 要获取的数据集结果，没有则返回undefined
	 */
	chartBase.resultAt = function(chartResult, index, dataSetResult)
	{
		if(dataSetResult === undefined)
		{
			return this.resultOf(chartResult, index);
		}
		else
		{
			this.resultOf(chartResult, index, dataSetResult);
		}
	};
	
	// > @deprecated 兼容5.2.0版本的API，将在未来版本移除，请使用chartBase.resultOf()
	
	// < @deprecated 兼容5.0.0版本的API，将在未来版本移除，请使用chartBase.dataSetFieldOfSign()
	
	chartBase.dataSetPropertyOfSign = function(dataSetBind, dataSign, nonEmpty)
	{
		return this.dataSetFieldOfSign(dataSetBind, dataSign, nonEmpty);
	};
	
	// > @deprecated 兼容5.0.0版本的API，将在未来版本移除，请使用chartBase.dataSetFieldOfSign()
	
	
	// < @deprecated 兼容5.0.0版本的API，将在未来版本移除，请使用chartBase.dataSetFieldsOfSign()
	
	chartBase.dataSetPropertiesOfSign = function(dataSetBind, dataSign, sort, nonEmpty)
	{
		return this.dataSetFieldsOfSign(dataSetBind, dataSign, sort, nonEmpty);
	};
	
	// > @deprecated 兼容5.0.0版本的API，将在未来版本移除，请使用chartBase.dataSetFieldsOfSign()
	
	
	// < @deprecated 兼容5.0.0版本的API，将在未来版本移除，请使用chartBase.dataSetFields()
	
	chartBase.dataSetProperties = function(dataSetBind, sort)
	{
		return this.dataSetFields(dataSetBind, sort);
	};
	
	// > @deprecated 兼容5.0.0版本的API，将在未来版本移除，请使用chartBase.dataSetFields()
	
	
	// < @deprecated 兼容5.0.0版本的API，将在未来版本移除，请使用chartBase.dataSetField()
	
	chartBase.dataSetProperty = function(dataSetBind, info)
	{
		return this.dataSetField(dataSetBind, info);
	};
	
	// > @deprecated 兼容5.0.0版本的API，将在未来版本移除，请使用chartBase.dataSetField()
	
	
	// < @deprecated 兼容5.0.0版本的API，将在未来版本移除，请使用chartBase.dataSetFieldAlias()
	
	chartBase.dataSetPropertyAlias = function(dataSetBind, dataSetField, alias)
	{
		return this.dataSetFieldAlias(dataSetBind, dataSetField, alias);
	};
	
	// > @deprecated 兼容5.0.0版本的API，将在未来版本移除，请使用chartBase.dataSetFieldAlias()
	
	
	// < @deprecated 兼容5.0.0版本的API，将在未来版本移除，请使用chartBase.dataSetFieldOrder()
	
	chartBase.dataSetPropertyOrder = function(dataSetBind, dataSetField, order)
	{
		return this.dataSetFieldOrder(dataSetBind, dataSetField, order);
	};
	
	// > @deprecated 兼容5.0.0版本的API，将在未来版本移除，请使用chartBase.dataSetFieldOrder()
	
	// < @deprecated 兼容5.0.0版本的API，将在未来版本移除，请使用chartBase.dataSetFieldSign()
	
	chartBase.dataSetPropertySign = function(dataSetBind, dataSetField, dataSign)
	{
		return this.dataSetFieldSign(dataSetBind, dataSetField, dataSign);
	};
	
	// > @deprecated 兼容5.0.0版本的API，将在未来版本移除，请使用chartBase.dataSetFieldSign()
	
	
	// < @deprecated 兼容5.0.0版本的API，将在未来版本移除，请使用chartBase.dataSetFieldSigns()
	
	chartBase.dataSetPropertySigns = function(dataSetBind, signs, increment)
	{
		return this.dataSetFieldSigns(dataSetBind, signs, increment);
	};
	
	// > @deprecated 兼容5.0.0版本的API，将在未来版本移除，请使用chartBase.dataSetFieldSigns()
	
	
	// < @deprecated 兼容4.7.0版本的dg-chart-map功能，将在未来版本移除，请使用chartSupport中的builtinOptionNames.mapName图表选项
	/**图表地图*/
	elementAttrConst.MAP = "dg-chart-map";
	
	/**
	 * 初始化图表的地图名。
	 * 此函数从图表元素的elementAttrConst.MAP属性获取图表地图名。
	 */
	chartBase._initMap = function()
	{
		var map = this.elementJquery().attr(elementAttrConst.MAP);
		
		// < @deprecated 兼容4.7.0版本的chart.map()函数功能，将在未来版本随之一起移除
		this.map(map);
		// > @deprecated 兼容4.7.0版本的chart.map()函数功能，将在未来版本随之一起移除
	};
	// > @deprecated 兼容4.7.0版本的dg-chart-map功能，将在未来版本移除，请使用chartSupport中的builtinOptionNames.mapName图表选项
	
	// < @deprecated 兼容4.7.0版本的API，将在未来版本移除，因为与底层图表组件本身的地图设置选项功能重复，容易引起混淆
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
	// > @deprecated 兼容4.7.0版本的API，将在未来版本移除，因为与底层图表组件本身的地图设置选项功能重复，容易引起混淆
	
	// < @deprecated 兼容4.7.0版本的API，将在未来版本移除，请使用chartBase.dataSetBindsMain()
	/**
	 * 获取主件数据集绑定对象数组，它们的用途是绘制图表。
	 * 
	 * @return []，空数组表示没有主件数据集绑定
	 */
	chartBase.chartDataSetsMain = function()
	{
		return this.dataSetBindsMain();
	};
	// > @deprecated 兼容4.7.0版本的API，将在未来版本移除，请使用chartBase.dataSetBindsMain()
	
	// < @deprecated 兼容4.7.0版本的API，将在未来版本移除，请使用chartBase.dataSetBindsAttachment()
	/**
	 * 获取附件数据集绑定对象数组，它们的用途不是绘制图表。
	 * 
	 * @return []，空数组表示没有附件数据集绑定
	 */
	chartBase.chartDataSetsAttachment = function()
	{
		return this.dataSetBindsAttachment();
	};
	// > @deprecated 兼容4.7.0版本的API，将在未来版本移除，请使用chartBase.dataSetBindsAttachment()
	
	// < @deprecated 兼容4.7.0版本的API，将在未来版本移除，请使用chartBase.dataSetBindAt()
	/**
	 * 获取指定索引的数据集绑定对象，没有则返回undefined。
	 * 
	 * @param index
	 */
	chartBase.chartDataSetAt = function(index)
	{
		return this.dataSetBindAt(index);
	};
	// > @deprecated 兼容4.7.0版本的API，将在未来版本移除，请使用chartBase.dataSetBindAt()
	
	// < @deprecated 兼容4.7.0版本的API，将在未来版本移除，请使用chartBase.dataSetBindMain()
	/**
	 * 获取第一个主件数据集绑定对象。
	 * 主件数据集绑定的用途是绘制图表。
	 * 
	 * @return 未找到时返回null
	 * @since 3.0.0
	 */
	chartBase.chartDataSetMain = function()
	{
		return this.dataSetBindMain();
	};
	// < @deprecated 兼容4.7.0版本的API，将在未来版本移除，请使用chartBase.dataSetBindMain()
	
	// < @deprecated 兼容4.7.0版本的API，将在未来版本移除，请使用chartBase.dataSetBindAttachment()
	/**
	 * 获取第一个附件数据集绑定对象。
	 * 附件数据集绑定的用途不是绘制图表。
	 * 
	 * @return 未找到时返回null
	 * @since 3.0.0
	 */
	chartBase.chartDataSetAttachment = function()
	{
		return this.dataSetBindAttachment();
	};
	// > @deprecated 兼容4.7.0版本的API，将在未来版本移除，请使用chartBase.dataSetBindAttachment()
	
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
	 * 图表事件支持函数：获取/设置图表事件数据（chartBase.eventData(chartEvent)返回值）对应的原始数据集绑定索引（chartEvent.originalChartDataSetIndex）。
	 * 
	 * @param chartEvent 图表事件对象，格式应为：{ ... }
	 * @param originalDataSetBindIndex 可选，要设置的原始数据集绑定索引，格式应为：
	 *                                  当图表事件数据是对象时：数据集绑定索引数值、数据集绑定索引数值数组
	 *                                  当图表事件数据是对象数组时：数组，其元素可能为数据集绑定索引数值、数据集绑定索引数值数组
	 *                                  其中，数据集绑定索引数值允许为null，因为图表事件数据可能并非由图表结果数据构建
	 * @returns 要获取的原始数据集绑定索引，未设置则返回null
	 */
	chartBase.eventOriginalChartDataSetIndex = function(chartEvent, originalDataSetBindIndex)
	{
		if(originalDataSetBindIndex === undefined)
			return chartEvent["originalChartDataSetIndex"];
		else
			chartEvent["originalChartDataSetIndex"] = originalDataSetBindIndex;
	};
	
	/**
	 * 图表事件支持函数：获取/设置图表事件数据（chartBase.eventData(chartEvent)返回值）对应的原始数据集结果数据索引（chartEvent.originalResultDataIndex）。
	 * 
	 * @param chartEvent 图表事件对象，格式应为：{ ... }
	 * @param originalResultDataIndex 可选，要设置的原始数据集结果数据索引，格式应为：
	 *                                与chartBase.eventOriginalChartDataSetIndex(chartEvent)返回值格式一致，
	 *                                只是每一个数据集绑定索引数值可能对应一个数据集结果数据索引数值、也可能对应一个数据集结果数据索引数值数组
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
	 * 图表事件支持函数：设置图表事件对象的原始数据集绑定索引、原始数据、原始结果数据索引。
	 * 
	 * @param chartEvent 图表事件对象，格式应为：{ ... }
	 * @param originalInfo 图表数据对象、数组，或者原始信息对象、数组（格式参考：chartBase.originalInfo函数返回值），或者原始数据集绑定索引数值（用于兼容旧版API）
	 * @param originalResultDataIndex 可选，当originalInfo是索引数值时的原始数据索引，格式可以是：数值、数值数组
	 */
	chartBase.eventOriginalInfo = function(chartEvent, originalInfo, originalResultDataIndex)
	{
		var ocdsi = null;
		var ordi = null;
		var odata = null;
		
		var chartResult = this.updateResult();
		
		if(originalInfo == null)
		{
		}
		else if(typeof(originalInfo) == "number")
		{
			ocdsi = originalInfo;
			ordi = originalResultDataIndex;
			
			odata = this.resultDataElement(this.resultOf(chartResult, ocdsi), ordi);
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
						odata[i][j] = this.resultDataElement(this.resultOf(chartResult, myOcdsi[j]), (myOrdi ? myOrdi[j] : null));
				}
				else
				{
					odata[i] = this.resultDataElement(this.resultOf(chartResult, myOcdsi), myOrdi);
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
	 * 获取/设置指定数据对象的原始信息属性值，包括：图表ID、数据集绑定索引、结果数据索引。
	 * 图表渲染器在构建用于渲染图表的内部数据对象时，应使用此函数设置其原始信息，以支持在后续的交互、事件处理中获取这些原始信息。
	 * 
	 * @param data 数据对象、数据对象数组，格式为：{ ... }、[ { ... }, ... ]，当是数组时，设置操作将为每个元素单独设置原始信息
	 * @param dataSetBindIndex 要设置的数据集绑定索引数值、数据集绑定对象（自动取其索引数值），或者它们的数组
	 * @param resultDataIndex 可选，要设置的结果数据索引，格式为：
	 *                        当dataSetBindIndex不是数组时：
	 *                        数值、数值数组
	 *                        当dataSetBindIndex是数组时：
	 *                        数值，表示dataSetBindIndex数组每个元素的结果数据索引都是此数值
	 *                        数组（元素可以是数值、数值数组），表示dataSetBindIndex数组每个元素的结果数据索引是此数组对应位置的元素
	 *                        默认值为：0
	 * @param autoIncrement 可选，当data是数组时：
	 *                      当dataSetBindIndex不是数组且resultDataIndex是数值时，设置时是否自动递增resultDataIndex；
	 *                      当dataSetBindIndex是数组且其元素对应位置的结果数据索引是数值时，是否自动递增这个结果数据索引是数值。
	 *                      默认值为：true
	 * @returns 要获取的原始信息属性值(可能为null），格式为：
	 *									{
	 *										//图表ID
	 *										chartId: "...",
	 *										//数据集绑定索引数值、数值数组
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
	chartBase.originalInfo = function(data, dataSetBindIndex, resultDataIndex, autoIncrement)
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
			
			//(data, dataSetBindIndex, true)、(data, dataSetBindIndex, false)
			if(resultDataIndex === true || resultDataIndex === false)
			{
				autoIncrement = resultDataIndex;
				resultDataIndex = undefined;
			}
			
			resultDataIndex = (resultDataIndex === undefined ? 0 : resultDataIndex);
			
			var isCdsiArray = $.isArray(dataSetBindIndex);
			
			if(isCdsiArray)
			{
				var cdsiNew = [];
				
				for(var i=0; i<dataSetBindIndex.length; i++)
				{
					cdsiNew[i] = (dataSetBindIndex[i] != null && dataSetBindIndex[i].index !== undefined ?
									dataSetBindIndex[i].index : dataSetBindIndex[i]);
				}
				
				dataSetBindIndex = cdsiNew;
				
				if(!$.isArray(resultDataIndex))
				{
					var rdiNew = [];
					
					for(var i=0; i<dataSetBindIndex.length; i++)
						rdiNew[i] = resultDataIndex;
					
					resultDataIndex = rdiNew;
				}
			}
			else
			{
				dataSetBindIndex = (dataSetBindIndex != null && dataSetBindIndex.index !== undefined ?
										dataSetBindIndex.index : dataSetBindIndex);
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
						"chartDataSetIndex": dataSetBindIndex
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
					"chartDataSetIndex": dataSetBindIndex,
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
	 * @param chartResult 图表结果
	 * @param attachment 可选，true 获取第一个附件图表数据集结果；false 获取第一个主件图表数据集结果。默认值为：false
	 */
	chartBase.resultFirst = function(chartResult, attachment)
	{
		attachment = (attachment == null ? false : attachment);
		
		var index = undefined;
		
		var dataSetBinds = this.dataSetBinds();
		for(var i=0; i<dataSetBinds.length; i++)
		{
			var isAttachment = dataSetBinds[i].attachment;
			
			if((isAttachment && attachment == true) || (!isAttachment && attachment != true))
			{
				index = i;
				break;
			}
		}
		
		return (index == null ? undefined : this.resultOf(chartResult, index));
	};
	// > @deprecated 兼容2.13.0版本的API，将在未来版本移除，请使用chartBase.resultOf()
	
	// < @deprecated 兼容2.13.0版本的API，将在未来版本移除，请使用chartBase.resultDatasOf()
	/**
	 * 获取第一个主件或者附件数据集结果的数据对象数组。
	 * 如果数据对象是null，返回空数组：[]；如果数据对象是数组，则直接返回；否则，返回：[ 数据对象 ]。
	 * 
	 * @param chartResult 图表结果
	 * @param attachment 可选，true 获取第一个附件图表数据集结果；false 获取第一个主件图表数据集结果。默认值为：false
	 * @return 不会为null的数组
	 */
	chartBase.resultDatasFirst = function(chartResult, attachment)
	{
		var dataSetResult = this.resultFirst(chartResult, attachment);
		return this.resultDatas(dataSetResult);
	};
	// > @deprecated 兼容2.13.0版本的API，将在未来版本移除，请使用chartBase.resultDatasOf()
	
	// < @deprecated 兼容2.13.0版本的API，将在未来版本移除，已被chartBase.dataSetBindMain()、dataSetBindAttachment()取代
	/**
	 * 获取第一个主件或者附件数据集绑定对象。
	 * 
	 * @param attachment 可选，true 获取第一个附件数据集绑定；false 获取第一个主件数据集绑定。默认值为：false
	 * @return {...} 或  undefined
	 */
	chartBase.chartDataSetFirst = function(attachment)
	{
		attachment = (attachment == null ? false : attachment);
		
		var re = undefined;
		
		var dataSetBinds = this.dataSetBinds();
		for(var i=0; i<dataSetBinds.length; i++)
		{
			var isAttachment = dataSetBinds[i].attachment;
			
			if((isAttachment && attachment == true) || (!isAttachment && attachment != true))
			{
				re = dataSetBinds[i];
				break;
			}
		}
		
		return re;
	};
	// > @deprecated 兼容2.13.0版本的API，将在未来版本移除，已被chartBase.dataSetBindMain()、dataSetBindAttachment()取代
	
	// < @deprecated 兼容2.9.0版本的API，将在未来版本移除，已被chartBase.hasDataSetParam()取代
	/**
	 * 判断此图表是否有参数化数据集。
	 */
	chartBase.hasParamDataSet = function()
	{
		return this.hasDataSetParam();
	};
	// > @deprecated 兼容2.9.0版本的API，将在未来版本移除，已被chartBase.hasDataSetParam()取代
	
	// < @deprecated 兼容2.9.0版本的API，将在未来版本移除，已被chartBase.dataSetFieldAlias()取代
	/**
	 * 获取数据集字段标签，它不会返回null。
	 * 
	 * @param dataSetField
	 * @returns "..."
	 */
	chartBase.dataSetPropertyLabel = function(dataSetField)
	{
		if(!dataSetField)
			return "";
		
		var label = (dataSetField.label ||  dataSetField.name);
		
		return (label || "");
	};
	// > @deprecated 兼容2.9.0版本的API，将在未来版本移除，已被chartBase.dataSetFieldAlias()取代
	
	// < @deprecated 兼容2.9.0版本的API，将在未来版本移除，已被chartBase.dataSetAlias()取代
	/**
	 * 获取指定数据集绑定对象名称，它不会返回null。
	 * 
	 * @param dataSetBind 数据集绑定对象
	 */
	chartBase.chartDataSetName = function(dataSetBind)
	{
		return this.dataSetAlias(dataSetBind);
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
	
	// < @deprecated 兼容2.3.0版本的API，将在未来版本移除，已被chartBase.dataSetBinds()取代
	/**
	 * 获取所有数据集绑定对象数组。
	 */
	chartBase.chartDataSetsNonNull = function()
	{
		return this.dataSetBinds();
	};
	// > @deprecated 兼容2.3.0版本的API，将在未来版本移除，已被chartBase.dataSetBinds()取代
	
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
	 * 获取指定数据集绑定参数值对象。
	 */
	chartBase.getDataSetParamValues = function(dataSetBind)
	{
		return this.dataSetParamValues(dataSetBind);
	};
	// > @deprecated 兼容1.8.1版本的API，将在未来版本移除，已被chartBase.dataSetParamValues取代
	
	// < @deprecated 兼容1.8.1版本的API，将在未来版本移除，已被chartBase.dataSetParamValues取代
	/**
	 * 设置指定数据集绑定多个参数值。
	 */
	chartBase.setDataSetParamValues = function(dataSetBind, paramValues)
	{
		this.dataSetParamValues(dataSetBind, paramValues);
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
			var dataSetBindIndex = odi.dataSetBindIndex;
			var resultDataIndex = odi.resultDataIndex;
			var chartResult = chart.updateResult();
			var originalDataMy = null;
			
			if($.isArray(dataSetBindIndex))
			{
				originalDataMy = [];
				
				for(var j=0; j<dataSetBindIndex.length; j++)
				{
					var result = chart.resultOf(chartResult, dataSetBindIndex[j]);
					originalDataMy[j] = chart.resultDataElement(result, (resultDataIndex != null ? resultDataIndex[j] : null));
				}
			}
			else
			{
				var result = chart.resultOf(chartResult, dataSetBindIndex);
				originalDataMy = chart.resultDataElement(result, resultDataIndex);
			}
			
			originalData[i] = originalDataMy;
		}
		
		return (isArray ? originalData : originalData[0]);
	};
	
	/**
	 * 获取/设置单条图表展示数据的原始数据索引（图表ID、数据集绑定索引、结果数据索引）。
	 * 
	 * @param data 展示数据，格式为：{ ... }、[ ... ]
	 * @param dataSetBindIndex 数据集绑定索引数值、数值数组
	 * @param resultDataIndex 图表数据集结果数据索引数值、数值数组、数值数组的数组
	 * @returns 要获取的原始数据索引(可能为null），格式参考chartBase.originalDataIndexes()函数返回值
	 * @since 3.1.0
	 */
	chartFactory.originalDataIndex = function(data, chartId, dataSetBindIndex, resultDataIndex)
	{
		var pname = chartFactory._ORIGINAL_DATA_INDEX_PROP_NAME;
		
		//获取
		if(arguments.length <= 1)
		{
			return (data == null ? undefined : data[pname]);
		}
		else
		{
			var originalIdx =
			{
				"chartId": chartId,
				"dataSetBindIndex": dataSetBindIndex,
				"resultDataIndex": resultDataIndex
			};
			
			// < @deprecated 兼容4.7.0版本的originalIdx.chartDataSetIndex，将在未来版本移除，已被originalIdx.dataSetBindIndex取代
			originalIdx.chartDataSetIndex = originalIdx.dataSetBindIndex;
			// > @deprecated 兼容4.7.0版本的originalIdx.chartDataSetIndex，将在未来版本移除，已被originalIdx.dataSetBindIndex取代
			
			//无需区分是否数组，因为数组也可以这样设置属性
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
	 * 为指定URL添加系统根路径前缀。
	 * 只有当URL以"/"开头时才会添加系统根路径前缀，否则，将直接返回原URL。
	 * 当需要访问系统内其他功能模块的资源时，应为其URL添加系统根路径前缀。
	 * 
	 * @param webContext web上下文
	 * @param url 可选，要处理的URL
	 * @return 添加后的新URL，如果未设置url参数，将返回系统根路径
	 */
	chartFactory.toWebContextPathURL = function(webContext, url)
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
	 * 获取HTML元素自身或其子孙元素中带有非空图表部件ID属性（"dg-chart-widget"）的全部元素。
	 * 
	 * @param element HTML元素、Jquery对象
	 * @returns DOM数组
	 */
	chartFactory.domsWithWidgetId = function(element)
	{
		element = $(element);
		element = element.add($("["+chartFactory.elementAttrConst.WIDGET+"]", element));
		
		var widgetEles = [];
		
		//处理元素自身
		element.each(function()
		{
			if(!chartFactory.isNullOrEmpty(chartFactory.elementWidgetId(this)))
			{
				widgetEles.push(this);
			}
		});
		
		return widgetEles;
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
	 * 校验设置图表元素ID。
	 * 图表元素必须有ID，且要与图表中的元素ID同步。
	 * 
	 * @param element
	 * @param chart 可选，要同步的图表
	 */
	chartFactory.checkSetChartElementId = function(element, chart)
	{
		element = $(element);
		
		var elementId = element.attr("id");
		if(!elementId)
		{
			elementId = chartFactory.uid();
			element.attr("id", elementId);
		}
		
		if(chart)
			chart.elementId = elementId;
		
		return elementId;
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
	 * 给URL追加参数。
	 * 
	 * @param url
	 * @param name 参数名
	 * @param value 参数值
	 */
	chartFactory.appendUrlParam = function(url, name, value)
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
	 * 静默执行函数。
	 * 
	 * @param func 函数
	 * @param exceptionHandler 可选，异常处理函数
	 */
	chartFactory.executeSilently = function(func, exceptionHandler)
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
				chartFactory.logException(e);
			}
		}
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
	 * @param color 颜色字符串，格式为："#FFF"、"#FFFFFF"、"#FFFFFF80"、"rgb(255,255,255)"、"rgba(255,255,255, 0.5)"
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
		var a = (color.a != null ? new Number(parseInt(color.a*255)).toString(16) : undefined);
		
		color = (prefix ? "#" : "") + (r.length == 1 ? "0"+r : r)
					 + (g.length == 1 ? "0"+g : g)
					 + (b.length == 1 ? "0"+b : b)
					 + (a != null ? (a.length == 1 ? "0"+a : a) : "");
		
		return color;
	};
	
	/**
	 * 解析颜色对象。
	 * 将颜色字符串解析为{r: number, g: number, b: number, a: number}格式的对象。
	 * 
	 * @param color 颜色字符串，格式为："#FFF"、"#FFFFFF"、"#FFFFFF80"、"rgb(255,255,255)"、"rgba(255,255,255, 0.5)"
	 */
	chartFactory.parseColor = function(color)
	{
		//默认a值应为undefined
		var re = {r: 0, g: 0, b: 0, a: undefined};
		
		if(!color)
			return re;
		
		//是颜色名称（red、green、yellow等），则通过元素css函数转换
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
		
		// #FFF、#FFFFFF、#FFFFFFFF
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
			if(color.length >= 8)
				re.a = parseInt(color.substr(6, 2), 16)/255;
		}
		// rgb()、rgba()
		else
		{
			var si = color.indexOf("(");
			var ei = (si >= 0 ? color.lastIndexOf(")") : -1);
			
			if(ei > si)
			{
				color = color.substring(si+1, ei);
				
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
			}
			else
				color = [];
			
			if(color.length >= 1)
				re.r = parseInt(color[0]);
			if(color.length >= 2)
				re.g = parseInt(color[1]);
			if(color.length >= 3)
				re.b = parseInt(color[2]);
			if(color.length >= 4 && color[3] != null)
				re.a = parseFloat(color[3]);
		}
		
		return re;
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
		if($lastGenStyle.length > 0)
		{
			$lastGenStyle.after($style);
			return;
		}
		
		var $lastImport = $("["+chartFactory.LIB_ATTR_NAME+"]:last", $head);
		
		if($lastImport.length > 0)
		{
			$lastImport.after($style);
			return;
		}
		
		var $firstLink = $("link:first", $head);
		
		if($firstLink.length > 0)
		{
			$firstLink.before($style);
			return;
		}
		
		var $firstStyle = $("style:first", $head);
		
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
		
		return "dgid" + time + seq;
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
		if(chartFactory.isNullOrEmpty(fontSize))
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
	
	//是否为null、undefined、空字符串、空数组
	chartFactory.isNullOrEmpty = function(v)
	{
		return (v == null || v === "" || (v.length !== undefined && v.length === 0));
	};
	
	/**
	 * 在数组中查找元素，返回其索引
	 * 
	 * @param array
	 * @param value
	 * @returns 索引数值，-1 表示没有找到
	 */
	chartFactory.indexInArray = function(array, value)
	{
		if(array == null)
			return -1;
		
		for(var i=0; i<array.length; i++)
		{
			if(array[i] === value)
			{
				return i;
			}
		}
		
		return -1;
	};
	
	/**
	 * 比较版本号。
	 * 支持版本号格式示例：
	 * 1、1-alpha、1.1、1.1-alpha、1.1.1、1.1.1-alpha、1.1.1.1、1.1.1.1-alpha
	 * 
	 * 此函数原封不动地拷贝自util.js中的$.compareVersion函数
	 * 
	 * @param v1
	 * @param v2
	 * @returns -1 v1低于v2；0 v1等于v2；1 v1高于v2
	 */
	chartFactory.compareVersion = function(v1, v2)
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
		var areaColor0 = chartFactory.themeGradualColor(chartTheme, 0.1);
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
	chartFactory.loadLib = function(lib, callback, contextCharts)
	{
		contextCharts = (contextCharts == null ? [] : contextCharts);
		
		if(!lib)
		{
			callback();
		}
		
		if(!$.isArray(lib))
			lib = [ lib ];
		
		var unloadeds = [];
		chartFactory.inflateUnloadedLibs(contextCharts, lib, unloadeds);
		
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
				chartFactory.loadLibInner(unloadeds, stateObjs);
			};
			
			for(var i=0; i<unloadeds.length; i++)
			{
				var stateObj = chartFactory.libState(unloadeds[i], true, chartFactory.LIB_STATE_INIT, false, loadedCallback);
				stateObjs.push(stateObj);
				deferreds.push(stateObj.loadedDeferred);
			}
			
			$.when.apply($, deferreds).always(function(){ callback(); });
			
			for(var i=0; i<stateObjs.length; i++)
			{
				chartFactory.triggerLibStateResolvedIfLoaded(stateObjs[i]);
			}
			
			chartFactory.loadLibInner(unloadeds, stateObjs);
		}
	};
	
	//填充所有待加载库，填充后，unloadeds中都是最新版本库，且都包含依赖库
	chartFactory.inflateUnloadedLibs = function(contextCharts, libs, unloadeds)
	{
		for(var i=0; i<libs.length; i++)
		{
			var lib = libs[i];
			
			if(chartFactory.isLibLoadedInEnv(lib))
			{
				continue;
			}
			
			var stateObj = chartFactory.libState(lib);
			if(stateObj && stateObj.state == chartFactory.LIB_STATE_LOADED)
			{
				continue;
			}
			
			var latestLib = chartFactory.findLatestLibInCharts(contextCharts, lib);
			
			if(latestLib !== lib)
			{
				if(chartFactory.isLibLoadedInEnv(latestLib))
				{
					//如果最新版已在环境中加载，应将其状态设为loaded，以减少后续加载操作的搜索步骤
					chartFactory.libState(latestLib, true, chartFactory.LIB_STATE_LOADED, true);
					continue;
				}
				
				stateObj = chartFactory.libState(latestLib);
				if(stateObj && stateObj.state == chartFactory.LIB_STATE_LOADED)
				{
					continue;
				}
			}
			
			if(chartFactory.libIndex(unloadeds, latestLib.name) > -1)
				continue;
			
			unloadeds.push(latestLib);
			
			//处理依赖
			if(latestLib.depend)
			{
				var depend = latestLib.depend;
				var dependLibs = [];
				
				if(!$.isArray(depend))
					depend = [ depend ];
				
				for(var j=0; j<depend.length; j++)
				{
					var dependName = depend[j];
					
					if(chartFactory.libIndex(unloadeds, dependName) > -1)
						continue;
					
					if(chartFactory.libIndex(libs, dependName) > -1)
						continue;
					
					if(chartFactory.libIndex(dependLibs, dependName) > -1)
						continue;
					
					var dependLib = chartFactory.findFirstLibInCharts(contextCharts, dependName);
					
					if(dependLib != null)
					{
						dependLibs.push(dependLib);
					}
					else
					{
						chartFactory.logException("No lib found for name '"+dependName+"', load ignored");
					}
				}
				
				if(dependLibs.length > 0)
				{
					chartFactory.inflateUnloadedLibs(contextCharts, dependLibs, unloadeds);
				}
			}
		}
	};
	
	chartFactory.loadLibInner = function(libs, stateObjs)
	{
		for(var i=0; i<libs.length; i++)
		{
			var lib = libs[i];
			var stateObj = stateObjs[i];
			
			if(stateObj.state === chartFactory.LIB_STATE_INIT && chartFactory.isLibReadyForLoad(lib))
			{
				stateObj.state = chartFactory.LIB_STATE_LOADING;
				
				var source = stateObj.lib.source;
				var srcDfds = stateObj.sourceLoadedDeferreds;
				
				if(source != null)
				{
					if(!$.isArray(source))
						source = [ source ];
					
					for(var j=0; j<source.length; j++)
					{
						chartFactory.loadSingleLibSource(lib, source[j], srcDfds[j]);
					}
				}
			}
		}
	};
	
	chartFactory.isLibReadyForLoad = function(lib)
	{
		var depend = lib.depend;
		
		if(chartFactory.isNullOrEmpty(depend))
			return true;
		
		if(!$.isArray(depend))
			depend = [ depend ];
		
		var ready = true;
		
		for(var i=0; i<depend.length; i++)
		{
			var dependName = depend[i];
			var dependStateObj = chartFactory.libStateByName(dependName);
			//没有找到依赖库也应认为已ready，因为通过HTML的<script>标签引入的库这里dependStateObj为null
			ready = (dependStateObj == null || dependStateObj.state == chartFactory.LIB_STATE_LOADED);
			
			if(!ready)
			{
				break;
			}
		}
		
		return ready;
	};
	
	chartFactory.loadSingleLibSource = function(lib, source, deferred)
	{
		if(deferred.state() !== "pending")
			return;
		
		if(chartFactory.isString(source))
		{
			source = { url: source, type: chartFactory.resolveLibSourceType(source) };
		}
		
		if(source.type == "js")
		{
			chartFactory.loadSingleJsLibSource(lib, source, deferred);
		}
		else if(source.type == "css")
		{
			chartFactory.loadSingleCssLibSource(lib, source, deferred);
		}
		else
		{
			deferred.resolve();
			chartFactory.logException("Unknown lib source type '"+source.type+"', load ignored");
		}
	};
	
	chartFactory.loadSingleJsLibSource = function(lib, source, deferred)
	{
		var ele = document.createElement("script");
		
		ele.src = source.url;
		ele.type = "text/javascript";
		ele.onload = function(){ deferred.resolve(); };
		ele.onerror = function(){ deferred.resolve(); };
		
		chartFactory.addLibSourceEleToDoc(lib, ele);
	};
	
	chartFactory.loadSingleCssLibSource = function(lib, source, deferred)
	{
		var ele = document.createElement("link");
		
		ele.href = source.url;
		ele.type = "text/css";
		ele.rel = "stylesheet";
		ele.onload = function(){ deferred.resolve(); };
		ele.onerror = function(){ deferred.resolve(); };
		
		chartFactory.addLibSourceEleToDoc(lib, ele);
	};
	
	/**
	 * 在DOM中插入依赖库源。
	 * 插入规则：
	 * 一级优先：插入在最后一个看板引入库（dg-lib-name）之后、且为其添加dg-lib-name属性，
	 * 			确保其可以使用之前依赖库和内置引入库、且可以被全部生成样式表覆盖（参考chartFactory.styleSheetText()函数说明）；
	 * 二级优先：插入在<head>末尾。
	 * 
	 * @param lib 库对象
	 * @param ele 库对应的DOM对象
	 */
	chartFactory.addLibSourceEleToDoc = function(lib, ele)
	{
		$(ele).attr(chartFactory.LIB_ATTR_NAME, lib.name);
		
		var $head = $("head:first");
		var headEle = $head[0];
		var beforeEle = null;
		
		var $lastImport = $("["+chartFactory.LIB_ATTR_NAME+"]:last", $head);
		if($lastImport.length > 0)
		{
			var $next = $lastImport.next();
			if($next.length > 0)
			{
				beforeEle = $next[0];
			}
		}
		
		//这里不能使用$的API，会无法正常执行绑定事件
		if(beforeEle != null)
			headEle.insertBefore(ele, beforeEle);
		else
			headEle.appendChild(ele);
	};
	
	chartFactory.resolveLibSourceType = function(url)
	{
		var qsIdx = url.indexOf("?");
		if(qsIdx < 0)
			qsIdx = url.indexOf("#");
		
		if(qsIdx > 0)
			url = url.substring(0, qsIdx);
		
		var type = "";
		
		if(chartFactory.LIB_JS_SOURCE_REGEX.test(url))
		{
			type = "js";
		}
		else if(chartFactory.LIB_CSS_SOURCE_REGEX.test(url))
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
	
	chartFactory.LIB_JS_SOURCE_REGEX = /\.(js)$/i;
	chartFactory.LIB_CSS_SOURCE_REGEX = /\.(css)$/i;
	
	//查找最新版的库
	chartFactory.findLatestLibInCharts = function(charts, lib)
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
			var rendererLib = chartFactory.rendererLib(renderer);
			rendererLatestLib = chartFactory.findLatestLibInLibs(rendererLib, rendererLatestLib);
		}
		
		for(var i=0; i<charts.length; i++)
		{
			var chart = charts[i];
			var pluginRenderer = (chart.plugin ? chart.plugin.renderer : null);
			var rendererLib = chartFactory.rendererLib(pluginRenderer);
			var myPluginLatestLib = chartFactory.findLatestLibInLibs(rendererLib, pluginLatestLib);
			
			if(myPluginLatestLib !== pluginLatestLib)
			{
				pluginLatestLib = myPluginLatestLib;
				pluginLatestLibChart = chart;
			}
		}
		
		//图表渲染器在看板页面定义，所以其依赖库应该优先使用
		var latestLib = chartFactory.resolveLatestLibByBase(rendererLatestLib, pluginLatestLib);
		
		//如果是插件依赖库，需要转换为可用依赖库
		if(latestLib !== lib && latestLib === pluginLatestLib && pluginLatestLibChart != null)
		{
			latestLib = chartFactory.convertPluginRendererLib(pluginLatestLibChart, latestLib);
		}
		
		return latestLib;
	};
	
	chartFactory.findLatestLibInLibs = function(libs, lib)
	{
		if(libs == null)
			return lib;
		
		var latestLib = lib;
		
		if($.isArray(libs))
		{
			for(var i=0; i<libs.length; i++)
			{
				latestLib = chartFactory.resolveLatestLibByBase(latestLib, libs[i]);
			}
		}
		else
		{
			latestLib = chartFactory.resolveLatestLibByBase(latestLib, libs);
		}
		
		return latestLib;
	};
	
	//如果compareLib与baseLib同名，且版本更高，返回compareLib；否则，返回baseLib
	chartFactory.resolveLatestLibByBase = function(baseLib, compareLib)
	{
		if(compareLib == null)
			return baseLib;
		
		var latestLib = baseLib;
		
		var name = chartFactory.resolveSameLibName(baseLib.name, compareLib.name);
		
		if(name != null)
		{
			//只有找到更高版本号的才替换，否则应该优先使用传入的lib参数
			var lower = (chartFactory.compareLibVersion(name, baseLib.version, compareLib.version) < 0);
			
			if(lower)
			{
				latestLib = compareLib;
			}
		}
		
		return latestLib;
	};
	
	//查找第一个库
	chartFactory.findFirstLibInCharts = function(charts, name)
	{
		if(charts == null)
			return null;
		
		for(var i=0; i<charts.length; i++)
		{
			var chart = charts[i];
			var renderer = chart.renderer();
			var rendererLib = chartFactory.rendererLib(renderer);
			var firstLib = chartFactory.findFirstLibInLibs(rendererLib, name);
			
			if(firstLib != null)
				return firstLib;
			
			var pluginRenderer = (chart.plugin ? chart.plugin.renderer : null);
			rendererLib = chartFactory.rendererLib(pluginRenderer);
			firstLib = chartFactory.findFirstLibInLibs(rendererLib, name);
			
			//插件依赖库需要转换为可用依赖库
			if(firstLib != null)
				return chartFactory.convertPluginRendererLib(chart, firstLib);
		}
		
		return null;
	};
	
	//查找第一个库
	chartFactory.findFirstLibInLibs = function(libs, name)
	{
		if(libs == null)
			return null;
		
		if($.isArray(libs))
		{
			for(var i=0; i<libs.length; i++)
			{
				if(chartFactory.resolveSameLibName(libs[i].name, name))
					return libs[i];
			}
		}
		else
		{
			if(chartFactory.resolveSameLibName(libs.name, name))
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
	chartFactory.compareLibVersion = function(name, v1, v2)
	{
		return chartFactory.compareVersion(v1, v2);
	};
	
	//查找第一个同名的库索引
	chartFactory.libIndex = function(libs, name)
	{
		for(var i=0; i<libs.length; i++)
		{
			if(chartFactory.resolveSameLibName(libs[i].name, name))
				return i;
		}
		
		return -1;
	};
	
	//当前环境是否已加载了指定库
	chartFactory.isLibLoadedInEnv = function(lib)
	{
		if(lib.loaded != null)
		{
			return lib.loaded();
		}
		else
		{
			if(chartFactory.isString(lib.name))
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
	chartFactory.resolveSameLibName = function(baseLibName, compareLibName)
	{
		if(baseLibName == null || baseLibName.length == 0
			|| compareLibName == null || compareLibName.length == 0)
		{
			return null;
		}
		
		var baseNameArray = (!chartFactory.isString(baseLibName));
		
		if(baseLibName === compareLibName)
		{
			if(!baseNameArray)
				return baseLibName;
			else
				return baseLibName[0];
		}
		
		var compareNameArray = (!chartFactory.isString(compareLibName));
		
		if(!baseNameArray && !compareNameArray)
		{
			return null;
		}
		else if(!baseNameArray)
		{
			var idx = chartFactory.indexInArray(compareLibName, baseLibName);
			return (idx > -1 ? baseLibName : null);
		}
		else if(!compareNameArray)
		{
			var idx = chartFactory.indexInArray(baseLibName, compareLibName);
			return (idx > -1 ? compareLibName : null);
		}
		else
		{
			for(var i=0; i<baseLibName; i++)
			{
				var idx = chartFactory.indexInArray(compareLibName, baseLibName[i]);
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
	chartFactory.libState = function(lib, nonNull, createState, resolvedIfLoaded, loadedCallback)
	{
		if(nonNull !== true)
		{
			return chartFactory.libStateByName(lib.name);
		}
		else
		{
			var stateObj = chartFactory.libState(lib);
			
			if(stateObj == null)
			{
				var states = chartFactory.LIB_STATES;
				stateObj = chartFactory.createLibState(lib, createState, resolvedIfLoaded, loadedCallback);
				
				if(chartFactory.isString(lib.name))
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
	chartFactory.libStateByName = function(name)
	{
		if(name == null)
			return null;
		
		var states = chartFactory.LIB_STATES;
		
		if(chartFactory.isString(name))
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
	
	chartFactory.createLibState = function(lib, state, resolvedIfLoaded, loadedCallback)
	{
		//应深度复制lib，避免可能的修改导致状态错乱
		lib = chartFactory.deepCloneLib(lib);
		state = (state == null ? chartFactory.LIB_STATE_INIT : state);
		resolvedIfLoaded = (resolvedIfLoaded == null ? false : resolvedIfLoaded);
		loadedCallback = (loadedCallback == null ? null : loadedCallback);
		
		//无论state是何状态，都应设置loadedDeferred、sourceLoadedDeferreds，
		//确保其在异步调用中结构完整
		var stateObj =
		{
			//库对象
			lib: lib,
			//库状态，参考：chartFactory.LIB_STATE_*
			state: state,
			//库加载完成后的回调函数
			loadedDeferred: $.Deferred(),
			//库中source对应的加载完成后回调函数
			sourceLoadedDeferreds: []
		};
		
		stateObj.loadedDeferred.always(function()
		{
			stateObj.state = chartFactory.LIB_STATE_LOADED;
			
			if(loadedCallback != null)
			{
				chartFactory.executeSilently(loadedCallback);
			}
		});
		
		var source = stateObj.lib.source;
		var sourceLen = (source == null ? 0 : ($.isArray(source) ? source.length : 1));
		
		if(sourceLen > 0)
		{
			for(var i=0; i<sourceLen; i++)
			{
				stateObj.sourceLoadedDeferreds[i] = $.Deferred();
			}
			
			$.when.apply($, stateObj.sourceLoadedDeferreds).always(function(){ stateObj.loadedDeferred.resolve(); });
		}
		
		if(resolvedIfLoaded)
		{
			chartFactory.triggerLibStateResolvedIfLoaded(stateObj);
		}
		
		return stateObj;
	};
	
	chartFactory.triggerLibStateResolvedIfLoaded = function(stateObj)
	{
		var source = stateObj.lib.source;
		var sourceLen = (source == null ? 0 : ($.isArray(source) ? source.length : 1));
		
		if(sourceLen == 0)
		{
			stateObj.state = chartFactory.LIB_STATE_LOADED;
			stateObj.loadedDeferred.resolve();
		}
		
		if(stateObj.state == chartFactory.LIB_STATE_LOADED)
		{
			for(var i=0; i<sourceLen; i++)
			{
				stateObj.sourceLoadedDeferreds[i].resolve();
			}
		}
	};
	
	chartFactory.deepCloneLib = function(lib)
	{
		if(!lib)
			return lib;
		
		if($.isArray(lib))
		{
			var newLibs = [];
			
			for(var i=0; i<lib.length; i++)
			{
				var newLib = $.extend(true, {}, lib[i]);
				newLibs.push(newLib);
			}
			
			return newLibs;
		}
		else
		{
			var newLib = $.extend(true, {}, lib);
			return newLib;
		}
	};
	
	//库及其状态，键值结构：库名 -> 库信息。
	chartFactory.LIB_STATES = {};
	
	//库状态：初始化
	chartFactory.LIB_STATE_INIT = "init";
	//库状态：加载中
	chartFactory.LIB_STATE_LOADING = "loading";
	//库状态：加载完成
	chartFactory.LIB_STATE_LOADED = "loaded";
	
	chartFactory.convertPluginRendererLib = function(chart, lib)
	{
		if(!lib)
			return lib;
		
		lib = chartFactory.deepCloneLib(lib);
		
		if($.isArray(lib))
		{
			for(var i=0; i<lib.length; i++)
			{
				chartFactory.trimPluginRendererLibSource(chart, lib[i]);
			}
		}
		else
		{
			chartFactory.trimPluginRendererLibSource(chart, lib);
		}
		
		return lib;
	};
	
	chartFactory.trimPluginRendererLibSource = function(chart, lib)
	{
		if(!lib.source)
			return;
		
		if($.isArray(lib.source))
		{
			for(var i=0; i<lib.source.length; i++)
			{
				lib.source[i] = chartFactory.trimPluginRendererLibSourceUrl(chart, lib.source[i]);
			}
		}
		else
		{
			lib.source = chartFactory.trimPluginRendererLibSourceUrl(chart, lib.source);
		}
	};
	
	//将图表插件的依赖库url解析为可直接加载的绝对路径
	chartFactory.trimPluginRendererLibSourceUrl = function(chart, singleSource)
	{
		var isStr = chartFactory.isString(singleSource);
		var url = (isStr ? singleSource : singleSource.url);
		
		if(!url)
			return singleSource;
		
		//相对应用根路径
		if(url.indexOf("/") == 0)
		{
			url = chart.contextURL(url);
		}
		//绝对路径
		else if(chartFactory.HTTP_S_PREFIX_REGEX.test(url))
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
	chartFactory.rendererLib = function(renderer)
	{
		if(!renderer || renderer.depend === undefined)
		{
			return undefined;
		}
		
		if(renderer.depend == null)
		{
			return null;
		}
		else if($.isFunction(renderer.depend))
		{
			return renderer.depend();
		}
		else
		{
			return renderer.depend;
		}
	};
	
	//以http://或者https://开头的正则表达式
	chartFactory.HTTP_S_PREFIX_REGEX = /^(http:\/\/|https:\/\/)/i;
	
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
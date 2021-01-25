/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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
 *   datagear-chartSetting.js
 * 
 * 
 * 此图表工厂支持为<body>元素、图表元素添加"dg-chart-options"属性来设置图表选项，格式为：
 * { title: { show: false },... }
 * 
 * 此图表工厂支持为<body>元素、图表元素添加"dg-chart-theme"属性来设置图表主题，格式为：
 * { color:'...', backgroundColor:'...', ... }
 * 
 * 此图表工厂支持为<body>元素、图表元素添加"dg-chart-listener"属性来设置图表监听器，格式参考chartBase.listener函数参数说明。
 * 
 * 此图表工厂支持为图表元素添加"dg-chart-map"属性来设置地图图表的地图名。
 * 
 * 此图表工厂支持为<body>元素、图表元素添加"dg-echarts-theme"属性来设置图表Echarts主题名。
 * 
 * 此图表工厂支持为<body>元素、图表元素添加"dg-chart-disable-setting"属性，用于禁用图表交互设置功能，
 * 值为"true"表示禁用，其他表示启用。
 * 
 * 此图表工厂支持为图表元素添加"dg-chart-on-*"属性来设置图表事件处理函数，具体参考chartBase._initEventHandlers函数说明。
 * 
 * 此图表工厂支持为图表元素添加"dg-chart-renderer"属性来自定义、扩展图表渲染器，具体参考chartBase._initCustomChartRenderer函数说明。
 * 
 * 此图表工厂要求图表插件的图表渲染器（chartRenderer）格式为：
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
	var chartFactory = (global.chartFactory || (global.chartFactory = {}));
	var chartBase = (chartFactory.chartBase || (chartFactory.chartBase = {}));
	
	/**
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
	if(!chartFactory.mapURLs)
		chartFactory.mapURLs = {};
	
	/**
	 * 图表使用的渲染上下文属性名。
	 */
	chartFactory.renderContextAttrs =
	{
		//可选，图表主题，org.datagear.analysis.ChartTheme
		chartTheme: "chartTheme",
		//必须，Web上下文，org.datagear.analysis.support.html.HtmlTplDashboardRenderAttr.WebContext
		webContext: "webContext"
	};
	
	/**
	 * 初始化渲染上下文。
	 * 注意：此方法应在初始化任意图表前且body已加载后调用。
	 * 
	 * @param renderContext 渲染上下文
	 */
	chartFactory.initRenderContext = function(renderContext)
	{
		var webContext = chartFactory.renderContextAttr(renderContext, chartFactory.renderContextAttrs.webContext);
		
		if(webContext == null)
			throw new Error("The render context attribute ["+chartFactory.renderContextAttrs.webContext+"] must be set");
		
		chartFactory._initChartTheme(renderContext);
	};
	
	/**
	 * 初始化渲染上下文中的图表主题。
	 * 初始渲染上下文中允许不设置图表主题，或者仅设置部分属性，比如前景色、背景色，此方法则初始化其他必要的属性。
	 * 
	 * 注意：此方法应在初始化任意图表前且body已加载后调用，因为它也会从body的"dg-chart-theme"读取用户设置的图表主题。
	 * 
	 * @param renderContext 渲染上下文
	 */
	chartFactory._initChartTheme = function(renderContext)
	{
		var theme = chartFactory.renderContextAttrChartTheme(renderContext);
		
		if(!theme)
		{
			theme = {};
			chartFactory.renderContextAttr(renderContext, chartFactory.renderContextAttrs.chartTheme, theme);
		}
		
		if(!theme.name)
			theme.name = "chartTheme";
		if(!theme.color)
			theme.color = "#333";
		if(!theme.backgroundColor)
			theme.backgroundColor = "transparent";
		if(!theme.actualBackgroundColor)
			theme.actualBackgroundColor = "#FFF";
		if(!theme.gradient)
			theme.gradient = 10;
		if(!theme.graphColors || theme.graphColors.length == 0)
			theme.graphColors = ["#2EC7C9", "#B6A2DE", "#FFB980", "#97B552", "#D87A80", "#8D98B3", "#E5CF0D",
				"#5AB1EF", "#95706D", "#DC69AA"];
		if(!theme.graphRangeColors || theme.graphRangeColors.length == 0)
			theme.graphRangeColors = ["#58A52D", "#FFD700", "#FF4500"];
		
		chartFactory._initChartThemeActualBackgroundColorIf(theme);
		
		var bodyThemeValue = $(document.body).attr("dg-chart-theme");
		if(bodyThemeValue)
		{
			var bodyThemeObj = chartFactory.evalSilently(bodyThemeValue, {});
			chartFactory._initChartThemeActualBackgroundColorIf(bodyThemeObj);
			
			chartFactory._GLOBAL_RAW_CHART_THEME = $.extend(true, {}, theme, bodyThemeObj);
			
			chartFactory._inflateChartThemeIf(bodyThemeObj);
			
			//@deprecated 兼容1.5.0版本的自定义ChartTheme结构，未来版本会移除
			if(bodyThemeObj.colorSecond)
			{
				bodyThemeObj.color = bodyThemeObj.colorSecond;
				bodyThemeObj.titleColor = bodyThemeObj.color;
				bodyThemeObj.legendColor = bodyThemeObj.colorSecond;
			}
			//@deprecated 兼容1.5.0版本的自定义ChartTheme结构，未来版本会移除
			
			$.extend(true, theme, bodyThemeObj);
		}
		
		if(!chartFactory._GLOBAL_RAW_CHART_THEME)
			chartFactory._GLOBAL_RAW_CHART_THEME = $.extend(true, {}, theme);
		
		chartFactory._inflateChartThemeIf(theme);
	};
	
	/**
	 * 初始化图表主题的实际背景色。
	 */
	chartFactory._initChartThemeActualBackgroundColorIf = function(theme)
	{
		//如果设置了非透明backgroundColor，那么也应同时设置actualBackgroundColor
		if(theme.backgroundColor && theme.backgroundColor != "transparent")
		{
			theme.actualBackgroundColor = theme.backgroundColor;
			return true;
		}
		
		return false;
	};
	
	/**
	 * 如果图表主题已具备了生成其他配色的条件（color、actualBackgroundColor已设置），则尝试生成它们。
	 */
	chartFactory._inflateChartThemeIf = function(theme)
	{
		if(theme.color && theme.actualBackgroundColor)
		{
			if(!theme.legendColor)
				theme.legendColor = chartFactory.getGradualColor(theme, 0.8);
			
			if(!theme.borderColor)
				theme.borderColor = chartFactory.getGradualColor(theme, 0.3);
			
			if(!theme.tooltipTheme)
			{
				var tooltipTheme =
				{
					name: "tooltipTheme",
					color: theme.actualBackgroundColor,
					backgroundColor: chartFactory.getGradualColor(theme, 0.7),
					actualBackgroundColor: chartFactory.getGradualColor(theme, 0.7),
					borderColor: chartFactory.getGradualColor(theme, 0.9),
					borderWidth: 1,
					gradient: theme.gradient
				};
				
				theme.tooltipTheme = tooltipTheme;
			}
			
			if(!theme.highlightTheme)
			{
				var highlightTheme =
				{
					name: "highlightTheme",
					color: theme.actualBackgroundColor,
					backgroundColor: chartFactory.getGradualColor(theme, 0.8),
					actualBackgroundColor: chartFactory.getGradualColor(theme, 0.8),
					borderColor: chartFactory.getGradualColor(theme, 1),
					borderWidth: 1,
					gradient: theme.gradient
				};
				
				theme.highlightTheme = highlightTheme;
			}
		}
		
		if(theme.color)
		{
			if(!theme.titleColor)
				theme.titleColor = theme.color;
			
			if(!theme.legendColor)
				theme.legendColor = theme.color;
		}
		
		if(theme.borderWidth && !theme.borderStyle)
			theme.borderStyle = "solid";
	};
	
	/**
	 * 初始化指定图表对象。
	 * 初始化前应确保chart.renderContext中包含chartFactory.renderContextAttrs必须的属性值。
	 * 
	 * @param chart 图表对象
	 */
	chartFactory.init = function(chart)
	{
		$.extend(chart, this.chartBase);
		chart.init();
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
	 * 获取渲染上下文中的WebContext对象。
	 * 
	 * @param renderContext
	 */
	chartFactory.renderContextAttrWebContext = function(renderContext)
	{
		return chartFactory.renderContextAttr(renderContext, chartFactory.renderContextAttrs.webContext);
	};
	
	/**
	 * 获取渲染上下文中的ChartTheme对象。
	 * 
	 * @param renderContext
	 */
	chartFactory.renderContextAttrChartTheme = function(renderContext)
	{
		return chartFactory.renderContextAttr(renderContext, chartFactory.renderContextAttrs.chartTheme);
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
	 * 判断指定HTML元素是否是图表组件。
	 * 
	 * @param element DOM元素、Jquery对象
	 */
	chartFactory.isChartElement = function(element)
	{
		return $(element).hasClass(chartFactory.CHART_DISTINCT_CSS_NAME);
	};
	
	/**图表状态：准备render*/
	chartFactory.STATUS_PRE_RENDER = "PRE_RENDER";
	
	/**图表状态：正在render*/
	chartFactory.STATUS_RENDERING = "RENDERING";
	
	/**图表状态：完成render*/
	chartFactory.STATUS_RENDERED = "RENDERED";
	
	/**图表状态：准备update*/
	chartFactory.STATUS_PRE_UPDATE = "PRE_UPDATE";
	
	/**图表状态：正在update*/
	chartFactory.STATUS_UPDATING = "UPDATING";
	
	/**图表状态：完成update*/
	chartFactory.STATUS_UPDATED = "UPDATED";
	
	/**图表状态：已销毁*/
	chartFactory.STATUS_DESTROYED = "DESTROYED";
	
	/**用于标识图表元素的CSS名*/
	chartFactory.CHART_DISTINCT_CSS_NAME = "dg-chart-for-distinction";

	/**图表事件的图表类型：Echarts*/
	chartFactory.CHART_EVENT_CHART_TYPE_ECHARTS = "echarts";
	
	/**图表事件的图表类型：HTML*/
	chartFactory.CHART_EVENT_CHART_TYPE_HTML = "html";
	
	//----------------------------------------
	// chartBase start
	//----------------------------------------
	
	/**
	 * 初始化图表。
	 */
	chartBase.init = function()
	{
		if(this._inited == true)
			throw new Error("Chart has been initialized");
		this._inited = true;
		
		this._initOptions();
		this._initTheme();
		this._initListener();
		this._initMap();
		this._initEchartsThemeName();
		this._initDisableSetting();
		this._initEventHandlers();
		this._initCustomChartRenderer();
		
		//最后才设置为可渲染状态
		this.statusPreRender(true);
	};
	
	/**
	 * 初始化图表选项。
	 * 此方法依次从<body>元素、图表元素的"dg-chart-options"属性读取、合并图表选项。
	 */
	chartBase._initOptions = function()
	{
		var options = {};
		
		var $ele = this.elementJquery();
		
		var bodyOptions = $(document.body).attr("dg-chart-options");
		var eleOptions = $ele.attr("dg-chart-options");
		
		if(bodyOptions)
			options = $.extend(true, options, chartFactory.evalSilently(bodyOptions, {}));
		
		if(eleOptions)
			options = $.extend(true, options, chartFactory.evalSilently(eleOptions, {}));
		
		this.options(options);
	};
	
	/**
	 * 初始化图表主题。
	 * 此方法依次从图表renderContext.chartTheme、<body>元素、图表元素的"dg-chart-theme"属性读取、合并图表主题。
	 * 
	 * @return {...}
	 */
	chartBase._initTheme = function()
	{
		var globalRawTheme = chartFactory._GLOBAL_RAW_CHART_THEME;
		var globalTheme = this.renderContextAttr(chartFactory.renderContextAttrs.chartTheme);
		
		if(!globalTheme || !globalRawTheme)
			throw new Error("chartFactory.initRenderContext() must be called first");
		
		var eleThemeValue = this.elementJquery().attr("dg-chart-theme");
		
		if(eleThemeValue)
		{
			var eleThemeObj = chartFactory.evalSilently(eleThemeValue, {});
			chartFactory._initChartThemeActualBackgroundColorIf(eleThemeObj);
			chartFactory._inflateChartThemeIf(eleThemeObj);
			
			var eleTheme = $.extend(true, {}, globalRawTheme, eleThemeObj);
			chartFactory._inflateChartThemeIf(eleTheme);
			
			this.theme(eleTheme);
		}
		else
			this.theme(globalTheme);
	};
	
	/**
	 * 初始化图表监听器。
	 * 此方法依次从图表元素、<body>元素的"dg-chart-listener"属性获取监听器对象。
	 */
	chartBase._initListener = function()
	{
		var $chart = this.elementJquery();
		
		var listenerStr = $chart.attr("dg-chart-listener");
		if(!listenerStr)
			listenerStr = $(document.body).attr("dg-chart-listener");
		
		if(listenerStr)
		{
			var listener = chartFactory.evalSilently(listenerStr);
			
			if(listener)
				this.listener(listener);
		}
	};
	
	/**
	 * 初始化图表的地图名。
	 * 此方法从图表元素的"dg-chart-map"属性获取图表地图名。
	 */
	chartBase._initMap = function()
	{
		var map = this.elementJquery().attr("dg-chart-map");
		
		if(map)
			this.map(map);
	};
	
	/**
	 * 初始化图表的echarts主题名。
	 * 此方法依次从图表元素、<body>元素的"dg-echarts-theme"属性获取echarts主题名。
	 */
	chartBase._initEchartsThemeName = function()
	{
		var themeName = this.elementJquery().attr("dg-echarts-theme");
		
		if(!themeName)
			themeName = $(document.body).attr("dg-echarts-theme");
		
		this.echartsThemeName(themeName);
	};
	
	/**
	 * 初始化图表是否禁用交互设置。
	 * 此方法从图表元素的"dg-chart-disable-setting"属性获取是否禁用值。
	 */
	chartBase._initDisableSetting = function()
	{
		var globalSetting = $(document.body).attr("dg-chart-disable-setting");
		var localSetting = this.elementJquery().attr("dg-chart-disable-setting");
		
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
	 * 此方法从图表元素的所有以"dg-chart-on-"开头的属性获取事件处理函数。
	 * 例如：
	 * dg-chart-on-click="clickHandler" 						定义"click"事件处理函数；
	 * dg-chart-on-mouseover="function(chartEvent){ ... }"		定义"mouseover"事件处理函数。
	 */
	chartBase._initEventHandlers = function()
	{
		var dom = this.element();
		var attrs = dom.attributes;
		
		var ehs = [];
		
		var prefix = "dg-chart-on-";
		
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
	 * 此方法从图表元素的"dg-chart-renderer"属性获取自定义图表渲染器。
	 */
	chartBase._initCustomChartRenderer = function()
	{
		var chartRenderer = this.elementJquery().attr("dg-chart-renderer");
		
		if(chartRenderer)
		{
			chartRenderer = chartFactory.evalSilently(chartRenderer);
			
			if(chartRenderer)
				this.customChartRenderer(chartRenderer);
		}
	};
	
	/**
	 * 获取/设置初始图表选项。
	 * 图表选项格式为：{ ... }
	 * 
	 * 图表渲染器实现相关：
	 * 图表渲染器应使用此函数获取并应用图表选项，以支持“dg-chart-options”特性。
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
	 * 获取/设置图表更新时的图表选项。
	 * 图表选项格式为： { ... }
	 * 
	 * 当希望根据图表更新数据动态自定义图表选项时，可以在图表监听器的onUpdate函数中调用此函数设置更新图表选项。
	 * 
	 * 图表渲染器实现相关：
	 * 图表渲染器应使用此函数获取并应用更新图表选项（在其update函数中），以支持此特性。
	 * 
	 * @param options 可选，要设置的图表选项，没有则执行获取操作
	 */
	chartBase.optionsUpdate = function(options)
	{
		if(options === undefined)
			return this.extValue("_optionsUpdate");
		else
			this.extValue("_optionsUpdate", options);
	};
	
	/**
	 * 获取/设置初始图表主题。
	 * 图表主题格式参考：org.datagear.analysis.ChartTheme。
	 * 
	 * 图表渲染器实现相关：
	 * 图表渲染器应使用此函数获取并应用图表主题，以支持“dg-chart-theme”特性。
	 * 
	 * @param theme 可选，要设置的图表主题，没有则执行获取操作
	 */
	chartBase.theme = function(theme)
	{
		if(theme === undefined)
			return this._theme;
		else
			this._theme = theme;
	};
	
	/**
	 * 获取/设置初始图表监听器。
	 * 图表监听器格式为：
	 * {
	 *   //渲染图表完成回调函数
	 *   render: function(chart){ ... },
	 *   //更新图表数据完成回调函数
	 *   update: function(chart, results){ ... },
	 *   //可选，渲染图表前置回调函数，返回false将阻止渲染图表
	 *   onRender: function(chart){ ... },
	 *   //可选，更新图表数据前置回调函数，返回false将阻止更新图表数据
	 *   onUpdate: function(chart, results){ ... },
	 * }
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
	 * 获取/设置初始图表地图名。
	 * 此方法用于为地图类图表提供支持，如果不是地图类图表，则不必设置此项。
	 * 
	 * 图表渲染器实现相关：
	 * 图表渲染器应使用此函数获取并应用图表地图，以支持“dg-chart-map”特性。
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
	 * 获取/设置初始图表的echarts主题名。
	 * 此方法用于为echarts图表提供支持，如果不是echarts图表，则不必设置此项。
	 * 
	 * 图表渲染器实现相关：
	 * 图表渲染器应使用此函数获取并应用echarts主题，以支持“dg-echarts-theme”特性。
	 * 
	 * @param themeName 可选，要设置的且已注册的echarts主题名，没有则执行获取操作
	 */
	chartBase.echartsThemeName = function(themeName)
	{
		if(themeName === undefined)
			return this._echartsThemeName;
		else
			this._echartsThemeName = themeName;
	};
	
	/**
	 * 获取/设置初始图表是否禁用设置。
	 * 
	 * @param setting 可选，禁用设置，没有则执行获取操作且返回格式为：{param: true||false, data: true||false}。
	 * 					禁用设置格式为：
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
	 * 获取/设置初始图表事件处理函数数组。
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
	 * @param customChartRenderer 可选，要设置的自定义图表渲染器，自定义图表渲染器允许仅定义要重写的内置图表插件渲染器函数
	 */
	chartBase.customChartRenderer = function(customChartRenderer)
	{
		if(customChartRenderer === undefined)
			return this._customChartRenderer;
		else
			this._customChartRenderer = customChartRenderer;
	};
	
	/**
	 * 渲染图表。
	 * 注意：只有this.statusPreRender()或者statusDestroyed()为true，此方法才会执行。
	 * 注意：
	 * 从render()开始产生的新扩展图表属性值都应该使用extValue()函数设置/获取，
	 * 因为图表会在destroy()中清除extValue()设置的所有值，之后允许重新render()。
	 */
	chartBase.render = function()
	{
		if(!this.statusPreRender() && !this.statusDestroyed())
			return false;
		
		var $chart = this.elementJquery();
		$chart.addClass(chartFactory.CHART_DISTINCT_CSS_NAME);
		chartFactory.setThemeStyle($chart, this.theme());
		
		this.statusRendering(true);
		
		var doRender = true;
		
		var listener = this.listener();
		if(listener && listener.onRender)
		  doRender = listener.onRender(this);
		
		if(doRender != false)
		{
			var async = this.isAsyncRender();
			
			this.doRender();
			
			if(!async)
				this.statusRendered(true);
		}
		
		return true;
	};
	
	/**
	 * 调用底层图表渲染器的render函数，执行渲染。
	 */
	chartBase.doRender = function()
	{
		if(this._customChartRenderer && this._customChartRenderer.render)
		{
			this._customChartRenderer.render(this);
		}
		else
		{
			this.plugin.chartRenderer.render(this);
		}
	};
	
	/**
	 * 更新图表。
	 * 注意：此函数在图表渲染完成后才可调用。
	 * 注意：只有this.statusRendered()或者this.statusPreUpdate()或者this.statusUpdated()为true，此方法才会执行。
	 * 
	 * @param results 图表数据集结果
	 */
	chartBase.update = function(results)
	{
		if(!this.statusRendered() && !this.statusPreUpdate() && !this.statusUpdated())
			return false;
		
		this.extValue("_updateResults", results);
		
		this.statusUpdating(true);
		
		var doUpdate = true;
		
		var listener = this.listener();
		if(listener && listener.onUpdate)
			doUpdate = listener.onUpdate(this, results);
		
		if(doUpdate != false)
		{
			var async = this.isAsyncUpdate(results);
			
			this.doUpdate(results);
			
			if(!async)
				this.statusUpdated(true);
		}
		
		return true;
	};
	
	/**
	 * 调用图表插件的update函数，执行更新数据。
	 */
	chartBase.doUpdate = function(results)
	{
		if(this._customChartRenderer && this._customChartRenderer.update)
		{
			this._customChartRenderer.update(this, results);
		}
		else
		{
			this.plugin.chartRenderer.update(this, results);
		}
	};
	
	/**
	 * 获取用于此次更新图表的结果数据，没有则返回null。
	 */
	chartBase.getUpdateResults = function()
	{
		return this.extValue("_updateResults");
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
		
		if(this._customChartRenderer && this._customChartRenderer.resize)
		{
			this._customChartRenderer.resize(this);
		}
		else if(this.plugin.chartRenderer.resize)
		{
			this.plugin.chartRenderer.resize(this);
		}
		else
		{
			var echartsInstance = this.echartsInstance();
			if(echartsInstance)
				echartsInstance.resize();
		}
	};
	
	/**
	 * 销毁图表，释放图表占用的资源、恢复图表HTML元素初值。
	 * 
	 * 图表渲染器实现相关：
	 * 图表渲染器应实现destroy函数，以支持此特性。
	 */
	chartBase.destroy = function()
	{
		this._assertActive();
		
		this.statusDestroyed(true);
		this.elementJquery().removeClass(chartFactory.CHART_DISTINCT_CSS_NAME);
		
		if(this._customChartRenderer && this._customChartRenderer.destroy)
		{
			this._customChartRenderer.destroy(this);
		}
		else if(this.plugin.chartRenderer.destroy)
		{
			this.plugin.chartRenderer.destroy(this);
		}
		else
		{
			var echartsInstance = this.echartsInstance();
			if(echartsInstance && !echartsInstance.isDisposed())
			{
				echartsInstance.dispose();
				this.echartsInstance(null);
			}
			
			this.elementJquery().empty();
		}
		
		this._destroySetting();
		
		//最后清空扩展属性值，因为上面逻辑可能会使用到
		this._extValues = {};
	};
	
	/**
	 * 销毁图表交互设置。
	 */
	chartBase._destroySetting = function()
	{
		chartFactory.chartSetting.unbindChartSettingPanelEvent(this);
	};
	
	/**
	 * 图表的render方法是否是异步的。
	 */
	chartBase.isAsyncRender = function()
	{
		if(this._customChartRenderer && this._customChartRenderer.asyncRender !== undefined)
		{
			if(typeof(this._customChartRenderer.asyncRender) == "function")
				return this._customChartRenderer.asyncRender(this);
			
			return (this._customChartRenderer.asyncRender == true);
		}
		
		if(this.plugin.chartRenderer.asyncRender == undefined)
			return false;
		
		if(typeof(this.plugin.chartRenderer.asyncRender) == "function")
			return this.plugin.chartRenderer.asyncRender(this);
		
		return (this.plugin.chartRenderer.asyncRender == true);
	};
	
	/**
	 * 图表的update方法是否是异步的。
	 * 
	 * @param results 图表数据集结果
	 */
	chartBase.isAsyncUpdate = function(results)
	{
		if(this._customChartRenderer && this._customChartRenderer.asyncUpdate !== undefined)
		{
			if(typeof(this._customChartRenderer.asyncUpdate) == "function")
				return this._customChartRenderer.asyncUpdate(this, results);
			
			return (this._customChartRenderer.asyncUpdate == true);
		}
		
		if(this.plugin.chartRenderer.asyncUpdate == undefined)
			return false;
		
		if(typeof(this.plugin.chartRenderer.asyncUpdate) == "function")
			return this.plugin.chartRenderer.asyncUpdate(this, results);
		
		return (this.plugin.chartRenderer.asyncUpdate == true);
	};
	
	/**
	 * 图表是否是活跃的，即已完成渲染且未被销毁。
	 */
	chartBase.isActive = function()
	{
		return (this._isActive == true);
	};
	
	/**
	 * 断言图表是活跃的。
	 */
	chartBase._assertActive = function()
	{
		if(this.isActive())
			return;
		
		throw new Error("Chart is not active");
	};
	
	/**
	 * 图表是否为/设置为：准备render。
	 * 
	 * @param set 可选，为true时设置状态；否则，判断状态
	 */
	chartBase.statusPreRender = function(set)
	{
		if(set === true)
			this.status(chartFactory.STATUS_PRE_RENDER);
		else
			return (this.status() == chartFactory.STATUS_PRE_RENDER);
	};
	
	/**
	 * 图表是否为/设置为：正在render。
	 * 
	 * @param set 可选，为true时设置状态；否则，判断状态
	 */
	chartBase.statusRendering = function(set)
	{
		if(set === true)
			this.status(chartFactory.STATUS_RENDERING);
		else
			return (this.status() == chartFactory.STATUS_RENDERING);
	};
	
	/**
	 * 图表是否为/设置为：完成render。
	 * 
	 * @param set 可选，为true时设置状态；否则，判断状态
	 * @param postProcess 可选，当set为true时，是否执行渲染后置操作，比如渲染交互设置表单、绑定初始事件、调用监听器、，默认为true
	 */
	chartBase.statusRendered = function(set, postProcess)
	{
		if(set === true)
		{
			this._isActive = true;
			this.status(chartFactory.STATUS_RENDERED);
			
			if(postProcess != false)
				this._postProcessRendered();
		}
		else
			return (this.status() == chartFactory.STATUS_RENDERED);
	};
	
	/**
	 * 执行渲染完成后置处理。
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
			this.status(chartFactory.STATUS_PRE_UPDATE);
		else
			return (this.status() == chartFactory.STATUS_PRE_UPDATE);
	};
	
	/**
	 * 图表是否为/设置为：正在update。
	 * 
	 * @param set 可选，为true时设置状态；否则，判断状态
	 */
	chartBase.statusUpdating = function(set)
	{
		if(set === true)
			this.status(chartFactory.STATUS_UPDATING);
		else
			return (this.status() == chartFactory.STATUS_UPDATING);
	};
	
	/**
	 * 图表是否为/设置为：完成update。
	 * 
	 * @param set 可选，为true时设置状态；否则，判断状态
	 * @param postProcess 可选，当set为true时，是否执行更新后置操作，比如调用监听器、，默认为true
	 */
	chartBase.statusUpdated = function(set, postProcess)
	{
		if(set === true)
		{
			this.status(chartFactory.STATUS_UPDATED);
			
			if(postProcess != false)
				this._postProcessUpdated();
		}
		else
			return (this.status() == chartFactory.STATUS_UPDATED);
	};
	
	/**
	 * 执行更新完成后置处理。
	 */
	chartBase._postProcessUpdated = function()
	{
		var listener = this.listener();
		if(listener && listener.update)
		  listener.update(this, this.getUpdateResults());
	};
	
	/**
	 * 图表是否为/设置为：已销毁。
	 * 
	 * @param set 可选，为true时设置状态；否则，判断状态
	 */
	chartBase.statusDestroyed = function(set)
	{
		if(set === true)
		{
			this._isActive = false;
			this.status(chartFactory.STATUS_DESTROYED);
		}
		else
			return (this.status() == chartFactory.STATUS_DESTROYED);
	};
	
	/**
	 * 获取/设置图表状态。
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
		
		if(this._customChartRenderer && this._customChartRenderer.on)
		{
			this._customChartRenderer.on(this, eventType, handler);
		}
		else if(this.plugin.chartRenderer.on)
		{
			this.plugin.chartRenderer.on(this, eventType, handler);
		}
		else
			throw new Error("Chart plugin ["+this.plugin.id+"] 's [chartRenderer.on] undefined");
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
		this._assertActive();
		
		if(this._customChartRenderer && this._customChartRenderer.off)
		{
			this._customChartRenderer.off(this, eventType, handler);
		}
		else if(this.plugin.chartRenderer.off)
		{
			this.plugin.chartRenderer.off(this, eventType, handler);
		}
		else
			throw new Error("Chart plugin ["+this.plugin.id+"] 's [chartRenderer.off] undefined");
	};
	
	/**
	 * 判断此图表是否有参数化数据集。
	 */
	chartBase.hasParamDataSet = function()
	{
		var re = false;
		
		var chartDataSets = this.chartDataSetsNonNull();
		for(var i=0; i<chartDataSets.length; i++)
		{
			var params = chartDataSets[i].dataSet.params;
			re = (params && params.length > 0);
			
			if(re)
				break;
		}
		
		return re;
	};
	
	/**
	 * 图表的所有/指定数据集参数值是否齐备。
	 * 
	 * @param chartDataSet 指定图表数据集或其索引，如果不设置，则取所有
	 */
	chartBase.isDataSetParamValueReady = function(chartDataSet)
	{
		chartDataSet = (typeof(chartDataSet) == "number" ? this.chartDataSets[chartDataSet] : chartDataSet);
		
		var chartDataSets = (chartDataSet ? [ chartDataSet ] : this.chartDataSetsNonNull());
		
		for(var i=0; i<chartDataSets.length; i++)
		{
			var dataSet = chartDataSets[i].dataSet;
			
			if(!dataSet.params || dataSet.params.length == 0)
				continue;
			
			var paramValues = (chartDataSets[i].paramValues || {});
			
			for(var j=0; j<dataSet.params.length; j++)
			{
				var dsp = dataSet.params[j];
				
				if((dsp.required+"") == "true" && paramValues[dsp.name] == null)
					return false;
			}
		}
		
		return true;
	};
	
	/**
	 * 获取/设置指定数据集单个参数值。
	 * 
	 * @param chartDataSet 指定图表数据集对象、图表数据集索引
	 * @param name 参数名、参数索引
	 * @param value 要设置的参数值，不设置则执行获取操作
	 */
	chartBase.dataSetParamValue = function(chartDataSet, name, value)
	{
		chartDataSet = (typeof(chartDataSet) == "number" ? this.chartDataSets[chartDataSet] : chartDataSet);
		
		//参数索引
		if(typeof(name) == "number")
		{
			var dataSet = chartDataSet.dataSet;
			
			if(!dataSet.params || dataSet.params.length <= name)
				throw new Error("No chart data set param defined for index: "+name);
			
			name = dataSet.params[name].name;
		}
		
		if(chartDataSet.paramValues == null)
			chartDataSet.paramValues = {};
		
		if(chartDataSet._originalParamValues == null)
			chartDataSet._originalParamValues = $.extend({}, chartDataSet.paramValues);
		
		if(value === undefined)
			return chartDataSet.paramValues[name];
		else
			chartDataSet.paramValues[name] = value;
	};
	
	/**
	 * 获取/设置指定数据集参数值集。
	 * 
	 * @param chartDataSet 指定图表数据集或其索引
	 * @param paramValuesObj 要设置的参数值集对象，不设置则执行获取操作
	 */
	chartBase.dataSetParamValues = function(chartDataSet, paramValuesObj)
	{
		chartDataSet = (typeof(chartDataSet) == "number" ? this.chartDataSets[chartDataSet] : chartDataSet);
		
		if(chartDataSet.paramValues == null)
			chartDataSet.paramValues = {};
		
		if(chartDataSet._originalParamValues == null)
			chartDataSet._originalParamValues = $.extend({}, chartDataSet.paramValues);
		
		if(paramValuesObj === undefined)
			return chartDataSet.paramValues;
		else
			chartDataSet.paramValues = paramValuesObj;
	};
	
	/**
	 * 已弃用，请使用dataSetParamValues函数。
	 */
	chartBase.getDataSetParamValues = function(chartDataSet)
	{
		return this.dataSetParamValues(chartDataSet);
	};
	
	/**
	 * 已弃用，请使用dataSetParamValues函数。
	 */
	chartBase.setDataSetParamValues = function(chartDataSet, paramValuesObj)
	{
		this.dataSetParamValues(chartDataSet, paramValuesObj);
	};
	
	/**
	 * 重置指定数据集参数值集。
	 * 
	 * @param chartDataSet 指定图表数据集或其索引
	 */
	chartBase.resetDataSetParamValues = function(chartDataSet)
	{
		chartDataSet = (typeof(chartDataSet) == "number" ? this.chartDataSets[chartDataSet] : chartDataSet);
		
		if(chartDataSet._originalParamValues == null)
			return;
		
		chartDataSet.paramValues = $.extend({}, chartDataSet._originalParamValues);
	};
	
	/**
	 * 获取图表的DOM元素。
	 */
	chartBase.element = function()
	{
		return document.getElementById(this.elementId);
	};
	
	/**
	 * 获取图表的DOM元素的Jquery对象。
	 */
	chartBase.elementJquery = function()
	{
		return $("#" + this.elementId);
	};
	
	/**
	 * 获取图表名称。
	 */
	chartBase.nameNonNull = function()
	{
		return (this.name || "");
	};
	
	/**
	 * 获取图表的更新间隔。
	 */
	chartBase.updateIntervalNonNull = function()
	{
		if(this.updateInterval != null)
			return this.updateInterval;
		
		return -1;
	},
	
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
	 * 
	 * @param name 扩展名
	 * @param value 要设置的扩展值，可选，不设置则执行获取操作
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
	
	/**
	 * 获取图表数据集对象数组。
	 */
	chartBase.chartDataSetsNonNull = function()
	{
		return (this.chartDataSets || []);
	};
	
	/**
	 * 获取第一个图表数据集对象。
	 * 
	 * @param chart
	 * @return {...} 或  undefined
	 */
	chartBase.chartDataSetFirst = function()
	{
		return this.chartDataSetAt(0);
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
	 * 获取指定图表数据集对象名称，它不会返回null。
	 * 
	 * @param chartDataSet 图表数据集对象
	 */
	chartBase.chartDataSetName = function(chartDataSet)
	{
		if(!chartDataSet)
			return "";
		
		if(chartDataSet.alias)
			return chartDataSet.alias;
		
		var dataSet = (chartDataSet.dataSet || chartDataSet);
		
		return (dataSet ? (dataSet.name || "") : "");
	};
	
	/**
	 * 获取指定标记的第一个数据集属性，没有则返回undefined。
	 * 
	 * @param chartDataSet 图表数据集对象
	 * @param dataSign 数据标记对象、标记名称
	 * @return {...}、undefined
	 */
	chartBase.dataSetPropertyOfSign = function(chartDataSet, dataSign)
	{
		var properties = this.dataSetPropertiesOfSign(chartDataSet, dataSign);
		
		return (properties.length > 0 ? properties[0] : undefined);
	};
	
	/**
	 * 获取指定标记的数据集属性数组。
	 * 
	 * @param chartDataSet 图表数据集对象
	 * @param dataSign 数据标记对象、标记名称
	 * @return [...]
	 */
	chartBase.dataSetPropertiesOfSign = function(chartDataSet, dataSign)
	{
		var re = [];
		
		if(!chartDataSet || !chartDataSet.dataSet || !dataSign)
			return re;
		
		dataSign = (dataSign.name || dataSign);
		var dataSetProperties = (chartDataSet.dataSet.properties || []);
		var propertySigns = (chartDataSet.propertySigns || {});
		
		var signPropertyNames = [];
		
		for(var pname in propertySigns)
		{
			var mySigns = (propertySigns[pname] || []);
			
			for(var i=0; i<mySigns.length; i++)
			{
				if(mySigns[i] == dataSign || mySigns[i].name == dataSign)
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
		
		return re;
	};
	
	/**
	 * 获取数据集属性标签，它不会返回null。
	 * 
	 * @param dataSetProperty
	 * @return "..."
	 */
	chartBase.dataSetPropertyLabel = function(dataSetProperty)
	{
		if(!dataSetProperty)
			return "";
		
		var label = (dataSetProperty.label ||  dataSetProperty.name);
		
		return (label || "");
	};

	/**
	 * 返回第一个数据集结果，没有则返回undefined。
	 * 
	 * @param results
	 */
	chartBase.resultFirst = function(results)
	{
		return this.resultAt(results, 0);
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
	 * 获取数据集结果的数据对象。
	 * 
	 * @param result 数据集结果对象
	 * @return
	 */
	chartBase.resultData = function(result)
	{
		return (result ? result.data : undefined);
	};
	
	/**
	 * 获取数据集结果的数据对象数组。
	 * 如果result.data是数组，则直接返回；否则，返回：[ result.data ]。
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
	 * 获取数据集结果的行对象指定属性值。
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
	 * 将数据集结果的行对象按照指定properties顺序转换为行值数组。
	 * 
	 * @param result 数据集结果对象
	 * @param properties 数据集属性对象数组、属性名数组、属性对象、属性名
	 * @param row 行索引，可选，默认为0
	 * @param count 获取的最多行数，可选，默认为全部
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
				var row = [];
				
				for(var j=0; j<properties.length; j++)
				{
					var p = properties[j];
					
					var name = (p ? (p.name || p) : undefined);
					if(!name)
						continue;
					
					row[j] = rowObj[name];
				}
				
				re.push(row);
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
	 * 将数据集结果的行对象按照指定properties顺序转换为列值数组。
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
	 * 获取数据集结果的名称/值对象数组。
	 * 
	 * @param result 数据集结果对象、对象数组
	 * @param nameProperty 名称属性对象、属性名
	 * @param valueProperty 值属性对象、属性名、数组
	 * @param row 行索引，以0开始，可选，默认为0
	 * @param count 获取结果数据的最多行数，可选，默认为全部
	 * @return [{name: ..., value: ...}, ...]
	 */
	chartBase.resultNameValueObjects = function(result, nameProperty, valueProperty, row, count)
	{
		var re = [];
		
		if(!result || !nameProperty || !valueProperty)
			return re;
		
		var datas = this.resultDatas(result);
		
		row = (row || 0);
		var getCount = datas.length;
		if(count != null && count < getCount)
			getCount = count;
		
		nameProperty = (nameProperty.name || nameProperty);
		
		if($.isArray(valueProperty))
		{
			for(var i=row; i< getCount; i++)
			{
				var name = datas[i][nameProperty];
				var value = [];
				
				for(var j=0; j<valueProperty.length; j++)
				{
					var vn = (valueProperty[j].name || valueProperty[j]);
					value[j] = datas[i][vn];
				}
				
				re.push({ "name" : name, "value" : value });
			}
		}
		else
		{
			valueProperty = (valueProperty.name || valueProperty);
			
			for(var i=row; i< getCount; i++)
			{
				var obj =
				{
					"name" : datas[i][nameProperty],
					"value" : datas[i][valueProperty]
				};
				
				re.push(obj);
			}
		}
		
		return re;
	};
	
	/**
	 * 获取数据集结果指定属性、指定行的单元格值，没有则返回undefined。
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
	 * 此方法先从chartFactory.mapURLs查找对应的地址，如果没有，则直接返回name作为地址。
	 * 
	 * @param name 地图名称
	 */
	chartBase.mapURL = function(name)
	{
		var url = chartFactory.mapURLs[name];
		
		if(!url && typeof(chartFactory.mapURLs.mapURL) == "function")
			url = chartFactory.mapURLs.mapURL(name);
		
		url = (url || name);
		
		var webContext = chartFactory.renderContextAttrWebContext(this.renderContext);
		url = chartFactory.toWebContextPathURL(webContext, url);
		
		return url;
	};
	
	/**
	 * Echarts图表支持函数：初始化图表的Echarts对象。
	 * 此方法会自动应用“dg-chart-theme”、“dg-echarts-theme”。
	 * 
	 * @param options echarts设置项
	 * @return echarts实例对象
	 */
	chartBase.echartsInit = function(options)
	{
		var instance = echarts.init(this.element(), this._echartsGetRegisteredThemeName());
		instance.setOption(options);
		
		this.echartsInstance(instance);
		
		return instance;
	};
	
	/**
	 * Echarts图表支持函数：获取/设置图表的Echarts实例对象。
	 * 
	 * @param instance 可选，要设置的echarts实例，不设置则执行获取操作
	 */
	chartBase.echartsInstance = function(instance)
	{
		if(instance === undefined)
			return this.extValue("_echartsInstance");
		else
			this.extValue("_echartsInstance", instance);
	};
	
	/**
	 * Echarts图表支持函数：设置图表的Echarts实例的选项。
	 * 
	 * @param options
	 */
	chartBase.echartsOptions = function(options)
	{
		var instance = this.echartsInstance();
		instance.setOption(options);
	};
	
	/**
	 * Echarts图表支持函数：获取用于此图表的且已注册的Echarts主题名。
	 */
	chartBase._echartsGetRegisteredThemeName = function()
	{
		var themeName = this.echartsThemeName();
		
		//从ChartTheme构建echarts主题
		if(!themeName)
		{
			var chartTheme = this.theme();
			
			if(!chartTheme._registeredEchartsThemeName)
			{
				var seq = (chartFactory._registeredEchartsThemeNameSeqNext != null ?
						chartFactory._registeredEchartsThemeNameSeqNext : 0);
				chartFactory._registeredEchartsThemeNameSeqNext = seq + 1;
				
				chartTheme._registeredEchartsThemeName = "themeNameByChartTheme-" + seq;
				
				var echartsTheme = chartFactory.buildEchartsTheme(chartTheme);
				echarts.registerTheme(chartTheme._registeredEchartsThemeName, echartsTheme);
			}
			
			themeName = chartTheme._registeredEchartsThemeName;
		}
		
	    return themeName;
	};
	
	/**
	 * Echarts图表支持函数：判断指定名称的echarts地图是否已经注册过而无需再加载。
	 * 
	 * @param name echarts地图名称
	 */
	chartBase.echartsMapRegistered = function(name)
	{
		return (echarts.getMap(name) != null);
	};
	
	/**
	 * Echarts图表支持函数：加载指定名称的echarts地图JSON，并在完成后执行回调函数。
	 * 注意：如果地图图表插件的render/update函数中调用此函数，应该首先设置插件的asyncRender/asyncUpdate，
	 * 并在callback中调用chart.statusRendered(true)/chart.statusUpdated(true)，具体参考此文件顶部的注释。
	 * 
	 * @param name echarts地图名称
	 * @param callback 完成回调函数：function(name){ ... }
	 */
	chartBase.echartsLoadMap = function(name, callback)
	{
		var url = this.mapURL(name);
		
		var thisChart = this;
		
		$.getJSON(url, function(geoJson)
		{
			echarts.registerMap(name, geoJson);
			
			if(callback)
				callback.call(thisChart, name);
		});
	};
	
	/**
	 * 图表事件支持函数：创建Echarts图表的事件对象。
	 * 
	 * @param eventType 事件类型
	 * @param echartsEventParams echarts事件处理函数的参数对象
	 */
	chartBase.eventNewEcharts = function(eventType, echartsEventParams)
	{
		var thisChart = this;
		var event =
		{
			"type": eventType,
			"chart": thisChart,
			"chartType": chartFactory.CHART_EVENT_CHART_TYPE_ECHARTS,
			"originalEvent": echartsEventParams
		};
		
		return event;
	};
	
	/**
	 * 图表事件支持函数：创建HTML图表的事件对象。
	 * 
	 * @param eventType 事件类型
	 * @param htmlEvent HTML事件对象
	 */
	chartBase.eventNewHtml = function(eventType, htmlEvent)
	{
		var thisChart = this;
		var event =
		{
			"type": eventType,
			"chart": thisChart,
			"chartType": chartFactory.CHART_EVENT_CHART_TYPE_HTML,
			"originalEvent": htmlEvent
		};
		
		return event;
	};
	
	/**
	 * 图表事件支持函数：获取/设置图表事件对象的数据（chartEvent.data）。
	 * 
	 * @param chartEvent 图表事件对象
	 * @param data 可选，要设置的数据对象，绘制图表条目的数据对象：{ 数据标记名 : 数据值, ... }
	 */
	chartBase.eventData = function(chartEvent, data)
	{
		if(data === undefined)
			return chartEvent["data"];
		else
			chartEvent["data"] = data;
	};
	
	/**
	 * 图表事件支持函数：获取/设置图表事件对象的原始数据（chartEvent.originalData）。
	 * 
	 * @param chartEvent 图表事件对象
	 * @param originalData 可选，要设置的原始数据，绘制图表条目的原始数据集结果数据，可以是单个数据对象、数据对象数组
	 */
	chartBase.eventOriginalData = function(chartEvent, originalData)
	{
		if(originalData === undefined)
			return chartEvent["originalData"];
		else
			chartEvent["originalData"] = originalData;
	};
	
	/**
	 * 图表事件支持函数：获取/设置图表事件对象的原始数据对应的图表数据集索引（chartEvent.originalChartDataSetIndex）。
	 * 
	 * @param chartEvent 图表事件对象
	 * @param originalChartDataSetIndex 可选，要设置的图表数据集索引数值
	 */
	chartBase.eventOriginalChartDataSetIndex = function(chartEvent, originalChartDataSetIndex)
	{
		if(originalChartDataSetIndex === undefined)
			return chartEvent["originalChartDataSetIndex"];
		else
			chartEvent["originalChartDataSetIndex"] = originalChartDataSetIndex;
	};
	
	/**
	 * 图表事件支持函数：获取/设置图表事件对象的原始数据在数据集结果数据中的索引（（chartEvent.originalResultDataIndex））。
	 * 
	 * @param chartEvent 图表事件对象
	 * @param originalResultDataIndex 可选，要设置的结果数据索引，当chartEvent.originalData为单个对象时，应是单个索引值；
	 * 									当chartEvent.originalData为对象数组时，应是与之对应的索引值数组
	 */
	chartBase.eventOriginalResultDataIndex = function(chartEvent, originalResultDataIndex)
	{
		if(originalResultDataIndex === undefined)
			return chartEvent["originalResultDataIndex"];
		else
			chartEvent["originalResultDataIndex"] = originalResultDataIndex;
	};
	
	/**
	 * 图表事件支持函数：设置图表事件对象的原始图表数据集索引、原始数据、原始数据索引。
	 * 
	 * @param chartEvent 图表事件对象
	 * @param originalChartDataSetIndex 原始图表数据集索引
	 * @param originalResultDataIndex 原始数据索引，格式允许：数值、数值数组
	 */
	chartBase.eventOriginalInfo = function(chartEvent, originalChartDataSetIndex, originalResultDataIndex)
	{
		var result = this.resultAt(this.getUpdateResults(), originalChartDataSetIndex);
		var resultDatas = (result == null ? [] : this.resultDatas(result));
		
		var originalData = undefined;
		
		var rdi = originalResultDataIndex;
		
		//索引数值
		if(typeof(rdi) == "number")
		{
			originalData = resultDatas[rdi];
		}
		//索引数值数组
		else if($.isArray(rdi))
		{
			originalData = [];
			
			for(var i=0; i<rdi.length; i++)
				originalData.push(resultDatas[rdi[i]]);
		}
		
		this.eventOriginalData(chartEvent, originalData);
		this.eventOriginalChartDataSetIndex(chartEvent, originalChartDataSetIndex);
		this.eventOriginalResultDataIndex(chartEvent, rdi);
	};
	
	/**
	 * 图表事件支持函数：绑定图表事件处理函数代理。
	 * 注意：此函数在图表渲染完成后才可调用。
	 * 
	 * 图表事件处理通常由内部组件的事件处理函数代理（比如Echarts），并在代理函数中调用图表事件处理函数。
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
		
		var delegations = this.extValue("eventHandlerDelegations");
		if(delegations == null)
		{
			delegations = [];
			this.extValue("eventHandlerDelegations", delegations);
		}
		
		delegationBinder.bind(this, eventType, eventHandlerDelegation);
		
		delegations.push({ eventType: eventType , eventHanlder: eventHanlder, eventHandlerDelegation: eventHandlerDelegation });
	};
	
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
		this._assertActive();
		
		if(delegationUnbinder == undefined)
		{
			delegationUnbinder = eventHanlder;
			eventHanlder = undefined;
		}
		
		var delegations = this.extValue("eventHandlerDelegations");
		
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
			
			this.extValue("eventHandlerDelegations", delegationsTmp);
		}
	};
	
	
	//----------------------------------------
	// chartBase end
	//----------------------------------------
	
	/** 生成元素ID用的前缀 */
	chartFactory.ELEMENT_ID_PREFIX = "DataGearClient" + new Number(new Date().getTime()).toString(16);
	
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
	 * 解析JSON。
	 * 如果参数不合法，将返回空对象：{}。
	 */
	chartFactory.parseJSONSilently = function(str)
	{
		if(!str)
			return {};
		
		try
		{
			if(typeof($) != "undefined" && $.parseJSON)
				return $.parseJSON(str);
			else
				return JSON.parse(str);
		}
		catch(e)
		{
			chartFactory.logException(e);
		}
		
		return {};
	};
	
	/**
	 * 将不符合JSON规范的字符串定义规范化：属性名添加双引号、或将属性名单引号替换为双引号。
	 */
	chartFactory.trimJSONString = function(str)
	{
		if(!str)
			return str;
		
		//替换单引号为双引号
		var str1 = "";
		for(var i=0; i<str.length;i++)
		{
			var c = str.charAt(i);
			
			if(c == '\\')
			{
				str1 += c;
				i = i+1;
				str1 += str.charAt(i);
			}
			else if(c == '\'')
				str1 += '"';
			else
				str1 += c;
		}
		
		str = str1;
		
		//属性名匹配表达式
		var reg = /([{,]\s*)([^\:\s]*)(\s*:)/g;
		
		return str.replace(reg, function(token, prefix, name, suffix)
		{
			var len = name.length;
			
			if(len > 1 && name.charAt(0) == '"' && name.charAt(len-1) == '"')
				return token;
			else if(len > 1 && name.charAt(0) == '\'' && name.charAt(len-1) == '\'')
			{
				name = '"' + name.substring(1, len-1) + '"';
				return prefix + name + suffix;
			}
			else
				return prefix + '"' + name + '"' + suffix;
		});
	};
	
	/**
	 * 将对象转换为JSON字符串。
	 */
	chartFactory.toJSONString = function(obj)
	{
		return JSON.stringify(obj);
	};
	
	/**
	 * 为元素设置主题样式。
	 * 
	 * @param element HTML元素、Jquery对象
	 * @param theme 主题对象
	 */
	chartFactory.setThemeStyle = function(element, theme)
	{
		return this.setStyles(element, theme);
	};
	
	/**
	 * 为元素设置样式集。
	 * 
	 * @param element HTML元素、Jquery对象
	 * @param stylesObj 样式对象，格式为：{ color: "...", backgroundColor: "...", fontSize: "...", ...  }，不合法的项将被忽略
	 * @return 旧样式集对象
	 */
	chartFactory.setStyles = function(element, stylesObj)
	{
		var olds = {};
		
		if(element.length > 0)
			element = element[0];
		
		if(stylesObj && element.style != undefined)
		{
			for(var p in stylesObj)
			{
				var newStyle = stylesObj[p];
				
				//忽略不合法的项
				var newStyleType = typeof(newStyle);
				if(newStyleType != "string" && newStyleType != "number")
					continue;
				
				olds[p] = element.style[p];
				element.style[p] = stylesObj[p];
			}
		}
		
		return olds;
	};
	
	/**
	 * 获取样式集对象的CSS样式文本。
	 * 
	 * @param stylesObj 样式对象，格式为：{ color: "...", backgroundColor: "...", fontSize: "...", ...  }，不合法的项将被忽略
	 * @return CSS样式文本，格式为："color: red; background-color: red; font-size: 1px;"
	 */
	chartFactory.stylesObjToCssText = function(stylesObj)
	{
		var elementId = chartFactory.ELEMENT_ID_PREFIX +"StylesObjToCss";
		
		var element = $("#" + elementId);
		if(element.length == 0)
		{
			var parent = $("<div style='display:none;position:absolute;left:0;bottom:0;width:0;height:0;z-index:-999;' />").appendTo(document.body);
			element = $("<div />").attr("id", elementId).appendTo(parent);
		}
		
		element.attr("style", "");
		chartFactory.setStyles(element, stylesObj);
		
		return (element.attr("style") || "");
	};
	
	/**
	 * 获取主题指定因子的渐变色。
	 * 
	 * @param theme
	 * @param factor 颜色因子，0-1之间
	 */
	chartFactory.getGradualColor = function(theme, factor)
	{
		var gcs = theme._gradualColors;
		
		if(!gcs || gcs.length == 0)
		{
			gcs = this.evalGradualColors(theme.actualBackgroundColor, theme.color, (theme.gradient || 10));
			theme._gradualColors = gcs;
		}
		
		var index = parseInt((gcs.length-1) * factor);
		
		if(index == 0 && factor > 0)
			index = 1;
		
		if(index == gcs.length - 1 && factor < 1)
			index == gcs.length - 2;
		
		return gcs[index];
	};
	
	/**
	 * 计算起始颜色和终止颜色之间的渐变颜色数组，数组中不包含起始颜色、但包含结束颜色。
	 * 
	 * @param start 起始颜色
	 * @param end 终止颜色
	 * @param count 计算数目
	 * @param rgb true 返回"rgb(...)"格式；fasle 返回"#FFFFFF"格式，默认为false
	 */
	chartFactory.evalGradualColors = function(start, end, count, rgb)
	{
		var colors = [];
		
		start = this.parseColor(start);
		end = this.parseColor(end);
		
		for(var i=1; i<=count; i++)
		{
			var color = {};
			
			color.r = parseInt(start.r + (end.r - start.r)/count*i);
			color.g = parseInt(start.g + (end.g - start.g)/count*i);
			color.b = parseInt(start.b + (end.b - start.b)/count*i);
			
			if(rgb)
				color = "rgb("+color.r+","+color.g+","+color.b+")";
			else
			{
				var r = new Number(color.r).toString(16);
				var g = new Number(color.g).toString(16);
				var b = new Number(color.b).toString(16);
				
				color = "#" + (r.length == 1 ? "0"+r : r)
							 + (g.length == 1 ? "0"+g : g)
							  + (b.length == 1 ? "0"+b : b);
			}
			
			colors.push(color);
		}
		
		return colors;
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
			var elementId = chartFactory.ELEMENT_ID_PREFIX +"ForConvertColor";
			
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
	 * 创建CSS样式表。
	 * 
	 * @param id 样式表元素ID
	 * @param cssText 样式文本
	 */
	chartFactory.createStyleSheet = function(id, cssText)
	{
	    var head = (document.head || document.getElementsByTagName("head")[0]);
	    var style = document.createElement("style");
	    head.appendChild(style);
	    
	    style.id = id;
	    style.type = "text/css";
	    
	    // 旧版IE
	    if (style.styleSheet)
	    	style.styleSheet.cssText = cssText;
	    else
	    	style.appendChild(document.createTextNode(cssText));
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
	 *
	 * @param prefix 可选，ID前缀
	 */
	chartFactory.nextElementId = function(prefix)
	{
		if(prefix == null)
			prefix = "";
		
		var seq = (this._nextElementIdSequence != null ? this._nextElementIdSequence : 0);
		this._nextElementIdSequence = seq + 1;
		
		return this.ELEMENT_ID_PREFIX + prefix + seq;
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
	
	/**
	 * 记录异常日志。
	 * 
	 * @param exception 异常对象、异常消息字符串
	 */
	chartFactory.logException = function(exception)
	{
		if(typeof console != "undefined")
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
	 * 由图表主题构建echarts主题。
	 * 
	 * @param chartTheme 图表主题对象：org.datagear.analysis.ChartTheme
	 */
	chartFactory.buildEchartsTheme = function(chartTheme)
	{
		var axisColor = this.getGradualColor(chartTheme, 0.8);
		var axisScaleLineColor = this.getGradualColor(chartTheme, 0.6);
		var areaColor0 = this.getGradualColor(chartTheme, 0.15);
		var areaBorderColor0 = this.getGradualColor(chartTheme, 0.3);
		var areaColor1 = this.getGradualColor(chartTheme, 0.25);
		var areaBorderColor1 = this.getGradualColor(chartTheme, 0.5);
		var shadowColor = this.getGradualColor(chartTheme, 0.9);
		
		//@deprecated 兼容1.8.1版本有ChartTheme.axisColor的结构
		if(chartTheme.axisColor)
			axisColor = chartTheme.axisColor;
		//@deprecated 兼容1.8.1版本有ChartTheme.axisColor的结构
		
		//@deprecated 兼容1.8.1版本有ChartTheme.axisScaleLineColor的结构
		if(chartTheme.axisScaleLineColor)
			axisScaleLineColor = chartTheme.axisScaleLineColor;
		//@deprecated 兼容1.8.1版本有ChartTheme.axisScaleLineColor的结构
		
		var theme =
		{
			"color" : chartTheme.graphColors,
			"backgroundColor" : chartTheme.backgroundColor,
			"textStyle" : {},
			"title" : {
		        "left" : "center",
				"textStyle" : {
					"color" : chartTheme.titleColor
				},
				"subtextStyle" : {
					"color" : chartTheme.titleColor
				}
			},
			"line" : {
				"itemStyle" : {
					"normal" : {
						"borderWidth" : 1
					}
				},
				"lineStyle" : {
					"normal" : {
						"width" : 2
					}
				},
				"symbolSize" : 4,
				"symbol" : "emptyCircle",
				"smooth" : false
			},
			"radar" : {
				"name" : { "textStyle" : { "color" : chartTheme.legendColor } },
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
			"boxplot" : {
				"itemStyle" : {
					"borderWidth" : 0,
					"borderColor" : chartTheme.borderColor
				},
				"emphasis" : {
					"itemStyle" : {
						"borderWidth" : 0,
						"borderColor" : chartTheme.borderColor
					}
				}
			},
			"parallel" : {
				"itemStyle" : {
					"borderWidth" : 0,
					"borderColor" : chartTheme.borderColor
				},
				"emphasis" : {
					"itemStyle" : {
						"borderWidth" : 0,
						"borderColor" : chartTheme.borderColor
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
						"color": areaColor1,
						"opacity": 0.6
					}
				}
			},
			"funnel" : {
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
				"title" : { color : chartTheme.legendColor },
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
					"color" : chartTheme.graphColors[0],
					"color0" : chartTheme.graphColors[1],
					"borderColor" : chartTheme.graphColors[0],
					"borderColor0" : chartTheme.graphColors[1],
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
			"tree":
			{
				"label":
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
			"graph" : {
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
					}
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
				"left": 30,
				"right": 46,
				"top": 80,
				"bottom": 20,
				"containLabel": true
			},
			"legend" : {
				"orient": "horizontal",
				"top": 25,
				"textStyle" : {
					"color" : chartTheme.legendColor
				},
				"inactiveColor" : axisScaleLineColor
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
		
		return theme;
	};
})
(this);
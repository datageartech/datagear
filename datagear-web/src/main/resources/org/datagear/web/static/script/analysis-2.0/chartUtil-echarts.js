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
 * ECharts图表工具函数库。
 * 全局变量名：window.chartUtil.echarts
 * 
 * 加载时依赖：
 *   无
 * 
 * 运行时依赖:
 *   echarts
 */
(function(global)
{

var chartUtil = (global.chartUtil || (global.chartUtil = {}));
var echartsUtil = (chartUtil.echarts || (chartUtil.echarts = {}));

/**
 * 初始化图表的ECharts主题名。
 * 此函数依次从图表元素、<body>元素的elementAttrConst.ECHARTS_THEME属性获取ECharts主题名。
 */
echartsUtil._initEchartsThemeName = function()
{
	var themeName = CF.eleAttr(this.element(), elementAttrConst.ECHARTS_THEME);
	if(!themeName)
		themeName = CF.eleAttr(document.body, elementAttrConst.ECHARTS_THEME);
	
	this.echartsThemeName(themeName);
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
echartsUtil.echartsThemeName = function(themeName)
{
	if(themeName === undefined)
		return this._echartsThemeName;
	else
		this._echartsThemeName = themeName;
};


/**
 * ECharts图表支持函数：将图表初始化为ECharts图表，设置其选项。
 * 此函数会自动应用chartBase.echartsGetThemeName()至初始化的ECharts图表。
 * 此函数会自动调用chartBase.internal()将初始化的ECharts实例对象设置为图表底层组件。
 * 
 * @param options 要设置的ECharts选项，为null表示不设置
 * @param opts 可选，ECharts的init函数附加参数，具体参考ECharts.init()函数的opts参数
 * @returns ECharts实例对象
 */
chartBase.echartsInit = function(options, opts)
{
	var instance = echarts.init(this.element(), this.echartsGetThemeName(), opts);
	this.internal(instance);
	
	if(options != null)
	{
		instance.setOption(options);
	}
	
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
		throw new Error("chart is not ECharts");
	
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
	else if(CF.isFunction(callback))
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
		var settings = CF.extend({}, callback);
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

/** 关键字：注册得ECharts主题名 */
var REGISTERED_ECHARTS_THEME_NAME = CF.BUILTIN_PROP_PREFIX + "RegisteredEchartsThemeName";

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
		var theme = this.theme();
		themeName = theme[REGISTERED_ECHARTS_THEME_NAME];
		
		if(!themeName)
		{
			themeName = (theme[REGISTERED_ECHARTS_THEME_NAME] = CF.uid());
			
			var echartsTheme = CF.buildEchartsTheme(theme);
			echarts.registerTheme(themeName, echartsTheme);
		}
	}
	
    return themeName;
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
	
})(this);

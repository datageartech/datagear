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
 *   chartFactory.js
 * 
 * 运行时依赖:
 *   echarts
 */
(function(global)
{

/**图表工厂*/
var CF = global.chartFactory;

var chartUtil = (global.chartUtil || (global.chartUtil = {}));
var EU = (chartUtil.echarts || (chartUtil.echarts = {}));

//图表元素属性名：ECharts主题名
EU.ELE_ATTR_ECHARTS_THEME = "dg-echarts-theme";

//图表主题中的ECharts主题名属性名
EU.THEME_PROP_ECHARTS_THEME_NAME = "DG_ECHARTS_THEME_NAME";

//注册地图状态
//键："地图名"；值：{ loaded: true、false, fetchPromise: Promise }
EU.MAP_REGISTER_STATES = {};

EU.version = "1.0";

/**
 * 获取全局ECharts对象。
 */
EU.echarts = function()
{
	return global.echarts;
};

/**
 * 将图表初始化为ECharts图表。
 * 此函数会自动将EU.themeName()函数、EU.themeNameOfChartTheme()函数返回的主题名应用至初始化的ECharts图表主题。
 * 此函数会自动调用chart.internal()将初始化的ECharts实例对象设置为图表底层组件。
 * 
 * @param chart 图表
 * @param opts 可选，同echarts.init()函数的opts附加参数
 * @returns ECharts实例
 */
EU.init = function(chart, opts)
{
	var themeName = EU.themeName(chart);
	
	if(!themeName)
		themeName = EU.themeNameOfChartTheme(chart);
	
	var instance = EU.echarts().init(chart.element(), themeName, opts);
	chart.internal(instance);
	
	return instance;
};

/**
 * 获取在图表元素上（优先）或者<body>元素上通过"dg-echarts-theme"属性定义的ECharts主题名
 * 
 * @param chart 图表
 * @returns ECharts主题名
 */
EU.themeName = function(chart)
{
	var themeName = CF.eleAttr(chart.element(), CF.elementAttrConst.ECHARTS_THEME);
	
	if(!themeName)
		themeName = CF.eleAttr(document.body, CF.elementAttrConst.ECHARTS_THEME);
	
	return themeName;
};

/**
 * 获取根据图表主题生成（只第一次生成）的ECharts主题名。
 * 
 * @param chart 图表
 * @returns ECharts主题名，对应的主题由图表主题生成且已经注册至ECharts
 */
EU.themeNameOfChartTheme = function(chart)
{
	var theme = chart.theme();
	var themeName = theme[EU.THEME_PROP_ECHARTS_THEME_NAME];
	
	if(!themeName)
	{
		themeName = (theme[EU.THEME_PROP_ECHARTS_THEME_NAME] = CF.uid());
		
		var echartsTheme = EU._buildEchartsTheme(chart);
		EU.echarts().registerTheme(themeName, echartsTheme);
	}
	
    return themeName;
};

/**
 * 为图表注册指定名称的地图（GeoJSON、SVG）至ECharts，并在注册完成后执行回调函数。
 * 如果地图未加载，将在加载后再注册。
 * 注意：如果在图表渲染器的render()、update()函数中调用此函数，应该首先设置渲染器的asyncRender、asyncUpdate，
 * 并在callback中调用chart.statusRendered(true)、chart.statusUpdated(true)。
 * 
 * @param chart 图表
 * @param name 地图名称
 * @param complete 可选，注册完成后（无论是否成功）的回调函数，格式为：function(name){ ... }
 */
EU.registerMap = function(chart, name, complete)
{
	var echarts = EU.echarts();
	
	if(echarts.getMap(name) != null)
	{
		if(complete != null)
			complete(name);
	}
	else
	{
		let state = EU.MAP_REGISTER_STATES[name];
		
		if(state && state.loaded === true)
		{
			//释放内存
			if(state.fetchPromise != null)
				state.fetchPromise = null;
			
			if(complete != null)
				complete(name);
		}
		else
		{
			if(state == null)
			{
				let mapUrl = chart.mapURL(name);
				
				state = { loaded: false, fetchPromise: fetch(mapUrl) };
				EU.MAP_REGISTER_STATES[name] = state;
				
				state.fetchPromise.then((response) =>
				{
					if(!response.ok)
						throw new Error(response.statusText ? response.statusText : response.status+"");
					
					let headers = response.headers;
					let contentType = (headers.get("Content-Type") || "");
					//是否SVG地图
					let isSvg = (/svg/i.test(contentType) || /(\.svg$)|(\.svg[\?\#])/i.test(url));
					
					if(isSvg)
					{
						return response.text().then((svgText) =>
						{
							EU.echarts().registerMap(name, {svg: svgText});
							state.loaded = true;
							
							return response;
						});
					}
					else
					{
						return response.json().then((geoJSON) =>
						{
							EU.echarts().registerMap(name, {geoJSON: geoJSON});
							state.loaded = true;
							
							return response;
						});
					}
				})
				.catch(() =>
				{
					EU.MAP_REGISTER_STATES[name] = null;
				});
			}
			
			state.fetchPromise.finally(() =>
			{
				complete(name);
			});
		}
	}
};

/**
 * 由图表主题构建ECharts主题。
 * 
 * @param chart 图表
 */
EU._buildEchartsTheme = function(chart)
{
	var axisColor = chart.themeGradualColor(0.7);
	var axisScaleLineColor = chart.themeGradualColor(0.35);
	var areaColor0 = chart.themeGradualColor(0.1);
	var areaBorderColor0 = chart.themeGradualColor(0.3);
	var areaColor1 = chart.themeGradualColor(0.25);
	var areaBorderColor1 = chart.themeGradualColor(0.5);
	var shadowColor = chart.themeGradualColor(0.9);
	var emptyAreaColor = chart.themeGradualColor(0);
	var emptyBorderColor = areaColor0;
	
	var chartTheme = chart.theme();
	
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
				"color": emptyAreaColor,
				"borderColor": emptyBorderColor
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

/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 图表工具集：window.chartUtil。
 * 
 * 依赖:
 * echarts.js
 */
(function(window)
{
	var util = (window.chartUtil || (window.chartUtil = {}));
	util.echarts = (util.echarts || {});
	
	/**
	 * 获取图表主题。
	 */
	util.chartTheme = function(chart)
	{
		return this.renderContextAttr(chart, "chartTheme");
	};
	
	/**
	 * 获取渲染样式。
	 */
	util.chartRenderStyle = function(chart)
	{
		return this.renderContextAttr(chart, "renderStyle");
	};

	/**
	 * 获取/设置图表的"name"属性值。
	 * 
	 * @param chart
	 * @param value 可选，要设置的属性值
	 */
	util.propertyValueName = function(chart, value)
	{
		return this.propertyValue(chart, "name", value);
	};

	/**
	 * 获取/设置图表的"updateInterval"属性值。
	 * 
	 * @param chart
	 * @param value 可选，要设置的属性值
	 */
	util.propertyValueUpdateInterval = function(chart, value)
	{
		return this.propertyValue(chart, "updateInterval", value);
	};
	
	/**
	 * 获取/设置图表属性值。
	 * 
	 * @param chart
	 * @param name
	 * @param value 可选，要设置的属性值
	 */
	util.propertyValue = function(chart, name, value)
	{
		if(!chart.propertyValues)
			chart.propertyValues = {};
		
		if(value == undefined)
			return chart.propertyValues[name];
		else
			chart.propertyValues[name] = value;
	};
	
	/**
	 * 获取/设置图表渲染上下文的属性值。
	 * 
	 * @param chart
	 * @param attrName
	 * @param attrValue 可选，要设置的属性值
	 */
	util.renderContextAttr = function(chart, attrName, attrValue)
	{
		var renderContext = chart.renderContext;
		
		if(attrValue == undefined)
			return renderContext.attributes[attrName];
		else
			return renderContext.attributes[attrName] = attrValue;
	};
	
	/**
	 * 获取第一个图标数据集对象。
	 * 
	 * @param chart
	 */
	util.firstChartDataSet = function(chart)
	{
		if(!chart || !chart.chartDataSets || chart.chartDataSets.length < 1)
			return {};
			
		return chart.chartDataSets[0];
	};
	
	/**
	 * 获取指定标记的第一个数据集属性，没有则返回null。
	 * 
	 * @param chartDataSet 图表数据集对象
	 * @param dataSign 数据标记对象、标记名称
	 */
	util.dataSetPropertyOfSign = function(chartDataSet, dataSign)
	{
		var properties = this.dataSetPropertiesOfSign(chartDataSet, dataSign);
		
		return (properties.length > 0 ? properties[0] : null);
	};
	
	/**
	 * 获取指定标记的数据集属性数组。
	 * 
	 * @param chartDataSet 图表数据集对象
	 * @param dataSign 数据标记对象、标记名称
	 */
	util.dataSetPropertiesOfSign = function(chartDataSet, dataSign)
	{
		var re = [];
		
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
		
		for(var i=0; i<signPropertyNames.length; i++)
		{
			for(var j=0; j<dataSetProperties.length; j++)
			{
				if(dataSetProperties[j].name == signPropertyNames[i])
					re.push(dataSetProperties[j]);
			}
		}
		
		return re;
	};

	/**
	 * 返回第一个数据结果集。
	 */
	util.firstResult = function(dataSetResults)
	{
		if(!dataSetResults || dataSetResults.length < 1)
			return [];
		
		return dataSetResults[0];
	};

	/**
	 * 获取结果数据属性值数组。
	 * 
	 * @param result 数据集结果对象、对象数组
	 * @param property 属性对象、属性名、属性对象数组、属性名数组
	 */
	util.dataPropertyValues = function(result, property)
	{
		var re = [];
		
		if(property == null)
			return re;
		
		var datas = (result.length != null ? result : (result.datas || []));
		
		if(property.length > 0)
		{
			for(var i=0; i<property.length; i++)
			{
				var myValues = [];
				
				var cm = property[i];
				
				var name = (cm.name || cm);
				
				for(var j=0; j< datas.length; j++)
					myValues[j] = datas[j][name];
				
				re[i] = myValues;
			}
		}
		else
		{
			var name = (property.name || property);
			
			for(var i=0; i< datas.length; i++)
				re[i] = datas[i][name];
		}
		
		return re;
	};
	
	/**
	 * 获取结果数据的名称/值对象数组：[{name: ..., value: ...}, ...]。
	 * 
	 * @param result 数据集结果对象、对象数组
	 * @param nameProperty 名称属性对象、属性名
	 * @param valueProperty 值属性对象、属性名
	 */
	util.dataNameValueObjects = function(result, nameProperty, valueProperty)
	{
		var re = [];
		
		var datas = (result.length != null ? result : (result.datas || []));
		
		nameProperty = (nameProperty.name || nameProperty);
		valueProperty = (valueProperty.name || valueProperty);
		
		for(var i=0; i< datas.length; i++)
		{
			var obj =
			{
				"name" : datas[i][nameProperty],
				"value" : datas[i][valueProperty]
			};
			
			re[i] = obj;
		}
		
		return re;
	};

	/**
	 * 初始化Echarts对象。
	 * 
	 * @param chart 图表对象
	 * @param echartsOption Echarts设置项
	 */
	util.echarts.init = function(chart, echartsOption)
	{
		var echartsObj = echarts.init(document.getElementById(chart.elementId), this.theme(chart));
		
		if(echartsOption != null)
			echartsObj.setOption(echartsOption);
		
		return echartsObj;
	};
	
	/**
	 * 获取echarts主题，如果没有，则自动注册。
	 * 
	 * @param dashboard
	 */
	util.echarts.theme = function(chart)
	{
		var renderStyle = util.chartRenderStyle(chart);
		var chartTheme = util.chartTheme(chart);
		
		if(!renderStyle || !chartTheme)
			return "";
		
		if(this._REGISTERED_THEME)
			return renderStyle;
		
		this._REGISTERED_THEME = true;
		
		var contrastColor = chartTheme.envMajorColor;
	    var axisCommon = function () {
	        return {
	            axisLine: {
	                lineStyle: {
	                    color: contrastColor
	                }
	            },
	            axisTick: {
	                lineStyle: {
	                    color: contrastColor
	                }
	            },
	            axisLabel: {
	                textStyle: {
	                    color: contrastColor
	                }
	            },
	            splitLine: {
	                lineStyle: {
	                    type: 'dashed',
	                    color: chartTheme.envLeastColor
	                }
	            },
	            splitArea: {
	                areaStyle: {
	                    color: contrastColor
	                }
	            }
	        };
	    };
	    
	    var colorPalette = chartTheme.graphColors;
	    var theme = {
	        color: colorPalette,
	        backgroundColor: chartTheme.backgroundColor,
	        tooltip: {
	            axisPointer: {
	                lineStyle: {
	                    color: contrastColor
	                },
	                crossStyle: {
	                    color: contrastColor
	                }
	            }
	        },
	        legend: {
	            textStyle: {
	                color: contrastColor
	            }
	        },
	        textStyle: {
	            color: contrastColor
	        },
	        title: {
	            textStyle: {
	                color: contrastColor
	            }
	        },
	        toolbox: {
	            iconStyle: {
	                normal: {
	                    borderColor: contrastColor
	                }
	            }
	        },
	        dataZoom: {
	            textStyle: {
	                color: contrastColor
	            }
	        },
	        timeline: {
	            lineStyle: {
	                color: contrastColor
	            },
	            itemStyle: {
	                normal: {
	                    color: colorPalette[1]
	                }
	            },
	            label: {
	                normal: {
	                    textStyle: {
	                        color: contrastColor
	                    }
	                }
	            },
	            controlStyle: {
	                normal: {
	                    color: contrastColor,
	                    borderColor: contrastColor
	                }
	            }
	        },
	        timeAxis: axisCommon(),
	        logAxis: axisCommon(),
	        valueAxis: axisCommon(),
	        categoryAxis: axisCommon(),
	        line: {
	            symbol: 'circle'
	        },
	        graph: {
	            color: colorPalette
	        },
	        gauge: {
	            title: {
	                textStyle: {
	                    color: contrastColor
	                }
	            }
	        },
	        candlestick: {
	            itemStyle: {
	                normal: {
	                    color: contrastColor,
	                    color0: chartTheme.envMinorColor,
	                    borderColor: chartTheme.borderColor,
	                    borderColor0: chartTheme.borderColor
	                }
	            }
	        }
	    };
	    
	    theme.categoryAxis.splitLine.show = false;
	    echarts.registerTheme(renderStyle, theme);
	    
	    return renderStyle;
	};
})
(window);
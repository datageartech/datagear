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
	 * 
	 * @param chart
	 * @return {...}
	 */
	util.chartTheme = function(chart)
	{
		return this.renderContextAttr(chart, "chartTheme");
	};
	
	/**
	 * 获取渲染风格。
	 * 
	 * @param chart
	 * @return "..."
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
	 * 获取第一个图表数据集对象。
	 * 
	 * @param chart
	 * @return {dataSet: [], propertySigns: {...}}
	 */
	util.firstChartDataSet = function(chart)
	{
		if(!chart || !chart.chartDataSets || chart.chartDataSets.length < 1)
			return {};
			
		return chart.chartDataSets[0];
	};
	
	/**
	 * 获取图表数据集对象数组。
	 */
	util.chartDataSets = function(chart)
	{
		if(!chart || !chart.chartDataSets || chart.chartDataSets.length < 1)
			return [];
			
		return chart.chartDataSets;
	};
	
	/**
	 * 获取图表名称。
	 * 
	 * @param chartDataSet 图表数据集对象
	 */
	util.dataSetName = function(chartDataSet)
	{
		var dataSet = (chartDataSet.dataSet || chartDataSet);
		var name = (dataSet ? dataSet.name : "");
		
		return (name ? name : "");
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
	 * 获取数据集属性标签，没有则返回空字符串。
	 * 
	 * @param dataSetProperty
	 */
	util.dataSetPropertyLabel = function(dataSetProperty)
	{
		var label = (dataSetProperty ? dataSetProperty.label : null);
		
		if(!label && dataSetProperty)
			label = dataSetProperty.name;
		
		if(!label)
			label = "";
		
		return label;
	};

	/**
	 * 返回第一个数据集结果。
	 */
	util.resultFirst = function(dataSetResults)
	{
		if(!dataSetResults || dataSetResults.length < 1)
			return {};
		
		return dataSetResults[0];
	};
	
	util.resultIndex = function(dataSetResults, index)
	{
		if(!dataSetResults || !dataSetResults.length || dataSetResults.length < index)
			return {};
			
		return dataSetResults[index];
	};
	
	/**
	 * 获取数据集结果指定属性、指定行的单元格值。
	 * 
	 * @param result 数据集结果对象、对象数组
	 * @param property 数据集属性对象、属性名
	 * @param row 行索引，以0开始，默认为0
	 */
	util.resultCell = function(result, property, row)
	{
		if(row == null)
			row = 0;
		
		var re = this.resultRowArrays(result, property, row+1);
		
		return (re.length > row ? re[row] : undefined);
	};
	
	/**
	 * 将数据集结果的行对象按照指定properties顺序转换为行值数组。
	 * 
	 * @param result 数据集结果对象、对象数组
	 * @param properties 数据集属性对象数组、属性名数组、属性对象、属性名
	 * @param count 获取的最多行数，可选，默认为全部
	 * @return properties为数组时：[[..., ...], ...]；properties非数组时：[..., ...]
	 */
	util.resultRowArrays = function(result, properties, count)
	{
		var re = [];
		
		if(properties == null)
			return re;
		
		var datas = (result.length != null ? result : (result.datas || []));
		
		var getCount = datas.length;
		if(count != null && count < getCount)
			getCount = count;
		
		if(properties.length > 0)
		{
			for(var i=0; i< getCount; i++)
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
				
				re[i] = row;
			}
		}
		else
		{
			var name = (properties ? (properties.name || properties) : undefined);
			
			if(name)
			{
				for(var i=0; i< getCount; i++)
				{
					var rowObj = datas[i];
					re[i] = rowObj[name];
				}
			}
		}
		
		return re;
	};
	
	/**
	 * 将数据集结果的行对象按照指定properties顺序转换为列值数组。
	 * 
	 * @param result 数据集结果对象、对象数组
	 * @param properties 数据集属性对象数组、属性名数组、属性对象、属性名
	 * @param count 获取的最多行数，可选，默认为全部
	 * @return properties为数组时：[[..., ...], ...]；properties非数组时：[..., ...]
	 */
	util.resultColumnArrays = function(result, properties, count)
	{
		var re = [];
		
		if(properties == null)
			return re;
		
		var datas = (result.length != null ? result : (result.datas || []));
		
		var getCount = datas.length;
		if(count != null && count < getCount)
			getCount = count;
		
		if(properties.length > 0)
		{
			for(var i=0; i<properties.length; i++)
			{
				var p = properties[i];
				
				var name = (p ? (p.name || p) : undefined);
				if(!name)
					continue;
				
				var column = [];
				
				for(var j=0; j< getCount; j++)
					column[j] = datas[j][name];
				
				re[i] = column;
			}
		}
		else
		{
			var name = (properties ? (properties.name || properties) : undefined);

			if(name)
			{
				for(var i=0; i< getCount; i++)
				{
					var rowObj = datas[i];
					re[i] = rowObj[name];
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
	 * @param valueProperty 值属性对象、属性名
	 * @param count 获取结果数据的最多行数，可选，默认为全部
	 * @return [{name: ..., value: ...}, ...]
	 */
	util.resultNameValueObjects = function(result, nameProperty, valueProperty, count)
	{
		var re = [];
		
		var datas = (result.length != null ? result : (result.datas || []));
		
		var getCount = datas.length;
		if(count != null && count < getCount)
			getCount = count;
		
		nameProperty = (nameProperty.name || nameProperty);
		valueProperty = (valueProperty.name || valueProperty);
		
		for(var i=0; i< getCount; i++)
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
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
	util.dataset = (util.dataset || {});
	
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
	 * 初始化Echarts对象。
	 * 
	 * @param chart 图表对象
	 */
	util.echarts.init = function(chart)
	{
		var echartsObj = echarts.init(document.getElementById(chart.elementId), this.theme(chart));
		
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
	
	/**
	 * 获取数据集数组的第一个元素，没有则返回undefined。
	 * 
	 * @param dataSets
	 */
	util.dataset.first = function(dataSets)
	{
		return (dataSets && dataSets.length > 0 ? dataSets[0] : undefined);
	};

	/**
	 * 获取指定名称的列元信息对象，没有则返回undefined。
	 * 
	 * @param dataSet
	 * @param name
	 */
	util.dataset.columnMetaByName = function(dataSet, name)
	{
		var columnMetas = dataSet.meta.columnMetas;
		
		for(var i=0; i<columnMetas.length; i++)
		{
			if(columnMetas[i].name === name)
				return columnMetas[i];
		}
		
		return undefined;
	};
	
	/**
	 * 获取第一个维度列元信息对象，没有则返回undefined。
	 * 
	 * @param dataSet
	 */
	util.dataset.columnMetaByDimension = function(dataSet)
	{
		var columnMetas = dataSet.meta.columnMetas;
		
		for(var i=0; i<columnMetas.length; i++)
		{
			if(columnMetas[i].dataCategory === "DIMENSION")
				return columnMetas[i];
		}
		
		return undefined;
	};
	
	/**
	 * 获取维度列元信息对象数组。
	 * 
	 * @param dataSet
	 */
	util.dataset.columnMetasByDimension = function(dataSet)
	{
		var re = [];
		
		var columnMetas = dataSet.meta.columnMetas;
		
		for(var i=0; i<columnMetas.length; i++)
		{
			if(columnMetas[i].dataCategory === "DIMENSION")
				re.push(columnMetas[i]);
		}
	};

	/**
	 * 获取第一个量度列元信息对象。
	 * 
	 * @param dataSet
	 */
	util.dataset.columnMetaByScalar = function(dataSet)
	{
		var columnMetas = dataSet.meta.columnMetas;
		
		for(var i=0; i<columnMetas.length; i++)
		{
			if(columnMetas[i].dataCategory === "SCALAR")
				return columnMetas[i];
		}
		
		return undefined;
	};
	
	/**
	 * 获取量度列元信息对象数组。
	 * 
	 * @param dataSet
	 */
	util.dataset.columnMetasByScalar = function(dataSet)
	{
		var re = [];
		
		var columnMetas = dataSet.meta.columnMetas;
		
		for(var i=0; i<columnMetas.length; i++)
		{
			if(columnMetas[i].dataCategory === "SCALAR")
				re.push(columnMetas[i]);
		}
	};
	
	/**
	 * 获取列值数组。
	 * 
	 * @param dataSet
	 * @param columnMeta 列元信息对象、列名、列元信息对象数组、列名数组
	 */
	util.dataset.columnValues = function(dataSet, columnMeta)
	{
		var re = [];
		
		if(columnMeta == null)
			return re;
		
		var datas = dataSet.datas;
		
		if(columnMeta.length > 0)
		{
			for(var i=0; i<columnMeta.length; i++)
			{
				var myValues = [];
				
				var cm = columnMeta[i];
				
				var name = (cm.name || cm);
				
				for(var j=0; j< datas.length; j++)
					myValues[j] = datas[j][name];
				
				re[i] = myValues;
			}
		}
		else
		{
			var name = (columnMeta.name || columnMeta);
			
			for(var i=0; i< datas.length; i++)
				re[i] = datas[i][name];
		}
		
		return re;
	};
	

	/**
	 * 获取列值的名称/值对象数组：[{name: ..., value: ...}, ...]。
	 * 
	 * @param dataSet
	 * @param nameColumnMeta 名称列元信息对象、列名
	 * @param valueColumnMeta 值列元信息对象、列名
	 */
	util.dataset.columnNameValues = function(dataSet, nameColumnMeta, valueColumnMeta)
	{
		var re = [];
		
		var datas = dataSet.datas;
		
		var nameColumn = (nameColumnMeta.name || nameColumnMeta);
		var valueColumn = (valueColumnMeta.name || valueColumnMeta);
		
		for(var i=0; i< datas.length; i++)
		{
			var obj =
			{
				"name" : datas[i][nameColumn],
				"value" : datas[i][valueColumn]
			};
			
			re[i] = obj;
		}
		
		return re;
	};
})
(window);
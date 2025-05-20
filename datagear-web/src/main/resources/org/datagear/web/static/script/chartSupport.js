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
 * 图表支持库。
 * 全局变量名：window.chartFactory.chartSupport
 * 
 * 加载时依赖：
 *   无
 * 
 * 运行时依赖:
 *   jquery.js
 *   echarts.js
 *   chartFactory.js
 */
(function(global)
{
	var chartFactory = (global.chartFactory || (global.chartFactory = {}));
	var chartSupport = (chartFactory.chartSupport || (chartFactory.chartSupport = {}));
	var builtinOptionNames = (chartFactory.builtinOptionNames || (chartFactory.builtinOptionNames = {}));
	
	// < @deprecated 兼容1.8.1版本的window.chartSupport变量名，未来版本会移除
	global.chartSupport = chartSupport;
	// > @deprecated 兼容1.8.1版本的window.chartSupport变量名，未来版本会移除
	
	//内置地图类图表的地图选项名
	//默认的ECharts地图类图表配置地图名稍微麻烦，
	//所有这里的内置图表都支持此快捷方式设置地图名选项
	builtinOptionNames.mapName = "mapName";
	
	//内置类目轴数据排序配置选项名
	builtinOptionNames.sortAxisData = "sortAxisData";
	
	//内置表格的更新底层组件回调函数选项名
	builtinOptionNames.updateInternal = "updateInternal";
	
	//内置表格图表服务端分页选项名
	builtinOptionNames.serverSidePaging = "serverSidePaging";
	
	//折线图
	
	chartSupport.lineRender = function(chart, options)
	{
		options = $.extend(true,
		{
			dg:
			{
				//name 必选，名称
				//value 必选，当标记category时单选，否则可多选，数值
				//category 可选，类别，不同类别绘制为不同系列
				dataSignNames: { name: "name", value: "value", category: "category" },
				//是否堆叠
				stack: false,
				//是否平滑
				smooth: false,
				//是否面积
				area: false,
				//阶梯：true, false, "start", "middle", "end"
				step: false
			}
		},
		options);
		
		var dataSignNames = options.dg.dataSignNames;
		var dataSetBind = chartSupport.dataSetBindMainNonNull(chart);
		var np = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.name);
		var vps = chart.dataSetFieldsOfSign(dataSetBind, dataSignNames.value);
		
		options = chartSupport.inflateRenderOptions(chart,
		{
			title:
			{
		        text: chart.name
		    },
			tooltip:
			{
				trigger: "axis"
			},
			legend:
			{
				id: 0,
				//将在update中设置：
				//data
			},
			xAxis: {
				id: 0,
				name: chart.dataSetFieldAlias(dataSetBind, np),
				nameGap: 5,
				type: chartSupport.evalDataSetFieldAxisType(chart, np),
				boundaryGap: false
			},
			yAxis: {
				name: (vps.length == 1 ? chart.dataSetFieldAlias(dataSetBind, vps[0]) : ""),
				nameGap: 5,
				type: "value"
			},
			series:
			[
				//将在update中设置：
				//{}
				//设初值以免渲染报错
				{
					id: 0,
					type: "line"
				}
			]
		},
		options);
		
		chart.echartsInit(options);
	};
	
	chartSupport.lineUpdate = function(chart, chartResult)
	{
		var renderOptions= chart.renderOptions();
		var dg = renderOptions.dg;
		var dataSignNames = dg.dataSignNames;
		
		var dataSetBinds = chart.dataSetBindsMain();
		
		var legendData = [];
		var series = [];
		
		for(var i=0; i<dataSetBinds.length; i++)
		{
			var dataSetBind = dataSetBinds[i];
			var dataSetAlias = chart.dataSetAlias(dataSetBind);
			var result = chart.resultOf(chartResult, dataSetBind);
			
			var np = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.name);
			var cp = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.category);
			
			if(cp)
			{
				var vp = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.value);
				
				var categoryNames = [];
				var categoryDatasMap = {};
				
				//使用{value: [name,value]}格式可以更好地兼容category、value、time坐标轴类型
				var propertyMap = { "value": [np, vp] };
				propertyMap = chartSupport.inflatePropertyMapWithCategory(propertyMap, cp);
				var data = chart.resultMapObjects(result, propertyMap);
				chart.originalDataIndexes(data, dataSetBind);
				chartSupport.splitDataByCategory(data, categoryNames, categoryDatasMap);
				
				for(var j=0; j<categoryNames.length; j++)
				{
					var categoryName = categoryNames[j];
					var legendName = chartSupport.legendNameForDataCategory(dataSetBinds, dataSetAlias, categoryName);
					var mySeries = {id: series.length, type: "line", name: legendName, data: categoryDatasMap[categoryName]};
					
					//折线图按数据集分组没有展示效果，所以都使用同一个堆叠
					if(dg.stack)
						mySeries.stack = "stack";
					if(dg.smooth)
						mySeries.smooth = true;
					if(dg.area)
						mySeries.areaStyle = {};
					if(dg.step != false)
						mySeries.step = dg.step;
					
					legendData.push(legendName);
					series.push(mySeries);
				}
			}
			else
			{
				var vps = chart.dataSetFieldsOfSign(dataSetBind, dataSignNames.value);
				
				for(var j=0; j<vps.length; j++)
				{
					var legendName = chartSupport.legendNameForDataValues(chart, dataSetBinds, dataSetBind, dataSetAlias, vps, j);
					//使用{value: [name,value]}格式可以更好地兼容category、value、time坐标轴类型
					var data = chart.resultValueObjects(result, [ np, vps[j] ]);
					chart.originalDataIndexes(data, dataSetBind);
					var mySeries = {id: series.length, type: "line", name: legendName, data: data};
					
					//折线图按数据集分组没有展示效果，所以都使用同一个堆叠
					if(dg.stack)
						mySeries.stack = "stack";
					if(dg.smooth)
						mySeries.smooth = true;
					if(dg.area)
						mySeries.areaStyle = {};
					if(dg.step != false)
						mySeries.step = dg.step;
					
					legendData.push(legendName);
					series.push(mySeries);
				}
			}
		}
		
		//坐标轴信息也应替换合并，不然图表刷新有数据变化时，坐标不能自动更新
		var options = { legend: { id: 0, data: legendData}, series: series, xAxis: { id: 0 } };
		
		chartSupport.inflateAxisDataForEchartsUpdateOptions(renderOptions, options, options.xAxis,
						chartSupport.inflateAxisDataExtractors.valueElement(0));
		
		chartSupport.adaptArrayPropsForUpdateOptions(options, renderOptions);
		
		options = chart.inflateUpdateOptions(chartResult, options, function(options)
		{
			chartSupport.adaptValueArrayObjSeriesData(chart, options, "line");
		});
		
		chartSupport.echartsOptionsReplaceMerge(chart, options);
	};
	
	chartSupport.lineResize = function(chart)
	{
		chartSupport.resizeChartEcharts(chart);
	};
	
	chartSupport.lineDestroy = function(chart)
	{
		chartSupport.destroyChartEcharts(chart);
	};
	
	chartSupport.lineOn = function(chart, eventType, handler)
	{
		chartSupport.bindChartEventHandlerForEcharts(chart, eventType, handler,
				chartSupport.lineSetChartEventData);
	};
	
	chartSupport.lineOff = function(chart, eventType, handler)
	{
		chart.echartsOffEventHandler(eventType, handler);
	};
	
	chartSupport.lineSetChartEventData = function(chart, chartEvent, echartsEventParams)
	{
		var renderOptions= chart.renderOptions();
		var dg = renderOptions.dg;
		var dataSignNames = dg.dataSignNames;
		var categoryPropName = chartSupport.builtinCategoryPropName();
		
		var echartsData = echartsEventParams.data;
		var data = chartSupport.extractNameValueStyleObj(echartsData, dataSignNames.name, dataSignNames.value);
		data[dataSignNames.category] = (echartsData && echartsData[categoryPropName] != null ?
											echartsData[categoryPropName] : undefined);
		
		chart.eventData(chartEvent, data);
		chart.eventOriginalDataIndex(chartEvent, chart.originalDataIndex(echartsData));
	};
	
	//柱状图
	
	chartSupport.barRender = function(chart, options)
	{
		options = $.extend(true,
		{
			dg:
			{
				//name 必选，名称
				//value 必选，当标记category时单选，否则可多选，数值
				//category 可选，类别，不同类别绘制为不同系列
				dataSignNames: { name: "name", value: "value", category: "category" },
				//是否堆叠
				stack: false,
				//是否按数据集分组堆叠
				stackGroup: true,
				//是否横向
				horizontal: false
			}
		},
		options);
		
		var dataSignNames = options.dg.dataSignNames;
		var dataSetBind = chartSupport.dataSetBindMainNonNull(chart);
		var np = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.name);
		var vps = chart.dataSetFieldsOfSign(dataSetBind, dataSignNames.value);
		
		options = chartSupport.inflateRenderOptions(chart,
		{
			title:
			{
		        text: chart.name
		    },
			tooltip:
			{
				trigger: "item"
			},
			legend:
			{
				id: 0,
				//将在update中设置：
				//data
			},
			xAxis:
			{
				id: 0,
				name: chart.dataSetFieldAlias(dataSetBind, np),
				nameGap: 5,
				type: chartSupport.evalDataSetFieldAxisType(chart, np),
				boundaryGap: true
			},
			yAxis:
			{
				id: 0,
				name: (vps.length == 1 ? chart.dataSetFieldAlias(dataSetBind, vps[0]) : ""),
				nameGap: 5,
				type: "value"
			},
			series:
			[
				//将在update中设置：
				//{}
				//设初值以免渲染报错
				{
					id: 0,
					type: "bar"
				}
			]
		},
		options,
		function(options)
		{
			if(options.dg.horizontal)
			{
				var xAxisTmp = options.xAxis;
				options.xAxis = options.yAxis;
				options.yAxis = xAxisTmp;
				
				//横向柱状图的yAxis.type不能为value，不然会变为竖向图形
				if(options.yAxis.type == "value")
					options.yAxis.type = "category";
			}
		});
		
		chart.echartsInit(options);
	};
	
	chartSupport.barUpdate = function(chart, chartResult)
	{
		var renderOptions= chart.renderOptions();
		var dg = renderOptions.dg;
		var dataSignNames = dg.dataSignNames;
		
		var dataSetBinds = chart.dataSetBindsMain();
		
		var legendData = [];
		var series = [];
		
		for(var i=0; i<dataSetBinds.length; i++)
		{
			var dataSetBind = dataSetBinds[i];
			var dataSetAlias = chart.dataSetAlias(dataSetBind);
			var result = chart.resultOf(chartResult, dataSetBind);
			
			var np = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.name);
			var cp = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.category);
			
			if(cp)
			{
				var vp = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.value);
				
				var categoryNames = [];
				var categoryDatasMap = {};
				
				//使用{value: [name,value]}格式可以更好地兼容category、value、time坐标轴类型
				var propertyMap = { "value": [np, vp] };
				propertyMap = chartSupport.inflatePropertyMapWithCategory(propertyMap, cp);
				var data = chart.resultMapObjects(result, propertyMap);
				chart.originalDataIndexes(data, dataSetBind);
				chartSupport.splitDataByCategory(data, categoryNames, categoryDatasMap);
				
				for(var j=0; j<categoryNames.length; j++)
				{
					var categoryName = categoryNames[j];
					var legendName = chartSupport.legendNameForDataCategory(dataSetBinds, dataSetAlias, categoryName);
					var mySeries = {id: series.length, type: "bar", name: legendName, data: categoryDatasMap[categoryName]};
					
					if(dg.horizontal)
					{
						mySeries.encode = { x: 1, y: 0 };
					}
					
					if(dg.stack)
					{
						mySeries.stack = (dg.stackGroup ? dataSetAlias : "stack");
						mySeries.label = { show: true };
					}
					
					legendData.push(legendName);
					series.push(mySeries);
				}
			}
			else
			{
				var vps = chart.dataSetFieldsOfSign(dataSetBind, dataSignNames.value);
				
				for(var j=0; j<vps.length; j++)
				{
					var legendName = chartSupport.legendNameForDataValues(chart, dataSetBinds, dataSetBind, dataSetAlias, vps, j);
					
					//使用{value: [name,value]}格式可以更好地兼容category、value、time坐标轴类型
					var vpsMy = [np, vps[j]];
					var data = chart.resultValueObjects(result, vpsMy);
					
					chart.originalDataIndexes(data, dataSetBind);
					
					var mySeries = {id: series.length, type: "bar", name: legendName, data: data};
					
					if(dg.horizontal)
					{
						mySeries.encode = { x: 1, y: 0 };
					}
					
					if(dg.stack)
					{
						mySeries.stack = (dg.stackGroup ? dataSetAlias : "stack");
						mySeries.label = { show: true };
					}
					
					legendData.push(legendName);
					series.push(mySeries);
				}
			}
		}
		
		var options = { legend: {id: 0, data: legendData}, series: series };
		
		//坐标轴信息也应替换合并，不然图表刷新有数据变化时，坐标不能自动更新
		if(dg.horizontal)
			options.yAxis = { id: 0 };
		else
			options.xAxis = { id: 0 };
		
		chartSupport.inflateAxisDataForEchartsUpdateOptions(renderOptions, options, (dg.horizontal ? options.yAxis : options.xAxis),
						chartSupport.inflateAxisDataExtractors.valueElement(0));
		
		chartSupport.adaptArrayPropsForUpdateOptions(options, renderOptions);
		
		options = chart.inflateUpdateOptions(chartResult, options, function(options)
		{
			chartSupport.adaptValueArrayObjSeriesData(chart, options, "bar");
		});
		
		chartSupport.echartsOptionsReplaceMerge(chart, options);
	};
	
	chartSupport.barResize = function(chart)
	{
		chartSupport.resizeChartEcharts(chart);
	};
	
	chartSupport.barDestroy = function(chart)
	{
		chartSupport.destroyChartEcharts(chart);
	};
	
	chartSupport.barOn = function(chart, eventType, handler)
	{
		chartSupport.bindChartEventHandlerForEcharts(chart, eventType, handler,
				chartSupport.barSetChartEventData);
	};
	
	chartSupport.barOff = function(chart, eventType, handler)
	{
		chart.echartsOffEventHandler(eventType, handler);
	};
	
	chartSupport.barSetChartEventData = function(chart, chartEvent, echartsEventParams)
	{
		var renderOptions= chart.renderOptions();
		var dg = renderOptions.dg;
		var dataSignNames = dg.dataSignNames;
		var categoryPropName = chartSupport.builtinCategoryPropName();
		
		var echartsData = echartsEventParams.data;
		var data = chartSupport.extractNameValueStyleObj(echartsData, dataSignNames.name, dataSignNames.value);
		data[dataSignNames.category] = (echartsData && echartsData[categoryPropName] != null ?
											echartsData[categoryPropName] : undefined);
		
		chart.eventData(chartEvent, data);
		chart.eventOriginalDataIndex(chartEvent, chart.originalDataIndex(echartsData));
	};
	
	//极坐标柱状图
	
	chartSupport.barPolarRender = function(chart, options)
	{
		options = $.extend(true,
		{
			dg:
			{
				//name 必选，名称
				//value 必选，当标记category时单选，否则可多选，数值
				//category 可选，类别，不同类别绘制为不同系列
				dataSignNames: { name: "name", value: "value", category: "category" },
				//是否堆叠
				stack: false,
				//是否按数据集分组堆叠
				stackGroup: true,
				//坐标类型：radius（径向）、angle（角度）
				axisType: "radius",
			}
		},
		options);
		
		options = chartSupport.inflateRenderOptions(chart,
		{
			title:
			{
				text: chart.name
			},
			angleAxis: {id: 0},
			radiusAxis: {id: 0},
			polar:
			{
				id: 0,
				radius: "60%"
			},
			tooltip:
			{
				trigger: "item"
			},
			legend:
			{
				id: 0,
				//将在update中设置：
				//data
			},
			series:
			[
				//将在update中设置：
				//{}
				//设初值以免渲染报错
				{
					id: 0,
					type: "bar",
					coordinateSystem: "polar"
				}
			]
		},
		options,
		function(options)
		{
			var dataSetBind = chartSupport.dataSetBindMainNonNull(chart);
			var dataSignNames = options.dg.dataSignNames;
			var np = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.name);
			
			if(options.dg.axisType == "angle")
			{
				options.angleAxis =
				{
					id: 0,
					type: chartSupport.evalDataSetFieldAxisType(chart, np)
					
					//将在update中设置：
					//data
				};
				
				//角度柱状图的angleAxis.type不能为value和time，不然图形会错乱
				if(options.angleAxis.type == "value" || options.angleAxis.type == "time")
					options.angleAxis.type = "category";
			}
			else
			{
				options.radiusAxis =
				{
					id: 0,
					name: chart.dataSetFieldAlias(dataSetBind, np),
					nameGap: 20,
					type: chartSupport.evalDataSetFieldAxisType(chart, np),
			        z: 10
					
					//将在update中设置：
					//data
				};
				
				//径向柱状图的radiusAxis.type不能为value，不然会变为角度图形
				if(options.radiusAxis.type == "value")
					options.radiusAxis.type = "category";
			}
		});
		
		chart.echartsInit(options);
	};
	
	chartSupport.barPolarUpdate = function(chart, chartResult)
	{
		var renderOptions= chart.renderOptions();
		var dg = renderOptions.dg;
		var dataSignNames = dg.dataSignNames;
		var isAngleAxis = (dg.axisType == "angle");
		
		var dataSetBinds = chart.dataSetBindsMain();
		
		var legendData = [];
		var series = [];
		
		for(var i=0; i<dataSetBinds.length; i++)
		{
			var dataSetBind = dataSetBinds[i];
			var dataSetAlias = chart.dataSetAlias(dataSetBind);
			var result = chart.resultOf(chartResult, dataSetBind);
			
			var np = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.name);
			var cp = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.category);
			
			if(cp)
			{
				var vp = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.value);
				
				var categoryNames = [];
				var categoryDatasMap = {};
				
				//角度图时使用{value: [name,value]}格式的数据会无法显示
				//径向图时使用{value: [name,value]}格式可以更好地兼容category、value、time坐标轴类型
				var propertyMap = (isAngleAxis ? {name: np, value: vp} : {"value": [np, vp]});
				propertyMap = chartSupport.inflatePropertyMapWithCategory(propertyMap, cp);
				var data = chart.resultMapObjects(result, propertyMap);
				chart.originalDataIndexes(data, dataSetBind);
				chartSupport.splitDataByCategory(data, categoryNames, categoryDatasMap);
				
				for(var j=0; j<categoryNames.length; j++)
				{
					var categoryName = categoryNames[j];
					var legendName = chartSupport.legendNameForDataCategory(dataSetBinds, dataSetAlias, categoryName);
					var mySeries = {id: series.length, type: "bar", name: legendName, data: categoryDatasMap[categoryName], coordinateSystem: "polar"};
					
					if(dg.stack)
					{
						mySeries.stack = (dg.stackGroup ? dataSetAlias : "stack");
						mySeries.label = { show: true };
					}
					
					legendData.push(legendName);
					series.push(mySeries);
				}
			}
			else
			{
				var vps = chart.dataSetFieldsOfSign(dataSetBind, dataSignNames.value);
				
				for(var j=0; j<vps.length; j++)
				{
					var legendName = chartSupport.legendNameForDataValues(chart, dataSetBinds, dataSetBind, dataSetAlias, vps, j);
					var data = null;
					
					//角度图时使用{value: [name,value]}格式的数据会无法显示
					if(isAngleAxis)
						data = chart.resultNameValueObjects(result, np, vps[j]);
					//径向图时使用{value: [name,value]}格式可以更好地兼容category、value、time坐标轴类型
					else
						data = chart.resultValueObjects(result, [np, vps[j]]);
					
					chart.originalDataIndexes(data, dataSetBind);
					
					var mySeries = {id: series.length, type: "bar", name: legendName, data: data, coordinateSystem: "polar"};
					
					if(dg.stack)
					{
						mySeries.stack = (dg.stackGroup ? dataSetAlias : "stack");
						mySeries.label = { show: true };
					}
					
					legendData.push(legendName);
					series.push(mySeries);
				}
			}
		}
		
		var options = { legend: {id: 0, data: legendData}, series: series };
		
		//坐标轴信息也应替换合并，不然图表刷新有数据变化时，坐标不能自动更新
		if(isAngleAxis)
			options.angleAxis = { id: 0 };
		else
			options.radiusAxis = { id: 0 };
		
		chartSupport.inflateAxisDataForEchartsUpdateOptions(renderOptions, options, (isAngleAxis ? options.angleAxis : options.radiusAxis),
						{
							get: function(s)
							{
								if(isAngleAxis)
									return chartSupport.inflateAxisDataExtractors.property("name");
								else
									return chartSupport.inflateAxisDataExtractors.valueElement(0);
							}
						});
		
		chartSupport.adaptArrayPropsForUpdateOptions(options, renderOptions);
		options = chart.inflateUpdateOptions(chartResult, options);
		
		chartSupport.echartsOptionsReplaceMerge(chart, options);
	};
	
	chartSupport.barPolarResize = function(chart)
	{
		chartSupport.resizeChartEcharts(chart);
	};
	
	chartSupport.barPolarDestroy = function(chart)
	{
		chartSupport.destroyChartEcharts(chart);
	};
	
	chartSupport.barPolarOn = function(chart, eventType, handler)
	{
		chartSupport.bindChartEventHandlerForEcharts(chart, eventType, handler,
				chartSupport.barPolarSetChartEventData);
	};
	
	chartSupport.barPolarOff = function(chart, eventType, handler)
	{
		chart.echartsOffEventHandler(eventType, handler);
	};
	
	chartSupport.barPolarSetChartEventData = function(chart, chartEvent, echartsEventParams)
	{
		var renderOptions= chart.renderOptions();
		var dg = renderOptions.dg;
		var dataSignNames = dg.dataSignNames;
		var categoryPropName = chartSupport.builtinCategoryPropName();
		
		var echartsData = echartsEventParams.data;
		var data = {};
		
		if(dg.axisType == "angle")
		{
			data[dataSignNames.name] = echartsData.name;
			data[dataSignNames.value] = echartsData.value;
		}
		else
		{
			data[dataSignNames.name] = echartsData.value[0];
			data[dataSignNames.value] = echartsData.value[1];
		}
		
		data[dataSignNames.category] = (echartsData && echartsData[categoryPropName] != null ?
											echartsData[categoryPropName] : undefined);
		
		chart.eventData(chartEvent, data);
		chart.eventOriginalDataIndex(chartEvent, chart.originalDataIndex(echartsData));
	};
	
	//饼图
	
	chartSupport.pieRender = function(chart, options)
	{
		options = $.extend(true,
		{
			dg:
			{
				//name 必选，名称
				//value 必选，数值
				//category 可选，类别，不同类别绘制为不同系列
				dataSignNames: { name: "name", value: "value", category: "category" },
				//是否按数据集分割系列，而非仅一个系列
				splitDataSet: false,
				//当splitDataSet=true时，各系列布局：
				//nest：嵌套；grid：网格
				seriesLayout: "nest",
				//当splitDataSet=false且数据集无category标记时，是否环形图
				ring: false,
				//当splitDataSet=false且数据集无category标记时，是否玫瑰图
				rose: false
			}
		},
		options);
		
		options = chartSupport.inflateRenderOptions(chart,
		{
			title:
			{
		        text: chart.name
		    },
			tooltip:
			{
				trigger: "item",
				formatter: "{a} <br />{b}: {c} ({d}%)"
			},
			legend:
			{
				id: 0,
				//将在update中设置：
				//data
			},
			series:
			[
				//将在update中设置：
				//{}
				//设初值以免渲染报错
				{
					id: 0,
					type: "pie"
				}
			]
		},
		options);
		
		chart.echartsInit(options);
	};
	
	chartSupport.pieUpdate = function(chart, chartResult)
	{
		var renderOptions= chart.renderOptions();
		var dg = renderOptions.dg;
		var dataSignNames = dg.dataSignNames;
		
		var dataSetBinds = chart.dataSetBindsMain();
		
		var series = [];
		
		for(var i=0; i<dataSetBinds.length; i++)
		{
			var dataSetBind = dataSetBinds[i];
			var dataSetAlias = chart.dataSetAlias(dataSetBind);
			var result = chart.resultOf(chartResult, dataSetBind);
			
			var np = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.name);
			var vp = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.value);
			var cp = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.category);
			
			var propertyMap = {"name": np, "value": vp};
			if(cp)
				propertyMap = chartSupport.inflatePropertyMapWithCategory(propertyMap, cp);
			
			var data = chart.resultMapObjects(result, propertyMap);
			chart.originalDataIndexes(data, dataSetBind);
			
			if(cp)
			{
				var categoryNames = [];
				var categoryDatasMap = {};
				chartSupport.splitDataByCategory(data, categoryNames, categoryDatasMap);
				
				for(var j=0; j<categoryNames.length; j++)
				{
					var categoryName = categoryNames[j];
					var legendName = chartSupport.legendNameForDataCategory(dataSetBinds, dataSetAlias, categoryName);
					var mySeries = {id: series.length, type: "pie", name: legendName, data: categoryDatasMap[categoryName]};
					series.push(mySeries);
				}
			}
			else if(dg.splitDataSet)
			{
				series.push({ id: series.length, type: "pie", name: dataSetAlias, data: data});
			}
			else
			{
				if(series.length == 0)
				{
					series.push({ id: series.length, type: "pie", name: dataSetAlias, data: [], radius: "60%" });
					
					if(dg.ring)
						series[0].radius = ["35%", "55%"];
					
					if(dg.rose)
						series[0].roseType = "radius";
				}
				
				series[0].data = series[0].data.concat(data);
			}
		}
		
		var options = { legend: { id: 0 }, series: series };
		
		chartSupport.inflateAxisDataForEchartsUpdateOptions(renderOptions, options, options.legend,
						chartSupport.inflateAxisDataExtractors.property("name"));
		
		chartSupport.pieEvalSeriesLayout(chart, renderOptions, options);
		chartSupport.adaptArrayPropsForUpdateOptions(options, renderOptions);
		options = chart.inflateUpdateOptions(chartResult, options);
		
		chartSupport.echartsOptionsReplaceMerge(chart, options);
	};
	
	chartSupport.pieResize = function(chart)
	{
		chartSupport.resizeChartEcharts(chart);
	};
	
	chartSupport.pieDestroy = function(chart)
	{
		chartSupport.destroyChartEcharts(chart);
	};
	
	chartSupport.pieOn = function(chart, eventType, handler)
	{
		chartSupport.bindChartEventHandlerForEcharts(chart, eventType, handler,
				chartSupport.pieSetChartEventData);
	};
	
	chartSupport.pieOff = function(chart, eventType, handler)
	{
		chart.echartsOffEventHandler(eventType, handler);
	};
	
	chartSupport.pieSetChartEventData = function(chart, chartEvent, echartsEventParams)
	{
		var renderOptions= chart.renderOptions();
		var dg = renderOptions.dg;
		var dataSignNames = dg.dataSignNames;
		
		var echartsData = echartsEventParams.data;
		var data = {};
		
		data[dataSignNames.name] = echartsData.name;
		data[dataSignNames.value] = echartsData.value;
		
		chart.eventData(chartEvent, data);
		chart.eventOriginalDataIndex(chartEvent, chart.originalDataIndex(echartsData));
	};
	
	chartSupport.pieEvalSeriesLayout = function(chart, renderOptions, updateOptions)
	{
		if(!renderOptions.dg.splitDataSet)
			return;
		
		var series = updateOptions.series;
		var len = series.length;
		
		if(!len)
			return;
		
		if(renderOptions.dg.seriesLayout == "nest")
		{
			var radiusMax = 80;
			var radiusInner = 0;
			//系列数=1取60，否则取30
			var radiusOuter = (len == 1 ? 60 : 30);
			var radiusStep = parseInt((radiusMax - radiusOuter)/len);
			var radiusGap = parseInt(radiusStep*4/9);
			radiusStep = radiusStep - radiusGap;
			
			for(var i=0; i<len; i++)
			{
				series[i].radius = [ radiusInner+"%", radiusOuter+"%" ];
				
				//不是最外圈系列标签显示在内部
				if(i < (len - 1))
					series[i].label = { position: "inner" };
				
				radiusInner = radiusOuter + radiusGap;
				radiusOuter = radiusInner + radiusStep;
			}
		}
		else if(renderOptions.dg.seriesLayout == "grid")
		{
			//TODO
		}
	};
	
	//仪表盘
	
	chartSupport.gaugeRender = function(chart, options)
	{
		options = $.extend(true,
		{
			dg:
			{
				//value 必选，可多选，数值
				//min 可选，最小值
				//max 可选，最大值
				dataSignNames: { value: "value", min: "min", max: "max" },
				//仪表盘类型："" 基本；"ring" 得分环；"step" 阶段
				gaugeType: ""
			}
		},
		options);
		
		var builtinOptions =
		{
			title: {
		        text: chart.name
		    },
			tooltip:
			{
				formatter: "{a} <br />{b} : {c}"
			},
			series:
			[
				//将在update中设置：
				//{}
				//设初值以免渲染报错
				{
					id: 0,
					type: "gauge"
				}
			]
		};
		
		if(options.dg.gaugeType == "ring")
		{
			var itemBorderColor = chart.themeGradualColor(0.8);
			var chartEle = chart.elementJquery();
			var axisLineWidth = parseInt(Math.min(chartEle.width(), chartEle.height())/12);
			
			$.extend(builtinOptions.series[0],
			{
				startAngle: 90,
				endAngle: -270,
				pointer:
				{
					show: false
				},
				progress:
				{
					show: true,
					overlap: false,
					roundCap: true,
					clip: false,
					itemStyle:
					{
						borderWidth: 1,
						borderColor: itemBorderColor
					}
				},
				axisLine:
				{
					lineStyle:
					{
						width: axisLineWidth
					}
				},
				splitLine:
				{
					show: false
				},
				axisTick:
				{
					show: false
				},
				axisLabel:
				{
					show: false
				},
				detail:
				{
					borderColor: 'auto',
					borderRadius: 20,
					borderWidth: 1
				}
			});
		}
		else if(options.dg.gaugeType == "step")
		{
			var chartEle = chart.elementJquery();
			var axisLineWidth = parseInt(Math.min(chartEle.width(), chartEle.height())/20);
			
			$.extend(builtinOptions.series[0],
			{
				axisLine:
				{
					lineStyle:
					{
						width: axisLineWidth,
						color:
						[
							[0.2, '#67e0e3'], [0.8, '#37a2da'], [1, '#fd666d']
						]
					}
				},
				pointer:
				{
					itemStyle:
					{
						color: 'auto'
					}
				},
				progress:
				{
					show: false
				},
				axisTick:
				{
					distance: (0-axisLineWidth),
					length: parseInt(axisLineWidth/3),
				},
				splitLine:
				{
					distance: (0-axisLineWidth),
					length: axisLineWidth
				},
				axisLabel:
				{
					color: 'auto',
					distance: axisLineWidth + parseInt(axisLineWidth/3)
				},
				detail:
				{
					valueAnimation: true,
					color: 'auto'
				}
			});
		}
		
		options = chartSupport.inflateRenderOptions(chart, builtinOptions, options);
		
		chart.echartsInit(options);
	};
	
	chartSupport.gaugeUpdate = function(chart, chartResult)
	{
		var renderOptions= chart.renderOptions();
		var dg = renderOptions.dg;
		var dataSignNames = dg.dataSignNames;
		
		var dataSetBinds = chart.dataSetBindsMain();
		
		var seriesName = "";
		var seriesData = [];
		var min = null;
		var max = null;
		
		for(var i=0; i<dataSetBinds.length; i++)
		{
			var dataSetBind = dataSetBinds[i];
			var dataSetAlias = chart.dataSetAlias(dataSetBind);
			var result = chart.resultOf(chartResult, dataSetBind);
			
			if(min == null)
			{
				var minp = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.min);
				if(minp)
				{
					var minpv = chart.resultColumnArrays(result, minp);
					min = chartSupport.findNonNull(minpv);
				}
			}
			
			if(max == null)
			{
				var maxp = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.max);
				if(maxp)
				{
					var maxpv = chart.resultColumnArrays(result, maxp);
					max = chartSupport.findNonNull(maxpv);
				}
			}
			
			var vps = chart.dataSetFieldsOfSign(dataSetBind, dataSignNames.value);
			var vpsvs = chart.resultRowArrays(result, vps);
			
			for(var j=0; j<vpsvs.length; j++)
			{
				var vRow = vpsvs[j];
				
				for(var k=0; k<vRow.length; k++)
				{
					var vpn = chart.dataSetFieldAlias(dataSetBind,vps[k]);
					var data = { name: vpn, value: vRow[k] };
					chart.originalDataIndex(data, dataSetBind, j);
					
					seriesData.push(data);
				}
			}
			
			if(!seriesName)
				seriesName = dataSetAlias;
		}
		
		if(dg.gaugeType == "ring")
			chartSupport.gaugeEvalDataTitlePosition(chart, seriesData, "center", null, 1);
		else
			chartSupport.gaugeEvalDataTitlePosition(chart, seriesData, "top");
		
		min = (min == null ? 0 : min);
		max = (max == null ? 100 : max);
		
		var options = { series : [ { id: 0, type: "gauge", name: seriesName, min: min, max: max, data: seriesData } ] };
		
		chartSupport.adaptArrayPropsForUpdateOptions(options, renderOptions);
		options = chart.inflateUpdateOptions(chartResult, options);
		
		chartSupport.echartsOptionsReplaceMerge(chart, options);
	};

	chartSupport.gaugeResize = function(chart)
	{
		chartSupport.resizeChartEcharts(chart);
	};
	
	chartSupport.gaugeDestroy = function(chart)
	{
		chartSupport.destroyChartEcharts(chart);
	};

	chartSupport.gaugeOn = function(chart, eventType, handler)
	{
		chartSupport.bindChartEventHandlerForEcharts(chart, eventType, handler,
				chartSupport.gaugeSetChartEventData);
	};
	
	chartSupport.gaugeOff = function(chart, eventType, handler)
	{
		chart.echartsOffEventHandler(eventType, handler);
	};
	
	chartSupport.gaugeSetChartEventData = function(chart, chartEvent, echartsEventParams)
	{
		var renderOptions= chart.renderOptions();
		var dg = renderOptions.dg;
		var dataSignNames = dg.dataSignNames;
		
		var echartsData = echartsEventParams.data;
		var data = {};
		
		data[dataSignNames.value] = echartsData.value;
		data[dataSignNames.min] = echartsData.min;
		data[dataSignNames.max] = echartsData.max;
		
		chart.eventData(chartEvent, data);
		chart.eventOriginalDataIndex(chartEvent, chart.originalDataIndex(echartsData));
	};
	
	chartSupport.gaugeEvalDataTitlePosition = function(chart, seriesData, positionType, topPposition, colCount, titleHeight, detailHeight)
	{
		positionType = (positionType == null ? "center" : positionType);
		topPposition = (topPposition == null ? 50 : topPposition);
		if(colCount == null)
		{
			var len = seriesData.length;
			if(len < 3)
				colCount = len;
			else if(len%3 == 0)
				colCount = 3;
			else if(len%2 == 0)
				colCount = 2;
			else
				colCount = 3;
		}
		titleHeight = (titleHeight == null? 14 : titleHeight);
		detailHeight = (detailHeight == null ? 15 : detailHeight);
		
		var rowHeight = titleHeight + detailHeight;
		var rowCount = Math.ceil(seriesData.length/colCount);
		var colCenterIdx = colCount/2;
		var rowCenterIdx = rowCount/2;
		var xGap = 100/colCount;
		
		for(var i=0; i<seriesData.length; i++)
		{
			var row = Math.floor(i/colCount);
			var col = i%colCount;
			
			var x = parseInt((col - colCenterIdx) * xGap + xGap/2);
			var yt = (positionType == "top" ? (topPposition + row*rowHeight) : ((row - rowCenterIdx)*rowHeight));
			var yd = yt + titleHeight;
			
			seriesData[i].title = { offsetCenter: [ x+'%', yt+"%" ] };
			seriesData[i].detail = { offsetCenter: [ x+'%', yd+"%" ] };
		}
	};
	
	//散点图
	
	chartSupport.scatterRender = function(chart, options)
	{
		chartSupport._scatterRender(chart, options, "scatter");
	};
	
	chartSupport.scatterUpdate = function(chart, chartResult)
	{
		chartSupport._scatterUpdate(chart, chartResult);
	};

	chartSupport.scatterResize = function(chart)
	{
		chartSupport._scatterResize(chart);
	};
	
	chartSupport.scatterDestroy = function(chart)
	{
		chartSupport._scatterDestroy(chart);
	};

	chartSupport.scatterOn = function(chart, eventType, handler)
	{
		chartSupport._scatterOn(chart, eventType, handler);
	};
	
	chartSupport.scatterOff = function(chart, eventType, handler)
	{
		chartSupport._scatterOff(chart, eventType, handler);
	};
	
	chartSupport.scatterRippleRender = function(chart, options)
	{
		chartSupport._scatterRender(chart, options, "effectScatter");
	};
	
	chartSupport.scatterRippleUpdate = function(chart, chartResult)
	{
		chartSupport._scatterUpdate(chart, chartResult);
	};

	chartSupport.scatterRippleResize = function(chart)
	{
		chartSupport._scatterResize(chart);
	};
	
	chartSupport.scatterRippleDestroy = function(chart)
	{
		chartSupport._scatterDestroy(chart);
	};

	chartSupport.scatterRippleOn = function(chart, eventType, handler)
	{
		chartSupport._scatterOn(chart, eventType, handler);
	};
	
	chartSupport.scatterRippleOff = function(chart, eventType, handler)
	{
		chartSupport._scatterOff(chart, eventType, handler);
	};
	
	chartSupport._scatterRender = function(chart, options, scatterType)
	{
		options = $.extend(true,
		{
			dg:
			{
				//name 必选，名称
				//value 必选，当标记category时单选，否则可多选，数值
				//category 可选，类别，不同类别绘制为不同系列
				dataSignNames: { name: "name", value: "value", category: "category" },
				//最大数据标记像素数
				symbolSizeMax: undefined,
				//最小数据标记像素数
				symbolSizeMin: undefined,
				//散点图类型："scatter"、"effectScatter"
				scatterType: scatterType
			}
		},
		options);
		
		var dataSignNames = options.dg.dataSignNames;
		var dataSetBind = chartSupport.dataSetBindMainNonNull(chart);
		var np = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.name);
		var vp = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.value);
		
		options = chartSupport.inflateRenderOptions(chart,
		{
			title:
			{
		        text: chart.name
		    },
			tooltip:
			{
				trigger: "item"
			},
			legend:
			{
				id: 0,
				//将在update中设置：
				//data
			},
			xAxis:
			{
				id: 0,
				name: chart.dataSetFieldAlias(dataSetBind, np),
				nameGap: 5,
				type: chartSupport.evalDataSetFieldAxisType(chart, np),
				boundaryGap: !chartSupport.isDataTypeNumber(np)
			},
			yAxis:
			{
				id: 0,
				name: chart.dataSetFieldAlias(dataSetBind, vp),
				nameGap: 5,
				type: "value"
			},
			series:
			[
				//将在update中设置：
				//{}
				//设初值以免渲染报错
				{
					id: 0,
					type: scatterType
				}
			]
		},
		options);
		
		chart.echartsInit(options);
	};
	
	chartSupport._scatterUpdate = function(chart, chartResult)
	{
		var renderOptions= chart.renderOptions();
		var dg = renderOptions.dg;
		var dataSignNames = dg.dataSignNames;
		
		var dataSetBinds = chart.dataSetBindsMain();
		
		var legendData = [];
		var series = [];
		
		var dataRange = { min: null, max: null };
		var symbolSizeMax = chartSupport.evalSymbolSizeMaxForScatter(chart, renderOptions, dg.scatterType);
		var symbolSizeMin = chartSupport.evalSymbolSizeMinForScatter(chart, renderOptions, symbolSizeMax, dg.scatterType);
		
		for(var i=0; i<dataSetBinds.length; i++)
		{
			var dataSetBind = dataSetBinds[i];
			var dataSetAlias = chart.dataSetAlias(dataSetBind);
			var result = chart.resultOf(chartResult, dataSetBind);
			
			var np = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.name);
			var cp = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.category);
			
			if(cp)
			{
				var vp = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.value);
				
				var categoryNames = [];
				var categoryDatasMap = {};
				
				//使用{value: [name,value]}格式可以更好地兼容category、value、time坐标轴类型
				var propertyMap = { "value": [np, vp] };
				propertyMap = chartSupport.inflatePropertyMapWithCategory(propertyMap, cp);
				var data = chart.resultMapObjects(result, propertyMap);
				
				chart.originalDataIndexes(data, dataSetBind);
				chartSupport.evalArrayDataRange(dataRange, data, "value", 1);
				chartSupport.splitDataByCategory(data, categoryNames, categoryDatasMap);
				
				for(var j=0; j<categoryNames.length; j++)
				{
					var categoryName = categoryNames[j];
					var legendName = chartSupport.legendNameForDataCategory(dataSetBinds, dataSetAlias, categoryName);
					var mySeries = {id: series.length, type: dg.scatterType, name: legendName, data: categoryDatasMap[categoryName]};
					
					legendData.push(legendName);
					series.push(mySeries);
				}
			}
			else
			{
				var vps = chart.dataSetFieldsOfSign(dataSetBind, dataSignNames.value);
				
				for(var j=0; j<vps.length; j++)
				{
					var legendName = chartSupport.legendNameForDataValues(chart, dataSetBinds, dataSetBind, dataSetAlias, vps, j);
					//使用{value: [name,value]}格式可以更好地兼容category、value、time坐标轴类型
					var data = chart.resultValueObjects(result, [np, vps[j]]);
					
					chart.originalDataIndexes(data, dataSetBind);
					chartSupport.evalArrayDataRange(dataRange, data, "value", 1);
					
					var mySeries = { id: series.length, type: dg.scatterType, name: legendName, data: data };
					
					legendData.push(legendName);
					series.push(mySeries);
				}
			}
		}
		
		chartSupport.evalSeriesDataValueSymbolSize(series, dataRange.min, dataRange.max, symbolSizeMax, symbolSizeMin, "value", 1);
		
		//坐标轴信息也应替换合并，不然图表刷新有数据变化时，坐标不能自动更新
		var options = { legend: {id: 0, data: legendData}, series: series, xAxis: { id: 0 } };
		
		chartSupport.inflateAxisDataForEchartsUpdateOptions(renderOptions, options, options.xAxis,
						chartSupport.inflateAxisDataExtractors.valueElement(0));
		
		chartSupport.adaptArrayPropsForUpdateOptions(options, renderOptions);
		
		options = chart.inflateUpdateOptions(chartResult, options, function(options)
		{
			chartSupport.adaptValueArrayObjSeriesData(chart, options, "scatter");
		});
		
		chartSupport.echartsOptionsReplaceMerge(chart, options);
	};
	
	chartSupport._scatterResize = function(chart)
	{
		chartSupport.resizeChartEcharts(chart);
	};
	
	chartSupport._scatterDestroy = function(chart)
	{
		chartSupport.destroyChartEcharts(chart);
	};

	chartSupport._scatterOn = function(chart, eventType, handler)
	{
		chartSupport.bindChartEventHandlerForEcharts(chart, eventType, handler,
				chartSupport._scatterSetChartEventData);
	};
	
	chartSupport._scatterOff = function(chart, eventType, handler)
	{
		chart.echartsOffEventHandler(eventType, handler);
	};
	
	chartSupport._scatterSetChartEventData = function(chart, chartEvent, echartsEventParams)
	{
		var renderOptions= chart.renderOptions();
		var dg = renderOptions.dg;
		var dataSignNames = dg.dataSignNames;
		var categoryPropName = chartSupport.builtinCategoryPropName();
		
		var echartsData = echartsEventParams.data;
		var data = chartSupport.extractNameValueStyleObj(echartsData, dataSignNames.name, dataSignNames.value);
		data[dataSignNames.category] = (echartsData && echartsData[categoryPropName] != null ?
											echartsData[categoryPropName] : undefined);
		
		chart.eventData(chartEvent, data);
		chart.eventOriginalDataIndex(chartEvent, chart.originalDataIndex(echartsData));
	};
	
	//坐标散点图
	
	chartSupport.scatterCoordRender = function(chart, options)
	{
		chartSupport._scatterCoordRender(chart, options, "scatter");
	};
	
	chartSupport.scatterCoordUpdate = function(chart, chartResult)
	{
		chartSupport._scatterCoordUpdate(chart, chartResult);
	};
	
	chartSupport.scatterCoordResize = function(chart)
	{
		chartSupport._scatterCoordResize(chart);
	};
	
	chartSupport.scatterCoordDestroy = function(chart)
	{
		chartSupport._scatterCoordDestroy(chart);
	};
	
	chartSupport.scatterCoordOn = function(chart, eventType, handler)
	{
		chartSupport._scatterCoordOn(chart, eventType, handler);
	};
	
	chartSupport.scatterCoordOff = function(chart, eventType, handler)
	{
		chartSupport._scatterCoordOff(chart, eventType, handler);
	};
	
	chartSupport.scatterCoordRippleRender = function(chart, options)
	{
		chartSupport._scatterCoordRender(chart, options, "effectScatter");
	};
	
	chartSupport.scatterCoordRippleUpdate = function(chart, chartResult)
	{
		chartSupport._scatterCoordUpdate(chart, chartResult);
	};
	
	chartSupport.scatterCoordRippleResize = function(chart)
	{
		chartSupport._scatterCoordResize(chart);
	};
	
	chartSupport.scatterCoordRippleDestroy = function(chart)
	{
		chartSupport._scatterCoordDestroy(chart);
	};
	
	chartSupport.scatterCoordRippleOn = function(chart, eventType, handler)
	{
		chartSupport._scatterCoordOn(chart, eventType, handler);
	};
	
	chartSupport.scatterCoordRippleOff = function(chart, eventType, handler)
	{
		chartSupport._scatterCoordOff(chart, eventType, handler);
	};
	
	chartSupport._scatterCoordRender = function(chart, options, scatterType)
	{
		options = $.extend(true,
		{
			dg:
			{
				//name 必选，名称
				//value 必选，数值
				//weight 可选，散点尺寸
				//category 可选，类别，不同类别绘制为不同系列
				dataSignNames: { name: "name", value: "value", weight: "weight", category: "category" },
				//最大数据标记像素数
				symbolSizeMax: undefined,
				//最小数据标记像素数
				symbolSizeMin: undefined,
				//散点图类型："scatter"、"effectScatter"
				scatterType: scatterType
			}
		},
		options);
		
		var dataSignNames = options.dg.dataSignNames;
		var dataSetBind = chartSupport.dataSetBindMainNonNull(chart);
		var np = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.name);
		var vp = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.value);
		
		options = chartSupport.inflateRenderOptions(chart,
		{
			title: {
		        text: chart.name
		    },
			tooltip:
			{
				trigger: "item"
			},
			legend:
			{
				id: 0,
				//将在update中设置：
				//data
			},
			xAxis:
			{
				id: 0,
				name: chart.dataSetFieldAlias(dataSetBind, np),
				nameGap: 5,
				type: chartSupport.evalDataSetFieldAxisType(chart, np),
				boundaryGap: !chartSupport.isDataTypeNumber(np)
			},
			yAxis:
			{
				id: 0,
				name: chart.dataSetFieldAlias(dataSetBind, vp),
				nameGap: 5,
				type: "value"
			},
			series:
			[
				//将在update中设置：
				//{}
				//设初值以免渲染报错
				{
					id: 0,
					type: scatterType
				}
			]
		},
		options);
		
		chart.echartsInit(options);
	};
	
	chartSupport._scatterCoordUpdate = function(chart, chartResult)
	{
		var renderOptions= chart.renderOptions();
		var dg = renderOptions.dg;
		var dataSignNames = dg.dataSignNames;
		
		var dataSetBinds = chart.dataSetBindsMain();
		
		var legendData = [];
		var series = [];
		
		var dataRange = { min: null, max: null };
		var symbolSizeMax = chartSupport.evalSymbolSizeMaxForScatter(chart, renderOptions, dg.scatterType);
		var symbolSizeMin = chartSupport.evalSymbolSizeMinForScatter(chart, renderOptions, symbolSizeMax, dg.scatterType);
		
		for(var i=0; i<dataSetBinds.length; i++)
		{
			var dataSetBind = dataSetBinds[i];
			var dataSetAlias = chart.dataSetAlias(dataSetBind);
			var result = chart.resultOf(chartResult, dataSetBind);
			
			var np = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.name);
			var vp = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.value);
			var wp = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.weight);
			var cp = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.category);
			var propertyMap = { "value": (wp ? [np, vp, wp] : [np, vp]) };
			
			if(cp)
				propertyMap = chartSupport.inflatePropertyMapWithCategory(propertyMap, cp);
			
			var data = chart.resultMapObjects(result, propertyMap);
			chart.originalDataIndexes(data, dataSetBind);
			if(wp)
				chartSupport.evalArrayDataRange(dataRange, data, "value", 2);
			
			if(cp)
			{
				var categoryNames = [];
				var categoryDatasMap = {};
				
				chartSupport.splitDataByCategory(data, categoryNames, categoryDatasMap);
				
				for(var j=0; j<categoryNames.length; j++)
				{
					var categoryName = categoryNames[j];
					var legendName = chartSupport.legendNameForDataCategory(dataSetBinds, dataSetAlias, categoryName);
					var mySeries = {id: series.length, type: dg.scatterType, name: legendName, data: categoryDatasMap[categoryName]};
					
					legendData.push(legendName);
					series.push(mySeries);
				}
			}
			else
			{
				legendData.push(dataSetAlias);
				series.push({ id: series.length, type: dg.scatterType, name: dataSetAlias, data: data });
			}
		}
		
		chartSupport.evalSeriesDataValueSymbolSize(series, dataRange.min, dataRange.max, symbolSizeMax, symbolSizeMin, "value", 2);
		
		//坐标轴信息也应替换合并，不然图表刷新有数据变化时，坐标不能自动更新
		var options = { legend: {id: 0, data: legendData}, series: series, xAxis: { id: 0 } };
		
		chartSupport.inflateAxisDataForEchartsUpdateOptions(renderOptions, options, options.xAxis,
						chartSupport.inflateAxisDataExtractors.valueElement(0));
		
		chartSupport.adaptArrayPropsForUpdateOptions(options, renderOptions);
		options = chart.inflateUpdateOptions(chartResult, options);
		
		chartSupport.echartsOptionsReplaceMerge(chart, options);
	};
	
	chartSupport._scatterCoordResize = function(chart)
	{
		chartSupport.resizeChartEcharts(chart);
	};
	
	chartSupport._scatterCoordDestroy = function(chart)
	{
		chartSupport.destroyChartEcharts(chart);
	};
	
	chartSupport._scatterCoordOn = function(chart, eventType, handler)
	{
		chartSupport.bindChartEventHandlerForEcharts(chart, eventType, handler,
				chartSupport._scatterCoordSetChartEventData);
	};
	
	chartSupport._scatterCoordOff = function(chart, eventType, handler)
	{
		chart.echartsOffEventHandler(eventType, handler);
	};
	
	chartSupport._scatterCoordSetChartEventData = function(chart, chartEvent, echartsEventParams)
	{
		var renderOptions= chart.renderOptions();
		var dg = renderOptions.dg;
		var dataSignNames = dg.dataSignNames;
		var categoryPropName = chartSupport.builtinCategoryPropName();
		
		var echartsData = echartsEventParams.data;
		var data = {};
		
		data[dataSignNames.name] = echartsData.value[0];
		data[dataSignNames.value] = echartsData.value[1];
		if(echartsData.value.length > 2)
			data[dataSignNames.weight] = echartsData.value[2];
		data[dataSignNames.category] = (echartsData && echartsData[categoryPropName] != null ?
											echartsData[categoryPropName] : undefined);
		
		chart.eventData(chartEvent, data);
		chart.eventOriginalDataIndex(chartEvent, chart.originalDataIndex(echartsData));
	};
	
	//雷达图
	
	chartSupport.radarRender = function(chart, options)
	{
		options = $.extend(true,
		{
			dg:
			{
				//item 可选，行式雷达网数据条目
				//name 名称
				//value 数值
				//max 最大值
				dataSignNames: { item: "item", name: "name", value: "value", max: "max" }
			}
		},
		options);
		
		options = chartSupport.inflateRenderOptions(chart,
		{
			title:
			{
		        text: chart.name
		    },
			tooltip:
			{
				trigger: "item"
			},
			legend:
			{
				id: 0,
				//将在update中设置：
				//data
			},
			radar:
			{
				id: 0,
				center: ["50%", "60%"],
				radius: "70%",
				nameGap: 6,
				//将在update中设置：
				//indicator
			},
			series:
			[
				//将在update中设置：
				//{}
				//设初值以免渲染报错
				{
					id: 0,
					type: "radar"
				}
			]
		},
		options);
		
		chart.echartsInit(options);
	};
	
	chartSupport.radarUpdate = function(chart, chartResult)
	{
		var renderOptions= chart.renderOptions();
		var dg = renderOptions.dg;
		var dataSignNames = dg.dataSignNames;
		
		var dataSetBinds = chart.dataSetBindsMain();
		
		var legendData = [];
		var indicatorData = [];
		var seriesData = [];
		
		//临时series，series[i]表示一条雷达网，series[i].name是雷达网名称，
		//series[i].data[i].name是雷达指标名、series[i].data[i].value雷达指标值
		//这样可以使用已有的排序逻辑，从而支持sortAxisData特性
		var tmpSeries = [];
		
		for(var i=0; i<dataSetBinds.length; i++)
		{
			var dataSetBind = dataSetBinds[i];
			var result = chart.resultOf(chartResult, dataSetBind);
			
			var ip = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.item);
			
			//行式雷达网数据，必设置【雷达网条目名称】标记
			//一行数据表示一条雷达网，行式结构为：雷达网条目名称, [指标名, 指标值, 指标上限值]*n
			//或者
			//相同条目名的多行数据表示一条雷达网，行式结构为：雷达网条目名称, [指标名, 指标值, 指标上限值]*1
			if(ip)
			{
				chartSupport.radarUpdateTmpSeriesForRowMode(chart, chartResult, renderOptions,
						dataSetBind, result, indicatorData, tmpSeries)
			}
			//列式雷达网数据
			//一列【指标值】数据表示一条雷达网，列式结构为：指标名, 指标上限值, [指标值]*n，其中【指标值】列名将作为雷达网条目名称
			else
			{
				chartSupport.radarUpdateTmpSeriesForColumnMode(chart, chartResult, renderOptions,
						dataSetBind, result, indicatorData, tmpSeries)
			}
		}
		
		if(chartSupport.sortAxisDataOption(renderOptions))
		{
			var tmpAxisData = [];
			$.each(indicatorData, function(i, indicator)
			{
				tmpAxisData.push(indicator.name);
			});
			
			var tmpOptions = { tmpAxis: { data: tmpAxisData }, series: tmpSeries };
			
			chartSupport.sortAxisDataForUpdateOptions(renderOptions, tmpOptions, tmpOptions.tmpAxis,
							true, true, chartSupport.inflateAxisDataExtractors.property("name"));
			
			indicatorData.sort(function(a, b)
			{
				var ia = chartSupport.findInArray(tmpAxisData, a.name);
				var ib = chartSupport.findInArray(tmpAxisData, b.name);
				
				return (ia - ib);
			});
		}
		
		//将上述tmpSeries转换为雷达网数据
		for(var i=0; i<tmpSeries.length; i++)
		{
			var ts = tmpSeries[i];
			var radarData = { name: ts.name, value: [] };
			
			$.each(indicatorData, function(j, indicator)
			{
				var idx = chartSupport.findInArray(ts.data, indicator.name, "name");
				radarData.value.push(idx > -1 ? ts.data[idx].value : null);
			});
			
			chart.originalDataIndex(radarData, ts.dataSetBindIndex, ts.resultDataIndex);
			seriesData.push(radarData);
			legendData.push(ts.name);
		}
		
		var series = [ { id: 0, type: "radar", data: seriesData } ];
		var options = { legend: {id: 0, data: legendData}, radar: {id: 0, indicator: indicatorData}, series: series };
		
		chartSupport.adaptArrayPropsForUpdateOptions(options, renderOptions);
		options = chart.inflateUpdateOptions(chartResult, options);
		
		chartSupport.echartsOptionsReplaceMerge(chart, options);
		
		chartFactory.extValueBuiltin(chart, "radarIndicatorData", indicatorData);
	};
	
	//行式雷达网数据处理
	chartSupport.radarUpdateTmpSeriesForRowMode = function(chart, chartResult, renderOptions,
			dataSetBind, result, indicatorData, series)
	{
		var dg = renderOptions.dg;
		var dataSignNames = dg.dataSignNames;
		
		var ip = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.item);
		var np = chart.dataSetFieldsOfSign(dataSetBind, dataSignNames.name);
		var mp = chart.dataSetFieldsOfSign(dataSetBind, dataSignNames.max);
		var indicatorLen = Math.min(np.length, mp.length);
		
		for(var i=0; i<indicatorLen; i++)
		{
			var indicators = chart.resultMapObjects(result, { name: np[i], max: mp[i] });
			$.each(indicators, function(j, indicator)
			{
				chartSupport.radarAppendValidIndicator(indicatorData, indicator);
			});
		}
		
		if(indicatorLen == 0){}
		//多行式雷达网
		else if(indicatorLen == 1)
		{
			var vp = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.value);
			
			var categoryNames = [];
			var categoryDatasMap = {};
			var propertyMap = chartSupport.inflatePropertyMapWithCategory({ name: np, value: vp }, ip);
			var data = chart.resultMapObjects(result, propertyMap);
			chart.originalDataIndexes(data, dataSetBind);
			chartSupport.splitDataByCategory(data, categoryNames, categoryDatasMap);
			
			for(var j=0; j<categoryNames.length; j++)
			{
				var categoryName = categoryNames[j];
				var categoryDatas = categoryDatasMap[categoryName];
				var mySeries = { name: categoryName, data: categoryDatas, dataSetBindIndex: dataSetBind.index, resultDataIndex: [] };
				
				$.each(categoryDatas, function(k, cd)
				{
					var odIdx = chart.originalDataIndex(cd);
					mySeries.resultDataIndex.push(odIdx.resultDataIndex);
				});
				
				series.push(mySeries);
			}
		}
		//单行式雷达网
		else
		{
			var iv = chart.resultColumnArrays(result, ip);
			var nv = chart.resultRowArrays(result, np);
			var vp = chart.dataSetFieldsOfSign(dataSetBind, dataSignNames.value);
			var vv = chart.resultRowArrays(result, vp);
			var dataLen = Math.min(np.length, vp.length);
			
			for(var i=0; i<iv.length; i++)
			{
				var mySeries = { name: iv[i], data: [], dataSetBindIndex: dataSetBind.index, resultDataIndex: i };
				
				for(var j=0; j<dataLen; j++)
				{
					mySeries.data.push({ name: nv[i][j], value: vv[i][j] });
				}
				
				series.push(mySeries);
			}
		}
	};
	
	chartSupport.radarUpdateTmpSeriesForColumnMode = function(chart, chartResult, renderOptions,
			dataSetBind, result, indicatorData, series)
	{
		var dg = renderOptions.dg;
		var dataSignNames = dg.dataSignNames;
		
		var np = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.name);
		var nv = chart.resultColumnArrays(result, np);
		var mp = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.max);
		var mv = chart.resultColumnArrays(result, mp);
		var indicatorLen = Math.min(nv.length, mv.length);
		
		for(var i=0; i<indicatorLen; i++)
		{
			var indicator = {name: nv[i], max: mv[i]};
			chartSupport.radarAppendValidIndicator(indicatorData, indicator);
		}
		
		var vp = chart.dataSetFieldsOfSign(dataSetBind, dataSignNames.value);
		var vv = chart.resultColumnArrays(result, vp);
		
		var resultDataIndex = [];
		for(var i=0; i<indicatorData.length; i++)
			resultDataIndex[i] = i;
		
		for(var i=0; i<vp.length; i++)
		{
			var name = chart.dataSetFieldAlias(dataSetBind, vp[i]);
			var mySeries = { name: name, data: [], dataSetBindIndex: dataSetBind.index, resultDataIndex: resultDataIndex };
			
			for(var j=0; j<nv.length; j++)
			{
				mySeries.data.push({ name: nv[j], value: vv[i][j] });
			}
			
			series.push(mySeries);
		}
	};
	
	chartSupport.radarAppendValidIndicator = function(indicatorData, indicator)
	{
		if(indicator && indicator.name != null)
		{
			var idx = chartSupport.findInArray(indicatorData, indicator.name, "name");
			
			if(idx < 0)
			{
				indicatorData.push(indicator);
			}
			else if(indicatorData[idx].max == null || indicatorData[idx].max < indicator.max)
			{
				indicatorData[idx] = indicator;
			}
		}
	};
	
	chartSupport.radarResize = function(chart)
	{
		chartSupport.resizeChartEcharts(chart);
	};
	
	chartSupport.radarDestroy = function(chart)
	{
		chartSupport.destroyChartEcharts(chart);
	};
	
	chartSupport.radarOn = function(chart, eventType, handler)
	{
		chartSupport.bindChartEventHandlerForEcharts(chart, eventType, handler,
				chartSupport.radarSetChartEventData);
	};
	
	chartSupport.radarOff = function(chart, eventType, handler)
	{
		chart.echartsOffEventHandler(eventType, handler);
	};
	
	chartSupport.radarSetChartEventData = function(chart, chartEvent, echartsEventParams)
	{
		var renderOptions= chart.renderOptions();
		var dg = renderOptions.dg;
		var dataSignNames = dg.dataSignNames;
		
		var echartsData = echartsEventParams.data;
		var data = {};
		
		data[dataSignNames.item] = echartsData.name;
		data[dataSignNames.value] = echartsData.value;
		
		var indicatorData = chartFactory.extValueBuiltin(chart, "radarIndicatorData");
		var names = [];
		var maxes = [];
		for(var i=0; i<indicatorData.length; i++)
		{
			names[i] = indicatorData[i].name;
			maxes[i] = indicatorData[i].max;
		}
		
		data[dataSignNames.name] = names;
		data[dataSignNames.max] = maxes;
		
		chart.eventData(chartEvent, data);
		chart.eventOriginalDataIndex(chartEvent, chart.originalDataIndex(echartsData));
	};
	
	//漏斗图
	
	chartSupport.funnelRender = function(chart, options)
	{
		options = $.extend(true,
		{
			dg:
			{
				//name 名称
				//value 数值
				dataSignNames: { name: "name", value: "value" },
				//同series[i].sort
				sort: "descending",
			}
		},
		options);
		
		options = chartSupport.inflateRenderOptions(chart,
		{
			title:
			{
		        text: chart.name
		    },
		    tooltip:
		    {
		        trigger: "item",
		        formatter: "{a} <br />{b} : {c}"
		    },
			legend:
			{
				id: 0,
				//将在update中设置：
				//data
			},
			series:
			[
				//将在update中设置：
				//{}
				//设初值以免渲染报错
				{
					id: 0,
		            type: "funnel"
		        }
			]
		},
		options);
		
		chart.echartsInit(options);
	};
	
	chartSupport.funnelUpdate = function(chart, chartResult)
	{
		var renderOptions= chart.renderOptions();
		var dg = renderOptions.dg;
		var dataSignNames = dg.dataSignNames;
		
		var dataSetBinds = chart.dataSetBindsMain();
		
		var legendData = [];
		var seriesName = "";
		var seriesData = [];
		var dataRange = { min: 0, max: 100 };
		
		for(var i=0; i<dataSetBinds.length; i++)
		{
			var dataSetBind = dataSetBinds[i];
			var dataSetAlias = chart.dataSetAlias(dataSetBind);
			var result = chart.resultOf(chartResult, dataSetBind);
			
			var np = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.name);
			var npv = chart.resultColumnArrays(result, np);
			var vp = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.value);
			var data = chart.resultNameValueObjects(result, np, vp);
			
			chart.originalDataIndexes(data, dataSetBind);
			
			legendData = legendData.concat(npv);
			if(!seriesName)
				seriesName = dataSetAlias;
			seriesData = seriesData.concat(data);
		}
		
		chartSupport.evalArrayDataRange(dataRange, seriesData, "value");
		
		var series = [ {id: 0, type: "funnel", name: seriesName, min: dataRange.min, max: dataRange.max, data: seriesData, sort: dg.sort } ];
		var options = { legend: { id: 0, data: legendData }, series: series };
		
		chartSupport.adaptArrayPropsForUpdateOptions(options, renderOptions);
		options = chart.inflateUpdateOptions(chartResult, options);
		
		chartSupport.echartsOptionsReplaceMerge(chart, options);
	};

	chartSupport.funnelResize = function(chart)
	{
		chartSupport.resizeChartEcharts(chart);
	};
	
	chartSupport.funnelDestroy = function(chart)
	{
		chartSupport.destroyChartEcharts(chart);
	};

	chartSupport.funnelOn = function(chart, eventType, handler)
	{
		chartSupport.bindChartEventHandlerForEcharts(chart, eventType, handler,
				chartSupport.funnelSetChartEventData);
	};
	
	chartSupport.funnelOff = function(chart, eventType, handler)
	{
		chart.echartsOffEventHandler(eventType, handler);
	};
	
	chartSupport.funnelSetChartEventData = function(chart, chartEvent, echartsEventParams)
	{
		var renderOptions= chart.renderOptions();
		var dg = renderOptions.dg;
		var dataSignNames = dg.dataSignNames;
		
		var echartsData = echartsEventParams.data;
		var data = {};
		
		data[dataSignNames.name] = echartsData.name;
		data[dataSignNames.value] = echartsData.value;
		
		chart.eventData(chartEvent, data);
		chart.eventOriginalDataIndex(chartEvent, chart.originalDataIndex(echartsData));
	};
	
	//地图
	
	chartSupport.mapRender = function(chart, options)
	{
		options = $.extend(true,
		{
			dg:
			{
				//name 名称
				//value 数值
				//map 可选，地图名
				dataSignNames: { name: "name", value: "value", map: "map" }
			}
		},
		options);
		
		options = chartSupport.inflateRenderOptions(chart,
		{
			title:
			{
		        text: chart.name
		    },
			tooltip:
			{
				trigger: "item",
				formatter: "{b}<br />{c}"
			},
			visualMap:
			{
				id: 0,
				text: ["高", "低"],
				realtime: true,
				calculable: true,
				
				//将在update中设置：
				//min: 0,
				//max: 100
			},
			series:
			[
				//将在update中设置：
				//{}
				//设初值以免渲染报错
				{
					id: 0,
		            type: "map"
					//将在下面和update中设置：
					//map
		        }
			]
		},
		options,
		null,
		function(renderOptions, chart)
		{
			chartSupport.echartsMapChartInitMap(chart, renderOptions);
		});
		
		chartSupport.echartsMapChartRender(chart, options);
	};
	
	chartSupport.mapUpdate = function(chart, chartResult)
	{
		var renderOptions= chart.renderOptions();
		var dg = renderOptions.dg;
		var dataSignNames = dg.dataSignNames;
		
		var dataSetBinds = chart.dataSetBindsMain();
		
		var seriesName = "";
		var seriesData = [];
		var dataRange = { min: undefined, max: undefined };
		var map = undefined;
		
		for(var i=0; i<dataSetBinds.length; i++)
		{
			var dataSetBind = dataSetBinds[i];
			var dataSetAlias = chart.dataSetAlias(dataSetBind);
			var result = chart.resultOf(chartResult, dataSetBind);
			
			//取任一不为空的地图名列值
			if(!map)
				map = chartSupport.resultFirstNonEmptyValueOfSign(chart, dataSetBind, result, dataSignNames.map);
			
			var np = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.name);
			var vp = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.value);
			var data = chart.resultNameValueObjects(result, np, vp);
			
			chart.originalDataIndexes(data, dataSetBind);
			chartSupport.evalArrayDataRange(dataRange, data, "value");
			
			if(!seriesName)
				seriesName = dataSetAlias;
			
			seriesData = seriesData.concat(data);
		}
		
		var options =
		{
			visualMap: {id: 0, min: dataRange.min, max: dataRange.max},
			series: [ {id: 0, type: "map", name: seriesName, data: seriesData } ]
		};
		
		chartSupport.trimNumberRange(options.visualMap);
		
		if(map)
			options.series[0].map = map;
		
		chartSupport.echartsMapChartUpdate(chart, chartResult, options, renderOptions);
	};
	
	chartSupport.mapResize = function(chart)
	{
		chartSupport.resizeChartEcharts(chart);
	};
	
	chartSupport.mapDestroy = function(chart)
	{
		chartSupport.destroyChartEcharts(chart);
	};

	chartSupport.mapOn = function(chart, eventType, handler)
	{
		chartSupport.bindChartEventHandlerForEcharts(chart, eventType, handler,
				chartSupport.mapSetChartEventData);
	};
	
	chartSupport.mapOff = function(chart, eventType, handler)
	{
		chart.echartsOffEventHandler(eventType, handler);
	};
	
	chartSupport.mapSetChartEventData = function(chart, chartEvent, echartsEventParams)
	{
		var renderOptions= chart.renderOptions();
		var dg = renderOptions.dg;
		var dataSignNames = dg.dataSignNames;
		
		var echartsData = echartsEventParams.data;
		
		//当指定地区没有设置数据时，echartsData为null
		if(!echartsData)
			echartsData = { name: echartsEventParams.name, value: null } ;
		
		var data = {};
		
		data[dataSignNames.name] = echartsData.name;
		data[dataSignNames.value] = echartsData.value;
		
		chart.eventData(chartEvent, data);
		chart.eventOriginalDataIndex(chartEvent, chart.originalDataIndex(echartsData));
	};
	
	//地图散点图
	
	chartSupport.mapScatterRender = function(chart, options)
	{
		chartSupport._mapScatterRender(chart, options, "scatter");
	};
	
	chartSupport.mapScatterUpdate = function(chart, chartResult)
	{
		chartSupport._mapScatterUpdate(chart, chartResult);
	};
	
	chartSupport.mapScatterResize = function(chart)
	{
		chartSupport._mapScatterResize(chart);
	};
	
	chartSupport.mapScatterDestroy = function(chart)
	{
		chartSupport._mapScatterDestroy(chart);
	};
	
	chartSupport.mapScatterOn = function(chart, eventType, handler)
	{
		chartSupport._mapScatterOn(chart, eventType, handler);
	};
	
	chartSupport.mapScatterOff = function(chart, eventType, handler)
	{
		chartSupport._mapScatterOff(chart, eventType, handler);
	};
	
	chartSupport.mapScatterRippleRender = function(chart, options)
	{
		chartSupport._mapScatterRender(chart, options, "effectScatter");
	};
	
	chartSupport.mapScatterRippleUpdate = function(chart, chartResult)
	{
		chartSupport._mapScatterUpdate(chart, chartResult);
	};
	
	chartSupport.mapScatterRippleResize = function(chart)
	{
		chartSupport._mapScatterResize(chart);
	};
	
	chartSupport.mapScatterRippleDestroy = function(chart)
	{
		chartSupport._mapScatterDestroy(chart);
	};
	
	chartSupport.mapScatterRippleOn = function(chart, eventType, handler)
	{
		chartSupport._mapScatterOn(chart, eventType, handler);
	};
	
	chartSupport.mapScatterRippleOff = function(chart, eventType, handler)
	{
		chartSupport._mapScatterOff(chart, eventType, handler);
	};
	
	chartSupport._mapScatterRender = function(chart, options, scatterType)
	{
		options = $.extend(true,
		{
			dg:
			{
				//name 名称
				//longitude 经度
				//latitude 维度
				//value 可选，数值
				//category 可选，类别，不同类别绘制为不同系列
				//map 可选，地图名
				dataSignNames:
				{
					name: "name", longitude: "longitude", latitude: "latitude", value: "value",
					category: "category", map: "map"
				},
				//最大数据标记像素数
				symbolSizeMax: undefined,
				//最小数据标记像素数
				symbolSizeMin: undefined,
				//散点图类型："scatter"、"effectScatter"
				scatterType: scatterType
			}
		},
		options);
		
		options = chartSupport.inflateRenderOptions(chart,
		{
			title:
			{
		        text: chart.name
		    },
			tooltip:
			{
				trigger: "item",
				formatter: function (params)
				{
					var fre = params.name;
					if(params.value && params.value[2] != null)
						fre +=  "<br />" + params.value[2];
					return fre;
				}
			},
			legend:
			{
				id: 0,
				//将在update中设置：
				//data
			},
			geo:
			{
				id: 0,
				roam: true
				//将在下面和update中设置：
				//map
			},
			series:
			[
				//将在update中设置：
				//{}
				//设初值以免渲染报错
				{
					id: 0,
					type: scatterType,
					coordinateSystem: "geo",
					geoIndex: 0
				}
			]
		},
		options,
		null,
		function(renderOptions, chart)
		{
			chartSupport.echartsMapChartInitMap(chart, renderOptions);
		});
		
		chartSupport.echartsMapChartRender(chart, options);
	};
	
	chartSupport._mapScatterUpdate = function(chart, chartResult)
	{
		var renderOptions= chart.renderOptions();
		var dg = renderOptions.dg;
		var dataSignNames = dg.dataSignNames;
		
		var dataSetBinds = chart.dataSetBindsMain();
		
		var legendData = [];
		var series = [];
		var map = undefined;
		
		var dataRange = { min: null, max: null };
		var symbolSizeMax = chartSupport.evalSymbolSizeMaxForScatter(chart, renderOptions, dg.scatterType);
		var symbolSizeMin = chartSupport.evalSymbolSizeMinForScatter(chart, renderOptions, symbolSizeMax, dg.scatterType);
		
		for(var i=0; i<dataSetBinds.length; i++)
		{
			var dataSetBind = dataSetBinds[i];
			var dataSetAlias = chart.dataSetAlias(dataSetBind);
			var result = chart.resultOf(chartResult, dataSetBind);
			
			//取任一不为空的地图名列值
			if(!map)
				map = chartSupport.resultFirstNonEmptyValueOfSign(chart, dataSetBind, result, dataSignNames.map);
			
			var np = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.name);
			var lop = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.longitude);
			var lap = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.latitude);
			var vp = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.value);
			var cp = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.category);
			var propertyMap = { "name": np, "value": (vp ? [lop, lap, vp] : [lop, lap]) };
			
			if(cp)
				propertyMap = chartSupport.inflatePropertyMapWithCategory(propertyMap, cp);
			
			var data = chart.resultMapObjects(result, propertyMap);
			chart.originalDataIndexes(data, dataSetBind);
			
			if(vp)
				chartSupport.evalArrayDataRange(dataRange, data, "value", 2);
			
			if(cp)
			{
				var categoryNames = [];
				var categoryDatasMap = {};
				
				chartSupport.splitDataByCategory(data, categoryNames, categoryDatasMap);
				
				for(var j=0; j<categoryNames.length; j++)
				{
					var categoryName = categoryNames[j];
					var legendName = chartSupport.legendNameForDataCategory(dataSetBinds, dataSetAlias, categoryName);
					var mySeries = {id: series.length, type: dg.scatterType, name: legendName,
									data: categoryDatasMap[categoryName], coordinateSystem: "geo"};
					
					legendData.push(legendName);
					series.push(mySeries);
				}
			}
			else
			{
				legendData.push(dataSetAlias);
				series.push({ id: series.length, type: dg.scatterType, name: dataSetAlias, data: data, coordinateSystem: "geo" });
			}
		}
		
		chartSupport.evalSeriesDataValueSymbolSize(series, dataRange.min, dataRange.max, symbolSizeMax, symbolSizeMin, "value", 2);
		
		var options = { legend: {id: 0, data: legendData}, series: series };
		
		if(map)
		{
			options.geo = { id: 0, map: map };
		}
		
		chartSupport.echartsMapChartUpdate(chart, chartResult, options, renderOptions);
	};
	
	chartSupport._mapScatterResize = function(chart)
	{
		chartSupport.resizeChartEcharts(chart);
	};
	
	chartSupport._mapScatterDestroy = function(chart)
	{
		chartSupport.destroyChartEcharts(chart);
	};

	chartSupport._mapScatterOn = function(chart, eventType, handler)
	{
		chartSupport.bindChartEventHandlerForEcharts(chart, eventType, handler,
				chartSupport._mapScatterSetChartEventData);
	};
	
	chartSupport._mapScatterOff = function(chart, eventType, handler)
	{
		chart.echartsOffEventHandler(eventType, handler);
	};
	
	chartSupport._mapScatterSetChartEventData = function(chart, chartEvent, echartsEventParams)
	{
		var renderOptions= chart.renderOptions();
		var dg = renderOptions.dg;
		var dataSignNames = dg.dataSignNames;
		var categoryPropName = chartSupport.builtinCategoryPropName();
		
		var echartsData = echartsEventParams.data;
		
		var data = {};
		
		data[dataSignNames.name] = echartsData.name;
		data[dataSignNames.longitude] = echartsData.value[0];
		data[dataSignNames.latitude] = echartsData.value[1];
		if(echartsData.value.length > 2)
			data[dataSignNames.value] = echartsData.value[2];
		data[dataSignNames.category] = (echartsData && echartsData[categoryPropName] != null ?
											echartsData[categoryPropName] : undefined);
		
		chart.eventData(chartEvent, data);
		chart.eventOriginalDataIndex(chartEvent, chart.originalDataIndex(echartsData));
	};
	
	//地图关系图
	
	chartSupport.mapGraphRender = function(chart, options)
	{
		options = $.extend(true,
		{
			dg:
			{
				//map 可选，地图名
				dataSignNames:
				{
					sourceId: "sourceId", sourceLongitude: "sourceLongitude", sourceLatitude: "sourceLatitude",
					sourceName: "sourceName", sourceCategory: "sourceCategory", sourceValue: "sourceValue",
					targetId: "targetId", targetLongitude: "targetLongitude", targetLatitude: "targetLatitude",
					targetName: "targetName", targetCategory: "targetCategory", targetValue: "targetValue",
					map: "map"
				},
				//最大数据标记像素数
				symbolSizeMax: undefined,
				//最小数据标记像素数
				symbolSizeMin: undefined,
			}
		},
		options);
		
		options = chartSupport.inflateRenderOptions(chart,
		{
			title:
			{
		        text: chart.name
		    },
			tooltip:
			{
				trigger: "item"
			},
			legend:
			{
				id: 0,
				//将在update中设置：
				//data
			},
			geo:
			{
				id: 0,
				roam: true
				//将在下面和update中设置：
				//map
			},
			series:
			[
				{
					//将在update中设置：
					//name
					//data
					//links
					
					id: 0,
					type: "graph",
			        coordinateSystem: "geo",
					geoIndex: 0,
			        layout: "none",
					tooltip:
					{
						formatter: "{a}<br />{b}：{c}"
					}
				}
			]
		},
		options,
		null,
		function(renderOptions, chart)
		{
			chartSupport.echartsMapChartInitMap(chart, renderOptions);
		});
		
		chartSupport.echartsMapChartRender(chart, options);
	};
	
	chartSupport.mapGraphUpdate = function(chart, chartResult)
	{
		var renderOptions= chart.renderOptions();
		var dg = renderOptions.dg;
		var dataSignNames = dg.dataSignNames;
		
		var dataSetBinds = chart.dataSetBindsMain();
		
		var legendData = [];
		var seriesName = "";
		var categories = [];
		var seriesData = [];
		var seriesLinks = [];
		var map = undefined;
		
		var min = undefined, max = undefined;
		var symbolSizeMax = chartSupport.evalSymbolSizeMax(chart, renderOptions);
		var symbolSizeMin = chartSupport.evalSymbolSizeMin(chart, renderOptions, symbolSizeMax);
		
		for(var i=0; i<dataSetBinds.length; i++)
		{
			var dataSetBind = dataSetBinds[i];
			
			var result = chart.resultOf(chartResult, dataSetBind);
			
			//取任一不为空的地图名列值
			if(!map)
				map = chartSupport.resultFirstNonEmptyValueOfSign(chart, dataSetBind, result, dataSignNames.map);
			
			if(!seriesName)
				seriesName = chart.dataSetAlias(dataSetBind);
			
			var sip = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.sourceId);
			var slop = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.sourceLongitude);
			var slap = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.sourceLatitude);
			var snp = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.sourceName);
			var scp = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.sourceCategory);
			var svp = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.sourceValue);
			var tip = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.targetId);
			var tlop = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.targetLongitude);
			var tlap = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.targetLatitude);
			var tnp = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.targetName);
			var tcp = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.targetCategory);
			var tvp = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.targetValue);
			
			var data = chart.resultDatas(result);
			
			for(var j=0; j<data.length; j++)
			{
				//ECharts-4.9.0时graph官方数据格式为【名/值数组】：{name: ..., value:[经度值, 纬度值, 关系数值]}
				//ECharts-5.0+ 时graph官方数据格式为【名/X/Y/值】：{name: ..., x: 经度值, y: 纬度值, value: 关系数值}
				//在ECharts由4.9.0升级至5.1.2版本后，【名/值数组】、【名/X/Y/值】格式都会报错：Can not read property 'off' of undefined，
				//在修改了源码（修改位置参考DataGear-2.8.0版本echarts-5.1.2/echarts.js的58833行）同时采用【名/值数组】格式后才解决。
				//在ECharts由5.1.2升级至5.2.0版本后，【名/X/Y/值】格式不会报错但是显示位置不对，【名/值数组】则可以正常展示
				var sd = { name: chart.resultRowCell(data[j], snp), value: [ chart.resultRowCell(data[j], slop), chart.resultRowCell(data[j], slap) ] };
				var td = { name: chart.resultRowCell(data[j], tnp), value: [ chart.resultRowCell(data[j], tlop), chart.resultRowCell(data[j], tlap) ] };
				
				if(sip)
					sd.id = chart.resultRowCell(data[j], sip);
				
				if(scp)
				{
					var category = chart.resultRowCell(data[j], scp);
					sd._categoryOrigin = category;
					if(category)
					{
						sd.category = chartSupport.appendDistinct(categories, {name: category}, "name");
						chartSupport.appendDistinct(legendData, category);
					}
				}
				
				if(svp)
				{
					var sv = chart.resultRowCell(data[j], svp);
					sd.value.push(sv);
					
					min = (min == null ? sv : Math.min(min, sv));
					max = (max == null ? sv : Math.max(max, sv));
				}
				
				if(tip)
					td.id = chart.resultRowCell(data[j], tip);
				
				if(tcp)
				{
					var category = chart.resultRowCell(data[j], tcp);
					td._categoryOrigin = category;
					if(category)
					{
						td.category = chartSupport.appendDistinct(categories, {name: category}, "name");
						chartSupport.appendDistinct(legendData, category);
					}
				}
				
				if(tvp)
				{
					var tv = chart.resultRowCell(data[j], tvp);
					td.value.push(tv);
					
					min = (min == null ? tv : Math.min(min, tv));
					max = (max == null ? tv : Math.max(max, tv));
				}
				
				var sidx = chartSupport.appendDistinct(seriesData, sd, (sip ? "id" : "name"));
				
				//新插入
				if(sidx == seriesData.length - 1 && seriesData[seriesData.length - 1] === sd)
				{
					chart.originalDataIndex(sd, dataSetBind, j);
				}
				
				var tidx = chartSupport.appendDistinct(seriesData, td, (tip ? "id" : "name"));
				
				//新插入
				if(tidx == seriesData.length - 1 && seriesData[seriesData.length - 1] === td)
				{
					chart.originalDataIndex(td, dataSetBind, j);
				}
				
				//如果使用id值表示关系，对于数值型id，echarts会误当做数据索引，所以这里直接使用数据索引
				var link = {};
				link.source = sidx;
				link.target = tidx;
				
				chart.originalDataIndex(link, dataSetBind, j);
				
				seriesLinks.push(link);
			}
		}
		
		var series = [ { id: 0, type: "graph", name: seriesName, categories: categories, data: seriesData, links: seriesLinks, 
			        		coordinateSystem: "geo" } ];
		
		chartSupport.evalSeriesDataValueSymbolSize(series, min, max, symbolSizeMax, symbolSizeMin, "value", 2);
		
		var options = { legend: {id: 0, data: legendData}, series: series };
		
		if(map)
		{
			options.geo = { id: 0, map: map };
		}
		
		chartSupport.echartsMapChartUpdate(chart, chartResult, options, renderOptions);
		
		chartFactory.extValueBuiltin(chart, "mapGraphSeriesData", seriesData);
	};
	
	chartSupport.mapGraphResize = function(chart)
	{
		chartSupport.resizeChartEcharts(chart);
	};
	
	chartSupport.mapGraphDestroy = function(chart)
	{
		chartSupport.destroyChartEcharts(chart);
	};
	
	chartSupport.mapGraphOn = function(chart, eventType, handler)
	{
		chartSupport.bindChartEventHandlerForEcharts(chart, eventType, handler,
				chartSupport.mapGraphSetChartEventData);
	};
	
	chartSupport.mapGraphOff = function(chart, eventType, handler)
	{
		chart.echartsOffEventHandler(eventType, handler);
	};
	
	chartSupport.mapGraphSetChartEventData = function(chart, chartEvent, echartsEventParams)
	{
		var renderOptions= chart.renderOptions();
		var dg = renderOptions.dg;
		var dataSignNames = dg.dataSignNames;
		
		var echartsData = echartsEventParams.data;
		
		var data = {};
		
		//节点，仅使用源数据标记对象
		if(echartsEventParams.dataType == "node")
		{
			data[dataSignNames.sourceId] = echartsData.id;
			data[dataSignNames.sourceLongitude] = echartsData.value[0];
			data[dataSignNames.sourceLatitude] = echartsData.value[1];
			data[dataSignNames.sourceName] = echartsData.name;
			data[dataSignNames.sourceCategory] = echartsData._categoryOrigin;
			if(echartsData.value.length > 2)
				data[dataSignNames.sourceValue] = echartsData.value[2];
			
			chart.eventData(chartEvent, data);
			chart.eventOriginalDataIndex(chartEvent, chart.originalDataIndex(echartsData));
		}
		//边
		else if(echartsEventParams.dataType == "edge")
		{
			var seriesData = chartFactory.extValueBuiltin(chart, "mapGraphSeriesData");
			var sourceData = seriesData[echartsData.source];
			var targetData = seriesData[echartsData.target];
			
			data[dataSignNames.sourceId] = sourceData.id;
			data[dataSignNames.sourceLongitude] = sourceData.value[0];
			data[dataSignNames.sourceLatitude] = sourceData.value[1];
			data[dataSignNames.sourceName] = sourceData.name;
			data[dataSignNames.sourceCategory] = sourceData._categoryOrigin;
			if(sourceData.value.length > 2)
				data[dataSignNames.sourceValue] = sourceData.value[2];
			
			if(targetData)
			{
				data[dataSignNames.targetId] = targetData.id;
				data[dataSignNames.targetLongitude] = targetData.value[0];
				data[dataSignNames.targetLatitude] = targetData.value[1];
				data[dataSignNames.targetName] = targetData.name;
				data[dataSignNames.targetCategory] = targetData._categoryOrigin;
				if(targetData.value.length > 2)
					data[dataSignNames.targetValue] = targetData.value[2];
			}
			
			chart.eventData(chartEvent, data);
			chart.eventOriginalDataIndex(chartEvent, chart.originalDataIndex(echartsData));
		}
	};
	
	//地图路径图
	
	chartSupport.mapLinesRender = function(chart, options)
	{
		options = $.extend(true,
		{
			dg:
			{
				//name 可选，路径名称，同一名称的坐标组成一条路径，如果不选，整个数据集组成一条路径
				//longitude 路径经度
				//latitude 路径纬度
				//map 可选，地图名
				dataSignNames: { name: "name", longitude: "longitude", latitude: "latitude", map: "map" }
			}
		},
		options);
		
		options = chartSupport.inflateRenderOptions(chart,
		{
			title:
			{
		        text: chart.name
		    },
			legend:
			{
				id: 0,
				//将在update中设置：
				//data
			},
			geo:
			{
				id: 0,
				roam: true
				//将在下面和update中设置：
				//map
			},
			series:
			[
				//将在update中设置：
				//{}
				//设初值以免渲染报错
				{
					id: 0,
					type: "lines",
					coordinateSystem: "geo",
					geoIndex: 0,
					polyline: true
				}
			]
		},
		options,
		null,
		function(renderOptions, chart)
		{
			chartSupport.echartsMapChartInitMap(chart, renderOptions);
		});
		
		chartSupport.echartsMapChartRender(chart, options);
	};
	
	chartSupport.mapLinesUpdate = function(chart, chartResult)
	{
		var renderOptions= chart.renderOptions();
		var dg = renderOptions.dg;
		var dataSignNames = dg.dataSignNames;
		
		var dataSetBinds = chart.dataSetBindsMain();
		
		var legendData = [];
		var series = [];
		var map = undefined;
		
		for(var i=0; i<dataSetBinds.length; i++)
		{
			var dataSetBind = dataSetBinds[i];
			var dataSetAlias = chart.dataSetAlias(dataSetBind);
			var result = chart.resultOf(chartResult, dataSetBind);
			
			//取任一不为空的地图名列值
			if(!map)
				map = chartSupport.resultFirstNonEmptyValueOfSign(chart, dataSetBind, result, dataSignNames.map);
			
			var np = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.name);
			var lop = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.longitude);
			var lap = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.latitude);
			
			var data = null;
			if(np)
			{
				//同名称的是一条路径
				
				var names = [];
				var coordsInfos = {};
				
				data = chart.resultNameValueObjects(result, np, [lop, lap]);
				
				for(var j=0; j<data.length; j++)
				{
					var dj = data[j];
					var name = dj.name;
					var coordsInfo = coordsInfos[name];
					
					if(!coordsInfo)
					{
						names.push(name);
						coordsInfo = (coordsInfos[name] = { coords: [], originalDataIndexes: [] });
					}
					
					coordsInfo.coords.push(dj.value);
					coordsInfo.originalDataIndexes.push(j);
				}
				
				data = [];
				
				for(var j=0; j<names.length; j++)
				{
					var name = names[j];
					data[j] = { name: name, coords: coordsInfos[name].coords };
					chart.originalDataIndex(data[j], dataSetBind, coordsInfos[name].originalDataIndexes);
				}
			}
			else
			{
				//整个数据集是一条路径
				data = chart.resultRowArrays(result, [lop, lap]);
				var originalDataIndexes = [];
				for(var j=0;j<data.length; j++)
					originalDataIndexes[j] = j;
				
				data = [ { name: dataSetAlias, coords: data } ];
				chart.originalDataIndex(data[0], dataSetBind, originalDataIndexes);
			}
			
			legendData.push(dataSetAlias);
			series.push({ id: series.length, name: dataSetAlias, data: data, type: "lines", coordinateSystem: "geo", polyline: true });
		}
		
		var options = { legend: {id: 0, data: legendData}, series: series };
		
		if(map)
		{
			options.geo = { id: 0, map: map };
		}
		
		chartSupport.echartsMapChartUpdate(chart, chartResult, options, renderOptions);
	};
	
	chartSupport.mapLinesResize = function(chart)
	{
		chartSupport.resizeChartEcharts(chart);
	};
	
	chartSupport.mapLinesDestroy = function(chart)
	{
		chartSupport.destroyChartEcharts(chart);
	};
	
	chartSupport.mapLinesOn = function(chart, eventType, handler)
	{
		chartSupport.bindChartEventHandlerForEcharts(chart, eventType, handler,
				chartSupport.mapLinesSetChartEventData);
	};
	
	chartSupport.mapLinesOff = function(chart, eventType, handler)
	{
		chart.echartsOffEventHandler(eventType, handler);
	};
	
	chartSupport.mapLinesSetChartEventData = function(chart, chartEvent, echartsEventParams)
	{
		var renderOptions= chart.renderOptions();
		var dg = renderOptions.dg;
		var dataSignNames = dg.dataSignNames;
		
		var echartsData = echartsEventParams.data;
		
		var data = {};
		data[dataSignNames.name] = echartsData.name;
		var dataLongitude = (data[dataSignNames.longitude] = []);
		var dataLatitude = (data[dataSignNames.latitude] = []);
		
		var coords = (echartsData.coords || []);
		for(var i=0; i<coords.length; i++)
		{
			var coord = coords[i];
			dataLongitude.push(coord[0]);
			dataLatitude.push(coord[1]);
		}
		
		chart.eventData(chartEvent, data);
		chart.eventOriginalDataIndex(chartEvent, chart.originalDataIndex(echartsData));
	};
	
	//地图飞线图
	
	chartSupport.mapFlylineRender = function(chart, options)
	{
		options = $.extend(true,
		{
			dg:
			{
				//name 飞线名
				//sourceLongitude 源点经度
				//sourceLatitude 源点纬度
				//targetLongitude 终点经度
				//targetLatitude 终点纬度
				//category 类别，可选，同一类别的绘制于同一系列
				//map 地图名，可选
				dataSignNames:
				{
					name: "name", sourceLongitude: "sourceLongitude", sourceLatitude: "sourceLatitude",
					targetLongitude: "targetLongitude", targetLatitude: "targetLatitude",
					category: "category", map: "map"
				}
			}
		},
		options);
		
		options = chartSupport.inflateRenderOptions(chart,
		{
			title:
			{
		        text: chart.name
		    },
			tooltip:
			{
				
			},
			legend:
			{
				id: 0,
				//将在update中设置：
				//data
			},
			geo:
			{
				id: 0,
				roam: true
				//将在下面和update中设置：
				//map
			},
			series:
			[
				//将在update中设置：
				//{}
				//设初值以免渲染报错
				{
					id: 0,
					type: "lines",
					coordinateSystem: "geo",
					geoIndex: 0,
					polyline: false
				}
			]
		},
		options,
		null,
		function(renderOptions, chart)
		{
			chartSupport.echartsMapChartInitMap(chart, renderOptions);
		});
		
		chartSupport.echartsMapChartRender(chart, options);
	};
	
	chartSupport.mapFlylineUpdate = function(chart, chartResult)
	{
		var renderOptions= chart.renderOptions();
		var dg = renderOptions.dg;
		var dataSignNames = dg.dataSignNames;
		
		var dataSetBinds = chart.dataSetBindsMain();
		var categoryNames = [];
		var categoryDatasMap = {};
		var map = undefined;
		
		for(var i=0; i<dataSetBinds.length; i++)
		{
			var dataSetBind = dataSetBinds[i];
			var dataSetAlias = chart.dataSetAlias(dataSetBind);
			var result = chart.resultOf(chartResult, dataSetBind);
			
			//取任一不为空的地图名列值
			if(!map)
				map = chartSupport.resultFirstNonEmptyValueOfSign(chart, dataSetBind, result, dataSignNames.map);
			
			var np = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.name);
			var vps = [
						chart.dataSetFieldOfSign(dataSetBind, dataSignNames.sourceLongitude),
						chart.dataSetFieldOfSign(dataSetBind, dataSignNames.sourceLatitude),
						chart.dataSetFieldOfSign(dataSetBind, dataSignNames.targetLongitude),
						chart.dataSetFieldOfSign(dataSetBind, dataSignNames.targetLatitude),
					];
			var cp = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.category);
			
			var propertyMap = { "name": np, "coords": vps };
			if(cp)
				propertyMap = chartSupport.inflatePropertyMapWithCategory(propertyMap, cp);
			
			var data = chart.resultMapObjects(result, propertyMap);
			
			for(var j=0; j<data.length; j++)
			{
				var coords = data[j].coords;
				data[j].coords = [[coords[0], coords[1]], [coords[2], coords[3]]];
			}
			
			chart.originalDataIndexes(data, dataSetBind);
			
			if(cp)
				chartSupport.splitDataByCategory(data, categoryNames, categoryDatasMap);
			else
				chartSupport.appendCategoryNameAndData(categoryNames, categoryDatasMap, dataSetAlias, data);
		}
		
		var series = [];
		
		for(var i=0; i<categoryNames.length; i++)
		{
			series[i] =
			{
				id: series.length,
				name: categoryNames[i],
				data: categoryDatasMap[categoryNames[i]],
				type: "lines",
				coordinateSystem: "geo",
				polyline: false,
				effect:
				{
					show: true,
					symbol: "arrow",
					symbolSize: 5,
					trailLength: 0
				},
				lineStyle:
				{
					curveness: 0.2
				}
			};
		}
		
		var options = { legend: {id: 0, data: categoryNames}, series: series };
		
		if(map)
		{
			options.geo = { id: 0, map: map };
		}
		
		chartSupport.echartsMapChartUpdate(chart, chartResult, options, renderOptions);
	};
	
	chartSupport.mapFlylineResize = function(chart)
	{
		chartSupport.resizeChartEcharts(chart);
	};
	
	chartSupport.mapFlylineDestroy = function(chart)
	{
		chartSupport.destroyChartEcharts(chart);
	};
	
	chartSupport.mapFlylineOn = function(chart, eventType, handler)
	{
		chartSupport.bindChartEventHandlerForEcharts(chart, eventType, handler,
				chartSupport.mapFlylineSetChartEventData);
	};
	
	chartSupport.mapFlylineOff = function(chart, eventType, handler)
	{
		chart.echartsOffEventHandler(eventType, handler);
	};
	
	chartSupport.mapFlylineSetChartEventData = function(chart, chartEvent, echartsEventParams)
	{
		var renderOptions= chart.renderOptions();
		var dg = renderOptions.dg;
		var dataSignNames = dg.dataSignNames;
		
		var echartsData = echartsEventParams.data;
		var data = undefined;
		
		if(echartsData)
		{
			var coords = (echartsData.coords || []);
			var coords0 = (coords[0] || []);
			var coords1 = (coords[1] || []);
			var categoryPropertyName = chartSupport.builtinCategoryPropName();
			
			data={};
			data[dataSignNames.name] = echartsData.name;
			data[dataSignNames.sourceLongitude] = coords0[0];
			data[dataSignNames.sourceLatitude] = coords0[1];
			data[dataSignNames.targetLongitude] = coords1[0];
			data[dataSignNames.targetLatitude] = coords1[1];
			data[dataSignNames.category] = echartsData[categoryPropertyName];
		}
		
		chart.eventData(chartEvent, data);
		chart.eventOriginalDataIndex(chartEvent, chart.originalDataIndex(echartsData));
	};
	
	//地图热力图
	
	chartSupport.mapHeatmapRender = function(chart, options)
	{
		options = $.extend(true,
		{
			dg:
			{
				//name 经度
				//value 纬度
				//weight 热力
				//map 可选，地图名
				dataSignNames: { name: "name", value: "value", weight: "weight", map: "map" }
			}
		},
		options);
		
		options = chartSupport.inflateRenderOptions(chart,
		{
			title:
			{
		        text: chart.name
		    },
			geo:
			{
				id: 0,
				roam: true
				//将在下面和update中设置：
				//map
			},
			visualMap:
			{
				//将在update中设置：
				//min
				//max
				
				id: 0,
				show: false,
				top: "top",
				calculable: true
			},
			series:
			[
				//将在update中设置：
				//{}
				//设初值以免渲染报错
				{
					id: 0,
					type: "heatmap",
					coordinateSystem: "geo",
					geoIndex: 0,
					pointSize: 5,
					blurSize: 6
				}
			]
		},
		options,
		null,
		function(renderOptions, chart)
		{
			chartSupport.echartsMapChartInitMap(chart, renderOptions);
		});
		
		chartSupport.echartsMapChartRender(chart, options);
	};
	
	chartSupport.mapHeatmapUpdate = function(chart, chartResult)
	{
		var renderOptions= chart.renderOptions();
		var dg = renderOptions.dg;
		var dataSignNames = dg.dataSignNames;
		var chartEle = chart.elementJquery();
		
		var dataSetBinds = chart.dataSetBindsMain();
		
		var seriesName = "";
		var seriesData = [];
		var dataRange = { min: undefined, max: undefined };
		var map = undefined;
		
		for(var i=0; i<dataSetBinds.length; i++)
		{
			var dataSetBind = dataSetBinds[i];
			
			var result = chart.resultOf(chartResult, dataSetBind);
			
			//取任一不为空的地图名列值
			if(!map)
				map = chartSupport.resultFirstNonEmptyValueOfSign(chart, dataSetBind, result, dataSignNames.map);
			
			var np = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.name);
			var vp = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.value);
			var wp = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.weight);
			
			var data = chart.resultValueObjects(result, [ np, vp, wp ]);
			
			chart.originalDataIndexes(data, dataSetBind);
			chartSupport.evalArrayDataRange(dataRange, data, "value", 2);
			
			seriesData = seriesData.concat(data);
			
			if(!seriesName)
				seriesName = chart.dataSetAlias(dataSetBind);
		}
		
		var pointSize = parseInt(Math.min(chartEle.width(), chartEle.height())/60);
		if(pointSize < 1)
			pointSize = 1;
		
		var series =
		[{
			id: 0,
			name: seriesName,
			data: seriesData,
			type: "heatmap",
			coordinateSystem: "geo",
			pointSize: pointSize,
			blurSize: parseInt(pointSize*1.2)
		}];
		
		var options = { visualMap: {id: 0, min: dataRange.min, max: dataRange.max}, series: series };
		chartSupport.trimNumberRange(options.visualMap);
		
		if(map)
		{
			options.geo = { id: 0, map: map };
		}
		
		chartSupport.echartsMapChartUpdate(chart, chartResult, options, renderOptions);
	};
	
	chartSupport.mapHeatmapResize = function(chart)
	{
		chartSupport.resizeChartEcharts(chart);
	};
	
	chartSupport.mapHeatmapDestroy = function(chart)
	{
		chartSupport.destroyChartEcharts(chart);
	};

	chartSupport.mapHeatmapOn = function(chart, eventType, handler)
	{
		chartSupport.bindChartEventHandlerForEcharts(chart, eventType, handler,
				chartSupport.mapHeatmapSetChartEventData);
	};
	
	chartSupport.mapHeatmapOff = function(chart, eventType, handler)
	{
		chart.echartsOffEventHandler(eventType, handler);
	};
	
	chartSupport.mapHeatmapSetChartEventData = function(chart, chartEvent, echartsEventParams)
	{
		var renderOptions= chart.renderOptions();
		var dg = renderOptions.dg;
		var dataSignNames = dg.dataSignNames;
		
		var echartsData = echartsEventParams.data;
		var data = {};
		
		data[dataSignNames.name] = echartsData.value[0];
		data[dataSignNames.value] = echartsData.value[1];
		data[dataSignNames.weight] = echartsData.value[2];
		
		chart.eventData(chartEvent, data);
		chart.eventOriginalDataIndex(chartEvent, chart.originalDataIndex(echartsData));
	};
	
	//K线图
	
	chartSupport.candlestickRender = function(chart, options)
	{
		options = $.extend(true,
		{
			dg:
			{
				dataSignNames: { name: "name", open: "open", close: "close", min: "min", max: "max" }
			}
		},
		options);
		
		var dataSignNames = options.dg.dataSignNames;
		var dataSetBind = chartSupport.dataSetBindMainNonNull(chart);
		var np = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.name);
		
		options = chartSupport.inflateRenderOptions(chart,
		{
			title:
			{
		        text: chart.name
		    },
			tooltip:
			{
				trigger: "item"
			},
			legend:
			{
				id: 0,
				//将在update中设置：
				//data
			},
			xAxis:
			{
				//将在update中设置：
				//data
				
				id: 0,
				name: chart.dataSetFieldAlias(dataSetBind, np),
				nameGap: 5,
				type: chartSupport.evalDataSetFieldAxisType(chart, np),
				boundaryGap: true,
				splitLine: {show:false}
			},
			yAxis:
			{
				id: 0,
				name: "",
				nameGap: 5,
				type: "value"
			},
			series:
			[
				//将在update中设置：
				//{}
				//设初值以免渲染报错
				{
					id: 0,
					type: "k"
				}
			]
		},
		options,
		function(options)
		{
			//K线图的angleAxis.type不能为value和time，不然图形无法显示
			if(options.xAxis.type == "value" || options.xAxis.type == "time")
				options.xAxis.type = "category";
		});
		
		chart.echartsInit(options);
	};
	
	chartSupport.candlestickUpdate = function(chart, chartResult)
	{
		var renderOptions= chart.renderOptions();
		var dg = renderOptions.dg;
		var dataSignNames = dg.dataSignNames;
		
		var dataSetBinds = chart.dataSetBindsMain();
		
		var legendData = [];
		var series = [];
		
		for(var i=0; i<dataSetBinds.length; i++)
		{
			var dataSetBind = dataSetBinds[i];
			var dataSetAlias = chart.dataSetAlias(dataSetBind);
			var result = chart.resultOf(chartResult, dataSetBind);
			
			var np = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.name);
			
			var data = chart.resultNameValueObjects(result, np,
					[
						chart.dataSetFieldOfSign(dataSetBind, dataSignNames.open),
						chart.dataSetFieldOfSign(dataSetBind, dataSignNames.close),
						chart.dataSetFieldOfSign(dataSetBind, dataSignNames.min),
						chart.dataSetFieldOfSign(dataSetBind, dataSignNames.max)
					]);
			
			chart.originalDataIndexes(data, dataSetBind);
			
			series.push({id: series.length, type: "k", name: dataSetAlias, data: data});
		}
		
		//坐标轴信息也应替换合并，不然图表刷新有数据变化时，坐标不能自动更新
		var options = { legend: {id: 0, data: legendData}, series: series, xAxis: { id: 0 } };
		
		chartSupport.inflateAxisDataForEchartsUpdateOptions(renderOptions, options, options.xAxis,
						chartSupport.inflateAxisDataExtractors.property("name"));
		
		chartSupport.adaptArrayPropsForUpdateOptions(options, renderOptions);
		options = chart.inflateUpdateOptions(chartResult, options);
		
		chartSupport.echartsOptionsReplaceMerge(chart, options);
	};
	
	chartSupport.candlestickResize = function(chart)
	{
		chartSupport.resizeChartEcharts(chart);
	};
	
	chartSupport.candlestickDestroy = function(chart)
	{
		chartSupport.destroyChartEcharts(chart);
	};
	
	chartSupport.candlestickOn = function(chart, eventType, handler)
	{
		chartSupport.bindChartEventHandlerForEcharts(chart, eventType, handler,
				chartSupport.candlestickSetChartEventData);
	};
	
	chartSupport.candlestickOff = function(chart, eventType, handler)
	{
		chart.echartsOffEventHandler(eventType, handler);
	};
	
	chartSupport.candlestickSetChartEventData = function(chart, chartEvent, echartsEventParams)
	{
		var renderOptions= chart.renderOptions();
		var dg = renderOptions.dg;
		var dataSignNames = dg.dataSignNames;
		
		var echartsData = echartsEventParams.data;
		var data = {};
		
		//echartsData不是设置的初始系列数据，第0个元素是数据索引，echarts的BUG？？？
		var idx = (echartsData.value.length > 4 ? 1 : 0);
		
		data[dataSignNames.name] = echartsData.name;
		data[dataSignNames.open] = echartsData.value[idx];
		data[dataSignNames.close] = echartsData.value[idx+1];
		data[dataSignNames.min] = echartsData.value[idx+2];
		data[dataSignNames.max] = echartsData.value[idx+3];
		
		chart.eventData(chartEvent, data);
		chart.eventOriginalDataIndex(chartEvent, chart.originalDataIndex(echartsData));
	};
	
	//热力图
	
	chartSupport.heatmapRender = function(chart, options)
	{
		options = $.extend(true,
		{
			dg:
			{
				//name 名称
				//value 数值
				//weight 热力值
				dataSignNames: { name: "name", value: "value", weight: "weight" }
			}
		},
		options);
		
		var dataSignNames = options.dg.dataSignNames;
		var dataSetBind = chartSupport.dataSetBindMainNonNull(chart);
		var np = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.name);
		var vp = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.value);
		
		var chartEle = chart.elementJquery();
		var vmItemWidth = parseInt(chartEle.height()/20);
		
		options = chartSupport.inflateRenderOptions(chart,
		{
			title:
			{
		        text: chart.name
		    },
			tooltip:
			{
				trigger: "item"
			},
			grid:
			{
				bottom: vmItemWidth + 20
			},
			legend:
			{
				id: 0,
				show: false
			},
			xAxis:
			{
				//将在update中设置：
				//data
				
				id: 0,
				name: chart.dataSetFieldAlias(dataSetBind, np),
				nameGap: 5,
				type: chartSupport.evalDataSetFieldAxisType(chart, np),
				splitArea: { show: true }
			},
			yAxis:
			{
				//将在update中设置：
				//data
				
				id: 0,
				name: chart.dataSetFieldAlias(dataSetBind, vp),
				nameGap: 5,
				type: chartSupport.evalDataSetFieldAxisType(chart, vp),
				splitArea: { show: true }
			},
			visualMap:
			{
				//将在update中设置：
				//min
				//max
				
				id: 0,
				text: ["高", "低"],
				realtime: true,
				calculable: true,
				orient: "horizontal",
		        left: "center",
		        itemWidth: vmItemWidth,
		        itemHeight: parseInt(chartEle.width()/8),
		        bottom: 0
			},
			series:
			[
				//将在update中设置：
				//{}
				//设初值以免渲染报错
				{
					id: 0,
					type: "heatmap"
				}
			]
		},
		options,
		function(options)
		{
			//热力图的xAxis.type、yAxis.type不能为value和time，不然图形无法显示
			if(options.xAxis.type == "value" || options.xAxis.type == "time")
				options.xAxis.type = "category";
			if(options.yAxis.type == "value" || options.yAxis.type == "time")
				options.yAxis.type = "category";
		});
		
		chart.echartsInit(options);
	};
	
	chartSupport.heatmapUpdate = function(chart, chartResult)
	{
		var renderOptions= chart.renderOptions();
		var dg = renderOptions.dg;
		var dataSignNames = dg.dataSignNames;
		
		var dataSetBinds = chart.dataSetBindsMain();
		
		var seriesName = "";
		var seriesData = [];
		var dataRange = { min: undefined, max: undefined };
		
		for(var i=0; i<dataSetBinds.length; i++)
		{
			var dataSetBind = dataSetBinds[i];
			
			var result = chart.resultOf(chartResult, dataSetBind);
			
			var np = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.name);
			var vp = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.value);
			var wp = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.weight);
			
			var data = chart.resultValueObjects(result, [ np, vp, wp ]);
			
			chart.originalDataIndexes(data, dataSetBind);
			chartSupport.evalArrayDataRange(dataRange, data, "value", 2);
			
			seriesData = seriesData.concat(data);
			
			if(!seriesName)
				seriesName = chart.dataSetAlias(dataSetBind);
		}
		
		var series = [ { id: 0, type: "heatmap", name: seriesName, data: seriesData } ];
		
		var options =
		{
			xAxis: { id: 0 }, yAxis: { id: 0 },
			visualMap: {id: 0, min: dataRange.min, max: dataRange.max},
			series: series
		};
		
		chartSupport.trimNumberRange(options.visualMap);
		
		chartSupport.inflateAxisDataForEchartsUpdateOptions(renderOptions, options, options.xAxis,
						chartSupport.inflateAxisDataExtractors.valueElement(0));
		chartSupport.inflateAxisDataForEchartsUpdateOptions(renderOptions, options, options.yAxis,
						chartSupport.inflateAxisDataExtractors.valueElement(1), false);
		
		chartSupport.adaptArrayPropsForUpdateOptions(options, renderOptions);
		options = chart.inflateUpdateOptions(chartResult, options);
		
		chartSupport.echartsOptionsReplaceMerge(chart, options);
	};
	
	chartSupport.heatmapResize = function(chart)
	{
		chartSupport.resizeChartEcharts(chart);
	};
	
	chartSupport.heatmapDestroy = function(chart)
	{
		chartSupport.destroyChartEcharts(chart);
	};

	chartSupport.heatmapOn = function(chart, eventType, handler)
	{
		chartSupport.bindChartEventHandlerForEcharts(chart, eventType, handler,
				chartSupport.heatmapSetChartEventData);
	};
	
	chartSupport.heatmapOff = function(chart, eventType, handler)
	{
		chart.echartsOffEventHandler(eventType, handler);
	};
	
	chartSupport.heatmapSetChartEventData = function(chart, chartEvent, echartsEventParams)
	{
		var renderOptions= chart.renderOptions();
		var dg = renderOptions.dg;
		var dataSignNames = dg.dataSignNames;
		
		var echartsData = echartsEventParams.data;
		var data = {};
		
		data[dataSignNames.name] = echartsData.value[0];
		data[dataSignNames.value] = echartsData.value[1];
		data[dataSignNames.weight] = echartsData.value[2];
		
		chart.eventData(chartEvent, data);
		chart.eventOriginalDataIndex(chartEvent, chart.originalDataIndex(echartsData));
	};
	
	//树图
	chartSupport.treeRender = function(chart, options)
	{
		options = $.extend(true,
		{
			dg:
			{
				dataSignNames: { id: "id", name: "name", parent: "parent", value: "value" },
				//同series[i].orient
				orient: "LR",
			}
		},
		options);
		
		options = chartSupport.inflateRenderOptions(chart,
		{
			title:
			{
		        text: chart.name
		    },
			tooltip:
			{
				trigger: "item"
			},
			series:
			[
				//将在update中设置：
				//{}
				//设初值以免渲染报错
				{
					id: 0,
					type: "tree"
				}
			]
		},
		options);
		
		chart.echartsInit(options);
	};
	
	chartSupport.treeUpdate = function(chart, chartResult)
	{
		var renderOptions= chart.renderOptions();
		
		var options = { series: [ chartSupport.buildTreeNodeSeries(chart, chartResult, { id: 0, type: "tree" }) ] };
		chartSupport.treeInflateUpdateOptions(chart, options, renderOptions);
		
		chartSupport.adaptArrayPropsForUpdateOptions(options, renderOptions);
		options = chart.inflateUpdateOptions(chartResult, options);
		
		chartSupport.echartsOptionsReplaceMerge(chart, options);
	};
	
	chartSupport.treeResize = function(chart)
	{
		chartSupport.resizeChartEcharts(chart);
	};
	
	chartSupport.treeDestroy = function(chart)
	{
		chartSupport.destroyChartEcharts(chart);
	};

	chartSupport.treeOn = function(chart, eventType, handler)
	{
		chartSupport.bindChartEventHandlerForEcharts(chart, eventType, handler,
				chartSupport.treeSetChartEventData);
	};
	
	chartSupport.treeOff = function(chart, eventType, handler)
	{
		chart.echartsOffEventHandler(eventType, handler);
	};
	
	chartSupport.treeSetChartEventData = function(chart, chartEvent, echartsEventParams)
	{
		var renderOptions= chart.renderOptions();
		var dg = renderOptions.dg;
		var dataSignNames = dg.dataSignNames;
		
		var echartsData = echartsEventParams.data;
		var data = {};
		
		data[dataSignNames.id] = echartsData.idOrigin;
		data[dataSignNames.name] = echartsData.name;
		data[dataSignNames.parent] = echartsData.parent;
		data[dataSignNames.value] = echartsData.value;

		chart.eventData(chartEvent, data);
		chart.eventOriginalDataIndex(chartEvent, chart.originalDataIndex(echartsData));
	};
	
	chartSupport.treeInflateUpdateOptions = function(chart, updateOptions, renderOptions)
	{
		var seriesEle = updateOptions.series[0];
		var seriesEleExt = {};
		
		seriesEle.orient = renderOptions.dg.orient;
		
		if(renderOptions.dg.orient == "LR")
		{
			seriesEleExt =
			{
				label: { position: "left", verticalAlign: "middle", align: "right" },
                leaves: { label: { position: "right", verticalAlign: "middle", align: "left" } },
                left: "16%",
                right: "16%",
                top: "12%",
                bottom: "12%"
			};
		}
		else if(renderOptions.dg.orient == "TB")
		{
			seriesEleExt =
			{
				label: { position: "left", verticalAlign: "middle", align: "right" },
                leaves: { label: { position: "right", verticalAlign: "middle", align: "left" } },
                left: "12%",
                right: "12%",
                top: "16%",
                bottom: "16%"
			};
		}
		else if(renderOptions.dg.orient == "RL")
		{
			seriesEleExt =
			{
				label: { position: "right", verticalAlign: "middle", align: "left" },
                leaves: { label: { position: "left", verticalAlign: "middle", align: "right" } },
                left: "16%",
                right: "16%",
                top: "12%",
                bottom: "12%"
			};
		}
		else if(renderOptions.dg.orient == "BT")
		{
			seriesEleExt =
			{
				label: { position: "left", verticalAlign: "middle", align: "right" },
                leaves: { label: { position: "right", verticalAlign: "middle", align: "left" } },
                left: "12%",
                right: "12%",
                top: "16%",
                bottom: "16%"
			};
		}
		
		$.extend(seriesEle, seriesEleExt);
	};
	
	//矩形树图
	chartSupport.treemapRender = function(chart, options)
	{
		options = $.extend(true,
		{
			dg:
			{
				dataSignNames: { id: "id", name: "name", parent: "parent", value: "value" }
			}
		},
		options);
		
		options = chartSupport.inflateRenderOptions(chart,
		{
			title:
			{
		        text: chart.name
		    },
			tooltip:
			{
				trigger: "item"
			},
			series:
			[
				//将在update中设置：
				//{}
				//设初值以免渲染报错
				{
					id: 0,
					type: "treemap"
				}
			]
		},
		options);
		
		chart.echartsInit(options);
	};
	
	chartSupport.treemapUpdate = function(chart, chartResult)
	{
		var renderOptions= chart.renderOptions();
		
		var options = { series: [ chartSupport.buildTreeNodeSeries(chart, chartResult, { id: 0, type: "treemap" }) ] };
		
		chartSupport.adaptArrayPropsForUpdateOptions(options, renderOptions);
		options = chart.inflateUpdateOptions(chartResult, options);
		
		chartSupport.echartsOptionsReplaceMerge(chart, options);
	};
	
	chartSupport.treemapResize = function(chart)
	{
		chartSupport.resizeChartEcharts(chart);
	};

	chartSupport.treemapDestroy = function(chart)
	{
		chartSupport.destroyChartEcharts(chart);
	};

	chartSupport.treemapOn = function(chart, eventType, handler)
	{
		chartSupport.bindChartEventHandlerForEcharts(chart, eventType, handler,
				chartSupport.treemapSetChartEventData);
	};
	
	chartSupport.treemapOff = function(chart, eventType, handler)
	{
		chart.echartsOffEventHandler(eventType, handler);
	};
	
	chartSupport.treemapSetChartEventData = function(chart, chartEvent, echartsEventParams)
	{
		var renderOptions= chart.renderOptions();
		var dg = renderOptions.dg;
		var dataSignNames = dg.dataSignNames;
		
		var echartsData = echartsEventParams.data;
		var data = {};
		
		//当事件在导航条时，echartsData为null
		if(echartsData)
		{
			data[dataSignNames.id] = echartsData.idOrigin;
			data[dataSignNames.name] = echartsData.name;
			data[dataSignNames.parent] = echartsData.parent;
			data[dataSignNames.value] = echartsData.value;
		}
		
		chart.eventData(chartEvent, data);
		chart.eventOriginalDataIndex(chartEvent, chart.originalDataIndex(echartsData));
		
		return data;
	};
	
	//旭日图
	
	chartSupport.sunburstRender = function(chart, options)
	{
		options = $.extend(true,
		{
			dg:
			{
				dataSignNames: { id: "id", name: "name", parent: "parent", value: "value" }
			}
		},
		options);
		
		options = chartSupport.inflateRenderOptions(chart,
		{
			title:
			{
		        text: chart.name
		    },
			tooltip:
			{
				trigger: "item"
			},
			series:
			[
				//将在update中设置：
				//{}
				//设初值以免渲染报错
				{
					id: 0,
					type: "sunburst"
				}
			]
		},
		options);
		
		chart.echartsInit(options);
	};
	
	chartSupport.sunburstUpdate = function(chart, chartResult)
	{
		var renderOptions= chart.renderOptions();
		
		var options = { series: [ chartSupport.buildTreeNodeSeries(chart, chartResult, { id: 0, type: "sunburst" }) ] };
		
		chartSupport.adaptArrayPropsForUpdateOptions(options, renderOptions);
		options = chart.inflateUpdateOptions(chartResult, options);
		
		chartSupport.echartsOptionsReplaceMerge(chart, options);
	};

	chartSupport.sunburstResize = function(chart)
	{
		chartSupport.resizeChartEcharts(chart);
	};
	
	chartSupport.sunburstDestroy = function(chart)
	{
		chartSupport.destroyChartEcharts(chart);
	};

	chartSupport.sunburstOn = function(chart, eventType, handler)
	{
		chartSupport.bindChartEventHandlerForEcharts(chart, eventType, handler,
				chartSupport.sunburstSetChartEventData);
	};
	
	chartSupport.sunburstOff = function(chart, eventType, handler)
	{
		chart.echartsOffEventHandler(eventType, handler);
	};
	
	chartSupport.sunburstSetChartEventData = function(chart, chartEvent, echartsEventParams)
	{
		var renderOptions= chart.renderOptions();
		var dg = renderOptions.dg;
		var dataSignNames = dg.dataSignNames;
		
		var echartsData = echartsEventParams.data;
		var data = {};
		
		data[dataSignNames.id] = echartsData.idOrigin;
		data[dataSignNames.name] = echartsData.name;
		data[dataSignNames.parent] = echartsData.parent;
		data[dataSignNames.value] = echartsData.value;
		
		chart.eventData(chartEvent, data);
		chart.eventOriginalDataIndex(chartEvent, chart.originalDataIndex(echartsData));
	};
	
	chartSupport.buildTreeNodeSeries = function(chart, chartResult, initSeries)
	{
		var renderOptions= chart.renderOptions();
		var dg = renderOptions.dg;
		var dataSignNames = dg.dataSignNames;
		
		initSeries = (initSeries || {});
		
		var dataSetBinds = chart.dataSetBindsMain();
		
		var seriesName = "";
		var seriesData = [];
		
		for(var i=0; i<dataSetBinds.length; i++)
		{
			var dataSetBind = dataSetBinds[i];
			
			var result = chart.resultOf(chartResult, dataSetBind);
			
			if(!seriesName)
				seriesName = chart.dataSetAlias(dataSetBind);
			
			var np = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.name);
			var ip = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.id);
			var pp = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.parent);
			var vp = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.value);
			
			var data = chart.resultDatas(result);
			
			for(var j=0; j<data.length; j++)
			{
				var node = {};
				
				node.name = chart.resultRowCell(data[j], np);
				node.idOrigin = (ip ? chart.resultRowCell(data[j], ip) : undefined);
				node.id = (ip ? node.idOrigin : node.name);
				node.parent = chart.resultRowCell(data[j], pp);
				if(vp)
				{
					node.value = chart.resultRowCell(data[j], vp);
					chartSupport.treeNodeEvalValueMark(node);
				}
				
				chart.originalDataIndex(node, dataSetBind, j);
				
				var added = false;
				for(var k=0; k<seriesData.length; k++)
				{
					if(chartSupport.treeAppendNode(seriesData[k], node))
					{
						added = true;
						break;
					}
				}
				
				if(!added)
					seriesData.push(node);
			}
		}
		
		initSeries = $.extend(initSeries, { name: seriesName, data: seriesData });
		
		return initSeries;
	};
	
	chartSupport.treeNodeEvalValueMark = function(node)
	{
		//标识节点值需要动态计算
		if(node.value == null || node.value == 0)
			node._evalValue = true;
	};
	
	chartSupport.treeAppendNode = function(treeNode, node)
	{
		if(!treeNode)
			return false;
		
		if(node.parent == treeNode.id)
		{
			if(!treeNode.children)
				treeNode.children = [];
			
			treeNode.children.push(node);
			
			//动态计算父节点的值
			if(treeNode._evalValue && typeof(node.value) == "number")
			{
				var treeNodeValue = (treeNode.value || 0);
				treeNode.value = treeNodeValue + node.value;
			}
			
			return true;
		}
		
		if(!treeNode.children)
			return false;
		
		for(var i=0; i<treeNode.children.length; i++)
		{
			if(chartSupport.treeAppendNode(treeNode.children[i], node))
			{
				//动态计算treeNode的值
				if(treeNode._evalValue && typeof(treeNode.children[i].value) == "number")
				{
					var treeNodeValue = (treeNode.value || 0);
					treeNode.value = treeNodeValue + treeNode.children[i].value;
				}
				
				return true;
			}
		}
		
		return false;
	};
	
	//桑基图

	chartSupport.sankeyRender = function(chart, options)
	{
		options = $.extend(true,
		{
			dg:
			{
				dataSignNames:
				{
					sourceName: "sourceName", sourceValue: "sourceValue",
					targetName: "targetName", targetValue: "targetValue",
					value: "value"
				},
				
				//同series[i].orient
				orient: "horizontal"
			}
		},
		options);
		
		options = chartSupport.inflateRenderOptions(chart,
		{
			title:
			{
		        text: chart.name
		    },
			tooltip:
			{
				trigger: "item"
			},
			series:
			[
				//将在update中设置：
				//{}
				//设初值以免渲染报错
				{
					id: 0,
					type: "sankey",
					//这里必须设置data、links，不然渲染会报错
					data: [],
					links: []
				}
			]
		},
		options);
		
		chart.echartsInit(options);
	};
	
	chartSupport.sankeyUpdate = function(chart, chartResult)
	{
		var renderOptions= chart.renderOptions();
		var dg = renderOptions.dg;
		var dataSignNames = dg.dataSignNames;
		
		var dataSetBinds = chart.dataSetBindsMain();
		
		var seriesName = "";
		var seriesData = [];
		var seriesLinks = [];
		
		for(var i=0; i<dataSetBinds.length; i++)
		{
			var dataSetBind = dataSetBinds[i];
			
			var result = chart.resultOf(chartResult, dataSetBind);
			
			if(!seriesName)
				seriesName = chart.dataSetAlias(dataSetBind);
			
			var snp = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.sourceName);
			var svp = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.sourceValue);
			var tnp = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.targetName);
			var tvp = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.targetValue);
			var vp = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.value);
			
			var data = chart.resultDatas(result);
			
			for(var j=0; j<data.length; j++)
			{
				var sd = { name: chart.resultRowCell(data[j], snp) };
				var td = { name: chart.resultRowCell(data[j], tnp) };
				
				if(svp)
					sd.value = chart.resultRowCell(data[j], svp);
				if(tvp)
					td.value = chart.resultRowCell(data[j], tvp);
				
				chart.originalDataIndex(sd, dataSetBind, j);
				
				var sidx = chartSupport.appendDistinct(seriesData, sd, "name");
				
				//新插入
				if(sidx == seriesData.length - 1 && seriesData[seriesData.length - 1] === sd)
				{
					chart.originalDataIndex(sd, dataSetBind, j);
				}
				
				var tidx = chartSupport.appendDistinct(seriesData, td, "name");
				
				//新插入
				if(tidx == seriesData.length - 1 && seriesData[seriesData.length - 1] === td)
				{
					chart.originalDataIndex(td, dataSetBind, j);
				}
				
				var link = {};
				link.source = sd.name;
				link.target = td.name;
				link.value = chart.resultRowCell(data[j], vp);
				
				link._sourceIndex = sidx;
				link._targetIndex = tidx;
				
				chart.originalDataIndex(link, dataSetBind, j);
				
				seriesLinks.push(link);
			}
		}
		
		var options = { series: [ { id: 0, type: "sankey", name: seriesName, data: seriesData, links: seriesLinks } ] };
		
		chartSupport.sankeyInflateUpdateOptions(chart, options, renderOptions);
		chartSupport.adaptArrayPropsForUpdateOptions(options, renderOptions);
		options = chart.inflateUpdateOptions(chartResult, options);
		
		chartFactory.extValueBuiltin(chart, "sankeySeriesData", seriesData);
		
		chartSupport.echartsOptionsReplaceMerge(chart, options);
	};
	
	chartSupport.sankeyResize = function(chart)
	{
		chartSupport.resizeChartEcharts(chart);
	};
	
	chartSupport.sankeyDestroy = function(chart)
	{
		chartSupport.destroyChartEcharts(chart);
	};

	chartSupport.sankeyOn = function(chart, eventType, handler)
	{
		chartSupport.bindChartEventHandlerForEcharts(chart, eventType, handler,
				chartSupport.sankeySetChartEventData);
	};
	
	chartSupport.sankeyOff = function(chart, eventType, handler)
	{
		chart.echartsOffEventHandler(eventType, handler);
	};
	
	chartSupport.sankeySetChartEventData = function(chart, chartEvent, echartsEventParams)
	{
		var renderOptions= chart.renderOptions();
		var dg = renderOptions.dg;
		var dataSignNames = dg.dataSignNames;
		
		var echartsData = echartsEventParams.data;
		var data = {};
		
		//TODO 点击节点没反应？？？
		//节点，仅使用源数据标记对象
		if(echartsEventParams.dataType == "node")
		{
			data[dataSignNames.sourceName] = echartsData.name;
			data[dataSignNames.sourceValue] = echartsData.value;
			
			chart.eventData(chartEvent, data);
			chart.eventOriginalDataIndex(chartEvent, chart.originalDataIndex(echartsData));
		}
		//边
		else if(echartsEventParams.dataType == "edge")
		{
			var seriesData = chartFactory.extValueBuiltin(chart, "sankeySeriesData");
			var sourceData = seriesData[echartsData._sourceIndex];
			var targetData = seriesData[echartsData._targetIndex];
			
			data[dataSignNames.sourceName] = sourceData.name;
			data[dataSignNames.sourceValue] = sourceData.value;
			
			if(targetData)
			{
				data[dataSignNames.targetName] = targetData.name;
				data[dataSignNames.targetValue] = targetData.value;
			}
			
			data[dataSignNames.value] = echartsData.value;
			
			chart.eventData(chartEvent, data);
			chart.eventOriginalDataIndex(chartEvent, chart.originalDataIndex(echartsData));
		}
	};
	
	chartSupport.sankeyInflateUpdateOptions = function(chart, updateOptions, renderOptions)
	{
		var seriesEle = updateOptions.series[0];
		var seriesEleExt = {};
		
		seriesEle.orient = renderOptions.dg.orient;
		
		if(renderOptions.dg.orient == "horizontal")
		{
			seriesEleExt =
			{
				left: "16%",
                right: "16%",
                top: "12%",
                bottom: "12%"
			};
		}
		else if(renderOptions.dg.orient == "vertical")
		{
			seriesEleExt =
			{
				label: { position: "top" },
                left: "12%",
                right: "12%",
                top: "16%",
                bottom: "16%"
			};
		}
		
		//自适应条目宽度和间隔
		var chartEle = chart.elementJquery();
		
		var totalWidth = (seriesEle.orient == "vertical" ? chartEle.height() : chartEle.width());
		nodeWidth = parseInt(totalWidth * 5/100);
		nodeWidth = (nodeWidth < 4 ? 4: nodeWidth);
		seriesEleExt.nodeWidth = nodeWidth;
		
		var totalWidth = (seriesEle.orient == "vertical" ? chartEle.width() : chartEle.height());
		nodeGap = parseInt(totalWidth * 2/100);
		nodeGap = (nodeWidth < 1 ? 1: nodeGap);
		seriesEleExt.nodeGap = nodeGap;
		
		$.extend(seriesEle, seriesEleExt);
	};
	
	//关系图
	
	chartSupport.graphRender = function(chart, options)
	{
		options = $.extend(true,
		{
			dg:
			{
				//map 可选，地图名
				dataSignNames:
				{
					sourceId: "sourceId", sourceName: "sourceName", sourceCategory: "sourceCategory", sourceValue: "sourceValue",
					targetId: "targetId", targetName: "targetName", targetCategory: "targetCategory", targetValue: "targetValue",
					value: "value"
				},
				//最大数据标记像素数
				symbolSizeMax: undefined,
				//最小数据标记像素数
				symbolSizeMin: undefined,
				//同series[i].layout
				layout: "force"
			}
		},
		options);
		
		options = chartSupport.inflateRenderOptions(chart,
		{
			title:
			{
		        text: chart.name
		    },
			tooltip:
			{
				trigger: "item"
			},
			legend:
			{
				id: 0,
				//将在update中设置：
				//data
			},
			series:
			[
				//将在update中设置：
				//{}
				//设初值以免渲染报错
				{
					id: 0,
					type: "graph"
				}
			]
		},
		options);
		
		chart.echartsInit(options);
	};
	
	chartSupport.graphUpdate = function(chart, chartResult)
	{
		var renderOptions= chart.renderOptions();
		var dg = renderOptions.dg;
		var dataSignNames = dg.dataSignNames;
		
		var dataSetBinds = chart.dataSetBindsMain();
		
		var legendData = [];
		var seriesName = "";
		var categories = [];
		var seriesData = [];
		var seriesLinks = [];
		
		var min = undefined, max = undefined;
		var symbolSizeMax = chartSupport.evalSymbolSizeMax(chart, renderOptions);
		var symbolSizeMin = chartSupport.evalSymbolSizeMin(chart, renderOptions, symbolSizeMax);
		
		for(var i=0; i<dataSetBinds.length; i++)
		{
			var dataSetBind = dataSetBinds[i];
			
			var result = chart.resultOf(chartResult, dataSetBind);
			
			if(!seriesName)
				seriesName = chart.dataSetAlias(dataSetBind);
			
			var sip = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.sourceId);
			var snp = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.sourceName);
			var scp = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.sourceCategory);
			var svp = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.sourceValue);
			var tip = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.targetId);
			var tnp = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.targetName);
			var tcp = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.targetCategory);
			var tvp = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.targetValue);
			var vp = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.value);
			
			var data = chart.resultDatas(result);
			
			for(var j=0; j<data.length; j++)
			{
				var sd = { name: chart.resultRowCell(data[j], snp) };
				var td = { name: chart.resultRowCell(data[j], tnp) };
				
				if(sip)
					sd.id = chart.resultRowCell(data[j], sip);
				
				if(scp)
				{
					var category = chart.resultRowCell(data[j], scp);
					sd._categoryOrigin = category;
					if(category)
					{
						sd.category = chartSupport.appendDistinct(categories, {name: category}, "name");
						chartSupport.appendDistinct(legendData, category);
					}
				}
				
				if(svp)
				{
					sd.value = chart.resultRowCell(data[j], svp);
					
					min = (min == null ? sd.value : Math.min(min, sd.value));
					max = (max == null ? sd.value : Math.max(max, sd.value));
				}
				
				if(tip)
					td.id = chart.resultRowCell(data[j], tip);
				
				if(tcp)
				{
					var category = chart.resultRowCell(data[j], tcp);
					td._categoryOrigin = category;
					if(category)
					{
						td.category = chartSupport.appendDistinct(categories, {name: category}, "name");
						chartSupport.appendDistinct(legendData, category);
					}
				}
				
				if(tvp)
				{
					td.value = chart.resultRowCell(data[j], tvp);
					
					min = (min == null ? td.value : Math.min(min, td.value));
					max = (max == null ? td.value : Math.max(max, td.value));
				}
				
				var sidx = chartSupport.appendDistinct(seriesData, sd, (sip ? "id" : "name"));
				
				//新插入
				if(sidx == seriesData.length - 1 && seriesData[seriesData.length - 1] === sd)
				{
					chart.originalDataIndex(sd, dataSetBind, j);
				}
				
				var tidx = chartSupport.appendDistinct(seriesData, td, (tip ? "id" : "name"));
				
				//新插入
				if(tidx == seriesData.length - 1 && seriesData[seriesData.length - 1] === td)
				{
					chart.originalDataIndex(td, dataSetBind, j);
				}
				
				//如果使用id值表示关系，对于数值型id，echarts会误当做数据索引，所以这里直接使用数据索引
				var link = {};
				link.source = sidx;
				link.target = tidx;
				
				if(vp)
					link.value = chart.resultRowCell(data[j], vp);
				
				chart.originalDataIndex(link, dataSetBind, j);
				
				seriesLinks.push(link);
			}
		}
		
		if(min == null && max == null && symbolSizeMin < 10)
			symbolSizeMin = 10;
		
		var series = [ { id: 0, type: "graph", name: seriesName, categories: categories, data: seriesData, links: seriesLinks } ];
		
		var options = { legend: {id: 0, data: legendData}, series: series };
		chartSupport.graphInflateUpdateOptions(chart, options, min, max, symbolSizeMax, symbolSizeMin, renderOptions);
		chartSupport.adaptArrayPropsForUpdateOptions(options, renderOptions);
		options = chart.inflateUpdateOptions(chartResult, options);
		
		chartFactory.extValueBuiltin(chart, "graphSeriesData", seriesData);
		
		chartSupport.echartsOptionsReplaceMerge(chart, options);
	};

	chartSupport.graphResize = function(chart)
	{
		chartSupport.resizeChartEcharts(chart);
	};
	
	chartSupport.graphDestroy = function(chart)
	{
		chartSupport.destroyChartEcharts(chart);
	};

	chartSupport.graphOn = function(chart, eventType, handler)
	{
		chartSupport.bindChartEventHandlerForEcharts(chart, eventType, handler,
				chartSupport.graphSetChartEventData);
	};
	
	chartSupport.graphOff = function(chart, eventType, handler)
	{
		chart.echartsOffEventHandler(eventType, handler);
	};
	
	chartSupport.graphSetChartEventData = function(chart, chartEvent, echartsEventParams)
	{
		var renderOptions= chart.renderOptions();
		var dg = renderOptions.dg;
		var dataSignNames = dg.dataSignNames;
		
		var echartsData = echartsEventParams.data;
		
		var data = {};
		
		//节点，仅使用源数据标记对象
		if(echartsEventParams.dataType == "node")
		{
			data[dataSignNames.sourceId] = echartsData.id;
			data[dataSignNames.sourceName] = echartsData.name;
			data[dataSignNames.sourceCategory] = echartsData._categoryOrigin;
			data[dataSignNames.sourceValue] = echartsData.value;

			chart.eventData(chartEvent, data);
			chart.eventOriginalDataIndex(chartEvent, chart.originalDataIndex(echartsData));
		}
		//边
		else if(echartsEventParams.dataType == "edge")
		{
			var seriesData = chartFactory.extValueBuiltin(chart, "graphSeriesData");
			var sourceData = seriesData[echartsData.source];
			var targetData = seriesData[echartsData.target];
			
			data[dataSignNames.sourceId] = sourceData.id;
			data[dataSignNames.sourceName] = sourceData.name;
			data[dataSignNames.sourceCategory] = sourceData._categoryOrigin;
			data[dataSignNames.sourceValue] = sourceData.value;
			
			if(targetData)
			{
				data[dataSignNames.targetId] = targetData.id;
				data[dataSignNames.targetName] = targetData.name;
				data[dataSignNames.targetCategory] = targetData._categoryOrigin;
				data[dataSignNames.targetValue] = targetData.value;
			}
			
			data[dataSignNames.value] = echartsData.value;
			
			chart.eventData(chartEvent, data);
			chart.eventOriginalDataIndex(chartEvent, chart.originalDataIndex(echartsData));
		}
	};
	
	chartSupport.graphInflateUpdateOptions = function(chart, updateOptions, min, max, symbolSizeMax, symbolSizeMin, renderOptions)
	{
		var seriesEle = updateOptions.series[0];
		
		seriesEle.layout = renderOptions.dg.layout;
		
		if(seriesEle.layout == "force")
		{
			seriesEle.draggable = true;
			seriesEle.force = {};
			//自动计算散点间距
			seriesEle.force.edgeLength = parseInt(symbolSizeMax*1.5);
			//自动计算散点稀疏度
			seriesEle.force.repulsion = parseInt(symbolSizeMax*2);
		}
		
		chartSupport.evalSeriesDataValueSymbolSize(seriesEle, min, max, symbolSizeMax, symbolSizeMin);
	};
	
	//箱型图
	
	chartSupport.boxplotRender = function(chart, options)
	{
		options = $.extend(true,
		{
			dg:
			{
				//name 名称
				//value 数值
				dataSignNames:
				{
					name: "name", min: "min", lower: "lower",
					median: "median", upper: "upper", max: "max", value: "value",
					category: "category"
				},
				//是否横向
				horizontal: false,
				//最大数据标记像素数
				symbolSizeMax: undefined,
				//最小数据标记像素数
				symbolSizeMin: undefined,
			}
		},
		options);
		
		var dataSignNames = options.dg.dataSignNames;
		var dataSetBind = chartSupport.dataSetBindMainNonNull(chart);
		var np = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.name);
		
		options = chartSupport.inflateRenderOptions(chart,
		{
			title:
			{
		        text: chart.name
		    },
			tooltip:
			{
				trigger: "item"
			},
			legend:
			{
				id: 0,
				//将在update中设置：
				//data
			},
			xAxis:
			{
				id: 0,
				name: chart.dataSetFieldAlias(dataSetBind, np),
				nameGap: 5,
				type: chartSupport.evalDataSetFieldAxisType(chart, np),
				boundaryGap: true,
				splitLine: { show: false }
			},
			yAxis:
			{
				id: 0,
				name: "",
				nameGap: 5,
				type: "value"
			},
			series:
			[
				//将在update中设置：
				//{}
				//设初值以免渲染报错
				{
					id: 0,
					type: "boxplot"
				}
			]
		},
		options,
		function(options)
		{
			//箱形图的angleAxis.type不能为value和time，不然图形无法显示
			if(options.xAxis.type == "value" || options.xAxis.type == "time")
				options.xAxis.type = "category";
			
			if(options.dg.horizontal)
			{
				var xAxisTmp = options.xAxis;
				options.xAxis = options.yAxis;
				options.yAxis = xAxisTmp;
			}
		});
		
		chart.echartsInit(options);
	};
	
	chartSupport.boxplotUpdate = function(chart, chartResult)
	{
		var renderOptions= chart.renderOptions();
		var dg = renderOptions.dg;
		var dataSignNames = dg.dataSignNames;
		
		var dataSetBinds = chart.dataSetBindsMain();
		
		var legendData = [];
		var series = [];
		
		var symbolSizeMax = chartSupport.evalSymbolSizeMax(chart, renderOptions);
		var symbolSizeMin = chartSupport.evalSymbolSizeMin(chart, renderOptions, symbolSizeMax);
		
		for(var i=0; i<dataSetBinds.length; i++)
		{
			var dataSetBind = dataSetBinds[i];
			var dataSetAlias = chart.dataSetAlias(dataSetBind);
			var result = chart.resultOf(chartResult, dataSetBind);
			
			var np = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.name);
			var minp = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.min);
			var cp = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.category);
			
			//箱形数据集
			if(minp)
			{
				var vp =
				[
					minp,
					chart.dataSetFieldOfSign(dataSetBind, dataSignNames.lower),
					chart.dataSetFieldOfSign(dataSetBind, dataSignNames.median),
					chart.dataSetFieldOfSign(dataSetBind, dataSignNames.upper),
					chart.dataSetFieldOfSign(dataSetBind, dataSignNames.max)
				];
				var propertyMap = { name: np, value: vp };
				if(cp)
					propertyMap = chartSupport.inflatePropertyMapWithCategory(propertyMap, cp);
				
				var data = chart.resultMapObjects(result, propertyMap);
				chart.originalDataIndexes(data, dataSetBind);
				
				if(cp)
				{
					var categoryNames = [];
					var categoryDatasMap = {};
					
					chartSupport.splitDataByCategory(data, categoryNames, categoryDatasMap);
					
					for(var j=0; j<categoryNames.length; j++)
					{
						var categoryName = categoryNames[j];
						var legendName = chartSupport.legendNameForDataCategory(dataSetBinds, dataSetAlias, categoryName);
						var mySeries = {id: series.length, type: "boxplot", name: legendName, data: categoryDatasMap[categoryName]};
						
						legendData.push(legendName);
						series.push(mySeries);
					}
				}
				else
				{
					legendData.push(dataSetAlias);
					series.push({ id: series.length, type: "boxplot", name: dataSetAlias, data: data });
				}
			}
			//异常值数据集
			else
			{
				if(cp)
				{
					var vp = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.value);
					
					var categoryNames = [];
					var categoryDatasMap = {};
					
					//使用{value: [name,value]}格式可以更好地兼容category、value、time坐标轴类型
					var propertyMap = { "value": [np, vp] }; 
					propertyMap = chartSupport.inflatePropertyMapWithCategory(propertyMap, cp);
					
					var data = chart.resultMapObjects(result, propertyMap);
					chartSupport.evalDataValueSymbolSize(data, 1, 1, symbolSizeMax, symbolSizeMin);
					chart.originalDataIndexes(data, dataSetBind);
					chartSupport.splitDataByCategory(data, categoryNames, categoryDatasMap);
					
					for(var j=0; j<categoryNames.length; j++)
					{
						var categoryName = categoryNames[j];
						var legendName = chartSupport.legendNameForDataCategory(dataSetBinds, dataSetAlias, categoryName);
						var mySeries = {id: series.length, type: "scatter", name: legendName, data: categoryDatasMap[categoryName]};
						
						if(dg.horizontal)
						{
							mySeries.encode = { x: 1, y: 0 };
						}
						
						legendData.push(legendName);
						series.push(mySeries);
					}
				}
				else
				{
					var vps = chart.dataSetFieldsOfSign(dataSetBind, dataSignNames.value);
					
					for(var j=0; j<vps.length; j++)
					{
						var legendName = chartSupport.legendNameForDataValues(chart, dataSetBinds, dataSetBind, dataSetAlias, vps, j);
						var vpsMy = [np, vps[j]];
						var data = chart.resultValueObjects(result, vpsMy);
						chartSupport.evalDataValueSymbolSize(data, 1, 1, symbolSizeMax, symbolSizeMin);
						chart.originalDataIndexes(data, dataSetBind);
						
						var mySeries = { id: series.length, type: "scatter", name: legendName, data: data };
						
						if(dg.horizontal)
						{
							mySeries.encode = { x: 1, y: 0 };
						}
						
						legendData.push(legendName);
						series.push(mySeries);
					}
				}
			}
		}
		
		var options = { legend: {id: 0, data: legendData}, series: series };
		
		//需要设置坐标值，不然刻度会错乱
		if(dg.horizontal)
			options.yAxis = { id: 0 };
		else
			options.xAxis = { id: 0 };
		
		chartSupport.inflateAxisDataForEchartsUpdateOptions(renderOptions, options, (dg.horizontal ? options.yAxis : options.xAxis),
						{
							get: function(s)
							{
								if(s.type == "boxplot")
									return chartSupport.inflateAxisDataExtractors.property("name");
								else
									return chartSupport.inflateAxisDataExtractors.valueElement(0);
							}
						});
		
		chartSupport.adaptArrayPropsForUpdateOptions(options, renderOptions);
		options = chart.inflateUpdateOptions(chartResult, options);
		
		chartSupport.echartsOptionsReplaceMerge(chart, options);
	};
	
	chartSupport.boxplotResize = function(chart)
	{
		chartSupport.resizeChartEcharts(chart);
	};
	
	chartSupport.boxplotDestroy = function(chart)
	{
		chartSupport.destroyChartEcharts(chart);
	};
	
	chartSupport.boxplotOn = function(chart, eventType, handler)
	{
		chartSupport.bindChartEventHandlerForEcharts(chart, eventType, handler,
				chartSupport.boxplotSetChartEventData);
	};
	
	chartSupport.boxplotOff = function(chart, eventType, handler)
	{
		chart.echartsOffEventHandler(eventType, handler);
	};
	
	chartSupport.boxplotSetChartEventData = function(chart, chartEvent, echartsEventParams)
	{
		var renderOptions= chart.renderOptions();
		var dg = renderOptions.dg;
		var dataSignNames = dg.dataSignNames;
		var categoryPropName = chartSupport.builtinCategoryPropName();
		
		var seriesType = echartsEventParams.seriesType;
		var echartsData = (echartsEventParams.data || {});
		var echartsValue = (echartsData.value || []);
		var data = {};
		
		//箱形系列
		if(seriesType == "boxplot")
		{
			//value的第一个元素是数据索引
			var startIdx = (echartsValue.length > 5 ? 1 : 0);
			
			data[dataSignNames.name] = echartsData.name;
			data[dataSignNames.min] = echartsValue[startIdx];
			data[dataSignNames.lower] = echartsValue[startIdx+1];
			data[dataSignNames.median] = echartsValue[startIdx+2];
			data[dataSignNames.upper] = echartsValue[startIdx+3];
			data[dataSignNames.max] = echartsValue[startIdx+4];
		}
		//异常值系列
		else
		{
			data[dataSignNames.name] = echartsValue[0];
			data[dataSignNames.value] = echartsValue[1];
		}
		data[dataSignNames.category] = (echartsData && echartsData[categoryPropName] != null ?
											echartsData[categoryPropName] : undefined);
		
		chart.eventData(chartEvent, data);
		chart.eventOriginalDataIndex(chartEvent, chart.originalDataIndex(echartsData));
	};
	
	//词云图
	
	chartSupport.wordcloudRender = function(chart, options)
	{
		//不支持在echarts主题中设置样式，只能在这里设置
		var chartTheme = chart.theme();
		
		options = $.extend(true,
		{
			dg:
			{
				//name 名称
				//value 数值
				dataSignNames: { name: "name", value: "value" },
				//由低到高值域颜色映射
				colorRange: chartTheme.graphRangeColors,
				//由低到高值渐变色数组，如果不设置，将由colorRange自动计算
				colorGradients: undefined
			}
		},
		options);
		
		options = chartSupport.inflateRenderOptions(chart,
		{
			title: {
		        text: chart.name
		    },
			tooltip:
			{
				trigger: "item"
			},
			series:
			[
				{
					//将在update中设置：
					//name
					//data
					//这里必须设置data，不然渲染会报错
					data: [],
					
					id: 0,
					type: "wordCloud",
					shape: "circle",
					"textStyle": { "color": chartTheme.color },
					"emphasis":
					{
						"focus": "self",
						"textStyle":
						{
							//echarts-wordcloud-2.0.0版本有BUG，shadowBlur不起作用，
							//所以这里采用fontWeight效果
							"fontWeight": "bold",
							"shadowBlur" : 10,
							"shadowColor" : chart.themeGradualColor(0.9)
						}
					}
				}
			]
		},
		options,
		function(options)
		{
			var chartEle = chart.elementJquery();
			
			//自适应字体大小
			var chartSize = Math.min(chartEle.height(), chartEle.width());
			var sizeRange = [parseInt(chartSize * 1/40), parseInt(chartSize * 1/8)];
			sizeRange[0] = (sizeRange[0] < 6 ? 6: sizeRange[0]);
			options.series[0].sizeRange = sizeRange;
			
			//计算渐变色
			var colorRange = options.dg.colorRange;
			var colorGradients = [];
			for(var i=0; i<colorRange.length; i++)
			{
				var fromColor = colorRange[i];
				var toColor = ((i+1) < colorRange.length ? colorRange[i+1] : null);
				
				if(!toColor)
					break;
				
				colorGradients = colorGradients.concat(chartFactory.evalGradualColors(fromColor, toColor, 5));
			}
			options.dg.colorGradients = colorGradients;
		});
		
		chart.echartsInit(options);
	};
	
	chartSupport.wordcloudUpdate = function(chart, chartResult)
	{
		var renderOptions= chart.renderOptions();
		var dg = renderOptions.dg;
		var dataSignNames = dg.dataSignNames;
		
		var dataSetBinds = chart.dataSetBindsMain();
		
		var seriesName = "";
		var seriesData = [];
		var dataRange = { min: undefined, max: undefined };
		
		for(var i=0; i<dataSetBinds.length; i++)
		{
			var dataSetBind = dataSetBinds[i];
			var result = chart.resultOf(chartResult, dataSetBind);
			
			var np = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.name);
			var vp = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.value);
			
			var data = chart.resultNameValueObjects(result, np, vp);
			
			chart.originalDataIndexes(data, dataSetBind);
			chartSupport.evalArrayDataRange(dataRange, data, "value");
			
			seriesData = seriesData.concat(data);
		}
		
		dataRange.min = (dataRange.min >= dataRange.max ? dataRange.max - 1 : dataRange.min);
		
		//映射颜色值
		var colorGradients = dg.colorGradients;
		if(colorGradients)
		{
			for(var i=0; i<seriesData.length; i++)
			{
				var colorIndex = parseInt((seriesData[i].value-dataRange.min)/(dataRange.max-dataRange.min) * (colorGradients.length-1));
				seriesData[i].textStyle = { "color": colorGradients[colorIndex] };
			}
		}
		
		var options = { series: [ {id: 0, type: "wordCloud", name: seriesName, data: seriesData} ] };
		
		chartSupport.adaptArrayPropsForUpdateOptions(options, renderOptions);
		options = chart.inflateUpdateOptions(chartResult, options);
		
		chartSupport.echartsOptionsReplaceMerge(chart, options);
	};
	
	chartSupport.wordcloudResize = function(chart)
	{
		chartSupport.resizeChartEcharts(chart);
	};
	
	chartSupport.wordcloudDestroy = function(chart)
	{
		chartSupport.destroyChartEcharts(chart);
	};
	
	chartSupport.wordcloudOn = function(chart, eventType, handler)
	{
		chartSupport.bindChartEventHandlerForEcharts(chart, eventType, handler,
				chartSupport.wordcloudSetChartEventData);
	};
	
	chartSupport.wordcloudOff = function(chart, eventType, handler)
	{
		chart.echartsOffEventHandler(eventType, handler);
	};
	
	chartSupport.wordcloudSetChartEventData = function(chart, chartEvent, echartsEventParams)
	{
		var renderOptions= chart.renderOptions();
		var dg = renderOptions.dg;
		var dataSignNames = dg.dataSignNames;
		
		var echartsData = echartsEventParams.data;
		var data = {};
		
		data[dataSignNames.name] = echartsData.name;
		data[dataSignNames.value] = echartsData.value;
		
		chart.eventData(chartEvent, data);
		chart.eventOriginalDataIndex(chartEvent, chart.originalDataIndex(echartsData));
	};
	
	//水球图
	
	chartSupport.liquidfillRender = function(chart, options)
	{
		options = $.extend(true,
		{
			dg:
			{
				//name 名称
				//value 数值
				dataSignNames: { name: "name", value: "value" },
				//同series[i].shape
				shape: "circle",
				//如果仅有一个波浪数据，则自动复制扩充至这些个波浪数据
				autoInflateWave: 3
			}
		},
		options);
		
		//不支持在echarts主题中设置样式，只能在这里设置
		var chartTheme = chart.theme();
		
		options = chartSupport.inflateRenderOptions(chart,
		{
			title:
			{
		        text: chart.name
		    },
			tooltip:
			{
				trigger: "item"
			},
			series:
			[
				{
					//将在update中设置：
					//data
					//这里必须设置data，不然渲染会报错
					data: [],
					
					id: 0,
					type: "liquidFill",
					radius: "75%",
					color: ['#294D99', '#156ACF', '#1598ED', '#45BDFF'],
					backgroundStyle:
					{
						color: "transparent"
					},
					outline:
					{
						itemStyle:
						{
							borderColor: chartTheme.borderColor,
							shadowColor: chart.themeGradualColor(0.4)
						}
					},
					label:
					{
						color: chartTheme.color
					}
				}
			]
		},
		options);
		
		chart.echartsInit(options);
	};
	
	chartSupport.liquidfillUpdate = function(chart, chartResult)
	{
		var renderOptions= chart.renderOptions();
		var dg = renderOptions.dg;
		var dataSignNames = dg.dataSignNames;
		
		var dataSetBinds = chart.dataSetBindsMain();
		
		var seriesData = [];
		
		for(var i=0; i<dataSetBinds.length; i++)
		{
			var dataSetBind = dataSetBinds[i];
			
			var result = chart.resultOf(chartResult, dataSetBind);
			
			var nps = chart.dataSetFieldsOfSign(dataSetBind, dataSignNames.name);
			var vps = chart.dataSetFieldsOfSign(dataSetBind, dataSignNames.value);
			var npsNone = (nps==null || nps.length==0);
			
			if(!npsNone && nps.length!=vps.length)
				throw new Error("The ["+dataSignNames.name+"] sign column must be "
						+"one-to-one with ["+dataSignNames.value+"] sign column");
			
			var data = [];
			
			if(npsNone)
			{
				var ras = chart.resultRowArrays(result, vps);
				for(var j=0; j<ras.length; j++)
				{
					var ra = ras[j];
					for(var k=0; k<ra.length; k++)
					{
						var sv = { name: chart.dataSetFieldAlias(dataSetBind, vps[k]), value: ra[k] };
						chart.originalDataIndex(sv, dataSetBind, j);
						data.push(sv);
					}
				}
			}
			else
			{
				var namess = chart.resultRowArrays(result, nps);
				var valuess = chart.resultRowArrays(result, vps);
				
				for(var j=0; j<namess.length; j++)
				{
					var names = namess[j];
					var values = valuess[j];
					
					for(var k=0; k<names.length; k++)
					{
						var sv = { name: names[k], value: values[k] };
						chart.originalDataIndex(sv, dataSetBind, j);
						data.push(sv);
					}
				}
			}
			
			seriesData = seriesData.concat(data);
		}
		
		//如果仅有一个波浪，则自动扩充
		if(seriesData.length == 1 && dg.autoInflateWave > 1)
		{
			for(var i=1; i<dg.autoInflateWave; i++)
			{
				var inflateValue = $.extend({}, seriesData[0]);
				seriesData.push(inflateValue);
			}
		}
		
		var options = { series: [ {id: 0, type: "liquidFill", data: seriesData, shape: dg.shape } ] };
		
		chartSupport.adaptArrayPropsForUpdateOptions(options, renderOptions);
		options = chart.inflateUpdateOptions(chartResult, options);
		
		chartSupport.echartsOptionsReplaceMerge(chart, options);
	};
	
	chartSupport.liquidfillResize = function(chart)
	{
		chartSupport.resizeChartEcharts(chart);
	};
	
	chartSupport.liquidfillDestroy = function(chart)
	{
		chartSupport.destroyChartEcharts(chart);
	};
	
	chartSupport.liquidfillOn = function(chart, eventType, handler)
	{
		chartSupport.bindChartEventHandlerForEcharts(chart, eventType, handler,
				chartSupport.liquidfillSetChartEventData);
	};
	
	chartSupport.liquidfillOff = function(chart, eventType, handler)
	{
		chart.echartsOffEventHandler(eventType, handler);
	};
	
	chartSupport.liquidfillSetChartEventData = function(chart, chartEvent, echartsEventParams)
	{
		var renderOptions= chart.renderOptions();
		var dg = renderOptions.dg;
		var dataSignNames = dg.dataSignNames;
		
		var echartsData = echartsEventParams.data;
		var data = {};
		
		data[dataSignNames.name] = echartsData.name;
		data[dataSignNames.value] = echartsData.value;
		
		chart.eventData(chartEvent, data);
		chart.eventOriginalDataIndex(chartEvent, chart.originalDataIndex(echartsData));
	};
	
	//平行坐标系
	
	chartSupport.parallelRender = function(chart, options)
	{
		options = $.extend(true,
		{
			dg:
			{
				//name 可选，一条平行线名称
				//value 必选，可多选，平行线指标
				//category 可选，平行线类别
				dataSignNames: { name: "name", value: "value", category: "category" },
				//是否平滑
				smooth: false
			}
		},
		options);
		
		options = chartSupport.inflateRenderOptions(chart,
		{
			title:
			{
		        text: chart.name
		    },
			tooltip:
			{
				show: true
			},
			legend:
			{
				id: 0,
				//将在update中设置：
				//data
			},
			parallelAxis:  [],
			series:
			[
				//将在update中设置：
				//{}
				//设初值以免渲染报错
				{
					id: 0,
					type: "parallel"
				}
			]
		},
		options);
		
		chart.echartsInit(options);
	};
	
	chartSupport.parallelUpdate = function(chart, chartResult)
	{
		var renderOptions= chart.renderOptions();
		var dg = renderOptions.dg;
		var dataSignNames = dg.dataSignNames;
		var parallelAxis = chartSupport.parallelEvalParallelAxis(chart);
		var valuePropertyNamess = chartSupport.parallelEvalValuePropertyNamess(chart, parallelAxis);
		
		var dataSetBinds = chart.dataSetBindsMain();
		var categoryNames = [];
		var categoryDatasMap = {};
		
		for(var i=0; i<dataSetBinds.length; i++)
		{
			var dataSetBind = dataSetBinds[i];
			var dataSetAlias = chart.dataSetAlias(dataSetBind);
			var result = chart.resultOf(chartResult, dataSetBind);
			
			var cp = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.category);
			
			var propertyMap =
			{
				"name": chart.dataSetFieldOfSign(dataSetBind, dataSignNames.name),
				"value": valuePropertyNamess[i]
			};
			
			if(cp)
				propertyMap = chartSupport.inflatePropertyMapWithCategory(propertyMap, cp);
			
			var data = chart.resultMapObjects(result, propertyMap);
			
			chart.originalDataIndexes(data, dataSetBind);
			
			if(cp)
				chartSupport.splitDataByCategory(data, categoryNames, categoryDatasMap);
			else
				chartSupport.appendCategoryNameAndData(categoryNames, categoryDatasMap, dataSetAlias, data);
			
			//设置每个坐标系的min、max、data
			for(var j=0; j<data.length; j++)
			{
				var vs = (data[j].value || []);
				
				for(var k=0; k<parallelAxis.length; k++)
				{
					var paxis = parallelAxis[k];
					var pv = vs[k];
					
					if(paxis.type == "category")
					{
						paxis.data = (paxis.data || (paxis.data = []));
						if(pv != null)
							chartSupport.appendDistinct(paxis.data, pv);
					}
					else
					{
						if(pv != null)
						{
							//设置min、max，不然当多系列时不能自动识别，可能导致某些线飞离
							if(paxis.min == null)
								paxis.min = pv;
							else if(paxis.min > pv)
								paxis.min = pv;
							
							if(paxis.max == null)
								paxis.max = pv;
							else if(paxis.max < pv)
								paxis.max = pv;
						}
					}
				}
			}
		}
		
		var series = [];
		
		for(var i=0; i<categoryNames.length; i++)
		{
			series[i] =
			{
				id: series.length,
				type: "parallel",
				name: categoryNames[i],
				data: categoryDatasMap[categoryNames[i]]
			};
			
			if(dg.smooth)
				series[i].smooth = true;
		}
		
		var options = { legend: {id: 0, data: categoryNames}, parallelAxis: parallelAxis, series: series };
		
		chartSupport.parallelTrimAxisMinMax(options);
		chartSupport.adaptArrayPropsForUpdateOptions(options, renderOptions);
		options = chart.inflateUpdateOptions(chartResult, options);
		
		chartSupport.echartsOptionsReplaceMerge(chart, options);
	};
	
	chartSupport.parallelResize = function(chart)
	{
		chartSupport.resizeChartEcharts(chart);
	};
	
	chartSupport.parallelDestroy = function(chart)
	{
		chartSupport.destroyChartEcharts(chart);
	};
	
	chartSupport.parallelOn = function(chart, eventType, handler)
	{
		chartSupport.bindChartEventHandlerForEcharts(chart, eventType, handler,
				chartSupport.parallelSetChartEventData);
	};
	
	chartSupport.parallelOff = function(chart, eventType, handler)
	{
		chart.echartsOffEventHandler(eventType, handler);
	};
	
	chartSupport.parallelSetChartEventData = function(chart, chartEvent, echartsEventParams)
	{
		var renderOptions= chart.renderOptions();
		var dg = renderOptions.dg;
		var dataSignNames = dg.dataSignNames;
		
		var echartsData = echartsEventParams.data;
		
		var data = undefined;
		
		if(echartsData)
		{
			var categoryPropertyName = chartSupport.builtinCategoryPropName();;
			
			data = {};
			data[dataSignNames.name] = echartsData.name;
			data[dataSignNames.value] = echartsData.value;
			data[dataSignNames.category] = echartsData[categoryPropertyName];
		}
		
		chart.eventData(chartEvent, data);
		chart.eventOriginalDataIndex(chartEvent, chart.originalDataIndex(echartsData));
	};
	
	chartSupport.parallelEvalParallelAxis = function(chart)
	{
		var renderOptions= chart.renderOptions();
		var dg = renderOptions.dg;
		var dataSignNames = dg.dataSignNames;
		
		var parallelAxis = [];
		
		var dataSetBinds = chart.dataSetBindsMain();
		
		for(var i=0; i<dataSetBinds.length; i++)
		{
			var dataSetBind = dataSetBinds[i];
			var vps = chart.dataSetFieldsOfSign(dataSetBind, dataSignNames.value);
			
			for(var j=0; j<vps.length; j++)
			{
				var vp = vps[j];
				//使用alias而非name作为坐标轴名，因为alias是可编辑得，使得用户可以自定义坐标轴
				var axisName = chart.dataSetFieldAlias(dataSetBind, vp);
				
				if(chartSupport.findInArray(parallelAxis, axisName, "name") < 0)
				{
					var axis =
					{
						name: axisName,
						type: chartSupport.evalDataSetFieldAxisType(chart, vp),
						nameGap: 5
					};
					
					parallelAxis.push(axis);
					
					/*禁用跨数据集排序，增加了排序概念的复杂性（因为其他图表都不支持），也不太有必要（数据集本身有排序支持）
					if(i == 0)
						parallelAxis.push(axis);
					else
					{
						//后续数据集字段按照order插入到parallelAxis的适当位置，
						//使得在多数据集情况时，也可自由调整坐标轴的顺序
						var order = chart.dataSetFieldOrder(dataSetBind, vp);
						if(order != null)
						{
							if(order < 0)
								parallelAxis.unshift(axis);
							else if(order >= 0 && order < parallelAxis.length)
								parallelAxis.splice(order, 0, axis);
							else
								parallelAxis.push(axis);
						}
						else
							parallelAxis.push(axis);
					}
					*/
				}
			}
		}
		
		for(var i=0; i<parallelAxis.length; i++)
		{
			parallelAxis[i].id = i;
			parallelAxis[i].dim = i;
		}
		
		return parallelAxis;
	};
	
	chartSupport.parallelEvalValuePropertyNamess = function(chart, parallelAxis)
	{
		var renderOptions= chart.renderOptions();
		var dg = renderOptions.dg;
		var dataSignNames = dg.dataSignNames;
		
		var valuePropertyNamess = [];
		
		var placeholderName = chartFactory.builtinPropName("DataPropNamePlaceholder");
		var dataSetBinds = chart.dataSetBindsMain();
		
		for(var i=0; i<dataSetBinds.length; i++)
		{
			var dataSetBind = dataSetBinds[i];
			var valuePropertyNames = [];
			
			var vps = chart.dataSetFieldsOfSign(dataSetBind, dataSignNames.value);
			
			for(var j=0; j<parallelAxis.length; j++)
			{
				var idx = chartSupport.findInArray(vps, parallelAxis[j].name,
							function(vp)
							{
								return chart.dataSetFieldAlias(dataSetBind, vp);
							});
				
				valuePropertyNames[j] = (idx < 0 ? placeholderName : vps[idx].name);
			}
			
			valuePropertyNamess[i] = valuePropertyNames;
		}
		
		return valuePropertyNamess;
	};
	
	chartSupport.parallelTrimAxisMinMax = function(options)
	{
		var parallelAxis = (options.parallelAxis || []);
		var series = (options.series || []);
		
		for(var i=0; i<parallelAxis.length; i++)
		{
			var pa = parallelAxis[i];
			
			//单系列ECharts会自动计算min、max，这里不必设置
			if(series.length < 2)
			{
				pa.min = undefined;
				pa.max = undefined;
			}
			//多系列ECharts不会自动计算，需要手动计算
			else
			{
				chartSupport.trimNumberRange(pa);
			}
		}
	};
	
	//主题河流图
	
	chartSupport.themeRiverRender = function(chart, options)
	{
		options = $.extend(true,
		{
			dg:
			{
				//name 河流X轴坐标，通常是数值、日期
				//value 河流数值，当标记category时单选，否则可多选，每一列作为一条河流
				//category 可选，类别，不同类别绘制为不同系列
				dataSignNames: { name: "name", value: "value", category: "category" }
			}
		},
		options);
		
		var dataSignNames = options.dg.dataSignNames;
		var dataSetBind = chartSupport.dataSetBindMainNonNull(chart);
		var np = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.name);
		
		options = chartSupport.inflateRenderOptions(chart,
		{
			title:
			{
		        text: chart.name
		    },
			tooltip:
			{
				show: true,
				trigger: 'axis'
			},
			legend:
			{
				id: 0,
				//将在update中设置：
				//data
			},
			singleAxis:
			{
				id: 0,
				type: chartSupport.evalDataSetFieldAxisType(chart, np),
				//ECharts-5.3.2版本主题配置不起作用，所以这里配置
				"left": "10%",
	            "top": "24%",
	            "right": "10%",
	            "bottom": "10%",
			},
			series:
			[
				//将在update中设置：
				//{}
				//设初值以免渲染报错
				{
					id: 0,
					type: "themeRiver"
				}
			]
		},
		options);
		
		chart.echartsInit(options);
	};
	
	chartSupport.themeRiverUpdate = function(chart, chartResult)
	{
		var renderOptions= chart.renderOptions();
		var dg = renderOptions.dg;
		var dataSignNames = dg.dataSignNames;
		
		var dataSetBinds = chart.dataSetBindsMain();
		
		var legendData = [];
		var seriesData = [];
		
		for(var i=0; i<dataSetBinds.length; i++)
		{
			var dataSetBind = dataSetBinds[i];
			var dataSetAlias = chart.dataSetAlias(dataSetBind);
			var result = chart.resultOf(chartResult, dataSetBind);
			
			var np = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.name);
			var cp = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.category);
			
			if(cp)
			{
				var vp = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.value);
				
				//主题河流图只支持[ name, value, category ]格式的数据条目
				var data = chart.resultRowArrays(result, [ np, vp, cp ]);
				chart.originalDataIndexes(data, dataSetBind);
				
				//为类别添加前缀，确保多数据集类别不重复
				for(var j=0; j<data.length; j++)
				{
					var myCategory = chartSupport.legendNameForDataCategory(dataSetBinds, dataSetAlias, data[j][2]);
					data[j][2] = myCategory;
					
					chartSupport.appendDistinct(legendData, myCategory);
				}
				
				chartSupport.appendElement(seriesData, data);
			}
			else
			{
				var vps = chart.dataSetFieldsOfSign(dataSetBind, dataSignNames.value);
				
				for(var j=0; j<vps.length; j++)
				{
					var legendName = chartSupport.legendNameForDataValues(chart, dataSetBinds, dataSetBind, dataSetAlias, vps, j);
					//主题河流图只支持[ name, value, lengendName ]格式的数据条目
					var data = chart.resultRowArrays(result, [ np, vps[j] ]);
					for(var k=0; k<data.length; k++)
						data[k].push(legendName);
					
					chart.originalDataIndexes(data, dataSetBind);
					
					chartSupport.appendDistinct(legendData, legendName);
					chartSupport.appendElement(seriesData, data);
				}
			}
		}
		
		var series =
		{
			id: 0,
			type: "themeRiver",
			data: seriesData
		};
		
		//坐标轴信息也应替换合并，不然图表刷新有数据变化时，坐标不能自动更新
		var options = { legend: { id: 0, data: legendData}, series: [ series ], singleAxis: { id: 0 } };
		
		chartSupport.inflateAxisDataForEchartsUpdateOptions(renderOptions, options, options.singleAxis,
						chartSupport.inflateAxisDataExtractors.element(0));
		
		chartSupport.adaptArrayPropsForUpdateOptions(options, renderOptions);
		options = chart.inflateUpdateOptions(chartResult, options);
		
		chartSupport.echartsOptionsReplaceMerge(chart, options);
	};
	
	chartSupport.themeRiverResize = function(chart)
	{
		chartSupport.resizeChartEcharts(chart);
	};
	
	chartSupport.themeRiverDestroy = function(chart)
	{
		chartSupport.destroyChartEcharts(chart);
	};
	
	chartSupport.themeRiverOn = function(chart, eventType, handler)
	{
		chartSupport.bindChartEventHandlerForEcharts(chart, eventType, handler,
				chartSupport.themeRiverSetChartEventData);
	};
	
	chartSupport.themeRiverOff = function(chart, eventType, handler)
	{
		chart.echartsOffEventHandler(eventType, handler);
	};
	
	chartSupport.themeRiverSetChartEventData = function(chart, chartEvent, echartsEventParams)
	{
		var renderOptions= chart.renderOptions();
		var dg = renderOptions.dg;
		var dataSignNames = dg.dataSignNames;
		
		//TODO ECharts-5.3.2版本这里的数据与实际的事件数据不匹配，可能是BUG
		var echartsData = echartsEventParams.data;
		
		var data = undefined;
		
		if(echartsData)
		{
			data = {};
			data[dataSignNames.name] = echartsData[0];
			data[dataSignNames.value] = echartsData[1];
			data[dataSignNames.category] = echartsData[2];
		}
		
		chart.eventData(chartEvent, data);
		chart.eventOriginalDataIndex(chartEvent, chart.originalDataIndex(echartsData));
	};
	
	//象形柱图
	
	chartSupport.pictorialBarSymbolPaths=
	{
		//星型
		"star" : "path://m15.5,19c-0.082,0 -0.164,-0.02 -0.239,-0.061l-5.261,-2.869l-5.261,2.869c-0.168,0.092 -0.373,0.079 -0.529,-0.032s-0.235,-0.301 -0.203,-0.49l0.958,-5.746l-3.818,-3.818c-0.132,-0.132 -0.18,-0.328 -0.123,-0.506s0.209,-0.31 0.394,-0.341l5.749,-0.958l2.386,-4.772c0.085,-0.169 0.258,-0.276 0.447,-0.276s0.363,0.107 0.447,0.276l2.386,4.772l5.749,0.958c0.185,0.031 0.337,0.162 0.394,0.341s0.01,0.374 -0.123,0.506l-3.818,3.818l0.958,5.746c0.031,0.189 -0.048,0.379 -0.203,0.49c-0.086,0.061 -0.188,0.093 -0.29,0.093z",
	};
	
	chartSupport.pictorialBarRender = function(chart, options)
	{
		options = $.extend(true,
		{
			dg:
			{
				//name 名称
				//value 数值，当标记category时单选，否则可多选
				//category 可选，类别，不同类别绘制为不同系列
				dataSignNames: { name: "name", value: "value", category: "category" },
				//是否横向
				horizontal: false,
				//图形类型
				symbol: "circle",
				//图形重复
				symbolRepeat: true,
				//柱条间距
				barGap: "100%"
			}
		},
		options);
		
		var dataSignNames = options.dg.dataSignNames;
		var dataSetBind = chartSupport.dataSetBindMainNonNull(chart);
		var np = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.name);
		var vps = chart.dataSetFieldsOfSign(dataSetBind, dataSignNames.value);
		
		options = chartSupport.inflateRenderOptions(chart,
		{
			dg:
			{
				symbolSize: (vps.length > 1 ? "100%" : "50%")
			},
			title:
			{
		        text: chart.name
		    },
			tooltip:
			{
				trigger: "item"
			},
			legend:
			{
				id: 0,
				//将在update中设置：
				//data
			},
			xAxis:
			{
				id: 0,
				name: chart.dataSetFieldAlias(dataSetBind, np),
				nameGap: 5,
				type: chartSupport.evalDataSetFieldAxisType(chart, np),
				splitLine: { show: false }
			},
			yAxis:
			{
				id: 0,
				name: (vps.length == 1 ? chart.dataSetFieldAlias(dataSetBind, vps[0]) : ""),
				nameGap: 5,
				type: "value"
			},
			series:
			[
				//将在update中设置：
				//{}
				//设初值以免渲染报错
				{
					id: 0,
					type: "pictorialBar"
				}
			]
		},
		options,
		function(options)
		{
			if(options.dg.horizontal)
			{
				var xAxisTmp = options.xAxis;
				options.xAxis = options.yAxis;
				options.yAxis = xAxisTmp;
				
				//横向柱状图的yAxis.type不能为value，不然会变为竖向图形
				if(options.yAxis.type == "value")
					options.yAxis.type = "category";
			}
		});
		
		chart.echartsInit(options);
	};
	
	chartSupport.pictorialBarUpdate = function(chart, chartResult)
	{
		var renderOptions= chart.renderOptions();
		var dg = renderOptions.dg;
		var dataSignNames = dg.dataSignNames;
		
		var symbol = dg.symbol;
		if(chartSupport.pictorialBarSymbolPaths[symbol])
			symbol = chartSupport.pictorialBarSymbolPaths[symbol];
		
		var dataSetBinds = chart.dataSetBindsMain();
		
		var legendData = [];
		var series = [];
		
		for(var i=0; i<dataSetBinds.length; i++)
		{
			var dataSetBind = dataSetBinds[i];
			var dataSetAlias = chart.dataSetAlias(dataSetBind);
			var result = chart.resultOf(chartResult, dataSetBind);
			
			var np = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.name);
			var cp = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.category);
			
			if(cp)
			{
				var vp = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.value);
				
				var categoryNames = [];
				var categoryDatasMap = {};
				
				//使用{value: [name,value]}格式可以更好地兼容category、value、time坐标轴类型
				var propertyMap = { "value": [np, vp] }; 
				propertyMap = chartSupport.inflatePropertyMapWithCategory(propertyMap, cp);
				var data = chart.resultMapObjects(result, propertyMap);
				chart.originalDataIndexes(data, dataSetBind);
				chartSupport.splitDataByCategory(data, categoryNames, categoryDatasMap);
				
				for(var j=0; j<categoryNames.length; j++)
				{
					var categoryName = categoryNames[j];
					var legendName = chartSupport.legendNameForDataCategory(dataSetBinds, dataSetAlias, categoryName);
					var mySeries =
					{
						id: series.length, type: "pictorialBar", name: legendName, data: categoryDatasMap[categoryName],
						symbol: symbol,
						symbolSize: dg.symbolSize, symbolRepeat: dg.symbolRepeat,
						barGap: dg.barGap
					};
					
					if(dg.horizontal)
					{
						mySeries.encode = { x: 1, y: 0 };
					}
					
					legendData.push(legendName);
					series.push(mySeries);
				}
			}
			else
			{
				var vps = chart.dataSetFieldsOfSign(dataSetBind, dataSignNames.value);
				
				for(var j=0; j<vps.length; j++)
				{
					var legendName = chartSupport.legendNameForDataValues(chart, dataSetBinds, dataSetBind, dataSetAlias, vps, j);
					
					//使用{value: [name,value]}格式可以更好地兼容category、value、time坐标轴类型
					var vpsMy = [np, vps[j]];
					var data = chart.resultValueObjects(result, vpsMy);
					
					chart.originalDataIndexes(data, dataSetBind);
					
					var mySeries =
					{
						id: series.length, type: "pictorialBar", name: legendName, data: data,
						symbol: symbol,
						symbolSize: dg.symbolSize, symbolRepeat: dg.symbolRepeat,
						barGap: dg.barGap
					};
					
					if(dg.horizontal)
					{
						mySeries.encode = { x: 1, y: 0 };
					}
					
					legendData.push(legendName);
					series.push(mySeries);
				}
			}
		}
		
		var options = { legend: {id: 0, data: legendData}, series: series };
		
		//坐标轴信息也应替换合并，不然图表刷新有数据变化时，坐标不能自动更新
		if(dg.horizontal)
			options.yAxis = { id: 0 };
		else
			options.xAxis = { id: 0 };
		
		chartSupport.inflateAxisDataForEchartsUpdateOptions(renderOptions, options, (dg.horizontal ? options.yAxis : options.xAxis),
						chartSupport.inflateAxisDataExtractors.valueElement(0));
		
		chartSupport.adaptArrayPropsForUpdateOptions(options, renderOptions);
		
		options = chart.inflateUpdateOptions(chartResult, options, function(options)
		{
			chartSupport.adaptValueArrayObjSeriesData(chart, options, "pictorialBar");
		});
		
		chartSupport.echartsOptionsReplaceMerge(chart, options);
	};
	
	chartSupport.pictorialBarResize = function(chart)
	{
		chartSupport.resizeChartEcharts(chart);
	};
	
	chartSupport.pictorialBarDestroy = function(chart)
	{
		chartSupport.destroyChartEcharts(chart);
	};
	
	chartSupport.pictorialBarOn = function(chart, eventType, handler)
	{
		chartSupport.bindChartEventHandlerForEcharts(chart, eventType, handler,
				chartSupport.pictorialBarSetChartEventData);
	};
	
	chartSupport.pictorialBarOff = function(chart, eventType, handler)
	{
		chart.echartsOffEventHandler(eventType, handler);
	};
	
	chartSupport.pictorialBarSetChartEventData = function(chart, chartEvent, echartsEventParams)
	{
		var renderOptions= chart.renderOptions();
		var dg = renderOptions.dg;
		var dataSignNames = dg.dataSignNames;
		var categoryPropName = chartSupport.builtinCategoryPropName();
		
		var echartsData = echartsEventParams.data;
		var data = chartSupport.extractNameValueStyleObj(echartsData, dataSignNames.name, dataSignNames.value);
		data[dataSignNames.category] = (echartsData && echartsData[categoryPropName] != null ?
											echartsData[categoryPropName] : undefined);
		
		chart.eventData(chartEvent, data);
		chart.eventOriginalDataIndex(chartEvent, chart.originalDataIndex(echartsData));
	};
	
	//象形进度柱图
	
	chartSupport.pictorialBarProgressRender = function(chart, options)
	{
		options = $.extend(true,
		{
			dg:
			{
				//name 名称，必选，单选
				//value 值，必选，单选
				//max 最大值，可选，单选，默认：100
				dataSignNames: { name: "name", value: "value", max: "max" },
				//是否横向
				horizontal: false,
				//图形类型
				symbol: "rect",
				//图形尺寸
				symbolSize: ["100%", "100%"],
				//图形重复
				symbolRepeat: false,
				//背景图形重复
				symbolRepeatForBg: false,
				//图形间距
				symbolMargin: 0,
				//柱条间距
				barGap: "-100%",
				//最大值
				max: 100,
			}
		},
		options);
		
		var dataSignNames = options.dg.dataSignNames;
		var dataSetBind = chartSupport.dataSetBindMainNonNull(chart);
		var np = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.name);
		var vp = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.value);
		
		options = chartSupport.inflateRenderOptions(chart,
		{
			title:
			{
		        text: chart.name
		    },
			tooltip:
			{
				trigger: "item"
			},
			legend:
			{
				id: 0,
				//将在update中设置：
				//data
			},
			xAxis:
			{
				id: 0,
				name: chart.dataSetFieldAlias(dataSetBind, np),
				nameGap: 5,
				type: chartSupport.evalDataSetFieldAxisType(chart, np),
				splitLine: { show: false }
			},
			yAxis:
			{
				id: 0,
				name: chart.dataSetFieldAlias(dataSetBind, vp),
				nameGap: 5,
				type: "value",
				splitLine: { show: false }
			},
			series:
			[
				//将在update中设置：
				//{}
				//设初值以免渲染报错
				{
					id: 0,
					type: "pictorialBar"
				}
			]
		},
		options,
		function(options)
		{
			if(options.dg.horizontal)
			{
				var xAxisTmp = options.xAxis;
				options.xAxis = options.yAxis;
				options.yAxis = xAxisTmp;
				
				//横向柱状图的yAxis.type不能为value，不然会变为竖向图形
				if(options.yAxis.type == "value")
					options.yAxis.type = "category";
			}
		});
		
		chart.echartsInit(options);
	};
	
	chartSupport.pictorialBarProgressUpdate = function(chart, chartResult)
	{
		var renderOptions= chart.renderOptions();
		var dg = renderOptions.dg;
		var dataSignNames = dg.dataSignNames;
		
		var dataSetBinds = chart.dataSetBindsMain();
		
		var seriesName = "";
		var seriesData = [];
		var maxValue = null;
		
		for(var i=0; i<dataSetBinds.length; i++)
		{
			var dataSetBind = dataSetBinds[i];
			var dataSetAlias = chart.dataSetAlias(dataSetBind);
			var result = chart.resultOf(chartResult, dataSetBind);
			
			var np = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.name);
			var vp = chart.dataSetFieldOfSign(dataSetBind, dataSignNames.value);
			
			//使用{value: [name,value]}格式可以更好地兼容category、value、time坐标轴类型
			var data = chart.resultValueObjects(result, [np, vp]);
			
			chart.originalDataIndexes(data, dataSetBind);
			
			//取任一不为空的地图名列值
			if(maxValue == null)
				maxValue = chartSupport.resultFirstNonEmptyValueOfSign(chart, dataSetBind, result, dataSignNames.max);
			
			seriesData = seriesData.concat(data);
			
			if(!seriesName)
				seriesName = dataSetAlias;
		}
		
		maxValue = (maxValue == null ? dg.max : maxValue);
		
		var symbol = dg.symbol;
		if(chartSupport.pictorialBarSymbolPaths[symbol])
			symbol = chartSupport.pictorialBarSymbolPaths[symbol];
		
		var series =
		[
			{
				id: 0,
				type: "pictorialBar",
				name: seriesName,
				data: seriesData,
				symbol: symbol,
				symbolSize: dg.symbolSize,
				symbolRepeat: dg.symbolRepeat,
				barGap: dg.barGap,
				symbolBoundingData: maxValue,
				symbolClip: true,
				symbolMargin: dg.symbolMargin,
				z: 10
			},
			{
				id: 1,
				type: "pictorialBar",
				name: seriesName+"-background",
				data: seriesData,
				symbol: symbol,
				symbolSize: dg.symbolSize,
				symbolRepeat: dg.symbolRepeatForBg,
				barGap: dg.barGap,
				symbolBoundingData: maxValue,
				symbolClip: false,
				symbolMargin: dg.symbolMargin,
				z: 1,
				animationDuration: 0,
				itemStyle:{ color: chart.themeGradualColor(0.2) },
				silent: true
			}
		];
		
		if(dg.horizontal)
		{
			series[0].encode = { x: 1, y: 0 };
			series[1].encode = { x: 1, y: 0 };
		}
		
		var options = { legend: {id: 0, data: [ seriesName ]}, series: series };
		
		//坐标轴信息也应替换合并，不然图表刷新有数据变化时，坐标不能自动更新
		if(dg.horizontal)
		{
			options.xAxis = { id: 0, max: maxValue };
			options.yAxis = { id: 0 };
		}
		else
		{
			options.xAxis = { id: 0 };
			options.yAxis = { id: 0, max: maxValue };
		}
		
		chartSupport.inflateAxisDataForEchartsUpdateOptions(renderOptions, options, (dg.horizontal ? options.yAxis : options.xAxis),
						chartSupport.inflateAxisDataExtractors.valueElement(0));
		
		chartSupport.adaptArrayPropsForUpdateOptions(options, renderOptions);
		
		options = chart.inflateUpdateOptions(chartResult, options, function(options)
		{
			chartSupport.adaptValueArrayObjSeriesData(chart, options, "pictorialBar");
		});
		
		chartSupport.echartsOptionsReplaceMerge(chart, options);
	};
	
	chartSupport.pictorialBarProgressResize = function(chart)
	{
		chartSupport.resizeChartEcharts(chart);
	};
	
	chartSupport.pictorialBarProgressDestroy = function(chart)
	{
		chartSupport.destroyChartEcharts(chart);
	};
	
	chartSupport.pictorialBarProgressOn = function(chart, eventType, handler)
	{
		chartSupport.bindChartEventHandlerForEcharts(chart, eventType, handler,
				chartSupport.pictorialBarProgressSetChartEventData);
	};
	
	chartSupport.pictorialBarProgressOff = function(chart, eventType, handler)
	{
		chart.echartsOffEventHandler(eventType, handler);
	};
	
	chartSupport.pictorialBarProgressSetChartEventData = function(chart, chartEvent, echartsEventParams)
	{
		var renderOptions= chart.renderOptions();
		var dg = renderOptions.dg;
		var dataSignNames = dg.dataSignNames;
		
		var echartsData = echartsEventParams.data;
		var data = chartSupport.extractNameValueStyleObj(echartsData, dataSignNames.name, dataSignNames.value);
		
		chart.eventData(chartEvent, data);
		chart.eventOriginalDataIndex(chartEvent, chart.originalDataIndex(echartsData));
	};
	
	//表格
	
	chartSupport.tableRender = function(chart, options)
	{
		options = $.extend(true,
		{
			dg:
			{
				dataSignNames: { column: "column" }
			}
		},
		options);
		
		var dataSignNames = options.dg.dataSignNames;
		var chartEle = chart.elementJquery();
		chartEle.addClass("dg-chart-table");
		var isV1 = chartSupport.tableIsV1();
		chartEle.addClass(isV1 ? "dg-table-v1" : "dg-table-v2");
		
		var columns = chartSupport.tableGetFieldColumns(chart, dataSignNames.column);
		
		if(columns.length == 0)
			throw new Error("Column required for rendering table in chart '"+chart.name+"'");
		
		options = chartSupport.inflateRenderOptions(chart,
		{
			//标题配置
			title:
			{
				show: true,
				text: chart.name
			},
			//标题样式，格式为：{ color:'red', 'background-color':'blue' }
			titleStyle: undefined,
			//表格样式，格式为：
			//{
			//	table: {...},
			//	head: { row: {...}, cell: {...} },
			//	body:
			//	{
			//		row: {...}, rowOdd: {}, rowEven: {}, rowHover: {...}, rowSelected: {...},
			//		cell: {...}, cellOdd: {}, cellEven: {}, cellHover: {...}, cellSelected: {...}
			//	}
			//}
			tableStyle: undefined,
			//自定义单元格渲染函数，格式为：function(value, name, rowIndex, columnIndex, row, meta){ return ...; }
			renderCell: undefined,
			//轮播，格式可以为：true、false、轮播interval数值、轮播interval返回函数、{...}
			carousel: undefined,
			//是否禁用条纹样式效果
			disableStripe: false,
			//是否禁用悬浮样式效果
			disableHover: false,
			//是否表格文本不换行
			enableWrapText: false,
			
			//DataTable配置项
			"columns": columns,
			"data" : [],
			"ordering": false,
			"scrollX": true,
			"scrollY": undefined,
			"autoWidth": true,
	        "scrollCollapse": false,
			"pagingType": "full_numbers",
			"lengthMenu": [],
			"pageLength": 50,
			"select" : { style : 'os' },
			"searching" : false,
			"language":
		    {
				"emptyTable": "",
				"zeroRecords": "",
				"search": "搜索",
				"lengthMenu": "每页_MENU_条",
				"info": "共_TOTAL_条，当前_START_-_END_条",
				"infoEmpty": "无数据",
				"infoFiltered": "_TOTAL_条",
				"loadingRecords": "加载中...",
				"paginate":
				{
					"first": "首页",
					"last": "尾页",
					"next": "下一页",
					"previous": "上一页"
				},
				select:
				{
					"rows": ""
				}
			}
		},
		options, null, function(options)
		{
			chartSupport.tableRenderProcessOptions(chart, options);
		});
		
		// < @deprecated 兼容2.8.0版本的{table:{renderValue:...}}配置项结构，未来版本会移除
		if(options.table && options.table.renderValue)
		{
			options.renderCell = options.table.renderValue;
		}
		// > @deprecated 兼容2.8.0版本的{table:{renderValue:...}}配置项结构，未来版本会移除
		
		// < @deprecated 兼容2.8.0版本的{title:{color:"..."}}配置项结构，未来版本会移除
		if(options.title && !options.titleStyle)
		{
			var titleStyle = $.extend(true, {}, options.title);
			delete titleStyle.show;
			delete titleStyle.text;
			
			if(!$.isEmptyObject(titleStyle))
				options.titleStyle = titleStyle;
		}
		// > @deprecated 兼容2.8.0版本的{title:{color:"..."}}配置项结构，未来版本会移除
		
		// < @deprecated 兼容2.8.0版本的{table:{header:{},row:{color:'red',odd:{...},even:{...},hover:{...},selected:{...}}}}配置项结构，未来版本会移除
		if(options.table && !options.tableStyle)
		{
			var tableStyle = $.extend(true, {}, options.table);
			delete tableStyle.renderValue;
			
			if(tableStyle.header || tableStyle.row)
			{
				tableStyle.head = { row: tableStyle.header };
				delete tableStyle.header;
				
				if(tableStyle.row)
				{
					tableStyle.body = { row: tableStyle.row };
					delete tableStyle.row;
					
					tableStyle.body.rowOdd = tableStyle.body.row.odd;
					tableStyle.body.rowEven = tableStyle.body.row.even;
					tableStyle.body.rowHover = tableStyle.body.row.hover;
					tableStyle.body.rowSelected = tableStyle.body.row.selected;
					delete tableStyle.body.row.odd;
					delete tableStyle.body.row.even;
					delete tableStyle.body.row.hover;
					delete tableStyle.body.row.selected;
				}
				
				options.tableStyle = tableStyle;
			}
		}
		// > @deprecated 兼容2.8.0版本的{table:{header:{},row:{color:'red',odd:{...},even:{...},hover:{...},selected:{...}}}}配置项结构，未来版本会移除
		
		//填充options.columns的render函数
		for(var i=0; i<options.columns.length; i++)
		{
			var column = options.columns[i];
			
			//DataTables-1.10.18是允许column.data为""的，升级至1.11.3后则会有一个警告弹出框，
			//这里设置defaultContent可以解决此问题
			if(chartFactory.isNullOrEmpty(column.data) && column.defaultContent == null)
				column.defaultContent = "";
			
			if(column.render == null)
			{
				column.render = function(value, type, row, meta)
				{
					//单元格展示绘制
					if(type == "display")
					{
						if(options.renderCell)
						{
							var rowIndex = meta.row;
							var columnIndex = meta.col;
							var name = options.columns[columnIndex].data;
							
							return options.renderCell(value, name, rowIndex, columnIndex, row, meta);
						}
						else
							return chartFactory.escapeHtml(value);
					}
					//其他绘制，比如排序
					else
						return value;
				};
			}
		}
		
		chartSupport.tableThemeStyleSheet(chart, options);
		
		if(options.carousel.enable)
			chartEle.addClass("dg-chart-table-carousel");
		
		if(!options.title.show)
			chartEle.addClass("dg-hide-title");
		
		if(options.enableWrapText)
			chartEle.addClass("dg-text-nowrap");
		
		var eleWrapper = (isV1 ? chartEle : $("<div class='dg-chart-ele-wrapper' />").appendTo(chartEle));
		
		var chartTitle = $("<div class='dg-chart-table-title' />").html(options.title.text).appendTo(eleWrapper);
		if(options.titleStyle)
			chart.elementStyle(chartTitle, options.titleStyle);
		
		var chartContent = $("<div class='dg-chart-table-content' />").appendTo(eleWrapper);
		
		if(isV1)
		{
			chartContent.css("top", (options.title.show ? chartTitle.outerHeight(true) : 0));
		}
		
		var table = $("<table width='100%' class='"+(options.disableStripe ? "" : " stripe ")+(options.disableHover ? "" : " hover ")+"'></table>")
						.appendTo(chartContent);
		var tableId = chart.id+"-table";
		table.attr("id", tableId);
		
		table.dataTable(options);
		
		var dataTable = table.DataTable();
		
		if(options.scrollY == null && isV1)
		{
			chartSupport.tableEvalBodyHeightV1(chart, chartContent, dataTable);
		}
		
		if(options.carousel.enable && options.carousel.hideVerticalScrollbar != false)
		{
			var tableBody = chartSupport.tableGetScrollBody(chart, chartContent);
			tableBody.css("overflow-y", "hidden");
		}
		
		$(dataTable.table().body()).on("mouseenter", "tr", function()
		{
			if(options.carousel.pauseOnHover)
				chartSupport.tableStopCarousel(chart);
		})
		.on("mouseleave", "tr", function()
		{
			if(options.carousel.pauseOnHover && options.carousel.enable)
				chartSupport.tableStartCarousel(chart);
		});
		
		chart.internal(dataTable);
	};
	
	chartSupport.tableUpdate = function(chart, chartResult)
	{
		var dataSetBinds = chart.dataSetBindsMain();
		
		var updateOptions = { data: [] };
		
		for(var i=0; i<dataSetBinds.length; i++)
		{
			var dataSetBind = dataSetBinds[i];
			
			var result = chart.resultOf(chartResult, dataSetBind);
			var resultDatas = chart.resultDatas(result);
			
			//复制，避免污染原始数据
			for(var j=0; j<resultDatas.length; j++)
			{
				var data = $.extend({}, resultDatas[j]);
				chart.originalDataIndex(data, dataSetBind, j);
				updateOptions.data.push(data);
			}
		}
		
		chartSupport.tableStopCarousel(chart);
		
		updateOptions = chart.inflateUpdateOptions(chartResult, updateOptions);
		chartSupport.tableUpdateInternalData(chart, chartResult, updateOptions);
	};
	
	chartSupport.tableResize = function(chart)
	{
		var renderOptions= chart.renderOptions();
		var chartContent = chartSupport.tableGetChartContent(chart);
		var dataTable = chart.internal();
		
		if(renderOptions.scrollY == null && chartSupport.tableIsV1())
		{
			chartSupport.tableEvalBodyHeightV1(chart, chartContent, dataTable);
		}
		
		chartSupport.tableAdjustColumn(dataTable);
	};
	
	chartSupport.tableDestroy = function(chart)
	{
		var chartEle = chart.elementJquery();
		
		chartSupport.tableStopCarousel(chart);
		chartEle.removeClass("dg-chart-table dg-table-v1 dg-table-v2 dg-hide-title dg-text-nowrap dg-chart-table-carousel");
		chartEle.removeClass(chart.extValue(chartFactory.builtinPropName("TableChartLocalStyleName")));
		$(".dg-chart-table-title", chartEle).remove();
		$(".dg-chart-table-content", chartEle).remove();
		$(".dg-chart-ele-wrapper", chartEle).remove();
	};
	
	chartSupport.tableOn = function(chart, eventType, handler)
	{
		var handlerDelegation = function(htmlEvent)
		{
			var rowElement = this;
			var chartEvent = chartSupport.chartEventForHtml(chart, eventType, htmlEvent);
			chartSupport.tableSetChartEventData(chart, chartEvent, htmlEvent, rowElement);
			
			chart.callEventHandler(handler, chartEvent);
		};
		
		chart.registerEventHandlerDelegation(eventType, handler, handlerDelegation);
		$(chart.internal().table().body()).on(eventType, "tr", handlerDelegation);
	};
	
	chartSupport.tableOff = function(chart, eventType, handler)
	{
		var $tableBody = $(chart.internal().table().body());
		
		chart.removeEventHandlerDelegation(eventType, handler, function(et, eh, ehd)
		{
			$tableBody.off(et, "tr", ehd);
		});
	};
	
	chartSupport.tableSetChartEventData = function(chart, chartEvent, htmlEvent, rowElement)
	{
		var renderOptions= chart.renderOptions();
		var dg = renderOptions.dg;
		var dataSignNames = dg.dataSignNames;
		
		var dataTable = chart.internal();
		
		var chartData = dataTable.row(rowElement).data();
		
		var data = {};
		
		if(chartData)
		{
			var columnData = [];
			var columnDataSrc = dataTable.columns().dataSrc();
			
			for(var i=0; i<columnDataSrc.length; i++)
				columnData[i] = chartData[columnDataSrc[i]];
			
			data[dataSignNames.column] = columnData;
		}
		
		chart.eventData(chartEvent, data);
		chart.eventOriginalDataIndex(chartEvent, chart.originalDataIndex(chartData));
	};
	
	chartSupport.tableRenderProcessOptions = function(chart, options)
	{
		chartSupport.tableRenderProcessServerSidePaging(chart, options);
		chartSupport.tableRenderProcessCarousel(chart, options)
		
		//必须明确设置paging=false，因为底层表格组件的paging默认值为true
		options.paging = (options.paging != null ? options.paging : false);
		
		//开启分页后，默认开启info
		options.info = (options.info != null ? options.info : options.paging);
		
		if(options.paging)
		{
			options.lengthMenu = (options.lengthMenu == null || options.lengthMenu.length == 0 ? [ 10, 25, 50, 75, 100 ] : options.lengthMenu);
			//如果有50，则取50，以兼容旧版逻辑
			options.pageLength = (chartFactory.indexInArray(options.lengthMenu, 50) >= 0 ? 50 : options.lengthMenu[0]);
		}
		
		if(chartSupport.tableIsV1())
		{
			if(options.dom == null)
			{
				options.dom = "t";
				options.dom = (options.paging ? (options.dom + "ilpr") : options.dom);
				options.dom = (options.searching ? ("f" + options.dom) : options.dom);
				options.dom = (options.buttons ? ("B" + options.dom) : options.dom);
			}
		}
		else
		{
			var dftLayout =
			{
				topStart: (options.buttons ? "buttons" : null),
				topEnd: (options.searching ? "search" : null),
				bottomStart: (options.info ? "info" : null),
				bottomEnd: (options.paging ? ["pageLength", "paging"] : null)
			};
			
			options.layout = (options.layout  == null ? dftLayout : options.layout);
		}
	};
	
	/**
	 * 表格处理carousel选项，格式为：
	 * {
	 *   carousel: ...
	 * }
	 */
	chartSupport.tableRenderProcessCarousel = function(chart, options)
	{
		//标准轮播格式
		var carouselConfig =
		{
			//是否开启，true 开启；false 禁用；"auto" 只有在行溢出时才开启
			enable: false,
			//滚动间隔毫秒数，或者返回间隔毫秒数的函数：
			//currentRow 当前可见行
			//visibleHeight 当前可见行的剩余可见高度
			//height 当前可见行高度
			//function(currentRow, visibleHeight, height){ return ...; }
			interval: 50,
			//滚动跨度像素数，或者返回跨度像素数的函数：
			//function(currentRow, visibleHeight, height){ return ...; }
			span: 1,
			//是否在鼠标悬停时暂停轮播
			pauseOnHover: true,
			//是否隐藏纵向滚动条
			hideVerticalScrollbar: true,
			//溢出删除个数，小于这个数的轮播溢出行数，不会执行删除操作
			overflowCount: 2
		};
		
		if(options.carousel == null)
		{
			
		}
		//true、false、"auto"
		else if(options.carousel === true || options.carousel === false || chartFactory.isString(options.carousel))
		{
			carouselConfig.enable = options.carousel;
		}
		//间隔数值、函数
		else if(chartFactory.isNumber(options.carousel) || $.isFunction(options.carousel))
		{
			carouselConfig.enable = true;
			carouselConfig.interval = options.carousel;
		}
		//轮播对象
		else
		{
			carouselConfig = $.extend(true, carouselConfig, options.carousel);
		}
		
		options.carousel = carouselConfig;
	};
	
	/**
	 * 表格处理serverSidePaging选项，格式为：
	 * {
	 *   serverSidePaging:
	 *   {
	 *      //必填，将data中的分页查询信息设置为图表数据集参数
	 *      param: function(chart, data){ ... },
	 *      //可选（与totalFieldName二选一），数据集附加数据中总记录数关键字
	 *      totalAdditionName: "...",
	 *      //可选（与totalAdditionName二选一），附件数据集中总记录数字段名
	 *      totalFieldName: "...",
	 *      //可选，根据图表数据集参数设置表格分页状态，或者返回要设置的状态数据（参考chartSupport.tableUpdatePagingState()函数），
	 *      //如果不设置，使用图表参数面板的查询信息不会同步显示到表格中
	 *      state: function(chart){ ... },
	 *      //可选，触发表格draw()函数时的paging参数
	 *      drawPagingArg: ...,
	 *   }
	 * }
	 */
	chartSupport.tableRenderProcessServerSidePaging = function(chart, options)
	{
		var serverSidePaging = chartSupport.serverSidePagingOption(options);
		
		if(!serverSidePaging)
			return;
		
		options.paging = true;
		options.serverSide = true;
		
		options.ajax = function(data, callback, settings)
		{
			var ajaxInfos = chartFactory.extValueBuiltin(chart, "serverSidePagingAjaxInfos");
			if(ajaxInfos == null)
			{
				ajaxInfos = [];
				chartFactory.extValueBuiltin(chart, "serverSidePagingAjaxInfos", ajaxInfos);
			}
			
			ajaxInfos.push({ data: data, callback: callback, settings: settings });
			
			var refreshInfo = chartFactory.extValueBuiltin(chart, "serverSidePagingRefreshInfo");
			
			//由图表API触发，此时已获取到数据，不应再执行chart.refreshData()函数
			if(refreshInfo != null)
			{
				chartFactory.extValueBuiltin(chart, "serverSidePagingRefreshInfo", null);
				
				if(chart.isActive())
					chartSupport.tableUpdateInternalData(chart, refreshInfo.chartResult, refreshInfo.updateOptions);
			}
			else
			{
				serverSidePaging.param(chart, data);
				
				if(chart.isActive())
					chart.refreshData();
			}
		};
		chartSupport.updateInternalOption(options, function(updateOptions, chart, chartResult)
		{
			var ajaxInfos = (chartFactory.extValueBuiltin(chart, "serverSidePagingAjaxInfos") || []);
			
			//由表格内部操作触发
			if(ajaxInfos.length > 0)
			{
				for(var i=0; i<ajaxInfos.length; i++)
				{
					var ajaxInfo = ajaxInfos[i];
					var recordsTotal = chartSupport.tableGetRecordsTotal(updateOptions, chart, chartResult, serverSidePaging);
					
					var pagingData =
					{
						draw: (ajaxInfo.data ? ajaxInfo.data.draw : undefined),
						recordsTotal: recordsTotal,
						recordsFiltered: recordsTotal,
						data: updateOptions.data
					};
					
					ajaxInfo.callback(pagingData);
				}
				
				chartFactory.extValueBuiltin(chart, "serverSidePagingAjaxInfos", []);
			}
			//由图表API触发，比如：参数表单提交、chart.refreshData()
			else
			{
				var pagingState = (serverSidePaging.state == null ? null : serverSidePaging.state(chart));
				if(pagingState != null)
					chartSupport.tableUpdatePagingState(chart, pagingState);
				
				var refreshInfo = { updateOptions: updateOptions, chartResult: chartResult };
				chartFactory.extValueBuiltin(chart, "serverSidePagingRefreshInfo", refreshInfo);
				chart.internal().draw(serverSidePaging.drawPagingArg == null ? false : serverSidePaging.drawPagingArg);
			}
		});
	};
	
	/**
	 * 表格更新分页状态（不应刷新数据），state格式为：
	 * {
	 *   //可选，页大小
	 *   length: 数值,
	 *   //可选，页码（以0开始）
	 *   page: 数值,
	 *   //可选，页数据起始索引
	 *   start: 数值
	 * }
	 */
	chartSupport.tableUpdatePagingState = function(chart, state)
	{
		if(!state)
			return;
		
		var dataTable = chart.internal();
		
		var pageLength = (state.length == null ? null : parseInt(state.length));
		var pagePage = (state.page == null ? null : parseInt(state.page));
		var pageStart = (state.start == null ? null : parseInt(state.start));
		
		if(pageLength != null)
			dataTable.page.len(pageLength);
		
		if(pagePage != null)
		{
			dataTable.page(pagePage);
		}
		else if(pageStart != null)
		{
			pageLength = (pageLength == null ? dataTable.page.info() : pageLength);
			pagePage = parseInt(pageStart/pageLength);
			dataTable.page(pagePage);
		}
	};
	
	chartSupport.tableGetRecordsTotal = function(updateOptions, chart, chartResult, serverSidePaging)
	{
		var recordsTotal = null;
		
		var dsbs = chart.dataSetBinds();
		
		for(var i=0; i<dsbs.length; i++)
		{
			var result = chart.resultOf(chartResult, dsbs[i]);
			
			if(serverSidePaging.totalAdditionName != null)
			{
				recordsTotal = chart.resultAddition(result, serverSidePaging.totalAdditionName);
			}
			
			if(recordsTotal == null && serverSidePaging.totalFieldName != null
				&& chart.dataSetField(dsbs[i], serverSidePaging.totalFieldName) != null)
			{
				var colValues = chart.resultColumnArrays(result, serverSidePaging.totalFieldName);
				recordsTotal = chartSupport.findNonNull(colValues);
			}
			
			if(recordsTotal != null)
				break;
		}
		
		if(recordsTotal == null)
			recordsTotal = (updateOptions.data ? updateOptions.data.length : 0);
			
		return recordsTotal;
	};
	
	chartSupport.tableGetChartContent = function(chart)
	{
		//图表的数据透视表功能也采用的是DataTable组件，可能会与表格图表处在同一个图表div内，
		//因此，获取图表表格的DOM操作都应限定在".dg-chart-table-content"内
		
		return $(".dg-chart-table-content", chart.element());
	};
	
	chartSupport.tableGetFieldColumns = function(chart, columnDataSignName)
	{
		var columns = [];
		
		var dataSetBinds = chart.dataSetBindsMain();
		
		for(var i=0; i<dataSetBinds.length; i++)
		{
			var dataSetBind = dataSetBinds[i];
			
			var fields = chart.dataSetFieldsOfSign(dataSetBind, columnDataSignName);
			if(!fields || fields.length == 0)
				fields = chart.dataSetFields(dataSetBind);
			
			for(var j=0; j<fields.length; j++)
			{
				var field = fields[j];
				var colIdx = chartSupport.findInArray(columns, field.name, "name");
				
				if(colIdx < 0)
				{
					var column =
					{
						title: chart.dataSetFieldAlias(dataSetBind, field),
						name: field.name,
						data: field.name,
						defaultContent: "",
						orderable: true,
						searchable: false,
						//需后续完善
						render: undefined
					};
					
					columns.push(column);
				}
			}
		}
		
		return columns;
	};
	
	chartSupport.tableInternalVersion = function()
	{
		if(typeof(DataTable) != "undefined")
			return (DataTable.version || "");
		else
			return "";
	};
	
	chartSupport.tableIsV1 = function()
	{
		var v = chartSupport.tableInternalVersion();
		return (v != null && /^1\./.test(v));
	};
	
	chartSupport.tableGetScrollHead = function(chart, $chartContent)
	{
		if(chartSupport.tableIsV1())
			return $(".dataTables_scrollHead", $chartContent);
		else
			return $(".dt-scroll-head", $chartContent);
	};
	
	chartSupport.tableGetScrollBody = function(chart, $chartContent)
	{
		if(chartSupport.tableIsV1())
			return $(".dataTables_scrollBody", $chartContent);
		else
			return $(".dt-scroll-body", $chartContent);
	};
	
	chartSupport.tableThemeStyleSheet = function(chart, options)
	{
		var isV1 = chartSupport.tableIsV1();
		var name = chartFactory.builtinPropName("TableChart");
		var isLocalStyle = (options.tableStyle != null);
		var forceUpdate = false;
		
		if(isLocalStyle)
		{
			//这里不应使用随机数，因为在图表多次destroy再init后，会导致残留无法销毁的样式表DOM
			name = "tableStyle" + chart.id;
			//需强制为每次都更新样式表，因为绑定的图表主题可能是全局主题
			forceUpdate = true;
			
			chart.elementJquery().addClass(name);
			chart.extValue(chartFactory.builtinPropName("TableChartLocalStyleName"), name);
		}
		
		chart.themeStyleSheet(name, function()
		{
			var theme = chart.theme();
			
			//使用实际背景色可以避免当backgroundColor是透明时，设置固定列后横向滚动时固定列无法遮挡其他滚动列的问题
			var rowBgColor = theme.actualBackgroundColor;
			var rowOddBgColor = chart.themeGradualColor(0);
			
			//V2版本通过透明度控制奇偶行颜色，这样可以适配任意背景，不过仍会出现固定列后横向滚动无法遮挡的问题（用户可以自定义颜色解决）
			if(!isV1)
			{
				rowBgColor ="rgba(0,0,0,0)";
				rowOddBgColor = chart.themeGradualColor(0.5) + "09";
			}
			
			var tableStyle =
			{
				table: {},
				head:
				{
					row:
					{
						"color": theme.color,
						//必须设置背景色，不然会是组件默认背景色无法适配图表主题
						"background-color": rowBgColor
					},
					cell: {}
				},
				body:
				{
					row:
					{
						"color": theme.color,
						//必须设置背景色，不然会是组件默认背景色无法适配图表主题
						"background-color": rowBgColor
					},
					rowOdd:
					{
						//必须设置背景色，不然会是组件默认背景色无法适配图表主题
						"background-color": rowOddBgColor
					},
					rowEven:
					{
						//必须设置背景色，不然会是组件默认背景色无法适配图表主题
						"background-color": rowBgColor
					},
					rowHover:
					{
						"background-color": chart.themeGradualColor(0.2)
					},
					rowSelected:
					{
						"color": theme.highlightTheme.color,
						"background-color": theme.highlightTheme.backgroundColor
					},
					cell: {},
					cellOdd: {},
					cellEven: {},
					cellHover: {},
					cellSelected:
					{
						"color": theme.highlightTheme.color
					}
				}
			};
			
			if(isLocalStyle)
			{
				var optionTableStyle = options.tableStyle;
				
				// < @deprecated 兼容2.8.0版本的驼峰命名CSS，将在未来版本移除
				//需要先转换可能的驼峰CSS命名，不然extend后的CSS可能重名而优先级混乱
				optionTableStyle = $.extend(true, {}, optionTableStyle);
				optionTableStyle.table = chartSupport.toLegalStyleNameObj(optionTableStyle.table);
				if(optionTableStyle.head)
				{
					optionTableStyle.head.row = chartSupport.toLegalStyleNameObj(optionTableStyle.head.row);
					optionTableStyle.head.cell = chartSupport.toLegalStyleNameObj(optionTableStyle.head.cell);
				}
				if(optionTableStyle.body)
				{
					optionTableStyle.body.row = chartSupport.toLegalStyleNameObj(optionTableStyle.body.row);
					optionTableStyle.body.rowOdd = chartSupport.toLegalStyleNameObj(optionTableStyle.body.rowOdd);
					optionTableStyle.body.rowEven = chartSupport.toLegalStyleNameObj(optionTableStyle.body.rowEven);
					optionTableStyle.body.rowHover = chartSupport.toLegalStyleNameObj(optionTableStyle.body.rowHover);
					optionTableStyle.body.rowSelected = chartSupport.toLegalStyleNameObj(optionTableStyle.body.rowSelected);
					optionTableStyle.body.cell = chartSupport.toLegalStyleNameObj(optionTableStyle.body.cell);
					optionTableStyle.body.cellOdd = chartSupport.toLegalStyleNameObj(optionTableStyle.body.cellOdd);
					optionTableStyle.body.cellEven = chartSupport.toLegalStyleNameObj(optionTableStyle.body.cellEven);
					optionTableStyle.body.cellHover = chartSupport.toLegalStyleNameObj(optionTableStyle.body.cellHover);
					optionTableStyle.body.cellSelected = chartSupport.toLegalStyleNameObj(optionTableStyle.body.cellSelected);
				}
				// > @deprecated 兼容2.8.0版本的驼峰命名CSS，将在未来版本移除
				
				tableStyle = $.extend(true, tableStyle, optionTableStyle);
			}
			
			//DataTable-1.11.3内置表头背景CSS添加了"!important"，这里也必须添加才能起作用
			chartSupport.tableCopyStyleBackground(tableStyle.head.row, tableStyle.head.row, true, true);
			
			//DataTable-1.11.3的固定列采用的sticky特性，导致单元格必须设置背景不然会变透明
			chartSupport.tableCopyStyleBackground(tableStyle.head.row, tableStyle.head.cell, false, true);
			chartSupport.tableCopyStyleBackground(tableStyle.body.row, tableStyle.body.cell);
			chartSupport.tableCopyStyleBackground(tableStyle.body.rowOdd, tableStyle.body.cellOdd);
			chartSupport.tableCopyStyleBackground(tableStyle.body.rowEven, tableStyle.body.cellEven);
			chartSupport.tableCopyStyleBackground(tableStyle.body.rowHover, tableStyle.body.cellHover);
			chartSupport.tableCopyStyleBackground(tableStyle.body.rowSelected, tableStyle.body.cellSelected);
			
			var headColor = (tableStyle.head.cell.color ? tableStyle.head.cell.color : tableStyle.head.row.color);
			
			//样式要加".dg-chart-table-content"限定，因为图表的数据透视表功能也采用的是DataTable组件，可能会处在同一个表格图表div内
			var qualifier = (isLocalStyle ? "." + name : "") + " .dg-chart-table-content";
			var qualifierV1 = (isLocalStyle ? "." + name : "") + ".dg-table-v1 .dg-chart-table-content";
			var qualifierV2 = (isLocalStyle ? "." + name : "") + ".dg-table-v2 .dg-chart-table-content";
			
			var css=
			[
				{
					name: (isLocalStyle ? "." + name : "") + " .dg-chart-table-title",
					value:
					{
						"font-size": chartFactory.toCssFontSize(theme.titleTheme.fontSize)
					}
				},
				{
					name: qualifier + " table.dataTable",
					value: chart.styleString(tableStyle.table)
				},
				{
					name: qualifier + " table.dataTable thead tr",
					value: chart.styleString(tableStyle.head.row)
				},
				{
					name:
					[
						qualifier + " table.dataTable thead tr th",
						qualifier + " table.dataTable thead tr td"
					],
					value: chart.styleString(tableStyle.head.cell)
				},
				{
					name: qualifier + " table.dataTable tbody tr",
					value: chart.styleString(tableStyle.body.row)
				},
				{
					name: qualifier + " table.dataTable tbody tr td",
					value: chart.styleString(tableStyle.body.cell)
				},
				{
					name:
					[
						qualifierV1 + " table.dataTable.stripe tbody tr.odd",
						qualifierV2 + " table.dataTable.stripe>tbody>tr:nth-child(odd)"
					],
					value: chart.styleString(tableStyle.body.rowOdd)
				},
				{
					name:
					[
						qualifierV1 + " table.dataTable.stripe tbody tr.odd td",
						qualifierV2 + " table.dataTable.stripe>tbody>tr:nth-child(odd)>*"
					],
					value: chart.styleString(tableStyle.body.cellOdd)
				},
				{
					name:
					[
						qualifierV1 + " table.dataTable.stripe tbody tr.even",
						qualifierV2 + " table.dataTable.stripe>tbody>tr:nth-child(even)"
					],
					value: chart.styleString(tableStyle.body.rowEven)
				},
				{
					name:
					[
						qualifierV1 + " table.dataTable.stripe tbody tr.even td",
						qualifierV2 + " table.dataTable.stripe>tbody>tr:nth-child(even)>*"
					],
					value: chart.styleString(tableStyle.body.cellEven)
				},
				{
					name:
					[
						qualifier + " table.dataTable.hover tbody tr:hover",
						qualifierV1 + " table.dataTable.hover.stripe tbody tr.odd:hover",
						qualifierV1 + " table.dataTable.hover.stripe tbody tr.even:hover",
						qualifierV2 + " table.dataTable.hover.stripe>tbody>tr:nth-child(odd):hover",
						qualifierV2 + " table.dataTable.hover.stripe>tbody>tr:nth-child(even):hover"
					],
					value: chart.styleString(tableStyle.body.rowHover)
				},
				{
					name:
					[
						qualifier + " table.dataTable.hover tbody tr:hover td",
						qualifierV1 + " table.dataTable.hover.stripe tbody tr.odd:hover td",
						qualifierV1 + " table.dataTable.hover.stripe tbody tr.even:hover td",
						qualifierV2 + " table.dataTable.hover.stripe>tbody>tr:nth-child(odd):hover>*",
						qualifierV2 + " table.dataTable.hover.stripe>tbody>tr:nth-child(even):hover>*",
					],
					value: chart.styleString(tableStyle.body.cellHover)
				},
				{
					name:
					[
						qualifier + " table.dataTable tbody tr.selected",
						qualifier + " table.dataTable.hover tbody tr:hover.selected",
						qualifierV1 + " table.dataTable.stripe tbody tr.odd.selected",
						qualifierV1 + " table.dataTable.stripe tbody tr.even.selected",
						qualifierV2 + " table.dataTable.stripe>tbody>tr:nth-child(odd).selected",
						qualifierV2 + " table.dataTable.display>tbody>tr:nth-child(odd).selected",
						qualifierV2 + " table.dataTable.stripe>tbody>tr:nth-child(even).selected",
						qualifierV2 + " table.dataTable.display>tbody>tr:nth-child(even).selected",
						
						qualifier + " table.dataTable.hover tbody tr:hover.selected",
						qualifierV1 + " table.dataTable.hover.stripe tbody tr.odd:hover.selected",
						qualifierV1 + " table.dataTable.hover.stripe tbody tr.even:hover.selected",
						qualifierV2 + " table.dataTable.hover.stripe>tbody>tr:nth-child(odd):hover.selected",
						qualifierV2 + " table.dataTable.hover.stripe>tbody>tr:nth-child(even):hover.selected"
					],
					value: chart.styleString(tableStyle.body.rowSelected)
				},
				{
					name:
					[
						qualifier + " table.dataTable tbody tr.selected td",
						qualifier + " table.dataTable.stripe tbody tr.odd.selected td",
						qualifier + " table.dataTable.stripe tbody tr.even.selected td",
						qualifier + " table.dataTable.hover tbody tr:hover.selected td",
						qualifierV2 + " table.dataTable>tbody>tr.selected>*",
						qualifierV2 + " table.dataTable.stripe>tbody>tr:nth-child(odd).selected>*",
						qualifierV2 + " table.dataTable.display>tbody>tr:nth-child(odd).selected>*",
						qualifierV2 + " table.dataTable.stripe>tbody>tr:nth-child(even).selected>*",
						qualifierV2 + " table.dataTable.display>tbody>tr:nth-child(even).selected>*",
						
						qualifier + " table.dataTable.hover tbody tr:hover.selected td",
						qualifierV1 + " table.dataTable.hover.stripe tbody tr.odd:hover.selected td",
						qualifierV1 + " table.dataTable.hover.stripe tbody tr.even:hover.selected td",
						qualifierV2 + " table.dataTable.hover.stripe>tbody>tr:nth-child(odd):hover.selected>*",
						qualifierV2 + " table.dataTable.hover.stripe>tbody>tr:nth-child(even):hover.selected>*",
					],
					value: chart.styleString(tableStyle.body.cellSelected)
				},
				{
					name: qualifierV1 + " table.dataTable thead th.sorting div.DataTables_sort_wrapper span",
					value:
					{
						"background": headColor
					}
				},
				{
					name: qualifierV1 + " table.dataTable thead th.sorting_asc div.DataTables_sort_wrapper span",
					value:
					{
						"border-bottom-color": headColor,
						"background": "none"
					}
				},
				{
					name: qualifierV1 + " table.dataTable thead th.sorting_desc div.DataTables_sort_wrapper span",
					value:
					{
						"border-top-color": headColor,
						"background": "none"
					}
				},
				{
					name:
					[
						qualifierV1 + " .dataTables_wrapper .dataTables_length select",
						qualifierV2 + " .dt-container .dt-length select"
					],
					value:
					{
						color: theme.color
					}
				},
				{
					name:
					[
						qualifierV1 + " .dataTables_wrapper .dataTables_length select option",
						qualifierV2 + " .dt-container .dt-length select option"
					],
					value:
					{
						color: theme.color,
						"background-color": chart.themeGradualColor(0)
					}
				},
				{
					name: qualifierV2 + " .dt-container .dt-scroll-body",
					value:
					{
						color: theme.color
					}
				},
				{
					name:
					[
						qualifierV2 + " table.dataTable>thead>tr>th",
						qualifierV2 + " table.dataTable>thead>tr>td"
					],
					value:
					{
						"border-bottom-color": chart.themeGradualColor(0)
					}
				},
				{
					name: qualifierV2 + " table.dataTable.dtfc-scrolling-left tr>.dtfc-fixed-left::after",
					value:
					{
						"box-shadow": "inset 10px 0 8px -8px " + chart.themeGradualColor(0.2)
					}
				},
				{
					name: qualifierV2 + " table.dataTable.dtfc-scrolling-right tr>.dtfc-fixed-right::after",
					value:
					{
						"box-shadow": "inset -10px 0 8px -8px " + chart.themeGradualColor(0.2)
					}
				},
				{
					name:
					[
						qualifierV1 + " .dataTables_wrapper .dataTables_filter input",
						qualifierV2 + " div.dt-container .dt-paging .dt-paging-button.current",
						qualifierV2 + " div.dt-container .dt-paging .dt-paging-button.current:hover",
						qualifierV2 + " div.dt-container .dt-input"
					],
					value:
					{
						"border-color": theme.borderColor
					}
				},
				{
					name:
					[
						qualifierV2 + " div.dt-container div.dt-buttons>.dt-button",
						qualifierV2 + " div.dt-container div.dt-buttons>div.dt-button-split .dt-button"
					],
					value:
					{
						"border-color": theme.borderColor
					}
				},
				{
					name:
					[
						qualifierV2 + " div.dt-container div.dt-buttons>.dt-button:focus:not(.disabled)",
						qualifierV2 + " div.dt-container div.dt-buttons>div.dt-button-split .dt-button:focus:not(.disabled)"
					],
					value:
					{
						"outline": "2px solid " + theme.borderColor
					}
				}
			];
			
			if(!isLocalStyle)
			{
				css.push(
				{
					name: " .dg-chart-table-title",
					value:
					{
						"color": theme.titleTheme.color,
						"background-color": theme.titleTheme.backgroundColor
					}
				});
			}
			
			return css;
		},
		forceUpdate);
	};
	
	chartSupport.tableCopyStyleBackground = function(from, to, force, important)
	{
		force = (force == null ? false : force);
		important = (important == null ? false : important);
		
		if(from["background-color"] && (force || !to["background-color"]))
			to["background-color"] = (important ? chartSupport.cssValueImportant(from["background-color"]) : from["background-color"]);
		
		if(from["background"] && (force || !to["background"]))
			to["background"] = (important ? chartSupport.cssValueImportant(from["background"]) : from["background"]);
	};
	
	chartSupport.tableEvalBodyHeightV1 = function(chart, $chartContent, dataTable)
	{
		var chartContentHeight = $chartContent.height();
		var container = $(dataTable.table().container());
		var containerHeight = container.outerHeight(true);
		var tableHeader = chartSupport.tableGetScrollHead(chart, $chartContent);
		var tableHeaderHeight = tableHeader.outerHeight(true);
		var tableBody = chartSupport.tableGetScrollBody(chart, $chartContent);
		var fixedColumnContainer = tableBody.closest(".DTFC_ScrollWrapper");
		var tableBodyHeight = chartContentHeight - tableHeaderHeight;
		tableBody.css("height", tableBodyHeight);
		tableBody.css("max-height", tableBodyHeight);
		fixedColumnContainer.css("height", tableBody.parent().height());
		
		containerHeight = container.outerHeight(true);
		
		//如果表格容器高度不等于图表内容限高，则重新设置
		if(containerHeight - chartContentHeight != 0)
		{
			tableBodyHeight = tableBodyHeight - (containerHeight - chartContentHeight);
			tableBody.css("height", tableBodyHeight);
			tableBody.css("max-height", tableBodyHeight);
			fixedColumnContainer.css("height", tableBody.parent().height());
		}
	};
	
	chartSupport.tableUpdateInternalData = function(chart, chartResult, updateOptions)
	{
		var renderOptions = chart.renderOptions();
		
		//自定义更新底层组件数据，当启用serverSide后，需要自定义调用其ajax配置项的callback更新数据，而非这里
		//格式为：function(updateOptions, chart, chartResult){ ... }
		var updateInternal = chartSupport.updateInternalOption(renderOptions);
		if(updateInternal)
		{
			updateInternal.call(renderOptions, updateOptions, chart, chartResult);
			return;
		}
		
		var dataTable = chart.internal();
		var rows = dataTable.rows();
		var datas = updateOptions.data;
		var removeRowIndexes = [];
		var startRowIndex = 0;
		var dataIndex = 0;
		
		rows.every(function(rowIndex)
		{
			if(rowIndex < startRowIndex)
				return;
			
			if(dataIndex >= datas.length)
				removeRowIndexes.push(rowIndex);
			else
				this.data(datas[dataIndex]);
			
			dataIndex++;
		});
		
		for(; dataIndex<datas.length; dataIndex++)
			dataTable.row.add(datas[dataIndex]);
		
		dataTable.rows(removeRowIndexes).remove();
		
		dataTable.draw();
		chartSupport.tableAdjustColumn(dataTable);
		
		if(renderOptions.carousel.enable)
		{
			var chartEle = chart.elementJquery();
			chartEle.data("tableCarouselPrepared", false);
			chartSupport.tableStartCarousel(chart);
		}
	};
	
	/**
	 * 调整图表表格。
	 * 当表格隐藏显示、位置调整、数据变更后，可能会出现表头、固定列错位的情况，需要重新调整。
	 */
	chartSupport.tableAdjustColumn = function(dataTable)
	{
		dataTable.columns.adjust();
		
		var initOptions = dataTable.init();
		
		if(initOptions.fixedHeader)
			dataTable.fixedHeader.adjust();
		
		/*
		if(initOptions.fixedColumns)
			dataTable.fixedColumns.relayout();
		*/
	};
	
	/**
	 * 表格准备轮播。
	 */
	chartSupport.tablePrepareCarousel = function(chart)
	{
		var renderOptions = chart.renderOptions();
		
		//此时需禁用轮播功能，不然dataTable.draw()会导致死循环
		if(chartSupport.serverSidePagingOption(renderOptions) != null)
			return;
		
		var chartContent = chartSupport.tableGetChartContent(chart);
		var dataTable = chart.internal();
		var rowIndexes = dataTable.rows().indexes();
		var rowCount = rowIndexes.length;
		
		//空表格
		if(rowCount == 0)
			return;
		
		var scrollBody = chartSupport.tableGetScrollBody(chart, chartContent);
		var scrollTable = $(".dataTable", scrollBody);
		
		var scrollBodyHeight = scrollBody.height();
		
		while(true)
		{
			var scrollTableHeight = scrollTable.height();
			
			//表格高度至少为容器高度两倍，保证滚动平滑
			if(scrollTableHeight >= scrollBodyHeight*2)
				break;
			
			//必须成倍添加数据，避免出现轮播次序混乱
			for(var i=0; i<rowCount; i++)
			{
				var addData = dataTable.row(rowIndexes[i]).data();
				dataTable.row.add(addData);
			}
			
			dataTable.draw();
		}
	};
	
	/**
	 * 表格开始轮播。
	 */
	chartSupport.tableStartCarousel = function(chart)
	{
		var renderOptions = chart.renderOptions();
		
		//此时需禁用轮播功能，不然dataTable.draw()会导致死循环
		if(chartSupport.serverSidePagingOption(renderOptions) != null)
			return;
		
		var chartEle = chart.elementJquery();
		var chartContent = chartSupport.tableGetChartContent(chart);
		var dataTable = chart.internal();
		var rowCount = dataTable.rows().indexes().length;
		
		var scrollBody = chartSupport.tableGetScrollBody(chart, chartContent);
		var scrollTable = $(".dataTable", scrollBody);
		
		//空表格，或者，"auto"且行数未溢出时不轮播
		if(rowCount == 0
			|| (renderOptions.carousel.enable == "auto" && (scrollTable.height() <= scrollBody.height())))
		{
			scrollTable.css("margin-top", "0px");
			return;
		}
		
		chartSupport.tableStopCarousel(chart);
		chartEle.data("tableCarouselStatus", "start");
		
		chartSupport.tableHandleCarousel(chart, renderOptions, chartEle, dataTable, scrollBody, scrollTable);
	};
	
	/**
	 * 表格停止轮播。
	 */
	chartSupport.tableStopCarousel = function(chart)
	{
		var chartEle = chart.elementJquery();
		chartEle.data("tableCarouselStatus", "stop");
		chartSupport.tableCarouselIntervalId(chart, null);
	};
	
	chartSupport.tableHandleCarousel = function(chart, renderOptions, chartEle, dataTable, scrollBody, scrollTable)
	{
		if(chartEle.data("tableCarouselStatus") == "stop")
			return;
		
		var carouselConfig = renderOptions.carousel;
		var doCarousel = true;
		
		//元素隐藏时会因为高度计算有问题导致浏览器卡死，所以隐藏式不实际执行轮播
		if(scrollBody.is(":hidden"))
			doCarousel = false;
		
		if(doCarousel)
		{
			if(chartEle.data("tableCarouselPrepared") != true)
			{
				chartSupport.tablePrepareCarousel(chart);
				chartEle.data("tableCarouselPrepared", true)
			}
			
			//不采用设置滚动高度的方式（scrollBody.scrollTop()），因为会出现影响整个页面滚动高度的情况
			var scrollTop = parseInt(scrollTable.css("margin-top"));
			scrollTop = (Math.abs(scrollTop) || 0);
			
			var tableBody = dataTable.table().body();
			var currentRow = undefined;
			var currentRowHeight = undefined;
			var currentRowVisibleHeight = undefined;
			
			var offset = 0;
			var removeRowIndexes = [];
			var addRowDatas = [];
			var doRemove = false;
			var $checkRow = $("> tr:first", tableBody);
			var tmpOffset = 0;
			
			while(true)
			{
				currentRow = $checkRow[0];
				currentRowHeight = $checkRow.outerHeight(true);
				currentRowVisibleHeight = currentRowHeight;
				
				if($checkRow.length == 0 || removeRowIndexes.length >= carouselConfig.overflowCount)
				{
					offset += tmpOffset;
					doRemove = true;
					break;
				}
				
				tmpOffset += currentRowHeight;
				
				if(scrollTop < tmpOffset)
				{
					currentRowVisibleHeight = tmpOffset - scrollTop;
					break;
				}
				
				var dtRow = dataTable.row($checkRow);
				removeRowIndexes.push(dtRow.index());
				addRowDatas.push(dtRow.data());
				$checkRow = $checkRow.next();
			}
			
			var needDraw = false;
			
			if(doRemove && removeRowIndexes.length > 0)
			{
				dataTable.rows(removeRowIndexes).remove();
				scrollTop = scrollTop - offset;
				needDraw = true;
			}
			
			if(doRemove && addRowDatas.length > 0)
			{
				dataTable.rows.add(addRowDatas);
				needDraw = true;
			}
			
			if(needDraw)
				dataTable.draw();
			
			var span = ($.isFunction(carouselConfig.span) ?
					carouselConfig.span(currentRow, currentRowVisibleHeight, currentRowHeight) : carouselConfig.span);
			
			scrollTable.css("margin-top", (0 - (scrollTop + span))+"px");
		}
		
		var interval = null;
		
		if(!$.isFunction(carouselConfig.interval))
		{
			interval = carouselConfig.interval;
		}
		else
		{
			if(doCarousel)
			{
				interval = carouselConfig.interval(currentRow, currentRowVisibleHeight, currentRowHeight);
			}
			else
			{
				//没有执行轮播时，无法执行interval函数，所以采用默认处理间隔（同轮播默认间隔）
				interval = 50;
			}
		}
		
		var intervalId = setTimeout(function()
		{
			chartSupport.tableHandleCarousel(chart, renderOptions, chartEle, dataTable, scrollBody, scrollTable);
		},
		interval);
		
		chartSupport.tableCarouselIntervalId(chart, intervalId);
	};
	
	/**
	 * 获取、设置表格轮播定时执行ID
	 * 
	 * @param chart
	 * @param intervalId 要设置的定时执行ID，为null则清除
	 */
	chartSupport.tableCarouselIntervalId = function(chart, intervalId)
	{
		var chartEle = chart.elementJquery();
		
		var curIntervalId = chartEle.data("tableCarouselIntervalId");
		
		if(intervalId === undefined)
			return curIntervalId;
		
		if(curIntervalId != null)
			clearInterval(curIntervalId);
		
		chartEle.data("tableCarouselIntervalId", intervalId);
	};
	
	//标签卡
	
	chartSupport.labelRender = function(chart, options)
	{
		options = $.extend(true,
		{
			dg:
			{
				//name 可选，名称
				//value 数值
				dataSignNames: { name: "name", value: "value" }
			}
		},
		options);
		
		var chartEle = chart.elementJquery();
		chartEle.addClass("dg-chart-label");
		
		options = chartSupport.inflateRenderOptions(chart,
		{
			//将在update中设置：
			//标签卡数据：
			// data:
			// [
			//	  {
			//	    //可选，标签名，默认为选项值
			//	    name: "...",
			//	    //标签值
			//	    value: ...,
			//	    //可选，标签条目元素css样式
			//	    itemStyle: { ... },
			//	    //可选，标签名元素css样式
			//	    nameStyle: { ... },
			//	    //可选，标签值元素css样式
			//	    valueStyle: { ... }
			//	  },
			//	  ...
			// ]
			
			//标签条目、标签名、标签值是否都行内显示
			inline: false,
			//是否以flex布局展示标签
			//弹性布局：true 是、居中间隔；false 否；"around" 居中间隔；"start" 左对齐；"end" 右对齐；"center" 居中；"between" 贴边间隔； 
			flex: false,
			//是否标签值在标签名之前展示
			valueFirst: false,
			//是否隐藏标签名
			hideName: false,
			//标签条目元素公用css样式，格式为：{ ... }
			itemStyle: undefined,
			 //标签名元素公用css样式，格式为：{ ... }
			nameStyle: undefined,
			//标签值元素公用css样式，格式为：{ ... }
			valueStyle: undefined
		},
		options);
		
		// < @deprecated 兼容2.7.0版本的{label:{name:{...},value:{...}}}配置项结构，未来版本会移除
		if(options.label && options.label.name)
			options.name = options.label.name;
		if(options.label && options.label.value)
			options.value = options.label.value;
		options.label = undefined;
		// > @deprecated 兼容2.7.0版本的{label:{name:{...},value:{...}}}配置项结构，未来版本会移除
		
		// < @deprecated 兼容2.8.0版本的{name:{ show:true|false, ... }, value:{...}}配置项结构，未来版本会移除
		if(options.name)
		{
			if(options.name.show !== undefined)
			{
				options.hideName = !options.name.show;
				options.name.show = undefined;
			}
			options.nameStyle = options.name;
		}
		options.name = undefined;
		
		if(options.value)
			options.valueStyle = options.value;
		options.value = undefined;
		// > @deprecated 兼容2.8.0版本的{name:{ show:true|false, ... }, value:{...}}配置项结构，未来版本会移除
		
		if(options.inline == true)
			chartEle.addClass("dg-chart-label-inline");
		
		if(options.hideName == true)
			chartEle.addClass("dg-hide-name");
		
		if(options.flex != null && options.flex != false)
		{
			chartEle.addClass("dg-chart-label-flex");
			
			if(options.flex == "start")
				chartEle.addClass("dg-chart-label-flex-start");
			else if(options.flex == "end")
				chartEle.addClass("dg-chart-label-flex-end");
			else if(options.flex == "center")
				chartEle.addClass("dg-chart-label-flex-center");
			else if(options.flex == "between")
				chartEle.addClass("dg-chart-label-flex-between");
			else
				chartEle.addClass("dg-chart-label-flex-around");
		}
		
		chart.internal(chart.element());
	};
	
	chartSupport.labelUpdate = function(chart, chartResult)
	{
		var renderOptions = chart.renderOptions();
		var dg = renderOptions.dg;
		var dataSignNames = dg.dataSignNames;
		var valueFirst = renderOptions.valueFirst;
		
		var dataSetBinds = chart.dataSetBindsMain();
		
		var $parent = $(chart.internal());
		
		$(".dg-chart-label-item", $parent).addClass("dg-chart-label-item-pending");
		
		var updateOptions = { data: [] };
		
		for(var i=0; i<dataSetBinds.length; i++)
		{
			var dataSetBind = dataSetBinds[i];
			
			var result = chart.resultOf(chartResult, dataSetBind);
			
			var nps = chart.dataSetFieldsOfSign(dataSetBind, dataSignNames.name);
			var vps = chart.dataSetFieldsOfSign(dataSetBind, dataSignNames.value);
			var hasNps = (nps && nps.length > 0);
			
			if(hasNps && nps.length != vps.length)
				throw new Error("The ["+dataSignNames.name+"] sign column must be "
						+"one-to-one with ["+dataSignNames.value+"] sign column");
			
			var namess = (hasNps ? chart.resultRowArrays(result, nps) : []);
			var valuess = chart.resultRowArrays(result, vps);
			
			var vpNames = [];
			if(!hasNps)
			{
				for(var j=0; j<vps.length; j++)
					vpNames[j] = chart.dataSetFieldAlias(dataSetBind, vps[j]);
			}
			
			for(var j=0; j<valuess.length; j++)
			{
				var values = valuess[j];
				var names = (hasNps ? namess[j] : vpNames);
				
				for(var k=0; k<names.length; k++)
				{
					var sv = { name: names[k], value: values[k] };
					chart.originalDataIndex(sv, dataSetBind, j);
					
					updateOptions.data.push(sv);
				}
			}
		}
		
		updateOptions = chart.inflateUpdateOptions(chartResult, updateOptions);
		
		for(var i=0; i<updateOptions.data.length; i++)
		{
			var labelData = updateOptions.data[i];
			
			var cssName = "dg-chart-label-item-"+i;
			
			var $label = $("."+ cssName, $parent);
			var $labelName = null;
			var $labelValue = null;
			
			if($label.length == 0)
			{
				$label = $("<div class='dg-chart-label-item "+cssName+"'></div>").appendTo($parent);
				
				if(valueFirst)
				{
					$labelValue = $("<div class='label-value'></div>").appendTo($label);
					$labelName = $("<div class='label-name'></div>").appendTo($label);
				}
				else
				{
					$labelName = $("<div class='label-name'></div>").appendTo($label);
					$labelValue = $("<div class='label-value'></div>").appendTo($label);
				}
			}
			else
			{
				$labelName = $(".label-name", $label);
				$labelValue = $(".label-value", $label);
				
				$label.removeClass("dg-chart-label-item-pending");
			}
			
			$labelName.html(labelData.name);
			$labelValue.html(labelData.value);
			$label.data("_dgChartLabelChartData", labelData);
			
			var itemStyle = chartSupport.evalLocalPlainObj(labelData.itemStyle, renderOptions.itemStyle);
			if(itemStyle)
				chart.elementStyle($label, itemStyle);
			
			var nameStyle = chartSupport.evalLocalPlainObj(labelData.nameStyle, renderOptions.nameStyle);
			if(nameStyle)
				chart.elementStyle($labelName, nameStyle);
			
			var valueStyle = chartSupport.evalLocalPlainObj(labelData.valueStyle, renderOptions.valueStyle);
			if(valueStyle)
				chart.elementStyle($labelValue, valueStyle);
		}
		
		$(".dg-chart-label-item-pending", $parent).remove();
	};
	
	chartSupport.labelResize = function(chart)
	{
		
	};
	
	chartSupport.labelDestroy = function(chart)
	{
		var chartEle = chart.elementJquery();
		chartEle.removeClass("dg-chart-label dg-chart-label-inline dg-hide-name dg-chart-label-flex "
								+"dg-chart-label-flex-around dg-chart-label-flex-start dg-chart-label-flex-end "
								+"dg-chart-label-flex-center dg-chart-label-flex-between");
		$(".dg-chart-label-item", chart.internal()).remove();
	};
	
	chartSupport.labelOn = function(chart, eventType, handler)
	{
		var handlerDelegation = function(htmlEvent)
		{
			var $label = $(this);
			var chartEvent = chartSupport.chartEventForHtml(chart, eventType, htmlEvent);
			chartSupport.labelSetChartEventData(chart, chartEvent, htmlEvent, $label);
			
			chart.callEventHandler(handler, chartEvent);
		};
		
		chart.registerEventHandlerDelegation(eventType, handler, handlerDelegation);
		$(chart.internal()).on(eventType, ".dg-chart-label-item", handlerDelegation);
	};
	
	chartSupport.labelOff = function(chart, eventType, handler)
	{
		var internal = $(chart.internal());
		
		chart.removeEventHandlerDelegation(eventType, handler, function(et, eh, ehd)
		{
			internal.off(et, ".dg-chart-label-item", ehd);
		});
	};
	
	chartSupport.labelSetChartEventData = function(chart, chartEvent, htmlEvent, $label)
	{
		var renderOptions= chart.renderOptions();
		var dg = renderOptions.dg;
		var dataSignNames = dg.dataSignNames;
		
		var chartData = $label.data("_dgChartLabelChartData");
		
		var data = {};
		
		if(chartData)
		{
			data[dataSignNames.name] = chartData.name;
			data[dataSignNames.value] = chartData.value;
		}
		
		chart.eventData(chartEvent, data);
		chart.eventOriginalDataIndex(chartEvent, chart.originalDataIndex(chartData));
	};
	
	//下拉框
	
	chartSupport.selectRender = function(chart, options)
	{
		options = $.extend(true,
		{
			dg:
			{
				//name 可选，名称
				//value 数值
				dataSignNames: { name: "name", value: "value" }
			}
		},
		options);
		
		var chartEle = chart.elementJquery();
		chartEle.addClass("dg-chart-select");
		
		options = chartSupport.inflateRenderOptions(chart,
		{
			//将在update中设置：
			//下拉框数据：
			// data:
			// [
			//	  {
			//	    //选项名，可选，默认为选项值
			//	    name: "...",
			//	    //选项值
			//	    value: ...,
			//	    //是否选中，可选，默认为：false
			//	    selected: true 或 false,
			//	    //选项css样式，可选
			//	    itemStyle: { ... }
			//	  },
			//	  ...
			// ]
			
			//下拉框ID
			id: undefined,
			//下拉框名称
			name: undefined,
			//是否多选
			multiple: false,
			//可见选项数目
			size: undefined,
			//默认选中项：null：默认；数值或其数组：选中指定索引的选项；
			selected: undefined,
			//前置添加的条目项，格式同data元素，或者其数组，通常用于添加默认选中项
			prepend: undefined,
			//下拉框是否填满父元素，"auto" 当是内联框时填满；true 是；false 否
			fillParent: "auto",
			//select框css样式，格式为：{ ... }
			selectStyle: undefined,
			//option选项公用css样式，格式为：{ ... }
			itemStyle: undefined
		},
		options);
		
		chartSupport.selectThemeStyleSheet(chart);
		
		var isDropdown = (!options.multiple && (options.size == null || options.size <= 1));
		
		if(isDropdown)
			chartEle.addClass("dg-chart-select-dropdown");
		
		var $select = $("<select class='dg-chart-select-select' />").appendTo(chartEle);
		
		if(options.id)
			$select.attr("id", options.id);
		if(options.name)
			$select.attr("name", options.name);
		if(options.multiple)
			$select.attr("multiple", "multiple");
		if(options.size != null)
			$select.attr("size", options.size);
		if(options.fillParent === true || (options.fillParent == "auto" && !isDropdown))
			$select.addClass("dg-fill-parent");
		if(options.selectStyle)
			chart.elementStyle($select, options.selectStyle);
		
		chart.internal($select[0]);
	};
	
	chartSupport.selectUpdate = function(chart, chartResult)
	{
		var renderOptions = chart.renderOptions();
		var dg = renderOptions.dg;
		var dataSignNames = dg.dataSignNames;
		
		var dataSetBinds = chart.dataSetBindsMain();
		
		var $select = $(chart.internal());
		
		$select.empty();
		
		var selected = renderOptions.selected;
		
		if(selected != null && typeof(selected) == "number")
			selected = [ selected ];
		
		var updateOptions = { data: [] };
		
		for(var i=0; i<dataSetBinds.length; i++)
		{
			var dataSetBind = dataSetBinds[i];
			
			var result = chart.resultOf(chartResult, dataSetBind);
			
			var nps = chart.dataSetFieldsOfSign(dataSetBind, dataSignNames.name);
			var vps = chart.dataSetFieldsOfSign(dataSetBind, dataSignNames.value);
			var hasNps = (nps && nps.length > 0);
			
			if(hasNps && nps.length != vps.length)
				throw new Error("The ["+dataSignNames.name+"] sign column must be "
						+"one-to-one with ["+dataSignNames.value+"] sign column");
			
			var namess = (hasNps ? chart.resultRowArrays(result, nps) : []);
			var valuess = chart.resultRowArrays(result, vps);
			
			for(var j=0; j<valuess.length; j++)
			{
				var values = valuess[j];
				var names = (hasNps ? namess[j] : values);
				
				for(var k=0; k<names.length; k++)
				{
					var sv = { name: names[k], value: values[k] };
					chart.originalDataIndex(sv, dataSetBind, j);
					
					updateOptions.data.push(sv);
				}
			}
		}
		
		updateOptions = chart.inflateUpdateOptions(chartResult, updateOptions);
		var data = updateOptions.data;
		
		if(renderOptions.prepend)
		{
			var newData = ($.isArray(renderOptions.prepend) ? renderOptions.prepend : [ renderOptions.prepend ]);
			data = newData.concat(data);
		}
		
		for(var i=0; i<data.length; i++)
		{
			var optData = data[i];
			
			var $opt = $("<option />").attr("value", optData.value)
				.html(optData.name ? optData.name : optData.value).appendTo($select);
			
			if(optData.selected || (selected != null && $.inArray(i, selected) > -1))
				$opt.attr("selected", "selected");
			
			$opt.data("_dgChartSelectOptionChartData", optData);
			
			var itemStyle = chartSupport.evalLocalPlainObj(optData.itemStyle, renderOptions.itemStyle);
			if(itemStyle)
				chart.elementStyle($opt, itemStyle);
		}
	};
	
	chartSupport.selectResize = function(chart)
	{
		
	};
	
	chartSupport.selectDestroy = function(chart)
	{
		var chartEle = chart.elementJquery();
		
		chartEle.removeClass("dg-chart-select dg-chart-select-dropdown dg-chart-beautify-scrollbar");
		
		$(chart.internal()).remove();
	};
	
	chartSupport.selectOn = function(chart, eventType, handler)
	{
		var handlerDelegation = function(htmlEvent)
		{
			var $select = $(this);
			var chartEvent = chartSupport.chartEventForHtml(chart, eventType, htmlEvent);
			chartSupport.selectSetChartEventData(chart, chartEvent, htmlEvent, $select);
			
			chart.callEventHandler(handler, chartEvent);
		};
		
		chart.registerEventHandlerDelegation(eventType, handler, handlerDelegation);
		$(chart.internal()).on(eventType, handlerDelegation);
	};
	
	chartSupport.selectOff = function(chart, eventType, handler)
	{
		var internal = $(chart.internal());
		
		chart.removeEventHandlerDelegation(eventType, handler, function(et, eh, ehd)
		{
			internal.off(et, ehd);
		});
	};
	
	chartSupport.selectSetChartEventData = function(chart, chartEvent, htmlEvent, $select)
	{
		var renderOptions = chart.renderOptions();
		var dg = renderOptions.dg;
		var dataSignNames = dg.dataSignNames;
		
		var $selectedOptions = $("option:selected", $select);
		var chartData = [];
		var data = [];
		
		for(var i=0; i<$selectedOptions.length; i++)
		{
			chartData.push($($selectedOptions[i]).data("_dgChartSelectOptionChartData"));
			
			var datai = (data[i] = {});
			datai[dataSignNames.name] = chartData[i].name;
			datai[dataSignNames.value] = chartData[i].value;
		}
		
		//单选
		if(!renderOptions.multiple)
		{
			chartData = (chartData.length > 0 ? chartData[0] : null);
			data = (data.length > 0 ? data[0] : null);
		}
		
		chart.eventData(chartEvent, data);
		chart.eventOriginalDataIndex(chartEvent, (renderOptions.multiple ? chart.originalDataIndexes(chartData) : chart.originalDataIndex(chartData)));
	};
	
	chartSupport.selectThemeStyleSheet = function(chart)
	{
		chart.themeStyleSheet(chartFactory.builtinPropName("SelectChart"), function()
		{
			var theme = chart.theme();
			
			var css=
			[
				{
					name: " .dg-chart-select-select",
					value:
					{
						"color": theme.color,
						"background-color": theme.backgroundColor,
						"border-color": theme.borderColor,
						"font-size": chartFactory.toCssFontSize(theme.fontSize)
					}
				},
				{
					name: " .dg-chart-select-select option",
					value:
					{
						"font-size": chartFactory.toCssFontSize(theme.fontSize)
					}
				},
				{
					name: ".dg-chart-select-dropdown .dg-chart-select-select option",
					value:
					{
						"color": theme.color,
						"background-color": chart.themeGradualColor(0.1)
					}
				}
			];
			
			return css;
		});
	};
	
	//原始数据
	
	chartSupport.rawDataRender = function(chart)
	{
		var ele = chart.elementJquery();
		ele.addClass("dg-chart-rawdata");
		
		$("<div class='dg-chart-rawdata-title' />").text(chart.name).appendTo(ele);
		$("<div class='dg-chart-rawdata-content' />").appendTo(ele);
	};
	
	chartSupport.rawDataUpdate = function(chart, chartResult)
	{
		var ele = chart.elementJquery();
		var $content = $("> .dg-chart-rawdata-content", ele);
		$(".dg-chart-rawdata-ds", $content).remove();
		
		var dataSetBinds = chart.dataSetBindsMain();
		
		for(var i=0; i<dataSetBinds.length; i++)
		{
			var dataSetBind = dataSetBinds[i];
			var dataSetAlias = chart.dataSetAlias(dataSetBind);
			var result = chart.resultOf(chartResult, dataSetBind);
			var datas = chart.resultDatas(result);
			
			var $ds = $("<div class='dg-chart-rawdata-ds' />").appendTo($content);
			$("<div class='dg-chart-rawdata-ds-name' />").text(dataSetAlias).appendTo($ds);
			var $dsd = $("<div class='dg-chart-rawdata-ds-data' />").appendTo($ds);
			
			for(var j=0; j<datas.length; j++)
			{
				var di = chartFactory.toJsonString(datas[j]);
				$("<div class='dg-chart-rawdata-ds-data-item' />").text(di).appendTo($dsd);
			}
		}
	};
	
	chartSupport.rawDataDestroy = function(chart)
	{
		var ele = chart.elementJquery();
		
		ele.removeClass("dg-chart-rawdata");
		$("> .dg-chart-rawdata-title", ele).remove();
		$("> .dg-chart-rawdata-content", ele).remove();
	};
	
	chartSupport.rawDataResize = function(chart){};
	chartSupport.rawDataOn = function(chart, eventType, handler){};
	chartSupport.rawDataOff = function(chart, eventType, handler){};
	
	//自定义
	
	chartSupport.customAsyncRender = function(chart)
	{
		var customRenderer = chartSupport.customGetCustomRenderer(chart, true);
		
		if(!customRenderer || customRenderer.asyncRender == null)
			return false;
		
		if(typeof(customRenderer.asyncRender) == "function")
			return customRenderer.asyncRender(chart);
		
		return (customRenderer.asyncRender == true);
	};
	
	chartSupport.customRender = function(chart)
	{
		var customRenderer = chartSupport.customGetCustomRenderer(chart, true);
		
		//如果未定义，则采用默认方式，避免空白页，又可以让用户浏览和调试数据
		if(!customRenderer)
		{
			chartSupport.rawDataRender(chart);
		}
		else
		{
			customRenderer.render(chart);
		}
	};
	
	chartSupport.customAsyncUpdate = function(chart, chartResult)
	{
		var customRenderer = chartSupport.customGetCustomRenderer(chart, true);
		
		if(!customRenderer || customRenderer.asyncUpdate == null)
			return false;
		
		if(typeof(customRenderer.asyncUpdate) == "function")
			return customRenderer.asyncUpdate(chart, chartResult);
		
		return (customRenderer.asyncUpdate == true);
	};
	
	chartSupport.customUpdate = function(chart, chartResult)
	{
		var customRenderer = chartSupport.customGetCustomRenderer(chart, true);
		
		//如果未定义，则采用默认方式，避免空白页，又可以让用户浏览和调试数据
		if(!customRenderer)
		{
			chartSupport.rawDataUpdate(chart, chartResult);
		}
		else
		{
			customRenderer.update(chart, chartResult);
		}
	};
	
	chartSupport.customResize = function(chart)
	{
		var customRenderer = chartSupport.customGetCustomRenderer(chart, true);
		
		//即使customRenderer未定义，resize操作也可以不抛出异常，因为不影响主体功能
		
		if(customRenderer && customRenderer.resize)
			customRenderer.resize(chart);
	};
	
	chartSupport.customDestroy = function(chart)
	{
		var customRenderer = chartSupport.customGetCustomRenderer(chart, true);
		
		if(!customRenderer)
		{
			chartSupport.rawDataDestroy(chart);
		}
		else if(customRenderer.destroy)
		{
			customRenderer.destroy(chart);
		}
	};
	
	chartSupport.customOn = function(chart, eventType, handler)
	{
		var customRenderer = chartSupport.customGetCustomRenderer(chart);
		
		if(customRenderer.on)
			customRenderer.on(chart, eventType, handler);
		else
			throw new Error("Chart renderer 's [on] rqeuired");
	};
	
	chartSupport.customOff = function(chart, eventType, handler)
	{
		var customRenderer = chartSupport.customGetCustomRenderer(chart);
		
		if(customRenderer.off)
			customRenderer.off(chart, eventType, handler);
		else
			throw new Error("Chart renderer 's [off] rqeuired");
	};
	
	chartSupport.customGetCustomRenderer = function(chart, nullable)
	{
		nullable = (nullable == null ? false : nullable);
		
		var renderer = chart.renderer();
		
		if(renderer == null && !nullable)
			throw new Error("Chart renderer required");
		
		return renderer;
	};
	
	//---------------------------------------------------------
	//    公用函数开始
	//---------------------------------------------------------
	
	//org.datagear.analysis.DataSetField.DataType
	chartSupport.DataSetFieldDataType =
	{
		STRING: "STRING",
		BOOLEAN: "BOOLEAN",
		NUMBER: "NUMBER",
		INTEGER: "INTEGER",
		DECIMAL: "DECIMAL",
		DATE: "DATE",
		TIME: "TIME",
		TIMESTAMP: "TIMESTAMP",
		UNKNOWN: "UNKNOWN"
	};
	
	//org.datagear.analysis.ResultDataFormat.TYPE_*
	chartSupport.ResultDataFormatType =
	{
		//TYPE_NUMBER
		NUMBER: "NUMBER",
		//TYPE_STRING
		STRING: "STRING"
	};
	
	/**
	 * 计算指定数据集字段的坐标轴类型。
	 */
	chartSupport.evalDataSetFieldAxisType = function(chart, dataSetField)
	{
		var type = "category";
		
		if(chartSupport.isDataTypeNumber(dataSetField))
		{
			type = "value";
		}
		else if(chartSupport.isDataTypeAboutDate(dataSetField))
		{
			var resultDataFormat = chart.resultDataFormat();
			if(!resultDataFormat)
				resultDataFormat = (chart.dashboard ? chart.dashboard.resultDataFormat() : null);
			
			if(resultDataFormat)
			{
				if(chartSupport.isDataTypeDate(dataSetField)
					&& resultDataFormat.dateType == chartSupport.ResultDataFormatType.NUMBER)
				{
					type = "time";
				}
				else if(chartSupport.isDataTypeTime(dataSetField)
					&& resultDataFormat.timeType == chartSupport.ResultDataFormatType.NUMBER)
				{
					type = "time";
				}
				else if(chartSupport.isDataTypeTimestamp(dataSetField)
					&& resultDataFormat.timestampType == chartSupport.ResultDataFormatType.NUMBER)
				{
					type = "time";
				}
			}
		}
		
		return type;
	};
	
	/**
	 * 填充图表渲染options。
	 * 注意： defaultOptions、builtinOptions，以及afterMergeHandlerFirst处理后的渲染options中，
	 *		 不应设置会在update函数中有设置的项（对于基本类型，不应出现，也不要将值设置为undefined、null，可能会影响图表内部逻辑；对于数组类型，可以不出现，也可以设置为：[]），
	 *		 因为update函数中调用的inflateUpdateOptions函数会把这里的设置高优先级深度合并。
	 *
	 * @param chart
	 * @param defaultOptions 默认options，优先级最低
	 * @param builtinOptions 内置options，优先级高于defaultOptions
	 * @param afterMergeHandlerFirst 可选，由defaultOptions、builtinOptions合并后的新渲染options处理函数，格式为：function(renderOptions, chart){ ... }
	 * @param beforeProcessHandler 可选
	 * @param seriesFirstAsTemplate 可选，是否使用defaultOptions和builtinOptions合并后的series[0]作为series后续元素的模板，true 是；false 否。默认值为：true 
	 * @returns 一个新的图表渲染options
	 */
	chartSupport.inflateRenderOptions = function(chart, defaultOptions, builtinOptions,
												afterMergeHandlerFirst, beforeProcessHandler, seriesFirstAsTemplate)
	{
		if(arguments.length == 4)
		{
			// (chart, defaultOptions, builtinOptions, seriesFirstAsTemplate)
			if(afterMergeHandlerFirst === true || afterMergeHandlerFirst === false)
			{
				seriesFirstAsTemplate = afterMergeHandlerFirst;
				afterMergeHandlerFirst = null;
			}
		}
		else if(arguments.length == 5)
		{
			// (chart, defaultOptions, builtinOptions, afterMergeHandlerFirst, seriesFirstAsTemplate)
			if(beforeProcessHandler === true || beforeProcessHandler === false)
			{
				seriesFirstAsTemplate = beforeProcessHandler;
				beforeProcessHandler = null;
			}
		}
		
		seriesFirstAsTemplate = (seriesFirstAsTemplate == null ? true : seriesFirstAsTemplate);
		
		var renderOptions = $.extend(true, {}, defaultOptions, builtinOptions);
		
		if(afterMergeHandlerFirst != null)
			afterMergeHandlerFirst(renderOptions, chart);
		
		var newBeforeProcessHandler = beforeProcessHandler;
		
		//使用series[0]作为series后续元素的模板，避免"dg-chart-options"中必须为series每个元素设置type等基础信息
		if(seriesFirstAsTemplate)
		{
			var series0 = (renderOptions.series && renderOptions.series[0] ?
							$.extend(true, {}, renderOptions.series[0]) : null);
			
			if(series0)
			{
				newBeforeProcessHandler = function(renderOptions, chart)
				{
					var series = renderOptions.series;
					
					for(var i=1; i<series.length; i++)
						series[i] = $.extend(true, {}, series0, series[i]);
					
					//必须指定id且不能重复，因为更新操作采用的是replaceMerge模式，必须有对应id
					for(var i=0; i<series.length; i++)
						series[i].id = i;
					
					if(beforeProcessHandler)
						beforeProcessHandler(renderOptions, chart);
				};
			}
		}
		
		return chart.inflateRenderOptions(renderOptions, newBeforeProcessHandler);
	};
	
	/**
	 * 指定数据集字段数据是否字符串类型。
	 */
	chartSupport.isDataTypeString = function(dataSetField)
	{
		var dataType = (dataSetField ? (dataSetField.type || dataSetField) : "");
		return (dataType == chartSupport.DataSetFieldDataType.STRING);
	};
	
	/**
	 * 指定数据集字段数据是否数值类型。
	 */
	chartSupport.isDataTypeNumber = function(dataSetField)
	{
		var dataType = (dataSetField ? (dataSetField.type || dataSetField) : "");
		return (dataType == chartSupport.DataSetFieldDataType.NUMBER
				|| dataType == chartSupport.DataSetFieldDataType.INTEGER
				|| dataType == chartSupport.DataSetFieldDataType.DECIMAL);
	};
	
	/**
	 * 指定数据集字段数据是否日期、时间、时间戳类型。
	 */
	chartSupport.isDataTypeAboutDate = function(dataSetField)
	{
		var dataType = (dataSetField ? (dataSetField.type || dataSetField) : "");
		return (dataType == chartSupport.DataSetFieldDataType.DATE
				|| dataType == chartSupport.DataSetFieldDataType.TIME
				|| dataType == chartSupport.DataSetFieldDataType.TIMESTAMP);
	};
	
	/**
	 * 指定数据集字段数据是否日期类型。
	 */
	chartSupport.isDataTypeDate = function(dataSetField)
	{
		var dataType = (dataSetField ? (dataSetField.type || dataSetField) : "");
		return (dataType == chartSupport.DataSetFieldDataType.DATE);
	};
	
	/**
	 * 指定数据集字段数据是否时间类型。
	 */
	chartSupport.isDataTypeTime = function(dataSetField)
	{
		var dataType = (dataSetField ? (dataSetField.type || dataSetField) : "");
		return (dataType == chartSupport.DataSetFieldDataType.TIME);
	};
	
	/**
	 * 指定数据集字段数据是否时间戳类型。
	 */
	chartSupport.isDataTypeTimestamp = function(dataSetField)
	{
		var dataType = (dataSetField ? (dataSetField.type || dataSetField) : "");
		return (dataType == chartSupport.DataSetFieldDataType.TIMESTAMP);
	};
	
	/**
	 * 为数组追加单个元素、数组
	 */
	chartSupport.appendElement = function(array, eles)
	{
		if($.isArray(eles))
		{
			for(var i=0; i<eles.length; i++)
				array.push(eles[i]);
		}
		else
			array.push(eles);
	};
	
	/**
	 * 为源数组追加不重复的元素。
	 * 
	 * @param array 待追加数组
	 * @param eles 追加元素、数组，可以是基本类型、对象类型
	 * @param propertyName 可选，当是对象类型时，用于指定判断重复的属性名
	 * @returns 追加的或重复元素的索引、或者索引数组
	 */
	chartSupport.appendDistinct = function(array, eles, propertyName)
	{
		return chartSupport.appendDistinctQuick(array, eles, {}, propertyName);
	};
	
	/**
	 * 为数组追加不重复的元素。
	 * 
	 * @param array 待追加数组
	 * @param eles 追加元素、数组，可以是基本类型、对象类型
	 * @param indexCache 索引缓存对象，用于存储arry中对应主键元素的索引，初始应为null、{}，格式为：{ 主键：索引数值 }
	 * @param propertyName 可选，当是对象类型时，用于指定判断重复的属性名
	 * @returns 追加的或重复元素的索引、或者索引数组
	 */
	chartSupport.appendDistinctQuick = function(array, eles, indexCache, propertyName)
	{
		indexCache = (indexCache == null ? {} : indexCache);
		
		var isArray = $.isArray(eles);
		
		if(!isArray)
			eles = [ eles ];
		
		var indexes = [];
		
		for(var i=0; i<eles.length; i++)
		{
			var key = (propertyName != null && eles[i] ? eles[i][propertyName] : eles[i]);
			var keyIdx = indexCache[key];
			
			if(keyIdx != null && keyIdx > -1)
			{
				indexes[i] = keyIdx;
			}
			else
			{
				keyIdx = chartSupport.findInArray(array, key, propertyName);
				
				if(keyIdx != null && keyIdx > -1)
				{
					indexes[i] = keyIdx;
					indexCache[key] = keyIdx;
				}
				else
				{
					array.push(eles[i]);
					indexCache[key] = array.length - 1;
					indexes[i] = array.length - 1;
				}
			}
		}
		
		return (isArray ? indexes : indexes[0]);
	};
	
	/**
	 * 在数组中查找元素，返回其索引
	 * 
	 * @param array
	 * @param value
	 * @param propertyName 当数组元素是对象类型时，用于指定判断属性名，格式为："..."、function(ele){ return ... }
	 * @returns 索引数值，-1 表示没有找到
	 */
	chartSupport.findInArray = function(array, value, propertyName)
	{
		var isPnFunction = (propertyName && $.isFunction(propertyName));
		
		for(var i=0; i<array.length; i++)
		{
			var ae = array[i];
			
			if(propertyName != null)
			{
				if(isPnFunction)
					ae = (ae ? propertyName(ae) : null);
				else
					ae = (ae ? ae[propertyName] : null);
			}
			
			if(ae == value)
				return i;
		}
		
		return -1;
	};
	
	/**
	 * 查找数组中第一个不为null的元素值，如果未找到，则返回undefined。
	 */
	chartSupport.findNonNull = function(array)
	{
		if(!array)
			return undefined;
		
		for(var i=0; i<array.length; i++)
		{
			if(array[i] != null)
				return array[i];
		}
		
		return undefined;
	};
	
	/**
	 * 查找数组中第一个不为空的元素值，如果未找到，则返回undefined。
	 */
	chartSupport.findNonEmpty = function(array)
	{
		if(!array)
			return undefined;
		
		for(var i=0; i<array.length; i++)
		{
			if(array[i] != null && array[i] != "")
				return array[i];
		}
		
		return undefined;
	};
	
	/**
	 * 校正obj.min、obj.max值，使得obj.min始终小于obj.max且都不为null。
	 */
	chartSupport.trimNumberRange = function(obj, defaultMin, defaultMax)
	{
		if(defaultMin == null)
			defaultMin = 0;
		if(defaultMax == null)
			defaultMax = 100;
		
		if(obj.min == null && obj.max == null)
		{
			obj.min = defaultMin;
			obj.max = defaultMax;
		}
		else if(obj.min == null)
		{
			obj.min = obj.max - Math.abs(obj.max)/2;
		}
		else if(obj.max == null)
		{
			obj.max = obj.min + Math.abs(obj.min)/2;
		}
		
		if(obj.min == obj.max)
			obj.max = obj.min + Math.abs(obj.min)/2;
		else if(obj.min > obj.max)
		{
			var min = obj.min;
			obj.min = obj.max;
			obj.max = min;
		}
		
		return obj;
	};
	
	/**
	 * 销毁图表的echarts对象。
	 */
	chartSupport.destroyChartEcharts = function(chart)
	{
		var internal = chart.internal();
		if(internal && !internal.isDisposed())
			internal.dispose();
	};
	
	/**
	 * 调整图表的echarts尺寸。
	 */
	chartSupport.resizeChartEcharts = function(chart)
	{
		var internal = chart.internal();
		if(internal)
			internal.resize();
	};
	
	chartSupport.bindChartEventHandlerForEcharts = function(chart, eventType, eventHanlder, chartEventDataSetter)
	{
		var hanlderDelegation = function(params)
		{
			var chartEvent = chart.eventNew(eventType, params);
			chartEventDataSetter(chart, chartEvent, params);
			
			// < @deprecated 兼容3.0.1版本的ChartEvent.chartType，将在未来版本移除
			chartEvent.chartType = "echarts";
			// > @deprecated 兼容3.0.1版本的ChartEvent.chartType，将在未来版本移除
			
			chart.callEventHandler(eventHanlder, chartEvent);
		};
		
		chart.registerEventHandlerDelegation(eventType, eventHanlder, hanlderDelegation);
		chart.internal().on(eventType, "series", hanlderDelegation);
	};
	
	//计算图例名
	chartSupport.legendNameForDataValues = function(chart, dataSetBinds, dataSetBind, dataSetAlias,
													valueProperties, valuePropertyIdx)
	{
		var legendName = dataSetAlias;
		
		if(dataSetBinds.length > 1 && valueProperties.length > 1)
		{
			legendName = dataSetAlias +"-" + chart.dataSetFieldAlias(dataSetBind, valueProperties[valuePropertyIdx]);
		}
		else if(valueProperties.length > 1)
		{
			legendName = chart.dataSetFieldAlias(dataSetBind, valueProperties[valuePropertyIdx]);
		}
		
		return legendName;
	};
	
	chartSupport.evalSymbolSizeMaxForScatter = function(chart, options, scatterType)
	{
		//涟漪效果会是散点显得很大，所以这里稍作调整
		var ratio = (scatterType == "effectScatter" ? 0.06 : undefined);
		return chartSupport.evalSymbolSizeMax(chart, options, ratio);
	};
	
	chartSupport.evalSymbolSizeMinForScatter = function(chart, options, symbolSizeMax, scatterType)
	{
		//最小涟漪散点不必调整
		return chartSupport.evalSymbolSizeMin(chart, options, symbolSizeMax, null);
	};
	
	/**
	 * 计算最大图符元素尺寸
	 * @param chart
	 * @param options
	 * @param ratio 可选，自动获取的比率
	 */
	chartSupport.evalSymbolSizeMax = function(chart, options, ratio)
	{
		var symbolSizeMax = (options && options.dg ? options.dg.symbolSizeMax : undefined);
		ratio = (ratio == null ? 0.08 : ratio);
		
		//根据图表元素尺寸自动计算
		if(!symbolSizeMax)
		{
			var chartEle = chart.elementJquery();
			symbolSizeMax =parseInt(Math.min(chartEle.width(), chartEle.height())*ratio);
		}
		
		return symbolSizeMax;
	};
	
	/**
	 * 计算最小图符元素尺寸
	 * @param chart
	 * @param options
	 * @param symbolSizeMax
	 * @param ratio 可选，自动获取的比率
	 */
	chartSupport.evalSymbolSizeMin = function(chart, options, symbolSizeMax, ratio)
	{
		var symbolSizeMin = (options && options.dg ? options.dg.symbolSizeMin : undefined);
		ratio = (ratio == null ? 0.15 : ratio);
		
		if(!symbolSizeMin)
		{
			symbolSizeMin = parseInt(symbolSizeMax * ratio);
			if(symbolSizeMin < 6)
				symbolSizeMin = 6;
		}
		
		return symbolSizeMin;
	};
	
	//计算数值的图符元素尺寸
	chartSupport.evalValueSymbolSize = function(value, minValue, maxValue, symbolSizeMax, symbolSizeMin)
	{
		if(symbolSizeMin == null)
			symbolSizeMin = 4;
		
		if(value == null || minValue == null || maxValue == null)
			return symbolSizeMin;
		
		if((maxValue-minValue) <= 0)
			return symbolSizeMin;
		
		var size = parseInt((value-minValue)/(maxValue-minValue)*symbolSizeMax);
		return (size < symbolSizeMin ? symbolSizeMin : size);
	};
	
	/**
	 * 计算系列数据数值的图符元素尺寸
	 * 
	 * @param series 系列对象：{ data: [ {value: ...}, ... ] }、或其数组
	 */
	chartSupport.evalSeriesDataValueSymbolSize = function(series, minValue, maxValue, symbolSizeMax, symbolSizeMin,
			valuePropertyName, valueElementIndex)
	{
		if(series == null)
			return;
		
		if(valuePropertyName == null)
			valuePropertyName = "value";
		
		if(!$.isArray(series))
			series = [ series ];
		
		for(var i=0; i<series.length; i++)
		{
			chartSupport.evalDataValueSymbolSize(series[i].data, minValue, maxValue, symbolSizeMax, symbolSizeMin,
						valuePropertyName, valueElementIndex);
		}
	};
	
	/**
	 * 计算系列数据数值的图符元素尺寸
	 * 
	 * @param data 数据对象：{value: ...}、或其数组
	 */
	chartSupport.evalDataValueSymbolSize = function(data, minValue, maxValue, symbolSizeMax, symbolSizeMin,
			valuePropertyName, valueElementIndex)
	{
		if(data == null)
			return;
		
		if(valuePropertyName == null)
			valuePropertyName = "value";
		
		if(!$.isArray(data))
			data = [ data ];
		
		for(var i=0; i<data.length; i++)
		{
			var obj = 	data[i];
			var value = obj[valuePropertyName];
			
			if(valueElementIndex != null)
				value = ($.isArray(value) && valueElementIndex < value.length ? value[valueElementIndex] : null);
			
			obj.symbolSize = chartSupport.evalValueSymbolSize(
				value, minValue, maxValue, symbolSizeMax, symbolSizeMin);
		}
	};
	
	chartSupport.appendCategoryNameAndData =function(categoryNames, categoryDatasMap, categoryName, categoryData)
	{
		chartSupport.appendDistinct(categoryNames, categoryName);
		
		var categoryDatas = (categoryDatasMap[categoryName] || (categoryDatasMap[categoryName] = []));
		chartSupport.appendElement(categoryDatas, categoryData);
	};
	
	chartSupport.splitDataByCategory =function(data, categoryNames, categoryDatasMap,
													defaultCategoryName, categoryPropertyName)
	{
		defaultCategoryName = (defaultCategoryName == null ? "" : defaultCategoryName);
		categoryPropertyName = (categoryPropertyName == null ? chartSupport.builtinCategoryPropName() : categoryPropertyName);
		
		for(var i=0; i<data.length; i++)
		{
			var di = data[i];
			var categoryName = (di == null ? defaultCategoryName : (di[categoryPropertyName] || defaultCategoryName));
			
			chartSupport.appendCategoryNameAndData(categoryNames, categoryDatasMap, categoryName, di);
		}
	};
	
	/**
	 * 从数据集结果中读取第一个不为空的数据标记数据值。
	 */
	chartSupport.resultFirstNonEmptyValueOfSign = function(chart, dataSetBind, result, valueSign)
	{
		var vp = chart.dataSetFieldOfSign(dataSetBind, valueSign);
		
		if(vp)
		{
			var values = chart.resultColumnArrays(result, vp);
			return chartSupport.findNonEmpty(values);
		}
		
		return undefined;
	};
	
	//初始化ECharts地图类图表的地图选项
	chartSupport.echartsMapChartInitMap = function(chart, options)
	{
		var map = options[builtinOptionNames.mapName];
		
		// < @deprecated 兼容5.2.0版本的dgMap图表选项，将在未来版本移除
		if(!map)
		{
			map = options["dgMap"];
		}
		// > @deprecated 兼容5.2.0版本的dgMap图表选项，将在未来版本移除
		
		// < @deprecated 兼容4.7.0版本的chart.map()函数功能，将在未来版本随之一起移除
		if(!map)
		{
			map = chart.map();
		}
		// > @deprecated 兼容4.7.0版本的chart.map()函数功能，将在未来版本随之一起移除
		
		//必须设置初始map，不然渲染会报错
		if(!map)
		{
			map = chartSupport.defaultMapName();
		}
		
		//不应替换原始地图名
		var coverOriginalMap = false;
		chartSupport.echartsSetMapOption(options, map, coverOriginalMap);
	};
	
	//渲染ECharts地图类图表
	chartSupport.echartsMapChartRender = function(chart, options)
	{
		chartSupport.echartsMapChartLoadMaps(chart, options, function()
		{
			chart.echartsInit(options);
			chart.statusRendered(true);
		});
	};
	
	//更新ECharts地图类图表
	chartSupport.echartsMapChartUpdate = function(chart, chartResult, updateOptions, renderOptions)
	{
		chartSupport.adaptArrayPropsForUpdateOptions(updateOptions, renderOptions);
		
		var updateMapOptions = chartSupport.echartsGetMapOptions(updateOptions);
		
		updateOptions = chart.inflateUpdateOptions(chartResult, updateOptions, function(updateOptions)
		{
			//inflateUpdateOptions()会将地图设置为renderOptions里的项，所以这里需要再次设置为updateMap
			$.extend(true, updateOptions, updateMapOptions);
			
			var renderMaps = chartSupport.echartsGetMapsDistinct(renderOptions);
			var updateMaps = chartSupport.echartsGetMapsDistinct(updateOptions);
			var mapChanged = (renderMaps.length !== updateMaps.length);
			
			if(!mapChanged)
			{
				for(var i=0; i<renderMaps.length; i++)
				{
					if(renderMaps[i] != updateMaps[i])
					{
						mapChanged = true;
						break;
					}
				}
			}
			
			if(mapChanged)
			{
				chartSupport.echartsResetMapSettings(updateOptions);
			}
		});
		
		chartSupport.echartsMapChartLoadMaps(chart, updateOptions, function()
		{
			chartSupport.echartsOptionsReplaceMerge(chart, updateOptions);
			chart.statusUpdated(true);
		});
	};
	
	//仅提取ECharts地图类图表选项中的非空地图名信息，并且保持原结构
	chartSupport.echartsGetMapOptions = function(echartsOptions)
	{
		var re = {};
		
		var geo = echartsOptions.geo;
		var series = echartsOptions.series;
		
		if(geo)
		{
			if($.isArray(geo))
			{
				re.geo = [];
				
				for(var i=0; i<geo.length; i++)
				{
					re.geo[i] = (geo[i].map ? { map: geo[i].map } : {});
				}
			}
			else
			{
				re.geo = (geo.map ? { map: geo.map } : {});
			}
		}
		
		if(series)
		{
			if($.isArray(series))
			{
				re.series = [];
				
				for(var i=0; i<series.length; i++)
				{
					re.series[i] = (series[i].type == "map" && series[i].map ? { map: series[i].map } : {});
				}
			}
			else
			{
				re.series = (series.type == "map" && series.map ? { map: series.map } : {});
			}
		}
		
		return re;
	};
	
	//仅提取ECharts地图类图表选项中的不重复地图名信息
	chartSupport.echartsGetMapsDistinct = function(echartsOptions)
	{
		var re = [];
		
		var maps = [];
		var geo = echartsOptions.geo;
		var series = echartsOptions.series;
		
		if(geo)
		{
			if($.isArray(geo))
			{
				for(var i=0; i<geo.length; i++)
				{
					if(geo[i].map)
					{
						maps.push(geo[i].map);
					}
				}
			}
			else
			{
				if(geo.map)
				{
					maps.push(geo.map);
				}
			}
		}
		
		if(series)
		{
			if($.isArray(series))
			{
				for(var i=0; i<series.length; i++)
				{
					if(series[i].type == "map" && series[i].map)
					{
						maps.push(series[i].map);
					}
				}
			}
			else
			{
				if(series.type == "map" && series.map)
				{
					maps.push(series.map);
				}
			}
		}
		
		chartSupport.appendDistinct(re, maps);
		
		return re;
	};
	
	//设置ECharts地图类图表选项中的地图名
	chartSupport.echartsSetMapOption = function(echartsOptions, map, force)
	{
		var geo = echartsOptions.geo;
		var series = echartsOptions.series;
		
		if(geo)
		{
			if($.isArray(geo))
			{
				for(var i=0; i<geo.length; i++)
				{
					if(geo[i].map == null || force)
					{
						geo[i].map = map;
					}
				}
			}
			else
			{
				if(geo.map == null || force)
				{
					geo.map = map;
				}
			}
		}
		
		if(series)
		{
			if($.isArray(series))
			{
				for(var i=0; i<series.length; i++)
				{
					if(series[i].type == "map" && (series[i].map == null || force))
					{
						series[i].map = map;
					}
				}
			}
			else
			{
				if(series.type == "map" && (series.map == null || force))
				{
					series.map = map;
				}
			}
		}
	};
	
	//重置ECharts地图类图表的中心位置、缩放比例
	chartSupport.echartsResetMapSettings = function(echartsOptions)
	{
		var geo = echartsOptions.geo;
		var series = echartsOptions.series;
		
		if(geo)
		{
			if($.isArray(geo))
			{
				for(var i=0; i<geo.length; i++)
				{
					geo[i].center = null;
					geo[i].zoom = 1;
				}
			}
			else
			{
				geo.center = null;
				geo.zoom = 1;
			}
		}
		
		if(series)
		{
			if($.isArray(series))
			{
				for(var i=0; i<series.length; i++)
				{
					if(series[i].type == "map")
					{
						series[i].center = null;
						series[i].zoom = 1;
					}
				}
			}
			else
			{
				if(series.type == "map")
				{
					series.center = null;
					series.zoom = 1;
				}
			}
		}
	};
	
	//加载ECharts地图类图表中的所有地图，并在全部加载完成后调用callbak
	chartSupport.echartsMapChartLoadMaps = function(chart, options, callback)
	{
		var maps = chartSupport.echartsGetMapsDistinct(options);
		var needLoads = [];
		
		for(var i=0; i<maps.length; i++)
		{
			if(!chart.echartsMapRegistered(maps[i]))
			{
				needLoads.push(maps[i]);
			}
		}
		
		if(needLoads.length == 0)
		{
			callback();
			return;
		}
		
		var loadedDeferreds = [];
		
		for(var i=0; i<needLoads.length; i++)
		{
			loadedDeferreds[i] = $.Deferred();
		}
		
		$.when.apply($, loadedDeferreds).done(function()
		{
			callback();
		});
		
		for(var i=0; i<needLoads.length; i++)
		{
			chart.echartsLoadMap(needLoads[i],
			{
				needLoadMap: needLoads[i],
				mapLoadedDeferred: loadedDeferreds[i],
				success: function()
				{
					this.mapLoadedDeferred.resolve();
				},
				error: function(jqXHR, textStatus, errorThrown)
				{
					this.mapLoadedDeferred.reject();
					chartFactory.logException("Load map '"+this.needLoadMap+"' error : " + (errorThrown || textStatus));
				}
			});
		}
	};
	
	/**
	 * 将值数组对象（{value: [name, value]}）格式的options.series[i].data元素适配为与options.series[i].type匹配的格式。
	 * 比如，对于"pie"的type，应适配为名值对象：{ name: name, value: value }格式，图表才能正确显示。
	 * 如果originalSeriesType与options.series[i].type相同，则不进行处理。
	 * 
	 * 某些内置图表允许修改series[i].type来自定义系列的类型，而不同类型的数据格式规范不同，所以需要适配。
	 * 
	 * @param chart
	 * @param options
	 * @param originalSeriesType
	 * @param nameIndex 可选，name在值数组对象的索引，默认为：0
	 * @param valueIndex 可选，value在值数组对象的索引，默认为：1
	 */
	chartSupport.adaptValueArrayObjSeriesData = function(chart, options, originalSeriesType, nameIndex, valueIndex)
	{
		nameIndex = (nameIndex == null ? 0 : nameIndex);
		valueIndex = (valueIndex == null ? 1 : valueIndex);
		
		var series = (options.series || []);
		
		for(var i=0; i<series.length; i++)
		{
			var type = series[i].type;
			
			if(type == originalSeriesType)
				continue;
			
			var seriesData = (series[i].data || []);
			
			//这些图表不支持值数组对象格式的数据，支持名值格式的数据，因此需要适配
			if(type == "pie" || type == "funnel" || type == "map"
				|| type == "wordCloud" || type == "liquidFill")
			{
				for(var j=0; j<seriesData.length; j++)
				{
					var value = (seriesData[j].value || []);
					seriesData[j].name = value[nameIndex];
					seriesData[j].value = value[valueIndex];
				}
			}
		}
	};
	
	/**
	 * 从obj中提取名值对象。
	 * 
	 * @param obj 待提取的对象，格式为：{name: ..., value: ...}、{ value: [..., ...] }
	 * @param nameProperty
	 * @param valueProperty
	 * @param nameIndex 可选，当obj.value是数组时，名在值数组对象的索引，默认为：0
	 * @param valueIndex 可选，当obj.value是数组时，值在值数组对象的索引，默认为：1
	 * @returns { nameProperty: ...,  valueProperty: ...}
	 */
	chartSupport.extractNameValueStyleObj = function(obj, nameProperty, valueProperty, nameIndex, valueIndex)
	{
		nameIndex = (nameIndex == null ? 0 : nameIndex);
		valueIndex = (valueIndex == null ? 1 : valueIndex);
		
		var re = undefined;
		
		if(obj)
		{
			re = {};
			
			var name = obj.name;
			var value = obj.value;
			
			//{ value: [..., ...] }
			if($.isArray(value))
			{
				name = value[nameIndex];
				value = value[valueIndex];
			}
			
			re[nameProperty] = name;
			re[valueProperty] = value;
		}
		
		return re;
	};
	
	chartSupport.evalLocalPlainObj = function(localPlainObj, publicPlainObj)
	{
		var re = null;
		
		if(localPlainObj && publicPlainObj)
			re = $.extend({}, publicPlainObj, localPlainObj);
		else if(publicPlainObj)
			re = publicPlainObj;
		else if(localPlainObj)
			re = localPlainObj;
		
		return re;
	};
	
	chartSupport.cssValueImportant = function(cssValue)
	{
		if(!cssValue)
			return cssValue;
		
		cssValue = (typeof(cssValue) == "string" ? cssValue : cssValue.toString());
		
		if(cssValue.indexOf("!important") < 0)
			cssValue += " !important";
		
		return cssValue;
	};
	
	chartSupport.toLegalStyleNameObj = function(obj)
	{
		if(!obj)
			return obj;
		
		var re = {};
		
		for(var p in obj)
		{
			var name = chartFactory.toLegalStyleName(p);
			var value = obj[p];
			
			re[name] = value;
		}
		
		return re;
	};
	
	chartSupport.echartsOptionsReplaceMerge = function(chart, options, replaceMerge)
	{
		if(replaceMerge == null)
		{
			replaceMerge = [];
			for(var p in options)
				replaceMerge.push(p);
		}
		
		var opts =
		{
			replaceMerge: replaceMerge
		};
		
		chart.echartsOptions(options, opts);
	};
	
	chartSupport.dataSetBindMainNonNull = function(chart, renderError)
	{
		renderError = (renderError == null ? true : renderError);
		
		var dataSetBind = chart.dataSetBindMain();
		
		if(dataSetBind == null)
		{
			if(renderError)
				$("<div />").html("Main DataSetBind required").appendTo(chart.elementJquery());
			
			throw new Error("Main DataSetBind required");
		}
		
		return dataSetBind;
	};
	
	chartSupport.chartEventForHtml = function(chart, type, htmlEvent)
	{
		var event = chart.eventNew(type, htmlEvent);
		
		// < @deprecated 兼容3.0.1版本的ChartEvent.chartType，将在未来版本移除
		event.chartType = "html";
		// > @deprecated 兼容3.0.1版本的ChartEvent.chartType，将在未来版本移除
		
		return event;
	};
	
	chartSupport.builtinCategoryPropName = function()
	{
		return chartFactory.builtinPropName("Category");
	};
	
	chartSupport.inflatePropertyMapWithCategory = function(propertyMap, categoryProperty, categoryPropName)
	{
		var categoryPropName = (categoryPropName == null ? chartSupport.builtinCategoryPropName() : categoryPropName);
		propertyMap[categoryPropName] = categoryProperty;
		
		return propertyMap;
	};
	
	chartSupport.legendNameForDataCategory = function(dataSetBinds, dataSetAlias, categoryName)
	{
		return (dataSetBinds.length > 1 ? dataSetAlias +"-" + categoryName : categoryName);
	};
	
	//计算数组数据最小/最大值
	//range 待填充的最小/最大值对象，格式为：{ min: 数值, max: 数值 }
	//data 数组
	//propertyName0 可选，当data[i]是对象或数组时，取值属性
	//propertyName1 可选，当data[i][propertyName0]是对象或数组时，取值属性
	chartSupport.evalArrayDataRange = function(range, data, propertyName0, propertyName1)
	{
		if(data == null)
			return range;
		
		for(var i=0; i<data.length; i++)
		{
			var val = (data[i] == null ? null : data[i]);
			
			if(propertyName0 != null)
				val = (val == null ? null : val[propertyName0]);
			
			if(propertyName1 != null)
				val = (val == null ? null : val[propertyName1]);
			
			if(val != null)
			{
				range.min = (range.min == null ? val : Math.min(range.min, val));
				range.max = (range.max == null ? val : Math.max(range.max, val));
			}
		}
		
		return range;
	};
	
	/**
	 * 从updateOptions.series[i].data[i]提取轴数据，并设置为updateAxis.data轴数据。
	 * 
	 * @param renderOptions 渲染选项，其中的builtinOptionNames.sortAxisData配置项可以控制数据排序方式，格式为：
	 *						"asc"、"ASC"：升序；
	 *						"desc"、"DESC"：降序；
	 *						自定义排序函数：function(a, b){}；
	 *						null、false、其他：不排序；
	 *						注意：ECharts对于轴type为"value"、"time"的，仅设置"desc"是无效的，需要把轴type改为"category"
	 * @param updateOptions 更新选项，格式应为：{ series: [ { data: [ ... ] } ] }
	 * @param updateAxis 要填充轴数据的更新的轴对象，格式应为：{ data: [ 基本类型值, ...], ... }
	 * @param valueExtractor 轴数据值提取器，格式同chartSupport.sortAxisDataForUpdateOptions的valueExtractor参数
	 * @param sortSeriesData 可选，是否排序系列数据，默认值为：true。
	 */
	chartSupport.inflateAxisDataForEchartsUpdateOptions = function(renderOptions, updateOptions, updateAxis, valueExtractor, sortSeriesData)
	{
		sortSeriesData = (sortSeriesData == null ? true : sortSeriesData);
		
		var axisData = [];
		var indexCache = {};
		var isValueExtractorFunc = $.isFunction(valueExtractor);
		var isValueExtractorAry = (!isValueExtractorFunc && $.isArray(valueExtractor));
		var valueExtractors = [];
		
		var series = (updateOptions.series || []);
		$.each(series, function(i, s)
		{
			var data = (s.data || []);
			var myData = [];
			var myValueExtractor = null;
			
			if(isValueExtractorFunc)
				myValueExtractor = valueExtractor;
			else if(isValueExtractorAry)
				myValueExtractor = valueExtractor[i];
			else
				myValueExtractor = valueExtractor.get(s);
			
			$.each(data, function(j, d)
			{
				var v = myValueExtractor(d, s);
				myData.push(v);
			});
			
			valueExtractors.push(myValueExtractor);
			chartSupport.appendDistinctQuick(axisData, myData, indexCache);
		});
		
		updateAxis.data = axisData;
		
		chartSupport.sortAxisDataForUpdateOptions(renderOptions, updateOptions, updateAxis,
					true, sortSeriesData, valueExtractors);
	};
	
	/**
	 * 依据renderOptions中的排序配置对updateAxis.data、updateOptions.series[i].data进行排序。
	 * 
	 * @param renderOptions 渲染选项，其中的builtinOptionNames.sortAxisData配置项用于控制数据排序方式，格式为：
	 *						"asc"、"ASC"：升序；
	 *						"desc"、"DESC"：降序；
	 *						自定义排序函数：function(a, b){}；
	 *						null、false、其他：不排序；
	 *						注意：ECharts对于轴type为"value"、"time"的，仅设置"desc"是无效的，需要把轴type改为"category"
	 * @param updateOptions 要排序的更新选项，格式应为：{ series: [ { data: [ ... ] } ] }
	 * @param updateAxis 要排序的轴对象，格式应为：{ data: [ 基本类型值, ...], ... }
	 * @param sortAxisData 是否对updateAxis.data进行排序
	 * @param sortSeriesData 是否对updateOptions.series[i].data按照updateAxis.data的顺序重排
	 * @param valueExtractor 可选，当sortSeriesData为true时，updateOptions.series[i].data[i]的排序值提取器，格式为：
	 *						function(dataEle, seriesEle){}
	 *						或者
	 *						{ get: function(seriesEle){ return 轴数据值提取器函数对象; } }
	 *						或者
	 *						[ ... ]
	 *						其元素索引与updateOptions.series[i]索引对应
	 */
	chartSupport.sortAxisDataForUpdateOptions = function(renderOptions, updateOptions, updateAxis,
					sortAxisData, sortSeriesData, valueExtractor)
	{
		if(!sortAxisData && !sortSeriesData)
			return;
		
		var sortHandler = chartSupport.sortAxisDataOption(renderOptions);
		
		if(chartFactory.isString(sortHandler))
		{
			sortHandler = sortHandler.toLowerCase();
			
			if(sortHandler == "asc")
			{
				sortHandler = function(a, b)
				{
					if(a == b)
						return 0;
					else
						return (a < b ? -1 : 1);
				};
			}
			else if(sortHandler == "desc")
			{
				sortHandler = function(a, b)
				{
					if(a == b)
						return 0;
					else
						return (a > b ? -1 : 1);
				};
			}
		}
		
		var axisData = updateAxis.data;
		var isValueExtractorFunc = $.isFunction(valueExtractor);
		var isValueExtractorAry = (!isValueExtractorFunc && $.isArray(valueExtractor));
		
		if($.isFunction(sortHandler))
		{
			axisData.sort(sortHandler);
			
			if(sortSeriesData)
			{
				var indexCache = {};
				$.each(axisData, function(i, a)
				{
					indexCache[a] = i;
				});
				
				var series = (updateOptions.series || []);
				$.each(series, function(i, s)
				{
					var data = (s.data || []);
					var myValueExtractor = null;
					
					if(isValueExtractorFunc)
						myValueExtractor = valueExtractor;
					else if(isValueExtractorAry)
						myValueExtractor = valueExtractor[i];
					else
						myValueExtractor = valueExtractor.get(s);
					
					data.sort(function(da, db)
					{
						var va = myValueExtractor(da, s);
						var vb = myValueExtractor(db, s);
						var ia = indexCache[va];
						var ib = indexCache[vb];
						
						if(ia == ib)
							return 0;
						else
							return (ia < ib ? -1 : 1);
					});
				});
			}
		}
	};
	
	chartSupport.sortAxisDataOption = function(options)
	{
		var value = chartFactory.optionValue(options, builtinOptionNames.sortAxisData);
		
		// < @deprecated 兼容5.2.0版本的dgSortAxisData图表选项，将在未来版本移除
		if(value == null)
		{
			value = options["dgSortAxisData"];
		}
		// > @deprecated 兼容5.2.0版本的dgSortAxisData图表选项，将在未来版本移除
		
		return value;
	};
	
	chartSupport.serverSidePagingOption = function(options, value)
	{
		return chartFactory.optionValue(options, builtinOptionNames.serverSidePaging, value);
	};
	
	chartSupport.updateInternalOption = function(options, value)
	{
		return chartFactory.optionValue(options, builtinOptionNames.updateInternal, value);
	};
	
	chartSupport.inflateAxisDataExtractors =
	{
		property: function(name)
		{
			var extractor = function(de)
			{
				return (de ? de[name] : null);
			};
			
			return extractor;
		},
		element: function(idx)
		{
			var extractor = function(de)
			{
				return (de ? de[idx] : null);
			};
			
			return extractor;
		},
		valueElement: function(idx)
		{
			var extractor = function(de)
			{
				return (de && de.value ? de.value[idx] : null);
			};
			
			return extractor;
		}
	};
	
	/**
	 * 获取默认地图名。
	 * 地图类图表需要默认地图执行render初始渲染。
	 * 注意：返回的默认地图名应是在dashboardFactory.js中dftBuiltinChartMaps的其中之一。
	 */
	chartSupport.defaultMapName = function()
	{
		//默认中国地图，这里应使用"china"，因为echarts内部只对"china"地图名的地图才会自动绘制右下角的南海诸岛缩略图
		return "china";
	};
	
	chartSupport.adaptArrayPropsForUpdateOptions = function(updateOptions, renderOptions)
	{
		for(var name in updateOptions)
		{
			var renderValue = renderOptions[name];
			var updateValue = updateOptions[name];
			var isRenderArray = $.isArray(renderValue);
			var isUpdateArray = $.isArray(updateValue);
			
			//如果渲染选项是数组，更新选项不是，应把更新选项包裹为数组
			if(isRenderArray && !isUpdateArray)
			{
				updateValue = [ updateValue ];
			}
			
			updateOptions[name] = updateValue;
		}
	};
	
	//---------------------------------------------------------
	//    公用函数结束
	//---------------------------------------------------------
})
(this);
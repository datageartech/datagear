/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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
 *   datagear-chartFactory.js
 */
(function(global)
{
	var chartFactory = (global.chartFactory || (global.chartFactory = {}));
	var chartSupport = (chartFactory.chartSupport || (chartFactory.chartSupport = {}));
	
	// < @deprecated 兼容1.8.1版本的window.chartSupport变量名，未来版本会移除
	global.chartSupport = chartSupport;
	// > @deprecated 兼容1.8.1版本的window.chartSupport变量名，未来版本会移除
	
	//折线图
	
	chartSupport.lineRender = function(chart, nameSign, valueSign, options)
	{
		chartSupport.chartSignNameMap(chart, { name: nameSign, value: valueSign });
		
		var chartDataSet = chart.chartDataSetFirst();
		var np = chart.dataSetPropertyOfSign(chartDataSet, nameSign);
		var vps = chart.dataSetPropertiesOfSign(chartDataSet, valueSign);
		
		options = chartSupport.inflateRenderOptions(chart,
		{
			//扩展配置项：是否堆叠
			dgStack: false,
			//扩展配置项：是否平滑
			dgSmooth: false,
			//扩展配置项：是否面积
			dgArea: false,
			
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
				//将在update中设置：
				//data
			},
			xAxis: {
				name: chart.dataSetPropertyLabel(np),
				nameGap: 5,
				type: chartSupport.evalDataSetPropertyAxisType(chart, np),
				boundaryGap: false
			},
			yAxis: {
				name: (vps.length == 1 ? chart.dataSetPropertyLabel(vps[0]) : ""),
				nameGap: 5,
				type: "value"
			},
			series:
			[
				//将在update中设置：
				//{}
				//设初值以免渲染报错
				{
					type: "line"
				}
			]
		},
		options);
		
		chart.echartsInit(options);
	};
	
	chartSupport.lineUpdate = function(chart, results)
	{
		var signNameMap = chartSupport.chartSignNameMap(chart);
		var renderOptions= chart.renderOptions();
		
		var chartDataSets = chart.chartDataSetsMain();
		
		var legendData = [];
		var series = [];
		
		for(var i=0; i<chartDataSets.length; i++)
		{
			var chartDataSet = chartDataSets[i];
			
			var dataSetName = chart.chartDataSetName(chartDataSet);
			var result = chart.resultOf(results, chartDataSet);
			
			var np = chart.dataSetPropertyOfSign(chartDataSet, signNameMap.name);
			var vps = chart.dataSetPropertiesOfSign(chartDataSet, signNameMap.value);
			
			for(var j=0; j<vps.length; j++)
			{
				var legendName = chartSupport.legendNameForMultipleSeries(chart, chartDataSets, i, dataSetName, vps, j);
				//使用{value: [name,value]}格式可以更好地兼容category、value、time坐标轴类型
				var data = chart.resultValueObjects(result, [ np, vps[j] ]);
				
				chart.originalInfo(data, chartDataSet);
				
				var mySeries = {type: "line", name: legendName, data: data};
				
				//折线图按数据集分组展示没有效果，所以都使用同一个堆叠
				if(renderOptions.dgStack)
					mySeries.stack = "stack";
				if(renderOptions.dgSmooth)
					mySeries.smooth = true;
				if(renderOptions.dgArea)
					mySeries.areaStyle = {};
				
				legendData.push(legendName);
				series.push(mySeries);
			}
		}
		
		var options = { legend: {data: legendData}, series: series };
		//需要明确重置轴坐标值，不然图表刷新有数据变化时，轴坐标不能自动更新
		options.xAxis = {data: null};
		
		options = chart.inflateUpdateOptions(results, options, function(options)
		{
			chartSupport.adaptValueArrayObjSeriesData(chart, options, "line");
		});
		
		chart.echartsOptions(options);
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
		var signNameMap = chartSupport.chartSignNameMap(chart);
		
		var echartsData = echartsEventParams.data;
		var data = chartSupport.extractNameValueStyleObj(echartsData, signNameMap.name, signNameMap.value);
		
		chart.eventData(chartEvent, data);
		chartSupport.setChartEventOriginalInfo(chart, chartEvent, echartsData);
	};
	
	//柱状图
	
	chartSupport.barRender = function(chart, nameSign, valueSign, options)
	{
		chartSupport.chartSignNameMap(chart, { name: nameSign, value: valueSign });
		
		var chartDataSet = chart.chartDataSetFirst();
		var np = chart.dataSetPropertyOfSign(chartDataSet, nameSign);
		var vps = chart.dataSetPropertiesOfSign(chartDataSet, valueSign);
		
		options = chartSupport.inflateRenderOptions(chart,
		{
			//扩展配置项：是否堆叠
			dgStack: false,
			//扩展配置项：是否横向
			dgHorizontal: false,
			//是否按数据集分组堆叠
			dgStackGroup: true,
			
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
				//将在update中设置：
				//data
			},
			xAxis:
			{
				name: chart.dataSetPropertyLabel(np),
				nameGap: 5,
				type: chartSupport.evalDataSetPropertyAxisType(chart, np),
				boundaryGap: true
			},
			yAxis:
			{
				name: (vps.length == 1 ? chart.dataSetPropertyLabel(vps[0]) : ""),
				nameGap: 5,
				type: "value"
			},
			series:
			[
				//将在update中设置：
				//{}
				//设初值以免渲染报错
				{
					type: "bar"
				}
			]
		},
		options,
		function(options)
		{
			if(options.dgHorizontal)
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
	
	chartSupport.barUpdate = function(chart, results)
	{
		var signNameMap = chartSupport.chartSignNameMap(chart);
		var renderOptions= chart.renderOptions();
		
		var chartDataSets = chart.chartDataSetsMain();
		
		var legendData = [];
		var series = [];
		
		for(var i=0; i<chartDataSets.length; i++)
		{
			var chartDataSet = chartDataSets[i];
			
			var dataSetName = chart.chartDataSetName(chartDataSet);
			var result = chart.resultOf(results, chartDataSet);
			
			var np = chart.dataSetPropertyOfSign(chartDataSet, signNameMap.name);
			var vps = chart.dataSetPropertiesOfSign(chartDataSet, signNameMap.value);
			
			for(var j=0; j<vps.length; j++)
			{
				var legendName = chartSupport.legendNameForMultipleSeries(chart, chartDataSets, i, dataSetName, vps, j);
				
				//使用{value: [name,value]}格式可以更好地兼容category、value、time坐标轴类型
				var vpsMy = (renderOptions.dgHorizontal ? [vps[j], np] : [np, vps[j]]);
				var data = chart.resultValueObjects(result, vpsMy);
				
				chart.originalInfo(data, chartDataSet);
				
				var mySeries = {type: "bar", name: legendName, data: data};
				
				if(renderOptions.dgStack)
				{
					mySeries.stack = (renderOptions.dgStackGroup ? dataSetName : "stack");
					mySeries.label = { show: true };
				}
				
				legendData.push(legendName);
				series.push(mySeries);
			}
		}
		
		var options = { legend: {data: legendData}, series: series };
		
		//需要明确重置轴坐标值，不然图表刷新有数据变化时，轴坐标不能自动更新
		if(renderOptions.dgHorizontal)
			options.yAxis = { data: null };
		else
			options.xAxis = { data: null };
		
		options = chart.inflateUpdateOptions(results, options, function(options)
		{
			if(renderOptions.dgHorizontal)
				chartSupport.adaptValueArrayObjSeriesData(chart, options, "bar", 1, 0);
			else
				chartSupport.adaptValueArrayObjSeriesData(chart, options, "bar");
		});
		
		chart.echartsOptions(options);
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
		var signNameMap = chartSupport.chartSignNameMap(chart);
		var renderOptions= chart.renderOptions();
		var dgHorizontal = renderOptions.dgHorizontal;
		
		var echartsData = echartsEventParams.data;
		var data = (dgHorizontal ?
				chartSupport.extractNameValueStyleObj(echartsData, signNameMap.name, signNameMap.value, 1, 0) :
				chartSupport.extractNameValueStyleObj(echartsData, signNameMap.name, signNameMap.value)
			);
		
		chart.eventData(chartEvent, data);
		chartSupport.setChartEventOriginalInfo(chart, chartEvent, echartsData);
	};
	
	//极坐标柱状图
	
	chartSupport.barPolarRender = function(chart, nameSign, valueSign, options)
	{
		chartSupport.chartSignNameMap(chart, { name: nameSign, value: valueSign });
		
		options = chartSupport.inflateRenderOptions(chart,
		{
			//扩展配置项：是否堆叠
			dgStack: false,
			//扩展配置项：坐标类型：radius（径向）、angle（角度）
			dgAxisType: "radius",
			//是否按数据集分组堆叠
			dgStackGroup: true,
			
			title:
			{
				text: chart.name
			},
			angleAxis: {},
			radiusAxis: {},
			polar:
			{
				radius: "60%"
			},
			tooltip:
			{
				trigger: "item"
			},
			legend:
			{
				//将在update中设置：
				//data
			},
			series:
			[
				//将在update中设置：
				//{}
				//设初值以免渲染报错
				{
					type: "bar",
					coordinateSystem: "polar"
				}
			]
		},
		options,
		function(options)
		{
			var chartDataSet = chart.chartDataSetFirst();
			var np = chart.dataSetPropertyOfSign(chartDataSet, nameSign);
			
			if(options.dgAxisType == "angle")
			{
				options.angleAxis =
				{
					type: chartSupport.evalDataSetPropertyAxisType(chart, np)
					
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
					name: chart.dataSetPropertyLabel(np),
					nameGap: 20,
					type: chartSupport.evalDataSetPropertyAxisType(chart, np),
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
	
	chartSupport.barPolarUpdate = function(chart, results)
	{
		var signNameMap = chartSupport.chartSignNameMap(chart);
		var renderOptions= chart.renderOptions();
		var isAngleAxis = (renderOptions.dgAxisType == "angle");
		
		var chartDataSets = chart.chartDataSetsMain();
		
		var legendData = [];
		var series = [];
		var axisData = [];
		
		for(var i=0; i<chartDataSets.length; i++)
		{
			var chartDataSet = chartDataSets[i];
			
			var dataSetName = chart.chartDataSetName(chartDataSet);
			var result = chart.resultOf(results, chartDataSet);
			
			var np = chart.dataSetPropertyOfSign(chartDataSet, signNameMap.name);
			var vps = chart.dataSetPropertiesOfSign(chartDataSet, signNameMap.value);
			
			for(var j=0; j<vps.length; j++)
			{
				var legendName = chartSupport.legendNameForMultipleSeries(chart, chartDataSets, i, dataSetName, vps, j);
				var data = null;
				
				//角度图时使用{value: [name,value]}格式的数据会无法显示
				if(isAngleAxis)
					data = chart.resultNameValueObjects(result, np, vps[j]);
				//径向图时使用{value: [name,value]}格式可以更好地兼容category、value、time坐标轴类型
				else
					data = chart.resultValueObjects(result, [np, vps[j]]);
				
				chart.originalInfo(data, chartDataSet);
				
				var mySeries = {type: "bar", name: legendName, data: data, coordinateSystem: "polar"};
				
				if(renderOptions.dgStack)
				{
					mySeries.stack = (renderOptions.dgStackGroup ? dataSetName : "stack");
					mySeries.label = { show: true };
				}
				
				legendData.push(legendName);
				series.push(mySeries);
				
				if(isAngleAxis)
					chartSupport.appendDistinct(axisData, chart.resultRowArrays(result, np));
			}
		}
		
		var options = { legend: {data: legendData}, series: series };
		
		//需要明确重置轴坐标值，不然图表刷新有数据变化时，轴坐标不能自动更新
		//角度图必须设置angleAxis.data，不然不显示刻度
		if(isAngleAxis)
			options.angleAxis = { data: axisData };
		//径向图只需置为null即可
		else
			options.radiusAxis = { data: null };
		
		options = chart.inflateUpdateOptions(results, options);
		
		chart.echartsOptions(options);
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
		var signNameMap = chartSupport.chartSignNameMap(chart);
		var renderOptions= chart.renderOptions();
		
		var echartsData = echartsEventParams.data;
		var data = {};
		
		if(renderOptions.dgAxisType == "angle")
		{
			data[signNameMap.name] = echartsData.name;
			data[signNameMap.value] = echartsData.value;
		}
		else
		{
			data[signNameMap.name] = echartsData.value[0];
			data[signNameMap.value] = echartsData.value[1];
		}
		
		chart.eventData(chartEvent, data);
		chartSupport.setChartEventOriginalInfo(chart, chartEvent, echartsData);
	};
	
	//饼图
	
	chartSupport.pieRender = function(chart, nameSign, valueSign, options)
	{
		chartSupport.chartSignNameMap(chart, { name: nameSign, value: valueSign });
		
		options = chartSupport.inflateRenderOptions(chart,
		{
			//扩展配置项：是否按数据集分割系列，而非仅一个系列
			dgSplitDataSet: false,
			//扩展配置项：当dgSplitDataSet=true时，各系列布局：
			//nest：嵌套；grid：网格
			dgSeriesLayout: "nest",
			//扩展配置项：当dgSplitDataSet=false时，是否环形图
			dgRing: false,
			//扩展配置项：当dgSplitDataSet=false时，是否玫瑰图
			dgRose: false,
			
			title:
			{
		        text: chart.name
		    },
			tooltip:
			{
				trigger: "item",
				formatter: "{a} <br/>{b}: {c} ({d}%)"
			},
			legend:
			{
				//将在update中设置：
				//data
			},
			series:
			[
				//将在update中设置：
				//{}
				//设初值以免渲染报错
				{
					type: "pie"
				}
			]
		},
		options);
		
		chart.echartsInit(options);
	};
	
	chartSupport.pieUpdate = function(chart, results)
	{
		var signNameMap = chartSupport.chartSignNameMap(chart);
		var renderOptions= chart.renderOptions();
		var dgSplitDataSet = renderOptions.dgSplitDataSet;
		
		var chartDataSets = chart.chartDataSetsMain();
		
		var legendData = [];
		var series = [];
		
		for(var i=0; i<chartDataSets.length; i++)
		{
			var chartDataSet = chartDataSets[i];
			
			var dataSetName = chart.chartDataSetName(chartDataSet);
			var result = chart.resultOf(results, chartDataSet);
			
			var np = chart.dataSetPropertyOfSign(chartDataSet, signNameMap.name);
			var npv = chart.resultColumnArrays(result, np);
			var vp = chart.dataSetPropertyOfSign(chartDataSet, signNameMap.value);
			var data = chart.resultNameValueObjects(result, np, vp);
			
			chart.originalInfo(data, chartDataSet);
			
			if(dgSplitDataSet)
			{
				series.push({ type: "pie", name: dataSetName, data: data});
			}
			else
			{
				if(series.length == 0)
				{
					series.push({ type: "pie", name: dataSetName, data: [], radius: "60%" });
					
					if(renderOptions.dgRing)
						series[0].radius = ["35%", "55%"];
					
					if(renderOptions.dgRose)
						series[0].roseType = "radius";
				}
				
				series[0].data = series[0].data.concat(data);
			}
			
			legendData = legendData.concat(npv);
		}
		
		var options = { legend: { data: legendData }, series: series };
		chartSupport.pieEvalSeriesLayout(chart, renderOptions, options);
		
		options = chart.inflateUpdateOptions(results, options);
		
		chart.echartsOptions(options);
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
		var signNameMap = chartSupport.chartSignNameMap(chart);
		
		var echartsData = echartsEventParams.data;
		var data = {};
		
		data[signNameMap.name] = echartsData.name;
		data[signNameMap.value] = echartsData.value;
		
		chart.eventData(chartEvent, data);
		chartSupport.setChartEventOriginalInfo(chart, chartEvent, echartsData);
	};
	
	chartSupport.pieEvalSeriesLayout = function(chart, renderOptions, updateOptions)
	{
		if(!renderOptions.dgSplitDataSet)
			return;
		
		var series = updateOptions.series;
		var len = series.length;
		
		if(!len)
			return;
		
		if(renderOptions.dgSeriesLayout == "nest")
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
		else if(renderOptions.dgSeriesLayout == "grid")
		{
			
		}
	};
	
	//仪表盘
	
	chartSupport.gaugeRender = function(chart, valueSign, minSign, maxSign, options)
	{
		chartSupport.chartSignNameMap(chart, { value: valueSign, min: minSign, max: maxSign });
		
		options = chartSupport.inflateRenderOptions(chart,
		{
			title: {
		        text: chart.name
		    },
			tooltip:
			{
				formatter: "{a} <br/>{b} : {c}"
			},
			series:
			[
				//将在update中设置：
				//{}
				//设初值以免渲染报错
				{
					type: "gauge"
				}
			]
		},
		options);
		
		chart.echartsInit(options);
	};
	
	chartSupport.gaugeUpdate = function(chart, results)
	{
		var signNameMap = chartSupport.chartSignNameMap(chart);
		var renderOptions= chart.renderOptions();
		
		var chartDataSets = chart.chartDataSetsMain();
		
		var seriesName = "";
		var seriesData = [];
		var min = null;
		var max = null;
		
		for(var i=0; i<chartDataSets.length; i++)
		{
			var chartDataSet = chartDataSets[i];
			
			var dataSetName = chart.chartDataSetName(chartDataSet);
			var result = chart.resultOf(results, chartDataSet);
			
			if(min == null)
			{
				var minp = chart.dataSetPropertyOfSign(chartDataSet, signNameMap.min);
				if(minp)
				{
					var minpv = chart.resultColumnArrays(result, minp);
					min = chartSupport.findNonNull(minpv);
				}
			}
			
			if(max == null)
			{
				var maxp = chart.dataSetPropertyOfSign(chartDataSet, signNameMap.max);
				if(maxp)
				{
					var maxpv = chart.resultColumnArrays(result, maxp);
					max = chartSupport.findNonNull(maxpv);
				}
			}
			
			var vps = chart.dataSetPropertiesOfSign(chartDataSet, signNameMap.value);
			var vpsvs = chart.resultRowArrays(result, vps);
			
			for(var j=0; j<vpsvs.length; j++)
			{
				var vRow = vpsvs[j];
				
				for(var k=0; k<vRow.length; k++)
				{
					var vpn = chart.dataSetPropertyLabel(vps[k]);
					var data = { name: vpn, value: vRow[k] };
					chart.originalInfo(data, chartDataSet, j);
					
					seriesData.push(data);
				}
			}
			
			if(!seriesName)
				seriesName = dataSetName;
		}
		
		chartSupport.gaugeEvalDataTitlePosition(chart, seriesData);
		
		min = (min == null ? 0 : min);
		max = (max == null ? 100 : max);
		
		var options = { series : [ { type: "gauge", name: seriesName, min: min, max: max, data: seriesData } ] };
		
		options = chart.inflateUpdateOptions(results, options);
		
		chart.echartsOptions(options);
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
		var signNameMap = chartSupport.chartSignNameMap(chart);
		
		var echartsData = echartsEventParams.data;
		var data = {};
		
		data[signNameMap.value] = echartsData.value;
		data[signNameMap.min] = echartsData.min;
		data[signNameMap.max] = echartsData.max;
		
		chart.eventData(chartEvent, data);
		chartSupport.setChartEventOriginalInfo(chart, chartEvent, echartsData);
	};
	
	chartSupport.gaugeEvalDataTitlePosition = function(chart, seriesData, colCount, initYposition, titleHeight, detailHeight)
	{
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
		if(initYposition == null)
			initYposition = 60;
		if(titleHeight == null)
			titleHeight = 14;
		if(detailHeight == null)
			detailHeight = 15;
		
		var colCenterIdx = colCount/2;
		var xGap = 100/colCount;
		
		for(var i=0; i<seriesData.length; i++)
		{
			var row = parseInt(i/colCount);
			var col = i%colCount;
			
			var x = parseInt((col - colCenterIdx) * xGap + xGap/2);
			var yt = initYposition + row*(titleHeight + detailHeight);
			var yd = yt + titleHeight;
			
			seriesData[i].title = { offsetCenter: [ x+'%', yt+"%" ] };
			seriesData[i].detail = { offsetCenter: [ x+'%', yd+"%" ] };
		}
	};
	
	//散点图
	
	chartSupport.scatterRender = function(chart, nameSign, valueSign, options)
	{
		chartSupport.chartSignNameMap(chart, { name: nameSign, value: valueSign });
		
		var chartDataSet = chart.chartDataSetFirst();
		var np = chart.dataSetPropertyOfSign(chartDataSet, nameSign);
		var vp = chart.dataSetPropertyOfSign(chartDataSet, valueSign);
		
		options = chartSupport.inflateRenderOptions(chart,
		{
			//扩展配置项：最大数据标记像素数
			dgSymbolSizeMax: undefined,
			//扩展配置项：最小数据标记像素数
			dgSymbolSizeMin: undefined,
			
			title: {
		        text: chart.name
		    },
			tooltip:
			{
				trigger: "item"
			},
			legend:
			{
				//将在update中设置：
				//data
			},
			xAxis:
			{
				name: chart.dataSetPropertyLabel(np),
				nameGap: 5,
				type: chartSupport.evalDataSetPropertyAxisType(chart, np),
				boundaryGap: !chartSupport.isDataTypeNumber(np)
			},
			yAxis:
			{
				name: chart.dataSetPropertyLabel(vp),
				nameGap: 5,
				type: "value"
			},
			series:
			[
				//将在update中设置：
				//{}
				//设初值以免渲染报错
				{
					type: "scatter"
				}
			]
		},
		options);
		
		chart.echartsInit(options);
	};
	
	chartSupport.scatterUpdate = function(chart, results)
	{
		var signNameMap = chartSupport.chartSignNameMap(chart);
		var renderOptions= chart.renderOptions();
		
		var chartDataSets = chart.chartDataSetsMain();
		
		var legendData = [];
		var series = [];
		
		var min = undefined, max = undefined;
		var symbolSizeMax = chartSupport.evalSymbolSizeMax(chart, renderOptions);
		var symbolSizeMin = chartSupport.evalSymbolSizeMin(chart, renderOptions, symbolSizeMax);
		
		for(var i=0; i<chartDataSets.length; i++)
		{
			var chartDataSet = chartDataSets[i];
			
			var dataSetName = chart.chartDataSetName(chartDataSet);
			var result = chart.resultOf(results, chartDataSet);
			
			var np = chart.dataSetPropertyOfSign(chartDataSet, signNameMap.name);
			var vps = chart.dataSetPropertiesOfSign(chartDataSet, signNameMap.value);
			
			for(var j=0; j<vps.length; j++)
			{
				var legendName = chartSupport.legendNameForMultipleSeries(chart, chartDataSets, i, dataSetName, vps, j);
				//使用{value: [name,value]}格式可以更好地兼容category、value、time坐标轴类型
				var data = chart.resultValueObjects(result, [np, vps[j]]);
				
				chart.originalInfo(data, chartDataSet);
				
				for(var k=0; k<data.length; k++)
				{
					var valMy = data[k].value[1];
					min = (min == null ? valMy : Math.min(min, valMy));
					max = (max == null ? valMy : Math.max(max, valMy));
				}
				
				var mySeries = { type: "scatter", name: legendName, data: data };
				
				legendData.push(legendName);
				series.push(mySeries);
			}
		}
		
		chartSupport.evalSeriesDataValueSymbolSize(series, min, max, symbolSizeMax, symbolSizeMin, "value", 1);
		
		var options = { legend: {data: legendData}, series: series };
		//需要明确重置轴坐标值，不然图表刷新有数据变化时，轴坐标不能自动更新
		options.xAxis = {data: null};
		
		options = chart.inflateUpdateOptions(results, options, function(options)
		{
			chartSupport.adaptValueArrayObjSeriesData(chart, options, "scatter");
		});
		
		chart.echartsOptions(options);
	};

	chartSupport.scatterResize = function(chart)
	{
		chartSupport.resizeChartEcharts(chart);
	};
	
	chartSupport.scatterDestroy = function(chart)
	{
		chartSupport.destroyChartEcharts(chart);
	};

	chartSupport.scatterOn = function(chart, eventType, handler)
	{
		chartSupport.bindChartEventHandlerForEcharts(chart, eventType, handler,
				chartSupport.scatterSetChartEventData);
	};
	
	chartSupport.scatterOff = function(chart, eventType, handler)
	{
		chart.echartsOffEventHandler(eventType, handler);
	};
	
	chartSupport.scatterSetChartEventData = function(chart, chartEvent, echartsEventParams)
	{
		var signNameMap = chartSupport.chartSignNameMap(chart);
		
		var echartsData = echartsEventParams.data;
		var data = chartSupport.extractNameValueStyleObj(echartsData, signNameMap.name, signNameMap.value);
		
		chart.eventData(chartEvent, data);
		chartSupport.setChartEventOriginalInfo(chart, chartEvent, echartsData);
	};
	
	//坐标散点图
	
	chartSupport.scatterCoordRender = function(chart, nameSign, valueSign, weightSign, options)
	{
		chartSupport.chartSignNameMap(chart, { name: nameSign, value: valueSign, weight: weightSign });
		
		var chartDataSet = chart.chartDataSetFirst();
		var np = chart.dataSetPropertyOfSign(chartDataSet, nameSign);
		var vp = chart.dataSetPropertyOfSign(chartDataSet, valueSign);
		
		options = chartSupport.inflateRenderOptions(chart,
		{
			//扩展配置项：最大数据标记像素数
			dgSymbolSizeMax: undefined,
			//扩展配置项：最小数据标记像素数
			dgSymbolSizeMin: undefined,
			
			title: {
		        text: chart.name
		    },
			tooltip:
			{
				trigger: "item"
			},
			legend:
			{
				//将在update中设置：
				//data
			},
			xAxis:
			{
				name: chart.dataSetPropertyLabel(np),
				nameGap: 5,
				type: chartSupport.evalDataSetPropertyAxisType(chart, np),
				boundaryGap: !chartSupport.isDataTypeNumber(np)
			},
			yAxis:
			{
				name: chart.dataSetPropertyLabel(vp),
				nameGap: 5,
				type: "value"
			},
			series:
			[
				//将在update中设置：
				//{}
				//设初值以免渲染报错
				{
					type: "scatter"
				}
			]
		},
		options);
		
		chart.echartsInit(options);
	};
	
	chartSupport.scatterCoordUpdate = function(chart, results)
	{
		var signNameMap = chartSupport.chartSignNameMap(chart);
		var renderOptions= chart.renderOptions();
		
		var chartDataSets = chart.chartDataSetsMain();
		
		var legendData = [];
		var series = [];
		
		var min = undefined, max = undefined;
		var symbolSizeMax = chartSupport.evalSymbolSizeMax(chart, renderOptions);
		var symbolSizeMin = chartSupport.evalSymbolSizeMin(chart, renderOptions, symbolSizeMax);
		
		for(var i=0; i<chartDataSets.length; i++)
		{
			var chartDataSet = chartDataSets[i];
			
			var dataSetName = chart.chartDataSetName(chartDataSet);
			var result = chart.resultOf(results, chartDataSet);
			
			var np = chart.dataSetPropertyOfSign(chartDataSet, signNameMap.name);
			var vp = chart.dataSetPropertyOfSign(chartDataSet, signNameMap.value);
			var wp = chart.dataSetPropertyOfSign(chartDataSet, signNameMap.weight);
			
			var data = (wp ? chart.resultValueObjects(result, [np, vp, wp]) : chart.resultValueObjects(result, [np, vp]));
			
			if(wp)
			{
				for(var j=0; j<data.length; j++)
				{
					var wv = data[j].value[2];
					min = (min == undefined ? wv : Math.min(min, wv));
					max = (max == undefined ? wv : Math.max(max, wv));
				}
			}
			
			chart.originalInfo(data, chartDataSet);
			
			series.push({ type: "scatter", name: dataSetName, data: data });
			legendData.push(dataSetName);
		}
		
		chartSupport.evalSeriesDataValueSymbolSize(series, min, max, symbolSizeMax, symbolSizeMin, "value", 2);
		
		var options = { legend: {data: legendData}, series: series };
		
		options = chart.inflateUpdateOptions(results, options);
		
		chart.echartsOptions(options);
	};
	
	chartSupport.scatterCoordResize = function(chart)
	{
		chartSupport.resizeChartEcharts(chart);
	};
	
	chartSupport.scatterCoordDestroy = function(chart)
	{
		chartSupport.destroyChartEcharts(chart);
	};
	
	chartSupport.scatterCoordOn = function(chart, eventType, handler)
	{
		chartSupport.bindChartEventHandlerForEcharts(chart, eventType, handler,
				chartSupport.scatterCoordSetChartEventData);
	};
	
	chartSupport.scatterCoordOff = function(chart, eventType, handler)
	{
		chart.echartsOffEventHandler(eventType, handler);
	};
	
	chartSupport.scatterCoordSetChartEventData = function(chart, chartEvent, echartsEventParams)
	{
		var signNameMap = chartSupport.chartSignNameMap(chart);
		
		var echartsData = echartsEventParams.data;
		var data = {};
		
		data[signNameMap.name] = echartsData.value[0];
		data[signNameMap.value] = echartsData.value[1];
		if(echartsData.value.length > 2)
			data[signNameMap.weight] = echartsData.value[2];
		
		chart.eventData(chartEvent, data);
		chartSupport.setChartEventOriginalInfo(chart, chartEvent, echartsData);
	};
	
	//雷达图
	
	chartSupport.radarRender = function(chart, itemSign, nameSign, valueSign, maxSign, options)
	{
		chartSupport.chartSignNameMap(chart, { item: itemSign, name: nameSign, value: valueSign, max: maxSign });
		
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
				//将在update中设置：
				//data
			},
			radar:
			{
				//将在update中设置：
				//indicator
				
				center: ["50%", "60%"],
				radius: "70%",
				nameGap: 6
			},
			series:
			[
				//将在update中设置：
				//{}
				//设初值以免渲染报错
				{
					type: "radar"
				}
			]
		},
		options);
		
		chart.echartsInit(options);
	};
	
	chartSupport.radarUpdate = function(chart, results)
	{
		var signNameMap = chartSupport.chartSignNameMap(chart);
		var renderOptions= chart.renderOptions();
		
		var chartDataSets = chart.chartDataSetsMain();
		
		var legendData = [];
		var indicatorData = [];
		var seriesData = [];
		
		for(var i=0; i<chartDataSets.length; i++)
		{
			var chartDataSet = chartDataSets[i];
			var result = chart.resultOf(results, chartDataSet);
			
			var ip = chart.dataSetPropertyOfSign(chartDataSet, signNameMap.item);
			
			//行式雷达网数据，必设置【雷达网条目名称】标记
			if(ip)
			{
				chartSupport.radarUpdateForRowData(chart, results, signNameMap, renderOptions,
						chartDataSet, i, result, legendData, indicatorData, seriesData)
			}
			//列式雷达网数据
			else
			{
				chartSupport.radarUpdateForColumnData(chart, results, signNameMap, renderOptions,
						chartDataSet, i, result, legendData, indicatorData, seriesData)
			}
		}
		
		var series = [ { type: "radar", data: seriesData } ];
		var options = { legend: {data: legendData}, radar: {indicator: indicatorData}, series: series };
		
		options = chart.inflateUpdateOptions(results, options);
		
		chart.echartsOptions(options);
		
		chartFactory.extValueBuiltin(chart, "radarIndicatorData", indicatorData);
	};
	
	//行式雷达网数据处理，一行数据表示一条雷达网，行式结构为：雷达网条目名称, [指标名, 指标值, 指标上限值]*n
	chartSupport.radarUpdateForRowData = function(chart, results, signNameMap, renderOptions,
			chartDataSet, chartDataSetIdx, result, legendData, indicatorData, seriesData)
	{
		var ip = chart.dataSetPropertyOfSign(chartDataSet, signNameMap.item);
		var iv = chart.resultColumnArrays(result, ip);
		chartSupport.appendElement(legendData, iv);
		
		//仅使用第一个数据集构建指示器
		if(chartDataSetIdx == 0)
		{
			var np = chart.dataSetPropertiesOfSign(chartDataSet, signNameMap.name);
			var npv = chart.resultRowArrays(result, np, 0, 1);
			npv = (npv.length > 0 ? npv[0] : []);
			
			var mp = chart.dataSetPropertiesOfSign(chartDataSet, signNameMap.max);
			var mpv = chart.resultRowArrays(result, mp, 0, 1);
			mpv = (mpv.length > 0 ? mpv[0] : []);
			
			var indicatorLen = Math.min(np.length, mp.length);
			
			for(var j=0; j<indicatorLen; j++)
			{
				var indicator = {name: npv[j], max: mpv[j]};
				indicatorData.push(indicator);
			}
		}
		
		var vp = chart.dataSetPropertiesOfSign(chartDataSet, signNameMap.value);
		var vpv = chart.resultRowArrays(result, vp);
		
		for(var j=0; j<iv.length; j++)
		{
			var myData = { name: iv[j], value: vpv[j] };
			
			chart.originalInfo(myData, chartDataSet, j);
			
			seriesData.push(myData);
		}
	};
	
	//列式雷达网数据处理，一列【指标值】数据表示一条雷达网，列式结构为：指标名, 指标上限值, [指标值]*n，其中【指标值】列名将作为雷达网条目名称
	chartSupport.radarUpdateForColumnData = function(chart, results, signNameMap, renderOptions,
			chartDataSet, chartDataSetIdx, result, legendData, indicatorData, seriesData)
	{
		//仅使用第一个数据集构建指示器
		if(chartDataSetIdx == 0)
		{
			var np = chart.dataSetPropertyOfSign(chartDataSet, signNameMap.name);
			var nv = chart.resultColumnArrays(result, np);
			var mp = chart.dataSetPropertyOfSign(chartDataSet, signNameMap.max);
			var mv = chart.resultColumnArrays(result, mp);
			
			var indicatorLen = Math.min(nv.length, mv.length);
			
			for(var i=0; i<indicatorLen; i++)
			{
				var indicator = {name: nv[i], max: mv[i]};
				indicatorData.push(indicator);
			}
		}
		
		var vp = chart.dataSetPropertiesOfSign(chartDataSet, signNameMap.value);
		var vv = chart.resultColumnArrays(result, vp);
		
		var resultDataIndex = [];
		for(var i=0; i<indicatorData.length; i++)
			resultDataIndex[i] = i;
		
		for(var i=0; i<vv.length; i++)
		{
			var name = chart.dataSetPropertyLabel(vp[i]);
			legendData.push(name);
			
			var myData = { name: name, value: vv[i] };
			
			chart.originalInfo(myData, chartDataSet, resultDataIndex);
			
			seriesData.push(myData);
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
		var signNameMap = chartSupport.chartSignNameMap(chart);
		
		var echartsData = echartsEventParams.data;
		var data = {};
		
		data[signNameMap.item] = echartsData.name;
		data[signNameMap.value] = echartsData.value;
		
		var indicatorData = chartFactory.extValueBuiltin(chart, "radarIndicatorData");
		var names = [];
		var maxes = [];
		for(var i=0; i<indicatorData.length; i++)
		{
			names[i] = indicatorData[i].name;
			maxes[i] = indicatorData[i].max;
		}
		
		data[signNameMap.name] = names;
		data[signNameMap.max] = maxes;
		
		chart.eventData(chartEvent, data);
		chartSupport.setChartEventOriginalInfo(chart, chartEvent, echartsData);
	};
	
	//漏斗图
	
	chartSupport.funnelRender = function(chart, nameSign, valueSign, options)
	{
		chartSupport.chartSignNameMap(chart, { name: nameSign, value: valueSign });
		
		options = chartSupport.inflateRenderOptions(chart,
		{
			//扩展配置项：同series[i].sort
			dgSort: "descending",
			
			title:
			{
		        text: chart.name
		    },
		    tooltip:
		    {
		        trigger: "item",
		        formatter: "{a} <br/>{b} : {c}"
		    },
			legend:
			{
				//将在update中设置：
				//data
			},
			series:
			[
				//将在update中设置：
				//{}
				//设初值以免渲染报错
				{
		            type: "funnel"
		        }
			]
		},
		options);
		
		chart.echartsInit(options);
	};
	
	chartSupport.funnelUpdate = function(chart, results)
	{
		var signNameMap = chartSupport.chartSignNameMap(chart);
		var renderOptions= chart.renderOptions();
		
		var chartDataSets = chart.chartDataSetsMain();
		
		var legendData = [];
		var seriesName = "";
		var seriesData = [];
		var min = 0;
		var max = 100;
		
		for(var i=0; i<chartDataSets.length; i++)
		{
			var chartDataSet = chartDataSets[i];
			
			var dataSetName = chart.chartDataSetName(chartDataSet);
			var result = chart.resultOf(results, chartDataSet);
			
			var np = chart.dataSetPropertyOfSign(chartDataSet, signNameMap.name);
			var npv = chart.resultColumnArrays(result, np);
			var vp = chart.dataSetPropertyOfSign(chartDataSet, signNameMap.value);
			var data = chart.resultNameValueObjects(result, np, vp);
			
			chart.originalInfo(data, chartDataSet);
			
			legendData = legendData.concat(npv);
			if(!seriesName)
				seriesName = dataSetName;
			seriesData = seriesData.concat(data);
		}
		
		for(var i=0; i<seriesData.length; i++)
		{
			var nvv = seriesData[i];
			var v = (nvv ? (nvv.value || 0) : 0);
			
			if(v < min)
				min = v;
			else if(v > max)
				max = v;
		}
		
		var series = [ {type: "funnel", name: seriesName, min: min, max: max, data: seriesData, sort: renderOptions.dgSort } ];
		var options = { legend: { data: legendData }, series: series };
		
		options = chart.inflateUpdateOptions(results, options);
		
		chart.echartsOptions(options);
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
		var signNameMap = chartSupport.chartSignNameMap(chart);
		
		var echartsData = echartsEventParams.data;
		var data = {};
		
		data[signNameMap.name] = echartsData.name;
		data[signNameMap.value] = echartsData.value;
		
		chart.eventData(chartEvent, data);
		chartSupport.setChartEventOriginalInfo(chart, chartEvent, echartsData);
	};
	
	//地图
	
	chartSupport.mapRender = function(chart, nameSign, valueSign, mapSign, options)
	{
		chartSupport.chartSignNameMap(chart, { name: nameSign, value: valueSign, map: mapSign });
		
		options = chartSupport.inflateRenderOptions(chart,
		{
			title:
			{
		        text: chart.name
		    },
			tooltip:
			{
				trigger: "item",
				formatter: "{b}<br/>{c}"
			},
			visualMap:
			{
				//将在update中设置：
				//min: 0,
				//max: 100,
				
				text: ["高", "低"],
				realtime: true,
				calculable: true
			},
			series:
			[
				//将在update中设置：
				//{}
				//设初值以免渲染报错
				{
		            type: "map",
					//这里必须设置map，不然渲染会报错，update中会特殊处理
					map: (chart.map() || "china")
		        }
			]
		},
		options);
		
		chartSupport.echartsMapChartInit(chart, options);
	};
	
	chartSupport.mapUpdate = function(chart, results)
	{
		var signNameMap = chartSupport.chartSignNameMap(chart);
		var renderOptions= chart.renderOptions();
		
		var chartDataSets = chart.chartDataSetsMain();
		
		var min = undefined;
		var max = undefined;
		var seriesName = "";
		var seriesData = [];
		var map = undefined;
		
		for(var i=0; i<chartDataSets.length; i++)
		{
			var chartDataSet = chartDataSets[i];
			
			var dataSetName = chart.chartDataSetName(chartDataSet);
			var result = chart.resultOf(results, chartDataSet);
			
			//取任一不为空的地图名列值
			if(!map)
			{
				var mp = chart.dataSetPropertyOfSign(chartDataSet, signNameMap.map);
				if(mp)
				{
					var maps = chart.resultColumnArrays(result, mp);
					map = chartSupport.findNonNull(maps);
				}
			}
			
			var np = chart.dataSetPropertyOfSign(chartDataSet, signNameMap.name);
			var vp = chart.dataSetPropertyOfSign(chartDataSet, signNameMap.value);
			var data = chart.resultNameValueObjects(result, np, vp);
			
			chart.originalInfo(data, chartDataSet);
			
			if(!seriesName)
				seriesName = dataSetName;
			
			seriesData = seriesData.concat(data);
			
			if(data && data.length)
			{
				for(var j=0; j<data.length; j++)
				{
					var val = data[j].value;
					min = (min == undefined ? val : Math.min(min, val));
					max = (max == undefined ? val : Math.max(max, val));
				}
			}
		}
		
		var options =
		{
			visualMap: {min, min, max: max},
			series: [ {type: "map", name: seriesName, data: seriesData } ]
		};
		
		chartSupport.checkMinAndMax(options.visualMap);
		
		if(map)
			options.series[0].map = map;
		
		chartSupport.echartsMapChartUpdate(chart, results, options, renderOptions);
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
		var signNameMap = chartSupport.chartSignNameMap(chart);
		
		var echartsData = echartsEventParams.data;
		
		//当指定地区没有设置数据时，echartsData为null
		if(!echartsData)
			echartsData = { name: echartsEventParams.name, value: null } ;
		
		var data = {};
		
		data[signNameMap.name] = echartsData.name;
		data[signNameMap.value] = echartsData.value;
		
		chart.eventData(chartEvent, data);
		chartSupport.setChartEventOriginalInfo(chart, chartEvent, echartsData);
	};
	
	//散点值地图
	
	chartSupport.mapScatterRender = function(chart, nameSign, longitudeSign, latitudeSign, valueSign, mapSign, options)
	{
		chartSupport.chartSignNameMap(chart, { name: nameSign, longitude: longitudeSign, latitude: latitudeSign,
			value: valueSign, map: mapSign });
		
		options = chartSupport.inflateRenderOptions(chart,
		{
			//扩展配置项：最大数据标记像素数
			dgSymbolSizeMax: undefined,
			//扩展配置项：最小数据标记像素数
			dgSymbolSizeMin: undefined,
			
			title:
			{
		        text: chart.name
		    },
			tooltip:
			{
				trigger: "item",
				formatter: function (params)
				{
					return params.name + "<br/>" + params.value[2];
				}
			},
			geo:
			{
				//将在update中设置：
				//map
				//这里必须设置map，不然渲染会报错，update中会特殊处理
				map: (chart.map() || "china"),
				
				roam: true
			},
			series:
			[
				//将在update中设置：
				//{}
				//设初值以免渲染报错
				{
					type: "scatter",
					coordinateSystem: "geo"
				}
			]
		},
		options);
		
		chartSupport.echartsMapChartInit(chart, options);
	};
	
	chartSupport.mapScatterUpdate = function(chart, results)
	{
		var signNameMap = chartSupport.chartSignNameMap(chart);
		var renderOptions= chart.renderOptions();
		
		var chartDataSets = chart.chartDataSetsMain();
		
		var legendData = [];
		var series = [];
		var map = undefined;
		
		var min = undefined, max = undefined;
		var symbolSizeMax = chartSupport.evalSymbolSizeMax(chart, renderOptions);
		var symbolSizeMin = chartSupport.evalSymbolSizeMin(chart, renderOptions, symbolSizeMax);
		
		for(var i=0; i<chartDataSets.length; i++)
		{
			var chartDataSet = chartDataSets[i];
			
			var dataSetName = chart.chartDataSetName(chartDataSet);
			var result = chart.resultOf(results, chartDataSet);
			
			//取任一不为空的地图名列值
			if(!map)
			{
				var mp = chart.dataSetPropertyOfSign(chartDataSet, signNameMap.map);
				if(mp)
				{
					var maps = chart.resultColumnArrays(result, mp);
					map = chartSupport.findNonNull(maps);
				}
			}
			
			var lop = chart.dataSetPropertyOfSign(chartDataSet, signNameMap.longitude);
			var lap = chart.dataSetPropertyOfSign(chartDataSet, signNameMap.latitude);
			var vp = chart.dataSetPropertyOfSign(chartDataSet, signNameMap.value);
			
			var data = chart.resultNameValueObjects(result, chart.dataSetPropertyOfSign(chartDataSet, signNameMap.name),
					(vp ? [lop, lap, vp] : [lop, lap]));
			
			chart.originalInfo(data, chartDataSet);
			
			if(vp)
			{
				for(var j=0; j<data.length; j++)
				{
					var dv = data[j].value;
					
					min = (min == undefined ? dv[2] : Math.min(min, dv[2]));
					max = (max == undefined ? dv[2] : Math.max(max, dv[2]));
				}
			}
			
			legendData.push(dataSetName);
			series.push({ type: "scatter", name: dataSetName, data: data, coordinateSystem: "geo" });
		}
		
		chartSupport.evalSeriesDataValueSymbolSize(series, min, max, symbolSizeMax, symbolSizeMin, "value", 2);
		
		var options = { legend: {data: legendData}, series: series };
		
		if(map)
			options.geo = { map: map };
		
		chartSupport.echartsMapChartUpdate(chart, results, options, renderOptions);
	};
	
	chartSupport.mapScatterResize = function(chart)
	{
		chartSupport.resizeChartEcharts(chart);
	};
	
	chartSupport.mapScatterDestroy = function(chart)
	{
		chartSupport.destroyChartEcharts(chart);
	};

	chartSupport.mapScatterOn = function(chart, eventType, handler)
	{
		chartSupport.bindChartEventHandlerForEcharts(chart, eventType, handler,
				chartSupport.mapScatterSetChartEventData);
	};
	
	chartSupport.mapScatterOff = function(chart, eventType, handler)
	{
		chart.echartsOffEventHandler(eventType, handler);
	};
	
	chartSupport.mapScatterSetChartEventData = function(chart, chartEvent, echartsEventParams)
	{
		var signNameMap = chartSupport.chartSignNameMap(chart);
		
		var echartsData = echartsEventParams.data;
		
		var data = {};
		
		data[signNameMap.name] = echartsData.name;
		data[signNameMap.longitude] = echartsData.value[0];
		data[signNameMap.latitude] = echartsData.value[1];
		if(echartsData.value.length > 2)
			data[signNameMap.value] = echartsData.value[2];
		
		chart.eventData(chartEvent, data);
		chartSupport.setChartEventOriginalInfo(chart, chartEvent, echartsData);
	};
	
	//关系地图
	
	chartSupport.mapGraphRender = function(chart, sourceIdSign, sourceLongitudeSign, sourceLatitudeSign, sourceNameSign, sourceCategorySign, sourceValueSign,
			targetIdSign, targetLongitudeSign, targetLatitudeSign, targetNameSign, targetCategorySign, targetValueSign, mapSign, options)
	{
		chartSupport.chartSignNameMap(chart, { sourceId: sourceIdSign, sourceLongitude: sourceLongitudeSign,
			sourceLatitude: sourceLatitudeSign, sourceName: sourceNameSign, sourceCategory: sourceCategorySign,
			sourceValue: sourceValueSign,
			targetId: targetIdSign, targetLongitude: targetLongitudeSign,
			targetLatitude: targetLatitudeSign, targetName: targetNameSign, targetCategory: targetCategorySign,
			targetValue: targetValueSign, map: mapSign });
		
		options = chartSupport.inflateRenderOptions(chart,
		{
			//扩展配置项：最大数据标记像素数
			dgSymbolSizeMax: undefined,
			//扩展配置项：最小数据标记像素数
			dgSymbolSizeMin: undefined,
			
			title:
			{
		        text: chart.name
		    },
			tooltip:
			{
				trigger: "item"
			},
			geo:
			{
				//将在update中设置：
				//map
				//这里必须设置map，不然渲染会报错，update中会特殊处理
				map: (chart.map() || "china"),
				
				roam: true
			},
			series:
			[
				{
					//将在update中设置：
					//name
					//data
					//links
					
					type: "graph",
			        coordinateSystem: "geo",
			        layout: "none",
					tooltip:
					{
						formatter: "{a}<br>{b}：{c}"
					}
				}
			]
		},
		options);
		
		chartSupport.echartsMapChartInit(chart, options);
	};
	
	chartSupport.mapGraphUpdate = function(chart, results)
	{
		var signNameMap = chartSupport.chartSignNameMap(chart);
		var renderOptions= chart.renderOptions();
		
		var chartDataSets = chart.chartDataSetsMain();
		
		var legendData = [];
		var seriesName = "";
		var categories = [];
		var seriesData = [];
		var seriesLinks = [];
		var map = undefined;
		
		var min = undefined, max = undefined;
		var symbolSizeMax = chartSupport.evalSymbolSizeMax(chart, renderOptions);
		var symbolSizeMin = chartSupport.evalSymbolSizeMin(chart, renderOptions, symbolSizeMax);
		
		for(var i=0; i<chartDataSets.length; i++)
		{
			var chartDataSet = chartDataSets[i];
			
			var result = chart.resultOf(results, chartDataSet);
			
			//取任一不为空的地图名列值
			if(!map)
			{
				var mp = chart.dataSetPropertyOfSign(chartDataSet, signNameMap.map);
				if(mp)
				{
					var maps = chart.resultColumnArrays(result, mp);
					map = chartSupport.findNonNull(maps);
				}
			}
			
			if(!seriesName)
				seriesName = chart.chartDataSetName(chartDataSet);
			
			var sip = chart.dataSetPropertyOfSign(chartDataSet, signNameMap.sourceId);
			var slop = chart.dataSetPropertyOfSign(chartDataSet, signNameMap.sourceLongitude);
			var slap = chart.dataSetPropertyOfSign(chartDataSet, signNameMap.sourceLatitude);
			var snp = chart.dataSetPropertyOfSign(chartDataSet, signNameMap.sourceName);
			var scp = chart.dataSetPropertyOfSign(chartDataSet, signNameMap.sourceCategory);
			var svp = chart.dataSetPropertyOfSign(chartDataSet, signNameMap.sourceValue);
			var tip = chart.dataSetPropertyOfSign(chartDataSet, signNameMap.targetId);
			var tlop = chart.dataSetPropertyOfSign(chartDataSet, signNameMap.targetLongitude);
			var tlap = chart.dataSetPropertyOfSign(chartDataSet, signNameMap.targetLatitude);
			var tnp = chart.dataSetPropertyOfSign(chartDataSet, signNameMap.targetName);
			var tcp = chart.dataSetPropertyOfSign(chartDataSet, signNameMap.targetCategory);
			var tvp = chart.dataSetPropertyOfSign(chartDataSet, signNameMap.targetValue);
			
			var data = chart.resultDatas(result);
			
			for(var j=0; j<data.length; j++)
			{
				//在ECharts-4.9.0时，这里格式为：{name: ..., value:[经度值, 纬度值, 关系数值]}，是没问题的，
				//但是升级至ECharts-5.0+后，会报错：Can not read property 'off' of undefined
				//按照ECharts-5.0+的graph的官方配置项，这里格式应为：{name: ..., x: 经度值, y: 纬度值, value: 关系数值}
				//但是这样仍然会报上述错误！
				//所以这里仍恢复为采用ECharts-4.9.0时的格式，配合修改了ECharts-5.1.2的源码后，终于解决了上述问题！
				//具体源码修改位置参考echarts-5.1.2/echarts.js的58833行
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
					
					min = (min == undefined ? sv : Math.min(min, sv));
					max = (max == undefined ? sv : Math.max(max, sv));
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
					
					min = (min == undefined ? tv : Math.min(min, tv));
					max = (max == undefined ? tv : Math.max(max, tv));
				}
				
				var sidx = chartSupport.appendDistinct(seriesData, sd, (sip ? "id" : "name"));
				
				//新插入
				if(sidx == seriesData.length - 1 && seriesData[seriesData.length - 1] === sd)
				{
					chart.originalInfo(sd, chartDataSet, j);
				}
				
				var tidx = chartSupport.appendDistinct(seriesData, td, (tip ? "id" : "name"));
				
				//新插入
				if(tidx == seriesData.length - 1 && seriesData[seriesData.length - 1] === td)
				{
					chart.originalInfo(td, chartDataSet, j);
				}
				
				//如果使用id值表示关系，对于数值型id，echarts会误当做数据索引，所以这里直接使用数据索引
				var link = {};
				link.source = sidx;
				link.target = tidx;
				
				chart.originalInfo(link, chartDataSet, j);
				
				seriesLinks.push(link);
			}
		}
		
		var series = [ { type: "graph", name: seriesName, categories: categories, data: seriesData, links: seriesLinks, 
			        		coordinateSystem: "geo" } ];
		
		chartSupport.evalSeriesDataValueSymbolSize(series, min, max, symbolSizeMax, symbolSizeMin, "value", 2);
		
		var options = { legend: {data: legendData}, series: series };
		
		if(map)
			options.geo = { map: map };
		
		chartSupport.echartsMapChartUpdate(chart, results, options, renderOptions);
		
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
		var signNameMap = chartSupport.chartSignNameMap(chart);
		
		var echartsData = echartsEventParams.data;
		
		var data = {};
		
		//节点，仅使用源数据标记对象
		if(echartsEventParams.dataType == "node")
		{
			data[signNameMap.sourceId] = echartsData.id;
			data[signNameMap.sourceLongitude] = echartsData.value[0];
			data[signNameMap.sourceLatitude] = echartsData.value[1];
			data[signNameMap.sourceName] = echartsData.name;
			data[signNameMap.sourceCategory] = echartsData._categoryOrigin;
			if(echartsData.value.length > 2)
				data[signNameMap.sourceValue] = echartsData.value[2];
			
			chart.eventData(chartEvent, data);
			chartSupport.setChartEventOriginalInfo(chart, chartEvent, echartsData);
		}
		//边
		else if(echartsEventParams.dataType == "edge")
		{
			var seriesData = chartFactory.extValueBuiltin(chart, "mapGraphSeriesData");
			var sourceData = seriesData[echartsData.source];
			var targetData = seriesData[echartsData.target];
			
			data[signNameMap.sourceId] = sourceData.id;
			data[signNameMap.sourceLongitude] = sourceData.value[0];
			data[signNameMap.sourceLatitude] = sourceData.value[1];
			data[signNameMap.sourceName] = sourceData.name;
			data[signNameMap.sourceCategory] = sourceData._categoryOrigin;
			if(sourceData.value.length > 2)
				data[signNameMap.sourceValue] = sourceData.value[2];
			
			if(targetData)
			{
				data[signNameMap.targetId] = targetData.id;
				data[signNameMap.targetLongitude] = targetData.value[0];
				data[signNameMap.targetLatitude] = targetData.value[1];
				data[signNameMap.targetName] = targetData.name;
				data[signNameMap.targetCategory] = targetData._categoryOrigin;
				if(targetData.value.length > 2)
					data[signNameMap.targetValue] = targetData.value[2];
			}
			
			chart.eventData(chartEvent, data);
			chartSupport.setChartEventOriginalInfo(chart, chartEvent, echartsData);
		}
	};
	
	
	//K线图
	
	chartSupport.candlestickRender = function(chart, nameSign, openSign, closeSign, minSign, maxSign, options)
	{
		chartSupport.chartSignNameMap(chart, { name: nameSign, open: openSign, close: closeSign, min: minSign, max: maxSign });
		
		var chartDataSet = chart.chartDataSetFirst();
		var np = chart.dataSetPropertyOfSign(chartDataSet, nameSign);
		
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
				//将在update中设置：
				//data
			},
			xAxis:
			{
				//将在update中设置：
				//data
				
				name: chart.dataSetPropertyLabel(np),
				nameGap: 5,
				type: chartSupport.evalDataSetPropertyAxisType(chart, np),
				boundaryGap: true,
				splitLine: {show:false}
			},
			yAxis:
			{
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
	
	chartSupport.candlestickUpdate = function(chart, results)
	{
		var signNameMap = chartSupport.chartSignNameMap(chart);
		var renderOptions= chart.renderOptions();
		
		var chartDataSets = chart.chartDataSetsMain();
		
		var legendData = [];
		var series = [];
		var axisData = [];
		
		for(var i=0; i<chartDataSets.length; i++)
		{
			var chartDataSet = chartDataSets[i];
			
			var dataSetName = chart.chartDataSetName(chartDataSet);
			var result = chart.resultOf(results, chartDataSet);
			
			var np = chart.dataSetPropertyOfSign(chartDataSet, signNameMap.name);
			
			var data = chart.resultNameValueObjects(result, np,
					[
						chart.dataSetPropertyOfSign(chartDataSet, signNameMap.open),
						chart.dataSetPropertyOfSign(chartDataSet, signNameMap.close),
						chart.dataSetPropertyOfSign(chartDataSet, signNameMap.min),
						chart.dataSetPropertyOfSign(chartDataSet, signNameMap.max)
					]);
			
			chart.originalInfo(data, chartDataSet);
			
			chartSupport.appendDistinct(axisData, chart.resultRowArrays(result, np));
			
			series.push({type: "k", name: dataSetName, data: data});
		}
		
		var options = { legend: {data: legendData}, series: series };
		//不设置坐标轴数据的话将无法显示刻度标签
		options.xAxis = { data: axisData };
		
		options = chart.inflateUpdateOptions(results, options);
		
		chart.echartsOptions(options);
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
		var signNameMap = chartSupport.chartSignNameMap(chart);
		
		var echartsData = echartsEventParams.data;
		var data = {};
		
		//echartsData不是设置的初始系列数据，第0个元素是数据索引，echarts的BUG？？？
		var idx = (echartsData.value.length > 4 ? 1 : 0);
		
		data[signNameMap.name] = echartsData.name;
		data[signNameMap.open] = echartsData.value[idx];
		data[signNameMap.close] = echartsData.value[idx+1];
		data[signNameMap.min] = echartsData.value[idx+2];
		data[signNameMap.max] = echartsData.value[idx+3];
		
		chart.eventData(chartEvent, data);
		chartSupport.setChartEventOriginalInfo(chart, chartEvent, echartsData);
	};
	
	//热力图
	
	chartSupport.heatmapRender = function(chart, nameSign, valueSign, weightSign, options)
	{
		chartSupport.chartSignNameMap(chart, { name: nameSign, value: valueSign, weight: weightSign });
		
		var chartDataSet = chart.chartDataSetFirst();
		var np = chart.dataSetPropertyOfSign(chartDataSet, nameSign);
		var vp = chart.dataSetPropertyOfSign(chartDataSet, valueSign);
		
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
			},
			xAxis:
			{
				//将在update中设置：
				//data
				
				name: chart.dataSetPropertyLabel(np),
				nameGap: 5,
				type: chartSupport.evalDataSetPropertyAxisType(chart, np),
				splitArea: { show: true }
			},
			yAxis:
			{
				//将在update中设置：
				//data
				
				name: chart.dataSetPropertyLabel(vp),
				nameGap: 5,
				type: chartSupport.evalDataSetPropertyAxisType(chart, vp),
				splitArea: { show: true }
			},
			visualMap:
			{
				//将在update中设置：
				//min
				//max
				
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
	
	chartSupport.heatmapUpdate = function(chart, results)
	{
		var signNameMap = chartSupport.chartSignNameMap(chart);
		var renderOptions= chart.renderOptions();
		
		var chartDataSets = chart.chartDataSetsMain();
		
		var xAxisData = [];
		var yAxisData = [];
		var seriesName = "";
		var seriesData = [];
		var min = undefined, max=undefined;
		
		for(var i=0; i<chartDataSets.length; i++)
		{
			var chartDataSet = chartDataSets[i];
			
			var result = chart.resultOf(results, chartDataSet);
			
			var np = chart.dataSetPropertyOfSign(chartDataSet, signNameMap.name);
			var vp = chart.dataSetPropertyOfSign(chartDataSet, signNameMap.value);
			var wp = chart.dataSetPropertyOfSign(chartDataSet, signNameMap.weight);
			
			var data = chart.resultValueObjects(result, [ np, vp, wp ]);
			
			for(var j=0; j<data.length; j++)
			{
				var dw = data[j].value[2];
				
				chartSupport.appendDistinct(xAxisData, data[j].value[0]);
				chartSupport.appendDistinct(yAxisData, data[j].value[1]);
				
				min = (min == undefined ? dw : Math.min(min, dw));
				max = (max == undefined ? dw : Math.max(max, dw));
			}
			
			chart.originalInfo(data, chartDataSet);
			
			seriesData = seriesData.concat(data);
			
			if(!seriesName)
				seriesName = chart.chartDataSetName(chartDataSet);
		}
		
		var series = [ { type: "heatmap", name: seriesName, data: seriesData } ];
		
		var options = { xAxis: { data: xAxisData }, yAxis: { data: yAxisData }, visualMap: {min: min, max: max}, series: series };
		chartSupport.checkMinAndMax(options.visualMap);
		
		options = chart.inflateUpdateOptions(results, options);
		
		chart.echartsOptions(options);
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
		var signNameMap = chartSupport.chartSignNameMap(chart);
		
		var echartsData = echartsEventParams.data;
		var data = {};
		
		data[signNameMap.name] = echartsData.value[0];
		data[signNameMap.value] = echartsData.value[1];
		data[signNameMap.weight] = echartsData.value[2];
		
		chart.eventData(chartEvent, data);
		chartSupport.setChartEventOriginalInfo(chart, chartEvent, echartsData);
	};
	
	//树图
	chartSupport.treeRender = function(chart, idSign, nameSign, parentSign, valueSign, options)
	{
		chartSupport.chartSignNameMap(chart, { id: idSign, name: nameSign, parent: parentSign, value: valueSign });
		
		options = chartSupport.inflateRenderOptions(chart,
		{
			//扩展属性：同同series[i].orient
			dgOrient: "LR",
			
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
					type: "tree"
				}
			]
		},
		options);
		
		chart.echartsInit(options);
	};
	
	chartSupport.treeUpdate = function(chart, results)
	{
		var renderOptions= chart.renderOptions();
		
		var options = { series: [ chartSupport.buildTreeNodeSeries(chart, results, { type: "tree" }) ] };
		chartSupport.treeInflateUpdateOptions(chart, options, renderOptions);
		
		options = chart.inflateUpdateOptions(results, options);
		
		chart.echartsOptions(options);
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
		var signNameMap = chartSupport.chartSignNameMap(chart);
		
		var echartsData = echartsEventParams.data;
		var data = {};
		
		data[signNameMap.id] = echartsData.idOrigin;
		data[signNameMap.name] = echartsData.name;
		data[signNameMap.parent] = echartsData.parent;
		data[signNameMap.value] = echartsData.value;

		chart.eventData(chartEvent, data);
		chartSupport.setChartEventOriginalInfo(chart, chartEvent, echartsData);
	};
	
	chartSupport.treeInflateUpdateOptions = function(chart, updateOptions, renderOptions)
	{
		var seriesEle = updateOptions.series[0];
		var seriesEleExt = {};
		
		seriesEle.orient = renderOptions.dgOrient;
		
		if(renderOptions.dgOrient == "LR")
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
		else if(renderOptions.dgOrient == "TB")
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
		else if(renderOptions.dgOrient == "RL")
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
		else if(renderOptions.dgOrient == "BT")
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
	chartSupport.treemapRender = function(chart, idSign, nameSign, parentSign, valueSign, options)
	{
		chartSupport.chartSignNameMap(chart, { id: idSign, name: nameSign, parent: parentSign, value: valueSign });
		
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
					type: "treemap"
				}
			]
		},
		options);
		
		chart.echartsInit(options);
	};
	
	chartSupport.treemapUpdate = function(chart, results)
	{
		var renderOptions= chart.renderOptions();
		
		var options = { series: [ chartSupport.buildTreeNodeSeries(chart, results, { type: "treemap" }) ] };
		
		options = chart.inflateUpdateOptions(results, options);
		
		chart.echartsOptions(options);
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
		var signNameMap = chartSupport.chartSignNameMap(chart);
		
		var echartsData = echartsEventParams.data;
		var data = {};
		
		//当事件在导航条时，echartsData为null
		if(echartsData)
		{
			data[signNameMap.id] = echartsData.idOrigin;
			data[signNameMap.name] = echartsData.name;
			data[signNameMap.parent] = echartsData.parent;
			data[signNameMap.value] = echartsData.value;
		}
		
		chart.eventData(chartEvent, data);
		chartSupport.setChartEventOriginalInfo(chart, chartEvent, echartsData);
		
		return data;
	};
	
	//旭日图
	
	chartSupport.sunburstRender = function(chart, idSign, nameSign, parentSign, valueSign, options)
	{
		chartSupport.chartSignNameMap(chart, { id: idSign, name: nameSign, parent: parentSign, value: valueSign });
		
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
					type: "sunburst"
				}
			]
		},
		options);
		
		chart.echartsInit(options);
	};
	
	chartSupport.sunburstUpdate = function(chart, results)
	{
		var renderOptions= chart.renderOptions();
		
		var options = { series: [ chartSupport.buildTreeNodeSeries(chart, results, { type: "sunburst" }) ] };
		
		options = chart.inflateUpdateOptions(results, options);
		
		chart.echartsOptions(options);
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
		var signNameMap = chartSupport.chartSignNameMap(chart);
		
		var echartsData = echartsEventParams.data;
		var data = {};
		
		data[signNameMap.id] = echartsData.idOrigin;
		data[signNameMap.name] = echartsData.name;
		data[signNameMap.parent] = echartsData.parent;
		data[signNameMap.value] = echartsData.value;

		chart.eventData(chartEvent, data);
		chartSupport.setChartEventOriginalInfo(chart, chartEvent, echartsData);
	};
	
	chartSupport.buildTreeNodeSeries = function(chart, results, initSeries)
	{
		initSeries = (initSeries || {});
		
		var signNameMap = chartSupport.chartSignNameMap(chart);
		
		var chartDataSets = chart.chartDataSetsMain();
		
		var seriesName = "";
		var seriesData = [];
		
		for(var i=0; i<chartDataSets.length; i++)
		{
			var chartDataSet = chartDataSets[i];
			
			var result = chart.resultOf(results, chartDataSet);
			
			if(!seriesName)
				seriesName = chart.chartDataSetName(chartDataSet);
			
			var np = chart.dataSetPropertyOfSign(chartDataSet, signNameMap.name);
			var ip = chart.dataSetPropertyOfSign(chartDataSet, signNameMap.id);
			var pp = chart.dataSetPropertyOfSign(chartDataSet, signNameMap.parent);
			var vp = chart.dataSetPropertyOfSign(chartDataSet, signNameMap.value);
			
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
				
				chart.originalInfo(node, chartDataSet, j);
				
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

	chartSupport.sankeyRender = function(chart, sourceNameSign, sourceValueSign, targetNameSign, targetValueSign, valueSign, options)
	{
		chartSupport.chartSignNameMap(chart,
				{ sourceName: sourceNameSign, sourceValue: sourceValueSign,
					targetName: targetNameSign, targetValue: targetValueSign, value: valueSign });
		
		options = chartSupport.inflateRenderOptions(chart,
		{
			//扩展属性：同同series[i].orient
			dgOrient: "horizontal",
			
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
	
	chartSupport.sankeyUpdate = function(chart, results)
	{
		var signNameMap = chartSupport.chartSignNameMap(chart);
		var renderOptions= chart.renderOptions();
		
		var chartDataSets = chart.chartDataSetsMain();
		
		var seriesName = "";
		var seriesData = [];
		var seriesLinks = [];
		
		for(var i=0; i<chartDataSets.length; i++)
		{
			var chartDataSet = chartDataSets[i];
			
			var result = chart.resultOf(results, chartDataSet);
			
			if(!seriesName)
				seriesName = chart.chartDataSetName(chartDataSet);
			
			var snp = chart.dataSetPropertyOfSign(chartDataSet, signNameMap.sourceName);
			var svp = chart.dataSetPropertyOfSign(chartDataSet, signNameMap.sourceValue);
			var tnp = chart.dataSetPropertyOfSign(chartDataSet, signNameMap.targetName);
			var tvp = chart.dataSetPropertyOfSign(chartDataSet, signNameMap.targetValue);
			var vp = chart.dataSetPropertyOfSign(chartDataSet, signNameMap.value);
			
			var data = chart.resultDatas(result);
			
			for(var j=0; j<data.length; j++)
			{
				var sd = { name: chart.resultRowCell(data[j], snp) };
				var td = { name: chart.resultRowCell(data[j], tnp) };
				
				if(svp)
					sd.value = chart.resultRowCell(data[j], svp);
				if(tvp)
					td.value = chart.resultRowCell(data[j], tvp);
				
				chart.originalInfo(sd, chartDataSet, j);
				
				var sidx = chartSupport.appendDistinct(seriesData, sd, "name");
				
				//新插入
				if(sidx == seriesData.length - 1 && seriesData[seriesData.length - 1] === sd)
				{
					chart.originalInfo(sd, chartDataSet, j);
				}
				
				var tidx = chartSupport.appendDistinct(seriesData, td, "name");
				
				//新插入
				if(tidx == seriesData.length - 1 && seriesData[seriesData.length - 1] === td)
				{
					chart.originalInfo(td, chartDataSet, j);
				}
				
				var link = {};
				link.source = sd.name;
				link.target = td.name;
				link.value = chart.resultRowCell(data[j], vp);
				
				link._sourceIndex = sidx;
				link._targetIndex = tidx;
				
				chart.originalInfo(link, chartDataSet, j);
				
				seriesLinks.push(link);
			}
		}
		
		var options = { series: [ { type: "sankey", name: seriesName, data: seriesData, links: seriesLinks } ] };
		chartSupport.sankeyInflateUpdateOptions(chart, options, renderOptions);
		
		options = chart.inflateUpdateOptions(results, options);
		
		chart.echartsOptions(options);
		
		chartFactory.extValueBuiltin(chart, "sankeySeriesData", seriesData);
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
		var signNameMap = chartSupport.chartSignNameMap(chart);
		
		var echartsData = echartsEventParams.data;
		var data = {};
		
		//TODO 点击节点没反应？？？
		//节点，仅使用源数据标记对象
		if(echartsEventParams.dataType == "node")
		{
			data[signNameMap.sourceName] = echartsData.name;
			data[signNameMap.sourceValue] = echartsData.value;
			
			chart.eventData(chartEvent, data);
			chartSupport.setChartEventOriginalInfo(chart, chartEvent, echartsData);
		}
		//边
		else if(echartsEventParams.dataType == "edge")
		{
			var seriesData = chartFactory.extValueBuiltin(chart, "sankeySeriesData");
			var sourceData = seriesData[echartsData._sourceIndex];
			var targetData = seriesData[echartsData._targetIndex];
			
			data[signNameMap.sourceName] = sourceData.name;
			data[signNameMap.sourceValue] = sourceData.value;
			
			if(targetData)
			{
				data[signNameMap.targetName] = targetData.name;
				data[signNameMap.targetValue] = targetData.value;
			}
			
			data[signNameMap.value] = echartsData.value;

			chart.eventData(chartEvent, data);
			chartSupport.setChartEventOriginalInfo(chart, chartEvent, echartsData);
		}
	};
	
	chartSupport.sankeyInflateUpdateOptions = function(chart, updateOptions, renderOptions)
	{
		var seriesEle = updateOptions.series[0];
		var seriesEleExt = {};
		
		seriesEle.orient = renderOptions.dgOrient;
		
		if(renderOptions.dgOrient == "horizontal")
		{
			seriesEleExt =
			{
				left: "16%",
                right: "16%",
                top: "12%",
                bottom: "12%"
			};
		}
		else if(renderOptions.dgOrient == "vertical")
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
	
	chartSupport.graphRender = function(chart, sourceIdSign, sourceNameSign, sourceCategorySign, sourceValueSign,
			targetIdSign, targetNameSign, targetCategorySign, targetValueSign, valueSign, options)
	{
		chartSupport.chartSignNameMap(chart,
				{ sourceId: sourceIdSign, sourceName: sourceNameSign, sourceCategory: sourceCategorySign, sourceValue: sourceValueSign,
					targetId: targetIdSign, targetName: targetNameSign, targetCategory: targetCategorySign, targetValue: targetValueSign,
					value: valueSign });
		
		options = chartSupport.inflateRenderOptions(chart,
		{
			//扩展配置项：最大数据标记像素数
			dgSymbolSizeMax: undefined,
			//扩展配置项：最小数据标记像素数
			dgSymbolSizeMin: undefined,
			//扩展配置项：同series[i].layout
			dgLayout: "force",
			
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
				//将在update中设置：
				//data
			},
			series:
			[
				//将在update中设置：
				//{}
				//设初值以免渲染报错
				{
					type: "graph"
				}
			]
		},
		options);
		
		chart.echartsInit(options);
	};
	
	chartSupport.graphUpdate = function(chart, results)
	{
		var signNameMap = chartSupport.chartSignNameMap(chart);
		var renderOptions= chart.renderOptions();
		
		var chartDataSets = chart.chartDataSetsMain();
		
		var legendData = [];
		var seriesName = "";
		var categories = [];
		var seriesData = [];
		var seriesLinks = [];
		
		var min = undefined, max = undefined;
		var symbolSizeMax = chartSupport.evalSymbolSizeMax(chart, renderOptions);
		var symbolSizeMin = chartSupport.evalSymbolSizeMin(chart, renderOptions, symbolSizeMax);
		
		for(var i=0; i<chartDataSets.length; i++)
		{
			var chartDataSet = chartDataSets[i];
			
			var result = chart.resultOf(results, chartDataSet);
			
			if(!seriesName)
				seriesName = chart.chartDataSetName(chartDataSet);
			
			var sip = chart.dataSetPropertyOfSign(chartDataSet, signNameMap.sourceId);
			var snp = chart.dataSetPropertyOfSign(chartDataSet, signNameMap.sourceName);
			var scp = chart.dataSetPropertyOfSign(chartDataSet, signNameMap.sourceCategory);
			var svp = chart.dataSetPropertyOfSign(chartDataSet, signNameMap.sourceValue);
			var tip = chart.dataSetPropertyOfSign(chartDataSet, signNameMap.targetId);
			var tnp = chart.dataSetPropertyOfSign(chartDataSet, signNameMap.targetName);
			var tcp = chart.dataSetPropertyOfSign(chartDataSet, signNameMap.targetCategory);
			var tvp = chart.dataSetPropertyOfSign(chartDataSet, signNameMap.targetValue);
			var vp = chart.dataSetPropertyOfSign(chartDataSet, signNameMap.value);
			
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
					
					min = (min == undefined ? sd.value : Math.min(min, sd.value));
					max = (max == undefined ? sd.value : Math.max(max, sd.value));
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
					
					min = (min == undefined ? td.value : Math.min(min, td.value));
					max = (max == undefined ? td.value : Math.max(max, td.value));
				}
				
				var sidx = chartSupport.appendDistinct(seriesData, sd, (sip ? "id" : "name"));
				
				//新插入
				if(sidx == seriesData.length - 1 && seriesData[seriesData.length - 1] === sd)
				{
					chart.originalInfo(sd, chartDataSet, j);
				}
				
				var tidx = chartSupport.appendDistinct(seriesData, td, (tip ? "id" : "name"));
				
				//新插入
				if(tidx == seriesData.length - 1 && seriesData[seriesData.length - 1] === td)
				{
					chart.originalInfo(td, chartDataSet, j);
				}
				
				//如果使用id值表示关系，对于数值型id，echarts会误当做数据索引，所以这里直接使用数据索引
				var link = {};
				link.source = sidx;
				link.target = tidx;
				
				if(vp)
					link.value = chart.resultRowCell(data[j], vp);
				
				chart.originalInfo(link, chartDataSet, j);
				
				seriesLinks.push(link);
			}
		}
		
		if(min == undefined && max == undefined && symbolSizeMin < 10)
			symbolSizeMin = 10;
		
		var series = [ { type: "graph", name: seriesName, categories: categories, data: seriesData, links: seriesLinks } ];
		var options = { legend: {data: legendData}, series: series };
		
		chartSupport.graphInflateUpdateOptions(chart, options, min, max, symbolSizeMax, symbolSizeMin, renderOptions);
		
		options = chart.inflateUpdateOptions(results, options);
		
		chart.echartsOptions(options);
		
		chartFactory.extValueBuiltin(chart, "graphSeriesData", seriesData);
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
		var signNameMap = chartSupport.chartSignNameMap(chart);
		
		var echartsData = echartsEventParams.data;
		
		var data = {};
		
		//节点，仅使用源数据标记对象
		if(echartsEventParams.dataType == "node")
		{
			data[signNameMap.sourceId] = echartsData.id;
			data[signNameMap.sourceName] = echartsData.name;
			data[signNameMap.sourceCategory] = echartsData._categoryOrigin;
			data[signNameMap.sourceValue] = echartsData.value;

			chart.eventData(chartEvent, data);
			chartSupport.setChartEventOriginalInfo(chart, chartEvent, echartsData);
		}
		//边
		else if(echartsEventParams.dataType == "edge")
		{
			var seriesData = chartFactory.extValueBuiltin(chart, "graphSeriesData");
			var sourceData = seriesData[echartsData.source];
			var targetData = seriesData[echartsData.target];
			
			data[signNameMap.sourceId] = sourceData.id;
			data[signNameMap.sourceName] = sourceData.name;
			data[signNameMap.sourceCategory] = sourceData._categoryOrigin;
			data[signNameMap.sourceValue] = sourceData.value;
			
			if(targetData)
			{
				data[signNameMap.targetId] = targetData.id;
				data[signNameMap.targetName] = targetData.name;
				data[signNameMap.targetCategory] = targetData._categoryOrigin;
				data[signNameMap.targetValue] = targetData.value;
			}
			
			data[signNameMap.value] = echartsData.value;

			chart.eventData(chartEvent, data);
			chartSupport.setChartEventOriginalInfo(chart, chartEvent, echartsData);
		}
	};
	
	chartSupport.graphInflateUpdateOptions = function(chart, updateOptions, min, max, symbolSizeMax, symbolSizeMin, renderOptions)
	{
		var seriesEle = updateOptions.series[0];
		
		seriesEle.layout = renderOptions.dgLayout;
		
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
	
	chartSupport.boxplotRender = function(chart, nameSign, minSign, lowerSign, medianSign,
	 			upperSign, maxSign, valueSign, options)
	{
		chartSupport.chartSignNameMap(chart, { name: nameSign, min: minSign, lower: lowerSign,
				median: medianSign, upper: upperSign, max: maxSign, value: valueSign });
		
		var chartDataSet = chart.chartDataSetFirst();
		var np = chart.dataSetPropertyOfSign(chartDataSet, nameSign);
		
		options = chartSupport.inflateRenderOptions(chart,
		{
			//扩展配置项：是否横向
			dgHorizontal: false,
			//扩展配置项：最大数据标记像素数
			dgSymbolSizeMax: undefined,
			//扩展配置项：最小数据标记像素数
			dgSymbolSizeMin: undefined,
			
			title: {
		        text: chart.name
		    },
			tooltip:
			{
				trigger: "item"
			},
			legend:
			{
				//将在update中设置：
				//data
			},
			xAxis:
			{
				name: chart.dataSetPropertyLabel(np),
				nameGap: 5,
				type: chartSupport.evalDataSetPropertyAxisType(chart, np),
				boundaryGap: true,
				splitLine: { show: false }
			},
			yAxis:
			{
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
			
			if(options.dgHorizontal)
			{
				var xAxisTmp = options.xAxis;
				options.xAxis = options.yAxis;
				options.yAxis = xAxisTmp;
			}
		});
		
		chart.echartsInit(options);
	};
	
	chartSupport.boxplotUpdate = function(chart, results)
	{
		var signNameMap = chartSupport.chartSignNameMap(chart);
		var renderOptions= chart.renderOptions();
		var dgHorizontal = renderOptions.dgHorizontal;
		
		var chartDataSets = chart.chartDataSetsMain();
		
		var legendData = [];
		var axisData = [];
		var series = [];
		
		var symbolSizeMax = chartSupport.evalSymbolSizeMax(chart, renderOptions);
		var symbolSizeMin = chartSupport.evalSymbolSizeMin(chart, renderOptions, symbolSizeMax);
		
		for(var i=0; i<chartDataSets.length; i++)
		{
			var chartDataSet = chartDataSets[i];
			
			var dataSetName = chart.chartDataSetName(chartDataSet);
			var result = chart.resultOf(results, chartDataSet);
			
			var np = chart.dataSetPropertyOfSign(chartDataSet, signNameMap.name);
			var minp = chart.dataSetPropertyOfSign(chartDataSet, signNameMap.min);
			
			//箱形数据集
			if(minp)
			{
				var vp =
				[
					minp,
					chart.dataSetPropertyOfSign(chartDataSet, signNameMap.lower),
					chart.dataSetPropertyOfSign(chartDataSet, signNameMap.median),
					chart.dataSetPropertyOfSign(chartDataSet, signNameMap.upper),
					chart.dataSetPropertyOfSign(chartDataSet, signNameMap.max)
				];
				var data = chart.resultNameValueObjects(result, np, vp);
				
				chart.originalInfo(data, chartDataSet);
				
				series.push({ type: "boxplot", name: dataSetName, data: data });
				legendData.push(dataSetName);
			}
			//异常值数据集
			else
			{
				var vps = chart.dataSetPropertiesOfSign(chartDataSet, signNameMap.value);
				
				for(var j=0; j<vps.length; j++)
				{
					var legendName = chartSupport.legendNameForMultipleSeries(chart, chartDataSets, i, dataSetName, vps, j);
					var vpsMy = (dgHorizontal ? [vps[j], np] : [np, vps[j]]);
					var data = chart.resultValueObjects(result, vpsMy);
					chartSupport.evalDataValueSymbolSize(data, 1, 1, symbolSizeMax, symbolSizeMin);
					
					chart.originalInfo(data, chartDataSet);
					
					series.push({ type: "scatter", name: legendName, data: data });
					legendData.push(legendName);
				}
			}
			
			chartSupport.appendDistinct(axisData, chart.resultRowArrays(result, np));
		}
		
		var options = { legend: {data: legendData}, series: series };
		
		//需要设置坐标值，不然刻度会错乱
		if(dgHorizontal)
			options.yAxis = { data: axisData };
		else
			options.xAxis = { data: axisData };
		
		options = chart.inflateUpdateOptions(results, options);
		
		chart.echartsOptions(options);
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
		var signNameMap = chartSupport.chartSignNameMap(chart);
		
		var seriesType = echartsEventParams.seriesType;
		var echartsData = (echartsEventParams.data || {});
		var echartsValue = (echartsData.value || []);
		var data = {};
		
		//箱形系列
		if(seriesType == "boxplot")
		{
			//value的第一个元素是数据索引
			var startIdx = (echartsValue.length > 5 ? 1 : 0);
			
			data[signNameMap.name] = echartsData.name;
			data[signNameMap.min] = echartsValue[startIdx];
			data[signNameMap.lower] = echartsValue[startIdx+1];
			data[signNameMap.median] = echartsValue[startIdx+2];
			data[signNameMap.upper] = echartsValue[startIdx+3];
			data[signNameMap.max] = echartsValue[startIdx+4];
		}
		//异常值系列
		else
		{
			var renderOptions= chart.renderOptions();
			var dgHorizontal = renderOptions.dgHorizontal;
			
			data[signNameMap.name] = (dgHorizontal ? echartsValue[1] : echartsValue[0]);
			data[signNameMap.value] = (dgHorizontal ? echartsValue[0] : echartsValue[1]);
		}
		
		chart.eventData(chartEvent, data);
		chartSupport.setChartEventOriginalInfo(chart, chartEvent, echartsData);
	};
	
	//词云图
	
	chartSupport.wordcloudRender = function(chart, nameSign, valueSign, options)
	{
		chartSupport.chartSignNameMap(chart, { name: nameSign, value: valueSign });
		
		//不支持在echarts主题中设置样式，只能在这里设置
		var chartTheme = chart.theme();
		
		options = chartSupport.inflateRenderOptions(chart,
		{
			//扩展配置项：由低到高值域颜色映射
			dgColorRange: chartTheme.graphRangeColors,
			//扩展配置项：由低到高值渐变色数组，如果不设置，将由dgColorRange自动计算
			dgColorGradients: undefined,
			
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
							"shadowColor" : chartFactory.getGradualColor(chartTheme, 0.9)
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
			var dgColorRange = options.dgColorRange;
			var dgColorGradients = [];
			for(var i=0; i<dgColorRange.length; i++)
			{
				var fromColor = dgColorRange[i];
				var toColor = ((i+1) < dgColorRange.length ? dgColorRange[i+1] : null);
				
				if(!toColor)
					break;
				
				dgColorGradients = dgColorGradients.concat(chartFactory.evalGradualColors(fromColor, toColor, 5));
			}
			options.dgColorGradients = dgColorGradients;
		});
		
		chart.echartsInit(options);
	};
	
	chartSupport.wordcloudUpdate = function(chart, results)
	{
		var signNameMap = chartSupport.chartSignNameMap(chart);
		var renderOptions= chart.renderOptions();
		
		var chartDataSets = chart.chartDataSetsMain();
		
		var seriesName = "";
		var seriesData = [];
		var min = undefined, max=undefined;
		
		for(var i=0; i<chartDataSets.length; i++)
		{
			var chartDataSet = chartDataSets[i];
			
			var result = chart.resultOf(results, chartDataSet);
			
			var data = chart.resultNameValueObjects(result, chart.dataSetPropertyOfSign(chartDataSet, signNameMap.name),
					chart.dataSetPropertyOfSign(chartDataSet, signNameMap.value));
			
			for(var j=0; j<data.length; j++)
			{
				min = (min == undefined ? data[j].value : Math.min(min, data[j].value));
				max = (max == undefined ? data[j].value : Math.max(max, data[j].value));
				
				chart.originalInfo(data[j], chartDataSet, j);
			}
			
			seriesData = seriesData.concat(data);
		}
		
		min = (min >= max ? max - 1 : min);
		
		//映射颜色值
		var dgColorGradients = renderOptions.dgColorGradients;
		if(dgColorGradients)
		{
			for(var i=0; i<seriesData.length; i++)
			{
				var colorIndex = parseInt((seriesData[i].value-min)/(max-min) * (dgColorGradients.length-1));
				seriesData[i].textStyle = { "color": dgColorGradients[colorIndex] };
			}
		}
		
		var options = { series: [ {type: "wordCloud", name: seriesName, data: seriesData} ] };
		
		options = chart.inflateUpdateOptions(results, options);
		
		chart.echartsOptions(options);
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
		var signNameMap = chartSupport.chartSignNameMap(chart);
		
		var echartsData = echartsEventParams.data;
		var data = {};
		
		data[signNameMap.name] = echartsData.name;
		data[signNameMap.value] = echartsData.value;
		
		chart.eventData(chartEvent, data);
		chartSupport.setChartEventOriginalInfo(chart, chartEvent, echartsData);
	};
	
	//水球图
	
	chartSupport.liquidfillRender = function(chart, nameSign, valueSign, options)
	{
		chartSupport.chartSignNameMap(chart, { name: nameSign, value: valueSign });
		
		//不支持在echarts主题中设置样式，只能在这里设置
		var chartTheme = chart.theme();
		
		options = chartSupport.inflateRenderOptions(chart,
		{
			//扩展配置项：同series[i].shape
			dgShape: "circle",
			//扩展配置项：如果仅有一个波浪数据，则自动复制扩充至这些个波浪数据
			dgAutoInflateWave: 3,
			
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
							shadowColor: chartFactory.getGradualColor(chartTheme, 0.4)
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
	
	chartSupport.liquidfillUpdate = function(chart, results)
	{
		var signNameMap = chartSupport.chartSignNameMap(chart);
		var renderOptions= chart.renderOptions();
		
		var chartDataSets = chart.chartDataSetsMain();
		
		var seriesData = [];
		
		for(var i=0; i<chartDataSets.length; i++)
		{
			var chartDataSet = chartDataSets[i];
			
			var result = chart.resultOf(results, chartDataSet);
			
			var nps = chart.dataSetPropertiesOfSign(chartDataSet, signNameMap.name);
			var vps = chart.dataSetPropertiesOfSign(chartDataSet, signNameMap.value);
			var npsNone = (nps==null || nps.length==0);
			
			if(!npsNone && nps.length!=vps.length)
				throw new Error("The ["+signNameMap.name+"] sign column must be "
						+"one-to-one with ["+signNameMap.value+"] sign column");
			
			var data = [];
			
			if(npsNone)
			{
				var ras = chart.resultRowArrays(result, vps);
				for(var j=0; j<ras.length; j++)
				{
					var ra = ras[j];
					for(var k=0; k<ra.length; k++)
					{
						var sv = { name: chart.dataSetPropertyLabel(vps[k]), value: ra[k] };
						chart.originalInfo(sv, chartDataSet, j);
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
						chart.originalInfo(sv, chartDataSet, j);
						data.push(sv);
					}
				}
			}
			
			seriesData = seriesData.concat(data);
		}
		
		//如果仅有一个波浪，则自动扩充
		if(seriesData.length == 1 && renderOptions.dgAutoInflateWave > 1)
		{
			for(var i=1; i<renderOptions.dgAutoInflateWave; i++)
			{
				var inflateValue = $.extend({}, seriesData[0]);
				seriesData.push(inflateValue);
			}
		}
		
		var options = { series: [ {type: "liquidFill", data: seriesData, shape: renderOptions.dgShape } ] };
		
		options = chart.inflateUpdateOptions(results, options);
		
		chart.echartsOptions(options);
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
		var signNameMap = chartSupport.chartSignNameMap(chart);
		
		var echartsData = echartsEventParams.data;
		var data = {};
		
		data[signNameMap.name] = echartsData.name;
		data[signNameMap.value] = echartsData.value;
		
		chart.eventData(chartEvent, data);
		chartSupport.setChartEventOriginalInfo(chart, chartEvent, echartsData);
	};
	
	//表格
	
	chartSupport.tableRender = function(chart, columnSign, options)
	{
		chartSupport.chartSignNameMap(chart, { column: columnSign });
		
		var chartTheme = chart.theme();
		
		var chartEle = chart.elementJquery();
		chartEle.addClass("dg-chart-table");
		
		//默认轮播配置
		var carouselConfig =
		{
			//是否开启
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
			hideVerticalScrollbar: true
		};
		
		var themeBindTableOptions = chartTheme._chartTableThemeBindOptions;
		if(themeBindTableOptions == null)
		{
			themeBindTableOptions=
			{
				//表头样式
				"header":
				{
					"color": chartTheme.titleColor,
					"backgroundColor": chartTheme.backgroundColor
				},
				//行
				"row":
				{
					//公用样式
					"color": chartTheme.color,
					
					//偶数行样式
					"odd":
					{
						"backgroundColor": chartFactory.getGradualColor(chartTheme, 0)
					},
					//奇数行样式
					"even":
					{
						"backgroundColor": chartTheme.backgroundColor
					},
					//悬浮行样式
					"hover":
					{
						"backgroundColor": chartFactory.getGradualColor(chartTheme, 0.2)
					},
					//选中行样式
					"selected":
					{
						"color": chartTheme.highlightTheme.color,
						"backgroundColor": chartTheme.highlightTheme.backgroundColor
					}
				},
				
				//单元格内容渲染函数，格式为：function(value, name, rowIndex, columnIndex, row, meta){ return ...; }
				renderValue: undefined
			};
			
			themeBindTableOptions._chartTableStyleClassName = chartFactory.nextElementId();
			themeBindTableOptions._chartTableStyleSheetId = chartFactory.nextElementId();
			
			chartTheme._chartTableThemeBindOptions = themeBindTableOptions;
		}
		
		var columns = [];
		
		var cps = chartSupport.tableGetColumnProperties(chart, columnSign);
		for(var i=0; i<cps.length; i++)
		{
			var column =
			{
				title: chart.dataSetPropertyLabel(cps[i]),
				data: cps[i].name,
				defaultContent: "",
				orderable: true,
				searchable: false,
				//下面完善
				render: undefined
			};
			
			columns.push(column);
		}
		
		options = chartSupport.inflateRenderOptions(chart,
		{
			//标题样式
			title:
			{
				"text": chart.name,
				"show": true,
				"color": chartTheme.titleColor,
				"backgroundColor": chartTheme.backgroundColor
			},
			
			//表格，格式参考上面的themeBindTableOptions
			table: undefined,
			
			//轮播，格式可以为：true、false、轮播interval数值、轮播interval返回函数、{...}
			carousel: undefined,
			
			//后置处理列函数，格式为：function(columns){ return columns; }
			postProcessColumns: undefined,
			
			//DataTable配置项
			"columns": columns,
			"data" : [],
			"ordering": false,
			"scrollX": true,
			"scrollY": undefined,
			"autoWidth": true,
	        "scrollCollapse": false,
			"pagingType": "full_numbers",
			"lengthMenu": [ 10, 25, 50, 75, 100 ],
			"pageLength": 50,
			"select" : { style : 'os' },
			"searching" : false,
			"language":
		    {
				"emptyTable": "",
				"zeroRecords": "",
				"lengthMenu": "每页_MENU_条",
				"info": "共_TOTAL_条，当前_START_-_END_条",
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
			//如果没有定义table选项，则采用全局themeBindTableOptions
			if(options.table == null)
				options.table = themeBindTableOptions;
			else
			{
				options.table = $.extend(true, {}, themeBindTableOptions, options.table);
				options.table._chartTableStyleClassName =chartFactory.nextElementId();
				options.table._chartTableStyleSheetId = chartFactory.nextElementId();
			}
			
			//完善分页选项
			options.paging = (options.paging != null ? options.paging : false);
			options.info = (options.info != null ? options.info :
									(options.paging ? true : false));
			options.dom = (options.dom != null ? options.dom :
									(options.paging ? "tilpr" : "t"));
			
			//完善轮播选项
			if(options.carousel == null)
			{
				
			}
			else if(options.carousel === true || options.carousel === false)
			{
				carouselConfig.enable = options.carousel;
			}
			else if(typeof(options.carousel) == "number" || $.isFunction(options.carousel))
			{
				carouselConfig.enable = true;
				carouselConfig.interval = options.carousel;
			}
			else
			{
				carouselConfig = $.extend(true, carouselConfig, options.carousel);
			}
			
			options.carousel = carouselConfig;
			
			//填充options.columns的render函数
			for(var i=0; i<options.columns.length; i++)
			{
				if(options.columns[i].render == null)
				{
					options.columns[i].render = function(value, type, row, meta)
					{
						//单元格展示绘制
						if(type == "display")
						{
							if(options.table.renderValue)
							{
								var rowIndex = meta.row;
								var columnIndex = meta.col;
								var name = columns[columnIndex].data;
								
								return options.table.renderValue(value, name,
										rowIndex, columnIndex, row, meta);
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
		});
		
		var evalHeight = (options.scrollY == null);
		
		//临时设一个较小值，后面会重新计算
		if(evalHeight)
			options.scrollY = 4;
		
		if(options.carousel.enable)
			chartEle.addClass("dg-chart-table-carousel");
		
		chartSupport.tableCreateTableStyleSheet(chart, options,
				options.table._chartTableStyleClassName, options.table._chartTableStyleSheetId);
		
		if(!options.title || !options.title.show)
			chartEle.addClass("dg-hide-title");
		
		var chartTitle = $("<div class='dg-chart-table-title' />").html(options.title.text).appendTo(chartEle);
		chartFactory.setStyles(chartTitle, options.title);
		var chartContent = $("<div class='dg-chart-table-content' />").appendTo(chartEle);
		var table = $("<table width='100%' class='hover stripe'></table>").appendTo(chartContent);
		var tableId = chart.id+"-table";
		table.attr("id", tableId);
		
		table.dataTable(options);
		
		var dataTable = table.DataTable();
		
		if(evalHeight)
		{
			chartSupport.tableEvalDataTableBodyHeight(chartContent, dataTable);
		}
		
		if(options.carousel.enable && options.carousel.hideVerticalScrollbar != false)
		{
			var tableBody = $(dataTable.table().body()).closest(".dataTables_scrollBody");
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
		
		//固定选择列后hover效果默认不能同步，需要自己实现
		if(options.fixedColumns)
		{
			$(dataTable.table().body()).on("mouseover mouseout", "tr",
			function(event)
			{
				var rowIndex = $(this).index() + 1;
				var $tableContainer = $(dataTable.table().container());
				
				$(".dataTable", $tableContainer).each(function()
				{
					if(event.type == "mouseover")
						$("tr:eq("+rowIndex+")", this).addClass("hover");
					else
						$("tr:eq("+rowIndex+")", this).removeClass("hover");
				});
			});
		}
		
		chart.internal(dataTable);
	};
	
	chartSupport.tableUpdate = function(chart, results, options)
	{
		var renderOptions = chart.renderOptions();
		var dataTable = chart.internal();
		var chartEle = chart.elementJquery();
		
		var chartDataSets = chart.chartDataSetsMain();
		
		var updateOptions = { data: [] };
		
		for(var i=0; i<chartDataSets.length; i++)
		{
			var chartDataSet = chartDataSets[i];
			
			var result = chart.resultOf(results, chartDataSet);
			var resultDatas = chart.resultDatas(result);
			
			//复制，避免污染原始数据
			for(var j=0; j<resultDatas.length; j++)
			{
				var data = $.extend({}, resultDatas[j]);
				chart.originalInfo(data, chartDataSet, j);
				updateOptions.data.push(data);
			}
		}
		
		chartSupport.tableStopCarousel(chart);
		
		updateOptions = chart.inflateUpdateOptions(results, updateOptions);
		
		chartSupport.tableAddDataTableData(dataTable, updateOptions.data, 0);
		chartSupport.tableAdjust(chart);
		
		if(renderOptions.carousel.enable)
		{
			chartEle.data("tableCarouselPrepared", false);
			chartSupport.tableStartCarousel(chart);
		}
	};
	
	chartSupport.tableResize = function(chart)
	{
		var chartContent = chartSupport.tableGetChartContent(chart);
		var dataTable = chart.internal();
		
		chartSupport.tableEvalDataTableBodyHeight(chartContent, dataTable);
		
		dataTable.columns.adjust();
		if(dataTable.init().fixedColumns)
			dataTable.fixedColumns().relayout();
	};
	
	chartSupport.tableDestroy = function(chart)
	{
		var chartEle = chart.elementJquery();
		chartSupport.tableStopCarousel(chart);
		chartEle.removeClass("dg-chart-table");
		chartEle.removeClass("dg-hide-title");
		chartEle.removeClass("dg-chart-table-carousel");
		$(".dg-chart-table-title", chartEle).remove();
		$(".dg-chart-table-content", chartEle).remove();
	};
	
	chartSupport.tableOn = function(chart, eventType, handler)
	{
		var handlerDelegation = function(htmlEvent)
		{
			var rowElement = this;
			var chartEvent = chart.eventNewHtml(eventType, htmlEvent);
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
		var signNameMap = chartSupport.chartSignNameMap(chart);
		
		var dataTable = chart.internal();
		
		var chartData = dataTable.row(rowElement).data();
		
		var data = {};
		
		if(chartData)
		{
			var columnData = [];
			
			var cps = chartSupport.tableGetColumnProperties(chart, signNameMap.column);
			for(var i=0; i<cps.length; i++)
				columnData[i] = chartData[cps[i].name];
			
			data[signNameMap.column] = columnData;
		}
		
		chart.eventData(chartEvent, data);
		chartSupport.setChartEventOriginalInfo(chart, chartEvent, chartData);
	};
	
	chartSupport.tableGetChartContent = function(chart)
	{
		//图表的数据透视表功能也采用的是DataTable组件，可能会与表格图表处在同一个图表div内，
		//因此，获取图表表格的DOM操作都应限定在".dg-chart-table-content"内
		
		return $(".dg-chart-table-content", chart.element());
	};
	
	chartSupport.tableGetColumnProperties = function(chart, columnSign)
	{
		var chartDataSet = chart.chartDataSetFirst();
		var cps = chart.dataSetPropertiesOfSign(chartDataSet, columnSign);
		if(!cps || cps.length == 0)
			cps =(chartDataSet && chartDataSet.dataSet ? (chartDataSet.dataSet.properties || []) : []);
		
		return cps;
	};
	
	chartSupport.tableCreateTableStyleSheet = function(chart, chartOptions, styleClassName, styleSheetId)
	{
		chart.elementJquery().addClass(styleClassName);
		
		if(chartFactory.isStyleSheetCreated(styleSheetId))
			return false;
		
		//样式要加".dg-chart-table-content"限定，因为图表的数据透视表功能也采用的是DataTable组件，可能会处在同一个表格图表div内
		var qualifier = "." + styleClassName + " .dg-chart-table-content";
		
		var cssText = 
			qualifier + " table.dataTable tbody tr{"
			+ chartFactory.stylesObjToCssText(chartOptions.table.row)
			+" }\n"
			+qualifier + " table.dataTable thead th,\n"
			+qualifier + " table.dataTable thead td{"
			+ chartFactory.stylesObjToCssText(chartOptions.table.header)
			+" }\n"
			+qualifier + " table.dataTable.stripe tbody tr.odd,\n"
			+qualifier + " table.dataTable.display tbody tr.odd{"
			+ chartFactory.stylesObjToCssText(chartOptions.table.row.odd)
			+" }\n"
			+qualifier + " table.dataTable.stripe tbody tr.even,\n"
			+qualifier + " table.dataTable.display tbody tr.even{"
			+ chartFactory.stylesObjToCssText(chartOptions.table.row.even)
			+" }\n"
			+qualifier + " table.dataTable.hover tbody tr.hover,\n"
			+qualifier + " table.dataTable.hover tbody tr:hover,\n"
			+qualifier + " table.dataTable.display tbody tr:hover,\n"
			+qualifier + " table.dataTable.hover tbody tr.hover.selected,\n"
			+qualifier + " table.dataTable.hover tbody > tr.selected:hover,\n"
			+qualifier + " table.dataTable.hover tbody > tr > .selected:hover,\n"
			+qualifier + " table.dataTable.display tbody > tr.selected:hover,\n"
			+qualifier + " table.dataTable.display tbody > tr > .selected:hover{"
			+ chartFactory.stylesObjToCssText(chartOptions.table.row.hover)
			+" }\n"
			+qualifier + " table.dataTable tbody > tr.selected,\n"
			+qualifier + " table.dataTable tbody > tr > .selected,\n"
			+qualifier + " table.dataTable.stripe tbody > tr.even.selected,\n"
			+qualifier + " table.dataTable.stripe tbody > tr.even > .selected,\n"
			+qualifier + " table.dataTable.display tbody > tr.even.selected,\n"
			+qualifier + " table.dataTable.display tbody > tr.even > .selected,\n"
			+qualifier + " table.dataTable.stripe tbody > tr.odd.selected,\n"
			+qualifier + " table.dataTable.stripe tbody > tr.odd > .selected,\n"
			+qualifier + " table.dataTable.display tbody > tr.odd.selected,\n"
			+qualifier + " table.dataTable.display tbody > tr.odd > .selected{"
			+ chartFactory.stylesObjToCssText(chartOptions.table.row.selected)
			+" }\n"
			+qualifier + " table.dataTable thead th.sorting div.DataTables_sort_wrapper span{"
			+ " background:" + chartOptions.table.header.color+";"
			+" }\n"
			+qualifier + " table.dataTable thead th.sorting_asc div.DataTables_sort_wrapper span{"
			+ " border-bottom-color:" + chartOptions.table.header.color+";"
			+ " background: none;"
			+" }\n"
			+qualifier + " table.dataTable thead th.sorting_desc div.DataTables_sort_wrapper span{"
			+ " border-top-color:" + chartOptions.table.header.color+";"
			+ " background: none;"
			+" }\n";
		
		chartFactory.createStyleSheet(styleSheetId, cssText, "beforeFirstScript");
		
		return true;
	};
	
	chartSupport.tableEvalDataTableBodyHeight = function($chartContent, dataTable)
	{
		var chartContentHeight = $chartContent.height();
		var container = $(dataTable.table().container());
		var containerHeight = container.outerHeight(true);
		var tableHeader = $(dataTable.table().header()).closest(".dataTables_scrollHead");
		var tableHeaderHeight = tableHeader.outerHeight(true);
		var tableBody = $(dataTable.table().body()).closest(".dataTables_scrollBody");
		var fixedColumnContainer = tableBody.closest(".DTFC_ScrollWrapper");
		var tableBodyHeight = chartContentHeight - tableHeaderHeight;
		tableBody.css("height", tableBodyHeight);
		fixedColumnContainer.css("height", tableBody.parent().height());
		
		containerHeight = container.outerHeight(true);
		
		//如果表格容器高度不等于图表内容限高，则重新设置
		if(containerHeight - chartContentHeight != 0)
		{
			tableBodyHeight = tableBodyHeight - (containerHeight - chartContentHeight);
			tableBody.css("height", tableBodyHeight);
			fixedColumnContainer.css("height", tableBody.parent().height());
		}
	};
	
	chartSupport.tableAddDataTableData = function(dataTable, datas, startRowIndex)
	{
		var rows = dataTable.rows();
		var removeRowIndexes = [];
		var dataIndex = 0;
		
		if(startRowIndex != null)
		{
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
		}
		
		for(; dataIndex<datas.length; dataIndex++)
			dataTable.row.add(datas[dataIndex]);
		
		dataTable.rows(removeRowIndexes).remove();
		
		dataTable.draw();
	};
	
	/**
	 * 调整图表表格。
	 * 当表格隐藏显示、位置调整、数据变更后，可能会出现表头、固定列错位的情况，需要重新调整。
	 */
	chartSupport.tableAdjust = function(chart)
	{
		var dataTable = chart.internal();
		
		dataTable.columns.adjust();
		if(dataTable.init().fixedHeader)
			dataTable.fixedHeader.adjust();
		if(dataTable.init().fixedColumns)
			dataTable.fixedColumns().relayout();
	};
	
	/**
	 * 表格准备轮播。
	 */
	chartSupport.tablePrepareCarousel = function(chart)
	{
		var chartContent = chartSupport.tableGetChartContent(chart);
		var dataTable = chart.internal();
		var rowCount = dataTable.rows().indexes().length;
		
		//空表格
		if(rowCount == 0)
			return;
		
		var scrollBody = $(".dataTables_scrollBody", chartContent);
		var scrollTable = $(".dataTable", scrollBody);
		
		var scrollBodyHeight = scrollBody.height();
		
		while(true)
		{
			//必须成倍添加数据，避免出现轮播次序混乱
			//且至少添加一倍，保证滚动平滑
			for(var i=0; i<rowCount; i++)
			{
				var addData = dataTable.row(i).data();
				dataTable.row.add(addData);
			}
			
			dataTable.draw();
			
			var scrollTableHeight = scrollTable.height();
			
			//表格高度至少为容器高度两倍，保证滚动平滑
			if(scrollTableHeight >= scrollBodyHeight*2)
				break;
		}
	};
	
	/**
	 * 表格开始轮播。
	 */
	chartSupport.tableStartCarousel = function(chart)
	{
		var renderOptions = chart.renderOptions();
		var chartEle = chart.elementJquery();
		var chartContent = chartSupport.tableGetChartContent(chart);
		var dataTable = chart.internal();
		
		var rowCount = dataTable.rows().indexes().length;
		
		//空表格
		if(rowCount == 0)
			return;
		
		chartSupport.tableStopCarousel(chart);
		chartEle.data("tableCarouselStatus", "start");
		
		var scrollBody = $(".dataTables_scrollBody", chartContent);
		var scrollTable = $(".dataTable", scrollBody);
		
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
			var scrollTop = scrollTable.css("margin-top");
			scrollTop = (scrollTop.indexOf("px") == scrollTop.length - 2 ? scrollTop.substring(0, scrollTop.length - 2) : scrollTop);
			scrollTop = (Math.abs(parseInt(scrollTop)) || 0);
			
			var currentRow = null;
			var currentRowHeight = null;
			var currentRowVisibleHeight = null;
			
			var removeRowIndexes = [];
			var addRowDatas = [];
			
			var offset = 0;
			var idx = 0;
			while(true)
			{
				var row0 = dataTable.row(idx);
				var $row0 = $(row0.node());
				var row0Height = $row0.outerHeight(true);
				
				//第一行仍可见
				if(scrollTop < (offset + row0Height))
				{
					currentRow = row0.node();
					currentRowHeight = row0Height;
					currentRowVisibleHeight = offset + row0Height - scrollTop;
					
					break;
				}
				
				var row1 = dataTable.row(idx+1);
				var $row1 = $(row1.node());
				var row1Height = $row1.outerHeight(true);
				
				//第二行仍可见
				if(scrollTop < (offset + row0Height + row1Height))
				{
					currentRow = row1.node();
					currentRowHeight = row1Height;
					currentRowVisibleHeight = offset + row0Height + row1Height - scrollTop;
					
					break;
				}
				
				//必须同时移除两行，不然奇偶行会变化，导致颜色交替重绘
				removeRowIndexes.push(idx);
				removeRowIndexes.push(idx+1);
				addRowDatas.push(row0.data());
				addRowDatas.push(row1.data());
				
				offset += row0Height + row1Height;
				idx += 2;
			}
			
			if(removeRowIndexes.length > 0)
			{
				dataTable.rows(removeRowIndexes).remove().draw();
				scrollTop = scrollTop - offset;
			}
			
			var span = ($.isFunction(renderOptions.carousel.span) ?
					renderOptions.carousel.span(currentRow, currentRowVisibleHeight, currentRowHeight) : renderOptions.carousel.span);
			
			scrollTable.css("margin-top", (0 - (scrollTop + span))+"px");
			
			if(addRowDatas.length > 0)
				dataTable.rows.add(addRowDatas).draw();
		}
		
		var interval = null;
		
		if(!$.isFunction(renderOptions.carousel.interval))
		{
			interval = renderOptions.carousel.interval;
		}
		else
		{
			if(doCarousel)
			{
				interval = renderOptions.carousel.interval(currentRow, currentRowVisibleHeight, currentRowHeight);
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
	
	chartSupport.labelRender = function(chart, nameSign, valueSign, options)
	{
		chartSupport.chartSignNameMap(chart, { name: nameSign, value: valueSign });
		
		var chartEle = chart.elementJquery();
		chartEle.addClass("dg-chart-label");
		
		var labelWrapper = $(".dg-chart-label-wrapper", chartEle);
		if(labelWrapper.length == 0)
			 labelWrapper = $("<div class='dg-chart-label-wrapper' />").appendTo(chartEle);
		
		options = chartSupport.inflateRenderOptions(chart,
		{
			//将在update中设置：
			//data
			
			//是否所有标签都行内显示
			"inline": false,
			//是否标签值在前
			"valueFirst": false,
			//标签名样式，这里不必添加默认样式，因为图表元素已设置
			"name":
			{
				"show": true
			},
			//标签值样式，这里不必添加默认样式，因为图表元素已设置
			"value": {}
		},
		options);
		
		if(options.inline == true)
			labelWrapper.addClass("dg-chart-label-inline");
		
		// < @deprecated 兼容2.7.0版本的{label:{name:{...},value:{...}}}配置项结构，未来版本会移除
		if(options.label && options.label.name)
			options.name = $.extend(true, {}, options.name, options.label.name);
		if(options.label && options.label.value)
			options.value = $.extend(true, {}, options.value, options.label.value);
		// > @deprecated 兼容2.7.0版本的{label:{name:{...},value:{...}}}配置项结构，未来版本会移除
		
		chart.internal(labelWrapper[0]);
	};
	
	chartSupport.labelUpdate = function(chart, results)
	{
		var signNameMap = chartSupport.chartSignNameMap(chart);
		var renderOptions = chart.renderOptions();
		var valueFirst = renderOptions.valueFirst;
		var showName = (renderOptions.name && renderOptions.name.show);
		
		var chartDataSets = chart.chartDataSetsMain();
		
		var $parent = $(chart.internal());
		
		$(".dg-chart-label-item", $parent).addClass("dg-chart-label-item-pending");
		
		var updateOptions = { data: [] };
		
		for(var i=0; i<chartDataSets.length; i++)
		{
			var chartDataSet = chartDataSets[i];
			
			var result = chart.resultOf(results, chartDataSet);
			
			var nps = chart.dataSetPropertiesOfSign(chartDataSet, signNameMap.name);
			var vps = chart.dataSetPropertiesOfSign(chartDataSet, signNameMap.value);
			var npsNone = (nps==null || nps.length==0);
			
			if(!npsNone && nps.length!=vps.length)
				throw new Error("The ["+signNameMap.name+"] sign column must be "
						+"one-to-one with ["+signNameMap.value+"] sign column");
			
			if(npsNone)
			{
				var ras = chart.resultRowArrays(result, vps);
				for(var j=0; j<ras.length; j++)
				{
					var ra = ras[j];
					for(var k=0; k<ra.length; k++)
					{
						var sv = { name: chart.dataSetPropertyLabel(vps[k]), value: ra[k] };
						chart.originalInfo(sv, chartDataSet, j);
						
						updateOptions.data.push(sv);
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
						chart.originalInfo(sv, chartDataSet, j);
						
						updateOptions.data.push(sv);
					}
				}
			}
		}
		
		updateOptions = chart.inflateUpdateOptions(results, updateOptions);
		
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
					
					if(renderOptions.value)
						chartFactory.setStyles($labelValue, renderOptions.value);
					
					if(showName)
					{
						$labelName = $("<div class='label-name'></div>").appendTo($label);
						
						if(renderOptions.name)
							chartFactory.setStyles($labelName, renderOptions.name);
					}
				}
				else
				{
					if(showName)
					{
						$labelName = $("<div class='label-name'></div>").appendTo($label);
						
						if(renderOptions.name)
							chartFactory.setStyles($labelName, renderOptions.name);
					}
					
					$labelValue = $("<div class='label-value'></div>").appendTo($label);
					
					if(renderOptions.value)
						chartFactory.setStyles($labelValue, renderOptions.value);
				}
			}
			else
			{
				$labelName = $(".label-name", $label);
				$labelValue = $(".label-value", $label);
				
				$label.removeClass("dg-chart-label-item-pending");
			}
			
			if(showName)
				$labelName.html(labelData.name);
			$labelValue.html(labelData.value);
			
			$label.data("_dgChartLabelChartData", labelData);
		}
		
		$(".dg-chart-label-item-pending", $parent).remove();
	};
	
	chartSupport.labelResize = function(chart)
	{
		
	};
	
	chartSupport.labelDestroy = function(chart)
	{
		var chartEle = chart.elementJquery();
		chartEle.removeClass("dg-chart-label");
		$(chart.internal()).remove();
	};
	
	chartSupport.labelOn = function(chart, eventType, handler)
	{
		var handlerDelegation = function(htmlEvent)
		{
			var $label = $(this);
			var chartEvent = chart.eventNewHtml(eventType, htmlEvent);
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
		var signNameMap = chartSupport.chartSignNameMap(chart);
		
		var chartData = $label.data("_dgChartLabelChartData");
		
		var data = {};
		
		if(chartData)
		{
			data[signNameMap.name] = chartData.name;
			data[signNameMap.value] = chartData.value;
		}
		
		chart.eventData(chartEvent, data);
		chartSupport.setChartEventOriginalInfo(chart, chartEvent, chartData);
	};
	
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
		
		//如果未定义，则采用表格组件，避免空白页，又可以让用户浏览和调试数据
		if(!customRenderer)
		{
			chartSupport.tableRender(chart, "column");
		}
		else
		{
			customRenderer.render(chart);
		}
	};
	
	chartSupport.customAsyncUpdate = function(chart, results)
	{
		var customRenderer = chartSupport.customGetCustomRenderer(chart, true);
		
		if(!customRenderer || customRenderer.asyncUpdate == null)
			return false;
		
		if(typeof(customRenderer.asyncUpdate) == "function")
			return customRenderer.asyncUpdate(chart, results);
		
		return (customRenderer.asyncUpdate == true);
	};
	
	chartSupport.customUpdate = function(chart, results)
	{
		var customRenderer = chartSupport.customGetCustomRenderer(chart, true);
		
		//如果未定义，则采用表格组件，避免空白页，又可以让用户浏览和调试数据
		if(!customRenderer)
		{
			chartSupport.tableUpdate(chart, results);
		}
		else
		{
			customRenderer.update(chart, results);
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
		var customRenderer = chartSupport.customGetCustomRenderer(chart);
		
		if(customRenderer.destroy)
			customRenderer.destroy(chart);
	};
	
	chartSupport.customOn = function(chart, eventType, handler)
	{
		var customRenderer = chartSupport.customGetCustomRenderer(chart);
		
		if(customRenderer.on)
			customRenderer.on(chart, eventType, handler);
		else
			throw new Error("Custom renderer 's [on] undefined");
	};
	
	chartSupport.customOff = function(chart, eventType, handler)
	{
		var customRenderer = chartSupport.customGetCustomRenderer(chart);
		
		if(customRenderer.off)
			customRenderer.off(chart, eventType, handler);
		else
			throw new Error("Custom renderer 's [off] undefined");
	};
	
	chartSupport.customGetCustomRenderer = function(chart, nullable)
	{
		if(nullable == null)
			nullable = false;
		
		var renderer = chart.renderer();
		
		if(renderer == null && !nullable)
			throw new Error("Chart renderer must be defined");
		
		return renderer;
	};
	
	//---------------------------------------------------------
	//    公用函数开始
	//---------------------------------------------------------
	
	//org.datagear.analysis.DataSetProperty.DataType
	chartSupport.DataSetPropertyDataType =
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
	 * 计算指定数据集属性的坐标轴类型。
	 */
	chartSupport.evalDataSetPropertyAxisType = function(chart, dataSetProperty)
	{
		var type = "category";
		
		if(chartSupport.isDataTypeNumber(dataSetProperty))
		{
			type = "value";
		}
		else if(chartSupport.isDataTypeAboutDate(dataSetProperty))
		{
			var resultDataFormat = chart.resultDataFormat();
			if(!resultDataFormat)
				resultDataFormat = (chart.dashboard ? chart.dashboard.resultDataFormat() : null);
			
			if(resultDataFormat)
			{
				if(chartSupport.isDataTypeDate(dataSetProperty)
					&& resultDataFormat.dateType == chartSupport.ResultDataFormatType.NUMBER)
				{
					type = "time";
				}
				else if(chartSupport.isDataTypeTime(dataSetProperty)
					&& resultDataFormat.timeType == chartSupport.ResultDataFormatType.NUMBER)
				{
					type = "time";
				}
				else if(chartSupport.isDataTypeTimestamp(dataSetProperty)
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
	 * @param beforeProcessHandler
	 * @returns 一个新的图表渲染options
	 */
	chartSupport.inflateRenderOptions = function(chart, defaultOptions, builtinOptions,
												afterMergeHandlerFirst, beforeProcessHandler)
	{
		var renderOptions = $.extend(true, {}, defaultOptions, builtinOptions);
		
		if(afterMergeHandlerFirst != null)
			afterMergeHandlerFirst(renderOptions, chart);
		
		return chart.inflateRenderOptions(renderOptions, beforeProcessHandler);
	};
	
	/**
	 * 指定数据集属性数据是否字符串类型。
	 */
	chartSupport.isDataTypeString = function(dataSetProperty)
	{
		var dataType = (dataSetProperty ? (dataSetProperty.type || dataSetProperty) : "");
		return (dataType == chartSupport.DataSetPropertyDataType.STRING);
	};
	
	/**
	 * 指定数据集属性数据是否数值类型。
	 */
	chartSupport.isDataTypeNumber = function(dataSetProperty)
	{
		var dataType = (dataSetProperty ? (dataSetProperty.type || dataSetProperty) : "");
		return (dataType == chartSupport.DataSetPropertyDataType.NUMBER
				|| dataType == chartSupport.DataSetPropertyDataType.INTEGER
				|| dataType == chartSupport.DataSetPropertyDataType.DECIMAL);
	};
	
	/**
	 * 指定数据集属性数据是否日期、时间、时间戳类型。
	 */
	chartSupport.isDataTypeAboutDate = function(dataSetProperty)
	{
		var dataType = (dataSetProperty ? (dataSetProperty.type || dataSetProperty) : "");
		return (dataType == chartSupport.DataSetPropertyDataType.DATE
				|| dataType == chartSupport.DataSetPropertyDataType.TIME
				|| dataType == chartSupport.DataSetPropertyDataType.TIMESTAMP);
	};
	
	/**
	 * 指定数据集属性数据是否日期类型。
	 */
	chartSupport.isDataTypeDate = function(dataSetProperty)
	{
		var dataType = (dataSetProperty ? (dataSetProperty.type || dataSetProperty) : "");
		return (dataType == chartSupport.DataSetPropertyDataType.DATE);
	};
	
	/**
	 * 指定数据集属性数据是否时间类型。
	 */
	chartSupport.isDataTypeTime = function(dataSetProperty)
	{
		var dataType = (dataSetProperty ? (dataSetProperty.type || dataSetProperty) : "");
		return (dataType == chartSupport.DataSetPropertyDataType.TIME);
	};
	
	/**
	 * 指定数据集属性数据是否时间戳类型。
	 */
	chartSupport.isDataTypeTimestamp = function(dataSetProperty)
	{
		var dataType = (dataSetProperty ? (dataSetProperty.type || dataSetProperty) : "");
		return (dataType == chartSupport.DataSetPropertyDataType.TIMESTAMP);
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
	 * @param sourceArray
	 * @param append 追加元素、数组，可以是基本类型、对象类型
	 * @param distinctPropertyName 当是对象类型时，用于指定判断重复的属性名
	 * @returns 追加的或重复元素的索引、或者索引数组
	 */
	chartSupport.appendDistinct = function(sourceArray, append, distinctPropertyName)
	{
		var isArray = $.isArray(append);
		
		if(!isArray)
			append = [ append ];
		
		var indexes = [];
		
		for(var i=0; i<append.length; i++)
		{
			var av = (distinctPropertyName != undefined ? append[i][distinctPropertyName] : append[i]);
			var foundIdx = -1;
			
			for(var j=0; j<sourceArray.length; j++)
			{
				var sv = (distinctPropertyName != undefined ? sourceArray[j][distinctPropertyName] : sourceArray[j]);
				
				if(sv == av)
				{
					foundIdx = j;
					break;
				}
			}
			
			if(foundIdx > -1)
				indexes[i] = foundIdx;
			else
			{
				sourceArray.push(append[i]);
				indexes[i] = (sourceArray.length - 1);
			}
		}
		
		return (isArray ? indexes : indexes[0]);
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
	 * 校正对象的"min"、"max"属性值。
	 */
	chartSupport.checkMinAndMax = function(obj, defaultMin, defaultMax)
	{
		if(!obj)
			return;
		
		if(defaultMin == null)
			defaultMin = 0;
		if(defaultMax == null)
			defaultMax = 100;
		
		if(obj.min == null && obj.max == null)
		{
			obj.min = defaultMin;
			obj.max = defaultMax;
		}
		else if(obj.min == null || obj.min >= obj.max)
			obj.min = obj.max - 1;
		else if(obj.max == null || obj.max <= obj.min)
			obj.max = obj.min + 1;
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
	
	chartSupport.chartSignNameMap = function(chart, signNameMap)
	{
		return chartFactory.extValueBuiltin(chart, "signNameMap", signNameMap);
	};
	
	chartSupport.setChartEventOriginalInfo = function(chart, chartEvent, chartInternalData)
	{
		var originalInfo = chart.originalInfo(chartInternalData);
		chart.eventOriginalInfo(chartEvent, originalInfo);
	};
	
	chartSupport.bindChartEventHandlerForEcharts = function(chart, eventType, eventHanlder, chartEventDataSetter)
	{
		var hanlderDelegation = function(params)
		{
			var chartEvent = chart.eventNewEcharts(eventType, params);
			chartEventDataSetter(chart, chartEvent, params);
			
			chart.callEventHandler(eventHanlder, chartEvent);
		};
		
		chart.registerEventHandlerDelegation(eventType, eventHanlder, hanlderDelegation);
		chart.internal().on(eventType, "series", hanlderDelegation);
	};
	
	//计算图例名
	chartSupport.legendNameForMultipleSeries = function(chart, chartDataSets, chartDataSetIdx, chartDataSetName,
					seriesProperties, seriesPropertyIdx)
	{
		var legendName = chartDataSetName;
		
		if(chartDataSets.length > 1 && seriesProperties.length > 1)
		{
			legendName = chartDataSetName +"-" + chart.dataSetPropertyLabel(seriesProperties[seriesPropertyIdx]);
		}
		else if(seriesProperties.length > 1)
		{
			legendName = chart.dataSetPropertyLabel(seriesProperties[seriesPropertyIdx]);
		}
		
		return legendName;
	};
	
	/**
	 * 计算最大图符元素尺寸
	 * @param chart
	 * @param options
	 * @param ratio 可选，自动获取的比率
	 */
	chartSupport.evalSymbolSizeMax = function(chart, options, ratio)
	{
		var symbolSizeMax = (options ? options.dgSymbolSizeMax : undefined);
		ratio = (ratio == undefined ? 0.1 : ratio);
		
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
		var symbolSizeMin = (options ? options.dgSymbolSizeMin : undefined);
		ratio = (ratio == undefined ? 0.125 : ratio);
		
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
		if(symbolSizeMin == undefined)
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
	
	/**
	 * 获取/设置ECharts地图图表的地图名option。
	 * 注意：获取操作不会读取chart.map()
	 *
	 * @param chart
	 * @param options
	 * @param isGeo 是否GEO地图坐标系（options.geo.map）而非地图系列（series.type="map"）
	 * @param map 可选，要设置的地图名
	 * @returns 获取操作时的地图名
	 */
	chartSupport.echartsMapChartMapOption = function(chart, options, isGeo, map)
	{
		if(map === undefined)
		{
			if(isGeo)
				map = (options.geo ? options.geo.map : null);
			else
				map = (options.series && options.series.length > 0 ? options.series[0].map : null);
			
			return map;
		}
		else
		{
			if(isGeo)
			{
				if(!options.geo)
					options.geo = {};
				
				options.geo.map = map;
			}
			else
			{
				if(!options.series)
					options.series = [];
				if(!options.series[0])
					options.series[0] = {};
				
				options.series[0].map = map;
			}
		}
	};
	
	chartSupport.echartsMapChartInit = function(chart, options, isGeo)
	{
		isGeo = (isGeo === undefined ? (options.geo != null) : isGeo);
		
		var map = (chart.map() || chartSupport.echartsMapChartMapOption(chart, options, isGeo));
		
		if(!map)
			throw new Error("[map] option must be set");
		
		chartSupport.echartsMapChartMapOption(chart, options, isGeo, map);
		
		if(chart.echartsMapRegistered(map))
		{
			chart.echartsInit(options);
			chart.statusRendered(true);
		}
		else
		{
			chart.echartsLoadMap(map, function()
			{
				chart.echartsInit(options);
				chart.statusRendered(true);
			});
		}
	};
	
	chartSupport.echartsMapChartUpdate = function(chart, results, updateOptions, renderOptions, isGeo)
	{
		isGeo = (isGeo === undefined ? (renderOptions.geo != null) : isGeo);
		
		var renderMap = chartSupport.echartsMapChartMapOption(chart, renderOptions, isGeo);
		var updateMap = chartSupport.echartsMapChartMapOption(chart, updateOptions, isGeo);
		var presetMap = chartFactory.extValueBuiltin(chart, "presetMap");
		
		if(!updateMap)
			updateMap = chart.map();
		
		if(!updateMap)
			updateMap = presetMap;
		
		updateOptions = chart.inflateUpdateOptions(results, updateOptions, function(updateOptions)
		{
			//inflateUpdateOptions()会将地图设置为renderMap，所以这里需要再次设置为updateMap
			if(updateMap && updateMap != renderMap)
			{
				chartSupport.echartsMapChartMapOption(chart, updateOptions, isGeo, updateMap);
				
				//要重置缩放比例和中心位置，不然会出现些地图无法显示的情况				
				if(isGeo)
				{
					updateOptions.geo.center = null;
					updateOptions.geo.zoom = 1;//此项非必须
				}
				else
				{
					updateOptions.series[0].center = null;
					updateOptions.series[0].zoom = 1;//此项非必须
				}
			}
		});
		
		var map = chartSupport.echartsMapChartMapOption(chart, updateOptions, isGeo);
		
		if(map)
			chartFactory.extValueBuiltin(chart, "presetMap", map);
		
		//更新地图未设置或者已注册
		if(!map || chart.echartsMapRegistered(map))
		{
			chart.echartsOptions(updateOptions);
			chart.statusUpdated(true);
		}
		else
		{
			chart.echartsLoadMap(map, function()
			{
				chart.echartsOptions(updateOptions);
				chart.statusUpdated(true);
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
	
	//---------------------------------------------------------
	//    公用函数结束
	//---------------------------------------------------------
})
(this);
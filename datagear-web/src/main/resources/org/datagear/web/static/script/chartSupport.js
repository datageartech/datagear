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
 *   chartFactory.js
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
			//扩展配置项，阶梯：true, false, "start", "middle", "end"
			dgStep: false,
			
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
				name: chart.dataSetPropertyLabel(chartDataSet, np),
				nameGap: 5,
				type: chartSupport.evalDataSetPropertyAxisType(chart, np),
				boundaryGap: false
			},
			yAxis: {
				name: (vps.length == 1 ? chart.dataSetPropertyLabel(chartDataSet, vps[0]) : ""),
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
			
			var dataSetName = chart.dataSetAlias(chartDataSet);
			var result = chart.resultOf(results, chartDataSet);
			
			var np = chart.dataSetPropertyOfSign(chartDataSet, signNameMap.name);
			var vps = chart.dataSetPropertiesOfSign(chartDataSet, signNameMap.value);
			
			for(var j=0; j<vps.length; j++)
			{
				var legendName = chartSupport.legendNameForMultipleSeries(chart, chartDataSets, chartDataSet, i, dataSetName, vps, j);
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
				if(renderOptions.dgStep != false)
					mySeries.step = renderOptions.dgStep;
				
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
		chart.eventOriginalInfo(chartEvent, echartsData);
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
				name: chart.dataSetPropertyLabel(chartDataSet, np),
				nameGap: 5,
				type: chartSupport.evalDataSetPropertyAxisType(chart, np),
				boundaryGap: true
			},
			yAxis:
			{
				name: (vps.length == 1 ? chart.dataSetPropertyLabel(chartDataSet, vps[0]) : ""),
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
			
			var dataSetName = chart.dataSetAlias(chartDataSet);
			var result = chart.resultOf(results, chartDataSet);
			
			var np = chart.dataSetPropertyOfSign(chartDataSet, signNameMap.name);
			var vps = chart.dataSetPropertiesOfSign(chartDataSet, signNameMap.value);
			
			for(var j=0; j<vps.length; j++)
			{
				var legendName = chartSupport.legendNameForMultipleSeries(chart, chartDataSets, chartDataSet, i, dataSetName, vps, j);
				
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
		chart.eventOriginalInfo(chartEvent, echartsData);
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
					name: chart.dataSetPropertyLabel(chartDataSet, np),
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
			
			var dataSetName = chart.dataSetAlias(chartDataSet);
			var result = chart.resultOf(results, chartDataSet);
			
			var np = chart.dataSetPropertyOfSign(chartDataSet, signNameMap.name);
			var vps = chart.dataSetPropertiesOfSign(chartDataSet, signNameMap.value);
			
			for(var j=0; j<vps.length; j++)
			{
				var legendName = chartSupport.legendNameForMultipleSeries(chart, chartDataSets, chartDataSet, i, dataSetName, vps, j);
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
		chart.eventOriginalInfo(chartEvent, echartsData);
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
			
			var dataSetName = chart.dataSetAlias(chartDataSet);
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
		chart.eventOriginalInfo(chartEvent, echartsData);
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
			
			var dataSetName = chart.dataSetAlias(chartDataSet);
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
					var vpn = chart.dataSetPropertyLabel(chartDataSet,vps[k]);
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
		chart.eventOriginalInfo(chartEvent, echartsData);
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
		chartSupport._scatterRender(chart, nameSign, valueSign, options, "scatter");
	};
	
	chartSupport.scatterUpdate = function(chart, results)
	{
		chartSupport._scatterUpdate(chart, results);
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
	
	chartSupport.scatterRippleRender = function(chart, nameSign, valueSign, options)
	{
		chartSupport._scatterRender(chart, nameSign, valueSign, options, "effectScatter");
	};
	
	chartSupport.scatterRippleUpdate = function(chart, results)
	{
		chartSupport._scatterUpdate(chart, results);
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
	
	chartSupport._scatterRender = function(chart, nameSign, valueSign, options, scatterType)
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
				name: chart.dataSetPropertyLabel(chartDataSet, np),
				nameGap: 5,
				type: chartSupport.evalDataSetPropertyAxisType(chart, np),
				boundaryGap: !chartSupport.isDataTypeNumber(np)
			},
			yAxis:
			{
				name: chart.dataSetPropertyLabel(chartDataSet, vp),
				nameGap: 5,
				type: "value"
			},
			series:
			[
				//将在update中设置：
				//{}
				//设初值以免渲染报错
				{
					type: scatterType
				}
			]
		},
		options);
		
		chartFactory.extValueBuiltin(chart, "scatterType", scatterType);
		
		chart.echartsInit(options);
	};
	
	chartSupport._scatterUpdate = function(chart, results)
	{
		var signNameMap = chartSupport.chartSignNameMap(chart);
		var renderOptions= chart.renderOptions();
		var scatterType = chartFactory.extValueBuiltin(chart, "scatterType");
		
		var chartDataSets = chart.chartDataSetsMain();
		
		var legendData = [];
		var series = [];
		
		var min = undefined, max = undefined;
		var symbolSizeMax = chartSupport.evalSymbolSizeMaxForScatter(chart, renderOptions, scatterType);
		var symbolSizeMin = chartSupport.evalSymbolSizeMinForScatter(chart, renderOptions, symbolSizeMax, scatterType);
		
		for(var i=0; i<chartDataSets.length; i++)
		{
			var chartDataSet = chartDataSets[i];
			
			var dataSetName = chart.dataSetAlias(chartDataSet);
			var result = chart.resultOf(results, chartDataSet);
			
			var np = chart.dataSetPropertyOfSign(chartDataSet, signNameMap.name);
			var vps = chart.dataSetPropertiesOfSign(chartDataSet, signNameMap.value);
			
			for(var j=0; j<vps.length; j++)
			{
				var legendName = chartSupport.legendNameForMultipleSeries(chart, chartDataSets, chartDataSet, i, dataSetName, vps, j);
				//使用{value: [name,value]}格式可以更好地兼容category、value、time坐标轴类型
				var data = chart.resultValueObjects(result, [np, vps[j]]);
				
				chart.originalInfo(data, chartDataSet);
				
				for(var k=0; k<data.length; k++)
				{
					var valMy = data[k].value[1];
					min = (min == null ? valMy : Math.min(min, valMy));
					max = (max == null ? valMy : Math.max(max, valMy));
				}
				
				var mySeries = { type: scatterType, name: legendName, data: data };
				
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
		var signNameMap = chartSupport.chartSignNameMap(chart);
		
		var echartsData = echartsEventParams.data;
		var data = chartSupport.extractNameValueStyleObj(echartsData, signNameMap.name, signNameMap.value);
		
		chart.eventData(chartEvent, data);
		chart.eventOriginalInfo(chartEvent, echartsData);
	};
	
	//坐标散点图
	
	chartSupport.scatterCoordRender = function(chart, nameSign, valueSign, weightSign, options)
	{
		chartSupport._scatterCoordRender(chart, nameSign, valueSign, weightSign, options, "scatter");
	};
	
	chartSupport.scatterCoordUpdate = function(chart, results)
	{
		chartSupport._scatterCoordUpdate(chart, results);
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
	
	chartSupport.scatterCoordRippleRender = function(chart, nameSign, valueSign, weightSign, options)
	{
		chartSupport._scatterCoordRender(chart, nameSign, valueSign, weightSign, options, "effectScatter");
	};
	
	chartSupport.scatterCoordRippleUpdate = function(chart, results)
	{
		chartSupport._scatterCoordUpdate(chart, results);
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
	
	chartSupport._scatterCoordRender = function(chart, nameSign, valueSign, weightSign, options, scatterType)
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
				name: chart.dataSetPropertyLabel(chartDataSet, np),
				nameGap: 5,
				type: chartSupport.evalDataSetPropertyAxisType(chart, np),
				boundaryGap: !chartSupport.isDataTypeNumber(np)
			},
			yAxis:
			{
				name: chart.dataSetPropertyLabel(chartDataSet, vp),
				nameGap: 5,
				type: "value"
			},
			series:
			[
				//将在update中设置：
				//{}
				//设初值以免渲染报错
				{
					type: scatterType
				}
			]
		},
		options);
		
		chartFactory.extValueBuiltin(chart, "scatterType", scatterType);
		
		chart.echartsInit(options);
	};
	
	chartSupport._scatterCoordUpdate = function(chart, results)
	{
		var signNameMap = chartSupport.chartSignNameMap(chart);
		var renderOptions= chart.renderOptions();
		var scatterType = chartFactory.extValueBuiltin(chart, "scatterType");
		
		var chartDataSets = chart.chartDataSetsMain();
		
		var legendData = [];
		var series = [];
		
		var min = undefined, max = undefined;
		var symbolSizeMax = chartSupport.evalSymbolSizeMaxForScatter(chart, renderOptions, scatterType);
		var symbolSizeMin = chartSupport.evalSymbolSizeMinForScatter(chart, renderOptions, symbolSizeMax, scatterType);
		
		for(var i=0; i<chartDataSets.length; i++)
		{
			var chartDataSet = chartDataSets[i];
			
			var dataSetName = chart.dataSetAlias(chartDataSet);
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
					min = (min == null ? wv : Math.min(min, wv));
					max = (max == null ? wv : Math.max(max, wv));
				}
			}
			
			chart.originalInfo(data, chartDataSet);
			
			series.push({ type: scatterType, name: dataSetName, data: data });
			legendData.push(dataSetName);
		}
		
		chartSupport.evalSeriesDataValueSymbolSize(series, min, max, symbolSizeMax, symbolSizeMin, "value", 2);
		
		var options = { legend: {data: legendData}, series: series };
		//需要明确重置轴坐标值，不然图表刷新有数据变化时，轴坐标不能自动更新
		options.xAxis = {data: null};
		
		options = chart.inflateUpdateOptions(results, options);
		
		chart.echartsOptions(options);
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
		var signNameMap = chartSupport.chartSignNameMap(chart);
		
		var echartsData = echartsEventParams.data;
		var data = {};
		
		data[signNameMap.name] = echartsData.value[0];
		data[signNameMap.value] = echartsData.value[1];
		if(echartsData.value.length > 2)
			data[signNameMap.weight] = echartsData.value[2];
		
		chart.eventData(chartEvent, data);
		chart.eventOriginalInfo(chartEvent, echartsData);
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
			var name = chart.dataSetPropertyLabel(chartDataSet, vp[i]);
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
		chart.eventOriginalInfo(chartEvent, echartsData);
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
			
			var dataSetName = chart.dataSetAlias(chartDataSet);
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
		chart.eventOriginalInfo(chartEvent, echartsData);
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
			
			var dataSetName = chart.dataSetAlias(chartDataSet);
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
					min = (min == null ? val : Math.min(min, val));
					max = (max == null ? val : Math.max(max, val));
				}
			}
		}
		
		var options =
		{
			visualMap: {min, min, max: max},
			series: [ {type: "map", name: seriesName, data: seriesData } ]
		};
		
		chartSupport.trimNumberRange(options.visualMap);
		
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
		chart.eventOriginalInfo(chartEvent, echartsData);
	};
	
	//散点值地图
	
	chartSupport.mapScatterRender = function(chart, nameSign, longitudeSign, latitudeSign, valueSign, mapSign, options)
	{
		chartSupport._mapScatterRender(chart, nameSign, longitudeSign, latitudeSign, 
										valueSign, mapSign, options, "scatter");
	};
	
	chartSupport.mapScatterUpdate = function(chart, results)
	{
		chartSupport._mapScatterUpdate(chart, results);
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
	
	chartSupport.mapScatterRippleRender = function(chart, nameSign, longitudeSign, latitudeSign, valueSign, mapSign, options)
	{
		chartSupport._mapScatterRender(chart, nameSign, longitudeSign, latitudeSign, 
										valueSign, mapSign, options, "effectScatter");
	};
	
	chartSupport.mapScatterRippleUpdate = function(chart, results)
	{
		chartSupport._mapScatterUpdate(chart, results);
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
	
	chartSupport._mapScatterRender = function(chart, nameSign, longitudeSign, latitudeSign, valueSign, mapSign,
												options, scatterType)
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
					type: scatterType,
					coordinateSystem: "geo"
				}
			]
		},
		options);
		
		chartFactory.extValueBuiltin(chart, "scatterType", scatterType);
		
		chartSupport.echartsMapChartInit(chart, options);
	};
	
	chartSupport._mapScatterUpdate = function(chart, results)
	{
		var signNameMap = chartSupport.chartSignNameMap(chart);
		var renderOptions= chart.renderOptions();
		var scatterType = chartFactory.extValueBuiltin(chart, "scatterType");
		
		var chartDataSets = chart.chartDataSetsMain();
		
		var legendData = [];
		var series = [];
		var map = undefined;
		
		var min = undefined, max = undefined;
		var symbolSizeMax = chartSupport.evalSymbolSizeMaxForScatter(chart, renderOptions, scatterType);
		var symbolSizeMin = chartSupport.evalSymbolSizeMinForScatter(chart, renderOptions, symbolSizeMax, scatterType);
		
		for(var i=0; i<chartDataSets.length; i++)
		{
			var chartDataSet = chartDataSets[i];
			
			var dataSetName = chart.dataSetAlias(chartDataSet);
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
					
					min = (min == null ? dv[2] : Math.min(min, dv[2]));
					max = (max == null ? dv[2] : Math.max(max, dv[2]));
				}
			}
			
			legendData.push(dataSetName);
			series.push({ type: scatterType, name: dataSetName, data: data, coordinateSystem: "geo" });
		}
		
		chartSupport.evalSeriesDataValueSymbolSize(series, min, max, symbolSizeMax, symbolSizeMin, "value", 2);
		
		var options = { legend: {data: legendData}, series: series };
		
		if(map)
			options.geo = { map: map };
		
		chartSupport.echartsMapChartUpdate(chart, results, options, renderOptions);
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
		var signNameMap = chartSupport.chartSignNameMap(chart);
		
		var echartsData = echartsEventParams.data;
		
		var data = {};
		
		data[signNameMap.name] = echartsData.name;
		data[signNameMap.longitude] = echartsData.value[0];
		data[signNameMap.latitude] = echartsData.value[1];
		if(echartsData.value.length > 2)
			data[signNameMap.value] = echartsData.value[2];
		
		chart.eventData(chartEvent, data);
		chart.eventOriginalInfo(chartEvent, echartsData);
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
				seriesName = chart.dataSetAlias(chartDataSet);
			
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
			chart.eventOriginalInfo(chartEvent, echartsData);
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
			chart.eventOriginalInfo(chartEvent, echartsData);
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
				
				name: chart.dataSetPropertyLabel(chartDataSet, np),
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
			
			var dataSetName = chart.dataSetAlias(chartDataSet);
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
		chart.eventOriginalInfo(chartEvent, echartsData);
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
				
				name: chart.dataSetPropertyLabel(chartDataSet, np),
				nameGap: 5,
				type: chartSupport.evalDataSetPropertyAxisType(chart, np),
				splitArea: { show: true }
			},
			yAxis:
			{
				//将在update中设置：
				//data
				
				name: chart.dataSetPropertyLabel(chartDataSet, vp),
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
				
				min = (min == null ? dw : Math.min(min, dw));
				max = (max == null ? dw : Math.max(max, dw));
			}
			
			chart.originalInfo(data, chartDataSet);
			
			seriesData = seriesData.concat(data);
			
			if(!seriesName)
				seriesName = chart.dataSetAlias(chartDataSet);
		}
		
		var series = [ { type: "heatmap", name: seriesName, data: seriesData } ];
		
		var options = { xAxis: { data: xAxisData }, yAxis: { data: yAxisData }, visualMap: {min: min, max: max}, series: series };
		chartSupport.trimNumberRange(options.visualMap);
		
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
		chart.eventOriginalInfo(chartEvent, echartsData);
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
		chart.eventOriginalInfo(chartEvent, echartsData);
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
		chart.eventOriginalInfo(chartEvent, echartsData);
		
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
		chart.eventOriginalInfo(chartEvent, echartsData);
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
				seriesName = chart.dataSetAlias(chartDataSet);
			
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
				seriesName = chart.dataSetAlias(chartDataSet);
			
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
			chart.eventOriginalInfo(chartEvent, echartsData);
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
			chart.eventOriginalInfo(chartEvent, echartsData);
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
				seriesName = chart.dataSetAlias(chartDataSet);
			
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
		
		if(min == null && max == null && symbolSizeMin < 10)
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
			chart.eventOriginalInfo(chartEvent, echartsData);
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
			chart.eventOriginalInfo(chartEvent, echartsData);
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
				name: chart.dataSetPropertyLabel(chartDataSet, np),
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
			
			var dataSetName = chart.dataSetAlias(chartDataSet);
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
					var legendName = chartSupport.legendNameForMultipleSeries(chart, chartDataSets, chartDataSet, i, dataSetName, vps, j);
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
		chart.eventOriginalInfo(chartEvent, echartsData);
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
							"shadowColor" : chart.gradualColor(0.9, chartTheme)
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
				min = (min == null ? data[j].value : Math.min(min, data[j].value));
				max = (max == null ? data[j].value : Math.max(max, data[j].value));
				
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
		chart.eventOriginalInfo(chartEvent, echartsData);
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
							shadowColor: chart.gradualColor(0.4, chartTheme)
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
						var sv = { name: chart.dataSetPropertyLabel(chartDataSet, vps[k]), value: ra[k] };
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
		chart.eventOriginalInfo(chartEvent, echartsData);
	};
	
	//平行坐标系
	
	chartSupport.parallelRender = function(chart, nameSign, valueSign, categorySign, options)
	{
		//name 可选，一条平行线名称
		//value 必选，可多选，平行线指标
		//category 可选，平行线类别
		chartSupport.chartSignNameMap(chart, { name: nameSign, value: valueSign, category: categorySign });
		
		options = chartSupport.inflateRenderOptions(chart,
		{
			//扩展配置项：是否平滑
			dgSmooth: false,
			
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
					type: "parallel"
				}
			]
		},
		options);
		
		chart.echartsInit(options);
	};
	
	chartSupport.parallelUpdate = function(chart, results)
	{
		var signNameMap = chartSupport.chartSignNameMap(chart);
		var renderOptions= chart.renderOptions();
		var parallelAxis = chartSupport.parallelEvalParallelAxis(chart);
		var valuePropertyNamess = chartSupport.parallelEvalValuePropertyNamess(chart, parallelAxis);
		
		var chartDataSets = chart.chartDataSetsMain();
		var categoryNames = [];
		var categoryDatasMap = {};
		
		for(var i=0; i<chartDataSets.length; i++)
		{
			var chartDataSet = chartDataSets[i];
			var dataSetName = chart.dataSetAlias(chartDataSet);
			var result = chart.resultOf(results, chartDataSet);
			
			var np = chart.dataSetPropertyOfSign(chartDataSet, signNameMap.name);
			var vps = valuePropertyNamess[i];
			var cp = chart.dataSetPropertyOfSign(chartDataSet, signNameMap.category);
			
			var data = [];
			
			if(np)
				data = chart.resultNameValueObjects(result, np, vps);
			else
				data = chart.resultValueObjects(result, vps);
			
			chart.originalInfo(data, chartDataSet);
			
			if(cp)
			{
				var cpv = chart.resultColumnArrays(result, cp);
				
				for(var j=0; j<cpv.length; j++)
				{
					var categoryName = cpv[j];
					var dataRow = data[j];
					dataRow["_dgParallelCategory"] = categoryName;
					
					chartSupport.appendDistinct(categoryNames, categoryName);
					
					var categoryDatas = (categoryDatasMap[categoryName] || (categoryDatasMap[categoryName] = []));
					chartSupport.appendElement(categoryDatas, dataRow);
				}
			}
			else
			{
				var categoryName = dataSetName;
				chartSupport.appendDistinct(categoryNames, categoryName);
				
				var categoryDatas = (categoryDatasMap[categoryName] || (categoryDatasMap[categoryName] = []));
				chartSupport.appendElement(categoryDatas, data);
			}
			
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
				type: "parallel",
				name: categoryNames[i],
				data: categoryDatasMap[categoryNames[i]]
			};
			
			if(renderOptions.dgSmooth)
				series[i].smooth = true;
		}
		
		var options = { legend: {data: categoryNames}, parallelAxis: parallelAxis, series: series };
		
		chartSupport.parallelTrimAxisMinMax(options);
		
		options = chart.inflateUpdateOptions(results, options);
		
		chart.echartsOptions(options);
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
		var signNameMap = chartSupport.chartSignNameMap(chart);
		
		var echartsData = echartsEventParams.data;
		
		var data = undefined;
		
		if(echartsData)
		{
			data = {};
			data[signNameMap.name] = echartsData.name;
			data[signNameMap.value] = echartsData.value;
			data[signNameMap.category] = echartsData["_dgParallelCategory"];
		}
		
		chart.eventData(chartEvent, data);
		chart.eventOriginalInfo(chartEvent, echartsData);
	};
	
	chartSupport.parallelEvalParallelAxis = function(chart)
	{
		var parallelAxis = [];
		
		var signNameMap = chartSupport.chartSignNameMap(chart);
		var chartDataSets = chart.chartDataSetsMain();
		
		for(var i=0; i<chartDataSets.length; i++)
		{
			var chartDataSet = chartDataSets[i];
			var vps = chart.dataSetPropertiesOfSign(chartDataSet, signNameMap.value);
			
			for(var j=0; j<vps.length; j++)
			{
				//使用label而非vps[j].name作为坐标轴名，因为label是可编辑得，使得用户可以自定义坐标轴
				var axisName = chart.dataSetPropertyLabel(chartDataSet, vps[j]);
				
				if(chartSupport.findInArray(parallelAxis, axisName, "name") < 0)
				{
					parallelAxis.push(
					{
						dim: parallelAxis.length,
						name: axisName,
						type: chartSupport.evalDataSetPropertyAxisType(chart, vps[j])
					});
				}
			}
		}
		
		return parallelAxis;
	};
	
	chartSupport.parallelEvalValuePropertyNamess = function(chart, parallelAxis)
	{
		var valuePropertyNamess = [];
		
		var placeholderName = chartFactory.builtinPropName("DataPropNamePlaceholder");
		var signNameMap = chartSupport.chartSignNameMap(chart);
		var chartDataSets = chart.chartDataSetsMain();
		
		for(var i=0; i<chartDataSets.length; i++)
		{
			var chartDataSet = chartDataSets[i];
			var valuePropertyNames = [];
			
			var vps = chart.dataSetPropertiesOfSign(chartDataSet, signNameMap.value);
			
			for(var j=0; j<parallelAxis.length; j++)
			{
				var idx = chartSupport.findInArray(vps, parallelAxis[j].name,
							function(vp)
							{
								return chart.dataSetPropertyLabel(chartDataSet, vp);
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
	
	//表格
	
	chartSupport.tableRender = function(chart, columnSign, options)
	{
		chartSupport.chartSignNameMap(chart, { column: columnSign });
		
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
		
		var columns = [];
		
		var chartDataSet = chart.chartDataSetFirst();
		var cps = chartSupport.tableGetColumnProperties(chart, chartDataSet, columnSign);
		for(var i=0; i<cps.length; i++)
		{
			var column =
			{
				title: chart.dataSetPropertyLabel(chartDataSet, cps[i]),
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
			delete titleStyle.name;
			
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
			if(column.data == "" && column.defaultContent == null)
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
		
		var evalHeight = (options.scrollY == null);
		
		//临时设一个较小值，后面会重新计算
		if(evalHeight)
			options.scrollY = 4;
		
		chartSupport.tableThemeStyleSheet(chart, options);
		
		if(options.carousel.enable)
			chartEle.addClass("dg-chart-table-carousel");
		
		if(!options.title || !options.title.show)
			chartEle.addClass("dg-hide-title");
		
		var chartTitle = $("<div class='dg-chart-table-title' />").html(options.title.text).appendTo(chartEle);
		if(options.titleStyle)
			chart.elementStyle(chartTitle, options.titleStyle);
		
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
		chartSupport.tableAdjustColumn(chart.internal());
		
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
		chartSupport.tableAdjustColumn(dataTable);
	};
	
	chartSupport.tableDestroy = function(chart)
	{
		var chartEle = chart.elementJquery();
		
		chartSupport.tableStopCarousel(chart);
		chartEle.removeClass("dg-chart-table");
		chartEle.removeClass("dg-hide-title");
		chartEle.removeClass("dg-chart-table-carousel");
		chartEle.removeClass("dg-chart-beautify-scrollbar");
		chartEle.removeClass(chart.extValue(chartFactory.builtinPropName("TableChartLocalStyleName")));
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
			
			var chartDataSet = chart.chartDataSetFirst();
			var cps = chartSupport.tableGetColumnProperties(chart, chartDataSet, signNameMap.column);
			for(var i=0; i<cps.length; i++)
				columnData[i] = chartData[cps[i].name];
			
			data[signNameMap.column] = columnData;
		}
		
		chart.eventData(chartEvent, data);
		chart.eventOriginalInfo(chartEvent, chartData);
	};
	
	chartSupport.tableGetChartContent = function(chart)
	{
		//图表的数据透视表功能也采用的是DataTable组件，可能会与表格图表处在同一个图表div内，
		//因此，获取图表表格的DOM操作都应限定在".dg-chart-table-content"内
		
		return $(".dg-chart-table-content", chart.element());
	};
	
	chartSupport.tableGetColumnProperties = function(chart, chartDataSet, columnSign)
	{
		var cps = chart.dataSetPropertiesOfSign(chartDataSet, columnSign);
		if(!cps || cps.length == 0)
			cps =(chartDataSet && chartDataSet.dataSet ? (chartDataSet.dataSet.properties || []) : []);
		
		return cps;
	};
	
	chartSupport.tableThemeStyleSheet = function(chart, options)
	{
		var name = chartFactory.builtinPropName("TableChart");
		var isLocalStyle = (options.tableStyle != null);
		
		if(isLocalStyle)
		{
			name = chartFactory.nextElementId();
			chart.elementJquery().addClass(name);
			chart.extValue(chartFactory.builtinPropName("TableChartLocalStyleName"), name);
		}
		
		chart.themeStyleSheet(name, function()
		{
			var theme = chart.theme();
			
			//行应该使用实际背景色，因为backgroundColor可能是透明的，当使用它设置固定列时，
			//横向滚动时固定列无法遮挡其他滚动列
			var rowBgColor = theme.actualBackgroundColor;
			
			var tableStyle =
			{
				table: {},
				head:
				{
					row:
					{
						"color": theme.titleColor,
						"background-color": rowBgColor
					},
					cell: {}
				},
				body:
				{
					row:
					{
						"color": theme.color
					},
					rowOdd:
					{
						"background-color": chart.gradualColor(0)
					},
					rowEven:
					{
						"background-color": rowBgColor
					},
					rowHover:
					{
						"background-color": chart.gradualColor(0.2)
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
					cellSelected: {}
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
			var qualifierBsb = (isLocalStyle ? "." + name : "")
						+ ".dg-chart-beautify-scrollbar .dg-chart-table-content";
			
			var css=
			[
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
					name: qualifier + " table.dataTable.stripe tbody tr.odd",
					value: chart.styleString(tableStyle.body.rowOdd)
				},
				{
					name: qualifier + " table.dataTable.stripe tbody tr.odd td",
					value: chart.styleString(tableStyle.body.cellOdd)
				},
				{
					name: qualifier + " table.dataTable.stripe tbody tr.even",
					value: chart.styleString(tableStyle.body.rowEven)
				},
				{
					name: qualifier + " table.dataTable.stripe tbody tr.even td",
					value: chart.styleString(tableStyle.body.cellEven)
				},
				{
					name: qualifier + " table.dataTable.hover tbody tr:hover",
					value: chart.styleString(tableStyle.body.rowHover)
				},
				{
					name: qualifier + " table.dataTable.hover tbody tr:hover td",
					value: chart.styleString(tableStyle.body.cellHover)
				},
				{
					name:
					[
						qualifier + " table.dataTable tbody tr.selected",
						qualifier + " table.dataTable.stripe tbody tr.odd.selected",
						qualifier + " table.dataTable.stripe tbody tr.even.selected",
						qualifier + " table.dataTable.hover tbody tr:hover.selected"
					],
					value: chart.styleString(tableStyle.body.rowSelected)
				},
				{
					name:
					[
						qualifier + " table.dataTable tbody tr.selected td",
						qualifier + " table.dataTable.stripe tbody tr.odd.selected td",
						qualifier + " table.dataTable.stripe tbody tr.even.selected td",
						qualifier + " table.dataTable.hover tbody tr:hover.selected td"
					],
					value: chart.styleString(tableStyle.body.cellSelected)
				},
				{
					name: qualifier + " table.dataTable thead th.sorting div.DataTables_sort_wrapper span",
					value:
					{
						"background": headColor
					}
				},
				{
					name: qualifier + " table.dataTable thead th.sorting_asc div.DataTables_sort_wrapper span",
					value:
					{
						"border-bottom-color": headColor,
						"background": "none"
					}
				},
				{
					name: qualifier + " table.dataTable thead th.sorting_desc div.DataTables_sort_wrapper span",
					value:
					{
						"border-top-color": headColor,
						"background": "none"
					}
				},
				{
					name: qualifier + " .dataTables_wrapper .dataTables_length select",
					value:
					{
						color: theme.color
					}
				},
				{
					name: qualifier + " .dataTables_wrapper .dataTables_length select option",
					value:
					{
						color: theme.color,
						"background-color": chart.gradualColor(0)
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
						"color": theme.color,
						"background-color": theme.backgroundColor
					}
				});
			}
			
			return css;
		});
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
			//是否标签值在前
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
	
	chartSupport.labelUpdate = function(chart, results)
	{
		var signNameMap = chartSupport.chartSignNameMap(chart);
		var renderOptions = chart.renderOptions();
		var valueFirst = renderOptions.valueFirst;
		
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
			var hasNps = (nps && nps.length > 0);
			
			if(hasNps && nps.length != vps.length)
				throw new Error("The ["+signNameMap.name+"] sign column must be "
						+"one-to-one with ["+signNameMap.value+"] sign column");
			
			var namess = (hasNps ? chart.resultRowArrays(result, nps) : []);
			var valuess = chart.resultRowArrays(result, vps);
			
			var vpNames = [];
			if(!hasNps)
			{
				for(var j=0; j<vps.length; j++)
					vpNames[j] = chart.dataSetPropertyLabel(chartDataSet, vps[j]);
			}
			
			for(var j=0; j<valuess.length; j++)
			{
				var values = valuess[j];
				var names = (hasNps ? namess[j] : vpNames);
				
				for(var k=0; k<names.length; k++)
				{
					var sv = { name: names[k], value: values[k] };
					chart.originalInfo(sv, chartDataSet, j);
					
					updateOptions.data.push(sv);
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
		chart.eventOriginalInfo(chartEvent, chartData);
	};
	
	//下拉框
	
	chartSupport.selectRender = function(chart, nameSign, valueSign, options)
	{
		chartSupport.chartSignNameMap(chart, { name: nameSign, value: valueSign });
		
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
	
	chartSupport.selectUpdate = function(chart, results)
	{
		var signNameMap = chartSupport.chartSignNameMap(chart);
		var renderOptions = chart.renderOptions();
		
		var chartDataSets = chart.chartDataSetsMain();
		
		var $select = $(chart.internal());
		
		$select.empty();
		
		var selected = renderOptions.selected;
		
		if(selected != null && typeof(selected) == "number")
			selected = [ selected ];
		
		var updateOptions = { data: [] };
		
		for(var i=0; i<chartDataSets.length; i++)
		{
			var chartDataSet = chartDataSets[i];
			
			var result = chart.resultOf(results, chartDataSet);
			
			var nps = chart.dataSetPropertiesOfSign(chartDataSet, signNameMap.name);
			var vps = chart.dataSetPropertiesOfSign(chartDataSet, signNameMap.value);
			var hasNps = (nps && nps.length > 0);
			
			if(hasNps && nps.length != vps.length)
				throw new Error("The ["+signNameMap.name+"] sign column must be "
						+"one-to-one with ["+signNameMap.value+"] sign column");
			
			var namess = (hasNps ? chart.resultRowArrays(result, nps) : []);
			var valuess = chart.resultRowArrays(result, vps);
			
			for(var j=0; j<valuess.length; j++)
			{
				var values = valuess[j];
				var names = (hasNps ? namess[j] : values);
				
				for(var k=0; k<names.length; k++)
				{
					var sv = { name: names[k], value: values[k] };
					chart.originalInfo(sv, chartDataSet, j);
					
					updateOptions.data.push(sv);
				}
			}
		}
		
		updateOptions = chart.inflateUpdateOptions(results, updateOptions);
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
			var chartEvent = chart.eventNewHtml(eventType, htmlEvent);
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
		var signNameMap = chartSupport.chartSignNameMap(chart);
		var renderOptions = chart.renderOptions();
		
		var $selectedOptions = $("option:selected", $select);
		var chartData = [];
		var data = [];
		
		for(var i=0; i<$selectedOptions.length; i++)
		{
			chartData.push($($selectedOptions[i]).data("_dgChartSelectOptionChartData"));
			
			var datai = (data[i] = {});
			datai[signNameMap.name] = chartData[i].name;
			datai[signNameMap.value] = chartData[i].value;
		}
		
		//单选
		if(!renderOptions.multiple)
		{
			chartData = (chartData.length > 0 ? chartData[0] : null);
			data = (data.length > 0 ? data[0] : null);
		}
		
		chart.eventData(chartEvent, data);
		chart.eventOriginalInfo(chartEvent, chartData);
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
						"border-color": theme.borderColor
					}
				},
				{
					name: ".dg-chart-select-dropdown .dg-chart-select-select option",
					value:
					{
						"color": theme.color,
						"background-color": chart.gradualColor(0.1)
					}
				}
			];
			
			return css;
		});
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
					
					if(beforeProcessHandler)
						beforeProcessHandler(renderOptions, chart);
				};
			}
		}
		
		return chart.inflateRenderOptions(renderOptions, newBeforeProcessHandler);
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
	 * @param propertyName 当是对象类型时，用于指定判断重复的属性名
	 * @returns 追加的或重复元素的索引、或者索引数组
	 */
	chartSupport.appendDistinct = function(sourceArray, append, propertyName)
	{
		var isArray = $.isArray(append);
		
		if(!isArray)
			append = [ append ];
		
		var indexes = [];
		
		for(var i=0; i<append.length; i++)
		{
			var av = (propertyName != null && append[i] ? append[i][propertyName] : append[i]);
			var foundIdx = chartSupport.findInArray(sourceArray, av, propertyName);
			
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
	 * 在数组中查找元素，返回其索引
	 * 
	 * @param array
	 * @param value
	 * @param propertyName 当数组元素是对象类型时，用于指定判断属性名，格式为："..."、function(ele){ return ... }
	 * @returns 索引数值，-1 表示无
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
	
	chartSupport.chartSignNameMap = function(chart, signNameMap)
	{
		return chartFactory.extValueBuiltin(chart, "signNameMap", signNameMap);
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
	chartSupport.legendNameForMultipleSeries = function(chart, chartDataSets, chartDataSet, chartDataSetIdx, dataSetName,
					seriesProperties, seriesPropertyIdx)
	{
		var legendName = dataSetName;
		
		if(chartDataSets.length > 1 && seriesProperties.length > 1)
		{
			legendName = dataSetName +"-" + chart.dataSetPropertyLabel(chartDataSet, seriesProperties[seriesPropertyIdx]);
		}
		else if(seriesProperties.length > 1)
		{
			legendName = chart.dataSetPropertyLabel(chartDataSet, seriesProperties[seriesPropertyIdx]);
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
		var symbolSizeMax = (options ? options.dgSymbolSizeMax : undefined);
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
		var symbolSizeMin = (options ? options.dgSymbolSizeMin : undefined);
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
	
	//---------------------------------------------------------
	//    公用函数结束
	//---------------------------------------------------------
})
(this);
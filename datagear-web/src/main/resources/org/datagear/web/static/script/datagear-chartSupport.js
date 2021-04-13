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
	
	//@deprecated 兼容1.8.1版本的window.chartSupport变量名，未来版本会移除
	global.chartSupport = chartSupport;
	//@deprecated 兼容1.8.1版本的window.chartSupport变量名，未来版本会移除
	
	//折线图
	
	chartSupport.lineRender = function(chart, nameSign, valueSign, options)
	{
		chartSupport.chartSignNameMap(chart, { name: nameSign, value: valueSign });
		
		var chartDataSet = chart.chartDataSetFirst();
		var np = chart.dataSetPropertyOfSign(chartDataSet, nameSign);
		var vps = chart.dataSetPropertiesOfSign(chartDataSet, valueSign);
		var stack = (options && options.stack);//是否堆叠
		
		options = $.extend(true,
		{
			title: {
		        text: chart.name
		    },
			tooltip:
			{
				trigger: "axis"
			},
			legend:
			{
				data: []
			},
			xAxis: {
				name: chart.dataSetPropertyLabel(np),
				nameGap: 5,
				type: (chartSupport.isDataTypeNumber(np) ? "value" : "category"),
				boundaryGap: false
			},
			yAxis: {
				name: (vps.length == 1 ? chart.dataSetPropertyLabel(vps[0]) : ""),
				nameGap: 5,
				type: "value"
			},
			series: [{
				name: "",
				type: "line",
				data: []
			}]
		},
		options,
		chart.options());
		
		options = chartSupport.processRenderOptions(chart, options);
		
		chart.echartsInit(options);
	};
	
	chartSupport.lineUpdate = function(chart, results)
	{
		var signNameMap = chartSupport.chartSignNameMap(chart);
		var renderOptions= chartSupport.renderOptions(chart);
		var stack = (renderOptions && renderOptions.stack);//是否堆叠
		var isCategory = (renderOptions.xAxis.type == "category");
		
		var chartDataSets = chart.chartDataSetsMain();
		
		var legendData = [];
		var xAxisData = [];
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
				//当np是数值类型时，采用{name:..., value:...}格式的数据会导致折线图不对，所以这里采用[name,value]格式
				var data = chart.resultRowArrays(result, [ np, vps[j] ]);
				var dataNew = [];
				
				for(var k=0; k<data.length; k++)
				{
					dataNew[k] = { value: data[k] };
				}
				
				data = dataNew;
				
				chartSupport.chartDataOriginalDataIndex(data, chartDataSet);
				
				var mySeries = chartSupport.optionsSeries(renderOptions, i*vps.length+j, {name: legendName, data: data});
				
				//折线图按数据集分组展示没有效果，所以都使用同一个堆叠
				if(stack)
					mySeries.stack = "stack";
				
				legendData.push(legendName);
				series.push(mySeries);
				
				//类目轴需要设置data，不然图表刷新数据有变化时，类目轴坐标不能自动更新
				if(isCategory)
				{
					if(xAxisData.length == 0)
						xAxisData = chart.resultRowArrays(result, np);
					else
					{
						var xAxisDataMy = chart.resultRowArrays(result, np);
						chartSupport.appendDistinct(xAxisData, xAxisDataMy);
					}
				}
			}
		}
		
		var options = { legend: {data: legendData}, series: series };
		if(isCategory)
			options.xAxis = {data: xAxisData};
		
		options = chartSupport.processUpdateOptions(chart, results, renderOptions, options);
		
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
		chartSupport.bindChartEventHandlerDelegationEcharts(chart, eventType, handler,
				chartSupport.lineSetChartEventData);
	};
	
	chartSupport.lineOff = function(chart, eventType, handler)
	{
		chartSupport.unbindChartEventHandlerDelegationEcharts(chart, eventType, handler);
	};
	
	chartSupport.lineSetChartEventData = function(chart, chartEvent, echartsEventParams)
	{
		var signNameMap = chartSupport.chartSignNameMap(chart);
		
		var echartsData = echartsEventParams.data;
		var data = {};
		
		data[signNameMap.name] = echartsData.value[0];
		data[signNameMap.value] = echartsData.value[1];
		
		chart.eventData(chartEvent, data);
		chartSupport.setChartEventOriginalDataForChartData(chart, chartEvent, echartsData);
	};
	
	//柱状图
	
	chartSupport.barRender = function(chart, nameSign, valueSign, options)
	{
		chartSupport.chartSignNameMap(chart, { name: nameSign, value: valueSign });
		
		var chartDataSet = chart.chartDataSetFirst();
		var np = chart.dataSetPropertyOfSign(chartDataSet, nameSign);
		var vps = chart.dataSetPropertiesOfSign(chartDataSet, valueSign);
		var stack = (options && options.stack);//是否堆叠
		var horizontal = (options && options.horizontal);//是否横向
		
		options = $.extend(true,
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
				data: []
			},
			xAxis: {
				name: chart.dataSetPropertyLabel(np),
				nameGap: 5,
				type: "category",
				boundaryGap: true
			},
			yAxis: {
				name: (vps.length == 1 ? chart.dataSetPropertyLabel(vps[0]) : ""),
				nameGap: 5,
				type: "value"
			},
			series: [{
				name: "",
				type: "bar",
				label: { show: stack },
				data: []
			}]
		},
		options,
		chart.options());
		
		if(horizontal)
		{
			var xAxisTmp = options.xAxis;
			options.xAxis = options.yAxis;
			options.yAxis = xAxisTmp;
		}
		
		options = chartSupport.processRenderOptions(chart, options);
		
		chart.echartsInit(options);
	};
	
	chartSupport.barUpdate = function(chart, results)
	{
		var signNameMap = chartSupport.chartSignNameMap(chart);
		var renderOptions= chartSupport.renderOptions(chart);
		var stack = renderOptions.stack;//是否堆叠
		var horizontal = renderOptions.horizontal;//是否横向
		//是否按数据集分组堆叠
		var stackGroup = (renderOptions.stackGroup == undefined ? true : renderOptions.stackGroup);
		var isCategory = ((horizontal ? renderOptions.yAxis.type : renderOptions.xAxis.type) == "category");
		
		var chartDataSets = chart.chartDataSetsMain();
		
		var legendData = [];
		var axisData = [];
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
				//当np是数值类型时，采用[name,value]格式的数据会导致柱状图无法绘制，所以这里采用{name:..., value:...}格式
				var data = chart.resultNameValueObjects(result, np, vps[j]);
				
				chartSupport.chartDataOriginalDataIndex(data, chartDataSet);
				
				var mySeries = chartSupport.optionsSeries(renderOptions, i*vps.length+j, {name: legendName, data: data});
				
				if(stack)
					mySeries.stack = (stackGroup ? "stack-"+i : "stack");
				
				legendData.push(legendName);
				series.push(mySeries);
				
				//类目轴需要设置data，不然图表刷新数据有变化时，类目轴坐标不能自动更新
				if(isCategory)
				{
					if(axisData.length == 0)
						axisData = chart.resultRowArrays(result, np);
					else
					{
						var axisDataMy = chart.resultRowArrays(result, np);
						chartSupport.appendDistinct(axisData, axisDataMy);
					}
				}
			}
		}
		
		var options = { legend: {data: legendData}, series: series };
		if(isCategory)
		{
			if(horizontal)
				options.yAxis = {data: axisData};
			else
				options.xAxis = {data: axisData};
		}
		
		options = chartSupport.processUpdateOptions(chart, results, renderOptions, options);
		
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
		chartSupport.bindChartEventHandlerDelegationEcharts(chart, eventType, handler,
				chartSupport.barSetChartEventData);
	};
	
	chartSupport.barOff = function(chart, eventType, handler)
	{
		chartSupport.unbindChartEventHandlerDelegationEcharts(chart, eventType, handler);
	};
	
	chartSupport.barSetChartEventData = function(chart, chartEvent, echartsEventParams)
	{
		var signNameMap = chartSupport.chartSignNameMap(chart);
		
		var echartsData = echartsEventParams.data;
		var data = {};
		
		data[signNameMap.name] = echartsData.name;
		data[signNameMap.value] = echartsData.value;
		
		chart.eventData(chartEvent, data);
		chartSupport.setChartEventOriginalDataForChartData(chart, chartEvent, echartsData);
	};
	
	//极坐标柱状图
	
	chartSupport.barPolarRender = function(chart, nameSign, valueSign, options)
	{
		chartSupport.chartSignNameMap(chart, { name: nameSign, value: valueSign });
		
		var chartDataSet = chart.chartDataSetFirst();
		var np = chart.dataSetPropertyOfSign(chartDataSet, nameSign);
		//是否堆叠
		var stack = (options && options.dgStack);
		//坐标类型：radius（径向）、angle（角度）
		var axisType = (options && options.dgAxisType ? options.dgAxisType : "radius");
		
		var defaultOptions =
		{
			dgStack: stack,
			dgAxisType: axisType,
			
			title: { text: chart.name },
			angleAxis: {},
			radiusAxis: {},
			polar: { radius: "60%" },
			tooltip: { trigger: "item" },
			legend:{ data: [] },
			series:
			[{
				name: "",
				type: "bar",
				label: { show: stack },
				coordinateSystem: 'polar',
				data: []
			}]
		};
		
		if(axisType == "angle")
		{
			defaultOptions.angleAxis =
			{
		        type: 'category',
		        data: []
			};
		}
		else
		{
			defaultOptions.radiusAxis =
			{
				name: chart.dataSetPropertyLabel(np),
				nameGap: 20,
		        type: 'category',
		        data: [],
		        z: 10
			};
		}
		
		options = $.extend(true,
		defaultOptions,
		options,
		chart.options());
		
		options = chartSupport.processRenderOptions(chart, options);
		
		chart.echartsInit(options);
	};
	
	chartSupport.barPolarUpdate = function(chart, results)
	{
		var signNameMap = chartSupport.chartSignNameMap(chart);
		var renderOptions= chartSupport.renderOptions(chart);
		var stack = renderOptions.dgStack;
		var axisType = renderOptions.dgAxisType;
		//是否按数据集分组堆叠
		var stackGroup = renderOptions.stackGroup == undefined ? true : renderOptions.stackGroup;
		var isCategory = true;
		
		var chartDataSets = chart.chartDataSetsMain();
		
		var legendData = [];
		var axisData = [];
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
				var data = chart.resultNameValueObjects(result, np, vps[j]);
				
				chartSupport.chartDataOriginalDataIndex(data, chartDataSet);
				
				var mySeries = chartSupport.optionsSeries(renderOptions, i*vps.length+j, {name: legendName, data: data});
				
				if(stack)
					mySeries.stack = (stackGroup ? "stack-"+i : "stack");
				
				legendData.push(legendName);
				series.push(mySeries);
				
				//类目轴需要设置data，不然图表刷新数据有变化时，类目轴坐标不能自动更新
				if(isCategory)
				{
					if(axisData.length == 0)
						axisData = chart.resultRowArrays(result, np);
					else
					{
						var axisDataMy = chart.resultRowArrays(result, np);
						chartSupport.appendDistinct(axisData, axisDataMy);
					}
				}
			}
		}
		
		var options = { legend: {data: legendData}, series: series };
		if(axisType == "angle")
			options.angleAxis = {data: axisData};
		else
			options.radiusAxis = {data: axisData};
		
		options = chartSupport.processUpdateOptions(chart, results, renderOptions, options);
		
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
		chartSupport.bindChartEventHandlerDelegationEcharts(chart, eventType, handler,
				chartSupport.barPolarSetChartEventData);
	};
	
	chartSupport.barPolarOff = function(chart, eventType, handler)
	{
		chartSupport.unbindChartEventHandlerDelegationEcharts(chart, eventType, handler);
	};
	
	chartSupport.barPolarSetChartEventData = function(chart, chartEvent, echartsEventParams)
	{
		var signNameMap = chartSupport.chartSignNameMap(chart);
		
		var echartsData = echartsEventParams.data;
		var data = {};
		
		data[signNameMap.name] = echartsData.name;
		data[signNameMap.value] = echartsData.value;
		
		chart.eventData(chartEvent, data);
		chartSupport.setChartEventOriginalDataForChartData(chart, chartEvent, echartsData);
	};
	
	//饼图
	
	chartSupport.pieRender = function(chart, nameSign, valueSign, options)
	{
		chartSupport.chartSignNameMap(chart, { name: nameSign, value: valueSign });
		
		options = $.extend(true,
		{
			title: {
		        text: chart.name
		    },
			tooltip:
			{
				trigger: "item",
				formatter: "{a} <br/>{b}: {c} ({d}%)"
			},
			legend:
			{
				data: []
			},
			series:
			[
				{
					name: chart.name,
					type: "pie",
					radius: "55%",
					center: ["50%", "60%"],
					data: []
				}
			]
		},
		options,
		chart.options());
		
		options = chartSupport.processRenderOptions(chart, options);
		
		chart.echartsInit(options);
	};
	
	chartSupport.pieUpdate = function(chart, results)
	{
		var signNameMap = chartSupport.chartSignNameMap(chart);
		var renderOptions= chartSupport.renderOptions(chart);
		
		var chartDataSets = chart.chartDataSetsMain();
		
		var legendData = [];
		var seriesName = "";
		var seriesData = [];
		
		for(var i=0; i<chartDataSets.length; i++)
		{
			var chartDataSet = chartDataSets[i];
			
			var dataSetName = chart.chartDataSetName(chartDataSet);
			var result = chart.resultOf(results, chartDataSet);
			
			var np = chart.dataSetPropertyOfSign(chartDataSet, signNameMap.name);
			var npv = chart.resultColumnArrays(result, np);
			var vp = chart.dataSetPropertyOfSign(chartDataSet, signNameMap.value);
			var data = chart.resultNameValueObjects(result, np, vp);
			
			chartSupport.chartDataOriginalDataIndex(data, chartDataSet);
			
			legendData = legendData.concat(npv);
			if(!seriesName)
				seriesName = dataSetName;
			seriesData = seriesData.concat(data);
		}
		
		var series = [ chartSupport.optionsSeries(renderOptions, 0, {name: seriesName, data: seriesData}) ];
		
		var options = { legend: { data: legendData }, series: series };
		
		options = chartSupport.processUpdateOptions(chart, results, renderOptions, options);
		
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
		chartSupport.bindChartEventHandlerDelegationEcharts(chart, eventType, handler,
				chartSupport.pieSetChartEventData);
	};
	
	chartSupport.pieOff = function(chart, eventType, handler)
	{
		chartSupport.unbindChartEventHandlerDelegationEcharts(chart, eventType, handler);
	};
	
	chartSupport.pieSetChartEventData = function(chart, chartEvent, echartsEventParams)
	{
		var signNameMap = chartSupport.chartSignNameMap(chart);
		
		var echartsData = echartsEventParams.data;
		var data = {};
		
		data[signNameMap.name] = echartsData.name;
		data[signNameMap.value] = echartsData.value;
		
		chart.eventData(chartEvent, data);
		chartSupport.setChartEventOriginalDataForChartData(chart, chartEvent, echartsData);
	};
	
	//仪表盘
	
	chartSupport.gaugeRender = function(chart, valueSign, minSign, maxSign, options)
	{
		chartSupport.chartSignNameMap(chart, { value: valueSign, min: minSign, max: maxSign });
		
		options = $.extend(true,
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
				{
					name: "",
					type: "gauge",
					detail: {formatter: "{value}"},
					data: [{value: 0, name: ''}]
				}
			]
		},
		options,
		chart.options());
		
		options = chartSupport.processRenderOptions(chart, options);
		
		chart.echartsInit(options);
	};
	
	chartSupport.gaugeUpdate = function(chart, results)
	{
		var signNameMap = chartSupport.chartSignNameMap(chart);
		var renderOptions= chartSupport.renderOptions(chart);
		var chartDataSet = chart.chartDataSetFirst();
		var result = chart.resultOf(results, chartDataSet);
		
		var seriesName = chart.chartDataSetName(chartDataSet);
		
		var minp = chart.dataSetPropertyOfSign(chartDataSet, signNameMap.min);
		var maxp = chart.dataSetPropertyOfSign(chartDataSet, signNameMap.max);
		var vp = chart.dataSetPropertyOfSign(chartDataSet, signNameMap.value);
		
		var min = (chart.resultCell(result, minp) || 0);
		var max = (chart.resultCell(result, maxp) || 100);
		var value = (chart.resultCell(result, vp) || 0);
		
		var data = [ { name: chart.dataSetPropertyLabel(vp), value: value, min: min, max: max } ];
		
		chartSupport.chartDataOriginalDataIndex(data, chartDataSet);
		
		var options = { series : [ chartSupport.optionsSeries(renderOptions, 0, { name: seriesName, min: min, max: max, data: data }) ] };
		
		options = chartSupport.processUpdateOptions(chart, results, renderOptions, options);
		
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
		chartSupport.bindChartEventHandlerDelegationEcharts(chart, eventType, handler,
				chartSupport.gaugeSetChartEventData);
	};
	
	chartSupport.gaugeOff = function(chart, eventType, handler)
	{
		chartSupport.unbindChartEventHandlerDelegationEcharts(chart, eventType, handler);
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
		chartSupport.setChartEventOriginalDataForChartData(chart, chartEvent, echartsData);
	};
	
	//散点图
	
	chartSupport.scatterRender = function(chart, nameSign, valueSign, options)
	{
		chartSupport.chartSignNameMap(chart, { name: nameSign, value: valueSign });
		
		var chartDataSet = chart.chartDataSetFirst();
		var np = chart.dataSetPropertyOfSign(chartDataSet, nameSign);
		var vp = chart.dataSetPropertyOfSign(chartDataSet, valueSign);
		
		options = $.extend(true,
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
				data: []
			},
			xAxis: {
				name: chart.dataSetPropertyLabel(np),
				nameGap: 5,
				type: (chartSupport.isDataTypeNumber(np) ? "value" : "category"),
				boundaryGap: !chartSupport.isDataTypeNumber(np)
			},
			yAxis: {
				name: chart.dataSetPropertyLabel(vp),
				nameGap: 5,
				type: "value"
			},
			//最大数据标记像素数
			symbolSizeMax: undefined,
			//最小数据标记像素数
			symbolSizeMin: undefined,
			series: [{
				name: "",
				type: "scatter",
				data: []
			}]
		},
		options,
		chart.options());
		
		options = chartSupport.processRenderOptions(chart, options);
		
		chart.echartsInit(options);
	};
	
	chartSupport.scatterUpdate = function(chart, results)
	{
		var signNameMap = chartSupport.chartSignNameMap(chart);
		var renderOptions= chartSupport.renderOptions(chart);
		
		var chartDataSets = chart.chartDataSetsMain();
		
		var isCategory = (renderOptions.xAxis.type == "category");
		
		var legendData = [];
		var xAxisData = [];
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
				var data = chart.resultNameValueObjects(result, np, vps[j]);
				
				chartSupport.chartDataOriginalDataIndex(data, chartDataSet);
				
				for(var k=0; k<data.length; k++)
				{
					min = (min == undefined ? data[k].value : Math.min(min, data[k].value));
					max = (max == undefined ? data[k].value : Math.max(max, data[k].value));
				}
				
				var mySeries = chartSupport.optionsSeries(renderOptions, i*vps.length+j, { name: legendName, data: data });
				
				legendData.push(legendName);
				series.push(mySeries);
				
				//类目轴需要设置data，不然图表刷新数据有变化时，类目轴坐标不能自动更新
				if(isCategory)
				{
					if(xAxisData.length == 0)
						xAxisData = chart.resultRowArrays(result, np);
					else
					{
						var xAxisDataMy = chart.resultRowArrays(result, np);
						chartSupport.appendDistinct(xAxisData, xAxisDataMy);
					}
				}
			}
		}
		
		if(min != null && max != null && max <= min)
			max = min + 1;
		
		chartSupport.evalSeriesDataValueSymbolSize(series, min, max, symbolSizeMax, symbolSizeMin);
		
		var options = { legend: {data: legendData}, series: series };
		if(isCategory)
			options.xAxis = {data: xAxisData};
		
		options = chartSupport.processUpdateOptions(chart, results, renderOptions, options);
		
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
		chartSupport.bindChartEventHandlerDelegationEcharts(chart, eventType, handler,
				chartSupport.scatterSetChartEventData);
	};
	
	chartSupport.scatterOff = function(chart, eventType, handler)
	{
		chartSupport.unbindChartEventHandlerDelegationEcharts(chart, eventType, handler);
	};
	
	chartSupport.scatterSetChartEventData = function(chart, chartEvent, echartsEventParams)
	{
		var signNameMap = chartSupport.chartSignNameMap(chart);
		
		var echartsData = echartsEventParams.data;
		var data = {};
		
		data[signNameMap.name] = echartsData.name;
		data[signNameMap.value] = echartsData.value;
		
		chart.eventData(chartEvent, data);
		chartSupport.setChartEventOriginalDataForChartData(chart, chartEvent, echartsData);
	};
	
	//坐标散点图
	
	chartSupport.scatterCoordRender = function(chart, nameSign, valueSign, weightSign, options)
	{
		chartSupport.chartSignNameMap(chart, { name: nameSign, value: valueSign, weight: weightSign });
		
		var chartDataSet = chart.chartDataSetFirst();
		var np = chart.dataSetPropertyOfSign(chartDataSet, nameSign);
		var vp = chart.dataSetPropertyOfSign(chartDataSet, valueSign);
		
		options = $.extend(true,
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
				data: []
			},
			xAxis: {
				name: chart.dataSetPropertyLabel(np),
				nameGap: 5,
				type: (chartSupport.isDataTypeNumber(np) ? "value" : "category"),
				boundaryGap: !chartSupport.isDataTypeNumber(np)
			},
			yAxis: {
				name: chart.dataSetPropertyLabel(vp),
				nameGap: 5,
				type: "value"
			},
			//最大数据标记像素数
			symbolSizeMax: undefined,
			//最小数据标记像素数
			symbolSizeMin: undefined,
			series: [{
				name: "",
				type: "scatter",
				data: []
			}]
		},
		options,
		chart.options());
		
		options = chartSupport.processRenderOptions(chart, options);
		
		chart.echartsInit(options);
	};
	
	chartSupport.scatterCoordUpdate = function(chart, results)
	{
		var signNameMap = chartSupport.chartSignNameMap(chart);
		var renderOptions= chartSupport.renderOptions(chart);
		
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
			
			var data = (wp ? chart.resultRowArrays(result, [np, vp, wp]) : chart.resultRowArrays(result, [np, vp]));
			var dataNew = [];
			
			for(var j=0; j<data.length; j++)
			{
				var dataEle = { value: data[j] };
				
				if(wp)
				{
					min = (min == undefined ? data[j][2] : Math.min(min, data[j][2]));
					max = (max == undefined ? data[j][2] : Math.max(max, data[j][2]));
				}
				
				dataNew[j] = dataEle;
			}
			
			data = dataNew;
			
			chartSupport.chartDataOriginalDataIndex(data, chartDataSet);
			
			var mySeries = chartSupport.optionsSeries(renderOptions, i, { name: dataSetName, data: data });
			legendData.push(dataSetName);
			series.push(mySeries);
		}
		
		if(min != null && max != null && max <= min)
			max = min + 1;
		
		chartSupport.evalSeriesDataValueSymbolSize(series, min, max, symbolSizeMax, symbolSizeMin, "value", 2);
		
		var options = { legend: {data: legendData}, series: series };
		
		options = chartSupport.processUpdateOptions(chart, results, renderOptions, options);
		
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
		chartSupport.bindChartEventHandlerDelegationEcharts(chart, eventType, handler,
				chartSupport.scatterCoordSetChartEventData);
	};
	
	chartSupport.scatterCoordOff = function(chart, eventType, handler)
	{
		chartSupport.unbindChartEventHandlerDelegationEcharts(chart, eventType, handler);
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
		chartSupport.setChartEventOriginalDataForChartData(chart, chartEvent, echartsData);
	};
	
	//雷达图
	
	chartSupport.radarRender = function(chart, itemSign, nameSign, valueSign, maxSign, options)
	{
		chartSupport.chartSignNameMap(chart, { item: itemSign, name: nameSign, value: valueSign, max: maxSign });
		
		options = $.extend(true,
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
				data: []
			},
			radar:
			{
				center: ["50%", "60%"],
				radius: "70%",
				nameGap: 6,
				indicator: []
			},
			series: [{
				name: "",
				type: "radar",
				data: []
			}]
		},
		options,
		chart.options());
		
		options = chartSupport.processRenderOptions(chart, options);
		
		chart.echartsInit(options);
	};
	
	chartSupport.radarUpdate = function(chart, results)
	{
		var signNameMap = chartSupport.chartSignNameMap(chart);
		var renderOptions= chartSupport.renderOptions(chart);
		
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
		
		var series = [ { data: seriesData } ];
		var options = { legend: {data: legendData}, radar: {indicator: indicatorData}, series: series };
		
		options = chartSupport.processUpdateOptions(chart, results, renderOptions, options);
		
		chart.echartsOptions(options);
		
		chart.extValue("radarIndicatorData", indicatorData);
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
			
			chartSupport.chartDataOriginalDataIndex(myData, chartDataSet, j);
			
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
		
		for(var i=0; i<vv.length; i++)
		{
			var name = chart.dataSetPropertyLabel(vp[i]);
			chartSupport.appendElement(legendData, name);
			
			var myData = { name: name, value: vv[i] };
			
			chartSupport.chartDataOriginalDataIndex(myData, chartDataSet, { start: 0, end: indicatorData.length });
			
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
		chartSupport.bindChartEventHandlerDelegationEcharts(chart, eventType, handler,
				chartSupport.radarSetChartEventData);
	};
	
	chartSupport.radarOff = function(chart, eventType, handler)
	{
		chartSupport.unbindChartEventHandlerDelegationEcharts(chart, eventType, handler);
	};
	
	chartSupport.radarSetChartEventData = function(chart, chartEvent, echartsEventParams)
	{
		var signNameMap = chartSupport.chartSignNameMap(chart);
		
		var echartsData = echartsEventParams.data;
		var data = {};
		
		data[signNameMap.item] = echartsData.name;
		data[signNameMap.value] = echartsData.value;
		
		var indicatorData = chart.extValue("radarIndicatorData");
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
		chartSupport.setChartEventOriginalDataForChartData(chart, chartEvent, echartsData);
	};
	
	//漏斗图
	
	chartSupport.funnelRender = function(chart, nameSign, valueSign, options)
	{
		chartSupport.chartSignNameMap(chart, { name: nameSign, value: valueSign });
		
		options = $.extend(true,
		{
			title: {
		        text: chart.name
		    },
		    tooltip:
		    {
		        trigger: "item",
		        formatter: "{a} <br/>{b} : {c}"
		    },
			legend:
			{
				data: []
			},
			series:
			[
				{
		            name: "",
		            type: "funnel",
		            left: "10%",
		            top: "20%",
		            right: "10%",
		            bottom: "10%",
		            min: 0,
		            max: 100,
		            minSize: "0%",
		            maxSize: "100%",
		            sort: "descending",
		            gap: 2,
		            data: []
		        }
			]
		},
		options,
		chart.options());
		
		options = chartSupport.processRenderOptions(chart, options);
		
		chart.echartsInit(options);
	};
	
	chartSupport.funnelUpdate = function(chart, results)
	{
		var signNameMap = chartSupport.chartSignNameMap(chart);
		var renderOptions= chartSupport.renderOptions(chart);
		
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
			
			chartSupport.chartDataOriginalDataIndex(data, chartDataSet);
			
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
		
		var series = [ chartSupport.optionsSeries(renderOptions, 0, {name: seriesName, min: min, max: max, data: seriesData }) ];
		
		var options = { legend: { data: legendData }, series: series };
		
		options = chartSupport.processUpdateOptions(chart, results, renderOptions, options);
		
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
		chartSupport.bindChartEventHandlerDelegationEcharts(chart, eventType, handler,
				chartSupport.funnelSetChartEventData);
	};
	
	chartSupport.funnelOff = function(chart, eventType, handler)
	{
		chartSupport.unbindChartEventHandlerDelegationEcharts(chart, eventType, handler);
	};
	
	chartSupport.funnelSetChartEventData = function(chart, chartEvent, echartsEventParams)
	{
		var signNameMap = chartSupport.chartSignNameMap(chart);
		
		var echartsData = echartsEventParams.data;
		var data = {};
		
		data[signNameMap.name] = echartsData.name;
		data[signNameMap.value] = echartsData.value;
		
		chart.eventData(chartEvent, data);
		chartSupport.setChartEventOriginalDataForChartData(chart, chartEvent, echartsData);
	};
	
	//地图
	
	chartSupport.mapInitChart = function(chart, options)
	{
		//坐标系地图
		var map = (options.geo ? options.geo.map : undefined);
		
		//数据系列地图
		if(map == undefined)
			map = (options.series && options.series.length > 0 ? options.series[0].map : undefined);
		
		if(!map)
			throw new Error("[map] option must be set");
		
		options = chartSupport.processRenderOptions(chart, options);
		
		if(chart.echartsMapRegistered(map))
		{
			chart.echartsInit(options);
			chart.extValue("presetMap", map);
			
			chart.statusRendered(true);
		}
		else
		{
			chart.echartsLoadMap(map, function()
			{
				chart.echartsInit(options);
				chart.extValue("presetMap", map);
				
				chart.statusRendered(true);
			});
		}
	};
	
	chartSupport.mapUpdateChart = function(chart, results, renderOptions, updateOptions)
	{
		var map = undefined;
		//地图作为坐标系，而非图表series
		var isGeo = (renderOptions.geo);
		
		if(isGeo)
			map = (updateOptions.geo ? updateOptions.geo.map : undefined);
		else
			map = (updateOptions.series && updateOptions.series.length > 0 ? updateOptions.series[0].map : undefined);

		var presetMap = chart.extValue("presetMap");
		
		if(!map)
		{
			var currentMap = chart.map();
			
			//通过chart.map(...)设置了新的地图
			if(currentMap && currentMap != presetMap)
			{
				if(isGeo)
				{
					if(!updateOptions.geo)
						updateOptions.geo = {};
					
					updateOptions.geo.map = currentMap;
				}
				else
				{
					if(!updateOptions.series)
						updateOptions.series = [];
					if(updateOptions.series.length == 0)
						updateOptions.series[0] = {};
					
					updateOptions.series[0].map = currentMap;
				}
				
				map = currentMap;
			}
		}
		
		//如果更新了地图，则要重置缩放比例和中心位置，避免出现某些地图无法显示的情况
		if(map && map != presetMap)
		{
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
		
		//没有更新地图、或者更新的地图已注册
		if(!map || chart.echartsMapRegistered(map))
		{
			options = chartSupport.processUpdateOptions(chart, results, renderOptions, updateOptions);
			
			chart.echartsOptions(updateOptions);
			
			if(map)
				chart.extValue("presetMap", map);
			
			chart.statusUpdated(true);
		}
		else
		{
			chart.echartsLoadMap(map, function()
			{
				options = chartSupport.processUpdateOptions(chart, results, renderOptions, updateOptions);
				
				chart.echartsOptions(updateOptions);
				chart.extValue("presetMap", map);
				
				chart.statusUpdated(true);
			});
		}
	};
	
	chartSupport.mapRender = function(chart, nameSign, valueSign, mapSign, options)
	{
		chartSupport.chartSignNameMap(chart, { name: nameSign, value: valueSign, map: mapSign });
		
		options = $.extend(true,
		{
			title: {
		        text: chart.name
		    },
			tooltip:
			{
				trigger: "item",
				formatter: "{b}<br/>{c}"
			},
			visualMap:
			{
				min: 0,
				max: 100,
				text: ["高", "低"],
				realtime: true,
				calculable: true
			},
			series:
			[
				{
					name: "",
					type: "map",
					map: (chart.map() || "china"),
					label: {
						show: true
					},
					data: []
				}
			]
		},
		options,
		chart.options());
		
		chartSupport.mapInitChart(chart, options);
	};
	
	chartSupport.mapUpdate = function(chart, results)
	{
		var signNameMap = chartSupport.chartSignNameMap(chart);
		var renderOptions= chartSupport.renderOptions(chart);
		
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
			
			chartSupport.chartDataOriginalDataIndex(data, chartDataSet);
			
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
		
		var options = { visualMap: {min, min, max: max}, series: [ {name: seriesName, data: seriesData } ] };
		chartSupport.checkMinAndMax(options.visualMap);
		
		if(map)
			options.series[0].map = map;
		
		chartSupport.mapUpdateChart(chart, results, renderOptions, options);
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
		chartSupport.bindChartEventHandlerDelegationEcharts(chart, eventType, handler,
				chartSupport.mapSetChartEventData);
	};
	
	chartSupport.mapOff = function(chart, eventType, handler)
	{
		chartSupport.unbindChartEventHandlerDelegationEcharts(chart, eventType, handler);
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
		chartSupport.setChartEventOriginalDataForChartData(chart, chartEvent, echartsData);
	};
	
	//散点值地图
	
	chartSupport.mapScatterRender = function(chart, nameSign, longitudeSign, latitudeSign, valueSign, mapSign, options)
	{
		chartSupport.chartSignNameMap(chart, { name: nameSign, longitude: longitudeSign, latitude: latitudeSign,
			value: valueSign, map: mapSign });
		
		options = $.extend(true,
		{
			title: {
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
				roam: true,
				map: (chart.map() || "china")
			},
			//最大数据标记像素数
			symbolSizeMax: undefined,
			//最小数据标记像素数
			symbolSizeMin: undefined,
			series:
			[
				{
					name: "",
					type: "scatter",
					coordinateSystem: "geo",
					data: []
				}
			]
		},
		options,
		chart.options());
		
		chartSupport.mapInitChart(chart, options);
	};
	
	chartSupport.mapScatterUpdate = function(chart, results)
	{
		var signNameMap = chartSupport.chartSignNameMap(chart);
		var renderOptions= chartSupport.renderOptions(chart);
		
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
			
			chartSupport.chartDataOriginalDataIndex(data, chartDataSet);
			
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
			series.push(chartSupport.optionsSeries(renderOptions, i, { name: dataSetName, data: data }));
		}
		
		if(min != null && max != null && max <= min)
			max = min + 1;
		
		chartSupport.evalSeriesDataValueSymbolSize(series, min, max, symbolSizeMax, symbolSizeMin, "value", 2);
		
		var options = { legend: {data: legendData}, series: series };
		
		if(map)
			options.geo = { map: map };
		
		chartSupport.mapUpdateChart(chart, results, renderOptions, options);
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
		chartSupport.bindChartEventHandlerDelegationEcharts(chart, eventType, handler,
				chartSupport.mapScatterSetChartEventData);
	};
	
	chartSupport.mapScatterOff = function(chart, eventType, handler)
	{
		chartSupport.unbindChartEventHandlerDelegationEcharts(chart, eventType, handler);
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
		chartSupport.setChartEventOriginalDataForChartData(chart, chartEvent, echartsData);
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
		
		var chartDataSet = chart.chartDataSetFirst();
		
		options = $.extend(true,
		{
			title: {
		        text: chart.name
		    },
			tooltip:
			{
				trigger: "item"
			},
			geo:
			{
				roam: true,
				map: (chart.map() || "china")
			},
			//最大数据标记像素数
			symbolSizeMax: undefined,
			//最大数据标记像素数
			symbolSizeMin: undefined,
			series: [{
				name: "",
				type: "graph",
		        layout: "none",
		        coordinateSystem: "geo",
				data: [],
				links: [],
				legendHoverLink: true,
                focusNodeAdjacency: true,
				label: { position: "right" }
			}]
		},
		options,
		chart.options());
		
		chartSupport.mapInitChart(chart, options);
	};
	
	chartSupport.mapGraphUpdate = function(chart, results)
	{
		var signNameMap = chartSupport.chartSignNameMap(chart);
		var renderOptions= chartSupport.renderOptions(chart);
		
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
					chartSupport.chartDataOriginalDataIndex(sd, chartDataSet, j);
				}
				
				var tidx = chartSupport.appendDistinct(seriesData, td, (tip ? "id" : "name"));
				
				//新插入
				if(tidx == seriesData.length - 1 && seriesData[seriesData.length - 1] === td)
				{
					chartSupport.chartDataOriginalDataIndex(td, chartDataSet, j);
				}
				
				//如果使用id值表示关系，对于数值型id，echarts会误当做数据索引，所以这里直接使用数据索引
				var link = {};
				link.source = sidx;
				link.target = tidx;
				
				chartSupport.chartDataOriginalDataIndex(link, chartDataSet, j);
				
				seriesLinks.push(link);
			}
		}
		
		if(min != null && max != null && max <= min)
			max = min + 1;
		
		var series = [ chartSupport.optionsSeries(renderOptions, 0, { name: seriesName, categories: categories, data: seriesData, links: seriesLinks }) ];
		
		chartSupport.evalSeriesDataValueSymbolSize(series, min, max, symbolSizeMax, symbolSizeMin, "value", 2);
		
		var options = { legend: {data: legendData}, series: series };
		
		if(map)
			options.geo = { map: map };
		
		chartSupport.mapUpdateChart(chart, results, renderOptions, options);
		
		chart.extValue("mapGraphSeriesData", seriesData);
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
		chartSupport.bindChartEventHandlerDelegationEcharts(chart, eventType, handler,
				chartSupport.mapGraphSetChartEventData);
	};
	
	chartSupport.mapGraphOff = function(chart, eventType, handler)
	{
		chartSupport.unbindChartEventHandlerDelegationEcharts(chart, eventType, handler);
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
			chartSupport.setChartEventOriginalDataForChartData(chart, chartEvent, echartsData);
		}
		//边
		else if(echartsEventParams.dataType == "edge")
		{
			var seriesData = chart.extValue("mapGraphSeriesData");
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
			chartSupport.setChartEventOriginalDataForChartData(chart, chartEvent, echartsData);
		}
	};
	
	
	//K线图
	
	chartSupport.candlestickRender = function(chart, nameSign, openSign, closeSign, minSign, maxSign, options)
	{
		chartSupport.chartSignNameMap(chart, { name: nameSign, open: openSign, close: closeSign, min: minSign, max: maxSign });
		
		var chartDataSet = chart.chartDataSetFirst();
		var np = chart.dataSetPropertyOfSign(chartDataSet, nameSign);
		
		options = $.extend(true,
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
				data: []
			},
			xAxis: {
				name: chart.dataSetPropertyLabel(np),
				nameGap: 5,
				type: (chartSupport.isDataTypeNumber(np) ? "value" : "category"),
				boundaryGap: true,
				splitLine: {show:false},
				data: []
			},
			yAxis: {
				name: "",
				nameGap: 5,
				type: "value"
			},
			series: [{
				name: "",
				type: "k",
				data: []
			}]
		},
		options,
		chart.options());
		
		options = chartSupport.processRenderOptions(chart, options);
		
		chart.echartsInit(options);
	};
	
	chartSupport.candlestickUpdate = function(chart, results)
	{
		var signNameMap = chartSupport.chartSignNameMap(chart);
		var renderOptions= chartSupport.renderOptions(chart);
		
		var chartDataSets = chart.chartDataSetsMain();
		
		var legendData = [];
		var series = [];
		
		for(var i=0; i<chartDataSets.length; i++)
		{
			var chartDataSet = chartDataSets[i];
			
			var dataSetName = chart.chartDataSetName(chartDataSet);
			var result = chart.resultOf(results, chartDataSet);
			
			var data = chart.resultNameValueObjects(result, chart.dataSetPropertyOfSign(chartDataSet, signNameMap.name),
					[
						chart.dataSetPropertyOfSign(chartDataSet, signNameMap.open),
						chart.dataSetPropertyOfSign(chartDataSet, signNameMap.close),
						chart.dataSetPropertyOfSign(chartDataSet, signNameMap.min),
						chart.dataSetPropertyOfSign(chartDataSet, signNameMap.max)
					]);
			
			chartSupport.chartDataOriginalDataIndex(data, chartDataSet);
			
			series.push(chartSupport.optionsSeries(renderOptions, i, {name: dataSetName, data: data}));
		}
		
		var options = { legend: {data: legendData}, series: series };
		
		options = chartSupport.processUpdateOptions(chart, results, renderOptions, options);
		
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
		chartSupport.bindChartEventHandlerDelegationEcharts(chart, eventType, handler,
				chartSupport.candlestickSetChartEventData);
	};
	
	chartSupport.candlestickOff = function(chart, eventType, handler)
	{
		chartSupport.unbindChartEventHandlerDelegationEcharts(chart, eventType, handler);
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
		chartSupport.setChartEventOriginalDataForChartData(chart, chartEvent, echartsData);
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
		
		options = $.extend(true,
		{
			title: {
		        text: chart.name
		    },
			tooltip:
			{
				trigger: "item"
			},
			grid: { bottom: vmItemWidth + 20 },
			legend:
			{
				data: []
			},
			xAxis: {
				name: chart.dataSetPropertyLabel(np),
				nameGap: 5,
				type: "category",
				splitArea: { show: true },
				data: []
			},
			yAxis: {
				name: chart.dataSetPropertyLabel(vp),
				nameGap: 5,
				type: "category",
				splitArea: { show: true },
				data: []
			},
			visualMap:
			{
				min: 0,
				max: 100,
				text: ["高", "低"],
				realtime: true,
				calculable: true,
				orient: "horizontal",
		        left: "center",
		        itemWidth: vmItemWidth,
		        itemHeight: parseInt(chartEle.width()/8),
		        bottom: 0
			},
			series: [{
				name: "",
				type: "heatmap",
				label: {show:true},
				emphasis: { itemStyle: { shadowBlur: 5 } },
				data: []
			}]
		},
		options,
		chart.options());
		
		options = chartSupport.processRenderOptions(chart, options);
		
		chart.echartsInit(options);
	};
	
	chartSupport.heatmapUpdate = function(chart, results)
	{
		var signNameMap = chartSupport.chartSignNameMap(chart);
		var renderOptions= chartSupport.renderOptions(chart);
		
		var chartDataSets = chart.chartDataSetsMain();
		
		var xAxisData = [];
		var yAxisData = [];
		var seriesName = "";
		var seriesData = [];
		var min = undefined, max=undefined;
		
		for(var i=0; i<chartDataSets.length; i++)
		{
			var chartDataSet = chartDataSets[i];
			
			seriesName = chart.chartDataSetName(chartDataSet);
			var result = chart.resultOf(results, chartDataSet);
			
			var np = chart.dataSetPropertyOfSign(chartDataSet, signNameMap.name);
			var vp = chart.dataSetPropertyOfSign(chartDataSet, signNameMap.value);
			var wp = chart.dataSetPropertyOfSign(chartDataSet, signNameMap.weight);
			
			var data = chart.resultRowArrays(result, [ np, vp, wp ]);
			var dataNew = [];
			
			for(var j=0; j<data.length; j++)
			{
				var dataEle = { value: data[j] };
				var dw = dataEle.value[2];
				
				chartSupport.appendDistinct(xAxisData, dataEle.value[0]);
				chartSupport.appendDistinct(yAxisData, dataEle.value[1]);
				
				min = (min == undefined ? dw : Math.min(min, dw));
				max = (max == undefined ? dw : Math.max(max, dw));
				
				dataNew[j] = dataEle;
			}
			
			data = dataNew;
			
			chartSupport.chartDataOriginalDataIndex(data, chartDataSet);
			
			seriesData = seriesData.concat(data);
		}
		
		if(min == undefined)
			min = 0;
		if(max == undefined)
			max = 1;
		if(max < min)
			max = min + 1;
		
		var series = [ chartSupport.optionsSeries(renderOptions, 0, { name: seriesName, data: seriesData }) ];
		
		var options = { xAxis: { data: xAxisData }, yAxis: { data: yAxisData }, visualMap: {min: min, max: max}, series: series };
		chartSupport.checkMinAndMax(options.visualMap);
		
		options = chartSupport.processUpdateOptions(chart, results, renderOptions, options);
		
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
		chartSupport.bindChartEventHandlerDelegationEcharts(chart, eventType, handler,
				chartSupport.heatmapSetChartEventData);
	};
	
	chartSupport.heatmapOff = function(chart, eventType, handler)
	{
		chartSupport.unbindChartEventHandlerDelegationEcharts(chart, eventType, handler);
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
		chartSupport.setChartEventOriginalDataForChartData(chart, chartEvent, echartsData);
	};
	
	//树图
	chartSupport.treeRender = function(chart, idSign, nameSign, parentSign, valueSign, options)
	{
		chartSupport.chartSignNameMap(chart, { id: idSign, name: nameSign, parent: parentSign, value: valueSign });
		
		var chartDataSet = chart.chartDataSetFirst();
		
		options = $.extend(true,
		{
			title: {
		        text: chart.name
		    },
			tooltip:
			{
				trigger: "item"
			},
			series: [{
				name: "",
				data: [],
				type: "tree",
				label:
				{
					position: "left",
					verticalAlign: "middle",
					align: "right"
                },
                leaves:
                {
                	label:
                	{
                		position: "right",
                		verticalAlign: "middle",
                		align: "left"
                	}
                },
                left: "16%",
                right: "16%",
                top: "12%",
                bottom: "12%",
                orient: "LR",
                expandAndCollapse: true
			}]
		},
		options,
		chart.options());
		
		options = chartSupport.processRenderOptions(chart, options);
		
		chart.echartsInit(options);
	};
	
	chartSupport.treeUpdate = function(chart, results)
	{
		var renderOptions= chartSupport.renderOptions(chart);
		
		var mySeries = chartSupport.buildTreeNodeSeries(chart, results);
		var series = [ chartSupport.optionsSeries(renderOptions, 0, mySeries) ];
		
		var options = { series: series };
		
		options = chartSupport.processUpdateOptions(chart, results, renderOptions, options);
		
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
		chartSupport.bindChartEventHandlerDelegationEcharts(chart, eventType, handler,
				chartSupport.treeSetChartEventData);
	};
	
	chartSupport.treeOff = function(chart, eventType, handler)
	{
		chartSupport.unbindChartEventHandlerDelegationEcharts(chart, eventType, handler);
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
		chartSupport.setChartEventOriginalDataForChartData(chart, chartEvent, echartsData);
	};
	
	//矩形树图
	chartSupport.treemapRender = function(chart, idSign, nameSign, parentSign, valueSign, options)
	{
		chartSupport.chartSignNameMap(chart, { id: idSign, name: nameSign, parent: parentSign, value: valueSign });
		
		var chartDataSet = chart.chartDataSetFirst();
		
		options = $.extend(true,
		{
			title: {
		        text: chart.name
		    },
			tooltip:
			{
				trigger: "item"
			},
			series: [{
				name: "",
				type: "treemap",
				data: []
			}]
		},
		options,
		chart.options());
		
		options = chartSupport.processRenderOptions(chart, options);
		
		chart.echartsInit(options);
	};
	
	chartSupport.treemapUpdate = function(chart, results)
	{
		var renderOptions= chartSupport.renderOptions(chart);
		
		var mySeries = chartSupport.buildTreeNodeSeries(chart, results);
		var series = [ chartSupport.optionsSeries(renderOptions, 0, mySeries) ];
		
		var options = { series: series };
		
		options = chartSupport.processUpdateOptions(chart, results, renderOptions, options);
		
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
		chartSupport.bindChartEventHandlerDelegationEcharts(chart, eventType, handler,
				chartSupport.treemapSetChartEventData);
	};
	
	chartSupport.treemapOff = function(chart, eventType, handler)
	{
		chartSupport.unbindChartEventHandlerDelegationEcharts(chart, eventType, handler);
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
		chartSupport.setChartEventOriginalDataForChartData(chart, chartEvent, echartsData);
		
		return data;
	};
	
	//旭日图
	
	chartSupport.sunburstRender = function(chart, idSign, nameSign, parentSign, valueSign, options)
	{
		chartSupport.chartSignNameMap(chart, { id: idSign, name: nameSign, parent: parentSign, value: valueSign });
		
		var chartDataSet = chart.chartDataSetFirst();
		
		options = $.extend(true,
		{
			title: {
		        text: chart.name
		    },
			tooltip:
			{
				trigger: "item"
			},
			series: [{
				name: "",
				type: "sunburst",
				data: []
			}]
		},
		options,
		chart.options());
		
		options = chartSupport.processRenderOptions(chart, options);
		
		chart.echartsInit(options);
	};
	
	chartSupport.sunburstUpdate = function(chart, results)
	{
		var renderOptions= chartSupport.renderOptions(chart);
		
		var mySeries = chartSupport.buildTreeNodeSeries(chart, results);
		var series = [ chartSupport.optionsSeries(renderOptions, 0, mySeries) ];
		
		var options = { series: series };
		
		options = chartSupport.processUpdateOptions(chart, results, renderOptions, options);
		
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
		chartSupport.bindChartEventHandlerDelegationEcharts(chart, eventType, handler,
				chartSupport.sunburstSetChartEventData);
	};
	
	chartSupport.sunburstOff = function(chart, eventType, handler)
	{
		chartSupport.unbindChartEventHandlerDelegationEcharts(chart, eventType, handler);
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
		chartSupport.setChartEventOriginalDataForChartData(chart, chartEvent, echartsData);
	};
	
	chartSupport.buildTreeNodeSeries = function(chart, results)
	{
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
				
				chartSupport.chartDataOriginalDataIndex(node, chartDataSet, j);
				
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
		
		return { name: seriesName, data: seriesData };
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
		
		var chartDataSet = chart.chartDataSetFirst();
		
		options = $.extend(true,
		{
			title: {
		        text: chart.name
		    },
			tooltip:
			{
				trigger: "item"
			},
			series: [{
				name: "",
				type: "sankey",
		        layout: "none",
				data: [],
				links: [],
				left: "16%",
                right: "16%",
                top: "12%",
                bottom: "12%",
		        focusNodeAdjacency: 'allEdges'
			}]
		},
		options,
		chart.options());
		
		//自适应条目宽度和间隔
		var chartEle = chart.elementJquery();
		var orient = options.series[0].orient;
		
		var nodeWidth = options.series[0].nodeWidth;
		if(nodeWidth == null)
		{
			var totalWidth = (orient == "vertical" ? chartEle.height() : chartEle.width());
			nodeWidth = parseInt(totalWidth * 5/100);
			nodeWidth = (nodeWidth < 4 ? 4: nodeWidth);
			options.series[0].nodeWidth = nodeWidth;
		}
		
		var nodeGap = options.series[0].nodeGap;
		if(nodeGap == null)
		{
			var totalWidth = (orient == "vertical" ? chartEle.width() : chartEle.height());
			nodeGap = parseInt(totalWidth * 2/100);
			nodeGap = (nodeWidth < 1 ? 1: nodeGap);
			options.series[0].nodeGap = nodeGap;
		}
		
		options = chartSupport.processRenderOptions(chart, options);
		
		chart.echartsInit(options);
	};
	
	chartSupport.sankeyUpdate = function(chart, results)
	{
		var signNameMap = chartSupport.chartSignNameMap(chart);
		var renderOptions= chartSupport.renderOptions(chart);
		
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
				
				chartSupport.chartDataOriginalDataIndex(sd, chartDataSet, j);
				
				var sidx = chartSupport.appendDistinct(seriesData, sd, "name");
				
				//新插入
				if(sidx == seriesData.length - 1 && seriesData[seriesData.length - 1] === sd)
				{
					chartSupport.chartDataOriginalDataIndex(sd, chartDataSet, j);
				}
				
				var tidx = chartSupport.appendDistinct(seriesData, td, "name");
				
				//新插入
				if(tidx == seriesData.length - 1 && seriesData[seriesData.length - 1] === td)
				{
					chartSupport.chartDataOriginalDataIndex(td, chartDataSet, j);
				}
				
				var link = {};
				link.source = sd.name;
				link.target = td.name;
				link.value = chart.resultRowCell(data[j], vp);
				
				link._sourceIndex = sidx;
				link._targetIndex = tidx;
				
				chartSupport.chartDataOriginalDataIndex(link, chartDataSet, j);
				
				seriesLinks.push(link);
			}
		}
		
		var series = [ chartSupport.optionsSeries(renderOptions, 0, { name: seriesName, data: seriesData, links: seriesLinks }) ];
		
		var options = { series: series };
		
		options = chartSupport.processUpdateOptions(chart, results, renderOptions, options);
		
		chart.echartsOptions(options);
		
		chart.extValue("sankeySeriesData", seriesData);
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
		chartSupport.bindChartEventHandlerDelegationEcharts(chart, eventType, handler, chartSupport.sankeySetChartEventData);
	};
	
	chartSupport.sankeyOff = function(chart, eventType, handler)
	{
		chartSupport.unbindChartEventHandlerDelegationEcharts(chart, eventType, handler);
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
			chartSupport.setChartEventOriginalDataForChartData(chart, chartEvent, echartsData);
		}
		//边
		else if(echartsEventParams.dataType == "edge")
		{
			var seriesData = chart.extValue("sankeySeriesData");
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
			chartSupport.setChartEventOriginalDataForChartData(chart, chartEvent, echartsData);
		}
	};
	
	//关系图
	
	chartSupport.graphRender = function(chart, sourceIdSign, sourceNameSign, sourceCategorySign, sourceValueSign,
			targetIdSign, targetNameSign, targetCategorySign, targetValueSign, valueSign, options)
	{
		chartSupport.chartSignNameMap(chart,
				{ sourceId: sourceIdSign, sourceName: sourceNameSign, sourceCategory: sourceCategorySign, sourceValue: sourceValueSign,
					targetId: targetIdSign, targetName: targetNameSign, targetCategory: targetCategorySign, targetValue: targetValueSign,
					value: valueSign });
		
		var chartDataSet = chart.chartDataSetFirst();
		
		options = $.extend(true,
		{
			title: {
		        text: chart.name
		    },
			tooltip:
			{
				trigger: "item"
			},
			//最大数据标记像素数
			symbolSizeMax: undefined,
			//最大数据标记像素数
			symbolSizeMin: undefined,
			series: [{
				name: "",
				type: "graph",
		        layout: "force",
				data: [],
				links: [],
				legendHoverLink: true,
                focusNodeAdjacency: true,
                draggable: true,
				label: { position: "right" },
				roam: true,
				left: "12%",
                right: "12%",
                top: "20%",
                bottom: "12%",
			}]
		},
		options,
		chart.options());
		
		options = chartSupport.processRenderOptions(chart, options);
		
		chart.echartsInit(options);
	};
	
	chartSupport.graphUpdate = function(chart, results)
	{
		var signNameMap = chartSupport.chartSignNameMap(chart);
		var renderOptions= chartSupport.renderOptions(chart);
		
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
					chartSupport.chartDataOriginalDataIndex(sd, chartDataSet, j);
				}
				
				var tidx = chartSupport.appendDistinct(seriesData, td, (tip ? "id" : "name"));
				
				//新插入
				if(tidx == seriesData.length - 1 && seriesData[seriesData.length - 1] === td)
				{
					chartSupport.chartDataOriginalDataIndex(td, chartDataSet, j);
				}
				
				//如果使用id值表示关系，对于数值型id，echarts会误当做数据索引，所以这里直接使用数据索引
				var link = {};
				link.source = sidx;
				link.target = tidx;
				
				if(vp)
					link.value = chart.resultRowCell(data[j], vp);
				
				chartSupport.chartDataOriginalDataIndex(link, chartDataSet, j);
				
				seriesLinks.push(link);
			}
		}
		
		if(min != undefined && max != undefined && min >= max)
			min = max - 1;
		
		if(min == undefined && max == undefined && symbolSizeMin < 10)
			symbolSizeMin = 10;
		
		var series = [ chartSupport.optionsSeries(renderOptions, 0,
						 { name: seriesName, categories: categories, data: seriesData, links: seriesLinks }) ];
		
		chartSupport.evalSeriesDataValueSymbolSize(series, min, max, symbolSizeMax, symbolSizeMin);
		
		if(series[0].layout == "force")
		{
			if(!series[0].force)
				series[0].force = {};
			
			//自动计算散点间距
			if(series[0].force.edgeLength == null)
				series[0].force.edgeLength = parseInt(symbolSizeMax*1.5);
			
			//自动计算散点稀疏度
			if(series[0].force.repulsion == null)
				series[0].force.repulsion = parseInt(symbolSizeMax*2);
		}
		
		var options = { legend: {data: legendData}, series: series };
		
		options = chartSupport.processUpdateOptions(chart, results, renderOptions, options);
		
		chart.echartsOptions(options);
		
		chart.extValue("graphSeriesData", seriesData);
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
		chartSupport.bindChartEventHandlerDelegationEcharts(chart, eventType, handler,
				chartSupport.graphSetChartEventData);
	};
	
	chartSupport.graphOff = function(chart, eventType, handler)
	{
		chartSupport.unbindChartEventHandlerDelegationEcharts(chart, eventType, handler);
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
			chartSupport.setChartEventOriginalDataForChartData(chart, chartEvent, echartsData);
		}
		//边
		else if(echartsEventParams.dataType == "edge")
		{
			var seriesData = chart.extValue("graphSeriesData");
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
			chartSupport.setChartEventOriginalDataForChartData(chart, chartEvent, echartsData);
		}
	};
	
	//词云图
	
	chartSupport.wordcloudRender = function(chart, nameSign, valueSign, options)
	{
		chartSupport.chartSignNameMap(chart, { name: nameSign, value: valueSign });
		
		//不支持在echarts主题中设置样式，只能在这里设置
		var chartTheme = chart.theme();
		
		options = $.extend(true,
		{
			title: {
		        text: chart.name
		    },
			tooltip:
			{
				trigger: "item"
			},
			//自定义由低到高值域颜色映射
			colorRange: chartTheme.graphRangeColors,
			//自定义由低到高值渐变色数组，如果不设置，将由colorRange自动计算
			colorGradients: undefined,
			series:
			[
				{
					type: "wordCloud",
					shape: "circle",
					data: [],
					"textStyle":
					{
						"normal":{},
						"emphasis":
						{
							"shadowBlur" : 10,
							"shadowColor" : chartFactory.getGradualColor(chartTheme, 0.9),
						}
					}
				}
			]
		},
		options,
		chart.options());
		
		var chartEle = chart.elementJquery();
		
		//自适应字体大小
		var sizeRange = options.series[0].sizeRange;
		if(sizeRange == null)
		{
			var chartSize = Math.min(chartEle.height(), chartEle.width());
			sizeRange = [parseInt(chartSize * 1/40), parseInt(chartSize * 1/8)];
			sizeRange[0] = (sizeRange[0] < 6 ? 6: sizeRange[0]);
			options.series[0].sizeRange = sizeRange;
		}
		
		//计算渐变色
		var colorRange = options.colorRange;
		if(colorRange != null)
		{
			var colorGradients = [];
			for(var i=0; i<colorRange.length; i++)
			{
				var fromColor = colorRange[i];
				var toColor = ((i+1) < colorRange.length ? colorRange[i+1] : null);
				
				if(!toColor)
					break;
				
				colorGradients = colorGradients.concat(chartFactory.evalGradualColors(fromColor, toColor, 5));
			}
			
			options.colorGradients = colorGradients;
		}
		
		options = chartSupport.processRenderOptions(chart, options);
		
		chart.echartsInit(options);
	};
	
	chartSupport.wordcloudUpdate = function(chart, results)
	{
		var signNameMap = chartSupport.chartSignNameMap(chart);
		var renderOptions= chartSupport.renderOptions(chart);
		
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
				
				chartSupport.chartDataOriginalDataIndex(data[j], chartDataSet, j);
			}
			
			seriesData = seriesData.concat(data);
		}
		
		if(min >= max)
			min = max - 1;
		
		//映射颜色值
		var colorGradients = renderOptions.colorGradients;
		if(colorGradients)
		{
			for(var i=0; i<seriesData.length; i++)
			{
				var colorIndex = parseInt((seriesData[i].value-min)/(max-min) * (colorGradients.length-1));
				seriesData[i].textStyle = { "normal":{ "color": colorGradients[colorIndex] } };
			}
		}
		
		var series = [ chartSupport.optionsSeries(renderOptions, 0, {name: seriesName, data: seriesData}) ];
		
		var options = { series: series };
		
		options = chartSupport.processUpdateOptions(chart, results, renderOptions, options);
		
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
		chartSupport.bindChartEventHandlerDelegationEcharts(chart, eventType, handler,
				chartSupport.wordcloudSetChartEventData);
	};
	
	chartSupport.wordcloudOff = function(chart, eventType, handler)
	{
		chartSupport.unbindChartEventHandlerDelegationEcharts(chart, eventType, handler);
	};
	
	chartSupport.wordcloudSetChartEventData = function(chart, chartEvent, echartsEventParams)
	{
		var signNameMap = chartSupport.chartSignNameMap(chart);
		
		var echartsData = echartsEventParams.data;
		var data = {};
		
		data[signNameMap.name] = echartsData.name;
		data[signNameMap.value] = echartsData.value;
		
		chart.eventData(chartEvent, data);
		chartSupport.setChartEventOriginalDataForChartData(chart, chartEvent, echartsData);
	};
	
	//水球图
	
	chartSupport.liquidfillRender = function(chart, nameSign, valueSign, options)
	{
		chartSupport.chartSignNameMap(chart, { name: nameSign, value: valueSign });
		
		//不支持在echarts主题中设置样式，只能在这里设置
		var chartTheme = chart.theme();
		
		options = $.extend(true,
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
					name: "",
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
					},
					shape: "circle",
					data: [],
					
					//扩展配置项
					//如果仅有一个波浪数据，则自动复制扩充至这些个波浪数据
					autoInflateWave: 3,
				}
			]
		},
		options,
		chart.options());
		
		options = chartSupport.processRenderOptions(chart, options);
		
		chart.echartsInit(options);
	};
	
	chartSupport.liquidfillUpdate = function(chart, results)
	{
		var signNameMap = chartSupport.chartSignNameMap(chart);
		var renderOptions= chartSupport.renderOptions(chart);
		
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
						chartSupport.chartDataOriginalDataIndex(sv, chartDataSet, j);
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
						chartSupport.chartDataOriginalDataIndex(sv, chartDataSet, j);
						data.push(sv);
					}
				}
			}
			
			seriesData = seriesData.concat(data);
		}
		
		//如果仅有一个波浪，则自动扩充
		if(seriesData.length == 1 && renderOptions.series[0].autoInflateWave > 1)
		{
			for(var i=1; i<renderOptions.series[0].autoInflateWave; i++)
			{
				var inflateValue = $.extend({}, seriesData[0]);
				seriesData.push(inflateValue);
			}
		}
		
		var series = [ chartSupport.optionsSeries(renderOptions, 0, {data: seriesData}) ];
		
		var options = { series: series };
		
		options = chartSupport.processUpdateOptions(chart, results, renderOptions, options);
		
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
		chartSupport.bindChartEventHandlerDelegationEcharts(chart, eventType, handler,
				chartSupport.liquidfillSetChartEventData);
	};
	
	chartSupport.liquidfillOff = function(chart, eventType, handler)
	{
		chartSupport.unbindChartEventHandlerDelegationEcharts(chart, eventType, handler);
	};
	
	chartSupport.liquidfillSetChartEventData = function(chart, chartEvent, echartsEventParams)
	{
		var signNameMap = chartSupport.chartSignNameMap(chart);
		
		var echartsData = echartsEventParams.data;
		var data = {};
		
		data[signNameMap.name] = echartsData.name;
		data[signNameMap.value] = echartsData.value;
		
		chart.eventData(chartEvent, data);
		chartSupport.setChartEventOriginalDataForChartData(chart, chartEvent, echartsData);
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
			pauseOnHover: true
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
		
		//表格图表样式设置项
		var options = $.extend(true,
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
		options,
		chart.options());
		
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
		
		options = chartSupport.processRenderOptions(chart, options);
		
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
			chartSupport.tableEvalDataTableBodyHeight(chartContent, dataTable);
		
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
		
		chart.extValue("chartTableId", tableId);
	};
	
	chartSupport.tableUpdate = function(chart, results, options)
	{
		var renderOptions = chartSupport.renderOptions(chart);
		var dataTable = chartSupport.tableGetChartDataTable(chart);
		
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
				chartSupport.chartDataOriginalDataIndex(data, chartDataSet, j);
				updateOptions.data.push(data);
			}
		}
		
		chartSupport.tableStopCarousel(chart);
		
		updateOptions = chartSupport.processUpdateOptions(chart, results, renderOptions, updateOptions);
		
		chartSupport.tableAddDataTableData(dataTable, updateOptions.data, 0);
		
		chartSupport.tableAdjust(chart);
		
		if(renderOptions.carousel.enable)
		{
			chartSupport.tablePrepareCarousel(chart);
			chartSupport.tableStartCarousel(chart);
		}
	};
	
	chartSupport.tableResize = function(chart)
	{
		var chartContent = chartSupport.tableGetChartContent(chart);
		var dataTable = chartSupport.tableGetChartDataTable(chart);
		
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
		var htmlHandler = function(htmlEvent)
		{
			var rowElement = this;
			var chartEvent = chart.eventNewHtml(eventType, htmlEvent);
			chartSupport.tableSetChartEventData(chart, chartEvent, htmlEvent, rowElement);
			handler.call(chart, chartEvent);
		};
		
		chart.eventBindHandlerDelegation(eventType, handler, htmlHandler,
				chartSupport.tableChartEventDelegationEventBinder);
	};
	
	chartSupport.tableOff = function(chart, eventType, handler)
	{
		chart.eventUnbindHandlerDelegation(eventType, handler,
				chartSupport.tableChartEventDelegationEventBinder);
	};
	
	chartSupport.tableSetChartEventData = function(chart, chartEvent, htmlEvent, rowElement)
	{
		var signNameMap = chartSupport.chartSignNameMap(chart);
		
		var dataTable = chartSupport.tableGetChartDataTable(chart);
		
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
		chartSupport.setChartEventOriginalDataForChartData(chart, chartEvent, chartData);
	};
	
	chartSupport.tableChartEventDelegationEventBinder =
	{
		bind: function(chart, eventType, delegateEventHandler)
		{
			var dataTable = chartSupport.tableGetChartDataTable(chart);
			$(dataTable.table().body()).on(eventType, "tr", delegateEventHandler);
		},
		unbind: function(chart, eventType, delegateEventHandler)
		{
			var dataTable = chartSupport.tableGetChartDataTable(chart);
			$(dataTable.table().body()).off(eventType, "tr", delegateEventHandler);
		}
	};
	
	chartSupport.tableGetChartContent = function(chart)
	{
		//图表的数据透视表功能也采用的是DataTable组件，可能会与表格图表处在同一个图表div内，
		//因此，获取图表表格的DOM操作都应限定在".dg-chart-table-content"内
		
		return $(".dg-chart-table-content", chart.element());
	};
	
	chartSupport.tableGetChartDataTable = function(chart)
	{
		var tableId = chart.extValue("chartTableId");
		return $("#" + tableId, chartSupport.tableGetChartContent(chart)).DataTable();
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
		
		chartFactory.createStyleSheet(styleSheetId, cssText);
		
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
			var row = dataTable.row.add(datas[dataIndex]);
		
		dataTable.rows(removeRowIndexes).remove();
		
		dataTable.draw();
	};
	
	/**
	 * 调整图表表格。
	 * 当表格隐藏显示、位置调整、数据变更后，可能会出现表头、固定列错位的情况，需要重新调整。
	 */
	chartSupport.tableAdjust = function(chart)
	{
		var dataTable = chartSupport.tableGetChartDataTable(chart);
		
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
		var renderOptions = chartSupport.renderOptions(chart);
		var chartEle = chart.elementJquery();
		var chartContent = chartSupport.tableGetChartContent(chart);
		var dataTable = chartSupport.tableGetChartDataTable(chart);
		var carousel = renderOptions.carousel;
		
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
		var renderOptions = chartSupport.renderOptions(chart);
		var chartEle = chart.elementJquery();
		var chartContent = chartSupport.tableGetChartContent(chart);
		var dataTable = chartSupport.tableGetChartDataTable(chart);
		
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
		
		var interval = ($.isFunction(renderOptions.carousel.interval) ?
				renderOptions.carousel.interval(currentRow, currentRowVisibleHeight, currentRowHeight) : renderOptions.carousel.interval);
		
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
		
		var chartTheme = chart.theme();
		
		var chartEle = chart.elementJquery();
		chartEle.addClass("dg-chart-label");
		
		options = $.extend(true,
		{
			//是否标签值在前
			"valueFirst": false,
			"label":
			{
				//标签名样式，这里不必添加默认样式，因为图表元素已设置
				"name":
				{
					"show": true
				},
				//标签值样式，这里不必添加默认样式，因为图表元素已设置
				"value":
				{
				}
			}
		},
		options,
		chart.options());
		
		options = chartSupport.processRenderOptions(chart, options);
	};
	
	chartSupport.labelUpdate = function(chart, results)
	{
		var signNameMap = chartSupport.chartSignNameMap(chart);
		var renderOptions = chartSupport.renderOptions(chart);
		var valueFirst = renderOptions.valueFirst;
		var showName = renderOptions.label.name.show;
		
		var chartDataSets = chart.chartDataSetsMain();
		
		var $parent = chart.elementJquery();
		
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
						chartSupport.chartDataOriginalDataIndex(sv, chartDataSet, j);
						
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
						chartSupport.chartDataOriginalDataIndex(sv, chartDataSet, j);
						
						updateOptions.data.push(sv);
					}
				}
			}
		}
		
		updateOptions = chartSupport.processUpdateOptions(chart, results, renderOptions, updateOptions);
		
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
					chartFactory.setStyles($labelValue, renderOptions.label.value);
					
					if(showName)
					{
						$labelName = $("<div class='label-name'></div>").appendTo($label);
						chartFactory.setStyles($labelName, renderOptions.label.name);
					}
				}
				else
				{
					if(showName)
					{
						$labelName = $("<div class='label-name'></div>").appendTo($label);
						chartFactory.setStyles($labelName, renderOptions.label.name);
					}
					
					$labelValue = $("<div class='label-value'></div>").appendTo($label);
					chartFactory.setStyles($labelValue, renderOptions.label.value);
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
		$(".dg-chart-label-item", chartEle).each(function()
		{
			$(this).remove();
		});
	};
	
	chartSupport.labelOn = function(chart, eventType, handler)
	{
		var htmlHandler = function(htmlEvent)
		{
			var $label = $(this);
			var chartEvent = chart.eventNewHtml(eventType, htmlEvent);
			chartSupport.labelSetChartEventData(chart, chartEvent, htmlEvent, $label);
			handler.call(chart, chartEvent);
		};
		
		chart.eventBindHandlerDelegation(eventType, handler, htmlHandler,
				chartSupport.labelChartEventDelegationEventBinder);
	};
	
	chartSupport.labelOff = function(chart, eventType, handler)
	{
		chart.eventUnbindHandlerDelegation(eventType, handler,
				chartSupport.labelChartEventDelegationEventBinder);
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
		chartSupport.setChartEventOriginalDataForChartData(chart, chartEvent, chartData);
	};
	
	chartSupport.labelChartEventDelegationEventBinder =
	{
		bind: function(chart, eventType, delegateEventHandler)
		{
			chart.elementJquery().on(eventType, ".dg-chart-label-item", delegateEventHandler);
		},
		unbind: function(chart, eventType, delegateEventHandler)
		{
			chart.elementJquery().off(eventType, ".dg-chart-label-item", delegateEventHandler);
		}
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
		
		var customRenderer = chart.customChartRenderer();
		
		if(customRenderer == null && !nullable)
			throw new Error("Custom chart renderer undefined");
		
		return customRenderer;
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
	
	/**
	 * 获取/设置图表渲染options。
	 */
	chartSupport.renderOptions = function(chart, renderOptions)
	{
		if(renderOptions == undefined)
			return (chart.extValue("renderOptions") || {});
		else
			chart.extValue("renderOptions", renderOptions);
	};
	
	/**
	 * 合并指定索引的series元素对象，如果索引不对，则返回前一个合并。
	 */
	chartSupport.optionsSeries = function(options, index, mergeEle)
	{
		var seriesLen = (options && options.series ? (options.series.length || 0) : 0);
		
		if(!seriesLen)
			return {};
		
		index = (index < seriesLen ? index : seriesLen - 1);
		
		var ele = (options.series[index] || {});
		
		if(mergeEle == undefined)
			return ele;
		
		return $.extend({}, ele, mergeEle);
	};
	
	/**
	 * 获取/设置选项series模板
	 * 
	 * @param chart 图表对象
	 * @param options 要设置的options、要获取的series元素索引
	 */
	chartSupport.optionSeriesTemplate = function(chart, options)
	{
		//获取series数组
		if(options == undefined)
		{
			return chart.extValue("chartOptionSeriesTemplate");
		}
		//获取series元素
		else if(typeof(options) == "number")
		{
			var template = chart.extValue("chartOptionSeriesTemplate");
			var index = (template.length > options ? options : 0);
			
			return (template[index] ? template[index] : {});
		}
		//设置series数组
		else
			chart.extValue("chartOptionSeriesTemplate", (options.series || []));
	};
	
	//在图表渲染前处理渲染options
	chartSupport.processRenderOptions = function(chart, renderOptions)
	{
		if(renderOptions.processRenderOptions)
		{
			var tmpOptions = renderOptions.processRenderOptions(renderOptions, chart);
			if(tmpOptions != null)
				renderOptions = tmpOptions;
		}
		
		chartSupport.renderOptions(chart, renderOptions);
		
		return renderOptions;
	};
	
	//在图表更新前处理更新options
	chartSupport.processUpdateOptions = function(chart, results, renderOptions, updateOptions)
	{
		//先将chart.optionsUpdate()合并至updateOptions
		var cou = chart.optionsUpdate();
		if(cou)
			$.extend(true, updateOptions, cou);
		
		if(renderOptions.processUpdateOptions)
		{
			var tmpOptions = renderOptions.processUpdateOptions(updateOptions, chart, results);
			if(tmpOptions != null)
				updateOptions = tmpOptions;
		}
		
		return updateOptions;
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
	 * 提取对象/对象数组的指定属性值。
	 * 
	 * @param source 对象、对象数组
	 * @param propertyName 属性名
	 * @return 输性值、属性值数组
	 */
	chartSupport.extractPropertyValue = function(source, propertyName)
	{
		if(!$.isArray(source))
			return (source ? source[propertyName] : undefined);
		
		var re = [];
		
		for(var i=0; i<source.length; i++)
			re[i] = source[i][propertyName];
		
		return re;
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
		var echartsInstance = chart.echartsInstance();
		if(echartsInstance && !echartsInstance.isDisposed())
			echartsInstance.dispose();
		
		chart.echartsInstance(null);
	};
	
	/**
	 * 调整图表的echarts尺寸。
	 */
	chartSupport.resizeChartEcharts = function(chart)
	{
		var echartsInstance = chart.echartsInstance();
		
		if(echartsInstance)
			echartsInstance.resize();
	};
	
	chartSupport.chartSignNameMap = function(chart, signNameMap)
	{
		if(signNameMap == undefined)
			return chart.extValue("signNameMap");
		else
			chart.extValue("signNameMap", signNameMap);
	};
	
	chartSupport.setChartEventOriginalDataForChartData = function(chart, chartEvent, chartData)
	{
		var index = chartSupport.chartDataOriginalDataIndex(chartData);
		this.setChartEventOriginalDataByIndex(chart, chartEvent, index);
	};
	
	/**
	 * 根据原始数据索引对象设置图表事件对象的原始数据相关信息。
	 * 
	 * @param chart
	 * @param chartEvent
	 * @param originalDataIndex 原始数据索引对象，格式为：
	 * 			{
	 * 				//图表数据集索引数值
	 * 				chartDataSetIndex: 数值,
	 * 				
	 * 				//图表数据集结果数据索引信息，格式为：
	 * 				//数值：单条结果数据索引；
	 * 				//[数值, ...]：多条结果数据索引；
	 * 				//{start: 数值, end: 数值}：范围结果数据索引；
	 * 				resultDataIndex: ... 
	 * 			}
	 */
	chartSupport.setChartEventOriginalDataByIndex = function(chart, chartEvent, originalDataIndex)
	{
		if(!originalDataIndex)
		{
			chart.eventOriginalData(chartEvent, null);
			chart.eventOriginalChartDataSetIndex(chartEvent, null);
			chart.eventOriginalResultDataIndex(chartEvent, null);
			
			return;
		}
		
		var rdi = originalDataIndex.resultDataIndex;
		
		if(rdi.start != null && rdi.end != null)
		{
			var rdiAry = [];
			
			for(var i=rdi.start; i<rdi.end; i++)
				rdiAry.push(i);
			
			rdi = rdiAry;
		}
		
		chart.eventOriginalInfo(chartEvent, originalDataIndex.chartDataSetIndex, rdi);
	};
	
	chartSupport.KEY_ORIGINAL_DATA_INDEX = "_DataGearOriginalDataIndex";
	
	/**
	 * 获取/设置图表数据对象的原始数据索引对象。
	 * 
	 * @param chartData 图表数据对象、数组
	 * @param chartDataSet 要设置的原始图表数据集对象
	 * @param resultDataIndex 要设置的原始结果数据索引，默认值为：0，参考setChartEventOriginalDataByIndex函数的originalDataIndex.resultDataIndex参数说明
	 */
	chartSupport.chartDataOriginalDataIndex = function(chartData, chartDataSet, resultDataIndex)
	{
		if(!chartData)
			return undefined;
		
		if(chartDataSet === undefined)
		{
			if($.isArray(chartData))
			{
				var re = [];
				
				for(var i=0; i<chartData.length; i++)
				{
					re.push(chartData[i][chartSupport.KEY_ORIGINAL_DATA_INDEX]);
				}
				
				return re;
			}
			else
			{
				return chartData[chartSupport.KEY_ORIGINAL_DATA_INDEX];
			}
		}
		
		if(resultDataIndex === undefined)
			resultDataIndex = 0;
		
		if($.isArray(chartData))
		{
			for(var i=0; i<chartData.length; i++)
			{
				var resultDataIndexMy = ($.isNumeric(resultDataIndex) ? resultDataIndex + i : resultDataIndex);
				
				chartData[i][chartSupport.KEY_ORIGINAL_DATA_INDEX] =
				{
					chartDataSetIndex : chartDataSet.index,
					resultDataIndex: resultDataIndexMy
				};
			}
		}
		else
		{
			chartData[chartSupport.KEY_ORIGINAL_DATA_INDEX] =
			{
				chartDataSetIndex : chartDataSet.index,
				resultDataIndex: resultDataIndex
			};
		}
	};
	
	chartSupport.bindChartEventHandlerDelegationEcharts = function(chart, eventType, chartEventHanlder, chartEventDataSetter)
	{
		var echartsEventHandler = function(params)
		{
			var chartEvent = chart.eventNewEcharts(eventType, params);
			chartEventDataSetter(chart, chartEvent, params);
			chartEventHanlder.call(chart, chartEvent);
		};
		
		chart.eventBindHandlerDelegation(eventType, chartEventHanlder, echartsEventHandler,
				chartSupport.chartEventDelegationEventBinderEcharts);
	};
	
	chartSupport.unbindChartEventHandlerDelegationEcharts = function(chart, eventType, chartEventHanlder)
	{
		chartSupport.unbindChartEventDelegateHandler(chart, eventType, chartEventHanlder,
				chartSupport.chartEventDelegationEventBinderEcharts);
	};
	
	chartSupport.chartEventDelegationEventBinderEcharts =
	{
		bind: function(chart, eventType, delegateEventHandler)
		{
			chart.echartsInstance().on(eventType, "series", delegateEventHandler);
		},
		unbind: function(chart, eventType, delegateEventHandler)
		{
			chart.echartsInstance().off(eventType, delegateEventHandler);
		}
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
	 * @param 可选，自动获取的比率
	 */
	chartSupport.evalSymbolSizeMax = function(chart, options, ratio)
	{
		var symbolSizeMax = (options ? options.symbolSizeMax : undefined);
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
	 */
	chartSupport.evalSymbolSizeMin = function(chart, options, symbolSizeMax, ratio)
	{
		var symbolSizeMin = (options ? options.symbolSizeMin : undefined);
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
		
		if(value == null)
			return symbolSizeMin;
		
		var size = parseInt((value-minValue)/(maxValue-minValue)*symbolSizeMax);
		return (size < symbolSizeMin ? symbolSizeMin : size);
	};
	
	/**
	 * 计算系列数据数值的图符元素尺寸
	 * 
	 * @param series 系列对象：{ data: [ {value: ...}, ... ] }，或者其数组
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
			var data = 	series[i].data;
			
			for(var j=0; j<data.length; j++)
			{
				var value = data[j][valuePropertyName];
				
				if(valueElementIndex != null)
					value = ($.isArray(value) && valueElementIndex < value.length ? value[valueElementIndex] : null);
				
				data[j].symbolSize = chartSupport.evalValueSymbolSize(
					value, minValue, maxValue, symbolSizeMax, symbolSizeMin);
			}
		}
	};
	
	//---------------------------------------------------------
	//    公用函数结束
	//---------------------------------------------------------
})
(this);
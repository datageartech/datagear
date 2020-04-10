/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 图表支持库。
 * 全局变量名：window.chartSupport。
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
	var chartSupport = (global.chartSupport || (global.chartSupport = {}));
	
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
	
	/**
	 * 指定数据集属性数据是否字符串类型。
	 */
	chartSupport.isDataTypeString = function(dataSetProperty)
	{
		var dataType = (dataSetProperty ? (dataSetProperty.type || dataSetProperty) : "");
		return (dataType == "STRING");
	};
	
	/**
	 * 指定数据集属性数据是否数值类型。
	 */
	chartSupport.isDataTypeNumber = function(dataSetProperty)
	{
		var dataType = (dataSetProperty ? (dataSetProperty.type || dataSetProperty) : "");
		return (dataType == "INTEGER" || dataType == "DECIMAL");
	};
	
	//折线图
	
	chartSupport.lineRender = function(chart, coordSign, valueSign, options)
	{
		var chartDataSet = chart.chartDataSetFirst();
		var cp = chart.dataSetPropertyOfSign(chartDataSet, coordSign);
		var vp = chart.dataSetPropertyOfSign(chartDataSet, valueSign);
		
		options = chart.options($.extend(true,
		{
			title: {
		        text: chart.nameNonNull()
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
				name: chart.dataSetPropertyLabel(cp),
				nameGap: 5,
				type: (chartSupport.isDataTypeNumber(cp) ? "value" : "category"),
				boundaryGap: false
			},
			yAxis: {
				name: chart.dataSetPropertyLabel(vp),
				nameGap: 5,
				type: "value"
			},
			series: [{
				name: "",
				type: "line",
				data: []
			}]
		},
		options));
		
		chartSupport.optionSeriesTemplate(chart, options);
		
		chart.echartsInit(options, false);
	};
	
	chartSupport.lineUpdate = function(chart, results, coordSign, valueSign)
	{
		var chartDataSets = chart.chartDataSetsNonNull();
		
		var legendData = [];
		var series = [];
		
		for(var i=0; i<chartDataSets.length; i++)
		{
			var chartDataSet = chartDataSets[i];
			var dataSetName = chart.dataSetName(chartDataSet);
			var properties = [chart.dataSetPropertyOfSign(chartDataSet, coordSign), chart.dataSetPropertyOfSign(chartDataSet, valueSign)];
			var result = chart.resultAt(results, i);
			var data = chart.resultRowArrays(result, properties);
			
			legendData[i] = dataSetName;
			series[i] = $.extend({}, chartSupport.optionSeriesTemplate(chart, i), {name: dataSetName, data: data});
		}
		
		var options = { legend: {data: legendData}, series: series };
		chart.echartsOptions(options);
	};
	
	//柱状图
	
	chartSupport.barRender = function(chart, coordSign, valueSign, options)
	{
		var chartDataSet = chart.chartDataSetFirst();
		var cp = chart.dataSetPropertyOfSign(chartDataSet, coordSign);
		var vps = (options && options.stackBar ? chart.dataSetPropertiesOfSign(chartDataSet, valueSign)
					: [chart.dataSetPropertyOfSign(chartDataSet, valueSign)]);
		
		options = chart.options($.extend(true,
		{
			title: {
		        text: chart.nameNonNull()
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
				name: chart.dataSetPropertyLabel(cp),
				nameGap: 5,
				type: "category",
				boundaryGap: true,
				data: []
			},
			yAxis: {
				name: (vps.length == 1 ? chart.dataSetPropertyLabel(vps[0]) : ""),
				nameGap: 5,
				type: "value"
			},
			series: [{
				name: "",
				type: "bar",
				stack: "",
				label: { show: (vps.length > 1) },
				data: []
			}]
		},
		options));
		
		chartSupport.optionSeriesTemplate(chart, options);
		chart.extValue("stackBar", (options && options.stackBar));
		
		chart.echartsInit(options, false);
	};
	
	chartSupport.barUpdate = function(chart, results, coordSign, valueSign)
	{
		var chartDataSets = chart.chartDataSetsNonNull();
		
		var legendData = [];
		var xAxisData = [];
		var series = [];
		
		for(var i=0; i<chartDataSets.length; i++)
		{
			var chartDataSet = chartDataSets[i];
			var dataSetName = chart.dataSetName(chartDataSet);
			var result = chart.resultAt(results, i);
			
			if(i == 0)
			{
				var cp = chart.dataSetPropertyOfSign(chartDataSet, coordSign);
				xAxisData = chart.resultColumnArrays(result, cp);
			}
			
			var vps = (chart.extValue("stackBar") ? chart.dataSetPropertiesOfSign(chartDataSet, valueSign)
						: [chart.dataSetPropertyOfSign(chartDataSet, valueSign)]);
			
			for(var j=0; j<vps.length; j++)
			{
				var vp = vps[j];
				
				var legendName = "";
				if(chartDataSets.length > 1 && vps.length > 1)
					legendName = dataSetName +"-" + chart.dataSetPropertyLabel(vp);
				else if(chartDataSets.length > 1)
					legendName = dataSetName;
				else if(vps.length > 1)
					legendName = chart.dataSetPropertyLabel(vp);
				
				var data = chart.resultColumnArrays(result, vp);
				
				legendData.push(legendName);
				series.push($.extend({}, chartSupport.optionSeriesTemplate(chart, i*vps.length+j), {name: legendName, stack: "stack-"+i, data: data}));
			}
		}
		
		var options = { legend: {data: legendData}, xAxis : { data : xAxisData }, series: series };
		chart.echartsOptions(options);
	};
	
	//横向柱状图
	
	chartSupport.barHorizontalRender = function(chart, coordSign, valueSign, options)
	{
		var chartDataSet = chart.chartDataSetFirst();
		var cp = chart.dataSetPropertyOfSign(chartDataSet, coordSign);
		var vps = (options && options.stackBar ? chart.dataSetPropertiesOfSign(chartDataSet, valueSign)
					: [chart.dataSetPropertyOfSign(chartDataSet, valueSign)]);
		
		options = chart.options($.extend(true,
		{
			title: {
		        text: chart.nameNonNull()
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
				name: (vps.length == 1 ? chart.dataSetPropertyLabel(vps[0]) : ""),
				nameGap: 5,
				type: "value"
			},
			yAxis: {
				name: chart.dataSetPropertyLabel(cp),
				nameGap: 5,
				type: "category",
				boundaryGap: true,
				data: []
			},
			series: [{
				name: "",
				type: "bar",
				stack: "",
				label: { show: (vps.length > 1) },
				data: []
			}]
		},
		options));
		
		chartSupport.optionSeriesTemplate(chart, options);
		chart.extValue("stackBar", (options && options.stackBar));
		
		chart.echartsInit(options, false);
	};
	
	chartSupport.barHorizontalUpdate = function(chart, results, coordSign, valueSign)
	{
		var chartDataSets = chart.chartDataSetsNonNull();
		
		var legendData = [];
		var yAxisData = [];
		var series = [];
		
		for(var i=0; i<chartDataSets.length; i++)
		{
			var chartDataSet = chartDataSets[i];
			var dataSetName = chart.dataSetName(chartDataSet);
			var result = chart.resultAt(results, i);
			
			if(i == 0)
			{
				var cp = chart.dataSetPropertyOfSign(chartDataSet, coordSign);
				yAxisData = chart.resultColumnArrays(result, cp);
			}
			
			var vps = (chart.extValue("stackBar") ? chart.dataSetPropertiesOfSign(chartDataSet, valueSign)
						: [chart.dataSetPropertyOfSign(chartDataSet, valueSign)]);
			
			for(var j=0; j<vps.length; j++)
			{
				var vp = vps[j];
				
				var legendName = "";
				if(chartDataSets.length > 1 && vps.length > 1)
					legendName = dataSetName +"-" + chart.dataSetPropertyLabel(vp);
				else if(chartDataSets.length > 1)
					legendName = dataSetName;
				else if(vps.length > 1)
					legendName = chart.dataSetPropertyLabel(vp);
				
				var data = chart.resultColumnArrays(result, vp);
				
				legendData.push(legendName);
				series.push($.extend({}, chartSupport.optionSeriesTemplate(chart, i*vps.length+j), {name: legendName, stack: "stack-"+i, data: data}));
			}
		}
		
		var options = { legend: {data: legendData}, yAxis : { data : yAxisData }, series: series };
		chart.echartsOptions(options);
	};
	
	//饼图
	
	chartSupport.pieRender = function(chart, coordSign, valueSign, options)
	{
		options = chart.options($.extend(true,
		{
			title: {
		        text: chart.nameNonNull()
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
					name: chart.nameNonNull(),
					type: "pie",
					radius: "55%",
					center: ["50%", "60%"],
					data: []
				}
			]
		},
		options));
		
		chartSupport.optionSeriesTemplate(chart, options);
		
		chart.echartsInit(options, false);
	};
	
	chartSupport.pieUpdate = function(chart, results, coordSign, valueSign)
	{
		var chartDataSets = chart.chartDataSetsNonNull();

		var legendData = [];
		var seriesName = "";
		var seriesData = [];
		
		for(var i=0; i<chartDataSets.length; i++)
		{
			var chartDataSet = chartDataSets[i];
			var dataSetName = chart.dataSetName(chartDataSet);
			var result = chart.resultAt(results, i);

			var cp = chart.dataSetPropertyOfSign(chartDataSet, coordSign);
			var cpv = chart.resultColumnArrays(result, cp);
			var vp = chart.dataSetPropertyOfSign(chartDataSet, valueSign);
			var nvv = chart.resultNameValueObjects(result, cp, vp);
			
			legendData = legendData.concat(cpv);
			if(i == 0)
				seriesName = dataSetName;
			seriesData = seriesData.concat(nvv);
		}
		
		var series = [ $.extend({}, chartSupport.optionSeriesTemplate(chart, 0), {name: seriesName, data: seriesData}) ];
		
		var options = { legend: { data: legendData }, series: series };
		chart.echartsOptions(options);
	};
	
	//仪表盘
	
	chartSupport.gaugeRender = function(chart, valueSign, minSign, maxSign, options)
	{
		options = chart.options($.extend(true,
		{
			title: {
		        text: chart.nameNonNull()
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
		options));
		
		chart.echartsInit(options, false);
	};
	
	chartSupport.gaugeUpdate = function(chart, results, valueSign, minSign, maxSign)
	{
		var chartDataSet = chart.chartDataSetFirst();
		var result = chart.resultFirst(results);
		
		var seriesName = chart.dataSetName(chartDataSet);
		
		var minp = chart.dataSetPropertyOfSign(chartDataSet, minSign);
		var maxp = chart.dataSetPropertyOfSign(chartDataSet, maxSign);
		var vp = chart.dataSetPropertyOfSign(chartDataSet, valueSign);
		
		var min = (chart.resultCell(result, minp) || 0);
		var max = (chart.resultCell(result, maxp) || 100);
		var value = (chart.resultCell(result, vp) || 0);
		
		var options = { series : 
			[
				{
					name: seriesName, min: min, max: max,
					data: [{ value: value, name: chart.dataSetPropertyLabel(vp) }]
				}
			]};
		
		chart.echartsOptions(options);
	};
	
	//散点图
	
	chartSupport.scatterRender = function(chart, coordSign, valueSign, options)
	{
		var chartDataSet = chart.chartDataSetFirst();
		var cp = chart.dataSetPropertyOfSign(chartDataSet, coordSign);
		var vp = chart.dataSetPropertyOfSign(chartDataSet, valueSign);
		
		options = chart.options($.extend(true,
		{
			title: {
		        text: chart.nameNonNull()
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
				name: chart.dataSetPropertyLabel(cp),
				nameGap: 5,
				type: (chartSupport.isDataTypeNumber(cp) ? "value" : "category"),
				boundaryGap: !chartSupport.isDataTypeNumber(cp)
			},
			yAxis: {
				name: chart.dataSetPropertyLabel(vp),
				nameGap: 5,
				type: "value"
			},
			//最大数据标记像素数
			symbolSizeMax: undefined,
			series: [{
				name: "",
				type: "scatter",
				data: []
			}]
		},
		options));
		
		chartSupport.optionSeriesTemplate(chart, options);
		
		if(options.symbolSizeMax)
			chartSupport.scatterSymbolSizeMax(chart, options.symbolSizeMax);
		
		chart.echartsInit(options, false);
	};
	
	chartSupport.scatterUpdate = function(chart, results, coordSign, valueSign)
	{
		var chartDataSets = chart.chartDataSetsNonNull();
		
		var legendData = [];
		var series = [];
		
		var min = undefined, max = undefined;
		var symbolSizeMax = chartSupport.scatterSymbolSizeMax(chart);
		
		for(var i=0; i<chartDataSets.length; i++)
		{
			var chartDataSet = chartDataSets[i];
			var dataSetName = chart.dataSetName(chartDataSet);
			var properties = [chart.dataSetPropertyOfSign(chartDataSet, coordSign), chart.dataSetPropertyOfSign(chartDataSet, valueSign)];
			var result = chart.resultAt(results, i);
			var data = chart.resultRowArrays(result, properties);
			
			for(var j=0; j<data.length; j++)
			{
				min = (min == undefined ? data[j][1] : Math.min(min, data[j][1]));
				max = (max == undefined ? data[j][1] : Math.max(max, data[j][1]));
				
				if(max <= min)
					max = min + 1;
			}
			
			legendData[i] = dataSetName;
			series[i] = $.extend({}, chartSupport.optionSeriesTemplate(chart, i),
					{
						name: dataSetName, data: data,
						symbolSize: function(value)
						{
							return chartSupport.scatterEvalSymbolSize(value[1], min, max, symbolSizeMax);
						}
					});
		}
		
		var options = { legend: {data: legendData}, series: series };
		chart.echartsOptions(options);
	};
	
	/**
	 * 获取、设置最大数据标记像素数
	 * @param chart
	 * @param symbolSizeMax 为数值时，设置操作；为undefined、{ratio:0.125}时，获取操作
	 */
	chartSupport.scatterSymbolSizeMax = function(chart, symbolSizeMax)
	{
		if(typeof(symbolSizeMax) == "number")
			chart.extValue("scatterSymbolSizeMax", symbolSizeMax);
		else
		{
			var ratio = (symbolSizeMax && symbolSizeMax.ratio ? symbolSizeMax.ratio : 0.125);
			
			symbolSizeMax = chart.extValue("scatterSymbolSizeMax");
			
			//根据图表元素尺寸自动计算
			if(!symbolSizeMax)
			{
				var chartEle = chart.elementJquery();
				symbolSizeMax =parseInt(Math.min(chartEle.width(), chartEle.height())*ratio);
			}
			
			return symbolSizeMax;
		}
	};
	
	//计算散点大小
	chartSupport.scatterEvalSymbolSize = function(value, minValue, maxValue, symbolSizeMax, symbolSizeMin)
	{
		if(symbolSizeMin == undefined)
			symbolSizeMin = 4;
		
		var size = parseInt((value-minValue)/(maxValue-minValue)*symbolSizeMax);
		return (size < symbolSizeMin ? symbolSizeMin : size);
	};
	
	//雷达图
	
	chartSupport.radarRender = function(chart, nameSign, coordSign, valueSign, maxSign, options)
	{
		options = chart.options($.extend(true,
		{
			title: {
		        text: chart.nameNonNull()
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
		options));
		
		chartSupport.optionSeriesTemplate(chart, options);
		
		chart.echartsInit(options, false);
	};
	
	chartSupport.radarUpdate = function(chart, results, nameSign, coordSign, valueSign, maxSign)
	{
		var chartDataSets = chart.chartDataSetsNonNull();
		
		var legendData = [];
		var indicatorData = [];
		var series = [];
		
		for(var i=0; i<chartDataSets.length; i++)
		{
			var chartDataSet = chartDataSets[i];
			var dataSetName = chart.dataSetName(chartDataSet);
			var result = chart.resultAt(results, i);
			
			var np = chart.dataSetPropertyOfSign(chartDataSet, nameSign);
			var nv = chart.resultColumnArrays(result, np);
			legendData = legendData.concat(nv);
			
			if(i == 0)
			{
				var dnp = chart.dataSetPropertiesOfSign(chartDataSet, coordSign);
				var dnpv = chart.resultRowArrays(result, dnp, 0, 1);
				dnpv = (dnpv.length > 0 ? dnpv[0] : []);
				var dmp = chart.dataSetPropertiesOfSign(chartDataSet, maxSign);
				var dmpv = chart.resultRowArrays(result, dmp, 0, 1);
				dmpv = (dmpv.length > 0 ? dmpv[0] : []);
				
				var indicatorLen = Math.min(dnp.length, dmp.length);
				
				for(var j=0; j<indicatorLen; j++)
				{
					var indicator = {name: dnpv[j], max: dmpv[j]};
					indicatorData[j] = indicator;
				}
			}
			
			var dvp = chart.dataSetPropertiesOfSign(chartDataSet, valueSign);
			var dvpv = chart.resultRowArrays(result, dvp);
			
			for(var j=0; j<nv.length; j++)
			{
				series.push($.extend({}, chartSupport.optionSeriesTemplate(chart, i*nv.length+j), {data: [{name: nv[j], value: dvpv[j]}]}));
			}
		}
		
		var options = { legend: {data: legendData}, radar: {indicator: indicatorData}, series: series };
		chart.echartsOptions(options);
	};
	
	//漏斗图
	
	chartSupport.funnelRender = function(chart, coordSign, valueSign, options)
	{
		options = chart.options($.extend(true,
		{
			title: {
		        text: chart.nameNonNull()
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
		            top: 80,
		            bottom: 60,
		            width: "80%",
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
		options));

		chartSupport.optionSeriesTemplate(chart, options);
		
		chart.echartsInit(options, false);
	};
	
	chartSupport.funnelUpdate = function(chart, results, coordSign, valueSign)
	{
		var chartDataSets = chart.chartDataSetsNonNull();

		var legendData = [];
		var seriesName = "";
		var seriesData = [];
		var min = 0;
		var max = 100;

		for(var i=0; i<chartDataSets.length; i++)
		{
			var chartDataSet = chartDataSets[i];
			var dataSetName = chart.dataSetName(chartDataSet);
			var result = chart.resultAt(results, i);

			var cp = chart.dataSetPropertyOfSign(chartDataSet, coordSign);
			var cpv = chart.resultColumnArrays(result, cp);
			var vp = chart.dataSetPropertyOfSign(chartDataSet, valueSign);
			var nvv = chart.resultNameValueObjects(result, cp, vp);
			
			legendData = legendData.concat(cpv);
			if(i == 0)
				seriesName = dataSetName;
			seriesData = seriesData.concat(nvv);
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
		
		var series = [$.extend({}, chartSupport.optionSeriesTemplate(chart, 0), {name: seriesName, min: min, max: max, data: seriesData })];
		
		var options = { legend: { data: legendData }, series: series };
		chart.echartsOptions(options);
	};
	
	//地图
	
	chartSupport.mapRender = function(chart, coordSign, valueSign, options)
	{
		options = chart.options($.extend(true,
		{
			title: {
		        text: chart.nameNonNull()
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
		options));
		
		var map = options.series[0].map;
		
		chart.extValue("mapPresetMap", map);
		chart.map(map);
		
		if(chart.echartsMapRegistered(map))
		{
			chart.echartsInit(options, false);
			chart.statusPreUpdate(true);
		}
		else
		{
			chart.echartsMapLoad(map, function()
			{
				chart.echartsInit(options, false);
				chart.statusPreUpdate(true);
			});
		}
	};
	
	chartSupport.mapUpdate = function(chart, results, coordSign, valueSign)
	{
		var chartDataSets = chart.chartDataSetsNonNull();
		
		var min = Number.MAX_VALUE;
		var max = Number.MIN_VALUE;
		var seriesName = "";
		var seriesData = [];

		for(var i=0; i<chartDataSets.length; i++)
		{
			var chartDataSet = chartDataSets[i];
			var dataSetName = chart.dataSetName(chartDataSet);
			var result = chart.resultAt(results, i);

			var cp = chart.dataSetPropertyOfSign(chartDataSet, coordSign);
			var vp = chart.dataSetPropertyOfSign(chartDataSet, valueSign);
			var nvv = chart.resultNameValueObjects(result, cp, vp);
			
			if(i == 0)
				seriesName = dataSetName;
			seriesData = seriesData.concat(nvv);
			
			if(nvv && nvv.length)
			{
				for(var j=0; j<nvv.length; j++)
				{
					var val = nvv[j].value;
					if(val < min)
						min = val;
					else if(val > max)
						max = val;
				}
			}
		}
		
		var options = { visualMap: {min, min, max: max}, series: [ {name: seriesName, data: seriesData } ] };
		options = chart.optionsModified(options);
		
		var map = options.series[0].map;
		
		if(!map)
		{
			var eleMap = chart.map();
			if(eleMap && eleMap != chart.extValue("mapPresetMap"))
			{
				options.series[0].map = eleMap;
				map = eleMap;
			}
		}
		
		if(map)
		{
			chart.extValue("mapPresetMap", map);
			chart.map(map);
		}
		
		if(!map || chart.echartsMapRegistered(map))
		{
			chart.echartsOptions(options, false);
			chart.statusUpdated(true);
		}
		else
		{
			chart.echartsMapLoad(map, function()
			{
				chart.echartsOptions(options, false);
				chart.statusUpdated(true);
			});
		}
	};
	
	//散点值地图
	
	chartSupport.mapScatterRender = function(chart, coordSign, coordLongitudeSign, coordLatitudeSign, valueSign, options)
	{
		options = chart.options($.extend(true,
		{
			title: {
		        text: chart.nameNonNull()
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
		options));
		
		var map = options.geo.map;
		
		chart.extValue("mapPresetMap", map);
		chart.map(map);
		if(options.symbolSizeMax)
			chartSupport.scatterSymbolSizeMax(chart, options.symbolSizeMax);
		chartSupport.optionSeriesTemplate(chart, options);
		
		if(chart.echartsMapRegistered(map))
		{
			chart.echartsInit(options, false);
			chart.statusPreUpdate(true);
		}
		else
		{
			chart.echartsMapLoad(map, function()
			{
				chart.echartsInit(options, false);
				chart.statusPreUpdate(true);
			});
		}
	};
	
	chartSupport.mapScatterUpdate = function(chart, results, coordSign, coordLongitudeSign, coordLatitudeSign, valueSign)
	{
		var chartDataSets = chart.chartDataSetsNonNull();
		
		var legendData = [];
		var series = [];
		
		var min = undefined, max = undefined;
		var symbolSizeMax = chartSupport.scatterSymbolSizeMax(chart, {ratio:0.1});
		
		for(var i=0; i<chartDataSets.length; i++)
		{
			var chartDataSet = chartDataSets[i];
			var dataSetName = chart.dataSetName(chartDataSet);
			var valueProperties =
			[
				chart.dataSetPropertyOfSign(chartDataSet, coordLongitudeSign),
				chart.dataSetPropertyOfSign(chartDataSet, coordLatitudeSign),
				chart.dataSetPropertyOfSign(chartDataSet, valueSign)
			];
			var result = chart.resultAt(results, i);
			var data = chart.resultNameValueObjects(result, chart.dataSetPropertyOfSign(chartDataSet, coordSign), valueProperties);
			
			for(var j=0; j<data.length; j++)
			{
				var dv = data[j].value;
				
				min = (min == undefined ? dv[2] : Math.min(min, dv[2]));
				max = (max == undefined ? dv[2] : Math.max(max, dv[2]));
				
				if(max <= min)
					max = min + 1;
			}
			
			legendData[i] = dataSetName;
			series[i] = $.extend({}, chartSupport.optionSeriesTemplate(chart, i),
					{
						name: dataSetName, data: data,
						symbolSize: function(value)
						{
							return chartSupport.scatterEvalSymbolSize(value[2], min, max, symbolSizeMax);
						}
					});
		}
		
		var options = { legend: {data: legendData}, series: series };
		options = chart.optionsModified(options);
		
		var map = (options.geo ? options.geo.map : undefined);
		
		if(!map)
		{
			var eleMap = chart.map();
			if(eleMap && eleMap != chart.extValue("mapPresetMap"))
			{
				options.geo = {map: eleMap};
				map = eleMap;
			}
		}
		
		if(map)
		{
			chart.extValue("mapPresetMap", map);
			chart.map(map);
		}
		
		if(!map || chart.echartsMapRegistered(map))
		{
			chart.echartsOptions(options, false);
			chart.statusUpdated(true);
		}
		else
		{
			chart.echartsMapLoad(map, function()
			{
				chart.echartsOptions(options, false);
				chart.statusUpdated(true);
			});
		}
	};
	
	//标签卡
	
	chartSupport.labelRender = function(chart, coordSign, valueSign, options)
	{
		options = chart.options($.extend(true,
		{
			valueFirst: false,    //是否标签值在前
			showName: true        //是否绘制标签名
		},
		options));
		
		chart.extValue("options", options);
	};
	
	chartSupport.labelUpdate = function(chart, results, coordSign, valueSign)
	{
		var options = chart.extValue("options");
		var valueFirst = options.valueFirst;
		var showName = options.showName;
		var options = chart.optionsModified(options);
		var clear = (valueFirst != options.valueFirst || showName != options.showName);
		
		var chartDataSets = chart.chartDataSetsNonNull();
		
		var $parent = chart.elementJquery();
		
		if(clear)
			$(".dg-chart-label", $parent).empty();
		else
			$(".dg-chart-label", $parent).addClass("dg-chart-label-pending");
		
		for(var i=0; i<chartDataSets.length; i++)
		{
			var chartDataSet = chartDataSets[i];
			var dataSetName = chart.dataSetName(chartDataSet);
			var result = chart.resultAt(results, i);
			
			var cps = chart.dataSetPropertiesOfSign(chartDataSet, coordSign);
			var vps = chart.dataSetPropertiesOfSign(chartDataSet, valueSign);
			var cv = (cps.length > 0 ? chart.resultRowArrays(result, cps) : [] );
			var vv = chart.resultRowArrays(result, vps);
			
			for(var j=0; j<vv.length; j++)
			{
				var vvj = vv[j];
				
				for(var k=0; k<vvj.length; k++)
				{
					var cssName = "dg-chart-label-"+i+"-"+j+"-"+k;
					var name = (cv.length > j && cv[j].length > k ? cv[j][k] : chart.dataSetPropertyLabel(vps[k]));
					var value = vv[j][k];
					
					var $label = $("."+ cssName, $parent);
					if($label.length == 0)
						$label = $("<div class='dg-chart-label dg-chart-label-"+i+" dg-chart-label-"+i+"-"+j+" "+cssName+"'></div>").appendTo($parent);
					else
						$label.removeClass("dg-chart-label-pending");
					
					var $labelName = $(".label-name", $label);
					var $labelValue = $(".label-value", $label);
					
					if(options.showName)
					{
						if(options.valueFirst && $labelValue.length == 0)
							$labelValue = $("<div class='label-value'></div>").appendTo($label);
						
						if($labelName.length == 0)
							$labelName = $("<div class='label-name'></div>").appendTo($label);
						
						if(!options.valueFirst && $labelValue.length == 0)
							$labelValue = $("<div class='label-value'></div>").appendTo($label);
					}
					else
					{
						if($labelValue.length == 0)
							$labelValue = $("<div class='label-value'></div>").appendTo($label);
					}
					
					if(options.showName)
						$labelName.html(name);
					$labelValue.html(value);
				}
			}
		}
		
		$(".dg-chart-label-pending", $parent).remove();
	};
	
	//自定义
	
	chartSupport.customAsyncRender = function(chart)
	{
		var customRenderer = chartSupport.customGetCustomRenderer(chart);
		
		if(!customRenderer || customRenderer.asyncRender == undefined)
			return false;
		
		if(typeof(customRenderer.asyncRender) == "function")
			return customRenderer.asyncRender(chart);
		
		return (customRenderer.asyncRender == true);
	};
	
	chartSupport.customRender = function(chart)
	{
		var customRenderer = chartSupport.customGetCustomRenderer(chart);
		
		if(customRenderer)
			customRenderer.render(chart);
	};
	
	chartSupport.customAsyncUpdate = function(chart, results)
	{
		var customRenderer = chartSupport.customGetCustomRenderer(chart);
		
		if(!customRenderer || customRenderer.asyncUpdate == undefined)
			return false;
		
		if(typeof(customRenderer.asyncUpdate) == "function")
			return customRenderer.asyncUpdate(chart, results);
		
		return (customRenderer.asyncUpdate == true);
	};
	
	chartSupport.customUpdate = function(chart, results)
	{
		var customRenderer = chartSupport.customGetCustomRenderer(chart);
		
		if(customRenderer)
			customRenderer.update(chart, results);
	};
	
	chartSupport.customGetCustomRenderer = function(chart)
	{
		var customRenderer = chart.extValue("customRenderer");
		
		if(customRenderer == "_undefined")
			return undefined;
		
		if(customRenderer)
			return customRenderer;
		
		var $element = chart.elementJquery();
		var customRendererVar = $element.attr("dg-chart-renderer");
		
		if(!customRendererVar)
			$element.html("The 'dg-chart-renderer' attribute must be set for this custom chart");
		else
		{
			customRenderer = chartFactory.evalSilently(customRendererVar);
			
			if(!customRenderer)
				$element.html("No chart renderer var '"+customRendererVar+"' defined for this custom chart");
		}
		
		chart.extValue("customRenderer", (customRenderer || "_undefined"));
		
		return customRenderer;
	};
})
(this);
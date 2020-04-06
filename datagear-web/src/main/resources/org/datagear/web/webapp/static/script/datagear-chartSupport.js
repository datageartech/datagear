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
	
	chartSupport.optionSeries0 = function(chart, options)
	{
		if(options == undefined)
			return chart.extValue("chartOptionSeries0");
		
		var series0 = (options.series[0] || {});
		chart.extValue("chartOptionSeries0", series0);
	};
	
	//折线图
	
	chartSupport.lineRender = function(chart, coordSign, valueSign, options)
	{
		var chartDataSet = chart.chartDataSetFirst();
		var xp = chart.dataSetPropertyOfSign(chartDataSet, coordSign);
		var yp = chart.dataSetPropertyOfSign(chartDataSet, valueSign);
		
		options = $.extend(true,
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
				name: chart.dataSetPropertyLabel(xp),
				nameGap: 5,
				type: "category",
				boundaryGap: false
			},
			yAxis: {
				name: chart.dataSetPropertyLabel(yp),
				nameGap: 5,
				type: "value",
			},
			series: [{
				name: "",
				type: "line",
				data: []
			}]
		},
		options);
		
		chartSupport.optionSeries0(chart, options);
		
		chart.echartsInit(options);
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
			series[i] = $.extend({}, chartSupport.optionSeries0(chart), {name: dataSetName, data: data});
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
		
		options = $.extend(true,
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
		options);
		
		chartSupport.optionSeries0(chart, options);
		chart.extValue("stackBar", (options && options.stackBar));
		
		chart.echartsInit(options);
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
				series.push($.extend({}, chartSupport.optionSeries0(chart), {name: legendName, stack: "stack-"+i, data: data}));
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
		
		options = $.extend(true,
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
		options);
		
		chartSupport.optionSeries0(chart, options);
		chart.extValue("stackBar", (options && options.stackBar));
		
		chart.echartsInit(options);
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
				series.push($.extend({}, chartSupport.optionSeries0(chart), {name: legendName, stack: "stack-"+i, data: data}));
			}
		}
		
		var options = { legend: {data: legendData}, yAxis : { data : yAxisData }, series: series };
		chart.echartsOptions(options);
	};
})
(this);
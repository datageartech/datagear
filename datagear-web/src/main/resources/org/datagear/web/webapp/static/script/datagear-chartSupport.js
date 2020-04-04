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
	
	chartSupport.lineRender = function(chart, xvalueSign, yvalueSign, options)
	{
		var chartDataSet = chart.chartDataSetFirst();
		var xp = chart.dataSetPropertyOfSign(chartDataSet, xvalueSign);
		var yp = chart.dataSetPropertyOfSign(chartDataSet, yvalueSign);
		
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
	
	chartSupport.lineUpdate = function(chart, results, xvalueSign, yvalueSign)
	{
		var chartDataSets = chart.chartDataSetsNonNull();
		
		var legendData = [];
		var series = [];
		
		for(var i=0; i<chartDataSets.length; i++)
		{
			var chartDataSet = chartDataSets[i];
			var dataSetName = chart.dataSetName(chartDataSet);
			var properties = [chart.dataSetPropertyOfSign(chartDataSet, xvalueSign), chart.dataSetPropertyOfSign(chartDataSet, yvalueSign)];
			var result = chart.resultAt(results, i);
			var data = chart.resultRowArrays(result, properties);
			
			legendData[i] = dataSetName;
			series[i] = $.extend({}, chartSupport.optionSeries0(chart), {name: dataSetName, data: data});
		}
		
		var options = { legend: {data: legendData}, series: series };
		chart.echartsOptions(options);
	};
})
(this);
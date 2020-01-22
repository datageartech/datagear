{
/**
 * 依赖：
 * chartUtil
 * echarts
 */
	render: function(chart)
	{
		chart.echarts = {};
		
		var options =
		{
			xAxis: {
				type: 'category',
				boundaryGap: false,
				data: []
			},
			yAxis: {
				type: 'value',
			},
			series: [{
				data: [],
				type: 'line'
			}]
		};
		
		chart.echarts.chart = chartUtil.echarts.init(chart, options);
	},
	update: function(chart, results)
	{
		var chartDataSet = chartUtil.firstChartDataSet(chart);
		var result = chartUtil.firstResult(results);
		
		var xp = chartUtil.dataSetPropertyOfSign(chartDataSet, "x-value");
		var yp = chartUtil.dataSetPropertyOfSign(chartDataSet, "y-value");
		
		var xdatas = chartUtil.dataPropertyValues(result, xp);
		var ydatas = chartUtil.dataPropertyValues(result, yp);
		
		var options = { xAxis : { data : xdatas }, series : [ { data : ydatas } ] };
		chart.echarts.chart.setOption(options);
	}
}
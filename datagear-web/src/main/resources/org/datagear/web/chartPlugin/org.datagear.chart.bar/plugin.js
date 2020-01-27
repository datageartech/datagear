{
	id : 'org.datagear.chart.bar',
	nameLabel : '柱状图',
	descLabel : '柱状图',
	dataSigns :
	[
		{ name : "x-value", nameLabel : "横坐标", occurRequired: true, occurMultiple: false },
		{ name : "y-value", nameLabel : "纵坐标", occurRequired: true, occurMultiple: true }
	],
	version: "0.1.0",
	order : 1,
	chartRender:
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
					type: 'value'
				},
				series: [{
					data: [],
					type: 'bar'
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
}
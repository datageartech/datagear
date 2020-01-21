/**
 * 依赖：
 * chartUtil
 * echarts
 */
(function(chart)
{
	chart.render = function()
	{
		this.echarts = {};
		
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
		
		this.echarts.chart = chartUtil.echarts.init(this);
		this.echarts.chart.setOption(options);
	};
	
	chart.update = function(results)
	{
		var chartDataSet = chartUtil.firstChartDataSet(this);
		var result = chartUtil.firstResult(results);
		
		var xp = chartUtil.dataSetPropertyOfSign(chartDataSet, "x-value");
		var yp = chartUtil.dataSetPropertyOfSign(chartDataSet, "y-value");
		
		var xdatas = chartUtil.dataPropertyValues(result, xp);
		var ydatas = chartUtil.dataPropertyValues(result, yp);
		
		var options = { xAxis : { data : xdatas }, series : [ { data : ydatas } ] };
		this.echarts.chart.setOption(options);
	};
})
($CHART);
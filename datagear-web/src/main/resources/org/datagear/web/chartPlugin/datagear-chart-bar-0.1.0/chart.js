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
				type: 'value'
			},
			series: [{
				data: [],
				type: 'bar'
			}]
		};
		
		this.echarts.chart = chartUtil.echarts.init(this);
		this.echarts.chart.setOption(options);
	};
	
	chart.update = function(dataSets)
	{
		var dataSet = chartUtil.dataset.first(dataSets);
		var xcolumnMeta = chartUtil.dataset.columnMetaByDimension(dataSet);
		var ycolumnMeta = chartUtil.dataset.columnMetaByScalar(dataSet);
		
		var xdatas = chartUtil.dataset.columnValues(dataSet, xcolumnMeta);
		var ydatas = chartUtil.dataset.columnValues(dataSet, ycolumnMeta);
		
		var options = { xAxis : { data : xdatas }, series : [ { data : ydatas } ] };
		this.echarts.chart.setOption(options);
	};
})
($CHART);
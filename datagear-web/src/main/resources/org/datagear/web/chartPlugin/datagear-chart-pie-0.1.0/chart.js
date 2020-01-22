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
			tooltip:
			{
				trigger: 'item',
				formatter: '{a} <br/>{b} : {c} ({d}%)'
			},
			legend:
			{
				orient: 'vertical',
				left: 'left'
			},
			series:
			[
				{
					name : "",
					type: 'pie',
					radius: '55%',
					center: ['50%', '60%'],
					data: [],
					emphasis:
					{
						itemStyle:
						{
							shadowBlur: 10,
							shadowOffsetX: 0,
							shadowColor: chartUtil.chartTheme(chart).envLeastColor
						}
					}
				}
			]
		};
		
		chart.echarts.chart = chartUtil.echarts.init(chart, options);
	},
	update: function(chart, results)
	{
		var chartDataSet = chartUtil.firstChartDataSet(chart);
		var result = chartUtil.firstResult(results);
		
		var np = chartUtil.dataSetPropertyOfSign(chartDataSet, "name");
		var vp = chartUtil.dataSetPropertyOfSign(chartDataSet, "value");
		
		var legendData = chartUtil.dataPropertyValues(result, np);
		var datas = chartUtil.dataNameValueObjects(result, np, vp);
		
		var options = { legend : { data : legendData }, series : [ { name : chartUtil.propertyValueName(chart), data : datas } ] };
		chart.echarts.chart.setOption(options);
	}
}
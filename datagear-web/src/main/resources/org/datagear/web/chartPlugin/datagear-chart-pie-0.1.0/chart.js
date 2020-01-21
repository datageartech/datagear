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
		
		this.echarts.chart = chartUtil.echarts.init(this);
		this.echarts.chart.setOption(options);
	};
	
	chart.update = function(results)
	{
		var chart = this;
		var chartDataSet = chartUtil.firstChartDataSet(chart);
		var result = chartUtil.firstResult(results);
		
		var np = chartUtil.dataSetPropertyOfSign(chartDataSet, "name");
		var vp = chartUtil.dataSetPropertyOfSign(chartDataSet, "value");
		
		var legendData = chartUtil.dataPropertyValues(result, np);
		var datas = chartUtil.dataNameValueObjects(result, np, vp);
		
		var options = { legend : { data : legendData }, series : [ { name : chartUtil.propertyValueName(chart), data : datas } ] };
		this.echarts.chart.setOption(options);
	};
})
($CHART);
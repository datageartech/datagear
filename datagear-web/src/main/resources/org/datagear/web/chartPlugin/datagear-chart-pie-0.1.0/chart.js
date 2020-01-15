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
	
	chart.update = function(dataSets)
	{
		var chart = this;
		
		var dataSet = chartUtil.dataset.first(dataSets);
		var xcolumnMeta = chartUtil.dataset.columnMetaByDimension(dataSet);
		var ycolumnMeta = chartUtil.dataset.columnMetaByScalar(dataSet);
		
		var legendData = chartUtil.dataset.columnValues(dataSet, xcolumnMeta);
		var datas = chartUtil.dataset.columnNameValues(dataSet, xcolumnMeta, ycolumnMeta);
		
		var options = { legend : { data : legendData }, series : [ { name : chart.name, data : datas } ] };
		this.echarts.chart.setOption(options);
	};
})
($CHART);
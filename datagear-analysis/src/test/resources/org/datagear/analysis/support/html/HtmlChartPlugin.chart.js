(function(chart)
{
	chart.render = function(){ document.getElementById(this.chartElementId).innerHTML = "pie chart"; };
	chart.updateData = function(dataSets){ alert("updateData"); };
})
($CHART);

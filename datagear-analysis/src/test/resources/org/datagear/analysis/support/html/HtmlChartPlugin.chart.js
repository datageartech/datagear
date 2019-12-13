(function(chart)
{
	chart.render = function()
	{
		var element = document.getElementById(this.elementId);
		var innerHtml = "my chart";
		
		if(this.renderContext && this.renderContext.attributes && this.renderContext.attributes.chartTheme)
		{
			var graphColors = (this.renderContext.attributes.chartTheme.graphColors || []);
			
			for(var i=0; i< graphColors.length; i++)
			{
				innerHtml +="<div style='background-color:"+graphColors[i]+";'>&nbsp;</div>";
			}
		}
		
		element.innerHTML = innerHtml;
	};
	
	chart.updateData = function(dataSets){ alert("updateData"); };
})
($CHART);

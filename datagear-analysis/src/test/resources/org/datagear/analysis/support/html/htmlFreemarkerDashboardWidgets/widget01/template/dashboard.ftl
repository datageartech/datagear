<!doctype html>
<html>
<head>
<@import />
<@theme />
<style type="text/css">
.chart-wrapper{
    width: 30%;
    height: 300px;
    display: inline-block;
}
</style>
<script type="text/javascript" src="<@resource name='customResource.js' />"></script>
<script type="text/javascript">
var myListener =
{
	beforeRender : function(dashboard)
	{
		var element = document.getElementById("listener");
		
		element.innerHTML = "<div>beforeRender</div>";
	},
	afterRender : function(dashboard)
	{
		var element = document.getElementById("listener");
		
		element.innerHTML += "<div>afterRender</div>";
	},
	beforeUpdate : function(dashboard)
	{
		var element = document.getElementById("listener");
		
		element.innerHTML += "<div>beforeUpdate</div>";
	},
	afterUpdate : function(dashboard)
	{
		var element = document.getElementById("listener");
		
		element.innerHTML += "<div>afterUpdate</div>";
	}
};
</script>
</head>
<body>
	<@dashboard listener="myListener">
	<div class="dashboard" role="dashboard">
		<h1>Hello dashbaord!</h1>
		<div class="chart-wrapper">
			<@chart widget="chart-widget-01" />
		</div>
		<div class="chart-wrapper">
			<@chart widget="chart-widget-01" />
		</div>
		<div class="chart-wrapper">
			<@chart widget="chart-widget-01" var="chart01" elementId="element01" />
		</div>
		<div class="chart-wrapper">
			<@chart widget="chart-widget-01" />
		</div>
		<div class="chart-wrapper">
			<@chart widget="chart-widget-01" var="chart02" />
		</div>
		<div class="chart-wrapper">
			<@chart widget="chart-widget-01" />
		</div>
	</div>
	</@dashboard>
	<div id="listener"></div>
</body>
</html>
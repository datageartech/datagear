<!doctype html>
<html>
<head>
<@import />
<script type="text/javascript" src="<@resource name='customResource.js' />"></script>
<@dashboard />
</head>
<body>
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
	</div>
</body>
</html>
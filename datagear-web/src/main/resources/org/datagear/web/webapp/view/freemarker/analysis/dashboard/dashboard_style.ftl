<#include "../../include/import_global.ftl">

@CHARSET "UTF-8";

/*表格行*/
.dg-chart-table .dg-chart-table-content table.dataTable tbody tr{
	background: ${chartTheme.backgroundColor};
}

/*表格奇数行*/
.dg-chart-table .dg-chart-table-content table.dataTable.stripe tbody tr.odd,
.dg-chart-table .dg-chart-table-content table.dataTable.display tbody tr.odd{
	background: ${chartTheme.borderColor};
}

/*表格选中、悬浮，拷贝自/src/main/resources/org/datagear/web/webapp/static/theme/lightness/common.css*/
.dg-chart-table .dg-chart-table-content table.dataTable.hover tbody tr.hover,
.dg-chart-table .dg-chart-table-content table.dataTable.hover tbody tr:hover,
.dg-chart-table .dg-chart-table-content table.dataTable.display tbody tr:hover {
	background-color: ${chartTheme.axisScaleLineColor};
}
.dg-chart-table .dg-chart-table-content table.dataTable.hover tbody tr.hover.selected,
.dg-chart-table .dg-chart-table-content table.dataTable tbody > tr.selected,
.dg-chart-table .dg-chart-table-content table.dataTable tbody > tr > .selected,
.dg-chart-table .dg-chart-table-content table.dataTable.stripe tbody > tr.odd.selected,
.dg-chart-table .dg-chart-table-content table.dataTable.stripe tbody > tr.odd > .selected,
.dg-chart-table .dg-chart-table-content table.dataTable.display tbody > tr.odd.selected,
.dg-chart-table .dg-chart-table-content table.dataTable.display tbody > tr.odd > .selected,
.dg-chart-table .dg-chart-table-content table.dataTable.hover tbody > tr.selected:hover,
.dg-chart-table .dg-chart-table-content table.dataTable.hover tbody > tr > .selected:hover,
.dg-chart-table .dg-chart-table-content table.dataTable.display tbody > tr.selected:hover,
.dg-chart-table .dg-chart-table-content table.dataTable.display tbody > tr > .selected:hover {
	background-color: ${chartTheme.highlightTheme.backgroundColor};
	color: ${chartTheme.highlightTheme.color};
}

/*图表设置框和面板*/
.dg-chart-setting-box .dg-chart-setting-button,
.dg-chart-setting-box .dg-chart-setting-panel,
.dg-chart-setting-box .dg-chart-setting-panel .dg-param-value-form-wrapper,
.dg-chart-setting-box .dg-chart-setting-panel .dg-dspv-form input,
.dg-chart-setting-box .dg-chart-setting-panel .dg-dspv-form select,
.dg-chart-setting-box .dg-chart-setting-panel .dg-dspv-form button,
.dg-chart-setting-box .dg-chart-setting-panel .dg-chart-setting-panel-foot button{
	background-color: <#if chartTheme.backgroundColor == "transparent">${dashboardTheme.backgroundColor}<#else>${chartTheme.backgroundColor}</#if>;
	border-color: ${chartTheme.borderColor};
	color: ${chartTheme.color};
}
.dg-chart-setting-box .dg-chart-setting-panel{
	-webkit-box-shadow: 0px 0px 6px ${chartTheme.color};
	box-shadow: 0px 0px 6px ${chartTheme.color};
}
.dg-chart-setting-box .dg-chart-setting-panel .dg-chart-setting-panel-foot button{
	background-color: ${chartTheme.tooltipTheme.backgroundColor};
	border-color: ${chartTheme.tooltipTheme.borderColor};
	color: ${chartTheme.tooltipTheme.color};
}
.dg-chart-setting-box .dg-chart-setting-panel .dg-chart-setting-panel-foot button:hover{
	border-color: ${chartTheme.tooltipTheme.color};
}
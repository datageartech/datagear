<#--
数据集表单页：预览、参数操作区
-->
<div class="workspace-operation-wrapper">
	<ul class="workspace-operation-nav">
		<li class="operation-preview"><a href="#${pageId}-previewResult"><@spring.message code='preview' /></a></li>
		<li class="operation-params"><a href="#${pageId}-dataSetParams"><@spring.message code='dataSet.param' /></a></li>
	</ul>
	<div id="${pageId}-previewResult" class="preview-result-table-wrapper minor-dataTable">
		<div class="operation">
			<button type="button" class="preview-button ui-button ui-corner-all ui-widget ui-button-icon-only" title="<@spring.message code='dataSet.previewButtonTip' />"><span class="ui-button-icon ui-icon ui-icon-play"></span><span class="ui-button-icon-space"> </span><@spring.message code='preview' /></button>
			<button type="button" class="more-button ui-button ui-corner-all ui-widget ui-button-icon-only" title="<@spring.message code='dataSet.loadMoreData' />"><span class="ui-button-icon ui-icon ui-icon-arrowthick-1-s"></span><span class="ui-button-icon-space"> </span><@spring.message code='sqlpad.loadMoreData' /></button>
			<button type="button" class="refresh-button ui-button ui-corner-all ui-widget ui-button-icon-only" title="<@spring.message code='dataSet.refreshSqlResult' />"><span class="ui-button-icon ui-icon ui-icon-refresh"></span><span class="ui-button-icon-space"> </span><@spring.message code='sqlpad.refreshSqlResult' /></button>
			<button type="button" class="export-button ui-button ui-corner-all ui-widget ui-button-icon-only" title="<@spring.message code='sqlpad.exportSqlResult' />"><span class="ui-button-icon ui-icon ui-icon-arrowthick-1-ne"></span><span class="ui-button-icon-space"> </span><@spring.message code='sqlpad.exportSqlResult' /></button>
		</div>
		<table id="${pageId}-previewResult-table" width='100%' class='hover stripe'></table>
		<div class='no-more-data-flag ui-widget ui-widget-content' title="<@spring.message code='dataSet.noMoreData' />"></div>
		<div class="result-resolved-source"><textarea class="ui-widget ui-widget-content ui-corner-all"></textarea></div>
	</div>
	<div id="${pageId}-dataSetParams" class="params-table-wrapper minor-dataTable">
		<div class="operation">
			<#if !readonly>
			<button type="button" class="add-param-button ui-button ui-corner-all ui-widget ui-button-icon-only" title="<@spring.message code='add' />"><span class="ui-button-icon ui-icon ui-icon-plus"></span><span class="ui-button-icon-space"> </span><@spring.message code='add' /></button>
			<button type="button" class="del-param-button ui-button ui-corner-all ui-widget ui-button-icon-only" title="<@spring.message code='delete' />"><span class="ui-button-icon ui-icon ui-icon-close"></span><span class="ui-button-icon-space"> </span><@spring.message code='delete' /></button>
			</#if>
		</div>
		<table id="${pageId}-dataSetParams-table" class='hover stripe'></table>
	</div>
</div>
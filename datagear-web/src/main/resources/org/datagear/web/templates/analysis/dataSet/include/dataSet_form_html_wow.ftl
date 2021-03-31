<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#--
数据集表单页：预览、参数、属性操作区
-->
<div class="workspace-operation-wrapper light-tabs">
	<ul class="workspace-operation-nav">
		<li class="operation-preview"><a href="#${pageId}-previewResult"><@spring.message code='preview' /></a></li>
		<li class="operation-params" title="<@spring.message code='dataSet.param.desc' />"><a href="#${pageId}-dataSetParams"><@spring.message code='dataSet.param' /></a></li>
		<li class="operation-properties" title="<@spring.message code='dataSet.property.desc' />"><a href="#${pageId}-dataSetProperties"><@spring.message code='dataSet.property' /></a></li>
	</ul>
	<div id="${pageId}-previewResult" class="preview-result-table-wrapper minor-dataTable">
		<div class="operation">
			<button type="button" class="preview-button ui-button ui-corner-all ui-widget ui-button-icon-only" title="<@spring.message code='dataSet.previewButtonTip' />"><span class="ui-button-icon ui-icon ui-icon-play"></span><span class="ui-button-icon-space"> </span><@spring.message code='preview' /></button>
			<button type="button" class="refresh-button ui-button ui-corner-all ui-widget ui-button-icon-only" title="<@spring.message code='dataSet.refreshSqlResult' />"><span class="ui-button-icon ui-icon ui-icon-refresh"></span><span class="ui-button-icon-space"> </span><@spring.message code='dataSet.refreshSqlResult' /></button>
		</div>
		<table id="${pageId}-previewResultTable" width='100%' class='hover stripe'></table>
		<div class="preview-result-foot">
			<div class="result-resolved-source">
				<button type="button" class="show-resolved-source-button ui-button ui-corner-all ui-widget ui-button-icon-only" title="<@spring.message code='dataSet.showResolvedSource' />"><span class="ui-button-icon ui-icon ui-icon-comment"></span><span class="ui-button-icon-space"> </span><@spring.message code='dataSet.showResolvedSource' /></button>
				<div class="result-resolved-source-panel ui-widget ui-widget-content ui-corner-all ui-widget-shadow ui-front">
					<div class="result-resolved-source-panel-content">
						<textarea class="ui-widget ui-widget-content"></textarea>
					</div>
				</div>
			</div>
			<div class="result-data-max-count" title="<@spring.message code='dataSet.previewResultDataMaxCount' />">
				<input type="text" class="resultDataMaxCountInput ui-widget ui-widget-content ui-corner-all" />
			</div>
		</div>
	</div>
	<div id="${pageId}-dataSetParams" class="params-table-wrapper minor-dataTable">
		<div class="operation">
			<#if !readonly>
			<button type="button" class="add-param-button ui-button ui-corner-all ui-widget ui-button-icon-only" title="<@spring.message code='add' />"><span class="ui-button-icon ui-icon ui-icon-plus"></span><span class="ui-button-icon-space"> </span><@spring.message code='add' /></button>
			<button type="button" class="up-param-button ui-button ui-corner-all ui-widget ui-button-icon-only" title="<@spring.message code='moveUp' />"><span class="ui-button-icon ui-icon ui-icon-arrowthick-1-n"></span><span class="ui-button-icon-space"> </span><@spring.message code='moveUp' /></button>
			<button type="button" class="down-param-button ui-button ui-corner-all ui-widget ui-button-icon-only" title="<@spring.message code='moveDown' />"><span class="ui-button-icon ui-icon ui-icon-arrowthick-1-s"></span><span class="ui-button-icon-space"> </span><@spring.message code='moveDown' /></button>
			<button type="button" class="del-param-button ui-button ui-corner-all ui-widget ui-button-icon-only" title="<@spring.message code='delete' />"><span class="ui-button-icon ui-icon ui-icon-close"></span><span class="ui-button-icon-space"> </span><@spring.message code='delete' /></button>
			</#if>
		</div>
		<table id="${pageId}-dataSetParamsTable" class='hover stripe'></table>
	</div>
	<div id="${pageId}-dataSetProperties" class="properties-table-wrapper minor-dataTable">
		<div class="operation">
			<#if !readonly>
			<button type="button" class="add-property-button ui-button ui-corner-all ui-widget ui-button-icon-only" title="<@spring.message code='add' />"><span class="ui-button-icon ui-icon ui-icon-plus"></span><span class="ui-button-icon-space"> </span><@spring.message code='add' /></button>
			<button type="button" class="up-property-button ui-button ui-corner-all ui-widget ui-button-icon-only" title="<@spring.message code='moveUp' />"><span class="ui-button-icon ui-icon ui-icon-arrowthick-1-n"></span><span class="ui-button-icon-space"> </span><@spring.message code='moveUp' /></button>
			<button type="button" class="down-property-button ui-button ui-corner-all ui-widget ui-button-icon-only" title="<@spring.message code='moveDown' />"><span class="ui-button-icon ui-icon ui-icon-arrowthick-1-s"></span><span class="ui-button-icon-space"> </span><@spring.message code='moveDown' /></button>
			<button type="button" class="del-property-button ui-button ui-corner-all ui-widget ui-button-icon-only" title="<@spring.message code='delete' />"><span class="ui-button-icon ui-icon ui-icon-close"></span><span class="ui-button-icon-space"> </span><@spring.message code='delete' /></button>
			</#if>
		</div>
		<table id="${pageId}-dataSetPropertiesTable" class='hover stripe'></table>
	</div>
</div>
<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#--
查询表单HTML片段。
-->
<form id="${pageId}-searchForm" class="search-form" action="#" tabindex="0">
	<div class="ui-widget ui-widget-content keyword-widget">
	<span class="ui-icon like-switch-icon ui-icon-radio-off" title="<@spring.message code='data.likeTitle' />"></span><div class="keyword-input-parent"><input name="keyword" type="text" class="ui-widget ui-widget-content keyword-input" tabindex="2" title="<@spring.message code='data.keywordTitle' />" /></div>
	<input type="hidden" name="notLike" value="" />
	</div>
	<div class="search-condition-icon-parent" title="<@spring.message code='data.conditionPanelWithShortcut' />">
		<span class="ui-icon ui-icon-caret-1-s search-condition-icon"></span>
		<span class="ui-icon ui-icon-bullet search-condition-icon-tip"></span>
	</div>
	<button type="submit" class="ui-button ui-corner-all ui-widget" tabindex="3"><@spring.message code='query' /></button>
	<div class="condition-panel-parent">
		<div class="ui-widget ui-widget-content ui-widget-shadow condition-panel" tabindex="0">
			<div class="ui-corner-all ui-widget-header ui-helper-clearfix ui-draggable-handle condition-panel-title-bar">
				<span class="ui-icon ui-icon-arrowthickstop-1-n condition-panel-resetpos-icon" title="<@spring.message code='restoration' />"></span>
			</div>
			<div class="condition-parent">
				<textarea name="condition" tabindex="5" class="ui-widget ui-widget-content" title="<@spring.message code='data.conditionTitle' />"></textarea>
			</div>
			<div class="condition-action">
				<span class="ui-icon ui-icon-trash condition-panel-clear-icon" title="<@spring.message code='data.clearWithShortcut' />"></span>
				<span class="ui-icon ui-icon-search condition-panel-submit-icon" title="<@spring.message code='data.queryWithShortcut' />"></span>
			</div>
		</div>
	</div>
</form>
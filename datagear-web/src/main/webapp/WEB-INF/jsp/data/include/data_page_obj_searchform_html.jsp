<%--
/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%--
查询表单HTML片段。
--%>
<form id="${pageId}-searchForm" class="search-form" action="#" tabindex="0">
	<div class="ui-widget ui-widget-content keyword-widget">
	<span class="ui-icon like-switch-icon ui-icon-radio-off" title="<fmt:message key='data.likeTitle' />"></span><div class="keyword-input-parent"><input name="keyword" type="text" class="ui-widget ui-widget-content keyword-input" tabindex="2" /></div>
	<input type="hidden" name="notLike" value="" />
	</div>
	<div class="search-condition-icon-parent" title="<fmt:message key='data.conditionPanelWithShortcut' />">
		<span class="ui-icon ui-icon-caret-1-s search-condition-icon"></span>
		<span class="ui-icon ui-icon-bullet search-condition-icon-tip"></span>
	</div>
	<button type="submit" class="ui-button ui-corner-all ui-widget" tabindex="3"><fmt:message key='query' /></button>
	<div class="condition-panel-parent">
		<div class="ui-widget ui-widget-content ui-widget-shadow condition-panel" tabindex="0">
			<div class="ui-corner-all ui-widget-header ui-helper-clearfix ui-draggable-handle condition-panel-title-bar">
				<span class="ui-icon ui-icon-arrowthickstop-1-n condition-panel-resetpos-icon" title="<fmt:message key='restoration' />"></span>
			</div>
			<div class="condition-parent">
				<textarea name="condition" tabindex="5" class="ui-widget ui-widget-content"></textarea>
			</div>
			<div class="condition-action">
				<span class="ui-icon ui-icon-trash condition-panel-clear-icon" title="<fmt:message key='data.clearWithShortcut' />"></span>
				<span class="ui-icon ui-icon-search condition-panel-submit-icon" title="<fmt:message key='data.queryWithShortcut' />"></span>
			</div>
		</div>
	</div>
</form>
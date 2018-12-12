<%--
/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="org.datagear.web.util.WebUtils"%>
<%--
编辑表格功能HTML片段。
--%>
<div class="edit-grid">
	<div class="edit-grid-switch-wrapper">
		<label for="${pageId}-editGridSwitch"><fmt:message key='editGrid' /></label>
		<input id="${pageId}-editGridSwitch" type="checkbox" value="1" />
	</div>
	<div class="edit-grid-operation">
		<button type="button" class="button-cancel highlight" style="display: none;"><fmt:message key='restore' /></button>
		<button type="button" class="button-cancel-all highlight" style="display: none;"><fmt:message key='restoreAll' /></button>
		<button type="button" class="button-save recommended" style="display: none;"><fmt:message key='save' /></button>
	</div>
</div>
<%
String editGridFormPageId_html = WebUtils.generatePageId();
request.setAttribute("editGridFormPageId", editGridFormPageId_html);
%>
<div id="<%=editGridFormPageId_html%>" class="edit-cell-form-panel ui-widget ui-widget-content ui-corner-all ui-widget-shadow">
	<form id="<%=editGridFormPageId_html%>-form" method="POST" action="#">
	</form>
</div>
<%--
/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%--
查询表单HTML片段。
--%>
<form id="${pageId}-searchForm" class="search-form" action="#">
	<div class="ui-widget ui-widget-content keyword-widget simple">
		<div class="keyword-input-parent">
			<input name="keyword" type="text" class="ui-widget ui-widget-content keyword-input" />
		</div>
	</div>
	<input name="submit" type="submit" value="<fmt:message key='query' />" />
</form>

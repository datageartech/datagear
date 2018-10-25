<%--
/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="../../include/page_obj_grid.jsp" %>
<script type="text/javascript">
(function(pageObj)
{
	/**
	 * 构建Model的本地表格。
	 */
	pageObj.initModelDataTableLocal = function(model, data, ignorePropertyNames)
	{
		var columns = $.buildDataTablesColumns(model, {"ignorePropertyNames" : ignorePropertyNames});
		var settings = pageObj.buildDataTableSettingsLocal(columns, data);
		
		pageObj.initDataTable(settings);
	};
	
	/**
	 * 构建Model的ajax表格。
	 */
	pageObj.initModelDataTableAjax = function(url, model, ignorePropertyNames)
	{
		var columns = $.buildDataTablesColumns(model, {"ignorePropertyNames" : ignorePropertyNames});
		var settings = pageObj.buildDataTableSettingsAjax(columns, url);
		
		pageObj.initDataTable(settings);
	};
})
(${pageId});
</script>

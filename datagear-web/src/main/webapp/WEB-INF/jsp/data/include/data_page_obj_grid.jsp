<%--
/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="../../include/page_obj_grid.jsp" %>
<script type="text/javascript">
(function(po)
{
	/**
	 * 构建Model的本地表格。
	 */
	po.initModelDataTableLocal = function(model, data, ignorePropertyNames)
	{
		var columns = $.buildDataTablesColumns(model, {"ignorePropertyNames" : ignorePropertyNames});
		var settings = po.buildDataTableSettingsLocal(columns, data);
		
		po.initDataTable(settings);
	};
	
	/**
	 * 构建Model的ajax表格。
	 */
	po.initModelDataTableAjax = function(url, model, ignorePropertyNames)
	{
		var columns = $.buildDataTablesColumns(model, {"ignorePropertyNames" : ignorePropertyNames});
		var settings = po.buildDataTableSettingsAjax(columns, url);
		
		po.initDataTable(settings);
	};
})
(${pageId});
</script>

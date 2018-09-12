<%--
/*
 * Copyright (c) 2018 by datagear.org.
 */
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="../../include/page_obj_grid.jsp" %>
<script type="text/javascript">
(function(pageObj)
{
	pageObj.initModelTable = function(model, datas, ignorePropertyNames)
	{
		var tableColumns = $.buildDataTablesColumns(model, {"ignorePropertyNames" : ignorePropertyNames});
		var tableSettings = pageObj.getTableSettings(tableColumns, datas);
		
		pageObj.initTable(tableSettings);
	};
})
(${pageId});
</script>

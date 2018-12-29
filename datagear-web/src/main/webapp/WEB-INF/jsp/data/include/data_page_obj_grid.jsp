<%--
/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.sql.Types"%>
<%@ include file="../../include/page_obj_grid.jsp" %>
<script type="text/javascript">
(function(po)
{
	po.queryLeftClobLengthOnReading = <%=request.getAttribute("queryLeftClobLengthOnReading")%>;
	
	//单元基本属性值是否已完全获取，如果不是单元基本属性，也将返回true（为了提高表格数据读取效率，后台对CLOB类的属性值仅会读取前段）
	po.isSinglePrimitivePropertyValueFullyFetched = function(model, property, propertyValue)
	{
		if(propertyValue == null)
			return true;
		
		if(po.queryLeftClobLengthOnReading == null || po.queryLeftClobLengthOnReading < 0)
			return true;
		
		var re = true;
		
		var propertyModelIndex = $.model.getPropertyModelIndexByValue(property, propertyValue);
		var jdbcType = $.model.featureJdbcTypeValue(property, propertyModelIndex);
		
		if(<%=Types.CLOB%> == jdbcType || <%=Types.NCLOB%> == jdbcType
				|| <%=Types.LONGNVARCHAR%> == jdbcType || <%=Types.LONGVARCHAR%> == jdbcType)
			re = (propertyValue.length < po.queryLeftClobLengthOnReading);
		
		return re;
	};
	
	//所有单元属性值是否已完全获取
	po.isAllSinglePrimitivePropertyValueFullyFetched = function(model, data)
	{
		if(!data)
			return true;
		
		var properties = model.properties;
		
		for(var i=0; i<properties.length; i++)
		{
			var property = properties[i];
			var propertyValue = $.model.propertyValue(data, property);
			
			if(!po.isSinglePrimitivePropertyValueFullyFetched(model, property, propertyValue))
				return false;
		}
		
		return true;
	};
	
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

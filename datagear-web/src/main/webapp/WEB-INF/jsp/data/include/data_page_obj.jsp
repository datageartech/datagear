<%--
/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="../../include/page_js_obj.jsp" %>
<script type="text/javascript">
(function(pageObj)
{
	var dataPageObj =
	{
		//当前模式ID
		schemaId : "${schema.id}",
		
		//当前模型名称
		tableName : "${tableName}",

		/**
		 * 生成指定操作的URL。
		 *
		 * @param tableName 可选，操作对应的表名，默认是当前表名
		 * @param action 操作名称
		 */
		url : function(tableName, action)
		{
			if(!action)
			{
				action = tableName;
				tableName = this.tableName;
			}
			
			return contextPath + $.toPath("data", this.schemaId, tableName, action);
		},
		
		/**
		 * 对指定表名的模型执行操作。
		 *
		 * @param tableName 可选，操作对应的表名，默认是当前表名
		 * @param callback 操作函数，格式为：function(model){ ... }
		 */
		onModel : function(tableName, callback)
		{
			if(!callback)
			{
				callback = tableName;
				tableName = this.tableName;
			}
			
			$.model.on(this.schemaId, tableName, callback);
		}
	};
	
	$.extend(pageObj, dataPageObj);
})
(${pageId});
</script>

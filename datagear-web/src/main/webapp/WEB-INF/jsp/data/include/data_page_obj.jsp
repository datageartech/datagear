<%--
/*
 * Copyright (c) 2018 by datagear.org.
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
		modelName : "${model.name}",

		/**
		 * 生成指定操作的URL。
		 *
		 * @param modelName 可选，操作对应的模型名，默认是当前模型名
		 * @param action 操作名称
		 */
		url : function(modelName, action)
		{
			if(!action)
			{
				action = modelName;
				modelName = this.modelName;
			}
			
			return contextPath + "/data/" + this.schemaId + "/" + modelName + "/" + action;
		},
		
		/**
		 * 对指定名称的模型执行操作。
		 *
		 * @param modelName 可选，操作对应的模型名，默认是当前模型名
		 * @param callback 操作函数，格式为：function(model){ ... }
		 */
		onModel : function(modelName, callback)
		{
			if(!callback)
			{
				callback = modelName;
				modelName = this.modelName;
			}
			
			$.model.on(this.schemaId, modelName, callback);
		}
	};
	
	$.extend(pageObj, dataPageObj);
})
(${pageId});
</script>

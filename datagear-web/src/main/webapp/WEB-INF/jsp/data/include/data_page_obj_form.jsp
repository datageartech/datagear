<%--
/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */
--%>
<%--
数据表单操作页面公用代码
依赖：data_page_obj.jsp

依赖变量：
//初始数据，由主页面定义，允许为null
pageObj.data = undefined;
//表单元素，由主页面定义，不允许为null
pageObj.form = undefined;
//是否是客户端操作，由主页面定义，允许为null
pageObj.clientOperation = undefined;
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<script type="text/javascript">
(function(pageObj)
{
	pageObj.formLabels =
	{
		add : "<fmt:message key='add' />",
		edit : "<fmt:message key='edit' />",
		del : "<fmt:message key='delete' />",
		view : "<fmt:message key='view' />",
		select : "<fmt:message key='select' />",
		submit : "<fmt:message key='save' />",
		reset : "<fmt:message key='reset' />",
		batchSet :
		{
			batchSetSwitchTitle : "<fmt:message key='batchSet.batchSetSwitchTitle' />",
			batchCount : "<fmt:message key='batchSet.batchCount' />",
			batchHandleErrorMode : "<fmt:message key='batchSet.batchHandleErrorMode' />",
			batchHandleErrorModeEnum : ["<fmt:message key='batchSet.batchHandleErrorMode.ignore' />", "<fmt:message key='batchSet.batchHandleErrorMode.abort' />", "<fmt:message key='batchSet.batchHandleErrorMode.rollback' />"]
		},
		validation :
		{
			required : "<fmt:message key='validation.required' />"
		}
	};
	
	var pageParam = pageObj.pageParam();
	if(pageParam && pageParam.submit)
		pageObj.formLabels.submit = "<fmt:message key='confirm' />";
	
	pageObj.propertySubmitHandler = function(property, propertyConcreteModel, propValue)
	{
		pageObj.form.modelform("propValue", property.name, propValue);
		
		if(pageObj.data)
			$.model.propertyValue(pageObj.data, property.name, propValue);
	};
	
	pageObj.propertyAfterSaveHandler = function(property, propertyConcreteModel, propValue)
	{
		pageObj.form.modelform("propValue", property.name, propValue);
		
		if(!pageObj.data)
			pageObj.data = {};
		
		$.model.propertyValue(pageObj.data, property.name, propValue);
	};
	
	//属性操作选项函数
	pageObj.buildPropertyActionOptions = function(property, propertyConcreteModel, extraRequestParams, extraPageParams)
	{
		var requestParams = { "data" : pageObj.data, "propertyPath" : $.propertyPath.escapePropertyName(property.name), "clientOperation" : pageObj.clientOperation };
		if(extraRequestParams)
			$.extend(requestParams, extraRequestParams);
		
		//单元属性值都不即时保存，作为客户端操作
		var clientOperation = (!$.model.isMultipleProperty(property) ? true : pageObj.clientOperation);
		
		var pageParams = {};
		
		if(clientOperation)
		{
			$.extend(pageParams,
			{
				"submit" : function(propValue)
				{
					pageObj.propertySubmitHandler(property, propertyConcreteModel, propValue);
				}
			});
		}
		else
		{
			$.extend(pageParams,
			{
				"afterSave" : function(propValue)
				{
					pageObj.propertyAfterSaveHandler(property, propertyConcreteModel, propValue);
				}
			});
		}
		
		if(extraPageParams)
			$.extend(pageParams, extraPageParams);
		
		var actionParam =
		{
			"data" : requestParams,
			"pageParam" : pageParams
		}
		
		return actionParam;
	};
	
	pageObj.addSinglePropertyValue = function(property, propertyConcreteModel)
	{
		var options = pageObj.buildPropertyActionOptions(property, propertyConcreteModel);
		options.pinTitleButton=true;
		
		pageObj.open(pageObj.url("addSinglePropValue"), options);
	};
	
	pageObj.editSinglePropertyValue = function(property, propertyConcreteModel)
	{
		var options = pageObj.buildPropertyActionOptions(property, propertyConcreteModel);
		options.pinTitleButton=true;
		
		pageObj.open(pageObj.url("editSinglePropValue"), options);
	};
	
	pageObj.deleteSinglePropertyValue = function(property, propertyConcreteModel)
	{
		pageObj.propertySubmitHandler(property, propertyConcreteModel);
	};
	
	pageObj.selectSinglePropertyValue = function(property, propertyConcreteModel)
	{
		var options = pageObj.buildPropertyActionOptions(property, propertyConcreteModel);
		$.setGridPageHeightOption(options);
		pageObj.open(pageObj.url("selectPropValue"), options);
	};
	
	pageObj.viewSinglePropertyValue = function(property, propertyConcreteModel)
	{
		var options = pageObj.buildPropertyActionOptions(property, propertyConcreteModel);
		pageObj.open(pageObj.url("viewSinglePropValue"), options);
	};
	
	pageObj.editMultiplePropertyValue = function(property, propertyConcreteModel)
	{
		var options = pageObj.buildPropertyActionOptions(property, propertyConcreteModel);
		$.setGridPageHeightOption(options);
		pageObj.open(pageObj.url("editMultiplePropValue"), options);
	};
	
	pageObj.viewMultiplePropertyValue = function(property, propertyConcreteModel)
	{
		var options = pageObj.buildPropertyActionOptions(property, propertyConcreteModel);
		$.setGridPageHeightOption(options);
		pageObj.open(pageObj.url("viewMultiplePropValue"), options);
	};
	
	pageObj.downloadSinglePropertyValueFile = function(property, propertyConcreteModel)
	{
		var options = pageObj.buildPropertyActionOptions(property, propertyConcreteModel);
		options.target="_file";
		
		pageObj.open(pageObj.url("downloadSinglePropertyValueFile"), options);
	};
})
(${pageId});
</script>

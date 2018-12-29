<%--
/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */
--%>
<%--
数据表单操作页面公用代码
依赖：
data_page_obj.jsp
jsp_method_get_string_value.jsp

依赖变量：
//初始数据，由主页面定义，允许为null
po.data = undefined;
//初始表单数据是否是客户端数据
po.isClientPageData = undefined;
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="org.datagear.web.util.WebUtils"%>
<%@ include file="../../include/page_obj_form.jsp" %>
<script type="text/javascript">
(function(po)
{
	po.formLabels =
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
	
	//如果页面参数里定义了提交回调函数，则标签改为“确定”
	if(po.pageParam("submit"))
		po.formLabels.submit = "<fmt:message key='confirm' />";
	
	po.propertySubmitHandler = function(property, propertyConcreteModel, propValue)
	{
		po.form().modelform("propValue", property.name, propValue);
		
		if(po.data)
			$.model.propertyValue(po.data, property.name, propValue);
	};
	
	po.propertyAfterSaveHandler = function(property, propertyConcreteModel, propValue)
	{
		po.form().modelform("propValue", property.name, propValue);
		
		if(!po.data)
			po.data = {};
		
		$.model.propertyValue(po.data, property.name, propValue);
	};
	
	po.isPropertyActionClientSubmit = function(property, propertyConcreteModel)
	{
		//单元属性值都不即时保存
		return (!$.model.isMultipleProperty(property) ? true : po.isClientPageData);
	};
	
	//属性操作选项函数
	po.buildPropertyActionOptions = function(property, propertyConcreteModel, extraRequestParams, extraPageParams)
	{
		var requestParams =
		{
			"data" : po.data,
			"propertyPath" : $.propertyPath.escapePropertyName(property.name),
			"isClientPageData" : po.isClientPageData
		};
		
		if(extraRequestParams)
			$.extend(requestParams, extraRequestParams);
		
		//单元属性值都不即时保存
		var clientSubmit = po.isPropertyActionClientSubmit(property, propertyConcreteModel);
		
		var pageParams = {};
		
		if(clientSubmit)
		{
			$.extend(pageParams,
			{
				"submit" : function(propValue)
				{
					po.propertySubmitHandler(property, propertyConcreteModel, propValue);
				}
			});
		}
		else
		{
			$.extend(pageParams,
			{
				"afterSave" : function(propValue)
				{
					po.propertyAfterSaveHandler(property, propertyConcreteModel, propValue);
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
	
	po.addSinglePropertyValue = function(property, propertyConcreteModel)
	{
		var options = po.buildPropertyActionOptions(property, propertyConcreteModel);
		options.pinTitleButton=true;
		
		po.open(po.url("addSinglePropValue"), options);
	};
	
	po.editSinglePropertyValue = function(property, propertyConcreteModel)
	{
		var options = po.buildPropertyActionOptions(property, propertyConcreteModel);
		options.pinTitleButton=true;
		
		po.open(po.url("editSinglePropValue"), options);
	};
	
	po.deleteSinglePropertyValue = function(property, propertyConcreteModel)
	{
		po.propertySubmitHandler(property, propertyConcreteModel);
	};
	
	po.selectSinglePropertyValue = function(property, propertyConcreteModel)
	{
		var options = po.buildPropertyActionOptions(property, propertyConcreteModel);
		$.setGridPageHeightOption(options);
		po.open(po.url("selectPropValue"), options);
	};
	
	po.viewSinglePropertyValue = function(property, propertyConcreteModel)
	{
		var options = po.buildPropertyActionOptions(property, propertyConcreteModel);
		po.open(po.url("viewSinglePropValue"), options);
	};
	
	po.editMultiplePropertyValue = function(property, propertyConcreteModel)
	{
		var options = po.buildPropertyActionOptions(property, propertyConcreteModel);
		$.setGridPageHeightOption(options);
		po.open(po.url("editMultiplePropValue"), options);
	};
	
	po.viewMultiplePropertyValue = function(property, propertyConcreteModel)
	{
		var options = po.buildPropertyActionOptions(property, propertyConcreteModel);
		$.setGridPageHeightOption(options);
		po.open(po.url("viewMultiplePropValue"), options);
	};
	
	po.downloadSinglePropertyValueFile = function(property, propertyConcreteModel)
	{
		var options = po.buildPropertyActionOptions(property, propertyConcreteModel);
		options.target="_file";
		
		po.open(po.url("downloadSinglePropertyValueFile"), options);
	};
	
	po.refreshParent = function()
	{
		var poParent = po.parent();
		if(poParent && poParent.refresh && $.isFunction(poParent.refresh))
		{
			try
			{
				poParent.refresh();
			}
			catch(e){}
		}
	};
})
(${pageId});
</script>

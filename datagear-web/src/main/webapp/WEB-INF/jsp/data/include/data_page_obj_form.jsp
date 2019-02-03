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
		//如果页面参数里定义了提交回调函数，则submit标签改为“确定”
		submit : (po.pageParam("submit") ? "<fmt:message key='confirm' />" : "<fmt:message key='save' />"),
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
	
	po.propertySubmitHandler = function(property, propertyModel, propValue)
	{
		po.form().modelform("propValue", property.name, propValue);
	};
	
	po.propertyAfterSaveHandler = function(property, propertyModel, propValue)
	{
		po.form().modelform("propValue", property.name, propValue);
		
		if(!po.data)
			po.data = {};
		
		$.model.propertyValue(po.data, property.name, propValue);
	};
	
	po.propertyDataTableAjaxSuccess = function(property, propertyModel, propertyValue, propertyValuePagingData)
	{
		if(!$.model.isMultipleProperty(property))
			return;
		
		var formPropertyValue = po.form().modelform("propValue", property.name);
		if(formPropertyValue == null || $.model.isSizeOnlyCollection(formPropertyValue))
			po.form().modelform("propValue", property.name, $.model.toSizeOnlyCollection(propertyValuePagingData.total));
		
		if(!po.data)
			po.data = {};
		
		var propertyValue = $.model.propertyValue(po.data, property.name);
		if(propertyValue == null || $.model.isSizeOnlyCollection(propertyValue))
			$.model.propertyValue(po.data, property.name, $.model.toSizeOnlyCollection(propertyValuePagingData.total));
	};
	
	po.isPropertyActionClientSubmit = function(property, propertyModel)
	{
		//单元属性值都不即时保存
		return (!$.model.isMultipleProperty(property) ? true : po.isClientPageData);
	};
	
	//属性操作选项函数
	po.buildPropertyActionOptions = function(property, propertyModel, propertyValue, extraRequestParams, extraPageParams)
	{
		var requestParams =
		{
			//如果页面是客户端数据则传递最新表单数据，因为不需要根据初始数据到服务端数据库查找
			"data" : (po.isClientPageData ? po.form().modelform("data") : po.data),
			"propertyPath" : $.propertyPath.escapePropertyName(property.name),
			"propertyValue" : (po.isClientPageData ? null : propertyValue),
			"isClientPageData" : po.isClientPageData
		};
		
		var pageParams = {};
		
		if(extraRequestParams)
			$.extend(requestParams, extraRequestParams);
		
		//单元属性值都不即时保存
		var clientSubmit = po.isPropertyActionClientSubmit(property, propertyModel);
		
		if(clientSubmit)
		{
			$.extend(pageParams,
			{
				"submit" : function(propertyValue)
				{
					po.propertySubmitHandler(property, propertyModel, propertyValue);
				}
			});
		}
		else
		{
			$.extend(pageParams,
			{
				"afterSave" : function(propertyValue)
				{
					po.propertyAfterSaveHandler(property, propertyModel, propertyValue);
				},
				"dataTableAjaxSuccess" : function(propertyValuePagingData)
				{
					po.propertyDataTableAjaxSuccess(property, propertyModel, propertyValue, propertyValuePagingData);
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
	
	po.addSinglePropertyValue = function(property, propertyModel)
	{
		var options = po.buildPropertyActionOptions(property, propertyModel);
		options.pinTitleButton=true;
		
		if(po.isClientPageData)
			po.open(po.url("addSinglePropValue"), options);
		else
			//服务端数据始终使用editSinglePropValue请求，因为受SelectOptions的影响，页面数据对象超过级联的属性值没有加载，无法判断是add还是edit
			po.open(po.url("editSinglePropValue"), options);
	};
	
	po.editSinglePropertyValue = function(property, propertyModel, propertyValue)
	{
		var options = po.buildPropertyActionOptions(property, propertyModel, propertyValue);
		options.pinTitleButton=true;
		
		po.open(po.url("editSinglePropValue"), options);
	};
	
	po.deleteSinglePropertyValue = function(property, propertyModel, propertyValue)
	{
		po.propertySubmitHandler(property, propertyModel, propertyValue);
	};
	
	po.selectSinglePropertyValue = function(property, propertyModel, propertyValue)
	{
		var options = po.buildPropertyActionOptions(property, propertyModel, propertyValue);
		$.setGridPageHeightOption(options);
		po.open(po.url("selectPropValue"), options);
	};
	
	po.viewSinglePropertyValue = function(property, propertyModel, propertyValue)
	{
		var options = po.buildPropertyActionOptions(property, propertyModel, propertyValue);
		po.open(po.url("viewSinglePropValue"), options);
	};
	
	po.editMultiplePropertyValue = function(property, propertyModel, propertyValue)
	{
		var options = po.buildPropertyActionOptions(property, propertyModel, propertyValue);
		$.setGridPageHeightOption(options);
		po.open(po.url("editMultiplePropValue"), options);
	};
	
	po.viewMultiplePropertyValue = function(property, propertyModel, propertyValue)
	{
		var options = po.buildPropertyActionOptions(property, propertyModel, propertyValue);
		$.setGridPageHeightOption(options);
		po.open(po.url("viewMultiplePropValue"), options);
	};
	
	po.downloadSinglePropertyValueFile = function(property, propertyModel)
	{
		var options = po.buildPropertyActionOptions(property, propertyModel);
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

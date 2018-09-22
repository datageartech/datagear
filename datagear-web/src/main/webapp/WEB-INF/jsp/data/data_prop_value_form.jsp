<%--
/*
 * Copyright (c) 2018 by datagear.org.
 */
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="../include/jsp_import.jsp" %>
<%@ include file="../include/jsp_ajax_request.jsp" %>
<%@ include file="../include/jsp_jstl.jsp" %>
<%@ include file="../include/jsp_page_id.jsp" %>
<%@ include file="../include/jsp_method_get_string_value.jsp" %>
<%@ include file="../include/jsp_method_write_json.jsp" %>
<%@ include file="include/data_jsp_define.jsp" %>
<%@ include file="../include/html_doctype.jsp" %>
<%
//初始数据，允许null
Object data = request.getAttribute("data");
//属性名称，不允许null
String propName = getStringValue(request, "propName");
//标题操作标签I18N关键字，不允许null
String titleOperationMessageKey = getStringValue(request, "titleOperationMessageKey");
//提交活动，pageObj.pageParam().submit(...)未定义时，不允许为null
String submitAction = getStringValue(request, "submitAction");
//是否是客户端操作，允许为null
boolean clientOperation = ("true".equalsIgnoreCase(getStringValue(request, "clientOperation")));
//是否只读操作，允许为null
boolean readonly = ("true".equalsIgnoreCase(getStringValue(request, "readonly")));

PropertyPath propertyPath = ModelUtils.toPropertyPath(propName);
PropertyPathInfo propertyPathInfo = ModelUtils.toPropertyPathInfoConcrete(model, propertyPath, data);
boolean isPrivatePropertyModel = ModelUtils.isPrivatePropertyModelTail(propertyPathInfo);
%>
<html>
<head>
<%@ include file="../include/html_head.jsp" %>
<title><%@ include file="../include/html_title_app_name.jsp" %><fmt:message key='<%=titleOperationMessageKey%>' /><fmt:message key='titleSeparator' /><%=ModelUtils.getNameLabelValuePath(model, propertyPath, WebUtils.getLocale(request))%></title>
</head>
<body>
<div id="${pageId}" class="page-data-form page-data-form-propvalue">
	<div class="head">
	</div>
	<div class="content">
		<form id="${pageId}-form" method="POST">
		</form>
	</div>
	<div class="foot">
	</div>
</div>
<%@ include file="include/data_page_obj.jsp" %>
<%@ include file="include/data_page_obj_form.jsp" %>
<script type="text/javascript">
(function(pageObj)
{
	pageObj.form = pageObj.element("#${pageId}-form");
	
	pageObj.readonly = <%=readonly%>;
	pageObj.submitAction = "<%=submitAction%>";
	pageObj.data = ($.unref(<%writeJson(application, out, data);%>) || {});
	pageObj.propName = "<%=propName%>";
	pageObj.clientOperation = <%=clientOperation%>;
	
	pageObj.superBuildPropertyActionOptions = pageObj.buildPropertyActionOptions;
	pageObj.buildPropertyActionOptions = function(property, propertyConcreteModel, extraRequestParams, extraPageParams)
	{
		var actionParam = pageObj.superBuildPropertyActionOptions(property, propertyConcreteModel, extraRequestParams, extraPageParams);
		
		actionParam["data"]["propName"] = pageObj.propName + "." + property.name;
		actionParam["data"]["data"] = pageObj.data;
		
		//客户端操作则传递最新表单数据，因为不需要到服务端数据库查找验证
		if(pageObj.clientOperation)
			$.model.propValue(actionParam["data"]["data"], pageObj.propName, pageObj.form.modelform("data")); 
		
		return actionParam;
	};
	
	pageObj.onModel(function(model)
	{
		var propertyInfo = $.model.getTailPropertyInfoConcrete(model, pageObj.propName);
		var property = propertyInfo.property;
		var propertyModel = propertyInfo.model;
		
		pageObj.form.modelform(
		{
			model : propertyModel,
			ignorePropertyNames : $.model.findMappedByWith(property, propertyModel),
			data : $.model.propValue(pageObj.data, pageObj.propName),
			readonly : pageObj.readonly,
			submit : function()
			{
				var propValue = $(this).modelform("data");
				
				var close = true;
				
				var pageParam = pageObj.pageParam();
				
				//父页面定义了submit回调函数，则优先执行
				if(pageParam && pageParam.submit)
				{
					close = (pageParam.submit(propValue) != false);

					if(close && !$(this).modelform("isDialogPinned"))
						pageObj.close();
				}
				//否则，POST至后台
				else
				{
					var thisForm = this;
					var param = { "data" : pageObj.data, "propName" : pageObj.propName, "propValue" : propValue };
					
					$.post(pageObj.url(pageObj.submitAction), param, function(operationMessage)
					{
						//如果有初始数据，则更新为已保存至后台的数据
						if(pageObj.data)
							$.model.propValue(pageObj.data, pageObj.propName, operationMessage.data);
						
						if(pageParam && pageParam.afterSave)
							close = (pageParam.afterSave(operationMessage.data) != false);
						
						var pageObjParent = pageObj.parent();
						if(pageObjParent && pageObjParent.refresh)
							pageObjParent.refresh();
						
						if(close && !$(thisForm).modelform("isDialogPinned"))
							pageObj.close();
					}, "json");
				}
				
				return false;
			},
			addSinglePropertyValue : function(property, propertyConcreteModel)
			{
				pageObj.addSinglePropertyValue(property, propertyConcreteModel);
			},
			editSinglePropertyValue : function(property, propertyConcreteModel)
			{
				pageObj.editSinglePropertyValue(property, propertyConcreteModel);
			},
			deleteSinglePropertyValue : function(property, propertyConcreteModel)
			{
				pageObj.deleteSinglePropertyValue(property, propertyConcreteModel);
			},
			selectSinglePropertyValue : function(property, propertyConcreteModel)
			{
				pageObj.selectSinglePropertyValue(property, propertyConcreteModel);
			},
			viewSinglePropertyValue : function(property, propertyConcreteModel)
			{
				pageObj.viewSinglePropertyValue(property, propertyConcreteModel);
			},
			editMultiplePropertyValue : function(property, propertyConcreteModel)
			{
				pageObj.editMultiplePropertyValue(property, propertyConcreteModel);
			},
			viewMultiplePropertyValue : function(property, propertyConcreteModel)
			{
				pageObj.viewMultiplePropertyValue(property, propertyConcreteModel);
			},
			filePropertyUploadURL : "<c:url value='/data/file/upload' />",
			filePropertyDeleteURL : "<c:url value='/data/file/delete' />",
			downloadSinglePropertyValueFile : function(property, propertyConcreteModel)
			{
				pageObj.downloadSinglePropertyValueFile(property, propertyConcreteModel);
			},
			validationRequiredAsAdd : ("saveAdd" == pageObj.submitAction),
			labels : pageObj.formLabels,
			dateFormat : "<c:out value='${dateFormat}' />",
			timestampFormat : "<c:out value='${sqlTimestampFormat}' />",
			timeFormat : "<c:out value='${sqlTimeFormat}' />"
		});
	});
})
(${pageId});
</script>
</body>
</html>

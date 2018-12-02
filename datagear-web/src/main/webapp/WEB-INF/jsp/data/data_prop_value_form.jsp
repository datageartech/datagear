<%--
/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
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
String propertyPath = getStringValue(request, "propertyPath");
//标题操作标签I18N关键字，不允许null
String titleOperationMessageKey = getStringValue(request, "titleOperationMessageKey");
//提交活动，pageObj.pageParam().submit(...)未定义时，不允许为null
String submitAction = getStringValue(request, "submitAction");
//是否是客户端操作，允许为null
boolean clientOperation = ("true".equalsIgnoreCase(getStringValue(request, "clientOperation")));
//是否只读操作，允许为null
boolean readonly = ("true".equalsIgnoreCase(getStringValue(request, "readonly")));
//是否开启批量执行功能，允许为null
boolean batchSet = ("true".equalsIgnoreCase(getStringValue(request, "batchSet")));

PropertyPath propertyPathObj = ModelUtils.toPropertyPath(propertyPath);
PropertyPathInfo propertyPathInfoObj = ModelUtils.toPropertyPathInfoConcrete(model, propertyPathObj, data);
boolean isPrivatePropertyModel = ModelUtils.isPrivatePropertyModelTail(propertyPathInfoObj);
%>
<html>
<head>
<%@ include file="../include/html_head.jsp" %>
<title>
	<%@ include file="../include/html_title_app_name.jsp" %>
	<fmt:message key='<%=titleOperationMessageKey%>' />
	<fmt:message key='titleSeparator' />
	<%=WebUtils.escapeHtml(ModelUtils.displayName(model, propertyPathObj, WebUtils.getLocale(request)))%>
</title>
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
	pageObj.propertyPath = "<%=WebUtils.escapeJavaScriptStringValue(propertyPath)%>";
	pageObj.clientOperation = <%=clientOperation%>;
	pageObj.batchSet = <%=batchSet%>;
	
	pageObj.superBuildPropertyActionOptions = pageObj.buildPropertyActionOptions;
	pageObj.buildPropertyActionOptions = function(property, propertyConcreteModel, extraRequestParams, extraPageParams)
	{
		var actionParam = pageObj.superBuildPropertyActionOptions(property, propertyConcreteModel, extraRequestParams, extraPageParams);
		
		actionParam["data"]["propertyPath"] = $.propertyPath.concatPropertyName(pageObj.propertyPath, property.name);
		actionParam["data"]["data"] = pageObj.data;
		
		//客户端操作则传递最新表单数据，因为不需要到服务端数据库查找验证
		if(pageObj.clientOperation)
			$.model.propertyPathValue(actionParam["data"]["data"], pageObj.propertyPath, pageObj.form.modelform("data")); 
		
		return actionParam;
	};
	
	pageObj.onModel(function(model)
	{
		var propertyInfo = $.model.getTailPropertyInfoConcrete(model, pageObj.propertyPath);
		var property = propertyInfo.property;
		var propertyModel = propertyInfo.model;
		
		pageObj.form.modelform(
		{
			model : propertyModel,
			ignorePropertyNames : $.model.findMappedByWith(property, propertyModel),
			data : $.model.propertyPathValue(pageObj.data, pageObj.propertyPath),
			readonly : pageObj.readonly,
			submit : function()
			{
				var propValue = $(this).modelform("data");
				var formParam = $(this).modelform("param");
				
				var close = true;
				
				var pageParam = pageObj.pageParam();
				
				//父页面定义了submit回调函数，则优先执行
				if(pageParam && pageParam.submit)
				{
					close = (pageParam.submit(propValue, formParam) != false);

					if(close && !$(this).modelform("isDialogPinned"))
						pageObj.close();
				}
				//否则，POST至后台
				else
				{
					var thisForm = this;
					var param = $.extend(formParam, { "data" : pageObj.data, "propertyPath" : pageObj.propertyPath, "propValue" : propValue });
					
					$(thisForm).modelform("disableOperation");
					
					$.ajax(pageObj.url(pageObj.submitAction), 
					{
						"data" : param,
						"success" : function(operationMessage)
						{
							var $form = $(thisForm);
							var batchSubmit = $form.modelform("isBatchSubmit");
							var isDialogPinned = $form.modelform("isDialogPinned");
							
							if(batchSubmit)
								;
							else
							{
								//如果有初始数据，则更新为已保存至后台的数据
								if(pageObj.data)
									$.model.propertyPathValue(pageObj.data, pageObj.propertyPath, operationMessage.data);
								
								if(pageParam && pageParam.afterSave)
									close = (pageParam.afterSave(operationMessage.data) != false);
								
								var pageObjParent = pageObj.parent();
								if(pageObjParent && pageObjParent.refresh)
									pageObjParent.refresh();
								
								if(close && !isDialogPinned)
									pageObj.close();
							}
						},
						"dataType" : "json",
						"complete" : function()
						{
							var $form = $(thisForm);
							$form.modelform("enableOperation");
							
							var batchSubmit = $form.modelform("isBatchSubmit");
							
							if(batchSubmit)
							{
								var pageObjParent = pageObj.parent();
								if(pageObjParent && pageObjParent.refresh)
									pageObjParent.refresh();
							}
						}
					});
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
			batchSet : pageObj.batchSet,
			labels : pageObj.formLabels,
			dateFormat : "<c:out value='${sqlDateFormat}' />",
			timestampFormat : "<c:out value='${sqlTimestampFormat}' />",
			timeFormat : "<c:out value='${sqlTimeFormat}' />"
		});
	});
})
(${pageId});
</script>
</body>
</html>

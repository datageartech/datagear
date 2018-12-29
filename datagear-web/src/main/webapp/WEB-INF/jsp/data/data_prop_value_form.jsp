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
//初始属性值，可用于设置初始表单数据，允许为null
Object propertyValue = request.getAttribute("propertyValue");
//初始属性值数据是否是客户端数据，默认为false
boolean isClientPageData = ("true".equalsIgnoreCase(getStringValue(request, "isClientPageData")));
//标题操作标签I18N关键字，不允许null
String titleOperationMessageKey = getStringValue(request, "titleOperationMessageKey");
//提交活动，po.pageParam().submit(...)未定义时，不允许为null
String submitAction = getStringValue(request, "submitAction");
//是否只读操作，默认为false
boolean readonly = ("true".equalsIgnoreCase(getStringValue(request, "readonly")));
//是否开启批量执行功能，默认为false
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
<div id="${pageId}" class="page-form page-form-propvalue">
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
(function(po)
{
	po.readonly = <%=readonly%>;
	po.submitAction = "<%=submitAction%>";
	po.data = ($.unref(<%writeJson(application, out, data);%>) || {});
	po.propertyValue = $.unref(<%writeJson(application, out, propertyValue);%>);
	po.propertyPath = "<%=WebUtils.escapeJavaScriptStringValue(propertyPath)%>";
	po.isClientPageData = <%=isClientPageData%>;
	po.batchSet = <%=batchSet%>;
	
	po.superBuildPropertyActionOptions = po.buildPropertyActionOptions;
	po.buildPropertyActionOptions = function(property, propertyConcreteModel, extraRequestParams, extraPageParams)
	{
		var actionParam = po.superBuildPropertyActionOptions(property, propertyConcreteModel, extraRequestParams, extraPageParams);
		
		actionParam["data"]["propertyPath"] = $.propertyPath.concatPropertyName(po.propertyPath, property.name);
		actionParam["data"]["data"] = po.data;
		
		//客户端属性值数据则传递最新表单数据，因为不需要根据初始属性值数据到服务端数据库查找
		if(po.isClientPageData)
			$.model.propertyPathValue(actionParam["data"]["data"], po.propertyPath, po.form().modelform("data")); 
		
		return actionParam;
	};
	
	po.onModel(function(model)
	{
		var propertyInfo = $.model.getTailPropertyInfoConcrete(model, po.propertyPath);
		var property = propertyInfo.property;
		var propertyModel = propertyInfo.model;
		
		po.form().modelform(
		{
			model : propertyModel,
			ignorePropertyNames : $.model.findMappedByWith(property, propertyModel),
			data : (po.propertyValue || $.model.propertyPathValue(po.data, po.propertyPath)),
			readonly : po.readonly,
			submit : function()
			{
				var propValue = $(this).modelform("data");
				var formParam = $(this).modelform("param");
				
				var close = true;
				
				//父页面定义了submit回调函数，则优先执行
				if(po.pageParam("submit"))
				{
					close = (po.pageParamCall("submit", propValue, formParam) != false);
					
					if(close && !$(this).modelform("isDialogPinned"))
						po.close();
				}
				//否则，POST至后台
				else
				{
					var thisForm = this;
					var param = $.extend(formParam, { "data" : po.data, "propertyPath" : po.propertyPath, "propValue" : propValue });
					
					po.ajaxSubmitForHandleDuplication(po.submitAction, param, "<fmt:message key='save.continueIgnoreDuplicationTemplate' />",
					{
						beforeSend : function()
						{
							$(thisForm).modelform("disableOperation");
						},
						success : function(operationMessage)
						{
							var $form = $(thisForm);
							var batchSubmit = $form.modelform("isBatchSubmit");
							var isDialogPinned = $form.modelform("isDialogPinned");
							
							$form.modelform("enableOperation");

							po.refreshParent();
							
							if(batchSubmit)
								;
							else
							{
								//如果有初始数据，则更新为已保存至后台的数据
								if(po.data)
									$.model.propertyPathValue(po.data, po.propertyPath, operationMessage.data);
								
								close = (po.pageParamCall("afterSave", operationMessage.data) != false);
								
								if(close && !isDialogPinned)
									po.close();
							}
						},
						error : function()
						{
							var $form = $(thisForm);
							var batchSubmit = $form.modelform("isBatchSubmit");

							$form.modelform("enableOperation");
							
							if(batchSubmit)
								po.refreshParent();
						}
					});
				}
				
				return false;
			},
			addSinglePropertyValue : function(property, propertyConcreteModel)
			{
				po.addSinglePropertyValue(property, propertyConcreteModel);
			},
			editSinglePropertyValue : function(property, propertyConcreteModel)
			{
				po.editSinglePropertyValue(property, propertyConcreteModel);
			},
			deleteSinglePropertyValue : function(property, propertyConcreteModel)
			{
				po.deleteSinglePropertyValue(property, propertyConcreteModel);
			},
			selectSinglePropertyValue : function(property, propertyConcreteModel)
			{
				po.selectSinglePropertyValue(property, propertyConcreteModel);
			},
			viewSinglePropertyValue : function(property, propertyConcreteModel)
			{
				po.viewSinglePropertyValue(property, propertyConcreteModel);
			},
			editMultiplePropertyValue : function(property, propertyConcreteModel)
			{
				po.editMultiplePropertyValue(property, propertyConcreteModel);
			},
			viewMultiplePropertyValue : function(property, propertyConcreteModel)
			{
				po.viewMultiplePropertyValue(property, propertyConcreteModel);
			},
			filePropertyUploadURL : "<c:url value='/data/file/upload' />",
			filePropertyDeleteURL : "<c:url value='/data/file/delete' />",
			downloadSinglePropertyValueFile : function(property, propertyConcreteModel)
			{
				po.downloadSinglePropertyValueFile(property, propertyConcreteModel);
			},
			validationRequiredAsAdd : ("saveAdd" == po.submitAction),
			batchSet : po.batchSet,
			labels : po.formLabels,
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

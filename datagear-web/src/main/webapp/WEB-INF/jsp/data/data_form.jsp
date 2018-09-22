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
//标题操作标签I18N关键字，不允许null
String titleOperationMessageKey = getStringValue(request, "titleOperationMessageKey");
//提交活动，pageObj.pageParam().submit(...)未定义时，不允许为null
String submitAction = getStringValue(request, "submitAction");
//是否是客户端操作，允许为null
boolean clientOperation = ("true".equalsIgnoreCase(getStringValue(request, "clientOperation")));
//是否只读操作，允许为null
boolean readonly = ("true".equalsIgnoreCase(getStringValue(request, "readonly")));
//忽略表单渲染和处理的属性名，允许为null
String ignorePropertyName = getStringValue(request, "ignorePropertyName", "");
%>
<html>
<head>
<%@ include file="../include/html_head.jsp" %>
<title><%@ include file="../include/html_title_app_name.jsp" %><fmt:message key='<%=titleOperationMessageKey%>' /><fmt:message key='titleSeparator' /><%=ModelUtils.getNameLabelValue(model, WebUtils.getLocale(request))%></title>
</head>
<body>
<div id="${pageId}" class="page-data-form">
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
	pageObj.originalData = $.unref(<%writeJson(application, out, data);%>);
	pageObj.data = $.unref($.ref(pageObj.originalData));
	pageObj.clientOperation = <%=clientOperation%>;
	
	pageObj.superBuildPropertyActionOptions = pageObj.buildPropertyActionOptions;
	pageObj.buildPropertyActionOptions = function(property, propertyConcreteModel, extraRequestParams, extraPageParams)
	{
		var actionParam = pageObj.superBuildPropertyActionOptions(property, propertyConcreteModel, extraRequestParams, extraPageParams);
		
		//客户端操作则传递最新表单数据，因为不需要到服务端数据库查找验证
		if(pageObj.clientOperation)
			actionParam["data"]["data"] = pageObj.form.modelform("data");
		
		return actionParam;
	};
	
	pageObj.onModel(function(model)
	{
		pageObj.form.modelform(
		{
			model : model,
			ignorePropertyNames : "<%=ignorePropertyName%>",
			data : pageObj.data,
			readonly : pageObj.readonly,
			submit : function()
			{
				var data = $(this).modelform("data");
				
				var pageParam = pageObj.pageParam();
				
				var close = true;
				
				//父页面定义了submit回调函数，则优先执行
				if(pageParam && pageParam.submit)
				{
					close = (pageParam.submit(data) != false);
					
					if(close && !$(this).modelform("isDialogPinned"))
						pageObj.close();
				}
				//否则，POST至后台
				else
				{
					var thisForm = this;
					var param = {"data" : data, "originalData" : pageObj.originalData};
					
					$.post(pageObj.url(pageObj.submitAction), param, function(operationMessage)
					{
						pageObj.data = $.unref(operationMessage.data);
						//如果有初始数据，则更新为已保存至后台的数据
						//注意：不能直接赋值pageObj.data，因为是同一个引用，有可能会被修改，而pageObj.originalData不应该被修改
						if(pageObj.originalData)
							pageObj.originalData = $.unref($.ref(operationMessage.data));
						
						if(pageParam && pageParam.afterSave)
							close = (pageParam.afterSave(operationMessage.data) != false);
						
						var pageObjParent = pageObj.parent();
						if(pageObjParent && pageObjParent.refresh)
							pageObjParent.refresh();
						
						if(close && !$(thisForm).modelform("isDialogPinned"))
							pageObj.close();
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

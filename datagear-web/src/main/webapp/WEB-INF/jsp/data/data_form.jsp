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
//初始数据是否是客户端数据，默认为false
boolean isClientPageData = ("true".equalsIgnoreCase(getStringValue(request, "isClientPageData")));
//标题操作标签I18N关键字，不允许null
String titleOperationMessageKey = getStringValue(request, "titleOperationMessageKey");
//提交活动，po.pageParam().submit(...)未定义时，不允许为null
String submitAction = getStringValue(request, "submitAction");
//是否只读操作，默认为false
boolean readonly = ("true".equalsIgnoreCase(getStringValue(request, "readonly")));
//忽略表单渲染和处理的属性名，默认为""
String ignorePropertyName = getStringValue(request, "ignorePropertyName", "");
//是否开启批量执行功能，默认为false
boolean batchSet = ("true".equalsIgnoreCase(getStringValue(request, "batchSet")));
%>
<html>
<head>
<%@ include file="../include/html_head.jsp" %>
<title>
	<%@ include file="../include/html_title_app_name.jsp" %>
	<fmt:message key='<%=titleOperationMessageKey%>' />
	<fmt:message key='titleSeparator' />
	<%=WebUtils.escapeHtml(ModelUtils.displayName(model, WebUtils.getLocale(request)))%>
</title>
</head>
<body>
<div id="${pageId}" class="page-form">
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
	po.data = $.unref(<%writeJson(application, out, data);%>);
	po.readonly = <%=readonly%>;
	po.submitAction = "<%=submitAction%>";
	po.isClientPageData = <%=isClientPageData%>;
	po.batchSet = <%=batchSet%>;
	
	if(!po.isClientPageData && po.data == null)
		po.isClientPageData = true;
	
	po.onModel(function(model)
	{
		po.form().modelform(
		{
			model : model,
			ignorePropertyNames : "<%=WebUtils.escapeJavaScriptStringValue(ignorePropertyName)%>",
			//不能直接使用po.data，因为po.data作为原始数据，不应该被表单编辑变更
			data : $.deepClone(po.data),
			readonly : po.readonly,
			submit : function()
			{
				var formData = $(this).modelform("data");
				var formParam = $(this).modelform("param");
				
				var close = true;
				
				//父页面定义了submit回调函数，则优先执行
				if(po.pageParam("submit"))
				{
					close = (po.pageParamCall("submit", formData, formParam) != false);
					
					if(close && !$(this).modelform("isDialogPinned"))
						po.close();
				}
				//否则，POST至后台
				else
				{
					var thisForm = this;
					var param = $.extend(formParam, {"data" : formData, "originalData" : po.data});
					
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
								//如果有初值，则更新为后台已保存值（编辑时）；如果没有初值，则不更新（添加时）
								if(po.data)
									po.data = operationMessage.data;
								
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
			editSinglePropertyValue : function(property, propertyConcreteModel, propertyValue)
			{
				po.editSinglePropertyValue(property, propertyConcreteModel, propertyValue);
			},
			deleteSinglePropertyValue : function(property, propertyConcreteModel, propertyValue)
			{
				po.deleteSinglePropertyValue(property, propertyConcreteModel, propertyValue);
			},
			selectSinglePropertyValue : function(property, propertyConcreteModel, propertyValue)
			{
				po.selectSinglePropertyValue(property, propertyConcreteModel, propertyValue);
			},
			viewSinglePropertyValue : function(property, propertyConcreteModel, propertyValue)
			{
				po.viewSinglePropertyValue(property, propertyConcreteModel, propertyValue);
			},
			editMultiplePropertyValue : function(property, propertyConcreteModel, propertyValue)
			{
				po.editMultiplePropertyValue(property, propertyConcreteModel, propertyValue);
			},
			viewMultiplePropertyValue : function(property, propertyConcreteModel, propertyValue)
			{
				po.viewMultiplePropertyValue(property, propertyConcreteModel, propertyValue);
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
			timeFormat : "<c:out value='${sqlTimeFormat}' />",
			filePropertyLabelValue : "<c:out value='${filePropertyLabelValue}' />"
		});
	});
})
(${pageId});
</script>
</body>
</html>

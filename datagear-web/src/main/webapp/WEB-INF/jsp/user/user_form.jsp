<%--
/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="org.datagear.web.controller.UserController" %>
<%@ include file="../include/jsp_import.jsp" %>
<%@ include file="../include/jsp_ajax_request.jsp" %>
<%@ include file="../include/jsp_jstl.jsp" %>
<%@ include file="../include/jsp_page_id.jsp" %>
<%@ include file="../include/jsp_method_get_string_value.jsp" %>
<%@ include file="../include/html_doctype.jsp" %>
<%
//标题标签I18N关键字，不允许null
String titleMessageKey = getStringValue(request, UserController.KEY_TITLE_MESSAGE_KEY);
//表单提交action，允许为null
String formAction = getStringValue(request, UserController.KEY_FORM_ACTION, "#");
//是否只读操作，允许为null
boolean readonly = ("true".equalsIgnoreCase(getStringValue(request, UserController.KEY_READONLY)));

boolean isAdd = "saveAdd".equals(formAction);
%>
<html>
<head>
<%@ include file="../include/html_head.jsp" %>
<title><%@ include file="../include/html_title_app_name.jsp" %><fmt:message key='<%=titleMessageKey%>' /></title>
</head>
<body>
<div id="${pageId}" class="page-form page-form-user">
	<form id="${pageId}-form" action="<%=request.getContextPath()%>/user/<%=formAction%>" method="POST">
		<div class="form-head"></div>
		<div class="form-content">
			<input type="hidden" name="id" value="<c:out value='${user.id}' />" />
			<div class="form-item">
				<div class="form-item-label">
					<label><fmt:message key='user.name' /></label>
				</div>
				<div class="form-item-value">
					<input type="text" name="name" value="<c:out value='${user.name}' />" class="ui-widget ui-widget-content" />
				</div>
			</div>
			<%if(!readonly){%>
			<div class="form-item">
				<div class="form-item-label">
					<label><fmt:message key='user.password' /></label>
				</div>
				<div class="form-item-value">
					<input type="password" name="password" value="" class="ui-widget ui-widget-content" />
				</div>
			</div>
			<div class="form-item">
				<div class="form-item-label">
					<label><fmt:message key='user.confirmPassword' /></label>
				</div>
				<div class="form-item-value">
					<input type="password" name="confirmPassword" value="" class="ui-widget ui-widget-content" />
				</div>
			</div>
			<%}%>
			<div class="form-item">
				<div class="form-item-label">
					<label><fmt:message key='user.realName' /></label>
				</div>
				<div class="form-item-value">
					<input type="text" name="realName" value="<c:out value='${user.realName}' />" class="ui-widget ui-widget-content" />
				</div>
			</div>
			<div class="form-item">
				<div class="form-item-label">
					<label><fmt:message key='user.email' /></label>
				</div>
				<div class="form-item-value">
					<input type="text" name="email" value="<c:out value='${user.email}' />" class="ui-widget ui-widget-content" />
				</div>
			</div>
			<%--禁用新建管理员账号功能
			<div class="form-item">
				<div class="form-item-label">
					<label><fmt:message key='user.admin' /></label>
				</div>
				<div class="form-item-value">
					<div class="user-admin-radios">
					<label for="${pageId}-userAdminYes"><fmt:message key='yes' /></label>
		   			<input type="radio" id="${pageId}-userAdminYes" name="admin" value="1" <c:if test='${user.admin}'>checked="checked"</c:if> />
					<label for="${pageId}-userAdminNo"><fmt:message key='no' /></label>
		   			<input type="radio" id="${pageId}-userAdminNo" name="admin" value="0" <c:if test='${!user.admin}'>checked="checked"</c:if> />
		   			</div>
				</div>
			</div>
			--%>
		</div>
		<div class="form-foot" style="text-align:center;">
			<%if(!readonly){%>
			<input type="submit" value="<fmt:message key='save' />" class="recommended" />
			&nbsp;&nbsp;
			<input type="reset" value="<fmt:message key='reset' />" />
			<%}%>
		</div>
	</form>
</div>
<%@ include file="../include/page_js_obj.jsp" %>
<%@ include file="../include/page_obj_form.jsp" %>
<script type="text/javascript">
(function(po)
{
	$.initButtons(po.element());
	
	<%--禁用新建管理员账号功能
	po.element("input[name='admin']").checkboxradio({icon:false});
	po.element(".user-admin-radios").controlgroup();
	--%>
	
	po.url = function(action)
	{
		return contextPath + "/user/" + action;
	};
	
	<%if(!readonly){%>
	
	po.form().validate(
	{
		rules :
		{
			name : "required",
			<%if(isAdd){%>
			password : "required",
			<%}%>
			confirmPassword :
			{
				<%if(isAdd){%>
				"required" : true,
				<%}%>
				"equalTo" : po.element("input[name='password']")
			},
			email : "email"
		},
		messages :
		{
			name : "<fmt:message key='validation.required' />",
			<%if(isAdd){%>
			password : "<fmt:message key='validation.required' />",
			<%}%>
			confirmPassword :
			{
				<%if(isAdd){%>
				"required" : "<fmt:message key='validation.required' />",
				<%}%>
				"equalTo" : "<fmt:message key='user.validation.confirmPasswordError' />"
			},
			email : "<fmt:message key='validation.email' />"
		},
		submitHandler : function(form)
		{
			$(form).ajaxSubmit(
			{
				success : function()
				{
					var close = (po.pageParamCall("afterSave")  != false);
					
					if(close)
						po.close();
				}
			});
		},
		errorPlacement : function(error, element)
		{
			error.appendTo(element.closest(".form-item-value"));
		}
	});
	<%}%>
})
(${pageId});
</script>
</body>
</html>
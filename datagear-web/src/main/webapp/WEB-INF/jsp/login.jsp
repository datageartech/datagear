<%--
/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="org.springframework.security.core.Authentication" %>
<%@ page import="org.springframework.security.core.AuthenticationException" %>
<%@ page import="org.springframework.security.web.WebAttributes" %>
<%@ include file="include/jsp_import.jsp" %>
<%@ include file="include/jsp_ajax_request.jsp" %>
<%@ include file="include/jsp_jstl.jsp" %>
<%@ include file="include/jsp_method_get_string_value.jsp" %>
<%@ include file="include/jsp_page_id.jsp" %>
<%@ include file="include/html_doctype.jsp" %>
<%
String loginUser = (String)session.getAttribute(org.datagear.web.controller.RegisterController.SESSION_KEY_REGISTER_USER_NAME);

//参考org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler.saveException()
AuthenticationException authenticationException = (AuthenticationException)request.getSession().getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
if(authenticationException != null)
{
	request.getSession().removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
	
	Authentication authentication =  authenticationException.getAuthentication();
	
	if(authentication != null && authentication.getPrincipal() != null)
		loginUser = authentication.getPrincipal().toString();
}

if(loginUser == null)
	loginUser = "";
%>
<html>
<head>
<%@ include file="include/html_head.jsp" %>
<title><%@ include file="include/html_title_app_name.jsp" %><fmt:message key='login.login' /></title>
</head>
<body>
<div id="${pageId}">
	<div class="main-page-head">
		<%@ include file="include/html_logo.jsp" %>
		<div class="toolbar">
			<c:if test='${!disableRegister}'>
			<a class="link" href="<c:url value="/register" />"><fmt:message key='register.register' /></a>
			</c:if>
			<a class="link" href="<c:url value="/" />"><fmt:message key='backToMainPage' /></a>
		</div>
	</div>
	<div class="page-form page-form-login">
		<form id="${pageId}-form" action="<c:url value="/login/doLogin" />" method="POST">
			<div class="form-head"></div>
			<div class="form-content">
				<div class="form-item">
					<div class="form-item-label">
						<label><fmt:message key='login.username' /></label>
					</div>
					<div class="form-item-value">
						<input type="text" name="name" value="<%=loginUser%>" class="ui-widget ui-widget-content" />
					</div>
				</div>
				<div class="form-item">
					<div class="form-item-label">
						<label><fmt:message key='login.password' /></label>
					</div>
					<div class="form-item-value">
						<input type="password" name="password" value="" class="ui-widget ui-widget-content" />
					</div>
				</div>
			</div>
			<div class="form-foot" style="text-align:center;">
				<input type="submit" class="recommended" value="<fmt:message key='login.login' />" />
				&nbsp;&nbsp;
				<input type="reset" value="<fmt:message key='reset' />" />
			</div>
			<div class="form-foot small-text" style="text-align:right;">
				<label for="auto-login-checkbox"><fmt:message key='login.autoLogin' /></label>
	   			<input type="checkbox" id="auto-login-checkbox" name="autoLogin" value="1" checked="checked" />
	   			<a class="link" href="<c:url value='/resetPassword' />"><fmt:message key='login.fogetPassword' /></a>
			</div>
		</form>
	</div>
</div>
<%@ include file="include/page_js_obj.jsp" %>
<%@ include file="include/page_obj_form.jsp" %>
<script type="text/javascript">
(function(po)
{
	//需要先渲染按钮，不然对话框尺寸不合适，出现滚动条
	$.initButtons(po.element());
	//元素设置了“checked='checked'”后icon显示有问题，这里先隐藏
	$("input[type=checkbox]", po.element()).checkboxradio({icon:false});
	
	var dialog=po.element(".page-form").dialog({
		appendTo: po.element(),
		title: "<fmt:message key='login.login' />",
		position: {my : "center top", at : "center top+75"},
		resizable: false,
		draggable: true,
		width: "41%",
		beforeClose: function(){ return false; }
	});
	
	po.form().validate(
	{
		rules :
		{
			name : "required",
			password : "required"
		},
		messages :
		{
			name : "<fmt:message key='validation.required' />",
			password : "<fmt:message key='validation.required' />"
		},
		errorPlacement : function(error, element)
		{
			error.appendTo(element.closest(".form-item-value"));
		}
	});
	
	$(".ui-dialog .ui-dialog-titlebar-close", dialog.widget).hide();
	
	<%if(authenticationException != null){%>
	$(document).ready(function()
	{
		$.tipError("<fmt:message key='login.userNameOrPasswordError' />");
	});
	<%}%>
})
(${pageId});
</script>
</body>
</html>
<%--
/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="include/jsp_import.jsp" %>
<%@ include file="include/jsp_ajax_request.jsp" %>
<%@ include file="include/jsp_jstl.jsp" %>
<%@ include file="include/jsp_page_id.jsp" %>
<%@ include file="include/html_doctype.jsp" %>
<html>
<head>
<%@ include file="include/html_head.jsp" %>
<title><%@ include file="include/html_title_app_name.jsp" %><fmt:message key='register.register' /></title>
</head>
<body>
<div id="${pageId}">
	<div class="main-page-head">
		<%@ include file="include/html_logo.jsp" %>
		<div class="toolbar">
			<a class="link" href="<c:url value="/login" />"><fmt:message key='login.login' /></a>
			<a class="link" href="<c:url value="/" />"><fmt:message key='backToMainPage' /></a>
		</div>
	</div>
	<div class="page-form page-form-register">
		<form id="${pageId}-form" action="<c:url value="/register/doRegister" />" method="POST">
			<div class="form-head"></div>
			<div class="form-content">
				<div class="form-item">
					<div class="form-item-label">
						<label><fmt:message key='register.name' /></label>
					</div>
					<div class="form-item-value">
						<input type="text" name="name" value="" class="ui-widget ui-widget-content" />
					</div>
				</div>
				<div class="form-item">
					<div class="form-item-label">
						<label><fmt:message key='register.password' /></label>
					</div>
					<div class="form-item-value">
						<input type="password" name="password" value="" class="ui-widget ui-widget-content" />
					</div>
				</div>
				<div class="form-item">
					<div class="form-item-label">
						<label><fmt:message key='register.confirmPassword' /></label>
					</div>
					<div class="form-item-value">
						<input type="password" name="confirmPassword" value="" class="ui-widget ui-widget-content" />
					</div>
				</div>
				<div class="form-item">
					<div class="form-item-label">
						<label><fmt:message key='register.realName' /></label>
					</div>
					<div class="form-item-value">
						<input type="text" name="realName" value="" class="ui-widget ui-widget-content" />
					</div>
				</div>
				<div class="form-item">
					<div class="form-item-label">
						<label><fmt:message key='register.email' /></label>
					</div>
					<div class="form-item-value">
						<input type="text" name="email" value="" class="ui-widget ui-widget-content" />
					</div>
				</div>
			</div>
			<div class="form-foot" style="text-align:center;">
				<input type="submit" class="recommended" value="<fmt:message key='register.register' />" />
				&nbsp;&nbsp;
				<input type="reset" value="<fmt:message key='reset' />" />
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
		title: "<fmt:message key='register.register' />",
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
			password : "required",
			confirmPassword : { "required" : true, "equalTo" : po.element("input[name='password']") },
			email : "email"
		},
		messages :
		{
			name : "<fmt:message key='validation.required' />",
			password : "<fmt:message key='validation.required' />",
			confirmPassword :
			{
				"required" : "<fmt:message key='validation.required' />",
				"equalTo" : "<fmt:message key='register.validation.confirmPasswordError' />"
			},
			email : "<fmt:message key='validation.email' />"
		},
		submitHandler : function(form)
		{
			$(form).ajaxSubmit(
			{
				success : function()
				{
					window.location.href="<c:url value='/register/success' />";
				}
			});
		},
		errorPlacement : function(error, element)
		{
			error.appendTo(element.closest(".form-item-value"));
		}
	});
	
	$(".ui-dialog .ui-dialog-titlebar-close", dialog.widget).hide();
})
(${pageId});
</script>
</body>
</html>
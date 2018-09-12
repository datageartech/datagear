<%--
/*
 * Copyright (c) 2018 by datagear.org.
 */
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="org.datagear.web.controller.ResetPasswordController" %>
<%@ page import="org.datagear.web.controller.ResetPasswordController.ResetPasswordStep" %>
<%@ include file="include/jsp_import.jsp" %>
<%@ include file="include/jsp_ajax_request.jsp" %>
<%@ include file="include/jsp_jstl.jsp" %>
<%@ include file="include/jsp_page_id.jsp" %>
<%@ include file="include/jsp_method_get_string_value.jsp" %>
<%@ include file="include/html_doctype.jsp" %>
<%!
protected String getStepClass(ResetPasswordStep currentStep, int step)
{
	if(currentStep.isAfter(step))
		return " ui-state-default";
	else if(currentStep.isStep(step))
		return " ui-state-active";
	else
		return " ui-state-disabled";
}
%>
<%
ResetPasswordStep step = (ResetPasswordStep)session.getAttribute(ResetPasswordController.KEY_STEP);
String formAction = request.getContextPath() + "/resetPassword/" + step.getAction();

String loginUrl = request.getContextPath() + "/login"; 
%>
<html>
<head>
<%@ include file="include/html_head.jsp" %>
<%if(step.isFinalStep() && !step.isSkipCheckUserAdmin()){%>
<meta http-equiv="refresh" content="4;url=<%=loginUrl%>">
<%}%>
<title><fmt:message key='resetPassword.resetPassword' /><%@ include file="include/html_title_app_name.jsp" %></title>
</head>
<body>
<div id="${pageId}">
	<div class="main-page-head main-page-head-reset-passord">
		<div class="toolbar">
			<a class="link" href="javascript:void(0);" id="viewResetPasswordAdminReqHistoryLink"><fmt:message key='resetPassword.viewResetPasswordAdminReqHistory' /></a>
			<a class="link" href="<c:url value="/login" />"><fmt:message key='resetPassword.backToLoginPage' /></a>
			<a class="link" href="<c:url value="/" />"><fmt:message key='backToMainPage' /></a>
		</div>
	</div>
	<div class="page-data-form page-data-form-reset-password">
		<div class="head">
			<fmt:message key='resetPassword.resetPassword' />
		</div>
		<div class="content">
			<div class="steps">
				<div class="step ui-widget ui-widget-content ui-corner-all <%=getStepClass(step, 1)%>"><fmt:message key='resetPassword.step.fillInUserInfo' /></div>
				<div class="step ui-widget ui-widget-content ui-corner-all <%=getStepClass(step, 2)%>"><fmt:message key='resetPassword.step.checkUser' /></div>
				<div class="step ui-widget ui-widget-content ui-corner-all <%=getStepClass(step, 3)%>"><fmt:message key='resetPassword.step.setNewPassword' /></div>
				<div class="step ui-widget ui-widget-content ui-corner-all <%=getStepClass(step, 4)%>"><fmt:message key='resetPassword.step.finish' /></div>
			</div>
			<form id="${pageId}-form" action="<%=formAction%>">
				<div class="ui-widget ui-widget-content ui-corner-all form-content">
					<%if(step.isStep(1)){%>
					<div class="form-item">
						<div class="form-item-label">
							<label><fmt:message key='resetPassword.username' /></label>
						</div>
						<div class="form-item-value">
							<input type="text" name="username" value="" class="ui-widget ui-widget-content" />
						</div>
					</div>
					<%}else if(step.isStep(2)){%>
					<div class="form-item">
						<div class="form-item-label">
							<label><fmt:message key='resetPassword.username' /></label>
						</div>
						<div class="form-item-value">
							<input type="text" value="<%=step.getUser().getName()%>" class="ui-widget ui-widget-content" readonly="readonly" />
						</div>
					</div>
					<div class="form-item">
						<div class="form-item-label">
							<label><fmt:message key='resetPassword.email' /></label>
						</div>
						<div class="form-item-value">
							<input type="text" value="<%=step.getBlurryEmail()%>" class="ui-widget ui-widget-content" readonly="readonly" />
							<button id="sendCheckCodeButton" type="button"><fmt:message key='resetPassword.sendCheckCode' /></button>
						</div>
					</div>
					<div class="form-item">
						<div class="form-item-label">
							<label><fmt:message key='resetPassword.checkcode' /></label>
						</div>
						<div class="form-item-value">
							<input type="text" name="checkCode" value="" class="ui-widget ui-widget-content" />
						</div>
					</div>
					<%}else if(step.isStep(3)){%>
					<div class="form-item">
						<div class="form-item-label">
							<label><fmt:message key='resetPassword.username' /></label>
						</div>
						<div class="form-item-value">
							<input type="text" value="<%=step.getUser().getName()%>" class="ui-widget ui-widget-content" readonly="readonly" />
						</div>
					</div>
					<div class="form-item">
						<div class="form-item-label">
							<label><fmt:message key='resetPassword.password' /></label>
						</div>
						<div class="form-item-value">
							<input type="password" name="password" value="" class="ui-widget ui-widget-content" />
						</div>
					</div>
					<div class="form-item">
						<div class="form-item-label">
							<label><fmt:message key='resetPassword.confirmPassword' /></label>
						</div>
						<div class="form-item-value">
							<input type="password" name="confirmPassword" value="" class="ui-widget ui-widget-content" />
						</div>
					</div>
					<%if(step.isSkipCheckUserAdmin()){%>
					<div class="form-item">
						<div class="ui-state-highlight ui-corner-all skip-check-user-admin-warn">
							<span class="ui-icon ui-icon-info"></span>
							<fmt:message key='resetPassword.setNewPassword.skipCheckUserAdminWarn'>
								<fmt:param value='<%=step.getSkipPasswordDelayHours()%>' />
							</fmt:message>
						</div>
					</div>
					<%}%>
					<%}else if(step.isFinalStep()){%>
					<div class="step-finish-content">
						<span class="ui-icon ui-icon-check"></span>
						<%if(step.isSkipCheckUserAdmin()){%>
						<fmt:message key='resetPassword.step.finish.content.skipCheckUserAdmin'>
							<fmt:param value='<%=step.getSkipPasswordDelayHours()%>' />
							<fmt:param value='<%=step.getSkipPasswordEffectiveTime()%>' />
						</fmt:message>
						<%}else{%>
						<fmt:message key='resetPassword.step.finish.content'>
							<fmt:param value='<%=loginUrl%>' />
						</fmt:message>
						<%}%>
					</div>
					<%}%>
				</div>
				<div class="form-foot" style="text-align:center;">
					<input type="button" id="restartResetPassword" value="<fmt:message key='restart' />" <%if(step.isFirstStep()){%>disabled="disabled"<%}%> />
					<input type="submit" value="<fmt:message key='resetPassword.next' />" <%if(step.isFinalStep()){%>disabled="disabled"<%}else{%> class="recommended"<%}%> />
				</div>
			</form>
		</div>
	</div>
</div>
<%@ include file="include/page_js_obj.jsp" %>
<script type="text/javascript">
(function(pageObj)
{
	pageObj.form = pageObj.element("#${pageId}-form");
	//需要先渲染按钮，不然对话框尺寸不合适，出现滚动条
	$("input:submit, input:button, input:reset, button", pageObj.element()).button();
	
	pageObj.element("#viewResetPasswordAdminReqHistoryLink").click(function()
	{
		var options = {};
		$.setGridPageHeightOption(options);
		pageObj.open("<c:url value='/resetPasswordRequestHistory' />", options);
	});
	
	pageObj.element("#restartResetPassword").click(function()
	{
		window.location.href="<c:url value='/resetPassword' />";
	});
	
	pageObj.element("#sendCheckCodeButton").click(function()
	{
		var _this= $(this);
		
		_this.button("disable");
		
		$.ajax(
		{
			url : "<c:url value='/resetPassword/sendCheckCode' />",
			error : function(jqXHR)
			{
				var operationMessage = $.getResponseJson(jqXHR);
				
				if(operationMessage && operationMessage.code
						&& (operationMessage.code.indexOf("sendCheckCode.admin.smtpSettingNotSet") > 0
								|| operationMessage.code.indexOf("sendCheckCode.admin.MessagingException") > 0))
				{
					pageObj.checkCodeNotRequired = true;
				}
				else
					pageObj.checkCodeNotRequired = false;
			},
			complete : function()
			{
				_this.button("enable");
			}
		});
	});
	
	$.validator.addMethod("checkCodeIfRequired", function(value, element, params)
	{
		if(pageObj.checkCodeNotRequired)
			return true;
		else
			return (value.length > 0);
	},"");
	
	pageObj.form.validate(
	{
		<%if(step.isStep(1)){%>
		rules : { username : "required" },
		messages : { username : "<fmt:message key='validation.required' />" },
		<%}else if(step.isStep(2)){%>
		rules : { checkCode : "checkCodeIfRequired" },
		messages : { checkCode : "<fmt:message key='validation.required' />" },
		<%}else if(step.isStep(3)){%>
		rules : { password : "required", confirmPassword : {"required" : true, "equalTo" : pageObj.element("input[name='password']")} },
		messages : { password : "<fmt:message key='validation.required' />", confirmPassword : {"required" : "<fmt:message key='validation.required' />", "equalTo" : "<fmt:message key='resetPassword.validation.confirmPasswordError' />"} },
		<%}%>
		errorPlacement : function(error, element)
		{
			error.appendTo(element.closest(".form-item"));
		},
		submitHandler : function(form)
		{
			$(form).ajaxSubmit(
			{
				success : function()
				{
					window.location.href="<c:url value='/resetPassword?step' />";
				}
			});
		}
	});
	
	<%if(step.isStep(3) && step.isSkipCheckUserAdmin() && step.getSkipReason() != null && !step.getSkipReason().isEmpty()){%>
	$(document).ready(function()
	{
		$.tipInfo("<%=step.getSkipReason()%>");
	});
	<%}%>
})
(${pageId});
</script>
</body>
</html>
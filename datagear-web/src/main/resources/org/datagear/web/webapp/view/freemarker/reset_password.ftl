<#include "include/import_global.ftl">
<#include "include/html_doctype.ftl">
<#macro stepCss currentStep myStepIndex><#if currentStep.step gt myStepIndex>ui-state-default<#elseif currentStep.step == myStepIndex>ui-state-active<#else>ui-state-disabled</#if></#macro>
<html>
<head>
<#include "include/html_head.ftl">
<#if step.finalStep && !step.skipCheckUserAdmin>
<meta http-equiv="refresh" content="4;url=${contextPath}/login">
</#if>
<title><#include "include/html_title_app_name.ftl"><@spring.message code='resetPassword.resetPassword' /></title>
</head>
<body>
<div id="${pageId}">
	<div class="main-page-head main-page-head-reset-passord">
		<#include "include/html_logo.ftl">
		<div class="toolbar">
			<a class="link" href="javascript:void(0);" id="viewResetPasswordAdminReqHistoryLink"><@spring.message code='resetPassword.viewResetPasswordAdminReqHistory' /></a>
			<a class="link" href="${contextPath}/login"><@spring.message code='resetPassword.backToLoginPage' /></a>
			<a class="link" href="${contextPath}/"><@spring.message code='backToMainPage' /></a>
		</div>
	</div>
	<div class="page-form page-form-reset-password">
		<div class="head">
			<@spring.message code='resetPassword.resetPassword' />
		</div>
		<div class="content">
			<div class="steps">
				<div class="step ui-widget ui-widget-content ui-corner-all <@stepCss currentStep=step myStepIndex=1 />"><@spring.message code='resetPassword.step.fillInUserInfo' /></div>
				<div class="step ui-widget ui-widget-content ui-corner-all <@stepCss currentStep=step myStepIndex=2 />"><@spring.message code='resetPassword.step.checkUser' /></div>
				<div class="step ui-widget ui-widget-content ui-corner-all <@stepCss currentStep=step myStepIndex=3 />"><@spring.message code='resetPassword.step.setNewPassword' /></div>
				<div class="step ui-widget ui-widget-content ui-corner-all <@stepCss currentStep=step myStepIndex=4 />"><@spring.message code='resetPassword.step.finish' /></div>
			</div>
			<form id="${pageId}-form" action="${contextPath}/resetPassword/${step.action}">
				<div class="ui-widget ui-widget-content ui-corner-all form-content">
					<#if step.step == 1>
					<div class="form-item">
						<div class="form-item-label">
							<label><@spring.message code='resetPassword.username' /></label>
						</div>
						<div class="form-item-value">
							<input type="text" name="username" value="" class="ui-widget ui-widget-content" />
						</div>
					</div>
					<#elseif step.step == 2>
					<div class="form-item">
						<div class="form-item-label">
							<label><@spring.message code='resetPassword.username' /></label>
						</div>
						<div class="form-item-value">
							<input type="text" value="${(step.user.name)!''?html}" class="ui-widget ui-widget-content" readonly="readonly" />
						</div>
					</div>
					<div class="form-item">
						<div class="form-item-label">
							<label><@spring.message code='resetPassword.email' /></label>
						</div>
						<div class="form-item-value">
							<input type="text" value="${(step.blurryEmail)!''?html}" class="ui-widget ui-widget-content" readonly="readonly" />
							<button id="sendCheckCodeButton" type="button"><@spring.message code='resetPassword.sendCheckCode' /></button>
						</div>
					</div>
					<div class="form-item">
						<div class="form-item-label">
							<label><@spring.message code='resetPassword.checkcode' /></label>
						</div>
						<div class="form-item-value">
							<input type="text" name="checkCode" value="" class="ui-widget ui-widget-content" />
						</div>
					</div>
					<#elseif step.step == 3>
					<div class="form-item">
						<div class="form-item-label">
							<label><@spring.message code='resetPassword.username' /></label>
						</div>
						<div class="form-item-value">
							<input type="text" value="${(step.user.name)!''?html}" class="ui-widget ui-widget-content" readonly="readonly" />
						</div>
					</div>
					<div class="form-item">
						<div class="form-item-label">
							<label><@spring.message code='resetPassword.password' /></label>
						</div>
						<div class="form-item-value">
							<input type="password" name="password" value="" class="ui-widget ui-widget-content" />
						</div>
					</div>
					<div class="form-item">
						<div class="form-item-label">
							<label><@spring.message code='resetPassword.confirmPassword' /></label>
						</div>
						<div class="form-item-value">
							<input type="password" name="confirmPassword" value="" class="ui-widget ui-widget-content" />
						</div>
					</div>
					<#if step.skipCheckUserAdmin>
					<div class="form-item">
						<div class="ui-state-highlight ui-corner-all skip-check-user-admin-warn">
							<span class="ui-icon ui-icon-info"></span>
							<#assign messageArgs=['${step.skipPasswordDelayHours}'] />
							<@spring.messageArgs code='resetPassword.setNewPassword.skipCheckUserAdminWarn' args=messageArgs />
						</div>
					</div>
					</#if>
					<#elseif step.finalStep>
					<div class="step-finish-content">
						<span class="ui-icon ui-icon-check"></span>
						<#if step.skipCheckUserAdmin>
							<#assign messageArgs=['${step.skipPasswordDelayHours}', '${step.skipPasswordEffectiveTime}'] />
							<@spring.messageArgs code='resetPassword.step.finish.content.skipCheckUserAdmin' args=messageArgs />
						<#else>
							<#assign messageArgs=['${contextPath}/login'] />
							<@spring.messageArgs code='resetPassword.step.finish.content' args=messageArgs />
						</#if>
					</div>
					</#if>
				</div>
				<div class="form-foot" style="text-align:center;">
					<input type="button" id="restartResetPassword" value="<@spring.message code='restart' />" <#if step.firstStep>disabled="disabled"</#if> />
					<input type="submit" value="<@spring.message code='resetPassword.next' />" <#if step.finalStep>disabled="disabled"<#else>class="recommended"</#if> />
				</div>
			</form>
		</div>
	</div>
</div>
<#include "include/page_js_obj.ftl">
<#include "include/page_obj_form.ftl">
<script type="text/javascript">
(function(po)
{
	//需要先渲染按钮，不然对话框尺寸不合适，出现滚动条
	$.initButtons(po.element());
	
	po.element("#viewResetPasswordAdminReqHistoryLink").click(function()
	{
		var options = {};
		$.setGridPageHeightOption(options);
		po.open("${contextPath}/resetPasswordRequestHistory", options);
	});
	
	po.element("#restartResetPassword").click(function()
	{
		window.location.href="${contextPath}/resetPassword";
	});
	
	po.element("#sendCheckCodeButton").click(function()
	{
		var _this= $(this);
		
		_this.button("disable");
		
		$.ajax(
		{
			url : "${contextPath}/resetPassword/sendCheckCode",
			error : function(jqXHR)
			{
				var operationMessage = $.getResponseJson(jqXHR);
				
				if(operationMessage && operationMessage.code
						&& (operationMessage.code.indexOf("sendCheckCode.admin.smtpSettingNotSet") > 0
								|| operationMessage.code.indexOf("sendCheckCode.admin.MessagingException") > 0))
				{
					po.checkCodeNotRequired = true;
				}
				else
					po.checkCodeNotRequired = false;
			},
			complete : function()
			{
				_this.button("enable");
			}
		});
	});
	
	$.validator.addMethod("checkCodeIfRequired", function(value, element, params)
	{
		if(po.checkCodeNotRequired)
			return true;
		else
			return (value.length > 0);
	},"");
	
	po.form().validate(
	{
		<#if step.step == 1>
		rules : { username : "required" },
		messages : { username : "<@spring.message code='validation.required' />" },
		<#elseif step.step == 2>
		rules : { checkCode : "checkCodeIfRequired" },
		messages : { checkCode : "<@spring.message code='validation.required' />" },
		<#elseif step.step == 3>
		rules : { password : "required", confirmPassword : {"required" : true, "equalTo" : po.element("input[name='password']")} },
		messages : { password : "<@spring.message code='validation.required' />", confirmPassword : {"required" : "<@spring.message code='validation.required' />", "equalTo" : "<@spring.message code='resetPassword.validation.confirmPasswordError' />"} },
		</#if>
		errorPlacement : function(error, element)
		{
			error.appendTo(element.closest(".form-item-value"));
		},
		submitHandler : function(form)
		{
			$(form).ajaxSubmit(
			{
				success : function()
				{
					window.location.href="${contextPath}/resetPassword?step";
				}
			});
		}
	});
	
	<#if step.step == 3 && step.skipCheckUserAdmin && (step.skipReason)??>
	$(document).ready(function()
	{
		$.tipInfo("${(step.skipReason)?js_string}");
	});
	</#if>
})
(${pageId});
</script>
</body>
</html>
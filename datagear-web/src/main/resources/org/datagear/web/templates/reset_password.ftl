<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#include "include/import_global.ftl">
<#include "include/html_doctype.ftl">
<#macro stepCss currentStep myStepIndex><#if currentStep.step gt myStepIndex>ui-state-default<#elseif currentStep.step == myStepIndex>ui-state-active<#else>ui-state-disabled</#if></#macro>
<html>
<head>
<#include "include/html_head.ftl">
${detectNewVersionScript?no_esc}
<#if step.finalStep>
<meta http-equiv="refresh" content="4;url=${contextPath}/login">
</#if>
<title><#include "include/html_title_app_name.ftl"><@spring.message code='resetPassword.resetPassword' /></title>
</head>
<body>
<#include "include/page_js_obj.ftl">
<div id="${pageId}">
	<div class="main-page-head main-page-head-reset-passord">
		<#include "include/html_logo.ftl">
		<div class="toolbar">
			<#include "include/page_obj_sys_menu.ftl">
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
							<input type="text" name="username" value="" class="ui-widget ui-widget-content ui-corner-all" />
						</div>
					</div>
					<#elseif step.step == 2>
					<div class="form-item">
						<div class="form-item-label">
							<label><@spring.message code='resetPassword.username' /></label>
						</div>
						<div class="form-item-value">
							<input type="text" value="${(step.user.name)!''}" class="ui-widget ui-widget-content ui-corner-all" readonly="readonly" />
						</div>
					</div>
					<div class="form-item">
						<div class="form-item-label">
							<label><@spring.message code='resetPassword.checkFile' /></label>
						</div>
						<div class="form-item-value form-item-value-checkFile">
							${step.checkFileTip?no_esc}
						</div>
					</div>
					<#elseif step.step == 3>
					<div class="form-item">
						<div class="form-item-label">
							<label><@spring.message code='resetPassword.username' /></label>
						</div>
						<div class="form-item-value">
							<input type="text" value="${(step.user.name)!''}" class="ui-widget ui-widget-content ui-corner-all" readonly="readonly" />
						</div>
					</div>
					<div class="form-item">
						<div class="form-item-label">
							<label><@spring.message code='resetPassword.password' /></label>
						</div>
						<div class="form-item-value">
							<input type="password" name="password" value="" class="ui-widget ui-widget-content ui-corner-all" autocomplete="new-password" />
						</div>
					</div>
					<div class="form-item">
						<div class="form-item-label">
							<label><@spring.message code='resetPassword.confirmPassword' /></label>
						</div>
						<div class="form-item-value">
							<input type="password" name="confirmPassword" value="" class="ui-widget ui-widget-content ui-corner-all" autocomplete="new-password" />
						</div>
					</div>
					<#elseif step.finalStep>
					<div class="step-finish-content">
						<span class="ui-icon ui-icon-check"></span>
						<#assign messageArgs=['${contextPath}/login'] />
						<@spring.messageArgs code='resetPassword.step.finish.content' args=messageArgs />
					</div>
					</#if>
				</div>
				<div class="form-foot">
					<input type="button" id="restartResetPassword" value="<@spring.message code='restart' />" <#if step.firstStep>disabled="disabled"</#if> />
					<input type="submit" value="<@spring.message code='resetPassword.next' />" <#if step.finalStep>disabled="disabled"<#else>class="recommended"</#if> />
				</div>
			</form>
		</div>
	</div>
</div>
<#include "include/page_obj_form.ftl">
<script type="text/javascript">
(function(po)
{
	po.initButtons();
	
	po.element("#restartResetPassword").click(function()
	{
		window.location.href="${contextPath}/resetPassword";
	});
	
	po.validateForm(
	{
		<#if step.step == 1>
		rules : { username : "required" },
		messages : { username : "<@spring.message code='validation.required' />" },
		<#elseif step.step == 2>
		<#elseif step.step == 3>
		rules : { password : "required", confirmPassword : {"required" : true, "equalTo" : po.element("input[name='password']")} },
		messages : { password : "<@spring.message code='validation.required' />", confirmPassword : {"required" : "<@spring.message code='validation.required' />", "equalTo" : "<@spring.message code='resetPassword.validation.confirmPasswordError' />"} },
		</#if>
		submitHandler : function(form)
		{
			$(form).ajaxSubmitJson(
			{
				success : function()
				{
					window.location.href="${contextPath}/resetPassword?step";
				}
			});
		}
	});

	po.initSysMenu();
})
(${pageId});
</script>
</body>
</html>
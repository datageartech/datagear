<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#assign LoginController=statics['org.datagear.web.controller.LoginController']>
<#include "include/page_import.ftl">
<#include "include/html_doctype.ftl">
<html>
<head>
<meta dg-page-name="login" />
<#include "include/html_head.ftl">
${detectNewVersionScript?no_esc}
<title><#include "include/html_title_app_name.ftl"><@spring.message code='login.login' /></title>
</head>
<body>
<#include "include/page_obj.ftl">
<div id="${pageId}" class="page-login">
	<div class="main-page-head">
		<#include "include/html_logo.ftl">
		<div class="toolbar">
			<#include "include/page_obj_sys_menu.ftl">
			<#if !disableRegister>
			<a class="link" href="${contextPath}/register"><@spring.message code='register.register' /></a>
			</#if>
			<a class="link" href="${contextPath}/"><@spring.message code='backToMainPage' /></a>
		</div>
	</div>
	<div class="page-form page-form-login">
		<form id="${pageId}-form" action="${contextPath}/login/doLogin" method="POST" class="display-block">
			<div class="form-head"></div>
			<div class="form-content">
				<div class="form-item">
					<div class="form-item-label">
						<label><@spring.message code='login.username' /></label>
					</div>
					<div class="form-item-value">
						<input type="text" name="${LoginController.LOGIN_PARAM_USER_NAME}" value="${loginUsername!}" required="required" maxlength="50" class="ui-widget ui-widget-content ui-corner-all" autofocus="autofocus" />
					</div>
				</div>
				<div class="form-item">
					<div class="form-item-label">
						<label><@spring.message code='login.password' /></label>
					</div>
					<div class="form-item-value">
						<input type="password" name="${LoginController.LOGIN_PARAM_PASSWORD}" value="" required="required" maxlength="50" class="ui-widget ui-widget-content ui-corner-all" />
					</div>
				</div>
				<#if !disableLoginCheckCode>
				<div class="form-item">
					<div class="form-item-label">
						<label><@spring.message code='checkCode' /></label>
					</div>
					<div class="form-item-value">
						<input type="text" name="${LoginController.LOGIN_PARAM_CHECK_CODE}" value="" required="required" maxlength="10" class="ui-widget ui-widget-content ui-corner-all" />
						<img class="checkCodeImg check-code" />
					</div>
				</div>
				</#if>
			</div>
			<div class="form-foot">
				<button type="submit" class="recommended"><@spring.message code='login.login' /></button>
			</div>
			<div class="form-foot small-text login-form-ext" style="text-align:right;">
				<div class="rememberMeGroup">
					<label for="${pageId}rememberMe"><@spring.message code='login.rememberMe' /></label>
		   			<input type="checkbox" id="${pageId}rememberMe" name="${LoginController.LOGIN_PARAM_REMEMBER_ME}" value="1" />
	   			</div>
	   			<a class="link" href="${contextPath}/resetPassword"><@spring.message code='login.fogetPassword' /></a>
			</div>
		</form>
	</div>
</div>
<#include "include/page_obj_form.ftl">
<script type="text/javascript">
(function(po)
{
	po.initFormBtns();
	po.element(".rememberMeGroup").checkboxradiogroup({icon:true});
	
	po.element(".page-form").dialog(
	{
		appendTo: po.element(),
		classes: { "ui-dialog": "loginDialog ui-corner-all" },
		title: "<@spring.message code='login.login' />",
		position: {my : "center top", at : "center top+75"},
		resizable: false,
		draggable: true,
		width: "30%",
		beforeClose: function(){ return false; }
	});
	
	po.element(".loginDialog .ui-dialog-titlebar-close").hide();
	
	//当登录超时打开对话框时，对话框内会显示登录页面，这里调整此时的登录页布局
	if($.isInDialog(po.element()))
	{
		po.element(".main-page-head").hide();
		po.element(".loginDialog").css("top", "14px");
		po.element(".loginDialog .ui-dialog-titlebar").hide();
	}
	
	po.initSysMenu();
	po.validateAjaxForm({},
	{
		tipSuccess: false,
		success: function()
		{
			(window.top ? window.top : window).location.href="${contextPath}/";
		}
	});
	
	<#if !disableLoginCheckCode>
	po.element(".checkCodeImg").click(function()
	{
		$(this).attr("src", "${contextPath}/checkCode?_=" + $.uid("rc")+"&m=${LoginController.CHECK_CODE_MODULE_LOGIN}");
	})
	.click();
	</#if>
})
(${pageId});
</script>
</body>
</html>
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
<html>
<head>
<meta dg-page-name="login" />
<#include "include/html_head.ftl">
${detectNewVersionScript?no_esc}
<title><#include "include/html_title_app_name.ftl"><@spring.message code='login.login' /></title>
</head>
<body>
<#include "include/page_js_obj.ftl">
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
						<input type="text" name="name" value="${loginUser}" class="ui-widget ui-widget-content" />
					</div>
				</div>
				<div class="form-item">
					<div class="form-item-label">
						<label><@spring.message code='login.password' /></label>
					</div>
					<div class="form-item-value">
						<input type="password" name="password" value="" class="ui-widget ui-widget-content" />
					</div>
				</div>
			</div>
			<div class="form-foot" style="text-align:center;">
				<input type="submit" class="recommended" value="<@spring.message code='login.login' />" />
				&nbsp;&nbsp;
				<input type="reset" value="<@spring.message code='reset' />" />
			</div>
			<div class="form-foot small-text login-form-ext" style="text-align:right;">
				<label for="remember-me-checkbox"><@spring.message code='login.rememberMe' /></label>
	   			<input type="checkbox" id="remember-me-checkbox" name="rememberMe" value="1" />
	   			<a class="link" href="${contextPath}/resetPassword"><@spring.message code='login.fogetPassword' /></a>
			</div>
		</form>
	</div>
</div>
<#include "include/page_obj_form.ftl">
<script type="text/javascript">
(function(po)
{
	//需要先渲染按钮，不然对话框尺寸不合适，出现滚动条
	$.initButtons(po.element());
	$("input[name=rememberMe]", po.element()).checkboxradio({icon:true});
	
	var dialog=po.element(".page-form").dialog({
		appendTo: po.element(),
		classes: { "ui-dialog": "login-form-dialog" },
		title: "<@spring.message code='login.login' />",
		position: {my : "center top", at : "center top+75"},
		resizable: false,
		draggable: true,
		width: "30%",
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
			name : "<@spring.message code='validation.required' />",
			password : "<@spring.message code='validation.required' />"
		},
		errorPlacement : function(error, element)
		{
			error.appendTo(element.closest(".form-item-value"));
		}
	});
	
	po.element(".login-form-dialog .ui-dialog-titlebar-close").hide();
	
	po.initSysMenu();
	
	//当登录超时打开对话框时，对话框内会显示登录页面，这里调整此时的登录页布局
	if($.isInDialog(po.element()))
	{
		po.element(".main-page-head").hide();
		po.element(".login-form-dialog").css("top", "14px");
		po.element(".login-form-dialog .ui-dialog-titlebar").hide();
	}
	
	<#if authenticationFailed>
	$(document).ready(function()
	{
		$.tipError("<@spring.message code='login.userNameOrPasswordError' />");
	});
	</#if>
})
(${pageId});
</script>
</body>
</html>
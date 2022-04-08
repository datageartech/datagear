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
<#include "include/html_head.ftl">
${detectNewVersionScript?no_esc}
<title><#include "include/html_title_app_name.ftl"><@spring.message code='register.register' /></title>
</head>
<body>
<#include "include/page_js_obj.ftl" >
<div id="${pageId}">
	<div class="main-page-head">
		<#include "include/html_logo.ftl">
		<div class="toolbar">
			<#include "include/page_obj_sys_menu.ftl">
			<a class="link" href="${contextPath}/login"><@spring.message code='login.login' /></a>
			<a class="link" href="${contextPath}/"><@spring.message code='backToMainPage' /></a>
		</div>
	</div>
	<div class="page-form page-form-register">
		<form id="${pageId}form" action="${contextPath}/register/doRegister" method="POST" class="display-block">
			<div class="form-head"></div>
			<div class="form-content">
				<div class="form-item">
					<div class="form-item-label">
						<label><@spring.message code='register.name' /></label>
					</div>
					<div class="form-item-value">
						<input type="text" name="name" value="" required="required" maxlength="50" class="ui-widget ui-widget-content ui-corner-all" autocomplete="off" />
					</div>
				</div>
				<div class="form-item">
					<div class="form-item-label">
						<label><@spring.message code='register.password' /></label>
					</div>
					<div class="form-item-value">
						<input type="password" name="password" value="" required="required" maxlength="50" class="ui-widget ui-widget-content ui-corner-all" autocomplete="new-password" />
					</div>
				</div>
				<div class="form-item">
					<div class="form-item-label">
						<label><@spring.message code='register.confirmPassword' /></label>
					</div>
					<div class="form-item-value">
						<input type="password" name="confirmPassword" value="" required="required" maxlength="50" class="ui-widget ui-widget-content ui-corner-all" autocomplete="new-password" />
					</div>
				</div>
				<div class="form-item">
					<div class="form-item-label">
						<label><@spring.message code='register.realName' /></label>
					</div>
					<div class="form-item-value">
						<input type="text" name="realName" value="" maxlength="50" class="ui-widget ui-widget-content ui-corner-all" />
					</div>
				</div>
			</div>
			<div class="form-foot">
				<button type="submit" class="recommended"><@spring.message code='register.register' /></button>
			</div>
		</form>
	</div>
</div>
<#include "include/page_obj_form.ftl">
<script type="text/javascript">
(function(po)
{
	po.initFormBtns();
	
	po.element(".page-form").dialog(
	{
		appendTo: po.element(),
		classes: { "ui-dialog": "registerDialog ui-corner-all" },
		title: "<@spring.message code='register.register' />",
		position: {my : "center top", at : "center top+75"},
		resizable: false,
		draggable: true,
		width: "30%",
		beforeClose: function(){ return false; }
	});
	
	po.element(".registerDialog .ui-dialog-titlebar-close").hide();
	
	po.validateAjaxJsonForm(
	{
		rules:
		{
			confirmPassword : { "equalTo" : po.elementOfName("password") }
		}
	},
	{
		handleData: function(data)
		{
			var newData = {};
			newData.user = data;
			newData.confirmPassword = data.confirmPassword;
			data.confirmPassword = undefined;
			
			return newData;
		},
		success : function()
		{
			window.location.href="${contextPath}/register/success";
		}
	});
	
	po.initSysMenu();
})
(${pageId});
</script>
</body>
</html>
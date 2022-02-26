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
		<form id="${pageId}-form" action="${contextPath}/register/doRegister" method="POST" class="display-block">
			<div class="form-head"></div>
			<div class="form-content">
				<div class="form-item">
					<div class="form-item-label">
						<label><@spring.message code='register.name' /></label>
					</div>
					<div class="form-item-value">
						<input type="text" name="name" value="" class="ui-widget ui-widget-content ui-corner-all" autocomplete="off" />
					</div>
				</div>
				<div class="form-item">
					<div class="form-item-label">
						<label><@spring.message code='register.password' /></label>
					</div>
					<div class="form-item-value">
						<input type="password" name="password" value="" class="ui-widget ui-widget-content ui-corner-all" autocomplete="new-password" />
					</div>
				</div>
				<div class="form-item">
					<div class="form-item-label">
						<label><@spring.message code='register.confirmPassword' /></label>
					</div>
					<div class="form-item-value">
						<input type="password" name="confirmPassword" value="" class="ui-widget ui-widget-content ui-corner-all" autocomplete="new-password" />
					</div>
				</div>
				<div class="form-item">
					<div class="form-item-label">
						<label><@spring.message code='register.realName' /></label>
					</div>
					<div class="form-item-value">
						<input type="text" name="realName" value="" class="ui-widget ui-widget-content ui-corner-all" />
					</div>
				</div>
			</div>
			<div class="form-foot">
				<input type="submit" class="recommended" value="<@spring.message code='register.register' />" />
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
	
	var dialog=po.element(".page-form").dialog({
		appendTo: po.element(),
		title: "<@spring.message code='register.register' />",
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
			password : "required",
			confirmPassword : { "required" : true, "equalTo" : po.element("input[name='password']") }
		},
		messages :
		{
			name : "<@spring.message code='validation.required' />",
			password : "<@spring.message code='validation.required' />",
			confirmPassword :
			{
				"required" : "<@spring.message code='validation.required' />",
				"equalTo" : "<@spring.message code='register.validation.confirmPasswordError' />"
			}
		},
		submitHandler : function(form)
		{
			$(form).ajaxSubmitJson(
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
		},
		errorPlacement : function(error, element)
		{
			error.appendTo(element.closest(".form-item-value"));
		}
	});
	
	$(".ui-dialog .ui-dialog-titlebar-close", dialog.widget).hide();
	
	po.initSysMenu();
})
(${pageId});
</script>
</body>
</html>
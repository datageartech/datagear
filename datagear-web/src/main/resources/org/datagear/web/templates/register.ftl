<#include "include/import_global.ftl">
<#include "include/html_doctype.ftl">
<html>
<head>
<#include "include/html_head.ftl">
<title><#include "include/html_title_app_name.ftl"><@spring.message code='register.register' /></title>
</head>
<body>
<div id="${pageId}">
	<div class="main-page-head">
		<#include "include/html_logo.ftl">
		<div class="toolbar">
			<a class="link" href="${contextPath}/login"><@spring.message code='login.login' /></a>
			<a class="link" href="${contextPath}/"><@spring.message code='backToMainPage' /></a>
		</div>
	</div>
	<div class="page-form page-form-register">
		<form id="${pageId}-form" action="${contextPath}/register/doRegister" method="POST">
			<div class="form-head"></div>
			<div class="form-content">
				<div class="form-item">
					<div class="form-item-label">
						<label><@spring.message code='register.name' /></label>
					</div>
					<div class="form-item-value">
						<input type="text" name="name" value="" class="ui-widget ui-widget-content" />
					</div>
				</div>
				<div class="form-item">
					<div class="form-item-label">
						<label><@spring.message code='register.password' /></label>
					</div>
					<div class="form-item-value">
						<input type="password" name="password" value="" class="ui-widget ui-widget-content" />
					</div>
				</div>
				<div class="form-item">
					<div class="form-item-label">
						<label><@spring.message code='register.confirmPassword' /></label>
					</div>
					<div class="form-item-value">
						<input type="password" name="confirmPassword" value="" class="ui-widget ui-widget-content" />
					</div>
				</div>
				<div class="form-item">
					<div class="form-item-label">
						<label><@spring.message code='register.realName' /></label>
					</div>
					<div class="form-item-value">
						<input type="text" name="realName" value="" class="ui-widget ui-widget-content" />
					</div>
				</div>
				<div class="form-item">
					<div class="form-item-label">
						<label><@spring.message code='register.email' /></label>
					</div>
					<div class="form-item-value">
						<input type="text" name="email" value="" class="ui-widget ui-widget-content" />
					</div>
				</div>
			</div>
			<div class="form-foot" style="text-align:center;">
				<input type="submit" class="recommended" value="<@spring.message code='register.register' />" />
				&nbsp;&nbsp;
				<input type="reset" value="<@spring.message code='reset' />" />
			</div>
		</form>
	</div>
</div>
<#include "include/page_js_obj.ftl" >
<#include "include/page_obj_form.ftl">
<script type="text/javascript">
(function(po)
{
	//需要先渲染按钮，不然对话框尺寸不合适，出现滚动条
	$.initButtons(po.element());
	//元素设置了“checked='checked'”后icon显示有问题，这里先隐藏
	$("input[type=checkbox]", po.element()).checkboxradio({icon:false});
	
	var dialog=po.element(".page-form").dialog({
		appendTo: po.element(),
		title: "<@spring.message code='register.register' />",
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
			name : "<@spring.message code='validation.required' />",
			password : "<@spring.message code='validation.required' />",
			confirmPassword :
			{
				"required" : "<@spring.message code='validation.required' />",
				"equalTo" : "<@spring.message code='register.validation.confirmPasswordError' />"
			},
			email : "<@spring.message code='validation.email' />"
		},
		submitHandler : function(form)
		{
			$(form).ajaxSubmit(
			{
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
})
(${pageId});
</script>
</body>
</html>
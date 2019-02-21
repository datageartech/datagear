<#include "../include/import_global.ftl">
<#include "../include/html_doctype.ftl">
<#--
titleMessageKey 标题标签I18N关键字，不允许null
formAction 表单提交action，允许为null
readonly 是否只读操作，允许为null
-->
<#assign formAction=(formAction!'#')>
<#assign readonly=(readonly!false)>
<#assign isAdd=(formAction == 'saveAdd')>
<html>
<head>
<#include "../include/html_head.ftl">
<title><#include "../include/html_title_app_name.ftl"><@spring.message code='${titleMessageKey}' /></title>
</head>
<body>
<div id="${pageId}" class="page-form page-form-user">
	<form id="${pageId}-form" action="${contextPath}/user/${formAction}" method="POST">
		<div class="form-head"></div>
		<div class="form-content">
			<input type="hidden" name="id" value="${(user.id)!''?html}" />
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='user.name' /></label>
				</div>
				<div class="form-item-value">
					<input type="text" name="name" value="${(user.name)!''?html}" class="ui-widget ui-widget-content" />
				</div>
			</div>
			<#if !readonly>
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='user.password' /></label>
				</div>
				<div class="form-item-value">
					<input type="password" name="password" value="" class="ui-widget ui-widget-content" />
				</div>
			</div>
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='user.confirmPassword' /></label>
				</div>
				<div class="form-item-value">
					<input type="password" name="confirmPassword" value="" class="ui-widget ui-widget-content" />
				</div>
			</div>
			</#if>
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='user.realName' /></label>
				</div>
				<div class="form-item-value">
					<input type="text" name="realName" value="${(user.realName)!''?html}" class="ui-widget ui-widget-content" />
				</div>
			</div>
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='user.email' /></label>
				</div>
				<div class="form-item-value">
					<input type="text" name="email" value="${(user.email)!''?html}" class="ui-widget ui-widget-content" />
				</div>
			</div>
			<#--
			禁用新建管理员账号功能
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='user.admin' /></label>
				</div>
				<div class="form-item-value">
					<div class="user-admin-radios">
					<label for="${pageId}-userAdminYes"><@spring.message code='yes' /></label>
		   			<input type="radio" id="${pageId}-userAdminYes" name="admin" value="1" <#if (user.admin)!false>checked="checked"</#if> />
					<label for="${pageId}-userAdminNo"><@spring.message code='no' /></label>
		   			<input type="radio" id="${pageId}-userAdminNo" name="admin" value="0" <#if !((user.admin)!false)>checked="checked"</#if> />
		   			</div>
				</div>
			</div>
			-->
		</div>
		<div class="form-foot" style="text-align:center;">
			<#if !readonly>
			<input type="submit" value="<@spring.message code='save' />" class="recommended" />
			&nbsp;&nbsp;
			<input type="reset" value="<@spring.message code='reset' />" />
			</#if>
		</div>
	</form>
</div>
<#include "../include/page_js_obj.ftl" >
<#include "../include/page_obj_form.ftl">
<script type="text/javascript">
(function(po)
{
	$.initButtons(po.element());
	
	<#--
	禁用新建管理员账号功能
	po.element("input[name='admin']").checkboxradio({icon:false});
	po.element(".user-admin-radios").controlgroup();
	-->
	
	po.url = function(action)
	{
		return "${contextPath}/user/" + action;
	};
	
	<#if !readonly>
	po.form().validate(
	{
		rules :
		{
			name : "required",
			<#if isAdd>
			password : "required",
			</#if>
			confirmPassword :
			{
				<#if isAdd>
				"required" : true,
				</#if>
				"equalTo" : po.element("input[name='password']")
			},
			email : "email"
		},
		messages :
		{
			name : "<@spring.message code='validation.required' />",
			<#if isAdd>
			password : "<@spring.message code='validation.required' />",
			</#if>
			confirmPassword :
			{
				<#if isAdd>
				"required" : "<@spring.message code='validation.required' />",
				</#if>
				"equalTo" : "<@spring.message code='user.validation.confirmPasswordError' />"
			},
			email : "<@spring.message code='validation.email' />"
		},
		submitHandler : function(form)
		{
			$(form).ajaxSubmit(
			{
				success : function()
				{
					var close = (po.pageParamCall("afterSave")  != false);
					
					if(close)
						po.close();
				}
			});
		},
		errorPlacement : function(error, element)
		{
			error.appendTo(element.closest(".form-item-value"));
		}
	});
	</#if>
})
(${pageId});
</script>
</body>
</html>
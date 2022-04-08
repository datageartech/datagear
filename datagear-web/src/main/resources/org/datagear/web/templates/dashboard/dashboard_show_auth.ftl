<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#include "../include/page_import.ftl">
<#include "../include/html_doctype.ftl">
<html>
<head>
<#include "../include/html_head.ftl">
<#if authed>
<meta http-equiv="refresh" content="3;url=${redirectPath}">
</#if>
<title><#include "../include/html_title_app_name.ftl"><@spring.message code='dashboard.showAuth' /></title>
</head>
<body>
<#include "../include/page_obj.ftl" >
<div id="${pageId}" class="page-dashboard-show-auth">
	<div class="main-page-head">
		<#include "../include/html_logo.ftl">
		<div class="toolbar">
			<#include "../include/page_obj_sys_menu.ftl">
			<#if !currentUser.anonymous>
			<div class="user-name">
			${currentUser.nameLabel}
			</div>
			<#else>
			<a class="link" href="${contextPath}/login"><@spring.message code='main.login' /></a>
			</#if>
		</div>
	</div>
	<div class="page-form page-form-dashboard-show-auth">
		<#if authed>
			<div class="authed">
				<input type="hidden" name="redirectPath" value="${redirectPath!''}" />
				<span class="ui-icon ui-icon-check"></span>
				<#assign messageArgs=['#'] />
				<@spring.messageArgs code='dashboard.showAuth.authed' args=messageArgs />
			</div>
		<#else>
			<form id="${pageId}form" action="${contextPath}/dashboard/authcheck" method="POST" class="display-block" autocomplete="off">
				<div class="form-head">
					<@spring.message code='dashboard.showAuth.dashboardNameQuoteLeft' /><a href="${redirectPath!''}" class="link dashboard-name">${dashboardNameMask!''}</a><@spring.message code='dashboard.showAuth.dashboardNameQuoteRight' />
				</div>
				<div class="form-content">
					<input type="hidden" name="id" value="${id!''}" />
					<input type="hidden" name="name" value="${name!''}" />
					<input type="hidden" name="redirectPath" value="${redirectPath!''}" />
					<div class="form-item">
						<div class="form-item-label">
							<label><@spring.message code='dashboard.showAuth.password' /></label>
						</div>
						<div class="form-item-value">
							<input type="password" name="password" value="" class="ui-widget ui-widget-content ui-corner-all" maxlength="20" autocomplete="off" autofocus="autofocus" />
						</div>
					</div>
				</div>
				<div class="form-foot">
					<button type="submit" class="recommended"><@spring.message code='confirm' /></button>
				</div>
			</form>
		</#if>
	</div>
</div>
<#include "../include/page_obj_form.ftl">
<script type="text/javascript">
(function(po)
{
	po.initFormBtns();
	
	<#if authed>
	po.element(".authed a").attr("href", po.element(".authed input[name='redirectPath']").val());
	</#if>
	
	po.initSysMenu();
	
	po.validateForm(
	{
		rules : {},
		messages : {},
		submitHandler : function(form)
		{
			$(form).ajaxSubmitJson(
			{
				handleData: function(data)
				{
					var newData = { id: data.id, password: data.password };
					return newData;
				},
				success : function(operationMessage)
				{
					var responseData = (operationMessage.data || {});
					
					if(responseData.type == "success")
					{
						window.location.href = po.element("input[name='redirectPath']").val();
					}
					else if(responseData.type == "fail")
					{
						po.element("input[name='password']").focus();
						
						if(responseData.authFailThreshold < 0)
							$.tipError("<@spring.message code='dashboard.showAuth.incorrectPassword' />");
						else
						{
							<#assign messageArgs=['"+responseData.authRemain+"'] />
							$.tipError("<@spring.messageArgs code='dashboard.showAuth.incorrectPasswordWithRemain' args=messageArgs />");
						}
					}
					else if(responseData.type == "deny")
					{
						po.element("input[name='password']").focus();
						$.tipError("<@spring.message code='dashboard.showAuth.authDenied' />");
					}
				}
			});
		}
	});
})
(${pageId});
</script>
</body>
</html>
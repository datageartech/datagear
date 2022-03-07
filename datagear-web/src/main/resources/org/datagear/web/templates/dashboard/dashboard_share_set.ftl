<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#include "../include/import_global.ftl">
<#include "../include/html_doctype.ftl">
<#--
titleMessageKey 标题标签I18N关键字，不允许null
formAction 表单提交action，允许为null
readonly 是否只读操作，允许为null
-->
<#assign formAction=(formAction!'#')>
<#assign readonly=(readonly!false)>
<html>
<head>
<#include "../include/html_head.ftl">
<title><#include "../include/html_title_app_name.ftl"><@spring.message code='${titleMessageKey}' /></title>
</head>
<body>
<#include "../include/page_js_obj.ftl" >
<div id="${pageId}" class="page-form page-form-dashboard-share-set">
	<form id="${pageId}-form" action="${contextPath}/dashboard/${formAction}" method="POST">
		<div class="form-head"></div>
		<div class="form-content">
			<input type="hidden" name="id" value="${(dashboard.id)!''}" />
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='dashboardShareSet.enablePassword' /></label>
				</div>
				<div class="form-item-value">
					<div class="enablePasswordRadios">
					<label for="${pageId}-enablePassword-yes"><@spring.message code='yes' /></label>
		   			<input type="radio" id="${pageId}-enablePassword-yes" name="enablePassword" value="true" <#if (dashboardShareSet.enablePassword)!false>checked="checked"</#if> />
					<label for="${pageId}-enablePassword-no"><@spring.message code='no' /></label>
		   			<input type="radio" id="${pageId}-enablePassword-no" name="enablePassword" value="false" <#if !((dashboardShareSet.enablePassword)!false)>checked="checked"</#if> />
		   			</div>
				</div>
			</div>
			<div class="form-item enablePasswordAware">
				<div class="form-item-label">
					<label title="<@spring.message code='dashboardShareSet.anonymousPassword.desc' />">
						<@spring.message code='dashboardShareSet.anonymousPassword' />
					</label>
				</div>
				<div class="form-item-value">
					<div class="anonymousPasswordRadios">
					<label for="${pageId}-anonymousPassword-yes"><@spring.message code='yes' /></label>
		   			<input type="radio" id="${pageId}-anonymousPassword-yes" name="anonymousPassword" value="true" <#if (dashboardShareSet.anonymousPassword)!false>checked="checked"</#if> />
					<label for="${pageId}-anonymousPassword-no"><@spring.message code='no' /></label>
		   			<input type="radio" id="${pageId}-anonymousPassword-no" name="anonymousPassword" value="false" <#if !((dashboardShareSet.anonymousPassword)!false)>checked="checked"</#if> />
		   			</div>
				</div>
			</div>
			<div class="form-item enablePasswordAware">
				<div class="form-item-label">
					<label><@spring.message code='dashboardShareSet.password' /></label>
				</div>
				<div class="form-item-value">
					<input type="password" name="password" value="${(dashboardShareSet.password)!''}" class="ui-widget ui-widget-content ui-corner-all" maxlength="20" autocomplete="new-password" />
					<button class="togglePasswordBtn" type="button"><@spring.message code='show' /></button>
				</div>
			</div>
			<div class="form-item form-item-confirmPassword enablePasswordAware">
				<div class="form-item-label">
					<label><@spring.message code='dashboardShareSet.confirmPassword' /></label>
				</div>
				<div class="form-item-value">
					<input type="password" name="confirmPassword" value="${(dashboardShareSet.password)!''}" class="ui-widget ui-widget-content ui-corner-all" maxlength="20" autocomplete="new-password" />
				</div>
			</div>
		</div>
		<div class="form-foot">
			<#if !readonly>
			<input type="submit" value="<@spring.message code='save' />" class="recommended" />
			</#if>
		</div>
	</form>
</div>
<#include "../include/page_obj_form.ftl">
<script type="text/javascript">
(function(po)
{
	$.initButtons(po.element());
	po.element(".enablePasswordRadios").checkboxradiogroup();
	po.element(".anonymousPasswordRadios").checkboxradiogroup();
	
	po.url = function(action)
	{
		return "${contextPath}/dashboard/" + action;
	};
	
	//保存无需刷新列表
	po.refreshParent = null;
	
	po.element("input[name='enablePassword']").on("click", function()
	{
		var enablePassword = (po.element("input[name='enablePassword']:checked").val() == "true");
		if(enablePassword)
			po.element(".enablePasswordAware").removeClass("ui-state-disabled");
		else
			po.element(".enablePasswordAware").addClass("ui-state-disabled");
	});
	po.element("input[name='enablePassword']:checked").click();
	
	po.element(".togglePasswordBtn").click(function()
	{
		var psdInput = po.element("input[name='password']");
		var confirmPsdInput = po.element("input[name='confirmPassword']");
		var isShow = (psdInput.attr("type") == "text");
		
		if(!isShow)
		{
			psdInput.attr("type", "text");
			confirmPsdInput.attr("type", "text");
			po.element(".form-item-confirmPassword").hide();
			$(this).text("<@spring.message code='hide' />");
		}
		else
		{
			psdInput.attr("type", "password");
			confirmPsdInput.attr("type", "password");
			po.element(".form-item-confirmPassword").show();
			$(this).text("<@spring.message code='show' />");
		}
	});
	
	<#if !readonly>
	po.form().validate(
	{
		rules :
		{
			confirmPassword : { "equalTo" : po.element("input[name='password']") }
		},
		messages :
		{
			confirmPassword : "<@spring.message code='dashboardShareSet.validation.confirmPasswordIncorrect' />"
		},
		submitHandler : function(form)
		{
			$(form).ajaxSubmitJson(
			{
				handleData: function(data)
				{
					data.confirmPassword = undefined;
				},
				success : function(operationMessage)
				{
					po.pageParamCallAfterSave(true, operationMessage.data);
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
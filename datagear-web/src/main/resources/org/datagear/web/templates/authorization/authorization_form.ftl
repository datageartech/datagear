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
ResourceMeta resourceMeta 资源元信息，不允许null
formAction 表单提交action，允许为null
readonly 是否只读操作，允许为null
-->
<#assign formAction=(formAction!'#')>
<#assign readonly=(readonly!false)>
<#assign isAdd=(formAction == 'saveAdd')>
<#assign Authorization=statics['org.datagear.management.domain.Authorization']>

<#assign principalType=((authorization.principalType)!Authorization.PRINCIPAL_TYPE_USER)>
<#assign permission=((authorization.permission)!resourceMeta.permissionMetas[0].permission)>
<#assign enabled=(((authorization.enabled)!true)?string('true', 'false'))>

<html>
<head>
<#include "../include/html_head.ftl">
<title><#include "../include/html_title_app_name.ftl"><@spring.message code='${titleMessageKey}' /> - <@spring.message code='${resourceMeta.resouceTypeLabel}' /></title>
</head>
<body>
<#include "../include/page_js_obj.ftl" >
<div id="${pageId}" class="page-form page-form-authorization">
	<form id="${pageId}-form" action="${contextPath}/authorization/${resourceMeta.resourceType}/${resource}/${formAction}" method="POST">
		<div class="form-head"></div>
		<div class="form-content">
			<input type="hidden" name="id" value="${(authorization.id)!''}" />
			<input type="hidden" name="resource" value="${resource}" />
			<input type="hidden" name="principal" value="${(authorization.principal)!''}" />
			<input type="hidden" name="resourceType" value="${resourceMeta.resourceType}" />
			
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='${resourceMeta.authPrincipalTypeLabel}' /></label>
				</div>
				<div class="form-item-value">
					<div class="principalType-radios">
						<label for="${pageId}-principalType_0"><@spring.message code='authorization.principalType.USER' /></label>
			   			<input type="radio" id="${pageId}-principalType_0" name="principalType" value="${Authorization.PRINCIPAL_TYPE_USER}" for-form-item="form-item-principal-user" />
						<label for="${pageId}-principalType_1"><@spring.message code='authorization.principalType.ROLE' /></label>
			   			<input type="radio" id="${pageId}-principalType_1" name="principalType" value="${Authorization.PRINCIPAL_TYPE_ROLE}" for-form-item="form-item-principal-role" />
						<label for="${pageId}-principalType_2"><@spring.message code='authorization.principalType.ANONYMOUS' /></label>
			   			<input type="radio" id="${pageId}-principalType_2" name="principalType" value="${Authorization.PRINCIPAL_TYPE_ANONYMOUS}" for-form-item="form-item-principal-anonymous" />
						<label for="${pageId}-principalType_3"><@spring.message code='authorization.principalType.ALL' /></label>
			   			<input type="radio" id="${pageId}-principalType_3" name="principalType" value="${Authorization.PRINCIPAL_TYPE_ALL}" for-form-item="form-item-principal-all" />
		   			</div>
				</div>
			</div>
			
			<div class="form-item form-item-principal form-item-principal-user">
				<div class="form-item-label">
					<label><@spring.message code='${resourceMeta.authPrincipalLabel}' /></label>
				</div>
				<div class="form-item-value">
					<input type="text" name="principalNameUser" value="${(Authorization.PRINCIPAL_TYPE_USER!=principalType)?string('', (authorization.principalName)!'')}" class="ui-widget ui-widget-content ui-corner-all" readonly="readonly" />
					<#if !readonly>
					<button type="button" class="principal-user-select-button"><@spring.message code='select' /></button>
					</#if>
				</div>
			</div>
			<div class="form-item form-item-principal form-item-principal-role">
				<div class="form-item-label">
					<label><@spring.message code='${resourceMeta.authPrincipalLabel}' /></label>
				</div>
				<div class="form-item-value">
					<input type="text" name="principalNameRole" value="${(Authorization.PRINCIPAL_TYPE_ROLE!=principalType)?string('', (authorization.principalName)!'')}" class="ui-widget ui-widget-content ui-corner-all" readonly="readonly" />
					<#if !readonly>
					<button type="button" class="principal-role-select-button"><@spring.message code='select' /></button>
					</#if>
				</div>
			</div>
			<div class="form-item form-item-principal form-item-principal-anonymous">
				<div class="form-item-label">
					<label><@spring.message code='${resourceMeta.authPrincipalLabel}' /></label>
				</div>
				<div class="form-item-value">
					<input type="text" name="principalNameAnonymous" value="<@spring.message code='authorization.principalType.ANONYMOUS' />" class="ui-widget ui-widget-content ui-corner-all" readonly="readonly" />
					<#if !readonly>
					<#--占位按钮，避免切换时界面尺寸变化-->
					<button type="button" style="visibility: hidden; padding-left: 0; padding-right: 0; width: 1px; margin-left: -3px; margin-right: 0;">&nbsp;</button>
					</#if>
				</div>
			</div>
			<div class="form-item form-item-principal form-item-principal-all">
				<div class="form-item-label">
					<label><@spring.message code='${resourceMeta.authPrincipalLabel}' /></label>
				</div>
				<div class="form-item-value">
					<input type="text" name="principalNameAll" value="<@spring.message code='authorization.principalType.ALL' />" class="ui-widget ui-widget-content ui-corner-all" readonly="readonly" />
					<#if !readonly>
					<#--占位按钮，避免切换时界面尺寸变化-->
					<button type="button" style="visibility: hidden; padding-left: 0; padding-right: 0; width: 1px; margin-left: -3px; margin-right: 0;">&nbsp;</button>
					</#if>
				</div>
			</div>
			
			<#if !(resourceMeta.singlePermission)>
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='${resourceMeta.authPermissionLabel}' /></label>
				</div>
				<div class="form-item-value">
					<div class="permission-radios">
						<#list resourceMeta.permissionMetas as pm>
						<label for="${pageId}-permission_${pm?counter}" title="<@spring.message code='${pm.permissionLabelDesc}' />">
							<@spring.message code='${pm.permissionLabel}' />
						</label>
			   			<input type="radio" id="${pageId}-permission_${pm?counter}" name="permission" value="${pm.permission}" />
						</#list>
		   			</div>
				</div>
			</div>
			<#else>
			<input type="hidden" name="permission" value="${resourceMeta.singlePermissionMeta.permission}" />
			</#if>
			
			<#if resourceMeta.enableSetEnable>
				<div class="form-item">
					<div class="form-item-label">
						<label><@spring.message code='${resourceMeta.authEnabledLabel}' /></label>
					</div>
					<div class="form-item-value">
						<div class="enabled-radios">
							<label for="${pageId}-enabled_0"><@spring.message code='yes' /></label>
				   			<input type="radio" id="${pageId}-enabled_0" name="enabled" value="true" />
							<label for="${pageId}-enabled_1"><@spring.message code='no' /></label>
				   			<input type="radio" id="${pageId}-enabled_1" name="enabled" value="false" />
			   			</div>
					</div>
				</div>
			<#else>
				<input type="hidden" name="enabled" value="true" />
			</#if>
			
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
	
	<#if !readonly>
	
	po.element(".principal-user-select-button").click(function()
	{
		var options =
		{
			pageParam :
			{
				select : function(user)
				{
					po.element("input[name='principal']").val(user.id);
					po.element("input[name='principalNameUser']").val(user.nameLabel);
				}
			}
		};
		
		$.setGridPageHeightOption(options);
		
		po.open("${contextPath}/user/select", options);
	});
	
	po.element(".principal-role-select-button").click(function()
	{
		var options =
		{
			pageParam :
			{
				select : function(role)
				{
					po.element("input[name='principal']").val(role.id);
					po.element("input[name='principalNameRole']").val(role.name);
				}
			}
		};
		
		$.setGridPageHeightOption(options);
		
		po.open("${contextPath}/role/select", options);
	});
	
	po.form().validate(
	{
		rules :
		{
			resource : "required",
			resourceType : "required",
			principal : "required",
			principalType : "required",
			permission : "required"
		},
		messages :
		{
			resource : "<@spring.message code='validation.required' />",
			resourceType : "<@spring.message code='validation.required' />",
			principal : "<@spring.message code='validation.required' />",
			principalType : "<@spring.message code='validation.required' />",
			permission : "<@spring.message code='validation.required' />"
		},
		submitHandler : function(form)
		{
			$(form).ajaxSubmitJson(
			{
				ignore: ["principalNameUser", "principalNameRole", "principalNameAnonymous", "principalNameAll"],
				success : function()
				{
					po.pageParamCallAfterSave(true);
				}
			});
		},
		errorPlacement : function(error, element)
		{
			error.appendTo(element.closest(".form-item-value"));
		}
	});
	</#if>
	
	po.element("input[name='principalType']").on("change", function()
	{
		var $this = $(this);
		var value = $this.val();
		
		var forFormItemClass = $this.attr("for-form-item");
		
		po.element(".form-item-principal").hide();
		po.element("."+forFormItemClass).show();
		
		if(value == "${Authorization.PRINCIPAL_TYPE_ANONYMOUS}")
			po.element("input[name='principal']").val("${Authorization.PRINCIPAL_ANONYMOUS}");
		else if(value == "${Authorization.PRINCIPAL_TYPE_ALL}")
			po.element("input[name='principal']").val("${Authorization.PRINCIPAL_ALL}");
		
		<#if !readonly>
		po.element(".form-item-principal").each(function()
		{
			$("input[type='text']", this).rules("remove");
		});
		$("input[type='text']", po.element("."+forFormItemClass)).rules("add",
		{
			"required" : true,
			messages : {"required" : "<@spring.message code='validation.required' />"}
		});
		</#if>
	});
	
	po.element("input[name='principalType'][value='${principalType}']").attr("checked", "checked").change();
	po.element(".principalType-radios").checkboxradiogroup();
	
	<#if !(resourceMeta.singlePermission)>
	po.element("input[name='permission'][value='${permission}']").attr("checked", "checked");
	po.element(".permission-radios").checkboxradiogroup();
	</#if>
	
	<#if resourceMeta.enableSetEnable>
	po.element("input[name='enabled'][value='${enabled}']").attr("checked", "checked");
	po.element(".enabled-radios").checkboxradiogroup();
	</#if>
})
(${pageId});
</script>
</body>
</html>
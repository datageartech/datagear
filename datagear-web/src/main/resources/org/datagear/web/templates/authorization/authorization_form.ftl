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
<#assign resourceTypePattern=resourceMeta.resourceType + Authorization.PATTERN_RESOURCE_TYPE_SUFFIX>

<#assign resourceType=((authorization.resourceType)!resourceMeta.resourceType)>
<#assign principalType=((authorization.principalType)!Authorization.PRINCIPAL_TYPE_USER)>
<#assign permission=((authorization.permission)!resourceMeta.permissionMetas[0].permission)>
<#assign enabled=(((authorization.enabled)!true)?string('true', 'false'))>
<#assign isResourceTypePattern=(resourceType == resourceTypePattern)>
<#if assignedResource??>
<#assign resource=assignedResource>
<#else>
<#assign resource=((authorization.resource)!'')>
</#if>

<html>
<head>
<#include "../include/html_head.ftl">
<title><#include "../include/html_title_app_name.ftl"><@spring.message code='${titleMessageKey}' /> - <@spring.message code='${resourceMeta.resouceTypeLabel}' /></title>
</head>
<body>
<div id="${pageId}" class="page-form page-form-authorization">
	<form id="${pageId}-form" action="${contextPath}/authorization/${resourceMeta.resourceType}/${formAction}" method="POST">
		<div class="form-head"></div>
		<div class="form-content">
			<input type="hidden" name="id" value="${(authorization.id)!''}" />
			<input type="hidden" name="resource" value="${resource}" />
			<input type="hidden" name="principal" value="${(authorization.principal)!''}" />
			
			<#if assignedResource??>
				<input type="hidden" name="resourceType" value="${resourceType}" />
			<#else>
				<#if (resourceMeta.supportSelectResource && resourceMeta.supportPatternResource)>
					<div class="form-item form-item-resourceType">
						<div class="form-item-label">
							<label><@spring.message code='${resourceMeta.authResourceTypeLabel}' /></label>
						</div>
						<div class="form-item-value">
							<div class="resourceType-radios">
								<label for="${pageId}-resourceType_0" title="<@spring.message code='${resourceMeta.authModeSelectResourceLabelDesc}' />">
									<@spring.message code='${resourceMeta.authModeSelectResourceLabel}' />
								</label>
					   			<input type="radio" id="${pageId}-resourceType_0" name="resourceType" value="${resourceMeta.resourceType}" />
								<label for="${pageId}-resourceType_1" title="<@spring.message code='${resourceMeta.authModePatternResourceLabelDesc}' />">
									<@spring.message code='${resourceMeta.authModePatternResourceLabel}' />
								</label>
					   			<input type="radio" id="${pageId}-resourceType_1" name="resourceType" value="${resourceTypePattern}"  />
				   			</div>
						</div>
					</div>
				<#elseif resourceMeta.supportPatternResource>
					<input type="hidden" name="resourceType" value="${resourceTypePattern}" />
				<#else>
					<input type="hidden" name="resourceType" value="${resourceType}" />
				</#if>
				
				<#if resourceMeta.supportSelectResource>
				<div class="form-item form-item-resource-name-entity">
					<div class="form-item-label">
						<label><@spring.message code='${resourceMeta.resouceTypeLabel}' /></label>
					</div>
					<div class="form-item-value">
						<input type="text" name="resourceNameForEntity" value="${isResourceTypePattern?string('', (authorization.resourceName)!'')}" class="ui-widget ui-widget-content" readonly="readonly" />
						<#if !readonly>
						<button type="button" class="resource-select-button"><@spring.message code='select' /></button>
						</#if>
					</div>
				</div>
				</#if>
				
				<#if resourceMeta.supportPatternResource>
				<div class="form-item form-item-resource-name-pattern">
					<div class="form-item-label">
						<label><@spring.message code='${resourceMeta.resouceTypeLabel}' /></label>
					</div>
					<div class="form-item-value">
						<input type="text" name="resourceNameForPattern" value="${(!isResourceTypePattern)?string('', (authorization.resourceName)!'')}" class="ui-widget ui-widget-content" />
						<#if !readonly>
						<#--占位按钮，避免切换时界面尺寸变化-->
						<button type="button" style="visibility: hidden; padding-left: 0; padding-right: 0; width: 1px; margin-left: -3px; margin-right: 0;">&nbsp;</button>
						</#if>
					</div>
				</div>
				</#if>
			</#if>
			
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
					<input type="text" name="principalNameUser" value="${(Authorization.PRINCIPAL_TYPE_USER!=principalType)?string('', (authorization.principalName)!'')}" class="ui-widget ui-widget-content" readonly="readonly" />
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
					<input type="text" name="principalNameRole" value="${(Authorization.PRINCIPAL_TYPE_ROLE!=principalType)?string('', (authorization.principalName)!'')}" class="ui-widget ui-widget-content" readonly="readonly" />
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
					<input type="text" name="principalNameAnonymous" value="<@spring.message code='authorization.principalType.ANONYMOUS' />" class="ui-widget ui-widget-content" readonly="readonly" />
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
					<input type="text" name="principalNameAll" value="<@spring.message code='authorization.principalType.ALL' />" class="ui-widget ui-widget-content" readonly="readonly" />
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
		<div class="form-foot" style="text-align:center;">
			<#if !readonly>
			<input type="submit" value="<@spring.message code='save' />" class="recommended" />
			&nbsp;&nbsp;
			<button type="button" class="reset-button"><@spring.message code='reset' /></button>
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
	
	<#if !readonly>
	
	<#if resourceMeta.supportSelectResource>
	po.element(".resource-select-button").click(function()
	{
		var options =
		{
			pageParam :
			{
				select : function(res)
				{
					po.element("input[name='resource']").val(res.${resourceMeta.selectResourceIdField});
					po.element("input[name='resourceNameForEntity']").val(res.${resourceMeta.selectResourceNameField});
				}
			}
		};
		
		$.setGridPageHeightOption(options);
		
		po.open("${contextPath}${resourceMeta.selectResourceURL}", options);
	});
	</#if>
	
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
	
	po.element(".reset-button").click(function()
	{
		po.form()[0].reset();
		po.element("input[name='resourceType']:checked").change();
		po.element("input[name='principalType']:checked").change();
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
			<#if assignedResource??>
			<#else>
				var resourceType = po.element("input[name='resourceType']:checked").val();
				if(resourceType == '${resourceTypePattern}')
					po.element("input[name='resource']").val(po.element("input[name='resourceNameForPattern']").val());
			</#if>
			
			$(form).ajaxSubmit(
			{
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
	
	<#if assignedResource??>
	<#else>
		<#if (resourceMeta.supportSelectResource && resourceMeta.supportPatternResource)>
		po.element("input[name='resourceType']").on("change", function()
		{
			var val = $(this).val();
			
			var $formItemForPattern = po.element(".form-item-resource-name-pattern");
			var $formItemForEntity = po.element(".form-item-resource-name-entity");
			var $resourceNameForPattern = po.element("input[name='resourceNameForPattern']");
			var $resourceNameForEntity = po.element("input[name='resourceNameForEntity']");
			
			if(val == '${resourceMeta.resourceType}')
			{
				$formItemForPattern.hide();
				$formItemForEntity.show();
				
				<#if !readonly>
				$resourceNameForPattern.rules("remove");
				$resourceNameForEntity.rules("add",
				{
					"required" : true,
					messages : {"required" : "<@spring.message code='validation.required' />"}
				});
				</#if>
			}
			else
			{
				$formItemForPattern.show();
				$formItemForEntity.hide();
				
				<#if !readonly>
				$resourceNameForPattern.rules("add",
				{
					"required" : true,
					messages : {"required" : "<@spring.message code='validation.required' />"}
				});
				$resourceNameForEntity.rules("remove");
				</#if>
			}
		});
		</#if>
		
		<#if resourceMeta.supportSelectResource>
			<#if !readonly>
				po.element("input[name='resourceNameForEntity']").rules("add",
				{
					"required" : true,
					messages : {"required" : "<@spring.message code='validation.required' />"}
				});
			</#if>
		</#if>
		
		<#if resourceMeta.supportPatternResource>
			<#if !readonly>
				po.element("input[name='resourceNameForPattern']").rules("add",
				{
					"required" : true,
					messages : {"required" : "<@spring.message code='validation.required' />"}
				});
			</#if>
		</#if>
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
	
	<#if assignedResource??>
	<#else>
	<#if (resourceMeta.supportSelectResource && resourceMeta.supportPatternResource)>
		po.element("input[name='resourceType'][value='${resourceType}']").attr("checked", "checked").change();
		po.element("input[name='resourceType']").checkboxradio({icon:false});
		po.element(".resourceType-radios").controlgroup();
	</#if>
	</#if>
	
	po.element("input[name='principalType'][value='${principalType}']").attr("checked", "checked").change();
	po.element("input[name='principalType']").checkboxradio({icon:false});
	po.element(".principalType-radios").controlgroup();
	
	<#if !(resourceMeta.singlePermission)>
	po.element("input[name='permission'][value='${permission}']").attr("checked", "checked");
	po.element("input[name='permission']").checkboxradio({icon:false});
	po.element(".permission-radios").controlgroup();
	</#if>
	
	<#if resourceMeta.enableSetEnable>
	po.element("input[name='enabled'][value='${enabled}']").attr("checked", "checked");
	po.element("input[name='enabled']").checkboxradio({icon:false});
	po.element(".enabled-radios").controlgroup();
	</#if>
	
	<#if assignedResource??>
	<#else>
	<#if (resourceMeta.supportSelectResource && resourceMeta.supportPatternResource)>
		po.element("input[name='resourceType'][value='${resourceType}']").attr("checked", "checked").change();
	</#if>
	</#if>
	
	<#--编辑时禁设资源类型，因为管理员也可能编辑普通用户设置的授权，而它们不允许是通配符-->
	<#if formAction == 'saveEdit'>
		<#if assignedResource??>
		<#else>
		<#if (resourceMeta.supportSelectResource && resourceMeta.supportPatternResource)>
			po.element("input[name='resourceType'][value!='${resourceType}']").attr("disabled", "disabled");
			po.element("input[name='resourceType']").checkboxradio("refresh");
		</#if>
		</#if>
	</#if>
})
(${pageId});
</script>
</body>
</html>
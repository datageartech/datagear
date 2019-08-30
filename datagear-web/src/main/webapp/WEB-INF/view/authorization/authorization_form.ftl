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
<#assign Authorization=statics['org.datagear.management.domain.Authorization']>
<#assign Schema=statics['org.datagear.management.domain.Schema']>
<#assign resourceType=((authorization.resourceType)!Authorization.RESOURCE_TYPE_DATA_SOURCE)>
<#assign resourceTypePattern=Authorization.RESOURCE_TYPE_DATA_SOURCE + Authorization.PATTERN_RESOURCE_TYPE_SUFFIX>
<#assign principalType=((authorization.principalType)!Authorization.PRINCIPAL_TYPE_USER)>
<#assign permission=((authorization.permission)!Schema.PERMISSION_TABLE_DATA_READ)>
<#assign enabled=(((authorization.enabled)!true)?string('true', 'false'))>
<#assign isResourceTypePattern=(resourceType != Authorization.RESOURCE_TYPE_DATA_SOURCE)>
<html>
<head>
<#include "../include/html_head.ftl">
<title><#include "../include/html_title_app_name.ftl"><@spring.message code='${titleMessageKey}' /></title>
</head>
<body>
<div id="${pageId}" class="page-form page-form-authorization">
	<form id="${pageId}-form" action="${contextPath}/authorization/${formAction}" method="POST">
		<div class="form-head"></div>
		<div class="form-content">
			<input type="hidden" name="id" value="${(authorization.id)!''?html}" />
			<input type="hidden" name="resource" value="${(authorization.resource)!''?html}" />
			<input type="hidden" name="principal" value="${(authorization.principal)!''?html}" />
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='authorization.resourceType' /></label>
				</div>
				<div class="form-item-value">
					<div class="resourceType-radios">
						<label for="${pageId}-resourceType_0"><@spring.message code='authorization.resourceType.DATA_SOURCE' /></label>
			   			<input type="radio" id="${pageId}-resourceType_0" name="resourceType" value="${Authorization.RESOURCE_TYPE_DATA_SOURCE}" />
						<label for="${pageId}-resourceType_1" title="<@spring.message code='authorization.resourceType.DATA_SOURCE_PATTERN.desc' />"><@spring.message code='authorization.resourceType.DATA_SOURCE_PATTERN' /></label>
			   			<input type="radio" id="${pageId}-resourceType_1" name="resourceType" value="${resourceTypePattern}"  />
		   			</div>
				</div>
			</div>
			<div class="form-item form-item-resource-name-pattern">
				<div class="form-item-label">
					<label><@spring.message code='authorization.resource' /></label>
				</div>
				<div class="form-item-value">
					<input type="text" name="resourceNameForPattern" value="${(!isResourceTypePattern)?string('', (authorization.resourceName)!'')}" class="ui-widget ui-widget-content" />
					<#if !readonly>
					<#--占位按钮，避免切换时界面尺寸变化-->
					<button type="button" style="visibility: hidden; padding-left: 0; padding-right: 0; width: 1px; margin-left: -3px; margin-right: 0;">&nbsp;</button>
					</#if>
				</div>
			</div>
			<div class="form-item form-item-resource-name-entity">
				<div class="form-item-label">
					<label><@spring.message code='authorization.resource' /></label>
				</div>
				<div class="form-item-value">
					<input type="text" name="resourceNameForEntity" value="${isResourceTypePattern?string('', (authorization.resourceName)!'')}" class="ui-widget ui-widget-content" readonly="readonly" />
					<#if !readonly>
					<button type="button" class="resource-select-button"><@spring.message code='select' /></button>
					</#if>
				</div>
			</div>
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='authorization.principalType' /></label>
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
					<label><@spring.message code='authorization.principal' /></label>
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
					<label><@spring.message code='authorization.principal' /></label>
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
					<label><@spring.message code='authorization.principal' /></label>
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
					<label><@spring.message code='authorization.principal' /></label>
				</div>
				<div class="form-item-value">
					<input type="text" name="principalNameAll" value="<@spring.message code='authorization.principalType.ALL' />" class="ui-widget ui-widget-content" readonly="readonly" />
					<#if !readonly>
					<#--占位按钮，避免切换时界面尺寸变化-->
					<button type="button" style="visibility: hidden; padding-left: 0; padding-right: 0; width: 1px; margin-left: -3px; margin-right: 0;">&nbsp;</button>
					</#if>
				</div>
			</div>
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='authorization.permission' /></label>
				</div>
				<div class="form-item-value">
					<div class="permission-radios">
						<label for="${pageId}-permission_0" title="<@spring.message code='authorization.permission.DATA_SOURCE.PERMISSION_TABLE_DATA_READ.desc' />"><@spring.message code='authorization.permission.READ' /></label>
			   			<input type="radio" id="${pageId}-permission_0" name="permission" value="${Schema.PERMISSION_TABLE_DATA_READ}" />
						<label for="${pageId}-permission_1" title="<@spring.message code='authorization.permission.DATA_SOURCE.PERMISSION_TABLE_DATA_EDIT.desc' />"><@spring.message code='authorization.permission.EDIT' /></label>
			   			<input type="radio" id="${pageId}-permission_1" name="permission" value="${Schema.PERMISSION_TABLE_DATA_EDIT}" />
						<label for="${pageId}-permission_2" title="<@spring.message code='authorization.permission.DATA_SOURCE.PERMISSION_TABLE_DATA_DELETE.desc' />"><@spring.message code='authorization.permission.DELETE' /></label>
			   			<input type="radio" id="${pageId}-permission_2" name="permission" value="${Schema.PERMISSION_TABLE_DATA_DELETE}" />
						<label for="${pageId}-permission_3"><@spring.message code='authorization.permission.NONE' /></label>
			   			<input type="radio" id="${pageId}-permission_3" name="permission" value="${Authorization.PERMISSION_NONE_START}" />
		   			</div>
				</div>
			</div>
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='authorization.enabled' /></label>
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
	
	po.url = function(action)
	{
		return "${contextPath}/authorization/" + action;
	};
	
	<#if !readonly>
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
			var resourceType = po.element("input[name='resourceType']:checked").val();
			if(resourceType == '${resourceTypePattern}')
				po.element("input[name='resource']").val(po.element("input[name='resourceNameForPattern']").val());
			
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
	
	po.element(".resource-select-button").click(function()
	{
		var options =
		{
			pageParam :
			{
				submit : function(schema)
				{
					po.element("input[name='resource']").val(schema.id);
					po.element("input[name='resourceNameForEntity']").val(schema.title);
				}
			}
		};
		
		$.setGridPageHeightOption(options);
		
		po.open("${contextPath}/schema/select", options);
	});
	
	po.element(".principal-user-select-button").click(function()
	{
		var options =
		{
			pageParam :
			{
				submit : function(user)
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
				submit : function(role)
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
	</#if>
	
	po.element("input[name='resourceType']").on("change", function()
	{
		var val = $(this).val();
		
		var $formItemForPattern = po.element(".form-item-resource-name-pattern");
		var $formItemForEntity = po.element(".form-item-resource-name-entity");
		var $resourceNameForPattern = po.element("input[name='resourceNameForPattern']");
		var $resourceNameForEntity = po.element("input[name='resourceNameForEntity']");
		
		if(val == '${Authorization.RESOURCE_TYPE_DATA_SOURCE}')
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
	
	po.element("input[name='principalType']").on("change", function()
	{
		var $this = $(this);
		
		var forFormItemClass = $this.attr("for-form-item");
		
		po.element(".form-item-principal").hide();
		po.element("."+forFormItemClass).show();
		
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
	
	po.element("input[name='resourceType'][value='${resourceType}']").attr("checked", "checked").change();
	po.element("input[name='resourceType']").checkboxradio({icon:false});
	po.element(".resourceType-radios").controlgroup();
	
	po.element("input[name='principalType'][value='${principalType}']").attr("checked", "checked").change();
	po.element("input[name='principalType']").checkboxradio({icon:false});
	po.element(".principalType-radios").controlgroup();
	
	po.element("input[name='permission'][value='${permission}']").attr("checked", "checked");
	po.element("input[name='permission']").checkboxradio({icon:false});
	po.element(".permission-radios").controlgroup();
	
	po.element("input[name='enabled'][value='${enabled}']").attr("checked", "checked");
	po.element("input[name='enabled']").checkboxradio({icon:false});
	po.element(".enabled-radios").controlgroup();
})
(${pageId});
</script>
</body>
</html>
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
<#assign principalType=((authorization.principalType)!Authorization.PRINCIPAL_TYPE_ROLE)>
<#assign permission=((authorization.permission)!Schema.PERMISSION_TABLE_DATA_READ)>
<#assign enabled=(((authorization.enabled)!true)?string('true', 'false'))>
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
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='authorization.resourceType' /></label>
				</div>
				<div class="form-item-value">
					<div class="resourceType-radios">
						<label for="${pageId}-resourceType_0"><@spring.message code='authorization.resourceType.DATA_SOURCE' /></label>
			   			<input type="radio" id="${pageId}-resourceType_0" name="resourceType" value="${Authorization.RESOURCE_TYPE_DATA_SOURCE}" />
						<label for="${pageId}-resourceType_1" title="<@spring.message code='authorization.resourceType.DATA_SOURCE_PATTERN.desc' />"><@spring.message code='authorization.resourceType.DATA_SOURCE_PATTERN' /></label>
			   			<input type="radio" id="${pageId}-resourceType_1" name="resourceType" value="${Authorization.RESOURCE_TYPE_DATA_SOURCE + Authorization.PATTERN_RESOURCE_TYPE_SUFFIX}"  />
		   			</div>
				</div>
			</div>
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='authorization.resource' /></label>
				</div>
				<div class="form-item-value">
					<input type="text" name="resource" value="${(authorization.resource)!''?html}" class="ui-widget ui-widget-content" />
				</div>
			</div>
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='authorization.principalType' /></label>
				</div>
				<div class="form-item-value">
					<div class="principalType-radios">
						<label for="${pageId}-principalType_0"><@spring.message code='authorization.principalType.USER' /></label>
			   			<input type="radio" id="${pageId}-principalType_0" name="principalType" value="${Authorization.PRINCIPAL_TYPE_USER}" />
						<label for="${pageId}-principalType_1"><@spring.message code='authorization.principalType.ROLE' /></label>
			   			<input type="radio" id="${pageId}-principalType_1" name="principalType" value="${Authorization.PRINCIPAL_TYPE_ROLE}" />
						<label for="${pageId}-principalType_2"><@spring.message code='authorization.principalType.ANONYMOUS' /></label>
			   			<input type="radio" id="${pageId}-principalType_2" name="principalType" value="${Authorization.PRINCIPAL_TYPE_ANONYMOUS}" />
						<label for="${pageId}-principalType_3"><@spring.message code='authorization.principalType.ALL' /></label>
			   			<input type="radio" id="${pageId}-principalType_3" name="principalType" value="${Authorization.PRINCIPAL_TYPE_ALl}" />
		   			</div>
				</div>
			</div>
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='authorization.principal' /></label>
				</div>
				<div class="form-item-value">
					<input type="text" name="principal" value="${(authorization.principal)!''?html}" class="ui-widget ui-widget-content" />
				</div>
			</div>
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='authorization.permission' /></label>
				</div>
				<div class="form-item-value">
					<div class="permission-radios">
						<label for="${pageId}-permission_0"><@spring.message code='authorization.permission.READ' /></label>
			   			<input type="radio" id="${pageId}-permission_0" name="permission" value="${Schema.PERMISSION_TABLE_DATA_READ}" />
						<label for="${pageId}-permission_1"><@spring.message code='authorization.permission.EDIT' /></label>
			   			<input type="radio" id="${pageId}-permission_1" name="permission" value="${Schema.PERMISSION_TABLE_DATA_EDIT}" />
						<label for="${pageId}-permission_2"><@spring.message code='authorization.permission.DELETE' /></label>
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
	
	po.element("input[name='resourceType'][value='${resourceType}']").attr("checked", "checked");
	po.element("input[name='resourceType']").checkboxradio({icon:false});
	po.element(".resourceType-radios").controlgroup();
	
	po.element("input[name='principalType'][value='${principalType}']").attr("checked", "checked");
	po.element("input[name='principalType']").checkboxradio({icon:false});
	po.element(".principalType-radios").controlgroup();
	
	po.element("input[name='permission'][value='${permission}']").attr("checked", "checked");
	po.element("input[name='permission']").checkboxradio({icon:false});
	po.element(".permission-radios").controlgroup();
	
	po.element("input[name='enabled'][value='${enabled}']").attr("checked", "checked");
	po.element("input[name='enabled']").checkboxradio({icon:false});
	po.element(".enabled-radios").controlgroup();
	
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
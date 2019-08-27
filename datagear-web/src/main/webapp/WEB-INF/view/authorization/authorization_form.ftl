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
<div id="${pageId}" class="page-form page-form-authorization">
	<form id="${pageId}-form" action="${contextPath}/authorization/${formAction}" method="POST">
		<div class="form-head"></div>
		<div class="form-content">
			<input type="hidden" name="id" value="${(authorization.id)!''?html}" />
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
					<label><@spring.message code='authorization.resourceType' /></label>
				</div>
				<div class="form-item-value">
					<input type="text" name="resourceType" value="${(authorization.resourceType)!''?html}" class="ui-widget ui-widget-content" />
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
					<label><@spring.message code='authorization.principalType' /></label>
				</div>
				<div class="form-item-value">
					<input type="text" name="principalType" value="${(authorization.principalType)!''?html}" class="ui-widget ui-widget-content" />
				</div>
			</div>
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='authorization.permission' /></label>
				</div>
				<div class="form-item-value">
					<input type="text" name="permission" value="${(authorization.permission)!''?html}" class="ui-widget ui-widget-content" />
				</div>
			</div>
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='authorization.enabled' /></label>
				</div>
				<div class="form-item-value">
					<div class="authorizationEnabled-radios">
					<label for="${pageId}-authorizationEnabledYes"><@spring.message code='yes' /></label>
		   			<input type="radio" id="${pageId}-authorizationEnabledYes" name="enabled" value="1" <#if (authorization.enabled)!false>checked="checked"</#if> />
					<label for="${pageId}-authorizationEnabledNo"><@spring.message code='no' /></label>
		   			<input type="radio" id="${pageId}-authorizationEnabledNo" name="enabled" value="0" <#if !((authorization.enabled)!false)>checked="checked"</#if> />
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
	po.element("input[name='enabled']").checkboxradio({icon:false});
	po.element(".authorizationEnabled-radios").controlgroup();
	
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
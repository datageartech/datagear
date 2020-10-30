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
<div id="${pageId}" class="page-form page-form-role">
	<form id="${pageId}-form" action="${contextPath}/role/${formAction}" method="POST">
		<div class="form-head"></div>
		<div class="form-content">
			<input type="hidden" name="id" value="${(role.id)!''?html}" />
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='role.name' /></label>
				</div>
				<div class="form-item-value">
					<input type="text" name="name" value="${(role.name)!''?html}" class="ui-widget ui-widget-content" />
				</div>
			</div>
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='role.description' /></label>
				</div>
				<div class="form-item-value">
					<textarea name="description" class="ui-widget ui-widget-content">${(role.description)!''?html}</textarea>
				</div>
			</div>
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='role.enabled' /></label>
				</div>
				<div class="form-item-value">
					<div class="roleEnabled-radios">
					<label for="${pageId}-roleEnabledYes"><@spring.message code='yes' /></label>
		   			<input type="radio" id="${pageId}-roleEnabledYes" name="enabled" value="1" <#if (role.enabled)!false>checked="checked"</#if> />
					<label for="${pageId}-roleEnabledNo"><@spring.message code='no' /></label>
		   			<input type="radio" id="${pageId}-roleEnabledNo" name="enabled" value="0" <#if !((role.enabled)!false)>checked="checked"</#if> />
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
	po.element(".roleEnabled-radios").controlgroup();
	
	po.url = function(action)
	{
		return "${contextPath}/role/" + action;
	};
	
	<#if !readonly>
	po.form().validate(
	{
		rules :
		{
			name : "required"
		},
		messages :
		{
			name : "<@spring.message code='validation.required' />"
		},
		submitHandler : function(form)
		{
			$(form).ajaxSubmit(
			{
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
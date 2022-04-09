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
<#include "../include/page_obj.ftl" >
<div id="${pageId}" class="page-form page-form-role">
	<form id="${pageId}form" action="${contextPath}/role/${formAction}" method="POST">
		<div class="form-head"></div>
		<div class="form-content">
			<input type="hidden" name="id" value="${(role.id)!''}" />
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='role.name' /></label>
				</div>
				<div class="form-item-value">
					<input type="text" name="name" value="${(role.name)!''}" required="required" maxlength="100" class="ui-widget ui-widget-content ui-corner-all" autofocus="autofocus" />
				</div>
			</div>
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='role.description' /></label>
				</div>
				<div class="form-item-value">
					<textarea name="description" maxlength="200" class="ui-widget ui-widget-content ui-corner-all">${(role.description)!''}</textarea>
				</div>
			</div>
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='role.enabled' /></label>
				</div>
				<div class="form-item-value">
					<div class="roleEnabled-radios">
					<label for="${pageId}-roleEnabledYes"><@spring.message code='yes' /></label>
		   			<input type="radio" id="${pageId}-roleEnabledYes" name="enabled" value="true" <#if (role.enabled)!false>checked="checked"</#if> />
					<label for="${pageId}-roleEnabledNo"><@spring.message code='no' /></label>
		   			<input type="radio" id="${pageId}-roleEnabledNo" name="enabled" value="false" <#if !((role.enabled)!false)>checked="checked"</#if> />
		   			</div>
				</div>
			</div>
		</div>
		<div class="form-foot">
			<#if !readonly>
			<button type="submit" class="recommended"><@spring.message code='save' /></button>
			</#if>
		</div>
	</form>
</div>
<#include "../include/page_obj_form.ftl">
<script type="text/javascript">
(function(po)
{
	po.initFormBtns();
	po.element(".roleEnabled-radios").checkboxradiogroup();
	
	po.url = function(action)
	{
		return "${contextPath}/role/" + action;
	};
	
	po.validateAjaxJsonForm();
})
(${pageId});
</script>
</body>
</html>
<#include "../../include/import_global.ftl">
<#include "../../include/html_doctype.ftl">
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
<#include "../../include/html_head.ftl">
<title><#include "../../include/html_title_app_name.ftl"><@spring.message code='${titleMessageKey}' /></title>
</head>
<body>
<div id="${pageId}" class="page-form page-form-analysisProject">
	<form id="${pageId}-form" action="${contextPath}/analysis/project/${formAction}" method="POST">
		<div class="form-head"></div>
		<div class="form-content">
			<input type="hidden" name="id" value="${(analysisProject.id)!''}" />
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='analysisProject.name' /></label>
				</div>
				<div class="form-item-value">
					<input type="text" name="name" value="${(analysisProject.name)!''}" class="ui-widget ui-widget-content" />
				</div>
			</div>
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='analysisProject.desc' /></label>
				</div>
				<div class="form-item-value">
					<textarea name="desc" class="ui-widget ui-widget-content">${(analysisProject.desc)!''}</textarea>
				</div>
			</div>
		</div>
		<div class="form-foot" style="text-align:center;">
			<#if !readonly>
			<input type="submit" value="<@spring.message code='save' />" class="recommended" />
			</#if>
		</div>
	</form>
</div>
<#include "../../include/page_js_obj.ftl" >
<#include "../../include/page_obj_form.ftl">
<script type="text/javascript">
(function(po)
{
	$.initButtons(po.element());
	
	po.url = function(action)
	{
		return "${contextPath}/analysis/project/" + action;
	};
	
	<#if !readonly>
	po.form().validate(
	{
		rules :
		{
			"name" : "required"
		},
		messages :
		{
			"name" : "<@spring.message code='validation.required' />"
		},
		submitHandler : function(form)
		{
			var data = $.formToJson(form);
			
			$.ajaxJson($(form).attr("action"),
			{
				data: data,
				success : function(response)
				{
					po.pageParamCallAfterSave(true, response.data);
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
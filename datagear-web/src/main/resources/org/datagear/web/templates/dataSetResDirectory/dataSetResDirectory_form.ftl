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
<#assign isAdd=(formAction == 'saveAdd')>
<html>
<head>
<#include "../include/html_head.ftl">
<title><#include "../include/html_title_app_name.ftl"><@spring.message code='${titleMessageKey}' /></title>
</head>
<body>
<#include "../include/page_js_obj.ftl" >
<div id="${pageId}" class="page-form page-form-dataSetResDirectory">
	<form id="${pageId}-form" action="${contextPath}/dataSetResDirectory/${formAction}" method="POST">
		<div class="form-head"></div>
		<div class="form-content">
			<input type="hidden" name="id" value="${(dataSetResDirectory.id)!''}" />
			<div class="form-item">
				<div class="form-item-label">
					<label title="<@spring.message code='dataSetResDirectory.directory.desc' />">
						<@spring.message code='dataSetResDirectory.directory' />
					</label>
				</div>
				<div class="form-item-value">
					<input type="text" name="directory" value="${(dataSetResDirectory.directory)!''}" class="ui-widget ui-widget-content" />
				</div>
			</div>
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='dataSetResDirectory.desc' /></label>
				</div>
				<div class="form-item-value">
					<textarea name="desc" class="ui-widget ui-widget-content">${(dataSetResDirectory.desc)!''}</textarea>
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
<#include "../include/page_obj_form.ftl">
<script type="text/javascript">
(function(po)
{
	$.initButtons(po.element());
	
	po.url = function(action)
	{
		return "${contextPath}/dataSetResDirectory/" + action;
	};
	
	<#if !readonly>
	po.form().validate(
	{
		rules :
		{
			"directory" : "required"
		},
		messages :
		{
			"directory" : "<@spring.message code='validation.required' />"
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
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
<html>
<head>
<#include "../include/html_head.ftl">
<title><#include "../include/html_title_app_name.ftl"><@spring.message code='${titleMessageKey}' /></title>
</head>
<body>
<#include "../include/page_obj.ftl" >
<div id="${pageId}" class="page-form page-form-dashboardGlobalResContent">
	<form id="${pageId}form" action="${contextPath}/dashboardGlobalRes/${formAction}" method="POST">
		<input type="hidden" name="initSavePath" value="${resourcePath}" />
		<div class="form-head"></div>
		<div class="form-content">
			<div class="form-item form-item-savePath">
				<div class="form-item-label">
					<label title="<@spring.message code='dashboardGlobalRes.savePath.desc' />">
						<@spring.message code='dashboardGlobalRes.savePath' />
					</label>
				</div>
				<div class="form-item-value">
					<input type="text" name="savePath" value="${resourcePath}" class="ui-widget ui-widget-content ui-corner-all" />
				</div>
			</div>
			<div class="form-item">
				<div class="form-item-label">
					<label>
						<@spring.message code='dashboardGlobalRes.resourceContent' />
					</label>
				</div>
				<div class="form-item-value">
					<textarea name="resourceContent" style="display:none;">${resourceContent!''}</textarea>
					<div class="resource-editor-wrapper ui-widget ui-widget-content ui-corner-all">
						<div id="${pageId}-resourceEditor" class="resource-editor code-editor"></div>
					</div>
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
<#include "../include/page_obj_codeEditor.ftl" >
<script type="text/javascript">
(function(po)
{
	po.initFormBtns();
	
	po.element(".resource-editor-wrapper").height($(window).height()*5/9);
	
	var resourcePath = po.element("input[name='savePath']").val();
	
	var resourceEditorOptions =
	{
		value: po.element("textarea[name='resourceContent']").val(),
		matchBrackets: true,
		matchTags: true,
		autoCloseTags: true,
		readOnly: ${readonly?string("true","false")},
		mode: po.evalCodeModeByName(resourcePath)
	};
	
	po.resourceEditor = po.createCodeEditor(po.element("#${pageId}-resourceEditor"), resourceEditorOptions);
	po.resourceEditor.focus();
	
	po.validateForm(
	{
		rules :
		{
			savePath : "required"
		},
		messages :
		{
			savePath : "<@spring.message code='validation.required' />"
		},
		submitHandler : function(form)
		{
			po.element("textarea[name='resourceContent']").val(po.getCodeText(po.resourceEditor));
			
			$(form).ajaxSubmitJson(
			{
				success : function(operationMessage)
				{
					po.pageParamCallAfterSave(true, operationMessage.data);
				}
			});
		}
	});
})
(${pageId});
</script>
</body>
</html>
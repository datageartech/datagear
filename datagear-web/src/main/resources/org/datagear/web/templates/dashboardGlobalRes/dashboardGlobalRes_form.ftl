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
<html>
<head>
<#include "../include/html_head.ftl">
<title><#include "../include/html_title_app_name.ftl"><@spring.message code='${titleMessageKey}' /></title>
</head>
<body>
<#include "../include/page_js_obj.ftl" >
<div id="${pageId}" class="page-form page-form-dashboardGlobalResContent">
	<form id="${pageId}-form" action="${contextPath}/dashboardGlobalRes/${formAction}" method="POST">
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
					<input type="text" name="savePath" value="${resourcePath}" class="ui-widget ui-widget-content" />
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
					<div class="resource-editor-wrapper ui-widget ui-widget-content">
						<div id="${pageId}-resourceEditor" class="resource-editor"></div>
					</div>
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
	
	po.element(".resource-editor-wrapper").height($(window).height()*5/9);
	
	var resourcePath = po.element("input[name='savePath']").val();
	po.resourceEditor = ace.edit("${pageId}-resourceEditor");
	po.resourceEditor.setShowPrintMargin(false);
	ace.require("ace/ext/language_tools");
	if($.isHtmlFile(resourcePath))
	{
		var HtmlMode = ace.require("ace/mode/html").Mode;
		po.resourceEditor.session.setMode(new HtmlMode());
	}
	else if($.isJsFile(resourcePath))
	{
		var JsMode = ace.require("ace/mode/javascript").Mode;
		po.resourceEditor.session.setMode(new JsMode());
	}
	else if($.isCssFile(resourcePath))
	{
		var CssMode = ace.require("ace/mode/css").Mode;
		po.resourceEditor.session.setMode(new CssMode());
	}
	
	var cursor = {row: 0, column: 0};
	po.resourceEditor.session.insert(cursor, po.element("textarea[name='resourceContent']").val());
	
	po.resourceEditor.moveCursorToPosition(cursor);
	po.resourceEditor.focus();
	<#if readonly>
	po.resourceEditor.setReadOnly(true);
	</#if>
	
	po.form().validate(
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
			po.element("textarea[name='resourceContent']").val(po.resourceEditor.getValue());
			
			$(form).ajaxSubmitJson(
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
})
(${pageId});
</script>
</body>
</html>
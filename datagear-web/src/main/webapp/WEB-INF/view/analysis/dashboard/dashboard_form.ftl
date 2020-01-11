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
<div id="${pageId}" class="page-form page-form-dashboard">
	<form id="${pageId}-form" action="${contextPath}/analysis/dashboard/${formAction}" method="POST">
		<div class="form-head"></div>
		<div class="form-content">
			<input type="hidden" name="id" value="${(dashboard.id)!''?html}" />
			<input type="hidden" name="template" value="${(dashboard.template)!''?html}" />
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='dashboard.name' /></label>
				</div>
				<div class="form-item-value">
					<input type="text" name="name" value="${(dashboard.name)!''?html}" class="ui-widget ui-widget-content" />
				</div>
			</div>
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='dashboard.template' /></label>
				</div>
				<div class="form-item-value">
					<textarea name="templateContent" class="ui-widget ui-widget-content" style="display: none;">${templateContent!''?html}</textarea>
					<div class="ui-widget ui-widget-content template-editor-wrapper">
						<div id="${pageId}-template-editor" class="template-editor"></div>
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
<#include "../../include/page_js_obj.ftl" >
<#include "../../include/page_obj_form.ftl">
<script type="text/javascript">
(function(po)
{
	$.initButtons(po.element());
	po.element(".template-editor-wrapper").height($(window).height()/5*3);

	po.url = function(action)
	{
		return "${contextPath}/analysis/dashboard/" + action;
	};
	
	po.templateEditorCompleters =
	[
		{
			identifierRegexps : [/[a-zA-Z_0-9\.\$]/],
			getCompletions: function(editor, session, pos, prefix, callback)
			{
				return [];
			}
		}
	];
	var languageTools = ace.require("ace/ext/language_tools");
	var HtmlMode = ace.require("ace/mode/html").Mode;
	po.templateEditor = ace.edit("${pageId}-template-editor");
	po.templateEditor.session.setMode(new HtmlMode());
	po.templateEditor.setShowPrintMargin(false);
	po.templateEditor.setOptions(
	{
		enableBasicAutocompletion: po.templateEditorCompleters,
		enableLiveAutocompletion: po.templateEditorCompleters
	});
	po.templateEditor.focus();
	var cursor = po.templateEditor.getCursorPosition();
	po.templateEditor.moveCursorToPosition(cursor);
	po.templateEditor.session.insert(cursor, po.element("textarea[name='templateContent']").val());
	<#if readonly>
	po.templateEditor.setReadOnly(true);
	</#if>
	
	<#if !readonly>
	$.validator.addMethod("dashboardTemplateContent", function(value, element)
	{
		var html = po.templateEditor.getValue();
		return html.length > 0;
	});
	
	po.form().validate(
	{
		ignore : "",
		rules :
		{
			"name" : "required",
			"templateContent" : "dashboardTemplateContent"
		},
		messages :
		{
			"name" : "<@spring.message code='validation.required' />",
			"templateContent" : "<@spring.message code='validation.required' />"
		},
		submitHandler : function(form)
		{
			po.element("textarea[name='templateContent']").val(po.templateEditor.getValue());
			
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
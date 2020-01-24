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
				<div class="form-item-value form-item-value-template">
					<textarea name="templateContent" class="ui-widget ui-widget-content" style="display: none;">${templateContent!''?html}</textarea>
					<div class="ui-widget ui-widget-content template-editor-wrapper">
						<div id="${pageId}-template-editor" class="template-editor"></div>
					</div>
					<#if !readonly>
					<button type="button" class="insert-chart-button"><@spring.message code='dashboard.insertChart' /></button>
					</#if>
				</div>
			</div>
		</div>
		<div class="form-foot" style="text-align:center;">
			<#if !readonly>
			<input type="submit" value="<@spring.message code='save' />" class="recommended" />
			&nbsp;&nbsp;
			<button type="button" name="saveAndShow"><@spring.message code='dashboard.saveAndShow' /></button>
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
	
	po.initTemplateEditor = function()
	{
		var templateEditorCompleters =
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
		var cursor = {row: 0, column: 0};
		po.templateEditor.session.insert(cursor, po.element("textarea[name='templateContent']").val());
		var found = po.templateEditor.find("</body>",{backwards: true, wrap: false, caseSensitive: false, wholeWord: false, regExp: false});
		if(found && found.start && found.start.row > 0)
		{
			cursor = {row: found.start.row-1, column: 0};
			po.templateEditor.moveCursorToPosition(cursor);
			var selection = po.templateEditor.session.getSelection();
			selection.clearSelection();
		}
		//滚动到底部
		po.templateEditor.session.setScrollTop(1000);
		<#if readonly>
		po.templateEditor.setReadOnly(true);
		</#if>
	};

	<#if !readonly>
	po.insertChartCode = function(charts)
	{
		if(!charts || !charts.length)
			return;
		
		var code = "";
		
		for(var i=0; i<charts.length; i++)
			code += "  <div class=\"dg-chart\" dg-chart-widget=\""+charts[i].id+"\">" + "<!--"+charts[i].name+"-->" + "</div>\n";
		
		var cursor = po.templateEditor.getCursorPosition();
		po.templateEditor.moveCursorToPosition(cursor);
		po.templateEditor.session.insert(cursor, code);
		po.templateEditor.focus();
	};
	po.element(".insert-chart-button").click(function()
	{
		var options =
		{
			pageParam :
			{
				submit : function(charts)
				{
					po.insertChartCode(charts);
				}
			}
		};
		
		$.setGridPageHeightOption(options);
		
		po.open("${contextPath}/analysis/chart/select?multiple", options);
	});
	
	po.showAfterSave = false;
	
	po.element("button[name=saveAndShow]").click(function()
	{
		po.showAfterSave = true;
		po.element("input[type='submit']").click();
	});
	
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
				success : function(response)
				{
					var dashboard = response.data;
					po.element("input[name='id']").val(dashboard.id);
					
					var close = (po.pageParamCall("afterSave")  == true);
					
					if(close)
						po.close();
					
					if(po.showAfterSave)
						window.open(po.url("show/"+dashboard.id+"/index"), dashboard.id);
				},
				complete: function()
				{
					po.showAfterSave = false;
				}
			});
		},
		errorPlacement : function(error, element)
		{
			error.appendTo(element.closest(".form-item-value"));
		}
	});
	</#if>
	
	po.initTemplateEditor();
})
(${pageId});
</script>
</body>
</html>
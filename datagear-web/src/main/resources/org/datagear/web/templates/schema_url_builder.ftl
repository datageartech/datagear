<#include "include/import_global.ftl">
<#include "include/html_doctype.ftl">
<html>
<head>
<#include "include/html_head.ftl">
<title><#include "include/html_title_app_name.ftl"><@spring.message code='schemaUrlBuilder.schemaUrlBuilder' /></title>
</head>
<body>
<div id="${pageId}" class="page-form page-form-schemaUrlBuilder">
	<form id="${pageId}-form" action="${contextPath}/schemaUrlBuilder/saveScriptCode" method="POST">
		<div class="form-head"></div>
		<div class="form-content">
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='schemaUrlBuilder.scriptCode' /></label>
				</div>
				<div class="form-item-value form-item-value-scriptCode">
					<textarea name="scriptCode" class="ui-widget ui-widget-content script-code-textarea">${scriptCode?html}</textarea>
					<div class="script-code-note">
					<span><@spring.message code='schemaUrlBuilder.scriptCodeNote.0' /></span>
					<pre>
{
   //<@spring.message code='schemaUrlBuilder.scriptCodeNote.required' /><@spring.message code='comma' /><@spring.message code='schemaUrlBuilder.scriptCodeNote.dbType' />
   dbType : "...",
   
   //<@spring.message code='schemaUrlBuilder.scriptCodeNote.required' /><@spring.message code='comma' /><@spring.message code='schemaUrlBuilder.scriptCodeNote.template' />
   template : "...{host}...{port}...{name}...",
   
   //<@spring.message code='schemaUrlBuilder.scriptCodeNote.optional' /><@spring.message code='comma' /><@spring.message code='schemaUrlBuilder.scriptCodeNote.defaultValue' />
   defaultValue : { host : "...", port : "...", name : "" }
}</pre>
					<span><@spring.message code='schemaUrlBuilder.scriptCodeNote.1' /></span>
					</div>
				</div>
			</div>
			<div class="form-item">
				<div class="form-item-label">
					<label>&nbsp;</label>
				</div>
				<div class="form-item-value">
					<button id="previewScriptCode" type="button" class="preview-script-code-button"><@spring.message code='preview' /></button>
				</div>
			</div>
		</div>
		<div class="form-foot" style="text-align:center;">
			<input type="submit" value="<@spring.message code='save' />" class="recommended" />
			&nbsp;&nbsp;
			<input type="reset" value="<@spring.message code='reset' />" />
		</div>
	</form>
</div>
<#include "include/page_js_obj.ftl">
<#include "include/page_obj_form.ftl">
<script type="text/javascript">
(function(po)
{
	$.initButtons(po.element());
	
	po.element("#previewScriptCode").click(function()
	{
		po.open("${contextPath}/schemaUrlBuilder/previewScriptCode",
		{
			data : { "scriptCode" : po.element("textarea[name='scriptCode']").val() }
		});
	});
	
	po.form().validate(
	{
		submitHandler : function(form)
		{
			$(form).ajaxSubmit(
			{
				success : function(response)
				{
					po.pageParamCallAfterSave(true);
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
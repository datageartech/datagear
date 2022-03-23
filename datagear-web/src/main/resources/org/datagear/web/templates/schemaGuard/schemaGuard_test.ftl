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
-->
<#assign formAction=(formAction!'#')>
<html>
<head>
<#include "../include/html_head.ftl">
<title><#include "../include/html_title_app_name.ftl"><@spring.message code='${titleMessageKey}' /></title>
</head>
<body>
<#include "../include/page_js_obj.ftl" >
<div id="${pageId}" class="page-form page-form-schemaGuardTest">
	<form id="${pageId}-form" action="${contextPath}/schemaGuard/${formAction}" method="POST">
		<div class="form-head"></div>
		<div class="form-content">
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='schemaGuard.schemaUrl' /></label>
				</div>
				<div class="form-item-value">
					<input type="text" name="url" value="" class="ui-widget ui-widget-content ui-corner-all" />
					<span id="schemaBuildUrlHelp" class="ui-state-default ui-corner-all" style="cursor: pointer;" title="<@spring.message code='schema.urlHelp' />"><span class="ui-icon ui-icon-help"></span></span>&nbsp;
				</div>
			</div>
			<div class="form-item">
				<div class="form-item-label">
					<label>&nbsp;</label>
				</div>
				<div class="form-item-value">
					<div class="test-url">&nbsp;</div>
					<span class="test-result ui-corner-all">&nbsp;</span>
				</div>
			</div>
		</div>
		<div class="form-foot">
			<input type="submit" value="<@spring.message code='test' />" class="recommended" />
		</div>
	</form>
</div>
<#include "../include/page_obj_form.ftl">
<script type="text/javascript">
(function(po)
{
	po.initFormBtns();
	
	po.element("#schemaBuildUrlHelp").click(function()
	{
		po.open("${contextPath}/schemaUrlBuilder/buildUrl",
		{
			data : { url : po.element("input[name='url']").val() },
			width: "60%",
			pageParam :
			{
				"setSchemaUrl" : function(url)
				{
					po.element("input[name='url']").val(url);
				}
			}
		});
	});
	
	po.validateForm(
	{
		rules :
		{
			url : "required"
		},
		messages :
		{
			url : "<@spring.message code='validation.required' />"
		},
		submitHandler : function(form)
		{
			$(form).ajaxSubmitJson(
			{
				success : function(response, textStatus, jqXHR, formData)
				{
					var permitted = response.data;
					
					po.element(".test-url").text(formData.url);
					
					if(permitted)
						po.element(".test-result").removeClass("denied ui-state-error").addClass("permitted ui-state-default")
						.html("<@spring.message code='schemaGuard.testSchemaUrl.permitted' />");
					else
						po.element(".test-result").removeClass("permitted ui-state-default").addClass("denied ui-state-error")
						.html("<@spring.message code='schemaGuard.testSchemaUrl.denied' />");
				}
			});
		}
	});
})
(${pageId});
</script>
</body>
</html>
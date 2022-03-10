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
<html>
<head>
<#include "../include/html_head.ftl">
<title><#include "../include/html_title_app_name.ftl"><@spring.message code='dashboard.showAuth' /></title>
</head>
<body>
<#include "../include/page_js_obj.ftl" >
<div id="${pageId}" class="page-dashboard-show-auth">
	<div class="main-page-head">
		<#include "../include/html_logo.ftl">
	</div>
	<div class="page-form page-form-dashboard-show-auth">
		<form id="${pageId}-form" action="#" method="POST" class="display-block" autocomplete="off">
			<div class="form-head"></div>
			<div class="form-content">
				<input type="hidden" name="id" value="${id!''}" />
				<input type="hidden" name="name" value="${name!''}" />
				<input type="hidden" name="redirectPath" value="${redirectPath!''}" />
				<div class="form-item">
					<div class="form-item-label">
						<label><@spring.message code='dashboard.showAuth.password' /></label>
					</div>
					<div class="form-item-value">
						<input type="password" name="password" value="" class="ui-widget ui-widget-content ui-corner-all" maxlength="20" autocomplete="off" autofocus="autofocus" />
					</div>
				</div>
			</div>
			<div class="form-foot">
				<button type="submit" class="recommended"><@spring.message code='confirm' /></button>
			</div>
		</form>
	</div>
</div>
<#include "../include/page_obj_form.ftl">
<script type="text/javascript">
(function(po)
{
	$.initButtons(po.element());
	
	po.form().validate(
	{
		rules : {},
		messages : {},
		submitHandler : function(form)
		{
			$(form).ajaxSubmitJson(
			{
				handleData: function(data)
				{
					data.confirmPassword = undefined;
				},
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
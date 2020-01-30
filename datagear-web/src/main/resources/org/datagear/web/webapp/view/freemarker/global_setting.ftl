<#include "include/import_global.ftl">
<#include "include/html_doctype.ftl">
<html>
<head>
<#include "include/html_head.ftl">
<title><#include "include/html_title_app_name.ftl"><@spring.message code='globalSetting.smtpSetting' /></title>
</head>
<body>
<div id="${pageId}" class="page-form page-form-globalSetting">
	<form id="${pageId}-form" action="${contextPath}/globalSetting/save" method="POST">
		<div class="form-head"></div>
		<div class="form-content">
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='globalSetting.smtpSetting.host' /></label>
				</div>
				<div class="form-item-value">
					<input type="text" name="smtpSetting.host" value="${(globalSetting.smtpSetting.host)!''?html}" class="ui-widget ui-widget-content" />
				</div>
			</div>
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='globalSetting.smtpSetting.port' /></label>
				</div>
				<div class="form-item-value">
					<input type="text" name="smtpSetting.port" value="${(globalSetting.smtpSetting.port)!''?html}" class="ui-widget ui-widget-content" />
				</div>
			</div>
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='globalSetting.smtpSetting.username' /></label>
				</div>
				<div class="form-item-value">
					<input type="text" name="smtpSetting.username" value="${(globalSetting.smtpSetting.username)!''?html}" class="ui-widget ui-widget-content" />
				</div>
			</div>
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='globalSetting.smtpSetting.password' /></label>
				</div>
				<div class="form-item-value">
					<input type="password" name="smtpSetting.password" value="${(globalSetting.smtpSetting.password)!''?html}" class="ui-widget ui-widget-content" />
				</div>
			</div>
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='globalSetting.smtpSetting.connectionType' /></label>
				</div>
				<div class="form-item-value">
					<select name="smtpSetting.connectionType">
						<option value="${connectionTypePlain}"><@spring.message code='globalSetting.smtpSetting.connectionType.PLAIN' /></option>
						<option value="${connectionTypeSsl}"><@spring.message code='globalSetting.smtpSetting.connectionType.SSL' /></option>
						<option value="${connectionTypeTls}"><@spring.message code='globalSetting.smtpSetting.connectionType.TLS' /></option>
					</select>
				</div>
			</div>
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='globalSetting.smtpSetting.systemEmail' /></label>
				</div>
				<div class="form-item-value">
					<input type="text" name="smtpSetting.systemEmail" value="${(globalSetting.smtpSetting.systemEmail)!''?html}" class="ui-widget ui-widget-content" />
				</div>
			</div>
			<div class="form-item" style="height:3em;">
				<div class="form-item-label">
					<label>&nbsp;</label>
				</div>
				<div class="form-item-value">
					<button type="button" id="testSmtpButton"><@spring.message code='globalSetting.testSmtp' /></button>
					&nbsp;&nbsp;
					<span id="testSmtpPanel" style="display: none;">
						<@spring.message code='globalSetting.testSmtp.recevierEmail' />&nbsp;
						<input type="text" name="testSmtpRecevierEmail" value="" class="ui-widget ui-widget-content" />
						<button type="button" id="testSmtpSendButton" style="vertical-align:baseline;"><@spring.message code='globalSetting.testSmtp.send' /></button>
					</span>
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
<#include "include/page_js_obj.ftl" >
<#include "include/page_obj_form.ftl">
<script type="text/javascript">
(function(po)
{
	$.initButtons(po.element());
	
	po.testSmtpRecevierEmailInput = function(){ return this.element("input[name='testSmtpRecevierEmail']"); };
	po.smtpSettingConnectionTypeSelect = function(){ return this.element("select[name='smtpSetting.connectionType']"); };
	
	po.testSmtpUrl = "${contextPath}/globalSetting/testSmtp";
	po.smtpSettingConnectionTypeSelect().val("${globalSetting.smtpSetting.connectionType?j_string}");
	po.smtpSettingConnectionTypeSelect().selectmenu(
	{
		"classes" : { "ui-selectmenu-button" : "global-setting-select" }
	});
	
	po.element("#testSmtpButton").click(function()
	{
		po.element("#testSmtpPanel").toggle();
	});
	
	po.element("#testSmtpSendButton").click(function()
	{
		var _this= $(this);
		
		var form = po.form();
		var initAction = form.attr("action");
		form.attr("action", po.testSmtpUrl);
		
		po.testSmtpRecevierEmailInput().rules("add",
		{
			"required" : true,
			"email" : true,
			messages : {"required" : "<@spring.message code='validation.required' />", "email" : "<@spring.message code='validation.email' />"}
		});
		
		form.submit();
		
		form.attr("action", initAction);
		po.testSmtpRecevierEmailInput().rules("remove");
	});
	
	po.form().validate(
	{
		rules :
		{
			"smtpSetting.host" : "required",
			"smtpSetting.port" : {"required" : true, "integer" : true},
			"smtpSetting.systemEmail" : {"required" : true, "email" : true}
		},
		messages :
		{
			"smtpSetting.host" : "<@spring.message code='validation.required' />",
			"smtpSetting.port" : {"required" : "<@spring.message code='validation.required' />", "integer" : "<@spring.message code='validation.integer' />"},
			"smtpSetting.systemEmail" : {"required" : "<@spring.message code='validation.required' />", "email" : "<@spring.message code='validation.email' />"}
		},
		submitHandler : function(form)
		{
			var $form = $(form);
			
			var isTestSmtp = (po.testSmtpUrl == $form.attr("action"));
			
			if(isTestSmtp)
				po.element("#testSmtpSendButton").button("disable");
			
			$(form).ajaxSubmit(
			{
				success : function(response)
				{
					var close = (po.pageParamCall("afterSave")  != false);
					
					if(close)
						po.close();
				},
				complete : function()
				{
					if(isTestSmtp)
						po.element("#testSmtpSendButton").button("enable");
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
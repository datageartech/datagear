<%--
/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="org.datagear.management.domain.SmtpSetting.ConnectionType" %>
<%@ include file="include/jsp_import.jsp" %>
<%@ include file="include/jsp_ajax_request.jsp" %>
<%@ include file="include/jsp_jstl.jsp" %>
<%@ include file="include/jsp_page_id.jsp" %>
<%@ include file="include/jsp_method_get_string_value.jsp" %>
<%@ include file="include/html_doctype.jsp" %>
<html>
<head>
<%@ include file="include/html_head.jsp" %>
<title><%@ include file="include/html_title_app_name.jsp" %><fmt:message key='globalSetting.smtpSetting' /></title>
</head>
<body>
<div id="${pageId}" class="page-form page-form-globalSetting">
	<form id="${pageId}-form" action="<%=request.getContextPath()%>/globalSetting/save" method="POST">
		<div class="form-head"></div>
		<div class="form-content">
			<div class="form-item">
				<div class="form-item-label">
					<label><fmt:message key='globalSetting.smtpSetting.host' /></label>
				</div>
				<div class="form-item-value">
					<input type="text" name="smtpSetting.host" value="<c:out value='${globalSetting.smtpSetting.host}' />" class="ui-widget ui-widget-content" />
				</div>
			</div>
			<div class="form-item">
				<div class="form-item-label">
					<label><fmt:message key='globalSetting.smtpSetting.port' /></label>
				</div>
				<div class="form-item-value">
					<input type="text" name="smtpSetting.port" value="<c:out value='${globalSetting.smtpSetting.port}' />" class="ui-widget ui-widget-content" />
				</div>
			</div>
			<div class="form-item">
				<div class="form-item-label">
					<label><fmt:message key='globalSetting.smtpSetting.username' /></label>
				</div>
				<div class="form-item-value">
					<input type="text" name="smtpSetting.username" value="<c:out value='${globalSetting.smtpSetting.username}' />" class="ui-widget ui-widget-content" />
				</div>
			</div>
			<div class="form-item">
				<div class="form-item-label">
					<label><fmt:message key='globalSetting.smtpSetting.password' /></label>
				</div>
				<div class="form-item-value">
					<input type="password" name="smtpSetting.password" value="<c:out value='${globalSetting.smtpSetting.password}' />" class="ui-widget ui-widget-content" />
				</div>
			</div>
			<div class="form-item">
				<div class="form-item-label">
					<label><fmt:message key='globalSetting.smtpSetting.connectionType' /></label>
				</div>
				<div class="form-item-value">
					<select name="smtpSetting.connectionType">
						<option value="<%=ConnectionType.PLAIN%>"><fmt:message key='globalSetting.smtpSetting.connectionType.PLAIN' /></option>
						<option value="<%=ConnectionType.SSL%>"><fmt:message key='globalSetting.smtpSetting.connectionType.SSL' /></option>
						<option value="<%=ConnectionType.TLS%>"><fmt:message key='globalSetting.smtpSetting.connectionType.TLS' /></option>
					</select>
				</div>
			</div>
			<div class="form-item">
				<div class="form-item-label">
					<label><fmt:message key='globalSetting.smtpSetting.systemEmail' /></label>
				</div>
				<div class="form-item-value">
					<input type="text" name="smtpSetting.systemEmail" value="<c:out value='${globalSetting.smtpSetting.systemEmail}' />" class="ui-widget ui-widget-content" />
				</div>
			</div>
			<div class="form-item" style="height:3em;">
				<div class="form-item-label">
					<label>&nbsp;</label>
				</div>
				<div class="form-item-value">
					<button type="button" id="testSmtpButton"><fmt:message key='globalSetting.testSmtp' /></button>
					&nbsp;&nbsp;
					<span id="testSmtpPanel" style="display: none;">
						<fmt:message key='globalSetting.testSmtp.recevierEmail' />&nbsp;
						<input type="text" name="testSmtpRecevierEmail" value="" class="ui-widget ui-widget-content" />
						<button type="button" id="testSmtpSendButton" style="vertical-align:baseline;"><fmt:message key='globalSetting.testSmtp.send' /></button>
					</span>
				</div>
			</div>
		</div>
		<div class="form-foot" style="text-align:center;">
			<input type="submit" value="<fmt:message key='save' />" class="recommended" />
			&nbsp;&nbsp;
			<input type="reset" value="<fmt:message key='reset' />" />
		</div>
	</form>
</div>
<%@ include file="include/page_js_obj.jsp" %>
<script type="text/javascript">
(function(pageObj)
{
	$.initButtons(pageObj.element());
	
	pageObj.form = pageObj.element("#${pageId}-form");
	pageObj.testSmtpUrl = "<%=request.getContextPath()%>/globalSetting/testSmtp";
	pageObj.testSmtpRecevierEmailInput = pageObj.element("input[name='testSmtpRecevierEmail']");
	
	pageObj.smtpSettingConnectionTypeSelect = pageObj.element("select[name='smtpSetting.connectionType']");
	pageObj.smtpSettingConnectionTypeSelect.val("<c:out value='${globalSetting.smtpSetting.connectionType}' />");
	pageObj.smtpSettingConnectionTypeSelect.selectmenu(
	{
		"classes" : { "ui-selectmenu-button" : "global-setting-select" }
	});
	
	pageObj.element("#testSmtpButton").click(function()
	{
		pageObj.element("#testSmtpPanel").toggle();
	});
	
	pageObj.element("#testSmtpSendButton").click(function()
	{
		var _this= $(this);
		
		var initAction = pageObj.form.attr("action");
		pageObj.form.attr("action", pageObj.testSmtpUrl);
		
		pageObj.testSmtpRecevierEmailInput.rules("add",
		{
			"required" : true,
			"email" : true,
			messages : {"required" : "<fmt:message key='validation.required' />", "email" : "<fmt:message key='validation.email' />"}
		});
		
		pageObj.form.submit();
		
		pageObj.form.attr("action", initAction);
		pageObj.testSmtpRecevierEmailInput.rules("remove");
	});
	
	pageObj.form.validate(
	{
		rules :
		{
			"smtpSetting.host" : "required",
			"smtpSetting.port" : {"required" : true, "integer" : true},
			"smtpSetting.systemEmail" : {"required" : true, "email" : true}
		},
		messages :
		{
			"smtpSetting.host" : "<fmt:message key='validation.required' />",
			"smtpSetting.port" : {"required" : "<fmt:message key='validation.required' />", "integer" : "<fmt:message key='validation.integer' />"},
			"smtpSetting.systemEmail" : {"required" : "<fmt:message key='validation.required' />", "email" : "<fmt:message key='validation.email' />"}
		},
		submitHandler : function(form)
		{
			var $form = $(form);
			
			var isTestSmtp = (pageObj.testSmtpUrl == $form.attr("action"));
			
			if(isTestSmtp)
				pageObj.element("#testSmtpSendButton").button("disable");
			
			$(form).ajaxSubmit(
			{
				success : function(response)
				{
					var pageParam = pageObj.pageParam();
					
					var close = false;
					
					if(pageParam && pageParam.afterSave)
						close = (pageParam.afterSave() != false);
					
					if(close)
						pageObj.close();
				},
				complete : function()
				{
					if(isTestSmtp)
						pageObj.element("#testSmtpSendButton").button("enable");
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
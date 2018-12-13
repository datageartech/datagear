<%--
/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="org.datagear.web.controller.DriverEntityController" %>
<%@ include file="../include/jsp_import.jsp" %>
<%@ include file="../include/jsp_ajax_request.jsp" %>
<%@ include file="../include/jsp_jstl.jsp" %>
<%@ include file="../include/jsp_page_id.jsp" %>
<%@ include file="../include/jsp_method_get_string_value.jsp" %>
<%@ include file="../include/html_doctype.jsp" %>
<html>
<head>
<%@ include file="../include/html_head.jsp" %>
<title><%@ include file="../include/html_title_app_name.jsp" %><fmt:message key='driverEntity.importDriverEntity' /></title>
</head>
<body>
<div id="${pageId}" class="page-form page-form-driverEntityImport">
	<form id="${pageId}-form" action="<%=request.getContextPath()%>/driverEntity/saveImport" method="POST">
		<div class="form-head"></div>
		<div class="form-content">
			<input type="hidden" name="importId" value="<c:out value='${importId}' />" />
			<div class="form-item">
				<div class="form-item-label">
					<label><fmt:message key='driverEntity.import.selectFile' /></label>
				</div>
				<div class="form-item-value">
					<div class="driver-import-parent">
						<div class="fileinput-button"><fmt:message key='select' /><input type="file" accept=".zip" class="ignore"></div>
						<div class="file-info"></div>
					</div>
				</div>
			</div>
			<div class="form-item">
				<div class="form-item-label">
					<label><fmt:message key='driverEntity.import.review' /></label>
				</div>
				<div class="form-item-value">
					<input type="hidden" name="inputForValidate" value="" />
					<div class="ui-widget ui-widget-content input driver-entity-infos"></div>
				</div>
			</div>
		</div>
		<div class="form-foot" style="text-align:center;">
			<input type="submit" value="<fmt:message key='import' />" class="recommended" />
		</div>
	</form>
</div>
<%@ include file="../include/page_js_obj.jsp" %>
<%@ include file="../include/page_obj_form.jsp" %>
<script type="text/javascript">
(function(po)
{
	po.element("input:submit, input:button, input:reset, button, .fileinput-button").button();
	
	po.driverEntityInfos = function(){ return this.element(".driver-entity-infos"); };

	po.fileUploadInfo = function(){ return this.element(".file-info"); };
	
	po.url = function(action)
	{
		return contextPath + "/driverEntity/" + action;
	};
	
	po.renderDriverEntityInfos = function(driverEntities)
	{
		po.driverEntityInfos().empty();
		
		for(var i=0; i<driverEntities.length; i++)
		{
			var driverEntity = driverEntities[i];
			
			var $item = $("<div class='ui-widget ui-widget-content ui-corner-all driver-entity-item' />")
				.appendTo(po.driverEntityInfos());
			
			$("<input type='hidden' />").attr("name", "driverEntity.id").attr("value", driverEntity.id).appendTo($item);
			$("<input type='hidden' />").attr("name", "driverEntity.driverClassName").attr("value", driverEntity.driverClassName).appendTo($item);
			$("<input type='hidden' />").attr("name", "driverEntity.displayName").attr("value", driverEntity.displayName).appendTo($item);
			$("<input type='hidden' />").attr("name", "driverEntity.displayDesc").attr("value", driverEntity.displayDesc).appendTo($item);
			
			$("<span class='ui-icon ui-icon-close' title='<fmt:message key='delete' />' />")
			.appendTo($item).click(function()
			{
				$(this).closest(".driver-entity-item").remove();
			});
			
			var content = driverEntity.displayText;
			$("<span class='driver-entity-info' />").attr("title", content).text(content)
			.appendTo($item);
		}
	};
	
	po.element(".fileinput-button").fileupload(
	{
		url : po.url("uploadImportFile"),
		paramName : "file",
		success : function(serverDriverEntities, textStatus, jqXHR)
		{
			$.fileuploadsuccessHandlerForUploadInfo(po.fileUploadInfo(), false);
			po.renderDriverEntityInfos(serverDriverEntities);
		}
	})
	.bind('fileuploadadd', function (e, data)
	{
		po.form().validate().resetForm();
		$.fileuploadaddHandlerForUploadInfo(e, data, po.fileUploadInfo());
	})
	.bind('fileuploadprogressall', function (e, data)
	{
		$.fileuploadprogressallHandlerForUploadInfo(e, data, po.fileUploadInfo());
	});
	
	$.validator.addMethod("importDriverEntityRequired", function(value, element)
	{
		var thisForm = $(element).closest("form");
		var $driverEntityId = $("input[name='driverEntity.id']", thisForm);
		
		return $driverEntityId.length > 0;
	});
	
	po.form().validate(
	{
		ignore : ".ignore",
		rules :
		{
			inputForValidate : "importDriverEntityRequired"
		},
		messages :
		{
			inputForValidate : "<fmt:message key='driverEntity.import.importDriverEntityRequired' />"
		},
		submitHandler : function(form)
		{
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
})
(${pageId});
</script>
</body>
</html>
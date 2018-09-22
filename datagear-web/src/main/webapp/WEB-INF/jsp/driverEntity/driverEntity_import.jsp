<%--
/*
 * Copyright (c) 2018 by datagear.org.
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
<div id="${pageId}" class="page-data-form page-data-form-driverEntityImport">
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
					<div class="ui-widget ui-widget-content driver-entity-infos"></div>
				</div>
			</div>
		</div>
		<div class="form-foot" style="text-align:center;">
			<input type="submit" value="<fmt:message key='import' />" class="recommended" />
		</div>
	</form>
</div>
<%@ include file="../include/page_js_obj.jsp" %>
<script type="text/javascript">
(function(pageObj)
{
	pageObj.element("input:submit, input:button, input:reset, button, .fileinput-button").button();
	
	pageObj.form = pageObj.element("#${pageId}-form");
	pageObj.driverEntityInfos = pageObj.element(".driver-entity-infos");

	pageObj.url = function(action)
	{
		return contextPath + "/driverEntity/" + action;
	};
	
	pageObj.renderDriverEntityInfos = function(driverEntities)
	{
		pageObj.driverEntityInfos.empty();
		
		for(var i=0; i<driverEntities.length; i++)
		{
			var driverEntity = driverEntities[i];
			
			var $item = $("<div class='ui-widget ui-widget-content ui-corner-all driver-entity-item' />")
				.appendTo(pageObj.driverEntityInfos);
			
			$("<input type='hidden' />").attr("name", "driverEntity.id").attr("value", driverEntity.id).appendTo($item);
			$("<input type='hidden' />").attr("name", "driverEntity.driverClassName").attr("value", driverEntity.driverClassName).appendTo($item);
			$("<input type='hidden' />").attr("name", "driverEntity.displayName").attr("value", driverEntity.displayName).appendTo($item);
			$("<input type='hidden' />").attr("name", "driverEntity.displayDesc").attr("value", driverEntity.displayDesc).appendTo($item);
			
			$("<span class='ui-icon ui-icon-close' title='<fmt:message key='delete' />' />")
			.appendTo($item).click(function()
			{
				$(this).closest(".driver-entity-item").remove();
			});
			
			var content = driverEntity.displayText + " ("+driverEntity.driverClassName+")";
			$("<span class='driver-entity-info' />").attr("title", content).text(content)
			.appendTo($item);
		}
	};
	
	pageObj.fileUploadInfo = pageObj.element(".file-info");
	
	pageObj.element(".fileinput-button").fileupload(
	{
		url : pageObj.url("uploadImportFile"),
		paramName : "file",
		success : function(serverDriverEntities, textStatus, jqXHR)
		{
			$.fileuploadsuccessHandlerForUploadInfo(pageObj.fileUploadInfo, false);
			pageObj.renderDriverEntityInfos(serverDriverEntities);
		}
	})
	.bind('fileuploadadd', function (e, data)
	{
		pageObj.form.validate().resetForm();
		$.fileuploadaddHandlerForUploadInfo(e, data, pageObj.fileUploadInfo);
	})
	.bind('fileuploadprogressall', function (e, data)
	{
		$.fileuploadprogressallHandlerForUploadInfo(e, data, pageObj.fileUploadInfo);
	});
	
	$.validator.addMethod("importDriverEntityRequired", function(value, element)
	{
		var thisForm = $(element).closest("form");
		var $driverEntityId = $("input[name='driverEntity.id']", thisForm);
		
		return $driverEntityId.length > 0;
	});
	
	pageObj.form.validate(
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
					var pageParam = pageObj.pageParam();
					
					var close = true;
					
					if(pageParam && pageParam.afterSave)
						close = (pageParam.afterSave() != false);
					
					if(close)
						pageObj.close();
				}
			});
		},
		errorPlacement : function(error, element)
		{
			error.appendTo(element.closest(".form-item"));
		}
	});
})
(${pageId});
</script>
</body>
</html>
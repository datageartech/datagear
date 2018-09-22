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
<%
//标题标签I18N关键字，不允许null
String titleMessageKey = getStringValue(request, DriverEntityController.KEY_TITLE_MESSAGE_KEY);
//表单提交action，允许为null
String formAction = getStringValue(request, DriverEntityController.KEY_FORM_ACTION, "#");
//是否只读操作，允许为null
boolean readonly = ("true".equalsIgnoreCase(getStringValue(request, DriverEntityController.KEY_READONLY)));
%>
<html>
<head>
<%@ include file="../include/html_head.jsp" %>
<title><%@ include file="../include/html_title_app_name.jsp" %><fmt:message key='<%=titleMessageKey%>' /></title>
</head>
<body>
<div id="${pageId}" class="page-data-form page-data-form-driverEntity">
	<form id="${pageId}-form" action="<%=request.getContextPath()%>/driverEntity/<%=formAction%>" method="POST">
		<div class="form-head"></div>
		<div class="form-content">
			<input type="hidden" name="id" value="<c:out value='${driverEntity.id}' />" />
			<div class="form-item">
				<div class="form-item-label">
					<label><fmt:message key='driverEntity.displayName' /></label>
				</div>
				<div class="form-item-value">
					<input type="text" name="displayName" value="<c:out value='${driverEntity.displayName}' />" class="ui-widget ui-widget-content" />
				</div>
			</div>
			<div class="form-item">
				<div class="form-item-label">
					<label><fmt:message key='driverEntity.driverClassName' /></label>
				</div>
				<div class="form-item-value">
					<input type="text" name="driverClassName" value="<c:out value='${driverEntity.driverClassName}' />" class="ui-widget ui-widget-content" />
				</div>
			</div>
			<div class="form-item">
				<div class="form-item-label">
					<label><fmt:message key='driverEntity.displayDesc' /></label>
				</div>
				<div class="form-item-value">
					<textarea name="displayDesc" class="ui-widget ui-widget-content"><c:out value='${driverEntity.displayDesc}' /></textarea>
				</div>
			</div>
			<div class="form-item">
				<div class="form-item-label">
					<label><fmt:message key='driverEntity.driverFiles' /></label>
				</div>
				<div class="form-item-value">
					<div class="ui-widget ui-widget-content driver-files">
					</div>
					<%if(!readonly){%>
					<div class="driver-upload-parent">
						<div class="fileinput-button"><fmt:message key='upload' /><input type="file"></div>
						<div class="file-info"></div>
					</div>
					<%}%>
				</div>
			</div>
		</div>
		<div class="form-foot" style="text-align:center;">
			<%if(!readonly){%>
			<input type="submit" value="<fmt:message key='save' />" class="recommended" />
			&nbsp;&nbsp;
			<input type="reset" value="<fmt:message key='reset' />" />
			<%}%>
		</div>
	</form>
</div>
<%@ include file="../include/page_js_obj.jsp" %>
<script type="text/javascript">
(function(pageObj)
{
	pageObj.element("input:submit, input:button, input:reset, button, .fileinput-button").button();
	
	pageObj.form = pageObj.element("#${pageId}-form");
	pageObj.driverFiles = pageObj.element(".driver-files");

	pageObj.url = function(action)
	{
		return contextPath + "/driverEntity/" + action;
	};
	
	pageObj.getDriverEntityId = function()
	{
		return pageObj.element("input[name='id']").val();
	};
	
	pageObj.renderDriverFiles = function(fileInfos)
	{
		pageObj.driverFiles.empty();
		
		for(var i=0; i<fileInfos.length; i++)
		{
			var $fileInfo = $("<div class='ui-widget ui-widget-content ui-corner-all driver-file' />")
				.appendTo(pageObj.driverFiles);
			
			<%if(!readonly){%>
			$("<input type='hidden' />").attr("name", "driverLibraryName").attr("value", fileInfos[i].name).appendTo($fileInfo);
			
			var $deleteIcon = $("<span class='ui-icon ui-icon-close driver-file-icon' title='<fmt:message key='delete' />' />")
				.attr("driverFile", fileInfos[i].name).appendTo($fileInfo);
			
			$deleteIcon.click(function()
			{
				var driverFile = $(this).attr("driverFile");
				pageObj.confirm("<fmt:message key='driverEntity.confirmDeleteDriverFile' />",
				{
					"confirm" : function()
					{
						var id = pageObj.getDriverEntityId();
						$.post(pageObj.url("deleteDriverFile"), {"id" : id, "file" : driverFile}, function(operationMessage)
						{
							pageObj.renderDriverFiles(operationMessage.data);
						});
					}
				});
			});
			<%}%>
			
			$("<a class='driver-file-info' href='javascript:void(0);' />").attr("title", fileInfos[i].name).text(fileInfos[i].name)
				.appendTo($fileInfo)
				.click(function()
				{
					var id = pageObj.getDriverEntityId();
					var driverFile = $(this).text();
					
					$.postOnForm(pageObj.url("downloadDriverFile"),
					{
						data : {"id" : id, "file" : driverFile}
					});
				});
		}
	};
	
	pageObj.refreshDriverFiles = function()
	{
		var id = pageObj.getDriverEntityId();
		
		if(id != "")
		{
			$.getJSON(pageObj.url("listDriverFile"), {"id" : id}, function(fileInfos)
			{
				pageObj.renderDriverFiles(fileInfos);
			});
		}
	};
	
	<%if(!readonly){%>
	
	pageObj.fileUploadInfo = pageObj.element(".file-info");
	
	pageObj.element(".fileinput-button").fileupload(
	{
		url : pageObj.url("uploadDriverFile"),
		paramName : "file",
		success : function(serverFileInfos, textStatus, jqXHR)
		{
			$.fileuploadsuccessHandlerForUploadInfo(pageObj.fileUploadInfo, true);
			
			pageObj.renderDriverFiles(serverFileInfos);
			
			$.tipSuccess("<fmt:message key='uploadSuccess' />");
		}
	})
	.bind('fileuploadadd', function (e, data)
	{
		$.fileuploadaddHandlerForUploadInfo(e, data, pageObj.fileUploadInfo);
	})
	.bind('fileuploadprogressall', function (e, data)
	{
		$.fileuploadprogressallHandlerForUploadInfo(e, data, pageObj.fileUploadInfo);
	});

	pageObj.form.validate(
	{
		rules :
		{
			displayName : "required",
			driverClassName : "required"
		},
		messages :
		{
			displayName : "<fmt:message key='validation.required' />",
			driverClassName : "<fmt:message key='validation.required' />"
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
	<%}%>
	
	<%if(!"saveAdd".equals(formAction)){%>
	pageObj.refreshDriverFiles();
	<%}%>
})
(${pageId});
</script>
</body>
</html>
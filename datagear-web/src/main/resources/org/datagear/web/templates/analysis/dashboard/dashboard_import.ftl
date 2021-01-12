<#include "../../include/import_global.ftl">
<#include "../../include/html_doctype.ftl">
<html>
<head>
<#include "../../include/html_head.ftl">
<title><#include "../../include/html_title_app_name.ftl"><@spring.message code='dashboard.importDashboard' /></title>
</head>
<body>
<#include "../../include/page_js_obj.ftl" >
<div id="${pageId}" class="page-form page-form-importDashboard">
	<form id="${pageId}-form" action="${contextPath}/analysis/dashboard/saveImport" method="POST">
		<div class="form-head"></div>
		<div class="form-content">
			<input type="hidden" name="dashboardFileName" value="" />
			<div class="form-item form-item-analysisProjectAware">
				<div class="form-item-label">
					<label title="<@spring.message code='dashboard.import.desc' />">
						<@spring.message code='dashboard.import.selectFile' />
					</label>
				</div>
				<div class="form-item-value">
					<input type="hidden" name="inputForValidate" value="" />
					<div class="fileinput-button">
						<@spring.message code='select' /><input type="file" accept=".html, .htm, .zip" class="ignore">
					</div>
					<div class="upload-file-info"></div>
				</div>
				<#assign readonly=false>
				<#include "../include/analysisProjectAware_form_select.ftl" >
			</div>
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='dashboard.name' /></label>
				</div>
				<div class="form-item-value">
					<input type="text" name="name" value="" class="ui-widget ui-widget-content" />
				</div>
			</div>
			<div class="form-item">
				<div class="form-item-label">
					<label title="<@spring.message code='dashboard.import.templateName.desc' />">
						<@spring.message code='dashboard.templateName' />
					</label>
				</div>
				<div class="form-item-value">
					<input type="text" name="template" value="" class="ui-widget ui-widget-content" />
				</div>
			</div>
		</div>
		<div class="form-foot" style="text-align:center;">
			<input type="submit" value="<@spring.message code='save' />" class="recommended" />
		</div>
	</form>
</div>
<#include "../../include/page_obj_form.ftl">
<script type="text/javascript">
(function(po)
{
	$.initButtons(po.element());
	po.element(".fileinput-button").button();
	po.initAnalysisProject("${((dashboard.analysisProject.id)!'')?js_string?no_esc}", "${((dashboard.analysisProject.name)!'')?js_string?no_esc}");
	
	po.url = function(action)
	{
		return "${contextPath}/analysis/dashboard/" + action;
	};
	
	po.fileUploadInfo = function(){ return this.element(".upload-file-info"); };
	
	po.element(".fileinput-button").fileupload(
	{
		url : po.url("uploadImportFile"),
		paramName : "file",
		success : function(uploadResult, textStatus, jqXHR)
		{
			$.fileuploadsuccessHandlerForUploadInfo(po.fileUploadInfo(), false);
			po.element("input[name='name']").val(uploadResult.dashboardName);
			po.element("input[name='template']").val(uploadResult.template);
			po.element("input[name='dashboardFileName']").val(uploadResult.dashboardFileName);
		}
	})
	.bind('fileuploadadd', function (e, data)
	{
		po.element("input[name='dashboardFileName']").val("");
		$.fileuploadaddHandlerForUploadInfo(e, data, po.fileUploadInfo());
	})
	.bind('fileuploadprogressall', function (e, data)
	{
		$.fileuploadprogressallHandlerForUploadInfo(e, data, po.fileUploadInfo());
	});

	$.validator.addMethod("uploadDashboardFileRequired", function(value, element)
	{
		var thisForm = $(element).closest("form");
		var dashboardFileName = $("input[name='dashboardFileName']", thisForm).val();
		
		return dashboardFileName.length > 0;
	});
	
	po.form().validate(
	{
		ignore : ".ignore",
		rules :
		{
			name : "required",
			inputForValidate : "uploadDashboardFileRequired",
			template : "required"
		},
		messages :
		{
			name : "<@spring.message code='validation.required' />",
			inputForValidate : "<@spring.message code='dashboard.import.validation.importDashboardFileRequired' />",
			template : "<@spring.message code='validation.required' />"
		},
		submitHandler : function(form)
		{
			$(form).ajaxSubmit(
			{
				success : function()
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
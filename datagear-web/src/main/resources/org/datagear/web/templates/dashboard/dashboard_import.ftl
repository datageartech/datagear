<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#include "../include/page_import.ftl">
<#include "../include/html_doctype.ftl">
<html>
<head>
<#include "../include/html_head.ftl">
<title><#include "../include/html_title_app_name.ftl"><@spring.message code='dashboard.importDashboard' /></title>
</head>
<body>
<#include "../include/page_obj.ftl" >
<div id="${pageId}" class="page-form page-form-importDashboard">
	<form id="${pageId}form" action="${contextPath}/dashboard/saveImport" method="POST">
		<div class="form-head"></div>
		<div class="form-content">
			<input type="hidden" name="dashboardFileName" value="" />
			<div class="form-item form-item-analysisProjectAware">
				<div class="form-item-label">
					<label title="<@spring.message code='dashboard.import.desc' />">
						<@spring.message code='dashboard.import.selectFile' />
					</label>
				</div>
				<div class="uploadFileWrapper form-item-value">
					<input type="hidden" name="inputForValidate" value="" />
					<div class="fileinput-button button">
						<@spring.message code='select' /><input type="file" accept=".html, .htm, .zip" class="ignore">
					</div>
					<div class="upload-file-info"></div>
				</div>
				<#assign readonly=false>
				<#include "../include/analysisProjectAware_form_select.ftl" >
			</div>
			<div class="form-item form-item-encoding">
				<div class="form-item-label">
					<label title="<@spring.message code='dashboard.import.zipFileNameEncoding.desc' />">
						<@spring.message code='dashboard.import.zipFileNameEncoding' />
					</label>
				</div>
				<div class="form-item-value">
					<select name="zipFileNameEncoding">
						<#list availableCharsetNames as item>
						<option value="${item}" <#if item == zipFileNameEncodingDefault>selected="selected"</#if>>${item}</option>
						</#list>
					</select>
				</div>
			</div>
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='dashboard.name' /></label>
				</div>
				<div class="form-item-value">
					<input type="text" name="name" value="" required="required" maxlength="100" class="ui-widget ui-widget-content ui-corner-all" />
				</div>
			</div>
			<div class="form-item">
				<div class="form-item-label">
					<label title="<@spring.message code='dashboard.import.templateName.desc' />">
						<@spring.message code='dashboard.templateName' />
					</label>
				</div>
				<div class="form-item-value">
					<input type="text" name="template" value="" required="required" maxlength="200" class="ui-widget ui-widget-content ui-corner-all" />
				</div>
			</div>
			<div class="form-item">
				<div class="form-item-label">
				</div>
				<div class="form-item-value minor">
					<@spring.message code='dashboard.import.notice' />
					<br>
					<@spring.message code='dashboard.import.notice.1' />
					<br>
					<@spring.message code='dashboard.import.notice.2' />
					<br>
					<@spring.message code='dashboard.import.notice.3' />
				</div>
			</div>
		</div>
		<div class="form-foot">
			<button type="submit" class="recommended"><@spring.message code='save' /></button>
		</div>
	</form>
</div>
<#include "../include/page_obj_form.ftl">
<script type="text/javascript">
(function(po)
{
	po.initFormBtns();
	po.initAnalysisProject("${((dashboard.analysisProject.id)!'')?js_string?no_esc}", "${((dashboard.analysisProject.name)!'')?js_string?no_esc}");
	po.elementOfName("zipFileNameEncoding").selectmenu({ appendTo : po.element(), classes : { "ui-selectmenu-menu" : "encoding-selectmenu-menu" } });
	
	po.url = function(action)
	{
		return "${contextPath}/dashboard/" + action;
	};
	
	po.element(".uploadFileWrapper").fileUpload(po.url("uploadImportFile"),
	{
		add: function(e, data)
		{
			po.elementOfName("dashboardFileName").val("");
		},
		success: function(response)
		{
			po.elementOfName("name").val(response.dashboardName);
			po.elementOfName("template").val(response.template);
			po.elementOfName("dashboardFileName").val(response.dashboardFileName);
		}
	});
	
	$.validator.addMethod("uploadDashboardFileRequired", function(value, element)
	{
		var dashboardFileName = po.elementOfName("dashboardFileName").val();
		return dashboardFileName.length > 0;
	});
	
	po.validateAjaxJsonForm(
	{
		ignore : ".ignore",
		rules :
		{
			inputForValidate : "uploadDashboardFileRequired"
		},
		messages :
		{
			inputForValidate : "<@spring.message code='dashboard.import.validation.importDashboardFileRequired' />"
		}
	},
	{
		ignore: "inputForValidate"
	});
})
(${pageId});
</script>
</body>
</html>
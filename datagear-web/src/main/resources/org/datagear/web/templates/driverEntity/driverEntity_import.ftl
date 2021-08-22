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
<title><#include "../include/html_title_app_name.ftl"><@spring.message code='driverEntity.importDriverEntity' /></title>
</head>
<body>
<#include "../include/page_js_obj.ftl" >
<div id="${pageId}" class="page-form page-form-driverEntityImport">
	<form id="${pageId}-form" action="${contextPath}/driverEntity/saveImport" method="POST">
		<div class="form-head"></div>
		<div class="form-content">
			<input type="hidden" name="importId" value="${importId}" />
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='driverEntity.import.selectFile' /></label>
				</div>
				<div class="form-item-value">
					<div class="driver-import-parent">
						<div class="fileinput-button"><@spring.message code='select' /><input type="file" accept=".zip" class="ignore"></div>
						<div class="upload-file-info"></div>
					</div>
				</div>
			</div>
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='driverEntity.import.review' /></label>
				</div>
				<div class="form-item-value">
					<input type="hidden" name="inputForValidate" value="" />
					<div class="driver-entity-infos minor-list deletable-list input ui-widget ui-widget-content"></div>
				</div>
			</div>
		</div>
		<div class="form-foot" style="text-align:center;">
			<input type="submit" value="<@spring.message code='import' />" class="recommended" />
		</div>
	</form>
</div>
<#include "../include/page_obj_form.ftl">
<script type="text/javascript">
(function(po)
{
	po.element("input:submit, input:button, input:reset, button, .fileinput-button").button();
	
	po.driverEntityInfos = function(){ return this.element(".driver-entity-infos"); };

	po.fileUploadInfo = function(){ return this.element(".upload-file-info"); };
	
	po.url = function(action)
	{
		return "${contextPath}/driverEntity/" + action;
	};
	
	po.renderDriverEntityInfos = function(driverEntities)
	{
		po.driverEntityInfos().empty();
		
		for(var i=0; i<driverEntities.length; i++)
		{
			var driverEntity = driverEntities[i];
			
			var $item = $("<div class='driver-entity-item minor-list-item ui-widget ui-widget-content ui-corner-all' />")
				.appendTo(po.driverEntityInfos());
			
			$("<input type='hidden' />").attr("name", "driverEntity.id").attr("value", driverEntity.id).appendTo($item);
			$("<input type='hidden' />").attr("name", "driverEntity.driverClassName").attr("value", driverEntity.driverClassName).appendTo($item);
			$("<input type='hidden' />").attr("name", "driverEntity.displayName").attr("value", driverEntity.displayName).appendTo($item);
			$("<input type='hidden' />").attr("name", "driverEntity.displayDesc").attr("value", driverEntity.displayDesc).appendTo($item);
			
			$("<span class='delete-icon ui-icon ui-icon-close' title='<@spring.message code='delete' />' />")
			.appendTo($item).click(function()
			{
				$(this).closest(".driver-entity-item").remove();
			});
			
			var content = driverEntity.displayText;
			$("<span class='driver-entity-info item-content' />").attr("title", content).text(content)
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
			inputForValidate : "<@spring.message code='driverEntity.import.importDriverEntityRequired' />"
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
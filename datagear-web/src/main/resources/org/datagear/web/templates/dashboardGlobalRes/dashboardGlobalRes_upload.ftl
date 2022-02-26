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
readonly 是否只读操作，允许为null
-->
<#assign formAction=(formAction!'#')>
<#assign readonly=(readonly!false)>
<html>
<head>
<#include "../include/html_head.ftl">
<title><#include "../include/html_title_app_name.ftl"><@spring.message code='${titleMessageKey}' /></title>
</head>
<body>
<#include "../include/page_js_obj.ftl" >
<div id="${pageId}" class="page-form page-form-dashboardGlobalRes">
	<form id="${pageId}-form" action="${contextPath}/dashboardGlobalRes/${formAction}" method="POST">
		<div class="form-head"></div>
		<div class="form-content">
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='dashboardGlobalRes.selectFile' /></label>
				</div>
				<div class="form-item-value">
					<input type="hidden" name="filePath" value="" />
					<input type="hidden" name="fileName" value="" />
					<div class="ui-widget ui-corner-all ui-button fileinput-button">
						<@spring.message code='select' /><input type="file">
					</div>
					<div class="upload-file-info"></div>
				</div>
			</div>
			<div class="form-item form-item-savePath">
				<div class="form-item-label">
					<label title="<@spring.message code='dashboardGlobalRes.savePath.desc' />">
						<@spring.message code='dashboardGlobalRes.savePath' />
					</label>
				</div>
				<div class="form-item-value">
					<input type="text" name="savePath" class="ui-widget ui-widget-content ui-corner-all" />
				</div>
			</div>
			<div class="form-item">
				<div class="form-item-label">
					<label title="<@spring.message code='dashboardGlobalRes.autoUnzip.desc' />">
						<@spring.message code='dashboardGlobalRes.autoUnzip' />
					</label>
				</div>
				<div class="form-item-value">
					<div class="autoUnzip-radios">
						<label for="${pageId}-autoUnzip_true" title="">
							<@spring.message code='yes' />
						</label>
			   			<input type="radio" id="${pageId}-autoUnzip_true" name="autoUnzip" value="true" />
						<label for="${pageId}-autoUnzip_false" title="">
							<@spring.message code='no' />
						</label>
			   			<input type="radio" id="${pageId}-autoUnzip_false" name="autoUnzip" value="false" checked="checked" />
					</div>
				</div>
			</div>
			<div class="form-item form-item-encoding">
				<div class="form-item-label">
					<label title="<@spring.message code='dashboardGlobalRes.upload.zipFileNameEncoding.desc' />">
						<@spring.message code='dashboardGlobalRes.upload.zipFileNameEncoding' />
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
				</div>
				<div class="form-item-value minor">
					<@spring.message code='dashboardGlobalRes.upload.notice' />
				</div>
			</div>
		</div>
		<div class="form-foot">
			<#if !readonly>
			<input type="submit" value="<@spring.message code='save' />" class="recommended" />
			</#if>
		</div>
	</form>
</div>
<#include "../include/page_obj_form.ftl">
<script type="text/javascript">
(function(po)
{
	$.initButtons(po.element());
	po.element(".autoUnzip-radios").checkboxradiogroup();
	po.element("select[name='zipFileNameEncoding']").selectmenu({ appendTo: po.element(), position: {my: "left bottom", at: "left top"}, classes: { "ui-selectmenu-menu" : "encoding-selectmenu-menu" } });
	
	po.isZipExtention = function(resName)
	{
		var reg = /\.(zip)$/gi;
		return (resName && reg.test(resName));
	};
	
	po.element("input[name='autoUnzip']").on("change", function()
	{
		var val = $(this).val();
		var $savePath = po.element("input[name='savePath']");
		
		if(val == "true")
		{
			var savePath = po.element("input[name='savePath']").val();
			
			if(po.isZipExtention(savePath))
				$savePath.val(savePath.substring(0, savePath.length - 4));
		}
		else
		{
			$savePath.val(po.element("input[name='fileName']").val());
		}
	});
	
	po.url = function(action)
	{
		return "${contextPath}/dashboardGlobalRes/" + action;
	};

	po.fileUploadInfo = function(){ return this.element(".upload-file-info"); };
	
	po.element(".fileinput-button").fileupload(
	{
		url : po.url("uploadFile"),
		paramName : "file",
		success : function(uploadResult, textStatus, jqXHR)
		{
			$.fileuploadsuccessHandlerForUploadInfo(po.fileUploadInfo(), false);
			po.element("input[name='filePath']").val(uploadResult.filePath);
			po.element("input[name='fileName']").val(uploadResult.fileName);
			po.element("input[name='savePath']").val(uploadResult.fileName);
		}
	})
	.bind('fileuploadadd', function (e, data)
	{
		po.element("input[name='filePath']").val("");
		po.element("input[name='fileName']").val("");
		po.element("input[name='savePath']").val("");
		po.form().validate().resetForm();
		$.fileuploadaddHandlerForUploadInfo(e, data, po.fileUploadInfo());
	})
	.bind('fileuploadprogressall', function (e, data)
	{
		$.fileuploadprogressallHandlerForUploadInfo(e, data, po.fileUploadInfo());
	});
	
	$.validator.addMethod("uploadDashboardGlobalResFileRequired", function(value, element)
	{
		var thisForm = $(element).closest("form");
		var $filePath = $("input[name='filePath']", thisForm).val();
		
		return ($filePath.length > 0);
	});
	
	po.form().validate(
	{
		ignore : ".ignore",
		rules :
		{
			filePath : "uploadDashboardGlobalResFileRequired"
		},
		messages :
		{
			filePath : "<@spring.message code='validation.required' />"
		},
		submitHandler : function(form)
		{
			var formData = $.formToJson(form);
			
			$.postJson("${contextPath}/dashboardGlobalRes/${formAction}", formData,
			function(response)
			{
				po.pageParamCallAfterSave(true, response.data);
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
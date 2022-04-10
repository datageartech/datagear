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
<#include "../include/page_obj.ftl" >
<div id="${pageId}" class="page-form page-form-dashboardGlobalRes">
	<form id="${pageId}-form" action="${contextPath}/dashboardGlobalRes/${formAction}" method="POST">
		<div class="form-head"></div>
		<div class="form-content">
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='dashboardGlobalRes.selectFile' /></label>
				</div>
				<div class="uploadFileWrapper form-item-value">
					<input type="hidden" name="filePath" value="" />
					<input type="hidden" name="fileName" value="" />
					<div class="fileinput-button button">
						<@spring.message code='select' /><input type="file" />
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
			<button type="submit" class="recommended"><@spring.message code='save' /></button>
			</#if>
		</div>
	</form>
</div>
<#include "../include/page_obj_form.ftl">
<script type="text/javascript">
(function(po)
{
	po.initFormBtns();
	po.element(".autoUnzip-radios").checkboxradiogroup();
	po.elementOfName("zipFileNameEncoding").selectmenu({ appendTo: po.element(), position: {my: "left bottom", at: "left top"}, classes: { "ui-selectmenu-menu" : "encoding-selectmenu-menu" } });
	
	po.isZipExtention = function(resName)
	{
		var reg = /\.(zip)$/gi;
		return (resName && reg.test(resName));
	};
	
	po.elementOfName("autoUnzip").on("change", function()
	{
		var val = $(this).val();
		var savePathInput = po.elementOfName("savePath");
		
		if(val == "true")
		{
			var savePath = savePathInput.val();
			
			if(po.isZipExtention(savePath))
				savePathInput.val(savePath.substring(0, savePath.length - 4));
		}
		else
		{
			savePathInput.val(po.elementOfName("fileName").val());
		}
	});
	
	po.url = function(action)
	{
		return "${contextPath}/dashboardGlobalRes/" + action;
	};
	
	po.element(".uploadFileWrapper").fileUpload(po.url("uploadFile"),
	{
		add: function(e, data)
		{
			po.elementOfName("filePath").val("");
			po.elementOfName("fileName").val("");
			po.elementOfName("savePath").val("");
		},
		success: function(response)
		{
			po.elementOfName("filePath").val(response.filePath);
			po.elementOfName("fileName").val(response.fileName);
			po.elementOfName("savePath").val(response.fileName);
		}
	});
	
	$.validator.addMethod("uploadDashboardGlobalResFileRequired", function(value, element)
	{
		var $filePath = po.elementOfName("filePath").val();
		return ($filePath.length > 0);
	});
	
	po.validateAjaxJsonForm(
	{
		ignore : ".ignore",
		rules :
		{
			filePath : "uploadDashboardGlobalResFileRequired"
		},
		messages :
		{
			filePath : po.validateMessages.required
		}
	});
})
(${pageId});
</script>
</body>
</html>
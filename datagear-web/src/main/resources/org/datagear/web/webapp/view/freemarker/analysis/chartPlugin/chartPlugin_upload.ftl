<#include "../../include/import_global.ftl">
<#include "../../include/html_doctype.ftl">
<html>
<head>
<#include "../../include/html_head.ftl">
<title><#include "../../include/html_title_app_name.ftl"><@spring.message code='chartPlugin.uploadChartPlugin' /></title>
</head>
<body>
<div id="${pageId}" class="page-form page-form-uploadChartPlugin">
	<form id="${pageId}-form" action="${contextPath}/analysis/chartPlugin/saveUpload" method="POST">
		<div class="form-head"></div>
		<div class="form-content">
			<input type="hidden" name="pluginFileName" value="" />
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='chartPlugin.upload.selectFile' /></label>
				</div>
				<div class="form-item-value">
					<div class="fileinput-button" title="<@spring.message code='chartPlugin.upload.desc' />">
						<@spring.message code='select' /><input type="file" accept=".zip" class="ignore">
					</div>
					<div class="upload-file-info"></div>
				</div>
			</div>
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='chartPlugin.upload.review' /></label>
				</div>
				<div class="form-item-value">
					<input type="hidden" name="inputForValidate" value="" />
					<div class="ui-widget ui-widget-content input chart-plugin-infos"></div>
				</div>
			</div>
		</div>
		<div class="form-foot" style="text-align:center;">
			<input type="submit" value="<@spring.message code='save' />" class="recommended" />
		</div>
	</form>
</div>
<#include "../../include/page_js_obj.ftl" >
<#include "../../include/page_obj_form.ftl">
<script type="text/javascript">
(function(po)
{
	po.element("input:submit, input:button, input:reset, button, .fileinput-button").button();

	po.url = function(action)
	{
		return "${contextPath}/analysis/chartPlugin/" + action;
	};

	po.chartPluginInfos = function(){ return this.element(".chart-plugin-infos"); };

	po.fileUploadInfo = function(){ return this.element(".upload-file-info"); };
	
	po.renderChartPluginInfos = function(uploadResult)
	{
		po.element("input[name='pluginFileName']").val(uploadResult.pluginFileName);
		
		po.chartPluginInfos().empty();
		
		var chartPluginInfos = uploadResult.pluginInfos;
		for(var i=0; i<chartPluginInfos.length; i++)
		{
			var chartPluginInfo = chartPluginInfos[i];
			
			var $item = $("<div class='ui-widget ui-widget-content ui-corner-all chart-plugin-item' />")
				.appendTo(po.chartPluginInfos());
			
			if(chartPluginInfo.hasIcon)
				$("<a class=\"plugin-icon\" style=\"background-image: url(${contextPath}/"+chartPluginInfo.iconUrl+")\">&nbsp;</a>").appendTo($item);
			
			$("<span class='name'></span>").text(chartPluginInfo.nameLabel.value).appendTo($item);
			
			if(chartPluginInfo.version)
				$("<span class='version'></span>").text("(" +chartPluginInfo.version+")").appendTo($item);
		}
	};
	
	po.element(".fileinput-button").fileupload(
	{
		url : po.url("uploadFile"),
		paramName : "file",
		success : function(uploadResult, textStatus, jqXHR)
		{
			$.fileuploadsuccessHandlerForUploadInfo(po.fileUploadInfo(), false);
			po.renderChartPluginInfos(uploadResult);
		}
	})
	.bind('fileuploadadd', function (e, data)
	{
		po.element("input[name='pluginFileName']").val("");
		po.form().validate().resetForm();
		$.fileuploadaddHandlerForUploadInfo(e, data, po.fileUploadInfo());
	})
	.bind('fileuploadprogressall', function (e, data)
	{
		$.fileuploadprogressallHandlerForUploadInfo(e, data, po.fileUploadInfo());
	});
	
	$.validator.addMethod("uploadChartPluginFileRequired", function(value, element)
	{
		var thisForm = $(element).closest("form");
		var $pluginFileName = $("input[name='pluginFileName']", thisForm).val();
		
		return $pluginFileName.length > 0;
	});
	
	po.form().validate(
	{
		ignore : ".ignore",
		rules :
		{
			inputForValidate : "uploadChartPluginFileRequired"
		},
		messages :
		{
			inputForValidate : "<@spring.message code='chartPlugin.upload.validation.uploadChartPluginFileRequired' />"
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
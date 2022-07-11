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
<title><#include "../include/html_title_app_name.ftl"><@spring.message code='chartPlugin.uploadChartPlugin' /></title>
</head>
<body>
<#include "../include/page_obj.ftl" >
<div id="${pageId}" class="page-form page-form-uploadChartPlugin">
	<form id="${pageId}-form" action="${contextPath}/chartPlugin/saveUpload" method="POST">
		<div class="form-head"></div>
		<div class="form-content">
			<input type="hidden" name="pluginFileName" value="" />
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='chartPlugin.upload.selectFile' /></label>
				</div>
				<div class="fileUploadWrapper form-item-value">
					<div class="fileinput-button button" title="<@spring.message code='chartPlugin.upload.desc' />">
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
					<div class="chart-plugin-infos minor-list deletable-list input ui-widget ui-widget-content ui-corner-all"></div>
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
	
	po.url = function(action)
	{
		return "${contextPath}/chartPlugin/" + action;
	};

	po.chartPluginInfos = function(){ return this.element(".chart-plugin-infos"); };

	po.renderChartPluginInfos = function(uploadResult)
	{
		po.elementOfName("pluginFileName").val(uploadResult.pluginFileName);
		
		po.chartPluginInfos().empty();
		
		var chartPluginInfos = uploadResult.pluginInfos;
		for(var i=0; i<chartPluginInfos.length; i++)
		{
			var chartPluginInfo = chartPluginInfos[i];
			
			var $item = $("<div class='chart-plugin-item minor-list-item ui-widget ui-widget-content ui-corner-all' />")
				.appendTo(po.chartPluginInfos());
			
			if(chartPluginInfo.iconUrl)
				$("<span class='plugin-icon' style='background-image: url(${contextPath}"+chartPluginInfo.iconUrl+")'></span>").appendTo($item);
			
			var $content = $("<div class='item-content' />").appendTo($item);
			
			$("<span class='name'></span>").text(chartPluginInfo.nameLabel.value).appendTo($content);
			
			if(chartPluginInfo.version)
				$("<span class='version'></span>").text("(" +chartPluginInfo.version+")").appendTo($content);
		}
	};
	
	po.element(".fileUploadWrapper").fileUpload(po.url("uploadFile"),
	{
		add: function(e, data)
		{
			po.elementOfName("pluginFileName").val("");
		},
		success: function(response)
		{
			po.renderChartPluginInfos(response);
		}
	});
	
	$.validator.addMethod("uploadChartPluginFileRequired", function(value, element)
	{
		var $pluginFileName = po.elementOfName("pluginFileName").val();
		return $pluginFileName.length > 0;
	});
	
	po.validateAjaxJsonForm(
	{
		ignore : ".ignore",
		rules :
		{
			inputForValidate : "uploadChartPluginFileRequired"
		},
		messages :
		{
			inputForValidate : "<@spring.message code='chartPlugin.upload.validation.uploadChartPluginFileRequired' />"
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
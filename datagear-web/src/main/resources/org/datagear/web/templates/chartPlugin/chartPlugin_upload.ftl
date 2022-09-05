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
<title>
	<#include "../include/html_app_name_prefix.ftl">
	<@spring.message code='module.chartPlugin' />
	<#include "../include/html_request_action_suffix.ftl">
</title>
</head>
<body class="p-card no-border">
<#include "../include/page_obj.ftl">
<div id="${pid}" class="page page-form horizontal">
	<form id="${pid}form" class="flex flex-column" :class="{readonly: pm.isReadonlyAction}">
		<div class="page-form-content flex-grow-1 px-2 py-1 overflow-y-auto">
			<div class="field grid">
				<label for="${pid}pluginFile" class="field-label col-12 mb-2 md:col-3 md:mb-0"
					title="<@spring.message code='chartPlugin.pluginFile.desc' />">
					<@spring.message code='pluginFile' />
				</label>
		        <div class="field-input col-12 md:col-9">
		        	<div id="${pid}pluginFile" class="fileupload-wrapper flex align-items-center mt-1" v-if="!pm.isReadonlyAction">
			        	<p-fileupload mode="basic" name="file" :url="pm.uploadFileUrl"
			        		@upload="onUploaded" @select="uploadFileOnSelect" @progress="uploadFileOnProgress"
			        		:auto="true" choose-label="<@spring.message code='select' />" class="mr-2">
			        	</p-fileupload>
						<#include "../include/page_fileupload.ftl">
		        	</div>
		        	<div class="validate-msg">
		        		<input name="pluginFileName" required type="text" class="validate-proxy" />
		        	</div>
		        </div>
			</div>
			<div class="field grid">
				<label for="${pid}preview" class="field-label col-12 mb-2 md:col-3 md:mb-0">
					<@spring.message code='preview' />
				</label>
		        <div class="field-input col-12 md:col-9">
		        	<div id="${pid}preview" class="input p-component p-inputtext w-full overflow-auto" style="height:8rem;">
		        		<p-chip v-for="p in pm.chartPlugins.plugins" :key="p.key" class="mb-2">
		        			<div v-html="formatChartPlugin(p)"></div>
		        		</p-chip>
		        	</div>
		        </div>
			</div>
		</div>
		<div class="page-form-foot flex-grow-0 pt-3 text-center">
			<p-button type="submit" label="<@spring.message code='save' />"></p-button>
		</div>
	</form>
</div>
<#include "../include/page_form.ftl">
<script>
(function(po)
{
	po.submitUrl = "/chartPlugin/"+po.submitAction;

	po.chartPluginKeySeq = 0;
	
	po.setChartPlugins = function(cps)
	{
		var pm = po.vuePageModel();
		var fm = po.vueFormModel();
		
		if(!cps)
			cps = po.vueRaw(pm.chartPlugins.plugins);
		
		var seq = po.chartPluginKeySeq++;
		$.each(cps, function(idx, cp)
		{
			cp.key = cp.id + seq;
			
			if(cp.iconUrl)
				cp.iconUrl = $.addParam(cp.iconUrl, "tmpPluginFileName", fm.pluginFileName);
		});
		
		pm.chartPlugins.plugins = cps;
	};
	
	po.setupForm({ pluginFileName: "" });
	
	po.vuePageModel(
	{
		chartPlugins: { plugins: [] },
		uploadFileUrl: po.concatContextPath("/chartPlugin/uploadFile")
	});
	
	po.vueMethod(
	{
		formatChartPlugin: function(chartPlugin)
		{
			return $.toChartPluginHtml(chartPlugin, po.contextPath);
		},
		
		onUploaded: function(e)
		{
			var fm = po.vueFormModel();
			var response = $.getResponseJson(e.xhr);
			
			po.uploadFileOnUploaded(e);
			fm.pluginFileName = response.pluginFileName;
			po.setChartPlugins(response.pluginInfos);
		}
	});
	
	po.vueMount();
})
(${pid});
</script>
</body>
</html>
<#--
 *
 * Copyright 2018-2024 datagear.tech
 *
 * This file is part of DataGear.
 *
 * DataGear is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * DataGear is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with DataGear.
 * If not, see <https://www.gnu.org/licenses/>.
 *
-->
<#include "../include/page_import.ftl">
<#include "../include/html_doctype.ftl">
<html>
<head>
<#include "../include/html_head.ftl">
<title>
	<@spring.message code='module.dashboardGlobalRes' />
	<#include "../include/html_request_action_suffix.ftl">
	<#include "../include/html_app_name_suffix.ftl">
</title>
</head>
<body class="p-card no-border">
<#include "../include/page_obj.ftl">
<div id="${pid}" class="page page-form horizontal">
	<form id="${pid}form" class="flex flex-column" :class="{readonly: pm.isReadonlyAction}">
		<div class="page-form-content flex-grow-1 px-2 py-1 overflow-y-auto">
			<div class="field grid">
				<label for="${pid}file" class="field-label col-12 mb-2 md:col-3 md:mb-0">
					<@spring.message code='file' />
				</label>
		        <div class="field-input col-12 md:col-9">
		        	<div id="${pid}file" class="fileupload-wrapper flex align-items-center mt-1" v-if="!pm.isReadonlyAction">
			        	<p-fileupload mode="basic" name="file" :url="pm.uploadFileUrl"
			        		@upload="onUploaded" @select="uploadFileOnSelect" @progress="uploadFileOnProgress" @error="uploadFileOnError"
			        		:auto="true" choose-label="<@spring.message code='select' />" class="mr-2">
			        	</p-fileupload>
						<#include "../include/page_fileupload.ftl">
		        	</div>
		        	<div class="validate-msg">
		        		<input name="filePath" required type="text" class="validate-proxy" />
		        	</div>
		        </div>
			</div>
			<div class="field grid">
				<label for="${pid}savePath" class="field-label col-12 mb-2 md:col-3 md:mb-0"
					title="<@spring.message code='dashboardGlobalRes.upload.savePath.desc' />">
					<@spring.message code='savePath' />
				</label>
		        <div class="field-input col-12 md:col-9">
		        	<p-inputtext id="${pid}savePath" v-model="fm.savePath" type="text" class="input w-full validate-normalizer"
		        		name="savePath" required maxlength="200">
		        	</p-inputtext>
		        </div>
			</div>
			<div class="field grid">
				<label for="${pid}autoUnzip" class="field-label col-12 mb-2 md:col-3 md:mb-0"
					title="<@spring.message code='dashboardGlobalRes.upload.autoUnzip.desc' />">
					<@spring.message code='autoUnzip' />
				</label>
		        <div class="field-input col-12 md:col-9">
		        	<p-selectbutton id="${pid}autoUnzip" v-model="fm.autoUnzip" :options="pm.booleanOptions"
		        		option-label="name" option-value="value" class="input w-full">
		        	</p-selectbutton>
		        </div>
			</div>
			<div class="field grid">
				<label for="${pid}zipFileNameEncoding" class="field-label col-12 mb-2 md:col-3 md:mb-0"
					title="<@spring.message code='zipFileEncoding.uploadAutoUzip.desc' />">
					<@spring.message code='zipFileEncoding' />
				</label>
		        <div class="field-input col-12 md:col-9">
		        	<p-dropdown id="${pid}zipFileNameEncoding" v-model="fm.zipFileNameEncoding"
		        		:options="pm.availableCharsetNames" class="input w-full">
		        	</p-dropdown>
		        	<div class="desc text-color-secondary">
		        		<small><@spring.message code='zipFileEncoding.uploadAutoUzip.notice' /></small>
		        	</div>
		        </div>
			</div>
		</div>
		<div class="page-form-foot flex-grow-0 flex justify-content-center gap-2 pt-2">
			<p-button type="submit" label="<@spring.message code='save' />"></p-button>
		</div>
	</form>
</div>
<#include "../include/page_form.ftl">
<#include "../include/page_boolean_options.ftl">
<script>
(function(po)
{
	po.submitUrl = "/dashboardGlobalRes/"+po.submitAction;
	
	var availableCharsetNames = $.unescapeHtmlForJson(<@writeJson var=availableCharsetNames />);
	
	po.setupForm(
	{
		fileName: "",
		filePath: "",
		savePath: "",
		autoUnzip: false,
		zipFileNameEncoding: "${zipFileNameEncodingDefault}"
	},
	{},
	{
		customNormalizers:
		{
			savePath: function()
			{
				var fm = po.vueFormModel();
				
				if(fm.autoUnzip && $.isZipFile(fm.fileName))
					return (fm.savePath || "savePathValidatePlaceholder");
				else
					return fm.savePath;
			}
		}
	});
	
	po.vuePageModel(
	{
		availableCharsetNames: availableCharsetNames,
		uploadFileUrl: po.concatContextPath("/dashboardGlobalRes/uploadFile")
	});
	
	po.vueMethod(
	{
		onUploaded: function(e)
		{
			var fm = po.vueFormModel();
			var response = $.getResponseJson(e.xhr);
			
			po.uploadFileOnUploaded(e);
			fm.fileName = response.fileName;
			fm.savePath = response.fileName;
			fm.filePath = response.filePath;
		}
	});
})
(${pid});
</script>
<#include "../include/page_vue_mount.ftl">
</body>
</html>
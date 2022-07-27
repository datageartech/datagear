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
	<@spring.message code='module.dashboardGlobalRes' />
	<#include "../include/html_request_action_suffix.ftl">
</title>
</head>
<body class="p-card no-border">
<#include "../include/page_obj.ftl">
<div id="${pid}" class="page page-form horizontal">
	<form class="flex flex-column" :class="{readonly: isReadonlyAction}">
		<div class="page-form-content flex-grow-1 pr-2 py-1 overflow-y-auto">
			<div class="field grid">
				<label for="${pid}file" class="field-label col-12 mb-2 md:col-3 md:mb-0">
					<@spring.message code='file' />
				</label>
		        <div class="field-input col-12 md:col-9">
		        	<div id="${pid}file" class="fileupload-wrapper mt-1" v-if="!isReadonlyAction">
			        	<p-fileupload mode="basic" name="file" :url="uploadFileUrl"
			        		@upload="onUploaded" @select="uploadFileOnSelect" @progress="uploadFileOnProgress"
			        		:auto="true" choose-label="<@spring.message code='select' />" class="p-button-secondary">
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
		        	<p-inputtext id="${pid}savePath" v-model="pm.savePath" type="text" class="input w-full"
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
		        	<p-selectbutton id="${pid}autoUnzip" v-model="pm.autoUnzip" :options="booleanOptions"
		        		option-label="name" option-value="value" class="input w-full">
		        	</p-selectbutton>
		        </div>
			</div>
			<div class="field grid">
				<label for="${pid}zipFileNameEncoding" class="field-label col-12 mb-2 md:col-3 md:mb-0"
					title="<@spring.message code='dashboardGlobalRes.upload.zipFileNameEncoding.desc' />">
					<@spring.message code='zipFileEncoding' />
				</label>
		        <div class="field-input col-12 md:col-9">
		        	<p-dropdown id="${pid}zipFileNameEncoding" v-model="pm.zipFileNameEncoding"
		        		:options="availableCharsetNames" class="input w-full">
		        	</p-dropdown>
		        	<div class="desc text-color-secondary">
		        		<small><@spring.message code='dashboardGlobalRes.upload.notice' /></small>
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
<#include "../include/page_boolean_options.ftl">
<script>
(function(po)
{
	po.submitUrl = "/dashboardGlobalRes/"+po.submitAction;
	
	var availableCharsetNames = <@writeJson var=availableCharsetNames />;
	availableCharsetNames = $.unescapeHtmlForJson(availableCharsetNames);
	po.vueRef("availableCharsetNames", availableCharsetNames);
	
	po.setupForm(
	{
		fileName: "",
		filePath: "",
		savePath: "",
		autoUnzip: false,
		zipFileNameEncoding: "${zipFileNameEncodingDefault}"
	});
	
	po.vueRef("uploadFileUrl", po.concatContextPath("/dashboardGlobalRes/uploadFile"));
	
	po.vueMethod(
	{
		onUploaded: function(e)
		{
			var pm = po.vuePageModel();
			var response = $.getResponseJson(e.xhr);
			
			po.uploadFileOnUploaded(e);
			pm.fileName = response.fileName;
			pm.savePath = response.fileName;
			pm.filePath = response.filePath;
		}
	});
	
	po.vueMount();
})
(${pid});
</script>
</body>
</html>
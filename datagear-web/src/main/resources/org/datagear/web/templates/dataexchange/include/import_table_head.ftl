<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#--
导入表格页头片段

依赖：
dataexchange_js.ftl
-->
<div class="flex align-items-center justify-content-between">
	<div class="fileupload-wrapper flex align-items-center">
		<label title="<@spring.message code='dataImport.uploadFile.desc' />" class="mr-3">
			<@spring.message code='uploadFile' />
		</label>
		<p-fileupload mode="basic" name="file" :url="pm.uploadFileUrl"
       		@upload="onUploaded" @select="uploadFileOnSelect" @before-upload="onBeforeUpload" @progress="uploadFileOnProgress"
       		:auto="true" choose-label="<@spring.message code='select' />" class="mr-2"
       		:disabled="pm.dataExchangeStatus != pm.DataExchangeStatusEnum.edit">
       	</p-fileupload>
		<#include "../../include/page_fileupload.ftl">
	</div>
	<div class="flex align-items-center w-3">
		<div class="w-full"
			v-if="pm.dataExchangeStatus != pm.DataExchangeStatusEnum.edit">
			<p-progressbar :value="pm.dataExchangeProgress.value">
				{{pm.dataExchangeProgress.label}}
			</p-progressbar>
		</div>
	</div>
	<div class="h-opts">
		<p-button type="button" label="<@spring.message code='delete' />"
			@click="onDeleteSelSubDataExchanges"
			class="p-button-danger"
			:disabled="pm.dataExchangeStatus != pm.DataExchangeStatusEnum.edit">
		</p-button>
		<p-button type="button" label="<@spring.message code='set' />"
			:disabled="pm.dataExchangeStatus != pm.DataExchangeStatusEnum.edit">
		</p-button>
	</div>
</div>
<script>
(function(po)
{
	po.inflateUploadParam = function(formData)
	{
		var fm = po.vueFormModel();
		
		formData.append("dataExchangeId", fm.dataExchangeId);
		if(fm.zipFileNameEncoding)
			formData.append("zipFileNameEncoding", fm.zipFileNameEncoding);
	};
	
	po.setupImportTableHead = function(uploadUrl, uploadedHandler)
	{
		po.dataExchangeStatusChanged = function(status, oldStatus)
		{
			po.clearFileuploadInfo();
		};
		
		po.vuePageModel(
		{
			uploadFileUrl: uploadUrl
		});
		
		po.vueMethod(
		{
			onBeforeUpload: function(e)
			{
				po.inflateUploadParam(e.formData);
			},
			onUploaded: function(e)
			{
				po.uploadFileOnUploaded(e);
				
				var response = $.getResponseJson(e.xhr);
				uploadedHandler(response);
			}
		});
	};
})
(${pid});
</script>
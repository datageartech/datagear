<#--
 *
 * Copyright 2018-2023 datagear.tech
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
<#--
导入表格页头片段

依赖：
dataexchange_js.ftl
import_js.ftl
-->
<div class="flex-grow-0 flex align-items-center justify-content-between">
	<div class="fileupload-wrapper flex align-items-center">
		<div class="mr-3">
			<label :title="pm.tableHeadOptions.uploadFileLabelDesc">
				<@spring.message code='uploadFile' />
			</label>
		</div>
		<p-fileupload mode="basic" name="file" :url="pm.uploadFileUrl"
       		@upload="onUploaded" @select="uploadFileOnSelect" @before-upload="onBeforeUpload" @progress="uploadFileOnProgress" @error="uploadFileOnError"
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
	<div class="flex gap-1">
		<p-button type="button" label="<@spring.message code='delete' />"
			@click="onDeleteSelSubDataExchanges"
			class="p-button-danger"
			v-if="pm.dataExchangeStatus == pm.DataExchangeStatusEnum.edit">
		</p-button>
		<p-button type="button" label="<@spring.message code='fileEncoding' />"
			aria:haspopup="true" aria-controls="${pid}fileEncodingPanel"
			@click="onToggleFileEncodingPanel" class="p-button-secondary"
			v-if="pm.dataExchangeStatus == pm.DataExchangeStatusEnum.edit">
		</p-button>
		<p-button type="button" label="<@spring.message code='cancel' />"
			title="<@spring.message code='dataImport.cancel.desc' />"
			@click="onCancelSubDataExchanges"
			v-if="pm.dataExchangeStatus == pm.DataExchangeStatusEnum.exchange">
		</p-button>
	</div>
	<p-overlaypanel ref="${pid}fileEncodingPanelEle" append-to="body"
		:show-close-icon="false" id="${pid}fileEncodingPanel">
		<div class="pb-2">
			<label class="text-lg font-bold">
				<@spring.message code='fileEncoding' />
			</label>
		</div>
		<div class="p-2 panel-content-size-xxs overflow-auto">
			<div class="field grid" v-if="pm.tableHeadOptions.fileEncodingEnable">
				<label for="${pid}fileEncoding" class="field-label col-12 mb-2"
					title="<@spring.message code='dataImport.fileEncoding.desc' />">
					<@spring.message code='fileEncoding' />
				</label>
		        <div class="field-input col-12">
		        	<p-dropdown id="${pid}fileEncoding" v-model="fm.fileEncoding"
		        		:options="pm.availableCharsetNames" class="input w-full">
		        	</p-dropdown>
		        </div>
			</div>
			<div class="field grid">
				<label for="${pid}zipFileNameEncoding" class="field-label col-12 mb-2"
					title="<@spring.message code='dataImport.zipFileEncoding.desc' />">
					<@spring.message code='zipFileEncoding' />
				</label>
		        <div class="field-input col-12">
		        	<p-dropdown id="${pid}zipFileNameEncoding" v-model="fm.zipFileNameEncoding"
		        		:options="pm.availableCharsetNames" class="input w-full">
		        	</p-dropdown>
		        </div>
			</div>
		</div>
	</p-overlaypanel>
</div>
<script>
(function(po)
{
	po.availableCharsetNames = $.unescapeHtmlForJson(<@writeJson var=availableCharsetNames />);
	
	po.inflateUploadParam = function(formData)
	{
		var fm = po.vueFormModel();
		
		formData.append("dataExchangeId", fm.dataExchangeId);
		if(fm.zipFileNameEncoding)
			formData.append("zipFileNameEncoding", fm.zipFileNameEncoding);
	};
	
	po.setupImportTableHead = function(uploadUrl, uploadedHandler, options)
	{
		options = $.extend(
		{
			uploadFileLabelDesc: "<@spring.message code='dataImport.uploadFile.desc' />",
			fileEncodingEnable: true
		},
		options);
		
		po.dataExchangeStatusChanged = function(status, oldStatus)
		{
			po.clearFileuploadInfo();
		};
		
		po.vuePageModel(
		{
			uploadFileUrl: uploadUrl,
			availableCharsetNames: po.availableCharsetNames,
			tableHeadOptions: options
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
			},
			
			onToggleFileEncodingPanel: function(e)
			{
				po.vueUnref("${pid}fileEncodingPanelEle").toggle(e);
			}
		});
		
		po.vueRef("${pid}fileEncodingPanelEle", null);
	};
})
(${pid});
</script>
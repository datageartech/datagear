<#--
 *
 * Copyright 2018-present datagear.tech
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
数据集文件源输入项

依赖：

-->
<#assign DirectoryFileDataSetEntity=statics['org.datagear.management.domain.DirectoryFileDataSetEntity']>
<div class="field grid">
	<label for="${pid}fileSourceType" class="field-label col-12 mb-2 md:col-3 md:mb-0">
		<@spring.message code='fileSource' />
	</label>
	<div class="field-input col-12 md:col-9">
      	<p-selectbutton v-model="fm.fileSourceType" :options="pm.fileSourceTypeOptions"
       		option-label="name" option-value="value" @change="onFileSourceTypeChange" class="input w-full">
       	</p-selectbutton>
	</div>
</div>
<div class="field grid" v-if="fm.fileSourceType == '${DirectoryFileDataSetEntity.FILE_SOURCE_TYPE_UPLOAD}'">
	<label for="${pid}displayName" class="field-label col-12 mb-2 md:col-3 md:mb-0">
		<@spring.message code='uploadFile' />
	</label>
	<div class="field-input col-12 md:col-9">
		<div class="p-inputgroup">
			<p-inputtext id="${pid}displayName" v-model="fm.displayName" type="text" class="input"
				name="displayName" required readonly>
			</p-inputtext>
			<a :href="evalDownloadFileUrl()" target="_blank" class="link p-inputgroup-addon px-3" v-if="pm.originalFileName">
				<@spring.message code='download' />
			</a>
		</div>
       	<div id="${pid}fileName" class="fileupload-wrapper flex align-items-center mt-1" v-if="!pm.isReadonlyAction">
        	<p-fileupload mode="basic" name="file" :url="pm.uploadFileUrl"
        		@upload="onUploaded" @select="uploadFileOnSelect" @progress="uploadFileOnProgress" @error="uploadFileOnError"
        		:auto="true" choose-label="<@spring.message code='select' />" class="mr-2">
			</p-fileupload>
			<#include "../../include/page_fileupload.ftl">
		</div>
	</div>
</div>
<div class="field grid" v-if="fm.fileSourceType == '${DirectoryFileDataSetEntity.FILE_SOURCE_TYPE_SERVER}'">
	<label for="${pid}fileSource" class="field-label col-12 mb-2 md:col-3 md:mb-0"
		title="<@spring.message code='dataSet.directoryOnServer.desc' />">
		<@spring.message code='directoryOnServer' />
	</label>
       <div class="field-input col-12 md:col-9">
       	<div class="p-inputgroup">
        	<div class="p-input-icon-right flex-grow-1">
				<i class="pi pi-times cursor-pointer opacity-60" @click="onDeleteFileSource" v-if="!pm.isReadonlyAction">
				</i>
				<p-inputtext id="${pid}fileSource" v-model="fm.fileSource.name" type="text" class="input w-full h-full border-noround-right"
					name="fileSource.name" required readonly>
				</p-inputtext>
			</div>
        	<p-button type="button" label="<@spring.message code='select' />" @click="onSelectFileSource"
        		v-if="!pm.isReadonlyAction">
        	</p-button>
		</div>
	</div>
</div>
<div class="field grid" v-if="fm.fileSourceType == '${DirectoryFileDataSetEntity.FILE_SOURCE_TYPE_SERVER}'">
	<label for="${pid}dataSetResFileName" class="field-label col-12 mb-2 md:col-3 md:mb-0"
		title="<@spring.message code='dataSet.fileInDirectory.desc' />">
		<@spring.message code='fileInDirectory' />
	</label>
       <div class="field-input col-12 md:col-9">
       	<div class="p-inputgroup">
        	<p-inputtext id="${pid}dataSetResFileName" v-model="fm.dataSetResFileName" type="text" class="input"
        		name="dataSetResFileName" required  maxlength="1000">
        	</p-inputtext>
        	<p-button type="button" label="<@spring.message code='select' />" @click="onSelectFileSourceFile" v-if="!pm.isReadonlyAction">
        	</p-button>
		</div>
	</div>
</div>
<script>
(function(po)
{
	po.vuePageModel(
	{
		fileSourceTypeOptions:
		[
			{name: "<@spring.message code='dataSet.FILE_SOURCE_TYPE_UPLOAD' />", value: "${DirectoryFileDataSetEntity.FILE_SOURCE_TYPE_UPLOAD}"},
			{name: "<@spring.message code='dataSet.FILE_SOURCE_TYPE_SERVER' />", value: "${DirectoryFileDataSetEntity.FILE_SOURCE_TYPE_SERVER}"}
		],
		uploadFileUrl: po.concatContextPath("/dataSet/uploadFile")
	});
	
	po.vueMethod(
	{
		evalDownloadFileUrl: function()
		{
			var fm = po.vueFormModel();
			return po.concatContextPath("/dataSet/downloadFile/?id="+encodeURIComponent(fm.id));
		},
		
		onFileSourceTypeChange: function()
		{
			
		},
		
		onUploaded: function(e)
		{
			var fm = po.vueFormModel();
			var response = $.getResponseJson(e.xhr);
			
			po.uploadFileOnUploaded(e);
			fm.fileName = response.fileName;
			fm.displayName = response.displayName;
		},
		
		onSelectFileSource: function(e)
		{
			po.handleOpenSelectAction("/fileSource/select", function(fs)
			{
				var fm = po.vueFormModel();
				fm.fileSource = fs;
			});
		},
		
		onDeleteFileSource: function(e)
		{
			var fm = po.vueFormModel();
			fm.fileSource = {};
		},
		
		onSelectFileSourceFile: function(e)
		{
			var fm = po.vueFormModel();
			
			if(!fm.fileSource || !fm.fileSource.id)
				return;
			
			po.handleOpenSelectAction("/fileSource/file/select?id=" + encodeURIComponent(fm.fileSource.id), function(fileInfo)
			{
				if(fileInfo.directory)
				{
					$.tipInfo("<@spring.message code='pleaseSelectOneFile' />");
					return false;
				}
				
				fm.dataSetResFileName = fileInfo.path;
			});
		}
	});
})
(${pid});
</script>

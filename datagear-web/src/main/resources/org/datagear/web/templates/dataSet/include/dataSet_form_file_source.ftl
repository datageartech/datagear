<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#--
数据集文件源输入项

依赖：

-->
<#assign DirectoryFileDataSetEntity=statics['org.datagear.management.domain.DirectoryFileDataSetEntity']>
<div class="field grid">
	<label for="${pid}fileSourceType" class="field-label col-12 mb-2 md:col-3 md:mb-0">
		<@spring.message code='fileType' />
	</label>
	<div class="field-input col-12 md:col-9">
      	<p-selectbutton v-model="pm.fileSourceType" :options="tm.fileSourceTypeOptions"
       		option-label="name" option-value="value" @change="onFileSourceTypeChange" class="input w-full">
       	</p-selectbutton>
	</div>
</div>
<div class="field grid" v-if="pm.fileSourceType == '${DirectoryFileDataSetEntity.FILE_SOURCE_TYPE_UPLOAD}'">
	<label for="${pid}displayName" class="field-label col-12 mb-2 md:col-3 md:mb-0">
		<@spring.message code='uploadFile' />
	</label>
	<div class="field-input col-12 md:col-9">
		<p-inputtext id="${pid}displayName" v-model="pm.displayName" type="text" class="input w-full"
			name="displayName" required readonly>
		</p-inputtext>
       	<div id="${pid}fileName" class="fileupload-wrapper flex align-items-center mt-1" v-if="!isReadonlyAction">
        	<p-fileupload mode="basic" name="file" :url="tm.uploadFileUrl"
        		@upload="onUploaded" @select="uploadFileOnSelect" @progress="uploadFileOnProgress"
        		:auto="true" choose-label="<@spring.message code='select' />" class="p-button-secondary mr-2">
			</p-fileupload>
			<#include "../../include/page_fileupload.ftl">
		</div>
	</div>
</div>
<div class="field grid" v-if="pm.fileSourceType == '${DirectoryFileDataSetEntity.FILE_SOURCE_TYPE_SERVER}'">
	<label for="${pid}dataSetResDirectory" class="field-label col-12 mb-2 md:col-3 md:mb-0"
		title="<@spring.message code='dataSet.directoryOnServer.desc' />">
		<@spring.message code='directoryOnServer' />
	</label>
       <div class="field-input col-12 md:col-9">
       	<div class="p-inputgroup">
        	<p-inputtext id="${pid}dataSetResDirectory" v-model="pm.dataSetResDirectory.directory" type="text" class="input"
        		name="dataSetResDirectory.directory" required readonly>
        	</p-inputtext>
        	<p-button type="button" label="<@spring.message code='select' />" @click="onSelectDataSetResDirectory"
        		class="p-button-secondary" v-if="!isReadonlyAction">
        	</p-button>
		</div>
	</div>
</div>
<div class="field grid" v-if="pm.fileSourceType == '${DirectoryFileDataSetEntity.FILE_SOURCE_TYPE_SERVER}'">
	<label for="${pid}dataSetResFileName" class="field-label col-12 mb-2 md:col-3 md:mb-0"
		title="<@spring.message code='dataSet.fileInDirectory.desc' />">
		<@spring.message code='fileInDirectory' />
	</label>
       <div class="field-input col-12 md:col-9">
       	<div class="p-inputgroup">
        	<p-inputtext id="${pid}dataSetResFileName" v-model="pm.dataSetResFileName" type="text" class="input"
        		name="dataSetResFileName" required  maxlength="1000">
        	</p-inputtext>
        	<p-button type="button" label="<@spring.message code='select' />" @click="onSelectDataSetResFileName"
        		class="p-button-secondary" v-if="!isReadonlyAction">
        	</p-button>
		</div>
	</div>
</div>
<script>
(function(po)
{
	po.vueTmpModel(
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
		onFileSourceTypeChange: function()
		{
			
		},
		
		onUploaded: function(e)
		{
			var pm = po.vuePageModel();
			var response = $.getResponseJson(e.xhr);
			
			po.uploadFileOnUploaded(e);
			pm.fileName = response.fileName;
			pm.displayName = response.displayName;
		},
		
		onSelectDataSetResDirectory: function(e)
		{
			po.handleOpenSelectAction("/dataSetResDirectory/select", function(dsrd)
			{
				var pm = po.vuePageModel();
				pm.dataSetResDirectory = dsrd;
			});
		},
		
		onSelectDataSetResFileName: function(e)
		{
			
		}
	});
})
(${pid});
</script>

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
        	<div class="p-input-icon-right flex-grow-1">
				<i class="pi pi-times cursor-pointer opacity-60" @click="onDeleteDataSetResDirectory" v-if="!isReadonlyAction">
				</i>
				<p-inputtext id="${pid}dataSetResDirectory" v-model="pm.dataSetResDirectory.directory" type="text" class="input w-full h-full border-noround-right"
					name="dataSetResDirectory.directory" required readonly>
				</p-inputtext>
			</div>
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
        		aria:haspopup="true" aria-controls="${pid}dsrFilesPanel"
        		class="p-button-secondary" v-if="!isReadonlyAction">
        	</p-button>
			<p-overlaypanel ref="dsrFilesPanelEle" append-to="body"
				:show-close-icon="true" @show="onDsrFilesPanelShow" id="${pid}dsrFilesPanel" class="dsr-files-panel">
				<div class="mb-2">
					<label class="text-lg font-bold">
						<@spring.message code='selectFile' />
					</label>
				</div>
				<div class="panel-content-size-sm overflow-auto">
					<p-tree :value="tm.dsrFileNodes"
						selection-mode="single" v-model:selection-keys="tm.dsrSelectedNodeKeys"
						@node-expand="onDsrFileNodeExpand" @node-select="onDsrFileNodeSelect"
						:loading="tm.dsrLoading" class="h-full overflow-auto">
					</p-tree>
				</div>
				<div class="pt-3 text-center">
					<p-button type="button" @click="onConfirmDataSetResFileName" label="<@spring.message code='confirm' />"></p-button>
				</div>
			</p-overlaypanel>
		</div>
	</div>
</div>
<script>
(function(po)
{
	po.loadDsrFiles = function(node)
	{
		var subPath = (node && node.path ? node.path : "");
		
		var pm = po.vuePageModel();
		var tm = po.vueTmpModel();
		
		tm.dsrLoading = true;
		po.ajax("/dataSetResDirectory/listFiles",
		{
			data: { id: pm.dataSetResDirectory.id, subPath: subPath },
			success: function(response)
			{
				var nodes = po.dsrFileInfosToNodes(subPath, response);
				
				if(node)
					node.children = nodes;
				else
					tm.dsrFileNodes = nodes;
				
				tm.dsrSelectedNodeKeys = null;
				tm.dsrSelectedNode = null;
			},
			complete: function()
			{
				tm.dsrLoading = false;
			}
		});
	};
	
	po.dsrFileInfosToNodes = function(subPath, fileInfos)
	{
		var re = [];
		
		$.each(fileInfos, function(idx, fi)
		{
			var myPath = (subPath ? subPath + "/" : "") + fi.name;
			
			re.push(
			{
				key: myPath,
				label: fi.name + (fi.directory ? "" : " ("+fi.size+")"),
				icon: (fi.directory ? "pi pi-folder" : "pi pi-file"),
				leaf: (fi.directory ? false : true),
				children: null,
				fileInfo: fi,
				path: myPath
			});
		});
		
		return re;
	};
	
	po.vueTmpModel(
	{
		fileSourceTypeOptions:
		[
			{name: "<@spring.message code='dataSet.FILE_SOURCE_TYPE_UPLOAD' />", value: "${DirectoryFileDataSetEntity.FILE_SOURCE_TYPE_UPLOAD}"},
			{name: "<@spring.message code='dataSet.FILE_SOURCE_TYPE_SERVER' />", value: "${DirectoryFileDataSetEntity.FILE_SOURCE_TYPE_SERVER}"}
		],
		uploadFileUrl: po.concatContextPath("/dataSet/uploadFile"),
		dsrFileNodes: null,
		dsrSelectedNodeKeys: null,
		dsrSelectedNode: null,
		dsrLoading: false
	});
	
	po.vueRef("dsrFilesPanelEle", null);
	
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
		
		onDeleteDataSetResDirectory: function(e)
		{
			var pm = po.vuePageModel();
			pm.dataSetResDirectory = {};
		},
		
		onSelectDataSetResFileName: function(e)
		{
			var pm = po.vuePageModel();
			
			if(!pm.dataSetResDirectory || !pm.dataSetResDirectory.id)
				return;
			
			po.vueUnref("dsrFilesPanelEle").show(e);
		},
		
		onDsrFilesPanelShow: function(e)
		{
			po.loadDsrFiles();
		},
		
		onDsrFileNodeExpand: function(node)
		{
			if(node.children == null)
				po.loadDsrFiles(node);
		},
		
		onDsrFileNodeSelect: function(node)
		{
			var tm = po.vueTmpModel();
			tm.dsrSelectedNode = node;
		},
		
		onConfirmDataSetResFileName: function(e)
		{
			var pm = po.vuePageModel();
			var tm = po.vueTmpModel();
			
			if(!tm.dsrSelectedNode || tm.dsrSelectedNode.fileInfo.directory)
			{
				$.tipInfo("<@spring.message code='pleaseSelectOneFile' />");
				return;
			}
			
			pm.dataSetResFileName = tm.dsrSelectedNode.path;
			po.vueUnref("dsrFilesPanelEle").hide();
		}
	});
})
(${pid});
</script>

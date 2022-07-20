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
	<@spring.message code='module.driverEntity' />
	<#include "../include/html_request_action_suffix.ftl">
</title>
</head>
<body class="p-card no-border">
<#include "../include/page_obj.ftl">
<div id="${pid}" class="page page-form horizontal">
	<form class="flex flex-column" :class="{readonly: isReadonlyAction}">
		<div class="page-form-content flex-grow-1 pr-2 py-1 overflow-y-auto">
			<div class="field grid">
				<label for="${pid}displayName" class="field-label col-12 mb-2 md:col-3 md:mb-0">
					<@spring.message code='name' />
				</label>
		        <div class="field-input col-12 md:col-9">
		        	<p-inputtext id="${pid}displayName" v-model="pm.displayName" type="text" class="input w-full"
		        		name="displayName" required maxlength="200" autofocus>
		        	</p-inputtext>
		        </div>
			</div>
			<div class="field grid">
				<label for="${pid}driverLibrary" class="field-label col-12 mb-2 md:col-3 md:mb-0"
					title="<@spring.message code='driverEntity.driverLibrary.desc' />">
					<@spring.message code='driverLibrary' />
				</label>
		        <div class="field-input col-12 md:col-9">
		        	<div id="${pid}driverLibrary" class="input p-component p-inputtext w-full overflow-auto" style="height:8rem;">
		        		<p-chip v-for="dlf in driverLibraryFiles.files" :key="dlf.key"
		        			class="mb-2" :removable="!isReadonlyAction" @remove="onRemovedLibraryFile($event, dlf.name)">
		        			<a :href="genDownloadLibraryPath(dlf.name)" target="_blank" class="link">
		        				{{dlf.name+' ('+dlf.size+')'}}
		        			</a>
		        		</p-chip>
		        	</div>
		        	<div class="fileupload-wrapper mt-1" v-if="!isReadonlyAction">
			        	<p-fileupload mode="basic" name="file" :url="uploadFileUrl"
			        		@upload="onUploadedLibraryFile" @select="uploadFileOnSelect" @progress="uploadFileOnProgress"
			        		:auto="true" choose-label="<@spring.message code='upload' />" class="p-button-secondary">
			        	</p-fileupload>
			        	<div class="desc text-color-secondary">
			        		<small><@spring.message code='driverEntity.driverLibrary.desc1' /></small>
			        	</div>
						<#include "../include/page_fileupload.ftl">
		        	</div>
		        </div>
			</div>
			<div class="field grid">
				<label for="${pid}driverClassName" class="field-label col-12 mb-2 md:col-3 md:mb-0"
					title="<@spring.message code='driverEntity.driverClassName.desc' />">
					<@spring.message code='driverClassName' />
				</label>
		        <div class="field-input col-12 md:col-9">
		        	<p-inputtext id="${pid}driverClassName" v-model="pm.driverClassName" type="text" class="input w-full"
		        		name="driverClassName" required maxlength="500">
		        	</p-inputtext>
		        </div>
			</div>
			<div class="field grid">
				<label for="${pid}displayDesc" class="field-label col-12 mb-2 md:col-3 md:mb-0">
					<@spring.message code='desc' />
				</label>
		        <div class="field-input col-12 md:col-9">
		        	<p-textarea id="${pid}displayDesc" v-model="pm.displayDesc" rows="6" class="input w-full"
		        		name="displayDesc" maxlength="500">
		        	</p-textarea>
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
	po.submitUrl = "/driverEntity/"+po.submitAction;
	
	po.refreshDriverLibrary = function()
	{
		var pm = po.vuePageModel();
		
		po.getJson("/driverEntity/listDriverFile", {"id": pm.id}, function(fileInfos)
		{
			po.setDriverLibrary(fileInfos);
		});
	};
	
	po.driverLibraryKeySeq = 0;
	
	po.setDriverLibrary = function(files)
	{
		var driverLibraryFiles = po.vueReactive("driverLibraryFiles");
		
		if(!files)
			files = po.vueRaw(driverLibraryFiles.files);
		
		var seq = po.driverLibraryKeySeq++;
		$.each(files, function(idx, file)
		{
			file.key = file.name + seq;
		});
		
		driverLibraryFiles.files = files;
	};
	
	po.inflateSubmitAction = function(action)
	{
		var newData = { driverEntity: action.options.data, driverLibraryFileNames: [] };
		
		var driverLibraryFiles = po.vueReactive("driverLibraryFiles");
		var dlfs = driverLibraryFiles.files;
		for(var i=0; i<dlfs.length; i++)
			newData.driverLibraryFileNames.push(dlfs[i].name);
		
		action.options.data = newData;
	};
	
	var formModel = <@writeJson var=formModel />;
	po.setupForm(formModel, po.submitUrl);
	
	po.vueReactive("driverLibraryFiles", { files: [] });
	po.vueRef("uploadFileUrl", po.concatContextPath("/driverEntity/uploadDriverFile?id="+formModel.id));
	
	po.vueMethod(
	{
		genDownloadLibraryPath: function(name)
		{
			var pm = po.vuePageModel();
			return po.concatContextPath("/driverEntity/downloadDriverFile?id="+encodeURIComponent(pm.id)+"&file="+encodeURIComponent(name));
		},
		
		onUploadedLibraryFile: function(e)
		{
			var pm = po.vuePageModel();
			var response = $.getResponseJson(e.xhr);
			
			po.uploadFileOnUploaded(e);
			po.setDriverLibrary(response.fileInfos);
			
			var driverClassNames = response.driverClassNames;
			if(!pm.driverClassName && driverClassNames && driverClassNames[0])
				pm.driverClassName = driverClassNames[0];
		},
		
		onRemovedLibraryFile: function(e, name)
		{
			po.confirmDelete(function()
			{
				var pm = po.vuePageModel();
				
				po.ajax("/driverEntity/deleteDriverFile",
				{
					data: {id: pm.id, file: name},
					tipSuccess: false,
					success: function(response)
					{
						po.setDriverLibrary(response.data);
					}
				});
			},
			function()
			{
				po.setDriverLibrary();
			});
		}
	});
	
	if(!po.isAddAction)
	{
		po.vueMounted(function()
		{
			po.refreshDriverLibrary();
		});
	}
	
	po.vueMount();
})
(${pid});
</script>
</body>
</html>
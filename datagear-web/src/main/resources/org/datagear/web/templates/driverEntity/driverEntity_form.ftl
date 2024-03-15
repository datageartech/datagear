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
<#include "../include/page_import.ftl">
<#include "../include/html_doctype.ftl">
<html>
<head>
<#include "../include/html_head.ftl">
<title>
	<@spring.message code='module.driverEntity' />
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
				<label for="${pid}displayName" class="field-label col-12 mb-2 md:col-3 md:mb-0">
					<@spring.message code='name' />
				</label>
		        <div class="field-input col-12 md:col-9">
		        	<p-inputtext id="${pid}displayName" v-model="fm.displayName" type="text" class="input w-full"
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
		        		<p-chip v-for="dlf in pm.driverLibraryFiles.files" :key="dlf.key"
		        			class="mb-2" :removable="!pm.isReadonlyAction" @remove="onRemovedLibraryFile($event, dlf.name)">
		        			<a :href="genDownloadLibraryPath(dlf.name)" target="_blank" class="link">
		        				{{dlf.name+' ('+dlf.size+')'}}
		        			</a>
		        		</p-chip>
		        	</div>
		        	<div class="fileupload-wrapper flex align-items-center mt-1" v-if="!pm.isReadonlyAction">
			        	<p-fileupload mode="basic" name="file" :url="pm.uploadFileUrl"
			        		@upload="onUploadedLibraryFile" @select="uploadFileOnSelect" @progress="uploadFileOnProgress" @error="uploadFileOnError"
			        		:auto="true" choose-label="<@spring.message code='upload' />" class="mr-2">
			        	</p-fileupload>
						<#include "../include/page_fileupload.ftl">
		        	</div>
		        	<div class="desc text-color-secondary" v-if="!pm.isReadonlyAction">
		        		<small><@spring.message code='driverEntity.driverLibrary.desc1' /></small>
		        	</div>
		        </div>
			</div>
			<div class="field grid">
				<label for="${pid}driverClassName" class="field-label col-12 mb-2 md:col-3 md:mb-0"
					title="<@spring.message code='driverEntity.driverClassName.desc' />">
					<@spring.message code='driverClassName' />
				</label>
		        <div class="field-input col-12 md:col-9">
		        	<p-inputtext id="${pid}driverClassName" v-model="fm.driverClassName" type="text" class="input w-full"
		        		name="driverClassName" required maxlength="500">
		        	</p-inputtext>
		        </div>
			</div>
			<div class="field grid">
				<label for="${pid}displayDesc" class="field-label col-12 mb-2 md:col-3 md:mb-0">
					<@spring.message code='desc' />
				</label>
		        <div class="field-input col-12 md:col-9">
		        	<p-textarea id="${pid}displayDesc" v-model="fm.displayDesc" rows="6" class="input w-full"
		        		name="displayDesc" maxlength="500">
		        	</p-textarea>
		        </div>
			</div>
		</div>
		<div class="page-form-foot flex-grow-0 flex justify-content-center gap-2 pt-2">
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
		var fm = po.vueFormModel();
		
		po.getJson("/driverEntity/listDriverFile", {"id": fm.id}, function(fileInfos)
		{
			po.setDriverLibrary(fileInfos);
		});
	};
	
	po.driverLibraryKeySeq = 0;
	
	po.setDriverLibrary = function(files)
	{
		var pm = po.vuePageModel();
		
		if(!files)
			files = po.vueRaw(pm.driverLibraryFiles.files);
		
		var seq = po.driverLibraryKeySeq++;
		$.each(files, function(idx, file)
		{
			file.key = file.name + seq;
		});
		
		pm.driverLibraryFiles.files = files;
	};
	
	po.beforeSubmitForm = function(action)
	{
		var pm = po.vuePageModel();
		var newData = { driverEntity: action.options.data, driverLibraryFileNames: [] };
		
		var dlfs = pm.driverLibraryFiles.files;
		for(var i=0; i<dlfs.length; i++)
			newData.driverLibraryFileNames.push(dlfs[i].name);
		
		action.options.data = newData;
	};
	
	var formModel = $.unescapeHtmlForJson(<@writeJson var=formModel />);
	po.setupForm(formModel);
	
	po.vuePageModel(
	{
		driverLibraryFiles: { files: [] },
		uploadFileUrl: po.concatContextPath("/driverEntity/uploadDriverFile?id="+formModel.id)
	});
	
	po.vueMethod(
	{
		genDownloadLibraryPath: function(name)
		{
			var fm = po.vueFormModel();
			return po.concatContextPath("/driverEntity/downloadDriverFile?id="+encodeURIComponent(fm.id)+"&file="+encodeURIComponent(name));
		},
		
		onUploadedLibraryFile: function(e)
		{
			var fm = po.vueFormModel();
			var response = $.getResponseJson(e.xhr);
			
			po.uploadFileOnUploaded(e);
			po.setDriverLibrary(response.fileInfos);
			
			var driverClassNames = response.driverClassNames;
			if(!fm.driverClassName && driverClassNames && driverClassNames[0])
				fm.driverClassName = driverClassNames[0];
		},
		
		onRemovedLibraryFile: function(e, name)
		{
			po.confirmDelete(function()
			{
				var fm = po.vueFormModel();
				
				po.ajax("/driverEntity/deleteDriverFile",
				{
					data: {id: fm.id, file: name},
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
})
(${pid});
</script>
<#include "../include/page_vue_mount.ftl">
</body>
</html>
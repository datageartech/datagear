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
				<label for="${pid}driverLibrary" class="field-label col-12 mb-2 md:col-3 md:mb-0">
					<@spring.message code='driverLibrary' />
				</label>
		        <div class="field-input col-12 md:col-9">
		        	<div id="${pid}driverLibrary" class="input p-inputtext w-full overflow-auto" style="height:8rem;">
		        		<p-chip v-for="dlf in driverLibraryFiles" :key="dlf.name" :label="dlf.name" class="mb-2" :removable="!isReadonlyAction" @remove="onRemoveDriverLibraryFile($event, dlf.name)"></p-chip>
		        	</div>
		        	<div>
			        	<p-button type="button" label="<@spring.message code='upload' />"
			        		@click="onUploadDriverLibraryFile" class="p-button-secondary mt-1"
			        		v-if="!isReadonlyAction">
			        	</p-button>
		        	</div>
		        	<div class="desc text-color-secondary">
			        	<small><@spring.message code='driverEntity.driverLibrary.desc' /></small>
		        	</div>
		        </div>
			</div>
			<div class="field grid">
				<label for="${pid}driverClassName" class="field-label col-12 mb-2 md:col-3 md:mb-0">
					<@spring.message code='driverClassName' />
				</label>
		        <div class="field-input col-12 md:col-9">
		        	<p-inputtext id="${pid}driverClassName" v-model="pm.driverClassName" type="text" class="input w-full"
		        		name="driverClassName" required maxlength="500">
		        	</p-inputtext>
		        	<div class="desc text-color-secondary">
		        		<small><@spring.message code='driverEntity.driverClassName.desc' /></small>
		        	</div>
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
	po.url = function(action)
	{
		return "/driverEntity/"+action;
	};
	
	po.submitUrl = "/driverEntity/"+po.submitAction;
	
	po.refreshDriverLibrary = function()
	{
		var pm = po.vuePageModel();
		
		if(pm.id)
		{
			$.getJSON("${contextPath}/driverEntity/listDriverFile", {"id": pm.id}, function(fileInfos)
			{
				po.vueRef("driverLibraryFiles", fileInfos);
			});
		}
	};
	
	po.vueRef("driverLibraryFiles", []);
	
	var formModel = <@writeJson var=formModel />;
	po.setupForm(formModel, po.submitUrl);
	
	po.vueMethod(
	{
		onRemoveDriverLibraryFile: function(e, name)
		{
			
		},
		
		onUploadDriverLibraryFile: function()
		{
			
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
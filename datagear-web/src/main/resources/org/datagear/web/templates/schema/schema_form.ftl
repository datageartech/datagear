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
	<@spring.message code='module.schema' />
	<#include "../include/html_request_action_suffix.ftl">
</title>
</head>
<body class="p-card no-border">
<#include "../include/page_obj.ftl">
<div id="${pid}" class="page page-form horizontal">
	<form class="flex flex-column" :class="{readonly: isReadonlyAction}">
		<div class="page-form-content flex-grow-1 pr-2 py-1 overflow-y-auto">
			<div class="field grid">
				<label for="${pid}name" class="field-label col-12 mb-2 md:col-3 md:mb-0">
					<@spring.message code='name' />
				</label>
		        <div class="field-input col-12 md:col-9">
		        	<p-inputtext id="${pid}name" v-model="pm.title" type="text" class="input w-full"
		        		name="title" required maxlength="100" autofocus>
		        	</p-inputtext>
		        </div>
			</div>
			<div class="field grid">
				<label for="${pid}url" class="field-label col-12 mb-2 md:col-3 md:mb-0">
					<@spring.message code='url' />
				</label>
		        <div class="field-input col-12 md:col-9">
		        	<div class="p-inputgroup">
			        	<p-inputtext id="${pid}url" v-model="pm.url" type="text" class="input"
			        		name="url" required maxlength="1000" placeholder="jdbc:">
			        	</p-inputtext>
			        	<p-button type="button" label="<@spring.message code='help' />" @click="onBuildSchemaUrl"
			        		class="p-button-secondary"
			        		v-if="!isReadonlyAction">
			        	</p-button>
		        	</div>
		        </div>
			</div>
			<div class="field grid">
				<label for="${pid}user" class="field-label col-12 mb-2 md:col-3 md:mb-0">
					<@spring.message code='username' />
				</label>
		        <div class="field-input col-12 md:col-9">
		        	<p-inputtext id="${pid}user" v-model="pm.user" type="text" class="input w-full"
		        		name="user" maxlength="200" autocomplete="off">
		        	</p-inputtext>
		        </div>
			</div>
			<div class="field grid" v-if="!isReadonlyAction">
				<label for="${pid}password" class="field-label col-12 mb-2 md:col-3 md:mb-0">
					<@spring.message code='password' />
				</label>
		        <div class="field-input col-12 md:col-9">
		        	<p-password id="${pid}password" v-model="pm.password" class="input w-full"
		        		input-class="w-full" toggle-mask :feedback="false"
		        		name="password" maxlength="100" autocomplete="new-password">
		        	</p-password>
		        </div>
			</div>
			<div class="field grid">
				<label for="${pid}driverEntity" class="field-label col-12 mb-2 md:col-3 md:mb-0">
					<@spring.message code='module.driverEntity' />
				</label>
		        <div class="field-input col-12 md:col-9">
		        	<div class="p-inputgroup">
		        		<div class="p-input-icon-right flex-grow-1">
			        		<i class="pi pi-times cursor-pointer" @click="onDeleteDriverEntity" v-if="!isReadonlyAction">
			        		</i>
				        	<p-inputtext id="${pid}driverEntity" v-model="pm.driverEntity.displayName" type="text" class="input w-full h-full border-noround-right"
				        		readonly="readonly" name="driverEntity.displayName" maxlength="200">
				        	</p-inputtext>
			        	</div>
			        	<p-button type="button" label="<@spring.message code='select' />"
			        		@click="onSelectDriverEntity" class="p-button-secondary"
			        		v-if="!isReadonlyAction">
			        	</p-button>
		        	</div>
		        </div>
			</div>
			<div class="field grid" v-if="isReadonlyAction">
				<label for="${pid}createUser" class="field-label col-12 mb-2 md:col-3 md:mb-0">
					<@spring.message code='createUser' />
				</label>
		        <div class="field-input col-12 md:col-9">
		        	<p-inputtext id="${pid}createUser" v-model="pm.createUser.nameLabel" type="text" class="input w-full" readonly="readonly">
		        	</p-inputtext>
		        </div>
			</div>
			<div class="field grid" v-if="isReadonlyAction">
				<label for="${pid}createTime" class="field-label col-12 mb-2 md:col-3 md:mb-0">
					<@spring.message code='createTime' />
				</label>
		        <div class="field-input col-12 md:col-9">
		        	<p-inputtext id="${pid}createTime" v-model="pm.createTime" type="text" class="input w-full" readonly="readonly">
		        	</p-inputtext>
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
	po.submitUrl = "/schema/"+po.submitAction;
	
	var formModel = <@writeJson var=formModel />;
	formModel = (formModel || {});
	formModel.driverEntity = (formModel.driverEntity == null ? {} : formModel.driverEntity);
	
	po.setupForm(formModel, po.submitUrl);
	
	po.vueMethod(
	{
		onBuildSchemaUrl: function()
		{
			var pm = po.vuePageModel();
			
			po.open("/schemaUrlBuilder/build",
			{
				data: {url: pm.url},
				contentType: $.CONTENT_TYPE_FORM,
				pageParam:
				{
					submitSuccess: function(url)
					{
						var pm = po.vuePageModel();
						pm.url = url;
					}
				}
			});
		},
		onDeleteDriverEntity: function()
		{
			var pm = po.vuePageModel();
			pm.driverEntity = {};
		},
		
		onSelectDriverEntity: function()
		{
			po.handleOpenSelectAction("/driverEntity/select", function(driverEntity)
			{
				var pm = po.vuePageModel();
				pm.driverEntity = driverEntity;
			});
		}
	});
	
	po.vueMount();
})
(${pid});
</script>
</body>
</html>
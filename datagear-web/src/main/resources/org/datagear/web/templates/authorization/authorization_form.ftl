<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#assign Authorization=statics['org.datagear.management.domain.Authorization']>
<#include "../include/page_import.ftl">
<#include "../include/html_doctype.ftl">
<html>
<head>
<#include "../include/html_head.ftl">
<title>
	<#include "../include/html_app_name_prefix.ftl">
	<@spring.message code='${resourceMeta.authModuleLabel}' />
	<#include "../include/html_request_action_suffix.ftl">
</title>
</head>
<body class="p-card no-border">
<#include "../include/page_obj.ftl">
<div id="${pid}" class="page page-form horizontal">
	<form class="flex flex-column" :class="{readonly: isReadonlyAction}">
		<div class="page-form-content flex-grow-1 pr-2 py-1 overflow-y-auto">
			<div class="field grid">
				<label for="${pid}principalType" class="field-label col-12 mb-2 md:col-3 md:mb-0">
					<@spring.message code='${resourceMeta.authPrincipalTypeLabel}' />
				</label>
		        <div class="field-input col-12 md:col-9">
		        	<p-selectbutton v-model="pm.principalType" :options="principalTypeOptions"
		        		option-label="name" option-value="value" @change="onPrincipalTypeChange" class="input w-full">
		        	</p-selectbutton>
		        </div>
			</div>
			<div class="field grid">
				<label for="${pid}principalName" class="field-label col-12 mb-2 md:col-3 md:mb-0">
					<@spring.message code='${resourceMeta.authPrincipalLabel}' />
				</label>
		        <div class="field-input col-12 md:col-9" style="min-height:2.5rem;">
		        	<div class="p-inputgroup" v-show="pm.principalType == '${Authorization.PRINCIPAL_TYPE_USER}'">
			        	<p-inputtext id="${pid}principalName" v-model="pm.principalName" type="text" class="input"
			        		name="principalName" required maxlength="200" readonly="readonly">
			        	</p-inputtext>
			        	<p-button type="button" label="<@spring.message code='select' />" @click="onSelectUser"
			        		class="p-button-secondary" v-if="!isReadonlyAction">
			        	</p-button>
		        	</div>
		        	
		        	<div class="p-inputgroup" v-show="pm.principalType == '${Authorization.PRINCIPAL_TYPE_ROLE}'">
			        	<p-inputtext id="${pid}principalName" v-model="pm.principalName" type="text" class="input"
			        		name="principalName" required maxlength="200" readonly="readonly">
			        	</p-inputtext>
			        	<p-button type="button" label="<@spring.message code='select' />" @click="onSelectRole"
			        		class="p-button-secondary" v-if="!isReadonlyAction">
			        	</p-button>
		        	</div>
		        	
		        	<p-inputtext id="${pid}principalName" v-model="pm.principalName" type="text" class="input w-full"
		        		v-show="pm.principalType == '${Authorization.PRINCIPAL_TYPE_ANONYMOUS}'"
		        		name="principalName" required maxlength="200" readonly="readonly">
		        	</p-inputtext>
		        	
		        	<p-inputtext id="${pid}principalName" v-model="pm.principalName" type="text" class="input w-full"
		        		v-show="pm.principalType == '${Authorization.PRINCIPAL_TYPE_ALL}'"
		        		name="principalName" required maxlength="200" readonly="readonly">
		        	</p-inputtext>
		        </div>
			</div>
			<div class="field grid" :class="{hidden: singlePermission}">
				<label for="${pid}permission" class="field-label col-12 mb-2 md:col-3 md:mb-0">
					<@spring.message code='${resourceMeta.authPermissionLabel}' />
				</label>
		        <div class="field-input col-12 md:col-9">
		        	<p-selectbutton v-model="pm.permission" :options="permissionOptions"
		        		option-label="name" option-value="value" class="input w-full" @change="onPermissionChange">
		        	</p-selectbutton>
		        	<div class="mt-1" style="min-height:1.5rem;">
		        		<small class="text-color-secondary">{{permissionDesc}}</small>
		        	</div>
		        </div>
			</div>
			<div class="field grid" :class="{hidden: !enableSetEnable}">
				<label for="${pid}enabled" class="field-label col-12 mb-2 md:col-3 md:mb-0">
					<@spring.message code='${resourceMeta.authEnabledLabel}' />
				</label>
		        <div class="field-input col-12 md:col-9">
		        	<p-selectbutton v-model="pm.enabled" :options="booleanOptions"
		        		option-label="name" option-value="value" class="input w-full">
		        	</p-selectbutton>
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
	po.submitUrl = "/authorization/${resourceMeta.resourceType}/" + encodeURIComponent("${resource?js_string?no_esc}") + "/" + po.submitAction;
	po.enableSetEnable = ("${resourceMeta.enableSetEnable?string('true', 'false')}" == "true");
	po.singlePermission = ("${resourceMeta.singlePermission?string('true', 'false')}"  == "true");
	
	po.vueRef("enableSetEnable", po.enableSetEnable);
	po.vueRef("singlePermission", po.singlePermission);
	
	po.vueRef("principalTypeOptions",
	[
		{name: "<@spring.message code='authorization.principalType.USER' />", value: "${Authorization.PRINCIPAL_TYPE_USER}"},
		{name: "<@spring.message code='authorization.principalType.ROLE' />", value: "${Authorization.PRINCIPAL_TYPE_ROLE}"},
		{name: "<@spring.message code='authorization.principalType.ANONYMOUS' />", value: "${Authorization.PRINCIPAL_TYPE_ANONYMOUS}"},
		{name: "<@spring.message code='authorization.principalType.ALL' />", value: "${Authorization.PRINCIPAL_TYPE_ALL}"}
	]);
	
	var permissionOptions = [];
	<#list resourceMeta.permissionMetas as pmeta>
	permissionOptions.push(
	{
		name: "<@spring.message code='${pmeta.permissionLabel}' />",
		value: ${pmeta.permission},
		desc: "<@spring.message code='${pmeta.permissionLabelDesc}' />"
	});
	</#list>
	
	po.vueRef("permissionOptions", permissionOptions);
	
	po.vueComputed("permissionDesc", function()
	{
		var pm = po.vuePageModel();
		var permissionOptions = po.vueUnref("permissionOptions");
		
		var idx = $.inArrayById(permissionOptions, pm.permission, "value");
		return (idx >= 0 ? permissionOptions[idx].desc : "");
	});
	
	var formModel = <@writeJson var=formModel />;
	formModel = $.unescapeHtmlForJson(formModel);
	po.setupForm(formModel, po.submitUrl);
	
	po.vueMethod(
	{
		onPrincipalTypeChange: function(e)
		{
			var pt = e.value;
			var pm = po.vuePageModel();
			
			if(pt == "${Authorization.PRINCIPAL_TYPE_ANONYMOUS}")
			{
				pm.principal = "${Authorization.PRINCIPAL_ANONYMOUS}";
				pm.principalName = "<@spring.message code='authorization.principalType.ANONYMOUS' />";
			}
			else if(pt == "${Authorization.PRINCIPAL_TYPE_ALL}")
			{
				pm.principal = "${Authorization.PRINCIPAL_ALL}";
				pm.principalName = "<@spring.message code='authorization.principalType.ALL' />";
			}
			else
			{
				pm.principal = "";
				pm.principalName = "";
			}
		},
		
		onSelectUser: function()
		{
			po.handleOpenSelectAction("/user/select", function(user)
			{
				var pm = po.vuePageModel();
				pm.principal = user.id;
				pm.principalName = user.nameLabel;
			});
		},
		
		onSelectRole: function()
		{
			po.handleOpenSelectAction("/role/select", function(role)
			{
				var pm = po.vuePageModel();
				pm.principal = role.id;
				pm.principalName = role.name;
			});
		}
	});
	
	po.vueMount();
})
(${pid});
</script>
</body>
</html>
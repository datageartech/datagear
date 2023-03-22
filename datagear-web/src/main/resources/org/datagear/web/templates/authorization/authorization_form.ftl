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
<#assign Authorization=statics['org.datagear.management.domain.Authorization']>
<#include "../include/page_import.ftl">
<#include "../include/html_doctype.ftl">
<html>
<head>
<#include "../include/html_head.ftl">
<title>
	<@spring.message code='${resourceMeta.authModuleLabel}' />
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
				<label for="${pid}principalType" class="field-label col-12 mb-2 md:col-3 md:mb-0">
					<@spring.message code='${resourceMeta.authPrincipalTypeLabel}' />
				</label>
		        <div class="field-input col-12 md:col-9">
		        	<p-selectbutton v-model="fm.principalType" :options="pm.principalTypeOptions"
		        		option-label="name" option-value="value" @change="onPrincipalTypeChange" class="input w-full">
		        	</p-selectbutton>
		        </div>
			</div>
			<div class="field grid">
				<label for="${pid}principalName" class="field-label col-12 mb-2 md:col-3 md:mb-0">
					<@spring.message code='${resourceMeta.authPrincipalLabel}' />
				</label>
		        <div class="field-input col-12 md:col-9" style="min-height:2.5rem;">
		        	<div class="p-inputgroup" v-show="fm.principalType == '${Authorization.PRINCIPAL_TYPE_USER}'">
			        	<p-inputtext id="${pid}principalName" v-model="fm.principalName" type="text" class="input"
			        		name="principalName" required maxlength="200" readonly="readonly">
			        	</p-inputtext>
			        	<p-button type="button" label="<@spring.message code='select' />" @click="onSelectUser"
			        		v-if="!pm.isReadonlyAction">
			        	</p-button>
		        	</div>
		        	
		        	<div class="p-inputgroup" v-show="fm.principalType == '${Authorization.PRINCIPAL_TYPE_ROLE}'">
			        	<p-inputtext id="${pid}principalName" v-model="fm.principalName" type="text" class="input"
			        		name="principalName" required maxlength="200" readonly="readonly">
			        	</p-inputtext>
			        	<p-button type="button" label="<@spring.message code='select' />" @click="onSelectRole"
			        		v-if="!pm.isReadonlyAction">
			        	</p-button>
		        	</div>
		        	
		        	<p-inputtext id="${pid}principalName" v-model="fm.principalName" type="text" class="input w-full"
		        		v-show="fm.principalType == '${Authorization.PRINCIPAL_TYPE_ANONYMOUS}'"
		        		name="principalName" required maxlength="200" readonly="readonly">
		        	</p-inputtext>
		        	
		        	<p-inputtext id="${pid}principalName" v-model="fm.principalName" type="text" class="input w-full"
		        		v-show="fm.principalType == '${Authorization.PRINCIPAL_TYPE_ALL}'"
		        		name="principalName" required maxlength="200" readonly="readonly">
		        	</p-inputtext>
		        </div>
			</div>
			<div class="field grid" :class="{hidden: pm.singlePermission}">
				<label for="${pid}permission" class="field-label col-12 mb-2 md:col-3 md:mb-0">
					<@spring.message code='${resourceMeta.authPermissionLabel}' />
				</label>
		        <div class="field-input col-12 md:col-9">
		        	<p-selectbutton v-model="fm.permission" :options="pm.permissionOptions"
		        		option-label="name" option-value="value" class="input w-full" @change="onPermissionChange">
		        	</p-selectbutton>
		        	<div class="mt-1" style="min-height:1.5rem;">
		        		<small class="text-color-secondary">{{permissionDesc}}</small>
		        	</div>
		        </div>
			</div>
			<div class="field grid" :class="{hidden: !pm.enableSetEnable}">
				<label for="${pid}enabled" class="field-label col-12 mb-2 md:col-3 md:mb-0">
					<@spring.message code='${resourceMeta.authEnabledLabel}' />
				</label>
		        <div class="field-input col-12 md:col-9">
		        	<p-selectbutton v-model="fm.enabled" :options="pm.booleanOptions"
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
	
	var permissionOptions = [];
	<#list resourceMeta.permissionMetas as pmeta>
	permissionOptions.push(
	{
		name: "<@spring.message code='${pmeta.permissionLabel}' />",
		value: ${pmeta.permission},
		desc: "<@spring.message code='${pmeta.permissionLabelDesc}' />"
	});
	</#list>
	
	po.vuePageModel(
	{
		enableSetEnable: po.enableSetEnable,
		singlePermission: po.singlePermission,
		principalTypeOptions:
		[
			{name: "<@spring.message code='authorization.principalType.USER' />", value: "${Authorization.PRINCIPAL_TYPE_USER}"},
			{name: "<@spring.message code='authorization.principalType.ROLE' />", value: "${Authorization.PRINCIPAL_TYPE_ROLE}"},
			{name: "<@spring.message code='authorization.principalType.ANONYMOUS' />", value: "${Authorization.PRINCIPAL_TYPE_ANONYMOUS}"},
			{name: "<@spring.message code='authorization.principalType.ALL' />", value: "${Authorization.PRINCIPAL_TYPE_ALL}"}
		],
		permissionOptions: permissionOptions
	});
	
	po.vueComputed("permissionDesc", function()
	{
		var fm = po.vueFormModel();
		var pm = po.vuePageModel();
		
		var idx = $.inArrayById(pm.permissionOptions, fm.permission, "value");
		return (idx >= 0 ? pm.permissionOptions[idx].desc : "");
	});
	
	var formModel = $.unescapeHtmlForJson(<@writeJson var=formModel />);
	po.setupForm(formModel);
	
	po.vueMethod(
	{
		onPrincipalTypeChange: function(e)
		{
			var pt = e.value;
			var fm = po.vueFormModel();
			
			if(pt == "${Authorization.PRINCIPAL_TYPE_ANONYMOUS}")
			{
				fm.principal = "${Authorization.PRINCIPAL_ANONYMOUS}";
				fm.principalName = "<@spring.message code='authorization.principalType.ANONYMOUS' />";
			}
			else if(pt == "${Authorization.PRINCIPAL_TYPE_ALL}")
			{
				fm.principal = "${Authorization.PRINCIPAL_ALL}";
				fm.principalName = "<@spring.message code='authorization.principalType.ALL' />";
			}
			else
			{
				fm.principal = "";
				fm.principalName = "";
			}
		},
		
		onSelectUser: function()
		{
			po.handleOpenSelectAction("/user/select", function(user)
			{
				var fm = po.vueFormModel();
				fm.principal = user.id;
				fm.principalName = user.nameLabel;
			});
		},
		
		onSelectRole: function()
		{
			po.handleOpenSelectAction("/role/select", function(role)
			{
				var fm = po.vueFormModel();
				fm.principal = role.id;
				fm.principalName = role.name;
			});
		}
	});
})
(${pid});
</script>
<#include "../include/page_vue_mount.ftl">
</body>
</html>
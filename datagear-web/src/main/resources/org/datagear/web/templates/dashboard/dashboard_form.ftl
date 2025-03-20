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
<#assign DashboardVersion=statics['org.datagear.web.analysis.DashboardVersion']>
<#include "../include/page_import.ftl">
<#include "../include/html_doctype.ftl">
<html>
<head>
<#include "../include/html_head.ftl">
<title>
	<@spring.message code='module.dashboard' />
	<#include "../include/html_request_action_suffix.ftl">
	<#include "../include/html_app_name_suffix.ftl">
</title>
</head>
<body class="p-card no-border">
<#include "../include/page_obj.ftl">
<div id="${pid}" class="page page-form horizontal page-form-dashboard">
	<form id="${pid}form" class="flex flex-column" :class="{readonly: pm.isReadonlyAction}">
		<div class="page-form-content flex-grow-1 px-2 py-1 overflow-y-auto">
			<div class="field grid">
				<label for="${pid}name" class="field-label col-12 mb-2 md:col-3 md:mb-0">
					<@spring.message code='name' />
				</label>
		        <div class="field-input col-12 md:col-9">
		        	<p-inputtext id="${pid}name" v-model="fm.name" type="text" class="input w-full"
		        		name="name" required maxlength="100" autofocus>
		        	</p-inputtext>
		        </div>
			</div>
			<div class="field grid">
				<label for="${pid}ownerProject" class="field-label col-12 mb-2 md:col-3 md:mb-0">
					<@spring.message code='ownerProject' />
				</label>
				<div class="field-input col-12 md:col-9">
					<div class="p-inputgroup">
						<div class="p-input-icon-right flex-grow-1">
							<i class="pi pi-times cursor-pointer opacity-60" @click="onDeleteAnalysisProject" v-if="!pm.isReadonlyAction">
							</i>
							<p-inputtext id="${pid}ownerProject" v-model="fm.analysisProject.name" type="text" class="input w-full h-full border-noround-right"
								readonly="readonly" name="analysisProject.name" maxlength="200">
							</p-inputtext>
						</div>
						<p-button type="button" label="<@spring.message code='select' />"
							@click="onSelectAnalysisProject" class="p-button-secondary"
							v-if="!pm.isReadonlyAction">
						</p-button>
					</div>
				</div>
			</div>
			<!--
			<div class="field grid">
				<label for="${pid}name" class="field-label col-12 mb-2 md:col-3 md:mb-0">
					<@spring.message code='dashboard.version' />
				</label>
		        <div class="field-input col-12 md:col-9">
		        	<p-dropdown v-model="fm.version" :options="pm.versionDropdownItems" option-label="label" option-value="value"
		        		@change="onVersionChange" class="input w-full">
		        	</p-dropdown>
		        	<div class="validate-msg">
		        		<input name="version" required type="text" class="validate-proxy" />
		        	</div>
		        	<div class="desc text-color-secondary">
		        		<small><@spring.message code='dashboard.version.desc' /></small>
		        	</div>
		        </div>
			</div>
			-->
		</div>
		<div class="page-form-foot flex-grow-0 flex justify-content-center gap-2 pt-2">
			<p-button type="submit" label="<@spring.message code='save' />"></p-button>
			<p-button type="button" label="<@spring.message code='saveAndDesign' />" @click="onSaveAndDesign"></p-button>
		</div>
	</form>
</div>
<#include "../include/page_form.ftl">
<script>
(function(po)
{
	po.submitUrl = "/dashboard/"+po.submitAction;
	po.copySourceId = "${copySourceId!''}";
	
	if(po.copySourceId)
		po.submitUrl = $.addParam(po.submitUrl, "copySourceId", po.copySourceId);
	
	po.inSaveAndDesignAction = function(val)
	{
		if(val === undefined)
			return (po._inSaveAndDesignAction == true);
		
		po._inSaveAndDesignAction = val;
	};
	
	po.beforeSubmitForm = function(action)
	{
		action.options.inSaveAndDesignAction = po.inSaveAndDesignAction();
	};
	
	var formModel = $.unescapeHtmlForJson(<@writeJson var=formModel />);
	formModel.analysisProject = (formModel.analysisProject == null ? {} : formModel.analysisProject);
	
	po.setupForm(formModel,
	{
		success: function(response)
		{
			var id = (response.data ? response.data.id : "");
			
			var options = this;
			if(options.inSaveAndDesignAction)
			{
				window.open(po.concatContextPath("/dashboard/design/"+encodeURIComponent(id)));
			}
		}
	});

	po.vuePageModel(
	{
		versionDropdownItems:
		[
			{
				label: "${DashboardVersion.V_1_0}",
				value: "${DashboardVersion.V_1_0}"
			}
		]
	});
	
	po.vueMethod(
	{
		onDeleteAnalysisProject: function()
		{
			var fm = po.vueFormModel();
			fm.analysisProject = {};
		},
		
		onSelectAnalysisProject: function()
		{
			po.handleOpenSelectAction("/analysisProject/select", function(analysisProject)
			{
				var fm = po.vueFormModel();
				fm.analysisProject = analysisProject;
			});
		},
		
		onSaveAndDesign: function(e)
		{
			try
			{
				po.inSaveAndDesignAction(true);
				po.form().submit();
			}
			finally
			{
				po.inSaveAndDesignAction(false);
			}
		}
	});
})
(${pid});
</script>
<#include "../include/page_vue_mount.ftl">
</body>
</html>
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
	<@spring.message code='module.dashboard' />
	<#include "../include/html_request_action_suffix.ftl">
</title>
</head>
<body class="p-card no-border">
<#include "../include/page_obj.ftl">
<div id="${pid}" class="page page-form horizontal page-form-dashboard">
	<form class="flex flex-column" :class="{readonly: pm.isReadonlyAction}">
		<div class="page-form-content flex-grow-1 px-2 py-1 overflow-y-auto">
			<div class="field grid" v-if="!pm.resourceContentWrapperFullSize">
				<label for="${pid}name" class="field-label col-12 mb-2 md:col-3 md:mb-0">
					<@spring.message code='name' />
				</label>
		        <div class="field-input col-12 md:col-9">
		        	<p-inputtext id="${pid}name" v-model="fm.name" type="text" class="input w-full"
		        		name="name" required maxlength="100" autofocus>
		        	</p-inputtext>
		        </div>
			</div>
			<div class="field grid" v-if="!pm.resourceContentWrapperFullSize">
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
			<div class="field grid mb-0">
				<label for="${pid}resourceContents" class="field-label col-12 mb-2">
					<p-button type="button" :icon="pm.resourceContentWrapperFullSize ? 'pi pi-arrow-down' : 'pi pi-arrow-up'"
						class="p-button-xs p-button-secondary p-button-text p-button-rounded mr-1"
						@click="onToggleResourceContentWrapperFullSize">
					</p-button>
					<span><@spring.message code='dashboardResource' /></span>
					<span v-if="pm.resourceContentWrapperFullSize">
						<span><@spring.message code='bracket.left' /></span>
						<span>{{fm.name}}</span>
						<span><@spring.message code='bracket.right' /></span>
					</span>
				</label>
		        <div class="field-input col-12">
		        	<div class="resource-contents-wrapper grid grid-nogutter flex-nowrap">
		        		<div class="col-8 md:col-9 pr-1">
		        			<#include "include/dashboard_form_editor.ftl">
		        		</div>
		        		<div class="col-4 md:col-3 pl-2">
		        			<#include "include/dashboard_form_resource.ftl">
		        		</div>
		        	</div>
		        </div>
			</div>
		</div>
		<div class="page-form-foot flex-grow-0 pt-3 text-center">
			<p-button type="submit" label="<@spring.message code='save' />"></p-button>
		</div>
	</form>
</div>
<#include "../include/page_form.ftl">
<#include "../include/page_tabview.ftl">
<#include "../include/page_code_editor.ftl">
<#include "include/dashboard_code_completions.ftl">
<script>
(function(po)
{
	po.submitUrl = "/dashboard/"+po.submitAction;
	po.dashboardGlobalResUrlPrefix = "${dashboardGlobalResUrlPrefix}";
	
	po.isPersistedDashboard = function()
	{
		if(!po.isAddAction)
			return true;
		
		return (po.isAddActionSaved == true);
	};
	
	var formModel = $.unescapeHtmlForJson(<@writeJson var=formModel />);
	formModel.analysisProject = (formModel.analysisProject == null ? {} : formModel.analysisProject);
	po.setupForm(formModel,
	{
		success: function()
		{
			if(po.isAddAction)
			{
				if(!po.isPersistedDashboard())
					po.refreshLocalResources();
				
				po.isAddActionSaved = true;
			}
		}
	});
	
	po.vuePageModel(
	{
		resourceContentWrapperFullSize: po.isEditAction
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
		
		onToggleResourceContentWrapperFullSize: function(e)
		{
			var pm = po.vuePageModel();
			pm.resourceContentWrapperFullSize = !pm.resourceContentWrapperFullSize;
		}
	});
	
	po.setupResourceList();
	po.setupResourceEditor();
	
	po.vueMount();
})
(${pid});
</script>
</body>
</html>
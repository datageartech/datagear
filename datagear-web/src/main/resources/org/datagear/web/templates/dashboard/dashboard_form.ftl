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
				<div class="field-label col-12 mb-2">
					<div class="flex justify-content-between">
						<div>
							<span><@spring.message code='dashboardResource' /></span>
							<span v-if="pm.resourceContentWrapperFullSize">
								<span><@spring.message code='bracket.left' /></span>
								<span>{{fm.name}}</span>
								<span><@spring.message code='bracket.right' /></span>
							</span>
						</div>
						<p-button type="button" :icon="pm.resourceContentWrapperFullSize ? 'pi pi-arrow-down' : 'pi pi-arrow-up'"
							class="p-button-xs p-button-secondary p-button-text p-button-rounded mr-1"
							@click="onToggleResourceContentWrapperFullSize" title="<@spring.message code='showOrHideName' />">
						</p-button>
					</div>
				</div>
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
		<div class="page-form-foot flex-grow-0 flex justify-content-center gap-2 pt-2">
			<p-button type="submit" label="<@spring.message code='save' />"></p-button>
			<p-button type="button" label="<@spring.message code='saveAndShow' />" @click="onSaveAndShow"></p-button>
		</div>
	</form>
	<#include "include/dashboard_form_resource_forms.ftl">
	<#include "include/dashboard_form_editor_forms.ftl">
	<#include "../include/page_copy_to_clipboard.ftl">
</div>
<#include "../include/page_form.ftl">
<#include "../include/page_simple_form.ftl">
<#include "../include/page_tabview.ftl">
<#include "../include/page_code_editor.ftl">
<#include "../include/page_boolean_options.ftl">
<#include "include/dashboard_code_completions.ftl">
<script>
(function(po)
{
	po.submitUrl = "/dashboard/"+po.submitAction;
	po.copySourceId = "${copySourceId!''}";
	
	po.showUrl = function(name)
	{
		name = (name || "");
		
		var fm = po.vueFormModel();
		return po.concatContextPath("/dashboard/show/"+encodeURIComponent(fm.id)+"/"+name);
	};

	po.inSaveAndShowAction = function(val)
	{
		if(val === undefined)
			return (po._inSaveAndShowAction == true);
		
		po._inSaveAndShowAction = val;
	};
	
	po.beforeSubmitForm = function(action)
	{
		var data = action.options.data;
		var templateCount = (data.templates && data.templates.length != null ? data.templates.length : 0);
		
		//隐藏基本信息后无法自动校验名称，所以这里手动校验
		if(!data.name)
		{
			$.tipInfo("<@spring.message code='dashboard.nameRequired' />");
			return false;
		}
		
		data =
		{
			dashboard: data,
			resourceNames: [],
			resourceContents: [],
			resourceIsTemplates: [],
			saveAdd: !po.isPersistedDashboard(),
			copySourceId: (po.isPersistedDashboard() ? "" : po.copySourceId)
		};
		
		var editResInfos = po.getEditResInfos();
		$.each(editResInfos, function(idx, ei)
		{
			data.resourceNames.push(ei.name);
			data.resourceContents.push(ei.content);
			data.resourceIsTemplates.push(ei.isTemplate);
			
			if(ei.isTemplate)
				templateCount += 1;
		});
		
		if(templateCount == 0)
		{
			$.tipWarn("<@spring.message code='dashboard.atLeastOneTemplateRequired' />");
			return false;
		}
		
		action.options.data = data;
		action.options.saveAndShowAction = po.inSaveAndShowAction();
	};
	
	po.isPersistedDashboard = function()
	{
		if(!po.isAddAction && !po.isCopyAction)
			return true;
		
		return (po._isPersistedDashboard == true);
	};
	
	po.checkPersistedDashboard = function()
	{
		var fm = po.vueFormModel();
		
		if(!po.isPersistedDashboard() || !fm.id)
		{
			$.tipInfo("<@spring.message code='dashboard.saveRequired' />");
			return false;
		}
		
		return true;
	};
	
	var formModel = $.unescapeHtmlForJson(<@writeJson var=formModel />);
	formModel.analysisProject = (formModel.analysisProject == null ? {} : formModel.analysisProject);
	po.setupForm(formModel,
	{
		closeAfterSubmit: false,
		success: function(response)
		{
			po.updateTemplateList(response.data.templates);
			po.refreshLocalRes();
			
			if(!po.isPersistedDashboard())
			{
				po._isPersistedDashboard = true;
			}
			
			var options = this;
			if(options.saveAndShowAction)
			{
				var fm = po.vueFormModel();
				var resInfo = po.getCurrentEditResInfo(true);
				
				window.open(po.showUrl(resInfo ? resInfo.name : ""), fm.id);
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
		},
		
		onSaveAndShow: function(e)
		{
			try
			{
				po.inSaveAndShowAction(true);
				po.form().submit();
			}
			finally
			{
				po.inSaveAndShowAction(false);
			}
		}
	});
	
	po.setupResourceList();
	po.setupResourceEditor();
	
	po.vueMounted(function()
	{
		po.showFirstTemplateContent();
	});
})
(${pid});
</script>
<#include "../include/page_vue_mount.ftl">
</body>
</html>
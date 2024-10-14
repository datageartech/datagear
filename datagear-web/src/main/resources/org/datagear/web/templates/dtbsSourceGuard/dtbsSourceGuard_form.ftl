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
<#assign DtbsSourceGuard=statics['org.datagear.management.domain.DtbsSourceGuard']>
<#include "../include/page_import.ftl">
<#include "../include/html_doctype.ftl">
<html>
<head>
<#include "../include/html_head.ftl">
<title>
	<@spring.message code='module.dtbsSourceGuard' />
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
				<label for="${pid}pattern" class="field-label col-12 mb-2 md:col-3 md:mb-0"
					title="<@spring.message code='dtbsSourceGuard.pattern.desc' />">
					<@spring.message code='urlPattern' />
				</label>
		        <div class="field-input col-12 md:col-9">
		        	<p-inputtext id="${pid}pattern" v-model="fm.pattern" type="text" class="input w-full"
		        		name="pattern" required maxlength="200">
		        	</p-inputtext>
		        </div>
			</div>
			<div class="field grid">
				<label for="${pid}userPattern" class="field-label col-12 mb-2 md:col-3 md:mb-0"
					title="<@spring.message code='dtbsSourceGuard.userPattern.desc' />">
					<@spring.message code='usernamePattern' />
				</label>
		        <div class="field-input col-12 md:col-9">
		        	<p-inputtext id="${pid}userPattern" v-model="fm.userPattern" type="text" class="input w-full"
		        		name="userPattern" required maxlength="100">
		        	</p-inputtext>
		        </div>
			</div>
			<div class="field grid">
				<label for="${pid}propertyPatterns" class="field-label col-12 mb-2"
					title="<@spring.message code='dtbsSourceGuard.propertyPatterns.desc' />">
					<@spring.message code='dtbsSourceGuard.propertyPatterns' />
				</label>
				<div class="field-input col-12">
					<div class="p-component p-inputtext">
						<div class="flex flex-row pb-2" v-if="!pm.isReadonlyAction">
							<div class="h-opts flex-grow-1">
								<p-button type="button" label="<@spring.message code='add' />"
									@click="onAddPropertyPattern" class="p-button-secondary p-button-sm">
								</p-button>
								<p-button type="button" label="<@spring.message code='edit' />"
									@click="onEditPropertyPattern" class="p-button-secondary p-button-sm">
								</p-button>
								<p-button type="button" label="<@spring.message code='delete' />"
									@click="onDeletePropertyPattern" class="p-button-danger p-button-sm">
								</p-button>
							</div>
						</div>
						<div id="${pid}propertyPatterns" class="property-patterns-wrapper input w-full overflow-auto">
							<p-datatable :value="fm.propertyPatterns" :scrollable="true"
								v-model:selection="pm.selectedPropertyPatterns"
								:resizable-columns="true" column-resize-mode="expand"
								selection-mode="multiple" :meta-key-selection="true" data-key="namePattern" striped-rows class="propertyPatterns-table table-sm">
								<p-column selection-mode="multiple" :frozen="true" class="col-check"></p-column>
								<p-column field="namePattern" header="<@spring.message code='propertyNamePattern' />">
								</p-column>
								<p-column field="valuePattern" header="<@spring.message code='propertyValuePattern' />">
								</p-column>
							</p-datatable>
						</div>
					</div>
				</div>
			</div>
			<div class="field grid">
				<label for="${pid}propertiesMatchMode" class="field-label col-12 mb-2"
					title="<@spring.message code='dtbsSourceGuard.propertiesMatchMode.desc' />">
					<@spring.message code='dtbsSourceGuard.propertiesMatchMode' />
				</label>
		        <div class="field-input col-12">
		        	<p-selectbutton id="${pid}propertiesMatchMode" v-model="fm.propertiesMatchMode" :options="pm.propertiesMatchModeOptions"
		        		option-label="name" option-value="value" class="input w-full">
		        	</p-selectbutton>
		        </div>
			</div>
			<div class="field grid">
				<label for="${pid}emptyPropertyPatternsForAll" class="field-label col-12 mb-2"
					title="<@spring.message code='dtbsSourceGuard.emptyPropertyPatternsForAll.desc' />">
					<@spring.message code='dtbsSourceGuard.emptyPropertyPatternsForAll' />
				</label>
		        <div class="field-input col-12">
		        	<p-selectbutton id="${pid}emptyPropertyPatternsForAll" v-model="fm.emptyPropertyPatternsForAll" :options="pm.booleanOptions"
		        		option-label="name" option-value="value" class="input w-full">
		        	</p-selectbutton>
		        </div>
			</div>
			<div class="field grid">
				<label for="${pid}permitted" class="field-label col-12 mb-2 md:col-3 md:mb-0"
					title="<@spring.message code='dtbsSourceGuard.permitted.desc' />">
					<@spring.message code='isPermit' />
				</label>
		        <div class="field-input col-12 md:col-9">
		        	<p-selectbutton id="${pid}permitted" v-model="fm.permitted" :options="pm.booleanOptions"
		        		option-label="name" option-value="value" class="input w-full">
		        	</p-selectbutton>
		        </div>
			</div>
			<div class="field grid">
				<label for="${pid}priority" class="field-label col-12 mb-2 md:col-3 md:mb-0"
					title="<@spring.message code='dtbsSourceGuard.priority.desc' />">
					<@spring.message code='priority' />
				</label>
		        <div class="field-input col-12 md:col-9">
		        	<p-inputtext id="${pid}priority" v-model="fm.priority" type="text" class="input w-full"
		        		name="priority" required maxlength="10">
		        	</p-inputtext>
		        </div>
			</div>
			<div class="field grid">
				<label for="${pid}enabled" class="field-label col-12 mb-2 md:col-3 md:mb-0">
					<@spring.message code='isEnable' />
				</label>
		        <div class="field-input col-12 md:col-9">
		        	<p-selectbutton id="${pid}enabled" v-model="fm.enabled" :options="pm.booleanOptions"
		        		option-label="name" option-value="value" class="input w-full">
		        	</p-selectbutton>
		        </div>
			</div>
		</div>
		<div class="page-form-foot flex-grow-0 flex justify-content-center gap-2 pt-2">
			<p-button type="submit" label="<@spring.message code='save' />"></p-button>
		</div>
	</form>
	<p-dialog :header="pm.propertyPatternForm.title" append-to="body"
		position="center" :dismissable-mask="true" :modal="true"
		v-model:visible="pm.propertyPatternForm.show" @show="onPropertyPatternFormPanelShow">
		<div class="page page-form">
			<form id="${pid}propertyPatternForm" class="flex flex-column">
				<div class="page-form-content flex-grow-1 px-2 py-1 panel-content-size-xs-minw overflow-y-auto">
					<div class="field grid">
						<label for="${pid}ppFormNamePattern" class="field-label col-12 mb-2"
							title="<@spring.message code='dtbsSourceGuard.propertyNamePattern.desc' />">
							<@spring.message code='propertyNamePattern' />
						</label>
						<div class="field-input col-12">
							<p-inputtext id="${pid}ppFormNamePattern" v-model="pm.propertyPatternForm.data.namePattern" type="text"
								class="input w-full" name="namePattern" required maxlength="100" autofocus>
							</p-inputtext>
						</div>
					</div>
					<div class="field grid">
						<label for="${pid}ppFormValuePattern" class="field-label col-12 mb-2"
							title="<@spring.message code='dtbsSourceGuard.propertyValuePattern.desc' />">
							<@spring.message code='propertyValuePattern' />
						</label>
						<div class="field-input col-12">
							<p-inputtext id="${pid}ppFormValuePattern" v-model="pm.propertyPatternForm.data.valuePattern" type="text"
								class="input w-full" name="valuePattern" required maxlength="100">
							</p-inputtext>
						</div>
					</div>
				</div>
				<div class="page-form-foot flex-grow-0 flex justify-content-center gap-2 pt-2">
					<p-button type="submit" label="<@spring.message code='confirm' />"></p-button>
				</div>
			</form>
		</div>
	</p-dialog>
</div>
<#include "../include/page_form.ftl">
<#include "../include/page_simple_form.ftl">
<#include "../include/page_boolean_options.ftl">
<script>
(function(po)
{
	po.submitUrl = "/dtbsSourceGuard/"+po.submitAction;
	
	po.showPropertyPatternFormPanel = function(action, data, submitHandler)
	{
		data = $.extend(true,
				{
					namePattern: "",
					valuePattern: "*"
				},
				po.vueRaw(data));
		
		var pm = po.vuePageModel();
		pm.propertyPatternForm.title = "<@spring.message code='dtbsSourceGuard.propertyPatterns' />" + " - " + action;
		pm.propertyPatternForm.data = data;
		pm.propertyPatternForm.submitHandler = submitHandler;
		pm.propertyPatternForm.show = true;
	};
	
	var formModel = $.unescapeHtmlForJson(<@writeJson var=formModel />);
	formModel.propertyPatterns = (formModel.propertyPatterns == null ? [] : formModel.propertyPatterns);
	
	po.setupForm(formModel, {},
	{
		rules :
		{
			priority : "integer"
		}
	});

	po.vuePageModel(
	{
		selectedPropertyPatterns: [],
		propertyPatternForm:
		{
			show: false,
			title: "",
			data: {},
			submitHandler: null
		},
		propertiesMatchModeOptions:
		[
			{name: "<@spring.message code='dtbsSourceGuard.propertiesMatchMode.ANY' />", value: "${DtbsSourceGuard.PROPERTIES_MATCH_MODE_ANY}"},
			{name: "<@spring.message code='dtbsSourceGuard.propertiesMatchMode.ALL' />", value: "${DtbsSourceGuard.PROPERTIES_MATCH_MODE_ALL}"}
		]
	});
	

	po.vueMethod(
	{
		onAddPropertyPattern: function(e)
		{
			po.showPropertyPatternFormPanel("<@spring.message code='add' />", {}, function(sp)
			{
				var fm = po.vueFormModel();
				fm.propertyPatterns.push(sp);
			});
		},
		
		onEditPropertyPattern: function(e)
		{
			var pm = po.vuePageModel();
			
			if(!pm.selectedPropertyPatterns || pm.selectedPropertyPatterns.length == 0)
				return;
			
			var fm = po.vueFormModel();
			var pp = pm.selectedPropertyPatterns[0];
			var ppIdx = $.inArrayById(fm.propertyPatterns, pp.namePattern, "namePattern");
			
			po.showPropertyPatternFormPanel("<@spring.message code='edit' />", pp, function(pp)
			{
				fm.propertyPatterns[ppIdx] = pp;
				pm.selectedPropertyPatterns = [];
			});
		},
		
		onDeletePropertyPattern: function(e)
		{
			var fm = po.vueFormModel();
			var pm = po.vuePageModel();
			var pps = $.wrapAsArray(po.vueRaw(pm.selectedPropertyPatterns));
			
			$.each(pps, function(idx, pp)
			{
				$.removeById(fm.propertyPatterns, pp.namePattern, "namePattern");
				pm.selectedPropertyPatterns = [];
			});
		},
		
		onPropertyPatternFormPanelShow: function()
		{
			var fm = po.vueFormModel();
			var pm = po.vuePageModel();
			
			var form = po.elementOfId("${pid}propertyPatternForm", document.body);
			po.setupSimpleForm(form, pm.propertyPatternForm.data, function()
			{
				var close = true;
				
				if(pm.propertyPatternForm.submitHandler)
				{
					var data = $.extend(true, {}, po.vueRaw(pm.propertyPatternForm.data));
					close = pm.propertyPatternForm.submitHandler(data);
				}
				
				pm.propertyPatternForm.show = (close === false);
			});
		}
	});
})
(${pid});
</script>
<#include "../include/page_vue_mount.ftl">
</body>
</html>
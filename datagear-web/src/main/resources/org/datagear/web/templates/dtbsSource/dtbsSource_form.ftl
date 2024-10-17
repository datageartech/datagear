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
	<@spring.message code='module.dtbsSource' />
	<#include "../include/html_request_action_suffix.ftl">
	<#include "../include/html_app_name_suffix.ftl">
</title>
</head>
<body class="p-card no-border">
<#include "../include/page_obj.ftl">
<div id="${pid}" class="page page-form horizontal page-form-dtbsSource">
	<form id="${pid}form" class="flex flex-column" :class="{readonly: pm.isReadonlyAction}">
		<div class="page-form-content flex-grow-1 px-2 py-1 overflow-y-auto">
			<div class="field grid">
				<label for="${pid}name" class="field-label col-12 mb-2 md:col-3 md:mb-0">
					<@spring.message code='name' />
				</label>
		        <div class="field-input col-12 md:col-9">
		        	<p-inputtext id="${pid}name" v-model="fm.title" type="text" class="input w-full"
		        		name="title" required maxlength="100" autofocus>
		        	</p-inputtext>
		        </div>
			</div>
			<div class="field grid">
				<label for="${pid}url" class="field-label col-12 mb-2 md:col-3 md:mb-0"
					title="<@spring.message code='dtbsSource.url.desc' />">
					<@spring.message code='url' />
				</label>
		        <div class="field-input col-12 md:col-9">
		        	<div class="p-inputgroup">
			        	<p-inputtext id="${pid}url" v-model="fm.url" type="text" class="input"
			        		name="url" required maxlength="2000" placeholder="jdbc:">
			        	</p-inputtext>
			        	<p-button type="button" label="<@spring.message code='help' />" @click="onBuildDtbsSourceUrl"
			        		class="p-button-secondary"
			        		v-if="!pm.isReadonlyAction">
			        	</p-button>
		        	</div>
		        </div>
			</div>
			<div class="field grid">
				<label for="${pid}user" class="field-label col-12 mb-2 md:col-3 md:mb-0"
					title="<@spring.message code='dtbsSource.username.desc' />">
					<@spring.message code='username' />
				</label>
		        <div class="field-input col-12 md:col-9">
		        	<p-inputtext id="${pid}user" v-model="fm.user" type="text" class="input w-full"
		        		name="user" maxlength="200" autocomplete="off">
		        	</p-inputtext>
		        </div>
			</div>
			<div class="field grid" v-if="!pm.isReadonlyAction">
				<label for="${pid}password" class="field-label col-12 mb-2 md:col-3 md:mb-0"
					title="<@spring.message code='dtbsSource.password.desc' />">
					<@spring.message code='password' />
				</label>
		        <div class="field-input col-12 md:col-9">
		        	<p-password id="${pid}password" v-model="fm.password" class="input w-full"
		        		input-class="w-full" toggle-mask :feedback="false"
		        		:pt="{input:{root:{name:'password',maxlength:'100',autocomplete:'new-password'}}}">
		        	</p-password>
		        	<div class="desc text-color-secondary">
		        		<small><@spring.message code='dtbsSource.password.input.desc' /></small>
		        	</div>
		        </div>
			</div>
			<div class="field grid">
				<label for="${pid}schemaName" class="field-label col-12 mb-2 md:col-3 md:mb-0"
					title="<@spring.message code='dtbsSource.schemaName.desc' />">
					<@spring.message code='schemaName' />
				</label>
		        <div class="field-input col-12 md:col-9">
		        	<p-inputtext id="${pid}schemaName" v-model="fm.schemaName" type="text" class="input w-full"
		        		name="schemaName" maxlength="100">
		        	</p-inputtext>
		        </div>
			</div>
			<div class="field grid">
				<label for="${pid}properties" class="field-label col-12 mb-2"
					title="<@spring.message code='dtbsSource.properties.desc' />">
					<@spring.message code='dtbsSource.properties' />
				</label>
				<div class="field-input col-12">
					<div class="p-component p-inputtext">
						<div class="flex flex-row pb-2" v-if="!pm.isReadonlyAction">
							<div class="h-opts flex-grow-1">
								<p-button type="button" label="<@spring.message code='add' />"
									@click="onAddProperty" class="p-button-secondary p-button-sm">
								</p-button>
								<p-button type="button" label="<@spring.message code='edit' />"
									@click="onEditProperty" class="p-button-secondary p-button-sm">
								</p-button>
								<p-button type="button" label="<@spring.message code='delete' />"
									@click="onDeleteProperty" class="p-button-danger p-button-sm">
								</p-button>
							</div>
						</div>
						<div id="${pid}properties" class="properties-wrapper input w-full overflow-auto">
							<p-datatable :value="fm.properties" :scrollable="true"
								v-model:selection="pm.selectedProperties"
								:resizable-columns="true" column-resize-mode="expand"
								selection-mode="multiple" :meta-key-selection="true" data-key="name" striped-rows class="properties-table table-sm">
								<p-column selection-mode="multiple" :frozen="true" class="col-check"></p-column>
								<p-column field="name" header="<@spring.message code='propertyName' />">
								</p-column>
								<p-column field="value" header="<@spring.message code='propertyValue' />">
								</p-column>
							</p-datatable>
						</div>
					</div>
				</div>
			</div>
			<div class="field grid">
				<label for="${pid}driverEntity" class="field-label col-12 mb-2 md:col-3 md:mb-0"
					title="<@spring.message code='dtbsSource.driverEntity.desc' />">
					<@spring.message code='module.driverEntity' />
				</label>
		        <div class="field-input col-12 md:col-9">
		        	<div class="p-inputgroup">
		        		<div class="p-input-icon-right flex-grow-1">
			        		<i class="pi pi-times cursor-pointer opacity-60" @click="onDeleteDriverEntity" v-if="!pm.isReadonlyAction">
			        		</i>
				        	<p-inputtext id="${pid}driverEntity" v-model="fm.driverEntity.displayName" type="text" class="input w-full h-full border-noround-right"
				        		readonly="readonly" name="driverEntity.displayName" maxlength="200">
				        	</p-inputtext>
			        	</div>
			        	<p-button type="button" label="<@spring.message code='select' />"
			        		@click="onSelectDriverEntity" class="p-button-secondary"
			        		v-if="!pm.isReadonlyAction">
			        	</p-button>
		        	</div>
		        </div>
			</div>
			<div class="field grid" v-if="pm.isReadonlyAction">
				<label for="${pid}createUser" class="field-label col-12 mb-2 md:col-3 md:mb-0">
					<@spring.message code='createUser' />
				</label>
		        <div class="field-input col-12 md:col-9">
		        	<p-inputtext id="${pid}createUser" v-model="fm.createUser.nameLabel" type="text" class="input w-full" readonly="readonly">
		        	</p-inputtext>
		        </div>
			</div>
			<div class="field grid" v-if="pm.isReadonlyAction">
				<label for="${pid}createTime" class="field-label col-12 mb-2 md:col-3 md:mb-0">
					<@spring.message code='createTime' />
				</label>
		        <div class="field-input col-12 md:col-9">
		        	<p-inputtext id="${pid}createTime" v-model="fm.createTime" type="text" class="input w-full" readonly="readonly">
		        	</p-inputtext>
		        </div>
			</div>
		</div>
		<div class="page-form-foot flex-grow-0 flex justify-content-center gap-2 pt-2">
			<p-button type="button" :label="pm.testActionBtnLabel" @click="onTest"
				:disabled="pm.inTestAction?true:false" class="p-button-secondary">
			</p-button>
			<p-button type="submit" label="<@spring.message code='save' />"></p-button>
		</div>
	</form>
	<p-dialog :header="pm.propertyForm.title" append-to="body"
		position="center" :dismissable-mask="true" :modal="true"
		v-model:visible="pm.propertyForm.show" @show="onPropertyFormPanelShow">
		<div class="page page-form">
			<form id="${pid}propertyForm" class="flex flex-column">
				<div class="page-form-content flex-grow-1 px-2 py-1 panel-content-size-xs-minw overflow-y-auto">
					<div class="field grid">
						<label for="${pid}propertyFormName" class="field-label col-12 mb-2">
							<@spring.message code='propertyName' />
						</label>
						<div class="field-input col-12">
							<p-inputtext id="${pid}propertyFormName" v-model="pm.propertyForm.data.name" type="text"
								class="input w-full" name="name" required maxlength="100" autofocus>
							</p-inputtext>
						</div>
					</div>
					<div class="field grid">
						<label for="${pid}propertyFormValue" class="field-label col-12 mb-2">
							<@spring.message code='propertyValue' />
						</label>
						<div class="field-input col-12">
							<p-inputtext id="${pid}propertyFormValue" v-model="pm.propertyForm.data.value" type="text"
								class="input w-full" name="value" maxlength="100">
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
<script>
(function(po)
{
	po.submitUrl = "/dtbsSource/"+po.submitAction;
	
	//XXX 没找到动vue动态启/禁用编辑表格的方法，暂时采用这个方式
	if(!po.isReadonlyAction)
	{
		var propertiesPtable = po.element("p-datatable", po.elementOfId("${pid}properties"));
		propertiesPtable.attr("edit-mode", "row");
	}
	
	po.inTestAction = function(boolVal)
	{
		var pm = po.vuePageModel();
		
		if(boolVal === undefined)
			return (pm.inTestAction == true);
		
		pm.inTestAction = boolVal;
		pm.testActionBtnLabel = (boolVal ? "<@spring.message code='testing' />" : "<@spring.message code='test' />");
	};
	
	po.beforeSubmitForm = function(action)
	{
		if(!po.inTestAction())
			return;
		
		action.url = "/dtbsSource/testConnection";
		action.options.defaultSuccessCallback = false;
	};

	po.showPropertyFormPanel = function(action, data, submitHandler)
	{
		data = $.extend(true,
				{
					name: "",
					value: ""
				},
				po.vueRaw(data));
		
		var pm = po.vuePageModel();
		pm.propertyForm.title = "<@spring.message code='dtbsSource.properties' />" + " - " + action;
		pm.propertyForm.data = data;
		pm.propertyForm.submitHandler = submitHandler;
		pm.propertyForm.show = true;
	};

	po.vuePageModel(
	{
		selectedProperties: [],
		inTestAction: false,
		testActionBtnLabel: "<@spring.message code='test' />",
		propertyForm:
		{
			show: false,
			title: "",
			data: {},
			submitHandler: null
		}
	});
	
	var formModel = $.unescapeHtmlForJson(<@writeJson var=formModel />);
	formModel.properties = (formModel.properties == null ? [] : formModel.properties);
	formModel.driverEntity = (formModel.driverEntity == null ? {} : formModel.driverEntity);
	
	po.setupForm(formModel,
	{
		complete: function()
		{
			if(po.inTestAction())
				po.inTestAction(false);
		}
	},
	{
		invalidHandler: function()
		{
			if(po.inTestAction())
				po.inTestAction(false);
		}
	});
	
	po.vueMethod(
	{
		onBuildDtbsSourceUrl: function()
		{
			var fm = po.vueFormModel();
			
			po.open("/dtbsSourceUrlBuilder/build",
			{
				data: {url: fm.url},
				contentType: $.CONTENT_TYPE_FORM,
				pageParam:
				{
					submitSuccess: function(url)
					{
						var fm = po.vueFormModel();
						fm.url = url;
					}
				}
			});
		},
		
		onAddProperty: function(e)
		{
			po.showPropertyFormPanel("<@spring.message code='add' />", {}, function(sp)
			{
				var fm = po.vueFormModel();
				fm.properties.push(sp);
			});
		},
		
		onEditProperty: function(e)
		{
			var pm = po.vuePageModel();
			
			if(!pm.selectedProperties || pm.selectedProperties.length == 0)
				return;
			
			var fm = po.vueFormModel();
			var sp = pm.selectedProperties[0];
			var spIdx = $.inArrayById(fm.properties, sp.name, "name");
			
			po.showPropertyFormPanel("<@spring.message code='edit' />", sp, function(sp)
			{
				fm.properties[spIdx] = sp;
				pm.selectedProperties = [];
			});
		},
		
		onDeleteProperty: function(e)
		{
			var fm = po.vueFormModel();
			var pm = po.vuePageModel();
			var sps = $.wrapAsArray(po.vueRaw(pm.selectedProperties));
			
			$.each(sps, function(idx, sp)
			{
				$.removeById(fm.properties, sp.name, "name");
				pm.selectedProperties = [];
			});
		},
		
		onPropertyFormPanelShow: function()
		{
			var fm = po.vueFormModel();
			var pm = po.vuePageModel();
			
			var form = po.elementOfId("${pid}propertyForm", document.body);
			po.setupSimpleForm(form, pm.propertyForm.data, function()
			{
				var close = true;
				
				if(pm.propertyForm.submitHandler)
				{
					var data = $.extend(true, {}, po.vueRaw(pm.propertyForm.data));
					close = pm.propertyForm.submitHandler(data);
				}
				
				pm.propertyForm.show = (close === false);
			});
		},
		
		onDeleteDriverEntity: function()
		{
			var fm = po.vueFormModel();
			fm.driverEntity = {};
		},
		
		onSelectDriverEntity: function()
		{
			po.handleOpenSelectAction("/driverEntity/select", function(driverEntity)
			{
				var fm = po.vueFormModel();
				fm.driverEntity = driverEntity;
			});
		},
		
		onTest: function(e)
		{
			po.inTestAction(true);
			po.form().submit();
		}
	});
})
(${pid});
</script>
<#include "../include/page_vue_mount.ftl">
</body>
</html>
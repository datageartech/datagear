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
<#include "../include/page_import.ftl">
<#include "../include/html_doctype.ftl">
<html>
<head>
<#include "../include/html_head.ftl">
<title>
	<@spring.message code='module.schema' />
	<#include "../include/html_request_action_suffix.ftl">
	<#include "../include/html_app_name_suffix.ftl">
</title>
</head>
<body class="p-card no-border">
<#include "../include/page_obj.ftl">
<div id="${pid}" class="page page-form horizontal page-form-schema">
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
					title="<@spring.message code='schema.url.desc' />">
					<@spring.message code='url' />
				</label>
		        <div class="field-input col-12 md:col-9">
		        	<div class="p-inputgroup">
			        	<p-inputtext id="${pid}url" v-model="fm.url" type="text" class="input"
			        		name="url" required maxlength="2000" placeholder="jdbc:">
			        	</p-inputtext>
			        	<p-button type="button" label="<@spring.message code='help' />" @click="onBuildSchemaUrl"
			        		class="p-button-secondary"
			        		v-if="!pm.isReadonlyAction">
			        	</p-button>
		        	</div>
		        </div>
			</div>
			<div class="field grid">
				<label for="${pid}user" class="field-label col-12 mb-2 md:col-3 md:mb-0"
					title="<@spring.message code='schema.username.desc' />">
					<@spring.message code='username' />
				</label>
		        <div class="field-input col-12 md:col-9">
		        	<p-inputtext id="${pid}user" v-model="fm.user" type="text" class="input w-full"
		        		name="user" maxlength="200" autocomplete="off">
		        	</p-inputtext>
		        </div>
			</div>
			<div class="field grid">
				<label for="${pid}password" class="field-label col-12 mb-2 md:col-3 md:mb-0"
					title="<@spring.message code='schema.password.desc' />">
					<@spring.message code='password' />
				</label>
		        <div class="field-input col-12 md:col-9">
		        	<p-password id="${pid}password" v-model="fm.password" class="input w-full"
		        		input-class="w-full" toggle-mask :feedback="false"
		        		name="password" maxlength="100" autocomplete="new-password">
		        	</p-password>
		        	<div class="desc text-color-secondary">
		        		<small><@spring.message code='schema.password.input.desc' /></small>
		        	</div>
		        </div>
			</div>
			<div class="field grid">
				<label for="${pid}properties" class="field-label col-12 mb-2"
					title="<@spring.message code='schema.properties.desc' />">
					<@spring.message code='schema.properties' />
				</label>
				<div class="field-input col-12">
					<div class="p-component p-inputtext">
						<div class="flex flex-row pb-2" v-if="!pm.isReadonlyAction">
							<div class="h-opts flex-grow-1">
								<p-button type="button" label="<@spring.message code='add' />"
									@click="onAddProperty" class="p-button-secondary p-button-sm">
								</p-button>
								<p-button type="button" label="<@spring.message code='delete' />"
									@click="onDeleteProperty" class="p-button-danger p-button-sm">
								</p-button>
							</div>
						</div>
						<div id="${pid}properties" class="properties-wrapper input w-full overflow-auto">
							<p-datatable :value="fm.properties" :scrollable="true"
								v-model:selection="pm.selectedProperties"
								v-model:editing-rows="pm.editingPropertyRows"  @row-edit-save="onPropertyRowEditSave"
								@row-edit-cancel="onPropertyRowEditCancel"
								:resizable-columns="true" column-resize-mode="expand"
								selection-mode="multiple" dataKey="name" striped-rows class="properties-table table-sm">
								<p-column selection-mode="multiple" :frozen="true" class="col-check"></p-column>
								<p-column :row-editor="true" :frozen="true"
									class="col-edit-btn" bodyStyle="text-align:center"
									v-if="!pm.isReadonlyAction">
								</p-column>
								<p-column field="name" header="<@spring.message code='propertyName' />">
									<template #editor="{ data, field }">
										<p-inputtext v-model="data[field]" @keydown.enter="onRowEditInputPreventDefault" maxlength="100" autofocus></p-inputtext>
									</template>
								</p-column>
								<p-column field="value" header="<@spring.message code='propertyValue' />">
									<template #editor="{ data, field }">
										<p-inputtext v-model="data[field]" @keydown.enter="onRowEditInputPreventDefault" maxlength="100"></p-inputtext>
									</template>
								</p-column>
							</p-datatable>
						</div>
					</div>
				</div>
			</div>
			<div class="field grid">
				<label for="${pid}driverEntity" class="field-label col-12 mb-2 md:col-3 md:mb-0"
					title="<@spring.message code='schema.driverEntity.desc' />">
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
</div>
<#include "../include/page_form.ftl">
<script>
(function(po)
{
	po.submitUrl = "/schema/"+po.submitAction;
	
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
		
		action.url = "/schema/testConnection";
		action.options.defaultSuccessCallback = false;
	};
	
	po.vuePageModel(
	{
		selectedProperties: [],
		editingPropertyRows: [],
		inTestAction: false,
		testActionBtnLabel: "<@spring.message code='test' />"
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
		onBuildSchemaUrl: function()
		{
			var fm = po.vueFormModel();
			
			po.open("/schemaUrlBuilder/build",
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
			var fm = po.vueFormModel();
			var pm = po.vuePageModel();
			
			fm.properties.push({ name: "", value: "" });
			pm.editingPropertyRows.push(fm.properties[fm.properties.length-1]);
		},
		
		onDeleteProperty: function(e)
		{
			var fm = po.vueFormModel();
			var pm = po.vuePageModel();
			var sps = $.wrapAsArray(po.vueRaw(pm.selectedProperties));
			
			$.each(sps, function(idx, sp)
			{
				$.removeById(fm.properties, sp.name, "name");
			});
		},
		
		onPropertyRowEditSave: function(e)
		{
			var fm = po.vueFormModel();
			var valid = true;
			
			if(!e.newData.name)
			{
				valid = false;
				$.tipInfo("<@spring.message code='propertyNameRequired' />");
			}
			
			if(!valid)
			{
				var pm = po.vuePageModel();
				pm.editingPropertyRows.push(e.data);
			}
			else
			{
				fm.properties[e.index] = e.newData;
			}
		},
		
		onPropertyRowEditCancel: function(e)
		{
			var fm = po.vueFormModel();
			var valid = true;
			
			if(!e.data.name)
			{
				valid = false;
				$.tipInfo("<@spring.message code='propertyNameRequired' />");
			}
			
			if(!valid)
			{
				var pm = po.vuePageModel();
				pm.editingPropertyRows.push(e.data);
			}
		},
		
		onRowEditInputPreventDefault: function(e)
		{
			e.preventDefault();
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
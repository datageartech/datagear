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
<#--
数据集参数、属性输入项

依赖：
dataSet_form_param_field_form.ftl

-->
<div class="field grid">
	<label for="${pid}params" class="field-label col-12 mb-2 flex-column align-items-start">
		<div>
			<@spring.message code='parameter' />
		</div>
		<div class="text-xs text-color-secondary">
			<@spring.message code='dataSet.params.desc' />
		</div>
	</label>
	<div class="field-input col-12">
		<div class="p-component p-inputtext">
			<div class="flex flex-row pb-2" v-if="!pm.isReadonlyAction">
				<div class="h-opts flex-grow-1">
					<p-button type="button" label="<@spring.message code='add' />"
						@click="onAddParam" class="p-button-secondary p-button-sm">
					</p-button>
					<p-button type="button" label="<@spring.message code='edit' />"
						@click="onEditParam" class="p-button-secondary p-button-sm">
					</p-button>
					<p-button type="button" label="<@spring.message code='moveUp' />"
						@click="onMoveUpParam" class="p-button-secondary p-button-sm">
					</p-button>
					<p-button type="button" label="<@spring.message code='moveDown' />"
						@click="onMoveDownParam" class="p-button-secondary p-button-sm">
					</p-button>
					<p-button type="button" label="<@spring.message code='delete' />"
						@click="onDeleteParam" class="p-button-danger p-button-sm">
					</p-button>
				</div>
			</div>
			<div id="${pid}params" class="params-wrapper input w-full overflow-auto">
				<p-datatable :value="fm.params" :scrollable="true"
					v-model:selection="pm.selectedParams"
					:resizable-columns="true" column-resize-mode="expand"
					selection-mode="multiple" :meta-key-selection="true" data-key="name" striped-rows class="params-table table-sm">
					<p-column selection-mode="multiple" :frozen="true" class="col-check"></p-column>
					<p-column field="name" header="<@spring.message code='name' />" class="col-name">
					</p-column>
					<p-column field="type" header="<@spring.message code='type' />" class="col-name">
						<template #body="{data}">
							{{formatParamType(data)}}
						</template>
					</p-column>
					<p-column field="required" header="<@spring.message code='isRequired' />" class="col-name">
						<template #body="{data}">
							{{formatParamRequired(data)}}
						</template>
					</p-column>
					<p-column field="desc" header="<@spring.message code='desc' />" class="col-name">
					</p-column>
					<p-column field="inputType" header="<@spring.message code='inputType' />" class="col-name">
						<template #body="{data}">
							{{formatParamInputType(data)}}
						</template>
					</p-column>
					<p-column field="inputPayload" header="<@spring.message code='inputConfig' />" class="col-name">
					</p-column>
				</p-datatable>
			</div>
		</div>
	</div>
</div>
<div class="field grid">
	<label for="${pid}fields" class="field-label col-12 mb-2"
		title="<@spring.message code='dataSet.fields.desc' />">
		<@spring.message code='field' />
	</label>
	<div class="field-input col-12">
		<div class="p-component p-inputtext">
			<div class="flex justify-content-between flex-row pb-2" v-if="!pm.isReadonlyAction">
				<div class="flex">
					<div class="h-opts">
						<p-button type="button" label="<@spring.message code='add' />"
							@click="onAddField" class="p-button-secondary p-button-sm">
						</p-button>
						<p-button type="button" label="<@spring.message code='edit' />"
							@click="onEditField" class="p-button-secondary p-button-sm">
						</p-button>
						<p-button type="button" label="<@spring.message code='moveUp' />"
							@click="onMoveUpField" class="p-button-secondary p-button-sm">
						</p-button>
						<p-button type="button" label="<@spring.message code='moveDown' />"
							@click="onMoveDownField" class="p-button-secondary p-button-sm">
						</p-button>
						<p-button type="button" label="<@spring.message code='delete' />"
							@click="onDeleteField" class="p-button-danger p-button-sm">
						</p-button>
					</div>
					<div class="flex align-items-center ml-5">
						<p-checkbox input-id="${pid}fieldAutoGen" v-model="pm.autoGenerateField" :binary="true"></p-checkbox>
						<label for="${pid}fieldAutoGen" class="ml-1 align-tip" title="<@spring.message code='dataSet.fields.autoGenerate.desc' />">
							<@spring.message code='autoGenerate' />
						</label>
					</div>
				</div>
				<div>
					<p-button type="button" label="<@spring.message code='dataFormat' />"
						@click="toggleDataSourceFormatPanel" aria:haspopup="true" aria-controls="${pid}dataSourceFormatPanel"
						class="p-button-secondary p-button-sm">
					</p-button>
					<p-overlaypanel ref="${pid}dataSourceFormatPanelEle" append-to="body" :show-close-icon="false" id="${pid}dataSourceFormatPanel">
						<div class="pb-2">
							<label class="text-lg font-bold" title="<@spring.message code='dataSet.dataSourceFormat.desc' />">
								<@spring.message code='dataFormat' />
							</label>
						</div>
						<div class="panel-content-size-xs overflow-auto p-2">
							<div class="field grid">
								<label for="${pid}dsfDate" class="field-label col-12 mb-2">
									<@spring.message code='dateFormat' />
								</label>
								<div class="field-input col-12">
									<p-inputtext id="${pid}dsfDate" v-model="fm.dataFormat.dateFormat" type="text" class="input w-full"
						        		name="dataFormat.dateFormat" maxlength="100">
						        	</p-inputtext>
								</div>
							</div>
							<div class="field grid">
								<label for="${pid}dsfTime" class="field-label col-12 mb-2">
									<@spring.message code='timeFormat' />
								</label>
								<div class="field-input col-12">
									<p-inputtext id="${pid}dsfTime" v-model="fm.dataFormat.timeFormat" type="text" class="input w-full"
						        		name="dataFormat.timeFormat" maxlength="100">
						        	</p-inputtext>
								</div>
							</div>
							<div class="field grid">
								<label for="${pid}dsfTimestamp" class="field-label col-12 mb-2">
									<@spring.message code='datetimeFormat' />
								</label>
								<div class="field-input col-12">
									<p-inputtext id="${pid}dsfTimestamp" v-model="fm.dataFormat.timestampFormat" type="text" class="input w-full"
						        		name="dataFormat.timestampFormat" maxlength="100">
						        	</p-inputtext>
								</div>
							</div>
							<div class="field grid">
								<label for="${pid}dsfNumber" class="field-label col-12 mb-2">
									<@spring.message code='numberFormat' />
								</label>
								<div class="field-input col-12">
									<p-inputtext id="${pid}dsfNumber" v-model="fm.dataFormat.numberFormat" type="text" class="input w-full"
						        		name="dataFormat.numberFormat" maxlength="100">
						        	</p-inputtext>
								</div>
							</div>
						</div>
					</p-overlaypanel>
				</div>
			</div>
			<div id="${pid}fields" class="fields-wrapper input w-full overflow-auto">
				<p-datatable :value="fm.fields" :scrollable="true"
					v-model:selection="pm.selectedFields"
					:resizable-columns="true" column-resize-mode="expand"
					selection-mode="multiple" :meta-key-selection="true" data-key="name" striped-rows class="fields-table table-sm">
					<p-column selection-mode="multiple" :frozen="true" class="col-check"></p-column>
					<p-column field="name" header="<@spring.message code='name' />" class="col-name">
					</p-column>
					<p-column field="type" header="<@spring.message code='type' />" class="col-name">
						<template #body="{data}">
							{{formatFieldType(data)}}
						</template>
					</p-column>
					<p-column field="label" header="<@spring.message code='displayName' />" class="col-name">
					</p-column>
					<p-column field="defaultValue" header="<@spring.message code='defaultValue' />" class="col-name">
					</p-column>
					<p-column field="evaluated" header="<@spring.message code='enableExpression' />" class="col-name">
						<template #body="{data}">
							{{formatFieldEvaludated(data)}}
						</template>
					</p-column>
					<p-column field="expression" header="<@spring.message code='expression' />" class="col-name">
					</p-column>
				</p-datatable>
			</div>
		</div>
	</div>
</div>
<div class="field grid">
	<label for="${pid}description" class="field-label col-12 mb-2">
		<@spring.message code='description' />
	</label>
	<div class="field-input col-12">
		<p-textarea id="${pid}description" v-model="fm.description" rows="5" class="input w-full"
       		name="description" maxlength="500">
       	</p-textarea>
	</div>
</div>
<script>
(function(po)
{
	po.vuePageModel(
	{
		selectedParams: [],
		selectedFields: [],
		autoGenerateField: true
	});
	
	po.vueRef("${pid}dataSourceFormatPanelEle", null);
	
	po.hasDuplicateNameNoCase = function(array, name, ignoreIdx)
	{
		name = (name || "").toUpperCase();
		
		for(var i=0; i<array.length; i++)
		{
			if(i != ignoreIdx)
			{
				var myName = (array[i].name || "").toUpperCase();
				if(name == myName)
				{
					return true;
				}
			}
		}
		
		return false;
	};
	
	po.vueMethod(
	{
		onAddParam: function(e)
		{
			po.showDataSetParamForm("<@spring.message code='add' />", {}, function(dsp)
			{
				var fm = po.vueFormModel();
				
				if(po.hasDuplicateNameNoCase(fm.params, dsp.name))
				{
					$.tipInfo("<@spring.message code='paramNameMustBeUniqueIgnoreCase' />");
					return false;
				}
				
				fm.params.push(dsp);
			});
		},
		onEditParam: function(e)
		{
			var pm = po.vuePageModel();
			
			if(!pm.selectedParams || pm.selectedParams.length == 0)
				return;
			
			var fm = po.vueFormModel();
			var dsp = pm.selectedParams[0];
			var dspIdx = $.inArrayById(fm.params, dsp.name, "name");
			
			po.showDataSetParamForm("<@spring.message code='edit' />", dsp, function(dsp)
			{
				if(po.hasDuplicateNameNoCase(fm.params, dsp.name, dspIdx))
				{
					$.tipInfo("<@spring.message code='paramNameMustBeUniqueIgnoreCase' />");
					return false;
				}
				
				fm.params[dspIdx] = dsp;
				pm.selectedParams = [];
			});
		},
		onAddField: function(e)
		{
			var fm = po.vueFormModel();
			
			po.showDataSetFieldForm("<@spring.message code='add' />", {}, function(dsp)
			{
				if(po.hasDuplicateNameNoCase(fm.fields, dsp.name))
				{
					$.tipInfo("<@spring.message code='fieldNameMustBeUniqueIgnoreCase' />");
					return false;
				}
				
				fm.fields.push(dsp);
			},
			fm.fields);
		},
		onEditField: function(e)
		{
			var pm = po.vuePageModel();
			
			if(!pm.selectedFields || pm.selectedFields.length == 0)
				return;
			
			var fm = po.vueFormModel();
			var dsp = pm.selectedFields[0];
			var dspIdx = $.inArrayById(fm.fields, dsp.name, "name");
			
			po.showDataSetFieldForm("<@spring.message code='edit' />", dsp, function(dsp)
			{
				if(po.hasDuplicateNameNoCase(fm.fields, dsp.name, dspIdx))
				{
					$.tipInfo("<@spring.message code='fieldNameMustBeUniqueIgnoreCase' />");
					return false;
				}
				
				fm.fields[dspIdx] = dsp;
				pm.selectedFields = [];
			},
			fm.fields);
		},
		onMoveUpParam: function(e)
		{
			var fm = po.vueFormModel();
			var pm = po.vuePageModel();
			var sps = $.wrapAsArray(po.vueRaw(pm.selectedParams));
			var spNames = $.propertyValue(sps, "name");
			$.moveUpById(fm.params, spNames, "name");
		},
		onMoveDownParam: function(e)
		{
			var fm = po.vueFormModel();
			var pm = po.vuePageModel();
			var sps = $.wrapAsArray(po.vueRaw(pm.selectedParams));
			var spNames = $.propertyValue(sps, "name");
			$.moveDownById(fm.params, spNames, "name");
		},
		onDeleteParam: function(e)
		{
			var fm = po.vueFormModel();
			var pm = po.vuePageModel();
			var sps = $.wrapAsArray(po.vueRaw(pm.selectedParams));
			
			$.each(sps, function(idx, sp)
			{
				$.removeById(fm.params, sp.name, "name");
				pm.selectedParams = [];
			});
		},
		onMoveUpField: function(e)
		{
			var fm = po.vueFormModel();
			var pm = po.vuePageModel();
			var sps = $.wrapAsArray(po.vueRaw(pm.selectedFields));
			var spNames = $.propertyValue(sps, "name");
			$.moveUpById(fm.fields, spNames, "name");
		},
		onMoveDownField: function(e)
		{
			var fm = po.vueFormModel();
			var pm = po.vuePageModel();
			var sps = $.wrapAsArray(po.vueRaw(pm.selectedFields));
			var spNames = $.propertyValue(sps, "name");
			$.moveDownById(fm.fields, spNames, "name");
		},
		onDeleteField: function(e)
		{
			var fm = po.vueFormModel();
			var pm = po.vuePageModel();
			var sps = $.wrapAsArray(po.vueRaw(pm.selectedFields));
			
			$.each(sps, function(idx, sp)
			{
				$.removeById(fm.fields, sp.name, "name");
				pm.selectedFields = [];
			});
		},
		toggleDataSourceFormatPanel: function(e)
		{
			po.vueUnref("${pid}dataSourceFormatPanelEle").toggle(e);
		}
	});
})
(${pid});
</script>
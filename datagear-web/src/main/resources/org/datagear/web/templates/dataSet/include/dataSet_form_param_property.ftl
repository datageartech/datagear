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
<#--
数据集参数、属性输入项

依赖：
dataSet_form_param_property_form.ftl

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
					selection-mode="multiple" dataKey="name" striped-rows class="params-table table-sm">
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
	<label for="${pid}properties" class="field-label col-12 mb-2"
		title="<@spring.message code='dataSet.properties.desc' />">
		<@spring.message code='property' />
	</label>
	<div class="field-input col-12">
		<div class="p-component p-inputtext">
			<div class="flex justify-content-between flex-row pb-2" v-if="!pm.isReadonlyAction">
				<div class="flex">
					<div class="h-opts">
						<p-button type="button" label="<@spring.message code='add' />"
							@click="onAddProperty" class="p-button-secondary p-button-sm">
						</p-button>
						<p-button type="button" label="<@spring.message code='edit' />"
							@click="onEditProperty" class="p-button-secondary p-button-sm">
						</p-button>
						<p-button type="button" label="<@spring.message code='moveUp' />"
							@click="onMoveUpProperty" class="p-button-secondary p-button-sm">
						</p-button>
						<p-button type="button" label="<@spring.message code='moveDown' />"
							@click="onMoveDownProperty" class="p-button-secondary p-button-sm">
						</p-button>
						<p-button type="button" label="<@spring.message code='delete' />"
							@click="onDeleteProperty" class="p-button-danger p-button-sm">
						</p-button>
					</div>
					<div class="flex align-items-center ml-5">
						<p-checkbox id="${pid}propertyAutoGen" v-model="pm.autoGenerateProperty" :binary="true"></p-checkbox>
						<label for="${pid}propertyAutoGen" class="ml-1 align-tip" title="<@spring.message code='dataSet.properties.autoGenerate.desc' />">
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
			<div id="${pid}properties" class="properties-wrapper input w-full overflow-auto">
				<p-datatable :value="fm.properties" :scrollable="true"
					v-model:selection="pm.selectedProperties"
					:resizable-columns="true" column-resize-mode="expand"
					selection-mode="multiple" dataKey="name" striped-rows class="properties-table table-sm">
					<p-column selection-mode="multiple" :frozen="true" class="col-check"></p-column>
					<p-column field="name" header="<@spring.message code='name' />" class="col-name">
					</p-column>
					<p-column field="type" header="<@spring.message code='type' />" class="col-name">
						<template #body="{data}">
							{{formatPropertyType(data)}}
						</template>
					</p-column>
					<p-column field="label" header="<@spring.message code='displayName' />" class="col-name">
					</p-column>
					<p-column field="defaultValue" header="<@spring.message code='defaultValue' />" class="col-name">
					</p-column>
					<p-column field="evaluated" header="<@spring.message code='enableExpression' />" class="col-name">
						<template #body="{data}">
							{{formatPropertyEvaludated(data)}}
						</template>
					</p-column>
					<p-column field="expression" header="<@spring.message code='expression' />" class="col-name">
					</p-column>
				</p-datatable>
			</div>
		</div>
	</div>
</div>
<script>
(function(po)
{
	po.vuePageModel(
	{
		selectedParams: [],
		selectedProperties: [],
		autoGenerateProperty: true
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
			});
		},
		onAddProperty: function(e)
		{
			var fm = po.vueFormModel();
			
			po.showDataSetPropertyForm("<@spring.message code='add' />", {}, function(dsp)
			{
				if(po.hasDuplicateNameNoCase(fm.properties, dsp.name))
				{
					$.tipInfo("<@spring.message code='propertyNameMustBeUniqueIgnoreCase' />");
					return false;
				}
				
				fm.properties.push(dsp);
			},
			fm.properties);
		},
		onEditProperty: function(e)
		{
			var pm = po.vuePageModel();
			
			if(!pm.selectedProperties || pm.selectedProperties.length == 0)
				return;
			
			var fm = po.vueFormModel();
			var dsp = pm.selectedProperties[0];
			var dspIdx = $.inArrayById(fm.properties, dsp.name, "name");
			
			po.showDataSetPropertyForm("<@spring.message code='edit' />", dsp, function(dsp)
			{
				if(po.hasDuplicateNameNoCase(fm.properties, dsp.name, dspIdx))
				{
					$.tipInfo("<@spring.message code='propertyNameMustBeUniqueIgnoreCase' />");
					return false;
				}
				
				fm.properties[dspIdx] = dsp;
			},
			fm.properties);
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
			});
		},
		onMoveUpProperty: function(e)
		{
			var fm = po.vueFormModel();
			var pm = po.vuePageModel();
			var sps = $.wrapAsArray(po.vueRaw(pm.selectedProperties));
			var spNames = $.propertyValue(sps, "name");
			$.moveUpById(fm.properties, spNames, "name");
		},
		onMoveDownProperty: function(e)
		{
			var fm = po.vueFormModel();
			var pm = po.vuePageModel();
			var sps = $.wrapAsArray(po.vueRaw(pm.selectedProperties));
			var spNames = $.propertyValue(sps, "name");
			$.moveDownById(fm.properties, spNames, "name");
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
		toggleDataSourceFormatPanel: function(e)
		{
			po.vueUnref("${pid}dataSourceFormatPanelEle").toggle(e);
		}
	});
})
(${pid});
</script>
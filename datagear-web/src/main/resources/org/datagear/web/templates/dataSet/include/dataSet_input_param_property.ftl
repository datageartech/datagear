<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#--
数据集参数、属性输入项

依赖：
page_boolean_options.ftl

-->
<#assign ParamDataType=statics['org.datagear.analysis.DataSetParam$DataType']>
<#assign ParamInputType=statics['org.datagear.analysis.DataSetParam$InputType']>
<#assign PropertyDataType=statics['org.datagear.analysis.DataSetProperty$DataType']>
<div class="field grid">
	<label for="${pid}parameters" class="field-label col-12 mb-2 md:col-3 md:mb-0">
		<@spring.message code='parameter' />
	</label>
	<div class="field-input col-12 md:col-9">
		<div class="p-component p-inputtext">
			<div class="flex flex-row pb-1">
				<div class="h-opts flex-grow-1">
					<p-button type="button" label="<@spring.message code='add' />" class="p-button-secondary p-button-sm"></p-button>
					<p-button type="button" label="<@spring.message code='moveUp' />" class="p-button-secondary p-button-sm"></p-button>
					<p-button type="button" label="<@spring.message code='moveDown' />" class="p-button-secondary p-button-sm"></p-button>
					<p-button type="button" label="<@spring.message code='delete' />" class="p-button-danger p-button-sm"></p-button>
				</div>
			</div>
			<div id="${pid}parameters" class="params-wrapper input w-full overflow-auto">
				<p-datatable :value="pm.params" :scrollable="true" scroll-height="flex"
					v-model:selection="pm.selectedParams"
					edit-mode="row" v-model:editing-rows="editingParamRows"  @row-edit-save="onParamRowEditSave"
					selection-mode="multiple" dataKey="name" striped-rows class="table-sm">
					<p-column selection-mode="multiple" :frozen="true" class="col-check"></p-column>
					<p-column :row-editor="true" :frozen="true" style="max-width:6rem;min-width:6rem" bodyStyle="text-align:center"></p-column>
					<p-column field="name" header="<@spring.message code='name' />" class="col-name">
						<template #editor="{ data, field }">
							<p-inputtext v-model="data[field]" @keydown.enter="onRowEditInputPreventDefault" maxlength="100" autofocus></p-inputtext>
						</template>
					</p-column>
					<p-column field="type" header="<@spring.message code='type' />" class="col-name">
						<template #body="{data}">
							{{formatParamType(data)}}
						</template>
						<template #editor="{ data, field }">
							<p-dropdown v-model="data[field]" :options="dataSetParamDataTypeOptions" option-label="name" option-value="value"
								@click="onRowEditInputStopPropagation">
							</p-dropdown>
						</template>
					</p-column>
					<p-column field="required" header="<@spring.message code='isRequired' />" class="col-name">
						<template #body="{data}">
							{{formatParamRequired(data)}}
						</template>
						<template #editor="{ data, field }">
							<p-dropdown v-model="data[field]" :options="booleanOptions" option-label="name" option-value="value"
								@click="onRowEditInputStopPropagation">
							</p-dropdown>
						</template>
					</p-column>
					<p-column field="desc" header="<@spring.message code='desc' />" class="col-name">
						<template #editor="{ data, field }">
							<p-inputtext v-model="data[field]" @keydown.enter="onRowEditInputPreventDefault" maxlength="100"></p-inputtext>
						</template>
					</p-column>
					<p-column field="inputType" header="<@spring.message code='inputType' />" class="col-name">
						<template #body="{data}">
							{{formatParamInputType(data)}}
						</template>
						<template #editor="{ data, field }">
							<p-dropdown v-model="data[field]" :options="dataSetParamInputTypeOptions" option-label="name" option-value="value"
								@click="onRowEditInputStopPropagation">
							</p-dropdown>
						</template>
					</p-column>
					<p-column field="inputPayload" header="<@spring.message code='inputConfig' />" class="col-name">
						<template #editor="{ data, field }">
							<p-inputtext v-model="data[field]" @keydown.enter="onRowEditInputPreventDefault" maxlength="1000"></p-inputtext>
						</template>
					</p-column>
				</p-datatable>
			</div>
		</div>
	</div>
</div>
<div class="field grid">
	<label for="${pid}properties" class="field-label col-12 mb-2 md:col-3 md:mb-0">
		<@spring.message code='property' />
	</label>
	<div class="field-input col-12 md:col-9">
		<div class="p-component p-inputtext">
			<div class="flex flex-row pb-1">
				<div class="h-opts flex-grow-1">
					<p-button type="button" label="<@spring.message code='add' />" class="p-button-secondary p-button-sm"></p-button>
					<p-button type="button" label="<@spring.message code='moveUp' />" class="p-button-secondary p-button-sm"></p-button>
					<p-button type="button" label="<@spring.message code='moveDown' />" class="p-button-secondary p-button-sm"></p-button>
					<p-button type="button" label="<@spring.message code='delete' />" class="p-button-danger p-button-sm"></p-button>
				</div>
				<div class="flex-grow-1 flex justify-content-end">
					<p-button type="button" label="<@spring.message code='set' />" class="p-button-secondary p-button-sm"></p-button>
				</div>
			</div>
			<div id="${pid}properties" class="properties-wrapper input w-full overflow-auto">
				<p-datatable :value="pm.properties" :scrollable="true" scroll-height="flex"
					v-model:selection="pm.selectedProperties"
					edit-mode="row" v-model:editing-rows="editingPropertyRows"  @row-edit-save="onPropertyRowEditSave"
					selection-mode="multiple" dataKey="name" striped-rows class="table-sm">
					<p-column selection-mode="multiple" :frozen="true" class="col-check"></p-column>
					<p-column :row-editor="true" :frozen="true" style="max-width:6rem;min-width:6rem" bodyStyle="text-align:center"></p-column>
					<p-column field="name" header="<@spring.message code='name' />" class="col-name">
						<template #editor="{ data, field }">
							<p-inputtext v-model="data[field]" @keydown.enter="onRowEditInputPreventDefault" maxlength="100" autofocus></p-inputtext>
						</template>
					</p-column>
					<p-column field="type" header="<@spring.message code='type' />" class="col-name">
						<template #body="{data}">
							{{formatPropertyType(data)}}
						</template>
						<template #editor="{ data, field }">
							<p-dropdown v-model="data[field]" :options="dataSetPropertyTypeOptions" option-label="name" option-value="value"
								@click="onRowEditInputStopPropagation">
							</p-dropdown>
						</template>
					</p-column>
					<p-column field="displayName" header="<@spring.message code='displayName' />" class="col-name">
						<template #editor="{ data, field }">
							<p-inputtext v-model="data[field]" @keydown.enter="onRowEditInputPreventDefault" maxlength="100"></p-inputtext>
						</template>
					</p-column>
					<p-column field="defaultValue" header="<@spring.message code='defaultValue' />" class="col-name">
						<template #editor="{ data, field }">
							<p-inputtext v-model="data[field]" @keydown.enter="onRowEditInputPreventDefault" maxlength="500"></p-inputtext>
						</template>
					</p-column>
				</p-datatable>
			</div>
		</div>
	</div>
</div>
<script>
(function(po)
{
	po.vueRef("editingParamRows", []);
	po.vueRef("editingPropertyRows", []);
	
	po.vueRef("dataSetParamDataTypeOptions",
	[
		{name: "<@spring.message code='dataSetParam.DataType.STRING' />", value: "${ParamDataType.STRING}"},
		{name: "<@spring.message code='dataSetParam.DataType.NUMBER' />", value: "${ParamDataType.NUMBER}"},
		{name: "<@spring.message code='dataSetParam.DataType.BOOLEAN' />", value: "${ParamDataType.BOOLEAN}"}
	]);

	po.vueRef("dataSetParamInputTypeOptions",
	[
		{name: "<@spring.message code='dataSetParam.InputType.TEXT' />", value: "${ParamInputType.TEXT}"},
		{name: "<@spring.message code='dataSetParam.InputType.SELECT' />", value: "${ParamInputType.SELECT}"},
		{name: "<@spring.message code='dataSetParam.InputType.DATE' />", value: "${ParamInputType.DATE}"},
		{name: "<@spring.message code='dataSetParam.InputType.TIME' />", value: "${ParamInputType.TIME}"},
		{name: "<@spring.message code='dataSetParam.InputType.DATETIME' />", value: "${ParamInputType.DATETIME}"},
		{name: "<@spring.message code='dataSetParam.InputType.RADIO' />", value: "${ParamInputType.RADIO}"},
		{name: "<@spring.message code='dataSetParam.InputType.CHECKBOX' />", value: "${ParamInputType.CHECKBOX}"},
		{name: "<@spring.message code='dataSetParam.InputType.TEXTAREA' />", value: "${ParamInputType.TEXTAREA}"}
	]);

	po.vueRef("dataSetPropertyTypeOptions",
	[
		{name: "<@spring.message code='dataSetProperty.DataType.STRING' />", value: "${PropertyDataType.STRING}"},
		{name: "<@spring.message code='dataSetProperty.DataType.NUMBER' />", value: "${PropertyDataType.NUMBER}"},
		{name: "<@spring.message code='dataSetProperty.DataType.INTEGER' />", value: "${PropertyDataType.INTEGER}"},
		{name: "<@spring.message code='dataSetProperty.DataType.DECIMAL' />", value: "${PropertyDataType.DECIMAL}"},
		{name: "<@spring.message code='dataSetProperty.DataType.DATE' />", value: "${PropertyDataType.DATE}"},
		{name: "<@spring.message code='dataSetProperty.DataType.TIME' />", value: "${PropertyDataType.TIME}"},
		{name: "<@spring.message code='dataSetProperty.DataType.TIMESTAMP' />", value: "${PropertyDataType.TIMESTAMP}"},
		{name: "<@spring.message code='dataSetProperty.DataType.BOOLEAN' />", value: "${PropertyDataType.BOOLEAN}"},
		{name: "<@spring.message code='dataSetProperty.DataType.UNKNOWN' />", value: "${PropertyDataType.UNKNOWN}"}
	]);
	
	po.vueMethod(
	{
		formatParamType: function(data)
		{
			var opts = po.vueUnref("dataSetParamDataTypeOptions");
			return $.findNameByValue(opts, data.type);
		},
		formatParamInputType: function(data)
		{
			var opts = po.vueUnref("dataSetParamInputTypeOptions");
			return $.findNameByValue(opts, data.inputType);
		},
		formatParamRequired: function(data)
		{
			return po.formatBooleanValue(data.required);
		},
		formatPropertyType: function(data)
		{
			var opts = po.vueUnref("dataSetPropertyTypeOptions");
			return $.findNameByValue(opts, data.type);
		},
		onPropertyRowEditSave: function(e)
		{
			var pm = po.vuePageModel();
			pm.properties[e.index] = e.newData;
		},
		onParamRowEditSave: function(e)
		{
			var pm = po.vuePageModel();
			pm.params[e.index] = e.newData;
		},
		onRowEditInputPreventDefault: function(e)
		{
			e.preventDefault();
		},
		onRowEditInputStopPropagation: function(e)
		{
			e.stopPropagation();
		}
	});
})
(${pid});
</script>

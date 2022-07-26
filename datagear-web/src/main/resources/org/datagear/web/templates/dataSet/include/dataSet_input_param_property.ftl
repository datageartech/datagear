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
	<label for="${pid}params" class="field-label col-12 mb-2 md:col-3 md:mb-0"
		title="<@spring.message code='dataSet.params.desc' />">
		<@spring.message code='parameter' />
	</label>
	<div class="field-input col-12 md:col-9">
		<div class="p-component p-inputtext">
			<div class="flex flex-row pb-2" v-if="!isReadonlyAction">
				<div class="h-opts flex-grow-1">
					<p-button type="button" label="<@spring.message code='add' />"
						@click="onAddParam" class="p-button-secondary p-button-sm">
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
				<p-datatable :value="pm.params" :scrollable="true" scroll-height="flex"
					v-model:selection="tm.selectedParams"
					v-model:editing-rows="tm.editingParamRows"  @row-edit-save="onParamRowEditSave"
					@row-edit-cancel="onParamRowEditCancel"
					selection-mode="multiple" dataKey="name" striped-rows class="table-sm">
					<p-column selection-mode="multiple" :frozen="true" class="col-check"></p-column>
					<p-column :row-editor="true" :frozen="true"
						style="max-width:6rem;min-width:6rem" bodyStyle="text-align:center"
						v-if="!isReadonlyAction">
					</p-column>
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
							<!-- 行内编辑的textarea点击和按键事件会莫名触发行保存事件，所以这里添加特殊处理函数屏蔽 -->
							<p-textarea v-model="data[field]" @click="onRowEditInputStopPropagation"
								@keydown="onRowEditInputStopPropagation" @keydown="onRowEditInputStopPropagation"
								rows="2" maxlength="2000">
							</p-textarea>
						</template>
					</p-column>
				</p-datatable>
			</div>
		</div>
	</div>
</div>
<div class="field grid">
	<label for="${pid}properties" class="field-label col-12 mb-2 md:col-3 md:mb-0"
		title="<@spring.message code='dataSet.properties.desc' />">
		<@spring.message code='property' />
	</label>
	<div class="field-input col-12 md:col-9">
		<div class="p-component p-inputtext">
			<div class="flex flex-row pb-2" v-if="!isReadonlyAction">
				<div class="h-opts flex-grow-1">
					<p-button type="button" label="<@spring.message code='add' />"
						@click="onAddProperty" class="p-button-secondary p-button-sm">
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
				<div class="flex-grow-1 flex justify-content-end">
					<p-button type="button" label="<@spring.message code='set' />"
						@click="toggleDataSourceFormatPanel" aria:haspopup="true" aria-controls="${pid}dataSourceFormatPanel"
						class="p-button-secondary p-button-sm">
					</p-button>
					<p-overlaypanel ref="dataSourceFormatPanelEle" :show-close-icon="true" id="${pid}dataSourceFormatPanel">
						<div class="field grid mb-0">
							<label class="field-label col-12 text-lg font-bold" title="<@spring.message code='dataSet.dataSourceFormat.desc' />">
								<@spring.message code='dataSourceFormat' />
							</label>
						</div>
						<div class="field grid">
							<label for="${pid}dsfDate" class="field-label col-12 mb-2">
								<@spring.message code='dateFormat' />
							</label>
							<div class="field-input col-12">
								<p-inputtext id="${pid}dsfDate" v-model="pm.dataFormat.dateFormat" type="text" class="input w-full"
					        		name="dataFormat.dateFormat" maxlength="100">
					        	</p-inputtext>
							</div>
						</div>
						<div class="field grid">
							<label for="${pid}dsfTime" class="field-label col-12 mb-2">
								<@spring.message code='timeFormat' />
							</label>
							<div class="field-input col-12">
								<p-inputtext id="${pid}dsfTime" v-model="pm.dataFormat.timeFormat" type="text" class="input w-full"
					        		name="dataFormat.timeFormat" maxlength="100">
					        	</p-inputtext>
							</div>
						</div>
						<div class="field grid">
							<label for="${pid}dsfTimestamp" class="field-label col-12 mb-2">
								<@spring.message code='datetimeFormat' />
							</label>
							<div class="field-input col-12">
								<p-inputtext id="${pid}dsfTimestamp" v-model="pm.dataFormat.timestampFormat" type="text" class="input w-full"
					        		name="dataFormat.timestampFormat" maxlength="100">
					        	</p-inputtext>
							</div>
						</div>
						<div class="field grid">
							<label for="${pid}dsfNumber" class="field-label col-12 mb-2">
								<@spring.message code='numberFormat' />
							</label>
							<div class="field-input col-12">
								<p-inputtext id="${pid}dsfNumber" v-model="pm.dataFormat.numberFormat" type="text" class="input w-full"
					        		name="dataFormat.numberFormat" maxlength="100">
					        	</p-inputtext>
							</div>
						</div>
					</p-overlaypanel>
				</div>
			</div>
			<div id="${pid}properties" class="properties-wrapper input w-full overflow-auto">
				<p-datatable :value="pm.properties" :scrollable="true" scroll-height="flex"
					v-model:selection="tm.selectedProperties"
					v-model:editing-rows="tm.editingPropertyRows" @row-edit-save="onPropertyRowEditSave"
					@row-edit-cancel="onPropertyRowEditCancel"
					selection-mode="multiple" dataKey="name" striped-rows class="table-sm">
					<p-column selection-mode="multiple" :frozen="true" class="col-check"></p-column>
					<p-column :row-editor="true" :frozen="true"
						style="max-width:6rem;min-width:6rem" bodyStyle="text-align:center"
						v-if="!isReadonlyAction">
					</p-column>
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
	//XXX 没找到动vue动态启/禁用编辑表格的方法，暂时采用这个方式
	if(!po.isReadonlyAction)
	{
		var paramsPtable = po.element("p-datatable", po.elementOfId("${pid}params"));
		paramsPtable.attr("edit-mode", "row");
		
		var propertiesPtable = po.element("p-datatable", po.elementOfId("${pid}properties"));
		propertiesPtable.attr("edit-mode", "row");
	}
	
	po.vueTmpModel(
	{
		selectedParams: [],
		selectedProperties: [],
		editingParamRows: [],
		editingPropertyRows: []
	});
	
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
	
	po.vueRef("dataSourceFormatPanelEle", null);
	
	po.hasDuplicateName = function(array, name, ignoreIdx)
	{
		var nameIdx = $.inArrayById(array, name, "name");
		return (nameIdx >= 0 && nameIdx != ignoreIdx ? true : false);
	};
	
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
		onParamRowEditSave: function(e)
		{
			var pm = po.vuePageModel();
			var valid = true;
			
			if(!e.newData.name)
			{
				valid = false;
				$.tipInfo("<@spring.message code='paramNameRequired' />");
			}
			else if(po.hasDuplicateName(pm.params, e.newData.name, e.index))
			{
				valid = false;
				$.tipInfo("<@spring.message code='paramNameMustBeUnique' />");
			}
			
			if(!valid)
			{
				var tm = po.vueTmpModel();
				tm.editingParamRows.push(e.data);
			}
			else
			{
				pm.params[e.index] = e.newData;
			}
		},
		onParamRowEditCancel: function(e)
		{
			var pm = po.vuePageModel();
			var valid = true;
			
			if(!e.data.name)
			{
				valid = false;
				$.tipInfo("<@spring.message code='paramNameRequired' />");
			}
			else if(po.hasDuplicateName(pm.params, e.data.name, e.index))
			{
				valid = false;
				$.tipInfo("<@spring.message code='paramNameMustBeUnique' />");
			}
			
			if(!valid)
			{
				var tm = po.vueTmpModel();
				tm.editingParamRows.push(e.data);
			}
		},
		onPropertyRowEditSave: function(e)
		{
			var pm = po.vuePageModel();
			var valid = true;
			
			if(!e.newData.name)
			{
				valid = false;
				$.tipInfo("<@spring.message code='propertyNameRequired' />");
			}
			else if(po.hasDuplicateName(pm.properties, e.newData.name, e.index))
			{
				valid = false;
				$.tipInfo("<@spring.message code='propertyNameMustBeUnique' />");
			}
			
			if(!valid)
			{
				var tm = po.vueTmpModel();
				tm.editingPropertyRows.push(e.data);
			}
			else
			{
				pm.properties[e.index] = e.newData;
			}
		},
		onPropertyRowEditCancel: function(e)
		{
			var pm = po.vuePageModel();
			var valid = true;
			
			if(!e.data.name)
			{
				valid = false;
				$.tipInfo("<@spring.message code='propertyNameRequired' />");
			}
			else if(po.hasDuplicateName(pm.properties, e.data.name, e.index))
			{
				valid = false;
				$.tipInfo("<@spring.message code='propertyNameMustBeUnique' />");
			}
			
			if(!valid)
			{
				var tm = po.vueTmpModel();
				tm.editingPropertyRows.push(e.data);
			}
		},
		onRowEditInputPreventDefault: function(e)
		{
			e.preventDefault();
		},
		onRowEditInputStopPropagation: function(e)
		{
			e.stopPropagation();
		},
		onAddParam: function(e)
		{
			var pm = po.vuePageModel();
			var tm = po.vueTmpModel();
			
			pm.params.push({ name: "", type: "${ParamDataType.STRING}", required: true, inputType: "${ParamInputType.TEXT}" });
			tm.editingParamRows.push(pm.params[pm.params.length-1]);
		},
		onMoveUpParam: function(e)
		{
			var pm = po.vuePageModel();
			var tm = po.vueTmpModel();
			var sps = $.wrapAsArray(po.vueRaw(tm.selectedParams));
			var spNames = $.propertyValue(sps, "name");
			$.moveUpById(pm.params, spNames, "name");
		},
		onMoveDownParam: function(e)
		{
			var pm = po.vuePageModel();
			var tm = po.vueTmpModel();
			var sps = $.wrapAsArray(po.vueRaw(tm.selectedParams));
			var spNames = $.propertyValue(sps, "name");
			$.moveDownById(pm.params, spNames, "name");
		},
		onDeleteParam: function(e)
		{
			var pm = po.vuePageModel();
			var tm = po.vueTmpModel();
			var sps = $.wrapAsArray(po.vueRaw(tm.selectedParams));
			
			$.each(sps, function(idx, sp)
			{
				$.removeById(pm.params, sp.name, "name");
			});
		},
		onAddProperty: function(e)
		{
			var pm = po.vuePageModel();
			var tm = po.vueTmpModel();
			
			pm.properties.push({ name: "", type: "${PropertyDataType.STRING}" });
			tm.editingPropertyRows.push(pm.properties[pm.properties.length-1]);
		},
		onMoveUpProperty: function(e)
		{
			var pm = po.vuePageModel();
			var tm = po.vueTmpModel();
			var sps = $.wrapAsArray(po.vueRaw(tm.selectedProperties));
			var spNames = $.propertyValue(sps, "name");
			$.moveUpById(pm.properties, spNames, "name");
		},
		onMoveDownProperty: function(e)
		{
			var pm = po.vuePageModel();
			var tm = po.vueTmpModel();
			var sps = $.wrapAsArray(po.vueRaw(tm.selectedProperties));
			var spNames = $.propertyValue(sps, "name");
			$.moveDownById(pm.properties, spNames, "name");
		},
		onDeleteProperty: function(e)
		{
			var pm = po.vuePageModel();
			var tm = po.vueTmpModel();
			var sps = $.wrapAsArray(po.vueRaw(tm.selectedProperties));
			
			$.each(sps, function(idx, sp)
			{
				$.removeById(pm.properties, sp.name, "name");
			});
		},
		toggleDataSourceFormatPanel: function(e)
		{
			po.vueUnref("dataSourceFormatPanelEle").toggle(e);
		}
	});
})
(${pid});
</script>
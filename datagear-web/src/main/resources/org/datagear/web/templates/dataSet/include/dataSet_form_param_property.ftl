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
	<label for="${pid}params" class="field-label col-12 mb-2"
		title="<@spring.message code='dataSet.params.desc' />">
		<@spring.message code='parameter' />
	</label>
	<div class="field-input col-12">
		<div class="p-component p-inputtext">
			<div class="flex flex-row pb-2" v-if="!pm.isReadonlyAction">
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
				<p-datatable :value="fm.params" :scrollable="true"
					v-model:selection="pm.selectedParams"
					v-model:editing-rows="pm.editingParamRows"  @row-edit-save="onParamRowEditSave"
					@row-edit-cancel="onParamRowEditCancel"
					selection-mode="multiple" dataKey="name" striped-rows class="params-table table-sm">
					<p-column selection-mode="multiple" :frozen="true" class="col-check"></p-column>
					<p-column :row-editor="true" :frozen="true"
						style="max-width:6rem;min-width:6rem" bodyStyle="text-align:center"
						v-if="!pm.isReadonlyAction">
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
							<p-dropdown v-model="data[field]" :options="pm.dataSetParamDataTypeOptions" option-label="name" option-value="value"
								@click="onRowEditInputStopPropagation">
							</p-dropdown>
						</template>
					</p-column>
					<p-column field="required" header="<@spring.message code='isRequired' />" class="col-name">
						<template #body="{data}">
							{{formatParamRequired(data)}}
						</template>
						<template #editor="{ data, field }">
							<p-dropdown v-model="data[field]" :options="pm.booleanOptions" option-label="name" option-value="value"
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
							<p-dropdown v-model="data[field]" :options="pm.dataSetParamInputTypeOptions" option-label="name" option-value="value"
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
	<label for="${pid}properties" class="field-label col-12 mb-2"
		title="<@spring.message code='dataSet.properties.desc' />">
		<@spring.message code='property' />
	</label>
	<div class="field-input col-12">
		<div class="p-component p-inputtext">
			<div class="flex flex-row pb-2" v-if="!pm.isReadonlyAction">
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
					v-model:editing-rows="pm.editingPropertyRows" @row-edit-save="onPropertyRowEditSave"
					@row-edit-cancel="onPropertyRowEditCancel"
					selection-mode="multiple" dataKey="name" striped-rows class="properties-table table-sm">
					<p-column selection-mode="multiple" :frozen="true" class="col-check"></p-column>
					<p-column :row-editor="true" :frozen="true"
						style="max-width:6rem;min-width:6rem" bodyStyle="text-align:center"
						v-if="!pm.isReadonlyAction">
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
							<p-dropdown v-model="data[field]" :options="pm.dataSetPropertyTypeOptions" option-label="name" option-value="value"
								@click="onRowEditInputStopPropagation">
							</p-dropdown>
						</template>
					</p-column>
					<p-column field="label" header="<@spring.message code='displayName' />" class="col-name">
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
	
	po.vuePageModel(
	{
		selectedParams: [],
		selectedProperties: [],
		editingParamRows: [],
		editingPropertyRows: [],
		dataSetParamDataTypeOptions:
		[
			{name: "<@spring.message code='dataSetParam.DataType.STRING' />", value: "${ParamDataType.STRING}"},
			{name: "<@spring.message code='dataSetParam.DataType.NUMBER' />", value: "${ParamDataType.NUMBER}"},
			{name: "<@spring.message code='dataSetParam.DataType.BOOLEAN' />", value: "${ParamDataType.BOOLEAN}"}
		],
		dataSetParamInputTypeOptions:
		[
			{name: "<@spring.message code='dataSetParam.InputType.TEXT' />", value: "${ParamInputType.TEXT}"},
			{name: "<@spring.message code='dataSetParam.InputType.SELECT' />", value: "${ParamInputType.SELECT}"},
			{name: "<@spring.message code='dataSetParam.InputType.DATE' />", value: "${ParamInputType.DATE}"},
			{name: "<@spring.message code='dataSetParam.InputType.TIME' />", value: "${ParamInputType.TIME}"},
			{name: "<@spring.message code='dataSetParam.InputType.DATETIME' />", value: "${ParamInputType.DATETIME}"},
			{name: "<@spring.message code='dataSetParam.InputType.RADIO' />", value: "${ParamInputType.RADIO}"},
			{name: "<@spring.message code='dataSetParam.InputType.CHECKBOX' />", value: "${ParamInputType.CHECKBOX}"},
			{name: "<@spring.message code='dataSetParam.InputType.TEXTAREA' />", value: "${ParamInputType.TEXTAREA}"}
		],
		dataSetPropertyTypeOptions:
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
		]
	});
	
	po.vueRef("${pid}dataSourceFormatPanelEle", null);
	
	po.hasDuplicateName = function(array, name, ignoreIdx)
	{
		var nameIdx = $.inArrayById(array, name, "name");
		return (nameIdx >= 0 && nameIdx != ignoreIdx ? true : false);
	};
	
	po.vueMethod(
	{
		formatParamType: function(data)
		{
			var pm = po.vuePageModel();
			return $.findNameByValue(pm.dataSetParamDataTypeOptions, data.type);
		},
		formatParamInputType: function(data)
		{
			var pm = po.vuePageModel();
			return $.findNameByValue(pm.dataSetParamInputTypeOptions, data.inputType);
		},
		formatParamRequired: function(data)
		{
			return po.formatBooleanValue(data.required);
		},
		formatPropertyType: function(data)
		{
			var pm = po.vuePageModel();
			return $.findNameByValue(pm.dataSetPropertyTypeOptions, data.type);
		},
		onParamRowEditSave: function(e)
		{
			var fm = po.vueFormModel();
			var valid = true;
			
			if(!e.newData.name)
			{
				valid = false;
				$.tipInfo("<@spring.message code='paramNameRequired' />");
			}
			else if(po.hasDuplicateName(fm.params, e.newData.name, e.index))
			{
				valid = false;
				$.tipInfo("<@spring.message code='paramNameMustBeUnique' />");
			}
			
			if(!valid)
			{
				var pm = po.vuePageModel();
				pm.editingParamRows.push(e.data);
			}
			else
			{
				fm.params[e.index] = e.newData;
			}
		},
		onParamRowEditCancel: function(e)
		{
			var fm = po.vueFormModel();
			var valid = true;
			
			if(!e.data.name)
			{
				valid = false;
				$.tipInfo("<@spring.message code='paramNameRequired' />");
			}
			else if(po.hasDuplicateName(fm.params, e.data.name, e.index))
			{
				valid = false;
				$.tipInfo("<@spring.message code='paramNameMustBeUnique' />");
			}
			
			if(!valid)
			{
				var pm = po.vuePageModel();
				pm.editingParamRows.push(e.data);
			}
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
			else if(po.hasDuplicateName(fm.properties, e.newData.name, e.index))
			{
				valid = false;
				$.tipInfo("<@spring.message code='propertyNameMustBeUnique' />");
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
			else if(po.hasDuplicateName(fm.properties, e.data.name, e.index))
			{
				valid = false;
				$.tipInfo("<@spring.message code='propertyNameMustBeUnique' />");
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
		onRowEditInputStopPropagation: function(e)
		{
			e.stopPropagation();
		},
		onAddParam: function(e)
		{
			var fm = po.vueFormModel();
			var pm = po.vuePageModel();
			
			fm.params.push({ name: "", type: "${ParamDataType.STRING}", required: true, inputType: "${ParamInputType.TEXT}" });
			pm.editingParamRows.push(fm.params[fm.params.length-1]);
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
		onAddProperty: function(e)
		{
			var fm = po.vueFormModel();
			var pm = po.vuePageModel();
			
			fm.properties.push({ name: "", type: "${PropertyDataType.STRING}" });
			pm.editingPropertyRows.push(fm.properties[fm.properties.length-1]);
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
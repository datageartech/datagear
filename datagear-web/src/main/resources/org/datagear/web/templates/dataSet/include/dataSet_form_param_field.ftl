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
<#assign FieldDataType=statics['org.datagear.analysis.DataSetField$DataType']>
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
				<div class="flex-grow-1 flex align-items-center gap-1">
					<p-button type="button" label="<@spring.message code='add' />"
						@click="onAddParam" class="p-button-secondary p-button-sm">
					</p-button>
					<p-button type="button" label="<@spring.message code='edit' />"
						@click="onEditParam" class="p-button-secondary p-button-sm">
					</p-button>
					<p-button type="button" label="<@spring.message code='moveUp' />"
						@click="onMoveParam($event, 'up')" class="p-button-secondary p-button-sm">
					</p-button>
					<p-button type="button" label="<@spring.message code='moveDown' />"
						@click="onMoveParam($event, 'down')" class="p-button-secondary p-button-sm">
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
					<p-column field="required" header="<@spring.message code='isRequired' />" class="col-boolean">
						<template #body="{data}">
							{{formatParamRequired(data)}}
						</template>
					</p-column>
					<p-column field="label" header="<@spring.message code='displayName' />" class="col-name">
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
				<div class="flex align-items-center gap-1">
					<p-button type="button" label="<@spring.message code='add' />"
						@click="onAddField" class="p-button-secondary p-button-sm">
					</p-button>
					<p-button type="button" label="<@spring.message code='edit' />"
						@click="onEditField" class="p-button-secondary p-button-sm">
					</p-button>
					<p-button type="button" label="<@spring.message code='moveUp' />"
						@click="onMoveField($event, 'up')" class="p-button-secondary p-button-sm">
					</p-button>
					<p-button type="button" label="<@spring.message code='moveDown' />"
						@click="onMoveField($event, 'down')" class="p-button-secondary p-button-sm">
					</p-button>
					<p-button type="button" label="<@spring.message code='delete' />"
						@click="onDeleteField" class="p-button-danger p-button-sm">
					</p-button>
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
				<p-treetable :value="pm.fieldTreeNodes" :scrollable="true"
					v-model:selection-keys="pm.selectedFieldKeys"
					v-model:expanded-keys="pm.expandedFieldKeys"
					:resizable-columns="true" column-resize-mode="expand"
					selection-mode="multiple" :meta-key-selection="true" data-key="key" class="fields-table table-sm">
					<p-column field="name" header="<@spring.message code='name' />" expander>
					</p-column>
					<p-column field="type" header="<@spring.message code='type' />">
						<template #body="{node}">
							{{formatFieldType(node.data)}}
						</template>
					</p-column>
					<p-column field="array" header="<@spring.message code='array' />">
						<template #body="{node}">
							{{formatFieldArray(node.data)}}
						</template>
					</p-column>
					<p-column field="label" header="<@spring.message code='displayName' />">
					</p-column>
					<p-column field="defaultValue" header="<@spring.message code='defaultValue' />">
					</p-column>
					<p-column field="evaluated" header="<@spring.message code='enableExpression' />">
						<template #body="{node}">
							{{formatFieldEvaludated(node.data)}}
						</template>
					</p-column>
					<p-column field="expression" header="<@spring.message code='expression' />">
					</p-column>
				</p-treetable>
			</div>
		</div>
	</div>
</div>
<div class="field grid">
	<label for="${pid}description" class="field-label col-12 mb-2">
		<@spring.message code='description' />
	</label>
	<div class="field-input col-12">
		<p-textarea id="${pid}description" v-model="fm.description" rows="4" class="input w-full"
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
		selectedFieldKeys: {},
		expandedFieldKeys: {}
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
	
	po.inflateFieldTreeNodes = function(fields)
	{
		var pm = po.vuePageModel();
		pm.fieldTreeNodes = po.fieldsToTreeNodes(fields);
	};
	
	po.fieldsToTreeNodes = function(fields, parentNode)
	{
		if(fields == null)
			return fields;
		
		var re = [];
		
		for(var i=0; i<fields.length; i++)
		{
			var field = fields[i];
			re[i] = po.fieldToTreeNode(field, parentNode);
		}
		
		return re;
	};
	
	po.fieldToTreeNode = function(field, parentNode)
	{
		var key = (parentNode ? parentNode.key+"-" : "") + field.name;
		var node = { key: key, data: field, leaf: po.isLeafField(field) };
		node.children = po.fieldsToTreeNodes(field.fields, node);
		node.parentNode = parentNode;
		
		return node;
	};
	
	po.isLeafField = function(field)
	{
		return (field.type !== "${FieldDataType.OBJECT}")
	};
	
	po.treeNodesToFields = function(fieldTreeNodes, handleChildren)
	{
		handleChildren = (handleChildren == null ? true : handleChildren);
		
		if(fieldTreeNodes == null)
			return fieldTreeNodes;
		
		var re = [];
		
		for(var i=0; i<fieldTreeNodes.length; i++)
		{
			var ftm = fieldTreeNodes[i];
			re[i] = ftm.data;
			
			if(handleChildren)
				re[i].fields = po.treeNodesToFields(ftm.children);
		}
		
		return re;
	};
	
	po.selectedFieldNodeCount = function()
	{
		var pm = po.vuePageModel();
		return $.propCountOfObj(pm.selectedFieldKeys);
	};
	
	po.firstSelectedFieldNode = function()
	{
		var pm = po.vuePageModel();
		var key = null;
		
		for(var p in pm.selectedFieldKeys)
		{
			key = p;
			break;
		}
		
		if(key == null)
			return null;
		
		return $.findTreeArrayById(pm.fieldTreeNodes, key, "key");
	};
	
	po.selectedFieldNodesOneParent = function()
	{
		var pm = po.vuePageModel();
		var parentNode = false;
		
		for(var p in pm.selectedFieldKeys)
		{
			var node = $.findTreeArrayById(pm.fieldTreeNodes, p, "key");
			
			if(node == null)
				continue;
			
			if(parentNode === false)
				parentNode = node.parentNode;
			else if(parentNode !== node.parentNode)
				return false;
		}
		
		return parentNode;
	};
	
	po.toSelectedFieldNodeArray = function()
	{
		var pm = po.vuePageModel();
		var array = [];
		
		for(var p in pm.selectedFieldKeys)
			array.push(p);
		
		return array;
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
					$.tipWarn("<@spring.message code='paramNameMustBeUniqueIgnoreCase' />");
					return false;
				}
				
				fm.params.push(dsp);
			});
		},
		onEditParam: function(e)
		{
			var pm = po.vuePageModel();
			var fm = po.vueFormModel();
			
			if(!pm.selectedParams || pm.selectedParams.length == 0)
				return;
			
			var dsp = pm.selectedParams[0];
			var dspIdx = $.inArrayById(fm.params, dsp.name, "name");
			
			po.showDataSetParamForm("<@spring.message code='edit' />", dsp, function(dsp)
			{
				if(po.hasDuplicateNameNoCase(fm.params, dsp.name, dspIdx))
				{
					$.tipWarn("<@spring.message code='paramNameMustBeUniqueIgnoreCase' />");
					return false;
				}
				
				fm.params[dspIdx] = dsp;
				pm.selectedParams = [];
			});
		},
		onAddField: function(e)
		{
			var pm = po.vuePageModel();
			var fm = po.vueFormModel();
			var selectedCount = po.selectedFieldNodeCount();
			
			if(selectedCount > 1)
			{
				$.tipWarn("<@spring.message code='pleaseSelectOnlyOne' />");
				return;
			}
			
			var fieldNode = po.firstSelectedFieldNode();
			
			if(fieldNode != null)
			{
				if(fieldNode.data.type !== "${FieldDataType.OBJECT}")
				{
					$.tipWarn("<@spring.message code='onlyObjTypeFieldCanAddChild' />");
					return;
				}
				
				fieldNode.children = (fieldNode.children ? fieldNode.children : []);
			}
			
			var addNodes = (fieldNode == null ? pm.fieldTreeNodes : fieldNode.children);
			var addFields = po.treeNodesToFields(addNodes, false);
			
			po.showDataSetFieldForm("<@spring.message code='add' />", {}, function(field)
			{
				if(po.hasDuplicateNameNoCase(addFields, field.name))
				{
					$.tipWarn("<@spring.message code='fieldNameMustBeUniqueIgnoreCase' />");
					return false;
				}
				
				addNodes.push(po.fieldToTreeNode(field, fieldNode));
				
				if(fieldNode != null && !pm.expandedFieldKeys[fieldNode.key])
					pm.expandedFieldKeys[fieldNode.key] = true;
			},
			addFields);
		},
		onEditField: function(e)
		{
			var pm = po.vuePageModel();
			var fm = po.vueFormModel();
			var selectedCount = po.selectedFieldNodeCount();
			
			if(selectedCount != 1)
			{
				if(selectedCount > 1)
					$.tipWarn("<@spring.message code='pleaseSelectOnlyOne' />");
				
				return;
			}
			
			var fieldNode = po.firstSelectedFieldNode();
			var parentNode = fieldNode.parentNode;
			var editNodes = (parentNode == null ? pm.fieldTreeNodes : parentNode.children);
			var editFields = po.treeNodesToFields(editNodes, false);
			var editFieldIdx = $.inArrayById(editFields, fieldNode.data.name, "name");
			
			po.showDataSetFieldForm("<@spring.message code='edit' />", fieldNode.data, function(field)
			{
				if(po.hasDuplicateNameNoCase(editFields, field.name, editFieldIdx))
				{
					$.tipWarn("<@spring.message code='fieldNameMustBeUniqueIgnoreCase' />");
					return false;
				}
				
				fieldNode.data = field;
			},
			editFields);
		},
		onMoveParam: function(e, dir)
		{
			var fm = po.vueFormModel();
			var pm = po.vuePageModel();
			var sps = $.wrapAsArray(po.vueRaw(pm.selectedParams));
			var spNames = $.propertyValue(sps, "name");
			
			if(dir == "up")
				$.moveUpById(fm.params, spNames, "name");
			else if(dir == "down")
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
		onMoveField: function(e, dir)
		{
			var fm = po.vueFormModel();
			var pm = po.vuePageModel();
			var selectedCount = po.selectedFieldNodeCount();
			
			if(selectedCount == 0)
				return;
			
			var parentNode = po.selectedFieldNodesOneParent();
			
			if(parentNode === false)
			{
				$.tipWarn("<@spring.message code='pleaseSelectItemInOneNode' />");
				return;
			}
			
			var moveNodes = (parentNode == null ? pm.fieldTreeNodes : parentNode.children);
			var keys = po.toSelectedFieldNodeArray();
			
			if(dir == "up")
				$.moveUpById(moveNodes, keys, "key");
			else if(dir == "down")
				$.moveDownById(moveNodes, keys, "key");
		},
		onDeleteField: function(e)
		{
			var fm = po.vueFormModel();
			var pm = po.vuePageModel();
			
			for(var p in pm.selectedFieldKeys)
			{
				$.removeTreeArrayById(pm.fieldTreeNodes, p, "key");
			}
			
			pm.selectedFieldKeys = {};
		},
		toggleDataSourceFormatPanel: function(e)
		{
			po.vueUnref("${pid}dataSourceFormatPanelEle").toggle(e);
		}
	});
})
(${pid});
</script>
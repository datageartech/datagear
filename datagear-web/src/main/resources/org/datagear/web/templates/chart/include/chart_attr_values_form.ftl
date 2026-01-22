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
图表属性值集表单

依赖：
page_boolean_options.ftl
page_palette.ftl
-->
<#assign FormPropertyType=statics['org.datagear.analysis.form.PropertyType']>
<#assign FormPropertyInputType=statics['org.datagear.analysis.form.PropertyInputType']>
<#assign JsonChartPluginPropertiesResolver=statics['org.datagear.analysis.support.JsonChartPluginPropertiesResolver']>
<form id="${pid}chartAttrValuesForm" class="chart-attr-values-form flex flex-column" :class="{readonly: pm.avoModel.readonly}">
	<div class="page-form-content flex-grow-1 px-2 py-1 overflow-y-auto">
		<p-accordion :multiple="true" :active-index="[0]">
			<p-accordion-tab v-for="(group, groupIdx) in pm.avoModel.groups">
				<template #header>
					<span>{{group.nameLabel.value}}</span>
					<span class="text-color-secondary text-sm ml-1">{{group.virtual ? "" : group.name}}</span>
				</template>
				<div class="flex flex-column gap-3 mb-2">
					<p-panel v-for="(grpDataEle, grpDataEleIdx) in pm.avoModel.data[group.name]"
						:class="{ 'disable-p-panel': !group.array, 'p-card': group.array }" :header="group.nameLabel.value+'-'+(grpDataEleIdx+1)+'/'+pm.avoModel.data[group.name].length"
						:toggleable="group.array" class="no-panel-border panel-icon-align-center">
						<template #icons>
							<div class="inline-flex gap-1 mx-2 text-sm" v-if="group.array && !pm.avoModel.readonly">
								<p-button type="button" severity="secondary"
									@click="onChartAttrValuesFormMoveUpGrpEle($event, group, grpDataEleIdx)">
									<@spring.message code='moveUp' />
								</p-button>
								<p-button type="button" severity="secondary"
									@click="onChartAttrValuesFormMoveDownGrpEle($event, group, grpDataEleIdx)">
									<@spring.message code='moveDown' />
								</p-button>
								<p-button type="button" severity="secondary"
									@click="onChartAttrValuesFormInsertGrpEle($event, group, grpDataEleIdx)">
									<@spring.message code='insert' />
								</p-button>
								<p-button type="button" severity="danger"
									@click="onChartAttrValuesFormRemoveGrpEle($event, group, grpDataEleIdx)">
									<@spring.message code='delete' />
								</p-button>
							</div>
						</template>
						<div class="field grid" v-for="(attr, attrIdx) in group.children">
							<label :for="attr.domId+'_'+groupIdx+'_'+grpDataEleIdx" class="field-label col-12 mb-2"
								:title="attr.descLabel && attr.descLabel.value ? attr.descLabel.value : null">
								<span>{{attr.nameLabel.value}}</span>
								<span class="text-color-secondary text-sm ml-1">{{attr.name}}</span>
							</label>
							<div class="field-input col-12">
								<div v-if="attr.inputType == pm.avoModel.FormPropertyInputType.SELECT">
									<div class="flex flex-column gap-1" v-if="attr.array">
										<div v-for="(vi, viIdx) in grpDataEle[attr.name]" :key="viIdx" class="flex gap-2">
											<div class="flex-grow-1 flex" v-if="attr.inputPayload.multiple">
												<p-treeselect v-model="grpDataEle[attr.name][viIdx]" :options="attr.inputPayload.options"
													selection-mode="multiple" class="input w-full" placeholder="<@spring.message code='none' />"
													v-if="attr.inputPayload.treeSelect == true">
												</p-treeselect>
												<p-multiselect v-model="grpDataEle[attr.name][viIdx]" :options="attr.inputPayload.options"
													option-label="name" option-value="value" :show-clear="true" class="input w-full"
													v-else>
												</p-multiselect>
											</div>
											<div class="flex-grow-1 flex" v-else>
												<p-treeselect v-model="grpDataEle[attr.name][viIdx]" :options="attr.inputPayload.options"
													class="input w-full" placeholder="<@spring.message code='none' />"
													v-if="attr.inputPayload.treeSelect == true">
												</p-treeselect>
												<p-dropdown v-model="grpDataEle[attr.name][viIdx]" :options="attr.inputPayload.options"
													option-label="name" option-value="value" class="input flex-grow-1 mr-1"
													v-else>
												</p-dropdown>
											</div>
											<div class="flex align-items-center gap-1">
												<p-button type="button" icon="pi pi-plus" severity="secondary" outlined
													@click="onChartAttrValuesFormInsertGrpEleEle($event, grpDataEle, attr, viIdx)"
													v-if="!pm.avoModel.readonly">
												</p-button>
												<p-button type="button" icon="pi pi-minus" severity="danger" outlined 
													@click="onChartAttrValuesFormRemoveGrpEleEle($event, grpDataEle, attr, viIdx)"
													v-if="!pm.avoModel.readonly">
												</p-button>
											</div>
										</div>
										<div v-if="!pm.avoModel.readonly">
											<p-button type="button" icon="pi pi-plus" severity="secondary" outlined
												@click="onChartAttrValuesFormInsertGrpEleEle($event, grpDataEle, attr)">
											</p-button>
										</div>
									</div>
									<div v-else-if="attr.inputPayload.multiple">
										<p-treeselect v-model="grpDataEle[attr.name]" :options="attr.inputPayload.options"
											selection-mode="multiple" class="input w-full" placeholder="<@spring.message code='none' />"
											v-if="attr.inputPayload.treeSelect == true">
										</p-treeselect>
										<p-multiselect v-model="grpDataEle[attr.name]" :options="attr.inputPayload.options"
											option-label="name" option-value="value" :show-clear="true" class="input w-full"
											v-else>
										</p-multiselect>
									</div>
									<div v-else>
										<p-treeselect v-model="grpDataEle[attr.name]" :options="attr.inputPayload.options"
											class="input w-full" placeholder="<@spring.message code='none' />"
											 v-if="attr.inputPayload.treeSelect == true">
										</p-treeselect>
										<p-dropdown v-model="grpDataEle[attr.name]" :options="attr.inputPayload.options"
											option-label="name" option-value="value" :show-clear="!attr.required" class="input w-full"
											v-else>
										</p-dropdown>
									</div>
								</div>
								<div v-else-if="attr.inputType == pm.avoModel.FormPropertyInputType.COLOR">
									<div class="flex flex-column gap-1" v-if="attr.array">
										<div v-for="(vi, viIdx) in grpDataEle[attr.name]" :key="viIdx" class="flex gap-2">
											<div class="flex-grow-1 flex gap-1">
												<p-inputtext v-model="grpDataEle[attr.name][viIdx]" type="text"
													class="input flex-grow-1">
												</p-inputtext>
												<p-button type="button" :style="{'background-color': grpDataEle[attr.name][viIdx]}"
													class="palette-btn surface-border mr-1"
													@click="showPalettePanel($event, grpDataEle[attr.name], viIdx)"></p-button>
											</div>
											<div class="flex align-items-center gap-1">
												<p-button type="button" icon="pi pi-plus" severity="secondary" outlined
													@click="onChartAttrValuesFormInsertGrpEleEle($event, grpDataEle, attr, viIdx)"
													v-if="!pm.avoModel.readonly">
												</p-button>
												<p-button type="button" icon="pi pi-minus" severity="danger" outlined
													@click="onChartAttrValuesFormRemoveGrpEleEle($event, grpDataEle, attr, viIdx)"
													v-if="!pm.avoModel.readonly">
												</p-button>
											</div>
										</div>
										<div v-if="!pm.avoModel.readonly">
											<p-button type="button" icon="pi pi-plus" severity="secondary" outlined
												@click="onChartAttrValuesFormInsertGrpEleEle($event, grpDataEle, attr)">
											</p-button>
										</div>
									</div>
									<div class="flex gap-1" v-else>
										<p-inputtext v-model="grpDataEle[attr.name]" type="text"
											class="input flex-grow-1" maxlength="100">
										</p-inputtext>
										<p-button type="button" :style="{'background-color': grpDataEle[attr.name]}" class="palette-btn surface-border"
											@click="showPalettePanel($event, grpDataEle, attr.name)"></p-button>
									</div>
								</div>
								<div v-else-if="attr.inputType == pm.avoModel.FormPropertyInputType.RADIO || attr.inputType == pm.avoModel.FormPropertyInputType.CHECKBOX">
									<div class="input flex flex-column gap-1" v-if="attr.array">
										<div v-for="(vi, viIdx) in grpDataEle[attr.name]" :key="viIdx" class="flex gap-2">
											<div class="flex-grow-1 p-inputtext p-component p-2 flex gap-3">
												<div v-for="(opt, optIdx) in attr.inputPayload.options" class="inline-flex align-items-center gap-1">
													<p-radiobutton :input-id="attr.domId+'_'+groupIdx+'_'+grpDataEleIdx+'_'+optIdx+'_'+viIdx"
														:value="opt.value" v-model="grpDataEle[attr.name][viIdx]"
														 v-if="attr.inputType == pm.avoModel.FormPropertyInputType.RADIO">
													</p-radiobutton>
													<p-checkbox :input-id="attr.domId+'_'+groupIdx+'_'+grpDataEleIdx+'_'+optIdx+'_'+viIdx"
														:value="opt.value" v-model="grpDataEle[attr.name][viIdx]"
														v-else>
													</p-checkbox>
													<label :for="attr.domId+'_'+groupIdx+'_'+grpDataEleIdx+'_'+optIdx+'_'+viIdx">{{opt.name}}</label>
												</div>
											</div>
											<div class="flex align-items-center gap-1">
												<p-button type="button" icon="pi pi-plus" severity="secondary" outlined
													@click="onChartAttrValuesFormInsertGrpEleEle($event, grpDataEle, attr, viIdx)"
													v-if="!pm.avoModel.readonly">
												</p-button>
												<p-button type="button" icon="pi pi-minus" severity="danger" outlined
													@click="onChartAttrValuesFormRemoveGrpEleEle($event, grpDataEle, attr, viIdx)"
													v-if="!pm.avoModel.readonly">
												</p-button>
											</div>
										</div>
										<div v-if="!pm.avoModel.readonly">
											<p-button type="button" icon="pi pi-plus" severity="secondary" outlined
												@click="onChartAttrValuesFormInsertGrpEleEle($event, grpDataEle, attr)">
											</p-button>
										</div>
									</div>
									<div class="input p-inputtext p-component p-2 flex gap-3" v-else>
										<div v-for="(opt, optIdx) in attr.inputPayload.options" class="inline-flex align-items-center gap-1">
											<p-radiobutton :input-id="attr.domId+'_'+groupIdx+'_'+grpDataEleIdx+'_'+optIdx"
												:value="opt.value" v-model="grpDataEle[attr.name]"
												 v-if="attr.inputType == pm.avoModel.FormPropertyInputType.RADIO">
											</p-radiobutton>
											<p-checkbox :input-id="attr.domId+'_'+groupIdx+'_'+grpDataEleIdx+'_'+optIdx"
												:value="opt.value" v-model="grpDataEle[attr.name]"
												v-else>
											</p-checkbox>
											<label :for="attr.domId+'_'+groupIdx+'_'+grpDataEleIdx+'_'+optIdx">{{opt.name}}</label>
										</div>
									</div>
								</div>
								<div v-else>
									<div class="input flex flex-column gap-1" v-if="attr.array">
										<div v-for="(vi, viIdx) in grpDataEle[attr.name]" :key="viIdx" class="flex gap-2">
											<p-textarea :id="attr.domId+'_'+groupIdx+'_'+grpDataEleIdx+'_'+viIdx"
												v-model="grpDataEle[attr.name][viIdx]" type="text" class="flex-grow-1"
												 v-if="attr.inputType == pm.avoModel.FormPropertyInputType.TEXTAREA">
											</p-textarea>
											<p-inputtext :id="attr.domId+'_'+groupIdx+'_'+grpDataEleIdx+'_'+viIdx"
												v-model="grpDataEle[attr.name][viIdx]" type="text" class="flex-grow-1"
												v-else>
											</p-inputtext>
											<div class="flex align-items-center gap-1">
												<p-button type="button" icon="pi pi-plus" severity="secondary" outlined
													@click="onChartAttrValuesFormInsertGrpEleEle($event, grpDataEle, attr, viIdx)"
													v-if="!pm.avoModel.readonly">
												</p-button>
												<p-button type="button" icon="pi pi-minus" severity="danger" outlined
													@click="onChartAttrValuesFormRemoveGrpEleEle($event, grpDataEle, attr, viIdx)"
													v-if="!pm.avoModel.readonly">
												</p-button>
											</div>
										</div>
										<div v-if="!pm.avoModel.readonly">
											<p-button type="button" icon="pi pi-plus" severity="secondary" outlined
												@click="onChartAttrValuesFormInsertGrpEleEle($event, grpDataEle, attr)">
											</p-button>
										</div>
									</div>
									<div v-else>
										<p-textarea :id="attr.domId+'_'+groupIdx+'_'+grpDataEleIdx"
											v-model="grpDataEle[attr.name]" type="text" class="input w-full"
											v-if="attr.inputType == pm.avoModel.FormPropertyInputType.TEXTAREA">
										</p-textarea>
										<p-inputtext :id="attr.domId+'_'+groupIdx+'_'+grpDataEleIdx"
											v-model="grpDataEle[attr.name]" type="text" class="input w-full"
											v-else>
										</p-inputtext>
									</div>
								</div>
					        	<div class="validate-msg">
					        		<input :name="toPropPathLiteral(group.name, grpDataEleIdx, attr.name)" type="text" class="validate-proxy"
					        			:class="{'required': attr.required, 'number': attr.type == pm.avoModel.FormPropertyType.NUMBER}" />
					        	</div>
							</div>
						</div>
					</p-panel>
					<div>
						<div class="text-sm" v-if="group.array && !pm.avoModel.readonly">
							<p-button type="button" icon="pi pi-plus" :label="group.nameLabel.value"
								severity="secondary" @click="onChartAttrValuesFormInsertGrpEle($event, group)">
							</p-button>
						</div>
						<div class="field-input" v-if="group.required">
				        	<div class="validate-msg">
				        		<input :name="group.name" required type="text" class="validate-proxy" />
				        	</div>
			        	</div>
		        	</div>
				</div>
			</p-accordion-tab>
		</p-accordion>
	</div>
	<div class="page-form-foot flex-grow-0 flex justify-content-center gap-2 pt-2">
		<p-button type="submit" label="<@spring.message code='confirm' />"></p-button>
		<p-button type="button" label="<@spring.message code='clear' />" severity="danger" @click="onClearChartAttrValuesFormData"></p-button>
		
		<p-button v-for="(btn, btnIdx) in pm.avoModel.buttons" :key="btnIdx"
			type="button" class="p-button-secondary" :label="btn.name" @click="btn.clickHandler">
		</p-button>
	</div>
</form>
<script>
(function(po)
{
	var avo = (po.avo || (po.avo = {}));
	
	avo.FormPropertyType =
	{
		STRING: "${FormPropertyType.STRING}",
		BOOLEAN: "${FormPropertyType.BOOLEAN}",
		INTEGER: "${FormPropertyType.INTEGER}",
		NUMBER: "${FormPropertyType.NUMBER}",
		OBJECT: "${FormPropertyType.OBJECT}"
	};
	
	avo.FormPropertyInputType =
	{
		TEXT: "${FormPropertyInputType.TEXT}",
		SELECT: "${FormPropertyInputType.SELECT}",
		RADIO: "${FormPropertyInputType.RADIO}",
		CHECKBOX: "${FormPropertyInputType.CHECKBOX}",
		TEXTAREA: "${FormPropertyInputType.TEXTAREA}",
		COLOR: "${FormPropertyInputType.COLOR}"
	};
	
	avo.FormPropertyInputPayload =
	{
		//多选
		MULTIPLE: "multiple",
		//地图
		DG_MAP: "DG_MAP",
		
		//5.5.0旧版的下拉框inputPayload.multiple="repeat"值，表示可重复选取
		MultipleRepeat: "repeat"
	};
	
	avo.INPUT_PROPERTY_ADDITION_OLD_GROUP = "${JsonChartPluginPropertiesResolver.INPUT_PROPERTY_ADDITION_OLD_GROUP}";
	
	//根插件对象属性的name值，其包含的属性值直接保存至根属性值对象下
	avo.rootObjectPropertyName = "cpgaName${pid}";
	
	avo.isObjectProperty = function(prop)
	{
		return (prop != null && prop.type == avo.FormPropertyType.OBJECT);
	};
	
	avo.isRootObjectProperty = function(prop)
	{
		return (avo.isObjectProperty(prop) && prop.name == avo.rootObjectPropertyName);
	};
	
	//将org.datagear.analysis.form.FormProperty转换为标准格式
	avo.toTrimProperty = function(prop, clone)
	{
		clone = (clone === undefined ? true : clone);
		
		if(prop == null)
			return prop;
		
		var re = (clone ? $.extend(true, {}, prop) : prop);
		
		avo.trimProperty(re);
		
		if(avo.isObjectProperty(re) && !$.isEmpty(re.properties))
		{
			for(var i=0; i<re.properties.length; i++)
			{
				avo.toTrimProperty(re.properties[i], false);
			}
		}
		
		return re;
	};
	
	avo.propertyDomIdIndex = 0;
	
	avo.trimProperty = function(prop)
	{
		prop.domId = po.concatPid("avoprop_"+ (avo.propertyDomIdIndex++));
		prop.nameLabel = (prop.nameLabel == null ? {} : prop.nameLabel);
		prop.nameLabel.value = ($.isEmpty(prop.nameLabel.value) ? prop.name : prop.nameLabel.value);
		prop.nameLabel.value = ($.isEmpty(prop.nameLabel.value) ? "<@spring.message code='unnamed' />" : prop.nameLabel.value);
		prop.isTrimmed = true;
		
		if(avo.isObjectProperty(prop))
			return;
		
		//布尔型默认作为RADIO处理
		if(prop.type == avo.FormPropertyType.BOOLEAN)
		{
			if(!prop.inputType)
				prop.inputType = avo.FormPropertyInputType.RADIO;
			
			if(!prop.inputPayload)
			{
				var pm = po.vuePageModel();
				prop.inputPayload = po.vueRaw(pm.booleanOptions);
			}
		}
		
		var inputType = prop.inputType;
		
		//下拉框、单选、复选框：将inputPayload转换为{multiple: ..., options: [{name: ..., value: ...}, ...]}格式
		if(inputType == avo.FormPropertyInputType.SELECT
				|| inputType == avo.FormPropertyInputType.RADIO
				|| inputType == avo.FormPropertyInputType.CHECKBOX)
		{
			var inputPayload = (prop.inputPayload || []);
			
			//数组、"DG_MAP"：转换为{ multiple: false, options: ... }格式
			if($.isArray(inputPayload) || (inputPayload == avo.FormPropertyInputPayload.DG_MAP))
				inputPayload = { multiple: false, options: inputPayload };
			
			//{ options: "DG_MAP" }：转换为实际地图数据options
			avo.trimPropertyInputPayloadIfMap(prop, inputPayload);
			
			//默认multiple为false
			inputPayload.multiple = (inputPayload.multiple == null ? false : inputPayload.multiple);
			avo.trimPropertyInputOptions(prop, inputPayload);
			
			if(inputType == avo.FormPropertyInputType.RADIO)
			{
				inputPayload.multiple = false;
			}
			else if(inputType == avo.FormPropertyInputType.CHECKBOX)
			{
				inputPayload.multiple = true;
			}
			
			prop.inputPayload = inputPayload;
		}
		//颜色框
		else if(inputType == avo.FormPropertyInputType.COLOR)
		{
			var inputPayload = prop.inputPayload;
			
			//将5.5.0旧版inputPayload格式{ multiple: true }、"multiple"转换为prop.array=true格式
			if(inputPayload != null)
			{
				if(inputPayload.multiple == true)
				{
					prop.array = true;
					inputPayload.multiple = false;
				}
				else if(inputPayload == avo.FormPropertyInputPayload.MULTIPLE)
				{
					prop.array = true;
					prop.inputPayload = null;
				}
			}
		}
		
		//将5.5.0旧版的{inputPayload: {multiple: "repeat"}}格式转换为6.0新版的{array: true, inputPayload: {multiple: false}}
		if(prop.inputPayload && prop.inputPayload.multiple == avo.FormPropertyInputPayload.MultipleRepeat)
		{
			prop.array = true;
			prop.inputPayload.multiple = false;
		}
	};
	
	avo.trimPropertyInputPayloadIfMap = function(inputProp, inputPayload)
	{
		var options = inputPayload.options;
		
		//内置地图
		if(options == avo.FormPropertyInputPayload.DG_MAP)
		{
			//只有下拉列表才使用树形结构，单选框、复选框只能使用平铺数组
			if(inputPayload.treeSelect == null
					&& inputProp.inputType == avo.FormPropertyInputType.SELECT)
			{
				inputPayload.treeSelect = true;
			}
			
			inputPayload.options = avo.propertyInputOptionsForMap(inputPayload.treeSelect);
		}
	};
	
	avo.trimPropertyInputOptions = function(inputProp, inputPayload)
	{
		if(!inputPayload.options)
			inputPayload.options = [];
		
		//支持非数组格式
		if(!$.isArray(inputPayload.options))
			inputPayload.options = [ inputPayload.options ];
		
		var options = inputPayload.options;
		
		//转换为标准的[ {name: ..., value: ...}, ... ]格式
		$.each(options, function(i, io)
		{
			//支持元素为基本类型
			if(io == null || $.isTypeString(io) || $.isTypeNumber(io) || $.isTypeBoolean(io))
			{
				options[i] = { name: io, value: io };
			}
			
			//支持{value: ...}格式的元素
			if(io.name == null)
				io.name = (io.value == null ? "null" : io.value);
		});
	};

	avo.propertyInputOptionsForMap = function(asTree)
	{
		//树
		if(asTree)
		{
			var listener =
			{
				added: function(node, parent, rootArray)
				{
					//转换为UI组件所需的结构
					node.key = node.mapName;
					node.label = node.mapLabel;
					if(parent && !parent.children)
						parent.children = parent.mapChildren;
				}
			};
			
			return dashboardFactory.getStdBuiltinMapTree(listener);
		}
		//数组
		else
		{
			var listener =
			{
				added: function(node, rootArray)
				{
					//转换为UI组件所需的结构
					node.value = node.mapName;
					node.name = node.mapLabel;
				}
			};
			
			return dashboardFactory.getStdBuiltinMapArray(listener);
		}
	};
	
	//将org.datagear.analysis.form.ObjectFormProperty.properties分组整理
	avo.groupProperties = function(objProp)
	{
		if(objProp == null)
			return;
		
		if($.isEmpty(objProp.properties))
			return;
		
		var oldGroups = avo.resolveOldGroup(objProp.properties);
		if(oldGroups.length > 0)
		{
			objProp.groups = (objProp.groups == null ? [] : objProp.groups);
			objProp.groups = objProp.groups.concat(oldGroups);
		}
		
		var groupProps = [];
		var groups = (objProp.groups || []);
		var props = objProp.properties;
		
		for(var i=0; i<props.length; i++)
		{
			var prop = props[i];
			var myGroup = null;
			var groupIdx = avo.findGroupIdxByPropName(groupProps, prop.name);
			
			if(groupIdx >= 0)
				myGroup = groupProps[groupIdx];
			else
			{
				groupIdx = avo.findGroupIdxByPropName(groups, prop.name);
				if(groupIdx >= 0)
				{
					myGroup = groups[groupIdx];
					groupProps.push(myGroup);
				}
			}
			
			if(myGroup == null)
			{
				if(groupProps.length > 0 && groupProps[groupProps.length-1].virtual)
				{
					myGroup = groupProps[groupProps.length-1];
				}
				else
				{
					myGroup = { nameLabel: { value: "<@spring.message code='ungrouped' />" }, virtual: true };
					groupProps.push(myGroup);
				}
			}
			
			myGroup.properties = (myGroup.properties == null ? [] : myGroup.properties);
			myGroup.properties.push(prop);
			
			if(avo.isObjectProperty(prop))
				avo.groupProperties(prop);
		}
		
		objProp.groupProps = groupProps;
	};
	
	avo.findGroupIdxByPropName = function(groups, propName)
	{
		if(groups == null)
			return -1;
		
		for(var i=0; i<groups.length; i++)
		{
			if(groups[i].names && groups[i].names.findIndex(propName) > -1)
				return i;
		}
		
		return -1;
	};
	
	//兼容处理5.5.0版本的org.datagear.analysis.ChartPluginAttribute.group
	avo.resolveOldGroup = function(props)
	{
		var groups = [];
		
		for(var i=0; i<props.length; i++)
		{
			var prop = props[i];
			
			if(!prop || !prop.additions || !prop.additions[avo.INPUT_PROPERTY_ADDITION_OLD_GROUP])
				continue;
			
			var oldGroup = prop.additions[avo.INPUT_PROPERTY_ADDITION_OLD_GROUP];
			
			var group =
			{
				nameLabel: { value: "" }, names: []
			};
			
			if(!$.isEmpty(oldGroup.name))
				group.nameLabel.value = oldGroup.name;
			else if(oldGroup.nameLabel && !$.isEmpty(oldGroup.nameLabel.value))
				group.nameLabel.value = oldGroup.nameLabel.value;
			
			//无分组名称标签的，只在末尾分组相同时才使用，否则新建
			if($.isEmpty(group.nameLabel.value))
			{
				group.nameLabel.value = "<@spring.message code='ungrouped' />";
				var groupTail = (groups.length > 0 ? groups[groups.length - 1] : null);
				
				if(groupTail && groupTail.nameLabel && groupTail.nameLabel.value == group.nameLabel.value)
				{
					group = groupTail;
				}
				else
				{
					groups.push(group);
				}
			}
			//有分组名称标签的，查找或新建
			else
			{
				var existIdx = -1;
				
				for(var j=0; j<groups.length; j++)
				{
					if(groups[j].nameLabel && groups[j].nameLabel.value == group.nameLabel.value)
					{
						existIdx = j;
						break;
					}
				}
				
				if(existIdx >= 0)
				{
					group = groups[existIdx];
				}
				else
				{
					groups.push(group);
				}
			}
			
			group.names.push(prop.name);
		}
		
		return groups;
	};
	
	//图表属性值对象转换为org.datagear.analysis.ChartPluginAttributeForm的表单数据模型
	avo.attrValuesToFormData = function(attrValues, pluginAttrForm, clone)
	{
		clone = (clone === undefined ? true : clone);
		
		var re = (attrValues || {});
		
		if(clone)
			re = $.extend(true, {}, re);
		
		if(pluginAttrForm == null)
			return re;
		
		if(!pluginAttrForm.isTrimmed)
			pluginAttrForm = avo.toTrimProperty(pluginAttrForm);
		
		var name = pluginAttrForm.name;
		
		if(name == null)
		{
			avo.doAttrValuesToFormData(re, pluginAttrForm);
		}
		else
		{
			if(re[name] == null)
				re[name] = {};
			
			avo.doAttrValuesToFormData(re[name], pluginAttrForm);
		}
		
		return re;
	};
	
	avo.doAttrValuesToFormData = function(attrValues, objProperty)
	{
		if(attrValues == null || objProperty == null || $.isEmpty(objProperty.properties))
			return;
		
		var data = attrValues;
		var props = objProperty.properties;
		
		for(var i=0; i<props.length; i++)
		{
			var prop = props[i];
			var v = data[prop.name];
			
			if(avo.isObjectProperty(prop))
			{
				//创建UI占位模型
				if(v == null)
					v = (prop.array ? [] : {});
				
				if(prop.array)
				{
					if(!$.isArray(v))
						v = [ v ];
					
					for(var j=0; j<v.length; j++)
						avo.doAttrValuesToFormData(v[j], prop);
				}
				else
				{
					avo.doAttrValuesToFormData(v, prop);
				}
			}
			else
			{
				v = avo.trimChartAttrValueArray(prop, v);
				v = avo.encodeAttrValueTreeModel(prop, v);
			}
			
			data[prop.name] = v;
		};
		
		return data;
	};
	
	//图表属性值转换为树组件Model
	// "v0" -> { v0: true }
	// [ "v0", "v1", ... ] -> { v0: true, v1: true, ... }、[ { v0: true }, { v1: true }, ... ]
	// [ [ "v0", "v1" ], ... ] -> [ { v0: true, v1: true, ... }, ... ]
	avo.encodeAttrValueTreeModel = function(inputProp, value)
	{
		if(value == null)
			return value;
		
		var isTreeSelect = (inputProp.inputPayload && inputProp.inputPayload.treeSelect == true);
		
		if(!isTreeSelect)
			return value;
		
		value = ($.isArray(value) ? value : [ value ]);
		
		var re;
		
		if(inputProp.array)
		{
			re = [];
			
			value.forEach((vi) =>
			{
				if(vi == null)
					return;
				
				var rei = {};
				
				if($.isArray(vi))
				{
					vi.forEach((vii) =>
					{
						if(vii != null)
							rei[vii] = true;
					});
				}
				else
				{
					rei[vi] = true;
				}
				
				re.push(rei);
			});
		}
		else
		{
			re = {};
			
			value.forEach((vi) =>
			{
				if(vi != null)
					re[vi] = true;
			});
		}
		
		return re;
	};
	
	//将由avo.attrValuesToFormData()函数生成的表单数据转换为图表属性值对象，执行类型转换、选项值限定等
	avo.formDataToAttrValues = function(formData, pluginAttrForm)
	{
		var re = (formData || {});
		re = $.extend(true, {}, re);
		
		if(pluginAttrForm == null)
			return re;
		
		if(!pluginAttrForm.isTrimmed)
			pluginAttrForm = avo.toTrimProperty(pluginAttrForm);
		
		var name = pluginAttrForm.name;
		
		if(name == null)
		{
			avo.doFormDataToAttrValues(re, pluginAttrForm);
		}
		else
		{
			var v = re[name];
			
			if(v != null)
				avo.doFormDataToAttrValues(v, pluginAttrForm);
			
			//删除由avo.attrValuesToFormData()生成的空对象
			if(v == null && $.isEmptyObject(v))
				delete re[name];
		}
		
		return re;
	};
	
	avo.doFormDataToAttrValues = function(formData, objProperty)
	{
		//注意：formData中对于没有在objProperty定义的属性值应原样保留，
		//因为看板的dg-chart-attr-values应允许定义图表插件属性之外的扩展值
		
		if(formData == null || objProperty == null || $.isEmpty(objProperty.properties))
			return formData;
		
		var data = formData;
		var props = objProperty.properties;
		
		for(var i=0; i<props.length; i++)
		{
			var prop = props[i];
			var v = data[prop.name];
			
			if(v == null)
			{
			}
			else if(avo.isObjectProperty(prop))
			{
				if($.isArray(v))
				{
					for(var j=0; j<v.length; j++)
						avo.doFormDataToAttrValues(v[j], prop);
					
					//由avo.doAttrValuesToFormData()生成的空数组置为null，后续删除
					if(v.length == 0)
						v = null;
				}
				else
				{
					avo.doFormDataToAttrValues(v, prop);
					
					//由avo.doAttrValuesToFormData()生成的空对象置为null，后续删除
					if($.isEmptyObject(v))
						v = null;
				}
			}
			else
			{
				v = avo.decodeAttrValueTreeModel(prop, v);
				v = avo.trimChartAttrValueArray(prop, v);
				v = avo.toChartAttrTypeValue(prop, v);
			}
			
			//null值不应保留，以支持后续组对象的判空逻辑
			if(v == null)
				delete data[prop.name];
			else
				data[prop.name] = v;
		};
	};
	
	//树组件Model转换为图表属性值，另参考avo.encodeAttrValueTreeModel()函数
	avo.decodeAttrValueTreeModel = function(inputProp, value)
	{
		if(value == null)
			return value;
		
		var isTreeSelect = (inputProp.inputPayload && inputProp.inputPayload.treeSelect == true);
		
		if(!isTreeSelect)
			return value;
		
		var isArray = inputProp.array;
		var isMultiple = (inputProp.inputPayload && inputProp.inputPayload.multiple == true);
		
		if($.isPlainObject(value))
			value = [ value ];
		
		var re;
		
		if($.isArray(value))
		{
			re = [];
			
			value.forEach((vi) =>
			{
				if(vi == null)
					return;
				
				if($.isPlainObject(vi))
				{
					var rei = [];
					
					for(var vip in vi)
					{
						if(vip != null)
							rei.push(vip);
					}
					
					if(rei.length > 0)
					{
						if(isMultiple)
							re.push(rei);
						else
						{
							re.push(rei[0]);
						}
					}
				}
				else
				{
					re.push(vi);
				}
			});
			
			if(!isArray)
				re = re[0];
		}
		else
			re = value;
		
		return re;
	};
	
	avo.trimChartAttrValueArray = function(inputProp, value)
	{
		if(value == null)
			return value;
		
		if(!$.isArray(value))
		{
			if(inputProp.inputPayload && inputProp.inputPayload.multiple == true)
				value = [ value ];
			
			if(inputProp.array)
				value = [ value ];
		}
		
		return value;
	};
	
	avo.toChartAttrTypeValue = function(inputProp, value)
	{
		var type = inputProp.type;
		
		if(type != avo.FormPropertyType.STRING && value === "")
			value = null;
		
		if(value == null)
		{
			return value;
		}
		else if($.isArray(value))
		{
			var re = [];
			
			value.forEach((vi) =>
			{
				vi = avo.toChartAttrTypeValue(inputProp, vi);
				
				if(vi != null)
					re.push(vi);
			});
			
			return (re.length > 0 ? re : null);
		}
		else
		{
			if(type == avo.FormPropertyType.BOOLEAN)
			{
				value = (value == true || value === "true" || value === "1" ? true : false);
			}
			else if(type == avo.FormPropertyType.NUMBER)
			{
				value = $.parseToNumber(value);
				value = (isNaN(value) ? null : value);
			}
			
			if(value != null)
			{
				//应将值限定为待选值集合内，比如图表插件升级后inputPayload有所删减，那么这里的旧值应删除
				var inputPayload = inputProp.inputPayload;
				var payloadOptions = (inputPayload && inputPayload.options ? inputPayload.options : null);
				var isTreeSelect = (inputPayload && inputPayload.treeSelect == true);
				
				if(payloadOptions != null && $.isArray(payloadOptions))
				{
					if(isTreeSelect)
					{
						if($.inTreeArrayById(payloadOptions, value, "key") != true)
							value = null;
					}
					else
					{
						if($.inArrayById(payloadOptions, value, "value") < 0)
							value = null;
					}
				}
			}
			
			return value;
		}
	};
	
	avo.toTrimAttrValues = function(attrValues, pluginAttrForm)
	{
		if(attrValues == null || pluginAttrForm == null || $.isEmpty(pluginAttrForm.properties))
			return attrValues;
		
		if(!pluginAttrForm.isTrimmed)
			pluginAttrForm = avo.toTrimProperty(pluginAttrForm);
		
		var formData = avo.attrValuesToFormData(attrValues, pluginAttrForm);
		attrValues = po.formDataToAttrValues(formData, pluginAttrForm);
		
		return attrValues;
	};
	
	avo.validateChartAttrValuesRequired = function(props, attrValues)
	{
		if(!props)
			return true;
		
		attrValues = (attrValues || {});
		
		var re = true;
		
		$.each(props, function(i, prop)
		{
			if(prop.required && $.isEmpty(attrValues[prop.name]))
				re = false;
			
			return re;
		});
		
		return re;
	};
	
	avo.setFormAttrValues = function(attrValues)
	{
		var pm = po.vuePageModel();
		var pluginAttrForm = pm.avoModel.pluginAttrForm;
		var data = avo.attrValuesToFormData(attrValues, pluginAttrForm);
		pm.avoModel.data = data;
	};
	
	avo.clearFormData = function()
	{
		var pm = po.vuePageModel();
		var pluginAttrForm = pm.avoModel.pluginAttrForm;
		var data = pm.avoModel.data;
		
		for(let p in data)
		{
			delete data[p];
		}
		
		avo.attrValuesToFormData(data, pluginAttrForm, false);
	};
	
	po.setupChartAttrValuesForm = function(pluginAttrForm, attrValues, options)
	{
		options = $.extend(
		{
			submitHandler: null,
			buttons: [],
			readonly: false
		},
		options);
		
		var pm = po.vuePageModel();
		pm.avoModel.pluginAttrForm = avo.toTrimProperty(pluginAttrForm);
		avo.groupProperties(pm.avoModel.pluginAttrForm);
		pm.avoModel.buttons = options.buttons;
		pm.avoModel.readonly = options.readonly;
		avo.setFormAttrValues(attrValues);
		
		var form = po.elementOfId("${pid}chartAttrValuesForm", document.body);
		po.setupSimpleForm(form, pm.avoModel.data,
		{
			submitHandler: function()
			{
				if(options && options.submitHandler)
				{
					var pluginAttrForm = pm.avoModel.pluginAttrForm;
					var data = po.vueRaw(pm.avoModel.data);
					var attrValues = avo.formDataToAttrValues(data, pluginAttrForm);
					options.submitHandler(attrValues);
				}
			}
		});
	};
	
	po.vuePageModel(
	{
		avoModel:
		{
			FormPropertyType: avo.FormPropertyType,
			FormPropertyInputType: avo.FormPropertyInputType,
			data: {},
			readonly: false,
			buttons: [],
			groups: []
		}
	});
	
	po.vueMethod(
	{
		toPropPathLiteral: function()
		{
			return $.concatPropPath.apply($, arguments);
		},
		
		onClearChartAttrValuesFormData: function()
		{
			po.confirm(
			{
				message: "<@spring.message code='confirmClearAllChartAttr' />",
				accept: function()
				{
					avo.clearFormData();
				} 
			});
		},
		
		onChartAttrValuesFormMoveUpGrpEle: function(e, group, idx)
		{
			var groupName = group.name;
			var pm = po.vuePageModel();
			var data = pm.avoModel.data;
			var groupData = data[groupName];
			
			if(idx > 0)
			{
				var me = groupData[idx];
				var prev = groupData[idx-1];
				groupData[idx-1] = me;
				groupData[idx] = prev;
			}
		},
		
		onChartAttrValuesFormMoveDownGrpEle: function(e, group, idx)
		{
			var groupName = group.name;
			var pm = po.vuePageModel();
			var data = pm.avoModel.data;
			var groupData = data[groupName];
			
			if(idx < (groupData.length -1))
			{
				var me = groupData[idx];
				var next = groupData[idx+1];
				groupData[idx+1] = me;
				groupData[idx] = next;
			}
		},
		
		onChartAttrValuesFormInsertGrpEle: function(e, group, idx)
		{
			var groupName = group.name;
			var pm = po.vuePageModel();
			var data = pm.avoModel.data;

			if(!data[groupName])
				data[groupName] = [];
			
			if(idx == null)
				data[groupName].push({});
			else
				data[groupName].splice(idx, 0, {});
		},
		
		onChartAttrValuesFormRemoveGrpEle: function(e, group, idx)
		{
			po.confirm(
			{
				message: "<@spring.message code='confirmDeleteThisDataAsk' />",
				accept: function()
				{
					var groupName = group.name;
					var pm = po.vuePageModel();
					var data = pm.avoModel.data;
					data[groupName].splice(idx, 1);
				}
			});
		},
		
		onChartAttrValuesFormInsertGrpEleEle: function(e, grpDataEle, prop, idx)
		{
			var propName = prop.name;
			var isTreeSelect = (prop.inputPayload && prop.inputPayload.treeSelect == true);
			
			if(grpDataEle[propName] == null)
				grpDataEle[propName] = [];
			
			if(idx == null)
				grpDataEle[propName].push(isTreeSelect ? {} : null);
			else
				grpDataEle[propName].splice(idx, 0, isTreeSelect ? {} : null);
		},
		
		onChartAttrValuesFormRemoveGrpEleEle: function(e, grpDataEle, prop, idx)
		{
			var propName = prop.name;
			
			if(grpDataEle[propName] == null)
				return;
			
			grpDataEle[propName].splice(idx, 1);
		}
	});
})
(${pid});
</script>
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
<form id="${pid}chartAttrValuesForm" class="chart-attr-values-form flex flex-column" :class="{readonly: pm.chartAttrValuesForm.readonly}">
	<div class="page-form-content flex-grow-1 px-2 py-1 overflow-y-auto">
		<p-accordion :multiple="true" :active-index="[0]">
			<p-accordion-tab v-for="(group, groupIdx) in pm.chartAttrValuesForm.groups">
				<template #header>
					<span>{{group.nameLabel.value}}</span>
					<span class="text-color-secondary text-sm ml-1">{{group.virtual ? "" : group.name}}</span>
				</template>
				<div class="flex flex-column gap-3 mb-2">
					<p-panel v-for="(grpDataEle, grpDataEleIdx) in pm.chartAttrValuesForm.data[group.name]"
						:class="{ 'disable-p-panel': !group.array, 'p-card': group.array }" :header="group.nameLabel.value+'-'+(grpDataEleIdx+1)+'/'+pm.chartAttrValuesForm.data[group.name].length"
						:toggleable="group.array" class="no-panel-border panel-icon-align-center">
						<template #icons>
							<div class="inline-flex gap-1 mx-2 text-sm" v-if="group.array && !pm.chartAttrValuesForm.readonly">
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
								<div v-if="attr.inputType == pm.FormPropertyInputType.SELECT">
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
													v-if="!pm.chartAttrValuesForm.readonly">
												</p-button>
												<p-button type="button" icon="pi pi-minus" severity="danger" outlined 
													@click="onChartAttrValuesFormRemoveGrpEleEle($event, grpDataEle, attr, viIdx)"
													v-if="!pm.chartAttrValuesForm.readonly">
												</p-button>
											</div>
										</div>
										<div v-if="!pm.chartAttrValuesForm.readonly">
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
								<div v-else-if="attr.inputType == pm.FormPropertyInputType.COLOR">
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
													v-if="!pm.chartAttrValuesForm.readonly">
												</p-button>
												<p-button type="button" icon="pi pi-minus" severity="danger" outlined
													@click="onChartAttrValuesFormRemoveGrpEleEle($event, grpDataEle, attr, viIdx)"
													v-if="!pm.chartAttrValuesForm.readonly">
												</p-button>
											</div>
										</div>
										<div v-if="!pm.chartAttrValuesForm.readonly">
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
								<div v-else-if="attr.inputType == pm.FormPropertyInputType.RADIO || attr.inputType == pm.FormPropertyInputType.CHECKBOX">
									<div class="input flex flex-column gap-1" v-if="attr.array">
										<div v-for="(vi, viIdx) in grpDataEle[attr.name]" :key="viIdx" class="flex gap-2">
											<div class="flex-grow-1 p-inputtext p-component p-2 flex gap-3">
												<div v-for="(opt, optIdx) in attr.inputPayload.options" class="inline-flex align-items-center gap-1">
													<p-radiobutton :input-id="attr.domId+'_'+groupIdx+'_'+grpDataEleIdx+'_'+optIdx+'_'+viIdx"
														:value="opt.value" v-model="grpDataEle[attr.name][viIdx]"
														 v-if="attr.inputType == pm.FormPropertyInputType.RADIO">
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
													v-if="!pm.chartAttrValuesForm.readonly">
												</p-button>
												<p-button type="button" icon="pi pi-minus" severity="danger" outlined
													@click="onChartAttrValuesFormRemoveGrpEleEle($event, grpDataEle, attr, viIdx)"
													v-if="!pm.chartAttrValuesForm.readonly">
												</p-button>
											</div>
										</div>
										<div v-if="!pm.chartAttrValuesForm.readonly">
											<p-button type="button" icon="pi pi-plus" severity="secondary" outlined
												@click="onChartAttrValuesFormInsertGrpEleEle($event, grpDataEle, attr)">
											</p-button>
										</div>
									</div>
									<div class="input p-inputtext p-component p-2 flex gap-3" v-else>
										<div v-for="(opt, optIdx) in attr.inputPayload.options" class="inline-flex align-items-center gap-1">
											<p-radiobutton :input-id="attr.domId+'_'+groupIdx+'_'+grpDataEleIdx+'_'+optIdx"
												:value="opt.value" v-model="grpDataEle[attr.name]"
												 v-if="attr.inputType == pm.FormPropertyInputType.RADIO">
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
												 v-if="attr.inputType == pm.FormPropertyInputType.TEXTAREA">
											</p-textarea>
											<p-inputtext :id="attr.domId+'_'+groupIdx+'_'+grpDataEleIdx+'_'+viIdx"
												v-model="grpDataEle[attr.name][viIdx]" type="text" class="flex-grow-1"
												v-else>
											</p-inputtext>
											<div class="flex align-items-center gap-1">
												<p-button type="button" icon="pi pi-plus" severity="secondary" outlined
													@click="onChartAttrValuesFormInsertGrpEleEle($event, grpDataEle, attr, viIdx)"
													v-if="!pm.chartAttrValuesForm.readonly">
												</p-button>
												<p-button type="button" icon="pi pi-minus" severity="danger" outlined
													@click="onChartAttrValuesFormRemoveGrpEleEle($event, grpDataEle, attr, viIdx)"
													v-if="!pm.chartAttrValuesForm.readonly">
												</p-button>
											</div>
										</div>
										<div v-if="!pm.chartAttrValuesForm.readonly">
											<p-button type="button" icon="pi pi-plus" severity="secondary" outlined
												@click="onChartAttrValuesFormInsertGrpEleEle($event, grpDataEle, attr)">
											</p-button>
										</div>
									</div>
									<div v-else>
										<p-textarea :id="attr.domId+'_'+groupIdx+'_'+grpDataEleIdx"
											v-model="grpDataEle[attr.name]" type="text" class="input w-full"
											v-if="attr.inputType == pm.FormPropertyInputType.TEXTAREA">
										</p-textarea>
										<p-inputtext :id="attr.domId+'_'+groupIdx+'_'+grpDataEleIdx"
											v-model="grpDataEle[attr.name]" type="text" class="input w-full"
											v-else>
										</p-inputtext>
									</div>
								</div>
					        	<div class="validate-msg">
					        		<input :name="toPropPathLiteral(group.name, grpDataEleIdx, attr.name)" type="text" class="validate-proxy"
					        			:class="{'required': attr.required, 'number': attr.type == pm.FormPropertyType.NUMBER}" />
					        	</div>
							</div>
						</div>
					</p-panel>
					<div>
						<div class="text-sm" v-if="group.array && !pm.chartAttrValuesForm.readonly">
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
		
		<p-button v-for="(btn, btnIdx) in pm.chartAttrValuesForm.buttons" :key="btnIdx"
			type="button" class="p-button-secondary" :label="btn.name" @click="btn.clickHandler">
		</p-button>
	</div>
</form>
<script>
(function(po)
{
	po.FormPropertyType =
	{
		STRING: "${FormPropertyType.STRING}",
		BOOLEAN: "${FormPropertyType.BOOLEAN}",
		INTEGER: "${FormPropertyType.INTEGER}",
		NUMBER: "${FormPropertyType.NUMBER}",
		OBJECT: "${FormPropertyType.OBJECT}"
	};
	
	po.FormPropertyInputType =
	{
		TEXT: "${FormPropertyInputType.TEXT}",
		SELECT: "${FormPropertyInputType.SELECT}",
		RADIO: "${FormPropertyInputType.RADIO}",
		CHECKBOX: "${FormPropertyInputType.CHECKBOX}",
		TEXTAREA: "${FormPropertyInputType.TEXTAREA}",
		COLOR: "${FormPropertyInputType.COLOR}"
	};
	
	po.FormPropertyInputPayload =
	{
		//多选
		MULTIPLE: "multiple",
		//地图
		DG_MAP: "DG_MAP",
		
		//5.5.0旧版的下拉框inputPayload.multiple="repeat"值，表示可重复选取
		MultipleRepeat: "repeat"
	};
	
	//根插件对象属性的name值，其包含的属性值直接保存至根属性值对象下
	po.rootPluginObjectAttrName = "cpgaName${pid}";
	
	po.isObjectFormProperty = function(attr)
	{
		return (attr != null && attr.type == po.FormPropertyType.OBJECT);
	};
	
	po.isRootChartPluginObjectAttr = function(attr)
	{
		return (po.isObjectFormProperty(attr) && attr.name == po.rootPluginObjectAttrName);
	};
	
	po.trimChartPluginAttrByGroup = function(attrs)
	{
		attrs = po.trimChartPluginAttributes(attrs);
		
		var groups = [];
		
		for(var i=0; i<attrs.length; i++)
		{
			var attr = attrs[i];
			
			if(po.isObjectFormProperty(attr))
			{
				groups.push(attr);
				continue;
			}
			
			//未定义分组的，建立虚拟分组，统一结构、易于处理
			var virtualGroup =
			{
				name: po.rootPluginObjectAttrName, type: po.FormPropertyType.OBJECT,
				array: false, children: [], nameLabel: { value: "" }, virtual: true
			};
			
			//兼容处理5.5.0版本的org.datagear.analysis.ChartPluginAttribute.group
			if(attr.group != null && !$.isEmpty(attr.group.name))
			{
				virtualGroup.nameLabel.value = attr.group.name;
			}
			
			//无分组名称标签的，只在末尾分组相同时才使用，否则新建
			if($.isEmpty(virtualGroup.nameLabel.value))
			{
				virtualGroup.nameLabel.value = "<@spring.message code='ungrouped' />";
				
				var groupTail = (groups.length > 0 ? groups[groups.length - 1] : null);
				
				if(groupTail && po.isVirtualChartPluginObjectAttr(groupTail)
						&& groupTail.nameLabel && groupTail.nameLabel.value == virtualGroup.nameLabel.value)
				{
					virtualGroup = groupTail;
				}
				else
				{
					groups.push(virtualGroup);
				}
			}
			//有分组名称标签的，查找或新建
			else
			{
				var existIdx = -1;
				
				for(var j=0; j<groups.length; j++)
				{
					if(po.isVirtualChartPluginObjectAttr(groups[j])
							&& groups[j].nameLabel && groups[j].nameLabel.value == virtualGroup.nameLabel.value)
					{
						existIdx = j;
						break;
					}
				}
				
				if(existIdx >= 0)
				{
					virtualGroup = groups[existIdx];
				}
				else
				{
					groups.push(virtualGroup);
				}
			}
			
			po.trimChartPluginAttribute(virtualGroup);
			virtualGroup.children.push(attr);
		}
		
		//检查并一致设置同名分组的array值，避免UI处理混乱
		for(var i=0; i<groups.length; i++)
		{
			var group = groups[i];
			var prevSameGroup = null;
			
			for(var j=0; j<i; j++)
			{
				if(groups[j].name == group.name)
				{
					prevSameGroup = groups[j];
					break;
				}
			}
			
			if(prevSameGroup != null)
				group.array = prevSameGroup.array;
		}
		
		return groups;
	};
	
	po.isVirtualChartPluginObjectAttr = function(objectAttr)
	{
		return (objectAttr != null && objectAttr.virtual);
	};
	
	po.chartPluginAttributeDomIdIdx = 0;
	
	po.trimChartPluginAttributes = function(attrs, clone)
	{
		attrs = (attrs == null ? [] : attrs);
		clone = (clone === undefined ? true : clone);
		
		if(clone)
			attrs = $.extend(true, [], attrs);
		
		for(var i=0; i<attrs.length; i++)
		{
			var attr = attrs[i];
			
			po.trimChartPluginAttribute(attr);
			
			if(po.isObjectFormProperty(attr))
			{
				po.trimChartPluginAttributes(attr.children, false);
			}
		}
		
		return attrs;
	};
	
	po.trimChartPluginAttribute = function(attr)
	{
		attr.domId = po.concatPid("cpattr_"+ (po.chartPluginAttributeDomIdIdx++));
		attr.nameLabel = (attr.nameLabel == null ? {} : attr.nameLabel);
		attr.nameLabel.value = ($.isEmpty(attr.nameLabel.value) ? attr.name : attr.nameLabel.value);
		attr.nameLabel.value = ($.isEmpty(attr.nameLabel.value) ? "<@spring.message code='unnamed' />" : attr.nameLabel.value);
		
		if(po.isObjectFormProperty(attr))
		{
		}
		else
		{
			//布尔型默认作为RADIO处理
			if(attr.type == po.FormPropertyType.BOOLEAN)
			{
				if(!attr.inputType)
					attr.inputType = po.FormPropertyInputType.RADIO;
				
				if(!attr.inputPayload)
				{
					var pm = po.vuePageModel();
					attr.inputPayload = po.vueRaw(pm.booleanOptions);
				}
			}
			
			var inputType = attr.inputType;
			
			//下拉框、单选、复选框：将inputPayload转换为{multiple: ..., options: [{name: ..., value: ...}, ...]}格式
			if(inputType == po.FormPropertyInputType.SELECT
					|| inputType == po.FormPropertyInputType.RADIO
					|| inputType == po.FormPropertyInputType.CHECKBOX)
			{
				var inputPayload = (attr.inputPayload || []);
				
				//数组、"DG_MAP"：转换为{ multiple: false, options: ... }格式
				if($.isArray(inputPayload) || (inputPayload == po.FormPropertyInputPayload.DG_MAP))
					inputPayload = { multiple: false, options: inputPayload };
				
				//{ options: "DG_MAP" }：转换为实际地图数据options
				po.trimChartPluginInputAttrInputPayloadIfMap(attr, inputPayload);
				
				//默认multiple为false
				inputPayload.multiple = (inputPayload.multiple == null ? false : inputPayload.multiple);
				po.trimChartPluginInputAttrInputOptions(attr, inputPayload);
				
				if(inputType == po.FormPropertyInputType.RADIO)
				{
					inputPayload.multiple = false;
				}
				else if(inputType == po.FormPropertyInputType.CHECKBOX)
				{
					inputPayload.multiple = true;
				}
				
				attr.inputPayload = inputPayload;
			}
			//颜色框
			else if(inputType == po.FormPropertyInputType.COLOR)
			{
				var inputPayload = attr.inputPayload;
				
				//将5.5.0旧版inputPayload格式{ multiple: true }、"multiple"转换为attr.array=true格式
				if(inputPayload != null)
				{
					if(inputPayload.multiple == true)
					{
						attr.array = true;
						inputPayload.multiple = false;
					}
					else if(inputPayload == po.FormPropertyInputPayload.MULTIPLE)
					{
						attr.array = true;
						attr.inputPayload = null;
					}
				}
			}
			
			//将5.5.0旧版的{inputPayload: {multiple: "repeat"}}格式转换为6.0新版的{array: true, inputPayload: {multiple: false}}
			if(attr.inputPayload && attr.inputPayload.multiple == po.FormPropertyInputPayload.MultipleRepeat)
			{
				attr.array = true;
				attr.inputPayload.multiple = false;
			}
		}
	};
	
	po.trimChartPluginInputAttrInputPayloadIfMap = function(inputAttr, inputPayload)
	{
		var options = inputPayload.options;
		
		//内置地图
		if(options == po.FormPropertyInputPayload.DG_MAP)
		{
			//只有下拉列表才使用树形结构，单选框、复选框只能使用平铺数组
			if(inputPayload.treeSelect == null
					&& inputAttr.inputType == po.FormPropertyInputType.SELECT)
			{
				inputPayload.treeSelect = true;
			}
			
			inputPayload.options = po.getChartPluginInputAttrInputOptionsForMap(inputPayload.treeSelect);
		}
	};
	
	po.getChartPluginInputAttrInputOptionsForMap = function(asTree)
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
	
	po.trimChartPluginInputAttrInputOptions = function(inputAttr, inputPayload)
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
	
	//将由po.chartAttrValuesToFormData()函数生成的表单数据转换为图表属性值对象，执行类型转换、选项值限定等
	po.formDataToChartAttrValues = function(formData, pluginAttrForm, clone)
	{
		clone = (clone === undefined ? true : clone);
		
		//注意：formData中对于没有在pluginAttrForm定义的属性值应原样保留，
		//因为看板的dg-chart-attr-values应允许定义图表插件属性之外的扩展值
		
		if(formData == null || pluginAttrForm == null || $.isEmpty(pluginAttrForm.properties))
			return formData;
		
		//要先清除循环引用，复制完后再恢复
		var rootObjRef = formData[po.rootPluginObjectAttrName];
		if(rootObjRef !== undefined)
			delete formData[po.rootPluginObjectAttrName];
		
		var re = (clone ? $.extend(true, {}, formData) : formData);
		
		if(rootObjRef !== undefined)
		{
			formData[po.rootPluginObjectAttrName] = rootObjRef;
			re[po.rootPluginObjectAttrName] = re;
		}
		
		var formProperties = pluginAttrForm.properties;
		for(var i=0; i<formProperties.length; i++)
		{
			var attr = formProperties[i];
			var v = re[attr.name];
			
			if(v == null)
			{
			}
			else if(po.isObjectFormProperty(attr))
			{
				v = ($.isArray(v) ? v : [ v ]);
				
				for(var j=0; j<v.length; j++)
					po.formDataToChartAttrValues(v[j], attr.children, false);
				
				if(!attr.array)
				{
					v = v[0];
					
					//删除由po.chartAttrValuesToFormData()生成的空对象
					if(v != null && $.isEmptyObject(v))
						v = null;
				}
				else
				{
					//删除由po.chartAttrValuesToFormData()生成的空数组
					if(v.length == 0)
						v = null;
				}
			}
			else
			{
				v = po.decodeChartAttrValueTreeModel(attr, v);
				v = po.trimChartAttrValueArray(attr, v);
				v = po.toChartAttrTypeValue(attr, v);
			}
			
			//null值不应保留，以支持后续组对象的判空逻辑
			if(v == null)
				delete re[attr.name];
			else
				re[attr.name] = v;
		};
		
		delete re[po.rootPluginObjectAttrName];
		
		return re;
	};
	
	//树组件Model转换为图表属性值，另参考po.encodeChartAttrValueTreeModel()函数
	po.decodeChartAttrValueTreeModel = function(inputAttr, value)
	{
		if(value == null)
			return value;
		
		var isTreeSelect = (inputAttr.inputPayload && inputAttr.inputPayload.treeSelect == true);
		
		if(!isTreeSelect)
			return value;
		
		var isArray = inputAttr.array;
		var isMultiple = (inputAttr.inputPayload && inputAttr.inputPayload.multiple == true);
		
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
	
	po.trimChartAttrValueArray = function(inputAttr, value)
	{
		if(value == null)
			return value;
		
		if(!$.isArray(value))
		{
			if(inputAttr.inputPayload && inputAttr.inputPayload.multiple == true)
				value = [ value ];
			
			if(inputAttr.array)
				value = [ value ];
		}
		
		return value;
	};
	
	po.toChartAttrTypeValue = function(inputAttr, value)
	{
		var type = inputAttr.type;
		
		if(type != po.FormPropertyType.STRING && value === "")
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
				vi = po.toChartAttrTypeValue(inputAttr, vi);
				
				if(vi != null)
					re.push(vi);
			});
			
			return (re.length > 0 ? re : null);
		}
		else
		{
			if(type == po.FormPropertyType.BOOLEAN)
			{
				value = (value == true || value === "true" || value === "1" ? true : false);
			}
			else if(type == po.FormPropertyType.NUMBER)
			{
				value = $.parseToNumber(value);
				value = (isNaN(value) ? null : value);
			}
			
			if(value != null)
			{
				//应将值限定为待选值集合内，比如图表插件升级后inputPayload有所删减，那么这里的旧值应删除
				var inputPayload = inputAttr.inputPayload;
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
	
	po.chartAttrValuesToFormData = function(attrValues, pluginAttrForm, clone)
	{
		clone = (clone === undefined ? true : clone);
		
		var data = (attrValues || {});
		
		if(clone)
			data = $.extend(true, {}, data);
		
		if(pluginAttrForm == null || $.isEmpty(pluginAttrForm.properties))
			return data;

		var formProperties = pluginAttrForm.properties;
		for(var i=0; i<formProperties.length; i++)
		{
			var attr = formProperties[i];
			var v = data[attr.name];
			
			if(po.isObjectFormProperty(attr))
			{
				if(attr.array)
				{
					if(v == null)
						v = [];
					else if(!$.isArray(v))
						v = [ v ];
				}
				else
				{
					if(po.isRootChartPluginObjectAttr(attr))
						v = data;
					
					if(v == null)
						v = {};
					
					//也将值转化为数组结构，便于UI统一处理
					if(!$.isArray(v))
						v = [ v ];
				}
				
				for(var j=0; j<v.length; j++)
					po.chartAttrValuesToFormData(v[j], attr.children, false);
			}
			else
			{
				v = po.trimChartAttrValueArray(attr, v);
				v = po.encodeChartAttrValueTreeModel(attr, v);
			}
			
			data[attr.name] = v;
		};
		
		return data;
	};
	
	//图表属性值转换为树组件Model
	// "v0" -> { v0: true }
	// [ "v0", "v1", ... ] -> { v0: true, v1: true, ... }、[ { v0: true }, { v1: true }, ... ]
	// [ [ "v0", "v1" ], ... ] -> [ { v0: true, v1: true, ... }, ... ]
	po.encodeChartAttrValueTreeModel = function(inputAttr, value)
	{
		if(value == null)
			return value;
		
		var isTreeSelect = (inputAttr.inputPayload && inputAttr.inputPayload.treeSelect == true);
		
		if(!isTreeSelect)
			return value;
		
		value = ($.isArray(value) ? value : [ value ]);
		
		var re;
		
		if(inputAttr.array)
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
	
	po.validateChartAttrValuesRequired = function(attrs, attrValues)
	{
		if(!attrs)
			return true;
		
		attrValues = (attrValues || {});
		
		var re = true;
		
		$.each(attrs, function(i, attr)
		{
			if(attr.required && $.isEmpty(attrValues[attr.name]))
				re = false;
			
			return re;
		});
		
		return re;
	};
	
	po.vuePageModel(
	{
		FormPropertyType: po.FormPropertyType,
		FormPropertyInputType: po.FormPropertyInputType,
		chartAttrValuesForm:
		{
			groups: [],
			data: {},
			readonly: false,
			buttons: []
		}
	});
	
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
		pm.chartAttrValuesForm.pluginAttrForm = pluginAttrForm;
		pm.chartAttrValuesForm.groups = po.trimChartPluginAttrByGroup(pluginAttrForm);
		pm.chartAttrValuesForm.buttons = options.buttons;
		pm.chartAttrValuesForm.readonly = options.readonly;
		po.setChartAttrValuesFormData(attrValues);
		
		var form = po.elementOfId("${pid}chartAttrValuesForm", document.body);
		po.setupSimpleForm(form, pm.chartAttrValuesForm.data,
		{
			submitHandler: function()
			{
				if(options && options.submitHandler)
				{
					var pluginAttrForm = pm.chartAttrValuesForm.pluginAttrForm;
					var data = po.vueRaw(pm.chartAttrValuesForm.data);
					var attrValues = po.formDataToChartAttrValues(data, pluginAttrForm);
					options.submitHandler(attrValues);
				}
			}
		});
	};
	
	po.setChartAttrValuesFormData = function(attrValues)
	{
		var pm = po.vuePageModel();
		var pluginAttrForm = pm.chartAttrValuesForm.pluginAttrForm;
		var data = po.chartAttrValuesToFormData(attrValues, pluginAttrForm);
		pm.chartAttrValuesForm.data = data;
	};
	
	po.clearChartAttrValuesFormData = function()
	{
		var pm = po.vuePageModel();
		var pluginAttrForm = pm.chartAttrValuesForm.pluginAttrForm;
		var data = pm.chartAttrValuesForm.data;
		
		for(let p in data)
		{
			delete data[p];
		}
		
		po.chartAttrValuesToFormData(data, pluginAttrForm, false);
	};
	
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
					po.clearChartAttrValuesFormData();
				} 
			});
		},
		
		onChartAttrValuesFormMoveUpGrpEle: function(e, group, idx)
		{
			var groupName = group.name;
			var pm = po.vuePageModel();
			var data = pm.chartAttrValuesForm.data;
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
			var data = pm.chartAttrValuesForm.data;
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
			var data = pm.chartAttrValuesForm.data;

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
					var data = pm.chartAttrValuesForm.data;
					data[groupName].splice(idx, 1);
				}
			});
		},
		
		onChartAttrValuesFormInsertGrpEleEle: function(e, grpDataEle, attr, idx)
		{
			var propName = attr.name;
			var isTreeSelect = (attr.inputPayload && attr.inputPayload.treeSelect == true);
			
			if(grpDataEle[propName] == null)
				grpDataEle[propName] = [];
			
			if(idx == null)
				grpDataEle[propName].push(isTreeSelect ? {} : null);
			else
				grpDataEle[propName].splice(idx, 0, isTreeSelect ? {} : null);
		},
		
		onChartAttrValuesFormRemoveGrpEleEle: function(e, grpDataEle, attr, idx)
		{
			var propName = attr.name;
			
			if(grpDataEle[propName] == null)
				return;
			
			grpDataEle[propName].splice(idx, 1);
		}
	});
})
(${pid});
</script>
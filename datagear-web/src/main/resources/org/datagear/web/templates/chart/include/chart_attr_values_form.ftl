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
<#assign ChartPluginInputAttrType=statics['org.datagear.analysis.ChartPluginInputAttribute$DataType']>
<#assign ChartPluginInputAttrInputType=statics['org.datagear.analysis.ChartPluginInputAttribute$InputType']>
<form id="${pid}chartAttrValuesForm" class="chart-attr-values-form flex flex-column" :class="{readonly: pm.chartAttrValuesForm.readonly}">
	<div class="page-form-content flex-grow-1 px-2 py-1 overflow-y-auto">
		<p-accordion :multiple="true" :active-index="[0]">
			<p-accordion-tab v-for="(group, groupIdx) in pm.chartAttrValuesForm.groups"
				:header="group.nameLabel.value">
				<div class="flex flex-column gap-2">
					<p-panel v-for="(grpDataEle, grpDataEleIdx) in pm.chartAttrValuesForm.data[group.name]"
						:class="{ 'disable-p-panel': !group.array, 'p-card': group.array }" :header="group.nameLabel.value+' #'+(grpDataEleIdx+1)"
						:toggleable="group.array" class="no-panel-border">
						<template #icons>
							<div class="inline-flex gap-1 mx-2 vertical-align-top text-sm" v-if="group.array && !pm.chartAttrValuesForm.readonly">
								<p-button type="button" severity="secondary"
									@click="">
									<@spring.message code='moveUp' />
								</p-button>
								<p-button type="button" severity="secondary"
									@click="">
									<@spring.message code='moveDown' />
								</p-button>
								<p-button type="button" severity="secondary"
									@click="">
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
							</label>
							<div class="field-input col-12" v-if="attr.inputType == pm.ChartPluginInputAttribute.InputType.RADIO">
								<div class="input p-inputtext p-component p-2">
									<div v-for="(ip, ipIdx) in attr.inputPayload.options" class="inline-block mr-2">
										<p-radiobutton :input-id="attr.domId+'_'+groupIdx+'_'+grpDataEleIdx+'_'+ipIdx"
											:value="ip.value" v-model="grpDataEle[attr.name]">
										</p-radiobutton>
										<label :for="attr.domId+'_'+groupIdx+'_'+grpDataEleIdx+'_'+ipIdx" class="ml-1">{{ip.name}}</label>
									</div>
								</div>
					        	<div class="validate-msg" v-if="attr.required">
					        		<input :name="attr.name" required type="text" class="validate-proxy" />
					        	</div>
							</div>
							<div class="field-input col-12" v-else-if="attr.inputType == pm.ChartPluginInputAttribute.InputType.SELECT">
								<div v-if="attr.inputPayload.multiple == true">
									<div v-if="attr.inputPayload.treeSelect == true">
										<p-treeselect v-model="grpDataEle[attr.name]" :options="attr.inputPayload.options"
											selection-mode="multiple" class="input w-full" placeholder="<@spring.message code='none' />">
										</p-treeselect>
									</div>
									<div v-else>
										<p-multiselect v-model="grpDataEle[attr.name]" :options="attr.inputPayload.options"
											option-label="name" option-value="value" :show-clear="true" class="input w-full">
										</p-multiselect>
									</div>
								</div>
								<div class="input border-1px-transparent p-inputtext p-component px-0 py-0"
									v-else-if="attr.array">
									<div v-for="(sv, svIdx) in grpDataEle[attr.name]" :key="svIdx">
										<div class="flex mb-1 gap-2">
											<div class="flex-grow-1 flex">
												<p-treeselect v-model="grpDataEle[attr.name][svIdx]" :options="attr.inputPayload.options"
													class="input w-full" placeholder="<@spring.message code='none' />" v-if="attr.inputPayload.treeSelect == true">
												</p-treeselect>
												<p-dropdown v-model="grpDataEle[attr.name][svIdx]" :options="attr.inputPayload.options"
													option-label="name" option-value="value" class="input flex-grow-1 mr-1" v-else>
												</p-dropdown>
											</div>
											<div class="flex gap-1">
												<p-button type="button" icon="pi pi-plus" severity="secondary"
													@click="onChartAttrValuesFormInsertGrpEleEle($event, grpDataEle, attr, svIdx)"
													v-if="!pm.chartAttrValuesForm.readonly">
												</p-button>
												<p-button type="button" icon="pi pi-minus" severity="danger"
													@click="onChartAttrValuesFormRemoveGrpEleEle($event, grpDataEle, attr, svIdx)"
													v-if="!pm.chartAttrValuesForm.readonly">
												</p-button>
											</div>
										</div>
									</div>
									<div class="mt-1" v-if="!pm.chartAttrValuesForm.readonly">
										<p-button type="button" icon="pi pi-plus" severity="secondary" @click="onChartAttrValuesFormInsertGrpEleEle($event, grpDataEle, attr)"></p-button>
									</div>
								</div>
								<div v-else>
									<div v-if="attr.inputPayload.treeSelect == true">
										<p-treeselect v-model="grpDataEle[attr.name]" :options="attr.inputPayload.options"
											class="input w-full" placeholder="<@spring.message code='none' />">
										</p-treeselect>
									</div>
									<div v-else>
										<p-dropdown v-model="grpDataEle[attr.name]" :options="attr.inputPayload.options"
											option-label="name" option-value="value" :show-clear="!attr.required" class="input w-full">
										</p-dropdown>
									</div>
								</div>
					        	<div class="validate-msg" v-if="attr.required">
					        		<input :name="attr.name" required type="text" class="validate-proxy" />
					        	</div>
							</div>
							<div class="field-input col-12" v-else-if="attr.inputType == pm.ChartPluginInputAttribute.InputType.CHECKBOX">
								<div class="input p-inputtext p-component p-2">
									<div v-for="(ip, ipIdx) in attr.inputPayload.options" class="inline-block mr-2">
										<p-checkbox :input-id="attr.domId+'_'+groupIdx+'_'+grpDataEleIdx+'_'+ipIdx"
											:value="ip.value" v-model="grpDataEle[attr.name]">
										</p-checkbox>
										<label :for="attr.domId+'_'+groupIdx+'_'+grpDataEleIdx+'_'+ipIdx" class="ml-1">{{ip.name}}</label>
									</div>
								</div>
					        	<div class="validate-msg" v-if="attr.required">
					        		<input :name="attr.name" required type="text" class="validate-proxy" />
					        	</div>
							</div>
							<div class="field-input col-12" v-else-if="attr.inputType == pm.ChartPluginInputAttribute.InputType.COLOR">
								<div class="input border-1px-transparent p-inputtext p-component px-0 py-0" v-if="attr.array">
									<div v-for="(color, colorIdx) in grpDataEle[attr.name]" :key="colorIdx">
										<div class="flex mb-1 gap-2">
											<div class="flex-grow-1 flex">
												<p-inputtext v-model="grpDataEle[attr.name][colorIdx]" type="text"
													class="input flex-grow-1 mr-1">
												</p-inputtext>
												<p-button type="button" :style="{'background-color': grpDataEle[attr.name][colorIdx]}" class="palette-btn surface-border mr-1"
													@click="showPalettePanel($event, grpDataEle[attr.name], colorIdx)"></p-button>
											</div>
											<div class="flex gap-1">
												<p-button type="button" icon="pi pi-plus" severity="secondary"
													@click="onChartAttrValuesFormInsertGrpEleEle($event, grpDataEle, attr, colorIdx)"
													v-if="!pm.chartAttrValuesForm.readonly">
												</p-button>
												<p-button type="button" icon="pi pi-minus" severity="danger"
													@click="onChartAttrValuesFormRemoveGrpEleEle($event, grpDataEle, attr, colorIdx)"
													v-if="!pm.chartAttrValuesForm.readonly">
												</p-button>
											</div>
										</div>
									</div>
									<div class="mt-1" v-if="!pm.chartAttrValuesForm.readonly">
										<p-button type="button" icon="pi pi-plus" severity="secondary" @click="onChartAttrValuesFormInsertGrpEleEle($event, grpDataEle, attr)"></p-button>
									</div>
								</div>
								<div class="flex" v-else>
									<p-inputtext v-model="grpDataEle[attr.name]" type="text"
										class="input flex-grow-1 mr-1" maxlength="100">
									</p-inputtext>
									<p-button type="button" :style="{'background-color': grpDataEle[attr.name]}" class="palette-btn surface-border"
										@click="showPalettePanel($event, grpDataEle, attr.name)"></p-button>
								</div>
					        	<div class="validate-msg" v-if="attr.required">
					        		<input :name="attr.name" required type="text" class="validate-proxy" />
					        	</div>
							</div>
							<div class="field-input col-12" v-else-if="attr.inputType == pm.ChartPluginInputAttribute.InputType.TEXTAREA">
								<p-textarea :id="attr.domId+'_'+groupIdx+'_'+grpDataEleIdx"
									v-model="grpDataEle[attr.name]" type="text" class="input w-full">
								</p-textarea>
					        	<div class="validate-msg" v-if="attr.required">
					        		<input :name="attr.name" required type="text" class="validate-proxy" />
					        	</div>
							</div>
							<div class="field-input col-12" v-else>
								<p-inputtext :id="attr.domId+'_'+groupIdx+'_'+grpDataEleIdx"
									v-model="grpDataEle[attr.name]" type="text" class="input w-full">
								</p-inputtext>
					        	<div class="validate-msg" v-if="attr.required">
					        		<input :name="attr.name" required type="text" class="validate-proxy" />
					        	</div>
							</div>
						</div>
					</p-panel>
					<div class="text-sm" v-if="group.array && !pm.chartAttrValuesForm.readonly">
						<p-button type="button" icon="pi pi-plus" :label="group.nameLabel.value"
							severity="secondary" @click="onChartAttrValuesFormInsertGrpEle($event, group)">
						</p-button>
					</div>
				</div>
			</p-accordion-tab>
		</p-accordion>
	</div>
	<div class="page-form-foot flex-grow-0 flex justify-content-center gap-2 pt-2">
		<p-button type="submit" label="<@spring.message code='confirm' />"></p-button>
		<p-button type="button" label="<@spring.message code='clear' />" severity="secondary" @click="onClearChartAttrValuesFormData"></p-button>
		
		<p-button v-for="(btn, btnIdx) in pm.chartAttrValuesForm.buttons" :key="btnIdx"
			type="button" class="p-button-secondary" :label="btn.name" @click="btn.clickHandler">
		</p-button>
	</div>
</form>
<script>
(function(po)
{
	po.ChartPluginInputAttribute =
	{
		DataType:
		{
			STRING: "${ChartPluginInputAttrType.STRING}",
			BOOLEAN: "${ChartPluginInputAttrType.BOOLEAN}",
			NUMBER: "${ChartPluginInputAttrType.NUMBER}"
		},
		InputType:
		{
			TEXT: "${ChartPluginInputAttrInputType.TEXT}",
			SELECT: "${ChartPluginInputAttrInputType.SELECT}",
			RADIO: "${ChartPluginInputAttrInputType.RADIO}",
			CHECKBOX: "${ChartPluginInputAttrInputType.CHECKBOX}",
			TEXTAREA: "${ChartPluginInputAttrInputType.TEXTAREA}",
			COLOR: "${ChartPluginInputAttrInputType.COLOR}"
		},
		InputPayload:
		{
			//多选
			MULTIPLE: "multiple",
			//地图
			DG_MAP: "DG_MAP"
		},
		//5.5.0旧版的下拉框inputPayload.multiple="repeat"值，表示可重复选取
		MultipleRepeat: "repeat"
	};
	
	//根分组的name值，其包含的属性值应直接保存至根属性值对象下
	po.rootChartPluginGroupAttrName = "${pid}RootCpgAttrName";
	
	po.isChartPluginGroupAttr = function(attr)
	{
		return (attr != null && attr.children !== undefined);
	};
	
	po.chartPluginAttributeDomIdIdx = 0;

	po.trimChartPluginAttrByGroup = function(attrs)
	{
		attrs = po.trimChartPluginAttributes(attrs);
		
		var groups = [];
		
		for(var i=0; i<attrs.length; i++)
		{
			var attr = attrs[i];
			
			if(po.isChartPluginGroupAttr(attr))
			{
				groups.push(attr);
				continue;
			}
			
			//无分组的，建立虚拟分组，统一结构、易于处理
			var virtualGroup = {};
			
			//处理5.0.0旧版的org.datagear.analysis.ChartPluginInputAttribute.group
			if(attr.group != null)
				virtualGroup = attr.group;
			
			//没有定义分组，如果末尾是【未分组】，则使用；否则，新建【未分组】
			if($.isEmpty(virtualGroup.name))
			{
				var groupTail = (groups.length > 0 ? groups[groups.length - 1] : null);
				
				if(groupTail && po.isVirtualChartPluginGroupAttr(groupTail) && groupTail.virtualName == "")
				{
					virtualGroup = groupTail;
				}
				else
				{
					virtualGroup = { name: po.rootChartPluginGroupAttrName, children: [], virtual: true, virtualName: "" };
					groups.push(virtualGroup);
				}
			}
			//有分组，查找或新建
			else
			{
				var idx = po.findVirtualChartPluginAttrIdxByName(groups, virtualGroup.name);
				if(idx >= 0)
				{
					virtualGroup = groups[idx];
				}
				else
				{
					virtualGroup =
					{
						name: po.rootChartPluginGroupAttrName,
						nameLabel: virtualGroup.nameLabel, children: [],
						virtual: true, virtualName: virtualGroup.name
					};
					
					groups.push(virtualGroup);
				}
			}
			
			if(!virtualGroup.nameLabel || $.isEmpty(virtualGroup.nameLabel.value))
			{
				var nameLabelValue = ($.isEmpty(virtualGroup.virtualName) ? "<@spring.message code='ungrouped' />" : virtualGroup.virtualName);
				virtualGroup.nameLabel = { value: nameLabelValue };
			}
			
			po.trimChartPluginGroupAttr(virtualGroup);
			virtualGroup.children.push(attr);
		};
		
		return groups;
	};
	
	po.trimChartPluginAttrNameLabel = function(attr)
	{
		if(!attr)
			return;
		
		if(attr.nameLabel && attr.nameLabel.value)
			return;
		
		attr.nameLabel = { value: attr.name };
	};
	
	po.isVirtualChartPluginGroupAttr = function(groupAttr)
	{
		return (groupAttr != null && groupAttr.virtual);
	};
	
	po.findVirtualChartPluginAttrIdxByName = function(groups, virtualName)
	{
		for(var i=0; i<groups.length; i++)
		{
			if(po.isVirtualChartPluginGroupAttr(groups[i]) && groups[i].virtualName == virtualName)
			{
				return i;
			}
		}
		
		return -1;
	};
	
	po.trimChartPluginAttributes = function(attrs, clone)
	{
		attrs = (attrs == null ? [] : attrs);
		clone = (clone === undefined ? true : clone);
		
		if(clone)
			attrs = $.extend(true, [], attrs);
		
		for(var i=0; i<attrs.length; i++)
		{
			var attr = attrs[i];
			
			po.trimChartPluginAttrNameLabel(attr);
			attr.domId = po.concatPid("cpattr_"+ (po.chartPluginAttributeDomIdIdx++));
			
			if(po.isChartPluginGroupAttr(attr))
			{
				po.trimChartPluginGroupAttr(attr);
				po.trimChartPluginAttributes(attr.children, false);
				continue;
			}
			
			//布尔型默认作为RADIO处理
			if(attr.type == po.ChartPluginInputAttribute.DataType.BOOLEAN)
			{
				if(!attr.inputType)
					attr.inputType = po.ChartPluginInputAttribute.InputType.RADIO;
				
				if(!attr.inputPayload)
				{
					var pm = po.vuePageModel();
					attr.inputPayload = po.vueRaw(pm.booleanOptions);
				}
			}
			
			var inputType = attr.inputType;
			
			//下拉框、单选、复选框：将inputPayload转换为{multiple: ..., options: [{name: ..., value: ...}, ...]}格式
			if(inputType == po.ChartPluginInputAttribute.InputType.SELECT
					|| inputType == po.ChartPluginInputAttribute.InputType.RADIO
					|| inputType == po.ChartPluginInputAttribute.InputType.CHECKBOX)
			{
				var inputPayload = (attr.inputPayload || []);
				
				//数组、"DG_MAP"：转换为{ multiple: false, options: ... }格式
				if($.isArray(inputPayload) || (inputPayload == po.ChartPluginInputAttribute.InputPayload.DG_MAP))
					inputPayload = { multiple: false, options: inputPayload };
				
				//{ options: "DG_MAP" }：转换为实际地图数据options
				po.trimChartPluginInputAttrInputPayloadIfMap(attr, inputPayload);
				
				//默认multiple为false
				inputPayload.multiple = (inputPayload.multiple == null ? false : inputPayload.multiple);
				po.trimChartPluginInputAttrInputOptions(attr, inputPayload);
				
				if(inputType == po.ChartPluginInputAttribute.InputType.RADIO)
				{
					inputPayload.multiple = false;
				}
				else if(inputType == po.ChartPluginInputAttribute.InputType.CHECKBOX)
				{
					inputPayload.multiple = true;
				}
				
				attr.inputPayload = inputPayload;
			}
			//颜色框：将inputPayload转换为标准的{multiple: ...}格式
			else if(inputType == po.ChartPluginInputAttribute.InputType.COLOR)
			{
				var inputPayload = attr.inputPayload;
				
				//null
				if(inputPayload == null)
				{
					inputPayload = { multiple: false };
				}
				//"multiple"
				else if($.isTypeString(inputPayload))
				{
					inputPayload = { multiple: (inputPayload == po.ChartPluginInputAttribute.InputPayload.MULTIPLE) };
				}
				//不支持数值、布尔型、数组
				else if($.isTypeNumber(inputPayload) || $.isTypeBoolean(inputPayload) || $.isArray(inputPayload))
				{
					inputPayload = { multiple: false };
				}
				//{...}
				else
				{
					inputPayload.multiple = (inputPayload.multiple == null ? false : true);
				}
				
				attr.inputPayload = inputPayload;
			}
			
			//将5.5.0旧版的{inputPayload: {multiple: "repeat"}}格式转换为6.0新版的{array: true, inputPayload: {multiple: false}}
			if(attr.inputPayload && attr.inputPayload.multiple === po.ChartPluginInputAttribute.MultipleRepeat)
			{
				attr.array = true;
				attr.inputPayload.multiple = false;
			}
			
			//将5.5.0旧版的颜色输入框{inputPayload: {multiple: true}}格式转换为6.0新版的{array: true}
			if(attr.inputType == po.ChartPluginInputAttribute.InputType.COLOR
					&& attr.inputPayload && attr.inputPayload.multiple === true)
			{
				attr.array = true;
			}
		};
		
		return attrs;
	};
	
	po.trimChartPluginGroupAttr = function(groupAttr)
	{
		if(!po.isChartPluginGroupAttr(groupAttr))
			return;
		
		//无name的分组不允许启用array=true特性，因为分组包含的属性值无法存储为对象数组
		if($.isEmpty(groupAttr.name))
		{
			groupAttr.name = po.rootChartPluginGroupAttrName;
			groupAttr.array = false;
		}
	};
	
	po.trimChartPluginInputAttrInputPayloadIfMap = function(inputAttr, inputPayload)
	{
		var options = inputPayload.options;
		
		//内置地图
		if(options == po.ChartPluginInputAttribute.InputPayload.DG_MAP)
		{
			//只有下拉列表才使用树形结构，单选框、复选框只能使用平铺数组
			if(inputPayload.treeSelect == null
					&& inputAttr.inputType == po.ChartPluginInputAttribute.InputType.SELECT)
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
	
	//整理图表属性值：类型转换、选项值限定
	po.toTrimChartAttrValues = function(attrValues, groupedAttrs, clone)
	{
		clone = (clone === undefined ? true : clone);
		
		//注意：attrValues中对于没有在groupedAttrs定义的属性值应原样保留，
		//因为看板的dg-chart-attr-values应允许定义图表插件属性之外的扩展值
		
		if(attrValues == null || $.isEmpty(groupedAttrs))
			return attrValues;
		
		//要先清除循环引用，复制完后再恢复
		var rootObjRef = attrValues[po.rootChartPluginGroupAttrName];
		if(rootObjRef !== undefined)
		{
			delete attrValues[po.rootChartPluginGroupAttrName];
		}
		
		var re = (clone ? $.extend(true, {}, attrValues) : attrValues);
		
		if(rootObjRef !== undefined)
		{
			attrValues[po.rootChartPluginGroupAttrName] = rootObjRef;
			re[po.rootChartPluginGroupAttrName] = re;
		}
		
		for(var i=0; i<groupedAttrs.length; i++)
		{
			var attr = groupedAttrs[i];
			var v = re[attr.name];
			
			if(v == null)
			{
			}
			else if(po.isChartPluginGroupAttr(attr))
			{
				v = ($.isArray(v) ? v : [ v ]);
				
				for(var j=0; j<v.length; j++)
					po.toTrimChartAttrValues(v[j], attr.children, false);
				
				if(!attr.array)
				{
					v = v[0];
					
					//删除由po.toChartAttrValuesFormData()生成的空对象
					if(v != null && $.isEmptyObject(v))
						v = null;
				}
				else
				{
					//删除由po.toChartAttrValuesFormData()生成的空数组
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
			
			if(v == null)
			{
				//null值不应保留，以支持后续组对象的判空逻辑
				delete re[attr.name];
			}
			else
			{
				re[attr.name] = v;
			}
		};
		
		delete re[po.rootChartPluginGroupAttrName];
		
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
			
			if(!inputAttr.array)
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
		
		if(type != po.ChartPluginInputAttribute.DataType.STRING && value === "")
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
			if(type == po.ChartPluginInputAttribute.DataType.BOOLEAN)
			{
				value = (value == true || value === "true" || value === "1" ? true : false);
			}
			else if(type == po.ChartPluginInputAttribute.DataType.NUMBER)
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
	
	po.toChartAttrValuesFormData = function(attrValues, attrs, clone)
	{
		clone = (clone === undefined ? true : clone);
		
		var data = (attrValues || {});
		
		if(clone)
			data = $.extend(true, {}, data);
		
		if($.isEmpty(attrs))
			return data;
		
		for(var i=0; i<attrs.length; i++)
		{
			var attr = attrs[i];
			var v = data[attr.name];
			
			if(po.isChartPluginGroupAttr(attr))
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
					if(attr.name == po.rootChartPluginGroupAttrName)
						v = data;
					
					if(v == null)
						v = {};
					
					//也将值转化为数组结构，便于UI统一处理
					if(!$.isArray(v))
						v = [ v ];
				}
				
				for(var j=0; j<v.length; j++)
					po.toChartAttrValuesFormData(v[j], attr.children, false);
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
			if(attr.required && $.isEmptyValue(attrValues[attr.name]))
				re = false;
			
			return re;
		});
		
		return re;
	};
	
	po.vuePageModel(
	{
		ChartPluginInputAttribute: po.ChartPluginInputAttribute,
		chartAttrValuesForm:
		{
			groups: [],
			data: {},
			readonly: false,
			buttons: []
		}
	});
	
	po.setupChartAttrValuesForm = function(attrs, attrValues, options)
	{
		options = $.extend(
		{
			submitHandler: null,
			buttons: [],
			readonly: false
		},
		options);
		
		var pm = po.vuePageModel();
		pm.chartAttrValuesForm.groups = po.trimChartPluginAttrByGroup(attrs);
		pm.chartAttrValuesForm.buttons = options.buttons;
		pm.chartAttrValuesForm.readonly = options.readonly;
		po.setChartAttrValuesFormData(attrValues);
		
		var validateRules = {};
		
		for(var i=0; i<attrs.length; i++)
		{
			var attr = attrs[i];
			if(attr.type == po.ChartPluginInputAttribute.DataType.NUMBER)
				validateRules[attr.name] = { "number": true };
		};
		
		var form = po.elementOfId("${pid}chartAttrValuesForm", document.body);
		po.setupSimpleForm(form, pm.chartAttrValuesForm.data,
		{
			rules: validateRules,
			submitHandler: function()
			{
				if(options && options.submitHandler)
				{
					var groups = pm.chartAttrValuesForm.groups;
					var data = po.vueRaw(pm.chartAttrValuesForm.data);
					var attrValues = po.toTrimChartAttrValues(data, groups);
					options.submitHandler(attrValues);
				}
			}
		});
	};
	
	po.setChartAttrValuesFormData = function(attrValues)
	{
		var pm = po.vuePageModel();
		var groups = pm.chartAttrValuesForm.groups;
		var data = po.toChartAttrValuesFormData(attrValues, groups);
		pm.chartAttrValuesForm.data = data;
	};
	
	po.vueMethod(
	{
		onClearChartAttrValuesFormData: function()
		{
			po.confirm(
			{
				message: "<@spring.message code='confirmClearAllChartAttr' />",
				accept: function()
				{
					po.setChartAttrValuesFormData({});
				} 
			});
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
			var groupName = group.name;
			var pm = po.vuePageModel();
			var data = pm.chartAttrValuesForm.data;
			data[groupName].splice(idx, 1);
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
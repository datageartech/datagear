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
					<p-panel :class="{ 'disable-p-panel': !group.array, 'p-card': group.array }" :header="'#1'"
						:toggleable="group.array" class="no-panel-border">
						<template #icons>
							<div class="inline-flex gap-1 mx-2 vertical-align-top text-xs" v-if="group.array && !pm.chartAttrValuesForm.readonly">
								<p-button type="button" severity="secondary"
									@click="onMoveUpDataSetBind($event, dsbIdx)">
									<@spring.message code='moveUp' />
								</p-button>
								<p-button type="button" severity="secondary"
									@click="onMoveDownDataSetBind($event, dsbIdx)">
									<@spring.message code='moveDown' />
								</p-button>
								<p-button type="button" severity="secondary"
									@click="onChartAttrValuesFormInsertValue($event, cpa.name, svIdx)">
									<@spring.message code='insert' />
								</p-button>
								<p-button type="button" severity="danger"
									@click="onChartAttrValuesFormRemoveValue($event, cpa.name, svIdx)">
									<@spring.message code='delete' />
								</p-button>
							</div>
						</template>
						<div>
							<div class="field grid" v-for="(cpa, cpaIdx) in group.children">
								<label :for="cpa.domId" class="field-label col-12 mb-2"
									:title="cpa.descLabel && cpa.descLabel.value ? cpa.descLabel.value : null">
									<span>{{cpa.nameLabel.value}}</span>
								</label>
								<div class="field-input col-12" v-if="cpa.inputType == pm.ChartPluginInputAttribute.InputType.RADIO">
									<div class="input p-inputtext p-component p-2">
										<div v-for="(ip, ipIdx) in cpa.inputPayload.options" class="inline-block mr-2">
											<p-radiobutton :input-id="cpa.domId+ipIdx" :value="ip.value" v-model="pm.chartAttrValuesForm.attrValues[cpa.name]"></p-radiobutton>
											<label :for="cpa.domId+ipIdx" class="ml-1">{{ip.name}}</label>
										</div>
									</div>
						        	<div class="validate-msg" v-if="cpa.required">
						        		<input :name="cpa.name" required type="text" class="validate-proxy" />
						        	</div>
								</div>
								<div class="field-input col-12" v-else-if="cpa.inputType == pm.ChartPluginInputAttribute.InputType.SELECT">
									<div v-if="cpa.inputPayload.multiple == true">
										<div v-if="cpa.inputPayload.treeSelect == true">
											<p-treeselect :id="cpa.domId" v-model="pm.chartAttrValuesForm.attrValues[cpa.name]" :options="cpa.inputPayload.options"
												selection-mode="multiple" class="input w-full" placeholder="<@spring.message code='none' />">
											</p-treeselect>
										</div>
										<div v-else>
											<p-multiselect :id="cpa.domId" v-model="pm.chartAttrValuesForm.attrValues[cpa.name]" :options="cpa.inputPayload.options"
												option-label="name" option-value="value" :show-clear="true" class="input w-full">
											</p-multiselect>
										</div>
									</div>
									<div class="input border-1px-transparent p-inputtext p-component px-0 py-0"
										v-else-if="cpa.array">
										<div v-for="(sv, svIdx) in pm.chartAttrValuesForm.attrValues[cpa.name]" :key="svIdx">
											<div class="flex mb-1 gap-2">
												<div class="flex-grow-1 flex">
													<p-treeselect :id="cpa.domId" v-model="pm.chartAttrValuesForm.attrValues[cpa.name]" :options="cpa.inputPayload.options"
														class="input w-full" placeholder="<@spring.message code='none' />" v-if="cpa.inputPayload.treeSelect == true">
													</p-treeselect>
													<p-dropdown :id="cpa.domId+svIdx" v-model="pm.chartAttrValuesForm.attrValues[cpa.name][svIdx]" :options="cpa.inputPayload.options"
														option-label="name" option-value="value" class="input flex-grow-1 mr-1" v-else>
													</p-dropdown>
												</div>
												<div class="flex gap-1">
													<p-button type="button" icon="pi pi-plus" severity="secondary"
														@click="onChartAttrValuesFormInsertValue($event, cpa.name, svIdx)"
														v-if="!pm.chartAttrValuesForm.readonly">
													</p-button>
													<p-button type="button" icon="pi pi-minus" severity="danger"
														@click="onChartAttrValuesFormRemoveValue($event, cpa.name, svIdx)"
														v-if="!pm.chartAttrValuesForm.readonly">
													</p-button>
												</div>
											</div>
										</div>
										<div class="mt-1" v-if="!pm.chartAttrValuesForm.readonly">
											<p-button type="button" icon="pi pi-plus" severity="secondary" @click="onChartAttrValuesFormInsertValue($event, cpa.name)"></p-button>
										</div>
									</div>
									<div v-else>
										<div v-if="cpa.inputPayload.treeSelect == true">
											<p-treeselect :id="cpa.domId" v-model="pm.chartAttrValuesForm.attrValues[cpa.name]" :options="cpa.inputPayload.options"
												class="input w-full" placeholder="<@spring.message code='none' />">
											</p-treeselect>
										</div>
										<div v-else>
											<p-dropdown :id="cpa.domId" v-model="pm.chartAttrValuesForm.attrValues[cpa.name]" :options="cpa.inputPayload.options"
												option-label="name" option-value="value" :show-clear="!cpa.required" class="input w-full">
											</p-dropdown>
										</div>
									</div>
						        	<div class="validate-msg" v-if="cpa.required">
						        		<input :name="cpa.name" required type="text" class="validate-proxy" />
						        	</div>
								</div>
								<div class="field-input col-12" v-else-if="cpa.inputType == pm.ChartPluginInputAttribute.InputType.CHECKBOX">
									<div class="input p-inputtext p-component p-2">
										<div v-for="(ip, ipIdx) in cpa.inputPayload.options" class="inline-block mr-2">
											<p-checkbox :input-id="cpa.domId+ipIdx" :value="ip.value" v-model="pm.chartAttrValuesForm.attrValues[cpa.name]"></p-checkbox>
											<label :for="cpa.domId+ipIdx" class="ml-1">{{ip.name}}</label>
										</div>
									</div>
						        	<div class="validate-msg" v-if="cpa.required">
						        		<input :name="cpa.name" required type="text" class="validate-proxy" />
						        	</div>
								</div>
								<div class="field-input col-12" v-else-if="cpa.inputType == pm.ChartPluginInputAttribute.InputType.COLOR">
									<div class="input border-1px-transparent p-inputtext p-component px-0 py-0" v-if="cpa.array">
										<div v-for="(color, colorIdx) in pm.chartAttrValuesForm.attrValues[cpa.name]" :key="colorIdx">
											<div class="flex mb-1 gap-2">
												<div class="flex-grow-1 flex">
													<p-inputtext :id="cpa.domId+colorIdx" v-model="pm.chartAttrValuesForm.attrValues[cpa.name][colorIdx]" type="text"
														class="input flex-grow-1 mr-1">
													</p-inputtext>
													<p-button type="button" :style="{'background-color': pm.chartAttrValuesForm.attrValues[cpa.name][colorIdx]}" class="palette-btn surface-border mr-1"
														@click="showPalettePanel($event, pm.chartAttrValuesForm.attrValues[cpa.name], colorIdx)"></p-button>
												</div>
												<div class="flex gap-1">
													<p-button type="button" icon="pi pi-plus" severity="secondary"
														@click="onChartAttrValuesFormInsertValue($event, cpa.name, colorIdx)"
														v-if="!pm.chartAttrValuesForm.readonly">
													</p-button>
													<p-button type="button" icon="pi pi-minus" severity="danger"
														@click="onChartAttrValuesFormRemoveValue($event, cpa.name, colorIdx)"
														v-if="!pm.chartAttrValuesForm.readonly">
													</p-button>
												</div>
											</div>
										</div>
										<div class="mt-1" v-if="!pm.chartAttrValuesForm.readonly">
											<p-button type="button" icon="pi pi-plus" severity="secondary" @click="onChartAttrValuesFormInsertValue($event, cpa.name)"></p-button>
										</div>
									</div>
									<div class="flex" v-else>
										<p-inputtext :id="cpa.domId" v-model="pm.chartAttrValuesForm.attrValues[cpa.name]" type="text"
											class="input flex-grow-1 mr-1" maxlength="100">
										</p-inputtext>
										<p-button type="button" :style="{'background-color': pm.chartAttrValuesForm.attrValues[cpa.name]}" class="palette-btn surface-border"
											@click="showPalettePanel($event, pm.chartAttrValuesForm.attrValues, cpa.name)"></p-button>
									</div>
						        	<div class="validate-msg" v-if="cpa.required">
						        		<input :name="cpa.name" required type="text" class="validate-proxy" />
						        	</div>
								</div>
								<div class="field-input col-12" v-else-if="cpa.inputType == pm.ChartPluginInputAttribute.InputType.TEXTAREA">
									<p-textarea :id="cpa.domId" v-model="pm.chartAttrValuesForm.attrValues[cpa.name]" type="text"
										class="input w-full">
									</p-textarea>
						        	<div class="validate-msg" v-if="cpa.required">
						        		<input :name="cpa.name" required type="text" class="validate-proxy" />
						        	</div>
								</div>
								<div class="field-input col-12" v-else>
									<p-inputtext :id="cpa.domId" v-model="pm.chartAttrValuesForm.attrValues[cpa.name]" type="text"
										class="input w-full">
									</p-inputtext>
						        	<div class="validate-msg" v-if="cpa.required">
						        		<input :name="cpa.name" required type="text" class="validate-proxy" />
						        	</div>
								</div>
							</div>
						</div>
					</p-panel>
				</div>
			</p-accordion-tab>
		</p-accordion>
	</div>
	<div class="page-form-foot flex-grow-0 flex justify-content-center gap-2 pt-2">
		<p-button type="submit" label="<@spring.message code='confirm' />"></p-button>
		
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
	
	po.isChartPluginGroupAttr = function(attr)
	{
		return (attr != null && attr.children !== undefined);
	};
	
	po.chartPluginAttributeDomIdIdx = 0;
	
	po.trimChartPluginAttributes = function(cpas, clone)
	{
		cpas = (cpas == null ? [] : cpas);
		clone = (clone === undefined ? true : clone);
		
		if(clone)
			cpas = $.extend(true, [], cpas);
		
		for(var i=0; i<cpas.length; i++)
		{
			var cpa = cpas[i];
			
			po.trimChartPluginAttrNameLabel(cpa);
			cpa.domId = po.concatPid("cpattr_"+ (po.chartPluginAttributeDomIdIdx++)+"_");
			
			if(po.isChartPluginGroupAttr(cpa))
			{
				po.trimChartPluginAttributes(cpa.children, false);
				continue;
			}
			
			//布尔型默认作为RADIO处理
			if(cpa.type == po.ChartPluginInputAttribute.DataType.BOOLEAN)
			{
				if(!cpa.inputType)
					cpa.inputType = po.ChartPluginInputAttribute.InputType.RADIO;
				
				if(!cpa.inputPayload)
				{
					var pm = po.vuePageModel();
					cpa.inputPayload = po.vueRaw(pm.booleanOptions);
				}
			}
			
			var inputType = cpa.inputType;
			
			//下拉框、单选、复选框：将inputPayload转换为{multiple: ..., options: [{name: ..., value: ...}, ...]}格式
			if(inputType == po.ChartPluginInputAttribute.InputType.SELECT
					|| inputType == po.ChartPluginInputAttribute.InputType.RADIO
					|| inputType == po.ChartPluginInputAttribute.InputType.CHECKBOX)
			{
				var inputPayload = (cpa.inputPayload || []);
				
				//数组、"DG_MAP"：转换为{ multiple: false, options: ... }格式
				if($.isArray(inputPayload) || (inputPayload == po.ChartPluginInputAttribute.InputPayload.DG_MAP))
					inputPayload = { multiple: false, options: inputPayload };
				
				//{ options: "DG_MAP" }：转换为实际地图数据options
				po.trimChartPluginInputAttrInputPayloadIfMap(cpa, inputPayload);
				
				//默认multiple为false
				inputPayload.multiple = (inputPayload.multiple == null ? false : inputPayload.multiple);
				po.trimChartPluginInputAttrInputOptions(cpa, inputPayload);
				
				if(inputType == po.ChartPluginInputAttribute.InputType.RADIO)
				{
					inputPayload.multiple = false;
				}
				else if(inputType == po.ChartPluginInputAttribute.InputType.CHECKBOX)
				{
					inputPayload.multiple = true;
				}
				
				cpa.inputPayload = inputPayload;
			}
			//颜色框：将inputPayload转换为标准的{multiple: ...}格式
			else if(inputType == po.ChartPluginInputAttribute.InputType.COLOR)
			{
				var inputPayload = cpa.inputPayload;
				
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
				
				cpa.inputPayload = inputPayload;
			}
			
			//将5.5.0旧版的{inputPayload: {multiple: "repeat"}}格式转换为6.0新版的{array: true, inputPayload: {multiple: false}}
			if(cpa.inputPayload && cpa.inputPayload.multiple === po.ChartPluginInputAttribute.MultipleRepeat)
			{
				cpa.array = true;
				cpa.inputPayload.multiple = false;
			}
			
			//将5.5.0旧版的颜色输入框{inputPayload: {multiple: true}}格式转换为6.0新版的{array: true}
			if(cpa.inputType == po.ChartPluginInputAttribute.InputType.COLOR
					&& cpa.inputPayload && cpa.inputPayload.multiple === true)
			{
				cpa.array = true;
			}
		};
		
		return cpas;
	};
	
	po.trimChartPluginInputAttrInputPayloadIfMap = function(cpa, inputPayload)
	{
		var options = inputPayload.options;
		
		//内置地图
		if(options == po.ChartPluginInputAttribute.InputPayload.DG_MAP)
		{
			//只有下拉列表才使用树形结构，单选框、复选框只能使用平铺数组
			if(inputPayload.treeSelect == null
					&& cpa.inputType == po.ChartPluginInputAttribute.InputType.SELECT)
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
	
	po.trimChartPluginInputAttrInputOptions = function(cpa, inputPayload)
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
	
	po.isVirtualChartPluginGroupAttr = function(group)
	{
		return (group != null && group.virtual);
	};
	
	po.findVirtualChartPluginAttrIdxByName = function(groups, name)
	{
		for(var i=0; i<groups.length; i++)
		{
			if(po.isVirtualChartPluginGroupAttr(groups[i]) && groups[i].name == name)
			{
				return i;
			}
		}
		
		return -1;
	};
	
	po.trimChartPluginAttrByGroup = function(cpas)
	{
		var groups = [];
		
		for(var i=0; i<cpas.length; i++)
		{
			var cpa = cpas[i];
			
			if(po.isChartPluginGroupAttr(cpa))
			{
				groups.push(cpa);
				continue;
			}
			
			//无分组的，建立虚拟分组，统一结构、易于处理
			var virtualGroup = {};
			
			//处理5.0.0旧版的org.datagear.analysis.ChartPluginInputAttribute.group
			if(cpa.group != null)
				virtualGroup = cpa.group;
			
			//没有定义分组，如果末尾是【未分组】，则使用；否则，新建【未分组】
			if(!virtualGroup.name)
			{
				var groupTail = (groups.length > 0 ? groups[groups.length - 1] : null);
				
				if(groupTail && po.isVirtualChartPluginGroupAttr(groupTail) && groupTail.name == "")
				{
					virtualGroup = groupTail;
				}
				else
				{
					virtualGroup = { name: "", children: [], virtual: true };
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
					virtualGroup = { name: virtualGroup.name, nameLabel: virtualGroup.nameLabel, children: [], virtual: true };
					groups.push(virtualGroup);
				}
			}
			
			if(!virtualGroup.nameLabel || !virtualGroup.nameLabel.value)
				virtualGroup.nameLabel = { value: "<@spring.message code='ungrouped' />" };
			
			virtualGroup.children.push(cpa);
		};
		
		return groups;
	};
	
	po.trimChartPluginAttrNameLabel = function(cpa)
	{
		if(!cpa)
			return;
		
		if(cpa.nameLabel && cpa.nameLabel.value)
			return;
		
		cpa.nameLabel = { value: cpa.name };
	};
	
	//整理图表属性值：类型转换、选项值限定
	po.trimChartAttrValues = function(attrValues, cpas)
	{
		//注意：attrValues中对于没有在cpas定义的属性值应原样保留，
		//因为看板的dg-chart-attr-values应允许定义图表插件属性之外的扩展值
		
		if(!attrValues)
			return null;
		
		if(!cpas || cpas.length == 0)
			return attrValues;
		
		var re = $.extend(true, {}, attrValues);
		
		for(var i=0; i<cpas.length; i++)
		{
			var cpa = cpas[i];
			var v = re[cpa.name];
			
			var inputType = cpa.inputType;
			var inputPayload = cpa.inputPayload;
			var isTreeSelect = (inputPayload && inputPayload.treeSelect == true);
			var isArrayValue = (cpa.array || (inputPayload && inputPayload.multiple == true));
			
			//需先转换树组件Model
			if(isTreeSelect)
				v = po.trimChartAttrValueIfTreeModel(v, !isArrayValue);
			
			//需转换类型
			v = po.toChartAttrTypeValue(cpa.type, v);
			
			if(v != null)
			{
				//多选输入框应强制转换为数组
				if(isArrayValue && !$.isArray(v))
				{
					v = [ v ];
				}
				
				//应将值限定为待选值集合内，比如图表插件升级后inputPayload有所删减，那么这里的旧值应删除
				if(inputPayload && inputPayload.options && $.isArray(inputPayload.options))
				{
					if($.isArray(v))
					{
						var vnew = [];
						$.each(v, function(j, vj)
						{
							if(isTreeSelect)
							{
								if($.inTreeArrayById(inputPayload.options, vj, "key"))
									vnew.push(vj);
							}
							else
							{
								if($.inArrayById(inputPayload.options, vj, "value") >= 0)
									vnew.push(vj);
							}
						});
						
						v = vnew;
					}
					else
					{
						if(isTreeSelect)
						{
							if($.inTreeArrayById(inputPayload.options, v, "key") != true)
								v = null;
						}
						else
						{
							if($.inArrayById(inputPayload.options, v, "value") < 0)
								v = null;
						}
					}
				}
			}
			
			re[cpa.name] = v;
		};
		
		return re;
	};
	
	//树组件Model结构是：{ v0: true, ... }，需进行转换
	po.trimChartAttrValueIfTreeModel = function(treeModel, single)
	{
		//不是树组件Model的应原样返回
		if(!treeModel || !$.isPlainObject(treeModel))
			return treeModel;
		
		var values = [];
		
		$.each(treeModel, function(p, v)
		{
			if(v === true)
				values.push(p);
		});
		
		return (single ? values[0] : values);
	};
	
	po.toChartAttrTypeValue = function(type, value)
	{
		if(value == null)
		{
			return null;
		}
		else if($.isArray(value))
		{
			var re = [];
			$.each(value, function(i, vi)
			{
				re.push(po.toChartAttrTypeValue(type, vi));
			});
			
			return re;
		}
		else if(type == po.ChartPluginInputAttribute.DataType.BOOLEAN)
		{
			return (value == true || value == "true" || value == "1" || value > 0 ? true : false);
		}
		else if(type == po.ChartPluginInputAttribute.DataType.NUMBER)
		{
			return $.parseToNumber(value);
		}
		else
			return value;
	};
	
	po.toChartAttrValuesFormModel = function(attrValues, cpas)
	{
		var formValues = $.extend(true, {}, (attrValues || {}));
		
		if(!cpas || cpas.length == 0)
			return attrValues;
		
		$.each(cpas, function(i, cpa)
		{
			var v = formValues[cpa.name];
			
			if(v == null)
				return;
			
			var inputPayload = cpa.inputPayload;
			var isTreeSelect = (inputPayload && inputPayload.treeSelect == true);
			
			//转换为树组件Model
			if(isTreeSelect)
				formValues[cpa.name] = po.chartAttrValueToTreeModel(v);
		});
		
		return formValues;
	};
	
	//插件属性值转换为树组件Model，它的模型结构是：{ v0: true, ... }
	po.chartAttrValueToTreeModel = function(value)
	{
		if(value != null && $.isPlainObject(value))
			return value;
		
		var re = {};
		
		if(value != null)
		{
			value = ($.isArray(value) ? value : [ value ]);
			$.each(value, function(i, v)
			{
				re[v] = true;
			});
		}
		
		return re;
	};
	
	po.validateChartAttrValuesRequired = function(cpas, attrValues)
	{
		if(!cpas)
			return true;
		
		attrValues = (attrValues || {});
		
		var re = true;
		
		$.each(cpas, function(i, cpa)
		{
			if(cpa.required && $.isEmptyValue(attrValues[cpa.name]))
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
			attributes: [],
			groups: [],
			attrValues: {},
			readonly: false,
			buttons: []
		}
	});
	
	po.setupChartAttrValuesForm = function(cpas, attrValues, options)
	{
		options = $.extend(
		{
			submitHandler: null,
			buttons: [],
			readonly: false
		},
		options);
		
		var cpas = po.trimChartPluginAttributes(cpas);
		
		var pm = po.vuePageModel();
		pm.chartAttrValuesForm.attributes = cpas;
		pm.chartAttrValuesForm.groups = po.trimChartPluginAttrByGroup(cpas);
		pm.chartAttrValuesForm.buttons = options.buttons;
		pm.chartAttrValuesForm.readonly = options.readonly;
		po.setChartAttrValuesFormAttrValues(attrValues);
		
		var validateRules = {};
		$.each(cpas, function(i, cpa)
		{
			if(cpa.type == po.ChartPluginInputAttribute.DataType.NUMBER)
				validateRules[cpa.name] = { "number": true };
		});
		
		var form = po.elementOfId("${pid}chartAttrValuesForm", document.body);
		po.setupSimpleForm(form, pm.chartAttrValuesForm.attrValues,
		{
			rules: validateRules,
			submitHandler: function()
			{
				if(options && options.submitHandler)
				{
					var formData = po.trimChartAttrValues(po.vueRaw(pm.chartAttrValuesForm.attrValues), pm.chartAttrValuesForm.attributes);
					options.submitHandler(formData);
				}
			}
		});
	};
	
	po.setChartAttrValuesFormAttrValues = function(attrValues)
	{
		var pm = po.vuePageModel();
		var cpas = pm.chartAttrValuesForm.attributes;
		var formValues = po.toChartAttrValuesFormModel(attrValues, cpas);
		
		pm.chartAttrValuesForm.attrValues = formValues;
	};
	
	po.vueMethod(
	{
		onChartAttrValuesFormInsertValue: function(e, propName, idx)
		{
			var pm = po.vuePageModel();
			var attrValues = pm.chartAttrValuesForm.attrValues;
			
			if(!attrValues[propName])
				attrValues[propName] = [];
			
			if(idx == null)
				attrValues[propName].push("");
			else
				attrValues[propName].splice(idx, 0, "");
		},
		
		onChartAttrValuesFormRemoveValue: function(e, propName, idx)
		{
			var pm = po.vuePageModel();
			var attrValues = pm.chartAttrValuesForm.attrValues;
			attrValues[propName].splice(idx, 1);
		}
	});
})
(${pid});
</script>
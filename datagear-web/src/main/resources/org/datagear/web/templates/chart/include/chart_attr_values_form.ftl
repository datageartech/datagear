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
图表属性值集表单

依赖：
page_boolean_options.ftl
-->
<#assign ChartPluginAttributeType=statics['org.datagear.analysis.ChartPluginAttribute$DataType']>
<#assign ChartPluginAttributeInputType=statics['org.datagear.analysis.ChartPluginAttribute$InputType']>
<form id="${pid}chartAttrValuesForm" class="chart-attr-values-form flex flex-column" :class="{readonly: pm.chartAttrValuesForm.readonly}">
	<div class="page-form-content flex-grow-1 px-2 py-1 overflow-y-auto">
		<div v-for="(group, groupIdx) in pm.chartAttrValuesForm.groups">
			<p-divider align="center">
				<label class="text-lg font-bold">{{group.label}}</label>
			</p-divider>
			<div class="field grid" v-for="(cpa, cpaIdx) in group.cpas">
				<label :for="'${pid}cpattr_'+cpa.name" class="field-label col-12 mb-2"
					:title="cpa.descLabel && cpa.descLabel.value ? cpa.descLabel.value : null">
					<span>
						{{cpa.nameLabel && cpa.nameLabel.value ? cpa.nameLabel.value : cpa.name}}
					</span>
					<span class="text-color-secondary text-sm ml-1">{{cpa.name}}</span>
				</label>
				<div class="field-input col-12" v-if="cpa.inputType == pm.ChartPluginAttribute.InputType.RADIO">
					<div class="input border-1px-transparent p-inputtext p-component px-0 py-0">
						<div v-for="(ip, ipIdx) in cpa.inputPayload.options" class="inline-block mr-2">
							<p-radiobutton :id="'${pid}cpattr_'+cpa.name+'_'+ipIdx" :value="ip.value" v-model="pm.chartAttrValuesForm.attrValues[cpa.name]"></p-radiobutton>
							<label :for="'${pid}cpattr_'+cpa.name+'_'+ipIdx" class="ml-1">{{ip.name}}</label>
						</div>
					</div>
		        	<div class="validate-msg" v-if="cpa.required">
		        		<input :name="cpa.name" required type="text" class="validate-proxy" />
		        	</div>
				</div>
				<div class="field-input col-12" v-else-if="cpa.inputType == pm.ChartPluginAttribute.InputType.SELECT">
					<div v-if="cpa.inputPayload.multiple == true">
						<div v-if="cpa.inputPayload.treeSelect == true">
							<p-treeselect :id="'${pid}cpattr_'+cpa.name" v-model="pm.chartAttrValuesForm.attrValues[cpa.name]" :options="cpa.inputPayload.options"
								selection-mode="multiple" class="input w-full" placeholder="<@spring.message code='none' />">
							</p-treeselect>
						</div>
						<div v-else>
							<p-multiselect :id="'${pid}cpattr_'+cpa.name" v-model="pm.chartAttrValuesForm.attrValues[cpa.name]" :options="cpa.inputPayload.options"
								option-label="name" option-value="value" :show-clear="true" class="input w-full">
							</p-multiselect>
						</div>
					</div>
					<div class="input border-1px-transparent p-inputtext p-component px-0 py-0"
						v-else-if="cpa.inputPayload.multiple == pm.ChartPluginAttribute.MultipleRepeat">
						<div v-for="(sv, svIdx) in pm.chartAttrValuesForm.attrValues[cpa.name]" :key="svIdx">
							<div class="flex mb-1">
								<p-treeselect :id="'${pid}cpattr_'+cpa.name" v-model="pm.chartAttrValuesForm.attrValues[cpa.name]" :options="cpa.inputPayload.options"
									class="input w-full" placeholder="<@spring.message code='none' />" v-if="cpa.inputPayload.treeSelect == true">
								</p-treeselect>
								<p-dropdown :id="'${pid}cpattr_'+cpa.name+'_'+svIdx" v-model="pm.chartAttrValuesForm.attrValues[cpa.name][svIdx]" :options="cpa.inputPayload.options"
									option-label="name" option-value="value" class="input flex-grow-1 mr-1" v-else>
								</p-dropdown>
								<p-button type="button" label="<@spring.message code='delete' />" class="p-button-danger"
									@click="onChartAttrValuesFormRemoveValue($event, cpa.name, svIdx)"
									v-if="!pm.chartAttrValuesForm.readonly">
								</p-button>
							</div>
						</div>
						<div class="mt-1" v-if="!pm.chartAttrValuesForm.readonly">
							<p-button type="button" icon="pi pi-plus" @click="onChartAttrValuesFormAddValue(cpa.name)"></p-button>
						</div>
					</div>
					<div v-else>
						<div v-if="cpa.inputPayload.treeSelect == true">
							<p-treeselect :id="'${pid}cpattr_'+cpa.name" v-model="pm.chartAttrValuesForm.attrValues[cpa.name]" :options="cpa.inputPayload.options"
								class="input w-full" placeholder="<@spring.message code='none' />">
							</p-treeselect>
						</div>
						<div v-else>
							<p-dropdown :id="'${pid}cpattr_'+cpa.name" v-model="pm.chartAttrValuesForm.attrValues[cpa.name]" :options="cpa.inputPayload.options"
								option-label="name" option-value="value" :show-clear="!cpa.required" class="input w-full">
							</p-dropdown>
						</div>
					</div>
		        	<div class="validate-msg" v-if="cpa.required">
		        		<input :name="cpa.name" required type="text" class="validate-proxy" />
		        	</div>
				</div>
				<div class="field-input col-12" v-else-if="cpa.inputType == pm.ChartPluginAttribute.InputType.CHECKBOX">
					<div class="input border-1px-transparent p-inputtext p-component px-0 py-0">
						<div v-for="(ip, ipIdx) in cpa.inputPayload.options" class="inline-block mr-2">
							<p-checkbox :input-id="'${pid}cpattr_'+cpa.name+'_'+ipIdx" :value="ip.value" v-model="pm.chartAttrValuesForm.attrValues[cpa.name]"></p-checkbox>
							<label :for="'${pid}cpattr_'+cpa.name+'_'+ipIdx" class="ml-1">{{ip.name}}</label>
						</div>
					</div>
		        	<div class="validate-msg" v-if="cpa.required">
		        		<input :name="cpa.name" required type="text" class="validate-proxy" />
		        	</div>
				</div>
				<div class="field-input col-12" v-else-if="cpa.inputType == pm.ChartPluginAttribute.InputType.COLOR">
					<div class="input border-1px-transparent p-inputtext p-component px-0 py-0" v-if="cpa.inputPayload.multiple">
						<div v-for="(color, colorIdx) in pm.chartAttrValuesForm.colorProxy[cpa.name]" :key="colorIdx">
							<div class="flex mb-1">
								<p-inputtext :id="'${pid}cpattr_'+cpa.name+'_'+colorIdx" v-model="pm.chartAttrValuesForm.attrValues[cpa.name][colorIdx]" type="text"
									class="input flex-grow-1 mr-1">
								</p-inputtext>
								<p-colorpicker v-model="pm.chartAttrValuesForm.colorProxy[cpa.name][colorIdx]"
									default-color="FFFFFF" class="flex-grow-0 preview-h-full mr-3"
									@change="onChartAttrValuesFormColorPickerChange($event, cpa.name, colorIdx)">
								</p-colorpicker>
								<p-button type="button" label="<@spring.message code='delete' />" class="p-button-danger"
									@click="onChartAttrValuesFormRemoveColor($event, cpa.name, colorIdx)"
									v-if="!pm.chartAttrValuesForm.readonly">
								</p-button>
							</div>
						</div>
						<div class="mt-1" v-if="!pm.chartAttrValuesForm.readonly">
							<p-button type="button" icon="pi pi-plus" @click="onChartAttrValuesFormAddColor(cpa.name)"></p-button>
						</div>
					</div>
					<div class="flex" v-else>
						<p-inputtext :id="'${pid}cpattr_'+cpa.name" v-model="pm.chartAttrValuesForm.attrValues[cpa.name]" type="text"
							class="input flex-grow-1 mr-1" maxlength="100">
						</p-inputtext>
						<p-colorpicker v-model="pm.chartAttrValuesForm.colorProxy[cpa.name]"
							default-color="FFFFFF" class="flex-grow-0 preview-h-full"
							@change="onChartAttrValuesFormColorPickerChange($event, cpa.name)">
						</p-colorpicker>
					</div>
		        	<div class="validate-msg" v-if="cpa.required">
		        		<input :name="cpa.name" required type="text" class="validate-proxy" />
		        	</div>
				</div>
				<div class="field-input col-12" v-else-if="cpa.inputType == pm.ChartPluginAttribute.InputType.TEXTAREA">
					<p-textarea :id="'${pid}cpattr_'+cpa.name" v-model="pm.chartAttrValuesForm.attrValues[cpa.name]" type="text"
						class="input w-full" maxlength="2000">
					</p-textarea>
		        	<div class="validate-msg" v-if="cpa.required">
		        		<input :name="cpa.name" required type="text" class="validate-proxy" />
		        	</div>
				</div>
				<div class="field-input col-12" v-else>
					<p-inputtext :id="'${pid}cpattr_'+cpa.name" v-model="pm.chartAttrValuesForm.attrValues[cpa.name]" type="text"
						class="input w-full" maxlength="1000">
					</p-inputtext>
		        	<div class="validate-msg" v-if="cpa.required">
		        		<input :name="cpa.name" required type="text" class="validate-proxy" />
		        	</div>
				</div>
			</div>
		</div>
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
	po.ChartPluginAttribute =
	{
		DataType:
		{
			STRING: "${ChartPluginAttributeType.STRING}",
			BOOLEAN: "${ChartPluginAttributeType.BOOLEAN}",
			NUMBER: "${ChartPluginAttributeType.NUMBER}"
		},
		InputType:
		{
			TEXT: "${ChartPluginAttributeInputType.TEXT}",
			SELECT: "${ChartPluginAttributeInputType.SELECT}",
			RADIO: "${ChartPluginAttributeInputType.RADIO}",
			CHECKBOX: "${ChartPluginAttributeInputType.CHECKBOX}",
			TEXTAREA: "${ChartPluginAttributeInputType.TEXTAREA}",
			COLOR: "${ChartPluginAttributeInputType.COLOR}"
		},
		InputPayload:
		{
			//多选
			MULTIPLE: "multiple",
			//地图
			DG_MAP: "DG_MAP"
		},
		//下拉框inputPayload.multiple="repeat"值，表示可重复选取
		MultipleRepeat: "repeat"
	};
	
	po.trimChartPluginAttributes = function(cpas)
	{
		if(!cpas)
			return [];
		
		cpas = $.extend(true, [], cpas);
		
		$.each(cpas, function(i, cpa)
		{
			//布尔型默认作为RADIO处理
			if(cpa.type == po.ChartPluginAttribute.DataType.BOOLEAN)
			{
				if(!cpa.inputType)
					cpa.inputType = po.ChartPluginAttribute.InputType.RADIO;
				
				if(!cpa.inputPayload)
				{
					var pm = po.vuePageModel();
					cpa.inputPayload = po.vueRaw(pm.booleanOptions);
				}
			}
			
			var inputType = cpa.inputType;
			
			//下拉框、单选、复选框：将inputPayload转换为{multiple: ..., options: [{name: ..., value: ...}, ...]}格式
			if(inputType == po.ChartPluginAttribute.InputType.SELECT
					|| inputType == po.ChartPluginAttribute.InputType.RADIO
					|| inputType == po.ChartPluginAttribute.InputType.CHECKBOX)
			{
				var inputPayload = (cpa.inputPayload || []);
				
				//数组、"DG_MAP"：转换为{ multiple: false, options: ... }格式
				if($.isArray(inputPayload) || (inputPayload == po.ChartPluginAttribute.InputPayload.DG_MAP))
					inputPayload = { multiple: false, options: inputPayload };
				
				//{ options: "DG_MAP" }：转换为实际地图数据options
				po.trimChartPluginAttributeInputPayloadIfMap(cpa, inputPayload);
				
				//默认multiple为false
				inputPayload.multiple = (inputPayload.multiple == null ? false : inputPayload.multiple);
				po.trimChartPluginAttributeInputOptions(cpa, inputPayload);
				
				if(inputType == po.ChartPluginAttribute.InputType.RADIO)
				{
					inputPayload.multiple = false;
				}
				else if(inputType == po.ChartPluginAttribute.InputType.CHECKBOX)
				{
					inputPayload.multiple = true;
				}
				
				cpa.inputPayload = inputPayload;
			}
			//颜色框：将inputPayload转换为标准的{multiple: ...}格式
			else if(inputType == po.ChartPluginAttribute.InputType.COLOR)
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
					inputPayload = { multiple: (inputPayload == po.ChartPluginAttribute.InputPayload.MULTIPLE) };
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
		});
		
		return cpas;
	};
	
	po.trimChartPluginAttributeInputPayloadIfMap = function(chartPluginAttr, inputPayload)
	{
		var options = inputPayload.options;
		
		//内置地图
		if(options == po.ChartPluginAttribute.InputPayload.DG_MAP)
		{
			//只有下拉列表才使用树形结构，单选框、复选框只能使用平铺数组
			if(inputPayload.treeSelect == null
					&& chartPluginAttr.inputType == po.ChartPluginAttribute.InputType.SELECT)
			{
				inputPayload.treeSelect = true;
			}
			
			inputPayload.options = po.getChartPluginAttributeInputOptionsForMap(inputPayload.treeSelect);
		}
	};
	
	po.getChartPluginAttributeInputOptionsForMap = function(asTree)
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
			
			return dashboardFactory.getStdBuiltinChartMapTree(listener);
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
			
			return dashboardFactory.getStdBuiltinChartMapArray(listener);
		}
	};
	
	po.trimChartPluginAttributeInputOptions = function(chartPluginAttr, inputPayload)
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
	
	po.toChartPluginAttributeGroups = function(cpas)
	{
		var groups = [];
		
		$.each(cpas, function(i, cpa)
		{
			var group = null;
			var myGroup = (cpa.group || {});
			
			//没有定义分组，如果末尾是【未分组】，则使用；否则，新建【未分组】
			if(!myGroup.name)
			{
				var groupPrev = (groups.length > 0 ? groups[groups.length - 1] : null);
				if(groupPrev && groupPrev.name == "")
				{
					group = groupPrev;
				}
				else
				{
					group = { name: "", label: "<@spring.message code='ungrouped' />", cpas: [] };
					groups.push(group);
				}
			}
			//有分组，查找或新建
			else
			{
				var idx = $.inArrayById(groups, myGroup.name, "name");
				if(idx >= 0)
				{
					group = groups[idx];
				}
				else
				{
					group = { name: myGroup.name, label: (myGroup.nameLabel && myGroup.nameLabel.value ? myGroup.nameLabel.value : myGroup.name), cpas: [] };
					groups.push(group);
				}
			}
			
			group.cpas.push(cpa);
		});
		
		return groups;
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
		
		$.each(cpas, function(i, cpa)
		{
			var v = re[cpa.name];
			
			var inputType = cpa.inputType;
			var inputPayload = cpa.inputPayload;
			var isTreeSelect = (inputPayload && inputPayload.treeSelect == true);
			var isMultipleSelect = (inputPayload && (inputPayload.multiple == true || inputPayload.multiple == po.ChartPluginAttribute.MultipleRepeat));
			
			//需先转换树组件Model
			if(isTreeSelect)
				v = po.trimChartAttrValueIfTreeModel(v, !isMultipleSelect);
			
			//需转换类型
			v = po.toChartAttrTypeValue(cpa.type, v);
			
			if(v != null)
			{
				//多选输入框应强制转换为数组
				if(isMultipleSelect && !$.isArray(v))
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
		});
		
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
		else if(type == po.ChartPluginAttribute.DataType.BOOLEAN)
		{
			return (value == true || value == "true" || value == "1" || value > 0 ? true : false);
		}
		else if(type == po.ChartPluginAttribute.DataType.NUMBER)
		{
			return parseFloat(value);
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
	
	po.toChartAttrValuesFormColorProxy = function(attrValues, cpas)
	{
		attrValues = (attrValues || {});
		
		var colorProxy = {};
		
		$.each(cpas, function(i, cpa)
		{
			var inputType = cpa.inputType;
			var inputPayload = cpa.inputPayload;
			
			if(inputType == po.ChartPluginAttribute.InputType.COLOR)
			{
				var v = attrValues[cpa.name];
				if(v)
				{
					if($.isArray(v))
					{
						var vNew = [];
						$.each(v, function(j, vj)
						{
							vNew.push(chartFactory.colorToHexStr(vj));
						});
						
						v = vNew;
					}
					else
						v = chartFactory.colorToHexStr(v);
					
					colorProxy[cpa.name] = v;
				}
			}
		});
		
		return colorProxy;
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
		ChartPluginAttribute: po.ChartPluginAttribute,
		chartAttrValuesForm:
		{
			attributes: [],
			groups: [],
			attrValues: {},
			readonly: false,
			buttons: [],
			colorProxy: {}
		}
	});
	
	po.setupChartAttrValuesForm = function(chartPluginAttributes, attrValues, options)
	{
		options = $.extend(
		{
			submitHandler: null,
			buttons: [],
			readonly: false
		},
		options);
		
		var cpas = po.trimChartPluginAttributes(chartPluginAttributes);
		
		var pm = po.vuePageModel();
		pm.chartAttrValuesForm.attributes = cpas;
		pm.chartAttrValuesForm.groups = po.toChartPluginAttributeGroups(cpas);
		pm.chartAttrValuesForm.buttons = options.buttons;
		pm.chartAttrValuesForm.readonly = options.readonly;
		po.setChartAttrValuesFormAttrValues(attrValues);
		
		var validateRules = {};
		$.each(cpas, function(i, cpa)
		{
			if(cpa.type == po.ChartPluginAttribute.DataType.NUMBER)
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
		pm.chartAttrValuesForm.colorProxy = po.toChartAttrValuesFormColorProxy(pm.chartAttrValuesForm.attrValues, pm.chartAttrValuesForm.attributes);
	};
	
	po.vueMethod(
	{
		onChartAttrValuesFormColorPickerChange: function(e, propName, idx)
		{
			var pm = po.vuePageModel();
			var proxy = pm.chartAttrValuesForm.colorProxy;
			var attrValues = pm.chartAttrValuesForm.attrValues;
			
			//XXX 使用e.value在第一次时返回的值不是新值！？
			var pickColor = (idx != null ? "#"+proxy[propName][idx] : "#"+proxy[propName]);
			
			if(idx != null)
			{
				pickColor = (pickColor ? pickColor : attrValues[propName][idx]);
				attrValues[propName][idx] = chartFactory.colorToHexStr(pickColor, true);
			}
			else
			{
				pickColor = (pickColor ? pickColor : attrValues[propName]);
				attrValues[propName] = chartFactory.colorToHexStr(pickColor, true);
			}
		},
		
		onChartAttrValuesFormAddValue: function(propName)
		{
			var pm = po.vuePageModel();
			var attrValues = pm.chartAttrValuesForm.attrValues;
			
			if(!attrValues[propName])
				attrValues[propName] = [];
			
			attrValues[propName].push("");
		},
		
		onChartAttrValuesFormRemoveValue: function(e, propName, idx)
		{
			var pm = po.vuePageModel();
			var attrValues = pm.chartAttrValuesForm.attrValues;
			
			attrValues[propName].splice(idx, 1);
		},
		
		onChartAttrValuesFormAddColor: function(propName)
		{
			var pm = po.vuePageModel();
			var proxy = pm.chartAttrValuesForm.colorProxy;
			var attrValues = pm.chartAttrValuesForm.attrValues;
			
			if(!proxy[propName])
				proxy[propName] = [];
			if(!attrValues[propName])
				attrValues[propName] = [];
			
			proxy[propName].push("");
			attrValues[propName].push("");
		},
		
		onChartAttrValuesFormRemoveColor: function(e, propName, idx)
		{
			var pm = po.vuePageModel();
			var proxy = pm.chartAttrValuesForm.colorProxy;
			var attrValues = pm.chartAttrValuesForm.attrValues;
			
			proxy[propName].splice(idx, 1);
			attrValues[propName].splice(idx, 1);
		}
	});
})
(${pid});
</script>
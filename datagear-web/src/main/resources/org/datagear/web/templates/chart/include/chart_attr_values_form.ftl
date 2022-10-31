<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#--
图表属性值集表单

依赖：
page_boolean_options.ftl
-->
<#assign ChartPluginAttributeType=statics['org.datagear.analysis.ChartPluginAttribute$DataType']>
<#assign ChartPluginAttributeInputType=statics['org.datagear.analysis.ChartPluginAttribute$InputType']>
<form id="${pid}chartAttrValuesForm" class="flex flex-column chart-attr-values-form" :class="{readonly: pm.chartAttrValuesForm.readonly}">
	<div class="page-form-content flex-grow-1 px-2 py-1 overflow-y-auto">
		<div class="field grid" v-for="(ca, caIdx) in pm.chartAttrValuesForm.attributes">
			<label :for="'${pid}pluginAttribute_'+caIdx" class="field-label col-12 mb-2"
				:title="ca.descLabel && ca.descLabel.value ? ca.descLabel.value : null">
				{{ca.nameLabel && ca.nameLabel.value ? ca.nameLabel.value : ca.name}}
			</label>
			<div class="field-input col-12" v-if="ca.inputType == pm.ChartPluginAttribute.InputType.RADIO">
				<div v-for="(ip, ipIdx) in ca.inputPayload.options" class="inline-block mr-2">
					<p-radiobutton :id="'${pid}pluginAttribute_'+caIdx+'_'+ipIdx" :name="ca.name" :value="ip.value" v-model="pm.chartAttrValuesForm.attrValues[ca.name]"></p-radiobutton>
					<label :for="'${pid}pluginAttribute_'+caIdx+'_'+ipIdx" class="ml-1">{{ip.name}}</label>
				</div>
			</div>
			<div class="field-input col-12" v-else-if="ca.inputType == pm.ChartPluginAttribute.InputType.SELECT">
				<div v-if="ca.inputPayload.multiple">
					<p-multiselect :id="'${pid}pluginAttribute_'+caIdx" v-model="pm.chartAttrValuesForm.attrValues[ca.name]" :options="ca.inputPayload.options"
						option-label="name" option-value="value" class="input w-full" :name="ca.name">
					</p-multiselect>
				</div>
				<div v-else>
					<p-dropdown :id="'${pid}pluginAttribute_'+caIdx" v-model="pm.chartAttrValuesForm.attrValues[ca.name]" :options="ca.inputPayload.options"
						option-label="name" option-value="value" class="input w-full" :name="ca.name">
					</p-dropdown>
				</div>
			</div>
			<div class="field-input col-12" v-else-if="ca.inputType == pm.ChartPluginAttribute.InputType.CHECKBOX">
				<div v-for="(ip, ipIdx) in ca.inputPayload.options" class="inline-block mr-2">
					<p-checkbox :id="'${pid}pluginAttribute_'+caIdx+'_'+ipIdx" :name="ca.name" :value="ip.value" v-model="pm.chartAttrValuesForm.attrValues[ca.name]"></p-checkbox>
					<label :for="'${pid}pluginAttribute_'+caIdx+'_'+ipIdx" class="ml-1">{{ip.name}}</label>
				</div>
			</div>
			<div class="field-input col-12" v-else-if="ca.inputType == pm.ChartPluginAttribute.InputType.COLOR">
				<div v-if="ca.inputPayload.multiple">
					<div v-for="(color, colorIdx) in pm.chartAttrValuesForm.colorProxy[ca.name]" :key="colorIdx">
						<div class="flex mb-1">
							<p-inputtext :id="'${pid}pluginAttribute_'+caIdx+'_'+colorIdx" v-model="pm.chartAttrValuesForm.attrValues[ca.name][colorIdx]" type="text"
								class="input flex-grow-1 mr-1" name="ca.name">
							</p-inputtext>
							<p-colorpicker v-model="pm.chartAttrValuesForm.colorProxy[ca.name][colorIdx]"
								default-color="FFFFFF" class="flex-grow-0 preview-h-full mr-3"
								@change="onChartAttrValuesFormColorPickerChange($event, ca.name, colorIdx)">
							</p-colorpicker>
							<p-button type="button" label="<@spring.message code='delete' />" class="p-button-danger"
								@click="onChartAttrValuesFormRemoveColor($event, ca.name, colorIdx)"
								v-if="!pm.chartAttrValuesForm.readonly">
							</p-button>
						</div>
					</div>
					<div class="mt-1" v-if="!pm.chartAttrValuesForm.readonly">
						<p-button type="button" icon="pi pi-plus" @click="onChartAttrValuesFormAddColor(ca.name)"></p-button>
					</div>
				</div>
				<div class="flex" v-else>
					<p-inputtext :id="'${pid}pluginAttribute_'+caIdx" v-model="pm.chartAttrValuesForm.attrValues[ca.name]" type="text"
						class="input flex-grow-1 mr-1" maxlength="100" :name="ca.name">
					</p-inputtext>
					<p-colorpicker v-model="pm.chartAttrValuesForm.colorProxy[ca.name]"
						default-color="FFFFFF" class="flex-grow-0 preview-h-full"
						@change="onChartAttrValuesFormColorPickerChange($event, ca.name)">
					</p-colorpicker>
				</div>
			</div>
			<div class="field-input col-12" v-else-if="ca.inputType == pm.ChartPluginAttribute.InputType.TEXTAREA">
				<p-textarea :id="'${pid}pluginAttribute_'+caIdx" v-model="pm.chartAttrValuesForm.attrValues[ca.name]" type="text"
					class="input w-full" maxlength="2000" :name="ca.name">
				</p-textarea>
			</div>
			<div class="field-input col-12" v-else>
				<p-inputtext :id="'${pid}pluginAttribute_'+caIdx" v-model="pm.chartAttrValuesForm.attrValues[ca.name]" type="text"
					class="input w-full" maxlength="1000" :name="ca.name">
				</p-inputtext>
			</div>
		</div>
	</div>
	<div class="page-form-foot flex-grow-0 pt-3 text-center h-opts">
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
		}
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
				
				//"DG_MAP"
				inputPayload = po.trimChartPluginAttributeInputPayloadIfMap(inputPayload);
				
				//数组：转换为{multiple: false, options: [...]}格式
				if($.isArray(inputPayload))
					inputPayload = { multiple: false, options: inputPayload };
				
				//{ options: "DG_MAP" }
				inputPayload.options = po.trimChartPluginAttributeInputPayloadIfMap(inputPayload.options);
				
				//默认multiple为false
				inputPayload.multiple = (inputPayload.multiple == null ? false : inputPayload.multiple);
				inputPayload.options = po.trimChartPluginAttributeInputOptions(inputPayload.options);
				
				if(inputType == po.ChartPluginAttribute.InputType.RADIO
						|| inputType == po.ChartPluginAttribute.InputType.CHECKBOX)
				{
					inputPayload.multiple = false;
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
	
	po.trimChartPluginAttributeInputPayloadIfMap = function(inputPayload)
	{
		//内置地图
		if(inputPayload == po.ChartPluginAttribute.InputPayload.DG_MAP)
		{
			inputPayload = [];
			
			$.each(dashboardFactory.builtinChartMaps, function(i, cms)
			{
				if(cms && cms.names && cms.names.length > 0)
				{
					inputPayload.push(cms.names[0]);
				}
			});
		}
		
		return inputPayload;
	};
	
	po.trimChartPluginAttributeInputOptions = function(inputOptions)
	{
		inputOptions = (inputOptions || []);
		
		//支持非数组格式
		if(!$.isArray(inputOptions))
			inputOptions = [ inputOptions ];
		
		var inputOptionsNew = [];
		
		//转换为标准的[ {name: ..., value: ...}, ... ]格式
		$.each(inputOptions, function(i, io)
		{
			//支持元素为基本类型
			if(io == null || $.isTypeString(io) || $.isTypeNumber(io) || $.isTypeBoolean(io))
				io = { name: io, value: io };
			
			//支持{value: ...}格式的元素
			if(io.name == null)
				io.name = (io.value == null ? "null" : io.value);
			
			inputOptionsNew.push(io);
		});
		
		return inputOptionsNew;
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
			
			if(v == null)
				return;
			
			//需转换类型
			v = po.toChartAttrTypeValue(cpa.type, v);
			
			var inputType = cpa.inputType;
			var inputPayload = cpa.inputPayload;
			
			//多选输入框应强制转换为数组
			if(inputPayload && inputPayload.multiple == true && !$.isArray(v))
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
						if($.inArrayById(inputPayload.options, vj, "value") >= 0)
							vnew.push(vj);
					});
					
					v = vnew;
				}
				else
				{
					if($.inArrayById(inputPayload.options, v, "value") < 0)
					{
						v = null;
					}
				}
			}
			
			if(v != null)
				re[cpa.name] = v;
		});
		
		return re;
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
	
	po.vuePageModel(
	{
		ChartPluginAttribute: po.ChartPluginAttribute,
		chartAttrValuesForm:
		{
			attributes: [],
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
		
		var pm = po.vuePageModel();
		pm.chartAttrValuesForm.attributes = po.trimChartPluginAttributes(chartPluginAttributes);
		pm.chartAttrValuesForm.buttons = options.buttons;
		pm.chartAttrValuesForm.readonly = options.readonly;
		po.setChartAttrValuesFormAttrValues(attrValues);
		
		var validateRules = {};
		$.each(pm.chartAttrValuesForm.attributes, function(i, cpa)
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
		
		pm.chartAttrValuesForm.attrValues = $.extend(true, {}, (attrValues || {}));
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
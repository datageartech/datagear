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
<form id="${pid}chartAttrValuesForm" class="flex flex-column" :class="{readonly: pm.chartAttrValuesForm.readonly}">
	<div class="page-form-content panel-content-size-xxs flex-grow-1 px-2 py-1 overflow-y-auto">
		<div class="field grid" v-for="(ca, caIdx) in pm.chartAttrValuesForm.attributes">
			<label :for="'${pid}pluginAttribute_'+caIdx" class="field-label col-12 mb-2"
				:title="ca.descLabel && ca.descLabel.value ? ca.descLabel.value : null">
				{{ca.nameLabel && ca.nameLabel.value ? ca.nameLabel.value : ca.name}}
			</label>
			<div class="field-input col-12" v-if="ca.inputType == pm.ChartPluginAttribute.InputType.RADIO">
				<div v-for="(ip, ipIdx) in ca.inputPayload" class="inline-block mr-2">
					<p-radiobutton :id="'${pid}pluginAttribute_'+caIdx+'_'+ipIdx" :name="ca.name" :value="ip.value" v-model="pm.chartAttrValuesForm.attrValues[ca.name]"></p-radiobutton>
					<label :for="'${pid}pluginAttribute_'+caIdx+'_'+ipIdx" class="ml-1">{{ip.name}}</label>
				</div>
			</div>
			<div class="field-input col-12" v-else-if="ca.inputType == pm.ChartPluginAttribute.InputType.SELECT">
				<div v-if="ca.multiple">
					<p-multiselect :id="'${pid}pluginAttribute_'+caIdx" v-model="pm.chartAttrValuesForm.attrValues[ca.name]" :options="ca.inputPayload"
						option-label="name" option-value="value" class="input w-full" :name="ca.name">
					</p-multiselect>
				</div>
				<div v-else>
					<p-dropdown :id="'${pid}pluginAttribute_'+caIdx" v-model="pm.chartAttrValuesForm.attrValues[ca.name]" :options="ca.inputPayload"
						option-label="name" option-value="value" class="input w-full" :name="ca.name">
					</p-dropdown>
				</div>
			</div>
			<div class="field-input col-12" v-else-if="ca.inputType == pm.ChartPluginAttribute.InputType.CHECKBOX">
				<div v-for="(ip, ipIdx) in ca.inputPayload" class="inline-block mr-2">
					<p-checkbox :id="'${pid}pluginAttribute_'+caIdx+'_'+ipIdx" :name="ca.name" :value="ip.value" v-model="pm.chartAttrValuesForm.attrValues[ca.name]"></p-checkbox>
					<label :for="'${pid}pluginAttribute_'+caIdx+'_'+ipIdx" class="ml-1">{{ip.name}}</label>
				</div>
			</div>
			<div class="field-input col-12" v-else-if="ca.inputType == pm.ChartPluginAttribute.InputType.COLOR">
				<div class="flex">
					<p-inputtext :id="'${pid}pluginAttribute_'+caIdx" v-model="pm.chartAttrValuesForm.attrValues[ca.name]" type="text"
						class="input flex-grow-1 mr-1" maxlength="100" :name="ca.name">
					</p-inputtext>
					<p-colorpicker v-model="pm.chartAttrValuesForm.attrValues[ca.name]"
						default-color="FFFFFF" class="flex-grow-0 preview-h-full">
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
		}
	};
	
	po.trimChartPluginAttributes = function(cpas)
	{
		if(!cpas)
			return [];
		
		cpas = $.extend(true, [], cpas);
		
		$.each(cpas, function(i, cpa)
		{
			//布尔型inputType转换为RADIO便于下面处理
			if(cpa.type == po.ChartPluginAttribute.DataType.BOOLEAN)
			{
				cpa.inputType = po.ChartPluginAttribute.InputType.RADIO;
				if(!cpa.inputPayload)
				{
					var pm = po.vuePageModel();
					cpa.inputPayload = po.vueRaw(pm.booleanOptions);
				}
			}
			
			var inputType = cpa.inputType;
			
			//将inputPayload转换为标准的[ {name: ..., value: ...}, ... ]格式
			if(inputType == po.ChartPluginAttribute.InputType.SELECT
					|| inputType == po.ChartPluginAttribute.InputType.RADIO
					|| inputType == po.ChartPluginAttribute.InputType.CHECKBOX)
			{
				var inputPayload = (cpa.inputPayload || []);
				
				//支持非数组格式
				if(!$.isArray(inputPayload))
					inputPayload = [ inputPayload ];
				
				var inputPayloadNew = [];
				
				$.each(inputPayload, function(j, ip)
				{
					//支持元素为基本类型
					if(ip == null || $.isTypeString(ip) || $.isTypeNumber(ip) || $.isTypeBoolean(ip))
						ip = { name: ip, value: ip };
					
					//支持{value: ...}格式的元素
					if(ip.name == null)
						ip.name = (ip.value == null ? "null" : ip.value);
					
					inputPayloadNew.push(ip);
				});
				
				cpa.inputPayload = inputPayloadNew;
			}
		});
		
		return cpas;
	};
	
	po.trimChartAttrValues = function(attrValues, cpas)
	{
		if(!attrValues)
			return null;
		
		if(!cpas || cpas.length == 0)
			return null;
		
		var re = {};
		
		$.each(cpas, function(i, cpa)
		{
			var v = attrValues[cpa.name];
			if(v != null)
			{
				var inputType = cpa.inputType;
				
				//多选应强制转换为数组
				if((cpa.multiple || inputType == po.ChartPluginAttribute.InputType.CHECKBOX)
						&& !$.isArray(v))
				{
					v = [ v ];
				}
				
				//应将值限定为待选值集合内，比如图表插件升级后inputPayload有所删减，那么这里的旧值应删除
				if(inputType == po.ChartPluginAttribute.InputType.SELECT
						|| inputType == po.ChartPluginAttribute.InputType.RADIO
						|| inputType == po.ChartPluginAttribute.InputType.CHECKBOX)
				{
					var inputPayload = (cpa.inputPayload || []);
					
					if($.isArray(v))
					{
						var vnew = [];
						$.each(v, function(j, vj)
						{
							if($.inArrayById(inputPayload, vj, "value") >= 0)
								vnew.push(vj);
						});
						
						v = vnew;
					}
					else if($.inArrayById(inputPayload, v, "value") < 0)
					{
						v = null;
					}
				}
				
				if(v != null)
					re[cpa.name] = v;
			}
		});
		
		return re;
	};
	
	po.vuePageModel(
	{
		ChartPluginAttribute: po.ChartPluginAttribute,
		chartAttrValuesForm:
		{
			attributes: [],
			attrValues: {},
			readonly: false
		}
	});
	
	po.setupChartAttrValuesForm = function(chartPluginAttributes, attrValues, options)
	{
		options = $.extend(
		{
			submitHandler: null,
			readonly: false
		},
		options);
		
		var pm = po.vuePageModel();
		pm.chartAttrValuesForm.attributes = po.trimChartPluginAttributes(chartPluginAttributes);
		pm.chartAttrValuesForm.attrValues = $.extend(true, {}, (attrValues || {}));
		pm.chartAttrValuesForm.readonly = options.readonly;
		
		var form = po.elementOfId("${pid}chartAttrValuesForm", document.body);
		po.setupSimpleForm(form, pm.chartAttrValuesForm.attrValues, function()
		{
			if(options && options.submitHandler)
			{
				var formData = po.trimChartAttrValues(po.vueRaw(pm.chartAttrValuesForm.attrValues), pm.chartAttrValuesForm.attributes);
				options.submitHandler(formData);
			}
		});
	};
})
(${pid});
</script>
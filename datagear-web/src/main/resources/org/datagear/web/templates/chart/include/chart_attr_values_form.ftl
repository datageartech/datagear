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
			<label :for="'${pid}pluginAttribute_'+caIdx" class="field-label col-12 mb-2">
				{{ca.nameLabel && ca.nameLabel.value ? ca.nameLabel.value : ca.name}}
			</label>
			<div class="field-input col-12" v-if="ca.type == pm.ChartPluginAttribute.DataType.BOOLEAN">
				<p-selectbutton :id="'${pid}pluginAttribute_'+caIdx" v-model="pm.chartAttrValuesForm.attrValues[ca.name]" :options="pm.booleanOptions"
					option-label="name" option-value="value" class="input w-full">
				</p-selectbutton>
			</div>
			<div class="field-input col-12" v-else>
				<p-inputtext :id="'${pid}pluginAttribute_'+caIdx" v-model="pm.chartAttrValuesForm.attrValues[ca.name]" type="text"
					class="input w-full" maxlength="1000">
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
		pm.chartAttrValuesForm.attributes = chartPluginAttributes;
		pm.chartAttrValuesForm.attrValues = $.extend(true, {}, (attrValues || {}));
		pm.chartAttrValuesForm.readonly = options.readonly;
		
		var form = po.elementOfId("${pid}chartAttrValuesForm", document.body);
		po.setupSimpleForm(form, pm.chartAttrValuesForm.attrValues, function()
		{
			if(options && options.submitHandler)
			{
				var formData = $.extend(true, {}, po.vueRaw(pm.chartAttrValuesForm.attrValues));
				options.submitHandler(formData);
			}
		});
	};
})
(${pid});
</script>
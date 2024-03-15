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
数据集参数、属性表单

依赖：
page_boolean_options.ftl
page_simple_form.ftl

-->
<#assign ParamDataType=statics['org.datagear.analysis.DataSetParam$DataType']>
<#assign ParamInputType=statics['org.datagear.analysis.DataSetParam$InputType']>
<#assign PropertyDataType=statics['org.datagear.analysis.DataSetProperty$DataType']>
<p-dialog :header="pm.dataSetParamForm.title" append-to="body"
	position="center" :dismissable-mask="true" :modal="true"
	v-model:visible="pm.dataSetParamForm.show" @show="onDataSetParamFormPanelShow">
	<div class="page page-form">
		<form id="${pid}dataSetParamForm" class="flex flex-column">
			<div class="page-form-content flex-grow-1 px-2 py-1 panel-content-size-xs panel-content-size-xs-mwh overflow-y-auto">
				<div class="field grid">
					<label for="${pid}dspFormName" class="field-label col-12 mb-2"
						title="<@spring.message code='dataSetParam.name.desc' />">
						<@spring.message code='name' />
					</label>
					<div class="field-input col-12">
						<p-inputtext id="${pid}dspFormName" v-model="pm.dataSetParamForm.data.name" type="text"
							class="input w-full" name="name" required maxlength="100" autofocus>
						</p-inputtext>
					</div>
				</div>
				<div class="field grid">
					<label for="${pid}dspFormType" class="field-label col-12 mb-2">
						<@spring.message code='type' />
					</label>
					<div class="field-input col-12">
						<p-dropdown id="${pid}dspFormType" v-model="pm.dataSetParamForm.data.type" :options="pm.dataSetParamDataTypeOptions"
							option-label="name" option-value="value" class="input w-full">
						</p-dropdown>
					</div>
				</div>
				<div class="field grid">
					<label for="${pid}dspFormRequired" class="field-label col-12 mb-2">
						<@spring.message code='isRequired' />
					</label>
					<div class="field-input col-12">
						<p-selectbutton id="${pid}dspFormRequired" v-model="pm.dataSetParamForm.data.required" :options="pm.booleanOptions"
							option-label="name" option-value="value" class="input w-full">
						</p-selectbutton>
					</div>
				</div>
				<div class="field grid">
					<label for="${pid}dspFormDesc" class="field-label col-12 mb-2">
						<@spring.message code='desc' />
					</label>
					<div class="field-input col-12">
						<p-inputtext id="${pid}dspFormDesc" v-model="pm.dataSetParamForm.data.desc" type="text"
							class="input w-full" name="name" maxlength="100">
						</p-inputtext>
					</div>
				</div>
				<div class="field grid">
					<label for="${pid}dspFormInputType" class="field-label col-12 mb-2">
						<@spring.message code='inputType' />
					</label>
					<div class="field-input col-12">
						<p-dropdown id="${pid}dspFormInputType" v-model="pm.dataSetParamForm.data.inputType" :options="pm.dataSetParamInputTypeOptions"
							option-label="name" option-value="value" class="input w-full">
						</p-dropdown>
					</div>
				</div>
				<div class="field grid">
					<label for="${pid}dspFormInputPayload" class="field-label col-12 mb-2"
						title="<@spring.message code='dataSetParam.inputPayload.desc' />">
						<@spring.message code='inputConfig' />
					</label>
					<div class="field-input col-12">
						<p-textarea id="${pid}dspFormInputPayload" v-model="pm.dataSetParamForm.data.inputPayload" type="text"
							class="input w-full" name="inputPayload" rows="3" maxlength="2000">
						</p-textarea>
					</div>
				</div>
			</div>
			<div class="page-form-foot flex-grow-0 flex justify-content-center gap-2 pt-2">
				<p-button type="submit" label="<@spring.message code='confirm' />"></p-button>
			</div>
		</form>
	</div>
</p-dialog>
<p-dialog :header="pm.dataSetPropertyForm.title" append-to="body"
	position="center" :dismissable-mask="true" :modal="true"
	v-model:visible="pm.dataSetPropertyForm.show" @show="onDataSetPropertyFormPanelShow">
	<div class="page page-form">
		<form id="${pid}dataSetPropertyForm" class="flex flex-column">
			<div class="page-form-content flex-grow-1 px-2 py-1 panel-content-size-xs panel-content-size-xs-mwh overflow-y-auto">
				<div class="field grid">
					<label for="${pid}dsppFormName" class="field-label col-12 mb-2"
						title="<@spring.message code='dataSetProperty.name.desc' />">
						<@spring.message code='name' />
					</label>
					<div class="field-input col-12">
						<p-inputtext id="${pid}dsppFormName" v-model="pm.dataSetPropertyForm.data.name" type="text"
							class="input w-full" name="name" required maxlength="100" autofocus>
						</p-inputtext>
					</div>
				</div>
				<div class="field grid">
					<label for="${pid}dsppFormType" class="field-label col-12 mb-2">
						<@spring.message code='type' />
					</label>
					<div class="field-input col-12">
						<p-dropdown id="${pid}dsppFormType" v-model="pm.dataSetPropertyForm.data.type" :options="pm.dataSetPropertyTypeOptions"
							option-label="name" option-value="value" class="input w-full">
						</p-dropdown>
					</div>
				</div>
				<div class="field grid">
					<label for="${pid}dsppFormLabel" class="field-label col-12 mb-2"
						title="<@spring.message code='dataSetProperty.label.desc' />">
						<@spring.message code='displayName' />
					</label>
					<div class="field-input col-12">
						<p-inputtext id="${pid}dsppFormLabel" v-model="pm.dataSetPropertyForm.data.label" type="text"
							class="input w-full" name="label" maxlength="100">
						</p-inputtext>
					</div>
				</div>
				<div class="field grid">
					<label for="${pid}dsppFormDefaultValue" class="field-label col-12 mb-2"
						title="<@spring.message code='dataSetProperty.defaultValue.desc' />">
						<@spring.message code='defaultValue' />
					</label>
					<div class="field-input col-12">
						<p-inputtext id="${pid}dsppFormDefaultValue" v-model="pm.dataSetPropertyForm.data.defaultValue" type="text"
							class="input w-full" name="defaultValue" maxlength="500">
						</p-inputtext>
					</div>
				</div>
				<div class="field grid">
					<label for="${pid}dsppFormEvaluated" class="field-label col-12 mb-2">
						<@spring.message code='enableExpression' />
					</label>
					<div class="field-input col-12">
						<p-selectbutton id="${pid}dsppFormEvaluated" v-model="pm.dataSetPropertyForm.data.evaluated" :options="pm.booleanOptions"
							option-label="name" option-value="value" class="input w-full">
						</p-selectbutton>
					</div>
				</div>
				<div class="field grid">
					<label for="${pid}dsppFormExpression" class="field-label col-12 mb-2"
						title="<@spring.message code='dataSetProperty.expression.desc' />">
						<@spring.message code='expression' />
					</label>
					<div class="field-input col-12">
						<p-textarea id="${pid}dsppFormExpression" v-model="pm.dataSetPropertyForm.data.expression" type="text"
							class="input w-full" name="expression" rows="3" maxlength="1000"
							:disabled="!pm.dataSetPropertyForm.data.evaluated">
						</p-textarea>
						<div class="mt-1" v-if="pm.dataSetPropertyForm.data.evaluated">
							<p-tabview class="xs-tabview text-color-secondary text-sm">
								<p-tabpanel header="<@spring.message code='insertPropName' />">
									<div>
										<p-button v-for="p in pm.dataSetPropertyForm.avaliableProperties" :key="p.name"
											type="button" :label="p.name"
											@click="onInsertExpPropName(p.name)" class="p-button-secondary p-button-sm mr-1 mt-1">
										</p-button>
									</div>
									<div class="mt-2 text-color-secondary">
										<@spring.message code='dataSetProperty.expression.usePropName.desc' />
									</div>
								</p-tabpanel>
								<p-tabpanel header="<@spring.message code='insertOperator' />">
									<div>
										<p-button type="button" label="+" title="<@spring.message code='exp.addition' />"
											@click="onInsertExpOperator('+')" class="p-button-secondary p-button-sm mr-1 mt-1">
										</p-button>
										<p-button type="button" label="-" title="<@spring.message code='exp.subtraction' />"
											@click="onInsertExpOperator('-')" class="p-button-secondary p-button-sm mr-1 mt-1">
										</p-button>
										<p-button type="button" label="*" title="<@spring.message code='exp.multiplication' />"
											@click="onInsertExpOperator('*')" class="p-button-secondary p-button-sm mr-1 mt-1">
										</p-button>
										<p-button type="button" label="/" title="<@spring.message code='exp.division' />"
											@click="onInsertExpOperator('/')" class="p-button-secondary p-button-sm mr-1 mt-1">
										</p-button>
										<p-button type="button" label="%" title="<@spring.message code='exp.modulus' />"
											@click="onInsertExpOperator('%')" class="p-button-secondary p-button-sm mr-1 mt-1">
										</p-button>
										<p-button type="button" label="("
											@click="onInsertExpOperator('(')" class="p-button-secondary p-button-sm mr-1 mt-1">
										</p-button>
										<p-button type="button" label=")"
											@click="onInsertExpOperator(')')" class="p-button-secondary p-button-sm mr-1 mt-1">
										</p-button>
									</div>
									<div>
										<p-button type="button" label="&gt;" title="<@spring.message code='exp.gt' />"
											@click="onInsertExpOperator('&gt;')" class="p-button-secondary p-button-sm mr-1 mt-1">
										</p-button>
										<p-button type="button" label="&lt;" title="<@spring.message code='exp.lt' />"
											@click="onInsertExpOperator('&lt;')" class="p-button-secondary p-button-sm mr-1 mt-1">
										</p-button>
										<p-button type="button" label="&gt;=" title="<@spring.message code='exp.ge' />"
											@click="onInsertExpOperator('&gt;=')" class="p-button-secondary p-button-sm mr-1 mt-1">
										</p-button>
										<p-button type="button" label="&lt;=" title="<@spring.message code='exp.le' />"
											@click="onInsertExpOperator('&lt;=')" class="p-button-secondary p-button-sm mr-1 mt-1">
										</p-button>
										<p-button type="button" label="==" title="<@spring.message code='exp.eq' />"
											@click="onInsertExpOperator('==')" class="p-button-secondary p-button-sm mr-1 mt-1">
										</p-button>
										<p-button type="button" label="!=" title="<@spring.message code='exp.ne' />"
											@click="onInsertExpOperator('!=')" class="p-button-secondary p-button-sm mr-1 mt-1">
										</p-button>
									</div>
									<div>
										<p-button type="button" label="&&" title="<@spring.message code='exp.and' />"
											@click="onInsertExpOperator('&&')" class="p-button-secondary p-button-sm mr-1 mt-1">
										</p-button>
										<p-button type="button" label="||" title="<@spring.message code='exp.or' />"
											@click="onInsertExpOperator('||')" class="p-button-secondary p-button-sm mr-1 mt-1">
										</p-button>
										<p-button type="button" label="!" title="<@spring.message code='exp.not' />"
											@click="onInsertExpOperator('!')" class="p-button-secondary p-button-sm mr-1 mt-1">
										</p-button>
										<p-button type="button" label="?"
											@click="onInsertExpOperator('?')" class="p-button-secondary p-button-sm mr-1 mt-1">
										</p-button>
										<p-button type="button" label=":"
											@click="onInsertExpOperator(':')" class="p-button-secondary p-button-sm mr-1 mt-1">
										</p-button>
									</div>
								</p-tabpanel>
							</p-tabview>
						</div>
					</div>
				</div>
			</div>
			<div class="page-form-foot flex-grow-0 flex justify-content-center gap-2 pt-2">
				<p-button type="submit" label="<@spring.message code='confirm' />"></p-button>
			</div>
		</form>
	</div>
</p-dialog>
<script>
(function(po)
{
	po.showDataSetParamForm = function(action, data, submitHandler)
	{
		data = $.extend(true,
				{
					name: "",
					type: "${ParamDataType.STRING}",
					required: true,
					inputType: "${ParamInputType.TEXT}"
				},
				po.vueRaw(data));
		
		var pm = po.vuePageModel();
		pm.dataSetParamForm.title = "<@spring.message code='parameter' />" + " - " + action;
		pm.dataSetParamForm.data = data;
		pm.dataSetParamForm.submitHandler = submitHandler;
		pm.dataSetParamForm.show = true;
	};

	po.showDataSetPropertyForm = function(action, data, submitHandler, avaliableProperties)
	{
		data = $.extend(true,
				{
					name: "",
					type: "${PropertyDataType.STRING}",
					evaluated: false
				},
				po.vueRaw(data));
		
		var pm = po.vuePageModel();
		pm.dataSetPropertyForm.title = "<@spring.message code='property' />" + " - " + action;
		pm.dataSetPropertyForm.data = data;
		pm.dataSetPropertyForm.submitHandler = submitHandler;
		pm.dataSetPropertyForm.avaliableProperties = avaliableProperties;
		pm.dataSetPropertyForm.show = true;
	};
	
	po.vuePageModel(
	{
		dataSetParamForm:
		{
			show: false,
			title: "",
			data: {},
			submitHandler: null
		},
		dataSetPropertyForm:
		{
			show: false,
			title: "",
			data: {},
			submitHandler: null,
			avaliableProperties: []
		},
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
	
	po.vueMethod(
	{
		onDataSetParamFormPanelShow: function()
		{
			var fm = po.vueFormModel();
			var pm = po.vuePageModel();
			
			var form = po.elementOfId("${pid}dataSetParamForm", document.body);
			po.setupSimpleForm(form, pm.dataSetParamForm.data, function()
			{
				var close = true;
				
				if(pm.dataSetParamForm.submitHandler)
				{
					var data = $.extend(true, {}, po.vueRaw(pm.dataSetParamForm.data));
					close = pm.dataSetParamForm.submitHandler(data);
				}
				
				pm.dataSetParamForm.show = (close === false);
			});
		},
		onDataSetPropertyFormPanelShow: function()
		{
			var fm = po.vueFormModel();
			var pm = po.vuePageModel();
			
			var form = po.elementOfId("${pid}dataSetPropertyForm", document.body);
			po.setupSimpleForm(form, pm.dataSetPropertyForm.data, function()
			{
				var close = true;
				
				if(pm.dataSetPropertyForm.submitHandler)
				{
					var data = $.extend(true, {}, po.vueRaw(pm.dataSetPropertyForm.data));
					close = pm.dataSetPropertyForm.submitHandler(data);
				}
				
				pm.dataSetPropertyForm.show = (close === false);
			});
		},
		onInsertExpOperator: function(text)
		{
			var form = po.elementOfId("${pid}dataSetPropertyForm", document.body);
			var textarea = po.elementOfId("${pid}dsppFormExpression", form);
			var newVal = $.insertAtCaret(textarea, text);
			
			var pm = po.vuePageModel();
			pm.dataSetPropertyForm.data.expression = newVal;
		},
		onInsertExpPropName: function(name)
		{
			var form = po.elementOfId("${pid}dataSetPropertyForm", document.body);
			var textarea = po.elementOfId("${pid}dsppFormExpression", form);
			
			var hasSq = false;
			var hasDq = false;
			var hasSpecial = false;
			
			for(var i=0; i<name.length; i++)
			{
				var ch = name.charAt(i);
				
				if(ch == "'")
					hasSq = true;
				else if(ch == "\"")
					hasDq = true;
				else if(!((ch >= 'a' && ch <='z') || (ch >= 'A' && ch <='Z') || ch == '_'))
					hasSpecial = true;
			}
			
			if(hasSq && hasDq)
			{
				$.tipWarn("<@spring.message code='sqWhileDqPropNameInExpIsIllegal' />");
				return;
			}
			
			if(hasSq)
				name = "[\"" + name + "\"]";
			else if(hasDq)
				name = "['" + name + "']";
			else if(hasSpecial)
				name = "['" + name + "']";
			
			var newVal = $.insertAtCaret(textarea, name);
			
			var pm = po.vuePageModel();
			pm.dataSetPropertyForm.data.expression = newVal;
		},
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
		formatPropertyEvaludated: function(data)
		{
			return po.formatBooleanValue(data.evaluated);
		},
	});
})
(${pid});
</script>
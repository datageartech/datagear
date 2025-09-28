/*
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
 */

/**
 * 图表设置库，参数表单、数据表格。
 * 全局变量名：window.chartFactory.chartSetting
 * 
 * 加载时依赖：
 *   无
 * 
 * 运行时依赖:
 *   jquery.js
 *   chartFactory.js
 */
(function(global)
{
	var CF = (global.chartFactory || (global.chartFactory = {}));
	var CST = (CF.chartSetting || (CF.chartSetting = {}));
	var builtinOptionNames = (CF.builtinOptionNames || (CF.builtinOptionNames = {}));
	
	//org.datagear.analysis.DataSetParam.InputType
	CST.DataSetParamInputType =
	{
		TEXT: "text",
		SELECT: "select",
		DATE: "date",
		TIME: "time",
		DATETIME: "datetime",
		RADIO: "radio",
		CHECKBOX: "checkbox",
		TEXTAREA: "textarea"
	};
	
	CST.labels = (CST.labels ||
	{
		confirm: "确定",
		close: "X",
		yes: "是",
		no: "否",
		param: "参数",
		data: "数据",
		colon: "：",
		chartParam: "图表参数",
		chartData: "图表数据",
		serialNumber: "序号",
		dataDetail: "数据明细"
	});
	
	//是否禁用日期组件输入框的浏览器自动完成功能，浏览器自动完成功能会阻挡日期选择框，默认禁用
	CST.disableDateAwareInputAutocomplete = (CST.disableDateAwareInputAutocomplete || true);
	
	/**
	 * 渲染数据集参数值表单。
	 * 
	 * @param parent 用于渲染表单的父元素，如果不是<form>元素，此函数将会自动新建<form>子元素，<form>元素结构也允许预先自定义
	 * @param dataSetParams 数据集参数集，格式参考：org.datagear.analysis.DataSetParam，也可附加"label"属性，用于定义输入项标签
	 * @param options 渲染配置项，格式为：
	 * 			{
	 *              chartTheme: {...}              //可选，用于支持渲染表单样式的图表主题
	 *              inChartElement: false,          //可选，要渲染的表单是否处于图表元素内	 
	 * 				submit: function(formData){},  //可选，提交处理函数
	 * 				paramValues: {...},     	   //可选，初始参数值
	 * 				readonly: false,			   //可选，是否只读
	 * 				submitText: "...",       	   //可选，提交按钮文本内容
	 *              labelColon: "..."              //可选，标签冒号值
	 * 				yesText: "...",       		   //可选，"是"选项文本内容
	 * 				noText: "...",       		   //可选，"否"选项文本内容
	 * 				render: function(){}		   //可选，渲染后回调函数
	 * 			}
	 * @return 表单HTML元素
	 */
	CST.renderDataSetParamValueForm = function(parent, dataSetParams, options)
	{
		options = CF.extend(
		{
			inChartElement: false,
			submitText: CST.labels.confirm,
			labelColon: CST.labels.colon,
			readonly: false,
			yesText: CST.labels.yes,
			noText: CST.labels.no
		},
		(options || {}));
		
		var paramValues = (options.paramValues || {});
		var InputType = CST.DataSetParamInputType;
		
		var form;
		
		if(CF.isEleMatches(parent, "form"))
			form = parent;
		else
		{
			form = CF.eleCreate("form", "dg-generated-ele");
			CF.eleAppend(parent, form);
		}
		
		CF.eleAddClass(form, "dg-dpform");
		
		//创建表单样式表
		if(options.chartTheme)
		{
			if(options.inChartElement)
				CST.dspvFormThemeStyle(options.chartTheme, true);
			else
			{
				var themeStyleName = CST.dspvFormThemeStyle(options.chartTheme, false);
				CF.eleAddClass(form, themeStyleName);
				CF.eleData(form, CF.builtinPropName("dpFormThemeClassName"), themeStyleName);
			}
		}
		
		var head = CF.eleOfSelector(".dg-dpform-head", form);
		var content = CF.eleOfSelector(".dg-dpform-content", form);
		var foot = CF.eleOfSelector(".dg-dpform-foot", form);
		
		//允许预先自定义表单结构
		if(head == null)
		{
			head = CF.eleCreate("div", "dg-dpform-head dg-generated-ele");
			CF.eleAppend(form, head);
		}
		
		if(content == null)
		{
			content = CF.eleCreate("div", "dg-dpform-content dg-generated-ele");
			CF.eleAppend(form, content);
		}
		
		if(foot == null)
		{
			foot = CF.eleCreate("div", "dg-dpform-foot dg-generated-ele");
			CF.eleAppend(form, foot);
		}
		
		var dftBooleanOptions = [ { name: options.yesText, value: "true" }, { name: options.noText, value: "false" } ];
		
		for(let i=0; i<dataSetParams.length; i++)
		{
			let dsp = dataSetParams[i];
			let value = paramValues[dsp.name];
			
			let item = CF.eleCreate("div", "dg-dpform-item dg-generated-ele");
			CF.eleAppend(content, item);
			
			let labelDiv = CF.eleCreate("div", "dg-dpform-item-label");
			CF.eleAppend(item, labelDiv);
			CST.renderDspvFormLabel(form, labelDiv, dsp, options);
			
			let valueDiv = CF.eleCreate("div", "dg-dpform-item-value");
			CF.eleAppend(item, valueDiv);
			
			if(dsp.type == CF.DataSetParamType.BOOLEAN)
			{
				let defaultSelOpts = (!dsp.inputPayload ? dftBooleanOptions : null);
				
				//XXX 上面不应将defaultSelOpts对象赋值给dsp.inputPayload，因为dsp.inputPayload应是字符串类型，
				//图表编辑保存时会将dsp传输至后台而进行类型转换，如果赋值，则会报错
				
				if(dsp.inputType == InputType.RADIO)
					CST.renderDspvFormInputRadio(form, valueDiv, dsp, value, options, defaultSelOpts);
				else if(dsp.inputType == InputType.CHECKBOX)
					CST.renderDspvFormInputCheckbox(form, valueDiv, dsp, value, options, defaultSelOpts);
				else
					CST.renderDspvFormInputSelect(form, valueDiv, dsp, value, options, defaultSelOpts);
			}
			else if(dsp.type == CF.DataSetParamType.STRING)
			{
				if(dsp.inputType == InputType.SELECT)
					CST.renderDspvFormInputSelect(form, valueDiv, dsp, value, options);
				else if(dsp.inputType == InputType.DATE)
					CST.renderDspvFormInputDate(form, valueDiv, dsp, value, options);
				else if(dsp.inputType == InputType.TIME)
					CST.renderDspvFormInputTime(form, valueDiv, dsp, value, options);
				else if(dsp.inputType == InputType.DATETIME)
					CST.renderDspvFormInputDateTime(form, valueDiv, dsp, value, options);
				else if(dsp.inputType == InputType.RADIO)
					CST.renderDspvFormInputRadio(form, valueDiv, dsp, value, options);
				else if(dsp.inputType == InputType.CHECKBOX)
					CST.renderDspvFormInputCheckbox(form, valueDiv, dsp, value, options);
				else if(dsp.inputType == InputType.TEXTAREA)
					CST.renderDspvFormInputTextarea(form, valueDiv, dsp, value, options);
				else
					CST.renderDspvFormInputText(form, valueDiv, dsp, value, options);
			}
			else if(dsp.type == CF.DataSetParamType.NUMBER)
			{
				if(dsp.inputType == InputType.SELECT)
					CST.renderDspvFormInputSelect(form, valueDiv, dsp, value, options);
				else if(dsp.inputType == InputType.RADIO)
					CST.renderDspvFormInputRadio(form, valueDiv, dsp, value, options);
				else if(dsp.inputType == InputType.CHECKBOX)
					CST.renderDspvFormInputCheckbox(form, valueDiv, dsp, value, options);
				else if(dsp.inputType == InputType.TEXTAREA)
					CST.renderDspvFormInputTextarea(form, valueDiv, dsp, value, options);
				else
					CST.renderDspvFormInputText(form, valueDiv, dsp, value, options);
			}
		}
		
		if(!options.readonly)
		{
			let submitBtn = CF.eleOfSelector("[type='submit']", foot);
			
			//允许自定义提交按钮
			if(submitBtn == null)
			{
				submitBtn = CF.eleCreate("button", "dg-generated-ele");
				CF.eleAttr(submitBtn, "type", "submit");
				CF.eleHtmlContent(submitBtn, options.submitText);
				CF.eleAppend(foot, submitBtn);
			}
		}
		
		var submitHandler = function()
		{
			if(options.readonly)
				return false;
			
			let validationOk = CST.validateDspvForm(this);
			let submitBtn = CF.eleOfSelector("[type='submit']", foot);
			
			if(validationOk)
				CF.eleRemoveClass(submitBtn, "dg-form-invalid");
			else
				CF.eleAddClass(submitBtn, "dg-form-invalid");
			
			if(!validationOk)
				return false;
			
			if(options.submit)
			{
				let formData = CST.getDataSetParamValueObj(this);
				return (options.submit.call(this, formData) == true);
			}
			else
				return false;
		};
		
		CF.eleData(form, CF.builtinPropName("dpFormSubmitHandler"), submitHandler);
		CF.eleOn(form, "submit", submitHandler);
		
		if(options.render)
			options.render(form);
		
		return form;
	};
	
	/**
	 * 销毁数据集参数值表单。
	 * 
	 * @param ancestor 渲染数据集参数值的<form>表单元素，或者它的祖先元素（其所有内部数据集参数值表单都会被销毁）。
	 */
	CST.destroyDataSetParamValueForm = function(ancestor)
	{
		var forms = [];
		
		if(CF.isEleMatches(ancestor, "form"))
			forms.push(ancestor);
		else
			forms = CF.elesOfSelector("form.dg-dpform", ancestor);
		
		forms.forEach((form) =>
		{
			if(CF.isEleMatches(form, ".dg-generated-ele"))
			{
				CF.eleRemove(form);
			}
			else
			{
				CF.eleRemoveClass(form, "dg-dpform");
				
				let themeStyleName = CF.eleData(form, CF.builtinPropName("dpFormThemeClassName"));
				if(themeStyleName)
					CF.eleRemoveClass(form, themeStyleName);
				
				let genEles = CF.elesOfSelector(".dg-generated-ele", form);
				genEles.forEach((genEle) =>
				{
					CF.eleRemove(genEle);
				});
			}
			
			let submitHandler = CF.eleData(form, CF.builtinPropName("dpFormSubmitHandler"));
			if(submitHandler != null)
				CF.eleOff(form, "submit", submitHandler);
		});
	};
	
	CST.dspvFormThemeStyle = function(chartTheme, isSubStyle)
	{
		var name = CF.builtinPropName("dataSetParamValueForm" + (isSubStyle ? "SubYes" : "SubNo"));
		return CF.themeStyleSheet(chartTheme, name, function()
		{
			var color = CF.themeGradualColor(chartTheme, 1);
			var bgColor = CF.themeGradualColor(chartTheme, 0);
			var borderColor = CF.themeGradualColor(chartTheme, 0.5);
			
			var cssPrefix = (isSubStyle ? " " : "") + ".dg-dpform";
			
			var css =
			[
				{
					name: cssPrefix,
					value:
					{
						"color": color,
						"background-color": bgColor,
						"border-color": borderColor
					}
				},
				{
					name:
					[
						cssPrefix + " .dg-dpform-item-value input",
						cssPrefix + " .dg-dpform-item-value textarea",
						cssPrefix + " .dg-dpform-item-value select",
						cssPrefix + " .dg-dpform-item-value select option",
						cssPrefix + " .dg-dpform-item-value .input"
					],
					value:
					{
						"color": color,
						"background-color": bgColor,
						"border-color": borderColor
					}
				},
				{
					name:
					[
						cssPrefix + " button",
						cssPrefix + " input[type=button]",
						cssPrefix + " input[type=submit]",
						cssPrefix + " .button"
					],
					value:
					{
						"color": color,
						"background-color": CF.themeGradualColor(chartTheme, 0.1),
						"border-color": borderColor
					}
				},
				{
					name:
					[
						cssPrefix + " button:hover",
						cssPrefix + " input[type=button]:hover",
						cssPrefix + " input[type=submit]:hover",
						cssPrefix + " .button:hover"
					],
					value:
					{
						"background-color": CF.themeGradualColor(chartTheme, 0.3)
					}
				}
			];
			
			return css;
		});
	};
	
	/**
	 * 渲染表单项标签。
	 * 
	 * @param form
	 * @param parent 渲染标签的父容器元素
	 * @param dataSetParam
	 * @param formOptions
	 */
	CST.renderDspvFormLabel = function(form, parent, dataSetParam, formOptions)
	{
		var label = CF.eleCreate("label");
		CF.eleHtmlContent(label, (dataSetParam.label ? dataSetParam.label : dataSetParam.name));
		CF.eleAttr(label, "title", dataSetParam.desc);
		CF.eleAppend(parent, label);
		
		return label;
	};
	
	/**
	 * 渲染输入项：文本框
	 * 
	 * @param form
	 * @param parent 渲染输入项的父容器元素
	 * @param dataSetParam
	 * @param value 可选
	 * @param formOptions
	 */
	CST.renderDspvFormInputText = function(form, parent, dataSetParam, value, formOptions)
	{
		var input = CF.eleCreate("input", "dg-dpform-input");
		CF.eleAttr(input, "type", "text");
		CF.eleAttr(input, "name", dataSetParam.name);
		CF.eleAttr(input, "value", (value == null ? "" : value));
		
		if(CF.isLiteralTrue(dataSetParam.required))
			CF.eleAttr(input, "dg-validation-check-required", "true");
		
		if(CF.DataSetParamType.NUMBER == dataSetParam.type)
			CF.eleAttr(input, "dg-validation-check-number", "true");
		
		CF.eleAppend(parent, input);
	};
	
	/**
	 * 渲染输入项：下拉框
	 * 
	 * dataSetParam.inputPayload格式可以为：
	 * null、空字符串
	 * 或者
	 * "[ 待选项名值对象, ... ]"、
	 * "{ multiple: true | false, options: [ 待选项名值对象, ... ] }"  //数据集定义功能时
	 * 或者
	 * [ 待选项名值对象, ... ]、
	 * { multiple: true | false, options: [ 待选项名值对象, ... ] }    //看板表单功能时
	 * 
	 * 其中，待选项名值对象格式允许为：
	 * { name: "...", value: ... }、{name: "..."}、{value: ...}、"..."
	 * 
	 * @param form
	 * @param parent 渲染输入项的父容器元素
	 * @param dataSetParam
	 * @param value 可选
	 * @param formOptions
	 * @param defaultSelOpts 可选，默认下拉框选项集
	 */
	CST.renderDspvFormInputSelect = function(form, parent, dataSetParam, value, formOptions, defaultSelOpts)
	{
		var payload = CST.evalDataSetParamInputPayload(dataSetParam, []);
		
		if(defaultSelOpts && CF.isEmpty(payload))
			payload = defaultSelOpts;
		
		if(CF.isString(payload))
			payload = [ payload ];
		
		if(CF.isArray(payload))
			payload = { multiple: false, options: payload };
		
		value = (value == null ? [] : (CF.isArray(value) ? value : [ value ]));
		
		var input = CF.eleCreate("select", "dg-dpform-input");
		CF.eleAttr(input, "name", dataSetParam.name);
		
		if(payload.multiple)
			CF.eleAttr(input, "multiple", "true");
		
		CF.eleAppend(parent, input);
		
		var opts = (payload.options || []);
		
		for(var i=0; i<opts.length; i++)
		{
			var opt = opts[i];
			
			var optName = (opt.name != null ? opt.name : opt.value);
			var optVal = (opt.value != null ? opt.value : opt.name);
			optName = (optName == null ? opt : optName);
			optVal = (optVal == null ? opt : optVal);
			
			var opt = CF.eleCreateWithAttr("option", "value", optVal);
			CF.eleHtmlContent(opt, optName);
			
			if(CST.containsValueAsString(value, optVal))
				CF.eleAttr(opt, "selected", "selected");
			
			CF.eleAppend(input, opt);
		}
		
		if(CF.isLiteralTrue(dataSetParam.required))
			CF.eleAttr(input, "dg-validation-check-required", "true");
		
		if(CF.DataSetParamType.NUMBER == dataSetParam.type)
			CF.eleAttr(input, "dg-validation-check-number", "true");
	};
	
	/**
	 * 渲染输入项：日期框
	 * 
	 * dataSetParam.inputPayload格式可以为：
	 * null、空字符串、
	 * "{ format: '...y...m...d...' }"  //数据集定义功能时
	 * { format: '...y...m...d...' }    //看板表单功能时
	 * 
	 * 其中：
	 * y：年份（4位长度）
	 * m：月份（2位长度，01-12）
	 * d：天（2位长度，01-31）
	 * 
	 * 注意：上述format只是发送至服务端的格式，显示格式由输入框自身决定。
	 * 
	 * @param form
	 * @param parent 渲染输入项的父容器元素
	 * @param dataSetParam
	 * @param value 可选
	 * @param formOptions
	 */
	CST.renderDspvFormInputDate = function(form, parent, dataSetParam, value, formOptions)
	{
		var options = CST.evalDataSetParamInputPayload(dataSetParam, {});
		options = CF.extend({ format: "y-m-d" }, options);
		
		var input = CF.eleCreate("input", "dg-dpform-input");
		CF.eleAttr(input, "type", "date");
		CF.eleAttr(input, "name", dataSetParam.name);
		CF.eleAttr(input, "value", (value == null ? "" : value));
		
		if(CF.isLiteralTrue(dataSetParam.required))
			CF.eleAttr(input, "dg-validation-check-required", "true");
		
		CF.eleAppend(parent, input);
	};
	
	/**
	 * 渲染输入项：时间框
	 * 
	 * dataSetParam.inputPayload格式可以为：
	 * null、空字符串、
	 * "{ format: '...h...i...s...' }"  //数据集定义功能时
	 * { format: '...h...i...s...' }    //看板表单功能时
	 * 
	 * 其中：
	 * h：小时（2位长度24时制，00-23）
	 * i：分钟（2位长度，00-59）
	 * s：秒数（2位长度，00-59）
	 * 
	 * 注意：上述format只是发送至服务端的格式，显示格式由输入框自身决定。
	 * 
	 * @param form
	 * @param parent 渲染输入项的父容器元素
	 * @param dataSetParam
	 * @param value 可选
	 * @param formOptions
	 */
	CST.renderDspvFormInputTime = function(form, parent, dataSetParam, value, formOptions)
	{
		var options = CST.evalDataSetParamInputPayload(dataSetParam, {});
		options = CF.extend({ format: "h:i:s" }, options);
		
		var input = CF.eleCreate("input", "dg-dpform-input");
		CF.eleAttr(input, "type", "time");
		CF.eleAttr(input, "name", dataSetParam.name);
		CF.eleAttr(input, "value", (value == null ? "" : value));
		CF.eleAttr(input, "step", "1");
		
		if(CF.isLiteralTrue(dataSetParam.required))
			CF.eleAttr(input, "dg-validation-check-required", "true");
		
		CF.eleAppend(parent, input);
	};
	
	/**
	 * 渲染输入项：日期时间框
	 * 
	 * dataSetParam.inputPayload格式可以为：
	 * null、空字符串、
	 * "{ format: '...y...m...d...h...i...s...' }"  //数据集定义功能时
	 * { format: '...y...m...d...h...i...s...' }    //看板表单功能时
	 * 
	 * 其中：
	 * y：年份（4位长度）
	 * m：月份（2位长度，01-12）
	 * d：天（2位长度，01-31）
	 * h：小时（2位长度24时制，00-23）
	 * i：分钟（2位长度，00-59）
	 * s：秒数（2位长度，00-59）
	 * 
	 * 注意：上述format只是发送至服务端的格式，显示格式由输入框自身决定。
	 * 
	 * @param form
	 * @param parent 渲染输入项的父容器元素
	 * @param dataSetParam
	 * @param value 可选
	 * @param formOptions
	 */
	CST.renderDspvFormInputDateTime = function(form, parent, dataSetParam, value, formOptions)
	{
		var options = CST.evalDataSetParamInputPayload(dataSetParam, {});
		options = CF.extend({ format: "y-m-d h:i:s" }, options);
		
		var input = CF.eleCreate("input", "dg-dpform-input");
		CF.eleAttr(input, "type", "datetime-local");
		CF.eleAttr(input, "name", dataSetParam.name);
		CF.eleAttr(input, "value", (value == null ? "" : value));
		CF.eleAttr(input, "step", "1");
		
		if(CF.isLiteralTrue(dataSetParam.required))
			CF.eleAttr(input, "dg-validation-check-required", "true");
		
		CF.eleAppend(parent, input);
	};
	
	/**
	 * 渲染输入项：单选框
	 * 
	 * dataSetParam.inputPayload格式可以为：
	 * null、空字符串
	 * "[ 待选项名值对象, ... ]"  //数据集定义功能时
	 * [ 待选项名值对象, ... ]    //看板表单功能时
	 * 
	 * 其中，待选项名值对象格式允许为：
	 * { name: "...", value: ... }、{name: "..."}、{value: ...}、"..."
	 * 
	 * @param form
	 * @param parent 渲染输入项的父容器元素
	 * @param dataSetParam
	 * @param value 可选
	 * @param formOptions
	 * @param defaultSelOpts 可选，默认单选框选项集
	 */
	CST.renderDspvFormInputRadio = function(form, parent, dataSetParam, value, formOptions, defaultSelOpts)
	{
		var opts = CST.evalDataSetParamInputPayload(dataSetParam, []);
		
		if(defaultSelOpts && CF.isEmpty(opts))
			opts = defaultSelOpts;
		
		if(!CF.isArray(opts))
			opts = [ opts ];
		
		var inputsWrapper = CF.eleCreate("div", "dg-dpform-inputs-wrapper");
		CF.eleAppend(parent, inputsWrapper);
		
		for(var i=0; i<opts.length; i++)
		{
			var opt = opts[i];
			
			var optName = (opt.name != null ? opt.name : opt.value);
			var optVal = (opt.value != null ? opt.value : opt.name);
			optName = (optName == null ? opt : optName);
			optVal = (optVal == null ? opt : optVal);
			
			var eleId = CF.uid();
			
			var wrapper = CF.eleCreate("div", "dg-dpform-radio-wrapper");
			CF.eleAppend(inputsWrapper, wrapper);
			
			var input = CF.eleCreateWithAttr("input", "type", "radio", "class", "dg-dpform-input",
							"id", eleId, "name", dataSetParam.name, "value", optVal);
			CF.eleAppend(wrapper, input);
			
			var label = CF.eleCreateWithAttr("label", "for", eleId);
			CF.eleHtmlContent(label, optName);
			CF.eleAppend(wrapper, label);
			
			if(CST.isEqualAsString(value, optVal))
				CF.eleAttr(input, "checked", "checked");
			
			if(CF.isLiteralTrue(dataSetParam.required))
				CF.eleAttr(input, "dg-validation-check-required", "true");
			
			if(CF.DataSetParamType.NUMBER == dataSetParam.type)
				CF.eleAttr(input, "dg-validation-check-number", "true");
		}
	};
	
	/**
	 * 渲染输入项：复选框
	 * 
	 * dataSetParam.inputPayload格式可以为：
	 * null、空字符串
	 * "[ 待选项名值对象, ... ]"  //数据集定义功能时
	 * [ 待选项名值对象, ... ]    //看板表单功能时
	 * 
	 * 其中，待选项名值对象格式允许为：
	 * { name: "...", value: ... }、{name: "..."}、{value: ...}、"..."
	 * 
	 * @param form
	 * @param parent 渲染输入项的父容器元素
	 * @param dataSetParam
	 * @param value 可选，值、值数组
	 * @param formOptions
	 * @param defaultSelOpts 可选，默认复选框选项集
	 */
	CST.renderDspvFormInputCheckbox = function(form, parent, dataSetParam, value, formOptions, defaultSelOpts)
	{
		var opts = CST.evalDataSetParamInputPayload(dataSetParam, []);
		
		if(defaultSelOpts && CF.isEmpty(payload))
			opts = defaultSelOpts;
		
		if(!CF.isArray(opts))
			opts = [ opts ];
		
		value = (value == null ? [] : (CF.isArray(value) ? value : [ value ]));
		
		var inputsWrapper = CF.eleCreate("div", "dg-dpform-inputs-wrapper");
		CF.eleAppend(parent, inputsWrapper);
		
		for(var i=0; i<opts.length; i++)
		{
			var opt = opts[i];
			
			var optName = (opt.name != null ? opt.name : opt.value);
			var optVal = (opt.value != null ? opt.value : opt.name);
			optName = (optName == null ? opt : optName);
			optVal = (optVal == null ? opt : optVal);
			
			var eleId = CF.uid();
			
			var wrapper = CF.eleCreate("div", "dg-dpform-radio-wrapper");
			CF.eleAppend(inputsWrapper, wrapper);
			
			var input = CF.eleCreateWithAttr("input", "type", "checkbox", "class", "dg-dpform-input",
							"id", eleId, "name", dataSetParam.name, "value", optVal);
			CF.eleAppend(wrapper, input);
			
			var label = CF.eleCreateWithAttr("label", "for", eleId);
			CF.eleHtmlContent(label, optName);
			CF.eleAppend(wrapper, label);
			
			if(CST.containsValueAsString(value, optVal))
				CF.eleAttr(input, "checked", "checked");
			
			if(CF.isLiteralTrue(dataSetParam.required))
				CF.eleAttr(input, "dg-validation-check-required", "true");
			
			if(CF.DataSetParamType.NUMBER == dataSetParam.type)
				CF.eleAttr(input, "dg-validation-check-number", "true");
		}
	};
	
	/**
	 * 渲染输入项：文本域
	 * 
	 * @param form
	 * @param parent 渲染输入项的父容器元素
	 * @param dataSetParam
	 * @param value 可选
	 * @param formOptions
	 */
	CST.renderDspvFormInputTextarea = function(form, parent, dataSetParam, value, formOptions)
	{
		var input = CF.eleCreate("textarea", "dg-dpform-input");
		CF.eleAttr(input, "type", "text");
		CF.eleAttr(input, "name", dataSetParam.name);
		CF.eleAttr(input, "value", (value == null ? "" : value));
		
		if(CF.isLiteralTrue(dataSetParam.required))
			CF.eleAttr(input, "dg-validation-check-required", "true");
		
		if(CF.DataSetParamType.NUMBER == dataSetParam.type)
			CF.eleAttr(input, "dg-validation-check-number", "true");
		
		CF.eleAppend(parent, input);
	};
	
	//日期格式解析支持类，支持"...y...m...d...h...i...s..."格式日期解析
	//注意：这里保留了大写'Y'标识符，以兼容旧数据集预览时的格式
	CST.dateFormatter =
	{
		parseDate: function(date, format)
		{
			date = (date || "");
			
			var dateObj=
			{
				y: 0, m: 1, d: 1,
				h: 0, i: 0, s: 0
			};
			
			format = this._parseFormat(format);
			
			var idx = 0;
			for(var j=0; j<format.length; j++)
			{
				var fmt = format[j];
				
				if(fmt == 'y' || fmt == 'Y')
					idx = this._readAndParseSet(date, idx, 4, dateObj, "y");
				else if(fmt == 'm')
					idx = this._readAndParseSet(date, idx, 2, dateObj, "m");
				else if(fmt == 'd')
					idx = this._readAndParseSet(date, idx, 2, dateObj, "d");
				else if(fmt == 'H' || fmt == 'h')
					idx = this._readAndParseSet(date, idx, 2, dateObj, "h");
				else if(fmt == 'i')
					idx = this._readAndParseSet(date, idx, 2, dateObj, "i");
				else if(fmt == 's')
					idx = this._readAndParseSet(date, idx, 2, dateObj, "s");
				else
					idx += fmt.length;
			}
			
			return new Date(dateObj.y, dateObj.m - 1, dateObj.d, dateObj.h, dateObj.i, dateObj.s, 0);
		},
		formatDate: function (date, format)
		{
			var re = "";
			
			var y = date.getFullYear(), m = date.getMonth() + 1, d = date.getDate(),
				h = date.getHours(), i = date.getMinutes(), s = date.getSeconds();
			
			format = this._parseFormat(format);
			
			for(var j=0; j<format.length; j++)
			{
				var fmt = format[j];
				
				if(fmt == 'y' || fmt == 'Y')
					re += this._paddingLeftZero(y, 4);
				else if(fmt == 'm')
					re += this._paddingLeftZero(m, 2);
				else if(fmt == 'd')
					re += this._paddingLeftZero(d, 2);
				else if(fmt == 'H' || fmt == 'h')
					re += this._paddingLeftZero(h, 2);
				else if(fmt == 'i')
					re += this._paddingLeftZero(i, 2);
				else if(fmt == 's')
					re += this._paddingLeftZero(s, 2);
				else
					re += fmt;
			}
			
			return re;
		},
		_parseFormat: function(format)
		{
			format = (format || "");
			
			if(this._formatArrayCache[format])
				return this._formatArrayCache[format];
			
			var re = [];
			
			var tmp = "";
			for(var i=0; i<format.length; i++)
			{
				var c = format[i];
				
				if(c == 'y' || c == 'Y' || c == 'm' || c == 'd'
					 || c == 'H' || c == 'h' || c == 'i' || c == 's')
				{
					if(tmp)
					{
						re.push(tmp);
						tmp = "";
					}
					
					re.push(c);
				}
				else
					tmp += c;
			}
			
			if(tmp)
				re.push(tmp);
			
			this._formatArrayCache[format] = re;
			
			return re;
		},
		_readAndParseSet: function(str, index, maxCount, obj, propName)
		{
			index = (index == null ? 0 : index);
			endIdx = (index + maxCount > str.length ? str.length : index + maxCount);
			
			var sub = "";
			
			for(; index<endIdx; index++)
			{
				var c = str[index];
				
				if(c >= '0' && c <= '9')
				{
					if(sub == '0')
					{
						if(c == '0')
							;
						else
							sub = c;
					}
					else
						sub += c;
				}
				else
				{
					break;
				}
			}
			
			if(sub)
				obj[propName] = parseInt(sub);
			
			return index;
		},
		_paddingLeftZero: function(number, length)
		{
			var re = number + "";
			
			while(re.length < length)
				re = "0" + re;
			
			return re;
		},
		_formatArrayCache:{}
	};
	
	CST.evalDataSetParamInputPayload = function(dataSetParam, defaultValue)
	{
		var inputPayload = dataSetParam.inputPayload;
		
		if(CF.isEmpty(inputPayload))
			return defaultValue;
		else if(CF.isString(inputPayload))
			return CF.evalSilently(dataSetParam.inputPayload, defaultValue);
		else
			return defaultValue;
	};
	
	CST.NUMBER_REGEX = /^-?\d+\.?\d*$/;
	
	/**
	 * 校验数据集参数值表单的必填项、数值项。
	 * 
	 * @param form
	 * @return true 验证通过；false 验证不通过
	 */
	CST.validateDspvForm = function(form)
	{
		var validationOk = true;
		
		var valueWrappers = CF.elesOfSelector(".dg-dpform-item-value", form);
		
		valueWrappers.forEach((valueWrapper) =>
		{
			let inputs = CF.elesOfSelector("[dg-validation-check-required]", valueWrapper);
			
			if(CF.isEmpty(inputs))
				return;
			
			let type = CST.eleInputType(inputs[0]);
			let isCheckboxRadio = (type == "checkbox" || type == "radio");
			let checkedValues = [];
			
			inputs.forEach((input) =>
			{
				let val = CST.eleInputActualValue(input);
				
				if(isCheckboxRadio)
				{
					if(!CF.isEmpty(val))
						checkedValues.puth(val);
				}
				else
				{
					if(CF.isEmpty(val))
					{
						CF.eleAddClass(input, "dg-validation-required");
						validationOk = false;
					}
					else
						CF.eleRemoveClass(input, "dg-validation-required");
				}
			});
			
			if(isCheckboxRadio)
			{
				let inputsWrapper = CF.eleOfSelector(valueWrapper, ".dg-dpform-inputs-wrapper");
				
				if(CF.isEmpty(checkedValues))
				{
					CF.eleAddClass(inputsWrapper, "dg-validation-required");
					validationOk = false;
				}
				else
					CF.eleRemoveClass(inputsWrapper, "dg-validation-required");
			}
		});
		
		valueWrappers.forEach((valueWrapper) =>
		{
			let inputs = CF.elesOfSelector("[dg-validation-check-number]", valueWrapper);
			
			if(CF.isEmpty(inputs))
				return;
			
			let type = CST.eleInputType(inputs[0]);
			let isCheckboxRadio = (type == "checkbox" || type == "radio");
			let checkedValues = [];
			
			inputs.forEach((input) =>
			{
				let val = CST.eleInputActualValue(input);
				
				if(isCheckboxRadio)
				{
					if(!CF.isEmpty(val))
						checkedValues.puth(val);
				}
				else
				{
					val = (CF.isEmpty(val) ? [] : (CF.isArray(val) ? val : [ val ]));
					
					if(!CST.isNonEmptyAllNumberic(val))
					{
						CF.eleAddClass(input, "dg-validation-number");
						validationOk = false;
					}
					else
						CF.eleRemoveClass(input, "dg-validation-number");
				}
			});
			
			if(isCheckboxRadio)
			{
				let inputsWrapper = CF.eleOfSelector(valueWrapper, ".dg-dpform-inputs-wrapper");
				
				if(!CST.isNonEmptyAllNumberic(checkedValues))
				{
					CF.eleAddClass(inputsWrapper, "dg-validation-number");
					validationOk = false;
				}
				else
					CF.eleRemoveClass(inputsWrapper, "dg-validation-number");
			}
		});
		
		return validationOk;
	};
	
	/**
	 * 获取数据集参数表单的参数值对象。
	 * 
	 * 图表参数化数据集要求这里的表单返回对象必须符合以下规则：
	 * 1. 所有输入项必须在返回对象中出现；（避免出现因未出现而导致无法覆盖上次设置数据集参数值的情况）
	 * 2. 如果返回对象的某个属性值为空字符串，则应将其置为null；（表示未填写，用于支持参数化数据集的“<#if param??>”语法）
	 * 3. 如果返回对象的某个属性值为空数组，则应将其置为null；（表示未填写，用于支持参数化数据集的“<#if param??>”语法）
	 * 4. 如果返回对象的某个属性值为数组，则元素不允许出现null；
	 * 切记遵循上述规则，否则可能导致已定义的参数化数据集逻辑错误。
	 * 
	 * @param form
	 */
	CST.getDataSetParamValueObj = function(form)
	{
		var re = {};
		
		var inputs = CF.elesOfSelector("input, textarea, select", form);
		inputs.forEach((input) =>
		{
			let name = input.name;
			
			if(CF.isEmpty(name))
				return;
				
			if(CF.isEleMatches(input, "button, input[type='submit'], input[type='reset'], input[type='button']"))
				return;
			
			let arrayValue = CF.isEleMatches(input, "input[type='checkbox'], select[multiple]");
			let value = CF.eleInputActualValue(input);
			let prevValue = re[name];
			
			if(arrayValue)
			{
				if(prevValue == null)
				{
					prevValue = [];
					re[name] = prevValue;
				}
				
				if(value != null)
					prevValue.push(value);
			}
			else if(prevValue == null)
			{
				re[name] = (value === undefined ? null : value);
			}
			//可能出现同名单值输入项的情况
			else
			{
				if(value != null)
				{
					if(CF.isArray(prevValue))
						prevValue.push(value);
					else
					{
						prevValue = [ prevValue ];
						prevValue.push(value);
						re[name] = prevValue;
					}
				}
			}
		});
		
		//空字符串、空数组置为null
		for(let name in re)
		{
			let v = re[name];
			
			if(CF.isEmpty(v))
				re[name] = null;
		}
		
		return re;
	};
	
	CST.setDataSetParamValueObj = function(form, data)
	{
		data = (data || {});
		
		var inputs = CF.elesOfSelector("input, textarea, select", form);
		inputs.forEach((input) =>
		{
			let name = input.name;
			
			if(CF.isEmpty(name))
				return;
			
			let value = data[name];
			CST.eleInputActualValue(input, value);
		});
	};
	
	/**
	 * 获取/设置单个输入框的实际值
	 * 
	 * @param input 输入框HTML元素
	 * @param 可选，要设置的值，多余复选框、单选框、多选下拉框时可以是数组
	 * @returns 实际值，如果复选框、单选框、下拉框未选中时，将返回undefined
	 */
	CST.eleInputActualValue = function(input, value)
	{
		var type = CST.eleInputType(input);
		
		if(value === undefined)
		{
			var re = undefined;
			
			if(type == "checkbox" || type == "radio")
			{
				if(CF.isEleMatches(input, ":checked"))
					re = input.value;
				else
					re = undefined;
			}
			else if(CF.isEleMatches(input, "select"))
			{
				if(CF.eleAttr(input, "multiple"))
				{
					re = Array.from(input.selectedOptions).map(option => option.value);
				}
				else
				{
					re = input.value;
				}
			}
			else
			{
				re = input.value;
			}
			
			return re;
		}
		else
		{
			if(type == "checkbox" || type == "radio")
			{
				if(value == null)
					input.checked = false;
				else if(CF.isArray(value))
					input.checked = CST.containsValueAsString(value, input.value);
				else
					input.checked = CST.isEqualAsString(value, input.value);
			}
			else if(CF.isEleMatches(input, "select"))
			{
				value = (value == null ? null : (CF.isArray(value) ? value : [ value ]));
				
				for (let i = 0; i < input.options.length; i++)
				{
					let option = input.options[i];
					option.selected = (value == null ? false : CST.containsValueAsString(value, option.value));
				}
			}
			else
			{
				input.value = (value == null ? "" : value);
			}
		}
	};
	
	CST.eleInputType = function(input)
	{
		var type = (CF.eleAttr(input, "type") || "").toLowerCase();
		return type;
	};
	
	CST.isNonEmptyAllNumberic = function(array)
	{
		for(let i=0; i<array.length; i++)
		{
			let val = array[i];
			
			if(CF.isEmpty(val) || CF.isNumber(val))
				continue;
			
			if(!CST.NUMBER_REGEX.test(val))
				return false;
		}
		
		return true;
	};
	
	CST.containsValueAsString = function(array, value)
	{
		if(array === value)
			return true;
		
		for(var i=0; i<array.length; i++)
		{
			if(CST.isEqualAsString(array[i], value))
				return true;
		}
		
		return false;
	};
	
	CST.isEqualAsString = function(a, b)
	{
		if(a == null)
			return (b == null);
		else if(b == null)
			return (a == null);
		else
			return (a == b || (a+"") == (b+""));
	};
	
	CST.getDataSetParamValueForm = function($parent)
	{
		return $(".dg-dpform", $parent);
	};

	CST.getDataSetParamValueFormHead = function(form)
	{
		return $(".dg-dpform-head", form);
	};
	
	CST.getDataSetParamValueFormContent = function(form)
	{
		return $(".dg-dpform-content", form);
	};
	
	CST.getDataSetParamValueFormFoot = function(form)
	{
		return $(".dg-dpform-foot", form);
	};
	
	CST.bindChartSettingPanelEvent = function(chart)
	{
		var disableSetting = chart.disableSetting();
		var noNeedParam = (disableSetting.param == true || !chart.hasDataSetParam());
		
		if(noNeedParam && disableSetting.data == true)
			return false;
		
		var chartOptions = chart.options();
		var builtinSetting = CF.builtinOptionValue(chartOptions, builtinOptionNames.builtinSetting);
		//显示模式："hover" 悬浮显示（默认）、"display" 始终显示
		var displayMode = (builtinSetting ? builtinSetting.displayMode : null);
		displayMode = (CF.isEmpty(displayMode) ? "hover" : displayMode);
		
		var $chart = chart.elementJquery();
		
		if(!$chart.data("dgChartSettingHasBindEvent"))
		{
			$chart.data("dgChartSettingHasBindEvent", true);
			
			if(displayMode == "display")
			{
				CST.showChartSettingBox(chart);
			}
			else if(displayMode == "hover")
			{
				var mouseenterHandler = function(event)
				{
					if(chart.isActive())
						CST.showChartSettingBox(chart);
				};
				var mouseleaveHandler = function(event)
				{
					if(CST.isChartSettingParamPanelClosed(chart)
						&& CST.isChartSettingDataPanelClosed(chart))
					{
						CST.hideChartSettingBox(chart);
					}
				};
				
				$chart.mouseenter(mouseenterHandler).mouseleave(mouseleaveHandler);
				
				$chart.data("dgChartSettingMouseEnterHandler", mouseenterHandler);
				$chart.data("dgChartSettingMouseLeaveHandler", mouseleaveHandler);
			}
		}
		
		return true;
	};
	
	CST.unbindChartSettingPanelEvent = function(chart)
	{
		var $chart = chart.elementJquery();
		var mouseenterHandler = $chart.data("dgChartSettingMouseEnterHandler");
		var mouseleaveHandler = $chart.data("dgChartSettingMouseLeaveHandler");
		
		$chart.removeData("dgChartSettingHasBindEvent");
		
		if(mouseenterHandler)
			$chart.off("mouseenter", mouseenterHandler);
		if(mouseleaveHandler)
			$chart.off("mouseleave", mouseleaveHandler);
		
		var $box = $(".dg-chart-setting-box", $chart);
		
		CST.destroyDataSetParamValueForm($box);
		$box.remove();
	};
	
	CST.showChartSettingBox = function(chart)
	{
		var disableSetting = chart.disableSetting();
		
		var $chart = chart.elementJquery();
		var $box = $(".dg-chart-setting-box", $chart);
		
		if($box.length <= 0)
		{
			var chartOptions = chart.options();
			var builtinSetting = CF.builtinOptionValue(chartOptions, builtinOptionNames.builtinSetting);
			
			//显示位置："rightTop" 右上（默认）；"leftTop" 左上；"leftBottom" 左下；"rightBottom" 右下
			var boxPosition = (builtinSetting ? builtinSetting.position : null);
			boxPosition = (CF.isEmpty(boxPosition) ? "rightTop" : boxPosition);
			
			//显示方向："row" 横向（默认）；"column" 竖向
			var boxDirection = (builtinSetting ? builtinSetting.direction : null);
			boxDirection = (CF.isEmpty(boxDirection) ? "row" : boxDirection);
			
			var boxPositionCssName = "dg-position-" + boxPosition;
			var boxDirectionCssName = "dg-flex-dir-" + boxDirection;
			
			$box = $("<div class='dg-chart-setting-box "+boxPositionCssName+" "+boxDirectionCssName+"' />").appendTo($chart);
			
			CST.setChartSettingBoxThemeStyle(chart, $box);
			
			//参数
			if(!disableSetting.param && chart.hasDataSetParam())
			{
				var $button = $("<button type='button' class='dg-chart-setting-button dg-chart-setting-param-button' />")
						.html(CST.labels.param);
				CST.setChartSettingButtonOptions($button, (builtinSetting ? builtinSetting.paramButton : null));
				$button.appendTo($box);
				
				$button.click(function()
				{
					CST.closeChartSettingDataPanel(chart);
					
					if(CST.isChartSettingParamPanelClosed(chart))
						CST.openChartSettingParamPanel($box, chart);
					else
						CST.closeChartSettingParamPanel(chart);
				});
				
				$chart.click(function(event)
				{
					if(!CST.isChartSettingParamPanelClosed(chart))
					{
						if($(event.target).closest(".dg-chart-setting-box").length == 0)
							CST.closeChartSettingParamPanel(chart);
					}
				});
			}
			
			//数据
			if(!disableSetting.data)
			{
				var $button = $("<button type='button' class='dg-chart-setting-button dg-chart-setting-data-button' />")
						.html(CST.labels.data);
				CST.setChartSettingButtonOptions($button, (builtinSetting ? builtinSetting.dataButton : null));
				$button.appendTo($box);
				
				$button.click(function()
				{
					CST.closeChartSettingParamPanel(chart);
					
					if(CST.isChartSettingDataPanelClosed(chart))
						CST.openChartSettingDataPanel($box, chart);
					else
						CST.closeChartSettingDataPanel(chart);
				});
				
				$chart.click(function(event)
				{
					if(!CST.isChartSettingDataPanelClosed(chart))
					{
						if($(event.target).closest(".dg-chart-setting-box").length == 0)
							CST.closeChartSettingDataPanel(chart);
					}
				});
			}
		}
		
		$box.show();
	};
	
	CST.hideChartSettingBox = function(chart)
	{
		$(".dg-chart-setting-box", chart.elementJquery()).hide();
	};
	
	//设置按钮选项，格式为：{ text: "", style: "...", styleClass: "..." }
	CST.setChartSettingButtonOptions = function($button, buttonOptions)
	{
		if(!buttonOptions)
			return;
		
		if(buttonOptions.text)
			$button.html(buttonOptions.text);
		
		if(buttonOptions.style)
			CF.eleStyle($button, buttonOptions.style);
		
		if(buttonOptions.styleClass)
			$button.addClass(buttonOptions.styleClass);
	};
	
	CST.setChartSettingBoxThemeStyle = function(chart, $box)
	{
		chart.themeStyleSheet(CF.builtinPropName("ChartSettingBox"), function()
		{
			var color = chart.themeGradualColor(1);
			var bgColor = chart.themeGradualColor(0);
			var btnBorderColor = chart.themeGradualColor(0.5);
			var panelBorderColor = chart.themeGradualColor(0.3);
			var shadowColor = chart.themeGradualColor(0.9);
			
			var css =
			[
				{
					name: " .dg-chart-setting-box .dg-chart-setting-button",
					value:
					{
						"color": color,
						"background-color": bgColor,
						"border-color": btnBorderColor
					}
				},
				{
					name: " .dg-chart-setting-box .dg-chart-setting-button:hover",
					value:
					{
						"background-color": chart.themeGradualColor(0.2)
					}
				},
				{
					name: " .dg-chart-setting-box .dg-chart-setting-panel",
					value:
					{
						"color": color,
						"background-color": bgColor,
						"border-color": panelBorderColor,
						"box-shadow": "0px 0px 6px " + shadowColor,
						"-webkit-box-shadow": "0px 0px 6px " + shadowColor
					}
				},
				{
					name: " .dg-chart-setting-box .dg-chart-setting-panel .dg-datasetbind-section",
					value:
					{
						"color": color,
						"background-color": bgColor,
						"border-color": panelBorderColor
					}
				},
				{
					name:
					[
						" .dg-chart-setting-box .dg-chart-setting-panel .dg-chart-setting-panel-head button",
						" .dg-chart-setting-box .dg-chart-setting-panel .dg-chart-setting-panel-foot button"
					],
					value:
					{
						"color": color,
						"background-color": chart.themeGradualColor(0.1),
						"border-color": btnBorderColor
					}
				},
				{
					name:
					[
						" .dg-chart-setting-box .dg-chart-setting-panel .dg-chart-setting-panel-head button:hover",
						" .dg-chart-setting-box .dg-chart-setting-panel .dg-chart-setting-panel-foot button:hover"
					],
					value:
					{
						"background-color": chart.themeGradualColor(0.3)
					}
				}
			];
			
			return css;
		});
	};
	
	/**
	 * 打开图表参数面板。
	 */
	CST.openChartSettingParamPanel = function($box, chart)
	{
		var dataSetBinds = chart.dataSetBinds();
		var $panel = $(".dg-chart-setting-param-panel", $box);
		
		if($panel.length <= 0)
		{
			$panel = $("<div class='dg-chart-setting-panel dg-chart-setting-param-panel' />").appendTo($box);
			
			CST.showChartSetingPanelOpacityOut($box, $panel, chart);
			
			var $panelHead = $("<div class='dg-chart-setting-panel-head' />").appendTo($panel);
			var $panelContent = $("<div class='dg-chart-setting-panel-content' />").appendTo($panel);
			var $panelFoot = $("<div class='dg-chart-setting-panel-foot' />").appendTo($panel);
			
			$("<div class='dg-chart-setting-panel-head-title' />").html(CST.labels.chartParam).appendTo($panelHead);
			var $headBtns = $("<div class='dg-chart-setting-panel-head-btns' />").appendTo($panelHead);
			$("<button type='button' class='dg-chart-setting-panel-closebtn' />")
				.html(CST.labels.close).appendTo($headBtns)
				.click(function()
				{
					CST.closeChartSettingParamPanel(chart);
				});
			
			var $button = $("<button type='button' />").html(CST.labels.confirm).appendTo($panelFoot);
			
			CST.setChartSetingPanelContentSizeRange(chart, $panel, $panelContent, $panelFoot);
			
			for(var i=0; i<dataSetBinds.length; i++)
			{
				var params = chart.dataSetParams(dataSetBinds[i]);
				
				if(!params || params.length == 0)
					continue;
				
				var myTitle = CST.evalDataSetBindPanelTitle(chart, dataSetBinds, i);
				
				var $fp = $("<div class='dg-datasetbind-section' />").data("dataSetBindIndex", i).appendTo($panelContent);
				var $head = $("<div class='dg-datasetbind-section-head' />").html(myTitle).appendTo($fp);
				var $content = $("<div class='dg-datasetbind-section-content' />").appendTo($fp);
				CST.renderDataSetParamValueForm($content, params,
				{
					chartTheme: chart.theme(),
					inChartElement: true,
					submit: function()
					{
						$("button", $panelFoot).click();
					},
					paramValues: chart.dataSetParamValues(i),
					render: function()
					{
						CST.getDataSetParamValueFormFoot(this).hide();
					}
				});
				
				$head.click(function()
				{
					$(".dg-datasetbind-section-content", $(this).parent()).toggle();
				});
				
				CST.toggleParamFormContentByIgnoreFetch($panel, $fp, chart, i);
			}
			
			$button.click(function()
			{
				var $thisButton = $(this);
				var validateOk = true;
				var paramValuess = [];
				
				$(".dg-datasetbind-section", $panelContent).each(function()
				{
					if(!validateOk)
						return;
					
					var $this = $(this);
					var $form = CST.getDataSetParamValueForm($this);
					var dataSetBindIndex = $this.data("dataSetBindIndex");
					var ignoreFetch = chart.dataSetIgnoreFetch(dataSetBindIndex);
					
					if(!ignoreFetch && !CST.validateDspvForm($form))
						validateOk = false;
					else
					{
						var myParamValues = CST.getDataSetParamValueObj($form);
						paramValuess.push({ index : dataSetBindIndex, paramValues: myParamValues });
					}
				});
				
				if(validateOk)
				{
					$thisButton.removeClass("dg-param-value-form-invalid");
					
					var chartOptions = chart.options();
					var builtinSetting = CF.builtinOptionValue(chartOptions, builtinOptionNames.builtinSetting);
					var convertParamFormValue = (builtinSetting ? builtinSetting.convertParamFormValue : null);
					convertParamFormValue = (convertParamFormValue == null ? true : convertParamFormValue);
					
					for(var i=0; i<paramValuess.length; i++)
					{
						CST.dataSetBindParamValues(chart, paramValuess[i].index, paramValuess[i].paramValues, convertParamFormValue);
					}
					
					var doRefresh = true;
					var onParamFormSubmit = (builtinSetting ? builtinSetting.onParamFormSubmit : null);
					
					//执行提交前回调
					if(onParamFormSubmit)
					{
						doRefresh = onParamFormSubmit.call(chartOptions, chart);
					}
					
					//不执行刷新
					if(doRefresh === false || doRefresh == "break-show" || doRefresh == "break-hide")
					{
						if(doRefresh === false || doRefresh == "break-show")
						{
							//不关闭面板
						}
						else
						{
							CST.closeChartSettingParamPanel(chart);
						}
					}
					else
					{
						if(doRefresh == "continue-show")
						{
							//不关闭面板
						}
						else
						{
							CST.closeChartSettingParamPanel(chart);
						}
						
						chart.refreshData();
					}
				}
				else
					$thisButton.addClass("dg-param-value-form-invalid");
			});
		}
		else
		{
			CST.showChartSetingPanelOpacityOut($box, $panel, chart);
			
			$(".dg-datasetbind-section", $panel).each(function()
			{
				var dataSetBindIndex = $(this).data("dataSetBindIndex");
				var $form = CST.getDataSetParamValueForm(this);
				
				CST.setDataSetParamValueObj($form, chart.dataSetParamValues(dataSetBindIndex));
				CST.toggleParamFormContentByIgnoreFetch($panel, this, chart, dataSetBindIndex);
			});
		}
		
		CST.adjustChartSetingPanelPosition($box, $panel, $(".dg-chart-setting-param-button", $box), chart);
		
		//聚焦至第一个可操作输入框
		CST.focusOnFirstInput($("form:first", $panel));
	};
	
	CST.dataSetBindParamValues = function(chart, dataSetBindIndex, paramValues, convert)
	{
		//这里设置参数应采用inflate模式，因为数据集允许隐式参数（未明确定义数据集参数的参数化语法），这里不应清除它们
		chart.dataSetParamValues(dataSetBindIndex, paramValues, true, convert);
	};
	
	CST.toggleParamFormContentByIgnoreFetch = function($panel, $section, chart, dataSetBindIndex)
	{
		var ignoreFetch = chart.dataSetIgnoreFetch(dataSetBindIndex);
		var $content = $(".dg-datasetbind-section-content", $section);
		
		if(ignoreFetch)
			$content.hide();
		else
			$content.show();
	};
	
	/**
	 * 关闭图表参数面板。
	 */
	CST.closeChartSettingParamPanel = function(chart)
	{
		$(".dg-chart-setting-param-panel", chart.elementJquery()).hide();
	};
	
	/**
	 * 获取图表参数面板。
	 */
	CST.getChartSettingParamPanel = function(chart)
	{
		return $(".dg-chart-setting-param-panel", chart.elementJquery());
	};
	
	CST.isChartSettingParamPanelClosed = function(chart)
	{
		var $panel = $(".dg-chart-setting-param-panel", chart.elementJquery());
		
		return ($panel.length == 0 || $panel.is(":hidden"));
	};
	
	/**
	 * 打开图表数据面板。
	 */
	CST.openChartSettingDataPanel = function($box, chart)
	{
		var dataSetBinds = chart.dataSetBinds();
		var $panel = $(".dg-chart-setting-data-panel", $box);
		
		if($panel.length <= 0)
		{
			$panel = $("<div class='dg-chart-setting-panel dg-chart-setting-data-panel' />").appendTo($box);
			
			CST.showChartSetingPanelOpacityOut($box, $panel, chart);
			
			var $panelHead = $("<div class='dg-chart-setting-panel-head' />").appendTo($panel);
			var $panelContent = $("<div class='dg-chart-setting-panel-content' />").appendTo($panel);
			var $panelFoot = $("<div class='dg-chart-setting-panel-foot' />").appendTo($panel);
			
			$("<div class='dg-chart-setting-panel-head-title' />").html(CST.labels.chartData).appendTo($panelHead);
			var $headBtns = $("<div class='dg-chart-setting-panel-head-btns' />").appendTo($panelHead);
			$("<button type='button' class='dg-chart-setting-panel-closebtn' />")
				.html(CST.labels.close).appendTo($headBtns)
				.click(function()
				{
					CST.closeChartSettingDataPanel(chart);
				});
			
			CST.setChartSettingDataPanelThemeStyle(chart, $panel);
			CST.setChartSetingPanelContentSizeRange(chart, $panel, $panelContent,$panelFoot);
			
			for(var i=0; i<dataSetBinds.length; i++)
			{
				var myTitle = CST.evalDataSetBindPanelTitle(chart, dataSetBinds, i);
				
				var $fp = $("<div class='dg-datasetbind-section' />").data("dataSetBindIndex", i).appendTo($panelContent);
				var $head = $("<div class='dg-datasetbind-section-head' />").html(myTitle).appendTo($fp);
				var $content = $("<div class='dg-datasetbind-section-content' />").appendTo($fp);
				
				var tableId = CST.initDataSetBindDataTable(chart, dataSetBinds, i, $content);
				
				$fp.data("chartDataTableId", tableId);
			}
		}
		else
		{
			CST.showChartSetingPanelOpacityOut($box, $panel, chart);
			
			$(".dg-datasetbind-section", $panel).each(function()
			{
				var dataSetBindIndex = $(this).data("dataSetBindIndex");
				var tableId = $(this).data("chartDataTableId");
				var $table = $("#"+tableId, this);
				
				CST.updateChartSettingDataTableData(chart, dataSetBinds, dataSetBindIndex, $table);
			});
		}
		
		CST.adjustChartSetingPanelPosition($box, $panel, $(".dg-chart-setting-data-button", $box), chart);
	};
	
	/**
	 * 关闭图表数据面板。
	 */
	CST.closeChartSettingDataPanel = function(chart)
	{
		$(".dg-chart-setting-data-panel", chart.elementJquery()).hide();
	};
	
	/**
	 * 获取图表数据面板。
	 */
	CST.getChartSettingDataPanel = function(chart)
	{
		return $(".dg-chart-setting-data-panel", chart.elementJquery());
	};
	
	CST.isChartSettingDataPanelClosed = function(chart)
	{
		var $panel = $(".dg-chart-setting-data-panel", chart.elementJquery());
		
		return ($panel.length == 0 || $panel.is(":hidden"));
	};
	
	CST.initDataSetBindDataTable = function(chart, dataSetBinds, index, $parent)
	{
		var dataSetBind = dataSetBinds[index];
		var dataSetFields = chart.dataSetFields(dataSetBind);
		var signFields = [];
		
		for(var i=0; i<dataSetFields.length; i++)
		{
			var signs = (chart.dataSetFieldSigns(dataSetBind, dataSetFields[i]) || []);
			if(signs.length > 0)
			{
				signFields.push(dataSetFields[i]);
			}
		}
		
		//如果没有任何标记，则认为全部标记，比如表格图表
		if(signFields.length == 0)
			signFields = dataSetFields;
		
		var columns = [];
		
		columns.push(
		{
			title: CST.labels.serialNumber,
			style: "width:4em",
			render: function(data, index)
			{
				return (index + 1);
			}
		});
		
		for(var i=0; i<signFields.length; i++)
		{
			var column =
			{
				title: CST.evalDataSetBindDataTableColumnTitle(chart, dataSetBind, signFields[i]),
				fieldName: signFields[i].name,
				render: function(data)
				{
					return CF.escapeHtml(data[this.fieldName]);
				}
			};
			
			columns.push(column);
		}
		
		if(chart.isMutableModel(dataSetBind))
		{
			columns.push(
			{
				title: CST.labels.dataDetail,
				render: function(data)
				{
					return CF.toJsonString(data);
				}
			});
		}
		
		var tableId = CF.uid();
		
		var table = $("<table width='100%' class='dg-chart-data-table'></table>");
		table.attr("id", tableId);
		table.data("tableColumns", columns);
		
		var thead = $("<thead />").appendTo(table);
		var tr = $("<tr />").appendTo(thead);
		
		for(var i=0; i<columns.length; i++)
		{
			var th = $("<th />").html(columns[i].title).appendTo(tr);
			if(columns[i].style)
				th.attr("style", columns[i].style);
		}
		
		$("<tbody />").appendTo(table);
		table.appendTo($parent);
		
		CST.updateChartSettingDataTableData(chart, dataSetBinds, index, table);
		
		return tableId;
	};
	
	CST.evalDataSetBindDataTableColumnTitle = function(chart, dataSetBind, dataSetField)
	{
		var title = chart.dataSetFieldAlias(dataSetBind, dataSetField);
		title = (title == dataSetField.name ? title : title + "-" + dataSetField.name);
		var signs = (chart.dataSetFieldSigns(dataSetBind, dataSetField) || []);
		var signInfo = "";
		
		for(var i=0; i<signs.length; i++)
		{
			if(signs[i])
				signInfo += (signInfo ? "," + signs[i] : signs[i]);
		}
		
		return (signInfo ? title + " (" + signInfo +")" : title);
	};
	
	CST.setChartSettingDataPanelThemeStyle = function(chart, $panel)
	{
		chart.themeStyleSheet(CF.builtinPropName("ChartSettingDataPanel"), function()
		{
			var theme = chart.theme();
			//表格背景色应与面板背景色一致，且不能设透明背景色，因为设置了固定列
			var bgColor = chart.themeGradualColor(0);
			
			var cssPrefix = " .dg-chart-setting-box .dg-chart-setting-data-panel";
			
			var css =
			[
				{
					name:
					[
						cssPrefix + " table.dg-chart-data-table thead th",
						cssPrefix + " table.dg-chart-data-table thead td"
					],
					value:
					{
						"color": theme.color,
						"background-color": bgColor
					}
				},
				{
					name:
					[
						cssPrefix + " table.dg-chart-data-table tbody tr",
						cssPrefix + " table.dg-chart-data-table tbody tr td",
					],
					value:
					{
						"color": theme.color
					}
				},
				{
					name:
					[
						cssPrefix + " table.dg-chart-data-table tbody tr:hover",
						cssPrefix + " table.dg-chart-data-table tbody tr:hover td"
					],
					value:
					{
						"background-color": chart.themeGradualColor(0.2)
					}
				}
			];
			
			return css;
		});
	};
	
	CST.updateChartSettingDataTableData = function(chart, dataSetBinds, index, $table)
	{
		var chartResult = chart.updateResult();
		var result = chart.resultOf(chartResult, index);
		var datas = chart.resultDatas(result);
		var columns = ($table.data("tableColumns") || []);
		
		var tbody = $("> tbody", $table);
		tbody.empty();
		
		for(var i=0; i<datas.length; i++)
		{
			var data = (datas[i] || {});
			var tr = $("<tr />").appendTo(tbody);
			
			for(var j=0; j<columns.length; j++)
			{
				var column = columns[j];
				$("<td />").html(column.render(data, i)).appendTo(tr);
			}
		}
	};
	
	CST.evalDataSetBindPanelTitle = function(chart, dataSetBinds, index)
	{
		var title = (dataSetBinds.length > 1 ? (index+1)+". " : "") + chart.dataSetAlias(dataSetBinds[index]);
		if(title != dataSetBinds[index].dataSet.name)
			title += " ("+dataSetBinds[index].dataSet.name+")";
		
		return title;
	};
	
	CST.setChartSetingPanelContentSizeRange = function(chart, $panel, $panelContent, $panelFoot)
	{
		var $chart = chart.elementJquery();
		
		var cw = $chart.width();
		var ch = $chart.height();
		var ww = $(window).width();
		var wh = $(window).height();
		var fh = ($panelFoot.is(":hidden") ? 0 : $panelFoot.outerHeight());
		
		$panelContent.css("min-width", Math.max(cw*2/5, ww*1/5));
		$panelContent.css("max-width", ww*3/5);
		//$panelContent.css("min-height", Math.max(ch*2/5, wh*1/5) - fh);
		$panelContent.css("max-height", wh*3/5 - fh);
	};
	
	CST.showChartSetingPanelOpacityOut = function($box, $panel, chart)
	{
		//先透明显示，避免布局计算错误，后续调整位置后再移除透明
		$panel.addClass("dg-opacity-hide")
			.css("left", -999999).css("top", -999999).css("right", "unset").css("bottom", "unset")
			.show();
	};
	
	CST.adjustChartSetingPanelPosition = function($box, $panel, $btn, chart)
	{
		var docWidth = $(document).width();
		var docHeight = $(document).height();
		
		var width = $panel.outerWidth(true);
		var height = $panel.outerHeight(true);
		var widthGap = width + 20;
		var heightGap = height + 20;
		
		var btnWidth = $btn.outerWidth(true);
		var btnHeight = $btn.outerHeight(true);
		var btnOffset = $btn.offset();
		var btnPosition = $btn.position();
		
		var left = "unset";
		var right = "unset";
		var top = "unset";
		var bottom = "unset";
		
		//按钮右侧有足够空间
		if((docWidth - btnOffset.left - btnWidth) > widthGap)
		{
			left = btnPosition.left + btnWidth;
		}
		//按钮左侧有足够空间
		else if(btnOffset.left > widthGap)
		{
			left = btnPosition.left - width;
		}
		else
		{
			left = btnPosition.left - width/2;
		}
		
		var bottomFirst = true;
		if($box.hasClass("dg-position-leftBottom") || $box.hasClass("dg-position-rightBottom") || $box.hasClass("dg-position-centerBottom"))
			bottomFirst = false;
		
		if(bottomFirst)
		{
			//按钮底部有足够空间
			if((docHeight - btnOffset.top - btnHeight) > heightGap)
			{
				top = btnPosition.top + btnHeight;
			}
			//按钮上部有足够空间
			else if(btnOffset.top > heightGap)
			{
				bottom = btnPosition.top + btnHeight;
			}
			else
			{
				top = btnPosition.top - height/2;
			}
		}
		else
		{
			//按钮上部有足够空间
			if(btnOffset.top > heightGap)
			{
				bottom = btnPosition.top + btnHeight;
			}
			//按钮底部有足够空间
			else if((docHeight - btnOffset.top - btnHeight) > heightGap)
			{
				top = btnPosition.top + btnHeight;
			}
			else
			{
				top = btnPosition.top - height/2;
			}
		}
		
		$panel.css("left", left);
		$panel.css("top", top);
		$panel.css("right", right);
		$panel.css("bottom", bottom);
		
		$panel.removeClass("dg-opacity-hide");
	};
	
	//聚焦至指定元素内的第一个可操作（非只读、非禁用）输入框
	CST.focusOnFirstInput = function(ele)
	{
		var input = $(":input:not(:disabled,[readonly]):first", ele); 
		input.focus();
	};
})
(this);

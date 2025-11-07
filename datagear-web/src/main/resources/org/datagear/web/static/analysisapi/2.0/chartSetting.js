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
(function(global, window)
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
	 * 渲染数据集参数表单。
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
	 * 				render: function(form){}	   //可选，渲染后回调函数
	 * 			}
	 * @return 表单HTML元素
	 */
	CST.renderDataSetParamForm = function(parent, dataSetParams, options)
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
		
		CF.eleAddClass(form, "dg-dspform");
		
		//创建表单样式表
		if(options.chartTheme)
		{
			if(options.inChartElement)
				CST.dspFormThemeStyle(options.chartTheme, true);
			else
			{
				var themeStyleName = CST.dspFormThemeStyle(options.chartTheme, false);
				CF.eleAddClass(form, themeStyleName);
				CF.eleData(form, CF.builtinPropName("dpFormThemeClassName"), themeStyleName);
			}
		}
		
		var head = CF.eleOfSelector(".dg-dspform-head", form);
		var content = CF.eleOfSelector(".dg-dspform-content", form);
		var foot = CF.eleOfSelector(".dg-dspform-foot", form);
		
		//允许预先自定义表单结构
		if(head == null)
		{
			head = CF.eleCreate("div", "dg-dspform-head dg-generated-ele");
			CF.eleAppend(form, head);
		}
		
		if(content == null)
		{
			content = CF.eleCreate("div", "dg-dspform-content dg-generated-ele");
			CF.eleAppend(form, content);
		}
		
		if(foot == null)
		{
			foot = CF.eleCreate("div", "dg-dspform-foot dg-generated-ele");
			CF.eleAppend(form, foot);
		}
		
		var dftBooleanOptions = [ { name: options.yesText, value: "true" }, { name: options.noText, value: "false" } ];
		
		for(let i=0; i<dataSetParams.length; i++)
		{
			let dsp = dataSetParams[i];
			let value = paramValues[dsp.name];
			
			let item = CF.eleCreate("div", "dg-dspform-item dg-generated-ele");
			CF.eleAppend(content, item);
			
			let labelDiv = CF.eleCreate("div", "dg-dspform-item-label");
			CF.eleAppend(item, labelDiv);
			let label = CST.renderDspFormLabel(form, options, labelDiv, dsp);
			
			let valueDiv = CF.eleCreate("div", "dg-dspform-item-value");
			CF.eleAppend(item, valueDiv);
			let input;
			
			if(dsp.type == CF.DataSetParamType.BOOLEAN)
			{
				let defaultSelOpts = (!dsp.inputPayload ? dftBooleanOptions : null);
				
				//XXX 上面不应将defaultSelOpts对象赋值给dsp.inputPayload，因为dsp.inputPayload应是字符串类型，
				//图表编辑保存时会将dsp传输至后台而进行类型转换，如果赋值，则会报错
				
				if(dsp.inputType == InputType.RADIO)
					input = CST.renderDspFormInputRadio(form, options, valueDiv, dsp, value, defaultSelOpts);
				else if(dsp.inputType == InputType.CHECKBOX)
					input = CST.renderDspFormInputCheckbox(form, options, valueDiv, dsp, value, defaultSelOpts);
				else
					input = CST.renderDspFormInputSelect(form, options, valueDiv, dsp, value, defaultSelOpts);
			}
			else if(dsp.type == CF.DataSetParamType.STRING)
			{
				if(dsp.inputType == InputType.SELECT)
					input = CST.renderDspFormInputSelect(form, options, valueDiv, dsp, value);
				else if(dsp.inputType == InputType.DATE)
					input = CST.renderDspFormInputDate(form, options, valueDiv, dsp, value);
				else if(dsp.inputType == InputType.TIME)
					input = CST.renderDspFormInputTime(form, options, valueDiv, dsp, value);
				else if(dsp.inputType == InputType.DATETIME)
					input = CST.renderDspFormInputDateTime(form, options, valueDiv, dsp, value);
				else if(dsp.inputType == InputType.RADIO)
					input = CST.renderDspFormInputRadio(form, options, valueDiv, dsp, value);
				else if(dsp.inputType == InputType.CHECKBOX)
					input = CST.renderDspFormInputCheckbox(form, options, valueDiv, dsp, value);
				else if(dsp.inputType == InputType.TEXTAREA)
					input = CST.renderDspFormInputTextarea(form, options, valueDiv, dsp, value);
				else
					input = CST.renderDspFormInputText(form, options, valueDiv, dsp, value);
			}
			else if(dsp.type == CF.DataSetParamType.NUMBER)
			{
				if(dsp.inputType == InputType.SELECT)
					input = CST.renderDspFormInputSelect(form, options, valueDiv, dsp, value);
				else if(dsp.inputType == InputType.RADIO)
					input = CST.renderDspFormInputRadio(form, options, valueDiv, dsp, value);
				else if(dsp.inputType == InputType.CHECKBOX)
					input = CST.renderDspFormInputCheckbox(form, options, valueDiv, dsp, value);
				else if(dsp.inputType == InputType.TEXTAREA)
					input = CST.renderDspFormInputTextarea(form, options, valueDiv, dsp, value);
				else
					input = CST.renderDspFormInputText(form, options, valueDiv, dsp, value);
			}
			
			let inputId = (input ? CF.eleAttr(input, "id") : null);
			inputId = (CF.isEmpty(inputId) ? CF.eleAttr(input, "dg-label-for-id") : inputId);
			if(!CF.isEmpty(inputId))
			{
				CF.eleAttr(label, "for", inputId);
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
				CF.eleHtml(submitBtn, options.submitText);
				CF.eleAppend(foot, submitBtn);
			}
		}
		
		var submitHandler = function(event)
		{
			event.preventDefault();
			
			if(options.readonly)
				return false;
			
			let validationOk = CST.validateDspForm(this);
			let submitBtn = CF.eleOfSelector("[type='submit']", foot);
			
			if(validationOk)
				CF.eleRemoveClass(submitBtn, "dg-form-invalid");
			else
				CF.eleAddClass(submitBtn, "dg-form-invalid");
			
			if(!validationOk)
				return false;
			
			if(options.submit)
			{
				let formData = CST.getDataSetParamFormData(this);
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
	 * 销毁数据集参数表单。
	 * 
	 * @param ancestor 渲染数据集参数值的<form>表单元素，或者它的祖先元素（其所有内部数据集参数值表单都会被销毁）。
	 */
	CST.destroyDataSetParamForm = function(ancestor)
	{
		var forms = [];
		
		if(CF.isEleMatches(ancestor, "form"))
			forms.push(ancestor);
		else
			forms = CF.elesOfSelector("form.dg-dspform", ancestor);
		
		forms.forEach((form) =>
		{
			if(CF.isEleMatches(form, ".dg-generated-ele"))
			{
				CF.eleRemove(form);
			}
			else
			{
				CF.eleRemoveClass(form, "dg-dspform");
				
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
	
	CST.dspFormThemeStyle = function(chartTheme, isSubStyle)
	{
		var name = CF.builtinPropName("dataSetParamValueForm" + (isSubStyle ? "SubYes" : "SubNo"));
		return CF.themeStyleSheet(chartTheme, name, function()
		{
			var color = CF.themeGradualColor(chartTheme, 1);
			var bgColor = CF.themeGradualColor(chartTheme, -1);
			var bgColor1 = CF.themeGradualColor(chartTheme, 0.2);
			var borderColor = CF.themeGradualColor(chartTheme, 0.4);
			var btnBg = CF.themeGradualColor(chartTheme, 0.1);
			var btnHoverBg = CF.themeGradualColor(chartTheme, 0.2);
			
			var cssPrefix = (isSubStyle ? " " : "") + ".dg-dspform";
			
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
						cssPrefix + " .dg-dspform-item-value input",
						cssPrefix + " .dg-dspform-item-value textarea",
						cssPrefix + " .dg-dspform-item-value select",
						cssPrefix + " .dg-dspform-item-value select option",
						cssPrefix + " .dg-dspform-item-value .dg-dspform-inputs-wrapper"
					],
					value:
					{
						"color": color,
						"background-color": bgColor,
						"border-color": borderColor
					}
				},
				{
					name: cssPrefix + " .dg-dspform-item-value select[multiple] option:checked",
					value:
					{
						"background-color": bgColor1
					}
				},
				{
					name:
					[
						cssPrefix + " button",
						cssPrefix + " input[type='button']",
						cssPrefix + " input[type='submit']",
						cssPrefix + " .button"
					],
					value:
					{
						"color": color,
						"background-color": btnBg,
						"border-color": borderColor
					}
				},
				{
					name:
					[
						cssPrefix + " button:hover",
						cssPrefix + " input[type='button']:hover",
						cssPrefix + " input[type='submit']:hover",
						cssPrefix + " .button:hover"
					],
					value:
					{
						"background-color": btnHoverBg
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
	 * @param formOptions
	 * @param parent 渲染标签的父容器元素
	 * @param dataSetParam
	 */
	CST.renderDspFormLabel = function(form, formOptions, parent, dataSetParam)
	{
		var label = CF.eleCreate("label");
		CF.eleHtml(label, (dataSetParam.label ? dataSetParam.label : dataSetParam.name));
		CF.eleAppend(parent, label);
		
		if(!CF.isEmpty(dataSetParam.desc))
			CF.eleAttr(label, "title", dataSetParam.desc);
		
		return label;
	};
	
	/**
	 * 渲染输入项：文本框
	 * 
	 * @param form
	 * @param formOptions
	 * @param parent 渲染输入项的父容器元素
	 * @param dataSetParam
	 * @param value 可选
	 */
	CST.renderDspFormInputText = function(form, formOptions, parent, dataSetParam, value)
	{
		var input = CF.eleCreate("input", "dg-dspform-input");
		CF.eleAttr(input, "type", "text");
		CF.eleAttr(input, "id", CF.uid());
		CF.eleAttr(input, "name", dataSetParam.name);
		
		if(CF.isLiteralTrue(dataSetParam.required))
			CF.eleAttr(input, "dg-validation-check-required", "true");
		
		if(CF.DataSetParamType.NUMBER == dataSetParam.type)
			CF.eleAttr(input, "dg-validation-check-number", "true");
		
		CF.eleAppend(parent, input);
		CST.eleInputActualValue(input, value);
		
		return input;
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
	 * @param formOptions
	 * @param parent 渲染输入项的父容器元素
	 * @param dataSetParam
	 * @param value 可选
	 * @param defaultSelOpts 可选，默认下拉框选项集
	 */
	CST.renderDspFormInputSelect = function(form, formOptions, parent, dataSetParam, value, defaultSelOpts)
	{
		var payload = CST.evalDataSetParamInputPayload(dataSetParam, []);
		
		if(defaultSelOpts && CF.isEmpty(payload))
			payload = defaultSelOpts;
		
		if(CF.isString(payload))
			payload = [ payload ];
		
		if(CF.isArray(payload))
			payload = { multiple: false, options: payload };
		
		value = (value == null ? [] : (CF.isArray(value) ? value : [ value ]));
		
		var input = CF.eleCreate("select", "dg-dspform-input");
		CF.eleAttr(input, "name", dataSetParam.name);
		CF.eleAttr(input, "id", CF.uid());
		
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
			CF.eleHtml(opt, optName);
			
			CF.eleAppend(input, opt);
		}
		
		if(CF.isLiteralTrue(dataSetParam.required))
			CF.eleAttr(input, "dg-validation-check-required", "true");
		
		if(CF.DataSetParamType.NUMBER == dataSetParam.type)
			CF.eleAttr(input, "dg-validation-check-number", "true");
		
		CST.eleInputActualValue(input, value);
		
		return input;
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
	 * @param formOptions
	 * @param parent 渲染输入项的父容器元素
	 * @param dataSetParam
	 * @param value 可选
	 */
	CST.renderDspFormInputDate = function(form, formOptions, parent, dataSetParam, value)
	{
		var options = CST.evalDataSetParamInputPayload(dataSetParam, {});
		options = CF.extend({ format: "y-m-d" }, options);
		
		let input;
		
		if(!CST.dateFormatter.hasDay(options.format))
		{
			let hasMonth = CST.dateFormatter.hasMonth(options.format);
			let inputId = CF.uid();
			let inputsWrapper = CF.eleCreate("div", "dg-date-widget dg-dspform-inputs-wrapper");
			CF.eleAppend(parent, inputsWrapper);
			
			input = CF.eleCreate("input", "dg-dspform-input dg-date-widget-hidden");
			CF.eleAttr(input, "dg-date-src-format", (hasMonth ? "y-m" : "y"));
			CF.eleAttr(input, "dg-date-dest-format", options.format);
			CF.eleAttr(input, "type", "hidden");
			CF.eleAttr(input, "name", dataSetParam.name);
			CF.eleAttr(input, "dg-label-for-id", inputId);
			CF.eleAppend(inputsWrapper, input);
			
			if(CF.isLiteralTrue(dataSetParam.required))
				CF.eleAttr(input, "dg-validation-check-required", "true");
			
			let inputs = CF.eleCreate("div", "dg-date-widget-inputs");
			CF.eleAppend(inputsWrapper, inputs);
			
			let yearSelect = CF.eleCreate("select", "dg-date-widget-year");
			CF.eleAttr(yearSelect, "id", inputId);
			CF.eleAppend(inputs, yearSelect);
			
			if(hasMonth)
			{
				let separator = CF.eleCreate("div", "dg-date-widget-separator");
				CF.eleAppend(inputs, separator);
				let separatorContent = CF.eleCreate("small");
				CF.eleHtml(separatorContent, "/");
				CF.eleAppend(separator, separatorContent);
				
				let monthSelect = CF.eleCreate("select", "dg-date-widget-month");
				CF.eleAppend(inputs, monthSelect);
				for(let i=0; i<CST.MONTH_OPTIONS.length; i++)
				{
					let opt = CF.eleCreateWithAttr("option", "value", CST.MONTH_OPTIONS[i]);
					CF.eleHtml(opt, CST.MONTH_OPTIONS[i]);
					CF.eleAppend(monthSelect, opt);
				}
			}
			
			let btns = CF.eleCreate("div", "dg-date-widget-year-btns");
			CF.eleAppend(inputsWrapper, btns);
			
			let nowBtn = CF.eleCreateWithAttr("button", "type", "button", "class", "dg-date-widget-year-btn dg-date-widget-now-year-btn");
			CF.eleHtml(nowBtn, "&#9679;");
			CF.eleAppend(btns, nowBtn);
			CF.eleOn(nowBtn, "click", () =>
			{
				CST.eleInputActualValue(input, new Date());
			});
			
			let prevBtn = CF.eleCreateWithAttr("button", "type", "button", "class", "dg-date-widget-year-btn dg-date-widget-prev-year-btn");
			CF.eleHtml(prevBtn, "&#8593;");
			CF.eleAppend(btns, prevBtn);
			CF.eleOn(prevBtn, "click", () =>
			{
				CST.eleYearSelectRollOptions(yearSelect, false);
			});
			
			let nextBtn = CF.eleCreateWithAttr("button", "type", "button", "class", "dg-date-widget-year-btn dg-date-widget-next-year-btn");
			CF.eleHtml(nextBtn, "&#8595;");
			CF.eleAppend(btns, nextBtn);
			CF.eleOn(nextBtn, "click", () =>
			{
				CST.eleYearSelectRollOptions(yearSelect, true);
			});
			
			CST.eleInputActualValue(input, value);
		}
		else
		{
			input = CF.eleCreate("input", "dg-dspform-input");
			CF.eleAttr(input, "dg-date-src-format", "y-m-d");
			CF.eleAttr(input, "dg-date-dest-format", options.format);
			CF.eleAttr(input, "type", "date");
			CF.eleAttr(input, "id", CF.uid());
			CF.eleAttr(input, "name", dataSetParam.name);
			
			if(CF.isLiteralTrue(dataSetParam.required))
				CF.eleAttr(input, "dg-validation-check-required", "true");
			
			CF.eleAppend(parent, input);
			CST.eleInputActualValue(input, value);
		}
		
		return input;
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
	CST.renderDspFormInputTime = function(form, formOptions, parent, dataSetParam, value)
	{
		var options = CST.evalDataSetParamInputPayload(dataSetParam, {});
		options = CF.extend({ format: "h:i:s" }, options);
		
		var input = CF.eleCreate("input", "dg-dspform-input");
		CF.eleAttr(input, "dg-date-src-format", "h:i:s");
		CF.eleAttr(input, "dg-date-dest-format", options.format);
		CF.eleAttr(input, "type", "time");
		CF.eleAttr(input, "id", CF.uid());
		CF.eleAttr(input, "name", dataSetParam.name);
		
		if(CST.dateFormatter.hasSecond(options.format))
			CF.eleAttr(input, "step", "1");
		
		if(CF.isLiteralTrue(dataSetParam.required))
			CF.eleAttr(input, "dg-validation-check-required", "true");
		
		CF.eleAppend(parent, input);
		CST.eleInputActualValue(input, value);
		
		return input;
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
	 * @param formOptions
	 * @param parent 渲染输入项的父容器元素
	 * @param dataSetParam
	 * @param value 可选
	 */
	CST.renderDspFormInputDateTime = function(form, formOptions, parent, dataSetParam, value)
	{
		var options = CST.evalDataSetParamInputPayload(dataSetParam, {});
		options = CF.extend({ format: "y-m-d h:i:s" }, options);
		
		var input = CF.eleCreate("input", "dg-dspform-input");
		CF.eleAttr(input, "dg-date-src-format", "y-m-dTh:i:s");
		CF.eleAttr(input, "dg-date-dest-format", options.format);
		CF.eleAttr(input, "type", "datetime-local");
		CF.eleAttr(input, "id", CF.uid());
		CF.eleAttr(input, "name", dataSetParam.name);
		
		if(CST.dateFormatter.hasSecond(options.format))
			CF.eleAttr(input, "step", "1");
		
		if(CF.isLiteralTrue(dataSetParam.required))
			CF.eleAttr(input, "dg-validation-check-required", "true");
		
		CF.eleAppend(parent, input);
		CST.eleInputActualValue(input, value);
		
		return input;
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
	 * @param formOptions
	 * @param parent 渲染输入项的父容器元素
	 * @param dataSetParam
	 * @param value 可选
	 * @param defaultSelOpts 可选，默认单选框选项集
	 */
	CST.renderDspFormInputRadio = function(form, formOptions, parent, dataSetParam, value, defaultSelOpts)
	{
		var opts = CST.evalDataSetParamInputPayload(dataSetParam, []);
		
		if(defaultSelOpts && CF.isEmpty(opts))
			opts = defaultSelOpts;
		
		if(!CF.isArray(opts))
			opts = [ opts ];
		
		var inputsWrapper = CF.eleCreate("div", "dg-dspform-inputs-wrapper");
		CF.eleAttr(inputsWrapper, "id", CF.uid());
		CF.eleAppend(parent, inputsWrapper);
		
		for(var i=0; i<opts.length; i++)
		{
			var opt = opts[i];
			
			var optName = (opt.name != null ? opt.name : opt.value);
			var optVal = (opt.value != null ? opt.value : opt.name);
			optName = (optName == null ? opt : optName);
			optVal = (optVal == null ? opt : optVal);
			
			var eleId = CF.uid();
			
			var wrapper = CF.eleCreate("div", "dg-dspform-radio-wrapper");
			CF.eleAppend(inputsWrapper, wrapper);
			
			var input = CF.eleCreateWithAttr("input", "type", "radio", "class", "dg-dspform-input",
							"id", eleId, "name", dataSetParam.name, "value", optVal);
			CF.eleAppend(wrapper, input);
			
			var label = CF.eleCreateWithAttr("label", "for", eleId);
			CF.eleHtml(label, optName);
			CF.eleAppend(wrapper, label);
			
			if(CF.isLiteralTrue(dataSetParam.required))
				CF.eleAttr(input, "dg-validation-check-required", "true");
			
			if(CF.DataSetParamType.NUMBER == dataSetParam.type)
				CF.eleAttr(input, "dg-validation-check-number", "true");
			
			CST.eleInputActualValue(input, value);
		}
		
		return inputsWrapper;
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
	 * @param formOptions
	 * @param parent 渲染输入项的父容器元素
	 * @param dataSetParam
	 * @param value 可选，值、值数组
	 * @param defaultSelOpts 可选，默认复选框选项集
	 */
	CST.renderDspFormInputCheckbox = function(form, formOptions, parent, dataSetParam, value, defaultSelOpts)
	{
		var opts = CST.evalDataSetParamInputPayload(dataSetParam, []);
		
		if(defaultSelOpts && CF.isEmpty(opts))
			opts = defaultSelOpts;
		
		if(!CF.isArray(opts))
			opts = [ opts ];
		
		value = (value == null ? [] : (CF.isArray(value) ? value : [ value ]));
		
		var inputsWrapper = CF.eleCreate("div", "dg-dspform-inputs-wrapper");
		CF.eleAttr(inputsWrapper, "id", CF.uid());
		CF.eleAppend(parent, inputsWrapper);
		
		for(var i=0; i<opts.length; i++)
		{
			var opt = opts[i];
			
			var optName = (opt.name != null ? opt.name : opt.value);
			var optVal = (opt.value != null ? opt.value : opt.name);
			optName = (optName == null ? opt : optName);
			optVal = (optVal == null ? opt : optVal);
			
			var eleId = CF.uid();
			
			var wrapper = CF.eleCreate("div", "dg-dspform-radio-wrapper");
			CF.eleAppend(inputsWrapper, wrapper);
			
			var input = CF.eleCreateWithAttr("input", "type", "checkbox", "class", "dg-dspform-input",
							"id", eleId, "name", dataSetParam.name, "value", optVal);
			CF.eleAppend(wrapper, input);
			
			var label = CF.eleCreateWithAttr("label", "for", eleId);
			CF.eleHtml(label, optName);
			CF.eleAppend(wrapper, label);
			
			if(CF.isLiteralTrue(dataSetParam.required))
				CF.eleAttr(input, "dg-validation-check-required", "true");
			
			if(CF.DataSetParamType.NUMBER == dataSetParam.type)
				CF.eleAttr(input, "dg-validation-check-number", "true");
			
			CST.eleInputActualValue(input, value);
		}
		
		return inputsWrapper;
	};
	
	/**
	 * 渲染输入项：文本域
	 * 
	 * @param form
	 * @param formOptions
	 * @param parent 渲染输入项的父容器元素
	 * @param dataSetParam
	 * @param value 可选
	 */
	CST.renderDspFormInputTextarea = function(form, formOptions, parent, dataSetParam, value)
	{
		var input = CF.eleCreate("textarea", "dg-dspform-input");
		CF.eleAttr(input, "type", "text");
		CF.eleAttr(input, "id", CF.uid());
		CF.eleAttr(input, "name", dataSetParam.name);
		CF.eleAttr(input, "value", (value == null ? "" : value));
		
		if(CF.isLiteralTrue(dataSetParam.required))
			CF.eleAttr(input, "dg-validation-check-required", "true");
		
		if(CF.DataSetParamType.NUMBER == dataSetParam.type)
			CF.eleAttr(input, "dg-validation-check-number", "true");
		
		CF.eleAppend(parent, input);
		CST.eleInputActualValue(input, value);
		
		return input;
	};
	
	CST.MONTH_OPTIONS = ["", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"];
	
	//日期格式解析支持类，支持"...y...m...d...h...i...s..."格式日期解析
	//注意：这里保留了大写'Y'标识符，以兼容旧数据集预览时的格式
	CST.dateFormatter =
	{
		hasYear: function(format)
		{
			return (format != null && (format.indexOf("y") >= 0 || format.indexOf("Y") >= 0));
		},
		hasMonth: function(format)
		{
			return (format != null && format.indexOf("m") >= 0);
		},
		hasDay: function(format)
		{
			return (format != null && format.indexOf("d") >= 0);
		},
		hasSecond: function(format)
		{
			return (format != null && format.indexOf("s") >= 0);
		},
		convertFormat: function(src, srcFormat, destFormat)
		{
			var date = this.parseDate(src, srcFormat);
			return this.formatDate(date, destFormat);
		},
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
					re += this.formatYear(y);
				else if(fmt == 'm')
					re += this.paddingLeftZero(m, 2);
				else if(fmt == 'd')
					re += this.paddingLeftZero(d, 2);
				else if(fmt == 'H' || fmt == 'h')
					re += this.paddingLeftZero(h, 2);
				else if(fmt == 'i')
					re += this.paddingLeftZero(i, 2);
				else if(fmt == 's')
					re += this.paddingLeftZero(s, 2);
				else
					re += fmt;
			}
			
			return re;
		},
		formatYear: function(yearNumber)
		{
			return this.paddingLeftZero(yearNumber, 4);
		},
		paddingLeftZero: function(number, length)
		{
			var re = number + "";
			
			while(re.length < length)
				re = "0" + re;
			
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
		_formatArrayCache:{}
	};
	
	CST.evalDataSetParamInputPayload = function(dataSetParam, defaultValue)
	{
		var inputPayload = dataSetParam.inputPayload;
		
		if(CF.isEmpty(inputPayload))
			return defaultValue;
		else if(CF.isString(inputPayload))
			return CF.evalSilently(inputPayload, defaultValue);
		else
			return inputPayload;
	};
	
	CST.NUMBER_REGEX = /^-?\d+\.?\d*$/;
	
	/**
	 * 校验数据集参数值表单的必填项、数值项。
	 * 
	 * @param form
	 * @return true 验证通过；false 验证不通过
	 */
	CST.validateDspForm = function(form)
	{
		var validationOk = true;
		
		var valueWrappers = CF.elesOfSelector(".dg-dspform-item-value", form);
		
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
						checkedValues.push(val);
				}
				else
				{
					let indicator = (type == "hidden" ? CF.eleAncestorOfSelector(input, ".dg-dspform-inputs-wrapper") : input);
					
					if(CF.isEmpty(val))
					{
						CF.eleAddClass(indicator, "dg-validation-required");
						validationOk = false;
					}
					else
						CF.eleRemoveClass(indicator, "dg-validation-required");
				}
			});
			
			if(isCheckboxRadio)
			{
				let inputsWrapper = CF.eleOfSelector(".dg-dspform-inputs-wrapper", valueWrapper);
				
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
						checkedValues.push(val);
				}
				else
				{
					val = (CF.isEmpty(val) ? [] : (CF.isArray(val) ? val : [ val ]));
					let indicator = (type == "hidden" ? CF.eleAncestorOfSelector(input, ".dg-dspform-inputs-wrapper") : input);
					
					if(!CST.isNonEmptyAllNumberic(val))
					{
						CF.eleAddClass(indicator, "dg-validation-number");
						validationOk = false;
					}
					else
						CF.eleRemoveClass(indicator, "dg-validation-number");
				}
			});
			
			if(isCheckboxRadio)
			{
				let inputsWrapper = CF.eleOfSelector(valueWrapper, ".dg-dspform-inputs-wrapper");
				
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
	CST.getDataSetParamFormData = function(form)
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
			let value = CST.eleInputActualValue(input);
			let prevValue = re[name];
			
			if(arrayValue)
			{
				if(prevValue == null)
				{
					prevValue = [];
					re[name] = prevValue;
				}
				
				if(value != null)
				{
					if(CF.isArray(value))
					{
						prevValue = prevValue.concat(value);
						re[name] = prevValue;
					}
					else
					{
						prevValue.push(value);
					}
				}
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
	
	CST.setDataSetParamFormData = function(form, data)
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
		
		if(arguments.length < 2)
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
					re = Array.from(input.selectedOptions).map(option => option.value);
				else
					re = input.value;
			}
			else
			{
				if(CF.isEleMatches(input, ".dg-date-widget-hidden"))
					re = CST.eleDateWidgetValue(input);
				else
					re = input.value;
				
				re = CST.eleInputConvertDateFormat(input, re);
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
				CST.eleSelectSetValue(input, value);
			}
			else
			{
				if(CF.isEleMatches(input, ".dg-date-widget-hidden"))
				{
					CST.eleDateWidgetValue(input, value);
				}
				else
				{
					value = CST.eleInputConvertDateFormat(input, value, true);
					input.value = (value == null ? "" : value);
				}
			}
		}
	};
	
	CST.eleDateWidgetValue = function(inputHidden, value)
	{
		let dateWidget = CF.eleAncestorOfSelector(inputHidden, ".dg-date-widget");
		let yearSelect = (dateWidget == null ? null : CF.eleOfSelector(".dg-date-widget-year", dateWidget));
		let monthSelect = (dateWidget == null ? null : CF.eleOfSelector(".dg-date-widget-month", dateWidget));
		
		if(arguments.length < 2)
		{
			let re = (yearSelect == null ? undefined : yearSelect.value);
			
			if(!CF.isEmpty(re) && monthSelect != null)
			{
				let month = monthSelect.value;
				re = (CF.isEmpty(month) ? undefined : (re + "-" + month));
			}
			
			return re;
		}
		else
		{
			let date = null;
			
			if(CF.isDate(value))
				date = value;
			else
				date = (CF.isEmpty(value) ? null : CST.dateFormatter.parseDate(value, CF.eleAttr(inputHidden, "dg-date-dest-format")));
			
			let yearValue = (date == null ? "" : CST.dateFormatter.formatYear(date.getFullYear()));
			let monthValue = (date == null ? "" : CST.dateFormatter.formatDate(date, "m"));
			
			CST.eleSetYearSelectOptions(yearSelect, (date == null ? null : date.getFullYear()));
			CST.eleSelectSetValue(yearSelect, yearValue);
			CST.eleSelectSetValue(monthSelect, monthValue);
		}
	};
	
	CST.eleSetYearSelectOptions = function(yearSelect, yearValue)
	{
		if(yearSelect == null)
			return;
		
		yearValue = (yearValue == null ? new Date().getFullYear() : yearValue);
		yearValue = yearValue - 5;
		yearValue = (yearValue < 0 ? 0 : yearValue);
		yearValue = (yearValue > 9999 ? 9999 : yearValue);
		
		CF.eleEmpty(yearSelect);
		
		let emptyOpt = CF.eleCreateWithAttr("option", "value", "");
		CF.eleHtml(emptyOpt, "");
		CF.eleAppend(yearSelect, emptyOpt);
		
		//前五年后四年
		for(let i=0; i<10; i++)
		{
			let value = yearValue+i;
			
			if(value > 9999)
				break;
			
			value = CST.dateFormatter.formatYear(value);
			let opt = CF.eleCreateWithAttr("option", "value", value);
			CF.eleHtml(opt, value);
			CF.eleAppend(yearSelect, opt);
		}
	};
	
	CST.eleYearSelectRollOptions = function(yearSelect, down)
	{
		if(yearSelect == null)
			return;
		
		down = (down == null ? true : down);
		
		let selectedValue = yearSelect.value;
		if(CF.isEmpty(selectedValue))
		{
			for (let i=0; i<yearSelect.options.length; i++)
			{
				let option = yearSelect.options[i];
				if(!CF.isEmpty(option.value))
				{
					selectedValue = option.value;
					break;
				}
			}
		}
		
		if(!CF.isEmpty(selectedValue))
		{
			selectedValue = parseInt(selectedValue);
			selectedValue = (down ? (selectedValue + 10) : (selectedValue - 10));
			selectedValue = CST.dateFormatter.formatYear(selectedValue);
		}
		
		let optIdx = (down ? (yearSelect.options.length - 1) : 1);
		let option = yearSelect.options[optIdx];
		let newYear = (option == null ? null : option.value);
		newYear = (CF.isEmpty(newYear) ? new Date().getFullYear() : parseInt(newYear));
		newYear = (down ? (newYear + 6) : (newYear - 5));
		
		CST.eleSetYearSelectOptions(yearSelect, newYear);
		CST.eleSelectSetValue(yearSelect, selectedValue);
	};
	
	CST.eleInputConvertDateFormat = function(input, value, toSrc)
	{
		toSrc = (toSrc == null ? false : toSrc);
		
		if(input == null || CF.isEmpty(value))
			return value;
		
		let dateSrcFormat = CF.eleAttr(input, "dg-date-src-format");
		let dateDestFormat = (dateSrcFormat == null ? null : CF.eleAttr(input, "dg-date-dest-format"));
		
		if(!CF.isEmpty(dateSrcFormat) && !CF.isEmpty(dateDestFormat))
		{
			if(toSrc)
				value = CST.dateFormatter.convertFormat(value, dateDestFormat, dateSrcFormat);
			else
				value = CST.dateFormatter.convertFormat(value, dateSrcFormat, dateDestFormat);
		}
		
		return value;
	};
	
	CST.eleSelectSetValue = function(select, value)
	{
		if(!select)
			return;
		
		value = (value == null ? null : (CF.isArray(value) ? value : [ value ]));
		
		for (let i = 0; i < select.options.length; i++)
		{
			let option = select.options[i];
			option.selected = (value == null ? false : CST.containsValueAsString(value, option.value));
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
	
	CST.getDataSetParamForm = function(parent)
	{
		return CF.eleOfSelector(".dg-dspform", parent);
	};
	
	CST.getDataSetParamFormHead = function(form)
	{
		return CF.eleOfSelector(".dg-dspform-head", form);
	};
	
	CST.getDataSetParamFormContent = function(form)
	{
		return CF.eleOfSelector(".dg-dspform-content", form);
	};
	
	CST.getDataSetParamFormFoot = function(form)
	{
		return CF.eleOfSelector(".dg-dspform-foot", form);
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
		
		var chartEle = chart.element();
		
		if(!CF.eleAttr(chartEle, "dg-chartsetting-bind-event"))
		{
			CF.eleAttr(chartEle, "dg-chartsetting-bind-event", "true");
			
			if(displayMode == "display")
			{
				CST.showChartSettingBox(chart);
			}
			else if(displayMode == "hover")
			{
				var mouseenterHandler = function()
				{
					if(chart.isActive())
						CST.showChartSettingBox(chart);
				};
				
				var mouseleaveHandler = function()
				{
					if(CST.isChartSettingParamPanelClosed(chart)
						&& CST.isChartSettingDataPanelClosed(chart))
					{
						CST.hideChartSettingBox(chart);
					}
				};
				
				CF.eleOn(chartEle, "mouseenter", mouseenterHandler);
				CF.eleOn(chartEle, "mouseleave", mouseleaveHandler);
				
				CF.eleData(chartEle, "dgChartSettingMouseEnterHandler", mouseenterHandler);
				CF.eleData(chartEle, "dgChartSettingMouseLeaveHandler", mouseleaveHandler);
			}
		}
		
		return true;
	};
	
	CST.unbindChartSettingPanelEvent = function(chart)
	{
		var chartEle = chart.element();
		var mouseenterHandler = CF.eleRemoveData(chartEle, "dgChartSettingMouseEnterHandler");
		var mouseleaveHandler = CF.eleRemoveData(chartEle, "dgChartSettingMouseLeaveHandler");
		
		CF.eleAttr(chartEle, "dg-chartsetting-bind-event", null);
		
		if(mouseenterHandler)
			CF.eleOff(chartEle, "mouseenter", mouseenterHandler);
		if(mouseleaveHandler)
			CF.eleOff(chartEle, "mouseleave", mouseleaveHandler);
		
		var box = CF.eleOfSelector(".dg-chart-setting-box", chartEle);
		CST.destroyDataSetParamForm(box);
		CF.eleRemove(box);
	};
	
	CST.showChartSettingBox = function(chart)
	{
		var disableSetting = chart.disableSetting();
		
		var chartEle = chart.element();
		var boxEle = CF.eleOfSelector(".dg-chart-setting-box", chartEle);
		
		if(boxEle == null)
		{
			let chartOptions = chart.options();
			let builtinSetting = CF.builtinOptionValue(chartOptions, builtinOptionNames.builtinSetting);
			
			//显示位置："rightTop" 右上（默认）；"leftTop" 左上；"leftBottom" 左下；"rightBottom" 右下
			let boxPosition = (builtinSetting ? builtinSetting.position : null);
			boxPosition = (CF.isEmpty(boxPosition) ? "rightTop" : boxPosition);
			
			//显示方向："row" 横向（默认）；"column" 竖向
			let boxDirection = (builtinSetting ? builtinSetting.direction : null);
			boxDirection = (CF.isEmpty(boxDirection) ? "row" : boxDirection);
			
			let boxPositionCssName = "dg-position-" + boxPosition;
			let boxDirectionCssName = "dg-flex-dir-" + boxDirection;
			
			boxEle = CF.eleCreate("div", "dg-chart-setting-box "+boxPositionCssName+" "+boxDirectionCssName);
			CF.eleAppend(chartEle, boxEle);
			
			CST.setChartSettingBoxThemeStyle(chart, boxEle);
			
			//参数
			if(!disableSetting.param && chart.hasDataSetParam())
			{
				let button = CF.eleCreateWithAttr("button", "type", "button", "class", "dg-chart-setting-button dg-chart-setting-param-button");
				CF.eleHtml(button, CST.labels.param);
				CF.eleAppend(boxEle, button);
				
				CST.setChartSettingButtonOptions(button, (builtinSetting ? builtinSetting.paramButton : null));
				
				CF.eleOn(button, "click", () =>
				{
					CST.closeChartSettingDataPanel(chart);
					
					if(CST.isChartSettingParamPanelClosed(chart))
						CST.openChartSettingParamPanel(boxEle, chart);
					else
						CST.closeChartSettingParamPanel(chart);
				});
				
				CF.eleOn(chartEle, "click", (event) =>
				{
					if(!CST.isChartSettingParamPanelClosed(chart))
					{
						if(CF.eleAncestorOfSelector(event.target, ".dg-chart-setting-box") == null)
							CST.closeChartSettingParamPanel(chart);
					}
				});
			}
			
			//数据
			if(!disableSetting.data)
			{
				let button = CF.eleCreateWithAttr("button", "type", "button", "class", "dg-chart-setting-button dg-chart-setting-data-button");
				CF.eleHtml(button, CST.labels.data);
				CF.eleAppend(boxEle, button);
				
				CST.setChartSettingButtonOptions(button, (builtinSetting ? builtinSetting.dataButton : null));
				
				CF.eleOn(button, "click", () =>
				{
					CST.closeChartSettingParamPanel(chart);
					
					if(CST.isChartSettingDataPanelClosed(chart))
						CST.openChartSettingDataPanel(boxEle, chart);
					else
						CST.closeChartSettingDataPanel(chart);
				});
				
				CF.eleOn(chartEle, "click", (event) =>
				{
					if(!CST.isChartSettingDataPanelClosed(chart))
					{
						if(CF.eleAncestorOfSelector(event.target, ".dg-chart-setting-box") == null)
							CST.closeChartSettingDataPanel(chart);
					}
				});
			}
		}
		
		CST.eleShow(boxEle);
	};
	
	CST.hideChartSettingBox = function(chart)
	{
		var chartEle = chart.element();
		var boxEle = CF.eleOfSelector(".dg-chart-setting-box", chartEle);
		CST.eleHide(boxEle);
	};
	
	//设置按钮选项，格式为：{ text: "", style: "...", styleClass: "..." }
	CST.setChartSettingButtonOptions = function(button, buttonOptions)
	{
		if(!buttonOptions)
			return;
		
		if(buttonOptions.text)
			CF.eleHtml(button, buttonOptions.text);
		
		if(buttonOptions.style)
			CF.eleStyle(button, buttonOptions.style);
		
		if(buttonOptions.styleClass)
			button.addClass(buttonOptions.styleClass);
	};
	
	CST.setChartSettingBoxThemeStyle = function(chart, boxEle)
	{
		chart.themeStyleSheet(CF.builtinPropName("chartSettingBox"), function()
		{
			var color = chart.themeGradualColor(1);
			var bgColor = chart.themeGradualColor(-1);
			var titleBg = chart.themeGradualColor(0);
			var btnBg = chart.themeGradualColor(0.1);
			var btnBorderColor = chart.themeGradualColor(0.5);
			var btnHoverBg = chart.themeGradualColor(0.2);
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
						"background-color": btnHoverBg
					}
				},
				{
					name: " .dg-chart-setting-panel",
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
					name:
					[
						" .dg-chart-setting-panel-head button",
						" .dg-chart-setting-panel-foot button"
					],
					value:
					{
						"color": color,
						"background-color": btnBg,
						"border-color": btnBorderColor
					}
				},
				{
					name:
					[
						" .dg-chart-setting-panel-head button:hover",
						" .dg-chart-setting-panel-foot button:hover"
					],
					value:
					{
						"background-color": btnHoverBg
					}
				},
				{
					name: " .dg-chart-setting-panel .dg-datasetbind-section",
					value:
					{
						"color": color,
						"background-color": bgColor,
						"border-color": panelBorderColor
					}
				},
				{
					name: " .dg-chart-setting-panel .dg-datasetbind-section-head",
					value:
					{
						"background-color": titleBg
					}
				},
				{
					name:
					[
						" .dg-chart-setting-data-panel table.dg-chart-data-table tbody tr:hover",
						" .dg-chart-setting-data-panel table.dg-chart-data-table tbody tr:hover td"
					],
					value:
					{
						"background-color": btnBg
					}
				}
			];
			
			return css;
		});
	};
	
	/**
	 * 打开图表参数面板。
	 */
	CST.openChartSettingParamPanel = function(boxEle, chart)
	{
		var dataSetBinds = chart.dataSetBinds();
		var panel = CF.eleOfSelector(".dg-chart-setting-param-panel", boxEle);
		
		if(panel == null)
		{
			panel = CF.eleCreate("div", "dg-chart-setting-panel dg-chart-setting-param-panel");
			CF.eleAppend(boxEle, panel);
			
			CST.showChartSetingPanelOpacityOut(boxEle, panel, chart);
			
			let panelHead = CF.eleCreate("div", "dg-chart-setting-panel-head");
			CF.eleAppend(panel, panelHead);
			
			let panelContent = CF.eleCreate("div", "dg-chart-setting-panel-content");
			CF.eleAppend(panel, panelContent);
			
			let panelFoot = CF.eleCreate("div", "dg-chart-setting-panel-foot");
			CF.eleAppend(panel, panelFoot);
			
			let headTitle = CF.eleCreate("div", "dg-chart-setting-panel-head-title");
			CF.eleHtml(headTitle, CST.labels.chartParam);
			CF.eleAppend(panelHead, headTitle);
			
			let headBtns = CF.eleCreate("div", "dg-chart-setting-panel-head-btns");
			CF.eleAppend(panelHead, headBtns);
			
			let closeBtn = CF.eleCreateWithAttr("button", "type", "button", "class", "dg-chart-setting-panel-closebtn");
			CF.eleHtml(closeBtn, CST.labels.close);
			CF.eleAppend(headBtns, closeBtn);
			CF.eleOn(closeBtn, "click", () => { CST.closeChartSettingParamPanel(chart); });
			
			let confirmBtn = CF.eleCreateWithAttr("button", "type", "button");
			CF.eleHtml(confirmBtn, CST.labels.confirm);
			CF.eleAppend(panelFoot, confirmBtn);
			
			CST.setChartSetingPanelContentSizeRange(chart, panel, panelContent, panelFoot);
			
			for(let i=0; i<dataSetBinds.length; i++)
			{
				let params = chart.dataSetParams(dataSetBinds[i]);
				
				if(CF.isEmpty(params))
					continue;
				
				let myTitle = CST.evalDataSetBindPanelTitle(chart, dataSetBinds, i);
				
				let dsbSection = CF.eleCreate("div", "dg-datasetbind-section");
				CF.eleAttr(dsbSection, "dg-datasetbind-index", i);
				CF.eleAppend(panelContent, dsbSection);
				
				let dsbHead = CF.eleCreate("div", "dg-datasetbind-section-head");
				CF.eleAppend(dsbSection, dsbHead);
				
				let dsbTitle = CF.eleCreate("span", "dg-datasetbind-section-title");
				CF.eleHtml(dsbTitle, myTitle);
				CF.eleAppend(dsbHead, dsbTitle);
				
				let dsbContent = CF.eleCreate("div", "dg-datasetbind-section-content");
				CF.eleAppend(dsbSection, dsbContent);
				
				CST.renderDataSetParamForm(dsbContent, params,
				{
					chartTheme: chart.theme(),
					inChartElement: true,
					submit: function()
					{
						confirmBtn.click();
					},
					paramValues: chart.dataSetParamValues(i),
					render: function(form)
					{
						CST.eleHide(CST.getDataSetParamFormFoot(form));
					}
				});
				
				CF.eleOn(dsbTitle, "click", () =>
				{
					CST.eleToggle(dsbContent);
				});
				
				CST.toggleParamFormContentByIgnoreFetch(panel, dsbSection, chart, i);
			}
			
			CF.eleOn(confirmBtn, "click", function()
			{
				let thisButton = this;
				let validateOk = true;
				let paramValuess = [];
				
				let dsbSections = CF.elesOfSelector(".dg-datasetbind-section", panelContent);
				dsbSections.forEach((dsbSection) =>
				{
					let dspForm = CST.getDataSetParamForm(dsbSection);
					let dataSetBindIndex = parseInt(CF.eleAttr(dsbSection, "dg-datasetbind-index"));
					let ignoreFetch = chart.dataSetIgnoreFetch(dataSetBindIndex);
					
					if(!ignoreFetch && !CST.validateDspForm(dspForm))
						validateOk = false;
					else
					{
						let myParamValues = CST.getDataSetParamFormData(dspForm);
						paramValuess.push({ index : dataSetBindIndex, paramValues: myParamValues });
					}
				});
				
				if(validateOk)
				{
					CF.eleRemoveClass(thisButton, "dg-param-value-form-invalid");
					
					let chartOptions = chart.options();
					let builtinSetting = CF.builtinOptionValue(chartOptions, builtinOptionNames.builtinSetting);
					let convertParamFormValue = (builtinSetting ? builtinSetting.convertParamFormValue : null);
					convertParamFormValue = (convertParamFormValue == null ? true : convertParamFormValue);
					
					for(let i=0; i<paramValuess.length; i++)
					{
						CST.dataSetBindParamValues(chart, paramValuess[i].index, paramValuess[i].paramValues, convertParamFormValue);
					}
					
					let doRefresh = true;
					let onParamFormSubmit = (builtinSetting ? builtinSetting.onParamFormSubmit : null);
					
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
					CF.eleAddClass(thisButton, "dg-param-value-form-invalid");
			});
		}
		else
		{
			CST.showChartSetingPanelOpacityOut(boxEle, panel, chart);
			
			let dsbSections = CF.elesOfSelector(".dg-datasetbind-section", panel);
			
			dsbSections.forEach(function(dsbSection)
			{
				let dspForm = CST.getDataSetParamForm(dsbSection);
				let dataSetBindIndex = parseInt(CF.eleAttr(dsbSection, "dg-datasetbind-index"));
				
				CST.setDataSetParamFormData(dspForm, chart.dataSetParamValues(dataSetBindIndex));
				CST.toggleParamFormContentByIgnoreFetch(panel, dsbSection, chart, dataSetBindIndex);
			});
		}
		
		CST.adjustChartSetingPanelPosition(boxEle, panel, CF.eleOfSelector(".dg-chart-setting-param-button", boxEle), chart);
		
		//聚焦至第一个可操作输入框
		CST.focusOnFirstInput(CF.eleOfSelector("form:first-child", panel));
	};
	
	CST.dataSetBindParamValues = function(chart, dataSetBindIndex, paramValues, convert)
	{
		//这里设置参数应采用inflate模式，因为数据集允许隐式参数（未明确定义数据集参数的参数化语法），这里不应清除它们
		chart.dataSetParamValues(dataSetBindIndex, paramValues, true, convert);
	};
	
	CST.toggleParamFormContentByIgnoreFetch = function(panel, section, chart, dataSetBindIndex)
	{
		var ignoreFetch = chart.dataSetIgnoreFetch(dataSetBindIndex);
		var content = CF.eleOfSelector(".dg-datasetbind-section-content", section);
		
		if(ignoreFetch)
			CST.eleHide(content);
		else
			CST.eleShow(content);
	};
	
	/**
	 * 关闭图表参数面板。
	 */
	CST.closeChartSettingParamPanel = function(chart)
	{
		var panel = CST.getChartSettingParamPanel(chart);
		CST.eleHide(panel);
	};
	
	/**
	 * 获取图表参数面板。
	 */
	CST.getChartSettingParamPanel = function(chart)
	{
		return CF.eleOfSelector(".dg-chart-setting-param-panel", chart.element());
	};
	
	CST.isChartSettingParamPanelClosed = function(chart)
	{
		var panel = CST.getChartSettingParamPanel(chart);
		return (panel == null || CF.eleAncestorOfSelector(panel, ".dg-display-none") != null);
	};
	
	/**
	 * 打开图表数据面板。
	 */
	CST.openChartSettingDataPanel = function(boxEle, chart)
	{
		var dataSetBinds = chart.dataSetBinds();
		var panel = CF.eleOfSelector(".dg-chart-setting-data-panel", boxEle);
		
		if(panel == null)
		{
			panel = CF.eleCreate("div", "dg-chart-setting-panel dg-chart-setting-data-panel")
			CF.eleAppend(boxEle, panel);
			
			CST.showChartSetingPanelOpacityOut(boxEle, panel, chart);
			
			let panelHead = CF.eleCreate("div", "dg-chart-setting-panel-head");
			CF.eleAppend(panel, panelHead);
			
			let panelContent = CF.eleCreate("div", "dg-chart-setting-panel-content");
			CF.eleAppend(panel, panelContent);
			
			let panelFoot = CF.eleCreate("div", "dg-chart-setting-panel-foot");
			CF.eleAppend(panel, panelFoot);
			
			let headTitle = CF.eleCreate("div", "dg-chart-setting-panel-head-title");
			CF.eleHtml(headTitle, CST.labels.chartData);
			CF.eleAppend(panelHead, headTitle);
			
			let headBtns = CF.eleCreate("div", "dg-chart-setting-panel-head-btns");
			CF.eleAppend(panelHead, headBtns);
			
			let closeBtn = CF.eleCreateWithAttr("button", "type", "button", "class", "dg-chart-setting-panel-closebtn");
			CF.eleHtml(closeBtn, CST.labels.close);
			CF.eleAppend(headBtns, closeBtn);
			CF.eleOn(closeBtn, "click", () => { CST.closeChartSettingDataPanel(chart); });
			
			CST.setChartSetingPanelContentSizeRange(chart, panel, panelContent,panelFoot);
			
			for(let i=0; i<dataSetBinds.length; i++)
			{
				let myTitle = CST.evalDataSetBindPanelTitle(chart, dataSetBinds, i);
				
				let dsbSection = CF.eleCreate("div", "dg-datasetbind-section");
				CF.eleAttr(dsbSection, "dg-datasetbind-index", i);
				CF.eleAppend(panelContent, dsbSection);
				
				let dsbHead = CF.eleCreate("div", "dg-datasetbind-section-head");
				CF.eleAppend(dsbSection, dsbHead);
				
				let dsbTitle = CF.eleCreate("span", "dg-datasetbind-section-title");
				CF.eleHtml(dsbTitle, myTitle);
				CF.eleAppend(dsbHead, dsbTitle);
				
				let dsbContent = CF.eleCreate("div", "dg-datasetbind-section-content");
				CF.eleAppend(dsbSection, dsbContent);
				
				let tableId = CST.initDataSetBindDataTable(chart, dataSetBinds, i, dsbContent);
				CF.eleAttr(dsbSection, "dg-datasetbind-table-id", tableId);
				
				CF.eleOn(dsbTitle, "click", () =>
				{
					CST.eleToggle(dsbContent);
				});
			}
		}
		else
		{
			CST.showChartSetingPanelOpacityOut(boxEle, panel, chart);
			
			CF.elesOfSelector(".dg-datasetbind-section", panel).forEach((dsbSection) =>
			{
				let dsbContent = CF.eleOfSelector(".dg-datasetbind-section-content", dsbSection);
				CST.eleShow(dsbContent);
				
				let dataSetBindIndex = parseInt(CF.eleAttr(dsbSection, "dg-datasetbind-index"));
				let tableId = CF.eleAttr(dsbSection, "dg-datasetbind-table-id");
				let table = CF.eleOfSelector("#"+tableId, dsbSection);
				
				CST.updateChartSettingDataTableData(chart, dataSetBinds, dataSetBindIndex, table);
			});
		}
		
		CST.adjustChartSetingPanelPosition(boxEle, panel, CF.eleOfSelector(".dg-chart-setting-data-button", boxEle), chart);
	};
	
	/**
	 * 关闭图表数据面板。
	 */
	CST.closeChartSettingDataPanel = function(chart)
	{
		var panel = CST.getChartSettingDataPanel(chart);
		CST.eleHide(panel);
	};
	
	/**
	 * 获取图表数据面板。
	 */
	CST.getChartSettingDataPanel = function(chart)
	{
		return CF.eleOfSelector(".dg-chart-setting-data-panel", chart.element());
	};
	
	CST.isChartSettingDataPanelClosed = function(chart)
	{
		var panel = CST.getChartSettingDataPanel(chart);
		return (panel == null || CF.eleAncestorOfSelector(panel, ".dg-display-none") != null);
	};
	
	CST.initDataSetBindDataTable = function(chart, dataSetBinds, index, parent)
	{
		var dataSetBind = dataSetBinds[index];
		var dataSetFields = chart.dataSetFields(dataSetBind);
		var signFields = [];
		
		for(let i=0; i<dataSetFields.length; i++)
		{
			let signs = (chart.dataSetFieldSigns(dataSetBind, dataSetFields[i]) || []);
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
		var table = CF.eleCreateWithAttr("table", "id", tableId, "width", "100%", "class", "dg-chart-data-table");
		CF.eleAppend(parent, table);
		CF.eleData(table, "tableColumns", columns);
		
		var thead = CF.eleCreate("thead")
		CF.eleAppend(table, thead);
		
		var tr = CF.eleCreate("tr");
		CF.eleAppend(thead, tr);
		
		for(let i=0; i<columns.length; i++)
		{
			let th = CF.eleCreate("th");
			CF.eleHtml(th, columns[i].title);
			
			if(columns[i].style)
				CF.eleAttr(th, "style", columns[i].style);
			
			CF.eleAppend(tr, th);
		}
		
		var tbody = CF.eleCreate("tbody");
		CF.eleAppend(table, tbody);
		
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
	
	CST.updateChartSettingDataTableData = function(chart, dataSetBinds, index, table)
	{
		var chartResult = chart.updateResult();
		var result = chart.resultOf(chartResult, index);
		var datas = chart.resultDatas(result);
		var columns = (CF.eleData(table, "tableColumns") || []);
		
		var tbody = CF.eleOfSelector("tbody", table);
		CF.eleEmpty(tbody);
		
		for(let i=0; i<datas.length; i++)
		{
			let data = (datas[i] || {});
			let tr = CF.eleCreate("tr");
			CF.eleAppend(tbody, tr);
			
			for(let j=0; j<columns.length; j++)
			{
				let column = columns[j];
				let td = CF.eleCreate("td");
				CF.eleHtml(td, column.render(data, i));
				CF.eleAppend(tr, td);
			}
		}
	};
	
	CST.evalDataSetBindPanelTitle = function(chart, dataSetBinds, index)
	{
		let alias = chart.dataSetAlias(dataSetBinds[index]);
		var title = (dataSetBinds.length > 1 ? (index+1)+". " : "") + alias;
		if(alias != dataSetBinds[index].dataSet.name)
			title += " ("+dataSetBinds[index].dataSet.name+")";
		
		return title;
	};
	
	CST.setChartSetingPanelContentSizeRange = function(chart, panel, panelContent, panelFoot)
	{
		var chartEle = chart.element();
		var cw = parseInt(CF.eleCss(chartEle, "width"));
		var ww = window.innerWidth;
		
		CF.eleCss(panelContent, "min-width", Math.max(cw*2/5, ww*3/10)+"px");
		CF.eleCss(panelContent, "max-width", "60vw");
		CF.eleCss(panelContent, "max-height", "55vh");
	};
	
	CST.showChartSetingPanelOpacityOut = function(boxEle, panel, chart)
	{
		//先透明显示，避免布局计算错误，后续调整位置后再移除透明
		CF.eleAddClass(panel, "dg-opacity-hide");
		CF.eleCss(panel, "left", "-999999px");
		CF.eleCss(panel, "top", "-999999px");
		CF.eleCss(panel, "right", "unset");
		CF.eleCss(panel, "bottom", "unset");
		CST.eleShow(panel);
	};
	
	CST.adjustChartSetingPanelPosition = function(boxEle, panel, btn, chart)
	{
		var docWidth = window.innerWidth;
		var docHeight = window.innerHeight;
		
		var width = CST.eleOuterWidth(panel);
		var height = CST.eleOuterHeight(panel);
		var widthGap = width + 20;
		var heightGap = height + 20;
		
		var btnWidth = CST.eleOuterWidth(btn);
		var btnHeight = CST.eleOuterHeight(btn);
		
		var btnOffset = CST.eleOffset(btn);
		var btnPosition = CST.elePosition(btn);
		
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
		if(CF.isEleMatches(boxEle, "dg-position-leftBottom, dg-position-rightBottom, dg-position-centerBottom"))
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
		
		CF.eleCss(panel, "left", (left === "unset" ? left : (left+"px")));
		CF.eleCss(panel, "top", (top === "unset" ? top : (top+"px")));
		CF.eleCss(panel, "right", (right === "unset" ? right : (right+"px")));
		CF.eleCss(panel, "bottom", (bottom === "unset" ? bottom : (bottom+"px")));
		
		CF.eleRemoveClass(panel, "dg-opacity-hide");
	};
	
	//聚焦至指定元素内的第一个可操作（非只读、非禁用）输入框
	CST.focusOnFirstInput = function(ele)
	{
		var input = CF.eleOfSelector("input:not(:disabled,[readonly])", ele);
		
		if(input)
			input.focus();
	};
	
	CST.eleHide = function(ele)
	{
		CF.eleAddClass(ele, "dg-display-none");
	};
	
	CST.eleShow = function(ele)
	{
		CF.eleRemoveClass(ele, "dg-display-none");
	};
	
	CST.eleToggle = function(ele)
	{
		if(CF.isEleMatches(ele, ".dg-display-none"))
			CST.eleShow(ele);
		else
			CST.eleHide(ele);
	};
	
	CST.eleOuterWidth = function(ele)
	{
		let width = parseFloat(CF.eleCss(ele, "width") || 0);
		let marginLeft = parseFloat(CF.eleCss(ele, "margin-left") || 0);
		let marginRight = parseFloat(CF.eleCss(ele, "margin-right") || 0);
		
		return (width + marginLeft + marginRight);
	};
	
	CST.eleOuterHeight = function(ele)
	{
		let height = parseFloat(CF.eleCss(ele, "height") || 0);
		let marginTop = parseFloat(CF.eleCss(ele, "margin-top") || 0);
		let marginBottom = parseFloat(CF.eleCss(ele, "margin-bottom") || 0);
		
		return (height + marginTop + marginBottom);
	};
	
	CST.eleOffset = function(ele)
	{
		let re = { left: 0, top: 0 };
		
		if(ele == null)
			return re;
		
		let rect = ele.getBoundingClientRect();
		let scrollLeft = window.scrollX;
		let scrollTop = window.scrollY;
		
		re.left = rect.left + scrollLeft;
		re.top = rect.top + scrollTop;
		
		return re;
	};
	
	CST.elePosition = function(ele)
	{
		let re = { left: 0, top: 0 };
		
		if(ele == null)
			return re;
		
		let rect = ele.getBoundingClientRect();
		let offsetParent = ele.offsetParent || document.documentElement;
		
		if (offsetParent === document.body && CF.eleCss(offsetParent, "position") === "static")
        	offsetParent = document.documentElement;
        	
        let parentRect = offsetParent.getBoundingClientRect();
        let parentBorderTop = (parseInt(CF.eleCss(offsetParent, "border-top-width")) || 0);
        let parentBorderLeft = (parseInt(CF.eleCss(offsetParent, "border-left-width")) || 0);
        
		let top = rect.top - parentRect.top - parentBorderTop;
		let left = rect.left - parentRect.left - parentBorderLeft;
		
		if(offsetParent !== document.documentElement && offsetParent !== document.body)
		{
			top += offsetParent.scrollTop;
			left += offsetParent.scrollLeft;
		}
		
		re.top = top;
		re.left = left;
		
		return re;
	};
})
(this, window);

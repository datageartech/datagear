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
	var chartFactory = (global.chartFactory || (global.chartFactory = {}));
	var chartSetting = (chartFactory.chartSetting || (chartFactory.chartSetting = {}));
	
	// < @deprecated 兼容1.8.1版本的window.chartSetting变量名，未来版本会移除
	global.chartForm = chartSetting;
	// > @deprecated 兼容1.8.1版本的window.chartSetting变量名，未来版本会移除
	
	// < @deprecated 兼容2.1.1版本的window.chartFactory.chartSetting变量名，未来版本会移除
	chartFactory.chartForm = chartSetting;
	// > @deprecated 兼容2.1.1版本的window.chartFactory.chartSetting变量名，未来版本会移除
	
	//org.datagear.analysis.DataSetParam.DataType
	chartSetting.DataSetParamDataType =
	{
		STRING: "STRING",
		BOOLEAN: "BOOLEAN",
		NUMBER: "NUMBER"
	};
	
	//org.datagear.analysis.DataSetParam.InputType
	chartSetting.DataSetParamInputType =
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
	
	chartSetting.labels = (chartSetting.labels ||
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
	
	//是否在参数表单提交时关闭参数面板
	chartSetting.closeChartSettingParamPanelOnSubmit = (chartSetting.closeChartSettingParamPanelOnSubmit || true);
	
	//是否禁用日期组件输入框的浏览器自动完成功能，浏览器自动完成功能会阻挡日期选择框，默认禁用
	chartSetting.disableDateAwareInputAutocomplete = (chartSetting.disableDateAwareInputAutocomplete || true);
	
	/**
	 * 渲染数据集参数值表单。
	 * 
	 * @param $parent 用于渲染表单的父元素，如果不是<form>元素，此函数将会自动新建<form>子元素，<form>元素结构也允许预先自定义
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
	 * @return 表单DOM元素
	 */
	chartSetting.renderDataSetParamValueForm = function($parent, dataSetParams, options)
	{
		options = $.extend(
		{
			inChartElement: false,
			submitText: chartSetting.labels.confirm,
			labelColon: chartSetting.labels.colon,
			readonly: false,
			yesText: chartSetting.labels.yes,
			noText: chartSetting.labels.no
		},
		(options || {}));
		
		var paramValues = (options.paramValues || {});
		var InputType = chartSetting.DataSetParamInputType;
		
		var $form = ($parent.is("form") ? $parent : $("<form dg-generated-ele='true' />").appendTo($parent));
		
		$form.addClass("dg-dspv-form");
		
		//创建表单样式表
		if(options.chartTheme)
		{
			if(options.inChartElement)
				chartSetting.dataSetParamValueFormThemeStyle(options.chartTheme, true);
			else
			{
				var themeStyleName = chartSetting.dataSetParamValueFormThemeStyle(options.chartTheme, false);
				$form.addClass(themeStyleName);
				$form.data("dgDspvFormThemeClassName", themeStyleName);
			}
		}
		
		var $head = $(".dg-dspv-form-head", $form);
		var $content = $(".dg-dspv-form-content", $form);
		var $foot = $(".dg-dspv-form-foot", $form);
		
		//允许预先自定义表单结构
		if($head.length == 0)
			$head = $("<div class='dg-dspv-form-head' />").prependTo($form);
		if($content.length == 0)
			$content = $("<div class='dg-dspv-form-content' />").appendTo($form);
		if($foot.length == 0)
			$foot = $("<div class='dg-dspv-form-foot' />").appendTo($form);
		
		for(var i=0; i<dataSetParams.length; i++)
		{
			var dsp = dataSetParams[i];
			var value = paramValues[dsp.name];
			
			var $item = $("<div class='dg-dspv-form-item' />").appendTo($content);
			
			var $labelDiv = $("<div class='dg-dspv-form-item-label' />").appendTo($item);
			chartSetting.renderDataSetParamValueFormLabel($form, $labelDiv, dsp, options);
			
			var $valueDiv = $("<div class='dg-dspv-form-item-value' />").appendTo($item);
			
			if(dsp.type == chartSetting.DataSetParamDataType.BOOLEAN)
			{
				var defaultSelOpts = undefined;
				
				if(!dsp.inputPayload)
					defaultSelOpts = [ { name: options.yesText, value: "true" }, { name: options.noText, value: "false" } ];
				
				//XXX 上面不应将defaultSelOpts对象赋值给dsp.inputPayload，因为dsp.inputPayload应是字符串类型，
				//图表编辑保存时会将dsp传输至后台而进行类型转换，如果赋值，则会报错
				
				if(dsp.inputType == InputType.RADIO)
					chartSetting.renderDataSetParamValueFormInputRadio($form, $valueDiv, dsp, value, options, defaultSelOpts);
				else if(dsp.inputType == InputType.CHECKBOX)
					chartSetting.renderDataSetParamValueFormInputCheckbox($form, $valueDiv, dsp, value, options, defaultSelOpts);
				else
					chartSetting.renderDataSetParamValueFormInputSelect($form, $valueDiv, dsp, value, options, defaultSelOpts);
			}
			else if(dsp.type == chartSetting.DataSetParamDataType.STRING)
			{
				if(dsp.inputType == InputType.SELECT)
					chartSetting.renderDataSetParamValueFormInputSelect($form, $valueDiv, dsp, value, options);
				else if(dsp.inputType == InputType.DATE)
					chartSetting.renderDataSetParamValueFormInputDate($form, $valueDiv, dsp, value, options);
				else if(dsp.inputType == InputType.TIME)
					chartSetting.renderDataSetParamValueFormInputTime($form, $valueDiv, dsp, value, options);
				else if(dsp.inputType == InputType.DATETIME)
					chartSetting.renderDataSetParamValueFormInputDateTime($form, $valueDiv, dsp, value, options);
				else if(dsp.inputType == InputType.RADIO)
					chartSetting.renderDataSetParamValueFormInputRadio($form, $valueDiv, dsp, value, options);
				else if(dsp.inputType == InputType.CHECKBOX)
					chartSetting.renderDataSetParamValueFormInputCheckbox($form, $valueDiv, dsp, value, options);
				else if(dsp.inputType == InputType.TEXTAREA)
					chartSetting.renderDataSetParamValueFormInputTextarea($form, $valueDiv, dsp, value, options);
				else
					chartSetting.renderDataSetParamValueFormInputText($form, $valueDiv, dsp, value, options);
			}
			else if(dsp.type == chartSetting.DataSetParamDataType.NUMBER)
			{
				if(dsp.inputType == InputType.SELECT)
					chartSetting.renderDataSetParamValueFormInputSelect($form, $valueDiv, dsp, value, options);
				else if(dsp.inputType == InputType.RADIO)
					chartSetting.renderDataSetParamValueFormInputRadio($form, $valueDiv, dsp, value, options);
				else if(dsp.inputType == InputType.CHECKBOX)
					chartSetting.renderDataSetParamValueFormInputCheckbox($form, $valueDiv, dsp, value, options);
				else if(dsp.inputType == InputType.TEXTAREA)
					chartSetting.renderDataSetParamValueFormInputTextarea($form, $valueDiv, dsp, value, options);
				else
					chartSetting.renderDataSetParamValueFormInputText($form, $valueDiv, dsp, value, options);
			}
		}
		
		if(!options.readonly)
		{
			var $submitBtn = $("[type='submit']", $foot);
			
			//允许自定义提交按钮
			if($submitBtn.length == 0)
				$submitBtn = $("<button type='submit' />").html(options.submitText).appendTo($foot);
		}
		
		var submitHandlerKey = chartFactory.builtinPropName("dspvFormSubmitHandler");
		var submitHandler = function()
		{
			if(options.readonly)
				return false;
			
			var validationOk = chartSetting.validateDataSetParamValueForm(this);
			
			if(validationOk)
				$("[type=submit]", $foot).removeClass("dg-form-invalid");
			else
				$("[type=submit]", $foot).addClass("dg-form-invalid");
			
			if(!validationOk)
				return false;
			
			if(options.submit)
			{
				var formData = chartSetting.getDataSetParamValueObj(this);
				return (options.submit.call(this, formData) == true);
			}
			else
				return false;
		};
		
		$form.data(submitHandlerKey, submitHandler);
		$form.on("submit", submitHandler);
		
		var formEle = $form[0];
		
		if(options.render)
			options.render.call(formEle, formEle);
		
		return formEle;
	};
	
	/**
	 * 销毁数据集参数值表单。
	 * 
	 * @param ancestor 渲染数据集参数值的<form>表单元素，或者它的祖先元素（其所有内部数据集参数值表单都会被销毁）。
	 */
	chartSetting.destroyDataSetParamValueForm = function(ancestor)
	{
		ancestor = $(ancestor);
		
		var $form = (ancestor.is("form") ? ancestor : $("form.dg-dspv-form", ancestor));
		
		$form.each(function()
		{
			var thisForm = $(this);
			
			var dateWidgets = $(".dg-dspv-form-widget-date", thisForm);
			dateWidgets.each(function()
			{
				chartSetting.destroyDatetimePicker($(this));
			});
			
			if(thisForm.attr("dg-generated-ele"))
				chartFactory.removeElementWithDerived(this);
			else
			{
				thisForm.removeClass("dg-dspv-form");
				var themeStyleName = thisForm.data("dgDspvFormThemeClassName");
				if(themeStyleName)
					thisForm.removeClass(themeStyleName);
				
				var des = chartFactory.derivedElements(this);
				if(des)
				{
					chartFactory.removeElementWithDerived($(des));
					chartFactory.derivedElements(this, null);
				}
				
				var submitHandlerKey = chartFactory.builtinPropName("dspvFormSubmitHandler");
				var submitHandler = thisForm.data(submitHandlerKey);
				thisForm.off("submit", submitHandler);
				thisForm.empty();
			}
		});
	};
	
	chartSetting.dataSetParamValueFormThemeStyle = function(chartTheme, isSubStyle)
	{
		var name = chartFactory.builtinPropName("DataSetParamValueForm" + (isSubStyle ? "SubYes" : "SubNo"));
		return chartFactory.themeStyleSheet(chartTheme, name, function()
		{
			var color = chartFactory.themeGradualColor(chartTheme, 1);
			var bgColor = chartFactory.themeGradualColor(chartTheme, 0);
			var borderColor = chartFactory.themeGradualColor(chartTheme, 0.5);
			
			var cssPrefix = (isSubStyle ? " " : "") + ".dg-dspv-form";
			
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
						cssPrefix + " .dg-dspv-form-item-value input",
						cssPrefix + " .dg-dspv-form-item-value textarea",
						cssPrefix + " .dg-dspv-form-item-value select",
						cssPrefix + " .dg-dspv-form-item-value select option",
						cssPrefix + " .dg-dspv-form-item-value .input"
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
						"background-color": chartFactory.themeGradualColor(chartTheme, 0.1),
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
						"background-color": chartFactory.themeGradualColor(chartTheme, 0.3)
					}
				}
			];
			
			return css;
		});
	};
	
	/**
	 * 渲染表单项标签。
	 * 
	 * @param $form
	 * @param $parent 渲染标签的父容器元素
	 * @param dataSetParam
	 * @param formOptions
	 */
	chartSetting.renderDataSetParamValueFormLabel = function($form, $parent, dataSetParam, formOptions)
	{
		var $label = $("<label />").html(dataSetParam.label ? dataSetParam.label : dataSetParam.name)
							.appendTo($parent);
		
		if(dataSetParam.desc)
			$label.attr("title", dataSetParam.desc);
	};
	
	/**
	 * 渲染输入项：文本框
	 * 
	 * @param $form
	 * @param $parent 渲染输入项的父容器元素
	 * @param dataSetParam
	 * @param value 可选
	 * @param formOptions
	 */
	chartSetting.renderDataSetParamValueFormInputText = function($form, $parent, dataSetParam, value, formOptions)
	{
		var $input = $("<input type='text' class='dg-dspv-form-input' />").attr("name", dataSetParam.name)
			.attr("value", (value == null ? "" : value)).appendTo($parent);
		
		if((dataSetParam.required+"") == "true")
			$input.attr("dg-validation-required", "true");
		
		if(chartSetting.DataSetParamDataType.NUMBER == dataSetParam.type)
			$input.attr("dg-validation-number", "true");
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
	 * @param $form
	 * @param $parent 渲染输入项的父容器元素
	 * @param dataSetParam
	 * @param value 可选
	 * @param formOptions
	 * @param defaultSelOpts 可选，默认下拉框选项集
	 */
	chartSetting.renderDataSetParamValueFormInputSelect = function($form, $parent, dataSetParam, value, formOptions, defaultSelOpts)
	{
		var payload = chartSetting.evalDataSetParamInputPayload(dataSetParam, []);
		
		if(defaultSelOpts && payload && payload.length == 0)
			payload = defaultSelOpts;
		
		if(chartSetting.isString(payload))
			payload = [ payload ];
		
		if($.isArray(payload))
			payload = { multiple: false, options: payload };
		
		if(value == null)
			value = [];
		else
			value = (value.length != undefined ? value : [ value ]);
		
		var $input = $("<select class='dg-dspv-form-input' />").attr("name", dataSetParam.name).appendTo($parent);
		
		if(payload.multiple)
			$input.attr("multiple", "true");
		
		var opts = (payload.options || []);
		
		for(var i=0; i<opts.length; i++)
		{
			var opt = opts[i];
			
			var optName = (opt.name != null ? opt.name : opt.value);
			var optVal = (opt.value != null ? opt.value : opt.name);
			if(optName == null)
				optName = opt;
			if(optVal == null)
				optVal = opt;
			
			var $opt = $("<option />").attr("value", optVal).html(optName).appendTo($input);
			
			if(chartSetting.containsValueForString(value, optVal))
				$opt.attr("selected", "selected");
		}
		
		if((dataSetParam.required+"") == "true")
			$input.attr("dg-validation-required", "true");
		
		if(chartSetting.DataSetParamDataType.NUMBER == dataSetParam.type)
			$input.attr("dg-validation-number", "true");
	};
	
	/**
	 * 渲染输入项：日期框
	 * 
	 * dataSetParam.inputPayload格式可以为：
	 * null、空字符串、
	 * 或者
	 * "{ format: '...Y|y...m...d...' }"、"{ format: '...Y|y...' }"（仅年份选择）  //数据集定义功能时
	 * 或者
	 * { format: '...Y|y...m...d...' }、{ format: '...Y|y...' }（仅年份选择）      //看板表单功能时
	 * 
	 * @param $form
	 * @param $parent 渲染输入项的父容器元素
	 * @param dataSetParam
	 * @param value 可选
	 * @param formOptions
	 */
	chartSetting.renderDataSetParamValueFormInputDate = function($form, $parent, dataSetParam, value, formOptions)
	{
		var options = chartSetting.evalDataSetParamInputPayload(dataSetParam, {});
		options = $.extend(
		{
			format: "Y-m-d",
			timepicker: false,
		},
		options);
		
		var $input = $("<input type='text' class='dg-dspv-form-input dg-dspv-form-widget-date' />").attr("name", dataSetParam.name)
			.attr("value", (value || "")).appendTo($parent);
		
		if(chartSetting.disableDateAwareInputAutocomplete)
			$input.attr("autocomplete", "off");
		
		if((dataSetParam.required+"") == "true")
			$input.attr("dg-validation-required", "true");
		
		if(chartSetting.DataSetParamDataType.NUMBER == dataSetParam.type)
			$input.attr("dg-validation-number", "true");
		
		var $wrapper = chartSetting.renderDatetimePicker($input, options, formOptions.chartTheme);
		chartFactory.derivedElements($form, $wrapper);
	};
	
	/**
	 * 渲染输入项：时间框
	 * 
	 * dataSetParam.inputPayload格式可以为：
	 * null、空字符串、
	 * 或者
	 * "{ format: '...H|h...i...s...' }"  //数据集定义功能时
	 * 或者
	 * { format: '...H|h...i...s...' }    //看板表单功能时
	 * 
	 * @param $form
	 * @param $parent 渲染输入项的父容器元素
	 * @param dataSetParam
	 * @param value 可选
	 * @param formOptions
	 */
	chartSetting.renderDataSetParamValueFormInputTime = function($form, $parent, dataSetParam, value, formOptions)
	{
		var options = chartSetting.evalDataSetParamInputPayload(dataSetParam, {});
		options = $.extend(
		{
			format: "H:i:s",
			datepicker: false,
			step:10,
		},
		options);
		
		var $input = $("<input type='text' class='dg-dspv-form-input dg-dspv-form-widget-date' />").attr("name", dataSetParam.name)
			.attr("value", (value || "")).appendTo($parent);
		
		if(chartSetting.disableDateAwareInputAutocomplete)
			$input.attr("autocomplete", "off");
		
		if((dataSetParam.required+"") == "true")
			$input.attr("dg-validation-required", "true");
		
		if(chartSetting.DataSetParamDataType.NUMBER == dataSetParam.type)
			$input.attr("dg-validation-number", "true");
		
		var $wrapper = chartSetting.renderDatetimePicker($input, options, formOptions.chartTheme);
		chartFactory.derivedElements($form, $wrapper);
	};
	
	/**
	 * 渲染输入项：日期时间框
	 * 
	 * dataSetParam.inputPayload格式可以为：
	 * null、空字符串、
	 * 或者
	 * "{ format: '...Y|y...m...d...H|h...i...s...' }"  //数据集定义功能时
	 * 或者
	 * { format: '...Y|y...m...d...H|h...i...s...' }    //看板表单功能时
	 *
	 * Y|y ：年份，四位长度，不足补0
	 * m   ：月份，两位长度，不足补0
	 * d   ：日，两位长度，不足补0
	 * H|h ：时，两位长度，不足补0
	 * i   ：分，两位长度，不足补0
	 * s   ：秒，两位长度，不足补0
	 * 
	 * @param $form
	 * @param $parent 渲染输入项的父容器元素
	 * @param dataSetParam
	 * @param value 可选
	 * @param formOptions
	 */
	chartSetting.renderDataSetParamValueFormInputDateTime = function($form, $parent, dataSetParam, value, formOptions)
	{
		var options = chartSetting.evalDataSetParamInputPayload(dataSetParam, {});
		options = $.extend(
		{
			format: "Y-m-d H:i:s",
			step:10
		},
		options);
		
		var $input = $("<input type='text' class='dg-dspv-form-input dg-dspv-form-widget-date' />").attr("name", dataSetParam.name)
			.attr("value", (value || "")).appendTo($parent);
		
		if(chartSetting.disableDateAwareInputAutocomplete)
			$input.attr("autocomplete", "off");
		
		if((dataSetParam.required+"") == "true")
			$input.attr("dg-validation-required", "true");
		
		if(chartSetting.DataSetParamDataType.NUMBER == dataSetParam.type)
			$input.attr("dg-validation-number", "true");
		
		var $wrapper = chartSetting.renderDatetimePicker($input, options, formOptions.chartTheme);
		chartFactory.derivedElements($form, $wrapper);
	};
	
	/**
	 * 渲染输入项：单选框
	 * 
	 * dataSetParam.inputPayload格式可以为：
	 * null、空字符串
	 * 或者
	 * "[ 待选项名值对象, ... ]"  //数据集定义功能时
	 * 或者
	 * [ 待选项名值对象, ... ]    //看板表单功能时
	 * 
	 * 其中，待选项名值对象格式允许为：
	 * { name: "...", value: ... }、{name: "..."}、{value: ...}、"..."
	 * 
	 * @param $form
	 * @param $parent 渲染输入项的父容器元素
	 * @param dataSetParam
	 * @param value 可选
	 * @param formOptions
	 * @param defaultSelOpts 可选，默认单选框选项集
	 */
	chartSetting.renderDataSetParamValueFormInputRadio = function($form, $parent, dataSetParam, value, formOptions, defaultSelOpts)
	{
		var opts = chartSetting.evalDataSetParamInputPayload(dataSetParam, []);
		
		if(defaultSelOpts && opts && opts.length == 0)
			opts = defaultSelOpts;
		
		if(chartSetting.isString(opts))
			opts = [ opts ];
		
		var $inputsWrapper = $("<div class='dg-dspv-form-inputs-wrapper' />").appendTo($parent);
		
		for(var i=0; i<opts.length; i++)
		{
			var opt = opts[i];
			
			var optName = (opt.name != null ? opt.name : opt.value);
			var optVal = (opt.value != null ? opt.value : opt.name);
			if(optName == null)
				optName = opt;
			if(optVal == null)
				optVal = opt;
			
			var eleId = chartFactory.uid();
			
			var $wrapper = $("<div class='dg-dspv-form-radio-wrapper' />").appendTo($inputsWrapper);
			
			var $input = $("<input type='radio' class='dg-dspv-form-input' />")
				.attr("id", eleId).attr("name", dataSetParam.name).attr("value", optVal).appendTo($wrapper);
			
			$("<label />").attr("for", eleId).html(optName).appendTo($wrapper);
			
			if((value+"") == (optVal+""))
				$input.attr("checked", "checked");
			
			if((dataSetParam.required+"") == "true")
				$input.attr("dg-validation-required", "true");
			
			if(chartSetting.DataSetParamDataType.NUMBER == dataSetParam.type)
				$input.attr("dg-validation-number", "true");
		}
	};
	
	/**
	 * 渲染输入项：复选框
	 * 
	 * dataSetParam.inputPayload格式可以为：
	 * null、空字符串
	 * 或者
	 * "[ 待选项名值对象, ... ]"  //数据集定义功能时
	 * 或者
	 * [ 待选项名值对象, ... ]    //看板表单功能时
	 * 
	 * 其中，待选项名值对象格式允许为：
	 * { name: "...", value: ... }、{name: "..."}、{value: ...}、"..."
	 * 
	 * @param $form
	 * @param $parent 渲染输入项的父容器元素
	 * @param dataSetParam
	 * @param value 可选，值、值数组
	 * @param formOptions
	 * @param defaultSelOpts 可选，默认复选框选项集
	 */
	chartSetting.renderDataSetParamValueFormInputCheckbox = function($form, $parent, dataSetParam, value, formOptions, defaultSelOpts)
	{
		var opts = chartSetting.evalDataSetParamInputPayload(dataSetParam, []);
		
		if(defaultSelOpts && opts && opts.length == 0)
			opts = defaultSelOpts;
		
		if(chartSetting.isString(opts))
			opts = [ opts ];
		
		if(value == null)
			value = [];
		else
			value = (value.length != undefined ? value : [ value ]);
		
		var $inputsWrapper = $("<div class='dg-dspv-form-inputs-wrapper' />").appendTo($parent);
		
		for(var i=0; i<opts.length; i++)
		{
			var opt = opts[i];
			
			var optName = (opt.name != null ? opt.name : opt.value);
			var optVal = (opt.value != null ? opt.value : opt.name);
			if(optName == null)
				optName = opt;
			if(optVal == null)
				optVal = opt;
			
			var eleId = chartFactory.uid();
			
			var $wrapper = $("<div class='dg-dspv-form-checkbox-wrapper' />").appendTo($inputsWrapper);
			
			var $input = $("<input type='checkbox' class='dg-dspv-form-input' />")
				.attr("id", eleId).attr("name", dataSetParam.name).attr("value", optVal).appendTo($wrapper);
			
			$("<label />").attr("for", eleId).html(optName).appendTo($wrapper);
			
			if(chartSetting.containsValueForString(value, optVal))
				$input.attr("checked", "checked");
			
			if((dataSetParam.required+"") == "true")
				$input.attr("dg-validation-required", "true");
			
			if(chartSetting.DataSetParamDataType.NUMBER == dataSetParam.type)
				$input.attr("dg-validation-number", "true");
		}
	};
	
	/**
	 * 渲染输入项：文本域
	 * 
	 * @param $form
	 * @param $parent 渲染输入项的父容器元素
	 * @param dataSetParam
	 * @param value 可选
	 * @param formOptions
	 */
	chartSetting.renderDataSetParamValueFormInputTextarea = function($form, $parent, dataSetParam, value, formOptions)
	{
		var $input = $("<textarea class='dg-dspv-form-input' />").attr("name", dataSetParam.name)
			.text(value || "").appendTo($parent);
		
		if((dataSetParam.required+"") == "true")
			$input.attr("dg-validation-required", "true");
		
		if(chartSetting.DataSetParamDataType.NUMBER == dataSetParam.type)
			$input.attr("dg-validation-number", "true");
	};
	
	/**
	 * 移除日期选择器组件root容器元素。
	 */
	chartSetting.removeDatetimePickerRoot = function()
	{
		var rootWrapperId = chartFactory._BUILT_IN_NAME_PART + "DatetimepickerRoot";
		$("#" + rootWrapperId).remove();
	};
	
	/**
	 * 渲染日期选择器组件。
	 */
	chartSetting.renderDatetimePicker = function($input, options, chartTheme)
	{
		//在这里检查并重写，避免依赖加载顺序
		if(!chartSetting._datetimepickerInited)
		{
			chartSetting.datetimepickerInit();
			chartSetting._datetimepickerInited = true;
		}
		
		var noSizeCss =
			 "width:0 !important;"
			+"height:0 !important;"
			+"padding:0 0 !important;"
			+"margin:0 0 !important;"
			+"border:0 !important;"
			+"border-width:0 !important;";
		
		var rootWrapperId = chartFactory._BUILT_IN_NAME_PART + "DatetimepickerRoot";
		var $rootWrapper = $("#" + rootWrapperId);
		if($rootWrapper.length < 1)
			$rootWrapper = $("<div style='"+noSizeCss+"' />").attr("id", rootWrapperId).appendTo(document.body);
		
		var wrapperId = chartFactory.uid();
		var $wrapper = $("<div style='"+noSizeCss+"' />").attr("id", wrapperId).appendTo($rootWrapper);
		
		if(chartTheme)
			$wrapper.addClass(chartSetting.datetimepickerThemeStyle(chartTheme));
		
		options = $.extend(
		{
			//inline应该为false，为true的话下面的datetimepickerThemeStyle函数创建的样式将不起作用
			inline: false,
			parentID: "#"+wrapperId,
			i18n:
			{
				zh: { months: ["1月","2月","3月","4月","5月","6月","7月","8月","9月","10月","11月","12月"] }
			}
		},
		options);
		
		//年份选择器
		if("Y" == options.format || "y" == options.format)
		{
			//显示确定按钮，用于直接选中默认年份
			options.showApplyButton = true;
			options.onGenerate = function(currentValue, $input)
			{
				var yearPickerInited = $(this).attr("yearPickerInited");
				if(!yearPickerInited)
				{
					$(this).attr("yearPickerInited", "yes");
					
					$(".xdsoft_prev", this).hide();
					$(".xdsoft_today_button", this).hide();
					$(".xdsoft_month", this).hide();
					$(".xdsoft_next", this).hide();
					$(".xdsoft_calendar", this).hide();
					
					$(".xdsoft_save_selected", this).removeClass("blue-gradient-button")
						.addClass("xdsoft_save_selected_year ui-button ui-corner-all").html(chartSetting.labels.confirm);
				}
			};
			options.onShow = function(currentValue,$input)
			{
				if(!$input.val())
				{
					//这样可以直接点击【确定】按钮选择默认年份
					this.setOptions({value: currentValue});
					$input.val("");
				}
			};
			options.onChangeYear = function(currentValue,$input)
			{
				this.setOptions({value: currentValue});
				$(".xdsoft_save_selected", this).click();
			};
		}
		
		$input.datetimepicker(options);
		
		return $wrapper;
	};
	
	/**
	 * 销毁日期选择器组件。
	 */
	chartSetting.destroyDatetimePicker = function($input)
	{
		try
		{
			$input.datetimepicker("destroy");
		}
		catch(e){}
	};
	
	chartSetting.datetimepickerInit = function()
	{
		$.datetimepicker.setLocale('zh');
		$.datetimepicker.setDateFormatter(chartSetting.datetimepickerDateFormatter);
	};
	
	//默认$.datetimepicker的DateFormatter有缺陷，支持格式有限，且对于"ymd"格式无法解析，所以这里重写
	chartSetting.datetimepickerDateFormatter =
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
				
				if(fmt == 'Y' || fmt == 'y')
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
				
				if(fmt == 'Y' || fmt == 'y')
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
				
				if(c == 'Y' || c == 'y' || c == 'm' || c == 'd'
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
	
	/**
	 * 获取或创建与指定图表主题匹配的datetimepicker组件样式表，并返回CSS类名。
	 * 
	 * @param chartTheme
	 */
	chartSetting.datetimepickerThemeStyle = function(chartTheme)
	{
		return chartFactory.themeStyleSheet(chartTheme, chartFactory.builtinPropName("Datetimepicker"), function()
		{
			var color = chartFactory.themeGradualColor(chartTheme, 1);
			var bgColor = chartFactory.themeGradualColor(chartTheme, 0);
			var borderColor = chartFactory.themeGradualColor(chartTheme, 0.3);
			var shadowColor = chartFactory.themeGradualColor(chartTheme, 0.9);
			var hoverColor = chartFactory.themeGradualColor(chartTheme, 0.3);
			
			var cssPrefix = " .xdsoft_datetimepicker";
			
			var css =
			[
				//主体
				{
					name: cssPrefix,
					value:
					{
						"color": color,
						"background": bgColor,
						"border-color": borderColor,
						"box-shadow": "0px 0px 6px " + shadowColor,
						"-webkit-box-shadow": "0px 0px 6px " + shadowColor
					}
				},
				//前景色
				{
					name: [ cssPrefix + " .xdsoft_calendar td", cssPrefix + " .xdsoft_calendar th" ],
					value:
					{
						"color": color
					}
				},
				//按钮
				{
					name:
					[
						cssPrefix + " .xdsoft_label i",
						cssPrefix + " .xdsoft_next",
						cssPrefix + " .xdsoft_prev",
						cssPrefix + " .xdsoft_today_button"
					],
					value:
					{
						"color": color
					}
				},
				//年、月
				{
					name: cssPrefix + " .xdsoft_label",
					value:
					{
						"background": bgColor
					}
				},
				//年、月下拉框
				{
					name: cssPrefix + " .xdsoft_label>.xdsoft_select",
					value:
					{
						"color": color,
						"background": bgColor,
						"border-color": borderColor,
						"box-shadow": "0px 0px 6px " + shadowColor,
						"-webkit-box-shadow": "0px 0px 6px " + shadowColor
					}
				},
				//时间框
				{
					name: cssPrefix + " .xdsoft_timepicker .xdsoft_time_box",
					value:
					{
						"border-color": borderColor
					}
				},
				//时间条目
				{
					name: cssPrefix + " .xdsoft_timepicker .xdsoft_time_box>div>div",
					value:
					{
						"color": color,
						"border-color": borderColor
					}
				},
				//悬停
				{
					name:
					[
						cssPrefix + " .xdsoft_calendar td:hover",
						cssPrefix + " .xdsoft_timepicker .xdsoft_time_box>div>div:hover",
						cssPrefix + " .xdsoft_label>.xdsoft_select>div>.xdsoft_option:hover"
					],
					value:
					{
						"color": color + " !important",
						"background": hoverColor + " !important"
					}
				},
				//今天
				{
					name: cssPrefix + " .xdsoft_calendar td.xdsoft_today",
					value:
					{
						"color": color,
						"font-weight": "bold"
					}
				},
				//选中
				{
					name:
					[
						cssPrefix + " .xdsoft_calendar td.xdsoft_default",
						cssPrefix + " .xdsoft_calendar td.xdsoft_current",
						cssPrefix + " .xdsoft_timepicker .xdsoft_time_box>div>div.xdsoft_current",
						cssPrefix + " .xdsoft_label>.xdsoft_select>div>.xdsoft_option.xdsoft_current"
					],
					value:
					{
						"color": chartTheme.highlightTheme.color,
						"background": chartTheme.highlightTheme.backgroundColor,
						"box-shadow": "none",
						"-webkit-box-shadow": "none"
					}
				},
				{
					name: cssPrefix + " .xdsoft_save_selected.xdsoft_save_selected_year",
					value:
					{
						"color": color,
						"background": bgColor,
						"border": "1px solid "+borderColor+" !important"
					}
				},
				{
					name: cssPrefix + " .xdsoft_save_selected.xdsoft_save_selected_year:hover",
					value:
					{
						"background": hoverColor
					}
				}
			];
			
			return css;
		});
	};
	
	chartSetting.evalDataSetParamInputPayload = function(dataSetParam, defaultValue)
	{
		if(typeof(dataSetParam.inputPayload) == "string" && dataSetParam.inputPayload != "")
			return chartFactory.evalSilently(dataSetParam.inputPayload, defaultValue);
		else
			return (dataSetParam.inputPayload || defaultValue);
	};
	
	chartSetting.isString = function(str)
	{
		return typeof(str) == "string";
	};
	
	/**
	 * 校验数据集参数值表单的必填项、数值项。
	 * 
	 * @param form
	 * @return true 验证通过；false 验证不通过
	 */
	chartSetting.validateDataSetParamValueForm = function(form)
	{
		var validationOk = true;
		
		var $itemValue = $(".dg-dspv-form-item-value", form);
		
		$itemValue.each(function()
		{
			var $required = $("[dg-validation-required]", this);
			
			if($required.length == 0)
				return;
			
			var type = $required.attr("type");
			
			if(type == "checkbox" || type == "radio")
			{
				var checkeds = $required.filter(":checked");
				if(checkeds.length == 0)
				{
					$(".dg-dspv-form-inputs-wrapper", this).addClass("dg-validation-required");
					validationOk = false;
				}
				else
					$(".dg-dspv-form-inputs-wrapper", this).removeClass("dg-validation-required");
			}
			else
			{
				var val = $required.val();
				
				if(chartFactory.isNullOrEmpty(val))
				{
					$required.addClass("dg-validation-required");
					validationOk = false;
				}
				else
					$required.removeClass("dg-validation-required");
			}
		});
		
		var regexNumber = /^-?\d+\.?\d*$/;
		
		$itemValue.each(function()
		{
			var $number = $("[dg-validation-number]", this);
			
			if($number.length == 0)
				return;
			
			var type = $number.attr("type");
			
			if(type == "checkbox" || type == "radio")
			{
				var checkeds = $number.filter(":checked");
				var myValid = true;
				
				for(var i=0; i<checkeds.length; i++)
				{
					if(!myValid)
						break;
					
					var val = $(checkeds[i]).attr("value");
					myValid = (chartFactory.isNullOrEmpty(val) ? true : regexNumber.test(val));
				}
				
				if(!myValid)
				{
					$(".dg-dspv-form-inputs-wrapper", this).addClass("dg-validation-number");
					validationOk = false;
				}
				else
					$(".dg-dspv-form-inputs-wrapper", this).removeClass("dg-validation-number");
			}
			else
			{
				var val = $number.val();
				val = ($.isArray(val) ? val: [ val ]);
				var myValid = true;
				
				for(var i=0; i<val.length; i++)
				{
					if(!myValid)
						break;
					
					myValid = (chartFactory.isNullOrEmpty(val[i]) ? true : regexNumber.test(val[i]));
				}
				
				if(!myValid)
				{
					$number.addClass("dg-validation-number");
					validationOk = false;
				}
				else
					$number.removeClass("dg-validation-number");
			}
		});
		
		return validationOk;
	};
	
	/**
	 * 获取图表数据集参数表单的参数值对象。
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
	chartSetting.getDataSetParamValueObj = function(form)
	{
		var $form = $(form);
		var array = $form.serializeArray();
		
		var multipleValNames = {};
		$("input[type='checkbox'], select[multiple]", $form).each(function()
		{
			var name = $(this).attr("name");
			multipleValNames[name] = true;
		});
		
		var re = {};
		
		$(array).each(function()
		{
			var name = this.name;
			var value = this.value;
			
			var prev = re[name];
			
			if(multipleValNames[name])
			{
				if(prev == null)
				{
					prev = [];
					re[name] = prev;
				}
				
				if(value != null)
					prev.push(value);
			}
			else if(prev == null)
			{
				re[name] = value;
			}
			//可能出现同名单值输入项的情况
			else
			{
				if(value != null)
				{
					if($.isArray(prev))
						prev.push(value);
					else
					{
						prev = [ prev ];
						prev.push(value);
						re[name] = prev;
					}
				}
			}
		});
		
		for(var p in re)
		{
			var v = re[p];
			
			if(chartFactory.isNullOrEmpty(v))
				re[p] = null;
		}
		
		//当多选框没有任一选中时，此时re不会出现对应属性，这里需要检查补充
		for(var p in multipleValNames)
		{
			if(re[p] === undefined)
				re[p] = null;
		}
		
		return re;
	};
	
	chartSetting.setDataSetParamValueObj = function(form, paramValueObj)
	{
		paramValueObj = (paramValueObj || {});
		
		$(".dg-dspv-form-input", form).each(function()
		{
			var $this = $(this);
			var name = $this.attr("name");
			
			if(!name)
				return;
			
			var value = paramValueObj[name];
			
			if($this.is("input"))
			{
				var type = $this.attr("type");
				
				if(type == "checkbox" || type == "radio")
				{
					if(value == null)
						value = [];
					else
						value = (value.length != undefined ? value : [ value ]);
					
					if(chartSetting.containsValueForString(value, $this.attr("value")))
						$this.prop("checked", true);
					else
						$this.prop("checked", false);
				}
				else
					$this.val(value || "");
			}
			else if($this.is("select"))
			{
				if(value == null)
					value = [];
				else
					value = (value.length != undefined ? value : [ value ]);
				
				$("option", $this).each(function()
				{
					var $thisOpt = $(this);
					
					if(chartSetting.containsValueForString(value, $thisOpt.attr("value")))
						$thisOpt.prop("selected", true);
					else
						$thisOpt.prop("selected", false);
				});
			}
			else if($this.is("textarea"))
			{
				$this.val(value || "");
			}
		});
	};
	
	chartSetting.containsValueForString = function(array, value)
	{
		if(array === value)
			return true;
		
		for(var i=0; i<array.length; i++)
		{
			if((array[i]+"") == (value+""))
				return true;
		}
		
		return false;
	};
	
	chartSetting.getDataSetParamValueForm = function($parent)
	{
		return $(".dg-dspv-form", $parent);
	};

	chartSetting.getDataSetParamValueFormHead = function(form)
	{
		return $(".dg-dspv-form-head", form);
	};
	
	chartSetting.getDataSetParamValueFormContent = function(form)
	{
		return $(".dg-dspv-form-content", form);
	};
	
	chartSetting.getDataSetParamValueFormFoot = function(form)
	{
		return $(".dg-dspv-form-foot", form);
	};
	
	chartSetting.bindChartSettingPanelEvent = function(chart)
	{
		var disableSetting = chart.disableSetting();
		
		if(disableSetting.param == true && disableSetting.data == true)
			return false;
		
		var $chart = chart.elementJquery();
		
		if(!$chart.attr("bind-chart-setting-panel-event"))
		{
			$chart.attr("bind-chart-setting-panel-event", "1");
			
			var mouseenterHandler = function(event)
			{
				if(chart.isActive())
					chartSetting.showChartSettingBox(chart);
			};
			var mouseleaveHandler = function(event)
			{
				if(chartSetting.isChartSettingParamPanelClosed(chart)
					&& chartSetting.isChartSettingDataPanelClosed(chart))
				{
					chartSetting.hideChartSettingBox(chart);
				}
			};
			
			$chart.mouseenter(mouseenterHandler).mouseleave(mouseleaveHandler);
			
			$chart.data("chartSettingPanel-mouseenterHandler", mouseenterHandler);
			$chart.data("chartSettingPanel-mouseleaveHandler", mouseleaveHandler);
		}
		
		return true;
	};
	
	chartSetting.unbindChartSettingPanelEvent = function(chart)
	{
		var disableSetting = chart.disableSetting();
		
		var $chart = chart.elementJquery();
		var mouseenterHandler = $chart.data("chartSettingPanel-mouseenterHandler");
		var mouseleaveHandler = $chart.data("chartSettingPanel-mouseleaveHandler");
		
		$chart.removeAttr("bind-chart-setting-panel-event");
		if(mouseenterHandler)
			$chart.off("mouseenter", mouseenterHandler);
		if(mouseleaveHandler)
			$chart.off("mouseleave", mouseleaveHandler);
		
		var $box = $(".dg-chart-setting-box", $chart);
		
		chartSetting.destroyDataSetParamValueForm($box);
		$box.remove();
	};
	
	chartSetting.showChartSettingBox = function(chart)
	{
		var disableSetting = chart.disableSetting();
		
		var $chart = chart.elementJquery();
		var $box = $(".dg-chart-setting-box", $chart);
		
		if($box.length <= 0)
		{
			$box = $("<div class='dg-chart-setting-box' />").appendTo($chart);
			
			chartSetting.setChartSettingBoxThemeStyle(chart, $box);
			
			//参数
			if(!disableSetting.param && chart.hasDataSetParam())
			{
				var $button = $("<button type='button' class='dg-chart-setting-button dg-chart-setting-param-button' />")
						.html(chartSetting.labels.param).appendTo($box);
				
				$button.click(function()
				{
					chartSetting.closeChartSettingDataPanel(chart);
					
					if(chartSetting.isChartSettingParamPanelClosed(chart))
						chartSetting.openChartSettingParamPanel($box, chart);
					else
						chartSetting.closeChartSettingParamPanel(chart);
				});
				
				$chart.click(function(event)
				{
					if(!chartSetting.isChartSettingParamPanelClosed(chart))
					{
						if($(event.target).closest(".dg-chart-setting-box").length == 0)
							chartSetting.closeChartSettingParamPanel(chart);
					}
				});
			}
			
			//数据
			if(!disableSetting.data)
			{
				var $button = $("<button type='button' class='dg-chart-setting-button dg-chart-setting-data-button' />")
						.html(chartSetting.labels.data).appendTo($box);
				
				$button.click(function()
				{
					chartSetting.closeChartSettingParamPanel(chart);
					
					if(chartSetting.isChartSettingDataPanelClosed(chart))
						chartSetting.openChartSettingDataPanel($box, chart);
					else
						chartSetting.closeChartSettingDataPanel(chart);
				});
				
				$chart.click(function(event)
				{
					if(!chartSetting.isChartSettingDataPanelClosed(chart))
					{
						//点击固定列的<td>，祖先元素竟然只能追溯到<table>！！！这里临时添加标识样式类名解决此问题！！！
						if($(event.target).closest(".dg-chart-setting-box, .dataTableClassForFixedColumnAncestor").length == 0)
							chartSetting.closeChartSettingDataPanel(chart);
					}
				});
			}
		}
		
		$box.show();
	};
	
	chartSetting.hideChartSettingBox = function(chart)
	{
		$(".dg-chart-setting-box", chart.elementJquery()).hide();
	};
	
	chartSetting.setChartSettingBoxThemeStyle = function(chart, $box)
	{
		chart.themeStyleSheet(chartFactory.builtinPropName("ChartSettingBox"), function()
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
	chartSetting.openChartSettingParamPanel = function($box, chart)
	{
		var $chart = chart.elementJquery();
		var dataSetBinds = chart.dataSetBinds();
		
		var $panel = $(".dg-chart-setting-param-panel", $box);
		
		if($panel.length <= 0)
		{
			$panel = $("<div class='dg-chart-setting-panel dg-chart-setting-param-panel' />").appendTo($box);
			
			//先显示，避免布局计算错误
			$panel.show();
			
			var $panelHead = $("<div class='dg-chart-setting-panel-head' />").appendTo($panel);
			var $panelContent = $("<div class='dg-chart-setting-panel-content' />").appendTo($panel);
			var $panelFoot = $("<div class='dg-chart-setting-panel-foot' />").appendTo($panel);
			
			$("<div class='dg-chart-setting-panel-head-title' />").html(chartSetting.labels.chartParam).appendTo($panelHead);
			var $headBtns = $("<div class='dg-chart-setting-panel-head-btns' />").appendTo($panelHead);
			$("<button type='button' class='dg-chart-setting-panel-closebtn' />")
				.html(chartSetting.labels.close).appendTo($headBtns)
				.click(function()
				{
					chartSetting.closeChartSettingParamPanel(chart);
				});
			
			var $button = $("<button type='button' />").html(chartSetting.labels.confirm).appendTo($panelFoot);
			
			chartSetting.setChartSetingPanelContentSizeRange(chart, $panel, $panelContent, $panelFoot);
			
			for(var i=0; i<dataSetBinds.length; i++)
			{
				var params = chart.dataSetParams(dataSetBinds[i]);
				
				if(!params || params.length == 0)
					continue;
				
				var myTitle = chartSetting.evalDataSetBindPanelTitle(chart, dataSetBinds, i);
				
				var $fp = $("<div class='dg-datasetbind-section' />").data("dataSetBindIndex", i).appendTo($panelContent);
				var $head = $("<div class='dg-datasetbind-section-head' />").html(myTitle).appendTo($fp);
				var $content = $("<div class='dg-datasetbind-section-content' />").appendTo($fp);
				chartSetting.renderDataSetParamValueForm($content, params,
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
						chartSetting.getDataSetParamValueFormFoot(this).hide();
					}
				});
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
					
					var $form = chartSetting.getDataSetParamValueForm($this);
					
					if(!chartSetting.validateDataSetParamValueForm($form))
						validateOk = false;
					else
					{
						var myIndex = $this.data("dataSetBindIndex");
						var myParamValues = chartSetting.getDataSetParamValueObj($form);
						paramValuess.push({ index : myIndex, paramValues: myParamValues });
					}
				});
				
				if(validateOk)
				{
					$thisButton.removeClass("dg-param-value-form-invalid");
					
					for(var i=0; i<paramValuess.length; i++)
						chartSetting.dataSetBindParamValues(chart, paramValuess[i].index, paramValuess[i].paramValues);
					
					if(chartSetting.closeChartSettingParamPanelOnSubmit)
						chartSetting.closeChartSettingParamPanel(chart);
					
					chart.refreshData();
				}
				else
					$thisButton.addClass("dg-param-value-form-invalid");
			});
		}
		else
		{
			//先显示，避免布局计算错误
			$panel.show();
			
			$(".dg-datasetbind-section", $panel).each(function()
			{
				var dataSetBindIndex = $(this).data("dataSetBindIndex");
				var $form = chartSetting.getDataSetParamValueForm(this);
				
				chartSetting.setDataSetParamValueObj($form, chart.dataSetParamValues(dataSetBindIndex));
			});
		}
		
		chartSetting.adjustChartSetingPanelPosition($panel);
		
		//聚焦至第一个可操作输入框
		chartSetting.focusOnFirstInput($("form:first", $panel));
	};
	
	chartSetting.dataSetBindParamValues = function(chart, dataSetBindIndex, paramValues)
	{
		//这里设置参数应采用inflate模式，因为数据集允许隐式参数（未明确定义数据集参数的参数化语法），这里不应清除它们
		chart.dataSetParamValues(dataSetBindIndex, paramValues, true);
	};
	
	/**
	 * 关闭图表参数面板。
	 */
	chartSetting.closeChartSettingParamPanel = function(chart)
	{
		$(".dg-chart-setting-param-panel", chart.elementJquery()).hide();
	};
	
	/**
	 * 获取图表参数面板。
	 */
	chartSetting.getChartSettingParamPanel = function(chart)
	{
		return $(".dg-chart-setting-param-panel", chart.elementJquery());
	};
	
	chartSetting.isChartSettingParamPanelClosed = function(chart)
	{
		var $panel = $(".dg-chart-setting-param-panel", chart.elementJquery());
		
		return ($panel.length == 0 || $panel.is(":hidden"));
	};
	
	/**
	 * 打开图表数据面板。
	 */
	chartSetting.openChartSettingDataPanel = function($box, chart)
	{
		var $chart = chart.elementJquery();
		
		var dataSetBinds = chart.dataSetBinds();
		
		var $panel = $(".dg-chart-setting-data-panel", $box);
		
		if($panel.length <= 0)
		{
			$panel = $("<div class='dg-chart-setting-panel dg-chart-setting-data-panel' />").appendTo($box);
			
			//先显示，避免布局计算错误
			$panel.show();
			
			var $panelHead = $("<div class='dg-chart-setting-panel-head' />").appendTo($panel);
			var $panelContent = $("<div class='dg-chart-setting-panel-content' />").appendTo($panel);
			var $panelFoot = $("<div class='dg-chart-setting-panel-foot' />").appendTo($panel);
			
			$("<div class='dg-chart-setting-panel-head-title' />").html(chartSetting.labels.chartData).appendTo($panelHead);
			var $headBtns = $("<div class='dg-chart-setting-panel-head-btns' />").appendTo($panelHead);
			$("<button type='button' class='dg-chart-setting-panel-closebtn' />")
				.html(chartSetting.labels.close).appendTo($headBtns)
				.click(function()
				{
					chartSetting.closeChartSettingDataPanel(chart);
				});
			
			chartSetting.setChartSettingDataPanelThemeStyle(chart, $panel);
			chartSetting.setChartSetingPanelContentSizeRange(chart, $panel, $panelContent,$panelFoot);
			
			for(var i=0; i<dataSetBinds.length; i++)
			{
				var myTitle = chartSetting.evalDataSetBindPanelTitle(chart, dataSetBinds, i);
				
				var $fp = $("<div class='dg-datasetbind-section' />").data("dataSetBindIndex", i).appendTo($panelContent);
				var $head = $("<div class='dg-datasetbind-section-head' />").html(myTitle).appendTo($fp);
				var $content = $("<div class='dg-datasetbind-section-content' />").appendTo($fp);
				
				var tableId = chartSetting.initDataSetBindDataTable(chart, dataSetBinds, i, $content);
				
				$fp.data("chartDataTableId", tableId);
			}
		}
		else
		{
			//先显示，避免布局计算错误
			$panel.show();
			
			$(".dg-datasetbind-section", $panel).each(function()
			{
				var dataSetBindIndex = $(this).data("dataSetBindIndex");
				var tableId = $(this).data("chartDataTableId");
				var $dataTable = $("#"+tableId, this);
				
				chartSetting.updateChartSettingDataTableData(chart, dataSetBinds, dataSetBindIndex, $dataTable);
			});
		}
		
		chartSetting.adjustChartSetingPanelPosition($panel);
		
		//当设置完所有表格后再重新调整，避免出现列头未对齐、固定列鼠标悬浮不起作用等问题
		$(".dg-datasetbind-section", $panel).each(function()
		{
			var tableId = $(this).data("chartDataTableId");
			var dataTable = $("#"+tableId, this).DataTable();
			
			chartSetting.adjustColumn(dataTable);
		});
	};
	
	/**
	 * 关闭图表数据面板。
	 */
	chartSetting.closeChartSettingDataPanel = function(chart)
	{
		$(".dg-chart-setting-data-panel", chart.elementJquery()).hide();
	};
	
	/**
	 * 获取图表数据面板。
	 */
	chartSetting.getChartSettingDataPanel = function(chart)
	{
		return $(".dg-chart-setting-data-panel", chart.elementJquery());
	};
	
	chartSetting.isChartSettingDataPanelClosed = function(chart)
	{
		var $panel = $(".dg-chart-setting-data-panel", chart.elementJquery());
		
		return ($panel.length == 0 || $panel.is(":hidden"));
	};
	
	chartSetting.initDataSetBindDataTable = function(chart, dataSetBinds, index, $parent)
	{
		var dataSetBind = dataSetBinds[index];
		var dataSetFields = chart.dataSetFields(dataSetBind);
		var fieldSigns = (chart.dataSetFieldSigns(dataSetBind) || {});
		var dataSigns = (chart.plugin && chart.plugin.dataSigns ? chart.plugin.dataSigns : []);
		var signFields = [];
		
		for(var i=0; i<dataSetFields.length; i++)
		{
			var name = dataSetFields[i].name;
			var signs = fieldSigns[name];
			
			if(signs != null && signs.length > 0)
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
			title: chartSetting.labels.serialNumber,
			orderable: false,
			data: null,
			defaultContent: "",
			width: "4em"
		});
		
		for(var i=0; i<signFields.length; i++)
		{
			var column =
			{
				title: chartSetting.evalDataSetBindDataTableColumnTitle(chart, dataSetBind, signFields[i], fieldSigns, dataSigns),
				data: signFields[i].name,
				defaultContent: "",
				orderable: false,
				searchable: false,
				render: function(value, type, row, meta)
				{
					//单元格展示绘制
					if(type == "display")
					{
						return chartFactory.escapeHtml(value);
					}
					//其他绘制，比如排序
					else
						return value;
				}
			};
			
			columns.push(column);
		}
		
		if(chart.isMutableModel(dataSetBind))
		{
			columns.push(
			{
				title: chartSetting.labels.dataDetail,
				orderable: false,
				data: null,
				defaultContent: "",
				render: function(value, type, row, meta)
				{
					//单元格展示绘制
					if(type == "display")
					{
						return chartFactory.toJsonString(row);
					}
					//其他绘制，比如排序
					else
						return value;
				}
			});
		}
		
		var results = chart.updateResults();
		var result = chart.resultAt(results, index);
		var data = chart.resultDatas(result);
		
		var scrollY = chart.elementJquery().height()*2/5;
		
		var tableOptions =
		{
			"columns": columns,
			"data" : data,
			"ordering": false,
			"scrollX": true,
			"scrollY": scrollY,
			"autoWidth": true,
	        "scrollCollapse": false,
	        "paging": false,
	        "dom": "t",
			"select" : { style : 'os' },
			"searching" : false,
			"fixedColumns": { leftColumns: 1 },
			"language":
		    {
				"emptyTable": "",
				"zeroRecords": ""
			},
			rowCallback: function(row, data, displayNum, displayIndex, dataIndex)
			{
				$("td:first", row).html(displayIndex+1);
			}
		};
		
		var table = $("<table width='100%' class='hover stripe dataTableClassForFixedColumnAncestor'></table>").appendTo($parent);
		var tableId = chartFactory.uid();
		table.attr("id", tableId);
		
		table.dataTable(tableOptions);
		
		return tableId;
	};
	
	chartSetting.evalDataSetBindDataTableColumnTitle = function(chart, dataSetBind, dataSetField, fieldSigns, dataSigns)
	{
		var title = chart.dataSetFieldAlias(dataSetBind, dataSetField);
		
		var name = dataSetField.name;
		var signs = fieldSigns[name];
		
		if(signs != null && signs.length > 0)
		{
			var signName = signs[0];
			
			for(var i=0; i<dataSigns.length; i++)
			{
				if(dataSigns[i].name == signName)
				{
					if(dataSigns[i].nameLabel && dataSigns[i].nameLabel.value)
					{
						signName = dataSigns[i].nameLabel.value +"("+signName+")";
					}
					
					break;
				}
			}
			
			title = "<a title='"+chartFactory.escapeHtml(title)+"'>"+chartFactory.escapeHtml(signName)+"</a>";
		}
		
		return title;
	};
	
	chartSetting.setChartSettingDataPanelThemeStyle = function(chart, $panel)
	{
		chart.themeStyleSheet(chartFactory.builtinPropName("ChartSettingDataPanel"), function()
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
						cssPrefix + " table.dataTable thead th",
						cssPrefix + " table.dataTable thead td"
					],
					value:
					{
						"color": theme.color,
						"background-color": bgColor + " !important"
					}
				},
				{
					name:
					[
						cssPrefix + " table.dataTable tbody tr",
						cssPrefix + " table.dataTable tbody tr td",
					],
					value:
					{
						"color": theme.color
					}
				},
				{
					name:
					[
						cssPrefix + " table.dataTable.stripe tbody tr.odd",
						cssPrefix + " table.dataTable.stripe tbody tr.odd td"
					],
					value:
					{
						"background-color": chart.themeGradualColor(0.1)
					}
				},
				{
					name:
					[
						cssPrefix + " table.dataTable.stripe tbody tr.even",
						cssPrefix + " table.dataTable.stripe tbody tr.even td"
					],
					value:
					{
						"background-color": bgColor
					}
				},
				{
					name:
					[
						cssPrefix + " table.dataTable.hover tbody tr:hover",
						cssPrefix + " table.dataTable.hover tbody tr:hover td"
					],
					value:
					{
						"background-color": chart.themeGradualColor(0.3)
					}
				},
				{
					name:
					[
						cssPrefix + " table.dataTable tbody tr.selected",
						cssPrefix + " table.dataTable tbody tr.selected td",
						cssPrefix + " table.dataTable.stripe tbody tr.odd.selected",
						cssPrefix + " table.dataTable.stripe tbody tr.odd.selected td",
						cssPrefix + " table.dataTable.stripe tbody tr.even.selected",
						cssPrefix + " table.dataTable.stripe tbody tr.even.selected td",
						cssPrefix + " table.dataTable.hover tbody tr:hover.selected",
						cssPrefix + " table.dataTable.hover tbody tr:hover.selected td"
					],
					value:
					{
						"color": theme.highlightTheme.color,
						"background-color": theme.highlightTheme.backgroundColor
					}
				}
			];
			
			return css;
		});
	};
	
	chartSetting.updateChartSettingDataTableData = function(chart, dataSetBinds, index, $dataTable)
	{
		var results = chart.updateResults();
		var result = chart.resultAt(results, index);
		var data = chart.resultDatas(result);
		
		var dataTable = $dataTable.DataTable();
		
		var rows = dataTable.rows();
		var removeRowIndexes = [];
		var dataIndex = 0;
		
		rows.every(function(rowIndex)
		{
			if(dataIndex >= data.length)
				removeRowIndexes.push(rowIndex);
			else
				this.data(data[dataIndex]);
			
			dataIndex++;
		});
		
		for(; dataIndex<data.length; dataIndex++)
			var row = dataTable.row.add(data[dataIndex]);
		
		if(removeRowIndexes.length > 0)
			dataTable.rows(removeRowIndexes).remove();
		
		dataTable.draw();
	};
	
	chartSetting.evalDataSetBindPanelTitle = function(chart, dataSetBinds, index)
	{
		var title = (dataSetBinds.length > 1 ? (index+1)+". " : "") + chart.dataSetAlias(dataSetBinds[index]);
		if(title != dataSetBinds[index].dataSet.name)
			title += " ("+dataSetBinds[index].dataSet.name+")";
		
		return title;
	};
	
	chartSetting.setChartSetingPanelContentSizeRange = function(chart, $panel, $panelContent, $panelFoot)
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
	
	chartSetting.adjustChartSetingPanelPosition = function($panel)
	{
		var offset = $panel.offset();
		
		if(offset.left >= 0 && offset.top >= 0)
			return;
		
		var position = $panel.position();
		
		if(offset.left < 0)
			position.left = position.left + Math.abs(offset.left) + 10;
		
		if(offset.top < 0)
			position.top = position.top + Math.abs(offset.top);
		
		$panel.css("left", position.left);
		$panel.css("top", position.top);
		$panel.css("right", "unset");
	};
	
	chartSetting.adjustColumn = function(dataTable)
	{
		dataTable.columns.adjust();
		
		var initOptions = dataTable.init();
		
		if(initOptions.fixedHeader)
			dataTable.fixedHeader.adjust();
		
		/*
		if(initOptions.fixedColumns)
			dataTable.fixedColumns.relayout();
		*/
	};
	
	//聚焦至指定元素内的第一个可操作（非只读、非禁用）输入框
	chartSetting.focusOnFirstInput = function(ele)
	{
		var input = $(":input:not(:disabled,[readonly]):first", ele); 
		input.focus();
	};
})
(this);

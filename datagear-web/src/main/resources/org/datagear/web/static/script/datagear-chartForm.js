/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

/**
 * 图表表单库。
 * 全局变量名：window.chartFactory.chartForm
 * 
 * 加载时依赖：
 *   无
 * 
 * 运行时依赖:
 *   jquery.js
 *   datagear-chartFactory.js
 */
(function(global)
{
	var chartFactory = (global.chartFactory || (global.chartFactory = {}));
	var chartForm = (chartFactory.chartForm || (chartFactory.chartForm = {}));
	
	//@deprecated 兼容1.8.1版本的window.chartForm变量名，未来版本会移除
	global.chartForm = chartForm;
	
	//org.datagear.analysis.DataSetParam.DataType
	chartForm.DataSetParamDataType =
	{
		STRING: "STRING",
		BOOLEAN: "BOOLEAN",
		NUMBER: "NUMBER"
	};
	
	//org.datagear.analysis.DataSetParam.InputType
	chartForm.DataSetParamInputType =
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
	
	chartForm.labels = (chartForm.labels ||
	{
		confirm: "确定",
		yes: "是",
		no: "否",
		set: "设置",
		openOrCloseSetPanel: "打开/关闭设置面板",
		colon: "：",
		setDataSetParamValue: "设置数据集参数值"
	});
	
	//datetimepicker组件I18N配置
	chartForm.datetimepickerI18n = (chartForm.datetimepickerI18n ||
	{
		zh:
		{
			months: ["1月","2月","3月","4月","5月","6月","7月","8月","9月","10月","11月","12月"]
		}
	});
	
	//是否禁用日期组件输入框的浏览器自动完成功能，浏览器自动完成功能会阻挡日期选择框，默认禁用
	chartForm.disableDateAwareInputAutocomplete = (chartForm.disableDateAwareInputAutocomplete || true);
	
	/**
	 * 渲染数据集参数值表单。
	 * 
	 * @param $parent 用于渲染表单的父元素，如果不是<form>元素，此函数将会自动新建<form>子元素，<form>元素结构也允许预先自定义
	 * @param dataSetParams 数据集参数集，格式参考：org.datagear.analysis.DataSetParam，也可附加"label"属性，用于定义输入项标签
	 * @param options 渲染配置项，格式为：
	 * 			{
	 *              chartTheme: {...}              //可选，用于支持渲染表单样式的图表主题
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
	chartForm.renderDataSetParamValueForm = function($parent, dataSetParams, options)
	{
		options = $.extend(
		{
			submitText: chartForm.labels.confirm,
			labelColon: chartForm.labels.colon,
			readonly: false,
			yesText: chartForm.labels.yes,
			noText: chartForm.labels.no
		},
		(options || {}));
		
		var paramValues = (options.paramValues || {});
		var InputType = chartForm.DataSetParamInputType;
		
		var $form = ($parent.is("form") ? $parent : $("<form />").appendTo($parent));
		
		$form.addClass("dg-dspv-form");
		
		//创建表单样式表
		if(options.chartTheme)
			chartForm.setDataSetParamValueFormStyle($form, options.chartTheme);
		
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
			var labelText = (dsp.label ? dsp.label : dsp.name) + options.labelColon;
			var value = paramValues[dsp.name];
			
			var $item = $("<div class='dg-dspv-form-item' />").appendTo($content);
			
			var $labelDiv = $("<div class='dg-dspv-form-item-label' />").appendTo($item);
			var $label = $("<label />").html(labelText).appendTo($labelDiv);
			
			if(dsp.desc)
				$label.attr("title", dsp.desc);
			
			var $valueDiv = $("<div class='dg-dspv-form-item-value' />").appendTo($item);
			
			if(dsp.type == chartForm.DataSetParamDataType.BOOLEAN)
			{
				var defaultSelOpts = undefined;
				
				if(!dsp.inputPayload)
					defaultSelOpts = [ { name: options.yesText, value: "true" }, { name: options.noText, value: "false" } ];
				
				//XXX 上面不应将defaultSelOpts对象赋值给dsp.inputPayload，因为dsp.inputPayload应是字符串类型，
				//图表编辑保存时会将dsp传输至后台而进行类型转换，如果赋值，则会报错
				
				if(dsp.inputType == InputType.RADIO)
					chartForm.renderDataSetParamValueFormInputRadio($valueDiv, dsp, value, options.chartTheme, defaultSelOpts);
				else if(dsp.inputType == InputType.CHECKBOX)
					chartForm.renderDataSetParamValueFormInputCheckbox($valueDiv, dsp, value, options.chartTheme, defaultSelOpts);
				else
					chartForm.renderDataSetParamValueFormInputSelect($valueDiv, dsp, value, options.chartTheme, defaultSelOpts);
			}
			else if(dsp.type == chartForm.DataSetParamDataType.STRING)
			{
				if(dsp.inputType == InputType.SELECT)
					chartForm.renderDataSetParamValueFormInputSelect($valueDiv, dsp, value, options.chartTheme);
				else if(dsp.inputType == InputType.DATE)
					chartForm.renderDataSetParamValueFormInputDate($valueDiv, dsp, value, options.chartTheme);
				else if(dsp.inputType == InputType.TIME)
					chartForm.renderDataSetParamValueFormInputTime($valueDiv, dsp, value, options.chartTheme);
				else if(dsp.inputType == InputType.DATETIME)
					chartForm.renderDataSetParamValueFormInputDateTime($valueDiv, dsp, value, options.chartTheme);
				else if(dsp.inputType == InputType.RADIO)
					chartForm.renderDataSetParamValueFormInputRadio($valueDiv, dsp, value, options.chartTheme);
				else if(dsp.inputType == InputType.CHECKBOX)
					chartForm.renderDataSetParamValueFormInputCheckbox($valueDiv, dsp, value, options.chartTheme);
				else if(dsp.inputType == InputType.TEXTAREA)
					chartForm.renderDataSetParamValueFormInputTextarea($valueDiv, dsp, value, options.chartTheme);
				else
					chartForm.renderDataSetParamValueFormInputText($valueDiv, dsp, value, options.chartTheme);
			}
			else if(dsp.type == chartForm.DataSetParamDataType.NUMBER)
			{
				if(dsp.inputType == InputType.SELECT)
					chartForm.renderDataSetParamValueFormInputSelect($valueDiv, dsp, value, options.chartTheme);
				else if(dsp.inputType == InputType.RADIO)
					chartForm.renderDataSetParamValueFormInputRadio($valueDiv, dsp, value, options.chartTheme);
				else if(dsp.inputType == InputType.CHECKBOX)
					chartForm.renderDataSetParamValueFormInputCheckbox($valueDiv, dsp, value, options.chartTheme);
				else if(dsp.inputType == InputType.TEXTAREA)
					chartForm.renderDataSetParamValueFormInputTextarea($valueDiv, dsp, value, options.chartTheme);
				else
					chartForm.renderDataSetParamValueFormInputText($valueDiv, dsp, value, options.chartTheme);
			}
		}
		
		if(!options.readonly)
		{
			var $submitBtn = $("[type='submit']", $foot);
			
			//允许自定义提交按钮
			if($submitBtn.length == 0)
				$submitBtn = $("<button type='submit' />").html(options.submitText).appendTo($foot);
		}
		
		$form.submit(function()
		{
			if(options.readonly)
				return false;
			
			var validationOk = chartForm.validateDataSetParamValueForm(this);
			
			if(validationOk)
				$("[type=submit]", $foot).removeClass("dg-form-invalid");
			else
				$("[type=submit]", $foot).addClass("dg-form-invalid");
			
			if(!validationOk)
				return false;
			
			if(options.submit)
			{
				var formData = chartForm.getDataSetParamValueObj(this);
				return (options.submit.call(this, formData) == true);
			}
			else
				return false;
		});
		
		if(options.render)
			options.render.call($form[0]);
		
		return $form[0];
	};
	
	chartForm.setDataSetParamValueFormStyle = function($form, chartTheme)
	{
		var styleClassName = chartTheme._dataSetParamValueFormStyleClassName;
		if(!styleClassName)
		{
			styleClassName = global.chartFactory.nextElementId();
			chartTheme._dataSetParamValueFormStyleClassName = styleClassName;
		}
		
		$form.addClass(styleClassName);
		
		var styleId = (chartTheme._dataSetParamValueFormStyleSheetId
				|| (chartTheme._dataSetParamValueFormStyleSheetId = global.chartFactory.nextElementId()));
		
		if(global.chartFactory.isStyleSheetCreated(styleId))
			return false;
		
		var qualifier = "." + styleClassName;
		
		var color = chartFactory.getGradualColor(chartTheme, 1);
		var bgColor = chartFactory.getGradualColor(chartTheme, 0);
		var borderColor = chartFactory.getGradualColor(chartTheme, 0.5);
		var hoverColor = chartFactory.getGradualColor(chartTheme, 0.3);
		
		var cssText =
			qualifier + ".dg-dspv-form{"
			+"  color: "+color+";"
			+"  background: "+bgColor+";"
			+"  border-color: "+borderColor+";"
			+"} "
			+qualifier + ".dg-dspv-form .dg-dspv-form-item-value input,"
			+qualifier + ".dg-dspv-form .dg-dspv-form-item-value textarea,"
			+qualifier + ".dg-dspv-form .dg-dspv-form-item-value select,"
			+qualifier + ".dg-dspv-form .dg-dspv-form-item-value select option,"
			+qualifier + ".dg-dspv-form .dg-dspv-form-item-value .input{"
			+"  color: "+color+";"
			+"  background: "+bgColor+";"
			+"  border-color: "+borderColor+";"
			+"} "
			+qualifier + ".dg-dspv-form button,"
			+qualifier + ".dg-dspv-form input[type=button],"
			+qualifier + ".dg-dspv-form input[type=submit],"
			+qualifier + ".dg-dspv-form .button{"
			+"  color: "+color+";"
			+"  background: "+chartFactory.getGradualColor(chartTheme, 0.1)+";"
			+"  border-color: "+borderColor+";"
			+"} "
			+qualifier + ".dg-dspv-form button:hover,"
			+qualifier + ".dg-dspv-form input[type=button]:hover,"
			+qualifier + ".dg-dspv-form input[type=submit]:hover,"
			+qualifier + ".dg-dspv-form .button:hover{"
			+"  background: "+chartFactory.getGradualColor(chartTheme, 0.3)+";"
			+"} "
			;
		
		global.chartFactory.createStyleSheet(styleId, cssText);
		
		return true;
	};
	
	/**
	 * 渲染输入项：文本框
	 * 
	 * @param $parent
	 * @param dataSetParam
	 * @param value 可选
	 * @param chartTheme 可选
	 */
	chartForm.renderDataSetParamValueFormInputText = function($parent, dataSetParam, value, chartTheme)
	{
		var $input = $("<input type='text' class='dg-dspv-form-input' />").attr("name", dataSetParam.name)
			.attr("value", (value || "")).appendTo($parent);
		
		if((dataSetParam.required+"") == "true")
			$input.attr("dg-validation-required", "true");
		
		if(chartForm.DataSetParamDataType.NUMBER == dataSetParam.type)
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
	 * @param $parent
	 * @param dataSetParam
	 * @param value 可选
	 * @param chartTheme 可选
	 * @param defaultSelOpts 可选，默认下拉框选项集
	 */
	chartForm.renderDataSetParamValueFormInputSelect = function($parent, dataSetParam, value, chartTheme, defaultSelOpts)
	{
		var payload = chartForm.evalDataSetParamInputPayload(dataSetParam, []);
		
		if(defaultSelOpts && payload && payload.length == 0)
			payload = defaultSelOpts;
		
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
			
			if(chartForm.containsValueForString(value, optVal))
				$opt.attr("selected", "selected");
		}
		
		if((dataSetParam.required+"") == "true")
			$input.attr("dg-validation-required", "true");
		
		if(chartForm.DataSetParamDataType.NUMBER == dataSetParam.type)
			$input.attr("dg-validation-number", "true");
	};
	
	/**
	 * 渲染输入项：日期框
	 * 
	 * dataSetParam.inputPayload格式可以为：
	 * null、空字符串、
	 * 或者
	 * "{ ... }"  //datetimepicker配置选项JSON字符串，数据集定义功能时
	 * 或者
	 * { ... }    //datetimepicker配置选项对象，看板表单功能时
	 * 
	 * @param $parent
	 * @param dataSetParam
	 * @param value 可选
	 * @param chartTheme 可选
	 */
	chartForm.renderDataSetParamValueFormInputDate = function($parent, dataSetParam, value, chartTheme)
	{
		var options = chartForm.evalDataSetParamInputPayload(dataSetParam, {});
		options = $.extend(
		{
			format: "Y-m-d",
			timepicker: false,
		},
		options);
		
		var $input = $("<input type='text' class='dg-dspv-form-input' />").attr("name", dataSetParam.name)
			.attr("value", (value || "")).appendTo($parent);
		
		if(chartForm.disableDateAwareInputAutocomplete)
			$input.attr("autocomplete", "off");
		
		if((dataSetParam.required+"") == "true")
			$input.attr("dg-validation-required", "true");
		
		if(chartForm.DataSetParamDataType.NUMBER == dataSetParam.type)
			$input.attr("dg-validation-number", "true");
		
		chartForm.datetimepicker($input, options, chartTheme);
	};
	
	/**
	 * 渲染输入项：时间框
	 * 
	 * dataSetParam.inputPayload格式可以为：
	 * null、空字符串、
	 * 或者
	 * "{ ... }"  //datetimepicker配置选项JSON字符串，数据集定义功能时
	 * 或者
	 * { ... }    //datetimepicker配置选项对象，看板表单功能时
	 * 
	 * @param $parent
	 * @param dataSetParam
	 * @param value 可选
	 * @param chartTheme 可选
	 */
	chartForm.renderDataSetParamValueFormInputTime = function($parent, dataSetParam, value, chartTheme)
	{
		var options = chartForm.evalDataSetParamInputPayload(dataSetParam, {});
		options = $.extend(
		{
			format: "H:i:s",
			datepicker: false,
			step:10,
		},
		options);
		
		var $input = $("<input type='text' class='dg-dspv-form-input' />").attr("name", dataSetParam.name)
			.attr("value", (value || "")).appendTo($parent);
		
		if(chartForm.disableDateAwareInputAutocomplete)
			$input.attr("autocomplete", "off");
		
		if((dataSetParam.required+"") == "true")
			$input.attr("dg-validation-required", "true");
		
		if(chartForm.DataSetParamDataType.NUMBER == dataSetParam.type)
			$input.attr("dg-validation-number", "true");
		
		chartForm.datetimepicker($input, options, chartTheme);
	};
	
	/**
	 * 渲染输入项：日期时间框
	 * 
	 * dataSetParam.inputPayload格式可以为：
	 * null、空字符串、
	 * 或者
	 * "{ ... }"  //datetimepicker配置选项JSON字符串，数据集定义功能时
	 * 或者
	 * { ... }    //datetimepicker配置选项对象，看板表单功能时
	 * 
	 * @param $parent
	 * @param dataSetParam
	 * @param value 可选
	 * @param chartTheme 可选
	 */
	chartForm.renderDataSetParamValueFormInputDateTime = function($parent, dataSetParam, value, chartTheme)
	{
		var options = chartForm.evalDataSetParamInputPayload(dataSetParam, {});
		options = $.extend(
		{
			format: "Y-m-d H:i:s",
			step:10
		},
		options);
		
		var $input = $("<input type='text' class='dg-dspv-form-input' />").attr("name", dataSetParam.name)
			.attr("value", (value || "")).appendTo($parent);
		
		if(chartForm.disableDateAwareInputAutocomplete)
			$input.attr("autocomplete", "off");
		
		if((dataSetParam.required+"") == "true")
			$input.attr("dg-validation-required", "true");
		
		if(chartForm.DataSetParamDataType.NUMBER == dataSetParam.type)
			$input.attr("dg-validation-number", "true");
		
		chartForm.datetimepicker($input, options, chartTheme);
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
	 * @param $parent
	 * @param dataSetParam
	 * @param value 可选
	 * @param chartTheme 可选
	 * @param defaultSelOpts 可选，默认单选框选项集
	 */
	chartForm.renderDataSetParamValueFormInputRadio = function($parent, dataSetParam, value, chartTheme, defaultSelOpts)
	{
		var opts = chartForm.evalDataSetParamInputPayload(dataSetParam, []);
		
		if(defaultSelOpts && opts && opts.length == 0)
			opts = defaultSelOpts;
		
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
			
			var eleId = global.chartFactory.nextElementId();
			
			var $wrapper = $("<div class='dg-dspv-form-radio-wrapper' />").appendTo($inputsWrapper);
			
			var $input = $("<input type='radio' class='dg-dspv-form-input' />")
				.attr("id", eleId).attr("name", dataSetParam.name).attr("value", optVal).appendTo($wrapper);
			
			$("<label />").attr("for", eleId).html(optName).appendTo($wrapper);
			
			if((value+"") == (optVal+""))
				$input.attr("checked", "checked");
			
			if((dataSetParam.required+"") == "true")
				$input.attr("dg-validation-required", "true");
			
			if(chartForm.DataSetParamDataType.NUMBER == dataSetParam.type)
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
	 * @param $parent
	 * @param dataSetParam
	 * @param value 可选，值、值数组
	 * @param chartTheme 可选
	 * @param defaultSelOpts 可选，默认复选框选项集
	 */
	chartForm.renderDataSetParamValueFormInputCheckbox = function($parent, dataSetParam, value, chartTheme, defaultSelOpts)
	{
		var opts = chartForm.evalDataSetParamInputPayload(dataSetParam, []);
		
		if(defaultSelOpts && opts && opts.length == 0)
			opts = defaultSelOpts;
		
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
			
			var eleId = global.chartFactory.nextElementId();
			
			var $wrapper = $("<div class='dg-dspv-form-checkbox-wrapper' />").appendTo($inputsWrapper);
			
			var $input = $("<input type='checkbox' class='dg-dspv-form-input' />")
				.attr("id", eleId).attr("name", dataSetParam.name).attr("value", optVal).appendTo($wrapper);
			
			$("<label />").attr("for", eleId).html(optName).appendTo($wrapper);
			
			if(chartForm.containsValueForString(value, optVal))
				$input.attr("checked", "checked");
			
			if((dataSetParam.required+"") == "true")
				$input.attr("dg-validation-required", "true");
			
			if(chartForm.DataSetParamDataType.NUMBER == dataSetParam.type)
				$input.attr("dg-validation-number", "true");
		}
	};
	
	/**
	 * 渲染输入项：文本域
	 * 
	 * @param $parent
	 * @param dataSetParam
	 * @param value 可选
	 * @param chartTheme 可选
	 */
	chartForm.renderDataSetParamValueFormInputTextarea = function($parent, dataSetParam, value, chartTheme)
	{
		var $input = $("<textarea class='dg-dspv-form-input' />").attr("name", dataSetParam.name)
			.text(value || "").appendTo($parent);
		
		if((dataSetParam.required+"") == "true")
			$input.attr("dg-validation-required", "true");
		
		if(chartForm.DataSetParamDataType.NUMBER == dataSetParam.type)
			$input.attr("dg-validation-number", "true");
	};
	
	chartForm.datetimepicker = function($input, datetimepickerOptions, chartTheme)
	{
		//在这里检查并重写，避免依赖加载顺序
		global.overwriteDateFormatter_parseDate();
		
		if(chartForm._datetimepickerSetLocale !== true)
		{
			$.datetimepicker.setLocale('zh');
			chartForm._datetimepickerSetLocale = true;
		}
		
		if(chartTheme)
		{
			var containerId = (chartTheme._datetimepickerContainerId
					|| (chartTheme._datetimepickerContainerId = global.chartFactory.nextElementId()));
			var container = document.getElementById(containerId);
			if(!container)
				container = $("<div class='dg-dspv-datetimepicker-container' />").attr("id", containerId).appendTo(document.body);
			
			chartForm.datetimepickerSetStyle("#"+containerId, chartTheme);
		}
		
		datetimepickerOptions = $.extend(
		{
			//inline应该为false，为true的话下面的datetimepickerSetStyle函数创建的样式将不起作用
			inline: false,
			parentID: (chartTheme ? "#"+containerId : document.body),
			i18n: chartForm.datetimepickerI18n
		},
		datetimepickerOptions);
		
		//初始化为年份选择器
		var isOnlySelectYear = ("Y" == datetimepickerOptions.format || "y" == datetimepickerOptions.format);
		if(isOnlySelectYear)
		{
			//显示确定按钮，用于直接选中默认年份
			datetimepickerOptions.showApplyButton = true;
			datetimepickerOptions.onGenerate = function(currentValue,$input)
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
						.addClass("xdsoft_save_selected_year ui-button ui-corner-all").html(chartForm.labels.confirm);
				}
			};
			datetimepickerOptions.onShow = function(currentValue,$input)
			{
				if(!$input.val())
				{
					//这样可以直接点击【确定】按钮选择默认年份
					this.setOptions({value: currentValue});
					$input.val("");
				}
			};
			datetimepickerOptions.onChangeYear = function(currentValue,$input)
			{
				this.setOptions({value: currentValue});
				$(".xdsoft_save_selected", this).click();
			};
		}
		
		$input.datetimepicker(datetimepickerOptions);
	};
	
	/**
	 * 创建与指定图表主题匹配的datetimepicker组件样式表。
	 * 
	 * @param parentSelector datetimepicker组件所在的父元素CSS选择器
	 * @param chartTheme
	 */
	chartForm.datetimepickerSetStyle = function(parentSelector, chartTheme)
	{
		var styleId = (chartTheme._datetimepickerStyleSheetId
				|| (chartTheme._datetimepickerStyleSheetId = global.chartFactory.nextElementId()));
		
		if(global.chartFactory.isStyleSheetCreated(styleId))
			return false;
		
		var color = chartFactory.getGradualColor(chartTheme, 1);
		var bgColor = chartFactory.getGradualColor(chartTheme, 0);
		var borderColor = chartFactory.getGradualColor(chartTheme, 0.3);
		var shadowColor = chartFactory.getGradualColor(chartTheme, 0.9);
		var hoverColor = chartFactory.getGradualColor(chartTheme, 0.3);
		
		var cssText =
			//主体
			parentSelector + " .xdsoft_datetimepicker{"
			+"  color: "+color+";"
			+"  background: "+bgColor+";"
			+"  border-color: "+borderColor+";"
			+"  box-shadow: 0px 0px 6px "+shadowColor+";"
			+"  -webkit-box-shadow: 0px 0px 6px "+shadowColor+";"
			+"} "
			//前景色
			+parentSelector + " .xdsoft_datetimepicker .xdsoft_calendar td,"
			+parentSelector + " .xdsoft_datetimepicker .xdsoft_calendar th{"
			+"  color: "+color+";"
			+"} "
			//按钮
			+parentSelector + " .xdsoft_datetimepicker .xdsoft_label i,"
			+parentSelector + " .xdsoft_datetimepicker .xdsoft_next,"
			+parentSelector + " .xdsoft_datetimepicker .xdsoft_prev,"
			+parentSelector + " .xdsoft_datetimepicker .xdsoft_today_button{"
			+"  color:"+color+";"
			+"} "
			//年、月
			+parentSelector + " .xdsoft_datetimepicker .xdsoft_label{"
			+"  background: "+bgColor+";"
			+"} "
			//年、月下拉框
			+parentSelector + " .xdsoft_datetimepicker .xdsoft_label>.xdsoft_select{"
			+"  color: "+color+";"
			+"  background: "+bgColor+";"
			+"  border-color: "+borderColor+";"
			+"  box-shadow: 0px 0px 6px "+shadowColor+";"
			+"  -webkit-box-shadow: 0px 0px 6px "+shadowColor+";"
			+"} "
			//时间框
			+parentSelector + " .xdsoft_datetimepicker .xdsoft_timepicker .xdsoft_time_box{"
			+"  border-color: "+borderColor+";"
			+"} "
			//时间条目
			+parentSelector + " .xdsoft_datetimepicker .xdsoft_timepicker .xdsoft_time_box>div>div{"
			+"  color: "+color+";"
			+"  border-color: "+borderColor+";"
			+"} "
			//悬停
			+parentSelector + " .xdsoft_datetimepicker .xdsoft_calendar td:hover,"
			+parentSelector + " .xdsoft_datetimepicker .xdsoft_timepicker .xdsoft_time_box>div>div:hover,"
			+parentSelector + " .xdsoft_datetimepicker .xdsoft_label>.xdsoft_select>div>.xdsoft_option:hover{"
			+"  color: "+color+" !important;"
			+"  background: "+hoverColor+" !important;"
			+"} "
			//今天
			+parentSelector + " .xdsoft_datetimepicker .xdsoft_calendar td.xdsoft_today{"
			+"  color: "+color+";"
			+"  font-weight: bold;"
			+"} "
			//选中
			+parentSelector + " .xdsoft_datetimepicker .xdsoft_calendar td.xdsoft_default,"
			+parentSelector + " .xdsoft_datetimepicker .xdsoft_calendar td.xdsoft_current,"
			+parentSelector + " .xdsoft_datetimepicker .xdsoft_timepicker .xdsoft_time_box>div>div.xdsoft_current,"
			+parentSelector + " .xdsoft_datetimepicker .xdsoft_label>.xdsoft_select>div>.xdsoft_option.xdsoft_current{"
			+"  color: "+chartTheme.highlightTheme.color+";"
			+"  background: "+chartTheme.highlightTheme.backgroundColor+";"
			+"  box-shadow: none;"
			+"  -webkit-box-shadow: none;"
			+"} "
			+parentSelector + " .xdsoft_datetimepicker .xdsoft_save_selected.xdsoft_save_selected_year{"
			+"  color: "+color+";"
			+"  background: "+bgColor+";"
			+"  border: 1px solid "+borderColor+" !important;"
			+"} "
			+parentSelector + " .xdsoft_datetimepicker .xdsoft_save_selected.xdsoft_save_selected_year:hover{"
			+"  background: "+hoverColor+";"
			+"} "
			;
		
		global.chartFactory.createStyleSheet(styleId, cssText);
		
		return true;
	};
	
	chartForm.evalDataSetParamInputPayload = function(dataSetParam, defaultValue)
	{
		if(typeof(dataSetParam.inputPayload) == "string" && dataSetParam.inputPayload != "")
			return global.chartFactory.evalSilently(dataSetParam.inputPayload, defaultValue);
		else
			return (dataSetParam.inputPayload || defaultValue);
	};
	
	/**
	 * 校验数据集参数值表单的必填项、数值项。
	 * 
	 * @param form
	 * @return true 验证通过；false 验证不通过
	 */
	chartForm.validateDataSetParamValueForm = function(form)
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
				
				if(!val || val == "" || val.length == 0)
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
					
					var val = $(this).attr("value");
					myValid = (val == "" ? true : regexNumber.test(val));
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
					
					myValid = (val[i] == "" ? true : regexNumber.test(val[i]));
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
	chartForm.getDataSetParamValueObj = function(form)
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
			
			if(v == "" || ($.isArray(v) && v.length == 0))
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
	
	chartForm.setDataSetParamValueObj = function(form, paramValueObj)
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
					
					if(chartForm.containsValueForString(value, $this.attr("value")))
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
					
					if(chartForm.containsValueForString(value, $thisOpt.attr("value")))
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
	
	chartForm.containsValueForString = function(array, value)
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
	
	chartForm.getDataSetParamValueForm = function($parent)
	{
		return $(".dg-dspv-form", $parent);
	};

	chartForm.getDataSetParamValueFormHead = function(form)
	{
		return $(".dg-dspv-form-head", form);
	};
	
	chartForm.getDataSetParamValueFormContent = function(form)
	{
		return $(".dg-dspv-form-content", form);
	};
	
	chartForm.getDataSetParamValueFormFoot = function(form)
	{
		return $(".dg-dspv-form-foot", form);
	};
	
	chartForm.bindChartSettingPanelEvent = function(chart)
	{
		if(!chart.hasParamDataSet())
			return false;
		
		var $chart = chart.elementJquery();
		
		if(!$chart.attr("bindChartSettingPanelEvent"))
		{
			$chart.attr("bindChartSettingPanelEvent", "1");
			
			var mouseenterHandler = function(event)
			{
				if(!chart.statusPreRender() && !chart.statusRendering())
					chartForm.showChartSettingBox(chart);
			};
			var mouseleaveHandler = function(event)
			{
				if(chartForm.isChartSettingPanelClosed(chart))
					chartForm.hideChartSettingBox(chart);
			};
			
			$chart.mouseenter(mouseenterHandler).mouseleave(mouseleaveHandler);
			
			$chart.data("chartSettingPanel-mouseenterHandler", mouseenterHandler);
			$chart.data("chartSettingPanel-mouseleaveHandler", mouseleaveHandler);
		}
		
		if(!chart.isDataSetParamValueReady())
			chartForm.showChartSettingBox(chart);
		
		return true;
	};
	
	chartForm.unbindChartSettingPanelEvent = function(chart)
	{
		var $chart = chart.elementJquery();
		var mouseenterHandler = $chart.data("chartSettingPanel-mouseenterHandler");
		var mouseleaveHandler = $chart.data("chartSettingPanel-mouseleaveHandler");
		
		$chart.removeAttr("bindChartSettingPanelEvent");
		if(mouseenterHandler)
			$chart.off("mouseenter", mouseenterHandler);
		if(mouseleaveHandler)
			$chart.off("mouseleave", mouseleaveHandler);
		
		$(".dg-chart-setting-box", $chart).remove();
	};
	
	chartForm.showChartSettingBox = function(chart)
	{
		var $chart = chart.elementJquery();
		var $box = $(".dg-chart-setting-box", $chart);
		
		if($box.length <= 0)
		{
			$box = $("<div class='dg-chart-setting-box' />").appendTo($chart);
			
			chartForm.setChartSettingBoxStyle($box, chart.theme());
			
			var $button = $("<button type='button' class='dg-chart-setting-button' />")
					.html(chartForm.labels.set).attr("title", chartForm.labels.openOrCloseSetPanel).appendTo($box);
			
			$button.click(function()
			{
				if(chartForm.isChartSettingPanelClosed(chart))
					chartForm.openChartSettingPanel($box, chart);
				else
					chartForm.closeChartSettingPanel(chart);
			});
			
			$chart.click(function(event)
			{
				if(!chartForm.isChartSettingPanelClosed(chart))
				{
					if($(event.target).closest(".dg-chart-setting-box").length == 0)
						chartForm.closeChartSettingPanel(chart);
				}
			});
		}
		
		$box.show();
	};
	
	chartForm.hideChartSettingBox = function(chart)
	{
		$(".dg-chart-setting-box", chart.elementJquery()).hide();
	};
	
	chartForm.setChartSettingBoxStyle = function($box, chartTheme)
	{
		var styleClassName = chartTheme._chartSettingBoxStyleClassName;
		if(!styleClassName)
		{
			styleClassName = global.chartFactory.nextElementId();
			chartTheme._chartSettingBoxStyleClassName = styleClassName;
		}
		
		$box.addClass(styleClassName);
		
		var styleId = (chartTheme._chartSettingBoxStyleSheetId
				|| (chartTheme._chartSettingBoxStyleSheetId = global.chartFactory.nextElementId()));
		
		if(global.chartFactory.isStyleSheetCreated(styleId))
			return false;
		
		var qualifier = "." + styleClassName;
		
		var color = chartFactory.getGradualColor(chartTheme, 1);
		var bgColor = chartFactory.getGradualColor(chartTheme, 0);
		var borderColor = chartFactory.getGradualColor(chartTheme, 0.5);
		var shadowColor = chartFactory.getGradualColor(chartTheme, 0.9);
		
		var cssText =
			qualifier + ".dg-chart-setting-box .dg-chart-setting-button{"
			+"  color: "+color+";"
			+"  background: "+bgColor+";"
			+"  border-color: "+borderColor+";"
			+"} "
			+qualifier + ".dg-chart-setting-box .dg-chart-setting-panel{"
			+"  color: "+color+";"
			+"  background: "+bgColor+";"
			+"  border-color: "+borderColor+";"
			+"  box-shadow: 0px 0px 6px "+shadowColor+";"
			+"  -webkit-box-shadow: 0px 0px 6px "+shadowColor+";"
			+"} "
			+qualifier + ".dg-chart-setting-box .dg-chart-setting-panel .dg-param-value-form-wrapper{"
			+"  color: "+color+";"
			+"  background: "+bgColor+";"
			+"  border-color: "+borderColor+";"
			+"} "
			+qualifier + ".dg-chart-setting-box .dg-chart-setting-panel .dg-chart-setting-panel-foot button{"
			+"  color: "+color+";"
			+"  background: "+chartFactory.getGradualColor(chartTheme, 0.1)+";"
			+"  border-color: "+borderColor+";"
			+"} "
			+qualifier + ".dg-chart-setting-box .dg-chart-setting-panel .dg-chart-setting-panel-foot button:hover{"
			+"  background: "+chartFactory.getGradualColor(chartTheme, 0.3)+";"
			+"} "
			;
		
		global.chartFactory.createStyleSheet(styleId, cssText);
		
		return true;
	};
	
	/**
	 * 打开图表设置面板。
	 */
	chartForm.openChartSettingPanel = function($box, chart)
	{
		var $chart = chart.elementJquery();
		
		var chartDataSets = chart.chartDataSetsNonNull();
		
		var $panel = $(".dg-chart-setting-panel", $box);
		
		if($panel.length <= 0)
		{
			$panel = $("<div class='dg-chart-setting-panel' />").appendTo($box);
			
			var $panelHead = $("<div class='dg-chart-setting-panel-head' />").html(chartForm.labels.setDataSetParamValue).appendTo($panel);
			var $panelContent = $("<div class='dg-chart-setting-panel-content' />").appendTo($panel);
			var $panelFoot = $("<div class='dg-chart-setting-panel-foot' />").appendTo($panel);
			
			for(var i=0; i<chartDataSets.length; i++)
			{
				var params = chartDataSets[i].dataSet.params;
				
				if(!params || params.length == 0)
					continue;
				
				var formTitle = (chartDataSets.length > 1 ? (i+1)+". " : "") + chart.chartDataSetName(chartDataSets[i]);
				if(formTitle != chartDataSets[i].dataSet.name)
					formTitle += " ("+chartDataSets[i].dataSet.name+")";
				
				var $fp = $("<div class='dg-param-value-form-wrapper' />").data("chartDataSetIndex", i).appendTo($panelContent);
				var $head = $("<div class='dg-param-value-form-head' />").html(formTitle).appendTo($fp);
				var $content = $("<div class='dg-param-value-form-content' />").appendTo($fp);
				chartForm.renderDataSetParamValueForm($content, params,
				{
					chartTheme: chart.theme(),
					submit: function()
					{
						$("button", $panelFoot).click();
					},
					paramValues: chartDataSets[i].paramValues,
					render: function()
					{
						chartForm.getDataSetParamValueFormFoot(this).hide();
					}
				});
			}
			
			var $button = $("<button type='button' />").html(chartForm.labels.confirm).appendTo($panelFoot);
			$button.click(function()
			{
				var $thisButton = $(this);
				var validateOk = true;
				var paramValuess = [];
				
				$(".dg-param-value-form-wrapper", $panelContent).each(function()
				{
					if(!validateOk)
						return;
					
					var $this = $(this);
					
					var $form = chartForm.getDataSetParamValueForm($this);
					
					if(!chartForm.validateDataSetParamValueForm($form))
						validateOk = false;
					else
					{
						var myIndex = $this.data("chartDataSetIndex");
						var myParamValues = chartForm.getDataSetParamValueObj($form);
						paramValuess.push({ index : myIndex, paramValues: myParamValues });
					}
				});
				
				if(validateOk)
				{
					$thisButton.removeClass("dg-param-value-form-invalid");
					
					for(var i=0; i<paramValuess.length; i++)
						chart.setDataSetParamValues(paramValuess[i].index, paramValuess[i].paramValues);
					
					chartForm.closeChartSettingPanel(chart);
					chart.refreshData();
				}
				else
					$thisButton.addClass("dg-param-value-form-invalid");
			});
			
			$panelContent.css("width", $chart.width()*3/4);
			$panelContent.css("max-height", $chart.height()*3/4 - $panelHead.outerHeight(true) - $panelFoot.outerHeight(true));
		}
		else
		{
			$(".dg-param-value-form-wrapper", $panel).each(function()
			{
				var chartDataSetIndex = $(this).data("chartDataSetIndex");
				var $form = chartForm.getDataSetParamValueForm(this);
				
				chartForm.setDataSetParamValueObj($form, chartDataSets[chartDataSetIndex].paramValues);
			});
		}
		
		$panel.show();
	};
	
	/**
	 * 关闭图表设置面板。
	 */
	chartForm.closeChartSettingPanel = function(chart)
	{
		$(".dg-chart-setting-panel", chart.elementJquery()).hide();
	};
	
	/**
	 * 获取图表设置面板。
	 */
	chartForm.getChartSettingPanel = function(chart)
	{
		return $(".dg-chart-setting-panel", chart.elementJquery());
	};
	
	chartForm.isChartSettingPanelClosed = function(chart)
	{
		var $panel = $(".dg-chart-setting-panel", chart.elementJquery());
		
		return ($panel.length == 0 || $panel.is(":hidden"));
	};
})
(this);

(function(global)
{
	global.overwriteDateFormatter_parseDate = function()
	{
		if(global._overwriteDateFormatter_parseDate == true)
			return;
		
		global._overwriteDateFormatter_parseDate = true;
		
		//重写datetimepicker的DateFormatter.prototype.parseDate函数，
		//解决当options配置为仅选择年份（{format:'Y'}）时，选择完毕后输入框blur后，年份会被重置的的BUG
		DateFormatter.prototype.parseDate = function(e, r) {
	        var n, a, u, i, s, o, c, f, l, h, d = this, g = !1, m = !1, p = d.dateSettings, y = {
	            date: null,
	            year: null,
	            month: null,
	            day: null,
	            hour: 0,
	            min: 0,
	            sec: 0
	        };
	        if (!e)
	            return null;
	        if (e instanceof Date)
	            return e;
	        if ("U" === r)
	            return u = parseInt(e),
	            u ? new Date(1e3 * u) : e;
	        switch (typeof e) {
	        case "number":
	            return new Date(e);
	        case "string":
	            break;
	        default:
	            return null
	        }
	        if (n = r.match(d.validParts),
	        !n || 0 === n.length)
	            throw new Error("Invalid date format definition.");
	        for (a = e.replace(d.separators, "\x00").split("\x00"),
	        u = 0; u < a.length; u++)
	            switch (i = a[u],
	            s = parseInt(i),
	            n[u]) {
	            case "y":
	            case "Y":
	                if (!s)
	                    return null;
	                l = i.length,
	                y.year = 2 === l ? parseInt((70 > s ? "20" : "19") + i) : s,
	                g = !0;
	                break;
	            case "m":
	            case "n":
	            case "M":
	            case "F":
	                if (isNaN(s)) {
	                    if (o = d.getMonth(i),
	                    !(o > 0))
	                        return null;
	                    y.month = o
	                } else {
	                    if (!(s >= 1 && 12 >= s))
	                        return null;
	                    y.month = s
	                }
	                g = !0;
	                break;
	            case "d":
	            case "j":
	                if (!(s >= 1 && 31 >= s))
	                    return null;
	                y.day = s,
	                g = !0;
	                break;
	            case "g":
	            case "h":
	                if (c = n.indexOf("a") > -1 ? n.indexOf("a") : n.indexOf("A") > -1 ? n.indexOf("A") : -1,
	                h = a[c],
	                c > -1)
	                    f = t(h, p.meridiem[0]) ? 0 : t(h, p.meridiem[1]) ? 12 : -1,
	                    s >= 1 && 12 >= s && f > -1 ? y.hour = s + f - 1 : s >= 0 && 23 >= s && (y.hour = s);
	                else {
	                    if (!(s >= 0 && 23 >= s))
	                        return null;
	                    y.hour = s
	                }
	                m = !0;
	                break;
	            case "G":
	            case "H":
	                if (!(s >= 0 && 23 >= s))
	                    return null;
	                y.hour = s,
	                m = !0;
	                break;
	            case "i":
	                if (!(s >= 0 && 59 >= s))
	                    return null;
	                y.min = s,
	                m = !0;
	                break;
	            case "s":
	                if (!(s >= 0 && 59 >= s))
	                    return null;
	                y.sec = s,
	                m = !0
	            }
	        
	        //----添加的内容
	        //如果选择了年份，检查并设置月份、日的初值，使下面的：
	        //if (g === !0 && y.year && y.month && y.day)
	        //逻辑可以执行到
	        if (g === !0 && y.year)
	        {
	        	if(!y.month)
	        		y.month = 1;
	        	if(!y.day)
	        		y.day = 1;
	        }
	        //----添加的内容
	        
	        if (g === !0 && y.year && y.month && y.day)
	            y.date = new Date(y.year,y.month - 1,y.day,y.hour,y.min,y.sec,0);
	        else {
	            if (m !== !0)
	                return null;
	            y.date = new Date(0,0,0,y.hour,y.min,y.sec,0)
	        }
	        return y.date
	    };
	}
})
(this);
/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
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
	
	/**
	 * 渲染数据集参数值表单。
	 * 
	 * @param $parent
	 * @param dataSetParams
	 * @param options
	 * 			{
	 * 				submit: function(){},    	//可选，提交处理函数
	 * 				paramValues: {...},     	//可选，初始参数值
	 * 				readonly: false,			//可选，是否只读
	 * 				submitText: "...",       	//可选，提交按钮文本内容
	 * 				yesText: "...",       		//可选，"是"选项文本内容
	 * 				noText: "...",       		//可选，"否"选项文本内容
	 * 				render: function(){}		//可选，渲染后回调函数
	 * 			}
	 * @return 表单DOM元素
	 */
	chartForm.renderDataSetParamValueForm = function($parent, dataSetParams, options)
	{
		options = $.extend({ submitText: chartForm.labels.confirm, readonly: false, yesText: chartForm.labels.yes, noText: chartForm.labels.no }, (options || {}));
		var paramValues = (options.paramValues || {});
		var InputType = chartForm.DataSetParamInputType;
		
		$parent.empty();
		var $form = $("<form />").appendTo($parent);
		
		$form.addClass("dg-dspv-form");
		
		$("<div class='dg-dspv-form-head' />").appendTo($form);
		var $content = $("<div class='dg-dspv-form-content' />").appendTo($form);
		var $foot = $("<div class='dg-dspv-form-foot' />").appendTo($form);
		
		for(var i=0; i<dataSetParams.length; i++)
		{
			var dsp = dataSetParams[i];
			var value = paramValues[dsp.name];
			
			var $item = $("<div class='dg-dspv-form-item' />").appendTo($content);
			
			var $labelDiv = $("<div class='dg-dspv-form-item-label' />").appendTo($item);
			var $label = $("<label />").html(dsp.name+chartForm.labels.colon).appendTo($labelDiv);
			
			if(dsp.desc)
				$label.attr("title", dsp.desc);
			
			var $valueDiv = $("<div class='dg-dspv-form-item-value' />").appendTo($item);
			
			if(dsp.type == chartForm.DataSetParamDataType.BOOLEAN)
			{
				if(!dsp.inputPayload)
					dsp.inputPayload = [ { name: options.yesText, value: "true" }, { name: options.noText, value: "false" } ];
				
				if(dsp.inputType == InputType.RADIO)
					chartForm.renderDataSetParamValueFormInputRadio($valueDiv, dsp, value);
				else if(dsp.inputType == InputType.CHECKBOX)
					chartForm.renderDataSetParamValueFormInputCheckbox($valueDiv, dsp, value);
				else
					chartForm.renderDataSetParamValueFormInputSelect($valueDiv, dsp, value);
			}
			else if(dsp.type == chartForm.DataSetParamDataType.STRING)
			{
				if(dsp.inputType == InputType.SELECT)
					chartForm.renderDataSetParamValueFormInputSelect($valueDiv, dsp, value);
				else if(dsp.inputType == InputType.DATE)
					chartForm.renderDataSetParamValueFormInputDate($valueDiv, dsp, value);
				else if(dsp.inputType == InputType.TIME)
					chartForm.renderDataSetParamValueFormInputTime($valueDiv, dsp, value);
				else if(dsp.inputType == InputType.DATETIME)
					chartForm.renderDataSetParamValueFormInputDateTime($valueDiv, dsp, value);
				else if(dsp.inputType == InputType.RADIO)
					chartForm.renderDataSetParamValueFormInputRadio($valueDiv, dsp, value);
				else if(dsp.inputType == InputType.CHECKBOX)
					chartForm.renderDataSetParamValueFormInputCheckbox($valueDiv, dsp, value);
				else if(dsp.inputType == InputType.TEXTAREA)
					chartForm.renderDataSetParamValueFormInputTextarea($valueDiv, dsp, value);
				else
					chartForm.renderDataSetParamValueFormInputText($valueDiv, dsp, value);
			}
			else if(dsp.type == chartForm.DataSetParamDataType.NUMBER)
			{
				if(dsp.inputType == InputType.SELECT)
					chartForm.renderDataSetParamValueFormInputSelect($valueDiv, dsp, value);
				else if(dsp.inputType == InputType.RADIO)
					chartForm.renderDataSetParamValueFormInputRadio($valueDiv, dsp, value);
				else if(dsp.inputType == InputType.CHECKBOX)
					chartForm.renderDataSetParamValueFormInputCheckbox($valueDiv, dsp, value);
				else if(dsp.inputType == InputType.TEXTAREA)
					chartForm.renderDataSetParamValueFormInputTextarea($valueDiv, dsp, value);
				else
					chartForm.renderDataSetParamValueFormInputText($valueDiv, dsp, value);
			}
		}
		
		if(!options.readonly)
			$("<button type='submit' />").html(options.submitText).appendTo($foot);
		
		$form.submit(function()
		{
			if(options.readonly)
				return false;
			
			var validationOk = chartForm.validateDataSetParamValueForm(this);
			
			if(!validationOk)
				return false;
			
			if(options.submit)
				return (options.submit.apply(this) == true);
			else
				return false;
		});
		
		if(options.render)
			options.render.apply($form[0]);
		
		return $form[0];
	};
	
	//渲染输入项：文本框
	chartForm.renderDataSetParamValueFormInputText = function($parent, dataSetParam, value)
	{
		var $input = $("<input type='text' class='dg-dspv-form-input' />").attr("name", dataSetParam.name)
			.attr("value", (value || "")).appendTo($parent);
		
		if((dataSetParam.required+"") == "true")
			$input.attr("dg-validation-required", "true");
		
		if(chartForm.DataSetParamDataType.NUMBER == dataSetParam.type)
			$input.attr("dg-validation-number", "true");
	};
	
	//渲染输入项：下拉框
	//dataSetParam.inputPayload格式应该为：[ {name: '...', value: ...}, ... ]
	chartForm.renderDataSetParamValueFormInputSelect = function($parent, dataSetParam, value)
	{
		var options = (dataSetParam.inputPayload ? global.chartFactory.evalSilently(dataSetParam.inputPayload, []) : []);
		
		var $input = $("<select class='dg-dspv-form-input' />").attr("name", dataSetParam.name).appendTo($parent);
		
		for(var i=0; i<options.length; i++)
		{
			var $opt = $("<option />").attr("value", options[i].value).html(options[i].name).appendTo($input);
			
			if(value == options[i].value)
				$opt.attr("selected", "selected");
		}
		
		if((dataSetParam.required+"") == "true")
			$input.attr("dg-validation-required", "true");
		
		if(chartForm.DataSetParamDataType.NUMBER == dataSetParam.type)
			$input.attr("dg-validation-number", "true");
	};
	
	//渲染输入项：日期框
	chartForm.renderDataSetParamValueFormInputDate = function($parent, dataSetParam, value)
	{
		var $input = $("<input type='text' class='dg-dspv-form-input' />").attr("name", dataSetParam.name)
			.attr("value", (value || "")).appendTo($parent);
		
		if((dataSetParam.required+"") == "true")
			$input.attr("dg-validation-required", "true");
		
		if(chartForm.DataSetParamDataType.NUMBER == dataSetParam.type)
			$input.attr("dg-validation-number", "true");
	};
	
	//渲染输入项：时间框
	chartForm.renderDataSetParamValueFormInputTime = function($parent, dataSetParam, value)
	{
		var $input = $("<input type='text' class='dg-dspv-form-input' />").attr("name", dataSetParam.name)
			.attr("value", (value || "")).appendTo($parent);
		
		if((dataSetParam.required+"") == "true")
			$input.attr("dg-validation-required", "true");
		
		if(chartForm.DataSetParamDataType.NUMBER == dataSetParam.type)
			$input.attr("dg-validation-number", "true");
	};
	
	//渲染输入项：日期时间框
	chartForm.renderDataSetParamValueFormInputDateTime = function($parent, dataSetParam, value)
	{
		var $input = $("<input type='text' class='dg-dspv-form-input' />").attr("name", dataSetParam.name)
			.attr("value", (value || "")).appendTo($parent);
		
		if((dataSetParam.required+"") == "true")
			$input.attr("dg-validation-required", "true");
		
		if(chartForm.DataSetParamDataType.NUMBER == dataSetParam.type)
			$input.attr("dg-validation-number", "true");
	};
	
	//渲染输入项：单选框
	//dataSetParam.inputPayload格式应该为：[ {name: '...', value: ...}, ... ]
	chartForm.renderDataSetParamValueFormInputRadio = function($parent, dataSetParam, value)
	{
		var options = (dataSetParam.inputPayload ? global.chartFactory.evalSilently(dataSetParam.inputPayload, []) : []);
		
		for(var i=0; i<options.length; i++)
		{
			var eleId = chartForm.nextElementId();
			
			var $input = $("<input type='radio' class='dg-dspv-form-input' />")
				.attr("id", eleId).attr("name", dataSetParam.name).attr("value", options[i].value).appendTo($parent);
			
			$("<label />").attr("for", eleId).html(options[i].name).appendTo($parent);
			
			if(value == options[i].value)
				$input.attr("checked", "checked");
			
			if((dataSetParam.required+"") == "true")
				$input.attr("dg-validation-required", "true");
			
			if(chartForm.DataSetParamDataType.NUMBER == dataSetParam.type)
				$input.attr("dg-validation-number", "true");
		}
	};
	
	//渲染输入项：复选框
	//dataSetParam.inputPayload格式应该为：[ {name: '...', value: ...}, ... ]
	chartForm.renderDataSetParamValueFormInputCheckbox = function($parent, dataSetParam, value)
	{
		var options = (dataSetParam.inputPayload ? global.chartFactory.evalSilently(dataSetParam.inputPayload, []) : []);
		
		for(var i=0; i<options.length; i++)
		{
			var eleId = chartForm.nextElementId();
			
			var $input = $("<input type='checkbox' class='dg-dspv-form-input' />")
				.attr("id", eleId).attr("name", dataSetParam.name).attr("value", options[i].value).appendTo($parent);
			
			$("<label />").attr("for", eleId).html(options[i].name).appendTo($parent);
			
			if(value == options[i].value)
				$input.attr("checked", "checked");
			
			if((dataSetParam.required+"") == "true")
				$input.attr("dg-validation-required", "true");
			
			if(chartForm.DataSetParamDataType.NUMBER == dataSetParam.type)
				$input.attr("dg-validation-number", "true");
		}
	};
	
	//渲染输入项：文本域
	chartForm.renderDataSetParamValueFormInputTextarea = function($parent, dataSetParam, value)
	{
		var $input = $("<textarea type='text' class='dg-dspv-form-input' />").attr("name", dataSetParam.name).appendTo($parent);
		$input.val(value || "");
		
		if((dataSetParam.required+"") == "true")
			$input.attr("dg-validation-required", "true");
		
		if(chartForm.DataSetParamDataType.NUMBER == dataSetParam.type)
			$input.attr("dg-validation-number", "true");
	};
	
	chartForm.nextElementId = function()
	{
		var nextIdSeq = (chartForm._nextElementIdSeq != null ? chartForm._nextElementIdSeq : 0);
		chartForm._nextElementIdSeq = nextIdSeq + 1;
		
		return "dg-chartFormEleId-" + nextIdSeq;
	};
	
	/**
	 * 验证数据集参数值表单。
	 * 
	 * @param form
	 * @return true 验证通过；false 验证不通过
	 */
	chartForm.validateDataSetParamValueForm = function(form)
	{
		var validationOk = true;
		
		var $requireds = $("[dg-validation-required]", form);
		$requireds.each(function()
		{
			var $this = $(this);
			if($this.val() == "")
			{
				$this.addClass("dg-validation-required");
				validationOk = false;
			}
			else
				$this.removeClass("dg-validation-required");
		});
		
		var $numbers = $("[dg-validation-number]", form);
		var regexNumber = /^-?\d+\.?\d*$/;
		$numbers.each(function()
		{
			var $this = $(this);
			var val = $this.val();
			var valid = (val == "" ? true : regexNumber.test(val));
			
			if(!valid)
			{
				$this.addClass("dg-validation-number");
				validationOk = false;
			}
			else
				$this.removeClass("dg-validation-number");
		});
		
		return validationOk;
	};
	
	chartForm.getDataSetParamValueObj = function(form)
	{
		var $form = $(form);

		var array = $form.serializeArray();
		
		var re = {};
		
		$(array).each(function()
		{
			var name = this.name;
			var value = this.value;
			
			if(!value)
				return;
			
			if(re[name] == undefined)
				re[name] = value;
			else
			{
				var prev = re[name];
				
				if($.isArray(prev))
					prev.push(value);
				else
				{
					prev = [ prev ];
					re[name] = prev;
					prev.push(value);
				}
			}
		});
		
		return re;
	};

	chartForm.setDataSetParamValueObj = function(form, paramValueObj)
	{
		paramValueObj = (paramValueObj || {});
		
		$(".dg-dspv-form-input", form).each(function()
		{
			var $this = $(this);
			var name = $this.attr("name");
			
			if(name)
			{
				var value = (paramValueObj[name] || "");
				$this.val(value+"");
			}
		});
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
			var $button = $("<button type='button' class='dg-chart-setting-button' />")
					.html(chartForm.labels.set).attr("title", chartForm.labels.openOrCloseSetPanel).appendTo($box);
			chartForm.setWidgetStyle($button, chart);
			
			$button.click(function()
			{
				if(chartForm.isChartSettingPanelClosed(chart))
					chartForm.openChartSettingPanel(chart, $box);
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
	
	/**
	 * 打开图表设置面板。
	 */
	chartForm.openChartSettingPanel = function(chart, $parent)
	{
		var $chart = chart.elementJquery();
		$parent = ($parent || $chart);
		
		var chartDataSets = chart.chartDataSetsNonNull();
		
		var $panel = $(".dg-chart-setting-panel", $parent);
		
		if($panel.length <= 0)
		{
			$panel = $("<div class='dg-chart-setting-panel' />").appendTo($parent);
			chartForm.setWidgetStyle($panel, chart, {shadow:true});
			
			var $panelHead = $("<div class='dg-chart-setting-panel-head' />").html(chartForm.labels.setDataSetParamValue).appendTo($panel);
			var $panelContent = $("<div class='dg-chart-setting-panel-content' />").css("max-height", parseInt($chart.height()/2)).appendTo($panel);
			var $panelFoot = $("<div class='dg-chart-setting-panel-foot' />").appendTo($panel);
			
			for(var i=0; i<chartDataSets.length; i++)
			{
				var params = chartDataSets[i].dataSet.params;
				
				if(!params || params.length == 0)
					continue;
				
				var formTitle = chart.chartDataSetName(chartDataSets[i]);
				if(formTitle != chartDataSets[i].dataSet.name)
					formTitle += " ("+chartDataSets[i].dataSet.name+")";
				
				var $fp = $("<div class='dg-param-value-form-wrapper' />").data("chartDataSetIndex", i).appendTo($panelContent);
				chartForm.setWidgetStyle($fp, chart);
				var $head = $("<div class='dg-param-value-form-head' />").html(formTitle).appendTo($fp);
				var $content = $("<div class='dg-param-value-form-content' />").appendTo($fp);
				chartForm.renderDataSetParamValueForm($content, params,
				{
					submit: function()
					{
						$("button", $panelFoot).click();
					},
					paramValues: chartDataSets[i].paramValues,
					render: function()
					{
						$("input, select, button, textarea", this).each(function()
						{
							chartForm.setWidgetStyle($(this), chart);
						});
						
						chartForm.getDataSetParamValueFormFoot(this).hide();
					}
				});
			}
			
			var $button = $("<button type='button' />").html(chartForm.labels.confirm).appendTo($panelFoot);
			chartForm.setPrimaryButtonStyle($button, chart);
			$button.click(function()
			{
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
					for(var i=0; i<paramValuess.length; i++)
						chart.setDataSetParamValues(paramValuess[i].index, paramValuess[i].paramValues);
					
					chartForm.closeChartSettingPanel(chart);
					chart.refreshData();
				}
			});
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
	
	chartForm.setWidgetStyle = function($widget, chart, options)
	{
		options = (options || { shadow: false });
		
		var chartFactory = global.chartFactory;
		var chartTheme = chart.theme();
		
		var color = chartFactory.getGradualColor(chartTheme, 1);
		var bgColor = chartFactory.getGradualColor(chartTheme, 0);
		var borderColor = chartFactory.getGradualColor(chartTheme, 0.4);
		var shadowColor = chartFactory.getGradualColor(chartTheme, 0.9);
		
		$widget.css("color", color);
		$widget.css("background-color", bgColor);
		$widget.css("border-color", borderColor);
		
		if(options.shadow)
		{
			$widget.css("box-shadow", "0px 0px 6px "+shadowColor);
			$widget.css("-webkit-box-shadow", "0px 0px 6px "+shadowColor);
		}
	};
	
	chartForm.setPrimaryButtonStyle = function($button, chart)
	{
		var chartFactory = global.chartFactory;
		var chartTheme = chart.theme();
		
		var color = chartFactory.getGradualColor(chartTheme, 1);
		var bgColor = chartFactory.getGradualColor(chartTheme, 0.2);
		var borderColor = chartFactory.getGradualColor(chartTheme, 0.5);
		
		$button.css("color", color);
		$button.css("background-color", bgColor);
		$button.css("border-color", borderColor);
	};
})
(this);
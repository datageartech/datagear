/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 图表表单库。
 * 全局变量名：window.chartForm。
 * 
 * 加载时依赖：
 *   无
 * 
 * 运行时依赖:
 *   jquery.js
 */
(function(global)
{
	var chartForm = (global.chartForm || (global.chartForm = {}));
	
	//org.datagear.analysis.DataSetParam.DataType
	chartForm.DataSetParamDataType =
	{
		STRING: "STRING",
		BOOLEAN: "BOOLEAN",
		NUMBER: "NUMBER"
	};
	
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
		options = $.extend({ submitText: "确定", readonly: false, yesText: "是", noText: "否" }, (options || {}));
		var paramValues = (options.paramValues || {});
		
		$parent.empty();
		var $form = $("<form />").appendTo($parent);
		
		$form.addClass("data-set-param-value-form");
		
		$("<div class='dspv-head' />").appendTo($form);
		var $content = $("<div class='dspv-content' />").appendTo($form);
		var $foot = $("<div class='dspv-foot' />").appendTo($form);
		
		for(var i=0; i<dataSetParams.length; i++)
		{
			var dsp = dataSetParams[i];
			
			var $item = $("<div class='form-item' />").appendTo($content);
			
			var $labelDiv = $("<div class='form-item-label' />").appendTo($item);
			var $label = $("<label />").html(dsp.name).appendTo($labelDiv);
			
			if(dsp.desc)
				$label.attr("title", dsp.desc);
			
			var $valueDiv = $("<div class='form-item-value' />").appendTo($item);
			
			var $input;
			
			if(chartForm.DataSetParamDataType.BOOLEAN == dsp.type)
			{
				$input = $("<select class='form-input ui-widget ui-widget-content' />").attr("name", dsp.name).appendTo($valueDiv);
				var $optNull = $("<option value='' />").html("").appendTo($input);
				var $optTrue = $("<option value='true' />").html(options.yesText).appendTo($input);
				var $optFalse = $("<option value='false' />").html(options.noText).appendTo($input);
				
				var value = paramValues[dsp.name];
				var $optSelected = (!value ? $optNull : ((value+"") == "false" ? $optFalse : $optTrue));
				$optSelected.attr("selected", "selected");
			}
			else
			{
				$input = $("<input type='text' class='form-input ui-widget ui-widget-content' />").attr("name", dsp.name)
								.attr("value", (paramValues[dsp.name] || "")).appendTo($valueDiv);
				
				if(chartForm.DataSetParamDataType.NUMBER == dsp.type)
					$input.attr("validation-number", "true");
			}
			
			if((dsp.required+"") == "true")
				$input.attr("validation-required", "true");
		}
		
		if(!options.readonly)
			$("<button type='submit' class='ui-button ui-corner-all ui-widget' />").html(options.submitText).appendTo($foot);
		
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
	
	/**
	 * 验证数据集参数值表单。
	 * 
	 * @param form
	 * @return true 验证通过；false 验证不通过
	 */
	chartForm.validateDataSetParamValueForm = function(form)
	{
		var validationOk = true;
		
		var $requireds = $("[validation-required]", form);
		$requireds.each(function()
		{
			var $this = $(this);
			if($this.val() == "")
			{
				$this.addClass("validation-required");
				validationOk = false;
			}
			else
				$this.removeClass("validation-required");
		});
		
		var $numbers = $("[validation-number]", form);
		var regexNumber = /^-?\d+\.?\d*$/;
		$numbers.each(function()
		{
			var $this = $(this);
			var val = $this.val();
			var valid = (val == "" ? true : regexNumber.test(val));
			
			if(!valid)
			{
				$this.addClass("validation-number");
				validationOk = false;
			}
			else
				$this.removeClass("validation-number");
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
	
	chartForm.getDataSetParamValueForm = function($parent)
	{
		return $(".data-set-param-value-form", $parent);
	};
	
	chartForm.getDataSetParamValueFormFoot = function(form)
	{
		return $(".dspv-foot", form);
	};
	
	chartForm.deleteEmptyDataSetParamValue = function(paramValueObj)
	{
		if(!paramValueObj)
			return paramValueObj;
		
		var re = {};
		
		for(var name in paramValueObj)
		{
			if(paramValueObj[name] == "")
				continue;
			
			re[name] = paramValueObj[name];
		}
		
		return re;
	};
	
	chartForm.bindChartSettingPanelEvent = function(chart)
	{
		var hasParam = false;
		var chartDataSets = chart.chartDataSetsNonNull();
		for(var i=0; i<chartDataSets.length; i++)
		{
			var params = chartDataSets[i].dataSet.params;
			hasParam = (params && params.length > 0);
			
			if(hasParam)
				break;
		}
		
		if(!hasParam)
			return false;
		
		chart.elementJquery().hover(
		function(event)
		{
			if(!chart.statusPreRender() || !chart.statusRendering())
				chartForm.showChartSettingBox(chart);
		},
		function(event)
		{
			chartForm.hideChartSettingBox(chart);
		});
		
		return true;
	};
	
	chartForm.showChartSettingBox = function(chart)
	{
		var $chart = chart.elementJquery();
		var $box = $(".dg-chart-setting-box", $chart);
		
		if($box.length <= 0)
		{
			$box = $("<div class='dg-chart-setting-box' />").appendTo($chart);
			var $button = $("<button type='button' class='dg-chart-setting-button ui-button ui-corner-all ui-widget' />").html("设置").appendTo($box);
			chartFactory.setTooltipThemeStyle($button, chart);
			
			$button.click(function()
			{
				chartForm.openChartSettingPanel(chart, $box);
			});
			
			$box.hover(function(){}, function()
			{
				chartForm.closeChartSettingPanel(chart);
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
		
		var $panel = $(".dg-chart-setting-panel", $parent);
		
		if($panel.length <= 0)
		{
			$panel = $("<div class='dg-chart-setting-panel' />").appendTo($parent);
			chartFactory.setTooltipThemeStyle($panel, chart);
			
			var $panelHead = $("<div class='dg-chart-setting-panel-head' />").html("设置图表参数值").appendTo($panel);
			var $panelContent = $("<div class='dg-chart-setting-panel-content' />").css("max-height", parseInt($(window).height()/2)).appendTo($panel);
			var $panelFoot = $("<div class='dg-chart-setting-panel-foot' />").appendTo($panel);
			
			var chartDataSets = chart.chartDataSetsNonNull();
			
			for(var i=0; i<chartDataSets.length; i++)
			{
				var params = chartDataSets[i].dataSet.params;
				
				if(!params || params.length == 0)
					continue;
				
				var formTitle = chart.dataSetName(chartDataSets[i]);
				if(formTitle != chartDataSets[i].dataSet.name)
					formTitle += " - "+chartDataSets[i].dataSet.name;
				
				var $fp = $("<div class='dg-param-value-form-wrapper' />").data("chartDataSetIndex", i).appendTo($panelContent);
				chartFactory.setTooltipThemeStyle($fp, chart);
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
						$("input, select, button", this).each(function()
						{
							chartFactory.setTooltipThemeStyle($(this), chart);
						});
						
						chartForm.getDataSetParamValueFormFoot(this).hide();
					}
				});
			}
			
			var $button = $("<button type='button' class='ui-button ui-corner-all ui-widget' />").html("确定").appendTo($panelFoot);
			chartFactory.setTooltipThemeStyle($button, chart);
			$button.click(function()
			{
				var validateOk = true;
				var paramValuesMap = {};
				
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
						paramValuesMap[myIndex] = myParamValues;
					}
				});
				
				if(validateOk)
				{
					for(var index in paramValuesMap)
						chartDataSets[index].paramValues = paramValuesMap[index];
					
					chart.statusPreUpdate(true);
				}
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
})
(this);
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
	 * 				submit: function(){}    //可选，提交处理函数
	 * 				paramValues: {...}      //可选，初始参数值
	 * 				submitText: "..."       //可选，提交按钮文本内容
	 * 				yesText: "..."       //可选，"是"选项文本内容
	 * 				noText: "..."       //可选，"否"选项文本内容
	 * 			}
	 * @return 表单DOM元素
	 */
	chartForm.renderDataSetParamValueForm = function($parent, dataSetParams, options)
	{
		options = $.extend({ submitText: "确定", yesText: "是", noText: "否" }, (options || {}));
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
			$("<label />").html(dsp.name).appendTo($labelDiv);
			
			var $valueDiv = $("<div class='form-item-value' />").appendTo($item);
			
			var $input;
			
			if(chartForm.DataSetParamDataType.BOOLEAN == dsp.type)
			{
				$input = $("<select class='ui-widget ui-widget-content' />").attr("name", dsp.name).appendTo($valueDiv);
				var $opt0 = $("<option value='true' />").html(options.yesText).appendTo($input);
				var $opt1 = $("<option value='false' />").html(options.noText).appendTo($input);
				
				var value = (paramValues[dsp.name]+"" || "true");
				
				(value == "false" ? $opt1 : $opt0).attr("selected", "selected");
			}
			else
			{
				$input = $("<input type='text' class='ui-widget ui-widget-content' />").attr("name", dsp.name)
								.attr("value", (paramValues[dsp.name] || "")).appendTo($valueDiv);
			}
			
			if((dsp.required+"") == "true")
				$input.attr("validation-required", "true");
		}
		
		$("<button type='submit' class='ui-button ui-corner-all ui-widget' />").html(options.submitText).appendTo($foot);
		
		$form.submit(function()
		{
			var validationOk = true;
			
			var $requireds = $("input[validation-required]", this);
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
			
			if(!validationOk)
				return false;
			
			if(options.submit)
				return (options.submit.apply(this) == true);
			else
				return false;
		});
		
		return $form[0];
	};
	
	chartForm.getDataSetParamValueForm = function($parent)
	{
		return $(".data-set-param-value-form", $parent);
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
})
(this);
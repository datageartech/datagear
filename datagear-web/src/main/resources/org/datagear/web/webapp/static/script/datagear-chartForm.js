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
	
	/**
	 * 渲染数据集参数值表单。
	 * 
	 * @param $parent
	 * @param dataSetParams
	 * @param options
	 * 			{
	 * 				paramValues: {...}      //初始参数值
	 * 			}
	 * @return 表单DOM元素
	 */
	chartForm.renderDataSetParamValueForm = function($parent, dataSetParams, options)
	{
		options = (options || {});
		
		var paramValues = (options.paramValues || {});
		var $form = ($parent.is("form") ? $parent : $(">form", $parent));
		if($form.length == 0)
			$form = $("<form />").appendTo($parent);
		else
			$form.empty();
		
		$form.addClass("data-set-param-value-form");
		
		for(var i=0; i<dataSetParams.length; i++)
		{
			var dsp = dataSetParams[i];
			
			var $item = $("<div class='form-item' />").appendTo($form);
			
			var $labelDiv = $("<div class='form-item-label' />").appendTo($item);
			$("<label />").html(dsp.name).appendTo($labelDiv);
			
			var $valueDiv = $("<div class='form-item-value' />").appendTo($item);
			$("<input type='text' />").attr("name", dsp.name).attr("value", (paramValues[dsp.name] || "")).appendTo($valueDiv);
		}
		
		return $form[0];
	};
})
(this);
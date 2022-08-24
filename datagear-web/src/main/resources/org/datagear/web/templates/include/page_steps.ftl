<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#--
阶段。
-->
<p-steps id="${pid}steps" :model="pm.steps.items" :readonly="pm.steps.readonly"></p-steps>
<script>
(function(po)
{
	//stepsItems数组元素格式：{ label: "", }
	po.setupSteps = function(stepsItems, readonly)
	{
		stepsItems = $.extend(true, [], stepsItems);
		readonly = (readonly == null ? true : readonly);
		
		$.each(stepsItems, function(i, stepsItem)
		{
			stepsItem.class = "step-item step-item-" + i;
		});
		
		po.vuePageModel(
		{
			steps:
			{
				items: stepsItems,
				readonly: readonly,
				activeIndex: 0
			}
		});
		
		po.vueMethod(
		{
			onToPrevStep: function()
			{
				po.toPrevStep();
			},

			onToNextStep: function()
			{
				po.toNextStep();
			}
		});
		
		po.vueMounted(function()
		{
			po.activeStep(0);
		});
	};
	
	po.isFirstStep = function()
	{
		var pm = po.vuePageModel();
		return (pm.steps.activeIndex == 0);
	};
	
	po.isLastStep = function()
	{
		var pm = po.vuePageModel();
		return (pm.steps.activeIndex == pm.steps.items.length-1);
	};
	
	po.toNextStep = function()
	{
		var pm = po.vuePageModel();
		po.activeStep(pm.steps.activeIndex + 1);
	};
	
	po.toPrevStep = function()
	{
		var pm = po.vuePageModel();
		po.activeStep(pm.steps.activeIndex - 1);
	};
	
	po.activeStep = function(index)
	{
		var pm = po.vuePageModel();
		
		index = (index <= 0 ? 0 : index);
		index = (index >= pm.steps.items.length ? pm.steps.items.length-1 : index);
		
		var prevActiveIndex = pm.steps.activeIndex;
		pm.steps.activeIndex = index;
		
		var stepsEle = po.elementOfId("${pid}steps");
		if(prevActiveIndex != index)
			po.element(".step-item-"+prevActiveIndex, stepsEle).addClass("p-disabled").removeClass("p-highlight p-steps-current");
		po.element(".step-item-"+index, stepsEle).removeClass("p-disabled").addClass("p-highlight p-steps-current");
	};
})
(${pid});
</script>

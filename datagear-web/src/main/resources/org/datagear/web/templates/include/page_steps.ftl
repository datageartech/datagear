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

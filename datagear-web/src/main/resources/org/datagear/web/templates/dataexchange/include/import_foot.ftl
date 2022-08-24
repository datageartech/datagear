<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#--
导入页脚片段
-->
<div class="page-form-foot pt-3 text-center relative h-opts">
	<div class="absolute" style="left:0;">
		<p-button type="button" label="<@spring.message code='return' />" class="p-button-secondary"></p-button>
	</div>
	<p-button type="button" label="<@spring.message code='previousStep' />"
		@click="onToPrevStep" :disabled="pm.steps.activeIndex == 0">
	</p-button>
	<p-button type="submit" label="<@spring.message code='nextStep' />"
		v-if="pm.steps.activeIndex < pm.steps.items.length-1">
	</p-button>
	<p-button type="submit" label="<@spring.message code='import' />"
		v-if="pm.steps.activeIndex == pm.steps.items.length-1">
	</p-button>
</div>
<script>
(function(po)
{
	
})
(${pid});
</script>
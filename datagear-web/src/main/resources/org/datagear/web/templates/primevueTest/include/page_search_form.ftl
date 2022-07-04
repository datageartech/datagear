<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#--
搜索表单。

-->
<form @submit.prevent="searchFormModel.handleSubmit">
	<div class="p-inputgroup">
		<p-inputtext type="text" v-model="searchFormModel.form.keyword"></p-inputtext>
		<p-button type="submit" icon="pi pi-search" />
	</div>
</form>
<script>
(function(po)
{
	po.vueSetup("searchFormModel",
	{
		form: { keyword: "" },
		handleSubmit: function()
		{
			var param = (po.vueSetup("searchFormModel").form);
			po.search(po.vueRaw(param));
		}
	});
	
	po.search = function(formData){};
})
(${pid});
</script>

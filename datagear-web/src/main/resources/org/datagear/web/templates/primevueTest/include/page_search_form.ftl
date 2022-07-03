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

依赖：
page_obj.ftl

-->
<form @submit.prevent="searchFormSubmit">
	<div class="p-inputgroup">
		<p-inputtext type="text" v-model="searchForm.keyword"></p-inputtext>
		<p-button type="submit" icon="pi pi-search" />
	</div>
</form>
<script>
(function(po)
{
	po.vueRef("searchForm",
	{
		keyword: ""
	});
	
	po.vueSetup("searchFormSubmit", function()
	{
		var formData = po.vueRef("searchForm");
		po.search(formData);
	});
	
	po.search = function(formData){};
})
(${pid});
</script>

<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#--
数据集文件编码输入项

依赖：

-->
<div class="field grid">
	<label for="${pid}encoding" class="field-label col-12 mb-2 md:col-3 md:mb-0">
		<@spring.message code='fileEncoding' />
	</label>
	<div class="field-input col-12 md:col-9">
		<p-dropdown id="${pid}encoding" v-model="pm.encoding" :options="tm.availableCharsetNames" class="input w-full">
       	</p-dropdown>
	</div>
</div>
<script>
(function(po)
{
	po.vueTmpModel(
	{
		availableCharsetNames: <@writeJson var=availableCharsetNames />
	});
})
(${pid});
</script>

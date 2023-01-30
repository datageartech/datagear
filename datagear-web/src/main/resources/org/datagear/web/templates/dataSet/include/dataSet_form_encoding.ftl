<#--
 *
 * Copyright 2018-2023 datagear.tech
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
数据集文件编码输入项

依赖：

-->
<div class="field grid">
	<label for="${pid}encoding" class="field-label col-12 mb-2 md:col-3 md:mb-0">
		<@spring.message code='fileEncoding' />
	</label>
	<div class="field-input col-12 md:col-9">
		<p-dropdown id="${pid}encoding" v-model="fm.encoding" :options="pm.availableCharsetNames" class="input w-full">
       	</p-dropdown>
	</div>
</div>
<script>
(function(po)
{
	po.vuePageModel(
	{
		availableCharsetNames: $.unescapeHtmlForJson(<@writeJson var=availableCharsetNames />)
	});
})
(${pid});
</script>

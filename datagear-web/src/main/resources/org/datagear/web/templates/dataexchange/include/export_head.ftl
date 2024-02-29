<#--
 *
 * Copyright 2018-2024 datagear.tech
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
导出页头片段
-->
<div class="page-header grid grid-nogutter align-items-center p-1 pb-2 flex-grow-0">
	<div class="col-12 flex align-items-center mb-1">
		<i class="pi pi-database text-color-secondary text-sm"></i>
		<div class="text-color-secondary text-sm ml-1">${schema.title}</div>
		<i class="pi pi-angle-right text-color-secondary text-sm mx-1"></i>
		<div class="text-color-secondary text-sm"><@spring.message code='module.exportData' /></div>
	</div>
	<div class="col-12">
		<div class="grid grid-nogutter">
			<label class="text-lg font-bold col-5 md:col-3">
				{{pm.exportHeadTitle}}
			</label>
			<div class="col-7 md:col-9 inline-steps">
				<#include "../../include/page_steps.ftl">
			</div>
		</div>
	</div>
</div>
<script>
(function(po)
{
	po.stepsItems =
	[
		{ label: "<@spring.message code='set' />" },
		{ label: "<@spring.message code='export' />" }
	];
	
	po.setupExportHead = function(title)
	{
		po.vuePageModel(
		{
			exportHeadTitle: title
		});
		
		po.setupSteps(po.stepsItems);
	};
})
(${pid});
</script>
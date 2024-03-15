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
导出页脚片段

依赖：
dataexchange_js.ftl
-->
<div class="page-form-foot p-1 pt-2 flex-grow-0 flex justify-content-between">
	<div class="w-4 text-left">
		<p-button type="button" label="<@spring.message code='return' />"
			class="p-button-secondary" @click="onReturn"
			:disabled="pm.dataExchangeStatus == pm.DataExchangeStatusEnum.exchange">
		</p-button>
	</div>
	<div class="w-4 flex justify-content-center gap-2">
		<p-button type="button" label="<@spring.message code='previousStep' />"
			@click="onToPrevStep" :disabled="pm.steps.activeIndex == 0 || pm.dataExchangeStatus != pm.DataExchangeStatusEnum.edit">
		</p-button>
		<p-button type="submit" label="<@spring.message code='nextStep' />"
			:disabled="pm.dataExchangeStatus != pm.DataExchangeStatusEnum.edit"
			v-if="pm.steps.activeIndex < pm.steps.items.length-1">
		</p-button>
		<p-button type="submit" label="<@spring.message code='export' />"
			:disabled="pm.dataExchangeStatus != pm.DataExchangeStatusEnum.edit"
			v-if="pm.steps.activeIndex == pm.steps.items.length-1">
		</p-button>
	</div>
	<div class="w-4 text-right">
		<p-button type="button" label="<@spring.message code='restart' />"
			class="p-button-secondary" @click="onRestart"
			v-if="pm.dataExchangeStatus == pm.DataExchangeStatusEnum.finish">
		</p-button>
	</div>
</div>
<script>
(function(po)
{
	po.vueMethod(
	{
		onReturn: function()
		{
			var url = "/dataexchange/"+encodeURIComponent(po.schemaId)+"/export/";
			
			if(po.queries)
			{
				$.each(po.queries, function(i, query)
				{
					url = $.addParam(url, "query", query, true);
				});
			}
			
			if(po.isAjaxRequest)
			{
				po.ajax(url,
				{
					closeAfterSubmit: false,
					success: function(response)
					{
						po.element().parent().html(response);
					}
				});
			}
			else
			{
				po.open(url, {target: "_self"});
			}
		},
		onRestart: function()
		{
			po.resetDataExchangeStatus();
		}
	});
})
(${pid});
</script>
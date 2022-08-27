<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#--
导出页脚片段

依赖：
dataexchange_js.ftl
-->
<div class="page-form-foot pt-3 text-center flex justify-content-between">
	<div class="w-4 text-left">
		<p-button type="button" label="<@spring.message code='return' />"
			class="p-button-secondary" @click="onReturn"
			:disabled="pm.dataExchangeStatus == pm.DataExchangeStatusEnum.exchange">
		</p-button>
	</div>
	<div class="w-4 text-center h-opts">
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
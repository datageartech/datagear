<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#include "../include/page_import.ftl">
<#include "../include/html_doctype.ftl">
<html>
<head>
<#include "../include/html_head.ftl">
<title>
	<#include "../include/html_app_name_prefix.ftl">
	<@spring.message code='detailLog' />
</title>
</head>
<body class="p-card no-border">
<#include "../include/page_obj.ftl">
<div id="${pid}" class="page page-form horizontal">
	<form id="${pid}form" class="flex flex-column">
		<div class="page-form-content flex-grow-1 pr-2 py-1 overflow-y-auto">
			<pre id="${pid}logContent" class="w-full"></pre>
		</div>
	</form>
</div>
<#include "../include/page_form.ftl">
<script>
(function(po)
{
	po.schemaId = "${schema.id}";
	po.dataExchangeId = "${dataExchangeId}";
	po.subDataExchangeId = "${subDataExchangeId}";
	
	po.vueMounted(function()
	{
		po.ajax("/dataexchange/" + encodeURIComponent(po.schemaId) +"/getLogContent",
		{
			data:
			{
				dataExchangeId : po.dataExchangeId,
				subDataExchangeId : po.subDataExchangeId
			},
			success: function(response)
			{
				po.elementOfId("${pid}logContent").html(response);
			}
		});
	});
	
	po.vueMount();
})
(${pid});
</script>
</body>
</html>
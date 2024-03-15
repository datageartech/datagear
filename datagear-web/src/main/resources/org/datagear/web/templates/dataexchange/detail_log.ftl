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
<#include "../include/page_import.ftl">
<#include "../include/html_doctype.ftl">
<html>
<head>
<#include "../include/html_head.ftl">
<title>
	<@spring.message code='detailLog' />
	<#include "../include/html_app_name_suffix.ftl">
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
})
(${pid});
</script>
<#include "../include/page_vue_mount.ftl">
</body>
</html>
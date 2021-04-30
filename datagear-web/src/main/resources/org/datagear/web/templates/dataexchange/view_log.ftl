<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#include "../include/import_global.ftl">
<#include "../include/html_doctype.ftl">
<#--
Schema schema 数据库，不允许为null
-->
<html>
<head>
<#include "../include/html_head.ftl">
<title>
	<#include "../include/html_title_app_name.ftl">
	<@spring.message code='dataExchange.viewLog' />
	<@spring.message code='bracketLeft' />
	${schema.title}
	<@spring.message code='bracketRight' />
</title>
</head>
<body class="fill-parent">
<#if !isAjaxRequest>
<div class="fill-parent">
</#if>
<#include "../include/page_js_obj.ftl">
<div id="${pageId}" class="page-dataexchange-log">
	<div class="head">
	</div>
	<div class="content fill-parent">
		<div class="dataexchange-log-content-wrapper">
			<pre class="dataexchange-log-content"></pre>
		</div>
	</div>
	<div class="foot">
	</div>
</div>
<#if !isAjaxRequest>
</div>
</#if>
<script type="text/javascript">
(function(po)
{
	po.schemaId = "${schema.id}";
	po.dataExchangeId = "${dataExchangeId}";
	po.subDataExchangeId = "${subDataExchangeId}";
	
	po.element(".dataexchange-log-content").load("${contextPath}/dataexchange/" + po.schemaId +"/getLogContent",
	{
		schemaId : po.schemaId,
		dataExchangeId : po.dataExchangeId,
		subDataExchangeId : po.subDataExchangeId
	});
})
(${pageId});
</script>
</body>
</html>

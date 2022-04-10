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
<#--
Schema schema 数据库，不允许为null
-->
<html>
<head>
<#include "../include/html_head.ftl">
<title>
	<#include "../include/html_title_app_name.ftl">
	<@spring.message code='dataImport.dataImport' />
	<@spring.message code='bracketLeft' />
	${schema.title}
	<@spring.message code='bracketRight' />
</title>
</head>
<body class="fill-parent">
<#if !isAjaxRequest>
<div class="fill-parent">
</#if>
<#include "../include/page_obj.ftl">
<div id="${pageId}" class="page-dataimport">
	<div class="head">
	</div>
	<div class="content">
		<form id="${pageId}-form" action="#" method="POST">
			<div class="steps">
				<h3>选择数据类型</h3>
				<section>
					<div class="importDataType">
						<label for="${pageId}-dataType-0">CSV</label>
						<input id="${pageId}-dataType-0" type="radio" name="dataType" value="csv" />
						<label for="${pageId}-dataType-1">XML</label>
						<input id="${pageId}-dataType-1" type="radio" name="dataType" value="xml" />
					</div>
				</section>
				<h3>上传数据</h3>
				<section>
					<input type="file">
				</section>
			</div>
		</form>
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
	po.form = po.element("#${pageId}-form");
	
	po.element(".importDataType").checkboxradiogroup();
	
	po.element(".steps").steps(
	{
		headerTag: "h3",
		bodyTag: "section",
		transitionEffect: "slideLeft"
	});
})
(${pageId});
</script>
</body>
</html>

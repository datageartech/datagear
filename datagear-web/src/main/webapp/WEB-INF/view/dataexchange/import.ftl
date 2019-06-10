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
	<@spring.message code='dataimport.dataimport' />
	<@spring.message code='bracketLeft' />
	${schema.title?html}
	<@spring.message code='bracketRight' />
</title>
</head>
<body class="fill-parent">
<#if !isAjaxRequest>
<div class="fill-parent">
</#if>
<div id="${pageId}" class="page-dataimport">
	<div class="head">
	</div>
	<div class="content">
		<form id="${pageId}-form" action="#" method="POST">
			<div class="form-head">
				<@spring.message code='dataimport.selectDataType' />
			</div>
			<div class="form-content">
				<div class="form-item">
					<div class="form-item-label">&nbsp;</div>
					<div class="form-item-value">
						<label for="${pageId}-dataType-0"><@spring.message code='dataimport.dataType.csv' /></label>
						<input id="${pageId}-dataType-0" type="radio" name="dataType" value="csv" />
						<div class="input-desc">
							<@spring.message code='dataimport.dataType.csv.desc' />
						</div>
					</div>
				</div>
				<div class="form-item">
					<div class="form-item-label">&nbsp;</div>
					<div class="form-item-value">
						<label for="${pageId}-dataType-1"><@spring.message code='dataimport.dataType.db' /></label>
						<input id="${pageId}-dataType-1" type="radio" name="dataType" value="db" />
						<div class="input-desc">
							<@spring.message code='dataimport.dataType.db.desc' />
						</div>
					</div>
				</div>
			</div>
			<div class="form-foot">
				<button type="submit" class="recommended">
					<@spring.message code='confirm' />
				</button>
			</div>
		</form>
	</div>
	<div class="foot">
	</div>
</div>
<#if !isAjaxRequest>
</div>
</#if>

<#include "../include/page_js_obj.ftl">
<script type="text/javascript">
(function(po)
{
	po.schemaId = "${schema.id}";
	po.form = po.element("#${pageId}-form");
	
	$.initButtons(po.element());
	po.element("input[type=radio]").checkboxradio({icon:true});
	po.element("#${pageId}-dataType-0").prop("checked", true);
	po.element("input[type=radio]").checkboxradio("refresh");
})
(${pageId});
</script>
</body>
</html>

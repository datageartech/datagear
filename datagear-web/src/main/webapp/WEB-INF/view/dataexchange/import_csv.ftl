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
<div id="${pageId}" class="page-dataimport-csv">
	<div class="head">
		<@spring.message code='dataimport.importCsvData' />
	</div>
	<div class="content">
		<form id="${pageId}-form" action="#" method="POST">
			<div class="form-content">
				<h3><@spring.message code='dataimport.setDataFormat' /></h3>
				<div>
					<div class="form-item">
						<div class="form-item-label"><@spring.message code='dataimport.dataFormat.dateFormat' /></div>
						<div class="form-item-value">
							<input type="text" name="dataFormat.dateFormat" value="${defaultDataFormat.dateFormat}" class="ui-widget ui-widget-content" />
						</div>
					</div>
					<div class="form-item">
						<div class="form-item-label"><@spring.message code='dataimport.dataFormat.timeFormat' /></div>
						<div class="form-item-value">
							<input type="text" name="dataFormat.timeFormat" value="${defaultDataFormat.timeFormat}" class="ui-widget ui-widget-content" />
						</div>
					</div>
					<div class="form-item">
						<div class="form-item-label"><@spring.message code='dataimport.dataFormat.timestampFormat' /></div>
						<div class="form-item-value">
							<input type="text" name="dataFormat.timestampFormat" value="${defaultDataFormat.timestampFormat}" class="ui-widget ui-widget-content" />
						</div>
					</div>
					<div class="form-item">
						<div class="form-item-label"><@spring.message code='dataimport.dataFormat.numberFormat' /></div>
						<div class="form-item-value">
							<input type="text" name="dataFormat.numberFormat" value="${defaultDataFormat.numberFormat}" class="ui-widget ui-widget-content" />
						</div>
					</div>
					<div class="form-item">
						<div class="form-item-label"><@spring.message code='dataimport.dataFormat.binaryFormat' /></div>
						<div class="form-item-value">
							<div id="${pageId}-binaryFormat">
								<label for="${pageId}-binaryFormat-0"><@spring.message code='dataimport.dataFormat.binaryFormat.HEX' /></label>
								<input id="${pageId}-binaryFormat-0" type="radio" name="dataFormat.binaryFormat" value="HEX" />
								<label for="${pageId}-binaryFormat-1"><@spring.message code='dataimport.dataFormat.binaryFormat.Base64' /></label>
								<input id="${pageId}-binaryFormat-1" type="radio" name="dataFormat.binaryFormat" value="BASE64" />
								<label for="${pageId}-binaryFormat-2"><@spring.message code='dataimport.dataFormat.binaryFormat.NULL' /></label>
								<input id="${pageId}-binaryFormat-2" type="radio" name="dataFormat.binaryFormat" value="NULL" />
							</div>
						</div>
					</div>
					<div class="form-item">
						<div class="form-item-label"><@spring.message code='dataimport.ignoreInexistentColumn' /></div>
						<div class="form-item-value">
							<div id="${pageId}-ignoreInexistentColumn">
							<label for="${pageId}-ignoreInexistentColumn-0"><@spring.message code='yes' /></label>
							<input id="${pageId}-ignoreInexistentColumn-0" type="radio" name="ignoreInexistentColumn" value="true" />
							<label for="${pageId}-ignoreInexistentColumn-1"><@spring.message code='no' /></label>
							<input id="${pageId}-ignoreInexistentColumn-1" type="radio" name="ignoreInexistentColumn" value="false" />
							</div>
						</div>
					</div>
					<div class="form-item">
						<div class="form-item-label"><@spring.message code='dataimport.nullForIllegalColumnValue' /></div>
						<div class="form-item-value">
							<div id="${pageId}-nullForIllegalColumnValue">
								<label for="${pageId}-nullForIllegalColumnValue-0"><@spring.message code='yes' /></label>
								<input id="${pageId}-nullForIllegalColumnValue-0" type="radio" name="nullForIllegalColumnValue" value="true" />
								<label for="${pageId}-nullForIllegalColumnValue-1"><@spring.message code='no' /></label>
								<input id="${pageId}-nullForIllegalColumnValue-1" type="radio" name="nullForIllegalColumnValue" value="false" />
							</div>
						</div>
					</div>
					<div class="form-item">
						<div class="form-item-label"><@spring.message code='dataimport.exceptionResolve' /></div>
						<div class="form-item-value">
							<div id="${pageId}-exceptionResolve">
								<label for="${pageId}-exceptionResolve-0"><@spring.message code='dataimport.exceptionResolve.ABORT' /></label>
								<input id="${pageId}-exceptionResolve-0" type="radio" name="exceptionResolve" value="ABORT" />
								<label for="${pageId}-exceptionResolve-1"><@spring.message code='dataimport.exceptionResolve.IGNORE' /></label>
								<input id="${pageId}-exceptionResolve-1" type="radio" name="exceptionResolve" value="IGNORE" />
								<label for="${pageId}-exceptionResolve-2"><@spring.message code='dataimport.exceptionResolve.ROLLBACK' /></label>
								<input id="${pageId}-exceptionResolve-2" type="radio" name="exceptionResolve" value="ROLLBACK" />
							</div>
						</div>
					</div>
				</div>
				<h3><@spring.message code='dataimport.uploadData' /></h3>
				<div>
					<input type="file">
				</div>
				<h3><@spring.message code='dataimport.import' /></h3>
				<div>
					<input type="file">
				</div>
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
	
	po.element(".form-content").steps(
	{
		headerTag: "h3",
		bodyTag: "div",
		labels:
		{
			previous: "<@spring.message code='wizard.previous' />",
			next: "<@spring.message code='wizard.next' />",
			finish: "<@spring.message code='wizard.finish' />"
		}
	});

	po.element("#${pageId}-binaryFormat").buttonset();
	po.element("#${pageId}-ignoreInexistentColumn").buttonset();
	po.element("#${pageId}-nullForIllegalColumnValue").buttonset();
	po.element("#${pageId}-exceptionResolve").buttonset();
	
	po.element("input[name='dataFormat.binaryFormat'][value='${defaultDataFormat.binaryFormat}']").click();
	po.element("#${pageId}-ignoreInexistentColumn-0").click();
	po.element("#${pageId}-nullForIllegalColumnValue-1").click();
	po.element("#${pageId}-exceptionResolve-0").click();
})
(${pageId});
</script>
</body>
</html>

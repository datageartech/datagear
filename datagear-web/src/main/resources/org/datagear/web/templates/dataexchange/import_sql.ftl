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
<#include "../include/page_js_obj.ftl">
<div id="${pageId}" class="page-dataexchange page-dataimport-text page-dataimport-csv">
	<div class="head">
		<@spring.message code='dataImport.importSqlData' />
	</div>
	<div class="content">
		<form id="${pageId}-form" action="${contextPath}/dataexchange/${schema.id}/import/sql/doImport" method="POST">
			<input type="hidden" name="dataExchangeId" value="${dataExchangeId}" />
			<div class="form-content form-content-wizard">
				<h3><@spring.message code='dataImport.setDataFormat' /></h3>
				<div>
					<div class="form-item">
						<div class="form-item-label"><@spring.message code='dataExchange.exceptionResolve' /></div>
						<div class="form-item-value">
							<div id="${pageId}-exceptionResolve">
								<label for="${pageId}-exceptionResolve-0"><@spring.message code='dataExchange.exceptionResolve.ROLLBACK' /></label>
								<input id="${pageId}-exceptionResolve-0" type="radio" name="importOption.exceptionResolve" value="ROLLBACK" />
								<label for="${pageId}-exceptionResolve-1"><@spring.message code='dataExchange.exceptionResolve.ABORT' /></label>
								<input id="${pageId}-exceptionResolve-1" type="radio" name="importOption.exceptionResolve" value="ABORT" />
								<label for="${pageId}-exceptionResolve-2"><@spring.message code='dataExchange.exceptionResolve.IGNORE' /></label>
								<input id="${pageId}-exceptionResolve-2" type="radio" name="importOption.exceptionResolve" value="IGNORE" />
							</div>
						</div>
					</div>
				</div>
				<h3><@spring.message code='dataImport.uploadAndImportData' /></h3>
				<div>
					<div class="form-item form-item-table-head form-item-upload page-status-aware-show edit-status-show">
						<div class="form-item-value">
							<!--
							<label><@spring.message code='dataImport.uploadSqlDataFile' /></label>
							-->
							<div class="fileinput-button ui-widget ui-button ui-corner-all" upload-action="sql/uploadImportFile" title="<@spring.message code='dataImport.uploadSqlDataFile.desc' />"><@spring.message code='add' /><input type="file"></div>
							<div class="upload-file-info"></div>
						</div>
					</div>
					<div class="form-item form-item-table-head form-item-progress page-status-aware-show exchange-status-show finish-status-show">
						<div class="form-item-value">
							<!--
							<label><@spring.message code='dataImport.importProgress' /></label>
							-->
							<div id="${pageId}-progress"></div>
							<div id="${pageId}-progress-percent" class="progress-percent"></div>
						</div>
					</div>
					<div class="form-item form-item-table">
						<div class="table-operation-wrapper">
							<button type="button" class="table-delete-item-button page-status-aware-show edit-status-show"><@spring.message code='delete' /></button>
							<button type="button" class="table-cancel-import-button page-status-aware-show exchange-status-show"><@spring.message code='cancel' /></button>
						</div>
						<div class="file-encoding-wrapper">
							<label class="file-encoding-label page-status-aware-enable edit-status-enable">
								<@spring.message code='dataImport.importFileEncoding' />
							</label>
							<select name="fileEncoding">
								<#list availableCharsetNames as item>
								<option value="${item}" <#if item == defaultCharsetName>selected="selected"</#if>>${item}</option>
								</#list>
							</select>
							&nbsp;
							<label class="file-encoding-label page-status-aware-enable edit-status-enable tip-label"
									title="<@spring.message code='dataImport.importZipFileNameEncoding.desc' />">
								<@spring.message code='dataImport.importZipFileNameEncoding' />
							</label>
							&nbsp;
							<select name="zipFileNameEncoding">
								<#list availableCharsetNames as item>
								<option value="${item}" <#if item == zipFileNameEncodingDefault>selected="selected"</#if>>${item}</option>
								</#list>
							</select>
						</div>
						<div class="table-wrapper minor-dataTable">
							<table id="${pageId}-table" width="100%" class="hover stripe"></table>
						</div>
					</div>
				</div>
			</div>
		</form>
		<div class="return-wrapper page-status-aware-show edit-status-show finish-status-show">
			<#include "include/dataExchange_return_form_import.ftl">
		</div>
		<div class="restart-wrapper page-status-aware-show finish-status-show">
			<button type="button" class="restart-button"><@spring.message code='restart' /></button>
		</div>
		<div id="${pageId}-exchange-exception-tooltip" title="import tooltip" style="width:0; height:0;"></div>
	</div>
	<div class="foot">
	</div>
</div>
<#if !isAjaxRequest>
</div>
</#if>
<#include "../include/page_obj_grid.ftl">
<#include "../include/page_obj_format_time.ftl" >
<#include "include/dataExchange_js.ftl" >
<#include "include/dataImport_js.ftl" >
<script type="text/javascript">
(function(po)
{
	po.dependentNumberInputPlaceholder = "<@spring.message code='dataImport.dependentNumber.none' />";
	
	po.initDataImportSteps();
	po.initDataExchangeUIs();
	po.initDataImportUIs();
	po.initDataImportDataTable();
	po.initDataExchangeActions();
	po.initDataImportActions();
	
	po.updateDataExchangePageStatus("edit");
})
(${pageId});
</script>
</body>
</html>

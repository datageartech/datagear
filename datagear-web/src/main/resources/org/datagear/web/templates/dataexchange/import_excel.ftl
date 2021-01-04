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
<div id="${pageId}" class="page-dataexchange page-dataimport-text page-dataimport-excel">
	<div class="head">
		<@spring.message code='dataImport.importExcelData' />
	</div>
	<div class="content">
		<form id="${pageId}-form" action="${contextPath}/dataexchange/${schema.id}/import/excel/doImport" method="POST">
			<input type="hidden" name="dataExchangeId" value="${dataExchangeId}" />
			<input type="hidden" name="dependentNumberAuto" value="<@spring.message code='dataImport.dependentNumber.auto' />" />
			<div class="form-content form-content-wizard">
				<h3><@spring.message code='dataImport.setDataFormat' /></h3>
				<div>
					<#include "include/dataExchange_form_dataFormat_html.ftl">
					<div class="form-item">
						<div class="form-item-label"><@spring.message code='dataImport.ignoreInexistentColumn' /></div>
						<div class="form-item-value">
							<div id="${pageId}-ignoreInexistentColumn">
							<label for="${pageId}-ignoreInexistentColumn-0"><@spring.message code='yes' /></label>
							<input id="${pageId}-ignoreInexistentColumn-0" type="radio" name="importOption.ignoreInexistentColumn" value="true" />
							<label for="${pageId}-ignoreInexistentColumn-1"><@spring.message code='no' /></label>
							<input id="${pageId}-ignoreInexistentColumn-1" type="radio" name="importOption.ignoreInexistentColumn" value="false" />
							</div>
						</div>
					</div>
					<div class="form-item">
						<div class="form-item-label"><@spring.message code='dataImport.nullForIllegalColumnValue' /></div>
						<div class="form-item-value">
							<div id="${pageId}-nullForIllegalColumnValue">
								<label for="${pageId}-nullForIllegalColumnValue-0"><@spring.message code='yes' /></label>
								<input id="${pageId}-nullForIllegalColumnValue-0" type="radio" name="importOption.nullForIllegalColumnValue" value="true" />
								<label for="${pageId}-nullForIllegalColumnValue-1"><@spring.message code='no' /></label>
								<input id="${pageId}-nullForIllegalColumnValue-1" type="radio" name="importOption.nullForIllegalColumnValue" value="false" />
							</div>
						</div>
					</div>
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
							<label><@spring.message code='dataImport.uploadExcelDataFile' /></label>
							<div class="fileinput-button ui-widget ui-button ui-corner-all" upload-action="excel/uploadImportFile" title="<@spring.message code='dataImport.uploadExcelDataFile.desc' />"><@spring.message code='add' /><input type="file"></div>
							<div class="upload-file-info"></div>
						</div>
					</div>
					<div class="form-item form-item-table-head form-item-progress page-status-aware-show exchange-status-show finish-status-show">
						<div class="form-item-value">
							<label><@spring.message code='dataImport.importProgress' /></label>
							<div id="${pageId}-progress"></div>
							<div id="${pageId}-progress-percent" class="progress-percent"></div>
						</div>
					</div>
					<div class="form-item form-item-table">
						<div class="table-operation-wrapper">
							<button type="button" class="table-delete-item-button page-status-aware-show edit-status-show"><@spring.message code='delete' /></button>
							<button type="button" class="table-cancel-import-button page-status-aware-show exchange-status-show"><@spring.message code='cancel' /></button>
						</div>
						<#--
						<div class="file-encoding-wrapper">
							<span class="file-encoding-label page-status-aware-enable edit-status-enable">
								<@spring.message code='dataImport.importFileEncoding' />
							</span>
							<select name="fileEncoding">
								<#list availableCharsetNames as item>
								<option value="${item}" <#if item == defaultCharsetName>selected="selected"</#if>>${item}</option>
								</#list>
							</select>
						</div>
						-->
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

<#include "../include/page_js_obj.ftl">
<#include "../include/page_obj_grid.ftl">
<#include "../include/page_obj_format_time.ftl" >
<#include "include/dataExchange_js.ftl" >
<#include "include/dataImport_js.ftl" >
<script type="text/javascript">
(function(po)
{
	po.dependentNumberInputPlaceholder = "<@spring.message code='dataImport.dependentNumber.none' />";
	
	po.postBuildSubDataExchange = function(subDataExchange)
	{
		subDataExchange.dependentNumber = "<@spring.message code='dataImport.dependentNumber.auto' />";
	};
	
	po.initDataImportExcelUIs = function()
	{
		po.element("#${pageId}-ignoreInexistentColumn").buttonset();
		po.element("#${pageId}-nullForIllegalColumnValue").buttonset();
		
		po.element("#${pageId}-ignoreInexistentColumn-1").click();
		po.element("#${pageId}-nullForIllegalColumnValue-1").click();
	};
	
	po.dataImportTableColumns.splice(3, 0,
	{
		title : "<@spring.message code='dataImport.importTableName' />",
		data : "tableName",
		render : function(data, type, row, meta)
		{
			return "<input type='text' name='tableNames[]' value='"+$.escapeHtml(data)+"' placeholder='<@spring.message code='dataImport.excel.importTableInput.placeholder' />' class='table-name-input ui-widget ui-widget-content' style='width:90%' />";
		},
		defaultContent: "",
		width : "20%"
	});
	po.dataImportTableColumns[1].width = "25%";
	
	po.initDataImportSteps();
	po.initDataExchangeUIs();
	po.initDataImportUIs();
	po.initDataImportExcelUIs();
	po.initDataImportDataTable();
	po.initDataExchangeActions();
	po.initDataImportActions();
	
	po.elementTable().on("click", ".table-name-input", function(event)
	{
		//阻止行选中
		event.stopPropagation();
	});
	
	po.updateDataExchangePageStatus("edit");
})
(${pageId});
</script>
</body>
</html>

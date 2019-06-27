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
	${schema.title?html}
	<@spring.message code='bracketRight' />
</title>
</head>
<body class="fill-parent">
<#if !isAjaxRequest>
<div class="fill-parent">
</#if>
<div id="${pageId}" class="page-dataexchange page-dataimport-text page-dataimport-csv">
	<div class="head">
		<@spring.message code='dataImport.importCsvData' />
	</div>
	<div class="content">
		<form id="${pageId}-form" action="#" method="POST">
			<input type="hidden" name="dataExchangeId" value="${dataExchangeId}" />
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
					<div class="form-item form-item-table-head form-item-upload upload-state-aware">
						<div class="form-item-value">
							<label><@spring.message code='dataImport.uploadCsvDataFile' /></label>
							<div class="fileinput-button ui-widget ui-button ui-corner-all"><@spring.message code='upload' /><input type="file"></div>
							<div class="file-info"></div>
						</div>
					</div>
					<div class="form-item form-item-table-head form-item-progress import-state-aware">
						<div class="form-item-value">
							<label><@spring.message code='dataImport.importProgress' /></label>
							<div id="${pageId}-progress"></div>
							<div id="${pageId}-progress-percent" class="progress-percent"></div>
						</div>
					</div>
					<div class="form-item form-item-table">
						<div class="table-operation-wrapper">
							<button type="button" class="table-delete-item-button upload-state-aware"><@spring.message code='delete' /></button>
							<button type="button" class="table-cancel-import-button import-state-aware"><@spring.message code='cancel' /></button>
						</div>
						<div class="file-encoding-wrapper">
							<span class="file-encoding-label">
								<@spring.message code='dataImport.importFileEncoding' />
							</span>
							<select name="fileEncoding">
								<#list availableCharsetNames as item>
								<option value="${item}" <#if item == defaultCharsetName>selected="selected"</#if>>${item}</option>
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
		<div class="restart-wrapper">
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
<#include "../include/page_obj_cometd.ftl">
<#include "../include/page_obj_format_time.ftl" >
<#include "include/dataExchange_js.ftl" >
<script type="text/javascript">
(function(po)
{
	po.dataExchangeChannelId = "${dataExchangeChannelId}";
	po.subDataExchangeStatusColumnIndex = 4;
	
	po.form = po.element("#${pageId}-form");
	po.fileUploadInfo = function(){ return this.element(".file-info"); };
	
	po.cometdInitIfNot();
	
	po.renderUploadFiles = function(fileInfos)
	{
		po.addRowData(fileInfos);
	};
	
	po.toggleUploadAndImportStatus = function(importStatus)
	{
		var importActionEle = po.element("#${pageId}-form .wizard .actions ul li:eq(2)");
		
		if(importStatus)
		{
			po.element(".upload-state-aware").hide();
			po.element(".import-state-aware").show();
			importActionEle.addClass("ui-state-disabled");
			po.element("select[name='fileEncoding']").selectmenu("disable");
			po.element(".file-encoding-label").addClass("ui-state-disabled");
		}
		else
		{
			po.element(".upload-state-aware").show();
			po.element(".import-state-aware").hide();
			importActionEle.removeClass("ui-state-disabled");
			po.element("select[name='fileEncoding']").selectmenu("enable");
			po.element(".file-encoding-label").removeClass("ui-state-disabled");
		}
	};
	
	po.element(".form-content").steps(
	{
		headerTag: "h3",
		bodyTag: "div",
		onStepChanged : function(event, currentIndex, priorIndex)
		{
			if(currentIndex == 1)
				po.adjustDataTable();
		},
		onFinished : function(event, currentIndex)
		{
			po.element("#${pageId}-form").submit();
		},
		labels:
		{
			previous: "<@spring.message code='wizard.previous' />",
			next: "<@spring.message code='wizard.next' />",
			finish: "<@spring.message code='import' />"
		}
	});

	$.initButtons(po.element());
	po.element("#${pageId}-binaryFormat").buttonset();
	po.element("#${pageId}-ignoreInexistentColumn").buttonset();
	po.element("#${pageId}-nullForIllegalColumnValue").buttonset();
	po.element("#${pageId}-exceptionResolve").buttonset();
	po.element("select[name='fileEncoding']").selectmenu({ appendTo : po.element(), classes : { "ui-selectmenu-menu" : "file-encoding-selectmenu-menu" } });
	
	po.element("input[name='dataFormat.binaryFormat'][value='${defaultDataFormat.binaryFormat}']").click();
	po.element("#${pageId}-ignoreInexistentColumn-1").click();
	po.element("#${pageId}-nullForIllegalColumnValue-1").click();
	po.element("#${pageId}-exceptionResolve-0").click();

	po.element(".fileinput-button").fileupload(
	{
		url : "${contextPath}/dataexchange/" + po.schemaId +"/import/uploadDataFile",
		paramName : "file",
		success : function(serverFileInfos, textStatus, jqXHR)
		{
			$.fileuploadsuccessHandlerForUploadInfo(po.fileUploadInfo(), true);
			
			po.renderUploadFiles(serverFileInfos);
			
			$.tipSuccess("<@spring.message code='uploadSuccess' />");
		}
	})
	.bind('fileuploadadd', function (e, data)
	{
		$.fileuploadaddHandlerForUploadInfo(e, data, po.fileUploadInfo());
	})
	.bind('fileuploadprogressall', function (e, data)
	{
		$.fileuploadprogressallHandlerForUploadInfo(e, data, po.fileUploadInfo());
	});
	
	po.element(".table-delete-item-button").click(function()
	{
		po.executeOnSelects(function(rowDatas, rowIndexes)
		{
			po.deleteRow(rowIndexes);
		});
	});
	
	po.element(".table-cancel-import-button").click(function()
	{
		po.cancelSelectedSubDataExchange();
	});
	
	po.element(".restart-button").click(function()
	{
		po.toggleUploadAndImportStatus(false);
		po.setDataExchangeProgress(0);
		po.toggleRestartStatus(false);
	});
	
	po.expectedResizeDataTableElements = [po.table()[0]];
	
	var tableColumns = [
		{
			title : "<@spring.message code='dataImport.importFileName' />",
			data : "displayName",
			render : function(data, type, row, meta)
			{
				return po.renderColumn(data, type, row, meta)
					+ "<input type='hidden' name='subDataExchangeIds' value='"+$.escapeHtml(row.subDataExchangeId)+"' />"
					+ "<input type='hidden' name='fileNames' value='"+$.escapeHtml(row.name)+"' />";
			},
			defaultContent: "",
			width : "35%",
		},
		{
			title : "<@spring.message code='dataImport.importFileSize' />",
			data : "size",
			render : po.renderColumn,
			defaultContent: "",
			width : "13%"
		},
		{
			title : "<@spring.message code='dataImport.importTableName' />",
			data : "tableName",
			render : function(data, type, row, meta)
			{
				return "<input type='text' name='tableNames' value='"+$.escapeHtml(data)+"' class='table-name-input ui-widget ui-widget-content' style='width:90%' />";
			},
			defaultContent: "",
			width : "25%"
		},
		{
			title : $.buildDataTablesColumnTitleWithTip("<@spring.message code='dataImport.importProgress' />", "<@spring.message code='dataImport.importStatusWithSuccessFail' />"),
			data : "status",
			render : function(data, type, row, meta)
			{
				if(!data)
					return "<@spring.message code='dataExchange.exchangeStatus.Unstart' />";
				else
					return data;
			},
			defaultContent: "",
			width : "27%"
		}
	];
	var tableSettings = po.buildDataTableSettingsLocal(tableColumns, [], {"order": []});
	po.initDataTable(tableSettings);
	po.bindResizeDataTable();
	
	po.table().on("click", ".table-name-input", function(event)
	{
		//阻止行选中
		event.stopPropagation();
	});
	
	po.table().on("click", ".exchange-result-icon", function(event)
	{
		//阻止行选中
		event.stopPropagation();
		
		var $this = $(this);
		
		if(!$this.hasClass("ui-state-error"))
			return;
		
		var subDataExchangeId = $this.attr("subDataExchangeId");
		var rowData = po.getSubDataExchangeRowData(subDataExchangeId);
		var displayName = $.escapeHtml((rowData ? rowData.displayName : ""));
		
		po.viewSubDataExchangeDetailLog(subDataExchangeId, displayName);
	});
	
	po.element("#${pageId}-form").submit(function()
	{
		po.cometdExecuteAfterSubscribe(po.dataExchangeChannelId,
		function()
		{
			po.element("#${pageId}-form").ajaxSubmit(
			{
				url : "${contextPath}/dataexchange/" + po.schemaId +"/import/csv/doImport",
				success: function(data)
				{
					po.toggleUploadAndImportStatus(true);
				}
			});
		},
		function(message)
		{
			po.handleDataExchangeCometdMessage(message);
		});
		
		return false;
	});
	
	po.toggleUploadAndImportStatus(false);
	po.setDataExchangeProgress(0);
	po.toggleRestartStatus(false);
})
(${pageId});
</script>
</body>
</html>

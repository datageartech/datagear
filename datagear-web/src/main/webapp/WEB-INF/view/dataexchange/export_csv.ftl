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
	<@spring.message code='dataExport.dataExport' />
	<@spring.message code='bracketLeft' />
	${schema.title?html}
	<@spring.message code='bracketRight' />
</title>
</head>
<body class="fill-parent">
<#if !isAjaxRequest>
<div class="fill-parent">
</#if>
<div id="${pageId}" class="page-dataexchange page-dataexport-text page-dataexport-csv">
	<div class="head">
		<@spring.message code='dataExport.exportCsvData' />
	</div>
	<div class="content">
		<form id="${pageId}-form" action="#" method="POST">
			<input type="hidden" name="dataExchangeId" value="${dataExchangeId}" />
			<div class="form-content form-content-wizard">
				<h3><@spring.message code='dataExport.setDataFormat' /></h3>
				<div>
					<#include "include/dataExchange_form_dataFormat_html.ftl">
					<div class="form-item">
						<div class="form-item-label"><@spring.message code='dataExport.nullForIllegalColumnValue' /></div>
						<div class="form-item-value">
							<div id="${pageId}-nullForIllegalColumnValue">
								<label for="${pageId}-nullForIllegalColumnValue-0"><@spring.message code='yes' /></label>
								<input id="${pageId}-nullForIllegalColumnValue-0" type="radio" name="exportOption.nullForIllegalColumnValue" value="true" />
								<label for="${pageId}-nullForIllegalColumnValue-1"><@spring.message code='no' /></label>
								<input id="${pageId}-nullForIllegalColumnValue-1" type="radio" name="exportOption.nullForIllegalColumnValue" value="false" />
							</div>
						</div>
					</div>
				</div>
				<h3><@spring.message code='dataExport.selectAndExportData' /></h3>
				<div>
					<div class="form-item form-item-progress">
						<div class="form-item-value export-state-aware">
							<span class="form-item-progress-label">
								<@spring.message code='dataExport.exportProgress' />
							</span>
							<div id="${pageId}-progress"></div>
							<div id="${pageId}-progress-percent" class="progress-percent"></div>
						</div>
					</div>
					<div class="form-item form-item-table">
						<div class="table-operation-wrapper">
							<button type="button" class="table-add-item-button edit-state-aware"><@spring.message code='add' /></button>
							<button type="button" class="table-cancel-export-button export-state-aware"><@spring.message code='cancel' /></button>
						</div>
						<div class="file-encoding-wrapper">
							<span class="file-encoding-label">
								<@spring.message code='dataExchange.fileEncoding' />
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
	
	po.cometdInitIfNot();
	
	//计算表格高度
	po.calTableHeight = function()
	{
		var height =  po.element(".form-content-wizard > .content").height() - po.element(".form-item-progress").outerHeight(true) - 60;
		return height;
	};
	
	po.element(".form-content").steps(
	{
		headerTag: "h3",
		bodyTag: "div",
		onStepChanged : function(event, currentIndex, priorIndex)
		{
			if(currentIndex == 1)
			{
				po.table().DataTable().columns.adjust();
				$.updateDataTableHeight(po.table(), po.calTableHeight());
			}
		},
		onFinished : function(event, currentIndex)
		{
			po.element("#${pageId}-form").submit();
		},
		labels:
		{
			previous: "<@spring.message code='wizard.previous' />",
			next: "<@spring.message code='wizard.next' />",
			finish: "<@spring.message code='export' />"
		}
	});

	$.initButtons(po.element());
	po.element("#${pageId}-binaryFormat").buttonset();
	po.element("#${pageId}-nullForIllegalColumnValue").buttonset();
	po.element("select[name='fileEncoding']").selectmenu({ appendTo : po.element(), classes : { "ui-selectmenu-menu" : "file-encoding-selectmenu-menu" } });
	
	po.element("input[name='dataFormat.binaryFormat'][value='${defaultDataFormat.binaryFormat}']").click();
	po.element("#${pageId}-nullForIllegalColumnValue-1").click();

	po.toggleEditAndExportStatus = function(exportStatus)
	{
		var exportActionEle = po.element("#${pageId}-form .wizard .actions ul li:eq(2)");
		
		if(exportStatus)
		{
			po.element(".edit-state-aware").hide();
			po.element(".export-state-aware").show();
			exportActionEle.addClass("ui-state-disabled");
			po.element("select[name='fileEncoding']").selectmenu("disable");
			po.element(".file-encoding-label").addClass("ui-state-disabled");
		}
		else
		{
			po.element(".edit-state-aware").show();
			po.element(".export-state-aware").hide();
			exportActionEle.removeClass("ui-state-disabled");
			po.element("select[name='fileEncoding']").selectmenu("enable");
			po.element(".file-encoding-label").removeClass("ui-state-disabled");
		}
	};
	
	po.renderColumn = function(data, type, row, meta)
	{
		return $.escapeHtml($.truncateIf(data));
	};
	
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
			title : "<@spring.message code='dataImport.importStatusWithSuccessFail' />",
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
	
	po.element(".restart-button").click(function()
	{
		po.toggleEditAndExportStatus(false);
		po.setDataExchangeProgress(0);
		po.toggleRestartStatus(false);
	});
	
	po.element("#${pageId}-form").submit(function()
	{
		po.cometdExecuteAfterSubscribe(po.dataExchangeChannelId,
		function()
		{
			po.element("#${pageId}-form").ajaxSubmit(
			{
				url : "${contextPath}/dataexchange/" + po.schemaId +"/export/csv/doExport",
				success: function(data)
				{
					po.toggleEditAndExportStatus(true);
				}
			});
		},
		function(message)
		{
			po.handleDataExchangeCometdMessage(message);
		});
		
		return false;
	});
	
	po.toggleEditAndExportStatus(false);
	po.setDataExchangeProgress(0);
	po.toggleRestartStatus(false);
})
(${pageId});
</script>
</body>
</html>

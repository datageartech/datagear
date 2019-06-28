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
					<div class="form-item form-item-table-head form-item-add page-status-aware-show edit-status-show">
						<div class="form-item-value">
							<div id="${pageId}-add-group">
								<button type="button" class="table-add-item-button edit-state-aware ui-corner-left"><@spring.message code='add' /></button>
								<select id="${pageId}-add-group-select">
									<option value="addAll"><@spring.message code='dataExport.addAllTable' /></option>
								</select>
							</div>
						</div>
					</div>
					<div class="form-item form-item-table-head form-item-progress page-status-aware-show exchange-status-show finish-status-show">
						<div class="form-item-value">
							<label><@spring.message code='dataExport.exportProgress' /></label>
							<div id="${pageId}-progress"></div>
							<div id="${pageId}-progress-percent" class="progress-percent"></div>
						</div>
					</div>
					<div class="form-item form-item-table">
						<div class="table-operation-wrapper">
							<button type="button" class="table-delete-item-button page-status-aware-show edit-status-show"><@spring.message code='delete' /></button>
							<button type="button" class="table-cancel-export-button page-status-aware-show exchange-status-show"><@spring.message code='cancel' /></button>
							<button type="button" class="table-download-all-button page-status-aware-show finish-status-show"><@spring.message code='downloadAll' /></button>
						</div>
						<div class="file-encoding-wrapper">
							<span class="file-encoding-label page-status-aware-enable edit-status-enable">
								<@spring.message code='dataExport.exportFileEncoding' />
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
<#include "../include/page_obj_cometd.ftl">
<#include "../include/page_obj_format_time.ftl" >
<#include "include/dataExchange_js.ftl" >
<script type="text/javascript">
(function(po)
{
	po.dataExchangeChannelId = "${dataExchangeChannelId}";
	po.subDataExchangeStatusColumnIndex = 3;
	
	po.cometdInitIfNot();
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
			finish: "<@spring.message code='export' />"
		}
	});
	
	po.element("#${pageId}-form .wizard .actions ul li:eq(2)").addClass("page-status-aware-enable edit-status-enable");
	
	$.initButtons(po.element());
	po.element("#${pageId}-binaryFormat").buttonset();
	po.element("#${pageId}-nullForIllegalColumnValue").buttonset();
	po.element("#${pageId}-add-group-select").selectmenu(
	{
		classes : {"ui-selectmenu-button": "ui-button-icon-only ui-corner-right"},
		select : function(event, ui)
		{
			if(ui.item.value == "addAll")
				po.addAllTable();
		}
	});
	po.element("select[name='fileEncoding']").selectmenu({ appendTo : po.element(), classes : { "ui-selectmenu-menu" : "file-encoding-selectmenu-menu" } });
	po.element("#${pageId}-add-group").controlgroup();
	
	po.element("input[name='dataFormat.binaryFormat'][value='${defaultDataFormat.binaryFormat}']").click();
	po.element("#${pageId}-nullForIllegalColumnValue-1").click();
	
	po.addSubDataExchange = function(query, fileName)
	{
		if(query == null)
			query="";
		if(fileName == null)
			fileName = "";
		
		if(!po.nextSubDataExchangeIdSeq)
			po.nextSubDataExchangeIdSeq = 0;
		
		var subDataExchangeId = po.dataExchangeId + "_" + (po.nextSubDataExchangeIdSeq++);
		
		po.addRowData({subDataExchangeId : subDataExchangeId, query : query, fileName : fileName, status : ""});
	};
	
	po.addAllTable = function()
	{
		if(po._addAllTableDoing)
			return;
		
		po._addAllTableDoing = true;
		
		$.ajax(
		{
			url : "${contextPath}/dataexchange/" + po.schemaId +"/getAllTableNames",
			success : function(tableNames)
			{
				if(!tableNames)
					return;
				
				for(var i=0; i<tableNames.length; i++)
					po.addSubDataExchange(tableNames[i], $.toValidFileName(tableNames[i])+".csv");
			},
			complete : function()
			{
				po._addAllTableDoing = false;
			}
		});
	};
	
	po.updateSubDataExchangeStatusForCometdMessageSuper = po.updateSubDataExchangeStatusForCometdMessage;
	po.updateSubDataExchangeStatusForCometdMessage = function(subDataExchangeId, status, message)
	{
		var type = (message ? message.type : "");
		
		if("SubSuccessWithCount" == type)
		{
			if(!message.failCount || message.failCount == 0)
			{
				var spanIndex = status.indexOf("<span");
				if(spanIndex > 0)
					status = status.substring(0, spanIndex);
			}
			
			status += "<span class='exchange-result-icon exchange-download-icon' title='"+$.escapeHtml("<@spring.message code='download' />")+"' subDataExchangeId='"+$.escapeHtml(message.subDataExchangeId)+"' >"
				+"<span class='ui-icon ui-icon-circle-arrow-s'></span></span>";
		}
		
		po.updateSubDataExchangeStatusForCometdMessageSuper(subDataExchangeId, status, message);
	};
	
	po.expectedResizeDataTableElements = [po.table()[0]];
	
	var tableColumns = [
		{
			title : "<@spring.message code='dataExport.tableNameOrQueryStatement' />",
			data : "query",
			render : function(data, type, row, meta)
			{
				return "<input type='hidden' name='subDataExchangeIds' value='"+$.escapeHtml(row.subDataExchangeId)+"' />"
						+ "<input type='text' name='queries' value='"+$.escapeHtml(data)+"' class='query-input input-in-table ui-widget ui-widget-content' style='width:90%' />";
			},
			defaultContent: "",
			width : "50%",
		},
		{
			title : "<@spring.message code='dataExport.exportFileName' />",
			data : "fileName",
			render : function(data, type, row, meta)
			{
				return "<input type='text' name='fileNames' value='"+$.escapeHtml(data)+"' class='file-name-input input-in-table ui-widget ui-widget-content' style='width:90%' />";
			},
			defaultContent: "",
			width : "23%"
		},
		{
			title : $.buildDataTablesColumnTitleWithTip("<@spring.message code='dataExport.exportProgress' />", "<@spring.message code='dataExport.exportStatusWithSuccessFail' />"),
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
	
	po.element(".table-add-item-button").click(function()
	{
		po.addSubDataExchange();
		
		//滚动到底部
		var $dataTableParent = po.dataTableParent();
		$dataTableParent.scrollTop($dataTableParent.prop("scrollHeight"));
	});
	
	po.element(".table-delete-item-button").click(function()
	{
		po.executeOnSelects(function(rowDatas, rowIndexes)
		{
			po.deleteRow(rowIndexes);
		});
	});
	
	po.element(".table-cancel-export-button").click(function()
	{
		po.cancelSelectedSubDataExchange();
	});
	
	po.element(".table-download-all-button").click(function()
	{
		po.open("${contextPath}/dataexchange/" + po.schemaId +"/export/downloadAll",
		{
			target : "_file",
			data :
			{
				dataExchangeId : po.dataExchangeId
			}
		});
	});
	
	po.table().on("click", ".input-in-table", function(event)
	{
		//阻止行选中
		event.stopPropagation();
	});
	
	po.table().on("click", ".exchange-result-icon", function(event)
	{
		//阻止行选中
		event.stopPropagation();
		
		var $this = $(this);
		
		if($this.hasClass("exchange-error-icon"))
		{
			var subDataExchangeId = $this.attr("subDataExchangeId");
			po.viewSubDataExchangeDetailLog(subDataExchangeId);
		}
		else if($this.hasClass("exchange-download-icon"))
		{
			var subDataExchangeId = $this.attr("subDataExchangeId");
			var fileName = (po.subDataExchangeFileNameMap ? po.subDataExchangeFileNameMap[subDataExchangeId] : null);
			
			if(fileName)
			{
				po.open("${contextPath}/dataexchange/" + po.schemaId +"/export/download",
				{
					target : "_file",
					data :
					{
						dataExchangeId : po.dataExchangeId,
						fileName : fileName
					}
				});
			}
		}
	});
	
	po.element(".restart-button").click(function()
	{
		po.updateDataExchangePageStatus("edit");
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
					po.subDataExchangeFileNameMap = data.data;
					po.updateDataExchangePageStatus("exchange");
				}
			});
		},
		function(message)
		{
			po.handleDataExchangeCometdMessage(message);
		});
		
		return false;
	});
	
	po.updateDataExchangePageStatus("edit");
})
(${pageId});
</script>
</body>
</html>

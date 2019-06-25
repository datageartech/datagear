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
<div id="${pageId}" class="page-dataimport-text page-dataimport-csv">
	<div class="head">
		<@spring.message code='dataimport.importCsvData' />
	</div>
	<div class="content">
		<form id="${pageId}-form" action="#" method="POST">
			<input type="hidden" name="importId" value="${importId}" />
			<div class="form-content form-content-wizard">
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
							<input id="${pageId}-ignoreInexistentColumn-0" type="radio" name="importOption.ignoreInexistentColumn" value="true" />
							<label for="${pageId}-ignoreInexistentColumn-1"><@spring.message code='no' /></label>
							<input id="${pageId}-ignoreInexistentColumn-1" type="radio" name="importOption.ignoreInexistentColumn" value="false" />
							</div>
						</div>
					</div>
					<div class="form-item">
						<div class="form-item-label"><@spring.message code='dataimport.nullForIllegalColumnValue' /></div>
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
						<div class="form-item-label"><@spring.message code='dataimport.exceptionResolve' /></div>
						<div class="form-item-value">
							<div id="${pageId}-exceptionResolve">
								<label for="${pageId}-exceptionResolve-0"><@spring.message code='dataimport.exceptionResolve.ROLLBACK' /></label>
								<input id="${pageId}-exceptionResolve-0" type="radio" name="importOption.exceptionResolve" value="ROLLBACK" />
								<label for="${pageId}-exceptionResolve-1"><@spring.message code='dataimport.exceptionResolve.ABORT' /></label>
								<input id="${pageId}-exceptionResolve-1" type="radio" name="importOption.exceptionResolve" value="ABORT" />
								<label for="${pageId}-exceptionResolve-2"><@spring.message code='dataimport.exceptionResolve.IGNORE' /></label>
								<input id="${pageId}-exceptionResolve-2" type="radio" name="importOption.exceptionResolve" value="IGNORE" />
							</div>
						</div>
					</div>
				</div>
				<h3><@spring.message code='dataimport.uploadAndImportData' /></h3>
				<div>
					<div class="form-item form-item-upload upload-state-aware">
						<div class="form-item-value">
							<span class="form-item-upload-label">
								<@spring.message code='dataimport.uploadCsvDataFile' />
							</span>
							<div class="fileinput-button ui-widget ui-button ui-corner-all"><@spring.message code='upload' /><input type="file"></div>
							<div class="file-info"></div>
						</div>
					</div>
					<div class="form-item form-item-progress import-state-aware">
						<div class="form-item-value">
							<span class="form-item-progress-label">
								<@spring.message code='dataimport.importProgress' />
							</span>
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
								<@spring.message code='dataimport.fileEncoding' />
							</span>
							<select name="fileEncoding">
								<option value="GBK">GBK</option>
								<option value="UTF-8">UTF-8</option>
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
		<div id="${pageId}-import-exception-tooltip" title="import tooltip" style="width:0; height:0;"></div>
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
<script type="text/javascript">
(function(po)
{
	po.schemaId = "${schema.id}";
	po.importId = "${importId}";
	po.importChannelId = "${importChannelId}";
	po.form = po.element("#${pageId}-form");
	po.fileUploadInfo = function(){ return this.element(".file-info"); };
	
	po.cometdInitIfNot();
	
	//计算表格高度
	po.calTableHeight = function()
	{
		var height =  po.element(".form-content-wizard > .content").height() - po.element(".form-item-upload").outerHeight(true) - 60;
		
		return height;
	};
	
	po.renderUploadFiles = function(fileInfos)
	{
		po.addRowData(fileInfos);
	};
	
	po.setImportProgress = function(progressNumber, duration)
	{
		po.element("#${pageId}-progress").progressbar({ value: progressNumber });
		
		var percentText = progressNumber + "%";
		
		if(duration != null)
		{
			var duration = po.formatDuration(duration);
			
			<#assign messageArgs=['"+progressNumber+"', '"+duration+"'] />
			percentText = "<@spring.messageArgs code='dataimport.importProgressPercentWithDuration' args=messageArgs />";
		}
		
		po.element("#${pageId}-progress-percent").text(percentText);
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
	
	po.toggleRestartStatus = function(enable)
	{
		if(enable)
		{
			po.element(".restart-wrapper").show();
			po.element(".restart-button").removeClass("ui-state-disabled");
		}
		else
		{
			po.element(".restart-wrapper").hide();
			po.element(".restart-button").addClass("ui-state-disabled");
		}
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
			finish: "<@spring.message code='import' />"
		}
	});

	$.initButtons(po.element());
	po.element("#${pageId}-binaryFormat").buttonset();
	po.element("#${pageId}-ignoreInexistentColumn").buttonset();
	po.element("#${pageId}-nullForIllegalColumnValue").buttonset();
	po.element("#${pageId}-exceptionResolve").buttonset();
	po.element("select[name='fileEncoding']").selectmenu();
	
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
		po.executeOnSelects(function(rowDatas, rowIndexes)
		{
			for(var i=0; i<rowDatas.length; i++)
			{
				var importProgress = rowDatas[i].importProgress;
				
				if(!importProgress || importProgress.indexOf("waiting") > -1)
				{
					//TODO 发送取消命令
					alert("发送取消命令");
				}
				else
				{
					if(rowDatas.length == 1)
						$.tipInfo("<@spring.message code='dataimport.cancelImportDeniedWithReason' />");
				}
			}
		});
	});
	
	po.element(".restart-button").click(function()
	{
		po.toggleUploadAndImportStatus(false);
		po.setImportProgress(0);
		po.toggleRestartStatus(false);
	});
	
	po.renderColumn = function(data, type, row, meta)
	{
		return $.escapeHtml($.truncateIf(data));
	};
	
	po.getSubDataExchangeRowData = function(subDataExchangeId)
	{
		var dataTable = po.table().DataTable();
		var rowIndex = po.getSubDataExchangeRowIndex(dataTable, subDataExchangeId);
		
		if(rowIndex < 0)
			return null;
		
		return dataTable.row(rowIndex).data();
	};
	
	po.getSubDataExchangeRowIndex = function(dataTable, subDataExchangeId)
	{
		var rowIndex = -1;
		
		var rowDatas = dataTable.rows().data();
		for(var i=0; i<rowDatas.length; i++)
		{
			if(rowDatas[i].id == subDataExchangeId)
			{
				rowIndex = i;
				break;
			}
		}
		
		return rowIndex;
	};
	
	po.updateSubDataExchangeStatus = function(subDataExchangeId, status)
	{
		var dataTable = po.table().DataTable();
		
		var rowIndex = po.getSubDataExchangeRowIndex(dataTable, subDataExchangeId);
		
		if(rowIndex < 0)
			return false;
		
		var cellIndex = { "row" : rowIndex, "column" : 4 };
		var cell = dataTable.cell(cellIndex);
		cell.data(status).draw();
		
		return true;
	};
	
	po.expectedResizeDataTableElements = [po.table()[0]];
	
	var tableColumns = [
		{
			title : "<@spring.message code='dataimport.importFileName' />",
			data : "displayName",
			render : function(data, type, row, meta)
			{
				return po.renderColumn(data, type, row, meta)
					+ "<input type='hidden' name='fileIds' value='"+$.escapeHtml(row.id)+"' />"
					+ "<input type='hidden' name='fileNames' value='"+$.escapeHtml(row.name)+"' />";
			},
			defaultContent: "",
			width : "35%",
		},
		{
			title : "<@spring.message code='dataimport.importFileSize' />",
			data : "size",
			render : po.renderColumn,
			defaultContent: "",
			width : "13%"
		},
		{
			title : "<@spring.message code='dataimport.importTableName' />",
			data : "tableName",
			render : function(data, type, row, meta)
			{
				return "<input type='text' name='tableNames' value='"+$.escapeHtml(data)+"' class='table-name-input ui-widget ui-widget-content' style='width:90%' />";
			},
			defaultContent: "",
			width : "25%"
		},
		{
			title : "<@spring.message code='dataimport.importStatusWithSuccessFail' />",
			data : "status",
			render : function(data, type, row, meta)
			{
				if(!data)
					return "<@spring.message code='dataimport.importStatus.Unstart' />";
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
	
	po.table().on("click", ".import-result-icon", function(event)
	{
		//阻止行选中
		event.stopPropagation();
		
		var $this = $(this);
		
		if(!$this.hasClass("ui-state-error"))
			return;
		
		var subDataExchangeId = $this.attr("subDataExchangeId");
		var rowData = po.getSubDataExchangeRowData(subDataExchangeId);
		var displayName = $.escapeHtml((rowData ? rowData.displayName : ""));
		
		<#assign messageArgs=['"+displayName+"'] />
		
		po.open("${contextPath}/dataexchange/" + po.schemaId +"/viewLog",
		{
			title : "<@spring.messageArgs code='dataImport.viewImportLog' args=messageArgs />",
			data :
			{
				schemaId : po.schemaId,
				dataExchangeId : po.importId,
				subDataExchangeId : subDataExchangeId,
				subDataExchangeDisplayName : displayName
			},
			height : $(window).height() * 0.75
		});
	});
	
	po.showExceptionTip = function(event, tipEle)
	{
		tipEle = $(tipEle);
		
		var subDataExchangeId = tipEle.attr("subDataExchangeId");
		var exception = po.subDataExchangeExceptionMessages[subDataExchangeId];
		
		//IGNORE模式是没有exception
		if(!exception)
			return;
		
		var $tooltip = po.element("#${pageId}-import-exception-tooltip");
		
		try{ $tooltip.tooltip("destroy"); }catch(e){}
		po.element("#${pageId}-import-exception-tooltip").tooltip({"classes" : { "ui-tooltip" : "import-exception-tooltip ui-state-error ui-corner-all ui-widget-shadow"}});
		$tooltip.tooltip("option", "content", exception);
		$tooltip.tooltip("option", "position", { my: "center top", at: "center bottom-1", of: tipEle, collision: "flipfit" });
		$tooltip.tooltip("open");
	};
	
	po.hideExceptionTip = function(event, tipEle)
	{
		po.element("#${pageId}-import-exception-tooltip").tooltip("close");
	};
	
	po.handleCometdMessage = function(message)
	{
		message = message.data;
		var type = (message ? message.type : "");
		
		if("Start" == type)
		{
			var dataTable = po.table().DataTable();
			po.subDataExchangeCount = dataTable.rows().indexes().length;
			po.subDataExchangeFinishCount=0;
			
			po.subDataExchangeExceptionMessages = {};
		}
		else if("Exception" == type)
		{
			<#assign messageArgs=['"+message.content+"'] />
			$.tipError("<@spring.messageArgs code='dataimport.importStatus.Exception' args=messageArgs />");
		}
		else if("SubmitSuccess" == type)
		{
			po.updateSubDataExchangeStatus(message.subDataExchangeId,
					"<@spring.message code='dataimport.importStatus.SubmitSuccess' />");
		}
		else if("SubmitFail" == type)
		{
			po.updateSubDataExchangeStatus(message.subDataExchangeId,
				"<@spring.message code='dataimport.importStatus.SubmitFail' />");
		}
		else if("CancelSuccess" == type)
		{
			po.updateSubDataExchangeStatus(message.subDataExchangeId,
				"<@spring.message code='dataimport.importStatus.CancelSuccess' />");
		}
		else if("TextImportSubImporting" == type)
		{
			var status = "";
			
			<#assign messageArgs=['"+message.successCount+"', '"+message.ignoreCount+"'] />
			status = "<@spring.messageArgs code='dataimport.importStatus.TextImportSubImporting' args=messageArgs />";
			
			po.updateSubDataExchangeStatus(message.subDataExchangeId, status);
		}
		else if("TextImportSubException" == type)
		{
			var exceptionResolve = message.exceptionResolve;
			
			var duration = po.formatDuration(message.duration);
			
			var status = "";
			
			<#assign messageArgs=['"+message.successCount+"', '"+message.ignoreCount+"', '"+duration+"', '"+message.content+"'] />
			
			//未进行任何实际导入操作
			if(message.successCount == 0 && message.ignoreCount == 0)
				status = "<@spring.messageArgs code='dataimport.importStatus.TextImportSubException' args=messageArgs />";
			else if(exceptionResolve == "ABORT")
				status = "<@spring.messageArgs code='dataimport.importStatus.TextImportSubException.ABORT' args=messageArgs />";
			else if(exceptionResolve == "IGNORE")
				status = "<@spring.messageArgs code='dataimport.importStatus.TextImportSubException.IGNORE' args=messageArgs />";
			else if(exceptionResolve == "ROLLBACK")
				status = "<@spring.messageArgs code='dataimport.importStatus.TextImportSubException.ROLLBACK' args=messageArgs />";
			
			status += "<span class='import-result-icon ui-state-error' onmouseover='${pageId}.showExceptionTip(event, this)'"
						+" onmouseout='${pageId}.hideExceptionTip(event, this)' subDataExchangeId='"+$.escapeHtml(message.subDataExchangeId)+"' >"
						+"<span class='ui-icon ui-icon-info'></span></span>";
			
			po.subDataExchangeExceptionMessages[message.subDataExchangeId] = message.content;
			
			po.updateSubDataExchangeStatus(message.subDataExchangeId, status);
		}
		else if("TextImportSubSuccess" == type)
		{
			var duration = po.formatDuration(message.duration);
			
			var status = "";
			
			<#assign messageArgs=['"+message.successCount+"', '"+message.ignoreCount+"', '"+duration+"'] />
			
			if(message.ignoreCount == 0)
			{
				status = "<@spring.messageArgs code='dataimport.importStatus.TextImportSubSuccess' args=messageArgs />";
				status += "<span class='import-result-icon'>"
						+"<span class='ui-icon ui-icon-circle-check'></span></span>";
			}
			else
			{
				status = "<@spring.messageArgs code='dataimport.importStatus.TextImportSubException.IGNORE' args=messageArgs />";
				status += "<span class='import-result-icon ui-state-error' onmouseover='${pageId}.showExceptionTip(event, this)'"
						+" onmouseout='${pageId}.hideExceptionTip(event, this)' subDataExchangeId='"+$.escapeHtml(message.subDataExchangeId)+"' >"
						+"<span class='ui-icon ui-icon-info'></span></span>";
				
				po.subDataExchangeExceptionMessages[message.subDataExchangeId] = message.ignoreException;
			}
			
			po.updateSubDataExchangeStatus(message.subDataExchangeId, status);
		}
		else if("SubFinish" == type)
		{
			if(!po.subDataExchangeFinishCount)
				po.subDataExchangeFinishCount = 0;
			
			po.subDataExchangeFinishCount += 1;
			
			po.setImportProgress(parseInt(po.subDataExchangeFinishCount/po.subDataExchangeCount * 100));
		}
		else if("Finish" == type)
		{
			po.setImportProgress(100, message.duration);
			po.toggleRestartStatus(true);
		}
	};
	
	po.element("#${pageId}-form").submit(function()
	{
		po.cometdExecuteAfterSubscribe(po.importChannelId,
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
			po.handleCometdMessage(message);
		});
		
		return false;
	});
	
	po.toggleUploadAndImportStatus(false);
	po.setImportProgress(0);
	po.toggleRestartStatus(false);
})
(${pageId});
</script>
</body>
</html>

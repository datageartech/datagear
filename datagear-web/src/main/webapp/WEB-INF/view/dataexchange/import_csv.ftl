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
								<label for="${pageId}-exceptionResolve-0"><@spring.message code='dataimport.exceptionResolve.ABORT' /></label>
								<input id="${pageId}-exceptionResolve-0" type="radio" name="importOption.exceptionResolve" value="ABORT" />
								<label for="${pageId}-exceptionResolve-1"><@spring.message code='dataimport.exceptionResolve.IGNORE' /></label>
								<input id="${pageId}-exceptionResolve-1" type="radio" name="importOption.exceptionResolve" value="IGNORE" />
								<label for="${pageId}-exceptionResolve-2"><@spring.message code='dataimport.exceptionResolve.ROLLBACK' /></label>
								<input id="${pageId}-exceptionResolve-2" type="radio" name="importOption.exceptionResolve" value="ROLLBACK" />
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
							<span id="${pageId}-progress-percent" class="progress-percent"></span>
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
	</div>
	<div class="foot">
	</div>
</div>
<#if !isAjaxRequest>
</div>
</#if>

<#include "../include/page_js_obj.ftl">
<#include "../include/page_obj_grid.ftl">
<script type="text/javascript">
(function(po)
{
	po.schemaId = "${schema.id}";
	po.importId = "${importId}";
	po.form = po.element("#${pageId}-form");
	po.fileUploadInfo = function(){ return this.element(".file-info"); };

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
	
	po.formatImportProgress = function(progress)
	{
		if(!progress)
			return "<@spring.message code='dataimport.importStatus.unstart' />";
		
		var re = "";
		
		if(progress.indexOf("waiting") > -1)
			re += "<@spring.message code='dataimport.importStatus.waiting' />";
		else if(progress.indexOf("importing") > -1)
			re += "<@spring.message code='dataimport.importStatus.importing' />";
		else if(progress.indexOf("cancel") > -1)
			re += "<@spring.message code='dataimport.importStatus.cancel' />";
		else if(progress.indexOf("abort") > -1)
			re += "<@spring.message code='dataimport.importStatus.abort' />";
		else if(progress.indexOf("rollback") > -1)
			re += "<@spring.message code='dataimport.importStatus.rollback' />";
		else if(progress.indexOf("finish") > -1)
			re += "<@spring.message code='dataimport.importStatus.finish' />";
		
		var leftBracketIdx = progress.indexOf("(");
		var slashIdx = (leftBracketIdx > -1 ? progress.indexOf("/", leftBracketIdx+1) : -1);
		var rightBracketIdx = (slashIdx > -1 ? progress.indexOf(")", slashIdx+1) : -1);
		
		if(slashIdx > leftBracketIdx + 1 && rightBracketIdx > slashIdx + 1)
		{
			var successCount = progress.substring(leftBracketIdx + 1, slashIdx);
			var failCount = progress.substring(slashIdx + 1, rightBracketIdx);
			
			<#assign messageArgs=['"+re+"', '"+successCount+"', '"+failCount+"'] />
			re = "<@spring.messageArgs code='dataimport.importProgressInfo' args=messageArgs />";
		}
		
		return re;
	}
	
	po.setImportProgress = function(progressNumber)
	{
		po.element("#${pageId}-progress").progressbar({ value: progressNumber });
		po.element("#${pageId}-progress-percent").text(progressNumber + "%");
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
	po.element("#${pageId}-exceptionResolve-2").click();

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
	
	po.renderColumn = function(data, type, row, meta)
	{
		return $.escapeHtml($.truncateIf(data));
	};
	
	po.updateTableImportProgress = function(dataTable, rowIndexes, progressValue)
	{
		var cellIndexes = [];
		
		for(var i=0; i<rowIndexes.length; i++)
		{
			var cellIndex = { "row" : rowIndexes[i], "column" : 4 };
			var cell = dataTable.cell(cellIndex);
			cell.data($.isArray(progressValue) ? progressValue[i] : progressValue);
			
			cellIndexes[i] = cellIndex;
		}
		
		dataTable.cells(cellIndexes).draw();
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
				return "<input type='text' name='tableNames' value='"+$.escapeHtml(data)+"' class='ui-widget ui-widget-content' style='width:90%' />";
			},
			defaultContent: "",
			width : "25%"
		},
		{
			title : "<@spring.message code='dataimport.importProgressWithSuccessFail' />",
			data : "importProgress",
			render : function(data, type, row, meta)
			{
				return po.formatImportProgress(data);
			},
			defaultContent: "",
			width : "27%"
		}
	];
	var tableSettings = po.buildDataTableSettingsLocal(tableColumns, [], {"order": []});
	po.initDataTable(tableSettings);
	po.bindResizeDataTable();
	
	po.element("#${pageId}-form").ajaxForm(
	{
		url : "${contextPath}/dataexchange/" + po.schemaId +"/import/csv/doImport",
		success: function(data)
		{
			po.toggleUploadAndImportStatus(true);
			
			var dataTable = po.table().DataTable();
			po.updateTableImportProgress(dataTable, dataTable.rows().indexes(), "waiting");
		}
	});
	
	po.toggleUploadAndImportStatus(false);
	po.setImportProgress(0);
})
(${pageId});
</script>
</body>
</html>

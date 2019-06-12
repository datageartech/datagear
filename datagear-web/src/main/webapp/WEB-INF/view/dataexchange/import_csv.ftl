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
					<div class="form-item form-item-upload">
						<div class="form-item-value">
							<span class="form-item-upload-label">
								<@spring.message code='dataimport.uploadCsvDataFile' />
							</span>
							<div class="fileinput-button ui-widget ui-button ui-corner-all"><@spring.message code='upload' /><input type="file"></div>
							<div class="file-info"></div>
						</div>
					</div>
					<div class="form-item form-item-table">
						<div class="table-operation-wrapper">
							<button type="button" class="table-delete-item-button"><@spring.message code='delete' /></button>
						</div>
						<div class="table-wrapper minor-dataTable">
							<table id="${pageId}-table" width="100%" class="hover stripe"></table>
						</div>
					</div>
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
<#include "../include/page_obj_grid.ftl">
<script type="text/javascript">
(function(po)
{
	po.schemaId = "${schema.id}";
	po.importId = "${importId}";
	po.form = po.element("#${pageId}-form");
	po.fileUploadInfo = function(){ return this.element(".file-info"); };

	$.initButtons(po.element());
	
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
		po.deleteSelectedRows();
	});
	
	po.renderColumn = function(data, type, row, meta)
	{
		return $.escapeHtml($.truncateIf(data));
	};
	
	po.expectedResizeDataTableElements = [po.table()[0]];
	
	var tableColumns = [
		{
			title : "name",
			data : "name",
			visible : false,
			render : function(data, type, row, meta)
			{
				return po.renderColumn(data, type, row, meta) + "<input type='hidden' name='fileName' value='"+$.escapeHtml(data)+"' />";
			},
			defaultContent: ""
		},
		{
			title : "<@spring.message code='dataimport.importFileName' />",
			data : "displayName",
			render : po.renderColumn,
			defaultContent: ""
		},
		{
			title : "<@spring.message code='dataimport.importFileSize' />",
			data : "size",
			render : po.renderColumn,
			defaultContent: ""
		},
		{
			title : "<@spring.message code='dataimport.importTableName' />",
			data : "tableName",
			render : function(data, type, row, meta)
			{
				return "<input type='text' name='tableName' value='"+$.escapeHtml(data)+"' class='ui-widget ui-widget-content' style='width:90%' />";
			},
			defaultContent: ""
		}
	];
	var tableSettings = po.buildDataTableSettingsLocal(tableColumns, [], {"order": []});
	po.initDataTable(tableSettings);
	po.bindResizeDataTable();
})
(${pageId});
</script>
</body>
</html>

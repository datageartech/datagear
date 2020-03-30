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
	<@spring.message code='sqlpad.sqlpad' />
	<@spring.message code='bracketLeft' />
	${schema.title?html}
	<@spring.message code='bracketRight' />
</title>
</head>
<body class="fill-parent">
<#if !isAjaxRequest>
<div class="fill-parent">
</#if>
<div id="${pageId}" class="page-sqlpad">
	<div class="head button-operation">
		<button id="executeSqlButton" class="ui-button ui-corner-all ui-widget ui-button-icon-only first" title="<@spring.message code='sqlpad.executeWithShortcut' />"><span class="ui-button-icon ui-icon ui-icon-play"></span><span class="ui-button-icon-space"> </span><@spring.message code='execute' /></button>
		<button id="stopSqlButton" class="ui-button ui-corner-all ui-widget ui-button-icon-only" title="<@spring.message code='sqlpad.stopExecution' />"><span class="ui-button-icon ui-icon ui-icon-stop"></span><span class="ui-button-icon-space"> </span><@spring.message code='sqlpad.stopExecution' /></button>
		<div class="button-divider ui-widget ui-widget-content"></div>
		<button id="commitSqlButton" class="ui-button ui-corner-all ui-widget ui-button-icon-only" title="<@spring.message code='sqlpad.commit' />"><span class="ui-button-icon ui-icon ui-icon-check"></span><span class="ui-button-icon-space"> </span><@spring.message code='sqlpad.commit' /></button>
		<button id="rollbackSqlButton" class="ui-button ui-corner-all ui-widget ui-button-icon-only" title="<@spring.message code='sqlpad.rollback' />"><span class="ui-button-icon ui-icon ui-icon-arrowreturnthick-1-w"></span><span class="ui-button-icon-space"> </span><@spring.message code='sqlpad.rollback' /></button>
		<div class="button-divider ui-widget ui-widget-content"></div>
		<#--禁用插入文件功能，因为没有应用场景
		<div class="insert-file-wrapper item">
			<div id="insertFileButton" class="ui-button ui-corner-all ui-widget ui-button-icon-only fileinput-button" title="<@spring.message code='sqlpad.insertFile' />"><span class="ui-button-icon ui-icon ui-icon ui-icon-document"></span><span class="ui-button-icon-space"> </span><@spring.message code='sqlpad.insertFile' /><input type="file"></div>
			<div class="insert-file-info ui-widget ui-widget-content ui-corner-all ui-widget-shadow ui-front upload-file-info"></div>
		</div>
		-->
		<input id="sqlDelimiterInput" type="text" class="sql-delimiter-input ui-widget ui-widget-content ui-corner-all" value=";"  title="<@spring.message code='sqlpad.sqlDelimiter' />"/>
		<button id="insertSqlDelimiterDefineButton" class="ui-button ui-corner-all ui-widget ui-button-icon-only" title="<@spring.message code='sqlpad.insertSqlDelimiterDefine' />"><span class="ui-button-icon ui-icon ui-icon-grip-dotted-horizontal"></span><span class="ui-button-icon-space"> </span><@spring.message code='sqlpad.insertSqlDelimiterDefine' /></button>
		<button id="insertSqlDelimiterButton" class="ui-button ui-corner-all ui-widget ui-button-icon-only" title="<@spring.message code='sqlpad.insertSqlDelimiter' />"><span class="ui-button-icon ui-icon ui-icon-grip-solid-horizontal"></span><span class="ui-button-icon-space"> </span><@spring.message code='sqlpad.insertSqlDelimiter' /></button>
		<div class="button-divider ui-widget ui-widget-content"></div>
		<button id="clearSqlButton" class="ui-button ui-corner-all ui-widget ui-button-icon-only" title="<@spring.message code='sqlpad.clearEditSql' />"><span class="ui-button-icon ui-icon ui-icon-trash"></span><span class="ui-button-icon-space"> </span><@spring.message code='sqlpad.clearEditSql' /></button>
		<div class="setting-wrapper">
			<button id="settingButton" class="ui-button ui-corner-all ui-widget ui-button-icon-only" title="<@spring.message code='sqlpad.setting' />"><span class="ui-button-icon ui-icon ui-icon-caret-1-s"></span><span class="ui-button-icon-space"> </span><@spring.message code='sqlpad.setting' /></button>
			<div class="setting-panel ui-widget ui-widget-content ui-corner-all ui-widget-shadow ui-front">
				<form id="settingForm" method="POST" action="#">
					<div class="form-content">
						<div class="form-item">
							<div class="form-item-label"><label><@spring.message code='sqlpad.sqlCommitMode' /></label></div>
							<div class="form-item-value">
								<div id="sqlCommitModeSet" class="ui-corner-all">
									<input type="radio" id="${pageId}-sqlcm-0" name="sqlCommitMode" value="AUTO"><label for="${pageId}-sqlcm-0"><@spring.message code='sqlpad.sqlCommitMode.auto' /></label>
									<input type="radio" id="${pageId}-sqlcm-1" name="sqlCommitMode" value="MANUAL"><label for="${pageId}-sqlcm-1"><@spring.message code='sqlpad.sqlCommitMode.manual' /></label>
								</div>
							</div>
						</div>
						<div class="form-item">
							<div class="form-item-label"><label><@spring.message code='sqlpad.sqlExceptionHandleMode' /></label></div>
							<div class="form-item-value">
								<div id="sqlExceptionHandleModeSet" class="ui-corner-all">
									<input type="radio" id="${pageId}-sqlehm-0" name="sqlExceptionHandleMode" value="ABORT" checked="checked"><label for="${pageId}-sqlehm-0"><@spring.message code='sqlpad.sqlExceptionHandleMode.abort' /></label>
									<input type="radio" id="${pageId}-sqlehm-1" name="sqlExceptionHandleMode" value="IGNORE"><label for="${pageId}-sqlehm-1"><@spring.message code='sqlpad.sqlExceptionHandleMode.ignore' /></label>
									<input type="radio" id="${pageId}-sqlehm-2" name="sqlExceptionHandleMode" value="ROLLBACK"><label for="${pageId}-sqlehm-2"><@spring.message code='sqlpad.sqlExceptionHandleMode.rollback' /></label>
								</div>
							</div>
						</div>
						<div class="form-item">
							<div class="form-item-label"><label><@spring.message code='sqlpad.overTimeThreashold' /></label></div>
							<div class="form-item-value">
								<input type="text" name="overTimeThreashold" value="10" class="ui-widget ui-widget-content" style="width:4em;" title="<@spring.message code='sqlpad.overTimeThreashold.desc' />" />
								<@spring.message code='sqlpad.overTimeThreashold.unit' />
							</div>
						</div>
						<div class="form-item">
							<div class="form-item-label"><label><@spring.message code='sqlpad.resultsetFetchSize' /></label></div>
							<div class="form-item-value">
								<input type="text" name="resultsetFetchSize" value="20" class="ui-widget ui-widget-content" style="width:4em;" title="<@spring.message code='sqlpad.resultsetFetchSize.desc' />" />
							</div>
						</div>
					</div>
				</form>
			</div>
		</div>
		<div class="view-sql-history-wrapper">
			<button id="viewSqlHistoryButton" class="ui-button ui-corner-all ui-widget ui-button-icon-only" title="<@spring.message code='sqlpad.viewSqlHistory' />"><span class="ui-button-icon ui-icon ui-icon-clock"></span><span class="ui-button-icon-space"> </span><@spring.message code='sqlpad.viewSqlHistory' /></button>
			<div class="view-sql-history-panel ui-widget ui-widget-content ui-corner-all ui-widget-shadow ui-front">
				<div class="sql-history-head">
					<form id="viewSqlHistorySearchForm" method="POST" action="${contextPath}/sqlpad/${schema.id}/sqlHistoryData" class="sql-history-search-form">
						<input type="hidden" name="page" value="1" />
						<input type="hidden" name="pageSize" value="20" />
						<div class="form-content">
							<div class="form-item">
								<div class="form-item-value">
									<input type="text" name="keyword" value="" class="ui-widget ui-widget-content" maxlength="50" />
									<button type="submit"><@spring.message code='query' /></button>
								</div>
							</div>
						</div>
					</form>
					<div class="sql-history-operation">
						<button id="insertSqlHistoryToEditorButton" title="<@spring.message code='sqlpad.insertSqlHistoryToEditor' />"><@spring.message code='insert' /></button>
						<button id="copySqlHistoryToClipbordButton"  title="<@spring.message code='sqlpad.copySqlHistoryToClipbord' />"><@spring.message code='copy' /></button>
					</div>
				</div>
				<div class="sql-history-list ui-widget ui-widget-content">
				</div>
				<div class="sql-history-foot">
					<button id="sqlHistoryLoadMoreButton" class="ui-button ui-corner-all ui-widget ui-button-icon-only" title="<@spring.message code='loadMore' />"><span class="ui-button-icon ui-icon ui-icon-arrowthick-1-s"></span><span class="ui-button-icon-space"> </span><@spring.message code='loadMore' /></button>
					<button id="sqlHistoryRefreshButton" class="ui-button ui-corner-all ui-widget ui-button-icon-only" title="<@spring.message code='refresh' />"><span class="ui-button-icon ui-icon ui-icon-refresh"></span><span class="ui-button-icon-space"> </span><@spring.message code='refresh' /></button>
				</div>
			</div>
		</div>
	</div>
	<div class="content ui-widget ui-widget-content">
		<div class="content-editor">
			<div class="content-edit-content">
				<div id="${pageId}-sql-editor" class="sql-editor">${initSql!''?html}</div>
			</div>
		</div>
		<div class="content-result">
			<div id="${pageId}-sqlResultTabs" class="result-tabs minor-dataTable">
				<ul>
					<li class="result-message-tab not-closable"><a class="result-message-anchor" href="#${pageId}-resultMessage">消息</a></li>
				</ul>
				<div id="${pageId}-resultMessage" class="result-message">
				</div>
				<div class="tabs-more-operation-menu-wrapper ui-widget ui-front ui-widget-content ui-corner-all ui-widget-shadow" style="position: absolute; left:0px; top:0px; display: none;">
					<ul class="tabs-more-operation-menu">
						<li class="tab-operation-close-left"><div><@spring.message code='main.closeLeft' /></div></li>
						<li class="tab-operation-close-right"><div><@spring.message code='main.closeRight' /></div></li>
						<li class="tab-operation-close-other"><div><@spring.message code='main.closeOther' /></div></li>
						<li class="tab-operation-close-all"><div><@spring.message code='main.closeAll' /></div></li>
					</ul>
				</div>
				<div class="tabs-more-tab-menu-wrapper ui-widget ui-front ui-widget-content ui-corner-all ui-widget-shadow" style="position: absolute; left:0px; top:0px; display: none;">
					<ul class="tabs-more-tab-menu">
					</ul>
				</div>
				<div id="viewLongTextResultPanel" class="view-long-text-result-panel ui-widget ui-front ui-widget-content ui-corner-all ui-widget-shadow">
					<textarea class="long-text-content ui-widget ui-widget-content"></textarea>
				</div>
			</div>
			<div class="result-operations button-operation">
				<div class="result-message-buttons">
					<button id="toggleAutoClearResultButton" class="result-message-button ui-button ui-corner-all ui-widget ui-button-icon-only stated-active" title="<@spring.message code='sqlpad.keepResult' />"><span class="ui-button-icon ui-icon ui-icon-pin-s"></span><span class="ui-button-icon-space"> </span><@spring.message code='sqlpad.keepResult' /></button>
					<button id="clearSqlResultMessageButton" class="result-message-button ui-button ui-corner-all ui-widget ui-button-icon-only" title="<@spring.message code='sqlpad.clearSqlResultMessage' />"><span class="ui-button-icon ui-icon ui-icon-trash"></span><span class="ui-button-icon-space"> </span><@spring.message code='sqlpad.clearSqlResultMessage' /></button>
				</div>
				<div class="sql-result-buttons">
					<button id="moreSqlResultTabButton" class="sql-result-button ui-button ui-corner-all ui-widget ui-button-icon-only" title="<@spring.message code='sqlpad.loadMoreData' />"><span class="ui-button-icon ui-icon ui-icon-arrowthick-1-s"></span><span class="ui-button-icon-space"> </span><@spring.message code='sqlpad.loadMoreData' /></button>
					<button id="refreshSqlResultTabButton" class="sql-result-button ui-button ui-corner-all ui-widget ui-button-icon-only" title="<@spring.message code='sqlpad.refreshSqlResult' />"><span class="ui-button-icon ui-icon ui-icon-refresh"></span><span class="ui-button-icon-space"> </span><@spring.message code='sqlpad.refreshSqlResult' /></button>
					<button id="exportSqlResultTabButton" class="sql-result-button ui-button ui-corner-all ui-widget ui-button-icon-only" title="<@spring.message code='sqlpad.exportSqlResult' />"><span class="ui-button-icon ui-icon ui-icon-arrowthick-1-ne"></span><span class="ui-button-icon-space"> </span><@spring.message code='sqlpad.exportSqlResult' /></button>
					&nbsp;&nbsp;
					<button id="viewSqlResultTabButton" class="sql-result-button ui-button ui-corner-all ui-widget ui-button-icon-only" title="<@spring.message code='sqlpad.viewSqlStatement' />"><span class="ui-button-icon ui-icon ui-icon-lightbulb"></span><span class="ui-button-icon-space"> </span><@spring.message code='sqlpad.viewSqlStatement' /></button>
					<button id="lockSqlResultTabButton" class="sql-result-button ui-button ui-corner-all ui-widget ui-button-icon-only stated-active" title="<@spring.message code='sqlpad.lockSqlResultTab' />"><span class="ui-button-icon ui-icon ui-icon-locked"></span><span class="ui-button-icon-space"> </span><@spring.message code='sqlpad.lockSqlResultTab' /></button>
				</div>
			</div>
			<div id="viewSqlStatementPanel" class="view-sql-statement-panel ui-widget ui-front ui-widget-content ui-corner-all ui-widget-shadow">
				<textarea class="sql-content ui-widget ui-widget-content"></textarea>
			</div>
		</div>
	</div>
	<div class="foot">
	</div>
	<div id="sqlExceptionDetailPanel" class="sql-exception-detail-panel ui-widget ui-widget-content ui-corner-all ui-widget-shadow">
		<div class="sql-exception-detail-content-wrapper">
			<div class="sql-exception-detail-content"></div>		
		</div>
	</div>
</div>
<#if !isAjaxRequest>
</div>
</#if>

<#include "../include/page_js_obj.ftl">
<#include "../include/page_obj_tabs.ftl" >
<#include "../include/page_obj_cometd.ftl">
<#include "../include/page_obj_format_time.ftl" >
<#include "../include/page_obj_data_permission.ftl">
<#include "../include/page_obj_data_permission_ds_table.ftl">
<#include "../include/page_obj_sqlEditor.ftl">
<script type="text/javascript">
(function(po)
{
	po.schemaId = "${schema.id}";
	po.sqlpadId = "${sqlpadId}";
	po.sqlpadChannelId = "${sqlpadChannelId}";
	po.sqlResultReadActualBlobRows = parseInt("${sqlResultRowMapper.readActualBlobRows}");
	po.sqlResultBlobPlaceholder = "${sqlResultRowMapper.blobPlaceholder?js_string}";
	
	po.resultMessageElement = po.element("#${pageId}-resultMessage");
	po.sqlResultTabs = po.element("#${pageId}-sqlResultTabs");
	
	$.initButtons(po.element(".head, .result-operations"));
	po.element("#sqlCommitModeSet").buttonset();
	po.element("#sqlExceptionHandleModeSet").buttonset();
	
	po.cometdInitIfNot();
	
	po.getSqlEditorSchemaId = function(){ return po.schemaId; };
	po.initSqlEditor();
	po.sqlEditor.focus();
	po.sqlEditor.navigateFileEnd();
	
	//数据库表条目、SQL历史拖入自动插入SQL
	$.enableTableNodeDraggable = true;
	po.element("#${pageId}-sql-editor").droppable(
	{
		accept: ".table-draggable, .sql-draggable",
		drop: function(event, ui)
		{
			var draggable = ui.draggable;
			var dropText = "";
			var cursor = po.sqlEditor.getCursorPosition();
			
			if(draggable.hasClass("table-draggable"))
			{
				dropText = ui.draggable.text();
				
				if(cursor.column == 0)
					dropText = "SELECT * FROM " +dropText;
			}
			else if(draggable.hasClass("sql-draggable"))
			{
				dropText = $(".sql-content", draggable).text();
			}
			
			if(dropText)
			{
				var delimiter = po.getSqlDelimiter();
				dropText += delimiter + "\n";
				
				po.sqlEditor.moveCursorToPosition(cursor);
				po.sqlEditor.session.insert(cursor, dropText);
				
				po.sqlEditor.focus();
			}
		}
	});
	
	//当前在执行的SQL语句数
	po.executingSqlCount = -1;
	
	po.sqlEditor.commands.addCommand(
	{
	    name: 'executeCommand',
	    bindKey: "Ctrl-ENTER",
	    exec: function(editor)
	    {
	    	po.element("#executeSqlButton").click();
	    }
	});
	
	$.resizableStopPropagation(po.element(".content-editor"),
	{
		containment : "parent",
		handles : "s",
		classes : { "ui-resizable-s" : "ui-widget-header" },
		resize : function(event, ui)
		{
			var parent = ui.element.parent();
			var parentHeight = parent.height();
			var editorHeight = ui.element.height();
			var editorHeightPercent =  (editorHeight/parentHeight * 100) + "%";
			var resultHeightpercent = ((parentHeight-editorHeight)/parentHeight * 100) + "%";
			
			ui.element.css("height", editorHeightPercent);
			$(".content-result", parent).css("height", resultHeightpercent);
			
			po.sqlEditor.resize();
			po.resizeSqlResultTabPanelDataTable();
		}
	});
	
	po.getSqlDelimiter = function()
	{
		var delimiter = po.element("#sqlDelimiterInput").val();
		
		if(!delimiter)
			delimiter = ";";
			
		return delimiter;
	};
	
	po.requestExecuteSql = function(sql, sqlStartRow, sqlStartColumn, commitMode, exceptionHandleMode, overTimeThreashold, resultsetFetchSize)
	{
		if(!po.element("#toggleAutoClearResultButton").hasClass("ui-state-active"))
			po.resultMessageElement.empty();
		
		$.ajax(
		{
			type : "POST",
			url : "${contextPath}/sqlpad/"+po.schemaId+"/execute",
			data :
			{
				"sqlpadId" : po.sqlpadId,
				"sql" : sql,
				"sqlDelimiter" : po.getSqlDelimiter(),
				"sqlStartRow" : sqlStartRow,
				"sqlStartColumn" : sqlStartColumn,
				"commitMode" : commitMode,
				"exceptionHandleMode" : exceptionHandleMode,
				"overTimeThreashold" : overTimeThreashold,
				"resultsetFetchSize" : resultsetFetchSize
			},
			error : function()
			{
				po.updateExecuteSqlButtonState(po.element("#executeSqlButton"), "init");
			}
		});
	},
	
	po.appendSqlStatementMessage = function($msgContent, sqlStatement, sqlStatementIndex)
	{
		if(sqlStatement == null)
			return;
		
		$("<div class='sql-index'>["+(sqlStatementIndex + 1)+"]</div>").appendTo($msgContent);
		var $sqlValue = $("<div class='sql-value' />").text($.truncateIf(sqlStatement.sql, "...", 100)).appendTo($msgContent);
		
		$sqlValue.click(function()
		{
			po.sqlEditor.gotoLine(sqlStatement.startRow);
			var selection = po.sqlEditor.session.getSelection();
			var range = new ace.Range(sqlStatement.startRow, sqlStatement.startColumn, sqlStatement.endRow, sqlStatement.endColumn);
			selection.setSelectionRange(range, true);
		});
		
		<#assign messageArgs=['"+(sqlStatement.startRow+1)+"', '"+sqlStatement.startColumn+"', '"+(sqlStatement.endRow+1)+"', '"+sqlStatement.endColumn+"'] />
		$sqlValue.attr("title", "<@spring.messageArgs code='sqlpad.executionSqlselectionRange' args=messageArgs />");
	};
	
	po.appendSQLExecutionStatMessage = function($msgContent, sqlExecutionStat)
	{
		if(sqlExecutionStat == null)
			return;
		
		var text = "<@spring.message code='sqlpad.sqlExecutionStat.quoteLeft' />";
		
		<#assign messageArgs=['"+(sqlExecutionStat.totalCount)+"', '"+sqlExecutionStat.successCount+"', '"+(sqlExecutionStat.exceptionCount)+"', '"+(sqlExecutionStat.abortCount)+"'] />
		text += "<@spring.messageArgs code='sqlpad.sqlExecutionStat.infoNoDuration' args=messageArgs />";
		
		if(sqlExecutionStat.sqlDuration >= 0)
		{
			<#assign messageArgs=['"+po.formatDuration(sqlExecutionStat.sqlDuration)+"'] />
			text += "<@spring.messageArgs code='sqlpad.sqlExecutionStat.infoSqlDurationSuffix' args=messageArgs />";
		}
		
		if(sqlExecutionStat.taskDuration >= 0)
		{
			<#assign messageArgs=['"+po.formatDuration(sqlExecutionStat.taskDuration)+"'] />
			text += "<@spring.messageArgs code='sqlpad.sqlExecutionStat.infoTaskDurationSuffix' args=messageArgs />";
		}
		
		text += "<@spring.message code='sqlpad.sqlExecutionStat.quoteRight' />";
		
		$("<div class='sql-stat' />").text(text).appendTo($msgContent);
	};
	
	po.handleMessage = function(message)
	{
		var msgData = message.data;
		var msgDataType = (msgData ? msgData.type : "");
		var $msgDiv = $("<div class='execution-message' />");
		
		if(msgData.timeText)
			$("<div class='message-time' />").html("["+msgData.timeText+"] ").appendTo($msgDiv);
		
		var $msgContent = $("<div class='message-content' />").appendTo($msgDiv);
		
		if(msgDataType == "START")
		{
			po.executingSqlCount = msgData.sqlCount;
			
			$msgDiv.addClass("execution-start");
			
			<#assign messageArgs=['"+msgData.sqlCount+"'] />
			$("<div />").html("<@spring.messageArgs code='sqlpad.executionStart' args=messageArgs />").appendTo($msgContent);
		}
		else if(msgDataType == "SQLSUCCESS")
		{
			$msgDiv.addClass("execution-success");
			
			po.appendSqlStatementMessage($msgContent, msgData.sqlStatement, msgData.sqlStatementIndex);
			
			if(msgData.sqlResultType == "UPDATE_COUNT")
			{
				<#assign messageArgs=['"+msgData.updateCount+"'] />
				$("<div class='sql-update-count' />").text("<@spring.messageArgs code='sqlpad.affectDataRowCount' args=messageArgs />")
						.appendTo($msgContent);
			}
			else if(msgData.sqlResultType == "RESULT_SET")
			{
				var tabId = po.element("#lockSqlResultTabButton").attr("lock-tab-id");
				
				if(!tabId)
					tabId = po.genSqlResultTabId();
				
				po.renderSqlResultTab(tabId, msgData.sqlStatement.sql, msgData.sqlSelectResult, (po.executingSqlCount == 1));
				
				$("<a href='javascript:void(0);' class='sql-result-link link' />")
					.html("<@spring.message code='sqlpad.viewResult' />")
					.attr("tab-id", tabId)
					.data("sql", msgData.sqlStatement.sql)
					.appendTo($msgContent)
					.click(function()
					{
						var $this = $(this);
						
						var tabId = $this.attr("tab-id");
						var sql = $this.data("sql");
						
						var tabsNav = po.getTabsNav(po.sqlResultTabs);
						var tab  = po.getTabsTabByTabId(po.sqlResultTabs, tabsNav, tabId);
						
						if(tab.length == 0)
						{
							$.tipInfo("<@spring.message code='sqlpad.selectResultExpired' />");
							return;
						}
						
						var tabPanel = po.getTabsTabPanelByTabId(po.sqlResultTabs, tabId);
						var tabForm = po.element("#" + po.getSqlResultTabPanelFormId(tabId), tabPanel);
						var tabSql = $("textarea[name='sql']", tabForm).val();
						
						var sqlEquals = (sql == tabSql);
						if(!sqlEquals)
							sqlEquals = (sql.replace(/\s/g, "") == tabSql.replace(/\s/g, ""));
						
						if(!sqlEquals)
						{
							$.tipInfo("<@spring.message code='sqlpad.selectResultExpired' />");
							return;
						}
						
						po.sqlResultTabs.tabs( "option", "active",  tab.index());
					});
			}
			else
				;
		}
		else if(msgDataType == "SQLEXCEPTION")
		{
			$msgDiv.addClass("execution-exception");
			
			po.appendSqlStatementMessage($msgContent, msgData.sqlStatement, msgData.sqlStatementIndex);
			
			var $seInfoSummary = $("<div class='sql-exception-summary' />").html(msgData.content).appendTo($msgContent);
			if(msgData.detailTrace)
			{
				$seInfoSummary.addClass("has-detail");
				$("<div class='sql-exception-detail' />").text(msgData.detailTrace).appendTo($msgContent);
				
				$seInfoSummary.click(function(event)
				{
					var $this = $(this);
					
					var uid = $this.attr("uid");
					if(!uid)
					{
						uid = $.uid();
						$this.attr("uid", uid);
					}
					
					var $seDetailPanel = po.element("#sqlExceptionDetailPanel");
					var $seDetailContent = $(".sql-exception-detail-content", $seDetailPanel);
					
					if(!$seDetailPanel.is(":hidden") && uid == $seDetailPanel.attr("uid"))
						$seDetailPanel.hide();
					else
					{
						$seDetailPanel.attr("uid", uid);
						
						$seDetailContent.empty();
						$("<pre />").text($this.next(".sql-exception-detail").text()).appendTo($seDetailContent);
						
						//XXX "of: $this"如果$this很长的话，$seDetailPanel定位不对
						$seDetailPanel.show().position({ my : "center bottom", at : "center top", of : po.resultMessageElement});
					}
				});
			}
		}
		else if(msgDataType == "SQLCOMMAND")
		{
			var appendContent = true;
			
			if(msgData.sqlCommand == "RESUME")
			{
				po.updateExecuteSqlButtonState(po.element("#executeSqlButton"), "executing");
				appendContent = false;
				$msgDiv = null;
			}
			else if(msgData.sqlCommand == "PAUSE")
			{
				po.updateExecuteSqlButtonState(po.element("#executeSqlButton"), "paused");
			}
			
			if(appendContent)
			{
				$("<div />").html(msgData.content).appendTo($msgContent);
				po.appendSQLExecutionStatMessage($msgContent, msgData.sqlExecutionStat);
			}
		}
		else if(msgDataType == "EXCEPTION")
		{
			$msgDiv.addClass("execution-exception");
			
			$("<div />").html(msgData.content).appendTo($msgContent);
		}
		else if(msgDataType == "TEXT")
		{
			$msgDiv.addClass("execution-text");
			
			if(msgData.cssClass)
				$msgContent.addClass(msgData.cssClass);
			
			$("<div />").html(msgData.text).appendTo($msgContent);
			po.appendSQLExecutionStatMessage($msgContent, msgData.sqlExecutionStat);
		}
		else if(msgDataType == "FINISH")
		{
			po.executingSqlCount = -1;
			
			$msgDiv.addClass("execution-finish");
			
			$("<div />").html("<@spring.message code='sqlpad.executeionFinish' />").appendTo($msgContent);
			po.appendSQLExecutionStatMessage($msgContent, msgData.sqlExecutionStat);
			
			po.updateExecuteSqlButtonState(po.element("#executeSqlButton"), "init");
		}
		else
			$msgDiv = null;
		
		if($msgDiv)
		{
			$msgDiv.appendTo(po.resultMessageElement);
			po.resultMessageElement.scrollTop(po.resultMessageElement.prop("scrollHeight"));
		}
	},
	
	po.sendSqlCommand = function(sqlCommand, $commandButton)
	{
		if($commandButton != undefined)
			$commandButton.button("disable");
		
		$.ajax(
		{
			type : "POST",
			url : "${contextPath}/sqlpad/"+po.schemaId+"/command",
			data :
			{
				"sqlpadId" : po.sqlpadId,
				"command" : sqlCommand
			},
			complete : function()
			{
				if($commandButton != undefined)
					$commandButton.button("enable");
			}
		});
	};
	
	po.updateExecuteSqlButtonState = function($executeSqlButton, state)
	{
		if(state == "paused")
		{
			$executeSqlButton.attr("execution-state", "paused").attr("title", "<@spring.message code='sqlpad.resumeExecutionWithShortcut' />");
			$(".ui-button-icon", $executeSqlButton).removeClass("ui-icon-pause").addClass("ui-icon-play");
		}
		else if(state == "executing")
		{
			$executeSqlButton.attr("execution-state", "executing").attr("title", "<@spring.message code='sqlpad.pauseExecutionWithShortcut' />");
			$(".ui-button-icon", $executeSqlButton).removeClass("ui-icon-play").addClass("ui-icon-pause");
		}
		else if(state == "init")
		{
			$executeSqlButton.removeAttr("execution-state").attr("title", "<@spring.message code='sqlpad.executeWithShortcut' />");
			$(".ui-button-icon", $executeSqlButton).removeClass("ui-icon-pause").addClass("ui-icon-play");
		}
	};
	
	po.sqlResultTabTemplate = "<li class='sql-result-tab' style='vertical-align:middle;'><a href='"+'#'+"{href}'>"+'#'+"{label}</a>"
		+"<div class='tab-operation'>"
		+"<span class='ui-icon ui-icon-close' title='<@spring.message code='close' />'>close</span>"
		+"<div class='tabs-more-operation-button' title='<@spring.message code='moreOperation' />'><span class='ui-icon ui-icon-caret-1-e'></span></div>"
		+"</div>"
		+"</li>";
	
	po.getSqlResultTabPanelTableId = function(tabId)
	{
		return tabId + "-table";
	};
	
	po.getSqlResultTabPanelFormId = function(tabId)
	{
		return tabId + "-form";
	};
	
	po.renderSqlResultTab = function(tabId, sql, sqlSelectResult, active)
	{
		var tabsNav = po.getTabsNav(po.sqlResultTabs);
		var tab = po.getTabsTabByTabId(po.sqlResultTabs, tabsNav, tabId);
		var tabPanel = null;
		
		if(tab.length > 0)
	    {
			tabPanel = po.getTabsTabPanelByTabId(po.sqlResultTabs, tabId);
			tabPanel.empty();
	    }
	    else
	    {
	    	var nameSeq = po.getNextSqlResultNameSeq();
	    	<#assign messageArgs=['"+nameSeq+"'] />
	    	var tabLabel = "<@spring.messageArgs code='sqlpad.selectResultWithIndex' args=messageArgs />";
	    	
	    	tab = $(po.sqlResultTabTemplate.replace( /#\{href\}/g, "#" + tabId).replace(/#\{label\}/g, tabLabel)).attr("id", $.uid("sqlResult-tab-"))
	    		.appendTo(tabsNav);
	    	
	    	tabPanel = $("<div id='"+tabId+"' class='sql-result-tab-panel' />").appendTo(po.sqlResultTabs);
	    	
    	    $(".tab-operation .ui-icon-close", tab).click(function()
    	    {
    	    	po.closeTab(po.sqlResultTabs, tabsNav, $(this).parent().parent());
    	    });
    	    
    	    $(".tab-operation .tabs-more-operation-button", tab).click(function()
    	    {
    	    	var tab = $(this).parent().parent();
    	    	var tabId = po.getTabsTabId(po.sqlResultTabs, tabsNav, tab);
    	    	
    	    	var menu = po.showTabMoreOperationMenu(po.sqlResultTabs, tabsNav, tab, $(this));
    	    });
	    }
		
	    po.sqlResultTabs.tabs("refresh");
		
	    if(active)
	    	po.sqlResultTabs.tabs( "option", "active",  tab.index());
	    else
	    {
	    	$.setResizeDataTableWhenShow(tabPanel);
	    	po.refreshTabsNavForHidden(po.sqlResultTabs, tabsNav);
	    }
	    
		var table = $("<table width='100%' class='hover stripe'></table>").attr("id", po.getSqlResultTabPanelTableId(tabId)).appendTo(tabPanel);
		po.initSqlResultDataTable(tabId, table, sql, sqlSelectResult);
		
		$("<div class='no-more-data-flag ui-widget ui-widget-content' />")
			.attr("title", "<@spring.message code='sqlpad.noMoreData' />").appendTo(tabPanel);
		
	    var form = $("<form style='display:none;' />")
	    	.attr("id", po.getSqlResultTabPanelFormId(tabId))
	    	.attr("action", "${contextPath}/sqlpad/"+po.schemaId+"/select")
	    	.attr("method", "POST")
	    	.attr("tab-id", tabId)
	    	.appendTo(tabPanel);
	    
	    $("<input name='sqlpadId' type='hidden' />").val(po.sqlpadId).appendTo(form);
	    $("<textarea name='sql' />").val(sql).appendTo(form);
	    $("<input name='startRow' type='hidden' />").val(sqlSelectResult.nextStartRow).appendTo(form);
	    $("<input name='fetchSize' type='hidden' />").val(sqlSelectResult.fetchSize).appendTo(form);

	    if(sqlSelectResult.rows == null || sqlSelectResult.rows.length < sqlSelectResult.fetchSize)
	    {
	    	form.attr("no-more-data", "1");
	    	$(".no-more-data-flag", tabPanel).show();
	    }
	    
	    form.submit(function()
	    {
	    	var $this = $(this);
	    	
	    	if("1" == $this.attr("no-more-data"))
	    	{
	    		$.tipInfo("<@spring.message code='sqlpad.noMoreData' />");
	    	}
	    	else
	    	{
		    	po.element("#moreSqlResultTabButton").button("disable");
		    	po.element("#refreshSqlResultTabButton").button("disable");
		    	
		    	$this.ajaxSubmit(
	   			{
	   				beforeSerialize: function($form, options)
	   				{
	   					var fetchSize = po.getResultsetFetchSize(po.element("#settingForm"));
	   					$("input[name='fetchSize']", $form).val(fetchSize);
	   				},
	   				success : function(sqlSelectResult, statusText, xhr, $form)
	   				{
	   					$("input[name='startRow']", $form).val(sqlSelectResult.nextStartRow);
	   					
	   					var tabId = $form.attr("tab-id");
	   					var tabPanel = po.getTabsTabPanelByTabId(po.sqlResultTabs, tabId);
	   					
	   					var dataTable = po.element("#" + po.getSqlResultTabPanelTableId(tabId), tabPanel).DataTable();
	   					
	   					$.addDataTableData(dataTable, sqlSelectResult.rows, sqlSelectResult.startRow-1);
	   					
	   					if(sqlSelectResult.rows.length < sqlSelectResult.fetchSize)
	   					{
	   						$form.attr("no-more-data", "1");
	   						$(".no-more-data-flag", tabPanel).show();
	   					}
	   					else
	   					{
	   						$form.attr("no-more-data", "0");
	   						$(".no-more-data-flag", tabPanel).hide();
	   					}
	   				},
	   				complete : function()
	   				{
	   			    	po.element("#moreSqlResultTabButton").button("enable");
	   			    	po.element("#refreshSqlResultTabButton").button("enable");
	   				}
	   			});
	    	}
	    	
	    	return false;
	    });
	};
	
	po.calSqlResultTableHeight = function(tabIdOrTabPanel)
	{
		if(typeof(tabIdOrTabPanel) == "string")
			tabIdOrTabPanel = po.getTabsTabPanelByTabId(po.sqlResultTabs, tabIdOrTabPanel);
		
		return tabIdOrTabPanel.height() - 37;
	};

	po.renderRowNumberColumn = function(data, type, row, meta)
	{
		var row = meta.row;
		
		if(row.length > 0)
			row = row[0];
		
		return row + 1;
	};
	
	po.viewSqlResultLongText = function(target)
	{
		target = $(target);
		var value = $("span", target).text();
		
		var panel = po.element("#viewLongTextResultPanel");
		$("textarea", panel).val(value);
		panel.show().position({ my : "left bottom", at : "left top-5", of : target});
	};
	
	po.initSqlResultDataTable = function(tabId, $table, sql, sqlSelectResult)
	{
		var dtColumns = $.buildDataTablesColumns(sqlSelectResult.table,
				{
					postRender : function(data, type, rowData, meta, rowIndex, renderValue, table, column, thisColumn)
					{
						if(!data)
							return renderValue;
						
						if($.meta.isBlobColumn(column))
						{
							if(rowIndex < po.sqlResultReadActualBlobRows)
							{
								return "<a href='${contextPath}/sqlpad/"+po.schemaId+"/downloadResultField?sqlpadId="+po.sqlpadId+"&value="+encodeURIComponent(data)+"'>"
										+ $.escapeHtml(po.sqlResultBlobPlaceholder) + "</a>";
							}
							else
								return renderValue;
						}
						else if(data != renderValue)
						{
							return "<a href='javascript:void(0);' onclick='${pageId}.viewSqlResultLongText(this)' class='view-sql-result-long-text-link'>"
									+ renderValue
									+ "<span style='display:none;'>"+$.escapeHtml(data)+"</span>" + "</a>";
						}
						else
							return renderValue;
					}
				});
		
		var newDtColumns = [
			{
				title : "<@spring.message code='rowNumber' />", data : "", defaultContent: "",
				render : po.renderRowNumberColumn, className : "column-row-number", width : "5em"
			}
		];
		newDtColumns = newDtColumns.concat(dtColumns);
		
		var settings =
		{
			"columns" : newDtColumns,
			"data" : (sqlSelectResult.rows ? sqlSelectResult.rows : []),
			"scrollX": true,
			"autoWidth": true,
			"scrollY" : po.calSqlResultTableHeight(tabId),
	        "scrollCollapse": false,
			"paging" : false,
			"searching" : false,
			"ordering": false,
			"select" : { style : 'os' },
		    "language":
		    {
				"emptyTable": "<@spring.message code='dataTables.noData' />",
				"zeroRecords" : "<@spring.message code='dataTables.zeroRecords' />"
			}
		};
		
		$table.dataTable(settings);
		
		$.bindResizeDataTableHandler($table,
				function()
				{
					return po.calSqlResultTableHeight(tabId);
				});
	};
	
	po.getNextSqlResultNameSeq = function()
	{
		if(po.getTabCount(po.sqlResultTabs, po.getTabsNav(po.sqlResultTabs)) == 1)
			po.nextSqlResultNameSeq = null;
		
		var seq = (po.nextSqlResultNameSeq == null ? 1 : po.nextSqlResultNameSeq);
		po.nextSqlResultNameSeq = seq + 1;
		
		return seq;
	};
	
	po.genSqlResultTabId = function()
	{
		var seq = (po.nextSqlResultIdSeq == null ? 0 : po.nextSqlResultIdSeq);
		po.nextSqlResultIdSeq = seq + 1;
		
		return "${pageId}-sqlResultTabs-tab-" + seq;
	};
	
	po.resizeSqlResultTabPanelDataTable = function()
	{
		$(".sql-result-tab-panel", po.sqlResultTabs).each(function()
		{
			var $this = $(this);
			
			if($this.is(":hidden"))
			{
				$.setResizeDataTableWhenShow($this);
			}
			else
			{
				var tableId = po.getSqlResultTabPanelTableId($this.attr("id"));
				var table = po.element("#"+tableId, $this);
				
				var height = po.calSqlResultTableHeight($this);
				$.updateDataTableHeight(table, height);
			}
		});
	};
	
	po.getOverTimeThreashold = function($form)
	{
		var overTimeThreashold = parseInt(po.element("input[name='overTimeThreashold']", $form).val());
		
		if(isNaN(overTimeThreashold))
			overTimeThreashold = 10;
		else if(overTimeThreashold < 1)
			overTimeThreashold = 1;
		else if(overTimeThreashold > 60)
			overTimeThreashold = 60;
		
		return overTimeThreashold;
	};
	
	po.getResultsetFetchSize = function($form)
	{
		var resultsetFetchSize = parseInt(po.element("input[name='resultsetFetchSize']", $form).val());
		
		if(isNaN(resultsetFetchSize))
			resultsetFetchSize = 20;
		else if(resultsetFetchSize < 1)
			resultsetFetchSize = 1;
		else if(resultsetFetchSize > 1000)
			resultsetFetchSize = 1000;
		
		return resultsetFetchSize;
	};
	
	po.element("#executeSqlButton").click(function()
	{
		var $this = $(this);
		
		var executionState = $this.attr("execution-state");
		
		if(executionState == "executing")
		{
			po.sendSqlCommand("PAUSE", $this);
		}
		else if(executionState == "paused")
		{
			po.sendSqlCommand("RESUME", $this);
		}
		else
		{
			var editor = po.sqlEditor;
			
			var selectionRange = editor.getSelectionRange();
			var sql = editor.session.getTextRange(selectionRange);
			var sqlStartRow = selectionRange.start.row;
			var sqlStartColumn = selectionRange.start.column;
			
			if(!sql)
			{
				sql = editor.getValue();
				sqlStartRow = 0;
				sqlStartColumn = 0;
			}
			
			if(!sql)
				return;
			
			var settingForm = po.element("#settingForm");
			
			var commitMode = po.element("input[name='sqlCommitMode']:checked", settingForm).val();
			var exceptionHandleMode = po.element("input[name='sqlExceptionHandleMode']:checked", settingForm).val();
			var overTimeThreashold = po.getOverTimeThreashold(settingForm);
			var resultsetFetchSize = po.getResultsetFetchSize(settingForm);
			
			po.updateExecuteSqlButtonState($this, "executing");
			
			po.cometdExecuteAfterSubscribe(po.sqlpadChannelId,
			function()
			{
				po.requestExecuteSql(sql, sqlStartRow, sqlStartColumn, commitMode, exceptionHandleMode, overTimeThreashold, resultsetFetchSize);
			},
			function(message)
			{
				po.handleMessage(message);
			});
		}
		
		po.sqlEditor.focus();
	});
	
	po.element("#stopSqlButton").click(function()
	{
		po.sendSqlCommand("STOP", $(this));
		
		po.sqlEditor.focus();
	});

	po.element("#commitSqlButton").click(function()
	{
		po.sendSqlCommand("COMMIT", $(this));
		
		po.sqlEditor.focus();
	});
	
	po.element("#rollbackSqlButton").click(function()
	{
		po.sendSqlCommand("ROLLBACK", $(this));
		
		po.sqlEditor.focus();
	});
	
	po.element("#clearSqlButton").click(function()
	{
		po.sqlEditor.setValue("");
		po.sqlEditor.focus();
	});
	
	<#--禁用插入文件功能，因为没有应用场景
	po.element(".insert-file-info").hide();
	if(!po.canDeleteTableData(${schema.dataPermission}))
		po.element(".insert-file-wrapper").attr("disabled", "disabled").hide();
	else
	{
		po.element("#insertFileButton").fileupload(
		{
			url : "${contextPath}/sqlpad/"+po.schemaId+"/uploadInsertFile?sqlpadId=" + po.sqlpadId,
			paramName : "file",
			success : function(serverFileInfo, textStatus, jqXHR)
			{
				$.fileuploadsuccessHandlerForUploadInfo(po.element(".insert-file-info"), true);
				po.element(".insert-file-info").hide();
				
				var fileName = serverFileInfo.name;
				
				po.sqlEditor.insert(fileName);
			},
			error : function()
			{
				po.element(".insert-file-info").hide();
			}
		})
		.bind('fileuploadadd', function (e, data)
		{
			po.element(".insert-file-info").show().position({ my : "left top", at : "left bottom"})
			$.fileuploadaddHandlerForUploadInfo(e, data, po.element(".insert-file-info"));
		})
		.bind('fileuploadprogressall', function (e, data)
		{
			$.fileuploadprogressallHandlerForUploadInfo(e, data, po.element(".insert-file-info"));
		});
	}
	-->
	
	po.element("#insertSqlDelimiterDefineButton").click(function()
	{
		var delimiter = po.getSqlDelimiter();
		
		if(delimiter)
		{
			var cursor = po.sqlEditor.selection.getCursor();
			
			var text = "";
			
			if(cursor.column == 0)
			{
				if(cursor.row != 0)
					text += "\n";
			}
			else
				text += "\n\n";
			
			text += "--@DELIMITER "+delimiter+"\n";
			
			po.sqlEditor.insert(text);
		}
		
		po.sqlEditor.focus();
	});
	
	po.element("#insertSqlDelimiterButton").click(function()
	{
		var delimiter = po.getSqlDelimiter();
		
		if(delimiter)
			po.sqlEditor.insert(delimiter+"\n");
		
		po.sqlEditor.focus();
	});
	
	po.element("#viewSqlHistorySearchForm").ajaxForm(
	{
		success : function(pagingData, statusText, xhr, $form)
		{
			var sqlHistories = pagingData.items;
			
			if(pagingData.page >= pagingData.pages)
				po.element("#sqlHistoryLoadMoreButton").button("disable");
			else
				po.element("#sqlHistoryLoadMoreButton").button("enable");
			
			var retainData = ($form.attr("retain-data") != null);
			if(retainData)
				$form.removeAttr("retain-data");
			
			var $hl = po.element(".sql-history-list");
			
			if(!retainData)
				$hl.empty();
			
			for(var i=0; i<sqlHistories.length; i++)
			{
				var sqlHistory = sqlHistories[i];
				
				if(i > 0 || pagingData.page > 1)
					$("<div class='sql-item-separator ui-widget ui-widget-content' />").appendTo($hl);
				
				var $item = $("<div class='sql-item' />").appendTo($hl);
				$("<div class='sql-date' />").text(sqlHistory.createTime).appendTo($item);
				$("<div class='sql-content' />").text(sqlHistory.sql).appendTo($item);
				
				$item.draggable(
				{
					helper: "clone",
					distance: 50,
					classes:
					{
						"ui-draggable" : "sql-draggable",
						"ui-draggable-dragging" : "ui-widget ui-widget-content ui-corner-all ui-widget-shadow sql-draggable-helper ui-front"
					}
				});
			}
		}
	});
	
	po.element(".sql-history-list").on("click", ".sql-item", function()
	{
		var $this = $(this);
		
		if($this.hasClass("ui-state-active"))
			$this.removeClass("ui-state-active");
		else
		{
			po.element(".sql-history-list .sql-item.ui-state-active").removeClass("ui-state-active");
			$this.addClass("ui-state-active");
		}
	});
	
	po.getSelectedSqlHistories = function()
	{
		var $selectedSql = po.element(".sql-history-list .sql-item.ui-state-active");
		
		if($selectedSql.length == 0)
			return null;
		
		var delimiter = po.getSqlDelimiter();
		
		var sql = "";
		
		$selectedSql.each(function()
		{
			var mySql = $(".sql-content", this).text();
			sql += mySql + delimiter + "\n";
		});
		
		return sql;
	};
	
	po.element("#insertSqlHistoryToEditorButton").click(function()
	{
		var sql = po.getSelectedSqlHistories();
		
		if(!sql)
			return;
		
		var cursor = po.sqlEditor.getCursorPosition();
		
		po.sqlEditor.moveCursorToPosition(cursor);
		po.sqlEditor.session.insert(cursor, sql);
		
		po.sqlEditor.focus();
	});
	
	var clipboard = new ClipboardJS(po.element("#copySqlHistoryToClipbordButton")[0],
	{
		text: function(trigger)
		{
			var sql = po.getSelectedSqlHistories();
			
			if(!sql)
				sql = "";
			
			return sql;
		}
	});
	clipboard.on('success', function(e)
	{
		$.tipSuccess("<@spring.message code='copyToClipboardSuccess' />");
	});
	
	po.element("#sqlHistoryLoadMoreButton").click(function()
	{
		var $form = po.element("#viewSqlHistorySearchForm");
		var $page = po.element("input[name='page']", $form);
		
		var page = parseInt($page.val());
		if(!page)
			page = 1;
		page += 1;
		
		$page.val(page);
		
		$form.attr("retain-data", "1");
		$form.submit();
	});
	
	po.element("#sqlHistoryRefreshButton").click(function()
	{
		var $form = po.element("#viewSqlHistorySearchForm");
		po.element("input[name='page']", $form).val("1");
		
		$form.submit();
	});
	
	po.element("#viewSqlHistoryButton").click(function()
	{
		var $vhp = po.element(".view-sql-history-panel");
		
		if(!$vhp.is(":hidden"))
		{
			$vhp.hide();
			return;
		}
		else
		{
			var $shl = po.element(".sql-history-list");
			$shl.height(po.element().height()/2.5);
			$vhp.show();
			
			var $hl = po.element(".sql-history-list");
			if($hl.children().length == 0)
				po.element("#viewSqlHistorySearchForm").submit();
		}
	});
	
	po.element("#settingButton").click(function()
	{
		po.element(".setting-panel").toggle();
	});
	
	po.element("input[name='sqlCommitMode']").change(function()
	{
		var value = $(this).val();
		
		if(value == "MANUAL")
		{
			//po.element("#commitSqlButton").button("enable");
			//po.element("#rollbackSqlButton").button("enable");
			
			var $rollbackExceptionHandle = po.element("input[name='sqlExceptionHandleMode'][value='ROLLBACK']");
			$rollbackExceptionHandle.attr("disabled", "disabled");
			if($rollbackExceptionHandle.is(":checked"))
				po.element("input[name='sqlExceptionHandleMode'][value='ABORT']").prop("checked", true);
			po.element("#sqlExceptionHandleModeSet").buttonset("refresh");
		}
		else
		{
			//po.element("#commitSqlButton").button("disable");
			//po.element("#rollbackSqlButton").button("disable");
			
			po.element("input[name='sqlExceptionHandleMode'][value='ROLLBACK']").removeAttr("disabled");
			po.element("#sqlExceptionHandleModeSet").buttonset("refresh");
		}
	});
	
	po.element("#toggleAutoClearResultButton").click(function()
	{
		var $this = $(this);
		
		if($this.hasClass("ui-state-active"))
		{
			$(this).removeClass("ui-state-active");
		}
		else
		{
			$(this).addClass("ui-state-active");
		}
	});
	
	po.element("#clearSqlResultMessageButton").click(function()
	{
		po.resultMessageElement.empty();
	});
	
	po.element("#moreSqlResultTabButton").click(function()
	{
		var tabsNav = po.getTabsNav(po.sqlResultTabs);
		var activeTab = po.getActiveTab(po.sqlResultTabs, tabsNav);
		var activeTabId = po.getTabsTabId(po.sqlResultTabs, tabsNav, activeTab);
		var activeTabFormId = po.getSqlResultTabPanelFormId(activeTabId);
		var activeTabForm = po.element("#" + activeTabFormId);
		
		activeTabForm.submit();
	});
	
	po.element("#refreshSqlResultTabButton").click(function()
	{
		var tabsNav = po.getTabsNav(po.sqlResultTabs);
		var activeTab = po.getActiveTab(po.sqlResultTabs, tabsNav);
		var activeTabId = po.getTabsTabId(po.sqlResultTabs, tabsNav, activeTab);
		var activeTabFormId = po.getSqlResultTabPanelFormId(activeTabId);
		var activeTabForm = po.element("#" + activeTabFormId);
		
		$("input[name='startRow']", activeTabForm).val(1);
		activeTabForm.attr("no-more-data", "0");
		
		activeTabForm.submit();
	});

	po.element("#exportSqlResultTabButton").click(function()
	{
		var tabsNav = po.getTabsNav(po.sqlResultTabs);
		var activeTab = po.getActiveTab(po.sqlResultTabs, tabsNav);
		
		if(activeTab.hasClass("sql-result-tab"))
		{
			var tabId = po.getTabsTabId(po.sqlResultTabs, tabsNav, activeTab);
			var tabFormId = po.getSqlResultTabPanelFormId(tabId);
			var tabForm = po.element("#" + tabId);
			var sql = $("textarea[name='sql']", tabForm).val();
			
			var options = {data: {"initSqls": sql}};
			$.setGridPageHeightOption(options);
			po.open("${contextPath}/dataexchange/"+po.schemaId+"/export", options);
		}
	});
	
	po.element("#viewSqlResultTabButton").click(function()
	{
		var $this = $(this);
		
		var viewSqlStatementPanel = po.element("#viewSqlStatementPanel");
		
		if(!viewSqlStatementPanel.is(":hidden"))
		{
			viewSqlStatementPanel.hide();
			return;
		}
		
		var tabsNav = po.getTabsNav(po.sqlResultTabs);
		var activeTab = po.getActiveTab(po.sqlResultTabs, tabsNav);
		
		if(activeTab.hasClass("sql-result-tab"))
		{
			var tabId = po.getTabsTabId(po.sqlResultTabs, tabsNav, activeTab);
			var tabFormId = po.getSqlResultTabPanelFormId(tabId);
			var tabForm = po.element("#" + tabId);
			var sql = $("textarea[name='sql']", tabForm).val();
			
			$("textarea", viewSqlStatementPanel).val(sql);
			viewSqlStatementPanel.show().position({ my : "right bottom", at : "right top-5", of : $this});
		}
	});
	
	po.element("#lockSqlResultTabButton").click(function()
	{
		var $this = $(this);
		
		var tabsNav = po.getTabsNav(po.sqlResultTabs);
		
		if($this.hasClass("ui-state-active"))
		{
			$this.removeAttr("lock-tab-id");
			$this.removeClass("ui-state-active");
		}
		else
		{
			var activeTab = po.getActiveTab(po.sqlResultTabs, tabsNav);
			
			if(activeTab.hasClass("sql-result-tab"))
			{
				var tabId = po.getTabsTabId(po.sqlResultTabs, tabsNav, activeTab);
				
				$this.attr("lock-tab-id", tabId);
				$this.addClass("ui-state-active");
			}
		}
	});
	
	po.element("#settingForm").validate(
	{
		rules :
		{
			overTimeThreashold : { required : true, integer : true, min : 1, max : 60 },
			resultsetFetchSize : { required : true, integer : true, min : 1, max : 1000 }
		},
		messages :
		{
			overTimeThreashold : "<@spring.message code='sqlpad.overTimeThreashold.validation' />",
			resultsetFetchSize : "<@spring.message code='sqlpad.resultsetFetchSize.validation' />"
		},
		submitHandler : function(form)
		{
			return false;
		},
		errorPlacement : function(error, element)
		{
			error.appendTo(element.closest(".form-item-value"));
		}
	});
	
	$(document.body).on("click", function(event)
	{
		var $target = $(event.target);

		var $vhp = po.element(".view-sql-history-panel");
		if(!$vhp.is(":hidden"))
		{
			if($target.closest(po.element(".view-sql-history-wrapper")).length == 0)
				$vhp.hide();
		}
		
		var $sp = po.element(".setting-panel");
		if(!$sp.is(":hidden"))
		{
			if($target.closest(po.element(".setting-wrapper")).length == 0)
				$sp.hide();
		}
		
		var $vsp = po.element("#viewSqlStatementPanel");
		if(!$vsp.is(":hidden"))
		{
			if($target.closest("#viewSqlStatementPanel, #viewSqlResultTabButton").length == 0)
				$vsp.hide();
		}
		
		var $vltp = po.element("#viewLongTextResultPanel");
		if(!$vltp.is(":hidden"))
		{
			if($target.closest("#viewLongTextResultPanel, .view-sql-result-long-text-link").length == 0)
				$vltp.hide();
		}
	});
	
	po.element("#sqlExceptionDetailPanel").mouseleave(function()
	{
		$(this).hide();
	});
	
	po.sqlResultTabs.tabs(
	{
		event: "click",
		activate: function(event, ui)
		{
			var $this = $(this);
			var newTab = $(ui.newTab);
			var newPanel = $(ui.newPanel);
			var tabsNav = po.getTabsNav($this);
			
			po.refreshTabsNavForHidden($this, tabsNav, newTab);
			
			var resultOperations = po.element(".result-operations");
			
			if(newTab.hasClass("sql-result-tab"))
			{
				$(".result-message-buttons", resultOperations).hide();
				$(".sql-result-buttons", resultOperations).show();
				
				var lockSqlResultTabButton = po.element("#lockSqlResultTabButton");
				var newTabId = po.getTabsTabId($this, tabsNav, newTab);
				
				if(newTabId == lockSqlResultTabButton.attr("lock-tab-id"))
					lockSqlResultTabButton.addClass("ui-state-active");
				else
					lockSqlResultTabButton.removeClass("ui-state-active");
			}
			else if(newTab.hasClass("result-message-tab"))
			{
				$(".sql-result-buttons", resultOperations).hide();
				$(".result-message-buttons", resultOperations).show();
			}
			
			$.callTabsPanelShowCallback(newPanel);
		}
	});
	
	po.getTabsTabMoreOperationMenu(po.sqlResultTabs).menu(
	{
		select: function(event, ui)
		{
			var $this = $(this);
			var item = ui.item;
			
			po.handleTabMoreOperationMenuSelect($this, item, po.sqlResultTabs);
			po.getTabsTabMoreOperationMenuWrapper(po.sqlResultTabs).hide();
		}
	});
	
	po.getTabsMoreTabMenu(po.sqlResultTabs).menu(
	{
		select: function(event, ui)
		{
			po.handleTabsMoreTabMenuSelect($(this), ui.item, po.sqlResultTabs);
	    	po.getTabsMoreTabMenuWrapper(po.sqlResultTabs).hide();
		}
	});
	
	po.element("input[name='sqlCommitMode'][value='AUTO']").click();
	po.element(".view-sql-history-panel").hide();
	po.element(".setting-panel").hide();
	po.element(".result-operations .sql-result-buttons").hide();
	po.element("#viewSqlStatementPanel").hide();
	po.element("#viewLongTextResultPanel").hide();
	
	po.bindTabsMenuHiddenEvent(po.sqlResultTabs);
})
(${pageId});
</script>
</body>
</html>

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
		<button id="clearSqlButton" class="ui-button ui-corner-all ui-widget ui-button-icon-only" title="<@spring.message code='sqlpad.clearEditSql' />"><span class="ui-button-icon ui-icon ui-icon-trash"></span><span class="ui-button-icon-space"> </span><@spring.message code='sqlpad.clearEditSql' /></button>		
		<div class="more-operation-wrapper">
			<button id="moreOperationButton" class="ui-button ui-corner-all ui-widget ui-button-icon-only" title="<@spring.message code='sqlpad.moreOperation' />"><span class="ui-button-icon ui-icon ui-icon-caret-1-s"></span><span class="ui-button-icon-space"> </span><@spring.message code='sqlpad.moreOperation' /></button>
			<div class="more-operation-panel ui-widget ui-widget-content ui-corner-all ui-widget-shadow ui-front">
				<form id="moreOperationForm" method="POST" action="#">
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
	</div>
	<div class="content ui-widget ui-widget-content">
		<div class="content-editor">
			<div class="content-edit-content">
				<div id="${pageId}-sql-editor" class="sql-editor">
select * from t_account;
select * from t_order;
update t_account set id = 5 where id=-99999;
delete from t_address where city = '-99999999';
				</div>
			</div>
		</div>
		<div class="content-result">
			<div id="${pageId}-sqlResultTabs" class="result-tabs">
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
			</div>
			<div class="result-operations button-operation">
				<div class="result-message-buttons">
					<button id="toggleAutoClearResultButton" class="result-message-button ui-button ui-corner-all ui-widget ui-button-icon-only stated-active" title="<@spring.message code='sqlpad.keepResult' />"><span class="ui-button-icon ui-icon ui-icon-pin-s"></span><span class="ui-button-icon-space"> </span><@spring.message code='sqlpad.keepResult' /></button>
					<button id="clearSqlResultMessageButton" class="result-message-button ui-button ui-corner-all ui-widget ui-button-icon-only" title="<@spring.message code='sqlpad.clearSqlResultMessage' />"><span class="ui-button-icon ui-icon ui-icon-trash"></span><span class="ui-button-icon-space"> </span><@spring.message code='sqlpad.clearSqlResultMessage' /></button>
				</div>
				<div class="sql-result-buttons">
					<button id="moreSqlResultTabButton" class="sql-result-button ui-button ui-corner-all ui-widget ui-button-icon-only" title="<@spring.message code='sqlpad.loadMoreData' />"><span class="ui-button-icon ui-icon ui-icon-arrowthick-1-s"></span><span class="ui-button-icon-space"> </span><@spring.message code='sqlpad.loadMoreData' /></button>
					<button id="refreshSqlResultTabButton" class="sql-result-button ui-button ui-corner-all ui-widget ui-button-icon-only" title="<@spring.message code='sqlpad.refreshSqlResult' />"><span class="ui-button-icon ui-icon ui-icon-refresh"></span><span class="ui-button-icon-space"> </span><@spring.message code='sqlpad.refreshSqlResult' /></button>
					&nbsp;&nbsp;
					<button id="lockSqlResultTabButton" class="sql-result-button ui-button ui-corner-all ui-widget ui-button-icon-only stated-active" title="<@spring.message code='sqlpad.lockSqlResultTab' />"><span class="ui-button-icon ui-icon ui-icon-locked"></span><span class="ui-button-icon-space"> </span><@spring.message code='sqlpad.lockSqlResultTab' /></button>
				</div>
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
<script type="text/javascript">
(function(po)
{
	po.schemaId = "${schema.id}";
	po.sqlpadId = "${sqlpadId}";
	po.sqlpadChannelId = "${sqlpadChannelId}";
	
	po.resultMessageElement = po.element("#${pageId}-resultMessage");
	po.sqlResultTabs = po.element("#${pageId}-sqlResultTabs");
	
	$.initButtons(po.element(".head, .result-operations"));
	po.element("#sqlCommitModeSet").buttonset();
	po.element("#sqlExceptionHandleModeSet").buttonset();
	
	po.sqlEditor = ace.edit("${pageId}-sql-editor");
	var SqlMode = ace.require("ace/mode/sql").Mode;
	po.sqlEditor.session.setMode(new SqlMode());
	po.sqlEditor.setShowPrintMargin(false);
	po.sqlEditor.focus();
	po.sqlEditor.navigateFileEnd();
	
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
	
	po.executeSql = function(sql, sqlStartRow, sqlStartColumn, commitMode, exceptionHandleMode, overTimeThreashold, resultsetFetchSize)
	{
		if(po.cometdSubscribed)
		{
			po.requestExecuteSql(sql, sqlStartRow, sqlStartColumn, commitMode, exceptionHandleMode, overTimeThreashold, resultsetFetchSize);
		}
		else
		{
			var cometd = $.cometd;
			
			cometd.subscribe(po.sqlpadChannelId, function(message)
			{
				po.handleMessage(message);
			},
			function(subscribeReply)
			{
				if(subscribeReply.successful)
				{
					po.cometdSubscribed = true;
					
					po.requestExecuteSql(sql, sqlStartRow, sqlStartColumn, commitMode, exceptionHandleMode, overTimeThreashold, resultsetFetchSize);
				}
			});
		}
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
	
	po.formatDuration = function(duration)
	{
		var text = "";
		
		var hours = Math.floor(duration/1000/60/60);
		
		if(hours > 0)
		{
			var minutes = Math.round(duration/1000/60 - hours*60);
			
			<#assign messageArgs=['"+hours+"', '"+minutes+"'] />
			text = "<@spring.messageArgs code='duration.H.M' args=messageArgs />";
		}
		else
		{
			var minutes = Math.floor(duration/1000/60);
			
			if(minutes > 0)
			{
				var seconds = Math.round(duration/1000 - minutes*60);
				
				<#assign messageArgs=['"+minutes+"', '"+seconds+"'] />
				text = "<@spring.messageArgs code='duration.M.S' args=messageArgs />";
			}
			else
			{
				var seconds = Math.floor(duration/1000);
				
				if(seconds > 0)
				{
					var mseconds = Math.round(duration - seconds*1000);
					
					<#assign messageArgs=['"+seconds+"', '"+mseconds+"'] />
					text = "<@spring.messageArgs code='duration.S.MS' args=messageArgs />";
				}
				else
				{
					<#assign messageArgs=['"+duration+"'] />
					text = "<@spring.messageArgs code='duration.MS' args=messageArgs />";
				}
			}
		}
		
		return text;
	};
	
	po.appendSqlStatementMessage = function($msgContent, sqlStatement, sqlStatementIndex)
	{
		if(sqlStatement == null)
			return;
		
		$("<div class='sql-index'>["+(sqlStatementIndex + 1)+"]</div>").appendTo($msgContent);
		var $sqlValue = $("<div class='sql-value' />").text($.truncateIf(sqlStatement.sql, "...", 100)).appendTo($msgContent);
		
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
				var tabId = null;
				
				if(po.element("#lockSqlResultTabButton").hasClass("ui-state-active"))
				{
					var tabsNav = po.getTabsNav(po.sqlResultTabs);
					var activeTab = po.getActiveTab(po.sqlResultTabs, tabsNav);
					
					if(activeTab.hasClass("sql-result-tab"))
						tabId = po.getTabsTabId(po.sqlResultTabs, tabsNav, activeTab);
				}
				
				if(tabId == null)
					tabId = po.genSqlResultTabId();
				
				po.renderSqlResultTab(tabId, msgData.sqlStatement.sql, msgData.modelSqlResult, (po.executingSqlCount == 1));
				
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
						
						if(sql != tabSql)
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
		+"<div class='tabs-more-operation-button' title='<@spring.message code='moreOperation' />'></div>"
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
	
	po.renderSqlResultTab = function(tabId, sql, modelSqlResult, active)
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
		po.initSqlResultDataTable(tabId, table, sql, modelSqlResult);
		
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
	    $("<input name='startRow' type='hidden' />").val(modelSqlResult.nextStartRow).appendTo(form);
	    $("<input name='fetchSize' type='hidden' />").val(modelSqlResult.fetchSize).appendTo(form);

	    if(modelSqlResult.datas == null || modelSqlResult.datas.length < modelSqlResult.fetchSize)
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
	   					var fetchSize = po.getResultsetFetchSize(po.element("#moreOperationForm"));
	   					$("input[name='fetchSize']", $form).val(fetchSize);
	   				},
	   				success : function(modelSqlResult, statusText, xhr, $form)
	   				{
	   					$("input[name='startRow']", $form).val(modelSqlResult.nextStartRow);
	   					
	   					var tabId = $form.attr("tab-id");
	   					var tabPanel = po.getTabsTabPanelByTabId(po.sqlResultTabs, tabId);
	   					
	   					var dataTable = po.element("#" + po.getSqlResultTabPanelTableId(tabId), tabPanel).DataTable();
	   					
	   					$.addDataTableData(dataTable, modelSqlResult.datas, modelSqlResult.startRow-1);
	   					
	   					if(modelSqlResult.datas.length < modelSqlResult.fetchSize)
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
	
	po.initSqlResultDataTable = function(tabId, $table, sql, modelSqlResult)
	{
		var model = modelSqlResult.model;
		var columns = $.buildDataTablesColumns(model);
		
		var newColumns = [
			{
				title : "<@spring.message code='rowNumber' />", data : "", defaultContent: "",
				render : po.renderRowNumberColumn, className : "column-row-number"
			}
		];
		newColumns = newColumns.concat(columns);
		
		var settings =
		{
			"columns" : newColumns,
			"data" : (modelSqlResult.datas ? modelSqlResult.datas : []),
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
			
			var moreOperationForm = po.element("#moreOperationForm");
			
			var commitMode = po.element("input[name='sqlCommitMode']:checked", moreOperationForm).val();
			var exceptionHandleMode = po.element("input[name='sqlExceptionHandleMode']:checked", moreOperationForm).val();
			var overTimeThreashold = po.getOverTimeThreashold(moreOperationForm);
			var resultsetFetchSize = po.getResultsetFetchSize(moreOperationForm);
			
			var cometd = $.cometd;
			
			po.updateExecuteSqlButtonState($this, "executing");
			
			if(!$.isCometdInit)
			{
				$.isCometdInit = true;
				cometd.init("${contextPath}/cometd", function(handshakeReply)
				{
					if(handshakeReply.successful)
					{
						po.executeSql(sql, sqlStartRow, sqlStartColumn, commitMode, exceptionHandleMode, overTimeThreashold, resultsetFetchSize);
					}
				});
			}
			else
				po.executeSql(sql, sqlStartRow, sqlStartColumn, commitMode, exceptionHandleMode, overTimeThreashold, resultsetFetchSize);
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
	
	po.element("#moreOperationButton").click(function()
	{
		po.element(".more-operation-panel").toggle();
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

	po.element("#lockSqlResultTabButton").click(function()
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
	
	po.element("#moreOperationForm").validate(
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
		var $mop = po.element(".more-operation-panel");
		if(!$mop.is(":hidden"))
		{
			if($(event.target).closest(po.element(".more-operation-wrapper")).length == 0)
				$mop.hide();
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
	po.element(".more-operation-panel").hide();
	po.element(".result-operations .sql-result-buttons").hide();
	
	po.bindTabsMenuHiddenEvent(po.sqlResultTabs);
})
(${pageId});
</script>
</body>
</html>

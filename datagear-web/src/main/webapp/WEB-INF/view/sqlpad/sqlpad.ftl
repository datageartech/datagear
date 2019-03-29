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
			<button id="moreOperationButton" class="ui-button ui-corner-all ui-widget ui-button-icon-only" title="<@spring.message code='sqlpad.moreOperation' />"><span class="ui-button-icon ui-icon ui-icon-triangle-1-s"></span><span class="ui-button-icon-space"> </span><@spring.message code='sqlpad.moreOperation' /></button>
			<div class="more-operation-panel ui-widget ui-widget-content ui-corner-all ui-widget-shadow">
				<form id="moreOperationForm" method="POST" action="#">
					<div class="form-content">
						<div class="form-item">
							<div class="form-item-label"><label><@spring.message code='sqlpad.sqlCommitMode' /></label></div>
							<div class="form-item-value">
								<div id="sqlCommitModeSet">
									<input type="radio" id="${pageId}-sqlcm-0" name="sqlCommitMode" value="AUTO"><label for="${pageId}-sqlcm-0"><@spring.message code='sqlpad.sqlCommitMode.auto' /></label>
									<input type="radio" id="${pageId}-sqlcm-1" name="sqlCommitMode" value="MANUAL"><label for="${pageId}-sqlcm-1"><@spring.message code='sqlpad.sqlCommitMode.manual' /></label>
								</div>
							</div>
						</div>
						<div class="form-item">
							<div class="form-item-label"><label><@spring.message code='sqlpad.sqlExceptionHandleMode' /></label></div>
							<div class="form-item-value">
								<div id="sqlExceptionHandleModeSet">
									<input type="radio" id="${pageId}-sqlehm-0" name="sqlExceptionHandleMode" value="ABORT" checked="checked"><label for="${pageId}-sqlehm-0"><@spring.message code='sqlpad.sqlExceptionHandleMode.abort' /></label>
									<input type="radio" id="${pageId}-sqlehm-2" name="sqlExceptionHandleMode" value="IGNORE"><label for="${pageId}-sqlehm-2"><@spring.message code='sqlpad.sqlExceptionHandleMode.ignore' /></label>
									<input type="radio" id="${pageId}-sqlehm-1" name="sqlExceptionHandleMode" value="ROLLBACK"><label for="${pageId}-sqlehm-1"><@spring.message code='sqlpad.sqlExceptionHandleMode.rollback' /></label>
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
					</div>
				</form>
			</div>
		</div>
	</div>
	<div class="content ui-widget ui-widget-content">
		<div class="content-editor">
			<div class="content-edit-content">
				<div id="${pageId}-sql-editor" class="sql-editor">
select count(*) from t_order where id = 3 and name = 'jack';
select count(*) from t_order where id = 3 and name = 'jack';
select count(*) from t_order where id = 3 and name = 'jack';
select count(*) from t_order where id = 3 and name = 'jack';
select count(*) from t_order where id = 3 and name = 'jack';
select count(*) from t_order where id = 3 and name = 'jack';
select count(*) from t_order where id = 3 and name = 'jack';
select count(*) from t_order where id = 3 and name = 'jack';
select count(*) from t_order where id = 3 and name = 'jack';
select count(*) from t_order where id = 3 and name = 'jack';
select count(*) from t_order where id = 3 and name = 'jack';
select count(*) from t_order where id = 3 and name = 'jack';
select count(*) from t_order where id = 3 and name = 'jack';
select count(*) from t_order where id = 3 and name = 'jack';
				</div>
			</div>
		</div>
		<div id="${pageId}-sql-result" class="content-result">
			<div class="result-head button-operation">
				<button id="toggleAutoClearResultButton" class="ui-button ui-corner-all ui-widget ui-button-icon-only stated-active" title="<@spring.message code='sqlpad.keepResult' />"><span class="ui-button-icon ui-icon ui-icon-pin-s"></span><span class="ui-button-icon-space"> </span><@spring.message code='sqlpad.keepResult' /></button>
				<button id="clearResultButton" class="ui-button ui-corner-all ui-widget ui-button-icon-only" title="<@spring.message code='sqlpad.clearSqlResult' />"><span class="ui-button-icon ui-icon ui-icon-trash"></span><span class="ui-button-icon-space"> </span><@spring.message code='sqlpad.clearSqlResult' /></button>
			</div>
			<div class="result-content ui-widget ui-widget-content"></div>
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
<script type="text/javascript">
(function(po)
{
	po.schemaId = "${schema.id}";
	po.sqlpadId = "${sqlpadId}";
	po.sqlpadChannelId = "${sqlpadChannelId}";
	
	po.sqlResultContentElement = po.element("#${pageId}-sql-result > .result-content");
	
	$.initButtons(po.element(".head"));
	po.element("#sqlCommitModeSet").buttonset();
	po.element("#sqlExceptionHandleModeSet").buttonset();
	po.element(".more-operation-panel").hide();
	
	po.sqlEditor = ace.edit("${pageId}-sql-editor");
	var SqlMode = ace.require("ace/mode/sql").Mode;
	po.sqlEditor.session.setMode(new SqlMode());
	po.sqlEditor.setShowPrintMargin(false);
	po.sqlEditor.focus();
	po.sqlEditor.navigateFileEnd();
	
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
		}
	});
	
	po.executeSql = function(sql, sqlStartRow, sqlStartColumn, commitMode, exceptionHandleMode, overTimeThreashold)
	{
		if(po.cometdSubscribed)
		{
			po.requestExecuteSql(sql, sqlStartRow, sqlStartColumn, commitMode, exceptionHandleMode, overTimeThreashold);
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
					
					po.requestExecuteSql(sql, sqlStartRow, sqlStartColumn, commitMode, exceptionHandleMode, overTimeThreashold);
				}
			});
		}
	};
	
	po.requestExecuteSql = function(sql, sqlStartRow, sqlStartColumn, commitMode, exceptionHandleMode, overTimeThreashold)
	{
		if(!po.element("#toggleAutoClearResultButton").hasClass("ui-state-active"))
			po.sqlResultContentElement.empty();
		
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
				"overTimeThreashold" : overTimeThreashold
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
			$msgDiv.addClass("execution-start");
			
			$("<div />").html("<@spring.message code='sqlpad.executionStart' />").appendTo($msgContent);
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
						$seDetailPanel.show().position({ my : "center bottom", at : "center top", of : po.sqlResultContentElement});
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
			$msgDiv.addClass("execution-finish");
			
			$("<div />").html("<@spring.message code='sqlpad.executeionFinish' />").appendTo($msgContent);
			po.appendSQLExecutionStatMessage($msgContent, msgData.sqlExecutionStat);
			
			po.updateExecuteSqlButtonState(po.element("#executeSqlButton"), "init");
		}
		else
			$msgDiv = null;
		
		if($msgDiv)
		{
			$msgDiv.appendTo(po.sqlResultContentElement);
			po.sqlResultContentElement.scrollTop(po.sqlResultContentElement.prop("scrollHeight"));
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
			
			var commitMode = po.element("input[name='sqlCommitMode']:checked").val();
			var exceptionHandleMode = po.element("input[name='sqlExceptionHandleMode']:checked").val();
			var overTimeThreashold = parseInt(po.element("input[name='overTimeThreashold']").val());
			
			if(isNaN(overTimeThreashold))
				overTimeThreashold = 10;
			else if(overTimeThreashold < 1)
				overTimeThreashold = 1;
			else if(overTimeThreashold > 60)
				overTimeThreashold = 60;
			
			var cometd = $.cometd;
			
			po.updateExecuteSqlButtonState($this, "executing");
			
			if(!$.isCometdInit)
			{
				$.isCometdInit = true;
				cometd.init("${contextPath}/cometd", function(handshakeReply)
				{
					if(handshakeReply.successful)
					{
						po.executeSql(sql, sqlStartRow, sqlStartColumn, commitMode, exceptionHandleMode, overTimeThreashold);
					}
				});
			}
			else
				po.executeSql(sql, sqlStartRow, sqlStartColumn, commitMode, exceptionHandleMode, overTimeThreashold);
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
	
	po.element("#clearResultButton").click(function()
	{
		po.sqlResultContentElement.empty();
	});
	
	po.element("#moreOperationForm").validate(
	{
		rules :
		{
			overTimeThreashold : { required : true, integer : true, min : 1, max : 60 }
		},
		messages :
		{
			overTimeThreashold : "<@spring.message code='sqlpad.overTimeThreashold.validation' />"
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
	
	$(document.body).bind("click", function(event)
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
	
	po.element("input[name='sqlCommitMode'][value='AUTO']").click();
})
(${pageId});
</script>
</body>
</html>

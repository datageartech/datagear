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
				<form action="#">
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
				<button id="toggleResultTimeButton" class="ui-button ui-corner-all ui-widget ui-button-icon-only stated-active" title="<@spring.message code='sqlpad.hideSqlResultTime' />"><span class="ui-button-icon ui-icon ui-icon-clock"></span><span class="ui-button-icon-space"> </span><@spring.message code='execute' /></button>
				<button id="clearResultButton" class="ui-button ui-corner-all ui-widget ui-button-icon-only" title="<@spring.message code='sqlpad.clearSqlResult' />"><span class="ui-button-icon ui-icon ui-icon-trash"></span><span class="ui-button-icon-space"> </span><@spring.message code='execute' /></button>
			</div>
			<div class="result-content ui-widget ui-widget-content"></div>
		</div>
	</div>
	<div class="foot">
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
	po.sqlpadChannelId = "${sqlpadChannelId}";
	
	po.sqlResultContentElement = po.element("#${pageId}-sql-result > .result-content");
	
	$.initButtons(po.element(".head"));
	po.element("#sqlCommitModeSet").buttonset();
	po.element("#sqlExceptionHandleModeSet").buttonset();
	po.element(".more-operation-panel").hide();
	
	$(document.body).bind("click", function(event)
	{
		if($(event.target).closest(po.element(".more-operation-wrapper")).length == 0)
			po.element(".more-operation-panel").hide();
	});
	
	po.sqlEditor = ace.edit("${pageId}-sql-editor");
	var SqlMode = ace.require("ace/mode/sql").Mode;
	po.sqlEditor.session.setMode(new SqlMode());
	po.sqlEditor.setShowPrintMargin(false);
	po.sqlEditor.focus();
	po.sqlEditor.navigateFileEnd();
	
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
	
	po.executeSql = function(sql, sqlStartRow, sqlStartColumn, commitMode, exceptionHandleMode)
	{
		if(po.cometdSubscribed)
		{
			po.requestExecuteSql(sql, sqlStartRow, sqlStartColumn, commitMode, exceptionHandleMode);
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
					
					po.requestExecuteSql(sql, sqlStartRow, sqlStartColumn, commitMode, exceptionHandleMode);
				}
			});
		}
	};
	
	po.requestExecuteSql = function(sql, sqlStartRow, sqlStartColumn, commitMode, exceptionHandleMode)
	{
		$.ajax(
		{
			type : "POST",
			url : "${contextPath}/sqlpad/"+po.schemaId+"/execute",
			data :
			{
				"sqlpadChannelId" : po.sqlpadChannelId,
				"sql" : sql,
				"sqlStartRow" : sqlStartRow,
				"sqlStartColumn" : sqlStartColumn,
				"commitMode" : commitMode,
				"exceptionHandleMode" : exceptionHandleMode
			},
			error : function()
			{
				po.element("#executeSqlButton").button("enable");
			}
		});
	},
	
	po.handleMessage = function(message)
	{
		var msgData = message.data;
		var msgDataType = (msgData ? msgData.type : "");
		var $msgDiv = $("<div class='execution-message' />");
		
		if(msgData.timeText)
			$("<span class='message-time' />").html("["+msgData.timeText+"] ").appendTo($msgDiv);
		
		var $msgContent = $("<span class='message-content' />").appendTo($msgDiv);
		
		if(msgDataType == "START")
		{
			$msgDiv.addClass("execution-start");
			$msgContent.html("<@spring.message code='sqlpad.executionStart' />");
		}
		else if(msgDataType == "SUCCESS")
		{
			var sqlStatement = msgData.sqlStatement;
			
			$msgDiv.addClass("execution-success");
			$msgContent.html("["+(msgData.sqlStatementIndex + 1)+"] " + $.truncateIf(sqlStatement.sql, "...", 38));
			
			<#assign messageArgs=['"+(sqlStatement.startRow+1)+"', '"+sqlStatement.startColumn+"', '"+(sqlStatement.endRow+1)+"', '"+sqlStatement.endColumn+"'] />
			$msgContent.attr("title", "<@spring.messageArgs code='sqlpad.executionSqlselectionRange' args=messageArgs />");
		}
		else if(msgDataType == "EXCEPTION")
		{
			$msgDiv.addClass("execution-exception");
			$msgContent.html(msgData.content);
		}
		else if(msgDataType == "FINISH")
		{
			$msgDiv.addClass("execution-finish");
			$msgContent.html("<@spring.message code='sqlpad.executeionFinish' />");
			
			po.element("#executeSqlButton").button("enable");
		}
		else
			$msgDiv = null;
		
		if($msgDiv)
		{
			$msgDiv.appendTo(po.sqlResultContentElement);
			po.sqlResultContentElement.scrollTop(po.sqlResultContentElement.prop("scrollHeight"));
		}
	},
	
	po.element("#executeSqlButton").click(function()
	{
		var $this = $(this);
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
		
		var cometd = $.cometd;
		
		$this.button("disable");
		
		if(!$.isCometdInit)
		{
			$.isCometdInit = true;
			cometd.init("${contextPath}/cometd", function(handshakeReply)
			{
				if(handshakeReply.successful)
				{
					po.executeSql(sql, sqlStartRow, sqlStartColumn, commitMode, exceptionHandleMode);
				}
			});
		}
		else
			po.executeSql(sql, sqlStartRow, sqlStartColumn, commitMode, exceptionHandleMode);
	});
	
	po.element("#stopSqlButton").click(function()
	{
		console.log("stop sql");
	});
	
	po.element("#clearSqlButton").click(function()
	{
		var editor = po.sqlEditor;
		
		editor.setValue("");
		editor.focus();
	});
	
	po.element("#moreOperationButton").click(function()
	{
		po.element(".more-operation-panel").toggle();
	});
	
	po.element("input[name='sqlCommitMode']").change(function()
	{
		var value = $(this).val();
		
		console.log(value);
		
		if(value == "MANUAL")
		{
			po.element("#commitSqlButton").button("enable");
			po.element("#rollbackSqlButton").button("enable");
			
			var $rollbackExceptionHandle = po.element("input[name='sqlExceptionHandleMode'][value='ROLLBACK']");
			$rollbackExceptionHandle.attr("disabled", "disabled");
			if($rollbackExceptionHandle.is(":checked"))
				po.element("input[name='sqlExceptionHandleMode'][value='ABORT']").prop("checked", true);
			po.element("#sqlExceptionHandleModeSet").buttonset("refresh");
		}
		else
		{
			po.element("#commitSqlButton").button("disable");
			po.element("#rollbackSqlButton").button("disable");
			po.element("input[name='sqlExceptionHandleMode'][value='ROLLBACK']").removeAttr("disabled");
			po.element("#sqlExceptionHandleModeSet").buttonset("refresh");
		}
	});
	
	po.element("#toggleResultTimeButton").click(function()
	{
		var $this = $(this);
		
		if($this.hasClass("ui-state-active"))
		{
			po.element(".result-content").removeClass("hide-message-time");
			$(this).removeClass("ui-state-active");
		}
		else
		{
			po.element(".result-content").addClass("hide-message-time");
			$(this).addClass("ui-state-active");
		}
	});
	
	po.element("#clearResultButton").click(function()
	{
		po.sqlResultContentElement.empty();
	});
	
	po.element("input[name='sqlCommitMode'][value='AUTO']").click();
})
(${pageId});
</script>
</body>
</html>

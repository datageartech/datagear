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
		<button id="stopSqlButton" class="ui-button ui-corner-all ui-widget ui-button-icon-only" title="<@spring.message code='sqlpad.stopExecution' />"><span class="ui-button-icon ui-icon ui-icon-stop"></span><span class="ui-button-icon-space"> </span><@spring.message code='execute' /></button>
		<button id="clearSqlButton" class="ui-button ui-corner-all ui-widget ui-button-icon-only" title="<@spring.message code='sqlpad.clearEditSql' />"><span class="ui-button-icon ui-icon ui-icon-trash"></span><span class="ui-button-icon-space"> </span><@spring.message code='execute' /></button>
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
	
	po.executeSql = function(sql, sqlStartRow, sqlStartColumn)
	{
		if(po.cometdSubscribed)
		{
			po.requestExecuteSql(sql, sqlStartRow, sqlStartColumn);
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
					
					po.requestExecuteSql(sql, sqlStartRow, sqlStartColumn);
				}
			});
		}
	};
	
	po.requestExecuteSql = function(sql, sqlStartRow, sqlStartColumn)
	{
		$.ajax(
		{
			type : "POST",
			url : "${contextPath}/sqlpad/"+po.schemaId+"/execute",
			data : { "sqlpadChannelId" : po.sqlpadChannelId, "sql" : sql, "sqlStartRow" : sqlStartRow, "sqlStartColumn" : sqlStartColumn },
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
		
		var cometd = $.cometd;
		
		$this.button("disable");
		
		if(!$.isCometdInit)
		{
			$.isCometdInit = true;
			cometd.init("${contextPath}/cometd", function(handshakeReply)
			{
				if(handshakeReply.successful)
				{
					po.executeSql(sql, sqlStartRow, sqlStartColumn);
				}
			});
		}
		else
			po.executeSql(sql, sqlStartRow, sqlStartColumn);
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
})
(${pageId});
</script>
</body>
</html>

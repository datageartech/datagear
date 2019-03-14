<#include "../include/import_global.ftl">
<#include "../include/html_doctype.ftl">
<#--
Schema schema 数据库，不允许为null
-->
<html style="height:100%;">
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
<body style="height:100%;">
<#if !isAjaxRequest>
<div style="height:99%;">
</#if>
<div id="${pageId}" class="page-sqlpad">
	<div class="head">
		<button id="executeSqlButton" class="ui-button ui-corner-all ui-widget ui-button-icon-only first" title="<@spring.message code='sqlpad.executeWithShortcut' />"><span class="ui-button-icon ui-icon ui-icon-play"></span><span class="ui-button-icon-space"> </span><@spring.message code='execute' /></button>
		<button id="stopSqlButton" class="ui-button ui-corner-all ui-widget ui-button-icon-only" title="<@spring.message code='sqlpad.executeWithShortcut' />"><span class="ui-button-icon ui-icon ui-icon-stop"></span><span class="ui-button-icon-space"> </span><@spring.message code='execute' /></button>
		<button id="clearSqlButton" class="ui-button ui-corner-all ui-widget ui-button-icon-only" title="<@spring.message code='sqlpad.executeWithShortcut' />"><span class="ui-button-icon ui-icon ui-icon-trash"></span><span class="ui-button-icon-space"> </span><@spring.message code='execute' /></button>
	</div>
	<div class="content ui-widget ui-widget-content">
		<div class="content-editor">
			<div class="content-edit-content">
				<div id="${pageId}-sql-editor" class="sql-editor">select count(*) from t_order where id = 3 and name = 'jack';</div>
			</div>
		</div>
		<div id="${pageId}-sql-result" class="content-result">
		</div>
	</div>
	<div class="foot">
		foot
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
	
	po.sqlResultElement = po.element("#${pageId}-sql-result");
	
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
	
	po.executeSql = function(sql)
	{
		if(po.cometdSubscribed)
		{
			$.post("${contextPath}/sqlpad/"+po.schemaId+"/execute",
			{
				"sqlpadChannelId" : po.sqlpadChannelId,
				"sql" : sql
			});
		}
		else
		{
			var cometd = $.cometd;
			
			cometd.subscribe(po.sqlpadChannelId, function(message)
			{
				$("<p />").html(message.data).appendTo(po.sqlResultElement);
				
				po.sqlResultElement.scrollTop(po.sqlResultElement.prop("scrollHeight"));
			},
			function(subscribeReply)
			{
				if(subscribeReply.successful)
				{
					po.cometdSubscribed = true;
					
					$.post("${contextPath}/sqlpad/"+po.schemaId+"/execute",
					{
						"sqlpadChannelId" : po.sqlpadChannelId,
						"sql" : sql
					});
				}
				else
				{
					//TODO 处理订阅失败逻辑
				}
			});
		}
	};
	
	po.element("#executeSqlButton").click(function()
	{
		var editor = po.sqlEditor;
		
		var sql = editor.session.getTextRange(editor.getSelectionRange());
		if(!sql)
			sql = editor.getValue();
		
		if(!sql)
			return;
		
		var cometd = $.cometd;
		
		if(!$.isCometdInit)
		{
			$.isCometdInit = true;
			cometd.init("${contextPath}/cometd", function(handshakeReply)
			{
				if(handshakeReply.successful)
				{
					po.executeSql(sql);
				}
				else
				{
					//TODO 处理连接失败逻辑
				}
			});
		}
		else
			po.executeSql(sql);
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
})
(${pageId});
</script>
</body>
</html>

<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#assign CommitMode=statics['org.datagear.web.sqlpad.SqlpadExecutionService$CommitMode']>
<#assign ExceptionHandleMode=statics['org.datagear.web.sqlpad.SqlpadExecutionService$ExceptionHandleMode']>
<#assign SqlCommand=statics['org.datagear.web.sqlpad.SqlpadExecutionService$SqlCommand']>
<#assign SqlResultType=statics['org.datagear.web.sqlpad.SqlpadExecutionService$SqlResultType']>
<#include "../include/page_import.ftl">
<#include "../include/html_doctype.ftl">
<html>
<head>
<#include "../include/html_head.ftl">
<title>
	<#include "../include/html_app_name_prefix.ftl">
	<@spring.message code='module.sqlpad' />
</title>
</head>
<body class="p-card no-border">
<#include "../include/page_obj.ftl">
<div id="${pid}" class="page page-manager page-sqlpad">
	<div class="page-header grid align-items-center">
		<div class="col-12 flex">
			<div class="flex-grow-1 h-opts">
				<p-button type="button" class="px-4" @click="onExecute"
					:icon="pm.executionStatus == pm.executionStatusType.EXECUTING ? 'pi pi-pause' : 'pi pi-play'"
					title="<@spring.message code='sqlpad.executeWithShortcut' />">
				</p-button>
				<p-button type="button" icon="pi pi-stop" class="px-4" @click="onStop"
					title="<@spring.message code='stop' />">
				</p-button>
				<p-button type="button" icon="pi pi-check" class="p-button-secondary px-4 ml-4" @click="onCommit"
					title="<@spring.message code='commit' />">
				</p-button>
				<p-button type="button" icon="pi pi-undo" class="p-button-secondary px-4" @click="onRollback"
					title="<@spring.message code='rollback' />">
				</p-button>
				<span class="p-inputgroup inline-flex w-auto">
					<p-inputtext v-model="fm.sqlDelimiter" class="ml-4" style="width:6rem;"
						title="<@spring.message code='sqlpad.sqlDelimiter' />">
					</p-inputtext>
					<p-button type="button" icon="pi pi-align-left" class="p-button-secondary px-4" @click="onInsertSqlDelimiterDefine"
						title="<@spring.message code='sqlpad.insertSqlDelimiterDefine' />">
					</p-button>
					<p-button type="button" icon="pi pi-align-right" class="p-button-secondary px-4" @click="onInsertSqlDelimiter"
						title="<@spring.message code='sqlpad.insertSqlDelimiter' />">
					</p-button>
				</span>
				<p-button type="button" icon="pi pi-trash" class="p-button-secondary px-4 ml-4" @click="onClearSql"
					title="<@spring.message code='sqlpad.clearEditSql' />">
				</p-button>
			</div>
			<div class="flex-grow-0 text-right h-opts">
				<p-button type="button" icon="pi pi-history" class="p-button-secondary px-4"
					title="<@spring.message code='sqlpad.viewSqlHistory' />">
				</p-button>
				<p-button type="button" icon="pi pi-cog" class="p-button-secondary px-4"
					aria:haspopup="true" aria-controls="${pid}setPanelPanel"
					@click="onToggleSetPanel" title="<@spring.message code='set' />">
				</p-button>
			</div>
		</div>
	</div>
	<div class="page-content">
		<p-splitter layout="vertical" class="h-full">
			<p-splitterpanel :size="60" :min-size="20" class="overflow-auto">
				<form id="${pid}form" class="w-full h-full p-0 m-0">
					<div id="${pid}sql" class="code-editor-wrapper input p-component p-inputtext w-full h-full border-0">
						<div id="${pid}sqlEditor" class="code-editor"></div>
					</div>
				</form>
			</p-splitterpanel>
			<p-splitterpanel :size="40" :min-size="20" class="overflow-auto">
				<div class="sqlpad-tabs-wrapper w-full h-full">
					<p-tabview v-model:active-index="pm.sqlpadTabs.activeIndex" :scrollable="true" @tab-change="onSqlpadTabChange"
						@tab-click="onSqlpadTabClick" class="contextmenu-tabview light-tabview h-full relative" :class="{'opacity-0': pm.sqlpadTabs.items.length == 0}">
						<p-tabpanel v-for="tab in pm.sqlpadTabs.items" :key="tab.id" :header="tab.title">
							<template #header>
								<p-button type="button" icon="pi pi-angle-down"
									class="context-menu-btn p-button-xs p-button-secondary p-button-text p-button-rounded"
									@click="onSqlpadTabMenuToggle($event, tab)" aria-haspopup="true" aria-controls="${pid}sqlpadTabMenu">
								</p-button>
							</template>
							<div :id="tab.id" class="h-full"></div>
						</p-tabpanel>
					</p-tabview>
					<p-menu id="${pid}sqlpadTabMenu" ref="${pid}sqlpadTabMenuEle" :model="pm.sqlpadTabMenuItems" :popup="true" class="text-sm"></p-menu>
				</div>
			</p-splitterpanel>
		</p-splitter>
	</div>
	<p-overlaypanel ref="${pid}setPanelEle" append-to="body"
		@show="onSetPanelShow" :show-close-icon="false" id="${pid}setPanel">
		<form id="${pid}setForm" action="#">
			<div class="pb-2">
				<label class="text-lg font-bold">
					<@spring.message code='set' />
				</label>
			</div>
			<div class="p-2">
				<div class="field grid">
					<label for="${pid}commitMode" class="field-label col-12 mb-2">
						<@spring.message code='sqlpad.sqlCommitMode' />
					</label>
					<div class="field-input col-12">
						<p-selectbutton v-model="pm.setFormModel.commitMode" :options="pm.commitModeOptions"
			        		option-label="name" option-value="value" class="input w-full">
			        	</p-selectbutton>
					</div>
				</div>
				<div class="field grid">
					<label for="${pid}exceptionHandleMode" class="field-label col-12 mb-2">
						<@spring.message code='sqlpad.sqlExceptionHandleMode' />
					</label>
					<div class="field-input col-12">
						<p-selectbutton v-model="pm.setFormModel.exceptionHandleMode" :options="pm.exceptionHandleModeOptions"
			        		option-label="name" option-value="value" class="input w-full">
			        	</p-selectbutton>
					</div>
				</div>
				<div class="field grid">
					<label for="${pid}overTimeThreashold" class="field-label col-12 mb-2"
						title="<@spring.message code='sqlpad.overTimeThreashold.desc' />">
						<@spring.message code='sqlpad.overTimeThreashold' />
					</label>
					<div class="field-input col-12">
						<div class="p-inputgroup">
							<p-inputtext id="${pid}overTimeThreashold" v-model="pm.setFormModel.overTimeThreashold" type="text"
				        		name="overTimeThreashold" class="input" required maxlength="10">
				        	</p-inputtext>
				        	<span class="p-inputgroup-addon"><@spring.message code='sqlpad.overTimeThreashold.unit' /></span>
			        	</div>
					</div>
				</div>
				<div class="field grid">
					<label for="${pid}resultsetFetchSize" class="field-label col-12 mb-2"
						title="<@spring.message code='sqlpad.resultsetFetchSize.desc' />">
						<@spring.message code='sqlpad.resultsetFetchSize' />
					</label>
					<div class="field-input col-12">
						<p-inputtext id="${pid}resultsetFetchSize" v-model="pm.setFormModel.resultsetFetchSize" type="text"
			        		name="resultsetFetchSize" class="input w-full" required maxlength="10">
			        	</p-inputtext>
					</div>
				</div>
			</div>
			<div class="pt-3 text-center">
				<p-button type="submit" label="<@spring.message code='confirm' />"></p-button>
			</div>
		</form>
	</p-overlaypanel>
</div>
<#include "../include/page_form.ftl">
<#include "../include/page_simple_form.ftl">
<#include "../include/page_code_editor.ftl">
<#include "../include/page_sql_editor.ftl">
<#include "../include/page_tabview.ftl">
<#include "../include/page_format_time.ftl">
<script>
(function(po)
{
	po.commitModel =
	{
		AUTO: "AUTO", //CommitMode.AUTO
		MANUAL: "MANUAL" //CommitMode.MANUAL
	};

	po.exceptionHandleMode =
	{
		ABORT: "ABORT", //ExceptionHandleMode.ABORT
		IGNORE: "IGNORE", //ExceptionHandleMode.IGNORE
		ROLLBACK: "ROLLBACK" //ExceptionHandleMode.ROLLBACK
	};
	
	po.executionStatusType =
	{
		STOPPED: "STOPPED",
		PAUSED: "PAUSED",
		EXECUTING: "EXECUTING"
	};
	
	po.sqlCommandType =
	{
		COMMIT: "COMMIT", //SqlCommand.COMMIT
		ROLLBACK: "ROLLBACK", //SqlCommand.ROLLBACK
		PAUSE: "PAUSE", //SqlCommand.PAUSE
		RESUME: "RESUME", //SqlCommand.RESUME
		STOP: "STOP", //SqlCommand.STOP
	};
	
	po.sqlResultType =
	{
		RESULT_SET: "RESULT_SET", //SqlResultType.RESULT_SET
		UPDATE_COUNT: "UPDATE_COUNT", //SqlResultType.UPDATE_COUNT
		NONE: "NONE" //SqlResultType.NONE
	};
	
	po.messageType =
	{
		START: "START", //SqlpadExecutionService.StartMessageData
		SQLSUCCESS: "SQLSUCCESS", //SqlpadExecutionService.SqlSuccessMessageData
		EXCEPTION: "EXCEPTION", //SqlpadExecutionService.ExceptionMessageData
		SQLEXCEPTION: "SQLEXCEPTION", //SqlpadExecutionService.SQLExceptionMessageData
		SQLCOMMAND: "SQLCOMMAND", //SqlpadExecutionService.SqlCommandMessageData
		TEXT: "TEXT", //SqlpadExecutionService.TextMessageData
		FINISH: "FINISH" //SqlpadExecutionService.FinishMessageData
	};
	
	po.msgsTabPanelId = $.uid("msgspanel");
	
	var formModel = $.unescapeHtmlForJson(<@writeJson var=formModel />);
	
	po.schemaId = formModel.schemaId;
	po.sqlpadId = formModel.sqlpadId;
	po.submitUrl = "/sqlpad/"+encodeURIComponent(po.schemaId)+"/execute";
	
	po.getSqlEditorSchemaId = function()
	{
		return po.schemaId;
	};

	po.getSqlDelimiter = function()
	{
		var fm = po.vueFormModel();
		if(fm.sqlDelimiter)
			fm.sqlDelimiter = ";";
		return fm.sqlDelimiter;
	};
	
	po.executionStatus = function(status)
	{
		var pm = po.vuePageModel();
		
		if(status == null)
			return pm.executionStatus;
		
		pm.executionStatus = status;
	};
	
	po.handleExecute = function()
	{
		//TODO
		var executionStatus = po.executionStatus();
		
		if(executionStatus == po.executionStatusType.PAUSED)
		{
			po.sqlpadTaskClient.resume();
			po.sendSqlCommand(po.sqlCommandType.RESUME);
		}
		else if(executionStatus == po.executionStatusType.EXECUTING)
		{
			po.sendSqlCommand(po.sqlCommandType.PAUSE);
		}
		else
		{
			var selInfo = po.getSelectedCodeInfo(po.sqlEditor);
			var sql = (selInfo.text ? selInfo.text : po.getCodeText(po.sqlEditor));
			var sqlStartRow = (selInfo.text && selInfo.from ? selInfo.from.line : po.sqlEditor.getDoc().firstLine());
			var sqlStartColumn = (selInfo.text && selInfo.from ? selInfo.from.ch : 0);

			if(!sql)
				return;
			
			if(po.sqlpadTaskClient.isActive())
				return;
			
			po.executionStatus(po.executionStatusType.EXECUTING);
			po.sqlpadTaskClient.start();
			po.executeSql(sql, sqlStartRow, sqlStartColumn);
		}
	};
	
	po.handleStop = function()
	{
		if(po.sqlpadTaskClient.isActive())
		{
			//如果挂起，则应唤醒接收命令响应
			if(po.sqlpadTaskClient.isSuspend())
				po.sqlpadTaskClient.resume();
			
			po.sendSqlCommand(po.sqlCommandType.STOP);
		}
		
		po.sqlEditor.focus();
	};
	
	po.handleCommit = function()
	{
		if(po.sqlpadTaskClient.isActive())
		{
			//如果挂起，则应唤醒接收命令响应
			if(po.sqlpadTaskClient.isSuspend())
				po.sqlpadTaskClient.resume();
			
			po.sendSqlCommand(po.sqlCommandType.COMMIT);
		}
		
		po.sqlEditor.focus();
	};
	
	po.handleRollback = function()
	{
		if(po.sqlpadTaskClient.isActive())
		{
			//如果挂起，则应唤醒接收命令响应
			if(po.sqlpadTaskClient.isSuspend())
				po.sqlpadTaskClient.resume();
			
			po.sendSqlCommand(po.sqlCommandType.ROLLBACK);
		}
		
		po.sqlEditor.focus();
	};
	
	po.executeSql = function(sql, sqlStartRow, sqlStartColumn)
	{
		po.clearMessage(false);
		
		var fm = po.vueFormModel();
		var data = po.vueRaw(fm);
		data.sql = sql;
		data.sqlStartRow = sqlStartRow;
		data.sqlStartColumn = sqlStartColumn;
		
		po.ajax("/sqlpad/"+encodeURIComponent(po.schemaId)+"/execute",
		{
			type : "POST",
			data : data,
			error : function()
			{
				po.sqlpadTaskClient.stop();
				var pm = po.vuePageModel();
				pm.executionStatus = po.executionStatusType.STOPPED;
			}
		});
	},
	
	po.sendSqlCommand = function(sqlCommand)
	{
		if(po._sendingSqlCommand == true)
			return;
		
		po._sendingSqlCommand = true;
		
		po.ajax("/sqlpad/"+po.schemaId+"/command",
		{
			type: "POST",
			data:
			{
				"sqlpadId" : po.sqlpadId,
				"command" : sqlCommand
			},
			complete: function()
			{
				po._sendingSqlCommand = false;
			}
		});
	};
	
	po.resultMsgsWrapper = function()
	{
		var panel = po.elementOfId(po.msgsTabPanelId);
		var msgsWrapperId = "${pid}resultMsgsWrapper";
		var msgsWrapper = po.elementOfId(msgsWrapperId, panel);
		if(msgsWrapper.length == 0)
			msgsWrapper = $("<div id='"+msgsWrapperId+"' class='result-message text-sm w-full h-full overflow-auto' />").appendTo(panel);
		
		return msgsWrapper;
	};
	
	po.handleMessage = function(message)
	{
		var isFinish = false;
		var msgData = message;
		var msgDataType = (msgData ? msgData.type : "");
		var msgDiv = $("<div class='execution-message flex align-items-center mb-1' />");
		
		if(msgData.timeText)
			$("<div class='message-time mr-2' />").html("["+msgData.timeText+"] ").appendTo(msgDiv);
		
		var msgContentDiv = $("<div class='message-content flex align-items-center' />").appendTo(msgDiv);
		
		if(msgDataType == po.messageType.START)
		{
			po.executingSqlCount = msgData.sqlCount;
			
			msgDiv.addClass("execution-start");
			$("<div />").html($.validator.format("<@spring.message code='sqlpad.executionStart' />", msgData.sqlCount))
				.appendTo(msgContentDiv);
		}
		else if(msgDataType == po.messageType.SQLSUCCESS)
		{
			msgDiv.addClass("execution-success");
			
			po.appendSqlStatementMessage(msgContentDiv, msgData.sqlStatement, msgData.sqlStatementIndex);
			
			if(msgData.sqlResultType == po.sqlResultType.UPDATE_COUNT)
			{
				var text = $.validator.format("<@spring.message code='sqlpad.affectDataRowCount' />", msgData.updateCount);
				$("<div class='inline-block sql-update-count' />").text(text).appendTo(msgContentDiv);
			}
			else if(msgData.sqlResultType == po.sqlResultType.RESULT_SET)
			{
				var tabId = po.nextSqlResultTabId();
				po.renderSqlResultTab(tabId, msgData.sqlStatement.sql, msgData.sqlSelectResult, (po.executingSqlCount == 1));
				
				$("<a href='javascript:void(0);' class='sql-result-link link' />")
					.html("<@spring.message code='viewResult' />")
					.data("tabId", tabId)
					.data("sql", msgData.sqlStatement.sql)
					.appendTo(msgContentDiv)
					.click(function()
					{
						var pm = po.vuePageModel();
						var myTabIdx = po.tabviewTabActive(pm.sqlpadTabs, $(this).data("tabId"));
						
						if(myTabIdx < 0)
							$.tipInfo("<@spring.message code='sqlpad.selectResultExpired' />");
					});
			}
			else
				;
		}
		else if(msgDataType == po.messageType.SQLEXCEPTION)
		{
			msgDiv.addClass("execution-exception p-error");
			
			po.appendSqlStatementMessage(msgContentDiv, msgData.sqlStatement, msgData.sqlStatementIndex);
			$("<div class='sql-exception-summary' />").html(msgData.content).appendTo(msgContentDiv);
		}
		else if(msgDataType == po.messageType.SQLCOMMAND)
		{
			var appendContent = true;
			
			if(msgData.sqlCommand == po.sqlCommandType.RESUME)
			{
				po.executionStatus(po.executionStatusType.EXECUTING);
				appendContent = false;
				msgDiv = null;
			}
			else if(msgData.sqlCommand == po.sqlCommandType.PAUSE)
			{
				po.executionStatus(po.executionStatusType.PAUSED);
			}
			
			if(appendContent)
			{
				$("<div class='inline-block' />").html(msgData.content).appendTo(msgContentDiv);
				po.appendSQLExecutionStatMessage(msgContentDiv, msgData.sqlExecutionStat);
			}
		}
		else if(msgDataType == po.messageType.EXCEPTION)
		{
			msgDiv.addClass("execution-exception p-error");
			$("<div class='inline-block' />").html(msgData.content).appendTo(msgContentDiv);
		}
		else if(msgDataType == po.messageType.TEXT)
		{
			msgDiv.addClass("execution-text");
			
			if(msgData.cssClass)
				msgContentDiv.addClass(msgData.cssClass);
			
			$("<div class='inline-block' />").html(msgData.text).appendTo(msgContentDiv);
			po.appendSQLExecutionStatMessage(msgContentDiv, msgData.sqlExecutionStat);
		}
		else if(msgDataType == po.messageType.FINISH)
		{
			isFinish = true;
			po.executingSqlCount = -1;
			
			msgDiv.addClass("execution-finish");
			
			$("<div class='inline-block p-tag p-tag-success' />").html("<@spring.message code='sqlpad.executeionFinish' />").appendTo(msgContentDiv);
			po.appendSQLExecutionStatMessage(msgContentDiv, msgData.sqlExecutionStat);
			
			po.executionStatus(po.executionStatusType.STOPPED);
		}
		else
			msgDiv = null;
		
		if(msgDiv)
		{
			var msgsWrapper = po.resultMsgsWrapper();
			
			msgDiv.appendTo(msgsWrapper);
			msgsWrapper.scrollTop(msgsWrapper.prop("scrollHeight"));
		}
		
		//如果在暂停，则应挂起（比如暂停时执行命令）；否则，应唤醒（比如暂停超时）
		if(po.executionStatus() == po.executionStatusType.PAUSED)
			po.sqlpadTaskClient.suspend();
		else
			po.sqlpadTaskClient.resume();
		
		return isFinish;
	};
	
	po.renderSqlResultTab = function(tabId, sql, sqlSelectResult, active)
	{
		var pm = po.vuePageModel();
		var tabIndex = po.tabviewTabIndex(pm.sqlpadTabs, tabId);
		var tab = po.tabviewTab(pm.sqlpadTabs, tabId);
		
		if(tab)
		{
			var tabPanel = po.elementOfId(tab.id);
			tabPanel.empty();
		}
		else
		{
			var nameSeq = po.nextSqlResultTabNameSeq();
			var tabTitle = $.validator.format("<@spring.message code='sqlpad.selectResultWithIndex' />", nameSeq);
			
			tab =
			{
				id: tabId,
				title: tabTitle,
				sql: sql,
				result: sqlSelectResult,
				type: "resultSet"
			};
			
			pm.sqlpadTabs.items.push(tab);
			tabIndex = pm.sqlpadTabs.items.length - 1;
		}
		
		if(active)
			pm.sqlpadTabs.activeIndex = tabIndex;
	};
	
	po.nextSqlResultTabId = function()
	{
		return $.uid("sqlresulttab");
	};
	
	po.nextSqlResultTabNameSeq = function()
	{
		var pm = po.vuePageModel();
		if(pm.sqlpadTabs.items.length == 1)
			po._nextSqlResultTabNameSeq = null;
		
		var seq = (po._nextSqlResultTabNameSeq == null ? 1 : po._nextSqlResultTabNameSeq);
		po._nextSqlResultTabNameSeq = seq + 1;
		
		return seq;
	};
	
	po.appendSqlStatementMessage = function(msgContentDiv, sqlStatement, sqlStatementIndex)
	{
		if(sqlStatement == null)
			return;
		
		$("<div class='sql-index mr-2'>["+(sqlStatementIndex + 1)+"]</div>").appendTo(msgContentDiv);
		var sqlValueEle = $("<div class='sql-value cursor-pointer mr-3' />").text($.truncateIf(sqlStatement.sql, "...", 100)).appendTo(msgContentDiv);
		
		sqlValueEle.click(function()
		{
			po.sqlEditor.setSelection({line: sqlStatement.startRow, ch: sqlStatement.startColumn},
					{line: sqlStatement.endRow, ch: sqlStatement.endColumn});
		});
		
		var title = $.validator.format("<@spring.message code='sqlpad.executionSqlselectionRange' />",
				sqlStatement.startRow+1, sqlStatement.startColumn, sqlStatement.endRow+1, sqlStatement.endColumn);
		sqlValueEle.attr("title", title);
	};
	
	po.appendSQLExecutionStatMessage = function(msgContentDiv, sqlExecutionStat)
	{
		if(sqlExecutionStat == null)
			return;
		
		var text = "<@spring.message code='sqlpad.sqlExecutionStat.quoteLeft' />";
		
		text += $.validator.format("<@spring.message code='sqlpad.sqlExecutionStat.infoNoDuration' />",
				sqlExecutionStat.totalCount, sqlExecutionStat.successCount, sqlExecutionStat.exceptionCount, sqlExecutionStat.abortCount);
		
		if(sqlExecutionStat.sqlDuration >= 0)
		{
			text += $.validator.format("<@spring.message code='sqlpad.sqlExecutionStat.infoSqlDurationSuffix' />",
						po.formatDuration(sqlExecutionStat.sqlDuration));
		}
		
		if(sqlExecutionStat.taskDuration >= 0)
		{
			text += $.validator.format("<@spring.message code='sqlpad.sqlExecutionStat.infoTaskDurationSuffix' />",
						po.formatDuration(sqlExecutionStat.taskDuration));
		}
		
		text += "<@spring.message code='sqlpad.sqlExecutionStat.quoteRight' />";
		
		$("<div class='sql-stat' />").text(text).appendTo(msgContentDiv);
	};
	
	po.clearMessage = function(force)
	{
		force = (force == null ? true : force);
		
		if(!force && po.keepMessage())
			return;
		
		var msgsWrapper = po.resultMsgsWrapper();
		msgsWrapper.empty();
	};
	
	po.isSqlpadTabMenuOnTabResultSet = function()
	{
		var tab = po.sqlpadTabMenuOnTab;
		return (tab && tab.type == "resultSet");
	};

	po.isSqlpadTabMenuOnTabMessage = function()
	{
		var tab = po.sqlpadTabMenuOnTab;
		return (tab && tab.type == "message");
	};
	
	po.keepMessage = function(keep)
	{
		if(keep === undefined)
			return (po._keepMessage == true);
		
		po._keepMessage = keep;
	};
	
	po.setupForm(formModel);
	
	po.vuePageModel(
	{
		executionStatus: po.executionStatusType.STOPPED,
		executionStatusType: po.executionStatusType,
		setFormModel:
		{
			commitMode: formModel.commitMode,
			exceptionHandleMode: formModel.exceptionHandleMode,
			overTimeThreashold: formModel.overTimeThreashold,
			resultsetFetchSize: formModel.resultsetFetchSize
		},
		commitModeOptions:
		[
			{ name: "<@spring.message code='auto' />", value: po.commitModel.AUTO },
			{ name: "<@spring.message code='manual' />", value: po.commitModel.MANUAL }
		],
		exceptionHandleModeOptions:
		[
			{ name: "<@spring.message code='abort' />", value: po.exceptionHandleMode.ABORT },
			{ name: "<@spring.message code='ignore' />", value: po.exceptionHandleMode.IGNORE },
			{ name: "<@spring.message code='rollback' />", value: po.exceptionHandleMode.ROLLBACK }
		],
		sqlpadTabs:
		{
			items:
			[
				{
					id: po.msgsTabPanelId,
					title: "<@spring.message code='message' />",
					closeable: false,
					type: "message"
				}
			],
			activeIndex: 0
		},
		sqlpadTabMenuItems:
		[
			{
				label: "<@spring.message code='sqlpad.clearMessage' />",
				visible: function()
				{
					return po.isSqlpadTabMenuOnTabMessage();
				},
				command: function()
				{
					po.clearMessage();
				}
			},
			{
				label: "<@spring.message code='sqlpad.isKeepMessage' />",
				icon: "pi pi-times",
				visible: function()
				{
					return po.isSqlpadTabMenuOnTabMessage();
				},
				command: function()
				{
					po.keepMessage(!po.keepMessage());
					
					if(po.keepMessage())
						this.icon = "pi pi-check";
					else
						this.icon = "pi pi-times";
				}
			},
			{
				label: "<@spring.message code='close' />",
				visible: function()
				{
					return po.isSqlpadTabMenuOnTabResultSet();
				},
				command: function()
				{
					po.tabviewClose(po.vuePageModel().sqlpadTabs, po.sqlpadTabMenuOnTab.id);
				}
			},
			{
				label: "<@spring.message code='closeOther' />",
				visible: function()
				{
					return po.isSqlpadTabMenuOnTabResultSet();
				},
				command: function()
				{
					po.tabviewCloseOther(po.vuePageModel().sqlpadTabs, po.sqlpadTabMenuOnTab.id);
				}
			},
			{
				label: "<@spring.message code='closeRight' />",
				visible: function()
				{
					return po.isSqlpadTabMenuOnTabResultSet();
				},
				command: function()
				{
					po.tabviewCloseRight(po.vuePageModel().sqlpadTabs, po.sqlpadTabMenuOnTab.id);
				}
			},
			{
				label: "<@spring.message code='closeLeft' />",
				visible: function()
				{
					return po.isSqlpadTabMenuOnTabResultSet();
				},
				command: function()
				{
					po.tabviewCloseLeft(po.vuePageModel().sqlpadTabs, po.sqlpadTabMenuOnTab.id);
				}
			},
			{
				label: "<@spring.message code='closeAll' />",
				visible: function()
				{
					return po.isSqlpadTabMenuOnTabResultSet();
				},
				command: function()
				{
					po.tabviewCloseAll(po.vuePageModel().sqlpadTabs);
				}
			}
		]
	});
	
	po.vueRef("${pid}sqlpadTabMenuEle", null);
	po.vueRef("${pid}setPanelEle", null);
	
	po.vueMethod(
	{
		onExecute: function()
		{
			po.handleExecute();
		},
		
		onStop: function()
		{
			po.handleStop();
		},
		
		onCommit: function()
		{
			po.handleCommit();
		},
		
		onRollback: function()
		{
			po.handleRollback();
		},
		
		onInsertSqlDelimiterDefine: function()
		{
			var delimiter = po.getSqlDelimiter();
			
			var text = "";
			var cursor = po.sqlEditor.getDoc().getCursor();
			
			if(cursor.ch == 0)
			{
				if(cursor.line != 0)
					text += "\n";
			}
			else
				text += "\n\n";
			
			text += "--@DELIMITER "+delimiter+"\n";
			
			po.insertCodeText(po.sqlEditor, text);
			
			po.sqlEditor.focus();
		},
		
		onInsertSqlDelimiter: function()
		{
			var delimiter = po.getSqlDelimiter();
			po.insertCodeText(po.sqlEditor, delimiter+"\n");
			
			po.sqlEditor.focus();
		},
		
		onClearSql: function()
		{
			po.sqlEditor.setValue("");
			po.sqlEditor.focus();
		},
		
		onSqlpadTabChange: function(e){},
		
		onSqlpadTabClick: function(e){},
		
		onSqlpadTabMenuToggle: function(e, tab)
		{
			po.sqlpadTabMenuOnTab = tab;
			po.vueUnref("${pid}sqlpadTabMenuEle").show(e);
		},
		
		onToggleSetPanel: function(e)
		{
			var pm = po.vuePageModel();
			po.vueUnref("${pid}setPanelEle").toggle(e);
		},
		
		onSetPanelShow: function(e)
		{
			var pm = po.vuePageModel();
			var panel = po.elementOfId("${pid}setPanel", document.body);
			var form = po.elementOfId("${pid}setForm", panel);
			po.elementOfId("${pid}addResName", form).focus();
			
			po.setupSimpleForm(form, pm.setFormModel, 
			{
				rules :
				{
					overTimeThreashold : { integer : true, min : 1, max : 60 },
					resultsetFetchSize : { integer : true, min : 1, max : 1000 }
				},
				submitHandler:function()
				{
					var fm = po.vueFormModel();
					fm.commitMode = pm.setFormModel.commitMode,
					fm.exceptionHandleMode = pm.setFormModel.exceptionHandleMode,
					fm.overTimeThreashold = pm.setFormModel.overTimeThreashold,
					fm.resultsetFetchSize = pm.setFormModel.resultsetFetchSize
					
					po.vueUnref("${pid}setPanelEle").hide();
				}
			});
		},
	});
	
	po.vueMounted(function()
	{
		po.sqlEditor = po.createSqlEditor(po.elementOfId("${pid}sqlEditor"));
		po.setCodeTextTimeout(po.sqlEditor, formModel.sql, true);
		
		po.sqlpadTaskClient = new $.TaskClient(po.concatContextPath("/sqlpad/"+encodeURIComponent(po.schemaId)+"/message"),
		function(message)
		{
			return po.handleMessage(message);
		},
		{
			data: { sqlpadId: po.sqlpadId }
		});
	});
	
	po.vueMount();
})
(${pid});
</script>
</body>
</html>
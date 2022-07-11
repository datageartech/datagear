<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#--
Schema schema 数据库，不允许为null
String dataExchangeId 数据交换ID，不允许为null

依赖：
page_obj_format_time.ftl
page_obj_grid.ftl

变量
po.subDataExchangeStatusColumnIndex 子数据交换表格中状态列索引
-->
<script type="text/javascript">
(function(po)
{
	po.schemaId = "${schema.id}";
	po.dataExchangeId = "${dataExchangeId}";
	
	po.getSubDataExchangeDataTable = function()
	{
		return po.tableDataTable();
	};

	po.getDataExchangeProgressEle = function()
	{
		return po.elementOfId("${pageId}-progress");
	};

	po.getDataExchangeProgressPercentEle = function()
	{
		return po.elementOfId("${pageId}-progress-percent");
	};
	
	po.getSubDataExchangeExceptionTipEle = function()
	{
		return po.elementOfId("${pageId}-exchange-exception-tooltip");
	};
	
	po.nextSubDataExchangeId = function()
	{
		if(!po.nextSubDataExchangeIdSeq)
			po.nextSubDataExchangeIdSeq = 0;
		
		return po.dataExchangeId + "_" + (po.nextSubDataExchangeIdSeq++);
	};
	
	po.currentSubDataExchangeId = function()
	{
		if(!po.nextSubDataExchangeIdSeq)
			po.nextSubDataExchangeIdSeq = 0;
		
		return po.nextSubDataExchangeIdSeq;
	};

	po.dataExchangeTaskClient = new $.TaskClient("${contextPath}/dataexchange/"+po.schemaId+"/message",
			function(message)
			{
				return po.handleDataExchangeMessage(message);
			},
			{
				data: { dataExchangeId: po.dataExchangeId }
			}
		);
	
	po.evalTableHeight = function()
	{
		var height =  po.element(".form-content-wizard > .content").height() - po.element(".form-item-table-head:not(:hidden)").outerHeight(true) - 60;
		return height;
	};
	
	po.adjustDataTable = function()
	{
		$.dataTableUtil.adjustColumn(po.getSubDataExchangeDataTable());
		$.updateDataTableHeight(po.table(), po.evalTableHeight());
	};

	po.renderColumn = function(data, type, row, meta)
	{
		return $.escapeHtml($.truncateIf(data));
	};
	
	po.setDataExchangeProgress = function(progressValue, duration)
	{
		po.getDataExchangeProgressEle().progressbar({ value: progressValue });
		
		var percentText = progressValue + "%";
		
		if(duration != null)
		{
			var duration = po.formatDuration(duration);
			
			<#assign messageArgs=['"+progressValue+"', '"+duration+"'] />
			percentText = "<@spring.messageArgs code='dataExchange.exchangeProgressPercentWithDuration' args=messageArgs />";
		}
		
		po.getDataExchangeProgressPercentEle().text(percentText);
	};
	
	po.cancelSelectedSubDataExchange = function()
	{
		po.executeOnSelects(function(rowDatas, rowIndexes)
		{
			var cancelIds = [];
			
			for(var i=0; i<rowDatas.length; i++)
			{
				var status = rowDatas[i].status;
				
				if(!status || status == ""
						|| status == "<@spring.message code='dataExchange.exchangeStatus.Unstart' />"
						|| status == "<@spring.message code='dataExchange.exchangeStatus.SubSubmitSuccess' />")
				{
					var subDataExchangeId = rowDatas[i].subDataExchangeId;
					cancelIds.push({"name" : "subDataExchangeId", value : subDataExchangeId});
				}
				else
				{
					if(rowDatas.length == 1)
					{
						$.tipInfo("<@spring.message code='dataExchange.cancelDeniedWithReason' />");
						return;
					}
				}
			}
			
			if(cancelIds.length > 0)
			{
				cancelIds.push({"name" : "dataExchangeId", "value" : po.dataExchangeId});
				$.post("${contextPath}/dataexchange/" + po.schemaId +"/cancel", cancelIds);
			}
		},
		po.getSubDataExchangeDataTable());
	};
	
	po.getSubDataExchangeRowData = function(subDataExchangeId)
	{
		var dataTable = po.getSubDataExchangeDataTable();
		var rowIndex = po.getSubDataExchangeRowIndex(dataTable, subDataExchangeId);
		
		if(rowIndex < 0)
			return null;
		
		return dataTable.row(rowIndex).data();
	};
	
	po.getSubDataExchangeRowIndex = function(dataTable, subDataExchangeId)
	{
		if(!po.subDataExchangeIdRowIndexMap)
			po.subDataExchangeIdRowIndexMap = {};
		
		var rowIndex = po.subDataExchangeIdRowIndexMap[subDataExchangeId];
		
		if(rowIndex > -1)
			return rowIndex;
		
		rowIndex = -1;
		
		var rowDatas = dataTable.rows().data();
		for(var i=0; i<rowDatas.length; i++)
		{
			if(rowDatas[i].subDataExchangeId == subDataExchangeId)
			{
				rowIndex = i;
				break;
			}
		}
		
		po.subDataExchangeIdRowIndexMap[subDataExchangeId] = rowIndex;
		
		return rowIndex;
	};
	
	po.showSubExceptionTip = function(event, tipEle)
	{
		tipEle = $(tipEle);
		
		var subDataExchangeId = tipEle.attr("subDataExchangeId");
		var exception = po.subDataExchangeExceptionMessages[subDataExchangeId];
		
		if(!exception)
			return;
		
		var $tooltip = po.getSubDataExchangeExceptionTipEle();
		
		try{ $tooltip.tooltip("destroy"); }catch(e){}
		$tooltip.tooltip({"classes" : { "ui-tooltip" : "import-exception-tooltip ui-state-error ui-corner-all ui-widget-shadow"}});
		$tooltip.tooltip("option", "content", exception);
		$tooltip.tooltip("option", "position", { my: "center top", at: "center bottom-1", of: tipEle, collision: "flipfit" });
		$tooltip.tooltip("open");
	};
	
	po.hideSubExceptionTip = function(event, tipEle)
	{
		po.getSubDataExchangeExceptionTipEle().tooltip("close");
	};
	
	po.viewSubDataExchangeDetailLog = function(subDataExchangeId)
	{
		po.open("${contextPath}/dataexchange/" + po.schemaId +"/viewLog",
		{
			title : "<@spring.message code='dataExchange.viewLog' />",
			data :
			{
				schemaId : po.schemaId,
				dataExchangeId : po.dataExchangeId,
				subDataExchangeId : subDataExchangeId
			},
			height : $(window).height() * 0.75
		});
	};
	
	po.isDataExchangePageStatus = function(status)
	{
		return (status == po.currentDataExchangePageStatus);
	};
	
	//更新页面状态，status：edit、exchange、finish
	po.updateDataExchangePageStatus = function(status)
	{
		if(status == po.currentDataExchangePageStatus)
			return false;
		
		po.element(".page-status-aware-show:not(."+status+"-status-show)").hide();
		po.element(".page-status-aware-enable:not(."+status+"-status-enable)").addClass("ui-state-disabled");
		
		po.element(".page-status-aware-show."+status+"-status-show").show();
		po.element(".page-status-aware-enable."+status+"-status-enable").removeClass("ui-state-disabled");
		
		var $zfne = po.elementOfName("zipFileNameEncoding");
		
		if("edit" == status)
		{
			po.elementOfName("fileEncoding").selectmenu("enable");
			
			if($zfne.length > 0)
				$zfne.selectmenu("enable");
			
			po.setDataExchangeProgress(0);
		}
		else
		{
			po.elementOfName("fileEncoding").selectmenu("disable");

			if($zfne.length > 0)
				$zfne.selectmenu("disable");
		}
		
		po.currentDataExchangePageStatus = status;
		
		return true;
	};
	
	po.handleDataExchangeMessage = function(message)
	{
		var isFinish = false;
		var type = (message ? message.type : "");
		
		if("Start" == type)
		{
			var dataTable = po.getSubDataExchangeDataTable();
			po.subDataExchangeCount = dataTable.rows().indexes().length;
			po.subDataExchangeFinishCount=0;
			po.subDataExchangeExceptionMessages = {};
			po.subDataExchangeIdRowIndexMap = {};
			po.subDataExchangeMessageCache = {};
			
			po.updateDataExchangePageStatus("exchange");
		}
		else if("Exception" == type)
		{
			<#assign messageArgs=['"+message.content+"'] />
			$.tipError("<@spring.messageArgs code='dataExchange.exchangeStatus.Exception' args=messageArgs />");
		}
		else if("Finish" == type)
		{
			isFinish = true;
			po.refreshSubDataExchangeStatus();
			po.setDataExchangeProgress(100, message.duration);
			po.updateDataExchangePageStatus("finish");
		}
		else
			po.handleSubDataExchangeMessage(message);
		
		return isFinish;
	};
	
	po.handleSubDataExchangeMessage = function(message)
	{
		var subDataExchangeId = message.subDataExchangeId;
		
		if(!subDataExchangeId)
			return;
		
		var type = (message ? message.type : "");
		
		var toCache = true;
		
		if("SubStart" == type)
		{
			toCache = false;
		}
		else if("SubFinish" == type)
		{
			po.subDataExchangeFinishCount += 1;
			po.setDataExchangeProgress(parseInt(po.subDataExchangeFinishCount/po.subDataExchangeCount * 100));
			
			toCache =false;
		}
		else if("SubCancelSuccess" == type)
		{
			po.subDataExchangeFinishCount += 1;
			po.setDataExchangeProgress(parseInt(po.subDataExchangeFinishCount/po.subDataExchangeCount * 100));
			
			toCache =true;
		}
		
		if(toCache)
		{
			var prevMessage = po.subDataExchangeMessageCache[subDataExchangeId];
			
			if(!prevMessage || message.order == null || message.order >= prevMessage.order)
				po.subDataExchangeMessageCache[subDataExchangeId] = message;
		}
		
		var refresh = false;
		
		var time = new Date().getTime();
		if(!po.prevRefreshSubDataExchangeStatusTime)
			po.prevRefreshSubDataExchangeStatusTime = time;
		if((time - po.prevRefreshSubDataExchangeStatusTime) >= 200)
			refresh = true;
		
		if(refresh)
		{
			po.refreshSubDataExchangeStatus();
			po.prevRefreshSubDataExchangeStatusTime = new Date().getTime();
		}
	};
	
	po.refreshSubDataExchangeStatus = function()
	{
		var dataTable = po.getSubDataExchangeDataTable();
		
		var cells = [];
		
		for(var subDataExchangeId in po.subDataExchangeMessageCache)
		{
			var rowIndex = po.getSubDataExchangeRowIndex(dataTable, subDataExchangeId);
			
			if(rowIndex < 0)
				continue;
			
			var message = po.subDataExchangeMessageCache[subDataExchangeId];
			var type = (message ? message.type : "");
			var status = "";
			
			if("SubSubmitSuccess" == type)
			{
				status = "<@spring.message code='dataExchange.exchangeStatus.SubSubmitSuccess' />";
			}
			else if("SubSubmitFail" == type)
			{
				status = "<@spring.message code='dataExchange.exchangeStatus.SubSubmitFail' />";
			}
			else if("SubCancelSuccess" == type)
			{
				status = "<@spring.message code='dataExchange.exchangeStatus.SubCancelSuccess' />";
			}
			else if("SubExchangingWithCount" == type)
			{
				<#assign messageArgs=['"+message.successCount+"', '"+message.failCount+"'] />
				status = "<@spring.messageArgs code='dataExchange.exchangeStatus.SubExchangingWithCount' args=messageArgs />";
			}
			else if("SubExceptionWithCount" == type)
			{
				var exceptionResolve = message.exceptionResolve;
				
				var duration = po.formatDuration(message.duration);
				
				<#assign messageArgs=['"+message.successCount+"', '"+message.failCount+"', '"+duration+"', '"+message.content+"'] />
				
				//未进行任何实际导入操作
				if(message.successCount == 0 && message.failCount == 0)
					status = "<@spring.messageArgs code='dataExchange.exchangeStatus.SubExceptionWithCount' args=messageArgs />";
				else if(exceptionResolve == "ABORT")
					status = "<@spring.messageArgs code='dataExchange.exchangeStatus.SubExceptionWithCount.ABORT' args=messageArgs />";
				else if(exceptionResolve == "IGNORE")
					status = "<@spring.messageArgs code='dataExchange.exchangeStatus.SubExceptionWithCount.IGNORE' args=messageArgs />";
				else if(exceptionResolve == "ROLLBACK")
					status = "<@spring.messageArgs code='dataExchange.exchangeStatus.SubExceptionWithCount.ROLLBACK' args=messageArgs />";
				
				status += "<span class='exchange-result-icon exchange-error-icon ui-state-error' onmouseover='${pageId}.showSubExceptionTip(event, this)'"
							+" onmouseout='${pageId}.hideSubExceptionTip(event, this)' subDataExchangeId='"+$.escapeHtml(message.subDataExchangeId)+"' >"
							+"<span class='ui-icon ui-icon-info'></span></span>";
				
				po.subDataExchangeExceptionMessages[message.subDataExchangeId] = message.content;
			}
			else if("SubSuccessWithCount" == type)
			{
				var duration = po.formatDuration(message.duration);
				
				<#assign messageArgs=['"+message.successCount+"', '"+message.failCount+"', '"+duration+"'] />
				
				if(!message.failCount || message.failCount == 0)
				{
					status = "<@spring.messageArgs code='dataExchange.exchangeStatus.SubSuccessWithCount' args=messageArgs />";
					status += "<span class='exchange-result-icon exchange-success-icon'><span class='ui-icon ui-icon-circle-check'></span></span>";
				}
				else
				{
					status = "<@spring.messageArgs code='dataExchange.exchangeStatus.SubExceptionWithCount.IGNORE' args=messageArgs />";
					status += "<span class='exchange-result-icon exchange-error-icon ui-state-error' onmouseover='${pageId}.showSubExceptionTip(event, this)'"
							+" onmouseout='${pageId}.hideSubExceptionTip(event, this)' subDataExchangeId='"+$.escapeHtml(message.subDataExchangeId)+"' >"
							+"<span class='ui-icon ui-icon-info'></span></span>";
					
					po.subDataExchangeExceptionMessages[message.subDataExchangeId] = message.ignoreException;
				}
			}
			
			status = po.handleSubDataExchangeStatus(message.subDataExchangeId, status, message);
			
			var cellIndex = { "row" : rowIndex, "column" : po.subDataExchangeStatusColumnIndex };
			var cell = dataTable.cell(cellIndex);
			cell.data(status);
			cells.push(cellIndex);
		}
		
		//统一绘制，效率更高
		dataTable.cells(cells).draw();
	};
	
	po.handleSubDataExchangeStatus = function(subDataExchangeId, status, message)
	{
		return status;
	};
	
	po.resetAllSubDataExchangeStatus = function()
	{
		var dataTable = po.getSubDataExchangeDataTable();
		var rowCount = dataTable.rows().indexes().length;
		var cells = [];
		
		for(var i=0; i<rowCount; i++)
		{
			var cellIndex = { "row" : i, "column" : po.subDataExchangeStatusColumnIndex };
			var cell = dataTable.cell(cellIndex);
			cell.data("");
			cells.push(cellIndex);
		}
		
		//统一绘制，效率更高
		dataTable.cells(cells).draw();
	};
	
	po.initDataExchangeUIs = function()
	{
		po.elementOfId("${pageId}-binaryFormat").controlgroup();
		po.element(".binaryFormatSetButton").click(function()
		{
			po.elementOfName("dataFormat.binaryFormat").val($(this).attr("value"));
		});
	};
	
	po.initDataExchangeActions = function()
	{
		<#if isAjaxRequest>
		po.validateAjaxForm({},
		{
			success: function(data)
			{
				po.element().parent().html(data);
			}
		},
		po.elementOfId("${pageId}-returnForm"));
		</#if>
	}
})
(${pageId});
</script>
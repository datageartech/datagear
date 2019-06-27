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
		return po.table().DataTable();
	};

	po.getDataExchangeProgressEle = function()
	{
		return po.element("#${pageId}-progress");
	};

	po.getDataExchangeProgressPercentEle = function()
	{
		return po.element("#${pageId}-progress-percent");
	};
	
	po.getSubDataExchangeExceptionTipEle = function()
	{
		return po.element("#${pageId}-exchange-exception-tooltip");
	};
	
	po.calTableHeight = function()
	{
		var height =  po.element(".form-content-wizard > .content").height() - po.element(".form-item-table-head:not(:hidden)").outerHeight(true) - 60;
		return height;
	};
	
	po.adjustDataTable = function()
	{
		po.table().DataTable().columns.adjust();
		$.updateDataTableHeight(po.table(), po.calTableHeight());
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
				
				if(status == "<@spring.message code='dataExchange.exchangeStatus.SubSubmitSuccess' />")
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
		var rowIndex = -1;
		
		var rowDatas = dataTable.rows().data();
		for(var i=0; i<rowDatas.length; i++)
		{
			if(rowDatas[i].subDataExchangeId == subDataExchangeId)
			{
				rowIndex = i;
				break;
			}
		}
		
		return rowIndex;
	};
	
	po.updateSubDataExchangeStatus = function(subDataExchangeId, status)
	{
		var dataTable = po.getSubDataExchangeDataTable();
		
		var rowIndex = po.getSubDataExchangeRowIndex(dataTable, subDataExchangeId);
		
		if(rowIndex < 0)
			return false;
		
		var cellIndex = { "row" : rowIndex, "column" : po.subDataExchangeStatusColumnIndex };
		var cell = dataTable.cell(cellIndex);
		cell.data(status).draw();
		
		return true;
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
	
	po.viewSubDataExchangeDetailLog = function(subDataExchangeId, subDataExchangeDisplayName)
	{
		<#assign messageArgs=['"+subDataExchangeDisplayName+"'] />
		
		po.open("${contextPath}/dataexchange/" + po.schemaId +"/viewLog",
		{
			title : "<@spring.messageArgs code='dataExchange.viewLog' args=messageArgs />",
			data :
			{
				schemaId : po.schemaId,
				dataExchangeId : po.dataExchangeId,
				subDataExchangeId : subDataExchangeId,
				subDataExchangeDisplayName : subDataExchangeDisplayName
			},
			height : $(window).height() * 0.75
		});
	};
	
	po.handleDataExchangeCometdMessage = function(message)
	{
		message = message.data;
		var type = (message ? message.type : "");
		
		if("Start" == type)
		{
			var dataTable = po.getSubDataExchangeDataTable();
			po.subDataExchangeCount = dataTable.rows().indexes().length;
			po.subDataExchangeFinishCount=0;
			po.subDataExchangeExceptionMessages = {};
		}
		else if("Exception" == type)
		{
			<#assign messageArgs=['"+message.content+"'] />
			$.tipError("<@spring.messageArgs code='dataExchange.exchangeStatus.Exception' args=messageArgs />");
		}
		else if("SubSubmitSuccess" == type)
		{
			po.updateSubDataExchangeStatus(message.subDataExchangeId,
					"<@spring.message code='dataExchange.exchangeStatus.SubSubmitSuccess' />");
		}
		else if("SubSubmitFail" == type)
		{
			po.updateSubDataExchangeStatus(message.subDataExchangeId,
				"<@spring.message code='dataExchange.exchangeStatus.SubSubmitFail' />");
		}
		else if("SubCancelSuccess" == type)
		{
			po.subDataExchangeFinishCount += 1;
			po.setDataExchangeProgress(parseInt(po.subDataExchangeFinishCount/po.subDataExchangeCount * 100));
			
			po.updateSubDataExchangeStatus(message.subDataExchangeId,
				"<@spring.message code='dataExchange.exchangeStatus.SubCancelSuccess' />");
		}
		else if("SubExchangingWithCount" == type)
		{
			var status = "";
			
			<#assign messageArgs=['"+message.successCount+"', '"+message.failCount+"'] />
			status = "<@spring.messageArgs code='dataExchange.exchangeStatus.SubExchangingWithCount' args=messageArgs />";
			
			po.updateSubDataExchangeStatus(message.subDataExchangeId, status);
		}
		else if("SubExceptionWithCount" == type)
		{
			var exceptionResolve = message.exceptionResolve;
			
			var duration = po.formatDuration(message.duration);
			
			var status = "";
			
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
			
			status += "<span class='exchange-result-icon ui-state-error' onmouseover='${pageId}.showSubExceptionTip(event, this)'"
						+" onmouseout='${pageId}.hideSubExceptionTip(event, this)' subDataExchangeId='"+$.escapeHtml(message.subDataExchangeId)+"' >"
						+"<span class='ui-icon ui-icon-info'></span></span>";
			
			po.subDataExchangeExceptionMessages[message.subDataExchangeId] = message.content;
			
			po.updateSubDataExchangeStatus(message.subDataExchangeId, status);
		}
		else if("SubSuccessWithCount" == type)
		{
			var duration = po.formatDuration(message.duration);
			
			var status = "";
			
			<#assign messageArgs=['"+message.successCount+"', '"+message.failCount+"', '"+duration+"'] />
			
			if(!message.failCount || message.failCount == 0)
			{
				status = "<@spring.messageArgs code='dataExchange.exchangeStatus.SubSuccessWithCount' args=messageArgs />";
				status += "<span class='exchange-result-icon'>"
						+"<span class='ui-icon ui-icon-circle-check'></span></span>";
			}
			else
			{
				status = "<@spring.messageArgs code='dataExchange.exchangeStatus.SubExceptionWithCount.IGNORE' args=messageArgs />";
				status += "<span class='exchange-result-icon ui-state-error' onmouseover='${pageId}.showSubExceptionTip(event, this)'"
						+" onmouseout='${pageId}.hideSubExceptionTip(event, this)' subDataExchangeId='"+$.escapeHtml(message.subDataExchangeId)+"' >"
						+"<span class='ui-icon ui-icon-info'></span></span>";
				
				po.subDataExchangeExceptionMessages[message.subDataExchangeId] = message.ignoreException;
			}
			
			po.updateSubDataExchangeStatus(message.subDataExchangeId, status);
		}
		else if("SubFinish" == type)
		{
			po.subDataExchangeFinishCount += 1;
			po.setDataExchangeProgress(parseInt(po.subDataExchangeFinishCount/po.subDataExchangeCount * 100));
		}
		else if("Finish" == type)
		{
			po.setDataExchangeProgress(100, message.duration);
			po.toggleRestartStatus(true);
		}
	};

	po.toggleRestartStatus = function(enable)
	{
		if(enable)
		{
			po.element(".restart-wrapper").show();
			po.element(".restart-button").removeClass("ui-state-disabled");
		}
		else
		{
			po.element(".restart-wrapper").hide();
			po.element(".restart-button").addClass("ui-state-disabled");
		}
	};
})
(${pageId});
</script>
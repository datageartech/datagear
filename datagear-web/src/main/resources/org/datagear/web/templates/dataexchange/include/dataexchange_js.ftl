<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#--
依赖：
page_format_time.ftl
-->
<script>
(function(po)
{
	po.schemaId = "${schema.id}";
	po.dataExchangeId = "${dataExchangeId}";
	po.subDataExchangeStatusUnstart = "<@spring.message code='dataExchange.exchangeStatus.Unstart' />";
	
	po.DataExchangeMessageType =
	{
		Exception: "Exception", //MessageBatchDataExchangeListener.Exception
		Finish: "Finish", //MessageBatchDataExchangeListener.Finish
		Start: "Start", //MessageBatchDataExchangeListener.Start
		Success: "Success", //MessageBatchDataExchangeListener.Success
		SubCancelSuccess: "SubCancelSuccess", //MessageBatchDataExchangeListener.SubCancelSuccess
		SubException: "SubException", //MessageSubDataExchangeListener.SubException
		SubExceptionWithCount: "SubExceptionWithCount", //MessageSubDataExchangeListener.SubExceptionWithCount
		SubExchangingWithCount: "SubExchangingWithCount", //MessageSubDataExchangeListener.SubExchangingWithCount
		SubFinish: "SubFinish", //MessageSubDataExchangeListener.SubFinish
		SubStart: "SubStart", //MessageSubDataExchangeListener.SubStart
		SubSubmitFail: "SubSubmitFail", //org.datagear.web.dataexchange.MessageBatchDataExchangeListener.SubSubmitFail
		SubSubmitSuccess: "SubSubmitSuccess", //org.datagear.web.dataexchange.MessageBatchDataExchangeListener.SubSubmitSuccess
		SubSuccess: "SubSuccess", //MessageSubDataExchangeListener.SubSuccess
		SubSuccessWithCount: "SubSuccessWithCount" //MessageSubDataExchangeListener.SubSuccessWithCount
	};
	
	po.DataExchangeStatusEnum =
	{
		edit: "edit",
		exchange: "exchange",
		finish: "finish"
	};
	
	po.beforeSubmitForm = function(action)
	{
		if(!po.isLastStep())
		{
			po.toNextStep();
			return false;
		}
		
		if(po.dataExchangeTaskClient.isActive())
			return false;
		
		return po.checkSubmitForm(action);
	};
	
	po.checkSubmitForm = function(action){ return true; };
	
	po.setupDataExchangeForm = function(formModel, ajaxOptions, validateOptions)
	{
		po.inflateFormModel(formModel);
		ajaxOptions = $.extend(
		{
			tipSuccess: false,
			beforeSend: function()
			{
				po.dataExchangeStatus(po.DataExchangeStatusEnum.exchange);
				po.resetDataExchangeProgress();
				po.resetAllSubDataExchangeStatus();
				po.dataExchangeTaskClient.start();
			},
			error: function()
			{
				po.dataExchangeStatus(po.DataExchangeStatusEnum.edit);
				po.dataExchangeTaskClient.stop();
			}
		},
		ajaxOptions);
		
		po.setupForm(formModel, ajaxOptions, validateOptions);
	};
	
	po.inflateFormModel = function(formModel)
	{
		formModel.subDataExchanges = (formModel.subDataExchanges || []);
	};
	
	po.nextSubDataExchangeId = function()
	{
		if(!po._nextSubDataExchangeIdSeq)
			po._nextSubDataExchangeIdSeq = 0;
		
		return po.dataExchangeId + "_" + (po._nextSubDataExchangeIdSeq++);
	};
	
	po.currentSubDataExchangeId = function()
	{
		if(!po._nextSubDataExchangeIdSeq)
			po._nextSubDataExchangeIdSeq = 0;
		
		return po._nextSubDataExchangeIdSeq;
	};
	
	po.nextSubDataExchangeNumber = function()
	{
		var fm = po.vueFormModel();
		
		if(!po._nextSubDataExchangeNumber || po._nextSubDataExchangeNumber < 1
				|| fm.subDataExchanges.length == 0)
		{
			po._nextSubDataExchangeNumber = 1;
		}
		
		var re = po._nextSubDataExchangeNumber;
		po._nextSubDataExchangeNumber++;
		
		return re;
	};
	
	po.addSubDataExchange = function(subDataExchange)
	{
		var fm = po.vueFormModel();
		fm.subDataExchanges.push(subDataExchange);
	};
	
	po.deleteSelSubDataExchanges = function()
	{
		var fm = po.vueFormModel();
		var pm = po.vuePageModel();
		var ss = $.wrapAsArray(po.vueRaw(pm.selectedSubDataExchanges));
		
		$.each(ss, function(idx, s)
		{
			$.removeById(fm.subDataExchanges, s.id);
		});
		
		pm.selectedSubDataExchanges = [];
	}
	
	po.handleDataExchangeMessage = function(message)
	{
		var isFinish = false;
		var type = (message ? message.type : "");
		
		if(po.DataExchangeMessageType.Start == type)
		{
			var fm = po.vueFormModel();
			po.subDataExchangeCount = fm.subDataExchanges.length;
			po.subDataExchangeFinishCount=0;
			po.subDataExchangeExceptionMessages = {};
			po.subDataExchangeIdRowIndexMap = {};
			po.subDataExchangeMessageCache = {};
			
			po.dataExchangeStatus(po.DataExchangeStatusEnum.exchange);
		}
		else if(po.DataExchangeMessageType.Exception == type)
		{
			$.tipError($.validator.format("<@spring.message code='dataExchange.exchangeStatus.Exception' />", message.content));
		}
		else if(po.DataExchangeMessageType.Finish == type)
		{
			isFinish = true;
			po.refreshSubDataExchangeStatus();
			po.setDataExchangeProgress(100, message.duration);
			po.dataExchangeStatus(po.DataExchangeStatusEnum.finish);
		}
		else
			po.handleSubDataExchangeMessage(message);
		
		return isFinish;
	};
	
	//获取/设置状态，参考：po.DataExchangeStatusEnum
	po.dataExchangeStatus = function(status)
	{
		var pm = po.vuePageModel();
		
		if(status == null)
			return pm.dataExchangeStatus;
		
		if(pm.dataExchangeStatus != status)
		{
			var oldStatus = pm.dataExchangeStatus;
			pm.dataExchangeStatus = status;
			
			po.dataExchangeStatusChanged(status, oldStatus);
		}
	};
	
	po.dataExchangeStatusChanged = function(status, oldStatus){};

	po.setDataExchangeProgress = function(value, duration)
	{
		var pm = po.vuePageModel();
		var progress = pm.dataExchangeProgress;
		
		var label = value + "%";
		
		if(duration != null)
		{
			var duration = po.formatDuration(duration);
			label = $.validator.format("<@spring.message code='dataExchange.exchangeProgressPercentWithDuration' />",
						value, duration);
		}
		
		progress.value = value;
		progress.label = label;
	};
	
	po.resetDataExchangeProgress = function()
	{
		var pm = po.vuePageModel();
		var progress = pm.dataExchangeProgress;
		
		progress.value = 0;
		progress.label = "0%";
	};
	
	po.handleSubDataExchangeMessage = function(message)
	{
		var subDataExchangeId = message.subDataExchangeId;
		
		if(!subDataExchangeId)
			return;
		
		var type = (message ? message.type : "");
		
		var toCache = true;
		
		if(po.DataExchangeMessageType.SubStart == type)
		{
			toCache = false;
		}
		else if(po.DataExchangeMessageType.SubFinish == type)
		{
			po.subDataExchangeFinishCount += 1;
			po.setDataExchangeProgress(parseInt(po.subDataExchangeFinishCount/po.subDataExchangeCount * 100));
			
			toCache =false;
		}
		else if(po.DataExchangeMessageType.SubCancelSuccess == type)
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
			po.prevRefreshSubDataExchangeStatusTime = time;
		}
	};
	
	po.refreshSubDataExchangeStatus = function()
	{
		var fm = po.vueFormModel();
		var subDataExchanges = fm.subDataExchanges;
		
		for(var subDataExchangeId in po.subDataExchangeMessageCache)
		{
			var rowIndex = $.inArrayById(subDataExchanges, subDataExchangeId);
			
			if(rowIndex < 0)
				continue;
			
			var subDataExchange = subDataExchanges[rowIndex];
			var message = po.subDataExchangeMessageCache[subDataExchangeId];
			var type = (message ? message.type : "");
			var status = "";
			
			if(po.DataExchangeMessageType.SubSubmitSuccess == type)
			{
				status = "<@spring.message code='dataExchange.exchangeStatus.SubSubmitSuccess' />";
			}
			else if(po.DataExchangeMessageType.SubSubmitFail == type)
			{
				status = "<@spring.message code='dataExchange.exchangeStatus.SubSubmitFail' />";
			}
			else if(po.DataExchangeMessageType.SubCancelSuccess == type)
			{
				status = "<@spring.message code='dataExchange.exchangeStatus.SubCancelSuccess' />";
			}
			else if(po.DataExchangeMessageType.SubExchangingWithCount == type)
			{
				status = $.validator.format("<@spring.message code='dataExchange.exchangeStatus.SubExchangingWithCount' />",
						message.successCount, message.failCount);
			}
			else if(po.DataExchangeMessageType.SubExceptionWithCount == type)
			{
				var exceptionResolve = message.exceptionResolve;
				var duration = po.formatDuration(message.duration);
				
				status = "<div class='flex align-items-center'>"
							+"<button type='button' class='log-detail-btn p-button p-component p-button-danger mr-1'"
							+" subDataExchangeId=\""+$.escapeHtml(message.subDataExchangeId)+"\""
							+" title=\""+$.escapeHtml(message.content+"\n"+"<@spring.message code='clickForDetail' />")+"\""
							+">"
							+"<i class='pi pi-info-circle text-sm'></i>"
							+"</button>";
				
				var statusTmp = "";
				//未进行任何实际导入操作
				if(message.successCount == 0 && message.failCount == 0)
					statusTmp = "<@spring.message code='dataExchange.exchangeStatus.SubExceptionWithCount' />";
				else if(exceptionResolve == "ABORT")
					statusTmp = "<@spring.message code='dataExchange.exchangeStatus.SubExceptionWithCount.ABORT' />";
				else if(exceptionResolve == "IGNORE")
					statusTmp = "<@spring.message code='dataExchange.exchangeStatus.SubExceptionWithCount.IGNORE' />";
				else if(exceptionResolve == "ROLLBACK")
					statusTmp = "<@spring.message code='dataExchange.exchangeStatus.SubExceptionWithCount.ROLLBACK' />";
				
				status += "<div title=\""+$.escapeHtml("<@spring.message code='dataExchange.exchangeStatus.desc' />")+"\">"
								+$.validator.format(statusTmp, message.successCount, message.failCount, duration, message.content);
							+"</div>";
				status += "</div>";
			}
			else if(po.DataExchangeMessageType.SubSuccessWithCount == type)
			{
				var duration = po.formatDuration(message.duration);
				
				if(message.failCount == null || message.failCount == 0)
				{
					status = "<span class='mr-1'>"
								+"<i class='pi pi-check-circle text-sm'></i>"
								+"</span>";
					
					status += "<span title=\""+$.escapeHtml("<@spring.message code='dataExchange.exchangeStatus.desc' />")+"\">"
								+ $.validator.format("<@spring.message code='dataExchange.exchangeStatus.SubSuccessWithCount' />",
											message.successCount, message.failCount, duration)
								+"</span>";
				}
				else
				{
					status = "<div class='flex align-items-center'>"
						+"<button type='button' class='log-detail-btn p-button p-component p-button-danger mr-1'"
						+" subDataExchangeId=\""+$.escapeHtml(message.subDataExchangeId)+"\""
						+" title=\""+$.escapeHtml((message.ignoreException ? message.ignoreException+"\n" : "")+"<@spring.message code='clickForDetail' />")+"\""
						+">"
						+"<i class='pi pi-info-circle text-sm'></i>"
						+"</button>";
					
					status += "<div title=\""+$.escapeHtml("<@spring.message code='dataExchange.exchangeStatus.desc' />")+"\">"
								+ $.validator.format("<@spring.message code='dataExchange.exchangeStatus.SubExceptionWithCount.IGNORE' />",
											message.successCount, message.failCount, duration)
							 	+"</div>";
					status += "</div>";
				}
			}
			
			status = po.handleSubDataExchangeStatus(message.subDataExchangeId, status, message);
			subDataExchange.status = status;
		}
	};
	
	po.handleSubDataExchangeStatus = function(subDataExchangeId, status, message)
	{
		return status;
	};
	
	po.viewSubDataExchangeDetailLog = function(subDataExchangeId)
	{
		po.open("/dataexchange/" + encodeURIComponent(po.schemaId) +"/viewLog",
		{
			data :
			{
				dataExchangeId : po.dataExchangeId,
				subDataExchangeId : subDataExchangeId
			}
		});
	};
	
	po.resetAllSubDataExchangeStatus = function()
	{
		var fm = po.vueFormModel();
		var subDataExchanges = fm.subDataExchanges;
		
		$.each(subDataExchanges, function(i, sde)
		{
			sde.status = po.subDataExchangeStatusUnstart;
		});
	};
	
	po.setupDataExchange = function()
	{
		po.vuePageModel(
		{
			selectedSubDataExchanges: [],
			DataExchangeStatusEnum: po.DataExchangeStatusEnum,
			dataExchangeStatus: po.DataExchangeStatusEnum.edit,
			dataExchangeProgress:
			{
				value: 0,
				label: "0%"
			}
		});
		
		po.vueMethod(
		{
			onDeleteSelSubDataExchanges: function()
			{
				po.deleteSelSubDataExchanges();
			}
		});
		
		po.vueMounted(function()
		{
			po.element(".subdataexchange-table-wrapper").on("click", ".log-detail-btn", function(e)
			{
				var subDataExchangeId = $(this).attr("subDataExchangeId");
				po.viewSubDataExchangeDetailLog(subDataExchangeId);
			});
		});
		
		po.dataExchangeTaskClient = new $.TaskClient(
				po.concatContextPath("/dataexchange/"+encodeURIComponent(po.schemaId)+"/message"),
				function(message)
				{
					return po.handleDataExchangeMessage(message);
				},
				{
					data: { dataExchangeId: po.dataExchangeId }
				}
		);
	};
})
(${pid});
</script>
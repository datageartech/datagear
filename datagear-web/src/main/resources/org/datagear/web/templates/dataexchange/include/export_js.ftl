<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#--
导出JS片段

依赖：
dataexchange_js.ftl
-->
<script>
(function(po)
{
	po.checkSubmitForm = function(action)
	{
		return po.checkSubmitSubDataExchanges(action);
	};
	
	po.checkSubmitSubDataExchanges = function(action)
	{
		var data = action.options.data;
		var subDataExchanges = data.subDataExchanges;
		
		if(!subDataExchanges || subDataExchanges.length == 0)
		{
			$.tipInfo("<@spring.message code='dataExport.queryRequired' />");
			return false;
		}
		
		for(var i=0; i<subDataExchanges.length; i++)
		{
			if(po.checkSubmitSubDataExchange(subDataExchanges[i], i, action) === false)
				return false;
		}
		
		return true;
	};
	
	po.checkSubmitSubDataExchange = function(subDataExchange, index, action)
	{
		return true;
	};
	
	po.checkSubmitSubDataExchangeQuery = function(subDataExchange, index)
	{
		if(!subDataExchange.query)
		{
			var msg = $.validator.format("<@spring.message code='dataExport.queryRequiredAtRow' />", index + 1);
			$.tipInfo(msg);
			
			return false;
		}
		
		return true;
	};

	po.checkSubmitSubDataExchangeFileName = function(subDataExchange, index)
	{
		if(!subDataExchange.fileName)
		{
			var msg = $.validator.format("<@spring.message code='dataExport.fileNameRequiredAtRow' />", index + 1);
			$.tipInfo(msg);
			
			return false;
		}
		
		return true;
	};
	
	po.checkSubmitSubDataExchangeTableName = function(subDataExchange, index)
	{
		if(!subDataExchange.tableName)
		{
			var msg = $.validator.format("<@spring.message code='dataExport.tableNameRequiredAtRow' />", index + 1);
			$.tipInfo(msg);
			
			return false;
		}
		
		return true;
	};

	po.addSubDataExchangesForQueries = function(queries)
	{
		if(!queries || !queries.length)
			return;
		
		$.each(queries, function(i, query)
		{
			var sde =
			{
				id: po.nextSubDataExchangeId(),
				query: query,
				fileName : po.toExportFileName(query),
				status: po.subDataExchangeStatusUnstart
			};
			
			po.postBuildSubDataExchange(sde);
			po.addSubDataExchange(sde);
		});
		
		po.vueApp().$nextTick(function()
		{
			//滚动到底部
			var tableWrapper = po.element(".p-datatable-wrapper", po.element(".subdataexchange-table-wrapper"));
			tableWrapper.scrollTop(tableWrapper.prop("scrollHeight"));
		});
	};
	
	po.postBuildSubDataExchange = function(subDataExchange){};

	po.toExportFileName = function(query)
	{
		if(!query)
			return "";
		
		var tableName = po.resolveTableName(query);
		if(!tableName)
			tableName = po.nextSubDataExchangeNumber();
		
		return po.handleExportFileNameExtension($.toValidFileName(tableName));
	};
	
	po.handleExportFileNameExtension = function(fileName)
	{
		return fileName;
	};
	
	po.resolveTableName = function(query)
	{
		if(!query)
			return "";
		
		//表名称
		if(!/\s/.test(query))
			return query;
		
		//第一个表名正则
		var result = query.match(/from\s([^\,\s]*)/i);
		
		if(result == null || result.length < 2)
			return "";
		
		return result[1];
	};

	po.handleSubDataExchangeStatus = function(subDataExchangeId, status, message)
	{
		var type = (message ? message.type : "");
		
		if(po.DataExchangeMessageType.SubSuccessWithCount == type)
		{
			if(message.failCount == null || message.failCount == 0)
			{
				var duration = po.formatDuration(message.duration);
				
				status = "<div class='flex align-items-center'>"
					+"<button type='button' class='download-file-btn p-button p-component p-button-success mr-1'"
					+" subDataExchangeId=\""+$.escapeHtml(message.subDataExchangeId)+"\""
					+" title=\""+$.escapeHtml("<@spring.message code='download' />")+"\""
					+">"
					+"<i class='pi pi-download text-sm'></i>"
					+"</button>";
				
				status += "<div title=\""+$.escapeHtml("<@spring.message code='dataExchange.exchangeStatus.desc' />")+"\">"
							+ $.validator.format("<@spring.message code='dataExchange.exchangeStatus.SubSuccessWithCount' />",
								message.successCount, message.failCount, duration)
							+"</div>";
				status += "</div>";
			}
		}
		
		return status;
	};
	
	po.setupExport = function()
	{
		po.vueMounted(function()
		{
			po.element(".subdataexchange-table-wrapper").on("click", ".download-file-btn", function(e)
			{
				var subDataExchangeId = $(this).attr("subDataExchangeId");
				var subDataExchange = po.getSubDataExchangeById(subDataExchangeId);
				if(subDataExchange)
				{
					var url = "/dataexchange/" + encodeURIComponent(po.schemaId) +"/export/download";
					url = $.addParam(url, "dataExchangeId", encodeURIComponent(po.dataExchangeId));
					url = $.addParam(url, "fileName", encodeURIComponent(subDataExchange.fileName));
					
					po.open(url, { target : "_blank" });
				}
			});
		});
	};
})
(${pid});
</script>
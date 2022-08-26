<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#--
导入JS片段

依赖：
dataexchange_js.ftl
-->
<script>
(function(po)
{
	//org.datagear.dataexchange.ExceptionResolve
	po.ExceptionResolve =
	{
		ABORT: "ABORT",
		IGNORE: "IGNORE",
		ROLLBACK: "ROLLBACK"
	};
	
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
			$.tipInfo("<@spring.message code='dataImport.importFileRequired' />");
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
	
	po.checkSubmitSubDataExchangeTableName = function(subDataExchange)
	{
		if(!subDataExchange.tableName)
		{
			var msg = $.validator.format("<@spring.message code='dataImport.tableNameRequiredAtNumber' />",
							subDataExchange.number);
			$.tipInfo(msg);
			
			return false;
		}
		
		return true;
	};
	
	po.addSubDataExchangesForFileInfos = function(fileInfos)
	{
		if(!fileInfos || !fileInfos.length)
			return;
		
		$.each(fileInfos, function(i, fileInfo)
		{
			var sde =
			{
				id: po.nextSubDataExchangeId(),
				number: po.nextSubDataExchangeNumber(),
				fileName: fileInfo.name,
				fileSize: fileInfo.size,
				fileDisplayName: fileInfo.displayName,
				tableName: fileInfo.tableName,
				dependentNumber: "",
				status: po.subDataExchangeStatusUnstart
			};
			
			po.postBuildSubDataExchange(sde);
			po.addSubDataExchange(sde);
		});
	};
	
	po.postBuildSubDataExchange = function(subDataExchange){};

	po.vuePageModel(
	{
		exceptionResolveOptions:
		[
			{
				name: "<@spring.message code='dataExchange.exceptionResolve.ROLLBACK' />",
				value: po.ExceptionResolve.ROLLBACK
			},
			{
				name: "<@spring.message code='dataExchange.exceptionResolve.ABORT' />",
				value: po.ExceptionResolve.ABORT
			},
			{
				name: "<@spring.message code='dataExchange.exceptionResolve.IGNORE' />",
				value: po.ExceptionResolve.IGNORE
			}
		]
	});
})
(${pid});
</script>
<#--
 *
 * Copyright 2018-2024 datagear.tech
 *
 * This file is part of DataGear.
 *
 * DataGear is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * DataGear is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with DataGear.
 * If not, see <https://www.gnu.org/licenses/>.
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
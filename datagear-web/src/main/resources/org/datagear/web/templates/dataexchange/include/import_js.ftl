<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<script>
(function(po)
{
	po.stepsItems =
	[
		{ label: "<@spring.message code='set' />" },
		{ label: "<@spring.message code='import' />" }
	];
	
	po.addSubDataExchangesForFileInfos = function(fileInfos)
	{
		if(!fileInfos.length)
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
	
})
(${pid});
</script>
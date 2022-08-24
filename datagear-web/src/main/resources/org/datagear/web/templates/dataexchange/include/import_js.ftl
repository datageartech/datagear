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
			fileInfo.subDataExchangeId = po.nextSubDataExchangeId();
			fileInfo.number = po.nextSubDataExchangeNumber();
			fileInfo.dependentNumber = "";
			
			po.postBuildSubDataExchange(fileInfo);
			po.addSubDataExchange(fileInfo);
		});
	};
	
	po.postBuildSubDataExchange = function(subDataExchange){};
	
})
(${pid});
</script>
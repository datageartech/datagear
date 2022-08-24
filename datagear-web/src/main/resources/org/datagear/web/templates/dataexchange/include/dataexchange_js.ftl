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
	po.schemaId = "${schema.id}";
	po.dataExchangeId = "${dataExchangeId}";
	
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
	
	po.dataExchangeTaskClient = new $.TaskClient("${contextPath}/dataexchange/"+po.schemaId+"/message",
		function(message)
		{
			return po.handleDataExchangeMessage(message);
		},
		{
			data: { dataExchangeId: po.dataExchangeId }
		}
	);
	
	po.addSubDataExchange = function(subDataExchange)
	{
		var fm = po.vueFormModel();
		fm.subDataExchanges.push(subDataExchange);
	};
	
	po.deleteSelSubDataExchanges = function()
	{
		var fm = po.vueFormModel();
		var ss = $.wrapAsArray(po.vueRaw(pm.selectedSubDataExchanges));
		
		$.each(ss, function(idx, s)
		{
			$.removeById(fm.subDataExchanges, s.id);
		});
		
		pm.selectedSubDataExchanges = [];
	}
	
	po.vuePageModel(
	{
		selectedSubDataExchanges: []
	});
	
	po.vueMethod(
	{
		onDeleteSelSubDataExchanges: function()
		{
			po.deleteSelSubDataExchanges();
		}
	});
})
(${pid});
</script>
<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#include "../../include/page_obj.ftl">
<script type="text/javascript">
(function(po)
{
	//当前模式ID
	po.schemaId = "${schema.id}";
	
	//当前模型名称
	po.tableName = "${tableName}";
	
	po.dataUrl = function(action)
	{
		return po.concatContextPath("/data/"+po.schemaId+"/"+encodeURIComponent(po.tableName)+"/"+action);
	};
	
	po.onDbTable = function(callback, reload)
	{
		if(reload)
			$.tableMeta.load(this.schemaId, this.tableName, callback);
		else
			$.tableMeta.on(this.schemaId, this.tableName, callback);
	};
	
	/**
	 * 提交可处理重复记录的请求。
	 */
	po.ajaxSubmitForHandleDuplication = function(url, data, messageTemplate, ajaxOptions, ignoreDuplication)
	{
		var errorCallback = function(jqXHR, textStatus, errorThrown)
		{
			var callResult = undefined;
			
			if(ajaxOptions.error)
				callResult = ajaxOptions.error.call(this, jqXHR, textStatus, errorThrown);

			if(!ignoreDuplication)
			{
				var operationMessage = $.getResponseJson(jqXHR);
				
				if(operationMessage.code == "error.DuplicateRecordException")
				{
					var expected = (operationMessage.data && operationMessage.data.length > 0 ? operationMessage.data[0] : "???");
					var actual = (operationMessage.data && operationMessage.data.length > 0 ? operationMessage.data[1] : "???");
					
					var message = messageTemplate.replace( /#\{expected\}/g, "" + expected).replace( /#\{actual\}/g, "" + actual);
					
					po.confirm(message,
					{
						"confirm" : function()
						{
							$.closeTip();
							
							po.ajaxSubmitForHandleDuplication(url, data, messageTemplate, ajaxOptions, true);
						},
						"cancel" : function()
						{
							$.closeTip();
						}
					});
				}
			}
			
			return callResult;
		};
		
		if(ignoreDuplication)
			url = $.addParam(url, "ignoreDuplication", "true");
		
		var options = $.extend({}, ajaxOptions, { data : data, error : errorCallback, type : "POST" });
		
		$.ajaxJson(url, options);
	};
})
(${pageId});
</script>

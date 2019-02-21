<#include "../../include/page_js_obj.ftl">
<script type="text/javascript">
(function(po)
{
	var dpo =
	{
		//当前模式ID
		schemaId : "${schema.id}",
		
		//当前模型名称
		tableName : "${tableName}",

		/**
		 * 生成指定操作的URL。
		 *
		 * @param tableName 可选，操作对应的表名，默认是当前表名
		 * @param action 操作名称
		 * @param param URL后面加的参数，不需要以'?'开头
		 */
		url : function(tableName, action, param)
		{
			if(action == undefined)
			{
				action = tableName;
				tableName = this.tableName;
			}
			
			if(!tableName)
				tableName = this.tableName;
			
			return contextPath + $.toPath("data", this.schemaId, tableName, action) + (param ? "?" + param : "");
		},
		
		/**
		 * 对指定表名的模型执行操作。
		 *
		 * @param tableName 可选，操作对应的表名，默认是当前表名
		 * @param callback 操作函数，格式为：function(model){ ... }
		 */
		onModel : function(tableName, callback)
		{
			if(!callback)
			{
				callback = tableName;
				tableName = this.tableName;
			}
			
			$.model.on(this.schemaId, tableName, callback);
		},
		
		/**
		 * 提交可处理重复记录的请求。
		 */
		ajaxSubmitForHandleDuplication : function(action, data, messageTemplate, ajaxOptions, ignoreDuplication)
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
								
								po.ajaxSubmitForHandleDuplication(action, data, messageTemplate, ajaxOptions, true);
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
			
			var url;
			
			if(ignoreDuplication)
				url = po.url("", action, "ignoreDuplication=true");
			else
				url = po.url(action);
			
			var options = $.extend({}, ajaxOptions, { data : data, error : errorCallback, type : "POST" });
			
			$.ajax(url, options);
		}
	};
	
	$.extend(po, dpo);
})
(${pageId});
</script>

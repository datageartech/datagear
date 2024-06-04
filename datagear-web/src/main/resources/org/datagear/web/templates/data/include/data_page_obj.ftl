<#--
 *
 * Copyright 2018-present datagear.tech
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
<#assign Schema=statics['org.datagear.management.domain.DtbsSource']>
<#include "../../include/page_obj.ftl">
<script type="text/javascript">
(function(po)
{
	po.schemaId = "${schema.id}";
	po.schemaPermission = parseInt("${schema.dataPermission}");
	po.tableName = "${tableName}";
	
	po.canReadTableData = (!isNaN(po.schemaPermission) && po.schemaPermission >= parseInt("${Schema.PERMISSION_TABLE_DATA_READ}"));
	po.canEditTableData = (!isNaN(po.schemaPermission) && po.schemaPermission >= parseInt("${Schema.PERMISSION_TABLE_DATA_EDIT}"));
	po.canDeleteTableData = (!isNaN(po.schemaPermission) && po.schemaPermission >= parseInt("${Schema.PERMISSION_TABLE_DATA_DELETE}"));
	
	po.dataUrl = function(action)
	{
		return "/data/"+encodeURIComponent(po.schemaId)+"/"+encodeURIComponent(po.tableName)+"/"+action;
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
		
		po.ajaxJson(url, options);
	};
	
	po.setupTableDataPermission = function()
	{
		po.vuePageModel(
		{
			canReadTableData: po.canReadTableData,
			canEditTableData: po.canEditTableData,
			canDeleteTableData: po.canDeleteTableData
		});
	};
})
(${pid});
</script>

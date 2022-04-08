<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#--
表格JS片段。

依赖：
page_obj.ftl
-->
<script type="text/javascript">
(function(po)
{
	po.elementTable = function(){ return this.element("#${pageId}-table"); };
	
	po.initGridBtns = function(parent)
	{
		parent = (parent == null ? po.element(".operation") : parent);
		$.initButtons(parent);
	};
	
	//计算表格高度
	po.calTableHeight = function()
	{
		var height =  po.element("> .content").height() - 50;
		return height;
	};
	
	po.renderCheckColumn = function(data, type, row, meta)
	{
		return $.dataTableUtil.renderCheckColumn(data, type, row, meta);
	};
	
	/**
	 * 默认po.buildDataTableSettingsAjax请求参数实现。
	 */
	po.dataTableAjaxParam = function()
	{
		var param = {};
		
		if(po.searchParam)
			$.extend(param, po.searchParam);
		else if(po.getSearchParam)
			$.extend(param, po.getSearchParam());
		
		if(po.pagingParam)
			$.extend(param, po.pagingParam);
		else if(po.getPagingParam)
			$.extend(param, po.getPagingParam());
		
		return param;
	};
	
	/**
	 * 默认po.buildDataTableSettingsAjax请求成功回调实现。
	 */
	po.dataTableAjaxSuccess = function(pagingData, textStatus, jqXHR)
	{
		if(po.refreshPagination)
			po.refreshPagination(pagingData.total, pagingData.page, pagingData.pageSize);
		
		po.pageParamCall("dataTableAjaxSuccess", pagingData, textStatus, jqXHR);
	};
	
	/**
	 * 集成page_obj_searchform.ftl的默认实现。
	 */
	po.search = function(searchParam)
	{
		po.searchParam = searchParam;
		
		//重置页码
		if(po.pagingParam)
			po.pagingParam.page = 1;
		
		po.refresh();
	};
	
	/**
	 * 集成page_obj_pagination.ftl的默认实现。
	 */
	po.paging = function(pagingParam)
	{
		po.pagingParam = pagingParam;
		po.refresh();
		
		return false;
	};

	po.confirmDeleteEntities = function(url, rows, idPropertyName)
	{
		po.confirm("<@spring.message code='confirmDelete' />",
		{
			"confirm" : function()
			{
				$.postJson(url, $.propertyValue(rows, (idPropertyName || "id")), function()
				{
					po.refresh();
				});
			}
		});
	};
	
	po.getOrdersOnName = function($table)
	{
		var dataTable = ($table || po.elementTable()).DataTable();
		return $.dataTableUtil.getOrdersOnName(dataTable);
	};
	
	/**
	 * 构建ajax数据表格选项。
	 * 此ajax选项支持两个回调函数：
	 *   po.dataTableAjaxParam() 用于扩展ajax请求参数；
	 *   po.dataTableAjaxSuccess(pagingData, textStatus, jqXHR) ajax成功回调函数；
	 * @param columns 必选，列元数据
	 * @param url 必选，ajax请求URL
	 * @param ajaxSuccessCallback 可选，ajax成功回调函数，function(pagingData, textStatus, jqXHR){}
	 * @param settings 可选，其他选项
	 */
	po.buildDataTableSettingsAjax = function(columns, url, settings)
	{
		settings = $.extend(
		{
			"serverSide": true,
			"columns" : columns,
			"ajax" : function(data, callback, settings)
			{
				var nameOrder = [];
				
				for(var i=0; i<data.order.length; i++)
				{
					var name = $.getDataTableColumnName(settings, data.order[i].column);
					nameOrder[i] = { "name" : name, "type" : data.order[i].dir };
				}
				
				var myData = po.dataTableAjaxParam();
				
				var param = $.extend({ "orders" : nameOrder }, myData);
				
				$.ajaxJson(
				{
					url : url,
					dataType : "json",
					type : "POST",
					data : param,
					success : function(data, textStatus, jqXHR)
					{
						var isPagingData = (data.page != undefined && data.pageSize != undefined);
						
						if(isPagingData)
						{
							data.data = data.items;
							callback(data);
						}
						else
						{
							var tableData = { "data" : data };
							callback(tableData);
						}
						
						if(po.dataTableAjaxSuccess)
							po.dataTableAjaxSuccess(data, textStatus, jqXHR);
					}
				});
			}
		},
		settings);
		
		return po.buildDataTableSettings(settings);
	};
	
	/**
	 * 构建本地数据表格选项。
	 * @param columns 必选，列元数据
	 * @param data 可选，初始数据
	 * @param settings 可选，其他选项
	 */
	po.buildDataTableSettingsLocal = function(columns, data, settings)
	{
		settings = $.extend(
		{
			"columns" : columns,
			"data" : (data ? data : [])
		}, 
		settings);
		
		return po.buildDataTableSettings(settings);
	};
	
	/**
	 * 构建表格选项。
	 * @param settings 必选，选项
	 */
	po.buildDataTableSettings = function(settings)
	{
		var newColumns = [ $.dataTableUtil.buildCheckCloumn("<@spring.message code='select' />") ];
		newColumns = newColumns.concat(settings.columns);
		
		var orderColumn = -1;
		
		for(var i=1; i < newColumns.length; i++)
		{
			var column = newColumns[i];
			
			if(column.visible == false || column.orderable == false)
				continue;
			else
			{
				orderColumn = i;
				break;
			}
		}
		
		settings = $.extend(
		{
			"scrollX": true,
			"autoWidth": true,
			"scrollY" : po.calTableHeight(),
	        "scrollCollapse": false,
			"paging" : false,
			"searching" : false,
			"select" : { style : 'os' },
			"order": (orderColumn > -1 ? [[orderColumn, "asc"]] : []),
			"fixedColumns": { left: 1 },
		    "language":
		    {
				"emptyTable": "<@spring.message code='dataTables.noData' />",
				"zeroRecords" : "<@spring.message code='dataTables.zeroRecords' />"
			}
		},
		settings);
		
		settings.columns = newColumns;
		
		return settings;
	};
	
	po.initDataTable = function(tableSettings, $table)
	{
		if($table == undefined)
			$table = po.elementTable();
		
		$table.dataTable(tableSettings);
		$.dataTableUtil.bindCheckColumnEvent($table.DataTable());
	};
	
	po.refresh = function()
	{
		po.elementTable().DataTable().draw();
	};
	
	po.setTableData = function(data, dataTable)
	{
		dataTable = (dataTable || po.elementTable().DataTable());
		$.setDataTableData(dataTable, data);
	};
	
	//单选处理函数
	po.executeOnSelect = function(callback)
	{
		$.dataTableUtil.executeOnSelect(po.elementTable().DataTable(), "<@spring.message code='pleaseSelectOnlyOneRow' />",
		function(row, rowIndex){ callback.call(po, row, rowIndex); });
	};
	
	//多选处理函数
	po.executeOnSelects = function(callback)
	{
		$.dataTableUtil.executeOnSelects(po.elementTable().DataTable(), "<@spring.message code='pleaseSelectAtLeastOneRow' />",
		function(rows, rowIndexes){ callback.call(po, rows, rowIndexes); });
	};
	
	//获取选中数据
	po.getSelectedData = function()
	{
		$.dataTableUtil.getSelectedData(po.elementTable().DataTable());
	};
	
	po.getRowsData = function(rows)
	{
		return $.dataTableUtil.getRowsData(po.elementTable().DataTable());
	};
	
	po.getRowsIndex = function(rows)
	{
		return $.dataTableUtil.getRowsIndex(po.elementTable().DataTable());
	};
	
	po.addRowData = function(data)
	{
		$.dataTableUtil.addRowData(po.elementTable().DataTable(), data);
	};
	
	po.setRowData = function(rowIndex, data)
	{
		$.dataTableUtil.setRowData(po.elementTable().DataTable(), rowIndex, data);
	};
	
	po.deleteRow = function(rowIndex)
	{
		$.dataTableUtil.deleteRow(po.elementTable().DataTable(), rowIndex);
	};
	
	po.deleteAllRow = function()
	{
		$.dataTableUtil.deleteAllRow(po.elementTable().DataTable());
	};
	
	po.deleteSelectedRows = function()
	{
		$.dataTableUtil.deleteSelectedRows(po.elementTable().DataTable());
	};
	
	//获取表格元素的父元素
	po.dataTableParent = function(dataTable)
	{
		return $.dataTableUtil.dataTableParent(dataTable || po.elementTable().DataTable());
	};
	
	po.expectedResizeDataTableElements = [po.elementTable()[0]];
	
	po.calChangedDataTableHeight = function()
	{
		var changedTableHeight = po.calTableHeight();
		
		if(changedTableHeight == po.prevTableHeight)
		{
			po.prevTableHeight = changedTableHeight;
			changedTableHeight = null;
		}
		else
			po.prevTableHeight = changedTableHeight;
		
		return changedTableHeight;
	};
	
	po.bindResizeDataTable = function()
	{
		$.bindResizeDataTableHandler(po.expectedResizeDataTableElements,
			function()
			{
				return po.calChangedDataTableHeight();
			});
	};
})
(${pageId});
</script>

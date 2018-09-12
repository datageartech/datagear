<%--
/*
 * Copyright (c) 2018 by datagear.org.
 */
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%--
表格JS片段。

依赖：
page_js_obj.jsp

变量：
//排序回调函数，允许为null，格式为：function(order){}
pageObj.sort = undefined;
--%>
<script type="text/javascript">
(function(pageObj)
{
	pageObj.table = pageObj.element("#${pageId}-table");
	//上一次order，DataTables会在某些无关的情况产生order事件，这里存储上一次order，用于比较并忽略排序
	pageObj.previousOrder = undefined;
	
	//计算表格高度
	pageObj.calTableHeight = function()
	{
		var height =  pageObj.element(".content").height() - 50;
		return height;
	};

	pageObj.getTableSettings = function(columns, initDatas)
	{
		var settings=
		{
			"scrollX": true,
			"scrollY" : pageObj.calTableHeight(),
	        "scrollCollapse": false,
			"paging" : false,
			"searching" : false,
			"select" : true,
			"data": (initDatas || []),
		    "columns": columns,
		    "language":
		    {
				"emptyTable": "<fmt:message key='noData' />"
			}
		};
		
		return settings;
	};
	
	pageObj.initTable = function(tableSettings)
	{
		pageObj.tableSettings = tableSettings;
		pageObj.table.dataTable(tableSettings)
		.on("order.dt", function(event, settings)
		{
			var doSort = false;
			
			var currentOrder = pageObj.getOrder();
			
			if(!pageObj.previousOrder)
				doSort=true;
			else
			{
				if(pageObj.previousOrder.length != currentOrder.length)
					doSort = true;
				else
				{
					var po = pageObj.previousOrder;
					var co = currentOrder;
					
					for(var i=0; i<po.length; i++)
					{
						if(po[i][0] != co[i][0] || po[i][1] != co[i][1])
						{
							doSort = true;
							break;
						}
					}
				}
			}
			
	    	if(doSort)
	    	{
	    		if(pageObj.sort)
	    		{
	    			var propNameOrder = pageObj.copyOrder(currentOrder, pageObj.tableSettings.columns);
	    			pageObj.sort(propNameOrder, currentOrder);
	    		}
	    		
	    		pageObj.previousOrder = pageObj.copyOrder(currentOrder);
	    	}
	    	
	    	return false;
	    });
	};
	
	pageObj.setPagingData = function(pagingData)
	{
		pageObj.deleteAllRow();
		pageObj.addRowData(pagingData.items);
		
		if(pageObj.refreshPagination)
			pageObj.refreshPagination(pagingData.total, pagingData.page, pagingData.pageSize);
	};
	
	pageObj.setTableData = function(tableDatas)
	{
		pageObj.deleteAllRow();
		pageObj.addRowData(tableDatas);
	};

	//单选处理函数
	pageObj.executeOnSelect = function(callback)
	{
		var rows = pageObj.table.DataTable().rows('.selected');
		var rowsData = pageObj.getRowsData(rows);
		
		if(!rowsData || rowsData.length != 1)
			$.tipInfo("<fmt:message key='pleaseSelectOnlyOneRow' />");
		else
		{
			callback.call(pageObj, rowsData[0], pageObj.getRowsIndex(rows)[0]);
		}
	};
	
	//多选处理函数
	pageObj.executeOnSelects = function(callback)
	{
		var rows = pageObj.table.DataTable().rows('.selected');
		var rowsData = pageObj.getRowsData(rows);
		
		if(!rowsData || rowsData.length < 1)
			$.tipInfo("<fmt:message key='pleaseSelectAtLeastOneRow' />");
		else
		{
			callback.call(pageObj, rowsData, pageObj.getRowsIndex(rows));
		}
	};
	
	pageObj.getRowsData = function(rows)
	{
		if(rows == undefined)
			rows = pageObj.table.DataTable().rows();
		
		var tableRowsData = rows.data();
		
		var rowsData = [];
		for(var i=0; i<tableRowsData.length; i++)
			rowsData[i] = tableRowsData[i];
		
		return rowsData;
	};
	
	pageObj.getRowsIndex = function(rows)
	{
		if(rows == undefined)
			rows = pageObj.table.DataTable().rows();
			
		var indexes = rows.indexes();
		
		return indexes;
	};
	
	pageObj.addRowData = function(data)
	{
		var table = pageObj.table.DataTable();
		
		if($.isArray(data))
			table.rows.add(data).draw();
		else
			table.row.add(data).draw();
	};
	
	pageObj.setRowData = function(rowIndex, data)
	{
		var table = pageObj.table.DataTable();
		
		if(rowIndex.length != undefined)
		{
			for(var i=0; i< rowIndex.length; i++)
			{
				table.row(rowIndex[i]).data(data[i]).draw();
			}
		}
		else
			table.row(rowIndex).data(data).draw();
	};
	
	pageObj.deleteRow = function(rowIndex)
	{
		var table = pageObj.table.DataTable();
		
		if(rowIndex.length != undefined)
		{
			table.rows(rowIndex).remove().draw();
		}
		else
			table.row(rowIndex).remove().draw();
	};
	
	pageObj.deleteAllRow = function()
	{
		pageObj.table.DataTable().rows().remove();
	};
	
	pageObj.getOrder = function()
	{
		var table = pageObj.table.DataTable();
		
    	return table.order();
	};
	
	pageObj.getOrderTyped = function()
	{
    	var order = pageObj.getOrder();
    	return pageObj.copyOrder(order, pageObj.tableSettings.columns);
	};
	
	pageObj.copyOrder = function(order, columns)
	{
		var target = [];
		
		for(var i=0; i<order.length; i++)
		{
			if(columns)
			{
				target[i] =
				{
					"name" : columns[order[i][0]].data,
					"type" : order[i][1]
				};
			}
			else
			{
				target[i] = [];
				target[i][0] = order[i][0];
				target[i][1] = order[i][1];
			}
		}
		
		return target;
	};
	
	//表格高度自适应
	$(window).on('resize', function(e) 
	{
		clearTimeout(pageObj.tableResizeTimer);
		
		pageObj.tableResizeTimer = setTimeout(function()
		{
			var table = pageObj.table.DataTable();
			pageObj.element('.dataTables_scrollBody').css('height', pageObj.calTableHeight()+"px");      
			table.draw();
		},
		250);
	});
})
(${pageId});
</script>

<%--
/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%--
表格JS片段。

依赖：
page_js_obj.jsp
--%>
<script type="text/javascript">
(function(pageObj)
{
	pageObj.table = pageObj.element("#${pageId}-table");
	
	//计算表格高度
	pageObj.calTableHeight = function()
	{
		var height =  pageObj.element("> .content").actual("height") - 50;
		
		return height;
	};
	
	//计算表格宽度
	pageObj.calTableWidth = function()
	{
		var width = pageObj.element("> .content").actual("width");
		
		return width;
	};
	
	pageObj.renderCheckColumn = function(data, type, row, meta)
	{
		return "<div class='ui-widget ui-widget-content ui-corner-all checkbox'><span class='ui-icon ui-icon-check'></span></div>";
	};
	
	/**
	 * 默认pageObj.buildDataTableSettingsAjax请求参数实现。
	 */
	pageObj.dataTableAjaxParam = function()
	{
		var param = {};
		
		if(pageObj.searchParam)
			$.extend(param, pageObj.searchParam);
		
		if(pageObj.pagingParam)
			$.extend(param, pageObj.pagingParam);
		
		return param;
	};
	
	/**
	 * 默认pageObj.buildDataTableSettingsAjax请求成功回调实现。
	 */
	pageObj.dataTableAjaxSuccess = function(pagingData, textStatus, jqXHR)
	{
		if(pageObj.refreshPagination)
			pageObj.refreshPagination(pagingData.total, pagingData.page, pagingData.pageSize);
	};
	
	/**
	 * 集成data_page_obj_searchform_js.jsp的默认实现。
	 */
	pageObj.search = function(searchParam)
	{
		pageObj.searchParam = searchParam;
		
		pageObj.refresh();
	};
	
	/**
	 * 集成page_obj_pagination.jsp的默认实现。
	 */
	pageObj.paging = function(pagingParam)
	{
		pageObj.pagingParam = pagingParam;
		pageObj.refresh();
		
		return false;
	};
	
	/**
	 * 构建ajax数据表格选项。
	 * 此ajax选项支持两个回调函数：
	 *   pageObj.dataTableAjaxParam() 用于扩展ajax请求参数；
	 *   pageObj.dataTableAjaxSuccess(pagingData, textStatus, jqXHR) ajax成功回调函数；
	 * @param columns 必选，列元数据
	 * @param url 必选，ajax请求URL
	 * @param ajaxSuccessCallback 可选，ajax成功回调函数，function(pagingData, textStatus, jqXHR){}
	 * @param settings 可选，其他选项
	 */
	pageObj.buildDataTableSettingsAjax = function(columns, url, settings)
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
					var name = $.unescapePropertyNameForDataTables(data.columns[data.order[i].column].data);
					name = $.propertyPath.escapePropertyName(name);
					
					nameOrder[i] = { "name" : name, "type" : data.order[i].dir };
				}
				
				var myData = undefined;
				
				if($.isFunction(pageObj.dataTableAjaxParam))
					myData = pageObj.dataTableAjaxParam();
				else
					myData = pageObj.dataTableAjaxParam;
				
				var param = $.extend({ "order" : nameOrder }, myData);
				
				$.ajax(
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
						
						if(pageObj.dataTableAjaxSuccess)
							pageObj.dataTableAjaxSuccess(data, textStatus, jqXHR);
					}
				});
			}
		},
		settings);
		
		return pageObj.buildDataTableSettings(settings);
	};
	
	/**
	 * 构建本地数据表格选项。
	 * @param columns 必选，列元数据
	 * @param data 可选，初始数据
	 * @param settings 可选，其他选项
	 */
	pageObj.buildDataTableSettingsLocal = function(columns, data, settings)
	{
		settings = $.extend(
		{
			"columns" : columns,
			"data" : (data ? data : [])
		}, 
		settings);
		
		return pageObj.buildDataTableSettings(settings);
	};
	
	/**
	 * 构建表格选项。
	 * @param settings 必选，选项
	 */
	pageObj.buildDataTableSettings = function(settings)
	{
		var newColumns = [
				{
					title : "<fmt:message key='select' />", data : "", defaultContent: "", width : "3em",
					orderable : false, render : pageObj.renderCheckColumn, className : "column-check"
				}
			];
		newColumns = newColumns.concat(settings.columns);
		
		settings = $.extend(
		{
			"scrollX": true,
			"scrollY" : pageObj.calTableHeight(),
	        "scrollCollapse": false,
			"paging" : false,
			"searching" : false,
			"select" : { style : 'os' },
			"order": [[1, "asc"]],
			"fixedColumns": { leftColumns: 1 },
		    "language":
		    {
				"emptyTable": "<fmt:message key='dataTables.noData' />",
				"zeroRecords" : "<fmt:message key='dataTables.zeroRecords' />"
			},
			"createdRow": function(row, data, dataIndex)
			{
				$(".column-check", row).click(function(event)
				{
					event.stopPropagation();
					
					var tr = $(this).closest("tr");
					var selected = tr.hasClass("selected");
					
					if(selected)
						pageObj.table.DataTable().row(tr).deselect();
					else
						pageObj.table.DataTable().row(tr).select();
				});
			}
		},
		settings);
		
		settings.columns = newColumns;
		
		return settings;
	};
	
	pageObj.initDataTable = function(tableSettings)
	{
		pageObj.tableSettings = tableSettings;
		pageObj.table.dataTable(tableSettings);
		
		$(".dataTables_scrollHead .column-check", pageObj.table.DataTable().table().container()).click(function()
		{
			var $this = $(this);
			var checked = $this.hasClass("all-checked");
			
			var rows = pageObj.table.DataTable().rows();
			
			if(checked)
			{
				rows.deselect();
				$this.removeClass("all-checked");
			}
			else
			{
				rows.select();
				$this.addClass("all-checked");
			}
		});
	};
	
	pageObj.refresh = function()
	{
		pageObj.table.DataTable().draw();
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
	
	//获取选中数据
	pageObj.getSelectedData = function()
	{
		var rows = pageObj.table.DataTable().rows('.selected');
		var rowsData = pageObj.getRowsData(rows);
		
		return (rowsData || []);
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
	
	//表格高度自适应
	$(window).on('resize', function(event) 
	{
		//窗口或者父元素（比如所在对话框）调整大小
		var resize = (event.target == window || pageObj.table.closest(event.target).length > 0);
		
		if(resize)
		{
			clearTimeout(pageObj.tableResizeTimer);
			
			pageObj.tableResizeTimer = setTimeout(function()
			{
				var dtScrollBody = pageObj.element('.dataTables_scrollBody');
				
				var height = pageObj.calTableHeight();
				var width = pageObj.element(".dataTable", dtScrollBody).actual("width");
				
				dtScrollBody.css('height', height);
				pageObj.element('.dataTables_scrollHeadInner').css('width', width);
				pageObj.element('.dataTables_scrollHeadInner > .dataTable').css('width', width);
				
				//XXX 不能使用下面的代码让表格自适应宽度，因为在隐藏选项卡中的表格宽度计算会有问题
				//var height = pageObj.calTableHeight();
				//pageObj.element('.dataTables_scrollBody').css('height', height);
				//pageObj.table.DataTable().draw();
			},
			250);
		}
	});
})
(${pageId});
</script>

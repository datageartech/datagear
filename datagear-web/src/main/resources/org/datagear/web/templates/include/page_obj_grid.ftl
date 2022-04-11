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
	po.selectOperation = ("${(selectOperation!false)?string('true','false')}" == "true");
	po.isMultipleSelect = ("${(isMultipleSelect!false)?string('true','false')}" == "true");
	
	po.table = function()
	{
		return this.elementOfId("${pageId}-table");
	};
	
	po.tableDataTable = function(table)
	{
		table = (table == null ? po.table() : table);
		return table.DataTable();
	};
	
	po.initGridBtns = function(parent)
	{
		parent = (parent == null ? po.element(".operation") : parent);
		$.initButtons(parent);
	};
	
	po.initTable = function(settings, table, autoResize)
	{
		//(settings, true||false)
		if(table === true || table === false)
		{
			autoResize = table;
			table = null;
		}
		table = (table == null ? po.table() : table);
		autoResize = (autoResize == null ? true : autoResize);
		
		table.dataTable(settings);
		$.dataTableUtil.bindCheckColumnEvent(po.tableDataTable(table));
		table.addClass($.AUTO_RESIZEABLE_ELE_CLASS_NAME);
		
		if(autoResize && !po.bindAutoResizableHandlerDone)
		{
			po.bindAutoResizableHandler();
			po.bindAutoResizableHandlerDone = true;
		}
	};
	
	po.bindAutoResizableHandler = function()
	{
		$.bindAutoResizableHandler(po.element().attr("id"),
		function(ele)
		{
			po.resizeAutoResizable(ele);
		});
	};
	
	po.resizeAutoResizable = function(ele)
	{
		ele = $(ele);
		
		if(ele.is("table"))
		{
			var height = po.evalTableHeight();
			$.updateDataTableHeight(ele, height, true);
		}
	};
	
	po.evalTableHeight = function()
	{
		var height =  po.element("> .content").height() - 50;
		return height;
	};
	
	po.refresh = function(table)
	{
		table = (table == null ? po.table() : table);
		
		po.tableDataTable(table).draw();
	};
	
	//单选处理函数
	po.executeOnSelect = function(callback, table)
	{
		table = (table == null ? po.table() : table);
		
		$.dataTableUtil.executeOnSelect(po.tableDataTable(table), "<@spring.message code='pleaseSelectOnlyOneRow' />",
		function(row, rowIndex){ callback.call(po, row, rowIndex); });
	};
	
	//多选处理函数
	po.executeOnSelects = function(callback, table)
	{
		table = (table == null ? po.table() : table);
		
		$.dataTableUtil.executeOnSelects(po.tableDataTable(table), "<@spring.message code='pleaseSelectAtLeastOneRow' />",
		function(rows, rowIndexes){ callback.call(po, rows, rowIndexes); });
	};
	
	po.getSelectedData = function(table)
	{
		table = (table == null ? po.table() : table);
		
		return $.dataTableUtil.getSelectedData(po.tableDataTable(table));
	};
	
	po.addRowData = function(data, table)
	{
		table = (table == null ? po.table() : table);
		
		$.dataTableUtil.addRowData(po.tableDataTable(table), data);
	};
	
	po.deleteSelectedRows = function(table)
	{
		table = (table == null ? po.table() : table);
		
		$.dataTableUtil.deleteSelectedRows(po.tableDataTable(table));
	};
	
	po.handleAddOperation = function(url, options)
	{
		if(po.selectOperation)
		{
			options = $.extend(true,
			{
				pageParam:
				{
					submitSuccess: function(response)
					{
						var data = (response.data ? response.data : response);
						data = (po.isMultipleSelect && !$.isArray(data) ? [data] : data);
						
						po.pageParamCallSelect(data);
					}
				}
			},
			options);
		}
		
		po.open(url, options);
	};
	
	po.handleOpenOfOperation = function(url, options, table)
	{
		table = (table == null ? po.table() : table);
		
		po.executeOnSelect(function(row)
		{
			var data = po.toOperationRequestData(row, false);
			options = $.extend({ data: data }, options);
			
			po.open(url, options);
		},
		table);
	};
	
	po.handleOpenOfsOperation = function(url, options, table)
	{
		table = (table == null ? po.table() : table);
		
		po.executeOnSelects(function(rows)
		{
			var data = po.toOperationRequestData(rows, false);
			options = $.extend({ data: data }, options);
			
			po.open(url, options);
		},
		table);
	};
	
	po.handleDeleteOperation = function(url, table)
	{
		table = (table == null ? po.table() : table);
		
		po.executeOnSelects(function(rows)
		{
			po.confirm("<@spring.message code='confirmDelete' />",
			{
				"confirm" : function()
				{
					$.postJson(url, po.toOperationRequestData(rows, true), function()
					{
						po.refresh();
					});
				}
			});
		},
		table);
	};
	
	po.handleSelectOperation = function(table)
	{
		table = (table == null ? po.table() : table);
		
		if(po.isMultipleSelect)
		{
			po.executeOnSelects(function(rows)
			{
				po.pageParamCallSelect(rows);
			},
			table);
		}
		else
		{
			po.executeOnSelect(function(row)
			{
				po.pageParamCallSelect(row);
			},
			table);
		}
	};
	
	/**
	 * 将单行或多行数据对象转换为操作请求数据。
	 * 
	 * @param data
	 * @param json 可选，是否JSON请求
	 */
	po.toOperationRequestData = function(data, json)
	{
		if(json)
			return $.propertyValue(data, "id");
		else
			return $.getPropertyParamString(data, "id");
	};
	
	/**
	 * 调用页面参数对象的"select"函数。
	 * @param selectedData 选中数据
	 * @param close 可选，是否关闭
	 */
	po.pageParamCallSelect = function(selectedData, close)
	{
		close = (close == null ? true : close);
		
		this.pageParamCall("select", selectedData);
		
		if(close && !this.isDialogPinned())
			this.close();
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
	
	/**
	 * 默认po.buildAjaxTableSettings请求参数实现。
	 */
	po.ajaxTableParam = function()
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
	 * 默认po.buildAjaxTableSettings请求成功回调实现。
	 */
	po.ajaxTableSuccess = function(pagingData, textStatus, jqXHR)
	{
		if(po.refreshPagination)
			po.refreshPagination(pagingData.total, pagingData.page, pagingData.pageSize);
		
		po.pageParamCall("ajaxTableSuccess", pagingData, textStatus, jqXHR);
	};
	
	/**
	 * 构建ajax数据表格选项。
	 * 此ajax选项支持两个回调函数：
	 *   po.ajaxTableParam() 用于扩展ajax请求参数；
	 *   po.ajaxTableSuccess(pagingData, textStatus, jqXHR) ajax成功回调函数；
	 * @param columns 列元数据
	 * @param url ajax请求URL
	 * @param settings 可选，其他选项
	 */
	po.buildAjaxTableSettings = function(columns, url, settings)
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
				
				var myData = po.ajaxTableParam();
				
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
						
						if(po.ajaxTableSuccess)
							po.ajaxTableSuccess(data, textStatus, jqXHR);
					}
				});
			}
		},
		settings);
		
		return po.buildTableSettings(settings);
	};
	
	/**
	 * 构建本地数据表格选项。
	 * @param columns 必选，列元数据
	 * @param data 可选，初始数据
	 * @param settings 可选，其他选项
	 */
	po.buildLocalTableSettings = function(columns, data, settings)
	{
		settings = $.extend(
		{
			"columns" : columns,
			"data" : (data ? data : [])
		}, 
		settings);
		
		return po.buildTableSettings(settings);
	};
	
	//构建表格选项。
	po.buildTableSettings = function(settings)
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
			"scrollY" : po.evalTableHeight(),
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
	
	po.renderCheckColumn = function(data, type, row, meta)
	{
		return $.dataTableUtil.renderCheckColumn(data, type, row, meta);
	};

	po.getOrdersOnName = function(table)
	{
		table = (table == null ? po.table() : table);
		
		return $.dataTableUtil.getOrdersOnName(po.tableDataTable(table));
	};
	
	//获取表格元素的父元素
	po.tableParent = function(table)
	{
		table = (table == null ? po.table() : table);
		
		return $.dataTableUtil.tableParent(po.tableDataTable(table));
	};
})
(${pageId});
</script>

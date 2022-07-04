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

变量：
//操作
String action

-->
<#assign PrimveVueTestController=statics['org.datagear.web.controller.PrimveVueTestController']>
<script>
(function(po)
{
	po.action = "${action!PrimveVueTestController.REQUEST_ACTION_QUERY}";
	po.isSingleSelectAction = (po.action == "${PrimveVueTestController.REQUEST_ACTION_SINGLE_SELECT}");
	po.isMultipleSelectAction = (po.action == "${PrimveVueTestController.REQUEST_ACTION_MULTIPLE_SELECT}");
	po.isSelectAction = (po.isSingleSelectAction || po.isMultipleSelectAction);
	
	po.rowsPerPageOptions = [10, 20, 50, 100, 200];
	po.rowsPerPage = po.rowsPerPageOptions[1];
	
	po.tableModel = function(obj)
	{
		return po.vueSetup("tableModel", obj);
	};
	
	po.tableAttr = function(obj)
	{
		return po.attr("tableAttr", obj);
	};
	
	po.setupAjaxTable = function(url, options)
	{
		options = $.extend({ multiSortMeta: [], initData: true }, options);
		
		var tableModel = po.tableModel(
		{
			items: [],
			paginator: true,
			paginatorTemplate: "CurrentPageReport FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink RowsPerPageDropdown",
			pageReportTemplate: "{first}-{last} / {totalRecords}",
			rowsPerPage: po.rowsPerPage,
			rowsPerPageOptions: po.rowsPerPageOptions,
			totalRecords: 0,
			loading: false,
			selectionMode: "multiple",
			multiSortMeta: options.multiSortMeta,
			selectedItems: [],
			
			handlePaginator: function(e)
			{
				po.setAjaxTableParam({ page: e.page+1, pageSize: e.rows, orders: po.sortMetaToOrders(e.multiSortMeta) });
				po.loadAjaxTable();
			},
			handleSort: function(e)
			{
				po.setAjaxTableParam({ orders: po.sortMetaToOrders(e.multiSortMeta) });
				po.loadAjaxTable();
			}
		});
		
		po.tableAttr(
		{
			url: url,
			param: { page: 1, pageSize: po.rowsPerPage, orders: po.sortMetaToOrders(options.multiSortMeta) }
		});
		
		if(po.isSelectAction)
			po.setupSelectAction();
		
		if(options.initData)
		{
			po.vueMounted(function()
			{
				po.loadAjaxTable();
			});
		}
		
		return tableModel;
	};
	
	po.setAjaxTableParam = function(param)
	{
		var tableAttr = po.tableAttr();
		$.extend(tableAttr.param, param);
	};
	
	po.loadAjaxTable = function(options)
	{
		options = (options || {});
		
		var tableAttr = po.tableAttr();
		var tableModel = po.tableModel();
		tableModel.loading = true;
		
		options = $.extend(
		{
			data: tableAttr.param,
			success: function(pagingData)
			{
				po.setAjaxTablePagingData(pagingData);
			},
			complete: function()
			{
				tableModel.loading = false;
			}
		},
		options);
		
		$.ajaxJson(po.concatContextPath(tableAttr.url), options)
	};
	
	po.sortMetaToOrders = function(sortMeta)
	{
		var orders = [];
		
		sortMeta.forEach(function(sm)
		{
			orders.push({ name: sm.field, type: (sm.order > 0 ? "ASC" : "DESC") });
		});
		
		return orders;
	};
	
	po.setAjaxTablePagingData = function(pagingData)
	{
		var tableModel = po.tableModel();
		
		tableModel.items = pagingData.items;
		tableModel.totalRecords = pagingData.total;
		tableModel.selectedItems = [];
	};
	
	po.refresh = function()
	{
		po.loadAjaxTable();
	};
	
	//重写搜索表单提交处理函数
	po.search = function(formData)
	{
		po.setAjaxTableParam($.extend(formData, { page: 1 }));
		po.loadAjaxTable();
	};
	
	//单选处理函数
	po.executeOnSelect = function(callback)
	{
		var selectedItems = po.tableModel().selectedItems;
		
		if(!selectedItems || selectedItems.length != 1)
		{
			$.tipInfo("<@spring.message code='pleaseSelectOnlyOneRow' />");
			return;
		}
		
		callback.call(po, po.vueRaw(selectedItems[0]));
	};
	
	//多选处理函数
	po.executeOnSelects = function(callback)
	{
		var selectedItems = po.tableModel().selectedItems;
		
		if(!selectedItems || selectedItems.length < 1)
		{
			$.tipInfo("<@spring.message code='pleaseSelectAtLeastOneRow' />");
			return;
		}
		
		callback.call(po, po.vueRaw(selectedItems));
	};
	
	po.handleAddAction = function(url, options)
	{
		var action = { url: po.concatContextPath(url), options: options };
		po.inflateFormActionPageParam(action);
		po.open(action.url, action.options);
	};
	
	po.handleOpenOfAction = function(url, options)
	{
		po.executeOnSelect(function(row)
		{
			var action = { url: po.concatContextPath(url), options: options };
			po.inflateFormActionPageParam(action);
			po.inflateRowAction(action, row);
			po.open(action.url, action.options);
		});
	};
	
	po.handleOpenOfsAction = function(url, options)
	{
		po.executeOnSelects(function(rows)
		{
			var action = { url: po.concatContextPath(url), options: options };
			po.inflateFormActionPageParam(action);
			po.inflateRowAction(action, rows);
			po.open(action.url, action.options);
		});
	};
	
	po.handleDeleteAction = function(url, options)
	{
		po.executeOnSelects(function(rows)
		{
			po.confirm("<@spring.message code='confirmDelete' />",
			{
				"confirm" : function()
				{
					options = $.extend(
					{
						contentType: $.CONTENT_TYPE_JSON,
						success: function(){ po.refresh(); }
					},
					options);
					
					var action = { url: po.concatContextPath(url), options: options };
					po.inflateRowAction(action, rows);
					
					$.ajaxJson(url, action.options);
				}
			});
		});
	};
	
	po.handleSelectAction = function()
	{
		if(po.isMultipleSelectAction)
		{
			po.executeOnSelects(function(rows)
			{
				po.pageParamCallSelect(rows);
			});
		}
		else
		{
			po.executeOnSelect(function(row)
			{
				po.pageParamCallSelect(row);
			});
		}
	};
	
	//调用页面参数对象的"select"函数
	po.pageParamCallSelect = function(selected, close)
	{
		close = (close == null ? true : close);
		
		var myClose = this.pageParamCall("select", selected);
		
		if(myClose === false)
			return;
		
		if(close)
			this.close();
	};
	
	po.inflateFormActionPageParam = function(action)
	{
		action.options = $.extend(
		{
			pageParam:
			{
				submitSuccess: function(response)
				{
					po.refresh();
				}
			}
		},
		action.options);
	};
	
	po.inflateRowActionIdPropName = "id";
	po.inflateRowActionIdParamName = "id";
	
	//将单行或多行数据对象转换为操作请求数据
	po.inflateRowAction = function(action, rowOrRows)
	{
		var id = $.propertyValue(rowOrRows, po.inflateRowActionIdPropName);
		
		if($.CONTENT_TYPE_JSON == action.options.contentType)
		{
			var options = action.options;
			if(options.data == null)
				options.data = id;
			else
			{
				var data = {};
				data[po.inflateRowActionIdParamName] = id;
				options.data = $.extend(data, options.data);
			}
		}
		else
		{
			if($.isArray(id))
			{
				for(var i=0; i<id.length; i++)
					action.url = $.addParam(action.url, po.inflateRowActionIdParamName, id[i], true);
			}
			else
				action.url = $.addParam(action.url, po.inflateRowActionIdParamName, id);
		}
	};
})
(${pageId});
</script>

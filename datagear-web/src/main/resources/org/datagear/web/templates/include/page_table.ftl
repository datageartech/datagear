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
page_manager.ftl

-->
<script>
(function(po)
{
	po.refresh = function()
	{
		po.loadAjaxTable();
	};
	
	po.getSelectedEntities = function()
	{
		var pm = po.vuePageModel();
		return $.wrapAsArray(po.vueRaw(pm.selectedItems));
	};
	
	po.rowsPerPageOptions = [10, 20, 50, 100, 200];
	po.rowsPerPage = po.rowsPerPageOptions[1];
	
	po.tableAttr = function(obj)
	{
		return po.attr("tableAttr", obj);
	};
	
	po.setupAjaxTable = function(url, options)
	{
		options = $.extend({ multiSortMeta: [], initData: true }, options);
		
		var pm = po.vuePageModel(
		{
			items: [],
			paginator: true,
			paginatorTemplate: "CurrentPageReport FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink RowsPerPageDropdown",
			pageReportTemplate: "{first}-{last} / {totalRecords}",
			rowsPerPage: po.rowsPerPage,
			rowsPerPageOptions: po.rowsPerPageOptions,
			totalRecords: 0,
			loading: false,
			selectionMode: (po.isSingleSelectAction ? "single" : "multiple"),
			multiSortMeta: options.multiSortMeta,
			selectedItems: null
		});
		
		if(po.isSelectAction)
		{
			po.vueRef("isSelectAction", po.isSelectAction);
		}
		
		po.vueMethod(
		{
			onPaginator: function(e)
			{
				po.setAjaxTableParam({ page: e.page+1, pageSize: e.rows, orders: po.sortMetaToOrders(e.multiSortMeta) });
				po.loadAjaxTable();
			},
			onSort: function(e)
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
		
		if(options.initData)
		{
			po.vueMounted(function()
			{
				po.loadAjaxTable();
			});
		}
		
		return pm;
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
		var pm = po.vuePageModel();
		pm.loading = true;
		
		options = $.extend(
		{
			data: tableAttr.param,
			success: function(response)
			{
				po.setAjaxTableData(response);
			},
			complete: function()
			{
				pm.loading = false;
			}
		},
		options);
		
		po.ajaxJson(tableAttr.url, options);
	};
	
	po.sortMetaToOrders = function(sortMeta)
	{
		var orders = [];
		
		$.each(sortMeta, function(idx, sm)
		{
			orders.push({ name: sm.field, type: (sm.order > 0 ? "ASC" : "DESC") });
		});
		
		return orders;
	};
	
	po.setAjaxTableData = function(data)
	{
		var isPagingData = (data.items != null && data.total != null);
		var pm = po.vuePageModel();
		
		pm.items = (isPagingData ? data.items : data);
		pm.totalRecords = (isPagingData ? data.total : data.length);
		pm.selectedItems = null;
	};
	
	//重写搜索表单提交处理函数
	po.search = function(formData)
	{
		po.setAjaxTableParam($.extend(formData, { page: 1 }));
		po.loadAjaxTable();
	};
})
(${pid});
</script>

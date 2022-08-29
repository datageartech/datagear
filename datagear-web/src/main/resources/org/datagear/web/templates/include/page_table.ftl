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
	//重写搜索表单提交处理函数
	po.search = function(formData)
	{
		po.ajaxTableQuery($.extend(formData, { page: 1 }));
		po.loadAjaxTable();
	};
	
	po.refresh = function()
	{
		//兼容搜索表单集成
		if(po.submitSearchForm)
			po.submitSearchForm();
		else
			po.loadAjaxTable();
	};
	
	po.getSelectedEntities = function()
	{
		var pm = po.vuePageModel();
		return $.wrapAsArray(po.vueRaw(pm.selectedItems));
	};
	
	po.rowsPerPageOptions = [10, 20, 50, 100, 200];
	po.rowsPerPage = po.rowsPerPageOptions[1];
	
	po.ajaxTableAttr = function(obj)
	{
		return po.attr("ajaxTableAttr", obj);
	};
	
	po.setupAjaxTable = function(url, options)
	{
		options = $.extend({ multiSortMeta: [], initData: true }, options);
		
		po.setupAction();
		
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
			selectionMode: ((po.isQueryAction || po.isMultipleSelect) ? "multiple" : "single"),
			multiSortMeta: options.multiSortMeta,
			selectedItems: null
		});
		
		po.vueMethod(
		{
			onPaginator: function(e)
			{
				po.ajaxTableQuery({ page: e.page+1, pageSize: e.rows, orders: po.sortMetaToOrders(e.multiSortMeta) });
				po.loadAjaxTable();
			},
			onSort: function(e)
			{
				po.ajaxTableQuery({ orders: po.sortMetaToOrders(e.multiSortMeta) });
				po.loadAjaxTable();
			}
		});
		
		po.ajaxTableAttr(
		{
			url: url,
			query: { page: 1, pageSize: po.rowsPerPage, orders: po.sortMetaToOrders(options.multiSortMeta) }
		});
		
		if(options.initData)
		{
			po.vueMounted(function()
			{
				po.refresh();
			});
		}
		
		return pm;
	};
	
	po.ajaxTableQuery = function(query)
	{
		var ajaxTableAttr = po.ajaxTableAttr();
		
		if(query === undefined)
			return ajaxTableAttr.query;
		else
			$.extend(ajaxTableAttr.query, query);
	};
	
	po.loadAjaxTable = function(options)
	{
		options = (options || {});
		
		var ajaxTableAttr = po.ajaxTableAttr();
		var pm = po.vuePageModel();
		pm.loading = true;
		
		options = $.extend(
		{
			data: ajaxTableAttr.query,
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
		
		po.ajaxJson(ajaxTableAttr.url, options);
	};
	
	po.sortMetaToOrders = function(sortMeta)
	{
		if(sortMeta == null)
		{
			var pm = po.vuePageModel();
			sortMeta = pm.multiSortMeta;
		}
		
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
})
(${pid});
</script>

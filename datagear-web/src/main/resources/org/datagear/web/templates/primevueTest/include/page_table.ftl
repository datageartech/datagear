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
	
	po.vueSetupTable = function(obj)
	{
		return po.vueSetup("tableModel", obj);
	};
	
	po.attrTable = function(obj)
	{
		return po.attr("tableAttr", obj);
	};
	
	po.setupAjaxTable = function(url, options)
	{
		options = $.extend({ multiSortMeta: [], initData: true }, options);
		
		po.vueSetupTable(
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
		
		po.attrTable(
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
	};
	
	po.setAjaxTableParam = function(param)
	{
		var tableAttr = po.attrTable();
		$.extend(tableAttr.param, param);
	};
	
	po.loadAjaxTable = function(options)
	{
		options = (options || {});
		
		var tableAttr = po.attrTable();
		var tableModel = po.vueSetupTable();
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
		var tableModel = po.vueSetupTable();
		
		tableModel.items = pagingData.items;
		tableModel.totalRecords = pagingData.total;
		tableModel.selectedItems = [];
	};
	
	//重写搜索表单提交处理函数
	po.search = function(formData)
	{
		po.setAjaxTableParam($.extend(formData, { page: 1 }));
		po.loadAjaxTable();
	};
})
(${pageId});
</script>

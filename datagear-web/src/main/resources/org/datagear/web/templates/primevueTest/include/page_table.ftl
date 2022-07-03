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
	
	po.setupAjaxTable = function(url, options)
	{
		options = $.extend({ multiSortMeta: [], initData: true }, options);
		
		po.vueRef("tableItems", []);
		po.vueRef("tablePaginator", true);
		po.vueRef("tablePaginatorTemplate", "CurrentPageReport FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink RowsPerPageDropdown");
		po.vueRef("tablePageReportTemplate", "{first}-{last} / {totalRecords}");
		po.vueRef("tableRowsPerPage", po.rowsPerPage);
		po.vueRef("tableRowsPerPageOptions", po.rowsPerPageOptions);
		po.vueRef("tableTotalRecords", 0);
		po.vueRef("tableLoading", false);
		po.vueRef("tableSelectionMode", "multiple");
		po.vueReactive("tableMultiSortMeta", options.multiSortMeta);
		po.vueRef("tableSelectedItems", []);
		
		po.attr("tableLoadUrl", url);
		po.loadAjaxTableParam({ page: 1, pageSize: po.rowsPerPage, orders: po.sortMetaToOrders(options.multiSortMeta) });
		
		po.vueSetup("tableHandlePaginator", function(e)
		{
			po.loadAjaxTableParam({ page: e.page+1, pageSize: e.rows, orders: po.sortMetaToOrders(e.multiSortMeta) });
			po.loadAjaxTable();
		});
		po.vueSetup("tableHandleSort", function(e)
		{
			po.loadAjaxTableParam({ orders: po.sortMetaToOrders(e.multiSortMeta) });
			po.loadAjaxTable();
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
	
	po.setupLocalTable = function()
	{
		
	};
	
	po.setupSelectAction = function()
	{
		
	};
	
	po.loadAjaxTable = function(options)
	{
		options = (options || {});
		
		var param = po.loadAjaxTableParam();
		
		var url = po.attr("tableLoadUrl");
		po.vueRef("tableLoading", true);
		
		options = $.extend(
		{
			data: param,
			success: function(pagingData)
			{
				po.setAjaxTablePagingData(pagingData);
			},
			complete: function()
			{
				po.vueRef("tableLoading", false);
			}
		},
		options);
		
		$.ajaxJson(po.concatContextPath(url), options)
	};
	
	po.loadAjaxTableParam = function(param)
	{
		var paramOld = (po.attr("tableAjaxParam") || {});
		
		if(param === undefined)
			return paramOld;
		
		paramOld = $.extend(paramOld, param);
		po.attr("tableAjaxParam", paramOld);
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
		po.vueRef("tableItems", pagingData.items);
		po.vueRef("tableTotalRecords", pagingData.total);
		po.vueRef("tableSelectedItems", []);
	};
	
	//重写搜索表单提交处理函数
	po.search = function(formData)
	{
		po.loadAjaxTableParam($.extend(formData, { page: 1 }));
		po.loadAjaxTable();
	};
})
(${pageId});
</script>

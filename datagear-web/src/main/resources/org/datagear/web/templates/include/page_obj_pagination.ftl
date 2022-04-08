<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#--
分页片段。

依赖：
page_obj.ftl

变量：
//分页回调函数，不允许为null，格式为：function(pagingParam){}
po.paging = undefined;
-->
<script type="text/javascript">
(function(po)
{
	po.pagination = function(){ return this.element("#${pageId}-pagination"); };
	
	po.getPagingParam = function()
	{
		var pagingParam =
		{
			"page" : po.pagination().pagination("option", "page"),
			"pageSize" : po.pagination().pagination("option", "pageSize")
		};
		
		return pagingParam;
	};
	
	po.initPagination = function()
	{
		po.pagination().pagination(
		{
			pageSizeSetLabel : "<@spring.message code='confirm' />",
			toPageLabel : "<@spring.message code='jumpto' />",
			labelTemplate: "<@spring.message code='pagination.label' />",
			pageSizeCookie: "PAGINATION_PAGE_SIZE",
			pageSizeCookiePath: "${contextPath}",
			update: function(page, pageSize, total)
			{
				var pagingParam =
				{
					"page" : page,
					"pageSize" : pageSize
				};
				
				po.paging(pagingParam);
				return false;
			}
		});
	};
	
	po.refreshPagination = function(total, page, pageSize)
	{
		po.pagination()
		.pagination("option", "total", total)
		.pagination("option", "pageSize", pageSize)
		.pagination("option", "page", page)
		.pagination("refresh");
	};
})
(${pageId});
</script>

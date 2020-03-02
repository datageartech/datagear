<#--
分页片段。

依赖：
data_page_obj.jsp

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

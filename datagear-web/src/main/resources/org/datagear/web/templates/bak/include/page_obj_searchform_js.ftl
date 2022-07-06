<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#--
查询表单片段。

依赖：
page_obj.ftl

变量：
//查询回调函数，不允许为null，格式为：function(searchParam){}
po.search = undefined;
-->
<script type="text/javascript">
(function(po)
{
	po.searchForm = function(){ return this.elementOfId("${pageId}-searchForm"); };
	
	po.searchForm().submit(function()
	{
		var searchParam = po.getSearchParam();
		po.search(searchParam);
		
		return false;
	});
	
	po.getSearchParam = function()
	{
		var param =
		{
			"keyword" : $.trim(po.elementOfName("keyword", po.searchForm()).val())
		};
		
		return param;
	};
	
	po.getSearchParamString = function()
	{
		var param = po.getSearchParam();
		return $.param(param);
	};
	
	po.element("input:submit", po.searchForm()).button();
})
(${pageId});
</script>

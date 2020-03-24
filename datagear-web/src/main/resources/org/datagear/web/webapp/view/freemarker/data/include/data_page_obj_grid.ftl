<#--
表格功能JS片段。

依赖：


变量：
//查询结果的行映射器，非null
DefaultLOBRowMapper queryDefaultLOBRowMapper

-->
<#include "../../include/page_obj_grid.ftl">
<script type="text/javascript">
(function(po)
{
	po.blobPlaceholder = "${queryDefaultLOBRowMapper.blobPlaceholder?js_string}";
	po.clobPlaceholder = "${queryDefaultLOBRowMapper.clobPlaceholder?js_string}";
	po.sqlXmlPlaceholder = "${queryDefaultLOBRowMapper.sqlXmlPlaceholder?js_string}";
	
	po.isPlaceholderColumnValue = function(column, value)
	{
		if(!value)
			return false;
		
		if($.meta.isBlobColumn(column) && po.blobPlaceholder == value)
			return true;
		
		if($.meta.isClobColumn(column) && po.clobPlaceholder == value)
			return true;
		
		if($.meta.isSqlxmlColumn(column) && po.sqlXmlPlaceholder == value)
			return true;
		
		return false;
	};
	
	/**
	 * 构建ajax数据表格。
	 */
	po.initDataTableAjax = function(url, table)
	{
		var columns = $.buildDataTablesColumns(table);
		var settings = po.buildDataTableSettingsAjax(columns, url);
		
		po.initDataTable(settings);
	};
})
(${pageId});
</script>

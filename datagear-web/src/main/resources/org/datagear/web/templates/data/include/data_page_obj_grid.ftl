<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#--
表格功能JS片段。

依赖：


变量：
//查询结果的行映射器，非null
DefaultLOBRowMapper queryDefaultLOBRowMapper
//关键字查询列数，非null
int keywordQueryColumnCount
-->
<#include "../../include/page_obj_grid.ftl">
<script type="text/javascript">
(function(po)
{
	po.binaryPlaceholder = "${queryDefaultLOBRowMapper.binaryPlaceholder?js_string?no_esc}";
	po.clobPlaceholder = "${queryDefaultLOBRowMapper.clobPlaceholder?js_string?no_esc}";
	po.sqlXmlPlaceholder = "${queryDefaultLOBRowMapper.sqlXmlPlaceholder?js_string?no_esc}";
	po.keywordQueryColumnCount = parseInt("${keywordQueryColumnCount?js_string?no_esc}");
	
	po.isPlaceholderColumnValue = function(column, value)
	{
		if(!value)
			return false;
		
		if($.tableMeta.isBinaryColumn(column) && po.binaryPlaceholder == value)
			return true;
		
		if($.tableMeta.isClobColumn(column) && po.clobPlaceholder == value)
			return true;
		
		if($.tableMeta.isSqlxmlColumn(column) && po.sqlXmlPlaceholder == value)
			return true;
		
		return false;
	};
	
	po.initTableForDbTable = function(url, dbTable)
	{
		var columns = $.buildDataTablesColumns(dbTable, {keywordQueryColumnCount: po.keywordQueryColumnCount});
		var settings = po.buildAjaxTableSettings(columns, url);
		
		po.initTable(settings);
	};
})
(${pageId});
</script>

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
	$.inflatePageTable(po);
})
(${pid});
</script>

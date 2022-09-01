<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#--
SQL编辑器JS片段。

依赖：
page_obj.ftl
page_code_editor.ftl

变量：
//数据源ID，不允许为null
po.getSqlEditorSchemaId
-->
<script type="text/javascript">
(function(po)
{
	$.inflatePageSqlEditor(po);
})
(${pid});
</script>

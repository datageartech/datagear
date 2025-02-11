<#--
 *
 * Copyright 2018-present datagear.tech
 *
 * This file is part of DataGear.
 *
 * DataGear is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * DataGear is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with DataGear.
 * If not, see <https://www.gnu.org/licenses/>.
 *
-->
<#--
SQL编辑器JS片段。

依赖：
page_obj.ftl
page_code_editor.ftl

变量：
//数据源ID，不允许为null
po.getSqlEditorDtbsSourceId
-->
<script type="text/javascript">
(function(po)
{
	//page.js
	$.inflatePageSqlEditor(po);
})
(${pid});
</script>

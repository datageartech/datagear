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
page_js_obj.ftl

变量：
//查询回调函数，不允许为null，格式为：function(searchParam){}
po.search = undefined;
-->
<form id="${pageId}-searchForm" class="search-form" action="#">
	<div class="ui-widget ui-widget-content keyword-widget">
		<div class="keyword-input-parent">
			<input name="keyword" type="text" class="ui-widget ui-widget-content keyword-input" />
		</div>
	</div>
	<input name="submit" type="submit" value="<@spring.message code='query' />" />
</form>
<#include "page_obj_searchform_js.ftl">
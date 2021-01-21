<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#--
查询表单HTML片段。
-->
<form id="${pageId}-searchForm" class="search-form" action="#">
	<div class="ui-widget ui-widget-content keyword-widget simple">
		<div class="keyword-input-parent">
			<input name="keyword" type="text" class="ui-widget ui-widget-content keyword-input" />
		</div>
	</div>
	<input name="submit" type="submit" value="<@spring.message code='query' />" />
</form>

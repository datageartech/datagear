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

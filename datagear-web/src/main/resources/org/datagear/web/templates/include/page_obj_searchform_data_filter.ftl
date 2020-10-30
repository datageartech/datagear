<#--
设置共享筛选条件片段。

依赖：
page_js_obj.jsp
-->
<#assign _ssf_AbstractController=statics['org.datagear.web.controller.AbstractController']>
<#assign _ssf_DataPermissionEntityService=statics['org.datagear.management.service.DataPermissionEntityService']>
<form id="${pageId}-searchForm" class="search-form" action="#">
	<div class="ui-widget ui-widget-content keyword-widget simple">
		<div class="keyword-input-parent">
			<input name="keyword" type="text" class="ui-widget ui-widget-content keyword-input" />
		</div>
	</div>
	<input name="submit" type="submit" value="<@spring.message code='query' />" />
	<div class="data-filter-select-wrapper">
	<select id="${pageId}-dataFilter" name="${_ssf_AbstractController.DATA_FILTER_PARAM}" class="data-filter-select">
		<option value="${_ssf_DataPermissionEntityService.DATA_FILTER_VALUE_ALL}"><@spring.message code='dataFilter.all' /></option>
		<option value="${_ssf_DataPermissionEntityService.DATA_FILTER_VALUE_MINE}"><@spring.message code='dataFilter.mine' /></option>
		<option value="${_ssf_DataPermissionEntityService.DATA_FILTER_VALUE_OTHER}"><@spring.message code='dataFilter.other' /></option>
	</select>
	</div>
</form>
<#include "page_obj_searchform_js.ftl">
<script type="text/javascript">
(function(po)
{
	po.getSearchParam = function()
	{
		var $form = po.searchForm();
		
		var param =
		{
			"keyword" : $.trim(po.element("input[name='keyword']", $form).val()),
			"${_ssf_AbstractController.DATA_FILTER_PARAM}": po.element("#${pageId}-dataFilter", $form).val()
		};
		
		return param;
	};
	
	po.initDataFilter = function()
	{
		var $ele = po.element("#${pageId}-dataFilter");
		
		var val = $.cookie("${_ssf_AbstractController.DATA_FILTER_COOKIE}");
		if(!val)
			val = $("option:first-child", $ele).attr("value");
		
		$ele.val(val);
		
		$ele.selectmenu(
		{
			appendTo: po.searchForm(),
			change: function(event, ui)
			{
				var val = ui.item.value;
				$ele.val(val);
				
				if(po.onSetDataFilter)
					po.onSetDataFilter(val);
				else
					po.searchForm().submit();
				
				$.cookie("${_ssf_AbstractController.DATA_FILTER_COOKIE}", val, {expires : 365*5, path : "${contextPath}/"});
			}
		});
	};
	
	po.getDataFilter = function()
	{
		return po.element("#${pageId}-dataFilter").val();
	};
})
(${pageId});
</script>

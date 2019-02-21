<#include "include/import_global.ftl">
<#include "include/html_doctype.ftl">
<html style="height:100%;">
<head>
<#include "include/html_head.ftl">
<title><#include "include/html_title_app_name.ftl"><@spring.message code='resetPasswordRequestHistory.resetPasswordRequestHistory' /></title>
</head>
<body style="height:100%;">
<#if !isAjaxRequest>
<div style="height:99%;">
</#if>
<div id="${pageId}" class="page-grid page-grid-reset-password-request-history">
	<div class="head">
		<div class="search">
			<#include "include/page_obj_searchform.html.ftl">
		</div>
		<div class="operation">
		</div>
	</div>
	<div class="content">
		<table id="${pageId}-table" width="100%" class="hover stripe">
		</table>
	</div>
	<div class="foot">
		<div class="pagination-wrapper">
			<div id="${pageId}-pagination" class="pagination"></div>
		</div>
	</div>
</div>
<#if !isAjaxRequest>
</div>
</#if>
<#include "include/page_js_obj.ftl">
<#include "include/page_obj_searchform_js.ftl">
<#include "include/page_obj_pagination.ftl">
<#include "include/page_obj_grid.ftl">
<script type="text/javascript">
(function(po)
{
	po.url = function(action)
	{
		return "${contextPath}/resetPasswordRequestHistory/" + action;
	};
	
	po.buildTableColumValueOption = function(title, data)
	{
		var option =
		{
			title : title,
			data : data,
			render: function(data, type, row, meta)
			{
				return data;
			},
			defaultContent: "",
		};
		
		return option;
	};
	
	var tableColumns = [
		po.buildTableColumValueOption("<@spring.message code='resetPasswordRequestHistory.time' />", "resetPasswordRequest.time"),
		po.buildTableColumValueOption("<@spring.message code='resetPasswordRequestHistory.principal' />", "resetPasswordRequest.principal"),
		po.buildTableColumValueOption("<@spring.message code='resetPasswordRequestHistory.username' />", "resetPasswordRequest.user.name"),
		po.buildTableColumValueOption("<@spring.message code='resetPasswordRequestHistory.effectiveTime' />", "effectiveTime"),
	];
	
	po.initPagination();
	
	var tableSettings = po.buildDataTableSettingsAjax(tableColumns, po.url("pagingQueryData"));
	tableSettings.order=[[1,"desc"]];
	po.initDataTable(tableSettings);
})
(${pageId});
</script>
</body>
</html>

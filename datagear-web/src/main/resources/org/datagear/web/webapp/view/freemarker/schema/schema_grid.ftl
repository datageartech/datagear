<#include "../include/import_global.ftl">
<#include "../include/html_doctype.ftl">
<#--
titleMessageKey 标题标签I18N关键字，不允许null
selectonly 是否选择操作，允许为null
-->
<#assign selectonly=(selectonly!false)>
<html>
<head>
<#include "../include/html_head.ftl">
<title><#include "../include/html_title_app_name.ftl"><@spring.message code='${titleMessageKey}' /></title>
</head>
<body class="fill-parent">
<#if !isAjaxRequest>
<div class="fill-parent">
</#if>
<div id="${pageId}" class="page-grid page-grid-hidden-foot page-grid-schema">
	<div class="head">
		<div class="search">
			<#include "../include/page_obj_searchform.html.ftl">
		</div>
		<div class="operation">
			<#if selectonly>
				<input name="confirmButton" type="button" class="recommended" value="<@spring.message code='confirm' />" />
				<input name="viewButton" type="button" value="<@spring.message code='view' />" />
			<#else>
				<input name="addButton" type="button" value="<@spring.message code='add' />" />
				<input name="editButton" type="button" value="<@spring.message code='edit' />" />
				<input name="viewButton" type="button" value="<@spring.message code='view' />" />
				<input name="deleteButton" type="button" value="<@spring.message code='delete' />" />
			</#if>
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
<#include "../include/page_js_obj.ftl">
<#include "../include/page_obj_searchform_js.ftl">
<#include "../include/page_obj_grid.ftl">
<script type="text/javascript">
(function(po)
{
	$.initButtons(po.element(".operation"));
	
	po.url = function(action)
	{
		return "${contextPath}/schema/" + action;
	};
	
	<#if !selectonly>
	po.element("input[name=addButton]").click(function()
	{
		po.open(po.url("add"),
		{
			pageParam :
			{
				afterSave : function()
				{
					po.refresh();
				}
			}
		});
	});
	
	po.element("input[name=editButton]").click(function()
	{
		po.executeOnSelect(function(row)
		{
			var data = {"id" : row.id};
			
			po.open(po.url("edit"),
			{
				data : data,
				pageParam :
				{
					afterSave : function()
					{
						po.refresh();
					}
				}
			});
		});
	});
	
	po.element("input[name=deleteButton]").click(
	function()
	{
		po.executeOnSelects(function(rows)
		{
			po.confirm("<@spring.message code='confirmDelete' />",
			{
				"confirm" : function()
				{
					var data = $.getPropertyParamString(rows, "id");
					
					$.post(po.url("delete"), data, function()
					{
						po.refresh();
					});
				}
			});
		});
	});
	</#if>
	
	po.element("input[name=viewButton]").click(function()
	{
		po.executeOnSelect(function(row)
		{
			var data = {"id" : row.id};
			
			po.open(po.url("view"),
			{
				data : data
			});
		});
	});
	
	<#if selectonly>
	po.element("input[name=confirmButton]").click(function()
	{
		po.executeOnSelect(function(row)
		{
			var close = po.pageParamCall("submit", row);
			
			//单选默认关闭
			if(close == undefined)
				close = true;
			
			if(close)
				po.close();
		});
	});
	</#if>
	
	var tableColumns = [
		$.buildDataTablesColumnSimpleOption("<@spring.message code='id' />", "id", true),
		$.buildDataTablesColumnSimpleOption("<@spring.message code='schema.title' />", "title"),
		$.buildDataTablesColumnSimpleOption("<@spring.message code='schema.url' />", "url"),
		$.buildDataTablesColumnSimpleOption("<@spring.message code='schema.user' />", "user"),
		$.buildDataTablesColumnSimpleOption("<@spring.message code='schema.createUser' />", "createUser.nameLabel"),
		$.buildDataTablesColumnSimpleOption("<@spring.message code='schema.createTime' />", "createTime")
	];
	var tableSettings = po.buildDataTableSettingsAjax(tableColumns, po.url("queryData"));
	po.initDataTable(tableSettings);
})
(${pageId});
</script>
</body>
</html>

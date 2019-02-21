<#include "../include/import_global.ftl">
<#include "../include/html_doctype.ftl">
<#--
titleMessageKey 标题标签I18N关键字，不允许null
selectonly 是否选择操作，允许为null
-->
<#assign selectonly=(selectonly!false)>
<html style="height:100%;">
<head>
<#include "../include/html_head.ftl">
<title><#include "../include/html_title_app_name.ftl"><@spring.message code='${titleMessageKey}' /></title>
</head>
<body style="height:100%;">
<#if !isAjaxRequest>
<div style="height:99%;">
</#if>
<div id="${pageId}" class="page-grid page-grid-hidden-foot page-grid-user">
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
		return "${contextPath}/user/" + action;
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
	
	<#if !selectonly>
		po.element("input[name=deleteButton]").click(
		function()
		{
			po.executeOnSelects(function(rows)
			{
				po.confirm("<@spring.message code='user.confirmDelete' />",
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
	
	po.buildTableColumValueOption = function(title, data, hidden)
	{
		var option =
		{
			title : title,
			data : data,
			visible : !hidden,
			render: function(data, type, row, meta)
			{
				return $.escapeHtml(data);
			},
			defaultContent: "",
		};
		
		return option;
	};
	
	var columnAdmin = po.buildTableColumValueOption("<@spring.message code='user.admin' />", "admin");
	columnAdmin.render = function(data, type, row, meta)
	{
		if(data == true)
			data = "<@spring.message code='yes' />";
		else
			data = "<@spring.message code='no' />";
		
		return data;
	};
	
	var tableColumns = [
		po.buildTableColumValueOption("<@spring.message code='user.user.id' />", "id", true),
		po.buildTableColumValueOption("<@spring.message code='user.name' />", "name"),
		po.buildTableColumValueOption("<@spring.message code='user.realName' />", "realName"),
		po.buildTableColumValueOption("<@spring.message code='user.email' />", "email"),
		columnAdmin,
		po.buildTableColumValueOption("<@spring.message code='user.createTime' />", "createTime")
	];
	var tableSettings = po.buildDataTableSettingsAjax(tableColumns, po.url("queryData"));
	po.initDataTable(tableSettings);
})
(${pageId});
</script>
</body>
</html>

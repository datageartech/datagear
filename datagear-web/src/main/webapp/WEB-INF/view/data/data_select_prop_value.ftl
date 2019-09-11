<#include "../include/import_global.ftl">
<#include "../include/html_doctype.ftl">
<#--
Schema schema 数据库，不允许为null
Model model 模型，不允许为null
Object data 初始数据，允许null
String propertyPath 属性名称，不允许null
String titleDisplayName 页面展示名称，默认为""
List PropertyPathDisplayName conditionSource 可用的查询条件列表，不允许为null
boolean isMultipleSelect 是否多选，默认为false
-->
<#assign titleDisplayName=(titleDisplayName!'')>
<#assign isMultipleSelect=(isMultipleSelect!false)>
<html>
<head>
<#include "../include/html_head.ftl">
<title>
	<#include "../include/html_title_app_name.ftl">
	<@spring.message code='select' /><@spring.message code='titleSeparator' />
	${titleDisplayName?html}
</title>
</head>
<body class="fill-parent">
<#if !isAjaxRequest>
<div class="fill-parent">
</#if>
<div id="${pageId}" class="page-grid page-grid-spv">
	<div class="head">
		<div class="search">
			<#include "include/data_page_obj_searchform_html.ftl">
		</div>
		<div class="operation">
			<input name="confirmButton" type="button" class="recommended" value="<@spring.message code='confirm' />" />
			<input name="addButton" type="button" value="<@spring.message code='add' />" />
			<input name="editButton" type="button" value="<@spring.message code='edit' />" />
			<input name="viewButton" type="button" value="<@spring.message code='view' />" />
		</div>
	</div>
	<div class="content">
		<table id="${pageId}-table" style="width:100%;" class="hover stripe">
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
<#include "include/data_page_obj.ftl">
<#include "include/data_page_obj_searchform_js.ftl">
<#include "../include/page_obj_pagination.ftl">
<#include "include/data_page_obj_grid.ftl">
<#include "../include/page_obj_data_permission.ftl">
<#include "../include/page_obj_data_permission_ds_table.ftl">
<script type="text/javascript">
(function(po)
{
	po.data = ($.unref(<@writeJson var=data />) || {});
	po.propertyPath = "${propertyPath?js_string}";
	po.isMultipleSelect = ${isMultipleSelect?c};
	po.conditionSource = $.unref(<@writeJson var=conditionSource />);
	
	$.initButtons(po.element(".operation"));
	
	if(!po.canEditTableData(${schema.dataPermission}))
	{
		po.element("input[name=addButton]").attr("disabled", "disabled").hide();
		po.element("input[name=editButton]").attr("disabled", "disabled").hide();
	}
	
	if(!po.canReadTableData(${schema.dataPermission}))
		po.element("input[name=viewButton]").attr("disabled", "disabled").hide();
	
	po.onModel(function(model)
	{
		var propertyInfo = $.model.getTailPropertyInfo(model, po.propertyPath);
		var property = propertyInfo.property;
		var propertyModel = property.model;
		var propertyModelTableName = $.model.featureTableName(propertyModel);
		
		po.mappedByWith = $.model.findMappedByWith(property);

		po.dataTableAjaxParamParent = po.dataTableAjaxParam;
		po.dataTableAjaxParam = function()
		{
			var param = po.dataTableAjaxParamParent();
			
			$.extend(param, 
			{
				"data" : po.data,
				"propertyPath" : po.propertyPath
			});
			
			return param;
		};
		
		po.element("input[name=confirmButton]").click(function()
		{
			if(po.isMultipleSelect)
			{
				po.executeOnSelects(function(rows)
				{
					var close = po.pageParamCall("submit", rows);
					
					if(close == undefined)
						close = true;
					
					if(close && !$.isDialogPinned($.getInDialog(po.element())))
						po.close();
				});
			}
			else
			{
				po.executeOnSelect(function(row)
				{
					var close = po.pageParamCall("submit", row);
					
					if(close == undefined)
						close = true;
					
					if(close && !$.isDialogPinned($.getInDialog(po.element())))
						po.close();
				});
			}
		});
		
		po.element("input[name=addButton]").click(function()
		{
			var options =
			{
				"data" : { "ignorePropertyName" : $.model.findMappedByWith(property) },
				"pageParam" :
				{
					"afterSave" : function(data)
					{
						var close = po.pageParamCall("submit", data);
							
						//单选默认关闭，多选默认不关闭
						if(close == undefined)
							close = (po.isMultipleSelect ? false : true);
						
						if(close)
							po.close();
					}
				}
			}
			
			po.open(po.url(propertyModelTableName, "add"), options);
		});
		
		po.element("input[name=editButton]").click(function()
		{
			po.executeOnSelect(function(row)
			{
				var data = {"data" : row};
				
				po.open(po.url(propertyModelTableName, "edit"),
				{
					data : data
				});
			});
		});
		
		po.element("input[name=viewButton]").click(function()
		{
			po.executeOnSelect(function(row)
			{
				var data = {"data" : row};
				
				po.open(po.url(propertyModelTableName, "view"),
				{
					data : data
				});
			});
		});
		
		po.conditionAutocompleteSource = $.buildSearchConditionAutocompleteSource(po.conditionSource);
		po.initConditionPanel();
		po.initPagination();
		po.initModelDataTableAjax(po.url("selectPropValueData"), propertyModel);
		po.bindResizeDataTable();
	});
})
(${pageId});
</script>
</body>
</html>

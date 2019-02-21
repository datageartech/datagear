<#include "../include/import_global.ftl">
<#include "../include/html_doctype.ftl">
<#--
Schema schema 数据库，不允许为null
Model model 模型，不允许为null
Object data 初始数据，允许null
String propertyPath 属性名称，不允许null
Object propertyValue 初始属性值，可用于设置初始表格数据，允许为null
boolean isClientPageData 初始数据是否是客户端数据，默认为false
String titleOperationMessageKey 标题操作标签I18N关键字，不允许null
String titleDisplayName 页面展示名称，默认为""
boolean readonly 是否只读操作，默认为false
boolean isPrivatePropertyModel 是否是私有属性，不允许为null
List PropertyPathDisplayName conditionSource 可用的查询条件列表，isClientPageData为false时不允许为null
-->
<#assign isClientPageData=(isClientPageData!false)>
<#assign titleDisplayName=(titleDisplayName!'')>
<#assign readonly=(readonly!false)>
<#assign isAllowEditGrid=(isPrivatePropertyModel && !readonly)>
<html style="height:100%;">
<head>
<#include "../include/html_head.ftl">
<title>
	<#include "../include/html_title_app_name.ftl">
	<@spring.message code='${titleOperationMessageKey}' />
	<@spring.message code='titleSeparator' />
	${titleDisplayName?html}
</title>
</head>
<body style="height:100%;">
<#if !isAjaxRequest>
<div style="height:99%;">
</#if>
<div id="${pageId}" class="page-grid page-grid-empv">
	<div class="head">
		<div class="search">
			<#if !isClientPageData>
			<#include "include/data_page_obj_searchform_html.ftl">
			</#if>
		</div>
		<div class="operation">
			<#if readonly>
				<input name="viewButton" type="button" value="<@spring.message code='view' />" />
			<#else>
				<#if isPrivatePropertyModel>
				<input name="addButton" type="button" value="<@spring.message code='add' />" />
				<input name="editButton" type="button" value="<@spring.message code='edit' />" />
				<#else>
				<input name="selectButton" type="button" class="recommended" value="<@spring.message code='select' />" />
				</#if>
				<input name="viewButton" type="button" value="<@spring.message code='view' />" />
				<input name="deleteButton" type="button" value="<@spring.message code='delete' />" />
			</#if>
		</div>
	</div>
	<div class="content">
		<table id="${pageId}-table" style="width:100%;" class="hover stripe">
		</table>
	</div>
	<div class="foot foot-edit-grid">
		<#if isAllowEditGrid>
		<#include "include/data_page_obj_edit_grid_html.ftl">
		</#if>
		<div class="pagination-wrapper">
			<div id="${pageId}-pagination" class="pagination"></div>
		</div>
	</div>
</div>
<#if !isAjaxRequest>
</div>
</#if>
<#include "include/data_page_obj.ftl">
<#if !isClientPageData>
<#include "include/data_page_obj_searchform_js.ftl">
<#include "../include/page_obj_pagination.ftl">
</#if>
<#include "include/data_page_obj_grid.ftl">
<#if isAllowEditGrid>
<#include "include/data_page_obj_edit_grid_js.ftl">
</#if>
<script type="text/javascript">
(function(po)
{
	po.data = ($.unref(<@writeJson var=data />) || {});
	po.propertyPath = "${propertyPath?js_string}";
	po.propertyValue = ($.unref(<@writeJson var=propertyValue />) || $.model.propertyPathValue(po.data, po.propertyPath));
	po.readonly = ${readonly?c};
	po.isClientPageData = ${isClientPageData?c};
	
	<#if !isClientPageData>
	po.conditionSource = $.unref(<@writeJson var=conditionSource />);
	</#if>
	
	$.initButtons(po.element(".operation"));
	
	po.buildActionOptions = function(property, propertyConcreteModel, extraRequestParams, extraPageParams)
	{
		var requestParams =
		{
			"data" : po.data,
			"propertyPath" : po.propertyPath,
			"isClientPageData" : po.isClientPageData
		};
		
		if(extraRequestParams)
			$.extend(requestParams, extraRequestParams);
		
		var pageParams = (extraPageParams || {});
		
		var actionParam =
		{
			"data" : requestParams,
			"pageParam" : pageParams
		}
		
		return actionParam;
	};
	
	po.storeGridPropertyValue = function(gridPropertyValue)
	{
		if(gridPropertyValue == undefined)
			gridPropertyValue = po.getRowsData();
		
		$.model.propertyPathValue(po.data, po.propertyPath, gridPropertyValue);
		
		po.pageParamCall("submit", gridPropertyValue);
	};
	
	<#if isAllowEditGrid>
	po.editGridFormPage.dpvgSuperBuildPropertyActionOptions = po.editGridFormPage.buildPropertyActionOptions;
	po.editGridFormPage.buildPropertyActionOptions = function(property, propertyModel, propertyValue, extraRequestParams, extraPageParams)
	{
		var actionParam = po.editGridFormPage.dpvgSuperBuildPropertyActionOptions(property, propertyModel, propertyValue, extraRequestParams,
				extraPageParams);
		
		if(po.editGridFormPage.dpvgData == null)
		{
			po.editGridFormPage.dpvgData = $.deepClone(po.data);
			if(po.editGridFormPage.dpvgData == null)
				po.editGridFormPage.dpvgData = {};
		}
		
		$.model.propertyPathValue(po.editGridFormPage.dpvgData, po.propertyPath, [ actionParam["data"]["data"] ]);
		var myPropertyPath = $.propertyPath.concatElementIndex(po.propertyPath, 0);
		myPropertyPath = $.propertyPath.concatPropertyName(myPropertyPath, property.name);
		
		actionParam["data"]["data"] = po.editGridFormPage.dpvgData;
		actionParam["data"]["propertyPath"] = myPropertyPath;
		
		return actionParam;
	};
	
	po.superBuildEditCellFetchPropertyValuessAjaxOptions = po.buildEditCellFetchPropertyValuessAjaxOptions;
	po.buildEditCellFetchPropertyValuessAjaxOptions = function(dataTable, indexes, focus, propertyIndexesMap, data,
			needFetchRows, needFetchRowDatas, needFetchPropertyNamess)
	{
		var options = po.superBuildEditCellFetchPropertyValuessAjaxOptions(dataTable, indexes, focus, propertyIndexesMap, data,
				needFetchRows, needFetchRowDatas, needFetchPropertyNamess);
		
		options.url = po.url("getPropertyPropertyValuess");
		options.data =
		{
			"data" : po.data,
			"propertyPath" : po.propertyPath,
			"propertyValues" : needFetchRowDatas,
			"propertyPropertyNamess" : needFetchPropertyNamess
		};
		
		return options;
	};
	
	po.afterSaveClientEditCell = function(editDataTable, editTableData)
	{
		po.storeGridPropertyValue();
	};
	
	po.dpvgSuperBuildAjaxSaveEditCellOptions = po.buildAjaxSaveEditCellOptions;
	po.buildAjaxSaveEditCellOptions = function(editDataTable, modifiedCells, addRows, deleteRows)
	{
		var options = po.dpvgSuperBuildAjaxSaveEditCellOptions(editDataTable, modifiedCells, addRows, deleteRows);
		
		options.url = po.url("saveMultiplePropertyValueElementss");
		
		options.data["data"] = po.data;
		options.data["propertyPath"] = po.propertyPath;
		
		return options;
	};
	</#if>
	
	po.onModel(function(model)
	{
		var propertyInfo = $.model.getTailPropertyInfoConcrete(model, po.propertyPath);
		var property = propertyInfo.property;
		var propertyModel = propertyInfo.model;
		
		po.mappedByWith = $.model.findMappedByWith(property, propertyModel);
		
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
		
		po.element("input[name=viewButton]").click(function()
		{
			po.executeOnSelect(function(row, index)
			{
				var options = undefined;
				
				if(po.isClientPageData)
				{
					options = po.buildActionOptions(property, propertyModel,
							{
								"propertyPath" : $.propertyPath.concatElementIndex(po.propertyPath, index)
							});
					
					po.open(po.url("viewMultiplePropValueElement"), options);
				}
				else
				{
					var originalPropertyValue = $.model.propertyPathValue(po.data, po.propertyPath);
					
					$.model.propertyPathValue(po.data, po.propertyPath, [ row ]);
					var propertyPath = $.propertyPath.concatElementIndex(po.propertyPath, 0);
					
					options = po.buildActionOptions(property, propertyModel,
							{
								"propertyPath" : propertyPath
							});
					
					po.open(po.url("viewMultiplePropValueElement"), options);
					
					$.model.propertyPathValue(po.data, po.propertyPath, originalPropertyValue);
				}
			});
		});
		
		<#if !readonly>
			po.addMultiplePropValueElement = function()
			{
				var url = undefined;
				var options = undefined;
				
				if(po.isClientPageData)
				{
					url = po.url("addMultiplePropValueElement");
					
					var index = po.table().DataTable().rows().data().length;
					
					options = po.buildActionOptions(property, propertyModel,
							{
								"propertyPath" : $.propertyPath.concatElementIndex(po.propertyPath, index)
							},
							{
								"submit" : function(propValueElement)
								{
									po.addRowData(propValueElement);
									po.storeGridPropertyValue();
									
									$.tipSuccess("<@spring.message code='haveAdd' />");
								}
							});
				}
				else
				{
					url = po.url("", "addMultiplePropValueElement", "batchSet=true");
					
					options = po.buildActionOptions(property, propertyModel,
							{
								"propertyPath" : $.propertyPath.concatElementIndex(po.propertyPath, 0)
							},
							null);
				}
				
				options.pinTitleButton = true;
				
				po.open(url, options);
			};
			
			<#if isPrivatePropertyModel>
				po.element("input[name=addButton]").click(function()
				{
					po.addMultiplePropValueElement();
				});
			<#else>
				po.element("input[name=selectButton]").click(function()
				{
					var options = po.buildActionOptions(property, propertyModel, null, 
							{
								"submit" : function(rows)
								{
									if(po.isClientPageData)
									{
										po.addRowData(rows);
										po.storeGridPropertyValue();
										
										$.tipSuccess("<@spring.message code='haveAdd' />");
									}
									else
									{
										var param = { "data" : po.data, "propertyPath" : po.propertyPath, "propValueElements" : rows };
										
										$.post(po.url("saveAddMultiplePropValueElements"), param, function()
										{
											po.refresh();
										});
									}
								}
							});
		
					$.setGridPageHeightOption(options);
					options.pinTitleButton = true;
					po.open(po.url("selectPropValue")+"?multiple", options);
				});
			</#if>
			
			po.element("input[name=editButton]").click(function()
			{
				po.executeOnSelect(function(row, index)
				{
					var options = undefined;
					
					if(po.isClientPageData)
					{
						options = po.buildActionOptions(property, propertyModel,
								{
									"propertyPath" : $.propertyPath.concatElementIndex(po.propertyPath, index)
								},
								{
									"submit" : function(propValueElement)
									{
										po.setRowData(index, propValueElement);
										po.storeGridPropertyValue();
									}
								});
						
						options.pinTitleButton = true;
						
						po.open(po.url("editMultiplePropValueElement"), options);
					}
					else
					{
						var originalPropertyValue = $.model.propertyPathValue(po.data, po.propertyPath);
						
						$.model.propertyPathValue(po.data, po.propertyPath, [ row ]);
						var propertyPath = $.propertyPath.concatElementIndex(po.propertyPath, 0);
						
						options = po.buildActionOptions(property, propertyModel,
								{
									"propertyPath" : propertyPath
								});
						
						options.pinTitleButton = true;
						
						po.open(po.url("editMultiplePropValueElement"), options);
						
						$.model.propertyPathValue(po.data, po.propertyPath, originalPropertyValue);
					}
				});
			});
			
			po.element("input[name=deleteButton]").click(
			function()
			{
				po.executeOnSelects(function(rows, indexes)
				{
					<#assign messageArgs=['"+rows.length+"'] />
					po.confirm("<@spring.messageArgs code='data.confirmDelete' args=messageArgs />",
					{
						"confirm" : function()
						{
							if(po.isClientPageData)
							{
								po.deleteRow(indexes);
								po.storeGridPropertyValue();
							}
							else
							{
								var options = po.buildActionOptions(property, propertyModel, {"propValueElements" : rows}, null);
								
								po.ajaxSubmitForHandleDuplication("deleteMultiplePropValueElements", options.data, "<@spring.message code='delete.continueIgnoreDuplicationTemplate' />",
								{
									"success" : function()
									{
										po.refresh();
									}
								});
							}
						}
					});
				});
			});
		</#if>
		
		<#if isClientPageData>
		po.initModelDataTableLocal(propertyModel, $.model.propertyPathValue(po.data, po.propertyPath), po.mappedByWith);
		<#else>
		po.conditionAutocompleteSource = $.buildSearchConditionAutocompleteSource(po.conditionSource);
		po.initConditionPanel();
		po.initPagination();
		po.initModelDataTableAjax(po.url("queryMultiplePropValueData"), propertyModel, po.mappedByWith);
		</#if>
		
		po.bindResizeDataTable();
		
		<#if isAllowEditGrid>
		po.initEditGrid(propertyModel, po.mappedByWith);
		</#if>
	});
})
(${pageId});
</script>
</body>
</html>

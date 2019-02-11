<%--
/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */
--%>
<%@ page import="java.sql.NClob"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="org.datagear.web.util.WebUtils"%>
<%@ page import="org.datagear.web.convert.AbstractDataConverter" %>
<%--
编辑表格功能JS片段。

依赖：
page_js_obj.jsp
page_obj_grid.jsp
data_page_obj_edit_grid_html.jsp

变量：

--%>
<%
//在表格页面中内嵌一个用于编辑表格的表单页面，并使用它来构建单元格编辑面板，重用代码
String gridPageId = WebUtils.getPageId(request);
String editGridFormPageId = (String)request.getAttribute("editGridFormPageId");
WebUtils.setPageId(request, editGridFormPageId);
%>
<%@ include file="../include/data_page_obj.jsp" %>
<%@ include file="../include/data_page_obj_form.jsp" %>
<script type="text/javascript">
(function(po)
{
	//XXX 这里必须添加handle设置，不然元素的按键、鼠标事件都会无效
	po.element().draggable({ handle : po.element(".form-panel-dragger") });
	po.element().hide();
	po.formLabels.submit = "<fmt:message key='confirm' />";
	
	//由后面设置
	po.gridPage = undefined;
	
	//由下面的showEditCellPanel设置
	po.currentDataTable = undefined;
	po.currentCellIndexes = undefined;
	po.currentPropertyIndexesMap = undefined;
	
	po.superPropertySubmitHandler = po.propertySubmitHandler;
	po.propertySubmitHandler = function(property, propertyModel, propValue)
	{
		po.superPropertySubmitHandler(property, propertyModel, propValue);
		
		if(po.isSubmitWhenPropertySubmit)
			po.form().modelform("submit");
	};
	
	po.superPropertyDataTableAjaxSuccess = po.propertyDataTableAjaxSuccess;
	po.propertyDataTableAjaxSuccess = function(property, propertyConcreteModel, propertyValue, propertyValuePagingData)
	{
		po.superPropertyDataTableAjaxSuccess(property, propertyConcreteModel, propertyValue, propertyValuePagingData);
		
		if(!$.model.isMultipleProperty(property))
			return;
		
		var singleRow = $.getDataTableRowIfSingle(po.currentCellIndexes);
		
		//更新服务端数据行的多元属性值数目
		if(singleRow != null && !po.gridPage.isClientDataRow(po.currentDataTable, singleRow))
		{
			var originalData = po.gridPage.originalRowData(po.currentDataTable, singleRow);
			var originalPropertyValue = $.model.propertyValue(originalData, property.name);
			
			if(propertyValuePagingData.total == $.model.getSizeOnlyCollectionSize(originalPropertyValue))
				return;
			
			var myColumn = $.getDataTableColumn(po.currentDataTable.settings(), property.name);
			var myCell = po.currentDataTable.cell({ "row" : singleRow, "column" : myColumn });
			var $myCell = $(myCell.node());
			
			var $originalSize = $myCell.find(".original-size");
			
			if($originalSize.length > 0)
				$originalSize.html(propertyValuePagingData.total);
			else
			{
				var $valueWrapper = $myCell.find(".value-wrapper");
				$valueWrapper.html(propertyValuePagingData.total);
			}
			
			if(originalPropertyValue == null || $.model.isSizeOnlyCollection(originalPropertyValue))
			{
				$.model.propertyValue(originalData, property.name, $.model.toSizeOnlyCollection(propertyValuePagingData.total));
				
				//原表格的单元格也要更新
				var dataTable = po.gridPage.table().DataTable();
				var cell = dataTable.cell({ "row" : singleRow, "column" : myColumn });
				cell.data($.model.toSizeOnlyCollection(propertyValuePagingData.total));
			}
			
			var tableRowData = po.currentDataTable.row(singleRow).data();
			var tableRowPropertyValue = $.model.propertyValue(tableRowData, property.name);
			
			if(tableRowPropertyValue == null || $.model.isSizeOnlyCollection(tableRowPropertyValue))
				$.model.propertyValue(tableRowData, property.name, $.model.toSizeOnlyCollection(propertyValuePagingData.total));
		}
	}
	
	po.isPropertyActionClientSubmit = function(property, propertyConcreteModel)
	{
		return true;
	};
	
	po.superBuildPropertyActionOptions = po.buildPropertyActionOptions;
	po.buildPropertyActionOptions = function(property, propertyModel, propertyValue, extraRequestParams, extraPageParams)
	{
		var actionParam = po.superBuildPropertyActionOptions(property, propertyModel, propertyValue,
								extraRequestParams, extraPageParams);
		
		var singleRow = $.getDataTableRowIfSingle(po.currentCellIndexes);
		
		var isClientPageData = true;
		
		//服务端数据
		if(singleRow != null && !po.gridPage.isClientDataRow(po.currentDataTable, singleRow))
		{
			var data = po.gridPage.originalRowData(po.currentDataTable, singleRow);
			data = $.deepClone(data);
			actionParam["data"]["data"] = data;
			
			var myColumn = $.getDataTableColumn(po.currentDataTable.settings(), property.name);
			var myCell = po.currentDataTable.cell({ "row" : singleRow, "column" : myColumn });
			var $myCell = $(myCell.node());
			
			var isModified = po.gridPage.isModifiedCell($myCell);
			
			//单元格没有任何修改，则直接采用服务端数据模式
			if(!isModified)
				isClientPageData = false;
			else
			{
				//集合属性值单元格修改了（多选编辑后），那么仅开启客户端数据模式，仅可添加、编辑、删除客户端集合属性值元素
				if($.model.isMultipleProperty(property))
				{
					$.model.propertyValue(data, property, myCell.data());
					isClientPageData = true;
				}
				else
				{
					actionParam["data"]["propertyValue"] = myCell.data();
					var propertyValue = $.model.propertyValue(data, property);
					//XXX 此处处理逻辑有缺陷，如果多选编辑了服务端单元属性值对象的集合属性值，然后再单选编辑时，单元属性值对象的集合属性值页面将仅会显示服务端数据
					isClientPageData = !propertyValue;
				}
			}
		}
		
		actionParam["data"]["isClientPageData"] = isClientPageData;
		
		if(!isClientPageData)
		{
			actionParam["pageParam"]["dataTableAjaxSuccess"] = function(propertyValuePagingData)
			{
				po.propertyDataTableAjaxSuccess(property, propertyModel, propertyValue, propertyValuePagingData);
			};
		}
		
		return actionParam;
	}
})
(<%=editGridFormPageId%>);
</script>
<%
WebUtils.setPageId(request, gridPageId);
%>
<script type="text/javascript">
(function(po)
{
	//内嵌的表单页面对象
	po.editGridFormPage = <%=editGridFormPageId%>;
	po.editGridFormPage.gridPage = po;
	//编辑表格对应的模型，会在initEditGrid函数中初始化
	po.editGridModel = undefined;
	
	po.isEnableEditGrid = false;
	//存储行初始数据的映射表，在单元格修改前存储，用于支持恢复等操作
	po.editGridOriginalRowDataMap = {};
	//存储行的指定属性值是否已从服务端加载
	po.editGridFetchedPropertyValueMap = {};
	//是否在单元格选中时编辑单元格，键盘快速导航时通常不需要打开编辑单元格面板
	po.editCellOnSelect = true;
	
	po.resetEditGridCache = function()
	{
		po.editGridOriginalRowDataMap = {};
		po.editGridFetchedPropertyValueMap = {};
		po.editCellOnSelect = true;
	};
	
	/**
	 * 获取行初始数据对象。
	 * @param editDataTable 必选，DataTable的API对象
	 * @param rowIndex 必选，行索引
	 */
	po.originalRowData = function(editDataTable, rowIndex)
	{
		var rowData = po.editGridOriginalRowDataMap[rowIndex];
		
		if(!rowData)
		{
			rowData = editDataTable.row(rowIndex).data();
		}
		
		return rowData;
	};
	
	//获取/设置行的指定属性值是否已从服务端加载
	po.fetchedPropertyValue = function(row, propertyName, fetched)
	{
		if(fetched == undefined)
		{
			var rowInfo = po.editGridFetchedPropertyValueMap[row];
			if(!rowInfo)
				return false;
			
			return rowInfo[propertyName];
		}
		else
		{
			var rowInfo = (po.editGridFetchedPropertyValueMap[row] || (po.editGridFetchedPropertyValueMap[row] = {}));
			rowInfo[propertyName] = fetched;
		}
	};
	
	po.editTable = function()
	{
		var id = po.pageId +"-edit-table";
		
		var $editTable = po.element("#" + id);
		if($editTable.length == 0)
			$editTable = $("<table id='"+id+"' width='100%' class='hover stripe' tabindex='0'></table>").appendTo(po.element(".content"));
		
		return $editTable;
	};
	
	//获取表格元素的父元素
	po.dataTableParent = function(dataTable)
	{
		var $tableParent = $(dataTable.table().body()).parent().parent();
		return $tableParent;
	};
	
	po.bindEditDataTableEvents = function($table)
	{
		$table.DataTable()
		.on("click", function(event)
		{
			if(po.isEnableEditGrid)
			{
				//阻止冒泡的行选择事件
				event.stopPropagation();
				
				var target = $(event.target);
				
				if(target.is("td"))
				{
					var editDataTable = $(this).DataTable();
					
					po.editCellOnSelect = true;
					
					$.handleCellSelectionForClick(editDataTable, event, target);
				}
			}
		})
		.on("keydown", function(event)
		{
			if(po.isEnableEditGrid)
			{
				var editDataTable = $(this).DataTable();
				
				if(event.keyCode == $.ui.keyCode.ESCAPE)
				{
					po.closeEditCellPanel(editDataTable);
					
					po.editCellOnSelect = false;
				}
				else if(event.keyCode == $.ui.keyCode.ENTER)
				{
					//必须加下面这行代码，不然当打开的编辑面板表单输入框自动焦点时，会触发表单提交事件
					event.preventDefault();
					
					var selectedIndexes = editDataTable.cells(".selected").indexes();
					
					if(selectedIndexes)
						po.editCell(editDataTable, selectedIndexes, true);
				}
				else
				{
					$.handleCellNavigationForKeydown(editDataTable, event);
				}
			}
		})
		.on("select", function(event, editDataTable, type, indexes)
		{
			if(po.isEnableEditGrid)
			{
				if(type == "cell")
				{
					if(po.editCellOnSelect)
						po.editCell(editDataTable, indexes);
				}
			}
		})
		.on("deselect", function(event, editDataTable, type, indexes)
		{
			if(po.isEnableEditGrid)
			{
				if(type == "cell")
				{
					po.closeEditCellPanel(editDataTable, indexes);
				}
			}
		});
	};
	
	po.getEditGridInitDatas = function(dataTable)
	{
		var editTableDatas = $.makeArray(dataTable.data());
		for(var i=0; i<editTableDatas.length; i++)
		{
			editTableDatas[po.TABLE_CHECK_COLUMN_PROPERTY_NAME] = undefined;
		}
		
		return editTableDatas;
	};
	
	po.initEditGridDataTable = function($editTable, dataTable)
	{
		var editTableDatas = po.getEditGridInitDatas(dataTable);
		
		var columns = $.buildDataTablesColumns(po.editGridModel, {"ignorePropertyNames" : po.editGridIgnorePropertyNames});
		var settings = po.buildDataTableSettingsLocal(columns, editTableDatas);
		
		//禁用排序，不然添加行会自动排序，不友好
		settings.ordering = false;
		
		var checkColumn = settings.columns[0];
		checkColumn.render = function(data, type, row, meta)
		{
			var content = po.renderCheckColumn.call(this, data, type, row, meta);
			
			if(data)
			{
				var className = "row-data-state";
				var iconClassName = "ui-icon";
				
				if(data == "add-row")
				{
					iconClassName += " ui-icon-circle-plus";
				}
				else if(data == "delete-row")
				{
					className += " ui-state-error";
					iconClassName += " ui-icon-circle-minus";
				}
				
				content = content + "<div class='"+className+"'><span class='"+iconClassName+"'></span></div>";
			}
			
			return content;
		};
		
		po.initDataTable(settings, $editTable);
		
		po.editGridResizeHandler = po.bindResizeDataTable($editTable, "editTableResizeTimer");
		po.bindEditDataTableEvents($editTable);
	};
	
	po.enableEditGrid = function()
	{
		var $headOperation = po.element(".head .operation");
		
		if(po.element(".edit-grid-button", $headOperation).length == 0)
		{
			po.element(".ui-button", $headOperation).addClass("not-edit-grid-button");
			
			var $buttonWrapper = $("<div class='edit-grid-button-wrapper' style='display:inline-block;' />").appendTo($headOperation);
			$("<button name='editGridAddButton' class='edit-grid-button highlight'><fmt:message key='add' /></button>&nbsp;"
				+"<button name='editGridEditButton' class='edit-grid-button highlight'><fmt:message key='edit' /></button>&nbsp;"
				+"<button name='editGridDeleteButton' class='edit-grid-button highlight'><fmt:message key='delete' /></button>").appendTo($buttonWrapper);
			
			$.initButtons($buttonWrapper);
			
			$buttonWrapper.hide();
			
			po.element("button[name='editGridAddButton']", $buttonWrapper).click(function()
			{
				po.editCellOnSelect = true;
				
				var rowData = $.model.instance(po.editGridModel);
				
				var editDataTable = po.editTable().DataTable();
				var row = editDataTable.row.add(rowData);
				$(row.node()).addClass("add-row");
				row.draw();
				editDataTable.cell(row.index(), 0).data("add-row").draw();
				
				//滚动到底部
				var $editDataTableParent = po.dataTableParent(editDataTable);
				$editDataTableParent.scrollTop($editDataTableParent.prop("scrollHeight"));
			});
			
			po.element("button[name='editGridEditButton']", $buttonWrapper).click(function()
			{
				po.editCellOnSelect = true;
				
				var editDataTable = po.editTable().DataTable();
				var selectedIndexes = editDataTable.cells(".selected").indexes();
				
				if(selectedIndexes)
					po.editCell(editDataTable, selectedIndexes, true);
			});
			
			po.element("button[name='editGridDeleteButton']", $buttonWrapper).click(function()
			{
				var editDataTable = po.editTable().DataTable();
				
				po.closeEditCellPanel(editDataTable);
				po.editCellOnSelect = true;
				
				var selectedAddRows = editDataTable.rows(".selected.add-row");
				var selectedAddRowIndexes = selectedAddRows.indexes();
				if(selectedAddRowIndexes.length > 0)
					selectedAddRows.remove().draw();
				
				var selectedRows = editDataTable.rows(".selected");
				var selectedRowIndexes = selectedRows.indexes();
				
				if(selectedRowIndexes.length > 0)
				{
					selectedRows.deselect();
					
					selectedRows.every(function(rowIndex)
					{
						var $row = $(this.node());
						
						$row.addClass("delete-row");
						editDataTable.cell(rowIndex, 0).data("delete-row");
					});
					
					//统一绘制，效率更高
					editDataTable.cells(selectedRows, 0).draw();
				}
				
				//删除单元格属性值
				if(selectedAddRowIndexes.length <= 0 && selectedRowIndexes.length <= 0)
				{
					var selectedCells = editDataTable.cells(".selected");
					var selectedCellIndexes = selectedCells.indexes();
					
					if(selectedCellIndexes.length > 0)
					{
						var settings = editDataTable.settings();
						var propertyIndexesMap = $.getDataTableCellPropertyIndexesMap(settings, selectedCellIndexes);
						
						po.storeEditCell(editDataTable, propertyIndexesMap, {});
					}
				}
			});
		}
		
		po.element(".head .search").addClass("ui-state-disabled");
		po.element(".foot .pagination").addClass("ui-state-disabled");
		
		var dataTable = po.table().DataTable();
		var dataTableScrollTop = po.dataTableParent(dataTable).prop("scrollTop");
		var $tableContainer = $(dataTable.table().container());
		$tableContainer.hide();
		
		//新建本地模式的DataTable，因为server-side模式的DataTable不能添加行
		var $editTable = po.editTable();
		po.initEditGridDataTable($editTable, dataTable);
		po.dataTableParent($editTable.DataTable()).scrollTop(dataTableScrollTop);
		
		po.resetEditGridCache();
		
		var $editGridOperation = po.element(".edit-grid-operation");
		//保存按钮居中
		$editGridOperation.css("right", (0 - po.element(".button-save", $editGridOperation).outerWidth(true)/2));
		po.element(".edit-grid-operation button").show("fade");
		
		po.element(".ui-button.not-edit-grid-button", $headOperation).hide();
		po.element(".edit-grid-button-wrapper", $headOperation).show("fade", function()
		{
			//防止快速点击复选框导致都显示出来
			if(!po.isEnableEditGrid)
				$(this).hide();
		});
		
		po.isEnableEditGrid = true;
	};
	
	po.disableEditGrid = function()
	{
		var $editTable = po.editTable();
		$editTable.DataTable().destroy();
		$editTable.remove();
		$(window).unbind("resize", po.editGridResizeHandler);
		
		var dataTable = po.table().DataTable();
		var $tableContainer = $(dataTable.table().container());
		$tableContainer.show();
		//不加此行，窗口有resize后列宽不对
		dataTable.columns.adjust();
		
		var $headOperation = po.element(".head .operation");
		
		po.element(".head .search").removeClass("ui-state-disabled");
		po.element(".foot .pagination").removeClass("ui-state-disabled");
		
		po.element(".edit-grid-operation button").hide("fade");
		po.element(".edit-grid-button-wrapper", $headOperation).hide();
		po.element(".ui-button.not-edit-grid-button", $headOperation).show("fade", function()
		{
			//防止快速点击复选框导致都显示出来
			if(po.isEnableEditGrid)
				$(this).hide();
		});
		
		po.isEnableEditGrid = false;
	};
	
	po.isModifiedCell = function($cell)
	{
		return $cell.hasClass("cell-modified");
	};
	
	po.markAsModifiedCell = function($cell)
	{
		if(!$cell.hasClass("cell-modified"))
			$cell.addClass("cell-modified");
		
		var $tip = $(".cell-midified-tip", $cell);
		
		if($tip.length == 0)
			$("<div class='cell-midified-tip ui-state-error'><span class='ui-icon ui-icon-triangle-1-sw' /></div>").appendTo($cell);
	};
	
	po.markAsUnmodifiedCell = function($cell)
	{
		if($cell.hasClass("cell-modified"))
			$cell.removeClass("cell-modified");
		
		var $tip = $(".cell-midified-tip", $cell);
		if($tip.length > 0)
			$tip.remove();
	};
	
	po.isClientDataRow = function(editDataTable, row)
	{
		if(po.isClientPageData)
			return true;
		
		var $row = $(editDataTable.row(row).node());
		return $row.hasClass("add-row");
	};
	
	//判断单元格是否需要从服务端加载数据
	po.needFetchPropertyValue = function(editDataTable, cellIndex, property, propertyValue)
	{
		if(propertyValue == null)
			return false;
		
		var re = !po.isSinglePrimitivePropertyValueFullyFetched(po.editGridModel, property, propertyValue);
		
		if(re && po.isClientDataRow(editDataTable, cellIndex.row))
			re =  false;
		
		if(re && po.fetchedPropertyValue(cellIndex.row, property.name))
			re = false;
		
		if(re)
		{
			var $cell = $(editDataTable.cell(cellIndex).node());
			
			if(po.isModifiedCell($cell))
				re = false;
		}
		
		return re;
	};
	
	//获取编辑表单初始数据
	po.getEditCellFormInitData = function(editDataTable, indexes, propertyIndexesMap, needFetchRowDataMap, needFetchPropertyNamesMap)
	{
		var data = {};
		
		var model = po.editGridModel;
		
		for(var pi in propertyIndexesMap)
		{
			var property = $.model.getProperty(model, parseInt(pi));
			
			var pindexes = propertyIndexesMap[pi];
			var pindex0 = pindexes[0];
			var propertyValue0 = editDataTable.cell(pindex0).data();
			
			//仅从后台获取选中一行的属性值
			if(pindexes.length == 1 && po.needFetchPropertyValue(editDataTable, pindex0, property, propertyValue0))
			{
				var pindex0Row = pindex0.row;
				
				if(!needFetchRowDataMap[pindex0Row])
					needFetchRowDataMap[pindex0Row] = po.originalRowData(editDataTable, pindex0Row);
				
				var propertyNames = (needFetchPropertyNamesMap[pindex0Row] || (needFetchPropertyNamesMap[pindex0Row] = []));
				propertyNames.push(property.name);
			}
			else
			{
				var allColumnValueEquals = true;
				
				for(var i=1; i<pindexes.length; i++)
				{
					var pindex = pindexes[i];
					var propertyValue = editDataTable.cell(pindex).data();
					
					if(!$.deepEquals(propertyValue, propertyValue0))
					{
						allColumnValueEquals = false;
						break;
					}
				}
				
				if(allColumnValueEquals)
					$.model.propertyValue(data, property.name, propertyValue0);
			}
		}
		
		return data;
	};
	
	//构建编辑表格从后台获取单元格属性值的ajax选项。
	po.buildEditCellFetchPropertyValuessAjaxOptions = function(editDataTable, indexes, focus, propertyIndexesMap, data,
			needFetchRows, needFetchRowDatas, needFetchPropertyNamess)
	{
		var options =
		{
			"type" : "POST",
			"url" : po.url("getPropertyValuess"),
			"data" : { "datas" : needFetchRowDatas, "propertyNamess" : needFetchPropertyNamess },
			"success" : function(fetchedPropertyValuess)
			{
				if(fetchedPropertyValuess)
				{
					var settings = editDataTable.settings();
					
					for(var i=0; i<needFetchRows.length; i++)
					{
						var needFetchRow = parseInt(needFetchRows[i]);
						var needFetchRowData = needFetchRowDatas[i];
						var needFetchPropertyNames = needFetchPropertyNamess[i];
						var fetchedPropertyValues = fetchedPropertyValuess[i];
						
						if(fetchedPropertyValues)
						{
							for(var j=0; j<needFetchPropertyNames.length; j++)
							{
								var fetchedPropertyValue = fetchedPropertyValues[j];
								
								if(fetchedPropertyValue != null)
								{
									var needFetchPropertyName = needFetchPropertyNames[j];
									
									$.model.propertyValue(data, needFetchPropertyName, fetchedPropertyValue);
									$.model.propertyValue(needFetchRowData, needFetchPropertyName, fetchedPropertyValue);
									
									var myColumn = $.getDataTableColumn(settings, needFetchPropertyName);
									var myCell = editDataTable.cell({ "row" : needFetchRow, "column" : myColumn });
									myCell.data(fetchedPropertyValue);
									
									po.fetchedPropertyValue(needFetchRow, needFetchPropertyName, true);
								}
							}
						}
					}
				}
				
				po.showEditCellPanel(editDataTable, indexes, propertyIndexesMap, data, focus);
			}
		};
		
		return options;
	};
	
	//编辑单元格
	po.editCell = function(editDataTable, indexes, focus)
	{
		var settings = editDataTable.settings();
		var propertyIndexesMap = $.getDataTableCellPropertyIndexesMap(settings, indexes);
		var model = po.editGridModel;
		var needFetchRowDataMap = {};
		var needFetchPropertyNamesMap = {};
		
		var data = po.getEditCellFormInitData(editDataTable, indexes, propertyIndexesMap,
						needFetchRowDataMap, needFetchPropertyNamesMap);
		
		if($.getPropertyCount(needFetchRowDataMap) > 0)
		{
			var sortFunction = function(k0, k1)
			{
				var nk0 = parseInt(k0);
				var nk1 = parseInt(k1);
				
				if(nk0 < nk1)
					return -1;
				else if(nk0 == nk1)
					return 0;
				else
					return 1;
			};
			
			var needFetchRowDataArrayObj = $.getMapKeyValueArray(needFetchRowDataMap, sortFunction);
			var needFetchRows = needFetchRowDataArrayObj.keys;
			var needFetchRowDatas = needFetchRowDataArrayObj.values;
			var needFetchPropertyNamess = $.getMapKeyValueArray(needFetchPropertyNamesMap, sortFunction).values;
			
			var fetchAjaxOptions = po.buildEditCellFetchPropertyValuessAjaxOptions(editDataTable, indexes, focus, propertyIndexesMap, data,
					needFetchRows, needFetchRowDatas, needFetchPropertyNamess);
			$.ajax(fetchAjaxOptions);
		}
		else
			po.showEditCellPanel(editDataTable, indexes, propertyIndexesMap, data, focus);
	};
	
	po.showEditCellPanel = function(editDataTable, indexes, propertyIndexesMap, data, focus)
	{
		if(indexes.length == 0)
			return;
		
		var $table = $(editDataTable.table().node());
		var $tableContainer = $(editDataTable.table().container());
		var $cellNodes = $(editDataTable.cells(indexes).nodes());
		var $editFormCell = $($cellNodes[0]);
		
		$cellNodes.removeClass("cell-edit-form");
		$editFormCell.addClass("cell-edit-form");
		
		var singlePropertyIndex = $.getPropertyNameIfSingle(propertyIndexesMap);
		var isHideFormPage = false;
		
		if(singlePropertyIndex != null)
		{
			var property = $.model.getProperty(po.editGridModel, parseInt(singlePropertyIndex));
			
			isHideFormPage = $.model.isMultipleProperty(property);
			
			if(!isHideFormPage)
			{
				var propertyValue = $.model.propertyValue(data, property.name);
				var propertyModel = $.model.getPropertyModelByValue(property, propertyValue);
				
				isHideFormPage = $.model.isCompositeModel(propertyModel);
			}
		}
		
		po.editGridFormPage.data = data;
		po.editGridFormPage.currentDataTable = editDataTable;
		po.editGridFormPage.currentCellIndexes = indexes;
		po.editGridFormPage.currentPropertyIndexesMap = propertyIndexesMap;
		po.editGridFormPage.isSubmitWhenPropertySubmit = isHideFormPage;
		
		var $formPage = po.editGridFormPage.element();
		var $formPanel = po.editGridFormPage.element(".form-panel");
		
		if($formPage.parent().is("td"))
			po.editGridFormPage.form().modelform("destroy");
		
		//将原单元格内容包裹元素，使原内容可操作
		var $cellValueWrappper = $editFormCell.find("span.value-wrapper");
		if($cellValueWrappper.length == 0)
			$editFormCell.wrapInner("<span class='value-wrapper'></span>");
		
		$formPage.appendTo($editFormCell);
		
		if(isHideFormPage)
			$formPage.hide();
		else
			$formPage.show();
		
		var form = po.editGridFormPage.form();
		
		//只有一个属性，隐藏标签，否则，显示标签
		if(singlePropertyIndex != null)
		{
			$formPanel.css("min-width", $tableContainer.width()/3);
			form.addClass("hide-form-label");
		}
		else
		{
			$formPanel.css("min-width", $tableContainer.width()/2);
			form.removeClass("hide-form-label");
		}
		
		form.modelform(
		{
			model : po.editGridModel,
			data : data,
			renderProperty : function(property, propertyIndex)
			{
				var propertyIndexesMap = po.editGridFormPage.currentPropertyIndexesMap;
				
				return (propertyIndexesMap[propertyIndex] != undefined);
			},
			submit : function()
			{
				var $this = $(this);
				
				var data = $this.modelform("data");
				var propertyIndexesMap = po.editGridFormPage.currentPropertyIndexesMap;
				
				var editDataTable = po.editTable().DataTable();
				
				po.closeEditCellPanel(editDataTable);
				po.storeEditCell(editDataTable, propertyIndexesMap, data);
				
				return false;
			},
			invalidHandler : (isHideFormPage ? undefined : function(){ $formPage.show(); }),
			addSinglePropertyValue : function(property, propertyModel)
			{
				po.editGridFormPage.addSinglePropertyValue(property, propertyModel);
			},
			editSinglePropertyValue : function(property, propertyModel, propertyValue)
			{
				po.editGridFormPage.editSinglePropertyValue(property, propertyModel, propertyValue);
			},
			deleteSinglePropertyValue : function(property, propertyModel, propertyValue)
			{
				po.editGridFormPage.deleteSinglePropertyValue(property, propertyModel, propertyValue);
			},
			selectSinglePropertyValue : function(property, propertyModel, propertyValue)
			{
				po.editGridFormPage.selectSinglePropertyValue(property, propertyModel, propertyValue);
			},
			viewSinglePropertyValue : function(property, propertyModel, propertyValue)
			{
				po.editGridFormPage.viewSinglePropertyValue(property, propertyModel, propertyValue);
			},
			editMultiplePropertyValue : function(property, propertyModel, propertyValue)
			{
				po.editGridFormPage.editMultiplePropertyValue(property, propertyModel, propertyValue);
			},
			viewMultiplePropertyValue : function(property, propertyModel, propertyValue)
			{
				po.editGridFormPage.viewMultiplePropertyValue(property, propertyModel, propertyValue);
			},
			filePropertyUploadURL : "<c:url value='/data/file/upload' />",
			filePropertyDeleteURL : "<c:url value='/data/file/delete' />",
			downloadSinglePropertyValueFile : function(property, propertyModel)
			{
				po.editGridFormPage.downloadSinglePropertyValueFile(property, propertyModel);
			},
			validationRequiredAsAdd : false,
			labels : po.editGridFormPage.formLabels,
			dateFormat : "<c:out value='${sqlDateFormat}' />",
			timestampFormat : "<c:out value='${sqlTimestampFormat}' />",
			timeFormat : "<c:out value='${sqlTimeFormat}' />",
			filePropertyLabelValue : "<c:out value='${filePropertyLabelValue}' />"
		});
		
		if(singlePropertyIndex != null || focus)
		{
			//激活第一个属性
			form.modelform("activeProperty");
		}
		
		$formPanel.position({ my : "left top", at : "left bottom", of : $editFormCell, within : $table});
	};
	
	//关闭编辑面板
	po.closeEditCellPanel = function(editDataTable)
	{
		var $formPage = po.editGridFormPage.element();
		var $formPageParent = $formPage.parent();
		
		if($formPageParent.is("td"))
		{
			$formPageParent.removeClass("cell-edit-form");
			
			$formPage.hide();
			
			if($formPage.hasClass("focus"))
				$formPage.removeClass("focus");
			
			po.editGridFormPage.form().modelform("destroy");
			
			$formPage.appendTo(po.element(".foot"));
			
			var $valueWrapper = $formPageParent.find("span.value-wrapper");
			if($valueWrapper.length > 0)
				$formPageParent.html($valueWrapper.html());
		}
		
		$(editDataTable.table().node()).focus();
	};
	
	//将数据存储至表格
	po.storeEditCell = function(editDataTable, propertyIndexesMap, data)
	{
		var model = po.editGridModel;
		
		var changedCellIndexes = [];
		var changedCellValues = [];
		var changedCellHtmls = [];
		var unchangedCellIndexes = [];
		
		for(var pi in propertyIndexesMap)
		{
			var pindexes = propertyIndexesMap[pi];
			var property = $.model.getProperty(model, parseInt(pi));
			var propertyValue = $.model.propertyValue(data, property.name);
			
			for(var i=0; i<pindexes.length; i++)
			{
				var index = pindexes[i];
				
				var originalRowData = po.originalRowData(editDataTable, index.row);
				var originalCellValue = $.model.propertyValue(originalRowData, property);
				var myPropertyValue = propertyValue;
				
				if($.model.isMultipleProperty(property))
				{
					//只允许集合属性值在初始值的基础上添加，因此当为null时即是恢复为初始值
					if(!myPropertyValue || ($.isArray(myPropertyValue) && myPropertyValue.length == 0))
						myPropertyValue = originalCellValue;
				}
				
				var changed = true;
				
				if(myPropertyValue == originalCellValue)
					changed = false;
				else
				{
					if($.model.isShowableValue(myPropertyValue))
						myPropertyValue = $.model.getShowableRawValue(myPropertyValue);
					
					if(myPropertyValue == originalCellValue)
						changed = false;
					//无原始值但是表单空字符串保存的情况
					else if((originalCellValue == null)
							&& (myPropertyValue == "" || myPropertyValue == null))
						changed = false;
					else
					{
						var ovType = $.type(originalCellValue);
						if((ovType == "object" || ovType == "array") && $.type(myPropertyValue) == ovType)
						{
							var propertyModel = $.model.getPropertyModelByValue(property, originalCellValue);
							
							changed = !$.deepEquals(originalCellValue, myPropertyValue, 
											$.model.findMappedByWith(property, propertyModel));
						}
					}
				}
				
				if(changed)
				{
					changedCellIndexes.push(index);
					
					changedCellValues.push($.deepClone(propertyValue));
					
					//多元属性值单元格显示“[原始元素个数]+[新加元素个数]”
					if($.model.isMultipleProperty(property) && !po.isClientDataRow(editDataTable, index.row) && $.isArray(myPropertyValue))
					{
						var originalLen = $.model.getMultiplePropertyValueLength(originalCellValue);
						var newLen = $.model.getMultiplePropertyValueLength(myPropertyValue);
						
						changedCellHtmls.push("<span class='original-size'>"+originalLen+"</span>+<span class='add-size'>"+newLen+"</span>");
					}
					else
						changedCellHtmls.push(null);
				}
				else
				{
					unchangedCellIndexes.push(index);
				}
			}
		}
		
		for(var i=0; i<changedCellIndexes.length; i++)
			po.updateEditDataTableCellValue(editDataTable, changedCellIndexes[i], changedCellValues[i]);
		
		//统一绘制，效率更高
		editDataTable.cells(changedCellIndexes).draw();
		
		for(var i=0; i<changedCellIndexes.length; i++)
		{
			var $cell = $(editDataTable.cell(changedCellIndexes[i]).node());
			
			if(changedCellHtmls[i] != null)
				$cell.html(changedCellHtmls[i]);
			
			po.markAsModifiedCell($cell);
		}
		
		for(var i=0; i<unchangedCellIndexes.length; i++)
		{
			var $cell = $(editDataTable.cell(unchangedCellIndexes[i]).node());
			po.markAsUnmodifiedCell($cell);
		}
		
		//新值可能会影响单元格宽度，因此需要重设列宽
		if(changedCellIndexes.length > 0)
			editDataTable.columns.adjust();
		
		//保存后的下一次选中单元格操作触发编辑
		po.editCellOnSelect = true;
	};
	
	//更新编辑表格单元格数据
	po.updateEditDataTableCellValue = function(editDataTable, cellIndex, cellValue)
	{
		var rowIndex = cellIndex.row;
		
		if(!po.editGridOriginalRowDataMap[rowIndex])
		{
			var originalRowData = editDataTable.row(rowIndex).data();
			po.editGridOriginalRowDataMap[rowIndex] = originalRowData;
			
			var clonedRowData = $.deepClone(originalRowData);
			editDataTable.row(rowIndex).data(clonedRowData);
		}
		
		//undefined值会使cell.data()语义不符
		editDataTable.cell(cellIndex).data(cellValue == undefined ? null : cellValue);
	};
	
	//恢复表格数据为初始值
	po.restoreEditCell = function(editDataTable, editCells, addRows, deleteRows, confirmCount, confirmCallback, cancelCallback)
	{
		var count = po.getEditCellCount(editDataTable, editCells, addRows, deleteRows);
		
		var _confirmCallback = function()
		{
			po.closeEditCellPanel(editDataTable);
			
			var model = po.editGridModel;
			var settings = editDataTable.settings();
			
			var editCellCount = 0;
			
			editCells.every(function()
			{
				var index = this.index();
				
				var originalRowData = po.originalRowData(editDataTable, index.row);
				
				if(originalRowData)
				{
					var propertyIndex = $.getDataTableCellPropertyIndex(settings, index);
					var property = $.model.getProperty(model, propertyIndex);
					var originalCellValue = $.model.propertyValue(originalRowData, property);
					
					this.data((originalCellValue == undefined ? null : originalCellValue));
				}
				
				po.markAsUnmodifiedCell($(this.node()));
				
				editCellCount++;
			});
			
			//新值可能会影响单元格宽度，因此需要重设列宽
			if(editCellCount > 0)
			{
				//统一绘制，效率更高
				editCells.draw();
				editDataTable.columns.adjust();
			}
			
			//删除新建行
			var addRowsCount = addRows.indexes().length;
			if(addRowsCount > 0)
				addRows.remove().draw();
			
			//恢复删除行
			var deleteRowsCount = deleteRows.indexes().length;
			if(deleteRowsCount > 0)
			{
				deleteRows.every(function(rowIndex)
				{
					var $row = $(this.node());
					
					$row.removeClass("delete-row");
					editDataTable.cell(rowIndex, 0).data("");
				});
				//统一绘制，效率更高
				editDataTable.cells(deleteRows, 0).draw();
			}
			
			if(confirmCallback)
				confirmCallback.call(po, editDataTable, editCells, addRows, deleteRows);
		};
		
		var _cancelCallback = function()
		{
			if(cancelCallback)
				cancelCallback.call(po, editDataTable, editCells, addRows, deleteRows);
		};
		
		if(count >= confirmCount)
		{
			po.confirm("<fmt:message key='data.confirmRestoreEditCell'><fmt:param>"+count+"</fmt:param></fmt:message>",
			{
				"confirm" : function()
				{
					_confirmCallback();
				},
				"cancel" : function()
				{
					_cancelCallback();
				}
			});
		}
		else
			_confirmCallback();
	};
	
	po.afterSaveClientEditCell = function(editDataTable, savedDatas){};
	
	po.afterSaveServerSideEditCell = function(editDataTable, modifiedCells, addRows, deleteRows){};
	
	po.saveEditCell = function(editDataTable, confirmCount)
	{
		var modifiedCells = editDataTable.cells(".cell-modified");
		var addRows = editDataTable.rows(".add-row");
		var deleteRows = editDataTable.rows(".delete-row");
		
		var count = po.getEditCellCount(editDataTable, modifiedCells, addRows, deleteRows);
		
		if(count <= 0)
			return;
		
		var isServerSide = po.table().DataTable().init().serverSide;
		
		var _confirmCallback = function()
		{
			if(!isServerSide)
			{
				var editTableDatas = $.deepClone($.makeArray(editDataTable.rows(":not(.delete-row)").data()));
				
				var dataTable = po.table().DataTable();
				$.setDataTableData(dataTable, editTableDatas);
				
				po.clearEditGrid(editDataTable, modifiedCells, addRows, deleteRows);
				
				po.afterSaveClientEditCell(editDataTable, editTableDatas);
			}
			else
			{
				var ajaxOptions = po.buildAjaxSaveEditCellOptions(editDataTable, modifiedCells, addRows, deleteRows);
				$.ajax(ajaxOptions);
			}
		};
		
		if(count >= confirmCount)
		{
			var message = (isServerSide ? "<fmt:message key='data.confirmSaveEditCellServerSide'><fmt:param>"+count+"</fmt:param></fmt:message>"
					: "<fmt:message key='data.confirmSaveEditCellClient'><fmt:param>"+count+"</fmt:param></fmt:message>");
			
			po.confirm(message,
			{
				"confirm" : function()
				{
					_confirmCallback();
				}
			});
		}
		else
			_confirmCallback();
	};
	
	po.buildAjaxSaveEditCellOptions = function(editDataTable, modifiedCells, addRows, deleteRows)
	{
		var updates = [];
		var updatePropertyIndexess = [];
		var updatePropertyNamess = [];
		var updatePropertyValuess = [];
		var updateCellIndexess = [];
		var adds  = $.makeArray(addRows.data());
		var deletes = [];
		
		var modifiedRowIndexesMap = $.getDataTableRowIndexesMap(modifiedCells.indexes());
		var editDataTableSettings = editDataTable.settings();
		for(var rowIndex in modifiedRowIndexesMap)
		{
			var myModifiedCellIndexes = modifiedRowIndexesMap[rowIndex];
			
			if(!myModifiedCellIndexes || myModifiedCellIndexes.length < 1)
				continue;
			
			var row = editDataTable.row(rowIndex);
			var $row = $(row.node());
			
			if($row.hasClass("add-row") || $row.hasClass("delete-row"))
				continue;
			
			updates.push(po.originalRowData(editDataTable, rowIndex));

			var updatePropertyIndexes = [];
			var updatePropertyNames = [];
			var updatePropertyValues = [];
			var updateCellIndexes = [];
			
			for(var i = 0; i<myModifiedCellIndexes.length; i++)
			{
				var myModifiedCellIndex = myModifiedCellIndexes[i];
				
				var updatePropertyIndex = $.getDataTableCellPropertyIndex(editDataTableSettings, myModifiedCellIndex);
				var updatePropertyName = $.model.getProperty(po.editGridModel, updatePropertyIndex).name;
				var updatePropertyValue = editDataTable.cell(myModifiedCellIndex).data();
				
				//jquery会将null参数转化为空字符串，某些情况时不合逻辑，这里使用后台null转换占位值
				if(updatePropertyValue == null)
					updatePropertyValue = "<%=AbstractDataConverter.NULL_VALUE_PLACE_HOLDER%>";
				
				updatePropertyIndexes.push(updatePropertyIndex);
				updatePropertyNames.push(updatePropertyName);
				updatePropertyValues.push(updatePropertyValue);
				updateCellIndexes.push(myModifiedCellIndex);
			}
			
			updatePropertyIndexess.push(updatePropertyIndexes);
			updatePropertyNamess.push(updatePropertyNames);
			updatePropertyValuess.push(updatePropertyValues);
			updateCellIndexess.push(updateCellIndexes);
		}
		
		deleteRows.every(function(rowIndex)
		{
			var deleteData = po.originalRowData(editDataTable, rowIndex);
			deletes.push(deleteData);
		});
		
		var options =
		{
			"type" : "POST",
			"url" : po.url("savess"),
			"data" :
			{
				"updates" : updates,
				"updatePropertyNamess" : updatePropertyNamess,
				"updatePropertyValuess" : updatePropertyValuess,
				"adds" : adds,
				"deletes" : deletes
			},
			"beforeSend" : function()
			{
				po.setEditGridOperationUIEnableStatus(false);
			},
			"error" : function()
			{
				po.setEditGridOperationUIEnableStatus(true);
			},
			"success" : function(operationMessage)
			{
				po.ajaxSaveEditCellSuccessHandler(editDataTable, modifiedCells, addRows, deleteRows,
						updates, updatePropertyIndexess, updatePropertyNamess, updatePropertyValuess, updateCellIndexess,
						adds, deletes, operationMessage);
			}
		};
		
		return options;
	};
	
	po.ajaxSaveEditCellSuccessHandler = function(editDataTable, modifiedCells, addRows, deleteRows,
			updateDatas, updatePropertyIndexess, updatePropertyNamess, updatePropertyValuess, updateCellIndexess,
			addDatas, deleteDatas, operationMessage)
	{
		po.clearEditGrid(editDataTable, modifiedCells, addRows, deleteRows, true);
		po.table().DataTable().draw();
		
		po.afterSaveServerSideEditCell(editDataTable, modifiedCells, addRows, deleteRows);
	};
	
	po.getEditCellCount = function(editDataTable, modifiedCells, addRows, deleteRows)
	{
		var count = 0;
		
		if(modifiedCells)
		{
			var modifiedRowIndexesMap = $.getDataTableRowIndexesMap(modifiedCells.indexes());
			
			for(var rowIndex in modifiedRowIndexesMap)
			{
				var row = editDataTable.row(rowIndex);
				var $row = $(row.node());
				
				if($row.hasClass("add-row") || $row.hasClass("delete-row"))
					continue;
				
				count += modifiedRowIndexesMap[rowIndex].length;
			}
		}
		
		if(addRows)
			count += addRows.indexes().length;
		
		if(deleteRows)
			count += deleteRows.indexes().length;
		
		return count;
	};
	
	//将表格中的编辑单元格、添加行置为已保存，删除标记为删除的行
	po.clearEditGrid = function(editDataTable, modifiedCells, addRows, deleteRows, notDrawTable)
	{
		po.closeEditCellPanel(editDataTable);
		
		var needDraw = false;
		
		if(modifiedCells != null)
		{
			if(modifiedCells.indexes().length > 0)
			{
				modifiedCells.every(function(rowIndex, columnIndex, tableLoopCounter, cellLoopCounter)
				{
					var $cell = $(this.node());
					po.markAsUnmodifiedCell($cell);
				});
				
				needDraw = true;
			}
		}
		
		if(addRows != null)
		{
			if(addRows.indexes().length > 0)
			{
				addRows.every(function(rowIndex, tableLoopCounter, rowLoopCounter)
				{
					var $row = $(this.node());
					$row.removeClass("add-row");
				});
				
				needDraw = true;
			}
		}
		
		if(deleteRows != null)
		{
			if(deleteRows.indexes().length > 0)
			{
				deleteRows.remove();
				
				needDraw = true;
			}
		}
		
		if(needDraw && !notDrawTable)
			editDataTable.draw();
		
		po.resetEditGridCache();
	};
	
	po.setEditGridOperationUIEnableStatus = function(enable)
	{
		var methodName = (enable ? "enable" : "disable");
		
		po.element(".edit-grid-button").button(methodName);
		po.element("#${pageId}-editGridSwitch").checkboxradio(methodName);
	};
	
	po.initEditGrid = function(model, ignorePropertyNames)
	{
		po.editGridModel = model;
		po.editGridIgnorePropertyNames = ignorePropertyNames;
		
		$.initButtons(po.element(".edit-grid-operation"));
		
		var $editGridSwitch = po.element("#${pageId}-editGridSwitch");
		//XXX 某些浏览器在刷新页面后会记住选中状态(比如火狐)，这回导致页面逻辑出错，所以这里需要重置
		$editGridSwitch.prop("checked", false);
		$editGridSwitch.checkboxradio({icon : true}).change(function(event)
		{
			var $thisCheckbox = $(this);
			
			if($thisCheckbox.is(":checked"))
			{
				po.enableEditGrid();
			}
			else
			{
				var editDataTable = po.editTable().DataTable();
				var modifiedCells = editDataTable.cells(".cell-modified");
				var addRows = editDataTable.rows(".add-row");
				var deleteRows = editDataTable.rows(".delete-row");
				
				po.restoreEditCell(editDataTable, modifiedCells, addRows, deleteRows, 1,
				function()
				{
					po.disableEditGrid();
				},
				function()
				{
					$thisCheckbox.prop("checked", true);
					$thisCheckbox.checkboxradio("refresh");
				});
			}
		});
		
		po.element(".button-restore", po.element(".edit-grid")).click(function()
		{
			var editDataTable = po.editTable().DataTable();
			var selectedCells = editDataTable.cells(".selected");
			var addRows = editDataTable.rows(".selected.add-row");
			var deleteRows = editDataTable.rows(".selected.delete-row");
			
			po.restoreEditCell(editDataTable, selectedCells, addRows, deleteRows, 2);
		});
		
		po.element(".button-restore-all", po.element(".edit-grid")).click(function()
		{
			var editDataTable = po.editTable().DataTable();
			
			var modifiedCells = editDataTable.cells(".cell-modified");
			var addRows = editDataTable.rows(".add-row");
			var deleteRows = editDataTable.rows(".delete-row");
			
			po.restoreEditCell(editDataTable, modifiedCells, addRows, deleteRows, 2);
		});
		
		po.element(".button-save", po.element(".edit-grid")).click(function()
		{
			var editDataTable = po.editTable().DataTable();
			po.saveEditCell(editDataTable, 2);
		});
		
		po.editGridFormPage.element()
		.focusin(function()
		{
			var $this = $(this);
			
			if(!$this.hasClass("focus"))
				$this.addClass("focus");
		})
		/* XXX 不在这里加失去焦点效果了，当切换单元格时会有一种卡顿感觉
		.focusout(function()
		{
			var $this = $(this);
			$this.removeClass("focus");
		})*/;
		
		po.editGridFormPage.element(".form-panel")
		.keydown(function(event)
		{
			if(event.keyCode == $.ui.keyCode.ESCAPE)
			{
				var editDataTable = po.editTable().DataTable();
				po.closeEditCellPanel(editDataTable);
				
				po.editCellOnSelect = false;
			}
			
			//禁止冒泡，因为这些快捷键在表格上有特殊处理逻辑
			if(event.keyCode == $.ui.keyCode.ESCAPE || event.keyCode == $.ui.keyCode.ENTER
					|| event.keyCode == $.ui.keyCode.DOWN || event.keyCode == $.ui.keyCode.UP
					|| event.keyCode == $.ui.keyCode.LEFT|| event.keyCode == $.ui.keyCode.RIGHT)
			{
				event.stopPropagation();
			}
		});
		
		po.editGridFormPage.element(".close-icon").click(function()
		{
			var editDataTable = po.editTable().DataTable();
			po.closeEditCellPanel(editDataTable);
			
			po.editCellOnSelect = false;
		});
		
		//serverSide表格在保存编辑表格后需要刷新编辑表格数据
		var dataTable = po.table().DataTable();
		var isServerSide = dataTable.init().serverSide;
		
		if(isServerSide)
		{
			dataTable.on("draw", function(e, settings)
			{
				if(po.isEnableEditGrid)
				{
					var dataTable = $(this).DataTable();
					var editTableDatas = po.getEditGridInitDatas(dataTable);
					
					var editDataTable = po.editTable().DataTable();
					$.setDataTableData(editDataTable, editTableDatas);
					
					po.resetEditGridCache();
					
					po.setEditGridOperationUIEnableStatus(true);
				}
			})
			.on("error", function()
			{
				if(po.isEnableEditGrid)
				{
					po.setEditGridOperationUIEnableStatus(true);
				}
			});
		}
	};
})
(${pageId});
</script>

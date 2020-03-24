<#--
编辑表格功能JS片段。

依赖：
page_js_obj.ftl
page_obj_grid.ftl
data_page_obj_grid.ftl
data_page_obj_edit_grid_html.ftl

变量：

-->

<#--在表格页面中内嵌一个用于编辑表格的表单页面，并使用它来构建单元格编辑面板，重用代码-->
<#assign gridPageId=pageId>
<#assign pageId=editGridFormPageId>
<#include "../include/data_page_obj.ftl">
<#include "../include/data_page_obj_form.ftl">
<script type="text/javascript">
(function(po)
{
	//XXX 这里必须添加handle设置，不然元素的按键、鼠标事件都会无效
	po.element().draggable({ handle : po.element(".form-panel-dragger") });
	po.element().hide();
	po.formLabels.submit = "<@spring.message code='confirm' />";
})
(${pageId});
</script>
<#assign pageId=gridPageId>
<script type="text/javascript">
(function(po)
{
	//内嵌的表单页面对象
	po.editGridFormPage = ${editGridFormPageId};
	//编辑表格对应的表，会在initEditGrid函数中初始化
	po.editGridMetaTable = undefined;
	
	po.isEnableEditGrid = false;
	//存储行初始数据的映射表，在单元格修改前存储，用于支持恢复等操作
	po.editGridOriginalRowDataMap = {};
	//存储行的指定列值是否已从服务端加载
	po.editGridColumnValueFetchedMap = {};
	//是否在单元格选中时编辑单元格，键盘快速导航时通常不需要打开编辑单元格面板
	po.editCellOnSelect = true;
	
	po.elementEditGridSwitch = function()
	{
		return po.element("#${pageId}-editGridSwitch");
	};
	
	po.elementEditGridSwitchWrapper = function()
	{
		return po.element(".edit-grid-switch-wrapper");
	};
	
	po.resetEditGridCache = function()
	{
		po.editGridOriginalRowDataMap = {};
		po.editGridColumnValueFetchedMap = {};
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
			rowData = editDataTable.row(rowIndex).data();
		
		return rowData;
	};
	
	//获取/设置行的指定列值是否已从服务端加载
	po.columnValueFetched = function(row, columnName, fetched)
	{
		if(fetched == undefined)
		{
			var rowInfo = po.editGridColumnValueFetchedMap[row];
			return (rowInfo ? rowInfo[columnName] : false);
		}
		else
		{
			var rowInfo = (po.editGridColumnValueFetchedMap[row] || (po.editGridColumnValueFetchedMap[row] = {}));
			rowInfo[columnName] = fetched;
		}
	};
	
	po.elementEditTable = function()
	{
		var id = po.pageId +"-edit-table";
		
		var $editTable = po.element("#" + id);
		if($editTable.length == 0)
			$editTable = $("<table id='"+id+"' width='100%' class='hover stripe' tabindex='0'></table>").appendTo(po.element(".content"));
		
		return $editTable;
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
		return po.removeCheckColumnProperty(editTableDatas);
	};
	
	po.initEditGridDataTable = function($editTable, dataTable)
	{
		var editTableDatas = po.getEditGridInitDatas(dataTable);
		
		var columns = $.buildDataTablesColumns(po.editGridMetaTable);
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
		po.bindEditDataTableEvents($editTable);
	};
	
	po.enableEditGrid = function()
	{
		var $headOperation = po.element(".head .operation");
		
		if(po.element(".edit-grid-button", $headOperation).length == 0)
		{
			po.element(".ui-button", $headOperation).addClass("not-edit-grid-button");
			
			var $buttonWrapper = $("<div class='edit-grid-button-wrapper' style='display:inline-block;' />").appendTo($headOperation);
			$("<button name='editGridAddButton' class='edit-grid-button highlight'><@spring.message code='add' /></button>&nbsp;"
				+"<button name='editGridEditButton' class='edit-grid-button highlight'><@spring.message code='edit' /></button>&nbsp;"
				+"<button name='editGridDeleteButton' class='edit-grid-button highlight'><@spring.message code='delete' /></button>").appendTo($buttonWrapper);
			
			$.initButtons($buttonWrapper);
			
			$buttonWrapper.hide();
			
			po.element("button[name='editGridAddButton']", $buttonWrapper).click(function()
			{
				po.editCellOnSelect = true;
				
				var rowData = $.meta.instance(po.editGridMetaTable);
				
				var editDataTable = po.elementEditTable().DataTable();
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
				
				var editDataTable = po.elementEditTable().DataTable();
				var selectedIndexes = editDataTable.cells(".selected").indexes();
				
				if(selectedIndexes)
					po.editCell(editDataTable, selectedIndexes, true);
			});
			
			po.element("button[name='editGridDeleteButton']", $buttonWrapper).click(function()
			{
				var editDataTable = po.elementEditTable().DataTable();
				
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
						var columnNameCellIndexes = $.getDataTableColumnNameCellIndexes(settings, selectedCellIndexes);
						
						po.storeEditCell(editDataTable, columnNameCellIndexes, {});
					}
				}
			});
		}
		
		po.element(".head .search").addClass("ui-state-disabled");
		po.element(".foot .pagination").addClass("ui-state-disabled");
		
		var dataTable = po.elementTable().DataTable();
		var dataTableScrollTop = po.dataTableParent(dataTable).prop("scrollTop");
		var $tableContainer = $(dataTable.table().container());
		$tableContainer.hide();
		
		//新建本地模式的DataTable，因为server-side模式的DataTable不能添加行
		var $editTable = po.elementEditTable();
		po.initEditGridDataTable($editTable, dataTable);
		po.dataTableParent($editTable.DataTable()).scrollTop(dataTableScrollTop);
		po.expectedResizeDataTableElements.push($editTable[0]);
		
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
		var $editTable = po.elementEditTable();
		$editTable.DataTable().destroy();
		$editTable.remove();
		po.expectedResizeDataTableElements.pop();
		
		var dataTable = po.elementTable().DataTable();
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
		var $row = $(editDataTable.row(row).node());
		return $row.hasClass("add-row");
	};
	
	//判断单元格是否需要从服务端加载数据
	po.needFetchColumnValue = function(editDataTable, cellIndex, column, columnValue)
	{
		if(!columnValue)
			return false;
		
		var re = ($.meta.isClobColumn(column) || $.meta.isSqlxmlColumn(column));
		
		if(re && po.isClientDataRow(editDataTable, cellIndex.row))
			re =  false;
		
		if(re && po.columnValueFetched(cellIndex.row, column.name))
			re = false;
		
		if(re)
		{
			var $cell = $(editDataTable.cell(cellIndex).node());
			
			if(po.isModifiedCell($cell))
				re = false;
		}
		
		return re;
	};
	
	po.getCellIndexesSingleRowIf = function(cellIndexes)
	{
		var row = -1;
		for(var i=0; i<cellIndexes.length; i++)
		{
			if(row == -1)
				row = cellIndexes[i].row;
			else if(row != cellIndexes[i].row)
				return -1;
		}
		
		return row;
	};
	
	//获取编辑表单初始数据
	po.getEditCellFormPageInitData = function(editDataTable, indexes, columnNameCellIndexes, needFetchRowDataMap,
			needFetchColumnNamesMap)
	{
		var data =
		{
			data: null,
			dataIsClient: true,
			formData: {}
		};
		
		var singleRow = po.getCellIndexesSingleRowIf(indexes);
		if(singleRow >= 0 && !po.isClientDataRow(editDataTable, singleRow))
		{
			data.data = po.originalRowData(editDataTable, singleRow);
			data.dataIsClient = false;
		}
		
		var table = po.editGridMetaTable;
		
		for(var columnName in columnNameCellIndexes)
		{
			var column = $.meta.column(table, columnName);
			
			var myIndexes = columnNameCellIndexes[columnName];
			var myIndex0 = myIndexes[0];
			var columnValue0 = editDataTable.cell(myIndex0).data();
			
			//此列单元格仅选中一行时才从后台获取列值
			if(myIndexes.length == 1 && po.needFetchColumnValue(editDataTable, myIndex0, column, columnValue0))
			{
				var myIndex0Row = myIndex0.row;
				
				if(!needFetchRowDataMap[myIndex0Row])
					needFetchRowDataMap[myIndex0Row] = po.originalRowData(editDataTable, myIndex0Row);
				
				var columnNames = (needFetchColumnNamesMap[myIndex0Row] || (needFetchColumnNamesMap[myIndex0Row] = []));
				columnNames.push(columnName);
			}
			else
			{
				var allColumnValueEquals = true;
				
				for(var i=1; i<myIndexes.length; i++)
				{
					var myValue = editDataTable.cell(myIndexes[i]).data();
					
					if(myValue != columnValue0 || po.isPlaceholderColumnValue(column, myValue))
					{
						allColumnValueEquals = false;
						break;
					}
				}
				
				if(allColumnValueEquals)
					$.meta.columnValue(data.formData, columnName, columnValue0);
			}
		}
		
		return data;
	};
	
	//构建编辑表格从后台获取单元格值的ajax选项。
	po.buildEditCellFetchColumnValuessAjaxOptions = function(editDataTable, indexes, focus, columnNameCellIndexes, data,
			needFetchRows, needFetchRowDatas, needFetchColumnNamess)
	{
		var options =
		{
			"contentType" : $.CONTENT_TYPE_JSON,
			"type" : "POST",
			"url" : po.url("getColumnValuess"),
			"data" : { "datas" : $.meta.uniqueRecordData(po.editGridMetaTable, needFetchRowDatas), "columnNamess" : needFetchColumnNamess },
			"success" : function(columnValueFetchedss)
			{
				if(columnValueFetchedss)
				{
					var settings = editDataTable.settings();
					
					for(var i=0; i<needFetchRows.length; i++)
					{
						var needFetchRow = parseInt(needFetchRows[i]);
						var needFetchRowData = needFetchRowDatas[i];
						var needFetchColumnNames = needFetchColumnNamess[i];
						var columnValueFetcheds = columnValueFetchedss[i];
						
						if(columnValueFetcheds)
						{
							for(var j=0; j<needFetchColumnNames.length; j++)
							{
								var columnValueFetched = columnValueFetcheds[j];
								
								if(columnValueFetched != null)
								{
									var needFetchColumnName = needFetchColumnNames[j];
									
									$.meta.columnValue(data.formData, needFetchColumnName, columnValueFetched);
									$.meta.columnValue(needFetchRowData, needFetchColumnName, columnValueFetched);
									
									var myColumn = $.getDataTableColumn(settings, needFetchColumnName);
									var myCell = editDataTable.cell({ "row" : needFetchRow, "column" : myColumn });
									myCell.data(columnValueFetched);
									
									po.columnValueFetched(needFetchRow, needFetchColumnName, true);
								}
							}
						}
					}
				}
				
				po.showEditCellPanel(editDataTable, indexes, columnNameCellIndexes, data, focus);
			}
		};
		
		return options;
	};
	
	//编辑单元格
	po.editCell = function(editDataTable, indexes, focus)
	{
		var settings = editDataTable.settings();
		var columnNameCellIndexes = $.getDataTableColumnNameCellIndexes(settings, indexes);
		var model = po.editGridMetaTable;
		var needFetchRowDataMap = {};
		var needFetchColumnNamesMap = {};
		
		var data = po.getEditCellFormPageInitData(editDataTable, indexes, columnNameCellIndexes,
						needFetchRowDataMap, needFetchColumnNamesMap);
		
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
			var needFetchColumnNamess = $.getMapKeyValueArray(needFetchColumnNamesMap, sortFunction).values;
			
			var fetchAjaxOptions = po.buildEditCellFetchColumnValuessAjaxOptions(editDataTable, indexes, focus, columnNameCellIndexes, data,
					needFetchRows, needFetchRowDatas, needFetchColumnNamess);
			$.ajax(fetchAjaxOptions);
		}
		else
			po.showEditCellPanel(editDataTable, indexes, columnNameCellIndexes, data, focus);
	};
	
	po.showEditCellPanel = function(editDataTable, indexes, columnNameCellIndexes, data, focus)
	{
		if(indexes.length == 0)
			return;
		
		var $table = $(editDataTable.table().node());
		var $tableContainer = $(editDataTable.table().container());
		var $editFormCell = $(editDataTable.cell(indexes[0]).node());
		
		var singleColumnName = $.getPropertyNameIfSingle(columnNameCellIndexes);
		
		po.editGridFormPage.data = data.data;
		po.editGridFormPage.dataIsClient = data.dataIsClient;
		
		po.currentColumnNameCellIndexes = columnNameCellIndexes;
		
		var $formPage = po.editGridFormPage.element();
		var $formPanel = po.editGridFormPage.element(".form-panel");
		var $form = po.editGridFormPage.form();
		
		if($form.isTableform())
			$form.tableform("destroy");
		
		$formPage.appendTo(po.dataTableParent(editDataTable));
		$formPage.show();
		
		//只有一个列，隐藏标签，否则，显示标签
		if(singleColumnName)
		{
			$formPanel.css("min-width", $tableContainer.width()/3);
			$form.addClass("hide-form-label");
		}
		else
		{
			$formPanel.css("min-width", $tableContainer.width()/2);
			$form.removeClass("hide-form-label");
		}
		
		$form.tableform(
		{
			table : po.editGridMetaTable,
			data : data.formData,
			renderColumn : function(column)
			{
				var columnNameCellIndexes = po.currentColumnNameCellIndexes;
				return (columnNameCellIndexes[column.name] != undefined);
			},
			submit : function()
			{
				var $this = $(this);
				
				var formData = $this.tableform("data");
				var columnNameCellIndexes = po.currentColumnNameCellIndexes;
				
				var editDataTable = po.elementEditTable().DataTable();
				
				po.closeEditCellPanel(editDataTable);
				po.storeEditCell(editDataTable, columnNameCellIndexes, formData);
				
				return false;
			},
			invalidHandler : function(){ $formPage.show(); },
			selectColumnValue : po.editGridFormPage.selectColumnValue,
			viewColumnValue: po.editGridFormPage.viewColumnValue,
			downloadColumnValue: po.editGridFormPage.downloadColumnValue,
			fileUploadUrl : "${contextPath}/data/uploadFile",
			fileDeleteUrl : "${contextPath}/data/deleteFile",
			validationRequiredAsAdd : false,
			labels : po.editGridFormPage.formLabels,
			dateFormat : "${sqlDateFormat}",
			timestampFormat : "${sqlTimestampFormat}",
			timeFormat : "${sqlTimeFormat}",
			binaryFileReturnLabeledValue: true
		});
		
		$formPage.position({ my : "left top", at : "left bottom", of : $editFormCell, within : $tableContainer});
		
		//激活第一个属性
		if(singleColumnName || focus)
			$form.tableform("activeColumn");
	};
	
	//关闭编辑面板
	po.closeEditCellPanel = function(editDataTable)
	{
		var $formPage = po.editGridFormPage.element();
		var $form = po.editGridFormPage.form();
		
		if($form.isTableform())
		{
			$formPage.hide();
			
			if($formPage.hasClass("focus"))
				$formPage.removeClass("focus");
			
			//不销毁表单，因为isSubmitWhenPropertySubmit逻辑有可能多次关闭表单面板
			//$form.tableform("destroy");
			
			$formPage.appendTo(po.element(".foot"));
		}
		
		$(editDataTable.table().node()).focus();
	};
	
	//将数据存储至表格
	po.storeEditCell = function(editDataTable, columnNameCellIndexes, data)
	{
		var storeCellIndexes = [];
		var storeCellValues = [];
		var storeCellHtmls = [];
		var storeCellChanges = [];
		
		for(var columnName in columnNameCellIndexes)
		{
			var myIndexes = columnNameCellIndexes[columnName];
			var column = $.meta.column(po.editGridMetaTable, columnName);
			var columnValue = $.meta.columnValue(data, column.name);
			
			for(var i=0; i<myIndexes.length; i++)
			{
				var index = myIndexes[i];
				
				var originalRowData = po.originalRowData(editDataTable, index.row);
				var originalCellValue = $.meta.columnValue(originalRowData, column);
				var myColumnValue = columnValue;
				
				var changed = true;
				
				if(myColumnValue == originalCellValue)
					changed = false;
				else
				{
					if($.meta.isLabeledValue(myColumnValue))
						myColumnValue = $.meta.valueOfLabeledValue(myColumnValue);
					
					if(myColumnValue == originalCellValue)
						changed = false;
					//无原始值但是表单空字符串保存的情况
					else if((originalCellValue == null) && (myColumnValue == "" || myColumnValue == null))
						changed = false;
				}
				
				storeCellIndexes.push(index);
				storeCellChanges.push(changed);
				storeCellValues.push(columnValue);
				storeCellHtmls.push(null);
			}
		}
		
		for(var i=0; i<storeCellIndexes.length; i++)
			po.updateEditDataTableCellValue(editDataTable, storeCellIndexes[i], storeCellValues[i]);
		
		//统一绘制，效率更高
		editDataTable.cells(storeCellIndexes).draw();
		
		for(var i=0; i<storeCellChanges.length; i++)
		{
			var $cell = $(editDataTable.cell(storeCellIndexes[i]).node());
			
			if(storeCellHtmls[i] != null)
				$cell.html(storeCellHtmls[i]);
			
			if(storeCellChanges[i] == true)
				po.markAsModifiedCell($cell);
			else
				po.markAsUnmodifiedCell($cell);
		}
		
		//新值可能会影响单元格宽度，因此需要重设列宽
		if(storeCellIndexes.length > 0)
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
			var originalRowData = $.extend({}, editDataTable.row(rowIndex).data());
			po.editGridOriginalRowDataMap[rowIndex] = originalRowData;
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
			
			var model = po.editGridMetaTable;
			var settings = editDataTable.settings();
			
			var editCellCount = 0;
			
			editCells.every(function()
			{
				var index = this.index();
				
				var originalRowData = po.originalRowData(editDataTable, index.row);
				
				if(originalRowData)
				{
					var columnName = $.getDataTableCellName(settings, index);
					var originalCellValue = $.meta.columnValue(originalRowData, columnName);
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
			<#assign messageArgs=['"+count+"'] />
			po.confirm("<@spring.messageArgs code='data.confirmRestoreEditCell' args=messageArgs />",
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
		
		var isServerSide = po.elementTable().DataTable().init().serverSide;
		
		var _confirmCallback = function()
		{
			if(!isServerSide)
			{
				var editTableDatas = $.deepClone($.makeArray(editDataTable.rows(":not(.delete-row)").data()));
				
				var dataTable = po.elementTable().DataTable();
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
			<#assign messageArgs=['"+count+"'] />
			var message = (isServerSide ? "<@spring.messageArgs code='data.confirmSaveEditCellServerSide' args=messageArgs />"
					: "<@spring.messageArgs code='data.confirmSaveEditCellClient' args=messageArgs />");
			
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
		var updateOrigins = [];
		var updateTargets = [];
		var updateCellIndexess = [];
		var adds  = $.makeArray(addRows.data());
		var deletes = [];
		
		var settings = editDataTable.settings();
		
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
			
			updateOrigins.push(po.originalRowData(editDataTable, rowIndex));
			
			var updateTarget = {};
			var updateCellIndexes = [];
			
			for(var i = 0; i<myModifiedCellIndexes.length; i++)
			{
				var myModifiedCellIndex = myModifiedCellIndexes[i];
				
				var updateColumnName = $.getDataTableCellName(settings, myModifiedCellIndex);
				var updateColumnValue = editDataTable.cell(myModifiedCellIndex).data();
				
				updateTarget[updateColumnName] = updateColumnValue;
				updateCellIndexes.push(myModifiedCellIndex);
			}
			
			updateTargets.push(updateTarget);
			updateCellIndexess.push(updateCellIndexes);
		}
		
		deleteRows.every(function(rowIndex)
		{
			var deleteData = po.originalRowData(editDataTable, rowIndex);
			deletes.push(deleteData);
		});
		
		var options =
		{
			"contentType" : $.CONTENT_TYPE_JSON,
			"type" : "POST",
			"url" : po.url("savess"),
			"data" :
			{
				"updateOrigins" : $.meta.uniqueRecordData(po.editGridMetaTable, updateOrigins),
				"updateTargets" : $.meta.removeLabeledValueFeature(updateTargets),
				"adds" : po.removeCheckColumnProperty($.meta.removeLabeledValueFeature(adds)),
				"deletes" : $.meta.uniqueRecordData(po.editGridMetaTable, deletes)
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
						updateOrigins, updateTargets, updateCellIndexess,
						adds, deletes, operationMessage);
			}
		};
		
		return options;
	};
	
	po.ajaxSaveEditCellSuccessHandler = function(editDataTable, modifiedCells, addRows, deleteRows,
			updateOrigins, updateTargets, updateCellIndexess,
			addDatas, deleteDatas, operationMessage)
	{
		po.clearEditGrid(editDataTable, modifiedCells, addRows, deleteRows, true);
		po.elementTable().DataTable().draw();
		
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
		po.elementEditGridSwitch().checkboxradio(methodName);
	};
	
	po.initEditGrid = function(table)
	{
		po.editGridMetaTable = table;
		
		$.initButtons(po.element(".edit-grid-operation"));
		
		var $editGridSwitch = po.elementEditGridSwitch();
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
				var editDataTable = po.elementEditTable().DataTable();
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
			var editDataTable = po.elementEditTable().DataTable();
			var selectedCells = editDataTable.cells(".selected");
			var addRows = editDataTable.rows(".selected.add-row");
			var deleteRows = editDataTable.rows(".selected.delete-row");
			
			po.restoreEditCell(editDataTable, selectedCells, addRows, deleteRows, 2);
		});
		
		po.element(".button-restore-all", po.element(".edit-grid")).click(function()
		{
			var editDataTable = po.elementEditTable().DataTable();
			
			var modifiedCells = editDataTable.cells(".cell-modified");
			var addRows = editDataTable.rows(".add-row");
			var deleteRows = editDataTable.rows(".delete-row");
			
			po.restoreEditCell(editDataTable, modifiedCells, addRows, deleteRows, 2);
		});
		
		po.element(".button-save", po.element(".edit-grid")).click(function()
		{
			var editDataTable = po.elementEditTable().DataTable();
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
				var editDataTable = po.elementEditTable().DataTable();
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
			var editDataTable = po.elementEditTable().DataTable();
			po.closeEditCellPanel(editDataTable);
			
			po.editCellOnSelect = false;
		});
		
		//serverSide表格在保存编辑表格后需要刷新编辑表格数据
		var dataTable = po.elementTable().DataTable();
		var isServerSide = dataTable.init().serverSide;
		
		if(isServerSide)
		{
			dataTable.on("draw", function(e, settings)
			{
				if(po.isEnableEditGrid)
				{
					var dataTable = $(this).DataTable();
					var editTableDatas = po.getEditGridInitDatas(dataTable);
					
					var editDataTable = po.elementEditTable().DataTable();
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

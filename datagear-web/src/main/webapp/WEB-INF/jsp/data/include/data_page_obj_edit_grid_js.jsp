<%--
/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */
--%>
<%@ page import="java.sql.NClob"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="org.datagear.web.util.WebUtils"%>
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
	
	po.isPropertyActionClientSubmit = function(property, propertyConcreteModel)
	{
		return true;
	};
	
	po.superBuildPropertyActionOptions = po.buildPropertyActionOptions;
	po.buildPropertyActionOptions = function(property, propertyModel, extraRequestParams, extraPageParams)
	{
		var actionParam = po.superBuildPropertyActionOptions(property, propertyModel, extraRequestParams, extraPageParams);
		
		var singleRow = $.getDataTableRowIfSingle(po.currentCellIndexes);
		
		var isClientPageData = true;
		
		//服务端数据
		if(singleRow != null && !po.gridPage.isClientDataRow(po.currentDataTable, singleRow))
		{
			var data = po.gridPage.originalRowData(po.currentDataTable, singleRow);
			data = $.unref($.ref(data));
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
	//存储行初始值
	po.editGridOriginalRowDatas = {};
	//存储行的指定属性值是否已从服务端加载
	po.editGridFetchedPropertyValues = {};
	//是否在单元格选中时编辑单元格，键盘快速导航时通常不需要打开编辑单元格面板
	po.editCellOnSelect = true;
	
	/**
	 * 获取行初始数据对象。
	 * @param dataTable 必选，DataTable的API对象
	 * @param row 必选，行索引
	 * @param forceStore 可选，是否强制缓存，默认为true
	 */
	po.originalRowData = function(dataTable, row, forceStore)
	{
		if(forceStore == undefined)
			forceStore = true;
		
		if(!po.editGridOriginalRowDatas)
			po.editGridOriginalRowDatas = {};
		
		var rowData = po.editGridOriginalRowDatas[row];
		
		if(!rowData && forceStore)
		{
			rowData = dataTable.row(row).data();
			//防止单元格编辑导致内部引用混乱
			rowData = $.unref($.ref(rowData));
			po.editGridOriginalRowDatas[row] = rowData;
		}
		
		return rowData;
	};
	
	//获取/设置行的指定属性值是否已从服务端加载
	po.fetchedPropertyValue = function(row, propertyName, fetched)
	{
		if(fetched == undefined)
		{
			var rowInfo = po.editGridFetchedPropertyValues[row];
			if(!rowInfo)
				return false;
			
			return rowInfo[propertyName];
		}
		else
		{
			var rowInfo = (po.editGridFetchedPropertyValues[row] || (po.editGridFetchedPropertyValues[row] = {}));
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
					var dataTable = $(this).DataTable();
					
					po.editCellOnSelect = true;
					
					$.handleCellSelectionForClick(dataTable, event, target);
				}
			}
		})
		.on("keydown", function(event)
		{
			if(po.isEnableEditGrid)
			{
				var dataTable = $(this).DataTable();
				
				if(event.keyCode == $.ui.keyCode.ESCAPE)
				{
					po.closeEditCellPanel(dataTable);
					
					po.editCellOnSelect = false;
				}
				else if(event.keyCode == $.ui.keyCode.ENTER)
				{
					//必须加下面这行代码，不然当打开的编辑面板表单输入框自动焦点时，会触发表单提交事件
					event.preventDefault();
					
					var selectedIndexes = dataTable.cells(".selected").indexes();
					
					if(selectedIndexes)
						po.editCell(dataTable, selectedIndexes, true);
				}
				else
				{
					$.handleCellNavigationForKeydown(dataTable, event);
				}
			}
		})
		.on("select", function(event, dataTable, type, indexes)
		{
			if(po.isEnableEditGrid)
			{
				if(type == "cell")
				{
					if(po.editCellOnSelect)
						po.editCell(dataTable, indexes);
				}
				/*
				else if(type == "row")
				{
					var columnCount = $.getDataTableColumnCount(dataTable);
					
					//编辑表格状态下无行选中状态，行选中操作仅作为选中此行的所有单元格处理，这样使逻辑简单一致
					//dataTable.rows(indexes).deselect();
					
					var cellIndexes = dataTable.cells(".selected").indexes();
					
					for(var i=0; i<indexes.length; i++)
					{
						var row = indexes[i];
						
						for(var j=1; j<columnCount; j++)
							cellIndexes.push({"row" : row, "column" : j});
					}
					
					$(dataTable.table().node()).focus();
					dataTable.cells(cellIndexes).select();
				}
				*/
			}
		})
		.on("deselect", function(event, dataTable, type, indexes)
		{
			if(po.isEnableEditGrid)
			{
				if(type == "cell")
				{
					po.closeEditCellPanel(dataTable, indexes);
				}
				/*
				else if(type == "row")
				{
					var columnCount = $.getDataTableColumnCount(dataTable);
					
					var cellIndexes = [];
					for(var i=0; i< indexes.length; i++)
					{
						var row = indexes[i];
						
						for(var j=1; j<columnCount; j++)
							cellIndexes.push({"row" : row, "column" : j});
					}
					
					$(dataTable.table().node()).focus();
					dataTable.cells(cellIndexes).deselect();
				}
				*/
			}
		})/*
		.on("preDraw", function(event, settings)
		{
			//禁止表格重绘，比如排序
			if(po.isEnableEditGrid)
				return false;
			else
				return true;
		})*/;
	};
	
	po.initEditGridDataTable = function($editTable, dataTable)
	{
		var editTableData = $.makeArray(dataTable.rows().data());
		
		var columns = $.buildDataTablesColumns(po.editGridModel);
		var settings = po.buildDataTableSettingsLocal(columns, editTableData);
		
		settings.ordering = false;
		
		var checkColumn = settings.columns[0];
		checkColumn.render = function(data, type, row, meta)
		{
			var content = po.renderCheckColumn.call(this, data, type, row, meta);
			
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
				
				var dataTable = po.editTable().DataTable();
				var row = dataTable.row.add($.model.instance(po.editGridModel));
				$(row.node()).addClass("add-row");
				row.draw();
				dataTable.cell(row.index(), 0).data("add-row").draw();
			});
			
			po.element("button[name='editGridEditButton']", $buttonWrapper).click(function()
			{
				po.editCellOnSelect = true;
				
				var dataTable = po.editTable().DataTable();
				var selectedIndexes = dataTable.cells(".selected").indexes();
				
				if(selectedIndexes)
					po.editCell(dataTable, selectedIndexes, true);
			});
			
			po.element("button[name='editGridDeleteButton']", $buttonWrapper).click(function()
			{
				po.editCellOnSelect = true;
				
				var dataTable = po.editTable().DataTable();
				
				var selectedAddRows = dataTable.rows(".selected.add-row");
				if(selectedAddRows)
					selectedAddRows.remove().draw();
				
				var selectedRows = dataTable.rows(".selected");
				
				if(selectedRows)
				{
					selectedRows.deselect();
					
					selectedRows.every(function(rowIndex)
					{
						var row = dataTable.row(rowIndex);
						var $row = $(row.node());
						
						$row.addClass("delete-row");
						dataTable.cell(rowIndex, 0).data("delete-row").draw();
					});
				}
			});
		}
		
		var dataTable = po.table().DataTable();
		var $tableContainer = $(dataTable.table().container());
		$tableContainer.hide();
		
		//新建本地模式的DataTable，因为server-side模式的DataTable不能添加行
		var $editTable = po.editTable();
		po.initEditGridDataTable($editTable, dataTable);
		
		po.editGridOriginalRowDatas = {};
		po.editGridFetchedPropertyValues = {};
		po.editCellOnSelect = true;
		
		po.element(".head .search").addClass("ui-state-disabled");
		po.element(".foot .pagination").addClass("ui-state-disabled");
		
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
		
		var $table = po.table();
		var $tableContainer = $($table.DataTable().table().container());
		$tableContainer.show();
		
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
	
	po.isClientDataRow = function(dataTable, row)
	{
		var $row = $(dataTable.row(row).node());
		
		return $row.hasClass("add-row");
	};
	
	//判断单元格是否需要从服务端加载数据
	po.needFetchPropertyValue = function(dataTable, cellIndex, property, propertyValue)
	{
		if(propertyValue == null)
			return false;
		
		var re = !po.isSinglePrimitivePropertyValueFullyFetched(po.editGridModel, property, propertyValue);
		
		if(re && po.isClientDataRow(dataTable, cellIndex.row))
			re =  false;
		
		if(re && po.fetchedPropertyValue(cellIndex.row, property.name))
			re = false;
		
		if(re)
		{
			var $cell = $(dataTable.cell(cellIndex).node());
			
			if(po.isModifiedCell($cell))
				re = false;
		}
		
		return re;
	};
	
	//获取编辑表单初始数据
	po.getEditCellFormInitData = function(dataTable, indexes, propertyIndexesMap, needFetchRowDataMap, needFetchPropertyNamesMap)
	{
		var data = {};
		
		var model = po.editGridModel;
		
		for(var pi in propertyIndexesMap)
		{
			var property = $.model.getProperty(model, parseInt(pi));
			
			var pindexes = propertyIndexesMap[pi];
			var pindex0 = pindexes[0];
			var propertyValue0 = dataTable.cell(pindex0).data();
			
			//仅从后台获取选中一行的属性值
			if(pindexes.length == 1 && po.needFetchPropertyValue(dataTable, pindex0, property, propertyValue0))
			{
				var pindex0Row = pindex0.row;
				
				if(!needFetchRowDataMap[pindex0Row])
					needFetchRowDataMap[pindex0Row] = po.originalRowData(dataTable, pindex0Row);
				
				var propertyNames = (needFetchPropertyNamesMap[pindex0Row] || (needFetchPropertyNamesMap[pindex0Row] = []));
				propertyNames.push(property.name);
			}
			else
			{
				var allColumnValueEquals = true;
				
				for(var i=1; i<pindexes.length; i++)
				{
					var pindex = pindexes[i];
					var propertyValue = dataTable.cell(pindex).data();
					
					if(propertyValue != propertyValue0)
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
	po.buildEditCellFetchPropertyValuessAjaxOptions = function(dataTable, indexes, focus, propertyIndexesMap, data,
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
					var settings = dataTable.settings();
					
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
									var myCell = dataTable.cell({ "row" : needFetchRow, "column" : myColumn });
									myCell.data(fetchedPropertyValue);
									
									po.fetchedPropertyValue(needFetchRow, needFetchPropertyName, true);
								}
							}
						}
					}
				}
				
				po.showEditCellPanel(dataTable, indexes, propertyIndexesMap, data, focus);
			}
		};
		
		return options;
	};
	
	//编辑单元格
	po.editCell = function(dataTable, indexes, focus)
	{
		var settings = dataTable.settings();
		var propertyIndexesMap = $.getDataTableCellPropertyIndexesMap(settings, indexes);
		var model = po.editGridModel;
		var needFetchRowDataMap = {};
		var needFetchPropertyNamesMap = {};
		
		var data = po.getEditCellFormInitData(dataTable, indexes, propertyIndexesMap,
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
			
			var fetchAjaxOptions = po.buildEditCellFetchPropertyValuessAjaxOptions(dataTable, indexes, focus, propertyIndexesMap, data,
					needFetchRows, needFetchRowDatas, needFetchPropertyNamess);
			$.ajax(fetchAjaxOptions);
		}
		else
			po.showEditCellPanel(dataTable, indexes, propertyIndexesMap, data, focus);
	};
	
	po.showEditCellPanel = function(dataTable, indexes, propertyIndexesMap, data, focus)
	{
		if(indexes.length == 0)
			return;
		
		var $table = $(dataTable.table().node());
		var $tableContainer = $(dataTable.table().container());
		var $cellNodes = $(dataTable.cells(indexes).nodes());
		var $editFormCell = $($cellNodes[0]);
		
		$cellNodes.removeClass("cell-edit-form");
		$editFormCell.addClass("cell-edit-form");
		
		var propertyCount = $.getPropertyCount(propertyIndexesMap);
		
		po.editGridFormPage.data = data;
		po.editGridFormPage.currentDataTable = dataTable;
		po.editGridFormPage.currentCellIndexes = indexes;
		po.editGridFormPage.currentPropertyIndexesMap = propertyIndexesMap;
		
		var $formPage = po.editGridFormPage.element();
		var $formPanel = po.editGridFormPage.element(".form-panel");
		
		if($formPage.parent().is("td"))
			po.editGridFormPage.form().modelform("destroy");
		$formPage.appendTo($editFormCell).show();
		
		var form = po.editGridFormPage.form();
		
		//只有一个属性，隐藏标签，否则，显示标签
		if(propertyCount == 1)
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
				
				var dataTable = po.editTable().DataTable();
				
				po.closeEditCellPanel(dataTable);
				po.saveEditCell(dataTable, propertyIndexesMap, data);
				
				return false;
			},
			addSinglePropertyValue : function(property, propertyModel)
			{
				po.editGridFormPage.addSinglePropertyValue(property, propertyModel);
			},
			editSinglePropertyValue : function(property, propertyModel)
			{
				po.editGridFormPage.editSinglePropertyValue(property, propertyModel);
			},
			deleteSinglePropertyValue : function(property, propertyModel)
			{
				po.editGridFormPage.deleteSinglePropertyValue(property, propertyModel);
			},
			selectSinglePropertyValue : function(property, propertyModel)
			{
				po.editGridFormPage.selectSinglePropertyValue(property, propertyModel);
			},
			viewSinglePropertyValue : function(property, propertyModel)
			{
				po.editGridFormPage.viewSinglePropertyValue(property, propertyModel);
			},
			editMultiplePropertyValue : function(property, propertyModel)
			{
				po.editGridFormPage.editMultiplePropertyValue(property, propertyModel);
			},
			viewMultiplePropertyValue : function(property, propertyModel)
			{
				po.editGridFormPage.viewMultiplePropertyValue(property, propertyModel);
			},
			filePropertyUploadURL : "<c:url value='/data/file/upload' />",
			filePropertyDeleteURL : "<c:url value='/data/file/delete' />",
			filePropertyReturnShowableValue : true,
			downloadSinglePropertyValueFile : function(property, propertyModel)
			{
				po.editGridFormPage.downloadSinglePropertyValueFile(property, propertyModel);
			},
			validationRequiredAsAdd : false,
			labels : po.editGridFormPage.formLabels,
			dateFormat : "<c:out value='${sqlDateFormat}' />",
			timestampFormat : "<c:out value='${sqlTimestampFormat}' />",
			timeFormat : "<c:out value='${sqlTimeFormat}' />"
		});
		
		if(propertyCount == 1 || focus)
		{
			//仅选中一个属性，激活焦点
			$(":input:not([readonly]):visible:eq(0)", form).focus();
		}
		
		$formPanel.position({ my : "left top", at : "left bottom", of : $editFormCell, within : $table});
	};
	
	//关闭编辑面板
	po.closeEditCellPanel = function(dataTable)
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
		}
		
		$(dataTable.table().node()).focus();
	};
	
	po.saveEditCell = function(dataTable, propertyIndexesMap, data)
	{
		var model = po.editGridModel;
		
		var saveCount = 0;
		
		for(var pi in propertyIndexesMap)
		{
			var pindexes = propertyIndexesMap[pi];
			var property = $.model.getProperty(model, parseInt(pi));
			var propertyValue = $.model.propertyValue(data, property.name);
			
			for(var i=0; i<pindexes.length; i++)
			{
				var index = pindexes[i];
				
				var cell = dataTable.cell(index);
				
				var originalRowData = po.originalRowData(dataTable, index.row);
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
					var $cell = $(cell.node());
					
					//undefined值会使cell.data()语义不符
					cell.data((propertyValue == undefined ? null : propertyValue)).draw();
					
					//多元属性值单元格显示“[原始元素个数]+[新加元素个数]”
					if($.model.isMultipleProperty(property) && !po.isClientDataRow(dataTable, index.row))
					{
						var originalLen = $.model.getMultiplePropertyValueLength(originalCellValue);
						var newLen = $.model.getMultiplePropertyValueLength(myPropertyValue);
						
						$cell.html(originalLen +"+"+newLen);
					}
					
					po.markAsModifiedCell($cell);
				}
				else
					po.markAsUnmodifiedCell($(cell.node()));
				
				saveCount++;
			}
		}
		
		//新值可能会影响单元格宽度，因此需要重设列宽
		if(saveCount > 0)
			dataTable.columns.adjust();
		
		//保存后的下一次选中单元格操作触发编辑
		po.editCellOnSelect = true;
	};
	
	//恢复单元格的数据
	po.restoreEditCell = function(dataTable, cells, confirmCallback, cancelCallback)
	{
		var count = cells.nodes().length;
		
		var _confirmCallback = function()
		{
			po.closeEditCellPanel(dataTable);
			
			var model = po.editGridModel;
			var settings = dataTable.settings();

			var restoreCount = 0;
			
			cells.every(function()
			{
				var index = this.index();
				
				var originalRowData = po.originalRowData(dataTable, index.row, false);
				
				if(originalRowData)
				{
					var propertyIndex = $.getDataTableCellPropertyIndex(settings, index);
					var property = $.model.getProperty(model, propertyIndex);
					var originalCellValue = $.model.propertyValue(originalRowData, property);
					
					this.data(originalCellValue).draw();
				}
				
				po.markAsUnmodifiedCell($(this.node()));
				
				restoreCount++;
			});
			
			//新值可能会影响单元格宽度，因此需要重设列宽
			if(restoreCount > 0)
				dataTable.columns.adjust();
			
			if(confirmCallback)
				confirmCallback.call(po, dataTable, cells, count);
		};
		
		var _cancelCallback = function()
		{
			if(cancelCallback)
				cancelCallback.call(po, dataTable, cells, count);
		};
		
		if(count > 1)
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
	
	po.initEditGrid = function(model)
	{
		po.editGridModel = model;
		
		$.initButtons(po.element(".edit-grid-operation"));
		
		po.element("#${pageId}-editGridSwitch").checkboxradio({icon : true}).change(function(event)
		{
			var $thisCheckbox = $(this);
			
			if($thisCheckbox.is(":checked"))
			{
				po.enableEditGrid();
			}
			else
			{
				var dataTable = po.editTable().DataTable();
				var modifiedCells = dataTable.cells(".cell-modified");
				
				po.restoreEditCell(dataTable, modifiedCells,
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
			var dataTable = po.editTable().DataTable();
			var selectedCells = dataTable.cells(".selected");
			
			po.restoreEditCell(dataTable, selectedCells);
		});
		
		po.element(".button-restore-all", po.element(".edit-grid")).click(function()
		{
			var dataTable = po.editTable().DataTable();
			
			var modifiedCells = dataTable.cells(".cell-modified");
			
			po.restoreEditCell(dataTable, modifiedCells);
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
				var dataTable = po.editTable().DataTable();
				po.closeEditCellPanel(dataTable);
				
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
			var dataTable = po.editTable().DataTable();
			po.closeEditCellPanel(dataTable);
			
			po.editCellOnSelect = false;
		});
	};
})
(${pageId});
</script>

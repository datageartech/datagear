<%--
/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */
--%>
<%@ page import="java.sql.NClob"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="org.datagear.web.util.WebUtils"%>
<%@ page import="java.sql.Types"%>
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
	po.element().draggable({ handle : po.element(".form-panel-title") });
	po.element().hide();
	po.formLabels.submit = "<fmt:message key='confirm' />";
})
(<%=editGridFormPageId%>);
</script>
<%
WebUtils.setPageId(request, gridPageId);
%>
<script type="text/javascript">
(function(po)
{
	po.isEnableEditGrid = false;
	//存储行初始值
	po.editGridOriginalRowDatas = {};
	//存储行的指定属性值是否已从服务端加载
	po.editGridFetchedPropertyValues = {};
	//编辑表格对应的模型，会在initEditGrid函数中初始化
	po.editGridModel = undefined;
	//是否在单元格选中时编辑单元格，键盘快速导航时通常不需要打开编辑单元格面板
	po.editCellOnSelect = true;
	po.editGridFormPage = <%=editGridFormPageId%>;
	
	po.queryLeftClobLengthOnReading = <%=request.getAttribute("queryLeftClobLengthOnReading")%>;
	
	po.editGridSwitch = function()
	{
		return po.element("#${pageId}-editGridSwitch");
	};
	
	po.editGridOperation = function()
	{
		return po.element(".edit-grid-operation");
	};
	
	po.editGridOperationButtons = function()
	{
		return po.element(".edit-grid-operation button");
	};
	
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
			
			po.element("button[name='editGridEditButton']", $buttonWrapper).click(function()
			{
				var dataTable = po.table().DataTable();
				var selectedIndexes = dataTable.cells(".selected").indexes();
				
				if(selectedIndexes)
					po.editCell(dataTable, selectedIndexes, true);
			});
		}
		
		po.isEnableEditGrid = true;
		
		var dataTable = po.table().DataTable();
		$(dataTable.table().node()).attr("tabindex", 0);
		
		po.element(".head .search").addClass("ui-state-disabled");
		po.element(".foot .pagination").addClass("ui-state-disabled");
		
		var $editGridOperation = po.editGridOperation();
		//保存按钮居中
		$editGridOperation.css("right", (0 - po.element(".button-save", $editGridOperation).outerWidth(true)/2));
		po.editGridOperationButtons().show("fade");
		
		po.element(".ui-button.not-edit-grid-button", $headOperation).hide();
		po.element(".edit-grid-button-wrapper", $headOperation).show("fade", function()
		{
			//防止快速点击复选框导致都显示出来
			if(!po.isEnableEditGrid)
				$(this).hide();
		});
	};
	
	po.disableEditGrid = function()
	{
		var dataTable = po.table().DataTable();
		dataTable.cells(".selected").deselect();
		
		$(dataTable.table().node()).removeAttr("tabindex");
		
		var $headOperation = po.element(".head .operation");
		
		po.element(".head .search").removeClass("ui-state-disabled");
		po.element(".foot .pagination").removeClass("ui-state-disabled");
		
		po.editGridOperationButtons().hide("fade");
		po.element(".edit-grid-button-wrapper", $headOperation).hide();
		po.element(".ui-button.not-edit-grid-button", $headOperation).show("fade", function()
		{
			//防止快速点击复选框导致都显示出来
			if(po.isEnableEditGrid)
				$(this).hide();
		});
		
		po.isEnableEditGrid = false;
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
	
	//判断单元格是否需要从服务端加载数据
	po.needFetchPropertyValue = function(dataTable, cellIndex, property, propertyValue)
	{
		var re = false;
		
		if(po.queryLeftClobLengthOnReading == null || po.queryLeftClobLengthOnReading < 0)
			re = false;
		else
		{
			var propertyModelIndex = $.model.getPropertyModelIndexByValue(property, propertyValue);
			var jdbcType = $.model.featureJdbcTypeValue(property);
			
			if(<%=Types.CLOB%> == jdbcType || <%=Types.NCLOB%> == jdbcType
					|| <%=Types.LONGNVARCHAR%> == jdbcType || <%=Types.LONGVARCHAR%> == jdbcType)
				re = (propertyValue && propertyValue.length >= po.queryLeftClobLengthOnReading);
			else
				re = false;
		}
		
		if(re)
		{
			var $cell = $(dataTable.cell(cellIndex).node());
			
			if(po.fetchedPropertyValue(cellIndex.row, property.name))
				re = false;
			else if($cell.hasClass("cell-modified"))
				re = false;
		}
		
		return re;
	};
	
	//编辑单元格
	po.editCell = function(dataTable, indexes, focus)
	{
		var settings = dataTable.settings();
		
		var propertyIndexesMap = $.getDataTableCellPropertyIndexesMap(settings, indexes);
		
		var model = po.editGridModel;
		var data = {};
		
		var needFetchPropertyValueRowDataMap = undefined;
		var needFetchPropertyNamesMap = undefined;
		
		for(var pi in propertyIndexesMap)
		{
			var property = $.model.getProperty(model, parseInt(pi));
			
			var pindexes = propertyIndexesMap[pi];
			var pindex0 = pindexes[0];
			var propertyValue0 = dataTable.cell(pindex0).data();
			
			//仅从后台获取选中一行的属性值
			if(pindexes.length == 1 && po.needFetchPropertyValue(dataTable, pindex0, property, propertyValue0))
			{
				if(!needFetchPropertyValueRowDataMap)
				{
					needFetchPropertyValueRowDataMap = {};
					needFetchPropertyNamesMap = {};
				}
				
				if(!needFetchPropertyValueRowDataMap[pindex0.row])
					needFetchPropertyValueRowDataMap[pindex0.row] = po.originalRowData(dataTable, pindex0.row);
				
				var propertyNames = (needFetchPropertyNamesMap[pindex0.row] || (needFetchPropertyNamesMap[pindex0.row] = []));
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
		
		if(needFetchPropertyValueRowDataMap)
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
			
			var needFetchPropertyValueRowDatas = $.getMapKeyValueArray(needFetchPropertyValueRowDataMap, sortFunction);
			var needFetchPropertyValueRows = needFetchPropertyValueRowDatas.keys;
			var needFetchPropertyValueDatas = needFetchPropertyValueRowDatas.values;
			var needFetchPropertyNamess = $.getMapKeyValueArray(needFetchPropertyNamesMap, sortFunction).values;
			
			var param = { "datas" : needFetchPropertyValueDatas, "propertyNames" : needFetchPropertyNamess };
			$.post(po.url("getPropertyValues"), param, function(fetchedPropertyValuess)
			{
				if(fetchedPropertyValuess)
				{
					for(var i=0; i<needFetchPropertyValueRows.length; i++)
					{
						var needFetchPropertyValueRow = parseInt(needFetchPropertyValueRows[i]);
						var needFetchPropertyValueData = needFetchPropertyValueDatas[i];
						var needFetchPropertyNames = needFetchPropertyNamess[i];
						var fetchedPropertyValues = fetchedPropertyValuess[i];
						
						if(fetchedPropertyValues)
						{
							for(var j=0; j<needFetchPropertyNames.length; j++)
							{
								var fetchedPropertyValue = fetchedPropertyValues[j];
								
								if(fetchedPropertyValue)
								{
									var needFetchPropertyName = needFetchPropertyNames[j];
									
									$.model.propertyValue(data, needFetchPropertyName, fetchedPropertyValue);
									$.model.propertyValue(needFetchPropertyValueData, needFetchPropertyName, fetchedPropertyValue);
									
									var myColumn = $.getDataTableColumn(settings, needFetchPropertyName);
									var myCell = dataTable.cell({ "row" : needFetchPropertyValueRow, "column" : myColumn });
									myCell.data(fetchedPropertyValue);
									
									po.fetchedPropertyValue(needFetchPropertyValueRow, needFetchPropertyName, true);
								}
							}
						}
					}
				}
				
				po.showEditCellPanel(dataTable, indexes, propertyIndexesMap, data, focus);
			});
		}
		else
			po.showEditCellPanel(dataTable, indexes, propertyIndexesMap, data, focus);
	};
	
	po.showEditCellPanel = function(dataTable, indexes, propertyIndexesMap, data, focus)
	{
		var $table = $(dataTable.table().node());
		var $tableParent = $(dataTable.table().container());
		var $cellNodes = $(dataTable.cells(indexes).nodes());
		var $editFormCell = $($cellNodes[0]);
		
		$cellNodes.removeClass("cell-edit-form");
		$editFormCell.addClass("cell-edit-form");
		
		var propertyCount = 0;
		for(var pi in propertyIndexesMap){ propertyCount++; }
		
		var $formPage = po.editGridFormPage.element();
		var $formPanel = po.editGridFormPage.element(".form-panel");
		
		if($formPage.parent().is("td"))
			po.editGridFormPage.form().modelform("destroy");
		$formPage.appendTo($editFormCell).show();
		
		var form = po.editGridFormPage.form();
		
		//只有一个属性，隐藏标签，否则，显示标签
		if(propertyCount == 1)
		{
			$formPanel.css("min-width", $tableParent.width()/3);
			form.addClass("hide-form-label");
		}
		else
		{
			$formPanel.css("min-width", $tableParent.width()/2);
			form.removeClass("hide-form-label");
		}
		
		form.modelform(
		{
			model : po.editGridModel,
			data : data,
			propertyIndexesMap : propertyIndexesMap,
			renderProperty : function(property, propertyIndex)
			{
				return (propertyIndexesMap[propertyIndex] != undefined);
			},
			submit : function()
			{
				var $this = $(this);
				
				var data = $this.modelform("data");
				var propertyIndexesMap = $this.modelform("option", "propertyIndexesMap");
				
				var dataTable = po.table().DataTable();
				
				po.closeEditCellPanel(dataTable);
				po.saveEditCell(dataTable, propertyIndexesMap, data);
				
				return false;
			},
			addSinglePropertyValue : function(property, propertyConcreteModel)
			{
				po.editGridFormPage.addSinglePropertyValue(property, propertyConcreteModel);
			},
			editSinglePropertyValue : function(property, propertyConcreteModel)
			{
				po.editGridFormPage.editSinglePropertyValue(property, propertyConcreteModel);
			},
			deleteSinglePropertyValue : function(property, propertyConcreteModel)
			{
				po.editGridFormPage.deleteSinglePropertyValue(property, propertyConcreteModel);
			},
			selectSinglePropertyValue : function(property, propertyConcreteModel)
			{
				po.editGridFormPage.selectSinglePropertyValue(property, propertyConcreteModel);
			},
			viewSinglePropertyValue : function(property, propertyConcreteModel)
			{
				po.editGridFormPage.viewSinglePropertyValue(property, propertyConcreteModel);
			},
			editMultiplePropertyValue : function(property, propertyConcreteModel)
			{
				po.editGridFormPage.editMultiplePropertyValue(property, propertyConcreteModel);
			},
			viewMultiplePropertyValue : function(property, propertyConcreteModel)
			{
				po.editGridFormPage.viewMultiplePropertyValue(property, propertyConcreteModel);
			},
			filePropertyUploadURL : "<c:url value='/data/file/upload' />",
			filePropertyDeleteURL : "<c:url value='/data/file/delete' />",
			filePropertyReturnShowableValue : true,
			downloadSinglePropertyValueFile : function(property, propertyConcreteModel)
			{
				po.editGridFormPage.downloadSinglePropertyValueFile(property, propertyConcreteModel);
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
				
				cell.data(propertyValue).draw();
				
				var changed = true;
				
				if(propertyValue == originalCellValue)
					changed = false;
				else
				{
					var tmpPropertyValue = propertyValue;
					
					if($.model.isShowableValue(propertyValue))
						tmpPropertyValue = $.model.getShowableRawValue(propertyValue);
					
					if(tmpPropertyValue == originalCellValue)
						changed = false;
					//无原始值但是表单空字符串保存的情况
					else if((originalCellValue == null || originalCellValue == undefined)
							&& (tmpPropertyValue == "" || tmpPropertyValue == null || tmpPropertyValue == undefined))
						changed = false;
				}
				
				if(changed)
					po.markAsModifiedCell($(cell.node()));
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
			
			if(confirmCallback)
				confirmCallback.call(po, dataTable, cells, count);

			//新值可能会影响单元格宽度，因此需要重设列宽
			if(restoreCount > 0)
				dataTable.columns.adjust();
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
		
		$.initButtons(po.editGridOperation());
		
		po.editGridSwitch().checkboxradio({icon : true}).change(function(event)
		{
			var $thisCheckbox = $(this);
			
			if($thisCheckbox.is(":checked"))
			{
				po.enableEditGrid();
			}
			else
			{
				var dataTable = po.table().DataTable();
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
			var dataTable = po.table().DataTable();
			var selectedCells = dataTable.cells(".selected");
			
			po.restoreEditCell(dataTable, selectedCells);
		});
		
		po.element(".button-restore-all", po.element(".edit-grid")).click(function()
		{
			var dataTable = po.table().DataTable();
			
			var modifiedCells = dataTable.cells(".cell-modified");
			
			po.restoreEditCell(dataTable, modifiedCells);
		});
		
		po.editGridFormPage.element()
		.focusin(function()
		{
			var $this = $(this);
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
				var dataTable = po.table().DataTable();
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
			var dataTable = po.table().DataTable();
			po.closeEditCellPanel(dataTable);
			
			po.editCellOnSelect = false;
		});
		
		po.table().DataTable()
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
					
					dataTable.rows(".selected").deselect();
					
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
				else if(type == "row")
				{
					dataTable.cells(".selected").deselect();
				}
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
			}
		})
		.on("preDraw", function(event, settings)
		{
			//禁止表格重绘，比如排序
			if(po.isEnableEditGrid)
				return false;
			else
				return true;
		});
	};
})
(${pageId});
</script>

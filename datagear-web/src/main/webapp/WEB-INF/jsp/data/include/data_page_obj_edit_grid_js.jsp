<%--
/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="org.datagear.web.util.WebUtils"%>
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
	//存储单元格初始值
	po.editGridOriginalCellValues = {};
	//编辑表格对应的模型，会在initEditGrid函数中初始化
	po.editGridModel = undefined;
	
	po.editGridFormPage = <%=editGridFormPageId%>;
	
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
	
	po.hasSetOriginalCellValue = function(cellIndex)
	{
		if(!po.editGridOriginalCellValues)
			return false;
		
		var rowObj = po.editGridOriginalCellValues[cellIndex.row];
		
		if(!rowObj)
			return false;
		
		return rowObj.hasOwnProperty(cellIndex.column);
	};
	
	//获取/设置单元格初始值
	po.originalCellValue = function(cellIndex, value)
	{
		if(arguments.length == 1)
		{
			if(!po.editGridOriginalCellValues)
				return undefined;
			
			var rowObj = po.editGridOriginalCellValues[cellIndex.row];
			
			if(!rowObj)
				return undefined;
			
			return rowObj[cellIndex.column];
		}
		else if(arguments.length == 2)
		{
			if(!po.editGridOriginalCellValues)
				po.editGridOriginalCellValues = {};
			
			var rowObj = (po.editGridOriginalCellValues[cellIndex.row]
							|| (po.editGridOriginalCellValues[cellIndex.row] = {}));
			
			rowObj[cellIndex.column] = value;
		}
	};
	
	po.editedCells = function()
	{
		return po.element("tbody td.edit-cell, tbody td.cell-modified", po.table());
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
					po.openEditCellPanel(dataTable, selectedIndexes, true);
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
	
	//打开编辑面板
	po.openEditCellPanel = function(dataTable, indexes, focus)
	{
		var $table = $(dataTable.table().node());
		var $tableParent = $(dataTable.table().container());
		var settings = dataTable.settings();
		
		var $cellNodes = $(dataTable.cells(indexes).nodes());
		var $editFormCell = $($cellNodes[0]);
		var propertyIndexesMap = $.getDataTablesColumnPropertyIndexesMap(settings, indexes);
		var propertyCount = 0;
		for(var pi in propertyIndexesMap){ propertyCount++; }
		
		$cellNodes.removeClass("cell-edit-form");
		$editFormCell.addClass("cell-edit-form");
		
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
		
		var model = po.editGridModel;
		var data = {};
		
		for(var pi in propertyIndexesMap)
		{
			var pindexes = propertyIndexesMap[pi];
			
			//仅赋值仅有一行选中的属性值
			if(pindexes.length == 1)
			{
				var property = $.model.getProperty(model, parseInt(pi));
				var propertyValue = dataTable.cell(pindexes[0]).data();
				
				$.model.propertyValue(data, property.name, propertyValue);
			}
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
			validationRequiredAsAdd: false,
			labels : po.editGridFormPage.formLabels
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
		
		for(var pi in propertyIndexesMap)
		{
			var pindexes = propertyIndexesMap[pi];
			var property = $.model.getProperty(model, parseInt(pi));
			var propertyValue = $.model.propertyValue(data, property.name);
			
			for(var i=0; i<pindexes.length; i++)
			{
				var index = pindexes[i];
				
				var cell = dataTable.cell(index);
				
				var originalValue = undefined;
				if(po.hasSetOriginalCellValue(index))
					originalValue = po.originalCellValue(index);
				else
				{
					originalValue = cell.data();
					po.originalCellValue(index, originalValue);
				}
				
				cell.data(propertyValue).draw();
				
				if(propertyValue == originalValue)
					po.markAsUnmodifiedCell($(cell.node()));
				else
					po.markAsModifiedCell($(cell.node()));
			}
		}
	};
	
	//恢复单元格的数据
	po.restoreEditCell = function(dataTable, cells)
	{
		po.closeEditCellPanel(dataTable);
		
		cells.every(function()
		{
			var index = this.index();
			
			if(po.hasSetOriginalCellValue(index))
			{
				var originalValue = po.originalCellValue(index);
				this.data(originalValue).draw();
				
				po.markAsUnmodifiedCell($(this.node()));
			}
		});
	};
	
	po.initEditGrid = function(model)
	{
		po.editGridModel = model;
		
		$.initButtons(po.editGridOperation());
		
		po.editGridSwitch().checkboxradio({icon : false}).click(function(event)
		{
			var $thisCheckbox = $(this);
			
			if($(this).is(":checked"))
			{
				po.enableEditGrid();
			}
			else
			{
				var dataTable = po.table().DataTable();
				
				var modifiedCells = dataTable.cells(".cell-modified");
				var count = modifiedCells.nodes().length;
				
				if(count > 1)
				{
					event.preventDefault();
					event.stopPropagation();
					
					po.confirm("<fmt:message key='data.confirmCancelAllEditedCell'><fmt:param>"+count+"</fmt:param></fmt:message>",
					{
						"confirm" : function()
						{
							po.restoreEditCell(dataTable, modifiedCells);
							po.disableEditGrid();
							
							$thisCheckbox.attr("checked", false);
							$thisCheckbox.checkboxradio("refresh");
						},
						"cancel" : function()
						{
						}
					});
				}
				else
				{
					po.disableEditGrid();
				}
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
			var count = modifiedCells.nodes().length;
			
			if(count > 1)
			{
				po.confirm("<fmt:message key='data.confirmCancelAllEditedCell'><fmt:param>"+count+"</fmt:param></fmt:message>",
				{
					"confirm" : function()
					{
						po.restoreEditCell(dataTable, modifiedCells);
					}
				});
			}
			else
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
				}
				else if(event.keyCode == $.ui.keyCode.ENTER)
				{
					//必须加下面这行代码，不然当打开的编辑面板表单输入框自动焦点时，会触发表单提交事件
					event.preventDefault();
					
					var selectedIndexes = dataTable.cells(".selected").indexes();
					
					if(selectedIndexes)
						po.openEditCellPanel(dataTable, selectedIndexes, true);
				}
				else
					$.handleCellNavigationForKeydown(dataTable, event);
			}
		})
		.on("select", function(event, dataTable, type, indexes)
		{
			if(po.isEnableEditGrid)
			{
				if(type == "cell")
				{
					po.openEditCellPanel(dataTable, indexes);
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

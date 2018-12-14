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
		}
		
		po.isEnableEditGrid = true;
		
		var dataTable = po.table().DataTable();
		$(dataTable.table().node()).attr("tabindex", 0);
		
		po.element(".head .search").addClass("ui-state-disabled");
		po.element(".foot .pagination").addClass("ui-state-disabled");
		
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
		
		$("<div class='cell-midified-tip ui-state-error'><span class='ui-icon ui-icon-triangle-1-sw' /></div>").appendTo($cell);
	};

	po.markAsUnmodifiedCell = function($cell)
	{
		if($cell.hasClass("cell-modified"))
			$cell.removeClass("cell-modified");
		
		var $cmt = $(".cell-midified-tip", $cell);
		if($cmt.length > 0)
			$cmt.remove();
	};
	
	//打开编辑面板
	po.openEditCellPanel = function(table, indexes)
	{
		var settings = table.settings();
		
		var $cellNodes = $(table.cells(indexes).nodes());
		var $editFormCell = $($cellNodes[0]);
		var propertyIndexes = $.getDataTablesColumnPropertyIndexes(settings, indexes);
		
		$cellNodes.removeClass("cell-edit-form");
		$editFormCell.addClass("cell-edit-form");
		
		var $formPage = po.editGridFormPage.element();
		
		if(!$formPage.parent().is(po.element(".foot")))
			po.editGridFormPage.form().modelform("destroy");
		
		$formPage.appendTo($editFormCell).show();
		
		var form = po.editGridFormPage.form();
		
		//只有一个属性，隐藏标签，否则，显示标签
		if(propertyIndexes.length == 1)
			form.addClass("hide-form-label");
		else
			form.removeClass("hide-form-label");
		
		form.modelform(
		{
			model : po.editGridModel,
			renderProperty : function(property, propertyIndex)
			{
				return ($.inArray(propertyIndex, propertyIndexes) >= 0);
			},
			submit : function()
			{
				console.log("save cells");
				
				var table = po.table().DataTable();
				po.closeEditCellPanel(table);
				
				return false;
			},
			labels : po.editGridFormPage.formLabels
		});
		
		//仅选中一个单元格，激活焦点
		if(indexes.length == 1)
			$(":input:not([readonly]):visible:eq(0)", form).focus();
	};
	
	//关闭编辑面板
	po.closeEditCellPanel = function(table, deselectCellIndexes)
	{
		if(deselectCellIndexes)
			$(table.cells(deselectCellIndexes).nodes()).removeClass("cell-edit-form");
		
		var $formPage = po.editGridFormPage.element();
		
		var $foot = po.element(".foot");
		if(!$formPage.parent().is($foot))
		{
			$formPage.hide();

			$formPage.css("left", 0);
			$formPage.css("top", 0);
			
			po.editGridFormPage.form().modelform("destroy");
			
			$formPage.appendTo($foot);
		}
		
		$(table.table().node()).focus();
	};
	
	//恢复单元格的数据
	po.restoreEditCell = function(table, $cells)
	{
		
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
				var $editedCells = po.editedCells();
				
				if($editedCells.length > 1)
				{
					event.preventDefault();
					event.stopPropagation();
					
					po.confirm("<fmt:message key='data.confirmCancelAllEditedCell'><fmt:param>"+$editedCells.length+"</fmt:param></fmt:message>",
					{
						"confirm" : function()
						{
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
			var table = po.table().DataTable();
			var selectedCells = table.cells(".selected");
			
			po.restoreEditCell(table, selectedCells);
		});
		
		po.element(".button-restore-all", po.element(".edit-grid")).click(function()
		{
			var table = po.table().DataTable();
			
			var modifiedCells = table.cells(".modified-cell");
			var count = modifiedCells.nodes().length;
			
			if(count > 1)
			{
				po.confirm("<fmt:message key='data.confirmCancelAllEditedCell'><fmt:param>"+count+"</fmt:param></fmt:message>",
				{
					"confirm" : function()
					{
						po.restoreEditCell(table, modifiedCells);
					}
				});
			}
			else
				po.restoreEditCell(table, modifiedCells);
		});
		
		po.editGridFormPage.element()
		.focusin(function()
		{
			var $this = $(this);
			$this.addClass("focus");
		})
		.focusout(function()
		{
			var $this = $(this);
			$this.removeClass("focus");
		});
		
		po.editGridFormPage.element(".form-panel")
		.keydown(function(event)
		{
			if(event.keyCode == $.ui.keyCode.ESCAPE)
			{
				var table = po.table().DataTable();
				po.closeEditCellPanel(table);
			}
			
			//禁止冒泡，因为这些快捷键在表格上有特殊处理逻辑
			if(event.keyCode == $.ui.keyCode.ESCAPE || event.keyCode == $.ui.keyCode.ENTER
					|| event.keyCode == $.ui.keyCode.DOWN || event.keyCode == $.ui.keyCode.UP
					|| event.keyCode == $.ui.keyCode.LEFT|| event.keyCode == $.ui.keyCode.RIGHT)
			{
				event.stopPropagation();
			}
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
					var table = $(this).DataTable();
					
					table.rows(".selected").deselect();
					$.handleCellSelectionForClick(table, event, target);
				}
			}
		})
		.on("keydown", function(event)
		{
			if(po.isEnableEditGrid)
			{
				var table = $(this).DataTable();
				
				if(event.keyCode == $.ui.keyCode.ESCAPE)
				{
					po.closeEditCellPanel(table);
				}
				else if(event.keyCode == $.ui.keyCode.ENTER)
				{
					event.preventDefault();
					
					var selectedIndexes = table.cells(".selected").indexes();
					
					if(selectedIndexes)
						po.openEditCellPanel(table, selectedIndexes);
				}
				else
					$.handleCellNavigationForKeydown(table, event);
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
					var $selectedCells = dataTable.cells(".selected").nodes();
					
					if($selectedCells.length == 0)
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

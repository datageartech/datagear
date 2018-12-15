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
	po.openEditCellPanel = function(dataTable, indexes)
	{
		var $table = $(dataTable.table().node());
		var $tableParent = $(dataTable.table().container());
		var settings = dataTable.settings();
		
		var $cellNodes = $(dataTable.cells(indexes).nodes());
		var $editFormCell = $($cellNodes[0]);
		var propertyIndexes = $.getDataTablesColumnPropertyIndexes(settings, indexes);
		
		$cellNodes.removeClass("cell-edit-form");
		$editFormCell.addClass("cell-edit-form");
		
		var $formPage = po.editGridFormPage.element();
		var $formPanel = po.editGridFormPage.element(".form-panel");
		
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
				
				var dataTable = po.table().DataTable();
				po.closeEditCellPanel(dataTable);
				
				return false;
			},
			validationRequiredAsAdd: false,
			labels : po.editGridFormPage.formLabels
		});
		
		if(propertyIndexes.length == 1)
		{
			//仅选中一个属性，激活焦点
			$(":input:not([readonly]):visible:eq(0)", form).focus();
			$formPanel.css("min-width", $tableParent.width()/3);
		}
		else
		{
			$formPanel.css("min-width", $tableParent.width()/2);
		}
		
		$formPanel.position({ my : "left top", at : "left bottom", of : $editFormCell, within : $table});
	};
	
	//关闭编辑面板
	po.closeEditCellPanel = function(dataTable, deselectCellIndexes)
	{
		if(deselectCellIndexes)
			$(dataTable.cells(deselectCellIndexes).nodes()).removeClass("cell-edit-form");
		
		var $formPage = po.editGridFormPage.element();
		
		var $foot = po.element(".foot");
		if(!$formPage.parent().is($foot))
		{
			$formPage.hide();
			
			if($formPage.hasClass("focus"))
				$formPage.removeClass("focus");
			
			po.editGridFormPage.form().modelform("destroy");
			
			$formPage.appendTo($foot);
		}
		
		$(dataTable.table().node()).focus();
	};
	
	//恢复单元格的数据
	po.restoreEditCell = function(dataTable, $cells)
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
			var dataTable = po.table().DataTable();
			var selectedCells = dataTable.cells(".selected");
			
			po.restoreEditCell(dataTable, selectedCells);
		});
		
		po.element(".button-restore-all", po.element(".edit-grid")).click(function()
		{
			var dataTable = po.table().DataTable();
			
			var modifiedCells = dataTable.cells(".modified-cell");
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
					//必须加下面这行代码，不然当打开的编辑面板表单是单一属性且输入框自动焦点时，会触发表单提交事件
					event.preventDefault();
					
					var selectedIndexes = dataTable.cells(".selected").indexes();
					
					if(selectedIndexes)
						po.openEditCellPanel(dataTable, selectedIndexes);
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

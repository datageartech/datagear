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
	
	po.currentEditCell = undefined;
	
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
		po.isEnableEditGrid = false;
		po.cancelAllEditCell();

		var dataTable = po.table().DataTable();
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
	
	po.beginEditCell = function($cell)
	{
		if($cell.is(po.currentEditCell))
			return;
		else if(po.currentEditCell != null)
			po.cancelEditCell(po.currentEditCell);
		
		po.currentEditCell = $cell;
		
		$cell.addClass("edit-cell ui-state-highlight");
		
		var text = $cell.text();
		var originalText = $cell.attr("original-text");
		
		if(originalText == undefined)
		{
			$cell.attr("original-text", text);
			originalText = text;
		}
		
		if(text != originalText)
			po.markAsModifiedCell($cell);
		else
			po.markAsUnmodifiedCell($cell);
		
		var cellIndex = $cell.index();
		var settings = po.table().DataTable().settings();
		var cellProperty = $.getDataTablesColumnProperty(po.editGridModel, settings, cellIndex);
		
		po.editGridFormPage.element().appendTo($cell).show().position({my: "left top", at: "left bottom"});
		
		po.editGridFormPage.form().modelform(
		{
			model : po.editGridModel,
			renderProperty : function(property)
			{
				return property == cellProperty;
			},
			submit : function()
			{
				alert("save cell");
				return false;
			},
			labels : po.editGridFormPage.formLabels
		});
	};
	
	po.storeEditCell = function($cell, value)
	{
		po.cancelEditCell($cell, value);
	};
	
	po.cancelEditCell = function($cell, newText)
	{
		$cell.removeClass("edit-cell ui-state-highlight");
		
		var originalText = $cell.attr("original-text");
		var text = newText;
		
		if(text == undefined)
			text = originalText;
		
		if(text != originalText)
			po.markAsModifiedCell($cell);
		else
			po.markAsUnmodifiedCell($cell);
		
		po.currentEditCell = null;
		
		var editGridFormPageEle = po.editGridFormPage.element();
		
		if(editGridFormPageEle.parent().is($cell))
		{
			po.editGridFormPage.form().modelform("destroy");
			
			editGridFormPageEle.hide();
			editGridFormPageEle.appendTo(po.element());
		}
	};
	
	po.cancelAllEditCell = function($editedCells)
	{
		if(!$editedCells)
			$editedCells = po.editedCells();
		
		$editedCells.each(function()
		{
			po.cancelEditCell($(this));
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
				var $editedCells = po.editedCells();
				
				if($editedCells.length > 1)
				{
					event.preventDefault();
					event.stopPropagation();
					
					po.confirm("<fmt:message key='data.confirmCancelAllEditedCell'><fmt:param>"+$editedCells.length+"</fmt:param></fmt:message>",
					{
						"confirm" : function()
						{
							po.cancelAllEditCell($editedCells);
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
					po.cancelAllEditCell($editedCells);
					po.disableEditGrid();
				}
			}
		});
		
		po.element(".button-cancel", po.element(".edit-grid")).click(function()
		{
			if(po.currentEditCell != null)
				po.cancelAllEditCell(po.currentEditCell);
		});
		
		po.element(".button-cancel-all", po.element(".edit-grid")).click(function()
		{
			var $editedCells = po.editedCells();
			
			if($editedCells.length > 1)
			{
				po.confirm("<fmt:message key='data.confirmCancelAllEditedCell'><fmt:param>"+$editedCells.length+"</fmt:param></fmt:message>",
				{
					"confirm" : function()
					{
						po.cancelAllEditCell($editedCells);
					}
				});
			}
			else
				po.cancelAllEditCell($editedCells);
		});
		
		po.table().DataTable()
		.on("click", function(event)
		{
			if(po.isEnableEditGrid)
			{
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
				
				$.handleCellNavigationForKeydown(table, event);
			}
		})
		.on("select", function(event, dataTable, type, indexes)
		{
			if(po.isEnableEditGrid)
			{
				if(type == "cell")
				{
					var $cells = $(dataTable.cells(indexes).nodes());
				}
				else if(type == "row")
				{
					dataTable.cells(".selected").deselect();
				}
			}
			
			console.log("select");
		})
		.on("deselect", function(event, dataTable, type, indexes)
		{
			if(po.isEnableEditGrid)
			{
				if(type == "cell")
				{
					var $cells = $(dataTable.cells(indexes).nodes());
				}
			}
			
			console.log("deselect");
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

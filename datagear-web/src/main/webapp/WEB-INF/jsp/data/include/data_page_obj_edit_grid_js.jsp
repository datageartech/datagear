<%--
/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%--
编辑表格功能JS片段。

依赖：
page_obj_grid.jsp
data_page_obj.jsp
data_page_obj_edit_grid_html.jsp

变量：

--%>
<script type="text/javascript">
(function(pageObj)
{
	pageObj.isEnableEditGrid = false;
	
	pageObj.editGridSwitch = function()
	{
		return pageObj.element("#${pageId}-editGridSwitch");
	};
	
	pageObj.editGridOperation = function()
	{
		return pageObj.element(".edit-grid-operation");
	};
	
	pageObj.editGridOperationButtons = function()
	{
		return pageObj.element(".edit-grid-operation button");
	};
	
	pageObj.editedCells = function()
	{
		return pageObj.element("tbody td.edit-cell, tbody td.cell-modified", pageObj.table);
	};
	
	pageObj.enableEditGrid = function()
	{
		var $headOperation = pageObj.element(".head .operation");
		
		if(pageObj.element(".edit-grid-button", $headOperation).length == 0)
		{
			pageObj.element(".ui-button", $headOperation).addClass("not-edit-grid-button");
			
			var $buttonWrapper = $("<div class='edit-grid-button-wrapper' style='display:inline-block;' />").appendTo($headOperation);
			$("<button name='editGridAddButton' class='edit-grid-button highlight'><fmt:message key='add' /></button>&nbsp;"
				+"<button name='editGridEditButton' class='edit-grid-button highlight'><fmt:message key='edit' /></button>&nbsp;"
				+"<button name='editGridDeleteButton' class='edit-grid-button highlight'><fmt:message key='delete' /></button>").appendTo($buttonWrapper);
			
			$.initButtons($buttonWrapper);
			
			$buttonWrapper.hide();
		}
		
		pageObj.isEnableEditGrid = true;

		pageObj.element(".head .search").addClass("ui-state-disabled");
		pageObj.element(".foot .pagination").addClass("ui-state-disabled");
		
		pageObj.editGridOperationButtons().show("fade");
		pageObj.element(".ui-button.not-edit-grid-button", $headOperation).hide();
		pageObj.element(".edit-grid-button-wrapper", $headOperation).show("fade", function()
		{
			//防止快速点击复选框导致都显示出来
			if(!pageObj.isEnableEditGrid)
				$(this).hide();
		});
	};
	
	pageObj.disableEditGrid = function()
	{
		pageObj.isEnableEditGrid = false;
		pageObj.cancelAllEditCell();
		
		var $headOperation = pageObj.element(".head .operation");

		pageObj.element(".head .search").removeClass("ui-state-disabled");
		pageObj.element(".foot .pagination").removeClass("ui-state-disabled");
		
		pageObj.editGridOperationButtons().hide("fade");
		pageObj.element(".edit-grid-button-wrapper", $headOperation).hide();
		pageObj.element(".ui-button.not-edit-grid-button", $headOperation).show("fade", function()
		{
			//防止快速点击复选框导致都显示出来
			if(pageObj.isEnableEditGrid)
				$(this).hide();
		});
	};
	
	pageObj.markAsModifiedCell = function($cell)
	{
		if(!$cell.hasClass("cell-modified"))
			$cell.addClass("cell-modified");
		
		$("<div class='cell-midified-tip ui-state-error'><span class='ui-icon ui-icon-triangle-1-sw' /></div>").appendTo($cell);
	};

	pageObj.markAsUnmodifiedCell = function($cell)
	{
		if($cell.hasClass("cell-modified"))
			$cell.removeClass("cell-modified");
		
		var $cmt = $(".cell-midified-tip", $cell);
		if($cmt.length > 0)
			$cmt.remove();
	};
	
	pageObj.beginEditCell = function($cell)
	{
		$cell.addClass("edit-cell");
		
		var text = $cell.text();
		var originalText = $cell.attr("original-text");
		
		if(originalText == undefined)
		{
			$cell.attr("original-text", text);
			originalText = text;
		}
		
		$cell.empty();
		
		if(text != originalText)
			pageObj.markAsModifiedCell($cell);
		else
			pageObj.markAsUnmodifiedCell($cell);
		
		var $input = $("<input type='text' class='edit-cell-input ui-widget ui-widget-content' />")
			.attr("value", text).css("width", $cell.width()-5).appendTo($cell);
		
		$input.on("blur", function(event)
		{
			pageObj.storeEditCell($cell, $(this).val());
		});
		
		$input.focus();
	};
	
	pageObj.storeEditCell = function($cell, value)
	{
		pageObj.cancelEditCell($cell, value);
	};
	
	pageObj.cancelEditCell = function($cell, newText)
	{
		$cell.removeClass("edit-cell");
		
		var originalText = $cell.attr("original-text");
		var text = newText;
		
		if(text == undefined)
			text = originalText;
		
		$cell.empty().text(text);
		
		if(text != originalText)
			pageObj.markAsModifiedCell($cell);
		else
			pageObj.markAsUnmodifiedCell($cell);
	};
	
	pageObj.cancelAllEditCell = function($editedCells)
	{
		if(!$editedCells)
			$editedCells = pageObj.editedCells();
		
		$editedCells.each(function()
		{
			pageObj.cancelEditCell($(this));
		});
	};
	
	pageObj.initEditGrid = function()
	{
		$.initButtons(pageObj.editGridOperation());
		
		pageObj.editGridSwitch().checkboxradio({icon : false}).click(function(event)
		{
			var $thisCheckbox = $(this);
			
			if($(this).is(":checked"))
			{
				pageObj.enableEditGrid();
			}
			else
			{
				var $editedCells = pageObj.editedCells();
				
				if($editedCells.length > 1)
				{
					event.preventDefault();
					event.stopPropagation();
					
					pageObj.confirm("<fmt:message key='data.confirmCancelAllEditedCell'><fmt:param>"+$editedCells.length+"</fmt:param></fmt:message>",
					{
						"confirm" : function()
						{
							pageObj.cancelAllEditCell($editedCells);
							pageObj.disableEditGrid();
							
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
					pageObj.cancelAllEditCell($editedCells);
					pageObj.disableEditGrid();
				}
			}
		});
		
		pageObj.table.DataTable().on("click.dt", function(event, settings)
		{
			if(pageObj.isEnableEditGrid)
			{
				event.stopPropagation();
				
				var target = $(event.target);
				
				if(target.is("td"))
				{
					pageObj.beginEditCell(target);
				}
			}
			else
			{
				
			}
		});
		
		pageObj.element(".button-cancel-all", pageObj.element(".edit-grid")).click(function()
		{
			var $editedCells = pageObj.editedCells();
			
			if($editedCells.length > 1)
			{
				pageObj.confirm("<fmt:message key='data.confirmCancelAllEditedCell'><fmt:param>"+$editedCells.length+"</fmt:param></fmt:message>",
				{
					"confirm" : function()
					{
						pageObj.cancelAllEditCell($editedCells);
					}
				});
			}
			else
				pageObj.cancelAllEditCell($editedCells);
		});
	};
})
(${pageId});
</script>

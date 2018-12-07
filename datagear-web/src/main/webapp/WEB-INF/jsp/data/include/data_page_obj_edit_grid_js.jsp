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
	
	pageObj.editGridSwitch().checkboxradio({icon:false}).click(function()
	{
		var $headOperation = pageObj.element(".head .operation");
		
		if(pageObj.element(".edit-grid-button", $headOperation).length == 0)
		{
			pageObj.element(".ui-button", $headOperation).addClass("not-edit-grid-button");
			
			var $buttonWrapper = $("<div class='edit-grid-button-wrapper' style='display:inline-block;' />").appendTo($headOperation);
			$("<button name='editGridAddButton' class='edit-grid-button highlight'><fmt:message key='add' /></button>&nbsp;"
				+"<button name='editGridEditButton' class='edit-grid-button highlight'><fmt:message key='edit' /></button>&nbsp;"
				+"<button name='editGridDeleteButton' class='edit-grid-button highlight'><fmt:message key='delete' /></button>").button().appendTo($buttonWrapper);
			
			$buttonWrapper.hide();
		}
		
		var $thisCheckbox = $(this);
		
		if($(this).is(":checked"))
		{
			pageObj.editGridOperationButtons().show("fade");
			pageObj.element(".ui-button.not-edit-grid-button", $headOperation).hide();
			pageObj.element(".edit-grid-button-wrapper", $headOperation).show("fade", function()
			{
				//防止快速点击复选框导致都显示出来
				if(!$thisCheckbox.is(":checked"))
					$(this).hide();
			});
			pageObj.element(".head .search").addClass("ui-state-disabled");
			pageObj.element(".foot .pagination").addClass("ui-state-disabled");
		}
		else
		{
			pageObj.editGridOperationButtons().hide("fade");
			pageObj.element(".edit-grid-button-wrapper", $headOperation).hide();
			pageObj.element(".ui-button.not-edit-grid-button", $headOperation).show("fade", function()
			{
				//防止快速点击复选框导致都显示出来
				if($thisCheckbox.is(":checked"))
					$(this).hide();
			});
			pageObj.element(".head .search").removeClass("ui-state-disabled");
			pageObj.element(".foot .pagination").removeClass("ui-state-disabled");
		}
	});
	
	$.initButtons(pageObj.editGridOperation());
})
(${pageId});
</script>

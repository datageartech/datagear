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
		if($(this).is(":checked"))
		{
			pageObj.editGridOperationButtons().show("fade");
			pageObj.element(".head .operation .ui-button").addClass("ui-state-highlight");
			pageObj.element(".head .search").addClass("ui-state-disabled");
			pageObj.element(".foot .pagination").addClass("ui-state-disabled");
		}
		else
		{
			pageObj.editGridOperationButtons().hide("fade");
			pageObj.element(".head .operation .ui-button").removeClass("ui-state-highlight");
			pageObj.element(".head .search").removeClass("ui-state-disabled");
			pageObj.element(".foot .pagination").removeClass("ui-state-disabled");
		}
	});
	
	$.initButtons(pageObj.editGridOperation());
})
(${pageId});
</script>

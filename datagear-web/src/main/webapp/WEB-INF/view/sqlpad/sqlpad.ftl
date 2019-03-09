<#include "../include/import_global.ftl">
<#include "../include/html_doctype.ftl">
<#--
Schema schema 数据库，不允许为null
-->
<html style="height:100%;">
<head>
<#include "../include/html_head.ftl">
<title>
	<#include "../include/html_title_app_name.ftl">
	<@spring.message code='sqlpad.sqlpad' />
	<@spring.message code='bracketLeft' />
	${schema.title?html}
	<@spring.message code='bracketRight' />
</title>
</head>
<body style="height:100%;">
<#if !isAjaxRequest>
<div style="height:99%;">
</#if>
<div id="${pageId}" class="page-sqlpad">
	<div class="head">
		head
	</div>
	<div class="content ui-widget ui-widget-content">
		<div class="content-editor">
			<div class="content-edit-content">
				<p>select * from project;</p>
				<p>select * from project;</p>
				<p>select * from project;</p>
				<p>select * from project;</p>
				<p>select * from project;</p>
				<p>select * from project;</p>
				<p>select * from project;</p>
				<p>select * from project;</p>
			</div>
		</div>
		<div class="content-result">
			<p>update ok</p>
			<p>update ok</p>
			<p>update ok</p>
			<p>update ok</p>
		</div>
	</div>
	<div class="foot">
		foot
	</div>
</div>
<#if !isAjaxRequest>
</div>
</#if>

<#include "../include/page_js_obj.ftl">
<script type="text/javascript">
(function(po)
{
	$.initButtons(po.element(".operation"));
	
	$.resizableStopPropagation(po.element(".content-editor"),
	{
		containment : "parent",
		handles : "s",
		classes : { "ui-resizable-s" : "ui-widget-header" },
		resize : function(event, ui)
		{
			var parent = ui.element.parent();
			var parentHeight = parent.height();
			var editorHeight = ui.element.height();
			var editorHeightPercent =  (editorHeight/parentHeight * 100) + "%";
			var resultHeightpercent = ((parentHeight-editorHeight)/parentHeight * 100) + "%";
			
			ui.element.css("height", editorHeightPercent);
			$(".content-result", parent).css("height", resultHeightpercent);
			
			return false;
		}
	});
})
(${pageId});
</script>
</body>
</html>

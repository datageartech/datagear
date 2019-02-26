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
	<div class="content">
		<div class="content-editor ui-widget ui-widget-content">
			select * from project;
		</div>
		<div class="content-result ui-widget ui-widget-content">
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
	
	po.element(".content-editor").resizable(
	{
		containment : "parent",
		handles : "s",
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

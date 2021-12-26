<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#include "../include/import_global.ftl">
<#include "../include/html_doctype.ftl">
<#--
titleMessageKey 标题标签I18N关键字，不允许null
-->
<html>
<head>
<#include "../include/html_head.ftl">
<title><#include "../include/html_title_app_name.ftl"><@spring.message code='${titleMessageKey}' /> - ${template}</title>
</head>
<body>
<#include "../include/page_js_obj.ftl" >
<div id="${pageId}" class="page-dashboard-edit-tpl-v fill-parent">
	<div style="display:none;">
		<textarea id="showContent">${(templateContents.showContent)!''}</textarea>
		<textarea id="sourceContent">${(templateContents.sourceContent)!''}</textarea>
	</div>
	<iframe id="${pageId}-sourceIframe" class="source-iframe"></iframe>
	<iframe id="${pageId}-showIframe" class="show-iframe fill-parent"></iframe>
</div>
<#include "../include/page_obj_form.ftl">
<script type="text/javascript">
(function(po)
{
	po.getIframeDocument = function($iframe)
	{
		$iframe = $iframe[0];
		return ($iframe.contentDocument || $iframe.contentWindow.document);
	}
	
	po.evalTopWindowSize = function()
	{
		var topWindow = window;
		while(topWindow.parent  && topWindow.parent != topWindow)
			topWindow = topWindow.parent;
		
		var size =
		{
			width: $(topWindow).width(),
			height: $(topWindow).height()
		};
		
		return size;
	};
	
	var showContent = po.element("#showContent").val();
	var sourceContent = po.element("#sourceContent").val();
	
	var showIframe = po.element("#${pageId}-showIframe");
	var sourceIframe = po.element("#${pageId}-sourceIframe");
	
	var topSize = po.evalTopWindowSize();
	showIframe.css("width", topSize.width);
	showIframe.css("height", topSize.height);
	
	po.getIframeDocument(showIframe).write(showContent);
	po.getIframeDocument(sourceIframe).write(sourceContent);
})
(${pageId});
</script>
</body>
</html>
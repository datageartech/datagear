<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#--
代码编辑器JS片段。

依赖：
page_js_obj.ftl

-->
<script type="text/javascript">
(function(po)
{
	po.createCodeEditor = function(dom, options)
	{
		options = (options || {});
		options.theme = "custom";
		
		return CodeMirror(dom, options);
	};
})
(${pageId});
</script>

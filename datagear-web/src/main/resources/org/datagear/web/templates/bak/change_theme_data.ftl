<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#include "include/page_import.ftl">
<#assign _td_Version=statics['org.datagear.util.Global'].VERSION>
[
	{
		"type": "css",
		"selector" : "#css_jquery_ui",
		"attr" : "href",
		"value" : "${contextPath}/static/theme/<@spring.theme code='theme' />/jquery-ui-1.12.1/jquery-ui.css"
	},
	{
		"type": "css",
		"selector" : "#css_jquery_ui_theme",
		"attr" : "href",
		"value" : "${contextPath}/static/theme/<@spring.theme code='theme' />/jquery-ui-1.12.1/jquery-ui.theme.css"
	},
	{
		"type": "css",
		"selector" : "#css_common",
		"attr" : "href",
		"value" : "${contextPath}/static/theme/<@spring.theme code='theme' />/style.css?v=${_td_Version}"
	},
	{
		"type": "css",
		"selector" : "#css_codemirror",
		"attr" : "href",
		"value" : "${contextPath}/static/theme/<@spring.theme code='theme' />/codemirror-5.64.0/custom.css?v=${_td_Version}"
	}
]
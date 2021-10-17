<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#include "include/import_global.ftl">
[
	{
		"selector" : "#css_jquery_ui",
		"attr" : "href",
		"value" : "${contextPath}/static/theme/<@spring.theme code='theme' />/jquery-ui-1.12.1/jquery-ui.css"
	},
	{
		"selector" : "#css_jquery_ui_theme",
		"attr" : "href",
		"value" : "${contextPath}/static/theme/<@spring.theme code='theme' />/jquery-ui-1.12.1/jquery-ui.theme.css"
	},
	{
		"selector" : "#css_common",
		"attr" : "href",
		"value" : "${contextPath}/static/theme/<@spring.theme code='theme' />/style.css"
	},
	{
		"selector" : "#css_ace",
		"attr" : "href",
		"value" : "${contextPath}/static/theme/<@spring.theme code='theme' />/ace-21.02.19/ace.css"
	}
]
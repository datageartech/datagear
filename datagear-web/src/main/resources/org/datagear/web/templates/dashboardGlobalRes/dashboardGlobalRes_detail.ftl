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
<html>
<head>
<#include "../include/html_head.ftl">
<title><#include "../include/html_title_app_name.ftl">查看看板全局资源</title>
</head>
<body>
<div id="${pageId}" class="page-form page-form-dashboardGlobalResDetail">
	<form id="${pageId}-form" action="#" method="POST">
		<div class="form-head"></div>
		<div class="form-content">
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='dashboardGlobalRes.path' /></label>
				</div>
				<div class="form-item-value">
					<input type="text" class="ui-widget ui-widget-content" value="${dashboardGlobalResItem.path}" />
				</div>
			</div>
		</div>
		<div class="form-foot" style="text-align:center;">
		</div>
	</form>
</div>
<#include "../include/page_js_obj.ftl" >
<#include "../include/page_obj_form.ftl">
<script type="text/javascript">
(function(po)
{
})
(${pageId});
</script>
</body>
</html>
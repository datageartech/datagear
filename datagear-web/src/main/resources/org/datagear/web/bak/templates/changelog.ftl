<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#include "include/page_import.ftl">
<#include "include/html_doctype.ftl">
<html>
<head>
<#include "include/html_head.ftl">
<title><#include "include/html_title_app_name.ftl"><@spring.message code='changelog.changelog' /></title>
</head>
<body>
<#include "include/page_obj.ftl" >
<div id="${pageId}" class="page-changelog">
	<#if !isAjaxRequest>
	<div class="main-page-head ui-widget ui-widget-content">
		<#include "include/html_logo.ftl">
	</div>
	</#if>
	<div class="changelogs">
		<form id="${pageId}-form">
			<div class="form-content">
				<#list versionChangelogs as versionChangelog>
				<div class="form-item form-item-version">
					<div class="form-item-label">
						<label><@spring.message code='changelog.version' /></label>
					</div>
					<div class="form-item-value">
						${versionChangelog.version}
					</div>
				</div>
				<div class="form-item">
					<div class="form-item-label">
						<label></label>
					</div>
					<div class="form-item-value">
						<ul class="changelog-content">
						<#list versionChangelog.contents as item>
						<li class="changelog-item">${item}</li>
						</#list>
						</ul>
					</div>
				</div>
				</#list>
			</div>
			<div class="form-foot">
				<#if !(allListed??) || allListed == false>
				<a href="${contextPath}/changelogs" class="link" target="_blank"><@spring.message code='changelog.viewAll' /></a>
				</#if>
			</div>
		</form>
	</div>
</div>
<#include "include/page_obj_form.ftl">
<script type="text/javascript">
(function(po)
{
	if(po.isInDialog())
	{
		var windowHeight = $(window).height();
		var maxHeight = windowHeight - windowHeight/4;
		po.element(".form-content").css("max-height", maxHeight+"px").css("overflow", "auto");
	}
})
(${pageId});
</script>
</body>
</html>
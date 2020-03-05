<#include "include/import_global.ftl">
<#include "include/html_doctype.ftl">
<#assign Global=statics['org.datagear.util.Global']>
<html>
<head>
<#include "include/html_head.ftl">
<title><#include "include/html_title_app_name.ftl"><@spring.message code='about.about' /></title>
</head>
<body>
<div id="${pageId}" class="page page-about">
	<form id="${pageId}-form">
		<div class="form-content">
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='about.app.name' /></label>
				</div>
				<div class="form-item-value">
					<@spring.message code='app.name' />
				</div>
			</div>
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='about.app.version' /></label>
				</div>
				<div class="form-item-value">
					${version}
				</div>
			</div>
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='about.app.website' /></label>
				</div>
				<div class="form-item-value">
					<a href="${Global.WEB_SITE}" target="_blank" class="link">${Global.WEB_SITE}</a>
				</div>
			</div>
		</div>
	</form>
</div>
<#include "include/page_js_obj.ftl" >
<#include "include/page_obj_form.ftl">
<script type="text/javascript">
(function(po)
{
})
(${pageId});
</script>
</body>
</html>
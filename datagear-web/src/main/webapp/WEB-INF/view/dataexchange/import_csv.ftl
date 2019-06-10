<#include "../include/import_global.ftl">
<#include "../include/html_doctype.ftl">
<#--
Schema schema 数据库，不允许为null
-->
<html>
<head>
<#include "../include/html_head.ftl">
<title>
	<#include "../include/html_title_app_name.ftl">
	<@spring.message code='dataimport.dataimport' />
	<@spring.message code='bracketLeft' />
	${schema.title?html}
	<@spring.message code='bracketRight' />
</title>
</head>
<body class="fill-parent">
<#if !isAjaxRequest>
<div class="fill-parent">
</#if>
<div id="${pageId}" class="page-dataimport-csv">
	<div class="head">
		导入CSV数据
	</div>
	<div class="content">
		<form id="${pageId}-form" action="#" method="POST">
			<div class="form-content">
				<h3>设置数据格式</h3>
				<div>
					<div class="form-item">
						<div class="form-item-label">日期格式</div>
						<div class="form-item-value">
							<input type="text" class="ui-widget ui-widget-content" />
						</div>
					</div>
					<div class="form-item">
						<div class="form-item-label">时间格式</div>
						<div class="form-item-value">
							<input type="text" class="ui-widget ui-widget-content" />
						</div>
					</div>
					<div class="form-item">
						<div class="form-item-label">时间戳格式</div>
						<div class="form-item-value">
							<input type="text" class="ui-widget ui-widget-content" />
						</div>
					</div>
					<div class="form-item">
						<div class="form-item-label">数值格式</div>
						<div class="form-item-value">
							<input type="text" class="ui-widget ui-widget-content" />
						</div>
					</div>
					<div class="form-item">
						<div class="form-item-label">忽略不存在字段</div>
						<div class="form-item-value">
							<input type="text" class="ui-widget ui-widget-content" />
						</div>
					</div>
					<div class="form-item">
						<div class="form-item-label">列值非法时设置为NULL</div>
						<div class="form-item-value">
							<input type="text" class="ui-widget ui-widget-content" />
						</div>
					</div>
					<div class="form-item">
						<div class="form-item-label">导入出错时</div>
						<div class="form-item-value">
							<input type="text" class="ui-widget ui-widget-content" />
						</div>
					</div>
				</div>
				<h3>上传数据</h3>
				<div>
					<input type="file">
				</div>
				<h3>导入</h3>
				<div>
					<input type="file">
				</div>
			</div>
		</form>
	</div>
	<div class="foot">
	</div>
</div>
<#if !isAjaxRequest>
</div>
</#if>

<#include "../include/page_js_obj.ftl">
<script type="text/javascript">
(function(po)
{
	po.schemaId = "${schema.id}";
	po.form = po.element("#${pageId}-form");
	
	po.element(".form-content").steps(
	{
		headerTag: "h3",
		bodyTag: "div",
		labels:
		{
			previous: "<@spring.message code='wizard.previous' />",
			next: "<@spring.message code='wizard.next' />",
			finish: "<@spring.message code='wizard.finish' />"
		}
	});
})
(${pageId});
</script>
</body>
</html>

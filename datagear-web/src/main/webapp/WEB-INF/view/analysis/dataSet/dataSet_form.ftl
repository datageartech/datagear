<#include "../../include/import_global.ftl">
<#include "../../include/html_doctype.ftl">
<#--
titleMessageKey 标题标签I18N关键字，不允许null
formAction 表单提交action，允许为null
readonly 是否只读操作，允许为null
-->
<#assign formAction=(formAction!'#')>
<#assign readonly=(readonly!false)>
<#assign isAdd=(formAction == 'saveAdd')>
<html>
<head>
<#include "../../include/html_head.ftl">
<title><#include "../../include/html_title_app_name.ftl"><@spring.message code='${titleMessageKey}' /></title>
</head>
<body>
<div id="${pageId}" class="page-form page-form-dataSet">
	<form id="${pageId}-form" action="${contextPath}/analysis/dataSet/${formAction}" method="POST">
		<div class="form-head"></div>
		<div class="form-content">
			<input type="hidden" name="id" value="${(dataSet.id)!''?html}" />
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='dataSet.name' /></label>
				</div>
				<div class="form-item-value">
					<input type="text" name="name" value="${(dataSet.name)!''?html}" class="ui-widget ui-widget-content" />
				</div>
			</div>
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='dataSet.dataSource' /></label>
				</div>
				<div class="form-item-value">
					<input type="text" name="schemaConnectionFactory.schema.title" class="ui-widget ui-widget-content" value="${(dataSet.schemaConnectionFactory.schema.title)!''?html}" readonly="readonly" />
					<input type="hidden" name="schemaConnectionFactory.schema.id" class="ui-widget ui-widget-content" value="${(dataSet.schemaConnectionFactory.schema.id)!''?html}" />
					<#if !readonly>
					<button type="button" class="select-schema-button"><@spring.message code='select' /></button>
					</#if>
				</div>
			</div>
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='dataSet.sql' /></label>
				</div>
				<div class="form-item-value">
					<textarea name="sql" class="ui-widget ui-widget-content" style="display:none;">${(dataSet.sql)!''?html}</textarea>
					<div class="sql-editor-wrapper ui-widget ui-widget-content">
						<div id="${pageId}-sql-editor" class="sql-editor"></div>
					</div>
				</div>
			</div>
		</div>
		<div class="form-foot" style="text-align:center;">
			<#if !readonly>
			<input type="submit" value="<@spring.message code='save' />" class="recommended" />
			&nbsp;&nbsp;
			<input type="reset" value="<@spring.message code='reset' />" />
			</#if>
		</div>
	</form>
</div>
<#include "../../include/page_js_obj.ftl" >
<#include "../../include/page_obj_form.ftl">
<#include "../../include/page_obj_sqlEditor.ftl">
<script type="text/javascript">
(function(po)
{
	$.initButtons(po.element());
	po.element(".sql-editor-wrapper").height($(window).height()/5*2);
	
	po.url = function(action)
	{
		return "${contextPath}/analysis/dataSet/" + action;
	};
	
	po.getSqlEditorSchemaId = function(){ return po.element("input[name='schemaConnectionFactory.schema.id']").val(); };
	po.initSqlEditor();
	var cursor = po.sqlEditor.getCursorPosition();
	po.sqlEditor.session.insert(cursor, po.element("textarea[name='sql']").val());
	
	<#if !readonly>
	po.element(".select-schema-button").click(function()
	{
		var options =
		{
			pageParam :
			{
				submit : function(schema)
				{
					po.element("input[name='schemaConnectionFactory.schema.title']").val(schema.title);
					po.element("input[name='schemaConnectionFactory.schema.id']").val(schema.id);
				}
			}
		};
		
		$.setGridPageHeightOption(options);
		
		po.open("${contextPath}/schema/select", options);
	});
	
	$.validator.addMethod("dataSetSqlRequired", function(value, element)
	{
		var sql = po.sqlEditor.getValue();
		return sql.length > 0;
	});
	
	po.form().validate(
	{
		ignore : "",
		rules :
		{
			"name" : "required",
			"schemaConnectionFactory.schema.title" : "required",
			"sql" : "dataSetSqlRequired",
		},
		messages :
		{
			"name" : "<@spring.message code='validation.required' />",
			"schemaConnectionFactory.schema.title" : "<@spring.message code='validation.required' />",
			"sql" : "<@spring.message code='validation.required' />"
		},
		submitHandler : function(form)
		{
			po.element("textarea[name='sql']").val(po.sqlEditor.getValue());
			
			$(form).ajaxSubmit(
			{
				success : function()
				{
					var close = (po.pageParamCall("afterSave")  != false);
					
					if(close)
						po.close();
				}
			});
		},
		errorPlacement : function(error, element)
		{
			error.appendTo(element.closest(".form-item-value"));
		}
	});
	</#if>
})
(${pageId});
</script>
</body>
</html>
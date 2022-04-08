<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#include "../include/page_import.ftl">
<#include "../include/html_doctype.ftl">
<#--
titleMessageKey 标题标签I18N关键字，不允许null
formAction 表单提交action，允许为null
readonly 是否只读操作，允许为null
-->
<#assign formAction=(formAction!'#')>
<#assign readonly=(readonly!false)>
<#assign isAdd=(formAction == 'saveAddForSQL')>
<html>
<head>
<#include "../include/html_head.ftl">
<title><#include "../include/html_title_app_name.ftl">
	<@spring.message code='${titleMessageKey}' /> - <@spring.message code='dataSet.dataSetType.SQL' />
</title>
</head>
<body>
<#include "../include/page_obj.ftl" >
<div id="${pageId}" class="page-form page-form-dataSet">
	<form id="${pageId}form" action="#" method="POST">
		<div class="form-head"></div>
		<div class="form-content">
			<#include "include/dataSet_form_html_name.ftl">
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='dataSet.dataSource' /></label>
				</div>
				<div class="form-item-value">
					<input type="text" name="shmConFactory.schema.title" class="ui-widget ui-widget-content ui-corner-all" value="${(dataSet.connectionFactory.schema.title)!''}" readonly="readonly" />
					<input type="hidden" name="shmConFactory.schema.id" class="ui-widget ui-widget-content ui-corner-all" value="${(dataSet.connectionFactory.schema.id)!''}" />
					<#if !readonly>
					<button type="button" class="select-schema-button"><@spring.message code='select' /></button>
					</#if>
				</div>
			</div>
			<div class="workspace">
				<div class="form-item error-newline">
					<div class="form-item-label">
						<label title="<@spring.message code='dataSet.sql.desc' />">
							<@spring.message code='dataSet.sql' />
						</label>
					</div>
					<div class="form-item-value">
						<textarea name="sql" class="ui-widget ui-widget-content ui-corner-all" style="display:none;">${(dataSet.sql)!''}</textarea>
						<div class="workspace-editor-wrapper ui-widget ui-widget-content ui-corner-all">
							<div id="${pageId}-workspaceEditor" class="workspace-editor code-editor"></div>
						</div>
					</div>
				</div>
				<#include "include/dataSet_form_html_wow.ftl" >
			</div>
		</div>
		<div class="form-foot">
			<#if !readonly>
			<input type="submit" value="<@spring.message code='save' />" class="recommended" />
			<#else>
			<div class="form-foot-placeholder">&nbsp;</div>
			</#if>
		</div>
	</form>
	<#include "include/dataSet_form_html_preview_pvp.ftl" >
</div>
<#include "../include/page_obj_form.ftl">
<#include "../include/page_obj_codeEditor.ftl" >
<#include "../include/page_obj_sqlEditor.ftl">
<#include "include/dataSet_form_js.ftl">
<script type="text/javascript">
(function(po)
{
	po.dataSetProperties = <@writeJson var=dataSetProperties />;
	po.dataSetParams = <@writeJson var=dataSetParams />;
	
	po.element(".preview-result-table-wrapper .refresh-button")
	.after("&nbsp;<button type='button' class='export-button ui-button ui-corner-all ui-widget ui-button-icon-only' title='<@spring.message code='sqlpad.exportSqlResult' />'>"
			+"<span class='ui-button-icon ui-icon ui-icon-arrowthick-1-ne'></span>"
			+"<span class='ui-button-icon-space'> </span><@spring.message code='sqlpad.exportSqlResult' /></button>");
	
	po.initFormBtns();
	po.initMtableModelInput();
	po.element().autoCloseSubPanel();
	po.initAnalysisProject("${((dataSet.analysisProject.id)!'')?js_string?no_esc}", "${((dataSet.analysisProject.name)!'')?js_string?no_esc}");
	po.initWorkspaceHeight();
	
	po.getDataSetSchemaId = function(){ return po.element("input[name='shmConFactory.schema.id']").val(); };

	po.element(".select-schema-button").click(function()
	{
		var options =
		{
			pageParam :
			{
				select : function(schema)
				{
					po.element("input[name='shmConFactory.schema.title']").val(schema.title);
					po.element("input[name='shmConFactory.schema.id']").val(schema.id);
				}
			}
		};
		
		$.setGridPageHeightOption(options);
		
		po.open("${contextPath}/schema/select", options);
	});
	
	po.getSqlEditorSchemaId = function(){ return po.getDataSetSchemaId(); };
	po.sqlEditor = po.createWorkspaceEditor(po.element("#${pageId}-workspaceEditor"),
			po.inflateSqlEditorOptions(
			{
				value: po.element("textarea[name='sql']").val(),
				readOnly: ${readonly?string("true","false")}
			})
	);
	
	po.initWorkspaceTabs();
	po.getAddPropertyName = function()
	{
		return po.getSelectedCodeText(po.sqlEditor);
	};
	po.initParamPropertyDataFormat(po.dataSetParams, po.dataSetProperties);

	po.updatePreviewOptionsData = function()
	{
		var schemaId = po.getDataSetSchemaId();
		var sql = po.getCodeText(po.sqlEditor);
		
		var dataSet = po.previewOptions.data.dataSet;
		
		dataSet.sql = sql;
		po.previewOptions.data.schemaId = schemaId;
	};
	
	<#if formAction != 'saveAddForSQL'>
	//编辑、查看操作应初始化为已完成预览的状态
	po.updatePreviewOptionsData();
	po.previewSuccess(true);
	</#if>
	
	po.isPreviewValueModified = function()
	{
		var schemaId = po.getDataSetSchemaId();
		var sql = po.getCodeText(po.sqlEditor);
		
		var pd = po.previewOptions.data.dataSet;
		
		return (pd.sql != sql || po.previewOptions.data.schemaId != schemaId || po.isPreviewParamPropertyDataFormatModified());
	};
	
	po.previewOptions.url = po.url("previewSql");
	po.previewOptions.beforePreview = function()
	{
		po.updatePreviewOptionsData();
		
		if(!this.data.dataSet.sql || !this.data.schemaId)
			return false;
	};
	po.previewOptions.beforeRefresh = function()
	{
		if(!this.data.dataSet.sql || !this.data.schemaId)
			return false;
	};
	
	po.initPreviewOperations();
	
	po.element(".preview-result-table-wrapper .export-button").click(function(event)
	{
		var schemaId = po.getDataSetSchemaId();
		var sql = po.getCodeText(po.sqlEditor);
		
		if(!schemaId || !sql)
			return;
		
		if(po.hasFormDataSetParam())
		{
			//避免设置参数面板被隐藏
			event.stopPropagation();
			po.showDataSetParamValuePanel(
			{
				submit: function(formData)
				{
					po.exportDataSetData(schemaId, sql, po.getFormDataSetParams(), formData);
				}
			});
		}
		else
		{
			po.exportDataSetData(schemaId, sql);
		}
	});
	
	po.exportDataSetData = function(schemaId, sql, dataSetParams, paramValues)
	{
		var data =
		{
			sql: sql,
			dataSetParams: (dataSetParams || []),
			paramValues: (paramValues || {})
		};
		
		$.postJson(po.url("resolveSql"), data, function(sql)
		{
			var options = {data: {"initSqls": sql}};
			$.setGridPageHeightOption(options);
			po.open("${contextPath}/dataexchange/"+schemaId+"/export", options);
		});
	};
	
	$.validator.addMethod("dataSetSqlRequired", function(value, element)
	{
		var sql = po.getCodeText(po.sqlEditor);
		return sql.length > 0;
	});
	
	$.validator.addMethod("dataSetSqlPreviewRequired", function(value, element)
	{
		return !po.isPreviewValueModified() && po.previewSuccess();
	});
	
	po.validateForm(
	{
		ignore : "",
		rules :
		{
			"name" : "required",
			"shmConFactory.schema.title" : "required",
			"sql" : {"dataSetSqlRequired": true, "dataSetSqlPreviewRequired": true}
		},
		messages :
		{
			"name" : "<@spring.message code='validation.required' />",
			"shmConFactory.schema.title" : "<@spring.message code='validation.required' />",
			"sql" :
			{
				"dataSetSqlRequired": "<@spring.message code='validation.required' />",
				"dataSetSqlPreviewRequired": "<@spring.message code='dataSet.validation.previewRequired' />"
			}
		},
		submitHandler : function(form)
		{
			var formData = $.formToJson(form);
			formData["properties"] = po.getFormDataSetProperties();
			formData["params"] = po.getFormDataSetParams();
			formData["sql"] = po.getCodeText(po.sqlEditor);
			
			$.postJson("${contextPath}/dataSet/${formAction}", formData,
			function(response)
			{
				po.pageParamCallAfterSave(true, response.data);
			});
		}
	});
})
(${pageId});
</script>
</body>
</html>
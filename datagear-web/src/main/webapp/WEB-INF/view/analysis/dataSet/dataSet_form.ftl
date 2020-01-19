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
			<div id="${pageId}-dataSetProperties" style="display: none;">
				<#if (dataSet.properties)??>
				<#list dataSet.properties as p>
					<input type='hidden' name='dataSetPropertyNames' value="${p.name?html}" />
					<input type='hidden' name='dataSetPropertyTypes' value="${p.type?html}" />
				</#list>
				</#if>
			</div>
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
				<div class="form-item-value form-item-value-sql">
					<textarea name="sql" class="ui-widget ui-widget-content" style="display:none;">${(dataSet.sql)!''?html}</textarea>
					<div class="sql-editor-wrapper ui-widget ui-widget-content">
						<div id="${pageId}-sql-editor" class="sql-editor"></div>
					</div>
					<#if !readonly>
					<div class="sql-preview-wrapper">
						<div class="operation">
							<button type="button" class="sql-preview-button" title="<@spring.message code='dataSet.sqlPreviewButtonTip' />"><@spring.message code='preview' /></button>
							<div class="operation-result">
								<button type="button" class="sql-result-more-button ui-button ui-corner-all ui-widget ui-button-icon-only" title="<@spring.message code='dataSet.loadMoreData' />"><span class="ui-button-icon ui-icon ui-icon-arrowthick-1-s"></span><span class="ui-button-icon-space"> </span><@spring.message code='sqlpad.loadMoreData' /></button>
								<button type="button" class="sql-result-refresh-button ui-button ui-corner-all ui-widget ui-button-icon-only" title="<@spring.message code='dataSet.refreshSqlResult' />"><span class="ui-button-icon ui-icon ui-icon-refresh"></span><span class="ui-button-icon-space"> </span><@spring.message code='sqlpad.refreshSqlResult' /></button>
							</div>
						</div>
						<div class="sql-result-table-wrapper minor-dataTable">
							<table id="${pageId}-sql-table" width='100%' height="100%" class='hover stripe'></table>
							<div class='no-more-data-flag ui-widget ui-widget-content' title="<@spring.message code='dataSet.noMoreData' />"></div>
						</div>
					</div>
					</#if>
				</div>
			</div>
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='dataSet.propertyLabelsText' /></label>
				</div>
				<div class="form-item-value">
					<input type="text" name="dataSetPropertyLabelsText" class="ui-widget ui-widget-content" value="${(dataSetPropertyLabelsText)!''?html}" placeholder="<@spring.message code='dataSet.propertyLabelsTextSplitByComma' />" />
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
	var sqlEditorHeight = $(window).height()/10*3;
	po.element(".sql-editor-wrapper").height(sqlEditorHeight);
	po.element(".sql-preview-wrapper").height(sqlEditorHeight);
	po.element(".form-item-value-sql").height(sqlEditorHeight + 25);
	
	po.url = function(action)
	{
		return "${contextPath}/analysis/dataSet/" + action;
	};
	
	po.getSqlEditorSchemaId = function(){ return po.element("input[name='schemaConnectionFactory.schema.id']").val(); };
	po.initSqlEditor();
	var cursor = po.sqlEditor.getCursorPosition();
	po.sqlEditor.session.insert(cursor, po.element("textarea[name='sql']").val());
	po.sqlEditor.commands.addCommand(
	{
	    name: 'sqlPreviewCommand',
	    bindKey: "Ctrl-ENTER",
	    exec: function(editor)
	    {
	    	po.element(".sql-preview-button").click();
	    }
	});
	<#if readonly>
	po.sqlEditor.setReadOnly(true);
	</#if>
	
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
	
	po.sqlTableElement = function()
	{
		return po.element("#${pageId}-sql-table");
	};
	
	po.sqlPreviewOptions =
	{
		sql : "",
		startRow : 1
	};
	
	po.element(".sql-preview-button").click(function()
	{
		var sql = po.sqlEditor.getValue();
		if(!sql)
			return;
		
		po.element(".operation-result").hide();
		
		var table = po.sqlTableElement();
		if($.isDatatTable(table))
		{
			table.DataTable().destroy();
			table.empty();
		}
		
		po.sqlPreviewOptions.schemaId = po.element("input[name='schemaConnectionFactory.schema.id']").val();
		po.sqlPreviewOptions.sql = po.sqlEditor.getValue();
		po.sqlPreviewOptions.startRow = 1;
		po.sqlPreview();
	});
	
	po.element(".sql-result-more-button").click(function()
	{
		if(po.sqlPreviewOptions.noMoreData)
			return;
		
		po.sqlPreviewOptions.startRow = po.sqlPreviewOptions.nextStartRow;
		po.sqlPreview();
	});

	po.element(".sql-result-refresh-button").click(function()
	{
		po.sqlPreviewOptions.startRow = 1;
		po.sqlPreview();
	});
	
	po.renderRowNumberColumn = function(data, type, row, meta)
	{
		var row = meta.row;
		
		if(row.length > 0)
			row = row[0];
		
		return row + 1;
	};
	
	po.calSqlResultTableHeight = function()
	{
		return po.element(".sql-result-table-wrapper").height() - 30;
	};
	
	po.sqlPreview = function()
	{
		if(!po.sqlPreviewOptions.schemaId || !po.sqlPreviewOptions.sql)
			return;
		
		po.element(".sql-preview-button").button("disable");
		po.element(".sql-result-more-button").button("disable");
		po.element(".sql-result-refresh-button").button("disable");
		
		var table = po.sqlTableElement();
		var returnModel = !$.isDatatTable(table);
		var initDataTable = returnModel;
		
		var data =
		{
			"sql" : po.sqlPreviewOptions.sql,
			"startRow" : po.sqlPreviewOptions.startRow,
			"returnModel" : returnModel
		};
		
		$.ajax(
		{
			type : "POST",
			url : po.url("sqlPreview/" + po.sqlPreviewOptions.schemaId),
			data : data,
			success : function(modelSqlResult)
			{
				po.element("textarea[name='sql']").val(data.sql);
				
				var $dspWrapper = po.element("#${pageId}-dataSetProperties");
				$dspWrapper.empty();
				var dataSetProperties = (modelSqlResult.dataSetProperties || []);
				for(var i=0; i< dataSetProperties.length; i++)
				{
					var dsp = dataSetProperties[i];
					$("<input type='hidden'>").attr("name", "dataSetPropertyNames").val(dsp.name).appendTo($dspWrapper);
					$("<input type='hidden'>").attr("name", "dataSetPropertyTypes").val(dsp.type).appendTo($dspWrapper);
				}
				
				po.sqlPreviewOptions.startRow = modelSqlResult.startRow;
				po.sqlPreviewOptions.nextStartRow = modelSqlResult.nextStartRow;
				po.sqlPreviewOptions.fetchSize = modelSqlResult.fetchSize;
				
				if(initDataTable)
				{
					var model = modelSqlResult.model;
					var columns = $.buildDataTablesColumns(model);
					
					var newColumns = [
						{
							title : "<@spring.message code='rowNumber' />", data : "", defaultContent: "",
							render : po.renderRowNumberColumn, className : "column-row-number", width : "3em"
						}
					];
					newColumns = newColumns.concat(columns);
					
					var settings =
					{
						"columns" : newColumns,
						"data" : (modelSqlResult.datas ? modelSqlResult.datas : []),
						"scrollX": true,
						"scrollY" : po.calSqlResultTableHeight(),
						"autoWidth": true,
				        "scrollCollapse": true,
						"paging" : false,
						"searching" : false,
						"ordering": false,
						"select" : { style : 'os' },
					    "language":
					    {
							"emptyTable": "<@spring.message code='dataTables.noData' />",
							"zeroRecords" : "<@spring.message code='dataTables.zeroRecords' />"
						}
					};
					
					table.dataTable(settings);
				}
				else
				{
					var dataTable = table.DataTable();
					$.addDataTableData(dataTable, modelSqlResult.datas, modelSqlResult.startRow-1);
				}
				
				if(modelSqlResult.datas.length < modelSqlResult.fetchSize)
				{
					po.sqlPreviewOptions.noMoreData = true;
					po.element(".no-more-data-flag").show();
				}
				else
				{
					po.sqlPreviewOptions.noMoreData = false;
					po.element(".no-more-data-flag").hide();
				}
				
				po.element(".operation-result").show();
				po.sqlEditor.focus();
			},
			complete : function()
			{
				po.element(".sql-preview-button").button("enable");
				po.element(".sql-result-more-button").button("enable");
				po.element(".sql-result-refresh-button").button("enable");
			}
		});
	};
	
	$.validator.addMethod("dataSetSqlRequired", function(value, element)
	{
		var sql = po.sqlEditor.getValue();
		return sql.length > 0;
	});
	
	$.validator.addMethod("dataSetSqlPreviewRequired", function(value, element)
	{
		var previewSql = po.element("textarea[name='sql']").val();
		var editorSql = po.sqlEditor.getValue();
		return previewSql == editorSql;
	});
	
	po.form().validate(
	{
		ignore : "",
		rules :
		{
			"name" : "required",
			"schemaConnectionFactory.schema.title" : "required",
			"sql" : {"dataSetSqlRequired": true, "dataSetSqlPreviewRequired": true}
		},
		messages :
		{
			"name" : "<@spring.message code='validation.required' />",
			"schemaConnectionFactory.schema.title" : "<@spring.message code='validation.required' />",
			"sql" : {"dataSetSqlRequired": "<@spring.message code='validation.required' />", "dataSetSqlPreviewRequired": "<@spring.message code='dataSet.validation.previewSqlForCorrection' />"}
		},
		submitHandler : function(form)
		{
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
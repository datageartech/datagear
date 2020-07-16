<#include "../../include/import_global.ftl">
<#include "../../include/html_doctype.ftl">
<#assign DataType=statics['org.datagear.analysis.DataSetParam$DataType']>
<#assign InputType=statics['org.datagear.analysis.DataSetParam$InputType']>
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
	<form id="${pageId}-form" action="#" method="POST">
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
					<input type="text" name="schemaConnectionFactory.schema.title" class="ui-widget ui-widget-content" value="${(dataSet.connectionFactory.schema.title)!''?html}" readonly="readonly" />
					<input type="hidden" name="schemaConnectionFactory.schema.id" class="ui-widget ui-widget-content" value="${(dataSet.connectionFactory.schema.id)!''?html}" />
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
					<div class="sql-operation-wrapper">
						<ul>
							<li><a href="#${pageId}-sql-result"><@spring.message code='preview' /></a></li>
							<li><a href="#${pageId}-sql-params">参数</a></li>
						</ul>
						<div id="${pageId}-sql-result" class="sql-result-table-wrapper minor-dataTable">
							<div class="operation">
								<button type="button" class="sql-preview-button ui-button ui-corner-all ui-widget ui-button-icon-only" title="<@spring.message code='dataSet.sqlPreviewButtonTip' />"><span class="ui-button-icon ui-icon ui-icon-play"></span><span class="ui-button-icon-space"> </span><@spring.message code='preview' /></button>
								<button type="button" class="sql-result-more-button ui-button ui-corner-all ui-widget ui-button-icon-only" title="<@spring.message code='dataSet.loadMoreData' />"><span class="ui-button-icon ui-icon ui-icon-arrowthick-1-s"></span><span class="ui-button-icon-space"> </span><@spring.message code='sqlpad.loadMoreData' /></button>
								<button type="button" class="sql-result-refresh-button ui-button ui-corner-all ui-widget ui-button-icon-only" title="<@spring.message code='dataSet.refreshSqlResult' />"><span class="ui-button-icon ui-icon ui-icon-refresh"></span><span class="ui-button-icon-space"> </span><@spring.message code='sqlpad.refreshSqlResult' /></button>
								<button type="button" class="sql-result-export-button ui-button ui-corner-all ui-widget ui-button-icon-only" title="<@spring.message code='sqlpad.exportSqlResult' />"><span class="ui-button-icon ui-icon ui-icon-arrowthick-1-ne"></span><span class="ui-button-icon-space"> </span><@spring.message code='sqlpad.exportSqlResult' /></button>
							</div>
							<table id="${pageId}-sql-result-table" width='100%' class='hover stripe'></table>
							<div class='no-more-data-flag ui-widget ui-widget-content' title="<@spring.message code='dataSet.noMoreData' />"></div>
							<div class="sql-result-sql"><textarea class="ui-widget ui-widget-content ui-corner-all"></textarea></div>
						</div>
						<div id="${pageId}-sql-params" class="sql-params-table-wrapper minor-dataTable">
							<div class="operation">
								<#if !readonly>
								<button type="button" class="sql-add-param-button ui-button ui-corner-all ui-widget ui-button-icon-only" title="<@spring.message code='add' />"><span class="ui-button-icon ui-icon ui-icon-plus"></span><span class="ui-button-icon-space"> </span><@spring.message code='add' /></button>
								<button type="button" class="sql-del-param-button ui-button ui-corner-all ui-widget ui-button-icon-only" title="<@spring.message code='delete' />"><span class="ui-button-icon ui-icon ui-icon-close"></span><span class="ui-button-icon-space"> </span><@spring.message code='delete' /></button>
								</#if>
							</div>
							<table id="${pageId}-sql-params-table" class='hover stripe'></table>
						</div>
					</div>
				</div>
			</div>
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='dataSet.propertyLabelsText' /></label>
				</div>
				<div class="form-item-value">
					<input type="text" name="propertyLabelsText" class="ui-widget ui-widget-content" value="${(dataSet.propertyLabelsText)!''?html}" placeholder="<@spring.message code='dataSet.propertyLabelsTextSplitByComma' />" />
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
	<div class="sql-param-value-panel ui-widget ui-widget-content ui-corner-all ui-widget-shadow ui-front">
		<div class="ui-widget-header ui-corner-all"><@spring.message code='dataSet.setSqlParamValue' /></div>
		<div class="sql-param-value-panel-content"></div>
	</div>
</div>
<#include "../../include/page_js_obj.ftl" >
<#include "../../include/page_obj_form.ftl">
<#include "../../include/page_obj_sqlEditor.ftl">
<script type="text/javascript">
(function(po)
{
	po.dataSetProperties = <@writeJson var=dataSetProperties />;
	po.dataSetParams = <@writeJson var=dataSetParams />;
	
	$.initButtons(po.element());
	var sqlEditorHeight = parseInt($(window).height()/11*5);
	po.element(".sql-editor-wrapper").height(sqlEditorHeight);
	po.element(".sql-operation-wrapper").height(sqlEditorHeight);
	po.element(".form-item-value-sql").height(sqlEditorHeight + 25);
	
	po.url = function(action)
	{
		return "${contextPath}/analysis/dataSet/" + action;
	};
	
	po.getDataSetSchemaId = function(){ return po.element("input[name='schemaConnectionFactory.schema.id']").val(); };
	
	po.getSqlEditorSchemaId = function(){ return po.getDataSetSchemaId(); };
	po.initSqlEditor();
	var cursor = po.sqlEditor.getCursorPosition();
	po.sqlEditor.session.insert(cursor, po.element("textarea[name='sql']").val());
	po.sqlEditor.commands.addCommand(
	{
	    name: 'sqlPreviewCommand',
	    bindKey: "Ctrl-ENTER",
	    exec: function(editor)
	    {
	    	var activeIndex = po.element(".sql-operation-wrapper").tabs("option", "active");
	    	
	    	if(activeIndex == 0)
	    		po.element(".sql-preview-button").click();
	    	else
	    		po.element(".sql-operation-wrapper").tabs("option", "active", 0);
	    }
	});
	<#if readonly>
	po.sqlEditor.setReadOnly(true);
	</#if>
	
	po.isSqlModified = function(textareaValue, editorValue)
	{
		if(textareaValue == undefined)
			textareaValue = po.element("textarea[name='sql']").val();
		if(editorValue == undefined)
			editorValue = po.sqlEditor.getValue();
		
		textareaValue = textareaValue.replace(/\s/g, '');
		editorValue = editorValue.replace(/\s/g, '');
		
		return textareaValue != editorValue;
	};
	
	po.sqlParamsTableElement = function()
	{
		return po.element("#${pageId}-sql-params-table");
	};
	
	po.sqlResultTableElement = function()
	{
		return po.element("#${pageId}-sql-result-table");
	};
	
	po.calSqlOperationTableHeight = function()
	{
		return po.element(".sql-result-table-wrapper").height() - 30;
	};
	
	po.element(".sql-operation-wrapper").tabs(
	{
		activate: function(event, ui)
		{
			var isSqlResultTab = (ui.newPanel && ui.newPanel.hasClass("sql-result-table-wrapper"));
			if(isSqlResultTab && (po.isSqlModified() || !po.sqlResultTableElement().hasClass("sql-result-table-inited")))
			{
				//避免设置参数面板被隐藏
				event.stopPropagation();
				po.element(".sql-preview-button").click();
			}
			else
			{
				po.sqlParamsTableElement().DataTable().columns.adjust().fixedColumns().relayout();
			}
		}
	});
	
	po.sqlParamsTableElement().dataTable(
	{
		"columns" :
		[
			$.dataTableUtil.buildCheckCloumn("<@spring.message code='select' />"),
			{
				title: "<@spring.message code='dataSet.DataSetParam.name' />",
				data: "name",
				render: function(data, type, row, meta)
				{
					return "<input type='text' value='"+$.escapeHtml(data)+"' class='dataSetParamName input-in-table ui-widget ui-widget-content' />";
				},
				width: "8em",
				defaultContent: "",
				orderable: true
			},
			{
				title: "<@spring.message code='dataSet.DataSetParam.type' />",
				data: "type",
				render: function(data, type, row, meta)
				{
					data = (data || "${DataType.STRING}");
					
					return "<select class='dataSetParamType input-in-table ui-widget ui-widget-content'>"
							+"<option value='${DataType.STRING}' "+(data == "${DataType.STRING}" ? "selected='selected'" : "")+"><@spring.message code='dataSet.DataSetParam.DataType.STRING' /></option>"
							+"<option value='${DataType.NUMBER}' "+(data == "${DataType.NUMBER}" ? "selected='selected'" : "")+"><@spring.message code='dataSet.DataSetParam.DataType.NUMBER' /></option>"
							+"<option value='${DataType.BOOLEAN}' "+(data == "${DataType.BOOLEAN}" ? "selected='selected'" : "")+"><@spring.message code='dataSet.DataSetParam.DataType.BOOLEAN' /></option>"
							+"</select>";
				},
				width: "6em",
				defaultContent: "",
				orderable: true
			},
			{
				title: "<@spring.message code='dataSet.DataSetParam.required' />",
				data: "required",
				render: function(data, type, row, meta)
				{
					data = data + "";
					
					return "<select class='dataSetParamRequired input-in-table ui-widget ui-widget-content'>"
							+"<option value='true' "+(data != "false" ? "selected='selected'" : "")+"><@spring.message code='yes' /></option>"
							+"<option value='false' "+(data == "false" ? "selected='selected'" : "")+"><@spring.message code='no' /></option>"
							+"</select>";
				},
				width: "4em",
				defaultContent: "",
				orderable: true
			},
			{
				title: "<@spring.message code='dataSet.DataSetParam.desc' />",
				data: "desc",
				render: function(data, type, row, meta)
				{
					return "<input type='text' value='"+$.escapeHtml(data)+"' class='dataSetParamDesc input-in-table ui-widget ui-widget-content' />";
				},
				width: "6em",
				defaultContent: "",
				orderable: true
			},
			{
				title: "<@spring.message code='dataSet.DataSetParam.inputType' />",
				data: "inputType",
				render: function(data, type, row, meta)
				{
					data = (data || "${InputType.TEXT}");
					
					return "<select class='dataSetParamInputType input-in-table ui-widget ui-widget-content'>"
							+"<option value='${InputType.TEXT}' "+(data == "${InputType.TEXT}" ? "selected='selected'" : "")+"><@spring.message code='dataSet.DataSetParam.InputType.TEXT' /></option>"
							+"<option value='${InputType.SELECT}' "+(data == "${InputType.SELECT}" ? "selected='selected'" : "")+"><@spring.message code='dataSet.DataSetParam.InputType.SELECT' /></option>"
							+"<option value='${InputType.DATE}' "+(data == "${InputType.DATE}" ? "selected='selected'" : "")+"><@spring.message code='dataSet.DataSetParam.InputType.DATE' /></option>"
							+"<option value='${InputType.TIME}' "+(data == "${InputType.TIME}" ? "selected='selected'" : "")+"><@spring.message code='dataSet.DataSetParam.InputType.TIME' /></option>"
							+"<option value='${InputType.DATETIME}' "+(data == "${InputType.DATETIME}" ? "selected='selected'" : "")+"><@spring.message code='dataSet.DataSetParam.InputType.DATETIME' /></option>"
							+"<option value='${InputType.RADIO}' "+(data == "${InputType.RADIO}" ? "selected='selected'" : "")+"><@spring.message code='dataSet.DataSetParam.InputType.RADIO' /></option>"
							+"<option value='${InputType.CHECKBOX}' "+(data == "${InputType.CHECKBOX}" ? "selected='selected'" : "")+"><@spring.message code='dataSet.DataSetParam.InputType.CHECKBOX' /></option>"
							+"<option value='${InputType.TEXTAREA}' "+(data == "${InputType.TEXTAREA}" ? "selected='selected'" : "")+"><@spring.message code='dataSet.DataSetParam.InputType.TEXTAREA' /></option>"
							+"</select>";
				},
				width: "6em",
				defaultContent: "",
				orderable: true
			},
			{
				title: "<@spring.message code='dataSet.DataSetParam.inputPayload' />",
				data: "inputPayload",
				render: function(data, type, row, meta)
				{
					return "<textarea class='dataSetParamInputPayload input-in-table ui-widget ui-widget-content' style='height:2em;margin-top:0.2em;margin-bottom:0.2em;'>"+$.escapeHtml(data)+"</textarea>";
				},
				width: "20em",
				defaultContent: "",
				orderable: true
			}
		],
		data: po.dataSetParams,
		"scrollX": true,
		"autoWidth": true,
		"scrollY" : po.calSqlOperationTableHeight(),
        "scrollCollapse": false,
		"paging" : false,
		"searching" : false,
		"ordering": false,
		"fixedColumns": { leftColumns: 1 },
		"select" : { style : 'os' },
	    "language":
	    {
			"emptyTable": "<@spring.message code='dataSet.noDataSetParamDefined' />",
			"zeroRecords" : "<@spring.message code='dataSet.noDataSetParamDefined' />"
		}
	});
	
	$.dataTableUtil.bindCheckColumnEvent(po.sqlParamsTableElement().DataTable());
	
	po.element(".sql-add-param-button").click(function()
	{
		var selectionRange = po.sqlEditor.getSelectionRange();
		var selectText = (po.sqlEditor.session.getTextRange(selectionRange) || "");
		
		po.sqlParamsTableElement().DataTable().row.add({ name: selectText, type: "${DataType.STRING}", required: true, desc: "" }).draw();
	});
	
	po.element(".sql-del-param-button").click(function()
	{
		$.dataTableUtil.deleteSelectedRows(po.sqlParamsTableElement().DataTable());
	});
	
	po.sqlParamsTableElement().on("click", ".input-in-table", function(event)
	{
		//阻止行选中
		event.stopPropagation();
	});
	
	po.hasFormDataSetParam = function()
	{
		var $names = po.element(".dataSetParamName");
		return ($names.length > 0);
	};
	
	po.getFormDataSetParams = function()
	{
		var params = [];
		
		po.element(".dataSetParamName").each(function(i)
		{
			params[i] = {};
			params[i]["name"] = $(this).val();
		});
		po.element(".dataSetParamType").each(function(i)
		{
			params[i]["type"] = $(this).val();
		});
		po.element(".dataSetParamRequired").each(function(i)
		{
			params[i]["required"] = $(this).val();
		});
		po.element(".dataSetParamDesc").each(function(i)
		{
			params[i]["desc"] = $(this).val();
		});
		po.element(".dataSetParamInputType").each(function(i)
		{
			params[i]["inputType"] = $(this).val();
		});
		po.element(".dataSetParamInputPayload").each(function(i)
		{
			params[i]["inputPayload"] = $(this).val();
		});
		
		return params;
	};
	
	po.element(".select-schema-button").click(function()
	{
		var options =
		{
			pageParam :
			{
				select : function(schema)
				{
					po.element("input[name='schemaConnectionFactory.schema.title']").val(schema.title);
					po.element("input[name='schemaConnectionFactory.schema.id']").val(schema.id);
				}
			}
		};
		
		$.setGridPageHeightOption(options);
		
		po.open("${contextPath}/schema/select", options);
	});
	
	po.sqlPreviewOptions =
	{
		schemaId: "",
		sql: "",
		dataSetParams: [],
		paramValues: {},
		startRow : 1
	};
	
	po.element(".sql-param-value-panel").draggable({ handle : ".ui-widget-header" });
	
	po.showDataSetParamValuePanel = function(formOptions)
	{
		var $panel = po.element(".sql-param-value-panel");
		
		formOptions = $.extend(
		{
			submitText: "<@spring.message code='confirm' />",
			yesText: "<@spring.message code='yes' />",
			noText: "<@spring.message code='no' />",
			paramValues: chartFactory.chartForm.getDataSetParamValueObj(chartFactory.chartForm.getDataSetParamValueForm($panel)),
			render: function()
			{
				$("select, input", this).addClass("ui-widget ui-widget-content");
				$("button", this).addClass("ui-button ui-corner-all ui-widget");
			}
		},
		formOptions);
		
		chartFactory.chartForm.renderDataSetParamValueForm($(".sql-param-value-panel-content", $panel),
				po.getFormDataSetParams(), formOptions);
		
		$panel.show();
		$panel.position({ my : "right top", at : "left top", of : po.element("#${pageId}-sql-result")});
	};
	
	$(po.element()).on("click", function(event)
	{
		var $target = $(event.target);
		
		var $pvp = po.element(".sql-param-value-panel");
		if(!$pvp.is(":hidden"))
		{
			if($target.closest(".sql-param-value-panel").length == 0)
				$pvp.hide();
		}
	});
	
	po.element(".sql-preview-button").click(function(event)
	{
		var sql = po.sqlEditor.getValue();
		if(!sql)
			return;
		
		if(po.hasFormDataSetParam())
		{
			//避免设置参数面板被隐藏
			event.stopPropagation();
			
			po.showDataSetParamValuePanel(
			{
				submit: function()
				{
					var table = po.sqlResultTableElement();
					if($.isDatatTable(table))
					{
						table.DataTable().destroy();
						table.empty();
					}
					
					po.sqlPreviewOptions.schemaId = po.getDataSetSchemaId();
					po.sqlPreviewOptions.sql = po.sqlEditor.getValue();
					po.sqlPreviewOptions.dataSetParams = po.getFormDataSetParams();
					po.sqlPreviewOptions.paramValues = chartFactory.chartForm.getDataSetParamValueObj(this);
					po.sqlPreviewOptions.startRow = 1;
					po.sqlPreview();
				}
			});
			
			return;
		}
		else
		{
			po.sqlPreviewOptions.dataSetParams = [];
			po.sqlPreviewOptions.paramValues = {};
		}
		
		var table = po.sqlResultTableElement();
		if($.isDatatTable(table))
		{
			table.DataTable().destroy();
			table.empty();
		}
		
		po.sqlPreviewOptions.schemaId = po.getDataSetSchemaId();
		po.sqlPreviewOptions.sql = sql;
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

	po.element(".sql-result-export-button").click(function(event)
	{
		var schemaId = po.getDataSetSchemaId();
		var sql = po.sqlEditor.getValue();
		
		if(!schemaId || !sql)
			return;
		
		if(po.hasFormDataSetParam())
		{
			//避免设置参数面板被隐藏
			event.stopPropagation();
			po.showDataSetParamValuePanel(
			{
				submit: function()
				{
					var paramValues = chartFactory.chartForm.getDataSetParamValueObj(this);
					
					var data =
					{
						sql: sql,
						dataSetParams: po.getFormDataSetParams(),
						paramValues: paramValues
					};
					
					$.postJson(po.url("resolveSql"), data, function(sql)
					{
						var options = {data: {"initSqls": sql}};
						$.setGridPageHeightOption(options);
						po.open("${contextPath}/dataexchange/"+schemaId+"/export", options);
					});
				}
			});
		}
		else
		{
			var options = {data: {"initSqls": sql}};
			$.setGridPageHeightOption(options);
			po.open("${contextPath}/dataexchange/"+schemaId+"/export", options);
		}
	});
	
	po.renderRowNumberColumn = function(data, type, row, meta)
	{
		var row = meta.row;
		
		if(row.length > 0)
			row = row[0];
		
		return row + 1;
	};
	
	po.sqlPreview = function()
	{
		if(!po.sqlPreviewOptions.schemaId || !po.sqlPreviewOptions.sql)
			return;
		
		po.element(".sql-preview-button").button("disable");
		po.element(".sql-result-more-button").button("disable");
		po.element(".sql-result-refresh-button").button("disable");
		
		var table = po.sqlResultTableElement();
		var returnMeta = !$.isDatatTable(table);
		var initDataTable = returnMeta;
		
		var data =
		{
			schemaId: po.sqlPreviewOptions.schemaId,
			sql: po.sqlPreviewOptions.sql,
			dataSetParams: po.sqlPreviewOptions.dataSetParams,
			paramValues: po.sqlPreviewOptions.paramValues,
			startRow: po.sqlPreviewOptions.startRow,
			returnMeta: returnMeta
		};
		
		$.ajaxJson(
		{
			url : po.url("sqlPreview"),
			data : data,
			success : function(sqlResult)
			{
				po.element("textarea[name='sql']").val(data.sql);
				
				po.dataSetProperties = (sqlResult.dataSetProperties || []);
				
				po.sqlPreviewOptions.startRow = sqlResult.startRow;
				po.sqlPreviewOptions.nextStartRow = sqlResult.nextStartRow;
				po.sqlPreviewOptions.fetchSize = sqlResult.fetchSize;
				
				if(initDataTable)
				{
					var columns = $.buildDataTablesColumns(sqlResult.table);
					
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
						"data" : (sqlResult.rows ? sqlResult.rows : []),
						"scrollX": true,
						"autoWidth": true,
						"scrollY" : po.calSqlOperationTableHeight(),
				        "scrollCollapse": false,
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
					
					table.addClass("sql-result-table-inited");
					table.dataTable(settings);
					
					if(po.hasFormDataSetParam())
					{
						po.element(".sql-result-sql textarea").val(sqlResult.sql);
						po.element(".sql-result-sql").show();
					}
					else
						po.element(".sql-result-sql").hide();
				}
				else
				{
					var dataTable = table.DataTable();
					$.addDataTableData(dataTable, sqlResult.rows, sqlResult.startRow-1);
				}
				
				if(sqlResult.rows.length < sqlResult.fetchSize)
				{
					po.sqlPreviewOptions.noMoreData = true;
					po.element(".no-more-data-flag").show();
				}
				else
				{
					po.sqlPreviewOptions.noMoreData = false;
					po.element(".no-more-data-flag").hide();
				}
				
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
		return !po.isSqlModified(value);
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
			var formData = $.formToJson(form);
			formData["properties"] = po.dataSetProperties;
			formData["params"] = po.getFormDataSetParams();
			
			$.postJson("${contextPath}/analysis/dataSet/${formAction}", formData,
			function(response)
			{
				po.pageParamCallAfterSave(true, response.data);
			});
		},
		errorPlacement : function(error, element)
		{
			error.appendTo(element.closest(".form-item-value"));
		}
	});
})
(${pageId});
</script>
</body>
</html>
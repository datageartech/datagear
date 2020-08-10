<#--
数据集表单页：JS片段

依赖：
page_js_obj.ftl

变量：
//预览URL，不允许为null
po.previewOptions.url = "...";
-->
<#assign PropertyDataType=statics['org.datagear.analysis.DataSetProperty$DataType']>
<#assign ParamDataType=statics['org.datagear.analysis.DataSetParam$DataType']>
<#assign ParamInputType=statics['org.datagear.analysis.DataSetParam$InputType']>
<script type="text/javascript">
(function(po)
{
	po.url = function(action)
	{
		return "${contextPath}/analysis/dataSet/" + action;
	};
	
	po.isModifiedIgnoreBlank = function(sourceVal, targetVal)
	{
		sourceVal = sourceVal.replace(/\s/g, '');
		targetVal = targetVal.replace(/\s/g, '');
		
		return sourceVal != targetVal;
	};
	
	po.dataSetParamsTableElement = function()
	{
		return po.element("#${pageId}-dataSetParamsTable");
	};
	
	po.previewResultTableElement = function()
	{
		return po.element("#${pageId}-previewResultTable");
	};
	
	po.dataSetPropertiesTableElement = function()
	{
		return po.element("#${pageId}-dataSetPropertiesTable");
	};
	
	po.calWorkspaceOperationTableHeight = function()
	{
		return po.element(".preview-result-table-wrapper").height() - 30;
	};
	
	po.initWorkspaceHeight = function()
	{
		var height = $(window).height();
		//减去上下留白
		height = height - height/10;
		//减去对话框标题高度
		if($.isInDialog(po.element()))
			height = height - 41;
		//减去其他表单元素高度
		height = height - po.element(".form-head").outerHeight(true);
		po.element(".form-content > .form-item:not(.form-item-workspace)").each(function()
		{
			height = height - $(this).outerHeight(true);
		});
		height = height - po.element(".form-foot").outerHeight(true);
		//减去杂项高度
		height = height - 41 - 10;
		
		var errorInfoHeight = 25;
		
		po.element(".form-item-workspace .form-item-value").height(height);
		po.element(".workspace-editor-wrapper").height(height - errorInfoHeight);
		po.element(".workspace-operation-wrapper").height(height - errorInfoHeight);
	};
	
	po.initWorkspaceEditor = function(editor, initValue)
	{
		if(initValue == null)
			initValue = "";
		
		var cursor = editor.getCursorPosition();
		editor.session.insert(cursor, initValue);
		editor.commands.addCommand(
		{
		    name: 'previewCommand',
		    bindKey: "Ctrl-ENTER",
		    exec: function(editor)
		    {
		    	var $operationWrapper = po.element(".workspace-operation-wrapper");
	    		$operationWrapper.tabs("option", "active", 0);
	    		po.element(".preview-button").click();
		    }
		});
		
		<#if readonly>
		editor.setReadOnly(true);
		</#if>
	};
	
	po.initWorkspaceTabs = function()
	{
		po.element(".workspace-operation-wrapper").tabs(
		{
			activate: function(event, ui)
			{
				if(ui.newPanel.hasClass("preview-result-table-wrapper"))
				{
				}
				else if(ui.newPanel.hasClass("params-table-wrapper"))
				{
					var dataTable = po.dataSetParamsTableElement().DataTable();
					dataTable.columns.adjust();
					dataTable.fixedColumns().relayout();
				}
				else if(ui.newPanel.hasClass("properties-table-wrapper"))
				{
					var dataTable = po.dataSetPropertiesTableElement().DataTable();
					dataTable.columns.adjust();
					dataTable.fixedColumns().relayout();
				}
			}
		});
	};

	//获取用于添加数据集属性的名
	po.getAddPropertyName = function()
	{
		return "";
	};
	
	po.initDataSetPropertiesTable = function(initDataSetProperties, hideTabIfNone)
	{
		hideTabIfNone = (hideTabIfNone === undefined ? true : hideTabIfNone);
		
		po.dataSetPropertiesTableElement().dataTable(
		{
			"columns" :
			[
				$.dataTableUtil.buildCheckCloumn("<@spring.message code='select' />"),
				{
					title: "<@spring.message code='dataSet.DataSetProperty.name' />",
					data: "name",
					render: function(data, type, row, meta)
					{
						return "<input type='text' value='"+$.escapeHtml(data)+"' class='dataSetPropertyName input-in-table ui-widget ui-widget-content' />";
					},
					width: "8em",
					defaultContent: "",
					orderable: true
				},
				{
					title: "<@spring.message code='dataSet.DataSetProperty.type' />",
					data: "type",
					render: function(data, type, row, meta)
					{
						data = (data || "${PropertyDataType.STRING}");
						
						return "<select class='dataSetPropertyType input-in-table ui-widget ui-widget-content'>"
								+"<option value='${PropertyDataType.STRING}' "+(data == "${PropertyDataType.STRING}" ? "selected='selected'" : "")+"><@spring.message code='dataSet.DataSetProperty.DataType.STRING' /></option>"
								+"<option value='${PropertyDataType.INTEGER}' "+(data == "${PropertyDataType.INTEGER}" ? "selected='selected'" : "")+"><@spring.message code='dataSet.DataSetProperty.DataType.INTEGER' /></option>"
								+"<option value='${PropertyDataType.DECIMAL}' "+(data == "${PropertyDataType.DECIMAL}" ? "selected='selected'" : "")+"><@spring.message code='dataSet.DataSetProperty.DataType.DECIMAL' /></option>"
								+"<option value='${PropertyDataType.DATE}' "+(data == "${PropertyDataType.DATE}" ? "selected='selected'" : "")+"><@spring.message code='dataSet.DataSetProperty.DataType.DATE' /></option>"
								+"<option value='${PropertyDataType.TIME}' "+(data == "${PropertyDataType.TIME}" ? "selected='selected'" : "")+"><@spring.message code='dataSet.DataSetProperty.DataType.TIME' /></option>"
								+"<option value='${PropertyDataType.TIMESTAMP}' "+(data == "${PropertyDataType.TIMESTAMP}" ? "selected='selected'" : "")+"><@spring.message code='dataSet.DataSetProperty.DataType.TIMESTAMP' /></option>"
								+"<option value='${PropertyDataType.BOOLEAN}' "+(data == "${PropertyDataType.BOOLEAN}" ? "selected='selected'" : "")+"><@spring.message code='dataSet.DataSetProperty.DataType.BOOLEAN' /></option>"
								+"</select>";
					},
					width: "6em",
					defaultContent: "",
					orderable: true
				},
				{
					title: "<@spring.message code='dataSet.DataSetProperty.label' />",
					data: "label",
					render: function(data, type, row, meta)
					{
						return "<input type='text' value='"+$.escapeHtml(data)+"' class='dataSetPropertyLabel input-in-table ui-widget ui-widget-content' />";
					},
					width: "8em",
					defaultContent: "",
					orderable: true
				}
			],
			data: (initDataSetProperties || []),
			"scrollX": true,
			"autoWidth": true,
			"scrollY" : po.calWorkspaceOperationTableHeight(),
	        "scrollCollapse": false,
			"paging" : false,
			"searching" : false,
			"ordering": false,
			"fixedColumns": { leftColumns: 1 },
			"select" : { style : 'os' },
		    "language":
		    {
				"emptyTable": "<@spring.message code='dataSet.noDataSetPropertyDefined' />",
				"zeroRecords" : "<@spring.message code='dataSet.noDataSetPropertyDefined' />"
			}
		});
		
		$.dataTableUtil.bindCheckColumnEvent(po.dataSetPropertiesTableElement().DataTable());
		
		po.element(".add-property-button").click(function()
		{
			var name = (po.getAddPropertyName() || "");
			
			po.dataSetPropertiesTableElement().DataTable().row.add({ name: name, type: "${PropertyDataType.STRING}", label: "" }).draw();
		});
		
		po.element(".del-property-button").click(function()
		{
			$.dataTableUtil.deleteSelectedRows(po.dataSetPropertiesTableElement().DataTable());
		});
		
		po.dataSetPropertiesTableElement().on("click", ".input-in-table", function(event)
		{
			//阻止行选中
			event.stopPropagation();
		});
		
		if(hideTabIfNone && (initDataSetProperties == null || initDataSetProperties.length == 0))
			po.hideDataSetPropertiesTab();
	};
	
	po.showDataSetPropertiesTab = function()
	{
		po.element(".workspace-operation-nav .operation-properties").show();
	};
	
	po.hideDataSetPropertiesTab = function()
	{
		po.element(".workspace-operation-nav .operation-properties").hide();
	};
	
	po.hasFormDataSetProperty = function()
	{
		var $names = po.element(".properties-table-wrapper .dataSetPropertyName");
		return ($names.length > 0);
	};
	
	po.getFormDataSetProperties = function()
	{
		var properties = [];
		
		po.element(".properties-table-wrapper .dataSetPropertyName").each(function(i)
		{
			properties[i] = {};
			properties[i]["name"] = $(this).val();
		});
		po.element(".properties-table-wrapper .dataSetPropertyType").each(function(i)
		{
			properties[i]["type"] = $(this).val();
		});
		po.element(".properties-table-wrapper .dataSetPropertyLabel").each(function(i)
		{
			properties[i]["label"] = $(this).val();
		});
		
		return properties;
	};
	
	po.updateFormDataSetProperties = function(dataSetProperties)
	{
		dataSetProperties = (dataSetProperties || []);
		
		var dataTable = po.dataSetPropertiesTableElement().DataTable();
		$.addDataTableData(dataTable, dataSetProperties, 0);
	};
	
	//获取用于添加数据集参数的参数名
	po.getAddParamName = function()
	{
		return po.getAddPropertyName();
	};
	
	po.initDataSetParamsTable = function(initDataSetParams)
	{
		po.dataSetParamsTableElement().dataTable(
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
						data = (data || "${ParamDataType.STRING}");
						
						return "<select class='dataSetParamType input-in-table ui-widget ui-widget-content'>"
								+"<option value='${ParamDataType.STRING}' "+(data == "${ParamDataType.STRING}" ? "selected='selected'" : "")+"><@spring.message code='dataSet.DataSetParam.DataType.STRING' /></option>"
								+"<option value='${ParamDataType.NUMBER}' "+(data == "${ParamDataType.NUMBER}" ? "selected='selected'" : "")+"><@spring.message code='dataSet.DataSetParam.DataType.NUMBER' /></option>"
								+"<option value='${ParamDataType.BOOLEAN}' "+(data == "${ParamDataType.BOOLEAN}" ? "selected='selected'" : "")+"><@spring.message code='dataSet.DataSetParam.DataType.BOOLEAN' /></option>"
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
						data = (data || "${ParamInputType.TEXT}");
						
						return "<select class='dataSetParamInputType input-in-table ui-widget ui-widget-content'>"
								+"<option value='${ParamInputType.TEXT}' "+(data == "${ParamInputType.TEXT}" ? "selected='selected'" : "")+"><@spring.message code='dataSet.DataSetParam.InputType.TEXT' /></option>"
								+"<option value='${ParamInputType.SELECT}' "+(data == "${ParamInputType.SELECT}" ? "selected='selected'" : "")+"><@spring.message code='dataSet.DataSetParam.InputType.SELECT' /></option>"
								+"<option value='${ParamInputType.DATE}' "+(data == "${ParamInputType.DATE}" ? "selected='selected'" : "")+"><@spring.message code='dataSet.DataSetParam.InputType.DATE' /></option>"
								+"<option value='${ParamInputType.TIME}' "+(data == "${ParamInputType.TIME}" ? "selected='selected'" : "")+"><@spring.message code='dataSet.DataSetParam.InputType.TIME' /></option>"
								+"<option value='${ParamInputType.DATETIME}' "+(data == "${ParamInputType.DATETIME}" ? "selected='selected'" : "")+"><@spring.message code='dataSet.DataSetParam.InputType.DATETIME' /></option>"
								+"<option value='${ParamInputType.RADIO}' "+(data == "${ParamInputType.RADIO}" ? "selected='selected'" : "")+"><@spring.message code='dataSet.DataSetParam.InputType.RADIO' /></option>"
								+"<option value='${ParamInputType.CHECKBOX}' "+(data == "${ParamInputType.CHECKBOX}" ? "selected='selected'" : "")+"><@spring.message code='dataSet.DataSetParam.InputType.CHECKBOX' /></option>"
								+"<option value='${ParamInputType.TEXTAREA}' "+(data == "${ParamInputType.TEXTAREA}" ? "selected='selected'" : "")+"><@spring.message code='dataSet.DataSetParam.InputType.TEXTAREA' /></option>"
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
			data: (initDataSetParams || []),
			"scrollX": true,
			"autoWidth": true,
			"scrollY" : po.calWorkspaceOperationTableHeight(),
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
		
		$.dataTableUtil.bindCheckColumnEvent(po.dataSetParamsTableElement().DataTable());
		
		po.element(".add-param-button").click(function()
		{
			var name = (po.getAddParamName() || "");
			
			po.dataSetParamsTableElement().DataTable().row.add({ name: name, type: "${ParamDataType.STRING}", required: true, desc: "" }).draw();
		});
		
		po.element(".del-param-button").click(function()
		{
			$.dataTableUtil.deleteSelectedRows(po.dataSetParamsTableElement().DataTable());
		});
		
		po.dataSetParamsTableElement().on("click", ".input-in-table", function(event)
		{
			//阻止行选中
			event.stopPropagation();
		});
	};

	po.hasFormDataSetParam = function()
	{
		var $names = po.element(".params-table-wrapper .dataSetParamName");
		return ($names.length > 0);
	};
	
	po.getFormDataSetParams = function()
	{
		var params = [];
		
		po.element(".params-table-wrapper .dataSetParamName").each(function(i)
		{
			params[i] = {};
			params[i]["name"] = $(this).val();
		});
		po.element(".params-table-wrapper .dataSetParamType").each(function(i)
		{
			params[i]["type"] = $(this).val();
		});
		po.element(".params-table-wrapper .dataSetParamRequired").each(function(i)
		{
			params[i]["required"] = $(this).val();
		});
		po.element(".params-table-wrapper .dataSetParamDesc").each(function(i)
		{
			params[i]["desc"] = $(this).val();
		});
		po.element(".params-table-wrapper .dataSetParamInputType").each(function(i)
		{
			params[i]["inputType"] = $(this).val();
		});
		po.element(".params-table-wrapper .dataSetParamInputPayload").each(function(i)
		{
			params[i]["inputPayload"] = $(this).val();
		});
		
		return params;
	};
	
	po.initPreviewParamValuePanel = function()
	{
		po.element(".preview-param-value-panel").draggable({ handle : ".ui-widget-header" });
		
		$(po.element()).on("click", function(event)
		{
			var $target = $(event.target);
			
			var $pvp = po.element(".preview-param-value-panel");
			if(!$pvp.is(":hidden"))
			{
				if($target.closest(".preview-param-value-panel").length == 0)
					$pvp.hide();
			}
		});
	}
	
	po.showDataSetParamValuePanel = function(formOptions)
	{
		var $panel = po.element(".preview-param-value-panel");
		var $panelContent = $(".preview-param-value-panel-content", $panel);
		
		formOptions = $.extend(
		{
			submitText: "<@spring.message code='confirm' />",
			yesText: "<@spring.message code='yes' />",
			noText: "<@spring.message code='no' />",
			paramValues: chartFactory.chartForm.getDataSetParamValueObj(chartFactory.chartForm.getDataSetParamValueForm($panel)),
			render: function()
			{
				$("select, input, textarea", this).addClass("ui-widget ui-widget-content");
				$("button", this).addClass("ui-button ui-corner-all ui-widget");
			}
		},
		formOptions);
		
		$panelContent.empty();
		
		chartFactory.chartForm.renderDataSetParamValueForm($panelContent,
				po.getFormDataSetParams(), formOptions);
		
		$panel.show();
		$panel.position({ my : "right top", at : "left+5 top", of : po.element(".workspace-operation-wrapper")});
	};
	
	//预览设置项
	po.previewOptions =
	{
		//预览请求URL，必须设置
		url: null,
		//是否分页预览
		paging: false,
		//预览请求参数数据
		data:
		{
			dataSetParams: [],
			paramValues: {},
			//分页预览时的起始行
			startRow: undefined,
			//分页预览时的页大小
			fetchSize: undefined,
		},
		//预览操作前置回调函数，返回false阻止
		beforePreview: function(){},
		//分页时更多操作前置回调函数，返回false阻止
		beforeMore: function(){},
		//刷新操作前置回调函数，返回false阻止
		beforeRefresh: function(){},
		//预览请求前置回调函数，返回false阻止
		beforeRequest: function(){},
		//预览响应构建表格列数组
		buildTablesColumns: function(previewResponse)
		{
			return po.buildDataSetPropertiesColumns(previewResponse.dataSetProperties);
		},
		//预览请求成功回调函数
		success: function(previewResponse){}
	};
	
	po.destroyPreviewResultTable = function()
	{
		var table = po.previewResultTableElement();
		if($.isDatatTable(table))
		{
			table.DataTable().destroy();
			table.empty();
		}
	};
	
	po.initPreviewOperations = function()
	{
		po.element(".preview-result-table-wrapper .preview-button").click(function(event)
		{
			if(po.previewOptions.beforePreview() == false)
				return;
			
			if(po.previewOptions.paging)
				po.previewOptions.data.startRow = 1;
			
			if(po.hasFormDataSetParam())
			{
				//避免设置参数面板被隐藏
				event.stopPropagation();
				
				po.showDataSetParamValuePanel(
				{
					submit: function()
					{
						po.destroyPreviewResultTable();
						
						po.previewOptions.data.dataSetParams = po.getFormDataSetParams();
						po.previewOptions.data.paramValues = chartFactory.chartForm.getDataSetParamValueObj(this);
						
						po.executePreview();
					}
				});
			}
			else
			{
				po.destroyPreviewResultTable();
				
				po.previewOptions.data.dataSetParams = [];
				po.previewOptions.data.paramValues = {};
				
				po.executePreview();
			}
		});
		
		if(po.previewOptions.paging)
		{
			po.element(".preview-result-table-wrapper .more-button").click(function()
			{
				if(!po.previewOptions.paging)
					return;

				if(po.previewOptions._noMoreData)
					return;
				
				if(!po.previewOptions._nextStartRow)
					return;
				
				if(po.previewOptions.beforeMore() == false)
					return;
				
				po.previewOptions.data.startRow = po.previewOptions._nextStartRow;
				
				po.executePreview();
			});
		}
		else
			po.element(".preview-result-table-wrapper .more-button").hide();
		
		po.element(".preview-result-table-wrapper .refresh-button").click(function()
		{
			if(po.previewOptions.beforeRefresh() == false)
				return;
			
			if(po.previewOptions.paging)
				po.previewOptions.data.startRow = 1;
			
			po.executePreview();
		});
	};
	
	po.executePreview = function()
	{
		if(po.previewOptions.beforeRequest() == false)
			return;
		
		var $buttons = po.element(".preview-result-table-wrapper > .operation > button");
		$buttons.each(function()
		{
			$(this).button("disable");
		});
		
		var table = po.previewResultTableElement();
		var initDataTable = !$.isDatatTable(table);
		
		$.ajaxJson(
		{
			url : po.previewOptions.url,
			data : po.previewOptions.data,
			success : function(previewResponse)
			{
				//previewResponse:
				//{
				//	data: ..., resolvedSource: "...", dataSetProperties: [...],
				//	startRow: ..., nextStartRow: ..., fetchSize: ...
				//}
				
				po.updateFormDataSetProperties(previewResponse.dataSetProperties);
				po.showDataSetPropertiesTab();
				
				var tableData = (previewResponse.data || []);
				if(!$.isArray(tableData))
					tableData = [ tableData ];
				
				if(po.previewOptions.paging)
				{
					po.previewOptions.data.startRow = previewResponse.startRow;
					po.previewOptions.data.fetchSize = previewResponse.fetchSize;
					po.previewOptions._nextStartRow = previewResponse.startRow + previewResponse.fetchSize;
				}
				
				if(initDataTable)
				{
					var columns = po.previewOptions.buildTablesColumns(previewResponse);
					
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
						"data" : tableData,
						"scrollX": true,
						"autoWidth": true,
						"scrollY" : po.calWorkspaceOperationTableHeight(),
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
					
					table.addClass("preview-result-table-inited");
					table.dataTable(settings);
					
					if(previewResponse.resolvedSource)
					{
						po.element(".result-resolved-source textarea").val(previewResponse.resolvedSource);
						po.element(".result-resolved-source").show();
					}
					else
					{
						po.element(".result-resolved-source textarea").val("");
						po.element(".result-resolved-source").hide();
					}
				}
				else
				{
					var dataTable = table.DataTable();
					$.addDataTableData(dataTable, tableData, (previewResponse.startRow ? previewResponse.startRow-1 : 0));
				}
				
				if(po.previewOptions.paging && tableData.length >= previewResponse.fetchSize)
				{
					po.previewOptions._noMoreData = false;
					po.element(".no-more-data-flag").hide();
				}
				else
				{
					po.previewOptions._noMoreData = true;
					po.element(".no-more-data-flag").show();
				}
				
				po.previewOptions.success(previewResponse);
			},
			complete: function()
			{
				$buttons.each(function()
				{
					$(this).button("enable");
				});
			}
		});
	};
	
	po.renderRowNumberColumn = function(data, type, row, meta)
	{
		var row = meta.row;
		
		if(row.length > 0)
			row = row[0];
		
		return row + 1;
	};
	
	po.buildDataSetPropertiesColumns = function(dataSetProperties)
	{
		dataSetProperties = (dataSetProperties || []);
		
		var columns = [];
		
		for(var i=0; i<dataSetProperties.length; i++)
		{
			columns[i] =
			{
				title: dataSetProperties[i].name,
				data: dataSetProperties[i].name,
				defaultContent: "",
			};
		}
		
		return columns;
	};
	
	$.validator.addMethod("dataSetPropertiesRequired", function(value, element)
	{
		return po.hasFormDataSetProperty();
	});
})
(${pageId});
</script>
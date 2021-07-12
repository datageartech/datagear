<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#--
数据集表单页：JS片段

依赖：
page_js_obj.ftl

变量：
//自上次预览后，预览值是否已修改，不允许为null
po.isPreviewValueModified = function(){ return true || false; };
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
		return "${contextPath}/dataSet/" + action;
	};
	
	po.isPreviewValueModified = function()
	{
		return true;
	};
	
	po.isModifiedIgnoreBlank = function(sourceVal, targetVal)
	{
		sourceVal = (sourceVal || "");
		targetVal = (targetVal || "");
		
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

	po.dataFormatPanelElement = function()
	{
		return po.element("#${pageId}-dataFormatPanel");
	};
	
	po.calWorkspaceOperationTableHeight = function()
	{
		var tableTitleHeight = 30;
		return po.element(".preview-result-table-wrapper").height() - tableTitleHeight;
	};
	
	po.initWorkspaceHeight = function()
	{
		var windowHeight = $(window).height();
		var height = windowHeight;
		
		//减去上下留白
		height = height - height/10;
		//减去对话框标题高度
		if($.isInDialog(po.element()))
			height = height - 41;
		//减去其他表单元素高度
		height = height - po.element(".form-head").outerHeight(true);
		height = height - po.element(".form-foot").outerHeight(true);
		
		var formContentHeight = height - 41;
		
		po.element(".form-content > .form-item").each(function()
		{
			height = height - $(this).outerHeight(true);
		});
		
		//减去杂项高度
		height = height - 41 - 10;
		
		if(height < 300)
			height = 300;
		
		var errorInfoHeight = 41;
		
		po.element(".workspace").css("min-height", height+"px");
		po.element(".workspace-editor-wrapper").height(height - errorInfoHeight);
		po.element(".workspace-operation-wrapper").height(height - errorInfoHeight);
		po.element(".form-content").css("max-height", formContentHeight+"px");
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
	
	po.initWorkspaceTabs = function(disableParams)
	{
		disableParams = (disableParams == true ? true : false);
		
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
					if(ui.newTab.hasClass("ui-state-highlight"))
						ui.newTab.removeClass("ui-state-highlight");
					
					var dataTable = po.dataSetPropertiesTableElement().DataTable();
					dataTable.columns.adjust();
					dataTable.fixedColumns().relayout();
				}
			}
		});
		
		if(disableParams)
		{
			po.disableDataSetParamOperation(true);
		}
	};
	
	//获取、设置数据参数选项卡是否禁用
	po.disableDataSetParamOperation = function(disable)
	{
		var nav = $(".workspace-operation-nav", po.element(".workspace-operation-wrapper"));
		var paramsTab = $(".operation-params", nav);
		
		if(disable === undefined)
			return paramsTab.hasClass("ui-state-disabled");
		else
		{
			var paramsIndex = paramsTab.index();
			
			if(disable)
				po.element(".workspace-operation-wrapper").tabs("disable", paramsIndex);
			else
				po.element(".workspace-operation-wrapper").tabs("enable", paramsIndex);
		}
	};
	
	//获取用于添加数据集属性的名
	po.getAddPropertyName = function()
	{
		return "";
	};
	
	po.initDataSetPropertiesTable = function(initDataSetProperties)
	{
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
						var manual = row.manual;
						return "<input type='text' value='"+$.escapeHtml(data)+"'"
							+" class='dataSetPropertyName input-in-table "+(manual ? "manual" : "readonly")+" ui-widget ui-widget-content'"
							+" "+(manual ? "" : "readonly='readonly'")+" />";
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
								+"<option value='${PropertyDataType.NUMBER}' "+(data == "${PropertyDataType.NUMBER}" ? "selected='selected'" : "")+"><@spring.message code='dataSet.DataSetProperty.DataType.NUMBER' /></option>"
								+"<option value='${PropertyDataType.INTEGER}' "+(data == "${PropertyDataType.INTEGER}" ? "selected='selected'" : "")+"><@spring.message code='dataSet.DataSetProperty.DataType.INTEGER' /></option>"
								+"<option value='${PropertyDataType.DECIMAL}' "+(data == "${PropertyDataType.DECIMAL}" ? "selected='selected'" : "")+"><@spring.message code='dataSet.DataSetProperty.DataType.DECIMAL' /></option>"
								+"<option value='${PropertyDataType.DATE}' "+(data == "${PropertyDataType.DATE}" ? "selected='selected'" : "")+"><@spring.message code='dataSet.DataSetProperty.DataType.DATE' /></option>"
								+"<option value='${PropertyDataType.TIME}' "+(data == "${PropertyDataType.TIME}" ? "selected='selected'" : "")+"><@spring.message code='dataSet.DataSetProperty.DataType.TIME' /></option>"
								+"<option value='${PropertyDataType.TIMESTAMP}' "+(data == "${PropertyDataType.TIMESTAMP}" ? "selected='selected'" : "")+"><@spring.message code='dataSet.DataSetProperty.DataType.TIMESTAMP' /></option>"
								+"<option value='${PropertyDataType.BOOLEAN}' "+(data == "${PropertyDataType.BOOLEAN}" ? "selected='selected'" : "")+"><@spring.message code='dataSet.DataSetProperty.DataType.BOOLEAN' /></option>"
								+"<option value='${PropertyDataType.UNKNOWN}' "+(data == "${PropertyDataType.UNKNOWN}" ? "selected='selected'" : "")+"><@spring.message code='dataSet.DataSetProperty.DataType.UNKNOWN' /></option>"
								+"</select>";
					},
					width: "6em",
					defaultContent: "",
					orderable: true
				},
				{
					title: $.buildDataTablesColumnTitleWithTip("<@spring.message code='dataSet.DataSetProperty.label' />", "<@spring.message code='dataSet.DataSetProperty.label.desc' />"),
					data: "label",
					render: function(data, type, row, meta)
					{
						return "<input type='text' value='"+$.escapeHtml(data)+"' class='dataSetPropertyLabel input-in-table ui-widget ui-widget-content' />";
					},
					width: "8em",
					defaultContent: "",
					orderable: true
				},
				{
					title: $.buildDataTablesColumnTitleWithTip("<@spring.message code='dataSet.DataSetProperty.defaultValue' />", "<@spring.message code='dataSet.DataSetProperty.defaultValue.desc' />"),
					data: "defaultValue",
					render: function(data, type, row, meta)
					{
						return "<input type='text' value='"+$.escapeHtml(data)+"' class='dataSetPropertyDefaultValue input-in-table ui-widget ui-widget-content' />";
					},
					width: "6em",
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
			var rowData =
			{
				name: (po.getAddPropertyName() || ""),
				type: "${PropertyDataType.STRING}",
				label: "",
				manual: true
			};
			
			po.dataSetPropertiesTableElement().DataTable().row.add(rowData).draw();
		});
		
		po.element(".del-property-button").click(function()
		{
			$.dataTableUtil.deleteSelectedRows(po.dataSetPropertiesTableElement().DataTable());
		});
		
		po.element(".up-property-button").click(function()
		{
			var dataTable = po.dataSetPropertiesTableElement().DataTable();
			$.setDataTableData(dataTable, po.getFormDataSetProperties(true));
			$.dataTableUtil.moveSelectedUp(dataTable);
		});
		
		po.element(".down-property-button").click(function()
		{
			var dataTable = po.dataSetPropertiesTableElement().DataTable();
			$.setDataTableData(dataTable, po.getFormDataSetProperties(true));
			$.dataTableUtil.moveSelectedDown(dataTable);
		});

		po.element(".dataformat-button").click(function()
		{
			po.dataFormatPanelElement().toggle();
		});
		
		po.dataSetPropertiesTableElement().on("click", ".input-in-table", function(event)
		{
			//阻止行选中
			event.stopPropagation();
		});

		po.element().on("click", function(event)
		{
			var $target = $(event.target);
			
			var $p = po.dataFormatPanelElement();
			if(!$p.is(":hidden"))
			{
				if($target.closest(".dataformat-panel, .dataformat-button").length == 0)
					$p.hide();
			}
		});
	};
	
	po.hasFormDataSetProperty = function()
	{
		var $names = po.element(".properties-table-wrapper .dataSetPropertyName");
		return ($names.length > 0);
	};
	
	po.getFormDataSetProperties = function(manualInfoReturn)
	{
		manualInfoReturn = (manualInfoReturn == true ? true : false);
		
		var properties = [];
		
		po.element(".properties-table-wrapper .dataSetPropertyName").each(function(i)
		{
			properties[i] = {};
			var $this = $(this);
			properties[i]["name"] = $this.val();
			if(manualInfoReturn)
				properties[i]["manual"] = $this.hasClass("manual");
		});
		po.element(".properties-table-wrapper .dataSetPropertyType").each(function(i)
		{
			properties[i]["type"] = $(this).val();
		});
		po.element(".properties-table-wrapper .dataSetPropertyLabel").each(function(i)
		{
			properties[i]["label"] = $(this).val();
		});
		po.element(".properties-table-wrapper .dataSetPropertyDefaultValue").each(function(i)
		{
			properties[i]["defaultValue"] = $(this).val();
		});
		
		return properties;
	};
	
	po.updateFormDataSetProperties = function(dataSetProperties)
	{
		dataSetProperties = (dataSetProperties || []);
		var prevProperties = po.getFormDataSetProperties(true);
		var updateProperties = [];
		
		//添加后台自动解析的属性
		for(var i=0; i<dataSetProperties.length; i++)
		{
			var prev = null;
			
			for(var j=0; j<prevProperties.length; j++)
			{
				if(dataSetProperties[i].name == prevProperties[j].name)
				{
					prev = prevProperties[j];
					break;
				}
			}
			
			//添加同名的旧属性，因为用户可能已编辑；否则，添加新属性
			if(prev != null)
				updateProperties.push(prev);
			else
				updateProperties.push(dataSetProperties[i]);
		}
		
		//添加用户手动添加的属性
		for(var i=0; i<prevProperties.length; i++)
		{
			if(prevProperties[i].manual == true)
			{
				var exists = false;
				
				for(var j=0; j<updateProperties.length; j++)
				{
					if(prevProperties[i].name == updateProperties[j].name)
					{
						exists = true;
						break;
					}
				}
				
				if(!exists)
					updateProperties.push(prevProperties[i]);
			}
		}
		
		$.addDataTableData(po.dataSetPropertiesTableElement().DataTable(), updateProperties, 0);
		
		po.element(".operation-properties").addClass("ui-state-highlight");
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
						return "<textarea class='dataSetParamInputPayload input-in-table ui-widget ui-widget-content' style='height:2em;'>"+$.escapeHtml(data)+"</textarea>";
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
		
		po.element(".up-param-button").click(function()
		{
			var dataTable = po.dataSetParamsTableElement().DataTable();
			$.setDataTableData(dataTable, po.getFormDataSetParams());
			$.dataTableUtil.moveSelectedUp(dataTable);
		});
		
		po.element(".down-param-button").click(function()
		{
			var dataTable = po.dataSetParamsTableElement().DataTable();
			$.setDataTableData(dataTable, po.getFormDataSetParams());
			$.dataTableUtil.moveSelectedDown(dataTable);
		});
		
		po.dataSetParamsTableElement().on("click", ".input-in-table", function(event)
		{
			//阻止行选中
			event.stopPropagation();
		});
	};

	po.hasFormDataSetParam = function()
	{
		if(po.disableDataSetParamOperation())
			return false;
		
		var $names = po.element(".params-table-wrapper .dataSetParamName");
		return ($names.length > 0);
	};
	
	po.getFormDataSetParams = function()
	{
		var params = [];
		
		if(po.disableDataSetParamOperation())
			return params;
		
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
			paramValues: chartFactory.chartSetting.getDataSetParamValueObj(chartFactory.chartSetting.getDataSetParamValueForm($panel)),
			render: function()
			{
				$("select, input, textarea", this).addClass("ui-widget ui-widget-content");
				$("button", this).addClass("ui-button ui-corner-all ui-widget");
			}
		},
		formOptions);
		
		$panelContent.empty();
		
		chartFactory.chartSetting.renderDataSetParamValueForm($panelContent,
				po.getFormDataSetParams(), formOptions);
		
		$panel.show();
		$panel.position({ my : "right top", at : "left+5 top", of : po.element(".workspace-operation-wrapper")});
	};
	
	po.resultFetchSizeDefault = 100;
	
	//预览设置项
	po.previewOptions =
	{
		//预览请求URL，必须设置
		url: null,
		//预览请求参数数据
		data:
		{
			dataSet: {},
			query: { resultFetchSize: po.resultFetchSizeDefault }
		},
		//预览操作前置回调函数，返回false阻止
		beforePreview: function(){},
		//刷新操作前置回调函数，返回false阻止
		beforeRefresh: function(){},
		//预览请求前置回调函数，返回false阻止
		beforeRequest: function(){},
		//预览响应构建表格列数组
		buildTablesColumns: function(previewResponse)
		{
			return po.buildDataSetPropertiesColumns(previewResponse.properties);
		},
		//预览请求成功回调函数
		success: function(previewResponse){}
	};
	
	po.resultFetchSizeVal = function(val)
	{
		var $input = po.element(".resultFetchSizeInput");
		
		if(val === undefined)
		{
			val = parseInt($input.val());
			var validVal = val;
			
			if(isNaN(validVal))
				validVal = po.resultFetchSizeDefault;
			else if(validVal < 1)
				validVal = 1;
			
			if(validVal != val)
			{
				val = validVal;
				$input.val(val);
			}
			
			return val;
		}
		else
			$input.val(val);
	};
	
	po.getFormDataFormat = function()
	{
		var df =
		{
			"dateFormat": po.element("input[name='dataFormat.dateFormat']").val(),
			"timeFormat": po.element("input[name='dataFormat.timeFormat']").val(),
			"timestampFormat": po.element("input[name='dataFormat.timestampFormat']").val(),
			"numberFormat": po.element("input[name='dataFormat.numberFormat']").val()
		};
		
		return df;
	};
	
	//获取、设置上一次预览是否成功
	po.previewSuccess = function(success)
	{
		if(success === undefined)
			return po._previewSuccess == true;
		else
			po._previewSuccess = success;
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
			var previewValueModified = po.isPreviewValueModified();
			
			if(po.previewOptions.beforePreview() == false)
				return;
			
			po.previewOptions.data.dataSet.id = po.element("input[name='id']").val();
			po.previewOptions.data.dataSet.name = po.element("input[name='name']").val();
			
			if(po.hasFormDataSetParam())
			{
				//避免设置参数面板被隐藏
				event.stopPropagation();
				
				po.showDataSetParamValuePanel(
				{
					submit: function()
					{
						po.destroyPreviewResultTable();
						
						po.previewOptions.data.dataSet.params = po.getFormDataSetParams();
						po.previewOptions.data.query.paramValues = chartFactory.chartSetting.getDataSetParamValueObj(this);
						
						po.executePreview(previewValueModified);
					}
				});
			}
			else
			{
				po.destroyPreviewResultTable();
				
				po.previewOptions.data.dataSet.params = [];
				po.previewOptions.data.query.paramValues = {};
				
				po.executePreview(previewValueModified);
			}
		});
		
		po.element(".preview-result-table-wrapper .refresh-button").click(function()
		{
			if(po.previewOptions.beforeRefresh() == false)
				return;
			
			po.executePreview(false);
		});
		
		po.element(".show-resolved-source-button").click(function()
		{
			var $panel = po.element(".result-resolved-source-panel");
			
			if($panel.is(":hidden"))
			{
				$panel.show();
				$panel.position({ my: "right bottom", at: "right top-5", of: this });
			}
			else
				$panel.hide();
		});
		
		po.resultFetchSizeVal(po.resultFetchSizeDefault);
		po.element(".resultFetchSizeInput").on("keydown", function(e)
		{
			//防止提交数据集表单
			if(e.keyCode == $.ui.keyCode.ENTER)
				return false;
		});
		
		$(po.element()).on("click", function(event)
		{
			var $target = $(event.target);
			
			var $panel = po.element(".result-resolved-source-panel");
			if(!$panel.is(":hidden"))
			{
				if($target.closest(".result-resolved-source").length == 0)
					$panel.hide();
			}
		});
	};
	
	po.executePreview = function(previewValueModified)
	{
		if(po.previewOptions.beforeRequest() == false)
			return;
		
		var $buttons = po.element(".preview-result-table-wrapper > .operation > button");
		$buttons.each(function()
		{
			$(this).button("disable");
		});
		
		po.element(".preview-result-foot").hide();
		
		var table = po.previewResultTableElement();
		var initDataTable = !$.isDatatTable(table);
		
		po.previewOptions.data.query.resultFetchSize = po.resultFetchSizeVal();
		po.previewOptions.data.dataSet.dataFormat = po.getFormDataFormat();
		po.previewOptions.data.dataSet.properties = po.getFormDataSetProperties();
		
		$.ajaxJson(
		{
			url : po.previewOptions.url,
			data : po.previewOptions.data,
			success : function(previewResponse)
			{
				po.previewSuccess(true);
				
				//如果工作区内容已变更才更新属性，防止上次保存后的属性被刷新
				//属性表单内容为空也更新，比如用户删除了所有属性时
				if(previewValueModified || !po.hasFormDataSetProperty())
					po.updateFormDataSetProperties(previewResponse.properties);
				
				var tableData = (previewResponse.result.data || []);
				if(!$.isArray(tableData))
					tableData = [ tableData ];
				
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
					
					po.element(".preview-result-foot").show();
					
					if(previewResponse.templateResult)
					{
						po.element(".result-resolved-source textarea").val(previewResponse.templateResult);
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
					$.addDataTableData(dataTable, tableData, 0);
					
					po.element(".preview-result-foot").show();
				}
				
				po.previewOptions.success(previewResponse);
			},
			error: function()
			{
				po.previewSuccess(false);
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
		
		var firstColumnIndex = null;
		var columns = [];
		for(var i=0; i<dataSetProperties.length; i++)
		{
			columns[i] =
			{
				title: dataSetProperties[i].name,
				//XXX 这里data不能直接使用dataSetProperties[i].name，
				//因为其中可能包含特殊字符（比如：'.'），而导致值无法展示
				data: function(row, type, setValue, meta)
				{
					//XXX DataTables-1.10.18这里有BUG，meta.col初值为1而非0，所以这里特殊处理
					if(firstColumnIndex == null)
						firstColumnIndex = meta.col;
					var colIndex = (firstColumnIndex == 1 ? meta.col - 1 : meta.col);
					
					var name = dataSetProperties[colIndex].name;
					
					if(setValue === undefined)
						return chartFactory.escapeHtml(row[name]);
					else
						row[name] = setValue;
				},
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
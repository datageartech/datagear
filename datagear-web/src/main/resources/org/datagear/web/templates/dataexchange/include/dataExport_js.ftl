<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#--
导出公用片段

String dataExchangeId 数据交换ID

依赖：
dataExchange_js.ftl

-->
<script type="text/javascript">
(function(po)
{
	po.addSubDataExchange = function(query)
	{
		if(query == null)
			query = "";
		
		var rowData = { subDataExchangeId : po.nextSubDataExchangeId(), query : query, fileName : po.toExportFileName(query), status : "" };
		po.postBuildSubDataExchange(rowData);
		po.addRowData(rowData);
		
		//滚动到底部
		var tableParent = po.tableParent();
		tableParent.scrollTop(tableParent.prop("scrollHeight"));
	};
	
	po.buildSubDataExchangesForTables = function(tableNames)
	{
		var datas = [];
		
		for(var i=0; i< tableNames.length; i++)
		{
			var data = {subDataExchangeId : po.nextSubDataExchangeId(), query : tableNames[i],
					fileName : po.toExportFileName(tableNames[i]), status : ""};
			
			po.postBuildSubDataExchange(data);
			
			datas.push(data);
		}
		
		return datas;
	};
	
	po.postBuildSubDataExchange = function(subDataExchange){};
	
	po.addAllTable = function()
	{
		if(po._addAllTableDoing)
			return;
		
		po._addAllTableDoing = true;
		
		$.ajax(
		{
			url : "${contextPath}/dataexchange/" + po.schemaId +"/getAllTableNames",
			success : function(tableNames)
			{
				if(!tableNames)
					return;
				
				var rowDatas = po.buildSubDataExchangesForTables(tableNames);
				po.addRowData(rowDatas);
			},
			complete : function()
			{
				po._addAllTableDoing = false;
			}
		});
	};
	
	po.toExportFileName = function(query, suffix)
	{
		if(!query)
			return "";
		
		var tableName = po.resolveTableName(query);
		
		if(!tableName)
		{
			if(!po.dftExportFileNameSeq)
				po.dftExportFileNameSeq = 1;
			
			tableName = po.dftExportFileNameSeq+"";
			po.dftExportFileNameSeq += 1;
		}
		
		return $.toValidFileName(tableName) + (suffix ? suffix : "");
	};
	
	po.resolveTableName = function(query)
	{
		if(!query)
			return "";
		
		//表名称
		if(!/\s/.test(query))
			return query;
		
		//第一个表名正则
		var result = query.match(/from\s([^\,\s]*)/i);
		
		if(result == null || result.length < 2)
			return "";
		
		return result[1];
	};
	
	po.handleSubDataExchangeStatus = function(subDataExchangeId, status, message)
	{
		var type = (message ? message.type : "");
		
		if("SubSuccessWithCount" == type)
		{
			if(!message.failCount || message.failCount == 0)
			{
				var spanIndex = status.indexOf("<span");
				if(spanIndex > 0)
					status = status.substring(0, spanIndex);
			}
			
			status += "<span class='exchange-result-icon exchange-download-icon' title='"+$.escapeHtml("<@spring.message code='download' />")+"' subDataExchangeId='"+$.escapeHtml(message.subDataExchangeId)+"' >"
				+"<span class='ui-icon ui-icon-circle-arrow-s'></span></span>";
		}
		
		return status;
	};
	
	po.dataExportTableColumns =
	[
		{
			title : "<@spring.message code='dataExport.tableNameOrQueryStatement' />",
			data : "query",
			render : function(data, type, row, meta)
			{
				if(!data)
					data = "";
				
				return "<input type='hidden' name='subDataExchangeIds[]' value='"+$.escapeHtml(row.subDataExchangeId)+"' />"
						+ "<textarea name='queries[]' class='query-input input-in-table ui-widget ui-widget-content ui-corner-all' style='width:90%'>"+$.escapeHtml(data)+"</textarea>";
			},
			defaultContent: "",
			width : "50%",
		},
		{
			title : "<@spring.message code='dataExport.exportFileName' />",
			data : "fileName",
			render : function(data, type, row, meta)
			{
				if(!data)
					data = "";
				
				return "<input type='text' name='fileNames[]' value='"+$.escapeHtml(data)+"' class='file-name-input input-in-table ui-widget ui-widget-content ui-corner-all' style='width:90%' />";
			},
			defaultContent: "",
			width : "20%"
		},
		{
			title : $.buildDataTablesColumnTitleWithTip("<@spring.message code='dataExport.exportProgress' />", "<@spring.message code='dataExport.exportStatusWithSuccessFail' />"),
			data : "status",
			render : function(data, type, row, meta)
			{
				if(!data)
					return "<@spring.message code='dataExchange.exchangeStatus.Unstart' />";
				else
					return data;
			},
			defaultContent: "",
			width : "30%"
		}
	];
	
	po.onStepChanged = function(event, currentIndex, priorIndex)
	{
		if(currentIndex == 1)
			po.adjustDataTable();
	};
	
	po.initDataExportSteps = function()
	{
		po.element(".form-content").steps(
		{
			headerTag: "h3",
			bodyTag: "div",
			onStepChanged : function(event, currentIndex, priorIndex)
			{
				po.onStepChanged(event, currentIndex, priorIndex);
			},
			onFinished : function(event, currentIndex)
			{
				po.element("#${pageId}-form").submit();
			},
			labels:
			{
				previous: "<@spring.message code='wizard.previous' />",
				next: "<@spring.message code='wizard.next' />",
				finish: "<@spring.message code='export' />"
			}
		});
		
		po.element("#${pageId}-form .wizard .actions ul li:eq(2)").addClass("page-status-aware-enable edit-status-enable");
	};
	
	po.initDataExportUIs = function()
	{
		po.initFormBtns();
		po.element("#${pageId}-nullForIllegalColumnValue").checkboxradiogroup();
		po.element("#${pageId}-add-group-select").selectmenu(
		{
			classes : {"ui-selectmenu-button": "ui-button-icon-only ui-corner-right"},
			select : function(event, ui)
			{
				if(ui.item.value == "addAll")
					po.addAllTable();
			}
		});
		po.elementOfName("fileEncoding").selectmenu({ appendTo : po.element(), classes : { "ui-selectmenu-menu" : "file-encoding-selectmenu-menu" } });
		po.element("#${pageId}-add-group").controlgroup();
		
		po.element("#${pageId}-nullForIllegalColumnValue-1").click();
	};
	
	po.initDataExportDataTable = function()
	{
		var tableSettings = po.buildLocalTableSettings(po.dataExportTableColumns, [], {"order": []});

		po.subDataExchangeStatusColumnIndex = tableSettings.columns.length - 1;
		
		po.initTable(tableSettings);
	};
	
	//数据库表条目拖入自动插入导出条目
	po.initDataExportDroppable = function($dropEle)
	{
		if($dropEle == undefined)
			$dropEle = po.element(".table-wrapper");
		
		$.enableTableNodeDraggable = true;
		
		$dropEle.droppable(
		{
			accept: ".table-draggable",
			drop: function(event, ui)
			{
				if(po.isDataExchangePageStatus("edit"))
				{
					var srcText = ui.draggable.text();
					
					if(srcText)
					{
						po.addSubDataExchange(srcText);
					}
				}
			}
		});
	};
	
	po.initDataExportActions = function()
	{
		po.element(".table-add-item-button").click(function()
		{
			po.addSubDataExchange();
		});
		
		po.element(".table-delete-item-button").click(function()
		{
			po.executeOnSelects(function()
			{
				po.deleteSelectedRows();
			});
		});
		
		po.element(".table-cancel-export-button").click(function()
		{
			po.cancelSelectedSubDataExchange();
		});
		
		po.element(".table-download-all-button").click(function()
		{
			var fileName = $(this).attr("file-name");
			if(!fileName)
				fileName = "export.zip";
			
			po.open("${contextPath}/dataexchange/" + po.schemaId +"/export/downloadAll",
			{
				target : "_file",
				data :
				{
					dataExchangeId : po.dataExchangeId,
					fileName : fileName
				}
			});
		});
		
		po.table().on("click", ".input-in-table", function(event)
		{
			//阻止行选中
			event.stopPropagation();
		});
		
		po.table().on("click", ".exchange-result-icon", function(event)
		{
			//阻止行选中
			event.stopPropagation();
			
			var $this = $(this);
			
			if($this.hasClass("exchange-error-icon"))
			{
				var subDataExchangeId = $this.attr("subDataExchangeId");
				po.viewSubDataExchangeDetailLog(subDataExchangeId);
			}
			else if($this.hasClass("exchange-download-icon"))
			{
				var subDataExchangeId = $this.attr("subDataExchangeId");
				var fileName = (po.subDataExchangeFileNameMap ? po.subDataExchangeFileNameMap[subDataExchangeId] : null);
				
				if(fileName)
				{
					po.open("${contextPath}/dataexchange/" + po.schemaId +"/export/download",
					{
						target : "_file",
						data :
						{
							dataExchangeId : po.dataExchangeId,
							fileName : fileName
						}
					});
				}
			}
		});
		
		po.element(".restart-button").click(function()
		{
			po.updateDataExchangePageStatus("edit");
		});
		
		po.element("#${pageId}-form").submit(function()
		{
			if(po.dataExchangeTaskClient.isActive())
				return;
			
			po.dataExchangeTaskClient.start();
			po.resetAllSubDataExchangeStatus();
			
			$(this).ajaxSubmitJson(
			{
				success: function(data)
				{
					po.subDataExchangeFileNameMap = data.data;
					
					if(!po.isDataExchangePageStatus("finish"))
						po.updateDataExchangePageStatus("exchange");
				},
				error: function()
				{
					po.dataExchangeTaskClient.stop();
				}
			});
			
			return false;
		});
		
		<#if initSqls??>
		var $returnForm = po.element("#${pageId}-returnForm");
		<#list initSqls as initSql>
		po.addSubDataExchange("${initSql?js_string?no_esc}");
		$("<textarea name='initSqls' style='display:none;'></textarea>").val("${initSql?js_string?no_esc}").appendTo($returnForm);
		</#list>
		</#if>
	};
})
(${pageId});
</script>
</body>
</html>

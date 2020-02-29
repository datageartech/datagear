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
<div id="${pageId}" class="page-form page-form-dashboard">
	<form id="${pageId}-form" action="${contextPath}/analysis/dashboard/${formAction}" method="POST">
		<div class="form-head"></div>
		<div class="form-content">
			<input type="hidden" name="id" value="${(dashboard.id)!''?html}" />
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='dashboard.name' /></label>
				</div>
				<div class="form-item-value">
					<input type="text" name="name" value="${(dashboard.name)!''?html}" class="ui-widget ui-widget-content" />
				</div>
			</div>
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='dashboard.template' /></label>
				</div>
				<div class="form-item-value form-item-value-template">
					<textarea name="templateContent" class="ui-widget ui-widget-content" style="display: none;">${templateContent!''?html}</textarea>
					<div class="template-editor-wrapper">
						<div class="template-editor-parent ui-widget ui-widget-content">
							<div id="${pageId}-template-editor" class="template-editor"></div>
						</div>
						<#if !readonly>
						<div class="insert-chart-button-wrapper">
							<button type="button" class="insert-chart-button"><@spring.message code='dashboard.insertChart' /></button>
						</div>
						</#if>
						<div class="dashboard-resource-wrapper ui-widget ui-widget-content ui-corner-all">
							<div class="resource-title ui-widget ui-widget-content">
								<@spring.message code='dashboard.dashboardResource' />
							</div>
							<#if !readonly>
							<div class="resource-button-wrapper">
								<button type='button' class='copy-resource-button resource-button ui-button ui-corner-all ui-widget ui-button-icon-only' title="<@spring.message code='dashboard.copyResourceNameToClipboard' />"><span class='ui-icon ui-icon-copy'></span><span class='ui-button-icon-space'></span></button>
								<button type='button' class='add-resource-button resource-button ui-button ui-corner-all ui-widget ui-button-icon-only' title="<@spring.message code='add' />"><span class='ui-icon ui-icon-plus'></span><span class='ui-button-icon-space'></span></button>
								<button type='button' class='refresh-resource-button resource-button ui-button ui-corner-all ui-widget ui-button-icon-only' title="<@spring.message code='refresh' />"><span class='ui-icon ui-icon-refresh'></span><span class='ui-button-icon-space'></span></button>
								<button type='button' class='delete-resource-button resource-button ui-button ui-corner-all ui-widget ui-button-icon-only' title="<@spring.message code='delete' />"><span class='ui-icon ui-icon-close'></span><span class='ui-button-icon-space'></span></button>
							</div>
							</#if>
							<div class="resource-content"></div>
							<div class='add-resource-panel ui-widget ui-widget-content ui-corner-all ui-front ui-widget-shadow'>
								<div class="add-resource-panel-head ui-widget-header ui-corner-all"><@spring.message code='dashboard.addResource' /></div>
								<div class="add-resource-panel-content">
									<div class="content-item">
										<div class="fileinput-button ui-button ui-corner-all ui-widget" title="<@spring.message code='dashboard.import.desc' />">
											<@spring.message code='select' /><input type="file" class="ignore">
										</div>
										<div class="upload-file-info"></div>
									</div>
									<div class="content-item">
										<input type="text" name="" value="" class="add-resource-name-input ui-widget ui-widget-content" />
										<input type="hidden" value="" class="resource-uploadFilePath" />
									</div>
								</div>
								<div class="add-resource-panel-foot">
									<button type="button" class="save-resource-button"><@spring.message code='confirm' /></button>
								</div>
							</div>
						</div>
					</div>
					<div class="resize-editor-wrapper">
						<button type='button' class='resize-editor-button ui-button ui-corner-all ui-widget ui-button-icon-only' title="<@spring.message code='expandOrCollapse' />"><span class='ui-icon ui-icon-arrowstop-1-w'></span><span class='ui-button-icon-space'></span></button>
					</div>
				</div>
			</div>
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='dashboard.templateName' /></label>
				</div>
				<div class="form-item-value">
					<input type="text" name="template" value="${(dashboard.template)!''?html}" class="ui-widget ui-widget-content" />
				</div>
			</div>
		</div>
		<div class="form-foot" style="text-align:center;">
			<#if !readonly>
			<input type="submit" value="<@spring.message code='save' />" class="recommended" />
			&nbsp;&nbsp;
			<button type="button" name="saveAndShow"><@spring.message code='dashboard.saveAndShow' /></button>
			&nbsp;&nbsp;
			<input type="reset" value="<@spring.message code='reset' />" />
			</#if>
		</div>
	</form>
</div>
<#include "../../include/page_js_obj.ftl" >
<#include "../../include/page_obj_form.ftl">
<script type="text/javascript">
(function(po)
{
	$.initButtons(po.element());
	var tewHeight = $(window).height()*5/9;
	po.element(".template-editor-wrapper").height(tewHeight);
	po.element(".form-item-value-template").height(tewHeight + 30);

	po.url = function(action)
	{
		return "${contextPath}/analysis/dashboard/" + action;
	};
	
	po.initTemplateEditor = function()
	{
		var templateEditorCompleters =
		[
			{
				identifierRegexps : [/[a-zA-Z_0-9\.\$]/],
				getCompletions: function(editor, session, pos, prefix, callback)
				{
					return [];
				}
			}
		];
		var languageTools = ace.require("ace/ext/language_tools");
		var HtmlMode = ace.require("ace/mode/html").Mode;
		po.templateEditor = ace.edit("${pageId}-template-editor");
		po.templateEditor.session.setMode(new HtmlMode());
		po.templateEditor.setShowPrintMargin(false);
		po.templateEditor.setOptions(
		{
			enableBasicAutocompletion: po.templateEditorCompleters,
			enableLiveAutocompletion: po.templateEditorCompleters
		});
		po.templateEditor.focus();
		var cursor = {row: 0, column: 0};
		po.templateEditor.session.insert(cursor, po.element("textarea[name='templateContent']").val());
		var found = po.templateEditor.find("</body>",{backwards: true, wrap: false, caseSensitive: false, wholeWord: false, regExp: false});
		if(found && found.start && found.start.row > 0)
		{
			cursor = {row: found.start.row-1, column: 0};
			po.templateEditor.moveCursorToPosition(cursor);
			var selection = po.templateEditor.session.getSelection();
			selection.clearSelection();
		}
		//滚动到底部
		po.templateEditor.session.setScrollTop(1000);
		<#if readonly>
		po.templateEditor.setReadOnly(true);
		</#if>
	};
	
	po.element(".resource-content").selectable({classes: {"ui-selected": "ui-state-active"}});
	
	po.element(".resize-editor-button").click(function()
	{
		var $ele = po.element();
		var $icon = $(".ui-icon", this);
		
		if($ele.hasClass("max-template-editor"))
		{
			$ele.removeClass("max-template-editor");
			$icon.removeClass("ui-icon-arrowstop-1-e").addClass("ui-icon-arrowstop-1-w");
		}
		else
		{
			$ele.addClass("max-template-editor");
			$icon.removeClass("ui-icon-arrowstop-1-w").addClass("ui-icon-arrowstop-1-e");
		}
	});
	
	<#if !readonly>
	po.getSelectedResourceName = function()
	{
		var $resources = po.element(".resource-content");
		var $res = $("> .resource-item.ui-state-active", $resources);
		return $res.attr("resource-name");
	};
	
	po.resourceNameClipboard = new ClipboardJS(po.element(".copy-resource-button")[0],
	{
		text: function(trigger)
		{
			var text = po.getSelectedResourceName();
			if(!text)
				text = "";
			
			return text;
		}
	});
	po.resourceNameClipboard.on('success', function(e)
	{
		$.tipSuccess("<@spring.message code='copyToClipboardSuccess' />");
	});
	
	po.element(".add-resource-panel").draggable({ handle : ".add-resource-panel-head" });

	$(document.body).on("click", function(event)
	{
		var $target = $(event.target);

		var $ssp = po.element(".add-resource-panel");
		if(!$ssp.is(":hidden"))
		{
			if($target.closest(".add-resource-panel, .add-resource-button").length == 0)
				$ssp.hide();
		}
	});
	
	po.element(".add-resource-button").click(function()
	{
		var id = po.element("input[name='id']").val();
		
		if(!id)
		{
			$.tipInfo("<@spring.message code='dashboard.pleaseSaveDashboardFirst' />");
			return;
		}
		
		po.element(".add-resource-name-input").val("");
		po.element(".resource-uploadFilePath").val("");
		po.element(".upload-file-info").text("");
		
		var $panel = po.element(".add-resource-panel");
		$panel.show();
		//$panel.position({ my : "right top", at : "right+20 bottom+3", of : this});
	});
	
	po.element(".save-resource-button").click(function()
	{
		var id = po.element("input[name='id']").val();
		var resourceFilePath = po.element(".resource-uploadFilePath").val();
		var resourceName = po.element(".add-resource-name-input").val();
		
		if(!id || !resourceFilePath || !resourceName)
			return;
		
		$.post(po.url("saveResourceFile"), {"id": id, "resourceFilePath": resourceFilePath, "resourceName": resourceName}, function()
		{
			po.refreshDashboardResources();
			po.element(".add-resource-panel").hide();
		});
	});
	
	po.element(".refresh-resource-button").click(function()
	{
		po.refreshDashboardResources();
	});
	
	po.element(".delete-resource-button").click(function()
	{
		var id = po.element("input[name='id']").val();
		var name = po.getSelectedResourceName();
		
		if(!id || !name)
			return;
		
		po.confirm("<@spring.message code='dashboard.confirmDeleteSelectedResource' />",
		{
			"confirm" : function()
			{
				$.post(po.url("deleteResource"), {"id": id, "name" : name}, function(){
					po.refreshDashboardResources();
				});
			}
		});
	});
	
	po.fileUploadInfo = function(){ return this.element(".upload-file-info"); };
	
	po.element(".fileinput-button").fileupload(
	{
		url : po.url("uploadResourceFile"),
		paramName : "file",
		success : function(uploadResult, textStatus, jqXHR)
		{
			var currentRes = po.getSelectedResourceName();
			if(currentRes)
			{
				var lastChar = currentRes.charAt(currentRes.length - 1);
				if(lastChar == "/" || lastChar == "\\")
					;
				else
					currentRes = "";
			}
			else
				currentRes = "";
			
			po.element(".add-resource-name-input").val(currentRes + uploadResult.fileName);
			po.element(".resource-uploadFilePath").val(uploadResult.uploadFilePath);
			
			$.fileuploadsuccessHandlerForUploadInfo(po.fileUploadInfo(), false);
		}
	})
	.bind('fileuploadadd', function (e, data)
	{
		$.fileuploadaddHandlerForUploadInfo(e, data, po.fileUploadInfo());
	})
	.bind('fileuploadprogressall', function (e, data)
	{
		$.fileuploadprogressallHandlerForUploadInfo(e, data, po.fileUploadInfo());
	});
	</#if>
	
	po.refreshDashboardResources = function()
	{
		var id = po.element("input[name='id']").val();
		
		if(!id)
			return;
		
		var $resources = po.element(".resource-content");
		$resources.empty();
		
		$.get(po.url("listResources?id="+id), function(resources)
		{
			if(!resources)
				return;
			
			for(var i=0; i<resources.length; i++)
			{
				var $res = $("<div class='resource-item'></div>").attr("resource-name", resources[i]).text(resources[i]);
				$resources.append($res);
			}
			
			$resources.selectable("refresh");
		});
	};

	<#if !readonly>
	po.insertChartCode = function(charts)
	{
		if(!charts || !charts.length)
			return;
		
		var cursor = po.templateEditor.getCursorPosition();
		
		var code = "";
		
		if(charts.length == 1)
		{
			var chartId = charts[0].id;
			
			var text = po.templateEditor.session.getLine(cursor.row).substring(0, cursor.column);
			
			//获取所处标签字符串
			var prevRow = cursor.row;
			while((!text || !(/[<>]/g.test(text))) && (prevRow--) >= 0)
				text = po.templateEditor.session.getLine(prevRow) + text;
			
			// =
			if(/=\s*$/g.test(text))
				code = "\"" + chartId + "\"";
			// =" 或 ='
			else if(/=\s*['"]$/g.test(text))
				code = chartId;
			// <...
			else if(/<[^>]*$/g.test(text))
				code = " dg-chart-widget=\""+chartId+"\"";
			else
				code = "<div class=\"dg-chart\" dg-chart-widget=\""+chartId+"\"></div>\n";
		}
		else
		{
			for(var i=0; i<charts.length; i++)
				code += "<div class=\"dg-chart\" dg-chart-widget=\""+charts[i].id+"\"></div>\n";
		}
		
		po.templateEditor.moveCursorToPosition(cursor);
		po.templateEditor.session.insert(cursor, code);
		po.templateEditor.focus();
	};
	po.element(".insert-chart-button").click(function()
	{
		var options =
		{
			pageParam :
			{
				submit : function(charts)
				{
					po.insertChartCode(charts);
				}
			}
		};
		
		$.setGridPageHeightOption(options);
		
		po.open("${contextPath}/analysis/chart/select?multiple", options);
	});
	
	po.showAfterSave = false;
	
	po.element("button[name=saveAndShow]").click(function()
	{
		po.showAfterSave = true;
		po.element("input[type='submit']").click();
	});
	
	$.validator.addMethod("dashboardTemplateContent", function(value, element)
	{
		var html = po.templateEditor.getValue();
		return html.length > 0;
	});
			
	po.form().validate(
	{
		ignore : "",
		rules :
		{
			"name" : "required",
			"templateContent" : "dashboardTemplateContent",
			"template" : "required"
		},
		messages :
		{
			"name" : "<@spring.message code='validation.required' />",
			"templateContent" : "<@spring.message code='validation.required' />",
			"template" : "<@spring.message code='validation.required' />"
		},
		submitHandler : function(form)
		{
			po.element("textarea[name='templateContent']").val(po.templateEditor.getValue());
			
			$(form).ajaxSubmit(
			{
				success : function(response)
				{
					var dashboard = response.data;
					po.element("input[name='id']").val(dashboard.id);
					
					var close = (po.pageParamCall("afterSave")  == true);
					
					if(close)
						po.close();
					
					if(po.showAfterSave)
						window.open(po.url("show/"+dashboard.id+"/"), dashboard.id);
				},
				complete: function()
				{
					po.showAfterSave = false;
				}
			});
		},
		errorPlacement : function(error, element)
		{
			error.appendTo(element.closest(".form-item-value"));
		}
	});
	</#if>
	
	if(po.element("input[name='id']").val())
		po.element(".resize-editor-button").click();
	
	po.initTemplateEditor();
	po.refreshDashboardResources();
})
(${pageId});
</script>
</body>
</html>
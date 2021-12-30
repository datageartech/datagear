<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#include "../include/import_global.ftl">
<#include "../include/html_doctype.ftl">
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
<#include "../include/html_head.ftl">
<title><#include "../include/html_title_app_name.ftl"><@spring.message code='${titleMessageKey}' /></title>
</head>
<body>
<#include "../include/page_js_obj.ftl" >
<div id="${pageId}" class="page-form page-form-dashboard">
	<form id="${pageId}-form" action="${contextPath}/dashboard/${formAction}" method="POST">
		<div class="form-head"></div>
		<div class="form-content">
			<input type="hidden" name="id" value="${(dashboard.id)!''}" />
			<input type="hidden" id="${pageId}-copySourceId" value="${copySourceId!''}" />
			<input type="hidden" name="templateEncoding" value="${(dashboard.templateEncoding)!''}" />
			<textarea id="${pageId}-initTemplateName" style="display:none;">${templateName}</textarea>
			<textarea id="${pageId}-initTemplateContent" style="display:none;">${templateContent!''}</textarea>
			<textarea id="${pageId}-defaultTemplateContent" style="display:none;">${defaultTemplateContent!''}</textarea>
			<div class="form-item form-item-analysisProjectAware">
				<div class="form-item-label">
					<label><@spring.message code='dashboard.name' /></label>
				</div>
				<div class="form-item-value">
					<input type="text" name="name" value="${(dashboard.name)!''}" class="ui-widget ui-widget-content" />
				</div>
				<#include "../include/analysisProjectAware_form_select.ftl" >
			</div>
			<div class="form-item form-item-resources">
				<div class="form-item-label">
					<label><@spring.message code='dashboard.dashboardResource' /></label>
				</div>
				<div class="form-item-value error-newline form-item-value-resources">
					<div class="resources-wrapper">
						<div id="${pageId}-resourceEditorTabs" class="resource-editor-tabs minor-tabs">
							<ul class="resource-editor-tab-nav always-show">
							</ul>
							<div class="tabs-more-operation-menu-wrapper ui-widget ui-front ui-widget-content ui-corner-all ui-widget-shadow" style="position: absolute; left:0px; top:0px; display: none;">
								<ul class="tabs-more-operation-menu">
									<li class="tab-operation-close-left"><div><@spring.message code='main.closeLeft' /></div></li>
									<li class="tab-operation-close-right"><div><@spring.message code='main.closeRight' /></div></li>
									<li class="tab-operation-close-other"><div><@spring.message code='main.closeOther' /></div></li>
									<li class="tab-operation-close-all"><div><@spring.message code='main.closeAll' /></div></li>
								</ul>
							</div>
							<div class="tabs-more-tab-menu-wrapper ui-widget ui-front ui-widget-content ui-corner-all ui-widget-shadow" style="position: absolute; left:0px; top:0px; display: none;">
								<ul class="tabs-more-tab-menu">
								</ul>
							</div>
							<div class="resize-editor-wrapper resize-left">
								<button type='button' class='resize-editor-button resize-editor-button-left ui-button ui-corner-all ui-widget ui-button-icon-only' title="<@spring.message code='expandOrCollapse' />"><span class='ui-icon ui-icon-arrowstop-1-w'></span><span class='ui-button-icon-space'></span></button>
							</div>
						</div>
						<div class="resource-list-tabs minor-tabs">
							<ul class="resource-list-tabs-nav">
								<li class="nav-item-local"><a href="#${pageId}-resourceListLocal"><@spring.message code='dashboard.localResource' /></a></li>
								<li class="nav-item-global"><a href="#${pageId}-resourceListGlobal"><@spring.message code='dashboard.globalResource' /></a></li>
							</ul>
							<div id="${pageId}-resourceListLocal" class="resource-list-wrapper resource-list-local-wrapper ui-widget ui-widget-content ui-corner-all">
								<div class="resource-list-head ui-widget ui-widget-content">
									<#if !readonly>
									<div class="resource-button-wrapper rbw-left">
										<button type='button' class='addResBtn resource-button ui-button ui-corner-all ui-widget ui-button-icon-only' title="<@spring.message code='dashboard.addResource.desc' />"><span class='ui-icon ui-icon-plus'></span><span class='ui-button-icon-space'></span></button>
										<button type='button' class='uploadResBtn resource-button ui-button ui-corner-all ui-widget ui-button-icon-only' title="<@spring.message code='dashboard.uploadResource' />"><span class='ui-icon ui-icon-arrowstop-1-n'></span><span class='ui-button-icon-space'></span></button>
									</div>
									</#if>
									<div class="resource-button-wrapper rbw-right">
										<#if !readonly>
										<button type='button' class='editResBtn resource-button ui-button ui-corner-all ui-widget ui-button-icon-only' title="<@spring.message code='dashboard.editResource.desc' />"><span class='ui-icon ui-icon-pencil'></span><span class='ui-button-icon-space'></span></button>
										<button type='button' class='copyResNameButton resource-button ui-button ui-corner-all ui-widget ui-button-icon-only' title="<@spring.message code='dashboard.copyResourceNameToClipboard' />"><span class='ui-icon ui-icon-copy'></span><span class='ui-button-icon-space'></span></button>
										<button type='button' class='viewResButton resource-button ui-button ui-corner-all ui-widget ui-button-icon-only' title="<@spring.message code='dashboard.viewResource' />"><span class='ui-icon ui-icon-extlink'></span><span class='ui-button-icon-space'></span></button>
										<div class="resource-more-button-wrapper">
											<span class="resource-more-icon ui-icon ui-icon-caret-1-s"></span>
											<div class="resource-more-button-panel ui-widget ui-widget-content ui-corner-all ui-front ui-widget-shadow">
												<button type='button' class='deleteResBtn resource-button ui-button ui-corner-all ui-widget ui-button-icon-only' title="<@spring.message code='dashboard.deleteResource' />"><span class='ui-icon ui-icon-close'></span><span class='ui-button-icon-space'></span></button>
												<button type='button' class='asTemplateBtn resource-button ui-button ui-corner-all ui-widget ui-button-icon-only' title="<@spring.message code='dashboard.resourceAsTemplate' />"><span class='ui-icon ui-icon-arrow-1-n'></span><span class='ui-button-icon-space'></span></button>
												<button type='button' class='asNormalResBtn resource-button ui-button ui-corner-all ui-widget ui-button-icon-only' title="<@spring.message code='dashboard.templateAsNormalResource' />"><span class='ui-icon ui-icon-arrow-1-s'></span><span class='ui-button-icon-space'></span></button>
												<button type='button' class='asFirstTemplateBtn resource-button ui-button ui-corner-all ui-widget ui-button-icon-only' title="<@spring.message code='dashboard.asFirstTemplate' />"><span class='ui-icon ui-icon-home'></span><span class='ui-button-icon-space'></span></button>
												<button type='button' class='refreshResListBtn resource-button ui-button ui-corner-all ui-widget ui-button-icon-only' title="<@spring.message code='dashboard.refreshResource' />"><span class='ui-icon ui-icon-refresh'></span><span class='ui-button-icon-space'></span></button>
											</div>
										</div>
										<#else>
										<button type='button' class='editResBtn resource-button ui-button ui-corner-all ui-widget ui-button-icon-only' title="<@spring.message code='dashboard.editResource.desc' />"><span class='ui-icon ui-icon-pencil'></span><span class='ui-button-icon-space'></span></button>
										<button type='button' class='copyResNameButton resource-button ui-button ui-corner-all ui-widget ui-button-icon-only' title="<@spring.message code='dashboard.copyResourceNameToClipboard' />"><span class='ui-icon ui-icon-copy'></span><span class='ui-button-icon-space'></span></button>
										<button type='button' class='viewResButton resource-button ui-button ui-corner-all ui-widget ui-button-icon-only' title="<@spring.message code='dashboard.viewResource' />"><span class='ui-icon ui-icon-extlink'></span><span class='ui-button-icon-space'></span></button>
										<button type='button' class='refreshResListBtn resource-button ui-button ui-corner-all ui-widget ui-button-icon-only' title="<@spring.message code='dashboard.refreshResource' />"><span class='ui-icon ui-icon-refresh'></span><span class='ui-button-icon-space'></span></button>
										</#if>
									</div>
								</div>
								<div class="resource-list-template"></div>
								<div class="resource-list-divider ui-widget ui-widget-content"></div>
								<div class="resource-list-content"></div>
								<div class='add-resource-panel minor-panel ui-widget ui-widget-content ui-corner-all ui-front ui-widget-shadow'>
									<div class="addResPanelHead panel-head ui-widget-header ui-corner-all"><@spring.message code='dashboard.addResource' /></div>
									<div class="panel-content">
										<div class="content-item">
											<div class="label-wrapper">
												<label title="<@spring.message code='dashboard.addResource.name.desc' />" class="tip-label">
													<@spring.message code='dashboard.addResource.name' />
												</label>
											</div>
											<input type="text" value="" class="addResNameInput ui-widget ui-widget-content" />
										</div>
									</div>
									<div class="panel-foot">
										<button type="button" class="saveAddResBtn"><@spring.message code='confirm' /></button>
									</div>
								</div>
								<div class='upload-resource-panel minor-panel ui-widget ui-widget-content ui-corner-all ui-front ui-widget-shadow'>
									<div class="uploadResPanelHead panel-head ui-widget-header ui-corner-all"><@spring.message code='dashboard.uploadResource' /></div>
									<div class="panel-content">
										<div class="content-item">
											<div class="label-wrapper">
												<label><@spring.message code='dashboard.uploadResource.select' /></label>
											</div>
											<div class="fileinput-button ui-button ui-corner-all ui-widget">
												<@spring.message code='select' /><input type="file" class="ignore">
											</div>
											<div class="upload-file-info"></div>
										</div>
										<div class="content-item">
											<div class="label-wrapper">
												<label title="<@spring.message code='dashboard.uploadResource.savePath.desc' />" class="tip-label">
													<@spring.message code='dashboard.uploadResource.savePath' />
												</label>
											</div>
											<input type="text" value="" class="uploadResNameInput ui-widget ui-widget-content" />
											<input type="hidden" value="" class="uploadResFilePath" />
										</div>
									</div>
									<div class="panel-foot">
										<button type="button" class="saveUploadResourceButton"><@spring.message code='confirm' /></button>
									</div>
								</div>
							</div>
							<div id="${pageId}-resourceListGlobal" class="resource-list-wrapper resource-list-global-wrapper ui-widget ui-widget-content ui-corner-all">
								<div class="resource-list-head ui-widget ui-widget-content">
									<div class="resource-button-wrapper rbw-left">
										<div class="search-group ui-widget ui-widget-content ui-corner-all">
											<input type="text" class="search-input ui-widget ui-widget-content" />
											<button type="button" class="search-button ui-button ui-corner-all ui-widget ui-button-icon-only">
												<span class="ui-icon ui-icon-search"></span><span class="ui-button-icon-space"></span>Search
											</button>
										</div>
									</div>
									<div class="resource-button-wrapper rbw-right">
										<button type='button' class='copyResNameButton resource-button ui-button ui-corner-all ui-widget ui-button-icon-only' title="<@spring.message code='dashboard.copyResourceNameToClipboard' />"><span class='ui-icon ui-icon-copy'></span><span class='ui-button-icon-space'></span></button>
										<button type='button' class='viewResButton resource-button ui-button ui-corner-all ui-widget ui-button-icon-only' title="<@spring.message code='dashboard.viewResource' />"><span class='ui-icon ui-icon-extlink'></span><span class='ui-button-icon-space'></span></button>
										<button type='button' class='refreshResListBtn resource-button ui-button ui-corner-all ui-widget ui-button-icon-only' title="<@spring.message code='dashboard.refreshResource' />"><span class='ui-icon ui-icon-refresh'></span><span class='ui-button-icon-space'></span></button>
									</div>
								</div>
								<div class="resource-list-global-prefix ui-state-default">${dashboardGlobalResUrlPrefix}</div>
								<div class='resource-none ui-state-disabled'><@spring.message code='none' /></div>
								<div class="resource-list-content">
								</div>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
		<div class="form-foot" style="text-align:center;">
			<#if !readonly>
			<button type="submit" class="recommended"><@spring.message code='save' /></button>
			&nbsp;&nbsp;
			<button id="saveAndShowDashboard" type="button"><@spring.message code='dashboard.saveAndShow' /></button>
			</#if>
		</div>
	</form>
	<div class="chart-list-panel togglable-table-panel minor-panel ui-widget ui-widget-content ui-corner-all ui-widget-shadow">
		<div class="panel-content minor-dataTable pagination-light"></div>
	</div>
	<form id="${pageId}-tplEditVisualForm" action="#" method="POST" style="display:none;">
		<input type="hidden" name="DG_EDIT_TEMPLATE" value="true" />
		<textarea name="DG_TEMPLATE_CONTENT"></textarea>
	</form>
</div>
<#include "../include/page_obj_form.ftl">
<#include "../include/page_obj_tabs.ftl" >
<#include "../include/page_obj_codeEditor.ftl" >
<script type="text/javascript">
(function(po)
{
	po.templates = <@writeJson var=templates />;
	po.dashboardGlobalResUrlPrefix = "${dashboardGlobalResUrlPrefix}";
	
	$.initButtons(po.element());
	po.initAnalysisProject("${((dashboard.analysisProject.id)!'')?js_string?no_esc}", "${((dashboard.analysisProject.name)!'')?js_string?no_esc}");
	po.resourceEditorTabs = po.element("#${pageId}-resourceEditorTabs");
	
	if(po.isInDialog())
		po.element(".form-item-value-resources").height($(window).height()*5/9);
	else
	{
		var th = $(window).height() - po.element(".form-item-analysisProjectAware").outerHeight(true);
		th = th - po.element(".form-foot").outerHeight(true);
		th = th - 50;
		po.element(".form-item-value-resources").height(th);
	}
	
	po.element(".resource-list-tabs").tabs(
	{
		activate: function(event, ui)
		{
			var $this = $(this);
			var newTab = $(ui.newTab);
			var newPanel = $(ui.newPanel);
			
			if(newTab.hasClass("nav-item-global"))
				po.initResListGlobalIfNon();
		}
	});
	
	po.elementResListLocal = function(selector)
	{
		var rll = po.element(".resource-list-local-wrapper");
		
		if(!selector)
			return rll;
		else
			return $(selector, rll);
	};
	
	po.elementResListGlobal = function(selector)
	{
		var rlg = po.element(".resource-list-global-wrapper");
		
		if(!selector)
			return rlg;
		else
			return $(selector, rlg);
	};
	
	po.url = function(action)
	{
		return "${contextPath}/dashboard/" + action;
	};
	
	po.showUrl = function(dashboardId, resName)
	{
		resName = (resName == null ? "" : resName);
		return po.url("show/"+dashboardId+"/" + resName);
	};

	po.iframeWindow = function(iframe)
	{
		return (iframe.contentDocument || iframe.contentWindow.document);
	};

	po.evalTopWindowSize = function()
	{
		var topWindow = window;
		while(topWindow.parent  && topWindow.parent != topWindow)
			topWindow = topWindow.parent;
		
		var size =
		{
			width: $(topWindow).width(),
			height: $(topWindow).height()
		};
		
		return size;
	};
	
	po.iframeWindow = function($iframe)
	{
		$iframe = $iframe[0];
		return $iframe.contentWindow;
	};
	
	po.iframeDocument = function($iframe)
	{
		$iframe = $iframe[0];
		return ($iframe.contentDocument || $iframe.contentWindow.document);
	};
	
	//设置可视编辑iframe的尺寸，使其适配父元素尺寸而不会出现滚动条
	po.setVisualEditorIframeScale = function($iframeWrapper, $iframe)
	{
		var ww = $iframeWrapper.width(), wh = $iframeWrapper.height();
		var iw = $iframe.width(), ih = $iframe.height();
		
		if(iw <= ww && ih <= wh)
			return;
		
		var borderError = 5;
		var scaleX = (ww-borderError)/iw, scaleY = (wh-borderError)/ih;
		$iframe.css("transform-origin", "0 0");
		$iframe.css("transform", "scale("+Math.min(scaleX, scaleY)+")");
	};
	
	po.resourceEditorTabTemplate = "<li class='resource-editor-tab' style='vertical-align:middle;'><a href='"+'#'+"{href}'>"+'#'+"{label}</a>"
		+"<div class='tab-operation'>"
		+"<span class='ui-icon ui-icon-close' title='<@spring.message code='close' />'>close</span>"
		+"<div class='tabs-more-operation-button' title='<@spring.message code='moreOperation' />'><span class='ui-icon ui-icon-caret-1-e'></span></div>"
		+"</div>"
		+"</li>";
	
	//切换至可是编辑模式
	po.editOnVisual = function($tabPane)
	{
		var dashboardId = po.getDashboardId();
		
		if(!dashboardId)
		{
			$.tipInfo("<@spring.message code='dashboard.pleaseSaveDashboardFirst' />");
			return;
		}
		
		var codeEditorDiv = po.element(".code-editor", $tabPane);
		var codeEditor = codeEditorDiv.data("resourceEditorInstance");
		var visualEditorIfm = po.element(".tpl-visual-editor-ifm", $tabPane);
		var changeFlag = visualEditorIfm.data("codeChangeFlag");
		
		//没有修改
		if(changeFlag != null && codeEditor.isClean(changeFlag))
		{
			codeEditorDiv.removeClass("show-editor").addClass("hide-editor");
			visualEditorIfm.removeClass("hide-editor").addClass("show-editor");
		}
		else
		{
			codeEditorDiv.removeClass("show-editor").addClass("hide-editor");
			//清空iframe后再显示，防止闪屏
			po.iframeDocument(visualEditorIfm).write("");
			visualEditorIfm.removeClass("hide-editor").addClass("show-editor");
			
			var templateName = po.element(".resource-name-wrapper input.resourceName", $tabPane).val();
			var templateContent = po.getCodeText(codeEditor);
			visualEditorIfm.data("codeChangeFlag", codeEditor.changeGeneration());
			
			var form = po.element("#${pageId}-tplEditVisualForm");
			form.attr("action", po.showUrl(dashboardId, templateName));
			form.attr("target", visualEditorIfm.attr("name"));
			$("textarea[name='DG_TEMPLATE_CONTENT']", form).val(templateContent);
			form.submit();
		}
	};
	
	po.newResourceEditorTab = function(name, content, isTemplate)
	{
		var label = name;
		var labelMaxLen = 5 + 3 + 10;
		if(label.length > labelMaxLen)
			label = name.substr(0, 5) +"..." + name.substr(label.length - 10);
		
		var tabsNav = po.getTabsNav(po.resourceEditorTabs);
		var tabId = $.uid("resourceEditorTabPane");
    	var tab = $(po.resourceEditorTabTemplate.replace( /#\{href\}/g, "#" + tabId).replace(/#\{label\}/g, $.escapeHtml(label)))
    		.attr("id", $.uid("resourceEditorTab")).attr("resourceName", name).attr("title", name).appendTo(tabsNav);
    	
    	var panePrevEle = $(".resource-editor-tab-pane", po.resourceEditorTabs).last();
    	if(panePrevEle.length == 0)
    		panePrevEle = $(".resource-editor-tab-nav", po.resourceEditorTabs);
    	var tabPane = $("<div id='"+tabId+"' class='resource-editor-tab-pane' />").insertAfter(panePrevEle);
    	var resNameWrapper = $("<div class='resource-name-wrapper' />").appendTo(tabPane);
    	$("<label class='name-label'></label>").html("<@spring.message code='name' />").appendTo(resNameWrapper);
    	$("<input type='text' class='resourceName name-input ui-widget ui-widget-content' readonly='readonly' />").val(name).appendTo(resNameWrapper);
    	$("<input type='hidden' class='resourceIsTemplate' />").val(isTemplate).appendTo(resNameWrapper);
    	
		var editorOptWrapper = $("<div class='editor-operation-wrapper' />").appendTo(tabPane);
		var editorLeftOptWrapper = $("<div class='operation-left' />").appendTo(editorOptWrapper);
		var editorRightOptWrapper = $("<div class='operation-right' />").appendTo(editorOptWrapper);
    	var editorWrapper = $("<div class='editor-wrapper ui-widget ui-widget-content' />").appendTo(tabPane);
		var editorDiv = $("<div class='resource-editor code-editor' />").attr("id", $.uid("resourceEditor")).appendTo(editorWrapper);
		
		var codeEditor;
		
		var codeEditorOptions =
		{
			value: content,
			matchBrackets: true,
			matchTags: true,
			autoCloseTags: true,
			readOnly: ${readonly?string("true","false")},
			mode: po.evalCodeModeByName(name)
		};
		
		if(isTemplate && !codeEditorOptions.readOnly)
		{
			codeEditorOptions.hintOptions =
			{
				hint: po.codeEditorHintHandler
			};
		}
		
		codeEditor = po.createCodeEditor(editorDiv, codeEditorOptions);
		
		if(isTemplate && !codeEditorOptions.readOnly)
		{
			//光标移至"</body>"的上一行，便于用户直接输入内容
			var cursor = codeEditor.getSearchCursor("</body>");
			if(cursor.findNext())
			{
				var cursorFrom = cursor.from();
				codeEditor.getDoc().setCursor({ line: cursorFrom.line-1, ch: 0 });
			}
		}
		
		editorDiv.data("resourceEditorInstance", codeEditor);
		
		<#if !readonly>
		if(isTemplate)
		{
			var visualEditorDiv = $("<div class='tpl-visual-editor-wrapper' />").appendTo(editorWrapper);
			
			var visualEditorId = $.uid("visualEditor");
			var visualEditorIfm = $("<iframe class='tpl-visual-editor-ifm hide-editor ui-widget-shadow' />")
				.attr("name", visualEditorId).attr("id", visualEditorId).appendTo(visualEditorDiv);
			
			var topWindowSize = po.evalTopWindowSize();
			visualEditorIfm.css("width", topWindowSize.width);
			visualEditorIfm.css("height", topWindowSize.height);
			
			po.setVisualEditorIframeScale(visualEditorDiv, visualEditorIfm);
			
			var editorSwitchGroup = $("<div class='switch-resource-editor-group' />").appendTo(editorLeftOptWrapper);
			$("<button type='button'></button>").text("<@spring.message code='dashboard.editOnSource' />")
			.appendTo(editorSwitchGroup).button().click(function()
			{
				editorDiv.removeClass("hide-editor").addClass("show-editor");
				visualEditorIfm.removeClass("show-editor").addClass("hide-editor");
			});
			$("<button type='button'></button>").text("<@spring.message code='dashboard.editOnVisual' />")
			.appendTo(editorSwitchGroup).button().click(function()
			{
				po.editOnVisual(tabPane);
			});
			editorSwitchGroup.controlgroup();
			
			var insertChartBtn = $("<button type='button' class='insert-chart-button' />")
				.text("<@spring.message code='dashboard.insertChart' />").appendTo(editorRightOptWrapper).button()
				.click(function()
				{
					var insertChartButton = this;
					var chartListPanel = po.element(".chart-list-panel");
					
					if(chartListPanel.is(":hidden"))
					{
						chartListPanel.show();
						chartListPanel.position({ my : "center top", at : "center bottom", of : insertChartButton});
						chartListPanel.css("left", "");
						chartListPanel.css("right", "1em");
						
						if(!chartListPanel.hasClass("chart-list-loaded"))
						{
							po.element(".panel-content", chartListPanel).empty();
							
							var options =
							{
								target: po.element(".panel-content", chartListPanel),
								asDialog: false,
								pageParam :
								{
									select : function(chartWidgets)
									{
										if(!$.isArray(chartWidgets))
											chartWidgets = [chartWidgets];
										
										po.insertChart(tabPane, chartWidgets);
										chartListPanel.hide();
										return false;
									}
								},
								success: function()
								{
									chartListPanel.addClass("chart-list-loaded");
								}
							};
							$.setGridPageHeightOption(options);
							po.open("${contextPath}/chart/select?multiple", options);
						}
						else
						{
							$.callPanelShowCallback(chartListPanel);
						}
					}
					else
					{
						chartListPanel.hide();
					}
				});
		}
		</#if>
		
		var searchGroup = $("<div class='search-group ui-widget ui-widget-content ui-corner-all' />").appendTo(editorRightOptWrapper);
		var searchInput = $("<input type='text' class='search-input ui-widget ui-widget-content' />").appendTo(searchGroup)
				.on("keydown", function(e)
				{
					if(e.keyCode == $.ui.keyCode.ENTER)
					{
						po.element(".search-button", tabPane).click();
						//防止提交表单
						return false;
					}
				});
		var searchButton = $("<button type='button' class='search-button ui-button ui-corner-all ui-widget ui-button-icon-only'>"
				+"<span class='ui-icon ui-icon-search'></span><span class='ui-button-icon-space'></span>Search</button>")
				.appendTo(searchGroup)
				.click(function()
				{
					var $this = $(this);
					
					var text = po.element(".search-input", tabPane).val();
					
					if(!text)
						return;
					
					var prevSearchText = $this.data("prevSearchText");
					var cursor = $this.data("prevSearchCursor");
					var doc = codeEditor.getDoc();
					
					if(!cursor || text != prevSearchText)
					{
						cursor = codeEditor.getSearchCursor(text);
						$this.data("prevSearchCursor", cursor);
						$this.data("prevSearchText", text)
					}
					
					codeEditor.focus();
					
					if(cursor.findNext())
						doc.setSelection(cursor.from(), cursor.to());
					else
					{
						//从头搜索
						$this.data("prevSearchCursor", null);
						$this.click();
					}
				});
		
   	    $(".tab-operation .ui-icon-close", tab).click(function()
   	    {
   	    	var tab = $(this).parent().parent();
   	    	po.closeTab(po.resourceEditorTabs, tabsNav, tab);
   	    });
   	    
   	    $(".tab-operation .tabs-more-operation-button", tab).click(function()
   	    {
   	    	var tab = $(this).parent().parent();
   	    	po.showTabMoreOperationMenu(po.resourceEditorTabs, tabsNav, tab, $(this));
   	    });
		
	    po.resourceEditorTabs.tabs("refresh");
    	po.resourceEditorTabs.tabs( "option", "active",  tab.index());
    	po.refreshTabsNavForHidden(po.resourceEditorTabs, tabsNav);
    	
    	codeEditor.focus();
	};
	
	po.codeEditorHintHandler = function(codeEditor)
	{
		var doc = codeEditor.getDoc();
		var cursor = doc.getCursor();
		var mode = (codeEditor.getModeAt(cursor) || {});
		var token = (codeEditor.getTokenAt(cursor) || {});
		var tokenString = (token ? $.trim(token.string) : "");
		
		//"dg*"的HTML元素属性
		if("xml" == mode.name && "attribute" == token.type && /^dg/i.test(tokenString))
		{
			var myTagToken = po.findPrevTokenOfType(codeEditor, doc, cursor, token, "tag");
			var myCategory = (myTagToken ? myTagToken.string : null);
			
			var completions =
			{
				list: po.findCompletionList(po.codeEditorCompletionsTagAttr, tokenString, myCategory),
				from: CodeMirror.Pos(cursor.line, token.start),
				to: CodeMirror.Pos(cursor.line, token.end)
			};
			
			return completions;
		}
		//javascript函数
		else if("javascript" == mode.name && (tokenString == "." || "property" == token.type))
		{
			var myVarToken = po.findPrevTokenOfType(codeEditor, doc, cursor, token, "variable");
			var myCategory = (myVarToken ? myVarToken.string : "");
			
			//无法确定要补全的是看板还是图表对象，所以这里采用：完全匹配变量名，否则就全部提示
			// *dashboard*
			if(/dashboard/i.test(myCategory))
				myCategory = "dashboard";
			// *chart*
			else if(/chart/i.test(myCategory))
				myCategory = "chart";
			else
				myCategory = null;
			
			var completions =
			{
				list: po.findCompletionList(po.codeEditorCompletionsJsFunction, (tokenString == "." ? "" : tokenString), myCategory),
				from: CodeMirror.Pos(cursor.line, (tokenString == "." ? token.start + 1 : token.start)),
				to: CodeMirror.Pos(cursor.line, token.end)
			};
			
			return completions;
		}
	};
	
	po.resourceEditorTabs.tabs(
	{
		event: "click",
		activate: function(event, ui)
		{
			var $this = $(this);
			var newTab = $(ui.newTab);
			var newPanel = $(ui.newPanel);
			var tabsNav = po.getTabsNav($this);
			
			po.refreshTabsNavForHidden($this, tabsNav, newTab);
		}
	});
	
	po.getTabsTabMoreOperationMenu(po.resourceEditorTabs).menu(
	{
		select: function(event, ui)
		{
			var $this = $(this);
			var item = ui.item;
			
			po.handleTabMoreOperationMenuSelect($this, item, po.resourceEditorTabs);
			po.getTabsTabMoreOperationMenuWrapper(po.resourceEditorTabs).hide();
		}
	});
	
	po.getTabsMoreTabMenu(po.resourceEditorTabs).menu(
	{
		select: function(event, ui)
		{
			po.handleTabsMoreTabMenuSelect($(this), ui.item, po.resourceEditorTabs);
	    	po.getTabsMoreTabMenuWrapper(po.resourceEditorTabs).hide();
		}
	});
	
	po.bindTabsMenuHiddenEvent(po.resourceEditorTabs);
	
	po.getLastTagText = function(text)
	{
		if(!text)
			return text;
		
		var idx = -1;
		for(var i=text.length-1;i>=0;i--)
		{
			var c = text.charAt(i);
			if(c == '>' || c == '<')
			{
				idx = i;
				break;
			}
		}
		
		return (idx < 0 ? text : text.substr(idx));
	};
	
	po.getTemplatePrevTagText = function(codeEditor, cursor)
	{
		var doc = codeEditor.getDoc();
		
		var text = doc.getLine(cursor.line).substring(0, cursor.ch);
		
		//反向查找直到'>'或'<'
		var prevRow = cursor.line;
		while((!text || !(/[<>]/g.test(text))) && (prevRow--) >= 0)
			text = doc.getLine(prevRow) + text;
		
		return po.getLastTagText(text);
	};
	
	po.insertChart = function($tabPane, chartWidgets)
	{
		var codeEditorDiv = po.element(".code-editor", $tabPane);
		var visualEditorIfm = po.element(".tpl-visual-editor-ifm", $tabPane);
		
		if(codeEditorDiv.hasClass("show-editor"))
		{
			var codeEditor = codeEditorDiv.data("resourceEditorInstance");
			po.insertChartCode(codeEditor, chartWidgets);
		}
		else if(visualEditorIfm.hasClass("show-editor"))
		{
			ifmWindow = po.iframeWindow(visualEditorIfm);
			var dashboardEditor = ifmWindow.dashboardFactory.dashboardEditor;
			dashboardEditor.insertChart(chartWidgets);
		}
	};
	
	po.insertChartCode = function(codeEditor, charts)
	{
		if(!charts || !charts.length)
			return;
		
		var doc = codeEditor.getDoc();
		var cursor = doc.getCursor();
		
		//如果body上没有定义dg-dashboard样式，则图表元素也不必添加dg-chart样式，比如导入的看板
		var setDashboardTheme = true;
		
		var code = "";
		
		if(charts.length == 1)
		{
			var chartId = charts[0].id;
			var chartName = charts[0].name;
			
			var text = po.getTemplatePrevTagText(codeEditor, cursor);
			
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
			{
				setDashboardTheme = po.isCodeHasDefaultThemeClass(codeEditor, cursor);
				code = "<div "+(setDashboardTheme ? "class=\"dg-chart\" " : "")+"dg-chart-widget=\""+chartId+"\"><!--"+chartName+"--></div>\n";
			}
		}
		else
		{
			setDashboardTheme = po.isCodeHasDefaultThemeClass(codeEditor, cursor);
			for(var i=0; i<charts.length; i++)
				code += "<div "+(setDashboardTheme ? "class=\"dg-chart\" " : "")+"dg-chart-widget=\""+charts[i].id+"\"><!--"+charts[i].name+"--></div>\n";
		}
		
		po.insertCodeText(codeEditor, cursor, code);
		codeEditor.focus();
	};
	
	po.isCodeHasDefaultThemeClass = function(codeEditor, cursor)
	{
		var doc = codeEditor.getDoc();
		
		var row = cursor.line;
		var text = "";
		while(row >= 0)
		{
			text = doc.getLine(row);
			
			if(text && /["'\s]dg-dashboard["'\s]/g.test(text))
				return true;
			
			if(/<body/gi.test(text))
				break;
			
			row--;
		}
		
		return false;
	};
	
	po.codeEditorCompletionsTagAttr =
	[
		{name: "dg-chart-auto-resize", value: "dg-chart-auto-resize=\"true\"",
			displayComment: "<@spring.message code='dashboard.templateEditor.autoComplete.dg-chart-auto-resize' />", categories: ["body", "div"]},
		{name: "dg-chart-disable-setting",
			displayComment: "<@spring.message code='dashboard.templateEditor.autoComplete.dg-chart-disable-setting' />", categories: ["body", "div"]},
		{name: "dg-chart-link",
			displayComment: "<@spring.message code='dashboard.templateEditor.autoComplete.dg-chart-link' />", categories: ["div"]},
		{name: "dg-chart-listener",
			displayComment: "<@spring.message code='dashboard.templateEditor.autoComplete.dg-chart-listener' />", categories: ["body", "div"]},
		{name: "dg-chart-map",
			displayComment: "<@spring.message code='dashboard.templateEditor.autoComplete.dg-chart-map' />", categories: ["div"]},
		{name: "dg-chart-map-urls",
			displayComment: "<@spring.message code='dashboard.templateEditor.autoComplete.dg-chart-map-urls' />", categories: ["body"]},
		{name: "dg-chart-on-",
			displayComment: "<@spring.message code='dashboard.templateEditor.autoComplete.dg-chart-on-' />", categories: ["div"]},
		{name: "dg-chart-options",
			displayComment: "<@spring.message code='dashboard.templateEditor.autoComplete.dg-chart-options' />", categories: ["body","div"]},
		{name: "dg-chart-renderer",
			displayComment: "<@spring.message code='dashboard.templateEditor.autoComplete.dg-chart-renderer' />", categories: ["div"]},
		{name: "dg-chart-theme",
			displayComment: "<@spring.message code='dashboard.templateEditor.autoComplete.dg-chart-theme' />", categories: ["body", "div"]},
		{name: "dg-chart-update-group",
			displayComment: "<@spring.message code='dashboard.templateEditor.autoComplete.dg-chart-update-group' />", categories: ["body", "div"]},
		{name: "dg-chart-widget",
			displayComment: "<@spring.message code='dashboard.templateEditor.autoComplete.dg-chart-widget' />", categories: ["div"]},
		{name: "dg-dashboard-form",
			displayComment: "<@spring.message code='dashboard.templateEditor.autoComplete.dg-dashboard-form' />", categories: ["form"]},
		{name: "dg-dashboard-listener",
			displayComment: "<@spring.message code='dashboard.templateEditor.autoComplete.dg-dashboard-listener' />", categories: ["body"]},
		{name: "dg-dashboard-var",
			displayComment: "<@spring.message code='dashboard.templateEditor.autoComplete.dg-dashboard-var' />", categories: ["html"]},
		{name: "dg-echarts-theme",
			displayComment: "<@spring.message code='dashboard.templateEditor.autoComplete.dg-echarts-theme' />", categories: ["body", "div"]},
	];
	
	po.codeEditorCompletionsJsFunction =
	[
		//看板JS对象
		{name: "addChart", value: "addChart(", displayName: "addChart()", displayComment: "dashboard", categories: ["dashboard"], categories: ["dashboard"]},
		{name: "chartIndex", value: "chartIndex(", displayName: "chartIndex()", displayComment: "dashboard", categories: ["dashboard"], categories: ["dashboard"]},
		{name: "chartOf", value: "chartOf(", displayName: "chartOf()", displayComment: "dashboard", categories: ["dashboard"], categories: ["dashboard"]},
		{name: "batchSetDataSetParamValues", value: "batchSetDataSetParamValues(", displayName: "batchSetDataSetParamValues()", displayComment: "dashboard", categories: ["dashboard"], categories: ["dashboard"]},
		{name: "charts", value: "charts", displayName: "charts", displayComment: "dashboard", categories: ["dashboard"], categories: ["dashboard"]},
		{name: "doRender", value: "doRender()", displayName: "doRender()", displayComment: "dashboard", categories: ["dashboard"], categories: ["dashboard"]},
		{name: "init", value: "init()", displayName: "init()", displayComment: "dashboard", categories: ["dashboard"], categories: ["dashboard"]},
		{name: "isHandlingCharts", value: "isHandlingCharts()", displayName: "isHandlingCharts()", displayComment: "dashboard", categories: ["dashboard"], categories: ["dashboard"]},
		{name: "listener", value: "listener(", displayName: "listener()", displayComment: "dashboard", categories: ["dashboard"], categories: ["dashboard"]},
		{name: "loadChart", value: "loadChart(", displayName: "loadChart()", displayComment: "dashboard", categories: ["dashboard"], categories: ["dashboard"]},
		{name: "loadCharts", value: "loadCharts(", displayName: "loadCharts()", displayComment: "dashboard", categories: ["dashboard"], categories: ["dashboard"]},
		{name: "loadUnsolvedCharts", value: "loadUnsolvedCharts()", displayName: "loadUnsolvedCharts()", displayComment: "dashboard", categories: ["dashboard"], categories: ["dashboard"]},
		{name: "mapURLs", value: "mapURLs(", displayName: "mapURLs()", displayComment: "dashboard", categories: ["dashboard"]},
		{name: "originalInfo", value: "originalInfo(", displayName: "originalInfo()", displayComment: "dashboard", categories: ["dashboard"]},
		{name: "refreshData", value: "refreshData(", displayName: "refreshData()", displayComment: "dashboard", categories: ["dashboard"]},
		{name: "removeChart", value: "removeChart(", displayName: "removeChart()", displayComment: "dashboard", categories: ["dashboard"]},
		{name: "render", value: "render()", displayName: "render()", displayComment: "dashboard", categories: ["dashboard"]},
		{name: "renderContext", value: "renderContext", displayName: "renderContext", displayComment: "dashboard", categories: ["dashboard"]},
		{name: "renderContextAttr", value: "renderContextAttr(", displayName: "renderContextAttr()", displayComment: "dashboard", categories: ["dashboard"]},
		{name: "renderForm", value: "renderForm(", displayName: "renderForm()", displayComment: "dashboard", categories: ["dashboard"]},
		{name: "renderedChart", value: "renderedChart(", displayName: "renderedChart()", displayComment: "dashboard", categories: ["dashboard"]},
		{name: "resizeAllCharts", value: "resizeAllCharts()", displayName: "resizeAllCharts()", displayComment: "dashboard", categories: ["dashboard"]},
		{name: "resizeChart", value: "resizeChart(", displayName: "resizeChart()", displayComment: "dashboard", categories: ["dashboard"]},
		{name: "resultDataFormat", value: "resultDataFormat(", displayName: "resultDataFormat()", displayComment: "dashboard", categories: ["dashboard"]},
		{name: "serverDate", value: "serverDate()", displayName: "serverDate()", displayComment: "dashboard", categories: ["dashboard"]},
		{name: "startHandleCharts", value: "startHandleCharts()", displayName: "startHandleCharts()", displayComment: "dashboard", categories: ["dashboard"]},
		{name: "stopHandleCharts", value: "stopHandleCharts()", displayName: "stopHandleCharts()", displayComment: "dashboard", categories: ["dashboard"]},
		{name: "user", value: "user()", displayName: "user()", displayComment: "dashboard", categories: ["dashboard"]},
		
		//图表JS对象
		{name: "autoResize", value: "autoResize(", displayName: "autoResize() ", displayComment: "chart", categories: ["chart"], categories: ["chart"]},
		{name: "bindLinksEventHanders", value: "bindLinksEventHanders(", displayName: "bindLinksEventHanders() ", displayComment: "chart", categories: ["chart"]},
		{name: "callEventHandler", value: "callEventHandler(", displayName: "callEventHandler() ", displayComment: "chart", categories: ["chart"]},
		{name: "chartDataSetAt", value: "chartDataSetAt(", displayName: "chartDataSetAt() ", displayComment: "chart", categories: ["chart"]},
		{name: "chartDataSetFirst", value: "chartDataSetFirst(", displayName: "chartDataSetFirst() ", displayComment: "chart", categories: ["chart"]},
		{name: "chartDataSets", value: "chartDataSets", displayName: "chartDataSets ", displayComment: "chart", categories: ["chart"]},
		{name: "chartDataSetsAttachment", value: "chartDataSetsAttachment()", displayName: "chartDataSetsAttachment() ", displayComment: "chart", categories: ["chart"]},
		{name: "chartDataSetsMain", value: "chartDataSetsMain()", displayName: "chartDataSetsMain() ", displayComment: "chart", categories: ["chart"]},
		{name: "dashboard", value: "dashboard", displayName: "dashboard ", displayComment: "chart", categories: ["chart"]},
		{name: "dataSetAlias", value: "dataSetAlias(", displayName: "dataSetAlias() ", displayComment: "chart", categories: ["chart"]},
		{name: "dataSetParam", value: "dataSetParam(", displayName: "dataSetParam() ", displayComment: "chart", categories: ["chart"]},
		{name: "dataSetParamValue", value: "dataSetParamValue(", displayName: "dataSetParamValue() ", displayComment: "chart", categories: ["chart"]},
		{name: "dataSetParamValueFirst", value: "dataSetParamValueFirst(", displayName: "dataSetParamValueFirst() ", displayComment: "chart", categories: ["chart"]},
		{name: "dataSetParamValues", value: "dataSetParamValues(", displayName: "dataSetParamValues() ", displayComment: "chart", categories: ["chart"]},
		{name: "dataSetParamValuesFirst", value: "dataSetParamValuesFirst(", displayName: "dataSetParamValuesFirst() ", displayComment: "chart", categories: ["chart"]},
		{name: "dataSetParams", value: "dataSetParams(", displayName: "dataSetParams() ", displayComment: "chart", categories: ["chart"]},
		{name: "dataSetProperties", value: "dataSetProperties(", displayName: "dataSetProperties() ", displayComment: "chart", categories: ["chart"]},
		{name: "dataSetPropertiesOfSign", value: "dataSetPropertiesOfSign(", displayName: "dataSetPropertiesOfSign() ", displayComment: "chart", categories: ["chart"]},
		{name: "dataSetProperty", value: "dataSetProperty(", displayName: "dataSetProperty() ", displayComment: "chart", categories: ["chart"]},
		{name: "dataSetPropertyAlias", value: "dataSetPropertyAlias(", displayName: "dataSetPropertyAlias() ", displayComment: "chart", categories: ["chart"]},
		{name: "dataSetPropertyOfSign", value: "dataSetPropertyOfSign(", displayName: "dataSetPropertyOfSign() ", displayComment: "chart", categories: ["chart"]},
		{name: "dataSetPropertyOrder", value: "dataSetPropertyOrder(", displayName: "dataSetPropertyOrder() ", displayComment: "chart", categories: ["chart"]},
		{name: "dataSetPropertySign", value: "dataSetPropertySign(", displayName: "dataSetPropertySign() ", displayComment: "chart", categories: ["chart"]},
		{name: "dataSetPropertySigns", value: "dataSetPropertySigns(", displayName: "dataSetPropertySigns() ", displayComment: "chart", categories: ["chart"]},
		{name: "destroy", value: "destroy()", displayName: "destroy() ", displayComment: "chart", categories: ["chart"]},
		{name: "disableSetting", value: "disableSetting(", displayName: "disableSetting() ", displayComment: "chart", categories: ["chart"]},
		{name: "doRender", value: "doRender()", displayName: "doRender() ", displayComment: "chart", categories: ["chart"]},
		{name: "doUpdate", value: "doUpdate(", displayName: "doUpdate() ", displayComment: "chart", categories: ["chart"]},
		{name: "echartsGetThemeName", value: "echartsGetThemeName()", displayName: "echartsGetThemeName() ", displayComment: "chart", categories: ["chart"]},
		{name: "echartsInit", value: "echartsInit(", displayName: "echartsInit() ", displayComment: "chart", categories: ["chart"]},
		{name: "echartsLoadMap", value: "echartsLoadMap(", displayName: "echartsLoadMap() ", displayComment: "chart", categories: ["chart"]},
		{name: "echartsMapRegistered", value: "echartsMapRegistered(", displayName: "echartsMapRegistered() ", displayComment: "chart", categories: ["chart"]},
		{name: "echartsOffEventHandler", value: "echartsOffEventHandler(", displayName: "echartsOffEventHandler() ", displayComment: "chart", categories: ["chart"]},
		{name: "echartsOptions", value: "echartsOptions(", displayName: "echartsOptions() ", displayComment: "chart", categories: ["chart"]},
		{name: "echartsThemeName", value: "echartsThemeName(", displayName: "echartsThemeName() ", displayComment: "chart", categories: ["chart"]},
		{name: "element", value: "element()", displayName: "element() ", displayComment: "chart", categories: ["chart"]},
		{name: "elementId", value: "elementId", displayName: "elementId ", displayComment: "chart", categories: ["chart"]},
		{name: "elementJquery", value: "elementJquery()", displayName: "elementJquery() ", displayComment: "chart", categories: ["chart"]},
		{name: "elementStyle", value: "elementStyle(", displayName: "elementStyle() ", displayComment: "chart", categories: ["chart"]},
		{name: "elementWidgetId", value: "elementWidgetId()", displayName: "elementWidgetId() ", displayComment: "chart", categories: ["chart"]},
		{name: "eventData", value: "eventData(", displayName: "eventData() ", displayComment: "chart", categories: ["chart"]},
		{name: "eventHandlers", value: "eventHandlers(", displayName: "eventHandlers() ", displayComment: "chart", categories: ["chart"]},
		{name: "eventNewEcharts", value: "eventNewEcharts(", displayName: "eventNewEcharts() ", displayComment: "chart", categories: ["chart"]},
		{name: "eventNewHtml", value: "eventNewHtml(", displayName: "eventNewHtml() ", displayComment: "chart", categories: ["chart"]},
		{name: "eventOriginalChartDataSetIndex", value: "eventOriginalChartDataSetIndex(", displayName: "eventOriginalChartDataSetIndex() ", displayComment: "chart", categories: ["chart"]},
		{name: "eventOriginalData", value: "eventOriginalData(", displayName: "eventOriginalData() ", displayComment: "chart", categories: ["chart"]},
		{name: "eventOriginalInfo", value: "eventOriginalInfo(", displayName: "eventOriginalInfo() ", displayComment: "chart", categories: ["chart"]},
		{name: "eventOriginalResultDataIndex", value: "eventOriginalResultDataIndex(", displayName: "eventOriginalResultDataIndex() ", displayComment: "chart", categories: ["chart"]},
		{name: "extValue", value: "extValue(", displayName: "extValue() ", displayComment: "chart", categories: ["chart"]},
		{name: "gradualColor", value: "gradualColor(", displayName: "gradualColor() ", displayComment: "chart", categories: ["chart"]},
		{name: "handleChartEventLink", value: "handleChartEventLink(", displayName: "handleChartEventLink() ", displayComment: "chart", categories: ["chart"]},
		{name: "hasDataSetParam", value: "hasDataSetParam()", displayName: "hasDataSetParam() ", displayComment: "chart", categories: ["chart"]},
		{name: "id", value: "id", displayName: "id ", displayComment: "chart", categories: ["chart"]},
		{name: "inflateRenderOptions", value: "inflateRenderOptions(", displayName: "inflateRenderOptions() ", displayComment: "chart", categories: ["chart"]},
		{name: "inflateUpdateOptions", value: "inflateUpdateOptions(", displayName: "inflateUpdateOptions() ", displayComment: "chart", categories: ["chart"]},
		{name: "init", value: "init()", displayName: "init() ", displayComment: "chart", categories: ["chart"]},
		{name: "internal", value: "internal()", displayName: "internal( ", displayComment: "chart", categories: ["chart"]},
		{name: "isActive", value: "isActive()", displayName: "isActive() ", displayComment: "chart", categories: ["chart"]},
		{name: "isAsyncRender", value: "isAsyncRender()", displayName: "isAsyncRender() ", displayComment: "chart", categories: ["chart"]},
		{name: "isAsyncUpdate", value: "isAsyncUpdate()", displayName: "isAsyncUpdate() ", displayComment: "chart", categories: ["chart"]},
		{name: "isDataSetParamValueReady", value: "isDataSetParamValueReady()", displayName: "isDataSetParamValueReady() ", displayComment: "chart", categories: ["chart"]},
		{name: "isInstance", value: "isInstance(", displayName: "isInstance() ", displayComment: "chart", categories: ["chart"]},
		{name: "links", value: "links(", displayName: "links() ", displayComment: "chart", categories: ["chart"]},
		{name: "listener", value: "listener(", displayName: "listener() ", displayComment: "chart", categories: ["chart"]},
		{name: "loadMap", value: "loadMap(", displayName: "loadMap() ", displayComment: "chart", categories: ["chart"]},
		{name: "map", value: "map(", displayName: "map() ", displayComment: "chart", categories: ["chart"]},
		{name: "mapURL", value: "mapURL(", displayName: "mapURL() ", displayComment: "chart", categories: ["chart"]},
		{name: "name", value: "name", displayName: "name ", displayComment: "chart", categories: ["chart"]},
		{name: "off", value: "off(", displayName: "off() ", displayComment: "chart", categories: ["chart"]},
		{name: "on", value: "on(", displayName: "on() ", displayComment: "chart", categories: ["chart"]},
		{name: "onClick", value: "onClick(", displayName: "onClick() ", displayComment: "chart", categories: ["chart"]},
		{name: "onDblclick", value: "onDblclick(", displayName: "onDblclick() ", displayComment: "chart", categories: ["chart"]},
		{name: "onMousedown", value: "onMousedown(", displayName: "onMousedown() ", displayComment: "chart", categories: ["chart"]},
		{name: "onMouseout", value: "onMouseout(", displayName: "onMouseout() ", displayComment: "chart", categories: ["chart"]},
		{name: "onMouseover", value: "onMouseover(", displayName: "onMouseover() ", displayComment: "chart", categories: ["chart"]},
		{name: "onMouseup", value: "onMouseup(", displayName: "onMouseup() ", displayComment: "chart", categories: ["chart"]},
		{name: "options", value: "options(", displayName: "options() ", displayComment: "chart", categories: ["chart"]},
		{name: "originalInfo", value: "originalInfo(", displayName: "originalInfo() ", displayComment: "chart", categories: ["chart"]},
		{name: "plugin", value: "plugin", displayName: "plugin ", displayComment: "chart", categories: ["chart"]},
		{name: "refreshData", value: "refreshData()", displayName: "refreshData() ", displayComment: "chart", categories: ["chart"]},
		{name: "registerEventHandlerDelegation", value: "registerEventHandlerDelegation(", displayName: "registerEventHandlerDelegation() ", displayComment: "chart", categories: ["chart"]},
		{name: "removeEventHandlerDelegation", value: "removeEventHandlerDelegation(", displayName: "removeEventHandlerDelegation() ", displayComment: "chart", categories: ["chart"]},
		{name: "render", value: "render()", displayName: "render() ", displayComment: "chart", categories: ["chart"]},
		{name: "renderContext", value: "renderContext", displayName: "renderContext ", displayComment: "chart", categories: ["chart"]},
		{name: "renderContextAttr", value: "renderContextAttr(", displayName: "renderContextAttr() ", displayComment: "chart", categories: ["chart"]},
		{name: "renderOptions", value: "renderOptions(", displayName: "renderOptions() ", displayComment: "chart", categories: ["chart"]},
		{name: "renderer", value: "renderer(", displayName: "renderer() ", displayComment: "chart", categories: ["chart"]},
		{name: "resetDataSetParamValues", value: "resetDataSetParamValues(", displayName: "resetDataSetParamValues() ", displayComment: "chart", categories: ["chart"]},
		{name: "resetDataSetParamValuesFirst", value: "resetDataSetParamValuesFirst()", displayName: "resetDataSetParamValuesFirst() ", displayComment: "chart", categories: ["chart"]},
		{name: "resize", value: "resize()", displayName: "resize() ", displayComment: "chart", categories: ["chart"]},
		{name: "resultAt", value: "resultAt(", displayName: "resultAt() ", displayComment: "chart", categories: ["chart"]},
		{name: "resultCell", value: "resultCell(", displayName: "resultCell() ", displayComment: "chart", categories: ["chart"]},
		{name: "resultColumnArrays", value: "resultColumnArrays(", displayName: "resultColumnArrays() ", displayComment: "chart", categories: ["chart"]},
		{name: "resultData", value: "resultData(", displayName: "resultData() ", displayComment: "chart", categories: ["chart"]},
		{name: "resultDataElement", value: "resultDataElement(", displayName: "resultDataElement() ", displayComment: "chart", categories: ["chart"]},
		{name: "resultDataFormat", value: "resultDataFormat(", displayName: "resultDataFormat() ", displayComment: "chart", categories: ["chart"]},
		{name: "resultDatas", value: "resultDatas(", displayName: "resultDatas() ", displayComment: "chart", categories: ["chart"]},
		{name: "resultDatasFirst", value: "resultDatasFirst(", displayName: "resultDatasFirst() ", displayComment: "chart", categories: ["chart"]},
		{name: "resultFirst", value: "resultFirst(", displayName: "resultFirst() ", displayComment: "chart", categories: ["chart"]},
		{name: "resultMapObjects", value: "resultMapObjects(", displayName: "resultMapObjects() ", displayComment: "chart", categories: ["chart"]},
		{name: "resultNameValueObjects", value: "resultNameValueObjects(", displayName: "resultNameValueObjects() ", displayComment: "chart", categories: ["chart"]},
		{name: "resultOf", value: "resultOf(", displayName: "resultOf() ", displayComment: "chart", categories: ["chart"]},
		{name: "resultRowArrays", value: "resultRowArrays(", displayName: "resultRowArrays() ", displayComment: "chart", categories: ["chart"]},
		{name: "resultRowCell", value: "resultRowCell(", displayName: "resultRowCell() ", displayComment: "chart", categories: ["chart"]},
		{name: "resultValueObjects", value: "resultValueObjects(", displayName: "resultValueObjects() ", displayComment: "chart", categories: ["chart"]},
		{name: "status", value: "status(", displayName: "status() ", displayComment: "chart", categories: ["chart"]},
		{name: "statusDestroyed", value: "statusDestroyed(", displayName: "statusDestroyed() ", displayComment: "chart", categories: ["chart"]},
		{name: "statusPreRender", value: "statusPreRender(", displayName: "statusPreRender() ", displayComment: "chart", categories: ["chart"]},
		{name: "statusPreUpdate", value: "statusPreUpdate(", displayName: "statusPreUpdate() ", displayComment: "chart", categories: ["chart"]},
		{name: "statusRendered", value: "statusRendered(", displayName: "statusRendered() ", displayComment: "chart", categories: ["chart"]},
		{name: "statusRendering", value: "statusRendering(", displayName: "statusRendering() ", displayComment: "chart", categories: ["chart"]},
		{name: "statusUpdated", value: "statusUpdated(", displayName: "statusUpdated() ", displayComment: "chart", categories: ["chart"]},
		{name: "statusUpdating", value: "statusUpdating(", displayName: "statusUpdating() ", displayComment: "chart", categories: ["chart"]},
		{name: "styleString", value: "styleString(", displayName: "styleString() ", displayComment: "chart", categories: ["chart"]},
		{name: "theme", value: "theme(", displayName: "theme() ", displayComment: "chart", categories: ["chart"]},
		{name: "themeStyleName", value: "themeStyleName()", displayName: "themeStyleName() ", displayComment: "chart", categories: ["chart"]},
		{name: "themeStyleSheet", value: "themeStyleSheet(", displayName: "themeStyleSheet() ", displayComment: "chart", categories: ["chart"]},
		{name: "update", value: "update(", displayName: "update() ", displayComment: "chart", categories: ["chart"]},
		{name: "updateGroup", value: "updateGroup(", displayName: "updateGroup() ", displayComment: "chart", categories: ["chart"]},
		{name: "updateInterval", value: "updateInterval", displayName: "updateInterval ", displayComment: "chart", categories: ["chart"]},
		{name: "updateResults", value: "updateResults(", displayName: "updateResults() ", displayComment: "chart", categories: ["chart"]},
		{name: "widgetId", value: "widgetId()", displayName: "widgetId() ", displayComment: "chart", categories: ["chart"]}
	];
	
	po.getDashboardId = function()
	{
		return  po.element("input[name='id']").val();
	};
	
	po.getSelectedResourceNameForTree = function($tree)
	{
		var tree = $tree.jstree(true);
		var sel = tree.get_selected(true);
		
		if(sel && sel.length > 0)
			return sel[0].original.fullPath;
		else
			return undefined;
	};
	
	po.deselectResourceNameForTree = function($tree)
	{
		var tree = $tree.jstree(true);
		var sel = tree.get_selected();
		tree.deselect_node(sel);
	};
	
	po.getSelectedResourceNameForTemplate = function()
	{
		var $template = po.elementResListLocal(".resource-list-template > .resource-item.ui-state-active");
		
		if($template.length > 0)
			return $template.attr("resource-name");
		
		return undefined;
	};
	
	po.deselectResourceNameForSelectable = function()
	{
		var $template = po.elementResListLocal(".resource-list-template > .resource-item.ui-state-active");
		$template.removeClass("ui-state-active");
	};
	
	po.getSelectedResourceName = function()
	{
		var resName = po.getSelectedResourceNameForTemplate();
		
		if(resName)
			return resName;
		else
			return po.getSelectedResourceNameForTree(po.elementResListLocal(".resource-list-content"));
	};
	
	po.addDashboardResourceItemTemplate = function($parent, templateName, prepend)
	{
		var $res = $("<div class='resource-item resource-item-template ui-corner-all'></div>").attr("resource-name", templateName).text(templateName);
		$res.prepend($("<span class='ui-icon ui-icon-contact'></span>").attr("title", "<@spring.message code='dashboard.dashboardTemplateResource' />"));
		$("<input type='hidden' name='templates[]' />").attr("value", templateName).appendTo($res);
		
		if(prepend == true)
			$parent.prepend($res);
		else
		{
			var last = $(".resource-item-template", $parent).last();
			if(last.length == 0)
				$parent.prepend($res);
			else
				last.after($res);
		}
	};
	
	po.getTemplateIndex = function(templateName, templates)
	{
		templates = (templates || po.templates);
		
		for(var i=0; i<templates.length; i++)
		{
			if(templates[i] == templateName)
				return i;
		}
		
		return -1;
	};
	
	po.isResourceNameDirectroy = function(resName)
	{
		return (resName && resName.charAt(resName.length - 1) == '/');
	};
	
	po.resourceNamesToTreeData = function(resourceNames, idPrefix)
	{
		if(idPrefix == null)
			idPrefix = "";
		
		return $.toPathTree(resourceNames,
				{
					nameProperty: "text", childrenProperty: "children",
					fullPathProperty: "fullPath",
					created: function(node)
					{
						node.id = idPrefix + node.fullPath;
					}
				});
	};
	
	po.refreshResourceListLocal = function()
	{
		var id = po.getDashboardId();
		
		if(!id)
			return;
		
		po.elementResListLocal(".resource-list-content").jstree(true).refresh(true);
	};
	
	po.elementResListLocal(".resource-list-template").selectable
	({
		classes: {"ui-selected": "ui-state-active"},
		filter: ".resource-item",
		selected: function()
		{
			po.deselectResourceNameForTree(po.elementResListLocal(".resource-list-content"));
		}
	})
	.on("mouseenter", ".resource-item", function()
	{
		var $this = $(this);
		$this.addClass("ui-state-default");
	})
	.on("mouseleave", ".resource-item", function()
	{
		var $this = $(this);
		$this.removeClass("ui-state-default");
	});
	
	po.elementResListLocal(".resource-list-content").jstree(
	{
		core:
		{
			data: function(node, callback)
			{
				var _this = this;
				
				//根节点
				if(node.id == "#")
				{
					var id = po.getDashboardId();
					
					if(!id)
					{
						callback.call(_this, []);
						return;
					}
					
					$.get(po.url("listResources?id="+id), function(resources)
					{
						resources = (resources || []);
						
						var $templates = po.elementResListLocal(".resource-list-template");
						$templates.empty();
						
						for(var i=0; i<po.templates.length; i++)
						{
							for(var j=0; j<resources.length; j++)
							{
								if(po.templates[i] == resources[j])
									po.addDashboardResourceItemTemplate($templates, resources[j]);
							}
						}
						
						var treeData = po.resourceNamesToTreeData(resources, "resLocal-");
						callback.call(_this, treeData);
					});
				}
			},
			check_callback: true,
			themes: {dots:false, icons: true}
		}
	})
	.bind("select_node.jstree", function()
	{
		po.deselectResourceNameForSelectable();
	})
	.bind("select_all.jstree", function()
	{
		po.deselectResourceNameForSelectable();
	});
	
	po.element(".resize-editor-button-left").click(function()
	{
		var $ele = po.element();
		var $icon = $(".ui-icon", this);
		
		if($ele.hasClass("max-resource-editor-left"))
		{
			$ele.removeClass("max-resource-editor-left");
			$icon.removeClass("ui-icon-arrowstop-1-e").addClass("ui-icon-arrowstop-1-w");
		}
		else
		{
			$ele.addClass("max-resource-editor-left");
			$icon.removeClass("ui-icon-arrowstop-1-w").addClass("ui-icon-arrowstop-1-e");
		}
	});
	
	var copyResNameButton = po.elementResListLocal(".copyResNameButton");
	if(copyResNameButton.length > 0)
	{
		var clipboard = new ClipboardJS(copyResNameButton[0],
		{
			//需要设置container，不然在对话框中打开页面后复制不起作用
			container: po.element()[0],
			text: function(trigger)
			{
				var text = po.getSelectedResourceName();
				if(!text)
					text = "";
				
				return text;
			}
		});
		clipboard.on('success', function(e)
		{
			$.tipSuccess("<@spring.message code='copyToClipboardSuccess' />");
		});
	}
	
	po.elementResListLocal(".add-resource-panel").draggable({ handle : ".addResPanelHead" });
	po.elementResListLocal(".upload-resource-panel").draggable({ handle : ".uploadResPanelHead" });
	
	po.elementResListLocal(".resource-more-button-wrapper").hover(
	function()
	{
		po.elementResListLocal(".resource-more-button-panel").show();
	},
	function()
	{
		po.elementResListLocal(".resource-more-button-panel").hide();
	});
	
	po.checkDashboardSaved = function()
	{
		var id = po.getDashboardId();
		
		if(!id)
		{
			$.tipInfo("<@spring.message code='dashboard.pleaseSaveDashboardFirst' />");
			return false;
		}
		
		return true;
	};
	
	po.elementResListLocal(".addResBtn").click(function()
	{
		var initVal = po.getSelectedResourceName();
		if(!po.isResourceNameDirectroy(initVal))
			initVal = "";
		
		po.elementResListLocal(".addResNameInput").val(initVal);
		po.elementResListLocal(".add-resource-panel").show();
	});
	
	po.elementResListLocal(".addResNameInput").on("keydown", function(e)
	{
		if(e.keyCode == $.ui.keyCode.ENTER)
		{
			po.elementResListLocal(".saveAddResBtn").click();
			//防止提交表单
			return false;
		}
	});
	
	po.elementResListLocal(".saveAddResBtn").click(function()
	{
		var name = po.elementResListLocal(".addResNameInput").val();
		if(!name)
			return;
		
		if(po.isResourceNameDirectroy(name))
		{
			$.tipInfo("<@spring.message code='dashboard.illegalSaveAddResourceName' />");
			return;
		}
		
		var content = "";
		var isHtml = $.isHtmlFile(name);
		
		if(isHtml)
			content = po.element("#${pageId}-defaultTemplateContent").val();
		
		po.newResourceEditorTab(name, content, isHtml);
		po.elementResListLocal(".add-resource-panel").hide();
	});
	
	po.elementResListLocal(".editResBtn").click(function()
	{
		if(!po.checkDashboardSaved())
			return;
		
		var resName = po.getSelectedResourceName();
		
		if(!resName)
			return;
		
	 	if(!$.isTextFile(resName))
	 	{
	 		$.tipInfo("<@spring.message code='dashboard.editResUnsupport' />");
	 		return;
	 	}
	 	
	 	var editIndex = -1;
	 	var tabsNav = po.getTabsNav(po.resourceEditorTabs);
	 	$(".resource-editor-tab", tabsNav).each(function(index)
	 	{
	 		if($(this).attr("resourceName") == resName)
	 		{
	 			editIndex = index;
	 			return false;
	 		}
	 	});
	 	
	 	if(editIndex > -1)
	 	{
	 		po.resourceEditorTabs.tabs( "option", "active",  editIndex);
	 	}
	 	else
	 	{
		 	$.get(po.url("getResourceContent"), {"id": po.getDashboardId(), "resourceName": resName}, function(data)
		 	{
		 		var isTemplate = (po.getTemplateIndex(data.resourceName) > -1);
		 		po.newResourceEditorTab(data.resourceName, data.resourceContent, isTemplate);
		 	});
	 	}
	});
	
	po.elementResListLocal(".uploadResBtn").click(function()
	{
		var id = po.getDashboardId();
		
		if(!id)
		{
			$.tipInfo("<@spring.message code='dashboard.pleaseSaveDashboardFirst' />");
			return;
		}
		
		po.elementResListLocal(".uploadResNameInput").val("");
		po.elementResListLocal(".uploadResFilePath").val("");
		po.elementResListLocal(".upload-file-info").text("");
		
		var $panel = po.elementResListLocal(".upload-resource-panel");
		$panel.show();
		//$panel.position({ my : "right top", at : "right+20 bottom+3", of : this});
	});

	po.elementResListLocal(".uploadResNameInput").on("keydown", function(e)
	{
		if(e.keyCode == $.ui.keyCode.ENTER)
		{
			po.elementResListLocal(".saveUploadResourceButton").click();
			//防止提交表单
			return false;
		}
	});
	
	po.elementResListLocal(".saveUploadResourceButton").click(function()
	{
		var id = po.getDashboardId();
		var resourceFilePath = po.elementResListLocal(".uploadResFilePath").val();
		var resourceName = po.elementResListLocal(".uploadResNameInput").val();
		
		if(!id || !resourceFilePath || !resourceName)
			return;
		
		$.post(po.url("saveResourceFile"), {"id": id, "resourceFilePath": resourceFilePath, "resourceName": resourceName},
		function()
		{
			po.refreshResourceListLocal();
			po.elementResListLocal(".upload-resource-panel").hide();
		});
	});
	
	po.elementResListLocal(".viewResButton").click(function(e)
	{
		var id = po.getDashboardId();
		
		if(!id)
		{
			$.tipInfo("<@spring.message code='dashboard.pleaseSaveDashboardFirst' />");
			return;
		}
		
		var path = po.getSelectedResourceName();
		
		if(!path)
			return;
		
		window.open(po.showUrl(id, path));
	});
	
	po.elementResListLocal(".asTemplateBtn").click(function()
	{
		if(!po.checkDashboardSaved())
			return;
		
		var resName = po.getSelectedResourceNameForTree(po.elementResListLocal(".resource-list-content"));
		
		if(!resName)
			return;
		
		if(!$.isHtmlFile(resName))
		{
	 		$.tipInfo("<@spring.message code='dashboard.resAsTemplateUnsupport' />");
	 		return;
		}
		
		var $templates = po.elementResListLocal(".resource-item-template");
		for(var i=0; i<$templates.length; i++)
		{
			if($($templates[i]).attr("resource-name") == resName)
				return;
		}
		
		var templates = po.templates.concat([]);
		templates.push(resName);
		
		po.saveTemplateNames(templates);
	});
	
	po.elementResListLocal(".asNormalResBtn").click(function()
	{
		if(!po.checkDashboardSaved())
			return;
		
		var resName = po.getSelectedResourceNameForTemplate();
		
		if(!resName)
			return;
		
		var templates = po.templates.concat([]);
		var idx = po.getTemplateIndex(resName, templates);
		if(idx > -1)
			templates.splice(idx, 1);
		
		po.saveTemplateNames(templates);
	});
	
	po.elementResListLocal(".asFirstTemplateBtn").click(function()
	{
		if(!po.checkDashboardSaved())
			return;
		
		var resName = po.getSelectedResourceNameForTemplate();
		
		if(!resName)
			return;
		
		var templates = po.templates.concat([]);
		var idx = po.getTemplateIndex(resName, templates);
		if(idx > -1)
			templates.splice(idx, 1);
		templates.unshift(resName);
		
		po.saveTemplateNames(templates);
	});
	
	po.saveTemplateNames = function(templateNames, success)
	{
		if(templateNames == null || templateNames.length == 0)
		{
			$.tipInfo("<@spring.message code='dashboard.atLeastOneTemplateRequired' />");
			return;
		}
		
		var id = po.getDashboardId();
		
		$.ajaxJson(po.url("saveTemplateNames?id="+id),
		{
			data: templateNames,
			success : function(response)
			{
				po.templates = response.data.templates;
				po.refreshResourceListLocal();
				
				if(success)
					success();
			}
		});
	};
	
	po.elementResListLocal(".refreshResListBtn").click(function()
	{
		var id = po.getDashboardId();
		
		if(!id)
		{
			$.tipInfo("<@spring.message code='dashboard.pleaseSaveDashboardFirst' />");
			return;
		}
		
		po.refreshResourceListLocal();
	});
	
	po.elementResListLocal(".deleteResBtn").click(function()
	{
		var id = po.getDashboardId();
		
		if(!id)
		{
			$.tipInfo("<@spring.message code='dashboard.pleaseSaveDashboardFirst' />");
			return;
		}
		
		var name = po.getSelectedResourceName();
		
		if(!name)
			return;
		
		po.confirm("<@spring.message code='dashboard.confirmDeleteSelectedResource' />",
		{
			"confirm" : function()
			{
				$.post(po.url("deleteResource"), {"id": id, "name" : name},
				function(response)
				{
					po.refreshResourceListLocal();
				});
			}
		});
	});
	
	po.fileUploadInfo = function(){ return this.elementResListLocal(".upload-file-info"); };
	
	po.elementResListLocal(".fileinput-button").fileupload(
	{
		url : po.url("uploadResourceFile"),
		paramName : "file",
		success : function(uploadResult, textStatus, jqXHR)
		{
			var parent = po.getSelectedResourceName();
			if(!po.isResourceNameDirectroy(parent))
				parent = "";
			
			po.elementResListLocal(".uploadResNameInput").val(parent + uploadResult.fileName);
			po.elementResListLocal(".uploadResFilePath").val(uploadResult.uploadFilePath);
			
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

	po.element().on("click", function(event)
	{
		var $target = $(event.target);
		
		var $p0 = po.elementResListLocal(".add-resource-panel");
		if(!$p0.is(":hidden"))
		{
			if($target.closest(".add-resource-panel, .addResBtn").length == 0)
				$p0.hide();
		}
		
		var $p1 = po.elementResListLocal(".upload-resource-panel");
		if(!$p1.is(":hidden"))
		{
			if($target.closest(".upload-resource-panel, .uploadResBtn").length == 0)
				$p1.hide();
		}
		
		var $p2 = po.element(".chart-list-panel");
		if(!$p2.is(":hidden"))
		{
			if($target.closest(".chart-list-panel, .insert-chart-button").length == 0)
				$p2.hide();
		}
	});
	
	po.initResListGlobalIfNon = function()
	{
		var $tree = po.elementResListGlobal(".resource-list-content");
		var tree = $.jstree.reference($tree);
		
		if(tree != null)
			return;
		
		$tree.jstree(
		{
			core:
			{
				data: function(node, callback)
				{
					var _this = this;
					
					//根节点
					if(node.id == "#")
					{
						var keyword = po.elementResListGlobal(".search-input").val();
						
						$.postJson("${contextPath}/dashboardGlobalRes/queryData", { "keyword": keyword }, function(resources)
						{
							resources = (resources || []);
							
							if(!resources || resources.length == 0)
							{
								po.elementResListGlobal(".resource-none").show();
								po.elementResListGlobal(".resource-list-content").hide();
							}
							else
							{
								po.elementResListGlobal(".resource-none").hide();
								po.elementResListGlobal(".resource-list-content").show();
							}
							
							var resNames = [];
							for(var i=0; i<resources.length; i++)
								resNames[i] = resources[i].path;
							
							var treeData = po.resourceNamesToTreeData(resNames, "resGlobal-");
							callback.call(_this, treeData);
						});
					}
				},
				check_callback: true,
				themes: {dots:false, icons: true}
			}
		});
	};
	
	po.refreshResourceListGlobal = function()
	{
		po.elementResListGlobal(".resource-list-content").jstree(true).refresh(true);
	};
	
	po.elementResListGlobal(".search-input").on("keydown", function(e)
	{
		if(e.keyCode == $.ui.keyCode.ENTER)
		{
			po.elementResListGlobal(".search-button").click();
			//防止提交表单
			return false;
		}
	});
	
	po.elementResListGlobal(".search-button").click(function(e)
	{
		po.refreshResourceListGlobal();
	});
	
	po.elementResListGlobal(".viewResButton").click(function(e)
	{
		var id = po.getDashboardId();
		
		if(!id)
		{
			$.tipInfo("<@spring.message code='dashboard.pleaseSaveDashboardFirst' />");
			return;
		}
		
		var path = po.getSelectedResourceGlobalName();
		
		if(!path)
			return;
		
		window.open(po.showUrl(id, path));
	});

	po.elementResListGlobal(".refreshResListBtn").click(function()
	{
		po.refreshResourceListGlobal();
	});
	
	var copyResGlobalNameButton = po.elementResListGlobal(".copyResNameButton");
	if(copyResGlobalNameButton.length > 0)
	{
		var clipboard = new ClipboardJS(copyResGlobalNameButton[0],
		{
			//需要设置container，不然在对话框中打开页面后复制不起作用
			container: po.element()[0],
			text: function(trigger)
			{
				var text = po.getSelectedResourceGlobalName();
				if(!text)
					text = "";
				
				return text;
			}
		});
		clipboard.on('success', function(e)
		{
			$.tipSuccess("<@spring.message code='copyToClipboardSuccess' />");
		});
	}
	
	po.getSelectedResourceGlobalName = function()
	{
		var name = po.getSelectedResourceNameForTree(po.elementResListGlobal(".resource-list-content"));
		
		if(name)
			name = po.dashboardGlobalResUrlPrefix + name;
		
		return name;
	};
	
	po.getResourceEditorData = function()
	{
		var data = {};
		data.resourceNames=[];
		data.resourceContents=[];
		data.resourceIsTemplates=[];
		
		po.element(".resource-editor-tab-pane").each(function()
		{
			var tp = $(this);
			
			var codeEditorDiv = po.element(".code-editor", tp);
			var codeEditor = codeEditorDiv.data("resourceEditorInstance");
			
			data.resourceNames.push($(".resourceName", tp).val());
			data.resourceIsTemplates.push($(".resourceIsTemplate", tp).val());
			data.resourceContents.push(po.getCodeText(codeEditor));
		});
		
		return data;
	};
	
	po.showAfterSave = false;
	
	po.element("button[id='saveAndShowDashboard']").click(function()
	{
		po.showAfterSave = true;
		po.form().submit();
	});
	
	po.form().validate(
	{
		ignore : "",
		rules :
		{
			"name" : "required"
		},
		messages :
		{
			"name" : "<@spring.message code='validation.required' />"
		},
		submitHandler : function(form)
		{
			$(form).ajaxSubmitJson(
			{
				handleData: function(data)
				{
					var newData = po.getResourceEditorData();
					newData.dashboard = data;
					newData.copySourceId = po.element("#${pageId}-copySourceId").val();
					
					var templateCount = (newData.dashboard.templates == null ? 0 : newData.dashboard.templates.length);
					for(var i=0; i<newData.resourceIsTemplates.length; i++)
					{
						if(newData.resourceIsTemplates[i] == "true")
							templateCount++;
					}
					
					if(templateCount == 0)
					{
						$.tipInfo("<@spring.message code='dashboard.atLeastOneTemplateRequired' />");
						po.showAfterSave = false;
						
						return false;
					}
					
					return newData;
				},
				success : function(response)
				{
					var isSaveAdd = !po.getDashboardId();
					
					var dashboard = response.data;
					po.element("input[name='id']").val(dashboard.id);
					po.templates = dashboard.templates;
					
					if(po.showAfterSave)
						window.open(po.showUrl(dashboard.id), dashboard.id);
					
					var close = po.pageParamCallAfterSave(false);
					if(!close)
						po.refreshResourceListLocal();
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
	
	if(po.getDashboardId() || po.element("#${pageId}-copySourceId").val())
		po.element(".resize-editor-button-left").click();
	
	po.newResourceEditorTab(po.element("#${pageId}-initTemplateName").val(), po.element("#${pageId}-initTemplateContent").val(), true);
})
(${pageId});
</script>
</body>
</html>
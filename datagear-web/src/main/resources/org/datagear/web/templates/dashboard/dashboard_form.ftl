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
									</div>
									</#if>
									<div class="resource-button-wrapper rbw-right">
										<#if !readonly>
										<button type='button' class='editResBtn resource-button ui-button ui-corner-all ui-widget ui-button-icon-only' title="<@spring.message code='dashboard.editResource.desc' />"><span class='ui-icon ui-icon-pencil'></span><span class='ui-button-icon-space'></span></button>
										<button type='button' class='uploadResBtn resource-button ui-button ui-corner-all ui-widget ui-button-icon-only' title="<@spring.message code='dashboard.uploadResource' />"><span class='ui-icon ui-icon-arrowstop-1-n'></span><span class='ui-button-icon-space'></span></button>
										<button type='button' class='deleteResBtn resource-button ui-button ui-corner-all ui-widget ui-button-icon-only' title="<@spring.message code='dashboard.deleteResource' />"><span class='ui-icon ui-icon-close'></span><span class='ui-button-icon-space'></span></button>
										<div class="resource-more-button-wrapper">
											<span class="resource-more-icon ui-icon ui-icon-caret-1-s"></span>
											<div class="resource-more-button-panel ui-widget ui-widget-content ui-corner-all ui-front ui-widget-shadow">
												<button type='button' class='copyResNameButton resource-button ui-button ui-corner-all ui-widget ui-button-icon-only' title="<@spring.message code='dashboard.copyResourceNameToClipboard' />"><span class='ui-icon ui-icon-copy'></span><span class='ui-button-icon-space'></span></button>
												<button type='button' class='viewResButton resource-button ui-button ui-corner-all ui-widget ui-button-icon-only' title="<@spring.message code='dashboard.viewResource' />"><span class='ui-icon ui-icon-extlink'></span><span class='ui-button-icon-space'></span></button>
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
							<div id="${pageId}-resourceListGlobal" class="resource-list-wrapper resource-list-global-wrapper content-unloaded ui-widget ui-widget-content ui-corner-all">
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
								<div class="resource-list-content"></div>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
		<div class="form-foot" style="text-align:center;">
			<#if !readonly>
			<input type="submit" value="<@spring.message code='save' />" class="recommended" />
			&nbsp;&nbsp;
			<button type="button" name="saveAndShow"><@spring.message code='dashboard.saveAndShow' /></button>
			</#if>
		</div>
	</form>
</div>
<#include "../include/page_obj_form.ftl">
<#include "../include/page_obj_tabs.ftl" >
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
			{
				if(newPanel.hasClass("content-unloaded"))
				{
					newPanel.removeClass("content-unloaded");
					po.refreshResourceGlobalList();
				}
			}
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
	
	po.showUrl = function(dashboardId)
	{
		return po.url("show/"+dashboardId+"/");
	};
	
	po.resourceEditorTabTemplate = "<li class='resource-editor-tab' style='vertical-align:middle;'><a href='"+'#'+"{href}'>"+'#'+"{label}</a>"
		+"<div class='tab-operation'>"
		+"<span class='ui-icon ui-icon-close' title='<@spring.message code='close' />'>close</span>"
		+"<div class='tabs-more-operation-button' title='<@spring.message code='moreOperation' />'><span class='ui-icon ui-icon-caret-1-e'></span></div>"
		+"</div>"
		+"</li>";
	
	po.newResourceEditorTab = function(name, content, isTemplate)
	{
		var label = name;
		var labelMaxLen = 5 + 3 + 10;
		if(label.length > labelMaxLen)
			label = name.substr(0, 5) +"..." + name.substr(label.length - 10);
		
		var tabsNav = po.getTabsNav(po.resourceEditorTabs);
		var tabId = $.uid("resourceEditorTabPane");
    	var tab = $(po.resourceEditorTabTemplate.replace( /#\{href\}/g, "#" + tabId).replace(/#\{label\}/g, $.escapeHtml(label)))
    		.attr("id", $.uid("resourceEditorTab")).attr("resourceName", name).appendTo(tabsNav);
    	
    	var panePrevEle = $(".resource-editor-tab-pane", po.resourceEditorTabs).last();
    	if(panePrevEle.length == 0)
    		panePrevEle = $(".resource-editor-tab-nav", po.resourceEditorTabs);
    	var tabPane = $("<div id='"+tabId+"' class='resource-editor-tab-pane' />").insertAfter(panePrevEle);
    	var pc0 = $("<div class='form-item-value form-item-value-resource-name' />").appendTo(tabPane);
    	$("<label class='name-label'></label>").html("<@spring.message code='name' />").appendTo(pc0);
    	$("<input type='text' class='resourceName name-input ui-widget ui-widget-content' readonly='readonly' />").val(name).appendTo(pc0);
    	$("<input type='hidden' class='resourceIsTemplate' />").val(isTemplate).appendTo(pc0);
    	
    	var pc1 = $("<div class='editor-wrapper ui-widget ui-widget-content' />").appendTo(tabPane);
		var pc2 = $("<div class='resource-editor' />").attr("id", $.uid("resourceEditor")).appendTo(pc1);
		
		var editor = ace.edit(pc2.attr("id"));
		editor.setShowPrintMargin(false);
		
		ace.require("ace/ext/language_tools");
		
		if($.isHtmlFile(name))
		{
			var HtmlMode = ace.require("ace/mode/html").Mode;
			editor.session.setMode(new HtmlMode());
		}
		else if($.isJsFile(name))
		{
			var JsMode = ace.require("ace/mode/javascript").Mode;
			editor.session.setMode(new JsMode());
		}
		else if($.isCssFile(name))
		{
			var CssMode = ace.require("ace/mode/css").Mode;
			editor.session.setMode(new CssMode());
		}
		
		if(isTemplate)
		{
			editor.setOptions(
			{
				enableBasicAutocompletion: po.templateEditorCompleters,
				enableLiveAutocompletion: po.templateEditorCompleters
			});
		}
		
		var cursor = {row: 0, column: 0};
		editor.session.insert(cursor, content);
		
		if(isTemplate)
		{
			//光标移至"</body>"之前，便于用户直接编辑
			var found = editor.find("</body>",{backwards: true, wrap: false, caseSensitive: false, wholeWord: false, regExp: false}, true);
			if(found && found.start && found.start.row > 0)
			{
				cursor = {row: found.start.row-1, column: 1000};
				var selection = editor.session.getSelection();
				selection.clearSelection();
			}
			//滚动到底部
			editor.session.setScrollTop(99999);
		}
		
		editor.moveCursorToPosition(cursor);
		editor.focus();
		<#if readonly>
		editor.setReadOnly(true);
		</#if>
		
		tabPane.data("resourceEditorInstance", editor);
		
		var pc3 = $("<div class='editor-operation-wrapper' />").appendTo(tabPane);
		
		<#if !readonly>
		if(isTemplate)
		{
			var insertChartBtn = $("<button type='button' class='insert-chart-button' />")
				.text("<@spring.message code='dashboard.insertChart' />").appendTo(pc3).button()
				.click(function()
				{
					var options =
					{
						pageParam :
						{
							select : function(charts)
							{
								if(!$.isArray(charts))
									charts = [charts];
								
								po.insertChartCode(editor, charts);
								return true;
							}
						}
					};
					$.setGridPageHeightOption(options);
					po.open("${contextPath}/chart/select?multiple", options);
				});
		}
		</#if>
		
		var searchGroup = $("<div class='search-group ui-widget ui-widget-content ui-corner-all' />").appendTo(pc3);
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
					
					var searchOptions = {backwards: false, wrap: true, caseSensitive: false, wholeWord: false, regExp: false};
					
					var prevSearchText = $this.data("prevSearchText");
					
					if(text == prevSearchText)
						editor.findNext(searchOptions, true);
					else
					{
						editor.find(text, searchOptions, true);
						$this.data("prevSearchText", text);
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
	
	po.getTemplatePrevTagText = function(editor, row, column)
	{
		var text = editor.session.getLine(row).substring(0, column);
		
		//反向查找直到'>'或'<'
		var prevRow = row;
		while((!text || !(/[<>]/g.test(text))) && (prevRow--) >= 0)
			text = editor.session.getLine(prevRow) + text;
		
		return po.getLastTagText(text);
	};
	
	po.resolveHtmlTagName = function(text)
	{
		var re = text.match(/<\S*/);
		
		if(re)
			return re[0].substr(1);
		
		return "";
	};
	
	po.getTemplatePrevToken = function(editor, row, column)
	{
		var text = editor.session.getLine(row).substring(0, column);
		
		//反向查找直到非空串
		var prevRow = row;
		while((!text || /^\s*$/.test(text)) && (prevRow--) >= 0)
			text = editor.session.getLine(prevRow) + text;
		
		return text;
	};
	
	po.templateEditorCompletionsTagAttr = [
		{name: "dg-chart-widget", value: "dg-chart-widget", caption: "",
			meta: "<@spring.message code='dashboard.templateEditor.autoComplete.dg-chart-widget' />", tagNames: ["div"]},
		{name: "dg-chart-options", value: "dg-chart-options", caption: "",
			meta: "<@spring.message code='dashboard.templateEditor.autoComplete.dg-chart-options' />", tagNames: ["body","div"]},
		{name: "dg-chart-theme", value: "dg-chart-theme", caption: "",
			meta: "<@spring.message code='dashboard.templateEditor.autoComplete.dg-chart-theme' />", tagNames: ["body", "div"]},
		{name: "dg-chart-map", value: "dg-chart-map", caption: "",
			meta: "<@spring.message code='dashboard.templateEditor.autoComplete.dg-chart-map' />", tagNames: ["div"]},
		{name: "dg-chart-link", value: "dg-chart-link", caption: "",
			meta: "<@spring.message code='dashboard.templateEditor.autoComplete.dg-chart-link' />", tagNames: ["div"]},
		{name: "dg-chart-auto-resize", value: "dg-chart-auto-resize=\"true\"", caption: "dg-chart-aut..ize",
			meta: "<@spring.message code='dashboard.templateEditor.autoComplete.dg-chart-auto-resize' />", tagNames: ["body", "div"]},
		{name: "dg-chart-disable-setting", value: "dg-chart-disable-setting", caption: "dg-chart-dis..ing",
			meta: "<@spring.message code='dashboard.templateEditor.autoComplete.dg-chart-disable-setting' />", tagNames: ["body", "div"]},
		{name: "dg-chart-on-", value: "dg-chart-on-", caption: "",
			meta: "<@spring.message code='dashboard.templateEditor.autoComplete.dg-chart-on-' />", tagNames: ["div"]},
		{name: "dg-chart-map-urls", value: "dg-chart-map-urls", caption: "",
			meta: "<@spring.message code='dashboard.templateEditor.autoComplete.dg-chart-map-urls' />", tagNames: ["body"]},
		{name: "dg-chart-listener", value: "dg-chart-listener", caption: "",
			meta: "<@spring.message code='dashboard.templateEditor.autoComplete.dg-chart-listener' />", tagNames: ["body", "div"]},
		{name: "dg-chart-renderer", value: "dg-chart-renderer", caption: "",
			meta: "<@spring.message code='dashboard.templateEditor.autoComplete.dg-chart-renderer' />", tagNames: ["div"]},
		{name: "dg-chart-update-group", value: "dg-chart-update-group", caption: "dg-chart-upd...oup",
			meta: "<@spring.message code='dashboard.templateEditor.autoComplete.dg-chart-update-group' />", tagNames: ["body", "div"]},
		{name: "dg-echarts-theme", value: "dg-echarts-theme", caption: "",
			meta: "<@spring.message code='dashboard.templateEditor.autoComplete.dg-echarts-theme' />", tagNames: ["body", "div"]},
		{name: "dg-dashboard-listener", value: "dg-dashboard-listener", caption: "",
			meta: "<@spring.message code='dashboard.templateEditor.autoComplete.dg-dashboard-listener' />", tagNames: ["body"]},
		{name: "dg-dashboard-form", value: "dg-dashboard-form", caption: "",
			meta: "<@spring.message code='dashboard.templateEditor.autoComplete.dg-dashboard-form' />", tagNames: ["form"]}
	];
	
	po.templateEditorCompletionsJsFunction = [
		//看板JS对象
		{name: "addChart", value: "addChart(", caption: "addChart()", meta: "dashboard"},
		{name: "chartIndex", value: "chartIndex(", caption: "chartIndex()", meta: "dashboard"},
		{name: "chartOf", value: "chartOf(", caption: "chartOf()", meta: "dashboard"},
		{name: "batchSetDataSetParamValues", value: "batchSetDataSetParamValues(", caption: "batchSetDataSetParamValues()", meta: "dashboard"},
		{name: "charts", value: "charts", caption: "charts", meta: "dashboard"},
		{name: "doRender", value: "doRender()", caption: "doRender()", meta: "dashboard"},
		{name: "init", value: "init()", caption: "init()", meta: "dashboard"},
		{name: "isHandlingCharts", value: "isHandlingCharts()", caption: "isHandlingCharts()", meta: "dashboard"},
		{name: "isWaitForRender", value: "isWaitForRender(", caption: "isWaitForRender()", meta: "dashboard"},
		{name: "isWaitForUpdate", value: "isWaitForUpdate(", caption: "isWaitForUpdate()", meta: "dashboard"},
		{name: "listener", value: "listener(", caption: "listener()", meta: "dashboard"},
		{name: "loadChart", value: "loadChart(", caption: "loadChart()", meta: "dashboard"},
		{name: "loadCharts", value: "loadCharts(", caption: "loadCharts()", meta: "dashboard"},
		{name: "loadUnsolvedCharts", value: "loadUnsolvedCharts()", caption: "loadUnsolvedCharts()", meta: "dashboard"},
		{name: "refreshData", value: "refreshData(", caption: "refreshData()", meta: "dashboard"},
		{name: "removeChart", value: "removeChart(", caption: "removeChart()", meta: "dashboard"},
		{name: "render", value: "render()", caption: "render()", meta: "dashboard"},
		{name: "renderContext", value: "renderContext", caption: "renderContext", meta: "dashboard"},
		{name: "renderContextAttr", value: "renderContextAttr(", caption: "renderContextAttr()", meta: "dashboard"},
		{name: "renderForm", value: "renderForm(", caption: "renderForm()", meta: "dashboard"},
		{name: "renderedChart", value: "renderedChart(", caption: "renderedChart()", meta: "dashboard"},
		{name: "resizeAllCharts", value: "resizeAllCharts()", caption: "resizeAllCharts()", meta: "dashboard"},
		{name: "resizeChart", value: "resizeChart(", caption: "resizeChart()", meta: "dashboard"},
		{name: "resultDataFormat", value: "resultDataFormat(", caption: "resultDataFormat()", meta: "dashboard"},
		{name: "serverDate", value: "serverDate()", caption: "serverDate()", meta: "dashboard"},
		{name: "startHandleCharts", value: "startHandleCharts()", caption: "startHandleCharts()", meta: "dashboard"},
		{name: "stopHandleCharts", value: "stopHandleCharts()", caption: "stopHandleCharts()", meta: "dashboard"},
		
		//图表JS对象
		{name: "autoResize", value: "autoResize(", caption: "autoResize()", meta: "chart"},
		{name: "bindLinksEventHanders", value: "bindLinksEventHanders(", caption: "bindLinksEventHanders()", meta: "chart"},
		{name: "chartDataSetAt", value: "chartDataSetAt(", caption: "chartDataSetAt()", meta: "chart"},
		{name: "chartDataSetFirst", value: "chartDataSetFirst(", caption: "chartDataSetFirst()", meta: "chart"},
		{name: "chartDataSetName", value: "chartDataSetName(", caption: "chartDataSetName()", meta: "chart"},
		{name: "chartDataSets", value: "chartDataSets", caption: "chartDataSets", meta: "chart"},
		{name: "chartDataSetsAttachment", value: "chartDataSetsAttachment()", caption: "chartDataSetsAttachment()", meta: "chart"},
		{name: "chartDataSetsMain", value: "chartDataSetsMain()", caption: "chartDataSetsMain()", meta: "chart"},
		{name: "dashboard", value: "dashboard", caption: "dashboard", meta: "chart"},
		{name: "dataSetParamValue", value: "dataSetParamValue(", caption: "dataSetParamValue()", meta: "chart"},
		{name: "dataSetParamValueFirst", value: "dataSetParamValueFirst(", caption: "dataSetParamValueFirst()", meta: "chart"},
		{name: "dataSetParamValues", value: "dataSetParamValues(", caption: "dataSetParamValues()", meta: "chart"},
		{name: "dataSetParamValuesFirst", value: "dataSetParamValuesFirst(", caption: "dataSetParamValuesFirst()", meta: "chart"},
		{name: "dataSetPropertiesOfSign", value: "dataSetPropertiesOfSign(", caption: "dataSetPropertiesOfSign()", meta: "chart"},
		{name: "dataSetPropertyLabel", value: "dataSetPropertyLabel(", caption: "dataSetPropertyLabel()", meta: "chart"},
		{name: "dataSetPropertyOfSign", value: "dataSetPropertyOfSign(", caption: "dataSetPropertyOfSign()", meta: "chart"},
		{name: "destroy", value: "destroy()", caption: "destroy()", meta: "chart"},
		{name: "disableSetting", value: "disableSetting(", caption: "disableSetting()", meta: "chart"},
		{name: "doRender", value: "doRender()", caption: "doRender()", meta: "chart"},
		{name: "doUpdate", value: "doUpdate(", caption: "doUpdate()", meta: "chart"},
		{name: "echartsInit", value: "echartsInit(", caption: "echartsInit()", meta: "chart"},
		{name: "echartsInstance", value: "echartsInstance(", caption: "echartsInstance()", meta: "chart"},
		{name: "echartsLoadMap", value: "echartsLoadMap(", caption: "echartsLoadMap()", meta: "chart"},
		{name: "echartsMapRegistered", value: "echartsMapRegistered(", caption: "echartsMapRegistered()", meta: "chart"},
		{name: "echartsOptions", value: "echartsOptions(", caption: "echartsOptions()", meta: "chart"},
		{name: "echartsThemeName", value: "echartsThemeName(", caption: "echartsThemeName()", meta: "chart"},
		{name: "element", value: "element()", caption: "element()", meta: "chart"},
		{name: "elementId", value: "elementId", caption: "elementId", meta: "chart"},
		{name: "elementJquery", value: "elementJquery()", caption: "elementJquery()", meta: "chart"},
		{name: "elementWidgetId", value: "elementWidgetId()", caption: "elementWidgetId()", meta: "chart"},
		{name: "eventBindHandlerDelegation", value: "eventBindHandlerDelegation(", caption: "eventBindHandlerDelegation()", meta: "chart"},
		{name: "eventData", value: "eventData(", caption: "eventData()", meta: "chart"},
		{name: "eventHandlers", value: "eventHandlers(", caption: "eventHandlers()", meta: "chart"},
		{name: "eventNewEcharts", value: "eventNewEcharts(", caption: "eventNewEcharts()", meta: "chart"},
		{name: "eventNewHtml", value: "eventNewHtml(", caption: "eventNewHtml()", meta: "chart"},
		{name: "eventOriginalChartDataSetIndex", value: "eventOriginalChartDataSetIndex(", caption: "eventOriginalChartDataSetIndex()", meta: "chart"},
		{name: "eventOriginalData", value: "eventOriginalData(", caption: "eventOriginalData()", meta: "chart"},
		{name: "eventOriginalInfo", value: "eventOriginalInfo(", caption: "eventOriginalInfo()", meta: "chart"},
		{name: "eventOriginalResultDataIndex", value: "eventOriginalResultDataIndex(", caption: "eventOriginalResultDataIndex()", meta: "chart"},
		{name: "eventUnbindHandlerDelegation", value: "eventUnbindHandlerDelegation(", caption: "eventUnbindHandlerDelegation()", meta: "chart"},
		{name: "extValue", value: "extValue(", caption: "extValue()", meta: "chart"},
		{name: "getUpdateResults", value: "getUpdateResults()", caption: "getUpdateResults()", meta: "chart"},
		{name: "handleChartEventLink", value: "handleChartEventLink(", caption: "handleChartEventLink()", meta: "chart"},
		{name: "hasParamDataSet", value: "()", caption: "hasParamDataSet()", meta: "chart"},
		{name: "id", value: "id", caption: "id", meta: "chart"},
		{name: "init", value: "init()", caption: "init()", meta: "chart"},
		{name: "isActive", value: "isActive()", caption: "isActive()", meta: "chart"},
		{name: "isAsyncRender", value: "isAsyncRender()", caption: "isAsyncRender()", meta: "chart"},
		{name: "isAsyncUpdate", value: "isAsyncUpdate()", caption: "isAsyncUpdate()", meta: "chart"},
		{name: "isDataSetParamValueReady", value: "isDataSetParamValueReady()", caption: "isDataSetParamValueReady()", meta: "chart"},
		{name: "isInstance", value: "isInstance(", caption: "isInstance()", meta: "chart"},
		{name: "links", value: "links(", caption: "links()", meta: "chart"},
		{name: "listener", value: "listener(", caption: "listener()", meta: "chart"},
		{name: "map", value: "map(", caption: "map()", meta: "chart"},
		{name: "mapURL", value: "mapURL(", caption: "mapURL()", meta: "chart"},
		{name: "name", value: "name", caption: "name", meta: "chart"},
		{name: "off", value: "off(", caption: "off()", meta: "chart"},
		{name: "on", value: "on(", caption: "on()", meta: "chart"},
		{name: "onClick", value: "onClick(", caption: "onClick()", meta: "chart"},
		{name: "onDblclick", value: "onDblclick(", caption: "onDblclick()", meta: "chart"},
		{name: "onMousedown", value: "onMousedown(", caption: "onMousedown()", meta: "chart"},
		{name: "onMouseout", value: "onMouseout(", caption: "onMouseout()", meta: "chart"},
		{name: "onMouseover", value: "onMouseover(", caption: "onMouseover()", meta: "chart"},
		{name: "onMouseup", value: "onMouseup(", caption: "onMouseup()", meta: "chart"},
		{name: "options", value: "options(", caption: "options()", meta: "chart"},
		{name: "optionsUpdate", value: "optionsUpdate(", caption: "optionsUpdate()", meta: "chart"},
		{name: "plugin", value: "plugin", caption: "plugin", meta: "chart"},
		{name: "refreshData", value: "refreshData()", caption: "refreshData()", meta: "chart"},
		{name: "render", value: "render()", caption: "render()", meta: "chart"},
		{name: "renderContext", value: "renderContext", caption: "renderContext", meta: "chart"},
		{name: "renderContextAttr", value: "renderContextAttr(", caption: "renderContextAttr()", meta: "chart"},
		{name: "renderer", value: "renderer(", caption: "renderer()", meta: "chart"},
		{name: "resetDataSetParamValues", value: "resetDataSetParamValues(", caption: "resetDataSetParamValues()", meta: "chart"},
		{name: "resetDataSetParamValuesFirst", value: "resetDataSetParamValuesFirst()", caption: "resetDataSetParamValuesFirst()", meta: "chart"},
		{name: "resize", value: "resize()", caption: "resize()", meta: "chart"},
		{name: "resultAt", value: "resultAt(", caption: "resultAt()", meta: "chart"},
		{name: "resultCell", value: "resultCell(", caption: "resultCell()", meta: "chart"},
		{name: "resultColumnArrays", value: "resultColumnArrays(", caption: "resultColumnArrays()", meta: "chart"},
		{name: "resultDataFormat", value: "resultDataFormat(", caption: "resultDataFormat()", meta: "chart"},
		{name: "resultDatas", value: "resultDatas(", caption: "resultDatas()", meta: "chart"},
		{name: "resultFirst", value: "resultFirst(", caption: "resultFirst()", meta: "chart"},
		{name: "resultNameValueObjects", value: "resultNameValueObjects(", caption: "resultNameValueObjects()", meta: "chart"},
		{name: "resultOf", value: "resultOf(", caption: "resultOf()", meta: "chart"},
		{name: "resultRowArrays", value: "resultRowArrays(", caption: "resultRowArrays()", meta: "chart"},
		{name: "resultRowCell", value: "resultRowCell(", caption: "resultRowCell()", meta: "chart"},
		{name: "status", value: "status(", caption: "status()", meta: "chart"},
		{name: "statusDestroyed", value: "statusDestroyed(", caption: "statusDestroyed()", meta: "chart"},
		{name: "statusPreRender", value: "statusPreRender(", caption: "statusPreRender()", meta: "chart"},
		{name: "statusPreUpdate", value: "statusPreUpdate(", caption: "statusPreUpdate()", meta: "chart"},
		{name: "statusRendered", value: "statusRendered(", caption: "statusRendered()", meta: "chart"},
		{name: "statusRendering", value: "statusRendering(", caption: "statusRendering()", meta: "chart"},
		{name: "statusUpdated", value: "statusUpdated(", caption: "statusUpdated()", meta: "chart"},
		{name: "statusUpdating", value: "statusUpdating(", caption: "statusUpdating()", meta: "chart"},
		{name: "theme", value: "theme(", caption: "theme()", meta: "chart"},
		{name: "update", value: "update(", caption: "update()", meta: "chart"},
		{name: "updateGroup", value: "updateGroup(", caption: "updateGroup()", meta: "chart"},
		{name: "updateInterval", value: "updateInterval", caption: "updateInterval", meta: "chart"},
		{name: "updateResults", value: "updateResults(", caption: "updateResults()", meta: "chart"},
		{name: "widgetId", value: "widgetId()", caption: "widgetId()", meta: "chart"}
	];
	
	po.templateEditorCompleters =
	[
		//自动补全：dg-*
		{
			identifierRegexps : [/[a-zA-Z_0-9\-]/],
			getCompletions: function(editor, session, pos, prefix, callback)
			{
				var completions = [];
				
				if(prefix && prefix.indexOf("dg") == 0)
					completions = this._getCompletionsForTagAttr(editor, session, pos, prefix, callback);
				
				callback(null, completions);
			},
			_getCompletionsForTagAttr: function(editor, session, pos, prefix, callback)
			{
				var completions = [];
				
				var prevText = po.getTemplatePrevTagText(editor, pos.row, pos.column);
				var tagName = po.resolveHtmlTagName(prevText);
				
				if(tagName)
				{
					tagName = tagName.toLowerCase();
					
					for(var i=0; i<po.templateEditorCompletionsTagAttr.length; i++)
					{
						var comp = po.templateEditorCompletionsTagAttr[i];
						
						if(comp.name.indexOf(prefix) != 0)
							continue;
						
						if(!comp.tagNames || $.inArray(tagName, comp.tagNames) > -1)
							completions.push(comp);
					}
				}
				
				return completions;
			}
		},
		//自动补全：dashboard.*、chart.*
		{
			identifierRegexps : [/[a-zA-Z]/],
			getCompletions: function(editor, session, pos, prefix, callback)
			{
				var completions = [];
				
				if(prefix)
				{
					var prevToken = po.getTemplatePrevToken(editor, pos.row, pos.column);
					
					//以"."加可选空格结尾
					if(prevToken && /\.\S*$/.test(prevToken))
						completions = this._getCompletionsForFunc(editor, session, pos, prefix, callback, prevToken);
				}
				
				callback(null, completions);
			},
			_getCompletionsForFunc: function(editor, session, pos, prefix, callback, prevToken)
			{
				var completions = [];
				
				var meta = "";
				
				//无法确定要补全的是看板还是图表对象，所以这里采用：完全匹配变量名，否则就全部提示
				
				// *dashboard*
				if(/dashboard/i.test(prevToken))
					meta = "dashboard";
				// *chart*
				else if(/chart/i.test(prevToken))
					meta = "chart";
				
				for(var i=0; i<po.templateEditorCompletionsJsFunction.length; i++)
				{
					var comp = po.templateEditorCompletionsJsFunction[i];
					
					if(!meta || (meta && comp.meta == meta))
					{
						if(comp.name.indexOf(prefix) == 0)
							completions.push(comp);
					}
				}
				
				return completions;
			}
		}
	];
	
	po.getDashboardId = function()
	{
		return  po.element("input[name='id']").val();
	};
	
	po.getSelectedResourceItem = function()
	{
		var $resources = po.elementResListLocal(".resource-list-content");
		return $("> .resource-item.ui-state-active", $resources);
	};
	
	po.getSelectedResourceName = function($res)
	{
		if(!$res)
			$res = po.getSelectedResourceItem();
		
		return $res.attr("resource-name");
	};
	
	po.concatSelectedResDirectory = function(resName)
	{
		var currentRes = po.getSelectedResourceName();
		if(currentRes)
		{
			var lastChar = currentRes.charAt(currentRes.length - 1);
			if(lastChar == "/")
				;
			else
				currentRes = "";
		}
		else
			currentRes = "";
		
		return currentRes + resName;
	};
	
	po.addDashboardResourceItem = function($parent, resourceName)
	{
		var $res = $("<div class='resource-item'></div>").attr("resource-name", resourceName).text(resourceName);
		$parent.append($res);
	};
	
	po.addDashboardResourceItemTemplate = function($parent, templateName, prepend)
	{
		var $res = $("<div class='resource-item resource-item-template'></div>").attr("resource-name", templateName).text(templateName);
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
	
	po.isTemplateResourceItem = function($res)
	{
		return $res.hasClass("resource-item-template");
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
	
	po.refreshResourceList = function()
	{
		var id = po.getDashboardId();
		
		if(!id)
			return;
		
		var $resources = po.elementResListLocal(".resource-list-content");
		$resources.empty();
		
		$.get(po.url("listResources?id="+id), function(resources)
		{
			if(!resources)
				return;
			
			var templateCount = 0;
			for(var i=0; i<po.templates.length; i++)
			{
				for(var j=0; j<resources.length; j++)
				{
					if(po.templates[i] == resources[j])
					{
						po.addDashboardResourceItemTemplate($resources, resources[j]);
						templateCount++;
					}
				}
			}
			
			if(templateCount > 0)
				$resources.append($("<div class='resource-item-divider ui-widget ui-widget-content'></div>"));
			
			for(var i=0; i<resources.length; i++)
				po.addDashboardResourceItem($resources, resources[i]);
			
			$resources.selectable("refresh");
		});
	};
	
	po.elementResListLocal(".resource-list-content").selectable
	(
		{
			classes: {"ui-selected": "ui-state-active"},
			filter: ".resource-item"
		}
	)
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
		po.elementResListLocal(".addResNameInput").val("");
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
		
		name = po.concatSelectedResDirectory(name);
		
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
		
		var $parent = po.elementResListLocal(".resource-list-content");
		var $res = $("> .resource-item.ui-state-active", $parent);
		var resName = $res.attr("resource-name");
		
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
			po.refreshResourceList();
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
		
		window.open(po.showUrl(id) + path);
	});
	
	po.elementResListLocal(".asTemplateBtn").click(function()
	{
		if(!po.checkDashboardSaved())
			return;
		
		var $parent = po.elementResListLocal(".resource-list-content");
		var $res = $("> .resource-item.ui-state-active", $parent);
		
		if(po.isTemplateResourceItem($res))
			return;
		
		var resName = $res.attr("resource-name");
		
		if(!resName)
			return;
		
		if(!$.isHtmlFile(resName))
		{
	 		$.tipInfo("<@spring.message code='dashboard.resAsTemplateUnsupport' />");
	 		return;
		}
		
		var $templates = $(".resource-item-template", $parent);
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
		
		var $parent = po.elementResListLocal(".resource-list-content");
		var $res = $("> .resource-item.ui-state-active", $parent);
		
		if(!po.isTemplateResourceItem($res))
			return;
		
		var resName = $res.attr("resource-name");

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
		
		var $parent = po.elementResListLocal(".resource-list-content");
		var $res = $("> .resource-item.ui-state-active", $parent);
		
		if(!po.isTemplateResourceItem($res))
			return;
		
		var resName = $res.attr("resource-name");
		
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
				po.refreshResourceList();
				
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
		
		po.refreshResourceList();
	});
	
	po.elementResListLocal(".deleteResBtn").click(function()
	{
		var id = po.getDashboardId();
		
		if(!id)
		{
			$.tipInfo("<@spring.message code='dashboard.pleaseSaveDashboardFirst' />");
			return;
		}
		
		var $res = po.getSelectedResourceItem();
		var name = po.getSelectedResourceName($res);
		
		if(!name)
			return;
		
		po.confirm("<@spring.message code='dashboard.confirmDeleteSelectedResource' />",
		{
			"confirm" : function()
			{
				$.post(po.url("deleteResource"), {"id": id, "name" : name},
				function(response)
				{
					po.refreshResourceList();
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
			po.elementResListLocal(".uploadResNameInput").val(po.concatSelectedResDirectory(uploadResult.fileName));
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
	});
	
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
		var keyword = po.elementResListGlobal(".search-input").val();
		po.refreshResourceGlobalList(keyword);
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
		
		window.open(po.showUrl(id) + path);
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
	
	po.getSelectedResourceGlobalName = function($res)
	{
		if(!$res)
			$res = po.getSelectedResourceGlobalItem();
		
		return $res.attr("resource-name");
	};
	
	po.getSelectedResourceGlobalItem = function()
	{
		var $resources = po.elementResListGlobal(".resource-list-content");
		return $("> .resource-item.ui-state-active", $resources);
	};
	
	po.elementResListGlobal(".refreshResListBtn").click(function()
	{
		po.refreshResourceGlobalList();
	});

	po.elementResListGlobal(".resource-list-content").selectable
	(
		{
			classes: {"ui-selected": "ui-state-active"},
			filter: ".resource-item"
		}
	)
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
	
	po.refreshResourceGlobalList = function(searchKeyword)
	{
		var $resources = po.elementResListGlobal(".resource-list-content");
		$resources.empty();
		
		var data = { "keyword": searchKeyword };
		
		$.postJson("${contextPath}/dashboardGlobalRes/queryData", data, function(resources)
		{
			if(!resources || resources.length == 0)
			{
				$resources.html("<div class='resource-none ui-state-disabled'><@spring.message code='none' /></div>");
				return;
			}
			
			for(var i=0; i<resources.length; i++)
				po.addDashboardResourceItem($resources, po.dashboardGlobalResUrlPrefix + resources[i].path);
			
			$resources.selectable("refresh");
		});
	};
	
	po.insertChartCode = function(editor, charts)
	{
		if(!charts || !charts.length)
			return;
		
		var cursor = editor.getCursorPosition();
		
		//如果body上没有定义dg-dashboard样式，则图表元素也不必添加dg-chart样式，比如导入的看板
		var setDashboardTheme = true;
		
		var code = "";
		
		if(charts.length == 1)
		{
			var chartId = charts[0].id;
			var chartName = charts[0].name;
			
			var text = po.getTemplatePrevTagText(editor, cursor.row, cursor.column);
			
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
				setDashboardTheme = po.isCodeHasDefaultThemeClass(editor, cursor);
				code = "<div "+(setDashboardTheme ? "class=\"dg-chart\" " : "")+"dg-chart-widget=\""+chartId+"\"><!--"+chartName+"--></div>\n";
			}
		}
		else
		{
			setDashboardTheme = po.isCodeHasDefaultThemeClass(editor, cursor);
			for(var i=0; i<charts.length; i++)
				code += "<div "+(setDashboardTheme ? "class=\"dg-chart\" " : "")+"dg-chart-widget=\""+charts[i].id+"\"><!--"+charts[i].name+"--></div>\n";
		}
		
		editor.moveCursorToPosition(cursor);
		editor.session.insert(cursor, code);
		editor.focus();
	};
	
	po.isCodeHasDefaultThemeClass = function(editor, cursor)
	{
		var row = cursor.row;
		var text = "";
		while(row >= 0)
		{
			text = editor.session.getLine(row);
			
			if(text && /["'\s]dg-dashboard["'\s]/g.test(text))
				return true;
			
			if(/<body/gi.test(text))
				break;
			
			row--;
		}
		
		return false;
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
			var editor = tp.data("resourceEditorInstance");
			
			data.resourceNames.push($(".resourceName", tp).val());
			data.resourceIsTemplates.push($(".resourceIsTemplate", tp).val());
			data.resourceContents.push(editor.getValue());
		});
		
		return data;
	};
	
	po.showAfterSave = false;
	
	po.element("button[name=saveAndShow]").click(function()
	{
		po.showAfterSave = true;
		po.element("input[type='submit']").click();
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
			var data = po.getResourceEditorData();
			var formData = $.formToJson(form);
			data.dashboard = formData;
			
			var templateCount = (data.dashboard.templates == null ? 0 : data.dashboard.templates.length);
			for(var i=0; i<data.resourceIsTemplates.length; i++)
			{
				if(data.resourceIsTemplates[i] == "true")
					templateCount++;
			}
			
			if(templateCount == 0)
			{
				$.tipInfo("<@spring.message code='dashboard.atLeastOneTemplateRequired' />");
				po.showAfterSave = false;
				
				return;
			}
			
			$.ajaxJson($(form).attr("action"),
			{
				data: data,
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
						po.refreshResourceList();
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
	
	if(po.getDashboardId())
		po.element(".resize-editor-button-left").click();
	
	po.newResourceEditorTab(po.element("#${pageId}-initTemplateName").val(), po.element("#${pageId}-initTemplateContent").val(), true);
	po.refreshResourceList();
})
(${pageId});
</script>
</body>
</html>
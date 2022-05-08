<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#--
看板表单代码编辑器功能片段
-->
<#include "dashboard_code_completions.ftl" >
<script type="text/javascript">
(function(po)
{
	po.initDashboardEditors = function()
	{
		var chartListPanel = po.element(".chart-list-panel");
		chartListPanel.draggable({ handle: ".panel-head" });
		
		//初始化可视编辑元素文本内容面板
		var veContentPanel = po.element(".veditor-content-panel");
		veContentPanel.draggable({ handle: ".panel-head" });
		var veContentForm = po.element("form", veContentPanel);
		veContentForm.submit(function()
		{
			try
			{
				veContentPanel.hide();
				
				var tabPane = po.getActiveResEditorTabPane();
				var dashboardEditor = po.visualDashboardEditor(tabPane);
				if(dashboardEditor)
				{
					var text = po.elementOfName("content", veContentPanel).val();
					dashboardEditor.setElementText(text);
				}
			}
			catch(e)
			{
				chartFactory.logException(e);
			}
			
			return false;
		});
		
		//初始化可视编辑样式面板
		var veStylePanel = po.element(".veditor-style-panel");
		veStylePanel.draggable({ handle: ".panel-head" });
		var veStyleForm = po.element("form", veStylePanel);
		veStyleForm.submit(function()
		{
			try
			{
				veStylePanel.hide();
				
				var tabPane = po.getActiveResEditorTabPane();
				var dashboardEditor = po.visualDashboardEditor(tabPane);
				if(dashboardEditor)
				{
					var styleObj = $.formToJson(this);
					
					if(po.isDisplayGrid(styleObj.display))
					{
						styleObj['align-items'] = styleObj['align-items-grid'];
						styleObj['justify-content'] = styleObj['justify-content-grid'];
						styleObj['align-content'] = styleObj['align-content-grid'];
					}
					else if(po.isDisplayFlex(styleObj.display))
					{
						styleObj['align-items'] = styleObj['align-items-flex'];
						styleObj['justify-content'] = styleObj['justify-content-flex'];
						styleObj['align-content'] = styleObj['align-content-flex'];
					}
					
					if(dashboardEditor.isGridItemElement())
						styleObj['align-self'] = styleObj['align-self-grid'];
					else if(dashboardEditor.isFlexItemElement())
						styleObj['align-self'] = styleObj['align-self-flex'];
					
					styleObj['align-items-grid']=null;
					styleObj['justify-content-grid']=null;
					styleObj['align-content-grid']=null;
					styleObj['align-self-grid']=null;
					styleObj['align-items-flex']=null;
					styleObj['justify-content-flex']=null;
					styleObj['align-content-flex']=null;
					styleObj['align-self-flex']=null;
					
					if(po.veOperation == "editStyle")
						dashboardEditor.setElementStyle(styleObj);
					else if(po.veOperation == "editGlobalStyle")
						dashboardEditor.setGlobalStyle(styleObj);
				}
			}
			catch(e)
			{
				chartFactory.logException(e);
			}
			
			return false;
		});
		po.element(".style-tabs", veStyleForm).tabs();
		po.elementOfName("color", veStyleForm).listpalllet(
		{
			indicator: po.element(".color-indicator", veStyleForm),
			container: po.elementOfName("color", veStyleForm).parent(),
			position: "fixed",
			autoCloseContext: po.element()
		});
		po.elementOfName("background-color", veStyleForm).listpalllet(
		{
			indicator: po.element(".bgcolor-indicator", veStyleForm),
			container: po.elementOfName("background-color", veStyleForm).parent(),
			position: "fixed",
			autoCloseContext: po.element()
		});
		po.elementOfName("border-color", veStyleForm).listpalllet(
		{
			indicator: po.element(".border-color-indicator", veStyleForm),
			container: po.elementOfName("border-color", veStyleForm).parent(),
			position: "fixed",
			autoCloseContext: po.element()
		});
		po.elementOfId("${pageId}-syncChartTheme", veStyleForm).checkboxradiogroup({classes:{"ui-checkboxradio-label": "small-button"}});
		
		//初始化可视编辑图表主题面板
		var veChartThemePanel = po.element(".veditor-chartTheme-panel");
		veChartThemePanel.draggable({ handle: ".panel-head" });
		var veChartThemeForm = po.element("form", veChartThemePanel);
		veChartThemeForm.submit(function()
		{
			try
			{
				veChartThemePanel.hide();
				
				var tabPane = po.getActiveResEditorTabPane();
				var dashboardEditor = po.visualDashboardEditor(tabPane);
				if(dashboardEditor)
				{
					var chartThemeObj = $.formToJson(this);
					//未出现的属性将不会更新，所以这里要检查设置
					chartThemeObj.graphColors = (chartThemeObj.graphColors === undefined ? [] : chartThemeObj.graphColors);
					chartThemeObj.graphRangeColors = (chartThemeObj.graphRangeColors === undefined ? [] : chartThemeObj.graphRangeColors);
					
					if(po.veOperation == "editChartTheme")
						dashboardEditor.setElementChartTheme(chartThemeObj);
					else if(po.veOperation == "editGlobalChartTheme")
						dashboardEditor.setGlobalChartTheme(chartThemeObj);
				}
			}
			catch(e)
			{
				chartFactory.logException(e);
			}
			
			return false;
		});
		po.elementOfName("color", veChartThemeForm).listpalllet(
		{
			indicator: po.element(".color-indicator", veChartThemeForm),
			container: po.elementOfName("color", veChartThemeForm).parent(),
			position: "fixed",
			autoCloseContext: po.element()
		});
		po.elementOfName("backgroundColor", veChartThemeForm).listpalllet(
		{
			indicator: po.element(".bgcolor-indicator", veChartThemeForm),
			container: po.elementOfName("backgroundColor", veChartThemeForm).parent(),
			position: "fixed",
			autoCloseContext: po.element()
		});
		po.elementOfName("actualBackgroundColor", veChartThemeForm).listpalllet(
		{
			indicator: po.element(".actbgcolor-indicator", veChartThemeForm),
			container: po.elementOfName("actualBackgroundColor", veChartThemeForm).parent(),
			position: "fixed",
			autoCloseContext: po.element()
		});
		veChartThemeForm.on("click", ".del-color-btn", function(event)
		{
			var $parent = $(this).parent();
			po.element("input[type='text']", $parent).listpalllet("destroy");
			$parent.remove();
			//阻止冒泡，不然会触发$.fn.autoCloseSubPanel函数关闭面板
			event.stopPropagation();
		});
		veChartThemeForm.on("click", ".addGraphColorsBtn", function()
		{
			po.addChartThemeFormGraphColorsItem(po.element(".graphColorsInput", veChartThemeForm));
		});
		veChartThemeForm.on("click", ".addGraphRangeColorsBtn", function()
		{
			po.addChartThemeFormGraphRangeColorsItem(po.element(".graphRangeColorsInput", veChartThemeForm));
		});
		
		//初始化看板尺寸面板
		var veDashboardSizePanel = po.element(".veditor-dashboardSize-panel");
		veDashboardSizePanel.draggable({ handle: ".panel-head" });
		var veDashboardSizeForm = po.element("form", veDashboardSizePanel);
		veDashboardSizeForm.submit(function()
		{
			try
			{
				veDashboardSizePanel.hide();
				
				var setting = $.formToJson(this);
				var topWindowSize = po.evalTopWindowSize();
				var ifmWidth = (setting.width ? parseInt(setting.width) : topWindowSize.width);
				var ifmHeight = (setting.height ? parseInt(setting.height) : topWindowSize.height);
				
				var tabPane = po.getActiveResEditorTabPane();
				var visualEditorIfm = po.element(".tpl-visual-editor-ifm", tabPane);
				
				if(ifmWidth > 0)
					visualEditorIfm.css("width", ifmWidth);
				if(ifmHeight > 0)
					visualEditorIfm.css("height", ifmHeight);
				
				po.setVisualEditorIframeScale(visualEditorIfm, setting.scale);
			}
			catch(e)
			{
				chartFactory.logException(e);
			}
			
			return false;
		});
		po.element(".setting-scale-wrapper", veDashboardSizeForm).checkboxradiogroup({classes:{"ui-checkboxradio-label": "small-button"}});
		
		//初始化图表选项面板
		var veChartOptionsPanel = po.element(".veditor-chartOptions-panel");
		veChartOptionsPanel.draggable({ handle: ".panel-head" });
		var veChartOptionsForm = po.element("form", veChartOptionsPanel);
		var veChartOptionsEditorOptions =
		{
			value: "",
			matchBrackets: true,
			autoCloseBrackets: true,
			mode: {name: "javascript", json: true}
		};
		po.element(".chartOptions-editor-wrapper", veChartOptionsForm).data("chartOptionsCodeEditor",
				po.createCodeEditor(po.elementOfId("${pageId}ChartOptionsEditor", veChartOptionsForm), veChartOptionsEditorOptions));
		veChartOptionsForm.submit(function()
		{
			try
			{
				veChartOptionsPanel.hide();
				
				var tabPane = po.getActiveResEditorTabPane();
				var dashboardEditor = po.visualDashboardEditor(tabPane);
				if(dashboardEditor)
				{
					var coEditor = po.element(".chartOptions-editor-wrapper", this).data("chartOptionsCodeEditor");
					var chartOptionsStr = po.getCodeText(coEditor);
					
					if(po.veOperation == "editChartOptions")
						dashboardEditor.setElementChartOptions(chartOptionsStr);
					else if(po.veOperation == "editGlobalChartOptions")
						dashboardEditor.setGlobalChartOptions(chartOptionsStr);
				}
			}
			catch(e)
			{
				chartFactory.logException(e);
			}
			
			return false;
		});
		
		//初始化插入网格布局面板
		var veGridLayoutPanel = po.element(".veditor-gridLayout-panel");
		veGridLayoutPanel.draggable({ handle: ".panel-head" });
		var veGridLayoutForm = po.element("form", veGridLayoutPanel);
		veGridLayoutForm.submit(function()
		{
			try
			{
				veGridLayoutPanel.hide();
				
				var tabPane = po.getActiveResEditorTabPane();
				var dashboardEditor = po.visualDashboardEditor(tabPane);
				if(dashboardEditor)
				{
					var gridLayoutObj = $.formToJson(this);
					dashboardEditor.insertGridLayout(gridLayoutObj, po.veOperationInsertType);
				}
			}
			catch(e)
			{
				chartFactory.logException(e);
			}
			
			return false;
		});
		po.element(".gridLayoutFillParentCheckbox", veGridLayoutForm).checkboxradiogroup();
		
		//初始化插入图片面板
		var veImagePanel = po.element(".veditor-image-panel");
		veImagePanel.draggable({ handle: ".panel-head" });
		var veImageForm = po.element("form", veImagePanel);
		veImageForm.submit(function()
		{
			try
			{
				veImagePanel.hide();
				
				var tabPane = po.getActiveResEditorTabPane();
				var dashboardEditor = po.visualDashboardEditor(tabPane);
				if(dashboardEditor)
				{
					var imageObj = $.formToJson(this);
					
					if(po.veOperationEditEleAttr)
						dashboardEditor.setImageAttr(imageObj);
					else
						dashboardEditor.insertImage(imageObj, po.veOperationInsertType);
				}
			}
			catch(e)
			{
				chartFactory.logException(e);
			}
			
			return false;
		});
		
		//初始化插入超链接面板
		var veHyperlinkPanel = po.element(".veditor-hyperlink-panel");
		veHyperlinkPanel.draggable({ handle: ".panel-head" });
		var veHyperlinkForm = po.element("form", veHyperlinkPanel);
		veHyperlinkForm.submit(function()
		{
			try
			{
				veHyperlinkPanel.hide();
				
				var tabPane = po.getActiveResEditorTabPane();
				var dashboardEditor = po.visualDashboardEditor(tabPane);
				if(dashboardEditor)
				{
					var hyperlinkObj = $.formToJson(this);
					
					if(po.veOperationEditEleAttr)
						dashboardEditor.setHyperlinkAttr(hyperlinkObj);
					else
						dashboardEditor.insertHyperlink(hyperlinkObj, po.veOperationInsertType);
				}
			}
			catch(e)
			{
				chartFactory.logException(e);
			}
			
			return false;
		});
		
		//初始化插入视频面板
		var veVideoPanel = po.element(".veditor-video-panel");
		veVideoPanel.draggable({ handle: ".panel-head" });
		var veVideoForm = po.element("form", veVideoPanel);
		veVideoForm.submit(function()
		{
			try
			{
				veVideoPanel.hide();
				
				var tabPane = po.getActiveResEditorTabPane();
				var dashboardEditor = po.visualDashboardEditor(tabPane);
				if(dashboardEditor)
				{
					var videoObj = $.formToJson(this);
					
					if(po.veOperationEditEleAttr)
						dashboardEditor.setVideoAttr(videoObj);
					else
						dashboardEditor.insertVideo(videoObj, po.veOperationInsertType);
				}
			}
			catch(e)
			{
				chartFactory.logException(e);
			}
			
			return false;
		});
		
		//初始化插入文本标签面板
		var veLabelPanel = po.element(".veditor-label-panel");
		veLabelPanel.draggable({ handle: ".panel-head" });
		var veLabelForm = po.element("form", veLabelPanel);
		veLabelForm.submit(function()
		{
			try
			{
				veLabelPanel.hide();
				
				var tabPane = po.getActiveResEditorTabPane();
				var dashboardEditor = po.visualDashboardEditor(tabPane);
				if(dashboardEditor)
				{
					var labelObj = $.formToJson(this);
					
					if(po.veOperationEditEleAttr)
						dashboardEditor.setLabelAttr(labelObj);
					else
						dashboardEditor.insertLabel(labelObj, po.veOperationInsertType);
				}
			}
			catch(e)
			{
				chartFactory.logException(e);
			}
			
			return false;
		});
		
		po.element(".veditor-panel  .helper-opt").controlgroupwrapper();
		po.element(".veditor-panel .form-item-value .help-src").click(function()
		{
			var $this = $(this);
			var helpValue = ($this.attr("help-value") || "");
			var helpTarget = po.element(".help-target", $this.closest(".form-item-value"));
			helpTarget.val(helpValue);
			helpTarget.focus();
		});
	};
	
	po.getActiveResEditorTabPane = function()
	{
		return po.tabsGetActivePane(po.resourceEditorTabs());
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
	po.setVisualEditorIframeScale = function($iframe, scale)
	{
		scale = (scale == null || scale <= 0 ? "auto" : scale);
		
		$iframe.data("veIframeScale", scale);
		
		if(scale == "auto")
		{
			var $iframeWrapper = $iframe.parent();
			var ww = $iframeWrapper.width(), wh = $iframeWrapper.height();
			var iw = $iframe.width(), ih = $iframe.height();
			
			//下面的计算只有$iframe在$iframeWrapper中是绝对定位的才准确
			var rightGap = 5, bottomGap = 5;
			var ileft = parseInt($iframe.css("left")), itop = parseInt($iframe.css("top"));
			ww = ww - ileft - rightGap;
			wh = wh - itop - bottomGap;
			
			if(iw <= ww && ih <= wh)
				return;
			
			var scaleX = ww/iw, scaleY = wh/ih;
			scale = Math.min(scaleX, scaleY);
		}
		else
			scale = scale/100;
		
		$iframe.css("transform-origin", "0 0");
		$iframe.css("transform", "scale("+scale+")");
	};
	
	po.initVisualDashboardEditor = function(ifmWrapper, visualEditorIfm)
	{
		visualEditorIfm = $(visualEditorIfm);
		var ifmWindow = po.iframeWindow(visualEditorIfm);
		var dashboardEditor = (ifmWindow && ifmWindow.dashboardFactory ? ifmWindow.dashboardFactory.dashboardEditor : null);
		
		if(dashboardEditor && !dashboardEditor._OVERWRITE_BY_CONTEXT)
		{
			dashboardEditor._OVERWRITE_BY_CONTEXT = true;
			
			dashboardEditor.i18n.insertInsideChartOnChartEleDenied="<@spring.message code='dashboard.opt.tip.insertInsideChartOnChartEleDenied' />";
			dashboardEditor.i18n.selectElementForSetChart="<@spring.message code='dashboard.opt.tip.selectElementForSetChart' />";
			dashboardEditor.i18n.canEditOnlyTextElement="<@spring.message code='dashboard.opt.tip.canOnlyEditTextElement' />";
			dashboardEditor.i18n.selectedElementRequired="<@spring.message code='dashboard.opt.tip.selectedElementRequired' />";
			dashboardEditor.i18n.selectedNotChartElement="<@spring.message code='dashboard.opt.tip.selectedNotChartElement' />";
			dashboardEditor.i18n.noSelectableNextElement="<@spring.message code='dashboard.opt.tip.noSelectableNextElement' />";
			dashboardEditor.i18n.noSelectablePrevElement="<@spring.message code='dashboard.opt.tip.noSelectablePrevElement' />";
			dashboardEditor.i18n.noSelectableChildElement="<@spring.message code='dashboard.opt.tip.noSelectableChildElement' />";
			dashboardEditor.i18n.noSelectableParentElement="<@spring.message code='dashboard.opt.tip.noSelectableParentElement' />";
			dashboardEditor.i18n.imgEleRequired = "<@spring.message code='dashboard.opt.tip.imgEleRequired' />";
			dashboardEditor.i18n.hyperlinkEleRequired = "<@spring.message code='dashboard.opt.tip.hyperlinkEleRequired' />";
			dashboardEditor.i18n.videoEleRequired = "<@spring.message code='dashboard.opt.tip.videoEleRequired' />";
			dashboardEditor.i18n.labelEleRequired = "<@spring.message code='dashboard.opt.tip.labelEleRequired' />";
			dashboardEditor.tipInfo = function(msg)
			{
				$.tipInfo(msg);
			};
			dashboardEditor.clickCallback = function()
			{
				//关闭可能已显示的面板
				po.element().click();
			};
			dashboardEditor.selectElementCallback = function(ele)
			{
				var elePathWrapper = po.element(".tpl-ve-ele-path-wrapper", ifmWrapper);
				var elePathEle = po.element(".tpl-ve-ele-path", elePathWrapper);
				elePathEle.empty();
				var elePath = this.getElementPath(ele);
				
				for(var i=0; i<elePath.length; i++)
				{
					var ep = elePath[i];
					var eleInfo = ep.tagName;
					if(ep.id)
						eleInfo += "#"+ep.id;
					if(ep.className)
						eleInfo += "."+ep.className;
					
					if(i > 0)
						$("<span class='info-separator ui-state-disabled' />").text(">").appendTo(elePathEle);
					
					$("<span class='ele-info' />").text($.truncateIf(eleInfo, "...", ep.tagName.length+27))
						.attr("visualEditId", (ep.visualEditId || "")).attr("title", eleInfo).appendTo(elePathEle);
				}
				
				var elePathWrapperWidth = elePathWrapper.width();
				var elePathEleWidth = elePathEle.outerWidth(true);
				elePathEle.css("margin-left", (elePathEleWidth > elePathWrapperWidth ? (elePathWrapperWidth - elePathEleWidth) : 0)+"px");
			};
			dashboardEditor.deselectElementCallback = function()
			{
				po.element(".tpl-ve-ele-path", ifmWrapper).empty();
				visualEditorIfm.data("selectedElementVeId", "");
			};
			dashboardEditor.beforeunloadCallback = function()
			{
				po.element(".tpl-ve-ele-path", ifmWrapper).empty();
				//保存编辑HTML、变更状态，用于刷新操作后恢复页面状态
				visualEditorIfm.data("veEditedHtml", this.editedHtml());
				visualEditorIfm.data("veEnableElementBoundary", this.enableElementBoundary());
				visualEditorIfm.data("veChangeFlag", this.changeFlag());
			};
			
			dashboardEditor.defaultInsertChartEleStyle = po.defaultInsertChartEleStyle;
		}
		
		if(dashboardEditor)
		{
			dashboardEditor.enableElementBoundary(visualEditorIfm.data("veEnableElementBoundary"));
			dashboardEditor.changeFlag(visualEditorIfm.data("veChangeFlag"));
			//XXX 这里无法恢复选中状态，因为每次重新加载后可视编辑ID会重新生成
		}
	};
	
	po.visualDashboardEditor = function(tabPane)
	{
		var visualEditorIfm = po.element(".tpl-visual-editor-ifm", tabPane);
		var ifmWindow = po.iframeWindow(visualEditorIfm);
		var dashboardEditor = (ifmWindow && ifmWindow.dashboardFactory ? ifmWindow.dashboardFactory.dashboardEditor : null);
		
		return dashboardEditor;
	};
	
	po.getResourceEditorData = function()
	{
		var data = {};
		data.resourceNames=[];
		data.resourceContents=[];
		data.resourceIsTemplates=[];
		
		po.element(".resource-editor-tab-pane").each(function()
		{
			var d = po.getSingleResourceEditorData(this);
			
			data.resourceNames.push(d.resourceName);
			data.resourceIsTemplates.push(d.isTemplate);
			data.resourceContents.push(d.resourceContent);
		});
		
		return data;
	};
	
	po.getSingleResourceEditorData = function(tabPane, returnResourceContent)
	{
		tabPane = $(tabPane);
		returnResourceContent = (returnResourceContent == null ? true : returnResourceContent);
		
		var resourceName = po.element(".resourceName", tabPane).val();
		var isTemplate = (po.element(".resourceIsTemplate", tabPane).val() == "true");
		
		var data =
		{
			resourceName: resourceName,
			isTemplate: isTemplate
		};
		
		if(returnResourceContent)
		{
			var resourceContent = "";
			var codeEditorDiv = po.element(".code-editor", tabPane);
			var codeEditor = codeEditorDiv.data("resourceEditorInstance");
			
			if(isTemplate)
			{
				var veIfmWrapper = po.element(".tpl-visual-editor-wrapper", tabPane);
				if(veIfmWrapper.hasClass("show-editor"))
				{
					var dashboardEditor = po.visualDashboardEditor(tabPane);
					resourceContent = (dashboardEditor ? dashboardEditor.editedHtml() : "");
				}
				
				if(!resourceContent)
					resourceContent = po.getCodeText(codeEditor);
			}
			else
			{
				resourceContent = po.getCodeText(codeEditor);
			}
			
			data.resourceContent = resourceContent;
		}
		
		return data;
	};
	
	po.saveResourceEditorContent = function(tabPane)
	{
		if(po.checkDashboardUnSaved())
			return;
		
		var d = po.getSingleResourceEditorData(tabPane);
		
		$.post(
				po.url("saveResourceContent"),
				{
					"id": po.getDashboardId(),
					"resourceName": d.resourceName,
					"resourceContent": d.resourceContent,
					"isTemplate": d.isTemplate
				},
				function(response)
				{
					if(response.data.templatesChanged || !response.data.resourceExists)
					{
						po.templates = response.data.templates;
						po.refreshResourceListLocal();
					}
				});
	};
	
	po.newResourceEditorTab = function(name, content, isTemplate)
	{
		tabTemplate = "<li class='resource-editor-tab' style='vertical-align:middle;'><a href='"+'#'+"{href}'>"+'#'+"{label}</a>"
			+"<div class='tab-operation'>"
			+"<span class='ui-icon ui-icon-close' title='<@spring.message code='close' />'>close</span>"
			+"<div class='tabs-more-operation-button' title='<@spring.message code='moreOperation' />'><span class='ui-icon ui-icon-caret-1-e'></span></div>"
			+"</div>"
			+"</li>";
		
		var label = name;
		var labelMaxLen = 5 + 3 + 10;
		if(label.length > labelMaxLen)
			label = name.substr(0, 5) +"..." + name.substr(label.length - 10);
		
		var tabId = $.uid("resourceEditorTabPane");
    	var tab = $(tabTemplate.replace( /#\{href\}/g, "#" + tabId).replace(/#\{label\}/g, $.escapeHtml(label)))
    		.attr("id", $.uid("resourceEditorTab")).attr("resourceName", name).attr("title", name)
    		.appendTo(po.tabsGetNav(po.resourceEditorTabs()));
    	
    	var panePrevEle = $(".resource-editor-tab-pane", po.resourceEditorTabs()).last();
    	if(panePrevEle.length == 0)
    		panePrevEle = $(".resource-editor-tab-nav", po.resourceEditorTabs());
    	var tabPane = $("<div id='"+tabId+"' class='resource-editor-tab-pane' />").insertAfter(panePrevEle);
    	var resNameWrapper = $("<div class='resource-name-wrapper' />").appendTo(tabPane);
    	$("<label class='name-label'></label>").html("<@spring.message code='name' />").appendTo(resNameWrapper);
    	$("<input type='text' class='resourceName name-input ui-widget ui-widget-content ui-corner-all' readonly='readonly' />").val(name).appendTo(resNameWrapper);
    	$("<input type='hidden' class='resourceIsTemplate' />").val(isTemplate).appendTo(resNameWrapper);
    	
		var editorOptWrapper = $("<div class='editor-operation-wrapper' />").appendTo(tabPane);
		var editorLeftOptWrapper = $("<div class='operation-left' />").appendTo(editorOptWrapper);
    	var editorWrapper = $("<div class='editor-wrapper ui-widget ui-widget-content ui-corner-all' />").appendTo(tabPane);
		var editorDiv = $("<div class='resource-editor code-editor' />").attr("id", $.uid("resourceEditor")).appendTo(editorWrapper);
		
		var codeEditor;
		
		var codeEditorOptions =
		{
			value: content,
			matchBrackets: true,
			matchTags: true,
			autoCloseTags: true,
			autoCloseBrackets: true,
			readOnly: po.readonly,
			foldGutter: true,
			gutters: ["CodeMirror-linenumbers", "CodeMirror-foldgutter"],
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
		
		if(isTemplate)
		{
			var visualEditorDiv = $("<div class='tpl-visual-editor-wrapper hide-editor' />").appendTo(editorWrapper);
			
			var elePathWrapper = $("<div class='tpl-ve-ele-path-wrapper'></div>")
									.appendTo(visualEditorDiv);
			$("<div class='tpl-ve-ele-path' />").on("click", ".ele-info", function()
			{
				var visualEditId = $(this).attr("visualEditId");
				var dashboardEditor = po.visualDashboardEditor(tabPane);
				
				if(dashboardEditor)
					dashboardEditor.selectElement(visualEditId);
			})
			.appendTo(elePathWrapper);
			
			var visualEditorIfmWrapper = $("<div class='tpl-visual-editor-ifm-wrapper' />").appendTo(visualEditorDiv);
			
			var visualEditorId = $.uid("visualEditor");
			var visualEditorIfm = $("<iframe class='tpl-visual-editor-ifm ui-widget-shadow' />")
				.attr("name", visualEditorId).attr("id", visualEditorId)
				.on("load", function()
				{
					po.initVisualDashboardEditor(visualEditorDiv, this);
				})
				.appendTo(visualEditorIfmWrapper);
			
			var topWindowSize = po.evalTopWindowSize();
			visualEditorIfm.css("width", topWindowSize.width);
			visualEditorIfm.css("height", topWindowSize.height);
			
			po.setVisualEditorIframeScale(visualEditorIfm);
			
			var editorSwitchGroup = $("<div class='switch-resource-editor-group' />").appendTo(editorLeftOptWrapper);
			$("<button type='button' class='switchToCodeEditorBtn'></button>").text("<@spring.message code='dashboard.switchToCodeEditor' />")
			.appendTo(editorSwitchGroup).button().click(function()
			{
				po.switchToCodeEditor(tabPane);
			});
			$("<button type='button' class='switchToVisualEditorBtn'></button>").text("<@spring.message code='dashboard.switchToVisualEditor' />")
			.appendTo(editorSwitchGroup).button().click(function()
			{
				po.switchToVisualEditor(tabPane);
			});
			editorSwitchGroup.controlgroup();
			
			//默认打开源码模式，因为如果默认为可视模式，如果页面中有导致死循环的代码，将会导致永远无法再次打开看板编辑页面
			po.switchToCodeEditor(tabPane);
		}
		else
		{
			po.initCodeEditorOperationIfNon(tabPane);
			codeEditor.focus();
		}
		
   	    $(".tab-operation .ui-icon-close", tab).click(function()
   	    {
   	    	var tab = $(this).parent().parent();
   	    	po.tabsCloseTab(po.resourceEditorTabs(), tab);
   	    });
   	    
   	    $(".tab-operation .tabs-more-operation-button", tab).click(function()
   	    {
   	    	var tab = $(this).parent().parent();
   	    	po.tabsShowMoreOptMenu(po.resourceEditorTabs(), tab, $(this));
   	    });
		
	    po.resourceEditorTabs().tabs("refresh");
    	po.resourceEditorTabs().tabs( "option", "active",  tab.index());
    	po.tabsRefreshNavForHidden(po.resourceEditorTabs());
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
			var myVarTokenInfo = po.findPrevTokenInfo(codeEditor, doc, cursor, token,
					function(token){ return (token.type == "variable" || token.type == "variable-2"); });
			var myVarToken = (myVarTokenInfo ? myVarTokenInfo.token : null);
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
	
	//切换至源码编辑模式
	po.switchToCodeEditor = function(tabPane)
	{
		po.initCodeEditorOperationIfNon(tabPane);
		
		po.element(".switchToCodeEditorBtn", tabPane).addClass("ui-state-active");
		po.element(".switchToVisualEditorBtn", tabPane).removeClass("ui-state-active");
		po.element(".code-editor-operation", tabPane).show();
		po.element(".visual-editor-operation", tabPane).hide();
		
		var codeEditorDiv = po.element(".code-editor", tabPane);
		var codeEditor = codeEditorDiv.data("resourceEditorInstance");
		var veIfmWrapper = po.element(".tpl-visual-editor-wrapper", tabPane);
		var changeFlag = codeEditorDiv.data("changeFlag");
		
		//初次由源码模式切换至可视编辑模式后，changeFlag会是1，
		//但此时是不需要同步的，所以这里手动设置为1
		if(changeFlag == null)
			changeFlag = 1;
		
		var dashboardEditor = po.visualDashboardEditor(tabPane);
		
		//有修改
		if(dashboardEditor && dashboardEditor.isChanged(changeFlag))
		{
			po.setCodeText(codeEditor, dashboardEditor.editedHtml());

			veIfmWrapper.data("changeFlag", codeEditor.changeGeneration());
			codeEditorDiv.data("changeFlag", dashboardEditor.changeFlag());
		}
		
		veIfmWrapper.removeClass("show-editor").addClass("hide-editor");
		codeEditorDiv.removeClass("hide-editor").addClass("show-editor");
		
    	codeEditor.focus();
	};
	
	//切换至可视编辑模式
	po.switchToVisualEditor = function(tabPane)
	{
		po.initVisualEditorOperationIfNon(tabPane);
		
		po.element(".switchToCodeEditorBtn", tabPane).removeClass("ui-state-active");
		po.element(".switchToVisualEditorBtn", tabPane).addClass("ui-state-active");
		po.element(".code-editor-operation", tabPane).hide();
		po.element(".visual-editor-operation", tabPane).show();
		
		var codeEditorDiv = po.element(".code-editor", tabPane);
		var codeEditor = codeEditorDiv.data("resourceEditorInstance");
		var veIfmWrapper = po.element(".tpl-visual-editor-wrapper", tabPane);
		var changeFlag = veIfmWrapper.data("changeFlag");
		
		//没有修改
		if(changeFlag != null && codeEditor.isClean(changeFlag))
		{
			codeEditorDiv.removeClass("show-editor").addClass("hide-editor");
			veIfmWrapper.removeClass("hide-editor").addClass("show-editor");
		}
		else
		{
			var veIfm = po.element(".tpl-visual-editor-ifm", veIfmWrapper);
			
			codeEditorDiv.removeClass("show-editor").addClass("hide-editor");
			//清空iframe后再显示，防止闪屏
			po.iframeDocument(veIfm).write("");
			veIfmWrapper.removeClass("hide-editor").addClass("show-editor");
			
			veIfmWrapper.data("changeFlag", codeEditor.changeGeneration());
			codeEditorDiv.data("changeFlag", null);
			
			var templateName = po.element(".resource-name-wrapper input.resourceName", tabPane).val();
			po.loadVisualEditorIframe(veIfm, templateName, (po.readonly ? "" : po.getCodeText(codeEditor)));
		}
	};
	
	po.loadVisualEditorIframe = function(visualEditorIfm, templateName, templateContent)
	{
		var veIfmWrapper = visualEditorIfm.closest(".tpl-visual-editor-wrapper");
		po.element(".tpl-ve-ele-path", veIfmWrapper).empty();
		
		var dashboardId = po.getDashboardId();
		var form = po.elementOfId("${pageId}-tplEditVisualForm");
		form.attr("action", po.showUrl(dashboardId, templateName));
		form.attr("target", visualEditorIfm.attr("name"));
		po.elementOfName("DG_EDIT_TEMPLATE", form).val(po.readonly ? "false" : "true");
		po.elementOfName("DG_TEMPLATE_CONTENT", form).val(templateContent);
		form.submit();
	};
	
	po.initCodeEditorOperationIfNon = function(tabPane)
	{
		var editorOptWrapper = po.element(".editor-operation-wrapper", tabPane);
		var editorRightOptWrapper = po.element(".code-editor-operation", editorOptWrapper);
		
		if(editorRightOptWrapper.length > 0)
			return false;
		
		editorRightOptWrapper = $("<div class='code-editor-operation operation-right' />").appendTo(editorOptWrapper)
		
		if(!po.readonly)
		{
			if(po.element(".resourceIsTemplate", tabPane).val() == "true")
			{
				var insertGroup = $("<div class='insert-group' auto-close-prevent='chart-list-panel' />").appendTo(editorRightOptWrapper);
				var insertChartBtn = $("<button type='button' class='insert-chart-button' />")
					.text("<@spring.message code='dashboard.insertChart' />").appendTo(insertGroup).button()
					.click(function()
					{
						po.toggleInsertChartListPannel(editorOptWrapper);
					});
			}
			
			$("<button type='button' />").text("<@spring.message code='save' />").appendTo(editorRightOptWrapper).button()
			.click(function()
			{
				po.saveResourceEditorContent(tabPane);
			});
		}
		
		var searchGroup = $("<div class='search-group ui-widget ui-widget-content ui-corner-all' />").appendTo(editorRightOptWrapper);
		var searchInput = $("<input type='text' class='search-input ui-widget ui-widget-content ui-corner-all' />").appendTo(searchGroup)
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
					
					var codeEditor = po.element(".code-editor", tabPane).data("resourceEditorInstance");
					
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
	};
	
	po.defaultInsertChartEleStyle = "display:inline-block;width:300px;height:300px;";
	
	po.insertCodeEditorChart = function(tabPane, chartWidgets)
	{
		if(!chartWidgets || !chartWidgets.length)
			return;
		
		var codeEditor = po.element(".code-editor", tabPane).data("resourceEditorInstance");
		
		var doc = codeEditor.getDoc();
		var cursor = doc.getCursor();
		
		var dftSize = po.defaultInsertChartSize;
		
		var code = "";
		
		if(chartWidgets.length == 1)
		{
			var chartId = chartWidgets[0].id;
			var chartName = chartWidgets[0].name;
			
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
				code = "<div style=\""+po.defaultInsertChartEleStyle+"\" dg-chart-widget=\""+chartId+"\"><!--"+chartName+"--></div>\n";
			}
		}
		else
		{
			for(var i=0; i<chartWidgets.length; i++)
				code += "<div style=\""+po.defaultInsertChartEleStyle+"\" dg-chart-widget=\""+chartWidgets[i].id+"\"><!--"+chartWidgets[i].name+"--></div>\n";
		}
		
		po.insertCodeText(codeEditor, cursor, code);
		codeEditor.focus();
	};
	
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
	
	po.initVisualEditorOperationIfNon = function(tabPane)
	{
		if(po.readonly)
			return false;
		
		var editorOptWrapper = po.element(".editor-operation-wrapper", tabPane);
		var editorRightOptWrapper = po.element(".visual-editor-operation", editorOptWrapper);
		
		if(editorRightOptWrapper.length > 0)
			return false;
		
		editorRightOptWrapper = $("<div class='visual-editor-operation operation-right' />").appendTo(editorOptWrapper);
		
		$("<button type='button' class='execute-quick-opt' />")
		.text("<@spring.message code='dashboard.opt.quickOpt' />")
		.attr("title", "<@spring.message code='dashboard.opt.quickOpt' />")
		.appendTo(editorRightOptWrapper).button()
		.click(function(event)
		{
			if(!po.veQuickOptId)
				return;
			
			event.stopPropagation();
			
			po.element(".quick-opt[quick-opt-id='"+po.veQuickOptId+"']", editorRightOptWrapper).click();
		})
		.button("disable")
		.tooltip({ "classes": {"ui-tooltip": "ui-corner-all ui-widget-shadow"}, position: { my: "center bottom-5", at: "center top" } });
		
		var selectGroup = $("<div class='select-group opt-group' />").appendTo(editorRightOptWrapper)
			.hover(
				function()
				{
					po.element(".select-menu", this).show()
						.position({ my : "right top", at : "right bottom", of : this});
				},
				function()
				{
					po.element(".select-menu", this).hide();
				});
		$("<button type='button' />").text("<@spring.message code='select' />").appendTo(selectGroup).button();
		
		var selectMenu = $("<ul class='select-menu operation-menu ui-widget ui-widget-content ui-corner-all ui-front ui-widget-shadow' />");
		$("<li veOperation='selectNext' />").html("<div><@spring.message code='dashboard.opt.select.next' /></div>").appendTo(selectMenu);
		$("<li veOperation='selectPrev' />").html("<div><@spring.message code='dashboard.opt.select.prev' /></div>").appendTo(selectMenu);
		$("<li veOperation='selectFirstChild' />").html("<div><@spring.message code='dashboard.opt.select.firstChild' /></div>").appendTo(selectMenu);
		$("<li veOperation='selectParent' />").html("<div><@spring.message code='dashboard.opt.select.parent' /></div>").appendTo(selectMenu);
		$("<li class='ui-menu-divider' />").appendTo(selectMenu);
		$("<li veOperation='selectDeselect' />").html("<div><@spring.message code='dashboard.opt.select.deselect' /></div>").appendTo(selectMenu);
		selectMenu.appendTo(selectGroup).menu(
		{
			select: function(event, ui)
			{
				var item = ui.item;
				var veOperation = item.attr("veOperation");
				po.veOperation = veOperation;
				
				var dashboardEditor = po.visualDashboardEditor(tabPane);
				
				if(!dashboardEditor)
					return;
				
				if(veOperation == "selectNext")
				{
					dashboardEditor.selectNextElement();
				}
				else if(veOperation == "selectPrev")
				{
					dashboardEditor.selectPrevElement();
				}
				else if(veOperation == "selectFirstChild")
				{
					dashboardEditor.selectFirstChildElement();
				}
				else if(veOperation == "selectParent")
				{
					dashboardEditor.selectParentElement();
				}
				else if(veOperation == "selectDeselect")
				{
					dashboardEditor.deselectElement();
				}
			}
		});
		
		var insertGroup = $("<div class='insert-group opt-group' auto-close-prevent='chart-list-panel' />").appendTo(editorRightOptWrapper)
			.hover(
				function()
				{
					po.element(".insert-menu", this).show()
						.position({ my : "right top", at : "right bottom", of : this});
				},
				function()
				{
					po.element(".insert-menu", this).hide();
				});
		$("<button type='button' />").text("<@spring.message code='insert' />").appendTo(insertGroup).button();
		
		var insertMenu = $("<ul class='insert-menu operation-menu ui-widget ui-widget-content ui-corner-all ui-front ui-widget-shadow' />");
		var insertItemAfter = $("<li />").html("<div><@spring.message code='dashboard.opt.insert.after' /></div>").appendTo(insertMenu);
		po.buildVisualEditorInsertMenuItems(insertItemAfter, "after");
		var insertItemBefore = $("<li />").html("<div><@spring.message code='dashboard.opt.insert.before' /></div>").appendTo(insertMenu);
		po.buildVisualEditorInsertMenuItems(insertItemBefore, "before");
		var insertItemAppend = $("<li />").html("<div><@spring.message code='dashboard.opt.insert.append' /></div>").appendTo(insertMenu);
		po.buildVisualEditorInsertMenuItems(insertItemAppend, "append");
		var insertItemPrepend = $("<li />").html("<div><@spring.message code='dashboard.opt.insert.prepend' /></div>").appendTo(insertMenu);
		po.buildVisualEditorInsertMenuItems(insertItemPrepend, "prepend");
		$("<li class='ui-menu-divider' />").appendTo(insertMenu);
		$("<li veOperation='bindChart' class='quick-opt' />").html("<div><@spring.message code='dashboard.opt.insert.bindOrReplaceChart' /></div>").appendTo(insertMenu);
		insertMenu.appendTo(insertGroup).menu(
		{
			select: function(event, ui)
			{
				var item = ui.item;
				var veOperation = item.attr("veOperation");
				po.veOperation = veOperation;
				var insertType = item.attr("insertType");
				po.veOperationInsertType = insertType;
				po.veOperationEditEleAttr = false;
				
				var dashboardEditor = po.visualDashboardEditor(tabPane);
				
				if(!dashboardEditor)
					return;
				
				if(veOperation == "insertGridLayout")
				{
					if(!dashboardEditor.checkInsertGridLayout(insertType))
						return;
					
					var canFillParent = dashboardEditor.canInsertFillParentGridLayout(insertType);
					
					var panel = po.element(".veditor-gridLayout-panel");
					if(canFillParent)
						po.element(".form-item-gridLayoutFillParent", panel).show();
					else
						po.element(".form-item-gridLayoutFillParent", panel).hide();
					$.jsonToForm(po.element("form", panel), { fillParent: canFillParent });
					panel.show().position({my: "right top", at: "right bottom", of : editorOptWrapper});
					po.resizeVisualEditorPanel(tabPane, panel);
				}
				else if(veOperation == "insertDiv")
				{
					if(!dashboardEditor.checkInsertDiv(insertType))
						return;
					
					dashboardEditor.insertDiv(insertType);
				}
				else if(veOperation == "insertImage")
				{
					if(!dashboardEditor.checkInsertImage(insertType))
						return;
					
					var panel = po.element(".veditor-image-panel");
					$.jsonToForm(po.element("form", panel), {});
					panel.show().position({my: "right top", at: "right bottom", of : editorOptWrapper});
					po.resizeVisualEditorPanel(tabPane, panel);
				}
				else if(veOperation == "insertHyperlink")
				{
					if(!dashboardEditor.checkInsertHyperlink(insertType))
						return;
					
					var panel = po.element(".veditor-hyperlink-panel");
					$.jsonToForm(po.element("form", panel), {});
					panel.show().position({my: "right top", at: "right bottom", of : editorOptWrapper});
					po.resizeVisualEditorPanel(tabPane, panel);
				}
				else if(veOperation == "insertVideo")
				{
					if(!dashboardEditor.checkInsertVideo(insertType))
						return;
					
					var panel = po.element(".veditor-video-panel");
					$.jsonToForm(po.element("form", panel), {});
					panel.show().position({my: "right top", at: "right bottom", of : editorOptWrapper});
					po.resizeVisualEditorPanel(tabPane, panel);
				}
				else if(veOperation == "insertLabel")
				{
					if(!dashboardEditor.checkInsertLabel(insertType))
						return;
					
					var panel = po.element(".veditor-label-panel");
					$.jsonToForm(po.element("form", panel), {});
					panel.show().position({my: "right top", at: "right bottom", of : editorOptWrapper});
					po.resizeVisualEditorPanel(tabPane, panel);
				}
				else if(veOperation == "insertChart")
				{
					if(!dashboardEditor.checkInsertChart(insertType))
						return;
					
					po.toggleInsertChartListPannel(editorOptWrapper);
				}
				else if(veOperation == "bindChart")
				{
					if(!dashboardEditor.checkBindChart())
						return;
					
					po.toggleInsertChartListPannel(editorOptWrapper);
				}
			}
		});
		
		var editGroup = $("<div class='edit-group opt-group' />").appendTo(editorRightOptWrapper)
			.hover(
				function()
				{
					po.element(".edit-menu", this).show().position({ my : "right top", at : "right bottom", of : this});
				},
				function()
				{
					po.element(".edit-menu", this).hide();
				});
		$("<button type='button' />").text("<@spring.message code='edit' />").appendTo(editGroup).button();
		
		var editMenu = $("<ul class='edit-menu operation-menu ui-widget ui-widget-content ui-corner-all ui-front ui-widget-shadow' />");
		$("<li veOperation='editGlobalStyle' class='quick-opt' auto-close-prevent='veditor-style-panel' />").html("<div><@spring.message code='dashboard.opt.edit.globalStyle' /></div>").appendTo(editMenu);
		$("<li veOperation='editGlobalChartTheme' class='quick-opt' auto-close-prevent='veditor-chartTheme-panel' />").html("<div><@spring.message code='dashboard.opt.edit.globalChartTheme' /></div>").appendTo(editMenu);
		$("<li veOperation='editGlobalChartOptions' class='quick-opt' auto-close-prevent='veditor-chartOptions-panel' />").html("<div><@spring.message code='dashboard.opt.edit.globalChartOptions' /></div>").appendTo(editMenu);
		$("<li class='ui-menu-divider' />").appendTo(editMenu);
		$("<li veOperation='editStyle' class='quick-opt' auto-close-prevent='veditor-style-panel' />").html("<div><@spring.message code='dashboard.opt.edit.style' /></div>").appendTo(editMenu);
		$("<li veOperation='editChartTheme' class='quick-opt' auto-close-prevent='veditor-chartTheme-panel' />").html("<div><@spring.message code='dashboard.opt.edit.chartTheme' /></div>").appendTo(editMenu);
		$("<li veOperation='editChartOptions' class='quick-opt' auto-close-prevent='veditor-chartOptions-panel' />").html("<div><@spring.message code='dashboard.opt.edit.chartOptions' /></div>").appendTo(editMenu);
		$("<li veOperation='editEleAttr' class='quick-opt' auto-close-prevent='veditor-panel' />").html("<div><@spring.message code='dashboard.opt.edit.eleAttr' /></div>").appendTo(editMenu);
		$("<li veOperation='editContent' class='quick-opt' auto-close-prevent='veditor-content-panel' />").html("<div><@spring.message code='dashboard.opt.edit.content' /></div>").appendTo(editMenu);
		editMenu.appendTo(editGroup).menu(
		{
			select: function(event, ui)
			{
				var item = ui.item;
				var veOperation = item.attr("veOperation");
				po.veOperation = veOperation;
				
				var dashboardEditor = po.visualDashboardEditor(tabPane);
				if(dashboardEditor)
				{
					if(veOperation == "editGlobalStyle")
					{
						var panel = po.element(".veditor-style-panel");
						po.element(".editStyleTitle", panel).hide();
						po.element(".editGlobalStyleTitle", panel).show();
						panel.show().position({my: "right top", at: "right bottom", of : editorOptWrapper});
						po.resizeVisualEditorPanel(tabPane, panel);
						po.resizeVisualEditorStylePanel(tabPane, panel);
						po.setVeditorStyleFormValue(po.element("form", panel), dashboardEditor.getGlobalStyle());
					}
					else if(veOperation == "editGlobalChartTheme")
					{
						var panel = po.element(".veditor-chartTheme-panel");
						po.element(".editChartThemeTitle", panel).hide();
						po.element(".editGlobalChartThemeTitle", panel).show();
						panel.show().position({my: "right top", at: "right bottom", of : editorOptWrapper});
						po.resizeVisualEditorPanel(tabPane, panel);
						po.setVeditorChartThemeFormValue(po.element("form", panel), dashboardEditor.getGlobalChartTheme());
					}
					else if(veOperation == "editGlobalChartOptions")
					{
						var panel = po.element(".veditor-chartOptions-panel");
						po.element(".chartOptionsTitle", panel).hide();
						po.element(".globalChartOptionsTitle", panel).show();
						panel.show().position({my: "right top", at: "right bottom", of : editorOptWrapper});
						po.resizeVisualEditorPanel(tabPane, panel);
						po.resizeVisualEditorChartOptionsPanel(tabPane, panel);
						po.setVeditorChartOptionsFormValue(po.element("form", panel), dashboardEditor.getGlobalChartOptions());
					}
					else if(veOperation == "editStyle")
					{
						if(!dashboardEditor.checkSetElementStyle())
							return;
						
						var panel = po.element(".veditor-style-panel");

						po.element(".editStyleTitle", panel).show();
						po.element(".editGlobalStyleTitle", panel).hide();
						var elementStyleObj = dashboardEditor.getElementStyle();
						elementStyleObj.isGridItemElement = dashboardEditor.isGridItemElement();
						elementStyleObj.isFlexItemElement = dashboardEditor.isFlexItemElement();
						panel.show().position({my: "right top", at: "right bottom", of : editorOptWrapper});
						po.resizeVisualEditorPanel(tabPane, panel);
						po.resizeVisualEditorStylePanel(tabPane, panel);
						po.setVeditorStyleFormValue(po.element("form", panel), elementStyleObj);
					}
					else if(veOperation == "editChartTheme")
					{
						if(!dashboardEditor.checkSetElementChartTheme())
							return;
						
						var panel = po.element(".veditor-chartTheme-panel");
						po.element(".editChartThemeTitle", panel).show();
						po.element(".editGlobalChartThemeTitle", panel).hide();
						panel.show().position({my: "right top", at: "right bottom", of : editorOptWrapper});
						po.resizeVisualEditorPanel(tabPane, panel);
						po.setVeditorChartThemeFormValue(po.element("form", panel), dashboardEditor.getElementChartTheme());
					}
					else if(veOperation == "editChartOptions")
					{
						if(!dashboardEditor.checkSetElementChartOptions())
							return;
						
						var panel = po.element(".veditor-chartOptions-panel");
						po.element(".chartOptionsTitle", panel).show();
						po.element(".globalChartOptionsTitle", panel).hide();
						panel.show().position({my: "right top", at: "right bottom", of : editorOptWrapper});
						po.resizeVisualEditorPanel(tabPane, panel);
						po.resizeVisualEditorChartOptionsPanel(tabPane, panel);
						po.setVeditorChartOptionsFormValue(po.element("form", panel), dashboardEditor.getElementChartOptions());
					}
					else if(veOperation == "editContent")
					{
						if(!dashboardEditor.checkSetElementText())
							return;
						
						var panel = po.element(".veditor-content-panel");
						panel.show().position({my: "right top", at: "right bottom", of : editorOptWrapper});
						po.resizeVisualEditorPanel(tabPane, panel);
						po.elementOfName("content", panel).val(dashboardEditor.getElementText()).focus();
					}
					else if(veOperation == "editEleAttr")
					{
						po.element(".veditor-panel").hide();
						
						var panel = null;
						
						if(dashboardEditor.isImage())
						{
							panel = po.element(".veditor-image-panel");
							$.jsonToForm(po.element("form", panel), dashboardEditor.getImageAttr());
						}
						else if(dashboardEditor.isHyperlink())
						{
							panel = po.element(".veditor-hyperlink-panel");
							$.jsonToForm(po.element("form", panel), dashboardEditor.getHyperlinkAttr());
						}
						else if(dashboardEditor.isVideo())
						{
							panel = po.element(".veditor-video-panel");
							$.jsonToForm(po.element("form", panel), dashboardEditor.getVideoAttr());
						}
						else if(dashboardEditor.isLabel())
						{
							panel = po.element(".veditor-label-panel");
							$.jsonToForm(po.element("form", panel), dashboardEditor.getLabelAttr());
						}
						else
							$.tipInfo("<@spring.message code='dashboard.opt.edit.eleAttr.eleRequired' />");
						
						if(panel)
						{
							po.veOperationEditEleAttr = true;
							panel.show().position({my: "right top", at: "right bottom", of : editorOptWrapper});
							po.resizeVisualEditorPanel(tabPane, panel);
						}
					}
				}
			}
		});
		
		var deleteGroup = $("<div class='delete-group opt-group' />").appendTo(editorRightOptWrapper)
			.hover(
				function()
				{
					po.element(".delete-menu", this).show().position({ my : "right top", at : "right bottom", of : this});
				},
				function()
				{
					po.element(".delete-menu", this).hide();
				});
		$("<button type='button' />").text("<@spring.message code='delete' />").appendTo(deleteGroup).button();
		
		var deleteMenu = $("<ul class='delete-menu operation-menu ui-widget ui-widget-content ui-corner-all ui-front ui-widget-shadow' />");
		$("<li veOperation='deleteElement' class='quick-opt' />").html("<div><@spring.message code='dashboard.opt.delete.element' /></div>").appendTo(deleteMenu);
		$("<li class='ui-menu-divider' />").appendTo(deleteMenu);
		$("<li veOperation='unbindChart' class='quick-opt' />").html("<div><@spring.message code='dashboard.opt.delete.unbindChart' /></div>").appendTo(deleteMenu);
		deleteMenu.appendTo(deleteGroup).menu(
		{
			select: function(event, ui)
			{
				var item = ui.item;
				var veOperation = item.attr("veOperation");
				po.veOperation = veOperation;
				
				var dashboardEditor = po.visualDashboardEditor(tabPane);
				if(dashboardEditor)
				{
					if(veOperation == "deleteElement")
					{
						if(dashboardEditor.checkDeleteElement())
						{
							po.confirm("<@spring.message code='dashboard.opt.delete.element.confirm' />",
							{
								confirm: function()
								{
									dashboardEditor.deleteElement();
								}
							});
						}
					}
					else if(veOperation == "unbindChart")
					{
						if(dashboardEditor.checkUnbindChart())
						{
							po.confirm("<@spring.message code='dashboard.opt.delete.unbindChart.confirm' />",
							{
								confirm: function()
								{
									dashboardEditor.unbindChart();
								}
							});
						}
					}
				}
			}
		});
		
		$("<button type='button' />").text("<@spring.message code='save' />").appendTo(editorRightOptWrapper).button()
		.click(function()
		{
			po.veOperation = "save";
			po.saveResourceEditorContent(tabPane);
		});
		
		var moreGroup = $("<div class='more-group opt-group' />").appendTo(editorRightOptWrapper)
			.hover(
				function()
				{
					po.element(".more-menu", this).show().position({ my : "right top", at : "right bottom", of : this});
				},
				function()
				{
					po.element(".more-menu", this).hide();
				});
		$("<button type='button' />").text("<@spring.message code='dashboard.opt.more' />").appendTo(moreGroup).button();
		
		var moreMenu = $("<ul class='more-menu operation-menu ui-widget ui-widget-content ui-corner-all ui-front ui-widget-shadow' />");
		$("<li veOperation='dashboardSize' auto-close-prevent='veditor-dashboardSize-panel' />").html("<div><@spring.message code='dashboard.opt.dashboardSize' /></div>").appendTo(moreMenu);
		$("<li veOperation='eleBoundary' />").html("<div><@spring.message code='dashboard.opt.eleBoundary' /></div>").appendTo(moreMenu);
		$("<li veOperation='refresh' />").html("<div><@spring.message code='refresh' /></div>").appendTo(moreMenu);
		moreMenu.appendTo(moreGroup).menu(
		{
			select: function(event, ui)
			{
				var item = ui.item;
				var veOperation = item.attr("veOperation");
				po.veOperation = veOperation;
				
				if(veOperation == "dashboardSize")
				{
					var panel = po.element(".veditor-dashboardSize-panel");
					var visualEditorIfm = po.element(".tpl-visual-editor-ifm", tabPane);
					$.jsonToForm(po.element("form", panel),
					{
						width: parseInt(visualEditorIfm.css("width")),
						height: parseInt(visualEditorIfm.css("height")),
						scale: visualEditorIfm.data("veIframeScale")
					});
					panel.show().position({my: "right top", at: "right bottom", of : editorOptWrapper});
					po.resizeVisualEditorPanel(tabPane, panel);
				}
				else if(veOperation == "eleBoundary")
				{
					var dashboardEditor = po.visualDashboardEditor(tabPane);
					
					if(!dashboardEditor)
						return;
					
					var visualEditorIfm = po.element(".tpl-visual-editor-ifm", tabPane);
					dashboardEditor.enableElementBoundary(!dashboardEditor.enableElementBoundary());
				}
				else if(veOperation == "refresh")
				{
					var visualEditorIfm = po.element(".tpl-visual-editor-ifm", tabPane);
					var templateName = po.element(".resource-name-wrapper input.resourceName", tabPane).val();
					var editedHtml = null;
					
					var dashboardEditor = po.visualDashboardEditor(tabPane);
					if(dashboardEditor)
						editedHtml = dashboardEditor.editedHtml();
					if(!editedHtml)
						editedHtml = visualEditorIfm.data("veEditedHtml");
					if(!editedHtml)
					{
						var codeEditorDiv = po.element(".code-editor", tabPane);
						var codeEditor = codeEditorDiv.data("resourceEditorInstance");
						editedHtml = po.getCodeText(codeEditor);
					}
					if(!editedHtml)
						editedHtml = "";
					
					po.loadVisualEditorIframe(visualEditorIfm, templateName, editedHtml);
				}
			}
		});
		
		po.element(".quick-opt", editorRightOptWrapper).each(function()
		{
			$(this).attr("quick-opt-id", $.uid());
		})
		.click(function()
		{
			var quickOptBtn = po.element(".execute-quick-opt", editorRightOptWrapper);
			if(!quickOptBtn.data("quick-opt-enabled"))
			{
				quickOptBtn.button("enable");
				quickOptBtn.data("quick-opt-enabled", true);
			}
			
			quickOptBtn.attr("title", po.evalVeOptNamePath($(this)));
			
			po.veQuickOptId = $(this).attr("quick-opt-id");
		});
	};
	
	po.evalVeOptNamePath = function(optItem)
	{
		var name = "";
		
		while(!optItem.is(".opt-group, body"))
		{
			var myText = $("> .ui-menu-item-wrapper:first", optItem).text();
			if(myText)
				name = (name ? myText + " - "+name : myText);
			
			optItem = optItem.parent();
		}
		
		if(optItem.is(".opt-group"))
		{
			var myText = $("> button:first", optItem).text();
			if(myText)
				name = (name ? myText + " - "+name : myText);
		}
		
		return name;
	};
	
	po.resizeVisualEditorPanel = function(tabPane, panel)
	{
		if(panel.data("resizeVisualEditorPanel"))
			return;
		panel.data("resizeVisualEditorPanel", true);
		
		var vePanelContentHeight = tabPane.height();
		vePanelContentHeight = vePanelContentHeight - po.element(".panel-head", panel).outerHeight(true);
		vePanelContentHeight = vePanelContentHeight - po.element(".panel-foot", panel).outerHeight(true);
		vePanelContentHeight = vePanelContentHeight - 20;//减去间隙高度
		
		po.element(".panel-content", panel).css("max-height", vePanelContentHeight);
	};
	
	po.resizeVisualEditorStylePanel = function(tabPane, panel)
	{
		if(panel.data("resizeVisualEditorStylePanel"))
			return;
		panel.data("resizeVisualEditorStylePanel", true);
		
		var panelContent = po.element(".panel-content", panel);
		panelContent.css("height", panelContent.css("max-height"));
		
		var styleTabsHeight = panelContent.height();
		styleTabsHeight = styleTabsHeight - po.element(".form-item-syncChartTheme", panelContent).outerHeight(true);
		styleTabsHeight = styleTabsHeight - po.element(".form-item-className", panelContent).outerHeight(true);
		
		po.element(".style-tabs", panelContent).css("height", styleTabsHeight);
	};
	
	po.resizeVisualEditorChartOptionsPanel = function(tabPane, panel)
	{
		if(panel.data("resizeVisualEditorChartOptionsPanel"))
			return;
		panel.data("resizeVisualEditorChartOptionsPanel", true);
		
		var panelContent = po.element(".panel-content", panel);
		var textareaHeight = parseInt(panelContent.css("max-height")) - po.element(".form-item-label", panelContent).outerHeight()*3;
		var chartOptionsEditorWrapper = po.element(".chartOptions-editor-wrapper", panelContent);
		
		chartOptionsEditorWrapper.css("height", textareaHeight);
		chartOptionsEditorWrapper.data("chartOptionsCodeEditor").refresh();
	};
	
	po.setVeditorStyleFormValue = function($form, styleObj)
	{
		styleObj = $.extend({ syncChartTheme: true }, styleObj);
		
		if(po.isDisplayGrid(styleObj.display))
		{
			styleObj['align-items-grid'] = styleObj['align-items'];
			styleObj['justify-content-grid'] = styleObj['justify-content'];
			styleObj['align-content-grid'] = styleObj['align-content'];
		}
		else if(po.isDisplayFlex(styleObj.display))
		{
			styleObj['align-items-flex'] = styleObj['align-items'];
			styleObj['justify-content-flex'] = styleObj['justify-content'];
			styleObj['align-content-flex'] = styleObj['align-content'];
		}
		
		if(styleObj.isGridItemElement)
			styleObj['align-self-grid'] = styleObj['align-self'];
		else if(styleObj.isFlexItemElement)
			styleObj['align-self-flex'] = styleObj['align-self'];
		
		$.jsonToForm($form, styleObj,
		{
			handlers:
			{
				"color": function(form, value)
				{
					po.element(".color-indicator", form).css("background-color", (value||""));
					return false;
				},
				"background-color": function(form, value)
				{
					po.element(".bgcolor-indicator", form).css("background-color", (value||""));
					return false;
				},
				"border-color": function(form, value)
				{
					po.element(".border-color-indicator", form).css("background-color", (value||""));
					return false;
				}
			}
		});
	};
	
	po.setVeditorChartThemeFormValue = function($form, chartTheme)
	{
		$.jsonToForm($form, chartTheme,
		{
			handlers:
			{
				"color": function(form, value)
				{
					po.element(".color-indicator", form).css("background-color", (value||""));
					return false;
				},
				"backgroundColor": function(form, value)
				{
					po.element(".bgcolor-indicator", form).css("background-color", (value||""));
					return false;
				},
				"actualBackgroundColor": function(form, value)
				{
					po.element(".actbgcolor-indicator", form).css("background-color", (value||""));
					return false;
				},
				"graphColors": function(form, value)
				{
					var re = $.jsonToFormArrayHandler(form, value, ".graphColorsInput", function(wrapper)
					{
						po.addChartThemeFormGraphColorsItem(wrapper);
					},
					function(wrapper, item)
					{
						po.element("input[type='text']", item).listpalllet("destroy");
						item.remove();
					});
					
					po.element(".graphColorsInput .listpallet-indicator", form).each(function(i)
					{
						$(this).css("background-color", (value[i] || ""));
					});
					
					return re;
				},
				"graphRangeColors": function(form, value)
				{
					var re = $.jsonToFormArrayHandler(form, value, ".graphRangeColorsInput", function(wrapper)
					{
						po.addChartThemeFormGraphRangeColorsItem(wrapper);
					},
					function(wrapper, item)
					{
						po.element("input[type='text']", item).listpalllet("destroy");
						item.remove();
					});
					
					po.element(".graphRangeColorsInput .listpallet-indicator", form).each(function(i)
					{
						$(this).css("background-color", (value[i] || ""));
					});
					
					return re;
				}
			}
		});
	};

	po.setVeditorChartOptionsFormValue = function($form, chartOptionsStr)
	{
		if(chartOptionsStr && /^\s*[\{\[]/.test(chartOptionsStr))
		{
			var obj = chartFactory.evalSilently(chartOptionsStr, chartOptionsStr);
			
			if(!chartFactory.isString(obj))
				chartOptionsStr = JSON.stringify(obj, null, '\t');
		}
		
		var coEditor = po.element(".chartOptions-editor-wrapper", $form).data("chartOptionsCodeEditor");
		po.setCodeText(coEditor, (chartOptionsStr || ""));
		coEditor.focus();
	};
	
	po.addChartThemeFormGraphColorsItem = function(wrapper)
	{
		var id = $.uid();
		$("<div id='"+id+"' class='input-value-item'><input type='text' name='graphColors[]' class='ui-widget ui-widget-content ui-corner-all' size='100' />"
			+"&nbsp;<div class='listpallet-indicator ui-widget ui-widget-content ui-corner-all'></div>"
			+"&nbsp;&nbsp;&nbsp;&nbsp;<button type='button' class='del-color-btn small-button ui-button ui-corner-all ui-widget ui-button-icon-only'><span class='ui-icon ui-icon-close'></span><span class='ui-button-icon-space'></span>&nbsp;</button>"
			+"</div>").appendTo(wrapper);
		
		var inputItem = po.elementOfId(id, wrapper);
		po.elementOfName("graphColors[]", inputItem).listpalllet(
		{
			indicator: po.element(".listpallet-indicator", inputItem),
			container: inputItem,
			position: "fixed",
			autoCloseContext: po.element()
		});
	};
	
	po.addChartThemeFormGraphRangeColorsItem = function(wrapper)
	{
		var id = $.uid();
		$("<div id='"+id+"' class='input-value-item'><input type='text' name='graphRangeColors[]' class='ui-widget ui-widget-content ui-corner-all' size='100' />"
			+"&nbsp;<div class='listpallet-indicator ui-widget ui-widget-content ui-corner-all'></div>"
			+"&nbsp;&nbsp;&nbsp;&nbsp;<button type='button' class='del-color-btn small-button ui-button ui-corner-all ui-widget ui-button-icon-only'><span class='ui-icon ui-icon-close'></span><span class='ui-button-icon-space'></span>&nbsp;</button>"
			+"</div>").appendTo(wrapper);
		
		var inputItem = po.elementOfId(id, wrapper);
		po.elementOfName("graphRangeColors[]", inputItem).listpalllet(
		{
			indicator: po.element(".listpallet-indicator", inputItem),
			container: inputItem,
			position: "fixed",
			autoCloseContext: po.element()
		});
	};
	
	po.buildVisualEditorInsertMenuItems = function($parent, insertType)
	{
		var ul = $("<ul class='ui-widget-shadow' />");
		
		$("<li veOperation='insertGridLayout' insertType='"+insertType+"' class='quick-opt' auto-close-prevent='veditor-gridLayout-panel' />")
			.html("<div><@spring.message code='dashboard.opt.insertType.gridLayout' /></div>").appendTo(ul);
		
		$("<li veOperation='insertDiv' insertType='"+insertType+"' class='quick-opt' />")
			.html("<div><@spring.message code='dashboard.opt.insertType.div' /></div>").appendTo(ul);
		
		$("<li veOperation='insertLabel' insertType='"+insertType+"' class='quick-opt' auto-close-prevent='veditor-label-panel' />")
			.html("<div><@spring.message code='dashboard.opt.insertType.label' /></div>").appendTo(ul);
		
		$("<li veOperation='insertImage' insertType='"+insertType+"' class='quick-opt' auto-close-prevent='veditor-image-panel' />")
			.html("<div><@spring.message code='dashboard.opt.insertType.image' /></div>").appendTo(ul);
		
		$("<li veOperation='insertHyperlink' insertType='"+insertType+"' class='quick-opt' auto-close-prevent='veditor-hyperlink-panel' />")
			.html("<div><@spring.message code='dashboard.opt.insertType.hyperlink' /></div>").appendTo(ul);
		
		$("<li veOperation='insertVideo' insertType='"+insertType+"' class='quick-opt' auto-close-prevent='veditor-video-panel' />")
			.html("<div><@spring.message code='dashboard.opt.insertType.video' /></div>").appendTo(ul);
		
		$("<li class='ui-menu-divider' />").appendTo(ul);
		
		$("<li veOperation='insertChart' insertType='"+insertType+"' class='quick-opt' />")
			.html("<div><@spring.message code='dashboard.opt.insertType.chart' /></div>").appendTo(ul);
		
		ul.appendTo($parent);
	};
	
	po.insertVisualEditorChart = function(tabPane, chartWidgets)
	{
		var dashboardEditor = po.visualDashboardEditor(tabPane);
		if(dashboardEditor)
		{
			if(po.veOperation == "insertChart")
			{
				dashboardEditor.insertChart(chartWidgets, po.veOperationInsertType);
			}
			else if(po.veOperation == "bindChart")
			{
				dashboardEditor.bindChart((chartWidgets ? chartWidgets[0] : null));
			}
		}
	};
	
	po.toggleInsertChartListPannel = function(eleForPosition)
	{
		var chartListPanel = po.element(".chart-list-panel");
		
		if(chartListPanel.is(":hidden"))
		{
			chartListPanel.show();
			chartListPanel.position({ my : "right top", at : "right bottom", of : eleForPosition});
			
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
							
							po.insertEditorChart(chartWidgets);
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
	};
	
	po.insertEditorChart = function(chartWidgets)
	{
		var tabPane = po.getActiveResEditorTabPane();
		var codeEditorDiv = po.element(".code-editor", tabPane);
		var veIfmWrapper = po.element(".tpl-visual-editor-wrapper", tabPane);
		
		if(codeEditorDiv.hasClass("show-editor"))
		{
			po.insertCodeEditorChart(tabPane, chartWidgets);
		}
		else if(veIfmWrapper.hasClass("show-editor"))
		{
			po.insertVisualEditorChart(tabPane, chartWidgets);
		}
	};
	
	po.isDisplayGrid = function(display)
	{
		if(!display)
			return false;
		
		return /^(grid|inline-grid)$/i.test(display);
	};
	
	po.isDisplayFlex = function(display)
	{
		if(!display)
			return false;
		
		return /^(flex|inline-flex)$/i.test(display);
	};
})
(${pageId});
</script>

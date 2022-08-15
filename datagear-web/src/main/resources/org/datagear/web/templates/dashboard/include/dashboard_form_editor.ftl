<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#--
看板资源编辑器

依赖：

-->
<p-tabview v-model:active-index="pm.resourceContentTabs.activeIndex"
	:scrollable="true" @tab-change="onResourceContentTabChange"
	@tab-click="onResourceContentTabClick" class="contextmenu-tabview light-tabview"
	:class="{'opacity-0': pm.resourceContentTabs.items.length == 0}">
	<p-tabpanel v-for="tab in pm.resourceContentTabs.items" :key="tab.key" :header="tab.title">
		<template #header>
			<p-button type="button" icon="pi pi-angle-down"
				class="context-menu-btn p-button-xs p-button-secondary p-button-text p-button-rounded"
				@click="onResourceContentTabMenuToggle($event, tab)"
				aria-haspopup="true" aria-controls="${pid}resourceContentTabMenu">
			</p-button>
		</template>
		<div :id="tab.id">
			<div class="flex align-content-center justify-content-between">
				<div>
					<p-selectbutton v-model="tab.editMode" :options="pm.templateEditModeOptions"
						option-label="name" option-value="value" class="text-sm" @change="onChangeEditMode($event, tab)"
						v-if="tab.isTemplate">
					</p-selectbutton>
				</div>
				<div class="flex" v-if="!pm.isReadonlyAction && tab.editMode == 'code'">
					<p-button label="<@spring.message code='insertChart' />" class="p-button-sm for-open-chart-panel"
						@click="onInsertCodeEditorChart($event, tab)" v-if="tab.isTemplate">
					</p-button>
					<p-menubar :model="pm.codeEditMenuItems" class="light-menubar no-root-icon-menubar border-none pl-2 text-sm z-99">
						<template #end>
							<div class="p-inputgroup pl-2">
								<p-inputtext type="text" v-model="tab.searchCodeKeyword" class="text-sm p-0 px-1" style="width:9rem;" @keydown.enter.prevent="onSearchInCodeEditor($event, tab)"></p-inputtext>
								<p-button type="button" icon="pi pi-search" class="p-button-secondary p-button-sm" @click="onSearchInCodeEditor($event, tab)"></p-button>
							</div>
						</template>
					</p-menubar>
				</div>
				<div class="flex" v-if="!pm.isReadonlyAction && tab.editMode == 'visual'" v-if="tab.isTemplate">
					<p-button label="<@spring.message code='quickExecute' />" @click="pm.onQuickExecute($event, tab)"
						class="p-button-sm" :disabled="pm.quickExecuteMenuItem == null">
					</p-button>
					<p-menubar :model="pm.tplVisualEditMenuItems" class="ve-menubar light-menubar no-root-icon-menubar border-none pl-2 text-sm z-99">
					</p-menubar>
				</div>
			</div>
			<div class="pt-1 relative">
				<div class="code-editor-wrapper res-editor-wrapper p-component p-inputtext p-0 w-full absolute">
					<div :id="resCodeEditorEleId(tab)" class="code-editor"></div>
				</div>
				<div class="visual-editor-wrapper res-editor-wrapper opacity-hide p-component p-inputtext p-0 w-full absolute">
					<div class="visual-editor-ele-path-wrapper text-color-secondary text-sm">
						<div class="ele-path white-space-nowrap"></div>
					</div>
					<div class="visual-editor-iframe-wrapper">
						<iframe class="visual-editor-iframe shadow-4 border-none" :id="resVisualEditorEleId(tab)"
							:name="resVisualEditorEleId(tab)" @load="onVisualEditorIframeLoad($event, tab)">
						</iframe>
					</div>
				</div>
			</div>
		</div>
	</p-tabpanel>
</p-tabview>
<p-contextmenu id="${pid}resourceContentTabMenu" ref="${pid}resourceContentTabMenuEle"
	:model="pm.resourceContentTabMenuItems" :popup="true" class="text-sm">
</p-contextmenu>
<script>
(function(po)
{
	po.defaultTemplateName = "${defaultTempalteName}";
	
	po.resContentTabId = function(resName)
	{
		var map = (po.resContentTabIdMap || (po.resContentTabIdMap = {}));
		
		//不直接使用resName作为元素ID，因为resName中可能存在与jquery冲突的字符，比如'$'
		var value = map[resName];
		
		if(value == null)
		{
			value = $.uid("resCntTab");
			map[resName] = value;
		}
		
		return value;
	};
	
	po.resCodeEditorEleId = function(tab)
	{
		return tab.id + "codeEditor";
	};

	po.resVisualEditorEleId = function(tab)
	{
		return tab.id + "visualEditor";
	};
	
	po.toResourceContentTab = function(resName, isTemplate)
	{
		if(isTemplate == null)
		{
			var fm = po.vueFormModel();
			isTemplate = ($.inArray(resName, fm.templates) > -1);
		}
		
		var re =
		{
			id: po.resContentTabId(resName),
			key: resName,
			title: resName,
			editMode: "code",
			resName: resName,
			isTemplate: isTemplate,
			searchCodeKeyword: null
		};
		
		return re;
	};
	
	po.showResourceContentTab = function(resName, isTemplate)
	{
		var pm = po.vuePageModel();
		var items = pm.resourceContentTabs.items;
		var idx = $.inArrayById(items, po.resContentTabId(resName));
		
		if(idx > -1)
			pm.resourceContentTabs.activeIndex = idx;
		else
		{
			var tab = po.toResourceContentTab(resName, isTemplate);
			pm.resourceContentTabs.items.push(tab);
			
			//直接设置activeIndex不会滚动到新加的卡片，所以采用此方案
			po.vueApp().$nextTick(function()
			{
				pm.resourceContentTabs.activeIndex = pm.resourceContentTabs.items.length - 1;
			});
		}
	};
	
	po.loadResourceContentIfNon = function(tab)
	{
		var tabPanel = po.elementOfId(tab.id);
		var loaded = tabPanel.prop("loaded");
		
		if(!loaded && !tabPanel.prop("loading"))
		{
			tabPanel.prop("loading", true);
			
			var fm = po.vueFormModel();
			
			po.ajax("/dashboard/getResourceContent",
			{
				data:
				{
					id: fm.id,
					resourceName: tab.resName
				},
				success: function(response)
				{
					var resourceContent = (response.resourceExists ? response.resourceContent : "");
					if(tab.isTemplate && !resourceContent)
						resourceContent = (response.defaultTemplateContent || "");
					
					po.setResourceContent(tab, resourceContent);
					tabPanel.prop("loaded", true);
				},
				complete: function()
				{
					tabPanel.prop("loading", false);
				}
			});
		}
	};
	
	po.setResourceContent = function(tab, content)
	{
		var tabPanel = po.elementOfId(tab.id);
		var codeEditorEle = po.elementOfId(po.resCodeEditorEleId(tab), tabPanel);
		var codeEditor = codeEditorEle.data("codeEditorInstance");
		
		if(!codeEditor)
		{
			var codeEditorOptions =
			{
				value: content,
				matchBrackets: true,
				matchTags: true,
				autoCloseTags: true,
				autoCloseBrackets: true,
				readOnly: po.isReadonlyAction,
				foldGutter: true,
				gutters: ["CodeMirror-linenumbers", "CodeMirror-foldgutter"],
				mode: po.evalCodeModeByName(tab.resName)
			};
			
			if(tab.isTemplate && !codeEditorOptions.readOnly)
			{
				codeEditorOptions.hintOptions =
				{
					hint: po.codeEditorHintHandler
				};
			}
			
			codeEditor = po.createCodeEditor(codeEditorEle, codeEditorOptions);
			codeEditorEle.data("codeEditorInstance", codeEditor);
			
			if(tab.isTemplate)
			{
				if(!po.isReadonlyAction)
				{
					//光标移至"</body>"的上一行，便于用户直接输入内容
					var cursor = codeEditor.getSearchCursor("</body>");
					if(cursor.findNext())
					{
						var cursorFrom = cursor.from();
						codeEditor.getDoc().setCursor({ line: cursorFrom.line-1, ch: 0 });
					}
				}
				
				var visualEditorIfm = po.elementOfId(po.resVisualEditorEleId(tab), tabPanel);
				
				var topWindowSize = po.evalTopWindowSize();
				visualEditorIfm.css("width", topWindowSize.width);
				visualEditorIfm.css("height", topWindowSize.height);
				
				po.setVisualEditorIframeScale(visualEditorIfm);
			}
			
			codeEditor.focus();
		}
		else
		{
			po.setCodeText(codeEditor, content);
			codeEditor.focus();
		}
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
	
	po.getEditResourceInfos = function()
	{
		var re = [];
		
		var pm = po.vuePageModel();
		var items = pm.resourceContentTabs.items;
		
		$.each(items, function(idx, item)
		{
			var info = po.getEditResourceInfo(item);
			if(info)
				re.push(info);
		});
		
		return re;
	};
	
	po.getEditResourceInfo = function(tab, noContent)
	{
		noContent = (noContent == null ? false : noContent);
		
		if($.isTypeNumber(tab))
		{
			var pm = po.vuePageModel();
			var items = pm.resourceContentTabs.items;
			tab = items[tab];
		}
		
		if(tab == null)
			return null;
		
		var info = { name: tab.resName, content: "", isTemplate: tab.isTemplate };
		
		if(!noContent)
		{
			var editorEle = po.elementOfId(po.resCodeEditorEleId(tab));
			var codeEditor = editorEle.data("codeEditorInstance");
			info.content = po.getCodeText(codeEditor);
		}
		
		return info;
	};
	
	po.getCurrentEditResourceInfo = function(noContent)
	{
		var pm = po.vuePageModel();
		return po.getEditResourceInfo(pm.resourceContentTabs.activeIndex, noContent);
	};
	
	po.saveResourceInfo = function(resInfo)
	{
		if(!resInfo || !po.checkPersistedDashboard())
			return;
		
		var fm = po.vueFormModel();
		
		po.post("/dashboard/saveResourceContent",
		{
			id: fm.id,
			resourceName: resInfo.name,
			resourceContent: resInfo.content,
			isTemplate: resInfo.isTemplate
		},
		function(response)
		{
			if(response.data.templatesChanged)
				po.updateTemplateList(response.data.templates);
			
			if(!response.data.resourceExists)
				po.refreshLocalRes();
		});
	};
	
	po.searchInCodeEditor = function(tab)
	{
		var text = tab.searchCodeKeyword;
		
		if(!text)
			return;
		
		var codeEditorEle = po.elementOfId(po.resCodeEditorEleId(tab));
		var codeEditor = codeEditorEle.data("codeEditorInstance");
		
		var prevSearchText = codeEditorEle.data("prevSearchText");
		var cursor = codeEditorEle.data("prevSearchCursor");
		var doc = codeEditor.getDoc();
		
		if(!cursor || text != prevSearchText)
		{
			cursor = codeEditor.getSearchCursor(text);
			codeEditorEle.data("prevSearchCursor", cursor);
			codeEditorEle.data("prevSearchText", text)
		}
		
		codeEditor.focus();
		
		if(cursor.findNext())
			doc.setSelection(cursor.from(), cursor.to());
		else
		{
			//下次从头搜索
			codeEditorEle.data("prevSearchCursor", null);
		}
	};
	
	po.handleChangeEditMode = function(tab)
	{
		var tabPanel = po.elementOfId(tab.id);
		var codeEditorEle = po.elementOfId(po.resCodeEditorEleId(tab), tabPanel);
		var codeEditorWrapper = codeEditorEle.parent();
		var codeEditor = codeEditorEle.data("codeEditorInstance");
		var visualEditorIfm = po.elementOfId(po.resVisualEditorEleId(tab), tabPanel);
		var visualEditorIfmWrapper = visualEditorIfm.parent();
		var visualEditorWrapper = visualEditorIfmWrapper.parent();
		
		if(tab.editMode == "code")
		{
			var changeFlag = codeEditorEle.data("changeFlag");
			//初次由源码模式切换至可视编辑模式后，changeFlag会是1，
			//但此时是不需要同步的，所以这里手动设置为1
			if(changeFlag == null)
				changeFlag = 1;
			
			var dashboardEditor = po.visualDashboardEditorByIframe(visualEditorIfm);
			
			//有修改
			if(dashboardEditor && dashboardEditor.isChanged(changeFlag))
			{
				po.setCodeText(codeEditor, dashboardEditor.editedHtml());
				
				visualEditorIfmWrapper.data("changeFlag", codeEditor.changeGeneration());
				codeEditorEle.data("changeFlag", dashboardEditor.changeFlag());
			}
			
			codeEditorWrapper.removeClass("opacity-hide");
			visualEditorWrapper.addClass("opacity-hide");
		}
		else
		{
			var changeFlag = visualEditorIfmWrapper.data("changeFlag");
			
			//没有修改
			if(changeFlag != null && codeEditor.isClean(changeFlag))
				;
			else
			{
				//清空iframe后再显示，防止闪屏
				po.iframeDocument(visualEditorIfm).write("");
				
				visualEditorIfmWrapper.data("changeFlag", codeEditor.changeGeneration());
				codeEditorEle.data("changeFlag", null);
				
				po.loadVisualEditorIframe(visualEditorIfm, tab.resName, (po.isReadonlyAction ? "" : po.getCodeText(codeEditor)));
			}
			
			codeEditorWrapper.addClass("opacity-hide");
			visualEditorWrapper.removeClass("opacity-hide");
		}
	};
	
	po.initVisualDashboardEditor = function(tab)
	{
		var tabPanel = po.elementOfId(tab.id);
		var visualEditorIfm = po.elementOfId(po.resVisualEditorEleId(tab), tabPanel);
		var visualEditorIfmWrapper = visualEditorIfm.parent();
		var visualEditorWrapper = visualEditorIfmWrapper.parent();
		
		var ifmWindow = po.iframeWindow(visualEditorIfm);
		var dashboardEditor = (ifmWindow && ifmWindow.dashboardFactory ? ifmWindow.dashboardFactory.dashboardEditor : null);
		
		var elePathWrapper = po.element("> .visual-editor-ele-path-wrapper", visualEditorWrapper);
		var elePathEle = po.element("> .ele-path", elePathWrapper);
		elePathEle.empty();
		
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
				elePathEle.empty();
				var elePath = this.getElementPath(ele);
				
				$.each(elePath, function(i, ep)
				{
					var eleInfo = ep.tagName;
					if(ep.id)
						eleInfo += "#"+ep.id;
					else if(ep.className)
						eleInfo += "."+ep.className;
					
					if(i > 0)
						$("<span class='info-separator p-1 opacity-50' />").text(">").appendTo(elePathEle);
					
					$("<span class='ele-info cursor-pointer' />").text($.truncateIf(eleInfo, "...", ep.tagName.length+17))
						.attr("visualEditId", (ep.visualEditId || "")).attr("title", eleInfo).appendTo(elePathEle);
				});
				
				var elePathWrapperWidth = elePathWrapper.width();
				var elePathEleWidth = elePathEle.outerWidth(true);
				elePathEle.css("margin-left", (elePathEleWidth > elePathWrapperWidth ? (elePathWrapperWidth - elePathEleWidth) : 0)+"px");
			};
			dashboardEditor.deselectElementCallback = function()
			{
				elePathEle.empty();
				visualEditorIfm.data("selectedElementVeId", "");
			};
			dashboardEditor.beforeunloadCallback = function()
			{
				elePathEle.empty();
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
	
	po.visualDashboardEditorByIframe = function(visualEditorIfm)
	{
		var ifmWindow = po.iframeWindow(visualEditorIfm);
		var dashboardEditor = (ifmWindow && ifmWindow.dashboardFactory ? ifmWindow.dashboardFactory.dashboardEditor : null);
		
		return dashboardEditor;
	};

	po.visualDashboardEditorByTab = function(tab)
	{
		if(tab == null)
		{
			var pm = po.vuePageModel();
			var items = pm.resourceContentTabs.items;
			tab = items[pm.resourceContentTabs.activeIndex];
		}
		
		if(!tab)
			return null;
		
		var tabPanel = po.elementOfId(tab.id);
		var visualEditorIfm = po.elementOfId(po.resVisualEditorEleId(tab), tabPanel);
		
		return po.visualDashboardEditorByIframe(visualEditorIfm);
	};
	
	po.loadVisualEditorIframe = function(visualEditorIfm, templateName, templateContent)
	{
		var fm = po.vueFormModel();
		
		var form = po.elementOfId("${pid}visualEditorLoadForm");
		form.attr("action", po.showUrl(templateName));
		form.attr("target", visualEditorIfm.attr("name"));
		po.elementOfName("DG_EDIT_TEMPLATE", form).val(po.isReadonlyAction ? "false" : "true");
		po.elementOfName("DG_TEMPLATE_CONTENT", form).val(templateContent);
		
		form.submit();
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
	
	po.iframeWindow = function(iframe)
	{
		iframe = $(iframe)[0];
		return iframe.contentWindow;
	};
	
	po.iframeDocument = function(iframe)
	{
		iframe = $(iframe)[0];
		return (iframe.contentDocument || iframe.contentWindow.document);
	};
	
	//设置可视编辑iframe的尺寸，使其适配父元素尺寸而不会出现滚动条
	po.setVisualEditorIframeScale = function(iframe, scale)
	{
		iframe = $(iframe);
		scale = (scale == null || scale <= 0 ? "auto" : scale);
		
		iframe.data("veIframeScale", scale);
		
		if(scale == "auto")
		{
			var iframeWrapper = iframe.parent();
			var ww = iframeWrapper.innerWidth(), wh = iframeWrapper.innerHeight();
			var iw = iframe.width(), ih = iframe.height();
			
			//下面的计算只有iframe在iframeWrapper中是绝对定位的才准确
			var rightGap = 10, bottomGap = 20;
			var ileft = parseInt(iframe.css("left")), itop = parseInt(iframe.css("top"));
			ww = ww - ileft - rightGap;
			wh = wh - itop - bottomGap;
			
			if(iw <= ww && ih <= wh)
				return;
			
			var scaleX = ww/iw, scaleY = wh/ih;
			scale = Math.min(scaleX, scaleY);
		}
		else
			scale = scale/100;
		
		iframe.css("transform-origin", "0 0");
		iframe.css("transform", "scale("+scale+")");
	};
	
	po.showSelectChartDialog = function(selectHandler)
	{
		var dialog = $(".dashboard-select-chart-wrapper", document.body);
		
		if(dialog.length == 0)
		{
			po.handleOpenSelectAction("/chart/select?multiple",
			function(chartWidgets)
			{
				var myDialog = $(".dashboard-select-chart-wrapper", document.body);
				var handler = myDialog.data("dashboardSelectChartHandler");
				
				if(handler)
					handler(chartWidgets);
				
				po.hideSelectChartDialog();
				return false;
			},
			{
				modal: false,
				styleClass: "dashboard-select-chart-wrapper table-sm",
				width: "50vw",
				position: "right",
				onShow: function(dialog)
				{
					dialog.data("dashboardSelectChartHandler", selectHandler);
				}
			});
		}
		else
		{
			dialog.data("dashboardSelectChartHandler", selectHandler);
			
			var dialogMask = dialog.parent();
			dialogMask.removeClass("opacity-hide");
		}
	};
	
	po.hideSelectChartDialog = function()
	{
		var dialog = $(".dashboard-select-chart-wrapper", document.body);
		var dialogMask = dialog.parent();
		dialogMask.addClass("opacity-hide");
	};
	
	po.defaultInsertChartEleStyle = "display:inline-block;width:300px;height:300px;";
	
	po.insertCodeEditorChart = function(tab, chartWidgets)
	{
		if(!chartWidgets || !chartWidgets.length)
			return;
		
		var tabPanel = po.elementOfId(tab.id);
		var codeEditorEle = po.elementOfId(po.resCodeEditorEleId(tab), tabPanel);
		var codeEditor = codeEditorEle.data("codeEditorInstance");
		
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
	
	po.showFirstTemplateContent =function()
	{
		var fm = po.vueFormModel();
		
		if(fm.templates && fm.templates.length > 0)
			po.showResourceContentTab(fm.templates[0], true);
		else
			po.showResourceContentTab(po.defaultTemplateName, true);
	};
	
	po.bindOrReplaceVeChart = function(chartWidgets)
	{
		var dashboardEditor = po.visualDashboardEditorByTab();
		
		if(!dashboardEditor || !dashboardEditor.checkBindChart())
			return false;
		
		try
		{
			dashboardEditor.bindChart(chartWidgets ? chartWidgets[0] : null);
		}
		catch(e)
		{
			chartFactory.logException(e);
			return false;
		}
	};
	
	po.insertVeChart = function(chartWidgets)
	{
		var dashboardEditor = po.visualDashboardEditorByTab();
		var insertType = po.veCurrentInsertType;
		
		if(!dashboardEditor || !insertType || !dashboardEditor.checkInsertChart(insertType))
			return false;
		
		try
		{
			dashboardEditor.insertChart(chartWidgets, po.veCurrentInsertType);
		}
		catch(e)
		{
			chartFactory.logException(e);
			return false;
		}
	};
	
	po.insertVeGridLayout = function(model)
	{
		var dashboardEditor = po.visualDashboardEditorByTab();
		var insertType = po.veCurrentInsertType;
		
		if(!dashboardEditor || !insertType || !dashboardEditor.checkInsertGridLayout(insertType))
			return false;
		
		try
		{
			dashboardEditor.insertGridLayout(model, po.veCurrentInsertType);
		}
		catch(e)
		{
			chartFactory.logException(e);
			return false;
		}
	};
	
	po.insertVeTextElement = function(model)
	{
		var dashboardEditor = po.visualDashboardEditorByTab();
		var insertType = po.veCurrentInsertType;
		
		if(!dashboardEditor || !insertType || !dashboardEditor.checkInsertLabel(insertType))
			return false;
		
		try
		{
			dashboardEditor.insertLabel(model, po.veCurrentInsertType);
		}
		catch(e)
		{
			chartFactory.logException(e);
			return false;
		}
	};

	po.insertVeImage = function(model)
	{
		var dashboardEditor = po.visualDashboardEditorByTab();
		var insertType = po.veCurrentInsertType;
		
		if(!dashboardEditor || !insertType || !dashboardEditor.checkInsertImage(insertType))
			return false;
		
		try
		{
			dashboardEditor.insertImage(model, po.veCurrentInsertType);
		}
		catch(e)
		{
			chartFactory.logException(e);
			return false;
		}
	};

	po.insertVeHyperlink = function(model)
	{
		var dashboardEditor = po.visualDashboardEditorByTab();
		var insertType = po.veCurrentInsertType;
		
		if(!dashboardEditor || !insertType || !dashboardEditor.checkInsertHyperlink(insertType))
			return false;
		
		try
		{
			dashboardEditor.insertHyperlink(model, po.veCurrentInsertType);
		}
		catch(e)
		{
			chartFactory.logException(e);
			return false;
		}
	};
	
	po.insertVeVideo = function(model)
	{
		var dashboardEditor = po.visualDashboardEditorByTab();
		var insertType = po.veCurrentInsertType;
		
		if(!dashboardEditor || !insertType || !dashboardEditor.checkInsertVideo(insertType))
			return false;
		
		try
		{
			dashboardEditor.insertVideo(model, po.veCurrentInsertType);
		}
		catch(e)
		{
			chartFactory.logException(e);
			return false;
		}
	};
	
	po.veQuickExecute = function(tab)
	{
		var pm = po.vuePageModel();
		
		if(pm.quickExecuteMenuItem)
			pm.quickExecuteMenuItem.command();
	};
	
	po.veQuickExecuteMenuItem = function(menuItem)
	{
		var pm = po.vuePageModel();
		pm.quickExecuteMenuItem = menuItem;
	};
	
	po.buildTplVisualInsertMenuItems = function(insertType)
	{
		var items =
		[
			{
				label: "<@spring.message code='chart' />",
				class: "for-open-chart-panel",
				insertType: insertType,
				command: function()
				{
					po.veQuickExecuteMenuItem(this);
					
					var dashboardEditor = po.visualDashboardEditorByTab();
					if(dashboardEditor)
					{
						if(!dashboardEditor.checkInsertChart(this.insertType))
							return;
						
						po.veCurrentInsertType = this.insertType;
						po.showSelectChartDialog(function(chartWidgets)
						{
							po.insertVeChart(chartWidgets);
						});
					}
				}
			},
			{ separator: true },
			{
				label: "<@spring.message code='gridLayout' />",
				insertType: insertType,
				class: "ve-panel-show-control gridLayoutShown",
				command: function()
				{
					po.veQuickExecuteMenuItem(this);
					
					var dashboardEditor = po.visualDashboardEditorByTab();
					if(dashboardEditor)
					{
						po.veCurrentInsertType = this.insertType;
						var showFillParent = dashboardEditor.canInsertFillParentGridLayout(this.insertType);
						po.showVeGridLayoutPanel(showFillParent);
					}
				}
			},
			{
				label: "<@spring.message code='divElement' />",
				insertType: insertType,
				command: function()
				{
					po.veQuickExecuteMenuItem(this);
					
					var dashboardEditor = po.visualDashboardEditorByTab();
					if(dashboardEditor)
					{
						if(!dashboardEditor.checkInsertDiv(this.insertType))
							return;
						
						dashboardEditor.insertDiv(this.insertType);
					}
				}
			},
			{
				label: "<@spring.message code='textElement' />",
				insertType: insertType,
				class: "ve-panel-show-control textElementShown",
				command: function()
				{
					po.veQuickExecuteMenuItem(this);
					
					var dashboardEditor = po.visualDashboardEditorByTab();
					if(dashboardEditor)
					{
						po.veCurrentInsertType = this.insertType;
						po.showVeTextElementPanel();
					}
				}
			},
			{
				label: "<@spring.message code='image' />",
				insertType: insertType,
				class: "ve-panel-show-control imageShown",
				command: function()
				{
					po.veQuickExecuteMenuItem(this);
					
					var dashboardEditor = po.visualDashboardEditorByTab();
					if(dashboardEditor)
					{
						po.veCurrentInsertType = this.insertType;
						po.showVeImagePanel();
					}
				}
			},
			{
				label: "<@spring.message code='hyperlink' />",
				insertType: insertType,
				class: "ve-panel-show-control hyperlinkShown",
				command: function()
				{
					po.veQuickExecuteMenuItem(this);
					
					var dashboardEditor = po.visualDashboardEditorByTab();
					if(dashboardEditor)
					{
						po.veCurrentInsertType = this.insertType;
						po.showVeHyperlinkPanel();
					}
				}
			},
			{
				label: "<@spring.message code='video' />",
				insertType: insertType,
				class: "ve-panel-show-control videoShown",
				command: function()
				{
					po.veQuickExecuteMenuItem(this);
					
					var dashboardEditor = po.visualDashboardEditorByTab();
					if(dashboardEditor)
					{
						po.veCurrentInsertType = this.insertType;
						po.showVeVideoPanel();
					}
				}
			}
		];
		
		return items;
	};

	po.setupResourceEditor = function()
	{
		var fm = po.vueFormModel();
		var pm = po.vuePageModel();
		
		po.vuePageModel(
		{
			templateEditModeOptions:
			[
				{ name: "<@spring.message code='dashboard.templateEditMode.code' />", value: "code" },
				{ name: "<@spring.message code='dashboard.templateEditMode.visual' />", value: "visual" }
			],
			resourceContentTabs:
			{
				items: [],
				activeIndex: 0
			},
			resourceContentTabMenuItems:
			[
				{
					label: "<@spring.message code='close' />",
					command: function()
					{
						po.tabviewClose(po.vuePageModel().resourceContentTabs, po.resourceContentTabMenuTargetId);
					}
				},
				{
					label: "<@spring.message code='closeOther' />",
					command: function()
					{
						po.tabviewCloseOther(po.vuePageModel().resourceContentTabs, po.resourceContentTabMenuTargetId);
					}
				},
				{
					label: "<@spring.message code='closeRight' />",
					command: function()
					{
						po.tabviewCloseRight(po.vuePageModel().resourceContentTabs, po.resourceContentTabMenuTargetId);
					}
				},
				{
					label: "<@spring.message code='closeLeft' />",
					command: function()
					{
						po.tabviewCloseLeft(po.vuePageModel().resourceContentTabs, po.resourceContentTabMenuTargetId);
					}
				},
				{
					label: "<@spring.message code='closeAll' />",
					command: function()
					{
						po.tabviewCloseAll(po.vuePageModel().resourceContentTabs);
					}
				}
			],
			codeEditMenuItems:
			[
				{
					label: "<@spring.message code='save' />",
					command: function(e)
					{
						var info = po.getEditResourceInfo(pm.resourceContentTabs.activeIndex);
						po.saveResourceInfo(info);
					}
				}
			],
			tplVisualEditMenuItems:
			[
				{
					label: "<@spring.message code='select' />",
					items:
					[
						{
							label: "<@spring.message code='nextElement' />",
							command: function()
							{
								var dashboardEditor = po.visualDashboardEditorByTab();
								if(dashboardEditor)
									dashboardEditor.selectNextElement();
							}
						},
						{
							label: "<@spring.message code='prevElement' />",
							command: function()
							{
								var dashboardEditor = po.visualDashboardEditorByTab();
								if(dashboardEditor)
									dashboardEditor.selectPrevElement();
							}
						},
						{
							label: "<@spring.message code='subElement' />",
							command: function()
							{
								var dashboardEditor = po.visualDashboardEditorByTab();
								if(dashboardEditor)
									dashboardEditor.selectFirstChildElement();
							}
						},
						{
							label: "<@spring.message code='parentElement' />",
							command: function()
							{
								var dashboardEditor = po.visualDashboardEditorByTab();
								if(dashboardEditor)
									dashboardEditor.selectParentElement();
							}
						},
						{ separator: true },
						{
							label: "<@spring.message code='cancelSelect' />",
							command: function()
							{
								var dashboardEditor = po.visualDashboardEditorByTab();
								if(dashboardEditor)
									dashboardEditor.deselectElement();
							}
						}
					]
				},
				{
					label: "<@spring.message code='insert' />",
					items:
					[
						{
							label: "<@spring.message code='bindOrReplaceChart' />",
							class: "for-open-chart-panel",
							command: function()
							{
								po.veQuickExecuteMenuItem(this);
								
								var dashboardEditor = po.visualDashboardEditorByTab();
								if(dashboardEditor)
								{
									if(!dashboardEditor.checkBindChart())
										return;
									
									po.showSelectChartDialog(function(chartWidgets)
									{
										po.bindOrReplaceVeChart(chartWidgets);
									});
								}
							}
						},
						{ separator: true },
						{ label: "<@spring.message code='outerInsertAfter' />", items: po.buildTplVisualInsertMenuItems("after") },
						{ label: "<@spring.message code='outerInsertBefore' />", items: po.buildTplVisualInsertMenuItems("before") },
						{ label: "<@spring.message code='innerInsertAfter' />", items: po.buildTplVisualInsertMenuItems("append") },
						{ label: "<@spring.message code='innerInsertBefore' />", items: po.buildTplVisualInsertMenuItems("prepend") }
					]
				},
				{
					label: "<@spring.message code='edit' />",
					items:
					[
						{ label: "<@spring.message code='globalStyle' />" },
						{ label: "<@spring.message code='globalChartTheme' />" },
						{ label: "<@spring.message code='globalChartOptions' />" },
						{ separator: true },
						{ label: "<@spring.message code='style' />" },
						{ label: "<@spring.message code='chartTheme' />" },
						{ label: "<@spring.message code='chartOptions' />" },
						{ label: "<@spring.message code='elementAttribute' />" },
						{ label: "<@spring.message code='textContent' />" }
					]
				},
				{
					label: "<@spring.message code='delete' />",
					items:
					[
						{
							label: "<@spring.message code='deleteElement' />",
							command: function()
							{
								var dashboardEditor = po.visualDashboardEditorByTab();
								if(dashboardEditor)
								{
									if(dashboardEditor.checkDeleteElement())
									{
										po.confirm(
										{
											message: "<@spring.message code='dashboard.opt.delete.element.confirm' />",
											accept: function()
											{
												dashboardEditor.deleteElement();
											}
										});
									}
								}
							}
						},
						{ separator: true },
						{ label: "<@spring.message code='unbindChart' />" }
					]
				},
				{
					label: "<@spring.message code='save' />",
					command: function(e)
					{
						var info = po.getEditResourceInfo(pm.resourceContentTabs.activeIndex);
						po.saveResourceInfo(info);
					}
				},
				{
					label: "<@spring.message code='more' />",
					items:
					[
						{
							label: "<@spring.message code='dashboardSize' />",
							command: function()
							{
							}
						},
						{
							label: "<@spring.message code='elementBoundary' />",
							command: function()
							{
								var dashboardEditor = po.visualDashboardEditorByTab();
								if(dashboardEditor)
									dashboardEditor.enableElementBoundary(!dashboardEditor.enableElementBoundary());
							}
						},
						{
							label: "<@spring.message code='refresh' />",
							command: function()
							{
							}
						}
					]
				}
			],
			quickExecuteMenuItem: null,
			onQuickExecute: function(e, tab)
			{
				e.stopPropagation();
				po.element().click();
				
				po.veQuickExecute(tab);
			}
		});
		
		po.vueMethod(
		{
			onResourceContentTabChange: function()
			{
				
			},
			
			onResourceContentTabMenuToggle: function(e, tab)
			{
				po.resourceContentTabMenuTargetId = tab.id;
				po.vueUnref("${pid}resourceContentTabMenuEle").show(e);
			},
			
			resCodeEditorEleId: function(tab)
			{
				return po.resCodeEditorEleId(tab);
			},
			
			resVisualEditorEleId: function(tab)
			{
				return po.resVisualEditorEleId(tab);
			},
			
			onChangeEditMode: function(e, tab)
			{
				po.handleChangeEditMode(tab);
			},
			
			onSearchInCodeEditor: function(e, tab)
			{
				po.searchInCodeEditor(tab);
			},
			
			onVisualEditorIframeLoad: function(e, tab)
			{
				po.initVisualDashboardEditor(tab);
			},
			
			onInsertCodeEditorChart: function(e, tab)
			{
				po.showSelectChartDialog(function(chartWidgets)
				{
					po.insertCodeEditorChart(tab, chartWidgets);
				});
			}
		});
		
		po.vueRef("${pid}resourceContentTabMenuEle", null);
		po.vueRef("${pid}chartPanelEle", null);
		
		//po.showResourceContentTab()里不能获取到创建的DOM元素，所以采用此方案
		po.vueWatch(pm.resourceContentTabs, function(oldVal, newVal)
		{
			var items = newVal.items;
			var activeIndex = newVal.activeIndex;
			var activeTab = items[activeIndex];
			
			if(activeTab)
			{
				po.vueApp().$nextTick(function()
				{
					po.loadResourceContentIfNon(activeTab);
				});
			}
		});
		
		po.element().click(function(e)
		{
			var targetEle = $(e.target);
			
			if(targetEle.hasClass("for-open-chart-panel") || targetEle.closest(".for-open-chart-panel").length > 0)
				;//保持选择图表对话框
			else
				po.hideSelectChartDialog();
			
			//隐藏其他对话框
			var vePanelShowControlEle = targetEle;
			if(!vePanelShowControlEle.hasClass("ve-panel-show-control"))
				vePanelShowControlEle = targetEle.closest(".ve-panel-show-control");
			var pm = po.vuePageModel();
			if(vePanelShowControlEle.length > 0)
			{
				$.each(pm.vepss, function(p, v)
				{
					if(v == true && !vePanelShowControlEle.hasClass(p))
						pm.vepss[p] = false;
				});
			}
			else
			{
				$.each(pm.vepss, function(p, v)
				{
					pm.vepss[p] = false;
				});
			}
		});
	};
})
(${pid});
</script>
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
<p-tabview v-model:active-index="pm.resContentTabs.activeIndex"
	:scrollable="true" @tab-change="onResourceContentTabChange"
	@tab-click="onResourceContentTabClick" class="contextmenu-tabview light-tabview"
	:class="{'opacity-0': pm.resContentTabs.items.length == 0}">
	<p-tabpanel v-for="tab in pm.resContentTabs.items" :key="tab.id" :header="tab.title">
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
					<p-menubar :model="pm.codeEditMenuItems" class="ve-menubar light-menubar no-root-icon-menubar border-none pl-2 text-sm z-99">
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
						class="p-button-sm" :disabled="pm.quickExecuteMenuItem == null" v-tooltip.top="pm.quickExecuteTooltip">
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
						<div class="ele-path white-space-nowrap">
							<span v-for="(ep, epIdx) in tab.veElementPath" :key="epIdx">
								<span class="info-separator p-1 opacity-50" v-if="epIdx > 0">&gt;</span>
								<span class="ele-info cursor-pointer" :title="ep.displayName"
									@click="onVeSelectByElePath($event, ep)">
									{{formatVeElePathDisplayName(ep)}}
								</span>
							</span>
						</div>
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
<p-menu id="${pid}resourceContentTabMenu" ref="${pid}resourceContentTabMenuEle"
	:model="pm.resContentTabMenuItems" :popup="true" class="text-sm">
</p-menu>
<script>
(function(po)
{
	po.defaultTemplateName = "${defaultTempalteName}";
	
	//实现重命名操作后编辑器更新逻辑
	po.updateEditorResNames = function(renames)
	{
		/*
		//暂时禁用，因为更新编辑器选项卡信息会导致卡片重绘而丢失已编辑信息
		var tabIdMap = po.resContentTabIdMap;
		if(tabIdMap)
		{
			$.each(renames, function(oldName, newName)
			{
				var tabId = tabIdMap[oldName];
				if(tabId != null)
				{
					tabIdMap[newName] = tabId;
					tabIdMap[oldName] = null;
				}
			});
		}
		
		var pm = po.vuePageModel();
		$.each(pm.resContentTabs.items, function(i, tab)
		{
			var newName = renames[tab.resName];
			if(newName != null)
			{
				tab.title = newName;
				tab.resName = newName;
			}
		});
		*/
	};
	
	po.resContentTabId = function(resName)
	{
		var map = (po.resContentTabIdMap || (po.resContentTabIdMap = {}));
		
		//不直接使用resName作为元素ID，因为resName中可能存在与jquery冲突的字符，比如'$'
		var tabId = map[resName];
		
		if(tabId == null)
		{
			tabId = $.uid("resCntTab");
			map[resName] = tabId;
		}
		
		return tabId;
	};
	
	po.resCodeEditorEleId = function(tab)
	{
		return tab.id + "codeEditor";
	};

	po.resVisualEditorEleId = function(tab)
	{
		return tab.id + "visualEditor";
	};
	
	po.toResContentTab = function(resName, isTemplate)
	{
		if(isTemplate == null)
		{
			var fm = po.vueFormModel();
			isTemplate = ($.inArray(resName, fm.templates) > -1);
		}
		
		var re =
		{
			id: po.resContentTabId(resName),
			title: resName,
			editMode: "code",
			resName: resName,
			isTemplate: isTemplate,
			searchCodeKeyword: null,
			veElementPath: []
		};
		
		return re;
	};
	
	po.showResContentTab = function(resName, isTemplate)
	{
		var pm = po.vuePageModel();
		var items = pm.resContentTabs.items;
		var idx = $.inArrayById(items, po.resContentTabId(resName));
		
		if(idx > -1)
			pm.resContentTabs.activeIndex = idx;
		else
		{
			var tab = po.toResContentTab(resName, isTemplate);
			pm.resContentTabs.items.push(tab);
			
			//直接设置activeIndex不会滚动到新加的卡片
			po.vueNextTick(function()
			{
				pm.resContentTabs.activeIndex = pm.resContentTabs.items.length - 1;
			});
		}
	};
	
	po.loadResContentIfNon = function(tab)
	{
		var loadStatus = tab.loadStatus;
		var needLoad = (loadStatus != "loaded" && loadStatus != "loading");
		
		if(needLoad)
		{
			tab.loadStatus = "loading";
			
			var tabPanel = po.elementOfId(tab.id);
			var fm = po.vueFormModel();
			var id = (po.isPersistedDashboard() ? fm.id : po.copySourceId);
			
			po.ajax("/dashboard/getResourceContent",
			{
				data:
				{
					id: id,
					resourceName: tab.resName
				},
				success: function(response)
				{
					var resourceContent = (response.resourceExists ? response.resourceContent : "");
					if(tab.isTemplate && !resourceContent)
						resourceContent = (response.defaultTemplateContent || "");
					
					po.setResContent(tab, resourceContent);
				},
				complete: function()
				{
					tab.loadStatus = "loaded";
				}
			});
		}
	};
	
	po.setResContent = function(tab, content)
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
				po.setVeDashboardSize(tab, {});
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
	
	po.getEditResInfos = function()
	{
		var re = [];
		
		var pm = po.vuePageModel();
		var items = pm.resContentTabs.items;
		
		$.each(items, function(idx, item)
		{
			var info = po.getEditResInfo(item);
			if(info)
				re.push(info);
		});
		
		return re;
	};
	
	po.getEditResInfo = function(tab, noContent)
	{
		noContent = (noContent == null ? false : noContent);
		
		if($.isTypeNumber(tab))
		{
			var pm = po.vuePageModel();
			var items = pm.resContentTabs.items;
			tab = items[tab];
		}
		
		if(tab == null)
			return null;
		
		var info = { name: tab.resName, content: "", isTemplate: tab.isTemplate };
		
		if(!noContent)
		{
			if(tab.editMode == "code")
			{
				var editorEle = po.elementOfId(po.resCodeEditorEleId(tab));
				var codeEditor = editorEle.data("codeEditorInstance");
				info.content = po.getCodeText(codeEditor);
			}
			else
			{
				var dashboardEditor = po.visualDashboardEditorByTab(tab);
				info.content = dashboardEditor.editedHtml();
			}
		}
		
		return info;
	};
	
	po.getCurrentEditResInfo = function(noContent)
	{
		return po.getEditResInfo(po.getCurrentEditTab(), noContent);
	};
	
	po.getCurrentEditTab = function()
	{
		var pm = po.vuePageModel();
		var items = pm.resContentTabs.items;
		return items[pm.resContentTabs.activeIndex];
	};
	
	po.saveResInfo = function(resInfo)
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
			dashboardEditor.i18n.chartPluginNoAttrDefined = "<@spring.message code='dashboard.opt.tip.chartPluginNoAttrDefined' />";
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
				var tab = po.getCurrentEditTab();
				tab.veElementPath = this.getElementPath(ele);
			};
			dashboardEditor.deselectElementCallback = function()
			{
				var tab = po.getCurrentEditTab();
				tab.veElementPath = [];
			};
			dashboardEditor.beforeunloadCallback = function()
			{
				var tab = po.getCurrentEditTab();
				tab.veElementPath = [];
				
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
			var items = pm.resContentTabs.items;
			tab = items[pm.resContentTabs.activeIndex];
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
	
	po.showSelectChartDialog = function(selectHandler)
	{
		var dialog = po.selectChartDialog();
		
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
		var dialog = po.selectChartDialog();
		var dialogMask = dialog.parent();
		dialogMask.addClass("opacity-hide");
	};
	
	po.closeSelectChartDialog = function()
	{
		var dialog = po.selectChartDialog();
		$.closeDialog(dialog);
	};
	
	po.selectChartDialog = function()
	{
		return $(".dashboard-select-chart-wrapper", document.body);
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
	
	po.showFirstTemplateContent = function()
	{
		var fm = po.vueFormModel();
		
		if(fm.templates && fm.templates.length > 0)
			po.showResContentTab(fm.templates[0], true);
		else
			po.showResContentTab(po.defaultTemplateName, true);
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

	po.insertVeFlexLayout = function(model)
	{
		var dashboardEditor = po.visualDashboardEditorByTab();
		var insertType = po.veCurrentInsertType;
		
		if(!dashboardEditor || !insertType || !dashboardEditor.checkInsertFlexLayout(insertType))
			return false;
		
		try
		{
			dashboardEditor.insertFlexLayout(model, po.veCurrentInsertType);
		}
		catch(e)
		{
			chartFactory.logException(e);
			return false;
		}
	};

	po.insertVeHxtitle = function(model)
	{
		var dashboardEditor = po.visualDashboardEditorByTab();
		var insertType = po.veCurrentInsertType;
		
		if(!dashboardEditor || !insertType || !dashboardEditor.checkInsertHxtitle(insertType))
			return false;
		
		try
		{
			dashboardEditor.insertHxtitle(model, po.veCurrentInsertType);
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

	po.updateVeTextElement = function(model)
	{
		var dashboardEditor = po.visualDashboardEditorByTab();
		
		if(!dashboardEditor || !dashboardEditor.checkSetElementText())
			return false;
		
		try
		{
			dashboardEditor.setElementText(model.content);
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

	po.updateVeImage = function(model)
	{
		var dashboardEditor = po.visualDashboardEditorByTab();
		
		if(!dashboardEditor)
			return false;
		
		try
		{
			dashboardEditor.setImageAttr(model);
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

	po.updateVeHyperlink = function(model)
	{
		var dashboardEditor = po.visualDashboardEditorByTab();
		
		if(!dashboardEditor)
			return false;
		
		try
		{
			dashboardEditor.setHyperlinkAttr(model);
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

	po.updateVeVideo = function(model)
	{
		var dashboardEditor = po.visualDashboardEditorByTab();
		
		if(!dashboardEditor)
			return false;
		
		try
		{
			dashboardEditor.setVideoAttr(model);
		}
		catch(e)
		{
			chartFactory.logException(e);
			return false;
		}
	};
	
	po.setVeChartOptions = function(model, global)
	{
		var dashboardEditor = po.visualDashboardEditorByTab();
		
		if(!dashboardEditor)
			return false;
		
		try
		{
			if(global)
				dashboardEditor.setGlobalChartOptions(model.value);
			else
				dashboardEditor.setElementChartOptions(model.value);
		}
		catch(e)
		{
			chartFactory.logException(e);
			return false;
		}
	};

	po.setVeChartTheme = function(model, global)
	{
		var dashboardEditor = po.visualDashboardEditorByTab();
		
		if(!dashboardEditor)
			return false;
		
		try
		{
			if(global)
				dashboardEditor.setGlobalChartTheme(model);
			else
				dashboardEditor.setElementChartTheme(model);
		}
		catch(e)
		{
			chartFactory.logException(e);
			return false;
		}
	};
	
	po.setVeElementChartAttrValues = function(model)
	{
		var dashboardEditor = po.visualDashboardEditorByTab();
		
		if(!dashboardEditor)
			return false;
		
		try
		{
			dashboardEditor.setElementChartAttrValues(model);
		}
		catch(e)
		{
			chartFactory.logException(e);
			return false;
		}
	};
	
	po.setVeStyle = function(model, global)
	{
		var dashboardEditor = po.visualDashboardEditorByTab();
		
		if(!dashboardEditor)
			return false;
		
		if(po.isDisplayGrid(model.display))
		{
			model['align-items'] = model['align-items-grid'];
			model['justify-content'] = model['justify-content-grid'];
			model['align-content'] = model['align-content-grid'];
		}
		else if(po.isDisplayFlex(model.display))
		{
			model['align-items'] = model['align-items-flex'];
			model['justify-content'] = model['justify-content-flex'];
			model['align-content'] = model['align-content-flex'];
		}
		
		if(dashboardEditor.isGridItemElement())
			model['align-self'] = model['align-self-grid'];
		else if(dashboardEditor.isFlexItemElement())
			model['align-self'] = model['align-self-flex'];
		
		model['align-items-grid'] = null;
		model['justify-content-grid'] = null;
		model['align-content-grid'] = null;
		model['align-self-grid'] = null;
		model['align-items-flex'] = null;
		model['justify-content-flex'] = null;
		model['align-content-flex'] = null;
		model['align-self-flex'] = null;
		
		try
		{
			if(global)
				dashboardEditor.setGlobalStyle(model);
			else
				dashboardEditor.setElementStyle(model);
		}
		catch(e)
		{
			chartFactory.logException(e);
			return false;
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
	
	po.convertToVeStyleFormModel = function(styleModel)
	{
		styleModel = $.extend({ syncChartTheme: true }, styleModel);
		
		if(po.isDisplayGrid(styleModel.display))
		{
			styleModel['align-items-grid'] = styleModel['align-items'];
			styleModel['justify-content-grid'] = styleModel['justify-content'];
			styleModel['align-content-grid'] = styleModel['align-content'];
		}
		else if(po.isDisplayFlex(styleModel.display))
		{
			styleModel['align-items-flex'] = styleModel['align-items'];
			styleModel['justify-content-flex'] = styleModel['justify-content'];
			styleModel['align-content-flex'] = styleModel['align-content'];
		}
		
		if(styleModel.isGridItemElement)
			styleModel['align-self-grid'] = styleModel['align-self'];
		else if(styleModel.isFlexItemElement)
			styleModel['align-self-flex'] = styleModel['align-self'];
		
		styleModel.isGridItemElement = undefined;
		styleModel.isFlexItemElement = undefined;
		
		return styleModel;
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
		
		var tooltip = "";
		if(menuItem)
		{
			var tooltip = "";
			var labelPath = [ menuItem.label ];
			if(menuItem.parentLabelPath)
			{
				labelPath = ($.isArray(menuItem.parentLabelPath) ?
								menuItem.parentLabelPath : [ menuItem.parentLabelPath ]).concat(labelPath);
			}
			
			$.each(labelPath, function(i, pl)
			{
				tooltip += (tooltip ? " - " + pl : pl);
			});
		}
		
		pm.quickExecuteTooltip = tooltip;
	};
	
	po.veRefresh = function()
	{
		var tab = po.getCurrentEditTab();
		var tabPanel = po.elementOfId(tab.id);
		var visualEditorIfm = po.elementOfId(po.resVisualEditorEleId(tab), tabPanel);
		var dashboardEditor = po.visualDashboardEditorByIframe(visualEditorIfm);
		var editedHtml = "";
		
		if(dashboardEditor)
			editedHtml = dashboardEditor.editedHtml();
		if(!editedHtml)
			editedHtml = visualEditorIfm.data("veEditedHtml");
		if(!editedHtml)
		{
			var codeEditorEle = po.elementOfId(po.resCodeEditorEleId(tab), tabPanel);
			var codeEditorWrapper = codeEditorEle.parent();
			var codeEditor = codeEditorEle.data("codeEditorInstance");
			editedHtml = po.getCodeText(codeEditor);
		}
		if(!editedHtml)
			editedHtml = "";
		
		po.loadVisualEditorIframe(visualEditorIfm, tab.resName, editedHtml);
	};
	
	po.setVeDashboardSize = function(tab, model)
	{
		tab = (tab == null ? po.getCurrentEditTab() : tab);
		model = (model || {});
		
		model =
		{
			width: parseInt(model.width),
			height: parseInt(model.height),
			scale: parseInt(model.scale)
		};
		
		var topWindowSize = po.evalTopWindowSize();
		
		if(isNaN(model.width))
			model.width = topWindowSize.width;
		if(isNaN(model.height))
			model.height = topWindowSize.height;
		if(isNaN(model.scale))
			model.scale = "auto";
		
		var tabPanel = po.elementOfId(tab.id);
		var visualEditorIfm = po.elementOfId(po.resVisualEditorEleId(tab), tabPanel);
		visualEditorIfm.data("dashboardSizeModel", model);
		
		visualEditorIfm.css("width", model.width);
		visualEditorIfm.css("height", model.height);
		
		var cssScale = model.scale;
		
		//设置可视编辑iframe的尺寸，使其适配父元素尺寸而不会出现滚动条
		if(cssScale == "auto")
		{
			var visualEditorIfmWrapper = visualEditorIfm.parent();
			var ww = visualEditorIfmWrapper.innerWidth(), wh = visualEditorIfmWrapper.innerHeight();
			
			//下面的计算只有iframe在iframeWrapper中是绝对定位的才准确
			var rightGap = 10, bottomGap = 20;
			var ileft = parseInt(visualEditorIfm.css("left")), itop = parseInt(visualEditorIfm.css("top"));
			ww = ww - ileft - rightGap;
			wh = wh - itop - bottomGap;
			
			if(model.width <= ww && model.height <= wh)
				cssScale = 1;
			else
				cssScale = Math.min(ww/model.width, wh/model.height);
		}
		else
			cssScale = cssScale/100;
		
		visualEditorIfm.css("transform-origin", "0 0");
		visualEditorIfm.css("transform", "scale("+cssScale+")");
		
		return true;
	};
	
	//设置可视编辑iframe的尺寸，使其适配父元素尺寸而不会出现滚动条
	po.setVisualEditorIframeScale = function(iframe, scale)
	{
		iframe = $(iframe);
		
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
	
	po.buildTplVisualInsertMenuItems = function(insertType, parentLabelPath)
	{
		var items =
		[
			{
				label: "<@spring.message code='chart' />",
				class: "for-open-chart-panel",
				insertType: insertType,
				parentLabelPath: parentLabelPath,
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
				parentLabelPath: parentLabelPath,
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
				label: "<@spring.message code='flexLayout' />",
				insertType: insertType,
				class: "ve-panel-show-control flexLayoutShown",
				parentLabelPath: parentLabelPath,
				command: function()
				{
					po.veQuickExecuteMenuItem(this);
					
					var dashboardEditor = po.visualDashboardEditorByTab();
					if(dashboardEditor)
					{
						po.veCurrentInsertType = this.insertType;
						var showFillParent = dashboardEditor.canInsertFillParentFlexLayout(this.insertType);
						po.showVeFlexLayoutPanel(showFillParent);
					}
				}
			},
			{
				label: "<@spring.message code='divElement' />",
				insertType: insertType,
				parentLabelPath: parentLabelPath,
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
				label: "<@spring.message code='titleElement' />",
				insertType: insertType,
				class: "ve-panel-show-control hxtitleShown",
				parentLabelPath: parentLabelPath,
				command: function()
				{
					po.veQuickExecuteMenuItem(this);
					
					var dashboardEditor = po.visualDashboardEditorByTab();
					if(dashboardEditor)
					{
						po.veCurrentInsertType = this.insertType;
						po.showVeHxtitlePanel(function(model)
						{
							return po.insertVeHxtitle(model);
						});
					}
				}
			},
			{
				label: "<@spring.message code='textElement' />",
				insertType: insertType,
				class: "ve-panel-show-control textElementShown",
				parentLabelPath: parentLabelPath,
				command: function()
				{
					po.veQuickExecuteMenuItem(this);
					
					var dashboardEditor = po.visualDashboardEditorByTab();
					if(dashboardEditor)
					{
						po.veCurrentInsertType = this.insertType;
						po.showVeTextElementPanel(function(model)
						{
							return po.insertVeTextElement(model);
						});
					}
				}
			},
			{
				label: "<@spring.message code='image' />",
				insertType: insertType,
				class: "ve-panel-show-control imageShown",
				parentLabelPath: parentLabelPath,
				command: function()
				{
					po.veQuickExecuteMenuItem(this);
					
					var dashboardEditor = po.visualDashboardEditorByTab();
					if(dashboardEditor)
					{
						po.veCurrentInsertType = this.insertType;
						po.showVeImagePanel(function(model)
						{
							return po.insertVeImage(model);
						});
					}
				}
			},
			{
				label: "<@spring.message code='hyperlink' />",
				insertType: insertType,
				class: "ve-panel-show-control hyperlinkShown",
				parentLabelPath: parentLabelPath,
				command: function()
				{
					po.veQuickExecuteMenuItem(this);
					
					var dashboardEditor = po.visualDashboardEditorByTab();
					if(dashboardEditor)
					{
						po.veCurrentInsertType = this.insertType;
						po.showVeHyperlinkPanel(function(model)
						{
							return po.insertVeHyperlink(model);
						});
					}
				}
			},
			{
				label: "<@spring.message code='video' />",
				insertType: insertType,
				class: "ve-panel-show-control videoShown",
				parentLabelPath: parentLabelPath,
				command: function()
				{
					po.veQuickExecuteMenuItem(this);
					
					var dashboardEditor = po.visualDashboardEditorByTab();
					if(dashboardEditor)
					{
						po.veCurrentInsertType = this.insertType;
						po.showVeVideoPanel(function(model)
						{
							return po.insertVeVideo(model);
						});
					}
				}
			}
		];
		
		return items;
	};
	
	po.setupResourceEditor = function()
	{
		po.setupResourceEditorForms();
		
		var fm = po.vueFormModel();
		var pm = po.vuePageModel();
		
		po.vuePageModel(
		{
			templateEditModeOptions:
			[
				{ name: "<@spring.message code='dashboard.templateEditMode.code' />", value: "code" },
				{ name: "<@spring.message code='dashboard.templateEditMode.visual' />", value: "visual" }
			],
			resContentTabs:
			{
				items: [],
				activeIndex: 0
			},
			resContentTabMenuItems:
			[
				{
					label: "<@spring.message code='close' />",
					command: function()
					{
						po.tabviewClose(po.vuePageModel().resContentTabs, po.resourceContentTabMenuTargetId);
					}
				},
				{
					label: "<@spring.message code='closeOther' />",
					command: function()
					{
						po.tabviewCloseOther(po.vuePageModel().resContentTabs, po.resourceContentTabMenuTargetId);
					}
				},
				{
					label: "<@spring.message code='closeRight' />",
					command: function()
					{
						po.tabviewCloseRight(po.vuePageModel().resContentTabs, po.resourceContentTabMenuTargetId);
					}
				},
				{
					label: "<@spring.message code='closeLeft' />",
					command: function()
					{
						po.tabviewCloseLeft(po.vuePageModel().resContentTabs, po.resourceContentTabMenuTargetId);
					}
				},
				{
					label: "<@spring.message code='closeAll' />",
					command: function()
					{
						po.tabviewCloseAll(po.vuePageModel().resContentTabs);
					}
				}
			],
			codeEditMenuItems:
			[
				{
					label: "<@spring.message code='save' />",
					command: function(e)
					{
						var info = po.getEditResInfo(pm.resContentTabs.activeIndex);
						po.saveResInfo(info);
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
							parentLabelPath: "<@spring.message code='insert' />",
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
						{
							label: "<@spring.message code='outerInsertAfter' />",
							items: po.buildTplVisualInsertMenuItems("after", ["<@spring.message code='insert' />", "<@spring.message code='outerInsertAfter' />"])
						},
						{
							label: "<@spring.message code='outerInsertBefore' />",
							items: po.buildTplVisualInsertMenuItems("before", ["<@spring.message code='insert' />", "<@spring.message code='outerInsertBefore' />"])
						},
						{
							label: "<@spring.message code='innerInsertAfter' />",
							items: po.buildTplVisualInsertMenuItems("append", ["<@spring.message code='insert' />", "<@spring.message code='innerInsertAfter' />"])
						},
						{
							label: "<@spring.message code='innerInsertBefore' />",
							items: po.buildTplVisualInsertMenuItems("prepend", ["<@spring.message code='insert' />", "<@spring.message code='innerInsertBefore' />"])
						}
					]
				},
				{
					label: "<@spring.message code='edit' />",
					items:
					[
						{
							label: "<@spring.message code='globalStyle' />",
							class: "ve-panel-show-control styleShown",
							parentLabelPath: "<@spring.message code='edit' />",
							command: function()
							{
								po.veQuickExecuteMenuItem(this);
								
								var dashboardEditor = po.visualDashboardEditorByTab();
								if(dashboardEditor)
								{
									var styleModel = dashboardEditor.getGlobalStyle();
									styleModel = po.convertToVeStyleFormModel(styleModel);
									
									po.showVeStylePanel(function(model)
									{
										return po.setVeStyle(model, true);
									},
									styleModel, this.label);
								}
							}
						},
						{
							label: "<@spring.message code='globalChartTheme' />",
							class: "ve-panel-show-control chartThemeShown",
							parentLabelPath: "<@spring.message code='edit' />",
							command: function()
							{
								po.veQuickExecuteMenuItem(this);
								
								var dashboardEditor = po.visualDashboardEditorByTab();
								if(dashboardEditor)
								{
									po.showVeChartThemePanel(function(model)
									{
										return po.setVeChartTheme(model, true);
									},
									dashboardEditor.getGlobalChartTheme(), this.label);
								}
							}
						},
						{
							label: "<@spring.message code='globalChartOptions' />",
							class: "ve-panel-show-control chartOptionsShown",
							parentLabelPath: "<@spring.message code='edit' />",
							command: function()
							{
								po.veQuickExecuteMenuItem(this);
								
								var dashboardEditor = po.visualDashboardEditorByTab();
								if(dashboardEditor)
								{
									po.showVeChartOptionsPanel(function(model)
									{
										return po.setVeChartOptions(model, true);
									},
									{ value: dashboardEditor.getGlobalChartOptions() }, true);
								}
							}
						},
						{ separator: true },
						{
							label: "<@spring.message code='style' />",
							class: "ve-panel-show-control styleShown",
							parentLabelPath: "<@spring.message code='edit' />",
							command: function()
							{
								po.veQuickExecuteMenuItem(this);
								
								var dashboardEditor = po.visualDashboardEditorByTab();
								if(dashboardEditor)
								{
									if(!dashboardEditor.checkSetElementStyle())
										return;
									
									var styleModel = dashboardEditor.getElementStyle();
									styleModel.isGridItemElement = dashboardEditor.isGridItemElement();
									styleModel.isFlexItemElement = dashboardEditor.isFlexItemElement();
									styleModel = po.convertToVeStyleFormModel(styleModel);
									
									po.showVeStylePanel(function(model)
									{
										return po.setVeStyle(model, false);
									},
									styleModel, this.label);
								}
							}
						},
						{
							label: "<@spring.message code='chartTheme' />",
							class: "ve-panel-show-control chartThemeShown",
							parentLabelPath: "<@spring.message code='edit' />",
							command: function()
							{
								po.veQuickExecuteMenuItem(this);
								
								var dashboardEditor = po.visualDashboardEditorByTab();
								if(dashboardEditor)
								{
									if(!dashboardEditor.checkSetElementChartTheme())
										return;
									
									po.showVeChartThemePanel(function(model)
									{
										return po.setVeChartTheme(model, false);
									},
									dashboardEditor.getElementChartTheme(), this.label);
								}
							}
						},
						{
							label: "<@spring.message code='chartAttribute' />",
							class: "ve-panel-show-control chartAttrValuesShown",
							parentLabelPath: "<@spring.message code='edit' />",
							command: function()
							{
								po.veQuickExecuteMenuItem(this);
								
								var dashboardEditor = po.visualDashboardEditorByTab();
								if(dashboardEditor)
								{
									if(!dashboardEditor.checkSetElementChartAttrValues())
										return;
									
									po.showVeChartAttrValuesPanel(function(model)
									{
										return po.setVeElementChartAttrValues(model);
									},
									dashboardEditor.getElementChartAttrValues(), this.label);
								}
							}
						},
						{
							label: "<@spring.message code='chartOptions' />",
							class: "ve-panel-show-control chartOptionsShown",
							parentLabelPath: "<@spring.message code='edit' />",
							command: function()
							{
								po.veQuickExecuteMenuItem(this);
								
								var dashboardEditor = po.visualDashboardEditorByTab();
								if(dashboardEditor)
								{
									if(!dashboardEditor.checkSetElementChartOptions())
										return;
									
									po.showVeChartOptionsPanel(function(model)
									{
										return po.setVeChartOptions(model, false);
									},
									{ value: dashboardEditor.getElementChartOptions() }, false);
								}
							}
						},
						{
							label: "<@spring.message code='elementAttribute' />",
							class: "ve-panel-show-control imageShown hyperlinkShown videoShown textElementShown",
							parentLabelPath: "<@spring.message code='edit' />",
							command: function()
							{
								po.veQuickExecuteMenuItem(this);
								
								var dashboardEditor = po.visualDashboardEditorByTab();
								if(dashboardEditor)
								{
									if(dashboardEditor.isImage())
									{
										po.showVeImagePanel(function(model)
										{
											return po.updateVeImage(model);
										},
										dashboardEditor.getImageAttr());
									}
									else if(dashboardEditor.isHyperlink())
									{
										po.showVeHyperlinkPanel(function(model)
										{
											return po.updateVeHyperlink(model);
										},
										dashboardEditor.getHyperlinkAttr());
									}
									else if(dashboardEditor.isVideo())
									{
										po.showVeVideoPanel(function(model)
										{
											return po.updateVeVideo(model);
										},
										dashboardEditor.getVideoAttr());
									}
									else if(dashboardEditor.isLabel())
									{
										po.showVeTextElementPanel(function(model)
										{
											return po.updateVeTextElement(model);
										},
										dashboardEditor.getLabelAttr());
									}
									else
										$.tipInfo("<@spring.message code='dashboard.opt.edit.eleAttr.eleRequired' />");
								}
							}
						},
						{
							label: "<@spring.message code='textContent' />",
							class: "ve-panel-show-control textElementShown",
							parentLabelPath: "<@spring.message code='edit' />",
							command: function()
							{
								po.veQuickExecuteMenuItem(this);
								
								var dashboardEditor = po.visualDashboardEditorByTab();
								if(dashboardEditor)
								{
									if(!dashboardEditor.checkSetElementText())
										return;
									
									po.showVeTextElementPanel(function(model)
									{
										return po.updateVeTextElement(model);
									},
									{ content: dashboardEditor.getElementText() });
								}
							}
						}
					]
				},
				{
					label: "<@spring.message code='delete' />",
					items:
					[
						{
							label: "<@spring.message code='deleteElement' />",
							class: "p-error",
							command: function()
							{
								var dashboardEditor = po.visualDashboardEditorByTab();
								if(dashboardEditor && dashboardEditor.checkDeleteElement())
								{
									dashboardEditor.deleteElement();
								}
							}
						},
						{ separator: true },
						{
							label: "<@spring.message code='unbindChart' />",
							class: "p-error",
							command: function()
							{
								var dashboardEditor = po.visualDashboardEditorByTab();
								if(dashboardEditor && dashboardEditor.checkUnbindChart())
								{
									dashboardEditor.unbindChart();
								}
							}
						}
					]
				},
				{
					label: "<@spring.message code='save' />",
					command: function(e)
					{
						var info = po.getEditResInfo(pm.resContentTabs.activeIndex);
						po.saveResInfo(info);
					}
				},
				{
					label: "<@spring.message code='more' />",
					items:
					[
						{
							label: "<@spring.message code='dashboardSize' />",
							class: "ve-panel-show-control dashboardSizeShown",
							command: function()
							{
								var tab = po.getCurrentEditTab();
								var tabPanel = po.elementOfId(tab.id);
								var visualEditorIfm = po.elementOfId(po.resVisualEditorEleId(tab), tabPanel);
								po.showVeDashboardSizePanel(visualEditorIfm.data("dashboardSizeModel"));
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
								po.veRefresh();
							}
						}
					]
				}
			],
			quickExecuteMenuItem: null,
			quickExecuteTooltip: " ", //XXX 默认空字符串的话后续没有效果！？
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
				e.stopPropagation();
				po.vueUnref("${pid}resourceContentTabMenuEle").hide();
				
				//直接show会导致面板还停留在上一个元素上
				po.vueNextTick(function()
				{
					po.resourceContentTabMenuTargetId = tab.id;
					po.vueUnref("${pid}resourceContentTabMenuEle").show(e);
				});
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
			},
			
			formatVeElePathDisplayName: function(elePath)
			{
				return $.truncateIf(elePath.displayName, "...", elePath.tagName.length+17);
			},
			
			onVeSelectByElePath: function(e, elePath)
			{
				var dashboardEditor = po.visualDashboardEditorByTab();
				if(dashboardEditor)
					dashboardEditor.selectElement(elePath.visualEditId);
			}
		});
		
		po.vueRef("${pid}resourceContentTabMenuEle", null);
		po.vueRef("${pid}chartPanelEle", null);
		
		//po.showResContentTab()里不能获取到创建的DOM元素，所以采用此方案
		po.vueWatch(pm.resContentTabs, function(oldVal, newVal)
		{
			var newItems = newVal.items;
			var newActiveIndex = newVal.activeIndex;
			var newActiveTab = newItems[newActiveIndex];
			
			if(newActiveTab)
			{
				po.vueNextTick(function()
				{
					po.loadResContentIfNon(newActiveTab);
				});
			}
		});
		
		po.beforeClose("closeSelectChartDialog", function()
		{
			po.closeSelectChartDialog();
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
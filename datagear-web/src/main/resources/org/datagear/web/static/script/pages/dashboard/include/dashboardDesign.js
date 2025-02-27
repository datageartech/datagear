/*
 * Copyright 2018-present datagear.tech
 *
 * This file is part of DataGear.
 *
 * DataGear is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * DataGear is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with DataGear.
 * If not, see <https://www.gnu.org/licenses/>.
 */

/**
 * 看板设计页面JS对象函数集。
 * 直接在页面中定义这些函数导致页面尺寸过大加载较慢。
 * 
 * 依赖:
 * jquery.js
 */

(function($, undefined)
{

//填充看板设计页面编辑器JS对象
//依赖：
//dashboard_design_editor.ftl
//dashboard_design_editor_forms.ftl
$.inflateDashboardDesignEditor = function(po)
{
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
	
	po.getCurrentAnalysisProjectId = function()
	{
		var fm = po.vueFormModel();
		return (fm.analysisProject ? fm.analysisProject.id : null);
	};
	
	po.addCurrentAnalysisProjectIdParam = function(url)
	{
		var paramValue = po.getCurrentAnalysisProjectId();
		
		if(paramValue)
			return $.addParam(url, po.currentAnalysisProjectIdParam, paramValue);
		else
			return url;
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
	
	po.codeEditorInstance = function(codeEditorEle, codeEditor)
	{
		if(codeEditor === undefined)
			return codeEditorEle.data("codeEditorInstance");
		else
			codeEditorEle.data("codeEditorInstance", codeEditor);
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
			
			var fm = po.vueFormModel();
			var id = fm.id;
			
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
					po.updateResSavedChangeFlag(tab);
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
		var codeEditor = po.codeEditorInstance(codeEditorEle);
		
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
			po.codeEditorInstance(codeEditorEle, codeEditor);
			
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
				
				po.setVeDashboardSize(tab, {});
			}
		}
		else
		{
			po.setCodeText(codeEditor, content);
		}
		
		if(po.focusOnEditorAfterSetContent(tab))
			codeEditor.focus();
	};
	
	po.focusOnEditorAfterSetContent = function(tab)
	{
		var fm = po.vueFormModel();
		var pm = po.vuePageModel();
		var items = pm.resContentTabs.items;
		
		//添加页面初始不设编辑器焦点
		if(!fm.name && items.length < 2)
			return false;
		else
			return true;
	};
	
	po.updateResSavedChangeFlag = function(tab)
	{
		tab.savedChangeFlag = po.getLatestResChangeFlag(tab);
	};
	
	po.updateAllResSavedChangeFlags = function()
	{
		var pm = po.vuePageModel();
		var items = pm.resContentTabs.items;
		
		$.each(items, function(idx, item)
		{
			po.updateResSavedChangeFlag(item);
		});
	};
	
	po.getLatestResChangeFlag = function(tab)
	{
		var tabPanel = po.elementOfId(tab.id);
		var codeEditorEle = po.elementOfId(po.resCodeEditorEleId(tab), tabPanel);
		var codeEditor = po.codeEditorInstance(codeEditorEle);
		var visualEditorIfm = po.elementOfId(po.resVisualEditorEleId(tab), tabPanel);
		var dashboardEditor = po.visualDashboardEditorByIframe(visualEditorIfm);
		
		var re =
		{
			code: codeEditor.changeGeneration(),
			ve: (dashboardEditor ? dashboardEditor.changeFlag() : po.dftVeLoadedChangeFlag)
		};
		
		return re;
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
				var codeEditor = po.codeEditorInstance(editorEle);
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
	
	po.saveResInfo = function(tab)
	{
		var resInfo = po.getEditResInfo(tab);
		
		if(!resInfo)
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
			po.updateResSavedChangeFlag(tab);
			
			if(response.data.templatesChanged)
				po.updateTemplateList(response.data.templates);
			
			if(!response.data.resourceExists)
				po.refreshLocalRes();
		});
	};
	
	po.confirmCloseUnSavedRes = function(tabIndexes, acceptHandler)
	{
		var unsaveds = po.getUnSavedResTabs(tabIndexes);
		
		if(unsaveds.length == 0)
		{
			acceptHandler();
		}
		else
		{
			var msg = $.validator.format(po.i18n.confirmCloseWithUnsaved, po.getTabsResNameStr(unsaveds));
			po.confirm({ message: msg, accept: acceptHandler });
		}
	};
	
	po.getTabsResNameStr = function(tabs)
	{
		var re = [];
		
		for(var i=0; i<tabs.length; i++)
		{
			re.push(tabs[i].resName);
		}
		
		return re.join(", ");
	};
	
	po.getUnSavedResTabs = function(tabIndexes)
	{
		var pm = po.vuePageModel();
		var items = pm.resContentTabs.items;
		
		//默认全部
		if(tabIndexes == null)
		{
			tabIndexes = [];
			for(var i=0; i<items.length; i++)
			{
				tabIndexes.push(i);
			}
		}
		
		var re = [];
		
		for(var i=0; i<tabIndexes.length; i++)
		{
			var tab = items[tabIndexes[i]];
			
			if(!tab)
				continue;
			
			var saved = (tab.savedChangeFlag || {});
			var latest = po.getLatestResChangeFlag(tab);
			
			if(saved.code != latest.code || saved.ve != latest.ve)
			{
				re.push(tab);
			}
		}
		
		return re;
	};
	
	po.searchInCodeEditor = function(tab)
	{
		var text = tab.searchCodeKeyword;
		
		if(!text)
			return;
		
		var codeEditorEle = po.elementOfId(po.resCodeEditorEleId(tab));
		var codeEditor = po.codeEditorInstance(codeEditorEle);
		
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
	
	//可视模式加载页面后的dashboardEditor.changeFlag()初始值值，
	//因为加载是异步执行，无法同步获取，所以这里明确定义
	po.dftVeLoadedChangeFlag = 1;
	
	po.handleChangeEditMode = function(tab)
	{
		var tabPanel = po.elementOfId(tab.id);
		var codeEditorEle = po.elementOfId(po.resCodeEditorEleId(tab), tabPanel);
		var codeEditorWrapper = codeEditorEle.parent();
		var codeEditor = po.codeEditorInstance(codeEditorEle);
		var visualEditorIfm = po.elementOfId(po.resVisualEditorEleId(tab), tabPanel);
		var visualEditorIfmWrapper = visualEditorIfm.parent();
		var visualEditorWrapper = visualEditorIfmWrapper.parent();
		
		if(tab.editMode == "code")
		{
			var veChangeFlag = codeEditorEle.data("veChangeFlag");
			var dashboardEditor = po.visualDashboardEditorByIframe(visualEditorIfm);
			
			//有修改
			if(veChangeFlag != null && dashboardEditor && dashboardEditor.isChanged(veChangeFlag))
			{
				po.setCodeText(codeEditor, dashboardEditor.editedHtml());
				
				visualEditorIfmWrapper.data("codeChangeFlag", codeEditor.changeGeneration());
				codeEditorEle.data("veChangeFlag", dashboardEditor.changeFlag());
			}
			
			codeEditorWrapper.removeClass("opacity-hide");
			visualEditorWrapper.addClass("opacity-hide");
		}
		else
		{
			var codeChangeFlag = visualEditorIfmWrapper.data("codeChangeFlag");
			
			//有修改
			if(codeChangeFlag == null || !codeEditor.isClean(codeChangeFlag))
			{
				tab.veElementPath = [];
				
				//清空iframe后再显示，防止闪屏
				po.iframeDocument(visualEditorIfm).write("");
				
				visualEditorIfmWrapper.data("codeChangeFlag", codeEditor.changeGeneration());
				codeEditorEle.data("veChangeFlag", po.dftVeLoadedChangeFlag);
				
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
		
		var ifmWindow = po.iframeWindow(visualEditorIfm);
		var dashboardEditor = (ifmWindow && ifmWindow.dashboardFactory ? ifmWindow.dashboardFactory.dashboardEditor : null);
		
		if(dashboardEditor && !dashboardEditor._OVERWRITE_BY_CONTEXT)
		{
			dashboardEditor._OVERWRITE_BY_CONTEXT = true;
			po.extendVeDashboardEditor(tab, visualEditorIfm, ifmWindow, dashboardEditor);
		}
		
		if(dashboardEditor)
		{
			dashboardEditor.enableElementBoundary(visualEditorIfm.data("veEnableElementBoundary"));
			dashboardEditor.changeFlag(visualEditorIfm.data("veChangeFlag"));
			//XXX 这里无法恢复选中状态，因为每次重新加载后可视编辑ID会重新生成
		}
	};
	
	po.extendVeDashboardEditor = function(veTab, veIframe, ifmWindow, dashboardEditor)
	{
		dashboardEditor.i18n = po.i18n;
		
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
			veIframe.data("veEditedHtml", this.editedHtml());
			veIframe.data("veEnableElementBoundary", this.enableElementBoundary());
			veIframe.data("veChangeFlag", this.changeFlag());
		};
		
		dashboardEditor.defaultInsertChartEleStyle = po.defaultInsertChartEleStyle;
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
		var form = po.elementOfPidPrefix("visualEditorLoadForm");
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
				var myDialog = po.selectChartDialog();
				var handler = myDialog.data("dashboardSelectChartHandler");
				
				if(handler)
					handler(chartWidgets);
				
				po.hideSelectChartDialog();
				return false;
			},
			{
				modal: true,
				closable: false,
				styleClass: "dashboard-select-chart-wrapper table-sm",
				templateHeader: "<span class='p-dialog-title'>"+po.i18n.chart+" - "+po.i18n.select+"</span>"
								+"<div class='dialog-btns p-dialog-header-icons'>"
								+"	<p-button type='button' icon='pi pi-times' class='p-dialog-header-icon p-dialog-header-close p-link' @click='onCustomHide'></p-button>"
								+"</div>",
				width: "50vw",
				position: "right",
				onShow: function(dialog)
				{
					dialog.data("dashboardSelectChartHandler", selectHandler);
				},
				onSetup: function(setup)
				{
					setup.onCustomHide = function()
					{
						po.hideSelectChartDialog();
					};
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
		var codeEditor = po.codeEditorInstance(codeEditorEle);
		
		var doc = codeEditor.getDoc();
		var cursor = doc.getCursor();
		var code = "";
		
		var chartId = chartWidgets[0].id;
		var text = po.getTemplatePrevTagText(codeEditor, cursor);
		var textNext = po.getTemplateNextText(codeEditor, cursor);
		
		// =
		if(/=\s*$/g.test(text))
			code = "\"" + chartId + "\"";
		// ="... 或 ='...
		else if(/=\s*['"][^'"]*$/g.test(text))
			code = chartId;
		// <...
		else if(/<[^>]*$/g.test(text))
			code = " dg-chart-widget=\""+chartId+"\"";
		// "..." 或 '...'
		else if(/['"][^'"]*$/g.test(text) && /^[^'"]*['"]/g.test(textNext))
			code = chartId;
		// >...
		else
		{
			for(var i=0; i<chartWidgets.length; i++)
				code += "<div style=\""+po.defaultInsertChartEleStyle+"\" dg-chart-widget=\""+chartWidgets[i].id+"\"><!--"+chartWidgets[i].name+"--></div>\n";
		}
		
		po.insertCodeText(codeEditor, cursor, code);
		codeEditor.focus();
		
		po.tipChartPermissionIfNeed(chartWidgets);
	};
	
	po.getTemplatePrevTagText = function(codeEditor, cursor)
	{
		var doc = codeEditor.getDoc();
		
		var text = (doc.getLine(cursor.line).substring(0, cursor.ch) || "");
		
		//反向查找直到'>'或'<'
		var prevRow = cursor.line;
		while(text.length <= 500 && !(/[<>]/g.test(text)) && (prevRow--) >= 0)
			text = doc.getLine(prevRow) + text;
		
		var idx = -1;
		for(var i=text.length-1; i>=0; i--)
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
	
	po.getTemplateNextText = function(codeEditor, cursor)
	{
		var doc = codeEditor.getDoc();
		var text = (doc.getLine(cursor.line).substring(cursor.ch) || "");
		
		return text;
	};
	
	//如果插入了看板创建用户没有权限的图表，这里给出提示
	po.tipChartPermissionIfNeed = function(chartWidgets)
	{
		if(!chartWidgets || chartWidgets.length == 0)
			return;
		
		var fm = po.vueFormModel();
		if(fm.createUser && fm.createUser.id && po.currentUserId != fm.createUser.id)
		{
			var chartWidgetIds = $.propertyValue(chartWidgets, "id");
			
			po.ajaxJson("/chart/hasReadPermission",
			{
				data: { userId: fm.createUser.id, chartWidgetIds: chartWidgetIds },
				success: function(response)
				{
					var msg = po.i18n.insertNoPermissionChart;
					for(var i=0; i<chartWidgets.length; i++)
					{
						if(!response[i])
						{
							var cw = chartWidgets[i];
							$.tipWarn({ summary: $.validator.format(msg, cw.name, cw.id), life: 5000});
						}
					}
				}
			});
		}
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
		
		$.executeSilently(function()
		{
			dashboardEditor.bindChart(chartWidgets ? chartWidgets[0] : null);
		});
		
		po.tipChartPermissionIfNeed(chartWidgets);
	};
	
	po.insertVeChart = function(chartWidgets)
	{
		var dashboardEditor = po.visualDashboardEditorByTab();
		var insertType = po.veCurrentInsertType;
		
		if(!dashboardEditor || !insertType || !dashboardEditor.checkInsertChart(insertType))
			return false;
		
		$.executeSilently(function()
		{
			dashboardEditor.insertChart(chartWidgets, po.veCurrentInsertType);
		});
		
		po.tipChartPermissionIfNeed(chartWidgets);
	};
	
	po.insertVeGridLayout = function(model)
	{
		var dashboardEditor = po.visualDashboardEditorByTab();
		var insertType = po.veCurrentInsertType;
		
		if(!dashboardEditor || !insertType || !dashboardEditor.checkInsertGridLayout(insertType))
			return false;
		
		$.executeSilently(function()
		{
			dashboardEditor.insertGridLayout(model, po.veCurrentInsertType);
		});
	};

	po.insertVeFlexLayout = function(model)
	{
		var dashboardEditor = po.visualDashboardEditorByTab();
		var insertType = po.veCurrentInsertType;
		
		if(!dashboardEditor || !insertType || !dashboardEditor.checkInsertFlexLayout(insertType))
			return false;
		
		$.executeSilently(function()
		{
			dashboardEditor.insertFlexLayout(model, po.veCurrentInsertType);
		});
	};

	po.insertVeHxtitle = function(model)
	{
		var dashboardEditor = po.visualDashboardEditorByTab();
		var insertType = po.veCurrentInsertType;
		
		if(!dashboardEditor || !insertType || !dashboardEditor.checkInsertHxtitle(insertType))
			return false;
		
		$.executeSilently(function()
		{
			dashboardEditor.insertHxtitle(model, po.veCurrentInsertType);
		});
	};

	po.insertVeTextElement = function(model)
	{
		var dashboardEditor = po.visualDashboardEditorByTab();
		var insertType = po.veCurrentInsertType;
		
		if(!dashboardEditor || !insertType || !dashboardEditor.checkInsertLabel(insertType))
			return false;
		
		$.executeSilently(function()
		{
			dashboardEditor.insertLabel(model, po.veCurrentInsertType);
		});
	};

	po.updateVeTextElement = function(model)
	{
		var dashboardEditor = po.visualDashboardEditorByTab();
		
		if(!dashboardEditor || !dashboardEditor.checkSetElementText())
			return false;
		
		$.executeSilently(function()
		{
			dashboardEditor.setElementText(model.content);
		});
	};
	
	po.insertVeImage = function(model)
	{
		var dashboardEditor = po.visualDashboardEditorByTab();
		var insertType = po.veCurrentInsertType;
		
		if(!dashboardEditor || !insertType || !dashboardEditor.checkInsertImage(insertType))
			return false;
		
		$.executeSilently(function()
		{
			dashboardEditor.insertImage(model, po.veCurrentInsertType);
		});
	};

	po.updateVeImage = function(model)
	{
		var dashboardEditor = po.visualDashboardEditorByTab();
		
		if(!dashboardEditor)
			return false;
		
		$.executeSilently(function()
		{
			dashboardEditor.setImageAttr(model);
		});
	};
	
	po.insertVeHyperlink = function(model)
	{
		var dashboardEditor = po.visualDashboardEditorByTab();
		var insertType = po.veCurrentInsertType;
		
		if(!dashboardEditor || !insertType || !dashboardEditor.checkInsertHyperlink(insertType))
			return false;
		
		$.executeSilently(function()
		{
			dashboardEditor.insertHyperlink(model, po.veCurrentInsertType);
		});
	};

	po.updateVeHyperlink = function(model)
	{
		var dashboardEditor = po.visualDashboardEditorByTab();
		
		if(!dashboardEditor)
			return false;
		
		$.executeSilently(function()
		{
			dashboardEditor.setHyperlinkAttr(model);
		});
	};
	
	po.insertVeVideo = function(model)
	{
		var dashboardEditor = po.visualDashboardEditorByTab();
		var insertType = po.veCurrentInsertType;
		
		if(!dashboardEditor || !insertType || !dashboardEditor.checkInsertVideo(insertType))
			return false;
		
		$.executeSilently(function()
		{
			dashboardEditor.insertVideo(model, po.veCurrentInsertType);
		});
	};

	po.updateVeVideo = function(model)
	{
		var dashboardEditor = po.visualDashboardEditorByTab();
		
		if(!dashboardEditor)
			return false;
		
		$.executeSilently(function()
		{
			dashboardEditor.setVideoAttr(model);
		});
	};
	
	po.setVeChartOptions = function(model, global)
	{
		var dashboardEditor = po.visualDashboardEditorByTab();
		
		if(!dashboardEditor)
			return false;
		
		$.executeSilently(function()
		{
			if(global)
				dashboardEditor.setGlobalChartOptions(model.value);
			else
				dashboardEditor.setElementChartOptions(model.value);
		});
	};

	po.setVeChartTheme = function(model, global)
	{
		var dashboardEditor = po.visualDashboardEditorByTab();
		
		if(!dashboardEditor)
			return false;
		
		$.executeSilently(function()
		{
			if(global)
				dashboardEditor.setGlobalChartTheme(model);
			else
				dashboardEditor.setElementChartTheme(model);
		});
	};
	
	po.setVeElementChartAttrValues = function(model)
	{
		var dashboardEditor = po.visualDashboardEditorByTab();
		
		if(!dashboardEditor)
			return false;
		
		$.executeSilently(function()
		{
			dashboardEditor.setElementChartAttrValues(model);
		});
	};
	
	po.setVeStyle = function(model, global)
	{
		var dashboardEditor = po.visualDashboardEditorByTab();
		
		if(!dashboardEditor)
			return false;
		
		$.executeSilently(function()
		{
			if(global)
				dashboardEditor.setGlobalStyle(model);
			else
				dashboardEditor.setElementStyle(model);
		});
	};
	
	po.setVeEleId = function(model)
	{
		var dashboardEditor = po.visualDashboardEditorByTab();
		
		if(!dashboardEditor)
			return false;
		
		$.executeSilently(function()
		{
			dashboardEditor.setElementAttr("id", model.value);
		});
	};
	
	po.insertVeIframe = function(model)
	{
		var dashboardEditor = po.visualDashboardEditorByTab();
		var insertType = po.veCurrentInsertType;
		
		if(!dashboardEditor || !insertType || !dashboardEditor.checkInsertIframe(insertType))
			return false;
		
		$.executeSilently(function()
		{
			dashboardEditor.insertIframe(model, po.veCurrentInsertType);
		});
	};
	
	po.updateVeIframe = function(model)
	{
		var dashboardEditor = po.visualDashboardEditorByTab();
		
		if(!dashboardEditor)
			return false;
		
		$.executeSilently(function()
		{
			dashboardEditor.setIframeAttr(model);
		});
	};
	
	po.veQuickExecute = function(tab)
	{
		var pm = po.vuePageModel();
		
		if(pm.quickExecuteMenuItem && pm.quickExecuteMenuItem.commandExec)
			pm.quickExecuteMenuItem.commandExec();
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
			var codeEditor = po.codeEditorInstance(codeEditorEle);
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
	
	po.openAddChartPanel = function(callback)
	{
		po.open(po.addCurrentAnalysisProjectIdParam("/chart/add?disableSaveShow=true"),
		{
			width: "70vw",
			pageParam:
			{
				submitSuccess: function(chartWidget)
				{
					if(!chartWidget)
						return;
					
					callback(chartWidget);
				}
			}
		});
	};
	
	po.veEditElementSetting = function(dashboardEditor)
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
		else if(dashboardEditor.isIframe())
		{
			po.showVeIframePanel(function(model)
			{
				return po.updateVeIframe(model);
			},
			dashboardEditor.getIframeAttr());
		}
		else
			$.tipInfo(po.i18n["dashboard.opt.edit.eleAttr.eleRequired"]);
	};
	
	po.buildTplVisualInsertMenuItems = function(insertType, parentLabelPath)
	{
		var items =
		[
			{
				label: po.i18n.chartTipSelect,
				class: "for-open-chart-panel",
				insertType: insertType,
				parentLabelPath: parentLabelPath,
				command: function(e)
				{
					e.item.commandExec();
				},
				commandExec: function()
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
			{
				label: po.i18n.chartTipCreate,
				insertType: insertType,
				parentLabelPath: parentLabelPath,
				visible: po.enableInsertNewChart,
				command: function(e)
				{
					e.item.commandExec();
				},
				commandExec: function()
				{
					po.veQuickExecuteMenuItem(this);
					
					var dashboardEditor = po.visualDashboardEditorByTab();
					if(dashboardEditor)
					{
						if(!dashboardEditor.checkInsertChart(this.insertType))
							return;
						
						po.veCurrentInsertType = this.insertType;
						po.openAddChartPanel(function(chartWidget)
						{
							chartWidget = [ chartWidget ];
							po.insertVeChart(chartWidget);
						});
					}
				}
			},
			{ separator: true },
			{
				label: po.i18n.gridLayout,
				insertType: insertType,
				class: "ve-panel-show-control gridLayoutShown",
				parentLabelPath: parentLabelPath,
				command: function(e)
				{
					e.item.commandExec();
				},
				commandExec: function()
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
				label: po.i18n.flexLayout,
				insertType: insertType,
				class: "ve-panel-show-control flexLayoutShown",
				parentLabelPath: parentLabelPath,
				command: function(e)
				{
					e.item.commandExec();
				},
				commandExec: function()
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
				label: po.i18n.divElement + " <div>",
				insertType: insertType,
				parentLabelPath: parentLabelPath,
				command: function(e)
				{
					e.item.commandExec();
				},
				commandExec: function()
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
				label: po.i18n.titleElement + " <h1>-<h6>",
				insertType: insertType,
				class: "ve-panel-show-control hxtitleShown",
				parentLabelPath: parentLabelPath,
				command: function(e)
				{
					e.item.commandExec();
				},
				commandExec: function()
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
				label: po.i18n.textElement + " <label>",
				insertType: insertType,
				class: "ve-panel-show-control textElementShown",
				parentLabelPath: parentLabelPath,
				command: function(e)
				{
					e.item.commandExec();
				},
				commandExec: function()
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
				label: po.i18n.image + " <img>",
				insertType: insertType,
				class: "ve-panel-show-control imageShown",
				parentLabelPath: parentLabelPath,
				command: function(e)
				{
					e.item.commandExec();
				},
				commandExec: function()
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
				label: po.i18n.hyperlink + " <a>",
				insertType: insertType,
				class: "ve-panel-show-control hyperlinkShown",
				parentLabelPath: parentLabelPath,
				command: function(e)
				{
					e.item.commandExec();
				},
				commandExec: function()
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
				label: po.i18n.video + " <video>",
				insertType: insertType,
				class: "ve-panel-show-control videoShown",
				parentLabelPath: parentLabelPath,
				command: function(e)
				{
					e.item.commandExec();
				},
				commandExec: function()
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
			},
			{
				label: po.i18n.iframe + " <iframe>",
				insertType: insertType,
				class: "ve-panel-show-control iframeShown",
				parentLabelPath: parentLabelPath,
				command: function(e)
				{
					e.item.commandExec();
				},
				commandExec: function()
				{
					po.veQuickExecuteMenuItem(this);
					
					var dashboardEditor = po.visualDashboardEditorByTab();
					if(dashboardEditor)
					{
						po.veCurrentInsertType = this.insertType;
						po.showVeIframePanel(function(model)
						{
							return po.insertVeIframe(model);
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
				{ name: po.i18n["dashboard.templateEditMode.code"], value: "code" },
				{ name: po.i18n["dashboard.templateEditMode.visual"], value: "visual" }
			],
			enableInsertNewChart: po.enableInsertNewChart,
			resContentTabs:
			{
				items: [],
				activeIndex: 0
			},
			resContentTabMenuItems:
			[
				{
					label: po.i18n.close,
					command: function()
					{
						var closeTabIndexes = po.tabviewIndexesOfClose(pm.resContentTabs, po.resourceContentTabMenuTargetId);
						po.confirmCloseUnSavedRes(closeTabIndexes, function()
						{
							po.tabviewClose(pm.resContentTabs, po.resourceContentTabMenuTargetId);
						});
					}
				},
				{
					label: po.i18n.closeOther,
					command: function()
					{
						var closeTabIndexes = po.tabviewIndexesOfCloseOther(pm.resContentTabs, po.resourceContentTabMenuTargetId);
						po.confirmCloseUnSavedRes(closeTabIndexes, function()
						{
							po.tabviewCloseOther(pm.resContentTabs, po.resourceContentTabMenuTargetId);
						});
					}
				},
				{
					label: po.i18n.closeRight,
					command: function()
					{
						var closeTabIndexes = po.tabviewIndexesOfCloseRight(pm.resContentTabs, po.resourceContentTabMenuTargetId);
						po.confirmCloseUnSavedRes(closeTabIndexes, function()
						{
							po.tabviewCloseRight(pm.resContentTabs, po.resourceContentTabMenuTargetId);
						});
					}
				},
				{
					label: po.i18n.closeLeft,
					command: function()
					{
						var closeTabIndexes = po.tabviewIndexesOfCloseLeft(pm.resContentTabs, po.resourceContentTabMenuTargetId);
						po.confirmCloseUnSavedRes(closeTabIndexes, function()
						{
							po.tabviewCloseLeft(pm.resContentTabs, po.resourceContentTabMenuTargetId);
						});
					}
				},
				{
					label: po.i18n.closeAll,
					command: function()
					{
						po.confirmCloseUnSavedRes(null, function()
						{
							po.tabviewCloseAll(pm.resContentTabs);
						});
					}
				}
			],
			codeEditMenuItems:
			[
				{
					label: po.i18n.save,
					command: function(e)
					{
						po.saveResInfo(po.getCurrentEditTab());
					}
				}
			],
			tplVisualEditMenuItems:
			[
				{
					label: po.i18n.select,
					items:
					[
						{
							label: po.i18n.nextElement,
							command: function()
							{
								var dashboardEditor = po.visualDashboardEditorByTab();
								if(dashboardEditor)
									dashboardEditor.selectNextElement();
							}
						},
						{
							label: po.i18n.prevElement,
							command: function()
							{
								var dashboardEditor = po.visualDashboardEditorByTab();
								if(dashboardEditor)
									dashboardEditor.selectPrevElement();
							}
						},
						{
							label: po.i18n.subElement,
							command: function()
							{
								var dashboardEditor = po.visualDashboardEditorByTab();
								if(dashboardEditor)
									dashboardEditor.selectFirstChildElement();
							}
						},
						{
							label: po.i18n.parentElement,
							command: function()
							{
								var dashboardEditor = po.visualDashboardEditorByTab();
								if(dashboardEditor)
									dashboardEditor.selectParentElement();
							}
						},
						{ separator: true },
						{
							label: po.i18n.cancelSelect,
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
					label: po.i18n.insert,
					class: "ve-insert-menuitem",
					items:
					[
						{
							label: po.i18n.bindOrReplaceChartTipSelect,
							class: "for-open-chart-panel",
							parentLabelPath: po.i18n.insert,
							command: function(e)
							{
								e.item.commandExec();
							},
							commandExec: function()
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
						{
							label: po.i18n.bindOrReplaceChartTipCreate,
							parentLabelPath: po.i18n.insert,
							visible: po.enableInsertNewChart,
							command: function(e)
							{
								e.item.commandExec();
							},
							commandExec: function()
							{
								po.veQuickExecuteMenuItem(this);
								
								var dashboardEditor = po.visualDashboardEditorByTab();
								if(dashboardEditor)
								{
									if(!dashboardEditor.checkBindChart())
										return;
									
									po.openAddChartPanel(function(chartWidget)
									{
										chartWidget = [ chartWidget ];
										po.bindOrReplaceVeChart(chartWidget);
									});
								}
							}
						},
						{ separator: true },
						{
							label: po.i18n.outerInsertAfter,
							items: po.buildTplVisualInsertMenuItems("after", [po.i18n.insert, po.i18n.outerInsertAfter])
						},
						{
							label: po.i18n.outerInsertBefore,
							items: po.buildTplVisualInsertMenuItems("before", [po.i18n.insert, po.i18n.outerInsertBefore])
						},
						{
							label: po.i18n.innerInsertAfter,
							items: po.buildTplVisualInsertMenuItems("append", [po.i18n.insert, po.i18n.innerInsertAfter])
						},
						{
							label: po.i18n.innerInsertBefore,
							items: po.buildTplVisualInsertMenuItems("prepend", [po.i18n.insert, po.i18n.innerInsertBefore])
						}
					]
				},
				{
					label: po.i18n.edit,
					class: "ve-edit-menuitem",
					items:
					[
						{
							label: po.i18n.globalStyle,
							class: "ve-panel-show-control styleShown",
							parentLabelPath: po.i18n.edit,
							command: function(e)
							{
								e.item.commandExec();
							},
							commandExec: function()
							{
								po.veQuickExecuteMenuItem(this);
								
								var dashboardEditor = po.visualDashboardEditorByTab();
								if(dashboardEditor)
								{
									var styleModel = dashboardEditor.getGlobalStyle();
									styleModel.syncChartTheme = true;
									
									po.showVeStylePanel(function(model)
									{
										return po.setVeStyle(model, true);
									},
									styleModel, this.label);
								}
							}
						},
						{
							label: po.i18n.globalChartTheme,
							class: "ve-panel-show-control chartThemeShown",
							parentLabelPath: po.i18n.edit,
							command: function(e)
							{
								e.item.commandExec();
							},
							commandExec: function()
							{
								po.veQuickExecuteMenuItem(this);
								
								var dashboardEditor = po.visualDashboardEditorByTab();
								if(dashboardEditor)
								{
									po.showVeChartThemePanel(function(model)
									{
										return po.setVeChartTheme(model, true);
									},
									dashboardEditor.getGlobalChartTheme(),
									po.i18n.globalChartTheme + " dg-chart-theme");
								}
							}
						},
						{
							label: po.i18n.globalChartOptions,
							class: "ve-panel-show-control chartOptionsShown",
							parentLabelPath: po.i18n.edit,
							command: function(e)
							{
								e.item.commandExec();
							},
							commandExec: function()
							{
								po.veQuickExecuteMenuItem(this);
								
								var dashboardEditor = po.visualDashboardEditorByTab();
								if(dashboardEditor)
								{
									po.showVeChartOptionsPanel(function(model)
									{
										return po.setVeChartOptions(model, true);
									},
									{ value: dashboardEditor.getGlobalChartOptions() },
									po.i18n.globalChartOptions + " dg-chart-options",
									true);
								}
							}
						},
						{ separator: true },
						{
							label: po.i18n.style,
							class: "ve-panel-show-control styleShown",
							parentLabelPath: po.i18n.edit,
							command: function(e)
							{
								e.item.commandExec();
							},
							commandExec: function()
							{
								po.veQuickExecuteMenuItem(this);
								
								var dashboardEditor = po.visualDashboardEditorByTab();
								if(dashboardEditor)
								{
									if(!dashboardEditor.checkSetElementStyle())
										return;
									
									var styleModel = dashboardEditor.getElementStyle();
									styleModel.syncChartTheme = false;
									
									po.showVeStylePanel(function(model)
									{
										return po.setVeStyle(model, false);
									},
									styleModel, this.label);
								}
							}
						},
						{
							label: po.i18n.chart,
							items:
							[
								{
									label: po.i18n.chartTheme,
									class: "ve-panel-show-control chartThemeShown",
									parentLabelPath: [po.i18n.edit, po.i18n.chart],
									command: function(e)
									{
										e.item.commandExec();
									},
									commandExec: function()
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
											dashboardEditor.getElementChartTheme(),
											po.i18n.chartTheme + " dg-chart-theme");
										}
									}
								},
								{
									label: po.i18n.chartOptions,
									class: "ve-panel-show-control chartOptionsShown",
									parentLabelPath: [po.i18n.edit, po.i18n.chart],
									command: function(e)
									{
										e.item.commandExec();
									},
									commandExec: function()
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
											{ value: dashboardEditor.getElementChartOptions() },
											po.i18n.chartOptions + " dg-chart-options",
											false);
										}
									}
								},
								{
									label: po.i18n.chartAttribute,
									class: "ve-panel-show-control chartAttrValuesShown",
									parentLabelPath: [po.i18n.edit, po.i18n.chart],
									command: function(e)
									{
										e.item.commandExec();
									},
									commandExec: function()
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
											dashboardEditor.getElementChartAttrValues());
										}
									}
								}
							]
						},
						{
							label: po.i18n.textContent,
							class: "ve-panel-show-control textElementShown",
							parentLabelPath: po.i18n.edit,
							command: function(e)
							{
								e.item.commandExec();
							},
							commandExec: function()
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
						},
						{
							label: po.i18n.elementSetting,
							class: "ve-panel-show-control imageShown hyperlinkShown videoShown textElementShown iframeShown",
							parentLabelPath: po.i18n.edit,
							command: function(e)
							{
								e.item.commandExec();
							},
							commandExec: function()
							{
								po.veQuickExecuteMenuItem(this);
								
								var dashboardEditor = po.visualDashboardEditorByTab();
								if(dashboardEditor)
								{
									po.veEditElementSetting(dashboardEditor);
								}
							}
						},
						{
							label: po.i18n.elementId,
							class: "ve-panel-show-control eleIdShown",
							parentLabelPath: po.i18n.edit,
							command: function(e)
							{
								e.item.commandExec();
							},
							commandExec: function()
							{
								po.veQuickExecuteMenuItem(this);
								
								var dashboardEditor = po.visualDashboardEditorByTab();
								if(dashboardEditor)
								{
									if(!dashboardEditor.checkSetElementAttr())
										return;
									
									po.showVeEleIdPanel(function(model)
									{
										return po.setVeEleId(model);
									},
									{ value: dashboardEditor.getElementAttr("id") });
								}
							}
						}
					]
				},
				{
					label: po.i18n["delete"],
					items:
					[
						{
							label: po.i18n.deleteElement,
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
							label: po.i18n.unbindChart,
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
					label: po.i18n.save,
					command: function(e)
					{
						po.saveResInfo(po.getCurrentEditTab());
					}
				},
				{
					label: po.i18n.more,
					items:
					[
						{
							label: po.i18n.dashboardSize,
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
							label: po.i18n.elementBoundary,
							command: function()
							{
								var dashboardEditor = po.visualDashboardEditorByTab();
								if(dashboardEditor)
									dashboardEditor.enableElementBoundary(!dashboardEditor.enableElementBoundary());
							}
						},
						{
							label: po.i18n.refresh,
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
				po.vueUnref(po.concatPid("resourceContentTabMenuEle")).hide();
				
				//直接show会导致面板还停留在上一个元素上
				po.vueNextTick(function()
				{
					po.resourceContentTabMenuTargetId = tab.id;
					po.vueUnref(po.concatPid("resourceContentTabMenuEle")).show(e);
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
			
			onInsertCodeEditorChart: function(e, tab, create)
			{
				create = (create == null ? false: create);
				
				if(create)
				{
					po.openAddChartPanel(function(chartWidget)
					{
						chartWidget = [ chartWidget ];
						po.insertCodeEditorChart(tab, chartWidget);
					});
				}
				else
				{
					po.showSelectChartDialog(function(chartWidgets)
					{
						po.insertCodeEditorChart(tab, chartWidgets);
					});
				}
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
		
		po.vueRef(po.concatPid("resourceContentTabMenuEle"), null);
		po.vueRef(po.concatPid("chartPanelEle"), null);
		
		//po.showResContentTab()里不能获取到创建的DOM元素，所以采用此方案
		po.vueWatch(pm.resContentTabs, function(newVal, oldVal)
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
			
			//确保同时只有一个对话框显示
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
};

//填充看板设计页面编辑器表单JS对象
//依赖：
//dashboard_design_editor_forms.ftl
$.inflateDashboardDesignEditorForms = function(po)
{
	po.veDftChartThemeModel = function()
	{
		var re = { graphColors: [], graphRangeColors: [] };
		return re;
	};
	
	po.veDftGridLayoutModel = function()
	{
		var re =
		{
			fillParent: false, rowHeightDivide: "avg", rowHeights: [], rowGap: "10px",
			colWidthDivide: "avg", colWidths: [], columnGap: "10px"
		};
		return re;
	};
	
	po.veDftVideoModel = function()
	{
		var re = {};
		return re;
	};
	
	po.veDftIframeModel = function()
	{
		var re = {};
		return re;
	};
	
	po.initVePanelHelperSrc = function(form, formModel, helpValueHandler, helpTargetHandler)
	{
		$(form).on("click", ".help-src", function()
		{
			var $this = $(this);
			var helpValue = ($this.attr("help-value") || "");
			var helpTarget = po.element(".help-target", $this.closest(".field-input"));
			var targetName = helpTarget.attr("name");
			
			if(helpValueHandler != null)
			{
				var newHelpValue = helpValueHandler(form, formModel, helpValue, targetName);
				if(newHelpValue !== false)
				{
					helpValue = newHelpValue;
				}
			}
			
			if(helpTargetHandler != null)
			{
				helpTargetHandler(form, formModel, helpValue, targetName);
			}
			else
			{
				if(targetName)
					$.propPathValue(formModel, targetName, helpValue);
				else
					helpTarget.val(helpValue);
			}
			
			//不自动聚焦了，总会激活浏览器自动补全框，影响操作
			//helpTarget.focus();
		});
	};
	
	po.cssColorToHexStrChartTheme = function(chartTheme)
	{
		var re = $.extend(true, {}, chartTheme);
		re.color = po.cssColorToHexStr(re.color);
		re.backgroundColor = po.cssColorToHexStr(re.backgroundColor);
		re.actualBackgroundColor = po.cssColorToHexStr(re.actualBackgroundColor);
		re.graphColors = po.cssColorsToHexStrs(re.graphColors);
		re.graphRangeColors = po.cssColorsToHexStrs(re.graphRangeColors);
		
		return re;
	};

	po.cssColorToHexStrStyle = function(style)
	{
		var re = $.extend(true, {}, style);
		re.color = po.cssColorToHexStr(re.color);
		re['background-color'] = po.cssColorToHexStr(re['background-color']);
		re['border-color'] = po.cssColorToHexStr(re['border-color']);
		
		return re;
	};
	
	//重写chart_attr_values_form.ftl中的函数
	po.getChartPluginAttributeInputOptionsForMap = function(asTree)
	{
		var re = [];
		
		var dashboardEditor = po.visualDashboardEditorByTab();
		if(dashboardEditor)
			re = dashboardEditor.getChartPluginAttributeInputOptionsForMap(asTree);
		
		return re;
	};
	
	po.veCodeEditoJsonOptions = function()
	{
		var editorOptions =
		{
			value: "",
			matchBrackets: true,
			autoCloseBrackets: true,
			mode: {name: "javascript", json: true}
		};
		
		return editorOptions;
	};
	
	po.showVeGridLayoutPanel = function(showFillParent)
	{
		showFillParent = (showFillParent == null ? false : showFillParent);
		
		var pm = po.vuePageModel();
		
		pm.veGridLayoutPanelShowFillParent = showFillParent;
		pm.vepms.gridLayout.fillParent = showFillParent;
		pm.vepss.gridLayoutShown = true;
	};

	po.showVeFlexLayoutPanel = function(showFillParent)
	{
		showFillParent = (showFillParent == null ? false : showFillParent);
		
		var pm = po.vuePageModel();
		
		pm.veFlexLayoutPanelShowFillParent = showFillParent;
		pm.vepms.flexLayout.fillParent = showFillParent;
		pm.vepss.flexLayoutShown = true;
	};
	
	po.showVeHxtitlePanel = function(submitHandler, model)
	{
		var pm = po.vuePageModel();
		pm.veshs.hxtitle = submitHandler;
		pm.vepms.hxtitle = $.extend(true, { type: "h1" }, model);
		pm.vepss.hxtitleShown = true;
	};
	
	po.showVeTextElementPanel = function(submitHandler, model)
	{
		var pm = po.vuePageModel();
		pm.veshs.textElement = submitHandler;
		pm.vepms.textElement = $.extend(true, {}, model);
		pm.vepss.textElementShown = true;
	};
	
	po.showVeImagePanel = function(submitHandler, model)
	{
		var pm = po.vuePageModel();
		pm.veshs.image = submitHandler;
		pm.vepms.image = $.extend(true, {}, model);
		pm.vepss.imageShown = true;
	};
	
	po.showVeHyperlinkPanel = function(submitHandler, model)
	{
		var pm = po.vuePageModel();
		pm.veshs.hyperlink = submitHandler;
		pm.vepms.hyperlink = $.extend(true, {}, model);
		pm.vepss.hyperlinkShown = true;
	};
	
	po.showVeVideoPanel = function(submitHandler, model)
	{
		var pm = po.vuePageModel();
		pm.veshs.video = submitHandler;
		pm.vepms.video = $.extend(true, po.veDftVideoModel(), model);
		pm.vepss.videoShown = true;
	};
	
	po.showVeDashboardSizePanel = function(model)
	{
		var pm = po.vuePageModel();
		pm.vepms.dashboardSize = $.extend(true, {}, model);
		pm.vepss.dashboardSizeShown = true;
	};

	po.showVeChartAttrValuesPanel = function(submitHandler, model, title)
	{
		var pm = po.vuePageModel();
		pm.veshs.chartAttrValues = submitHandler;
		if(title)
			pm.vepts.chartAttrValues = title; 
		pm.vepss.chartAttrValuesShown = true;
	};
	
	po.showVeChartOptionsPanel = function(submitHandler, model, title, global)
	{
		var pm = po.vuePageModel();
		pm.veshs.chartOptions = submitHandler;
		pm.vepms.chartOptions = $.extend(true, {}, model);
		if(title)
			pm.vepts.chartOptions = title;
		pm.veChartOptionsPanelForGlobal = global;
		pm.vepss.chartOptionsShown = true;
	};
	
	po.showVeChartThemePanel = function(submitHandler, model, title)
	{
		var pm = po.vuePageModel();
		pm.veshs.chartTheme = submitHandler;
		pm.vepms.chartTheme = $.extend(true, po.veDftChartThemeModel(), model);
		if(title)
			pm.vepts.chartTheme = title;
		pm.vepss.chartThemeShown = true;
	};
	
	po.showVeStylePanel = function(submitHandler, model, title)
	{
		var pm = po.vuePageModel();
		pm.veshs.style = submitHandler;
		pm.vepms.style = $.extend(true, {}, model);
		if(title)
			pm.vepts.style = title;
		pm.vepss.styleShown = true;
	};

	po.showVeEleIdPanel = function(submitHandler, model)
	{
		var pm = po.vuePageModel();
		pm.veshs.eleId = submitHandler;
		pm.vepms.eleId = $.extend(true, {}, model);
		pm.vepss.eleIdShown = true;
	};
	
	po.showVeIframePanel = function(submitHandler, model)
	{
		var pm = po.vuePageModel();
		pm.veshs.iframe = submitHandler;
		pm.vepms.iframe = $.extend(true, po.veDftIframeModel(), model);
		pm.vepss.iframeShown = true;
	};
	
	po.setupResourceEditorForms = function()
	{
		po.vueRef(po.concatPid("optionsOriginPanelEle"), null);
		
		po.vuePageModel(
		{
			//可视编辑操作对话框是否显示
			vepss:
			{
				gridLayoutShown: false,
				flexLayoutShown: false,
				hxtitleShown: false,
				textElementShown: false,
				imageShown: false,
				hyperlinkShown: false,
				videoShown: false,
				dashboardSizeShown: false,
				chartOptionsShown: false,
				chartAttrValuesShown: false,
				chartThemeShown: false,
				styleShown: false,
				eleIdShown: false,
				iframeShown: false
			},
			//可视编辑操作对话框标题
			vepts:
			{
				gridLayout: po.i18n.gridLayout,
				flexLayout: po.i18n.flexLayout,
				hxtitle: po.i18n.titleElement,
				textElement: po.i18n.textElement,
				image: po.i18n.image,
				hyperlink: po.i18n.hyperlink,
				video: po.i18n.video,
				dashboardSize: po.i18n.dashboardSize,
				chartOptions: po.i18n.chartOptions,
				chartAttrValues: po.i18n.chartAttribute + " dg-chart-attr-values",
				chartTheme: po.i18n.chartTheme,
				style: po.i18n.style,
				eleId: po.i18n.elementId,
				iframe: po.i18n.iframe
			},
			//可视编辑操作对话框表单模型
			vepms:
			{
				gridLayout: po.veDftGridLayoutModel(),
				flexLayout: { fillParent: false },
				hxtitle: { type: "h1", content: "" },
				textElement: { content: "" },
				image: {},
				hyperlink: {},
				video: po.veDftVideoModel(),
				dashboardSize: { scale: "auto" },
				chartOptions: { value: "" },
				chartAttrValues: {},
				chartTheme: po.veDftChartThemeModel(),
				style: {},
				eleId: {},
				iframe: po.veDftIframeModel()
			},
			//可视编辑操作对话框提交处理函数
			veshs:
			{
				hxtitle: function(model){},
				textElement: function(model){},
				image: function(model){},
				hyperlink: function(model){},
				video: function(model){},
				chartOptions: function(model){},
				chartAttrValues: function(model){},
				chartTheme: function(model){},
				style: function(model){},
				eleId: function(model){},
				iframe: function(model){}
			},
			veGridLayoutPanelShowFillParent: false,
			veFlexLayoutPanelShowFillParent: false,
			dashboardSizeScaleOptions:
			[
				{ name: po.i18n.auto, value: "auto" },
				{ name: "100%", value: 100 },
				{ name: "75%", value: 75 },
				{ name: "50%", value: 50 },
				{ name: "25%", value: 25 }
			],
			veStyleTabviewActiveIndex: 0,
			hxtitleTypeOptions:
			[
				{ name: po.i18n["dashboard.veditor.hxtitle.type.h1"], value: "h1" },
				{ name: po.i18n["dashboard.veditor.hxtitle.type.h2"], value: "h2" },
				{ name: po.i18n["dashboard.veditor.hxtitle.type.h3"], value: "h3" },
				{ name: po.i18n["dashboard.veditor.hxtitle.type.h4"], value: "h4" },
				{ name: po.i18n["dashboard.veditor.hxtitle.type.h5"], value: "h5" },
				{ name: po.i18n["dashboard.veditor.hxtitle.type.h6"], value: "h6" }
			],
			veChartOptionsPanelForGlobal: false,
			gridLayoutDivideOptions:
			[
				{ name: po.i18n["dashboard.veditor.gridLayout.divide.avg"], value: "avg" },
				{ name: po.i18n["dashboard.veditor.gridLayout.divide.custom"], value: "custom" }
			]
		});
		
		var pm = po.vuePageModel();
		
		po.vueMethod(
		{
			formatHxtitleOptionLabel: function(option)
			{
				return "<"+option.value+">"+option.name+"</"+option.value+">";
			},
			onVeGridLayoutPanelShow: function()
			{
				var form = po.elementOfPidPrefix("veGridLayoutForm", document.body);
				
				po.initVePanelHelperSrc(form, pm.vepms.gridLayout);
				
				po.setupSimpleForm(form, pm.vepms.gridLayout, function()
				{
					if(po.insertVeGridLayout(pm.vepms.gridLayout) !== false)
					{
						pm.vepms.gridLayout = po.veDftGridLayoutModel();
						pm.vepss.gridLayoutShown = false;
					}
				});
			},
			
			onVeFlexLayoutPanelShow: function()
			{
				var form = po.elementOfPidPrefix("veFlexLayoutForm", document.body);
				
				po.initVePanelHelperSrc(form, pm.vepms.flexLayout);
				
				po.setupSimpleForm(form, pm.vepms.flexLayout, function()
				{
					if(po.insertVeFlexLayout(pm.vepms.flexLayout) !== false)
					{
						pm.vepms.flexLayout = { fillParent: false };
						pm.vepss.flexLayoutShown = false;
					}
				});
			},
			
			onVeHxtitlePanelShow: function()
			{
				var form = po.elementOfPidPrefix("veHxtitleForm", document.body);
				po.initVePanelHelperSrc(form, pm.vepms.hxtitle);
				
				po.setupSimpleForm(form, pm.vepms.hxtitle, function()
				{
					if(pm.veshs.hxtitle(pm.vepms.hxtitle) !== false)
					{
						pm.vepms.hxtitle = { type: "h1", content: "" };
						pm.vepss.hxtitleShown = false;
					}
				});
			},
			
			onVeTextElementPanelShow: function()
			{
				var form = po.elementOfPidPrefix("veTextElementForm", document.body);
				
				po.setupSimpleForm(form, pm.vepms.textElement, function()
				{
					if(pm.veshs.textElement(pm.vepms.textElement) !== false)
					{
						pm.vepms.textElement = {};
						pm.vepss.textElementShown = false;
					}
				});
			},
			
			onVeImagePanelShow: function()
			{
				var form = po.elementOfPidPrefix("veImageForm", document.body);
				
				po.initVePanelHelperSrc(form, pm.vepms.image);
				
				po.setupSimpleForm(form, pm.vepms.image, function()
				{
					if(pm.veshs.image(pm.vepms.image) !== false)
					{
						pm.vepms.image = {};
						pm.vepss.imageShown = false;
					}
				});
			},
			
			onVeHyperlinkPanelShow: function()
			{
				var form = po.elementOfPidPrefix("veHyperlinkForm", document.body);
				
				po.initVePanelHelperSrc(form, pm.vepms.hyperlink);
				
				po.setupSimpleForm(form, pm.vepms.hyperlink, function()
				{
					if(pm.veshs.hyperlink(pm.vepms.hyperlink) !== false)
					{
						pm.vepms.hyperlink = {};
						pm.vepss.hyperlinkShown = false;
					}
				});
			},
			
			onVeVideoPanelShow: function()
			{
				var form = po.elementOfPidPrefix("veVideoForm", document.body);
				
				po.initVePanelHelperSrc(form, pm.vepms.video);
				
				po.setupSimpleForm(form, pm.vepms.video, function()
				{
					if(pm.veshs.video(pm.vepms.video) !== false)
					{
						pm.vepms.video = po.veDftVideoModel();
						pm.vepss.videoShown = false;
					}
				});
			},
			
			onVeDashboardSizePanelShow: function()
			{
				var form = po.elementOfPidPrefix("veDashboardSizeForm", document.body);
				
				po.setupSimpleForm(form, pm.vepms.dashboardSize, function()
				{
					if(po.setVeDashboardSize(null, pm.vepms.dashboardSize) !== false)
					{
						pm.vepss.dashboardSizeShown = false;
					}
				});
			},
			
			onVeDashboardSizeResetToDft: function()
			{
				if(po.setVeDashboardSize(null, {}) !== false)
				{
					pm.vepss.dashboardSizeShown = false;
				}
			},
			
			onVeChartAttrValuesPanelShow: function()
			{
				var dashboardEditor = po.visualDashboardEditorByTab();
				var cpas = [];
				var attrValues = [];
				
				if(dashboardEditor)
				{
					cpas = (dashboardEditor.getElementChartPluginAttrs() || []);
					attrValues = (dashboardEditor.getElementChartAttrValues() || {});
				}
				
				po.setupChartAttrValuesForm(cpas, attrValues,
				{
					submitHandler: function(avs)
					{
						if(pm.veshs.chartAttrValues(avs) !== false)
						{
							pm.vepss.chartAttrValuesShown = false;
						}
					},
					buttons:
					[
						{
							name: po.i18n.resetToOrigin,
							clickHandler: function()
							{
								var attrValuesReset = {};
								
								if(dashboardEditor)
									attrValuesReset = (dashboardEditor.getElementChartAttrValuesForReset() || {});
								
								po.setChartAttrValuesFormAttrValues(attrValuesReset);
							}
						}
					]
				});
			},
			
			onVeChartOptionsPanelShow: function()
			{
				var form = po.elementOfPidPrefix("veChartOptionsForm", document.body);
				var codeEditorEle = po.elementOfPidPrefix("veChartOptionsCodeEditor", form);
				
				codeEditorEle.empty();
				var editorOptions = po.veCodeEditoJsonOptions();
				var codeEditor = po.createCodeEditor(codeEditorEle, editorOptions);
				po.setCodeTextTimeout(codeEditor, (pm.vepms.chartOptions.value || ""), true);
				
				po.setupSimpleForm(form, pm.vepms.chartOptions, function()
				{
					pm.vepms.chartOptions.value = po.getCodeText(codeEditor);
					if(pm.veshs.chartOptions(pm.vepms.chartOptions) !== false)
					{
						pm.vepms.chartOptions = {};
						pm.vepss.chartOptionsShown = false;
					}
				});
			},
			
			onVeChartThemePanelShow: function()
			{
				var form = po.elementOfPidPrefix("veChartThemeForm", document.body);
				
				po.setupSimpleForm(form, pm.vepms.chartTheme, function()
				{
					if(pm.veshs.chartTheme(pm.vepms.chartTheme) !== false)
					{
						pm.vepms.chartTheme = po.veDftChartThemeModel();
						pm.vepss.chartThemeShown = false;
					}
				});
			},
			
			onVeChartThemeAddGraphColor: function()
			{
				var chartTheme = pm.vepms.chartTheme;
				chartTheme.graphColors.push("");
			},
			
			onVeChartThemeAddGraphRangeColor: function()
			{
				var chartTheme = pm.vepms.chartTheme;
				chartTheme.graphRangeColors.push("");
			},
			
			onVeChartThemeInsertGraphColor: function(e, idx)
			{
				var chartTheme = pm.vepms.chartTheme;
				//不在idx+1位置插入，这样无法在第一个之前插入
				chartTheme.graphColors.splice(idx, 0, "");
			},
			
			onVeChartThemeRemoveGraphColor: function(e, idx)
			{
				var chartTheme = pm.vepms.chartTheme;
				chartTheme.graphColors.splice(idx, 1);
			},

			onVeChartThemeInsertGraphRangeColor: function(e, idx)
			{
				var chartTheme = pm.vepms.chartTheme;
				//不在idx+1位置插入，这样无法在第一个之前插入
				chartTheme.graphRangeColors.splice(idx, 0, "");
			},
			
			onVeChartThemeRemoveGraphRangeColor: function(e, idx)
			{
				var chartTheme = pm.vepms.chartTheme;
				chartTheme.graphRangeColors.splice(idx, 1);
			},
			
			onVeStylePanelShow: function()
			{
				var form = po.elementOfPidPrefix("veStyleForm", document.body);
				
				po.initVePanelHelperSrc(form, pm.vepms.style);
				
				po.setupSimpleForm(form, pm.vepms.style, function()
				{
					if(pm.veshs.style(pm.vepms.style) !== false)
					{
						pm.vepms.style = {};
						pm.vepss.styleShown = false;
					}
				});
			},
			
			onShowOptionsOriginPanel: function(e)
			{
				po.vueUnref(po.concatPid("optionsOriginPanelEle")).toggle(e);
			},
			
			onOptionsOriginPanelShow: function()
			{
				var optionsOrigin = "";
				
				var dashboardEditor = po.visualDashboardEditorByTab();
				if(dashboardEditor)
					optionsOrigin = dashboardEditor.getElementChartOptionsOrigin();
				
				var form = po.elementOfPidPrefix("optionsOriginForm", document.body);
				var codeEditorEle = po.elementOfPidPrefix("optionsContentCodeEditor", form);
				
				codeEditorEle.empty();
				var editorOptions = po.veCodeEditoJsonOptions();
				var codeEditor = po.createCodeEditor(codeEditorEle, editorOptions);
				po.setCodeTextTimeout(codeEditor, (optionsOrigin || ""), true);
				
				po.setupSimpleForm(form, {}, function(){});
			},
			
			onVeEleIdPanelShow: function()
			{
				var form = po.elementOfPidPrefix("veEleIdForm", document.body);
				
				po.setupSimpleForm(form, pm.vepms.eleId, function()
				{
					if(pm.veshs.eleId(pm.vepms.eleId) !== false)
					{
						pm.vepms.eleId = {};
						pm.vepss.eleIdShown = false;
					}
				});
			},

			onVeIframePanelShow: function()
			{
				var form = po.elementOfPidPrefix("veIframeForm", document.body);
				
				po.initVePanelHelperSrc(form, pm.vepms.iframe);
				
				po.setupSimpleForm(form, pm.vepms.iframe, function()
				{
					if(pm.veshs.iframe(pm.vepms.iframe) !== false)
					{
						pm.vepms.iframe = po.veDftIframeModel();
						pm.vepss.iframeShown = false;
					}
				});
			}
		});
		
		po.vueWatch(function()
		{
			return pm.vepms.gridLayout.rows;
		},
		function(newVal, oldVal)
		{
			//默认不使用"auto"，内部插入元素后会导致尺寸变化
			var dftValue = "1fr";
			$.trimArrayLen(pm.vepms.gridLayout.rowHeights, pm.vepms.gridLayout.rows, dftValue);
		});
		
		po.vueWatch(function()
		{
			return pm.vepms.gridLayout.columns;
		},
		function(newVal, oldVal)
		{
			//默认不使用"auto"，内部插入元素后会导致尺寸变化
			var dftValue = "1fr";
			$.trimArrayLen(pm.vepms.gridLayout.colWidths, pm.vepms.gridLayout.columns, dftValue);
		});
	};
};


//填充看板设计页面资源管理JS对象
//依赖：
//dashboard_design_resource.ftl
//dashboard_design_resource_forms.ftl
$.inflateDashboardDesignResource = function(po)
{
	po.isResTemplate = function(name)
	{
		var fm = po.vueFormModel();
		return ($.inArray(name, fm.templates) > -1);
	};
	
	po.resNamesToTree = function(names)
	{
		var tree = $.toPathTree(names,
		{
			created: function(node)
			{
				node.key = node.fullPath;
				node.label = node.name;
			}
		});
		
		return tree;
	};
	
	po.refreshLocalRes = function()
	{
		var fm = po.vueFormModel();
		
		if(!fm.id)
			return;
		
		po.getJson("/dashboard/listResources", { id: fm.id }, function(response)
		{
			var pm = po.vuePageModel();
			pm.localRes.resourceNodes = po.resNamesToTree(response);
			pm.localRes.selectedNodeKeys = null;
			pm.localRes.selectedNode = null;
		});
	};

	po.showSelectGlobalResDialog = function()
	{
		var dialog = po.selectGlobalResDialog();
		
		if(dialog.length == 0)
		{
			po.openTableDialog("/dashboardGlobalRes/select",
			{
				modal: false,
				closable: false,
				styleClass: "dashboard-select-global-res-wrapper table-sm",
				templateHeader: "<span class='p-dialog-title'>"+po.i18n.dashboardGlobalRes+" - "+po.i18n.select+"</span>"
								+"<div class='dialog-btns p-dialog-header-icons'>"
								+"	<p-button type='button' icon='pi pi-times' class='p-dialog-header-icon p-dialog-header-close p-link' @click='onCustomHide'></p-button>"
								+"</div>",
				width: "45vw",
				position: "right",
				onSetup: function(setup)
				{
					setup.onCustomHide = function()
					{
						po.hideSelectGlobalResDialog();
					};
				},
				pageParam:
				{
					select: function(res)
					{
						po.copyToClipboard(po.toGlobalResUrl(res.path));
						
						po.hideSelectGlobalResDialog();
						return false;
					},
					onView: function(res)
					{
						window.open(po.showUrl(po.toGlobalResUrl(res.path)));
					}
				}
			});
		}
		else
		{
			var dialogMask = dialog.parent();
			dialogMask.removeClass("opacity-hide");
		}
	};
	
	po.hideSelectGlobalResDialog = function()
	{
		var dialog = po.selectGlobalResDialog();
		var dialogMask = dialog.parent();
		dialogMask.addClass("opacity-hide");
	};
	
	po.closeSelectGlobalResDialog = function()
	{
		var dialog = po.selectGlobalResDialog();
		$.closeDialog(dialog);
	};
	
	po.toGlobalResUrl = function(path)
	{
		return po.dashboardGlobalResUrlPrefix + path;
	};
	
	po.selectGlobalResDialog = function()
	{
		return $(".dashboard-select-global-res-wrapper", document.body);
	};
	
	po.openSelectedLocalRes = function()
	{
		var fm = po.vueFormModel();
		
		if(!fm.id)
			return;
		
		var sr = po.getSelectedLocalRes();
		if(sr)
			window.open(po.showUrl(sr), fm.id+"/"+sr);
	};
	
	po.getSelectedLocalRes = function()
	{
		var pm = po.vuePageModel();
		var localRes = pm.localRes;
		
		if(localRes.selectedTemplate)
			return localRes.selectedTemplate;
		else if(localRes.selectedNodeKeys && localRes.selectedNode)
			return localRes.selectedNode.fullPath;
		else
			return null;
	};
	
	po.addRes = function(name)
	{
		if(!name)
			return false;
		
		if($.isDirectoryFile(name))
		{
			$.tipInfo(po.i18n.illegalSaveAddResourceName);
			return false;
		}
		
		var isTemplate = $.isHtmlFile(name);
		po.showResContentTab(name, isTemplate);
		
		return true;
	};
	
	po.uploadRes = function(uploadModel)
	{
		if(!uploadModel.filePath)
			return false;
		
		var fm = po.vueFormModel();
		
		po.post("/dashboard/saveUploadResourceFile",
		{
			id: fm.id,
			resourceFilePath: uploadModel.filePath,
			resourceName: (uploadModel.savePath || ""),
			autoUnzip: uploadModel.autoUnzip,
			zipFileNameEncoding: uploadModel.zipFileNameEncoding
		},
		function(response)
		{
			po.vueUnref(po.concatPid("uploadResPanelEle")).hide();
			
			var pm = po.vuePageModel();
			pm.uploadResModel.savePath = "";
			pm.uploadResModel.filePath = "";
			pm.uploadResModel.autoUnzip = false;
			po.clearFileuploadInfo();
			po.refreshLocalRes();
		});
	};
	
	po.updateTemplateList = function(templates)
	{
		var fm = po.vueFormModel();
		fm.templates = templates;
	};
	
	po.setResAsTemplate = function(name)
	{
		if(!name)
			return;
		
		if(!$.isHtmlFile(name))
		{
	 		$.tipInfo(po.i18n.setResAsTemplateUnsupport);
	 		return;
		}
		
		var fm = po.vueFormModel();
		var templates = po.vueRaw(fm.templates);
		
		if($.inArray(name, templates) < 0)
		{
			templates.push(name);
			po.saveTemplateNames(templates);
		}
	};
	
	po.setResAsFirstTemplate = function(name)
	{
		if(!name)
			return;
		
		if(!$.isHtmlFile(name))
		{
	 		$.tipInfo(po.i18n.setResAsTemplateUnsupport);
	 		return;
		}
		
		var fm = po.vueFormModel();
		var templates = po.vueRaw(fm.templates);
		var idx = $.inArray(name, templates);
		
		if(idx == 0)
			return;
		else
		{
			if(idx > 0)
				templates.splice(idx, 1);
			
			templates.unshift(name);
			po.saveTemplateNames(templates);
		}
	};
	
	po.setTemplateAsNormalRes = function(name)
	{
		if(!name)
			return;
		
		var fm = po.vueFormModel();
		var templates = po.vueRaw(fm.templates);
		var idx = $.inArray(name, templates);
		
		if(idx > -1)
		{
			if(templates.length < 2)
			{
				$.tipWarn(po.i18n.atLeastOneTemplateRequired);
				return;
			}
			
			templates.splice(idx, 1);
			po.saveTemplateNames(templates);
		}
	};
	
	po.saveTemplateNames = function(templates)
	{
		var fm = po.vueFormModel();
		
		po.ajaxJson("/dashboard/saveTemplateNames?id="+encodeURIComponent(fm.id),
		{
			data: templates,
			success: function(response)
			{
				po.updateTemplateList(response.data.templates);
			}
		});
	};
	
	po.renameRes = function(srcName, destName)
	{
		if(!srcName || !destName || srcName == destName)
			return;
		
		var fm = po.vueFormModel();
		
		po.post("/dashboard/renameResource", { id: fm.id, srcName: srcName, destName: destName},
		function(response)
		{
			po.updateTemplateList(response.data.templates);
			po.refreshLocalRes();
			
			if(po.updateEditorResNames)
				po.updateEditorResNames(response.data.renames);
		});
	};
	
	po.deleteRes = function(name)
	{
		if(!name)
			return;
		
		var fm = po.vueFormModel();
		var templates = fm.templates;
		var idx = $.inArray(name, templates);
		
		if(idx > -1 && templates.length < 2)
		{
			$.tipWarn(po.i18n.atLeastOneTemplateRequired);
			return;
		}
		
		po.post("/dashboard/deleteResource", { id: fm.id, name: name},
		function(response)
		{
			po.updateTemplateList(response.data.templates);
			po.refreshLocalRes();
		});
	};
	
	po.setupResourceList = function()
	{
		po.vuePageModel(
		{
			availableCharsetNames: po.availableCharsetNames,
			localRes:
			{
				selectedTemplate: null,
				resourceNodes: null,
				selectedNodeKeys: null,
				selectedNode: null
			},
			localResMenuItems:
			[
				{
					label: po.i18n.setAsTemplate,
					command: function()
					{
						var resName = po.getSelectedLocalRes();
						po.setResAsTemplate(resName);
					}
				},
				{
					label: po.i18n.setAsHomeTemplate,
					command: function()
					{
						var resName = po.getSelectedLocalRes();
						po.setResAsFirstTemplate(resName);
					}
				},
				{
					label: po.i18n.unsetAsTemplate,
					command: function()
					{
						var resName = po.getSelectedLocalRes();
						po.setTemplateAsNormalRes(resName);
					}
				},
				{
					label: po.i18n.rename,
					command: function(e)
					{
						var resName = po.getSelectedLocalRes();
						if(resName)
						{
							var pm = po.vuePageModel();
							pm.renameResModel.srcName = resName;
							pm.renameResModel.destName = resName;
							
							e.originalEvent.stopPropagation();
							po.elementOfPidPrefix("renameResBtn").click();
						}
					}
				},
				{
					label: po.i18n.refresh,
					command: function()
					{
						po.refreshLocalRes();
					}
				},
				{ separator: true },
				{
					label: po.i18n["delete"],
					class: "p-error",
					command: function()
					{
						var resName = po.getSelectedLocalRes();
						
						if(resName)
						{
							po.confirmDelete(function()
							{
								po.deleteRes(resName);
							});
						}
					}
				}
			],
			addResModel:
			{
				resName: null
			},
			uploadResModel:
			{
				url: po.concatContextPath("/dashboard/uploadResourceFile"),
				filePath: null,
				savePath: null,
				autoUnzip: false,
				zipFileNameEncoding: po.zipFileNameEncodingDefault
			},
			renameResModel:
			{
				srcName: null,
				destName: null
			}
		});
		
		po.vueMethod(
		{
			onResourceTabChange: function(e){},
			
			onChangeTemplateListItem: function(e)
			{
				var pm = po.vuePageModel();
				pm.localRes.selectedNodeKeys = null;
				pm.localRes.selectedNode = null;
			},
			
			onLocalResNodeSelect: function(node)
			{
				var pm = po.vuePageModel();
				pm.localRes.selectedNode = node;
				pm.localRes.selectedTemplate = null;
			},
			
			onCopyLocalResToClipboard: function(e)
			{
				po.copyToClipboard(po.getSelectedLocalRes());
			},
			
			onOpenSelectedLocalRes: function(e)
			{
				po.openSelectedLocalRes();
			},
			
			onEditSelectedLocalRes: function(e)
			{
				var res = po.getSelectedLocalRes();
				if(res)
				{
				 	if(!$.isTextFile(res))
				 	{
				 		$.tipInfo(po.i18n.editResUnsupport);
				 		return;
				 	}
				 	else
						po.showResContentTab(res);
				}
			},
			
			toggleLocalResMenu: function(e)
			{
				po.vueUnref(po.concatPid("localResMenuEle")).toggle(e);
			},
			
			onShowGlobalRes: function(e)
			{
				po.showSelectGlobalResDialog();
			},
			
			onToggleAddResPanel: function(e)
			{
				var pm = po.vuePageModel();
				po.vueUnref(po.concatPid("addResPanelEle")).toggle(e);
			},
			
			onAddResPanelShow: function(e)
			{
				var pm = po.vuePageModel();
				var panel = po.elementOfPidPrefix("addResPanel", document.body);
				var form = po.elementOfPidPrefix("addResForm", panel);
				po.elementOfPidPrefix("addResName", form).focus();
				
				po.setupSimpleForm(form, pm.addResModel, function()
				{
					if(po.addRes(pm.addResModel.resName))
					{
						po.vueUnref(po.concatPid("addResPanelEle")).hide();
						pm.addResModel.resName = "";
					}
				});
			},
			
			onToggleUploadResPanel: function(e)
			{
				po.vueUnref(po.concatPid("uploadResPanelEle")).toggle(e);
			},
			
			onUploadResPanelShow: function(e)
			{
				var pm = po.vuePageModel();
				var panel = po.elementOfPidPrefix("uploadResPanel", document.body);
				var form = po.elementOfPidPrefix("uploadResForm", panel);
				
				po.setupSimpleForm(form, pm.uploadResModel,
				{
					customNormalizers:
					{
						savePath: function()
						{
							if(pm.uploadResModel.autoUnzip && $.isZipFile(pm.uploadResModel.filePath))
								return (pm.uploadResModel.savePath || "savePathValidatePlaceholder");
							else
								return pm.uploadResModel.savePath;
						}
					},
					submitHandler: function()
					{
						po.uploadRes(pm.uploadResModel);
					}
				});
			},
			
			onResUploaded: function(e)
			{
				var pm = po.vuePageModel();
				var response = $.getResponseJson(e.xhr);
				
				po.uploadFileOnUploaded(e);
				
				var sr = po.getSelectedLocalRes();
				
				pm.uploadResModel.savePath = ($.isDirectoryFile(sr) ? sr + response.fileName : response.fileName);
				pm.uploadResModel.filePath = response.uploadFilePath;
			},
			
			onToggleRenameResPanel: function(e)
			{
				po.vueUnref(po.concatPid("renameResPanelEle")).toggle(e);
			},
			
			onRenameResPanelShow: function(e)
			{
				var pm = po.vuePageModel();
				var panel = po.elementOfPidPrefix("renameResPanel", document.body);
				var form = po.elementOfPidPrefix("renameResForm", panel);
				po.elementOfPidPrefix("destResName", form).focus();
				
				po.setupSimpleForm(form, pm.renameResModel, function()
				{
					po.renameRes(pm.renameResModel.srcName, pm.renameResModel.destName);
					po.vueUnref(po.concatPid("renameResPanelEle")).hide();
				});
			}
		});
		
		po.vueMounted(function()
		{
			po.refreshLocalRes();
		});
		
		po.vueRef(po.concatPid("localResMenuEle"), null);
		po.vueRef(po.concatPid("addResPanelEle"), null);
		po.vueRef(po.concatPid("uploadResPanelEle"), null);
		po.vueRef(po.concatPid("renameResPanelEle"), null);

		po.beforeClose("closeSelectGlobalResDialog", function()
		{
			po.closeSelectGlobalResDialog();
		});

		po.element().click(function(e)
		{
			var targetEle = $(e.target);
			
			if(targetEle.hasClass("for-open-global-res-panel") || targetEle.closest(".for-open-global-res-panel").length > 0)
				;//保持选择图表对话框
			else
				po.hideSelectGlobalResDialog();
		});
	};
};

})
(jQuery);
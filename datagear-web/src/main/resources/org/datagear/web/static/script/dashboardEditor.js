/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

/**
 * 看板可视编辑器。
 * 全局变量名：window.dashboardFactory.dashboardEditor
 * 
 * 加载时依赖：
 *   dashboardFactory.js
 * 
 * 运行时依赖:
 *   jquery.js
 *   dashboardFactory.js
 *   chartFactory.js
 */
(function(global)
{
	/**看板工厂*/
	var dashboardFactory = (global.dashboardFactory || (global.dashboardFactory = {}));
	var editor = (dashboardFactory.dashboardEditor || (dashboardFactory.dashboardEditor = {}));
	var i18n = (editor.i18n || (editor.i18n = {}));
	
	i18n.insertInsideChartOnChartEleDenied = "图表元素内不允许再插入图表元素";
	i18n.selectElementForSetChart = "请选择要设置/替换的图表元素";
	i18n.canEditOnlyTextElement = "仅可编辑纯文本元素";
	i18n.selectedElementRequired = "请选择要操作的元素";
	i18n.selectedNotChartElement = "选定元素不是图表元素";
	i18n.noSelectableNextElement="没有可选择的下一个元素";
	i18n.noSelectablePrevElement="没有可选择的上一个元素";
	i18n.noSelectableChildElement="没有可选择的子元素";
	i18n.noSelectableParentElement="没有可选择的父元素";
	
	//参考org.datagear.web.controller.DashboardController.DASHBOARD_BUILTIN_RENDER_CONTEXT_ATTR_EDIT_HTML_INFO
	var DASHBOARD_BUILTIN_RENDER_CONTEXT_ATTR_EDIT_HTML_INFO = (editor.DASHBOARD_BUILTIN_RENDER_CONTEXT_ATTR_EDIT_HTML_INFO = "DG_EDIT_HTML_INFO");
	
	//参考org.datagear.web.controller.DashboardController.DashboardShowForEdit.ELEMENT_ATTR_VISUAL_EDIT_ID
	var ELEMENT_ATTR_VISUAL_EDIT_ID = (editor.ELEMENT_ATTR_VISUAL_EDIT_ID = "dg-visual-edit-id");
	
	var ELEMENT_CLASS_SELECTED = (editor.ELEMENT_CLASS_SELECTED = "dg-show-ve-selected");
	
	var ELEMENT_CLASS_NEW_INSERT = (editor.ELEMENT_CLASS_NEW_INSERT = "dg-show-ve-new-insert");
	
	dashboardFactory._initSuperByDashboardEditor = dashboardFactory.init;
	dashboardFactory.init = function(dashboard)
	{
		dashboardFactory._initSuperByDashboardEditor(dashboard);
		editor.init(dashboard);
	};
	
	/**
	 * 初始化可视编辑器。
	 */
	editor.init = function(dashboard)
	{
		this.dashboard = dashboard;
		
		this._initStyle();
		this._initEditHtmlIframe();
		this._initInteraction();
	};
	
	///初始化样式。
	editor._initStyle = function()
	{
		this._setPageStyle();
	};
	
	//初始化编辑HTML的iframe
	editor._initEditHtmlIframe = function()
	{
		var editHtmlInfo = this._editHtmlInfo();
		var editBodyHtml = this._unescapeEditHtml(editHtmlInfo.bodyHtml);
		this._editIframe(editBodyHtml);
	};
	
	//初始化交互控制
	editor._initInteraction = function()
	{
		$(document.body).addClass("dg-show-ve");
		
		$(document.body).on("click", function(event)
		{
			editor._removeElementClassNewInsert();
			
			var target = $(event.target);
			var veEle = (target.attr(ELEMENT_ATTR_VISUAL_EDIT_ID) ? target :
								target.closest("["+ELEMENT_ATTR_VISUAL_EDIT_ID+"]"));
			
			if(veEle.length == 0)
			{
				editor._deselectAllElement();
			}
			else
			{
				if(veEle.is(":hidden"))
				{
					editor._deselectAllElement();
				}
				else if(editor._isSelectedElement(veEle))
				{
					//再次点击选中元素，不取消选择
				}
				else
				{
					editor._deselectAllElement();
					editor._selectElement(veEle);
				}
			}
		});
	};
	
	//获取当前编辑HTML
	editor.editedHtml = function()
	{
		var editHtmlInfo = this._editHtmlInfo();
		var editBodyHtml = this._editBodyHtml();
		
		//将占位标签还原为原始标签
		var placeholderSources = (editHtmlInfo.placeholderSources || {});
		for(var placeholder in placeholderSources)
		{
			var source = placeholderSources[placeholder];
			editBodyHtml = editBodyHtml.replace(placeholder, source);
		}
		
		//删除末尾的：" dg-visual-edit-id='...'>"
		var eidRegex0 = /\s?dg\-visual\-edit\-id\=["'][^"']*["']\s*>/gi;
		editBodyHtml = editBodyHtml.replace(eidRegex0, ">");
		
		//删除中间的：" dg-visual-edit-id='...'"
		var eidRegex1 = /\s?dg\-visual\-edit\-id\=["'][^"']*["']/gi;
		editBodyHtml = editBodyHtml.replace(eidRegex1, "");
		
		var editedHtml = editHtmlInfo.beforeBodyHtml + editBodyHtml + editHtmlInfo.afterBodyHtml;
		return this._unescapeEditHtml(editedHtml);
	};
	
	//是否在指定changeFlag后有修改
	editor.isChanged = function(changeFlag)
	{
		return (this.changeFlag() != changeFlag);
	};
	
	//获取当前修改标识
	editor.changeFlag = function(set)
	{
		if(this._changeFlag == null)
			this._changeFlag = 0;
		
		if(set == true)
		{
			this._changeFlag++;
		}
		else
		{
			return this._changeFlag;
		}
	};
	
	//提示信息
	editor.tipInfo = function(msg)
	{
		alert(msg);
	};
	
	/**
	 * 选择下一个可编辑元素。
	 * 如果元素时<body>，将选中其第一个可编辑子元素。
	 * 
	 * @param ele 可选，元素，默认为：当前选中元素、或者<body>元素
	 * @param tip 可选，未选择时是否给出提示，默认为：true
	 * @returns true 已选择，false 未选择
	 */
	editor.selectNextElement = function(ele, tip)
	{
		//(true)、(false)
		if(arguments.length == 1 && (ele === true || ele === false))
		{
			tip = ele;
			ele = undefined;
		}
		
		tip = (tip == null ? true : tip);
		
		this._removeElementClassNewInsert();
		
		ele = this._refElement(ele);
		
		if(ele.is("body"))
			return this.selectFirstChildElement(ele, tip);
		
		var target = ele;
		while((target = target.next()))
		{
			if(target.length == 0
				|| (target.attr(ELEMENT_ATTR_VISUAL_EDIT_ID) && !target.is("script,style") && !target.is(":hidden")))
			{
				break;
			}
		}
		
		if(target.length == 0)
		{
			if(tip)
				this.tipInfo(i18n.noSelectableNextElement);
			
			return false;
		}
		
		this._deselectAllElement();
		this._selectElement(target);
		return true;
	};
	
	/**
	 * 选择前一个可编辑元素。
	 * 如果元素时<body>，将选中其第一个可编辑子元素。
	 * 
	 * @param ele 可选，元素，默认为：当前选中元素、或者<body>元素
	 * @param tip 可选，未选择时是否给出提示，默认为：true
	 * @returns true 已选择，false 未选择
	 */
	editor.selectPrevElement = function(ele, tip)
	{
		//(true)、(false)
		if(arguments.length == 1 && (ele === true || ele === false))
		{
			tip = ele;
			ele = undefined;
		}
		
		tip = (tip == null ? true : tip);
		
		this._removeElementClassNewInsert();
		
		ele = this._refElement(ele);
		
		if(ele.is("body"))
			return this.selectFirstChildElement(ele, tip);
		
		var target = ele;
		while((target = target.prev()))
		{
			if(target.length == 0
				|| (target.attr(ELEMENT_ATTR_VISUAL_EDIT_ID) && !target.is("script,style") && !target.is(":hidden")))
			{
				break;
			}
		}
		
		if(target.length == 0)
		{
			if(tip)
				this.tipInfo(i18n.noSelectablePrevElement);
			return false;
		}
		
		this._deselectAllElement();
		this._selectElement(target);
		return true;
	};
	
	/**
	 * 选择第一个可编辑子元素。
	 * 
	 * @param ele 可选，元素，默认为：当前选中元素、或者<body>元素
	 * @param tip 可选，未选择时是否给出提示，默认为：true
	 * @returns true 已选择，false 未选择
	 */
	editor.selectFirstChildElement = function(ele, tip)
	{
		//(true)、(false)
		if(arguments.length == 1 && (ele === true || ele === false))
		{
			tip = ele;
			ele = undefined;
		}
		
		tip = (tip == null ? true : tip);
		
		this._removeElementClassNewInsert();
		
		ele = this._refElement(ele);
		var firstChild = $("> *:first", ele);
		
		var target = firstChild;
		while(true)
		{
			if(target.length == 0
				|| (target.attr(ELEMENT_ATTR_VISUAL_EDIT_ID) && !target.is("script,style") && !target.is(":hidden")))
			{
				break;
			}
			
			target = target.next();
		}
		
		if(target.length == 0)
		{
			if(tip)
				this.tipInfo(i18n.noSelectableChildElement);
			return false;
		}
		
		this._deselectAllElement();
		this._selectElement(target);
		return true;
	};
	
	/**
	 * 选择可编辑上级元素。
	 * 
	 * @param ele 可选，元素，默认为：当前选中元素、或者<body>元素
	 * @param tip 可选，未选择时是否给出提示，默认为：true
	 * @returns true 已选择，false 未选择
	 */
	editor.selectParentElement = function(ele, tip)
	{
		//(true)、(false)
		if(arguments.length == 1 && (ele === true || ele === false))
		{
			tip = ele;
			ele = undefined;
		}
		
		tip = (tip == null ? true : tip);
		
		this._removeElementClassNewInsert();
		
		ele = this._refElement(ele);
		
		if(ele.is("body"))
		{
			if(tip)
				this.tipInfo(i18n.noSelectableParentElement);
			return false;
		}
		
		var target = ele;
		while((target = target.parent()))
		{
			if(target.length == 0 || target.is("body")
				|| (target.attr(ELEMENT_ATTR_VISUAL_EDIT_ID) && !target.is("script,style") && !target.is(":hidden")))
			{
				break;
			}
		}
		
		if(target.is("body") || target.length == 0)
		{
			if(tip)
				this.tipInfo(i18n.noSelectableParentElement);
			return;
		}
		
		this._deselectAllElement();
		this._selectElement(target);
		return true;
	};
	
	/**
	 * 取消选择元素。
	 * 
	 * @param ele 可选，元素，默认为：当前选中元素、或者<body>元素
	 */
	editor.deselectElement = function(ele)
	{
		this._removeElementClassNewInsert();
		
		ele = this._refElement(ele);
		this._deselectElement(ele);
	};
	
	/**
	 * 是否是图表元素。
	 * 
	 * @param ele 可选，元素，默认为：当前选中元素
	 */
	editor.isChartElement = function(ele)
	{
		ele = this._refElement(ele);
		return (this.dashboard.renderedChart(ele) != null);
	};
	
	/**
	 * 校验insertDiv操作。
	 * 
	 * @param insertType 可选，参考insertElement函数的insertType参数
	 * @param refEle 可选，参考insertElement函数的refEle参数
	 */
	editor.checkInsertDiv = function(insertType, refEle)
	{
		return true;
	};
	
	/**
	 * 插入div元素。
	 * 
	 * @param insertType 可选，参考insertElement函数的insertType参数
	 * @param refEle 可选，参考insertElement函数的refEle参数
	 */
	editor.insertDiv = function(insertType, refEle)
	{
		var div = $("<div></div>");
		
		//设置默认尺寸，不然不能显示
		div.attr("style", "height:3em;");
		
		this.insertElement(div, insertType, refEle);
	};
	
	/**
	 * 校验insertChart操作。
	 * 
	 * @param insertType 可选，参考insertChart函数的insertType参数
	 * @param refEle 可选，参考insertChart函数的refEle参数
	 */
	editor.checkInsertChart = function(insertType, refEle)
	{
		refEle = this._refElement(refEle);
		insertType = this._trimInsertType(refEle, insertType);
		
		//图表元素内部不允许再插入图表元素
		if(this.isChartElement(refEle) && (insertType == "append" || insertType == "prepend"))
		{
			this.tipInfo(i18n.insertInsideChartOnChartEleDenied);
			return false;
		}
		else
			return true;
	};
	
	//插入图表元素时的默认元素样式
	editor.defaultInsertChartEleStyle = "";
	
	/**
	 * 插入图表。
	 * 
	 * @param chartWidgets 要插入的图表部件对象、数组
	 * @param insertType 可选，参考insertElement函数的insertType参数
	 * @param refEle 可选，参考insertElement函数的refEle参数
	 */
	editor.insertChart = function(chartWidgets, insertType, refEle)
	{
		if(!chartWidgets || chartWidgets.length == 0)
			return;
		
		chartWidgets = (!$.isArray(chartWidgets) ? [ chartWidgets ] : chartWidgets);
		
		refEle = this._refElement(refEle);
		insertType = this._trimInsertType(refEle, insertType);
		
		//图表元素内部不允许再插入图表元素
		if(!this.checkInsertChart(insertType, refEle))
			return;
		
		var styleStr = "";
		var insertParentEle = null;
		
		if(refEle.is("body"))
			insertParentEle = refEle;
		else if("after" == insertType || "before" == insertType)
			insertParentEle = refEle.parent();
		else
			insertParentEle = refEle;
		
		if(chartFactory.isStaticPosition(insertParentEle) || insertParentEle.is("body"))
			styleStr = this.defaultInsertChartEleStyle;
		else
			styleStr = "position:absolute;left:0;top:0;right:0;bottom:0;";
		
		for(var i=0; i<chartWidgets.length; i++)
		{
			var chartWidget = chartWidgets[i];
			
			var chartDiv = $("<div></div>");
			
			//先设style，与源码模式一致
			if(styleStr)
				chartDiv.attr("style", styleStr);
			
			chartDiv.attr(chartFactory.elementAttrConst.WIDGET, chartWidget.id)
						.html("<!--"+chartWidget.name+"-->");
			
			this.insertElement(chartDiv, insertType, refEle);
		}
		
		this.dashboard.loadUnsolvedCharts();
	};
	
	/**
	 * 校验bindChart操作。
	 *
	 * @param ele 可选，要绑定的图表元素，默认为：当前选中图表元素
	 */
	editor.checkBindChart = function(ele)
	{
		ele = this._refElement(ele, true);
		
		if(!this._checkNotEmptyElement(ele))
			return false;
		
		return true;
	};
	
	/**
	 * 绑定或替换图表。
	 * 
	 * @param chartWidget 要绑定的新图表部件对象
	 * @param ele 可选，要绑定的图表元素，默认为：当前选中图表元素
	 */
	editor.bindChart = function(chartWidget, ele)
	{
		if(!chartWidget)
			return;
		
		ele = this._refElement(ele, true);
		
		if(!this.checkBindChart(ele))
			return;
		
		if(this.isChartElement(ele))
		{
			this.dashboard.removeChart(ele);
		}
		
		this._setElementAttr(ele, chartFactory.elementAttrConst.WIDGET, chartWidget.id);
		this.dashboard.loadChart(ele);
	};
	
	/**
	 * 校验unbindChart操作。
	 *
	 * @param ele 可选，要解绑的图表元素，默认为：当前选中图表元素
	 */
	editor.checkUnbindChart = function(ele)
	{
		ele = this._refElement(ele, true);
		
		if(!this._checkNotEmptyElement(ele))
			return false;
		
		if(!this.isChartElement(ele))
		{
			this.tipInfo(i18n.selectedNotChartElement);
			return false;
		}
		
		return true;
	};
	
	/**
	 * 解绑图表。
	 * 
	 * @param ele 可选，要解绑的图表元素，默认为：当前选中图表元素
	 */
	editor.unbindChart = function(ele)
	{
		ele = this._refElement(ele, true);
		
		if(!this.checkUnbindChart(ele))
			return;
		
		this.dashboard.removeChart(ele);
		this._removeElementAttr(ele, chartFactory.elementAttrConst.WIDGET);
	};
	
	/**
	 * 插入元素。
	 * 
	 * @param insertEle 要插入的jq元素、HTML文本，不要使用"<div />"的格式，可能导致编辑HTML代码格式不对
	 * @param insertType 可选，插入类型："after"、"before"、"append"、"prepend"，默认为："after"
	 * @param refEle 插入参照元素，默认为：当前选中元素，或者<body>
	 * @param sync 可选，是否将插入操作同步至编辑iframe中，默认为：true
	 */
	editor.insertElement = function(insertEle, insertType, refEle, sync)
	{
		refEle = this._refElement(refEle);
		insertType = this._trimInsertType(refEle, insertType);
		sync = (sync == null ? true : sync);
		
		if(chartFactory.isString(insertEle))
			insertEle = $(insertEle);
		
		insertEle.attr(ELEMENT_ATTR_VISUAL_EDIT_ID, this._nextVisualEditId());
		
		this._insertElement(refEle, insertEle, insertType);
		
		if(sync)
		{
			var editEle = this._editElement(refEle);
			var insertEleClone = insertEle.clone();
			this._insertElement(editEle, insertEleClone, insertType);
		}
		
		insertEle.addClass(ELEMENT_CLASS_NEW_INSERT);
		this._hasElementClassNewInsert = true;
		
		this.changeFlag(true);
	};
	
	/**
	 * 获取元素文本内容。
	 * 
	 * @param ele 可选，元素，默认为：当前选中元素
	 */
	editor.getElementText = function(ele)
	{
		ele = this._refElement(ele);
		return $.trim(ele.text());
	};
	
	/**
	 * 校验setElementText操作。
	 *
	 * @param ele 可选，元素，默认为：当前选中元素
	 */
	editor.checkSetElementText = function(ele)
	{
		ele = this._refElement(ele, true);
		
		if(!this._checkNotEmptyElement(ele))
			return false;
		
		var firstChild = $("> *:first-child", ele);
		
		if(firstChild.length > 0)
		{
			this.tipInfo(i18n.canEditOnlyTextElement);
			return false;
		}
		
		return true;
	};
	
	/**
	 * 设置元素文本内容。
	 * 
	 * @param text 要设置的文本内容
	 * @param ele 可选，元素，默认为：当前选中元素
	 */
	editor.setElementText = function(text, ele)
	{
		ele = this._refElement(ele, true);
		
		if(!this.checkSetElementText(ele))
			return;
		
		this._setElementText(ele, text);
	};
	
	/**
	 * 校验deleteElement操作。
	 * 
	 * @param ele 可选，元素，默认为：当前选中元素
	 */
	editor.checkDeleteElement = function(ele)
	{
		ele = this._refElement(ele, true);
		
		if(!this._checkNotEmptyElement(ele))
			return false;
		
		return true;
	};
	
	/**
	 * 删除元素。
	 * 
	 * @param ele 可选，元素，默认为：当前选中元素
	 */
	editor.deleteElement = function(ele)
	{
		ele = this._refElement(ele, true);
		
		if(!this.checkDeleteElement(ele))
			return;
		
		//TODO 如果元素内包含图表元素，应先销毁它们
		
		var iframeEle = this._editElement(ele);
		
		ele.remove();
		iframeEle.remove();
		
		this.changeFlag(true);
	};
	
	/**
	 * 校验setElementStyle操作。
	 * 
	 * @param ele 可选，元素，默认为：当前选中元素
	 */
	editor.checkSetElementStyle = function(ele)
	{
		ele = this._refElement(ele, true);
		
		if(!this._checkNotEmptyElement(ele))
			return false;
		
		return true;
	};
	
	/**
	 * 设置元素样式。
	 * 
	 * @param styleObj 要设置的样式对象，格式为：{ 'color': '...', 'background-color': '...' }
	 * @param ele 可选，元素，默认为：当前选中元素
	 */
	editor.setElementStyle = function(styleObj, ele)
	{
		ele = this._refElement(ele, true);
		
		if(!this.checkSetElementStyle(ele))
			return;
		
		var so = this._spitStyleAndOption(styleObj);
		
		this._setElementStyle(ele, so.style);
		
		if(so.option.syncChartTheme && this.isChartElement(ele))
		{
			var chartTheme = this._evalElementChartThemeByStyleObj(ele, so.style);
			this.setElementChartTheme(chartTheme, ele);
		}
	};
	
	/**
	 * 获取元素样式对象。
	 * 
	 * @param ele 可选，元素，默认为：当前选中元素
	 */
	editor.getElementStyle = function(ele)
	{
		ele = this._refElement(ele, true);
		return this._getElementStyleObj(ele);
	};
	
	/**
	 * 设置全局样式（body）。
	 * 
	 * @param styleObj 要设置的样式对象，格式为：{ 'color': '...', 'background-color': '...' }
	 */
	editor.setGlobalStyle = function(styleObj)
	{
		var so = this._spitStyleAndOption(styleObj);
		
		this._setElementStyle($(document.body), so.style);
		
		if(so.style.color)
		{
			this._setPageStyle(
			{
				selectedBorderColor: so.style.color
			});
		}
		
		if(so.option.syncChartTheme)
		{
			var chartTheme = this._evalElementChartThemeByStyleObj($(document.body), so.style);
			this.setGlobalChartTheme(chartTheme);
		}
	};
	
	/**
	 * 获取全局样式对象（body）。
	 */
	editor.getGlobalStyle = function()
	{
		return this._getElementStyleObj($(document.body));
	};
	
	editor._spitStyleAndOption = function(styleObj)
	{
		var optionObj =
		{
			syncChartTheme: (styleObj.syncChartTheme == true || styleObj.syncChartTheme == "true")
		};
		
		var plainStyleObj = $.extend({}, styleObj);
		plainStyleObj.syncChartTheme = undefined;
		
		var re =
		{
			style: plainStyleObj,
			option: optionObj
		};
		
		return re;
	};
	
	editor._evalElementChartThemeByStyleObj = function(ele, styleObj)
	{
		var nowTheme = this._getElementChartTheme(ele);
		var styleTheme = {};
		
		var color = styleObj.color;
		var bgColor = styleObj['background-color'];
		
		if(color || bgColor)
		{
			if(color)
				styleTheme.color = color;
			
			//只有之前设置了图表背景色且不是透明的才需要同步
			if(bgColor && nowTheme && nowTheme.backgroundColor
					&& nowTheme.backgroundColor != "transparent")
				styleTheme.backgroundColor = bgColor;
			
			if(bgColor && bgColor != "transparent")
				styleTheme.actualBackgroundColor = bgColor;
		}
		
		if(!nowTheme)
		{
			return styleTheme;
		}
		else
		{
			nowTheme.color = (styleTheme.color ? styleTheme.color : undefined);
			nowTheme.backgroundColor = (styleTheme.backgroundColor ? styleTheme.backgroundColor : undefined);
			nowTheme.actualBackgroundColor = (styleTheme.actualBackgroundColor ? styleTheme.actualBackgroundColor : undefined);
			
			return nowTheme;
		}
	};
	
	/**
	 * 校验setElementChartTheme操作。
	 * 
	 * @param ele 可选，元素，默认为：当前选中元素
	 */
	editor.checkSetElementChartTheme = function(ele)
	{
		ele = this._refElement(ele, true);
		
		if(!this._checkNotEmptyElement(ele))
			return false;
		
		var chart = this.dashboard.renderedChart(ele);
		
		if(!chart)
		{
			this.tipInfo(i18n.selectedNotChartElement);
			return false;
		}
		
		return true;
	};
	
	/**
	 * 设置元素图表主题。
	 * 
	 * @param chartTheme 要设置的图表主题对象，格式为：{ 'color': '...', 'backgroundColor': '...', ... }
	 * @param ele 可选，元素，默认为：当前选中元素
	 */
	editor.setElementChartTheme = function(chartTheme, ele)
	{
		ele = this._refElement(ele, true);
		
		if(!this.checkSetElementChartTheme(ele))
			return;
		
		var chart = this.dashboard.renderedChart(ele);
		
		this._setElementChartTheme(ele, chartTheme);
		
		chart.destroy();
		chart.init();
	};
	
	/**
	 * 获取元素图表主题。
	 * 
	 * @param ele 可选，元素，默认为：当前选中元素
	 */
	editor.getElementChartTheme = function(ele)
	{
		ele = this._refElement(ele, true);
		return this._getElementChartTheme(ele);
	};
	
	/**
	 * 设置全局图表主题。
	 * 
	 * @param chartTheme 要设置的图表主题对象，格式为：{ 'color': '...', 'backgroundColor': '...', ... }
	 */
	editor.setGlobalChartTheme = function(chartTheme)
	{
		this._setElementChartTheme($(document.body), chartTheme);
		
		this.dashboard.destroy();
		this.dashboard.render();
	};
	
	/**
	 * 获取全局图表主题。
	 */
	editor.getGlobalChartTheme = function()
	{
		return this._getElementChartTheme($(document.body));
	};
	
	/**
	 * 校验元素不为空。
	 *
	 * @param ele
	 */
	editor._checkNotEmptyElement = function(ele)
	{
		if(this._isEmptyElement(ele))
		{
			this.tipInfo(i18n.selectedElementRequired);
			return false;
		}
		
		return true;
	};
	
	editor._getElementChartTheme = function(ele)
	{
		var themeStr = ele.attr(chartFactory.elementAttrConst.THEME);
		
		if(!themeStr)
			return null;
		
		return chartFactory.evalSilently(themeStr, {});
	};
	
	editor._setElementChartTheme = function(ele, chartTheme, sync)
	{
		chartTheme = $.extend(true, {}, chartTheme); 
		
		if(chartFactory.isString(chartTheme.graphColors))
			chartTheme.graphColors = this._spitIgnoreEmpty(chartTheme.graphColors);
		if(chartFactory.isString(chartTheme.graphRangeColors))
			chartTheme.graphRangeColors = this._spitIgnoreEmpty(chartTheme.graphRangeColors);
		
		var trim = {};
		
		for(var p in chartTheme)
		{
			var v = chartTheme[p];
			
			if(v == null)
				;
			else if(chartFactory.isString(v) && v != "")
				trim[p] = v;
			else if($.isArray(v) && v.length > 0)
				trim[p] = v;
		}
		
		var attrValue = this._serializeForAttrValue(trim);
		
		if(attrValue == "{}")
			this._removeElementAttr(ele, chartFactory.elementAttrConst.THEME);
		else
			this._setElementAttr(ele, chartFactory.elementAttrConst.THEME, attrValue, sync);
	};
	
	editor._setElementStyle = function(ele, styleObj, sync)
	{
		styleObj = (styleObj || {});
		sync = (sync == null ? true : sync);
		
		this._setElementStyleNoSync(ele, styleObj);
		
		if(sync)
		{
			var editEle = this._editElement(ele);
			this._setElementStyleNoSync(editEle, styleObj);
		}
		
		this.changeFlag(true);
	};
	
	editor._setElementStyleNoSync = function(ele, styleObj)
	{
		//这里不能采用整体设置"style"属性的方式，因为"style"属性可能有很多不支持编辑的、或者动态生成的css属性，
		//它们应该被保留，且不能同步至对应的编辑元素上
		
		var nowStyleObj = chartFactory.styleStringToObj(chartFactory.elementStyle(ele) || "");
		
		for(var name in styleObj)
		{
			var value = styleObj[name];
			
			if(value == null || value == "")
				delete nowStyleObj[name];
			else
			{
				if(name == "background-image")
				{
					//不是"url(...)"格式
					if(/^url\(/i.test(value) != true)
						value = "url(" + value +")"
				}
				
				nowStyleObj[name] = value;
			}
		}
		
		if($.isEmptyObject(nowStyleObj))
			ele.removeAttr("style");
		else
			chartFactory.elementStyle(ele, nowStyleObj);
	};
	
	editor._getElementStyleObj = function(ele)
	{
		var newStyleObj = {};
		
		var styleObj = chartFactory.styleStringToObj(chartFactory.elementStyle(ele));
		
		//先处理复合css，因为它们应是低优先级
		for(var p in styleObj)
		{
			if(p == "inset")
			{
				this._resolveSetStyleInset(newStyleObj, styleObj[p]);
			}
			else if(p == "background")
			{
				this._resolveSetStyleBackground(newStyleObj, styleObj[p]);
			}
		}
		
		for(var p in styleObj)
		{
			if(this._editableElementStyles[p] && styleObj[p])
				newStyleObj[p] = styleObj[p];
		}
		
		return newStyleObj;
	};
	
	//将css的background属性转换为background-color、background-image等属性
	editor._resolveSetStyleBackground = function(styleObj, background)
	{
		if(!background)
			return;
		
		var ary = background.split(" ");
		var beforePositionSizeSplitter = true;
		var bgPositionCount = 0, bgSizeCount = 0;
		
		for(var i=0; i<ary.length; i++)
		{
			var v = ary[i];
			
			// "background-position / background-size"
			if("/" == v)
			{
				beforePositionSizeSplitter = false;
			}
			else if(/^url\(/i.test(v))
			{
				styleObj["background-image"] = v;
			}
			else if(/^(\#|rgb)/.test(v))
			{
				styleObj["background-color"] = v;
			}
			else if(/^(no\-repeat|repeat|repeat\-x|repeat\-y)$/i.test(v))
			{
				styleObj["background-repeat"] = v;
			}
			else if(beforePositionSizeSplitter && bgPositionCount < 2 && (/^(left|right|top|bottom|center)$/i.test(v) || /^\d/.test(v)))
			{
				styleObj["background-position"] = (bgPositionCount == 0 ? v : styleObj["background-position"]+" "+v);
				bgPositionCount++;
			}
			else if(!beforePositionSizeSplitter && bgSizeCount < 2 && (/^(auto|cover|contain)$/i.test(v) || /^\d/.test(v)))
			{
				styleObj["background-size"] = (bgSizeCount == 0 ? v : styleObj["background-size"]+" "+v);
				bgSizeCount++;
			}
			// 颜色单词
			else if(i == 0 && !styleObj["background-color"] && /^[a-zA-Z]/.test(v))
			{
				styleObj["background-color"] = v;
			}
		}
	};
	
	//将css的inset属性转换为top、left、right、bottom属性
	editor._resolveSetStyleInset = function(styleObj, inset)
	{
		if(!inset)
			return;
		
		var ary = inset.split(" ");
		
		if(ary.length == 0)
			return;
		
		if(ary.length == 1)
		{
			ary[1] = ary[0];
			ary[2] = ary[0];
			ary[3] = ary[0];
		}
		else if(ary.length == 2)
		{
			ary[2] = ary[0];
			ary[3] = ary[1];
		}
		else if(ary.length == 3)
		{
			ary[3] = ary[1];
		}
		
		styleObj["top"] = ary[0];
		styleObj["right"] = ary[1];
		styleObj["bottom"] = ary[2];
		styleObj["left"] = ary[3];
	};
	
	editor._editableElementStyles =
	{
		"color": true,
		"background-color": true,
		"background-image": true,
		"background-position": true,
		"background-size": true,
		"background-repeat": true,
		"display": true,
		"width": true,
		"height": true,
		"padding": true,
		"margin": true,
		"position": true,
		"left": true,
		"top": true,
		"right": true,
		"bottom": true,
		"font-size": true,
		"font-weight": true,
		"text-align": true
	};
	
	//设置元素文本内容
	editor._setElementText = function(ele, text, sync)
	{
		text = (text || "");
		sync = (sync == null ? true : sync);
		
		ele.text(text);
		
		if(sync)
		{
			var editEle = this._editElement(ele);
			editEle.text(text);
		}
		
		this.changeFlag(true);
	};
	
	//设置元素属性
	editor._setElementAttr = function(ele, name, value, sync)
	{
		sync = (sync == null ? true : sync);
		
		ele.attr(name, value);
		
		if(sync)
		{
			var editEle = this._editElement(ele);
			editEle.attr(name, value);
		}
		
		this.changeFlag(true);
	};
	
	//删除元素属性
	editor._removeElementAttr = function(ele, name, sync)
	{
		sync = (sync == null ? true : sync);
		
		ele.removeAttr(name);
		
		if(sync)
		{
			var editEle = this._editElement(ele);
			editEle.removeAttr(name);
		}
		
		this.changeFlag(true);
	};
	
	editor._refElement = function(refEle, excludeBody)
	{
		excludeBody = (excludeBody == null ? false : excludeBody);
		
		refEle = (this._isEmptyElement(refEle) ? this._selectedElement() : refEle);
		
		if(!excludeBody)
			refEle = (this._isEmptyElement(refEle) ? $(document.body) : refEle);
		
		return refEle;
	};
	
	editor._insertElement = function(refEle, insertEle, insertType)
	{
		if(insertType == "after")
		{
			refEle.after(insertEle);
			refEle.after("\n");
		}
		else if(insertType == "before")
		{
			refEle.before(insertEle);
			refEle.before("\n");
		}
		else if(insertType == "append")
		{
			refEle.append(insertEle);
			refEle.append("\n");
		}
		else if(insertType == "prepend")
		{
			refEle.prepend(insertEle);
			refEle.prepend("\n");
		}
	};
	
	editor._trimInsertType = function(refEle, insertType)
	{
		insertType = (!insertType ? "after" : insertType);
		insertType = (insertType == "after" || insertType == "before"
						|| insertType == "append" || insertType == "prepend" ? insertType : "after");
		
		if(refEle.is("body"))
		{
			if(insertType == "after")
				insertType = "append";
			else if(insertType == "before")
				insertType = "prepend";
		}
		
		return insertType;
	};
	
	editor._selectedElement = function()
	{
		return $("."+ELEMENT_CLASS_SELECTED);
	};
	
	editor._isSelectedElement = function($ele)
	{
		return $ele.hasClass(ELEMENT_CLASS_SELECTED);
	};
	
	editor._selectElement = function($ele)
	{
		$ele.addClass(ELEMENT_CLASS_SELECTED);
	};
	
	editor._deselectElement = function($ele)
	{
		$ele.removeClass(ELEMENT_CLASS_SELECTED);
	};
	
	editor._deselectAllElement = function()
	{
		$("."+ELEMENT_CLASS_SELECTED).removeClass(ELEMENT_CLASS_SELECTED);
	};
	
	editor._removeElementClassNewInsert = function()
	{
		if(this._hasElementClassNewInsert)
		{
			$("."+ELEMENT_CLASS_NEW_INSERT).removeClass(ELEMENT_CLASS_NEW_INSERT);
			this._hasElementClassNewInsert = false;
		}
	};
	
	editor._isEmptyElement = function(ele)
	{
		return (ele == null || ele.length == 0);
	};
	
	editor._getVisualEditId = function($ele)
	{
		return $ele.attr(ELEMENT_ATTR_VISUAL_EDIT_ID);
	};
	
	editor._nextVisualEditId = function()
	{
		return chartFactory.uid();
	};
	
	/**
	 * 设置编辑页面样式。
	 *
	 * @param options 可选，格式为：{ selectedBorderColor: "..." }
	 */
	editor._setPageStyle = function(options)
	{
		options = $.extend(
		{
			selectedBorderColor: $(document.body).css("color")
		},
		options);
		
		chartFactory.styleSheetText("dg-show-ve-style",
			"\n"
			+ ".dg-show-ve .dg-show-ve-selected{\n"
			+ "  border-color: " + options.selectedBorderColor + " !important;"
			+ "\n}\n");
	};
	
	//获取编辑HTML信息
	//结构参考：org.datagear.web.controller.DashboardController.DashboardShowForEdit.EditHtmlInfo
	editor._editHtmlInfo = function()
	{
		return this.dashboard.renderContextAttr(DASHBOARD_BUILTIN_RENDER_CONTEXT_ATTR_EDIT_HTML_INFO);
	};
	
	//反转义编辑HTML（转义操作由后台执行）
	editor._unescapeEditHtml = function(editHtml)
	{
		return (editHtml ? editHtml.replaceAll("<\\/", "</") : editHtml);
	};
	
	editor._spitIgnoreEmpty = function(str, splitter)
	{
		splitter = (splitter ? splitter : ",");
		
		var ary = [];
		
		if(!str)
			return ary;
		
		ary = str.split(splitter);
		
		var re = [];
		
		for(var i=0; i<ary.length; i++)
		{
			var ele = $.trim(ary[i]);
			if(ele)
				re.push(ele);
		}
		
		return re;
	};
	
	editor._serializeForAttrValue = function(obj)
	{
		if(obj == null)
			return null;
		
		var type = typeof(obj);
		
		if(type == "string")
			return "'" + obj.replace(/\'/g, "\\'") + "'";
		else if(type == "number")
			return obj.toString();
		else if(type == "boolean")
			return obj.toString();
		else if($.isArray(obj))
		{
			var str = "[";
			
			for(var i=0; i<obj.length; i++)
			{
				if(i > 0)
					str += ",";
				
				str += this._serializeForAttrValue(obj[i]);
			}
			
			str += "]";
			
			return str;
		}
		else if($.isPlainObject(obj))
		{
			var str = "{";
			
			for(var p in obj)
			{
				if(str != "{")
					str += ",";
				
				var v = this._serializeForAttrValue(obj[p]);
				
				if(v != null)
					str += this._serializeForAttrValue(p) + ":" + v;
			}
			
			str += "}";
			
			return str;
		}
		else
			return obj.toString();
	};
	
	/**
	 * 获取编辑iframe，也可设置其HTML。
	 * 
	 * 这里的editBodyHtml应只使用"<body>...</body>"，因为渲染iframe页面时，如果"<body>"前、"</body>"后里有不合规的元素，
	 * 可能会被渲染至<body></body>内，导致【结果HTML】还原不对。
	 * 
	 * @param editBodyHtml 
	 */
	editor._editIframe = function(editBodyHtml)
	{
		var id = (this._editIframeId != null ? this._editIframeId
					: (this._editIframeId = chartFactory.uid()));
		
		var iframe = $("#" + id);
		
		if(iframe.length == 0)
		{
			iframe = $("<iframe class='dg-edit-html-ifm' style='display:none;'></iframe>")
				.attr("name", id).attr("id", id).appendTo(document.body);
		}
		
		iframe = iframe[0];
		
		if(editBodyHtml != null)
		{
			//使用这种方式会导致浏览器一至处于加载中的状态，所以改为后面的方式
			//this._editDocument(iframe).write("<html><head></head>" + editBodyHtml + "</html>");
			
			var editIframeBodyHtml = this._toEditIframeBodyHtml(editBodyHtml);
			var editDoc = this._editDocument();
			$(editDoc.body).html(editIframeBodyHtml);
			
			this.changeFlag(true);
		}
		
		return iframe;
	};
	
	//将"<body>...</body>"转换为"<div>...</div>"，使得可以直接使用：$(document.body).html("...");
	editor._toEditIframeBodyHtml = function(editBodyHtml)
	{
		var startTagRegex = /^\s*<body/i;
		var endTagRegex = /\/body>\s*$/i;
		
		var editIframeBodyHtml = editBodyHtml.replace(startTagRegex, "<div");
		editIframeBodyHtml = editIframeBodyHtml.replace(endTagRegex, "/div>");
		
		return editIframeBodyHtml;
	};
	
	//将由editor._toEditIframeBodyHtml()转换的"<div>...</div>"恢复为"<body>...</body>"
	editor._fromEditIframeBodyHtml = function(editIframeBodyHtml)
	{
		var startTagRegex = /^\s*<div/i;
		var endTagRegex = /\/div>\s*$/i;
		
		var editBodyHtml = editIframeBodyHtml.replace(startTagRegex, "<body");
		editBodyHtml = editBodyHtml.replace(endTagRegex, "/body>");
		
		return editBodyHtml;
	};
	
	/**
	 * 获取编辑iframe的document对象。
	 */
	editor._editDocument = function(iframe)
	{
		iframe = (iframe == null ? this._editIframe() : iframe);
		
		return (iframe.contentDocument || iframe.contentWindow.document);
	};
	
	//获取编辑HTML的<body>...</body>内容
	editor._editBodyHtml = function()
	{
		var editDoc = this._editDocument();
		var editIframeBodyHtml = $(editDoc.body).html();
		
		return this._fromEditIframeBodyHtml(editIframeBodyHtml);
	};
	
	/**
	 * 获取编辑iframe中的元素。
	 * 
	 * @param $ele 展示元素
	 */
	editor._editElement = function($ele)
	{
		var editDoc = this._editDocument();
		
		if($ele.is("body"))
		{
			// <body>被转换为了<div>，参考editor._toEditIframeBodyHtml()函数
			return $("> div", editDoc.body);
		}
		
		var editId = (this._getVisualEditId($ele) || "");
		return $("["+ELEMENT_ATTR_VISUAL_EDIT_ID+"='"+editId+"']", editDoc.body);
	};
	
	editor._evalTopWindowSize = function()
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
})
(this);
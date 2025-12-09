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
 * 看板可视编辑器。
 * 全局变量名：window.dashboardFactory.dashboardEditor
 * 
 * 加载时依赖：
 *   chartFactory.js
 *   dashboardFactory.js
 * 
 * 运行时依赖:
 * 
 */
(function(global)
{
	/**看板工厂*/
	var CF = global.chartFactory;
	var DF = global.dashboardFactory;
	var renderContextAttrConst = CF.renderContextAttrConst;
	var DE = (DF.dashboardEditor || (DF.dashboardEditor = {}));
	var i18n = (DE.i18n || (DE.i18n = {}));
	
	i18n.insertInsideChartOnChartEleDenied = "图表元素内不允许再插入图表元素";
	i18n.selectElementForSetChart = "请选择要设置/替换的图表元素";
	i18n.canEditOnlyTextElement = "仅可编辑纯文本元素";
	i18n.selectedElementRequired = "请选择要操作的元素";
	i18n.selectedNotChartElement = "选定元素不是图表元素";
	i18n.selectedNotHasChartElement = "选定元素不是图表元素，也未包含任何图表元素";
	i18n.noSelectableNextElement="没有可选择的下一个元素";
	i18n.noSelectablePrevElement="没有可选择的上一个元素";
	i18n.noSelectableChildElement="没有可选择的子元素";
	i18n.noSelectableParentElement="没有可选择的父元素";
	i18n.imgEleRequired = "不是图片元素";
	i18n.hyperlinkEleRequired = "不是超链接元素";
	i18n.videoEleRequired = "不是视频元素";
	i18n.iframeEleRequired = "不是内嵌框体元素";
	i18n.labelEleRequired = "不是文本标签元素";
	i18n.chartPluginNoAttrDefined = "此类型图表插件没有定义可编辑属性";
	i18n.bindChartElementMustBeDiv = "绑定图表的元素必须是<div>元素";
	
	//参考org.datagear.web.controller.DashboardVisualController.RENDER_CONTEXT_ATTR_EDIT_HTML_INFO
	var RENDER_CONTEXT_ATTR_EDIT_HTML_INFO = (DE.RENDER_CONTEXT_ATTR_EDIT_HTML_INFO = "DG_EDIT_HTML_INFO");
	
	var BODY_CLASS_VISUAL_EDITOR = (DE.BODY_CLASS_VISUAL_EDITOR = "dg-show-ve");
	
	//参考org.datagear.web.controller.DashboardVisualController.DashboardShowForEdit.ELEMENT_ATTR_VISUAL_EDIT_ID
	var ELEMENT_ATTR_VISUAL_EDIT_ID = (DE.ELEMENT_ATTR_VISUAL_EDIT_ID = "dg-visual-edit-id");
	
	var ELEMENT_CLASS_SELECTED = (DE.ELEMENT_CLASS_SELECTED = "dg-show-ve-selected");
	
	var ELEMENT_CLASS_NEW_INSERT = (DE.ELEMENT_CLASS_NEW_INSERT = "dg-show-ve-new-insert");
	
	var BODY_CLASS_ELEMENT_BOUNDARY = (DE.BODY_CLASS_ELEMENT_BOUNDARY = "dg-show-ve-boundary");
	
	var SHOW_BODY_CLASS_FLAG = (DE.SHOW_BODY_CLASS_FLAG = "dg-show-html-body");
	var EDIT_BODY_CLASS_FLAG = (DE.EDIT_BODY_CLASS_FLAG = "dg-edit-html-body");
	
	var INSERT_ELE_FORMAT_START = (DE.INSERT_ELE_FORMAT_START = "<!--dgInsertFmtStart-->");
	var INSERT_ELE_FORMAT_END = (DE.INSERT_ELE_FORMAT_END = "<!--dgInsertFmtEnd-->");
	var DELETE_ELE_FORMAT_FLAG = (DE.DELETE_ELE_FORMAT_FLAG = "<!--dgDeleteFmtFlag-->");
	
	//参考org.datagear.web.controller.DashboardVisualController.LOAD_CHART_FOR_EDITOR_PARAM
	var LOAD_CHART_FOR_EDITOR_PARAM = (DE.LOAD_CHART_FOR_EDITOR_PARAM = "loadChartForEditor");
	
	var INSERT_TYPE_APPEND = (DE.INSERT_TYPE_APPEND = "append");
	var INSERT_TYPE_PREPEND = (DE.INSERT_TYPE_PREPEND = "prepend");
	var INSERT_TYPE_AFTER = (DE.INSERT_TYPE_AFTER = "after");
	var INSERT_TYPE_BEFORE = (DE.INSERT_TYPE_BEFORE = "before");
	
	//响应式布局断点
	var RESPONSIVE_BREAKPOINTS = (DE.RESPONSIVE_BREAKPOINTS = [ "xs", "sm", "md", "lg", "xl", "2xl" ]);
	//响应式布局名称，详细参考analysis.css中的【.dg-rsp-布局名称[-断点]-布局值】样式类定义
	var RESPONSIVE_LAYOUT_NAMES = (DE.RESPONSIVE_LAYOUT_NAMES = [ "col", "h", "d" ]);
	
	DF._createSuperByDbdEditor = DF.create;
	DF.create = function(root)
	{
		var dashboard = DF._createSuperByDbdEditor(root);
		DE.init(dashboard);
		
		return dashboard;
	};
	
	/**
	 * 初始化可视编辑器。
	 */
	DE.init = function(dashboard)
	{
		DE.dashboard = dashboard;
		
		DE._initRenderContext();
		DE._initStyle();
		DE._initEditHtmlIframe();
		DE._initInteraction();
	};
	
	DE._initRenderContext = function()
	{
		var renderContext = DE.dashboard.renderContext();
		var loadChartURL = CF.renderContextValNonNull(renderContext, renderContextAttrConst.LOAD_CHART_URL);
		loadChartURL = CF.appendUrlParam(loadChartURL, LOAD_CHART_FOR_EDITOR_PARAM, "true");
		CF.renderContextValue(renderContext, renderContextAttrConst.LOAD_CHART_URL, loadChartURL);
	};
	
	///初始化样式。
	DE._initStyle = function()
	{
		DE._setPageStyle();
	};
	
	//初始化编辑HTML的iframe
	DE._initEditHtmlIframe = function()
	{
		var editHtmlInfo = DE._editHtmlInfo();
		var editBodyHtml = DE._unescapeEditHtml(editHtmlInfo.bodyHtml);
		DE._editIframe(editBodyHtml);
	};
	
	//初始化交互控制
	DE._initInteraction = function()
	{
		CF.eleOn(document, "DOMContentLoaded", () =>
		{
			CF.eleAddClass(document.body, [ BODY_CLASS_VISUAL_EDITOR, SHOW_BODY_CLASS_FLAG]);
			
			CF.eleOn(document.body, "click", (event) =>
			{
				var veEle = CF.eleAncestorOfSelector(event.target, "["+ELEMENT_ATTR_VISUAL_EDIT_ID+"]");
				
				if(veEle == null)
				{
					DE.deselectElement();
				}
				else
				{
					if(!DE._isSelectableElement(veEle))
					{
						DE.deselectElement();
					}
					else
					{
						DE.selectElement(veEle);
					}
				}
				
				if(DE.clickCallback)
					DE.clickCallback(event);
			});
			
			CF.eleOn(window, "beforeunload", () =>
			{
				DE.beforeunloadCallback();
			});
		});
	};
	
	//获取当前编辑HTML
	DE.editedHtml = function()
	{
		var editHtmlInfo = DE._editHtmlInfo();
		var editBodyHtml = DE._editBodyHtml();
		
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
		
		//处理插入元素后又删除元素遗留的格式符：INSERT_ELE_FORMAT_START...INSERT_ELE_FORMAT_END...DELETE_ELE_FORMAT_FLAG
		var insertFormatRegex0 = /\<\!\-\-dgInsertFmtStart\-\-\>\s*\<\!\-\-dgInsertFmtEnd\-\-\>\s*<\!\-\-dgDeleteFmtFlag\-\-\>/gi;
		editBodyHtml = editBodyHtml.replace(insertFormatRegex0, "");
		
		//处理插入元素时的格式符：INSERT_ELE_FORMAT_START
		var insertFormatRegex1 = /\<\!\-\-dgInsertFmtStart\-\-\>/gi;
		editBodyHtml = editBodyHtml.replace(insertFormatRegex1, "");
		
		//处理插入元素时的格式符：INSERT_ELE_FORMAT_END
		var insertFormatRegex2 = /\<\!\-\-dgInsertFmtEnd\-\-\>/gi;
		editBodyHtml = editBodyHtml.replace(insertFormatRegex2, "");
		
		//处理删除元素时的格式符：DELETE_ELE_FORMAT_FLAG
		var deleteFormatRegex0 = /\s*\<\!\-\-dgDeleteFmtFlag\-\-\>/gi;
		editBodyHtml = editBodyHtml.replace(deleteFormatRegex0, "");
		
		var editedHtml = editHtmlInfo.beforeBodyHtml + editBodyHtml + editHtmlInfo.afterBodyHtml;
		return DE._unescapeEditHtml(editedHtml);
	};
	
	/**
	 * 获取看板API版本号。
	 */
	DE.dashboardApiVersion = function()
	{
		return this.dashboard.apiVersion();
	};
	
	/**
	 * 是否在指定changeFlag后有修改。
	 *
	 * @param changeFlag 待比较的变更标识
	 */
	DE.isChanged = function(changeFlag)
	{
		return (DE.changeFlag() != changeFlag);
	};
	
	/**
	 * 获取/设置变更标识。
	 *
	 * @param set 可选，要设置的变更标识，格式为：true 自增，数值 设置明确值
	 */
	DE.changeFlag = function(set)
	{
		if(DE._changeFlag == null)
			DE._changeFlag = 0;
		
		if(set == true)
		{
			DE._changeFlag++;
		}
		else if(CF.isNumber(set))
		{
			DE._changeFlag = set;
		}
		else
		{
			return DE._changeFlag;
		}
	};
	
	//提示信息
	DE.tipInfo = function(msg)
	{
		alert(msg);
	};
	
	//页面点击回调函数，格式为：function(event){}
	DE.clickCallback = function(event){};
	
	/**
	 * 选择元素回调函数。
	 * 
	 * @param ele 选中HTML元素
	 */
	DE.selectElementCallback = function(ele){};
	
	/**
	 * 取消选择元素回调函数。
	 * 
	 * @param ele 取消厕HTML元素、null
	 */
	DE.deselectElementCallback = function(ele){};
	
	//页面卸载前回调函数，比如：保存编辑HTML
	DE.beforeunloadCallback = function(){};
	
	/**
	 * 获取/设置元素边界线启用禁用/状态。
	 *
	 * @param enable 可选，true 启用；false 禁用。
	 * @returns 是否已启用 
	 */
	DE.enableElementBoundary = function(enable)
	{
		if(arguments.length == 0)
			return CF.eleHasClass(document.body, BODY_CLASS_ELEMENT_BOUNDARY);
		
		if(enable)
			CF.eleAddClass(document.body, BODY_CLASS_ELEMENT_BOUNDARY);
		else
			CF.eleRemoveClass(document.body, BODY_CLASS_ELEMENT_BOUNDARY);
	};
	
	/**
	 * 是否未选中任何元素。
	 */
	DE.isNonSelectedElement = function()
	{
		var selected = DE._selectedElement();
		return (selected == null);
	};
	
	/**
	 * 获取元素的可视编辑ID。
	 * 
	 * @param ele 可选，元素，默认为：当前选中元素
	 */
	DE.getElementVisualEditId = function(ele)
	{
		ele = DE._currentElement(ele, true);
		return DE._getVisualEditId(ele);
	};
	
	/**
	 * 选中指定元素。
	 * 
	 * @param eleOrVisualEditId 元素、元素可编辑ID
	 * @returns true 已选择，false 未选择
	 */
	DE.selectElement = function(eleOrVisualEditId)
	{
		var ele = eleOrVisualEditId;
		
		if(CF.isString(ele))
			ele = CF.eleOfSelector("["+ELEMENT_ATTR_VISUAL_EDIT_ID+"='"+ele+"']");
		
		DE.deselectElement();
		
		if(!DE._isEmptyElement(ele))
		{
			DE._removeElementClassNewInsert(ele);
			DE._selectElement(ele);
			
			if(DE.selectElementCallback)
				DE.selectElementCallback(ele);
			
			return true;
		}
		
		return false;
	};
	
	/**
	 * 取消选中元素，将根元素内的全部选中元素设为未选。
	 * 
	 * @param root 可选，要处理的根元素，默认为：document.body
	 */
	DE.deselectElement = function(root)
	{
		var eles = DE._selectedElements(root);
		
		for(let i=0; i<eles.length; i++)
		{
			let ele = eles[i];
			
			DE._deselectElement(ele);
			
			if(DE.deselectElementCallback)
				DE.deselectElementCallback(ele);
		}
		
		if(eles.length == 0)
		{
			if(DE.deselectElementCallback)
				DE.deselectElementCallback(null);
		}
	};
	
	/**
	 * 选中下一个可编辑元素。
	 * 如果元素时<body>，将选中其第一个可编辑子元素。
	 * 
	 * @param ele 可选，元素，默认为：当前选中元素、或者<body>元素
	 * @param tip 可选，未选择时是否给出提示，默认为：true
	 * @returns true 已选择，false 未选择
	 */
	DE.selectNextElement = function(ele, tip)
	{
		//(true)、(false)
		if(tip === undefined && (ele === true || ele === false))
		{
			tip = ele;
			ele = undefined;
		}
		
		tip = (tip === undefined ? true : tip);
		ele = DE._currentElement(ele);
		
		if(DE._isBodyEle(ele))
			return DE.selectFirstChildElement(ele, tip);
		
		var target = ele;
		while((target = CF.eleOfNext(target)))
		{
			if(DE._isEmptyElement(target) || DE._isSelectableElement(target))
			{
				break;
			}
		}
		
		if(DE._isEmptyElement(target))
		{
			if(tip)
				DE.tipInfo(i18n.noSelectableNextElement);
			
			return false;
		}
		
		return DE.selectElement(target);
	};
	
	/**
	 * 选中前一个可编辑元素。
	 * 如果元素时<body>，将选中其第一个可编辑子元素。
	 * 
	 * @param ele 可选，元素，默认为：当前选中元素、或者<body>元素
	 * @param tip 可选，未选择时是否给出提示，默认为：true
	 * @returns true 已选择，false 未选择
	 */
	DE.selectPrevElement = function(ele, tip)
	{
		//(true)、(false)
		if(tip === undefined && (ele === true || ele === false))
		{
			tip = ele;
			ele = undefined;
		}
		
		tip = (tip === undefined ? true : tip);
		ele = DE._currentElement(ele);
		
		if(DE._isBodyEle(ele))
			return DE.selectFirstChildElement(ele, tip);
		
		var target = ele;
		while((target = CF.eleOfPrev(target)))
		{
			if(DE._isEmptyElement(target) || DE._isSelectableElement(target))
			{
				break;
			}
		}
		
		if(DE._isEmptyElement(target))
		{
			if(tip)
				DE.tipInfo(i18n.noSelectablePrevElement);
			
			return false;
		}
		
		return DE.selectElement(target);
	};
	
	/**
	 * 选中第一个可编辑子元素。
	 * 
	 * @param ele 可选，元素，默认为：当前选中元素、或者<body>元素
	 * @param tip 可选，未选择时是否给出提示，默认为：true
	 * @returns true 已选择，false 未选择
	 */
	DE.selectFirstChildElement = function(ele, tip)
	{
		//(true)、(false)
		if(tip === undefined && (ele === true || ele === false))
		{
			tip = ele;
			ele = undefined;
		}
		
		tip = (tip === undefined ? true : tip);
		ele = DE._currentElement(ele);
		var firstChild = CF.eleOfFirstChild(ele);
		
		var target = firstChild;
		while(true)
		{
			if(DE._isEmptyElement(target) || DE._isSelectableElement(target))
			{
				break;
			}
			
			target = CF.eleOfNext(target);
		}
		
		if(DE._isEmptyElement(target))
		{
			if(tip)
				DE.tipInfo(i18n.noSelectableChildElement);
			
			return false;
		}
		
		return DE.selectElement(target);
	};
	
	/**
	 * 选中可编辑上级元素。
	 * 
	 * @param ele 可选，元素，默认为：当前选中元素、或者<body>元素
	 * @param tip 可选，未选择时是否给出提示，默认为：true
	 * @returns true 已选择，false 未选择
	 */
	DE.selectParentElement = function(ele, tip)
	{
		//(true)、(false)
		if(tip === undefined && (ele === true || ele === false))
		{
			tip = ele;
			ele = undefined;
		}
		
		tip = (tip === undefined ? true : tip);
		ele = DE._currentElement(ele);
		
		if(DE._isBodyEle(ele))
		{
			if(tip)
				DE.tipInfo(i18n.noSelectableParentElement);
			
			return false;
		}
		
		var target = ele;
		while((target = CF.eleOfParent(target)))
		{
			if(DE._isEmptyElement(target) || DE._isBodyEle(target) || DE._isSelectableElement(target))
			{
				break;
			}
		}
		
		if(DE._isBodyEle(target) || DE._isEmptyElement(target))
		{
			if(tip)
				DE.tipInfo(i18n.noSelectableParentElement);
			
			return false;
		}
		
		return DE.selectElement(target);
	};
	
	DE._isSelectableElement = function(ele)
	{
		if(!CF.eleAttr(ele, ELEMENT_ATTR_VISUAL_EDIT_ID))
			return false;
		
		var tagName = DE._tagNameOfEleLowerCase(ele);
		
		if(CF.isEmpty(tagName))
			return false;
		
		if(tagName == "body")
			return false;
		
		if(!DE._isVisualEleTag(tagName))
			return false;
		
		if(CF.isEleHidden(ele))
			return false;
		
		//没有尺寸的不再忽略，因为插入元素没填内容时，元素本身可能没有尺寸，
		//这样会导致无法选中元素后编辑
		/*
		var w = ele.outerWidth(), h = ele.outerHeight();
		if(w == null || w <= 0 || h == null || h <= 0)
			return false;
		*/
		
		return true;
	};
	
	DE._tagNameOfEleLowerCase = function(ele)
	{
		return (ele && ele.tagName ? ele.tagName : "").toLowerCase();
	};
	
	DE._isVisualEleTag = function(tagNameLowerCase)
	{
		if(tagNameLowerCase == "script" || tagNameLowerCase == "style" || tagNameLowerCase == "template")
		{
			return false;
		}
		else
		{
			return true;
		}
	};
	
	/**
	 * 校验是否有选中元素。
	 */
	DE.checkSelectedElement = function()
	{
		ele = DE._currentElement(null, true);
		
		if(DE._isEmptyElement(ele))
		{
			DE.tipInfo(i18n.selectedElementRequired);
			return false;
		}
		
		return true;
	};
	
	/**
	 * 校验元素本身或其子元素是否有图表元素。
	 * 
	 * @param ele 可选，元素，默认为：当前选中元素
	 */
	DE.checkHasChartElement = function(ele)
	{
		ele = DE._currentElement(ele, true);
		
		if(!DE._checkNotEmptyElement(ele))
			return false;
		
		var chartEles = DE._getChartElements(ele);
		
		if(DE._isEmptyElement(chartEles))
		{
			DE.tipInfo(i18n.selectedNotHasChartElement);
			return false;
		}
		
		return true;
	};
	
	/**
	 * 校验是否图表元素。
	 * 
	 * @param ele 可选，元素，默认为：当前选中元素
	 */
	DE.checkChartElement = function(ele)
	{
		ele = DE._currentElement(ele, true);
		
		if(!DE._checkNotEmptyElement(ele))
			return false;
		
		var chart = DE.dashboard.renderedChart(ele);
		if(!chart)
		{
			DE.tipInfo(i18n.selectedNotChartElement);
			return false;
		}
		
		return true;
	};
	
	/**
	 * 校验是否定义了图表插件属性的图表元素。
	 * 
	 * @param ele 可选，元素，默认为：当前选中元素
	 */
	DE.checkAttrChartElement = function(ele)
	{
		ele = DE._currentElement(ele, true);
		
		if(!DE._checkNotEmptyElement(ele))
			return false;
		
		var chart = DE.dashboard.renderedChart(ele);
		if(!chart)
		{
			DE.tipInfo(i18n.selectedNotChartElement);
			return false;
		}
		
		var cpas = chart.pluginAttributes();
		if(cpas == null || cpas.length == 0)
		{
			DE.tipInfo(i18n.chartPluginNoAttrDefined);
			return false;
		}
		
		return true;
	};
	
	/**
	 * 是否是图表元素。
	 * 
	 * @param ele 可选，元素，默认为：当前选中元素
	 */
	DE.isChartElement = function(ele)
	{
		ele = DE._currentElement(ele);
		return (DE.dashboard.renderedChart(ele) != null);
	};
	
	/**
	 * 是否是网格布局条目元素。
	 * 
	 * @param ele 可选，元素，默认为：当前选中元素
	 */
	DE.isGridItemElement = function(ele)
	{
		ele = DE._currentElement(ele, true);
		var parent = CF.eleOfParent(ele);
		
		return DE._isDisplayGrid(parent);
	};
	
	/**
	 * 是否是弹性布局条目元素。
	 * 
	 * @param ele 可选，元素，默认为：当前选中元素
	 */
	DE.isFlexItemElement = function(ele)
	{
		ele = DE._currentElement(ele, true);
		var parent = CF.eleOfParent(ele);
		
		return DE._isDisplayFlex(parent);
	};
	
	/**
	 * 获取元素节点路径信息。
	 */
	DE.getElementPath = function(ele)
	{
		var paths = [];
		
		while(true)
		{
			if(DE._isEmptyElement(ele))
				break;
			
			var isBody =  DE._isBodyEle(ele);
			
			if(!DE._isSelectableElement(ele) && !isBody)
			{
				ele = CF.eleOfParent(ele);
				continue;
			}
			
			var editEle = DE._editElement(ele);
			var pathInfo =
			{
				tagName: DE._tagNameOfEleLowerCase(ele),
				selected: DE._isSelectedElement(ele),
				id: CF.eleAttr(editEle, "id"),
				className: CF.eleAttr(editEle, "class"),
				cssDisplay: CF.eleCss(ele, "display"),
				visualEditId: CF.eleAttr(editEle, ELEMENT_ATTR_VISUAL_EDIT_ID)
			};
			
			var displayName = pathInfo.tagName;
			
			if(DE._isDisplayGrid(pathInfo.cssDisplay))
				displayName += "(grid)";
			else if(DE._isDisplayFlex(pathInfo.cssDisplay))
				displayName += "(flex)";
			
			if(pathInfo.id)
				displayName += "#"+pathInfo.id;
			else if(pathInfo.className)
				displayName += "."+pathInfo.className;
			
			pathInfo.displayName = displayName;
			
			paths.push(pathInfo);
			
			if(isBody)
				break;
			else
				ele = CF.eleOfParent(ele);
		}
		
		return paths.reverse();
	};
	
	/**
	 * 是否在空白<body>元素内插入元素。
	 * 
	 * @param insertType 可选，参考_insertElement()函数的insertType参数
	 * @param refEle 可选，参考_insertElement()函数的refEle参数
	 */
	DE.isInsertToEmptyBody = function(insertType, refEle)
	{
		refEle = DE._currentElement(refEle);
		insertType = DE._trimInsertType(refEle, insertType);
		var insertParentEle = DE._getInsertParentElement(refEle, insertType);
		
		if(!DE._isBodyEle(insertParentEle))
			return false;
		
		var canInsert = true;
		
		//只有还未插入任何可选择元素时，才可以插入填满父容器元素
		CF.elesOfChildren(insertParentEle).forEach(function(child)
		{
			if(DE._isSelectableElement(child))
				canInsert = false;
		});
		
		return canInsert;
	};
	
	/**
	 * 校验网格布局元素。
	 * 
	 * @param insertType 可选，参考_insertElement()函数的insertType参数
	 * @param refEle 可选，参考_insertElement()函数的refEle参数
	 */
	DE.checkInsertGridLayout = function(insertType, refEle)
	{
		return true;
	};
	
	/**
	 * 插入网格布局元素。
	 * 
	 * @param gridAttr 网格设置，格式为：
	 * 					{
	 * 						rows: 数值或数值字符串,
	 * 						columns: 数值或数值字符串,
	 * 						fillParent: 布尔值或布尔值字符串,
	 * 						rowHeightDivide: "avg"、"custom",
	 * 						rowHeights: [ "", ... ],
	 * 						colWidthDivide: "avg"、"custom",
	 * 						colWidths: [ "", ... ],
	 * 						rowGap: "...",
	 * 						columnGap: "..."
	 * 					}
	 * @param insertType 可选，参考_insertElement()函数的insertType参数
	 * @param refEle 可选，参考_insertElement()函数的refEle参数
	 * 
	 * @returns 元素
	 */
	DE.insertGridLayout = function(gridAttr, insertType, refEle)
	{
		refEle = DE._currentElement(refEle);
		insertType = DE._trimInsertType(refEle, insertType);
		
		var rows = (!CF.isNumber(gridAttr.rows) ? parseInt(gridAttr.rows) : gridAttr.rows);
		var columns = (!CF.isNumber(gridAttr.columns) ? parseInt(gridAttr.columns) : gridAttr.columns);
		
		var div = CF.eleCreate("div");
		
		var styleStr = "";
		var insertParentEle = DE._getInsertParentElement(refEle, insertType);
		styleStr += DE._evalInsertLayoutHeightStyle(gridAttr.fillParent, insertParentEle);
		styleStr += "display:grid;";
		
		if(rows > 0)
		{
			if(gridAttr.rowHeightDivide == "custom" && gridAttr.rowHeights && gridAttr.rowHeights.length > 0)
			{
				styleStr += "grid-template-rows:"+gridAttr.rowHeights.join(" ")+";";
			}
			else
			{
				styleStr += "grid-template-rows:repeat("+rows+", 1fr);";
			}
		}
		
		if(columns > 0)
		{
			if(gridAttr.colWidthDivide == "custom" && gridAttr.colWidths && gridAttr.colWidths.length > 0)
			{
				styleStr += "grid-template-columns:"+gridAttr.colWidths.join(" ")+";";
			}
			else
			{
				styleStr += "grid-template-columns:repeat("+columns+", 1fr);";
			}
		}
		
		if(!CF.isEmpty(gridAttr.rowGap))
			styleStr += "row-gap:"+gridAttr.rowGap+";";
		
		if(!CF.isEmpty(gridAttr.columnGap))
			styleStr += "column-gap:"+gridAttr.columnGap+";";
		
		CF.eleAttr(div, "style", styleStr);
		
		for(let i=0; i<rows; i++)
		{
			for(let j=0; j<columns; j++)
				DE._insertElementFormat(div, CF.eleCreate("div"), INSERT_TYPE_APPEND);
		}
		
		DE._insertElement(div, insertType, refEle, true);
		
		return div;
	};
	
	/**
	 * 校验插入弹性布局元素。
	 * 
	 * @param insertType 可选，参考_insertElement()函数的insertType参数
	 * @param refEle 可选，参考_insertElement()函数的refEle参数
	 */
	DE.checkInsertFlexLayout = function(insertType, refEle)
	{
		return true;
	};
	
	/**
	 * 插入弹性布局元素。
	 * 
	 * @param flexAttr 网格设置，格式为：{ items: 数值或数值字符串, direction: "...", fillParent: 布尔值或布尔值字符串 }
	 * @param insertType 可选，参考_insertElement()函数的insertType参数
	 * @param refEle 可选，参考_insertElement()函数的refEle参数
	 * 
	 * @returns 元素
	 */
	DE.insertFlexLayout = function(flexAttr, insertType, refEle)
	{
		refEle = DE._currentElement(refEle);
		insertType = DE._trimInsertType(refEle, insertType);
		
		var items = (!CF.isNumber(flexAttr.items) ? parseInt(flexAttr.items) : flexAttr.items);
		
		var div = CF.eleCreate("div");
		
		var styleStr = "";
		var insertParentEle = DE._getInsertParentElement(refEle, insertType);
		styleStr += DE._evalInsertLayoutHeightStyle(flexAttr.fillParent, insertParentEle);
		styleStr += "display:flex;"+(flexAttr.direction ? "flex-direction:"+flexAttr.direction+";" : "")
						+"justify-content:space-around;align-items:center;align-content:space-around;";
		
		CF.eleAttr(div, "style", styleStr);
		
		for(var i=0; i<items; i++)
		{
			DE._insertElementFormat(div, CF.eleCreate("div"), INSERT_TYPE_APPEND);
		}
		
		DE._insertElement(div, insertType, refEle, true);
		
		return div;
	};
	
	/**
	 * 校验插入响应式弹性布局元素。
	 * 
	 * @param insertType 可选，参考_insertElement()函数的insertType参数
	 * @param refEle 可选，参考_insertElement()函数的refEle参数
	 */
	DE.checkInsertResponsiveFlex = function(insertType, refEle)
	{
		return true;
	};
	
	/**
	 * 插入响应式弹性布局元素。
	 * 
	 * @param model 布局模型，格式为：{ itemCount: 条目数, layout: { xs: { ... }, ... }, itemLayouts: [ { xs: { "布局名称": ..., ...}, sm: {...}, ... }, ... ] }
	 * @param insertType 可选，参考_insertElement()函数的insertType参数
	 * @param refEle 可选，参考_insertElement()函数的refEle参数
	 * 
	 * @returns 元素
	 */
	DE.insertResponsiveFlex = function(model, insertType, refEle)
	{
		refEle = DE._currentElement(refEle);
		insertType = DE._trimInsertType(refEle, insertType);
		
		var itemCount = (!CF.isNumber(model.itemCount) ? parseInt(model.itemCount) : model.itemCount);
		
		var div = CF.eleCreate("div");
		
		var styleClass = DE._evalResponsiveFlexLayoutClass(model.layout);
		styleClass = (styleClass ? "dg-rsp-row " + styleClass : "dg-rsp-row");
		CF.eleAttr(div, "class", styleClass);
		
		for(let i=0; i<itemCount; i++)
		{
			let itemStyleClass = DE._evalResponsiveFlexLayoutClass(model.itemLayouts[i]);
			let itemDiv = CF.eleCreate("div", itemStyleClass);
			DE._insertElementFormat(div, itemDiv, INSERT_TYPE_APPEND);
		}
		
		DE._insertElement(div, insertType, refEle, true);
		
		return div;
	};
	
	DE._evalResponsiveFlexLayoutClass = function(layout)
	{
		layout = (layout == null ? {} : layout);
		
		var re = "";
		
		for(let i=0; i<RESPONSIVE_BREAKPOINTS.length; i++)
		{
			let breakpoint = RESPONSIVE_BREAKPOINTS[i];
			let myLayout = (layout[breakpoint] || {});
			let myRe = DE._evalResponsiveFlexBreakpointClass(breakpoint, myLayout);
			
			if(myRe)
				re += (re == "" ? "" : " ") + myRe;
		}
		
		return re;
	};
	
	DE._evalResponsiveFlexBreakpointClass = function(breakpoint, layout)
	{
		var re = "";

		var infix = (breakpoint == "xs" ? "" : "-"+breakpoint);
		for(let i=0; i<RESPONSIVE_LAYOUT_NAMES.length; i++)
		{
			let name = RESPONSIVE_LAYOUT_NAMES[i];
			let value = layout[name];
			
			if(!CF.isEmpty(value))
			{
				re += (re == "" ? "" : " ") + "dg-rsp-"+name + infix +"-" + value;
			}
		}
		
		return re;
	};
	
	DE._evalResponsiveFlexCssLengthUnit = function(unit)
	{
		return (unit == "%" ? "pct" : unit);
	};
	
	/**
	 * 获取指定元素的响应式弹性布局设置。
	 * 
	 * @param ele 可选，参考_insertElement()函数的refEle参数
	 */
	DE.getResponsiveFlex = function(ele)
	{
		ele = DE._editElement(DE._currentElement(ele, true));
		
		var re = { itemCount: 0, layout: {}, itemLayouts: [] };
		
		re.layout = DE._evalResponsiveFlexLayout(CF.eleAttr(ele, "class"), [ "dg-rsp-row" ]);
		
		CF.elesOfChildren(ele).forEach(function(child)
		{
			var editId = DE._layoutAffectEleEditId(child);
			
			if(CF.isEmpty(editId))
				return;
			
			var layout = DE._evalResponsiveFlexLayout(CF.eleAttr(child, "class"));
			layout.visualEditId = editId;
			
			re.itemCount++;
			re.itemLayouts.push(layout);
		});
		
		return re;
	};
	
	DE._layoutAffectEleEditId = function(ele)
	{
		var editId = CF.eleAttr(ele, ELEMENT_ATTR_VISUAL_EDIT_ID);
		
		if(CF.isEmpty(editId))
			return null;
		
		var tagName = DE._tagNameOfEleLowerCase(ele);
		
		if(CF.isEmpty(tagName))
			return null;
		
		if(!DE._isVisualEleTag(tagName))
			return null;
		
		var position = CF.eleCss(ele, "position");
		if(position === "absolute" || position === "fixed")
			return null;
		
		return editId;
	};
	
	DE._evalResponsiveFlexLayout = function(classStr, ignoreClasses)
	{
		var classNames = (CF.isEmpty(classStr) ? [] : classStr.split(" "));
		
		var re = {};
		
		//从"dg-rsp-名称[-breakpoint]-值"中解析布局信息
		for(var i=0; i<classNames.length; i++)
		{
			var className = classNames[i];
			
			if(CF.isEmpty(className) || !className.indexOf("dg-rsp-") == 0)
				continue;
			
			if(ignoreClasses != null && CF.indexInArray(ignoreClasses, className) > -1)
				continue;
			
			var partStr = className.substr("dg-rsp-".length);
			var splitIdx = partStr.indexOf("-");
			
			if(splitIdx <= 0)
				continue;
			
			var breakpoint = "xs";
			var name = partStr.substring(0, splitIdx);
			var value = (splitIdx == partStr.length-1 ? "" : partStr.substring(splitIdx + 1));
			
			if(CF.isEmpty(value))
				continue;
			
			splitIdx = value.indexOf("-");
			if(splitIdx >= 0)
			{
				var part0 = value.substring(0, splitIdx);
				var isBreakpoint = (CF.indexInArray(RESPONSIVE_BREAKPOINTS, part0) > -1);
				
				if(isBreakpoint)
				{
					breakpoint = part0;
					value = (splitIdx == value.length-1 ? "" : value.substring(splitIdx + 1));
				}
			}
			
			if(CF.isEmpty(value))
				continue;
			
			re[breakpoint] = (re[breakpoint] || {});
			DE._inflateResponsiveFlexBreakpoint(re[breakpoint], name, value);
		}
		
		return re;
	};
	
	DE._inflateResponsiveFlexBreakpoint = function(breakpointObj, name, value)
	{
		breakpointObj[name] = value;
	};
	
	/**
	 * 校验设置元素响应式弹性布局。
	 * 
	 * @param ele 可选，参考_insertElement()函数的refEle参数
	 */
	DE.checkSetResponsiveFlex = function(ele)
	{
		ele = DE._currentElement(ele, true);
		
		if(!DE._checkNotEmptyElement(ele))
			return false;
		
		return true;
	};
	
	/**
	 * 设置元素响应式弹性布局。
	 * 
	 * @param model 布局模型，格式为：{ itemCount: 条目数, itemLayouts: [ { xs: { "布局名称": ..., ...}, sm: {...}, ... }, ... ] }
	 * @param ele 可选
	 * 
	 * @returns 元素
	 */
	DE.setResponsiveFlex = function(model, ele)
	{
		ele = DE._currentElement(ele, true);
		
		if(!DE.checkSetResponsiveFlex(ele))
			return false;
		
		var editEle = DE._editElement(ele);
		
		var styleClass = DE._evalResponsiveFlexLayoutClass(model.layout);
		styleClass = (styleClass ? "dg-rsp-row " + styleClass : "dg-rsp-row");
		var newClassName = DE._removeClassName(CF.eleAttr(editEle, "class"), DE._isResponsiveClassName);
		newClassName = styleClass + (newClassName == "" ? "" : " " + newClassName);
		
		DE._setElementClass(ele, newClassName);
		
		var itemLayouts = (model.itemLayouts || []);
		for(var i=0; i<itemLayouts.length; i++)
		{
			var itemLayout = itemLayouts[i];
			
			if(!itemLayout.visualEditId)
				continue;
			
			var child = DE._getEleByVisualEditId(itemLayout.visualEditId);
			var editChild = DE._editElement(child);
			
			var layoutClass = DE._evalResponsiveFlexLayoutClass(itemLayout);
			var newChildClassName = DE._removeClassName(CF.eleAttr(editChild, "class"), DE._isResponsiveClassName);
			newChildClassName = layoutClass + (newChildClassName == "" ? "" : " " + newChildClassName);
			
			DE._setElementClass(child, newChildClassName);
		}
		
		return ele;
	};
	
	DE._isResponsiveClassName = function(className)
	{
		return (className.indexOf("dg-rsp-") == 0);
	};
	
	/**
	 * 校验insertDiv操作。
	 * 
	 * @param insertType 可选，参考_insertElement()函数的insertType参数
	 * @param refEle 可选，参考_insertElement()函数的refEle参数
	 */
	DE.checkInsertDiv = function(insertType, refEle)
	{
		return true;
	};
	
	/**
	 * 插入div元素。
	 * 
	 * @param insertType 可选，参考_insertElement()函数的insertType参数
	 * @param refEle 可选，参考_insertElement()函数的refEle参数
	 * 
	 * @returns 元素
	 */
	DE.insertDiv = function(insertType, refEle)
	{
		refEle = DE._currentElement(refEle);
		insertType = DE._trimInsertType(refEle, insertType);
		
		var div = CF.eleCreate("div");
		
		var styleStr = "";
		var insertParentEle = DE._getInsertParentElement(refEle, insertType);
		
		if(DE._isBodyEle(insertParentEle))
			styleStr = "height:300px;";
		else if(DE._isDisplayGrid(insertParentEle))
			styleStr = "";
		else if(DE._isDisplayFlex(insertParentEle))
			styleStr = "";
		else
			styleStr = "";
		
		if(styleStr)
			CF.eleAttr(div, "style", styleStr);
		
		DE._insertElement(div, insertType, refEle, true);
		
		return div;
	};
	
	/**
	 * 校验insertImage操作。
	 * 
	 * @param insertType 可选，参考_insertElement()函数的insertType参数
	 * @param refEle 可选，参考_insertElement()函数的refEle参数
	 */
	DE.checkInsertImage = function(insertType, refEle)
	{
		return true;
	};
	
	/**
	 * 插入图片元素。
	 * 
	 * @param imgAttr 参考_setImageAttr()函数
	 * @param insertType 可选，参考_insertElement()函数的insertType参数
	 * @param refEle 可选，参考_insertElement()函数的refEle参数
	 * 
	 * @returns 元素
	 */
	DE.insertImage = function(imgAttr, insertType, refEle)
	{
		refEle = DE._currentElement(refEle);
		insertType = DE._trimInsertType(refEle, insertType);
		
		var img = CF.eleCreate("img");
		
		DE._insertElement(img, insertType, refEle);
		DE._setImageAttr(imgAttr, img);
		
		return img;
	};
	
	/**
	 * 元素是否是图片。
	 * 
	 * @param ele 可选，参考_insertElement()函数的refEle参数
	 */
	DE.isImage = function(ele)
	{
		ele = DE._currentElement(ele);
		return CF.isEleMatches(ele, "img");
	};
	
	/**
	 * 获取图片元素属性。
	 * 
	 * @param ele 可选，参考_insertElement()函数的refEle参数
	 */
	DE.getImageAttr = function(ele)
	{
		ele = DE._currentElement(ele);
		
		var attrObj = {};
		
		if(!DE.isImage(ele))
			return attrObj;
		
		ele = DE._editElement(ele);
		
		var eleStyle = DE.getElementStyle(ele);
		
		attrObj.src = (CF.eleAttr(ele, "src") || "");
		attrObj.width = eleStyle.width;
		attrObj.height = eleStyle.height;
		
		return attrObj;
	};
	
	/**
	 * 设置图片元素属性。
	 * 
	 * @returns 元素、false
	 */
	DE.setImageAttr = function(imgAttr, ele)
	{
		return DE._setImageAttr(imgAttr, ele);
	};
	
	/**
	 * 设置图片元素属性。
	 * 
	 * @param imgAttr 图片设置，格式为：{ src: "", width: ..., height: ... }
	 * @param ele 可选，参考_insertElement()函数的refEle参数
	 * 
	 * @returns 元素、false
	 */
	DE._setImageAttr = function(imgAttr, ele)
	{
		ele = DE._currentElement(ele, true);
		
		if(!DE.isImage(ele))
		{
			DE.tipInfo(i18n.imgEleRequired);
			return false;
		}
		
		var eleStyle = { width: imgAttr.width, height: imgAttr.height };
		
		DE._setElementAttr(ele, "src", (imgAttr.src || ""));
		DE._setElementStyleAppend(ele, eleStyle);
		
		return ele;
	};
	
	/**
	 * 校验insertHyperlink操作。
	 * 
	 * @param insertType 可选，参考_insertElement()函数的insertType参数
	 * @param refEle 可选，参考_insertElement()函数的refEle参数
	 */
	DE.checkInsertHyperlink = function(insertType, refEle)
	{
		return true;
	};
	
	/**
	 * 插入超链接元素。
	 * 
	 * @param hyperlinkAttr 参考_setHyperlinkAttr()函数
	 * @param insertType 可选，参考_insertElement()函数的insertType参数
	 * @param refEle 可选，参考_insertElement()函数的refEle参数
	 * 
	 * @returns 元素
	 */
	DE.insertHyperlink = function(hyperlinkAttr, insertType, refEle)
	{
		refEle = DE._currentElement(refEle);
		insertType = DE._trimInsertType(refEle, insertType);
		
		var a = CF.eleCreate("a");
		
		DE._insertElement(a, insertType, refEle);
		DE._setHyperlinkAttr(hyperlinkAttr, a);
		
		return a;
	};
	
	/**
	 * 元素是否是超链接。
	 * 
	 * @param ele 可选，参考_insertElement()函数的refEle参数
	 */
	DE.isHyperlink = function(ele)
	{
		ele = DE._currentElement(ele);
		return CF.isEleMatches(ele, "a");
	};
	
	/**
	 * 获取超链接元素属性。
	 * 
	 * @param ele 可选，参考_insertElement()函数的refEle参数
	 */
	DE.getHyperlinkAttr = function(ele)
	{
		ele = DE._currentElement(ele);
		
		var attrObj = {};
		
		if(!DE.isHyperlink(ele))
			return attrObj;
		
		ele = DE._editElement(ele);
		
		attrObj.content = CF.trim(ele.html());
		attrObj.href = (CF.eleAttr(ele, "href") || "");
		attrObj.target = (CF.eleAttr(ele, "target") || "");
		
		return attrObj;
	};
	
	/**
	 * 设置超链接元素属性。
	 * 
	 * @returns 元素、false
	 */
	DE.setHyperlinkAttr = function(hyperlinkAttr, ele)
	{
		return DE._setHyperlinkAttr(hyperlinkAttr, ele);
	};
	
	/**
	 * 设置超链接元素属性。
	 * 
	 * @param hyperlinkAttr 超链接设置，格式为：{ content: "...", href: "...", target: "..." }
	 * @param ele 可选，参考_insertElement()函数的refEle参数
	 * 
	 * @returns 元素、false
	 */
	DE._setHyperlinkAttr = function(hyperlinkAttr, ele)
	{
		ele = DE._currentElement(ele, true);
		
		if(!DE.isHyperlink(ele))
		{
			DE.tipInfo(i18n.hyperlinkEleRequired);
			return false;
		}
		
		DE._setElementText(ele, (hyperlinkAttr.content || hyperlinkAttr.href || ""));
		DE._setElementAttr(ele, "href", (hyperlinkAttr.href || ""));
		
		if(hyperlinkAttr.target)
			DE._setElementAttr(ele, "target", hyperlinkAttr.target);
		else
			DE._setElementAttr(ele, "target", null);
		
		return ele;
	};
	
	/**
	 * 校验insertVideo操作。
	 * 
	 * @param insertType 可选，参考_insertElement()函数的insertType参数
	 * @param refEle 可选，参考_insertElement()函数的refEle参数
	 */
	DE.checkInsertVideo = function(insertType, refEle)
	{
		return true;
	};
	
	/**
	 * 插入视频元素。
	 * 
	 * @param videoAttr 参考_setVideoAttr()函数
	 * @param insertType 可选，参考_insertElement()函数的insertType参数
	 * @param refEle 可选，参考_insertElement()函数的refEle参数
	 * 
	 * @returns 元素
	 */
	DE.insertVideo = function(videoAttr, insertType, refEle)
	{
		refEle = DE._currentElement(refEle);
		insertType = DE._trimInsertType(refEle, insertType);
		
		var ele = CF.eleCreate("video");
		CF.eleAttr(ele, "controls", "controls");
		
		DE._insertElement(ele, insertType, refEle);
		DE._setVideoAttr(videoAttr, ele);
		
		return ele;
	};
	
	/**
	 * 是否是视频元素。
	 * 
	 * @param ele 可选，参考_insertElement()函数的refEle参数
	 */
	DE.isVideo = function(ele)
	{
		ele = DE._currentElement(ele);
		return CF.isEleMatches(ele, "video");
	};
	
	/**
	 * 获取视频元素属性。
	 * 
	 * @param ele 可选，参考_insertElement()函数的refEle参数
	 */
	DE.getVideoAttr = function(ele)
	{
		ele = DE._currentElement(ele);
		
		var attrObj = {};
		
		if(!DE.isVideo(ele))
			return attrObj;
		
		ele = DE._editElement(ele);
		
		var eleStyle = DE.getElementStyle(ele);
		
		attrObj.src = (CF.eleAttr(ele, "src") || "");
		attrObj.width = eleStyle.width;
		attrObj.height = eleStyle.height;
		
		return attrObj;
	};
	
	/**
	 * 设置视频元素属性。
	 * 
	 * @returns 元素、false
	 */
	DE.setVideoAttr = function(videoAttr, ele)
	{
		return DE._setVideoAttr(videoAttr, ele);
	};
	
	/**
	 * 设置视频元素属性。
	 * 
	 * @param videoAttr 视频设置，格式为：{ src: "", width: ..., height: ... }
	 * @param ele 可选，参考_insertElement()函数的refEle参数
	 * 
	 * @returns 元素、false
	 */
	DE._setVideoAttr = function(videoAttr, ele)
	{
		ele = DE._currentElement(ele, true);
		
		if(!DE.isVideo(ele))
		{
			DE.tipInfo(i18n.videoEleRequired);
			return false;
		}
		
		var eleStyle = { width: videoAttr.width, height: videoAttr.height };
		
		DE._setElementAttr(ele, "src", (videoAttr.src || ""));
		DE._setElementStyleAppend(ele, eleStyle);
		
		return ele;
	};
	
	/**
	 * 校验insertIframe操作。
	 * 
	 * @param insertType 可选，参考_insertElement()函数的insertType参数
	 * @param refEle 可选，参考_insertElement()函数的refEle参数
	 */
	DE.checkInsertIframe = function(insertType, refEle)
	{
		return true;
	};
	
	/**
	 * 插入iframe元素。
	 * 
	 * @param iframeAttr 参考_setIframeAttr()函数
	 * @param insertType 可选，参考_insertElement()函数的insertType参数
	 * @param refEle 可选，参考_insertElement()函数的refEle参数
	 * 
	 * @returns 元素
	 */
	DE.insertIframe = function(iframeAttr, insertType, refEle)
	{
		refEle = DE._currentElement(refEle);
		insertType = DE._trimInsertType(refEle, insertType);
		
		var ele = CF.eleCreate("iframe");
		
		DE._insertElement(ele, insertType, refEle);
		DE._setIframeAttr(iframeAttr, ele);
		
		return ele;
	};
	
	/**
	 * 是否是iframe元素。
	 * 
	 * @param ele 可选，参考_insertElement()函数的refEle参数
	 */
	DE.isIframe = function(ele)
	{
		ele = DE._currentElement(ele);
		return CF.isEleMatches(ele, "iframe");
	};
	
	/**
	 * 获取iframe元素属性。
	 * 
	 * @param ele 可选，参考_insertElement()函数的refEle参数
	 */
	DE.getIframeAttr = function(ele)
	{
		ele = DE._currentElement(ele);
		
		var attrObj = {};
		
		if(!DE.isIframe(ele))
			return attrObj;
		
		ele = DE._editElement(ele);
		
		var eleStyle = DE.getElementStyle(ele);
		
		attrObj.src = (CF.eleAttr(ele, "src") || "");
		attrObj.width = eleStyle.width;
		attrObj.height = eleStyle.height;
		
		return attrObj;
	};
	
	/**
	 * 设置iframe元素属性。
	 * 
	 * @returns 元素、false
	 */
	DE.setIframeAttr = function(iframeAttr, ele)
	{
		return DE._setIframeAttr(iframeAttr, ele);
	};
	
	/**
	 * 设置iframe元素属性。
	 * 
	 * @param iframeAttr 视频设置，格式为：{ src: "", width: ..., height: ... }
	 * @param ele 可选，参考_insertElement()函数的refEle参数
	 * 
	 * @returns 元素、false
	 */
	DE._setIframeAttr = function(iframeAttr, ele)
	{
		ele = DE._currentElement(ele, true);
		
		if(!DE.isIframe(ele))
		{
			DE.tipInfo(i18n.iframeEleRequired);
			return false;
		}
		
		var eleStyle = { width: iframeAttr.width, height: iframeAttr.height };
		
		DE._setElementAttr(ele, "src", (iframeAttr.src || ""));
		DE._setElementStyleAppend(ele, eleStyle);
		
		return ele;
	};
	
	/**
	 * 校验insertHxtitle操作。
	 * 
	 * @param insertType 可选，参考_insertElement()函数的insertType参数
	 * @param refEle 可选，参考_insertElement()函数的refEle参数
	 */
	DE.checkInsertHxtitle = function(insertType, refEle)
	{
		return true;
	};
	
	/**
	 * 插入h1-h6元素。
	 * 
	 * @param model 标题模型，格式为：{ type: "h1到h6", content: "", textAlign: "" }
	 * @param insertType 可选，参考_insertElement()函数的insertType参数
	 * @param refEle 可选，参考_insertElement()函数的refEle参数
	 * 
	 * @returns 元素
	 */
	DE.insertHxtitle = function(model, insertType, refEle)
	{
		model.type = (model.type || "h1");
		refEle = DE._currentElement(refEle);
		insertType = DE._trimInsertType(refEle, insertType);
		
		var ele = CF.eleCreate(model.type);
		
		if(model.textAlign)
			CF.eleAttr(ele, "style", "text-align:"+model.textAlign+";");
		
		CF.eleHtml(ele, (model.content || ""));
		
		DE._insertElement(ele, insertType, refEle);
		
		return ele;
	};
	
	/**
	 * 校验insertLabel操作。
	 * 
	 * @param insertType 可选，参考_insertElement()函数的insertType参数
	 * @param refEle 可选，参考_insertElement()函数的refEle参数
	 */
	DE.checkInsertLabel = function(insertType, refEle)
	{
		return true;
	};
	
	/**
	 * 插入标签元素。
	 * 
	 * @param labelAttr 标签设置，格式为：{ content: "" }
	 * @param insertType 可选，参考_insertElement()函数的insertType参数
	 * @param refEle 可选，参考_insertElement()函数的refEle参数
	 * 
	 * @returns 元素
	 */
	DE.insertLabel = function(labelAttr, insertType, refEle)
	{
		refEle = DE._currentElement(refEle);
		insertType = DE._trimInsertType(refEle, insertType);
		
		var ele = CF.eleCreate("label");
		CF.eleHtml(ele, (labelAttr.content || ""));
		
		DE._insertElement(ele, insertType, refEle);
		
		return ele;
	};
	
	/**
	 * 是否是标签元素。
	 * 
	 * @param ele 可选，参考_insertElement()函数的refEle参数
	 */
	DE.isLabel = function(ele)
	{
		ele = DE._currentElement(ele);
		return CF.isEleMatches(ele, "label");
	};
	
	/**
	 * 获取标签元素属性。
	 * 
	 * @param ele 可选，参考_insertElement()函数的refEle参数
	 */
	DE.getLabelAttr = function(ele)
	{
		ele = DE._currentElement(ele);
		
		var attrObj = {};
		
		if(!DE.isLabel(ele))
			return attrObj;
		
		ele = DE._editElement(ele);
		
		attrObj.content = CF.trim(CF.eleHtml(ele));
		
		return attrObj;
	};
	
	/**
	 * 设置标签元素属性。
	 * 
	 * @param labelAttr 标签设置，格式为：{ content: "..." }
	 * @param ele 可选，参考_insertElement()函数的refEle参数
	 * 
	 * @returns 元素、false
	 */
	DE.setLabelAttr = function(labelAttr, ele)
	{
		ele = DE._currentElement(ele, true);
		
		if(!DE.isLabel(ele))
		{
			DE.tipInfo(i18n.labelEleRequired);
			return false;
		}
		
		DE._setElementText(ele, (labelAttr.content || ""));
		
		return ele;
	};
	
	/**
	 * 校验insertChart操作。
	 * 
	 * @param insertType 可选，参考insertChart函数的insertType参数
	 * @param refEle 可选，参考insertChart函数的refEle参数
	 */
	DE.checkInsertChart = function(insertType, refEle)
	{
		refEle = DE._currentElement(refEle);
		insertType = DE._trimInsertType(refEle, insertType);
		
		//图表元素内部不允许再插入图表元素
		if(DE.isChartElement(refEle) && (insertType == INSERT_TYPE_APPEND || insertType == INSERT_TYPE_PREPEND))
		{
			DE.tipInfo(i18n.insertInsideChartOnChartEleDenied);
			return false;
		}
		else
			return true;
	};
	
	//插入图表元素时的默认元素样式
	DE.defaultInsertChartEleStyle = "";
	
	/**
	 * 插入图表。
	 * 
	 * @param chartWidgets 要插入的图表部件对象、数组
	 * @param insertType 可选，参考_insertElement()函数的insertType参数
	 * @param refEle 可选，参考_insertElement()函数的refEle参数
	 * 
	 * @returns 元素数组、false
	 */
	DE.insertChart = function(chartWidgets, insertType, refEle)
	{
		if(!chartWidgets || chartWidgets.length == 0)
			return;
		
		chartWidgets = (!CF.isArray(chartWidgets) ? [ chartWidgets ] : chartWidgets);
		
		refEle = DE._currentElement(refEle);
		insertType = DE._trimInsertType(refEle, insertType);
		
		//图表元素内部不允许再插入图表元素
		if(!DE.checkInsertChart(insertType, refEle))
			return false;
		
		var eles = [];
		
		var styleStr = "";
		var insertParentEle = DE._getInsertParentElement(refEle, insertType);
		
		if(DE._isBodyEle(insertParentEle))
			styleStr = DE.defaultInsertChartEleStyle;
		else
			styleStr = "width:100%;height:100%;";
		
		for(var i=0; i<chartWidgets.length; i++)
		{
			var chartWidget = chartWidgets[i];
			var chartDiv = CF.eleCreate("div");
			
			//先设style，与源码模式一致
			if(styleStr)
				CF.eleAttr(chartDiv, "style", styleStr);
			
			CF.eleAttr(chartDiv, CF.elementAttrConst.WIDGET, chartWidget.id);
			CF.eleHtml(chartDiv, "<!--"+chartWidget.name+"-->");
			
			DE._insertElement(chartDiv, insertType, refEle);
			
			eles.push(chartDiv);
		}
		
		var loadChartsEle = (insertType == INSERT_TYPE_APPEND || insertType == INSERT_TYPE_PREPEND ? refEle : CF.eleOfParent(refEle));
		DE._loadUnsolvedChartsInElement(loadChartsEle);
		
		return eles;
	};
	
	/**
	 * 校验bindChart操作。
	 *
	 * @param ele 可选，要绑定的图表元素，默认为：当前选中图表元素
	 */
	DE.checkBindChart = function(ele)
	{
		ele = DE._currentElement(ele, true);
		
		if(!DE._checkNotEmptyElement(ele))
			return false;
		
		var editEle = DE._editElement(ele);
		
		//绑定图表的元素需要是div
		if(DE._isEmptyElement(editEle) || !CF.isEleMatches(editEle, "div"))
		{
			DE.tipInfo(i18n.bindChartElementMustBeDiv);
			return false;
		}
		
		return true;
	};
	
	/**
	 * 绑定或替换图表。
	 * 
	 * @param chartWidget 要绑定的新图表部件对象
	 * @param ele 可选，要绑定的图表元素，默认为：当前选中图表元素
	 * 
	 * @returns 元素、false
	 */
	DE.bindChart = function(chartWidget, ele)
	{
		if(!chartWidget)
			return false;
		
		ele = DE._currentElement(ele, true);
		
		if(!DE.checkBindChart(ele))
			return false;
		
		if(DE.isChartElement(ele))
		{
			DE._removeAndDestroyChart(ele);
		}
		
		DE._setElementAttr(ele, CF.elementAttrConst.WIDGET, chartWidget.id);
		DE.dashboard.loadChart(ele);
		
		return ele;
	};
	
	/**
	 * 校验unbindChart操作。
	 *
	 * @param ele 可选，要解绑的图表元素，默认为：当前选中图表元素
	 */
	DE.checkUnbindChart = function(ele)
	{
		ele = DE._currentElement(ele, true);
		
		if(!DE._checkNotEmptyElement(ele))
			return false;
		
		if(!DE.isChartElement(ele))
		{
			DE.tipInfo(i18n.selectedNotChartElement);
			return false;
		}
		
		return true;
	};
	
	/**
	 * 解绑图表。
	 * 
	 * @param ele 可选，要解绑的图表元素，默认为：当前选中图表元素
	 * 
	 * @returns 元素、false
	 */
	DE.unbindChart = function(ele)
	{
		ele = DE._currentElement(ele, true);
		
		if(!DE.checkUnbindChart(ele))
			return false;
		
		DE._removeAndDestroyChart(ele);
		DE._setElementAttr(ele, CF.elementAttrConst.WIDGET, null);
		
		return ele;
	};
	
	/**
	 * 获取元素文本内容。
	 * 
	 * @param ele 可选，元素，默认为：当前选中元素
	 */
	DE.getElementText = function(ele)
	{
		ele = DE._editElement(DE._currentElement(ele));
		return CF.trim(CF.eleText(ele));
	};
	
	/**
	 * 校验setElementText操作。
	 *
	 * @param ele 可选，元素，默认为：当前选中元素
	 */
	DE.checkSetElementText = function(ele)
	{
		ele = DE._currentElement(ele, true);
		
		if(!DE._checkNotEmptyElement(ele))
			return false;
		
		var firstChild = CF.eleOfFirstChild(ele);
		
		if(!DE._isEmptyElement(firstChild))
		{
			DE.tipInfo(i18n.canEditOnlyTextElement);
			return false;
		}
		
		return true;
	};
	
	/**
	 * 设置元素文本内容。
	 * 
	 * @param text 要设置的文本内容
	 * @param ele 可选，元素，默认为：当前选中元素
	 * 
	 * @returns 元素、false
	 */
	DE.setElementText = function(text, ele)
	{
		ele = DE._currentElement(ele, true);
		
		if(!DE.checkSetElementText(ele))
			return false;
		
		DE._setElementText(ele, text);
		
		return ele;
	};
	
	/**
	 * 获取<body>元素属性
	 * 
	 * @param name 属性名
	 */
	DE.getGlobalAttr = function(name)
	{
		var ele = DE._editElement(document.body);
		return CF.eleAttr(ele, name);
	};
	
	/**
	 * 设置<body>元素属性
	 * 
	 * @param name 属性名
	 * @param value 属性值
	 * 
	 * @returns 元素
	 */
	DE.setGlobalAttr = function(name, value)
	{
		var body = document.body;
		
		if(CF.isEmpty(value))
		{
			DE._setElementAttr(body, name, null);
		}
		else
		{
			DE._setElementAttr(body, name, value);
		}
		
		return body;
	};
	
	/**
	 * 获取元素属性。
	 * 
	 * @param name 属性名
	 * @param ele 可选，元素，默认为：当前选中元素
	 */
	DE.getElementAttr = function(name, ele)
	{
		ele = DE._editElement(DE._currentElement(ele, true));
		return CF.eleAttr(ele, name);
	};
	
	/**
	 * 校验setElementAttr操作。
	 * 
	 * @param ele 可选，元素，默认为：当前选中元素
	 */
	DE.checkSetElementAttr = function(ele)
	{
		ele = DE._currentElement(ele, true);
		
		if(!DE._checkNotEmptyElement(ele))
			return false;
		
		return true;
	};
	
	/**
	 * 设置元素属性
	 * 
	 * @param name 属性名
	 * @param value 属性值
	 * @param ele 可选，元素，默认为：当前选中元素
	 * 
	 * @returns 元素
	 */
	DE.setElementAttr = function(name, value, ele)
	{
		ele = DE._currentElement(ele, true);
		
		if(!DE.checkSetElementAttr(ele))
			return false;
		
		if(CF.isEmpty(value))
		{
			DE._setElementAttr(ele, name, null);
		}
		else
		{
			DE._setElementAttr(ele, name, value);
		}
		
		DE._checkSetElementIdAttrForChart(ele, name);
		
		return ele;
	};
	
	//校验设置图表元素ID，图表元素必须有ID，且设置后必须更新图表的elementId属性
	DE._checkSetElementIdAttrForChart = function(ele, name, reRender)
	{
		reRender = (reRender === undefined ? true : reRender);
		
		var isIdAttr = /^\s*id\s*$/i.test(name);
		
		if(!isIdAttr)
			return false;
		
		var chart = DE.dashboard.renderedChart(ele);
		
		if(!chart)
			return false;
		
		var id = CF.checkSetChartElementId(ele, chart);
		
		if(reRender)
			DE._reRenderChart(chart);
		
		return id;
	};
	
	/**
	 * 校验deleteElement操作。
	 * 
	 * @param ele 可选，元素，默认为：当前选中元素
	 */
	DE.checkDeleteElement = function(ele)
	{
		ele = DE._currentElement(ele, true);
		
		if(!DE._checkNotEmptyElement(ele))
			return false;
		
		return true;
	};
	
	/**
	 * 删除元素。
	 * 
	 * @param ele 可选，元素，默认为：当前选中元素
	 * 
	 * @returns 元素
	 */
	DE.deleteElement = function(ele)
	{
		ele = DE._currentElement(ele, true);
		
		if(!DE.checkDeleteElement(ele))
			return false;
		
		DE._preDeleteElement(ele);
		DE._deleteElement(ele);
		
		return ele;
	};
	
	DE._preDeleteElement = function(ele)
	{
		//应删除元素包含的所有图表
		DE._removeChartsInElement(ele);
		
		var selEle = (DE._isSelectedElement(ele) ? ele : DE._selectedElement(ele));
		DE.deselectElement(selEle);
		
		//删除后默认选中临近元素
		if(!DE.selectNextElement(ele, false))
		{
			if(!DE.selectPrevElement(ele, false))
			{
				DE.selectParentElement(ele, false);
			}
		}
	};
	
	/**
	 * 校验setElementStyle操作。
	 * 
	 * @param ele 可选，元素，默认为：当前选中元素
	 */
	DE.checkSetElementStyle = function(ele)
	{
		ele = DE._currentElement(ele, true);
		
		if(!DE._checkNotEmptyElement(ele))
			return false;
		
		return true;
	};
	
	/**
	 * 设置元素样式。
	 * 
	 * @param styleObj 要设置的样式对象，格式为：{ 'color': '...', 'background-color': '...' }
	 * @param ele 可选，元素，默认为：当前选中元素
	 * 
	 * @returns 元素
	 */
	DE.setElementStyle = function(styleObj, ele)
	{
		ele = DE._currentElement(ele, true);
		
		if(!DE.checkSetElementStyle(ele))
			return false;
		
		var so = DE._spitStyleAndOption(styleObj);
		
		DE._setElementStyle(ele, so.style);
		DE._setElementClass(ele, so.option.className);
		
		var chartEles = DE._getChartElements(ele);
		
		chartEles.forEach((chartEle) =>
		{
			if(so.option.syncChartTheme)
			{
				var chartTheme = DE._evalElementChartThemeByStyleObj(chartEle, ele, so.style);
				DE._setElementChartTheme(chartEle, chartTheme);
				DE._reRenderChartsInElement(chartEle);
			}
			else
			{
				DE._resizeChartsInElement(chartEle);
			}
		});
		
		return ele;
	};
	
	/**
	 * 获取元素样式对象。
	 * 
	 * @param ele 可选，元素，默认为：当前选中元素
	 */
	DE.getElementStyle = function(ele)
	{
		ele = DE._editElement(DE._currentElement(ele, true));
		return DE._getElementStyleObj(ele);
	};
	
	/**
	 * 设置全局样式（body）。
	 * 
	 * @param styleObj 要设置的样式对象，格式为：{ 'color': '...', 'background-color': '...' }
	 * 
	 * @returns 元素
	 */
	DE.setGlobalStyle = function(styleObj)
	{
		var so = DE._spitStyleAndOption(styleObj);
		var body = document.body;
		
		DE._setElementStyle(body, so.style);
		DE._setElementClass(body, so.option.className);
		
		DE._setPageStyle(
		{
			selectedBorderColor: (so.style.color ? so.style.color : undefined)
		});
		
		if(so.option.syncChartTheme)
		{
			var chartTheme = DE._evalElementChartThemeByStyleObj(body, body, so.style);
			DE._setGlobalChartTheme(chartTheme);
		}
		
		return body;
	};
	
	/**
	 * 获取全局样式对象（body）。
	 */
	DE.getGlobalStyle = function()
	{
		var ele = DE._editElement(document.body);
		return DE._getElementStyleObj(ele);
	};
	
	/**
	 * 校验setElementChartTheme操作。
	 * 
	 * @param ele 可选，元素，默认为：当前选中元素
	 */
	DE.checkSetElementChartTheme = function(ele)
	{
		return DE.checkHasChartElement(ele);
	};
	
	/**
	 * 设置元素或其所有子图表元素的图表主题。
	 * 
	 * @param chartTheme 要设置的图表主题对象，格式为：{ 'color': '...', 'backgroundColor': '...', ... }
	 * @param ele 可选，元素，默认为：当前选中元素
	 * 
	 * @returns 元素、false
	 */
	DE.setElementChartTheme = function(chartTheme, ele)
	{
		ele = DE._currentElement(ele, true);
		
		if(!DE.checkSetElementChartTheme(ele))
			return false;
		
		var chartEles = DE._getChartElements(ele);
		
		chartEles.forEach((chartEle) =>
		{
			DE._setElementChartTheme(chartEle, chartTheme);
			DE._reRenderChartsInElement(chartEle);
		});
		
		return ele;
	};
	
	/**
	 * 获取元素图表主题。
	 * 
	 * @param ele 可选，元素，默认为：当前选中元素
	 */
	DE.getElementChartTheme = function(ele)
	{
		ele = DE._editElement(DE._currentElement(ele, true));
		return DE._getElementChartTheme(ele);
	};
	
	/**
	 * 设置全局图表主题。
	 * 
	 * @param chartTheme 要设置的图表主题对象，格式为：{ 'color': '...', 'backgroundColor': '...', ... }
	 * 
	 * @returns 元素
	 */
	DE.setGlobalChartTheme = function(chartTheme)
	{
		return DE._setGlobalChartTheme(chartTheme);
	};
	
	DE._setGlobalChartTheme = function(chartTheme)
	{
		var ele = document.body;
		
		DE._setElementChartTheme(document.body, chartTheme);
		DE._reRenderDashboard();
		
		return ele;
	};
	
	/**
	 * 获取全局图表主题。
	 */
	DE.getGlobalChartTheme = function()
	{
		var ele = DE._editElement(document.body);
		return DE._getElementChartTheme(ele);
	};
	
	/**
	 * 获取图表元素的图表属性值。
	 * 
	 * @param ele 可选，元素，默认为：当前选中元素
	 */
	DE.getElementChartAttrValues = function(ele)
	{
		ele = DE._currentElement(ele, true);
		
		if(!DE._checkNotEmptyElement(ele))
			return null;
		
		var chart = DE.dashboard.renderedChart(ele);
		var attrValues = (chart ? chart.attrValues() : null);
		
		//应复制一份，避免被不可预料的修改
		if(attrValues != null)
			attrValues = CF.extend(true, {}, attrValues);
		
		return attrValues;
	};
	
	/**
	 * 获取图表元素的重置图表属性值。
	 * 
	 * @param ele 可选，元素，默认为：当前选中元素
	 */
	DE.getElementChartAttrValuesForReset = function(ele)
	{
		ele = DE._currentElement(ele, true);
		
		if(!DE._checkNotEmptyElement(ele))
			return null;
		
		var chart = DE.dashboard.renderedChart(ele);
		if(!chart)
			return null;
		
		var attrValuesOrigin =  (chart.attrValuesOrigin() || {});
		var attrValuesEle = CF.eleAttr(chart.element(), CF.elementAttrConst.ATTR_VALUES);
		attrValuesEle = CF.evalSilently(attrValuesEle, {});
		var cpas = chart.pluginAttributes();
		
		cpas.forEach((cpa) =>
		{
			delete attrValuesEle[cpa.name];
		});
		
		//保留元素上定义的图表插件属性之外的扩展值
		var re = CF.extend(true, {}, attrValuesOrigin, attrValuesEle);
		return re;
	};
	
	/**
	 * 获取看板图表插件属性内置地图选项集。
	 */
	DE.getChartPluginAttributeInputOptionsForMap = function(asTree)
	{
		var re = [];
		
		//树
		if(asTree)
		{
			var listener =
			{
				added: function(node, parent)
				{
					//转换为UI组件所需的结构
					node.key = node.mapName;
					node.label = node.mapLabel;
					if(parent && !parent.children)
						parent.children = parent.mapChildren;
				}
			};
			
			re = DF.getStdBuiltinMapTree(listener);
		}
		//数组
		else
		{
			var listener =
			{
				added: function(node)
				{
					//转换为UI组件所需的结构
					node.value = node.mapName;
					node.name = node.mapLabel;
				}
			};
			
			re = DF.getStdBuiltinMapArray(listener);
		}
		
		var mapURLs = [];
		
		var mapURLsBody = CF.eleAttr(document.body, CF.elementAttrConst.MAP_URLS);
		mapURLsBody = (mapURLsBody ? CF.evalSilently(mapURLsBody, {}) : {});
		
		for(let p in mapURLsBody)
		{
			let v = mapURLsBody[p];
			
			if(p && CF.isString(v))
			{
				if(asTree)
					mapURLs.push({ key: p, label: p });
				else
					mapURLs.push({ name: p, value: p });
			}
		}
		
		if(mapURLs.length > 0)
			re = mapURLs.concat(re);
		
		return re;
	};
	
	/**
	 * 校验setElementChartAttrValues操作。
	 * 
	 * @param ele 可选，元素，默认为：当前选中元素
	 */
	DE.checkSetElementChartAttrValues = function(ele)
	{
		return DE.checkAttrChartElement(ele);
	};
	
	/**
	 * 设置图表元素的图表属性值。
	 * 
	 * @param attrValues 要设置的图表主题对象，格式为：{ ... }
	 * @param ele 可选，元素，默认为：当前选中元素
	 * 
	 * @returns 元素、false
	 */
	DE.setElementChartAttrValues = function(attrValues, ele)
	{
		attrValues = (attrValues || {});
		ele = DE._currentElement(ele, true);
		
		if(!DE.checkSetElementChartAttrValues(ele))
			return false;
		
		var chart = DE.dashboard.renderedChart(ele);
		var attrValuesOrigin = (chart.attrValuesOrigin() || {});
		var attrValuesMerge = {};
		
		//应该只设置有修改的图表属性值，这样在图表模块再次编辑其他图表属性值才能应用于所有引用它看板
		for(let p in attrValues)
		{
			if(!CF.deepEquals(attrValuesOrigin[p], attrValues[p]))
				attrValuesMerge[p] = attrValues[p];
		}
		
		var eleAttrValue = DE._serializeForAttrValue(attrValuesMerge);
		
		if(DE._isEmptyJsonObjStr(eleAttrValue))
			DE._setElementAttr(ele, CF.elementAttrConst.ATTR_VALUES, null);
		else
			DE._setElementAttr(ele, CF.elementAttrConst.ATTR_VALUES, eleAttrValue);
		
		DE._reRenderChart(chart);
		
		return ele;
	};
	
	/**
	 * 获取图表元素的ChartPluginAttribute数组。
	 * 
	 * @param ele 可选，元素，默认为：当前选中元素
	 */
	DE.getElementChartPluginAttrs = function(ele)
	{
		ele = DE._currentElement(ele, true);
		
		if(!DE._checkNotEmptyElement(ele))
			return null;
		
		var chart = DE.dashboard.renderedChart(ele);
		if(!chart)
			return null;
		
		return chart.pluginAttributes();
	};
	
	/**
	 * 校验setElementChartOptions操作。
	 * 
	 * @param ele 可选，元素，默认为：当前选中元素
	 */
	DE.checkSetElementChartOptions = function(ele)
	{
		return DE.checkHasChartElement(ele);
	};
	
	/**
	 * 设置元素或其所有子图表元素的图表选项。
	 * 
	 * @param chartOptionsStr 要设置的图表选项字符串
	 * @param ele 可选，元素，默认为：当前选中元素
	 * 
	 * @returns 元素、false
	 */
	DE.setElementChartOptions = function(chartOptionsStr, ele)
	{
		ele = DE._currentElement(ele, true);
		
		if(!DE.checkSetElementChartOptions(ele))
			return false;
		
		var chartEles = DE._getChartElements(ele);
		
		chartEles.forEach((chartEle) =>
		{
			DE._setElementChartOptions(chartEle, chartOptionsStr);
			DE._reRenderChartsInElement(chartEle);
		});
		
		return ele;
	};
	
	/**
	 * 获取元素图表选项的字符串格式。
	 * 
	 * @param ele 可选，元素，默认为：当前选中元素
	 * @oaram asString 可选，是否以字符串形式返回，默认为：true
	 */
	DE.getElementChartOptions = function(ele, asString)
	{
		ele = DE._editElement(DE._currentElement(ele, true));
		asString = (asString === undefined ? true : asString);
		
		return DE._getElementChartOptions(ele);
	};
	
	/**
	 * 获取元素原始图表选项的字符串格式。
	 * 
	 * @param ele 可选，元素，默认为：当前选中元素
	 */
	DE.getElementChartOptionsOrigin = function(ele)
	{
		ele = DE._currentElement(ele, true);
		
		if(!DE._checkNotEmptyElement(ele))
			return "";
		
		var chart = DE.dashboard.renderedChart(ele);
		if(!chart)
			return "";
		
		return (chart.optionsOrigin() || "");
	};
	
	/**
	 * 设置全局图表选项。
	 * 
	 * @param chartOptionsStr 要设置的全局图表选项字符串
	 * 
	 * @returns 元素
	 */
	DE.setGlobalChartOptions = function(chartOptionsStr)
	{
		var ele = document.body;
		
		DE._setElementChartOptions(ele, chartOptionsStr);
		DE._reRenderDashboard();
		
		return ele;
	};
	
	/**
	 * 获取全局图表选项的字符串格式。
	 * 
	 * @oaram asString 可选，是否以字符串形式返回，默认为：true
	 */
	DE.getGlobalChartOptions = function(asString)
	{
		asString = (asString === undefined ? true : asString);
		
		var ele = DE._editElement(document.body);
		return DE._getElementChartOptions(ele);
	};
	
	DE._reRenderDashboard = function()
	{
		var dashboard = DE.dashboard;
		CF.executeSilently(() =>
		{
			dashboard.destroy();
			dashboard.init();
			dashboard.render();
		});
	};
	
	DE._renderChart = function(chart)
	{
		if(!chart)
			return;
		
		CF.executeSilently(() =>
		{
			chart.render();
		});
	};
	
	DE._reRenderChart = function(chart)
	{
		if(!chart)
			return;
		
		CF.executeSilently(() =>
		{
			chart.destroy();
			chart.init();
			chart.render();
		});
	};
	
	DE._resizeChart = function(chart)
	{
		if(!chart)
			return;
		
		CF.executeSilently(() =>
		{
			chart.resize();
		});
	};
	
	DE._removeAndDestroyChart = function(chartInfo)
	{
		if(!chartInfo)
			return;
		
		var dashboard = DE.dashboard;
		CF.executeSilently(() =>
		{
			dashboard.removeChart(chartInfo, true);
		});
	};
	
	DE._reRenderChartsInElement = function(ele)
	{
		var chartEles = DE._getChartElements(ele);
		
		chartEles.forEach((chartEle) =>
		{
			var chart = DE.dashboard.renderedChart(chartEle);
			DE._reRenderChart(chart);
		});
	};
	
	DE._resizeChartsInElement = function(ele)
	{
		var chartEles = DE._getChartElements(ele);
		
		chartEles.forEach((chartEle) =>
		{
			var chart = DE.dashboard.renderedChart(chartEle);
			DE._resizeChart(chart);
		});
	};
	
	DE._removeChartsInElement = function(ele)
	{
		var chartEles = DE._getChartElements(ele);
		
		chartEles.forEach((chartEle) =>
		{
			DE._removeAndDestroyChart(chartEle);
		});
	};
	
	DE._loadUnsolvedChartsInElement = function(ele)
	{
		DE.dashboard.loadUnsolvedCharts(ele);
	};
	
	DE._setElementChartOptions = function(ele, chartOptionsStr)
	{
		if(!chartOptionsStr)
		{
			DE._setElementAttr(ele, CF.elementAttrConst.OPTIONS, null);
			return;
		}
		
		var attrValue = (chartOptionsStr ? chartOptionsStr : "{}");
		DE._setElementAttr(ele, CF.elementAttrConst.OPTIONS, attrValue);
	};
	
	DE._getElementChartOptions = function(ele)
	{
		var optionsStr = CF.eleAttr(ele, CF.elementAttrConst.OPTIONS);
		return optionsStr;
	};
	
	DE._getElementChartTheme = function(ele)
	{
		var themeStr = CF.eleAttr(ele, CF.elementAttrConst.THEME);
		
		if(!themeStr)
			return null;
		
		return CF.evalSilently(themeStr, {});
	};
	
	DE._setElementChartTheme = function(ele, chartTheme)
	{
		chartTheme = CF.extend(true, {}, chartTheme); 
		
		if(CF.isString(chartTheme.graphColors))
			chartTheme.graphColors = DE._spitIgnoreEmpty(chartTheme.graphColors);
		
		if(CF.isString(chartTheme.graphRangeColors))
			chartTheme.graphRangeColors = DE._spitIgnoreEmpty(chartTheme.graphRangeColors);
		
		var mergedChartTheme = (DE.getElementChartTheme(ele) || {});
		
		for(var p in chartTheme)
		{
			var v = chartTheme[p];
			
			if(CF.isEmpty(v))
				delete mergedChartTheme[p];
			else
				mergedChartTheme[p] = v;
		}
		
		//确保fontSize为数值
		if(mergedChartTheme.fontSize != null && !CF.isNumber(mergedChartTheme.fontSize))
		{
			var fontSize = parseInt(mergedChartTheme.fontSize);
			if(isNaN(fontSize))
				delete mergedChartTheme.fontSize;
			else
				 mergedChartTheme.fontSize = fontSize;
		}
		
		var trim = {};
		
		for(var p in mergedChartTheme)
		{
			var v = mergedChartTheme[p];
			
			if(!CF.isEmpty(v))
				trim[p] = v;
		}
		
		var attrValue = DE._serializeForAttrValue(trim);
		
		if(DE._isEmptyJsonObjStr(attrValue))
			DE._setElementAttr(ele, CF.elementAttrConst.THEME, null);
		else
			DE._setElementAttr(ele, CF.elementAttrConst.THEME, attrValue);
	};
	
	DE._isBodyEle = function(ele)
	{
		return CF.isEleMatches(ele, "body");
	};
	
	/**
	 * 插入元素，同时同步至编辑HTML中。
	 * 
	 * @param insertEle 要插入的HTML元素、HTML文本，不要使用"<div />"的格式，可能导致编辑HTML代码格式不对
	 * @param insertType 可选，插入类型：INSERT_TYPE_*
	 * @param refEle 插入参照元素，默认为：当前选中元素，或者<body>
	 * @param highlight 可选，是否为元素添加高亮样式，默认为：false
	 */
	DE._insertElement = function(insertEle, insertType, refEle, highlight)
	{
		refEle = DE._currentElement(refEle);
		insertType = DE._trimInsertType(refEle, insertType);
		highlight = (highlight === undefined ? false : highlight);
		
		if(CF.isString(insertEle))
			insertEle = CF.eleCreateByHtml(insertEle);
		
		DE._addVisualEditIdAttr(insertEle);
		DE._insertElementFormat(refEle, insertEle, insertType);
		
		//同步至编辑HTML中
		var editEle = DE._editElement(refEle);
		var insertEleClone = CF.eleClone(insertEle);
		DE._insertElementFormat(editEle, insertEleClone, insertType, true);
		
		if(highlight)
		{
			CF.eleAddClass(insertEle, ELEMENT_CLASS_NEW_INSERT);
			let ds = CF.elesOfSelector("*", insertEle);
			ds.forEach((d) =>
			{
				CF.eleAddClass(d, ELEMENT_CLASS_NEW_INSERT);
			});
		}
		
		DE.changeFlag(true);
	};
	
	/**
	 * 删除元素，同时同步至编辑HTML中。
	 */
	DE._deleteElement = function(ele)
	{
		var editEle = DE._editElement(ele);
		
		DE._deleteElementFormat(editEle);
		DE._deleteElementFormat(ele);
		
		DE.changeFlag(true);
	};
	
	/**
	 * 追加设置元素style属性，同时同步至编辑HTML中。
	 */
	DE._setElementStyleAppend = function(ele, styleObj)
	{
		DE._setElementStyle(ele, styleObj, false);
	};
	
	/**
	 * 设置元素style属性，同时同步至编辑HTML中。
	 */
	DE._setElementStyle = function(ele, styleObj, strictSet)
	{
		styleObj = (styleObj || {});
		
		DE._setElementStyleIfStrict(ele, styleObj, strictSet);
		
		//同步至编辑HTML中
		var editEle = DE._editElement(ele);
		DE._setElementStyleIfStrict(editEle, styleObj, strictSet);
		
		DE._reSelectElementIf(ele);
		DE.changeFlag(true);
	};
	
	DE._setElementStyleIfStrict = function(ele, styleObj, strictSet)
	{
		//这里不能采用整体设置"style"属性的方式，因为"style"属性可能有很多不支持编辑的、或者动态生成的css属性，
		//它们应该被保留，且不能同步至对应的编辑元素上
		
		//默认严格设置模式，这样才能支持删除styleObj中未出现的样式
		strictSet = (strictSet === undefined ? true : strictSet);
		
		var nowStyleObj = CF.styleStringToObj(CF.eleStyle(ele) || "");
		
		if(strictSet)
		{
			for(var editableName in DE._editableElementStyles)
			{
				delete nowStyleObj[editableName];
			}
		}
		
		for(var name in styleObj)
		{
			var value = styleObj[name];
			
			if(CF.isEmpty(value))
				delete nowStyleObj[name];
			else
			{
				nowStyleObj[name] = value;
			}
		}
		
		if(CF.isEmptyObject(nowStyleObj))
			DE._setElementAttrNoSync(ele, "style", null);
		else
		{
			var cssText = CF.styleString(nowStyleObj);
			DE._setElementAttrNoSync(ele, "style", cssText);
		}
	};
	
	/**
	 * 设置元素class属性，同时同步至编辑HTML中。
	 */
	DE._setElementClass = function(ele, className)
	{
		className = (className || "");
		
		var editEle = DE._editElement(ele);
		var oldClassNames = CF.splitByWhitespace(CF.eleAttr(editEle, "class") || "");
		
		var eleClassStr = (CF.eleAttr(ele, "class") || "");
		eleClassStr = DE._removeClassName(eleClassStr, (name) => { return (CF.indexInArray(oldClassNames, name) > -1); });
		eleClassStr += (CF.isEmpty(className) ? "" : " " + className);
		
		DE._setElementAttrNoSync(ele, "class", eleClassStr);
		
		//同步至编辑HTML中
		if(CF.isEmpty(className))
			DE._setElementAttrNoSync(editEle, "class", null);
		else
			DE._setElementAttrNoSync(editEle, "class", className);
		
		DE._reSelectElementIf(ele);
		DE.changeFlag(true);
	};
	
	DE._removeClassName = function(classStr, predicate)
	{
		var re = "";
		
		var classNames = (CF.isEmpty(classStr) ? [] : CF.splitByWhitespace(classStr));
		
		for(var i=0; i<classNames.length; i++)
		{
			var className = classNames[i];
			
			if(predicate(className))
				continue;
			
			re += (re == "" ? "" : " ") + className;
		}
		
		return re;
	};
	
	/**
	 * 设置元素文本内容，同时同步至编辑HTML中。
	 */
	DE._setElementText = function(ele, text)
	{
		text = (text || "");
		
		DE._setElementTextNoSync(ele, text);
		
		//同步至编辑HTML中
		var editEle = DE._editElement(ele);
		DE._setElementTextNoSync(editEle, text);
		
		DE._reSelectElementIf(ele);
		DE.changeFlag(true);
	};
	
	/**
	 * 设置元素属性，同时同步至编辑HTML中，值为null时将移除属性。
	 */
	DE._setElementAttr = function(ele, name, value)
	{
		DE._setElementAttrNoSync(ele, name, value);
		
		//同步至编辑HTML中
		var editEle = DE._editElement(ele);
		DE._setElementAttrNoSync(editEle, name, value);
		
		DE._reSelectElementIf(ele);
		DE.changeFlag(true);
	};
	
	DE._insertElementFormat = function(refEle, insertEle, insertType, formatInner)
	{
		formatInner = (formatInner === undefined ? false : formatInner);
		
		var refEleLevel = DE._evalElementLevel(refEle);
		
		if(insertType == INSERT_TYPE_AFTER)
		{
			DE._insertElementAfterNoSync(refEle, insertEle);
			CF.eleAfter(refEle, INSERT_ELE_FORMAT_START + "\n" + DE._genFormatTabs(refEleLevel) + INSERT_ELE_FORMAT_END);
		}
		else if(insertType == INSERT_TYPE_BEFORE)
		{
			DE._insertElementBeforeNoSync(refEle, insertEle);
			CF.eleBefore(refEle, INSERT_ELE_FORMAT_START + "\n" + DE._genFormatTabs(refEleLevel) + INSERT_ELE_FORMAT_END);
		}
		else if(insertType == INSERT_TYPE_APPEND)
		{
			var children = CF.elesOfChildren(refEle);
			
			if(CF.isEmpty(children))
			{
				var innerHtml = DE._getInnerHTML(refEle);
				if(DE._isOnlyEmptyOrFormat(innerHtml))
				{
					DE._setInnerHTMLNoSync(refEle, "");
				}
				
				CF.eleAppend(refEle, INSERT_ELE_FORMAT_START + "\n" + DE._genFormatTabs(refEleLevel+1) + INSERT_ELE_FORMAT_END);
				DE._insertElementAppendNoSync(refEle, insertEle);
				CF.eleAppend(refEle, INSERT_ELE_FORMAT_START + "\n" + DE._genFormatTabs(refEleLevel) + INSERT_ELE_FORMAT_END);
			}
			else
			{
				DE._insertElementFormat(children[children.length-1], insertEle, INSERT_TYPE_AFTER, formatInner);
				return;
			}
		}
		else if(insertType == INSERT_TYPE_PREPEND)
		{
			var insertTailFormat = false;
			
			var children = CF.elesOfChildren(refEle);
			if(CF.isEmpty(children))
			{
				var innerHtml = DE._getInnerHTML(refEle);
				if(DE._isOnlyEmptyOrFormat(innerHtml))
				{
					insertTailFormat = true;
					DE._setInnerHTMLNoSync(refEle, "");
				}
			}
			
			DE._insertElementPrependNoSync(refEle, insertEle);
			CF.elePrepend(refEle, INSERT_ELE_FORMAT_START + "\n" + DE._genFormatTabs(refEleLevel+1) + INSERT_ELE_FORMAT_END);
			
			if(insertTailFormat)
				CF.eleAppend(refEle, INSERT_ELE_FORMAT_START + "\n" + DE._genFormatTabs(refEleLevel) + INSERT_ELE_FORMAT_END);
		}
		else
			throw new Error("Unsupported insert type : " + insertType);
		
		//为所有内部元素补齐格式
		if(formatInner)
		{
			var tabsText = DE._genFormatTabs(DE._evalElementLevel(insertEle)-1);
			DE._appendElementSubFormat(insertEle, tabsText);
		}
	};
	
	DE._appendElementSubFormat = function(ele, tabsText)
	{
		//在每一个<!--dgInsertFmtEnd-->注释节点前插入格式文本
		//注意：这里不应该使用替换HTML内容文本后再设置的方式，因为会新建DOM对象而导致旧DOM引用失效
		CF.elesOfChildren(ele, true).forEach(function(node)
		{
			if(node.nodeType == Node.COMMENT_NODE && node.nodeValue == "dgInsertFmtEnd")
			{
				CF.eleBefore(node, CF.eleCreateText(tabsText));
			}
		});
		
		CF.elesOfChildren(ele).forEach((child) =>
		{
			DE._appendElementSubFormat(child, CF.eleCreateText(tabsText));
		});
	};
	
	DE._deleteElementFormat = function(ele)
	{
		CF.eleBefore(ele, DELETE_ELE_FORMAT_FLAG);
		DE._deleteElementNoSync(ele);
	};
	
	DE._setElementTextNoSync = function(ele, text)
	{
		CF.eleText(ele, text);
	};
	
	DE._setElementAttrNoSync = function(ele, name, value)
	{
		// value是""时不应移除
		CF.eleAttr(ele, name, value);
	};
	
	DE._insertElementAppendNoSync = function(refEle, insertEle)
	{
		CF.eleAppend(refEle, insertEle);
	};
	
	DE._insertElementPrependNoSync = function(refEle, insertEle)
	{
		CF.elePrepend(refEle, insertEle);
	};
	
	DE._insertElementAfterNoSync = function(refEle, insertEle)
	{
		CF.eleAfter(refEle, insertEle);
	};
	
	DE._insertElementBeforeNoSync = function(refEle, insertEle)
	{
		CF.eleBefore(refEle, insertEle);
	};
	
	DE._deleteElementNoSync = function(ele)
	{
		CF.eleRemove(ele);
	};
	
	DE._setInnerHTMLNoSync = function(ele, html)
	{
		CF.eleHtml(ele, html);
	};
	
	DE._editableElementStyles =
	{
		"color": true,
		"background-color": true,
		"background-image": true,
		"background-size": true,
		"background-repeat": true,
		"background-position": true,
		"border-width": true,
		"border-color": true,
		"border-style": true,
		"border-radius": true,
		"box-shadow": true,
		"display": true,
		"width": true,
		"height": true,
		"min-width": true,
		"min-height": true,
		"max-width": true,
		"max-height": true,
		"padding": true,
		"margin": true,
		"overflow": true,
		"box-sizing": true,
		"position": true,
		"left": true,
		"top": true,
		"right": true,
		"bottom": true,
		"z-index": true,
		"grid-template-rows": true,
		"grid-template-columns": true,
		"row-gap": true,
		"column-gap": true,
		"grid-template-areas": true,
		"grid-auto-flow": true,
		"justify-items": true,
		"align-items": true,
		"justify-content": true,
		"align-content": true,
		"grid-auto-rows": true,
		"grid-auto-columns": true,
		"grid-row-start": true,
		"grid-row-end": true,
		"grid-column-start": true,
		"grid-column-end": true,
		"grid-area": true,
		"justify-self": true,
		"align-self": true,
		"flex-direction": true,
		"flex-wrap": true,
		"order": true,
		"flex-grow": true,
		"flex-shrink": true,
		"flex-basis": true,
		"font-size": true,
		"text-align": true,
		"font-weight": true,
		"font-family": true,
		"line-height": true
	};
	
	DE._getInnerHTML = function(ele)
	{
		return CF.eleHtml(ele);
	};
	
	DE._getElementStyleObj = function(ele)
	{
		var styleObj = CF.styleStringToObj(CF.eleStyle(ele));
		styleObj.className = (CF.eleAttr(ele, "class") || "");
		
		return styleObj;
	};
	
	DE._spitStyleAndOption = function(styleObj)
	{
		var optionObj =
		{
			syncChartTheme: CF.isLiteralTrue(styleObj.syncChartTheme),
			className: styleObj.className
		};
		
		var plainStyleObj = CF.extend({}, styleObj);
		plainStyleObj.syncChartTheme = undefined;
		plainStyleObj.className = undefined;
		
		var re =
		{
			style: plainStyleObj,
			option: optionObj
		};
		
		return re;
	};
	
	DE._evalElementChartThemeByStyleObj = function(chartEle, styleEle, styleObj)
	{
		var nowTheme = DE._getElementChartTheme(chartEle);
		var styleTheme = { color: null, actualBackgroundColor: null, fontSize: null };
		
		var color = styleObj['color'];
		var bgColor = styleObj['background-color'];
		var fontSize = styleObj['font-size'];
		
		if(!CF.isEmpty(color))
			styleTheme.color = color;
		
		//始终将图表元素的背景色置为null，因为背景色会自动继承父级元素
		styleTheme.backgroundColor = null;
		
		if(!CF.isEmpty(bgColor))
		{
			//应忽略透明度
			var bgColorObj = CF.parseColor(bgColor);
			bgColorObj.a = undefined;
			styleTheme.actualBackgroundColor = CF.colorToHexStr(bgColorObj, true);
		}
		
		if(!CF.isEmpty(fontSize))
		{
			//从元素的css中取才能获取字体尺寸像素数
			styleTheme.fontSize = CF.eleCss(styleEle, "font-size");
		}
		
		if(!nowTheme)
		{
			return styleTheme;
		}
		else
		{
			nowTheme.color = styleTheme.color;
			nowTheme.backgroundColor = styleTheme.backgroundColor;
			nowTheme.actualBackgroundColor = styleTheme.actualBackgroundColor;
			nowTheme.fontSize = styleTheme.fontSize;
			
			return nowTheme;
		}
	};
	
	DE._reSelectElementIf = function(ele)
	{
		if(DE._isEmptyElement(ele))
			return false;
		
		var currentEle = DE._currentElement(null, true);
		
		if(DE._isEmptyElement(currentEle))
			return false;
		
		if(ele === currentEle)
		{
			DE.selectElement(ele);
			return true;
		}
		else
		{
			return false;
		}
	};
	
	DE._currentElement = function(currentEle, excludeBody)
	{
		excludeBody = (excludeBody === undefined ? false : excludeBody);
		
		currentEle = (DE._isEmptyElement(currentEle) ? DE._selectedElement() : currentEle);
		
		if(!excludeBody)
			currentEle = (DE._isEmptyElement(currentEle) ? document.body : currentEle);
		
		return currentEle;
	};
	
	DE._addVisualEditIdAttr = function(ele)
	{
		var veId = DE._nextVisualEditId();
		CF.eleAttr(ele, ELEMENT_ATTR_VISUAL_EDIT_ID, veId);
		
		var children = CF.elesOfChildren(ele);
		
		if(CF.isEmpty(children))
			return;
		
		children.forEach((child) =>
		{
			DE._addVisualEditIdAttr(child);
		});
		
		return veId;
	};
	
	DE._genFormatTabs = function(count)
	{
		var re = "";
		
		for(var i=0; i<count; i++)
			re += "\t";
		
		return re;
	};
	
	DE._evalElementLevel = function(ele)
	{
		var level = 0;
		
		var tmpEle = ele;
		
		while(!DE._isEmptyElement(tmpEle) && !DE._isBodyEle(tmpEle))
		{
			level += 1;
			tmpEle = CF.eleOfParent(tmpEle);
		}
		
		if(level > 0 && !DE._isEmptyElement(tmpEle) && DE._isBodyEle(tmpEle))
		{
			//编辑HTML做了转换，多内嵌了一层，参考DE._toEditIframeBodyHtml()函数，所以这里要减一层
			if(CF.eleHasClass(tmpEle, EDIT_BODY_CLASS_FLAG))
			{
				level -= 1;
			}
			//展示HTML内的元素，应根据对应的编辑HTML内元素计算层级，因为展示HTML内的可能是被自定义引入的UI库动态调整层级了
			else if(CF.eleHasClass(tmpEle, SHOW_BODY_CLASS_FLAG))
			{
				var editEle = DE._editElement(ele);
				level = DE._evalElementLevel(editEle);
			}
		}
		
		return level;
	};
	
	DE._isOnlyEmptyOrFormat = function(text)
	{
		if(CF.isEmpty(text))
			return true;
		
		if(/^\s*$/.test(text))
			return true;
		
		if(/^(\s*\<\!\-\-((dgInsertFmtStart)|(dgInsertFmtEnd)|(dgDeleteFmtFlag))\-\-\>\s*)*$/i.test(text))
			return true;
		
		return false;
	};
	
	DE._getInsertParentElement = function(refEle, insertType)
	{
		var insertParentEle = null;
		
		if(DE._isBodyEle(refEle))
			insertParentEle = refEle;
		else if(INSERT_TYPE_AFTER == insertType || INSERT_TYPE_BEFORE == insertType)
			insertParentEle = CF.eleOfParent(refEle);
		else
			insertParentEle = refEle;
		
		return insertParentEle;
	};
	
	DE._trimInsertType = function(refEle, insertType)
	{
		insertType = (!insertType ? INSERT_TYPE_AFTER : insertType);
		insertType = (insertType == INSERT_TYPE_AFTER || insertType == INSERT_TYPE_BEFORE
						|| insertType == INSERT_TYPE_APPEND || insertType == INSERT_TYPE_PREPEND ? insertType : INSERT_TYPE_AFTER);
		
		if(DE._isBodyEle(refEle))
		{
			if(insertType == INSERT_TYPE_AFTER)
				insertType = INSERT_TYPE_APPEND;
			else if(insertType == INSERT_TYPE_BEFORE)
				insertType = INSERT_TYPE_PREPEND;
		}
		
		return insertType;
	};
	
	//获取元素本身、子孙元素中所有的图表元素
	//注意：返回的图表元素中可能有还未渲染为图表的元素
	DE._getChartElements = function(ele)
	{
		return CF.elesWithWidgetId(ele).elements;
	};
	
	DE._selectedElements = function(root)
	{
		if(root == null)
			return CF.elesOfSelector("."+ELEMENT_CLASS_SELECTED);
		else
		{
			var re = CF.elesOfSelector("."+ELEMENT_CLASS_SELECTED, root);
			
			if(DE._isSelectedElement(root))
				re.push(root);
			
			return re;
		}
	};
	
	DE._selectedElement = function(root)
	{
		if(root == null)
			return CF.eleOfSelector("."+ELEMENT_CLASS_SELECTED);
		else
			return CF.eleOfSelector("."+ELEMENT_CLASS_SELECTED, root);
	};
	
	DE._isSelectedElement = function(ele)
	{
		return CF.eleHasClass(ele, ELEMENT_CLASS_SELECTED);
	};
	
	DE._selectElement = function(ele)
	{
		CF.eleAddClass(ele, ELEMENT_CLASS_SELECTED);
	};
	
	DE._deselectElement = function(ele)
	{
		CF.eleRemoveClass(ele, ELEMENT_CLASS_SELECTED);
	};
	
	DE._removeElementClassNewInsert = function(ele)
	{
		while(ele != null)
		{
			CF.eleRemoveClass(ele, ELEMENT_CLASS_NEW_INSERT);
			ele = CF.eleOfParent(ele);
		}
	};
	
	DE._checkNotEmptyElement = function(ele)
	{
		if(DE._isEmptyElement(ele))
		{
			DE.tipInfo(i18n.selectedElementRequired);
			return false;
		}
		
		return true;
	};
	
	DE._isEmptyElement = function(ele)
	{
		return (ele == null);
	};
	
	DE._getVisualEditId = function(ele)
	{
		return CF.eleAttr(ele, ELEMENT_ATTR_VISUAL_EDIT_ID);
	};
	
	DE._getEleByVisualEditId = function(editId, forEditEle)
	{
		forEditEle = (forEditEle === undefined ? false : forEditEle);
		
		var re = CF.eleOfSelector("["+ELEMENT_ATTR_VISUAL_EDIT_ID+"='"+editId+"']");
		
		if(forEditEle)
			re = DE._editElement(re);
		
		return re;
	};
	
	DE._nextVisualEditId = function()
	{
		return CF.uid();
	};
	
	/**
	 * 设置编辑页面样式。
	 *
	 * @param options 可选，格式为：{ selectedBorderColor: "..." }
	 */
	DE._setPageStyle = function(options)
	{
		options = CF.extend(
		{
			selectedBorderColor: CF.eleCss(document.body, "color")
		},
		options);
		
		CF.styleSheetText("dg-show-ve-style", DE._buildPageStyleText(options));
	};
	
	DE._buildPageStyleText = function(options)
	{
		var bgColorNew = CF.parseColor(options.selectedBorderColor);
		bgColorNew.a = 0.1;
		bgColorNew = CF.colorToHexStr(bgColorNew, true);
		
		var re = "\n"
			+ "."+BODY_CLASS_VISUAL_EDITOR+"."+BODY_CLASS_ELEMENT_BOUNDARY+" *["+ELEMENT_ATTR_VISUAL_EDIT_ID+"]{\n"
			+ "  box-shadow: inset 0 0 1px 1px " + options.selectedBorderColor + ",0 0 1px 1px " + options.selectedBorderColor + ";"
			+ "\n}"
			+ "\n"
			+ "."+BODY_CLASS_VISUAL_EDITOR+" ."+ELEMENT_CLASS_SELECTED+",\n"
			+ "."+BODY_CLASS_VISUAL_EDITOR+"."+BODY_CLASS_ELEMENT_BOUNDARY+" ."+ELEMENT_CLASS_SELECTED+"{\n"
			+ "  box-shadow: inset 0 0 2px 2px " + options.selectedBorderColor + ",0 0 2px 2px " + options.selectedBorderColor + " !important;"
			+ "\n}"
			+ "\n"
			+ "."+BODY_CLASS_VISUAL_EDITOR+" ."+ELEMENT_CLASS_NEW_INSERT+",\n"
			+ "."+BODY_CLASS_VISUAL_EDITOR+"."+BODY_CLASS_ELEMENT_BOUNDARY+" ."+ELEMENT_CLASS_NEW_INSERT+"{\n"
			+ "  box-shadow: inset 0 0 1px 1px " + options.selectedBorderColor + ";"
			+ "  background-color: " + bgColorNew + ";"
			+ "\n}";
		
		return re;
	};
	
	//获取编辑HTML信息
	//结构参考：org.datagear.web.controller.DashboardVisualController.DashboardShowForEdit.EditHtmlInfo
	DE._editHtmlInfo = function()
	{
		return DE.dashboard.renderContextValue(RENDER_CONTEXT_ATTR_EDIT_HTML_INFO);
	};
	
	//反转义编辑HTML（转义操作由后台执行）
	DE._unescapeEditHtml = function(editHtml)
	{
		return (editHtml ? editHtml.replace(/<\\\//g, "</") : editHtml);
	};
	
	DE._spitIgnoreEmpty = function(str, splitter)
	{
		splitter = (splitter ? splitter : ",");
		
		var ary = [];
		
		if(!str)
			return ary;
		
		ary = str.split(splitter);
		
		var re = [];
		
		for(var i=0; i<ary.length; i++)
		{
			var ele = CF.trim(ary[i]);
			if(ele)
				re.push(ele);
		}
		
		return re;
	};
	
	//将符合JSON规范的对象序列化为元素属性值字符串
	//注意：此函数使用单引号而非双引号作为引号符，因为双引号会被HTML转义为'&quot;'，对源码不友好
	DE._serializeForAttrValue = function(obj)
	{
		if(obj == null)
			return null;
		
		if(CF.isString(obj))
		{
			return DE._toSingleQuoteJsString(obj, true);
		}
		else if(CF.isNumber(obj) || CF.isBoolean(obj))
		{
			return obj;
		}
		else if(CF.isArray(obj))
		{
			var str = "[";
			
			for(var i=0; i<obj.length; i++)
			{
				var vstr = DE._serializeForAttrValue(obj[i]);
				if(vstr != null && vstr !== "")
				{
					if(str != "[")
						str += ",";
					
					str += vstr;
				}
			}
			
			str += "]";
			
			return str;
		}
		else if(CF.isPlainObject(obj))
		{
			var str = "{";
			
			for(var p in obj)
			{
				var vstr = DE._serializeForAttrValue(obj[p]);
				if(vstr != null && vstr !== "")
				{
					if(str != "{")
						str += ",";
					
					str += DE._serializeForAttrValue(p) + ":" + vstr;
				}
			}
			
			str += "}";
			
			return str;
		}
		else
			return DE._serializeForAttrValue(obj.toString());
	};
	
	DE._toSingleQuoteJsString = function(str, quote)
	{
		quote = (quote === undefined ? false : quote);
		
		if(str == null)
			return str;
		
		var re = (quote ? "'" : "");
		
		for(var i=0; i<str.length; i++)
		{
			var c = str.charAt(i);
			
			if(c == '\'')
				re += "\\'";
			else if(c == '\n')
				re += "\\n";
			else if(c == '\r')
				re += "\\r";
			else if(c == '\t')
				re += "\\t";
			else if(c == '\\')
				re += "\\\\";
			else
				re += c;
		}
		
		if(quote)
			re += "'";
		
		return re;
	};
	
	DE._isEmptyJsonObjStr = function(str)
	{
		if(CF.isEmpty(str))
			return true;
		
		return /^\s*\{\s*\}\s*$/i.test(str);
	};
	
	/**
	 * 获取编辑iframe，也可设置其HTML。
	 * 
	 * 这里的editBodyHtml应只使用"<body>...</body>"，因为渲染iframe页面时，如果"<body>"前、"</body>"后里有不合规的元素，
	 * 可能会被渲染至<body></body>内，导致【结果HTML】还原不对。
	 * 
	 * @param editBodyHtml 
	 */
	DE._editIframe = function(editBodyHtml)
	{
		var id = (DE._editIframeId != null ? DE._editIframeId
					: (DE._editIframeId = CF.uid()));
		
		var iframe = CF.eleOfId(id);
		
		if(iframe == null)
		{
			iframe = CF.eleCreateWithAttr("iframe", "class", "dg-edit-html-ifm", "style", "display:none;",
						"name", "id", "id", id);
			CF.eleAppend(document.body, iframe);
		}
		
		if(editBodyHtml != null)
		{
			var editIframeBodyHtml = DE._toEditIframeBodyHtml(editBodyHtml);
			
			var editDoc = DE._editDocument();
			editDoc.open();
			editDoc.write("<!DOCTYPE html><html><head></head><body class='"+EDIT_BODY_CLASS_FLAG+"'>");
			editDoc.write(editIframeBodyHtml);
			editDoc.write("</body></html>");
			editDoc.close();
			
			DE.changeFlag(true);
		}
		
		return iframe;
	};
	
	//将"<body>...</body>"转换为"<div>...</div>"，使得可以直接使用：CF.eleHtml(document.body, "...");
	DE._toEditIframeBodyHtml = function(editBodyHtml)
	{
		var startTagRegex = /^\s*<body/i;
		var endTagRegex = /\/body>\s*$/i;
		
		var editIframeBodyHtml = editBodyHtml.replace(startTagRegex, "<div");
		editIframeBodyHtml = editIframeBodyHtml.replace(endTagRegex, "/div>");
		
		return editIframeBodyHtml;
	};
	
	//将由DE._toEditIframeBodyHtml()转换的"<div>...</div>"恢复为"<body>...</body>"
	DE._fromEditIframeBodyHtml = function(editIframeBodyHtml)
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
	DE._editDocument = function(iframe)
	{
		iframe = (iframe == null ? DE._editIframe() : iframe);
		return (iframe.contentDocument || iframe.contentWindow.document);
	};
	
	//获取编辑HTML的<body>...</body>内容
	DE._editBodyHtml = function()
	{
		var editDoc = DE._editDocument();
		var editIframeBodyHtml = CF.eleHtml(editDoc.body);
		
		return DE._fromEditIframeBodyHtml(editIframeBodyHtml);
	};
	
	/**
	 * 获取编辑iframe中的元素。
	 * 
	 * @param ele 展示元素
	 */
	DE._editElement = function(ele)
	{
		var editDoc = DE._editDocument();
		
		if(DE._isBodyEle(ele))
		{
			// <body>被转换为了<div>，参考DE._toEditIframeBodyHtml()函数
			return CF.eleOfSelector("div", editDoc.body);
		}
		
		var editId = (DE._getVisualEditId(ele) || "");
		return CF.eleOfSelector("["+ELEMENT_ATTR_VISUAL_EDIT_ID+"='"+editId+"']", editDoc.body);
	};
	
	DE._evalTopWindowSize = function()
	{
		var topWindow = window;
		while(topWindow.parent  && topWindow.parent != topWindow)
			topWindow = topWindow.parent;
		
		var size =
		{
			width: topWindow.clientWidth,
			height: topWindow.clientHeight
		};
		
		return size;
	};
	
	DE._isJsonString = function(str)
	{
		return CF.isJsonString(str);
	};
	
	DE._isDisplayGrid = function(display)
	{
		display = DE._displayCssValue(display);
		
		if(!display)
			return false;
		
		return /^(grid|inline-grid)$/i.test(display);
	};
	
	DE._isDisplayFlex = function(display)
	{
		display = DE._displayCssValue(display);
		
		if(!display)
			return false;
		
		return /^(flex|inline-flex)$/i.test(display);
	};
	
	DE._displayCssValue = function(display)
	{
		if(!display)
			return display;
		
		//是DOM元素
		if(!CF.isStringOrNumber(display))
		{
			display = CF.eleCss(display, "display");
		}
		
		return display;
	};
	
	DE._evalInsertLayoutHeightStyle = function(fillParent, parentEle)
	{
		fillParent = CF.isLiteralTrue(fillParent);
		
		var re = "";	
		var isBodyParent = DE._isBodyEle(parentEle);
		
		if(fillParent)
		{
			re = "height:100%;";
		}
		else if(isBodyParent)
			re = "height:300px;";
		else
			re = "height:100%;";
		
		return re;
	};
})
(this);
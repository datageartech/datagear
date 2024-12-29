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
	i18n.imgEleRequired = "不是图片元素";
	i18n.hyperlinkEleRequired = "不是超链接元素";
	i18n.videoEleRequired = "不是视频元素";
	i18n.labelEleRequired = "不是文本标签元素";
	i18n.chartPluginNoAttrDefined = "此类型图表插件没有定义可编辑属性";
	
	//参考org.datagear.web.controller.DashboardVisualController.DASHBOARD_BUILTIN_RENDER_CONTEXT_ATTR_EDIT_HTML_INFO
	var DASHBOARD_BUILTIN_RENDER_CONTEXT_ATTR_EDIT_HTML_INFO = (editor.DASHBOARD_BUILTIN_RENDER_CONTEXT_ATTR_EDIT_HTML_INFO = "DG_EDIT_HTML_INFO");
	
	var BODY_CLASS_VISUAL_EDITOR = (editor.BODY_CLASS_VISUAL_EDITOR = "dg-show-ve");
	
	//参考org.datagear.web.controller.DashboardVisualController.DashboardShowForEdit.ELEMENT_ATTR_VISUAL_EDIT_ID
	var ELEMENT_ATTR_VISUAL_EDIT_ID = (editor.ELEMENT_ATTR_VISUAL_EDIT_ID = "dg-visual-edit-id");
	
	var ELEMENT_CLASS_SELECTED = (editor.ELEMENT_CLASS_SELECTED = "dg-show-ve-selected");
	
	var ELEMENT_CLASS_NEW_INSERT = (editor.ELEMENT_CLASS_NEW_INSERT = "dg-show-ve-new-insert");
	
	var BODY_CLASS_ELEMENT_BOUNDARY = (editor.BODY_CLASS_ELEMENT_BOUNDARY = "dg-show-ve-boundary");
	
	var INSERT_ELE_FORMAT_FLAG = (editor.INSERT_ELE_FORMAT_FLAG = "<!--dg-format-flag-->");
	
	//参考org.datagear.web.controller.DashboardVisualController.LOAD_CHART_FOR_EDITOR_PARAM
	var LOAD_CHART_FOR_EDITOR_PARAM = (editor.LOAD_CHART_FOR_EDITOR_PARAM = "loadChartForEditor");
	
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
		$(function()
		{
			$(document.body).addClass(BODY_CLASS_VISUAL_EDITOR);
			
			$(document.body).on("click", function(event)
			{
				editor._removeElementClassNewInsert();
				
				var target = $(event.target);
				var veEle = (target.attr(ELEMENT_ATTR_VISUAL_EDIT_ID) ? target :
									target.closest("["+ELEMENT_ATTR_VISUAL_EDIT_ID+"]"));
				
				if(veEle.length == 0)
				{
					editor.deselectElement();
				}
				else
				{
					if(!editor._isSelectableElement(veEle))
					{
						editor.deselectElement();
					}
					else if(editor._isSelectedElement(veEle))
					{
						//再次点击选中元素，不取消选择
					}
					else
					{
						editor.selectElement(veEle);
					}
				}
				
				if(editor.clickCallback)
					editor.clickCallback(event);
			});
			
			$(window).on("beforeunload", function()
			{
				editor.beforeunloadCallback();
			});
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
		
		//删除插入元素后又删除元素遗留的多余格式符
		var insertFormatRegex0 = /\n\<\!\-\-dg\-format\-flag\-\-\>\n\s*(\n\<\!\-\-dg\-format\-flag\-\-\>\n)+/gi;
		editBodyHtml = editBodyHtml.replace(insertFormatRegex0, "\n");
		
		//删除插入元素时的格式符
		var insertFormatRegex1 = /\n\<\!\-\-dg\-format\-flag\-\-\>\n/gi;
		editBodyHtml = editBodyHtml.replace(insertFormatRegex1, "\n");
		
		var editedHtml = editHtmlInfo.beforeBodyHtml + editBodyHtml + editHtmlInfo.afterBodyHtml;
		return this._unescapeEditHtml(editedHtml);
	};
	
	/**
	 * 是否在指定changeFlag后有修改。
	 *
	 * @param changeFlag 待比较的变更标识
	 */
	editor.isChanged = function(changeFlag)
	{
		return (this.changeFlag() != changeFlag);
	};
	
	/**
	 * 获取/设置变更标识。
	 *
	 * @param set 可选，要设置的变更标识，格式为：true 自增，数值 设置明确值
	 */
	editor.changeFlag = function(set)
	{
		if(this._changeFlag == null)
			this._changeFlag = 0;
		
		if(set == true)
		{
			this._changeFlag++;
		}
		else if(chartFactory.isNumber(set))
		{
			this._changeFlag = set;
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
	
	//页面点击回调函数，格式为：function(event){}
	editor.clickCallback = function(event)
	{
		
	};
	
	/**
	 * 选择元素回调函数。
	 * 
	 * @param ele JQ元素
	 */
	editor.selectElementCallback = function(ele)
	{
		
	};
	
	/**
	 * 取消选择元素回调函数。
	 * 
	 * @param ele JQ元素
	 */
	editor.deselectElementCallback = function(ele)
	{
		
	};
	
	//页面卸载前回调函数，比如：保存编辑HTML
	editor.beforeunloadCallback = function()
	{
		
	};
	
	/**
	 * 获取/设置元素边界线启用禁用/状态。
	 *
	 * @param enable 可选，true 启用；false 禁用。
	 * @returns 是否已启用 
	 */
	editor.enableElementBoundary = function(enable)
	{
		var body = $(document.body);
		
		if(arguments.length == 0)
			return body.hasClass(BODY_CLASS_ELEMENT_BOUNDARY);
		
		if(enable)
			body.addClass(BODY_CLASS_ELEMENT_BOUNDARY);
		else
			body.removeClass(BODY_CLASS_ELEMENT_BOUNDARY);
	};
	
	/**
	 * 是否未选中任何元素。
	 */
	editor.isNonSelectedElement = function()
	{
		var selected = this._selectedElement();
		return (selected.length == 0);
	};
	
	/**
	 * 获取元素的可视编辑ID。
	 * 
	 * @param ele 可选，元素，默认为：当前选中元素
	 */
	editor.getElementVisualEditId = function(ele)
	{
		ele = this._currentElement(ele, true);
		return this._getVisualEditId(ele);
	};
	
	/**
	 * 选中指定元素。
	 * 
	 * @param eleOrVisualEditId 元素、元素可编辑ID
	 * @returns true 已选择，false 未选择
	 */
	editor.selectElement = function(eleOrVisualEditId)
	{
		var ele = eleOrVisualEditId;
		
		if(chartFactory.isString(ele))
			ele = $("["+ELEMENT_ATTR_VISUAL_EDIT_ID+"='"+ele+"']");
		
		this._removeElementClassNewInsert();
		
		this.deselectElement();
		
		if(ele && ele.length > 0)
		{
			this._selectElement(ele);
			
			if(this.selectElementCallback)
				this.selectElementCallback(ele);
			
			return true;
		}
		
		return false;
	};
	
	/**
	 * 取消选中元素。
	 * 
	 * @param ele 可选，元素，默认为：当前选中元素
	 */
	editor.deselectElement = function(ele)
	{
		ele = this._currentElement(ele, true);
		
		this._removeElementClassNewInsert();
		
		if(ele.length > 0)
		{
			this._deselectElement(ele);
			
			if(this.deselectElementCallback)
				this.deselectElementCallback(ele);
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
		
		ele = this._currentElement(ele);
		
		if(ele.is("body"))
			return this.selectFirstChildElement(ele, tip);
		
		var target = ele;
		while((target = target.next()))
		{
			if(target.length == 0 || this._isSelectableElement(target))
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
		
		return this.selectElement(target);
	};
	
	/**
	 * 选中前一个可编辑元素。
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
		
		ele = this._currentElement(ele);
		
		if(ele.is("body"))
			return this.selectFirstChildElement(ele, tip);
		
		var target = ele;
		while((target = target.prev()))
		{
			if(target.length == 0 || this._isSelectableElement(target))
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
		
		return this.selectElement(target);
	};
	
	/**
	 * 选中第一个可编辑子元素。
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
		
		ele = this._currentElement(ele);
		var firstChild = $("> *:first", ele);
		
		var target = firstChild;
		while(true)
		{
			if(target.length == 0 || this._isSelectableElement(target))
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
		
		return this.selectElement(target);
	};
	
	/**
	 * 选中可编辑上级元素。
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
		
		ele = this._currentElement(ele);
		
		if(ele.is("body"))
		{
			if(tip)
				this.tipInfo(i18n.noSelectableParentElement);
			return false;
		}
		
		var target = ele;
		while((target = target.parent()))
		{
			if(target.length == 0 || target.is("body") || this._isSelectableElement(target))
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
		
		return this.selectElement(target);
	};
	
	editor._isSelectableElement = function($ele)
	{
		if(!$ele.attr(ELEMENT_ATTR_VISUAL_EDIT_ID))
			return false;
		
		var tagName = ($ele[0].tagName || "").toLowerCase();
		
		if(chartFactory.isNullOrEmpty(tagName))
			return false;
		
		if(tagName == "body")
			return false;
		
		if(tagName == "script" || tagName == "style" || tagName == "template")
			return false;
		
		if($ele.is(":hidden"))
			return false;
		
		//没有尺寸的也忽略
		var w = $ele.outerWidth(), h = $ele.outerHeight();
		if(w == null || w <= 0 || h == null || h <= 0)
			return false;
		
		return true;
	};
	
	/**
	 * 是否是图表元素。
	 * 
	 * @param ele 可选，元素，默认为：当前选中元素
	 */
	editor.isChartElement = function(ele)
	{
		ele = this._currentElement(ele);
		return (this.dashboard.renderedChart(ele) != null);
	};
	
	/**
	 * 是否是网格布局条目元素。
	 * 
	 * @param ele 可选，元素，默认为：当前选中元素
	 */
	editor.isGridItemElement = function(ele)
	{
		ele = this._currentElement(ele, true);
		var parent = ele.parent();
		
		return this._isDisplayGrid(parent.css("display"));
	};
	
	/**
	 * 是否是弹性布局条目元素。
	 * 
	 * @param ele 可选，元素，默认为：当前选中元素
	 */
	editor.isFlexItemElement = function(ele)
	{
		ele = this._currentElement(ele, true);
		var parent = ele.parent();
		
		return this._isDisplayFlex(parent.css("display"));
	};
	
	/**
	 * 校验网格布局元素。
	 * 
	 * @param insertType 可选，参考insertElement函数的insertType参数
	 * @param refEle 可选，参考insertElement函数的refEle参数
	 */
	editor.checkInsertGridLayout = function(insertType, refEle)
	{
		return true;
	};
	
	/**
	 * 是否可以插入填满父元素的网格布局元素。
	 * 
	 * @param insertType 可选，参考insertElement函数的insertType参数
	 * @param refEle 可选，参考insertElement函数的refEle参数
	 */
	editor.canInsertFillParentGridLayout = function(insertType, refEle)
	{
		refEle = this._currentElement(refEle);
		insertType = this._trimInsertType(refEle, insertType);
		var insertParentEle = this._getInsertParentElement(refEle, insertType);
		
		if(!insertParentEle.is("body"))
			return false;
		
		var canInsert = true;
		
		//只有还未插入任何可选择元素时，才可以插入填满父容器元素
		insertParentEle.children().each(function()
		{
			if(editor._isSelectableElement($(this)))
				canInsert = false;
		});
		
		return canInsert;
	};
	
	/**
	 * 插入网格布局元素。
	 * 
	 * @param gridAttr 网格设置，格式为：{ rows: 数值或数值字符串, columns: 数值或数值字符串, fillParent: 布尔值或布尔值字符串 }
	 * @param insertType 可选，参考insertElement函数的insertType参数
	 * @param refEle 可选，参考insertElement函数的refEle参数
	 */
	editor.insertGridLayout = function(gridAttr, insertType, refEle)
	{
		refEle = this._currentElement(refEle);
		insertType = this._trimInsertType(refEle, insertType);
		
		var rows = (!chartFactory.isNumber(gridAttr.rows) ? parseInt(gridAttr.rows) : gridAttr.rows);
		var columns = (!chartFactory.isNumber(gridAttr.columns) ? parseInt(gridAttr.columns) : gridAttr.columns);
		
		//不能使用"<div />"，生成的源码格式不对
		var div = $("<div></div>");
		
		var styleStr = "";
		var insertParentEle = this._getInsertParentElement(refEle, insertType);
		var isBodyParent = insertParentEle.is("body");
		
		if(gridAttr.fillParent === "true" || gridAttr.fillParent === true)
		{
			if(isBodyParent)
			{
				this._setElementStyle(insertParentEle, this._fillBodyStyleByAbsolute());
			}
			
			styleStr += "width:100%;height:100%;";
		}
		else if(isBodyParent)
			styleStr += "width:100%;height:300px;";
		else
			styleStr += "width:100%;height:100%;";
		
		styleStr += "display:grid;";
		
		if(rows > 0)
			styleStr += "grid-template-rows:repeat("+rows+", 1fr);";
		if(columns > 0)
			styleStr += "grid-template-columns:repeat("+columns+", 1fr);";
		
		div.attr("style", styleStr);
		
		for(var i=0; i<rows; i++)
		{
			for(var j=0; j<columns; j++)
				this._insertElement(div, $("<div></div>"), "append");
		}
		
		this.insertElement(div, insertType, refEle);
	};
	
	/**
	 * 校验插入弹性布局元素。
	 * 
	 * @param insertType 可选，参考insertElement函数的insertType参数
	 * @param refEle 可选，参考insertElement函数的refEle参数
	 */
	editor.checkInsertFlexLayout = function(insertType, refEle)
	{
		return true;
	};
	
	/**
	 * 是否可以插入填满父元素的弹性布局元素。
	 * 
	 * @param insertType 可选，参考insertElement函数的insertType参数
	 * @param refEle 可选，参考insertElement函数的refEle参数
	 */
	editor.canInsertFillParentFlexLayout = function(insertType, refEle)
	{
		return this.canInsertFillParentGridLayout(insertType, refEle);
	};
	
	/**
	 * 插入弹性布局元素。
	 * 
	 * @param flexAttr 网格设置，格式为：{ items: 数值或数值字符串, direction: "...", fillParent: 布尔值或布尔值字符串 }
	 * @param insertType 可选，参考insertElement函数的insertType参数
	 * @param refEle 可选，参考insertElement函数的refEle参数
	 */
	editor.insertFlexLayout = function(flexAttr, insertType, refEle)
	{
		refEle = this._currentElement(refEle);
		insertType = this._trimInsertType(refEle, insertType);
		
		var items = (!chartFactory.isNumber(flexAttr.items) ? parseInt(flexAttr.items) : flexAttr.items);
		
		//不能使用"<div />"，生成的源码格式不对
		var div = $("<div></div>");
		
		var styleStr = "";
		var insertParentEle = this._getInsertParentElement(refEle, insertType);
		var isBodyParent = insertParentEle.is("body");
		
		if(flexAttr.fillParent === "true" || flexAttr.fillParent === true)
		{
			if(isBodyParent)
			{
				this._setElementStyle(insertParentEle, this._fillBodyStyleByAbsolute());
			}
			
			styleStr += "width:100%;height:100%;";
		}
		else if(isBodyParent)
			styleStr += "width:100%;height:300px;";
		else
			styleStr += "width:100%;height:100%;";
		
		styleStr += "display:flex;"+(flexAttr.direction ? "flex-direction:"+flexAttr.direction+";" : "")
						+"justify-content:space-between;align-items:stretch;";
		
		div.attr("style", styleStr);
		
		for(var i=0; i<items; i++)
		{
			var itemDiv = $("<div></div>");
			itemDiv.attr("style", "flex-grow:1;");
			this._insertElement(div, itemDiv, "append");
		}
		
		this.insertElement(div, insertType, refEle);
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
		refEle = this._currentElement(refEle);
		insertType = this._trimInsertType(refEle, insertType);
		
		//不能使用"<div />"，生成的源码格式不对
		var div = $("<div></div>");
		
		var styleStr = "";
		var insertParentEle = this._getInsertParentElement(refEle, insertType);
		
		if(insertParentEle.is("body"))
			styleStr = "width:100%;height:100px;";
		else
			styleStr = "width:100px;height:100px;";
		
		div.attr("style", styleStr);
		
		this.insertElement(div, insertType, refEle);
	};
	
	/**
	 * 校验insertImage操作。
	 * 
	 * @param insertType 可选，参考insertElement函数的insertType参数
	 * @param refEle 可选，参考insertElement函数的refEle参数
	 */
	editor.checkInsertImage = function(insertType, refEle)
	{
		return true;
	};
	
	/**
	 * 插入图片元素。
	 * 
	 * @param imgAttr 图片设置，格式为：{ src: "", width: ..., height: ... }
	 * @param insertType 可选，参考insertElement函数的insertType参数
	 * @param refEle 可选，参考insertElement函数的refEle参数
	 */
	editor.insertImage = function(imgAttr, insertType, refEle)
	{
		refEle = this._currentElement(refEle);
		insertType = this._trimInsertType(refEle, insertType);
		
		var img = $("<img>");
		
		this.insertElement(img, insertType, refEle);
		this.setImageAttr(imgAttr, img);
	};
	
	/**
	 * 元素是否是图片。
	 * 
	 * @param ele 可选，参考insertElement函数的refEle参数
	 */
	editor.isImage = function(ele)
	{
		ele = this._currentElement(ele);
		return ele.is("img");
	};
	
	/**
	 * 获取图片元素属性。
	 * 
	 * @param ele 可选，参考insertElement函数的refEle参数
	 */
	editor.getImageAttr = function(ele)
	{
		ele = this._currentElement(ele);
		
		var attrObj = {};
		
		if(!this.isImage(ele))
			return attrObj;
		
		ele = this._editElement(ele);
		
		var eleStyle = this.getElementStyle(ele);
		
		attrObj.src = (ele.attr("src") || "");
		attrObj.width = eleStyle.width;
		attrObj.height = eleStyle.height;
		
		return attrObj;
	};
	
	/**
	 * 设置图片元素属性。
	 * 
	 * @param imgAttr 图片设置，格式为：{ src: "", width: ..., height: ... }
	 * @param ele 可选，参考insertElement函数的refEle参数
	 */
	editor.setImageAttr = function(imgAttr, ele)
	{
		ele = this._currentElement(ele, true);
		
		if(!this.isImage(ele))
		{
			this.tipInfo(i18n.imgEleRequired);
			return;
		}
		
		var eleStyle = { width: imgAttr.width, height: imgAttr.height };
		
		this._setElementAttr(ele, "src", (imgAttr.src || ""));
		this._setElementStyle(ele, eleStyle);
	};
	
	/**
	 * 校验insertHyperlink操作。
	 * 
	 * @param insertType 可选，参考insertElement函数的insertType参数
	 * @param refEle 可选，参考insertElement函数的refEle参数
	 */
	editor.checkInsertHyperlink = function(insertType, refEle)
	{
		return true;
	};
	
	/**
	 * 插入超链接元素。
	 * 
	 * @param hyperlinkAttr 超链接设置，格式为：{ content: "...", href: "...", target: "..." }
	 * @param insertType 可选，参考insertElement函数的insertType参数
	 * @param refEle 可选，参考insertElement函数的refEle参数
	 */
	editor.insertHyperlink = function(hyperlinkAttr, insertType, refEle)
	{
		refEle = this._currentElement(refEle);
		insertType = this._trimInsertType(refEle, insertType);
		
		var a = $("<a></a>");
		
		this.insertElement(a, insertType, refEle);
		this.setHyperlinkAttr(hyperlinkAttr, a);
	};
	
	/**
	 * 元素是否是超链接。
	 * 
	 * @param ele 可选，参考insertElement函数的refEle参数
	 */
	editor.isHyperlink = function(ele)
	{
		ele = this._currentElement(ele);
		return ele.is("a");
	};
	
	/**
	 * 获取超链接元素属性。
	 * 
	 * @param ele 可选，参考insertElement函数的refEle参数
	 */
	editor.getHyperlinkAttr = function(ele)
	{
		ele = this._currentElement(ele);
		
		var attrObj = {};
		
		if(!this.isHyperlink(ele))
			return attrObj;
		
		ele = this._editElement(ele);
		
		attrObj.content = $.trim(ele.html());
		attrObj.href = (ele.attr("href") || "");
		attrObj.target = (ele.attr("target") || "");
		
		return attrObj;
	};
	
	/**
	 * 设置超链接元素属性。
	 * 
	 * @param hyperlinkAttr 超链接设置，格式为：{ content: "...", href: "...", target: "..." }
	 * @param ele 可选，参考insertElement函数的refEle参数
	 */
	editor.setHyperlinkAttr = function(hyperlinkAttr, ele)
	{
		ele = this._currentElement(ele, true);
		
		if(!this.isHyperlink(ele))
		{
			this.tipInfo(i18n.hyperlinkEleRequired);
			return;
		}
		
		this._setElementText(ele, (hyperlinkAttr.content || hyperlinkAttr.href || ""));
		this._setElementAttr(ele, "href", (hyperlinkAttr.href || ""));
		if(hyperlinkAttr.target)
			this._setElementAttr(ele, "target", hyperlinkAttr.target);
		else
			this._removeElementAttr(ele, "target");
	};
	
	/**
	 * 校验insertVideo操作。
	 * 
	 * @param insertType 可选，参考insertElement函数的insertType参数
	 * @param refEle 可选，参考insertElement函数的refEle参数
	 */
	editor.checkInsertVideo = function(insertType, refEle)
	{
		return true;
	};
	
	/**
	 * 插入视频元素。
	 * 
	 * @param videoAttr 视频设置，格式为：{ src: "", width: ..., height: ... }
	 * @param insertType 可选，参考insertElement函数的insertType参数
	 * @param refEle 可选，参考insertElement函数的refEle参数
	 */
	editor.insertVideo = function(videoAttr, insertType, refEle)
	{
		refEle = this._currentElement(refEle);
		insertType = this._trimInsertType(refEle, insertType);
		
		var ele = $("<video controls=\"controls\"></video>");
		
		this.insertElement(ele, insertType, refEle);
		this.setVideoAttr(videoAttr, ele);
	};
	
	/**
	 * 是否是视频元素。
	 * 
	 * @param ele 可选，参考insertElement函数的refEle参数
	 */
	editor.isVideo = function(ele)
	{
		ele = this._currentElement(ele);
		return ele.is("video");
	};
	
	/**
	 * 获取视频元素属性。
	 * 
	 * @param ele 可选，参考insertElement函数的refEle参数
	 */
	editor.getVideoAttr = function(ele)
	{
		ele = this._currentElement(ele);
		
		var attrObj = {};
		
		if(!this.isVideo(ele))
			return attrObj;
		
		ele = this._editElement(ele);
		
		var eleStyle = this.getElementStyle(ele);
		
		attrObj.src = (ele.attr("src") || "");
		attrObj.width = eleStyle.width;
		attrObj.height = eleStyle.height;
		
		return attrObj;
	};
	
	/**
	 * 设置视频元素属性。
	 * 
	 * @param videoAttr 视频设置，格式为：{ src: "", width: ..., height: ... }
	 * @param ele 可选，参考insertElement函数的refEle参数
	 */
	editor.setVideoAttr = function(videoAttr, ele)
	{
		ele = this._currentElement(ele, true);
		
		if(!this.isVideo(ele))
		{
			this.tipInfo(i18n.videoEleRequired);
			return;
		}
		
		var eleStyle = { width: videoAttr.width, height: videoAttr.height };
		
		this._setElementAttr(ele, "src", (videoAttr.src || ""));
		this._setElementStyle(ele, eleStyle);
	};
	
	/**
	 * 校验insertHxtitle操作。
	 * 
	 * @param insertType 可选，参考insertElement函数的insertType参数
	 * @param refEle 可选，参考insertElement函数的refEle参数
	 */
	editor.checkInsertHxtitle = function(insertType, refEle)
	{
		return true;
	};
	
	/**
	 * 插入h1-h6元素。
	 * 
	 * @param model 标题模型，格式为：{ type: "h1到h6", content: "", textAlign: "" }
	 * @param insertType 可选，参考insertElement函数的insertType参数
	 * @param refEle 可选，参考insertElement函数的refEle参数
	 */
	editor.insertHxtitle = function(model, insertType, refEle)
	{
		model.type = (model.type || "h1");
		refEle = this._currentElement(refEle);
		insertType = this._trimInsertType(refEle, insertType);
		
		var ele = $("<"+model.type+"></"+model.type+">");
		
		if(model.textAlign)
			ele.attr("style", "text-align:"+model.textAlign+";");
		
		ele.html(model.content || "");
		
		this.insertElement(ele, insertType, refEle);
	};
	
	/**
	 * 校验insertLabel操作。
	 * 
	 * @param insertType 可选，参考insertElement函数的insertType参数
	 * @param refEle 可选，参考insertElement函数的refEle参数
	 */
	editor.checkInsertLabel = function(insertType, refEle)
	{
		return true;
	};
	
	/**
	 * 插入标签元素。
	 * 
	 * @param labelAttr 标签设置，格式为：{ content: "" }
	 * @param insertType 可选，参考insertElement函数的insertType参数
	 * @param refEle 可选，参考insertElement函数的refEle参数
	 */
	editor.insertLabel = function(labelAttr, insertType, refEle)
	{
		refEle = this._currentElement(refEle);
		insertType = this._trimInsertType(refEle, insertType);
		
		var ele = $("<label></label>");
		ele.html(labelAttr.content || "");
		
		this.insertElement(ele, insertType, refEle);
	};
	
	/**
	 * 是否是标签元素。
	 * 
	 * @param ele 可选，参考insertElement函数的refEle参数
	 */
	editor.isLabel = function(ele)
	{
		ele = this._currentElement(ele);
		return ele.is("label");
	};
	
	/**
	 * 获取标签元素属性。
	 * 
	 * @param ele 可选，参考insertElement函数的refEle参数
	 */
	editor.getLabelAttr = function(ele)
	{
		ele = this._currentElement(ele);
		
		var attrObj = {};
		
		if(!this.isLabel(ele))
			return attrObj;
		
		ele = this._editElement(ele);
		
		attrObj.content = $.trim(ele.html());
		
		return attrObj;
	};
	
	/**
	 * 设置标签元素属性。
	 * 
	 * @param labelAttr 标签设置，格式为：{ content: "..." }
	 * @param ele 可选，参考insertElement函数的refEle参数
	 */
	editor.setLabelAttr = function(labelAttr, ele)
	{
		ele = this._currentElement(ele, true);
		
		if(!this.isLabel(ele))
		{
			this.tipInfo(i18n.labelEleRequired);
			return;
		}
		
		this._setElementText(ele, (labelAttr.content || ""));
	};
	
	/**
	 * 校验insertChart操作。
	 * 
	 * @param insertType 可选，参考insertChart函数的insertType参数
	 * @param refEle 可选，参考insertChart函数的refEle参数
	 */
	editor.checkInsertChart = function(insertType, refEle)
	{
		refEle = this._currentElement(refEle);
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
		
		refEle = this._currentElement(refEle);
		insertType = this._trimInsertType(refEle, insertType);
		
		//图表元素内部不允许再插入图表元素
		if(!this.checkInsertChart(insertType, refEle))
			return;
		
		var styleStr = "";
		var insertParentEle = this._getInsertParentElement(refEle, insertType);
		
		if(insertParentEle.is("body"))
			styleStr = this.defaultInsertChartEleStyle;
		else
			styleStr = "width:100%;height:100%;";
		
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
		
		this.dashboard.loadUnsolvedCharts(this._buildLoadChartAjaxOptions());
	};
	
	editor._getInsertParentElement = function(refEle, insertType)
	{
		var insertParentEle = null;
		
		if(refEle.is("body"))
			insertParentEle = refEle;
		else if("after" == insertType || "before" == insertType)
			insertParentEle = refEle.parent();
		else
			insertParentEle = refEle;
		
		return insertParentEle;
	};
	
	/**
	 * 校验bindChart操作。
	 *
	 * @param ele 可选，要绑定的图表元素，默认为：当前选中图表元素
	 */
	editor.checkBindChart = function(ele)
	{
		ele = this._currentElement(ele, true);
		
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
		
		ele = this._currentElement(ele, true);
		
		if(!this.checkBindChart(ele))
			return;
		
		if(this.isChartElement(ele))
		{
			this.dashboard.removeChart(ele);
		}
		
		this._setElementAttr(ele, chartFactory.elementAttrConst.WIDGET, chartWidget.id);
		this.dashboard.loadChart(ele, this._buildLoadChartAjaxOptions());
	};
	
	/**
	 * 校验unbindChart操作。
	 *
	 * @param ele 可选，要解绑的图表元素，默认为：当前选中图表元素
	 */
	editor.checkUnbindChart = function(ele)
	{
		ele = this._currentElement(ele, true);
		
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
		ele = this._currentElement(ele, true);
		
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
		refEle = this._currentElement(refEle);
		insertType = this._trimInsertType(refEle, insertType);
		sync = (sync == null ? true : sync);
		
		if(chartFactory.isString(insertEle))
			insertEle = $(insertEle);
		
		this._addVisualEditIdAttr(insertEle);
		
		this._insertElement(refEle, insertEle, insertType);
		
		if(sync)
		{
			var editEle = this._editElement(refEle);
			var insertEleClone = insertEle.clone();
			this._insertElement(editEle, insertEleClone, insertType);
		}
		
		insertEle.addClass(ELEMENT_CLASS_NEW_INSERT);
		$("*", insertEle).addClass(ELEMENT_CLASS_NEW_INSERT);
		
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
		ele = this._editElement(this._currentElement(ele));
		return $.trim(ele.text());
	};
	
	/**
	 * 校验setElementText操作。
	 *
	 * @param ele 可选，元素，默认为：当前选中元素
	 */
	editor.checkSetElementText = function(ele)
	{
		ele = this._currentElement(ele, true);
		
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
		ele = this._currentElement(ele, true);
		
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
		ele = this._currentElement(ele, true);
		
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
		ele = this._currentElement(ele, true);
		
		if(!this.checkDeleteElement(ele))
			return;
		
		var iframeEle = this._editElement(ele);
		
		//应先删除元素包含的所有图表
		var chartEles = this._getChartElements(ele);
		chartEles.each(function()
		{
			editor.dashboard.removeChart(this);
		});
		
		var selEle = (this._isSelectedElement(ele) ? ele : this._selectedElement(ele));
		this.deselectElement(selEle);
		
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
		ele = this._currentElement(ele, true);
		
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
		ele = this._currentElement(ele, true);
		
		if(!this.checkSetElementStyle(ele))
			return;
		
		var so = this._spitStyleAndOption(styleObj);
		
		this._setElementStyle(ele, so.style);
		this._setElementClass(ele, so.option.className);
		
		var chartEles = this._getChartElements(ele);
		chartEles.each(function()
		{
			if(so.option.syncChartTheme)
			{
				var thisEle = $(this);
				var chartTheme = editor._evalElementChartThemeByStyleObj(thisEle, ele, so.style);
				editor.setElementChartTheme(chartTheme, thisEle);
			}
			else
			{
				var renderedChart = editor.dashboard.renderedChart(this);
				editor._resizeChart(renderedChart);
			}
		});
	};
	
	editor._resizeChart = function(chart)
	{
		if(!chart)
			return;
		
		try
		{
			chart.resize();
		}
		catch(e){}
	};
	
	/**
	 * 获取元素样式对象。
	 * 
	 * @param ele 可选，元素，默认为：当前选中元素
	 */
	editor.getElementStyle = function(ele)
	{
		ele = this._editElement(this._currentElement(ele, true));
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
		var body = $(document.body);
		
		this._setElementStyle(body, so.style);
		this._setElementClass(body, so.option.className);
		
		if(so.style.color)
		{
			this._setPageStyle(
			{
				selectedBorderColor: so.style.color
			});
		}
		
		if(so.option.syncChartTheme)
		{
			var chartTheme = this._evalElementChartThemeByStyleObj($(document.body), $(document.body), so.style);
			this.setGlobalChartTheme(chartTheme);
		}
	};
	
	/**
	 * 获取全局样式对象（body）。
	 */
	editor.getGlobalStyle = function()
	{
		var ele = this._editElement($(document.body));
		return this._getElementStyleObj(ele);
	};
	
	editor._spitStyleAndOption = function(styleObj)
	{
		var optionObj =
		{
			syncChartTheme: (styleObj.syncChartTheme == true || styleObj.syncChartTheme == "true"),
			className: styleObj.className
		};
		
		var plainStyleObj = $.extend({}, styleObj);
		plainStyleObj.syncChartTheme = undefined;
		plainStyleObj.className = undefined;
		
		var re =
		{
			style: plainStyleObj,
			option: optionObj
		};
		
		return re;
	};
	
	editor._evalElementChartThemeByStyleObj = function(chartEle, styleEle, styleObj)
	{
		var nowTheme = this._getElementChartTheme(chartEle);
		var styleTheme = {};
		
		var color = styleObj.color;
		var bgColor = styleObj['background-color'];
		var fontSize = styleObj['font-size'];
		
		if(color != null || bgColor != null || fontSize != null)
		{
			if(color != null)
				styleTheme.color = color;
			
			//无需同步图表元素背景色，因为背景色会自动继承
			//styleTheme.backgroundColor = bgColor;
			
			if(bgColor != null)
			{
				if(bgColor == "")
				{
					styleTheme.actualBackgroundColor = "";
				}
				else
				{
					var bgColorObj = chartFactory.parseColor(bgColor);
					
					//未设透明度、或者透明度大于0.5才同步
					if(bgColorObj.a == null || bgColorObj.a > 0.5)
					{
						//应忽略透明度
						bgColorObj.a = undefined;
						styleTheme.actualBackgroundColor = chartFactory.colorToHexStr(bgColorObj, true);
					}
					else
					{
						styleTheme.actualBackgroundColor = "";
					}
				}
			}
			
			if(fontSize != null)
			{
				if(fontSize == "")
				{
					styleTheme.fontSize = "";
				}
				else
				{
					//从元素的css中取才能获取字体尺寸像素数
					styleTheme.fontSize = styleEle.css("font-size");
				}
			}
		}
		
		if(!nowTheme)
		{
			return styleTheme;
		}
		else
		{
			nowTheme.color = (styleTheme.color != null ? styleTheme.color : undefined);
			nowTheme.actualBackgroundColor = (styleTheme.actualBackgroundColor != null ? styleTheme.actualBackgroundColor : undefined);
			nowTheme.fontSize = (styleTheme.fontSize != null ? styleTheme.fontSize : undefined);
			
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
		ele = this._currentElement(ele, true);
		
		if(!this._checkNotEmptyElement(ele))
			return false;
		
		return true;
	};
	
	/**
	 * 设置元素或其所有子图表元素的图表主题。
	 * 
	 * @param chartTheme 要设置的图表主题对象，格式为：{ 'color': '...', 'backgroundColor': '...', ... }
	 * @param ele 可选，元素，默认为：当前选中元素
	 */
	editor.setElementChartTheme = function(chartTheme, ele)
	{
		ele = this._currentElement(ele, true);
		
		if(!this.checkSetElementChartTheme(ele))
			return;
		
		var chartEles = this._getChartElements(ele);
		chartEles.each(function()
		{
			var thisEle = $(this);
			
			editor._setElementChartTheme(thisEle, chartTheme);
			var chart = editor.dashboard.renderedChart(thisEle);
			editor._reRenderChart(chart);
		});
	};
	
	/**
	 * 获取元素图表主题。
	 * 
	 * @param ele 可选，元素，默认为：当前选中元素
	 */
	editor.getElementChartTheme = function(ele)
	{
		ele = this._editElement(this._currentElement(ele, true));
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
		this._reRenderDashboard();
	};
	
	/**
	 * 获取全局图表主题。
	 */
	editor.getGlobalChartTheme = function()
	{
		var ele = this._editElement($(document.body));
		return this._getElementChartTheme(ele);
	};
	
	/**
	 * 获取图表元素的图表属性值。
	 * 
	 * @param ele 可选，元素，默认为：当前选中元素
	 */
	editor.getElementChartAttrValues = function(ele)
	{
		ele = this._currentElement(ele, true);
		
		if(!this._checkNotEmptyElement(ele))
			return null;
		
		var chart = this.dashboard.renderedChart(ele);
		if(!chart)
			return null;
		
		return chart.attrValues();
	};
	
	/**
	 * 获取图表元素的重置图表属性值。
	 * 
	 * @param ele 可选，元素，默认为：当前选中元素
	 */
	editor.getElementChartAttrValuesForReset = function(ele)
	{
		ele = this._currentElement(ele, true);
		
		if(!this._checkNotEmptyElement(ele))
			return null;
		
		var chart = this.dashboard.renderedChart(ele);
		if(!chart)
			return null;
		
		var attrValuesOrigin =  (chart.attrValuesOrigin() || {});
		var attrValuesEle = chart.elementJquery().attr(chartFactory.elementAttrConst.ATTR_VALUES);
		attrValuesEle = chartFactory.evalSilently(attrValuesEle, {});
		var cpas = chart.pluginAttributes();
		
		$.each(cpas, function(i, cpa)
		{
			delete attrValuesEle[cpa.name];
		});
		
		//保留元素上定义的图表插件属性之外的扩展值
		var re = $.extend(true, {}, attrValuesOrigin, attrValuesEle);
		return re;
	};
	
	/**
	 * 获取看板图表插件属性内置地图选项集。
	 */
	editor.getChartPluginAttributeInputOptionsForMap = function(asTree)
	{
		var re = [];
		
		//树
		if(asTree)
		{
			var listener =
			{
				added: function(node, parent, rootArray)
				{
					//转换为UI组件所需的结构
					node.key = node.mapName;
					node.label = node.mapLabel;
					if(parent && !parent.children)
						parent.children = parent.mapChildren;
				}
			};
			
			re = dashboardFactory.getStdBuiltinChartMapTree(listener);
		}
		//数组
		else
		{
			var listener =
			{
				added: function(node, rootArray)
				{
					//转换为UI组件所需的结构
					node.value = node.mapName;
					node.name = node.mapLabel;
				}
			};
			
			re = dashboardFactory.getStdBuiltinChartMapArray(listener);
		}
		
		var mapURLs = [];
		
		var mapURLsBody = $(document.body).attr(chartFactory.elementAttrConst.MAP_URLS);
		mapURLsBody = (mapURLsBody ? chartFactory.evalSilently(mapURLsBody, {}) : {});
		
		$.each(mapURLsBody, function(p, v)
		{
			if(p && chartFactory.isString(v))
			{
				if(asTree)
					mapURLs.push({ key: p, label: p });
				else
					mapURLs.push({ name: p, value: p });
			}
		});
		
		if(mapURLs.length > 0)
			re = mapURLs.concat(re);
		
		return re;
	};
	
	/**
	 * 校验setElementChartAttrValues操作。
	 * 
	 * @param ele 可选，元素，默认为：当前选中元素
	 */
	editor.checkSetElementChartAttrValues = function(ele)
	{
		ele = this._currentElement(ele, true);
		
		if(!this._checkNotEmptyElement(ele))
			return false;
		
		var chart = this.dashboard.renderedChart(ele);
		if(!chart)
		{
			this.tipInfo(i18n.selectedNotChartElement);
			return false;
		}
		
		var cpas = chart.pluginAttributes();
		if(cpas == null || cpas.length == 0)
		{
			this.tipInfo(i18n.chartPluginNoAttrDefined);
			return false;
		}
		
		return true;
	};
	
	/**
	 * 设置图表元素的图表属性值。
	 * 
	 * @param attrValues 要设置的图表主题对象，格式为：{ ... }
	 * @param ele 可选，元素，默认为：当前选中元素
	 */
	editor.setElementChartAttrValues = function(attrValues, ele)
	{
		attrValues = (attrValues || {});
		ele = this._currentElement(ele, true);
		
		if(!this.checkSetElementChartAttrValues(ele))
			return;
		
		var chart = this.dashboard.renderedChart(ele);
		var attrValuesOrigin = (chart.attrValuesOrigin() || {});
		var attrValuesMerge = {};
		
		//应该只设置有修改的图表属性值，这样在图表模块再次编辑其他图表属性值才能应用于所有引用它看板
		for(var p in attrValues)
		{
			if(!this._deepEqualsForJson(attrValuesOrigin[p], attrValues[p]))
				attrValuesMerge[p] = attrValues[p];
		}
		
		var eleAttrValue = this._serializeForAttrValue(attrValuesMerge);
		
		if(eleAttrValue == "{}")
			this._removeElementAttr(ele, chartFactory.elementAttrConst.ATTR_VALUES, true);
		else
			this._setElementAttr(ele, chartFactory.elementAttrConst.ATTR_VALUES, eleAttrValue, true);
		
		this._reRenderChart(chart);
	};
	
	/**
	 * 获取图表元素的ChartPluginAttribute数组。
	 * 
	 * @param ele 可选，元素，默认为：当前选中元素
	 */
	editor.getElementChartPluginAttrs = function(ele)
	{
		ele = this._currentElement(ele, true);
		
		if(!this._checkNotEmptyElement(ele))
			return null;
		
		var chart = this.dashboard.renderedChart(ele);
		if(!chart)
			return null;
		
		return chart.pluginAttributes();
	};
	
	/**
	 * 校验setElementChartOptions操作。
	 * 
	 * @param ele 可选，元素，默认为：当前选中元素
	 */
	editor.checkSetElementChartOptions = function(ele)
	{
		ele = this._currentElement(ele, true);
		
		if(!this._checkNotEmptyElement(ele))
			return false;
		
		return true;
	};
	
	/**
	 * 设置元素或其所有子图表元素的图表选项。
	 * 
	 * @param chartOptionsStr 要设置的图表选项字符串
	 * @param ele 可选，元素，默认为：当前选中元素
	 */
	editor.setElementChartOptions = function(chartOptionsStr, ele)
	{
		ele = this._currentElement(ele, true);
		
		if(!this.checkSetElementChartOptions(ele))
			return;
		
		var chartEles = this._getChartElements(ele);
		chartEles.each(function()
		{
			var thisEle = $(this);
			
			editor._setElementChartOptions(thisEle, chartOptionsStr);
			var chart = editor.dashboard.renderedChart(thisEle);
			editor._reRenderChart(chart);
		});
	};
	
	/**
	 * 获取元素图表选项的字符串格式。
	 * 
	 * @param ele 可选，元素，默认为：当前选中元素
	 * @oaram asString 可选，是否以字符串形式返回，默认为：true
	 */
	editor.getElementChartOptions = function(ele, asString)
	{
		ele = this._editElement(this._currentElement(ele, true));
		asString = (asString == null ? true : asString);
		
		return this._getElementChartOptions(ele);
	};
	
	/**
	 * 获取元素原始图表选项的字符串格式。
	 * 
	 * @param ele 可选，元素，默认为：当前选中元素
	 */
	editor.getElementChartOptionsOrigin = function(ele)
	{
		ele = this._currentElement(ele, true);
		
		if(!this._checkNotEmptyElement(ele))
			return "";
		
		var chart = this.dashboard.renderedChart(ele);
		if(!chart)
			return "";
		
		return (chart.optionsOrigin() || "");
	};
	
	/**
	 * 设置全局图表选项。
	 * 
	 * @param chartOptionsStr 要设置的全局图表选项字符串
	 */
	editor.setGlobalChartOptions = function(chartOptionsStr)
	{
		this._setElementChartOptions($(document.body), chartOptionsStr);
		this._reRenderDashboard();
	};
	
	/**
	 * 获取全局图表选项的字符串格式。
	 * 
	 * @oaram asString 可选，是否以字符串形式返回，默认为：true
	 */
	editor.getGlobalChartOptions = function(asString)
	{
		asString = (asString == null ? true : asString);
		
		var ele = this._editElement($(document.body));
		return this._getElementChartOptions(ele);
	};
	
	editor._fillBodyStyleByAbsolute = function()
	{
		var re = this._fillParentStyleByAbsolute();
		//设置默认内边距，贴边效果不佳
		re.padding = "6px";
		
		return re;
	};
	
	editor._fillParentStyleByAbsolute = function()
	{
		var re =
		{
			position: "absolute", left: "0", top: "0", right: "0", bottom: "0",
			padding: "0", margin: "0", "box-sizing": "border-box"
		};
		
		return re;
	};
	
	editor._reRenderDashboard = function(chart)
	{
		this.dashboard.destroy();
		this.dashboard.init();
		this.dashboard.render();
	};
	
	editor._reRenderChart = function(chart)
	{
		if(chart)
		{
			chart.destroy();
			chart.init();
			chart.render();
		}
	};
	
	editor._setElementChartOptions = function(ele, chartOptionsStr, sync)
	{
		if(!chartOptionsStr)
		{
			this._removeElementAttr(ele, chartFactory.elementAttrConst.OPTIONS, sync);
			return;
		}
		
		var attrValue = (chartOptionsStr ? chartOptionsStr : "{}");
		this._setElementAttr(ele, chartFactory.elementAttrConst.OPTIONS, attrValue, sync);
	};
	
	editor._getElementChartOptions = function(ele)
	{
		var optionsStr = ele.attr(chartFactory.elementAttrConst.OPTIONS);
		return optionsStr;
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
		
		var mergedChartTheme = (this.getElementChartTheme(ele) || {});
		
		for(var p in chartTheme)
		{
			var v = chartTheme[p];
			
			if(chartFactory.isNullOrEmpty(v))
				delete mergedChartTheme[p];
			else
				mergedChartTheme[p] = v;
		}
		
		//确保fontSize为数值
		if(mergedChartTheme.fontSize != null && !chartFactory.isNumber(mergedChartTheme.fontSize))
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
			
			if(v == null)
				;
			else if(chartFactory.isString(v))
			{
				if(v != "")
					trim[p] = v;
			}
			else if($.isArray(v))
			{
				if(v.length > 0)
					trim[p] = v;
			}
			else
				trim[p] = v;
		}
		
		var attrValue = this._serializeForAttrValue(trim);
		
		if(attrValue == "{}")
			this._removeElementAttr(ele, chartFactory.elementAttrConst.THEME, sync);
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
	
	editor._setElementClass = function(ele, className, sync)
	{
		className = (className || "");
		sync = (sync == null ? true : sync);
		
		var editEle = this._editElement(ele);
		var removeClassName = editEle.attr("class");
		
		if(removeClassName)
			ele.removeClass(removeClassName);
		if(sync)
		{
			if(!className)
				editEle.removeAttr("class");
			else
				editEle.removeClass(removeClassName);
		}
		
		if(className)
		{
			ele.addClass(className);
			if(sync)
				editEle.addClass(className);
		}
	};
	
	editor._setElementStyleNoSync = function(ele, styleObj)
	{
		//这里不能采用整体设置"style"属性的方式，因为"style"属性可能有很多不支持编辑的、或者动态生成的css属性，
		//它们应该被保留，且不能同步至对应的编辑元素上
		
		var nowStyleObj = chartFactory.styleStringToObj(chartFactory.elementStyle(ele) || "");
		
		for(var name in styleObj)
		{
			var value = styleObj[name];
			
			if(chartFactory.isNullOrEmpty(value))
				delete nowStyleObj[name];
			else
			{
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
		var styleObj = chartFactory.styleStringToObj(chartFactory.elementStyle(ele));
		styleObj.className = (ele.attr("class") || "");
		
		return styleObj;
	};
	
	editor._editableElementStyles =
	{
		"color": true,
		"background-color": true,
		"background-image": true,
		"background-position": true,
		"background-size": true,
		"background-repeat": true,
		"border-width": true,
		"border-color": true,
		"border-style": true,
		"border-radius": true,
		"box-shadow": true,
		"display": true,
		"width": true,
		"height": true,
		"padding": true,
		"margin": true,
		"box-sizing": true,
		"position": true,
		"left": true,
		"top": true,
		"right": true,
		"bottom": true,
		"z-index": true,
		"flex-direction": true,
		"flex-wrap": true,
		"justify-content": true,
		"align-items": true,
		"order": true,
		"flex-grow": true,
		"flex-shrink": true,
		"flex-basis": true,
		"align-self": true,
		"align-content": true,
		"grid-template-columns": true,
		"grid-template-rows": true,
		"column-gap": true,
		"row-gap": true,
		"grid-template-areas": true,
		"grid-auto-flow": true,
		"justify-items": true,
		"grid-auto-columns": true,
		"grid-auto-rows": true,
		"grid-column-start": true,
		"grid-column-end": true,
		"grid-row-start": true,
		"grid-row-end": true,
		"grid-area": true,
		"justify-self": true,
		"font-family": true,
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
	
	editor._currentElement = function(currentEle, excludeBody)
	{
		excludeBody = (excludeBody == null ? false : excludeBody);
		
		currentEle = (this._isEmptyElement(currentEle) ? this._selectedElement() : currentEle);
		
		if(!excludeBody)
			currentEle = (this._isEmptyElement(currentEle) ? $(document.body) : currentEle);
		
		return $(currentEle);
	};
	
	editor._addVisualEditIdAttr = function($ele)
	{
		$ele.attr(ELEMENT_ATTR_VISUAL_EDIT_ID, this._nextVisualEditId());
		
		var children = $ele.children();
		
		if(children.length < 1)
			return;
			
		children.each(function()
		{
			editor._addVisualEditIdAttr($(this));
		});
	};
	
	editor._insertElement = function(refEle, insertEle, insertType)
	{
		if(insertType == "after")
		{
			refEle.after(insertEle);
			
			refEle.after("\n"+INSERT_ELE_FORMAT_FLAG+"\n");
		}
		else if(insertType == "before")
		{
			refEle.before(insertEle);
			
			refEle.before("\n"+INSERT_ELE_FORMAT_FLAG+"\n");
		}
		else if(insertType == "append")
		{
			var innerHtml = refEle.prop("innerHTML");
			if(!innerHtml || innerHtml.charAt(innerHtml.length-1) != '\n')
				refEle.append("\n"+INSERT_ELE_FORMAT_FLAG+"\n");
			
			refEle.append(insertEle);
			
			refEle.append("\n"+INSERT_ELE_FORMAT_FLAG+"\n");
		}
		else if(insertType == "prepend")
		{
			var innerHtml = refEle.prop("innerHTML");
			if(!innerHtml || innerHtml.charAt(0) != '\n')
				refEle.prepend("\n"+INSERT_ELE_FORMAT_FLAG+"\n");
			
			refEle.prepend(insertEle);
			
			refEle.prepend("\n"+INSERT_ELE_FORMAT_FLAG+"\n");
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
	
	//获取元素本身、子孙元素中所有的图表元素
	//注意：返回的图表元素中可能有还未渲染为图表的元素
	editor._getChartElements = function(ele)
	{
		var chartEles = [];
		
		if(ele.attr(chartFactory.elementAttrConst.WIDGET))
			chartEles.push(ele[0]);
		
		$("["+chartFactory.elementAttrConst.WIDGET+"]", ele).each(function()
		{
			chartEles.push(this);
		});
		
		return $(chartEles);
	};
	
	editor._selectedElement = function(context)
	{
		if(context == null)
			return $("."+ELEMENT_CLASS_SELECTED);
		else
			return $("."+ELEMENT_CLASS_SELECTED, context);
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
			+ "\n}");
	};
	
	//获取编辑HTML信息
	//结构参考：org.datagear.web.controller.DashboardVisualController.DashboardShowForEdit.EditHtmlInfo
	editor._editHtmlInfo = function()
	{
		return this.dashboard.renderContextAttr(DASHBOARD_BUILTIN_RENDER_CONTEXT_ATTR_EDIT_HTML_INFO);
	};
	
	//反转义编辑HTML（转义操作由后台执行）
	editor._unescapeEditHtml = function(editHtml)
	{
		return (editHtml ? editHtml.replace(/<\\\//g, "</") : editHtml);
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
	
	//将符合JSON规范的对象序列化为元素属性值字符串
	//注意：此函数使用单引号而非双引号作为引号符，因为双引号会被HTML转义为'&quot;'，对源码不友好
	editor._serializeForAttrValue = function(obj)
	{
		if(obj == null)
			return null;
		
		var type = typeof(obj);
		
		if(type == "string")
		{
			return this._toSingleQuoteJsString(obj, true);
		}
		else if(type == "number" || type == "boolean")
		{
			return obj;
		}
		else if($.isArray(obj))
		{
			var str = "[";
			
			for(var i=0; i<obj.length; i++)
			{
				var vstr = this._serializeForAttrValue(obj[i]);
				if(vstr != null && vstr != "")
				{
					if(str != "[")
						str += ",";
					
					str += vstr;
				}
			}
			
			str += "]";
			
			return str;
		}
		else if($.isPlainObject(obj))
		{
			var str = "{";
			
			for(var p in obj)
			{
				var vstr = this._serializeForAttrValue(obj[p]);
				if(vstr != null && vstr != "")
				{
					if(str != "{")
						str += ",";
					
					str += this._serializeForAttrValue(p) + ":" + vstr;
				}
			}
			
			str += "}";
			
			return str;
		}
		else
			return this._serializeForAttrValue(obj.toString());
	};
	
	editor._toSingleQuoteJsString = function(str, quote)
	{
		quote = (quote == null ? false : quote);
		
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
	
	editor._deepEqualsForJson = function(a, b)
	{
		if(a == null)
		{
			return (b == null);
		}
		else if(b == null)
		{
			return (a == null);
		}
		else if($.isArray(a))
		{
			if(!$.isArray(b))
				return false;
			
			if(a.length != b.length)
				return false;
			
			for(var i=0; i<a.length; i++)
			{
				if(!editor._deepEqualsForJson(a[i], b[i]))
					return false;
			}
			
			return true;
		}
		else if($.isPlainObject(a))
		{
			if(!$.isPlainObject(b))
				return false;
			
			for(var p in a)
			{
				if(!editor._deepEqualsForJson(a[p], b[p]))
					return false;
			}
			
			return true;
		}
		else
			return (a == b);
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
			var editIframeBodyHtml = this._toEditIframeBodyHtml(editBodyHtml);
			
			var editDoc = this._editDocument();
			editDoc.open();
			editDoc.write("<!DOCTYPE html><html><head></head><body>");
			editDoc.write(editIframeBodyHtml);
			editDoc.write("</body></html>");
			editDoc.close();
			
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
	
	editor._isJsonString = function(str)
	{
		return chartFactory.isJsonString(str);
	};
	
	editor._buildLoadChartAjaxOptions = function()
	{
		var webContext = chartFactory.renderContextAttrWebContext(this.dashboard.renderContext);
		var url = this.dashboard.contextURL(webContext.attributes.loadChartURL);
		var qidx = url.indexOf('?');
		url = url + (qidx < 0 ? "?" : "&") + LOAD_CHART_FOR_EDITOR_PARAM + "=true";
		
		var re =
		{
			url: url
		};
		
		return re;
	};
	
	/**
	 * 获取元素节点路径信息。
	 */
	editor.getElementPath = function(ele)
	{
		ele = $(ele);
		
		var paths = [];
		
		while(true)
		{
			if(ele.length == 0)
				break;
			
			var isBody =  ele.is("body");
			
			if(!this._isSelectableElement(ele) && !isBody)
			{
				ele = ele.parent();
				continue;
			}
			
			var editEle = this._editElement(ele);
			var pathInfo =
			{
				tagName: (ele[0].tagName || "").toLowerCase(),
				selected: this._isSelectedElement(ele),
				id: editEle.attr("id"),
				className: editEle.attr("class"),
				cssDisplay: ele.css("display"),
				visualEditId: editEle.attr(ELEMENT_ATTR_VISUAL_EDIT_ID)
			};
			
			var displayName = pathInfo.tagName;
			
			if(this._isDisplayGrid(pathInfo.cssDisplay))
				displayName += "(grid)";
			else if(this._isDisplayFlex(pathInfo.cssDisplay))
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
				ele = ele.parent();
		}
		
		return paths.reverse();
	};
	
	editor._isDisplayGrid = function(display)
	{
		if(!display)
			return false;
		
		return /^(grid|inline-grid)$/i.test(display);
	};
	
	editor._isDisplayFlex = function(display)
	{
		if(!display)
			return false;
		
		return /^(flex|inline-flex)$/i.test(display);
	};
	
})
(this);
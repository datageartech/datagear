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
	
	//参考org.datagear.web.controller.DashboardController.DASHBOARD_BUILTIN_RENDER_CONTEXT_ATTR_EDIT_HTML_INFO
	var DASHBOARD_BUILTIN_RENDER_CONTEXT_ATTR_EDIT_HTML_INFO = (editor.DASHBOARD_BUILTIN_RENDER_CONTEXT_ATTR_EDIT_HTML_INFO = "DG_EDIT_HTML_INFO");
	
	//参考org.datagear.web.controller.DashboardController.DashboardShowForEdit.ELEMENT_ATTR_VISUAL_EDIT_ID
	var ELEMENT_ATTR_VISUAL_EDIT_ID = (editor.ELEMENT_ATTR_VISUAL_EDIT_ID = "dg-visual-edit-id");
	
	var DG_VISUAL_EDIT_ID_PREFIX = (editor.DG_VISUAL_EDIT_ID_PREFIX = "c" + new Number(new Date().getTime()).toString(16));
	
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
	
	//初始化样式
	editor._initStyle = function()
	{
		chartFactory.styleSheetText("dg-show-ve-style",
			"\n"
			+ ".dg-show-ve .dg-show-ve-selected{\n"
			+ "  border-color: " + $(document.body).css("color") + " !important;"
			+ "\n}\n");
	};
	
	//初始化编辑HTML的iframe
	editor._initEditHtmlIframe = function()
	{
		var editHtmlInfo = this._editHtmlInfo();
		
		//只使用bodyHtml，因为渲染ifarme页面时beforeBodyHtml、afterBodyHtml如果有不合规的元素，
		//可能会被渲染至<body></body>内，导致【结果HTML】还原不对
		var editHtml = "<html><head></head>" + editHtmlInfo.bodyHtml + "</html>";
		this._editIframe(editHtml);
	};
	
	//初始化交互控制
	editor._initInteraction = function()
	{
		$(document.body).addClass("dg-show-ve");
		
		$(document.body).on("click", "["+ELEMENT_ATTR_VISUAL_EDIT_ID+"]", function(event)
		{
			var $this = $(this);
			
			if($this.hasClass("dg-show-ve-selected"))
				$this.removeClass("dg-show-ve-selected");
			else
			{
				$(".dg-show-ve-selected").removeClass("dg-show-ve-selected");
				$(this).addClass("dg-show-ve-selected");
			}
			
			event.stopPropagation();
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
	 * 判断元素是否允许插入图表。
	 * 
	 * @param insertType 可选，参考insertElement函数的insertType参数
	 * @param refEle 可选，参考insertElement函数的refEle参数
	 */
	editor.canInsertChart = function(insertType, refEle)
	{
		refEle = this._refElement(refEle);
		insertType = this._trimInsertType(refEle, insertType);
		
		//图表元素内部不允许再插入图表元素
		if(this.dashboard.renderedChart(refEle) && (insertType == "append" || insertType == "prepend"))
			return false;
		else
			return true;
	};
	
	/**
	 * 是否有选中元素。
	 */
	editor.hasSelectedElement = function()
	{
		var ele = this._refElement(null, true);
		return !this._isEmptyElement(ele);
	};
	
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
		if(!this.canInsertChart(insertType, refEle))
		{
			this.tipInfo(i18n.insertInsideChartOnChartEleDenied);
			return;
		}
		
		for(var i=0; i<chartWidgets.length; i++)
		{
			var chartWidget = chartWidgets[i];
			
			var chartDiv = $("<div class='dg-chart' />").attr(chartFactory.elementAttrConst.WIDGET, chartWidget.id)
				.attr(ELEMENT_ATTR_VISUAL_EDIT_ID, this._nextVisualEditId()).html("<!--"+chartWidget.name+"-->");
			
			this.insertElement(chartDiv, insertType, refEle);
		}
		
		this.dashboard.loadUnsolvedCharts();
	};
	
	/**
	 * 绑定或替换图表。
	 * 
	 * @param chartWidget 要绑定的新图表部件对象
	 * @param chartEle 可选，要绑定的图表元素，默认为：当前选中图表元素
	 */
	editor.bindChart = function(chartWidget, chartEle)
	{
		if(!chartWidget)
			return;
		
		chartEle = this._refElement(chartEle);
		
		if(!chartEle || chartEle.is("body"))
		{
			this.tipInfo(i18n.selectElementForSetChart);
			return;
		}
		
		if(this.dashboard.renderedChart(chartEle))
		{
			this.dashboard.removeChart(chartEle);
		}
		
		this._setElementAttr(chartEle, chartFactory.elementAttrConst.WIDGET, chartWidget.id);
		this.dashboard.loadChart(chartEle);
	};
	
	/**
	 * 解绑图表。
	 * 
	 * @param chartEle 可选，要解绑的图表元素，默认为：当前选中图表元素
	 */
	editor.unbindChart = function(chartEle)
	{
		chartEle = this._refElement(chartEle, true);
		
		if(this._isEmptyElement(chartEle))
		{
			this.tipInfo(i18n.selectedElementRequired);
			return false;
		}
		
		if(!this.dashboard.renderedChart(chartEle))
		{
			this.tipInfo(i18n.selectedNotChartElement);
			return false;
		}
		
		this.dashboard.removeChart(chartEle);
		this._removeElementAttr(chartEle, chartFactory.elementAttrConst.WIDGET);
	};
	
	/**
	 * 插入元素。
	 * 
	 * @param insertEle 要插入的jq元素、文本
	 * @param insertType 可选，插入类型："after"、"before"、"append"、"prepend"，默认为："after"
	 * @param refEle 插入参照元素，默认为：当前选中元素，或者<body>
	 * @param sync 可选，是否将插入操作同步至编辑iframe中，默认为：true
	 */
	editor.insertElement = function(insertEle, insertType, refEle, sync)
	{
		refEle = this._refElement(refEle);
		insertType = this._trimInsertType(refEle, insertType);
		sync = (sync == null ? true : sync);
		
		this._insertElement(refEle, insertEle, insertType);
		
		if(sync)
		{
			var editEle = this._editElement(refEle);
			var insertEleClone = (chartFactory.isString(insertEle) ? insertEle : insertEle.clone());
			this._insertElement(editEle, insertEleClone, insertType);
		}
		
		this.changeFlag(true);
	};
	
	/**
	 * 是否是可编辑文本的元素，即元素内没有子元素、且不是<body>元素。
	 *
	 * @param ele 可选，元素，默认为：当前选中元素
	 */
	editor.isEditTextElement = function(ele)
	{
		refEle = this._refElement(ele);
		
		if(refEle.is("body"))
			return false;
			
		var firstChild = $("> *:first-child", refEle);
		
		if(firstChild.length == 0)
			return true;
		
		return false;
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
	 * 设置元素文本内容。
	 * 
	 * @param text 要设置的文本内容
	 * @param ele 可选，元素，默认为：当前选中元素
	 */
	editor.setElementText = function(text, ele)
	{
		ele = this._refElement(ele, true);
		
		if(this._isEmptyElement(ele))
		{
			this.tipInfo(i18n.selectedElementRequired);
			return false;
		}
		
		if(!this.isEditTextElement(ele))
		{
			this.tipInfo(i18n.canEditOnlyTextElement);
			return false;
		}
		
		this._setElementText(ele, text);
	};
	
	/**
	 * 删除元素。
	 *
	 * @param ele 可选，元素，默认为：当前选中元素
	 */
	editor.deleteElement = function(ele)
	{
		ele = this._refElement(ele, true);
		
		if(this._isEmptyElement(ele))
		{
			this.tipInfo(i18n.selectedElementRequired);
			return false;
		}
		
		var iframeEle = this._editElement(ele);
		
		ele.remove();
		iframeEle.remove();
		
		this.changeFlag(true);
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
		
		if(this._isEmptyElement(ele))
		{
			this.tipInfo(i18n.selectedElementRequired);
			return false;
		}
		
		this._setElementStyle(ele, styleObj);
	};
	
	/**
	 * 设置全局样式（body）。
	 * 
	 * @param styleObj 要设置的样式对象，格式为：{ 'color': '...', 'background-color': '...' }
	 */
	editor.setGlobalStyle = function(styleObj)
	{
		this._setElementStyle($(document.body), styleObj);
	};
	
	editor._setElementStyle = function(ele, styleObj, sync)
	{
		styleObj = (styleObj || {});
		sync = (sync == null ? true : sync);
		
		chartFactory.elementStyle(ele, styleObj);
		
		if(sync)
		{
			var editEle = this._editElement(ele);
			chartFactory.elementStyle(editEle, styleObj);
		}
		
		this.changeFlag(true);
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
		
		refEle = (this._isEmptyElement(refEle) ? this._getSelected() : refEle);
		
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
	
	editor._getSelected = function()
	{
		return $(".dg-show-ve-selected");
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
		var seq = (this._nextVisualEditIdSequence != null ? this._nextVisualEditIdSequence : 0);
		this._nextVisualEditIdSequence = seq + 1;
		
		return DG_VISUAL_EDIT_ID_PREFIX + seq;
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
	
	/**
	 * 获取编辑iframe，也可设置其HTML。
	 */
	editor._editIframe = function(editHtml)
	{
		var id = (this._editIframeId != null ? this._editIframeId
					: (this._editIframeId = chartFactory.nextElementId()));
		
		var iframe = $("#" + id);
		
		if(iframe.length == 0)
		{
			iframe = $("<iframe class='dg-edit-html-ifm' style='display:none;' />")
				.attr("name", id).attr("id", id).appendTo(document.body);
		}
		
		iframe = iframe[0];
		
		if(editHtml != null)
		{
			editHtml = this._unescapeEditHtml(editHtml);
			this._editDocument(iframe).write(editHtml);
			
			this.changeFlag(true);
		}
		
		return iframe;
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
		return $(editDoc.body).prop("outerHTML");
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
			return $(editDoc.body);
		
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
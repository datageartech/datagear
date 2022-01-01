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
		
		this._initEditHtmlIframe();
		this._initInteraction();
	};
	
	//初始化编辑HTML的iframe
	editor._initEditHtmlIframe = function()
	{
		var editHtmlInfo = this._editHtmlInfo();
		
		//只使用bodyHtml，因为渲染ifarme页面时beforeBodyHtml、afterBodyHtml如果有不合规的元素，
		//可能会被渲染至<body></body>内，导致【结果HTML】还原不对
		var editHtml = "<html><head></head>" + editHtmlInfo.bodyHtml + "</html>";
		this._editHtmlIframe(editHtml);
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
		var bodyHtml = this._editBodyHtml();
		
		//将占位标签还原为原始标签
		var placeholderSources = (editHtmlInfo.placeholderSources || {});
		for(var placeholder in placeholderSources)
		{
			var source = placeholderSources[placeholder];
			bodyHtml = bodyHtml.replace(placeholder, source);
		}
		
		//删除dg-visual-edit-id属性
		var veidRegex = /\s?dg\-visual\-edit\-id\=["'][^"']*["']\s*>/gi;
		bodyHtml = bodyHtml.replace(veidRegex, ">");
		
		var editedHtml = editHtmlInfo.beforeBodyHtml + bodyHtml + editHtmlInfo.afterBodyHtml;
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
	
	editor.insertChart = function(chartWidgets)
	{
		if(!chartWidgets || chartWidgets.length == 0)
			return;
		
		if(!$.isArray(chartWidgets))
			chartWidgets = [ chartWidgets ];
		
		var iframeDoc = this._iframeDocument();
		
		for(var i=0; i<chartWidgets.length; i++)
		{
			var chartWidget = chartWidgets[i];
			
			var dgStaticId = this._nextVisualEditId();
			
			var vdiv = $("<div class='dg-chart' />").attr(chartFactory.elementAttrConst.WIDGET, chartWidget.id)
				.attr(ELEMENT_ATTR_VISUAL_EDIT_ID, dgStaticId).appendTo(document.body);
			vdiv.after("\n");
			
			var sdiv = $("<div class='dg-chart' />").attr(chartFactory.elementAttrConst.WIDGET, chartWidget.id)
				.attr(ELEMENT_ATTR_VISUAL_EDIT_ID, dgStaticId).appendTo(iframeDoc.body);
			sdiv.after("\n");
		}
		
		this.changeFlag(true);
		
		this.dashboard.loadUnsolvedCharts();
	};
	
	editor.deleteSelected = function()
	{
		var selected = this._getSelected();
		var iframeEle = this._getIframeEle(selected);
		
		selected.remove();
		iframeEle.remove();
		
		this.changeFlag(true);
	};
	
	editor._getSelected = function()
	{
		return $(".dg-show-ve-selected");
	};
	
	editor._getIframeEle = function($ele)
	{
		var eid = (this._getVisualEditId($ele) || "");
		var iframeDoc = this._iframeDocument();
		return $("["+ELEMENT_ATTR_VISUAL_EDIT_ID+"='"+eid+"']", iframeDoc.body);
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
	
	//获取编辑HTML的<body>...</body>内容
	editor._editBodyHtml = function()
	{
		var iframeDoc = this._iframeDocument();
		return $(iframeDoc.body).prop("outerHTML");
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
	 * 获取编辑HTML的iframe对象，也可设置其编辑HTML。
	 */
	editor._editHtmlIframe = function(editHtml)
	{
		var id = (this._editHtmlIframeId != null ? this._editHtmlIframeId
					: (this._editHtmlIframeId = chartFactory.nextElementId()));
		
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
			
			var iframeDoc = this._iframeDocument(iframe);
			iframeDoc.write(editHtml);
			
			this.changeFlag(true);
		}
		
		return iframe;
	};
	
	/**
	 * 获取iframe的document对象。
	 */
	editor._iframeDocument = function(iframe)
	{
		iframe = (iframe == null ? this._editHtmlIframe() : iframe);
		return (iframe.contentDocument || iframe.contentWindow.document);
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
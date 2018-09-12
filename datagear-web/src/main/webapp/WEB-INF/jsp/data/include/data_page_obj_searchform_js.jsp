<%--
/*
 * Copyright (c) 2018 by datagear.org.
 */
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%--
查询表单JS片段。

依赖：
data_page_obj.jsp
data_page_obj_searchform_html.jsp

变量：
//查询回调函数，不允许为null，格式为：function(searchParam){}
pageObj.search = undefined;
//查询条件autocomplete初始数据，不允许为null
pageObj.conditionAutocompleteSource = undefined;
--%>
<script type="text/javascript">
(function(pageObj)
{
	pageObj.searchForm = pageObj.element("#${pageId}-searchForm");
	pageObj.likeSwitchIcon = pageObj.element(".like-switch-icon", pageObj.searchForm);
	pageObj.notLikeInput = pageObj.element("input[name='notLike']", pageObj.searchForm);
	pageObj.keywordInput = pageObj.element("input[name='keyword']", pageObj.searchForm);
	pageObj.conditionPanel = pageObj.element(".condition-panel", pageObj.searchForm);
	pageObj.conditionTextarea = pageObj.element("textarea[name='condition']", pageObj.searchForm);
	pageObj.conditionIconParent = pageObj.element(".search-condition-icon-parent", pageObj.searchForm);
	pageObj.conditionIcon = pageObj.element(".search-condition-icon", pageObj.searchForm);
	pageObj.conditionIconTip = pageObj.element(".search-condition-icon-tip", pageObj.searchForm);
	
	pageObj._closeCondtionPanelWhenSubmit = true;
	
	pageObj.searchForm.submit(function()
	{
		var searchParam = pageObj.getSearchParam();
		
		pageObj.search(searchParam);
		
		if(pageObj._closeCondtionPanelWhenSubmit)
		{
			pageObj.closeCondtionPanel();
			pageObj.keywordInput.focus();
		}
		
		return false;
	});
	
	pageObj.getSearchParam = function()
	{
		var param =
		{
			"keyword" : $.trim(pageObj.keywordInput.val()),
			"notLike" : $.trim(pageObj.notLikeInput.val()),
			"condition" : $.trim(pageObj.conditionTextarea.val())
		};
		
		return param;
	};
	
	pageObj.clearSearchCondition = function()
	{
		pageObj.conditionTextarea.val("");
	};

	//提取用于autocomplete的关键词
	pageObj.extractLastConditionTerm = function(text)
	{
		var term = "";
		
		for(var i=text.length - 1; i>=0; i--)
		{
			var c = text.charAt(i);
			
			if(c == " " || c== "=" || c == "(" || c == '.')
				break;
			
			term = c + term;
		}
		
		return term;
	};
	
	pageObj.initConditionPanel = function()
	{
		pageObj.conditionTextarea.autocomplete(
		{
			appendTo : pageObj.element(".condition-parent", pageObj.searchForm),
			minLength: 0,
			autoFocus: false,
			source: function(request, response)
			{
				response($.ui.autocomplete.filter(pageObj.conditionAutocompleteSource, pageObj.extractLastConditionTerm(request.term)));
		    },
	        focus: function()
	        {
	        	return false;
			},
			select: function(event, ui)
			{
				var lastTerm = pageObj.extractLastConditionTerm(this.value);
				
	            this.value = (lastTerm.length > 0 ? this.value.substring(0, this.value.length - lastTerm.length) : this.value) + ui.item.value;
	            return false;
	          },
			classes: { "ui-autocomplete": "ui-widget-shadow" },
			position:
			{
				//定位至光标位置
				using : function(pos, eleInfo)
				{
					var pos = pageObj.conditionTextarea.textareaHelper('caretPos');
					pos.top = pageObj.conditionTextarea.textareaHelper('height');
					
					$(this).css("left", pos.left + 6).css("top", pos.top);
				}
			},
			close: function(event, ui)
			{
				//阻止关闭条件面板
				event.stopPropagation();
			}
		});
	};
	
	pageObj.updateNotLikeKeyword = function(notLike)
	{
		if(notLike == undefined)
			notLike = pageObj.notLikeInput.val();
		
		if(notLike)
		{
			pageObj.likeSwitchIcon.removeClass("ui-icon-radio-off").addClass("ui-icon-radio-on").attr("title", "<fmt:message key='data.notLikeTitle' />");
			pageObj.notLikeInput.val("1");
		}
		else
		{
			pageObj.likeSwitchIcon.removeClass("ui-icon-radio-on").addClass("ui-icon-radio-off").attr("title", "<fmt:message key='data.likeTitle' />");
			pageObj.notLikeInput.val("");
		}
	};
	
	pageObj.switchLikeNotLikeKeyword = function()
	{
		if(pageObj.notLikeInput.val())
			pageObj.updateNotLikeKeyword(false);
		else
			pageObj.updateNotLikeKeyword(true);
	};
	
	pageObj.closeCondtionPanel = function()
	{
		pageObj.conditionPanel.hide();

		pageObj.conditionIcon.removeClass("ui-icon-caret-1-n").addClass("ui-icon-caret-1-s");
		
		if($.trim(pageObj.conditionTextarea.val()) != "")
			pageObj.conditionIconTip.show();
	};
	
	pageObj.openCondtionPanel = function()
	{
		pageObj.conditionPanel.show();
		pageObj.conditionIcon.removeClass("ui-icon-caret-1-s").addClass("ui-icon-caret-1-n");
		pageObj.conditionIconTip.hide();
		pageObj.conditionTextarea.focus();
	};
	
	pageObj.likeSwitchIcon.click(function()
	{
		pageObj.switchLikeNotLikeKeyword();

		pageObj.keywordInput.focus();
	});
	
	pageObj.conditionIconParent.click(function()
	{
		if(pageObj.conditionIcon.hasClass("ui-icon-caret-1-s"))
		{
			pageObj.openCondtionPanel();
		}
		else
		{
			pageObj.closeCondtionPanel();
		}
	});
	
	pageObj.element(".condition-panel-resetpos-icon", pageObj.searchForm).click(function()
	{
		pageObj.conditionPanel.css("left", 0).css("top", 0);
	});
	
	pageObj.element(".condition-panel-clear-icon", pageObj.searchForm).click(function()
	{
		pageObj.clearSearchCondition();
		pageObj.conditionTextarea.focus();
	});
	
	pageObj.element(".condition-panel-submit-icon", pageObj.searchForm).click(function()
	{
		pageObj._closeCondtionPanelWhenSubmit = false;
		pageObj.searchForm.submit();
		pageObj.conditionTextarea.focus();
		pageObj._closeCondtionPanelWhenSubmit = true;
	});
	
	pageObj.searchForm.keydown(function(event)
	{
		//打开、关闭条件面板
		if(event.keyCode == $.ui.keyCode.DOWN && event.ctrlKey)
		{
			if(pageObj.conditionPanel.is(":hidden"))
				pageObj.openCondtionPanel();
			else
			{
				pageObj.closeCondtionPanel();
				pageObj.keywordInput.focus();
			}
			
			event.stopPropagation();
		}
		//切换“LIKE”与“NOT LIKE”
		else if(event.keyCode == 49 && event.ctrlKey && event.shiftKey)
		{
			pageObj.switchLikeNotLikeKeyword();
			event.stopPropagation();
		}
	});
	
	pageObj.conditionPanel.keydown(function(event)
	{
		if(event.keyCode == $.ui.keyCode.ENTER && event.ctrlKey)
		{
			pageObj._closeCondtionPanelWhenSubmit = false;
			pageObj.searchForm.submit();
			pageObj._closeCondtionPanelWhenSubmit = true;
			
			event.stopPropagation();
		}
		else if(event.keyCode == $.ui.keyCode.ESCAPE)
		{
			pageObj.closeCondtionPanel();
			pageObj.keywordInput.focus();
			
			event.stopPropagation();
		}
		else if(event.keyCode == $.ui.keyCode.BACKSPACE && event.ctrlKey && event.shiftKey)
		{
			pageObj.clearSearchCondition();
			pageObj.conditionTextarea.focus();
			
			event.stopPropagation();
		}
	});
	
	$(document.body).bind("click", function(event)
	{
		if($(event.target).closest(pageObj.searchForm).length == 0)
			pageObj.closeCondtionPanel();
	});
	
	pageObj.conditionPanel.draggable({ handle: ".condition-panel-title-bar" });
	
	pageObj.element("input:submit", pageObj.searchForm).button();
	pageObj.updateNotLikeKeyword();
	pageObj.closeCondtionPanel();
})
(${pageId});
</script>

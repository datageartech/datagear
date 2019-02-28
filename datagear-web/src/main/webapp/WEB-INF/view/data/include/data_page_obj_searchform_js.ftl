<#--
查询表单JS片段。

依赖：
data_page_obj.ftl
data_page_obj_searchform_html.ftl

变量：
//查询回调函数，不允许为null，格式为：function(searchParam){}
po.search = undefined;
//查询条件autocomplete初始数据，不允许为null
po.conditionAutocompleteSource = undefined;
-->
<script type="text/javascript">
(function(po)
{
	po.searchForm = function(){ return this.element("#${pageId}-searchForm"); };
	po.likeSwitchIcon = function(){ return this.element(".like-switch-icon", this.searchForm()); };
	po.notLikeInput = function(){ return this.element("input[name='notLike']", this.searchForm()); };
	po.keywordInput = function(){ return this.element("input[name='keyword']", this.searchForm()); };
	po.conditionPanel = function(){ return this.element(".condition-panel", this.searchForm()); };
	po.conditionTextarea = function(){ return this.element("textarea[name='condition']", this.searchForm()); };
	po.conditionIconParent = function(){ return this.element(".search-condition-icon-parent", this.searchForm()); };
	po.conditionIcon = function(){ return this.element(".search-condition-icon", this.searchForm()); };
	po.conditionIconTip = function(){ return this.element(".search-condition-icon-tip", this.searchForm()); };
	
	po._closeCondtionPanelWhenSubmit = true;
	
	po.searchForm().submit(function()
	{
		var searchParam = po.getSearchParam();
		
		po.search(searchParam);
		
		if(po._closeCondtionPanelWhenSubmit)
		{
			po.closeCondtionPanel();
			po.keywordInput().focus();
		}
		
		return false;
	});
	
	po.getSearchParam = function()
	{
		var nameLableCondition = $.trim(po.conditionTextarea().val());
		
		var param =
		{
			"keyword" : $.trim(po.keywordInput().val()),
			"notLike" : $.trim(po.notLikeInput().val()),
			"condition" : $.convertToPropertyPathCondtion(po.conditionAutocompleteSource, nameLableCondition)
		};
		
		return param;
	};
	
	po.clearSearchCondition = function()
	{
		po.conditionTextarea().val("");
	};

	//提取用于autocomplete的关键词
	po.extractLastConditionTerm = function(text)
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
	
	po.initConditionPanel = function()
	{
		po.conditionTextarea().autocomplete(
		{
			appendTo : po.element(".condition-parent", po.searchForm()),
			minLength: 0,
			autoFocus: false,
			source: function(request, response)
			{
				response($.ui.autocomplete.filter(po.conditionAutocompleteSource, po.extractLastConditionTerm(request.term)));
		    },
	        focus: function()
	        {
	        	return false;
			},
			select: function(event, ui)
			{
				var lastTerm = po.extractLastConditionTerm(this.value);
				
	            this.value = (lastTerm.length > 0 ? this.value.substring(0, this.value.length - lastTerm.length) : this.value) + ui.item.value;
	            return false;
	          },
			classes: { "ui-autocomplete": "ui-widget-shadow" },
			position:
			{
				//定位至光标位置
				using : function(pos, eleInfo)
				{
					var pos = po.conditionTextarea().textareaHelper('caretPos');
					pos.top = po.conditionTextarea().textareaHelper('height');
					
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
	
	po.updateNotLikeKeyword = function(notLike)
	{
		if(notLike == undefined)
			notLike = po.notLikeInput().val();
		
		if(notLike)
		{
			po.likeSwitchIcon().removeClass("ui-icon-radio-off").addClass("ui-icon-radio-on").attr("title", "<@spring.message code='data.notLikeTitle' />");
			po.notLikeInput().val("1");
		}
		else
		{
			po.likeSwitchIcon().removeClass("ui-icon-radio-on").addClass("ui-icon-radio-off").attr("title", "<@spring.message code='data.likeTitle' />");
			po.notLikeInput().val("");
		}
	};
	
	po.switchLikeNotLikeKeyword = function()
	{
		if(po.notLikeInput().val())
			po.updateNotLikeKeyword(false);
		else
			po.updateNotLikeKeyword(true);
	};
	
	po.closeCondtionPanel = function()
	{
		po.conditionPanel().hide();

		po.conditionIcon().removeClass("ui-icon-caret-1-n").addClass("ui-icon-caret-1-s");
		
		if($.trim(po.conditionTextarea().val()) != "")
			po.conditionIconTip().show();
	};
	
	po.openCondtionPanel = function()
	{
		po.conditionPanel().show();
		po.conditionIcon().removeClass("ui-icon-caret-1-s").addClass("ui-icon-caret-1-n");
		po.conditionIconTip().hide();
		po.conditionTextarea().focus();
	};
	
	po.likeSwitchIcon().click(function()
	{
		po.switchLikeNotLikeKeyword();

		po.keywordInput().focus();
	});
	
	po.conditionIconParent().click(function()
	{
		if(po.conditionIcon().hasClass("ui-icon-caret-1-s"))
		{
			po.openCondtionPanel();
		}
		else
		{
			po.closeCondtionPanel();
		}
	});
	
	po.element(".condition-panel-resetpos-icon", po.searchForm()).click(function()
	{
		po.conditionPanel().css("left", 0).css("top", 0);
	});
	
	po.element(".condition-panel-clear-icon", po.searchForm()).click(function()
	{
		po.clearSearchCondition();
		po.conditionTextarea().focus();
	});
	
	po.element(".condition-panel-submit-icon", po.searchForm()).click(function()
	{
		po._closeCondtionPanelWhenSubmit = false;
		po.searchForm().submit();
		po.conditionTextarea().focus();
		po._closeCondtionPanelWhenSubmit = true;
	});
	
	po.searchForm().keydown(function(event)
	{
		//打开、关闭条件面板
		if(event.keyCode == $.ui.keyCode.DOWN && event.ctrlKey)
		{
			if(po.conditionPanel().is(":hidden"))
				po.openCondtionPanel();
			else
			{
				po.closeCondtionPanel();
				po.keywordInput().focus();
			}
			
			event.stopPropagation();
		}
		//切换“LIKE”与“NOT LIKE”
		else if(event.keyCode == 49 && event.ctrlKey && event.shiftKey)
		{
			po.switchLikeNotLikeKeyword();
			event.stopPropagation();
		}
	});
	
	po.conditionPanel().keydown(function(event)
	{
		if(event.keyCode == $.ui.keyCode.ENTER && event.ctrlKey)
		{
			po._closeCondtionPanelWhenSubmit = false;
			po.searchForm().submit();
			po._closeCondtionPanelWhenSubmit = true;
			
			event.stopPropagation();
		}
		else if(event.keyCode == $.ui.keyCode.ESCAPE)
		{
			po.closeCondtionPanel();
			po.keywordInput().focus();
			
			event.stopPropagation();
		}
		else if(event.keyCode == $.ui.keyCode.BACKSPACE && event.ctrlKey && event.shiftKey)
		{
			po.clearSearchCondition();
			po.conditionTextarea().focus();
			
			event.stopPropagation();
		}
	});
	
	$(document.body).bind("click", function(event)
	{
		if($(event.target).closest(po.searchForm()).length == 0)
			po.closeCondtionPanel();
	});
	
	po.conditionPanel().draggable({ handle: ".condition-panel-title-bar" });
	$.resizableStopPropagation(po.conditionPanel());
	
	po.element("input:submit", po.searchForm()).button();
	po.updateNotLikeKeyword();
	po.closeCondtionPanel();
})
(${pageId});
</script>

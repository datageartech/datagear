<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#--
查询表单片段。

依赖：
data_page_obj.ftl
page_obj_sqlEditor.ftl

变量：
//查询回调函数，不允许为null，格式为：function(searchParam){}
po.search = undefined;
-->
<form id="${pageId}-searchForm" class="search-form search-form-data" action="#" tabindex="0" auto-close-prevent="condition-panel">
	<div class="ui-widget ui-widget-content ui-corner-all keyword-widget">
		<div class="like-switch-icon-parent" title="<@spring.message code='data.likeTitle' />">
			<span class="ui-icon like-switch-icon ui-icon-radio-on"></span>
		</div>
		<div class="keyword-input-parent"><input name="keyword" type="text" class="ui-widget ui-widget-content ui-corner-all keyword-input" tabindex="2" title="<@spring.message code='data.keywordTitle' />" /></div>
		<input type="hidden" name="notLike" value="false" />
		<div class="search-condition-icon-parent" title="<@spring.message code='data.conditionPanelWithShortcut' />">
			<span class="search-condition-icon ui-icon ui-icon-caret-1-s"></span>
			<span class="search-condition-icon-tip ui-icon ui-icon-bullet"></span>
		</div>
		<div class="condition-panel-parent">
			<div class="condition-panel auto-close-panel ui-widget ui-widget-content ui-corner-all ui-widget-shadow" tabindex="0">
				<div class="ui-corner-all ui-widget-header ui-helper-clearfix ui-draggable-handle condition-panel-title-bar">
					<span class="ui-icon ui-icon-arrowthickstop-1-n condition-panel-resetpos-icon" title="<@spring.message code='restoration' />"></span>
				</div>
				<div class="condition-parent ui-widget ui-widget-content ui-corner-all">
					<div id="${pageId}-conditionEditor" class="code-editor"></div>
				</div>
				<div class="condition-action">
					<span class="ui-icon ui-icon-trash condition-panel-clear-icon" title="<@spring.message code='data.clearWithShortcut' />"></span>
				</div>
			</div>
		</div>
	</div>
	<button type="submit" class="ui-button ui-corner-all ui-widget" tabindex="3"><@spring.message code='query' /></button>
</form>
<script type="text/javascript">
(function(po)
{
	po.searchForm = function(){ return this.elementOfId("${pageId}-searchForm"); };
	po.likeSwitchIconParent = function(){ return this.element(".like-switch-icon-parent", this.searchForm()); };
	po.notLikeInput = function(){ return this.elementOfName("notLike", this.searchForm()); };
	po.keywordInput = function(){ return this.elementOfName("keyword", this.searchForm()); };
	po.conditionPanel = function(){ return this.element(".condition-panel", this.searchForm()); };
	po.conditionIconParent = function(){ return this.element(".search-condition-icon-parent", this.searchForm()); };
	po.conditionIconTip = function(){ return this.element(".search-condition-icon-tip", this.searchForm()); };
	
	po.conditionPanel().addClass("transparency");
	
	po.searchForm().submit(function()
	{
		var searchParam = po.getSearchParam();
		po.search(searchParam);
		
		return false;
	});
	
	po.getSearchParam = function()
	{
		var param =
		{
			"keyword" : $.trim(po.keywordInput().val()),
			"notLike" : $.trim(po.notLikeInput().val()),
			"condition" : po.getConditionText()
		};
		
		return param;
	};
	
	po.conditionPrefixRegex = /^\s*WHERE/i;
	
	po.getConditionText = function()
	{
		var ct = po.getCodeText(po.conditionEditor);
		ct = ct.replace(po.conditionPrefixRegex, "");
		
		return $.trim(ct);
	};
	
	po.clearSearchCondition = function()
	{
		po.setCodeText(po.conditionEditor, "");
		po.insertCodeText(po.conditionEditor, "WHERE\n");
	};
	
	po.getSqlEditorSchemaId = function()
	{
		return po.schemaId;
	};
	
	po.initConditionPanel = function(dbTable)
	{
		po.updateNotLikeKeyword();
		
		var columnCompletions = [];
		var columns = (dbTable.columns || []);
		for(var i=0; i<columns.length; i++)
			columnCompletions[i] = { name: columns[i].name, displayComment: dbTable.name };
		
		po.sqlHintCache.tableColumnCompletions[dbTable.name] = columnCompletions;
		
		po.resolveSqlHintInfoSuper = po.resolveSqlHintInfo;
		po.resolveSqlHintInfo = function(codeEditor, doc, cursor, cursorToken)
		{
			var hi = po.resolveSqlHintInfoSuper(codeEditor, doc, cursor, cursorToken);
			
			if(hi && hi.type == "column" && !hi.tableName)
				hi.tableName = dbTable.name;
			
			return hi;
		};
		
		po.conditionEditor = po.createSqlEditor(po.elementOfId("${pageId}-conditionEditor"), {lineNumbers: false});
		po.conditionPanel().removeClass("transparency")
			.data("auto-close-callback", function()
			{
				if(po.getConditionText() != "")
					po.conditionIconTip().show();
			});
		po.closeCondtionPanel();
	};
	
	po.updateNotLikeKeyword = function(notLike)
	{
		if(notLike == undefined)
			notLike = po.notLikeInput().val();
		
		if(notLike == "true")
		{
			po.likeSwitchIconParent().attr("title", "<@spring.message code='data.notLikeTitle' />");
			po.element(".like-switch-icon", po.likeSwitchIconParent()).removeClass("ui-icon-radio-on").addClass("ui-icon-radio-off");
			po.notLikeInput().val("true");
		}
		else
		{
			po.likeSwitchIconParent().attr("title", "<@spring.message code='data.likeTitle' />");
			po.element(".like-switch-icon", po.likeSwitchIconParent()).removeClass("ui-icon-radio-off").addClass("ui-icon-radio-on");
			po.notLikeInput().val("false");
		}
	};
	
	po.switchLikeNotLikeKeyword = function()
	{
		if(po.notLikeInput().val() == "true")
			po.updateNotLikeKeyword("false");
		else
			po.updateNotLikeKeyword("true");
	};
	
	po.closeCondtionPanel = function()
	{
		po.conditionPanel().hide();
		
		if(po.getConditionText() != "")
			po.conditionIconTip().show();
	};
	
	po.openCondtionPanel = function()
	{
		po.conditionPanel().show();
		po.conditionIconTip().hide();
		po.conditionEditor.focus();
		
		var ct = po.getCodeText(po.conditionEditor);
		if(!po.conditionPrefixRegex.test(ct))
		{
			po.setCodeText(po.conditionEditor, "");
			po.insertCodeText(po.conditionEditor, "WHERE\n" + ct);
		}
	};
	
	po.likeSwitchIconParent().click(function()
	{
		po.switchLikeNotLikeKeyword();
		po.keywordInput().focus();
	});
	
	po.conditionIconParent().click(function()
	{
		if(po.conditionPanel().is(":hidden"))
			po.openCondtionPanel();
		else
			po.closeCondtionPanel();
	});
	
	po.element(".condition-panel-resetpos-icon", po.searchForm()).click(function()
	{
		po.conditionPanel().css("left", 0).css("top", 0);
	});
	
	po.element(".condition-panel-clear-icon", po.searchForm()).click(function()
	{
		po.clearSearchCondition();
		po.conditionEditor.focus();
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
			po.searchForm().submit();
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
			po.conditionEditor.focus();
			event.stopPropagation();
		}
	});
	
	po.conditionPanel().draggable({ handle: ".condition-panel-title-bar" });
})
(${pageId});
</script>

<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#--
搜索表单。

依赖：
page_sql_editor.ftl
-->
<form id="${pid}searchForm" @submit.prevent="onSearchFormSubmit" class="py-1">
	<div class="p-inputgroup">
		<div class="p-input-icon-left flex-grow-1">
			<i class="cursor-pointer" @click="onToggleSearchNotLike"
				:class="pm.searchForm.notLike ? 'pi pi-circle-fill' : 'pi pi-circle'"
				:title="pm.searchForm.notLike ? pm.searchNotLikeTrueDesc : pm.searchNotLikeFalseDesc">
			</i>
			<p-inputtext type="text" v-model="pm.searchForm.keyword" class="w-full h-full border-noround-right"></p-inputtext>
		</div>
		<p-button type="button" :icon="pm.searchForm.condition == '' ? 'pi pi-angle-down' : 'pi pi-angle-double-down'"
			aria:haspopup="true" aria-controls="${pid}searchConditionPanel"
			@click="onToggleSearchConditionPanel">
		</p-button>
		<p-button type="submit" icon="pi pi-search" class="px-4"></p-button>
	</div>
</form>
<p-overlaypanel ref="${pid}searchConditionPanelEle" append-to="body"
	:show-close-icon="false" id="${pid}searchConditionPanel" class="opacity-hide-absolute"
	@show="onSearchConditionPanelShow" @hide="onSearchConditionPanelHide">
	<div class="pb-2">
		<label class="text-lg font-bold">
			<@spring.message code='searchCondition' />
		</label>
	</div>
	<div class="p-2 panel-content-size-xxs">
		<div class="code-editor-wrapper p-component p-inputtext w-full h-full">
			<div id="${pid}searchConditionEditor" class="code-editor"></div>
		</div>
	</div>
	<div class="px-2">
		<div class="flex justify-content-between">
			<p-button type="button"  icon="pi pi-trash" title="<@spring.message code='clear' />"
				class="p-button-secondary p-button-sm" @click="onClearSearchCondition">
			</p-button>
			<p-button type="button" icon="pi pi-search"
				class="p-button-sm px-4" @click="onSearchFormSubmit">
			</p-button>
		</div>
	</div>
</p-overlaypanel>
<script>
(function(po)
{
	po.search = function(formData){ /*需实现*/ };
	
	po.submitSearchForm = function()
	{
		var param = po.searchFormParam();
		po.search(param);
	};
	
	po.searchFormParam = function()
	{
		return po.vueRaw(po.vuePageModel().searchForm);
	};
	
	po.searchConditionPrefixRegex = /^\s*WHERE/i;
	
	po.toSearchConditionPanelValue = function(condition)
	{
		if(!po.searchConditionPrefixRegex.test(condition))
			condition = "WHERE \n" + condition;
		
		return condition;
	};
	
	po.fromSearchConditionPanelValue = function(condition)
	{
		condition = condition.replace(po.searchConditionPrefixRegex, "");
		return $.trim(condition);
	};
	
	po.rewriteSqlEditorHint = function(dbTable)
	{
		var columns = (dbTable.columns || []);
		var columnCompletions = [];
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
	};
	
	po.mergeSearchConditionPanelValue = function()
	{
		var pm = po.vuePageModel();
		
		if(po.searchConditionCodeEditor)
		{
			var condition = po.getCodeText(po.searchConditionCodeEditor);
			pm.searchForm.condition = po.fromSearchConditionPanelValue(condition);
		}
	};
	
	po.initSearchConditionCodeEditor = function(ele, dbTable)
	{
		ele.empty();
		
		var pm = po.vuePageModel();
		var condition = po.toSearchConditionPanelValue(pm.searchForm.condition);
		
		po.searchConditionCodeEditor = po.createSqlEditor(ele,
		{
			lineNumbers: false,
			extraKeys:
			{
				"Ctrl-Enter": function(editor)
				{
					var condition = po.getCodeText(editor);
					pm.searchForm.condition = po.fromSearchConditionPanelValue(condition);
					po.submitSearchForm();
				}
			}
		});
		//在对话框时，直接初始化代码编辑器会出现行号错位的情况，使用这种方式可解决
		setTimeout(function()
		{
			po.searchConditionCodeEditor.focus();
			po.insertCodeText(po.searchConditionCodeEditor, condition);
		},
		100);
	};
	
	po.setupSearchForm = function(dbTable)
	{
		po.rewriteSqlEditorHint(dbTable);
		
		po.vuePageModel(
		{
			searchForm: { keyword: "", notLike: false, condition: "" },
			searchNotLikeTrueDesc: "<@spring.message code='data.searchForm.notLike.true.desc' />",
			searchNotLikeFalseDesc: "<@spring.message code='data.searchForm.notLike.false.desc' />"
		});
		
		po.vueMethod(
		{
			onToggleSearchNotLike: function()
			{
				var pm = po.vuePageModel();
				pm.searchForm.notLike = !pm.searchForm.notLike;
			},
			
			onToggleSearchConditionPanel: function(e)
			{
				po.vueUnref("${pid}searchConditionPanelEle").toggle(e);
			},
			
			onSearchConditionPanelShow: function()
			{
				var panel = po.elementOfId("${pid}searchConditionPanel", document.body);
				panel.removeClass("opacity-hide-absolute");
				
				var codeEditorEle = po.elementOfId("${pid}searchConditionEditor", panel);
				po.initSearchConditionCodeEditor(codeEditorEle, dbTable);
			},
			
			onSearchConditionPanelHide: function()
			{
				var panel = po.elementOfId("${pid}searchConditionPanel", document.body);
				//避免刷新表结构操作时空白区显示这个面板，避免po.onDbTable()过程中此面板占空间导致出现滚动条而抖屏
				panel.addClass("opacity-hide-absolute");
				
				po.mergeSearchConditionPanelValue();
			},
			
			onClearSearchCondition: function()
			{
				if(po.searchConditionCodeEditor)
				{
					var condition = po.toSearchConditionPanelValue("");
					po.setCodeText(po.searchConditionCodeEditor, "");
					po.insertCodeText(po.searchConditionCodeEditor, condition);
					po.searchConditionCodeEditor.focus();
				}
			},
			
			onSearchFormSubmit: function()
			{
				po.mergeSearchConditionPanelValue();
				po.submitSearchForm();
			}
		});
		
		po.vueRef("${pid}searchConditionPanelEle", null);
	};
})
(${pid});
</script>

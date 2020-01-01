<#--
SQL编辑器JS片段。

依赖：
page_js_obj.ftl

变量：
//数据源ID，不允许为null
po.getSqlEditorSchemaId
-->
<script type="text/javascript">
(function(po)
{
	//SQL编辑器，调用po.initSqlEditor()后初始化
	po.sqlEditor = undefined;
	
	po.getSqlEditorSchemaId = function(){ return undefined; };
	
	po.getSqlEditorElementId = function()
	{
		return "${pageId}-sql-editor";
	};
	
	po.getSqlEditorAutocompleteAjaxOptions = function(autocompleteInfo)
	{
		var url = "${contextPath}/sqlEditor/"+po.getSqlEditorSchemaId()+"/";
		var data = { "keyword" : "" };
		
		if(autocompleteInfo.type == "table")
			url += "findTableNames";
		else if(autocompleteInfo.type == "column")
		{
			url += "findColumnNames";
			data.table = autocompleteInfo.table;
		}
		else
			url += "findUnknownNames";
		
		return { "url" : url, "data" : data };
	};
	
	po.sqlEditorCompleters =
	[
		{
			identifierRegexps : [/[a-zA-Z_0-9\.\$]/],
			getCompletions: function(editor, session, pos, prefix, callback)
			{
				po.getSqlAutocompleteCompletions(editor, session, pos, prefix, callback);
			}
		}
	];
	
	po.getSqlAutocompleteCompletions = function(editor, session, pos, prefix, callback)
	{
		if(!po.getSqlEditorSchemaId())
		{
			callback(null, []);
			return;
		}
		
		var info = $.sqlAutocomplete.resolveAutocompleteInfo(editor, session, pos, prefix, ";");
		
		if(info && info.type == "table" && po.sqlAutocompleteTableCompletions)
		{
			callback(null, po.sqlAutocompleteTableCompletions);
			return;
		}
		
		var tableAlias = $.sqlAutocomplete.resolveTableAlias(prefix);
		
		if(info && info.type == "column" && info.table && po.sqlAutocompleteColumnCompletions)
		{
			var columns = po.sqlAutocompleteColumnCompletions[info.table];
			
			if(columns != null)
			{
				var completions = $.sqlAutocomplete.buildCompletions(columns, (tableAlias ? tableAlias+"." : ""));
				
				callback(null, completions);
				return;
			}
		}
		
		if(info && (info.type == "table" || (info.type == "column" && info.table)))
		{
			var ajaxOptions =
			{
				type : "POST",
				success : function(names)
				{
					var completions;
					
					if(info.type == "table")
					{
						completions = $.sqlAutocomplete.buildCompletions(names);
						po.sqlAutocompleteTableCompletions = completions;
					}
					else if(info.type == "column")
					{
						completions = $.sqlAutocomplete.buildCompletions(names, (tableAlias ? tableAlias+"." : ""));
						
						if(!po.sqlAutocompleteColumnCompletions)
							po.sqlAutocompleteColumnCompletions = {};
						
						if(names && names.length > 0)
							po.sqlAutocompleteColumnCompletions[info.table] = names;
					}
					
					callback(null, completions);
				},
				error : function(){}
			};
			
			$.extend(ajaxOptions, po.getSqlEditorAutocompleteAjaxOptions(info));
			
			$.ajax(ajaxOptions);
		}
		else
			callback(null, []);
	};
	
	po.initSqlEditor = function()
	{
		var languageTools = ace.require("ace/ext/language_tools");
		var SqlMode = ace.require("ace/mode/sql").Mode;
		po.sqlEditor = ace.edit(po.getSqlEditorElementId());
		po.sqlEditor.session.setMode(new SqlMode());
		po.sqlEditor.setShowPrintMargin(false);
		po.sqlEditor.setOptions(
		{
			enableBasicAutocompletion: po.sqlEditorCompleters,
			enableLiveAutocompletion: po.sqlEditorCompleters
		});
		
		return po.sqlEditor;
	};
})
(${pageId});
</script>

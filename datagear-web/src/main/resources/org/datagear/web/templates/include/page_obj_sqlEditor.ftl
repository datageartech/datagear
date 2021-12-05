<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#--
SQL编辑器JS片段。

依赖：
page_js_obj.ftl
page_obj_codeEditor.ftl

变量：
//数据源ID，不允许为null
po.getSqlEditorSchemaId
-->
<script type="text/javascript">
(function(po)
{
	//SQL提示缓存
	po.sqlHintCache =
	{
		//表名 -> 列名
		tableColumnCompletions: {},
		tableNameCompletions: [],
		tableNameCompletionsLoaded: false,
		ajaxRunning: false
	};
	
	po.initSqlEditor = function(dom, options)
	{
		options = (options || {});
		options.mode = "sql";
		
		if(!options.readOnly)
		{
			options.hintOptions = (options.hintOptions || {});
			options.hintOptions.hint = po.sqlEditorHintHandler;
			options.hintOptions.hint.async = true;
		}
		
		return po.initWorkspaceEditor(dom, options);
	};
	
	po.sqlEditorHintHandler = function(codeEditor, callback)
	{
		var doc = codeEditor.getDoc();
		var cursor = doc.getCursor();
		var mode = (codeEditor.getModeAt(cursor) || {});
		var token = (codeEditor.getTokenAt(cursor) || {});
		
		//关键字token不应提示
		//回车、空格等空白token也不提示，因为显得过于干扰
		if(token.type == "keyword" || /^\s*$/.test(token.string))
		{
			callback();
			return;
		}
		
		var hintInfo = po.resolveSqlHintInfo(codeEditor, doc, cursor, token);
		
		console.log("hintInfo :");
		console.dir(hintInfo);
		
		if(!hintInfo)
		{
			callback();
			return;
		}
		
		var namePrefix = hintInfo.namePrefix;
		
		if(hintInfo.type == "table")
		{
			if(po.sqlHintCache.tableNameCompletionsLoaded)
			{
				var completions =
				{
					list: po.findCompletionList(po.sqlHintCache.tableNameCompletions, namePrefix),
					from: CodeMirror.Pos(cursor.line, (namePrefix ? token.start : token.end)),
					to: CodeMirror.Pos(cursor.line, token.end)
				};
				
				callback(completions);
			}
			else
			{
				if(po.sqlHintCache.ajaxRunning)
					callback();
				else
				{
					po.sqlHintCache.ajaxRunning = true;
					
					$.ajax(
					{
						type : "POST",
						url: "${contextPath}/sqlEditor/"+po.getSqlEditorSchemaId()+"/findTableNames",
						success: function(names)
						{
							names = (names || []);
							
							var tableNameCompletions = [];
							
							for(var i=0; i<names.length; i++)
								tableNameCompletions[i] = { name: names[i] };
							
							po.sqlHintCache.tableNameCompletions = tableNameCompletions;
							po.sqlHintCache.tableNameCompletionsLoaded = true;
							
							var completions =
							{
								list: po.findCompletionList(po.sqlHintCache.tableNameCompletions, namePrefix),
								from: CodeMirror.Pos(cursor.line, (namePrefix ? token.start : token.end)),
								to: CodeMirror.Pos(cursor.line, token.end)
							};
							
							callback(completions);
							po.sqlHintCache.ajaxRunning = false;
						},
						error: function()
						{
							callback();
							po.sqlHintCache.ajaxRunning = false;
						}
					});
				}
			}
		}
		else if(hintInfo.type == "column" && hintInfo.tableName)
		{
			if(po.sqlHintCache.tableColumnCompletions[hintInfo.tableName])
			{
				var completions =
				{
					list: po.findCompletionList(po.sqlHintCache.tableColumnCompletions[hintInfo.tableName], namePrefix),
					from: CodeMirror.Pos(cursor.line, (namePrefix ? token.start : token.end)),
					to: CodeMirror.Pos(cursor.line, token.end)
				};
				
				callback(completions);
			}
			else
			{
				if(po.sqlHintCache.ajaxRunning)
					callback();
				else
				{
					po.sqlHintCache.ajaxRunning = true;
					
					$.ajax(
					{
						type : "POST",
						url: "${contextPath}/sqlEditor/"+po.getSqlEditorSchemaId()+"/findColumnNames",
						data: { table: hintInfo.tableName },
						success: function(names)
						{
							names = (names || []);
							
							var columnCompletions = [];
							
							for(var i=0; i<names.length; i++)
								columnCompletions[i] = { name: names[i], displayComment: hintInfo.tableName };
							
							po.sqlHintCache.tableColumnCompletions[hintInfo.tableName] = columnCompletions;
							
							var completions =
							{
								list: po.findCompletionList(po.sqlHintCache.tableColumnCompletions[hintInfo.tableName], namePrefix),
								from: CodeMirror.Pos(cursor.line, (namePrefix ? token.start : token.end)),
								to: CodeMirror.Pos(cursor.line, token.end)
							};
							
							callback(completions);
							po.sqlHintCache.ajaxRunning = false;
						},
						error: function()
						{
							callback();
							po.sqlHintCache.ajaxRunning = false;
						}
					});
				}
			}
		}
		else
			callback();
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
	
	po.resolveSqlHintInfo = function(codeEditor, doc, cursor, cursorToken)
	{
		var info = null;
		
		var cursorTokenString = ($.trim(cursorToken.string) || "");
		
		var tokenInfo = null;
		var cursorTmp = cursor;
		var cursorTokenTmp = cursorToken;
		
		while((tokenInfo = po.findPrevTokenInfoOfType(codeEditor, doc, cursorTmp, cursorTokenTmp, "keyword")) != null)
		{
			var keywordToken = tokenInfo.token;
			var keyword = (keywordToken.string || "").toUpperCase();
			
			if(po.sqlKeywords.all[keyword])
			{
				if(po.sqlKeywords.nextIsTable[keyword])
					info = { type: "table", namePrefix: (po.isNormalSqlNameTokenType(cursorToken.type) ? ($.trim(cursorToken.string) || "") : "") };
				else if(po.sqlKeywords.nextIsColumn[keyword])
					info = { type: "column" };
				
				break;
			}
			
			cursorTmp = CodeMirror.Pos(tokenInfo.line, keywordToken.start);
			cursorTokenTmp = keywordToken;
		}
		
		//查找表名
		if(info && info.type == "column" && tokenInfo)
		{
			var columnInfoStr = po.resolveSqlColumnInfoString(codeEditor, doc, cursor, cursorToken);
			
			if(columnInfoStr)
			{
				var columnInfoStrs = columnInfoStr.split(".");
				info.namePrefix = (columnInfoStrs.length > 1 ? columnInfoStrs[1] : columnInfoStrs[0]);
				info.tableName = (columnInfoStrs.length > 1 ? columnInfoStrs[0] : null);
			}
			
			//向上直到SQL语句开头
			while(tokenInfo != null)
			{
				var myToken = tokenInfo.token;
				var myString = (myToken.string || "").toUpperCase();
				
				if(po.sqlKeywords.start[myString])
					break;
				
				tokenInfo = po.findPrevTokenInfoOfType(codeEditor, doc, CodeMirror.Pos(tokenInfo.line, myToken.start), myToken, "keyword");
			}
			
			//向下查找表名的前置关键字token
			while(tokenInfo != null)
			{
				var myToken = tokenInfo.token;
				var myString = (myToken.string || "").toUpperCase();
				
				if(po.sqlKeywords.nextIsTable[myString])
					break;
				
				tokenInfo = po.findNextTokenInfoOfType(codeEditor, doc, CodeMirror.Pos(tokenInfo.line, myToken.start), myToken, "keyword");
			}
			
			//向下解析表名
			if(tokenInfo)
			{
				var prevTokenType = null, prevTokenString = null;
				var prevPrevTokenType = null, prevPrevTokenString = null;
				tokenInfo = po.findNextTokenInfo(codeEditor, doc, CodeMirror.Pos(tokenInfo.line, tokenInfo.token.start), tokenInfo.token,
				function(token)
				{
					//如果有括号，说明是复杂语句，暂不解析
					if(token.type == "bracket")
						return true;
					
					var myString = ($.trim(token.string) || "");
					
					if(!myString)
						return false;
					
					if(po.isNormalSqlNameTokenType(token.type))
					{
						//如果没有表别名，则使用第一个作为表名
						if(!info.tableName)
						{
							info.tableName = myString;
							return true;
						}
						else
						{
							//判断是否表别名
							if(myString == info.tableName)
							{
								//表名 AS 别名
								if(prevTokenType == "keyword" && /as/i.test(prevTokenString)
										&& po.isNormalSqlNameTokenType(prevPrevTokenType) && prevPrevTokenString)
								{
									info.tableName = prevPrevTokenString;
								}
								//表名 别名
								else if(po.isNormalSqlNameTokenType(prevTokenType) && prevTokenString)
								{
									info.tableName = prevTokenString;
								}
								
								return true;
							}
						}
					}
					
					prevPrevTokenType = prevTokenType;
					prevPrevTokenString = prevTokenString;
					prevTokenType = token.type;
					prevTokenString = myString;
				});
			}
		}
		
		return info;
	};
	
	po.resolveSqlColumnInfoString = function(codeEditor, doc, cursor, cursorToken)
	{
		var columnInfoString = "";
		
		if(po.isSqlColumnInputStringPart(cursorToken))
		{
			columnInfoString = cursorToken.string;
			
			po.findPrevTokenInfo(codeEditor, doc, cursor, cursorToken, function(token)
			{
				if(po.isSqlColumnInputStringPart(token))
					columnInfoString = token.string + columnInfoString;
				else
					return true;
			});
		}
		
		return columnInfoString;
	};
	
	po.isSqlColumnInputStringPart = function(cursorToken)
	{
		var str = cursorToken.string;
		
		//","、"("、"空白" 不是列相关输入字符串
		if(/^[\(\,]$/.test(str) || /^\s*$/.test(str))
			return false;
		
		return true;
	};
	
	po.isNormalSqlNameTokenType = function(tokenType)
	{
		return (tokenType == null);
	};
	
	po.sqlKeywords =
	{
		//全部，会由下面关键字合并而得
		all: {},
		
		//SQL语句开始关键字*（必须大写）
		start:
		{
			"SELECT" : true, "INSERT" : true, "UPDATE" : true, "DELETE" : true,
			"ALTER" : true, "DROP" : true, "CREATE" : true, "REPLACE" : true, "MERGE" : true,
			"GRANT" : true
		},
		
		//下一个Token是表名（必须大写）
		nextIsTable:
		{
			"FROM" : true,
			"JOIN" : true,
			"UPDATE" : true,
			"INTO" : true,
			"TABLE" : true
		},
		
		//下一个Token是列名（必须大写）
		nextIsColumn:
		{
			"SELECT" : true,
			"WHERE" : true,
			"ON" : true,
			"BY" : true,
			"SET" : true
		}
	};
	
	po.sqlKeywords.all = $.extend(po.sqlKeywords.all, po.sqlKeywords.start, po.sqlKeywords.nextIsTable, po.sqlKeywords.nextIsColumn);
})
(${pageId});
</script>

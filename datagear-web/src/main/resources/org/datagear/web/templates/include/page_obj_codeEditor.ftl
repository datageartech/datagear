<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#--
代码编辑器JS片段。

依赖：
page_js_obj.ftl

-->
<script type="text/javascript">
(function(po)
{
	po.createCodeEditor = function(dom, options)
	{
		dom = $(dom)[0];
		
		options = (options || {});
		
		//采用系统切换主题功能模式
		options.theme = "custom";
		
		if(options.lineNumbers == null)
			options.lineNumbers = true;
		
		if(options.smartIndent == null)
			options.smartIndent = false;
		
		//强制禁用completeSingle选项，因为编辑器hint都是在下面的change事件中触发的
		//如果这里为true，将会hint死循环，且会导致退格操作无效
		if(options.hintOptions)
			options.hintOptions.completeSingle = false;
		
		//if(options.hintOptions)
		//	options.hintOptions.closeOnUnfocus = false;
		
		var codeEditor = CodeMirror(dom, options);
		
		if(options.hintOptions && !options.readOnly)
		{
			codeEditor.on("change", function(codeEditor, changeObj)
			{
				codeEditor.showHint();
			});
		}
		
		return codeEditor;
	};
	
	po.evalCodeModeByName = function(name)
	{
		var mode = undefined;
		
		if($.isHtmlFile(name))
			mode = "htmlmixed";
		else if($.isJsFile(name))
			mode = "javascript";
		else if($.isCssFile(name))
			mode = "css";
		
		return mode;
	};
	
	po.getCodeText = function(codeEditor)
	{
		return codeEditor.getValue();
	};
	
	po.getSelectedCodeText = function(codeEditor)
	{
		var doc = codeEditor.getDoc();
		return (doc.getSelection() || "");
	};
	
	po.getSelectedCodeInfo = function(codeEditor)
	{
		var doc = codeEditor.getDoc();
		var selCodes = doc.getSelections();
		var selRanges = doc.listSelections();
		
		var selText = (selCodes && selCodes[0] ? (selCodes[0] || "") : "");
		var from = (selRanges && selRanges[0] ? selRanges[0].anchor : null);
		var to = (selRanges && selRanges[0] ? selRanges[0].head : null);
		
		if(from && to)
		{
			var swap = ((from.line > to.line) || (from.line == to.line && from.ch > to.ch));
			if(swap)
			{
				var fromTmp = from;
				from = to;
				to = fromTmp;
			}
		}
		
		return { text: selText, from: from, to: to };
	};
	
	po.insertCodeText = function(codeEditor, cursor, text)
	{
		//(codeEditor, text)
		if(arguments.length == 2)
		{
			text = cursor;
			cursor = undefined;
		}
		
		var doc = codeEditor.getDoc();
		cursor = (cursor == null ? doc.getCursor() : cursor);
		
		doc.replaceRange(text, cursor);
	};
	
	//查找补全列表
	//completions : { name: "...", ?value: "...", ?displayName: "...", ?displayComment: "...", ?categories: [ "小写字符串", ... ] }
	po.findCompletionList = function(completions, namePrefix, category)
	{
		var re = [];
		
		if(!completions)
			return re;
		
		namePrefix = (namePrefix ? namePrefix.toLowerCase() : namePrefix);
		category = (category ? category.toLowerCase() : category);
		
		for(var i=0; i<completions.length; i++)
		{
			var comp = completions[i];
			
			//相同时不必列入提示，影响输入
			if(namePrefix && namePrefix.length == comp.name.length)
				continue;
			
			var nameLower = comp.name.toLowerCase();
			
			if(namePrefix && nameLower.indexOf(namePrefix) != 0)
				continue;
			
			if(!category || (category && comp.categories && $.inArray(category, comp.categories) > -1))
			{
				re.push(
				{
					text: (comp.value ? comp.value : comp.name),
					displayText: (comp.displayName ? comp.displayName : comp.name),
					displayComment: comp.displayComment,
					render: po.renderCompletionItem
				});
			}
		}
		
		return re;
	};
	
	po.renderCompletionItem = function(element, self, data)
	{
		//$(element).addClass("code-completion-item");
		
		$("<span class='code-completion-item' />").text(data.displayText ? data.displayText : data.text).appendTo(element);
		if(data.displayComment)
			$("<span class='code-completion-comment' />").text(data.displayComment ? data.displayComment : "").appendTo(element);
	};
	
	po.findPrevTokenOfType = function(codeEditor, doc, cursor, cursorToken, tokenType)
	{
		var tokenInfo = po.findPrevTokenInfoOfType(codeEditor, doc, cursor, cursorToken, tokenType);
		return (tokenInfo ? tokenInfo.token : undefined);
	};
	
	po.findPrevTokenInfoOfType = function(codeEditor, doc, cursor, cursorToken, tokenType)
	{
		return po.findPrevTokenInfo(codeEditor, doc, cursor, cursorToken, function(token){ return (token.type == tokenType); });
	};
	
	po.findPrevTokenInfo = function(codeEditor, doc, cursor, cursorToken, predicate)
	{
		doc = (doc ? doc : codeEditor.getDoc());
		cursor = (cursor ? cursor : doc.getCursor());
		cursorToken = (cursorToken ? cursorToken : (codeEditor.getTokenAt(cursor) || {}));
		var minLine = (cursor.line-100 <= 0 ? 0 : cursor.line-100);
		
		for(var line=cursor.line; line >=minLine; line--)
		{
			var tokens = codeEditor.getLineTokens(line);
			for(var i=tokens.length-1; i>=0; i--)
			{
				var token = tokens[i];
				
				if(line == cursor.line && token.start >= cursorToken.start)
					continue;
				
				if(predicate(token) == true)
					return { token: token, line: line };
			}
		}
		
		return null;
	};
	
	po.findNextTokenInfoOfType = function(codeEditor, doc, cursor, cursorToken, tokenType)
	{
		return po.findNextTokenInfo(codeEditor, doc, cursor, cursorToken, function(token){ return (token.type == tokenType); });
	};
	
	po.findNextTokenInfo = function(codeEditor, doc, cursor, cursorToken, predicate)
	{
		doc = (doc ? doc : codeEditor.getDoc());
		var lastLine = doc.lastLine();
		cursor = (cursor ? cursor : doc.getCursor());
		cursorToken = (cursorToken ? cursorToken : (codeEditor.getTokenAt(cursor) || {}));
		var maxLine = (cursor.line+100 >= lastLine ? lastLine : cursor.line+100);
		
		for(var line=cursor.line; line <=lastLine; line++)
		{
			var tokens = codeEditor.getLineTokens(line);
			for(var i=0; i<tokens.length; i++)
			{
				var token = tokens[i];
				
				if(line == cursor.line && token.start <= cursorToken.start)
					continue;
				
				if(predicate(token) == true)
					return { token: token, line: line };
			}
		}
		
		return null;
	};
})
(${pageId});
</script>

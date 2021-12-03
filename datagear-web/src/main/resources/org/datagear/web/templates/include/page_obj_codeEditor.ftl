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
		options = (options || {});
		//采用系统切换主题功能模式
		options.theme = "custom";
		
		//强制禁用completeSingle选项，因为系统的代码编辑器hint都是在change事件中触发的
		//如果这里为true，将会hint死循环，且会导致退格操作无效
		if(options.hintOptions)
			options.hintOptions.completeSingle = false;
		
		if(options.hintOptions)
			options.hintOptions.closeOnUnfocus = false;
		
		return CodeMirror(dom, options);
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
		doc = (doc ? doc : codeEditor.getDoc());
		cursor = (cursor ? cursor : doc.getCursor());
		cursorToken = (cursorToken ? cursorToken : (codeEditor.getTokenAt(cursor) || {}));
		var minLine = (cursor.line-100 <= 0 ? 0 : cursor.line-100);
		
		for(var line=cursor.line; line >=minLine; line--)
		{
			var tokens = codeEditor.getLineTokens(cursor.line);
			for(var i=0; i<tokens.length; i++)
			{
				var token = tokens[i];
				
				if(line == cursor.line && token.start >= cursorToken.start)
					break;
				
				if(token.type == tokenType)
					return token;
			}
		}
		
		return null;
	};
})
(${pageId});
</script>

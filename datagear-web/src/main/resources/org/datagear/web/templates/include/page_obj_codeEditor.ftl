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
		
		return CodeMirror(dom, options);
	};
	
	//查找补全列表
	//completions : { name: "...", ?value: "...", ?displayName: "...", ?displayDesc: "...", ?categories: [ "...", ... ] }
	po.findCompletionList = function(completions, namePrefix, category)
	{
		var re = [];
		
		if(!completions || !namePrefix)
			return re;
		
		namePrefix = namePrefix.toLowerCase();
		
		for(var i=0; i<completions.length; i++)
		{
			var comp = completions[i];
			var nameLower = comp.name.toLowerCase();
			
			if(nameLower.indexOf(namePrefix) != 0)
				continue;
			
			if(!category || (category && comp.categories && $.inArray(category, comp.categories) > -1))
			{
				var displayText = (comp.displayName ? comp.displayName : comp.name)
						+ (comp.displayComment ? comp.displayComment : "");
				
				re.push(
				{
					text: (comp.value ? comp.value : comp.name),
					displayText: displayText
				});
			}
		}
		
		return re;
	};
})
(${pageId});
</script>

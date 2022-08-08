<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#--
复制到剪切板片段。
-->
<button id="${pid}copyToClipboardBtn" type="button" class="hidden">&nbsp;</button>
<script>
(function(po)
{
	po.copyToClipboard = function(content)
	{
		po.clipboardContent(content);
		po.elementOfId("${pid}copyToClipboardBtn").click();
	};
	
	po.clipboardContent = function(content)
	{
		if(content === undefined)
			return (po._clipboardContent || "");
		
		po._clipboardContent = content;
	};
	
	po.vueMounted(function()
	{
		var clipboard = new ClipboardJS(po.elementOfId("${pid}copyToClipboardBtn")[0],
		{
			//需要设置container，不然在对话框中打开页面后复制不起作用
			container: po.element()[0],
			text: function(trigger)
			{
				return po.clipboardContent();
			}
		});
		clipboard.on('success', function(e)
		{
			$.tipSuccess("<@spring.message code='copyToClipboardSuccess' />");
		});
	});
})
(${pid});
</script>

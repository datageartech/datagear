<#--
 *
 * Copyright 2018-2023 datagear.tech
 *
 * This file is part of DataGear.
 *
 * DataGear is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * DataGear is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with DataGear.
 * If not, see <https://www.gnu.org/licenses/>.
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

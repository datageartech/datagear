<#--
 *
 * Copyright 2018-present datagear.tech
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
	po.copyToClipboard = function(content, btnEle)
	{
		btnEle = (btnEle == null ? po.elementOfId("${pid}copyToClipboardBtn") : btnEle);
		
		po.clipboardContent(btnEle, content);
		btnEle.click();
	};
	
	po.clipboardContent = function(btnEle, content)
	{
		if(content === undefined)
			return (btnEle.data("COPY_TO_CLIPBOARD_CONTENT") || "");
		else
			btnEle.data("COPY_TO_CLIPBOARD_CONTENT", content);
	};
	
	po.initCopyToClipboard = function(containerEle, btnEle, successTip)
	{
		containerEle = $(containerEle);
		btnEle = $(btnEle);
		successTip = (successTip == null ? true : successTip);
		
		var clipboard = new ClipboardJS(btnEle[0],
		{
			//需要设置container，不然在对话框中打开页面后复制不起作用
			container: containerEle[0],
			text: function(trigger)
			{
				return po.clipboardContent(btnEle);
			}
		});
		
		if(successTip)
		{
			clipboard.on('success', function(e)
			{
				$.tipSuccess(successTip === true ? "<@spring.message code='copyToClipboardSuccess' />" : successTip);
			});
		}
		
		return clipboard;
	};
	
	po.vueMounted(function()
	{
		po.initCopyToClipboard(po.element(), po.elementOfId("${pid}copyToClipboardBtn"),
				"<@spring.message code='copyToClipboardSuccess' />");
	});
})
(${pid});
</script>

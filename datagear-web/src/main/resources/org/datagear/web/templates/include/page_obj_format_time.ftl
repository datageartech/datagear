<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#--
格式化时间JS片段。

依赖：
page_obj.ftl
-->
<script type="text/javascript">
(function(po)
{
	//格式化耗时毫秒数
	po.formatDuration = function(duration)
	{
		var text = "";
		
		var hours = Math.floor(duration/1000/60/60);
		
		if(hours > 0)
		{
			var minutes = Math.round(duration/1000/60 - hours*60);
			
			<#assign messageArgs=['"+hours+"', '"+minutes+"'] />
			text = "<@spring.messageArgs code='duration.H.M' args=messageArgs />";
		}
		else
		{
			var minutes = Math.floor(duration/1000/60);
			
			if(minutes > 0)
			{
				var seconds = Math.round(duration/1000 - minutes*60);
				
				<#assign messageArgs=['"+minutes+"', '"+seconds+"'] />
				text = "<@spring.messageArgs code='duration.M.S' args=messageArgs />";
			}
			else
			{
				var seconds = Math.floor(duration/1000);
				
				if(seconds > 0)
				{
					var mseconds = Math.round(duration - seconds*1000);
					
					<#assign messageArgs=['"+seconds+"', '"+mseconds+"'] />
					text = "<@spring.messageArgs code='duration.S.MS' args=messageArgs />";
				}
				else
				{
					<#assign messageArgs=['"+duration+"'] />
					text = "<@spring.messageArgs code='duration.MS' args=messageArgs />";
				}
			}
		}
		
		return text;
	};
})
(${pageId});
</script>

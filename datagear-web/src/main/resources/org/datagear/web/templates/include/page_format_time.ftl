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
			text = $.validator.format("<@spring.message code='duration.H.M' />", hours, minutes);
		}
		else
		{
			var minutes = Math.floor(duration/1000/60);
			
			if(minutes > 0)
			{
				var seconds = Math.round(duration/1000 - minutes*60);
				text = $.validator.format("<@spring.message code='duration.M.S' />", minutes, seconds);
			}
			else
			{
				var seconds = Math.floor(duration/1000);
				
				if(seconds > 0)
				{
					var mseconds = Math.round(duration - seconds*1000);
					text = $.validator.format("<@spring.message code='duration.S.MS' />", seconds, mseconds);
				}
				else
				{
					text = $.validator.format("<@spring.message code='duration.MS' />", duration);
				}
			}
		}
		
		return text;
	};
})
(${pid});
</script>

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

<#--
Cometd JS片段。

依赖：
page_js_obj.jsp
-->
<script type="text/javascript">
(function(po)
{
	if(!window.cometdSubscribedMap)
		window.cometdSubscribedMap = {};
	
	po.cometdInitIfNot = function()
	{
		var cometd = $.cometd;
		
		if(!window.cometdIsInit)
		{
			window.cometdIsInit = true;
			
			cometd.configure(
			{
				/*logLevel : "debug",*/
				url : "${contextPath}/cometd"
			});
			
			cometd.handshake(function(handshakeReply)
			{
				window.cometdSubscribedMap = {};
				window.cometdConnected = false;
				
				if(handshakeReply.successful)
					window.cometdConnected = true;
			});
		}
	};
	
	po.cometdExecuteAfterSubscribe = function(channelId, executeCallback, messageHandler)
	{
		if(!window.cometdConnected)
			return false;
		
		if(window.cometdSubscribedMap[channelId] != null)
			executeCallback();
		else
		{
			var cometd = $.cometd;
			
			cometd.subscribe(channelId, messageHandler,
			function(subscribeReply)
			{
				if(subscribeReply.successful)
				{
					window.cometdSubscribedMap[channelId] = "1";
					executeCallback();
				}
			});
		}
	}
})
(${pageId});
</script>

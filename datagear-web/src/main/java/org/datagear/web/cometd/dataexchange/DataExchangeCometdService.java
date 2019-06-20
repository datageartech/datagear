/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.web.cometd.dataexchange;

import org.cometd.bayeux.MarkedReference;
import org.cometd.bayeux.server.BayeuxServer;
import org.cometd.bayeux.server.ServerChannel;
import org.cometd.server.AbstractService;

/**
 * 数据交换cometd服务。
 * 
 * @author datagear@163.com
 *
 */
public class DataExchangeCometdService extends AbstractService
{
	public DataExchangeCometdService(BayeuxServer bayeux)
	{
		super(bayeux, "dataExchangeCometdService");
	}

	/**
	 * 发送消息。
	 * 
	 * @param channel
	 * @param message
	 */
	public void sendMessage(ServerChannel channel, DataExchangeMessage message)
	{
		channel.publish(getServerSession(), message);
	}

	/**
	 * 获取指定ID的消息通道，必要时建立通道。
	 * 
	 * @param channelId
	 * @return
	 */
	public ServerChannel getChannelWithCreation(String channelId)
	{
		MarkedReference<ServerChannel> markedReference = getBayeux().createChannelIfAbsent(channelId);

		return markedReference.getReference();
	}
}

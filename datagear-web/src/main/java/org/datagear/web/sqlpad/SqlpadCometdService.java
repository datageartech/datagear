package org.datagear.web.sqlpad;

import org.cometd.bayeux.server.BayeuxServer;
import org.cometd.server.AbstractService;

public class SqlpadCometdService extends AbstractService
{
	public SqlpadCometdService(BayeuxServer bayeux)
	{
		super(bayeux, "sqlpadCometdService");
		
		getServerSession();
	}
	
	/**
	 * 发送消息。
	 * @param channelName
	 * @param data
	 */
	public void publish(String channelName, String data)
	{
		getBayeux().getChannel(channelName).publish(getServerSession(), data);
	}
}

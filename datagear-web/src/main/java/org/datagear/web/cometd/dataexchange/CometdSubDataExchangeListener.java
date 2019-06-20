/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.web.cometd.dataexchange;

import org.cometd.bayeux.server.ServerChannel;
import org.datagear.dataexchange.DataExchangeListener;

/**
 * 基于Cometd的子数据交换{@linkplain DataExchangeListener}。
 * 
 * @author datagear@163.com
 *
 */
public abstract class CometdSubDataExchangeListener extends CometdDataExchangeListener
{
	private String subDataExchangeId;

	public CometdSubDataExchangeListener()
	{
		super();
	}

	public CometdSubDataExchangeListener(DataExchangeCometdService dataExchangeCometdService,
			ServerChannel dataExchangeServerChannel, String subDataExchangeId)
	{
		super(dataExchangeCometdService, dataExchangeServerChannel);
		this.subDataExchangeId = subDataExchangeId;
	}

	public String getSubDataExchangeId()
	{
		return subDataExchangeId;
	}

	public void setSubDataExchangeId(String subDataExchangeId)
	{
		this.subDataExchangeId = subDataExchangeId;
	}

	@Override
	protected DataExchangeMessage buildStartMessage()
	{
		return new SubStart(this.subDataExchangeId);
	}

	@Override
	protected DataExchangeMessage buildFinishMessage(long duration)
	{
		return new SubFinish(this.subDataExchangeId, duration);
	}

	/**
	 * 子数据交换消息。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public abstract static class SubDataExchangeMessage extends DataExchangeMessage
	{
		private String subDataExchangeId;

		public SubDataExchangeMessage()
		{
			super();
		}

		public SubDataExchangeMessage(String subDataExchangeId)
		{
			super();
			this.subDataExchangeId = subDataExchangeId;
		}

		public String getSubDataExchangeId()
		{
			return subDataExchangeId;
		}

		public void setSubDataExchangeId(String subDataExchangeId)
		{
			this.subDataExchangeId = subDataExchangeId;
		}
	}

	/**
	 * 子数据交换开始。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class SubStart extends StartMessage
	{
		private String subDataExchangeId;

		public SubStart()
		{
			super();
		}

		public SubStart(String subDataExchangeId)
		{
			super();
			this.subDataExchangeId = subDataExchangeId;
		}

		public String getSubDataExchangeId()
		{
			return subDataExchangeId;
		}

		public void setSubDataExchangeId(String subDataExchangeId)
		{
			this.subDataExchangeId = subDataExchangeId;
		}
	}

	/**
	 * 子数据交换开始。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class SubFinish extends SubDataExchangeMessage
	{
		private long duration;

		public SubFinish()
		{
			super();
		}

		public SubFinish(String subDataExchangeId, long duration)
		{
			super(subDataExchangeId);
			this.duration = duration;
		}

		public long getDuration()
		{
			return duration;
		}

		public void setDuration(long duration)
		{
			this.duration = duration;
		}
	}
}

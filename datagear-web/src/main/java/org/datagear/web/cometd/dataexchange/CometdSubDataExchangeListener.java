/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.web.cometd.dataexchange;

import org.cometd.bayeux.server.ServerChannel;
import org.datagear.dataexchange.DataExchangeException;
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
	protected DataExchangeMessage buildExceptionMessage(DataExchangeException e)
	{
		return new SubException(this.subDataExchangeId, e.getMessage());
	}

	@Override
	protected DataExchangeMessage buildSuccessMessage()
	{
		return new SubSuccess(this.subDataExchangeId);
	}

	@Override
	protected DataExchangeMessage buildFinishMessage(long duration)
	{
		return new SubFinish(this.subDataExchangeId, duration);
	}

	/**
	 * 子数据交换开始。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class SubStart extends SubDataExchangeMessage
	{
		public SubStart()
		{
			super();
		}

		public SubStart(String subDataExchangeId)
		{
			super(subDataExchangeId);
		}
	}

	/**
	 * 子数据交换异常。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class SubException extends SubDataExchangeMessage
	{
		private String content;

		public SubException()
		{
			super();
		}

		public SubException(String subDataExchangeId, String content)
		{
			super(subDataExchangeId);
			this.content = content;
		}

		public String getContent()
		{
			return content;
		}

		public void setContent(String content)
		{
			this.content = content;
		}
	}

	/**
	 * 子数据交换成功。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class SubSuccess extends SubDataExchangeMessage
	{
		public SubSuccess()
		{
			super();
		}

		public SubSuccess(String subDataExchangeId)
		{
			super(subDataExchangeId);
		}
	}

	/**
	 * 子数据交换完成。
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

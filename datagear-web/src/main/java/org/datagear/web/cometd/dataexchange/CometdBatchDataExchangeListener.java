/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.web.cometd.dataexchange;

import org.cometd.bayeux.server.ServerChannel;
import org.datagear.dataexchange.BatchDataExchangeListener;
import org.datagear.dataexchange.DataExchange;
import org.datagear.dataexchange.DataExchangeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 基于Cometd的{@linkplain BatchDataExchangeListener}。
 * 
 * @author datagear@163.com
 *
 * @param <T>
 */
public class CometdBatchDataExchangeListener<T extends DataExchange> extends CometdDataExchangeListener
		implements BatchDataExchangeListener<T>
{
	protected static final Logger LOGGER = LoggerFactory.getLogger(CometdBatchDataExchangeListener.class);

	private String[] subDataExchangeIds;

	public CometdBatchDataExchangeListener()
	{
		super();
	}

	public CometdBatchDataExchangeListener(DataExchangeCometdService dataExchangeCometdService,
			ServerChannel dataExchangeServerChannel, String[] subDataExchangeIds)
	{
		super(dataExchangeCometdService, dataExchangeServerChannel);
		this.subDataExchangeIds = subDataExchangeIds;
	}

	public String[] getSubDataExchangeIds()
	{
		return subDataExchangeIds;
	}

	public void setSubDataExchangeIds(String[] subDataExchangeIds)
	{
		this.subDataExchangeIds = subDataExchangeIds;
	}

	@Override
	public void onSubmitSuccess(T subDataExchange, int subDataExchangeIndex)
	{
		String subDataExchangeId = getSubDataExchangeId(subDataExchangeIndex);

		if (subDataExchange == null)
			return;

		sendMessage(new SubmitSuccess(subDataExchangeId));
	}

	@Override
	public void onSubmitFail(T subDataExchange, int subDataExchangeIndex, Throwable cause)
	{
		String subDataExchangeId = getSubDataExchangeId(subDataExchangeIndex);

		if (subDataExchange == null)
			return;

		sendMessage(new SubmitFail(subDataExchangeId));
	}

	@Override
	public void onCancel(T subDataExchange, int subDataExchangeIndex)
	{
		String subDataExchangeId = getSubDataExchangeId(subDataExchangeIndex);

		if (subDataExchange == null)
			return;

		sendMessage(new CancelSuccess(subDataExchangeId));
	}

	@Override
	protected DataExchangeMessage buildStartMessage()
	{
		return new Start();
	}

	@Override
	protected DataExchangeMessage buildExceptionMessage(DataExchangeException e)
	{
		return new Exception(e.getMessage());
	}

	@Override
	protected DataExchangeMessage buildSuccessMessage()
	{
		return new Success();
	}

	@Override
	protected DataExchangeMessage buildFinishMessage(long duration)
	{
		return new Finish(duration);
	}

	/**
	 * 获取子数据交换ID。
	 * <p>
	 * 出现任何异常将返回{@code null}。
	 * </p>
	 * 
	 * @param subDataExchangeIndex
	 * @return
	 */
	protected String getSubDataExchangeId(int subDataExchangeIndex)
	{
		try
		{
			return this.subDataExchangeIds[subDataExchangeIndex];
		}
		catch (Throwable t)
		{
			LOGGER.error("getSubDataExchangeId error", t);

			return null;
		}
	}

	/**
	 * 数据交换开始消息。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class Start extends DataExchangeMessage
	{
		public Start()
		{
			super();
		}
	}

	/**
	 * 数据交换异常消息。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class Exception extends DataExchangeMessage
	{
		private String content;

		public Exception()
		{
			super();
		}

		public Exception(String content)
		{
			super();
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

	public static class Success extends DataExchangeMessage
	{
		public Success()
		{
			super();
		}
	}

	/**
	 * 数据交换完成消息。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class Finish extends DataExchangeMessage
	{
		private long duration;

		public Finish()
		{
			super();
		}

		public Finish(long duration)
		{
			super();
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

	/**
	 * 子数据交换提交成功消息。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class SubmitSuccess extends SubDataExchangeMessage
	{
		public SubmitSuccess()
		{
			super();
		}

		public SubmitSuccess(String subDataExchangeId)
		{
			super(subDataExchangeId);
		}
	}

	/**
	 * 子数据交换提交失败消息。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class SubmitFail extends SubDataExchangeMessage
	{
		public SubmitFail()
		{
			super();
		}

		public SubmitFail(String subDataExchangeId)
		{
			super(subDataExchangeId);
		}
	}

	/**
	 * 子数据交换取消成功消息。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class CancelSuccess extends SubDataExchangeMessage
	{
		public CancelSuccess()
		{
			super();
		}

		public CancelSuccess(String subDataExchangeId)
		{
			super(subDataExchangeId);
		}
	}
}

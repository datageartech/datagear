/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.web.cometd.dataexchange;

import org.cometd.bayeux.server.ServerChannel;
import org.datagear.dataexchange.BatchDataExchangeListener;
import org.datagear.dataexchange.DataExchange;
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
	 * 子数据交换消息。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static abstract class SuDataExchangeMessage extends DataExchangeMessage
	{
		private String subDataExchangeId;

		public SuDataExchangeMessage()
		{
			super();
		}

		public SuDataExchangeMessage(String subDataExchangeId)
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
	 * 子数据交换提交成功消息。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class SubmitSuccess extends SuDataExchangeMessage
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
	public static class SubmitFail extends SuDataExchangeMessage
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
	public static class CancelSuccess extends SuDataExchangeMessage
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

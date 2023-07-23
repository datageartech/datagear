/*
 * Copyright 2018-2023 datagear.tech
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
 */

package org.datagear.web.dataexchange;

import java.util.Locale;

import org.datagear.dataexchange.BatchDataExchangeListener;
import org.datagear.dataexchange.CancelReason;
import org.datagear.dataexchange.SubDataExchange;
import org.datagear.dataexchange.SubmitFailException;
import org.datagear.web.util.MessageChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;

/**
 * 发送消息的{@linkplain BatchDataExchangeListener}。
 * 
 * @author datagear@163.com
 *
 * @param <T>
 */
public class MessageBatchDataExchangeListener extends MessageDataExchangeListener implements BatchDataExchangeListener
{
	protected static final Logger LOGGER = LoggerFactory.getLogger(MessageBatchDataExchangeListener.class);

	public MessageBatchDataExchangeListener()
	{
		super();
	}

	public MessageBatchDataExchangeListener(MessageChannel messageChannel,
			String dataExchangeServerChannel, MessageSource messageSource, Locale locale)
	{
		super(messageChannel, dataExchangeServerChannel, messageSource, locale);
	}

	@Override
	public void onSubmitSuccess(SubDataExchange subDataExchange)
	{
		sendMessage(new SubSubmitSuccess(subDataExchange.getId()));
	}

	@Override
	public void onSubmitFail(SubDataExchange subDataExchange, SubmitFailException exception)
	{
		sendMessage(new SubSubmitFail(subDataExchange.getId()));
	}

	@Override
	public void onCancel(SubDataExchange subDataExchange, CancelReason reason)
	{
		sendMessage(new SubCancelSuccess(subDataExchange.getId()));
	}

	/**
	 * 子数据交换提交成功消息。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class SubSubmitSuccess extends SubDataExchangeMessage
	{
		public static final int ORDER = 0;

		public SubSubmitSuccess()
		{
			super();
		}

		public SubSubmitSuccess(String subDataExchangeId)
		{
			super(subDataExchangeId, 0);
		}
	}

	/**
	 * 子数据交换提交失败消息。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class SubSubmitFail extends SubDataExchangeMessage
	{
		public static final int ORDER = 0;

		public SubSubmitFail()
		{
			super();
		}

		public SubSubmitFail(String subDataExchangeId)
		{
			super(subDataExchangeId, ORDER);
		}
	}

	/**
	 * 子数据交换取消成功消息。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class SubCancelSuccess extends SubDataExchangeMessage
	{
		public static final int ORDER = SubSubmitSuccess.ORDER + 99;

		public SubCancelSuccess()
		{
			super();
		}

		public SubCancelSuccess(String subDataExchangeId)
		{
			super(subDataExchangeId, ORDER);
		}
	}
}

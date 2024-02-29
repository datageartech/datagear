/*
 * Copyright 2018-2024 datagear.tech
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

import org.datagear.dataexchange.BatchDataExchangeListener;
import org.datagear.dataexchange.SubDataExchange;
import org.datagear.web.util.msg.Message;

/**
 * 批量数据交换的子数据交换消息。
 * 
 * @author datagear@163.com
 *
 */
public class SubDataExchangeMessage extends Message
{
	private static final long serialVersionUID = 1L;

	/** 子数据交换标识 */
	private String subDataExchangeId;

	/**
	 * 消息次序，用于定义消息的逻辑次序。
	 * <p>
	 * 批量数据交换和子数据交换的监听器并不在同一个线程中，部分回调并不能保证时序，导致消息也可能时序错乱，
	 * 比如{@linkplain BatchDataExchangeListener#onSubmitSuccess(SubDataExchange)}和子数据交换的监听回调，
	 * 所以这里添加此字段，用于标识消息的逻辑次序。
	 * </p>
	 */
	private int order;

	public SubDataExchangeMessage()
	{
		super();
	}

	public SubDataExchangeMessage(String subDataExchangeId, int order)
	{
		super();
		this.subDataExchangeId = subDataExchangeId;
		this.order = order;
	}

	public String getSubDataExchangeId()
	{
		return subDataExchangeId;
	}

	public void setSubDataExchangeId(String subDataExchangeId)
	{
		this.subDataExchangeId = subDataExchangeId;
	}

	public int getOrder()
	{
		return order;
	}

	public void setOrder(int order)
	{
		this.order = order;
	}

}

/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.web.cometd.dataexchange;

import org.datagear.dataexchange.BatchDataExchangeListener;
import org.datagear.dataexchange.SubDataExchange;

/**
 * 批量数据交换的子数据交换消息。
 * 
 * @author datagear@163.com
 *
 */
public class SubDataExchangeMessage extends DataExchangeMessage
{
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

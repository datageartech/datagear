/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.web.cometd.dataexchange;

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

/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.web.dataexchange;

/**
 * 数据交换消息。
 * 
 * @author datagear@163.com
 *
 */
public abstract class DataExchangeMessage
{
	private String type;

	public DataExchangeMessage()
	{
		super();
		this.type = getClass().getSimpleName();
	}

	public String getType()
	{
		return type;
	}

	protected void setType(String type)
	{
		this.type = type;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [type=" + type + "]";
	}
}

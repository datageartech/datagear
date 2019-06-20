/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.web.cometd;

/**
 * Cometd消息。
 * 
 * @author datagear@163.com
 *
 */
public class Message
{
	private String type;

	public Message()
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

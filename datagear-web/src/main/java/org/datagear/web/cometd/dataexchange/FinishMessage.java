/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.web.cometd.dataexchange;

/**
 * 数据交换完成消息。
 * 
 * @author datagear@163.com
 *
 */
public class FinishMessage extends DataExchangeMessage
{
	private long duration;

	public FinishMessage()
	{
		super();
	}

	public FinishMessage(long duration)
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

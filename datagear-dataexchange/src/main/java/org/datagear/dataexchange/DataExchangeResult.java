/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

/**
 * 数据交换结果。
 * 
 * @author datagear@163.com
 *
 */
public abstract class DataExchangeResult
{
	/** 耗时毫秒数 */
	private long duration;

	public DataExchangeResult()
	{
		super();
	}

	public DataExchangeResult(long duration)
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

/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

/**
 * 导出结果。
 * 
 * @author datagear@163.com
 *
 */
public class ExportResult
{
	/** 导出耗时毫秒数 */
	private long duration;

	public ExportResult()
	{
		super();
	}

	public ExportResult(long duration)
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

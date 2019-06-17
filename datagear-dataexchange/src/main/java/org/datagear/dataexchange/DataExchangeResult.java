/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

import java.util.List;

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

	/**
	 * 获取最大耗时。
	 * 
	 * @param dataImportResults
	 * @return
	 */
	public long getMaxDuration(List<? extends DataExchangeResult> dataExchangeResults)
	{
		if (dataExchangeResults == null)
			return 0;

		long duration = 0;

		for (int i = 0, len = dataExchangeResults.size(); i < len; i++)
		{
			long myDuration = dataExchangeResults.get(i).getDuration();

			if (duration < myDuration)
				duration = myDuration;
		}

		return duration;
	}
}

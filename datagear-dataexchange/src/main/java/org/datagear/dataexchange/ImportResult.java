/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

import java.io.Serializable;

/**
 * 导入结果。
 * 
 * @author datagear@163.com
 *
 */
public class ImportResult
{
	/** 导入耗时毫秒数 */
	private long duration;

	public ImportResult()
	{
		super();
	}

	public ImportResult(long duration)
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
	 * 导入失败信息。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class FailInfo implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private int index;

		private String reason;

		public FailInfo()
		{
			super();
		}

		public FailInfo(int index, String reason)
		{
			super();
			this.index = index;
			this.reason = reason;
		}

		public int getIndex()
		{
			return index;
		}

		public void setIndex(int index)
		{
			this.index = index;
		}

		public String getReason()
		{
			return reason;
		}

		public void setReason(String reason)
		{
			this.reason = reason;
		}

		@Override
		public String toString()
		{
			return getClass().getSimpleName() + " [index=" + index + ", reason=" + reason + "]";
		}
	}
}

/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

import java.io.Serializable;
import java.util.List;

/**
 * 导入结果。
 * 
 * @author datagear@163.com
 *
 */
public class ImportResult
{
	private int total;

	private int successCount;

	private int failCount;

	private int undealtCount;

	private List<FailInfo> failInfos;

	public ImportResult()
	{
		super();
	}

	public ImportResult(int total, int successCount, int failCount, int undealtCount, List<FailInfo> failInfos)
	{
		super();
		this.total = total;
		this.successCount = successCount;
		this.failCount = failCount;
		this.undealtCount = undealtCount;
		this.failInfos = failInfos;
	}

	public int getTotal()
	{
		return total;
	}

	public void setTotal(int total)
	{
		this.total = total;
	}

	public int getSuccessCount()
	{
		return successCount;
	}

	public void setSuccessCount(int successCount)
	{
		this.successCount = successCount;
	}

	public int getFailCount()
	{
		return failCount;
	}

	public void setFailCount(int failCount)
	{
		this.failCount = failCount;
	}

	public int getUndealtCount()
	{
		return undealtCount;
	}

	public void setUndealtCount(int undealtCount)
	{
		this.undealtCount = undealtCount;
	}

	public List<FailInfo> getFailInfos()
	{
		return failInfos;
	}

	public void setFailInfos(List<FailInfo> failInfos)
	{
		this.failInfos = failInfos;
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

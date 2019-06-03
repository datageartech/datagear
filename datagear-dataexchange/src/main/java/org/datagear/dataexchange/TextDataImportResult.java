/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

/**
 * 文本数据导入结果。
 * 
 * @author datagear@163.com
 *
 */
public class TextDataImportResult extends DataExchangeResult
{
	/** 导入成功记录数 */
	private int successCount;

	/** 导入失败记录数 */
	private int failCount;

	public TextDataImportResult()
	{
		super();
	}

	public TextDataImportResult(long duration, int successCount, int failCount)
	{
		super(duration);
		this.successCount = successCount;
		this.failCount = failCount;
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
}

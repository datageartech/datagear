/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.web.controller;

/**
 * 有重复记录异常。
 * 
 * @author datagear@163.com
 *
 */
public class DuplicateRecordException extends ControllerException
{
	private static final long serialVersionUID = 1L;

	/** 期望数目 */
	private int expectedCount;

	/** 实际数目 */
	private int actualCount;

	public DuplicateRecordException(int expectedCount, int actualCount)
	{
		super();
		this.expectedCount = expectedCount;
		this.actualCount = actualCount;
	}

	public int getExpectedCount()
	{
		return expectedCount;
	}

	protected void setExpectedCount(int expectedCount)
	{
		this.expectedCount = expectedCount;
	}

	public int getActualCount()
	{
		return actualCount;
	}

	protected void setActualCount(int actualCount)
	{
		this.actualCount = actualCount;
	}

}

/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

import javax.sql.DataSource;

/**
 * 数据交换。
 * 
 * @author datagear@163.com
 *
 */
public abstract class DataExchange
{
	/** 数据交换数据源 */
	private DataSource dataSource;

	/** 出错时是否终止 */
	private boolean abortOnError;

	public DataExchange()
	{
		super();
	}

	public DataExchange(DataSource dataSource, boolean abortOnError)
	{
		super();
		this.dataSource = dataSource;
		this.abortOnError = abortOnError;
	}

	public DataSource getDataSource()
	{
		return dataSource;
	}

	public void setDataSource(DataSource dataSource)
	{
		this.dataSource = dataSource;
	}

	public boolean isAbortOnError()
	{
		return abortOnError;
	}

	public void setAbortOnError(boolean abortOnError)
	{
		this.abortOnError = abortOnError;
	}
}

/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange.support;

import javax.sql.DataSource;

/**
 * 查询{@linkplain TextDataExport}。
 * 
 * @author datagear@163.com
 *
 */
public class QueryTextDataExport extends TextDataExport
{
	/** 查询 */
	private Query query;

	public QueryTextDataExport()
	{
		super();
	}

	public QueryTextDataExport(DataSource dataSource, boolean abortOnError, DataFormat dataFormat, Query query)
	{
		super(dataSource, abortOnError, dataFormat);
		this.query = query;
	}

	public Query getQuery()
	{
		return query;
	}

	public void setQuery(Query query)
	{
		this.query = query;
	}
}

/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

/**
 * 查询导出{@linkplain TextDataExchange}。
 * 
 * @author datagear@163.com
 *
 */
public abstract class QueryTextDataExport extends TextDataExport
{
	/** 查询 */
	private Query query;

	public QueryTextDataExport()
	{
		super();
	}

	public QueryTextDataExport(ConnectionFactory connectionFactory, DataFormat dataFormat,
			boolean nullForUnsupportedColumn, Query query)
	{
		super(connectionFactory, dataFormat, nullForUnsupportedColumn);
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

/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

import org.datagear.util.resource.ConnectionFactory;

/**
 * 查询文本导出。
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
			TextDataExportOption exportOption, Query query)
	{
		super(connectionFactory, dataFormat, exportOption);
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

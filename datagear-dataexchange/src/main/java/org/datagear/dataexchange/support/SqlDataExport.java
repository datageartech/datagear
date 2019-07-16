/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange.support;

import java.io.Writer;

import org.datagear.dataexchange.ConnectionFactory;
import org.datagear.dataexchange.DataFormat;
import org.datagear.dataexchange.Query;
import org.datagear.dataexchange.QueryTextDataExport;
import org.datagear.dataexchange.ResourceFactory;
import org.datagear.dataexchange.TextDataExportOption;

/**
 * SQL导出。
 * 
 * @author datagear@163.com
 *
 */
public class SqlDataExport extends QueryTextDataExport
{
	private String tableName;
	private ResourceFactory<Writer> writerFactory;

	public SqlDataExport()
	{
		super();
	}

	public SqlDataExport(ConnectionFactory connectionFactory, DataFormat dataFormat, TextDataExportOption exportOption,
			Query query, String tableName, ResourceFactory<Writer> writerFactory)
	{
		super(connectionFactory, dataFormat, exportOption, query);
		this.tableName = tableName;
		this.writerFactory = writerFactory;
	}

	public String getTableName()
	{
		return tableName;
	}

	public void setTableName(String tableName)
	{
		this.tableName = tableName;
	}

	public ResourceFactory<Writer> getWriterFactory()
	{
		return writerFactory;
	}

	public void setWriterFactory(ResourceFactory<Writer> writerFactory)
	{
		this.writerFactory = writerFactory;
	}
}

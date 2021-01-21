/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.dataexchange.support;

import java.io.Writer;

import org.datagear.dataexchange.DataFormat;
import org.datagear.dataexchange.Query;
import org.datagear.dataexchange.QueryTextDataExport;
import org.datagear.dataexchange.TextDataExportOption;
import org.datagear.util.resource.ConnectionFactory;
import org.datagear.util.resource.ResourceFactory;

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

	public SqlDataExport(ConnectionFactory connectionFactory, DataFormat dataFormat, SqlDataExportOption exportOption,
			Query query, String tableName, ResourceFactory<Writer> writerFactory)
	{
		super(connectionFactory, dataFormat, exportOption, query);
		this.tableName = tableName;
		this.writerFactory = writerFactory;
	}

	@Override
	public SqlDataExportOption getExportOption()
	{
		return (SqlDataExportOption) super.getExportOption();
	}

	@Override
	public void setExportOption(TextDataExportOption exportOption)
	{
		if (!(exportOption instanceof SqlDataExportOption))
			throw new IllegalArgumentException();

		super.setExportOption(exportOption);
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

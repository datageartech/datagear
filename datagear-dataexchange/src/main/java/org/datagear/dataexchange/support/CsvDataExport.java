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
 * CSV导出。
 * 
 * @author datagear@163.com
 *
 */
public class CsvDataExport extends QueryTextDataExport
{
	private ResourceFactory<Writer> writerFactory;

	public CsvDataExport()
	{
		super();
	}

	public CsvDataExport(ConnectionFactory connectionFactory, DataFormat dataFormat, TextDataExportOption exportOption,
			Query query, ResourceFactory<Writer> writerFactory)
	{
		super(connectionFactory, dataFormat, exportOption, query);
		this.writerFactory = writerFactory;
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

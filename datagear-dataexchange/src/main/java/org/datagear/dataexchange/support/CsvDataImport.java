/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange.support;

import java.io.Reader;

import org.datagear.dataexchange.DataFormat;
import org.datagear.dataexchange.TableTextValueDataImport;
import org.datagear.dataexchange.ValueDataImportOption;
import org.datagear.util.resource.ConnectionFactory;
import org.datagear.util.resource.ResourceFactory;

/**
 * CSV导入。
 * 
 * @author datagear@163.com
 *
 */
public class CsvDataImport extends TableTextValueDataImport
{
	private ResourceFactory<Reader> readerFactory;

	public CsvDataImport()
	{
		super();
	}

	public CsvDataImport(ConnectionFactory connectionFactory, DataFormat dataFormat,
			ValueDataImportOption importOption, String table, ResourceFactory<Reader> readerFactory)
	{
		super(connectionFactory, dataFormat, importOption, table);
		this.readerFactory = readerFactory;
	}

	public ResourceFactory<Reader> getReaderFactory()
	{
		return readerFactory;
	}

	public void setReaderFactory(ResourceFactory<Reader> readerFactory)
	{
		this.readerFactory = readerFactory;
	}
}

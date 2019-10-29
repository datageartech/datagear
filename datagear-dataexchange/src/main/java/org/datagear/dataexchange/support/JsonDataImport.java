/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange.support;

import java.io.Reader;

import org.datagear.dataexchange.ConnectionFactory;
import org.datagear.dataexchange.DataFormat;
import org.datagear.dataexchange.ResourceFactory;
import org.datagear.dataexchange.TableTextValueDataImport;
import org.datagear.dataexchange.ValueDataImportOption;

/**
 * JSON导入。
 * 
 * @author datagear@163.com
 *
 */
public class JsonDataImport extends TableTextValueDataImport
{
	private ResourceFactory<Reader> readerFactory;

	public JsonDataImport()
	{
		super();
	}

	public JsonDataImport(ConnectionFactory connectionFactory, DataFormat dataFormat, JsonDataImportOption importOption,
			String table, ResourceFactory<Reader> readerFactory)
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

	@Override
	public JsonDataImportOption getImportOption()
	{
		return (JsonDataImportOption) super.getImportOption();
	}

	@Override
	public void setImportOption(ValueDataImportOption importOption)
	{
		if (!(importOption instanceof JsonDataImportOption))
			throw new IllegalArgumentException();

		super.setImportOption(importOption);
	}
}

/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange.support;

import java.io.Reader;

import org.datagear.dataexchange.DataExchange;
import org.datagear.dataexchange.DataImportListener;
import org.datagear.dataexchange.DataImportOption;
import org.datagear.util.resource.ConnectionFactory;
import org.datagear.util.resource.ResourceFactory;

/**
 * SQL数据导入。
 * 
 * @author datagear@163.com
 *
 */
public class SqlDataImport extends DataExchange
{
	private DataImportOption importOption;

	private ResourceFactory<Reader> readerFactory;

	private DataImportListener listener;

	public SqlDataImport()
	{
		super();
	}

	public SqlDataImport(ConnectionFactory connectionFactory, DataImportOption importOption,
			ResourceFactory<Reader> readerFactory)
	{
		super(connectionFactory);
		this.importOption = importOption;
		this.readerFactory = readerFactory;
	}

	public DataImportOption getImportOption()
	{
		return importOption;
	}

	public void setImportOption(DataImportOption importOption)
	{
		this.importOption = importOption;
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
	public DataImportListener getListener()
	{
		return listener;
	}

	public void setListener(DataImportListener listener)
	{
		this.listener = listener;
	}
}

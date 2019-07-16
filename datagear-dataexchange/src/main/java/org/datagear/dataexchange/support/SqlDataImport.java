/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange.support;

import java.io.Reader;

import org.datagear.dataexchange.ConnectionFactory;
import org.datagear.dataexchange.DataExchange;
import org.datagear.dataexchange.ResourceFactory;

/**
 * SQL数据导入。
 * 
 * @author datagear@163.com
 *
 */
public class SqlDataImport extends DataExchange
{
	private SqlDataImportOption importOption;

	private ResourceFactory<Reader> readerFactory;

	private SqlDataImportListener listener;

	public SqlDataImport()
	{
		super();
	}

	public SqlDataImport(ConnectionFactory connectionFactory, SqlDataImportOption importOption,
			ResourceFactory<Reader> readerFactory)
	{
		super(connectionFactory);
		this.importOption = importOption;
		this.readerFactory = readerFactory;
	}

	public SqlDataImportOption getImportOption()
	{
		return importOption;
	}

	public void setImportOption(SqlDataImportOption importOption)
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
	public SqlDataImportListener getListener()
	{
		return listener;
	}

	public void setListener(SqlDataImportListener listener)
	{
		this.listener = listener;
	}
}

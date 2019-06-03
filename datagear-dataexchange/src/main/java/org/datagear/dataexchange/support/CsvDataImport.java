/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange.support;

import java.io.Reader;

import org.datagear.dataexchange.ConnectionFactory;
import org.datagear.dataexchange.DataImport;

/**
 * CSV {@linkplain DataImport}。
 * 
 * @author datagear@163.com
 *
 */
public class CsvDataImport extends TableTextDataImport
{
	/** CSV输入流 */
	private Reader reader;

	public CsvDataImport()
	{
		super();
	}

	public CsvDataImport(ConnectionFactory connectionFactory, boolean abortOnError, DataFormat dataFormat, String table,
			boolean ignoreInexistentColumn, Reader reader)
	{
		super(connectionFactory, abortOnError, dataFormat, table, ignoreInexistentColumn);
		this.reader = reader;
	}

	public Reader getReader()
	{
		return reader;
	}

	public void setReader(Reader reader)
	{
		this.reader = reader;
	}
}

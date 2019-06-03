/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange.support;

import java.io.Writer;

import org.datagear.dataexchange.ConnectionFactory;
import org.datagear.dataexchange.DataExport;

/**
 * CSV {@linkplain DataExport}。
 * 
 * @author datagear@163.com
 *
 */
public class CsvDataExport extends QueryTextDataExport
{
	private Writer writer;

	public CsvDataExport()
	{
		super();
	}

	public CsvDataExport(ConnectionFactory connectionFactory, boolean abortOnError, DataFormat dataFormat, Query query,
			Writer writer)
	{
		super(connectionFactory, abortOnError, dataFormat, query);
		this.writer = writer;
	}

	public Writer getWriter()
	{
		return writer;
	}

	public void setWriter(Writer writer)
	{
		this.writer = writer;
	}
}

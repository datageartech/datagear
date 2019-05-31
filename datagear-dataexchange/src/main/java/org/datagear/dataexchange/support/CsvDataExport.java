/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange.support;

import java.io.Writer;

import javax.sql.DataSource;

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

	public CsvDataExport(DataSource dataSource, boolean abortOnError, DataFormat dataFormat, Query query, Writer writer)
	{
		super(dataSource, abortOnError, dataFormat, query);
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

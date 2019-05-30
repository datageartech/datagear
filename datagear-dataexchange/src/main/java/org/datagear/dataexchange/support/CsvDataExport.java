/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange.support;

import java.io.Writer;
import java.sql.Connection;
import java.sql.ResultSet;

import org.datagear.dataexchange.DataExport;

/**
 * CSV {@linkplain DataExport}。
 * 
 * @author datagear@163.com
 *
 */
public class CsvDataExport extends AbstractTextDataExport
{
	public CsvDataExport()
	{
		super();
	}

	public CsvDataExport(Connection connection, boolean abortOnError, ResultSet resultSet, Writer writer,
			DataFormat dataFormat)
	{
		super(connection, abortOnError, resultSet, writer, dataFormat);
	}
}

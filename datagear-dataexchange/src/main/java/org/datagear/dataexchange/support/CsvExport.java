/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange.support;

import java.io.Writer;
import java.sql.Connection;
import java.sql.ResultSet;

import org.datagear.dataexchange.Export;

/**
 * CSV {@linkplain Export}。
 * 
 * @author datagear@163.com
 *
 */
public class CsvExport extends AbstractTextExport
{
	public CsvExport()
	{
		super();
	}

	public CsvExport(Connection connection, ResultSet resultSet, Writer writer, DataFormat dataFormat)
	{
		super(connection, resultSet, writer, dataFormat);
	}
}

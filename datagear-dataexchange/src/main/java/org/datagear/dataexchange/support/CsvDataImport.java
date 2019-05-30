/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange.support;

import java.io.Reader;
import java.sql.Connection;

import org.datagear.dataexchange.DataImport;

/**
 * CSV {@linkplain DataImport}。
 * 
 * @author datagear@163.com
 *
 */
public class CsvDataImport extends AbstractTextDataImport
{
	/** 要导入的表名 */
	private String table;

	public CsvDataImport()
	{
		super();
	}

	public CsvDataImport(Connection connection, boolean abortOnError, Reader reader, DataFormat dataFormat,
			boolean ignoreInexistentColumn, String table)
	{
		super(connection, abortOnError, reader, dataFormat, ignoreInexistentColumn);
		this.table = table;
	}

	public String getTable()
	{
		return table;
	}

	public void setTable(String table)
	{
		this.table = table;
	}
}

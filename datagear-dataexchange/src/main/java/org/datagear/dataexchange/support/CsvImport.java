/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange.support;

import java.io.Reader;
import java.sql.Connection;

import org.datagear.dataexchange.ImportReporter;

/**
 * CSV导入。
 * 
 * @author datagear@163.com
 *
 */
public class CsvImport extends AbstractTextImport
{
	/** 要导入的表名 */
	private String table;

	public CsvImport()
	{
		super();
	}

	public CsvImport(Connection connection, boolean abortOnError, ImportReporter importReporter, Reader reader,
			DataFormat dataFormat, boolean ignoreInexistentColumn, String table)
	{
		super(connection, abortOnError, importReporter, reader, dataFormat, ignoreInexistentColumn);
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

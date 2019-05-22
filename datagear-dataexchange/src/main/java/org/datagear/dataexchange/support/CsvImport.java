/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange.support;

import java.io.Reader;
import java.sql.Connection;

import org.datagear.dataexchange.Import;

/**
 * CSV导入。
 * 
 * @author datagear@163.com
 *
 */
public class CsvImport extends Import
{
	/** 要导入的表名 */
	private String table;

	/** CSV输入流 */
	private Reader reader;

	public CsvImport()
	{
		super();
	}

	public CsvImport(Connection connection, boolean abortOnError, String table, Reader reader)
	{
		super(connection, abortOnError);
	}

	public String getTable()
	{
		return table;
	}

	public void setTable(String table)
	{
		this.table = table;
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

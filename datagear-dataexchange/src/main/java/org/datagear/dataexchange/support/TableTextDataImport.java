/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange.support;

import javax.sql.DataSource;

/**
 * 单表{@linkplain TextDataImport}。
 * 
 * @author datagear@163.com
 *
 */
public abstract class TableTextDataImport extends TextDataImport
{
	/** 要导入的表名 */
	private String table;

	/** 是否忽略不存在的列 */
	private boolean ignoreInexistentColumn;

	public TableTextDataImport()
	{
		super();
	}

	public TableTextDataImport(DataSource dataSource, boolean abortOnError, DataFormat dataFormat, String table,
			boolean ignoreInexistentColumn)
	{
		super(dataSource, abortOnError, dataFormat);
		this.table = table;
		this.ignoreInexistentColumn = ignoreInexistentColumn;
	}

	public String getTable()
	{
		return table;
	}

	public void setTable(String table)
	{
		this.table = table;
	}

	public boolean isIgnoreInexistentColumn()
	{
		return ignoreInexistentColumn;
	}

	public void setIgnoreInexistentColumn(boolean ignoreInexistentColumn)
	{
		this.ignoreInexistentColumn = ignoreInexistentColumn;
	}
}

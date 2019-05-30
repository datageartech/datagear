/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange.support;

import java.io.Reader;
import java.sql.Connection;

import org.datagear.dataexchange.DataImport;

/**
 * 抽象文本{@linkplain DataImport}。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractTextDataImport extends DataImport
{
	/** 文本输入流 */
	private Reader reader;

	/** 文本数据格式 */
	private DataFormat dataFormat;

	/** 是否忽略不存在的列 */
	private boolean ignoreInexistentColumn;

	public AbstractTextDataImport()
	{
		super();
	}

	public AbstractTextDataImport(Connection connection, boolean abortOnError, Reader reader, DataFormat dataFormat,
			boolean ignoreInexistentColumn)
	{
		super(connection, abortOnError);
		this.reader = reader;
		this.dataFormat = dataFormat;
		this.ignoreInexistentColumn = ignoreInexistentColumn;
	}

	public Reader getReader()
	{
		return reader;
	}

	public void setReader(Reader reader)
	{
		this.reader = reader;
	}

	public DataFormat getDataFormat()
	{
		return dataFormat;
	}

	public void setDataFormat(DataFormat dataFormat)
	{
		this.dataFormat = dataFormat;
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

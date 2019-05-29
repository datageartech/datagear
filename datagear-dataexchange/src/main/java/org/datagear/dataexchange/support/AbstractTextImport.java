/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange.support;

import java.io.Reader;
import java.sql.Connection;

import org.datagear.dataexchange.Import;
import org.datagear.dataexchange.ImportReporter;

/**
 * 抽象文本{@linkplain Import}。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractTextImport extends Import
{
	/** 文本输入流 */
	private Reader reader;

	/** 文本数据格式 */
	private DataFormat dataFormat;

	/** 是否忽略不存在的列 */
	private boolean ignoreInexistentColumn;

	public AbstractTextImport()
	{
		super();
	}

	public AbstractTextImport(Connection connection, boolean abortOnError, ImportReporter importReporter, Reader reader,
			DataFormat dataFormat, boolean ignoreInexistentColumn)
	{
		super(connection, abortOnError, importReporter);
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

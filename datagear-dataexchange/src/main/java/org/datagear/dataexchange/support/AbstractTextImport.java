/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange.support;

import java.io.Reader;
import java.sql.Connection;

import org.datagear.dataexchange.Import;

/**
 * 抽象文本数据导入。
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

	public AbstractTextImport()
	{
		super();
	}

	public AbstractTextImport(Connection connection, boolean abortOnError, Reader reader, DataFormat dataFormat)
	{
		super(connection, abortOnError);
		this.reader = reader;
		this.dataFormat = dataFormat;
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
}

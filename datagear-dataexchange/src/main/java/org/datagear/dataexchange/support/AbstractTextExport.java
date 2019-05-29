/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange.support;

import java.io.Writer;
import java.sql.Connection;
import java.sql.ResultSet;

import org.datagear.dataexchange.Export;

/**
 * 抽象文本{@linkplain Export}。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractTextExport extends Export
{
	/** 文本输出流 */
	private Writer writer;

	/** 文本数据格式 */
	private DataFormat dataFormat;

	public AbstractTextExport()
	{
		super();
	}

	public AbstractTextExport(Connection connection, ResultSet resultSet, Writer writer, DataFormat dataFormat)
	{
		super(connection, resultSet);
		this.writer = writer;
		this.dataFormat = dataFormat;
	}

	public Writer getWriter()
	{
		return writer;
	}

	public void setWriter(Writer writer)
	{
		this.writer = writer;
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

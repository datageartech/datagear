/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange.support;

import java.io.Writer;
import java.sql.ResultSet;

import javax.sql.DataSource;

import org.datagear.dataexchange.DataExport;

/**
 * 抽象文本{@linkplain DataExport}。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractTextDataExport extends DataExport
{
	/** 文本输出流 */
	private Writer writer;

	/** 文本数据格式 */
	private DataFormat dataFormat;

	public AbstractTextDataExport()
	{
		super();
	}

	public AbstractTextDataExport(DataSource dataSource, boolean abortOnError, ResultSet resultSet, Writer writer,
			DataFormat dataFormat)
	{
		super(dataSource, abortOnError, resultSet);
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

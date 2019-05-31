/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange.support;

import javax.sql.DataSource;

import org.datagear.dataexchange.DataExport;

/**
 * 文本{@linkplain DataExport}。
 * 
 * @author datagear@163.com
 *
 */
public abstract class TextDataExport extends DataExport
{
	/** 文本数据格式 */
	private DataFormat dataFormat;

	public TextDataExport()
	{
		super();
	}

	public TextDataExport(DataSource dataSource, boolean abortOnError, DataFormat dataFormat)
	{
		super(dataSource, abortOnError);
		this.dataFormat = dataFormat;
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

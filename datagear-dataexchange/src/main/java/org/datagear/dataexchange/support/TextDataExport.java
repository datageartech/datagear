/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange.support;

import org.datagear.dataexchange.ConnectionFactory;
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

	public TextDataExport(ConnectionFactory connectionFactory, boolean abortOnError, DataFormat dataFormat)
	{
		super(connectionFactory, abortOnError);
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

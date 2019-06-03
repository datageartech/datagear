/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange.support;

import org.datagear.dataexchange.ConnectionFactory;
import org.datagear.dataexchange.DataImport;

/**
 * 文本{@linkplain DataImport}。
 * 
 * @author datagear@163.com
 *
 */
public abstract class TextDataImport extends DataImport
{
	/** 文本数据格式 */
	private DataFormat dataFormat;

	public TextDataImport()
	{
		super();
	}

	public TextDataImport(ConnectionFactory connectionFactory, boolean abortOnError, DataFormat dataFormat)
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

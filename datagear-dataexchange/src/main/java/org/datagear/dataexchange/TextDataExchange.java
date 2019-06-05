/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

/**
 * 文本数据交换。
 * 
 * @author datagear@163.com
 *
 */
public abstract class TextDataExchange extends DataExchange
{
	/** 文本数据格式 */
	private DataFormat dataFormat;

	public TextDataExchange()
	{
		super();
	}

	public TextDataExchange(ConnectionFactory connectionFactory, DataFormat dataFormat)
	{
		super(connectionFactory);
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

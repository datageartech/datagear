/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

import org.datagear.util.resource.ConnectionFactory;

/**
 * 格式化数据交换。
 * 
 * @author datagear@163.com
 *
 */
public abstract class FormatDataExchange extends DataExchange
{
	/** 数据格式 */
	private DataFormat dataFormat;

	public FormatDataExchange()
	{
		super();
	}

	public FormatDataExchange(ConnectionFactory connectionFactory, DataFormat dataFormat)
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

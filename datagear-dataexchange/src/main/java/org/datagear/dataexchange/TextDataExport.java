/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

/**
 * 文本导出。
 * 
 * @author datagear@163.com
 *
 */
public abstract class TextDataExport extends TextDataExchange
{
	/** 对于不支持的列设置为null */
	private boolean nullForUnsupportedColumn = true;

	public TextDataExport()
	{
		super();
	}

	public TextDataExport(ConnectionFactory connectionFactory, DataFormat dataFormat, boolean nullForUnsupportedColumn)
	{
		super(connectionFactory, dataFormat);
		this.nullForUnsupportedColumn = nullForUnsupportedColumn;
	}

	public boolean isNullForUnsupportedColumn()
	{
		return nullForUnsupportedColumn;
	}

	public void setNullForUnsupportedColumn(boolean nullForUnsupportedColumn)
	{
		this.nullForUnsupportedColumn = nullForUnsupportedColumn;
	}

	@Override
	public abstract DataExchangeListener getListener();
}

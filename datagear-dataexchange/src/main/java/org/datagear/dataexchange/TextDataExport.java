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
	public TextDataExport()
	{
		super();
	}

	public TextDataExport(ConnectionFactory connectionFactory, DataFormat dataFormat)
	{
		super(connectionFactory, dataFormat);
	}

	/**
	 * 获取{@linkplain TextDataExportOption}。
	 * 
	 * @return
	 */
	public abstract TextDataExportOption getExportOption();

	@Override
	public abstract TextDataExportListener getListener();
}

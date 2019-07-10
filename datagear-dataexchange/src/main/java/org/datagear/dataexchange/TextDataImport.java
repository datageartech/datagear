/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

/**
 * 文本导入。
 * 
 * @author datagear@163.com
 *
 */
public abstract class TextDataImport extends TextDataExchange
{
	public TextDataImport()
	{
		super();
	}

	public TextDataImport(ConnectionFactory connectionFactory, DataFormat dataFormat)
	{
		super(connectionFactory, dataFormat);
	}

	/**
	 * 获取{@linkplain TextDataImportOption}。
	 * 
	 * @return
	 */
	public abstract TextDataImportOption getImportOption();

	@Override
	public abstract TextDataImportListener getListener();
}

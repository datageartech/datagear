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
	private TextDataExportOption exportOption;

	public TextDataExport()
	{
		super();
	}

	public TextDataExport(ConnectionFactory connectionFactory, DataFormat dataFormat, TextDataExportOption exportOption)
	{
		super(connectionFactory, dataFormat);
		this.exportOption = exportOption;
	}

	public TextDataExportOption getExportOption()
	{
		return exportOption;
	}

	public void setExportOption(TextDataExportOption exportOption)
	{
		this.exportOption = exportOption;
	}

	@Override
	public abstract DataExchangeListener getListener();
}

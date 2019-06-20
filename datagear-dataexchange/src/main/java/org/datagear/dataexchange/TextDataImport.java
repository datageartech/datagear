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
	private TextDataImportOption importOption;

	public TextDataImport()
	{
		super();
	}

	public TextDataImport(ConnectionFactory connectionFactory, DataFormat dataFormat, TextDataImportOption importOption)
	{
		super(connectionFactory, dataFormat);
		this.importOption = importOption;
	}

	public TextDataImportOption getImportOption()
	{
		return importOption;
	}

	public void setImportOption(TextDataImportOption importOption)
	{
		this.importOption = importOption;
	}

	@Override
	public abstract TextDataImportListener getListener();
}

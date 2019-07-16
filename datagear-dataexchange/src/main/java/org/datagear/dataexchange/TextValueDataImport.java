/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

/**
 * 文本值导入。
 * <p>
 * 导入数据源为字段名称-文本字段值集合，比如：CSV、JSON、EXCEL等。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public abstract class TextValueDataImport extends FormatDataExchange
{
	private TextValueDataImportOption importOption;

	private TextValueDataImportListener listener;

	public TextValueDataImport()
	{
		super();
	}

	public TextValueDataImport(ConnectionFactory connectionFactory, DataFormat dataFormat,
			TextValueDataImportOption importOption)
	{
		super(connectionFactory, dataFormat);
		this.importOption = importOption;
	}

	public TextValueDataImportOption getImportOption()
	{
		return importOption;
	}

	public void setImportOption(TextValueDataImportOption importOption)
	{
		this.importOption = importOption;
	}

	public void setListener(TextValueDataImportListener listener)
	{
		this.listener = listener;
	}

	@Override
	public TextValueDataImportListener getListener()
	{
		return this.listener;
	}
}

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
	private ValueDataImportOption importOption;

	private ValueDataImportListener listener;

	public TextValueDataImport()
	{
		super();
	}

	public TextValueDataImport(ConnectionFactory connectionFactory, DataFormat dataFormat,
			ValueDataImportOption importOption)
	{
		super(connectionFactory, dataFormat);
		this.importOption = importOption;
	}

	public ValueDataImportOption getImportOption()
	{
		return importOption;
	}

	public void setImportOption(ValueDataImportOption importOption)
	{
		this.importOption = importOption;
	}

	public void setListener(ValueDataImportListener listener)
	{
		this.listener = listener;
	}

	@Override
	public ValueDataImportListener getListener()
	{
		return this.listener;
	}
}

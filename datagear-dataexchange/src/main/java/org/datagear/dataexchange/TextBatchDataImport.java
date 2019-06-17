/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

/**
 * 文本批量导入。
 * 
 * @author datagear@163.com
 *
 */
public abstract class TextBatchDataImport<T extends TextDataImport> extends BatchDataExchange<T>
{
	private DataFormat dataFormat;

	private TextDataImportOption importOption;

	public TextBatchDataImport()
	{
		super();
	}

	public TextBatchDataImport(ConnectionFactory connectionFactory, DataFormat dataFormat,
			TextDataImportOption importOption)
	{
		super(connectionFactory);
		this.dataFormat = dataFormat;
		this.importOption = importOption;
	}

	public DataFormat getDataFormat()
	{
		return dataFormat;
	}

	public void setDataFormat(DataFormat dataFormat)
	{
		this.dataFormat = dataFormat;
	}

	public TextDataImportOption getImportOption()
	{
		return importOption;
	}

	public void setImportOption(TextDataImportOption importOption)
	{
		this.importOption = importOption;
	}
}

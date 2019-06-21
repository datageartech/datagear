/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

/**
 * 指定索引的单条文本数据导入异常。
 * 
 * @author datagear@163.com
 *
 */
public class TextDataImportException extends DataExchangeException
{
	private static final long serialVersionUID = 1L;

	private int dataIndex;

	public TextDataImportException(int dataIndex)
	{
		super();
		this.dataIndex = dataIndex;
	}

	public TextDataImportException(int dataIndex, String message)
	{
		super(message);
		this.dataIndex = dataIndex;
	}

	public TextDataImportException(int dataIndex, Throwable cause)
	{
		super(cause);
		this.dataIndex = dataIndex;
	}

	public TextDataImportException(int dataIndex, String message, Throwable cause)
	{
		super(message, cause);
		this.dataIndex = dataIndex;
	}

	public int getDataIndex()
	{
		return dataIndex;
	}

	protected void setDataIndex(int dataIndex)
	{
		this.dataIndex = dataIndex;
	}

}

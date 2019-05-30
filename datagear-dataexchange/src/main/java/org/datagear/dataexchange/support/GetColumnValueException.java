/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange.support;

import org.datagear.dataexchange.DataExportException;

/**
 * 获取列值异常。
 * 
 * @author datagear@163.com
 *
 */
public class GetColumnValueException extends DataExportException
{
	private static final long serialVersionUID = 1L;

	private int dataIndex;

	private String columnName;

	public GetColumnValueException(int dataIndex, String columnName)
	{
		super();
		this.dataIndex = dataIndex;
		this.columnName = columnName;
	}

	public GetColumnValueException(int dataIndex, String columnName, String message)
	{
		super(message);
		this.dataIndex = dataIndex;
		this.columnName = columnName;
	}

	public GetColumnValueException(int dataIndex, String columnName, Throwable cause)
	{
		super(cause);
		this.dataIndex = dataIndex;
		this.columnName = columnName;
	}

	public GetColumnValueException(int dataIndex, String columnName, String message, Throwable cause)
	{
		super(message, cause);
		this.dataIndex = dataIndex;
		this.columnName = columnName;
	}

	public int getDataIndex()
	{
		return dataIndex;
	}

	protected void setDataIndex(int dataIndex)
	{
		this.dataIndex = dataIndex;
	}

	public String getColumnName()
	{
		return columnName;
	}

	protected void setColumnName(String columnName)
	{
		this.columnName = columnName;
	}
}

/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange.support;

import org.datagear.dataexchange.DataExchangeException;

/**
 * 导入数据时设置列值异常。
 * 
 * @author datagear@163.com
 *
 */
public class SetImportColumnValueException extends DataExchangeException
{
	private static final long serialVersionUID = 1L;

	private String table;

	private int dataIndex;

	private String columnName;

	private Object sourceValue;

	public SetImportColumnValueException(String table, int dataIndex, String columnName, Object sourceValue)
	{
		super();
		this.table = table;
		this.dataIndex = dataIndex;
		this.columnName = columnName;
		this.sourceValue = sourceValue;
	}

	public SetImportColumnValueException(String table, int dataIndex, String columnName, Object sourceValue,
			String message)
	{
		super(message);
		this.table = table;
		this.dataIndex = dataIndex;
		this.columnName = columnName;
		this.sourceValue = sourceValue;
	}

	public SetImportColumnValueException(String table, int dataIndex, String columnName, Object sourceValue,
			Throwable cause)
	{
		super(cause);
		this.table = table;
		this.dataIndex = dataIndex;
		this.columnName = columnName;
		this.sourceValue = sourceValue;
	}

	public SetImportColumnValueException(String table, int dataIndex, String columnName, Object sourceValue,
			String message, Throwable cause)
	{
		super(message, cause);
		this.table = table;
		this.dataIndex = dataIndex;
		this.columnName = columnName;
		this.sourceValue = sourceValue;
	}

	public String getTable()
	{
		return table;
	}

	protected void setTable(String table)
	{
		this.table = table;
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

	public void setColumnName(String columnName)
	{
		this.columnName = columnName;
	}

	public Object getSourceValue()
	{
		return sourceValue;
	}

	protected void setSourceValue(Object sourceValue)
	{
		this.sourceValue = sourceValue;
	}
}

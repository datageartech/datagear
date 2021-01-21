/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.dataexchange;

/**
 * 导入数据时设置列值异常。
 * 
 * @author datagear@163.com
 *
 */
public class SetImportColumnValueException extends IndexDataExchangeException
{
	private static final long serialVersionUID = 1L;

	private String columnName;

	private Object sourceValue;

	public SetImportColumnValueException(DataIndex dataIndex, String columnName, Object sourceValue)
	{
		super(dataIndex);
		this.columnName = columnName;
		this.sourceValue = sourceValue;
	}

	public SetImportColumnValueException(DataIndex dataIndex, String columnName, Object sourceValue, String message)
	{
		super(dataIndex, message);
		this.columnName = columnName;
		this.sourceValue = sourceValue;
	}

	public SetImportColumnValueException(DataIndex dataIndex, String columnName, Object sourceValue, Throwable cause)
	{
		super(dataIndex, cause);
		this.columnName = columnName;
		this.sourceValue = sourceValue;
	}

	public SetImportColumnValueException(DataIndex dataIndex, String columnName, Object sourceValue, String message,
			Throwable cause)
	{
		super(dataIndex, message, cause);
		this.columnName = columnName;
		this.sourceValue = sourceValue;
	}

	public String getColumnName()
	{
		return columnName;
	}

	protected void setColumnName(String columnName)
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

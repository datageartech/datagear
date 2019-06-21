/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange.support;

/**
 * 导入源值不合法异常。
 * 
 * @author datagear@163.com
 *
 */
public class IllegalSourceValueException extends SetImportColumnValueException
{
	private static final long serialVersionUID = 1L;

	public IllegalSourceValueException(int dataIndex, String columnName, Object sourceValue)
	{
		super(dataIndex, columnName, sourceValue);
	}

	public IllegalSourceValueException(int dataIndex, String columnName, Object sourceValue, String message)
	{
		super(dataIndex, columnName, sourceValue, message);
	}

	public IllegalSourceValueException(int dataIndex, String columnName, Object sourceValue, Throwable cause)
	{
		super(dataIndex, columnName, sourceValue, cause);
	}

	public IllegalSourceValueException(int dataIndex, String columnName, Object sourceValue, String message,
			Throwable cause)
	{
		super(dataIndex, columnName, sourceValue, message, cause);
	}
}

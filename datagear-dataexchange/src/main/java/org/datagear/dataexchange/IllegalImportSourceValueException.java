/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.dataexchange;

/**
 * 导入源值不合法异常。
 * 
 * @author datagear@163.com
 *
 */
public class IllegalImportSourceValueException extends SetImportColumnValueException
{
	private static final long serialVersionUID = 1L;

	public IllegalImportSourceValueException(DataIndex dataIndex, String columnName, Object sourceValue)
	{
		super(dataIndex, columnName, sourceValue);
	}

	public IllegalImportSourceValueException(DataIndex dataIndex, String columnName, Object sourceValue, String message)
	{
		super(dataIndex, columnName, sourceValue, message);
	}

	public IllegalImportSourceValueException(DataIndex dataIndex, String columnName, Object sourceValue, Throwable cause)
	{
		super(dataIndex, columnName, sourceValue, cause);
	}

	public IllegalImportSourceValueException(DataIndex dataIndex, String columnName, Object sourceValue, String message,
			Throwable cause)
	{
		super(dataIndex, columnName, sourceValue, message, cause);
	}
}

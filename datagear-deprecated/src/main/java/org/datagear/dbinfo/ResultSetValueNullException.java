/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.dbinfo;

import java.sql.ResultSet;

/**
 * {@linkplain ResultSet}结果集列值为{@code null}异常。
 * 
 * @author datagear@163.com
 *
 */
public class ResultSetValueNullException extends ResultSetIncompatibleException
{
	private static final long serialVersionUID = 1L;

	private String columnName;

	public ResultSetValueNullException(String columnName)
	{
		super("Column [" + columnName + "] 's value must not be null");

		this.columnName = columnName;
	}

	public String getColumnName()
	{
		return columnName;
	}
}

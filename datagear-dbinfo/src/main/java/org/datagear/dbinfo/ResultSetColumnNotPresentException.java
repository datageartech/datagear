/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.dbinfo;

import java.sql.ResultSet;

/**
 * {@linkplain ResultSet}结果集列缺失异常。
 * 
 * @author datagear@163.com
 *
 */
public class ResultSetColumnNotPresentException extends ResultSetIncompatibleException
{
	private static final long serialVersionUID = 1L;

	/** 缺失列名 */
	private String columnName;

	public ResultSetColumnNotPresentException(String columnName)
	{
		super("Column [" + columnName + "] is not present");

		this.columnName = columnName;
	}

	public String getColumnName()
	{
		return columnName;
	}
}

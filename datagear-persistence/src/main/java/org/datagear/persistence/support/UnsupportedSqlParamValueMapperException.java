/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.support;

import org.datagear.meta.Column;
import org.datagear.meta.Table;
import org.datagear.persistence.SqlParamValueMapperException;
import org.datagear.util.SqlParamValue;

/**
 * 不支持{@linkplain SqlParamValue}映射异常。
 * 
 * @author datagear@163.com
 *
 */
public class UnsupportedSqlParamValueMapperException extends SqlParamValueMapperException
{
	private static final long serialVersionUID = 1L;

	public UnsupportedSqlParamValueMapperException(Table table, Column column, Object value)
	{
		super(table, column, value);
	}

	public UnsupportedSqlParamValueMapperException(Table table, Column column, Object value, Throwable cause)
	{
		super(table, column, value, cause);
	}

	public UnsupportedSqlParamValueMapperException(Table table, Column column, Object value, String message)
	{
		super(table, column, value, message);
	}

	public UnsupportedSqlParamValueMapperException(Table table, Column column, Object value, String message,
			Throwable cause)
	{
		super(table, column, value, message, cause);
	}

}

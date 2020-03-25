/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence;

import org.datagear.util.SqlParamValue;

/**
 * 字面值{@linkplain SqlParamValue}。
 * <p>
 * 此类实例的{@linkplain #getValue()}不是参数值，而是SQL语句。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class LiteralSqlParamValue extends SqlParamValue
{
	public LiteralSqlParamValue()
	{
		super();
	}

	public LiteralSqlParamValue(String value, int type)
	{
		super(value, type);
	}

	@Override
	public String getValue()
	{
		return (String) super.getValue();
	}

	@Override
	public void setValue(Object value)
	{
		if (value != null && !(value instanceof String))
			throw new IllegalArgumentException("[value] must be a string");

		super.setValue(value);
	}
}

/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence;

import java.sql.Connection;

import org.datagear.meta.Column;
import org.datagear.meta.Table;

/**
 * 简单{@linkplain PstValueConverter}。
 * 
 * @author datagear@163.com
 *
 */
public class SimplePstValueConverter implements PstValueConverter
{
	public SimplePstValueConverter()
	{
		super();
	}

	@Override
	public Object convert(Connection cn, Dialect dialect, Table table, Column column, Object value)
			throws PstValueConverterException
	{
		return value;
	}
}

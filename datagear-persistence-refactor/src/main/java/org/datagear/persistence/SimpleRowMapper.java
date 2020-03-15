/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.datagear.meta.Column;
import org.datagear.meta.Table;

/**
 * 简单{@linkplain RowMapper}。
 * 
 * @author datagear@163.com
 *
 */
public class SimpleRowMapper extends PersistenceSupport implements RowMapper
{
	public SimpleRowMapper()
	{
		super();
	}

	@Override
	public Row map(Connection cn, Table table, ResultSet rs, int row) throws RowMapperException
	{
		Row rowObj = new Row();

		try
		{
			Column[] columns = table.getColumns();
			for (int i = 0; i < columns.length; i++)
			{
				Object value = getColumnValue(cn, rs, columns[i]);
				rowObj.put(columns[i].getName(), value);
			}
		}
		catch(SQLException e)
		{
			throw new RowMapperException(e);
		}

		return rowObj;
	}
}

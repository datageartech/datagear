/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.support;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.datagear.meta.Column;
import org.datagear.meta.Table;
import org.datagear.persistence.Row;
import org.datagear.persistence.RowMapper;
import org.datagear.persistence.RowMapperException;

/**
 * 抽象{@linkplain RowMapper}。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractRowMapper extends PersistenceSupport implements RowMapper
{
	public AbstractRowMapper()
	{
		super();
	}

	@Override
	public Row map(Connection cn, Table table, ResultSet rs, int rowIndex) throws RowMapperException
	{
		Row rowObj = new Row();

		try
		{
			Column[] columns = table.getColumns();
			for (int i = 0; i < columns.length; i++)
			{
				Object value = mapColumn(cn, table, rs, rowIndex, columns[i]);
				rowObj.put(columns[i].getName(), value);
			}
		}
		catch (SQLException e)
		{
			throw new RowMapperException(e);
		}

		return rowObj;
	}

	/**
	 * 映射列值。
	 * <p>
	 * 子类应该重写此方法实现映射列值。
	 * </p>
	 * 
	 * @param cn
	 * @param table
	 * @param rs
	 * @param rowIndex
	 * @param column
	 * @return
	 * @throws SQLException
	 * @throws RowMapperException
	 */
	protected Object mapColumn(Connection cn, Table table, ResultSet rs, int rowIndex, Column column)
			throws SQLException, RowMapperException
	{
		throw new UnsupportedOperationException("Not implement");
	}
}

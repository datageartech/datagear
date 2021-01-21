/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.persistence.support;

import java.sql.Connection;
import java.sql.ResultSet;

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
				if (!supportsColumn(columns[i]))
					continue;

				Object value = mapColumn(cn, table, rs, rowIndex, columns[i]);
				rowObj.put(columns[i].getName(), value);
			}
		}
		catch (RowMapperException e)
		{
			throw e;
		}
		catch (Throwable t)
		{
			throw new RowMapperException(t);
		}

		return rowObj;
	}

	/**
	 * 映射列值。
	 * <p>
	 * 注意：{@linkplain #map(Connection, Table, ResultSet, int)}方法已过滤掉不支持的列（参考：{@linkplain #supportsColumn(Column)}）。
	 * </p>
	 * 
	 * @param cn
	 * @param table
	 * @param rs
	 * @param rowIndex
	 * @param column
	 * @return
	 * @throws Throwable
	 */
	protected abstract Object mapColumn(Connection cn, Table table, ResultSet rs, int rowIndex, Column column)
			throws Throwable;
}

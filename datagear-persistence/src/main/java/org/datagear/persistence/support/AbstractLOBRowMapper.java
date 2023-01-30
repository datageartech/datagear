/*
 * Copyright 2018-2023 datagear.tech
 *
 * This file is part of DataGear.
 *
 * DataGear is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * DataGear is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with DataGear.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package org.datagear.persistence.support;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Types;

import org.datagear.meta.Column;
import org.datagear.meta.Table;
import org.datagear.persistence.RowMapper;

/**
 * 抽象LOB、二进制{@linkplain RowMapper}。
 * <p>
 * 此类定义LOB、二进制处理抽象方法。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractLOBRowMapper extends AbstractRowMapper
{
	public AbstractLOBRowMapper()
	{
	}

	@Override
	protected Object mapColumn(Connection cn, Table table, ResultSet rs, int rowIndex, Column column) throws Throwable
	{
		String columnName = column.getName();
		int sqlType = column.getType();

		Object value = null;

		switch (sqlType)
		{
			case Types.LONGVARCHAR:
			{
				value = mapColumnLONGVARCHAR(cn, table, rs, rowIndex, column);
				break;
			}

			case Types.BINARY:
			case Types.VARBINARY:
			{
				value = mapColumnBINARY(cn, table, rs, rowIndex, column);
				break;
			}

			case Types.LONGVARBINARY:
			{
				value = mapColumnLONGVARBINARY(cn, table, rs, rowIndex, column);
				break;
			}

			case Types.CLOB:
			{
				value = mapColumnCLOB(cn, table, rs, rowIndex, column);
				break;
			}

			case Types.BLOB:
			{
				value = mapColumnBLOB(cn, table, rs, rowIndex, column);
				break;
			}

			case Types.LONGNVARCHAR:
			{
				value = mapColumnLONGNVARCHAR(cn, table, rs, rowIndex, column);
				break;
			}

			case Types.NCLOB:
			{
				value = mapColumnNCLOB(cn, table, rs, rowIndex, column);
				break;
			}

			case Types.SQLXML:
			{
				value = mapColumnSQLXML(cn, table, rs, rowIndex, column);
				break;
			}

			default:
				value = getColumnValue(cn, rs, columnName, sqlType);
				break;
		}

		return value;
	}

	protected abstract Object mapColumnLONGVARCHAR(Connection cn, Table table, ResultSet rs, int rowIndex,
			Column column) throws Throwable;

	protected abstract Object mapColumnBINARY(Connection cn, Table table, ResultSet rs, int rowIndex, Column column)
			throws Throwable;

	protected abstract Object mapColumnLONGVARBINARY(Connection cn, Table table, ResultSet rs, int rowIndex,
			Column column) throws Throwable;

	protected abstract Object mapColumnCLOB(Connection cn, Table table, ResultSet rs, int rowIndex, Column column)
			throws Throwable;

	protected abstract Object mapColumnBLOB(Connection cn, Table table, ResultSet rs, int rowIndex, Column column)
			throws Throwable;

	protected abstract Object mapColumnLONGNVARCHAR(Connection cn, Table table, ResultSet rs, int rowIndex,
			Column column) throws Throwable;

	protected abstract Object mapColumnNCLOB(Connection cn, Table table, ResultSet rs, int rowIndex, Column column)
			throws Throwable;

	protected abstract Object mapColumnSQLXML(Connection cn, Table table, ResultSet rs, int rowIndex, Column column)
			throws Throwable;

	protected boolean isNullValue(ResultSet rs, Object value) throws Throwable
	{
		return (value == null || rs.wasNull());
	}
}

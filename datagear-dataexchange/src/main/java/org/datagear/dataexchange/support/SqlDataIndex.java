/*
 * Copyright 2018-present datagear.tech
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

package org.datagear.dataexchange.support;

import org.datagear.dataexchange.DataIndex;
import org.datagear.util.SqlScriptParser.SqlStatement;

/**
 * SQL数据索引。
 * 
 * @author datagear@163.com
 *
 */
public class SqlDataIndex extends DataIndex
{
	private static final long serialVersionUID = 1L;

	private SqlStatement sqlStatement;

	public SqlDataIndex()
	{
		super();
	}

	public SqlDataIndex(SqlStatement sqlStatement)
	{
		super();
		this.sqlStatement = sqlStatement;
	}

	public SqlStatement getSqlStatement()
	{
		return sqlStatement;
	}

	public void setSqlStatement(SqlStatement sqlStatement)
	{
		this.sqlStatement = sqlStatement;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();

		sb.append(this.sqlStatement.getStartRow());
		sb.append(',');
		sb.append(this.sqlStatement.getStartColumn());
		sb.append('-');
		sb.append(this.sqlStatement.getEndRow());
		sb.append(',');
		sb.append(this.sqlStatement.getEndColumn());
		sb.append(' ');

		String sql = this.sqlStatement.getSql();
		int sqlLen = sql.length();

		if (sqlLen <= 10 + 10 + 3)
			sb.append(sql);
		else
		{
			sb.append(sql.substring(0, 10));
			sb.append("...");
			sb.append(sql.substring(sqlLen - 10));
		}

		return sb.toString();
	}

	/**
	 * 构建{@linkplain SqlDataIndex}。
	 * 
	 * @param sqlStatement
	 * @return
	 */
	public static SqlDataIndex valueOf(SqlStatement sqlStatement)
	{
		return new SqlDataIndex(sqlStatement);
	}
}

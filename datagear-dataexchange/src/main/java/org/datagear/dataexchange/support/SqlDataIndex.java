/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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

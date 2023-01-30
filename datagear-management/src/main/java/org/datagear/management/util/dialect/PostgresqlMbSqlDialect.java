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

package org.datagear.management.util.dialect;

/**
 * PostgreSQL 方言。
 * 
 * @author datagear@163.com
 *
 */
public class PostgresqlMbSqlDialect extends MbSqlDialect
{
	public PostgresqlMbSqlDialect()
	{
		super();
	}

	public PostgresqlMbSqlDialect(String identifierQuote)
	{
		super(identifierQuote);
	}

	@Override
	public boolean supportsPaging()
	{
		return true;
	}

	@Override
	public String pagingSqlHead(int index, int fetchSize)
	{
		return "SELECT PGQ.* FROM (";
	}

	@Override
	public String pagingSqlFoot(int index, int fetchSize)
	{
		return ") PGQ LIMIT " + fetchSize + " OFFSET " + index;
	}

	@Override
	public String toStringLiteral(String value)
	{
		String literal = super.toStringLiteral(value);

		// PostgreSQL对于
		// SELECT 'abc' AS STR_LITERAL
		// 格式的SQL语句，查询结果中STR_LITERAL是unknown类型，这会导致如下类似查询语句
		// SELECT * FROM (SELECT 'abc' AS STR_LITERAL) A INNER JOIN SOME_TABLE B
		// ON A.STR_LITERAL = B.SOME_VARCHAR_COLUMN
		// 报
		// failed to find conversion function from unknown to text
		// 错误，无法执行，在末尾添加::text进行类型转换可解决。

		return literal + "::text";
	}
}

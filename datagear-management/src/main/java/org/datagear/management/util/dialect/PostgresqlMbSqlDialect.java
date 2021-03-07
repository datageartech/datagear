/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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

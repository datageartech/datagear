/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.persistence.support.dialect;

import org.datagear.persistence.Order;
import org.datagear.persistence.SqlBuilder;
import org.datagear.persistence.support.AbstractDialect;

/**
 * Oracle方言。
 * 
 * @author datagear@163.com
 *
 */
public class OracleDialect extends AbstractDialect
{
	public OracleDialect()
	{
		super();
	}

	public OracleDialect(String identifierQuote)
	{
		super(identifierQuote);
	}

	@Override
	public boolean supportsPagingSql()
	{
		return true;
	}

	@Override
	public SqlBuilder toPagingSql(SqlBuilder queryView, SqlBuilder condition, Order[] orders, long startRow, int count)
	{
		SqlBuilder sql = SqlBuilder.valueOf();

		SqlBuilder orderSql = toOrderSql(orders);

		sql.sql("SELECT T2.* FROM (SELECT T1.*, ROWNUM AS ROWNUM_____ FROM (");

		if (isEmptySql(condition) && isEmptySql(orderSql))
		{
			sql.sql(queryView);
		}
		else
		{
			sql.sql("SELECT * FROM (");
			sql.sql(queryView);
			sql.sql(") T0 ");

			if (!isEmptySql(condition))
			{
				sql.sql(" WHERE ");
				sql.sql(condition);
			}

			if (!isEmptySql(orderSql))
			{
				sql.sql(" ORDER BY ");
				sql.sql(orderSql);
			}
		}

		sql.sql(") T1 WHERE ROWNUM < " + (startRow + count));
		sql.sql(") T2 WHERE ROWNUM_____ >= " + startRow);

		return sql;
	}
}
